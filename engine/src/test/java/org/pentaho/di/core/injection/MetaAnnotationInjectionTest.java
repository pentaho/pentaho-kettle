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

package org.pentaho.di.core.injection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.injection.bean.BeanInjectionInfo;
import org.pentaho.di.core.injection.bean.BeanInjector;
import org.pentaho.di.core.injection.inheritance.MetaBeanChild;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;

public class MetaAnnotationInjectionTest {

  private static final String FIELD_ONE = "FIELD_ONE";

  private static final String COMPLEX_NAME = "COMPLEX_NAME";

  private static final String TEST_NAME = "TEST_NAME";

  @Before
  public void before() {
    KettleLogStore.init();
  }

  @Test
  public void testInjectionDescription() throws Exception {

    BeanInjectionInfo ri = new BeanInjectionInfo( MetaBeanLevel1.class );

    assertEquals( 3, ri.getGroups().size() );
    assertEquals( "", ri.getGroups().get( 0 ).getName() );
    assertEquals( "FILENAME_LINES", ri.getGroups().get( 1 ).getName() );
    assertEquals( "FILENAME_LINES2", ri.getGroups().get( 2 ).getName() );

    assertTrue( ri.getProperties().containsKey( "SEPARATOR" ) );
    assertTrue( ri.getProperties().containsKey( "FILENAME" ) );
    assertTrue( ri.getProperties().containsKey( "BASE" ) );
    assertTrue( ri.getProperties().containsKey( "FIRST" ) );

    assertEquals( "FILENAME_LINES", ri.getProperties().get( "FILENAME" ).getGroupName() );
    assertEquals( "!DESCRIPTION!", ri.getDescription( "DESCRIPTION" ) );
  }

  @Test
  public void testInjectionSets() throws Exception {
    MetaBeanLevel1 obj = new MetaBeanLevel1();

    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "f1" ) );
    meta.addValueMeta( new ValueMetaString( "f2" ) );
    meta.addValueMeta( new ValueMetaString( "fstrint" ) );
    meta.addValueMeta( new ValueMetaString( "fstrlong" ) );
    meta.addValueMeta( new ValueMetaString( "fstrboolean" ) ); // TODO STLOCALE
    List<RowMetaAndData> rows = new ArrayList<>();
    rows.add( new RowMetaAndData( meta, "<sep>", "/tmp/file.txt", "123", "1234567891213", "y" ) );
    rows.add( new RowMetaAndData( meta, "<sep>", "/tmp/file2.txt", "123", "1234567891213", "y" ) );

    BeanInjector inj = buildBeanInjectorFor( MetaBeanLevel1.class );
    inj.setProperty( obj, "SEPARATOR", rows, "f1" );
    inj.setProperty( obj, "FILENAME", rows, "f2" );
    inj.setProperty( obj, "FILENAME_ARRAY", rows, "f2" );
    inj.setProperty( obj, "FBOOLEAN", rows, "fstrboolean" );
    inj.setProperty( obj, "FINT", rows, "fstrint" );
    inj.setProperty( obj, "FLONG", rows, "fstrlong" );
    inj.setProperty( obj, "FIRST", rows, "fstrint" );

    assertEquals( "<sep>", obj.getSub().getSeparator() );
    assertEquals( "/tmp/file.txt", obj.getSub().getFiles()[0].getName() );
    assertTrue( obj.fboolean );
    assertEquals( 123, obj.fint );
    assertEquals( 1234567891213L, obj.flong );
    assertEquals( "123", obj.getSub().first() );
    assertArrayEquals( new String[] { "/tmp/file.txt", "/tmp/file2.txt" }, obj.getSub().getFilenames() );
  }

  @Test
  public void testInjectionConstant() throws Exception {
    MetaBeanLevel1 obj = new MetaBeanLevel1();

    BeanInjector inj = buildBeanInjectorFor( MetaBeanLevel1.class );
    inj.setProperty( obj, "SEPARATOR", null, "<sep>" );
    inj.setProperty( obj, "FINT", null, "123" );
    inj.setProperty( obj, "FLONG", null, "1234567891213" );
    inj.setProperty( obj, "FBOOLEAN", null, "true" );
    inj.setProperty( obj, "FILENAME", null, "f1" );
    inj.setProperty( obj, "FILENAME_ARRAY", null, "f2" );

    assertEquals( "<sep>", obj.getSub().getSeparator() );
    assertTrue( obj.fboolean );
    assertEquals( 123, obj.fint );
    assertEquals( 1234567891213L, obj.flong );
    assertNull( obj.getSub().getFiles() );
    assertNull( obj.getSub().getFilenames() );

    obj.getSub().files = new MetaBeanLevel3[] { new MetaBeanLevel3(), new MetaBeanLevel3() };
    obj.getSub().filenames = new String[] { "", "", "" };
    inj.setProperty( obj, "FILENAME", null, "f1" );
    inj.setProperty( obj, "FILENAME_ARRAY", null, "f2" );
    assertEquals( 2, obj.getSub().getFiles().length );
    assertEquals( "f1", obj.getSub().getFiles()[0].getName() );
    assertEquals( "f1", obj.getSub().getFiles()[1].getName() );
    assertArrayEquals( new String[] { "f2", "f2", "f2" }, obj.getSub().getFilenames() );
  }

  @Test
  public void testInjectionForArrayPropertyWithoutDefaultConstructor_class_parameter() throws KettleException {
    BeanInjector beanInjector = buildBeanInjectorFor( MetadataBean.class );
    MetadataBean targetBean = new MetadataBean();
    beanInjector.setProperty( targetBean, COMPLEX_NAME, createRowMetaAndData(), FIELD_ONE );

    assertNotNull( targetBean.getComplexField() );
    assertTrue( targetBean.getComplexField().length == 1 );
    assertEquals( TEST_NAME, targetBean.getComplexField()[0].getFieldName() );
  }

  @Test
  public void testInjectionForArrayPropertyWithoutDefaultConstructor_interface_parameter() throws KettleException {
    BeanInjector beanInjector = buildBeanInjectorFor( MetadataBeanImplementsInterface.class );
    MetadataBeanImplementsInterface targetBean = new MetadataBeanImplementsInterface();
    beanInjector.setProperty( targetBean, COMPLEX_NAME, createRowMetaAndData(), FIELD_ONE );

    assertNotNull( targetBean.getComplexField() );
    assertTrue( targetBean.getComplexField().length == 1 );
    assertEquals( TEST_NAME, targetBean.getComplexField()[0].getFieldName() );
  }

  @Test
  public void testWrongDeclarations() throws Exception {
    try {
      new BeanInjectionInfo( MetaBeanWrong1.class );
      fail();
    } catch ( Exception ex ) {
    }
    try {
      new BeanInjectionInfo( MetaBeanWrong2.class );
      fail();
    } catch ( Exception ex ) {
    }
    try {
      new BeanInjectionInfo( MetaBeanWrong3.class );
      fail();
    } catch ( Exception ex ) {
    }
    try {
      new BeanInjectionInfo( MetaBeanWrong4.class );
      fail();
    } catch ( Exception ex ) {
    }
    try {
      new BeanInjectionInfo( MetaBeanWrong5.class );
      fail();
    } catch ( Exception ex ) {
    }
    try {
      new BeanInjectionInfo( MetaBeanWrong6.class );
      fail();
    } catch ( Exception ex ) {
    }
    try {
      new BeanInjectionInfo( MetaBeanWrong7.class );
      fail();
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }
  }

  @Test
  public void testGenerics() throws Exception {
    BeanInjectionInfo ri = new BeanInjectionInfo( MetaBeanChild.class );

    assertTrue( ri.getProperties().size() == 7 );
    assertTrue( ri.getProperties().containsKey( "BASE_ITEM_NAME" ) );
    assertTrue( ri.getProperties().containsKey( "ITEM_CHILD_NAME" ) );
    assertTrue( ri.getProperties().containsKey( "A" ) );
    assertTrue( ri.getProperties().containsKey( "ITEM.BASE_ITEM_NAME" ) );
    assertTrue( ri.getProperties().containsKey( "ITEM.ITEM_CHILD_NAME" ) );
    assertTrue( ri.getProperties().containsKey( "SUB.BASE_ITEM_NAME" ) );
    assertTrue( ri.getProperties().containsKey( "SUB.ITEM_CHILD_NAME" ) );

    assertEquals( String.class, ri.getProperties().get( "A" ).getPropertyClass() );
  }

  private static BeanInjector buildBeanInjectorFor( Class<?> clazz ) {
    BeanInjectionInfo metaBeanInfo = new BeanInjectionInfo( clazz );
    return new BeanInjector( metaBeanInfo );
  }

  private static List<RowMetaAndData> createRowMetaAndData() {
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( FIELD_ONE ) );
    return Collections.singletonList( new RowMetaAndData( meta, TEST_NAME ) );
  }

  private static interface MetadataInterface {
  }

  @InjectionSupported( localizationPrefix = "", groups = "COMPLEX" )
  public static class MetadataBean {

    @InjectionDeep
    private ComplexField[] complexField;

    public ComplexField[] getComplexField() {
      return complexField;
    }

    public void setComplexField( ComplexField[] complexField ) {
      this.complexField = complexField;
    }
  }

  public static class ComplexField {

    @Injection( name = "COMPLEX_NAME", group = "COMPLEX" )
    private String fieldName;

    private final MetadataBean parentMeta;

    public ComplexField( MetadataBean parentMeta ) {
      this.parentMeta = parentMeta;
    }

    public String getFieldName() {
      return fieldName;
    }

    public void setFieldName( String fieldName ) {
      this.fieldName = fieldName;
    }

    public MetadataBean getParentMeta() {
      return parentMeta;
    }
  }

  @InjectionSupported( localizationPrefix = "", groups = "COMPLEX" )
  public static class MetadataBeanImplementsInterface implements MetadataInterface {

    @InjectionDeep
    private ComplexFieldWithInterfaceArg[] complexField;

    public ComplexFieldWithInterfaceArg[] getComplexField() {
      return complexField;
    }

    public void setComplexField( ComplexFieldWithInterfaceArg[] complexField ) {
      this.complexField = complexField;
    }
  }

  public static class ComplexFieldWithInterfaceArg {

    @Injection( name = "COMPLEX_NAME", group = "COMPLEX" )
    private String fieldName;

    private final MetadataInterface parentMeta;

    public ComplexFieldWithInterfaceArg( MetadataInterface parentMeta ) {
      this.parentMeta = parentMeta;
    }

    public String getFieldName() {
      return fieldName;
    }

    public void setFieldName( String fieldName ) {
      this.fieldName = fieldName;
    }

    public MetadataInterface getParentMeta() {
      return parentMeta;
    }
  }

}
