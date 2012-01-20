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
    final String PLUGIN_DIR = "/opt/pentaho";
    final Configuration c = new Configuration();
    c.set(MRUtil.PROPERTY_PENTAHO_KETTLE_PLUGINS_DIR, PLUGIN_DIR);
    String pluginDirProperty = MRUtil.getPluginDirProperty(c);
    assertTrue("Plugin Directory Property not configured as expected: " + pluginDirProperty, pluginDirProperty.endsWith(PLUGIN_DIR));
  }

  @Test
  public void getPluginDirProperty_missingKettlePluginsDirProperty() {
    final Configuration c = new Configuration();
    try {
      MRUtil.getPluginDirProperty(c);
      fail("Expected exception for missing " + MRUtil.PROPERTY_PENTAHO_KETTLE_PLUGINS_DIR);
    } catch (KettleException ex) {
      assertTrue("Wrong exception: " + ex.getMessage(), ex.getMessage().contains(MRUtil.PROPERTY_PENTAHO_KETTLE_PLUGINS_DIR));
    }
  }

  @Test
  public void createTrans_missingKettleHomeProperty() {
    final Configuration c = new Configuration();
    c.set(MRUtil.PROPERTY_PENTAHO_KETTLE_PLUGINS_DIR, "/opt/pentaho");
    try {
      MRUtil.getTrans(c, null, false);
      fail("Expected exception for missing " + MRUtil.PROPERTY_PENTAHO_KETTLE_HOME);
    } catch (KettleException ex) {
      assertTrue("Wrong exception: " + ex.getMessage(), ex.getMessage().contains(MRUtil.PROPERTY_PENTAHO_KETTLE_HOME));
    }
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
