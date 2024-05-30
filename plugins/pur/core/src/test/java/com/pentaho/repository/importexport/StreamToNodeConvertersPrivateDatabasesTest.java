/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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

package com.pentaho.repository.importexport;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Andrey Khayrutdinov
 */
@RunWith( Parameterized.class )
public class StreamToNodeConvertersPrivateDatabasesTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String FILE_ID = "fileId";

  @Parameterized.Parameters
  public static List<Object[]> getData() throws Exception {
    RepositoryFile repositoryFile = new RepositoryFile.Builder( "test file" ).build();
    IUnifiedRepository pur = mock( IUnifiedRepository.class );
    when( pur.getFileById( FILE_ID ) ).thenReturn( repositoryFile );

    TransMeta transMeta = new TransMeta();
    JobMeta jobMeta = new JobMeta();

    Repository repository = mock( Repository.class );
    when( repository.loadTransformation( any( StringObjectId.class ), any() ) ).thenReturn( transMeta );
    when( repository.loadJob( any( StringObjectId.class ), any() ) ).thenReturn( jobMeta );

    StreamToTransNodeConverter transNodeConverter = new StreamToTransNodeConverter( pur );
    transNodeConverter = spy( transNodeConverter );
    doReturn( repository ).when( transNodeConverter ).connectToRepository();

    StreamToJobNodeConverter jobNodeConverter = new StreamToJobNodeConverter( pur );
    jobNodeConverter = spy( jobNodeConverter );
    doReturn( repository ).when( jobNodeConverter ).connectToRepository();

    Object[] trans = { transNodeConverter, TransMeta.XML_TAG, transMeta };
    Object[] job = { jobNodeConverter, JobMeta.XML_TAG, jobMeta };
    return asList( trans, job );
  }

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  private final Converter converter;
  private final String metaTag;
  private final AbstractMeta meta;

  public StreamToNodeConvertersPrivateDatabasesTest( Converter converter, String metaTag, AbstractMeta meta ) {
    this.converter = converter;
    this.metaTag = metaTag;
    this.meta = meta;
  }

  @Test
  public void removesSharedDatabases() throws Exception {
    List<DatabaseMeta> dbs =
        new ArrayList<DatabaseMeta>( asList( createDb( "meta1" ), createDb( "private" ), createDb( "meta2" ) ) );
    meta.setDatabases( dbs );
    meta.setPrivateDatabases( Collections.singleton( "private" ) );
    System.setProperty( Const.STRING_ONLY_USED_DB_TO_XML, "N" );
    InputStream stream = converter.convert( FILE_ID );
    assertDatabaseNodes( stream, "private" );
    System.setProperty( Const.STRING_ONLY_USED_DB_TO_XML, "Y" );
  }

  @Test
  public void removesAll_IfPrivateSetIsEmpty() throws Exception {
    List<DatabaseMeta> dbs = new ArrayList<DatabaseMeta>( asList( createDb( "meta1" ), createDb( "meta2" ) ) );
    meta.setDatabases( dbs );
    meta.setPrivateDatabases( Collections.<String> emptySet() );
    System.setProperty( Const.STRING_ONLY_USED_DB_TO_XML, "N" );

    InputStream stream = converter.convert( FILE_ID );
    assertDatabaseNodes( stream );
    System.setProperty( Const.STRING_ONLY_USED_DB_TO_XML, "Y" );
  }

  @Test
  public void removesAll_IfPrivateSetIsEmptyOnlyUsed() throws Exception {
    List<DatabaseMeta> dbs = new ArrayList<DatabaseMeta>( asList( createDb( "meta1" ), createDb( "meta2" ) ) );
    meta.setDatabases( dbs );
    meta.setPrivateDatabases( Collections.<String> emptySet() );

    InputStream stream = converter.convert( FILE_ID );
    assertDatabaseNodesEmpty( stream );
  }

  @Test
  public void keepsAll_IfPrivateSetIsNullOnlyUsed() throws Exception {
    List<DatabaseMeta> dbs = new ArrayList<DatabaseMeta>( asList( createDb( "meta1" ), createDb( "meta2" ) ) );
    meta.setDatabases( dbs );
    meta.setPrivateDatabases( null );

    System.setProperty( Const.STRING_ONLY_USED_DB_TO_XML, "N" );
    InputStream stream = converter.convert( FILE_ID );

    assertDatabaseNodes( stream, "meta1", "meta2" );
    System.setProperty( Const.STRING_ONLY_USED_DB_TO_XML, "Y" );
  }

  @Test
  public void keepsAll_IfPrivateSetIsNullExportAll() throws Exception {
    List<DatabaseMeta> dbs = new ArrayList<DatabaseMeta>( asList( createDb( "meta1" ), createDb( "meta2" ) ) );
    meta.setDatabases( dbs );
    meta.setPrivateDatabases( null );

    InputStream stream = converter.convert( FILE_ID );
    assertDatabaseNodesEmpty( stream, "meta1", "meta2" );
  }

  private void assertDatabaseNodes( InputStream stream, String... names ) throws Exception {
    if ( names == null ) {
      names = new String[0];
    }
    Document document = XMLHandler.loadXMLFile( stream, null, false, false );
    Node metaNode = XMLHandler.getSubNode( document, metaTag );
    List<Node> nodes = XMLHandler.getNodes( metaNode, DatabaseMeta.XML_TAG );
    assertNodes( nodes, names );
  }
  private void assertDatabaseNodesEmpty( InputStream stream, String... names ) throws Exception {
    if ( names == null ) {
      names = new String[0];
    }
    Document document = XMLHandler.loadXMLFile( stream, null, false, false );
    Node metaNode = XMLHandler.getSubNode( document, metaTag );
    List<Node> nodes = XMLHandler.getNodes( metaNode, DatabaseMeta.XML_TAG );
    assertTrue( nodes.isEmpty() );
  }

  private void assertNodes( List<Node> nodes, String... names ) {
    assertEquals( names.length, nodes.size() );

    Set<String> expectedNames = new HashSet<String>( asList( names ) );
    for ( Node node : nodes ) {
      String name = XMLHandler.getTagValue( node, "name" );
      assertNotNull( name );
      assertTrue( name, expectedNames.remove( name ) );
    }
  }

  private DatabaseMeta createDb( String name ) {
    DatabaseMeta meta = new DatabaseMeta();
    meta.setName( name );
    meta.getDatabaseInterface().setDatabaseName( name );
    return meta;
  }
}
