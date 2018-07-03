package com.w11k.lsql.cli.tests;

import com.w11k.lsql.cli.tests.structural_fields.*;
import com.w11k.lsql.cli.tests.stmtswithcustomconverter.*;
import java.util.*;

public class StmtsWithCustomConverter {

    // Statement: load ----------------------------

    private final String sql_load = "select * from custom_converter \n where field = /*: custom =*/ 1 /**/ \n ;";

    /**    
     * select * from custom_converter<br>
     * where field = &#42;&#47;: custom =&#47;&#42; 1 &#42;&#47;&#47;&#42;<br>
     * ;<br>
    */
    public load load() {
        return new load();
    }

    @SuppressWarnings({"Duplicates", "WeakerAccess"})
    public final class load extends com.w11k.lsql.TypedStatementQuery<Load> implements com.w11k.lsql.TableRow, Field_Com_W11k_Lsql_Cli_Tests_Testcliconfig_Customtype {

        // constructors ----------

        @SuppressWarnings("ConstantConditions")
        public load() {
            super(lSql, sql_load);
            this.field = null;
        }

        @SuppressWarnings("NullableProblems")
        private load(
                com.w11k.lsql.cli.tests.TestCliConfig.CustomType field) {
            super(lSql, sql_load);
            this.field = field;
    }

        // fields ----------

        @javax.annotation.Nullable public final com.w11k.lsql.cli.tests.TestCliConfig.CustomType field;

        @javax.annotation.Nullable public com.w11k.lsql.cli.tests.TestCliConfig.CustomType getField() {
            return this.field;
        }

        public load withField(@javax.annotation.Nullable com.w11k.lsql.cli.tests.TestCliConfig.CustomType field) {
            return new load(field);
        }

        // class methods ----------

        public java.util.Map<String, Object> toInternalMap() {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("field", this.field);
            return map;
        }

        public java.util.Map<String, Object> toMap() {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("field", this.field);
            return map;
        }

        // Object methods ----------

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            load that = (load) o;
            return     Objects.equals(field, that.field);
        }

        @Override
        public int hashCode() {
            return Objects.hash(field);
        }

        @Override
        public String toString() {
            return "load{" + "field=" + field + "}";
        }

        protected Load createTypedRow(com.w11k.lsql.Row row) {
            return Load.fromInternalMap(row);
        }

        protected java.util.Map<String, Object>  getQueryParameters() {
            return this.toInternalMap();
        }

        public String getStatementFileName() {
            return "StmtsWithCustomConverter.sql";
        }

        public String getStatementName() {
            return "load";
        }

    }

    // Statement: testQueryParamter ----------------------------

    private final String sql_testQueryParamter = "select \n * \n from \n person1 \n WHERE id = /*=*/ 1 /**/;";

    /**    
     * select<br>
     * *<br>
     * from<br>
     * person1<br>
     * WHERE id = &#42;&#47;=&#47;&#42; 1 &#42;&#47;&#47;&#42;;<br>
    */
    public testQueryParamter testQueryParamter() {
        return new testQueryParamter();
    }

    @SuppressWarnings({"Duplicates", "WeakerAccess"})
    public final class testQueryParamter extends com.w11k.lsql.TypedStatementQuery<TestQueryParamter> implements com.w11k.lsql.TableRow, Id_Number {

        // constructors ----------

        @SuppressWarnings("ConstantConditions")
        public testQueryParamter() {
            super(lSql, sql_testQueryParamter);
            this.id = null;
        }

        @SuppressWarnings("NullableProblems")
        private testQueryParamter(
                java.lang.Number id) {
            super(lSql, sql_testQueryParamter);
            this.id = id;
    }

        // fields ----------

        @javax.annotation.Nullable public final java.lang.Number id;

        @javax.annotation.Nullable public java.lang.Number getId() {
            return this.id;
        }

        public testQueryParamter withId(@javax.annotation.Nullable java.lang.Number id) {
            return new testQueryParamter(id);
        }

        // class methods ----------

        public java.util.Map<String, Object> toInternalMap() {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", this.id);
            return map;
        }

        public java.util.Map<String, Object> toMap() {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", this.id);
            return map;
        }

        // Object methods ----------

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            testQueryParamter that = (testQueryParamter) o;
            return     Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "testQueryParamter{" + "id=" + id + "}";
        }

        protected TestQueryParamter createTypedRow(com.w11k.lsql.Row row) {
            return TestQueryParamter.fromInternalMap(row);
        }

        protected java.util.Map<String, Object>  getQueryParameters() {
            return this.toInternalMap();
        }

        public String getStatementFileName() {
            return "StmtsWithCustomConverter.sql";
        }

        public String getStatementName() {
            return "testQueryParamter";
        }

    }

    private final com.w11k.lsql.LSql lSql;

    @com.google.inject.Inject
    public StmtsWithCustomConverter(com.w11k.lsql.LSql lSql) {
        this.lSql = lSql;
    }

}
