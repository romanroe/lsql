package com.w11k.lsql;

import com.google.common.collect.Lists;
import com.w11k.lsql.converter.Converter;
import com.w11k.lsql.dialects.H2Dialect;
import com.w11k.lsql.dialects.PostgresDialect;
import com.w11k.lsql.jdbc.ConnectionProviders;
import org.apache.commons.dbcp.BasicDataSource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractLSqlTest {

    private Connection connection;

    private static List<Consumer<TestConfig>> CONFIG_HOOKS = Lists.newLinkedList();

    public static class TestConfig extends Config {

        static String driverClassName = null;

        public TestConfig() {
            if (driverClassName.equals("org.h2.Driver")) {
                this.setDialect(new H2Dialect());
            } else if (driverClassName.equals("org.postgresql.Driver")) {
                this.setDialect(new PostgresDialect());
            }

            CONFIG_HOOKS.forEach(hook -> hook.accept(this));
        }

        @Override
        public void setConverter(String tableName, String columnName, Class<?> classForConverterLookup) {
            super.setConverter(tableName, columnName, classForConverterLookup);
        }

        @Override
        public void setConverter(String tableName, String columnName, Converter converter) {
            super.setConverter(tableName, columnName, converter);
        }

        @Override
        public void setUseColumnTypeForConverterLookupInQueries(boolean useColumnTypeForConverterLookupInQueries) {
            super.setUseColumnTypeForConverterLookupInQueries(useColumnTypeForConverterLookupInQueries);
        }

    }

    protected LSql lSql;

    protected void addConfigHook(Consumer<TestConfig> hook) {
        CONFIG_HOOKS.add(hook);
        this.createLSqlInstance();
    }

    @Parameters({
            TestParameter.jdbcDriverClassName,
            TestParameter.jdbcUrl,
            TestParameter.jdbcUsername,
            TestParameter.jdbcPassword
    })
    @BeforeMethod()
    public final void beforeMethod(@Optional String driverClassName,
                                   @Optional String url,
                                   @Optional String username,
                                   @Optional String password) {

        this.reset();

        driverClassName = driverClassName != null ? driverClassName : "org.h2.Driver";
        url = url != null ? url : "jdbc:h2:mem:testdb;mode=postgresql";
        username = username != null ? username : "";
        password = password != null ? password : "";

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driverClassName);
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);

        ds.setDefaultAutoCommit(false);
        TestUtils.clear(ds);
        try {
            this.connection = ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        TestConfig.driverClassName = driverClassName;
        this.createLSqlInstance();
        this.beforeMethodHook();
    }

    @AfterMethod
    public final void afterMethod() throws Exception {
        lSql.getConnectionProvider().call().close();
    }

    protected void reset() {
        CONFIG_HOOKS = Lists.newLinkedList();
    }

    protected void beforeMethodHook() {
    }

    protected void createTable(String sql) {
        lSql.executeRawSql(sql);
    }

    protected void createLSqlInstance() {
        this.lSql = new LSql(TestConfig.class, ConnectionProviders.fromInstance(this.connection));
    }

}
