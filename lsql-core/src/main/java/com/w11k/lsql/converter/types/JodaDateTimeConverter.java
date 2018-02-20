package com.w11k.lsql.converter.types;

import com.w11k.lsql.LSql;
import com.w11k.lsql.converter.Converter;
import org.joda.time.DateTime;

import java.sql.*;

public class JodaDateTimeConverter extends Converter {

    public JodaDateTimeConverter() {
        super(DateTime.class, Types.TIMESTAMP);
    }

    @Override
    public void setValue(LSql lSql, PreparedStatement ps, int index, Object val) throws SQLException {
        DateTime dt = (DateTime) val;
        Timestamp ts = new Timestamp(dt.getMillis());
        ps.setTimestamp(index, ts);
    }

    @Override
    public Object getValue(LSql lSql, ResultSet rs, int index) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(index);
        if (timestamp != null) {
            return new DateTime(timestamp.getTime());
        } else {
            return null;
        }
    }
}
