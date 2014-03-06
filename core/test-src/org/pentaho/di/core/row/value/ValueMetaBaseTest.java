package org.pentaho.di.core.row.value;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.NetezzaDatabaseMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaBaseTest {

  /**
   * PDI-10877 Table input step returns no data when pulling a timestamp column from IBM Netezza
   * 
   * @throws Exception
   */
  @Test
  public void testGetValueFromSqlType() throws Exception {
    ValueMetaBase obj = new ValueMetaBase();
    DatabaseInterface databaseInterface = new NetezzaDatabaseMeta();
    ResultSet resultSet = Mockito.mock( ResultSet.class );
    ResultSetMetaData metaData = Mockito.mock( ResultSetMetaData.class );
    Mockito.when( resultSet.getMetaData() ).thenReturn( metaData );

    Mockito.when( metaData.getColumnType( 1 ) ).thenReturn( Types.DATE );
    Mockito.when( metaData.getColumnType( 2 ) ).thenReturn( Types.TIME );

    obj.type = ValueMetaInterface.TYPE_DATE;
    // call to testing method
    obj.getValueFromResultSet( databaseInterface, resultSet, 0 );
    // for jdbc Date type getDate method called
    Mockito.verify( resultSet, Mockito.times( 1 ) ).getDate( Mockito.anyInt() );

    obj.getValueFromResultSet( databaseInterface, resultSet, 1 );
    // for jdbc Time type getTime method called
    Mockito.verify( resultSet, Mockito.times( 1 ) ).getTime( Mockito.anyInt() );
  }

}
