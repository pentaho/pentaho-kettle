package org.pentaho.di.job.entries.mail;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;

public class JobEntryTest {

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Test
  public void testJobEntrymailPasswordFixed() {
    JobEntryMail jem = new JobEntryMail();
    Assert.assertEquals( jem.getPassword( "asdf" ), "asdf" );
  }

  @Test
  public void testJobEntrymailPasswordEcr() {
    JobEntryMail jem = new JobEntryMail();
    Assert.assertEquals( jem.getPassword( "Encrypted 2be98afc86aa7f2e4cb79ce10df81abdc" ), "asdf" );
  }

  @Test
  public void testJobEntrymailPasswordVar() {
    JobEntryMail jem = new JobEntryMail();
    jem.setVariable( "my_pass", "asdf" );
    Assert.assertEquals( jem.getPassword( "${my_pass}" ), "asdf" );
  }

  @Test
  public void testJobEntrymailPasswordEncrVar() {
    JobEntryMail jem = new JobEntryMail();
    jem.setVariable( "my_pass", "Encrypted 2be98afc86aa7f2e4cb79ce10df81abdc" );
    Assert.assertEquals( jem.getPassword( "${my_pass}" ), "asdf" );
  }
}
