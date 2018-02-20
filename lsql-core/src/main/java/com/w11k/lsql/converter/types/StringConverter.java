package com.w11k.lsql.converter.types;

import com.w11k.lsql.LSql;
import com.w11k.lsql.converter.Converter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class StringConverter extends Converter {

    public static int[] SQL_TYPES = new int[]{
            Types.CHAR, Types.VARCHAR, Types.LONGNVARCHAR, Types.LONGVARCHAR, Types.NCHAR, Types.NVARCHAR
    };


    public StringConverter(int sqlType) {
        super(String.class, sqlType);
    }

    @Override
    public void setValue(LSql lSql, PreparedStatement ps, int index, Object val) throws SQLException {
        ps.setString(index, val.toString());

    }

    @Override
    public Object getValue(LSql lSql, ResultSet rs, int index) throws SQLException {
        return rs.getString(index);
    }
}
