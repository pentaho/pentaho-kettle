package be.ibridge.kettle.version;

import java.util.Date;

public class UpdateBuildVersion
{
    public static final String BASE_DIRECTORY = "src";
    
    public static void main(String[] args)
    {
        BuildVersion buildVersion = BuildVersion.getInstance(BASE_DIRECTORY);
        buildVersion.setRevision(buildVersion.getRevision()+1);
        buildVersion.setBuildDate(new Date());
        buildVersion.save(BASE_DIRECTORY);
    }
}
