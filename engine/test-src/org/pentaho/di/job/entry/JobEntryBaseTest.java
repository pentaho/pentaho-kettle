package org.pentaho.di.job.entry;

import org.junit.Assert;
import org.junit.Test;

public class JobEntryBaseTest {

  /**
   * PDI-10553 - log output add/delete filenames to/from result steps shows CheckDb connections
   */
  @Test
  public void testIdIsNullByDefault() {
    JobEntryBase base = new JobEntryBase();
    Assert.assertNull("Object ID is null by default", base.getObjectId() );
  }

}
