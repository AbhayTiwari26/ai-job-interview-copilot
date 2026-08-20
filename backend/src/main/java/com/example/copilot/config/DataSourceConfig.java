package com.example.copilot.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Robust DataSource configuration that handles both Railway's auto-injected
 * PG* variables and its DATABASE_URL format (postgresql://user:pass@host:port/db).
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Value("${PGHOST:}")
    private String pgHost;

    @Value("${PGPORT:5432}")
    private String pgPort;

    @Value("${PGDATABASE:}")
    private String pgDatabase;

    @Value("${PGUSER:}")
    private String pgUser;

    @Value("${PGPASSWORD:}")
    private String pgPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setConnectionTimeout(20_000);
        ds.setMaximumPoolSize(5);

        // ── Option A: individual PG* vars (Railway auto-injects these when PostgreSQL is linked) ──
        if (!pgHost.isBlank() && !pgDatabase.isBlank()) {
            String jdbcUrl = "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDatabase;
            log.info("DataSource: using PG* variables → {}", jdbcUrl);
            ds.setJdbcUrl(jdbcUrl);
            ds.setUsername(pgUser);
            ds.setPassword(pgPassword);
            return ds;
        }

        // ── Option B: DATABASE_URL (postgresql://user:pass@host:port/db) ──
        if (!databaseUrl.isBlank()) {
            try {
                String cleanUrl = databaseUrl.startsWith("postgresql://")
                        ? databaseUrl.replace("postgresql://", "http://")
                        : databaseUrl.replace("postgres://", "http://");
                URI uri = new URI(cleanUrl);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String db = uri.getPath().replaceFirst("^/", "");
                String userInfo = uri.getUserInfo();
                String user = userInfo != null ? userInfo.split(":")[0] : "";
                String pass = userInfo != null && userInfo.contains(":")
                        ? userInfo.substring(userInfo.indexOf(':') + 1) : "";

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + db;
                log.info("DataSource: using DATABASE_URL → {}", jdbcUrl);
                ds.setJdbcUrl(jdbcUrl);
                ds.setUsername(user);
                ds.setPassword(pass);
                return ds;
            } catch (Exception e) {
                log.error("Failed to parse DATABASE_URL: {}", e.getMessage());
            }
        }

        throw new IllegalStateException(
                "No valid database configuration found. " +
                "Set PGHOST/PGPORT/PGDATABASE/PGUSER/PGPASSWORD or DATABASE_URL in Railway Variables."
        );
    }
}
