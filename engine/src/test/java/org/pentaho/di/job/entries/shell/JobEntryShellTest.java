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
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JobEntryShellTest {

  @Mock
  private JobEntryShell jobEntryShellMock;

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

}
