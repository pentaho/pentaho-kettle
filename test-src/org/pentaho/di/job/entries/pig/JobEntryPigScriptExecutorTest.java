package org.pentaho.di.job.entries.pig;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs.VFS;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.hadoop.HadoopConfigurationBootstrap;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.hadoop.shim.ConfigurationException;
import org.pentaho.hadoop.shim.HadoopConfiguration;
import org.pentaho.hadoop.shim.common.CommonHadoopShim;
import org.pentaho.hadoop.shim.common.CommonPigShim;
import org.pentaho.hadoop.shim.common.CommonSqoopShim;
import org.pentaho.hadoop.shim.spi.HadoopConfigurationProvider;

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
         ClassLoader.getSystemResourceAsStream("resources/org/pentaho/di/job/entries/pig/JobEntryPigScriptExecutorTest.ref")));

    m_reference = readResource(br);
  }

  @Test
  public void testRegressionTutorialLocal() throws Exception {
    HadoopConfigurationProvider provider = new HadoopConfigurationProvider() {
      
      HadoopConfiguration config = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "test", "test", new CommonHadoopShim(), new CommonSqoopShim(), new CommonPigShim());
      
      @Override
      public boolean hasConfiguration(String id) {
        return true;
      }
      
      @Override
      public List<? extends HadoopConfiguration> getConfigurations() {
        return Arrays.asList(config);
      }
      
      @Override
      public HadoopConfiguration getConfiguration(String id) throws ConfigurationException {
        return config;
      }
      
      @Override
      public HadoopConfiguration getActiveConfiguration() throws ConfigurationException {
        return config;
      }
    };
    
    Field providerField = HadoopConfigurationBootstrap.class.getDeclaredField("provider");
    providerField.setAccessible(true);
    providerField.set(null, provider);
    
    System.setProperty("KETTLE_PLUGIN_CLASSES", "org.pentaho.di.job.entries.pig.JobEntryPigScriptExecutor");
    KettleEnvironment.init();
    JobMeta meta = new JobMeta("test-res/pig/pigTest.kjb", null);
    
    Job job = new Job(null, meta);

    job.start();
    job.waitUntilFinished();

    BufferedReader br = 
      new BufferedReader(new FileReader("bin/test/pig/script1-local-results.txt/part-r-00000"));
    StringBuffer pigOutput = readResource(br);

    assertEquals(m_reference.toString(), pigOutput.toString());    
  }
  
}
