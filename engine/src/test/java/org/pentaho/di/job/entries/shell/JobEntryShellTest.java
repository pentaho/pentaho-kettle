/*! ******************************************************************************
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
package org.pentaho.di.job.entries.shell;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.Const;

import java.util.Map;

public class JobEntryShellTest {
  public static final String TEST_LIST_ENV_VARIABLE_TO_IGNORE = "package.mock,info.mock";
  public static final String TEST_LIST_ENV_VARIABLE_TO_IGNORE_EMPTY = "";
  public static final String TEST_ENV_VARIABLE = "MOCK.VARIABLE";
  public static final String TEST_ENV_VARIABLE_VALUE = "MOCK VALUE";

  @Mock
  private JobEntryShell jobEntryShellMock;

  private final JobEntryShell jobEntryShell = new JobEntryShell();

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

  @Test
  public void testPopulateProcessBuilderEnvironment() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    Map<String, String> environment = processBuilder.environment();

    JobEntryShell spyJobEntryShell = Mockito.spy( jobEntryShell );
    doReturn( TEST_LIST_ENV_VARIABLE_TO_IGNORE ).when( spyJobEntryShell )
            .getVariable( Const.SHELL_STEP_ENVIRONMENT_VARIABLES_TO_IGNORE );

    String[] envsToIgnore = TEST_LIST_ENV_VARIABLE_TO_IGNORE.split( "," );
    for ( String envToIgnore : envsToIgnore ) {
      spyJobEntryShell.setVariable( envToIgnore, TEST_ENV_VARIABLE_VALUE );
    }
    spyJobEntryShell.setVariable( TEST_ENV_VARIABLE, TEST_ENV_VARIABLE_VALUE );
    spyJobEntryShell.populateProcessBuilderEnvironment( processBuilder );

    for ( String envToIgnore : envsToIgnore ) {
      Assert.assertNull( environment.get( envToIgnore ) );
    }
    Assert.assertEquals( environment.get( TEST_ENV_VARIABLE ), TEST_ENV_VARIABLE_VALUE );
  }

  @Test
  public void testPopulateProcessBuilderEnvironmentWithoutEnvsToIgnore() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    Map<String, String> environment = processBuilder.environment();

    JobEntryShell spyJobEntryShell = Mockito.spy( jobEntryShell );
    doReturn( TEST_LIST_ENV_VARIABLE_TO_IGNORE_EMPTY ).when( spyJobEntryShell )
            .getVariable( Const.SHELL_STEP_ENVIRONMENT_VARIABLES_TO_IGNORE );

    spyJobEntryShell.setVariable( TEST_ENV_VARIABLE, TEST_ENV_VARIABLE_VALUE );
    spyJobEntryShell.populateProcessBuilderEnvironment( processBuilder );

    Assert.assertEquals( environment.get( TEST_ENV_VARIABLE ), TEST_ENV_VARIABLE_VALUE );
  }

}
