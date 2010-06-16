package org.pentaho.di.job.entries.hadoopcopyfiles;

import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.job.entries.copyfiles.JobEntryCopyFiles;

@JobEntry (id="HadoopCopyFilesPlugin", name="Hadoop Copy Files", categoryDescription="Hadoop", description="Copy files to and from HDFS", image = "HDP.png")
public class JobEntryHadoopCopyFiles 
       extends JobEntryCopyFiles {

    
    public JobEntryHadoopCopyFiles(String name) {
        super(name);
    }
}
