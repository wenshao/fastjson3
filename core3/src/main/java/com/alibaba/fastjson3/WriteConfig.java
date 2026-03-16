package com.alibaba.fastjson3;

/**
 * Semantic configuration presets for JSON serialization.
 * <p>These presets provide common feature combinations without requiring
 * knowledge of individual {@link WriteFeature} flags.
 *
 * <h3>Usage:</h3>
 * <pre>
 * // Pretty output for logging
 * String json = JSON.write(obj, WriteConfig.PRETTY);
 *
 * // Include null values
 * String json = JSON.write(obj, WriteConfig.WITH_NULLS);
 *
 * // Pretty with nulls
 * String json = JSON.write(obj, WriteConfig.PRETTY_WITH_NULLS);
 * </pre>
 *
 * <h3>Presets:</h3>
 * <table border="1">
 * <caption>Write Config Presets</caption>
 * <tr><th>Preset</th><th>Description</th><th>Use Case</th></tr>
 * <tr><td>{@link #DEFAULT}</td><td>Standard JSON output</td><td>General purpose, network transmission</td></tr>
 * <tr><td>{@link #PRETTY}</td><td>Formatted with indentation</td><td>Logging, debugging, display</td></tr>
 * <tr><td>{@link #WITH_NULLS}</td><td>Include null field values</td><td>When nulls are meaningful</td></tr>
 * <tr><td>{@link #PRETTY_WITH_NULLS}</td><td>Formatted with nulls included</td><td>Debugging complete data</td></tr>
 * </table>
 *
 * @see ParseConfig
 * @see WriteFeature
 */
public enum WriteConfig {
    /**
     * Standard JSON output with default settings.
     * <p>No special formatting. Omits null values by default.</p>
     * <p><b>Use case:</b> Network transmission, storage, general use</p>
     */
    DEFAULT(new WriteFeature[0]),

    /**
     * Pretty formatted JSON output.
     * <p>Enabled features:</p>
     * <ul>
     *   <li>{@link WriteFeature#PrettyFormat} - Format with indentation</li>
     * </ul>
     * <p><b>Use case:</b> Logging, debugging, human-readable output</p>
     */
    PRETTY(new WriteFeature[]{
        WriteFeature.PrettyFormat
    }),

    /**
     * Include null values in output.
     * <p>Enabled features:</p>
     * <ul>
     *   <li>{@link WriteFeature#WriteNulls} - Write null values instead of omitting</li>
     * </ul>
     * <p><b>Use case:</b> When null values have semantic meaning</p>
     */
    WITH_NULLS(new WriteFeature[]{
        WriteFeature.WriteNulls
    }),

    /**
     * Pretty formatted JSON with null values included.
     * <p>Enabled features:</p>
     * <ul>
     *   <li>{@link WriteFeature#PrettyFormat} - Format with indentation</li>
     *   <li>{@link WriteFeature#WriteNulls} - Write null values instead of omitting</li>
     * </ul>
     * <p><b>Use case:</b> Debugging, displaying complete data structures</p>
     */
    PRETTY_WITH_NULLS(new WriteFeature[]{
        WriteFeature.PrettyFormat,
        WriteFeature.WriteNulls
    });

    private final WriteFeature[] features;
    private final long mask;

    WriteConfig(WriteFeature[] features) {
        this.features = features.clone();
        this.mask = WriteFeature.of(features);
    }

    /**
     * Returns the {@link WriteFeature} flags for this preset.
     *
     * @return a defensive copy of the feature array
     */
    public WriteFeature[] features() {
        return features.clone();
    }

    /**
     * Returns the feature mask as a long value.
     * <p>This is the internal representation used by the generator.</p>
     *
     * @return the feature mask
     */
    public long mask() {
        return mask;
    }
}
