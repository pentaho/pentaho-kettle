/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
