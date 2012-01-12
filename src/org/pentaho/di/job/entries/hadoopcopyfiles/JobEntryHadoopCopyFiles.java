/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.di.job.entries.hadoopcopyfiles;

import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.job.entries.copyfiles.JobEntryCopyFiles;

@JobEntry (id="HadoopCopyFilesPlugin", name="Hadoop Copy Files", categoryDescription="Hadoop", description="Copy files to and from HDFS", image = "HDM.png")
public class JobEntryHadoopCopyFiles 
       extends JobEntryCopyFiles {

	public JobEntryHadoopCopyFiles() {
		this(""); //$NON-NLS-1$
	}
    
    public JobEntryHadoopCopyFiles(String name) {
        super(name);
    }
}
