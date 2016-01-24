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

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.pentaho.di.repository.pur.TransDelegate.NODE_TRANS_PRIVATE_DATABASES;
import static org.pentaho.di.repository.pur.JobDelegate.NODE_JOB_PRIVATE_DATABASES;

/**
 * @author Andrey Khayrutdinov
 */
@RunWith( Parameterized.class )
public class DelegatesPrivateDatabasesTest {

  private static final String DB_NAME = "privateDatabase";

  @Parameterized.Parameters
  public static List<Object[]> getData() {
    Repository repository = mock( Repository.class );
    IUnifiedRepository pur = mock( IUnifiedRepository.class );

    Object[] trans = { new TransDelegate( repository, pur ), new TransMeta(), NODE_TRANS_PRIVATE_DATABASES };
    Object[] job = { new JobDelegate( repository, pur ), new JobMeta(), NODE_JOB_PRIVATE_DATABASES };
    return Arrays.asList( trans, job );
  }

  private final ITransformer delegate;
  private final AbstractMeta meta;
  private final String privateDbsNodeName;

  public DelegatesPrivateDatabasesTest( ITransformer delegate, AbstractMeta meta, String privateDbsNodeName ) {
    this.delegate = delegate;
    this.meta = meta;
    this.privateDbsNodeName = privateDbsNodeName;
  }

  @Test
  public void savesNode_IfSetIsNotEmpty() throws Exception {
    meta.setPrivateDatabases( Collections.singleton( DB_NAME ) );

    DataNode dataNode = element2node();

    DataNode dbsNode = dataNode.getNode( privateDbsNodeName );
    assertNotNull( dbsNode );

    DataNode databaseNode = Iterables.getFirst( dbsNode.getNodes(), null );
    assertNotNull( databaseNode );
    assertEquals( DB_NAME, databaseNode.getName() );
  }

  @Test
  public void doesNotSaveNode_IfSetIsNull() throws Exception {
    meta.setPrivateDatabases( null );

    DataNode dataNode = element2node();

    DataNode dbsNode = dataNode.getNode( privateDbsNodeName );
    assertNull( dbsNode );
  }

  @Test
  public void savesNode_IfSetIsEmpty() throws Exception {
    meta.setPrivateDatabases( Collections.<String> emptySet() );

    DataNode dataNode = element2node();

    DataNode dbsNode = dataNode.getNode( privateDbsNodeName );
    assertNotNull( "Even if the set is empty, the node should be saved as an indicator", dbsNode );

    DataNode databaseNode = Iterables.getFirst( dbsNode.getNodes(), null );
    assertNull( databaseNode );
  }

  private DataNode element2node() throws KettleException {
    DataNode dataNode = delegate.elementToDataNode( meta );
    assertNotNull( dataNode );
    return dataNode;
  }

  @Test
  public void saveAndLoad_SetIsNotEmpty() throws Exception {
    meta.setPrivateDatabases( Collections.singleton( DB_NAME ) );

    AbstractMeta restored = (AbstractMeta) delegate.dataNodeToElement( delegate.elementToDataNode( meta ) );

    assertEquals( meta.getPrivateDatabases(), restored.getPrivateDatabases() );
  }

  @Test
  public void saveAndLoad_SetIsEmpty() throws Exception {
    meta.setPrivateDatabases( Collections.<String> emptySet() );

    AbstractMeta restored = (AbstractMeta) delegate.dataNodeToElement( delegate.elementToDataNode( meta ) );

    assertNotNull( restored.getPrivateDatabases() );
    assertTrue( restored.getPrivateDatabases().isEmpty() );
  }

  @Test
  public void saveAndLoad_SetIsNull() throws Exception {
    meta.setPrivateDatabases( null );

    AbstractMeta restored = (AbstractMeta) delegate.dataNodeToElement( delegate.elementToDataNode( meta ) );

    assertNull( restored.getPrivateDatabases() );
  }
}
