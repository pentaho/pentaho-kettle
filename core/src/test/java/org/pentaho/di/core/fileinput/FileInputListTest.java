/*******************************************************************************
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
package org.pentaho.di.core.fileinput;

import org.apache.commons.vfs2.FileObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by ccaspanello on 6/5/17.
 */
public class FileInputListTest {

  @Test
  public void testGetUrlStrings() throws Exception {

    String sFileA = "hdfs://myfolderA/myfileA.txt";
    String sFileB = "file:///myfolderB/myfileB.txt";

    FileObject fileA = mock( FileObject.class );
    FileObject fileB = mock( FileObject.class );

    when( fileA.getPublicURIString() ).thenReturn( sFileA );
    when( fileB.getPublicURIString() ).thenReturn( sFileB );

    FileInputList fileInputList = new FileInputList();
    fileInputList.addFile( fileA );
    fileInputList.addFile( fileB );
    String[] result = fileInputList.getUrlStrings();
    assertEquals( 2, result.length );
    assertEquals( sFileA, result[ 0 ] );
    assertEquals( sFileB, result[ 1 ] );
  }
}
