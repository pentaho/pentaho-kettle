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

package org.pentaho.di.repository.pur;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;

public class RepositoryProxyTest {

  private static final String DATABASE_ATTRIBUTE = "database";
  private static final String DATABASE_ID = "database-id";

  @Test
  public void returnsRepositoryDatabaseWhenNoLocalOverrideExists() throws Exception {
    DatabaseMeta repositoryDatabase = mockDatabase( DATABASE_ID, "repositoryDatabase" );
    Repository parentRepository = mockParentRepository( repositoryDatabase );

    RepositoryProxy proxy = new RepositoryProxy( databaseAttributeNode(), parentRepository );

    assertSame( repositoryDatabase,
      proxy.loadDatabaseMetaFromStepAttribute( new StringObjectId( "step-id" ), DATABASE_ATTRIBUTE,
        Collections.emptyList() ) );
  }

  @Test
  public void prefersSameNameLocalDatabaseOverride() throws Exception {
    DatabaseMeta repositoryDatabase = mockDatabase( DATABASE_ID, "sharedDatabase" );
    DatabaseMeta localOverride = mockDatabase( "local-database-id", "sharedDatabase" );
    Repository parentRepository = mockParentRepository( repositoryDatabase );

    RepositoryProxy proxy = new RepositoryProxy( databaseAttributeNode(), parentRepository );

    assertSame( localOverride,
      proxy.loadDatabaseMetaFromStepAttribute( new StringObjectId( "step-id" ), DATABASE_ATTRIBUTE,
        Collections.singletonList( localOverride ) ) );
  }

  private DataNode databaseAttributeNode() {
    DataNode node = new DataNode( "custom" );
    node.setProperty( DATABASE_ATTRIBUTE, new DataNodeRef( DATABASE_ID ) );
    return node;
  }

  private DatabaseMeta mockDatabase( String objectId, String name ) {
    DatabaseMeta database = mock( DatabaseMeta.class );
    when( database.getObjectId() ).thenReturn( new StringObjectId( objectId ) );
    when( database.getName() ).thenReturn( name );
    return database;
  }

  private Repository mockParentRepository( DatabaseMeta database ) throws Exception {
    Repository repository = mock( Repository.class );
    when( repository.getBowl() ).thenReturn( DefaultBowl.getInstance() );
    when( repository.loadDatabaseMeta( any( ObjectId.class ), isNull( String.class ) ) ).thenReturn( database );
    return repository;
  }
}
