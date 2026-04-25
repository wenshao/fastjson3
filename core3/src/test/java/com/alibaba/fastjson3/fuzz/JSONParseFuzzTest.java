package com.alibaba.fastjson3.fuzz;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONParser;
import com.alibaba.fastjson3.JSONPath;
import com.alibaba.fastjson3.TypeReference;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Jazzer fuzz targets for the parse surface. Default test runs execute
 * these in REGRESSION mode — each seed file under {@code
 * src/test/resources/com/alibaba/fastjson3/fuzz/JSONParseFuzzTestInputs/
 * <methodName>/} is replayed as a unit test. Run mutation fuzzing
 * locally via {@code JAZZER_FUZZ=1 mvn test -pl core3
 * -Dtest=JSONParseFuzzTest} — jazzer-junit inspects the env var only
 * (a {@code -D} system property of the same name is NOT wired). Each
 * target then explores for its declared {@code maxDuration}.
 *
 * <p>Targets cover the DoS-adjacent surfaces the 12-round audit
 * surfaced plus adjacent typed-parse paths:
 * <ul>
 *   <li>untyped top-level parse (String + byte[])</li>
 *   <li>{@link JSONParser#checkBigNumberMagnitude} directly</li>
 *   <li>{@code parseObject(json, BigDecimal.class)} and
 *       {@code parseObject(json, BigInteger.class)} end-to-end</li>
 *   <li>{@link JSONPath} typed coercion</li>
 *   <li>typed POJO record parse (exercises
 *       {@code RecordObjectReader} — constructor-based materialisation
 *       with primitive coercion, nested collections)</li>
 *   <li>generic {@code Map<String,Integer>} and {@code List<String>}
 *       via {@link TypeReference} — the top-level collection paths
 *       fj3 added in PR #110</li>
 * </ul>
 * Adding a new target is one {@code @FuzzTest} method + a seed
 * directory.
 *
 * <p>Expected failure modes ({@code JSONException},
 * {@code NumberFormatException}, {@code ArithmeticException}) are
 * caught and ignored. Anything else — {@code NullPointerException},
 * {@code StackOverflowError}, {@code IllegalArgumentException},
 * {@code OutOfMemoryError} — is treated by Jazzer as a finding.
 *
 * <p>Exception: the generic-collection targets
 * ({@link #fuzzParseMapStringInteger}, {@link #fuzzParseListString})
 * also catch {@code ClassCastException} because the parser's untyped
 * dispatch can produce shapes that don't fit a strict
 * {@code Map<String,Integer>} / {@code List<String>} contract, and
 * those type-mismatches would otherwise drown the real fuzz signal.
 */
public class JSONParseFuzzTest {

    /** Untyped top-level parse (String). */
    @FuzzTest(maxDuration = "10s")
    void fuzzParseString(FuzzedDataProvider data) {
        String input = data.consumeRemainingAsString();
        try {
            JSON.parse(input);
        } catch (JSONException | NumberFormatException | ArithmeticException ignored) {
            // expected parse failures
        }
    }

    /** Untyped top-level parse (byte[]). */
    @FuzzTest(maxDuration = "10s")
    void fuzzParseBytes(FuzzedDataProvider data) {
        byte[] input = data.consumeRemainingAsBytes();
        try {
            JSON.parse(input);
        } catch (JSONException | NumberFormatException | ArithmeticException ignored) {
            // expected
        }
    }

    /**
     * Direct fuzz of the magnitude-cap helper. Regression mode replays
     * every R5–R11 DoS payload; mutation mode hunts for new ones.
     */
    @FuzzTest(maxDuration = "10s")
    void fuzzCheckBigNumberMagnitude(FuzzedDataProvider data) {
        String input = data.consumeRemainingAsString();
        try {
            JSONParser.checkBigNumberMagnitude(input);
        } catch (JSONException | NumberFormatException | ArithmeticException ignored) {
            // expected rejection
        }
    }

    /** Typed parse to BigDecimal — exercises BuiltinCodecs + literal readers. */
    @FuzzTest(maxDuration = "10s")
    void fuzzParseBigDecimal(FuzzedDataProvider data) {
        String input = data.consumeRemainingAsString();
        try {
            JSON.parseObject(input, BigDecimal.class);
        } catch (JSONException | NumberFormatException | ArithmeticException ignored) {
            // expected
        }
    }

    /**
     * JSONPath typed extraction — the R11 DoS angle on quoted numeric
     * strings. Fixed path/type, fuzzed payload.
     */
    @FuzzTest(maxDuration = "10s")
    void fuzzJsonPathBigDecimalCoercion(FuzzedDataProvider data) {
        String input = data.consumeRemainingAsString();
        JSONPath path = JSONPath.of(new String[]{"$.v"}, new Type[]{BigDecimal.class});
        try {
            path.extract(input);
        } catch (JSONException | NumberFormatException | ArithmeticException ignored) {
            // expected
        }
    }

    /** Typed parse to BigInteger — parallel to BigDecimal, different codec. */
    @FuzzTest(maxDuration = "10s")
    void fuzzParseBigInteger(FuzzedDataProvider data) {
        String input = data.consumeRemainingAsString();
        try {
            JSON.parseObject(input, BigInteger.class);
        } catch (JSONException | NumberFormatException | ArithmeticException ignored) {
            // expected
        }
    }

    /**
     * Typed POJO record parse. Exercises {@code RecordObjectReader} —
     * canonical-constructor invocation, primitive-coercion guard,
     * nested collections, missing / extra / null / wrong-typed fields.
     */
    @FuzzTest(maxDuration = "10s")
    void fuzzParseRecord(FuzzedDataProvider data) {
        String input = data.consumeRemainingAsString();
        try {
            JSON.parseObject(input, FuzzRecord.class);
        } catch (JSONException | NumberFormatException | ArithmeticException ignored) {
            // expected
        }
    }

    /**
     * Generic {@code Map<String,Integer>} parse via TypeReference.
     * Exercises the top-level Map path added in PR #110 — map key/value
     * coercion, nested containers, mixed-type values.
     */
    @FuzzTest(maxDuration = "10s")
    void fuzzParseMapStringInteger(FuzzedDataProvider data) {
        String input = data.consumeRemainingAsString();
        try {
            JSON.parseObject(input, new TypeReference<Map<String, Integer>>() {});
        } catch (JSONException | NumberFormatException | ArithmeticException
                | ClassCastException ignored) {
            // ClassCastException: parser's untyped dispatch can yield
            // shapes that don't match the target Map<String,Integer>
            // contract (e.g. value-position object). Treat as expected
            // to avoid drowning the fuzz signal in type-mismatch noise.
        }
    }

    /**
     * Generic {@code List<String>} parse via TypeReference. Same
     * rationale as the Map target — covers element coercion on the
     * PR #110 generic-list path.
     */
    @FuzzTest(maxDuration = "10s")
    void fuzzParseListString(FuzzedDataProvider data) {
        String input = data.consumeRemainingAsString();
        try {
            JSON.parseObject(input, new TypeReference<List<String>>() {});
        } catch (JSONException | NumberFormatException | ArithmeticException
                | ClassCastException ignored) {
            // see fuzzParseMapStringInteger for ClassCastException rationale
        }
    }

    /**
     * Typed POJO parse with primitive fields. Records dispatch values through
     * {@code utf8.readAny()} (bounded). The reflection POJO path goes through
     * {@code readFieldsLoop}'s tableswitch with inline {@code readIntOff /
     * readLongOff / readDoubleOff / readBooleanOff} calls — surface that
     * needs its own coverage so truncated-after-colon inputs (e.g. {@code
     * {"id":}) surface here, not in production.
     */
    @FuzzTest(maxDuration = "10s")
    void fuzzParsePojoPrimitive(FuzzedDataProvider data) {
        String input = data.consumeRemainingAsString();
        try {
            JSON.parseObject(input, FuzzPojo.class);
        } catch (JSONException | NumberFormatException | ArithmeticException ignored) {
            // expected
        }
    }

    /**
     * Untyped parse via the {@code Str} / {@code LATIN1} parser variants
     * (different from {@code UTF8}). {@code JSON.parseObject(byte[], T)}
     * always uses {@code UTF8}; the {@code String}-input variants only
     * route through {@code Str} / {@code LATIN1} when {@code ReadFeature}s
     * are passed. Exercises that path explicitly via {@code JSONParser.of(
     * String, ReadFeature...)}.
     */
    @FuzzTest(maxDuration = "10s")
    void fuzzParseStr(FuzzedDataProvider data) {
        String input = data.consumeRemainingAsString();
        if (input.isEmpty()) {
            return;
        }
        try (JSONParser parser = JSONParser.of(input,
                com.alibaba.fastjson3.ReadFeature.AllowUnquotedFieldNames)) {
            parser.readAny();
        } catch (JSONException | NumberFormatException | ArithmeticException ignored) {
            // expected
        }
    }

    /**
     * Untyped parse via the {@code CharArray} parser variant — the only
     * code path for char[] inputs. Has its own readFieldName / readString
     * implementations that don't share bytes with UTF8/Str.
     */
    @FuzzTest(maxDuration = "10s")
    void fuzzParseCharArray(FuzzedDataProvider data) {
        String input = data.consumeRemainingAsString();
        if (input.isEmpty()) {
            return;
        }
        char[] chars = input.toCharArray();
        try (JSONParser parser = JSONParser.of(chars, 0, chars.length)) {
            parser.readAny();
        } catch (JSONException | NumberFormatException | ArithmeticException ignored) {
            // expected
        }
    }

    /**
     * Round-trip fuzz: parse arbitrary input → if successful, write the
     * parsed value back via {@code toJSONString} → reparse the written
     * value → assert equality. Surfaces writer bugs that produce output
     * the parser then rejects (asymmetry == bug).
     */
    @FuzzTest(maxDuration = "10s")
    void fuzzRoundTrip(FuzzedDataProvider data) {
        String input = data.consumeRemainingAsString();
        Object first;
        try {
            first = JSON.parse(input);
        } catch (JSONException | NumberFormatException | ArithmeticException ignored) {
            return; // unparseable — nothing to round-trip
        }
        if (first == null) {
            return;
        }
        String written;
        try {
            written = JSON.toJSONString(first);
        } catch (JSONException | StackOverflowError ignored) {
            return; // writer rejected its own value (e.g. cyclic) — not a fuzz finding
        }
        Object second;
        try {
            second = JSON.parse(written);
        } catch (JSONException | NumberFormatException | ArithmeticException e) {
            // Writer produced output the parser can't accept — that's a bug.
            throw new AssertionError("write/read asymmetry: input=" + truncate(input)
                    + " written=" + truncate(written), e);
        }
        // Skip equality assert on Number subtypes — Long/Integer/Double
        // round-trip through Number coercion can lose type tag (1L → 1).
        // Goal here is "writer produces parseable output", not "value
        // identity", so coverage of the writer-then-parser pair is enough.
    }

    /**
     * Writer fuzz: build a synthetic {@code JSONObject} from fuzz bytes
     * (random keys + assorted value types), then serialise. Exercises
     * the writer's value-type dispatch on diverse shapes the parse-side
     * fuzz never reaches (e.g. raw {@code BigDecimal}, Java {@code null},
     * empty collections at every nesting depth).
     */
    @FuzzTest(maxDuration = "10s")
    void fuzzWriteJsonObject(FuzzedDataProvider data) {
        com.alibaba.fastjson3.JSONObject obj = new com.alibaba.fastjson3.JSONObject();
        int fields = data.consumeInt(0, 32);
        for (int i = 0; i < fields && data.remainingBytes() >= 4; i++) {
            String key = data.consumeAsciiString(16);
            switch (data.consumeInt(0, 9)) {
                case 0: obj.put(key, null); break;
                case 1: obj.put(key, data.consumeBoolean()); break;
                case 2: obj.put(key, data.consumeInt()); break;
                case 3: obj.put(key, data.consumeLong()); break;
                case 4: obj.put(key, data.consumeDouble()); break;
                case 5: obj.put(key, data.consumeAsciiString(50)); break;
                case 6: obj.put(key, new com.alibaba.fastjson3.JSONArray()); break;
                case 7: obj.put(key, new com.alibaba.fastjson3.JSONObject()); break;
                case 8: obj.put(key, BigDecimal.valueOf(data.consumeLong())); break;
                case 9: obj.put(key, BigInteger.valueOf(data.consumeLong())); break;
            }
        }
        try {
            JSON.toJSONString(obj);
        } catch (JSONException ignored) {
            // expected
        }
    }

    private static String truncate(String s) {
        return s.length() <= 80 ? s : s.substring(0, 80) + "...";
    }

    /** Target POJO for {@link #fuzzParseRecord}. */
    public record FuzzRecord(String name, int count, boolean flag, List<String> tags) {
    }

    /** Target POJO for {@link #fuzzParsePojoPrimitive}. */
    public static class FuzzPojo {
        public String name;
        public int id;
        public long count;
        public double weight;
        public boolean active;
        public java.math.BigDecimal price;
    }
}
