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

package org.pentaho.di.core.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ThinPreparedStatementTest {

  static ThinConnection thinConn = mock( ThinConnection.class );

  @SuppressWarnings( "deprecation" )
  @Test
  public void testParameterTypes() throws SQLException {
    StringBuilder originalSQL = new StringBuilder();
    originalSQL.append( "SELECT \"test\" AS colA" ).append( Const.CR );
    originalSQL.append( "FROM dual" ).append( Const.CR );
    originalSQL.append( "WHERE" ).append( Const.CR );
    originalSQL.append( "intID = ?" ).append( Const.CR );
    originalSQL.append( "AND StringID = ?" ).append( Const.CR );
    originalSQL.append( "AND decimalID = ?" ).append( Const.CR );
    ThinPreparedStatement ps = new ThinPreparedStatement( thinConn, originalSQL.toString() );

    assertEquals( 3, ps.getParamMeta().length );
    for ( ValueMetaInterface paramMeta : ps.getParamMeta() ) {
      assertEquals( ValueMetaInterface.TYPE_STRING, paramMeta.getType() );
      assertTrue( paramMeta.getName().startsWith( "param-" ) );
    }

    ps.setInt( 1, 12345 );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, ps.getParamMeta()[0].getType() );
    assertEquals( Long.valueOf( 12345 ), ps.getParamData()[0] );

    ps.setLong( 2, 56789 );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, ps.getParamMeta()[1].getType() );
    assertEquals( Long.valueOf( 56789 ), ps.getParamData()[1] );

    ps.setShort( 2, (short) 255 );
    assertEquals( ValueMetaInterface.TYPE_INTEGER, ps.getParamMeta()[1].getType() );
    assertEquals( Long.valueOf( 255 ), ps.getParamData()[1] );

    ps.setDouble( 1, Double.valueOf( "12345.6789" ) );
    assertEquals( ValueMetaInterface.TYPE_NUMBER, ps.getParamMeta()[0].getType() );
    assertEquals( Double.valueOf( "12345.6789" ), ps.getParamData()[0] );

    ps.setFloat( 2, Float.valueOf( "98765.4321" ) );
    assertEquals( ValueMetaInterface.TYPE_NUMBER, ps.getParamMeta()[1].getType() );
    assertEquals( Float.valueOf( "98765.4321" ).doubleValue(), (Double) ps.getParamData()[1], Float.valueOf( "0.1" ).doubleValue() );

    ps.setTime( 1, new Time( 12345 ) );
    assertEquals( ValueMetaInterface.TYPE_DATE, ps.getParamMeta()[0].getType() );
    assertEquals( new Date( 12345 ), ps.getParamData()[0] );

    ps.setTime( 2, new Time( 23456 ), Calendar.getInstance() );
    assertEquals( ValueMetaInterface.TYPE_DATE, ps.getParamMeta()[1].getType() );
    assertEquals( new Date( 23456 ), ps.getParamData()[1] );

    ps.setTimestamp( 1, new Timestamp( 98765 ) );
    assertEquals( ValueMetaInterface.TYPE_DATE, ps.getParamMeta()[0].getType() );
    assertEquals( new Date( 98765 ), ps.getParamData()[0] );

    ps.setTimestamp( 2, new Timestamp( 87654 ), Calendar.getInstance() );
    assertEquals( ValueMetaInterface.TYPE_DATE, ps.getParamMeta()[1].getType() );
    assertEquals( new Date( 87654 ), ps.getParamData()[1] );

    ps.close();
  }
}
