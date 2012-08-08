package org.pentaho.hadoop.shim.spi;

import org.pentaho.hadoop.shim.ShimVersion;

/**
 * Represents a type of Hadoop shim. Shims provide an abstraction over a set of
 * APIs that depend upon a set of specific Hadoop libraries. Their implementations
 * must be abstracted so that they may be swapped out at runtime.
 *
 */
public interface PentahoHadoopShim {
  /**
   * @return the version of this shim
   */
  ShimVersion getVersion();
}
