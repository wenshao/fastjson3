package com.alibaba.fastjson3.benchmark.eishay;

import com.alibaba.fastjson3.benchmark.eishay.vo.Image;
import com.alibaba.fastjson3.benchmark.eishay.vo.Media;
import com.alibaba.fastjson3.benchmark.eishay.vo.MediaContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class EishayParseString {
    static String str;
    static byte[] utf8Bytes; // for isolating String→UTF8 overhead

    // Isolated leaf-type JSON strings for bypassing MediaContent's outer dispatch.
    // These let us measure fj3 vs fj2 ASM / REFLECT readers directly at Media and Image level.
    static final String MEDIA_JSON =
            "{\"bitrate\":262144,\"duration\":18000000,\"format\":\"video/mpg4\","
                    + "\"height\":480,\"persons\":[\"Bill Gates\",\"Steve Jobs\"],"
                    + "\"player\":\"JAVA\",\"size\":58982400,\"title\":\"Javaone Keynote\","
                    + "\"uri\":\"http://javaone.com/keynote.mpg\",\"width\":640}";
    static final String IMAGE_JSON =
            "{\"height\":768,\"size\":\"LARGE\",\"title\":\"Javaone Keynote\","
                    + "\"uri\":\"http://javaone.com/keynote_large.jpg\",\"width\":1024}";
    static final ObjectMapper jackson = new ObjectMapper();
    static final Gson gson = new Gson();
    static com.alibaba.fastjson3.ObjectReader<MediaContent> reflectReader;
    static com.alibaba.fastjson3.ObjectReader<MediaContent> asmReader;
    static final com.alibaba.fastjson3.ObjectMapper asmMapper = com.alibaba.fastjson3.ObjectMapper.builder()
            .readerCreatorType(com.alibaba.fastjson3.reader.ReaderCreatorType.ASM)
            .build();

    static {
        try {
            try (InputStream is = EishayParseString.class.getClassLoader()
                    .getResourceAsStream("data/eishay/eishay_compact.json")) {
                utf8Bytes = is.readAllBytes();
            }
            str = new String(utf8Bytes, StandardCharsets.UTF_8);
            reflectReader = com.alibaba.fastjson3.reader.ObjectReaderCreator.createObjectReader(MediaContent.class);
            asmReader = com.alibaba.fastjson3.reader.ObjectReaderCreatorASM.createObjectReader(MediaContent.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(str, MediaContent.class));
    }

    @Benchmark
    public void fastjson3(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(str, MediaContent.class));
    }

    @Benchmark
    public void fastjson3_asm(Blackhole bh) {
        bh.consume(asmMapper.readValue(str, MediaContent.class));
    }

    /** Parse via Str parser directly, bypassing String→UTF8 conversion */
    @Benchmark
    public void fastjson3_str_parser(Blackhole bh) {
        try (com.alibaba.fastjson3.JSONParser parser = com.alibaba.fastjson3.JSONParser.of(str)) {
            bh.consume(parser.read(MediaContent.class));
        }
    }

    /** Pre-converted UTF8, isolates String→byte[] conversion cost */
    @Benchmark
    public void fastjson3_preconv_utf8(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(utf8Bytes, MediaContent.class));
    }

    @Benchmark
    public void jackson(Blackhole bh) throws Exception {
        bh.consume(jackson.readValue(str, MediaContent.class));
    }

    @Benchmark
    public void gson(Blackhole bh) {
        bh.consume(gson.fromJson(str, MediaContent.class));
    }

    @Benchmark
    public void wast(Blackhole bh) {
        bh.consume(io.github.wycst.wast.json.JSON.parseObject(str, MediaContent.class));
    }

    // ======================================================================
    //   Isolated leaf-level benchmarks (Media-only, Image-only)
    //   Used to measure fj3 vs fj2 ASM / REFLECT readers at the leaf level,
    //   bypassing MediaContent's outer field dispatch. This tells us whether
    //   the nested-reader gap is at the leaf level or in dispatch.
    // ======================================================================

    @Benchmark
    public void media_fj2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(MEDIA_JSON, Media.class));
    }

    @Benchmark
    public void media_fj3_reflect(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(MEDIA_JSON, Media.class));
    }

    @Benchmark
    public void media_fj3_asm(Blackhole bh) {
        bh.consume(asmMapper.readValue(MEDIA_JSON, Media.class));
    }

    @Benchmark
    public void image_fj2(Blackhole bh) {
        bh.consume(com.alibaba.fastjson2.JSON.parseObject(IMAGE_JSON, Image.class));
    }

    @Benchmark
    public void image_fj3_reflect(Blackhole bh) {
        bh.consume(com.alibaba.fastjson3.JSON.parseObject(IMAGE_JSON, Image.class));
    }

    @Benchmark
    public void image_fj3_asm(Blackhole bh) {
        bh.consume(asmMapper.readValue(IMAGE_JSON, Image.class));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(EishayParseString.class.getName())
                .exclude(EishayParseStringPretty.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(2)
                .measurementIterations(3)
                .forks(2)
                .threads(16)
                .build();
        new Runner(options).run();
    }
}
