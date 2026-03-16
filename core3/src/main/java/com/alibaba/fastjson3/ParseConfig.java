package com.alibaba.fastjson3;

/**
 * Semantic configuration presets for JSON parsing.
 * <p>These presets provide common feature combinations without requiring
 * knowledge of individual {@link ReadFeature} flags.
 *
 * <h3>Usage:</h3>
 * <pre>
 * // Lenient parsing for config files
 * User user = JSON.parse(configJson, User.class, ParseConfig.LENIENT);
 *
 * // Strict parsing for API contracts
 * User user = JSON.parse(apiJson, User.class, ParseConfig.STRICT);
 *
 * // API mode with type safety
 * User user = JSON.parse(apiJson, User.class, ParseConfig.API);
 * </pre>
 *
 * <h3>Presets:</h3>
 * <table border="1">
 * <caption>Parse Config Presets</caption>
 * <tr><th>Preset</th><th>Description</th><th>Use Case</th></tr>
 * <tr><td>{@link #DEFAULT}</td><td>Standard JSON parsing</td><td>General purpose</td></tr>
 * <tr><td>{@link #LENIENT}</td><td>Allows comments, single quotes, unquoted fields, smart match</td><td>Config files, user input</td></tr>
 * <tr><td>{@link #STRICT}</td><td>Errors on unknown properties, null for primitives</td><td>API contracts</td></tr>
 * <tr><td>{@link #API}</td><td>Strict mode + BigDecimal for doubles</td><td>API with type safety</td></tr>
 * </table>
 *
 * @see WriteConfig
 * @see ReadFeature
 */
public enum ParseConfig {
    /**
     * Standard JSON parsing with default settings.
     * <p>No special features enabled. Suitable for most general-purpose JSON processing.</p>
     */
    DEFAULT(new ReadFeature[0]),

    /**
     * Lenient parsing for configuration files and user input.
     * <p>Enabled features:</p>
     * <ul>
     *   <li>{@link ReadFeature#AllowComments} - Allow // and /* * / comments</li>
     *   <li>{@link ReadFeature#AllowSingleQuotes} - Allow single-quoted strings</li>
     *   <li>{@link ReadFeature#AllowUnquotedFieldNames} - Allow unquoted field names</li>
     *   <li>{@link ReadFeature#SupportSmartMatch} - Case-insensitive and underscore-insensitive matching</li>
     * </ul>
     * <p><b>Use case:</b> Configuration files, user-generated content, debugging</p>
     */
    LENIENT(new ReadFeature[]{
        ReadFeature.AllowComments,
        ReadFeature.AllowSingleQuotes,
        ReadFeature.AllowUnquotedFieldNames,
        ReadFeature.SupportSmartMatch
    }),

    /**
     * Strict parsing for API contracts and data exchange.
     * <p>Enabled features:</p>
     * <ul>
     *   <li>{@link ReadFeature#ErrorOnUnknownProperties} - Throw exception on unknown properties</li>
     *   <li>{@link ReadFeature#ErrorOnNullForPrimitives} - Throw exception when null for primitive types</li>
     * </ul>
     * <p><b>Use case:</b> API responses, data validation, strict schemas</p>
     */
    STRICT(new ReadFeature[]{
        ReadFeature.ErrorOnUnknownProperties,
        ReadFeature.ErrorOnNullForPrimitives
    }),

    /**
     * API mode with strict validation and type safety.
     * <p>Enabled features:</p>
     * <ul>
     *   <li>{@link ReadFeature#ErrorOnUnknownProperties} - Throw exception on unknown properties</li>
     *   <li>{@link ReadFeature#ErrorOnNullForPrimitives} - Throw exception when null for primitive types</li>
     *   <li>{@link ReadFeature#UseBigDecimalForDoubles} - Use BigDecimal for double values</li>
     * </ul>
     * <p><b>Use case:</b> Financial data, precise calculations, strict API contracts</p>
     */
    API(new ReadFeature[]{
        ReadFeature.ErrorOnUnknownProperties,
        ReadFeature.ErrorOnNullForPrimitives,
        ReadFeature.UseBigDecimalForDoubles
    });

    private final ReadFeature[] features;
    private final long mask;

    ParseConfig(ReadFeature[] features) {
        this.features = features.clone();
        this.mask = ReadFeature.of(features);
    }

    /**
     * Returns the {@link ReadFeature} flags for this preset.
     *
     * @return a defensive copy of the feature array
     */
    public ReadFeature[] features() {
        return features.clone();
    }

    /**
     * Returns the feature mask as a long value.
     * <p>This is the internal representation used by the parser.</p>
     *
     * @return the feature mask
     */
    public long mask() {
        return mask;
    }
}
