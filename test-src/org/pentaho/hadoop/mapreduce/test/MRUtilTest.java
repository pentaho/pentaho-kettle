package org.pentaho.hadoop.mapreduce.test;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.hadoop.mapreduce.MRUtil;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


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
      MRUtil.getTrans(c, null);
      fail("Expected exception for missing " + MRUtil.PROPERTY_PENTAHO_KETTLE_HOME);
    } catch (KettleException ex) {
      assertTrue("Wrong exception: " + ex.getMessage(), ex.getMessage().contains(MRUtil.PROPERTY_PENTAHO_KETTLE_HOME));
    }
  }
}
