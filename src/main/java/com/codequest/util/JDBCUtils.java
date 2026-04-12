package com.codequest.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * JDBC 工具类，基于 HikariCP 统一管理连接池。
 * Author: 张雨泽
 */
public final class JDBCUtils {

    private static volatile HikariDataSource DATA_SOURCE;

    private JDBCUtils() {
    }

    public static synchronized void initDataSource() {
        if (DATA_SOURCE != null && !DATA_SOURCE.isClosed()) {
            return;
        }

        try (InputStream inputStream = JDBCUtils.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Cannot find db.properties in classpath.");
            }

            Properties properties = new Properties();
            properties.load(inputStream);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.username"));
            config.setPassword(properties.getProperty("db.password"));
            config.setDriverClassName(properties.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));

            config.setPoolName(properties.getProperty("hikari.poolName", "CodeQuestHikariPool"));
            config.setMinimumIdle(Integer.parseInt(properties.getProperty("hikari.minimumIdle", "5")));
            config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("hikari.maximumPoolSize", "20")));
            config.setIdleTimeout(Long.parseLong(properties.getProperty("hikari.idleTimeout", "300000")));
            config.setConnectionTimeout(Long.parseLong(properties.getProperty("hikari.connectionTimeout", "30000")));
            config.setMaxLifetime(Long.parseLong(properties.getProperty("hikari.maxLifetime", "1800000")));

            DATA_SOURCE = new HikariDataSource(config);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load database configuration.", ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        initDataSource();
        return DATA_SOURCE.getConnection();
    }

    public static synchronized void closeDataSource() {
        if (DATA_SOURCE != null) {
            if (!DATA_SOURCE.isClosed()) {
                DATA_SOURCE.close();
            }
            DATA_SOURCE = null;
        }
    }

    public static boolean isInitialized() {
        return DATA_SOURCE != null && !DATA_SOURCE.isClosed();
    }
}