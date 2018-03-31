/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.olapinput;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.trans.steps.olapinput.olap4jhelper.AbstractBaseCell;
import org.pentaho.di.trans.steps.olapinput.olap4jhelper.CellDataSet;
import org.pentaho.di.trans.steps.olapinput.olap4jhelper.OlapUtil;

/**
 * <code>Olap4j Helper class</code> ...
 *
 * @author Paul Stoellberger
 * @since Mar 12, 2010
 */
public class OlapHelper {

  private String olap4jDriver;
  private String olap4jUrl;
  private String catalogName;
  private String mdx;
  private String username;
  private String password;

  private CellSet result;

  private String[] headerValues = null;
  private String[][] cellValues = null;
  private OlapConnection olapConnection;

  public OlapHelper( String olap4jDriver, String olap4jUrl, String username, String password, String catalogName,
    String mdx ) {
    this.olap4jDriver = olap4jDriver;
    this.olap4jUrl = "jdbc:xmla:Server=" + olap4jUrl;
    this.catalogName = catalogName;
    this.mdx = mdx;
    this.username = username;
    this.password = password;
  }

  public void openQuery() throws Exception {

    Class.forName( olap4jDriver );
    OlapConnection connection = null;

    if ( Utils.isEmpty( username ) && Utils.isEmpty( password ) ) {
      connection = (OlapConnection) DriverManager.getConnection( olap4jUrl );
    } else {
      connection = (OlapConnection) DriverManager.getConnection( olap4jUrl, username, password );
    }

    OlapWrapper wrapper = connection;
    olapConnection = wrapper.unwrap( OlapConnection.class );

    try {
      if ( !Utils.isEmpty( catalogName ) ) {
        olapConnection.setCatalog( catalogName );
      }
    } catch ( SQLException e ) {
      throw new OlapException( "Error setting catalog for MDX statement: '" + catalogName + "'" );
    }

    OlapStatement stmt = olapConnection.createStatement();

    if ( !Utils.isEmpty( mdx ) ) {
      CellSet tmp = stmt.executeOlapQuery( mdx );
      result = tmp;
    } else {
      throw new Exception( "Error executing empty MDX query" );
    }

  }

  public void close() throws KettleDatabaseException {
    try {
      if ( result != null ) {
        result.close();
      }
      if ( olapConnection != null ) {
        olapConnection.close();
      }
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Error closing connection" );
    }
  }

  /**
   * Outputs one row per tuple on the rows axis.
   *
   * @throws KettleDatabaseException
   *           in case some or other error occurs
   */
  public void createRectangularOutput() throws KettleDatabaseException {
    if ( result != null ) {
      CellDataSet cs = OlapUtil.cellSet2Matrix( result );
      AbstractBaseCell[][] headers = cs.getCellSetHeaders();
      headerValues = concatHeader( headers );
      cellValues = castResult( cs.getCellSetBody() );
    }
  }

  private static String[][] castResult( AbstractBaseCell[][] cellset ) {
    String[][] result = new String[cellset.length][];

    for ( int i = 0; i < cellset.length; i++ ) {
      String[] row = new String[cellset[i].length];
      for ( int k = 0; k < cellset[i].length; k++ ) {
        String value = cellset[i][k].getFormattedValue();
        if ( value == null || value.equals( "" ) || value.equals( "null" ) ) {
          value = "";
        }
        row[k] = value;
      }
      result[i] = row;

    }
    return result;
  }

  private static String[] concatHeader( AbstractBaseCell[][] cellset ) {
    if ( cellset.length > 0 ) {
      String[] row = new String[cellset[0].length];
      for ( int k = 0; k < cellset[0].length; k++ ) {
        String header = "";
        for ( int i = 0; i < cellset.length; i++ ) {
          String value = cellset[i][k].getFormattedValue();
          if ( value == null || value.equals( "" ) || value.equals( "null" ) ) {
            value = cellset[i][k].getRawValue();
            if ( value == null || value.equals( "" ) || value.equals( "null" ) ) {
              value = "";
            }
          }
          if ( value.length() > 0 ) {
            if ( i > 0 ) {
              header = header + ".";
            }
            header = header + "[" + value + "]";
          }
        }
        if ( Utils.isEmpty( header ) ) {
          header = "Column" + k;
        }
        row[k] = header;
      }
      return row;
    }
    return null;
  }

  public String[][] getRows() {
    return cellValues;
  }

  public String[] getHeaderValues() {
    return headerValues;
  }

  public String[][] getCellValues() {
    return cellValues;
  }

}
