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

/*
 * Created on 6-okt-2004
 *
 */
package be.ibridge.kettle.trans;

/**
 * @author Matt
 *
 */
public class StepPlugin
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

    private String          category;

    private String          errorHelpFile;
    
    private boolean         separateClassloaderNeeded;

    public StepPlugin(int type, String id, String description, String tooltip, String directory, String jarfiles[], String icon_filename,
            String classname, String category, String errorHelpFile)
    {
        this.type = type;
        this.id = id;
        this.description = description;
        this.tooltip = tooltip;
        this.directory = directory;
        this.jarfiles = jarfiles;
        this.icon_filename = icon_filename;
        this.classname = classname;
        this.category = category;
        this.errorHelpFile = errorHelpFile;
        this.separateClassloaderNeeded = false;
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
     * @return The ID (code String) of the step or plugin. (TextFileInput, DatabaseLookup, ...)
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

    public String getCategory()
    {
        if (category == null) return "General";
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public int hashCode()
    {
        return id.hashCode();
    }

    public boolean equals(Object obj)
    {
        return getID().equals(((StepPlugin) obj).getID());
    }
    
    public void setErrorHelpFile(String errorHelpText)
    {
        this.errorHelpFile = errorHelpText;
    }

    public String getErrorHelpFile()
    {
        return errorHelpFile;
    }

    /**
     * @return Returns the separateClassloaderNeeded.
     */
    public boolean isSeparateClassloaderNeeded()
    {
        return separateClassloaderNeeded;
    }

    /**
     * @param separateClassloaderNeeded The separateClassloaderNeeded to set.
     */
    public void setSeparateClassloaderNeeded(boolean separateClassloaderNeeded)
    {
        this.separateClassloaderNeeded = separateClassloaderNeeded;
    }
}
