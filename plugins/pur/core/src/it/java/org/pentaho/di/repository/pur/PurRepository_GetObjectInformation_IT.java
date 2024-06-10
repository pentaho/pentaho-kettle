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
package org.pentaho.di.repository.pur;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Test;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;

import java.io.Serializable;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class PurRepository_GetObjectInformation_IT extends PurRepositoryTestBase {

  public PurRepository_GetObjectInformation_IT( Boolean lazyRepo ) {
    super( lazyRepo );
  }

  @Test
  public void getObjectInformation_IsDeletedFlagSet_Job() throws Exception {
    testDeletedFlagForObject( new Callable<RepositoryElementInterface>() {
      @Override
      public RepositoryElementInterface call() throws Exception {
        JobMeta jobMeta = new JobMeta();
        jobMeta.setName( "testJobMeta" );
        return jobMeta;
      }
    } );
  }

  @Test
  public void getObjectInformation_IsDeletedFlagSet_Trans() throws Exception {
    testDeletedFlagForObject( new Callable<RepositoryElementInterface>() {
      @Override
      public RepositoryElementInterface call() throws Exception {
        TransMeta transMeta = new TransMeta();
        transMeta.setName( "testTransMeta" );
        return transMeta;
      }
    } );
  }

  private void testDeletedFlagForObject( Callable<RepositoryElementInterface> elementProvider ) throws Exception {
    TransDelegate transDelegate = new TransDelegate( purRepository, unifiedRepository );
    JobDelegate jobDelegate = new JobDelegate( purRepository, unifiedRepository );
    FieldUtils.writeField( purRepository, "transDelegate", transDelegate, true );
    FieldUtils.writeField( purRepository, "jobDelegate", jobDelegate, true );

    RepositoryElementInterface element = elementProvider.call();
    RepositoryDirectoryInterface directory = purRepository.findDirectory( element.getRepositoryDirectory().getPath() ).findDirectory( "public" );
    element.setRepositoryDirectory( directory );

    purRepository.save( element, null, null );
    assertNotNull( "Element was saved", element.getObjectId() );

    RepositoryObject information;
    information = purRepository.getObjectInformation( element.getObjectId(), element.getRepositoryElementType() );
    assertNotNull( information );
    assertFalse( information.isDeleted() );

    purRepository.deleteTransformation( element.getObjectId() );
    assertNotNull( "Element was moved to Trash", unifiedRepository.getFileById( element.getObjectId().getId() ) );

    information = purRepository.getObjectInformation( element.getObjectId(), element.getRepositoryElementType() );
    assertNotNull( information );
    assertTrue( information.isDeleted() );
  }

  @Test
  public void getObjectInformation_InvalidRepositoryId_ExceptionIsHandled() throws Exception {
    IUnifiedRepository unifiedRepository = mock( IUnifiedRepository.class );
    when( unifiedRepository.getFileById( any( Serializable.class ) ) ).thenThrow( new RuntimeException( "unknown id" ) );
    purRepository.setTest( unifiedRepository );

    RepositoryObject information = purRepository.getObjectInformation( new StringObjectId( "invalid id" ),
        RepositoryObjectType.JOB );
    assertNull( "Should return null if file was not found", information );
  }

  @Test
  public void getObjectInformation_InvalidRepositoryId_NullIsHandled() throws Exception {
    IUnifiedRepository unifiedRepository = mock( IUnifiedRepository.class );
    when( unifiedRepository.getFileById( any( Serializable.class ) ) ).thenReturn( null );
    purRepository.setTest( unifiedRepository );

    RepositoryObject information = purRepository.getObjectInformation( new StringObjectId( "invalid id" ),
        RepositoryObjectType.JOB );
    assertNull( "Should return null if file was not found", information );
  }
}
