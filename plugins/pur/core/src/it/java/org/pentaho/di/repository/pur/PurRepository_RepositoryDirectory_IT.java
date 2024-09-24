/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.di.repository.pur;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.trans.TransMeta;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by nbaker on 1/14/16.
 */
public class PurRepository_RepositoryDirectory_IT extends PurRepositoryTestBase {


  private TransMeta transMeta;
  private RepositoryDirectoryInterface defaultSaveDirectory;

  public PurRepository_RepositoryDirectory_IT( Boolean lazyRepo ) {
    super( lazyRepo );
  }

  @Before
  public void setup() throws Exception {

    transMeta = new TransMeta();
    transMeta.setName( "Test" );
    transMeta.setRepositoryDirectory( purRepository.getDefaultSaveDirectory( transMeta ) );
    final Calendar date = Calendar.getInstance();
    date.setTimeInMillis( 0 );
    purRepository.save( transMeta, "test", date, null, false );
    createPurRepository();

    defaultSaveDirectory = purRepository.getDefaultSaveDirectory( transMeta );
    purRepository.createRepositoryDirectory( defaultSaveDirectory, "test dir" );
    defaultSaveDirectory = purRepository.getDefaultSaveDirectory( transMeta );
  }
  @Test
  public void testGetRepositoryObjectsFirst() throws Exception {
    // Try it accessing getRepositoryObjects() first
    List<RepositoryElementMetaInterface> repositoryObjects = defaultSaveDirectory.getRepositoryObjects();
    assertEquals( 1, repositoryObjects.size() );
    assertEquals( "Test", repositoryObjects.get( 0 ).getName() );

  }
  @Test
  public void testGetChildrenFirst() throws Exception {
    // Try it again this time calling getChildren() then getRepositoryObjects()
    defaultSaveDirectory.getChildren();
    List<RepositoryElementMetaInterface> repositoryObjects = defaultSaveDirectory.getRepositoryObjects();
    assertEquals( 1, repositoryObjects.size() );
    assertEquals( "Test", repositoryObjects.get( 0 ).getName() );

  }


  @Test
  public void testGetChildrenThenGetSubDirectory() throws Exception {

    // Try it again this time calling getChildren() then getRepositoryObjects()
    defaultSaveDirectory.getChildren();
    defaultSaveDirectory.getSubdirectory( 0 );
    List<RepositoryDirectoryInterface> children = defaultSaveDirectory.getChildren();
    assertEquals( 1, children.size() );
    assertEquals( "test dir", children.get( 0 ).getName() );

  }
}
