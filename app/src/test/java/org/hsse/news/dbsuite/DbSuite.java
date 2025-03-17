package org.hsse.news.dbsuite;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

@ContextConfiguration(initializers = DbSuite.Initializer.class)
public class DbSuite {
  private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

  static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> { // NOPMD

    @Override
    public void initialize(final @NotNull ConfigurableApplicationContext context) {
      Startables.deepStart(POSTGRES).join();

      TestPropertyValues.of(
          "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
          "spring.datasource.username=" + POSTGRES.getUsername(),
          "spring.datasource.password=" + POSTGRES.getPassword()
      ).applyTo(context);
    }
  }
}