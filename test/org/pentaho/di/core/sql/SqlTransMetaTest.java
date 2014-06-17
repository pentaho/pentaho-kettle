/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.sql;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.jdbc.TransDataService;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.sql.SqlTransExecutor;
import org.pentaho.di.trans.step.RowAdapter;

public class SqlTransMetaTest extends TestCase {

  private List<TransDataService> getServices() {
    List<TransDataService> services = new ArrayList<TransDataService>();
    TransDataService service =
      new TransDataService( "Service", "testfiles/sql-transmeta-test-data.ktr", null, "Output" );
    services.add( service );
    return services;
  }

  public void test01_BasicSelectFrom() throws Exception {
    KettleEnvironment.init();

    String sqlQuery = "SELECT * FROM Service";

    SqlTransExecutor executor = new SqlTransExecutor( sqlQuery, getServices() );

    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();

    // print the eventual result rows...
    //
    executor.executeQuery( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( new RowMetaAndData( rowMeta, row ) );
      }
    } );

    // Now the generated transformation is waiting for input so we
    // can start the service transformation
    //
    executor.waitUntilFinished();

    assertEquals( 8, rows.size() );
    RowMetaAndData row = rows.get( 0 );
    assertEquals( 4, row.size() );
  }

  public void test02_SelectFrom() throws Exception {
    KettleEnvironment.init();

    String sqlQuery = "SELECT Category, Country, products_sold as nr, sales_amount as sales FROM Service";

    SqlTransExecutor executor = new SqlTransExecutor( sqlQuery, getServices() );

    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();

    // print the eventual result rows...
    //
    executor.executeQuery( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( new RowMetaAndData( rowMeta, row ) );
      }
    } );

    // Now the generated transformation is waiting for input so we
    // can start the service transformation
    //
    executor.waitUntilFinished();

    assertEquals( 8, rows.size() );
  }

  public void test03_SelectFromWhere() throws Exception {
    KettleEnvironment.init();

    String sqlQuery =
      "SELECT Category, Country, products_sold as nr, sales_amount as sales FROM Service WHERE Category = 'A'";

    SqlTransExecutor executor = new SqlTransExecutor( sqlQuery, getServices() );

    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();

    // print the eventual result rows...
    //
    executor.executeQuery( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( new RowMetaAndData( rowMeta, row ) );
      }
    } );

    // Now the generated transformation is waiting for input so we
    // can start the service transformation
    //
    executor.waitUntilFinished();

    // Save to temp file for checking
    //
    File file = new File( "/tmp/gen.ktr" );
    FileOutputStream fos = new FileOutputStream( file );
    fos.write( org.pentaho.di.core.xml.XMLHandler.getXMLHeader().getBytes( "UTF-8" ) );
    fos.write( executor.getGenTransMeta().getXML().getBytes( "UTF-8" ) );
    fos.close();

    assertEquals( 4, rows.size() );

  }

  public void test04_SelectFromWhereGroupBy() throws Exception {
    KettleEnvironment.init();

    String sqlQuery =
      "SELECT Country, SUM(products_sold) as count, SUM(sales_amount) as sales "
        + "FROM Service WHERE Category = 'A' GROUP BY Country";

    SqlTransExecutor executor = new SqlTransExecutor( sqlQuery, getServices() );

    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();

    // print the eventual result rows...
    //
    executor.executeQuery( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( new RowMetaAndData( rowMeta, row ) );
      }
    } );

    // Now the generated transformation is waiting for input so we
    // can start the service transformation
    //
    executor.waitUntilFinished();

    assertEquals( 4, rows.size() );
  }

  public void test05_SelectFromGroupBy() throws Exception {
    KettleEnvironment.init();

    String sqlQuery =
      "SELECT Country, SUM(products_sold) as count, SUM(sales_amount) as sales FROM Service GROUP BY Country";

    SqlTransExecutor executor = new SqlTransExecutor( sqlQuery, getServices() );

    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();

    // collect the eventual result rows...
    //
    executor.executeQuery( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( new RowMetaAndData( rowMeta, row ) );
      }
    } );

    // Now the generated transformation is waiting for input so we
    // can start the service transformation
    //
    executor.waitUntilFinished();

    assertEquals( 4, rows.size() );
  }

  public void test06_SelectFromGroupByOrderBy() throws Exception {
    KettleEnvironment.init();

    String sqlQuery =
      "SELECT Country, SUM(products_sold) as count, SUM(sales_amount) as sales "
        + "FROM Service GROUP BY Country ORDER BY SUM(sales_amount) DESC";

    SqlTransExecutor executor = new SqlTransExecutor( sqlQuery, getServices() );

    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();

    // collect the eventual result rows...
    //
    executor.executeQuery( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( new RowMetaAndData( rowMeta, row ) );
      }
    } );

    // Now the generated transformation is waiting for input so we
    // can start the service transformation
    //
    executor.waitUntilFinished();

    assertEquals( 4, rows.size() );

    // Validate results...
    //
    int rowNr = 0;
    assertEquals( "Germany", rows.get( rowNr ).getString( "Country", null ) );
    assertEquals( 76, rows.get( rowNr ).getInteger( "count", -1 ) );
    assertEquals( 12697, Math.round( rows.get( rowNr ).getNumber( "sales", -1.0 ) ) );

    rowNr++;
    assertEquals( "Great Britain", rows.get( rowNr ).getString( "Country", null ) );
    assertEquals( 18, rows.get( rowNr ).getInteger( "count", -1 ) );
    assertEquals( 11657, Math.round( rows.get( rowNr ).getNumber( "sales", -1.0 ) ) );

    rowNr++;
    assertEquals( "France", rows.get( rowNr ).getString( "Country", null ) );
    assertEquals( 24, rows.get( rowNr ).getInteger( "count", -1 ) );
    assertEquals( 2021, Math.round( rows.get( rowNr ).getNumber( "sales", -1.0 ) ) );

    rowNr++;
    assertEquals( "Belgium", rows.get( rowNr ).getString( "Country", null ) );
    assertEquals( 10, rows.get( rowNr ).getInteger( "count", -1 ) );
    assertEquals( 780, Math.round( rows.get( rowNr ).getNumber( "sales", -1.0 ) ) );

  }

  public void test07_SelectFromGroupCountDistinct() throws Exception {
    KettleEnvironment.init();

    String sqlQuery =
      "SELECT Category, COUNT(DISTINCT Country) as \"Number of countries\" "
        + "FROM Service GROUP BY Category ORDER BY COUNT(DISTINCT Country) DESC";

    SqlTransExecutor executor = new SqlTransExecutor( sqlQuery, getServices() );

    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();

    // collect the eventual result rows...
    //
    executor.executeQuery( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( new RowMetaAndData( rowMeta, row ) );
      }
    } );

    // Now the generated transformation is waiting for input so we
    // can start the service transformation
    //
    executor.waitUntilFinished();

    assertEquals( 2, rows.size() );

    // Validate results...
    //
    int rowNr = 0;
    assertEquals( "A", rows.get( rowNr ).getString( "Category", null ) );
    assertEquals( 4, rows.get( rowNr ).getInteger( "Number of countries", -1 ) );

    rowNr++;
    assertEquals( "B", rows.get( rowNr ).getString( "Category", null ) );
    assertEquals( 4, rows.get( rowNr ).getInteger( "Number of countries", -1 ) );
  }

  public void test08_SelectFromGroupCountMinMax() throws Exception {
    KettleEnvironment.init();

    String sqlQuery =
      "SELECT Country, COUNT(Category) as \"Number of categories\", "
        + "MIN( sales_amount) min_sales, MAX( products_sold) max_count "
        + "FROM Service GROUP BY Country ORDER BY COUNT(Category) ASC";

    SqlTransExecutor executor = new SqlTransExecutor( sqlQuery, getServices() );

    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();

    // collect the eventual result rows...
    //
    executor.executeQuery( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( new RowMetaAndData( rowMeta, row ) );
      }
    } );

    // Save to temp file for checking
    //
    File file = new File( "/tmp/gen.ktr" );
    FileOutputStream fos = new FileOutputStream( file );
    fos.write( org.pentaho.di.core.xml.XMLHandler.getXMLHeader().getBytes( "UTF-8" ) );
    fos.write( executor.getGenTransMeta().getXML().getBytes( "UTF-8" ) );
    fos.close();

    // Now the generated transformation is waiting for input so we
    // can start the service transformation
    //
    executor.waitUntilFinished();

    assertEquals( 4, rows.size() );

    // Validate results...
    //
    int rowNr = 0;
    assertEquals( "France", rows.get( rowNr ).getString( "Country", null ) );
    assertEquals( 2, rows.get( rowNr ).getInteger( "Number of categories", -1 ) );
    assertEquals( 724, Math.round( rows.get( rowNr ).getNumber( "min_sales", -1.0 ) ) );
    assertEquals( 12, Math.round( rows.get( rowNr ).getNumber( "max_count", -1.0 ) ) );

    rowNr++;
    assertEquals( "Belgium", rows.get( rowNr ).getString( "Country", null ) );
    assertEquals( 2, rows.get( rowNr ).getInteger( "Number of categories", -1 ) );
    assertEquals( 257, Math.round( rows.get( rowNr ).getNumber( "min_sales", -1.0 ) ) );
    assertEquals( 5, Math.round( rows.get( rowNr ).getNumber( "max_count", -1.0 ) ) );

    rowNr++;
    assertEquals( "Germany", rows.get( rowNr ).getString( "Country", null ) );
    assertEquals( 2, rows.get( rowNr ).getInteger( "Number of categories", -1 ) );
    assertEquals( 2864, Math.round( rows.get( rowNr ).getNumber( "min_sales", -1.0 ) ) );
    assertEquals( 38, Math.round( rows.get( rowNr ).getNumber( "max_count", -1.0 ) ) );

    rowNr++;
    assertEquals( "Great Britain", rows.get( rowNr ).getString( "Country", null ) );
    assertEquals( 2, rows.get( rowNr ).getInteger( "Number of categories", -1 ) );
    assertEquals( 4298, Math.round( rows.get( rowNr ).getNumber( "min_sales", -1.0 ) ) );
    assertEquals( 9, Math.round( rows.get( rowNr ).getNumber( "max_count", -1.0 ) ) );
  }

  public void test09_SelectFromGroupMaxHaving() throws Exception {
    KettleEnvironment.init();

    String sqlQuery =
      "SELECT Country, MAX(products_sold) max_count "
        + "FROM Service GROUP BY Country HAVING MAX(products_sold) > 10 ORDER BY max_count DESC";

    SqlTransExecutor executor = new SqlTransExecutor( sqlQuery, getServices() );

    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();

    // collect the eventual result rows...
    //
    executor.executeQuery( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( new RowMetaAndData( rowMeta, row ) );
      }
    } );

    // Save to temp file for checking
    //
    File file = new File( "/tmp/gen.ktr" );
    FileOutputStream fos = new FileOutputStream( file );
    fos.write( org.pentaho.di.core.xml.XMLHandler.getXMLHeader().getBytes( "UTF-8" ) );
    fos.write( executor.getGenTransMeta().getXML().getBytes( "UTF-8" ) );
    fos.close();

    // Now the generated transformation is waiting for input so we
    // can start the service transformation
    //
    executor.waitUntilFinished();

    assertEquals( 2, rows.size() );

    // Validate results...
    //
    int rowNr = 0;
    assertEquals( "Germany", rows.get( rowNr ).getString( "Country", null ) );
    assertEquals( 38, Math.round( rows.get( rowNr ).getNumber( "max_count", -1.0 ) ) );

    rowNr++;
    assertEquals( "France", rows.get( rowNr ).getString( "Country", null ) );
    assertEquals( 12, Math.round( rows.get( rowNr ).getNumber( "max_count", -1.0 ) ) );
  }

  /*
   *
   * SELECT Category, SUM(sales_amount) as "Sales" FROM Service GROUP BY Category ORDER BY SUM(sales_amount) DESC
   */

  public void test10_SelectFromGroupSumAlias() throws Exception {
    KettleEnvironment.init();

    String sqlQuery =
      "SELECT   Category, SUM(sales_amount) as \"Sales\"\n"
        + "FROM     Service\nGROUP BY Category\nORDER BY SUM(sales_amount) DESC";

    SqlTransExecutor executor = new SqlTransExecutor( sqlQuery, getServices() );

    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();

    // collect the eventual result rows...
    //
    executor.executeQuery( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( new RowMetaAndData( rowMeta, row ) );
      }
    } );

    // Save to temp file for checking
    //
    File file = new File( "/tmp/gen.ktr" );
    FileOutputStream fos = new FileOutputStream( file );
    fos.write( org.pentaho.di.core.xml.XMLHandler.getXMLHeader().getBytes( "UTF-8" ) );
    fos.write( executor.getGenTransMeta().getXML().getBytes( "UTF-8" ) );
    fos.close();

    // Now the generated transformation is waiting for input so we
    // can start the service transformation
    //
    executor.waitUntilFinished();

    assertEquals( 2, rows.size() );

    // Validate results...
    //
    int rowNr = 0;
    assertEquals( "A", rows.get( rowNr ).getString( "Category", null ) );
    assertEquals( 19013, Math.round( rows.get( rowNr ).getNumber( "Sales", -1.0 ) ) );

    rowNr++;
    assertEquals( "B", rows.get( rowNr ).getString( "Category", null ) );
    assertEquals( 8142, Math.round( rows.get( rowNr ).getNumber( "Sales", -1.0 ) ) );
  }

  public void test11_NotSelectedHaving() throws Exception {
    KettleEnvironment.init();

    String sqlQuery =
      "select \"Service\".\"Category\" as \"c0\", \"Service\".\"Country\" as \"c1\" "
        + "from \"Service\" as \"Service\" where (\"Service\".\"Category\" = 'A') "
        + "group by \"Service\".\"Category\", \"Service\".\"Country\" "
        + "having (NOT((sum(\"Service\".\"sales_amount\") is null)) "
        + "OR NOT((sum(\"Service\".\"products_sold\") is null)) ) "
        + "order by CASE WHEN \"Service\".\"Category\" IS NULL THEN 1 ELSE 0 END, \"Service\".\"Category\" ASC, "
        + "CASE WHEN \"Service\".\"Country\" IS NULL THEN 1 ELSE 0 END";

    SqlTransExecutor executor = new SqlTransExecutor( sqlQuery, getServices() );

    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();

    // collect the eventual result rows...
    //
    executor.executeQuery( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( new RowMetaAndData( rowMeta, row ) );
      }
    } );

    // Save to temp file for checking
    //
    File file = new File( "/tmp/gen.ktr" );
    FileOutputStream fos = new FileOutputStream( file );
    fos.write( org.pentaho.di.core.xml.XMLHandler.getXMLHeader().getBytes( "UTF-8" ) );
    fos.write( executor.getGenTransMeta().getXML().getBytes( "UTF-8" ) );
    fos.close();

    // Now the generated transformation is waiting for input so we
    // can start the service transformation
    //
    executor.waitUntilFinished();

    assertEquals( 4, rows.size() );

  }

  //

  public void test12_DistinctFromPMD() throws Exception {
    KettleEnvironment.init();

    String sqlQuery =
      "SELECT DISTINCT            BT_SERVICE_SERVICE.Country AS COL0 FROM            Service BT_SERVICE_SERVICE";

    SqlTransExecutor executor = new SqlTransExecutor( sqlQuery, getServices() );

    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();

    // collect the eventual result rows...
    //
    executor.executeQuery( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( new RowMetaAndData( rowMeta, row ) );
      }
    } );

    // Save to temp file for checking
    //
    File file = new File( "/tmp/gen.ktr" );
    FileOutputStream fos = new FileOutputStream( file );
    fos.write( org.pentaho.di.core.xml.XMLHandler.getXMLHeader().getBytes( "UTF-8" ) );
    fos.write( executor.getGenTransMeta().getXML().getBytes( "UTF-8" ) );
    fos.close();

    // Now the generated transformation is waiting for input so we
    // can start the service transformation
    //
    executor.waitUntilFinished();

    assertEquals( 4, rows.size() );

  }

  public void test13_DistinctOrderFromPMD() throws Exception {
    KettleEnvironment.init();

    String sqlQuery =
      "SELECT DISTINCT            BT_SERVICE_SERVICE.Category AS COL0          ,BT_SERVICE_SERVICE.Country AS COL1 "
        + "FROM            Service BT_SERVICE_SERVICE ORDER BY            COL0";

    SqlTransExecutor executor = new SqlTransExecutor( sqlQuery, getServices() );

    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();

    // collect the eventual result rows...
    //
    executor.executeQuery( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( new RowMetaAndData( rowMeta, row ) );
      }
    } );

    // Save to temp file for checking
    //
    File file = new File( "/tmp/gen.ktr" );
    FileOutputStream fos = new FileOutputStream( file );
    fos.write( org.pentaho.di.core.xml.XMLHandler.getXMLHeader().getBytes( "UTF-8" ) );
    fos.write( executor.getGenTransMeta().getXML().getBytes( "UTF-8" ) );
    fos.close();

    // Now the generated transformation is waiting for input so we
    // can start the service transformation
    //
    executor.waitUntilFinished();

    assertEquals( 8, rows.size() );

  }

}
