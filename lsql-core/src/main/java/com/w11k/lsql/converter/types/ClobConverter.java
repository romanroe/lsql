package com.w11k.lsql.converter.types;

import com.google.common.io.CharStreams;
import com.w11k.lsql.LSql;
import com.w11k.lsql.converter.Converter;

import javax.sql.rowset.serial.SerialClob;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;

public class ClobConverter extends Converter {

    public ClobConverter() {
        super(String.class, Types.CLOB);
    }

    @Override
    public void setValue(LSql lSql, PreparedStatement ps, int index, Object val) throws SQLException {
        ps.setClob(index, new SerialClob(val.toString().toCharArray()));

    }

    @Override
    public Object getValue(LSql lSql, ResultSet rs, int index) throws SQLException {
        Clob clob = rs.getClob(index);
        if (clob != null) {
            Reader reader = clob.getCharacterStream();
            try {
                return CharStreams.toString(reader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }
}
