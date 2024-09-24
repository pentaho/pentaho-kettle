/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.plugins.fileopensave.api.overwrite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.pentaho.di.plugins.fileopensave.api.progress.FileCopyProgressDialog;

import static org.junit.Assert.*;

public class OverwriteStatusTest {
  private final String DUMMY_FILE = "dummyfile.kjb";
  private final String FILE_TYPE = "file";
  OverwriteStatus overwriteStatus;
  @Mock
  FileCopyProgressDialog mockFileCopyProgressDialog;

  @Before
  public void setUp() throws Exception {
    //Can only test the non-ui aspects
    overwriteStatus = new OverwriteStatus( null );
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void setAndGetFileCopyProgressDialog() {
    overwriteStatus.setFileCopyProgressDialog( mockFileCopyProgressDialog );
    assertEquals( mockFileCopyProgressDialog, overwriteStatus.getFileCopyProgressDialog() );
  }

  @Test
  public void setAndGetisApplyToAll() {
    assertFalse( overwriteStatus.isApplyToAll() );
    overwriteStatus.setApplyToAll( true );
    assertTrue( overwriteStatus.isApplyToAll() );
  }

  @Test
  public void getandSetOverwriteMode() {
    assertEquals( OverwriteStatus.OverwriteMode.NONE, overwriteStatus.getOverwriteMode() );
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.RENAME );
    assertEquals( OverwriteStatus.OverwriteMode.RENAME, overwriteStatus.getOverwriteMode() );
  }

  @Test
  public void testHappyPath() {
    //Next Line does nothing with null shell, but would have prompted for duplicate file otherwise.  This is
    // the form used when running with a null shell, OR, you just want to prompt the duplicate dialog unconditionally
    // for whatever reason.
    overwriteStatus.promptOverwriteIfNecessary( DUMMY_FILE, FILE_TYPE );
    //Next line simulates the user pressing skip
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.SKIP );
    // We would check the value when it comes back and do whatever needs done, in this case skip the file
    assertTrue( overwriteStatus.isSkip() );
    assertEquals( OverwriteStatus.OverwriteMode.SKIP, overwriteStatus.getOverwriteMode() );
    // The next imaginary file copied is not a duplicate but we still need to make the call to reset the
    // SKIP we stored earlier.  We'll use the normal form here.  In this form we specify whether the file
    // is a duplicate or not so the overwrite logic can do it thing.  The dialog would not have shown because
    // we sent that the flle is not a duplicate.
    overwriteStatus.promptOverwriteIfNecessary( false, DUMMY_FILE, FILE_TYPE );
    // So we check the status again
    assertTrue( overwriteStatus.isNone() );
    assertFalse( overwriteStatus.isSkip() );
    assertEquals( OverwriteStatus.OverwriteMode.NONE, overwriteStatus.getOverwriteMode() );
    // Next file, this one will be a duplicate, the next line would have prompted for the duplicate if
    // we had a shell.
    overwriteStatus.promptOverwriteIfNecessary( true, DUMMY_FILE, FILE_TYPE );
    // this will simulate the user hitting rename and checking the ApplyToAll
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.RENAME );
    overwriteStatus.setApplyToAll( true );
    //Now we check the value again
    assertTrue( overwriteStatus.isRename() );
    assertFalse( overwriteStatus.isSkip() );
    assertEquals( OverwriteStatus.OverwriteMode.RENAME, overwriteStatus.getOverwriteMode() );
    // We said to rename all duplicates.  There should be no more prompting.  Next file is not a duplicate
    overwriteStatus.promptOverwriteIfNecessary( false, DUMMY_FILE, FILE_TYPE );
    //It's still rename because setApplyToAll was checked, If we didn't check the applyToAll it would have
    //come back as NONE.
    assertTrue( overwriteStatus.isRename() );
  }

  @Test
  public void isOverwrite() {
    assertFalse( overwriteStatus.isOverwrite() );
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.OVERWRITE );
    assertTrue( overwriteStatus.isOverwrite() );
  }

  @Test
  public void isSkip() {
    assertFalse( overwriteStatus.isSkip() );
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.SKIP );
    assertTrue( overwriteStatus.isSkip() );
  }

  @Test
  public void isRename() {
    assertFalse( overwriteStatus.isRename() );
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.RENAME );
    assertTrue( overwriteStatus.isRename() );
  }

  @Test
  public void isCancel() {
    assertFalse( overwriteStatus.isCancel() );
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.CANCEL );
    assertTrue( overwriteStatus.isCancel() );
  }

  @Test
  public void isNone() {
    assertTrue( overwriteStatus.isNone() );
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.SKIP );
    assertFalse( overwriteStatus.isNone() );
  }

  @Test
  public void setOverwriteMode() {
    assertFalse( overwriteStatus.isOverwrite() );
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.OVERWRITE );
    assertTrue( overwriteStatus.isOverwrite() );
  }

  @Test
  @SuppressWarnings( "squid:S2699" )
  public void activateProgressDialog() {
    //Can't really test this without a shell, just make sure it does not error
    overwriteStatus.activateProgressDialog( "title" );
  }

  @Test
  @SuppressWarnings( "squid:S2699" )
  public void setCurrentFileInProgressDialog() {
    //Can't really test this without a shell, just make sure it does not error
    overwriteStatus.setCurrentFileInProgressDialog( DUMMY_FILE );
  }

  @Test
  public void reset() {
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.OVERWRITE );
    assertTrue( overwriteStatus.isOverwrite() );
    overwriteStatus.reset();
    assertFalse( overwriteStatus.isOverwrite() );
    assertTrue( overwriteStatus.isNone() );
  }
}