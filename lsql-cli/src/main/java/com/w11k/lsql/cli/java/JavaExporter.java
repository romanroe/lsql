package com.w11k.lsql.cli.java;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.w11k.lsql.LSql;
import com.w11k.lsql.TableLike;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static com.w11k.lsql.cli.CodeGenUtils.log;

public class JavaExporter {

    private final LSql lSql;

    private final List<? extends TableLike> tables;

    private List<StatementFileExporter> statementFileExporters;

    private File outputRootPackageDir = null;

    private String packageName;

    private boolean guice = false;

    public JavaExporter(LSql lSql, List<? extends TableLike> tables) throws SQLException {
        this.lSql = lSql;
        this.tables = tables;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setOutputRootPackageDir(File outputDir) {
        this.outputRootPackageDir = outputDir;
    }

    public File getOutputRootPackageDir() {
        return outputRootPackageDir;
    }

    public boolean isGuice() {
        return guice;
    }

    public void setGuice(boolean guice) {
        this.guice = guice;
    }

    public List<StatementFileExporter> getStatementFileExporters() {
        return statementFileExporters;
    }

    public void setStatementFileExporters(List<StatementFileExporter> statementFileExporters) {
        this.statementFileExporters = statementFileExporters;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void export() {
        List<JavaRowClassExporter> tableRowClassExporters = Lists.newLinkedList();
        Iterable<? extends TableLike> tables = this.tables;

        Set<StructuralTypingField> structuralTypingFields = Sets.newHashSet();

        // Tables
        for (TableLike cc : tables) {
            // collect all row classes
            JavaRowClassExporter rowClassExporter = new JavaRowClassExporter(this.lSql, cc, this);
            tableRowClassExporters.add(rowClassExporter);

            // generate table classes
            new TableExporter(this.lSql, cc, this).export();
        }

        // StatementFileExporter
        for (StatementFileExporter statementFileExporter : this.statementFileExporters) {
            Set<StructuralTypingField> stfs = statementFileExporter.exportAndGetStructuralTypingFields();
            structuralTypingFields.addAll(stfs);
        }

        // find unique StructuralTypingFields
        for (JavaRowClassExporter tableRowClassExporter : tableRowClassExporters) {
            structuralTypingFields.addAll(tableRowClassExporter.getStructuralTypingFields());
        }

        // generate StructuralTypingField
        for (StructuralTypingField structuralTypingField : structuralTypingFields) {
            new StructuralTypingFieldExporter(structuralTypingField, this).export();
        }

        // generate row classes
        for (JavaRowClassExporter tableRowClassExporter : tableRowClassExporters) {
            tableRowClassExporter.export();
        }

        log("");
    }


}
