package ua.hudyma.config;

import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class HibernateConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            DataSource dataSource) {

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put(
                "hibernate.transaction.jta.platform",
                org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform.INSTANCE
        );

        return builder
                .dataSource(dataSource)
                .packages("ua.hudyma.domain")
                .properties(jpaProperties)
                .build();
    }
}

