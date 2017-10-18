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

package org.pentaho.di.job.entries.missing;

import org.junit.Test;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.logging.LogChannel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MissingEntryTest {

  @Test
  public void testExecute() throws KettleJobException {
    MissingEntry entry = spy( new MissingEntry() );
    when( entry.getLogChannel() ).thenReturn( mock( LogChannel.class ) );
    entry.setName( "MissingTest" );
    Result result = new Result();
    result.setNrErrors( 0 );
    result.setResult( true );
    entry.execute( result, 0 );
    assertEquals( 1, result.getNrErrors() );
    assertEquals( false, result.getResult() );
  }

}
