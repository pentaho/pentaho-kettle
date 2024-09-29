/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.starmodeler.metastore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.starmodeler.StarDomain;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class StarDomainMetaStoreUtilTest {
  private IMetaStore metaStore;
  private IMetaStoreElementType metaStoreElementType;

  @Before
  public void setUp() throws Exception {
    metaStore = mock( IMetaStore.class );
    metaStoreElementType = mock( IMetaStoreElementType.class );
    when( metaStore.getElementTypeByName( anyString(), eq( StarDomainMetaStoreUtil.METASTORE_STAR_DOMAIN_TYPE_NAME ) ) )
        .thenReturn( metaStoreElementType );
  }

  @Test
  public void testGetStarDomainElementType() throws Exception {
    final IMetaStoreElementType starDomainElementType = StarDomainMetaStoreUtil.getStarDomainElementType( metaStore );

    assertEquals( starDomainElementType, metaStoreElementType );
    verify( metaStore, times( 1 ) ).createNamespace( anyString() );
  }

  @Test
  public void testCreateStarDomainElementType() throws Exception {
    final IMetaStoreElementType metaStoreElementType = mock( IMetaStoreElementType.class );
    when( metaStore.newElementType( anyString() ) ).thenReturn( metaStoreElementType );

    final IMetaStoreElementType starDomainElementType = StarDomainMetaStoreUtil
        .createStarDomainElementType( metaStore );

    assertEquals( starDomainElementType, metaStoreElementType );
    verify( starDomainElementType, times( 1 ) ).setName( StarDomainMetaStoreUtil.METASTORE_STAR_DOMAIN_TYPE_NAME );
    verify( starDomainElementType, times( 1 ) )
        .setDescription( StarDomainMetaStoreUtil.METASTORE_STAR_DOMAIN_TYPE_DESCRIPTION );
    verify( metaStore, times( 1 ) ).createElementType( anyString(), eq( metaStoreElementType ) );
  }

  @Test
  public void testSaveStarDomain() throws Exception {
    final StarDomain starDomain = mock( StarDomain.class );
    final ObjectId objectId = mock( ObjectId.class );
    final String objectIdStr = "objectIdStr";
    when( objectId.toString() ).thenReturn( objectIdStr );
    when( starDomain.getObjectId() ).thenReturn( objectId );
    final String starDomainName = "starDomainName";
    when( starDomain.getName() ).thenReturn( starDomainName );

    final IMetaStoreElement metaStoreElement = mock( IMetaStoreElement.class );
    when( metaStoreElement.getId() ).thenReturn( "id" );
    when( metaStore.getElement( anyString(), eq( metaStoreElementType ), eq( objectIdStr ) ) )
        .thenReturn( metaStoreElement );

    StarDomainMetaStoreUtil.saveStarDomain( metaStore, starDomain );

    verify( metaStoreElement, times( 1 ) ).setElementType( eq( metaStoreElementType ) );
    verify( metaStoreElement, times( 1 ) ).setName( eq( starDomainName ) );
    verify( metaStoreElement, times( 1 ) ).addChild( any( IMetaStoreAttribute.class ) );
    verify( metaStore, times( 1 ) )
        .updateElement( anyString(), eq( metaStoreElementType ), eq( objectIdStr ), eq( metaStoreElement ) );
    verify( starDomain, times( 1 ) ).setObjectId( any( ObjectId.class ) );
  }

  @Test
  public void testGetStarDomainList() throws Exception {
    final IMetaStoreElement metaStoreElement = mock( IMetaStoreElement.class );
    final String id = "id";
    when( metaStoreElement.getId() ).thenReturn( id );
    final String name = "name";
    when( metaStoreElement.getName() ).thenReturn( name );
    final IMetaStoreAttribute metaStoreAttribute = mock( IMetaStoreAttribute.class );
    when( metaStoreElement.getChild( eq( StarDomainMetaStoreUtil.Attribute.ID_STAR_DOMAIN_DESCRIPTION.id ) ) )
        .thenReturn( metaStoreAttribute );
    when( metaStore.getElements( anyString(), eq( metaStoreElementType ) ) )
        .thenReturn( new LinkedList<IMetaStoreElement>() {
          {
            add( metaStoreElement );
          }
        } );

    final List<IdNameDescription> starDomainList = StarDomainMetaStoreUtil.getStarDomainList( metaStore );

    assertNotNull( starDomainList );
    assertEquals( 1, starDomainList.size() );
    final IdNameDescription result = starDomainList.get( 0 );
    assertEquals( id, result.getId() );
    assertEquals( name, result.getName() );
  }

  @Test
  public void testLoadStarDomain() throws Exception {
    final String id = "id";

    final String msName = "MSName";
    when( metaStore.getName() ).thenReturn( msName );
    final DelegatingMetaStore delegatingMetaStore = spy( new DelegatingMetaStore( metaStore ) );
    delegatingMetaStore.setActiveMetaStoreName( msName );
    doAnswer( new Answer<IMetaStoreElementType>() {
      @Override
      public IMetaStoreElementType answer( InvocationOnMock invocationOnMock ) throws Throwable {
        return metaStore.getElementTypeByName( (String) invocationOnMock.getArguments()[0],
            (String) invocationOnMock.getArguments()[1] );
      }
    } ).when( delegatingMetaStore ).getElementTypeByName( anyString(), anyString() );

    assertNull( StarDomainMetaStoreUtil.loadStarDomain( delegatingMetaStore, id ) );

    final IMetaStoreElement metaStoreElement = mock( IMetaStoreElement.class );
    final String name = "name";
    when( metaStoreElement.getName() ).thenReturn( name );
    doReturn( metaStoreElement ).when( delegatingMetaStore )
        .getElement( anyString(), eq( metaStoreElementType ), eq( id ) );

    final StarDomain starDomain = StarDomainMetaStoreUtil.loadStarDomain( delegatingMetaStore, id );

    assertEquals( id, starDomain.getObjectId().getId() );
    assertEquals( name, starDomain.getName() );
  }
}
