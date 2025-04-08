package com.monikit.metric;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("QueryMetricUtils 테스트")
class QueryMetricUtilsTest {

    @ParameterizedTest
    @DisplayName("SQL 쿼리를 카테고리화해야 한다")
    @CsvSource({
        "'SELECT * FROM users', SELECT_users",
        "'INSERT INTO orders VALUES (1, ''item'')', INSERT_orders",   // ✅ 쉼표 포함 시 따옴표로 감싸기
        "'UPDATE products SET name = ''new'' WHERE id = 1', UPDATE_products",
        "'DELETE FROM customers WHERE id = 10', DELETE_customers",
        "'INSERT INTO items (id, name) VALUES (1, ''test'')', INSERT_items",
        "'ALTER TABLE items ADD COLUMN price INT', OTHER",
        "'DROP TABLE temp', OTHER"
    })
    void shouldCategorizeSqlQueryCorrectly(String input, String expected) {
        // When
        String result = QueryMetricUtils.categorizeQuery(input);

        // Then
        assertEquals(expected, result);
    }

}
