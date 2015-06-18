/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.synchronizeaftermerge;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Properties;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.pentaho.di.job.entries.evaluatetablecontent.MockDriver;

public class SynchMockDriver extends MockDriver {

  @Override
  public Connection connect( String url, Properties info ) throws SQLException {
    Connection conn = super.connect( url, info );

    Mockito.doThrow(new SQLException( "Wrong rollback" ) ).when( conn ).rollback( Matchers.isNull( Savepoint.class ) );

    PreparedStatement prepStatement = mock( PreparedStatement.class );
    when( conn.prepareStatement( anyString() ) ).thenReturn( prepStatement );

    ResultSet rs = mock( ResultSet.class );
    when( prepStatement.executeQuery() ).thenReturn( rs );
    ResultSetMetaData rsmd = mock( ResultSetMetaData.class );
    when( rs.getMetaData() ).thenReturn( rsmd );
    DatabaseMetaData md = mock( DatabaseMetaData.class );
    when( conn.getMetaData() ).thenReturn( md );
    when( md.supportsTransactions() ).thenReturn( true );
    return conn;
  }

}
