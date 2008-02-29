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
package org.pentaho.di.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.core.Const;


/**
 * Singleton class to allow us to see on which date & time the kettle3.jar was built.
 * 
 * @author Matt
 * @since 2006-aug-12
 */
public class BuildVersion
{
    /** name of the Kettle version file, updated in the ant script, contains date and time of build */
    public static final String BUILD_VERSION_FILE = "build_version.txt";

    public static final String SEPARATOR = "@";
    
    public static final String BUILD_DATE_FORMAT = "yyyy/MM/dd'T'HH:mm:ss";
    
    private static BuildVersion buildVersion;
    
    /**
     * @return the instance of the BuildVersion singleton
     */
    public static final BuildVersion getInstance()
    {
        if (buildVersion!=null) return buildVersion;
        
        buildVersion = new BuildVersion();
        
        return buildVersion;
    }
    
    private int version;
    private Date buildDate;
    private String hostname;
    
    private BuildVersion()
    {
        String filename = BUILD_VERSION_FILE;
        StringBuffer buffer = new StringBuffer(30);

        try
        {
            // The version file only contains a single lines of text
            InputStream inputStream = getClass().getResourceAsStream( "/"+filename ); // try to find it in the jars...
            if (inputStream==null) // not found
            {
                // System.out.println("Stream not found for filename [/"+filename+"], looking for it on the normal filesystem...");
                try
                {
                    inputStream = new FileInputStream(filename); // Retry from normal file system
                    // System.out.println("BuildVersion: filename ["+filename+"] found!");
                }
                catch(FileNotFoundException e)
                {
                    inputStream = new FileInputStream("./"+filename);
                    // System.out.println("BuildVersion: filename [./"+filename+"] found!");
                }
            }
            else
            {
                // System.out.println("BuildVersion: filename [/"+filename+"] found!");
            }
            
            // read the file into a String
            int c=inputStream.read();
            while ( c>0 && c!='\n' && c!='\r' )
            {
                if (c!=' ' && c!='\t') buffer.append((char)c);  // no spaces or tabs please ;-)
                c=inputStream.read();
            }
            
            // The 3 parts we expect are in here: 
            String parts[] = buffer.toString().split(SEPARATOR);
            
            if (parts.length!=3)
            {
                throw new RuntimeException("Could not find 3 parts in versioning line : ["+buffer+"]");
            }
            
            // Get the revision
            version = Integer.parseInt(parts[0]);

            // Get the build date
            SimpleDateFormat format = new SimpleDateFormat(BUILD_DATE_FORMAT);
            buildDate = format.parse(parts[1]);
            
        }
        catch(Exception e)
        {
            System.out.println("Unable to load revision number from file : ["+filename+"] : "+e.toString());
            System.out.println(Const.getStackTracker(e));
            
            version = 1;
            buildDate = new Date();
        }
    }

    /**
     * @return the buildDate
     */
    public Date getBuildDate()
    {
        return buildDate;
    }

    /**
     * @param buildDate the buildDate to set
     */
    public void setBuildDate(Date buildDate)
    {
        this.buildDate = buildDate;
    }

    /**
     * @return the revision
     */
    public int getVersion()
    {
        return version;
    }

    /**
     * @param revision the revision to set
     */
    public void setVersion(int revision)
    {
        this.version = revision;
    }
    
    public void save()
    {
        FileWriter fileWriter = null;
        String filename = BUILD_VERSION_FILE;
        File file = new File( filename );
        
        try
        {
            fileWriter = new FileWriter(file);
            
            // First write the revision
            fileWriter.write(Integer.toString(version)+" ");
            
            // Then the separator
            fileWriter.write(SEPARATOR);
            
            // Finally the build date
            SimpleDateFormat format = new SimpleDateFormat(BUILD_DATE_FORMAT);
            fileWriter.write(" "+format.format(buildDate)+" ");
            
            // Then the separator
            fileWriter.write(SEPARATOR);
            
            // Then the hostname
            fileWriter.write(" "+Const.getHostname());

            // Return
            fileWriter.write(Const.CR);
            
            System.out.println("Saved build version info to file ["+file.getAbsolutePath()+"]");
        }
        catch(Exception e)
        {
            throw new RuntimeException("Unable to save revision information to file ["+BUILD_VERSION_FILE+"]", e);
        }
        finally
        {
            try
            {
                if (fileWriter!=null)
                {
                    fileWriter.close();
                }
            }
            catch(Exception e)
            {
                throw new RuntimeException("Unable to close file ["+BUILD_VERSION_FILE+"] after writing", e);
            }
        }
    }

    /**
     * @return the hostname
     */
    public String getHostname()
    {
        return hostname;
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }
    

}
