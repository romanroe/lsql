package com.w11k.lsql.converter.types;

import com.w11k.lsql.LSql;
import com.w11k.lsql.converter.Converter;

import java.sql.*;

public class BlobConverter extends Converter {

    public BlobConverter() {
        super(com.w11k.lsql.Blob.class, Types.BLOB);
    }

    @Override
    public void setValue(LSql lSql, PreparedStatement ps, int index, Object val) throws SQLException {
        com.w11k.lsql.Blob blob = (com.w11k.lsql.Blob) val;
        ps.setBlob(index, blob.getInputStream());
    }

    @Override
    public Object getValue(LSql lSql, ResultSet rs, int index) throws SQLException {
        Blob blob = rs.getBlob(index);
        return new com.w11k.lsql.Blob(blob.getBinaryStream());
    }
}
