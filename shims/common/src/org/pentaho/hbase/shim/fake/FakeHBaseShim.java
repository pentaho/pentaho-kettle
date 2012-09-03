package org.pentaho.hbase.shim.fake;

import org.pentaho.hadoop.shim.ShimVersion;
import org.pentaho.hbase.shim.spi.HBaseConnection;
import org.pentaho.hbase.shim.spi.HBaseShim;

public class FakeHBaseShim extends HBaseShim {

  public ShimVersion getVersion() {
    return new ShimVersion(1, 0);
  }

  @Override
  public HBaseConnection getHBaseConnection() {
    return new FakeHBaseConnection();
  }
}
