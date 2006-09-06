package be.ibridge.kettle.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import be.ibridge.kettle.core.Const;

/**
 * Singleton class to allow us to see on which date & time the kettle.jar was built.
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
    
    private int revision;
    private Date buildDate;
    
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
                System.out.println("Stream not found for filename [/"+filename+"], looking for it on the normal filesystem...");
                inputStream = new FileInputStream(filename); // Retry from normal file system
            }
            
            // read the file into a String
            int c=inputStream.read();
            while ( c!=0 && c!='\n' && c!='\r' )
            {
                if (c!=' ' && c!='\t') buffer.append((char)c);  // no spaces or tabs please ;-)
                c=inputStream.read();
            }
            
            // The 2 parts we expect are in here: 
            String parts[] = buffer.toString().split(SEPARATOR);
            
            if (parts.length!=2)
            {
                throw new RuntimeException("Could not find 2 parts in versioning line : ["+buffer+"]");
            }
            
            // Get the revision
            revision = Integer.parseInt(parts[0]);

            // Get the build date
            SimpleDateFormat format = new SimpleDateFormat(BUILD_DATE_FORMAT);
            buildDate = format.parse(parts[1]);
            
        }
        catch(Exception e)
        {
            System.out.println("Unable to load revision number from file : ["+filename+"] : "+e.toString());
            System.out.println(Const.getStackTracker(e));
            
            revision = 1;
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
    public int getRevision()
    {
        return revision;
    }

    /**
     * @param revision the revision to set
     */
    public void setRevision(int revision)
    {
        this.revision = revision;
    }
    
    public void save()
    {
        FileWriter fileWriter = null;
        
        try
        {
            String filename = BUILD_VERSION_FILE;
            
            File file = new File( filename );
            fileWriter = new FileWriter(file);
            
            // First write the revision
            fileWriter.write(Integer.toString(revision)+" ");
            
            // Then the separator
            fileWriter.write(SEPARATOR);
            
            // Finally the build date
            SimpleDateFormat format = new SimpleDateFormat(BUILD_DATE_FORMAT);
            fileWriter.write(" "+format.format(buildDate));
            
            // Return
            fileWriter.write("\n\r");
            
            System.out.println("Saved build version info to file ["+filename+"]");
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
    

}
