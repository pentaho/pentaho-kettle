package be.ibridge.kettle.version;

import java.util.Date;

public class UpdateBuildVersion
{
    public static void main(String[] args)
    {
        BuildVersion buildVersion = BuildVersion.getInstance();
        buildVersion.setRevision(buildVersion.getRevision()+1);
        buildVersion.setBuildDate(new Date());
        buildVersion.save();
    }
}
