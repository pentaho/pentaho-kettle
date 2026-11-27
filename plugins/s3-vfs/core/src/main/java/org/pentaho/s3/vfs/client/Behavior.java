package org.pentaho.s3.vfs.client;

import software.amazon.awssdk.regions.Region;

/**
 * This keeps track of some behavior from previous driver that is considered questionable
 * but was kept for backward compatibility
 */
public class Behavior {
  private static final boolean OVERRIDE_REGION = true;

  private static final boolean PREFER_ENV_CREDENTIALS = true;

  /** Override region from environment even if a region is explicitly defined. */
  public boolean overrideRegionFromEnv() {
    return OVERRIDE_REGION;
  }

  /**
   * Wether to always give preference to environment credentials
   * As a side effect, a change in the {@code aws.accessKeyId} / {@code aws.secretKey} system properties
   * replaces a client that was already built from configuration.
   * <p>
   * This flags keep track of where this choice is made in the code. Additional changes may
   * be needed to disable it.
   */
  public boolean preferEnvCredentials() {
    return PREFER_ENV_CREDENTIALS;
  }

  public Region defaultRegion() {
    return Region.US_EAST_1;
  }

}
