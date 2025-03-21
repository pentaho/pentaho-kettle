/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.job.entries.job;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

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
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.base.MetaFileLoaderImpl;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
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
import org.w3c.dom.Node;

@RunWith( MockitoJUnitRunner.class )
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
    doReturn( JOB_ENTRY_FILE_PATH ).when( space ).environmentSubstitute( JOB_ENTRY_FILE_PATH );
    doReturn( JOB_ENTRY_FILE_NAME ).when( space ).environmentSubstitute( JOB_ENTRY_FILE_NAME );
    doReturn( JOB_ENTRY_FILE_DIRECTORY ).when( space ).environmentSubstitute( JOB_ENTRY_FILE_DIRECTORY );
    doReturn( "hdfs://server/path/" ).when( space ).environmentSubstitute( "${hdfs}" );
    doReturn( "/home/admin/folder/job.kjb" ).when( space ).environmentSubstitute( "${repositoryfullfilepath}" );
    doReturn( "/home/admin/folder/" ).when( space ).environmentSubstitute( "${repositorypath}" );
    doReturn( "job.kjb" ).when( space ).environmentSubstitute( "${jobname}" );
    doReturn( "job" ).when( space ).environmentSubstitute( "job" );
  }

  /**
   * When disconnected from the repository and {@link JobEntryJob} contains no info,
   * default to {@link ObjectLocationSpecificationMethod}.{@code FILENAME}
   */
  @Test
  public void testNotConnectedLoad_NoInfo() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ), any( VariableSpace.class ),
                nullable( Repository.class ), nullable( Job.class ), anyString() );
            } ) ) {
      JobEntryJob jej = getJej();
      jej.loadXML( getNode( jej ), databases, servers, null, store );

      assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    }
  }

  /**
   * When disconnected from the repository and {@link JobEntryJob} references a child job by {@link ObjectId},
   * this reference will be invalid to run such job.
   * Default to {@link ObjectLocationSpecificationMethod}.{@code FILENAME} with a {@code null} file path.
   */
  @Test
  public void testNotConnectedLoad_RepByRef() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction =
            mockConstruction( JobMeta.class,
              ( mock, context ) -> {
                if ( context.getCount() > 1 ) {
                  assertEquals( context.arguments().get( 0 ), DefaultBowl.getInstance() );
                  assertEquals( context.arguments().get( 1 ), space );
                  assertEquals( context.arguments().get( 2 ), null );
                  assertEquals( context.arguments().get( 3 ), null );
                  assertEquals( context.arguments().get( 4 ), store );
                  assertEquals( context.arguments().get( 5 ), null );
                } else {
                  assertEquals( 0, context.arguments().size() );
                }
              } );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ),
                nullable( String.class ) );
            } ) ) {
      JobEntryJob jej = getJej();
      jej.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
      jej.setJobObjectId( JOB_ENTRY_JOB_OBJECT_ID );
      jej.loadXML( getNode( jej ), databases, servers, null, store );
      jej.getJobMeta( null, store, space );

      assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    }
  }

  /**
   * When disconnected from the repository and {@link JobEntryJob} references a child job by name,
   * this reference will be invalid to run such job.
   * Default to {@link ObjectLocationSpecificationMethod}.{@code FILENAME} with a {@code null} file path.
   */
  @Test
  public void testNotConnectedLoad_RepByName() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction =
            mockConstruction( JobMeta.class,
              ( mock, context ) -> {
                if ( context.getCount() > 1 ) {
                  assertEquals( context.arguments().get( 0 ), DefaultBowl.getInstance() );
                  assertEquals( context.arguments().get( 1 ), space );
                  assertEquals( context.arguments().get( 2 ), null );
                  assertEquals( context.arguments().get( 3 ), null );
                  assertEquals( context.arguments().get( 4 ), store );
                  assertEquals( context.arguments().get( 5 ), null );
                } else {
                  assertEquals( 0, context.arguments().size() );
                }
              } );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ), anyString() );
            } ) ) {
      JobEntryJob jej = getJej();
      jej.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
      jej.setJobName( JOB_ENTRY_FILE_NAME );
      jej.setDirectory( JOB_ENTRY_FILE_DIRECTORY );
      jej.loadXML( getNode( jej ), databases, servers, null, store );
      jej.getJobMeta( null, store, space );

      assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    }
  }

  /**
   * When disconnected from the repository and {@link JobEntryJob} references a child job by file path,
   * {@link ObjectLocationSpecificationMethod} will be {@code FILENAME}.
   */
  @Test
  public void testNotConnectedLoad_Filename() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction =
            mockConstruction( JobMeta.class,
              ( mock, context ) -> {
                if ( context.getCount() > 1 ) {
                  assertEquals( context.arguments().get( 0 ), DefaultBowl.getInstance() );
                  assertEquals( context.arguments().get( 1 ), space );
                  assertEquals( context.arguments().get( 2 ), JOB_ENTRY_FILE_PATH );
                  assertEquals( context.arguments().get( 3 ), null );
                  assertEquals( context.arguments().get( 4 ), store );
                  assertEquals( context.arguments().get( 5 ), null );
                } else {
                  assertEquals( 0, context.arguments().size() );
                }
              } );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ), anyString() );
            } ) ) {
      JobEntryJob jej = getJej();
      jej.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
      jej.setFileName( JOB_ENTRY_FILE_PATH );
      jej.loadXML( getNode( jej ), databases, servers, null, store );
      jej.getJobMeta( null, store, space );

      assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} contains no info,
   * default to {@link ObjectLocationSpecificationMethod}.{@code REPOSITORY_BY_NAME}
   */
  @Test
  public void testConnectedImport_NoInfo() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), any( Repository.class ), any( Job.class ), anyString() );
            } ) ) {
      JobEntryJob jej = getJej();
      jej.loadXML( getNode( jej ), databases, servers, repository, store );

      assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by {@link ObjectId},
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_REFERENCE}.
   * Load the job from the repository using the specified {@link ObjectId}.
   */
  @Test
  public void testConnectedImport_RepByRef() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), any( Repository.class ), any( Job.class ), anyString() );
            } ) ) {
      JobEntryJob jej = getJej();
      jej.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
      jej.setJobObjectId( JOB_ENTRY_JOB_OBJECT_ID );
      jej.loadXML( getNode( jej ), databases, servers, repository, store );
      jej.getJobMeta( repository, store, space );

      assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE, jej.getSpecificationMethod() );
      verify( repository, times( 1 ) ).loadJob( JOB_ENTRY_JOB_OBJECT_ID, null );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name,
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedImport_RepByName() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ),
                nullable( String.class ) );
            } ) ) {
      JobEntryJob jej = getJej();
      jej.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
      jej.setJobName( JOB_ENTRY_FILE_NAME );
      jej.setDirectory( JOB_ENTRY_FILE_DIRECTORY );
      jej.loadXML( getNode( jej ), databases, servers, repository, store );
      jej.getJobMeta( repository, store, space );

      assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
      verify( repository, times( 1 ) ).loadJob( JOB_ENTRY_FILE_NAME, directory, null, null );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by file path,
   * keep {@link ObjectLocationSpecificationMethod} as {@code FILENAME}.
   * Load the job from the repository using the specified file path.
   */
  @Test
  public void testConnectedImport_Filename() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction =
            mockConstruction( JobMeta.class,
              ( mock, context ) -> {
                if ( context.getCount() > 1 ) {
                  assertEquals( context.arguments().get( 0 ), DefaultBowl.getInstance() );
                  assertEquals( context.arguments().get( 1 ), space );
                  assertEquals( context.arguments().get( 2 ), JOB_ENTRY_FILE_PATH );
                  assertEquals( context.arguments().get( 3 ), repository );
                  assertEquals( context.arguments().get( 4 ), store );
                  assertEquals( context.arguments().get( 5 ), null );
                } else {
                  assertEquals( 0, context.arguments().size() );
                }
              } );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ), anyString() );
            } ) ) {
      JobEntryJob jej = getJej();
      jej.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
      jej.setFileName( JOB_ENTRY_FILE_PATH );
      jej.loadXML( getNode( jej ), databases, servers, repository, store );
      jej.getJobMeta( repository, store, space );

      assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by {@link ObjectId},
   * guess {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_REFERENCE}.
   * Load the job from the repository using the specified {@link ObjectId}.
   */
  @Test
  public void testConnectedImport_RepByRef_Guess() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), any( Repository.class ), any( Job.class ), anyString() );
            } ) ) {
      JobEntryJob jej = getJej();
      jej.setJobObjectId( JOB_ENTRY_JOB_OBJECT_ID );
      jej.loadXML( getNode( jej ), databases, servers, repository, store );
      jej.getJobMeta( repository, store, space );

      assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE, jej.getSpecificationMethod() );
      verify( repository, times( 1 ) ).loadJob( JOB_ENTRY_JOB_OBJECT_ID, null );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name,
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedImport_RepByName_Guess() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ),
                nullable( String.class ) );
            } ) ) {
      JobEntryJob jej = getJej();
      jej.setJobName( JOB_ENTRY_FILE_NAME );
      jej.setDirectory( JOB_ENTRY_FILE_DIRECTORY );
      jej.loadXML( getNode( jej ), databases, servers, repository, store );
      jej.getJobMeta( repository, store, space );

      assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
      verify( repository, times( 1 ) ).loadJob( JOB_ENTRY_FILE_NAME, directory, null, null );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by file path,
   * guess {@link ObjectLocationSpecificationMethod} as {@code FILENAME}.
   * Load the job from the repository using the specified file path.
   */
  @Test
  public void testConnectedImport_Filename_Guess() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction =
            mockConstruction( JobMeta.class,
              ( mock, context ) -> {
                if ( context.getCount() > 1 ) {
                  assertEquals( context.arguments().get( 0 ), DefaultBowl.getInstance() );
                  assertEquals( context.arguments().get( 1 ), space );
                  assertEquals( context.arguments().get( 2 ), JOB_ENTRY_FILE_PATH );
                  assertEquals( context.arguments().get( 3 ), repository );
                  assertEquals( context.arguments().get( 4 ), store );
                  assertEquals( context.arguments().get( 5 ), null );
                } else {
                  assertEquals( 0, context.arguments().size() );
                }
              } );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ), anyString() );
            } ) ) {
      JobEntryJob jej = getJej();
      jej.setFileName( JOB_ENTRY_FILE_PATH );
      jej.loadXML( getNode( jej ), databases, servers, repository, store );
      jej.getJobMeta( repository, store, space );

      assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} contains no info,
   * default to {@link ObjectLocationSpecificationMethod}.{@code REPOSITORY_BY_NAME}
   */
  @Test
  public void testConnectedLoad_NoInfo() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), any( Repository.class ), any( Job.class ), anyString() );
            } ) ) {
      JobEntryJob jej = getJej();
      jej.loadRep( repository, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );

      assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by {@link ObjectId},
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_REFERENCE}.
   * Load the job from the repository using the specified {@link ObjectId}.
   */
  @Test
  public void testConnectedLoad_RepByRef() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), any( Repository.class ), any( Job.class ), anyString() );
            } ) ) {
      Repository myrepo = mock( Repository.class );
      doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
      doReturn( "rep_ref" ).when( myrepo )
        .getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
      doReturn( JOB_ENTRY_JOB_OBJECT_ID.toString() ).when( myrepo )
        .getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "job_object_id" );

      JobEntryJob jej = getJej();
      jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
      jej.getJobMeta( myrepo, store, space );

      assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE, jej.getSpecificationMethod() );
      verify( myrepo, times( 1 ) ).loadJob( JOB_ENTRY_JOB_OBJECT_ID, null );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name,
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedLoad_RepByName() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ),
                nullable( String.class ) );
            } ) ) {
      Repository myrepo = mock( Repository.class );
      doReturn( rdi ).when( myrepo ).loadRepositoryDirectoryTree();
      doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
      doReturn( "rep_name" ).when( myrepo )
        .getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
      doReturn( JOB_ENTRY_FILE_NAME ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "name" );
      doReturn( JOB_ENTRY_FILE_DIRECTORY ).when( myrepo )
        .getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "dir_path" );

      JobEntryJob jej = getJej();
      jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
      jej.getJobMeta( myrepo, store, space );

      assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
      verify( myrepo, times( 1 ) ).loadJob( JOB_ENTRY_FILE_NAME, directory, null, null );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by file path,
   * keep {@link ObjectLocationSpecificationMethod} as {@code FILENAME}.
   * Load the job from the repository using the specified file path.
   */
  @Test
  public void testConnectedLoad_Filename() throws Exception {
    Repository myrepo = mock( Repository.class );
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction =
            mockConstruction( JobMeta.class,
              ( mock, context ) -> {
                if ( context.getCount() > 1 ) {
                  assertEquals( context.arguments().get( 0 ), DefaultBowl.getInstance() );
                  assertEquals( context.arguments().get( 1 ), space );
                  assertEquals( context.arguments().get( 2 ), JOB_ENTRY_FILE_PATH );
                  assertEquals( context.arguments().get( 3 ), myrepo );
                  assertEquals( context.arguments().get( 4 ), store );
                  assertEquals( context.arguments().get( 5 ), null );
                } else {
                  assertEquals( 0, context.arguments().size() );
                }
              } );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ), anyString() );
            } ) ) {

      doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
      doReturn( "filename" ).when( myrepo )
        .getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
      doReturn( JOB_ENTRY_FILE_PATH ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "file_name" );

      JobEntryJob jej = getJej();
      jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
      jej.getJobMeta( myrepo, store, space );

      assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name,
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedLoad_RepByName_HDFS() throws Exception {
    Repository myrepo = mock( Repository.class );
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction =
            mockConstruction( JobMeta.class,
              ( mock, context ) -> {
                if ( context.getCount() > 1 ) {
                  assertEquals( context.arguments().get( 0 ), DefaultBowl.getInstance() );
                  assertEquals( context.arguments().get( 1 ), space );
                  assertEquals( context.arguments().get( 2 ), "hdfs://server/path/job.kjb" );
                  assertEquals( context.arguments().get( 3 ), myrepo );
                  assertEquals( context.arguments().get( 4 ), store );
                  assertEquals( context.arguments().get( 5 ), null );
                } else {
                  assertEquals( 0, context.arguments().size() );
                }
              } );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ),
                nullable( String.class ) );
            } ) ) {
      doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
      doReturn( "rep_name" ).when( myrepo )
        .getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
      doReturn( "job" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "name" );
      doReturn( "${hdfs}" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "dir_path" );

      JobEntryJob jej = getJej();
      jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
      jej.getJobMeta( myrepo, store, space );

      assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name using a single parameter,
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedLoad_RepByName_SingleParameter() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ),
                nullable( String.class ) );
            } ) ) {
      Repository myrepo = mock( Repository.class );
      doReturn( rdi ).when( myrepo ).loadRepositoryDirectoryTree();
      doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
      doReturn( "rep_name" ).when( myrepo )
        .getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
      doReturn( "${repositoryfullfilepath}" ).when( myrepo )
        .getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "name" );

      JobEntryJob jej = getJej();
      jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
      jej.getJobMeta( myrepo, store, space );

      assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
      verify( myrepo, times( 1 ) ).loadJob( "job.kjb", directory, null, null );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name using multiple parameters,
   * keep {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedLoad_RepByName_MultipleParameters() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ),
                nullable( String.class ) );
            } ) ) {
      Repository myrepo = mock( Repository.class );
      doReturn( rdi ).when( myrepo ).loadRepositoryDirectoryTree();
      doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
      doReturn( "rep_name" ).when( myrepo )
        .getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
      doReturn( "${jobname}" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "name" );
      doReturn( "${repositorypath}" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "dir_path" );

      JobEntryJob jej = getJej();
      jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
      jej.getJobMeta( myrepo, store, space );

      assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
      verify( myrepo, times( 1 ) ).loadJob( "job.kjb", directory, null, null );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by {@link ObjectId},
   * guess {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_REFERENCE}.
   * Load the job from the repository using the specified {@link ObjectId}.
   */
  @Test
  public void testConnectedLoad_RepByRef_Guess() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), any( Repository.class ), any( Job.class ), anyString() );
            } ) ) {
      Repository myrepo = mock( Repository.class );
      doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
      doReturn( JOB_ENTRY_JOB_OBJECT_ID.toString() ).when( myrepo )
        .getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "job_object_id" );

      JobEntryJob jej = getJej();
      jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
      jej.getJobMeta( myrepo, store, space );

      assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE, jej.getSpecificationMethod() );
      verify( myrepo, times( 1 ) ).loadJob( JOB_ENTRY_JOB_OBJECT_ID, null );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by name,
   * guess {@link ObjectLocationSpecificationMethod} as {@code REPOSITORY_BY_NAME}.
   * Load the job from the repository using the specified job name and directory.
   */
  @Test
  public void testConnectedLoad_RepByName_Guess() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ),
                nullable( String.class ) );
            } ) ) {
      Repository myrepo = mock( Repository.class );
      doReturn( rdi ).when( myrepo ).loadRepositoryDirectoryTree();
      doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
      doReturn( JOB_ENTRY_FILE_NAME ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "name" );
      doReturn( JOB_ENTRY_FILE_DIRECTORY ).when( myrepo )
        .getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "dir_path" );

      JobEntryJob jej = getJej();
      jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
      jej.getJobMeta( myrepo, store, space );

      assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, jej.getSpecificationMethod() );
      verify( myrepo, times( 1 ) ).loadJob( JOB_ENTRY_FILE_NAME, directory, null, null );
    }
  }

  /**
   * When connected to the repository and {@link JobEntryJob} references a child job by file path,
   * guess {@link ObjectLocationSpecificationMethod} as {@code FILENAME}.
   * Load the job from the repository using the specified file path.
   */
  @Test
  public void testConnectedLoad_Filename_Guess() throws Exception {
    Repository myrepo = mock( Repository.class );
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction =
            mockConstruction( JobMeta.class,
              ( mock, context ) -> {
                if ( context.getCount() > 1 ) {
                  assertEquals( context.arguments().get( 0 ), DefaultBowl.getInstance() );
                  assertEquals( context.arguments().get( 1 ), space );
                  assertEquals( context.arguments().get( 2 ), JOB_ENTRY_FILE_PATH );
                  assertEquals( context.arguments().get( 3 ), myrepo );
                  assertEquals( context.arguments().get( 4 ), store );
                  assertEquals( context.arguments().get( 5 ), null );
                } else {
                  assertEquals( 0, context.arguments().size() );
                }
              } );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ), anyString() );
            } ) ) {
      doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
      doReturn( JOB_ENTRY_FILE_PATH ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "file_name" );

      JobEntryJob jej = getJej();
      jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
      jej.getJobMeta( myrepo, store, space );

      assertEquals( ObjectLocationSpecificationMethod.FILENAME, jej.getSpecificationMethod() );
    }
  }

  private Node getNode( JobEntryJob jej ) throws Exception {
    String string = "<job>" + jej.getXML() + "</job>";
    InputStream stream = new ByteArrayInputStream( string.getBytes( StandardCharsets.UTF_8 ) );
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    return db.parse( stream ).getFirstChild();
  }

  @Test
  public void testCurrDirListener() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ), anyString() );
            } ) ) {
      JobMeta meta = mock( JobMeta.class );
      JobEntryJob jej = getJej();
      jej.setParentJobMeta( null );
      jej.setParentJobMeta( meta );
      jej.setParentJobMeta( null );
      verify( meta, times( 1 ) ).addCurrentDirectoryChangedListener( any() );
      verify( meta, times( 1 ) ).removeCurrentDirectoryChangedListener( any() );
    }
  }

  @Test
  public void testExportResources() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ), anyString() );
            } ) ) {
      JobMeta meta = mock( JobMeta.class );
      JobEntryJob jej = getJej();
      jej.setDescription( JOB_ENTRY_DESCRIPTION );

      doReturn( meta ).when( jej ).getJobMeta(
        nullable( Repository.class ), nullable( IMetaStore.class ), nullable( VariableSpace.class ) );
      doReturn( JOB_ENTRY_JOB_NAME ).when( meta ).exportResources( nullable( Bowl.class ), nullable( Bowl.class ),
        nullable( JobMeta.class ), nullable( Map.class ), nullable( ResourceNamingInterface.class ),
        nullable( Repository.class ), nullable( IMetaStore.class ) );

      jej.exportResources( null, null, null, null, null, null, null );

      verify( meta ).setFilename( "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}/" + JOB_ENTRY_JOB_NAME );
      verify( jej ).setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    }
  }

  @Test
  public void testJobMetaInitializeVariablesFrom() throws Exception {
    try ( MockedConstruction<JobMeta> jobMetaMockedConstruction = mockConstruction( JobMeta.class );
          MockedConstruction<CurrentDirectoryResolver> currentDirectoryResolverMockedConstruction = mockConstruction(
            CurrentDirectoryResolver.class, ( mock, context ) ->
            {
              doCallRealMethod().when( mock ).normalizeSlashes( anyString() );
              doReturn( space ).when( mock ).resolveCurrentDirectory( any( Bowl.class ),
                any( ObjectLocationSpecificationMethod.class ),
                any( VariableSpace.class ), nullable( Repository.class ), nullable( Job.class ), anyString() );
            } ) ) {
      Repository myrepo = mock( Repository.class );
      JobMeta jobMeta = new JobMeta();
      doReturn( directory ).when( rdi ).findDirectory( nullable( String.class ) );
      doReturn( jobMeta ).when( myrepo ).loadJob( any(), any(), any(), any() );
      doReturn( rdi ).when( myrepo ).loadRepositoryDirectoryTree();
      doReturn( null ).when( myrepo ).getJobEntryAttributeString( any( ObjectId.class ), anyString() );
      doReturn( "rep_name" ).when( myrepo )
        .getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "specification_method" );
      doReturn( "${jobname}" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "name" );
      doReturn( "${repositorypath}" ).when( myrepo ).getJobEntryAttributeString( JOB_ENTRY_JOB_OBJECT_ID, "dir_path" );

      JobEntryJob jej = new JobEntryJob( JOB_ENTRY_JOB_NAME );
      jej.loadRep( myrepo, store, JOB_ENTRY_JOB_OBJECT_ID, databases, servers );
      jej.getJobMetaFromRepository( myrepo, resolver, "", space );

      verify( jobMeta, times( 1 ) ).initializeVariablesFrom( any() );
    }
  }

  private JobEntryJob getJej() {
    JobEntryJob jej = spy( new JobEntryJob( JOB_ENTRY_JOB_NAME ) );
    jej.setParentJobMeta( new JobMeta() );
    when( jej.getParentJobMeta().getNamedClusterEmbedManager() ).thenReturn( namedClusterEmbedManager );
    when( jej.getParentJobMeta().getBowl() ).thenReturn( DefaultBowl.getInstance() );
    return jej;
  }
}
