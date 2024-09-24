/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.connections.ui.tree;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metastore.locator.api.MetastoreLocator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


public class ConnectionFolderProviderTest {
  private ConnectionFolderProvider connectionFolderProvider;
  private Spoon mockSpoon;
  private GUIResource mockGuiResource;
  private Repository mockRepository;
  private IUser mockUser;
  private MetastoreLocator mockMetastoreLocator;

  private MockedStatic<Spoon> spoonMockedStatic;
  private MockedStatic<GUIResource> guiResourceMockedStatic;
  @Before
  public void setup() {
    mockSpoon = mock( Spoon.class );
    mockGuiResource = mock( GUIResource.class );
    spoonMockedStatic = mockStatic( Spoon.class );
    guiResourceMockedStatic = mockStatic( GUIResource.class );
    when( Spoon.getInstance() ).thenReturn( mockSpoon );
    when( GUIResource.getInstance() ).thenReturn( mockGuiResource );
    mockRepository = mock( Repository.class );
    mockUser = mock( IUser.class );
    mockMetastoreLocator = mock( MetastoreLocator.class );
  }

  @After
  public void tearDown() {
    spoonMockedStatic.close();
    guiResourceMockedStatic.close();
  }

  @Test
  public void create_TestAdmin(){
    doReturn( mockRepository ).when( mockSpoon ).getRepository();
    doReturn( mockUser ).when( mockRepository ).getUserInfo();
    doReturn( true ).when( mockUser ).isAdmin();
    connectionFolderProvider = new ConnectionFolderProvider( mockMetastoreLocator );
    TreeNode parentTreeNode = new TreeNode();
    assertFalse( parentTreeNode.hasChildren() );

    connectionFolderProvider.create( null, parentTreeNode );
    assertTrue( parentTreeNode.hasChildren() );
    assertTrue( StringUtils.isNotBlank( connectionFolderProvider.getTitle() ) );
    assertNotNull( parentTreeNode.getChildren().stream().filter(
      tn -> connectionFolderProvider.getTitle().equals( tn.getLabel() )
    ).findAny().get());
  }
  @Test
  public void create_TestNonAdmin(){
    doReturn( mockRepository ).when( mockSpoon ).getRepository();
    doReturn( mockUser ).when( mockRepository ).getUserInfo();
    doReturn( false ).when( mockUser ).isAdmin();
    connectionFolderProvider = new ConnectionFolderProvider( mockMetastoreLocator );
    TreeNode parentTreeNode = new TreeNode();
    assertFalse( parentTreeNode.hasChildren() );

    connectionFolderProvider.create( null, parentTreeNode );
    assertFalse( parentTreeNode.hasChildren() );
  }

  @Test
  public void create_TestNullRepo(){
    doReturn( null ).when( mockSpoon ).getRepository();
    connectionFolderProvider = new ConnectionFolderProvider( mockMetastoreLocator );
    TreeNode parentTreeNode = new TreeNode();
    assertFalse( parentTreeNode.hasChildren() );

    connectionFolderProvider.create( null, parentTreeNode );
    assertTrue( parentTreeNode.hasChildren() );
    assertTrue( StringUtils.isNotBlank( connectionFolderProvider.getTitle() ) );
    assertNotNull( parentTreeNode.getChildren().stream().filter(
      tn -> connectionFolderProvider.getTitle().equals( tn.getLabel() )
    ).findAny().get());
  }

  @Test
  public void create_TestNullIsAdmin(){
    doReturn( mockRepository ).when( mockSpoon ).getRepository();
    doReturn( mockUser ).when( mockRepository ).getUserInfo();
    doReturn( null ).when( mockUser ).isAdmin();
    connectionFolderProvider = new ConnectionFolderProvider( mockMetastoreLocator );
    TreeNode parentTreeNode = new TreeNode();
    assertFalse( parentTreeNode.hasChildren() );

    connectionFolderProvider.create( null, parentTreeNode );
    assertTrue( parentTreeNode.hasChildren() );
    assertTrue( StringUtils.isNotBlank( connectionFolderProvider.getTitle() ) );
    assertNotNull( parentTreeNode.getChildren().stream().filter(
      tn -> connectionFolderProvider.getTitle().equals( tn.getLabel() )
    ).findAny().get());
  }

  @Test
  public void create_TestNullUser(){
    doReturn( mockRepository ).when( mockSpoon ).getRepository();
    doReturn( null ).when( mockRepository ).getUserInfo();
    connectionFolderProvider = new ConnectionFolderProvider( mockMetastoreLocator );
    TreeNode parentTreeNode = new TreeNode();
    assertFalse( parentTreeNode.hasChildren() );

    connectionFolderProvider.create( null, parentTreeNode );
    assertTrue( parentTreeNode.hasChildren() );
    assertTrue( StringUtils.isNotBlank( connectionFolderProvider.getTitle() ) );
    assertNotNull( parentTreeNode.getChildren().stream().filter(
      tn -> connectionFolderProvider.getTitle().equals( tn.getLabel() )
    ).findAny().get());
  }
}
