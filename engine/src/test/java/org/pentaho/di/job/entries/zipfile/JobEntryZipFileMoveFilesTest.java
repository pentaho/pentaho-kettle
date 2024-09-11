/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.zipfile;

import org.apache.commons.vfs2.FileObject;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import java.io.File;
import java.io.IOException;

public class JobEntryZipFileMoveFilesTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    JobEntryZipFile jobEntryZipFile = new JobEntryZipFile();

    @Test
    public void testFoldersToMove() throws IOException {
        File sourceFileorFolderPath =  temporaryFolder.newFolder( "Source" );
        File destFolder = temporaryFolder.newFolder( "Dest" );
        FileObject sourceFileOrFolder = null;
        FileObject[] fileObjects = new FileObject[2];
        boolean result;
        jobEntryZipFile.setParentJobMeta( new JobMeta() );
        int i=0;
        //Try to duplicate the functionality of moving files
        //without zipping and with Junit TemporaryFolder feature
        try {
            IKettleVFS vfs = KettleVFS.getInstance( DefaultBowl.getInstance() );

            sourceFileOrFolder = vfs.getFileObject( sourceFileorFolderPath.toString() );
            //Creating source files/folders
            while ( true ) {
                File sourceFile = temporaryFolder.newFile( "/Source/"+"source" + i+".txt" );
                fileObjects[i] = vfs.getFileObject( sourceFile.toString() );
                i++;
                if ( i == 2 )
                    break;
            }
            for ( FileObject fileObject : fileObjects ) {
                result = jobEntryZipFile.moveFilesToDestinationFolder( sourceFileOrFolder, fileObject,
                        destFolder.toString(), sourceFileOrFolder.isFolder(), fileObject );
                Assert.assertTrue( result );
                fileObject.close();
            }
            sourceFileOrFolder.close();
            sourceFileorFolderPath.delete();
            destFolder.deleteOnExit();
        }
        catch ( KettleFileException e ) {
            e.printStackTrace();
        }
    }
}