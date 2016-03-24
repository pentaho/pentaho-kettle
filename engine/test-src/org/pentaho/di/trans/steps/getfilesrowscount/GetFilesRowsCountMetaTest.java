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

package org.pentaho.di.trans.steps.getfilesrowscount;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class GetFilesRowsCountMetaTest {

  @Test
  public void testClone() throws Exception {
    GetFilesRowsCountMeta meta = new GetFilesRowsCountMeta();
    meta.allocate( 2 );
    meta.setFileName( new String[] { "field1", "field2" } );
    meta.setFileMask( new String[] { "mask1", "mask2" } );
    meta.setFileRequired( new String[] { "Y", "Y" } );
    meta.setIncludeSubFolders( new String[] { "N", "N" } );
    meta.setExcludeFileMask( new String[] { "excludemask1", "excludemask2" } );

    GetFilesRowsCountMeta cloned = (GetFilesRowsCountMeta) meta.clone();
    assertFalse( cloned.getFileName() == meta.getFileName() );
    assertTrue( Arrays.equals( cloned.getFileName(), meta.getFileName() ) );
    assertFalse( cloned.getFileMask() == meta.getFileMask() );
    assertTrue( Arrays.equals( cloned.getFileMask(), meta.getFileMask() ) );
    assertFalse( cloned.getFileRequired() == meta.getFileRequired() );
    assertTrue( Arrays.equals( cloned.getFileRequired(), meta.getFileRequired() ) );
    assertFalse( cloned.getIncludeSubFolders() == meta.getIncludeSubFolders() );
    assertTrue( Arrays.equals( cloned.getIncludeSubFolders(), meta.getIncludeSubFolders() ) );
    assertFalse( cloned.getExludeFileMask() == meta.getExludeFileMask() );
    assertTrue( Arrays.equals( cloned.getExludeFileMask(), meta.getExludeFileMask() ) );
  }
}
