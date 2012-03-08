package org.pentaho.hadoop.mapreduce.test;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.hadoop.mapreduce.MRUtil;

import static org.junit.Assert.*;


/**
 * Tests for {@link MRUtil}.
 */
public class MRUtilTest {

  @Test
  public void getPluginDirProperty() throws KettleException {
    final String USER_DIR = System.getProperty("user.dir");
    
    final Configuration c = new Configuration();
    assertNull(c.get(MRUtil.PROPERTY_PENTAHO_KETTLE_PLUGINS_DIR));

    String pluginDirProperty = MRUtil.getPluginDirProperty(c);
    assertTrue("Plugin Directory Property not configured as expected: " + pluginDirProperty, pluginDirProperty.endsWith(USER_DIR));
  }
  
  @Test
  public void getPluginDirProperty_explicitly_set() throws KettleException {
    final String PLUGIN_DIR = "/opt/pentaho";
    final Configuration c = new Configuration();
    // Working directory will be used for the plugin directory if it is not explicitly provided
    c.set(MRUtil.PROPERTY_PENTAHO_KETTLE_PLUGINS_DIR, PLUGIN_DIR);
    String pluginDirProperty = MRUtil.getPluginDirProperty(c);
    assertTrue("Plugin Directory Property not configured as expected: " + pluginDirProperty, pluginDirProperty.endsWith(PLUGIN_DIR));
  }

  @Test
  public void getKettleHomeProperty() {
    final String USER_DIR = System.getProperty("user.dir");
    final Configuration c = new Configuration();
    String kettleHome = MRUtil.getKettleHomeProperty(c);
    assertEquals(USER_DIR, kettleHome);
  }

  @Test
  public void getKettleHomeProperty_explicitly_set() {
    final String KETTLE_HOME = "/my/kettle";
    final Configuration c = new Configuration();
    // Working directory will be used for Kettle Home if it is not explicitly provided
    c.set(MRUtil.PROPERTY_PENTAHO_KETTLE_HOME, KETTLE_HOME);
    String kettleHome = MRUtil.getKettleHomeProperty(c);
    assertEquals(KETTLE_HOME, kettleHome);
  }

  @Test
  public void createTrans_normalEngine() throws Exception {
    KettleEnvironment.init();
    final Configuration c = new Configuration();
    final TransMeta transMeta = new TransMeta("./test-res/wordcount-reducer.ktr");
    final Trans trans = MRUtil.getTrans(c, transMeta.getXML(), false);
    assertEquals(TransMeta.TransformationType.Normal, trans.getTransMeta().getTransformationType());
  }

  @Test
  public void createTrans_singleThreaded() throws Exception {
    KettleEnvironment.init();
    final Configuration c = new Configuration();
    final TransMeta transMeta = new TransMeta("./test-res/wordcount-reducer.ktr");
    final Trans trans = MRUtil.getTrans(c, transMeta.getXML(), true);
    assertEquals(TransMeta.TransformationType.SingleThreaded, trans.getTransMeta().getTransformationType());
  }
}
