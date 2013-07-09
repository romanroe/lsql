package com.weiglewilczek.lsql;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Function;
import org.h2.jdbcx.JdbcDataSource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class LSqlTest {

    private LSql lSql;

    private JdbcDataSource dataSource = new JdbcDataSource();

    public LSqlTest() {
        dataSource.setURL("jdbc:h2:mem:testdb;MODE=PostgreSQL");
    }

    @BeforeMethod
    public void beforeTest() throws SQLException {
        final Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        lSql = new LSql(ConnectionFactories.fromInstance(connection));
    }

    @AfterMethod
    public void afterTest() throws SQLException {
        lSql.execute("drop table if exists table1");
        lSql.getConnection().commit();
    }

    @Test
    public void getConnectionFromConnectionFactory() throws SQLException {
        assertNotNull(lSql.getConnection());
    }

    @Test
    public void testSelectFieldAccess() throws SQLException {
        lSql.execute("create table table1 (name char(50), age int);" +
                "insert into table1 (name, age) values ('cus1', 20);" +
                "insert into table1 (name, age) values ('cus2', 30)");

<<<<<<< HEAD:src/test/java/com/weiglewilczek/lsql/LSqlTest.java
        List<Integer> ages = lSql.select().from("table1").map(new Function<Row, Integer>() {
            @Override
            public Integer apply(@Nullable Row row) {
                return Integer.parseInt(row.get("AGE").toString());
            }
        });
=======
        List<Integer> ages = lSql.executeQuery(
                "select * from table1",
                new Function<Row, Integer>() {
                    @Override
                    public Integer apply(@Nullable Row input) {
                        return Integer.parseInt(input.get("age").toString());
                    }
                });

>>>>>>> origin/master:src/test/java/de/romanroe/lsql/LSqlTest.java
        int sum = 0;
        for (int age : ages) {
            sum += age;
        }
        assertEquals(sum, 50);
    }

    @Test
    public void testSelectFullAccessToMapEntrySet() throws SQLException {
        lSql.execute("create table table1 (name char (50), age int);" +
                "insert into table1 (name, age) values ('cus1', '20');");

        // All key->values
        final List<String> entries = Lists.newArrayList();

<<<<<<< HEAD:src/test/java/com/weiglewilczek/lsql/LSqlTest.java
        lSql.select().from("table1").map(new Function<Row, Integer>() {
            @Override
            public Integer apply(@Nullable Row input) {
                for (Map.Entry<String, Object> entry : input.entrySet()) {
                    entries.add(entry.getKey() + "->" + entry.getValue());
                }
                return null;
            }
        });
        assertTrue(entries.contains("NAME->cus1"));
        assertTrue(entries.contains("AGE->20"));
=======
        lSql.executeQuery(
                "select * from table1",
                new Function<Row, Integer>() {
                    @Override
                    public Integer apply(@Nullable Row input) {
                        for (Map.Entry<String, Object> entry : input.entrySet()) {
                            entries.add(entry.getKey() + "->" + entry.getValue());
                        }
                        return null;
                    }
                });

        assertTrue(entries.contains("name->cus1"));
        assertTrue(entries.contains("age->20"));
    }

    @Test
    public void testNameConversions() {
        lSql.execute("create table table1 (test_name1 char (50), TEST_NAME2 char (50));" +
                "insert into table1 (test_name1, TEST_NAME2) values ('name1', 'name2');");

        final boolean[] assertCallbackCalled = {false};
        lSql.executeQuery(
                "select * from table1",
                new Function<Row, Integer>() {
                    public Integer apply(@Nullable Row input) {
                        assertCallbackCalled[0] = true;
                        assertEquals(input.get("testName1"), "name1");
                        assertEquals(input.get("testName2"), "name2");
                        return null;
                    }
                });
        assertTrue(assertCallbackCalled[0]);

    }

    @Test
    public void testInsertAndKeyRetrieval() {
        lSql.execute("create table table1 (id serial, test_name1 char (50), age int)");

        Object newId = lSql.executeInsert("table1", L.createMap(
                "testName1", "a name",
                "age", 2));
        assertNotNull(newId);

        Map<String, Object> query = lSql.executeQueryAndGetFirstRow("select * from table1 where id = " + newId);
        assertEquals(query.get("testName1"), "a name");
        assertEquals(query.get("age"), 2);
    }

    @Test
    public void testExecuteQueryAndGetFirstRow() {
        lSql.execute("create table table1 (id serial, number int)");
        lSql.executeInsert("table1", L.createMap("number", 1));
        lSql.executeInsert("table1", L.createMap("number", 2));
        lSql.executeInsert("table1", L.createMap("number", 3));

        Map<String, Object> map = lSql.executeQueryAndGetFirstRow("select sum(number) as X from table1");
        System.out.println(map);
        assertEquals(map.get("x"), 6L);
>>>>>>> origin/master:src/test/java/de/romanroe/lsql/LSqlTest.java
    }

}
