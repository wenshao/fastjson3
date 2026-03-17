package com.alibaba.fastjson3.samples.basic;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.TypeToken;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.annotation.JSONType;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 注解示例：演示使用注解控制序列化行为
 */
public class AnnotationExample {

    @JSONType(naming = com.alibaba.fastjson3.annotation.NamingStrategy.SnakeCase)
    static class Product {
        @JSONField(name = "id")
        private Long productId;

        @JSONField(name = "name")
        private String productName;

        @JSONField(format = "yyyy-MM-dd")
        private LocalDate manufactureDate;

        @JSONField(serialize = false)
        private String internalCode;

        @JSONField(ordinal = 1)
        private String firstField;

        @JSONField(ordinal = 2)
        private String secondField;

        public Product() {}

        public Product(Long id, String name, LocalDate date, String code) {
            this.productId = id;
            this.productName = name;
            this.manufactureDate = date;
            this.internalCode = code;
        }

        // getters & setters
        public Long getProductId() { return productId; }
        public void setProductId(Long id) { productId = id; }
        public String getProductName() { return productName; }
        public void setProductName(String name) { productName = name; }
        public LocalDate getManufactureDate() { return manufactureDate; }
        public void setManufactureDate(LocalDate date) { manufactureDate = date; }
        public String getInternalCode() { return internalCode; }
        public void setInternalCode(String code) { internalCode = code; }
        public String getFirstField() { return firstField; }
        public void setFirstField(String f) { firstField = f; }
        public String getSecondField() { return secondField; }
        public void setSecondField(String s) { secondField = s; }
    }

    public static void main(String[] args) {
        Product product = new Product(1L, "笔记本电脑", LocalDate.of(2024, 1, 15), "INT-001");
        product.setFirstField("值1");
        product.setSecondField("值2");

        System.out.println("=== 注解控制序列化 ===");

        // 序列化
        String json = JSON.toJSONString(product, com.alibaba.fastjson3.WriteFeature.PrettyFormat);
        System.out.println(json);

        /* 输出（注意字段变化）:
        {
            "id":1,
            "name":"笔记本电脑",
            "manufacture_date":"2024-01-15",
            "first_field":"值1",
            "second_field":"值2"
        }
        注意：
        - productId → id (字段重命名)
        - productName → name
        - manufactureDate → manufacture_date (snake_case)
        - internalCode 不输出 (serialize=false)
        - 按 ordinal 顺序输出
        */

        // 反序列化
        String inputJson = """
            {"id":2,"name":"鼠标","manufacture_date":"2024-02-01"}
            """;
        Product parsed = JSON.parseObject(inputJson, Product.class);
        System.out.println("\n反序列化结果:");
        System.out.println("  ID: " + parsed.getProductId());
        System.out.println("  名称: " + parsed.getProductName());
        System.out.println("  日期: " + parsed.getManufactureDate());
    }
}
