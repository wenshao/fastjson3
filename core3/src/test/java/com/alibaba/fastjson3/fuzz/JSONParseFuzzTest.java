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
