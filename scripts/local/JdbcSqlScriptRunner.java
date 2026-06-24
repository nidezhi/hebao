import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地 SQL 脚本执行器。
 *
 * <p>该工具只用于本地/开发环境执行手工初始化脚本，避免把数据库密码写入命令行历史。
 * 它会优先读取环境变量，其次读取 Git 忽略的 {@code config/application-secrets.yaml}，
 * 最后回退到项目 local/dev 默认数据库配置。</p>
 *
 * @author dz
 * @date 2026-06-24
 */
public class JdbcSqlScriptRunner {
    private static final Pattern DATASOURCE_BLOCK = Pattern.compile("(?ms)^  datasource:\\R(.*?)(?=^  \\S|\\z)");
    private static final Pattern YAML_VALUE = Pattern.compile("(?m)^\\s{4}%s:\\s*(.*)\\s*$");

    /**
     * 执行指定 SQL 文件，并输出脚本末尾的校验结果。
     *
     * @param args 第一个参数为本地 secrets 文件，第二个参数为 SQL 文件
     * @throws Exception 当数据库连接或脚本执行失败时抛出
     * @author dz
     * @date 2026-06-24
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: java JdbcSqlScriptRunner <secrets-yaml> <sql-file>");
        }
        Path secretsPath = Path.of(args[0]);
        Path sqlPath = Path.of(args[1]);
        String secrets = Files.exists(secretsPath) ? Files.readString(secretsPath) : "";
        String datasourceBlock = datasourceBlock(secrets);
        String url = firstNonBlank(System.getenv("DB_URL"), yamlValue(datasourceBlock, "url"),
                "jdbc:mysql://localhost:3306/dz_database?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai");
        String username = firstNonBlank(System.getenv("DB_USERNAME"), yamlValue(datasourceBlock, "username"), "root");
        String password = firstNonBlank(System.getenv("DB_PASSWORD"), yamlValue(datasourceBlock, "password"), "");
        String script = Files.readString(sqlPath);
        String jdbcUrl = withParameter(url, "allowMultiQueries", "true");

        try (var connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement()) {
            try (ResultSet database = statement.executeQuery("SELECT DATABASE()")) {
                database.next();
                System.out.println("Target database: " + database.getString(1));
            }
            boolean hasResultSet = statement.execute(script);
            int resultIndex = 0;
            while (true) {
                if (hasResultSet) {
                    resultIndex++;
                    try (ResultSet resultSet = statement.getResultSet()) {
                        printResultSet(resultIndex, resultSet);
                    }
                } else if (statement.getUpdateCount() == -1) {
                    break;
                }
                hasResultSet = statement.getMoreResults();
            }
        }
    }

    /**
     * 提取 spring.datasource 配置块。
     *
     * @param yaml YAML 文本
     * @return datasource 配置块，未找到时返回空串
     * @author dz
     * @date 2026-06-24
     */
    private static String datasourceBlock(String yaml) {
        Matcher matcher = DATASOURCE_BLOCK.matcher(yaml);
        return matcher.find() ? matcher.group(1) : "";
    }

    /**
     * 从简化 YAML 块读取单个键的值。
     *
     * @param block YAML 配置块
     * @param key 键名
     * @return 对应值，未找到时返回空串
     * @author dz
     * @date 2026-06-24
     */
    private static String yamlValue(String block, String key) {
        Matcher matcher = Pattern.compile(String.format(YAML_VALUE.pattern(), Pattern.quote(key))).matcher(block);
        return matcher.find() ? trimQuotes(matcher.group(1).trim()) : "";
    }

    /**
     * 返回第一个非空字符串。
     *
     * @param values 候选值
     * @return 第一个非空值
     * @author dz
     * @date 2026-06-24
     */
    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    /**
     * 去除 YAML 简单引号包裹。
     *
     * @param value 原始值
     * @return 去除包裹后的值
     * @author dz
     * @date 2026-06-24
     */
    private static String trimQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * 为 JDBC URL 追加连接参数。
     *
     * @param url 原始 JDBC URL
     * @param key 参数名
     * @param value 参数值
     * @return 带参数的 JDBC URL
     * @author dz
     * @date 2026-06-24
     */
    private static String withParameter(String url, String key, String value) {
        if (url.contains(key + "=")) {
            return url;
        }
        return url + (url.contains("?") ? "&" : "?") + key + "=" + value;
    }

    /**
     * 输出查询结果，主要用于脚本末尾的行数校验。
     *
     * @param resultIndex 结果集序号
     * @param resultSet 查询结果
     * @throws Exception 当读取结果失败时抛出
     * @author dz
     * @date 2026-06-24
     */
    private static void printResultSet(int resultIndex, ResultSet resultSet) throws Exception {
        int columnCount = resultSet.getMetaData().getColumnCount();
        System.out.println("Result set #" + resultIndex);
        while (resultSet.next()) {
            StringBuilder line = new StringBuilder();
            for (int index = 1; index <= columnCount; index++) {
                if (index > 1) {
                    line.append(" | ");
                }
                line.append(resultSet.getMetaData().getColumnLabel(index)).append('=').append(resultSet.getString(index));
            }
            System.out.println(line);
        }
    }
}
