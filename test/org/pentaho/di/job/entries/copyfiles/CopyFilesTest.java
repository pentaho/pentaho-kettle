package org.pentaho.di.job.entries.copyfiles;

import org.pentaho.di.TestUtilities;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.job.Job;


import junit.framework.TestCase;

public class CopyFilesTest 
extends TestCase {
    
    /**
     * Creates a Result and logs that fact.
     * @return
     */
    private static Result createStartJobEntryResult() {
        
        Result startResult = new Result();
        startResult.setLogText(TestUtilities.now() +" - START - Starting job entry\r\n ") ;
        return startResult;
        
    }    
    
    /**
     * Tests copying a folder contents.  The folders used are created in 
     * the Java's temp location using unique folder and file names.
     * 
     * @throws Exception
     */
    public void testLocalFileCopy() throws Exception {
 
        String sourceFolder = TestUtilities.createTempFolder("testLocalFileCopy_source");
        String destinationFolder = TestUtilities.createTempFolder("testLocalFileCopy_destination");
        
        if (Const.isEmpty(sourceFolder) || Const.isEmpty(destinationFolder)) {
            fail("Could not create the source and/or destination folder(s).");
        }
        
        //  create a text file named testLocalFileCopy with a delimiter of ;
        TestUtilities.writeTextFile(sourceFolder, "testLocalFileCopy", ";");
        
        //  the parent job 
        Job parentJob = new Job();
        
        //  Set up the job entry to do wildcard copy
        JobEntryCopyFiles jobEntry = new JobEntryCopyFiles("Job entry copy files");
        jobEntry.source_filefolder = new String[]{sourceFolder};
        jobEntry.destination_filefolder = new String[]{destinationFolder};
        jobEntry.wildcard = new String[]{""};
        jobEntry.setParentJob(parentJob);
        
        //  Check the result for errors.
        Result result = jobEntry.execute(createStartJobEntryResult(), 1);
        if(result.getNrErrors()!=0) {
            fail(result.getLogText());
        }
    }
}

