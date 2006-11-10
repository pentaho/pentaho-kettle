package be.ibridge.kettle.pkg;

import java.util.ArrayList;
import java.util.List;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.trans.TransMeta;

public class KettleDependencies
{
    public static final String[] generalLibs = new String[] 
        { 
            "log4j-1.2.8.jar",             // Logging
            "js.jar",                      // JavaScript / Rhino
            "pentaho-1.2.0.jar",           // JNDI database connections
            "simple-jndi-0.11.1.jar",      // JNDI database connections
            "edtftpj-1.5.3.jar",           // EnterpriseDT FTP for JobEntryFTP
            "activation.jar",              // JobEntryMail
            "mail.jar",                    // JobEntryMail
            "jsch-0.1.24.jar",             // JobEntrySFTP
        };

    private String[] libraryFiles;
    
    public KettleDependencies(TransMeta transMeta)
    {
        extractLibraries(transMeta);
        
    }

    private void extractLibraries(TransMeta transMeta)
    {
        List libraries = new ArrayList();

        // First the common libraries
        //
        libraries.add("lib/kettle.jar");
        for (int i=0;i<generalLibs.length;i++)
        {
            libraries.add("libext/"+generalLibs[i]);
        }

        // Determine the libs that the steps use.
        //
        for (int s=0;s<transMeta.nrSteps();s++)
        {
            // The step itself
            String stepLibs[] = transMeta.getStep(s).getStepMetaInterface().getUsedLibraries();
            if (stepLibs!=null)
            {
                for (int i=0;i<stepLibs.length;i++)
                {
                    libraries.add("libext/"+stepLibs[i]);
                }
            }

            // Used connections
            DatabaseMeta[] usedDatabaseConnections = transMeta.getStep(s).getStepMetaInterface().getUsedDatabaseConnections();
            for (int c=0;c<usedDatabaseConnections.length;c++)
            {
                String dbLibs[] = usedDatabaseConnections[c].getDatabaseInterface().getUsedLibraries();
                if (dbLibs!=null)
                {
                    for (int i=0;i<dbLibs.length;i++)
                    {
                        libraries.add("libext/"+dbLibs[i]);
                    }
                }
            }
        }
        
        libraryFiles = Const.getDistinctStrings( (String[]) libraries.toArray(new String[libraries.size()]) );
    }

    /**
     * @return the libraryFiles (library filenames, including the relative path to those (libext/, lib/)
     */
    public String[] getLibraryFiles()
    {
        return libraryFiles;
    }

    /**
     * @param libraryFiles the libraryFiles to set, including the relative path to those (libext/, lib/)
     */
    public void setLibraryFiles(String[] libraryFiles)
    {
        this.libraryFiles = libraryFiles;
    }
}
