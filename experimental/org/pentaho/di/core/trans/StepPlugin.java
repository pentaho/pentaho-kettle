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
package org.pentaho.di.core.trans;

import java.util.Hashtable;
import java.util.Map;

import be.ibridge.kettle.i18n.LanguageChoice;

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

    private String          id[];

    private String          description;

    private String          tooltip;

    private String          directory;

    private String          jarfiles[];

    private String          icon_filename;

    private String          classname;

    private String          category;

    private String          errorHelpFile;
    
    private boolean         separateClassloaderNeeded;

    private Map             localizedCategories;
    private Map             localizedDescriptions;
    private Map             localizedTooltips;

    public StepPlugin(int type, String id[], String description, String tooltip, String directory, String jarfiles[], String icon_filename,
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
        
        this.localizedCategories = new Hashtable();
        this.localizedDescriptions = new Hashtable();
        this.localizedTooltips = new Hashtable();
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
    public String[] getID()
    {
        return id;
    }

    public String getDescription()
    {
        return getDescription(LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase());
    }
    
    public String getDescription(String locale)
    {
        String localizedDescription = (String) localizedDescriptions.get(locale.toLowerCase());
        if (localizedDescription!=null) 
        {
            return localizedDescription;
        }
        return description;
    }

    public String getTooltip()
    {
        return getTooltip(LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase());
    }
    
    public String getTooltip(String locale)
    {
        String localizedTooltip = (String) localizedTooltips.get(locale.toLowerCase());
        if (localizedTooltip!=null)
        {
            return localizedTooltip;
        }
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
        return getCategory(LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase());
    }
    
    public String getCategory(String locale)
    {
        String localizedCategory = (String) localizedCategories.get(locale.toLowerCase());
        if (localizedCategory!=null)
        {
            return localizedCategory;
        }
        if (category == null) return Messages.getString("StepPlugin.Label"); //$NON-NLS-1$
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
        return handles( ((StepPlugin)obj).getID() );
    }
    
    public boolean handles(String pluginID)
    {
        for (int i=0;i<id.length;i++)
        {
            if (id[i].equals(pluginID)) return true;
        }
        return false;
    }
    
    public boolean handles(String pluginID[])
    {
        for (int i=0;i<id.length;i++)
        {
            for (int j=0;j<pluginID.length;j++)
            {
                if (id[i].equals(pluginID[j])) return true;
            }
        }
        return false;
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

    /**
     * @param jarfiles the jarfiles to set
     */
    public void setJarfiles(String[] jarfiles)
    {
        this.jarfiles = jarfiles;
    }

    public void setLocalizedCategories(Map localizedCategories)
    {
        this.            localizedCategories = localizedCategories;
        
    }

    /**
     * @return the localized categories map.
     */
    public Map getLocalizedCategories()
    {
        return localizedCategories;
    }

    public void setLocalizedDescriptions(Map localizedDescriptions)
    {
        this.localizedDescriptions = localizedDescriptions;
    }

    /**
     * @return the localized descriptions map.
     */
    public Map getLocalizedDescriptions()
    {
        return localizedDescriptions;
    }

    /**
     * @return the localizedTooltips
     */
    public Map getLocalizedTooltips()
    {
        return localizedTooltips;
    }

    /**
     * @param localizedTooltips the localizedTooltips to set
     */
    public void setLocalizedTooltips(Map localizedTooltips)
    {
        this.localizedTooltips = localizedTooltips;
    }
    
}
