package com.w11k.lsql;

import com.google.common.base.Optional;
import com.w11k.lsql.converter.Converter;
import com.w11k.lsql.utils.SqlTypesNames;
import com.w11k.lsql.validation.AbstractValidationError;
import com.w11k.lsql.validation.StringTooLongError;
import com.w11k.lsql.validation.TypeError;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

public class Column {

    private final String columnName;

    private final int columnSize;

    private final int sqlType;

    private final Table table;

    private final Converter converter;

    private boolean ignored = false;

    private boolean ignoreOnUpdate = false;

    private boolean nullable;

//    public static Column create(Table table,
//                                    String columnName,
//                                    int sqlType,
//                                    Converter converter,
//                                    int columnSize) {
//
//        Optional<Table> tableOptional = Optional.fromNullable(table);
//        return new Column(tableOptional, columnName, sqlType, converter, columnSize);
//    }

    /**
     * @param table      The corresponding table. Optional.absent(), if this column is
     *                   based on a function (e.g. count) or used for a raw list.
     * @param columnName The name of the column.
     * @param sqlType    The java.sql.Types value
     * @param converter  Converter instance used to convert between SQL and Java values.
     * @param columnSize The maximum column size. -1 if not applicable.
     */
    public Column(Table table, String columnName, int sqlType, Converter converter, int columnSize) {
        this.table = table;
        this.columnName = columnName;
        this.sqlType = sqlType;
        this.converter = converter;
        this.columnSize = columnSize;
    }

    public String getJavaColumnName() {
        return this.columnName;
    }

    public String getSqlColumnName() {
        return this.table.getlSql().convertInternalSqlToExternalSql(getJavaColumnName());
    }

    public Table getTable() {
        return this.table;
    }

    public int getSqlType() {
        return this.sqlType;
    }

    public Converter getConverter() {
        return this.converter;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public boolean isIgnoreOnUpdate() {
        return ignoreOnUpdate;
    }

    public void setIgnoreOnUpdate(boolean ignoreOnUpdate) {
        this.ignoreOnUpdate = ignoreOnUpdate;
    }

    public Optional<? extends AbstractValidationError> validateValue(Object value) {
        if (!this.converter.isValueValid(value)) {
            return of(new TypeError(
              this.table.getSqlSchemaAndTableName(),
                    this.columnName,
                    this.converter.getJavaType().getSimpleName(), value.getClass().getSimpleName()));
        }

        Class<?> targetType = this.converter.getJavaType();
        if (this.columnSize != -1 && String.class.isAssignableFrom(targetType)) {
            String string = (String) value;
            if (string != null && string.length() > this.columnSize) {
                return of(new StringTooLongError(
                        this.table.getSqlSchemaAndTableName(), this.columnName, this.columnSize, string.length()));
            }
        }

        return absent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Column that = (Column) o;
        return this.columnName.equals(that.columnName) && this.table.equals(that.table);

    }

    @Override
    public int hashCode() {
        int result = this.table.hashCode();
        result = 31 * result + this.columnName.hashCode();
        return result;
    }


    @Override
    public String toString() {
        return "Column{" +
                "table=" + table.getSqlSchemaAndTableName() +
                ", name=" + columnName +
                ", sqlType=" + SqlTypesNames.getName(sqlType) +
                ", javaType=" + converter.getJavaType().getCanonicalName() +
                '}';
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isNullable() {
        return nullable;
    }
}
