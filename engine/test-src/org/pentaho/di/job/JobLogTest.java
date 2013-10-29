package org.pentaho.di.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.Result;

public class JobLogTest {
  
  public static final String NAME = "Junit_JobTest";
  public static final String USER = "JUNIT";
  
  public static String CREATE="logTableCreate.sql";
  public static String HEADERS="logTableHeaders.csv";
  
  public static String JOB_1 = "log_job_1.kjb";
  public static String JOB_2 = "log_job_2.kjb";
  public static String JOB_CALLER_1 = "log_job_1_caller.kjb";
  public static String JOB_CALLER_2 = "log_job_2_caller.kjb";
  public static String JOB_TRANS_CALLER_1 = "log_trans_1_caller.kjb";
  public static String JOB_TRANS_CALLER_2 = "log_trans_2_caller.kjb";
  
  public static String PKG="";
  private static String TMP;
  
  private static DatabaseMeta databaseMeta;
  private static LoggingObjectInterface log;
  private static Database logDataBase;
  
  private static String[] H2_GARBG = {".h2.db", ".trace.db"};
    
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
    log = new SimpleLoggingObject("junit", LoggingObjectType.GENERAL, null);
    
    PKG = JobLogTest.class.getPackage().getName().replace(".", "/");
    PKG = PKG+"/";
        
    File file = File.createTempFile(JobLogTest.class.getSimpleName(), "");
    file.deleteOnExit();
    TMP = file.getCanonicalPath();    
    
    databaseMeta = new DatabaseMeta(NAME, "H2", "JDBC", null, TMP, null, USER, USER);
    logDataBase = new Database(log, databaseMeta);
    logDataBase.connect();
    
    //run sql create for database
    InputStream input = JobLogTest.class.getClassLoader().getResourceAsStream(PKG+CREATE);
    String sql = JobLogTest.getStringFromInput(input);
    logDataBase.execStatements(sql);
    logDataBase.commit(true);
    
    logDataBase.disconnect();    
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    logDataBase.disconnect();   
    for (int i=0;i<H2_GARBG.length;i++){
      File temp = new File(TMP+H2_GARBG[i]);
      temp.deleteOnExit();
      temp.delete();
    }
  }

  /**
   * Test PDI-9790.
   * If during job execution attempt to write to job log database
   * is failed - JobEntryResult should contains information about it.
   * 
   * originaly:
   * 
   * This is reproduction of 
   *   - log_job_1.kbj (simulates an issue at start of job)
      Seen behavior: 
      - log table issue is logged (correct)
      - Job metrics keeps in start
      Expected behavior:
      - log table issue is logged (correct)
      - Job metrics should show Failure of the job
   * 
   * @throws IOException
   * @throws KettleException
   * @throws SQLException
   * @throws URISyntaxException
   * @throws InterruptedException
   */
  @Test
  public void testJobStartLogNegative() throws IOException, KettleException, SQLException, URISyntaxException, InterruptedException {
    Job job = new Job(null, getJobMeta(JOB_1));
    job.setLogLevel(LogLevel.DETAILED);
    
    job.start();
    job.waitUntilFinished();
    
    //this simulates - Spoon 'Job Metrics' tab attempt to refresh:
    JobTracker tracker = job.getJobTracker(); 
    List<JobTracker> trackers = tracker.getJobTrackers();
    
    Assert.assertTrue("2 trackers records: ", trackers.size()==2);
    //(Job metrics should show Failure of the job)
    Assert.assertFalse( "Last result is unseccess", trackers.get( 1 ).getJobEntryResult().getResult().getResult() );    
  }
  
  /**
   * Test PDI-9790
   * 
   * This is reproduction of 
   * - log_job_1.kbj (simulates an issue at start of job),
   * but for positive scenario (without Exception)
   * 
   * (Use database table that allows long text values)
   * 
   * @throws UnknownParamException
   * @throws KettleXMLException
   * @throws URISyntaxException
   * @throws IOException
   */
  @Test
  public void testJobStartLogPositive() throws UnknownParamException, KettleXMLException, URISyntaxException, IOException {
    JobMeta jobMeta = getJobMeta(JOB_1);
    jobMeta.setParameterValue("junit.logtable", "LOG_JOB_TEST15_CLOB");
    
    Job job = new Job(null, jobMeta);
    job.setLogLevel(LogLevel.DETAILED);
    
    job.start();
      job.waitUntilFinished();
    
      //this simulates - Spoon 'Job Metrics' tab attempt to refresh:
    JobTracker tracker = job.getJobTracker();   
    List<JobTracker> trackers = tracker.getJobTrackers();
    
    Assert.assertNotNull("6 result trackers available", trackers.size()==6);
    
    long actual;
    Result result;
    //iterate all results
    for (int i=0; i<trackers.size();i++){
      JobTracker tr = trackers.get( i );
      result = tr.getJobEntryResult().getResult();
      if (result!=null){
        actual = tr.getJobEntryResult().getResult().getNrErrors();
        Assert.assertEquals("there is no errors for this transformation", 0, actual); 
      }
    }
  }

  /**
   * Test PDI-9790
   * Simulates the issue at the end of the job execution.
   * (at attempt to write success log record (ERROR: value too long for type 
   * character varying(255)) throws Exception etc...)
   * ... and then incorrect results is shown in JobMetrics tab.
   * 
   * Expected behavior - last step should show incorrect result.
   * 
   * log_job_2.kbj (simulates an issue at end of job)
      Seen behavior: 
      - log table issue is logged (correct)
      - Job metrics shows Success of job
      Expected behavior:
      - log table issue is logged (correct)
      - Job metrics should show Failure of the job
   * 
   * @throws UnknownParamException
   * @throws KettleXMLException
   * @throws URISyntaxException
   * @throws IOException
   */
  @Test
  public void testJobEndLogNegative() throws UnknownParamException, KettleXMLException, URISyntaxException, IOException{
    Job job = new Job(null, getJobMeta(JOB_2));
    //Attempt to set log level to detailed will cause KettleJobException at start!
    //job.setLogLevel(LogLevel.DETAILED);
    
    job.start();
    job.waitUntilFinished();
    
      //this simulates - Spoon 'Job Metrics' tab attempt to refresh:
    JobTracker tracker = job.getJobTracker();
    List<JobTracker> trackers = tracker.getJobTrackers();
      
    //for this case we have 7 trackers messages,
    Assert.assertEquals(trackers.size(), 7);
    
    boolean success = trackers.get(6).getJobEntryResult().getResult().getResult();
    Assert.assertFalse("Last result is fail.", success);
  }
  
  /**
   * Test PDI-9790 (originally correct behavior)
   * - log_job_1_caller.kbj (calls log_job_1)
   * Note, job caller does not have it's own defined log table.
   * 
   * log_job_1_caller.kbj (calls log_job_1)
      Seen behavior: 
      - log table issue is logged (correct)
      - logs "Finished job entry [log_job_1.kjb] (result=[false])" (correct)
      - Job metrics follows the DUMMY path and shows Failure of the job (correct)     
      Expected behavior:
      - (all correct as seen behavior)
   * 
   * @throws IOException 
   * @throws URISyntaxException 
   * @throws KettleXMLException 
   * @throws UnknownParamException 
   * 
   */
  @Test
  public void testJobLogCallerAtStartPositive() throws UnknownParamException, KettleXMLException, URISyntaxException, IOException{
    Job job = new Job(null, getJobMeta(JOB_CALLER_1));
    job.setLogLevel(LogLevel.DETAILED);
    
    job.start();
    job.waitUntilFinished();
      
    //this simulates - Spoon 'Job Metrics' tab attempt to refresh:
    JobTracker tracker = job.getJobTracker();
    List<JobTracker> trackers = tracker.getJobTrackers();
    
    //as a result of execution we have 8 trackers messages,
    Assert.assertEquals(trackers.size(), 8);
      
    //error was generated by nested job.
    JobTracker traker = trackers.get(4);
    Assert.assertEquals("5 position is for nested job", JOB_1, traker.getJobEntryResult().getJobEntryName());
    //error is reported!
    long actual = traker.getJobEntryResult().getResult().getNrErrors();
    Assert.assertEquals(1, actual);
  }
  
  /**
   *  Test PDI-9790   
   *   
   *   - log_job_2_caller.kbj (calls log_job_2)
    Seen behavior: 
    - log table issue is logged (correct)
    - logs "Finished job entry [log_job_2.kjb] (result=[true])"
    - Job metrics shows Success of job
    Expected behavior:
    - log table issue is logged (correct)
    - log should show result=[false]
    - Job metrics should show Failure of the job    
   * 
   * 
   * 
   * 
   * @throws UnknownParamException
   * @throws KettleXMLException
   * @throws URISyntaxException
   * @throws IOException
   */
  @Test
  public void testJobLogCallerAtEnd() throws UnknownParamException, KettleXMLException, URISyntaxException, IOException{
    Job job = new Job(null, getJobMeta(JOB_CALLER_2));
    job.setLogLevel(LogLevel.DETAILED);
    
    job.start();
    job.waitUntilFinished();
    
    //this simulates - Spoon 'Job Metrics' tab attempt to refresh:
    JobTracker tracker = job.getJobTracker();
    List<JobTracker> trackers = tracker.getJobTrackers();
    
    Assert.assertEquals("9 items is available: ", 9, trackers.size());
    
    JobTracker track = trackers.get( 5 );
    Assert.assertFalse("Shows inner job is failed. ", track.getJobEntryResult().getResult().getResult());    
  }
  
  
  /**
   * Test PDI-9790
   * (the difference is that logging is defined for transformation itself,
   * not for job.)
   * 
   * error from db side is:
   * Error inserting/updating row
   * ERROR: value too long for type character varying(15)
   *  
   * log_trans_1_caller.kbj (calls log_trans_5sec_1.ktr)
      Seen behavior: 
      - log table issue is logged (correct)
      - logs "Finished job entry [log_trans_5sec_1.ktr] (result=[false])" (correct)
      - Job metrics follows the DUMMY path and shows Failure of the job (correct)     
      Expected behavior:
      - (all correct as seen behavior)
   * 
   * @throws UnknownParamException
   * @throws KettleXMLException
   * @throws URISyntaxException
   * @throws IOException
   */
  @Test
  public void testJobLogTransCallerPositive() throws UnknownParamException, KettleXMLException, URISyntaxException, IOException{
    Job job = new Job(null, getJobMeta(JOB_TRANS_CALLER_1));
    job.setLogLevel(LogLevel.DETAILED);
    
    job.start();
      job.waitUntilFinished();
      
      JobTracker tracker = job.getJobTracker();
      List<JobTracker> trackers = tracker.getJobTrackers();
      
      //there is 8 entries now
      Assert.assertTrue(trackers.size()==8);
      
      JobTracker track = trackers.get(4);
      //this is step for transformation
      Assert.assertEquals("log_trans_5sec_1.ktr", track.getJobEntryResult().getJobEntryName());
      //is is in error state.
      Assert.assertTrue("Step for transformation is failed. ",track.getJobEntryResult().getResult().getNrErrors()>0);   
  }
  
  /**
   * Test PDI-9790
   * Job generates Exception at the end: Value too long for column "LOG_FIELD VARCHAR(2000)"...
   * 
   *   - log_trans_2_caller.kbj (calls log_trans_5sec_2.ktr)
      Seen behavior: 
      - log table issue is logged (correct)
      - logs "Finished job entry [log_trans_5sec_2.ktr] (result=[true])"
      - Job metrics shows Success of job
      Expected behavior:
      - log table issue is logged (correct)
      - log should show result=[false]
      - Job metrics should show Failure of the job and follows the DUMMY path
   * 
   * 
   * @throws UnknownParamException
   * @throws KettleXMLException
   * @throws URISyntaxException
   * @throws IOException
   */
  @Test
  public void testJobLogTransCaller2Negative() throws UnknownParamException, KettleXMLException, URISyntaxException, IOException{
    Job job = new Job(null, getJobMeta(JOB_TRANS_CALLER_2));
    job.setLogLevel(LogLevel.DETAILED);
    
    job.start();
    job.waitUntilFinished();
      
    JobTracker tracker = job.getJobTracker();
    List<JobTracker> trackers = tracker.getJobTrackers();   
    
    Assert.assertTrue("Exact 8 entries for Job tracker", trackers.size()==8);
    JobTracker track = trackers.get(4);
    //this is internal transaction fail: 
    Assert.assertEquals("log_trans_5sec_2.ktr", track.getJobEntryResult().getJobEntryName());
    //error is reported
    Assert.assertTrue(track.getJobEntryResult().getResult().getNrErrors()>0);
    
    //check the error step result (we follow the DUMMY path as transaction was failed):
    track = trackers.get(6);
    Assert.assertEquals("DUMMY", track.getJobEntryResult().getJobEntryName());
    //there is no errors
    Assert.assertEquals(0, track.getJobEntryResult().getResult().getNrErrors());
  }  
  
  private JobMeta getJobMeta(String resource) throws KettleXMLException, URISyntaxException, IOException, UnknownParamException{
    JobMeta jobMeta = new JobMeta(getCanonicalPath(resource), null);
    jobMeta.setParameterValue("junit.name", TMP);
    jobMeta.setParameterValue("junit.user", USER);
    jobMeta.setParameterValue("junit.password", USER);
    return jobMeta;
  }
  
  public static String getCanonicalPath(String resource) throws URISyntaxException, IOException{
    URL url = JobLogTest.class.getClassLoader().getResource(PKG+resource);
    File file = new File (url.toURI());
    return file.getCanonicalPath();
  }
  
  private static String getStringFromInput(InputStream in) throws IOException{
    InputStreamReader is = new InputStreamReader(in);
    BufferedReader br = new BufferedReader(is);
    StringBuilder sb=new StringBuilder();
    String read = br.readLine();
    while(read != null) {
        sb.append(read);
        read =br.readLine();
    }
    return sb.toString();
  }
  
  public static List<String> getJobDefaultRunParameters(){
    List<String> list = new ArrayList<String>();
    list.add( "-param:junit.name="+TMP );
    list.add( "-param:junit.user="+USER );
    list.add( "-param:junit.password="+USER );    
    return list;
  }
}
