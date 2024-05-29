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

package org.pentaho.di.ui.trans.steps.googleanalytics;

import static org.mockito.Mockito.*;

import com.google.api.services.analytics.model.GaData;
import com.google.api.services.analytics.model.GaData.ColumnHeaders;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.google.api.services.analytics.Analytics;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.googleanalytics.GaInputStepMeta;
import org.pentaho.di.ui.core.widget.TableView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Sakun
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class GaInputStepDialogTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Mock
  Analytics.Data.Ga.Get query;
  @Mock
  TableItem tableItem;
  @Mock
  Table table;
  @Mock
  TableView tableView;
  @Mock
  GaInputStepDialog dialog;
  List<ColumnHeaders> headers;

  @Before
  public void setup() throws IOException, KettlePluginException {
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.init();

    GaData gaData = new GaData();
    headers = new ArrayList<>();
    headers.add( createColumnHeader( "DIMENSION", "ga:date", null ) );
    headers.add( createColumnHeader( "DIMENSION", "ga:daysSinceLastVisit", null ) );
    headers.add( createColumnHeader( "DIMENSION", "ga:visitLength", null ) );
    headers.add( createColumnHeader( "DIMENSION", "ga:visitCount", null ) );
    headers.add( createColumnHeader( "DIMENSION", "ga:latitude", null ) );
    headers.add( createColumnHeader( "DIMENSION", "ga:longitude", null ) );
    headers.add( createColumnHeader( "DIMENSION", "ga:other", null ) );

    headers.add( createColumnHeader( "METRIC", "currency", "currency" ) );
    headers.add( createColumnHeader( "METRIC", "float", "float" ) );
    headers.add( createColumnHeader( "METRIC", "percent", "percent" ) );
    headers.add( createColumnHeader( "METRIC", "us_currency", "us_currency" ) );
    headers.add( createColumnHeader( "METRIC", "time", "time" ) );
    headers.add( createColumnHeader( "METRIC", "integer", "integer" ) );
    headers.add( createColumnHeader( "METRIC", "other", "other" ) );

    gaData.setColumnHeaders( headers );

    gaData.setProfileInfo( new GaData.ProfileInfo() );

    List<List<String>> data = new ArrayList<>();
    data.add( new ArrayList<String>() );

    gaData.setRows( data );
    doReturn( gaData ).when( query ).execute();
    doReturn( tableItem ).when( table ).getItem( anyInt() );
    tableView.table = table;
    doReturn( tableView ).when( dialog ).getTableView();
    doCallRealMethod().when( dialog ).getFields();
    doReturn( query ).when( dialog ).getPreviewQuery();
    doReturn( mock( GaInputStepMeta.class ) ).when( dialog ).getInput();
  }

  @Test
  public void testGetFields() throws Exception {
    dialog.getFields();
    // Verify we do not read more data that needed
    verify( query ).setMaxResults( 1 );

    // Verify we're reading fields correctly and filling that info into table
    verify( table, times( 19 ) ).getItem( anyInt() );
    verify( tableItem, times( 7 ) ).setText( 1, GaInputStepMeta.FIELD_TYPE_DIMENSION );
    verify( tableItem, times( 7 ) ).setText( 1, GaInputStepMeta.FIELD_TYPE_METRIC );
    verify( tableItem, times( 3 ) ).setText( 1, GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_PROPERTY );
    verify( tableItem, times( 2 ) ).setText( 1, GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_FIELD );

    for ( ColumnHeaders header : headers ) {
      verify( tableItem, times( 1 ) ).setText( 2, header.getName() );
      verify( tableItem, times( 1 ) ).setText( 3, header.getName() );
    }
    verify( tableItem, times( 1 ) ).setText( 2, GaInputStepMeta.PROPERTY_DATA_SOURCE_PROFILE_ID );
    verify( tableItem, times( 1 ) ).setText( 3, GaInputStepMeta.PROPERTY_DATA_SOURCE_PROFILE_ID );
    verify( tableItem, times( 1 ) ).setText( 2, GaInputStepMeta.PROPERTY_DATA_SOURCE_WEBPROP_ID );
    verify( tableItem, times( 1 ) ).setText( 3, GaInputStepMeta.PROPERTY_DATA_SOURCE_WEBPROP_ID );
    verify( tableItem, times( 1 ) ).setText( 2, GaInputStepMeta.PROPERTY_DATA_SOURCE_ACCOUNT_NAME );
    verify( tableItem, times( 1 ) ).setText( 3, GaInputStepMeta.PROPERTY_DATA_SOURCE_ACCOUNT_NAME );
    verify( tableItem, times( 1 ) ).setText( 2, GaInputStepMeta.FIELD_DATA_SOURCE_TABLE_ID );
    verify( tableItem, times( 1 ) ).setText( 3, GaInputStepMeta.FIELD_DATA_SOURCE_TABLE_ID );
    verify( tableItem, times( 1 ) ).setText( 2, GaInputStepMeta.FIELD_DATA_SOURCE_TABLE_NAME );
    verify( tableItem, times( 1 ) ).setText( 3, GaInputStepMeta.FIELD_DATA_SOURCE_TABLE_NAME );

    verify( tableItem, times( 1 ) ).setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_DATE ) );
    verify( tableItem, times( 5 ) ).setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_INTEGER ) );
    verify( tableItem, times( 6 ) ).setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_NUMBER ) );
    verify( tableItem, times( 7 ) ).setText( 4, ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_STRING ) );
  }

  private ColumnHeaders createColumnHeader( String type, String name, String dataType ) {
    return new ColumnHeaders().setColumnType( type ).setName( name ).setDataType( dataType );
  }
}
