 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package be.ibridge.kettle.job;

/**
 * Contains the description of a job-entry of a job-entry plugin, what jars to load, the icon, etc.
 *  
 * @since 2005-mai-09
 * @author Matt
 *
 */
public class JobPlugin
{
    public static final int TYPE_ALL    = 0;

    public static final int TYPE_NATIVE = 1;

    public static final int TYPE_PLUGIN = 2;

    private int             type;

    private String          id;

    private String          description;

    private String          tooltip;

    private String          directory;

    private String          jarfiles[];

    private String          icon_filename;

    private String          classname;


    public JobPlugin(int type, String id, String description, String tooltip, String directory, String jarfiles[], String icon_filename,
            String classname)
    {
        this.type = type;
        this.id = id;
        this.description = description;
        this.tooltip = tooltip;
        this.directory = directory;
        this.jarfiles = jarfiles;
        this.icon_filename = icon_filename;
        this.classname = classname;
    }

    public int getType()
    {
        return type;
    }

    public boolean isNative()
    {
        return type == TYPE_NATIVE;
    }

    public boolean isPlugin()
    {
        return type == TYPE_PLUGIN;
    }

    /**
     * @return The ID (code String) of the job or job-plugin.
     */
    public String getID()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public String getTooltip()
    {
        return tooltip;
    }

    public String getDirectory()
    {
        return directory;
    }

    public String[] getJarfiles()
    {
        return jarfiles;
    }

    public String getIconFilename()
    {
        return icon_filename;
    }

    public String getClassname()
    {
        return classname;
    }

    public int hashCode()
    {
        return id.hashCode();
    }

    public boolean equals(Object obj)
    {
        return getID().equals(((JobPlugin) obj).getID());
    }
}
