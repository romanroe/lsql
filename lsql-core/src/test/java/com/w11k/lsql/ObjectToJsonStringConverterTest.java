package com.w11k.lsql;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.fasterxml.jackson.core.type.TypeReference;
import com.w11k.lsql.converter.predefined.ObjectToJsonStringConverter;
import com.w11k.lsql.testdata.Person;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class ObjectToJsonStringConverterTest extends AbstractLSqlTest {

    @Test
    public void pojo() {
        createTable("CREATE TABLE table1 (id INT PRIMARY KEY, person TEXT)");
        this.addConfigHook(c ->
                c.setConverter("table1", "person", new ObjectToJsonStringConverter(Person.class, new TypeReference<Person>() {
                })));

        Table t1 = this.lSql.table("table1");

        Person p = new Person(1, "Adam", 30);
        t1.insert(Row.fromKeyVals("id", 1, "person", p));
        Row row = t1.load(1).get();
        assertEquals(row.get("person"), p);
    }

    @Test
    public void listOfString() {
        createTable("CREATE TABLE table1 (id INT PRIMARY KEY, data TEXT)");
        this.addConfigHook(c ->
                c.setConverter("table1", "data", new ObjectToJsonStringConverter(List.class, new TypeReference<List<String>>() {
                })));

        Table t1 = lSql.table("table1");

        List<String> list = Lists.newLinkedList();
        list.add("first");
        list.add("second");

        t1.insert(Row.fromKeyVals("id", 1, "data", list));
        Row row = t1.load(1).get();
        assertEquals(row.get("data"), list);
    }

    @Test
    public void listOfMapStringString() {
        createTable("CREATE TABLE table1 (id INT PRIMARY KEY, data TEXT)");
        this.addConfigHook(c ->
                c.setConverter("table1", "data", new ObjectToJsonStringConverter(List.class, new TypeReference<List<Map<String, String>>>() {
                })));


        Table t1 = lSql.table("table1");

        List<Map<String, String>> list = Lists.newLinkedList();
        Map<String, String> e = Maps.newHashMap();
        e.put("a", "1");
        e.put("b", "2");
        list.add(e);
        list.add(e);

        t1.insert(Row.fromKeyVals("id", 1, "data", list));
        Row row = t1.load(1).get();
        assertEquals(row.get("data"), list);
    }

    @Test
    public void listOfMapStringStringInALinkedRow() {
        createTable("CREATE TABLE table1 (id SERIAL PRIMARY KEY, data TEXT)");
        this.addConfigHook(c ->
                c.setConverter(
                        "table1",
                        "data",
                        new ObjectToJsonStringConverter(List.class, new TypeReference<List<Map<String, String>>>() {
                        })));

        Table t1 = lSql.table("table1");

        List<Map<String, String>> list = Lists.newLinkedList();
        Map<String, String> e = Maps.newHashMap();
        e.put("a", "1");
        e.put("b", "2");
        list.add(e);
        list.add(e);
        t1.newLinkedRow("id", 1, "data", list).save();

        Row row = t1.load(1).get();
        assertEquals(row.get("data"), list);
    }


}
