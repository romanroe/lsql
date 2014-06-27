package com.w11k.lsql.jdbc;

import com.w11k.lsql.LSql;
import com.w11k.lsql.exceptions.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);

    public static Connection getConnection(LSql lSql) {
        try {
            logger.debug("Obtaining connection");
            return lSql.getConnectionProvider().call();
        } catch (Exception e) {
            throw new DatabaseAccessException(e);
        }
    }

    public static Statement createStatement(LSql lSql) {
        try {
            return getConnection(lSql).createStatement();
        } catch (SQLException e) {
            throw new DatabaseAccessException(e);
        }
    }

    public static PreparedStatement prepareStatement(LSql lSql, String sqlString,
                                                     boolean returnAutoGeneratedKeys) {
        try {
            if (returnAutoGeneratedKeys) {
                return getConnection(lSql).prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS);
            } else {
                return getConnection(lSql).prepareStatement(sqlString);
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(e);
        }
    }

}
