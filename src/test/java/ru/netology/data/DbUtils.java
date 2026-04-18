package ru.netology.data;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbUtils {
    private static final QueryRunner RUNNER = new QueryRunner();
    private static final String DB_URL = System.getProperty("db.url", System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/app"));
    private static final String DB_USER = System.getProperty("db.user", System.getenv().getOrDefault("DB_USER", "app"));
    private static final String DB_PASS = System.getProperty("db.password", System.getenv().getOrDefault("DB_PASS", "pass"));

    private DbUtils() {
    }

    public static String getVerificationCode(String login) {
        String sql = "SELECT ac.code FROM auth_codes ac " +
                "JOIN users u ON ac.user_id = u.id " +
                "WHERE u.login = ? " +
                "ORDER BY ac.created DESC LIMIT 1";
        try (Connection conn = getConnection()) {
            String code = RUNNER.query(conn, sql, new ScalarHandler<>(), login);
            if (code == null) {
                throw new IllegalStateException("Verification code was not found for login: " + login);
            }
            return code;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getUserStatus(String login) {
        String sql = "SELECT status FROM users WHERE login = ?";
        try (Connection conn = getConnection()) {
            return RUNNER.query(conn, sql, new ScalarHandler<>(), login);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resetDemoState() {
        try (Connection conn = getConnection()) {
            RUNNER.update(conn, "DELETE FROM auth_codes");
            RUNNER.update(conn, "DELETE FROM card_transactions");
            RUNNER.update(conn, "UPDATE users SET status = 'active' WHERE login = 'vasya'");
            RUNNER.update(conn, "UPDATE users SET status = 'blocked' WHERE login = 'petya'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}
