package com.w11k.lsql.pojo;

import com.google.common.base.Optional;
import com.w11k.lsql.LinkedRow;
import com.w11k.lsql.PojoTable;
import com.w11k.lsql.Row;
import com.w11k.lsql.Table;
import com.w11k.lsql.converter.predefined.AtomicIntegerConverter;
import com.w11k.lsql.dialects.RowKeyConverter;
import com.w11k.lsql.AbstractLSqlTest;
import com.w11k.lsql.testdata.Person;
import com.w11k.lsql.testdata.PersonSubclass;
import com.w11k.lsql.testdata.PersonTestData;
import com.w11k.lsql.testdata.PersonWithAtomicIntegerAge;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

public class PojoTableTest extends AbstractLSqlTest {

    @BeforeMethod
    public void setRowKeyConverter() {
        addConfigHook(config -> {
            config.setRowKeyConverter(RowKeyConverter.JAVA_CAMEL_CASE_TO_SQL_LOWER_UNDERSCORE);
        });
    }

    @Test
    public void insert() {
        PersonTestData.init(this.lSql, false);
        PojoTable<Person> personTable = this.lSql.table("person", Person.class);
        Person p1 = new Person(1, "Max", 30);
        personTable.insert(p1);
        Table personRowTable = this.lSql.table("person");
        LinkedRow linkedRow = personRowTable.load(1).get();
        assertEquals(linkedRow.getInt("idPk"), Integer.valueOf(1));
        assertEquals(linkedRow.getString("firstName"), "Max");
    }

    @Test
    public void update() {
        PersonTestData.init(this.lSql, false);
        PojoTable<Person> personTable = this.lSql.table("person", Person.class);
        Person p1 = new Person(1, "Max", 30);
        personTable.insert(p1);

        p1.setFirstName("Walter");
        personTable.update(p1);

        Table personRowTable = this.lSql.table("person");
        LinkedRow linkedRow = personRowTable.load(1).get();
        assertEquals(linkedRow.getInt("idPk"), Integer.valueOf(1));
        assertEquals(linkedRow.getString("firstName"), "Walter");
    }

    @Test
    public void delete() {
        PersonTestData.init(this.lSql, false);
        PojoTable<Person> personTable = this.lSql.table("person", Person.class);
        Person p1 = new Person(1, "Max", 30);
        personTable.insert(p1);

        personTable.delete(p1);

        Table personRowTable = this.lSql.table("person");
        Optional<LinkedRow> load = personRowTable.load(1);
        assertFalse(load.isPresent());
    }

    @Test
    public void deleteById() {
        PersonTestData.init(this.lSql, false);
        PojoTable<Person> personTable = this.lSql.table("person", Person.class);
        Person p1 = new Person(1, "Max", 30);
        personTable.insert(p1);

        personTable.deleteById(1);

        Table personRowTable = this.lSql.table("person");
        Optional<LinkedRow> load = personRowTable.load(1);
        assertFalse(load.isPresent());
    }

    @Test
    public void insertAssignsDefaultValue() {
        PersonTestData.init(this.lSql, false);
        PojoTable<Person> personTable = this.lSql.table("person", Person.class);
        Person p1 = new Person();
        p1.setIdPk(1);
        personTable.insert(p1);
        assertEquals(p1.getTitle(), "n/a");
    }

    @Test
    public void insertIgnoresDefaultValueOnPureInsert() {
        PersonTestData.init(this.lSql, false);
        PojoTable<Person> personTable = this.lSql.table("person", Person.class);
        Person p1 = new Person();
        p1.setIdPk(1);
        personTable.insert(p1, true);
        assertNull(p1.getTitle());
    }

    @Test
    public void loadById() {
        PersonTestData.init(this.lSql, true);
        PojoTable<Person> personTable = this.lSql.table("person", Person.class);

        Person person = personTable.load(1).get();
        assertEquals(person.getIdPk(), 1);
        assertEquals(person.getFirstName(), "Adam");
    }

    @Test
    public void insertIgnoresFieldsFromSubclass() {
        PersonTestData.init(this.lSql, false);
        PojoTable<Person> personTable = this.lSql.table("person", Person.class);
        PersonSubclass p = new PersonSubclass(1, "Adam", 30);
        personTable.insert(p);
    }

    @Test
    public void fieldsUsesColumnConverter() {
        PersonTestData.init(this.lSql, false);
        this.addConfigHook(c ->
                c.setConverter("person", "age", new AtomicIntegerConverter()));

        PojoTable<PersonWithAtomicIntegerAge> personTable = this.lSql.table("person", PersonWithAtomicIntegerAge.class);

        PersonWithAtomicIntegerAge adam1 = new PersonWithAtomicIntegerAge(1, "Adam", new AtomicInteger(30));
        personTable.insert(adam1);

        PersonWithAtomicIntegerAge adam2 = personTable.load(1).get();
        assertEquals(adam1, adam2);

        Object ai = this.lSql.table("person").load(1).get().get("age");
        assertTrue(ai instanceof AtomicInteger);
        assertEquals(((AtomicInteger) ai).get(), 30);
    }

//    @Test(
//            expectedExceptions = IllegalArgumentException.class,
//            expectedExceptionsMessageRegExp = ".*missing field.*title.*String.*"
//    )
//    public void errorMessageOnMissingField() {
//        PersonTestData.init(this.lSql, false);
//        this.lSql.table("person", PersonMissingTitle.class);
//    }

//    @Test(
//            expectedExceptions = IllegalArgumentException.class,
//            expectedExceptionsMessageRegExp = ".*title.*wrong type.*Boolean.*String.*"
//    )
//    public void errorMessageOnWrongFieldType() {
//        PersonTestData.init(this.lSql, false);
//        this.lSql.table("person", PersonWrongTitleType.class);
//    }
//
//    @Test(
//            expectedExceptions = IllegalArgumentException.class,
//            expectedExceptionsMessageRegExp = ".*superfluous field.*oops.*"
//    )
//    public void errorMessageOnSuperfluousType() {
//        PersonTestData.init(this.lSql, false);
//        this.lSql.table("person", PersonWithSuperfluousField.class);
//    }

    @Test
    public void nullInColumnForStringFieldInPojo() {
        PersonTestData.init(this.lSql, false);
        Table personTable = this.lSql.table("person");
        personTable.insert(Row.fromKeyVals(
                "idPk", 1,
                "firstName", null,
                "age", 10,
                "title", "Title"
        ));
        PojoTable<Person> personPojoTable = personTable.withPojo(Person.class);
        Person person = personPojoTable.load(1).get();
        assertNull(person.getFirstName());
    }

}
