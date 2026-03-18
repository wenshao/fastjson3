package com.alibaba.fastjson3.benchmark;

import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.ObjectReader;
import com.alibaba.fastjson3.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson3.reader.ReaderCreatorType;

/**
 * Debug ASM generation issues
 */
public class ASMDebug {

    public static class SimplePOJO {
        public long id;
        public String name;
    }

    public static class NestedPOJO {
        public long id;
        public String name;
        public SimplePOJO nested;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("==============================================");
        System.out.println("ASM Generation Debug");
        System.out.println("==============================================");

        // Test 1: Direct ASM creation
        System.out.println("\n1. Direct ObjectReaderCreatorASM.createObjectReader():");
        try {
            ObjectReader<SimplePOJO> reader = ObjectReaderCreatorASM.createObjectReader(SimplePOJO.class);
            System.out.println("  SimplePOJO: " + reader.getClass().getName());
        } catch (Exception e) {
            System.out.println("  SimplePOJO FAILED: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            ObjectReader<NestedPOJO> reader = ObjectReaderCreatorASM.createObjectReader(NestedPOJO.class);
            System.out.println("  NestedPOJO: " + reader.getClass().getName());
        } catch (Exception e) {
            System.out.println("  NestedPOJO FAILED: " + e.getMessage());
            e.printStackTrace();
        }

        // Test 2: Via ObjectMapper with ASM provider
        System.out.println("\n2. ObjectMapper with ASM provider:");
        ObjectMapper asmMapper = ObjectMapper.builder()
                .readerCreatorType(ReaderCreatorType.ASM)
                .build();

        ObjectReader<SimplePOJO> pojoReader = asmMapper.getObjectReader(SimplePOJO.class);
        System.out.println("  SimplePOJO: " + pojoReader.getClass().getName());

        ObjectReader<NestedPOJO> nestedReader = asmMapper.getObjectReader(NestedPOJO.class);
        System.out.println("  NestedPOJO: " + nestedReader.getClass().getName());

        // Test 3: Check provider type
        System.out.println("\n3. Provider info:");
        System.out.println("  Provider: " + asmMapper.getClass().getName());
        System.out.println("  Can't access readerProvider directly - it's private");

        // Test with public nested POJO
        System.out.println("\n4. Test with public nested POJO:");
        try {
            ObjectReader<NestedPOJO> reader = ObjectReaderCreatorASM.createObjectReader(NestedPOJO.class);
            System.out.println("  NestedPOJO: " + reader.getClass().getName());
        } catch (Exception e) {
            System.out.println("  NestedPOJO FAILED: " + e.getMessage());
        }
    }
}
