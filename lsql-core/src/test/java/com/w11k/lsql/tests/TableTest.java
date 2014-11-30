package com.w11k.lsql.tests;

import com.google.common.base.Optional;
import com.w11k.lsql.LinkedRow;
import com.w11k.lsql.QueriedRow;
import com.w11k.lsql.Row;
import com.w11k.lsql.Table;
import com.w11k.lsql.exceptions.DatabaseAccessException;
import com.w11k.lsql.exceptions.InsertException;
import com.w11k.lsql.exceptions.UpdateException;
import com.w11k.lsql.validation.AbstractValidationError;
import com.w11k.lsql.validation.KeyError;
import com.w11k.lsql.validation.TypeError;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class TableTest extends AbstractLSqlTest {

    @Test
    public void getById() {
        createTable("CREATE TABLE table1 (id INTEGER PRIMARY KEY, age INT)");
        Table<?> table1 = lSql.table("table1");

        table1.insert(Row.fromKeyVals("id", 1, "age", 1));
        table1.insert(Row.fromKeyVals("id", 2, "age", 2));
        table1.insert(Row.fromKeyVals("id", 3, "age", 3));

        assertEquals(table1.load(1).get().getInt("age"), (Integer) 1);
        assertEquals(table1.load(2).get().getInt("age"), (Integer) 2);
        assertEquals(table1.load(3).get().getInt("age"), (Integer) 3);
    }

    @Test
    public void getByIdReturnAbsentOnWrongId() {
        createTable("CREATE TABLE table1 (id INTEGER PRIMARY KEY, age INT)");
        Table table1 = lSql.table("table1");

        assertFalse(table1.load(999).isPresent());
    }

    @Test
    public void insertRow() throws SQLException {
        createTable("CREATE TABLE table1 (name TEXT)");
        Table table1 = lSql.table("table1");

        Row row = new Row().addKeyVals("name", "cus1");
        table1.insert(row);

        Row insertedRow = lSql.executeRawQuery("SELECT * FROM table1").getFirstRow().get();
        assertEquals(insertedRow.getString("name"), "cus1");
    }

    @Test(expectedExceptions = DatabaseAccessException.class)
    public void insertFailsOnWrongColumnName() throws SQLException {
        createTable("CREATE TABLE table1 (name TEXT)");
        Table table1 = lSql.table("table1");

        Row row = new Row().addKeyVals("nameTYPO", "cus1");
        table1.insert(row);
    }

    @Test
    public void insertShouldReturnGeneratedKey() {
        createTable("CREATE TABLE table1 (id SERIAL PRIMARY KEY, age INT)");
        Table table1 = lSql.table("table1");
        Object newId = table1.insert(new Row().addKeyVals("age", 1)).get();

        Row query = lSql.executeRawQuery("select * from table1 where id = " + newId).getFirstRow()
                .get();
        assertEquals(query.getInt("age"), (Integer) 1);
    }

    @Test
    public void insertShouldPutIdIntoRowObject() {
        createTable("CREATE TABLE table1 (id SERIAL PRIMARY KEY, age INT)");
        Table<?> table1 = lSql.table("table1");
        Row row = new Row().addKeyVals("age", 1);
        Optional<Object> optional = table1.insert(row);
        assertTrue(optional.isPresent());
        assertEquals(optional.get(), row.get("id"));
    }

    @Test(expectedExceptions = InsertException.class)
    public void insertShouldFailOnWrongKeys() {
        createTable("CREATE TABLE table1 (id INTEGER PRIMARY KEY, age INT)");
        Table<?> table1 = lSql.table("table1");
        Row row = new Row().addKeyVals("age", 1, "wrong", "value");
        Optional<Object> optional = table1.insert(row);
        assertTrue(optional.isPresent());
        assertEquals(optional.get(), row.get("id"));
    }

    @Test(expectedExceptions = UpdateException.class)
    public void updateShouldFailWhenIdNotPresent() throws SQLException {
        createTable("CREATE TABLE table1 (id INTEGER PRIMARY KEY, name TEXT)");
        Table<?> table1 = lSql.table("table1");
        Row row = new Row().addKeyVals("name", "Max");
        table1.update(row);
    }

    @Test(expectedExceptions = UpdateException.class)
    public void updateShouldFailOnWrongKeys() throws SQLException {
        createTable("CREATE TABLE table1 (id INTEGER PRIMARY KEY, name TEXT)");
        Table<?> table1 = lSql.table("table1");
        Row row = new Row().addKeyVals("id", 1, "name", "Max");
        table1.insert(row);

        row.put("wrong", "value");
        table1.update(row);
    }

    @Test
    public void updateById() throws SQLException {
        createTable("CREATE TABLE table1 (id INTEGER PRIMARY KEY, name TEXT)");
        Table<?> table1 = lSql.table("table1");
        Row row = new Row().addKeyVals("id", 1, "name", "Max");
        table1.insert(row);
        LinkedRow queriedRow = table1.load(1).get();
        assertEquals(queriedRow, row);

        row.put("name", "John");
        table1.update(row);
        queriedRow = table1.load(1).get();
        assertEquals(queriedRow, row);
    }

    @Test(expectedExceptions = UpdateException.class)
    public void updateWithWrongId() throws SQLException {
        createTable("CREATE TABLE table1 (id SERIAL PRIMARY KEY, name TEXT)");
        Table<?> table1 = lSql.table("table1");
        Row row = new Row().addKeyVals("name", "Max");
        Object id = table1.insert(row).get();
        LinkedRow queriedRow = table1.load(id).get();
        assertEquals(queriedRow, row);

        row.put("id", 999);
        row.put("name", "John");
        table1.update(row);
    }

    @Test
    public void save() throws SQLException {
        createTable("CREATE TABLE table1 (id SERIAL PRIMARY KEY, name TEXT)");
        Table<?> table1 = lSql.table("table1");

        // Insert
        Row row = Row.fromKeyVals("name", "Max");
        Object id = table1.save(row).get();
        assertEquals(id, row.get(table1.getPrimaryKeyColumn().get()));

        // Verify insert
        LinkedRow queriedRow = table1.load(id).get();
        assertEquals(queriedRow, row);

        // Update
        row.put("name", "John");
        id = table1.save(row).get();

        // Verify update
        queriedRow = table1.load(id).get();
        assertEquals(queriedRow, row);
    }

    @Test
    public void saveWithoutAutoIncrement() throws SQLException {
        createTable("CREATE TABLE table1 (id INT PRIMARY KEY, name TEXT)");
        Table<?> table1 = lSql.table("table1");

        // Insert
        Row row = Row.fromKeyVals("id", 1, "name", "Max");
        Object id = table1.save(row).get();
        assertEquals(id, row.get(table1.getPrimaryKeyColumn().get()));

        // Verify insert
        LinkedRow queriedRow = table1.load(id).get();
        assertEquals(queriedRow, row);

        // Update
        row.put("name", "John");
        id = table1.save(row).get();

        // Verify update
        queriedRow = table1.load(id).get();
        assertEquals(queriedRow, row);

        List<QueriedRow> rows = lSql.executeRawQuery("SELECT * FROM table1").asList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void delete() throws SQLException {
        createTable("CREATE TABLE table1 (id SERIAL PRIMARY KEY, name TEXT)");
        Table<?> table1 = lSql.table("table1");

        // Insert
        Row row = new Row().addKeyVals("name", "Max");
        table1.insert(row).get();

        // Verify insert
        int tableSize = lSql.executeRawQuery("SELECT * FROM table1;").asList().size();
        assertEquals(tableSize, 1);

        // Insert 2nd row
        table1.insert(new Row().addKeyVals("name", "Phil"));

        // Delete
        table1.delete(row);

        // Verify delete
        tableSize = lSql.executeRawQuery("SELECT * FROM table1;").asList().size();
        assertEquals(tableSize, 1);
    }

    @Test
    public void fetchColumns() throws SQLException {
        createTable("CREATE TABLE table1 (id SERIAL PRIMARY KEY, name TEXT, age INT)");
        Table<?> table1 = lSql.table("table1");
        assertEquals(table1.getColumns().size(), 3);
        assertTrue(table1.getColumns().containsKey("id"));
        assertTrue(table1.getColumns().containsKey("name"));
        assertTrue(table1.getColumns().containsKey("age"));
    }

    @Test
    public void fetchMetaWithRecursiveFkTable() {
        createTable("CREATE TABLE table1 (id SERIAL PRIMARY KEY, ref INT REFERENCES table1(id))");
        lSql.table("table1");
    }

    @Test
    public void validate() throws SQLException {
        createTable("CREATE TABLE table1 (id SERIAL PRIMARY KEY, field1 INT, field2 INT)");
        Table<?> table1 = lSql.table("table1");

        Row r = Row.fromKeyVals(
                "field1", 1,
                "field2", "2",
                "field3", 3
        );

        Map<String, AbstractValidationError> validate = table1.validate(r);
        assertEquals(validate.size(), 2);
        assertEquals(validate.get("field2").getClass(), TypeError.class);
        assertEquals(validate.get("field3").getClass(), KeyError.class);
    }

    @Test
    public void ignoreColumnOnInsert() {
        createTable("CREATE TABLE t1 (c1 INT, c2 INT DEFAULT 999)");
        Table t1 = lSql.table("t1");
        t1.column("c2").setIgnored(true);
        t1.insert(Row.fromKeyVals("c1", 1, "c2", 2));
        QueriedRow row = lSql.executeRawQuery("SELECT c1, sum(c2) AS s FROM t1 GROUP BY c1").getFirstRow().get();
        assertEquals(row.getInt("c1"), (Integer) 1);
        assertEquals(row.getInt("s"), (Integer) 999);
    }

    @Test
    public void ignoreColumnOnUpdate() {
        createTable("CREATE TABLE t1 (c1 INT PRIMARY KEY, c2 INT DEFAULT 999, c3 INT)");
        lSql.executeRawSql("INSERT INTO t1 (c1, c2, c3) VALUES (1, 555, 3)");

        Table t1 = lSql.table("t1");
        t1.column("c2").setIgnored(true);
        t1.update(Row.fromKeyVals("c1", 1, "c2", 2, "c3", 3));

        QueriedRow row = lSql.executeRawQuery("SELECT c1, sum(c2) AS s FROM t1 GROUP BY c1").getFirstRow().get();
        assertEquals(row.getInt("c1"), (Integer) 1);
        assertEquals(row.getInt("s"), (Integer) 555);
    }

    @Test
    public void ignoreColumnOnQuery() {
        createTable("CREATE TABLE t1 (c1 INT, c2 INT)");
        lSql.executeRawSql("INSERT INTO t1 (c1, c2) VALUES (1, 555)");

        Table t1 = lSql.table("t1");
        t1.column("c2").setIgnored(true);

        QueriedRow row = lSql.executeRawQuery("SELECT c1, c2 FROM t1").getFirstRow().get();
        assertEquals(row.getInt("c1"), (Integer) 1);
        assertFalse(row.containsKey("c2"));
    }

}
