package com.w11k.lsql.cli;

import com.google.common.io.MoreFiles;
import com.w11k.lsql.Config;
import com.w11k.lsql.LSql;
import com.w11k.lsql.cli.java.JavaExporter;
import com.w11k.lsql.jdbc.ConnectionProviders;
import com.w11k.lsql.sqlfile.LSqlFile;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.w11k.lsql.cli.CodeGenUtils.log;

public class Main {

    private String configClassName;
    private String url;
    private String user;
    private String password;

    private String packageName;
    private String sqlStatements;
    private boolean guice = false;
    private String outDirJava;
    private String outDirTypeScript;

    public Main(String[] args) throws ClassNotFoundException, SQLException {
        log("=================");
        log("LSql CLI Exporter");
        log("=================\n");

        this.parseArgs(args);

        // Config
        @SuppressWarnings("unchecked")
        Class<? extends Config> configClass =
                (Class<? extends Config>) Main.class.getClassLoader().loadClass(configClassName);

        log("Config class:", configClass.getCanonicalName(), "\n");

        Connection connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(false);

        log("JDBC URL:", url, "\n");

        LSql lSql = new LSql(configClass, ConnectionProviders.fromInstance(connection));
        lSql.fetchMetaDataForAllTables();

        // Java table and row classes
        JavaExporter javaExporter = new JavaExporter(lSql);
        javaExporter.setPackageName(this.packageName);
        File outDirJava = new File(this.outDirJava);
        //noinspection ResultOfMethodCallIgnored
        outDirJava.mkdirs();
        javaExporter.setOutputPath(outDirJava);
        javaExporter.setGuice(this.guice);
        javaExporter.export();

        // Java statements
        if (this.sqlStatements != null) {

            Iterable<Path> children = MoreFiles.directoryTreeTraverser().preOrderTraversal(new File(this.sqlStatements).toPath());
            for (Path child : children) {
                File file = child.toFile();
                if (file.isFile() && file.getName().endsWith(".sql")) {
                    System.out.println("child = " + child);

                    LSqlFile lSqlFile = new LSqlFile(lSql, file.getAbsolutePath(), file.getAbsolutePath());

                }
            }

        }

//        TypeScriptExporter tse = new TypeScriptExporter(lSql);
//        File outDirTs = new File(this.outDirTypeScript);
//        tse.setOutputPath(outDirTs);
//        tse.export();
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        new Main(args);
    }

    private void parseArgs(String[] args) {
        for (String arg : args) {
            String value = getParamValue(arg);
            if (arg.startsWith("config:")) {
                this.configClassName = value;
            } else if (arg.startsWith("url:")) {
                this.url = value;
            } else if (arg.startsWith("user:")) {
                this.user = value;
            } else if (arg.startsWith("password:")) {
                this.password = value;
            } else if (arg.startsWith("package:")) {
                this.packageName = value;
            } else if (arg.startsWith("sqlStatements:")) {
                this.sqlStatements = value;
            } else if (arg.startsWith("outDirJava:")) {
                this.outDirJava = value;
            } else if (arg.startsWith("di:")) {
                if (value.equalsIgnoreCase("guice")) {
                    this.guice = true;
                }
            }
        }
    }

    private String getParamValue(String param) {
        return param.substring(param.indexOf(":") + 1).trim();
    }
}
