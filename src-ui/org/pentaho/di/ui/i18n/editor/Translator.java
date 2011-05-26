/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.i18n.editor;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterListDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



public class Translator
{
    public static final String APP_NAME = "Pentaho Translator";
    public static final String ROOT = "src/be/ibridge/kettle";
    public static final String EXTENSION = ".properties";
    public static final String MESSAGES_DIR = "messages";
    public static final String MESSAGES_PREFIX = "messages";
    public static final int    LOCALE_LENGTH = 5;
    public static final String SYSTEM_KEY_PREFIX = "System";
    public static final String REFERENCE_LOCALE = "en_US";
    
    private Display display;
    private Shell shell;
    private LogChannelInterface log;
    private PropsUI props;
    private Color unusedColor;
    
    // All the properties files found...
    Hashtable<String,Properties> files;
    private SashForm sashform;
    private List wList;
    private TableView wGrid;
    private Hashtable<String,Integer> directories;
    private Hashtable<String,Boolean> locales;
    private Hashtable<String,String> javaFiles;

    private Button wReload;
    private Button wClose;
    private Button wLocale;
    private Button wVerify;
    private Button wUsed;
    private Button wAvailable;
    
    public Translator(Display display)
    {
        this.display = display;
        this.log = new LogChannel(APP_NAME);
        this.props = PropsUI.getInstance();

        clear();

        unusedColor = display.getSystemColor(SWT.COLOR_YELLOW);
    }
    
    private void clear()
    {
        files = new Hashtable<String,Properties>(500);
        directories = new Hashtable<String,Integer>(100);
        javaFiles = new Hashtable<String,String>(500);
        locales = new Hashtable<String,Boolean>(20);
    }
    
    public void readFiles(String directory) throws KettleFileException
    {
        log.logBasic("Scanning directory: "+directory);
        try
        {
            File file = new File(directory);
            
            File[] entries = file.listFiles();
            
            for (int i=0;i<entries.length;i++)
            {
                File entry = entries[i];
                if (entry.isDirectory())
                {
                    if (!entry.getName().startsWith(".svn"))
                    {
                        readFiles(directory+"/"+entry.getName());
                    }
                }
                else
                {
                    if (entry.isFile())
                    {
                        if (entry.getName().endsWith(".properties")) // Load this one!
                        {
                            String filename = directory+"/"+entry.getName(); 
                            log.logBasic("Reading properties file: "+filename+"  ("+entry.getAbsolutePath()+")");
                            Properties properties = new Properties();
                            properties.load(new FileInputStream(entry));
                            
                            // Store it in the map:
                            files.put(filename, properties);
                        }
                    }
                }
            }
            
            // get the list of distinct directories:
            // at the same time, keep a list of available locales
            //
            directories = new Hashtable<String,Integer>(files.size());
            locales = new Hashtable<String,Boolean>(10);
            
            for (String filename : files.keySet())
            {
                String path = getPath(filename);
                
                // is it already in there?
                Integer num = directories.get(path);
                if (num!=null) 
                { 
                    num = Integer.valueOf(num.intValue()+1); 
                }
                else
                {
                    num = Integer.valueOf(1);
                }
                directories.put(path, num);
                
                // What's the locale?
                String locale = getLocale(filename);
                locales.put(locale, Boolean.TRUE);
                
                if (locale.charAt(2)!='_')
                {
                	log.logError("This i18n locale file is not conform the Kettle standard: "+filename);
                }
            }
        }
        catch(Exception e)
        {
            throw new KettleFileException("Unable to get all files from directory ["+ROOT+"]", e);
        }
    }
    
    public void open()
    {
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setText(APP_NAME);
        
        try
        {
            readFiles(ROOT);
        }
        catch(Exception e)
        {
            new ErrorDialog(shell, "Error reading translations", "There was an unexpected error reading the translations", e);        
        }
        
        // Put something on the screen
        sashform = new SashForm(shell, SWT.HORIZONTAL);
        sashform.setLayout(new FillLayout());
        
        addList();
        addGrid();
        addListeners();
        
        sashform.setWeights(new int[] { 30, 70 });
        sashform.setVisible(true);
        
        refresh();
        
		BaseStepDialog.setSize(shell);

        shell.open();
    }
    
    private void addListeners()
    {
        // In case someone dares to press the [X] in the corner ;-)
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { e.doit=quitFile(); } } );

        // wOK : nothing yet
        
        // wLocale
        wLocale.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent arg0)
                {
                    selectLocales();
                    refreshGrid();
                }
            }
        );
        
        wReload.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent arg0)
                {
                    int idx[] = wList.getSelectionIndices();
                    reload();
                    wList.setSelection(idx);
                    refreshGrid();
                }
            }
        );
        
        wClose.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent arg0)
                {
                    quitFile();
                }
            }
        );
        
        wUsed.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent arg0)
                {
                    refreshGrid();
                }
            }
        );

        wAvailable.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent arg0)
                    {
                        refreshGrid();
                    }
                }
            );        
    }

    public void reload()
    {
        try
        {
            // Clear the hashtables...
            clear();
            
            shell.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));
            readFiles(ROOT);
            shell.setCursor(null);

            javaFiles = new Hashtable<String,String>(500);
            
            refresh();
        }
        catch(Exception e)
        {
            new ErrorDialog(shell, "Error loading data", "There was an unexpected error re-loading the data", e);
        }
    }

    public boolean quitFile()
    {
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        props.saveProps();

        shell.dispose();
        display.dispose();
        return true;
    }

    private void addList()
    {
        Composite composite = new Composite(sashform, SWT.NONE);
        props.setLook(composite);
        FillLayout fillLayout = new FillLayout();
        fillLayout.marginWidth  = Const.FORM_MARGIN;
        fillLayout.marginHeight = Const.FORM_MARGIN;
        composite.setLayout(fillLayout);
        
        // Make a listbox
        wList = new List(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        
        // Add a selection listener.
        wList.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent arg0)
                {
                    refreshGrid();
                }
            }
        );
    }

    private void addGrid()
    {
        Composite composite = new Composite(sashform, SWT.NONE);
        props.setLook(composite);
        
        FormLayout formLayout = new FormLayout ();
        formLayout.marginWidth  = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;
        composite.setLayout(formLayout);

        wReload = new Button(composite, SWT.NONE);
        wReload.setText("  &Reload  ");
        wLocale= new Button(composite, SWT.NONE);
        wLocale.setText("  &Select locale  ");
        wClose = new Button(composite, SWT.NONE);
        wClose.setText("  &Close ");
        wVerify = new Button(composite, SWT.CHECK);
        wVerify.setText("&Verify usage");
        wVerify.setSelection(true);  // Check it!
        wUsed = new Button(composite, SWT.CHECK);
        wUsed.setText("&Remove used keys");
        wUsed.setSelection(false);  // Check it!
        wAvailable= new Button(composite, SWT.CHECK);
        wAvailable.setText("&Check key against other locale");
        wAvailable.setSelection(true);  // Check it!
        
        BaseStepDialog.positionBottomButtons(composite, new Button[] { wReload, wLocale, wClose, wVerify, wUsed, wAvailable } , Const.MARGIN, null);

        ColumnInfo[] colinf=new ColumnInfo[]
           {
             new ColumnInfo("Locale",           ColumnInfo.COLUMN_TYPE_TEXT,   true),
             new ColumnInfo("Package",          ColumnInfo.COLUMN_TYPE_TEXT,   true),
             new ColumnInfo("Class",            ColumnInfo.COLUMN_TYPE_TEXT,   true),
             new ColumnInfo("Key",              ColumnInfo.COLUMN_TYPE_TEXT,   true),
             new ColumnInfo("Value",            ColumnInfo.COLUMN_TYPE_TEXT,   true),
             new ColumnInfo("Used?",            ColumnInfo.COLUMN_TYPE_TEXT,   true),
             new ColumnInfo("Not available in", ColumnInfo.COLUMN_TYPE_TEXT,   true),
           };
        
        wGrid=new TableView(  Variables.getADefaultVariableSpace(),
        		              composite, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
                              colinf, 
                              0,  
                              null,
                              PropsUI.getInstance()
                              );
        
        FormData fdGrid=new FormData();
        fdGrid.left = new FormAttachment(0, 0);
        fdGrid.top  = new FormAttachment(0, 0);
        fdGrid.right  = new FormAttachment(100, 0);
        fdGrid.bottom = new FormAttachment(wReload, -Const.MARGIN*3);
        wGrid.setLayoutData(fdGrid);

    }


    public void refresh()
    {
        refreshList();
        refreshGrid();
    }
    
    public void refreshList()
    {
        // OK, now we have a distinct list of directories or packages to work with...
        ArrayList<String> dirList = new ArrayList<String>(directories.keySet());
        Collections.sort(dirList);
        
        // Put it in the listbox:
        wList.removeAll();
        
        for (int i=0;i<dirList.size();i++)
        {
            wList.add(dirList.get(i));
        }
    }

    public void refreshGrid()
    {
        try
        {
            if (wList.getSelectionCount()>0)
            {
                String languages[] = getSelectedLocale();
                System.out.println("Selected languages: "+languages.length);
                
                shell.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));
                
                // Remove all entries..
                wGrid.table.clearAll();
                
                for (int i=0;i<wList.getSelectionCount();i++)
                {
                    String dir = wList.getSelection()[i];
    
                    // Loop over the files and see if it belongs to this directory
                    for (String filename : files.keySet())
                    {
                        if (getPath(filename).equals(dir)) // yep, add this one
                        {
                            Properties properties = files.get(filename);
                            ArrayList<Object> entryObjects = new ArrayList<Object>(properties.keySet());
                            ArrayList<String> entries = new ArrayList<String>();
                            for (Object object : entryObjects) {
                              entries.add((String)object);
                            }
                            Collections.sort(entries);
                            
                            for (int e=0;e<entries.size();e++)
                            {
                                String entry = entries.get(e);
                                String value = properties.getProperty(entry, "");
                                String locale = getLocale(filename);
                                String classname = getClassname(entry);
                                String key = entry.length()>classname.length() ? entry.substring(classname.length()+1) : entry;
                                boolean systemKey = entry.startsWith(SYSTEM_KEY_PREFIX); 
                                String fileContent = "";
                                
                                if (wVerify.getSelection()) // check existance of keys in java files...
                                {
                                    if (systemKey)
                                    {
                                        fileContent = "";
                                    }
                                    else
                                    {
                                        String javaFile = dir+"/"+classname+".java";
                                        fileContent = javaFiles.get(javaFile);
                                        if (fileContent==null)
                                        {
                                            fileContent = loadJava(javaFile, filename, entry);
                                            javaFiles.put(javaFile, fileContent);
                                        }
                                    }
                                    
                                }
                                
                                // Is this a selected locale?
                                boolean localeSelected = (locales.get(locale)).booleanValue();
                                
                                if (localeSelected)
                                {
                                    String used = "?";
                                    if (systemKey)
                                    {
                                        used = "System key";
                                    }
                                    else
                                    {
                                        if (wVerify.getSelection())
                                        {
                                            String keyString = "\""+entry+"\"";
                                            
                                            used = fileContent.indexOf(keyString)>=0 ? "Y" : "N";
                                        }
                                    }
                                    
                                    boolean notUsed = "N".equalsIgnoreCase(used);
                                    
                                    if (key.equalsIgnoreCase("Log.FinishedProcessing"))
                                    {
                                        System.out.println("Debug!");
                                    }

                                    String available = checkAvailability(dir, entry, locale, languages);
                                    
                                    if (!wUsed.getSelection() || notUsed)
                                    {
                                        // Add a new line to the grid
                                        //
                                        TableItem item = new TableItem(wGrid.table, SWT.NONE);
                                        int pos=1;
                                        item.setText(pos++, locale);
                                        item.setText(pos++, dir);
                                        item.setText(pos++, classname);
                                        item.setText(pos++, key);
                                        item.setText(pos++, value);
                                        item.setText(pos++, used);
                                        item.setText(pos++, available);
                                        
                                        if (notUsed) item.setBackground(unusedColor);
                                    }
                                }
                            }
                        }
                    }
                }
                
            }
        }
        catch(Exception e)
        {
            new ErrorDialog(shell, "Error loading data", "There was an unexpected error loading data for the translation grid", e);
        }
        finally
        {
            shell.setCursor(null);
        }
      
        // See if the grid is not empty: most platforms don't support this...
        if (wGrid.table.getItemCount()==0) new TableItem(wGrid.table, SWT.NONE);
        
        wGrid.removeEmptyRows();
        wGrid.setRowNums();
        wGrid.optWidth(true);
        
        // limit width of column 5:
        TableColumn col = wGrid.table.getColumn(5);
        if (col.getWidth()>200) col.setWidth(200);
    }

 
    private String checkAvailability(String dir, String entry, String locale, String[] languages)
    {
        String available = "Not checked";
        
        // Lookup the key in other languages
        boolean first = true;
        for (int x=0;x<languages.length;x++)
        {
            // The properties file:
            String propfile = ROOT + "/" + dir + "/" + MESSAGES_DIR + "/" + MESSAGES_PREFIX + "_" + languages[x] + EXTENSION;
            String add=null;
            Properties p = files.get(propfile);
            if (p==null)
            {
                add=languages[x]+" : missing file";
            }
            else
            {
                // OK, is the key present?
                String pkey = p.getProperty(entry);
                if (pkey==null)
                {
                    add=languages[x]+" : missing key";
                }
            }
            
            if (add!=null)
            {
                if (first)
                {
                    available=add;
                    first=false;
                }
                else
                {
                    available+=", "+add;
                }
            }
        }
        
        if (first)
        {
            available="All keys are present in the selected "+languages.length+" locale: ";
            for (int a=0;a<languages.length;a++) if (a==0) available+=languages[a]; else available+=", "+languages[a];
        }
        return available;
    }

    private String loadJava(String javaFile, String propertiesFilename, String entry) throws KettleFileException
    {
        if (Const.isEmpty(entry)) return "";
        
        try
        {
            String filename = ROOT+"/"+javaFile;
            StringBuffer content = new StringBuffer(5000);
            FileInputStream stream = new FileInputStream(filename);
    
            try
            {
                int c = 0;
                while ((c = stream.read()) != -1)
                {
                    content.append((char) c);
                }
            }
            finally
            {
                stream.close();
            }
    
            return content.toString();
        }
        catch(Exception e)
        {
            throw new KettleFileException(propertiesFilename+": Unable to load file ["+javaFile+"] for key ["+entry+"]", e);
        }
    }

    public String getClassname(String key)
    {
        int idxDot = key.indexOf('.');
        if (idxDot<0) return "";
        return key.substring(0, idxDot);
    }

    public String getLocale(String filename)
    {
        // src/be/ibridge/kettle/i18n/messages/messages_nl_NL.properties
        int idx = filename.length() - MESSAGES_DIR.length() - 3 - LOCALE_LENGTH;
        return filename.substring(idx, idx+LOCALE_LENGTH);
    }

    /**
     * Get the path until the first occurrence of "/messages/"
     * @param entry 
     * @return
     */
    private String getPath(String entry)
    {
        String retval = entry;
        
        int idxRoot = retval.indexOf(ROOT);
        if (idxRoot>=0)
        {
            retval = retval.substring(ROOT.length()+1);
        }
        
        int idxMess = retval.indexOf("/"+MESSAGES_DIR+"/");
        if (idxMess>=0)
        {
            retval = retval.substring(0, idxMess);
        }
        
        return retval;
    }
    
    public String[] getAvailableLocale()
    {
        Set<String> set = locales.keySet();
        return set.toArray(new String[set.size()]);
    }
    
    public String[] getSelectedLocale()
    {
        ArrayList<String> selection = new ArrayList<String>();
        String locs[] = getAvailableLocale();
        for (int i=0;i<locs.length;i++)
        {
            if ( (locales.get(locs[i])).booleanValue())
            {
                selection.add(locs[i]);
            }
        }
        return selection.toArray(new String[selection.size()]);
    }
    
    public void selectLocales()
    {
        String available[] = getAvailableLocale();
        EnterListDialog eld = new EnterListDialog(shell, SWT.NONE, available);
        String[] selection = eld.open();
        if (selection!=null)
        {
            for (int i=0;i<available.length;i++) locales.put(available[i], Boolean.FALSE);
            for (int i=0;i<selection.length;i++) locales.put(selection[i], Boolean.TRUE);
        }
    }

    public String toString()
    {
        return APP_NAME;
    }
    
    public static void main(String[] args)
    {
        Display display = new Display();
        LogChannelInterface log = new LogChannel(APP_NAME);
        PropsUI.init(display, Props.TYPE_PROPERTIES_SPOON);
        
        Translator translator = new Translator(display);
        translator.open();
        
        try
        {
            while (!display.isDisposed ()) 
            {
                if (!display.readAndDispatch()) display.sleep ();
            }
        }
        catch(Throwable e)
        {
            log.logError("An unexpected error occurred : "+e.getMessage());
            log.logError(Const.getStackTracker(e));
        }
    }



}
