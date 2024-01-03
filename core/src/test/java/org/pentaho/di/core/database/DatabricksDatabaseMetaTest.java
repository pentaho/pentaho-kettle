package org.pentaho.di.core.database;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.database.DatabricksDatabaseMeta.AuthMethod;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.row.value.ValueMetaString;

import java.util.HashMap;
import java.util.Map;

public class DatabricksDatabaseMetaTest {

  @BeforeClass
  public static void setupOnce() throws Exception {
    // this will make the type discoverable for DatabaseMeta
    DatabasePluginType.getInstance().searchPlugins();
  }

  @Test
  public void testDbMeta() throws Exception {
    DatabaseMeta meta = new DatabaseMeta( "dbMeta", "Databricks", "Native", "host", "db", "443", null, null );
    assertEquals( DatabricksDatabaseMeta.class, meta.getDatabaseInterface().getClass() );
    assertEquals( "com.databricks.client.jdbc.Driver", meta.getDriverClass() );
    assertEquals( "databricks", meta.getDatabaseInterface().getXulOverlayFile() );
    assertEquals( "useSchemaNameForTableList", false, meta.useSchemaNameForTableList() );
  }

  @Test
  public void testFieldQuoting() {
    DatabaseMeta meta = getDBMeta();
    assertEquals( "reserved words quoted", "`from`", meta.quoteField( "from" ) );
    assertEquals( "regular fields not quoted", "something", meta.quoteField( "something" ) );
  }

  private DatabaseMeta getDBMeta() {
    return new DatabaseMeta( "dbMeta", "Databricks", "Native", "host", "db", "443", null, null );
  }

  @Test
  public void testUrl() throws Exception {
    DatabricksDatabaseMeta dbMeta = new DatabricksDatabaseMeta();
    dbMeta.setAuthMethod( AuthMethod.Token );
    dbMeta.setToken( "atokenvalue" );
    dbMeta.setHttpPath( "h/tt/p/path" );
    String url = dbMeta.getURL( "hostn", "444", "dbName" );
    String[] urlParts = url.split( ";" );
    Map<String, String> urlParams = new HashMap<>( urlParts.length - 1 );
    assertEquals( "jdbc:databricks://hostn:444", urlParts[0] );
    for ( int i = 1; i < urlParts.length; i++ ) {
      String[] pair = urlParts[i].split( "=" );
      assertEquals( 2, pair.length );
      urlParams.put( pair[0], pair[1] );
    }
    ;
    assertEquals( "token", urlParams.get( "UID" ) );
    assertEquals( "atokenvalue", urlParams.get( "PWD" ) );
    assertEquals( "dbName", urlParams.get( "ConnCatalog" ) );
  }

  @Test
  public void testTokenUnsetsUserPass() {
    // PDI will always include any user/pass if set, which will override the token
    DatabricksDatabaseMeta dbMeta = new DatabricksDatabaseMeta();
    dbMeta.setPassword( "oldpwd" );
    dbMeta.setUsername( "oldusr" );
    assertEquals( "oldusr", dbMeta.getUsername() );
    assertEquals( "oldpwd", dbMeta.getPassword() );
    dbMeta.setToken( "tok" );
    dbMeta.setHttpPath( "hp" );
    assertNull( "passwd after token", dbMeta.getPassword() );
    assertNull( "user after token", dbMeta.getUsername() );
  }

  @Test
  public void testQuerySchema() {
    DatabricksDatabaseMeta db = new DatabricksDatabaseMeta();
    assertEquals( "SELECT * FROM thetable LIMIT 0", db.getSQLQueryFields( "thetable" ) );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testNoPath() throws Exception {
    DatabaseMeta dbMeta = getDBMeta();
    dbMeta.getURL();
  }

  @Test
  public void testStringFieldDef() throws Exception {
    DatabricksDatabaseMeta dbricks = new DatabricksDatabaseMeta();
    String fieldDef = dbricks.getFieldDefinition( new ValueMetaString( "name" ), null, null, false, false, false );
    assertEquals( "VARCHAR()", fieldDef );
  }

}
