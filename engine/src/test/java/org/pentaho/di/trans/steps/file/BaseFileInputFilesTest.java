/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
