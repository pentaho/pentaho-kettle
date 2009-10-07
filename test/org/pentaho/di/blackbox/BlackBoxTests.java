package org.pentaho.di.blackbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.Log4jBufferAppender;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

public class BlackBoxTests extends TestCase {

	protected int failures = 0;
	protected File currentFile = null;
	
	public void testBlackBox() {
		
		// set the locale to English so that log file comparisons work
		GlobalMessages.setLocale( new Locale("en-US") );
		
		// do not process the output folder, there won't be any tests there
		File dir = new File("testfiles/blackbox/tests");
		
		assertTrue( dir.exists() );
		assertTrue( dir.isDirectory() );
		processDirectory( dir );
		assertEquals( 0, failures );
	}
	
	protected void addFailure( String message ) {
		System.err.println("failure: "+message);
		failures++;
	}
	
	protected void processDirectory( File dir ) {
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
					try {
						runTrans( files[i], expected );
					} catch ( AssertionFailedError failure ) {
						// we're going to trap these so that we can continue with the other black box tests
						System.err.println( failure.getMessage() );
					}
				}
				else if( name.endsWith(".kjb") ) 
				{
					// we found a job
					System.out.println(name);
				}
			}
		}
		
	}
	
	protected void runTrans( File transFile, List<File> expectedFiles ) {

		System.out.println("Running: "+getPath(transFile));
		LogWriter log;
        log=LogWriter.getInstance( LogWriter.LOG_LEVEL_ERROR );
        Log4jBufferAppender bufferAppender = CentralLogStore.getAppender();
        log.addAppender(bufferAppender);
        
        LogChannelInterface logChannel = new LogChannel("BlackBoxTest ["+transFile.toString()+"]");

        int failsIn = failures;
        Result result = new Result();
        
		try {
			currentFile = transFile;
			if( !transFile.exists() ) 
			{
				logChannel.logError( "Transformation does not exist: "+ getPath( transFile ) );
				addFailure( "Transformation does not exist: "+ getPath( transFile ) );
			}
			if( expectedFiles.isEmpty() ) 
			{
				fail( "No expected output files found: "+ getPath( transFile ) );
				addFailure("No expected output files found: "+ getPath( transFile ));
			}

			try {
				result = runTrans( transFile.getAbsolutePath(), logChannel );
				
				// verify all the expected output files...
				//
		        for (int i=0;i<expectedFiles.size();i++) {
		            
		        	File expected = expectedFiles.get(i);
		        	
		    		// create a path to the expected output
		    		String actualFile = expected.getAbsolutePath();
		    		actualFile = actualFile.replaceFirst(".expected.", ".actual."); // single file case
		    		actualFile = actualFile.replaceFirst(".expected_"+i+".", ".actual_"+i+"."); // multiple files case

					File actual = new File( actualFile );
					if( !result.getResult() ) {
						fileCompare( expected, actual, logChannel );
					}
		        }			
			} catch (KettleException ke) {
				// this will get logged below
			} catch ( AssertionFailedError failure ) {
				// we're going to trap these so that we can continue with the other black box tests
			} catch (Throwable t) {
			}
		} catch ( AssertionFailedError failure ) {
			// we're going to trap these so that we can continue with the other black box tests
			System.err.println( failure.getMessage() );
			result.setResult(false);
		}
		log.removeAppender(bufferAppender);
		
		if( !result.getResult() && expectedFiles.size()==1) {
			
			File expected = expectedFiles.get(0);
			String logStr = CentralLogStore.getAppender().getBuffer(result.getLogChannelId(), true).toString();
			
			String tmpFileName = transFile.getAbsolutePath().substring(0, transFile.getAbsolutePath().length()-4)+"-log.txt";
			File logFile = new File( tmpFileName );
			writeLog( logFile, logStr );
			try {
			if( fileCompare( expected, logFile, logChannel ) ) {
				// we were expecting this to fail, reset any accumulated failures
				failures = failsIn;
			}
			} catch (IOException e) {
				addFailure("Could not compare log files: " + getPath( logFile ) + "" +e.getMessage());
				fail( "Could not compare log files: " + getPath( logFile ) + "" +e.getMessage() );
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
	
	public boolean fileCompare( File expected, File actual, LogChannelInterface log ) throws IOException {
		
		int failsIn = failures;
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
            	addFailure( "Comparison files are not same length" );
            }
        } catch (Exception e) {
        	addFailure("Error trying to compare output files: " + getPath(actual));
        	e.printStackTrace();
        	fail( "Error trying to compare output files: " + getPath(actual) );
        }
        return failsIn == failures;
		
	}

	/**
	 * Tries to find an output file to match a transformation or job file
	 * @param dir The directory to look in
	 * @param baseName Name of the transformation or the job without the extension
	 * @return
	 */
	protected List<File> getExpectedOutputFile( File dir, String baseName ) {
		List<File> files = new ArrayList<File>();
		
		for (String extension : new String[] { ".txt", ".csv", ".xml" }) {
			File expected;
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
			result = trans.getResult();
		}
		catch(Exception e)
		{
			result = trans.getResult();
			trans=null;
			transMeta=null;
			addFailure("Processing has stopped because of an error: " + getPath(fileName));
			log.logError("BlackBoxTest", "Processing has stopped because of an error: " + getPath(fileName), e);
			return result;
		}

		if (trans==null)
		{
			addFailure("Can't continue because the transformation couldn't be loaded." + getPath(fileName));
            log.logError("BlackBoxTest", "Can't continue because the transformation couldn't be loaded." + getPath(fileName));
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
			
			// TODO move this code to a separate test
/*
			// clone it and convert it back into XML and compare it with the one we started with
			// this tests that the clone and the conversion to and from XML are all consistent
			TransMeta clone = (TransMeta) trans.getTransMeta().clone();
			clone.setName( trans.getTransMeta().getName() );
			clone.setModifiedDate( trans.getTransMeta().getModifiedDate() );
			String xml = clone.getXML();
			
			String tmpFileName = fileName.substring(0, fileName.length()-4)+"-tmp.ktr";
			File tmpFile = new File( tmpFileName );
			try {
				// document encoding will be important here
				OutputStream stream = new FileOutputStream( tmpFile );
				stream.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes() );
				stream.write( xml.getBytes("UTF-8") );
				stream.close();
				// now compare the two transformation XML files
				fileCompare( new File(fileName), tmpFile, log );
				// if that succeeded we can remove the tmp file
				tmpFile.delete();
			} catch (Exception e)
			{
				addFailure("Could not write to tmp file: " + getPath(tmpFileName));
				log.logError("BlackBoxTest", "Could not write to tmp file: " + getPath(tmpFileName), e);
			}
*/
		    // allocate & run the required sub-threads
			try {
				trans.execute(null); 
			}
			catch (Exception e) {
            	addFailure("Unable to prepare and initialize this transformation: " + getPath(fileName));
                log.logError("BlackBoxTest", "Unable to prepare and initialize this transformation: " + getPath(fileName));
                return null;
            }
			trans.waitUntilFinished();
			trans.endProcessing(Database.LOG_STATUS_END);
			
			return trans.getResult();
		}
		catch(KettleException ke)
		{
			addFailure("Unexpected error occurred: " + getPath(fileName));
            log.logError("BlackBoxTest", "Unexpected error occurred: " + getPath(fileName), ke);
            result.setResult(false);
            result.setNrErrors(1);
            return result;
		}
	}

	public static void main( String args[] ) {
		try {
			BlackBoxTests test = new BlackBoxTests();
			test.setUp();
			test.testBlackBox();
			test.tearDown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
