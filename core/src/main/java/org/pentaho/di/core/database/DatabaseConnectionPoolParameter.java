/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.database;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;

public class DatabaseConnectionPoolParameter {
  private String parameter;
  private String defaultValue;
  private String description;

  public DatabaseConnectionPoolParameter() {
  }

  /**
   * @param parameter
   * @param defaultValue
   * @param description
   */
  public DatabaseConnectionPoolParameter( String parameter, String defaultValue, String description ) {
    this();
    this.parameter = parameter;
    this.defaultValue = defaultValue;
    this.description = description;
  }

  /**
   * @return the defaultValue
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * @param defaultValue
   *          the defaultValue to set
   */
  public void setDefaultValue( String defaultValue ) {
    this.defaultValue = defaultValue;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  /**
   * @return the parameter
   */
  public String getParameter() {
    return parameter;
  }

  /**
   * @param parameter
   *          the parameter to set
   */
  public void setParameter( String parameter ) {
    this.parameter = parameter;
  }

  public static final String[] getParameterNames( DatabaseConnectionPoolParameter[] poolParameters ) {
    String[] names = new String[poolParameters.length];
    for ( int i = 0; i < names.length; i++ ) {
      names[i] = poolParameters[i].getParameter();
    }
    return names;
  }

  public static final DatabaseConnectionPoolParameter findParameter( String parameterName,
    DatabaseConnectionPoolParameter[] poolParameters ) {
    for ( int i = 0; i < poolParameters.length; i++ ) {
      if ( poolParameters[i].getParameter().equalsIgnoreCase( parameterName ) ) {
        return poolParameters[i];
      }
    }
    return null;
  }

  public static final List<RowMetaAndData> getRowList( DatabaseConnectionPoolParameter[] poolParameters,
    String titleParameter, String titleDefaultValue, String titleDescription ) {
    RowMetaInterface rowMeta = new RowMeta();

    rowMeta.addValueMeta( new ValueMetaString( titleParameter ) );
    rowMeta.addValueMeta( new ValueMetaString( titleDefaultValue ) );
    rowMeta.addValueMeta( new ValueMetaString( titleDescription ) );

    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    for ( int i = 0; i < poolParameters.length; i++ ) {
      DatabaseConnectionPoolParameter p = poolParameters[i];

      Object[] row = new Object[rowMeta.size()];
      row[0] = p.getParameter();
      row[1] = p.getDefaultValue();
      row[2] = p.getDescription();

      list.add( new RowMetaAndData( rowMeta, row ) );
    }

    return list;
  }
}
