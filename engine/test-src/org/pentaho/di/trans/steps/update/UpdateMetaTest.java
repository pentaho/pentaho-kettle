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

package org.pentaho.di.trans.steps.update;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;

public class UpdateMetaTest extends TestCase {

  private StepMeta stepMeta;
  private Update upd;
  private UpdateData ud;
  private UpdateMeta umi;

  public static final String databaseXML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<connection>" + "<name>lookup</name>" + "<server>127.0.0.1</server>" + "<type>H2</type>"
        + "<access>Native</access>" + "<database>mem:db</database>" + "<port></port>" + "<username>sa</username>"
        + "<password></password>" + "</connection>";


  @Before
  protected void setUp() throws KettleException {
    KettleEnvironment.init();
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "delete1" );

    Map<String, String> vars = new HashMap<String, String>();
    vars.put( "max.sz", "10" );
    transMeta.injectVariables( vars );

    umi = new UpdateMeta();
    ud = new UpdateData();

    PluginRegistry plugReg = PluginRegistry.getInstance();
    String deletePid = plugReg.getPluginId( StepPluginType.class, umi );

    stepMeta = new StepMeta( deletePid, "delete", umi );
    Trans trans = new Trans( transMeta );
    transMeta.addStep( stepMeta );
    upd = new Update( stepMeta, ud, 1, transMeta, trans );
    upd.copyVariablesFrom( transMeta );
  }

  @Test
  public void testCommitCountFixed() {
    umi.setCommitSize( "100" );
    assertTrue( umi.getCommitSize( upd ) == 100 );
  }

  @Test
  public void testCommitCountVar() {
    umi.setCommitSize( "${max.sz}" );
    assertTrue( umi.getCommitSize( upd ) == 10 );
  }

  @Test
  public void testCommitCountMissedVar() {
    umi.setCommitSize( "missed-var" );
    try {
      umi.getCommitSize( upd );
      fail();
    } catch ( Exception ex ) {
    }
  }

  @Test
  public void testUseDefaultSchemaName() throws Exception {
    String schemaName = "";
    String tableName = "tableName";
    String schemaTable = "default.tableName";

    DatabaseMeta databaseMeta = spy( new DatabaseMeta( databaseXML ) {
      public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc ) {
        return "someValue";
      }
    } );
    when( databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName ) ).thenReturn( schemaTable );

    ValueMetaInterface valueMeta = mock( ValueMetaInterface.class );
    when( valueMeta.clone() ).thenReturn( mock( ValueMetaInterface.class ) );
    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    when( rowMetaInterface.size() ).thenReturn( 1 );
    when( rowMetaInterface.searchValueMeta( anyString() ) ).thenReturn( valueMeta );

    UpdateMeta updateMeta = new UpdateMeta();
    updateMeta.setDatabaseMeta( databaseMeta );
    updateMeta.setTableName( tableName );
    updateMeta.setSchemaName( schemaName );
    updateMeta.setKeyLookup( new String[] { "KeyLookup1", "KeyLookup2" } );
    updateMeta.setKeyStream( new String[] { "KeyStream1", "KeyStream2" } );
    updateMeta.setUpdateLookup( new String[] { "updateLookup1", "updateLookup2" } );
    updateMeta.setUpdateStream( new String[] { "UpdateStream1", "UpdateStream2" } );

    SQLStatement sqlStatement =
        updateMeta.getSQLStatements( new TransMeta(), mock( StepMeta.class ), rowMetaInterface,
            mock( Repository.class ), mock( IMetaStore.class ) );
    String sql = sqlStatement.getSQL();

    Assert.assertTrue( StringUtils.countMatches( sql, schemaTable ) == 2 );
  }

}
