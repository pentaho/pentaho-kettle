package org.pentaho.hadoop.jobconf;

import org.junit.Test;
import org.pentaho.hadoop.HadoopConfigurerException;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for {@link HadoopConfigurerFactory}
 */
public class HadoopConfigurerFactoryTest {
  @Test
  public void getAvailableConfigurers() {
    List<HadoopConfigurer> configurers = HadoopConfigurerFactory.getAvailableConfigurers();

    // Default list of configurers will contain any configurer whose configurer.isDetectable() == false
    assertEquals(2, configurers.size());
    assertEquals(ClouderaHadoopConfigurer.class, configurers.get(0).getClass());
    assertEquals(GenericHadoopConfigurer.class, configurers.get(1).getClass());
  }

  @Test
  public void getConfigurer() throws HadoopConfigurerException {
    final HadoopConfigurer c = HadoopConfigurerFactory.getConfigurer(GenericHadoopConfigurer.DISTRIBUTION_NAME);
    assertNotNull(c);
  }

  @Test
  public void getConfigurer_unknown() {
    final String BOGUS = "bogus hadoop distro";
    try {
      HadoopConfigurerFactory.getConfigurer(BOGUS);
      fail("Expected exception for invalid configurer lookup");
    } catch (HadoopConfigurerException ex) {
      assertTrue(ex.getMessage().contains(BOGUS));
    }
  }

  @Test
  public void locateConfigurer() {
    HadoopConfigurer c = HadoopConfigurerFactory.locateConfigurer();
    assertNull(c);
  }
}
