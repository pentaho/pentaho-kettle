package org.pentaho.di.core.sql;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
  
  public void test02_NoData() throws Exception {
    startServer();
    database.connect();
    
    ResultSet resultSet = database.openQuery("SELECT * FROM Service WHERE Country = 'NoCountry'");
    List<Object[]> rows = database.getRows(resultSet, 0, null);
    RowMetaInterface rowMeta = database.getReturnRowMeta();
    assertNotNull(rowMeta);
    assertEquals(0, rows.size());
    
    database.disconnect();
    stopServer();
  }
  
  /**
   * Test query:
   *          select "Service"."Category" as "c0", "Service"."Country" as "c1" 
   *          from "Service" as "Service" 
   *          where ((not ("Service"."Country" = 'Belgium') or ("Service"."Country" is null))) 
   *          group by "Service"."Category", "Service"."Country" 
   *          order by CASE WHEN "Service"."Category" IS NULL THEN 1 ELSE 0 END, "Service"."Category" ASC, CASE WHEN "Service"."Country" IS NULL THEN 1 ELSE 0 END, "Service"."Country" ASC
   *          
   * @throws Exception
   */
  
  public void test03_MondrianQuery() throws Exception {
    startServer();
    database.connect();
    
    String query = "select \"Service\".\"Category\" as \"c0\", \"Service\".\"Country\" as \"c1\" from \"Service\" as \"Service\" where ((not (\"Service\".\"Country\" = 'Belgium') or (\"Service\".\"Country\" is null))) group by \"Service\".\"Category\", \"Service\".\"Country\" order by CASE WHEN \"Service\".\"Category\" IS NULL THEN 1 ELSE 0 END, \"Service\".\"Category\" ASC, CASE WHEN \"Service\".\"Country\" IS NULL THEN 1 ELSE 0 END, \"Service\".\"Country\" ASC"; 
    ResultSet resultSet = database.openQuery(query);
    List<Object[]> rows = database.getRows(resultSet, 0, null);
    RowMetaInterface rowMeta = database.getReturnRowMeta();
    assertNotNull(rowMeta);
    assertEquals(6, rows.size());
    
    database.disconnect();
    stopServer();
  }
  
  
  
  protected void startServer() throws Exception {
    KettleEnvironment.init();
    launchSlaveServer();
    databaseMeta = new DatabaseMeta("TestConnection", "KettleThin", "JDBC", slaveServer.getHostname(), "kettle", slaveServer.getPort(), "cluster", "cluster");
    databaseMeta.addExtraOption("KettleThin", "debugtrans", "/tmp/gen.ktr");
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
    slaveServerConfig.setServices(getServicesMap());
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

  private List<TransDataService> getServicesMap() {
    List<TransDataService> servicesMap = new ArrayList<TransDataService>();
    TransDataService service = new TransDataService("Service", "testfiles/sql-transmeta-test-data.ktr", null, null, "Output");
    servicesMap.add(service);
    return servicesMap;
  }

}
