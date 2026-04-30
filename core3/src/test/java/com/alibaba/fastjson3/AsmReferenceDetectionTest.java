package com.alibaba.fastjson3;

import com.alibaba.fastjson3.writer.WriterCreatorType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for ASM-generated bean writers honoring
 * {@link WriteFeature#ReferenceDetection}. Pre-fix, ASM never called
 * {@code generator.pushReference(bean)} so a cyclic bean graph
 * stack-overflowed even with the feature enabled. The reflection
 * writer at {@code ObjectWriterCreator$ReflectObjectWriter} already
 * detected cycles via {@code FieldWriter.writeObject}'s pushReference;
 * ASM was the asymmetric outlier.
 */
class AsmReferenceDetectionTest {
    public static class Node {
        public String name;
        public Node next;
    }

    @Test
    void asm_detectsCycle_whenReferenceDetectionEnabled() {
        Node a = new Node();
        a.name = "A";
        Node b = new Node();
        b.name = "B";
        a.next = b;
        b.next = a;
        ObjectMapper m = ObjectMapper.builder()
                .writerCreatorType(WriterCreatorType.ASM)
                .enableWrite(WriteFeature.ReferenceDetection)
                .build();
        JSONException ex = assertThrows(JSONException.class, () -> m.writeValueAsString(a));
        assertTrue(ex.getMessage().contains("circular reference"),
                "expected circular reference detection, got: " + ex.getMessage());
    }

    @Test
    void reflect_detectsCycle_whenReferenceDetectionEnabled() {
        // Companion: confirm REFLECT path's existing detection still works.
        Node a = new Node();
        a.name = "A";
        Node b = new Node();
        b.name = "B";
        a.next = b;
        b.next = a;
        ObjectMapper m = ObjectMapper.builder()
                .writerCreatorType(WriterCreatorType.REFLECT)
                .enableWrite(WriteFeature.ReferenceDetection)
                .build();
        JSONException ex = assertThrows(JSONException.class, () -> m.writeValueAsString(a));
        assertTrue(ex.getMessage().contains("circular reference"));
    }

    @Test
    void asm_acyclicGraph_unchangedOutput() {
        // Sanity: the new pushReference / popReference calls must not
        // change output for a non-cyclic bean graph. ASM with
        // ReferenceDetection should still produce the same JSON as
        // ASM without (when there's no cycle).
        Node a = new Node();
        a.name = "A";
        Node b = new Node();
        b.name = "B";
        a.next = b;
        // a -> b -> null; no cycle.
        ObjectMapper m1 = ObjectMapper.builder()
                .writerCreatorType(WriterCreatorType.ASM)
                .enableWrite(WriteFeature.ReferenceDetection)
                .build();
        ObjectMapper m2 = ObjectMapper.builder()
                .writerCreatorType(WriterCreatorType.ASM)
                .build();
        assertEquals(m2.writeValueAsString(a), m1.writeValueAsString(a),
                "ReferenceDetection feature should not change output for acyclic graphs");
    }

    @Test
    void mixed_reflectParent_asmChild_acyclic_noFalsePositive() {
        // qwen R5-PR154-Critical-#1: pre-fix, a reflection bean writer
        // calling into an ASM-generated child writer with
        // ReferenceDetection enabled would double-push the child value
        // (FieldWriter.writeObject pushed the value, then ASM-generated
        // write pushed the same value at entry → put-twice → false
        // "circular reference" on an acyclic graph).
        //
        // Post-fix: reference tracking moved to writer side
        // (ReflectObjectWriter.write + ASM emitted push); FieldWriter no
        // longer pushes the value. Single-source-of-truth tracking,
        // mixed reflection→ASM paths emit correctly.
        Outer o = new Outer();
        o.tag = "x";
        o.first = new Inner("a", 1);
        o.second = new Inner("b", 2);
        ObjectMapper m = ObjectMapper.builder()
                .enableWrite(WriteFeature.ReferenceDetection)
                .build();
        // Outer has @JSONField(format=) so it falls back to REFLECT;
        // Inner is a simple POJO so it's ASM-generatable.
        String out = m.writeValueAsString(o);
        assertTrue(out.contains("\"tag\":\"x\"")
                && out.contains("\"first\":{") && out.contains("\"second\":{"),
                "acyclic mixed reflection→ASM graph must serialize cleanly, got: " + out);
    }

    public static class Inner {
        public String tag;
        public int n;
        public Inner() {}
        public Inner(String tag, int n) { this.tag = tag; this.n = n; }
    }

    public static class Outer {
        // @JSONField(format=) forces REFLECT writer for Outer (per
        // ObjectWriterCreatorASM.canGenerate's @JSONField(format=) gate
        // at line ~178), while Inner (no annotations) gets the default
        // ASM writer. Reproduces the mixed reflection-parent / ASM-child
        // path that qwen flagged.
        @com.alibaba.fastjson3.annotation.JSONField(format = "yyyy-MM-dd")
        public java.time.LocalDateTime when;
        public String tag;
        public Inner first;
        public Inner second;
    }

    @Test
    void asm_referenceDetectionDisabled_isFastPathNoop() {
        // Without the feature, pushReference is a no-op. We can't directly
        // observe that from the public API, but at least confirm output
        // is correct and no exception is thrown.
        Node a = new Node();
        a.name = "A";
        a.next = null;
        ObjectMapper m = ObjectMapper.builder()
                .writerCreatorType(WriterCreatorType.ASM)
                .build();
        String out = m.writeValueAsString(a);
        assertTrue(out.contains("\"name\":\"A\""), "got: " + out);
    }
}
