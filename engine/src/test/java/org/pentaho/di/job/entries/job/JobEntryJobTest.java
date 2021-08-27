/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.job;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.base.MetaFileLoaderImpl;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.util.CurrentDirectoryResolver;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;
import org.pentaho.metastore.api.IMetaStore;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Node;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( { JobEntryJob.class, MetaFileLoaderImpl.class  } )
public class JobEntryJobTest {

  private final String JOB_ENTRY_JOB_NAME = "My Job";
  private final StringObjectId JOB_ENTRY_JOB_OBJECT_ID = new StringObjectId( "00x1" );
  private final String JOB_ENTRY_FILE_NAME = "job.kjb";
  private final String JOB_ENTRY_FILE_DIRECTORY = "/public/test";
  private final String JOB_ENTRY_FILE_PATH = "/home/ljm/job.kjb";
  private final String JOB_ENTRY_DESCRIPTION = "This is yet another job";

  private Repository repository = mock( Repository.class );
  private List<DatabaseMeta> databases = mock( List.class );
  private List<SlaveServer> servers = mock( List.class );
  private IMetaStore store = mock( IMetaStore.class );
  private VariableSpace space = mock( VariableSpace.class );
  private CurrentDirectoryResolver resolver = mock( CurrentDirectoryResolver.class );
  private RepositoryDirectoryInterface rdi = mock( RepositoryDirectoryInterface.class );
  private RepositoryDirectoryInterface directory = mock( RepositoryDirectoryInterface.class );
  private NamedClusterEmbedManager namedClusterEmbedManager = mock( NamedClusterEmbedManager.class );

  @Before
  public void setUp() throws Exception {
    doReturn( true ).when( repository ).isConnected();
    doReturn( null ).when( repository ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
    doReturn( rdi ).when( repository ).loadRepositoryDirectoryTree();
    doReturn( directory ).when( rdi ).findDirectory( JOB_ENTRY_FILE_DIRECTORY );
    doReturn( directory ).when( rdi ).findDirectory( "/home/admin/folder" );

    doReturn( null ).when( space ).environmentSubstitute( anyString() );
    doReturn( "" ).when( space ).environmentSubstitute( "" );
    doReturn( new String[]{} ).when( space ).listVariables();
    doReturn( JOB_ENTRY_FILE_PATH ).when( space ).environmentSubstitute( JOB_ENTRY_FILE_PATH );
    doReturn( JOB_ENTRY_FILE_NAME ).when( space ).environmentSubstitute( JOB_ENTRY_FILE_NAME );
    doReturn( JOB_ENTRY_FILE_DIRECTORY ).when( space ).environmentSubstitute( JOB_ENTRY_FILE_DIRECTORY );
    doReturn( "hdfs://server/path/" ).when( space ).environmentSubstitute( "${hdfs}" );
    doReturn( "/home/admin/folder/job.kjb" ).when( space ).environmentSubstitute( "${repositoryfullfilepath}" );
    doReturn( "/home/admin/folder/" ).when( space ).environmentSubstitute( "${repositorypath}" );
    doReturn( "job.kjb" ).when( space ).environmentSubstitute( "${jobname}" );
    doReturn( "job" ).when( space ).environmentSubstitute( "job" );

    doCallRealMethod().when( resolver ).normalizeSlashes( anyString() );
    doReturn( space ).when( resolver ).resolveCurrentDirectory(
      any( ObjectLocationSpecificationMethod.class ), any( VariableSpace.class ), any( Repository.class ), any( Job.class ), anyString() );

    whenNew( CurrentDirectoryResolver.class ).withNoArguments().thenReturn( resolver );
    whenNew( JobMeta.class ).withAnyArguments().thenReturn( mock( JobMeta.class ) );
  }

  /**
   * When disconnected from the repository and {@link JobEntryJob} contains no info,
   * default to {@link ObjectLocationSpecificationMethod}.{@code FILENAME}
   */
  @Test
  public void testNotConnectedLoad_NoInfo() throws Exception {
    JobEntryJob jej = getJej();
    jej.loadXML( getNode( jej ), databases, servers, null, store );

    assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
  }

  /**
   * When disconnected from the repository and {@link JobEntryJob} references a child job by {@link ObjectId},
   * this reference will be invalid to run such job.
   * Default to {@link ObjectLocationSpecificationMethod}.{@code FILENAME} with a {@code null} file path.
   */
  @Test
  public void testNotConnectedLoad_RepByRef() throws Exception {
    JobEntryJob jej = getJej();
    jej.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    jej.setJobObjectId( JOB_ENTRY_JOB_OBJECT_ID );
    jej.loadXML( getNode( jej ), databases, servers, null, store );
    jej.getJobMeta( null, store, space );

    assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    verifyNew( JobMeta.class )
      .withArguments( any( VariableSpace.class ), eq( null ), eq( null ), eq( store ), eq( null ) );
  }

  /**
   * When disconnected from the repository and {@link JobEntryJob} references a child job by name,
   * this reference will be invalid to run such job.
   * Default to {@link ObjectLocationSpecificationMethod}.{@code FILENAME} with a {@code null} file path.
   */
  @Test
  public void testNotConnectedLoad_RepByName() throws Exception {
    JobEntryJob jej = getJej();
    jej.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    jej.setJobName( JOB_ENTRY_FILE_NAME );
    jej.setDirectory( JOB_ENTRY_FILE_DIRECTORY );
    jej.loadXML( getNode( jej ), databases, servers, null, store );
    jej.getJobMeta( null, store, space );

    assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    verifyNew( JobMeta.class )
      .withArguments( any( VariableSpace.class ), eq( null ), eq( null ), eq( store ), eq( null ) );
  }

  /**
   * When disconnected from the repository and {@link JobEntryJob} references a child job by file path,
   * {@link ObjectLocationSpecificationMethod} will be {@code FILENAME}.
   */
  @Test
  public void testNotConnectedLoad_Filename() throws Exception {
    JobEntryJob jej = getJej();
    jej.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    jej.setFileName( JOB_ENTRY_FILE_PATH );
    jej.loadXML( getNode( jej ), databases, servers, null, store );
    jej.getJobMeta( null, store, space );

    assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    verifyNew( JobMeta.class )
      .withArguments( any( VariableSpace.class ), eq( JOB_ENTRY_FILE_PATH ), eq( null ), eq( store ), eq( null ) );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} contains no info,
   * default to {@link ObjectLocationSpecificationMethod}.{@code REPOSITORY_BY_NAME}
   */
  @Test
  public void testConnectedImport_NoInfo() throws Exception {
    JobEntryJob jej = getJej();
    jej.loadXML( getNode( jej ), databases, servers, repository, store );

    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by {@link ObjectId},
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_REFERENCE}.
   * Load the job from the repository using the specified {@link ObjectId}.
   */
  @Test
  public void testConnectedImport_RepByRef() throws Exception {
    JobEntryJob jej = getJej();
    jej.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    jej.setJobObjectId( JOB_ENTRY_JOB_OBJECT_ID );
    jej.loadXML( getNode( jej ), databases, servers, repository, store );
    jej.getJobMeta( repository, store, space );

    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE, jej.getSpecificationMethod() );
    verify( repository, times( 1 ) ).loadJob( JOB_ENTRY_JOB_OBJECT_ID, null );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name,
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedImport_RepByName() throws Exception {
    JobEntryJob jej = getJej();
    jej.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    jej.setJobName( JOB_ENTRY_FILE_NAME );
    jej.setDirectory( JOB_ENTRY_FILE_DIRECTORY );
    jej.loadXML( getNode( jej ), databases, servers, repository, store );
    jej.getJobMeta( repository, store, space );

    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
    verify( repository, times( 1 ) ).loadJob( JOB_ENTRY_FILE_NAME, directory, null, null );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by file path,
   * keep {@link ObjectLocationSpecificationMethod} as {@code FILENAME}.
   * Load the job from the repository using the specified file path.
   */
  @Test
  public void testConnectedImport_Filename() throws Exception {
    JobEntryJob jej = getJej();
    jej.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    jej.setFileName( JOB_ENTRY_FILE_PATH );
    jej.loadXML( getNode( jej ), databases, servers, repository, store );
    jej.getJobMeta( repository, store, space );

    assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    verifyNew( JobMeta.class )
      .withArguments( any( VariableSpace.class ), eq( JOB_ENTRY_FILE_PATH ), eq( repository ), eq( store ),
        eq( null ) );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by {@link ObjectId},
   * guess {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_REFERENCE}.
   * Load the job from the repository using the specified {@link ObjectId}.
   */
  @Test
  public void testConnectedImport_RepByRef_Guess() throws Exception {
    JobEntryJob jej = getJej();
    jej.setJobObjectId( JOB_ENTRY_JOB_OBJECT_ID );
    jej.loadXML( getNode( jej ), databases, servers, repository, store );
    jej.getJobMeta( repository, store, space );

    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE, jej.getSpecificationMethod() );
    verify( repository, times( 1 ) ).loadJob( JOB_ENTRY_JOB_OBJECT_ID, null );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name,
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedImport_RepByName_Guess() throws Exception {
    JobEntryJob jej = getJej();
    jej.setJobName( JOB_ENTRY_FILE_NAME );
    jej.setDirectory( JOB_ENTRY_FILE_DIRECTORY );
    jej.loadXML( getNode( jej ), databases, servers, repository, store );
    jej.getJobMeta( repository, store, space );

    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
    verify( repository, times( 1 ) ).loadJob( JOB_ENTRY_FILE_NAME, directory, null, null );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by file path,
   * guess {@link ObjectLocationSpecificationMethod} as {@code FILENAME}.
   * Load the job from the repository using the specified file path.
   */
  @Test
  public void testConnectedImport_Filename_Guess() throws Exception {
    JobEntryJob jej = getJej();
    jej.setFileName( JOB_ENTRY_FILE_PATH );
    jej.loadXML( getNode( jej ), databases, servers, repository, store );
    jej.getJobMeta( repository, store, space );

    assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    verifyNew( JobMeta.class )
      .withArguments( any( VariableSpace.class ), eq( JOB_ENTRY_FILE_PATH ), eq( repository ), eq( store ), eq( null ) );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} contains no info,
   * default to {@link ObjectLocationSpecificationMethod}.{@code REPOSITORY_BY_NAME}
   */
  @Test
  public void testConnectedLoad_NoInfo() throws Exception {
    JobEntryJob jej = getJej();
    jej.loadRep( repository, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );

    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by {@link ObjectId},
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_REFERENCE}.
   * Load the job from the repository using the specified {@link ObjectId}.
   */
  @Test
  public void testConnectedLoad_RepByRef() throws Exception {
    Repository myrepo = mock( Repository.class );
    doReturn( true ).when( myrepo ).isConnected();
    doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
    doReturn( "rep_ref" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
    doReturn( JOB_ENTRY_JOB_OBJECT_ID.toString() ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "job_object_id" );

    JobEntryJob jej = getJej();
    jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
    jej.getJobMeta( myrepo, store, space );

    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE, jej.getSpecificationMethod() );
    verify( myrepo, times( 1 ) ).loadJob( JOB_ENTRY_JOB_OBJECT_ID, null );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name,
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedLoad_RepByName() throws Exception {
    Repository myrepo = mock( Repository.class );
    doReturn( true ).when( myrepo ).isConnected();
    doReturn( rdi ).when( myrepo ).loadRepositoryDirectoryTree();
    doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
    doReturn( "rep_name" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
    doReturn( JOB_ENTRY_FILE_NAME ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "name" );
    doReturn( JOB_ENTRY_FILE_DIRECTORY ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "dir_path" );

    JobEntryJob jej = getJej();
    jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
    jej.getJobMeta( myrepo, store, space );

    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
    verify( myrepo, times( 1 ) ).loadJob( JOB_ENTRY_FILE_NAME, directory, null, null );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by file path,
   * keep {@link ObjectLocationSpecificationMethod} as {@code FILENAME}.
   * Load the job from the repository using the specified file path.
   */
  @Test
  public void testConnectedLoad_Filename() throws Exception {
    Repository myrepo = mock( Repository.class );
    doReturn( true ).when( myrepo ).isConnected();
    doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
    doReturn( "filename" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
    doReturn( JOB_ENTRY_FILE_PATH ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "file_name" );

    JobEntryJob jej = getJej();
    jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
    jej.getJobMeta( myrepo, store, space );

    assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    verifyNew( JobMeta.class )
      .withArguments( any( VariableSpace.class ), eq( JOB_ENTRY_FILE_PATH ), eq( myrepo ), eq( store ), eq( null ) );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name,
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedLoad_RepByName_HDFS() throws Exception {
    Repository myrepo = mock( Repository.class );
    doReturn( true ).when( myrepo ).isConnected();
    doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
    doReturn( "rep_name" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
    doReturn( "job" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "name" );
    doReturn( "${hdfs}" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "dir_path" );

    JobEntryJob jej = getJej();
    jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
    jej.getJobMeta( myrepo, store, space );

    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
    verifyNew( JobMeta.class )
      .withArguments( any( VariableSpace.class ), eq( "hdfs://server/path/job.kjb" ), eq( myrepo ), eq( store ), eq( null ) );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name using a single parameter,
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedLoad_RepByName_SingleParameter() throws Exception {
    Repository myrepo = mock( Repository.class );
    doReturn( true ).when( myrepo ).isConnected();
    doReturn( rdi ).when( myrepo ).loadRepositoryDirectoryTree();
    doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
    doReturn( "rep_name" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
    doReturn( "${repositoryfullfilepath}" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "name" );

    JobEntryJob jej = getJej();
    jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
    jej.getJobMeta( myrepo, store, space );

    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
    verify( myrepo, times( 1 ) ).loadJob( "job.kjb", directory, null, null );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name using multiple parameters,
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedLoad_RepByName_MultipleParameters() throws Exception {
    Repository myrepo = mock( Repository.class );
    doReturn( true ).when( myrepo ).isConnected();
    doReturn( rdi ).when( myrepo ).loadRepositoryDirectoryTree();
    doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
    doReturn( "rep_name" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
    doReturn( "${jobname}" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "name" );
    doReturn( "${repositorypath}" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "dir_path" );

    JobEntryJob jej = getJej();
    jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
    jej.getJobMeta( myrepo, store, space );

    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
    verify( myrepo, times( 1 ) ).loadJob( "job.kjb", directory, null, null );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by {@link ObjectId},
   * guess {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_REFERENCE}.
   * Load the job from the repository using the specified {@link ObjectId}.
   */
  @Test
  public void testConnectedLoad_RepByRef_Guess() throws Exception {
    Repository myrepo = mock( Repository.class );
    doReturn( true ).when( myrepo ).isConnected();
    doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
    doReturn( JOB_ENTRY_JOB_OBJECT_ID.toString() ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "job_object_id" );

    JobEntryJob jej = getJej();
    jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
    jej.getJobMeta( myrepo, store, space );

    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE, jej.getSpecificationMethod() );
    verify( myrepo, times( 1 ) ).loadJob( JOB_ENTRY_JOB_OBJECT_ID, null );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name,
   * guess {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedLoad_RepByName_Guess() throws Exception {
    Repository myrepo = mock( Repository.class );
    doReturn( true ).when( myrepo ).isConnected();
    doReturn( rdi ).when( myrepo ).loadRepositoryDirectoryTree();
    doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
    doReturn( JOB_ENTRY_FILE_NAME ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "name" );
    doReturn( JOB_ENTRY_FILE_DIRECTORY ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "dir_path" );

    JobEntryJob jej = getJej();
    jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
    jej.getJobMeta( myrepo, store, space );

    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
    verify( myrepo, times( 1 ) ).loadJob( JOB_ENTRY_FILE_NAME, directory, null, null );
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by file path,
   * guess {@link ObjectLocationSpecificationMethod} as {@code FILENAME}.
   * Load the job from the repository using the specified file path.
   */
  @Test
  public void testConnectedLoad_Filename_Guess() throws Exception {
    Repository myrepo = mock( Repository.class );
    doReturn( true ).when( myrepo ).isConnected();
    doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
    doReturn( JOB_ENTRY_FILE_PATH ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "file_name" );

    JobEntryJob jej = getJej();
    jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
    jej.getJobMeta( myrepo, store, space );

    assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    verifyNew( JobMeta.class )
      .withArguments( any( VariableSpace.class ), eq( JOB_ENTRY_FILE_PATH ), eq( myrepo ), eq( store ), eq( null ) );
  }

  private Node getNode( JobEntryJob jej ) throws Exception {
    String string = "<job>" + jej.getXML() + "</job>";
    InputStream stream = new ByteArrayInputStream( string.getBytes( StandardCharsets.UTF_8 ) );
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    return db.parse( stream ).getFirstChild();
  }

  @Test
  public void testCurrDirListener() throws Exception {
    JobMeta meta = mock( JobMeta.class );
    JobEntryJob jej = getJej();
    jej.setParentJobMeta( null );
    jej.setParentJobMeta( meta );
    jej.setParentJobMeta( null );
    verify( meta, times( 1 ) ).addCurrentDirectoryChangedListener( any() );
    verify( meta, times( 1 ) ).removeCurrentDirectoryChangedListener( any() );
  }

  @Test
  public void testExportResources() throws Exception {
    JobMeta meta = mock( JobMeta.class );
    JobEntryJob jej = getJej();
    jej.setDescription( JOB_ENTRY_DESCRIPTION );

    doReturn( meta ).when( jej ).getJobMeta(
      any( Repository.class ), any( IMetaStore.class ), any( VariableSpace.class ) );
    doReturn( JOB_ENTRY_JOB_NAME ).when( meta ).exportResources(
      any( JobMeta.class ), any( Map.class ), any( ResourceNamingInterface.class ),
      any( Repository.class ), any( IMetaStore.class ) );

    jej.exportResources( null, null, null, null, null );

    verify( meta ).setFilename( "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}/" + JOB_ENTRY_JOB_NAME );
    verify( jej ).setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
  }

  @Test
  public void testJobMetaInitializeVariablesFrom() throws Exception {
    Repository myrepo = mock( Repository.class );
    JobMeta jobMeta = new JobMeta();
    doReturn( directory ).when( rdi ).findDirectory( anyString() );
    doReturn( jobMeta ).when( myrepo ).loadJob( any(), any(), any(), any() );
    doReturn( rdi ).when( myrepo ).loadRepositoryDirectoryTree();
    doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
    doReturn( "rep_name" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
    doReturn( "${jobname}" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "name" );
    doReturn( "${repositorypath}" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "dir_path" );
    doReturn( true ).when( myrepo ).isConnected();

    JobEntryJob jej =  new JobEntryJob( JOB_ENTRY_JOB_NAME );
    jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
    jej.getJobMetaFromRepository( myrepo, resolver, "", space );

    verify( jobMeta, times( 1 ) ).initializeVariablesFrom( any() );
  }

  private JobEntryJob getJej() {
    JobEntryJob jej = spy( new JobEntryJob( JOB_ENTRY_JOB_NAME ) );
    JobMeta parentJobMeta = spy( new JobMeta() );
    when( parentJobMeta.getNamedClusterEmbedManager() ).thenReturn( namedClusterEmbedManager );
    jej.setParentJobMeta( parentJobMeta);
    return jej;
  }
}
