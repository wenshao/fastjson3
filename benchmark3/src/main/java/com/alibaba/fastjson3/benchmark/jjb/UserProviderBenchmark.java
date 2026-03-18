package com.alibaba.fastjson3.benchmark.jjb;

import com.alibaba.fastjson3.JSONParser;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.reader.ReaderCreatorType;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Test ObjectReaderProvider with different ReaderCreatorType strategies.
 * Tests the User class (23 fields) with:
 * - AUTO: automatic strategy selection
 * - ASM: forced ASM generation
 * - REFLECT: forced reflection
 */
public class UserProviderBenchmark {

    static byte[] utf8Bytes;

    // Single User JSON (23 fields + nested types)
    static final String USER_JSON = """
        {
            "about":"6D64jf3ia1g5yMOGLPag",
            "address":"T3TOoBQTV0u7DRPx8ZIc",
            "age":7,
            "balance":"7ntZasFKbEXAtaW1Nu3D",
            "company":"dcnie9rfwYUOztw9GmqX",
            "email":"fem0ungSAX1yslDMkkaW",
            "eyeColor":"qLCoY5FLA0Zt9JIHJ8MR",
            "favoriteFruit":"96KPJxH8NKlTU3YKYVUB",
            "friends":[
                {"id":"470","name":"MknlKbqSPtMhqcdsoDjZMmNQwUQWIe"},
                {"id":"1319","name":"woWUvBwdGLFLOAvDKUNMshwQAdIPWo"}
            ],
            "gender":"9cYKMs3HsG3IbgspGeda",
            "greeting":"NpUykrVbL37pE9FFTadC",
            "guid":"Vr2Udrm9xEUAikvUyIua",
            "id":"37136587875948954752",
            "index":724263927,
            "isActive":false,
            "latitude":82.93807772236312,
            "longitude":159.3915724620277,
            "name":"UEuUItlnfpHSU4oDNFwN",
            "phone":"mW4wKsnDVxIrJ3JR3ssr",
            "picture":"t1GrN0zYWxnhw7xXvlVH",
            "registered":"27XLpUtACK4nptxwSh0b",
            "tags":["CvdhGyJKOX","iJTnB3nz2y"]
        }
        """;

    static {
        utf8Bytes = USER_JSON.getBytes();
    }

    // ==================== AUTO ====================

    @Benchmark
    public Users.User auto_type() {
        try (JSONParser parser = JSONParser.of(utf8Bytes)) {
            return parser.read(Users.User.class);
        }
    }

    // ==================== ASM ====================

    @Benchmark
    public Users.User asm_type() {
        try (JSONParser parser = JSONParser.of(utf8Bytes)) {
            return parser.read(Users.User.class);
        }
    }

    // ==================== REFLECT ====================

    @Benchmark
    public Users.User reflect_type() {
        try (JSONParser parser = JSONParser.of(utf8Bytes)) {
            return parser.read(Users.User.class);
        }
    }

    // ==================== Setup methods ====================

    @State(Scope.Benchmark)
    public static class AutoState {
        ObjectMapper mapper = ObjectMapper.shared();
        @Setup(Level.Trial)
        public void setup() {
            // Reset to default
        }
    }

    @State(Scope.Benchmark)
    public static class ASMState {
        ObjectMapper mapper = ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.ASM)
                .build();

        @Setup(Level.Invocation)
        public void setup() {
            // Warmup the cache
            mapper.readValue(utf8Bytes, Users.User.class);
        }
    }

    @State(Scope.Benchmark)
    public static class ReflectState {
        ObjectMapper mapper = ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.REFLECT)
                .build();

        @Setup(Level.Invocation)
        public void setup() {
            // Warmup the cache
            mapper.readValue(utf8Bytes, Users.User.class);
        }
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.runner.options.Options opts = new org.openjdk.jmh.runner.options.OptionsBuilder()
                .include(UserProviderBenchmark.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .measurementIterations(5)
                .forks(2)
                .threads(1)
                .build();
        new org.openjdk.jmh.runner.Runner(opts).run();
    }
}
