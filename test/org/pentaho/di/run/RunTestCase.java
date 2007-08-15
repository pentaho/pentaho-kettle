package org.pentaho.di.run;

import java.util.List;

import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;

import junit.framework.TestCase;

public class RunTestCase extends TestCase {

	// this value scales the size of (most of) the performance tests
	// set this to 100000 for the baseline tests
	// set it to a small value (e.g. 100) to verify that the performance tests are successful
	
	protected final int rowCount = 100;
	
	@SuppressWarnings("unchecked")
	protected void deleteOldResultFiles(be.ibridge.kettle.core.Result oldResult, String[] extentions) throws FileSystemException {
		// Delete the files that where created...
        List<be.ibridge.kettle.core.ResultFile> oldResultFilesList = oldResult.getResultFilesList();
        for (be.ibridge.kettle.core.ResultFile resultFile : oldResultFilesList) {

        	for (int i=0;i<extentions.length;i++) {
        		if (resultFile.getFile().getName().toString().endsWith(extentions[i])) {
        			resultFile.getFile().delete();
        			break;
        		}
        	}
        
        }
	}

	protected void deleteNewResultFiles(Result newResult, String[] extentions) throws FileSystemException {
		// Delete the files that where created...
        List<ResultFile> newResultFilesList = newResult.getResultFilesList();
        for (ResultFile resultFile : newResultFilesList) {
        	
        	for (int i=0;i<extentions.length;i++) {
        		if (resultFile.getFile().getName().toString().endsWith(extentions[i])) {
        			resultFile.getFile().delete();
        			break;
        		}
        	}
        	
        }
        
	}

}
