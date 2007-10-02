package org.pentaho.di.ui.i18n.editor;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterStringDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.i18n.KeyOccurrence;
import org.pentaho.di.ui.i18n.MessagesSourceCrawler;
import org.pentaho.di.ui.i18n.MessagesStore;
import org.pentaho.di.ui.i18n.TranslationsStore;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



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
    private List wPackages;
    private List wTodo;
    
    private String selectedLocale;
    private String selectedMessagesPackage;
    
    private Text wKey;
    private Text wMain;
    private Text wValue;

    private Button wReload;
    private Button wClose;
    private Button wApply;
    private Button wRevert;
    private Button wSave;

    private Button wSearch;
    private Button wNext;
    
    private Button wSearchV;
    private Button wNextV;
    
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
        
        sashform.setWeights(new int[] { 25, 75 });
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
                    int idx[] = wPackages.getSelectionIndices();
                    reload();
                    wPackages.setSelection(idx);
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
        
        wPackages = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        FormData fdPackages = new FormData();
        fdPackages.left  = new FormAttachment(0, 0);
        fdPackages.right = new FormAttachment(100, 0);
        fdPackages.top= new FormAttachment(wLocale, Const.MARGIN);
        fdPackages.bottom= new FormAttachment(100, 0);
        wPackages.setLayoutData(fdPackages);

        FormData fdComposite = new FormData();
        fdComposite.left  = new FormAttachment(0, 0);
        fdComposite.right = new FormAttachment(100, 0);
        fdComposite.top= new FormAttachment(0,0);
        fdComposite.bottom= new FormAttachment(100, 0);
        composite.setLayoutData(fdComposite);

        // Add a selection listener.
        wLocale.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { refreshGrid(); } } );
        wPackages.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { refreshGrid(); } } );
        
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

        int left = 35;
        int middle = 50;
        
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
        wSearch.setText("   &Search   ");
        FormData fdSearch = new FormData();
        fdSearch.right  = new FormAttachment(middle, -Const.MARGIN*2);
        fdSearch.top    = new FormAttachment(wMain, 0, SWT.CENTER);
        wSearch.setLayoutData(fdSearch);
        
        wNext = new Button(composite, SWT.PUSH);
        wNext.setText("   &Next   ");
        FormData fdNext = new FormData();
        fdNext.right  = new FormAttachment(middle, -Const.MARGIN*2);
        fdNext.top    = new FormAttachment(wSearch, Const.MARGIN*2);
        wNext.setLayoutData(fdNext);
        
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
        fdValue.bottom = new FormAttachment(wClose, -Const.MARGIN);
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
        wSearchV.setText("   &Search   ");
        FormData fdSearchV = new FormData();
        fdSearchV.right  = new FormAttachment(middle, -Const.MARGIN*2);
        fdSearchV.top    = new FormAttachment(wRevert, Const.MARGIN*4);
        wSearchV.setLayoutData(fdSearchV);
        
        wNextV = new Button(composite, SWT.PUSH);
        wNextV.setText("   &Next   ");
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

						handleKeySelection(key);
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
					java.util.List<MessagesStore> changedMessagesStores = store.getChangedMessagesStores();
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
								handleKeySelection(wTodo.getSelection()[0]);
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

	protected void handleKeySelection(String key) {
		if (!key.equals(selectedKey)) {
			
			applyChangedValue();
			
			String mainLocale = REFERENCE_LOCALE;
			
			if (selectedLocale!=null && key!=null && selectedMessagesPackage!=null) {
				String mainValue = store.lookupKeyValue(mainLocale, selectedMessagesPackage, key);
				String value = store.lookupKeyValue(selectedLocale, selectedMessagesPackage, key);
				wKey.setText(key);
				wMain.setText(Const.NVL(mainValue, ""));
				wValue.setText(Const.NVL(value, ""));
				
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
	}

	public void refreshGrid() {
    	
    	applyChangedValue();
    	
    	wTodo.removeAll();
    	wKey.setText("");
    	wMain.setText("");
    	wValue.setText("");
    	
    	selectedLocale = wLocale.getSelectionCount()==0 ? null : wLocale.getSelection()[0];
    	selectedMessagesPackage = wPackages.getSelectionCount()==0 ? null : wPackages.getSelection()[0];
    	
    	// Only continue with a locale & a messages package, otherwise we won't budge ;-)
		//
    	if (selectedLocale!=null && selectedMessagesPackage!=null) {
    		// Get the list of keys that need a translation...
    		//
    		java.util.List<KeyOccurrence> keys = crawler.getOccurrencesForPackage(selectedMessagesPackage);
    		java.util.List<KeyOccurrence> todo = new ArrayList<KeyOccurrence>();
    		for (KeyOccurrence keyOccurrence : keys) {
    			String value = store.lookupKeyValue(selectedLocale, selectedMessagesPackage, keyOccurrence.getKey());
    			if ((value==null || wAll.getSelection()) && !keyOccurrence.getKey().startsWith("System.")) { // Avoid the System keys.  Those are taken care off.
    				todo.add(keyOccurrence);
    			}
    		}
    		
    		String[] todoItems = new String[todo.size()];
    		for (int i=0;i<todoItems.length;i++) todoItems[i] = todo.get(i).getKey();
    		wTodo.setItems(todoItems);
		}
    }


    private void applyChangedValue() {
    	// Hang on, before we clear it all, did we have a previous value?
    	//
    	int todoIndex = wTodo.getSelectionIndex();
    	
    	if (selectedKey!=null && selectedLocale!=null && selectedMessagesPackage!=null && lastValueChanged) {
    		// Store the last modified value
    		//
    		if (Const.isEmpty(lastValue)) {
    			store.removeValue(selectedLocale, selectedMessagesPackage, selectedKey);
    		}
    		else {
    			store.storeValue(selectedLocale, selectedMessagesPackage, selectedKey, lastValue);
    			if (!wAll.getSelection()) {
					wTodo.remove(selectedKey);
					if (wTodo.getSelectionIndex()<0) {
						// Select the next one in the list...
						if (todoIndex>wTodo.getItemCount()) todoIndex=wTodo.getItemCount()-1;
						
						if (todoIndex>=0 && todoIndex<wTodo.getItemCount()) {
							wTodo.setSelection(todoIndex);
							selectedKey = null;
							handleKeySelection(wTodo.getSelection()[0]);
						}
					}
    			}
    		}
    		lastValueChanged = false;
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
        // OK, we have a distinct list of packages to work with...
        wPackages.removeAll();
        wPackages.setItems(messagesPackages.toArray(new String[messagesPackages.size()]));
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
