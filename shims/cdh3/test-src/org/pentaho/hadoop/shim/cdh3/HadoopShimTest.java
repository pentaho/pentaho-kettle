package org.pentaho.hadoop.shim.cdh3;

import static org.junit.Assert.*;

import org.junit.Test;

public class HadoopShimTest {

  @Test
  public void testGetDefaultNamenodePort() {
    assertEquals("8020", new HadoopShim().getDefaultNamenodePort());
  }

  @Test
  public void testGetDefaultJobtrackerPort() {
    assertEquals("8021", new HadoopShim().getDefaultJobtrackerPort());
  }

}
