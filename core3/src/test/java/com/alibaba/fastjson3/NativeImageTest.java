package com.alibaba.fastjson3;

import com.alibaba.fastjson3.util.JDKUtils;

import java.util.List;
import java.util.Map;

/**
 * Smoke-test program for GraalVM Native Image. Exercises every code path
 * that has shipped without native-image regression coverage to date —
 * each {@code expect(...)} below corresponds to a feature whose breakage
 * in a native binary would be a release blocker.
 *
 * <p>Driven by {@code scripts/test-native-image.sh}. Uses System.exit on
 * failure so the smoke-test script's exit code propagates upstream
 * (CI / release gate).
 */
public class NativeImageTest {

    public static class User {
        public String name;
        public int age;

        public User() {
        }

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    public static class Address {
        public String city;
        public int zipcode;
    }

    public static class UserWithAddress {
        public String name;
        public int age;
        public Address address;
    }

    /** Parent + Child for the inheritance-with-TypeVariable scenario (PR #112). */
    public static class Box<T> {
        public String id;
        public T payload;
        public List<T> history;
    }

    public static class BeanBox extends Box<User> {
        public int extra;
    }

    /** Large bean (>15 fields) — exercises ASM-generator's batched path
     *  even though native-image disables ASM (REFLECT path must still work). */
    public static class LargeBean {
        public String f01, f02, f03, f04, f05, f06, f07, f08, f09, f10;
        public String f11, f12, f13, f14, f15, f16, f17, f18, f19, f20;
        public int n01, n02;
    }

    public record RecordUser(String name, int age) {}

    public static void main(String[] args) {
        System.out.println("=== FastJSON3 Native Image Test ===");
        System.out.println("NATIVE_IMAGE: " + JDKUtils.NATIVE_IMAGE);
        System.out.println("UNSAFE_AVAILABLE: " + JDKUtils.UNSAFE_AVAILABLE);
        System.out.println("FAST_STRING_CREATION: " + JDKUtils.FAST_STRING_CREATION);

        // 1. Simple POJO parse + serialize.
        User u = JSON.parseObject("{\"name\":\"Alice\",\"age\":30}", User.class);
        expect("Alice".equals(u.name) && u.age == 30, "simple parse");
        String written = JSON.toJSONString(u);
        expect(written.contains("\"name\":\"Alice\"") && written.contains("\"age\":30"),
                "simple write: " + written);

        // 2. List<POJO> via parseArray.
        List<User> list = JSON.parseArray(
                "[{\"name\":\"Bob\",\"age\":25},{\"name\":\"Carol\",\"age\":35}]", User.class);
        expect(list.size() == 2 && "Bob".equals(list.get(0).name), "parseArray size + first");

        // 3. TypeReference<List<User>> top-level (PR #110).
        List<User> tref = JSON.parseObject(
                "[{\"name\":\"Dan\",\"age\":40}]", new TypeReference<List<User>>() {});
        expect(tref.size() == 1 && "Dan".equals(tref.get(0).name), "TypeReference<List<User>>");

        // 4. TypeReference<Map<String, User>> top-level (PR #110).
        Map<String, User> mref = JSON.parseObject(
                "{\"alice\":{\"name\":\"Alice\",\"age\":1}}",
                new TypeReference<Map<String, User>>() {});
        expect(mref.size() == 1 && "Alice".equals(mref.get("alice").name),
                "TypeReference<Map<String, User>>");

        // 5. Nested POJO field — exercises generateFieldCase nested-POJO branch.
        UserWithAddress uwa = JSON.parseObject(
                "{\"name\":\"Eve\",\"age\":50,\"address\":{\"city\":\"SF\",\"zipcode\":94101}}",
                UserWithAddress.class);
        expect("SF".equals(uwa.address.city) && uwa.address.zipcode == 94101,
                "nested POJO field");

        // 6. Inherited TypeVariable — PR #112 generic-support
        //    Box<User>.payload (declared T) must resolve to User in BeanBox.
        BeanBox bb = JSON.parseObject(
                "{\"id\":\"b1\",\"payload\":{\"name\":\"P\",\"age\":9},\"extra\":42,"
                        + "\"history\":[{\"name\":\"H\",\"age\":1}]}",
                BeanBox.class);
        expect(bb.payload != null && "P".equals(bb.payload.name) && bb.extra == 42,
                "inherited TypeVariable resolution");
        expect(bb.history != null && bb.history.size() == 1
                && "H".equals(bb.history.get(0).name),
                "inherited List<T> resolution");

        // 7. Large bean (22 fields) — PR #114 validates batched non-fast-path.
        //    In native-image the ASM generator is disabled, so this exercises
        //    REFLECT's readFieldsLoop on a > 15-field POJO.
        LargeBean lb = JSON.parseObject(
                "{\"f01\":\"a\",\"f02\":\"b\",\"f03\":\"c\",\"f04\":\"d\",\"f05\":\"e\","
                        + "\"f06\":\"f\",\"f07\":\"g\",\"f08\":\"h\",\"f09\":\"i\",\"f10\":\"j\","
                        + "\"f11\":\"k\",\"f12\":\"l\",\"f13\":\"m\",\"f14\":\"n\",\"f15\":\"o\","
                        + "\"f16\":\"p\",\"f17\":\"q\",\"f18\":\"r\",\"f19\":\"s\",\"f20\":\"t\","
                        + "\"n01\":1,\"n02\":2}",
                LargeBean.class);
        expect("a".equals(lb.f01) && "t".equals(lb.f20) && lb.n01 == 1 && lb.n02 == 2,
                "large bean (22 fields)");

        // 8. Record (PR #109/#112).
        RecordUser ru = JSON.parseObject("{\"name\":\"Faye\",\"age\":7}", RecordUser.class);
        expect("Faye".equals(ru.name()) && ru.age() == 7, "record parse");

        // 9. JSON-path breadcrumb on bad value (PR #113).
        try {
            JSON.parseObject(
                    "{\"name\":\"X\",\"age\":50,\"address\":{\"city\":\"A\",\"zipcode\":\"bad\"}}",
                    UserWithAddress.class);
            fail("expected JSONException for bad zipcode");
        } catch (JSONException e) {
            expect(e.getMessage() != null && e.getMessage().contains("address.zipcode"),
                    "path breadcrumb in error: " + e.getMessage());
        }

        // 10. Latin1 string fast path (degraded under native-image, must still work).
        String latin1 = "Hello";
        byte[] bytes = latin1.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        String created = JDKUtils.createAsciiString(bytes, 0, bytes.length);
        expect("Hello".equals(created), "Latin1 string roundtrip");

        System.out.println("=== All " + checks + " checks passed! ===");
    }

    private static int checks = 0;

    private static void expect(boolean cond, String label) {
        checks++;
        if (cond) {
            System.out.println("  ✓ " + label);
        } else {
            fail(label);
        }
    }

    private static void fail(String label) {
        System.err.println("  ✗ FAILED: " + label);
        System.exit(1);
    }
}
