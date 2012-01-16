/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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

