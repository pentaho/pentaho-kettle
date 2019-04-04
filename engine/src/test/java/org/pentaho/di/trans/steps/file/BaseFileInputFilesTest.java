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

package org.pentaho.di.trans.steps.file;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class BaseFileInputFilesTest {
  @Test
  public void testClone() {
    BaseFileInputFiles orig = new BaseFileInputFiles();
    orig.fileName = new String[] { "1", "2" };
    orig.fileMask = new String[] { "3", "4" };
    orig.excludeFileMask = new String[] { "5", "6" };
    orig.fileRequired = new String[] { "7", "8" };
    orig.includeSubFolders = new String[] { "9", "0" };

    BaseFileInputFiles clone = (BaseFileInputFiles) orig.clone();
    assertNotEquals( orig.fileName, clone.fileName );
    assertTrue( Arrays.equals( orig.fileName, clone.fileName ) );
    assertNotEquals( orig.fileMask, clone.fileMask );
    assertTrue( Arrays.equals( orig.fileMask, clone.fileMask ) );
    assertNotEquals( orig.excludeFileMask, clone.excludeFileMask );
    assertTrue( Arrays.equals( orig.excludeFileMask, clone.excludeFileMask ) );
    assertNotEquals( orig.fileRequired, clone.fileRequired );
    assertTrue( Arrays.equals( orig.fileRequired, clone.fileRequired ) );
    assertNotEquals( orig.includeSubFolders, clone.includeSubFolders );
    assertTrue( Arrays.equals( orig.includeSubFolders, clone.includeSubFolders ) );
  }
}
