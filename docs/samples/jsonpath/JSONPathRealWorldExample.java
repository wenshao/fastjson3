package com.alibaba.fastjson3.samples.jsonpath;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.JSONPath;

/**
 * JSONPath 实际应用示例：日志分析、API 响应处理
 */
public class JSONPathRealWorldExample {

    public static void main(String[] args) {
        System.out.println("=== JSONPath 实际应用示例 ===\n");

        // 场景1：分析 API 日志
        analyzeApiLogs();

        // 场景2：提取配置文件
        extractConfig();

        // 场景3：批量数据提取
        batchExtract();

        // 场景4：条件查询
        conditionalQuery();
    }

    /**
     * 场景1：分析 API 日志
     */
    private static void analyzeApiLogs() {
        System.out.println("场景1：分析 API 日志");

        String logJson = """
            {
                "timestamp": "2026-03-17T10:30:00",
                "level": "INFO",
                "events": [
                    {"type": "request", "method": "GET", "path": "/api/users", "status": 200, "duration": 45},
                    {"type": "request", "method": "POST", "path": "/api/users", "status": 201, "duration": 123},
                    {"type": "error", "message": "Connection timeout", "service": "database"},
                    {"type": "request", "method": "GET", "path": "/api/posts", "status": 200, "duration": 67}
                ]
            }
            """;

        JSONObject log = JSON.parseObject(logJson);

        // 提取所有请求方法
        JSONPath methodsPath = JSONPath.compile("$.events[?(@.type == 'request')].method");
        JSONArray methods = methodsPath.eval(log, JSONArray.class);
        System.out.println("  请求方法: " + methods);

        // 提取所有错误事件
        JSONPath errorPath = JSONPath.compile("$.events[?(@.type == 'error')]");
        JSONArray errors = errorPath.eval(log, JSONArray.class);
        System.out.println("  错误事件: " + errors.size() + " 个");

        // 计算平均响应时间
        JSONPath durationPath = JSONPath.compile("$.events[?(@.type == 'request')].duration");
        JSONArray durations = durationPath.eval(log, JSONArray.class);
        double avgDuration = durations.stream()
            .mapToDouble(o -> ((Number) o).doubleValue())
            .average()
            .orElse(0);
        System.out.println("  平均响应时间: " + avgDuration + "ms");

        // 慢请求 (>100ms)
        JSONPath slowPath = JSONPath.compile("$.events[?(@.type == 'request' && @.duration > 100)]");
        JSONArray slowRequests = slowPath.eval(log, JSONArray.class);
        System.out.println("  慢请求: " + slowRequests.size() + " 个");
        System.out.println();
    }

    /**
     * 场景2：提取配置文件
     */
    private static void extractConfig() {
        System.out.println("场景2：提取配置文件");

        String configJson = """
            {
                "application": {
                    "name": "myapp",
                    "version": "1.0.0",
                    "server": {
                        "host": "localhost",
                        "port": 8080,
                        "ssl": {"enabled": true, "port": 8443}
                    },
                    "database": {
                        "host": "db.example.com",
                        "port": 5432,
                        "name": "mydb"
                    }
                }
            }
            """;

        JSONObject config = JSON.parseObject(configJson);

        // 提取应用名称
        String appName = config.eval("$.application.name", String.class);
        System.out.println("  应用名: " + appName);

        // 提取服务器端口
        Integer serverPort = config.eval("$.application.server.port", Integer.class);
        System.out.println("  服务器端口: " + serverPort);

        // 提取数据库连接信息
        String dbHost = config.eval("$.application.database.host", String.class);
        Integer dbPort = config.eval("$.application.database.port", Integer.class);
        System.out.println("  数据库: " + dbHost + ":" + dbPort);

        // 检查 SSL 是否启用
        Boolean sslEnabled = config.eval("$.application.server.ssl.enabled", Boolean.class);
        System.out.println("  SSL: " + sslEnabled);

        // 多路径提取
        JSONPath.TypedMultiPath multi = JSONPath.typedMulti()
            .path("$.application.server.host", String.class)
            .path("$.application.server.port", Integer.class)
            .path("$.application.database.host", String.class)
            .build();
        Object[] values = multi.extract(configJson);
        System.out.println("  服务器: " + values[0] + ":" + values[1]);
        System.out.println("  数据库: " + values[2]);
        System.out.println();
    }

    /**
     * 场景3：批量数据提取
     */
    private static void batchExtract() {
        System.out.println("场景3：批量数据提取");

        String dataJson = """
            {
                "users": [
                    {"id": 1, "name": "张三", "department": "研发", "salary": 15000},
                    {"id": 2, "name": "李四", "department": "销售", "salary": 12000},
                    {"id": 3, "name": "王五", "department": "研发", "salary": 18000},
                    {"id": 4, "name": "赵六", "department": "销售", "salary": 11000}
                ]
            }
            """;

        // 提取所有研发部人员姓名
        JSONPath devNamesPath = JSONPath.compile("$.users[?(@.department == '研发')].name");
        List<String> devNames = devNamesPath.extract(dataJson, List.class);
        System.out.println("  研发人员: " + devNames);

        // 提取所有薪水
        JSONPath salariesPath = JSONPath.compile("$.users[*].salary");
        JSONArray salaries = salariesPath.eval(dataJson, JSONArray.class);
        int totalSalary = salaries.stream()
            .mapToInt(o -> ((Number) o).intValue())
            .sum();
        System.out.println("  薪水总和: " + totalSalary);

        // 按部门分组统计
        JSONPath deptPath = JSONPath.compile("$.users[*].department");
        List<String> departments = deptPath.extract(dataJson, List.class);
        System.out.println("  部门: " + departments.stream().distinct().toList());

        // 提取高薪员工 (>15000)
        JSONPath highEarnersPath = JSONPath.compile("$.users[?(@.salary > 15000)]");
        JSONArray highEarners = highEarnersPath.eval(dataJson, JSONArray.class);
        System.out.println("  高薪员工: " + highEarners.size() + " 人");
        System.out.println();
    }

    /**
     * 场景4：条件查询
     */
    private static void conditionalQuery() {
        System.out.println("场景4：条件查询");

        String productsJson = """
            {
                "products": [
                    {"id": 1, "name": "笔记本电脑", "category": "电子", "price": 5999, "stock": 50},
                    {"id": 2, "name": "鼠标", "category": "电子", "price": 99, "stock": 200},
                    {"id": 3, "name": "办公椅", "category": "家具", "price": 899, "stock": 30},
                    {"id": 4, "name": "键盘", "category": "电子", "price": 299, "stock": 0}
                ]
            }
            """;

        JSONObject data = JSON.parseObject(productsJson);

        // 库存 > 100 的商品
        JSONPath inStockPath = JSONPath.compile("$.products[?(@.stock > 100)]");
        JSONArray inStock = inStockPath.eval(data, JSONArray.class);
        System.out.println("  库存>100: " + inStock.size() + " 种商品");

        // 电子类且价格 < 1000
        JSONPath cheapElectronicsPath = JSONPath.compile("$.products[?(@.category == '电子' && @.price < 1000)]");
        JSONArray cheapElectronics = cheapElectronicsPath.eval(data, JSONArray.class);
        System.out.println("  便宜电子产品: " + cheapElectronics.size() + " 种");
        for (int i = 0; i < cheapElectronics.size(); i++) {
            JSONObject p = cheapElectronics.getJSONObject(i);
            System.out.println("    - " + p.get("name") + ": ¥" + p.get("price"));
        }

        // 缺货商品
        JSONPath outOfStockPath = JSONPath.compile("$.products[?(@.stock == 0)]");
        JSONArray outOfStock = outOfStockPath.eval(data, JSONArray.class);
        System.out.println("  缺货: " + outOfStock.size() + " 种商品");

        // 计算电子产品总价
        JSONPath electronicsPricesPath = JSONPath.compile("$.products[?(@.category == '电子')].price");
        JSONArray electronicsPrices = electronicsPricesPath.eval(data, JSONArray.class);
        double total = electronicsPrices.stream()
            .mapToDouble(o -> ((Number) o).doubleValue())
            .sum();
        System.out.println("  电子产品总价: ¥" + total);
    }
}
