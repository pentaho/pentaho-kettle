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

package org.pentaho.di.repository.kdr.delegates;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

    verify( rep.connectionDelegate )
      .getIDsWithValues( anyString(), anyString(), anyString(), argThat( new BaseMatcher<String[]>() {

        @Override public boolean matches( Object item ) {
          return ( ( (String[]) item ).length == amount ) && ( ( (String[]) item )[ 0 ].equals( values[ 0 ] ) );
        }

        @Override public void describeTo( Description description ) {
        }
      } ) );
  }

  @Test
  public void testGetStepTypeCodeToIdMap() throws KettleException {
    KettleDatabaseRepository repository = mock( KettleDatabaseRepository.class );
    KettleDatabaseRepositoryConnectionDelegate connectionDelegate =
      mock( KettleDatabaseRepositoryConnectionDelegate.class );
    repository.connectionDelegate = connectionDelegate;
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    when( connectionDelegate.getDatabaseMeta() ).thenReturn( databaseMeta );
    when( databaseMeta.quoteField( anyString() ) ).thenAnswer( new Answer<String>() {
      @Override public String answer( InvocationOnMock invocationOnMock ) throws Throwable {
        return "QUOTE_" + String.valueOf( invocationOnMock.getArguments()[ 0 ] + "_QUOTE" );
      }
    } );
    when( databaseMeta.getQuotedSchemaTableCombination( anyString(), anyString() ) ).thenAnswer( new Answer<String>() {
      @Override public String answer( InvocationOnMock invocationOnMock ) throws Throwable {
        return "QUOTE_" + String.valueOf( invocationOnMock.getArguments()[ 0 ] ) + "____" + String
          .valueOf( invocationOnMock.getArguments()[ 1 ] + "_QUOTE" );
      }
    } );
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
