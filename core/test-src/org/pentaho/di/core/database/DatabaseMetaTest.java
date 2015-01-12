package org.pentaho.di.core.database;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class DatabaseMetaTest {

  @BeforeClass
  public static void setupClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Test
  public void testGetDatabaseInterfacesMapWontReturnNullIfCalledSimultaneouslyWithClear() throws InterruptedException, ExecutionException {
    final AtomicBoolean done = new AtomicBoolean( false );
    ExecutorService executorService = Executors.newCachedThreadPool();
    executorService.submit( new Runnable() {

      @Override
      public void run() {
        while ( !done.get() ) {
          DatabaseMeta.clearDatabaseInterfacesMap();
        }
      }
    } );
    Future<Exception> getFuture = executorService.submit( new Callable<Exception>() {

      @Override
      public Exception call() throws Exception {
        int i = 0;
        while ( !done.get() ) {
          assertNotNull( "Got null on try: " + i++, DatabaseMeta.getDatabaseInterfacesMap() );
          if ( i > 30000 ) {
            done.set( true );
          }
        }
        return null;
      }
    } );
    getFuture.get();
  }

  @Test
  public void testDatabaseAccessTypeCode() throws Exception {
    String expectedJndi = "JNDI";
    String access = DatabaseMeta.getAccessTypeDesc( DatabaseMeta.getAccessType( expectedJndi ) );
    assertEquals( expectedJndi, access );
  }

  @Test
  public void testCheckParametersSAPR3DatabaseMeta() {
    testCheckParams( Mockito.mock( SAPR3DatabaseMeta.class ), false );
  }

  @Test
  public void testCheckParametersGenericDatabaseMeta() {
    testCheckParams( Mockito.mock( GenericDatabaseMeta.class ), false );
  }

  @Test
  public void testAddOptionsMysql() {
    DatabaseMeta databaseMeta = new DatabaseMeta( "", "Mysql", "JDBC", null, "stub:stub", null, null, null );
    Map<String, String> options = databaseMeta.getExtraOptions();
    if ( !options.keySet().contains( "MYSQL.defaultFetchSize" ) ) {
      fail();
    }
  }

  @Test
  public void testAddOptionsInfobright() {
    DatabaseMeta databaseMeta = new DatabaseMeta( "", "Infobright", "JDBC", null, "stub:stub", null, null, null );
    Map<String, String> options = databaseMeta.getExtraOptions();
    if ( !options.keySet().contains( "INFOBRIGHT.characterEncoding" ) ) {
      fail();
    }
  }

  private void testCheckParams( DatabaseInterface databaseInterface, boolean contains ) {
    DatabaseMeta dbMeta = new DatabaseMeta();
    dbMeta.setDatabaseInterface( databaseInterface );

    String[] params = dbMeta.checkParameters();
    String remark = "Please specify the name of the database";
    assertEquals( Arrays.asList( params ).contains( remark ), contains );
  }


}
