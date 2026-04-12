package com.alibaba.fastjson3.benchmark.jjb;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.reader.ReaderCreatorType;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

/**
 * Compare ReaderCreatorType strategies on the User payload.
 * <ul>
 *   <li>AUTO    — ObjectMapper.shared() default strategy</li>
 *   <li>ASM     — forced ASM codegen</li>
 *   <li>REFLECT — forced reflection</li>
 * </ul>
 */
public class UserProviderBenchmark {

    static final byte[] UTF8_BYTES;

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
        UTF8_BYTES = USER_JSON.getBytes();
    }

    @Benchmark
    public Users.User auto_type(AutoState s) {
        return s.mapper.readValue(UTF8_BYTES, Users.User.class);
    }

    @Benchmark
    public Users.User asm_type(ASMState s) {
        return s.mapper.readValue(UTF8_BYTES, Users.User.class);
    }

    @Benchmark
    public Users.User reflect_type(ReflectState s) {
        return s.mapper.readValue(UTF8_BYTES, Users.User.class);
    }

    @State(Scope.Benchmark)
    public static class AutoState {
        ObjectMapper mapper;

        @Setup(Level.Trial)
        public void setup() {
            mapper = ObjectMapper.shared();
            mapper.readValue(UTF8_BYTES, Users.User.class);
        }
    }

    @State(Scope.Benchmark)
    public static class ASMState {
        ObjectMapper mapper;

        @Setup(Level.Trial)
        public void setup() {
            mapper = ObjectMapper.builder()
                    .readerCreatorType(ReaderCreatorType.ASM)
                    .build();
            mapper.readValue(UTF8_BYTES, Users.User.class);
        }
    }

    @State(Scope.Benchmark)
    public static class ReflectState {
        ObjectMapper mapper;

        @Setup(Level.Trial)
        public void setup() {
            mapper = ObjectMapper.builder()
                    .readerCreatorType(ReaderCreatorType.REFLECT)
                    .build();
            mapper.readValue(UTF8_BYTES, Users.User.class);
        }
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.runner.options.Options opts = new org.openjdk.jmh.runner.options.OptionsBuilder()
                .include(UserProviderBenchmark.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .measurementIterations(5)
                .forks(1)
                .threads(16)
                .build();
        new org.openjdk.jmh.runner.Runner(opts).run();
    }
}
