package com.velmart.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.io.File;
import java.net.URI;
import java.sql.Connection;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    private final Environment env;

    @Autowired
    public DataSourceConfig(Environment env) {
        this.env = env;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbType = env.getProperty("DB_TYPE", "auto");
        if ("h2".equalsIgnoreCase(dbType)) {
            log.info("[DATABASE CONFIG] DB_TYPE=h2 requested. Using embedded H2 database.");
            return createH2DataSource();
        }

        // 1. Resolve raw URL if provided
        String rawUrl = getFirstNonBlank(
                env.getProperty("SPRING_DATASOURCE_URL"),
                env.getProperty("spring.datasource.url"),
                env.getProperty("DATABASE_URL"),
                env.getProperty("MYSQL_URL")
        );

        String username = getFirstNonBlank(
                env.getProperty("SPRING_DATASOURCE_USERNAME"),
                env.getProperty("spring.datasource.username"),
                env.getProperty("MYSQL_USER"),
                env.getProperty("MYSQLUSER"),
                "root"
        );

        String password = getFirstNonBlank(
                env.getProperty("SPRING_DATASOURCE_PASSWORD"),
                env.getProperty("spring.datasource.password"),
                env.getProperty("MYSQL_PASSWORD"),
                env.getProperty("MYSQLPASSWORD"),
                ""
        );

        // 2. Direct H2 URL support (used in test suites or explicit H2 config)
        if (rawUrl != null && rawUrl.startsWith("jdbc:h2:")) {
            log.info("[DATABASE CONFIG] Connecting to configured H2 database: {}", rawUrl);
            HikariConfig h2Config = new HikariConfig();
            h2Config.setJdbcUrl(rawUrl);
            h2Config.setDriverClassName("org.h2.Driver");
            h2Config.setUsername(username != null ? username : "sa");
            h2Config.setPassword(password != null ? password : "");
            h2Config.setPoolName("Velmart-H2-Configured-Pool");
            return new HikariDataSource(h2Config);
        }

        // 3. Resolve MySQL Host, Port, and Database
        String host = getFirstNonBlank(
                env.getProperty("MYSQL_HOST"),
                env.getProperty("MYSQLHOST"),
                "localhost"
        );

        int port = 3306;
        String portStr = getFirstNonBlank(env.getProperty("MYSQL_PORT"), env.getProperty("MYSQLPORT"));
        if (portStr != null) {
            try {
                port = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        String database = getFirstNonBlank(
                env.getProperty("MYSQL_DATABASE"),
                env.getProperty("MYSQLDATABASE"),
                "velmart_db"
        );

        // 4. Parse URI format (e.g. mysql://user:pass@host:port/dbname from cloud platforms)
        if (rawUrl != null && rawUrl.startsWith("mysql://")) {
            try {
                URI uri = new URI(rawUrl);
                if (uri.getHost() != null) host = uri.getHost();
                if (uri.getPort() != -1) port = uri.getPort();
                if (uri.getPath() != null && uri.getPath().length() > 1) {
                    database = uri.getPath().substring(1);
                }
                if (uri.getUserInfo() != null) {
                    String[] userParts = uri.getUserInfo().split(":", 2);
                    username = userParts[0];
                    if (userParts.length > 1) password = userParts[1];
                }
            } catch (Exception e) {
                log.warn("[DATABASE CONFIG] Could not parse raw URL URI format: {}", e.getMessage());
            }
        }

        String jdbcUrl;
        if (rawUrl != null && rawUrl.startsWith("jdbc:mysql://")) {
            jdbcUrl = rawUrl;
        } else {
            jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    host, port, database);
        }

        // 5. Attempt MySQL connection with safe fallback
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setPoolName("Velmart-MySQL-Pool");
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setConnectionTimeout(3000);
            hikariConfig.setInitializationFailTimeout(3000);

            log.info("[DATABASE CONFIG] Attempting connection to MySQL: {}:{}/{}", host, port, database);
            HikariDataSource ds = new HikariDataSource(hikariConfig);

            try (Connection conn = ds.getConnection()) {
                log.info("✅ [DATABASE CONFIG] Successfully connected to MySQL at {}:{}/{}", host, port, database);
                return ds;
            }
        } catch (Throwable t) {
            log.warn("================================================================================");
            log.warn("⚠️  [DATABASE AUTO-FALLBACK] Could not connect to MySQL at {}:{}/{} ({})", host, port, database, t.getMessage());
            log.warn("⚠️  AUTOMATIC FALLBACK: Activating embedded persistent database (H2 in MySQL mode).");
            log.warn("⚠️  VELMART will start up successfully on Render with full storefront & admin access!");
            log.warn("⚠️  To connect your external MySQL database later, set MYSQL_HOST in your Render dashboard.");
            log.warn("================================================================================");
            return createH2DataSource();
        }
    }

    private DataSource createH2DataSource() {
        File dataDir = new File("./data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        String h2Url = "jdbc:h2:file:./data/velmart_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDER=HIGH;DB_CLOSE_DELAY=-1";
        log.info("[DATABASE CONFIG] Initializing embedded H2 datasource at: {}", h2Url);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(h2Url);
        hikariConfig.setDriverClassName("org.h2.Driver");
        hikariConfig.setUsername("sa");
        hikariConfig.setPassword("");
        hikariConfig.setPoolName("Velmart-H2-Fallback-Pool");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);

        return new HikariDataSource(hikariConfig);
    }

    private String getFirstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }
}
