/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.core.logging;

import junit.framework.Assert;
import org.junit.Test;


/**
 * Test for {@link KettleLogLayout}.
 */
public class KettleLogLayoutTest {


  @Test
  public void testFormat() throws Exception {
    LogMessage mcg = new LogMessage( "Log message for {0}",
        "Channel 01",
        new String[]{ "Test" },
        LogLevel.DEBUG );
    mcg.setSubject( "Subject" );

    KettleLoggingEvent event = new KettleLoggingEvent( mcg, 0, LogLevel.BASIC );
    KettleLogLayout layout = new KettleLogLayout();

    final String formattedMsg = layout.format( event );

    Assert.assertEquals( "The log message must be formatted and not contain placeholders.",
        "Subject - Log message for Test",
        formattedMsg.substring( formattedMsg.indexOf( '-' ) + 2 ) );
  }

  /**
   * Log4j2 Date Patterns
   * %d{HH:mm:ss,SSS} -> Parseable to Java SimpleDateFormat
   * %d{HH:mm:ss,nnnn} to %d{HH:mm:ss,nnnnnnnnn} -> Not Parseable to Java SimpleDateFormat
   * %d{dd MMM yyyy HH:mm:ss,SSS} -> Parseable to Java SimpleDateFormat
   * %d{dd MMM yyyy HH:mm:ss,nnnn} to %d{dd MMM yyyy HH:mm:ss,nnnnnnnnn} -> Not Parseable to Java SimpleDateFormat
   * %d{MMM dd,yyyy HH:mm:ss} -> Parseable to Java SimpleDateFormat
   * %d{HH:mm:ss}{GMT+0} -> Parseable to Java SimpleDateFormat
   *
   * Test log4j2.xml core/src/test/resources/log4j2.xml with multiple "pdi-execution-appender" for testing.
   */
  @Test
  public void testGetLog4j2AppenderSetDefaultDate() {
    //"pdi-execution-appender" Date Pattern within the {} are empty, therefore unable to parse to SimpleDateFormat().
    // Returns default date pattern "yyyy/MM/dd HH:mm:ss"
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test";
    Assert.assertEquals( "yyyy/MM/dd HH:mm:ss",
      KettleLogLayout.getLog4j2Appender().toPattern() );
  }

  @Test
  public void testGetLog4j2UsingAppender1() {
    // Testing HH:mm:ss,SSS pattern
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test-1";
    Assert.assertEquals( "HH:mm:ss,SSS",
      KettleLogLayout.getLog4j2Appender(  ).toPattern() );
  }

  @Test
  public void testGetLog4j2UsingAppender2() {
    // This will throw an Illegal pattern character 'n' Exception parsing to Java SimpleDateFormat() and set Default
    // Pattern value "yyyy/MM/dd HH:mm:ss"
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test-2";
    Assert.assertNotSame( "HH:mm:ss,nnnn",
      KettleLogLayout.getLog4j2Appender().toPattern() );
    Assert.assertEquals( "yyyy/MM/dd HH:mm:ss",
      KettleLogLayout.getLog4j2Appender().toPattern() );
  }

  @Test
  public void testGetLog4j2UsingAppender3() {
    // Testing dd MMM yyyy HH:mm:ss,SSS pattern
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test-3";
    Assert.assertEquals( "dd MMM yyyy HH:mm:ss,SSS",
      KettleLogLayout.getLog4j2Appender().toPattern() );
  }

  @Test
  public void testGetLog4j2UsingAppender4() {
    // This will throw an Illegal pattern character 'n' Exception parsing to Java SimpleDateFormat() and set Default
    // Pattern value "yyyy/MM/dd HH:mm:ss"
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test-4";
    Assert.assertNotSame( "dd MMM yyyy HH:mm:ss,nnnn",
      KettleLogLayout.getLog4j2Appender().toPattern() );
    Assert.assertEquals( "yyyy/MM/dd HH:mm:ss",
      KettleLogLayout.getLog4j2Appender().toPattern() );
  }

  @Test
  public void testGetLog4j2UsingAppender5() {
    // Testing dd-MMM-yyyy HH:mm:ss,SSS pattern
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test-5";
    Assert.assertEquals( "dd-MMM-yyyy HH:mm:ss,SSS",
      KettleLogLayout.getLog4j2Appender().toPattern() );
  }

  @Test
  public void testGetLog4j2UsingAppender6() {
    // Testing dd/MMM/yyyy HH:mm:ss,SSS pattern
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test-6";
    Assert.assertEquals( "dd/MMM/yyyy HH:mm:ss,SSS",
      KettleLogLayout.getLog4j2Appender().toPattern() );
  }

  @Test
  public void testGetLog4j2UsingAppender7() {
    // Testing MMM dd,yyyy HH:mm:ss pattern
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test-7";
    Assert.assertEquals( "MMM dd,yyyy HH:mm:ss",
      KettleLogLayout.getLog4j2Appender().toPattern() );
  }

  @Test
  public void testGetLog4j2UsingAppender8() {
    // Testing MMM-dd,yyyy pattern
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test-8";
    Assert.assertEquals( "MMM-dd,yyyy",
      KettleLogLayout.getLog4j2Appender().toPattern() );
  }

  @Test
  public void testGetLog4j2UsingAppender9() {
    // Testing adding empty {} for TimeZone
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test-9";
    Assert.assertEquals( "MMM/dd,yyyy HH:mm:ss",
      KettleLogLayout.getLog4j2Appender().toPattern() );
  }

  @Test
  public void testGetLog4j2UsingAppender10() {
    // Testing adding TimeZone GMT+0
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test-10";
    Assert.assertEquals( "MMM dd,yyyy HH:mm:ss",
      KettleLogLayout.getLog4j2Appender().toPattern() );
  }

  @Test
  public void testGetLog4j2UsingAppender11() {
    // Testing adding TimeZone GMT-5
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test-11";
    Assert.assertEquals( "HH:mm:ss",
      KettleLogLayout.getLog4j2Appender().toPattern() );
  }

  @Test
  public void testGetLog4j2UsingAppender12() {
    // Test with no matching appender name and set Default Pattern value "yyyy/MM/dd HH:mm:ss"
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test-twelve";
    Assert.assertEquals( "yyyy/MM/dd HH:mm:ss",
      KettleLogLayout.getLog4j2Appender().toPattern() );
  }

  @Test
  public void testGetLog4j2UsingAppender13() {
    // Test with un-parseable TimeZone
    KettleLogLayout.log4J2Appender = "pdi-execution-appender-test-13";
    Assert.assertEquals( "HH:mm:ss",
      KettleLogLayout.getLog4j2Appender().toPattern() );
  }

}
