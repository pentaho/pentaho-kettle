package org.pentaho.di.core;

import org.junit.Test;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.MySQLDatabaseMeta;
import org.pentaho.di.core.database.SAPR3DatabaseMeta;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * //TODO Add some javadoc or remove this comment
 *
 * @author Pavel Sakun
 */
public class ConstDBTest {

  @Test
  public void testSelectSAPR3Databases() throws Exception {
    KettleClientEnvironment.init();
    final DatabaseMeta mysqlMeta = new DatabaseMeta();
    mysqlMeta.setDatabaseInterface( new MySQLDatabaseMeta() );
    final DatabaseMeta sapR3Meta = new DatabaseMeta();
    sapR3Meta.setDatabaseInterface( new SAPR3DatabaseMeta() );
    List<DatabaseMeta> databaseMetas = new ArrayList<>();
    databaseMetas.add( mysqlMeta );
    databaseMetas.add( sapR3Meta );

    List<DatabaseMeta> sapR3Metas = ConstDB.selectSAPR3Databases( databaseMetas );
    assertEquals( 1, sapR3Metas.size() );
    assertSame( sapR3Meta, sapR3Metas.get( 0 ) );
  }
}
