/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.mysqlbulkloader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MySQLBulkLoaderTest {

  @Test
  public void testFieldFormatType() throws KettleXMLException {
    MySQLBulkLoaderMeta lm = new MySQLBulkLoaderMeta();
    Document document = XMLHandler.loadXMLFile( this.getClass().getResourceAsStream( "step.xml" ) );
    IMetaStore metastore = null;
    Node stepNode = (Node) document.getDocumentElement();
    lm.loadXML( stepNode, Collections.EMPTY_LIST, metastore );
    int[] codes = lm.getFieldFormatType();
    Assert.assertEquals( 3, codes[0] );
    Assert.assertEquals( 4, codes[1] );
  }

  @Test
  public void testEscapeCharacters() throws KettleException, IOException {
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.init( true );

    MySQLBulkLoader loader;
    MySQLBulkLoaderData ld = new MySQLBulkLoaderData();
    MySQLBulkLoaderMeta lm = new MySQLBulkLoaderMeta();

    TransMeta transMeta = new TransMeta();
    transMeta.setName( "loader" );

    PluginRegistry plugReg = PluginRegistry.getInstance();

    String loaderPid = plugReg.getPluginId( StepPluginType.class, lm );
    StepMeta stepMeta = new StepMeta( loaderPid, "loader", lm );
    Trans trans = new Trans( transMeta );
    transMeta.addStep( stepMeta );
    trans.setRunning( true );

    loader = Mockito.spy( new MySQLBulkLoader( stepMeta, ld, 1, transMeta, trans ) );

    RowMeta rm = new RowMeta();
    ValueMetaString vm = new ValueMetaString( "I don't want NPE!" );
    rm.addValueMeta( vm );
    RowMeta spyRowMeta = Mockito.spy( new RowMeta() );
    Mockito.when( spyRowMeta.getValueMeta( Mockito.anyInt() ) ).thenReturn( vm );
    loader.setInputRowMeta( spyRowMeta );

    MySQLBulkLoaderMeta smi = new MySQLBulkLoaderMeta();
    smi.setFieldStream( new String[] { "Test" } );
    smi.setFieldFormatType( new int[] { MySQLBulkLoaderMeta.FIELD_FORMAT_TYPE_STRING_ESCAPE } );
    smi.setEscapeChar( "\\" );
    smi.setEnclosure( "\"" );
    smi.setDatabaseMeta( Mockito.mock( DatabaseMeta.class ) );

    MySQLBulkLoaderData sdi = new MySQLBulkLoaderData();
    sdi.keynrs = new int[1];
    sdi.keynrs[0] = 0;
    sdi.fifoStream = Mockito.mock( OutputStream.class );
    loader.init( smi, sdi );
    loader.first = false;

    Mockito.when( loader.getRow() ).thenReturn( new String[] { "test\"Escape\\" } );
    loader.processRow( smi, sdi );
    Mockito.verify( sdi.fifoStream, Mockito.times( 1 ) ).write( "test\\\"Escape\\\\".getBytes() );
  }
}
