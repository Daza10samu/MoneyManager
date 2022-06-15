package com.dazai.moneymanager.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.datasource.DriverManagerDataSource

@Configuration
@Profile("dev", "test", "prod", "debug")
class TestDatabase {
    @Value("#{systemEnvironment['DB_HOST']}")
    private val dbHost: String? = null

    @Value("#{systemEnvironment['DB_NAME']}")
    private val dbName: String? = null

    @Value("#{systemEnvironment['DB_USER_NAME']}")
    private val userName: String? = null

    @Value("#{systemEnvironment['DB_PASSWORD']}")
    private val password: String? = null

    @Bean
    fun getDataSource(): DriverManagerDataSource {
            val bds = DriverManagerDataSource()
            bds.setDriverClassName("org.postgresql.Driver")
            bds.url = "jdbc:postgresql://$dbHost/$dbName"
            bds.username = userName
            bds.password = password
            bds.schema = null
            return bds
        }
}