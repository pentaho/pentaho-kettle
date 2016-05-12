/*!
 * Copyright 2010 - 2016 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.pentaho.pdi.ws;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.dom4j.Document;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.annotations.RepositoryPlugin;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.laf.BasePropertyHandler;
import org.pentaho.di.laf.LAFFactory;
import org.pentaho.di.laf.PropertyHandler;
import org.pentaho.di.repository.BaseRepositoryMeta;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemEntryPoint;
import org.pentaho.platform.api.engine.IPentahoSystemExitPoint;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.test.platform.engine.core.SimpleObjectFactory;
import org.w3c.dom.Node;

@SuppressWarnings( "nls" )
/**
 * This test case writes to the current user's home .kettleTest folder and then cleans it up afterwards  
 */
public class RepositorySyncWebServiceTest implements Serializable {

  private static final long serialVersionUID = -5736355727016778872L; /* EESOURCE: UPDATE SERIALVERUID */

  final PropertyHandler handler = (PropertyHandler) LAFFactory.getHandler( PropertyHandler.class );

  public class TestAppContext implements IApplicationContext {
    public String getBaseUrl() {
      return "";
    }

    public File createTempFile( IPentahoSession session, String prefix, String extension, File parentDir,
        boolean trackFile ) throws IOException {
      return null;
    }

    public File createTempFile( IPentahoSession session, String prefix, String extension, boolean trackFile )
      throws IOException {
      return null;
    }

    public String getApplicationPath( String path ) {
      return null;
    }

    public Object getContext() {
      return null;
    }

    public String getFileOutputPath( String path ) {
      return null;
    }

    public String getPentahoServerName() {
      return null;
    }

    public String getProperty( String key ) {
      return null;
    }

    public String getProperty( String key, String defaultValue ) {
      return null;
    }

    public String getSolutionPath( String path ) {
      return "";
    }

    public String getSolutionRootPath() {
      return null;
    }

    public void invokeEntryPoints() {
    }

    public void invokeExitPoints() {
    }

    public void removeEntryPointHandler( IPentahoSystemEntryPoint entryPoint ) {
    }

    public void removeExitPointHandler( IPentahoSystemExitPoint exitPoint ) {
    }

    public void setContext( Object context ) {
    }

    public void setSolutionRootPath( String path ) {
    }

    public void addEntryPointHandler( IPentahoSystemEntryPoint entryPoint ) {
    }

    public void addExitPointHandler( IPentahoSystemExitPoint exitPoint ) {
    }

    public String getFullyQualifiedServerURL() {
      return fullyQualifiedServerUrl;
    }

    public void setFullyQualifiedServerURL( String url ) {
      fullyQualifiedServerUrl = url;

    }

    public void setBaseUrl( String url ) {
    }
  }

  public class TestPropertyHandler implements PropertyHandler {
    public boolean exists( String arg0 ) {
      throw new RuntimeException( "Not Expecting call to exists(" + arg0 + ")" );
    }

    public String getProperty( String arg0 ) {
      return handler.getProperty( arg0 );
      // throw new RuntimeException("Not Expecting call to getProperty("+arg0+")");
    }

    public String getProperty( String arg0, String arg1 ) {
      if ( arg0.equals( "userBaseDir" ) ) {
        return ".kettleTest";
      }
      throw new RuntimeException( "Not Expecting request for " + arg0 );
    }

    public boolean loadProps( String arg0 ) {
      throw new RuntimeException( "Not Expecting call to loadProps" );
    }
  }

  @RepositoryPlugin( id = "PentahoEnterpriseRepository", name = "PentahoEnterpriseRepository",
      metaClass = "com.pentaho.pdi.ws.RepositorySyncWebServiceTest$TestRepositoryMeta" )
  public static class TestRepositoryMeta extends BaseRepositoryMeta implements RepositoryMeta {
    public TestRepositoryMeta() {
      super( "PentahoEnterpriseRepository" );
    }

    public RepositoryCapabilities getRepositoryCapabilities() {
      return null;
    }

    public RepositoryMeta clone() {
      return null;
    }

    String url;

    public String getXML() {
      StringBuffer retval = new StringBuffer( 100 );

      retval.append( "  " ).append( XMLHandler.openTag( XML_TAG ) );
      retval.append( super.getXML() );
      retval.append( "    " ).append( XMLHandler.addTagValue( "repository_location_url", url ) );
      retval.append( "  " ).append( XMLHandler.closeTag( XML_TAG ) );

      return retval.toString();
    }

    public void loadXML( Node repnode, List<DatabaseMeta> databases ) throws KettleException {
      super.loadXML( repnode, databases );
      try {
        url = XMLHandler.getTagValue( repnode, "repository_location_url" );
      } catch ( Exception e ) {
        throw new KettleException( "Unable to load Kettle database repository meta object", e );
      }
    }
  }

  public IRepositorySyncWebService getRepositorySyncWebService() {
    return new RepositorySyncWebService();
  }

  String fullyQualifiedServerUrl = "http://localhost:8080/pentaho-di";

  @After
  public void after() {

    // clean up parent directory
    File f = new File( Const.getKettleUserRepositoriesFile() );
    f.delete();
    f = new File( Const.getKettleDirectory() );
    f.delete();
    BasePropertyHandler.getInstance().notify( (PropertyHandler) LAFFactory.getHandler( PropertyHandler.class ) );
  }

  @Test
  public void testSyncWebService() throws Exception {

    // first init kettle

    KettleEnvironment.init( false );
    BasePropertyHandler.getInstance().notify( new TestPropertyHandler() );
    File f = new File( Const.getKettleDirectory() );
    f.mkdirs();

    // second init platform
    PentahoSystem.registerObjectFactory( new SimpleObjectFactory() );
    PentahoSystem.init( new TestAppContext(), null );
    PentahoSystem.setSystemSettingsService( new ISystemSettings() {
      public String getSystemCfgSourceName() {
        return null;
      }

      public String getSystemSetting( String arg0, String arg1 ) {
        if ( "singleDiServerInstance".equals( arg0 ) ) {
          return "false";
        }
        return arg1;
      }

      public String getSystemSetting( String arg0, String arg1, String arg2 ) {
        return null;
      }

      public List getSystemSettings( String arg0 ) {
        return null;
      }

      public List getSystemSettings( String arg0, String arg1 ) {
        return null;
      }

      public Document getSystemSettingsDocument( String arg0 ) {
        return null;
      }

      public Properties getSystemSettingsProperties( String arg0 ) {
        return null;
      }

      public void resetSettingsCache() {
      }

    } );

    // now test the webservice
    IRepositorySyncWebService webservice = getRepositorySyncWebService();

    // first without the plugin available
    try {
      webservice.sync( "test id", "http://localhost:8080/pentaho-di" );
      Assert.fail();
    } catch ( RepositorySyncException e ) {
      Assert.assertTrue( e.getMessage().indexOf( "unable to load the PentahoEnterpriseRepository plugin" ) >= 0 );
    }

    // second with plugin but not registered
    RepositoryPluginType.getInstance().registerCustom( TestRepositoryMeta.class, "PentahoEnterpriseRepository",
        "PentahoEnterpriseRepository", "PentahoEnterpriseRepository", "PentahoEnterpriseRepository", "" );
    PluginRegistry.getInstance().getPlugin( RepositoryPluginType.class, "PentahoEnterpriseRepository" ).getClassMap()
        .put( RepositoryMeta.class, "com.pentaho.pdi.ws.RepositorySyncWebServiceTest$TestRepositoryMeta" );

    RepositorySyncStatus status = webservice.sync( "test id", "http://localhost:8080/pentaho-di" );

    Assert.assertEquals( RepositorySyncStatus.REGISTERED, status );

    // third after already registered
    status = webservice.sync( "test id", "http://localhost:8080/pentaho-di" );

    Assert.assertEquals( RepositorySyncStatus.ALREADY_REGISTERED, status );

    // forth test with different url
    try {
      webservice.sync( "test id", "http://localhost:9090/pentaho-di" );
      Assert.fail();
    } catch ( RepositorySyncException e ) {
      Assert.assertTrue( e.getMessage().indexOf( "with the URL:" ) >= 0 );
    }

    // fifth test different base-url
    fullyQualifiedServerUrl = "http://localhost:9090/pentaho-di";
    try {
      webservice.sync( "test id", "http://localhost:8080/pentaho-di" );
      Assert.fail();
    } catch ( RepositorySyncException e ) {
      Assert.assertTrue( e.getMessage().indexOf( "fully qualified server url" ) >= 0 );
    }
  }

}
