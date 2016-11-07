package org.pentaho.di.ui.job.entries.job;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

/**
 * Created by Vadim_Polynkov on 11/7/2016.
 */
public class JobEntryJobDialogTest {

  private static final String FILE_NAME =  "TestJob.kjb";

  JobEntryJobDialog dialog;

  @Test
  public void testVariable() throws Exception {
    dialog = mock ( JobEntryJobDialog.class );
    doCallRealMethod().when( dialog ).getEntryName( any() );

    assertEquals( dialog.getEntryName( FILE_NAME ), "${Internal.Entry.Current.Directory}/" + FILE_NAME );
  }
}
