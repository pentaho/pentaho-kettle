package org.pentaho.di.core.sql;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.pentaho.di.cluster.CarteLauncher;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.jdbc.TransDataService;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.www.Carte;
import org.pentaho.di.www.SlaveServerConfig;

public class TransDataServletTest extends TestCase {

  private CarteLauncher carteLauncher;
  private Carte carte;
  private SlaveServer slaveServer;
  private DatabaseMeta databaseMeta;
  private Database database;
    
  public void test01_BasicQuery() throws Exception {
    startServer();
    database.connect();
    
    ResultSet resultSet = database.openQuery("SELECT * FROM Service");
    List<Object[]> rows = database.getRows(resultSet, 0, null);
    RowMetaInterface rowMeta = database.getReturnRowMeta();
    assertNotNull(rowMeta);
    assertEquals(8, rows.size());
    
    database.disconnect();
    stopServer();
  }
  
  protected void startServer() throws Exception {
    KettleEnvironment.init();
    launchSlaveServer();
    databaseMeta = new DatabaseMeta("TestConnection", "KettleThin", "JDBC", slaveServer.getHostname(), "kettle", slaveServer.getPort(), "cluster", "cluster");
    SimpleLoggingObject loggingObject = new SimpleLoggingObject(getClass().getName(), LoggingObjectType.GENERAL, null);
    database = new Database(loggingObject, databaseMeta);
  }
  
  protected void stopServer() throws Exception {
    carte.getWebServer().stopServer();
  }

  private CarteLauncher launchSlaveServer() throws Exception {
    slaveServer = new SlaveServer("test-localhost-8585-master", "127.0.0.1", "8686", "cluster", "cluster", null, null, null, true);
    SlaveServerConfig slaveServerConfig = new SlaveServerConfig();
    slaveServerConfig.setSlaveServer(slaveServer);
    slaveServerConfig.setServicesMap(getServicesMap());
    slaveServerConfig.setJoining(false);
    
    carteLauncher = new CarteLauncher(slaveServerConfig);
    Thread thread = new Thread(carteLauncher);
    thread.setName("Carte Launcher"+thread.getName());
    thread.start();
    // Wait until the carte object is available...
    while (carteLauncher.getCarte()==null && !carteLauncher.isFailure()) {
      Thread.sleep(100);
    }
    carte = carteLauncher.getCarte();
    
    // If there is a failure, stop the servers already launched and throw the exception
    if (carteLauncher.isFailure()) {
      carteLauncher.getCarte().getWebServer().stopServer();
      throw carteLauncher.getException(); // throw the exception for good measure.
    }
    
    return carteLauncher;
  }

  private Map<String, TransDataService> getServicesMap() {
    Map<String, TransDataService> servicesMap = new HashMap<String, TransDataService>();
    TransDataService service = new TransDataService("Service", "testfiles/sql-transmeta-test-data.ktr", null, null, "Output");
    servicesMap.put(service.getName(), service);
    return servicesMap;
  }

}
