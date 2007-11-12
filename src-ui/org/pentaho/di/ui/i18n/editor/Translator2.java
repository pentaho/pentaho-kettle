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
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterStringDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.i18n.KeyOccurrence;
import org.pentaho.di.ui.i18n.MessagesSourceCrawler;
import org.pentaho.di.ui.i18n.MessagesStore;
import org.pentaho.di.ui.i18n.TranslationsStore;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.w3c.dom.Document;
import org.w3c.dom.Node;



public class Translator2
{
    public static final String APP_NAME = "Pentaho Translator";
    public static final String[] ROOT = new String[] { "src", "src-ui", };
    public static final String SYSTEM_KEY_PREFIX = "System";
    public static final String REFERENCE_LOCALE = "en_US";
    public static final String[] FILES_TO_AVOID = new String[] { 
			"MessagesSourceCrawler.java", "KeyOccurence.java", "TransLator.java", 
			"MenuHelper.java", "Messages.java", "XulMessages.java", 
			"AnnotatedStepsConfigManager.java", "AnnotatedJobConfigManager.java", 
			"JobEntryValidatorUtils.java", "Const.java", "XulHelper.java", 
		  };
    
    private Display display;
    private Shell shell;
    private LogWriter log;
    private PropsUI props;
    
    /** The crawler that can find and contain all the keys in the source code */ 
    private MessagesSourceCrawler crawler;
    
    /** The translations store containing all the translations for all keys, locale, packages */
    private TranslationsStore store;
    
    /** derived from the crawler */
    private java.util.List<String> messagesPackages;
    
    private SashForm sashform;
    private List wLocale;
    private TableView wPackages;
    private List wTodo;
    
    private String selectedLocale;
    private String selectedMessagesPackage;
    
    private Text wKey;
    private Text wMain;
    private Text wValue;
    private Text wSource;

    private Button wReload;
    private Button wClose;
    private Button wApply;
    private Button wRevert;
    private Button wSave;

    private Button wSearch;
    private Button wNext;
    
    private Button wSearchV;
    private Button wNextV;
    
    /*
    private Button wSearchG;
    private Button wNextG;
    */
    
    private Button wAll;
    
	private ArrayList<String> localeList;
	protected String lastValue;
	protected boolean lastValueChanged;
	protected String selectedKey;
	protected String searchString;
	protected String lastFoundKey;
		
    public Translator2(Display display)
    {
        this.display = display;
        this.log = LogWriter.getInstance();
        this.props = PropsUI.getInstance();
    }
    
    public boolean showKey(String key, String messagesPackage) {
    	return !key.startsWith("System.") || messagesPackage.equals(BaseMessages.class.getPackage().getName());
    }
    
    public void readFiles(String[] directories) throws KettleFileException
    {
        log.logBasic(toString(), "Scanning source directories and Java source files for i18n keys...");
        try
        {
        	// crawl through the source directories...
        	//
        	crawler = new MessagesSourceCrawler(directories);
        	crawler.setFilesToAvoid(FILES_TO_AVOID);
        	crawler.setXulDirectories( new String[] { "ui", } );
        	crawler.crawl();
        	
        	// get the packages...
        	//
        	messagesPackages = crawler.getMessagesPackagesList();
        	store = new TranslationsStore(localeList, messagesPackages, REFERENCE_LOCALE); // en_US : main locale
        	store.read(directories);
        	
        	// What are the statistics?
        	//
        	int nrKeys = 0;
        	
        	int keyCounts[] = new int[localeList.size()];
        	for (int i=0;i<localeList.size();i++) {
        		String locale = localeList.get(i);
        		
        		// Count the number of keys available in that locale...
        		//
        		keyCounts[i]=0;
        		for (KeyOccurrence keyOccurrence : crawler.getOccurrences()) {
        			// We don't want the system keys, just the regular ones.
        			//
        			if (showKey(keyOccurrence.getKey(), keyOccurrence.getMessagesPackage())) {
            			String value = store.lookupKeyValue(locale, keyOccurrence.getMessagesPackage(), keyOccurrence.getKey());
	        			if (!Const.isEmpty(value)) {
	        				keyCounts[i]++;
	        			}
	        			if (locale.equals(REFERENCE_LOCALE)) {
	        				nrKeys++;
	        			}
        			}
        		}
        	}
        	
        	DecimalFormat df = new DecimalFormat("##0.00");
        	
        	double donePct[] = new double[localeList.size()];
        	System.out.println("Number of keys found : "+nrKeys);
        	for (int i=0;i<localeList.size();i++) {
        		donePct[i] = 100 * (double)keyCounts[i] / (double)nrKeys;
            	System.out.println(localeList.get(i)+" : "+df.format(donePct[i])+"% complete  ("+keyCounts[i]+")");
        	}
        	
        }
        catch(Exception e)
        {
            throw new KettleFileException("Unable to get all files from directory ["+ROOT+"]", e);
        }
    }
    
    public void setLocaleList() {
    	// What are the locale to handle?
    	//
    	localeList = new ArrayList<String>();
    	localeList.add("en_US");
    	localeList.add("fr_FR");
    	localeList.add("es_ES");
    	localeList.add("nl_NL");
    	localeList.add("de_DE");
    	localeList.add("zh_CN");
    	localeList.add("pt_BR");
    	localeList.add("pt_PT");
    	localeList.add("es_AR");
    	localeList.add("no_NO");
    	
    	File file = new File("translator.xml");
    	if (file.exists()) {
    		
    		// TODO: add the other configurations that are now hard coded to the XML document as well.
    		
    		try {
    			Document doc = XMLHandler.loadXMLFile(file);
    			Node configNode = XMLHandler.getSubNode(doc, "translator-config");
    			Node localeListNode = XMLHandler.getSubNode(configNode, "locale-list");
    			int nrLocale = XMLHandler.countNodes(localeListNode, "locale");
    			if (nrLocale>0) localeList.clear();
    			for (int i=0;i<nrLocale;i++) {
    				Node localeNode = XMLHandler.getSubNodeByNr(localeListNode, "locale", i);
    				String locale = XMLHandler.getTagValue(localeNode, "code");
    				localeList.add(locale);
    			}
    		}
    		catch (Exception e) {
				log.logError("Translator", "Error reading translator.xml", e);
			}
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
        sashform.setLayout(new FormLayout());
        props.setLook(sashform);
        
        addLists();
        addGrid();
        addListeners();
        
        sashform.setWeights(new int[] { 20, 80 });
        sashform.setVisible(true);
        
        shell.pack();
        
        refresh();
        
		shell.setSize(1024, 768);

        shell.open();
    }
    
    private void addListeners()
    {
        // In case someone dares to press the [X] in the corner ;-)
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { e.doit=quitFile(); } } );
                
        wReload.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent arg0)
                {
                    int idx[] = wPackages.table.getSelectionIndices();
                    reload();
                    wPackages.table.setSelection(idx);
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
      
    }

    public void reload()
    {
        try
        {
            shell.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));
            readFiles(ROOT);
            shell.setCursor(null);

            refresh();
        }
        catch(Exception e)
        {
            new ErrorDialog(shell, "Error loading data", "There was an unexpected error re-loading the data", e);
        }
    }

    public boolean quitFile()
    {
		java.util.List<MessagesStore> changedMessagesStores = store.getChangedMessagesStores();
		if (changedMessagesStores.size()>0) {
			MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_WARNING);
            mb.setMessage("There are unsaved changes."+Const.CR+Const.CR+"The number of changed messages files is: "+changedMessagesStores.size()+Const.CR+Const.CR+"Are you sure you want to exit?");
			mb.setText("Warning!");
			int answer = mb.open();
			if (answer==SWT.NO) return false;
		}

        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        props.saveProps();

        shell.dispose();
        display.dispose();
        return true;
    }

    private void addLists()
    {
        Composite composite = new Composite(sashform, SWT.NONE);
        props.setLook(composite);
        FormLayout formLayout = new FormLayout();
        formLayout.marginHeight = Const.FORM_MARGIN;
        formLayout.marginWidth = Const.FORM_MARGIN;
        composite.setLayout(formLayout);
        
        wLocale = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        FormData fdLocale = new FormData();
        fdLocale.left  = new FormAttachment(0, 0);
        fdLocale.right = new FormAttachment(100, 0);
        fdLocale.top= new FormAttachment(0, 0);
        fdLocale.bottom= new FormAttachment(20, 0);
        wLocale.setLayoutData(fdLocale);
        
        ColumnInfo[] colinfo=new ColumnInfo[]
	        {
	            new ColumnInfo("Packagename", ColumnInfo.COLUMN_TYPE_TEXT, false, true),
	        };

        
	    wPackages = new TableView(new Variables(), composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, 1, true, null, props);
        FormData fdPackages = new FormData();
        fdPackages.left  = new FormAttachment(0, 0);
        fdPackages.right = new FormAttachment(100, 0);
        fdPackages.top= new FormAttachment(wLocale, Const.MARGIN);
        fdPackages.bottom= new FormAttachment(100, 0);
        wPackages.setLayoutData(fdPackages);
        wPackages.setSortable(false);

        FormData fdComposite = new FormData();
        fdComposite.left  = new FormAttachment(0, 0);
        fdComposite.right = new FormAttachment(100, 0);
        fdComposite.top= new FormAttachment(0,0);
        fdComposite.bottom= new FormAttachment(100, 0);
        composite.setLayoutData(fdComposite);

        // Add a selection listener.
        wLocale.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { refreshGrid(); } } );
		        wPackages.table.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						refreshGrid();
					}
				}
	        );
        
        composite.layout();
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
        wSave= new Button(composite, SWT.NONE);
        wSave.setText("  &Save  ");
        wClose = new Button(composite, SWT.NONE);
        wClose.setText("  &Close  ");
        
        BaseStepDialog.positionBottomButtons(composite, new Button[] { wReload, wSave, wClose, } , Const.MARGIN*3, null);

        /*
        wSearchG = new Button(composite, SWT.PUSH);
        wSearchG.setText("   Search &key  ");
        FormData fdSearchG = new FormData();
        fdSearchG.left   = new FormAttachment(0, 0);
        fdSearchG.bottom = new FormAttachment(100, 0);
        wSearchG.setLayoutData(fdSearchG);
        
        wNextG = new Button(composite, SWT.PUSH);
        wNextG.setText("   Next ke&y  ");
        FormData fdNextG = new FormData();
        fdNextG.left   = new FormAttachment(wSearchG, Const.MARGIN);
        fdNextG.bottom = new FormAttachment(100, 0);
        wNextG.setLayoutData(fdNextG);
		*/
        
        int left = 25;
        int middle = 40;
        
        wAll = new Button(composite, SWT.CHECK);
        wAll.setText("Show all keys, not just the TODO list");
        props.setLook(wAll);
        FormData fdAll = new FormData();
        fdAll.left  = new FormAttachment(0, 0);
        fdAll.right = new FormAttachment(left, 0);
        fdAll.bottom= new FormAttachment(wClose, -Const.MARGIN);
        wAll.setLayoutData(fdAll);
        
        Label wlTodo = new Label(composite, SWT.LEFT);
        props.setLook(wlTodo);
        wlTodo.setText("Todo list:");
        FormData fdlTodo = new FormData();
        fdlTodo.left  = new FormAttachment(0, 0);
        fdlTodo.right = new FormAttachment(left, 0);
        fdlTodo.top= new FormAttachment(0, 0);
        wlTodo.setLayoutData(fdlTodo);
        
        wTodo = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        FormData fdTodo = new FormData();
        fdTodo.left  = new FormAttachment(0, 0);
        fdTodo.right = new FormAttachment(left, 0);
        fdTodo.top= new FormAttachment(wlTodo, Const.MARGIN);
        fdTodo.bottom= new FormAttachment(wAll, -Const.MARGIN);
        wTodo.setLayoutData(fdTodo);

        // The key
        //
        Label wlKey = new Label(composite, SWT.RIGHT);
        wlKey.setText("Translation key: ");
        props.setLook(wlKey);
        FormData fdlKey = new FormData();
        fdlKey.left  = new FormAttachment(left, Const.MARGIN);
        fdlKey.right = new FormAttachment(middle, 0);
        fdlKey.top= new FormAttachment(wlTodo, Const.MARGIN);
        wlKey.setLayoutData(fdlKey);

        wKey = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        props.setLook(wKey);
        FormData fdKey = new FormData();
        fdKey.left  = new FormAttachment(middle, Const.MARGIN);
        fdKey.right = new FormAttachment(100, 0);
        fdKey.top= new FormAttachment(wlTodo, Const.MARGIN);
        wKey.setLayoutData(fdKey);
        wKey.setEditable(false);

        // The Main translation
        //
        Label wlMain = new Label(composite, SWT.RIGHT);
        wlMain.setText("Main translation: ");
        props.setLook(wlMain);
        FormData fdlMain = new FormData();
        fdlMain.left  = new FormAttachment(left, Const.MARGIN);
        fdlMain.right = new FormAttachment(middle, 0);
        fdlMain.top= new FormAttachment(wKey, Const.MARGIN);
        wlMain.setLayoutData(fdlMain);

        wMain = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        props.setLook(wMain);
        FormData fdMain = new FormData();
        fdMain.left  = new FormAttachment(middle, Const.MARGIN);
        fdMain.right = new FormAttachment(100, 0);
        fdMain.top= new FormAttachment(wKey, Const.MARGIN);
        fdMain.bottom= new FormAttachment(wKey, 150+Const.MARGIN);
        wMain.setLayoutData(fdMain);
        wMain.setEditable(false);

        wSearch = new Button(composite, SWT.PUSH);
        wSearch.setText("   Search   ");
        FormData fdSearch = new FormData();
        fdSearch.right  = new FormAttachment(middle, -Const.MARGIN*2);
        fdSearch.top    = new FormAttachment(wMain, 0, SWT.CENTER);
        wSearch.setLayoutData(fdSearch);
        
        wNext = new Button(composite, SWT.PUSH);
        wNext.setText("   Next   ");
        FormData fdNext = new FormData();
        fdNext.right  = new FormAttachment(middle, -Const.MARGIN*2);
        fdNext.top    = new FormAttachment(wSearch, Const.MARGIN*2);
        wNext.setLayoutData(fdNext);
        
        // A few lines of source code at the bottom...
        //
        Label wlSource = new Label(composite, SWT.RIGHT);
        wlSource.setText("Line of source code : ");
        props.setLook(wlSource);
        FormData fdlSource = new FormData();
        fdlSource.left  = new FormAttachment(left, Const.MARGIN);
        fdlSource.right = new FormAttachment(middle, 0);
        fdlSource.top   = new FormAttachment(wClose, -100-Const.MARGIN);
        wlSource.setLayoutData(fdlSource);

        wSource = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        props.setLook(wSource);
        FormData fdSource = new FormData();
        fdSource.left   = new FormAttachment(middle, Const.MARGIN);
        fdSource.right  = new FormAttachment(100, 0);
        fdSource.top    = new FormAttachment(wClose, -100-Const.MARGIN);
        fdSource.bottom = new FormAttachment(wClose, -Const.MARGIN);
        wSource.setLayoutData(fdSource);
        wSource.setEditable(false);
        
        // The translation
        //
        Label wlValue = new Label(composite, SWT.RIGHT);
        wlValue.setText("Translation: ");
        props.setLook(wlValue);
        FormData fdlValue = new FormData();
        fdlValue.left  = new FormAttachment(left, Const.MARGIN);
        fdlValue.right = new FormAttachment(middle, 0);
        fdlValue.top   = new FormAttachment(wMain, Const.MARGIN);
        wlValue.setLayoutData(fdlValue);

        wValue = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
        props.setLook(wValue);
        FormData fdValue = new FormData();
        fdValue.left   = new FormAttachment(middle, Const.MARGIN);
        fdValue.right  = new FormAttachment(100, 0);
        fdValue.top    = new FormAttachment(wMain, Const.MARGIN);
        fdValue.bottom = new FormAttachment(wSource, -Const.MARGIN);
        wValue.setLayoutData(fdValue);
        wValue.setEditable(true);
        
        wApply = new Button(composite, SWT.PUSH);
        wApply.setText("   &Apply   ");
        FormData fdApply = new FormData();
        fdApply.right  = new FormAttachment(middle, -Const.MARGIN*2);
        fdApply.top    = new FormAttachment(wValue, 0, SWT.CENTER);
        wApply.setLayoutData(fdApply);
    	wApply.setEnabled(false);

    	wRevert = new Button(composite, SWT.PUSH);
        wRevert.setText("  &Revert   ");
        FormData fdRevert = new FormData();
        fdRevert.right  = new FormAttachment(middle, -Const.MARGIN*2);
        fdRevert.top    = new FormAttachment(wApply, Const.MARGIN*2);
        wRevert.setLayoutData(fdRevert);
    	wRevert.setEnabled(false);
    	
        wSearchV = new Button(composite, SWT.PUSH);
        wSearchV.setText("   Search   ");
        FormData fdSearchV = new FormData();
        fdSearchV.right  = new FormAttachment(middle, -Const.MARGIN*2);
        fdSearchV.top    = new FormAttachment(wRevert, Const.MARGIN*4);
        wSearchV.setLayoutData(fdSearchV);
        
        wNextV = new Button(composite, SWT.PUSH);
        wNextV.setText("   Next   ");
        FormData fdNextV = new FormData();
        fdNextV.right  = new FormAttachment(middle, -Const.MARGIN*2);
        fdNextV.top    = new FormAttachment(wSearchV, Const.MARGIN*2);
        wNextV.setLayoutData(fdNextV);

        

    	wAll.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					refreshGrid();
				}
			}
    	);
    	
		wTodo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// If someone clicks on the todo list, we set the appropriate values
					//
					if (wTodo.getSelectionCount()==1) {

						String key = wTodo.getSelection()[0]; 

						showKeySelection(key);
					}
				}
			}
		);
		
		wValue.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					// The main value got changed...
					// Capture this automatically 
					//
					lastValueChanged = true;
					lastValue = wValue.getText();
					
					wApply.setEnabled(true);
					wRevert.setEnabled(true);
				}
			}
		);
		
		wApply.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					applyChangedValue();
				}
			}
		);
		
		wRevert.addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					revertChangedValue();
				}
			}
		);
		
		wSave.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					saveFiles();
				}
			}
		);

		wSearch.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					search(REFERENCE_LOCALE);
				}
			}
		);

		wNext.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					searchAgain(REFERENCE_LOCALE);
				}
			}
		);
			
		wSearchV.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					search(selectedLocale);
				}
			}
		);

		wNextV.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					searchAgain(selectedLocale);
				}
			}
		);

     }
    
    protected void saveFiles() {
		java.util.List<MessagesStore> changedMessagesStores = store.getChangedMessagesStores();
		if (changedMessagesStores.size()>0) {
				
			StringBuffer msg = new StringBuffer();
			for (MessagesStore messagesStore : changedMessagesStores) {
				// Find the main locale variation for this messages store...
				//
				MessagesStore mainLocaleMessagesStore = store.findMainLocaleMessagesStore(messagesStore.getMessagesPackage());
				String sourceDirectory = mainLocaleMessagesStore.getSourceDirectory(ROOT);
				String filename = messagesStore.getSaveFilename(sourceDirectory);
				messagesStore.setFilename(filename);
				msg.append(filename).append(Const.CR);
			}
			
			EnterTextDialog dialog = new EnterTextDialog(shell, "Changed files", "Below are the changed messages files.  Select 'OK' to save these files to disk.", msg.toString());
			if (dialog.open()!=null)
			{
				try
				{
					for (MessagesStore messagesStore : changedMessagesStores) {
						messagesStore.write();
						LogWriter.getInstance().logBasic(toString(), "Saved messages file : "+messagesStore.getFilename());	
					}
				}
				catch(KettleException e) {
					new ErrorDialog(shell, "Error", "There was an error saving the changed messages files:", e);
				}
			}
			
		}
		else {
			// Nothing was saved.
			// TODO: disable the button if nothing changed.
			
		}

	}

	protected void search(String searchLocale) {
		// Ask for the search string...
		//
		EnterStringDialog dialog = new EnterStringDialog(shell, Const.NVL(searchString, ""), "Search key", "Search the translated '"+searchLocale+"' strings in this package");
		searchString = dialog.open();
		
		lastFoundKey = null;
		
		searchAgain(searchLocale);
	}

	protected void searchAgain(String searchLocale) {
		if (searchString!=null) {
			
			// We want to search for key in the list here...
			// That means we'll enter a String to search for in the values
			//
			
			String upperSearchString = searchString.toUpperCase();
			
			boolean lastKeyFound = lastFoundKey==null;
			
			// Search through all the main locale messages stores for the selected package
			//
			java.util.List<MessagesStore> mainLocaleMessagesStores = store.getMessagesStores(searchLocale, selectedMessagesPackage);
			for (MessagesStore messagesStore : mainLocaleMessagesStores) {
				for (String key : messagesStore.getMessagesMap().keySet()) {
					String value = messagesStore.getMessagesMap().get(key);
					String upperValue = value.toUpperCase();
					if (upperValue.indexOf(upperSearchString)>=0) {
						// OK, we found a key worthy of our attention...
						//
						if (lastKeyFound) {
							int index = wTodo.indexOf(key);
							if (index>=0) {
								lastFoundKey = key;
								wTodo.setSelection(index);
								showKeySelection(wTodo.getSelection()[0]);
								return;
							}
						}
						if (key.equals(lastFoundKey)) {
							lastKeyFound=true;
						}
					}
				}
			}
		}

	}

	protected void showKeySelection(String key) {
		if (!key.equals(selectedKey)) {
			
			applyChangedValue();
		}
			
		String mainLocale = REFERENCE_LOCALE;
		
		if (selectedLocale!=null && key!=null && selectedMessagesPackage!=null) {
			String mainValue = store.lookupKeyValue(mainLocale, selectedMessagesPackage, key);
			String value = store.lookupKeyValue(selectedLocale, selectedMessagesPackage, key);
			KeyOccurrence keyOccurrence = crawler.getKeyOccurrence(key, selectedMessagesPackage);
			
			wKey.setText(key);
			wMain.setText(Const.NVL(mainValue, ""));
			wValue.setText(Const.NVL(value, ""));
			wSource.setText(keyOccurrence.getSourceLine());
			
			// Focus on the entry field
			// Put the cursor all the way at the back
			//
			wValue.setFocus();
			wValue.setSelection(wValue.getText().length());
			wValue.showSelection();
			wValue.clearSelection();
			
			selectedKey = key;
			lastValueChanged=false;
			wApply.setEnabled(false);
			wRevert.setEnabled(false);
		}
	}

	public void refreshGrid() {
    	
    	applyChangedValue();
    	
    	wTodo.removeAll();
    	wKey.setText("");
    	wMain.setText("");
    	wValue.setText("");
    	wSource.setText("");
    	
    	selectedLocale = wLocale.getSelectionCount()==0 ? null : wLocale.getSelection()[0];
    	selectedMessagesPackage = wPackages.table.getSelectionCount()==0 ? null : wPackages.table.getSelection()[0].getText(1);
    	refreshPackages();
    	
    	// Only continue with a locale & a messages package, otherwise we won't budge ;-)
		//
    	if (selectedLocale!=null && selectedMessagesPackage!=null) {
    		// Get the list of keys that need a translation...
    		//
    		java.util.List<KeyOccurrence> todo = getTodoList(selectedLocale, selectedMessagesPackage, false);
    		String[] todoItems = new String[todo.size()];
    		for (int i=0;i<todoItems.length;i++) todoItems[i] = todo.get(i).getKey();
    		wTodo.setItems(todoItems);
		}
    }
	
	private java.util.List<KeyOccurrence> getTodoList(String locale, String messagesPackage, boolean strict) {
		// Get the list of keys that need a translation...
		//
		java.util.List<KeyOccurrence> keys = crawler.getOccurrencesForPackage(messagesPackage);
		java.util.List<KeyOccurrence> todo = new ArrayList<KeyOccurrence>();
		for (KeyOccurrence keyOccurrence : keys) {
			// Avoid the System keys.  Those are taken care off in a different package
			//
			if (showKey(keyOccurrence.getKey(), keyOccurrence.getMessagesPackage())) { 
    			String value = store.lookupKeyValue(locale, messagesPackage, keyOccurrence.getKey());
    			if ( Const.isEmpty(value) || ( wAll.getSelection() && !strict) ) { 
    				todo.add(keyOccurrence);
    			}
			}
		}

		return todo;
	}


    private void applyChangedValue() {
    	// Hang on, before we clear it all, did we have a previous value?
    	//
    	int todoIndex = wTodo.getSelectionIndex();
    	
    	if (selectedKey!=null && selectedLocale!=null && selectedMessagesPackage!=null && lastValueChanged) {
    		// Store the last modified value
    		//
    		if (!Const.isEmpty(lastValue)) {
    			store.storeValue(selectedLocale, selectedMessagesPackage, selectedKey, lastValue);
        		lastValueChanged = false;

    			if (!wAll.getSelection()) {
					wTodo.remove(selectedKey);
					if (wTodo.getSelectionIndex()<0) {
						// Select the next one in the list...
						if (todoIndex>wTodo.getItemCount()) todoIndex=wTodo.getItemCount()-1;
						
						if (todoIndex>=0 && todoIndex<wTodo.getItemCount()) {
							wTodo.setSelection(todoIndex);
							showKeySelection(wTodo.getSelection()[0]);
						} else {
							refreshGrid();
						}
					}
    			}
    		}
        	lastValue = null;
        	wApply.setEnabled(false);
        	wRevert.setEnabled(false);
    	}
	}
    
    private void revertChangedValue() {
    	lastValueChanged = false;
    	refreshGrid();
	}


	public void refresh()
    {
    	refreshLocale();
        refreshPackages();
        refreshGrid();
    }
    
    public void refreshPackages()
    {
    	int index = wPackages.getSelectionIndex();
    	
        // OK, we have a distinct list of packages to work with...
    	wPackages.table.removeAll();
        for (int i=0;i<messagesPackages.size();i++) {
        	String messagesPackage = messagesPackages.get(i);
        	TableItem item = new TableItem(wPackages.table, SWT.NONE);
        	item.setText(1, messagesPackage);
        	
        	// count the number of keys for the package that are NOT yet translated...
        	//
        	if (selectedLocale!=null) {
	        	java.util.List<KeyOccurrence> todo = getTodoList(selectedLocale, messagesPackage, true);
	        	if (todo.size()>50) {
	        		item.setBackground(GUIResource.getInstance().getColorRed());
	        	} else if (todo.size()>25) {
	        		item.setBackground(GUIResource.getInstance().getColorOrange());
	        	} else if (todo.size()>10) {
	        		item.setBackground(GUIResource.getInstance().getColorYellow());
	        	} else if (todo.size()>5) {
	        		item.setBackground(GUIResource.getInstance().getColorGray());
	        	} else if (todo.size()>0) {
	        		item.setBackground(GUIResource.getInstance().getColorLightGray());
	        	}
        	}
        }
        if (messagesPackages.size()==0) {
        	new TableItem(wPackages.table, SWT.NONE);
        } else {
        	wPackages.setRowNums();
        	wPackages.optWidth(true);
        }
        
        if (index>=0) {
        	wPackages.table.setSelection(index);
        	wPackages.table.showSelection();
        }
    }
    
    public void refreshLocale()
    {
        // OK, we have a distinct list of locale to work with...
        wLocale.removeAll();
        wLocale.setItems(localeList.toArray(new String[localeList.size()]));
    }


    public String toString()
    {
        return APP_NAME;
    }
    
    public static void main(String[] args)
    {
        Display display = new Display();
        LogWriter log = LogWriter.getInstance();
        PropsUI.init(display, Props.TYPE_PROPERTIES_SPOON);
        
        Translator2 translator = new Translator2(display);
        translator.setLocaleList();
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
            log.logError(APP_NAME, "An unexpected error occurred : "+e.getMessage());
            log.logError(APP_NAME, Const.getStackTracker(e));
        }
    }



}
