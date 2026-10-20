package com.learnloop.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties,
                                 @Value("${DATABASE_URL:}") String databaseUrl,
                                 @Value("${DB_SSL_MODE:prefer}") String sslMode) {
        HikariConfig config = new HikariConfig();

        // Support Render's standard DATABASE_URL (postgres://user:pass@host:port/dbname)
        if (StringUtils.hasText(databaseUrl) && (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://"))) {
            log.info("Detected Render DATABASE_URL; converting to PostgreSQL JDBC URL");
            try {
                URI uri = new URI(databaseUrl);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                String dbName = (path != null && path.length() > 1) ? path.substring(1) : "learnloop";

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?sslmode=" + sslMode;
                config.setJdbcUrl(jdbcUrl);

                if (uri.getUserInfo() != null) {
                    String[] userParts = uri.getUserInfo().split(":", 2);
                    config.setUsername(userParts[0]);
                    if (userParts.length > 1) {
                        config.setPassword(userParts[1]);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse DATABASE_URL URI; falling back to standard datasource properties", e);
                config.setJdbcUrl(properties.determineUrl());
                config.setUsername(properties.determineUsername());
                config.setPassword(properties.determinePassword());
            }
        } else {
            config.setJdbcUrl(properties.determineUrl());
            config.setUsername(properties.determineUsername());
            config.setPassword(properties.determinePassword());
        }

        config.setDriverClassName(StringUtils.hasText(properties.getDriverClassName()) 
                ? properties.getDriverClassName() 
                : "org.postgresql.Driver");
        config.setMaximumPoolSize(15);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(20000);
        config.setMaxLifetime(1200000);

        return new HikariDataSource(config);
    }
}
