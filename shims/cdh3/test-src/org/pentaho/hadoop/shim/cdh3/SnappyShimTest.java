package org.pentaho.hadoop.shim.cdh3;

import static org.junit.Assert.*;

import org.junit.Test;

public class SnappyShimTest {

  @Test
  public void isHadoopSnappyAvailable() {
    SnappyShim shim = new SnappyShim();
    assertFalse(shim.isHadoopSnappyAvailable());
  }

}
