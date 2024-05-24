/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.base;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.CurrentDirectoryResolver;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.jobexecutor.JobExecutorMeta;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.pentaho.metastore.api.IMetaStore;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class MetaFileLoaderImplTest {
  private final String JOB_FILE = "one-step-job.kjb";
  private final String TRANS_FILE = "one-step-trans.ktr";

  private final Repository repository = mock( Repository.class );
  private final IMetaStore store = mock( IMetaStore.class );
  private VariableSpace space;
  private CurrentDirectoryResolver resolver = mock( CurrentDirectoryResolver.class );
  private RepositoryDirectoryInterface directory = mock( RepositoryDirectoryInterface.class );
  private final NamedClusterEmbedManager namedClusterEmbedManager = mock( NamedClusterEmbedManager.class );

  private String targetMetaName;
  private MetaFileCacheImpl metaFileCache;
  private JobEntryBase jobEntryBase;
  private BaseStepMeta baseStepMeta;
  private ObjectLocationSpecificationMethod specificationMethod;
  private String keyPath; //The absolute path used as part of the cachekey
  private String oldSystemParam;

  @Before
  public void setUp() throws Exception {
    oldSystemParam = System.getProperty( Const.KETTLE_USE_META_FILE_CACHE );
    System.setProperty( Const.KETTLE_USE_META_FILE_CACHE, "Y" );
  }

  @After
  public void tearDown() {
    System.setProperty( Const.KETTLE_USE_META_FILE_CACHE, null == oldSystemParam ? "" : oldSystemParam );
  }

  @Test
  //A job getting the JobMeta from the fileSystem
  public void getMetaForEntryAsJobFromFileSystemTest() throws Exception {
    setupJobEntryJob();
    specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    MetaFileLoaderImpl metaFileLoader = new MetaFileLoaderImpl<JobMeta>( jobEntryBase, specificationMethod );
    JobMeta jobMeta = (JobMeta) metaFileLoader.getMetaForEntry( repository, store, space );

    validateFirstJobMetaAccess( jobMeta );
    jobMeta = (JobMeta) metaFileLoader.getMetaForEntry( repository, store, space );
    validateSecondJobMetaAccess( jobMeta );
  }

  @Test
  //A job getting the JobMeta from the repo
  public void getMetaForEntryAsJobFromRepoTest() throws Exception {
    setupJobEntryJob();
    specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
    MetaFileLoaderImpl metaFileLoader = new MetaFileLoaderImpl<JobMeta>( jobEntryBase, specificationMethod );
    JobMeta jobMeta = (JobMeta) metaFileLoader.getMetaForEntry( repository, store, space );

    validateFirstJobMetaAccess( jobMeta );
    jobMeta = (JobMeta) metaFileLoader.getMetaForEntry( repository, store, space );
    validateSecondJobMetaAccess( jobMeta );
  }

  @Test
  //A job getting the TransMeta from the fileSystem
  public void getMetaForEntryAsTransFromFileSystemTest() throws Exception {
    setupJobEntryTrans();
    specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    MetaFileLoaderImpl metaFileLoader = new MetaFileLoaderImpl<TransMeta>( jobEntryBase, specificationMethod );
    TransMeta transMeta = (TransMeta) metaFileLoader.getMetaForEntry( repository, store, space );

    validateFirstTransMetaAccess( transMeta );
    transMeta = (TransMeta) metaFileLoader.getMetaForEntry( repository, store, space );
    validateSecondTransMetaAccess( transMeta );
  }

  @Test
  //A job getting the TransMeta from the repo
  public void getMetaForEntryAsTransFromRepoTest() throws Exception {
    setupJobEntryTrans();
    specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
    MetaFileLoaderImpl metaFileLoader = new MetaFileLoaderImpl<TransMeta>( jobEntryBase, specificationMethod );
    TransMeta transMeta = (TransMeta) metaFileLoader.getMetaForEntry( repository, store, space );

    validateFirstTransMetaAccess( transMeta );
    transMeta = (TransMeta) metaFileLoader.getMetaForEntry( repository, store, space );
    validateSecondTransMetaAccess( transMeta );
  }

  @Test
  //A Transformation getting the jobMeta from the filesystem
  public void getMetaForStepAsJobFromFileSystemTest() throws Exception {
    setupJobExecutorMeta();
    specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    MetaFileLoaderImpl metaFileLoader = new MetaFileLoaderImpl<JobMeta>( baseStepMeta, specificationMethod );
    JobMeta jobMeta = (JobMeta) metaFileLoader.getMetaForStep( repository, store, space );
    validateFirstJobMetaAccess( jobMeta );
    jobMeta = (JobMeta) metaFileLoader.getMetaForStep( repository, store, space );
    validateSecondJobMetaAccess( jobMeta );
  }

  @Test
  //A Transformation getting the jobMeta from the repo
  public void getMetaForStepAsJobFromRepoTest() throws Exception {
    setupJobExecutorMeta();
    specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
    MetaFileLoaderImpl metaFileLoader = new MetaFileLoaderImpl<JobMeta>( baseStepMeta, specificationMethod );
    JobMeta jobMeta = (JobMeta) metaFileLoader.getMetaForStep( repository, store, space );
    validateFirstJobMetaAccess( jobMeta );
    jobMeta = (JobMeta) metaFileLoader.getMetaForStep( repository, store, space );
    validateSecondJobMetaAccess( jobMeta );
  }

  @Test
  //A Transformation getting the TransMeta from the file system
  public void getMetaForStepAsTransFromFileSystemTest() throws Exception {
    setupTransExecutorMeta();
    specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    MetaFileLoaderImpl metaFileLoader = new MetaFileLoaderImpl<TransMeta>( baseStepMeta, specificationMethod );
    TransMeta transMeta = (TransMeta) metaFileLoader.getMetaForStep( repository, store, space );
    validateFirstTransMetaAccess( transMeta );
    transMeta = (TransMeta) metaFileLoader.getMetaForStep( repository, store, space );
    validateSecondTransMetaAccess( transMeta );
  }

  @Test
  //A Transformation getting the TransMeta from the repo
  public void getMetaForStepAsTransFromRepoTest() throws Exception {
    setupTransExecutorMeta();
    specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
    MetaFileLoaderImpl metaFileLoader = new MetaFileLoaderImpl<TransMeta>( baseStepMeta, specificationMethod );
    TransMeta transMeta = (TransMeta) metaFileLoader.getMetaForStep( repository, store, space );
    validateFirstTransMetaAccess( transMeta );
    transMeta = (TransMeta) metaFileLoader.getMetaForStep( repository, store, space );
    validateSecondTransMetaAccess( transMeta );
  }

  private void validateMetaName( String fileNameWithExtension, AbstractMeta meta ) {
    assertEquals( stripExtension( fileNameWithExtension ), meta.getName() );
  }

  private String stripExtension( String fileNameWithExtension ) {
    return fileNameWithExtension.substring( 0, fileNameWithExtension.lastIndexOf( "." ) );
  }

  private void setupJobEntryJob() throws Exception {
    JobEntryJob jobEntryJob = new JobEntryJob();
    jobEntryBase = jobEntryJob;
    JobMeta parentJobMeta = spy( new JobMeta() );
    LogChannelInterface logger = mock( LogChannelInterface.class );
    metaFileCache = new MetaFileCacheImpl( logger );
    parentJobMeta.setMetaFileCache( metaFileCache );
    jobEntryJob.setParentJobMeta( parentJobMeta );
    keyPath = getClass().getResource( JOB_FILE ).getPath();
    jobEntryJob.setFileName( keyPath );
    Job job = new Job();
    space = job;
    jobEntryJob.setParentJob( job );

    targetMetaName = JOB_FILE;
    jobEntryJob.setJobName( targetMetaName );

    //repo stuff
    keyPath = convertToRepoKeyPath( keyPath );
    jobEntryJob.setDirectory( keyPath.substring( 0, keyPath.lastIndexOf( "/" ) ) );
    jobEntryJob.setJobObjectId( null );
    RepositoryDirectoryInterface rdi = mock( RepositoryDirectoryInterface.class );
    when( rdi.findDirectory( jobEntryJob.getDirectory() ) ).thenReturn( rdi );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( rdi );
    JobMeta jobMeta = new JobMeta();
    jobMeta.setName( stripExtension( JOB_FILE ) );
    when( repository.loadJob( JOB_FILE, rdi, null, null ) ).thenReturn( jobMeta );
  }

  private String convertToRepoKeyPath( String fileKeyPath ) {
    String keyPath = fileKeyPath.replace( File.separator, "/" );
    return keyPath;
  }

  private void setupJobEntryTrans() throws Exception {
    JobEntryTrans jobEntryTrans = new JobEntryTrans();
    jobEntryBase = jobEntryTrans;
    JobMeta parentJobMeta = spy( new JobMeta() );
    LogChannelInterface logger = mock( LogChannelInterface.class );
    metaFileCache = new MetaFileCacheImpl( logger );
    parentJobMeta.setMetaFileCache( metaFileCache );
    jobEntryTrans.setParentJobMeta( parentJobMeta );
    keyPath = getClass().getResource( TRANS_FILE ).getPath();
    jobEntryTrans.setFileName( keyPath );
    Job job = new Job();
    space = job;
    jobEntryTrans.setParentJob( job );

    targetMetaName = TRANS_FILE;
    jobEntryTrans.setTransname( targetMetaName );
    jobEntryTrans.setTransObjectId( null );

    //repo stuff
    keyPath = convertToRepoKeyPath( keyPath );
    jobEntryTrans.setDirectory( keyPath.substring( 0, keyPath.lastIndexOf( "/" ) ) );
    jobEntryTrans.setTransObjectId( null );
    RepositoryDirectoryInterface rdi = mock( RepositoryDirectoryInterface.class );
    when( rdi.findDirectory( jobEntryTrans.getDirectory() ) ).thenReturn( rdi );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( rdi );
    TransMeta transMeta = new TransMeta();
    transMeta.setName( stripExtension( TRANS_FILE ) );
    when( repository.loadTransformation( TRANS_FILE, rdi, null, true, null ) ).thenReturn( transMeta );
  }

  private void setupJobExecutorMeta() throws Exception {
    JobExecutorMeta jobExecutorMeta = new JobExecutorMeta();
    baseStepMeta = jobExecutorMeta;
    TransMeta parentTransMeta = spy( new TransMeta() );
    LogChannelInterface logger = mock( LogChannelInterface.class );
    metaFileCache = new MetaFileCacheImpl( logger );
    parentTransMeta.setMetaFileCache( metaFileCache );
    StepMeta stepMeta = new StepMeta();
    stepMeta.setParentTransMeta( parentTransMeta );
    jobExecutorMeta.setParentStepMeta( stepMeta );
    keyPath = getClass().getResource( JOB_FILE ).getPath();
    jobExecutorMeta.setFileName( keyPath );
    Job job = new Job();
    space = job;

    targetMetaName = JOB_FILE;
    jobExecutorMeta.setJobName( targetMetaName );
    jobExecutorMeta.setJobObjectId( null );

    //repo stuff
    keyPath = convertToRepoKeyPath( keyPath );
    jobExecutorMeta.setDirectoryPath( keyPath.substring( 0, keyPath.lastIndexOf( "/" ) ) );
    jobExecutorMeta.setJobObjectId( null );
    RepositoryDirectoryInterface rdi = mock( RepositoryDirectoryInterface.class );
    when( repository.findDirectory( jobExecutorMeta.getDirectoryPath() ) ).thenReturn( rdi );
    JobMeta jobMeta = new JobMeta();
    jobMeta.setName( stripExtension( JOB_FILE ) );
    when( repository.loadJob( JOB_FILE, rdi, null, null ) ).thenReturn( jobMeta );
  }

  private void setupTransExecutorMeta() throws Exception {
    TransExecutorMeta transExecutorMeta = new TransExecutorMeta();
    baseStepMeta = transExecutorMeta;
    TransMeta parentTransMeta = spy( new TransMeta() );
    LogChannelInterface logger = mock( LogChannelInterface.class );
    metaFileCache = new MetaFileCacheImpl( logger );
    parentTransMeta.setMetaFileCache( metaFileCache );
    StepMeta stepMeta = new StepMeta();
    stepMeta.setParentTransMeta( parentTransMeta );
    transExecutorMeta.setParentStepMeta( stepMeta );
    keyPath = getClass().getResource( TRANS_FILE ).getPath();
    transExecutorMeta.setFileName( keyPath );
    space = new Job();

    targetMetaName = TRANS_FILE;
    transExecutorMeta.setTransName( targetMetaName );
    transExecutorMeta.setTransObjectId( null );

    //repo stuff
    keyPath = convertToRepoKeyPath( keyPath );
    transExecutorMeta.setDirectoryPath( keyPath.substring( 0, keyPath.lastIndexOf( "/" ) ) );
    transExecutorMeta.setTransObjectId( null );
    RepositoryDirectoryInterface rdi = mock( RepositoryDirectoryInterface.class );
    when( repository.findDirectory( transExecutorMeta.getDirectoryPath() ) ).thenReturn( rdi );
    TransMeta transMeta = new TransMeta();
    transMeta.setName( stripExtension( TRANS_FILE ) );
    when( repository.loadTransformation( TRANS_FILE, rdi, null, true, null ) ).thenReturn( transMeta );
  }

  private void validateFirstJobMetaAccess( JobMeta jobMeta ) {
    validateMetaName( JOB_FILE, jobMeta );
    //ensure it loaded the meta in the cache
    assertEquals( 1, metaFileCache.cacheMap.size() );
    String key = getKey();
    assertEquals( 0, metaFileCache.cacheMap.get( key ).getTimesUsed() );
    assertEquals( jobMeta, metaFileCache.cacheMap.get( key ).getMeta() );
    assertEquals( 1, metaFileCache.cacheMap.get( key ).getTimesUsed() );
  }

  private void validateSecondJobMetaAccess( JobMeta jobMeta ) {
    //If we get it again it should come from the cache
    validateMetaName( JOB_FILE, jobMeta );
    assertEquals( 2, metaFileCache.cacheMap.get( getKey() ).getTimesUsed() );
  }

  private void validateFirstTransMetaAccess( TransMeta transMeta ) {
    validateMetaName( TRANS_FILE, transMeta );
    //ensure it loaded the meta in the cache
    assertEquals( 1, metaFileCache.cacheMap.size() );
    String key = getKey();
    assertEquals( 0, metaFileCache.cacheMap.get( key ).getTimesUsed() );
    assertEquals( transMeta, metaFileCache.cacheMap.get( key ).getMeta() );
    assertEquals( 1, metaFileCache.cacheMap.get( key ).getTimesUsed() );
  }

  private void validateSecondTransMetaAccess( TransMeta transMeta ) {
    //If we get it again it should come from the cache
    validateMetaName( TRANS_FILE, transMeta );
    assertEquals( 2, metaFileCache.cacheMap.get( getKey() ).getTimesUsed() );
  }

  private String getKey() {
    return specificationMethod + ":" + keyPath;
  }
}
