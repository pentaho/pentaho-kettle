/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @author Michael D'Amour
 */

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
