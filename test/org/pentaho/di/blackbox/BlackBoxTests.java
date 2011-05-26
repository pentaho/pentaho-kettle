package org.pentaho.di.blackbox;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

@RunWith(Parameterized.class)
public class BlackBoxTests {

	private File transFile;
	private List<File> expectedFiles;
	
	private static ArrayList<Object> allTests;
	
	public BlackBoxTests(File transFile, List<File> expectedFiles) {
		this.transFile = transFile;
		this.expectedFiles = expectedFiles;
	}

	@BeforeClass
	public static void setupBlackbox() {
		
		// set the locale to English so that log file comparisons work
		GlobalMessages.setLocale(EnvUtil.createLocale("en-US")); //$NON-NLS-1$
		
		// Keep all log rows for at least 60 minutes as per BaseCluster.java
		CentralLogStore.init(0, 60);
	}	

	@Parameters
	public static Collection<Object[]> getTests() {
		
		allTests = new ArrayList<Object>();
		
		// Traverse the "testfiles" tree to generate the collection		
		// do not process the output folder, there won't be any tests there
		File dir = new File("testfiles/blackbox/tests");
		
		assertTrue( dir.exists() );
		assertTrue( dir.isDirectory() );
		processDirectory( dir );
		
		Object[][] d = new Object[allTests.size()][2];
		
		for (int i=0; i<allTests.size(); i++) {
			Object params[] = (Object[])allTests.get(i);
			
			d[i][0] = params[0];
			d[i][1] = params[1];
		}
			
		return Arrays.asList(d);		
	}
	
	protected static void processDirectory( File dir ) {
		File files[] = dir.listFiles();
		
		// recursively process every folder in testfiles/blackbox/tests
		for( int i=0; i<files.length; i++ ) 
		{
			if( files[i].isDirectory() ) 
			{
				processDirectory( files[i] );
			}
		}
		
		// now process any transformations or jobs we find
		for( int i=0; i<files.length; i++ ) 
		{
			if( files[i].isFile() ) 
			{
				String name = files[i].getName();
				if( name.endsWith(".ktr") && !name.endsWith("-tmp.ktr") ) 
				{
					// we found a transformation
					// see if we can find an output file
					List<File> expected = getExpectedOutputFile( dir, name.substring(0, name.length()-4) );
					
					Object params[] = { files[i], expected };
					allTests.add(params);
				}
				else if( name.endsWith(".kjb") ) 
				{
					// we found a job
					System.out.println("JOBS NOT YET HANDLED: " + name);
				}
			}
		}
		
	}

	/**
	 * Tries to find an output file to match a transformation or job file
	 * @param dir The directory to look in
	 * @param baseName Name of the transformation or the job without the extension
	 * @return
	 */		
	protected static List<File> getExpectedOutputFile( File dir, String baseName ) {
		List<File> files = new ArrayList<File>();
		
		File expected = new File( dir, baseName + ".fail.txt" );
		if (expected.exists()) {
			files.add(expected);
		}
		
		for (String extension : new String[] { ".txt", ".csv", ".xml" }) {
			expected = new File( dir, baseName + ".expected"+extension );
			if( expected.exists() ) 
			{
				files.add(expected);
			}
		
			// now see if there are perhaps multiple files generated...
			//
			boolean found=true;
			int nr=0;
			while (found) {
				expected = new File( dir, baseName + ".expected_"+nr+extension );
				if( expected.exists() ) 
				{
					files.add(expected);
					nr++;
				}
				else
				{
					found=false;
				}
			}
		}
		
		return files;
	}

	// This is a generic JUnit 4 test that takes no parameters
	@Test
	public void runTransOrJob() throws Exception {
		
		// Params are:
		//    File transFile
		//    List<File> expectedFiles

        LogChannelInterface log = new LogChannel("BlackBoxTest ["+transFile.toString()+"]");
        Result result = new Result();
        
		if( !transFile.exists() ) 
		{
			log.logError( "Transformation does not exist: "+ getPath( transFile ) );
			addFailure( "Transformation does not exist: "+ getPath( transFile ) );
			fail( "Transformation does not exist: "+ getPath(transFile) );
		}
		if( expectedFiles.isEmpty() ) 
		{
			addFailure("No expected output files found: "+ getPath( transFile ));
			fail( "No expected output files found: "+ getPath( transFile ) );
		}

		result = runTrans( transFile.getAbsolutePath(), log );
		
		// verify all the expected output files...
		//
        for (int i=0;i<expectedFiles.size();i++) {
            
        	File expected = expectedFiles.get(i);
        	
        	if (expected.getAbsoluteFile().toString().contains(".expected")) {
        	
        		// create a path to the expected output
        		String actualFile = expected.getAbsolutePath();
        		actualFile = actualFile.replaceFirst(".expected_"+i+".", ".actual_"+i+"."); // multiple files case
        		actualFile = actualFile.replaceFirst(".expected.", ".actual."); // single file case
        		File actual = new File( actualFile );
        		if( result.getResult() ) {
        			fileCompare( expected, actual, log );
        		}
        	}
        }
        
        // We didn't get a result, so the only expected file should be a ".fail.txt" file
		//
        if( !result.getResult() ) {
			String logStr = CentralLogStore.getAppender().getBuffer(result.getLogChannelId(), true).toString();
			
        	if (expectedFiles.size()==0) {
        		// We haven't got a ".fail.txt" file, so this is a real failure
        		fail("Error running " + getPath(transFile) + ":" + logStr);
        	}
        }	
	}

	public void writeLog( File logFile, String logStr ) 
	{
		try {
			// document encoding will be important here
			OutputStream stream = new FileOutputStream( logFile );
			
			// parse the log file and remove things that will make comparisons hard
			int length = logStr.length();
			int pos = 0;
			String line;
			while( pos < length ) {
				line = null;
				int eol = logStr.indexOf("\r\n", pos);
				if( eol != -1 ) {
					line = logStr.substring(pos, eol);
					pos = eol+2;
				} else {
					eol = logStr.indexOf("\n", pos);
					if( eol != -1 ) {
						line = logStr.substring(pos, eol);
						pos = eol+1;
					} else {
						// this must be the last line
						line = logStr.substring(pos);
						pos = length;
					}
				}
				if( line != null ) {
					// remove the date/time
					line = line.substring(22);
					// find the subject
					String subject = "";
					int idx = line.indexOf(" - ");
					if( idx != -1 ) {
						subject = line.substring(0, idx);
					}
					// skip the version and build numbers
					idx = line.indexOf(" : ", idx );
					if( idx != -1 ) {
						String details = line.substring(idx+3);
						// filter out stacktraces
						if( details.startsWith( "\tat " ) ) {
							continue;
						}
						if( details.startsWith( "\t... " ) ) {
							continue;
						}
						// force the windows EOL characters
						stream.write( (subject+" : "+details+"\r\n").getBytes("UTF-8") );
					}
				}
			}

			stream.close();
		} catch (Exception e)
		{
			addFailure("Could not write to log file: "+logFile.getAbsolutePath());
		}
	
	}

	public String getPath( File file ) {
		return getPath (file.getAbsolutePath() );
	}
	
	public String getPath( String filepath ) {
		int idx = filepath.indexOf( "/testfiles/" );
		if( idx == - 1) {
			idx = filepath.indexOf( "\\testfiles\\" );
		}
		if( idx != - 1) {
			return filepath.substring( idx+1 );
		}
		return filepath;
		
	}
	
	public void fileCompare( File expected, File actual, LogChannelInterface log ) throws IOException {
		
		InputStream expectedStream = new FileInputStream( expected );
		InputStream actualStream = new FileInputStream( actual );
		
        // compare the two files
        
        int goldPos = 0;
        int tmpPos = 0;
        byte goldBuffer[] = new byte[2048];
        byte tmpBuffer[] = new byte[2048];
        try {
        		// read the start of both files
            goldPos = expectedStream.read( goldBuffer );
            tmpPos = actualStream.read( tmpBuffer );
            // assume lock-step
//            if( goldPos != tmpPos ) 
//            {
//            	addFailure("Test file pointers are out of step : "+getPath( actual ));
//            	assertEquals( "Test file pointers are out of step : "+getPath( actual ), goldPos, tmpPos );
//            }
            int lineno = 1;
            int charno = 0;
        	int indexGold = 0;
        	int indexTmp = 0;
        	int totalGold = goldPos;
        	int totalTmp = tmpPos;
            while( goldPos > 0 && tmpPos > 0 ) {
            	if( indexGold == goldPos ) {
            		goldPos = expectedStream.read( goldBuffer );
            		if( goldPos > 0 ) {
            			totalGold += goldPos;
            		}
            		indexGold = 0;
            	}
            	if( indexTmp == tmpPos ) {
            		tmpPos = actualStream.read( tmpBuffer );
            		if( tmpPos > 0 ) {
            			totalTmp += tmpPos;
            		}
            		indexTmp = 0;
            	}
        		if( goldPos < 0 ) {
        			break;
        		}
        		if( tmpPos < 0 ) {
        			break;
        		}

        		charno++;
            			if( goldBuffer[indexGold] != tmpBuffer[indexTmp] )
            			{
            				int start = indexTmp > 10 ? indexTmp-10 : 0;
            				int end = indexTmp < tmpBuffer.length-11 ? indexTmp+10 : tmpBuffer.length-1;
            				int offset = indexTmp-start;
            				byte bytes[] = new byte[offset];
            				System.arraycopy(tmpBuffer, start, bytes, 0, bytes.length);
            				String frag = "-->"+new String(bytes);
            				frag += "[" + (char) tmpBuffer[indexTmp] + "]";
            				bytes = new byte[end-start-offset];
            				System.arraycopy(tmpBuffer, start+offset+1, bytes, 0, bytes.length);
            				frag += new String(bytes);
            				frag += "<--";
            				String exp = goldBuffer[indexGold] < 32 ? "\\"+ (char) (goldBuffer[indexGold]-'a') : ""+ (char) goldBuffer[indexGold]  ;
            				String act = tmpBuffer[indexTmp] < 32 ? "\\"+ (char) (tmpBuffer[indexTmp]-'a') : ""+ (char) tmpBuffer[indexTmp]  ;
            				String message = "Test files ("+getPath(actual)+") differ at: line " +lineno + " char " +charno + " expecting '"+ exp + "' but found '" + act + "' - "+frag;
            				addFailure(message);
            				log.logError("BlackBoxTest", message);
            				fail( message );
            			} 
            			else if( tmpBuffer[indexTmp] == '\n' )
            			{
            				lineno++;
            				charno=0;
            			}
            			indexGold++;
            			indexTmp++;
            		
            }
            if( totalGold != totalTmp ) {
            	String message = "Comparison files are not same length. " +
            			"Expected=" + expected.getPath() + " (" + totalGold + ") " + 
            			"Actual=" + actual.getPath() + " (" + totalTmp +")";
            	addFailure(message);
            	fail(message);
            }
        } catch (Exception e) {
        	addFailure("Error trying to compare output files: " + getPath(actual));
        	e.printStackTrace();
        	fail( "Error trying to compare output files: " + getPath(actual) );
        }
	}

	public Result runTrans(String fileName, LogChannelInterface log) throws KettleException
	{
		Result result = new Result();
		
    	// Bootstrap the Kettle API...
    	//
    	KettleEnvironment.init();
        
		Trans trans = null;
		TransMeta transMeta = new TransMeta();

		try
		{
			transMeta = new TransMeta(fileName);
			trans = new Trans(transMeta);
			trans.setLogLevel(LogLevel.ERROR);
			result = trans.getResult();
		}
		catch(Exception e)
		{
			result = trans.getResult();
			trans=null;
			transMeta=null;
			String message = "Processing has stopped because of an error: " + getPath(fileName); 
			addFailure(message);
			log.logError(message, e);
			fail(message);
			return result;
		}
		
		try
		{
			trans.initializeVariablesFrom(null);
			trans.getTransMeta().setInternalKettleVariables(trans);
			
			trans.setSafeModeEnabled(true);
			
			// see if the transformation checks ok
			List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
			trans.getTransMeta().checkSteps(remarks, false, null);
			for( CheckResultInterface remark : remarks ) {
				if( remark.getType() == CheckResultInterface.TYPE_RESULT_ERROR ) {
					// add this to the log
					addFailure("Check error: " + getPath(fileName) + ", "+remark.getErrorCode());
					log.logError("BlackBoxTest", "Check error: " + getPath(fileName) + ", "+remark.getErrorCode() );
				}
			}
			
		    // allocate & run the required sub-threads
			try {
				trans.execute(null); 
			} catch (Exception e) {
            	addFailure("Unable to prepare and initialize this transformation: " + getPath(fileName));
                log.logError("BlackBoxTest", "Unable to prepare and initialize this transformation: " + getPath(fileName));
                fail("Unable to prepare and initialize this transformation: " + getPath(fileName));
                return null;
			}

			trans.waitUntilFinished();
			result = trans.getResult();
			
			// The result flag is not set to true by a transformation - set it to true if got no errors
			// FIXME: Find out if there is a better way to check if a transformation has thrown an error
			result.setResult(result.getNrErrors() == 0);
			
			return result;
		}
		catch(Exception e)
		{
			addFailure("Unexpected error occurred: " + getPath(fileName));
            log.logError("BlackBoxTest", "Unexpected error occurred: " + getPath(fileName), e);
            result.setResult(false);
            result.setNrErrors(1);
            fail("Unexpected error occurred: " + getPath(fileName));
            return result;
		}
	}

	protected void addFailure( String message ) {
		System.err.println("failure: "+message);
	}
}
