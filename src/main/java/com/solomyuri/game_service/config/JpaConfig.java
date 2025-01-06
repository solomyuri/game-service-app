package com.solomyuri.game_service.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
		basePackages = "com.solomyuri.game_service.repository",
		entityManagerFactoryRef = "gameServiceEntityManagerFactory",
		transactionManagerRef = "gameServiceTransactionManager")
@EntityScan("com.solomyuri.game_service.model.entity")
public class JpaConfig {

	@Bean(name = "gameServiceDataSourceProperies")
	@ConfigurationProperties("spring.datasource")
	DataSourceProperties gameServiceDataSourceProperties() {

		return new DataSourceProperties();
	}

	@Bean(name = "gameServiceDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.hikari")
	DataSource gameServiceDataSource(DataSourceProperties gameServiceDataSourceProperies) {

		return gameServiceDataSourceProperies.initializeDataSourceBuilder().type(HikariDataSource.class).build();
	}

	@Bean(name = "gameServiceEntityManagerFactory")
	LocalContainerEntityManagerFactoryBean gameServiceEntityManagerFactory(EntityManagerFactoryBuilder builder,
			DataSource gameServiceDataSource) {

		return builder.dataSource(gameServiceDataSource)
				.packages("com.solomyuri.game_service.model.entity")
				.persistenceUnit("gameServiceDataSource")
				.build();
	}

	@Bean(name = "gameServiceTransactionManager")
	PlatformTransactionManager platformTransactionManager(EntityManagerFactory gameServiceEntityManagerFactory) {

		return new JpaTransactionManager(gameServiceEntityManagerFactory);
	}

	@Bean
	JdbcTemplate gameServiceJdbcTemplate(DataSource gameServiceDataSource) {

		return new JdbcTemplate(gameServiceDataSource);
	}

}
