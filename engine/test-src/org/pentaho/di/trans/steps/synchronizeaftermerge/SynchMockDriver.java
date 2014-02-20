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
