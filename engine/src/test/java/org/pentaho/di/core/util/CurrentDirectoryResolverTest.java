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


package org.pentaho.di.core.util;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.mockito.MockedStatic;
import org.pentaho.di.core.vfs.KettleVFS;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CurrentDirectoryResolverTest {

  private CurrentDirectoryResolver resolver;

  @Mock
  private Bowl bowl;

  @Mock
  private VariableSpace parentVariables;

  @Mock
  private Repository repository;

  @Mock
  private RepositoryDirectoryInterface directory;

  @Mock
  private FileObject fileObject;

  @Mock
  private FileName fileName;

  @Mock
  private FileName parentFileName;

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  public static final String TEST_VARIABLE = "test.variable";

  @Before
  public void setUp() {
    when( parentVariables.environmentSubstitute( anyString() ) ).thenReturn( TEST_VARIABLE );
    when( parentVariables.listVariables() ).thenReturn( new String[] { TEST_VARIABLE } );
    when( parentVariables.getVariable( anyString() ) ).thenReturn( TEST_VARIABLE );
    resolver = new CurrentDirectoryResolver();
  }

  @Test
  public void resolveCurrentDirectory_WhenRepositoryDirectoryProvided_ThenSetsDirectoryVariables() {
    when( directory.toString() ).thenReturn( "/home/user/project" );

    VariableSpace result = resolver.resolveCurrentDirectory( bowl, parentVariables, directory, null );

    assertNotNull( result );
    assertEquals( "/home/user/project", result.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
    assertEquals( "/home/user/project", result.getVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY ) );
    assertEquals( "/home/user/project",
      result.getVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY ) );
  }

  @Test
  public void resolveCurrentDirectory_WhenOnlyFilenameProvided_ThenExtractsDirectoryFromFile() throws Exception {
    String filename = "/home/user/project/test.ktr";

    when( fileObject.exists() ).thenReturn( true );
    when( fileObject.getName() ).thenReturn( fileName );
    when( fileName.getBaseName() ).thenReturn( "test.ktr" );
    when( fileName.getParent() ).thenReturn( parentFileName );
    when( parentFileName.getURI() ).thenReturn( "file:///home/user/project" );

    VariableSpace result = resolver.resolveCurrentDirectory( bowl, parentVariables, null, filename );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenFileDoesNotExist_ThenReturnsVariableSpaceWithoutSettings() throws Exception {
    String filename = "/home/user/project/nonexistent.ktr";

    when( fileObject.exists() ).thenReturn( false );

    VariableSpace result = resolver.resolveCurrentDirectory( bowl, parentVariables, null, filename );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenDirectoryAndFilenameAreNull_ThenReturnsVariableSpace() {
    VariableSpace result = resolver.resolveCurrentDirectory( bowl, parentVariables, null, null );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenBothDirectoryAndFilenameProvided_ThenUsesDirectory() {
    when( directory.toString() ).thenReturn( "/repo/directory" );

    VariableSpace result = resolver.resolveCurrentDirectory( bowl, parentVariables, directory, "/some/file.ktr" );

    assertNotNull( result );
    assertEquals( "/repo/directory", result.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
  }

  @Test
  public void resolveCurrentDirectory_WhenFileProcessingThrowsException_ThenHandlesGracefully() {
    String filename = "/invalid/path";

    VariableSpace result = resolver.resolveCurrentDirectory( bowl, parentVariables, null, filename );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenStepMetaWithRepositoryByName_ThenUsesRepositoryDirectory() {
    StepMeta stepMeta = mock( StepMeta.class );
    TransMeta transMeta = mock( TransMeta.class );
    RepositoryDirectoryInterface repoDir = mock( RepositoryDirectoryInterface.class );

    when( stepMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( transMeta.getRepositoryDirectory() ).thenReturn( repoDir );
    when( repoDir.toString() ).thenReturn( "/repo/trans" );

    VariableSpace result = resolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME,
      parentVariables,
      repository,
      stepMeta,
      null
    );

    assertNotNull( result );
    assertEquals( "/repo/trans", result.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
  }

  @Test
  public void resolveCurrentDirectory_WhenStepMetaWithoutRepository_ThenUsesTransMetaFilename() {
    StepMeta stepMeta = mock( StepMeta.class );
    TransMeta transMeta = mock( TransMeta.class );

    when( stepMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( transMeta.getFilename() ).thenReturn( "/local/trans.ktr" );

    VariableSpace result = resolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME,
      parentVariables,
      null,
      stepMeta,
      null
    );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenStepMetaWithFilenameSpecification_ThenUsesRepositoryDirectory() {
    StepMeta stepMeta = mock( StepMeta.class );
    TransMeta transMeta = mock( TransMeta.class );
    RepositoryDirectoryInterface repoDir = mock( RepositoryDirectoryInterface.class );

    when( stepMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( transMeta.getRepositoryDirectory() ).thenReturn( repoDir );
    when( repoDir.toString() ).thenReturn( "/repo/trans" );

    VariableSpace result = resolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.FILENAME,
      parentVariables,
      repository,
      stepMeta,
      "/some/file.ktr"
    );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenStepMetaWithRootDirectory_ThenFallsBackToFilename() {
    StepMeta stepMeta = mock( StepMeta.class );
    TransMeta transMeta = mock( TransMeta.class );
    RepositoryDirectoryInterface rootDir = mock( RepositoryDirectoryInterface.class );

    when( stepMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( transMeta.getRepositoryDirectory() ).thenReturn( rootDir );
    when( rootDir.toString() ).thenReturn( "/" );
    when( transMeta.getFilename() ).thenReturn( "/local/trans.ktr" );

    VariableSpace result = resolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME,
      parentVariables,
      repository,
      stepMeta,
      null
    );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenStepMetaWithNullParentTransMeta_ThenReturnsVariableSpace() {
    StepMeta stepMeta = mock( StepMeta.class );

    when( stepMeta.getParentTransMeta() ).thenReturn( null );

    VariableSpace result = resolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME,
      parentVariables,
      repository,
      stepMeta,
      "/some/file.ktr"
    );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenJobWithRepositoryByName_ThenUsesRepositoryDirectory() {
    Job job = mock( Job.class );
    JobMeta jobMeta = mock( JobMeta.class );
    RepositoryDirectoryInterface repoDir = mock( RepositoryDirectoryInterface.class );

    when( job.getJobMeta() ).thenReturn( jobMeta );
    when( jobMeta.getRepositoryDirectory() ).thenReturn( repoDir );
    when( repoDir.toString() ).thenReturn( "/repo/job" );

    VariableSpace result = resolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME,
      parentVariables,
      repository,
      job,
      null
    );

    assertNotNull( result );
    assertEquals( "/repo/job", result.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
  }

  @Test
  public void resolveCurrentDirectory_WhenJobWithoutRepository_ThenUsesJobFilename() {
    Job job = mock( Job.class );

    when( job.getFilename() ).thenReturn( "/local/job.kjb" );

    VariableSpace result = resolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME,
      parentVariables,
      null,
      job,
      null
    );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenJobWithFilenameSpecification_ThenUsesRepositoryDirectory() {
    Job job = mock( Job.class );
    JobMeta jobMeta = mock( JobMeta.class );
    RepositoryDirectoryInterface repoDir = mock( RepositoryDirectoryInterface.class );

    when( job.getJobMeta() ).thenReturn( jobMeta );
    when( jobMeta.getRepositoryDirectory() ).thenReturn( repoDir );
    when( repoDir.toString() ).thenReturn( "/repo/job" );

    VariableSpace result = resolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.FILENAME,
      parentVariables,
      repository,
      job,
      "/some/file.kjb"
    );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenJobWithNullFilename_ThenUsesJobDefaultFilename() {
    Job job = mock( Job.class );

    when( job.getFilename() ).thenReturn( "/default/job.kjb" );

    VariableSpace result = resolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.FILENAME,
      parentVariables,
      repository,
      job,
      null
    );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenJobMetaAsParentVariables_ThenUsesRepositoryDirectory() {
    JobMeta jobMeta = mock( JobMeta.class );
    RepositoryDirectoryInterface repoDir = mock( RepositoryDirectoryInterface.class );

    when( jobMeta.getRepositoryDirectory() ).thenReturn( repoDir );
    when( jobMeta.environmentSubstitute( anyString() ) ).thenReturn( TEST_VARIABLE );
    when( jobMeta.listVariables() ).thenReturn( new String[] { TEST_VARIABLE } );
    when( jobMeta.getVariable( anyString() ) ).thenReturn( TEST_VARIABLE );
    when( repoDir.toString() ).thenReturn( "/repo/parent" );

    VariableSpace result = resolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME,
      jobMeta,
      repository,
      (Job) null,
      null
    );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenJobMetaAsParentVariablesNoRepository_ThenUsesJobFilename() {
    JobMeta jobMeta = mock( JobMeta.class );

    when( jobMeta.environmentSubstitute( anyString() ) ).thenReturn( TEST_VARIABLE );
    when( jobMeta.listVariables() ).thenReturn( new String[] { TEST_VARIABLE } );
    when( jobMeta.getVariable( anyString() ) ).thenReturn( TEST_VARIABLE );
    when( jobMeta.getFilename() ).thenReturn( "/local/job.kjb" );

    VariableSpace result = resolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.FILENAME,
      jobMeta,
      null,
      (Job) null,
      null
    );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenJobWithRootDirectory_ThenFallsBackToFilename() {
    Job job = mock( Job.class );
    JobMeta jobMeta = mock( JobMeta.class );
    RepositoryDirectoryInterface rootDir = mock( RepositoryDirectoryInterface.class );

    when( job.getJobMeta() ).thenReturn( jobMeta );
    when( jobMeta.getRepositoryDirectory() ).thenReturn( rootDir );
    when( rootDir.toString() ).thenReturn( "/" );
    when( jobMeta.getFilename() ).thenReturn( "/local/job.kjb" );

    VariableSpace result = resolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME,
      parentVariables,
      repository,
      job,
      null
    );

    assertNotNull( result );
  }

  @Test
  public void resolveCurrentDirectory_WhenJobWithNullJobMeta_ThenReturnsVariableSpace() {
    Job job = mock( Job.class );

    when( job.getJobMeta() ).thenReturn( null );

    VariableSpace result = resolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME,
      parentVariables,
      repository,
      job,
      "/some/file.kjb"
    );

    assertNotNull( result );
  }

  @Test
  public void normalizeSlashes_WhenBackslashesProvided_ThenConverts() {
    assertNormalizeSlashes( "C:\\Users\\test\\file.ktr", "C:/Users/test/file.ktr" );
    assertNormalizeSlashes( "C:\\Users//test\\\\file.ktr", "C:/Users/test/file.ktr" );
    assertNormalizeSlashes( "file://home//user//file.ktr", "file:/home/user/file.ktr" );
  }

  public void assertNormalizeSlashes( String input, String expected ) {
    String result = resolver.normalizeSlashes( input );
    assertEquals( expected, result );
  }

  @Test
  public void normalizeSlashes_WhenNullProvided_ThenReturnsNull() {
    String result = resolver.normalizeSlashes( null );

    assertNull( result );
  }

  @Test
  public void normalizeSlashes_WhenEmptyStringProvided_ThenReturnsEmptyString() {
    String result = resolver.normalizeSlashes( "" );

    assertEquals( "", result );
  }

  @Test
  public void normalizeSlashes_WhenBlankStringProvided_ThenReturnsBlankString() {
    String result = resolver.normalizeSlashes( "   " );

    assertEquals( "   ", result );
  }

  @Test
  public void normalizeSlashes_WhenAlreadyNormalizedPath_ThenReturnsUnchanged() {
    String input = "/home/user/file.ktr";
    String expected = "/home/user/file.ktr";

    String result = resolver.normalizeSlashes( input );

    assertEquals( expected, result );
  }

  @Test
  public void normalizeSlashes_WhenOnlySlashesProvided_ThenReturnsSingleSlash() {
    String input = "\\\\\\";
    String expected = "/";

    String result = resolver.normalizeSlashes( input );

    assertEquals( expected, result );
  }

  @Test
  public void resolveCurrentDirectory_WhenParentVariablesProvided_ThenInheritsVariables() {
    Variables parent = new Variables();
    parent.setVariable( "TEST_VAR", "test_value" );

    when( directory.toString() ).thenReturn( "/test/dir" );

    VariableSpace result = resolver.resolveCurrentDirectory( bowl, parent, directory, null );

    assertNotNull( result );
    assertEquals( "test_value", result.getVariable( "TEST_VAR" ) );
  }

  /**
   * Regression test for PDI-20828 / PR-10484 follow-up:
   *
   * When a job entry invokes a sub-transformation by FILENAME (no repository), the cleanup block in
   * resolveCurrentDirectory(Job) must NOT overwrite the provided child filename with the parent
   * job's filename.  Before the fix, this overwrote always, causing MetaFileLoaderImpl to seed the
   * wrong Internal.Entry.Current.Directory (parent's directory instead of the child's directory)
   * onto the loaded TransMeta.
   */
  @Test
  public void resolveCurrentDirectory_WhenFilenameSpecAndNoRepository_ThenPreservesChildFilenameNotParentJobFilename() {
    Job job = mock( Job.class );
    JobMeta jobMeta = mock( JobMeta.class );

    // Parent job lives in a different directory than the child transformation
    when( job.getJobMeta() ).thenReturn( jobMeta );
    when( jobMeta.getFilename() ).thenReturn( "file:///parent/dir/job.kjb" );

    String childFilename = "file:///parent/dir/subfolder/child.ktr";

    CurrentDirectoryResolver spyResolver = spy( resolver );

    spyResolver.resolveCurrentDirectory(
      bowl,
      ObjectLocationSpecificationMethod.FILENAME,
      parentVariables,
      null,       // no repository
      job,
      childFilename
    );

    // The 4-arg inner method must be called with the child's filename so that VFS can extract the
    // child's directory.  It must NOT be called with the parent job's filename.
    verify( spyResolver ).resolveCurrentDirectory( bowl, parentVariables, null, childFilename );
    verify( spyResolver, never() ).resolveCurrentDirectory( bowl, parentVariables, null, "file:///parent/dir/job.kjb" );
  }

  @Test
  public void resolveCurrentDirectory_WhenFilenameHasUnresolvedVariables_ThenSkipsFileExistenceCheck() {
    // Verifies that when a filename contains ${...} expressions (unresolved variables),
    // KettleVFS.getInstance() is never invoked. The fix returns tmpSpace early before
    // any VFS call, preventing premature directory resolution using the parent's context.

    String filenameWithUnresolvedVar = "/parent/dir/${Internal.Entry.Current.Directory}/child.ktr";
    Variables variables = new Variables();

    try ( MockedStatic<KettleVFS> mockedVFS = mockStatic( KettleVFS.class ) ) {
      VariableSpace result = resolver.resolveCurrentDirectory( bowl, variables, null, filenameWithUnresolvedVar );

      assertNotNull( result );
      // The core assertion: KettleVFS must never be touched when the filename has unresolved variables
      mockedVFS.verify( () -> KettleVFS.getInstance( bowl ), never() );
    }
  }

  @Test
  public void resolveCurrentDirectory_WhenFilenameHasUnresolvedWindowsVariables_ThenSkipsFileExistenceCheck() {
    String filenameWithUnresolvedVar = "/parent/dir/%%Internal.Entry.Current.Directory%%/child.ktr";
    Variables variables = new Variables();

    try ( MockedStatic<KettleVFS> mockedVFS = mockStatic( KettleVFS.class ) ) {
      VariableSpace result = resolver.resolveCurrentDirectory( bowl, variables, null, filenameWithUnresolvedVar );

      assertNotNull( result );
      mockedVFS.verify( () -> KettleVFS.getInstance( bowl ), never() );
    }
  }

  @Test
  public void resolveCurrentDirectory_WhenFilenameVariablesResolve_ThenUsesResolvedFilename() throws Exception {
    String filenameWithVariable = "${BASE_DIR}/child.ktr";
    String resolvedFilename = "/home/user/project/child.ktr";
    Variables variables = new Variables();
    variables.setVariable( "BASE_DIR", "/home/user/project" );

    IKettleVFS kettleVFS = mock( IKettleVFS.class );

    when( fileObject.exists() ).thenReturn( true );
    when( fileObject.getName() ).thenReturn( fileName );
    when( fileName.getBaseName() ).thenReturn( "child.ktr" );
    when( fileName.getParent() ).thenReturn( parentFileName );
    when( parentFileName.getURI() ).thenReturn( "file:///home/user/project" );

    try ( MockedStatic<KettleVFS> mockedVFS = mockStatic( KettleVFS.class ) ) {
      mockedVFS.when( () -> KettleVFS.getInstance( bowl ) ).thenReturn( kettleVFS );
      when( kettleVFS.getFileObject( eq( resolvedFilename ), any( VariableSpace.class ) ) ).thenReturn( fileObject );

      VariableSpace result = resolver.resolveCurrentDirectory( bowl, variables, null, filenameWithVariable );

      assertNotNull( result );
      assertEquals( "file:///home/user/project", result.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
      verify( kettleVFS ).getFileObject( eq( resolvedFilename ), any( VariableSpace.class ) );
    }
  }
}

