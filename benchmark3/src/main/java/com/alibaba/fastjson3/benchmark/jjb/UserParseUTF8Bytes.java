package com.alibaba.fastjson3.benchmark.jjb;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 直接测试 User 类（23个字段）的 ASM vs Reflection 性能
 * 这才是真正的性能瓶颈所在
 */
public class UserParseUTF8Bytes {
    static byte[] utf8Bytes;
    static com.alibaba.fastjson3.ObjectReader<Users.User> reflectReader;
    static com.alibaba.fastjson3.ObjectReader<Users.User> asmReader;

    static {
        // 单个 User 对象的 JSON（23个字段）
        utf8Bytes = """
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
            """.getBytes();

        reflectReader = com.alibaba.fastjson3.reader.ObjectReaderCreator.createObjectReader(Users.User.class);
        asmReader = com.alibaba.fastjson3.reader.ObjectReaderCreatorASM.createObjectReader(Users.User.class);
    }

    @Benchmark
    public void fastjson3_reflect(Blackhole bh) {
        try (com.alibaba.fastjson3.JSONParser parser = com.alibaba.fastjson3.JSONParser.of(utf8Bytes)) {
            bh.consume(reflectReader.readObject(parser, null, null, 0));
        }
    }

    @Benchmark
    public void fastjson3_asm(Blackhole bh) {
        try (com.alibaba.fastjson3.JSONParser parser = com.alibaba.fastjson3.JSONParser.of(utf8Bytes)) {
            bh.consume(asmReader.readObject(parser, null, null, 0));
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(UserParseUTF8Bytes.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .measurementIterations(5)
                .forks(2)
                .threads(1)
                .build();
        new Runner(options).run();
    }
}
