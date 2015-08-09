package org.pentaho.di.job.entries.setvariables;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;

public class JobEntrySetVariablesTest {
  private Job job;
  private JobEntrySetVariables entry;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleLogStore.init();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    job = new Job( null, new JobMeta() );
    entry = new JobEntrySetVariables();
    job.getJobMeta().addJobEntry( new JobEntryCopy( entry ) );
    entry.setParentJob( job );
    job.setStopped( false );
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testASCIIText() throws Exception {
    // properties file with native2ascii
    entry.setFilename("test-src/org/pentaho/di/job/entries/setvariables/ASCIIText.properties");
    entry.setVariableName( new String[]{} ); // For absence of null check in execute method
    entry.setReplaceVars(true);
    Result result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertEquals( "日本語", entry.getVariable("Japanese") );
    assertEquals( "English", entry.getVariable("English") );
    assertEquals( "中文", entry.getVariable("Chinese") );
  }

  @Test
  public void testUTF8Text() throws Exception {
    // properties files without native2ascii
    entry.setFilename("test-src/org/pentaho/di/job/entries/setvariables/UTF8Text.properties");
    entry.setVariableName( new String[]{} ); // For absence of null check in execute method
    entry.setReplaceVars(true);
    Result result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertEquals( "日本語", entry.getVariable("Japanese") );
    assertEquals( "English", entry.getVariable("English") );
    assertEquals( "中文", entry.getVariable("Chinese") );
  }
}
