/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.salesforceinput;

import static org.mockito.Mockito.*;

import java.util.GregorianCalendar;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

public class SalesforceConnectionTest {

  private LogChannelInterface logInterface = mock( LogChannelInterface.class );

  private String url = "url";

  private String username = "username";

  private String password = "password";

  private int recordsFilter = 0;

  @Test( expected = KettleException.class )
  public void testConstructor_emptyUrl() throws KettleException {
    new SalesforceConnection( logInterface, null, username, password );
  }

  @Test( expected = KettleException.class )
  public void testConstructor_emptyUserName() throws KettleException {
    new SalesforceConnection( logInterface, url, null, password );
  }

  @Test( expected = KettleException.class )
  public void testSetCalendarStartNull() throws KettleException {
    SalesforceConnection connection = new SalesforceConnection( logInterface, url, username, password );
    GregorianCalendar endDate = new GregorianCalendar( 2000, 2, 10 );
    connection.setCalendar( recordsFilter, null, endDate );
  }

  @Test( expected = KettleException.class )
  public void testSetCalendarEndNull() throws KettleException {
    SalesforceConnection connection = new SalesforceConnection( logInterface, url, username, password );
    GregorianCalendar startDate = new GregorianCalendar( 2000, 2, 10 );
    connection.setCalendar( recordsFilter, startDate, null );
  }

  @Test( expected = KettleException.class )
  public void testSetCalendarStartDateTooOlder() throws KettleException {
    SalesforceConnection connection = new SalesforceConnection( logInterface, url, username, password );
    GregorianCalendar startDate = new GregorianCalendar( 2000, 3, 20 );
    GregorianCalendar endDate = new GregorianCalendar( 2000, 2, 10 );
    connection.setCalendar( recordsFilter, startDate, endDate );
  }

}
