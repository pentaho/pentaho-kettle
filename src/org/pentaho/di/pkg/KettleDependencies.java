/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.pkg;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.TransMeta;

public class KettleDependencies
{
    public static final String[] generalLibs = new String[] 
        { 
            "log4j-1.2.8.jar",             // Logging
            "js.jar",                      // JavaScript / Rhino
            "pentaho-1.2.0.jar",           // JNDI database connections
            "simple-jndi-0.11.1.jar",      // JNDI database connections
            "commons-logging.jar",         // Apache commons: Logging
            "commons-pool-1.3.jar",        // Apache commons: Object pooling
            "commons-dbcp-1.2.1.jar",      // Apache commons: Database Connection Pooling
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
        List<String> libraries = new ArrayList<String>();

        // First the common libraries
        //
        libraries.add("lib/kettle-engine-3.0.jar");
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
