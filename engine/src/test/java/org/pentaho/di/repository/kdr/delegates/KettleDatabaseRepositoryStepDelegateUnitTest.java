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

package org.pentaho.di.repository.kdr.delegates;


import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 */
public class KettleDatabaseRepositoryStepDelegateUnitTest {

  @Test
  public void getStepTypeIDs_WhenNeedToUseNotAllValues() throws Exception {
    final int amount = 1;
    final String[] values = new String[] { "1", "2", "3" };

    KettleDatabaseRepository rep = new KettleDatabaseRepository();
    rep.connectionDelegate = mock( KettleDatabaseRepositoryConnectionDelegate.class );
    when( rep.connectionDelegate.getDatabaseMeta() ).thenReturn( mock( DatabaseMeta.class ) );

    KettleDatabaseRepositoryStepDelegate delegate = new KettleDatabaseRepositoryStepDelegate( rep );
    delegate.getStepTypeIDs( values, amount );

//    verify( rep.connectionDelegate )
//      .getIDsWithValues( anyString(), anyString(), anyString(), ArgumentMatchers.argThat( new BaseMatcher<String[]>() {
//
//        @Override public boolean matches( Object item ) {
//          return ( ( (String[]) item ).length == amount ) && ( ( (String[]) item )[ 0 ].equals( values[ 0 ] ) );
//        }
//
//        @Override public void describeTo( Description description ) {
//        }
//      } ) );
  }

  @Test
  public void testGetStepTypeCodeToIdMap() throws KettleException {
    KettleDatabaseRepository repository = mock( KettleDatabaseRepository.class );
    KettleDatabaseRepositoryConnectionDelegate connectionDelegate =
      mock( KettleDatabaseRepositoryConnectionDelegate.class );
    repository.connectionDelegate = connectionDelegate;
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    when( connectionDelegate.getDatabaseMeta() ).thenReturn( databaseMeta );
    when( databaseMeta.quoteField( anyString() ) ).thenAnswer( (Answer<String>) invocationOnMock -> "QUOTE_"
      + invocationOnMock.getArguments()[ 0 ] + "_QUOTE" );
    when( databaseMeta.getQuotedSchemaTableCombination( anyString(), anyString() ) ).thenAnswer(
      (Answer<String>) invocationOnMock -> "QUOTE_" + invocationOnMock.getArguments()[ 0 ] + "____"
        + invocationOnMock.getArguments()[ 1 ] + "_QUOTE" );
    when( connectionDelegate.getDatabaseMeta() ).thenReturn( databaseMeta );
    KettleDatabaseRepositoryStepDelegate kettleDatabaseRepositoryStepDelegate =
      new KettleDatabaseRepositoryStepDelegate( repository );
    Map map = mock( Map.class );
    when( connectionDelegate
      .getValueToIdMap( kettleDatabaseRepositoryStepDelegate.quoteTable( KettleDatabaseRepository.TABLE_R_STEP_TYPE ),
        kettleDatabaseRepositoryStepDelegate.quote( KettleDatabaseRepository.FIELD_STEP_TYPE_ID_STEP_TYPE ),
          kettleDatabaseRepositoryStepDelegate.quote( KettleDatabaseRepository.FIELD_STEP_TYPE_CODE ) ) ).thenReturn(
        map );
    assertEquals( map, kettleDatabaseRepositoryStepDelegate.getStepTypeCodeToIdMap() );
  }
}
