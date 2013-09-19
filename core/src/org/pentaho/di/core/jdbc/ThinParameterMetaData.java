package org.pentaho.di.core.jdbc;

import java.sql.ParameterMetaData;
import java.sql.SQLException;

public class ThinParameterMetaData implements ParameterMetaData {
  
  private ThinPreparedStatement ps;
  
  public ThinParameterMetaData(ThinPreparedStatement preparedStatement) {
    this.ps = preparedStatement; 
  }

  @Override
  public String getParameterClassName(int param) throws SQLException {
    return null;
  }

  @Override
  public int getParameterCount() throws SQLException {
    return ps.getParamMeta().length;
  }

  @Override
  public int getParameterMode(int param) throws SQLException {
    return ParameterMetaData.parameterModeIn;
  }

  @Override
  public int getParameterType(int param) throws SQLException {
    return ThinUtil.getSqlType(ps.getParamMeta()[param-1]);
  }

  @Override
  public String getParameterTypeName(int param) throws SQLException {
    return ThinUtil.getSqlTypeDesc(ps.getParamMeta()[param-1]);
  }

  @Override
  public int getPrecision(int param) throws SQLException {
    return ps.getParamMeta()[param-1].getLength();
  }

  @Override
  public int getScale(int param) throws SQLException {
    return ps.getParamMeta()[param-1].getPrecision();
  }

  @Override
  public int isNullable(int param) throws SQLException {
    return ParameterMetaData.parameterNullableUnknown;
  }

  @Override
  public boolean isSigned(int param) throws SQLException {
    return ps.getParamMeta()[param-1].isNumeric();
  }



  @Override
  public boolean isWrapperFor(Class<?> arg0) throws SQLException {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> arg0) throws SQLException {
    return null;
  }
}
