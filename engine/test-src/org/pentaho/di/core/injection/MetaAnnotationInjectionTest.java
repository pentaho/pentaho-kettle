/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.injection.bean.BeanInjectionInfo;
import org.pentaho.di.core.injection.bean.BeanInjector;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;

public class MetaAnnotationInjectionTest {

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
  }

  @Test
  public void testInjectionSets() throws Exception {
    BeanInjectionInfo ri = new BeanInjectionInfo( MetaBeanLevel1.class );

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

    BeanInjector inj = new BeanInjector( ri );
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
    assertEquals( new String[] { "/tmp/file.txt", "/tmp/file2.txt" }, obj.getSub().getFilenames() );
  }
}
