package com.topsell.backend.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        // Intentar usar variables PG* de Railway primero
        String pgHost = System.getenv("PGHOST");
        String pgPort = System.getenv("PGPORT");
        String pgDatabase = System.getenv("PGDATABASE");
        String pgUser = System.getenv("PGUSER");
        String pgPassword = System.getenv("PGPASSWORD");
        
        if (pgHost != null && pgPort != null && pgDatabase != null && pgUser != null && pgPassword != null) {
            String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", pgHost, pgPort, pgDatabase);
            
            System.out.println("🔵 Conectando a PostgreSQL: " + jdbcUrl.replace(pgPassword, "****"));
            
            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(pgUser)
                    .password(pgPassword)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }
        
        // Fallback: usar DATABASE_URL si está disponible
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && databaseUrl.startsWith("postgres://")) {
            databaseUrl = databaseUrl.replace("postgres://", "jdbc:postgresql://");
            
            String[] parts = databaseUrl.split("@");
            if (parts.length == 2) {
                String credentials = parts[0].replace("jdbc:postgresql://", "");
                String[] userPass = credentials.split(":");
                String jdbcUrl = "jdbc:postgresql://" + parts[1];
                
                System.out.println("🟡 Conectando usando DATABASE_URL");
                
                return DataSourceBuilder.create()
                        .url(jdbcUrl)
                        .username(userPass[0])
                        .password(userPass[1])
                        .driverClassName("org.postgresql.Driver")
                        .build();
            }
        }
        
        // Fallback final: configuración local de application.properties
        System.out.println("🟢 Usando configuración local de application.properties");
        return DataSourceBuilder.create().build();
    }
}
