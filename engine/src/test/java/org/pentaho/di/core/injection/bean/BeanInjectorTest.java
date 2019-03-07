/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.injection.bean;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.MetaBeanLevel1;
import org.pentaho.di.core.injection.MetaBeanLevel2;
import org.pentaho.di.core.logging.KettleLogStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BeanInjectorTest {

  @Before
  public void before() {
    // NOTE: needed to init BeanInjectionInfo
    KettleLogStore.init();
  }

  @Test
  public void allocateCollectionField_List() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    // should set other field based on this size
    mbl1.getSub().setFilenames( new String[] {"one", "two", "three", "four"} );
    assertNull( mbl1.getSub().getAscending() );

    bi.allocateCollectionField( mbl1.getSub(), bii, "ASCENDING_LIST" );
    assertEquals(4, mbl1.getSub().getAscending().size() );
  }

  @Test
  public void allocateCollectionField_Array() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    // should set other field based on this size
    mbl1.getSub().setAscending( Arrays.asList( new Boolean[] { true, false } ) );
    assertNull( mbl1.getSub().getFilenames() );

    bi.allocateCollectionField( mbl1.getSub(), bii, "FILENAME_ARRAY"  );
    assertEquals(2, mbl1.getSub().getFilenames().length );
  }

  @Test
  public void allocateCollectionField_NonCollection() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    // sizes of other fields shouldn't change
    assertNull( mbl1.getSub().getFilenames() );
    assertNull( mbl1.getSub().getAscending() );

    bi.allocateCollectionField( mbl1.getSub(), bii, "SEPARATOR"  );
    assertNull( mbl1.getSub().getFilenames() );
    assertNull( mbl1.getSub().getAscending() );
  }

  @Test
  public void allocateCollectionField_Property_Array_IntiallyNull() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    BeanInjectionInfo.Property arrayProperty = bii.getProperties().values().stream()
      .filter( p -> p.getName().equals( "FILENAME_ARRAY" ) ).findFirst().orElse( null );

    assertNull( mbl1.getSub().getFilenames() );
    bi.allocateCollectionField( arrayProperty,  mbl1.getSub(), 7);

    assertEquals( 7, mbl1.getSub().getFilenames().length );
  }

  @Test
  public void allocateCollectionField_Property_Array_IntiallyEmpty() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    mbl1.getSub().setFilenames( new String[]{ /* empty on purpose */ } );
    BeanInjectionInfo.Property arrayProperty = bii.getProperties().values().stream()
      .filter( p -> p.getName().equals( "FILENAME_ARRAY" ) ).findFirst().orElse( null );

    assertEquals(0, mbl1.getSub().getFilenames().length);
    bi.allocateCollectionField( arrayProperty,  mbl1.getSub(), 7);

    assertEquals( 7, mbl1.getSub().getFilenames().length );
  }

  @Test
  public void allocateCollectionField_Property_List_IntiallyNull() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    BeanInjectionInfo.Property listProperty = bii.getProperties().values().stream()
      .filter( p -> p.getName().equals( "ASCENDING_LIST"  ) ).findFirst().orElse( null );

    assertNull( mbl1.getSub().getAscending() );
    bi.allocateCollectionField( listProperty,  mbl1.getSub(), 6);

    assertEquals( 6,mbl1.getSub().getAscending().size() );
  }

  @Test
  public void allocateCollectionField_Property_List_IntiallyEmpty() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    mbl1.getSub().setAscending( new ArrayList<>() );
    BeanInjectionInfo.Property listProperty = bii.getProperties().values().stream()
      .filter( p -> p.getName().equals( "ASCENDING_LIST" ) ).findFirst().orElse( null );

    assertEquals(0, mbl1.getSub().getAscending().size() );
    bi.allocateCollectionField( listProperty,  mbl1.getSub(), 6);

    assertEquals( 6, mbl1.getSub().getAscending().size() );
  }

  @Test
  public void isCollection_True() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    BeanInjectionInfo.Property collectionProperty = bii.getProperties().values().stream()
      .filter( p -> p.getName().equals( "FILENAME_ARRAY") ).findFirst().orElse( null );

    assertTrue( bi.isCollection( collectionProperty  ) );
  }

  @Test
  public void isCollection_False() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    BeanInjectionInfo.Property seperatorProperty = bii.getProperties().values().stream()
      .filter( p -> p.getName().equals( "SEPARATOR" ) ).findFirst().orElse( null );

    assertFalse( bi.isCollection( seperatorProperty  ) );
  }

  @Test
  public void isCollection_BeanLevelInfo() {
    BeanInjector bi = new BeanInjector(null );

    BeanLevelInfo bli_list = new BeanLevelInfo();
    bli_list.dim = BeanLevelInfo.DIMENSION.LIST;

    assertTrue( bi.isCollection( bli_list ));

    BeanLevelInfo bli_array = new BeanLevelInfo();
    bli_array.dim = BeanLevelInfo.DIMENSION.ARRAY;

    assertTrue( bi.isCollection( bli_array ));

    BeanLevelInfo bli_none = new BeanLevelInfo();
    bli_list.dim = BeanLevelInfo.DIMENSION.NONE;

    assertFalse( bi.isCollection( bli_none ));
  }

  @Test
  public void isArray() {
    BeanInjector bi = new BeanInjector(null );

    BeanLevelInfo bli_list = new BeanLevelInfo();
    bli_list.dim = BeanLevelInfo.DIMENSION.LIST;

    assertFalse( bi.isArray( bli_list ));

    BeanLevelInfo bli_array = new BeanLevelInfo();
    bli_array.dim = BeanLevelInfo.DIMENSION.ARRAY;

    assertTrue( bi.isArray( bli_array ));

    BeanLevelInfo bli_none = new BeanLevelInfo();
    bli_list.dim = BeanLevelInfo.DIMENSION.NONE;

    assertFalse( bi.isArray( bli_none ));
  }

  @Test
  public void isList() {
    BeanInjector bi = new BeanInjector(null );

    BeanLevelInfo bli_list = new BeanLevelInfo();
    bli_list.dim = BeanLevelInfo.DIMENSION.LIST;

    assertTrue( bi.isList( bli_list ));

    BeanLevelInfo bli_array = new BeanLevelInfo();
    bli_array.dim = BeanLevelInfo.DIMENSION.ARRAY;

    assertFalse( bi.isList( bli_array ));

    BeanLevelInfo bli_none = new BeanLevelInfo();
    bli_list.dim = BeanLevelInfo.DIMENSION.NONE;

    assertFalse( bi.isList( bli_none ));
  }

  @Test
  public void getFinalPath_Null() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    BeanInjectionInfo.Property noPathProperty = bii. new Property("name", "groupName",
        Collections.EMPTY_LIST );

    assertNull(bi.getFinalPath(  noPathProperty ) );
  }

  @Test
  public void getFinalPath_Found() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    BeanInjectionInfo.Property seperatorProperty = bii.getProperties().values().stream()
        .filter( p -> p.getName().equals( "SEPARATOR" ) ).findFirst().orElse( null );

    assertEquals( seperatorProperty.getPath().get( 2 ), bi.getFinalPath(  seperatorProperty ) );
  }

  @Test
  public void getProperty_Found() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    BeanInjectionInfo.Property actualProperty = bi.getProperty( bii, "SEPARATOR" );

    assertNotNull(actualProperty);
    assertEquals("SEPARATOR", actualProperty.getName() );
  }

  @Test
  public void getProperty_NotFound() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    BeanInjectionInfo.Property actualProperty = bi.getProperty( bii, "DOES_NOT_EXIST" );

    assertNull(actualProperty);
  }

  @Test
  public void getGroupProperties_NonEmptyGroup() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    mbl1.getSub().setFilenames( new String[] { "file1", "file2", "file3"} );
    mbl1.getSub().setAscending( Arrays.asList( new Boolean[] { true, false, false, true} ) );
    mbl1.getSub().setSeparator( "/" );

    List<BeanInjectionInfo.Property> actualProperties = bi.getGroupProperties( bii, "FILENAME_LINES2"  );
    assertNotNull( actualProperties );
    assertEquals(2, actualProperties.size() );
    assertNotNull( bii.getProperties().values().stream().filter( p -> p.getName().equals( "ASCENDING_LIST" ) ) );
    assertNotNull( bii.getProperties().values().stream().filter( p -> p.getName().equals( "FILENAME_ARRAY" ) ) );
  }

  @Test
  public void getGroupProperties_GroupDoesNotExist() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    mbl1.getSub().setFilenames( new String[] { "file1", "file2", "file3"} );
    mbl1.getSub().setAscending( Arrays.asList( new Boolean[] { true, false, false, true} ) );
    mbl1.getSub().setSeparator( "/" );

    List<BeanInjectionInfo.Property> actualProperties = bi.getGroupProperties( bii, "GLOBAL_DOES_NOT_EXIST"  );
    assertNotNull( actualProperties );
    assertEquals(0, actualProperties.size() );
  }

  @Test
  public void getMaxSize_Collections() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    mbl1.getSub().setFilenames( new String[] { "file1", "file2", "file3"} );
    mbl1.getSub().setAscending( Arrays.asList( new Boolean[] { true, false, false, true} ) );
    mbl1.getSub().setSeparator( "/" );

    assertEquals(new Integer(4 ), bi.getMaxSize( bii.getProperties().values(), mbl1.getSub() ) );
  }

  @Test
  public void getMaxSize_OnlyOneField() {
    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    mbl1.getSub().setSeparator( "/" );

    assertEquals(new Integer(1 ), bi.getMaxSize( bii.getProperties().values(), mbl1.getSub() ) );
  }

  @Test
  public void getCollectionSize_Property_Array() throws Exception {

    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    mbl1.getSub().setFilenames( new String[] { "file1", "file2", "file3"} );
    BeanInjectionInfo.Property property = bii.getProperties().values().stream()
        .filter( p -> p.getName().equals( "FILENAME_ARRAY" )  ).findFirst().orElse( null );

    assertEquals(3, bi.getCollectionSize( property, mbl1.getSub() ) );
  }

  @Test
  public void getCollectionSize_Property_List() throws Exception {

    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    mbl1.getSub().setAscending( Arrays.asList( new Boolean[] { true, false, false, true} ) );
    BeanInjectionInfo.Property property = bii.getProperties().values().stream()
      .filter( p -> p.getName().equals( "ASCENDING_LIST" )  ).findFirst().orElse( null );

    assertEquals(4, bi.getCollectionSize( property, mbl1.getSub() ) );
  }

  @Test
  public void getCollectionSize_BeanLevelInfo_Exception() throws Exception {
    BeanInjector bi = new BeanInjector(null );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    mbl1.getSub().setAscending( Arrays.asList( new Boolean[] { true, false, false, true} ) );

    BeanLevelInfo mockBeanLevelInfo = mock(BeanLevelInfo.class);

    doThrow( new RuntimeException( "SOME ERROR" ) ).when( mockBeanLevelInfo).getField();

    assertEquals(-1, bi.getCollectionSize( mockBeanLevelInfo, mbl1 ) );
  }

  @Test
  public void getCollectionSize_Property_NonCollection() throws Exception {

    BeanInjector bi = new BeanInjector(null );
    BeanInjectionInfo bii = new BeanInjectionInfo( MetaBeanLevel1.class );
    MetaBeanLevel1 mbl1 = new MetaBeanLevel1();
    mbl1.setSub( new MetaBeanLevel2() );
    mbl1.getSub().setAscending( Arrays.asList( new Boolean[] { true, false, false, true} ) );
    BeanInjectionInfo.Property property = bii.getProperties().values().stream()
      .filter( p -> p.getName().equals( "SEPARATOR" )  ).findFirst().orElse( null );

    assertEquals(-1, bi.getCollectionSize( property, mbl1.getSub() ) );
  }

}