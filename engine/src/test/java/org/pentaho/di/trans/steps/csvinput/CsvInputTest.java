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

package org.pentaho.di.trans.steps.csvinput;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LogChannelInterface;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.pentaho.di.trans.steps.csvinput.CsvInput.guessStringsFromLine;

public class CsvInputTest {

  private LogChannelInterface logChannelInterface;
  private String line;
  private CsvInputMeta csvInputMeta;

  @Before
  public void setUp() throws Exception {
    logChannelInterface = mock( LogChannelInterface.class );
    csvInputMeta = mock( CsvInputMeta.class );
  }

  @Test ( expected = NullPointerException.class )
  public void guessStringsFromLineWithEmptyLine() throws Exception {

    line = null;
    String[] saData = guessStringsFromLine( logChannelInterface, line, csvInputMeta.getDelimiter(),
      csvInputMeta.getEnclosure(), csvInputMeta.getEscapeCharacter() );

    assertNull( saData );

    for ( int i = 0; i < saData.length; i++ ) {
      return;
    }

  }

}
