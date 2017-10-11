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

package org.pentaho.di.job.entries.zipfile;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.util.Assert;
import java.util.regex.Pattern;

public class ZipJobEntryPatternFileSelectorTest {

  private FileSelector fileSelector;
  private FileSelectInfo fileSelectInfoMock;
  private FileObject fileObjectMock;
  FileName fileNameMock;

  private static final String PATTERN = "^.*\\.(txt)$";
  private static final String PATTERN_FILE_NAME = "do-not-open.txt";
  private static final String EXCLUDE_PATTERN = "^.*\\.(sh)$";
  private static final String EXCLUDE_PATTERN_FILE_NAME = "performance-boost.sh";

  @Before
  public void init() throws FileSystemException {
    fileSelectInfoMock = Mockito.mock( FileSelectInfo.class );
    fileSelector = new JobEntryZipFile
      .ZipJobEntryPatternFileSelector( Pattern.compile( PATTERN ), Pattern.compile( EXCLUDE_PATTERN ) );
    fileObjectMock = Mockito.mock( FileObject.class );
    fileNameMock = Mockito.mock( FileName.class );

    Mockito.when( fileSelectInfoMock.getFile() ).thenReturn( fileObjectMock );
    Mockito.when( fileObjectMock.getType() ).thenReturn( FileType.FILE );
    Mockito.when( fileObjectMock.getName() ).thenReturn( fileNameMock );
    Mockito.when( fileNameMock.getBaseName() ).thenReturn( PATTERN_FILE_NAME );

  }

  @Test
  public void testPatternNull() throws Exception {
    fileSelector = new JobEntryZipFile.ZipJobEntryPatternFileSelector( null, Pattern.compile( EXCLUDE_PATTERN ) );
    boolean includeFile = fileSelector.includeFile( fileSelectInfoMock );
    Assert.assertTrue( includeFile );

    Mockito.when( fileNameMock.getBaseName() ).thenReturn( EXCLUDE_PATTERN_FILE_NAME );
    includeFile = fileSelector.includeFile( fileSelectInfoMock );
    Assert.assertFalse( includeFile );
  }

  @Test
  public void testExcludePatternNull() throws Exception {
    fileSelector = new JobEntryZipFile.ZipJobEntryPatternFileSelector( Pattern.compile( PATTERN ), null );
    boolean includeFile = fileSelector.includeFile( fileSelectInfoMock );
    Assert.assertTrue( includeFile );

    Mockito.when( fileNameMock.getBaseName() ).thenReturn( EXCLUDE_PATTERN_FILE_NAME );
    includeFile = fileSelector.includeFile( fileSelectInfoMock );
    Assert.assertFalse( includeFile );
  }

  @Test
  public void testPatternAndExcludePatternNull() throws Exception {
    fileSelector = new JobEntryZipFile.ZipJobEntryPatternFileSelector( null, null );
    boolean includeFile = fileSelector.includeFile( fileSelectInfoMock );
    Assert.assertTrue( includeFile );

    Mockito.when( fileNameMock.getBaseName() ).thenReturn( EXCLUDE_PATTERN_FILE_NAME );
    includeFile = fileSelector.includeFile( fileSelectInfoMock );
    Assert.assertTrue( includeFile );
  }

  @Test
  public void testMatchesPattern() throws Exception {
    boolean includeFile = fileSelector.includeFile( fileSelectInfoMock );
    Assert.assertTrue( includeFile );
  }

  @Test
  public void testMatchesExcludePattern() throws Exception {
    Mockito.when( fileNameMock.getBaseName() ).thenReturn( EXCLUDE_PATTERN_FILE_NAME );
    boolean includeFile = fileSelector.includeFile( fileSelectInfoMock );
    Assert.assertFalse( includeFile );
  }

  @Test
  public void testMatchesPatternAndExcludePattern() throws Exception {
    fileSelector =
      new JobEntryZipFile.ZipJobEntryPatternFileSelector( Pattern.compile( PATTERN ), Pattern.compile( PATTERN ) );
    boolean includeFile = fileSelector.includeFile( fileSelectInfoMock );
    Assert.assertFalse( includeFile );
  }

  @Test
  public void testDifferentFileType() throws Exception {
    Mockito.when( fileObjectMock.getType() ).thenReturn( FileType.IMAGINARY );
    boolean includeFile = fileSelector.includeFile( fileSelectInfoMock );
    Assert.assertFalse( includeFile );
  }
}
