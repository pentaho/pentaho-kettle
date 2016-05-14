/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.datagrid;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DataGridMetaTest {

  @Test
  public void cloneTest() throws Exception {
    DataGridMeta meta = new DataGridMeta();
    meta.allocate( 2 );
    meta.setFieldName( new String[] { "aa", "bb" } );
    meta.setFieldType( new String[] { "cc", "dd" } );
    meta.setFieldFormat( new String[] { "ee", "ff" } );
    meta.setCurrency( new String[] { "gg", "hh" } );
    meta.setDecimal( new String[] { "ii", "jj" } );
    meta.setGroup( new String[] { "kk", "ll" } );
    meta.setEmptyString( new boolean[] { false, true } );
    meta.setFieldLength( new int[] { 10, 50 } );
    meta.setFieldPrecision( new int[] { 3, 5 } );
    List<List<String>> dataLinesList = new ArrayList<List<String>>();
    for ( int i = 0; i < 3; i++ ) {
      List<String> dl = new ArrayList<String>();
      dl.add( "line" + ( ( i * 2 ) + 1 ) );
      dl.add( "line" + ( ( i * 2 ) + 2 ) );
      dl.add( "line" + ( ( i * 2 ) + 3 ) );
      dataLinesList.add( dl );
    }
    meta.setDataLines( dataLinesList );
    DataGridMeta aClone = (DataGridMeta) meta.clone();
    assertFalse( aClone == meta );
    assertTrue( Arrays.equals( meta.getFieldName(), aClone.getFieldName() ) );
    assertTrue( Arrays.equals( meta.getFieldType(), aClone.getFieldType() ) );
    assertTrue( Arrays.equals( meta.getFieldFormat(), aClone.getFieldFormat() ) );
    assertTrue( Arrays.equals( meta.getCurrency(), aClone.getCurrency() ) );
    assertTrue( Arrays.equals( meta.getDecimal(), aClone.getDecimal() ) );
    assertTrue( Arrays.equals( meta.getGroup(), aClone.getGroup() ) );
    assertTrue( Arrays.equals( meta.isSetEmptyString(), aClone.isSetEmptyString() ) );
    assertTrue( Arrays.equals( meta.getFieldLength(), aClone.getFieldLength() ) );
    assertTrue( Arrays.equals( meta.getFieldPrecision(), aClone.getFieldPrecision() ) );
    List<List<String>> clonedDataLinesList = aClone.getDataLines();
    assertEquals( meta.getDataLines().size(), clonedDataLinesList.size() );
    for ( int i = 0; i < dataLinesList.size(); i++ ) {
      List<String> metaList = dataLinesList.get( i );
      List<String> cloneList = clonedDataLinesList.get( i );
      assertEquals( metaList.size(), cloneList.size() );
      assertFalse( metaList == cloneList );
      for ( int j = 0; j < metaList.size(); j++ ) {
        assertEquals( metaList.get( j ), cloneList.get( j ) );
      }
    }
    assertEquals( meta.getXML(), aClone.getXML() );
  }
}
