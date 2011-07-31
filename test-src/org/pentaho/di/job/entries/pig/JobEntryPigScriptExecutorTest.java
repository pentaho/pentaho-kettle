package org.pentaho.di.job.entries.pig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.Job;
import org.pentaho.di.core.KettleEnvironment;

public class JobEntryPigScriptExecutorTest {

  StringBuffer m_reference;

  private StringBuffer readResource(Reader r) throws IOException {

    StringBuffer ret = new StringBuffer();
    char [] buf = new char[5];

    for(int read = r.read(buf); read > 0; read = r.read(buf)) {
      ret.append(new String(buf, 0, read));
    }

    r.close();
    return ret;
  }

  @Before
  public void setup() throws IOException {
    BufferedReader br = 
      new BufferedReader(new InputStreamReader(
         ClassLoader.getSystemResourceAsStream("org/pentaho/di/job/entries/pig/JobEntryPigScriptExecutorTest.ref")));

    m_reference = readResource(br);
  }

  @Test
  public void testRegressionTutorialLocal() throws IOException, KettleException {
    System.setProperty("KETTLE_PLUGIN_CLASSES", "org.pentaho.di.job.entries.pig.JobEntryPigScriptExecutor");
    KettleEnvironment.init();
    JobMeta meta = new JobMeta("test-res/pig/pigTest.kjb", null);
    Job job = new Job(null, meta);

    job.start();
    job.waitUntilFinished();

    BufferedReader br = 
      new BufferedReader(new FileReader("test-res/pig/script1-local-results.txt/part-r-00000"));
    StringBuffer pigOutput = readResource(br);

    assertEquals(m_reference.toString(), pigOutput.toString());    
  }
  
}
