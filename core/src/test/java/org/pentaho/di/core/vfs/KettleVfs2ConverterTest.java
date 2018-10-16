/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.vfs;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

public class KettleVfs2ConverterTest {
  @Test
  public void normalizeFilePath_NullFilePath() {
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( null, "None", "*.*", "*.*" );
    assertNull( normalizeFilePath );
  }

  @Test
  public void normalizeFilePath_EmptyString() {
    String filePath  = "file:///some/path/to/no/where/someFile.txt";
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( filePath, "", "*.*", "*.*" );
    assertEquals( filePath, normalizeFilePath );
  }

  @Test
  public void normalizeFilePath_None() {
    String filePath  = "file:///some/path/to/no/where/someFile.txt";
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( filePath, "None", "*.*", "*.*" );
    assertEquals( filePath, normalizeFilePath );
  }

  @Test
  public void normalizeFilePath_ZipSingleFile() {
    String filePath  = "file:///some/path/to/no/where/someFile.zip";
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( filePath, "Zip", "", "" );
    assertEquals( "zip:" + filePath, normalizeFilePath );
  }

  @Test
  public void normalizeFilePath_ZipDirectory() {
    String filePath  = "file:///some/path/to/no/where/someFile.zip";
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( filePath, "Zip", "*.*", "" );
    assertEquals( "zip:" + filePath, normalizeFilePath );
  }

  @Test
  public void normalizeFilePath_GZipSingleFile() {
    String filePath  = "file:///some/path/to/no/where/someFile.txt.gz";
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( filePath, "GZip", "", "" );
    assertEquals( "gz:" + filePath, normalizeFilePath );
  }

  @Test
  public void normalizeFilePath_GZipSingleFile2() {
    String filePath  = "file:///some/path/to/no/where/someFile.txt.gz";
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( filePath, "GZip", null,  null );
    assertEquals( "gz:" + filePath, normalizeFilePath );
  }

  @Test
  public void normalizeFilePath_GZipDirectory() {
    String filePath  = "file:///some/path/to/no/where/someFile.tar.gz";
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( filePath, "GZip", "*.*", "" );
    assertEquals( "tgz:" + filePath, normalizeFilePath );
  }

  @Test
  public void normalizeFilePath_Snappy() {
    String filePath  = "file:///some/path/to/no/where/someFile.sz";
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( filePath, "Snappy", "*.*", "*.*" );
    assertEquals( filePath, normalizeFilePath );
  }

  @Test
  public void normalizeFilePath_HadoopSnappy() {
    String filePath  = "file:///some/path/to/no/where/someFile.hsz";
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( filePath, "Hadoop-Snappy", "*.*", "*.*" );
    assertEquals( filePath, normalizeFilePath );
  }

  @Test
  public void normalizeFilePath_PrefixPresentZip() {
    String filePath  = "zip:file:///some/path/to/no/where/someFile.zip";
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( filePath, "Zip", "*.*", "*.*" );
    assertEquals( filePath, normalizeFilePath );
  }

  @Test
  public void normalizeFilePath_PrefixPresentGZipFile() {
    String filePath  = "gz:file:///some/path/to/no/where/someFile.txt.gz";
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( filePath, "GZip", "*.*", "*.*" );
    assertEquals( filePath, normalizeFilePath );
  }

  @Test
  public void normalizeFilePath_PrefixPresentGZipDirectory() {
    String filePath  = "tgz:file:///some/path/to/no/where/someFile.tar.gz";
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( filePath, "GZip", "*.*", "*.*" );
    assertEquals( filePath, normalizeFilePath );
  }

  @Test
  public void normalizeFilePath_PrefixPresentGZipDirectory2() {
    String filePath  = "tar:gz:file:///some/path/to/no/where/someFile.tar.gz";
    String normalizeFilePath = KettleVfs2Converter.normalizeFilePath( filePath, "GZip", "*.*", "*.*" );
    assertEquals( filePath, normalizeFilePath );
  }
}
