/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.di.repository.pur;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatabaseDelegateTest {
  private PurRepository mockPurRepository;
  private DatabaseDelegate dbDelegate;

  @BeforeClass
  public static void before() throws KettleException {
  }

  @Before
  public void setup() throws KettleException {
    KettleClientEnvironment.init();
    mockPurRepository = mock( PurRepository.class );
    dbDelegate = new DatabaseDelegate( mockPurRepository );
  }

  @Test
  public void testExtraOptionEscapeWithInvalidCharInDatabaseType() throws KettleException {
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( dbMeta.getPluginId() ).thenReturn( "pluginId" );
    when( dbMeta.getAccessTypeDesc() ).thenReturn( "Native" );
    when( dbMeta.getHostname() ).thenReturn( "AS/400Host" );
    when( dbMeta.getDatabaseName() ).thenReturn( "mainframeTable" );
    when( dbMeta.getDatabasePortNumberString() ).thenReturn( "1234" );
    when( dbMeta.getUsername() ).thenReturn( "testUser" );
    when( dbMeta.getPassword() ).thenReturn( "123" );
    when( dbMeta.getServername() ).thenReturn( "as400.dot.com" );
    when( dbMeta.getDataTablespace() ).thenReturn( "tableSpace" );
    when( dbMeta.getIndexTablespace() ).thenReturn( "123" );

    // Create an extra options that has an unsupported character like '/'
    Properties extraOptions = new Properties();
    extraOptions.setProperty( "EXTRA_OPTION_AS/400.optionExtraOption", "true" );
    when( dbMeta.getAttributes() ).thenReturn( extraOptions );

    IUnifiedRepository purRepo = mock( IUnifiedRepository.class );
    when( purRepo.getReservedChars() ).thenReturn( Arrays.asList( new Character[] { '/' } ) );
    when( mockPurRepository.getPur() ).thenReturn( purRepo );

    DataNode escapedAttributes = dbDelegate.elementToDataNode( dbMeta );

    // Should only be one option in list
    for ( Iterator<DataNode> iter = escapedAttributes.getNodes().iterator(); iter.hasNext(); ) {
      DataNode options = iter.next();

      assertTrue( "Invalid escaped extra options", options.hasProperty( "EXTRA_OPTION_AS%2F400.optionExtraOption" ) );
      assertFalse( "Should not contain un-escaped option", options
          .hasProperty( "EXTRA_OPTION_AS/400.optionExtraOption" ) );
    }
  }

  @Test
  public void testExtraOptionUnescapeWithInvalidCharInDatabaseType() throws KettleException {
    DataNode mockDataNode = mock( DataNode.class );

    DataNode unescapedExtraOptions = new DataNode( "options" );
    unescapedExtraOptions.setProperty( "EXTRA_OPTION_AS%2F400.optionExtraOption", true );
    when( mockDataNode.getNode( "attributes" ) ).thenReturn( unescapedExtraOptions );

    DatabaseMeta unescapedDbMeta = mock( DatabaseMeta.class );
    when( unescapedDbMeta.getAttributes() ).thenReturn( new Properties() );

    dbDelegate.dataNodeToElement( mockDataNode, unescapedDbMeta );
    assertEquals( "true", unescapedDbMeta.getAttributes().getProperty( "EXTRA_OPTION_AS/400.optionExtraOption" ) );
  }
}
