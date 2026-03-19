/**
 * Fastjson3 — high-performance JSON library for Java 21+.
 *
 * <p>Public API:
 * <ul>
 *   <li>{@code com.alibaba.fastjson3} — core types: JSON, ObjectMapper, JSONParser, JSONGenerator,
 *       ObjectReader, ObjectWriter, ReadFeature, WriteFeature, TypeReference</li>
 *   <li>{@code com.alibaba.fastjson3.annotation} — @JSONField, @JSONType, @JSONCreator, NamingStrategy</li>
 *   <li>{@code com.alibaba.fastjson3.writer} — WriterCreatorType, ObjectWriterProvider</li>
 *   <li>{@code com.alibaba.fastjson3.reader} — ReaderCreatorType, ObjectReaderProvider</li>
 *   <li>{@code com.alibaba.fastjson3.modules} — extension SPI: ObjectReaderModule, ObjectWriterModule</li>
 * </ul>
 *
 * <p>Internal packages ({@code util}) are NOT exported.
 * Do not depend on them — they may change without notice.
 */
module com.alibaba.fastjson3 {
    requires jdk.unsupported; // for sun.misc.Unsafe (fast String creation, field access)
    requires static jdk.incubator.vector; // optional: SIMD-accelerated string scanning

    // Public API
    exports com.alibaba.fastjson3;
    exports com.alibaba.fastjson3.annotation;
    exports com.alibaba.fastjson3.filter;
    exports com.alibaba.fastjson3.modules;
    exports com.alibaba.fastjson3.writer;
    exports com.alibaba.fastjson3.reader;

    // Internal packages — NOT exported:
    //   com.alibaba.fastjson3.util  (JDKUtils, UnsafeAllocator, BufferPool)
}
