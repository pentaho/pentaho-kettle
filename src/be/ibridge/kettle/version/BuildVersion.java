package be.ibridge.kettle.version;

import java.io.File;
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
    private static BuildVersion buildVersion;
    
    public static final BuildVersion getInstance()
    {
        if (buildVersion!=null) return buildVersion;
        
        buildVersion = new BuildVersion();
        
        return buildVersion;
    }
    
    private String version;
    
    private BuildVersion()
    {
        try
        {
            // The version file only contains a single line of text
            // InputStream inputStream = getClass().getResourceAsStream();
            File file = new File( getClass().getResource(Const.BUILD_VERSION_FILE).getFile() );
            
            Date lastChanged = new Date(file.lastModified());
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            version = format.format(lastChanged);
        }
        catch(Exception e)
        {
            version = "unknown"; 
        }
    }
    
    public String getVersion()
    {
        return version;
    }
}
