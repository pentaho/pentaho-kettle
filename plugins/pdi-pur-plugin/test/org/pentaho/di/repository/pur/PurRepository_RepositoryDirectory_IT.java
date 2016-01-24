/*!
 * Copyright 2016 Pentaho Corporation.  All rights reserved.
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

import org.junit.Test;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.trans.TransMeta;

import java.util.Calendar;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by nbaker on 1/14/16.
 */
public class PurRepository_RepositoryDirectory_IT extends PurRepositoryTestBase {


  @Test
  public void testGetRepositoryObjects() throws Exception {


    TransMeta transMeta = new TransMeta();
    transMeta.setName( "Test" );
    transMeta.setRepositoryDirectory( purRepository.getDefaultSaveDirectory( transMeta ) );
    final Calendar date = Calendar.getInstance();
    date.setTimeInMillis( 0 );
    purRepository.save( transMeta, "test", date, null, false );

    // Try it accessing getRepositoryObjects() first
    createPurRepository();
    RepositoryDirectoryInterface defaultSaveDirectory = purRepository.getDefaultSaveDirectory( transMeta );
    List<RepositoryElementMetaInterface> repositoryObjects = defaultSaveDirectory.getRepositoryObjects();
    assertEquals( 1, repositoryObjects.size() );
    assertEquals( "Test", repositoryObjects.get( 0 ).getName() );

    // Try it again this time calling getChildren() then getRepositoryObjects()
    createPurRepository();
    defaultSaveDirectory = purRepository.getDefaultSaveDirectory( transMeta );
    defaultSaveDirectory.getChildren();
    repositoryObjects = defaultSaveDirectory.getRepositoryObjects();
    assertEquals( 1, repositoryObjects.size() );
    assertEquals( "Test", repositoryObjects.get( 0 ).getName() );

    //    assertNull( "Should return null if file was not found", information );
  }
}
