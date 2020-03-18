/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entry;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.util.StringUtil;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JobEntryBaseTest {

  private static final String DATE_PATTERN = "_[0-9]{8}";
  private static final String TIME_PATTERN = "_[0-9]{9}";
  private static final String DATE_TIME_PATTERN = "[(][0-9]{8}_[0-9]{9}[)]";
  private static final String EXTENSION = ".zip";
  private static final String REGEX_EXTENSION = regexDotEscape( EXTENSION );


  /**
   * PDI-10553 - log output add/delete filenames to/from result steps shows CheckDb connections
   */
  @Test
  public void testIdIsNullByDefault() {
    JobEntryBase base = new JobEntryBase();
    Assert.assertNull( "Object ID is null by default", base.getObjectId() );
  }

  @Test
  public void testAddDatetimeToFilename_zipWithoutDotsInFolderWithoutDots() {
    JobEntryBase jobEntryBase = new JobEntryBase();
    String fullFilename;
    String filename = "/folder_without_dots/zip_without_dots_in_folder_without_dots";

    // add nothing
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, false, null, false, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( filename, fullFilename ) );
    // add date
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, "yyyyMMdd", false, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( filename + DATE_PATTERN, fullFilename ) );
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, null, false, null, false, null );
    assertNotNull( fullFilename );
    assertEquals( filename, fullFilename );
    // add time
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, false, null, true, "HHmmssSSS", false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( filename + TIME_PATTERN, fullFilename ) );
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, false, null, true, null, false, null );
    assertNotNull( fullFilename );
    assertEquals( filename, fullFilename );
    // add date and time
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, "yyyyMMdd", true, "HHmmssSSS", false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( filename + DATE_PATTERN + TIME_PATTERN, fullFilename ) );
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, null, true, "HHmmssSSS", false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( filename + TIME_PATTERN, fullFilename ) );
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, "yyyyMMdd", true, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( filename + DATE_PATTERN, fullFilename ) );
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, null, true, null, false, null );
    assertNotNull( fullFilename );
    assertEquals( filename, fullFilename );
    // add datetime
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename, false, null, false, null, true, "(yyyyMMdd_HHmmssSSS)" );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( filename + DATE_TIME_PATTERN, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename, false, null, false, null, true, null );
    assertNotNull( fullFilename );
    assertEquals( filename, fullFilename );
  }

  @Test
  public void testAddDatetimeToFilename_ZipWithoutDotsInFolderWithDots() {
    JobEntryBase jobEntryBase = new JobEntryBase();
    String fullFilename;
    String filename = "/folder.with.dots/zip_without_dots_in_folder_with_dots";
    String regexFilename = regexDotEscape( filename );

    // add nothing
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, false, null, false, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename, fullFilename ) );
    // add date
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, "yyyyMMdd", false, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + DATE_PATTERN, fullFilename ) );
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, null, false, null, false, null );
    assertNotNull( fullFilename );
    assertEquals( filename, fullFilename );
    // add time
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, false, null, true, "HHmmssSSS", false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + TIME_PATTERN, fullFilename ) );
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, false, null, true, null, false, null );
    assertNotNull( fullFilename );
    assertEquals( filename, fullFilename );
    // add date and time
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, "yyyyMMdd", true, "HHmmssSSS", false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + DATE_PATTERN + TIME_PATTERN, fullFilename ) );
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, null, true, "HHmmssSSS", false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + TIME_PATTERN, fullFilename ) );
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, "yyyyMMdd", true, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + DATE_PATTERN, fullFilename ) );
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, null, true, null, false, null );
    assertNotNull( fullFilename );
    assertEquals( filename, fullFilename );
    // add datetime
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename, false, null, false, null, true, "(yyyyMMdd_HHmmssSSS)" );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( filename + DATE_TIME_PATTERN, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename, false, null, false, null, true, null );
    assertNotNull( fullFilename );
    assertEquals( filename, fullFilename );
  }

  @Test
  public void testAddDatetimeToFilename_ZipWithDotsInFolderWithoutDots() {
    JobEntryBase jobEntryBase = new JobEntryBase();
    String fullFilename;
    String filename = "/folder_without_dots/zip.with.dots.in.folder.without.dots";
    String regexFilename = regexDotEscape( filename );

    // add nothing
    fullFilename = jobEntryBase.addDatetimeToFilename( filename + EXTENSION, false, null, false, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + REGEX_EXTENSION, fullFilename ) );
    // add date
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, true, "yyyyMMdd", false, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + DATE_PATTERN + REGEX_EXTENSION, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, true, null, false, null, false, null );
    assertNotNull( filename );
    assertEquals( filename + EXTENSION, fullFilename );
    // add time
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, false, null, true, "HHmmssSSS", false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + TIME_PATTERN + REGEX_EXTENSION, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, false, null, true, null, false, null );
    assertNotNull( filename );
    assertEquals( filename + EXTENSION, fullFilename );
    // add date and time
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, true, "yyyyMMdd", true, "HHmmssSSS", false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + DATE_PATTERN + TIME_PATTERN + REGEX_EXTENSION, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, true, null, true, "HHmmssSSS", false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + TIME_PATTERN + REGEX_EXTENSION, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, true, "yyyyMMdd", true, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + DATE_PATTERN + REGEX_EXTENSION, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, true, null, true, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + REGEX_EXTENSION, fullFilename ) );
    // add datetime
    fullFilename = jobEntryBase
      .addDatetimeToFilename( filename + EXTENSION, false, null, false, null, true, "(yyyyMMdd_HHmmssSSS)" );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + DATE_TIME_PATTERN + REGEX_EXTENSION, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, false, null, false, null, true, null );
    assertNotNull( fullFilename );
    assertEquals( filename + EXTENSION, fullFilename );
  }

  @Test
  public void testAddDatetimeToFilename_ZipWithDotsInFolderWithDots() {
    JobEntryBase jobEntryBase = new JobEntryBase();
    String fullFilename;
    String filename = "/folder.with.dots/zip.with.dots.in.folder.with.dots";
    String regexFilename = regexDotEscape( filename );

    // add nothing
    fullFilename = jobEntryBase.addDatetimeToFilename( filename + EXTENSION, false, null, false, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + REGEX_EXTENSION, fullFilename ) );
    // add date
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, true, "yyyyMMdd", false, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + DATE_PATTERN + REGEX_EXTENSION, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, true, null, false, null, false, null );
    assertNotNull( fullFilename );
    assertEquals( filename + EXTENSION, fullFilename );
    // add time
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, false, null, true, "HHmmssSSS", false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + TIME_PATTERN + REGEX_EXTENSION, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, false, null, true, null, false, null );
    assertNotNull( fullFilename );
    assertEquals( filename + EXTENSION, fullFilename );
    // add date and time
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, true, "yyyyMMdd", true, "HHmmssSSS", false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + DATE_PATTERN + TIME_PATTERN + REGEX_EXTENSION, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, true, null, true, "HHmmssSSS", false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + TIME_PATTERN + REGEX_EXTENSION, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, true, "yyyyMMdd", true, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + DATE_PATTERN + REGEX_EXTENSION, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, true, null, true, null, false, null );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + REGEX_EXTENSION, fullFilename ) );
    // add datetime
    fullFilename = jobEntryBase
      .addDatetimeToFilename( filename + EXTENSION, false, null, false, null, true, "(yyyyMMdd_HHmmssSSS)" );
    assertNotNull( fullFilename );
    assertTrue( Pattern.matches( regexFilename + DATE_TIME_PATTERN + REGEX_EXTENSION, fullFilename ) );
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename + EXTENSION, false, null, false, null, true, null );
    assertNotNull( fullFilename );
    assertEquals( filename + EXTENSION, fullFilename );
  }

  @Test
  public void testAddDatetimeToFilename_NoFilename() {
    JobEntryBase jobEntryBase = new JobEntryBase();
    String fullFilename;
    String filename;

    //
    // null filename
    //
    filename = null;
    // add nothing
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, false, null, false, null, false, null );
    // add date
    assertNull( fullFilename );
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, "yyyyMMdd", false, null, false, null );
    assertNull( fullFilename );
    // add time
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, false, null, true, "HHmmssSSS", false, null );
    assertNull( fullFilename );
    // add date and time
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, "yyyyMMdd", true, "HHmmssSSS", false, null );
    assertNull( fullFilename );
    // add datetime
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename, false, null, false, null, true, "(yyyyMMdd_HHmmssSSS)" );
    assertNull( fullFilename );

    //
    // empty filename
    //
    filename = StringUtil.EMPTY_STRING;
    // add nothing
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, false, null, false, null, false, null );
    assertNull( fullFilename );
    // add date
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, "yyyyMMdd", false, null, false, null );
    assertNull( fullFilename );
    // add time
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, false, null, true, "HHmmssSSS", false, null );
    assertNull( fullFilename );
    // add date and time
    fullFilename = jobEntryBase.addDatetimeToFilename( filename, true, "yyyyMMdd", true, "HHmmssSSS", false, null );
    assertNull( fullFilename );
    // add datetime
    fullFilename =
      jobEntryBase.addDatetimeToFilename( filename, false, null, false, null, true, "(yyyyMMdd_HHmmssSSS)" );
    assertNull( fullFilename );
  }

  /**
   * <p>Returns the given string with all dots ('.') replaced by "[.]" so that it may be used as a regex expression
   * considering dots as dots and not as "any character"</p>
   *
   * @param str the string to be "escaped"
   * @return a regex-friendly string that recognizes dots as dots and not as "any character"
   */
  private static String regexDotEscape( String str ) {
    return str.replaceAll( "[.]", "[.]" );
  }
}
