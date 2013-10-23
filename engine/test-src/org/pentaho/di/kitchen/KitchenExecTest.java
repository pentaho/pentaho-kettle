package org.pentaho.di.kitchen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.job.JobLogTest;

/**
 * As Kitchen uses System.exit() we can't run it in
 * junit's JVM process. To avoid it - we will 
 * create separate process (JVM) instance.
 * 
 * Kitchen exit statuses:
    0 : The job ran without a problem.
    1 : Errors occurred during processing
    2 : An unexpected error occurred during loading / running of the job
    7 : The job couldn't be loaded from XML or the Repository
    8 : Error loading steps or plugins (error in loading one of the plugins mostly)
    9 : Command line usage printing
 * 
 * 
 * @see http://wiki.pentaho.com/display/EAI/Kitchen+User+Documentation
 */
public class KitchenExecTest {

  private static List<String> COMMAND = new ArrayList<String>(3);
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    JobLogTest.setUpBeforeClass();
    
    StringBuilder sb = new StringBuilder(System.getProperty("java.home"));
    sb.append( File.separator );
    sb.append( "bin" );
    sb.append( File.separator );
    sb.append( "java" );
    COMMAND.add( sb.toString() );  
    COMMAND.add( "-cp" );
    COMMAND.add( System.getProperty("java.class.path") );
    COMMAND.add( Kitchen.class.getCanonicalName() );
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    JobLogTest.tearDownAfterClass();
  }

  /**
   * Test Kitchen can be run in separate process (ensure launching conf is correct)
   * 
   * @throws IOException
   * @throws InterruptedException
   */
  @Test
  public void testUsagePrinting() throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder(COMMAND);
    
    Integer exit = this.timeRestrictionRun( builder );
    Assert.assertNotNull( exit );    
    Assert.assertEquals("Test run kitchen without additional parameters", 9, (int) exit );
  }

  /**
   * PDI-9790 Test
   * 
   *   - Run sample log_job_1.bat (modify the path inside accordingly)
      Seen behavior: 
      - console log table issue is logged (correct)
      - console log shows Kitchen - Finished with errors (correct)
      - Errorlevel is 1 (correct)
      Expected behavior:
      - (all correct as seen behavior)
   * 
   * @throws URISyntaxException
   * @throws IOException
   * @throws InterruptedException
   */
  @Test
  public void testJobRunErrorAtStart() throws URISyntaxException, IOException, InterruptedException{  
    ProcessBuilder builder = getProcessForJob(JobLogTest.JOB_1);
    File err = this.redirectError( builder );
    
    Integer exit = this.timeRestrictionRun( builder );
    Assert.assertNotNull( exit );    
    Assert.assertEquals( "Errorlevel is correct", 1, (int) exit);
    
    //console log shows Kitchen - Finished with errors (correct)
    String conStr = "Unable to begin processing by logging start";
    Assert.assertTrue( "Error log contains error", this.findInFile( err, conStr ));
  }
  
  /**
   * PDI-9790 Test
   * 
   *   - Run sample log_job_2.bat (modify the path inside accordingly)
    Seen behavior: 
      - console log table issue is logged (correct)
    - console log shows Kitchen - Finished!
    - Errorlevel is 0
    Expected behavior:    
    - console log table issue is logged (correct)
    - console log shows Kitchen - Finished with errors
    - Errorlevel is 1
   * 
   * @throws URISyntaxException
   * @throws IOException
   * @throws InterruptedException
   */
  @Test
  public void testJobRunErrorAtEnd() throws URISyntaxException, IOException, InterruptedException{
    ProcessBuilder builder = getProcessForJob(JobLogTest.JOB_2); 
    File err = this.redirectError( builder );
    
    Integer exit = this.timeRestrictionRun( builder );
    Assert.assertNotNull( exit );    
    Assert.assertEquals( "Errorlevel is 1", 1, (int) exit);
    
    //console log table issue is logged (correct)
    String conStr = "Unable to end processing by writing log record to table";
    Assert.assertTrue( "Error log contains error", this.findInFile( err, conStr ));
  }
  
  /**
   * PDI 9790 Test
   * 
   *   - Run sample log_trans_1_caller.bat (modify the path inside accordingly)
      Seen behavior: 
      - console log table issue is logged (correct)
      - console log shows "Finished job entry [DUMMY] (result=[false])" (correct)
      - console log shows "Finished job entry [log_trans_5sec_1.ktr] (result=[false])" (correct)    
      - console log shows Kitchen - Finished! (correct)
      - Errorlevel is 0 (correct)
      Expected behavior:    
      - (all correct as seen behavior)
   * 
   * @throws URISyntaxException
   * @throws IOException
   * @throws InterruptedException 
   */
  @Test
  public void testJobTransCallAtStart() throws URISyntaxException, IOException, InterruptedException{
    ProcessBuilder builder = getProcessForJob(JobLogTest.JOB_TRANS_CALLER_1);
    File err = this.redirectError( builder );
    File out = this.redirectOutput( builder );
    
    Integer exit = this.timeRestrictionRun( builder );
    Assert.assertNotNull( exit );   
    Assert.assertEquals( "Errorlevel is 0", 0, (int) exit);

    String outStr = "Finished job entry [DUMMY] (result=[false])";
    Assert.assertTrue( "console log shows Finished job entry [DUMMY] (result=[false])", 
        this.findInFile( out, outStr ));
    
    //console log table issue is logged (correct)
    outStr = "Finished job entry [log_trans_5sec_1.ktr] (result=[false])";
    Assert.assertTrue( "console log shows Finished job entry [log_trans_5sec_1.ktr] (result=[false])", 
        this.findInFile( out, outStr ));
    
    outStr = "Error writing log record to table";
    Assert.assertTrue( "console log table issue is logged", 
        this.findInFile( err, outStr ));
  }
  
  /**
   * PDI 9790 Test
   * 
   *   - Run sample log_trans_2_caller.bat (modify the path inside accordingly)
      Seen behavior: 
        - NO console log table issue is logged (wrong!, the status in the log table is still 'start')
      - console log shows "Finished job entry [Success] (result=[true])" (wrong)
      - console log shows "Finished job entry [log_trans_5sec_2.ktr] (result=[true])" (wrong)   
      - console log shows Kitchen - Finished! (correct)
      - Errorlevel is 0 (correct)
      Expected behavior:    
        - console log table issue is logged
      - console log shows "Finished job entry [DUMMY] (result=[false])"
      - console log shows "Finished job entry [log_trans_5sec_2.ktr] (result=[false])"
      - console log shows Kitchen - Finished!
      - Errorlevel is 0
   * 
   * @throws URISyntaxException
   * @throws IOException
   * @throws InterruptedException
   */
  @Test
  public void testJobTransCallAtEnd() throws URISyntaxException, IOException, InterruptedException{
    ProcessBuilder builder = getProcessForJob(JobLogTest.JOB_TRANS_CALLER_2);
    File err = this.redirectError( builder );
    File out = this.redirectOutput( builder );
    
    Integer exit = this.timeRestrictionRun( builder );
    Assert.assertNotNull( exit );   
    Assert.assertEquals( "Errorlevel is 0", 0, (int) exit);
    
    //console log table issue is logged
    String outStr = "Error writing log record to table";
    Assert.assertTrue( "console log table issue is logged", 
        this.findInFile( err, outStr ));
    
    //console log shows Finished job entry [DUMMY] (result=[false])
    outStr = "Finished job entry [DUMMY] (result=[false])";
    Assert.assertTrue( "console log shows Finished job entry [DUMMY] (result=[false])", 
        this.findInFile( out, outStr ));
    
    //console log shows Finished job entry [log_trans_5sec_2.ktr] (result=[false])
    outStr = "Finished job entry [log_trans_5sec_2.ktr] (result=[false])";
    Assert.assertTrue( "console log shows Finished job entry [log_trans_5sec_2.ktr] (result=[false])", 
        this.findInFile( out, outStr ));    
  }  
  
  private ProcessBuilder getProcessForJob(String job) throws URISyntaxException, IOException{
    String path = JobLogTest.getCanonicalPath( job );
    List<String> local = new ArrayList<String>();
    local.addAll( COMMAND );
    local.add( "-file="+path );
    local.addAll( JobLogTest.getJobDefaultRunParameters() );
    
    ProcessBuilder builder = new ProcessBuilder(local);
    //by default
    builder.redirectOutput( Redirect.INHERIT );
    builder.redirectError( Redirect.INHERIT );
    
    return builder;
  }
  
  private File redirectOutput(ProcessBuilder builder) throws IOException{
    File temp = File.createTempFile( "junit_out_", null );
    temp.deleteOnExit();
    builder.redirectOutput( temp );
    return temp;
  }
  
  private File redirectError(ProcessBuilder builder) throws IOException{
    File temp = File.createTempFile( "junit_err_", null );
    temp.deleteOnExit();
    builder.redirectError( temp );
    return temp;
  }
  
  private boolean findInFile(File file, String string) throws IOException{
    Reader reader = new FileReader(file);
    BufferedReader br = null;
    try{
      br = new BufferedReader(reader);
      String line = br.readLine();
      while (line != null) {
        if (line.contains( string )){
          return true;        
        }
        line = br.readLine();
      }
      return false;
    } finally {
      if (br!=null){
        br.close();
      }
    }
  }
  
  
  /**
   * Restrict execution up to 2 minutes.
   * Avoid possible process halting
   * 
   * @param builder
   * @return Kitchen exit status
   * @throws InterruptedException
   * @throws ExecutionException
   * @throws TimeoutException
   */
  private Integer timeRestrictionRun(ProcessBuilder builder) {
    return this.timeRestrictionRun( builder, 2, TimeUnit.MINUTES);
  }
  
  private Integer timeRestrictionRun(ProcessBuilder builder, long val, TimeUnit tunit) {
    ExecutorService service = Executors.newSingleThreadExecutor();
    TimerKiller task = new TimerKiller(builder);
    Future<Integer> res = service.submit( task );
    Integer exitCode = null;

    try {
      exitCode = res.get(val, tunit);
    } catch ( Exception e ) {
      service.shutdownNow();
    }
    
    service.shutdownNow();
    return exitCode;
  }
  
  /**
   * Restrict Kitchen evaluation time
   */
  private class TimerKiller implements Callable<Integer>{
    
    private ProcessBuilder builder;
    
    TimerKiller(ProcessBuilder builder){
      this.builder = builder;      
    }
    
    /**
     * Attempt to interrupt will try to destroy whole process
     */
    @Override
    public Integer call() throws IOException {
      Process process = builder.start();
      Integer exit = null;
      try{
        exit = process.waitFor();
      } catch (InterruptedException e){
        process.destroy();
      }
      return exit;
    }
  }
}
