package com.monikit.starter;

public class QueryMetricUtils {

    private QueryMetricUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated.");
    }

    /**
     * SQL 쿼리를 카테고리화하여 `SELECT_테이블명`, `INSERT_테이블명` 등의 형식으로 변환
     */
    public static String categorizeQuery(String sql) {
        sql = sql.toLowerCase().trim();

        if (sql.startsWith("select")) return "SELECT_" + extractTableName(sql);
        if (sql.startsWith("insert")) return "INSERT_" + extractTableName(sql);
        if (sql.startsWith("update")) return "UPDATE_" + extractTableName(sql);
        if (sql.startsWith("delete")) return "DELETE_" + extractTableName(sql);
        return "OTHER";
    }

    /**
     * SQL 쿼리에서 테이블명을 추출
     */
    private static String extractTableName(String sql) {
        String[] tokens = sql.split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("from") || tokens[i].equals("update")) {
                return cleanTableName(tokens[i + 1]); // `FROM`, `UPDATE` 다음이 테이블명
            }
            if (tokens[i].equals("into") && i + 1 < tokens.length) {
                return cleanTableName(tokens[i + 1]); // `INTO` 다음이 테이블명
            }
        }
        return "UNKNOWN";
    }

    /**
     * 테이블명에서 특수문자(콤마, 괄호 등)를 제거하는 메서드
     */
    private static String cleanTableName(String tableName) {
        return tableName.replaceAll("[^a-zA-Z0-9_]", ""); // 알파벳, 숫자, 밑줄(_)만 남김
    }


}