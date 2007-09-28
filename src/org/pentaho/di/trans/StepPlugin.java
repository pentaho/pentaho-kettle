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
package org.pentaho.di.trans;

import java.util.Hashtable;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.LanguageChoice;

/**
 * @author Matt
 *
 */
public class StepPlugin
{
    public static final int TYPE_ALL    = 0;
    public static final int TYPE_NATIVE = 1;
    public static final int TYPE_PLUGIN = 2;
    
    public static final String[] typeDesc = new String[] { Messages.getString("StepPlugin.Type.All.Desc"), Messages.getString("StepPlugin.Type.Native.Desc"), Messages.getString("StepPlugin.Type.Plugin.Desc"), }; 

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

    private Map<String, String> localizedCategories;
    private Map<String, String> localizedDescriptions;
    private Map<String, String> localizedTooltips;

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
        
        this.localizedCategories = new Hashtable<String, String>();
        this.localizedDescriptions = new Hashtable<String, String>();
        this.localizedTooltips = new Hashtable<String, String>();
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
    
    public String getJarfilesList()
    {
    	String list = "";
    	if (jarfiles!=null)
    	{
	    	for (int i=0;i<jarfiles.length;i++)
	    	{
	    		if (i>0) list+=Const.PATH_SEPARATOR;
	    		list+=jarfiles[i];
	    	}
    	}
        return list;
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

    public void setLocalizedCategories(Map<String, String> localizedCategories)
    {
        this.localizedCategories = localizedCategories;
        
    }

    /**
     * @return the localized categories map.
     */
    public Map<String, String> getLocalizedCategories()
    {
        return localizedCategories;
    }

    public void setLocalizedDescriptions(Map<String, String> localizedDescriptions)
    {
        this.localizedDescriptions = localizedDescriptions;
    }

    /**
     * @return the localized descriptions map.
     */
    public Map<String, String> getLocalizedDescriptions()
    {
        return localizedDescriptions;
    }

    /**
     * @return the localizedTooltips
     */
    public Map<String, String> getLocalizedTooltips()
    {
        return localizedTooltips;
    }

    /**
     * @param localizedTooltips the localizedTooltips to set
     */
    public void setLocalizedTooltips(Map<String, String> localizedTooltips)
    {
        this.localizedTooltips = localizedTooltips;
    }
    
    public String getTypeDesc()
    {
    	return typeDesc[type];
    }

    public static RowMetaInterface getPluginInformationRowMeta()
    {
    	RowMetaInterface row = new RowMeta();
    	
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.Type.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.ID.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.Description.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.ToolTip.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.Directory.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.JarFiles.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.IconFile.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.ClassName.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.Category.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.ErrorHelpFile.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.SeparateClassloader.Label"), ValueMetaInterface.TYPE_BOOLEAN));

        return row;
    }

    public Object[] getPluginInformation()
    {
    	Object[] row = new Object[getPluginInformationRowMeta().size()];
    	int rowIndex=0;
    	
    	row[rowIndex++] = getTypeDesc();
    	row[rowIndex++] = getID()[0];
    	row[rowIndex++] = getDescription();
    	row[rowIndex++] = getTooltip();
    	row[rowIndex++] = getDirectory();
    	row[rowIndex++] = getJarfilesList();
    	row[rowIndex++] = getIconFilename();
    	row[rowIndex++] = getClassname();
    	row[rowIndex++] = getCategory();
    	row[rowIndex++] = getErrorHelpFile();
    	row[rowIndex++] = isSeparateClassloaderNeeded();

        return row;
    }
}
