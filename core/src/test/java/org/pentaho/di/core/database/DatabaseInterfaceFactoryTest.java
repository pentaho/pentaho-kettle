package org.pentaho.di.core.database;

import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.row.value.ValueMetaPluginType;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DatabaseInterfaceFactoryTest {

  @BeforeClass
  public static void setUpOnce() throws KettleException {
    DatabasePluginType.getInstance().searchPlugins();
    ValueMetaPluginType.getInstance().searchPlugins();
    KettleClientEnvironment.init();
  }

  // @Test
  @Disabled( "This test is asserting current behavior of returning ConnectionManagementServiceMeta for blank type, "
    + "but this behavior is not implemented yet." )
  public void createReturnsConnectionManagementServiceMetaForBlankType() throws KettleDatabaseException {
    assertTrue( DatabaseInterfaceFactory.create( null ) instanceof ConnectionManagementServiceMeta );
    assertTrue( DatabaseInterfaceFactory.create( "" ) instanceof ConnectionManagementServiceMeta );
    assertTrue( DatabaseInterfaceFactory.create( "  " ) instanceof ConnectionManagementServiceMeta );
  }

  @Test
  public void createResolvesByPluginId() throws KettleDatabaseException {
    DatabaseInterface di = DatabaseInterfaceFactory.create( "MYSQL" );
    assertNotNull( di );
    assertEquals( MySQLDatabaseMeta.class, di.getClass() );
  }

  @Test
  public void createResolvesByPluginName() throws KettleDatabaseException {
    DatabaseInterface di = DatabaseInterfaceFactory.create( "MySQL" );
    assertNotNull( di );
    assertEquals( MySQLDatabaseMeta.class, di.getClass() );
  }

  @Test
  public void createThrowsForUnknownType() {
    try {
      DatabaseInterfaceFactory.create( "no-such-database-type" );
      fail( "Expected KettleDatabaseException for unknown database type" );
    } catch ( KettleDatabaseException expected ) {
      // expected
    }
  }

  @Test
  public void isConnectionManagementServiceTypeDetectsBlank() {
    assertTrue( DatabaseInterfaceFactory.isConnectionManagementServiceType( null ) );
    assertTrue( DatabaseInterfaceFactory.isConnectionManagementServiceType( "" ) );
    assertTrue( DatabaseInterfaceFactory.isConnectionManagementServiceType( "   " ) );
    assertFalse( DatabaseInterfaceFactory.isConnectionManagementServiceType( "MYSQL" ) );
  }
}