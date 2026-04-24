package com.alibaba.fastjson3.fuzz;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONParser;
import com.alibaba.fastjson3.JSONPath;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import java.lang.reflect.Type;
import java.math.BigDecimal;

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
 * <p>Current targets focus on DoS-adjacent surfaces the 12-round
 * audit surfaced: untyped top-level parse (String + byte[]),
 * {@link JSONParser#checkBigNumberMagnitude} directly,
 * {@code parseObject(json, BigDecimal.class)} end-to-end, and
 * {@link JSONPath} typed coercion. Follow-up targets (BigInteger,
 * typed POJO, generic Map/List) can be added as single
 * {@code @FuzzTest} methods + seed directories.
 *
 * <p>Expected failure modes ({@code JSONException},
 * {@code NumberFormatException}, {@code ArithmeticException}) are
 * caught and ignored. Anything else — {@code NullPointerException},
 * {@code StackOverflowError}, {@code ClassCastException},
 * {@code IllegalArgumentException}, {@code OutOfMemoryError} — is
 * treated by Jazzer as a finding.
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
}
