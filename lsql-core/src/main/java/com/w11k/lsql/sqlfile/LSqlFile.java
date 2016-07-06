package com.w11k.lsql.sqlfile;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.w11k.lsql.LSql;
import com.w11k.lsql.SqlStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.ImmutableMap.copyOf;

public class LSqlFile {

    private static final Pattern STMT_BLOCK_BEGIN = Pattern.compile(
            "^--\\s*(\\w*)\\s*$",
            Pattern.MULTILINE);

    private static final Pattern STMT_BLOCK_END = Pattern.compile(
            ";\\s*$",
            Pattern.MULTILINE);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final LSql lSql;

    private final String fileName; // without .sql extension

    private final String path;

    private final Map<String, SqlStatement> statements = Maps.newHashMap();

    public LSqlFile(LSql lSql, String fileName, String path) {
        this.lSql = lSql;
        this.fileName = fileName;
        this.path = path;
        parseSqlStatements();
    }

    public String getFileName() {
        return fileName;
    }

    // ----- public -----

    public ImmutableMap<String, SqlStatement> getStatements() {
        return copyOf(statements);
    }

    public SqlStatement statement(String name) {
        if (!statements.containsKey(name)) {
            throw new IllegalArgumentException("No statement with name '" + name +
                                                       "' found in file '" + path + "'.");
        }
        return statements.get(name);
    }

    // ----- private -----

    private void parseSqlStatements() {
        logger.info("Reading SQL file '" + fileName + "'");
        statements.clear();

        InputStream is = getClass().getResourceAsStream(path);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String content = CharStreams.toString(reader);
            Matcher startMatcher = STMT_BLOCK_BEGIN.matcher(content);
            while (startMatcher.find()) {
                String name = startMatcher.group(1);
                String sub = content.substring(startMatcher.end());
                Matcher endMatcher = STMT_BLOCK_END.matcher(sub);
                if (!endMatcher.find()) {
                    throw new IllegalStateException(
                            "Could not find the end of the SQL expression '" +
                                    name + "'. Did you add ';' at the end?");
                }
                sub = sub.substring(0, endMatcher.end()).trim();
                logger.debug("Found SQL statement '{}'", name);
                statements.put(name, new SqlStatement(lSql, fileName + "." + name, sub));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
