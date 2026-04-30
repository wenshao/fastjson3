package com.alibaba.fastjson3;

import com.alibaba.fastjson3.filter.NameFilter;
import com.alibaba.fastjson3.filter.PropertyFilter;
import com.alibaba.fastjson3.filter.ValueFilter;
import com.alibaba.fastjson3.modules.ObjectReaderModule;
import com.alibaba.fastjson3.modules.ObjectWriterModule;
import com.alibaba.fastjson3.reader.ASMObjectReaderProvider;
import com.alibaba.fastjson3.reader.AutoObjectReaderProvider;
import com.alibaba.fastjson3.reader.ObjectReaderCreator;
import com.alibaba.fastjson3.reader.ObjectReaderProvider;
import com.alibaba.fastjson3.reader.ReaderCreatorType;
import com.alibaba.fastjson3.reader.ReflectObjectReaderProvider;
import com.alibaba.fastjson3.writer.ASMObjectWriterProvider;
import com.alibaba.fastjson3.writer.AutoObjectWriterProvider;
import com.alibaba.fastjson3.writer.ObjectWriterCreator;
import com.alibaba.fastjson3.writer.ObjectWriterProvider;
import com.alibaba.fastjson3.writer.ReflectObjectWriterProvider;
import com.alibaba.fastjson3.writer.WriterCreatorType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Thread-safe JSON object mapper. Central entry point for JSON-to-Object
 * and Object-to-JSON conversion with full type support.
 *
 * <p>Configuration (features, modules, filters) is immutable after construction via {@link Builder}.
 * The reader/writer caches are thread-safe and lazily populated.
 * {@link #registerReader} and {@link #registerWriter} allow runtime registration
 * of custom codecs on the shared cache.</p>
 *
 * <p>Replaces fastjson2's ObjectReaderProvider + ObjectWriterProvider with a single unified API,
 * inspired by Jackson 3's immutable ObjectMapper design.</p>
 *
 * <h3>Basic usage:</h3>
 * <pre>
 * // Use shared default instance
 * ObjectMapper mapper = ObjectMapper.shared();
 * User user = mapper.readValue("{\"name\":\"test\"}", User.class);
 * String json = mapper.writeValueAsString(user);
 * </pre>
 *
 * <h3>Using presets for common configurations:</h3>
 * <pre>
 * // Lenient parsing for config files
 * ObjectMapper lenient = ObjectMapper.builder(Preset.LENIENT).build();
 *
 * // Strict validation for APIs
 * ObjectMapper strict = ObjectMapper.builder(Preset.STRICT).build();
 *
 * // Pretty output for logging
 * ObjectMapper pretty = ObjectMapper.builder(Preset.PRETTY).build();
 * </pre>
 *
 * <h3>Custom configuration (immutable builder):</h3>
 * <pre>
 * ObjectMapper mapper = ObjectMapper.builder()
 *     .enableRead(ReadFeature.AllowComments, ReadFeature.SupportSmartMatch)
 *     .enableWrite(WriteFeature.PrettyFormat, WriteFeature.WriteNulls)
 *     .addReaderModule(myModule)
 *     .build();
 *
 * // Derive variant from existing mapper
 * ObjectMapper pretty = mapper.rebuild()
 *     .enableWrite(WriteFeature.PrettyFormat)
 *     .build();
 * </pre>
 *
 * <h3>ASM vs Reflection strategy:</h3>
 * <pre>
 * // AUTO (default): automatically choose best strategy
 * ObjectMapper mapper = ObjectMapper.builder()
 *     .readerCreatorType(ReaderCreatorType.AUTO)
 *     .build();
 *
 * // ASM: force bytecode generation (including nested types)
 * ObjectMapper asmMapper = ObjectMapper.builder()
 *     .readerCreatorType(ReaderCreatorType.ASM)
 *     .build();
 *
 * // REFLECT: force reflection only
 * ObjectMapper reflectMapper = ObjectMapper.builder()
 *     .readerCreatorType(ReaderCreatorType.REFLECT)
 *     .build();
 * </pre>
 *
 * <h3>Per-call configuration (mutant factory):</h3>
 * <pre>
 * // Create a per-call reader with extra features
 * User user = mapper.readerFor(User.class)
 *     .with(ReadFeature.SupportSmartMatch)
 *     .readValue(json);
 *
 * // Create a per-call writer with extra features
 * String json = mapper.writerFor(User.class)
 *     .with(WriteFeature.PrettyFormat)
 *     .writeValueAsString(user);
 * </pre>
 */
public final class ObjectMapper {
    private static final PropertyFilter[] NO_PROPERTY_FILTERS = {};
    private static final ValueFilter[] NO_VALUE_FILTERS = {};
    private static final NameFilter[] NO_NAME_FILTERS = {};

    /**
     * Quick ASCII check for byte[]. Android-safe: no Unsafe dependency, uses simple byte loop.
     */
    private static boolean isAscii(byte[] bytes) {
        final int len = bytes.length;
        int off = 0;
        // SWAR: 8-byte batch check for high-bit (non-ASCII marker).
        final int swarLimit = len - 7;
        while (off < swarLimit) {
            long w = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(bytes, off);
            if ((w & 0x8080808080808080L) != 0) return false;
            off += 8;
        }
        while (off < len) {
            if (bytes[off] < 0) return false;
            off++;
        }
        return true;
    }

    private static final ObjectMapper SHARED = new ObjectMapper(0, 0,
            Collections.<ObjectReaderModule>emptyList(),
            Collections.<ObjectWriterModule>emptyList(),
            null, null,
            NO_PROPERTY_FILTERS, NO_VALUE_FILTERS, NO_NAME_FILTERS,
            null,
            Collections.<Class<?>, Class<?>>emptyMap(),
            false,
            null,
            null,
            com.alibaba.fastjson3.util.DynamicClassLoader.getSharedInstance(),
            null, null, null);

    private final long readFeatures;
    private final long writeFeatures;
    private final List<ObjectReaderModule> readerModules;
    private final List<ObjectWriterModule> writerModules;

    // Creator functions: Class -> ObjectReader/ObjectWriter
    // null means use default auto-detection (ASM > Reflection)
    private final Function<Class<?>, ObjectReader<?>> readerCreator;
    private final Function<Class<?>, ObjectWriter<?>> writerCreator;

    // ObjectReaderProvider for ASM/Reflection strategy control
    private final ObjectReaderProvider readerProvider;

    // Fast-path flag: true only for custom providers that need context propagation
    // Default providers (ReflectObjectReaderProvider, AutoObjectReaderProvider) don't need context
    private final boolean needsReaderContext;

    // ObjectWriterProvider for ASM/Reflection strategy control
    private final ObjectWriterProvider writerProvider;

    // DynamicClassLoader for ASM-generated classes (one per ObjectMapper)
    // This ensures generated classes can be unloaded when the ObjectMapper is GC'd
    private final com.alibaba.fastjson3.util.DynamicClassLoader classLoader;

    // Filters (empty arrays = no overhead)
    private com.alibaba.fastjson3.filter.BeforeFilter[] beforeFilters;
    private com.alibaba.fastjson3.filter.AfterFilter[] afterFilters;
    private com.alibaba.fastjson3.filter.PropertyPreFilter[] propertyPreFilters;
    private final PropertyFilter[] propertyFilters;
    private final ValueFilter[] valueFilters;
    private final NameFilter[] nameFilters;
    private final com.alibaba.fastjson3.filter.LabelFilter labelFilter;

    // Mixin mappings: target class → mixin source class
    // ConcurrentHashMap to support runtime addMixIn() registration
    private final java.util.concurrent.ConcurrentHashMap<Class<?>, Class<?>> mixInCache;

    // Optional Jackson annotation support (off by default)
    private final boolean useJacksonAnnotation;

    // Per-instance Map / List supplier overrides (fj2-compat). null = use
    // JSONObject.setMapCreator() global / JSONObjectMap default.
    private final java.util.function.Supplier<? extends java.util.Map<String, Object>> mapSupplier;
    private final java.util.function.Supplier<? extends java.util.List<Object>> listSupplier;

    // Mapper-level default date/time format. Field-level @JSONField(format=)
    // wins over this; applies to fields that have no field-level override.
    // null = use fastjson3's default ISO-8601 emit per Temporal type.
    private final String dateFormat;
    private final com.alibaba.fastjson3.util.DateFormatPattern dateFormatPattern;

    // Holder for ObjectReader that supports invalidation via cleanup
    private static final class ReaderHolder {
        final ObjectReader<?> reader;
        final int cacheVersion;  // Track which cache generation created this holder

        ReaderHolder(ObjectReader<?> reader, int cacheVersion) {
            this.reader = reader;
            this.cacheVersion = cacheVersion;
        }

        ObjectReader<?> get() {
            return reader;
        }
    }

    // Cache version for cleanup visibility - increments on each cleanup()
    private final java.util.concurrent.atomic.AtomicInteger cacheVersion = new java.util.concurrent.atomic.AtomicInteger(0);

    // Map of Class -> ReaderHolder (for cleanup visibility - cleared on cleanup())
    private final ConcurrentHashMap<Class<?>, ReaderHolder> readerHolderMap = new ConcurrentHashMap<>();

    // Thread-safe caches for ObjectReader/ObjectWriter instances
    private final ConcurrentHashMap<Type, ObjectReader<?>> readerCache;
    private final ConcurrentHashMap<Type, ObjectWriter<?>> writerCache;
    // Fast Class→Reader cache: ClassValue lookup ~3ns vs ConcurrentHashMap ~20ns (fory-style)
    // Uses ReaderHolder indirection for cleanup visibility.
    private final ClassValue<ReaderHolder> readerClassCache = new ClassValue<>() {
        @Override
        protected ReaderHolder computeValue(Class<?> type) {
            // Check readerCache first (may contain post-cleanup recomputed values)
            ObjectReader<?> cached = readerCache.get(type);
            if (cached != null) {
                return new ReaderHolder(cached, cacheVersion.get());
            }

            // Check holder map (fast path for already-created readers)
            ReaderHolder existing = readerHolderMap.get(type);
            if (existing != null && existing.cacheVersion == cacheVersion.get()) {
                return existing;
            }

            // Try modules first (user-provided readers)
            for (ObjectReaderModule module : readerModules) {
                ObjectReader<?> reader = module.getObjectReader(type);
                if (reader != null) {
                    ReaderHolder holder = new ReaderHolder(reader, cacheVersion.get());
                    readerHolderMap.put(type, holder);
                    return holder;
                }
            }

            // Try built-in codecs (Optional, UUID, Duration, etc.)
            ObjectReader<?> reader = BuiltinCodecs.getReader(type);
            if (reader != null) {
                ReaderHolder holder = new ReaderHolder(reader, cacheVersion.get());
                readerHolderMap.put(type, holder);
                return holder;
            }

            // Basic types don't need custom readers
            if (isBasicType(type)) {
                return null;
            }

            // Auto-create POJO reader (ASM or reflection)
            try {
                if (useJacksonAnnotation || !mixInCache.isEmpty()) {
                    // When annotations/mixIns are needed, use ObjectReaderCreator
                    // directly because the ASM reader doesn't process mixIn
                    // overlays or Jackson annotations.
                    Class<?> mixIn = mixInCache.get(type);
                    reader = ObjectReaderCreator.createObjectReader(type, mixIn, useJacksonAnnotation);
                } else if (readerCreator != null) {
                    reader = readerCreator.apply(type);
                } else {
                    // Delegate to provider (AUTO picks ASM for simple POJOs,
                    // falls back to REFLECT for complex types / anySetter /
                    // BuiltinCodecs fields, etc.)
                    reader = readerProvider.getObjectReader(type);
                }
                if (reader != null) {
                    ReaderHolder holder = new ReaderHolder(reader, cacheVersion.get());
                    readerHolderMap.put(type, holder);
                    return holder;
                }
            } catch (Exception e) {
                // Log but don't fail - allow fallback to default handling
                com.alibaba.fastjson3.util.Logger.warn("Failed to create ObjectReader for " + type.getName() + ": " + e.getMessage());
            }
            return null;
        }
    };
    // Fast Class→Writer cache: ClassValue lookup ~3ns vs ConcurrentHashMap ~20ns (fory-style)
    // Note: writerCache is checked first in getObjectWriter() to support runtime registerWriter() overrides.
    private final ClassValue<ObjectWriter<?>> writerClassCache = new ClassValue<>() {
        @Override
        protected ObjectWriter<?> computeValue(Class<?> type) {
            // Defensive: check writerCache in case a race condition put a value during compute
            ObjectWriter<?> cached = writerCache.get(type);
            if (cached != null) {
                return cached;
            }
            // Delegate to slow path which contains all writer creation logic
            // Note: type is always Class<?> here, but slow path handles it correctly
            return getObjectWriterSlow(type);
        }
    };

    private ObjectMapper(
            long readFeatures,
            long writeFeatures,
            List<ObjectReaderModule> readerModules,
            List<ObjectWriterModule> writerModules,
            Function<Class<?>, ObjectReader<?>> readerCreator,
            Function<Class<?>, ObjectWriter<?>> writerCreator,
            PropertyFilter[] propertyFilters,
            ValueFilter[] valueFilters,
            NameFilter[] nameFilters,
            com.alibaba.fastjson3.filter.LabelFilter labelFilter,
            Map<Class<?>, Class<?>> mixInCache,
            boolean useJacksonAnnotation,
            ObjectReaderProvider readerProvider,
            ObjectWriterProvider writerProvider,
            com.alibaba.fastjson3.util.DynamicClassLoader classLoader,
            java.util.function.Supplier<? extends java.util.Map<String, Object>> mapSupplier,
            java.util.function.Supplier<? extends java.util.List<Object>> listSupplier,
            String dateFormat
    ) {
        this.readFeatures = readFeatures;
        this.writeFeatures = writeFeatures;
        this.readerModules = readerModules;
        this.writerModules = writerModules;
        this.readerCreator = readerCreator;
        this.writerCreator = writerCreator;
        this.propertyFilters = propertyFilters;
        this.valueFilters = valueFilters;
        this.nameFilters = nameFilters;
        this.labelFilter = labelFilter;
        this.mixInCache = new java.util.concurrent.ConcurrentHashMap<>(mixInCache);
        this.useJacksonAnnotation = useJacksonAnnotation;
        this.mapSupplier = mapSupplier;
        this.listSupplier = listSupplier;
        this.dateFormat = dateFormat;
        this.dateFormatPattern = com.alibaba.fastjson3.util.DateFormatPattern.of(dateFormat);
        this.classLoader = classLoader != null ? classLoader
            : com.alibaba.fastjson3.util.DynamicClassLoader.getSharedInstance();

        // Use custom provider if provided, otherwise use default
        // The Builder.build() method creates per-instance providers when readerCreatorType is set
        this.readerProvider = readerProvider != null
            ? readerProvider
            : ObjectReaderProvider.defaultProvider();

        // Use custom writer provider if provided, otherwise use default
        // The Builder.build() method creates per-instance providers when writerCreatorType is set
        this.writerProvider = writerProvider != null
            ? writerProvider
            : ObjectWriterProvider.defaultProvider();

        // Fast-path: context propagation only needed for custom ASM providers
        // Default providers don't need context since they don't do recursive ASM generation
        this.needsReaderContext = needsContext(this.readerProvider);

        this.readerCache = new ConcurrentHashMap<Type, ObjectReader<?>>();
        this.writerCache = new ConcurrentHashMap<Type, ObjectWriter<?>>();
    }

    /**
     * Check if a provider needs context propagation.
     *
     * <p>The {@code ObjectReaderProvider.CONTEXT} ThreadLocal is only used by
     * {@code openContext()} itself (to save the previous value for nesting).
     * No parser or reader actually reads the context during a parse, so the
     * per-parse ThreadLocal.get / set / remove overhead from
     * {@code readerProvider.openContext()} is pure overhead — measured at
     * roughly 20% of CPU time on Eishay-shape input via JFR profiling after
     * PR #75 closed the structural gap. As of Phase B6 this method returns
     * {@code false} for all built-in provider classes, including any custom
     * instance of {@link ASMObjectReaderProvider} created by
     * {@link Builder#readerCreatorType(ReaderCreatorType)}. External custom
     * subclasses still get context for backwards compatibility.</p>
     */
    private static boolean needsContext(ObjectReaderProvider provider) {
        if (provider == null) {
            return false;
        }
        Class<?> cls = provider.getClass();
        if (cls == ReflectObjectReaderProvider.class
                || cls == AutoObjectReaderProvider.class
                || cls == ASMObjectReaderProvider.class) {
            return false;
        }
        return true;
    }

    /**
     * Create a provider instance with a specific classloader.
     * This ensures generated classes can be unloaded when the ObjectMapper is discarded.
     */
    private static ObjectReaderProvider createProviderWithClassLoader(
            ReaderCreatorType creatorType,
            com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        return switch (creatorType) {
            case AUTO -> new AutoObjectReaderProvider(classLoader);
            case ASM -> new ASMObjectReaderProvider(classLoader);
            case REFLECT -> new ReflectObjectReaderProvider();
        };
    }

    /**
     * Create a writer provider instance with a specific classloader.
     * This ensures generated classes can be unloaded when the ObjectMapper is discarded.
     */
    private static ObjectWriterProvider createWriterProviderWithClassLoader(
            WriterCreatorType creatorType,
            com.alibaba.fastjson3.util.DynamicClassLoader classLoader) {
        return switch (creatorType) {
            case AUTO -> new AutoObjectWriterProvider(classLoader);
            case ASM -> new ASMObjectWriterProvider(classLoader);
            case REFLECT -> new ReflectObjectWriterProvider(classLoader);
        };
    }

    // ==================== Factory methods ====================

    /**
     * Get the shared default mapper (no features, no custom modules).
     * Thread-safe singleton — suitable for most use cases.
     */
    public static ObjectMapper shared() {
        return SHARED;
    }

    /**
     * Create a new builder for configuring a custom mapper.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder with a preset configuration.
     * Presets provide common configurations for typical use cases.
     *
     * <pre>
     * // Lenient: allow comments, single quotes, unquoted fields
     * ObjectMapper lenient = ObjectMapper.builder(Preset.LENIENT).build();
     *
     * // Strict: error on unknown properties, null for primitives
     * ObjectMapper strict = ObjectMapper.builder(Preset.STRICT).build();
     *
     * // Pretty: human-readable output with indentation
     * ObjectMapper pretty = ObjectMapper.builder(Preset.PRETTY).build();
     *
     * // Compat: browser-compatible, pretty format
     * ObjectMapper compat = ObjectMapper.builder(Preset.COMPAT).build();
     * </pre>
     *
     * @param preset the preset configuration to apply
     * @return a builder pre-configured with the preset
     * @see Preset
     */
    public static Builder builder(Preset preset) {
        return preset.applyTo(new Builder());
    }

    /**
     * Predefined configurations for common use cases.
     * These presets provide sensible defaults without requiring
     * knowledge of individual features.
     *
     * <table border="1">
     * <caption>Presets overview</caption>
     * <tr><th>Preset</th><th>Description</th></tr>
     * <tr><td>{@link #DEFAULT}</td><td>Standard JSON parsing (no special features)</td></tr>
     * <tr><td>{@link #LENIENT}</td><td>Permissive parsing for configs/user input</td></tr>
     * <tr><td>{@link #STRICT}</td><td>Strict validation for API contracts</td></tr>
     * <tr><td>{@link #PRETTY}</td><td>Human-readable output</td></tr>
     * <tr><td>{@link #COMPAT}</td><td>Maximum compatibility (browser + lenient)</td></tr>
     * </table>
     */
    public enum Preset {
        /**
         * Standard JSON parsing with default settings.
         * Suitable for most general-purpose JSON processing.
         */
        DEFAULT(b -> b),

        /**
         * Lenient parsing for configuration files and user input.
         * Enables: comments, single quotes, unquoted field names.
         */
        LENIENT(b -> b
            .enableRead(
                ReadFeature.AllowComments,
                ReadFeature.AllowSingleQuotes,
                ReadFeature.AllowUnquotedFieldNames,
                ReadFeature.SupportSmartMatch
            )),

        /**
         * Strict validation for API contracts and data exchange.
         * Enables: error on unknown properties, error on null for primitives.
         */
        STRICT(b -> b
            .enableRead(
                ReadFeature.ErrorOnUnknownProperties,
                ReadFeature.ErrorOnNullForPrimitives
            )),

        /**
         * Pretty formatted output for logging and debugging.
         * Enables: pretty format.
         */
        PRETTY(b -> b
            .enableWrite(WriteFeature.PrettyFormat)),

        /**
         * Maximum compatibility for web browsers and lenient parsing.
         * Enables: browser compatible, pretty format, escape non-ASCII.
         */
        COMPAT(b -> b
            .enableRead(
                ReadFeature.AllowComments,
                ReadFeature.AllowSingleQuotes,
                ReadFeature.SupportSmartMatch
            )
            .enableWrite(
                WriteFeature.PrettyFormat,
                WriteFeature.BrowserCompatible,
                WriteFeature.EscapeNoneAscii
            ));

        private final java.util.function.Function<Builder, Builder> configurator;

        Preset(java.util.function.Function<Builder, Builder> configurator) {
            this.configurator = configurator;
        }

        Builder applyTo(Builder builder) {
            return configurator.apply(builder);
        }
    }

    /**
     * Create a new builder pre-configured with this mapper's settings.
     * Use this to derive variants from an existing mapper.
     */
    public Builder rebuild() {
        Builder b = new Builder();
        b.readFeatures = this.readFeatures;
        b.writeFeatures = this.writeFeatures;
        b.readerModules.addAll(this.readerModules);
        b.writerModules.addAll(this.writerModules);
        b.readerCreator = this.readerCreator;
        b.writerCreator = this.writerCreator;
        b.readerCreatorType = this.readerProvider.getCreatorType();
        b.writerCreatorType = this.writerProvider.getCreatorType();
        b.classLoader = this.classLoader; // Preserve classLoader for rebuild
        Collections.addAll(b.propertyFilters, this.propertyFilters);
        Collections.addAll(b.valueFilters, this.valueFilters);
        Collections.addAll(b.nameFilters, this.nameFilters);
        b.mixIns.putAll(this.mixInCache);
        b.labelFilter = this.labelFilter;
        b.useJacksonAnnotation = this.useJacksonAnnotation;
        b.mapSupplier = this.mapSupplier;
        b.listSupplier = this.listSupplier;
        b.dateFormat = this.dateFormat;
        return b;
    }

    /**
     * Mapper-level date/time format string, or {@code null} if not configured.
     * Field-level {@code @JSONField(format=...)} overrides this.
     *
     * @see Builder#dateFormat(String)
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Pre-classified strategy for {@link #getDateFormat()}, or {@code null}
     * when no format is set. Used by the writer dispatch path.
     */
    public com.alibaba.fastjson3.util.DateFormatPattern getDateFormatPattern() {
        return dateFormatPattern;
    }

    /**
     * Apply per-mapper Map / List suppliers (if configured) to the given
     * parser. Called from each {@code readValue} entry point so the
     * untyped object/array readers materialise into supplier-provided
     * backing storage.
     */
    private void applySuppliers(JSONParser parser) {
        if (mapSupplier != null) {
            parser.setMapSupplier(mapSupplier);
        }
        if (listSupplier != null) {
            parser.setListSupplier(listSupplier);
        }
    }

    // ==================== Read: String input ====================

    /**
     * Parse JSON string to auto-detected type (JSONObject, JSONArray, String, Number, Boolean, null).
     */
    public Object readValue(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(json, readFeatures)) {
            applySuppliers(parser);
            return parser.readAny();
        }
    }

    /**
     * Parse JSON string to typed Java object.
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(String json, Class<T> type) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        // Convert String to UTF-8 bytes to use optimized UTF-8 parser with ASM ObjectReader
        byte[] jsonBytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return readValue(jsonBytes, type);
    }

    /**
     * Parse JSON string to generic type.
     *
     * <pre>
     * List&lt;User&gt; users = mapper.readValue(json, new TypeReference&lt;List&lt;User&gt;&gt;(){}.getType());
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(String json, Type type) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        // Convert String to UTF-8 bytes to use optimized UTF-8 parser with ASM ObjectReader
        byte[] jsonBytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return readValue(jsonBytes, type);
    }

    /**
     * Parse JSON string to generic type using TypeReference.
     *
     * <pre>
     * List&lt;User&gt; users = mapper.readValue(json, new TypeReference&lt;List&lt;User&gt;&gt;(){});
     * </pre>
     */
    public <T> T readValue(String json, TypeReference<T> typeRef) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        // Convert String to UTF-8 bytes to use optimized UTF-8 parser with ASM ObjectReader
        byte[] jsonBytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return readValue(jsonBytes, typeRef.getType());
    }

    // ==================== Read: byte[] input ====================

    /**
     * Parse UTF-8 JSON bytes to typed Java object.
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(byte[] jsonBytes, Class<T> type) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        ObjectReader<T> objectReader = (ObjectReader<T>) getObjectReader(type);
        if (objectReader != null) {
            // JSONParser.of(byte[],...) always returns a UTF8; call the
            // UTF8-specialized readObjectUTF8 directly to skip the generic
            // readObject → instanceof-check → readObjectUTF8 wrapper emitted
            // by the ASM generator. Measured ~6% of ARM CPU time on Eishay
            // was spent in that wrapper alone.
            if (needsReaderContext) {
                try (JSONParser parser = JSONParser.of(jsonBytes, readFeatures);
                     ObjectReaderProvider.SafeContext ctx = readerProvider.openContext()) {
                    applySuppliers(parser);
                    return objectReader.readObjectUTF8((JSONParser.UTF8) parser, readFeatures);
                }
            } else {
                try (JSONParser parser = JSONParser.of(jsonBytes, readFeatures)) {
                    applySuppliers(parser);
                    return objectReader.readObjectUTF8((JSONParser.UTF8) parser, readFeatures);
                }
            }
        }
        if (needsReaderContext) {
            try (JSONParser parser = JSONParser.of(jsonBytes, readFeatures);
                 ObjectReaderProvider.SafeContext ctx = readerProvider.openContext()) {
                applySuppliers(parser);
                return parser.read(type);
            }
        } else {
            try (JSONParser parser = JSONParser.of(jsonBytes, readFeatures)) {
                applySuppliers(parser);
                return parser.read(type);
            }
        }
    }

    /**
     * Parse UTF-8 JSON bytes to generic type.
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(byte[] jsonBytes, Type type) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        ObjectReader<T> objectReader = (ObjectReader<T>) getObjectReader(type);
        if (objectReader != null) {
            if (needsReaderContext) {
                try (JSONParser parser = JSONParser.of(jsonBytes, readFeatures);
                     ObjectReaderProvider.SafeContext ctx = readerProvider.openContext()) {
                    applySuppliers(parser);
                    return objectReader.readObject(parser, type, null, readFeatures);
                }
            } else {
                try (JSONParser parser = JSONParser.of(jsonBytes, readFeatures)) {
                    applySuppliers(parser);
                    return objectReader.readObject(parser, type, null, readFeatures);
                }
            }
        }
        if (needsReaderContext) {
            try (JSONParser parser = JSONParser.of(jsonBytes, readFeatures);
                 ObjectReaderProvider.SafeContext ctx = readerProvider.openContext()) {
                applySuppliers(parser);
                return parser.read(type);
            }
        } else {
            try (JSONParser parser = JSONParser.of(jsonBytes, readFeatures)) {
                applySuppliers(parser);
                return parser.read(type);
            }
        }
    }

    /**
     * Parse UTF-8 JSON bytes to generic type using TypeReference.
     */
    public <T> T readValue(byte[] jsonBytes, TypeReference<T> typeRef) {
        return readValue(jsonBytes, typeRef.getType());
    }

    // ==================== Read: InputStream input ====================

    /**
     * Parse JSON from InputStream (UTF-8) to typed Java object.
     * Reads all bytes then delegates to the byte[] path.
     */
    public <T> T readValue(InputStream in, Class<T> type) {
        return readValue(readAllBytes(in), type);
    }

    /**
     * Parse JSON from InputStream (UTF-8) to generic type.
     */
    public <T> T readValue(InputStream in, Type type) {
        return readValue(readAllBytes(in), type);
    }

    /**
     * Parse JSON from InputStream (UTF-8) to generic type using TypeReference.
     */
    public <T> T readValue(InputStream in, TypeReference<T> typeRef) {
        return readValue(readAllBytes(in), typeRef.getType());
    }

    // ==================== Read: Reader input ====================

    /**
     * Parse JSON from Reader to typed Java object.
     * Reads all characters then delegates to the String path.
     */
    public <T> T readValue(Reader reader, Class<T> type) {
        return readValue(readAllChars(reader), type);
    }

    /**
     * Parse JSON from Reader to generic type.
     */
    public <T> T readValue(Reader reader, Type type) {
        return readValue(readAllChars(reader), type);
    }

    /**
     * Parse JSON from Reader to generic type using TypeReference.
     */
    public <T> T readValue(Reader reader, TypeReference<T> typeRef) {
        return readValue(readAllChars(reader), typeRef.getType());
    }

    // ==================== Read: JSONObject / JSONArray ====================

    /**
     * Parse JSON string to JSONObject.
     */
    public JSONObject readObject(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(json, readFeatures)) {
            applySuppliers(parser);
            return parser.readObject();
        }
    }

    /**
     * Parse UTF-8 JSON bytes to JSONObject.
     */
    public JSONObject readObject(byte[] jsonBytes) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        // ASCII fast path: use LATIN1 parser (has optimized inline tree parsing)
        if (readFeatures == 0 && isAscii(jsonBytes)) {
            try (JSONParser parser = new JSONParser.LATIN1(jsonBytes, 0, jsonBytes.length, 0)) {
                applySuppliers(parser);
                return parser.readObject();
            }
        }
        try (JSONParser parser = JSONParser.of(jsonBytes, readFeatures)) {
            applySuppliers(parser);
            return parser.readObject();
        }
    }

    /**
     * Parse JSON string to JSONArray.
     */
    public JSONArray readArray(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(json, readFeatures)) {
            applySuppliers(parser);
            return parser.readArray();
        }
    }

    /**
     * Parse UTF-8 JSON bytes to JSONArray.
     */
    public JSONArray readArray(byte[] jsonBytes) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        if (readFeatures == 0 && isAscii(jsonBytes)) {
            try (JSONParser parser = new JSONParser.LATIN1(jsonBytes, 0, jsonBytes.length, 0)) {
                applySuppliers(parser);
                return parser.readArray();
            }
        }
        try (JSONParser parser = JSONParser.of(jsonBytes, readFeatures)) {
            applySuppliers(parser);
            return parser.readArray();
        }
    }

    // ==================== Read: typed list ====================

    /**
     * Parse JSON string to typed list.
     *
     * <pre>
     * List&lt;User&gt; users = mapper.readList(json, User.class);
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> readList(String json, Class<T> type) {
        return readList(json, type, readFeatures);
    }

    /**
     * Parse JSON string to typed list with custom features.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> readList(String json, Class<T> type, long features) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(json, features)) {
            applySuppliers(parser);
            JSONArray array = parser.readArray();
            if (array == null) {
                return null;
            }
            ObjectReader<T> objectReader = (ObjectReader<T>) getObjectReader(type);
            List<T> list = new ArrayList<>(array.size());
            for (int i = 0, size = array.size(); i < size; i++) {
                Object item = array.get(i);
                if (item == null) {
                    list.add(null);
                } else if (type.isInstance(item)) {
                    list.add(type.cast(item));
                } else if (item instanceof JSONObject jsonObj && objectReader != null) {
                    // Convert JSONObject to target type via ObjectReader
                    String itemJson = JSON.toJSONString(jsonObj);
                    try (JSONParser itemParser = JSONParser.of(itemJson, features)) {
                        list.add(objectReader.readObject(itemParser, type, null, features));
                    }
                } else {
                    list.add(coerceElement(type, item));
                }
            }
            return list;
        }
    }

    /**
     * Assignment-fallback for the collection read paths. When the raw JSON element
     * isn't assignable to the declared element type AND we don't have an
     * ObjectReader for materialisation, the only remaining option is the unchecked
     * cast — but for the specific case of a {@code JSONObject} node targeted at an
     * abstract or interface element type, that unchecked cast silently succeeds and
     * surfaces later as a confusing {@code ClassCastException}. Raise the targeted
     * polymorphic-registration error at the point of first hit so users get an
     * actionable message; otherwise preserve the pre-existing unchecked-cast
     * behaviour (empty collections, Number/CharSequence supertypes, null-only, etc.
     * already handled by the caller's fast branches).
     */
    @SuppressWarnings("unchecked")
    private static <T> T coerceElement(Class<T> type, Object item) {
        if (item instanceof JSONObject
                && (type.isInterface() || java.lang.reflect.Modifier.isAbstract(type.getModifiers()))) {
            throw new JSONException("cannot deserialize collection values of "
                    + (type.isInterface() ? "interface" : "abstract class") + " " + type.getName()
                    + ": register subtypes via @JSONType(seeAlso=..., typeKey=...),"
                    + " make the type sealed, or use Jackson @JsonTypeInfo + @JsonSubTypes");
        }
        return (T) item;
    }

    /**
     * Parse JSON bytes (UTF-8) to typed list.
     */
    public <T> List<T> readList(byte[] jsonBytes, Class<T> type) {
        return readList(jsonBytes, type, readFeatures);
    }

    /**
     * Parse JSON bytes (UTF-8) to typed list with custom features.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> readList(byte[] jsonBytes, Class<T> type, long features) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(jsonBytes, features)) {
            applySuppliers(parser);
            JSONArray array = parser.readArray();
            if (array == null) {
                return null;
            }
            ObjectReader<T> objectReader = (ObjectReader<T>) getObjectReader(type);
            List<T> list = new ArrayList<>(array.size());
            for (int i = 0, size = array.size(); i < size; i++) {
                Object item = array.get(i);
                if (item == null) {
                    list.add(null);
                } else if (type.isInstance(item)) {
                    list.add(type.cast(item));
                } else if (item instanceof JSONObject jsonObj && objectReader != null) {
                    String itemJson = JSON.toJSONString(jsonObj);
                    try (JSONParser itemParser = JSONParser.of(itemJson, features)) {
                        list.add(objectReader.readObject(itemParser, type, null, features));
                    }
                } else {
                    list.add(coerceElement(type, item));
                }
            }
            return list;
        }
    }

    // ==================== Read: typed set ====================

    /**
     * Parse JSON string to typed set.
     *
     * <pre>
     * Set&lt;User&gt; users = mapper.readSet(json, User.class);
     * </pre>
     */
    public <T> java.util.Set<T> readSet(String json, Class<T> type) {
        return readSet(json, type, readFeatures);
    }

    /**
     * Parse JSON string to typed set with custom features.
     */
    public <T> java.util.Set<T> readSet(String json, Class<T> type, long features) {
        List<T> list = readList(json, type, features);
        return list == null ? null : new java.util.LinkedHashSet<>(list);
    }

    /**
     * Parse JSON bytes (UTF-8) to typed set.
     */
    public <T> java.util.Set<T> readSet(byte[] jsonBytes, Class<T> type) {
        return readSet(jsonBytes, type, readFeatures);
    }

    /**
     * Parse JSON bytes (UTF-8) to typed set with custom features.
     */
    public <T> java.util.Set<T> readSet(byte[] jsonBytes, Class<T> type, long features) {
        List<T> list = readList(jsonBytes, type, features);
        return list == null ? null : new java.util.LinkedHashSet<>(list);
    }

    // ==================== Read: typed map ====================

    /**
     * Parse JSON string to typed map.
     * <p>Note: JSON object keys are always strings.</p>
     *
     * <pre>
     * Map&lt;String, User&gt; users = mapper.readMap(json, User.class);
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public <V> java.util.Map<String, V> readMap(String json, Class<V> valueType) {
        return readMap(json, valueType, readFeatures);
    }

    /**
     * Parse JSON string to typed map with custom features.
     */
    @SuppressWarnings("unchecked")
    public <V> java.util.Map<String, V> readMap(String json, Class<V> valueType, long features) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(json, features)) {
            applySuppliers(parser);
            JSONObject object = parser.readObject();
            if (object == null) {
                return null;
            }
            ObjectReader<V> objectReader = (ObjectReader<V>) getObjectReader(valueType);
            java.util.Map<String, V> map = new java.util.LinkedHashMap<>(object.size());
            for (String key : object.keySet()) {
                Object item = object.get(key);
                if (item == null) {
                    map.put(key, null);
                } else if (valueType.isInstance(item)) {
                    map.put(key, valueType.cast(item));
                } else if (item instanceof JSONObject jsonObj && objectReader != null) {
                    String itemJson = JSON.toJSONString(jsonObj);
                    try (JSONParser itemParser = JSONParser.of(itemJson, features)) {
                        map.put(key, objectReader.readObject(itemParser, valueType, null, features));
                    }
                } else {
                    map.put(key, coerceElement(valueType, item));
                }
            }
            return map;
        }
    }

    /**
     * Parse JSON bytes (UTF-8) to typed map.
     */
    public <V> java.util.Map<String, V> readMap(byte[] jsonBytes, Class<V> valueType) {
        return readMap(jsonBytes, valueType, readFeatures);
    }

    /**
     * Parse JSON bytes (UTF-8) to typed map with custom features.
     */
    public <V> java.util.Map<String, V> readMap(byte[] jsonBytes, Class<V> valueType, long features) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(jsonBytes, features)) {
            applySuppliers(parser);
            JSONObject object = parser.readObject();
            if (object == null) {
                return null;
            }
            ObjectReader<V> objectReader = (ObjectReader<V>) getObjectReader(valueType);
            java.util.Map<String, V> map = new java.util.LinkedHashMap<>(object.size());
            for (String key : object.keySet()) {
                Object item = object.get(key);
                if (item == null) {
                    map.put(key, null);
                } else if (valueType.isInstance(item)) {
                    map.put(key, valueType.cast(item));
                } else if (item instanceof JSONObject jsonObj && objectReader != null) {
                    String itemJson = JSON.toJSONString(jsonObj);
                    try (JSONParser itemParser = JSONParser.of(itemJson, features)) {
                        map.put(key, objectReader.readObject(itemParser, valueType, null, features));
                    }
                } else {
                    map.put(key, coerceElement(valueType, item));
                }
            }
            return map;
        }
    }

    // ==================== Read: with custom features ====================

    /**
     * Parse JSON string to typed Java object with custom features.
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(String json, Class<T> type, long features) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        ObjectReader<T> objectReader = (ObjectReader<T>) getObjectReader(type);
        if (objectReader != null) {
            if (needsReaderContext) {
                try (JSONParser parser = JSONParser.of(json, features);
                     ObjectReaderProvider.SafeContext ctx = readerProvider.openContext()) {
                    applySuppliers(parser);
                    return objectReader.readObject(parser, type, null, features);
                }
            } else {
                try (JSONParser parser = JSONParser.of(json, features)) {
                    applySuppliers(parser);
                    return objectReader.readObject(parser, type, null, features);
                }
            }
        }
        if (needsReaderContext) {
            try (JSONParser parser = JSONParser.of(json, features);
                 ObjectReaderProvider.SafeContext ctx = readerProvider.openContext()) {
                applySuppliers(parser);
                return parser.read(type);
            }
        } else {
            try (JSONParser parser = JSONParser.of(json, features)) {
                applySuppliers(parser);
                return parser.read(type);
            }
        }
    }

    /**
     * Parse JSON bytes (UTF-8) to typed Java object with custom features.
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(byte[] jsonBytes, Class<T> type, long features) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        ObjectReader<T> objectReader = (ObjectReader<T>) getObjectReader(type);
        if (objectReader != null) {
            if (needsReaderContext) {
                try (JSONParser parser = JSONParser.of(jsonBytes, features);
                     ObjectReaderProvider.SafeContext ctx = readerProvider.openContext()) {
                    applySuppliers(parser);
                    return objectReader.readObject(parser, type, null, features);
                }
            } else {
                try (JSONParser parser = JSONParser.of(jsonBytes, features)) {
                    applySuppliers(parser);
                    return objectReader.readObject(parser, type, null, features);
                }
            }
        }
        if (needsReaderContext) {
            try (JSONParser parser = JSONParser.of(jsonBytes, features);
                 ObjectReaderProvider.SafeContext ctx = readerProvider.openContext()) {
                applySuppliers(parser);
                return parser.read(type);
            }
        } else {
            try (JSONParser parser = JSONParser.of(jsonBytes, features)) {
                applySuppliers(parser);
                return parser.read(type);
            }
        }
    }

    /**
     * Parse JSON string to generic type with custom features.
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(String json, Type type, long features) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        ObjectReader<T> objectReader = (ObjectReader<T>) getObjectReader(type);
        if (objectReader != null) {
            if (needsReaderContext) {
                try (JSONParser parser = JSONParser.of(json, features);
                     ObjectReaderProvider.SafeContext ctx = readerProvider.openContext()) {
                    applySuppliers(parser);
                    return objectReader.readObject(parser, type, null, features);
                }
            } else {
                try (JSONParser parser = JSONParser.of(json, features)) {
                    applySuppliers(parser);
                    return objectReader.readObject(parser, type, null, features);
                }
            }
        }
        if (needsReaderContext) {
            try (JSONParser parser = JSONParser.of(json, features);
                 ObjectReaderProvider.SafeContext ctx = readerProvider.openContext()) {
                applySuppliers(parser);
                return parser.read(type);
            }
        } else {
            try (JSONParser parser = JSONParser.of(json, features)) {
                applySuppliers(parser);
                return parser.read(type);
            }
        }
    }

    /**
     * Parse JSON bytes (UTF-8) to generic type with custom features.
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(byte[] jsonBytes, Type type, long features) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        ObjectReader<T> objectReader = (ObjectReader<T>) getObjectReader(type);
        if (objectReader != null) {
            if (needsReaderContext) {
                try (JSONParser parser = JSONParser.of(jsonBytes, features);
                     ObjectReaderProvider.SafeContext ctx = readerProvider.openContext()) {
                    applySuppliers(parser);
                    return objectReader.readObject(parser, type, null, features);
                }
            } else {
                try (JSONParser parser = JSONParser.of(jsonBytes, features)) {
                    applySuppliers(parser);
                    return objectReader.readObject(parser, type, null, features);
                }
            }
        }
        if (needsReaderContext) {
            try (JSONParser parser = JSONParser.of(jsonBytes, features);
                 ObjectReaderProvider.SafeContext ctx = readerProvider.openContext()) {
                applySuppliers(parser);
                return parser.read(type);
            }
        } else {
            try (JSONParser parser = JSONParser.of(jsonBytes, features)) {
                applySuppliers(parser);
                return parser.read(type);
            }
        }
    }

    // ==================== Convert / Tree ====================

    /**
     * Convert an object to a different type by serializing and deserializing.
     * Useful for Map-to-POJO conversion.
     *
     * <pre>
     * Map&lt;String, Object&gt; map = ...;
     * User user = mapper.convertValue(map, User.class);
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public <T> T convertValue(Object fromValue, Class<T> toType) {
        if (fromValue == null) {
            return null;
        }
        if (toType.isInstance(fromValue)) {
            return toType.cast(fromValue);
        }
        byte[] json = writeValueAsBytes(fromValue);
        return readValue(json, toType);
    }

    /**
     * Convert an object to a different generic type.
     */
    public <T> T convertValue(Object fromValue, TypeReference<T> typeRef) {
        if (fromValue == null) {
            return null;
        }
        byte[] json = writeValueAsBytes(fromValue);
        return readValue(json, typeRef);
    }

    /**
     * Parse JSON string to tree (JSONObject or JSONArray).
     * Alias for {@link #readValue(String)} for Jackson API compatibility.
     */
    public Object readTree(String json) {
        return readValue(json);
    }

    /**
     * Parse UTF-8 JSON bytes to tree (JSONObject or JSONArray).
     */
    public Object readTree(byte[] jsonBytes) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(jsonBytes, readFeatures)) {
            applySuppliers(parser);
            return parser.readAny();
        }
    }

    /**
     * Parse JSON from InputStream to tree (JSONObject or JSONArray).
     */
    public Object readTree(InputStream in) {
        return readTree(readAllBytes(in));
    }

    // ==================== Write: String output ====================

    /**
     * Serialize object to JSON string.
     */
    public String writeValueAsString(Object obj) {
        return writeValueAsString(obj, writeFeatures);
    }

    /**
     * Serialize object to JSON string with custom features.
     */
    @SuppressWarnings("unchecked")
    public String writeValueAsString(Object obj, long features) {
        if (obj == null) {
            return "null";
        }
        // Fast path: UTF8 serialize → Latin-1 String (bypasses char[] entirely).
        // Inlined to minimize call chain depth for JIT inlining.
        if (com.alibaba.fastjson3.util.JDKUtils.FAST_STRING_CREATION
                && propertyFilters == null && valueFilters == null && nameFilters == null
                && features == 0) {
            ObjectWriter<Object> writer = (ObjectWriter<Object>) getObjectWriter(obj.getClass());
            if (writer != null) {
                try (JSONGenerator.UTF8 gen = (JSONGenerator.UTF8) JSONGenerator.ofUTF8()) {
                    gen.owner = this;
                    writer.write(gen, obj, null, null, 0);
                    String result = gen.toStringLatin1();
                    if (result != null) {
                        return result;
                    }
                    return gen.toString();
                }
            }
        }
        try (JSONGenerator generator = createCharGenerator(features)) {
            generator.owner = this;
            applyFilters(generator);
            writeValue0(generator, obj, features);
            return generator.toString();
        }
    }

    /**
     * Create a character generator with the specified features.
     */
    private JSONGenerator createCharGenerator(long features) {
        return JSONGenerator.of(features);  // direct mask, no array allocation
    }

    // ==================== Write: byte[] output ====================

    /**
     * Serialize object to UTF-8 JSON byte array.
     */
    public byte[] writeValueAsBytes(Object obj) {
        return writeValueAsBytes(obj, writeFeatures);
    }

    /**
     * Serialize object to UTF-8 JSON byte array with custom features.
     */
    public byte[] writeValueAsBytes(Object obj, long features) {
        if (obj == null) {
            return "null".getBytes(StandardCharsets.UTF_8);
        }
        try (JSONGenerator generator = createUTF8Generator(features)) {
            generator.owner = this;
            applyFilters(generator);
            writeValue0(generator, obj, features);
            return generator.toByteArray();
        }
    }

    /**
     * Create a UTF-8 generator with the specified features.
     */
    private JSONGenerator createUTF8Generator(long features) {
        return JSONGenerator.ofUTF8(features);  // direct mask, no array allocation
    }

    /**
     * Internal write method with custom features.
     */
    @SuppressWarnings("unchecked")
    private void writeValue0(JSONGenerator generator, Object obj, long features) {
        ObjectWriter<Object> writer = (ObjectWriter<Object>) getObjectWriter(obj.getClass());
        if (writer != null) {
            writer.write(generator, obj, null, null, features);
        } else {
            generator.writeAny(obj);
        }
    }

    // ==================== Write: OutputStream ====================

    /**
     * Serialize object to OutputStream as UTF-8 JSON.
     */
    public void writeValue(OutputStream out, Object obj) {
        byte[] bytes = writeValueAsBytes(obj);
        try {
            out.write(bytes);
        } catch (java.io.IOException e) {
            throw new JSONException("write to OutputStream error", e);
        }
    }

    // ==================== Per-call ObjectReader / ObjectWriter ====================

    /**
     * Create a per-call reader builder for the given type.
     * The returned reader is a lightweight, immutable object.
     *
     * <pre>
     * User user = mapper.readerFor(User.class)
     *     .with(ReadFeature.SupportSmartMatch)
     *     .readValue(json);
     * </pre>
     */
    public <T> ValueReader<T> readerFor(Class<T> type) {
        return new ValueReader<T>(this, type, readFeatures);
    }

    /**
     * Create a per-call writer builder.
     * The returned writer is a lightweight, immutable object.
     *
     * <pre>
     * String json = mapper.writer()
     *     .with(WriteFeature.PrettyFormat)
     *     .writeValueAsString(user);
     * </pre>
     */
    public ValueWriter writer() {
        return new ValueWriter(this, writeFeatures);
    }

    /**
     * Create a per-call writer builder for a specific type.
     * The given type is used for ObjectWriter lookup instead of the runtime class,
     * which is useful for serializing an object as a parent type.
     */
    public ValueWriter writerFor(Class<?> type) {
        return new ValueWriter(this, writeFeatures, type);
    }

    // ==================== ObjectReader/ObjectWriter registry ====================

    /**
     * Look up or create an ObjectReader for the given type.
     * Results are cached for reuse. Runtime registration via {@link #registerReader(Type, ObjectReader)}
     * is supported and takes precedence over cached ClassValue entries for Class types.
     *
     * <p>This method sets the ObjectReaderProvider context so that nested ObjectReader
     * creation (within the created reader) will use the same provider strategy.
     * This ensures ASM/Reflect/Auto strategy is applied consistently to nested types.</p>
     */
    @SuppressWarnings("unchecked")
    public <T> ObjectReader<T> getObjectReader(Type type) {
        // Check readerCache first to support runtime registerReader() overrides
        ObjectReader<?> reader = readerCache.get(type);
        if (reader != null) {
            return (ObjectReader<T>) reader;
        }

        // Expose this mapper's mix-in cache to creator helpers that walk inner types
        // (e.g. @JSONField(unwrapped=true) expansion, nested POJO field readers) so
        // addMixIn(inner, innerMixIn) actually reaches them. Restore after the call
        // so nested / parallel creations don't leak state.
        // Skip the ThreadLocal round-trip when mixInCache is empty — the common
        // case for mappers without addMixIn calls. Saves ~30–50ns per lookup.
        boolean needsMixinContext = !mixInCache.isEmpty();
        java.util.Map<Class<?>, Class<?>> prev = null;
        if (needsMixinContext) {
            prev = com.alibaba.fastjson3.reader.ObjectReaderCreator.MIXIN_CONTEXT.get();
            com.alibaba.fastjson3.reader.ObjectReaderCreator.MIXIN_CONTEXT.set(mixInCache);
        }
        try {
            // Fast path for Class types: ClassValue lookup ~3ns vs ConcurrentHashMap ~20ns
            if (type instanceof Class<?> cls) {
                return getReaderForClass(cls);
            }
            // Slow path for non-Class types (ParameterizedType, GenericArrayType, TypeVariable, WildcardType)
            return getObjectReaderSlow(type);
        } finally {
            if (needsMixinContext) {
                if (prev == null) {
                    com.alibaba.fastjson3.reader.ObjectReaderCreator.MIXIN_CONTEXT.remove();
                } else {
                    com.alibaba.fastjson3.reader.ObjectReaderCreator.MIXIN_CONTEXT.set(prev);
                }
            }
        }
    }

    /**
     * Get ObjectReader for a Class type with version checking and optional context propagation.
     */
    @SuppressWarnings("unchecked")
    private <T> ObjectReader<T> getReaderForClass(Class<?> cls) {
        ReaderHolder holder = readerClassCache.get(cls);
        if (holder == null) {
            // First-time creation - use context only for custom providers
            if (needsReaderContext) {
                return readerProvider.withContext(() -> {
                    ReaderHolder h = readerClassCache.get(cls);
                    return h != null ? (ObjectReader<T>) h.get() : (ObjectReader<T>) createAndCacheReader(cls);
                });
            } else {
                // Fast path: no context needed
                ReaderHolder h = readerClassCache.get(cls);
                if (h != null) {
                    return (ObjectReader<T>) h.get();
                }
                return (ObjectReader<T>) createAndCacheReader(cls);
            }
        }

        // Check if holder is stale (post-cleanup)
        int currentVersion = cacheVersion.get();
        if (holder.cacheVersion != currentVersion) {
            // Try to get fresh holder from holder map
            ReaderHolder freshHolder = readerHolderMap.get(cls);
            if (freshHolder != null && freshHolder.cacheVersion == currentVersion) {
                return (ObjectReader<T>) freshHolder.get();
            }
            // Fully recompute with context if needed
            if (needsReaderContext) {
                return readerProvider.withContext(() -> (ObjectReader<T>) recomputeReader(cls));
            } else {
                return (ObjectReader<T>) recomputeReader(cls);
            }
        }

        return (ObjectReader<T>) holder.get();
    }

    /**
     * Create and cache an ObjectReader for the given type.
     * Should be called within withContext() to propagate provider strategy.
     */
    @SuppressWarnings("unchecked")
    private <T> ObjectReader<T> createAndCacheReader(Class<?> type) {
        ObjectReader<?> reader = createReaderInternal(type);
        if (reader != null) {
            ReaderHolder holder = new ReaderHolder(reader, cacheVersion.get());
            readerHolderMap.put(type, holder);
        }
        return (ObjectReader<T>) reader;
    }

    /**
     * Recompute an ObjectReader when the cached holder is stale (post-cleanup).
     */
    @SuppressWarnings("unchecked")
    private <T> ObjectReader<T> recomputeReader(Class<T> type) {
        ObjectReader<?> reader = createReaderInternal(type);
        if (reader != null) {
            ReaderHolder holder = new ReaderHolder(reader, cacheVersion.get());
            readerHolderMap.put(type, holder);
        }
        return (ObjectReader<T>) reader;
    }

    /**
     * Internal method to create an ObjectReader without caching.
     * Tries modules, built-in codecs, and auto-creation in order.
     */
    @SuppressWarnings("unchecked")
    private ObjectReader<?> createReaderInternal(Type type) {
        // Try modules first
        for (ObjectReaderModule module : readerModules) {
            ObjectReader<?> reader = module.getObjectReader(type);
            if (reader != null) {
                return reader;
            }
        }
        // For Class types, try built-in codecs and auto-create
        if (type instanceof Class<?> clazz) {
            ObjectReader<?> reader = BuiltinCodecs.getReader(clazz);
            if (reader != null) {
                return reader;
            }
            // Auto-create
            try {
                if (readerCreator != null) {
                    return readerCreator.apply(clazz);
                } else {
                    // JSONObject / JSONArray and the raw collection / map
                    // interfaces (Map, List, Set, Collection, Iterable) plus
                    // their common abstract / default-impl classes have native
                    // routing in JSONParser.read(Class). Without this
                    // short-circuit the auto-built reflection POJO reader
                    // assumes `{` and rejects arrays outright or yields an
                    // empty container. Placed here (after modules /
                    // BuiltinCodecs / user-supplied readerCreator) so SPI
                    // overrides for these types still take precedence; only
                    // the default auto-build path is bypassed in favor of
                    // parser fallback.
                    if (isParserShortCircuitClass(clazz)) {
                        return null;
                    }
                    Class<?> mixIn = mixInCache.get(clazz);
                    // MIXIN_CONTEXT is set by the top-level getObjectReader entry so
                    // inner-type helpers (unwrapped expansion, nested POJO collection)
                    // can consult this mapper's full mix-in cache.
                    return ObjectReaderCreator.createObjectReader(clazz, mixIn, useJacksonAnnotation);
                }
            } catch (Exception e) {
                // fall through
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> ObjectReader<T> getObjectReaderSlow(Type type) {
        // Only used for non-Class types (ParameterizedType, GenericArrayType, TypeVariable, WildcardType)
        // Class types are handled by readerClassCache in getObjectReader()
        if (type instanceof Class<?>) {
            // Should not reach here, but handle gracefully
            return null;
        }

        // Try modules for generic types
        ObjectReader<?> reader = null;
        for (ObjectReaderModule module : readerModules) {
            reader = module.getObjectReader(type);
            if (reader != null) {
                readerCache.putIfAbsent(type, reader);
                return (ObjectReader<T>) reader;
            }
        }

        // Auto-create a parameterized POJO reader. {@code TypeReference<Parent<Bean>>}
        // and {@code List<Parent<Bean>>} element-level reads land here when no module
        // handles the raw type. {@link ObjectReaderCreator#createObjectReader(ParameterizedType,
        // Class, boolean)} builds field readers with {@code T → Bean} substituted so
        // {@code T value} fields parse as the concrete type rather than {@code Object}.
        if (type instanceof java.lang.reflect.ParameterizedType pt
                && pt.getRawType() instanceof Class<?> raw
                && !raw.isInterface()
                && !java.util.Collection.class.isAssignableFrom(raw)
                && !java.util.Map.class.isAssignableFrom(raw)) {
            try {
                Class<?> mixIn = mixInCache.get(raw);
                ObjectReader<?> pojoReader = com.alibaba.fastjson3.reader.ObjectReaderCreator
                        .createObjectReader(pt, mixIn, useJacksonAnnotation);
                if (pojoReader != null) {
                    readerCache.putIfAbsent(type, pojoReader);
                    return (ObjectReader<T>) pojoReader;
                }
            } catch (Exception ignored) {
                // fall through
            }
        }

        return null;
    }

    /**
     * Look up or create an ObjectWriter for the given type.
     * Results are cached for reuse. Runtime registration via {@link #registerWriter(Type, ObjectWriter)}
     * is supported and takes precedence over cached ClassValue entries for Class types.
     */
    @SuppressWarnings("unchecked")
    public <T> ObjectWriter<T> getObjectWriter(Type type) {
        // Check writerCache first to support runtime registerWriter() overrides
        ObjectWriter<?> writer = writerCache.get(type);
        if (writer != null) {
            return (ObjectWriter<T>) writer;
        }

        // Fast path for Class types: ClassValue lookup ~3ns vs ConcurrentHashMap ~20ns
        if (type instanceof Class<?> cls) {
            return (ObjectWriter<T>) writerClassCache.get(cls);
        }
        // Slow path for non-Class types (ParameterizedType, GenericArrayType, TypeVariable, WildcardType)
        return getObjectWriterSlow(type);
    }

    @SuppressWarnings("unchecked")
    private <T> ObjectWriter<T> getObjectWriterSlow(Type type) {
        ObjectWriter<?> writer = writerCache.get(type);
        if (writer != null) {
            return (ObjectWriter<T>) writer;
        }

        Class<?> rawType;
        if (type instanceof Class) {
            rawType = (Class<?>) type;
        } else if (type instanceof java.lang.reflect.ParameterizedType pt) {
            java.lang.reflect.Type raw = pt.getRawType();
            rawType = raw instanceof Class<?> c ? c : null;
        } else {
            rawType = null;
        }

        // Try modules
        for (ObjectWriterModule module : writerModules) {
            writer = module.getObjectWriter(type, rawType);
            if (writer != null) {
                writerCache.putIfAbsent(type, writer);
                return (ObjectWriter<T>) writer;
            }
        }

        // Try built-in codecs first (Optional, UUID, Duration, etc.)
        if (rawType != null) {
            writer = BuiltinCodecs.getWriter(rawType);
            if (writer != null) {
                writerCache.putIfAbsent(type, writer);
                return (ObjectWriter<T>) writer;
            }
        }

        // Check for @JSONField(value=true) on enum types (bypasses isBasicType)
        if (rawType != null && rawType.isEnum()) {
            writer = ObjectWriterCreator.findValueWriter(rawType, null, useJacksonAnnotation);
            if (writer != null) {
                writerCache.putIfAbsent(type, writer);
                return (ObjectWriter<T>) writer;
            }
        }

        // Auto-create writer (only for POJO types)
        if (rawType != null && !isBasicType(rawType)) {
            try {
                if (useJacksonAnnotation || !mixInCache.isEmpty()
                        || (writeFeatures & (WriteFeature.IgnoreNoneSerializable.mask
                            | WriteFeature.ErrorOnNoneSerializable.mask
                            | WriteFeature.IgnoreNonFieldGetter.mask)) != 0) {
                    // When annotations/mixIns/serializable checks are needed, use ObjectWriterCreator directly
                    Class<?> mixIn = mixInCache.get(rawType);
                    writer = ObjectWriterCreator.createObjectWriter(rawType, mixIn, useJacksonAnnotation, writeFeatures, mixInCache);
                } else if (writerCreator != null) {
                    writer = writerCreator.apply(rawType);
                } else if (writerProvider != null) {
                    // Check if filters are configured - ASM doesn't support runtime filters
                    boolean hasFilters = (propertyFilters != null && propertyFilters.length > 0) ||
                                        (valueFilters != null && valueFilters.length > 0) ||
                                        (nameFilters != null && nameFilters.length > 0) ||
                                        (propertyPreFilters != null && propertyPreFilters.length > 0) ||
                                        (beforeFilters != null && beforeFilters.length > 0) ||
                                        (afterFilters != null && afterFilters.length > 0) ||
                                        labelFilter != null;
                    if (hasFilters) {
                        // Use reflection for filter support
                        Class<?> mixIn = mixInCache.get(rawType);
                        writer = ObjectWriterCreator.createObjectWriter(rawType, mixIn, useJacksonAnnotation);
                    } else {
                        // Use writerProvider (AutoObjectWriterProvider will use ASM when possible)
                        writer = writerProvider.getObjectWriter(rawType);
                    }
                } else {
                    // Fallback to ObjectWriterCreator
                    Class<?> mixIn = mixInCache.get(rawType);
                    writer = ObjectWriterCreator.createObjectWriter(rawType, mixIn, useJacksonAnnotation);
                }
            } catch (JSONException e) {
                throw e; // propagate explicit errors (e.g., ErrorOnNoneSerializable)
            } catch (Exception e) {
                return null;
            }
            if (writer != null) {
                writerCache.putIfAbsent(type, writer);
                return (ObjectWriter<T>) writer;
            }
        }
        return null;
    }

    /**
     * Register a custom ObjectReader for a specific type.
     * Useful for runtime customization on a per-mapper basis.
     */
    public <T> void registerReader(Type type, ObjectReader<T> reader) {
        readerCache.put(type, reader);
    }

    /**
     * Register a custom ObjectWriter for a specific type.
     */
    public <T> void registerWriter(Type type, ObjectWriter<T> writer) {
        writerCache.put(type, writer);
    }

    /**
     * Remove a previously registered ObjectReader for a specific type.
     */
    public void unregisterReader(Type type) {
        readerCache.remove(type);
    }

    /**
     * Remove a previously registered ObjectWriter for a specific type.
     */
    public void unregisterWriter(Type type) {
        writerCache.remove(type);
    }

    /**
     * Clean up resources to support ClassLoader unloading.
     *
     * <p>Clears all cached ObjectReader and ObjectWriter instances, and signals
     * the provider to release its resources. Call this method when the mapper
     * is no longer needed to allow the ClassLoader to be garbage collected.</p>
     *
     * <p><strong>Important:</strong> After calling cleanup(), this mapper should
     * not be used for further operations. Create a new mapper instance instead.</p>
     *
     * <p>For the shared mapper ({@link #shared()}), this method is a no-op to
     * avoid affecting other users of the shared instance.</p>
     *
     * <pre>
     * // In a hot-redeployment environment (e.g., application server)
     * ObjectMapper mapper = ObjectMapper.builder()
     *     .readerCreatorType(ReaderCreatorType.ASM)
     *     .build();
     * try {
     *     // ... use mapper ...
     * } finally {
     *     mapper.cleanup(); // Release resources for ClassLoader unload
     * }
     * </pre>
     */
    public void cleanup() {
        // No-op for shared singleton
        if (this == SHARED) {
            return;
        }
        // Increment cache version - this invalidates all ReaderHolder instances in ClassValue
        cacheVersion.incrementAndGet();
        // Clear holder map - forces recomputation on next access
        readerHolderMap.clear();
        // Clear caches
        readerCache.clear();
        writerCache.clear();
        // Provider cleanup (only clears if it's a per-instance provider)
        readerProvider.cleanup();
        writerProvider.cleanup();
    }

    // ==================== Feature queries ====================

    public boolean isReadEnabled(ReadFeature feature) {
        return (readFeatures & feature.mask) != 0;
    }

    public boolean isWriteEnabled(WriteFeature feature) {
        return (writeFeatures & feature.mask) != 0;
    }

    public long getReadFeatures() {
        return readFeatures;
    }

    public long getWriteFeatures() {
        return writeFeatures;
    }

    /**
     * Get the mixin source class for the given target class, or null if none registered.
     */
    public Class<?> getMixIn(Class<?> target) {
        return mixInCache.get(target);
    }

    /**
     * Whether Jackson annotations (e.g. {@code @JsonProperty}, {@code @JsonIgnore})
     * are interpreted on top of fastjson's own. Set via
     * {@link Builder#useJacksonAnnotation(boolean)}. Exposed so deep-in-writer
     * paths that create fresh per-type writers (e.g. unwrap flattening) can
     * reproduce the mapper's configuration.
     */
    public boolean useJacksonAnnotation() {
        return useJacksonAnnotation;
    }

    /**
     * Register a mixin class at runtime. Annotations from {@code mixinSource}
     * will be applied to {@code target} during serialization and deserialization.
     * <p>Note: Cached ObjectReaders/ObjectWriters for the target type are not
     * automatically invalidated. Call this before any parsing/serialization of
     * the target type, or call {@link #cleanup()} to clear caches.</p>
     */
    public void addMixIn(Class<?> target, Class<?> mixinSource) {
        mixInCache.put(target, mixinSource);
    }

    /**
     * Get the ObjectReaderProvider used by this mapper.
     */
    public ObjectReaderProvider getReaderProvider() {
        return readerProvider;
    }

    /**
     * Get the ReaderCreatorType used by this mapper.
     */
    public ReaderCreatorType getReaderCreatorType() {
        return readerProvider.getCreatorType();
    }

    /**
     * Get the ObjectWriterProvider used by this mapper.
     */
    public ObjectWriterProvider getWriterProvider() {
        return writerProvider;
    }

    /**
     * Get the WriterCreatorType used by this mapper.
     */
    public WriterCreatorType getWriterCreatorType() {
        return writerProvider.getCreatorType();
    }

    // ==================== Internal ====================

    /** Maximum InputStream size (128 MB). */
    private static final int MAX_INPUT_SIZE = 128 * 1024 * 1024;

    private static byte[] readAllBytes(InputStream in) {
        if (in == null) {
            return null;
        }
        try {
            byte[] bytes = in.readNBytes(MAX_INPUT_SIZE + 1);
            if (bytes.length > MAX_INPUT_SIZE) {
                throw new JSONException("input size exceeds maximum "
                        + (MAX_INPUT_SIZE / 1024 / 1024) + " MB");
            }
            return bytes;
        } catch (IOException e) {
            throw new JSONException("read InputStream error", e);
        }
    }

    private static String readAllChars(Reader reader) {
        if (reader == null) {
            return null;
        }
        try {
            StringBuilder sb = new StringBuilder(1024);
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new JSONException("read Reader error", e);
        }
    }

    /**
     * Format compact JSON string with 2-space indentation.
     * Post-processing approach: zero overhead on the hot serialization path.
     */
    static String prettyFormat(String json) {
        int len = json.length();
        StringBuilder sb = new StringBuilder(len + (len >> 2));
        int indent = 0;
        boolean inString = false;

        for (int i = 0; i < len; i++) {
            char c = json.charAt(i);

            if (inString) {
                sb.append(c);
                if (c == '"' && !isEscaped(json, i)) {
                    inString = false;
                }
                continue;
            }

            switch (c) {
                case '"':
                    sb.append(c);
                    inString = true;
                    break;
                case '{':
                case '[':
                    sb.append(c);
                    // Don't add newline if next non-whitespace char is the matching close
                    if (i + 1 < len && isMatchingClose(json, i + 1, c)) {
                        // empty object/array: write close immediately
                        sb.append(c == '{' ? '}' : ']');
                        i = skipTo(json, i + 1, c == '{' ? '}' : ']');
                    } else {
                        indent++;
                        sb.append('\n');
                        appendIndent(sb, indent);
                    }
                    break;
                case '}':
                case ']':
                    indent--;
                    sb.append('\n');
                    appendIndent(sb, indent);
                    sb.append(c);
                    break;
                case ',':
                    sb.append(c);
                    sb.append('\n');
                    appendIndent(sb, indent);
                    break;
                case ':':
                    sb.append(c);
                    sb.append(' ');
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean isEscaped(String json, int quoteIndex) {
        int backslashes = 0;
        for (int i = quoteIndex - 1; i >= 0 && json.charAt(i) == '\\'; i--) {
            backslashes++;
        }
        return (backslashes & 1) == 1;
    }

    private static boolean isMatchingClose(String json, int from, char open) {
        char close = (open == '{') ? '}' : ']';
        for (int i = from; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                continue;
            }
            return c == close;
        }
        return false;
    }

    private static int skipTo(String json, int from, char target) {
        for (int i = from; i < json.length(); i++) {
            if (json.charAt(i) == target) {
                return i;
            }
        }
        return from;
    }

    private static void appendIndent(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
    }

    /**
     * Types that {@link JSONParser#read(Class)} can deserialize natively —
     * either via dedicated routing ({@code JSONObject}, {@code JSONArray})
     * or via the generic Map / List / Set fallback for raw container
     * targets. Returning null from {@link #createReaderInternal} for these
     * lets the parser take over instead of synthesizing a broken
     * reflection POJO reader.
     */
    private static boolean isParserShortCircuitClass(Class<?> clazz) {
        return clazz == JSONObject.class
                || clazz == JSONArray.class
                || clazz == Object.class
                // Map family
                || clazz == java.util.Map.class
                || clazz == java.util.AbstractMap.class
                || clazz == java.util.HashMap.class
                || clazz == java.util.TreeMap.class
                || clazz == java.util.SortedMap.class
                || clazz == java.util.NavigableMap.class
                || clazz == java.util.concurrent.ConcurrentMap.class
                || clazz == java.util.concurrent.ConcurrentHashMap.class
                || clazz == java.util.concurrent.ConcurrentNavigableMap.class
                || clazz == java.util.concurrent.ConcurrentSkipListMap.class
                // List family
                || clazz == java.util.List.class
                || clazz == java.util.Collection.class
                || clazz == Iterable.class
                || clazz == java.util.ArrayList.class
                || clazz == java.util.AbstractCollection.class
                || clazz == java.util.AbstractList.class
                || clazz == java.util.LinkedList.class
                || clazz == java.util.Queue.class
                || clazz == java.util.Deque.class
                || clazz == java.util.AbstractSequentialList.class
                || clazz == java.util.Vector.class
                || clazz == java.util.Stack.class
                || clazz == java.util.concurrent.CopyOnWriteArrayList.class
                // Set family
                || clazz == java.util.Set.class
                || clazz == java.util.AbstractSet.class
                || clazz == java.util.HashSet.class
                || clazz == java.util.LinkedHashSet.class
                || clazz == java.util.TreeSet.class
                || clazz == java.util.SortedSet.class
                || clazz == java.util.NavigableSet.class
                || clazz == java.util.concurrent.CopyOnWriteArraySet.class;
    }

    private static boolean isBasicType(Class<?> type) {
        return type == String.class
                || type == Object.class
                || type.isPrimitive()
                || type == Integer.class || type == Long.class || type == Double.class
                || type == Float.class || type == Boolean.class || type == Short.class
                || type == Byte.class || type == Character.class
                || type == java.math.BigDecimal.class || type == java.math.BigInteger.class
                || type == JSONObject.class || type == JSONArray.class
                || type == java.util.concurrent.atomic.AtomicInteger.class
                || type == java.util.concurrent.atomic.AtomicLong.class
                || type == java.util.concurrent.atomic.AtomicBoolean.class
                || type == java.util.concurrent.atomic.AtomicIntegerArray.class
                || type == java.util.concurrent.atomic.AtomicLongArray.class
                || type == java.util.concurrent.atomic.AtomicReference.class
                || type.isArray() || type.isEnum()
                || java.util.Collection.class.isAssignableFrom(type)
                || java.util.Map.class.isAssignableFrom(type)
                || java.time.temporal.Temporal.class.isAssignableFrom(type)
                || type == java.util.Date.class;
    }

    public PropertyFilter[] getPropertyFilters() {
        return propertyFilters;
    }

    public ValueFilter[] getValueFilters() {
        return valueFilters;
    }

    public NameFilter[] getNameFilters() {
        return nameFilters;
    }

    private void applyFilters(JSONGenerator generator) {
        if (beforeFilters != null && beforeFilters.length > 0) {
            generator.beforeFilters = beforeFilters;
        }
        if (afterFilters != null && afterFilters.length > 0) {
            generator.afterFilters = afterFilters;
        }
        if (propertyPreFilters != null && propertyPreFilters.length > 0) {
            generator.propertyPreFilters = propertyPreFilters;
        }
        if (propertyFilters.length > 0 || valueFilters.length > 0 || nameFilters.length > 0) {
            generator.setFilters(propertyFilters, valueFilters, nameFilters);
        }
        if (labelFilter != null) {
            generator.labelFilter = labelFilter;
        }
        if ((writeFeatures & WriteFeature.ReferenceDetection.mask) != 0) {
            generator.setReferenceDetection(true);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeValue0(JSONGenerator jsonGenerator, Object obj) {
        ObjectWriter<Object> objectWriter = (ObjectWriter<Object>) getObjectWriter(obj.getClass());
        if (objectWriter != null) {
            objectWriter.write(jsonGenerator, obj, null, null, writeFeatures);
        } else {
            jsonGenerator.writeAny(obj);
        }
    }

    // ==================== Per-call ValueReader ====================

    /**
     * Immutable, per-call deserialization configurator. Lightweight — safe to create per request.
     * Uses "mutant factory" pattern: each {@code with()} returns a new instance.
     */
    public static final class ValueReader<T> {
        private final ObjectMapper mapper;
        private final Class<T> type;
        private final long features;

        ValueReader(ObjectMapper mapper, Class<T> type, long features) {
            this.mapper = mapper;
            this.type = type;
            this.features = features;
        }

        /**
         * Return a new ValueReader with the given features enabled.
         */
        public ValueReader<T> with(ReadFeature... readFeatures) {
            long f = this.features;
            for (ReadFeature rf : readFeatures) {
                f |= rf.mask;
            }
            return f == this.features ? this : new ValueReader<T>(mapper, type, f);
        }

        /**
         * Return a new ValueReader with the given features disabled.
         */
        public ValueReader<T> without(ReadFeature... readFeatures) {
            long f = this.features;
            for (ReadFeature rf : readFeatures) {
                f &= ~rf.mask;
            }
            return f == this.features ? this : new ValueReader<T>(mapper, type, f);
        }

        /**
         * Read from JSON string.
         */
        @SuppressWarnings("unchecked")
        public T readValue(String json) {
            if (json == null || json.isEmpty()) {
                return null;
            }
            ObjectReader<T> reader = (ObjectReader<T>) mapper.getObjectReader(type);
            if (reader != null) {
                try (JSONParser jr = JSONParser.of(json, features)) {
                    mapper.applySuppliers(jr);
                    return reader.readObject(jr, type, null, features);
                }
            }
            try (JSONParser jr = JSONParser.of(json, features)) {
                mapper.applySuppliers(jr);
                return jr.read(type);
            }
        }

        /**
         * Read from UTF-8 JSON bytes.
         */
        @SuppressWarnings("unchecked")
        public T readValue(byte[] jsonBytes) {
            if (jsonBytes == null || jsonBytes.length == 0) {
                return null;
            }
            ObjectReader<T> reader = (ObjectReader<T>) mapper.getObjectReader(type);
            if (reader != null) {
                try (JSONParser jr = JSONParser.of(jsonBytes, features)) {
                    mapper.applySuppliers(jr);
                    return reader.readObject(jr, type, null, features);
                }
            }
            try (JSONParser jr = JSONParser.of(jsonBytes, features)) {
                mapper.applySuppliers(jr);
                return jr.read(type);
            }
        }
    }

    // ==================== Per-call ValueWriter ====================

    /**
     * Immutable, per-call serialization configurator. Lightweight — safe to create per request.
     * Uses "mutant factory" pattern: each {@code with()} returns a new instance.
     */
    public static final class ValueWriter {
        private final ObjectMapper mapper;
        private final long features;
        private final Class<?> forType;

        ValueWriter(ObjectMapper mapper, long features) {
            this(mapper, features, null);
        }

        ValueWriter(ObjectMapper mapper, long features, Class<?> forType) {
            this.mapper = mapper;
            this.features = features;
            this.forType = forType;
        }

        /**
         * Return a new ValueWriter with the given features enabled.
         */
        public ValueWriter with(WriteFeature... writeFeatures) {
            long f = this.features;
            for (WriteFeature wf : writeFeatures) {
                f |= wf.mask;
            }
            return f == this.features ? this : new ValueWriter(mapper, f, forType);
        }

        /**
         * Return a new ValueWriter with the given features disabled.
         */
        public ValueWriter without(WriteFeature... writeFeatures) {
            long f = this.features;
            for (WriteFeature wf : writeFeatures) {
                f &= ~wf.mask;
            }
            return f == this.features ? this : new ValueWriter(mapper, f, forType);
        }

        /**
         * Serialize to JSON string.
         */
        public String writeValueAsString(Object obj) {
            if (obj == null) {
                return "null";
            }
            try (JSONGenerator generator = features != 0 ? new JSONGenerator.Char(features) : JSONGenerator.of()) {
                generator.owner = mapper;
                writeValue0(generator, obj);
                return generator.toString();
            }
        }

        /**
         * Serialize to UTF-8 JSON byte array.
         */
        public byte[] writeValueAsBytes(Object obj) {
            if (obj == null) {
                return "null".getBytes(StandardCharsets.UTF_8);
            }
            try (JSONGenerator generator = features != 0 ? new JSONGenerator.UTF8(features) : JSONGenerator.ofUTF8()) {
                generator.owner = mapper;
                writeValue0(generator, obj);
                return generator.toByteArray();
            }
        }

        @SuppressWarnings("unchecked")
        private void writeValue0(JSONGenerator jsonGenerator, Object obj) {
            Class<?> type = forType != null ? forType : obj.getClass();
            ObjectWriter<Object> objectWriter = (ObjectWriter<Object>) mapper.getObjectWriter(type);
            if (objectWriter != null) {
                objectWriter.write(jsonGenerator, obj, null, null, features);
            } else {
                jsonGenerator.writeAny(obj);
            }
        }
    }

    // ==================== Builder ====================

    /**
     * Builder for creating immutable ObjectMapper instances.
     * All configuration happens here; the resulting mapper is thread-safe and immutable.
     */
    public static final class Builder {
        long readFeatures;
        long writeFeatures;
        final List<ObjectReaderModule> readerModules = new ArrayList<ObjectReaderModule>();
        final List<ObjectWriterModule> writerModules = new ArrayList<ObjectWriterModule>();
        Function<Class<?>, ObjectReader<?>> readerCreator;
        Function<Class<?>, ObjectWriter<?>> writerCreator;
        ReaderCreatorType readerCreatorType;
        ObjectReaderProvider readerProviderInstance;
        WriterCreatorType writerCreatorType;
        ObjectWriterProvider writerProviderInstance;
        com.alibaba.fastjson3.util.DynamicClassLoader classLoader;
        final List<com.alibaba.fastjson3.filter.BeforeFilter> beforeFilters = new ArrayList<>();
        final List<com.alibaba.fastjson3.filter.AfterFilter> afterFilters = new ArrayList<>();
        final List<com.alibaba.fastjson3.filter.PropertyPreFilter> propertyPreFilters = new ArrayList<>();
        final List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
        final List<ValueFilter> valueFilters = new ArrayList<ValueFilter>();
        final List<NameFilter> nameFilters = new ArrayList<NameFilter>();
        final Map<Class<?>, Class<?>> mixIns = new LinkedHashMap<Class<?>, Class<?>>();
        com.alibaba.fastjson3.filter.LabelFilter labelFilter;
        boolean useJacksonAnnotation;
        java.util.function.Supplier<? extends java.util.Map<String, Object>> mapSupplier;
        java.util.function.Supplier<? extends java.util.List<Object>> listSupplier;
        String dateFormat;

        Builder() {
        }

        /**
         * Set a default date/time format for this mapper. Applied to fields
         * that have no field-level {@code @JSONField(format=...)} override.
         * Field-level annotation always wins.
         *
         * <p>Recognized values:</p>
         * <ul>
         *   <li>{@code null} or empty — clear the default; emit per the
         *       Temporal type's natural ISO shape (current fastjson3 behavior).</li>
         *   <li>{@code "millis"} — emit {@code Date.getTime()} as a JSON number.</li>
         *   <li>{@code "unixtime"} — emit {@code Date.getTime() / 1000} as a JSON number.</li>
         *   <li>{@code "iso8601"} — emit ISO-8601 (equivalent to no format set).</li>
         *   <li>{@code "yyyy-MM-dd"} / {@code "yyyyMMdd"} /
         *       {@code "yyyy-MM-dd HH:mm"} / {@code "yyyy-MM-dd HH:mm:ss"} /
         *       {@code "yyyyMMddHHmmss"} — fast hand-rolled byte writers.
         *       Other patterns fall through to a cached {@link java.time.format.DateTimeFormatter}.</li>
         * </ul>
         *
         * <h4>Scope and edge cases</h4>
         * <ul>
         *   <li><b>Date-shaped types only</b>: applies to {@link java.time.LocalDate},
         *       {@link java.time.LocalDateTime}, {@link java.time.Instant},
         *       {@link java.time.ZonedDateTime}, {@link java.time.OffsetDateTime},
         *       and {@link java.util.Date} (and subclasses). Time-only
         *       ({@link java.time.LocalTime}, {@link java.time.OffsetTime})
         *       and partial-date ({@link java.time.Year},
         *       {@link java.time.YearMonth}, {@link java.time.MonthDay})
         *       types pass through unchanged — date-shaped patterns have
         *       no meaningful projection onto these.</li>
         *   <li><b>Year range</b>: the fast-path byte writers handle years
         *       {@code 0–9999}. Out-of-range years (BCE or {@code > 9999})
         *       route through the pre-compiled {@link java.time.format.DateTimeFormatter}
         *       fallback, which signs them per JDK conventions
         *       ({@code -0001-01-01}, {@code +12345-01-01}).</li>
         *   <li><b>Module / WriterCreator overrides win</b>: a custom
         *       {@code ObjectWriter} for a Date type registered via
         *       {@link #addWriterModule(com.alibaba.fastjson3.modules.ObjectWriterModule)}
         *       or {@link #writerCreator(java.util.function.Function)} bypasses
         *       the built-in mapper-format hook. The user's writer is responsible
         *       for consulting {@code generator.effectiveMapper().getDateFormatPattern()}
         *       if it wants to honor mapper-level formats.</li>
         *   <li><b>Read side</b>: this is a write-side feature only. Parser
         *       configuration for the same format is a separate follow-up.</li>
         * </ul>
         *
         * <p>Mirrors fastjson2's {@code FastJsonConfig.setDateFormat(...)} for
         * fj2 → fj3 migration parity.</p>
         */
        public Builder dateFormat(String pattern) {
            this.dateFormat = pattern;
            return this;
        }

        /**
         * Set a per-mapper {@code Map} supplier. When set, untyped reads
         * via this mapper produce {@link JSONObject}s whose backing
         * storage is whatever the supplier returns (e.g.
         * {@code ConcurrentHashMap::new}). Equivalent to fastjson2's
         * {@code JSONReader.Context.setObjectSupplier} but per-mapper
         * instead of per-context.
         *
         * <pre>
         * ObjectMapper mapper = ObjectMapper.builder()
         *     .mapSupplier(java.util.concurrent.ConcurrentHashMap::new)
         *     .build();
         * </pre>
         */
        public Builder mapSupplier(java.util.function.Supplier<? extends java.util.Map<String, Object>> supplier) {
            this.mapSupplier = supplier;
            return this;
        }

        /**
         * Set a per-mapper {@code List} supplier. Mirrors
         * {@link #mapSupplier(java.util.function.Supplier)} for
         * {@link JSONArray} backing. Common choices:
         * {@code java.util.concurrent.CopyOnWriteArrayList::new},
         * {@code java.util.LinkedList::new}.
         */
        public Builder listSupplier(java.util.function.Supplier<? extends java.util.List<Object>> supplier) {
            this.listSupplier = supplier;
            return this;
        }

        // ---- Features ----

        public Builder enableRead(ReadFeature... features) {
            for (ReadFeature f : features) {
                readFeatures |= f.mask;
            }
            return this;
        }

        public Builder disableRead(ReadFeature... features) {
            for (ReadFeature f : features) {
                readFeatures &= ~f.mask;
            }
            return this;
        }

        public Builder enableWrite(WriteFeature... features) {
            for (WriteFeature f : features) {
                writeFeatures |= f.mask;
            }
            return this;
        }

        public Builder disableWrite(WriteFeature... features) {
            for (WriteFeature f : features) {
                writeFeatures &= ~f.mask;
            }
            return this;
        }

        // ---- Modules ----

        /**
         * Add a reader module for custom deserialization.
         */
        public Builder addReaderModule(ObjectReaderModule module) {
            readerModules.add(module);
            return this;
        }

        /**
         * Add a writer module for custom serialization.
         */
        public Builder addWriterModule(ObjectWriterModule module) {
            writerModules.add(module);
            return this;
        }

        // ---- Creator strategy ----

        /**
         * Set a custom ObjectReader creator function.
         * Use this to plug in ASM or APT-based reader creation.
         */
        public Builder readerCreator(Function<Class<?>, ObjectReader<?>> creator) {
            this.readerCreator = creator;
            return this;
        }

        /**
         * Set the ObjectReaderProvider strategy (AUTO, ASM, REFLECT).
         * Controls whether to use ASM bytecode generation or reflection.
         *
         * <pre>
         * // Force ASM for all types (including nested)
         * ObjectMapper mapper = ObjectMapper.builder()
         *     .readerCreatorType(ReaderCreatorType.ASM)
         *     .build();
         *
         * // Force reflection only
         * ObjectMapper mapper = ObjectMapper.builder()
         *     .readerCreatorType(ReaderCreatorType.REFLECT)
         *     .build();
         * </pre>
         *
         * @param type the creator type strategy
         * @return this builder
         */
        public Builder readerCreatorType(ReaderCreatorType type) {
            this.readerCreatorType = type;
            this.readerCreator = null; // Will be set in build()
            return this;
        }

        /**
         * Set a custom ObjectReaderProvider instance.
         *
         * @param provider the provider to use
         * @return this builder
         */
        public Builder readerProvider(ObjectReaderProvider provider) {
            this.readerProviderInstance = provider;
            this.readerCreatorType = provider != null ? provider.getCreatorType() : null;
            this.readerCreator = provider != null ? provider::getObjectReader : null;
            return this;
        }

        /**
         * Set the ObjectWriterProvider strategy (AUTO, ASM, REFLECT).
         * Controls whether to use ASM bytecode generation or reflection.
         *
         * <pre>
         * // Force ASM for all types (including nested)
         * ObjectMapper mapper = ObjectMapper.builder()
         *     .writerCreatorType(WriterCreatorType.ASM)
         *     .build();
         *
         * // Force reflection only
         * ObjectMapper mapper = ObjectMapper.builder()
         *     .writerCreatorType(WriterCreatorType.REFLECT)
         *     .build();
         * </pre>
         *
         * @param type the creator type strategy
         * @return this builder
         */
        public Builder writerCreatorType(WriterCreatorType type) {
            this.writerCreatorType = type;
            this.writerCreator = null; // Will be set in build()
            return this;
        }

        /**
         * Set a custom ObjectWriterProvider instance.
         *
         * @param provider the provider to use
         * @return this builder
         */
        public Builder writerProvider(ObjectWriterProvider provider) {
            this.writerProviderInstance = provider;
            this.writerCreatorType = provider != null ? provider.getCreatorType() : null;
            this.writerCreator = provider != null ? provider::getObjectWriter : null;
            return this;
        }

        /**
         * Set a custom ObjectWriter creator function.
         * Use this to plug in ASM or APT-based writer creation.
         */
        public Builder writerCreator(Function<Class<?>, ObjectWriter<?>> creator) {
            this.writerCreator = creator;
            return this;
        }

        // ---- Filters ----

        /**
         * Add a property pre-filter to control which properties are serialized
         * (without reading the property value — more efficient than PropertyFilter).
         */
        public Builder addPropertyPreFilter(com.alibaba.fastjson3.filter.PropertyPreFilter filter) {
            propertyPreFilters.add(filter);
            return this;
        }

        /**
         * Add a before filter to inject properties at the start of each object.
         */
        public Builder addBeforeFilter(com.alibaba.fastjson3.filter.BeforeFilter filter) {
            beforeFilters.add(filter);
            return this;
        }

        /**
         * Add an after filter to append properties at the end of each object.
         */
        public Builder addAfterFilter(com.alibaba.fastjson3.filter.AfterFilter filter) {
            afterFilters.add(filter);
            return this;
        }

        /**
         * Add a property filter to control which properties are serialized.
         */
        public Builder addPropertyFilter(PropertyFilter filter) {
            propertyFilters.add(filter);
            return this;
        }

        /**
         * Add a value filter to transform property values during serialization.
         */
        public Builder addValueFilter(ValueFilter filter) {
            valueFilters.add(filter);
            return this;
        }

        /**
         * Add a name filter to transform property names during serialization.
         */
        public Builder addNameFilter(NameFilter filter) {
            nameFilters.add(filter);
            return this;
        }

        // ---- Label filter ----

        /**
         * Set a label filter for view-based field filtering.
         * Only fields whose label matches the filter will be serialized.
         */
        public Builder addLabelFilter(com.alibaba.fastjson3.filter.LabelFilter filter) {
            this.labelFilter = filter;
            return this;
        }

        // ---- Jackson annotation support ----

        /**
         * Enable or disable optional Jackson annotation recognition.
         * When enabled, Jackson annotations (@JsonProperty, @JsonIgnore, etc.) are
         * detected via reflection with zero compile-time dependency on Jackson.
         * Native @JSONField/@JSONType annotations always take precedence.
         *
         * <p>Disabled by default. Requires Jackson annotations on the classpath at runtime.</p>
         */
        public Builder useJacksonAnnotation(boolean enable) {
            this.useJacksonAnnotation = enable;
            return this;
        }

        // ---- Mixins ----

        /**
         * Register a mixin class for a target type. Annotations from the mixin class
         * will be applied to the target class during serialization/deserialization.
         *
         * @param target the target class to augment
         * @param mixIn  the mixin class providing annotations
         */
        public Builder addMixIn(Class<?> target, Class<?> mixIn) {
            mixIns.put(target, mixIn);
            return this;
        }

        // ---- Custom readers/writers ----

        /**
         * Register a custom ObjectReader for a specific type.
         */
        public <T> Builder addReader(Class<T> type, ObjectReader<T> reader) {
            readerModules.add(new SingleTypeReaderModule(type, reader));
            return this;
        }

        /**
         * Register a custom ObjectWriter for a specific type.
         */
        public <T> Builder addWriter(Class<T> type, ObjectWriter<T> writer) {
            writerModules.add(new SingleTypeWriterModule(type, writer));
            return this;
        }

        // ---- Build ----

        /**
         * Build an immutable ObjectMapper from the current configuration.
         */
        public ObjectMapper build() {
            // Create per-ObjectMapper DynamicClassLoader for proper cleanup
            com.alibaba.fastjson3.util.DynamicClassLoader loader = this.classLoader;
            if (loader == null) {
                loader = com.alibaba.fastjson3.util.DynamicClassLoader.getSharedInstance();
            }

            // Determine the reader provider
            ObjectReaderProvider provider = readerProviderInstance;
            boolean createdProvider = false;
            if (provider == null && readerCreatorType != null) {
                // Create per-instance provider with custom classLoader
                provider = createProviderWithClassLoader(readerCreatorType, loader);
                createdProvider = true;
            }
            // Update readerCreator to use the provider when it was created from readerCreatorType
            if (createdProvider && readerCreator == null) {
                readerCreator = provider::getObjectReader;
            }

            // Determine the writer provider
            ObjectWriterProvider writerProvider = writerProviderInstance;
            boolean createdWriterProvider = false;
            if (writerProvider == null && writerCreatorType != null) {
                // Create per-instance provider with custom classLoader
                writerProvider = createWriterProviderWithClassLoader(writerCreatorType, loader);
                createdWriterProvider = true;
            }
            // Update writerCreator to use the provider when it was created from writerCreatorType
            if (createdWriterProvider && writerCreator == null) {
                writerCreator = writerProvider::getObjectWriter;
            }

            ObjectMapper mapper = new ObjectMapper(
                    readFeatures,
                    writeFeatures,
                    Collections.unmodifiableList(new ArrayList<ObjectReaderModule>(readerModules)),
                    Collections.unmodifiableList(new ArrayList<ObjectWriterModule>(writerModules)),
                    readerCreator,
                    writerCreator,
                    propertyFilters.toArray(NO_PROPERTY_FILTERS),
                    valueFilters.toArray(NO_VALUE_FILTERS),
                    nameFilters.toArray(NO_NAME_FILTERS),
                    labelFilter,
                    new LinkedHashMap<Class<?>, Class<?>>(mixIns),
                    useJacksonAnnotation,
                    provider,
                    writerProvider,
                    loader,
                    mapSupplier,
                    listSupplier,
                    dateFormat
            );
            // Set before/after/pre filters
            if (!beforeFilters.isEmpty()) {
                mapper.beforeFilters = beforeFilters.toArray(new com.alibaba.fastjson3.filter.BeforeFilter[0]);
            }
            if (!afterFilters.isEmpty()) {
                mapper.afterFilters = afterFilters.toArray(new com.alibaba.fastjson3.filter.AfterFilter[0]);
            }
            if (!propertyPreFilters.isEmpty()) {
                mapper.propertyPreFilters = propertyPreFilters.toArray(
                        new com.alibaba.fastjson3.filter.PropertyPreFilter[0]);
            }
            // Initialize modules
            for (ObjectReaderModule module : mapper.readerModules) {
                module.init();
            }
            for (ObjectWriterModule module : mapper.writerModules) {
                module.init();
            }
            return mapper;
        }
    }

    // ==================== Internal module helpers ====================

    static final class SingleTypeReaderModule implements ObjectReaderModule {
        private final Type type;
        private final ObjectReader<?> reader;

        SingleTypeReaderModule(Type type, ObjectReader<?> reader) {
            this.type = type;
            this.reader = reader;
        }

        @Override
        public ObjectReader<?> getObjectReader(Type t) {
            return type.equals(t) ? reader : null;
        }
    }

    static final class SingleTypeWriterModule implements ObjectWriterModule {
        private final Type type;
        private final ObjectWriter<?> writer;

        SingleTypeWriterModule(Type type, ObjectWriter<?> writer) {
            this.type = type;
            this.writer = writer;
        }

        @Override
        public ObjectWriter<?> getObjectWriter(Type t, Class<?> rawType) {
            return type.equals(t) ? writer : null;
        }
    }
}
