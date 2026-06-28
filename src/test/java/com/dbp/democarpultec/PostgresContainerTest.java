package com.dbp.democarpultec;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PostgresContainerTest {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("carpultec_test")
            .withUsername("test")
            .withPassword("test");
    private static final boolean POSTGRES_STARTED;

    static {
        boolean started = false;
        try {
            POSTGRES.start();
            started = true;
        } catch (IllegalStateException exception) {
            started = false;
        }
        POSTGRES_STARTED = started;
    }

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        if (!POSTGRES_STARTED) {
            return;
        }

        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }
}
