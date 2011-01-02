 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 
package org.pentaho.di.ui.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.ObjectUsageCount;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.gui.GUIOption;
import org.pentaho.di.core.gui.GUIPositionInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.laf.BasePropertyHandler;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;


/**
 * We use Props to store all kinds of user interactive information such as the selected colors, fonts, positions of windows, etc.
 * 
 * @author Matt
 * @since 15-12-2003
 *
 */
public class PropsUI extends Props
{
	
	private static final String NO = "N";

	private static final String YES = "Y";

	private static Display display;
		
	protected List<LastUsedFile> lastUsedFiles;
	protected List<LastUsedFile> openTabFiles;

    protected String overriddenFileName;
  
	private Hashtable<String,WindowProperty> screens;
	
    private static final String STRING_SHOW_COPY_OR_DISTRIBUTE_WARNING = "ShowCopyOrDistributeWarning";
    
    private static final String STRING_SHOW_WELCOME_PAGE_ON_STARTUP = "ShowWelcomePageOnStartup";

    private static final String STRING_SHOW_BRANDING_GRAPHICS = "ShowBrandingGraphics";

    private static final String STRING_ONLY_SHOW_ACTIVE_FILE = "OnlyShowActiveFileInTree";
    
    private static final String SHOW_TOOL_TIPS = "ShowToolTips";
    
    private static final String SHOW_HELP_TOOL_TIPS = "ShowHelpToolTips";

    private static final String CANVAS_GRID_SIZE = "CanvasGridSize";

    private static List<GUIOption<Object>> editables;

	/**
	 * Initialize the properties: load from disk.
	 * @param display The Display
	 * @param t The type of properties file.
	 */
	public static final void init(Display d, int t)
	{
		if (props==null)
		{
        	display = d;
			props = new PropsUI(t);
            
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
    public static final void init(Display d, String filename)
    {
        if (props==null)
        {
        	display = d;
            props = new PropsUI(filename);
            
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
	
	public static final PropsUI getInstance()
	{
		if (props!=null) return (PropsUI) props;
		
		throw new RuntimeException("Properties, Kettle systems settings, not initialised!");
	}
	
	private PropsUI(int t)
	{
		super(t);
	}

    private PropsUI(String filename)
    {
    	super(filename);
    }
    
	@SuppressWarnings("unchecked")
	protected synchronized void init() {
		super.createLogChannel();
		properties = new Properties();
        pluginHistory = new ArrayList<ObjectUsageCount>();

        setDefault();
        loadProps();
        addDefaultEntries();
        
        loadPluginHistory();

		loadScreens();
        loadLastUsedFiles();
        loadOpenTabFiles();

        PluginRegistry registry = PluginRegistry.getInstance();
        List<PluginInterface> plugins = registry.getPlugins(LifecyclePluginType.class);
        List<GUIOption<Object>> leditables = new ArrayList<GUIOption<Object>>();
        for (PluginInterface plugin : plugins) {
        	try {
        		leditables.add( registry.loadClass(plugin, GUIOption.class) );
        	} catch(Exception e) {
        		LogChannel.GENERAL.logError("Unexpected error loading class for plugin "+plugin.getName(), e);
        	}
        }
        
        editables = Collections.unmodifiableList(leditables);

	}

	public void setDefault()
	{
		FontData fd;
		RGB col;

        lastUsedFiles = new ArrayList<LastUsedFile>();
        openTabFiles = new ArrayList<LastUsedFile>();
		screens = new Hashtable<String,WindowProperty>();

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
			
			// Set Default Look for all dialogs and sizes.
			String prop = BasePropertyHandler.getProperty("Default_UI_Properties_Resource", "org.pentaho.di.ui.core.default"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				ResourceBundle bundle = ResourceBundle.getBundle(prop);
				// ResourceBundle bundle = ResourceBundle.getBundle(prop);
				if (bundle != null) {
					Enumeration<String> enumer = bundle.getKeys();
					String theKey;
					while (enumer.hasMoreElements()) {
						theKey = enumer.nextElement();
						properties.setProperty(theKey, bundle.getString(theKey));
					}
				}
			} catch (Exception ex) {
				// don't throw an exception, but log it.
				ex.printStackTrace();
			}
		}
    
	}
	
	public void storeScreens()
	{
		// Add screens hash table to properties..
		// 
		Enumeration<String> keys = screens.keys();
		int nr=1;
		while (keys.hasMoreElements())
		{
			String name = (String)keys.nextElement();
			properties.setProperty("ScreenName"+nr, name);
			
			WindowProperty winprop = screens.get(name);
			properties.setProperty(STRING_SIZE_MAX+nr, winprop.isMaximized()?YES:NO);
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
		screens = new Hashtable<String,WindowProperty>();
		
		int nr = 1;
		
		String name = properties.getProperty("ScreenName"+nr);
		while (name!=null)
		{
			boolean max = YES.equalsIgnoreCase(properties.getProperty(STRING_SIZE_MAX+nr));
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
        setOpenTabFiles();

        super.saveProps();
	}
    
	public void setLastFiles()
	{
		properties.setProperty("lastfiles", ""+lastUsedFiles.size());
		for (int i=0;i<lastUsedFiles.size();i++)
		{
            LastUsedFile lastUsedFile = lastUsedFiles.get(i);
            
            properties.setProperty("filetype"+(i+1), Const.NVL(lastUsedFile.getFileType(), LastUsedFile.FILE_TYPE_TRANSFORMATION));
			properties.setProperty("lastfile"+(i+1), Const.NVL(lastUsedFile.getFilename(), ""));
			properties.setProperty("lastdir"+(i+1),  Const.NVL(lastUsedFile.getDirectory(), ""));
			properties.setProperty("lasttype"+(i+1), lastUsedFile.isSourceRepository()?YES:NO);
			properties.setProperty("lastrepo"+(i+1), Const.NVL(lastUsedFile.getRepositoryName(), ""));
		}
	}

	public void setOpenTabFiles()
	{
		properties.setProperty("tabfiles", ""+openTabFiles.size());
		for (int i=0;i<openTabFiles.size();i++)
		{
            LastUsedFile openTabFile = openTabFiles.get(i);
            
            properties.setProperty("tabtype"+(i+1), Const.NVL(openTabFile.getFileType(), LastUsedFile.FILE_TYPE_TRANSFORMATION));
			properties.setProperty("tabfile"+(i+1), Const.NVL(openTabFile.getFilename(), ""));
			properties.setProperty("tabdir"+(i+1),  Const.NVL(openTabFile.getDirectory(), ""));
			properties.setProperty("tabrep"+(i+1), openTabFile.isSourceRepository()?YES:NO);
			properties.setProperty("tabrepname"+(i+1), Const.NVL(openTabFile.getRepositoryName(), ""));
			properties.setProperty("tabopened"+(i+1), openTabFile.isOpened()?YES:NO);
			properties.setProperty("tabopentypes"+(i+1), ""+openTabFile.getOpenItemTypes());
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
		LastUsedFile lastUsedFile = new LastUsedFile(fileType, filename, directory, sourceRepository, repositoryName, false, LastUsedFile.OPENED_ITEM_TYPE_MASK_GRAPH);
        
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
	
	/**
	 * Add a last opened file to the top of the recently used list.
     * @param fileType the type of file to use @see LastUsedFile
	 * @param filename The name of the file or transformation
	 * @param directory The repository directory path, null in case lf is an XML file  
	 * @param sourceRepository True if the file was loaded from repository, false if ld is an XML file.
	 * @param repositoryName The name of the repository the file was loaded from or save to.
	 */
	public void addOpenTabFile(String fileType, String filename, String directory, boolean sourceRepository, String repositoryName, int openTypes)
	{
		LastUsedFile lastUsedFile = new LastUsedFile(fileType, filename, directory, sourceRepository, repositoryName, true, openTypes);
        openTabFiles.add(lastUsedFile);
	}
	
    public void loadLastUsedFiles()
    {
        lastUsedFiles = new ArrayList<LastUsedFile>();
        int nr = Const.toInt(properties.getProperty("lastfiles"), 0);
        for (int i=0;i<nr;i++)
        {
            String fileType = properties.getProperty("filetype"+(i+1), LastUsedFile.FILE_TYPE_TRANSFORMATION); // default: transformation
            String filename = properties.getProperty("lastfile"+(i+1), "");
            String directory = properties.getProperty("lastdir"+(i+1), "");
            boolean sourceRepository = YES.equalsIgnoreCase(properties.getProperty("lasttype"+(i+1), NO));
            String repositoryName = properties.getProperty("lastrepo"+(i+1));
            boolean isOpened = YES.equalsIgnoreCase(properties.getProperty("lastopened"+(i+1), NO));
            int openItemTypes = Const.toInt(properties.getProperty("lastopentypes"+(i+1), "0"), 0);
            
            lastUsedFiles.add(new LastUsedFile(fileType, filename, directory, sourceRepository, repositoryName, isOpened, openItemTypes));
        }
    }
    
    public void loadOpenTabFiles()
    {
        openTabFiles = new ArrayList<LastUsedFile>();
        int nr = Const.toInt(properties.getProperty("tabfiles"), 0);
        for (int i=0;i<nr;i++)
        {
            String fileType = properties.getProperty("tabtype"+(i+1), LastUsedFile.FILE_TYPE_TRANSFORMATION); // default: transformation
            String filename = properties.getProperty("tabfile"+(i+1), "");
            String directory = properties.getProperty("tabdir"+(i+1), "");
            boolean sourceRepository = YES.equalsIgnoreCase(properties.getProperty("tabrep"+(i+1), NO));
            String repositoryName = properties.getProperty("tabrepname"+(i+1));
            boolean isOpened = YES.equalsIgnoreCase(properties.getProperty("tabopened"+(i+1), NO));
            int openItemTypes = Const.toInt(properties.getProperty("tabopentypes"+(i+1), "0"), 0);
            
            openTabFiles.add(new LastUsedFile(fileType, filename, directory, sourceRepository, repositoryName, isOpened, openItemTypes));
        }
    }
    
    public List<LastUsedFile> getLastUsedFiles()
    {
        return lastUsedFiles;
    }
    
    public void setLastUsedFiles(List<LastUsedFile> lastUsedFiles)
    {
        this.lastUsedFiles = lastUsedFiles;
    }
    
    public String[] getLastFileTypes()
    {
        String retval[] = new String[lastUsedFiles.size()];
        for (int i=0;i<retval.length;i++)
        {
            LastUsedFile lastUsedFile = lastUsedFiles.get(i);
            retval[i]=lastUsedFile.getFileType();
        }
        return retval;
    }
    
	public String[] getLastFiles()
	{
        String retval[] = new String[lastUsedFiles.size()];
        for (int i=0;i<retval.length;i++)
        {
            LastUsedFile lastUsedFile = lastUsedFiles.get(i);
            retval[i]=lastUsedFile.getFilename();
        }
		return retval;
	}
	
  public String getFilename() 
  {
    if (this.filename == null) {
      String s = System.getProperty("org.pentaho.di.ui.PropsUIFile"); //$NON-NLS-1$
      if (s != null) {
        return s;
      } else {
        return super.getFilename();
      }
    } else {
      return this.filename;
    }
  }
  
	public String[] getLastDirs()
	{
        String retval[] = new String[lastUsedFiles.size()];
        for (int i=0;i<retval.length;i++)
        {
            LastUsedFile lastUsedFile = lastUsedFiles.get(i);
            retval[i]=lastUsedFile.getDirectory();
        }
        return retval;
	}


	public boolean[] getLastTypes()
	{
        boolean retval[] = new boolean[lastUsedFiles.size()];
        for (int i=0;i<retval.length;i++)
        {
            LastUsedFile lastUsedFile = lastUsedFiles.get(i);
            retval[i]=lastUsedFile.isSourceRepository();
        }
        return retval;
	}

	public String[] getLastRepositories()
	{
        String retval[] = new String[lastUsedFiles.size()];
        for (int i=0;i<retval.length;i++)
        {
            LastUsedFile lastUsedFile = lastUsedFiles.get(i);
            retval[i]=lastUsedFile.getRepositoryName();
        }
        return retval;
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
		String name   = properties.getProperty(STRING_FONT_FIXED_NAME, ConstUI.FONT_FIXED_NAME);
		String ssize  = properties.getProperty(STRING_FONT_FIXED_SIZE);
		String sstyle = properties.getProperty(STRING_FONT_FIXED_STYLE);
		
		int size = Const.toInt(ssize, ConstUI.FONT_FIXED_SIZE);
		int style = Const.toInt(sstyle, ConstUI.FONT_FIXED_TYPE);
		
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
        int r = Const.toInt(properties.getProperty(STRING_BACKGROUND_COLOR_R), ConstUI.COLOR_BACKGROUND_RED); // Defaut: 
		int g = Const.toInt(properties.getProperty(STRING_BACKGROUND_COLOR_G), ConstUI.COLOR_BACKGROUND_GREEN);
		int b = Const.toInt(properties.getProperty(STRING_BACKGROUND_COLOR_B), ConstUI.COLOR_BACKGROUND_BLUE);
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
		int r = Const.toInt(properties.getProperty(STRING_GRAPH_COLOR_R), ConstUI.COLOR_GRAPH_RED); // default White
		int g = Const.toInt(properties.getProperty(STRING_GRAPH_COLOR_G), ConstUI.COLOR_GRAPH_GREEN);
		int b = Const.toInt(properties.getProperty(STRING_GRAPH_COLOR_B), ConstUI.COLOR_GRAPH_BLUE);
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
		int r = Const.toInt(properties.getProperty(STRING_TAB_COLOR_R), ConstUI.COLOR_TAB_RED); // default White
		int g = Const.toInt(properties.getProperty(STRING_TAB_COLOR_G), ConstUI.COLOR_TAB_GREEN);
		int b = Const.toInt(properties.getProperty(STRING_TAB_COLOR_B), ConstUI.COLOR_TAB_BLUE);
		RGB rgb = new RGB(r,g,b);
		
		return rgb;
	}

	public void setIconSize(int size)
	{
		properties.setProperty(STRING_ICON_SIZE,  ""+size );
	}
	
	public int getIconSize()
	{
		return Const.toInt(properties.getProperty(STRING_ICON_SIZE), ConstUI.ICON_SIZE);
	}

	public void setLineWidth(int width)
	{
		properties.setProperty(STRING_LINE_WIDTH,  ""+width );
	}
	
	public int getLineWidth()
	{
		return Const.toInt(properties.getProperty(STRING_LINE_WIDTH), ConstUI.LINE_WIDTH);
	}
	
	public void setShadowSize(int size)
	{
		properties.setProperty(STRING_SHADOW_SIZE,  ""+size );
	}
	
	public int getShadowSize()
	{
		return Const.toInt(properties.getProperty(STRING_SHADOW_SIZE), Const.SHADOW_SIZE);
	}

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
		return screens.get(windowname);
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
		properties.setProperty(STRING_SHOW_TIPS,  show?YES:NO);
	}
	
	public boolean showTips()
	{
		String show=properties.getProperty(STRING_SHOW_TIPS);
		return !NO.equalsIgnoreCase(show);
	}

	public void setOpenLastFile(boolean open)
	{
		properties.setProperty(STRING_OPEN_LAST_FILE, open?YES:NO);
	}

	public boolean openLastFile()
	{
		String open=properties.getProperty(STRING_OPEN_LAST_FILE);
		return !NO.equalsIgnoreCase(open);
	}

	public void setAutoSave(boolean autosave)
	{
		properties.setProperty(STRING_AUTO_SAVE, autosave?YES:NO);
	}

	public boolean getAutoSave()
	{
		String autosave=properties.getProperty(STRING_AUTO_SAVE);
		return YES.equalsIgnoreCase(autosave); // Default = OFF
	}

	public void setSaveConfirmation(boolean saveconf)
	{
		properties.setProperty(STRING_SAVE_CONF, saveconf?YES:NO);
	}

	public boolean getSaveConfirmation()
	{
		String saveconf=properties.getProperty(STRING_SAVE_CONF);
		return YES.equalsIgnoreCase(saveconf); // Default = OFF
	}

	public void setAutoSplit(boolean autosplit)
	{
		properties.setProperty(STRING_AUTO_SPLIT, autosplit?YES:NO);
	}

	public boolean getAutoSplit()
	{
		String autosplit=properties.getProperty(STRING_AUTO_SPLIT);
		return YES.equalsIgnoreCase(autosplit); // Default = OFF
	}

  public void setAutoCollapseCoreObjectsTree(boolean autoCollapse)
  {
    properties.setProperty(STRING_AUTO_COLLAPSE_CORE_TREE, autoCollapse?YES:NO);
  }

  public boolean getAutoCollapseCoreObjectsTree()
  {
    String autoCollapse=properties.getProperty(STRING_AUTO_COLLAPSE_CORE_TREE);
    return YES.equalsIgnoreCase(autoCollapse); // Default = OFF
  }

  public boolean showRepositoriesDialogAtStartup()
    {
        String show = properties.getProperty(STRING_START_SHOW_REPOSITORIES, YES);
        return YES.equalsIgnoreCase(show); // Default: show warning before tool exit.
    }
    
    public void setExitWarningShown(boolean show)
    {
        properties.setProperty(STRING_SHOW_EXIT_WARNING, show?YES:NO);
    }

    public boolean isAntiAliasingEnabled()
    {
        String anti = properties.getProperty(STRING_ANTI_ALIASING, NO);
        return YES.equalsIgnoreCase(anti); // Default: don't do anti-aliasing
    }
    
    public void setAntiAliasingEnabled(boolean anti)
    {
        properties.setProperty(STRING_ANTI_ALIASING, anti?YES:NO);
    }

    public boolean showExitWarning()
    {
        String show = properties.getProperty(STRING_SHOW_EXIT_WARNING, YES);
        return YES.equalsIgnoreCase(show); // Default: show repositories dialog at startup
    }
    
    public void setRepositoriesDialogAtStartupShown(boolean show)
    {
        properties.setProperty(STRING_START_SHOW_REPOSITORIES, show?YES:NO);
    }
    
    public boolean isOSLookShown()
    {
        String show = properties.getProperty(STRING_SHOW_OS_LOOK, NO);
        return YES.equalsIgnoreCase(show); // Default: don't show gray dialog boxes, show Kettle look.
    }
    
    public void setOSLookShown(boolean show)
    {
        properties.setProperty(STRING_SHOW_OS_LOOK, show?YES:NO);
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
    	if (this.isOSLookShown() && style!=WIDGET_STYLE_FIXED) return;
        
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
            if (!this.isOSLookShown()) background = gui.getColorBackground(); 
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
            CTabFolder tabFolder = (CTabFolder)control; 
            tabFolder.setSimple(false);
            tabFolder.setBorderVisible(false);

            // Set a small vertical gradient
            tabFolder.setSelectionBackground(new Color[] {
                    display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
                    display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW),
                    }, 
                    new int[] { 55, },
                    true);
            break;
        default                   : 
            background = gui.getColorBackground(); 
            font       = null; // gui.getFontDefault();
            break;
        }

        if (font!=null && !font.isDisposed())
        {
            control.setFont(font);
        }
        
        if (background!=null && !background.isDisposed())
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
     * @return Returns the display.
     */
    public static Display getDisplay()
    {
        return display;
    }

    /**
     * @param display The display to set.
     */
    public static void setDisplay(Display d)
    {
        display = d;
    }
    
    public void setDefaultPreviewSize(int size)
    {
        properties.setProperty(STRING_DEFAULT_PREVIEW_SIZE,  ""+size);
    }
    
    public int getDefaultPreviewSize()
    {
        return Const.toInt(properties.getProperty(STRING_DEFAULT_PREVIEW_SIZE), 1000);
    }
    
    public boolean showCopyOrDistributeWarning()
    {
        String show = properties.getProperty(STRING_SHOW_COPY_OR_DISTRIBUTE_WARNING, YES);
        return YES.equalsIgnoreCase(show);
    }
    
    public void setShowCopyOrDistributeWarning(boolean show)
    {
        properties.setProperty(STRING_SHOW_COPY_OR_DISTRIBUTE_WARNING, show?YES:NO);
    }
    
    public boolean showWelcomePageOnStartup()
    {
        String show = properties.getProperty(STRING_SHOW_WELCOME_PAGE_ON_STARTUP, YES);
        return YES.equalsIgnoreCase(show);
    }
    
    public void setShowWelcomePageOnStartup(boolean show)
    {
        properties.setProperty(STRING_SHOW_WELCOME_PAGE_ON_STARTUP, show?YES:NO);
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
    
    public boolean isBrandingActive()
    {
        String show = properties.getProperty(STRING_SHOW_BRANDING_GRAPHICS, NO);
        return YES.equalsIgnoreCase(show);
    }
    
    public void setBrandingActive(boolean active)
    {
        properties.setProperty(STRING_SHOW_BRANDING_GRAPHICS, active?YES:NO);
    }

    public boolean isOnlyActiveFileShownInTree()
    {
        String show = properties.getProperty(STRING_ONLY_SHOW_ACTIVE_FILE, YES);
        return YES.equalsIgnoreCase(show);
    }
    
    public void setOnlyActiveFileShownInTree(boolean show)
    {
        properties.setProperty(STRING_ONLY_SHOW_ACTIVE_FILE, show?YES:NO);
    }
    
    public boolean showToolTips()
    {
    	return YES.equalsIgnoreCase(properties.getProperty(SHOW_TOOL_TIPS,YES));
    }
    
    public void setShowToolTips(boolean show)
    {
        properties.setProperty(SHOW_TOOL_TIPS, show?YES:NO);
    }
    
    public List<GUIOption<Object>> getRegisteredEditableComponents()
    {
    	return editables;
    }

	public boolean isShowingHelpToolTips() 
	{
		return YES.equalsIgnoreCase(properties.getProperty(SHOW_HELP_TOOL_TIPS,YES));
	}
	
	public void setShowingHelpToolTips(boolean show) 
	{
		properties.setProperty(SHOW_HELP_TOOL_TIPS, show?YES:NO);
	}

	/**
	 * @return the openTabFiles
	 */
	public List<LastUsedFile> getOpenTabFiles() {
		return openTabFiles;
	}

	/**
	 * @param openTabFiles the openTabFiles to set
	 */
	public void setOpenTabFiles(List<LastUsedFile> openTabFiles) {
		this.openTabFiles = openTabFiles;
	}
	
	public int getCanvasGridSize() 
	{
		return Const.toInt(properties.getProperty(CANVAS_GRID_SIZE,"1"), 1);
	}
	
	public void setCanvasGridSize(int gridSize) 
	{
		properties.setProperty(CANVAS_GRID_SIZE, Integer.toString(gridSize));
	}

    public static final void setLocation(GUIPositionInterface guiElement, int x, int y) {
      if (x<0) x=0;
      if (y<0) y=0;
      guiElement.setLocation(calculateGridPosition(new Point(x,y)));
	}
    
    public static final Point calculateGridPosition(Point p) {
    	int gridSize = PropsUI.getInstance().getCanvasGridSize();
    	if (gridSize>1) {
    		// Snap to grid...
    		//
    		return new Point(gridSize*Math.round(p.x/gridSize), gridSize*Math.round(p.y/gridSize));
    	} else {
    		// Normal draw
    		//
    		return p;
    	}
    }
    
    public boolean isIndicateSlowTransStepsEnabled()
    {
        String indicate= properties.getProperty(STRING_INDICATE_SLOW_TRANS_STEPS, "Y");
        return YES.equalsIgnoreCase(indicate);
    }
    
    public void setIndicateSlowTransStepsEnabled(boolean indicate)
    {
        properties.setProperty(STRING_INDICATE_SLOW_TRANS_STEPS, indicate?YES:NO);
    }    

}
