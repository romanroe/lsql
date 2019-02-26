package com.w11k.lsql.tests;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.w11k.lsql.*;
import com.w11k.lsql.converter.Converter;
import com.w11k.lsql.dialects.PostgresDialect;
import com.w11k.lsql.dialects.RowKeyConverter;
import com.w11k.lsql.exceptions.QueryException;
import com.w11k.lsql.query.PlainQuery;
import com.w11k.lsql.statement.AnnotatedSqlStatementToQuery;
import com.w11k.lsql.statement.AnnotatedSqlStatement;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SqlStatementTest extends AbstractLSqlTest {

    @BeforeMethod
    public void setRowKeyConverter() {
        addConfigHook(config -> {
            config.setRowKeyConverter(RowKeyConverter.JAVA_CAMEL_CASE_TO_SQL_LOWER_UNDERSCORE);
        });
    }

    @Test
    public void statementNoParameter() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person").query().toList();
        assertEquals(rows.size(), 5);
    }

    @Test(expectedExceptions = QueryException.class)
    public void statementUnusedParameter() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE id = /*xxxxx=*/ -1 /**/;").query(
                "id", 1
        ).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementOneParameter() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE id = /*id=*/ -1 /**/;").query("id", 1).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementOneUnnamed() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE id = /*=*/ -1 /**/;").query("id", 1).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementOneUnnamed2() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE id != /*=*/ -1 /**/;").query("id", 1).toList();
        assertEquals(rows.size(), 4);
    }

    @Test
    public void statementOneUnnamed3() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE id!= /*=*/ -1 /**/;").query("id", 1).toList();
        assertEquals(rows.size(), 4);
    }

    @Test
    public void statementOneUnnamedWithTableNameInQuery() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE person.id = /*=*/ -1 /**/;").query("person.id", 1).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementOneParameterSpecialName1() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE id = /*id_a=*/ -1 /**/;").query("id_a", 1).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementOneParameterSpecialName2() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE id = /*id_1=*/ -1 /**/;").query("id_1", 1).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementOneParameterSpecialName3() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE id = /*person.id=*/ -1 /**/;").query("person.id", 1).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementOneParameterSpecialName4() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE id = /* person.id = */ -1 /**/;").query("person.id", 1).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementOneParameterMultipleOccurence() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE " +
                "id > /*val=*/ 99999 /**/\n" +
                "AND age > /*val=*/ 99999 /**/" +
                ";")
                .query("val", 3).toList();
        assertEquals(rows.size(), 2);
    }

    @Test
    public void statementTwoParameters() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE " +
                "id = /*id=*/ -1 /**/\n" +
                "AND age = /*age=*/ -1 /**/" +
                ";")
                .query(
                        "id", 2,
                        "age", 12
                ).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementThreeParameters() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE " +
                "id = /*id=*/ -1 /**/\n" +
                "AND age = /*age=*/ -1 /**/\n" +
                "AND fullname = /*fullname=*/ 'xxx' /**/" +
                ";")
                .query(
                        "id", 3,
                        "age", 13,
                        "fullname", "c"
                ).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementThreeParametersInOneLine() {
        setup();
        List<Row> rows = lSql.createSqlStatement(
                "SELECT * FROM person WHERE " +
                        "id = /*id=*/ -1 /**/ AND age = /*age=*/ -1 /**/ AND fullname = /*fullname=*/ 'xxx' /**/;")
                .query(
                        "id", 3,
                        "age", 13,
                        "fullname", "c"
                ).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementThreeParametersUnused() {
        setup();
        List<Row> rows = lSql.createSqlStatement(
                "SELECT * FROM person WHERE " +
                        "id = /*id=*/ 1 /**/ AND age = /*age=*/ 11 /**/ AND fullname = /*fullname=*/ 'a' /**/;")
                .query().toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementThreeParametersUnnamed() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE " +
                "id = /*=*/ -1 /**/" +
                "AND age = /*=*/ -1 /**/" +
                "AND fullname = /*=*/ 'xxx' /**/" +
                ";")
                .query(
                        "id", 4,
                        "age", 14,
                        "fullname", "d"
                ).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementThreeParametersUnnamedMissingSpace() {
        setup();
        List<Row> rows = lSql.createSqlStatement("SELECT * FROM person WHERE " +
                "id = /*=*/ -1 /**/" +
                "AND age = /*=*/ -1 /**/" +
                "AND fullname= /*=*/ 'xxx' /**/" +
                ";")
                .query(
                        "id", 4,
                        "age", 14,
                        "fullname", "d"
                ).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementThreeParametersMultipleOccurences() {
        setup();
        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement("SELECT * FROM person WHERE " +
                "id > /*val=*/ 99999 /**/" +
                "AND age > /*val=*/ 99999 /**/" +
                "AND fullname= /*=*/ 'c' /**/" +
                ";");

        List<Row> rows = statement.query("val", 3, "fullname", "d").toList();
        assertEquals(rows.size(), 1);
        rows = statement.query("val", 4, "fullname", "e").toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void statementInsert() {
        setup();
        lSql.executeRawSql("delete from person;");

        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement(
                "insert into person (id, age) \n" +
                        "values ( /*id=*/ 100 /**/,/*age=*/ 100 /**/);"
        );

        statement.execute("id", 100, "age", 100);
        statement.execute("id", 101, "age", 101);

        List<Row> rows = lSql.createSqlStatement("select * from person").query().toList();
        assertEquals(rows.size(), 2);
    }

    @Test
    public void statementQueryParameter() {
        setup();
        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement("SELECT * FROM \n" +
                "person \n" +
                "WHERE \n" +
                "id = /*=*/ 99999 /**/\n" +
                "and age = /*: age =*/ 11 /**/\n;");

        ImmutableMap<String, List<AnnotatedSqlStatement.Parameter>> params = statement.getParameters();
        assertEquals(params.get("id").get(0).getName(), "id");
        assertEquals(params.get("age").get(0).getName(), "age");

        List<Row> rows = statement.query("id", (QueryParameter) (ps, index) -> ps.setInt(index, 1)).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void nullValueAsQueryParameter() {
        createTable();
        insert(0, 50, null);
        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement(
                "SELECT * FROM person WHERE fullname IS /*: string =*/ NULL /**/;");

        List<Row> rows = statement.query("fullname", null).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void nullValueAsNamedQueryParameter() {
        createTable();
        insert(0, 50, null);
        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement(
                "SELECT * FROM person WHERE fullname IS /*aaa: string =*/ NULL /**/;");

        List<Row> rows = statement.query("aaa", null).toList();
        assertEquals(rows.size(), 1);
    }

    @Test
    public void literalQueryParameter() {
        setup();
        List<Row> rows;

        AnnotatedSqlStatementToQuery<PlainQuery> statement = this.lSql.createSqlStatement("select * from person where " +
                "age in (/*ages=*/ 11, 12, 13 /**/) " +
                "and 1 = /*param=*/ 1 /**/;");

        final int[] ages = new int[]{11, 12};

        // Manual String concat
        rows = statement.query(
                "ages", new LiteralQueryParameter() {
                    @Override
                    public String getSqlString() {
                        return "11, 12";
                    }

                    @Override
                    public int getNumberOfQueryParameters() {
                        return 0;
                    }

                    @Override
                    public void set(PreparedStatement ps, int preparedStatementIndex, int localIndex) {
                    }
                },
                "param", 1
        ).toList();
        assertEquals(rows.size(), 2);

        // Use parameters
        rows = statement.query(
                "ages", new LiteralQueryParameter() {
                    @Override
                    public String getSqlString() {
                        return "?, ?";
                    }

                    @Override
                    public int getNumberOfQueryParameters() {
                        return 2;
                    }

                    @Override
                    public void set(PreparedStatement ps, int preparedStatementIndex, int localIndex) throws SQLException {
                        ps.setInt(preparedStatementIndex, ages[localIndex]);
                    }
                },
                "param", 1
        ).toList();
        assertEquals(rows.size(), 2);
    }

    @Test
    public void listLiteralQueryParameter() {
        setup();
        List<Row> rows;

        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement("select * from person where" +
                " age in (/*ages=*/ 11, 12, 13 /**/) " +
                "and 1 = /*param=*/ 1 /**/;");

        // API Version 1
        List<Integer> ages = Lists.newArrayList(11, 12);
        rows = statement.query(
                "ages", ListLiteralQueryParameter.of(ages),
                "param", 1
        ).toList();
        assertEquals(rows.size(), 2);

        // API Version 2
        rows = statement.query(
                "ages", ListLiteralQueryParameter.of(11, 12),
                "param", 1
        ).toList();
        assertEquals(rows.size(), 2);
    }

    @Test
    public void listLiteralQueryParameterWithPlaceholderStrings() {
        setup();

        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement("select * from person where" +
                " fullname in (/*fullnames=*/ 'a', 'b', 'c' /**/) ;");

        List<Row> rows = statement.query("fullnames", ListLiteralQueryParameter.of("a", "x")).toList();
        assertEquals(rows.size(), 1);
    }

    @Test()
    public void listLiteralQueryParameterEmptyArray() {
        boolean skipTest = lSql.getDialectClass().equals(PostgresDialect.class);
        if (skipTest) {
            throw new SkipException("empty list literal not support");
        }

        setup();
        List<Row> rows;

        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement("select * from person where" +
                " age in (/*ages=*/ 11, 12, 13 /**/) " +
                "and 1 = /*param=*/ 1 /**/;");

        // API Version 2, empty
        rows = statement.query(
                "ages", ListLiteralQueryParameter.of(),
                "param", 1
        ).toList();
        assertEquals(rows.size(), 0);
    }

    @Test()
    public void resultSetTypeAnnotations() {
        setup();

        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement(
                "select count(id) as count_id /*:string*/, max(age) as max_age /*:long*/ from person;");
        PlainQuery query = statement.query();
        List<Row> rows = query.toList();
        assertEquals(rows.size(), 1);
        Row row = rows.get(0);
        assertTrue(row.get("countId") instanceof String);
        assertTrue(row.get("maxAge") instanceof Long);
    }

    @Test()
    public void resultSetTypeAnnotationsWithQuotedIdentifier() {
        setup();

        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement(
                "select count(id) as \"count_id\" /*:string*/, max(age) as \"max_age\" /*:long*/ from person;");
        PlainQuery query = statement.query();
        List<Row> rows = query.toList();
        assertEquals(rows.size(), 1);
        Row row = rows.get(0);
        assertTrue(row.get("countId") instanceof String);
        assertTrue(row.get("maxAge") instanceof Long);
    }

    @Test()
    public void explicitParameterConverter() {
        setup();

        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement(
                "select * from person where id = /*=*/ 1 /**/;")
                .setParameterConverter("id", new Converter(String.class, Types.INTEGER) {
                    @Override
                    public void setValue(LSql lSql, PreparedStatement ps, int index, Object val) throws SQLException {
                        ps.setInt(1, Integer.parseInt(val.toString()));
                    }

                    @Nullable
                    @Override
                    public Object getValue(LSql lSql, ResultSet rs, int index) {
                        return null;
                    }
                });

        Row row = statement.query("id", "1").toList().get(0);
        Assert.assertEquals(row.getInt("id"), new Integer(1));
    }

    @Test()
    public void queryParameterTypeAnnotations1() {
        setup();

        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement(
                "select * from person where age > /*:int=*/ 18 /**/;");

        AnnotatedSqlStatement.Parameter param = statement.getParameters().get("age").get(0);
        Assert.assertEquals(param.getJavaTypeAlias(), "int");
    }

    @Test()
    public void queryParameterTypeAnnotations2() {
        setup();

        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement(
                "select * from person where age > /* :  int  =*/ 18 /**/;");

        AnnotatedSqlStatement.Parameter param = statement.getParameters().get("age").get(0);
        Assert.assertEquals(param.getJavaTypeAlias(), "int");
    }

    @Test()
    public void queryParameterTypeAnnotations3() {
        setup();

        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement(
                "select * from person where age > /*p1:int=*/ 18 /**/;");

        AnnotatedSqlStatement.Parameter param = statement.getParameters().get("p1").get(0);
        Assert.assertEquals(param.getJavaTypeAlias(), "int");
    }

    @Test()
    public void queryParameterTypeAnnotations4() {
        setup();

        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement(
                "select * from person where age > /* p1 : int =*/ 18 /**/;");

        AnnotatedSqlStatement.Parameter param = statement.getParameters().get("p1").get(0);
        Assert.assertEquals(param.getJavaTypeAlias(), "int");
    }

    @Test()
    public void queryParameterTypeAnnotations5() {
        setup();

        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement(
                "select * from person where age > /*p1: int =*/ 18 /**/;");

        AnnotatedSqlStatement.Parameter param = statement.getParameters().get("p1").get(0);
        Assert.assertEquals(param.getJavaTypeAlias(), "int");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*age.*Integer.*String")
    public void queryParameterTypeAnnotationsAreCheckedAtQueryTime() {
        setup();

        AnnotatedSqlStatementToQuery<PlainQuery> statement = lSql.createSqlStatement(
                "select * from person where age > /*:int =*/ 18 /**/;");

        statement.query("age", "3").toList();
    }

    private void setup() {
        createTable();
        insert(1, 11, "a");
        insert(2, 12, "b");
        insert(3, 13, "c");
        insert(4, 14, "d");
        insert(5, 15, "e");
    }

    private void createTable() {
        lSql.executeRawSql("CREATE TABLE person (" +
                "id INT PRIMARY KEY," +
                "age INT," +
                "fullname VARCHAR(100)" +
                ")");
    }

    private void insert(int id, int age, String fullname) {
        Table person = lSql.table("person");
        person.insert(Row.fromKeyVals("id", id, "age", age, "fullname", fullname));
    }

}
