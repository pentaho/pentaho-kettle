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

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.Log4jStringAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

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
					File expected = getExpectedOutputFile( dir, name.substring(0, name.length()-4) );
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
	
	protected void runTrans( File transFile, File expected ) {

		System.out.println("Running: "+getPath(transFile));
		LogWriter log;
        log=LogWriter.getInstance( LogWriter.LOG_LEVEL_ERROR );
        Log4jStringAppender stringAppender = LogWriter.createStringAppender();
        log.addAppender(stringAppender);

        boolean ok = false;
        int failsIn = failures;
		// create a path to the expected output
		String actualFile = expected.getAbsolutePath();
		actualFile = actualFile.replaceFirst(".expected.", ".actual.");

		try {
			currentFile = transFile;
			if( !transFile.exists() ) 
			{
				log.logError( "BlackBoxTest", "Transformation does not exist: "+ getPath( transFile ) );
				addFailure( "Transformation does not exist: "+ getPath( transFile ) );
			}
			if( !expected.exists() ) 
			{
				fail( "Expected output file does not exist: "+ getPath( expected ) );
				addFailure("Expected output file does not exist: "+ getPath( expected ));
			}
			File actual = new File( actualFile );

			try {
				ok = runTrans( transFile.getAbsolutePath(), log );
				if( ok ) {
					fileCompare( expected, actual, log );
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
		}
		log.removeAppender(stringAppender);
		
		if( !ok ) {
			String logStr = stringAppender.getBuffer().toString();
			
			String tmpFileName = transFile.getAbsolutePath().substring(0, transFile.getAbsolutePath().length()-4)+"-log.txt";
			File logFile = new File( tmpFileName );
			writeLog( logFile, logStr );
			try {
			if( fileCompare( expected, logFile, log ) ) {
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
	
	public boolean fileCompare( File expected, File actual, LogWriter log ) throws IOException {
		
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
	protected File getExpectedOutputFile( File dir, String baseName ) {
		File expected;
		expected = new File( dir, baseName + ".expected.txt" );
		if( expected.exists() ) 
		{
			return expected;
		}
		expected = new File( dir, baseName + ".expected.csv" );
		if( expected.exists() ) 
		{
			return expected;
		}
		expected = new File( dir, baseName + ".expected.xml" );
		if( expected.exists() ) 
		{
			return expected;
		}
		return null;
		
	}
	
	public boolean runTrans(String fileName, LogWriter log) throws KettleException
	{
		EnvUtil.environmentInit();
		
		Trans trans = null;

		/* Load the plugins etc.*/
		try 
		{
			StepLoader.init();
		}
		catch(KettleException e)
		{
			addFailure("Error loading steps... halting!" + getPath(fileName));
			log.logError("BlackBoxTest", "Error loading steps... halting!" + getPath(fileName), e);
			return false;
		}
		
        /* Load the plugins etc.*/
		try 
		{
			JobEntryLoader.init();
		}
		catch(KettleException e)
        {
        	addFailure("Error loading job entries & plugins... halting!" + getPath(fileName));
            log.logError("BlackBoxTest", "Error loading job entries & plugins... halting!" + getPath(fileName), e);
            return false;
        }
        
		TransMeta transMeta = new TransMeta();

		try
		{
			transMeta = new TransMeta(fileName);
			trans = new Trans(transMeta);
		}
		catch(Exception e)
		{
			trans=null;
			transMeta=null;
			addFailure("Processing has stopped because of an error: " + getPath(fileName));
			log.logError("BlackBoxTest", "Processing has stopped because of an error: " + getPath(fileName), e);
			return false;
		}

		if (trans==null)
		{
			addFailure("Can't continue because the transformation couldn't be loaded." + getPath(fileName));
            log.logError("BlackBoxTest", "Can't continue because the transformation couldn't be loaded." + getPath(fileName));
            return false;
            
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
                return false;
            }
			trans.waitUntilFinished();
			trans.endProcessing("end");

			
			return true;
		}
		catch(KettleException ke)
		{
			addFailure("Unexpected error occurred: " + getPath(fileName));
            log.logError("BlackBoxTest", "Unexpected error occurred: " + getPath(fileName), ke);
		}

		return false;
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
