package com.alibaba.fastjson3.samples.jsonpath;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.JSONPath;

/**
 * JSONPath 示例：演示 JSONPath 查询功能
 */
public class JSONPathExample {

    public static void main(String[] args) {
        String json = """
            {
                "store": {
                    "book": [
                        {"category": "reference", "author": "Nigel Rees", "price": 8.95, "title": "Sayings of the Century"},
                        {"category": "fiction", "author": "Evelyn Waugh", "price": 12.99, "title": "Sword of Honour"},
                        {"category": "fiction", "author": "Herman Melville", "price": 8.99, "title": "Moby Dick"},
                        {"category": "fiction", "author": "J. R. R. Tolkien", "price": 22.99, "title": "The Lord of the Rings"}
                    ],
                    "bicycle": {"color": "red", "price": 19.95, "type": "bike"}
                },
                "expensive": 10
            }
            """;

        System.out.println("=== JSONPath 示例 ===\n");

        // 1. 获取根节点
        System.out.println("1. 获取根节点 $");
        JSONPath rootPath = JSONPath.of("$");
        JSONObject root = rootPath.eval(json, JSONObject.class);
        System.out.println("  根节点类型: " + root.getClass().getSimpleName());

        // 2. 获取嵌套属性
        System.out.println("\n2. 获取嵌套属性 $.store.bicycle.color");
        JSONPath colorPath = JSONPath.of("$.store.bicycle.color");
        String color = colorPath.eval(json, String.class);
        System.out.println("  自行车颜色: " + color);

        // 3. 数组索引
        System.out.println("\n3. 数组索引 $.store.book[0]");
        JSONPath firstBookPath = JSONPath.of("$.store.book[0]");
        JSONObject firstBook = firstBookPath.eval(json, JSONObject.class);
        System.out.println("  第一本书: " + firstBook.get("title"));

        // 4. 获取最后一本书
        System.out.println("\n4. 获取最后一本书 $.store.book[-1]");
        JSONPath lastBookPath = JSONPath.of("$.store.book[-1]");
        JSONObject lastBook = lastBookPath.eval(json, JSONObject.class);
        System.out.println("  最后一本书: " + lastBook.get("title"));

        // 5. 通配符 - 所有作者
        System.out.println("\n5. 获取所有作者 $.store.book[*].author");
        JSONPath authorsPath = JSONPath.of("$.store.book[*].author");
        List<String> authors = authorsPath.extract(json, List.class);
        for (String author : authors) {
            System.out.println("  - " + author);
        }

        // 6. 过滤 - 价格小于 10 的书
        System.out.println("\n6. 价格小于 10 的书 $.store.book[?(@.price < 10)]");
        JSONPath cheapBooksPath = JSONPath.of("$.store.book[?(@.price < 10)]");
        JSONArray cheapBooks = cheapBooksPath.eval(json, JSONArray.class);
        System.out.println("  便宜的书: " + cheapBooks.size() + " 本");
        for (int i = 0; i < cheapBooks.size(); i++) {
            JSONObject book = cheapBooks.getJSONObject(i);
            System.out.println("    - " + book.get("title") + ": $" + book.get("price"));
        }

        // 7. 过滤 - fiction 类别
        System.out.println("\n7. fiction 类别的书 $.store.book[?(@.category == 'fiction')]");
        JSONPath fictionBooksPath = JSONPath.of("$.store.book[?(@.category == 'fiction')]");
        JSONArray fictionBooks = fictionBooksPath.eval(json, JSONArray.class);
        System.out.println("  小说: " + fictionBooks.size() + " 本");

        // 8. 数组切片
        System.out.println("\n8. 前两本书 $.store.book[0:2]");
        JSONPath slicePath = JSONPath.of("$.store.book[0:2]");
        JSONArray slice = slicePath.eval(json, JSONArray.class);
        System.out.println("  前2本: " + slice.size() + " 本");

        // 9. 多值查询
        System.out.println("\n9. 多值查询 $.store.book[0].[author, price, title]");
        JSONPath multiPath = JSONPath.of("$.store.book[0].[author, price, title]");
        Object[] values = multiPath.eval(json, Object[].class);
        System.out.println("  作者: " + values[0]);
        System.out.println("  价格: " + values[1]);
        System.out.println("  标题: " + values[2]);

        // 10. 预编译并复用（性能推荐）
        System.out.println("\n10. 预编译 JSONPath");
        JSONPath compiledPath = JSONPath.compile("$.store.book[*].price");
        List<Double> prices = compiledPath.extract(json, List.class);
        System.out.println("  所有价格: " + prices);
        double total = prices.stream().mapToDouble(Double::doubleValue).sum();
        System.out.println("  总价: $" + total);

        // 11. 类型化多路径提取
        System.out.println("\n11. 类型化多路径提取");
        JSONPath.TypedMultiPath multi = JSONPath.typedMulti()
            .path("$.store.bicycle.color", String.class)
            .path("$.store.bicycle.price", Double.class)
            .path("$.expensive", Integer.class)
            .build();
        Object[] multiValues = multi.extract(json);
        System.out.println("  自行车颜色: " + multiValues[0]);
        System.out.println("  自行车价格: " + multiValues[1]);
        System.out.println("  阈值: " + multiValues[2]);

        // 12. 使用已解析对象
        System.out.println("\n12. 使用已解析对象");
        JSONObject root = JSON.parseObject(json);
        String bikeColor = root.eval("$.store.bicycle.color", String.class);
        System.out.println("  自行车颜色: " + bikeColor);
    }
}
