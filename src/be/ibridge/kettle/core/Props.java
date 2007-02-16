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

 


package be.ibridge.kettle.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import be.ibridge.kettle.core.util.SortedFileOutputStream;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.spoon.Messages;


/**
 * We use Props to store all kinds of user interactive information such as the selected colors, fonts, positions of windows, etc.
 * 
 * @author Matt
 * @since 15-12-2003
 *
 */
public class Props implements Cloneable
{
	private static Props props;
	
	public static final String STRING_FONT_FIXED_NAME  = "FontFixedName";
	public static final String STRING_FONT_FIXED_SIZE  = "FontFixedSize";
	public static final String STRING_FONT_FIXED_STYLE = "FontFixedStyle";

	public static final String STRING_FONT_DEFAULT_NAME  = "FontDefaultName";
	public static final String STRING_FONT_DEFAULT_SIZE  = "FontDefaultSize";
	public static final String STRING_FONT_DEFAULT_STYLE = "FontDefaultStyle";

	public static final String STRING_FONT_GRAPH_NAME  = "FontGraphName";
	public static final String STRING_FONT_GRAPH_SIZE  = "FontGraphSize";
	public static final String STRING_FONT_GRAPH_STYLE = "FontGraphStyle";

	public static final String STRING_FONT_GRID_NAME  = "FontGridName";
	public static final String STRING_FONT_GRID_SIZE  = "FontGridSize";
	public static final String STRING_FONT_GRID_STYLE = "FontGridStyle";

	public static final String STRING_FONT_NOTE_NAME  = "FontNoteName";
	public static final String STRING_FONT_NOTE_SIZE  = "FontNoteSize";
	public static final String STRING_FONT_NOTE_STYLE = "FontNoteStyle";

	public static final String STRING_BACKGROUND_COLOR_R = "BackgroundColorR";
	public static final String STRING_BACKGROUND_COLOR_G = "BackgroundColorG";
	public static final String STRING_BACKGROUND_COLOR_B = "BackgroundColorB";

	public static final String STRING_GRAPH_COLOR_R = "GraphColorR";
	public static final String STRING_GRAPH_COLOR_G = "GraphColorG";
	public static final String STRING_GRAPH_COLOR_B = "GraphColorB";

	public static final String STRING_TAB_COLOR_R = "TabColorR";
	public static final String STRING_TAB_COLOR_G = "TabColorG";
	public static final String STRING_TAB_COLOR_B = "TabColorB";

	public static final String STRING_ICON_SIZE   = "IconSize";
	public static final String STRING_LINE_WIDTH  = "LineWidth";
	public static final String STRING_SHADOW_SIZE = "ShadowSize";
	public static final String STRING_LOG_LEVEL   = "LogLevel";	
	public static final String STRING_LOG_FILTER  = "LogFilter";
	public static final String STRING_MIDDLE_PCT  = "MiddlePct";
	
	public static final String STRING_LAST_PREVIEW_TRANS = "LastPreviewTrans";
	public static final String STRING_LAST_PREVIEW_STEP  = "LastPreviewStep";
	public static final String STRING_LAST_PREVIEW_SIZE  = "LastPreviewSize";
		
	public static final String STRING_MAX_UNDO  = "MaxUndo";

	public static final String STRING_SIZE_MAX = "SizeMax";
	public static final String STRING_SIZE_X   = "SizeX";
	public static final String STRING_SIZE_Y   = "SizeY";
	public static final String STRING_SIZE_W   = "SizeW";
	public static final String STRING_SIZE_H   = "SizeH";

	public static final String STRING_SASH_W1  = "SashWeight1";
	public static final String STRING_SASH_W2  = "SashWeight2";

	public static final String STRING_SHOW_TIPS               = "ShowTips";
	public static final String STRING_TIP_NR                  = "TipNr";
	public static final String STRING_AUTO_SAVE               = "AutoSave";
	public static final String STRING_SAVE_CONF               = "SaveConfirmation";
	public static final String STRING_AUTO_SPLIT              = "AutoSplit";

	public static final String STRING_USE_DB_CACHE            = "UseDBCache";
	public static final String STRING_OPEN_LAST_FILE          = "OpenLastFile";
	
	public static final String STRING_LAST_REPOSITORY_LOGIN   = "RepositoryLastLogin";
	public static final String STRING_LAST_REPOSITORY         = "RepositoryLast";
	
	public static final String STRING_ONLY_ACTIVE_STEPS       = "OnlyActiveSteps";
    public static final String STRING_START_SHOW_REPOSITORIES = "ShowRepositoriesAtStartup";
    public static final String STRING_ANTI_ALIASING           = "EnableAntiAliasing";
    public static final String STRING_SHOW_EXIT_WARNING       = "ShowExitWarning";
    public static final String STRING_SHOW_OS_LOOK            = "ShowOSLook";
    public static final String STRING_LAST_ARGUMENT           = "LastArgument";

    public static final String STRING_CUSTOM_PARAMETER        = "CustomParameter";
    
    public static final String STRING_PLUGIN_HISTORY          = "PluginHistory";

    public static final String STRING_DEFAULT_PREVIEW_SIZE    = "DefaultPreviewSize";
    public static final String STRING_ONLY_USED_DB_TO_XML     = "SaveOnlyUsedConnectionsToXML";
		
    public static final String STRING_ASK_ABOUT_REPLACING_DATABASES = "AskAboutReplacingDatabases";
    public static final String STRING_REPLACE_DATABASES             = "ReplaceDatabases";

    private static final String STRING_SHOW_COPY_OR_DISTRIBUTE_WARNING = "ShowCopyOrDistributeWarning";
    
    private static final String STRING_SHOW_WELCOME_PAGE_ON_STARTUP = "ShowWelcomePageOnStartup";

    private static final String STRING_MAX_NR_LINES_IN_LOG = "MaxNrOfLinesInLog";

    private LogWriter log = LogWriter.getInstance();
	private Properties properties;
	
    List lastUsedFiles;
    
    private ArrayList pluginHistory;
	
	private Display display;
		
	private int type;
    private String filename;
	
	private Hashtable screens;
	
	public static final int TYPE_PROPERTIES_EMPTY   = 0;
	public static final int TYPE_PROPERTIES_SPOON   = 1;
	public static final int TYPE_PROPERTIES_PAN     = 2;
	public static final int TYPE_PROPERTIES_CHEF    = 3;
	public static final int TYPE_PROPERTIES_KITCHEN = 4;
	public static final int TYPE_PROPERTIES_MENU    = 5;
	public static final int TYPE_PROPERTIES_PLATE   = 6;
    
    public static final int WIDGET_STYLE_DEFAULT = 0;
    public static final int WIDGET_STYLE_FIXED   = 1;
    public static final int WIDGET_STYLE_TABLE   = 2;
    public static final int WIDGET_STYLE_NOTEPAD = 3;
    public static final int WIDGET_STYLE_GRAPH   = 4;
    public static final int WIDGET_STYLE_TAB     = 5;




 


	/**
	 * Initialize the properties: load from disk.
	 * @param display The Display
	 * @param t The type of properties file.
	 */
	public static final void init(Display display, int t)
	{
		if (props==null)
		{
			props = new Props(display, t);
            
            // Also init the colors and fonts to use...
            GUIResource.getInstance();
		}
		else
		{
			throw new RuntimeException("The Properties systems settings are already initialised!");
		}
	}
    
    /**
     * Initialize the properties: load from disk.
     * @param display The Display
     * @param filename the filename to use 
     */
    public static final void init(Display display, String filename)
    {
        if (props==null)
        {
            props = new Props(display, filename);
            
            // Also init the colors and fonts to use...
            GUIResource.getInstance();
        }
        else
        {
            throw new RuntimeException("The properties systems settings are already initialised!");
        }
    }
	
	/**
	 * Check to see whether the Kettle properties where loaded.
	 * @return true if the Kettle properties where loaded.
	 */
	public static final boolean isInitialized()
	{
		return props!=null;
	}
	
	public static final Props getInstance()
	{
		if (props!=null) return props;
		
		throw new RuntimeException("Properties, Kettle systems settings, not initialised!");
	}
	
	private Props(Display dis, int t)
	{
		display=dis;
		properties = new Properties();
		setDefault();
		type=t;
        filename=getFilename();
        
        pluginHistory = new ArrayList();

        loadProps();
        addDefaultEntries();
        
        loadLastUsedFiles();
        loadScreens();
        loadPluginHistory();
	}

    private Props(Display dis, String filename)
    {
        display=dis;
        properties = new Properties();
        setDefault();
        this.type=TYPE_PROPERTIES_EMPTY;
        this.filename=filename;
        
        pluginHistory = new ArrayList();

        loadProps();
        
        loadLastUsedFiles();
        loadScreens();
        loadPluginHistory();
    }
    
    public String toString()
    {
        return "User preferences";
    }

    public String getFilename()
	{
		String directory = Const.getKettleDirectory();
		String filename = "";
		
		// Try to create the directory...
		File dir = new File(directory);
		try { dir.mkdirs(); } catch(Exception e) { }
		
		switch(type)
		{
			case TYPE_PROPERTIES_SPOON:
			case TYPE_PROPERTIES_PAN:
				filename=directory+Const.FILE_SEPARATOR+".spoonrc";
				break;
			case TYPE_PROPERTIES_CHEF:
			case TYPE_PROPERTIES_KITCHEN:
				filename=directory+Const.FILE_SEPARATOR+".chefrc";
				break;
			case TYPE_PROPERTIES_MENU:
				filename=directory+Const.FILE_SEPARATOR+".menurc";
				break;
			case TYPE_PROPERTIES_PLATE:
				filename=directory+Const.FILE_SEPARATOR+".platerc";
				break;
			default: break;
		}

		return filename;
	}
	
	public String getLicenseFilename()
	{
		String directory = Const.getKettleDirectory();
		String filename = directory+Const.FILE_SEPARATOR+".licence";
		
		// Try to create the directory...
		File dir = new File(directory);
        if (!dir.exists())
        {
            try { dir.mkdirs(); } 
            catch(Exception e) { }
        }
		
		return filename;
	}

	public boolean fileExists()
	{
	    File f = new File(filename);
		return f.exists();
	}
	
	public void setType(int t)
	{
		type=t;
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setDefault()
	{
		FontData fd;
		RGB col;

        lastUsedFiles = new ArrayList();
		screens = new Hashtable();

		properties.setProperty(STRING_LOG_LEVEL,  getLogLevel());
		properties.setProperty(STRING_LOG_FILTER, getLogFilter());
		
		if (display!=null)
		{
			fd=getFixedFont();
			properties.setProperty(STRING_FONT_FIXED_NAME,     fd.getName()   );
			properties.setProperty(STRING_FONT_FIXED_SIZE,  ""+fd.getHeight() );
			properties.setProperty(STRING_FONT_FIXED_STYLE, ""+fd.getStyle()  );
	
			fd=getDefaultFont();
            properties.setProperty(STRING_FONT_DEFAULT_NAME,     fd.getName());
			properties.setProperty(STRING_FONT_DEFAULT_SIZE,  ""+fd.getHeight() );
			properties.setProperty(STRING_FONT_DEFAULT_STYLE, ""+fd.getStyle() );
	
			fd=getDefaultFont();
			properties.setProperty(STRING_FONT_GRAPH_NAME,     fd.getName()   );
			properties.setProperty(STRING_FONT_GRAPH_SIZE,  ""+fd.getHeight() );
			properties.setProperty(STRING_FONT_GRAPH_STYLE, ""+fd.getStyle()  );
	
			fd=getDefaultFont();
			properties.setProperty(STRING_FONT_GRID_NAME,     fd.getName()   );
			properties.setProperty(STRING_FONT_GRID_SIZE,  ""+fd.getHeight() );
			properties.setProperty(STRING_FONT_GRID_STYLE, ""+fd.getStyle()  );
	
			fd=getDefaultFont();
			properties.setProperty(STRING_FONT_NOTE_NAME,     fd.getName()   );
			properties.setProperty(STRING_FONT_NOTE_SIZE,  ""+fd.getHeight() );
			properties.setProperty(STRING_FONT_NOTE_STYLE, ""+fd.getStyle()  );
	
			col=getBackgroundRGB();
			properties.setProperty(STRING_BACKGROUND_COLOR_R, ""+col.red  );
			properties.setProperty(STRING_BACKGROUND_COLOR_G, ""+col.green);
			properties.setProperty(STRING_BACKGROUND_COLOR_B, ""+col.blue );
	
			col=getGraphColorRGB();
			properties.setProperty(STRING_GRAPH_COLOR_R, ""+col.red   );
			properties.setProperty(STRING_GRAPH_COLOR_G, ""+col.green );
			properties.setProperty(STRING_GRAPH_COLOR_B, ""+col.blue  );
			
			properties.setProperty(STRING_ICON_SIZE,     ""+getIconSize());
			properties.setProperty(STRING_LINE_WIDTH,    ""+getLineWidth());
			properties.setProperty(STRING_SHADOW_SIZE,   ""+getShadowSize());
			properties.setProperty(STRING_MAX_UNDO,      ""+getMaxUndo());
			
			setSashWeights(getSashWeights());
		}
	}
	
	public boolean loadProps()
	{
		try
		{
			properties.load(new FileInputStream(filename));
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}

    private void addDefaultEntries()
    {
        if (!properties.containsKey("JobDialogStyle"))
            properties.setProperty("JobDialogStyle", "RESIZE,MAX,MIN");
    }

	public void storeScreens()
	{
		// Add screens hastable to properties..
		Enumeration keys = screens.keys();
		int nr=1;
		while (keys.hasMoreElements())
		{
			String name = (String)keys.nextElement();
			properties.setProperty("ScreenName"+nr, name);
			
			WindowProperty winprop = (WindowProperty)screens.get(name);
			properties.setProperty(STRING_SIZE_MAX+nr, winprop.isMaximized()?"Y":"N");
			if (winprop.getRectangle()!=null)
			{
				properties.setProperty(STRING_SIZE_X+nr, ""+winprop.getX());
				properties.setProperty(STRING_SIZE_Y+nr, ""+winprop.getY());
				properties.setProperty(STRING_SIZE_W+nr, ""+winprop.getWidth());
				properties.setProperty(STRING_SIZE_H+nr, ""+winprop.getHeight());
			}
			
			nr++;
		}
	}
	
	public void loadScreens()
	{
		screens = new Hashtable();
		
		int nr = 1;
		
		String name = properties.getProperty("ScreenName"+nr);
		while (name!=null)
		{
			boolean max = "Y".equalsIgnoreCase(properties.getProperty(STRING_SIZE_MAX+nr));
			int x = Const.toInt(properties.getProperty(STRING_SIZE_X+nr),   0);
			int y = Const.toInt(properties.getProperty(STRING_SIZE_Y+nr),   0);
			int w = Const.toInt(properties.getProperty(STRING_SIZE_W+nr), 320);
			int h = Const.toInt(properties.getProperty(STRING_SIZE_H+nr), 200);
			
			WindowProperty winprop = new WindowProperty(name, max, x, y, w, h);
			screens.put(name, winprop);
			
			nr++;
			name = properties.getProperty("ScreenName"+nr);
		}
	}

	public void saveProps()
	{
		storeScreens();
        setLastFiles();
		
        File spoonRc = new File(filename);
		try
		{
            // FileOutputStream fos = new FileOutputStream(spoonRc);

            SortedFileOutputStream fos = new SortedFileOutputStream(spoonRc);
            fos.setLogger(log);
			properties.store(fos, "Kettle Properties file");
            fos.close();
            log.logDetailed(toString(), Messages.getString("Spoon.Log.SaveProperties"));
		}
		catch(IOException e)
		{
            // If saving fails this could be a known Java bug: If running Spoon on windows the spoon
            // config file gets created with the 'hidden' attribute set. Some Java JREs cannot open
            // FileOutputStreams on files with that attribute set. The user has to unset that attribute
            // manually.
            if (spoonRc.isHidden() && filename.indexOf('\\') != -1)
            {
                // If filename contains a backslash we consider Spoon as running on Windows
                log.logError(toString(), Messages.getString("Spoon.Log.SavePropertiesFailedWindowsBugAttr", filename));
            }
            else
            {
                // Another reason why the save failed
                log.logError(toString(), Messages.getString("Spoon.Log.SavePropertiesFailed") + e.getMessage());
            }
		}
	}
    
	public void setLastFiles()
	{
		properties.setProperty("lastfiles", ""+lastUsedFiles.size());
		for (int i=0;i<lastUsedFiles.size();i++)
		{
            LastUsedFile lastUsedFile = (LastUsedFile) lastUsedFiles.get(i);
            
            properties.setProperty("filetype"+(i+1), Const.NVL(lastUsedFile.getFileType(), LastUsedFile.FILE_TYPE_TRANSFORMATION));
			properties.setProperty("lastfile"+(i+1), Const.NVL(lastUsedFile.getFilename(), ""));
			properties.setProperty("lastdir"+(i+1),  Const.NVL(lastUsedFile.getDirectory(), ""));
			properties.setProperty("lasttype"+(i+1), lastUsedFile.isSourceRepository()?"Y":"N");
			properties.setProperty("lastrepo"+(i+1), Const.NVL(lastUsedFile.getRepositoryName(), ""));
		}
	}
	
	/**
	 * Add a last opened file to the top of the recently used list.
     * @param fileType the type of file to use @see LastUsedFile
	 * @param filename The name of the file or transformation
	 * @param directory The repository directory path, null in case lf is an XML file  
	 * @param sourceRepository True if the file was loaded from repository, false if ld is an XML file.
	 * @param repositoryName The name of the repository the file was loaded from or save to.
	 */
	public void addLastFile(String fileType, String filename, String directory, boolean sourceRepository, String repositoryName)
	{
		LastUsedFile lastUsedFile = new LastUsedFile(fileType, filename, directory, sourceRepository, repositoryName);
        
        int idx = lastUsedFiles.indexOf(lastUsedFile);
        if (idx>=0)
        {
            lastUsedFiles.remove(idx);
        }
        // Add it to position 0
        lastUsedFiles.add(0, lastUsedFile);
        
        // If we have more than Const.MAX_FILE_HIST, top it off
        while (lastUsedFiles.size()>Const.MAX_FILE_HIST)
        {
            lastUsedFiles.remove(lastUsedFiles.size()-1);
        }
	}
	
    public void loadLastUsedFiles()
    {
        lastUsedFiles = new ArrayList();
        int nr = Const.toInt(properties.getProperty("lastfiles"), 0);
        for (int i=0;i<nr;i++)
        {
            String fileType = properties.getProperty("filetype"+(i+1), LastUsedFile.FILE_TYPE_TRANSFORMATION); // default: transformation
            String filename = properties.getProperty("lastfile"+(i+1), "");
            String directory = properties.getProperty("lastdir"+(i+1), "");
            boolean sourceRepository = "Y".equalsIgnoreCase(properties.getProperty("lasttype"+(i+1), "N"));
            String repositoryName = properties.getProperty("lastrepo"+(i+1));
            
            lastUsedFiles.add(new LastUsedFile(fileType, filename, directory, sourceRepository, repositoryName));
        }
    }
    
    public List getLastUsedFiles()
    {
        return lastUsedFiles;
    }
    
    public void setLastUsedFiles(List lastUsedFiles)
    {
        this.lastUsedFiles = lastUsedFiles;
    }
    
    public String[] getLastFileTypes()
    {
        String retval[] = new String[lastUsedFiles.size()];
        for (int i=0;i<retval.length;i++)
        {
            LastUsedFile lastUsedFile = (LastUsedFile) lastUsedFiles.get(i);
            retval[i]=lastUsedFile.getFileType();
        }
        return retval;
    }
    
	public String[] getLastFiles()
	{
        String retval[] = new String[lastUsedFiles.size()];
        for (int i=0;i<retval.length;i++)
        {
            LastUsedFile lastUsedFile = (LastUsedFile) lastUsedFiles.get(i);
            retval[i]=lastUsedFile.getFilename();
        }
		return retval;
	}
	
	public String[] getLastDirs()
	{
        String retval[] = new String[lastUsedFiles.size()];
        for (int i=0;i<retval.length;i++)
        {
            LastUsedFile lastUsedFile = (LastUsedFile) lastUsedFiles.get(i);
            retval[i]=lastUsedFile.getDirectory();
        }
        return retval;
	}


	public boolean[] getLastTypes()
	{
        boolean retval[] = new boolean[lastUsedFiles.size()];
        for (int i=0;i<retval.length;i++)
        {
            LastUsedFile lastUsedFile = (LastUsedFile) lastUsedFiles.get(i);
            retval[i]=lastUsedFile.isSourceRepository();
        }
        return retval;
	}

	public String[] getLastRepositories()
	{
        String retval[] = new String[lastUsedFiles.size()];
        for (int i=0;i<retval.length;i++)
        {
            LastUsedFile lastUsedFile = (LastUsedFile) lastUsedFiles.get(i);
            retval[i]=lastUsedFile.getRepositoryName();
        }
        return retval;
	}
	
	public void setLogLevel(String level)
	{
		properties.setProperty(STRING_LOG_LEVEL, level);
	}

	public String getLogLevel()
	{
		String level = properties.getProperty(STRING_LOG_LEVEL, "Basic");
		return level;
	}

	public void setLogFilter(String filter)
	{
		properties.setProperty(STRING_LOG_FILTER, Const.NVL(filter, ""));
	}

	public String getLogFilter()
	{
		String level = properties.getProperty(STRING_LOG_FILTER, "");
		return level;
	}
	
	public void setFixedFont(FontData fd)
	{
		properties.setProperty(STRING_FONT_FIXED_NAME,     fd.getName() );
		properties.setProperty(STRING_FONT_FIXED_SIZE,  ""+fd.getHeight() );
		properties.setProperty(STRING_FONT_FIXED_STYLE, ""+fd.getStyle() );
	}
	
	public FontData getFixedFont()
	{
		// Default:?
		String name   = properties.getProperty(STRING_FONT_FIXED_NAME, Const.FONT_FIXED_NAME);
		String ssize  = properties.getProperty(STRING_FONT_FIXED_SIZE);
		String sstyle = properties.getProperty(STRING_FONT_FIXED_STYLE);
		
		int size = Const.toInt(ssize, Const.FONT_FIXED_SIZE);
		int style = Const.toInt(sstyle, Const.FONT_FIXED_TYPE);
		
		FontData fd = new FontData(name, size, style);
		
		return fd;
	}

	public void setDefaultFont(FontData fd)
	{
        if (fd!=null)
        {
    		properties.setProperty(STRING_FONT_DEFAULT_NAME,     fd.getName() );
    		properties.setProperty(STRING_FONT_DEFAULT_SIZE,  ""+fd.getHeight() );
    		properties.setProperty(STRING_FONT_DEFAULT_STYLE, ""+fd.getStyle() );
        }
	}
	
	public FontData getDefaultFont()
	{
        if (isOSLookShown()) return display.getSystemFont().getFontData()[0];

		FontData def = getDefaultFontData();
		
		String name   = properties.getProperty(STRING_FONT_DEFAULT_NAME);
		String ssize  = properties.getProperty(STRING_FONT_DEFAULT_SIZE);
		String sstyle = properties.getProperty(STRING_FONT_DEFAULT_STYLE);

        int size = Const.toInt(ssize, def.getHeight());
        int style = Const.toInt(sstyle, def.getStyle());

        if (name==null || name.length()==0)
        {
            name = def.getName(); 
            size = def.getHeight();
            style=def.getStyle();
        }
        
        // Still nothing?
        if (name==null || name.length()==0)
        {
            name = "Arial";
            size = 10;
            style = SWT.NORMAL;
        }
        // System.out.println("Font default: ["+name+"], size="+size+", style="+style+", default font name = "+def.getName());
        
		FontData fd = new FontData(name, size, style);
		
		return fd;
	}

	public void setGraphFont(FontData fd)
	{
		properties.setProperty(STRING_FONT_GRAPH_NAME,     fd.getName() );
		properties.setProperty(STRING_FONT_GRAPH_SIZE,  ""+fd.getHeight() );
		properties.setProperty(STRING_FONT_GRAPH_STYLE, ""+fd.getStyle() );
	}
	
	public FontData getGraphFont()
	{
		FontData def = getDefaultFontData();
		
		String name   = properties.getProperty(STRING_FONT_GRAPH_NAME, def.getName());
		String ssize  = properties.getProperty(STRING_FONT_GRAPH_SIZE);
		String sstyle = properties.getProperty(STRING_FONT_GRAPH_STYLE);
		
		int size = Const.toInt(ssize, def.getHeight());
		int style = Const.toInt(sstyle, def.getStyle());
		
		FontData fd = new FontData(name, size, style);
		
		return fd;
	}

	public void setGridFont(FontData fd)
	{
		properties.setProperty(STRING_FONT_GRID_NAME,     fd.getName() );
		properties.setProperty(STRING_FONT_GRID_SIZE,  ""+fd.getHeight() );
		properties.setProperty(STRING_FONT_GRID_STYLE, ""+fd.getStyle() );
	}
	
	public FontData getGridFont()
	{
		FontData def = getDefaultFontData();
		
		String name   = properties.getProperty(STRING_FONT_GRID_NAME, def.getName());
		String ssize  = properties.getProperty(STRING_FONT_GRID_SIZE);
		String sstyle = properties.getProperty(STRING_FONT_GRID_STYLE);
		
		int size = Const.toInt(ssize, def.getHeight());
		int style = Const.toInt(sstyle, def.getStyle());
		
		FontData fd = new FontData(name, size, style);
		
		return fd;
	}

	public void setNoteFont(FontData fd)
	{
		properties.setProperty(STRING_FONT_NOTE_NAME,     fd.getName() );
		properties.setProperty(STRING_FONT_NOTE_SIZE,  ""+fd.getHeight() );
		properties.setProperty(STRING_FONT_NOTE_STYLE, ""+fd.getStyle() );
	}

	public FontData getNoteFont()
	{
		FontData def = getDefaultFontData();
		
		String name   = properties.getProperty(STRING_FONT_NOTE_NAME, def.getName());
		String ssize  = properties.getProperty(STRING_FONT_NOTE_SIZE);
		String sstyle = properties.getProperty(STRING_FONT_NOTE_STYLE);
		
		int size = Const.toInt(ssize, def.getHeight());
		int style = Const.toInt(sstyle, def.getStyle());
		
		FontData fd = new FontData(name, size, style);
		
		return fd;
	}

	public void setBackgroundRGB(RGB c)
	{
		properties.setProperty(STRING_BACKGROUND_COLOR_R,  c!=null?""+c.red:"" );
		properties.setProperty(STRING_BACKGROUND_COLOR_G,  c!=null?""+c.green:"" );
		properties.setProperty(STRING_BACKGROUND_COLOR_B,  c!=null?""+c.blue:"" );
	}
	
	public RGB getBackgroundRGB()
	{
        int r = Const.toInt(properties.getProperty(STRING_BACKGROUND_COLOR_R), Const.COLOR_BACKGROUND_RED); // Defaut: 
		int g = Const.toInt(properties.getProperty(STRING_BACKGROUND_COLOR_G), Const.COLOR_BACKGROUND_GREEN);
		int b = Const.toInt(properties.getProperty(STRING_BACKGROUND_COLOR_B), Const.COLOR_BACKGROUND_BLUE);
		RGB rgb = new RGB(r,g,b);
		
		return rgb;
	}
    
    /**
     * @deprecated
     * @return The background RGB color.
     */
    public RGB getBackupgroundRGB()
    {
        return getBackgroundRGB();
    }

	public void setGraphColorRGB(RGB c)
	{
		properties.setProperty(STRING_GRAPH_COLOR_R,  ""+c.red );
		properties.setProperty(STRING_GRAPH_COLOR_G,  ""+c.green );
		properties.setProperty(STRING_GRAPH_COLOR_B,  ""+c.blue );
	}
	
	public RGB getGraphColorRGB()
	{
		int r = Const.toInt(properties.getProperty(STRING_GRAPH_COLOR_R), Const.COLOR_GRAPH_RED); // default White
		int g = Const.toInt(properties.getProperty(STRING_GRAPH_COLOR_G), Const.COLOR_GRAPH_GREEN);
		int b = Const.toInt(properties.getProperty(STRING_GRAPH_COLOR_B), Const.COLOR_GRAPH_BLUE);
		RGB rgb = new RGB(r,g,b);
		
		return rgb;
	}

	public void setTabColorRGB(RGB c)
	{
		properties.setProperty(STRING_TAB_COLOR_R,  ""+c.red );
		properties.setProperty(STRING_TAB_COLOR_G,  ""+c.green );
		properties.setProperty(STRING_TAB_COLOR_B,  ""+c.blue );
	}

	public RGB getTabColorRGB()
	{
		int r = Const.toInt(properties.getProperty(STRING_TAB_COLOR_R), Const.COLOR_TAB_RED); // default White
		int g = Const.toInt(properties.getProperty(STRING_TAB_COLOR_G), Const.COLOR_TAB_GREEN);
		int b = Const.toInt(properties.getProperty(STRING_TAB_COLOR_B), Const.COLOR_TAB_BLUE);
		RGB rgb = new RGB(r,g,b);
		
		return rgb;
	}

	public void setIconSize(int size)
	{
		properties.setProperty(STRING_ICON_SIZE,  ""+size );
	}
	
	public int getIconSize()
	{
		return Const.toInt(properties.getProperty(STRING_ICON_SIZE), Const.ICON_SIZE);
	}

	public void setLineWidth(int width)
	{
		properties.setProperty(STRING_LINE_WIDTH,  ""+width );
	}
	
	public int getLineWidth()
	{
		return Const.toInt(properties.getProperty(STRING_LINE_WIDTH), Const.LINE_WIDTH);
	}
	
	public void setShadowSize(int size)
	{
		properties.setProperty(STRING_SHADOW_SIZE,  ""+size );
	}
	
	public int getShadowSize()
	{
		return Const.toInt(properties.getProperty(STRING_SHADOW_SIZE), Const.SHADOW_SIZE);
	}

	/*
	 * LICENCE INFO...
	 */

		
	public void setLastTrans(String trans)
	{
		properties.setProperty(STRING_LAST_PREVIEW_TRANS, trans);
	}

	public String   getLastTrans()
	{
		return properties.getProperty(STRING_LAST_PREVIEW_TRANS, "");
	}

	public void setLastPreview(String lastpreview[], int stepsize[])
	{
		properties.setProperty(STRING_LAST_PREVIEW_STEP, ""+lastpreview.length);

		for (int i=0;i<lastpreview.length;i++)
		{
			properties.setProperty(STRING_LAST_PREVIEW_STEP+(i+1), lastpreview[i]);
			properties.setProperty(STRING_LAST_PREVIEW_SIZE+(i+1), ""+stepsize[i]);
		}
	}
	
	public String[] getLastPreview()
	{
		String snr = properties.getProperty(STRING_LAST_PREVIEW_STEP);
		int nr = Const.toInt(snr, 0);
		String lp[] = new String[nr];
		for (int i=0;i<nr;i++)
		{
			lp[i] = properties.getProperty(STRING_LAST_PREVIEW_STEP+(i+1), "");
		}
		return lp;
	}

	public int[] getLastPreviewSize()
	{
		String snr = properties.getProperty(STRING_LAST_PREVIEW_STEP);
		int nr = Const.toInt(snr, 0);
		int si[] = new int[nr];
		for (int i=0;i<nr;i++)
		{
			si[i] = Const.toInt(properties.getProperty(STRING_LAST_PREVIEW_SIZE+(i+1), ""), 0);
		}
		return si;
	}
		
	public FontData getDefaultFontData()
	{
		return display.getSystemFont().getFontData()[0];
	}


	public void setMaxUndo(int max)
	{
		properties.setProperty(STRING_MAX_UNDO,  ""+max );
	}
	
	public int getMaxUndo()
	{
		return Const.toInt(properties.getProperty(STRING_MAX_UNDO), Const.MAX_UNDO);
	}
	
	public void setMiddlePct(int pct)
	{
		properties.setProperty(STRING_MIDDLE_PCT,  ""+pct );
	}
	
	public int getMiddlePct()
	{
		return Const.toInt(properties.getProperty(STRING_MIDDLE_PCT), Const.MIDDLE_PCT);
	}

	public void setScreen(WindowProperty winprop)
	{
		screens.put(winprop.getName(), winprop);
	}
	
	public WindowProperty getScreen(String windowname)
	{
		if (windowname==null) return null;
		return (WindowProperty)screens.get(windowname);
	}

	public void setSashWeights(int w[])
	{
		properties.setProperty(STRING_SASH_W1, ""+w[0]);
		properties.setProperty(STRING_SASH_W2, ""+w[1]);
	}

	public int[] getSashWeights()
	{
		int w1 = Const.toInt(properties.getProperty(STRING_SASH_W1), 25);
		int w2 = Const.toInt(properties.getProperty(STRING_SASH_W2), 75);
		
		return new int[] { w1, w2 };
	}

	public void setTipNr(int nr)
	{
		properties.setProperty(STRING_TIP_NR,  ""+nr);
	}
	
	public int getTipNr()
	{
		return Const.toInt(properties.getProperty(STRING_TIP_NR), 0);
	}

	public void setShowTips(boolean show)
	{
		properties.setProperty(STRING_SHOW_TIPS,  show?"Y":"N");
	}
	
	public boolean showTips()
	{
		String show=properties.getProperty(STRING_SHOW_TIPS);
		return !"N".equalsIgnoreCase(show);
	}

	public void setUseDBCache(boolean use)
	{
		properties.setProperty(STRING_USE_DB_CACHE, use?"Y":"N");
	}

	public boolean useDBCache()
	{
		String use=properties.getProperty(STRING_USE_DB_CACHE);
		return !"N".equalsIgnoreCase(use);
	}

	public void setOpenLastFile(boolean open)
	{
		properties.setProperty(STRING_OPEN_LAST_FILE, open?"Y":"N");
	}

	public boolean openLastFile()
	{
		String open=properties.getProperty(STRING_OPEN_LAST_FILE);
		return !"N".equalsIgnoreCase(open);
	}

	public void setLastRepository(String repname)
	{
		properties.setProperty(STRING_LAST_REPOSITORY, repname);
	}
	
	public String getLastRepository()
	{
		return properties.getProperty(STRING_LAST_REPOSITORY);
	}

	public void setLastRepositoryLogin(String login)
	{
		properties.setProperty(STRING_LAST_REPOSITORY_LOGIN, login);
	}
	
	public String getLastRepositoryLogin()
	{
		return properties.getProperty(STRING_LAST_REPOSITORY_LOGIN);
	}

	public void setAutoSave(boolean autosave)
	{
		properties.setProperty(STRING_AUTO_SAVE, autosave?"Y":"N");
	}

	public boolean getAutoSave()
	{
		String autosave=properties.getProperty(STRING_AUTO_SAVE);
		return "Y".equalsIgnoreCase(autosave); // Default = OFF
	}

	public void setSaveConfirmation(boolean saveconf)
	{
		properties.setProperty(STRING_SAVE_CONF, saveconf?"Y":"N");
	}

	public boolean getSaveConfirmation()
	{
		String saveconf=properties.getProperty(STRING_SAVE_CONF);
		return "Y".equalsIgnoreCase(saveconf); // Default = OFF
	}

	public void setAutoSplit(boolean autosplit)
	{
		properties.setProperty(STRING_AUTO_SPLIT, autosplit?"Y":"N");
	}

	public boolean getAutoSplit()
	{
		String autosplit=properties.getProperty(STRING_AUTO_SPLIT);
		return "Y".equalsIgnoreCase(autosplit); // Default = OFF
	}

	public void setOnlyActiveSteps(boolean only)
	{
		properties.setProperty(STRING_ONLY_ACTIVE_STEPS, only?"Y":"N");
	}
	
	public boolean getOnlyActiveSteps()
	{
		String only = properties.getProperty(STRING_ONLY_ACTIVE_STEPS, "N");
		return "Y".equalsIgnoreCase(only); // Default: show active steps.
	}
    
    /**
     * @param parameterName The parameter name
     * @param defaultValue The default value in case the parameter doesn't exist yet.
     * @return The custom parameter
     */
    public String getCustomParameter(String parameterName, String defaultValue)
    {
        return properties.getProperty(STRING_CUSTOM_PARAMETER+parameterName, defaultValue);
    }
    
    /**
     * Set the custom parameter
     * @param parameterName The name of the parameter
     * @param value The value to be stored in the properties file.
     */
    public void setCustomParameter(String parameterName, String value)
    {
        properties.setProperty(STRING_CUSTOM_PARAMETER+parameterName, value);
    }

    public void clearCustomParameters()
    {
        Enumeration keys = properties.keys();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            if (key.startsWith(STRING_CUSTOM_PARAMETER)) // Clear this one
            {
                properties.remove(key);
            }
        }
    }

    public boolean showRepositoriesDialogAtStartup()
    {
        String show = properties.getProperty(STRING_START_SHOW_REPOSITORIES, "Y");
        return "Y".equalsIgnoreCase(show); // Default: show warning before tool exit.
    }
    
    public void setExitWarningShown(boolean show)
    {
        properties.setProperty(STRING_SHOW_EXIT_WARNING, show?"Y":"N");
    }

    public boolean isAntiAliasingEnabled()
    {
        String anti = properties.getProperty(STRING_ANTI_ALIASING, "N");
        return "Y".equalsIgnoreCase(anti); // Default: don't do anti-aliasing
    }
    
    public void setAntiAliasingEnabled(boolean anti)
    {
        properties.setProperty(STRING_ANTI_ALIASING, anti?"Y":"N");
    }

    public boolean showExitWarning()
    {
        String show = properties.getProperty(STRING_SHOW_EXIT_WARNING, "Y");
        return "Y".equalsIgnoreCase(show); // Default: show repositories dialog at startup
    }
    
    public void setRepositoriesDialogAtStartupShown(boolean show)
    {
        properties.setProperty(STRING_START_SHOW_REPOSITORIES, show?"Y":"N");
    }
    
    public boolean isOSLookShown()
    {
        String show = properties.getProperty(STRING_SHOW_OS_LOOK, "N");
        return "Y".equalsIgnoreCase(show); // Default: don't show gray dialog boxes, show Kettle look.
    }
    
    public void setOSLookShown(boolean show)
    {
        properties.setProperty(STRING_SHOW_OS_LOOK, show?"Y":"N");
    }
    
    
    
    
    public static final void setGCFont(GC gc, Device device, FontData fontData)
    {
        if (Const.getOS().startsWith("Windows"))
        {
            Font font = new Font(device, fontData);
            gc.setFont( font );
            font.dispose();
        }
        else
        {
            gc.setFont( device.getSystemFont() );
        }
    }

    public void setLook(Control widget)
    {
        setLook(widget, WIDGET_STYLE_DEFAULT);
    }

    public void setLook(Control control, int style)
    {
        if (!Const.getOS().startsWith("Windows")) return;
        if (props.isOSLookShown() && style!=WIDGET_STYLE_FIXED) return;
        
        GUIResource gui = GUIResource.getInstance();
        Font font        = null;
        Color background = null;
        // Color tabColor   = null;
        
        switch(style) 
        {
        case WIDGET_STYLE_DEFAULT :
            background = gui.getColorBackground(); 
            font       = null; // GUIResource.getInstance().getFontDefault();
            break;
        case WIDGET_STYLE_FIXED   : 
            if (!props.isOSLookShown()) background = gui.getColorBackground(); 
            font       = gui.getFontFixed();
            break;
        case WIDGET_STYLE_TABLE   : 
            background = gui.getColorBackground(); 
            font       = null; // gui.getFontGrid();
            break;
        case WIDGET_STYLE_NOTEPAD : 
            background = gui.getColorBackground(); 
            font       = gui.getFontNote();
            break;
        case WIDGET_STYLE_GRAPH   : 
            background = gui.getColorBackground(); 
            font       = gui.getFontGraph();
            break;
        case WIDGET_STYLE_TAB     : 
            background = gui.getColorBackground(); 
            // font       = gui.getFontDefault();
            ((CTabFolder)control).setSimple(false);
            ((CTabFolder)control).setBorderVisible(false);
            ((CTabFolder)control).setSelectionBackground(display.getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
            break;
        default                   : 
            background = gui.getColorBackground(); 
            font       = null; // gui.getFontDefault();
            break;
        }

        if (font!=null)
        {
            control.setFont(font);
        }
        
        if (background!=null)
        {
            control.setBackground(background);
        }        
    }

    public static void setTableItemLook(TableItem item, Display disp)
    {
        if (!Const.getOS().startsWith("Windows")) return;
        
        Font gridFont    = null; // GUIResource.getInstance().getFontGrid();
        Color background = GUIResource.getInstance().getColorBackground();
        if (gridFont!=null)
        {
            item.setFont(gridFont);
        }
        if (background!=null)
        {
            item.setBackground(background);
        }
    }
    
    /**
     * Convert "argument 1" to 1
     * @param value The value to determine the argument number for
     * @return The argument number
     */
    public static final int getArgumentNumber(Value value)
    {
        if (value!=null && value.getName().startsWith("Argument "))
        {
            return Const.toInt(value.getName().substring("Argument ".length()), -1);
        }
        return -1;
    }
    
    public static final String[] convertArguments(Row row)
    {
        String args[] = new String[10];
        for (int i=0;i<row.size();i++)
        {
            Value value = row.getValue(i);
            int argNr = getArgumentNumber(value);
            if (argNr>=0 && argNr<10)
            {
                args[argNr] = value.getString();
            }
        }
        return args;
    }
    
    /**
     * Set the last arguments so that we can recall it the next time...
     * 
     * @param args the arguments to save
     */
    public void setLastArguments(String args[])
    {
        for (int i=0;i<args.length;i++)
        {
            if (args[i]!=null)
            {
                properties.setProperty(STRING_LAST_ARGUMENT+"_"+i, args[i]);
            }
        }
    }

    /** Get the last entered arguments...
     * 
     * @return the last entered arguments...
     */
    public String[] getLastArguments()
    {
        String args[] = new String[10];
        for (int i=0;i<args.length;i++)
        {
            args[i] = properties.getProperty(STRING_LAST_ARGUMENT+"_"+i);
        }
        return args;
    }
    
    /**
     * Get the list of recently used step
     * @return a list of strings: the plugin IDs
     */
    public List getPluginHistory()
    {
        return pluginHistory;
    }

    /**
     * Set the last plugin used in the plugin history
     * @param pluginID The last plugin ID
     */
    public void addPluginHistory(String pluginID)
    {
        // Add at the front
        pluginHistory.add(0, pluginID);
        
        // Remove in the rest of the list
        for (int i=pluginHistory.size()-1;i>0;i--)
        {
            String id = (String)pluginHistory.get(i);
            if (id.equalsIgnoreCase(pluginID)) pluginHistory.remove(i); 
        }
        savePluginHistory();
    }

    /**
     * Load the plugin history from the properties file
     *
     */
    private void loadPluginHistory()
    {
        pluginHistory = new ArrayList();
        int i=0;
        String pluginID = properties.getProperty(STRING_PLUGIN_HISTORY+"_"+i);
        while (pluginID!=null)
        {
            pluginHistory.add(pluginID);
            i++;
            pluginID = properties.getProperty(STRING_PLUGIN_HISTORY+"_"+i);
        }
    }
    
    private void savePluginHistory()
    {
        for (int i=0;i<pluginHistory.size();i++)
        {
            String id = (String) pluginHistory.get(i);
            properties.setProperty(STRING_PLUGIN_HISTORY+"_"+i, id);
        }
    }

    /**
     * @return Returns the display.
     */
    public Display getDisplay()
    {
        return display;
    }

    /**
     * @param display The display to set.
     */
    public void setDisplay(Display display)
    {
        this.display = display;
    }
    
    public void setDefaultPreviewSize(int size)
    {
        properties.setProperty(STRING_DEFAULT_PREVIEW_SIZE,  ""+size);
    }
    
    public int getDefaultPreviewSize()
    {
        return Const.toInt(properties.getProperty(STRING_DEFAULT_PREVIEW_SIZE), 100);
    }
    
    public boolean areOnlyUsedConnectionsSavedToXML()
    {
        String show = properties.getProperty(STRING_ONLY_USED_DB_TO_XML, "N");
        return !"N".equalsIgnoreCase(show); // Default: save all connections
    }
    
    public void setOnlyUsedConnectionsSavedToXML(boolean onlyUsedConnections)
    {
        properties.setProperty(STRING_ONLY_USED_DB_TO_XML, onlyUsedConnections?"Y":"N");
    }

    public boolean askAboutReplacingDatabaseConnections()
    {
        String ask = properties.getProperty(STRING_ASK_ABOUT_REPLACING_DATABASES, "N");
        return "Y".equalsIgnoreCase(ask);
    }
    
    public void setAskAboutReplacingDatabaseConnections(boolean ask)
    {
        properties.setProperty(STRING_ASK_ABOUT_REPLACING_DATABASES, ask?"Y":"N");
    }

    public boolean replaceExistingDatabaseConnections()
    {
        String replace = properties.getProperty(STRING_REPLACE_DATABASES, "Y");
        return "Y".equalsIgnoreCase(replace);
    }

    public void setReplaceDatabaseConnections(boolean replace)
    {
        properties.setProperty(STRING_REPLACE_DATABASES, replace?"Y":"N");
    }

    public boolean showCopyOrDistributeWarning()
    {
        String show = properties.getProperty(STRING_SHOW_COPY_OR_DISTRIBUTE_WARNING, "Y");
        return "Y".equalsIgnoreCase(show);
    }
    
    public void setShowCopyOrDistributeWarning(boolean show)
    {
        properties.setProperty(STRING_SHOW_COPY_OR_DISTRIBUTE_WARNING, show?"Y":"N");
    }
    
    public boolean showWelcomePageOnStartup()
    {
        String show = properties.getProperty(STRING_SHOW_WELCOME_PAGE_ON_STARTUP, "Y");
        return "Y".equalsIgnoreCase(show);
    }
    
    public void setShowWelcomePageOnStartup(boolean show)
    {
        properties.setProperty(STRING_SHOW_WELCOME_PAGE_ON_STARTUP, show?"Y":"N");
    }
    
    public int getMaxNrLinesInLog()
    {
        String lines = properties.getProperty(STRING_MAX_NR_LINES_IN_LOG);
        return Const.toInt(lines, Const.MAX_NR_LOG_LINES);
    }
    
    public void setMaxNrLinesInLog(int maxNrLinesInLog)
    {
        properties.setProperty(STRING_MAX_NR_LINES_IN_LOG, Integer.toString(maxNrLinesInLog));
    }

    public int getJobsDialogStyle()
    {
        String prop = properties.getProperty("JobDialogStyle");
        return parseStyle(prop);
    }
    
    public int getDialogStyle(String styleProperty)
    {
        String prop = properties.getProperty(styleProperty);
        if (Const.isEmpty(prop))
            return SWT.NONE;

        return parseStyle(prop);
    }

    private int parseStyle(String sStyle)
    {
        int style = SWT.DIALOG_TRIM;
        String[] styles = sStyle.split(",");
        for (int i = 0; i < styles.length; i++)
        {
            if ("APPLICATION_MODAL".equals(styles[i]))
                style |= SWT.APPLICATION_MODAL;
            else if ("RESIZE".equals(styles[i]))
                style |= SWT.RESIZE;
            else if ("MIN".equals(styles[i]))
                style |= SWT.MIN;
            else if ("MAX".equals(styles[i]))
                style |= SWT.MAX;
        }
        
        return style;
    }

    public void setDialogSize(Shell shell, String styleProperty)
    {
        String prop = properties.getProperty(styleProperty);
        if (Const.isEmpty(prop))
            return;

        String[] xy = prop.split(",");
        if (xy.length != 2)
            return;

        shell.setSize(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
    }    
}
