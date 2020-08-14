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
package org.pentaho.di.job.entries.shell;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JobEntryShellTest {
  private static final String TEST_MAX_ARG_LENGTH_VAR = "TEST_MAX_ARG_LENGTH_VAR";
  private static final String TEST_MAX_ARG_LENGTH_EXCEEDING_VAR = "TEST_MAX_ARG_LENGTH_EXCEEDING_VAR";
  
  @Mock
  private JobEntryShell jobEntryShellMock;
  
  private JobEntryShell jobEntryShell = new JobEntryShell();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks( this );
  }

  /**
   * tests if Windows's EOL characters is replaced.
   * 
   * @see <a href="http://jira.pentaho.com/browse/PDI-12176">Jira issue</a>
   */
  @Test
  public void replaceWinEOLtest() {
    // string is shell content from PDI-12176
    String content = "#!/bin/bash\r\n"
        + "\r\n"
        + "echo `date` > /home/pentaho/test_output/output.txt";
    doCallRealMethod().when( jobEntryShellMock ).replaceWinEOL( anyString() );
    content = jobEntryShellMock.replaceWinEOL( content );
    verify( jobEntryShellMock ).replaceWinEOL( anyString() );
    String assertionFailedMessage = "Windows EOL character is detected";
    // shouldn't contains CR and CR+LF characters  
    Assert.assertFalse( assertionFailedMessage, content.contains( "\r\n" ) );
    Assert.assertFalse( assertionFailedMessage, content.contains( "\r" ) );
  }

  /**
   * Verifies whether variables having value exceeding the maximum argument length are excluded or not.
   *
   * @see <a href="https://jira.pentaho.com/browse/PDI-18803">PDI-18803</a>
   */
  @Test
  public void testPopulateProcessBuilderEnvironment() {
    ProcessBuilder procBuilder = new ProcessBuilder();
    Map<String, String> pbEnv = procBuilder.environment();
    int maxArgStrLen = jobEntryShell.getMaxArgStrLen();
    String validValue = RandomStringUtils.randomAscii( maxArgStrLen );
    jobEntryShell.setVariable( TEST_MAX_ARG_LENGTH_VAR, validValue );
    jobEntryShell.setVariable( TEST_MAX_ARG_LENGTH_EXCEEDING_VAR, RandomStringUtils.randomAscii( maxArgStrLen + 1 ) );
    jobEntryShell.populateProcessBuilderEnvironment( procBuilder );
    Assert.assertNull( "The process builder environment must not contains a variable which value exceeds the maximum allowed length.", pbEnv.get( TEST_MAX_ARG_LENGTH_EXCEEDING_VAR ) );
    Assert.assertEquals( "Variable having a valid value must be present in the process builder environment after population.", validValue, pbEnv.get( TEST_MAX_ARG_LENGTH_VAR ) );
  }
}
