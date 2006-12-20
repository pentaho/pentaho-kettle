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
 
package be.ibridge.kettle.spoon;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.cluster.ClusterSchema;
import be.ibridge.kettle.cluster.SlaveServer;
import be.ibridge.kettle.cluster.dialog.ClusterSchemaDialog;
import be.ibridge.kettle.cluster.dialog.SlaveServerDialog;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.DBCache;
import be.ibridge.kettle.core.DragAndDropContainer;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.LastUsedFile;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.PrintSpool;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SharedObjectInterface;
import be.ibridge.kettle.core.SourceToTargetMapping;
import be.ibridge.kettle.core.TransAction;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLTransfer;
import be.ibridge.kettle.core.clipboard.ImageDataTransfer;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.CheckResultDialog;
import be.ibridge.kettle.core.dialog.DatabaseDialog;
import be.ibridge.kettle.core.dialog.DatabaseExplorerDialog;
import be.ibridge.kettle.core.dialog.EnterMappingDialog;
import be.ibridge.kettle.core.dialog.EnterOptionsDialog;
import be.ibridge.kettle.core.dialog.EnterSearchDialog;
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.dialog.EnterStringDialog;
import be.ibridge.kettle.core.dialog.EnterStringsDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.dialog.PreviewRowsDialog;
import be.ibridge.kettle.core.dialog.SQLEditor;
import be.ibridge.kettle.core.dialog.SQLStatementsDialog;
import be.ibridge.kettle.core.dialog.ShowBrowserDialog;
import be.ibridge.kettle.core.dialog.Splash;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.reflection.StringSearchResult;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TreeMemory;
import be.ibridge.kettle.core.wizards.createdatabase.CreateDatabaseWizard;
import be.ibridge.kettle.job.JobEntryLoader;
import be.ibridge.kettle.pan.CommandLineOption;
import be.ibridge.kettle.partition.PartitionSchema;
import be.ibridge.kettle.partition.dialog.PartitionSchemaDialog;
import be.ibridge.kettle.pkg.JarfileGenerator;
import be.ibridge.kettle.repository.PermissionMeta;
import be.ibridge.kettle.repository.RepositoriesMeta;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.RepositoryMeta;
import be.ibridge.kettle.repository.UserInfo;
import be.ibridge.kettle.repository.dialog.RepositoriesDialog;
import be.ibridge.kettle.repository.dialog.RepositoryExplorerDialog;
import be.ibridge.kettle.repository.dialog.SelectObjectDialog;
import be.ibridge.kettle.repository.dialog.UserDialog;
import be.ibridge.kettle.spoon.dialog.AnalyseImpactProgressDialog;
import be.ibridge.kettle.spoon.dialog.CheckTransProgressDialog;
import be.ibridge.kettle.spoon.dialog.GetSQLProgressDialog;
import be.ibridge.kettle.spoon.dialog.ShowCreditsDialog;
import be.ibridge.kettle.spoon.dialog.TipsDialog;
import be.ibridge.kettle.spoon.wizards.CopyTableWizardPage1;
import be.ibridge.kettle.spoon.wizards.CopyTableWizardPage2;
import be.ibridge.kettle.trans.DatabaseImpact;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.StepPlugin;
import be.ibridge.kettle.trans.TransConfiguration;
import be.ibridge.kettle.trans.TransExecutionConfiguration;
import be.ibridge.kettle.trans.TransHopMeta;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.cluster.TransSplitter;
import be.ibridge.kettle.trans.dialog.TransDialog;
import be.ibridge.kettle.trans.dialog.TransExecutionConfigurationDialog;
import be.ibridge.kettle.trans.dialog.TransHopDialog;
import be.ibridge.kettle.trans.dialog.TransLoadProgressDialog;
import be.ibridge.kettle.trans.dialog.TransSaveProgressDialog;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.StepPartitioningMeta;
import be.ibridge.kettle.trans.step.selectvalues.SelectValuesMeta;
import be.ibridge.kettle.trans.step.tableinput.TableInputMeta;
import be.ibridge.kettle.trans.step.tableoutput.TableOutputMeta;
import be.ibridge.kettle.version.BuildVersion;
import be.ibridge.kettle.www.AddTransServlet;
import be.ibridge.kettle.www.PrepareExecutionTransHandler;
import be.ibridge.kettle.www.StartExecutionTransHandler;
import be.ibridge.kettle.www.WebResult;

/**
 * This class handles the main window of the Spoon graphical transformation editor.
 * 
 * @author Matt
 * @since 16-may-2003
 * 
 * Add i18n support
 * import the package:be.ibridge.kettle.i18n.Messages
 * 
 * @since 07-Feb-2006
 */
public class Spoon
{
    public static final String APP_NAME = Messages.getString("Spoon.Application.Name");  //"Spoon";
    
    private LogWriter log;
    private Display disp;
    private Shell shell;
    private boolean destroy;
    
    private SashForm sashform;
    public  CTabFolder tabfolder;
    
    public boolean shift;
    public boolean control;

    public  Row variables;
    
    /**
     * These are the arguments that were given at Spoon launch time...
     */
    private String[] arguments;
    
    private boolean stopped;
    
    private Cursor cursor_hourglass, cursor_hand;
    
    public  Props props;
    
    public  Repository rep;
        
    /** 
     * This contains a map between the name of a transformation and the TransMeta object.
     * If the transformation has no name it will be mapped under a number [1], [2] etc. 
     */
    private Map transformationMap;
    
    /**
     * This contains a map between the name of the tab name and the object name and type 
     */
    private Map tabMap;
    
    /**
     * This contains a map with all the unnamed transformation (just a filename)
     */

    private ToolBar  tBar;

    private Menu     msFile;
    private MenuItem miFileSep3;
    private MenuItem miEditUndo, miEditRedo;
    
    private Tree selectionTree;
    private TreeItem  tiBase, tiPlug;

    private Tree pluginHistoryTree;    
    
    public static final String STRING_TRANSFORMATIONS = Messages.getString("Spoon.STRING_TRANSFORMATIONS"); // Transformations
    public static final String STRING_BUILDING_BLOCKS = Messages.getString("Spoon.STRING_BUILDING_BLOCKS"); // Building blocks
    public static final String STRING_ELEMENTS        = Messages.getString("Spoon.STRING_ELEMENTS");        // Model elements
    public static final String STRING_CONNECTIONS     = Messages.getString("Spoon.STRING_CONNECTIONS");     // Connections
    public static final String STRING_STEPS           = Messages.getString("Spoon.STRING_STEPS");           // Steps
    public static final String STRING_HOPS            = Messages.getString("Spoon.STRING_HOPS");            // Hops
    public static final String STRING_PARTITIONS      = Messages.getString("Spoon.STRING_PARTITIONS");      // Database Partition schemas
    public static final String STRING_SLAVES          = Messages.getString("Spoon.STRING_SLAVES");          // Slave servers
    public static final String STRING_CLUSTERS        = Messages.getString("Spoon.STRING_CLUSTERS");        // Cluster Schemas
    public static final String STRING_BASE            = Messages.getString("Spoon.STRING_BASE");            // Base step types
    public static final String STRING_PLUGIN          = Messages.getString("Spoon.STRING_PLUGIN");          // Plugin step types
    public static final String STRING_HISTORY         = Messages.getString("Spoon.STRING_HISTORY");         // Step creation history

    public static final String STRING_TRANS_NO_NAME = Messages.getString("Spoon.STRING_TRANS_NO_NAME"); // <unnamed transformation>

    private static final String APPL_TITLE         = APP_NAME;
            
    public  KeyAdapter defKeys;
    public  KeyAdapter modKeys;

    private Menu mBar;

    private Composite tabComp;

    private SashForm leftSash;

    private MenuItem miWizardCopyTable;

    private TransExecutionConfiguration executionConfiguration;

    private TreeItem tiTrans;

    private TreeItem tiBlocks;

    private Menu mCSH;

    private MenuItem miFileClose;

    private MenuItem miFileSave;

    private MenuItem miFileSaveAs;

    private MenuItem miFilePrint;

    private MenuItem miEditSelectAll;

    private MenuItem miEditUnselectAll;

    private MenuItem miEditCopy;

    private MenuItem miEditPaste;

    private MenuItem miTransRun;

    private MenuItem miTransPreview;

    private MenuItem miTransCheck;

    private MenuItem miTransImpact;

    private MenuItem miTransSQL;

    private MenuItem miLastImpact;

    private MenuItem miLastCheck;

    private MenuItem miLastPreview;

    private MenuItem miTransCopy;

    private MenuItem miTransPaste;

    private MenuItem miTransImage;

    private MenuItem miTransDetails;

    private MenuItem miWizardNewConnection;

    private MenuItem miRepDisconnect;

    private MenuItem miRepUser;

    private MenuItem miRepExplore;
        
    public Spoon(LogWriter l, Repository rep)
    {
        this(l, null, null, rep);
    }

    public Spoon(LogWriter l, Display d, Repository rep)
    {
        this(l, d, null, rep);
    }
    
    public Spoon(LogWriter log, Display d, TransMeta ti, Repository rep)
    {
        this.log        = log;
        this.rep = rep;
        
        if (d!=null) 
        {
            disp=d;
            destroy=false;
        } 
        else 
        {
            disp=new Display();
            destroy=true;
        } 
        shell=new Shell(disp);
        shell.setText(APPL_TITLE);
        FormLayout layout = new FormLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        shell.setLayout (layout);
        
        transformationMap = new Hashtable();
        tabMap = new Hashtable();
        
        // INIT Data structure
        if (ti!=null)
        {
            addTransformation(ti);
        }
        
        if (!Props.isInitialized()) 
        {
            //log.logDetailed(toString(), "Load properties for Spoon...");
            log.logDetailed(toString(),Messages.getString("Spoon.Log.LoadProperties"));
            Props.init(disp, Props.TYPE_PROPERTIES_SPOON);  // things to remember...
        }
        props=Props.getInstance();
        
        // Load settings in the props
        loadSettings();
        
        executionConfiguration = new TransExecutionConfiguration();
        
        // Clean out every time we start, auto-loading etc, is not a good idea
        // If they are needed that often, set them in the kettle.properties file
        //
        variables = new Row(); 
        
        // props.setLook(shell);
        
        shell.setImage(GUIResource.getInstance().getImageSpoon());
        
        cursor_hourglass = new Cursor(disp, SWT.CURSOR_WAIT);
        cursor_hand      = new Cursor(disp, SWT.CURSOR_HAND);
        
        // widgets = new WidgetContainer();
        
        defKeys = new KeyAdapter() 
            {
                public void keyPressed(KeyEvent e) 
                {
                    TransMeta transMeta = getActiveTransformation();
                    SpoonLog spoonLog = getActiveSpoonLog();
                    
                    boolean ctrl = (( e.stateMask&SWT.CONTROL)!=0);
                    boolean alt  = (( e.stateMask&SWT.ALT)!=0);
                    
                    // ESC --> Unselect All steps
                    if (e.keyCode == SWT.ESC && !ctrl && !alt)   {  if (transMeta!=null) { transMeta.unselectAll(); refreshGraph(); } };

                    // F3 --> createDatabaseWizard
                    if (e.keyCode == SWT.F3 && !ctrl && !alt)    { createDatabaseWizard(transMeta); }

                    // F4 --> copyTableWizard
                    if (e.keyCode == SWT.F4 && !ctrl && !alt)    { copyTableWizard(transMeta); }

                    // CTRL-F4 --> close active transformation
                    if (e.keyCode == SWT.F4 && ctrl && !alt)    { closeTransformation(transMeta); }

                    // F5 --> refresh
                    if (e.keyCode == SWT.F5 && !ctrl && !alt)    { refreshGraph(); refreshTree(); }
                    
                    // F6 --> show last impact analyses
                    if (e.keyCode == SWT.F6 && !ctrl && !alt)    { showLastImpactAnalyses(transMeta); }
                    
                    // F7 --> show last verify results
                    if (e.keyCode == SWT.F7 && !ctrl && !alt)    { showLastTransCheck(); }
                    
                    // F8 --> show last preview
                    if (e.keyCode == SWT.F8 && !ctrl && !alt)    { if (spoonLog!=null) { spoonLog.showPreview(); } }
                    
                    // F9 --> run
                    if (e.keyCode == SWT.F9 && !ctrl && !alt)    { executeTransformation(transMeta, true, false, false, false, null); }
                    
                    // F10 --> preview
                    if (e.keyCode == SWT.F10 && !ctrl && !alt)   { executeTransformation(transMeta, true, false, false, true, null);  }

                    // F11 --> Verify
                    if (e.keyCode == SWT.F11 && !ctrl && !alt) { checkTrans(transMeta); }

                    // CTRL-A --> Select All steps
                    if ((int)e.character ==  1 && ctrl && !alt) { if (transMeta!=null) { transMeta.selectAll(); refreshGraph(); } };
                    
                    // CTRL-D --> Disconnect from repository
                    if ((int)e.character ==  4 && ctrl && !alt) { closeRepository();  };
                    
                    // CTRL-E --> Explore the repository
                    if ((int)e.character ==  5 && ctrl && !alt) { exploreRepository(); };

                    // CTRL-F --> Java examination
                    if ((int)e.character ==  6 && ctrl && !alt ) { searchMetaData(); };

                    // CTRL-I --> Import from XML file         && (e.keyCode&SWT.CONTROL)!=0
                    if ((int)e.character ==  9 && ctrl && !alt ) { openFile(true);  };

                    // CTRL-ALT-I --> Copy Transformation Image to clipboard
                    if ((int)e.character ==  9 && ctrl && alt) { if (transMeta!=null) { copyTransformationImage(transMeta); } }

                    // CTRL-J --> Get variables
                    if ((int)e.character == 10 && ctrl && !alt ) { getVariables(); };

                    // CTRL-K --> Create Kettle archive
                    if ((int)e.character == 11 && ctrl && !alt ) { if (transMeta!=null) { createKettleArchive(transMeta); } };

                    // CTRL-L --> Show variables
                    if ((int)e.character == 12 && ctrl && !alt ) { showVariables(); };

                    // CTRL-N --> new
                    if ((int)e.character == 14 && ctrl && !alt) { newFile();  }
                        
                    // CTRL-O --> open
                    if ((int)e.character == 15 && ctrl && !alt) { openFile(false);  }
                    
                    // CTRL-P --> print
                    if ((int)e.character == 16 && ctrl && !alt) { printFile(transMeta);  }
                    
                    // CTRL-Q --> Impact analyses
                    if ((int)e.character == 17 && ctrl && !alt) { analyseImpact(transMeta);}
                    
                    // CTRL-R --> Connect to repository
                    if ((int)e.character == 18 && ctrl && !alt) { openRepository(); };

                    // CTRL-S --> save
                    if ((int)e.character == 19 && ctrl && !alt) { saveFile(transMeta);  }
                    
                    // CTRL-ALT-S --> send to slave server
                    if ((int)e.character == 19 && ctrl && alt) { executeTransformation(transMeta, false, true, false, false, null);  }

                    // CTRL-T --> transformation
                    if ((int)e.character == 20 && ctrl && !alt) { editTransformationProperties(transMeta);  }

                    // CTRL-U --> transformation
                    if ((int)e.character == 21 && ctrl && !alt) { executeTransformation(transMeta, false, false, true, false, null);  }

                    // CTRL-Y --> redo action
                    if ((int)e.character == 25 && ctrl && !alt) { redoAction(transMeta);  }
                    
                    // CTRL-Z --> undo action
                    if ((int)e.character == 26 && ctrl && !alt) { undoAction(transMeta);  }
                                        
                    // System.out.println("(int)e.character = "+(int)e.character+", keycode = "+e.keyCode+", stateMask="+e.stateMask);
                }
            };
        modKeys = new KeyAdapter() 
            {
                public void keyPressed(KeyEvent e) 
                {
                    shift = (e.keyCode == SWT.SHIFT  );
                    control = (e.keyCode == SWT.CONTROL);                    
                }

                public void keyReleased(KeyEvent e) 
                {
                    shift = (e.keyCode == SWT.SHIFT  );
                    control = (e.keyCode == SWT.CONTROL);                    
                }
            };

        
        addBar();

        FormData fdBar = new FormData();
        fdBar.left = new FormAttachment(0, 0);
        fdBar.top = new FormAttachment(0, 0);
        tBar.setLayoutData(fdBar);

        sashform = new SashForm(shell, SWT.HORIZONTAL);
        // props.setLook(sashform);
        
        FormData fdSash = new FormData();
        fdSash.left = new FormAttachment(0, 0);
        fdSash.top = new FormAttachment(tBar, 0);
        fdSash.bottom = new FormAttachment(100, 0);
        fdSash.right  = new FormAttachment(100, 0);
        sashform.setLayoutData(fdSash);

        addMenu();
        addTree();
        addTabs();
                
        // In case someone dares to press the [X] in the corner ;-)
        shell.addShellListener( 
            new ShellAdapter() 
            { 
                public void shellClosed(ShellEvent e) 
                { 
                    e.doit=quitFile(); 
                } 
            } 
        );

        shell.layout();
        
        // Set the shell size, based upon previous time...
        WindowProperty winprop = props.getScreen(APPL_TITLE);
        if (winprop!=null) winprop.setShell(shell); 
        else 
        {
            shell.pack();
            shell.setMaximized(true); // Default = maximized!
        }
    }



    /**
     * Add a transformation to the 
     * @param transMeta the transformation to add to the map
     * @return the key used to store the transformation in the map
     */
    public String addTransformation(TransMeta transMeta)
    {
        String key = makeGraphTabName(transMeta);

        if (transformationMap.get(key)==null)
        {
            transformationMap.put(key, transMeta);
        }
        else
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
            mb.setMessage(Messages.getString("Spoon.Dialog.TransAlreadyLoaded.Message")); // Transformation is already loaded
            mb.setText(Messages.getString("Spoon.Dialog.TransAlreadyLoaded.Title")); // Sorry!
            mb.open();
        }
        
        return key;
    }

    /**
     * @param transMeta the transformation to close, make sure it's ok to dispose of it BEFORE you call this.
     */
    public void closeTransformation(TransMeta transMeta)
    {
        transformationMap.remove(makeGraphTabName(transMeta));

        // Close the associated tabs...
        CTabItem graphTab = findCTabItem(makeGraphTabName(transMeta));
        if (graphTab!=null) graphTab.dispose();
        CTabItem logTab = findCTabItem(makeLogTabName(transMeta));
        if (logTab!=null) logTab.dispose();
        CTabItem historyTab = findCTabItem(makeHistoryTabName(transMeta));
        if (historyTab!=null) historyTab.dispose();
        
        refreshTree();
    }

    
    
    /**
     * Search the transformation meta-data.
     *
     */
    public void searchMetaData()
    {
        TransMeta[] transMetas = getLoadedTransformations();

        if (transMetas==null || transMetas.length==0) return;
        
        EnterSearchDialog esd = new EnterSearchDialog(shell);
        if (!esd.open())
        {
            return;
        }

        ArrayList rows = new ArrayList();
        for (int t=0;t<transMetas.length;t++)
        {
            TransMeta transMeta = transMetas[t];
            String filterString = esd.getFilterString();
            String filter = filterString;
            if (filter!=null) filter = filter.toUpperCase();
            
            List stringList = transMeta.getStringList(esd.isSearchingSteps(), esd.isSearchingDatabases(), esd.isSearchingNotes());
            for (int i=0;i<stringList.size();i++)
            {
                StringSearchResult result = (StringSearchResult) stringList.get(i);

                boolean add = Const.isEmpty(filter);
                if (filter!=null && result.getString().toUpperCase().indexOf(filter)>=0) add=true;
                if (filter!=null && result.getFieldName().toUpperCase().indexOf(filter)>=0) add=true;
                if (filter!=null && result.getParentObject().toString().toUpperCase().indexOf(filter)>=0) add=true;
                if (filter!=null && result.getGrandParentObject().toString().toUpperCase().indexOf(filter)>=0) add=true;
                
                if (add) rows.add(result.toRow());
            }
        }
        
        if (rows.size()!=0)
        {
            PreviewRowsDialog prd = new PreviewRowsDialog(shell, SWT.NONE, "String searcher", rows);
            prd.open();
        }
        else
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
            mb.setMessage(Messages.getString("Spoon.Dialog.NothingFound.Message")); // Nothing found that matches your criteria
            mb.setText(Messages.getString("Spoon.Dialog.NothingFound.Title")); // Sorry!
            mb.open();
        }
    }

    public void getVariables()
    {
        TransMeta[] transMetas = getLoadedTransformations();

        if (transMetas==null || transMetas.length==0) return;
        
        KettleVariables kettleVariables = KettleVariables.getInstance();
        Properties sp = new Properties();
        sp.putAll(kettleVariables.getProperties());
        sp.putAll(System.getProperties());
        
        for (int t=0;t<transMetas.length;t++)
        {
            TransMeta transMeta = transMetas[t];
            
            List list = transMeta.getUsedVariables();
            for (int i=0;i<list.size();i++)
            {
                String varName = (String)list.get(i);
                String varValue = sp.getProperty(varName, "");
                if (variables.searchValueIndex(varName)<0 && !varName.startsWith(Const.INTERNAL_VARIABLE_PREFIX))
                {
                    variables.addValue(new Value(varName, varValue));
                }
            }
        }
            
        // Now ask the use for more info on these!
        EnterStringsDialog esd = new EnterStringsDialog(shell, SWT.NONE, variables);
        esd.setTitle(Messages.getString("Spoon.Dialog.SetVariables.Title"));
        esd.setMessage(Messages.getString("Spoon.Dialog.SetVariables.Message"));
        esd.setReadOnly(false); 
        if (esd.open()!=null)
        {
            for (int i=0;i<variables.size();i++)
            {
                Value varval = variables.getValue(i);
                if (!Const.isEmpty(varval.getString()))
                {
                    kettleVariables.setVariable(varval.getName(), varval.getString());
                }
            }
        }
    }
    
    public void showVariables()
    {
        Properties sp = new Properties();
        KettleVariables kettleVariables = KettleVariables.getInstance();
        sp.putAll(kettleVariables.getProperties());
        sp.putAll(System.getProperties());

        Row allVars = new Row();
        
        Enumeration keys = kettleVariables.getProperties().keys();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            String value = kettleVariables.getVariable(key);
            
            allVars.addValue(new Value(key, value));
        }
        
        // Now ask the use for more info on these!
        EnterStringsDialog esd = new EnterStringsDialog(shell, SWT.NONE, allVars);
        esd.setTitle(Messages.getString("Spoon.Dialog.ShowVariables.Title"));
        esd.setMessage(Messages.getString("Spoon.Dialog.ShowVariables.Message"));
        esd.setReadOnly(true); 
        esd.open();
    }
    
    public void open()
    {       
        shell.open();
        
        // Shared database entries to load from repository?
        // loadRepositoryObjects();
        
        // Load shared objects from XML file.
        // loadSharedObjects();
        
        // What plugins did we use previously?
        refreshPluginHistory();
        
        // Perhaps the transformation contains elements at startup?
        refreshTree();  // Do a complete refresh then...
        
        setShellText();
        
        if (props.showTips()) 
        {
            TipsDialog tip = new TipsDialog(shell, props);
            tip.open();
        }
    }
    
    public boolean readAndDispatch ()
    {
        return disp.readAndDispatch();
    }
    
    /**
     * @return check whether or not the application was stopped.
     */
    public boolean isStopped()
    {
        return stopped;
    }
    
    /**
     * @param stopped True to stop this application.
     */
    public void setStopped(boolean stopped)
    {
        this.stopped = stopped;
    }
    
    /**
     * @param destroy Whether or not to distroy the display.
     */
    public void setDestroy(boolean destroy)
    {
        this.destroy = destroy;
    }
    
    /**
     * @return Returns whether or not we should distroy the display.
     */
    public boolean doDestroy()
    {
        return destroy;
    }
    
    /**
     * @param arguments The arguments to set.
     */
    public void setArguments(String[] arguments)
    {
        this.arguments = arguments;
    }

    /**
     * @return Returns the arguments.
     */
    public String[] getArguments()
    {
        return arguments;
    }
    
    public synchronized void dispose()
    {
        setStopped(true);
        cursor_hand.dispose();
        cursor_hourglass.dispose();
        
        if (destroy && !disp.isDisposed()) disp.dispose();        
    }

    public boolean isDisposed()
    {
        return disp.isDisposed();
    }

    public void sleep()
    {
        disp.sleep();
    }
    
    public void addMenu()
    {
        if (mBar!=null)
        {
            mBar.dispose();
        }
        mBar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(mBar);
        
        ////////////////////////////////////////////////////////////
        // File
        //
        //

        // main File menu...
        MenuItem mFile = new MenuItem(mBar, SWT.CASCADE); 
        //mFile.setText("&File");
        mFile.setText(Messages.getString("Spoon.Menu.File") );
        msFile = new Menu(shell, SWT.DROP_DOWN);
        mFile.setMenu(msFile);
        // New
        //
        MenuItem miFileNew = new MenuItem(msFile, SWT.CASCADE); 
        miFileNew.setText(Messages.getString("Spoon.Menu.File.New")); //miFileNew.setText("&New \tCTRL-N");
        miFileNew.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { newFile(); } } );
        // Open
        //
        MenuItem miFileOpen = new MenuItem(msFile, SWT.CASCADE); 
        miFileOpen.setText(Messages.getString("Spoon.Menu.File.Open")); //&Open \tCTRL-O
        miFileOpen.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { openFile(false); } });
        // Import from XML
        //
        MenuItem miFileImport = new MenuItem(msFile, SWT.CASCADE); 
        miFileImport.setText(Messages.getString("Spoon.Menu.File.Import")); //"&Import from an XML file\tCTRL-I"
        miFileImport.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { openFile(true); } });
        // Export to XML
        //
        MenuItem miFileExport = new MenuItem(msFile, SWT.CASCADE); 
        miFileExport.setText(Messages.getString("Spoon.Menu.File.Export")); //&Export to an XML file
        miFileExport.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { saveXMLFile(getActiveTransformation()); } });
        // Save
        //
        miFileSave = new MenuItem(msFile, SWT.CASCADE); 
        miFileSave.setText(Messages.getString("Spoon.Menu.File.Save"));  //"&Save \tCTRL-S"
        miFileSave.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { saveFile(getActiveTransformation()); } });
        // Save as
        //
        miFileSaveAs = new MenuItem(msFile, SWT.CASCADE); 
        miFileSaveAs.setText(Messages.getString("Spoon.Menu.File.SaveAs"));  //"Save &as..."
        miFileSaveAs.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { saveFileAs(getActiveTransformation()); } });
        // Close
        //
        miFileClose = new MenuItem(msFile, SWT.CASCADE); 
        miFileClose.setText(Messages.getString("Spoon.Menu.File.Close")); //&Close \tCTRL-F4
        miFileClose.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { closeTransformation(getActiveTransformation()); } });
        new MenuItem(msFile, SWT.SEPARATOR);
        // Print
        //
        miFilePrint = new MenuItem(msFile, SWT.CASCADE); 
        miFilePrint.setText(Messages.getString("Spoon.Menu.File.Print")); //"&Print \tCTRL-P"
        miFilePrint.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { printFile(getActiveTransformation()); } });
        new MenuItem(msFile, SWT.SEPARATOR);
        // Quit
        //
        MenuItem miFileQuit = new MenuItem(msFile, SWT.CASCADE); 
        miFileQuit.setText(Messages.getString("Spoon.Menu.File.Quit")); //miFileQuit.setText("&Quit");
        miFileQuit.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { quitFile(); } });
        
        miFileSep3= new MenuItem(msFile, SWT.SEPARATOR);
        // History
        addMenuLast();
        
        ////////////////////////////////////////////////////////////
        // Edit
        //
        //

        // main Edit menu...
        MenuItem mEdit = new MenuItem(mBar, SWT.CASCADE); 
        mEdit.setText(Messages.getString("Spoon.Menu.Edit")); //&Edit
        Menu msEdit = new Menu(shell, SWT.DROP_DOWN);
        mEdit.setMenu(msEdit);
        // Undo
        //
        miEditUndo = new MenuItem(msEdit, SWT.CASCADE);
        miEditUndo.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { undoAction(getActiveTransformation()); } });
        // Redo
        //
        miEditRedo = new MenuItem(msEdit, SWT.CASCADE);
        miEditRedo.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { redoAction(getActiveTransformation()); } });
        setUndoMenu(getActiveTransformation());
        new MenuItem(msEdit, SWT.SEPARATOR);
        // Search
        //
        MenuItem miEditSearch = new MenuItem(msEdit, SWT.CASCADE); 
        miEditSearch.setText(Messages.getString("Spoon.Menu.Edit.Search"));  //Search Metadata \tCTRL-F
        miEditSearch.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { searchMetaData(); } });
        // Set variables
        //
        MenuItem miEditVars = new MenuItem(msEdit, SWT.CASCADE); 
        miEditVars.setText(Messages.getString("Spoon.Menu.Edit.Variables"));  //Set variables \tCTRL-J
        miEditVars.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { getVariables(); } });
        // Show variables
        MenuItem miEditSVars = new MenuItem(msEdit, SWT.CASCADE); 
        miEditSVars.setText(Messages.getString("Spoon.Menu.Edit.ShowVariables"));  //Show variables \tCTRL-L
        miEditSVars.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { showVariables(); } });
        new MenuItem(msEdit, SWT.SEPARATOR);
        // Clear selection
        //
        miEditUnselectAll = new MenuItem(msEdit, SWT.CASCADE); 
        miEditUnselectAll.setText(Messages.getString("Spoon.Menu.Edit.ClearSelection"));  //&Clear selection \tESC
        miEditUnselectAll.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { editUnselectAll(getActiveTransformation()); } });
        // Select all
        //
        miEditSelectAll = new MenuItem(msEdit, SWT.CASCADE); 
        miEditSelectAll.setText(Messages.getString("Spoon.Menu.Edit.SelectAllSteps")); //"&Select all steps \tCTRL-A"
        miEditSelectAll.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { editSelectAll(getActiveTransformation());   } });
        new MenuItem(msEdit, SWT.SEPARATOR);
        // Copy to clipboard
        //
        miEditCopy         = new MenuItem(msEdit, SWT.CASCADE); 
        miEditCopy.setText(Messages.getString("Spoon.Menu.Edit.CopyToClipboard")); //Copy selected steps to clipboard\tCTRL-C
        // Paste from clipboard
        //
        miEditPaste        = new MenuItem(msEdit, SWT.CASCADE); 
        miEditPaste.setText(Messages.getString("Spoon.Menu.Edit.PasteFromClipboard")); //Paste steps from clipboard\tCTRL-V
        new MenuItem(msEdit, SWT.SEPARATOR);
        // Refresh
        //
        MenuItem miEditRefresh      = new MenuItem(msEdit, SWT.CASCADE); 
        miEditRefresh.setText(Messages.getString("Spoon.Menu.Edit.Refresh"));  //&Refresh \tF5
        new MenuItem(msEdit, SWT.SEPARATOR);
        // Options
        //
        MenuItem miEditOptions      = new MenuItem(msEdit, SWT.CASCADE); 
        miEditOptions.setText(Messages.getString("Spoon.Menu.Edit.Options"));  //&Options...
        miEditOptions.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { editOptions(); } });
        
        ////////////////////////////////////////////////////////////
        // Repository
        //
        //

        // main Repository menu...
        MenuItem mRep = new MenuItem(mBar, SWT.CASCADE); mRep.setText(Messages.getString("Spoon.Menu.Repository")); //&Repository
        Menu msRep = new Menu(shell, SWT.DROP_DOWN);
        mRep.setMenu(msRep);
        // Connect to repository
        //
        MenuItem miRepConnect    = new MenuItem(msRep, SWT.CASCADE); 
        miRepConnect.setText(Messages.getString("Spoon.Menu.Repository.ConnectToRepository"));  //&Connect to repository \tCTRL-R
        miRepConnect.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { openRepository(); } });
        // Disconnect from repository
        //
        miRepDisconnect = new MenuItem(msRep, SWT.CASCADE); 
        miRepDisconnect.setText(Messages.getString("Spoon.Menu.Repository.DisconnectRepository")); //&Disconnect repository \tCTRL-D
        miRepDisconnect.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { closeRepository(); } });
        // Explore the repository
        //
        miRepExplore    = new MenuItem(msRep, SWT.CASCADE); 
        miRepExplore.setText(Messages.getString("Spoon.Menu.Repository.ExploreRepository"));  //&Explore repository \tCTRL-E
        miRepExplore.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { exploreRepository(); } });
        new MenuItem(msRep, SWT.SEPARATOR);
        // Edit current user
        //
        miRepUser       = new MenuItem(msRep, SWT.CASCADE); 
        miRepUser.setText(Messages.getString("Spoon.Menu.Repository.EditCurrentUser")); //&Edit current user\tCTRL-U
        miRepUser.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { editRepositoryUser();} });
        
        ////////////////////////////////////////////////////////////
        // Transformation
        //
        //

        // main Transformation menu...
        MenuItem mTrans = new MenuItem(mBar, SWT.CASCADE); mTrans.setText(Messages.getString("Spoon.Menu.Transformation"));  //&Transformation
        Menu msTrans = new Menu(shell, SWT.DROP_DOWN );
        mTrans.setMenu(msTrans);

        // Run
        //
        miTransRun = new MenuItem(msTrans, SWT.CASCADE); 
        miTransRun.setText(Messages.getString("Spoon.Menu.Transformation.Run"));//&Run \tF9
        miTransRun.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { executeTransformation(getActiveTransformation(), true, false, false, false, null); } });
        // Preview
        //
        miTransPreview = new MenuItem(msTrans, SWT.CASCADE); 
        miTransPreview.setText(Messages.getString("Spoon.Menu.Transformation.Preview"));//&Preview \tF10
        miTransPreview.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { executeTransformation(getActiveTransformation(), true, false, false, true, null); } });
        // Check
        //
        miTransCheck = new MenuItem(msTrans, SWT.CASCADE); 
        miTransCheck.setText(Messages.getString("Spoon.Menu.Transformation.Verify"));//&Verify \tF11
        miTransCheck.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { checkTrans(getActiveTransformation());} });
        // Impact
        //
        miTransImpact = new MenuItem(msTrans, SWT.CASCADE); 
        miTransImpact.setText(Messages.getString("Spoon.Menu.Transformation.Impact"));//&Impact
        miTransImpact.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { analyseImpact(getActiveTransformation());    } });
        // SQL
        //
        miTransSQL = new MenuItem(msTrans, SWT.CASCADE); 
        miTransSQL.setText(Messages.getString("Spoon.Menu.Transformation.GetSQL"));//&Get SQL
        miTransSQL.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { getSQL(getActiveTransformation());           } });
        new MenuItem(msTrans, SWT.SEPARATOR);
        // Show last Impact results
        //
        miLastImpact = new MenuItem(msTrans, SWT.CASCADE); 
        miLastImpact.setText(Messages.getString("Spoon.Menu.Transformation.ShowLastImpactAnalyses"));//Show last impact analyses \tF6
        miLastImpact.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { showLastImpactAnalyses(getActiveTransformation());  } });
        // Show last verify results
        //
        miLastCheck = new MenuItem(msTrans, SWT.CASCADE); 
        miLastCheck.setText(Messages.getString("Spoon.Menu.Transformation.ShowLastVerifyResults"));//Show last verify results  \tF7
        miLastCheck.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { showLastTransCheck();      } });
        // Show last preview results
        //
        miLastPreview = new MenuItem(msTrans, SWT.CASCADE); 
        miLastPreview.setText(Messages.getString("Spoon.Menu.Transformation.ShowLastPreviewResults"));//Show last preview results \tF8
        miLastPreview.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { SpoonLog spoonLog = getActiveSpoonLog(); if (spoonLog!=null) spoonLog.showPreview(); } });
        new MenuItem(msTrans, SWT.SEPARATOR);
        // Copy transformation to clipboard
        //
        miTransCopy = new MenuItem(msTrans, SWT.CASCADE); 
        miTransCopy.setText(Messages.getString("Spoon.Menu.Transformation.CopyTransformationToClipboard"));//&Copy transformation to clipboard
        miTransCopy.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { copyTransformation(getActiveTransformation()); } });
        // Paste transformation to clipboard
        //
        miTransPaste = new MenuItem(msTrans, SWT.CASCADE); 
        miTransPaste.setText(Messages.getString("Spoon.Menu.Transformation.PasteTransformationFromClipboard"));//P&aste transformation from clipboard
        miTransPaste.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { copyTransformationImage(getActiveTransformation()); } });
        // Copy image of transformation to clipboard
        //
        miTransImage = new MenuItem(msTrans, SWT.CASCADE); 
        miTransImage.setText(Messages.getString("Spoon.Menu.Transformation.CopyTransformationImageClipboard"));//Copy the transformation image clipboard \tCTRL-ALT-I
        miTransImage.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { pasteTransformation(); } });
        new MenuItem(msTrans, SWT.SEPARATOR);
        // Edit transformation setttings
        //
        miTransDetails   = new MenuItem(msTrans, SWT.CASCADE); 
        miTransDetails.setText(Messages.getString("Spoon.Menu.Transformation.Settings"));//&Settings... \tCTRL-T
        miTransDetails.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { editTransformationProperties(getActiveTransformation());   } });
        
        ////////////////////////////////////////////////////////////
        // Wizard
        //
        //

        // Wizard menu
        MenuItem mWizard = new MenuItem(mBar, SWT.CASCADE); mWizard.setText(Messages.getString("Spoon.Menu.Wizard"));  //"&Wizard"
        Menu msWizard = new Menu(shell, SWT.DROP_DOWN );
        mWizard.setMenu(msWizard);

        // New database connection wizard
        //
        miWizardNewConnection = new MenuItem(msWizard, SWT.CASCADE); 
        miWizardNewConnection.setText(Messages.getString("Spoon.Menu.Wizard.CreateDatabaseConnectionWizard"));//&Create database connection wizard...\tF3
        miWizardNewConnection.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { createDatabaseWizard(getActiveTransformation()); }});

        // Copy table wizard
        //
        miWizardCopyTable = new MenuItem(msWizard, SWT.CASCADE); 
        miWizardCopyTable.setText(Messages.getString("Spoon.Menu.Wizard.CopyTableWizard"));//&Copy table wizard...\tF4
        miWizardCopyTable.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { copyTableWizard(getActiveTransformation()); }});
          
        
        ////////////////////////////////////////////////////////////
        // Help
        //
        //
        
        // main Help menu...
        MenuItem mHelp = new MenuItem(mBar, SWT.CASCADE); mHelp.setText(Messages.getString("Spoon.Menu.Help")); //"&Help"
        Menu msHelp = new Menu(shell, SWT.DROP_DOWN );
        mHelp.setMenu(msHelp);
        
        // Credits
        //
        MenuItem miHelpCredit = new MenuItem(msHelp, SWT.CASCADE); 
        miHelpCredit.setText(Messages.getString("Spoon.Menu.Help.Credits"));//&Credits
        miHelpCredit.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { ShowCreditsDialog scd = new ShowCreditsDialog(shell, props, GUIResource.getInstance().getImageCredits()); scd.open(); } });
        // Tip of the day
        //
        MenuItem miHelpTOTD = new MenuItem(msHelp, SWT.CASCADE); 
        miHelpTOTD.setText(Messages.getString("Spoon.Menu.Help.Tip"));//&Tip of the day
        miHelpTOTD.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { new TipsDialog(shell, props).open(); }});
        new MenuItem(msHelp, SWT.SEPARATOR);
        // About
        //
        MenuItem miHelpAbout = new MenuItem(msHelp, SWT.CASCADE); 
        miHelpAbout.setText(Messages.getString("Spoon.Menu.About"));//"&About"
        miHelpAbout.addListener (SWT.Selection, new Listener() { public void handleEvent(Event e) { helpAbout(); } });
    }
   
    private void addMenuLast()
    {
        int idx = msFile.indexOf(miFileSep3);
        int max = msFile.getItemCount();
        
        // Remove everything until end... 
        for (int i=max-1;i>idx;i--)
        {
            MenuItem mi = msFile.getItem(i);
            mi.dispose();
        }
        
        // Previously loaded files...
        List lastUsedFiles = props.getLastUsedFiles();
        for (int i = 0; i < lastUsedFiles.size(); i++)
        {
            final LastUsedFile lastUsedFile = (LastUsedFile) lastUsedFiles.get(i);
            MenuItem miFileLast = new MenuItem(msFile, SWT.CASCADE);

            char chr = (char) ('1' + i);
            int accel = SWT.CTRL | chr;

            if (i < 9)
            {
                miFileLast.setAccelerator(accel);
                miFileLast.setText("&" + chr + "  " + lastUsedFile + "\tCTRL-" + chr);
            }
            else
            {
                miFileLast.setText("   " + lastUsedFile);
            }

            Listener lsFileLast = new Listener()
            {
                public void handleEvent(Event e)
                {
                    // If the file comes from a repository and it's not the same as
                    // the one we're connected to, ask for a username/password!
                    //
                    boolean noRepository = false;
                    if (lastUsedFile.isSourceRepository()
                            && (rep == null || !rep.getRepositoryInfo().getName().equalsIgnoreCase(lastUsedFile.getRepositoryName())))
                    {
                        // Ask for a username password to get the required repository access
                        //
                        int perms[] = new int[] { PermissionMeta.TYPE_PERMISSION_TRANSFORMATION };
                        RepositoriesDialog rd = new RepositoriesDialog(disp, SWT.NONE, perms, Messages.getString("Spoon.Application.Name")); // RepositoriesDialog.ToolName="Spoon"
                        rd.setRepositoryName(lastUsedFile.getRepositoryName());
                        if (rd.open())
                        {
                            // Close the previous connection...
                            if (rep != null) rep.disconnect();
                            rep = new Repository(log, rd.getRepository(), rd.getUser());
                            try
                            {
                                rep.connect(APP_NAME);
                            }
                            catch (KettleException ke)
                            {
                                rep = null;
                                new ErrorDialog(
                                        shell,
                                        Messages.getString("Spoon.Dialog.UnableConnectRepository.Title"), Messages.getString("Spoon.Dialog.UnableConnectRepository.Message"), ke); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                        else
                        {
                            noRepository = true;
                        }
                    }

                    if (lastUsedFile.isSourceRepository())
                    {
                        if (!noRepository && rep != null && rep.getRepositoryInfo().getName().equalsIgnoreCase(lastUsedFile.getRepositoryName()))
                        {
                            // OK, we're connected to the new repository...
                            // Load the transformation...
                            RepositoryDirectory fdRepdir = rep.getDirectoryTree().findDirectory(lastUsedFile.getDirectory());
                            TransLoadProgressDialog tlpd = new TransLoadProgressDialog(shell, rep, lastUsedFile.getFilename(), fdRepdir);
                            TransMeta transMeta = tlpd.open();
                            if (transMeta != null)
                            {
                                transMeta.clearChanged();
                                addSpoonGraph(transMeta);
                                
                                props.addLastFile(Props.TYPE_PROPERTIES_SPOON, lastUsedFile.getFilename(), fdRepdir.getPath(), true, rep.getName());
                            }
                        }
                        else
                        {
                            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
                            mb.setMessage(Messages.getString("Spoon.Dialog.UnableLoadTransformation.Message")); // Can't load from rep, please connect first
                            mb.setText(Messages.getString("Spoon.Dialog.UnableLoadTransformation.Title"));// Error!
                            mb.open();
                        }
                    }
                    else
                    // Load from XML!
                    {
                        try
                        {
                            TransMeta transMeta = new TransMeta(lastUsedFile.getFilename());
                            transMeta.clearChanged();
                            transMeta.setFilename(lastUsedFile.getFilename());
                            addSpoonGraph(transMeta);
                            
                            props.addLastFile(Props.TYPE_PROPERTIES_SPOON, lastUsedFile.getFilename(), null, false, null);
                        }
                        catch (KettleException ke)
                        {
                            // "Error loading transformation", "I was unable to load this transformation from the
                            // XML file because of an error"
                            new ErrorDialog(shell, Messages.getString("Spoon.Dialog.LoadTransformationError.Title"), Messages.getString("Spoon.Dialog.LoadTransformationError.Message"), ke);
                        }
                    }
                    addMenuLast();
                    refreshTree();
                    refreshGraph();
                    refreshHistory();
                }
            };
            miFileLast.addListener(SWT.Selection, lsFileLast);
        }
    }
    
    private void addBar()
    {
        tBar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT );
        // props.setLook(tBar);
        
        final ToolItem tiFileNew = new ToolItem(tBar, SWT.PUSH);
        final Image imFileNew = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"new.png")); 
        tiFileNew.setImage(imFileNew);
        tiFileNew.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newFile(); }});
        tiFileNew.setToolTipText(Messages.getString("Spoon.Tooltip.NewTranformation"));//New transformation, clear all settings

        final ToolItem tiFileOpen = new ToolItem(tBar, SWT.PUSH);
        final Image imFileOpen = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"open.png")); 
        tiFileOpen.setImage(imFileOpen);
        tiFileOpen.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { openFile(false); }});
        tiFileOpen.setToolTipText(Messages.getString("Spoon.Tooltip.OpenTranformation"));//Open tranformation

        final ToolItem tiFileSave = new ToolItem(tBar, SWT.PUSH);
        final Image imFileSave = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"save.png")); 
        tiFileSave.setImage(imFileSave);
        tiFileSave.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { saveFile(getActiveTransformation()); }});
        tiFileSave.setToolTipText(Messages.getString("Spoon.Tooltip.SaveCurrentTranformation"));//Save current transformation

        final ToolItem tiFileSaveAs = new ToolItem(tBar, SWT.PUSH);
        final Image imFileSaveAs = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"saveas.png")); 
        tiFileSaveAs.setImage(imFileSaveAs);
        tiFileSaveAs.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { saveFileAs(getActiveTransformation()); }});
        tiFileSaveAs.setToolTipText(Messages.getString("Spoon.Tooltip.SaveDifferentNameTranformation"));//Save transformation with different name

        new ToolItem(tBar, SWT.SEPARATOR);
        final ToolItem tiFilePrint = new ToolItem(tBar, SWT.PUSH);
        final Image imFilePrint = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"print.png")); 
        tiFilePrint.setImage(imFilePrint);
        tiFilePrint.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { printFile(getActiveTransformation()); }});
        tiFilePrint.setToolTipText(Messages.getString("Spoon.Tooltip.Print"));//Print

        new ToolItem(tBar, SWT.SEPARATOR);
        final ToolItem tiFileRun = new ToolItem(tBar, SWT.PUSH);
        final Image imFileRun = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"run.png")); 
        tiFileRun.setImage(imFileRun);
        tiFileRun.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { executeTransformation(getActiveTransformation(), true, false, false, false, null); }});
        tiFileRun.setToolTipText(Messages.getString("Spoon.Tooltip.RunTranformation"));//Run this transformation

        final ToolItem tiFilePreview = new ToolItem(tBar, SWT.PUSH);
        final Image imFilePreview = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"preview.png")); 
        tiFilePreview.setImage(imFilePreview);
        tiFilePreview.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { executeTransformation(getActiveTransformation(), true, false, false, true, null); }});
        tiFilePreview.setToolTipText(Messages.getString("Spoon.Tooltip.PreviewTranformation"));//Preview this transformation

        final ToolItem tiFileReplay = new ToolItem(tBar, SWT.PUSH);
        final Image imFileReplay = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"replay.png")); 
        tiFileReplay.setImage(imFileReplay);
        tiFileReplay.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { executeTransformation(getActiveTransformation(), true, false, false, true, null); }});
        tiFileReplay.setToolTipText("Replay this transformation");

        new ToolItem(tBar, SWT.SEPARATOR);
        final ToolItem tiFileCheck = new ToolItem(tBar, SWT.PUSH);
        final Image imFileCheck = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"check.png")); 
        tiFileCheck.setImage(imFileCheck);
        tiFileCheck.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { checkTrans(getActiveTransformation()); }});
        tiFileCheck.setToolTipText(Messages.getString("Spoon.Tooltip.VerifyTranformation"));//Verify this transformation

        new ToolItem(tBar, SWT.SEPARATOR);
        final ToolItem tiImpact = new ToolItem(tBar, SWT.PUSH);
        final Image imImpact = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"impact.png")); 
        // Can't seem to get the transparency correct for this image!
        ImageData idImpact = imImpact.getImageData();
        int impactPixel = idImpact.palette.getPixel(new RGB(255, 255, 255));
        idImpact.transparentPixel = impactPixel;
        Image imImpact2 = new Image(disp, idImpact);
        tiImpact.setImage(imImpact2);
        tiImpact.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { analyseImpact(getActiveTransformation()); }});
        tiImpact.setToolTipText(Messages.getString("Spoon.Tooltip.AnalyzeTranformation"));//Analyze the impact of this transformation on the database(s)

        new ToolItem(tBar, SWT.SEPARATOR);
        final ToolItem tiSQL = new ToolItem(tBar, SWT.PUSH);
        final Image imSQL = new Image(disp, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"SQLbutton.png")); 
        // Can't seem to get the transparency correct for this image!
        ImageData idSQL = imSQL.getImageData();
        int sqlPixel= idSQL.palette.getPixel(new RGB(255, 255, 255));
        idSQL.transparentPixel = sqlPixel;
        Image imSQL2= new Image(disp, idSQL);
        tiSQL.setImage(imSQL2);
        tiSQL.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getSQL(getActiveTransformation());  }});
        tiSQL.setToolTipText(Messages.getString("Spoon.Tooltip.GenerateSQLForTranformation"));//Generate the SQL needed to run this transformation

        tBar.addDisposeListener(new DisposeListener() 
            {
                public void widgetDisposed(DisposeEvent e) 
                {
                    imFileNew.dispose();
                    imFileOpen.dispose();
                    imFileSave.dispose();
                    imFileSaveAs.dispose();
                }
            }
        );
        tBar.addKeyListener(defKeys);
        tBar.addKeyListener(modKeys);
        tBar.pack();
    }

    private static final String STRING_SPOON_MAIN_TREE = "Spoon Main Tree";
    
    private void addTree()
    {
        if (leftSash!=null)
        {
            leftSash.dispose();
        }
        
        // Split the left side of the screen in half
        leftSash = new SashForm(sashform, SWT.VERTICAL);
        
        // Now set up the main CSH tree
        selectionTree = new Tree(leftSash, SWT.SINGLE | SWT.BORDER);
        props.setLook(selectionTree);
        selectionTree.setLayout(new FillLayout());
        
        // Add a tree memory as well...
        TreeMemory.addTreeListener(selectionTree, STRING_SPOON_MAIN_TREE);
        
        tiTrans  = new TreeItem(selectionTree, SWT.NONE); tiTrans.setText(STRING_TRANSFORMATIONS);
        
        tiBlocks = new TreeItem(selectionTree, SWT.NONE); tiBlocks.setText(STRING_BUILDING_BLOCKS);
        tiBase   = new TreeItem(tiBlocks, SWT.NONE); tiBase.setText(STRING_BASE);
        tiPlug   = new TreeItem(tiBlocks, SWT.NONE); tiPlug.setText(STRING_PLUGIN);
        
        // Fill the base components...
        StepLoader steploader = StepLoader.getInstance();
        StepPlugin basesteps[] = steploader.getStepsWithType(StepPlugin.TYPE_NATIVE);
        String basecat[] = steploader.getCategories(StepPlugin.TYPE_NATIVE);
        TreeItem tiBaseCat[] = new TreeItem[basecat.length];
        for (int i=0;i<basecat.length;i++)
        {
            tiBaseCat[i] = new TreeItem(tiBase, SWT.NONE);
            tiBaseCat[i].setText(basecat[i]);
            
            for (int j=0;j<basesteps.length;j++)
            {
                if (basesteps[j].getCategory().equalsIgnoreCase(basecat[i]))
                {
                    TreeItem ti = new TreeItem(tiBaseCat[i], 0);
                    ti.setText(basesteps[j].getDescription());
                }
            }
        }

        // Show the plugins...
        StepPlugin plugins[] = steploader.getStepsWithType(StepPlugin.TYPE_PLUGIN);
        String plugcat[] = steploader.getCategories(StepPlugin.TYPE_PLUGIN);
        TreeItem tiPlugCat[] = new TreeItem[plugcat.length];
        for (int i=0;i<plugcat.length;i++)
        {
            tiPlugCat[i] = new TreeItem(tiPlug, SWT.NONE);
            tiPlugCat[i].setText(plugcat[i]);
            
            for (int j=0;j<plugins.length;j++)
            {
                if (plugins[j].getCategory().equalsIgnoreCase(plugcat[i]))
                {
                    TreeItem ti = new TreeItem(tiPlugCat[i], 0);
                    ti.setText(plugins[j].getDescription());
                }
            }
        }
        
        tiBase.setExpanded(true);
        tiPlug.setExpanded(true);

        addToolTipsToTree(selectionTree);

        // Default selection (double-click, enter)
        // lsNewDef  = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e){ newSelected();  } };
        
        // Add all the listeners... 
        // selectionTree.addSelectionListener(lsEditDef); // double click somewhere in the tree...
        // tCSH.addSelectionListener(lsNewDef); // double click somewhere in the tree...
        
        selectionTree.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { setMenu(); } });
        selectionTree.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { showSelection(); } });
        selectionTree.addSelectionListener(new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e){ editSelected(); } });
        
        // Keyboard shortcuts!
        selectionTree.addKeyListener(defKeys);
        selectionTree.addKeyListener(modKeys);
        
        // Set a listener on the tree
        addDragSourceToTree(selectionTree);
        
        
        
        // OK, now add a list of often-used icons to the bottom of the tree...
        pluginHistoryTree = new Tree(leftSash, SWT.SINGLE );
        
        // Add tooltips for history tree too
        addToolTipsToTree(pluginHistoryTree);
        
        // Set the same listener on this tree
        addDragSourceToTree(pluginHistoryTree);

        leftSash.setWeights(new int[] { 70, 30 } );
        
        setTreeImages();
    }
    
    protected void shareObject(SharedObjectInterface sharedObjectInterface)
    {
        sharedObjectInterface.setShared(true);
        refreshTree();
    }

    /**
     * @return The object that is selected in the tree or null if we couldn't figure it out. (titles etc. == null)
     */
    public TreeSelection[] getTreeObjects(final Tree tree)
    {
        List objects = new ArrayList();
        
        if (tree.equals(selectionTree))
        {
            TreeItem[] selection = selectionTree.getSelection();
            for (int s=0;s<selection.length;s++)
            {
                TreeItem treeItem = selection[s];
                String[] path = Const.getTreeStrings(treeItem);
                
                TreeSelection object = null;
                
                switch(path.length)
                {
                case 0: break;
                case 1: // ------complete-----
                    if (path[0].equals(STRING_TRANSFORMATIONS)) // the top level Transformations entry
                    {
                        object = new TreeSelection(TransMeta.class);
                    }
                    break;
                    
                case 2: // ------complete-----
                    if (path[0].equals(STRING_BUILDING_BLOCKS)) // the top level Transformations entry
                    {
                        if (path[1].equals(STRING_BASE) || path[1].equals(STRING_PLUGIN))
                        {
                            object = new TreeSelection(StepPlugin.class);
                        }
                    }
                    if (path[0].equals(STRING_TRANSFORMATIONS)) // Transformation title
                    {
                        object = new TreeSelection(findTransformation(path[1]));
                    }
                    break;
                        
                case 3:  // ------complete-----
                    if (path[0].equals(STRING_TRANSFORMATIONS)) // Transformations title
                    {
                        TransMeta transMeta = findTransformation(path[1]);
                        if (path[2].equals(STRING_CONNECTIONS)) object = new TreeSelection(DatabaseMeta.class, transMeta);
                        if (path[2].equals(STRING_STEPS)) object = new TreeSelection(StepMeta.class, transMeta);
                        if (path[2].equals(STRING_HOPS)) object = new TreeSelection(TransHopMeta.class, transMeta);
                        if (path[2].equals(STRING_PARTITIONS)) object = new TreeSelection(PartitionSchema.class, transMeta);
                        if (path[2].equals(STRING_SLAVES)) object = new TreeSelection(SlaveServer.class, transMeta);
                        if (path[2].equals(STRING_CLUSTERS)) object = new TreeSelection(ClusterSchema.class, transMeta);
                    }
                    break;
                    
                case 4:  // ------complete-----
                    if (path[0].equals(STRING_TRANSFORMATIONS)) // The name of a transformation
                    {
                        TransMeta transMeta = findTransformation(path[1]);
                        if (path[2].equals(STRING_CONNECTIONS)) object = new TreeSelection(transMeta.findDatabase(path[3]), transMeta);
                        if (path[2].equals(STRING_STEPS)) object = new TreeSelection(transMeta.findStep(path[3]), transMeta);
                        if (path[2].equals(STRING_HOPS)) object = new TreeSelection(transMeta.findTransHop(path[3]), transMeta);
                        if (path[2].equals(STRING_PARTITIONS)) object = new TreeSelection(transMeta.findPartitionSchema(path[3]), transMeta);
                        if (path[2].equals(STRING_SLAVES)) object = new TreeSelection(transMeta.findSlaveServer(path[3]), transMeta);
                        if (path[2].equals(STRING_CLUSTERS)) object = new TreeSelection(transMeta.findClusterSchema(path[3]), transMeta);
                    }
                    if (path[0].equals(STRING_BUILDING_BLOCKS)) // building blocks top
                    {
                        if (path[1].equals(STRING_BASE) || path[1].equals(STRING_PLUGIN))
                        {
                            object = new TreeSelection(StepLoader.getInstance().findStepPluginWithDescription(path[3]));
                        }
                    }
                    break;
                    
                case 5:
                    if (path[0].equals(STRING_TRANSFORMATIONS)) // The name of a transformation
                    {
                        TransMeta transMeta = findTransformation(path[1]);
                        if (path[2].equals(STRING_CLUSTERS))
                        {
                            ClusterSchema clusterSchema = transMeta.findClusterSchema(path[3]);
                            object = new TreeSelection(clusterSchema.findSlaveServer(path[4]), clusterSchema, transMeta);
                        }
                    }
                    break;
                default: break;
                }
                
                if (object!=null)
                {
                    objects.add(object);
                }
            }
        }
        if (tree.equals(pluginHistoryTree))
        {
            TreeItem[] selection = pluginHistoryTree.getSelection();
            for (int s=0;s<selection.length;s++)
            {
                TreeItem treeItem = selection[s];
                String[] path = Const.getTreeStrings(treeItem);
                
                TreeSelection object = null;
                
                switch(path.length)
                {
                case 0: break;
                case 1: break; // ------complete-----
                case 2: // ------complete-----
                    if (path[0].equals(STRING_HISTORY))
                    {
                        StepPlugin stepPlugin = StepLoader.getInstance().findStepPluginWithDescription(path[1]);
                        if (stepPlugin!=null)
                        {
                            object = new TreeSelection(stepPlugin);
                        }
                    }
                    break;
                default: break;
                }
                
                if (object!=null)
                {
                    objects.add(object);
                }
            }
        }
        
        return (TreeSelection[]) objects.toArray(new TreeSelection[objects.size()]);
    }
    
    private void addToolTipsToTree(Tree tree)
    {
        tree.addListener(SWT.MouseHover, new Listener()
            {
                public void handleEvent(Event e)
                {
                    String tooltip=null;
                    Tree tree = (Tree)e.widget;
                    TreeItem item = tree.getItem(new org.eclipse.swt.graphics.Point(e.x, e.y));
                    if (item!=null)
                    {
                        StepLoader steploader = StepLoader.getInstance();
                        StepPlugin sp = steploader.findStepPluginWithDescription(item.getText());
                        if (sp!=null)
                        {
                            tooltip = sp.getTooltip();
                        }
                        else
                        if (item.getText().equalsIgnoreCase(STRING_BASE) ||
                            item.getText().equalsIgnoreCase(STRING_PLUGIN)
                           )
                        {
                            
                            tooltip=Messages.getString("Spoon.Tooltip.SelectStepType",Const.CR);  //"Select one of the step types listed below and"+Const.CR+"drag it onto the graphical view tab to the right.";
                        }
                    }
                    tree.setToolTipText(tooltip);
                }
            }
        );
    }
    
    
    private void addDragSourceToTree(final Tree tree)
    {
        // Drag & Drop for steps
        Transfer[] ttypes = new Transfer[] { XMLTransfer.getInstance() };
        
        DragSource ddSource = new DragSource(tree, DND.DROP_MOVE);
        ddSource.setTransfer(ttypes);
        ddSource.addDragListener(new DragSourceListener() 
            {
                public void dragStart(DragSourceEvent event){ }
    
                public void dragSetData(DragSourceEvent event) 
                {
                    TreeSelection[] treeObjects = getTreeObjects(tree);
                    if (treeObjects.length==0)
                    {
                        event.doit=false;
                        return;
                    }
                    
                    int type=0;
                    String data = null;
                    
                    TreeSelection treeObject = treeObjects[0];
                    Object object = treeObject.getSelection();
                    
                    // Drop of existing hidden step onto canvas?
                    if (object instanceof StepMeta)
                    {
                        StepMeta stepMeta = (StepMeta)object;
                    	type = DragAndDropContainer.TYPE_STEP;
                        data=stepMeta.getName(); // name of the step.
                    }
                    else
                	if (object instanceof StepPlugin)
                    {
                        StepPlugin stepPlugin = (StepPlugin)object;
                    	type = DragAndDropContainer.TYPE_BASE_STEP_TYPE;
                        data=stepPlugin.getDescription(); // Step type
                    }
                    else
                    if (object instanceof DatabaseMeta)
                    {
                        DatabaseMeta databaseMeta = (DatabaseMeta) object;
                    	type = DragAndDropContainer.TYPE_DATABASE_CONNECTION;
                        data=databaseMeta.getName();
                    }
                    else
                    if (object instanceof TransHopMeta)
                    {
                        TransHopMeta hop = (TransHopMeta) object;
                    	type = DragAndDropContainer.TYPE_TRANS_HOP;
                        data=hop.toString(); // nothing for really ;-)
                    }
                    else
                    {
                        event.doit=false;
                    	return; // ignore anything else you drag.
                    }

                	event.data = new DragAndDropContainer(type, data);
                }
    
                public void dragFinished(DragSourceEvent event) {}
            }
        );

    }
    
    public void refreshPluginHistory()
    {
        pluginHistoryTree.removeAll();
        
        TreeItem tiMain = new TreeItem(pluginHistoryTree, SWT.NONE);
        tiMain.setText(STRING_HISTORY);
        tiMain.setImage(GUIResource.getInstance().getImageSpoon());
        
        List pluginHistory = props.getPluginHistory();
        for (int i=0;i<pluginHistory.size();i++)
        {
            String pluginID = (String)pluginHistory.get(i);
            StepPlugin stepPlugin = StepLoader.getInstance().findStepPluginWithID(pluginID);
            if (stepPlugin!=null)
            {
                Image image = (Image) GUIResource.getInstance().getImagesSteps().get(pluginID);
    
                TreeItem ti = new TreeItem(tiMain, SWT.NONE);
                ti.setText(stepPlugin.getDescription());
                ti.setImage(image);
            }
        }
        
        tiMain.setExpanded(true);
    }

    /**
     * If you click in the tree, you might want to show the corresponding window.
     */
    private void showSelection()
    {
        TreeSelection[] objects = getTreeObjects(selectionTree);
        if (objects.length!=1) return; // not yet supported, we can do this later when the OSX bug goes away

        TreeSelection object = objects[0];
        
        final Object selection   = object.getSelection();
        final Object parent      = object.getParent();
        
        TransMeta transMeta = null;
        if (selection instanceof TransMeta) transMeta = (TransMeta) selection;
        if (parent instanceof TransMeta) transMeta = (TransMeta) parent;
        
        if (transMeta!=null)
        {
            // Search the corresponding SpoonGraph tab
            CTabItem tabItem = findCTabItem(makeGraphTabName(transMeta));
            if (tabItem!=null)
            {
                int current = tabfolder.getSelectionIndex();
                int desired = tabfolder.indexOf(tabItem); 
                if (current!=desired) tabfolder.setSelection(desired);
            }
        }
    }
    
    private void setMenu()
    {
        if (mCSH==null)
        {
            mCSH = new Menu(shell, SWT.POP_UP);
        }
        else
        {
            MenuItem[] items = mCSH.getItems();
            for (int i=0;i<items.length;i++) items[i].dispose();
        }
        
        TreeSelection[] objects = getTreeObjects(selectionTree);
        if (objects.length!=1) return; // not yet supported, we can do this later when the OSX bug goes away

        TreeSelection object = objects[0];
        
        final Object selection   = object.getSelection();
        final Object parent      = object.getParent();
        // final Object grandParent = object.getGrandParent();
                
        // No clicked on a real object: returns a class
        if (selection instanceof Class)
        {
            Class selClass = (Class) selection;
            System.out.println("Selection class: "+selClass.getName());
            
            if (selection.equals(TransHopMeta.class))
            {
                // New
                MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText(Messages.getString("Spoon.Menu.Popup.BASE.New"));
                miNew.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newHop((TransMeta)parent); }} );

                // Sort hops
                MenuItem miSort  = new MenuItem(mCSH, SWT.PUSH); miSort.setText(Messages.getString("Spoon.Menu.Popup.HOPS.SortHops"));
                miSort.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { ((TransMeta)parent).sortHops(); refreshTree(); } });
            }
            if (selection.equals(DatabaseMeta.class))
            {
                // New
                MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText(Messages.getString("Spoon.Menu.Popup.BASE.New"));
                miNew.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newConnection((TransMeta)parent); }} );

                // New Connection Wizard
                MenuItem miWizard  = new MenuItem(mCSH, SWT.PUSH); miWizard.setText(Messages.getString("Spoon.Menu.Popup.CONNECTIONS.NewConnectionWizard"));
                miWizard.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent arg0) { createDatabaseWizard((TransMeta)parent); } } );
                
                // Clear complete DB Cache
                MenuItem miCache  = new MenuItem(mCSH, SWT.PUSH); miCache.setText(Messages.getString("Spoon.Menu.Popup.CONNECTIONS.ClearDBCacheComplete"));
                miCache.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { clearDBCache((TransMeta)parent); } } );
            }
            if (selection.equals(PartitionSchema.class))
            {
                // New
                MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText(Messages.getString("Spoon.Menu.Popup.BASE.New"));
                miNew.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newDatabasePartitioningSchema((TransMeta)parent); }} );
            }
            if (selection.equals(ClusterSchema.class))
            {
                MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); 
                miNew.setText(Messages.getString("Spoon.Menu.Popup.CLUSTERS.New"));//New clustering schema
                miNew.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newClusteringSchema((TransMeta)parent); } } );
            }
            if (selection.equals(SlaveServer.class))
            {
                MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); 
                miNew.setText(Messages.getString("Spoon.Menu.Popup.SLAVE_SERVER.New"));// New slave server
                miNew.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newSlaveServer((TransMeta)parent); } } );
            }
        }
        else
        {
            if (selection instanceof StepPlugin)
            {
                // New
                MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText(Messages.getString("Spoon.Menu.Popup.BASE.New"));
                miNew.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newStep(getActiveTransformation()); }} );
            }

            if (selection instanceof DatabaseMeta)
            {
                final DatabaseMeta databaseMeta = (DatabaseMeta) selection;
                final TransMeta transMeta = (TransMeta) parent;
                
                MenuItem miNew  = new MenuItem(mCSH, SWT.PUSH); miNew.setText(Messages.getString("Spoon.Menu.Popup.CONNECTIONS.New"));//New
                miNew.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { newConnection((TransMeta)parent); } } );

                MenuItem miEdit = new MenuItem(mCSH, SWT.PUSH); miEdit.setText(Messages.getString("Spoon.Menu.Popup.CONNECTIONS.Edit"));//Edit
                miEdit.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editConnection(transMeta, databaseMeta); } } );
                
                MenuItem miDupe = new MenuItem(mCSH, SWT.PUSH); miDupe.setText(Messages.getString("Spoon.Menu.Popup.CONNECTIONS.Duplicate"));//Duplicate
                miDupe.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { dupeConnection(transMeta, databaseMeta); } } );
                
                MenuItem miCopy = new MenuItem(mCSH, SWT.PUSH); miCopy.setText(Messages.getString("Spoon.Menu.Popup.CONNECTIONS.CopyToClipboard"));//Copy to clipboard
                miCopy.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { clipConnection(transMeta, databaseMeta); } } );
                
                MenuItem miDel  = new MenuItem(mCSH, SWT.PUSH); miDel.setText(Messages.getString("Spoon.Menu.Popup.CONNECTIONS.Delete"));//Delete
                miDel.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delConnection(transMeta, databaseMeta); } } );
                
                new MenuItem(mCSH, SWT.SEPARATOR);
                
                MenuItem miSQL  = new MenuItem(mCSH, SWT.PUSH); miSQL.setText(Messages.getString("Spoon.Menu.Popup.CONNECTIONS.SQLEditor"));//SQL Editor
                miSQL.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { sqlConnection(transMeta, databaseMeta); } } );
                
                MenuItem miCache= new MenuItem(mCSH, SWT.PUSH); miCache.setText(Messages.getString("Spoon.Menu.Popup.CONNECTIONS.ClearDBCache")+databaseMeta.getName());//Clear DB Cache of 
                miCache.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { clearDBCache(transMeta, databaseMeta); } } );
                
                new MenuItem(mCSH, SWT.SEPARATOR);
                MenuItem miShare = new MenuItem(mCSH, SWT.PUSH); miShare.setText(Messages.getString("Spoon.Menu.Popup.CONNECTIONS.Share"));
                miShare.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { shareObject(databaseMeta); } } );
                
                new MenuItem(mCSH, SWT.SEPARATOR);
                MenuItem miExpl = new MenuItem(mCSH, SWT.PUSH); miExpl.setText(Messages.getString("Spoon.Menu.Popup.CONNECTIONS.Explore"));//Explore
                miExpl.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { exploreDB(transMeta, databaseMeta); } } );
                
                // disable for now if the connection is an SAP R/3 type of database...
                if (databaseMeta.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_SAPR3) miExpl.setEnabled(false);
                
            }
            if (selection instanceof StepMeta)
            {
                final TransMeta transMeta = (TransMeta)parent;
                final StepMeta stepMeta = (StepMeta)selection;
                
                // Edit
                MenuItem miEdit   = new MenuItem(mCSH, SWT.PUSH); miEdit.setText(Messages.getString("Spoon.Menu.Popup.STEPS.Edit"));
                miEdit.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editStep(transMeta, stepMeta); } } );

                // Duplicate
                MenuItem miDupe   = new MenuItem(mCSH, SWT.PUSH); miDupe.setText(Messages.getString("Spoon.Menu.Popup.STEPS.Duplicate"));
                miDupe.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { dupeStep(transMeta, stepMeta); } } );
                
                // Delete
                MenuItem miDel    = new MenuItem(mCSH, SWT.PUSH); miDel.setText(Messages.getString("Spoon.Menu.Popup.STEPS.Delete"));
                miDel.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delStep(transMeta, stepMeta); } } );
                
                MenuItem miShare = new MenuItem(mCSH, SWT.PUSH); miShare.setText(Messages.getString("Spoon.Menu.Popup.STEPS.Share"));
                miShare.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { shareObject(stepMeta); } } );
            }
            if (selection instanceof TransHopMeta)
            {
                final TransMeta transMeta = (TransMeta)parent;
                final TransHopMeta transHopMeta = (TransHopMeta)selection;
                
                MenuItem miEdit = new MenuItem(mCSH, SWT.PUSH); miEdit.setText(Messages.getString("Spoon.Menu.Popup.HOPS.Edit"));//Edit
                miEdit.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editHop(transMeta, transHopMeta); } } );
                
                MenuItem miDel  = new MenuItem(mCSH, SWT.PUSH); miDel.setText(Messages.getString("Spoon.Menu.Popup.HOPS.Delete"));//Delete
                miDel.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delHop(transMeta, transHopMeta); } } );
            }
            if (selection instanceof PartitionSchema)
            {
                final TransMeta transMeta = (TransMeta)parent;
                final PartitionSchema partitionSchema = (PartitionSchema)selection;
                
                MenuItem miEdit = new MenuItem(mCSH, SWT.PUSH); miEdit.setText(Messages.getString("Spoon.Menu.Popup.PARTITIONS.Edit"));//Edit
                miEdit.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editPartitionSchema(transMeta, partitionSchema); } } );
                
                MenuItem miDel  = new MenuItem(mCSH, SWT.PUSH); miDel.setText(Messages.getString("Spoon.Menu.Popup.PARTITIONS.Delete"));//Delete
                miDel.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delPartitionSchema(transMeta, partitionSchema); } } );
                
                MenuItem miShare = new MenuItem(mCSH, SWT.PUSH); miShare.setText(Messages.getString("Spoon.Menu.Popup.PARTITIONS.Share"));
                miShare.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { shareObject(partitionSchema); } } );
            }
            if (selection instanceof ClusterSchema)
            {
                final TransMeta transMeta = (TransMeta)parent;
                final ClusterSchema clusterSchema = (ClusterSchema)selection;
                
                MenuItem miEdit = new MenuItem(mCSH, SWT.PUSH); 
                miEdit.setText(Messages.getString("Spoon.Menu.Popup.CLUSTERS.Edit"));//Edit
                miEdit.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editClusterSchema(transMeta, clusterSchema); } } );

                MenuItem miDel  = new MenuItem(mCSH, SWT.PUSH); 
                miDel.setText(Messages.getString("Spoon.Menu.Popup.CLUSTERS.Delete"));//Delete
                miDel.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delClusterSchema(transMeta, clusterSchema); } } );
                
                MenuItem miShare = new MenuItem(mCSH, SWT.PUSH); 
                miShare.setText(Messages.getString("Spoon.Menu.Popup.CLUSTERS.Share"));
                miShare.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { shareObject(clusterSchema); } } );
                
                MenuItem miMonitor  = new MenuItem(mCSH, SWT.PUSH); 
                miMonitor.setText(Messages.getString("Spoon.Menu.Popup.CLUSTERS.Monitor"));//New
                miMonitor.addListener( SWT.Selection, new Listener() { public void handleEvent(Event e) { monitorClusterSchema(transMeta, clusterSchema); } } );   
            }
            
            // Right click on a slave server
            if (selection instanceof SlaveServer)
            {
                final TransMeta transMeta = (TransMeta)parent;
                final SlaveServer slaveServer = (SlaveServer)selection;
                
                MenuItem miEdit  = new MenuItem(mCSH, SWT.PUSH); 
                miEdit.setText(Messages.getString("Spoon.Menu.Popup.SLAVE_SERVER.Edit"));//New
                miEdit.addListener( SWT.Selection, new Listener() { public void handleEvent(Event e) { editSlaveServer(transMeta, slaveServer); } } );   

                MenuItem miDel  = new MenuItem(mCSH, SWT.PUSH); 
                miDel.setText(Messages.getString("Spoon.Menu.Popup.SLAVE_SERVER.Delete"));//Delete
                miDel.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delSlaveServer(transMeta, slaveServer); } } );

                MenuItem miMonitor  = new MenuItem(mCSH, SWT.PUSH); 
                miMonitor.setText(Messages.getString("Spoon.Menu.Popup.SLAVE_SERVER.Monitor"));//New
                miMonitor.addListener( SWT.Selection, new Listener() { public void handleEvent(Event e) { addSpoonSlave(slaveServer); } } );   

            }
        }
        selectionTree.setMenu(mCSH);
    }
    
    /**
     * Reaction to double click
     *
     */
    private void editSelected()
    {
        
        TreeSelection[] objects = getTreeObjects(selectionTree);
        if (objects.length!=1) return; // not yet supported, we can do this later when the OSX bug goes away

        TreeSelection object = objects[0];
        
        final Object selection   = object.getSelection();
        final Object parent      = object.getParent();
                
        if (selection instanceof Class)
        {
            if (selection.equals(TransHopMeta.class)) newHop((TransMeta)parent);
            if (selection.equals(DatabaseMeta.class)) newConnection((TransMeta)parent);
            if (selection.equals(PartitionSchema.class)) newDatabasePartitioningSchema((TransMeta)parent);
            if (selection.equals(ClusterSchema.class)) newClusteringSchema((TransMeta)parent);
            if (selection.equals(SlaveServer.class)) newSlaveServer((TransMeta)parent);
        }
        else
        {
            if (selection instanceof StepPlugin) newStep(getActiveTransformation());
            if (selection instanceof DatabaseMeta) editConnection((TransMeta) parent, (DatabaseMeta) selection);
            if (selection instanceof StepMeta) editStep((TransMeta)parent, (StepMeta)selection);
            if (selection instanceof TransHopMeta) editHop((TransMeta)parent, (TransHopMeta)selection);
            if (selection instanceof PartitionSchema) editPartitionSchema((TransMeta)parent, (PartitionSchema)selection);
            if (selection instanceof ClusterSchema) editClusterSchema((TransMeta)parent, (ClusterSchema)selection);
            if (selection instanceof SlaveServer) editSlaveServer((TransMeta)parent, (SlaveServer)selection);
        }
    }
    
    protected void monitorClusterSchema(TransMeta transMeta, ClusterSchema clusterSchema)
    {
        for (int i=0;i<clusterSchema.getSlaveServers().size();i++)
        {
            SlaveServer slaveServer = (SlaveServer) clusterSchema.getSlaveServers().get(i);
            addSpoonSlave(slaveServer);
        }
    }
    
    protected void editSlaveServer(TransMeta transMeta, SlaveServer slaveServer)
    {
        SlaveServerDialog dialog = new SlaveServerDialog(shell, slaveServer);
        if (dialog.open())
        {
            refreshTree();
            refreshGraph();
        }
    }
    

    private void addTabs()
    {
        if (tabComp!=null)
        {
            tabComp.dispose();
        }
        
        tabComp = new Composite(sashform, SWT.BORDER );
        props.setLook(tabComp);
        
        tabComp.setLayout(new GridLayout());
        
        tabfolder= new CTabFolder(tabComp, SWT.BORDER);
        tabfolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        props.setLook(tabfolder, Props.WIDGET_STYLE_TAB);
        
        tabfolder.setSimple(false);
        tabfolder.setUnselectedImageVisible(true);
        tabfolder.setUnselectedCloseVisible(true);
        
        tabfolder.addKeyListener(defKeys);
        tabfolder.addKeyListener(modKeys);
        
        // tabfolder.setSelection(0);
        
        sashform.addKeyListener(defKeys);
        sashform.addKeyListener(modKeys);
        
        int weights[] = props.getSashWeights();
        sashform.setWeights(weights);
        sashform.setVisible(true);      
        
        tabfolder.addCTabFolder2Listener(new CTabFolder2Adapter() 
            {
                public void close(CTabFolderEvent event) 
                {
                    // Try to find the tab-item that's being closed.
                    Collection collection = tabMap.values();
                    for (Iterator iter = collection.iterator(); iter.hasNext();)
                    {
                        TabMapEntry entry = (TabMapEntry) iter.next();
                        if (event.item.equals(entry.getTabItem())) 
                        {
                            // Can we close this tab?
                            event.doit = entry.getObject().canBeClosed();
                            
                            // Also clean up the log/history associated with this transformation
                            //
                            if (event.doit && entry.getObject() instanceof SpoonGraph)
                            {
                                TransMeta transMeta = (TransMeta)entry.getObject().getManagedObject();
                                closeTransformation(transMeta);
                                refreshTree();
                            }
                        }
                    }
                }
            }
        );
    }
    
    public String getRepositoryName()
    {
        if (rep==null) return null;
        return rep.getRepositoryInfo().getName();
    }

    public void sqlConnection(TransMeta transMeta, DatabaseMeta databaseMeta)
    {
        SQLEditor sql = new SQLEditor(shell, SWT.NONE, databaseMeta, transMeta.getDbCache(), "");
        sql.open();
    }
    
    public void editConnection(TransMeta transMeta, DatabaseMeta databaseMeta)
    {
        DatabaseMeta before = (DatabaseMeta)databaseMeta.clone();

        DatabaseDialog con = new DatabaseDialog(shell, SWT.NONE, log, databaseMeta, props);
        con.setDatabases(transMeta.getDatabases());
        String newname = con.open(); 
        if (!Const.isEmpty(newname))  // null: CANCEL
        {                
            // newname = db.verifyAndModifyDatabaseName(transMeta.getDatabases(), name);
            
            // Store undo/redo information
            DatabaseMeta after = (DatabaseMeta)databaseMeta.clone();
            addUndoChange(transMeta, new DatabaseMeta[] { before }, new DatabaseMeta[] { after }, new int[] { transMeta.indexOfDatabase(databaseMeta) } );
            
            saveConnection(databaseMeta);
            
            // The connection is saved, clear the changed flag.
            databaseMeta.setChanged(false);
            
            refreshTree();
        }
        setShellText();
    }

    public void dupeConnection(TransMeta transMeta, DatabaseMeta databaseMeta)
    {
        String name = databaseMeta.getName();
        int pos = transMeta.indexOfDatabase(databaseMeta);                
        if (databaseMeta!=null)
        {
            DatabaseMeta copy = (DatabaseMeta)databaseMeta.clone();
            String dupename = Messages.getString("Spoon.Various.DupeName") +name; //"(copy of) "
            copy.setName(dupename);

            DatabaseDialog con = new DatabaseDialog(shell, SWT.NONE, log, copy, props);
            String newname = con.open(); 
            if (newname != null)  // null: CANCEL
            {
                copy.verifyAndModifyDatabaseName(transMeta.getDatabases(), name);
                transMeta.addDatabase(pos+1, copy);
                addUndoNew(transMeta, new DatabaseMeta[] { (DatabaseMeta)copy.clone() }, new int[] { pos+1 });
                saveConnection(copy);             
                refreshTree();
            }
        }
    }
    
    public void clipConnection(TransMeta transMeta, DatabaseMeta databaseMeta)
    {
        String xml = XMLHandler.getXMLHeader() + databaseMeta.getXML();
        toClipboard(xml);
    }


    /**
     * Delete a database connection
     * @param name The name of the database connection.
     */
    public void delConnection(TransMeta transMeta, DatabaseMeta db)
    {
        int pos = transMeta.indexOfDatabase(db);                
        boolean worked=false;
        
        // delete from repository?
        if (rep!=null)
        {
            if (!rep.getUserInfo().isReadonly())
            {
                try
                {
                    long id_database = rep.getDatabaseID(db.getName());
                    rep.delDatabase(id_database);
        
                    worked=true;
                }
                catch(KettleDatabaseException dbe)
                {
                    
                    new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorDeletingConnection.Title"), Messages.getString("Spoon.Dialog.ErrorDeletingConnection.Message",db.getName()), dbe);//"Error deleting connection ["+db+"] from repository!"
                }
            }
            else
            {
                new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorDeletingConnection.Title"),Messages.getString("Spoon.Dialog.ErrorDeletingConnection.Message",db.getName()) , new KettleException(Messages.getString("Spoon.Dialog.Exception.ReadOnlyUser")));//"Error deleting connection ["+db+"] from repository!" //This user is read-only!
            }
        }

        if (rep==null || worked)
        {
            addUndoDelete(transMeta, new DatabaseMeta[] { (DatabaseMeta)db.clone() }, new int[] { pos });
            transMeta.removeDatabase(pos);
        }

        refreshTree();
        setShellText();
    }
    
    public String editStep(TransMeta transMeta, StepMeta stepMeta)
    {
        try
        {
            String name = stepMeta.getName();

            // Before we do anything, let's store the situation the way it was...
            StepMeta before = (StepMeta) stepMeta.clone();
            StepMetaInterface stepint = stepMeta.getStepMetaInterface();
            StepDialogInterface dialog = stepint.getDialog(shell, stepMeta.getStepMetaInterface(), transMeta, name);
            dialog.setRepository(rep);
            String stepname = dialog.open();

            if (stepname != null)
            {
                // OK, so the step has changed...
                //
                // First, backup the situation for undo/redo
                StepMeta after = (StepMeta) stepMeta.clone();
                addUndoChange(transMeta, new StepMeta[] { before }, new StepMeta[] { after }, new int[] { transMeta.indexOfStep(stepMeta) });

                // Then, store the size of the
                // See if the new name the user enter, doesn't collide with another step.
                // If so, change the stepname and warn the user!
                //
                String newname = stepname;
                StepMeta smeta = transMeta.findStep(newname, stepMeta);
                int nr = 2;
                while (smeta != null)
                {
                    newname = stepname + " " + nr;
                    smeta = transMeta.findStep(newname);
                    nr++;
                }
                if (nr > 2)
                {
                    stepname = newname;
                    MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
                    mb.setMessage(Messages.getString("Spoon.Dialog.StepnameExists.Message", stepname)); // $NON-NLS-1$
                    mb.setText(Messages.getString("Spoon.Dialog.StepnameExists.Title")); // $NON-NLS-1$
                    mb.open();
                }
                stepMeta.setName(stepname);
                refreshTree(); // Perhaps new connections were created in the step dialog.
            }
            else
            {
                // Scenario: change connections and click cancel...
                // Perhaps new connections were created in the step dialog?
                if (transMeta.haveConnectionsChanged())
                {
                    refreshTree();
                }
            }
            refreshGraph(); // name is displayed on the graph too.
            setShellText();
        }
        catch (Throwable e)
        {
            if (shell.isDisposed()) return null;
            new ErrorDialog(shell, Messages.getString("Spoon.Dialog.UnableOpenDialog.Title"), Messages
                    .getString("Spoon.Dialog.UnableOpenDialog.Message"), new Exception(e));//"Unable to open dialog for this step"
        }
        
        return stepMeta.getName();
    }

    public void dupeStep(TransMeta transMeta, StepMeta stepMeta)
    {
        log.logDebug(toString(), Messages.getString("Spoon.Log.DuplicateStep")+stepMeta.getName());//Duplicate step: 
        
        StepMeta stMeta = (StepMeta)stepMeta.clone();
        if (stMeta!=null)
        {
            String newname = transMeta.getAlternativeStepname(stepMeta.getName());
            int nr=2;
            while (transMeta.findStep(newname)!=null)
            {
                newname = stepMeta.getName()+" (copy "+nr+")";
                nr++;
            }
            stMeta.setName(newname);
            // Don't select this new step!
            stMeta.setSelected(false);
            Point loc = stMeta.getLocation();
            stMeta.setLocation(loc.x+20, loc.y+20);
            transMeta.addStep(stMeta);
            addUndoNew(transMeta, new StepMeta[] { (StepMeta)stMeta.clone() }, new int[] { transMeta.indexOfStep(stMeta) });
            refreshTree();
            refreshGraph();
        }
    }

    public void clipStep(TransMeta transMeta, StepMeta stepMeta)
    {
        String xml = stepMeta.getXML();
        toClipboard(xml);
    }

    public void pasteXML(TransMeta transMeta, String clipcontent, Point loc)
    {
        try
        {
            //System.out.println(clipcontent);
            
            Document doc = XMLHandler.loadXMLString(clipcontent);
            Node transnode = XMLHandler.getSubNode(doc, "transformation");
            // De-select all, re-select pasted steps...
            transMeta.unselectAll();
            
            Node stepsnode = XMLHandler.getSubNode(transnode, "steps");
            int nr = XMLHandler.countNodes(stepsnode, "step");
            log.logDebug(toString(), Messages.getString("Spoon.Log.FoundSteps",""+nr)+loc);//"I found "+nr+" steps to paste on location: "
            StepMeta steps[] = new StepMeta[nr];
            
            //Point min = new Point(loc.x, loc.y);
            Point min = new Point(99999999,99999999);

            // Load the steps...
            for (int i=0;i<nr;i++)
            {
                Node stepnode = XMLHandler.getSubNodeByNr(stepsnode, "step", i);
                steps[i] = new StepMeta(stepnode, transMeta.getDatabases(), transMeta.getCounters());

                if (loc!=null)
                {
                    Point p = steps[i].getLocation();
                    
                    if (min.x > p.x) min.x = p.x;
                    if (min.y > p.y) min.y = p.y;
                }
            }
            
            // Load the hops...
            Node hopsnode = XMLHandler.getSubNode(transnode, "order");
            nr = XMLHandler.countNodes(hopsnode, "hop");
            log.logDebug(toString(), Messages.getString("Spoon.Log.FoundHops",""+nr));//"I found "+nr+" hops to paste."
            TransHopMeta hops[] = new TransHopMeta[nr];
            
            ArrayList alSteps = new ArrayList();
            for (int i=0;i<steps.length;i++) alSteps.add(steps[i]);
            
            for (int i=0;i<nr;i++)
            {
                Node hopnode = XMLHandler.getSubNodeByNr(hopsnode, "hop", i);
                hops[i] = new TransHopMeta(hopnode, alSteps);
            }

            // What's the difference between loc and min?
            // This is the offset:
            Point offset = new Point(loc.x-min.x, loc.y-min.y);
            
            // Undo/redo object positions...
            int position[] = new int[steps.length];
            
            for (int i=0;i<steps.length;i++)
            {
                Point p = steps[i].getLocation();
                String name = steps[i].getName();
                
                steps[i].setLocation(p.x+offset.x, p.y+offset.y);
                steps[i].setDraw(true);
                
                // Check the name, find alternative...
                steps[i].setName( transMeta.getAlternativeStepname(name) );
                transMeta.addStep(steps[i]);
                position[i] = transMeta.indexOfStep(steps[i]);
            }
            
            // Add the hops too...
            for (int i=0;i<hops.length;i++)
            {
                transMeta.addTransHop(hops[i]);
            }

            // Load the notes...
            Node notesnode = XMLHandler.getSubNode(transnode, "notepads");
            nr = XMLHandler.countNodes(notesnode, "notepad");
            log.logDebug(toString(), Messages.getString("Spoon.Log.FoundNotepads",""+nr));//"I found "+nr+" notepads to paste."
            NotePadMeta notes[] = new NotePadMeta[nr];
            
            for (int i=0;i<notes.length;i++)
            {
                Node notenode = XMLHandler.getSubNodeByNr(notesnode, "notepad", i);
                notes[i] = new NotePadMeta(notenode);
                Point p = notes[i].getLocation();
                notes[i].setLocation(p.x+offset.x, p.y+offset.y);
                transMeta.addNote(notes[i]);
            }
            
            // Set the source and target steps ...
            for (int i=0;i<steps.length;i++)
            {
                StepMetaInterface smi = steps[i].getStepMetaInterface();
                smi.searchInfoAndTargetSteps(transMeta.getSteps());
            }
            
            // Save undo information too...
            addUndoNew(transMeta, steps, position, false);

            int hoppos[] = new int[hops.length];
            for (int i=0;i<hops.length;i++) hoppos[i] = transMeta.indexOfTransHop(hops[i]);
            addUndoNew(transMeta, hops, hoppos, true);
            
            int notepos[] = new int[notes.length];
            for (int i=0;i<notes.length;i++) notepos[i] = transMeta.indexOfNote(notes[i]);
            addUndoNew(transMeta, notes, notepos, true);
    
            if (transMeta.haveStepsChanged())
            {
                refreshTree();
                refreshGraph();
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("Spoon.Dialog.UnablePasteSteps.Title"),Messages.getString("Spoon.Dialog.UnablePasteSteps.Message") , e);//"Error pasting steps...", "I was unable to paste steps to this transformation"
        }
    }
    
    public void copySelected(TransMeta transMeta, StepMeta stepMeta[], NotePadMeta notePadMeta[])
    {
        if (stepMeta==null || stepMeta.length==0) return;
        
        String xml = XMLHandler.getXMLHeader();
        xml+="<transformation>"+Const.CR;
        xml+=" <steps>"+Const.CR;

        for (int i=0;i<stepMeta.length;i++)
        {
            xml+=stepMeta[i].getXML();
        }
        
        xml+="    </steps>"+Const.CR;
        
        // 
        // Also check for the hops in between the selected steps...
        //
        
        xml+="<order>"+Const.CR;
        if (stepMeta!=null)
        for (int i=0;i<stepMeta.length;i++)
        {
            for (int j=0;j<stepMeta.length;j++)
            {
                if (i!=j)
                {
                    TransHopMeta hop = transMeta.findTransHop(stepMeta[i], stepMeta[j]); 
                    if (hop!=null) // Ok, we found one...
                    {
                        xml+=hop.getXML()+Const.CR;
                    }
                }
            }
        }
        xml+="  </order>"+Const.CR;
        
        xml+="  <notepads>"+Const.CR;
        if (notePadMeta!=null)
        for (int i=0;i<notePadMeta.length;i++)
        {
            xml+= notePadMeta[i].getXML();
        }
        xml+="   </notepads>"+Const.CR; 

        xml+=" </transformation>"+Const.CR;
        
        toClipboard(xml);
    }

    public void delStep(TransMeta transMeta, StepMeta stepMeta)
    {
        log.logDebug(toString(), Messages.getString("Spoon.Log.DeleteStep")+stepMeta.getName());//"Delete step: "
        
        for (int i=transMeta.nrTransHops()-1;i>=0;i--)
        {
            TransHopMeta hi = transMeta.getTransHop(i);
            if ( hi.getFromStep().equals(stepMeta) || hi.getToStep().equals(stepMeta) )
            {
                addUndoDelete(transMeta, new TransHopMeta[] { hi }, new int[] { transMeta.indexOfTransHop(hi) }, true);
                transMeta.removeTransHop(i);
                refreshTree();
            }
        }
        
        int pos = transMeta.indexOfStep(stepMeta);
        transMeta.removeStep(pos);
        addUndoDelete(transMeta, new StepMeta[] { stepMeta }, new int[] { pos });
        
        refreshTree();
        refreshGraph();
    }   

    public void editHop(TransMeta transMeta, TransHopMeta transHopMeta)
    {
        // Backup situation BEFORE edit:
        String name = transHopMeta.toString();
        TransHopMeta before = (TransHopMeta)transHopMeta.clone();
        
        TransHopDialog hd = new TransHopDialog(shell, SWT.NONE, transHopMeta, transMeta);
        if (hd.open()!=null)
        {
            // Backup situation for redo/undo:
            TransHopMeta after = (TransHopMeta)transHopMeta.clone();
            addUndoChange(transMeta, new TransHopMeta[] { before }, new TransHopMeta[] { after }, new int[] { transMeta.indexOfTransHop(transHopMeta) } );
        
            String newname = transHopMeta.toString();
            if (!name.equalsIgnoreCase(newname)) 
            {
                refreshTree(); 
                refreshGraph(); // color, nr of copies...
            }
        }
        setShellText();
    }

    public void delHop(TransMeta transMeta, TransHopMeta transHopMeta)
    {
        int i = transMeta.indexOfTransHop(transHopMeta);
        addUndoDelete(transMeta, new Object[] { (TransHopMeta)transHopMeta.clone() }, new int[] { transMeta.indexOfTransHop(transHopMeta) });
        transMeta.removeTransHop(i);
        refreshTree();
        refreshGraph();
    }

    public void newHop(TransMeta transMeta, StepMeta fr, StepMeta to)
    {
        TransHopMeta hi = new TransHopMeta(fr, to);
        
        TransHopDialog hd = new TransHopDialog(shell, SWT.NONE, hi, transMeta);
        if (hd.open()!=null)
        {
            boolean error=false;

            if (transMeta.findTransHop(hi.getFromStep(), hi.getToStep())!=null)
            {
                MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                mb.setMessage(Messages.getString("Spoon.Dialog.HopExists.Message"));//"This hop already exists!"
                mb.setText(Messages.getString("Spoon.Dialog.HopExists.Title"));//Error!
                mb.open();
                error=true;
            }
            
            if (transMeta.hasLoop(fr) || transMeta.hasLoop(to))
            {
                refreshTree();
                refreshGraph();
                
                MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING );
                mb.setMessage(Messages.getString("Spoon.Dialog.AddingHopCausesLoop.Message"));//Adding this hop causes a loop in the transformation.  Loops are not allowed!
                mb.setText(Messages.getString("Spoon.Dialog.AddingHopCausesLoop.Title"));//Warning!
                mb.open();
                error=true;
            }
            
            if (!error)
            {
                transMeta.addTransHop(hi);
                addUndoNew(transMeta, new TransHopMeta[] { (TransHopMeta)hi.clone() }, new int[] { transMeta.indexOfTransHop(hi) });
                hi.getFromStep().drawStep();
                hi.getToStep().drawStep();
                refreshTree();
                refreshGraph();
                
                // Check if there are 2 hops coming out of the "From" step.
                verifyCopyDistribute(transMeta, fr);
            }
        }
    }

    public void verifyCopyDistribute(TransMeta transMeta, StepMeta fr)
    {
        int nrNextSteps = transMeta.findNrNextSteps(fr);
        
        // don't show it for 3 or more hops, by then you should have had the message
        if (nrNextSteps==2) 
        {
            boolean distributes = false;
            
            if (props.showCopyOrDistributeWarning())
            {
                MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
                        Messages.getString("System.Warning"),//"Warning!" 
                        null,
                        Messages.getString("Spoon.Dialog.CopyOrDistribute.Message", fr.getName(), Integer.toString(nrNextSteps)),
                        MessageDialog.WARNING,
                        new String[] { Messages.getString("Spoon.Dialog.CopyOrDistribute.Copy"), Messages.getString("Spoon.Dialog.CopyOrDistribute.Distribute") },//"Copy Distribute 
                        0,
                        Messages.getString("Spoon.Message.Warning.NotShowWarning"),//"Please, don't show this warning anymore."
                        !props.showCopyOrDistributeWarning()
                    );
                int idx = md.open();
                props.setShowCopyOrDistributeWarning(!md.getToggleState());
                props.saveProps();
                
                distributes = (idx&0xFF)==1;
            }
               
            if (distributes)
            {
                fr.setDistributes(true);
            }
            else
            {
                fr.setDistributes(false);
            }
            
            refreshTree();
            refreshGraph();
        }
    }

    public void newHop(TransMeta transMeta)
    {
        newHop(transMeta, null, null);
    }
    
    public void newConnection(TransMeta transMeta)
    {
        DatabaseMeta db = new DatabaseMeta(); 
        DatabaseDialog con = new DatabaseDialog(shell, SWT.APPLICATION_MODAL, log, db, props);
        String con_name = con.open(); 
        if (!Const.isEmpty(con_name))
        {
            db.verifyAndModifyDatabaseName(transMeta.getDatabases(), null);
            transMeta.addDatabase(db);
            addUndoNew(transMeta, new DatabaseMeta[] { (DatabaseMeta)db.clone() }, new int[] { transMeta.indexOfDatabase(db) });
            saveConnection(db);
            refreshTree();
        }
    }
    
    public void saveConnection(DatabaseMeta db)
    {
        // Also add to repository?
        if (rep!=null)
        {
            if (!rep.userinfo.isReadonly())
            {
                try
                {
                    db.saveRep(rep);
                    log.logDetailed(toString(), Messages.getString("Spoon.Log.SavedDatabaseConnection",db.getDatabaseName()));//"Saved database connection ["+db+"] to the repository."
                    
                    // Put a commit behind it!
                    rep.commit();
                    
                    db.setChanged(false);
                }
                catch(KettleException ke)
                {
                    rep.rollback(); // In case of failure: undo changes!
                    new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorSavingConnection.Title"),Messages.getString("Spoon.Dialog.ErrorSavingConnection.Message",db.getDatabaseName()), ke);//"Can't save...","Error saving connection ["+db+"] to repository!"
                }
            }
            else
            {
                new ErrorDialog(shell, Messages.getString("Spoon.Dialog.UnableSave.Title"),Messages.getString("Spoon.Dialog.ErrorSavingConnection.Message",db.getDatabaseName()), new KettleException(Messages.getString("Spoon.Dialog.Exception.ReadOnlyRepositoryUser")));//This repository user is read-only!
            }
        }
    }
    
    public void openRepository()
    {
        int perms[] = new int[] { PermissionMeta.TYPE_PERMISSION_TRANSFORMATION };
        RepositoriesDialog rd = new RepositoriesDialog(disp, SWT.NONE, perms, APP_NAME);
        rd.getShell().setImage(GUIResource.getInstance().getImageSpoon());
        if (rd.open())
        {
            // Close previous repository...
            
            if (rep!=null) 
            {
                rep.disconnect();
            }
            
            rep = new Repository(log, rd.getRepository(), rd.getUser());
            try
            {
                rep.connect(APP_NAME);
            }
            catch(KettleException ke)
            {
                rep=null;
                new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorConnectingRepository.Title"), Messages.getString("Spoon.Dialog.ErrorConnectingRepository.Message",Const.CR), ke); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            TransMeta transMetas[] = getLoadedTransformations();
            for (int t=0;t<transMetas.length;t++)
            {
                TransMeta transMeta = transMetas[t];
                
                for (int i=0;i<transMeta.nrDatabases();i++) 
                {
                    transMeta.getDatabase(i).setID(-1L);
                }
            
                // Set for the existing transformation the ID at -1!
                transMeta.setID(-1L);

                // Keep track of the old databases for now.
                ArrayList oldDatabases = transMeta.getDatabases();
            
                // In order to re-match the databases on name (not content), we need to load the databases from the new repository.
                // NOTE: for purposes such as DEVELOP - TEST - PRODUCTION sycles.
                
                // first clear the list of databases, partition schemas, slave servers, clusters
                transMeta.setDatabases(new ArrayList());
                transMeta.setPartitionSchemas(new ArrayList());
                transMeta.setSlaveServers(new ArrayList());
                transMeta.setClusterSchemas(new ArrayList());
    
                // Read them from the new repository.
                try
                {
                    transMeta.readSharedObjects(rep);
                }
                catch(KettleException e)
                {
                    new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Title"), Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Message", makeGraphTabName(transMeta)), e);
                }
            
                // Then we need to re-match the databases at save time...
                for (int i=0;i<oldDatabases.size();i++)
                {
                    DatabaseMeta oldDatabase = (DatabaseMeta) oldDatabases.get(i);
                    DatabaseMeta newDatabase = Const.findDatabase(transMeta.getDatabases(), oldDatabase.getName());
                    
                    // If it exists, change the settings...
                    if (newDatabase!=null)
                    {
                        // System.out.println("Found the new database in the repository ["+oldDatabase.getName()+"]");
                        // A database connection with the same name exists in the new repository.
                        // Change the old connections to reflect the settings in the new repository 
                        oldDatabase.setDatabaseInterface(newDatabase.getDatabaseInterface());
                    }
                    else
                    {
                        // System.out.println("Couldn't find the new database in the repository ["+oldDatabase.getName()+"]");
                        // The old database is not present in the new repository: simply add it to the list.
                        // When the transformation gets saved, it will be added to the repository.
                        transMeta.addDatabase(oldDatabase);
                    }
                }
                
                // For the existing transformation, change the directory too:
                // Try to find the same directory in the new repository...
                RepositoryDirectory redi = rep.getDirectoryTree().findDirectory(transMeta.getDirectory().getPath());
                if (redi!=null)
                {
                    transMeta.setDirectory(redi);
                }
                else
                {
                    transMeta.setDirectory(rep.getDirectoryTree()); // the root is the default!
                }
            }
            
            refreshTree();
            setShellText();
        }
        else
        {
            // Not cancelled? --> Clear repository...
            if (!rd.isCancelled())
            {
                closeRepository();
            }
        }
    }
    
    public void exploreRepository()
    {
        if (rep!=null)
        {
            RepositoryExplorerDialog erd = new RepositoryExplorerDialog(shell, SWT.NONE, rep, rep.getUserInfo());
            String objname = erd.open();
            if (objname!=null)
            {
                String object_type = erd.getObjectType();
                RepositoryDirectory repdir = erd.getObjectDirectory();
                
                // System.out.println("Load ["+object_type+"] --> ["+objname+"] from dir ["+(repdir==null)+"]");
                
                // Try to open it as a transformation.
                if (object_type.equals(RepositoryExplorerDialog.STRING_TRANSFORMATIONS))
                {
                    try
                    {
                        TransMeta transMeta = new TransMeta(rep, objname, repdir);
                        transMeta.clearChanged();
                        transMeta.setFilename(objname);
                        addSpoonGraph(transMeta);
                        refreshTree();
                        refreshGraph();
                    }
                    catch(KettleException e)
                    {
                        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                        mb.setMessage(Messages.getString("Spoon.Dialog.ErrorOpening.Message")+objname+Const.CR+e.getMessage());//"Error opening : "
                        mb.setText(Messages.getString("Spoon.Dialog.ErrorOpening.Title"));
                        mb.open();
                    }
                }
            }
        }
    }
    
    public void editRepositoryUser()
    {
        if (rep!=null)
        {
            UserInfo userinfo = rep.getUserInfo();
            UserDialog ud = new UserDialog(shell, SWT.NONE, log, props, rep, userinfo);
            UserInfo ui = ud.open();
            if (!userinfo.isReadonly())
            {
                if (ui!=null)
                {
                    try
                    {
                        ui.saveRep(rep);
                    }
                    catch(KettleException e)
                    {
                        MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
                        mb.setMessage(Messages.getString("Spoon.Dialog.UnableChangeUser.Message")+Const.CR+e.getMessage());//Sorry, I was unable to change this user in the repository: 
                        mb.setText(Messages.getString("Spoon.Dialog.UnableChangeUser.Title"));//"Edit user"
                        mb.open();
                    }
                }
            }
            else
            {
                MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
                mb.setMessage(Messages.getString("Spoon.Dialog.NotAllowedChangeUser.Message"));//"Sorry, you are not allowed to change this user."
                mb.setText(Messages.getString("Spoon.Dialog.NotAllowedChangeUser.Title"));
                mb.open();
            }       
        }
    }
    
    public void closeRepository()
    {
        if (rep!=null) rep.disconnect();
        rep = null;
        setShellText();
    }

    public void openFile(boolean importfile)
    {
        if (rep==null || importfile)  // Load from XML
        {
            FileDialog dialog = new FileDialog(shell, SWT.OPEN);
            // dialog.setFilterPath("C:\\Projects\\kettle\\source\\");
            dialog.setFilterExtensions(Const.STRING_TRANS_FILTER_EXT);
            dialog.setFilterNames(Const.STRING_TRANS_FILTER_NAMES);
            String fname = dialog.open();
            if (fname!=null)
            {
                try
                {
                    TransMeta transMeta = new TransMeta(fname, rep);
                    props.addLastFile(Props.TYPE_PROPERTIES_SPOON, fname, null, false, null);
                    addMenuLast();
                    if (!importfile) transMeta.clearChanged();
                    transMeta.setFilename(fname);
                    addSpoonGraph(transMeta);
                }
                catch(KettleException e)
                {
                    MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                    mb.setMessage(Messages.getString("Spoon.Dialog.ErrorOpening.Message")+fname+Const.CR+e.getMessage());//"Error opening : "
                    mb.setText(Messages.getString("Spoon.Dialog.ErrorOpening.Title"));//"Error!"
                    mb.open();
                }

                refreshGraph();
                refreshTree();
                refreshHistory();
            }
        }
        else // Read a transformation from the repository!
        {
            SelectObjectDialog sod = new SelectObjectDialog(shell, props, rep, true, false, false);
            String transname            = sod.open();
            RepositoryDirectory repdir = sod.getDirectory();
            if (transname!=null && repdir!=null)
            {
                TransLoadProgressDialog tlpd = new TransLoadProgressDialog(shell, rep, transname, repdir);
                TransMeta transMeta = tlpd.open();
                if (transMeta!=null)
                {
                    // transMeta = new TransInfo(log, rep, transname, repdir);
                    log.logDetailed(toString(),Messages.getString("Spoon.Log.LoadToTransformation",transname,repdir.getDirectoryName()) );//"Transformation ["+transname+"] in directory ["+repdir+"] loaded from the repository."
                    //System.out.println("name="+transMeta.getName());
                    props.addLastFile(Props.TYPE_PROPERTIES_SPOON, transname, repdir.getPath(), true, rep.getName());
                    addMenuLast();
                    transMeta.clearChanged();
                    transMeta.setFilename(transname);
                    addSpoonGraph(transMeta);
                }
                refreshGraph();
                refreshTree();
                refreshHistory();
            }
        }
    }
    
    public void newFile()
    {
        TransMeta transMeta = new TransMeta();
        
        loadRepositoryObjects(transMeta);
        
        addSpoonGraph(transMeta);
        refreshTree();
    }
    
    public void loadRepositoryObjects(TransMeta transMeta)
    {
        // Load common database info from active repository...
        if (rep!=null)
        {
            try
            {
                transMeta.readSharedObjects(rep);
            }
            catch(Exception e)
            {
                new ErrorDialog(shell, Messages.getString("Spoon.Error.UnableToLoadSharedObjects.Title"), Messages.getString("Spoon.Error.UnableToLoadSharedObjects.Message"), e);
            }

        }
    }
    
    public boolean quitFile()
    {
        log.logDetailed(toString(), Messages.getString("Spoon.Log.QuitApplication"));//"Quit application."

        boolean exit        = true;
        
        saveSettings();

        if (props.showExitWarning())
        {
            MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
                    Messages.getString("System.Warning"),//"Warning!" 
                    null,
                    Messages.getString("Spoon.Message.Warning.PromptExit"), // Are you sure you want to exit?
                    MessageDialog.WARNING,
                    new String[] { Messages.getString("Spoon.Message.Warning.Yes"), Messages.getString("Spoon.Message.Warning.No") },//"Yes", "No" 
                    1,
                    Messages.getString("Spoon.Message.Warning.NotShowWarning"),//"Please, don't show this warning anymore."
                    !props.showExitWarning()
              );
            int idx = md.open();
            props.setExitWarningShown(!md.getToggleState());
            props.saveProps();
            if ((idx&0xFF)==1) return false; // No selected: don't exit!
        }
           
        
        
        // Check all tabs to see if we can close them...
        List list = new ArrayList();
        list.addAll(tabMap.values());
        
        for (Iterator iter = list.iterator(); iter.hasNext() && exit;)
        {
            TabMapEntry mapEntry = (TabMapEntry) iter.next();
            
            if (!mapEntry.getObject().canBeClosed())
            {
                // Unsaved transformation?
                //
                if (mapEntry.getObject() instanceof SpoonGraph)
                {
                    TransMeta transMeta = (TransMeta) mapEntry.getObject().getManagedObject();
                    if (transMeta.hasChanged())
                    {
                        // Show the transformation in question
                        //
                        tabfolder.setSelection( mapEntry.getTabItem() );
                        
                        // Ask if we should save it before closing...
                        MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING );
                        mb.setMessage(Messages.getString("Spoon.Dialog.SaveChangedFile.Message", makeGraphTabName(transMeta)));//"File has changed!  Do you want to save first?"
                        mb.setText(Messages.getString("Spoon.Dialog.SaveChangedFile.Title"));//"Warning!"
                        int answer = mb.open();
                    
                        switch(answer)
                        {
                        case SWT.YES: exit=saveFile(transMeta); break;
                        case SWT.NO:  exit=true; break;
                        case SWT.CANCEL: 
                            exit=false;
                            break;
                        }
                    }
                }
                // A running transformation?
                //
                if (mapEntry.getObject() instanceof SpoonLog)
                {
                    SpoonLog spoonLog = (SpoonLog) mapEntry.getObject();
                    if (spoonLog.isRunning())
                    {
                        MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
                        mb.setMessage(Messages.getString("Spoon.Message.Warning.PromptExitWhenRunTransformation"));// There is a running transformation.  Do you want to stop it and quit Spoon?
                        mb.setText(Messages.getString("System.Warning")); //Warning
                        int reply = mb.open();
                        
                        if (reply==SWT.NO) exit=false; // No selected: don't exit!
                    }
                }
            }
        }
        
        if (exit) // we have asked about it all and we're still here.  Now close all the tabs, stop the running transformations
        {
            for (Iterator iter = list.iterator(); iter.hasNext() && exit;)
            {
                TabMapEntry mapEntry = (TabMapEntry) iter.next();
                
                if (!mapEntry.getObject().canBeClosed())
                {
                    // Unsaved transformation?
                    //
                    if (mapEntry.getObject() instanceof SpoonGraph)
                    {
                        TransMeta transMeta = (TransMeta) mapEntry.getObject().getManagedObject();
                        if (transMeta.hasChanged())
                        {
                            mapEntry.getTabItem().dispose();
                        }
                    }
                    // A running transformation?
                    //
                    if (mapEntry.getObject() instanceof SpoonLog)
                    {
                        SpoonLog spoonLog = (SpoonLog) mapEntry.getObject();
                        if (spoonLog.isRunning())
                        {
                            spoonLog.stop();
                            mapEntry.getTabItem().dispose();
                        }
                    }
                }
            }
        }
          

        if (exit) dispose();
            
        return exit;
    }
    
    public boolean saveFile(TransMeta transMeta)
    {
        if (transMeta==null) return false;
        
        boolean saved=false;
        
        log.logDetailed(toString(), Messages.getString("Spoon.Log.SaveToFileOrRepository"));//"Save to file or repository..."
        
        if (rep!=null)
        {
            saved=saveRepository(transMeta);
        }
        else
        {
            if (transMeta.getFilename()!=null)
            {
                saved=save(transMeta, transMeta.getFilename());
            }
            else
            {
                saved=saveFileAs(transMeta);
            }
        }
        
        if (saved) // all was OK
        {
            saved=saveSharedObjects(transMeta);
        }
        
        try
        {
            if (props.useDBCache()) transMeta.getDbCache().saveCache(log);
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorSavingDatabaseCache.Title"), Messages.getString("Spoon.Dialog.ErrorSavingDatabaseCache.Message"), e);//"An error occured saving the database cache to disk"
        }
        
        renameTabs(); // filename or name of transformation might have changed.

        return saved;
    }
    
    public boolean saveRepository(TransMeta transMeta)
    {
        return saveRepository(transMeta, false);
    }

    public boolean saveRepository(TransMeta transMeta, boolean ask_name)
    {
        log.logDetailed(toString(), Messages.getString("Spoon.Log.SaveToRepository"));//"Save to repository..."
        if (rep!=null)
        {
            boolean answer = true;
            boolean ask    = ask_name;
            while (answer && ( ask || transMeta.getName()==null || transMeta.getName().length()==0 ) )
            {
                if (!ask)
                {
                    MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
                    mb.setMessage(Messages.getString("Spoon.Dialog.PromptTransformationName.Message"));//"Please give this transformation a name before saving it in the database."
                    mb.setText(Messages.getString("Spoon.Dialog.PromptTransformationName.Title"));//"Transformation has no name."
                    mb.open();
                }
                ask=false;
                answer = editTransformationProperties(transMeta);
                // System.out.println("answer="+answer+", ask="+ask+", transMeta.getName()="+transMeta.getName());
            }
            
            if (answer && transMeta.getName()!=null && transMeta.getName().length()>0)
            {
                if (!rep.getUserInfo().isReadonly())
                {
                    int response = SWT.YES;
                    if (transMeta.showReplaceWarning(rep))
                    {
                        MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
                        mb.setMessage(Messages.getString("Spoon.Dialog.PromptOverwriteTransformation.Message",transMeta.getName(),Const.CR));//"There already is a transformation called ["+transMeta.getName()+"] in the repository."+Const.CR+"Do you want to overwrite the transformation?"
                        mb.setText(Messages.getString("Spoon.Dialog.PromptOverwriteTransformation.Title"));//"Overwrite?"
                        response = mb.open();
                    }
                    
                    boolean saved=false;
                    if (response == SWT.YES)
                    {
                        shell.setCursor(cursor_hourglass);

                        // Keep info on who & when this transformation was changed...
                        transMeta.setModifiedDate( new Value("MODIFIED_DATE", Value.VALUE_TYPE_DATE) );                 
                        transMeta.getModifiedDate().sysdate();
                        transMeta.setModifiedUser( rep.getUserInfo().getLogin() );

                        TransSaveProgressDialog tspd = new TransSaveProgressDialog(shell, rep, transMeta);
                        if (tspd.open())
                        {
                            saved=true;
                            if (!props.getSaveConfirmation())
                            {
                                MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
                                                                                         Messages.getString("Spoon.Message.Warning.SaveOK"), //"Save OK!"
                                                                                         null,
                                                                                         Messages.getString("Spoon.Message.Warning.TransformationWasStored"),//"This transformation was stored in repository"
                                                                                         MessageDialog.QUESTION,
                                                                                         new String[] { Messages.getString("Spoon.Message.Warning.OK") },//"OK!"
                                                                                         0,
                                                                                         Messages.getString("Spoon.Message.Warning.NotShowThisMessage"),//"Don't show this message again."
                                                                                         props.getSaveConfirmation()
                                                                                         );
                                md.open();
                                props.setSaveConfirmation(md.getToggleState());
                            }
    
                            // Handle last opened files...
                            props.addLastFile(Props.TYPE_PROPERTIES_SPOON, transMeta.getName(), transMeta.getDirectory().getPath(), true, getRepositoryName());
                            saveSettings();
                            addMenuLast();
    
                            setShellText();
                        }
                        shell.setCursor(null);
                    }
                    return saved;
                }
                else
                {
                    MessageBox mb = new MessageBox(shell, SWT.CLOSE | SWT.ICON_ERROR);
                    mb.setMessage(Messages.getString("Spoon.Dialog.OnlyreadRepository.Message"));//"Sorry, the user you're logged on with, can only read from the repository"
                    mb.setText(Messages.getString("Spoon.Dialog.OnlyreadRepository.Title"));//"Transformation not saved!"
                    mb.open();
                }
            }
        }
        else
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setMessage(Messages.getString("Spoon.Dialog.NoRepositoryConnection.Message"));//"There is no repository connection available."
            mb.setText(Messages.getString("Spoon.Dialog.NoRepositoryConnection.Title"));//"No repository available."
            mb.open();
        }
        return false;
    }

    public boolean saveFileAs(TransMeta transMeta)
    {
        boolean saved=false;
        
        log.logBasic(toString(), Messages.getString("Spoon.Log.SaveAs"));//"Save as..."

        if (rep!=null)
        {
            transMeta.setID(-1L);
            saved=saveRepository(transMeta, true);
            renameTabs();
        }
        else
        {
            saved=saveXMLFile(transMeta);
            renameTabs();
        }
        
        return saved;
    }
    
    private void loadSharedObjects(TransMeta transMeta)
    {
        try
        {
            transMeta.readSharedObjects(rep);
        }
        catch(Exception e)
        {
            new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Title"), Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Message", makeGraphTabName(transMeta)), e);
        }
    }
    
    private boolean saveSharedObjects(TransMeta transMeta)
    {
        try
        {
            transMeta.saveSharedObjects();
            return true;
        }
        catch(Exception e)
        {
            log.logError(toString(), "Unable to save shared ojects: "+e.toString());
            return false;
        }
    }

    private boolean saveXMLFile(TransMeta transMeta)
    {
        boolean saved=false;
        
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterExtensions(Const.STRING_TRANS_FILTER_EXT);
        dialog.setFilterNames(Const.STRING_TRANS_FILTER_NAMES);
        String fname = dialog.open();
        if (fname!=null) 
        {
            // Is the filename ending on .ktr, .xml?
            boolean ending=false;
            for (int i=0;i<Const.STRING_TRANS_FILTER_EXT.length-1;i++)
            {
                if (fname.endsWith(Const.STRING_TRANS_FILTER_EXT[i].substring(1))) 
                {
                    ending=true;
                } 
            }
            if (fname.endsWith(Const.STRING_TRANS_DEFAULT_EXT)) ending=true;
            if (!ending)
            {
                fname+=Const.STRING_TRANS_DEFAULT_EXT;
            }
            // See if the file already exists...
            File f = new File(fname);
            int id = SWT.YES;
            if (f.exists())
            {
                MessageBox mb = new MessageBox(shell, SWT.NO | SWT.YES | SWT.ICON_WARNING);
                mb.setMessage(Messages.getString("Spoon.Dialog.PromptOverwriteFile.Message"));//"This file already exists.  Do you want to overwrite it?"
                mb.setText(Messages.getString("Spoon.Dialog.PromptOverwriteFile.Title"));//"This file already exists!"
                id = mb.open();
            }
            if (id==SWT.YES)
            {
                saved=save(transMeta, fname);
                transMeta.setFilename(fname);
            }
        }
        
        return saved;
    }
    
    private boolean save(TransMeta transMeta, String fname)
    {
        boolean saved = false;
        String xml = XMLHandler.getXMLHeader() + transMeta.getXML();
        try
        {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(fname)));
            dos.write(xml.getBytes(Const.XML_ENCODING));
            dos.close();

            saved=true;

            // Handle last opened files...
            props.addLastFile(Props.TYPE_PROPERTIES_SPOON, fname, null, false, null);
            saveSettings();
            addMenuLast();
            
            transMeta.clearChanged();
            setShellText();
            log.logDebug(toString(), Messages.getString("Spoon.Log.FileWritten")+" ["+fname+"]"); //"File written to
        }
        catch(Exception e)
        {
            log.logDebug(toString(), Messages.getString("Spoon.Log.ErrorOpeningFileForWriting")+e.toString());//"Error opening file for writing! --> "
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setMessage(Messages.getString("Spoon.Dialog.ErrorSavingFile.Message")+Const.CR+e.toString());//"Error saving file:"
            mb.setText(Messages.getString("Spoon.Dialog.ErrorSavingFile.Title"));//"ERROR"
            mb.open();
        }
        
        return saved;
    }
    
    public void helpAbout()
    {
        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION | SWT.CENTER);
        String mess = Messages.getString("System.ProductInfo")+Const.VERSION+Const.CR+Const.CR+Const.CR;//Kettle - Spoon version 
        mess+=Messages.getString("System.CompanyInfo")+Const.CR;
        mess+="         "+Messages.getString("System.ProductWebsiteUrl")+Const.CR; //(c) 2001-2004 i-Bridge bvba     www.kettle.be
        mess+=Const.CR; 
        mess+=Const.CR; 
        mess+=Const.CR; 
        mess+="         Build version : "+BuildVersion.getInstance().getVersion()+Const.CR;
        mess+="         Build date    : "+BuildVersion.getInstance().getBuildDate()+Const.CR;
        
        mb.setMessage(mess);
        mb.setText(APP_NAME);
        mb.open();
    }

    public void editUnselectAll(TransMeta transMeta)
    {
        transMeta.unselectAll(); 
        // spoongraph.redraw();
    }
    
    public void editSelectAll(TransMeta transMeta)
    {
        transMeta.selectAll(); 
        // spoongraph.redraw();
    }
    
    public void editOptions()
    {
        EnterOptionsDialog eod = new EnterOptionsDialog(shell, props);
        if (eod.open()!=null)
        {
            props.saveProps();
            loadSettings();
            changeLooks();

            MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
            mb.setMessage(Messages.getString("Spoon.Dialog.PleaseRestartApplication.Message"));
            mb.setText(Messages.getString("Spoon.Dialog.PleaseRestartApplication.Title"));
            mb.open();
        } 
    }
    
    /**
     * Refresh the object selection tree (on the left of the screen)
     * @param complete true refreshes the complete tree, false tries to do a differential update to avoid flickering.
     */
    public void refreshTree()
    {
        if (shell.isDisposed()) return;
        
        GUIResource guiResource = GUIResource.getInstance();
        
        // get a list of transformations from the transformation map
        Collection collection = transformationMap.values();
        TransMeta[] transMetas = (TransMeta[]) collection.toArray(new TransMeta[collection.size()]);
        
        // Sort the transformations by name
        //
        // Arrays.sort(transMetas);
        
        // Refresh the content of the tree for those transformations
        //
        // First remove the old ones.
        tiTrans.removeAll();

        // Now add the data back 
        //
        for (int t=0;t<transMetas.length;t++)
        {
            TransMeta transMeta = transMetas[t];
            
            // Add a tree item with the name of transformation
            //
            TreeItem tiTransName = new TreeItem(tiTrans, SWT.NONE);
            String name = makeGraphTabName(transMeta);
            if (Const.isEmpty(name)) name = STRING_TRANS_NO_NAME;
            tiTransName.setText(name);
            tiTransName.setImage(guiResource.getImageBol());
            
            ///////////////////////////////////////////////////////
            //
            // Now add the database connections
            //
            TreeItem tiDbTitle = new TreeItem(tiTransName, SWT.NONE);
            tiDbTitle.setText(STRING_CONNECTIONS);
            tiDbTitle.setImage(guiResource.getImageConnection());
            
            // Draw the connections themselves below it.
            for (int i=0;i<transMeta.nrDatabases();i++)
            {
                DatabaseMeta databaseMeta = transMeta.getDatabase(i);
                TreeItem tiDb = new TreeItem(tiDbTitle, SWT.NONE);
                tiDb.setText(databaseMeta.getName());
                if (databaseMeta.isShared()) tiDb.setFont(guiResource.getFontBold());
                tiDb.setImage(guiResource.getImageConnection());
            }

            ///////////////////////////////////////////////////////
            //
            // The steps
            //
            TreeItem tiStepTitle = new TreeItem(tiTransName, SWT.NONE);
            tiStepTitle.setText(STRING_STEPS);
            tiStepTitle.setImage(guiResource.getImageBol());
            
            // Put the steps below it.
            for (int i=0;i<transMeta.nrSteps();i++)
            {
                StepMeta stepMeta = transMeta.getStep(i);
                TreeItem tiStep = new TreeItem(tiStepTitle, SWT.NONE);
                tiStep.setText(stepMeta.getName());
                if (stepMeta.isShared()) tiStep.setFont(guiResource.getFontBold());
                tiStep.setImage(guiResource.getImageBol());
            }
            
            ///////////////////////////////////////////////////////
            //
            // The hops
            //
            TreeItem tiHopTitle = new TreeItem(tiTransName, SWT.NONE);
            tiHopTitle.setText(STRING_HOPS);
            tiHopTitle.setImage(guiResource.getImageHop());
            
            // Put the steps below it.
            for (int i=0;i<transMeta.nrTransHops();i++)
            {
                TransHopMeta hopMeta = transMeta.getTransHop(i);
                TreeItem tiHop = new TreeItem(tiHopTitle, SWT.NONE);
                tiHop.setText(hopMeta.toString());
                tiHop.setImage(guiResource.getImageHop());
            }

            ///////////////////////////////////////////////////////
            //
            // The partitions
            //
            TreeItem tiPartitionTitle = new TreeItem(tiTransName, SWT.NONE);
            tiPartitionTitle.setText(STRING_PARTITIONS);
            tiPartitionTitle.setImage(guiResource.getImageConnection());
            
            // Put the steps below it.
            for (int i=0;i<transMeta.getPartitionSchemas().size();i++)
            {
                PartitionSchema partitionSchema = (PartitionSchema) transMeta.getPartitionSchemas().get(i);
                TreeItem tiPartition = new TreeItem(tiPartitionTitle, SWT.NONE);
                tiPartition.setText(partitionSchema.getName());
                tiPartition.setImage(guiResource.getImageBol());
                if (partitionSchema.isShared()) tiPartition.setFont(guiResource.getFontBold());
            }

            ///////////////////////////////////////////////////////
            //
            // The slaves
            //
            TreeItem tiSlaveTitle = new TreeItem(tiTransName, SWT.NONE);
            tiSlaveTitle.setText(STRING_SLAVES);
            tiSlaveTitle.setImage(guiResource.getImageBol());
            
            // Put the steps below it.
            for (int i=0;i<transMeta.getSlaveServers().size();i++)
            {
                SlaveServer slaveServer = (SlaveServer) transMeta.getSlaveServers().get(i);
                TreeItem tiSlave = new TreeItem(tiSlaveTitle, SWT.NONE);
                tiSlave.setText(slaveServer.getName());
                tiSlave.setImage(guiResource.getImageBol());
                if (slaveServer.isShared()) tiSlave.setFont(guiResource.getFontBold());
            }
            
            ///////////////////////////////////////////////////////
            //
            // The clusters
            //
            TreeItem tiClusterTitle = new TreeItem(tiTransName, SWT.NONE);
            tiClusterTitle.setText(STRING_CLUSTERS);
            tiClusterTitle.setImage(guiResource.getImageBol());
            
            // Put the steps below it.
            for (int i=0;i<transMeta.getClusterSchemas().size();i++)
            {
                ClusterSchema clusterSchema = (ClusterSchema) transMeta.getClusterSchemas().get(i);
                TreeItem tiCluster = new TreeItem(tiClusterTitle, SWT.NONE);
                tiCluster.setText(clusterSchema.toString());
                tiCluster.setImage(guiResource.getImageBol());
                if (clusterSchema.isShared()) tiCluster.setFont(guiResource.getFontBold());
            }
        }

        
        // Set the expanded state of the complete tree.
        TreeMemory.setExpandedFromMemory(selectionTree, STRING_SPOON_MAIN_TREE);

        selectionTree.setFocus();
        refreshPluginHistory();
        setShellText();
    }
    
    public String getActiveTabText()
    {
        if (tabfolder.getSelection()==null) return null;
        return tabfolder.getSelection().getText();
    }
    
    public void refreshGraph()
    {
        if (shell.isDisposed()) return;
        
        String tabText = getActiveTabText();
        if (tabText==null) return;
        
        TabMapEntry tabMapEntry = (TabMapEntry) tabMap.get(tabText);
        if (tabMapEntry.getObject() instanceof SpoonGraph)
        {
            SpoonGraph spoonGraph = (SpoonGraph) tabMapEntry.getObject();
            spoonGraph.redraw();
        }
         
        setShellText();
    }
    
    public void refreshHistory()
    {
        final SpoonHistory spoonHistory = getActiveSpoonHistory();
        if (spoonHistory!=null)
        {
            spoonHistory.markRefreshNeeded();
        }
    }

    public StepMeta newStep(TransMeta transMeta)
    {
        return newStep(transMeta, true, true);
    }
    
    public StepMeta newStep(TransMeta transMeta, boolean openit, boolean rename)
    {
        if (transMeta==null) return null;
        TreeItem ti[] = selectionTree.getSelection();
        StepMeta inf = null;
        
        if (ti.length==1)
        {
            String steptype = ti[0].getText();
            log.logDebug(toString(), Messages.getString("Spoon.Log.NewStep")+steptype);//"New step: "
            
            inf = newStep(transMeta, steptype, steptype, openit, rename);
        }

        return inf;
    }

    /**
     * Allocate new step, optionally open and rename it.
     * 
     * @param name Name of the new step
     * @param description Description of the type of step
     * @param openit Open the dialog for this step?
     * @param rename Rename this step?
     * 
     * @return The newly created StepMeta object.
     * 
     */
    public StepMeta newStep(TransMeta transMeta, String name, String description, boolean openit, boolean rename)
    {
        StepMeta inf = null;
        
        // See if we need to rename the step to avoid doubles!
        if (rename && transMeta.findStep(name)!=null)
        {
            int i=2;
            String newname = name+" "+i;
            while (transMeta.findStep(newname)!=null)
            {
                i++;
                newname = name+" "+i;
            }
            name=newname;
        }

        StepLoader steploader = StepLoader.getInstance();
        StepPlugin stepPlugin = null;
        
        try
        {
            stepPlugin = steploader.findStepPluginWithDescription(description);
            if (stepPlugin!=null)
            {
                StepMetaInterface info = BaseStep.getStepInfo(stepPlugin, steploader);
    
                info.setDefault();
                
                if (openit)
                {
                    StepDialogInterface dialog = info.getDialog(shell, info, transMeta, name);
                    name = dialog.open();
                }
                inf=new StepMeta(stepPlugin.getID()[0], name, info);
    
                if (name!=null) // OK pressed in the dialog: we have a step-name
                {
                    String newname=name;
                    StepMeta stepMeta = transMeta.findStep(newname);
                    int nr=2;
                    while (stepMeta!=null)
                    {
                        newname = name+" "+nr;
                        stepMeta = transMeta.findStep(newname);
                        nr++;
                    }
                    if (nr>2)
                    {
                        inf.setName(newname);
                        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
                        mb.setMessage(Messages.getString("Spoon.Dialog.ChangeStepname.Message",newname));//"This stepname already exists.  Spoon changed the stepname to ["+newname+"]"
                        mb.setText(Messages.getString("Spoon.Dialog.ChangeStepname.Title"));//"Info!"
                        mb.open();
                    }
                    inf.setLocation(20, 20); // default location at (20,20)
                    transMeta.addStep(inf);
        
                    // Save for later:
                    // if openit is false: we drag&drop it onto the canvas!
                    if (openit)
                    {   
                        addUndoNew(transMeta, new StepMeta[] { inf }, new int[] { transMeta.indexOfStep(inf) });
                    }
                    
                    // Also store it in the pluginHistory list...
                    props.addPluginHistory(stepPlugin.getID()[0]);
        
                    refreshTree();
                }
                else
                {
                	return null; // Cancel pressed in dialog.
                }
                setShellText();
            }   
        }
        catch(KettleException e)
        {
            String filename = stepPlugin.getErrorHelpFile();
            if (stepPlugin!=null && filename!=null)
            {
                // OK, in stead of a normal error message, we give back the content of the error help file... (HTML)
                try
                {
                    StringBuffer content=new StringBuffer();
                    
                    System.out.println("Filename = "+filename);
                    FileInputStream fis = new FileInputStream(new File(filename));
                    int ch = fis.read();
                    while (ch>=0)
                    {
                        content.append( (char)ch);
                        ch = fis.read();
                    }

                    System.out.println("Content = "+content);
                    ShowBrowserDialog sbd = new ShowBrowserDialog(shell, Messages.getString("Spoon.Dialog.ErrorHelpText.Title"), content.toString());//"Error help text"
                    sbd.open();
                }
                catch(Exception ex)
                {
                    new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorShowingHelpText.Title"), Messages.getString("Spoon.Dialog.ErrorShowingHelpText.Message"), ex);//"Error showing help text"
                }
            }
            else
            {
                new ErrorDialog(shell, Messages.getString("Spoon.Dialog.UnableCreateNewStep.Title"),Messages.getString("Spoon.Dialog.UnableCreateNewStep.Message") , e);//"Error creating step"  "I was unable to create a new step"
            }
            return null;
        }
        catch(Throwable e)
        {
            if (!shell.isDisposed()) new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorCreatingStep.Title"), Messages.getString("Spoon.Dialog.UnableCreateNewStep.Message"), new Exception(e));//"Error creating step"
            return null;
        }
                
        return inf;
    }

    private void setTreeImages()
    {
        tiTrans.setImage(GUIResource.getInstance().getImageBol());
        tiBase.setImage(GUIResource.getInstance().getImageBol());
        tiPlug.setImage(GUIResource.getInstance().getImageBol());
        tiBlocks.setImage(GUIResource.getInstance().getImageBol());

        TreeItem tiBaseCat[]=tiBase.getItems();
        for (int x=0;x<tiBaseCat.length;x++)
        {
            tiBaseCat[x].setImage(GUIResource.getInstance().getImageBol());
            
            TreeItem ti[] = tiBaseCat[x].getItems();
            for (int i=0;i<ti.length;i++)
            {
                TreeItem stepitem = ti[i];
                String description = stepitem.getText();
                
                StepLoader steploader = StepLoader.getInstance();
                StepPlugin sp = steploader.findStepPluginWithDescription(description);
                if (sp!=null)
                {
                    Image stepimg = (Image)GUIResource.getInstance().getImagesStepsSmall().get(sp.getID()[0]);
                    if (stepimg!=null)
                    {
                        stepitem.setImage(stepimg);
                    }
                }
            }
        }
        TreeItem tiPlugCat[]=tiPlug.getItems();
        for (int x=0;x<tiPlugCat.length;x++)
        {
            tiPlugCat[x].setImage(GUIResource.getInstance().getImageBol());
            
            TreeItem ti[] = tiPlugCat[x].getItems();
            for (int i=0;i<ti.length;i++)
            {
                TreeItem stepitem = ti[i];
                String description = stepitem.getText();
                StepLoader steploader = StepLoader.getInstance();
                StepPlugin sp = steploader.findStepPluginWithDescription(description);
                if (sp!=null)
                {
                    Image stepimg = (Image)((GUIResource.getInstance().getImagesStepsSmall()).get(sp.getID()[0]));
                    if (stepimg!=null)
                    {
                        stepitem.setImage(stepimg);
                    }
                }
            }
        }
    }

    public void setShellText()
    {
        if (shell.isDisposed()) return;

        TransMeta transMeta = getActiveTransformation();
        String fname = transMeta!=null ? transMeta.getFilename() : "" ;

        String text = "";
        
        if (rep!=null)
        {
            text+= APPL_TITLE+" - ["+getRepositoryName()+"] ";
        }
        else
        {
            text+= APPL_TITLE+" - ";
        }
        
        if (rep!=null && transMeta!=null && transMeta.getId()>0)
        {
            if (Const.isEmpty(transMeta.getName()))
            {
                text+=Messages.getString("Spoon.Various.NoName");//"[no name]"
            }
            else
            {
                text+=transMeta.getName();
            }
        }
        else
        {
            if (!Const.isEmpty(fname))
            {
                text+=fname;
            }
        }
        
        if (transMeta!=null && transMeta.hasChanged())
        {
            text+=" "+Messages.getString("Spoon.Various.Changed");
        }
        
        shell.setText(text);
        
        enableMenus();
        markTabsChanged();
    }
    
    public void enableMenus()
    {
        boolean enableTransMenu = getActiveTransformation()!=null;
        boolean enableRepositoryMenu = rep!=null;
        
        // Only enable certain menu-items if we need to.
        miFileSave.setEnabled(enableTransMenu);
        miFileSaveAs.setEnabled(enableTransMenu);
        miFileClose.setEnabled(enableTransMenu);
        miFilePrint.setEnabled(enableTransMenu);

        miEditUndo.setEnabled(enableTransMenu);
        miEditRedo.setEnabled(enableTransMenu);
        miEditUnselectAll.setEnabled(enableTransMenu);
        miEditSelectAll.setEnabled(enableTransMenu);
        miEditCopy.setEnabled(enableTransMenu);
        miEditPaste.setEnabled(enableTransMenu);

        miTransRun.setEnabled(enableTransMenu);
        miTransPreview.setEnabled(enableTransMenu);
        miTransCheck.setEnabled(enableTransMenu);
        miTransImpact.setEnabled(enableTransMenu);
        miTransSQL.setEnabled(enableTransMenu);
        miLastImpact.setEnabled(enableTransMenu);
        miLastCheck.setEnabled(enableTransMenu);
        miLastPreview.setEnabled(enableTransMenu);
        miTransCopy.setEnabled(enableTransMenu);
        miTransPaste.setEnabled(enableTransMenu);
        miTransImage.setEnabled(enableTransMenu);
        miTransDetails.setEnabled(enableTransMenu);

        miWizardNewConnection.setEnabled(enableTransMenu);
        miWizardCopyTable.setEnabled(enableTransMenu);
        
        miRepDisconnect.setEnabled(enableRepositoryMenu);
        miRepExplore.setEnabled(enableRepositoryMenu);
        miRepUser.setEnabled(enableRepositoryMenu);
    }
    
    private void markTabsChanged()
    {
        Collection c = tabMap.values();
        for (Iterator iter = c.iterator(); iter.hasNext();)
        {
            TabMapEntry entry = (TabMapEntry) iter.next();
            
            boolean changed = entry.getObject().hasContentChanged();
            if (changed && !entry.getTabItem().isDisposed())
            {
                entry.getTabItem().setFont(GUIResource.getInstance().getFontBold());
            }
            else
            {
                entry.getTabItem().setFont(GUIResource.getInstance().getFontGraph());
            }
        }
    }
    
    private void printFile(TransMeta transMeta)
    {
        SpoonGraph spoonGraph = getActiveSpoonGraph();
        
        PrintSpool ps = new PrintSpool();
        Printer printer = ps.getPrinter(shell);
        
        // Create an image of the screen
        Point max = transMeta.getMaximum();
        
        Image img = spoonGraph.getTransformationImage(printer, max.x, max.y);

        ps.printImage(shell, props, img);
        
        img.dispose();
        ps.dispose();
    }
    
    private SpoonGraph getActiveSpoonGraph()
    {
        TabMapEntry mapEntry = (TabMapEntry) tabMap.get(tabfolder.getSelection().getText());
        if (mapEntry.getObject() instanceof SpoonGraph) return (SpoonGraph) mapEntry.getObject();
        return null;
    }

    /**
     * @return the Log tab associated with the active transformation
     */
    private SpoonLog getActiveSpoonLog()
    {
        TransMeta transMeta = getActiveTransformation();
        if (transMeta==null) return null; // nothing to work with.

        return findSpoonLogOfTransformation(transMeta);
    }
    
    public SpoonGraph findSpoonGraphOfTransformation(TransMeta transMeta)
    {
        // Now loop over the entries in the tab-map
        Collection collection = tabMap.values();
        for (Iterator iter = collection.iterator(); iter.hasNext();)
        {
            TabMapEntry mapEntry = (TabMapEntry) iter.next();
            if (mapEntry.getObject() instanceof SpoonGraph)
            {
                SpoonGraph spoonGraph = (SpoonGraph) mapEntry.getObject();
                if (spoonGraph.getTransMeta().equals(transMeta)) return spoonGraph;
            }
        }
        return null;
    }
    
    public SpoonLog findSpoonLogOfTransformation(TransMeta transMeta)
    {
        // Now loop over the entries in the tab-map
        Collection collection = tabMap.values();
        for (Iterator iter = collection.iterator(); iter.hasNext();)
        {
            TabMapEntry mapEntry = (TabMapEntry) iter.next();
            if (mapEntry.getObject() instanceof SpoonLog)
            {
                SpoonLog spoonLog = (SpoonLog) mapEntry.getObject();
                if (spoonLog.getTransMeta().equals(transMeta)) return spoonLog;
            }
        }
        return null;
    }
    
    public SpoonHistory findSpoonHistoryOfTransformation(TransMeta transMeta)
    {
        if (transMeta==null) return null;
        
        // Now loop over the entries in the tab-map
        Collection collection = tabMap.values();
        for (Iterator iter = collection.iterator(); iter.hasNext();)
        {
            TabMapEntry mapEntry = (TabMapEntry) iter.next();
            if (mapEntry.getObject() instanceof SpoonHistory)
            {
                SpoonHistory spoonHistory = (SpoonHistory) mapEntry.getObject();
                if (spoonHistory.getTransMeta()!=null && spoonHistory.getTransMeta().equals(transMeta)) return spoonHistory;
            }
        }
        return null;
    }

    /**
     * @return the history tab associated with the active transformation
     */
    private SpoonHistory getActiveSpoonHistory()
    {
        TransMeta transMeta = getActiveTransformation();
        if (transMeta==null) return null; // nothing to work with.
        
        return findSpoonHistoryOfTransformation(transMeta);
    }
    
    
    /**
     * @return The active TransMeta object by looking at the selected SpoonGraph, SpoonLog, SpoonHist
     *         If nothing valueable is selected, we return null
     */
    public TransMeta getActiveTransformation()
    {
        if (tabfolder==null) return null;
        CTabItem tabItem = tabfolder.getSelection();
        if (tabItem==null) return null;
        
        // What transformation is in the active tab?
        // SpoonLog, SpoonGraph & SpoonHist contain the same transformation
        //
        TabMapEntry mapEntry = (TabMapEntry) tabMap.get(tabfolder.getSelection().getText());
        TransMeta transMeta = null;
        if (mapEntry.getObject() instanceof SpoonGraph) transMeta = ((SpoonGraph) mapEntry.getObject()).getTransMeta();
        if (mapEntry.getObject() instanceof SpoonLog) transMeta = ((SpoonLog) mapEntry.getObject()).getTransMeta();
        if (mapEntry.getObject() instanceof SpoonHistory) transMeta = ((SpoonHistory) mapEntry.getObject()).getTransMeta();
        
        return transMeta;
    }

    public TransMeta findTransformation(String name)
    {
        return (TransMeta)transformationMap.get(name);
    }
    
    public TransMeta[] getLoadedTransformations()
    {
        List list = new ArrayList(transformationMap.values());
        return (TransMeta[]) list.toArray(new TransMeta[list.size()]);
    }

    public SpoonGraph getSpoonGraph(TransMeta transMeta)
    {
        Set set = tabMap.entrySet();
        for (Iterator iter = set.iterator(); iter.hasNext();)
        {
            TabMapEntry mapEntry = (TabMapEntry) iter.next();
            if (mapEntry.getObject() instanceof SpoonGraph) 
            {
                SpoonGraph spoonGraph = (SpoonGraph)mapEntry.getObject(); 
                TransMeta look = spoonGraph.getTransMeta(); 
                if ( (Const.isEmpty(look.getName()) && Const.isEmpty(transMeta.getName())) ||  look.getName().equals(transMeta.getName()) )
                {
                    return spoonGraph; 
                }
            }
        }
        return null;
    }

    private boolean editTransformationProperties(TransMeta transMeta)
    {
        TransDialog tid = new TransDialog(shell, SWT.NONE, transMeta, rep);
        TransMeta ti = tid.open();
        
        // In this case, load shared objects
        //
        if (tid.isSharedObjectsFileChanged())
        {
            loadSharedObjects(transMeta);
        }
        
        if (tid.isSharedObjectsFileChanged() || ti!=null)
        {
            refreshTree();
            renameTabs(); // cheap operation, might as will do it anyway
        }
        
        setShellText();
        return ti!=null;
    }
    
    public void saveSettings()
    {
        WindowProperty winprop = new WindowProperty(shell);
        winprop.setName(APPL_TITLE);
        props.setScreen(winprop);
        
        props.setLogLevel(log.getLogLevelDesc());
        props.setLogFilter(log.getFilter());
        props.setSashWeights(sashform.getWeights());
        props.saveProps();
    }

    public void loadSettings()
    {
        log.setLogLevel(props.getLogLevel());
        log.setFilter(props.getLogFilter());

        // transMeta.setMaxUndo(props.getMaxUndo());
        DBCache.getInstance().setActive(props.useDBCache());
    }
    
    public void changeLooks()
    {
        props.setLook(selectionTree);
        props.setLook(tabfolder, Props.WIDGET_STYLE_TAB);
        
        GUIResource.getInstance().reload();

        refreshTree();
        refreshGraph();
    }
    
    public void undoAction(TransMeta transMeta)
    {
        if (transMeta==null) return;
        SpoonGraph spoonGraph = findSpoonGraphOfTransformation(transMeta);
        spoonGraph.forceFocus();
        
        TransAction ta = transMeta.previousUndo();
        if (ta==null) return;
        
        setUndoMenu(transMeta); // something changed: change the menu
        switch(ta.getType())
        {
            //
            // NEW
            //

            // We created a new step : undo this...
            case TransAction.TYPE_ACTION_NEW_STEP:
                // Delete the step at correct location:
                for (int i=ta.getCurrent().length-1;i>=0;i--)
                {
                    int idx = ta.getCurrentIndex()[i];
                    transMeta.removeStep(idx);
                }
                refreshTree();
                refreshGraph();
                break;
    
            // We created a new connection : undo this...
            case TransAction.TYPE_ACTION_NEW_CONNECTION:
                // Delete the connection at correct location:
                for (int i=ta.getCurrent().length-1;i>=0;i--)
                {
                    int idx = ta.getCurrentIndex()[i];
                    transMeta.removeDatabase(idx);
                }
                refreshTree();
                refreshGraph();
                break;
    
            // We created a new note : undo this...
            case TransAction.TYPE_ACTION_NEW_NOTE:
                // Delete the note at correct location:
                for (int i=ta.getCurrent().length-1;i>=0;i--)
                {
                    int idx = ta.getCurrentIndex()[i];
                    transMeta.removeNote(idx);
                }
                refreshTree();
                refreshGraph();
                break;
    
            // We created a new hop : undo this...
            case TransAction.TYPE_ACTION_NEW_HOP:
                // Delete the hop at correct location:
                for (int i=ta.getCurrent().length-1;i>=0;i--)
                {
                    int idx = ta.getCurrentIndex()[i];
                    transMeta.removeTransHop(idx);
                }
                refreshTree();
                refreshGraph();
                break;
                
            // We created a new slave : undo this...
            case TransAction.TYPE_ACTION_NEW_SLAVE:
                // Delete the slave at correct location:
                for (int i=ta.getCurrent().length-1;i>=0;i--)
                {
                    int idx = ta.getCurrentIndex()[i];
                    transMeta.getSlaveServers().remove(idx);
                }
                refreshTree();
                refreshGraph();
                break;

                // We created a new slave : undo this...
            case TransAction.TYPE_ACTION_NEW_CLUSTER:
                // Delete the slave at correct location:
                for (int i=ta.getCurrent().length-1;i>=0;i--)
                {
                    int idx = ta.getCurrentIndex()[i];
                    transMeta.getClusterSchemas().remove(idx);
                }
                refreshTree();
                refreshGraph();
                break;
                
            //
            // DELETE
            //

            // We delete a step : undo this...
            case TransAction.TYPE_ACTION_DELETE_STEP:
                // un-Delete the step at correct location: re-insert
                for (int i=0;i<ta.getCurrent().length;i++)
                {
                    StepMeta stepMeta = (StepMeta)ta.getCurrent()[i];
                    int idx = ta.getCurrentIndex()[i];
                    transMeta.addStep(idx, stepMeta);
                }
                refreshTree();
                refreshGraph();
                break;
    
            // We deleted a connection : undo this...
            case TransAction.TYPE_ACTION_DELETE_CONNECTION:
                // re-insert the connection at correct location:
                for (int i=0;i<ta.getCurrent().length;i++)
                {
                    DatabaseMeta ci = (DatabaseMeta)ta.getCurrent()[i];
                    int idx = ta.getCurrentIndex()[i];
                    transMeta.addDatabase(idx, ci);
                }
                refreshTree();
                refreshGraph();
                break;
    
            // We delete new note : undo this...
            case TransAction.TYPE_ACTION_DELETE_NOTE:
                // re-insert the note at correct location:
                for (int i=0;i<ta.getCurrent().length;i++)
                {
                    NotePadMeta ni = (NotePadMeta)ta.getCurrent()[i];
                    int idx = ta.getCurrentIndex()[i];
                    transMeta.addNote(idx, ni);
                }
                refreshTree();
                refreshGraph();
                break;
    
            // We deleted a hop : undo this...
            case TransAction.TYPE_ACTION_DELETE_HOP:
                // re-insert the hop at correct location:
                for (int i=0;i<ta.getCurrent().length;i++)
                {
                    TransHopMeta hi = (TransHopMeta)ta.getCurrent()[i];
                    int idx = ta.getCurrentIndex()[i];
                    // Build a new hop:
                    StepMeta from = transMeta.findStep(hi.getFromStep().getName());
                    StepMeta to   = transMeta.findStep(hi.getToStep().getName());
                    TransHopMeta hinew = new TransHopMeta(from, to);
                    transMeta.addTransHop(idx, hinew);
                }
                refreshTree();
                refreshGraph();
                break;


            //
            // CHANGE
            //

            // We changed a step : undo this...
            case TransAction.TYPE_ACTION_CHANGE_STEP:
                // Delete the current step, insert previous version.
                for (int i=0;i<ta.getCurrent().length;i++)
                {
                    StepMeta prev = (StepMeta)ta.getPrevious()[i];
                    int idx = ta.getCurrentIndex()[i];

                    transMeta.removeStep(idx);
                    transMeta.addStep(idx, prev);
                }
                refreshTree();
                refreshGraph();
                break;
    
            // We changed a connection : undo this...
            case TransAction.TYPE_ACTION_CHANGE_CONNECTION:
                // Delete & re-insert
                for (int i=0;i<ta.getCurrent().length;i++)
                {
                    DatabaseMeta prev = (DatabaseMeta)ta.getPrevious()[i];
                    int idx = ta.getCurrentIndex()[i];

                    transMeta.removeDatabase(idx);
                    transMeta.addDatabase(idx, prev);
                }
                refreshTree();
                refreshGraph();
                break;
    
            // We changed a note : undo this...
            case TransAction.TYPE_ACTION_CHANGE_NOTE:
                // Delete & re-insert
                for (int i=0;i<ta.getCurrent().length;i++)
                {
                    int idx = ta.getCurrentIndex()[i];
                    transMeta.removeNote(idx);
                    NotePadMeta prev = (NotePadMeta)ta.getPrevious()[i];
                    transMeta.addNote(idx, prev);
                }
                refreshTree();
                refreshGraph();
                break;
    
            // We changed a hop : undo this...
            case TransAction.TYPE_ACTION_CHANGE_HOP:
                // Delete & re-insert
                for (int i=0;i<ta.getCurrent().length;i++)
                {
                    TransHopMeta prev = (TransHopMeta)ta.getPrevious()[i];
                    int idx = ta.getCurrentIndex()[i];

                    transMeta.removeTransHop(idx);
                    transMeta.addTransHop(idx, prev);
                }
                refreshTree();
                refreshGraph();
                break;

            //
            // POSITION
            //
                
            // The position of a step has changed: undo this...
            case TransAction.TYPE_ACTION_POSITION_STEP:
                // Find the location of the step:
                for (int i = 0; i < ta.getCurrentIndex().length; i++) 
                {
                    StepMeta stepMeta = transMeta.getStep(ta.getCurrentIndex()[i]);
                    stepMeta.setLocation(ta.getPreviousLocation()[i]);
                }
                refreshGraph();
                break;
    
            // The position of a note has changed: undo this...
            case TransAction.TYPE_ACTION_POSITION_NOTE:
                for (int i=0;i<ta.getCurrentIndex().length;i++)
                {
                    int idx = ta.getCurrentIndex()[i];
                    NotePadMeta npi = transMeta.getNote(idx);
                    Point prev = ta.getPreviousLocation()[i];
                    npi.setLocation(prev);
                }
                refreshGraph();
                break;
            default: break;
        }
        
        // OK, now check if we need to do this again...
        if (transMeta.viewNextUndo()!=null)
        {
            if (transMeta.viewNextUndo().getNextAlso()) undoAction(transMeta);
        }
    }
    
    public void redoAction(TransMeta transMeta)
    {
        if (transMeta==null) return;
        SpoonGraph spoonGraph = findSpoonGraphOfTransformation(transMeta);
        spoonGraph.forceFocus();

        TransAction ta = transMeta.nextUndo();
        if (ta==null) return;
        setUndoMenu(transMeta); // something changed: change the menu
        switch(ta.getType())
        {
        //
        // NEW
        //
        case TransAction.TYPE_ACTION_NEW_STEP:
            // re-delete the step at correct location:
            for (int i=0;i<ta.getCurrent().length;i++)
            {
                StepMeta stepMeta = (StepMeta)ta.getCurrent()[i];
                int idx = ta.getCurrentIndex()[i];
                transMeta.addStep(idx, stepMeta);
                                
                refreshTree();
                refreshGraph();
            }
            break;

        case TransAction.TYPE_ACTION_NEW_CONNECTION:
            // re-insert the connection at correct location:
            for (int i=0;i<ta.getCurrent().length;i++)
            {
                DatabaseMeta ci = (DatabaseMeta)ta.getCurrent()[i];
                int idx = ta.getCurrentIndex()[i];
                transMeta.addDatabase(idx, ci);
                refreshTree();
                refreshGraph();
            }
            break;

        case TransAction.TYPE_ACTION_NEW_NOTE:
            // re-insert the note at correct location:
            for (int i=0;i<ta.getCurrent().length;i++)
            {
                NotePadMeta ni = (NotePadMeta)ta.getCurrent()[i];
                int idx = ta.getCurrentIndex()[i];
                transMeta.addNote(idx, ni);
                refreshTree();
                refreshGraph();
            }
            break;

        case TransAction.TYPE_ACTION_NEW_HOP:
            // re-insert the hop at correct location:
            for (int i=0;i<ta.getCurrent().length;i++)
            {
                TransHopMeta hi = (TransHopMeta)ta.getCurrent()[i];
                int idx = ta.getCurrentIndex()[i];
                transMeta.addTransHop(idx, hi);
                refreshTree();
                refreshGraph();
            }
            break;
        
        //  
        // DELETE
        //
        case TransAction.TYPE_ACTION_DELETE_STEP:
            // re-remove the step at correct location:
            for (int i=ta.getCurrent().length-1;i>=0;i--)
            {
                int idx = ta.getCurrentIndex()[i];
                transMeta.removeStep(idx);
            }
            refreshTree();
            refreshGraph();
            break;

        case TransAction.TYPE_ACTION_DELETE_CONNECTION:
            // re-remove the connection at correct location:
            for (int i=ta.getCurrent().length-1;i>=0;i--)
            {
                int idx = ta.getCurrentIndex()[i];
                transMeta.removeDatabase(idx);
            }
            refreshTree();
            refreshGraph();
            break;

        case TransAction.TYPE_ACTION_DELETE_NOTE:
            // re-remove the note at correct location:
            for (int i=ta.getCurrent().length-1;i>=0;i--)
            {
                int idx = ta.getCurrentIndex()[i];
                transMeta.removeNote(idx);
            }
            refreshTree();
            refreshGraph();
            break;

        case TransAction.TYPE_ACTION_DELETE_HOP:
            // re-remove the hop at correct location:
            for (int i=ta.getCurrent().length-1;i>=0;i--)
            {
                int idx = ta.getCurrentIndex()[i];
                transMeta.removeTransHop(idx);
            }
            refreshTree();
            refreshGraph();
            break;

        //
        // CHANGE
        //

        // We changed a step : undo this...
        case TransAction.TYPE_ACTION_CHANGE_STEP:
            // Delete the current step, insert previous version.
            for (int i=0;i<ta.getCurrent().length;i++)
            {
                StepMeta stepMeta = (StepMeta)ta.getCurrent()[i];
                int idx = ta.getCurrentIndex()[i];
                
                transMeta.removeStep(idx);
                transMeta.addStep(idx, stepMeta);
            }
            refreshTree();
            refreshGraph();
            break;

        // We changed a connection : undo this...
        case TransAction.TYPE_ACTION_CHANGE_CONNECTION:
            // Delete & re-insert
            for (int i=0;i<ta.getCurrent().length;i++)
            {
                DatabaseMeta ci = (DatabaseMeta)ta.getCurrent()[i];
                int idx = ta.getCurrentIndex()[i];

                transMeta.removeDatabase(idx);
                transMeta.addDatabase(idx, ci);
            }
            refreshTree();
            refreshGraph();
            break;

        // We changed a note : undo this...
        case TransAction.TYPE_ACTION_CHANGE_NOTE:
            // Delete & re-insert
            for (int i=0;i<ta.getCurrent().length;i++)
            {
                NotePadMeta ni = (NotePadMeta)ta.getCurrent()[i];
                int idx = ta.getCurrentIndex()[i];

                transMeta.removeNote(idx);
                transMeta.addNote(idx, ni);
            }
            refreshTree();
            refreshGraph();
            break;

        // We changed a hop : undo this...
        case TransAction.TYPE_ACTION_CHANGE_HOP:
            // Delete & re-insert
            for (int i=0;i<ta.getCurrent().length;i++)
            {
                TransHopMeta hi = (TransHopMeta)ta.getCurrent()[i];
                int idx = ta.getCurrentIndex()[i];

                transMeta.removeTransHop(idx);
                transMeta.addTransHop(idx, hi);
            }
            refreshTree();
            refreshGraph();
            break;

        //
        // CHANGE POSITION
        //
        case TransAction.TYPE_ACTION_POSITION_STEP:
            for (int i=0;i<ta.getCurrentIndex().length;i++)
            {
                // Find & change the location of the step:
                StepMeta stepMeta = transMeta.getStep(ta.getCurrentIndex()[i]);
                stepMeta.setLocation(ta.getCurrentLocation()[i]);
            }
            refreshGraph();
            break;
        case TransAction.TYPE_ACTION_POSITION_NOTE:
            for (int i=0;i<ta.getCurrentIndex().length;i++)
            {
                int idx = ta.getCurrentIndex()[i];
                NotePadMeta npi = transMeta.getNote(idx);
                Point curr = ta.getCurrentLocation()[i];
                npi.setLocation(curr);
            }
            refreshGraph();
            break;
        default: break;
        }
        
        // OK, now check if we need to do this again...
        if (transMeta.viewNextUndo()!=null)
        {
            if (transMeta.viewNextUndo().getNextAlso()) redoAction(transMeta);
        }
    }
    
    public void setUndoMenu(TransMeta transMeta)
    {
        if (shell.isDisposed()) return;

        TransAction prev = transMeta!=null ? transMeta.viewThisUndo() : null;
        TransAction next = transMeta!=null ? transMeta.viewNextUndo() : null;
        
        if (prev!=null) 
        {
            miEditUndo.setEnabled(true);
            miEditUndo.setText(Messages.getString("Spoon.Menu.Undo.Available", prev.toString()));//"Undo : "+prev.toString()+" \tCTRL-Z"
        } 
        else            
        {
            miEditUndo.setEnabled(false);
            miEditUndo.setText(Messages.getString("Spoon.Menu.Undo.NotAvailable"));//"Undo : not available \tCTRL-Z"
        } 

        if (next!=null) 
        {
            miEditRedo.setEnabled(true);
            miEditRedo.setText(Messages.getString("Spoon.Menu.Redo.Available",next.toString()));//"Redo : "+next.toString()+" \tCTRL-Y"
        } 
        else            
        {
            miEditRedo.setEnabled(false);
            miEditRedo.setText(Messages.getString("Spoon.Menu.Redo.NotAvailable"));//"Redo : not available \tCTRL-Y"          
        } 
    }


    public void addUndoNew(TransMeta transMeta, Object obj[], int position[])
    {
        addUndoNew(transMeta, obj, position, false);
    }   

    public void addUndoNew(TransMeta transMeta, Object obj[], int position[], boolean nextAlso)
    {
        // New object?
        transMeta.addUndo(obj, null, position, null, null, TransMeta.TYPE_UNDO_NEW, nextAlso);
        setUndoMenu(transMeta);
    }   

    // Undo delete object
    public void addUndoDelete(TransMeta transMeta, Object obj[], int position[])
    {
        addUndoDelete(transMeta, obj, position, false);
    }   

    // Undo delete object
    public void addUndoDelete(TransMeta transMeta, Object obj[], int position[], boolean nextAlso)
    {
        transMeta.addUndo(obj, null, position, null, null, TransMeta.TYPE_UNDO_DELETE, nextAlso);
        setUndoMenu(transMeta);
    }   

    // Change of step, connection, hop or note...
    public void addUndoChange(TransMeta transMeta, Object from[], Object to[], int[] pos)
    {
        addUndoChange(transMeta, from, to, pos, false);
    }

    // Change of step, connection, hop or note...
    public void addUndoChange(TransMeta transMeta, Object from[], Object to[], int[] pos, boolean nextAlso)
    {
        transMeta.addUndo(from, to, pos, null, null, TransMeta.TYPE_UNDO_CHANGE, nextAlso);
        setUndoMenu(transMeta);
    }

    
    
    /**
     * Checks *all* the steps in the transformation, puts the result in remarks list
     */
    public void checkTrans(TransMeta transMeta)
    {
        checkTrans(transMeta, false);
    }
    

    /**
     * Check the steps in a transformation
     * 
     * @param only_selected True: Check only the selected steps...
     */
    public void checkTrans(TransMeta transMeta, boolean only_selected)
    {
        SpoonGraph spoonGraph = findSpoonGraphOfTransformation(transMeta);
        if (spoonGraph==null) return;

        CheckTransProgressDialog ctpd = new CheckTransProgressDialog(shell, transMeta, spoonGraph.getRemarks(), only_selected);
        ctpd.open(); // manages the remarks arraylist...
        showLastTransCheck();
    }

    /**
     * Show the remarks of the last transformation check that was run.
     * @see #checkTrans()
     */
    public void showLastTransCheck()
    {
        TransMeta transMeta = getActiveTransformation();
        if (transMeta==null) return;
        SpoonGraph spoonGraph = findSpoonGraphOfTransformation(transMeta);
        if (spoonGraph==null) return;
        
        CheckResultDialog crd = new CheckResultDialog(shell, SWT.NONE, spoonGraph.getRemarks());
        String stepname = crd.open();
        if (stepname!=null)
        {
            // Go to the indicated step!
            StepMeta stepMeta = transMeta.findStep(stepname);
            if (stepMeta!=null)
            {
                editStep(transMeta, stepMeta);
            }
        }
    }

    public void clearDBCache(TransMeta transMeta)
    {
        clearDBCache(transMeta, null);
    }
    
    public void clearDBCache(TransMeta transMeta, DatabaseMeta databaseMeta)
    {
        if (databaseMeta!=null)
        {
            transMeta.getDbCache().clear(databaseMeta.getName());
        }
        else
        {
            transMeta.getDbCache().clear(null);
        }
    }

    public void exploreDB(TransMeta transMeta, DatabaseMeta databaseMeta)
    {
        DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, databaseMeta, transMeta.getDatabases(), true );
        std.open();
    }
    
    public void analyseImpact(TransMeta transMeta)
    {
        if (transMeta==null) return;
        SpoonGraph spoonGraph = findSpoonGraphOfTransformation(transMeta);
        if (spoonGraph==null) return;

        AnalyseImpactProgressDialog aipd = new AnalyseImpactProgressDialog(shell, transMeta, spoonGraph.getImpact());
        spoonGraph.setImpactFinished( aipd.open() );
        if (spoonGraph.isImpactFinished()) showLastImpactAnalyses(transMeta);
    }
    
    public void showLastImpactAnalyses(TransMeta transMeta)
    {
        if (transMeta==null) return;
        SpoonGraph spoonGraph = findSpoonGraphOfTransformation(transMeta);
        if (spoonGraph==null) return;

        ArrayList rows = new ArrayList();
        for (int i=0;i<spoonGraph.getImpact().size();i++)
        {
            DatabaseImpact ii = (DatabaseImpact)spoonGraph.getImpact().get(i);
            rows.add(ii.getRow());
        }
        
        if (rows.size()>0)
        {
            // Display all the rows...
            PreviewRowsDialog prd = new PreviewRowsDialog(shell, SWT.NONE, "-", rows);
            prd.setTitleMessage(Messages.getString("Spoon.Dialog.ImpactAnalyses.Title"), Messages.getString("Spoon.Dialog.ImpactAnalyses.Message"));//"Impact analyses"  "Result of analyses:"
            prd.open();
        }
        else
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
            if (spoonGraph.isImpactFinished())
            {
                mb.setMessage(Messages.getString("Spoon.Dialog.TransformationNoImpactOnDatabase.Message"));//"As far as I can tell, this transformation has no impact on any database."
            }
            else
            {
                mb.setMessage(Messages.getString("Spoon.Dialog.RunImpactAnalysesFirst.Message"));//"Please run the impact analyses first on this transformation."
            }
            mb.setText(Messages.getString("Spoon.Dialog.ImpactAnalyses.Title"));//Impact
            mb.open();
        }
    }
    
    /**
     * Get & show the SQL required to run the loaded transformation...
     *
     */
    public void getSQL(TransMeta transMeta)
    {
        GetSQLProgressDialog pspd = new GetSQLProgressDialog(shell, transMeta);
        ArrayList stats = pspd.open();
        if (stats!=null) // null means error, but we already displayed the error
        {
            if (stats.size()>0)
            {
                SQLStatementsDialog ssd = new SQLStatementsDialog(shell, SWT.NONE, stats);
                ssd.open();
            }
            else
            {
                MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
                mb.setMessage(Messages.getString("Spoon.Dialog.NoSQLNeedEexecuted.Message"));//As far as I can tell, no SQL statements need to be executed before this transformation can run.
                mb.setText(Messages.getString("Spoon.Dialog.NoSQLNeedEexecuted.Title"));//"SQL"
                mb.open();
            }
        }
    }
    
    
    public void toClipboard(String cliptext)
    {
        GUIResource.getInstance().toClipboard(cliptext);
    }
    
    public String fromClipboard()
    {
        return GUIResource.getInstance().fromClipboard();
    }
    
    /**
     * Paste transformation from the clipboard...
     *
     */
    public void pasteTransformation()
    {
        log.logDetailed(toString(), Messages.getString("Spoon.Log.PasteTransformationFromClipboard"));//"Paste transformation from the clipboard!"
        String xml = fromClipboard();
        try
        {
            Document doc = XMLHandler.loadXMLString(xml);
            TransMeta transMeta = new TransMeta(XMLHandler.getSubNode(doc, "transformation"));
            addSpoonGraph(transMeta); // create a new tab
            refreshGraph();
            refreshTree();
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("Spoon.Dialog.ErrorPastingTransformation.Title"),  Messages.getString("Spoon.Dialog.ErrorPastingTransformation.Message"), e);//Error pasting transformation  "An error occurred pasting a transformation from the clipboard"
        }
    }
    
    public void copyTransformation(TransMeta transMeta)
    {
        toClipboard(XMLHandler.getXMLHeader()+transMeta.getXML());
    }
    
    public void copyTransformationImage(TransMeta transMeta)
    {
        SpoonGraph spoonGraph = getSpoonGraph(transMeta);
        if (spoonGraph==null) return; // TODO: should not happen, check this
        
        Clipboard clipboard = GUIResource.getInstance().getNewClipboard();
        
        Point area = transMeta.getMaximum();
        Image image = spoonGraph.getTransformationImage(Display.getCurrent(), area.x, area.y);
        clipboard.setContents(new Object[] { image.getImageData() }, new Transfer[]{ImageDataTransfer.getInstance()});
    }

	/**
	 * Shows a wizard that creates a new database connection...
	 *
	 */
    private void createDatabaseWizard(TransMeta transMeta)
    {
    	CreateDatabaseWizard cdw=new CreateDatabaseWizard();
    	DatabaseMeta newDBInfo=cdw.createAndRunDatabaseWizard(shell, props, transMeta.getDatabases());
    	if(newDBInfo!=null){ //finished
    		transMeta.addDatabase(newDBInfo);
    		refreshTree();
    		refreshGraph();
    	}
    }
        
    /**
     * Create a transformation that extracts tables & data from a database.<p><p>
     * 
     * 0) Select the database to rip<p>
     * 1) Select the table in the database to copy<p>
     * 2) Select the database to dump to<p>
     * 3) Select the repository directory in which it will end up<p>
     * 4) Select a name for the new transformation<p>
     * 6) Create 1 transformation for the selected table<p> 
     */
    private void copyTableWizard(final TransMeta transMeta)
    {
        final CopyTableWizardPage1 page1 = new CopyTableWizardPage1("1", transMeta.getDatabases());
        page1.createControl(shell);
        final CopyTableWizardPage2 page2 = new CopyTableWizardPage2("2");
        page2.createControl(shell);
        // final CopyTableWizardPage3 page3 = new CopyTableWizardPage3 ("3", rep);
        // page3.createControl(shell);

        Wizard wizard = new Wizard() 
        {
            public boolean performFinish() 
            {
                return copyTable(transMeta, page1.getSourceDatabase(), page1.getTargetDatabase(), page2.getSelection()
                      );
            }
            
            /**
             * @see org.eclipse.jface.wizard.Wizard#canFinish()
             */
            public boolean canFinish()
            {
                return page2.canFinish();
            }
        };
                
        wizard.addPage(page1);
        wizard.addPage(page2);
                
        WizardDialog wd = new WizardDialog(shell, wizard);
        wd.setMinimumPageSize(700,400);
        wd.open();
    }

    public boolean copyTable(TransMeta transMeta, DatabaseMeta sourceDBInfo, DatabaseMeta targetDBInfo, String tablename )
    {
        try
        {
            //
            // Create a new transformation...
            //
            TransMeta meta = new TransMeta();
            meta.addDatabase(sourceDBInfo);
            meta.addDatabase(targetDBInfo);
            
            //
            // Add a note
            //
            String note = Messages.getString("Spoon.Message.Note.ReadInformationFromTableOnDB",tablename,sourceDBInfo.getDatabaseName() )+Const.CR;//"Reads information from table ["+tablename+"] on database ["+sourceDBInfo+"]"
            note+=Messages.getString("Spoon.Message.Note.WriteInformationToTableOnDB",tablename,targetDBInfo.getDatabaseName() );//"After that, it writes the information to table ["+tablename+"] on database ["+targetDBInfo+"]"
            NotePadMeta ni = new NotePadMeta(note, 150, 10, -1, -1);
            meta.addNote(ni);
    
            // 
            // create the source step...
            //
            String fromstepname = Messages.getString("Spoon.Message.Note.ReadFromTable",tablename); //"read from ["+tablename+"]";
            TableInputMeta tii = new TableInputMeta();
            tii.setDatabaseMeta(sourceDBInfo);
            tii.setSQL("SELECT * FROM "+tablename);
            
            StepLoader steploader = StepLoader.getInstance();
            
            String fromstepid = steploader.getStepPluginID(tii);
            StepMeta fromstep = new StepMeta(fromstepid, fromstepname, (StepMetaInterface)tii );
            fromstep.setLocation(150,100);
            fromstep.setDraw(true);
            fromstep.setDescription(Messages.getString("Spoon.Message.Note.ReadInformationFromTableOnDB",tablename,sourceDBInfo.getDatabaseName() ));
            meta.addStep(fromstep);
            
            //
            // add logic to rename fields in case any of the field names contain reserved words...
            // Use metadata logic in SelectValues, use SelectValueInfo...
            //
            Database sourceDB = new Database(sourceDBInfo);
            sourceDB.connect();
            
            // Get the fields for the input table...
            Row fields = sourceDB.getTableFields(tablename);
            
            // See if we need to deal with reserved words...
            int nrReserved = targetDBInfo.getNrReservedWords(fields); 
            if (nrReserved>0)
            {
                SelectValuesMeta svi = new SelectValuesMeta();
                svi.allocate(0,0,nrReserved);
                int nr = 0;
                for (int i=0;i<fields.size();i++)
                {
                    Value v = fields.getValue(i);
                    if (targetDBInfo.isReservedWord( v.getName() ) )
                    {
                        svi.getMetaName()[nr] = v.getName();
                        svi.getMetaRename()[nr] = targetDBInfo.quoteField( v.getName() );
                        nr++;
                    }
                }
                
                String selstepname =Messages.getString("Spoon.Message.Note.HandleReservedWords"); //"Handle reserved words";
                String selstepid = steploader.getStepPluginID(svi);
                StepMeta selstep = new StepMeta(selstepid, selstepname, (StepMetaInterface)svi );
                selstep.setLocation(350,100);
                selstep.setDraw(true);
                selstep.setDescription(Messages.getString("Spoon.Message.Note.RenamesReservedWords",targetDBInfo.getDatabaseTypeDesc()) );//"Renames reserved words for "+targetDBInfo.getDatabaseTypeDesc()
                meta.addStep(selstep);
                
                TransHopMeta shi = new TransHopMeta(fromstep, selstep);
                meta.addTransHop(shi);
                fromstep = selstep;
            }
            
            // 
            // Create the target step...
            //
            //
            // Add the TableOutputMeta step...
            //
            String tostepname = Messages.getString("Spoon.Message.Note.WriteToTable",tablename); // "write to ["+tablename+"]";
            TableOutputMeta toi = new TableOutputMeta();
            toi.setDatabase( targetDBInfo );
            toi.setTablename( tablename );
            toi.setCommitSize( 200 );
            toi.setTruncateTable( true );
            
            String tostepid = steploader.getStepPluginID(toi);
            StepMeta tostep = new StepMeta(tostepid, tostepname, (StepMetaInterface)toi );
            tostep.setLocation(550,100);
            tostep.setDraw(true);
            tostep.setDescription(Messages.getString("Spoon.Message.Note.WriteInformationToTableOnDB2",tablename,targetDBInfo.getDatabaseName() ));//"Write information to table ["+tablename+"] on database ["+targetDBInfo+"]"
            meta.addStep(tostep);
            
            //
            // Add a hop between the two steps...
            //
            TransHopMeta hi = new TransHopMeta(fromstep, tostep);
            meta.addTransHop(hi);
            
            // OK, if we're still here: overwrite the current transformation...
            // Set a name on this generated transformation
            // 
            String name = "Copy table from ["+sourceDBInfo.getName()+"] to ["+targetDBInfo.getName()+"]";
            String transName = name;
            int nr=1;
            if (findTransformation(transName)!=null)
            {
                nr++;
                transName = name+" "+nr;
            }
            meta.setName(transName);
            addSpoonGraph(meta);
            
            refreshGraph();
            refreshTree();
        }
        catch(Exception e)
        {
            new ErrorDialog(shell, Messages.getString("Spoon.Dialog.UnexpectedError.Title"), Messages.getString("Spoon.Dialog.UnexpectedError.Message"), new KettleException(e.getMessage(), e));//"Unexpected error"  "An unexpected error occurred creating the new transformation" 
            return false;
        }
        return true;
    }
        
    public String toString()
    {
        return APP_NAME;
    }
    
    /**
     * This is the main procedure for Spoon.
     * 
     * @param a Arguments are available in the "Get System Info" step.
     */
    public static void main (String [] a) throws KettleException
    {
    	EnvUtil.environmentInit();
        ArrayList args = new ArrayList();
        for (int i=0;i<a.length;i++) args.add(a[i]);
        
        Display display = new Display();
        
        Splash splash = new Splash(display);
        
        StringBuffer optionRepname, optionUsername, optionPassword, optionTransname, optionFilename, optionDirname, optionLogfile, optionLoglevel;

		CommandLineOption options[] = new CommandLineOption[] 
            {
			    new CommandLineOption("rep", "Repository name", optionRepname=new StringBuffer()),
			    new CommandLineOption("user", "Repository username", optionUsername=new StringBuffer()),
			    new CommandLineOption("pass", "Repository password", optionPassword=new StringBuffer()),
			    new CommandLineOption("trans", "The name of the transformation to launch", optionTransname=new StringBuffer()),
			    new CommandLineOption("dir", "The directory (don't forget the leading /)", optionDirname=new StringBuffer()),
			    new CommandLineOption("file", "The filename (Transformation in XML) to launch", optionFilename=new StringBuffer()),
			    new CommandLineOption("level", "The logging level (Basic, Detailed, Debug, Rowlevel, Error, Nothing)", optionLoglevel=new StringBuffer()),
			    new CommandLineOption("logfile", "The logging file to write to", optionLogfile=new StringBuffer()),
			    new CommandLineOption("log", "The logging file to write to (deprecated)", optionLogfile=new StringBuffer(), false, true),
            };

		// Parse the options...
		CommandLineOption.parseArguments(args, options);

        String kettleRepname  = Const.getEnvironmentVariable("KETTLE_REPOSITORY", null);
        String kettleUsername = Const.getEnvironmentVariable("KETTLE_USER", null);
        String kettlePassword = Const.getEnvironmentVariable("KETTLE_PASSWORD", null);
        
        if (!Const.isEmpty(kettleRepname )) optionRepname  = new StringBuffer(kettleRepname);
        if (!Const.isEmpty(kettleUsername)) optionUsername = new StringBuffer(kettleUsername);
        if (!Const.isEmpty(kettlePassword)) optionPassword = new StringBuffer(kettlePassword);
        
        // Before anything else, check the runtime version!!!
        String version = Const.JAVA_VERSION;
        if ("1.4".compareToIgnoreCase(version)>0)
        {
            System.out.println("The System is running on Java version "+version);
            System.out.println("Unfortunately, it needs version 1.4 or higher to run.");
            return;
        }

        // Set default Locale:
        Locale.setDefault(Const.DEFAULT_LOCALE);
        
        LogWriter log;
        LogWriter.setConsoleAppenderDebug();
        if (Const.isEmpty(optionLogfile))
        {
            log=LogWriter.getInstance(Const.SPOON_LOG_FILE, false, LogWriter.LOG_LEVEL_BASIC);
        }
        else
        {
            log=LogWriter.getInstance( optionLogfile.toString(), true, LogWriter.LOG_LEVEL_BASIC );
        }

        if (log.getRealFilename()!=null) log.logBasic(APP_NAME, Messages.getString("Spoon.Log.LoggingToFile")+log.getRealFilename());//"Logging goes to "
        
        if (!Const.isEmpty(optionLoglevel)) 
        {
            log.setLogLevel(optionLoglevel.toString());
            log.logBasic(APP_NAME, Messages.getString("Spoon.Log.LoggingAtLevel")+log.getLogLevelDesc());//"Logging is at level : "
        }
        
        /* Load the plugins etc.*/
        StepLoader stloader = StepLoader.getInstance();
        if (!stloader.read())
        {
            log.logError(APP_NAME, Messages.getString("Spoon.Log.ErrorLoadingAndHaltSystem"));//Error loading steps & plugins... halting Spoon!
            return;
        }
        
        /* Load the plugins etc. we need to load jobentry*/
        JobEntryLoader jeloader = JobEntryLoader.getInstance();
        if (!jeloader.read())
        {
            log.logError("Spoon", "Error loading job entries & plugins... halting Kitchen!");
            return;
        }

        final Spoon win = new Spoon(log, display, null);
        win.setDestroy(true);
        win.setArguments((String[])args.toArray(new String[args.size()]));
        
        log.logBasic(APP_NAME, Messages.getString("Spoon.Log.MainWindowCreated"));//Main window is created.
        
        RepositoryMeta repinfo = null;
        UserInfo userinfo = null;
        
        if (Const.isEmpty(optionRepname) && Const.isEmpty(optionFilename) && win.props.showRepositoriesDialogAtStartup())
        {       
            log.logBasic(APP_NAME, Messages.getString("Spoon.Log.AskingForRepository"));//"Asking for repository"

            int perms[] = new int[] { PermissionMeta.TYPE_PERMISSION_TRANSFORMATION };
            splash.hide();
            RepositoriesDialog rd = new RepositoriesDialog(win.disp, SWT.NONE, perms, Messages.getString("Spoon.Application.Name"));//"Spoon"
            if (rd.open())
            {
                repinfo = rd.getRepository();
                userinfo = rd.getUser();
                if (!userinfo.useTransformations())
                {
                    MessageBox mb = new MessageBox(win.shell, SWT.OK | SWT.ICON_ERROR );
                    mb.setMessage(Messages.getString("Spoon.Dialog.RepositoryUserCannotWork.Message"));//"Sorry, this repository user can't work with transformations from the repository."
                    mb.setText(Messages.getString("Spoon.Dialog.RepositoryUserCannotWork.Title"));//"Error!"
                    mb.open();
                    
                    userinfo = null;
                    repinfo  = null;
                }
            }
            else
            {
                // Exit point: user pressed CANCEL!
                if (rd.isCancelled()) 
                {
                    splash.dispose();
                    win.quitFile();
                    return;
                }
            }
        }
        
        try
        {
            // Read kettle transformation specified on command-line?
            if (!Const.isEmpty(optionRepname) || !Const.isEmpty(optionFilename))
            {           
                if (!Const.isEmpty(optionRepname))
                {
                    RepositoriesMeta repsinfo = new RepositoriesMeta(log);
                    if (repsinfo.readData())
                    {
                        repinfo = repsinfo.findRepository(optionRepname.toString());
                        if (repinfo!=null)
                        {
                            // Define and connect to the repository...
                            win.rep = new Repository(log, repinfo, userinfo);
                            if (win.rep.connect(Messages.getString("Spoon.Application.Name")))//"Spoon"
                            {
                                if (Const.isEmpty(optionDirname)) optionDirname=new StringBuffer(RepositoryDirectory.DIRECTORY_SEPARATOR);
                                
                                // Check username, password
                                win.rep.userinfo = new UserInfo(win.rep, optionUsername.toString(), optionPassword.toString());
                                
                                if (win.rep.userinfo.getID()>0)
                                {
                                    // OK, if we have a specified transformation, try to load it...
                                    // If not, keep the repository logged in.
                                    if (!Const.isEmpty(optionTransname))
                                    {
                                        RepositoryDirectory repdir = win.rep.getDirectoryTree().findDirectory(optionDirname.toString());
                                        if (repdir!=null)
                                        {
                                            TransMeta transMeta = new TransMeta(win.rep, optionTransname.toString(), repdir);
                                            transMeta.setFilename(optionRepname.toString());
                                            transMeta.clearChanged();
                                            
                                            win.addSpoonGraph(transMeta);
                                        }
                                        else
                                        {
                                            log.logError(APP_NAME, Messages.getString("Spoon.Log.UnableFindDirectory",optionDirname.toString()));//"Can't find directory ["+dirname+"] in the repository."
                                        }
                                    }
                                }
                                else
                                {
                                    log.logError(APP_NAME, Messages.getString("Spoon.Log.UnableVerifyUser"));//"Can't verify username and password."
                                    win.rep.disconnect();
                                    win.rep=null;
                                }
                            }
                            else
                            {
                                log.logError(APP_NAME, Messages.getString("Spoon.Log.UnableConnectToRepository"));//"Can't connect to the repository."
                            }
                        }
                        else
                        {
                            log.logError(APP_NAME, Messages.getString("Spoon.Log.NoRepositoryRrovided"));//"No repository provided, can't load transformation."
                        }
                    }
                    else
                    {
                        log.logError(APP_NAME, Messages.getString("Spoon.Log.NoRepositoriesDefined"));//"No repositories defined on this system."
                    }
                }
                else
                if (!Const.isEmpty(optionFilename))
                {
                    TransMeta transMeta = new TransMeta(optionFilename.toString());
                    transMeta.setFilename(optionFilename.toString());
                    transMeta.clearChanged();
                    win.addSpoonGraph(transMeta);
                }
            }
            else // Normal operations, nothing on the commandline...
            {
                // Can we connect to the repository?
                if (repinfo!=null && userinfo!=null)
                {
                    win.rep = new Repository(log, repinfo, userinfo);
                    if (!win.rep.connect(Messages.getString("Spoon.Application.Name"))) //"Spoon"
                    {
                        win.rep = null;
                    }
                }
    
                if (win.props.openLastFile())
                {
                    log.logDetailed(APP_NAME, Messages.getString("Spoon.Log.TryingOpenLastUsedFile"));//"Trying to open the last file used."
                    
                    String  lastfiles[] = win.props.getLastFiles();
                    String  lastdirs[]  = win.props.getLastDirs();
                    boolean lasttypes[] = win.props.getLastTypes();
                    String  lastrepos[] = win.props.getLastRepositories();
            
                    if (lastfiles.length>0)
                    {
                        boolean use_repository = repinfo!=null;
                        
                        // Perhaps we need to connect to the repository?
                        if (lasttypes[0])
                        {
                            if (!Const.isEmpty(lastrepos[0]))
                            {
                                if (use_repository && !lastrepos[0].equalsIgnoreCase(repinfo.getName()))
                                {
                                    // We just asked...
                                    use_repository = false;
                                }
                            }
                        }
                        
                        if (use_repository && lasttypes[0])
                        {
                            if (win.rep!=null) // load from this repository...
                            {
                                if (win.rep.getName().equalsIgnoreCase(lastrepos[0]))
                                {
                                    RepositoryDirectory repdir = win.rep.getDirectoryTree().findDirectory(lastdirs[0]);
                                    if (repdir!=null)
                                    {
                                        log.logDetailed(APP_NAME, Messages.getString("Spoon.Log.AutoLoadingTransformation",lastfiles[0],lastdirs[0]));//"Auto loading transformation ["+lastfiles[0]+"] from repository directory ["+lastdirs[0]+"]"
                                        TransLoadProgressDialog tlpd = new TransLoadProgressDialog(win.shell, win.rep, lastfiles[0], repdir);
                                        TransMeta transMeta = tlpd.open(); // = new TransInfo(log, win.rep, lastfiles[0], repdir);
                                        if (transMeta != null) 
                                        {
                                            transMeta.setFilename(lastfiles[0]);
                                            transMeta.clearChanged();
                                            win.addSpoonGraph(transMeta);
                                        }
                                    }
                                }
                            }
                        }

                        if (!lasttypes[0] && !Const.isEmpty(lastfiles[0]))
                        {
                            TransMeta transMeta = new TransMeta(lastfiles[0]);
                            transMeta.setFilename(lastfiles[0]);
                            transMeta.clearChanged();
                            win.addSpoonGraph(transMeta);
                        }                       
                    }
                }
            }
        }
        catch(KettleException ke)
        {
            log.logError(APP_NAME, Messages.getString("Spoon.Log.ErrorOccurred")+Const.CR+ke.getMessage());//"An error occurred: "
            win.rep=null;
            // ke.printStackTrace();
        }
                
        win.open ();

        splash.dispose();
        
        try
        {
            while (!win.isDisposed ()) 
            {
                if (!win.readAndDispatch ()) win.sleep ();
            }
        }
        catch(Throwable e)
        {
            log.logError(APP_NAME, Messages.getString("Spoon.Log.UnexpectedErrorOccurred")+Const.CR+e.getMessage());//"An unexpected error occurred in Spoon: probable cause: please close all windows before stopping Spoon! "
            e.printStackTrace();
        }
        win.dispose();

        log.logBasic(APP_NAME, APP_NAME+" "+Messages.getString("Spoon.Log.AppHasEnded"));//" has ended."

        // Close the logfile
        log.close();
        
        // Kill all remaining things in this VM!
        System.exit(0);
    }

    /**
     * Create a new SelectValues step in between this step and the previous.
     * If the previous fields are not there, no mapping can be made, same with the required fields.
     * @param stepMeta The target step to map against.
     */
    public void generateMapping(TransMeta transMeta, StepMeta stepMeta)
    {
        try
        {
            if (stepMeta!=null)
            {
                StepMetaInterface smi = stepMeta.getStepMetaInterface();
                Row targetFields = smi.getRequiredFields();
                Row sourceFields = transMeta.getPrevStepFields(stepMeta);
                
                // Build the mapping: let the user decide!!
                String[] source = sourceFields.getFieldNames();
                for (int i=0;i<source.length;i++)
                {
                    Value v = sourceFields.getValue(i);
                    source[i]+=EnterMappingDialog.STRING_ORIGIN_SEPARATOR+v.getOrigin()+")";
                }
                String[] target = targetFields.getFieldNames();
                
                EnterMappingDialog dialog = new EnterMappingDialog(shell, source, target);
                ArrayList mappings = dialog.open();
                if (mappings!=null)
                {
                    // OK, so we now know which field maps where.
                    // This allows us to generate the mapping using a SelectValues Step...
                    SelectValuesMeta svm = new SelectValuesMeta();
                    svm.allocate(mappings.size(), 0, 0);
                    
                    for (int i=0;i<mappings.size();i++)
                    {
                        SourceToTargetMapping mapping = (SourceToTargetMapping) mappings.get(i);
                        svm.getSelectName()[i] = sourceFields.getValue(mapping.getSourcePosition()).getName();
                        svm.getSelectRename()[i] = target[mapping.getTargetPosition()];
                        svm.getSelectLength()[i] = -1;
                        svm.getSelectPrecision()[i] = -1;
                    }
                    
                    // Now that we have the meta-data, create a new step info object
                    
                    String stepName = stepMeta.getName()+" Mapping";
                    stepName = transMeta.getAlternativeStepname(stepName);  // if it's already there, rename it.
                    
                    StepMeta newStep = new StepMeta("SelectValues", stepName, svm);
                    newStep.setLocation(stepMeta.getLocation().x+20, stepMeta.getLocation().y+20);
                    newStep.setDraw(true);

                    transMeta.addStep(newStep);
                    addUndoNew(transMeta, new StepMeta[] { newStep }, new int[] { transMeta.indexOfStep(newStep) });
                    
                    // Redraw stuff...
                    refreshTree();
                    refreshGraph();
                }
            }
            else
            {
                System.out.println("No target to do mapping against!");
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, "Error creating mapping", "There was an error when Kettle tried to generate a mapping against the target step", e);
        }
    }

    public void editPartitioning(TransMeta transMeta, StepMeta stepMeta)
    {
        StepPartitioningMeta stepPartitioningMeta = stepMeta.getStepPartitioningMeta();
        if (stepPartitioningMeta==null) stepPartitioningMeta = new StepPartitioningMeta();
        
        String[] options = StepPartitioningMeta.methodDescriptions;
        EnterSelectionDialog dialog = new EnterSelectionDialog(shell, options, "Partioning method", "Select the partitioning method");
        String methodDescription = dialog.open(stepPartitioningMeta.getMethod());
        if (methodDescription!=null)
        {
            int method = StepPartitioningMeta.getMethod(methodDescription);
            stepPartitioningMeta.setMethod(method);
            switch(method)
            {
            case StepPartitioningMeta.PARTITIONING_METHOD_NONE:  break;
            case StepPartitioningMeta.PARTITIONING_METHOD_MOD:
                // ask for a fieldname
                EnterStringDialog stringDialog = new EnterStringDialog(shell, props, Const.NVL(stepPartitioningMeta.getFieldName(), ""), "Fieldname", "Enter a field name to partition on");
                String fieldName = stringDialog.open();
                stepPartitioningMeta.setFieldName(fieldName);

                // Ask for a Partitioning Schema
                String schemaNames[] = transMeta.getPartitionSchemasNames();
                if (schemaNames.length==0)
                {
                    MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                    box.setText("Create a partition schema");
                    box.setMessage("You first need to create one or more partition schemas in the transformation settings dialog before you can select one!");
                    box.open();
                }
                else
                {
                    // Set the partitioning schema too.
                    PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
                    int idx = -1;
                    if (partitionSchema!=null)
                    {
                        idx = Const.indexOfString(partitionSchema.getName(), schemaNames);
                    }
                    EnterSelectionDialog askSchema = new EnterSelectionDialog(shell, schemaNames, "Select a partition schema", "Select the partition schema to use:");
                    String schemaName = askSchema.open(idx);
                    if (schemaName!=null)
                    {
                        idx = Const.indexOfString(schemaName, schemaNames);
                        stepPartitioningMeta.setPartitionSchema((PartitionSchema) transMeta.getPartitionSchemas().get(idx));
                    }
                }
                break;
            }
            refreshGraph();
        }
    }
    
    /**
     * Select a clustering schema for this step.
     * 
     * @param stepMeta The step to set the clustering schema for.
     */
    public void editClustering(TransMeta transMeta, StepMeta stepMeta)
    {
        int idx = -1;
        if (stepMeta.getClusterSchema()!=null)
        {
            idx = transMeta.getClusterSchemas().indexOf( stepMeta.getClusterSchema() );
        }
        String[] clusterSchemaNames = transMeta.getClusterSchemaNames();
        EnterSelectionDialog dialog = new EnterSelectionDialog(shell, clusterSchemaNames, "Cluster schema", "Select the cluster schema to use (cancel=clear)");
        String schemaName = dialog.open(idx);
        if (schemaName==null)
        {
            stepMeta.setClusterSchema(null);
        }
        else
        {
            ClusterSchema clusterSchema = transMeta.findClusterSchema(schemaName);
            stepMeta.setClusterSchema( clusterSchema );
        }
        
        refreshTree();
        refreshGraph();
    }

    
    public void createKettleArchive(TransMeta transMeta)
    {
        JarfileGenerator.generateJarFile(transMeta);
    }
    


    /**
     * This creates a new database partitioning schema, edits it and adds it to the transformation metadata
     *
     */
    public void newDatabasePartitioningSchema(TransMeta transMeta)
    {
        PartitionSchema partitionSchema = new PartitionSchema();
        
        PartitionSchemaDialog dialog = new PartitionSchemaDialog(shell, partitionSchema, transMeta.getDatabases());
        if (dialog.open())
        {
            transMeta.getPartitionSchemas().add(partitionSchema);
            refreshTree();
        }
    }
    
    private void editPartitionSchema(TransMeta transMeta, PartitionSchema partitionSchema)
    {
        PartitionSchemaDialog dialog = new PartitionSchemaDialog(shell, partitionSchema, transMeta.getDatabases());
        if (dialog.open())
        {
            refreshTree();
        }
    }
    

    private void delPartitionSchema(TransMeta transMeta, PartitionSchema partitionSchema)
    {
        int idx = transMeta.getPartitionSchemas().indexOf(partitionSchema);
        transMeta.getPartitionSchemas().remove(idx);
        refreshTree();
    }

    /**
     * This creates a new clustering schema, edits it and adds it to the transformation metadata
     *
     */
    public void newClusteringSchema(TransMeta transMeta)
    {
        ClusterSchema clusterSchema = new ClusterSchema();
        
        ClusterSchemaDialog dialog = new ClusterSchemaDialog(shell, clusterSchema, transMeta.getSlaveServers());
        if (dialog.open())
        {
            transMeta.getClusterSchemas().add(clusterSchema);
            refreshTree();
        }
    }

    private void editClusterSchema(TransMeta transMeta, ClusterSchema clusterSchema)
    {
        ClusterSchemaDialog dialog = new ClusterSchemaDialog(shell, clusterSchema, transMeta.getSlaveServers());
        if (dialog.open())
        {
            refreshTree();
        }
    }

    private void delClusterSchema(TransMeta transMeta, ClusterSchema clusterSchema)
    {
        int idx = transMeta.getClusterSchemas().indexOf(clusterSchema);
        transMeta.getClusterSchemas().remove(idx);
        refreshTree();
    }
    
    /**
     * This creates a slave server, edits it and adds it to the transformation metadata
     *
     */
    public void newSlaveServer(TransMeta transMeta)
    {
        SlaveServer slaveServer = new SlaveServer();
        
        SlaveServerDialog dialog = new SlaveServerDialog(shell, slaveServer);
        if (dialog.open())
        {
            transMeta.getSlaveServers().add(slaveServer);
            refreshTree();
        }
    }

    private void delSlaveServer(TransMeta transMeta, SlaveServer slaveServer)
    {
        // TODO: remove from cluster schemas that use it as well.
        //
        int idx = transMeta.getSlaveServers().indexOf(slaveServer);
        transMeta.getSlaveServers().remove(idx);
        refreshTree();
    }
    
    
    /**
     * Sends transformation to slave server
     * @param executionConfiguration 
     */
    public void sendXMLToSlaveServer(TransMeta transMeta, TransExecutionConfiguration executionConfiguration)
    {
        SlaveServer slaveServer = executionConfiguration.getRemoteServer();
        
        try
        {
            if (slaveServer==null) throw new KettleException("No slave server specified");
            if (Const.isEmpty(transMeta.getName())) throw new KettleException("The transformation needs a name to uniquely identify it by on the remote server.");
            
            String reply = slaveServer.sendXML(new TransConfiguration(transMeta, executionConfiguration).getXML(), AddTransServlet.CONTEXT_PATH+"?xml=Y");
            WebResult webResult = WebResult.fromXMLString(reply);
            if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
            {
                throw new KettleException("There was an error posting the transformation on the remote server: "+Const.CR+webResult.getMessage());
            }
            
            reply = slaveServer.getContentFromServer(PrepareExecutionTransHandler.CONTEXT_PATH+"?name="+transMeta.getName()+"&xml=Y");
            webResult = WebResult.fromXMLString(reply);
            if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
            {
                throw new KettleException("There was an error preparing the transformation for excution on the remote server: "+Const.CR+webResult.getMessage());
            }
            
            reply = slaveServer.getContentFromServer(StartExecutionTransHandler.CONTEXT_PATH+"?name="+transMeta.getName()+"&xml=Y");
            webResult = WebResult.fromXMLString(reply);
            if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
            {
                throw new KettleException("There was an error starting the transformation on the remote server: "+Const.CR+webResult.getMessage());
            }
        }
        catch (Exception e)
        {
            new ErrorDialog(shell, "Error", "Error sending transformation to server", e);
        }
    }

    private void splitTrans(TransMeta transMeta, boolean show, boolean post, boolean prepare, boolean start)
    {
        try
        {
            if (Const.isEmpty(transMeta.getName())) throw new KettleException("The transformation needs a name to uniquely identify it by on the remote server.");

            TransSplitter transSplitter = new TransSplitter(transMeta);
            transSplitter.splitOriginalTransformation(shell);
            
            // Send the transformations to the servers...
            //
            // First the master...
            //
            TransMeta master = transSplitter.getMaster();
            SlaveServer masterServer = transSplitter.getMasterServer();
            if (show) addSpoonGraph(master);
            if (post)
            {
                String masterReply = masterServer.sendXML(new TransConfiguration(master, executionConfiguration).getXML(), AddTransServlet.CONTEXT_PATH+"?xml=Y");
                WebResult webResult = WebResult.fromXMLString(masterReply);
                if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                {
                    throw new KettleException("An error occurred sending the master transformation: "+webResult.getMessage());
                }
            }
            
            // Then the slaves...
            //
            SlaveServer slaves[] = transSplitter.getSlaveTargets();
            for (int i=0;i<slaves.length;i++)
            {
                TransMeta slaveTrans = (TransMeta) transSplitter.getSlaveTransMap().get(slaves[i]);
                if (show) addSpoonGraph(slaveTrans);
                if (post)
                {
                    String slaveReply = slaves[i].sendXML(new TransConfiguration(slaveTrans, executionConfiguration).getXML(), AddTransServlet.CONTEXT_PATH+"?xml=Y");
                    WebResult webResult = WebResult.fromXMLString(slaveReply);
                    if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                    {
                        throw new KettleException("An error occurred sending a slave transformation: "+webResult.getMessage());
                    }
                }
            }
            
            if (post)
            {
                if (prepare)
                {
                    // Prepare the master...
                    String masterReply = masterServer.getContentFromServer(PrepareExecutionTransHandler.CONTEXT_PATH+"?name="+master.getName()+"&xml=Y");
                    WebResult webResult = WebResult.fromXMLString(masterReply);
                    if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                    {
                        throw new KettleException("An error occurred while preparing the execution of the master transformation: "+webResult.getMessage());
                    }
                    
                    // Prepare the slaves
                    for (int i=0;i<slaves.length;i++)
                    {
                        TransMeta slaveTrans = (TransMeta) transSplitter.getSlaveTransMap().get(slaves[i]);
                        String slaveReply = slaves[i].getContentFromServer(PrepareExecutionTransHandler.CONTEXT_PATH+"?name="+slaveTrans.getName()+"&xml=Y");
                        webResult = WebResult.fromXMLString(slaveReply);
                        if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                        {
                            throw new KettleException("An error occurred while preparing the execution of a slave transformation: "+webResult.getMessage());
                        }
                    }
                }
                
                if (start)
                {
                    // Start the master...
                    String masterReply = masterServer.getContentFromServer(StartExecutionTransHandler.CONTEXT_PATH+"?name="+master.getName()+"&xml=Y");
                    WebResult webResult = WebResult.fromXMLString(masterReply);
                    if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                    {
                        throw new KettleException("An error occurred while starting the execution of the master transformation: "+webResult.getMessage());
                    }
                    
                    // Start the slaves
                    for (int i=0;i<slaves.length;i++)
                    {
                        TransMeta slaveTrans = (TransMeta) transSplitter.getSlaveTransMap().get(slaves[i]);
                        String slaveReply = slaves[i].getContentFromServer(StartExecutionTransHandler.CONTEXT_PATH+"?name="+slaveTrans.getName()+"&xml=Y");
                        webResult = WebResult.fromXMLString(slaveReply);
                        if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                        {
                            throw new KettleException("An error occurred while starting the execution of a slave transformation: "+webResult.getMessage());
                        }
                    }
                }
                
                // Now add monitors for the master and all the slave servers
                addSpoonSlave(masterServer);
                for (int i=0;i<slaves.length;i++)
                {
                    addSpoonSlave(slaves[i]);
                }
            }
        }
        catch(Exception e)
        {
            new ErrorDialog(shell, "Split transformation", "There was an error during transformation split", e);
        }
    }

    public void executeTransformation(TransMeta transMeta, boolean local, boolean remote, boolean cluster, boolean preview, Date replayDate)
    {
        if (transMeta==null) return;
        
        executionConfiguration.setExecutingLocally(local);
        executionConfiguration.setExecutingRemotely(remote);
        executionConfiguration.setExecutingClustered(cluster);
        
        executionConfiguration.getUsedVariables(transMeta);
        executionConfiguration.getUsedArguments(transMeta, arguments);
        executionConfiguration.setReplayDate(replayDate);
        executionConfiguration.setLocalPreviewing(preview);
        
        executionConfiguration.setLogLevel(log.getLogLevel());
        // executionConfiguration.setSafeModeEnabled( spoonLog!=null && spoonLog.isSafeModeChecked() );
        
        TransExecutionConfigurationDialog dialog = new TransExecutionConfigurationDialog(shell, executionConfiguration, transMeta);
        if (dialog.open())
        {
            addSpoonLog(transMeta);
            SpoonLog spoonLog = getActiveSpoonLog();
            
            if (executionConfiguration.isExecutingLocally())
            {
                if (executionConfiguration.isLocalPreviewing())
                {
                    spoonLog.preview(executionConfiguration);
                }
                else
                {
                    spoonLog.start(executionConfiguration);
                }
            }
            else if(executionConfiguration.isExecutingRemotely())
            {
                if (executionConfiguration.getRemoteServer()!=null)
                {
                    sendXMLToSlaveServer(transMeta, executionConfiguration);
                    addSpoonSlave(executionConfiguration.getRemoteServer());
                }
                else
                {
                    MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
                    mb.setMessage(Messages.getString("Spoon.Dialog.NoRemoteServerSpecified.Message")); 
                    mb.setText(Messages.getString("Spoon.Dialog.NoRemoteServerSpecified.Title"));
                    mb.open();
                }
            }
            else if (executionConfiguration.isExecutingClustered())
            {
                splitTrans( transMeta,
                        executionConfiguration.isClusterShowingTransformation(), 
                        executionConfiguration.isClusterPosting(),
                        executionConfiguration.isClusterPreparing(),
                        executionConfiguration.isClusterStarting()
                        );
            }
        }
    }
    
    public CTabItem findCTabItem(String text)
    {
        CTabItem[] items = tabfolder.getItems();
        for (int i=0;i<items.length;i++)
        {
            if (items[i].getText().equalsIgnoreCase(text)) return items[i];
        }
        return null;
    }

    private void addSpoonSlave(SlaveServer slaveServer)
    {
        // See if there is a SpoonSlave for this slaveServer...
        String tabName = makeSlaveTabName(slaveServer);
        CTabItem tabItem=findCTabItem(tabName);
        if (tabItem==null)
        {
            SpoonSlave spoonSlave = new SpoonSlave(tabfolder, SWT.NONE, this, slaveServer);
            tabItem = new CTabItem(tabfolder, SWT.CLOSE);
            tabItem.setText(tabName);
            tabItem.setToolTipText("Status of slave server : "+slaveServer.getName()+" : "+slaveServer.getServerAndPort());
            tabItem.setControl(spoonSlave);
            
            tabMap.put(tabName, new TabMapEntry(tabItem, tabName, spoonSlave));
        }
        int idx = tabfolder.indexOf(tabItem);
        tabfolder.setSelection(idx);        
    }
    
    public void addSpoonGraph(TransMeta transMeta)
    {
        String key = addTransformation(transMeta);
        if (key!=null)
        {
            // See if there already is a tab for this graph
            // If no, add it
            // If yes, select that tab
            //
            String tabName = makeGraphTabName(transMeta);
            CTabItem tabItem=findCTabItem(tabName);
            if (tabItem==null)
            {
                SpoonGraph spoonGraph = new SpoonGraph(tabfolder, this, transMeta);
                tabItem = new CTabItem(tabfolder, SWT.CLOSE);
                tabItem.setText(tabName);
                tabItem.setToolTipText("Graphical view of Transformation : "+tabName);
                tabItem.setImage(GUIResource.getInstance().getImageSpoonGraph());
                tabItem.setControl(spoonGraph);
                
                tabMap.put(tabName, new TabMapEntry(tabItem, tabName, spoonGraph));
            }
            int idx = tabfolder.indexOf(tabItem);
            
            // OK, also see if we need to open a new history window.
            if (transMeta.getLogConnection()!=null && !Const.isEmpty(transMeta.getLogTable()))
            {
                addSpoonHistory(transMeta, false);
            }
            // keep the focus on the graph
            tabfolder.setSelection(idx);
        }
    }
    
    public String makeLogTabName(TransMeta transMeta)
    {
        return "Log: "+makeGraphTabName(transMeta);
    }
    
    public String makeGraphTabName(TransMeta transMeta)
    {
        if (Const.isEmpty(transMeta.getName()))
        {
            if (Const.isEmpty(transMeta.getFilename()))
            {
                return STRING_TRANS_NO_NAME;
            }
            return transMeta.getFilename();
        }
        return transMeta.getName();
    }
    
    public String makeHistoryTabName(TransMeta transMeta)
    {
        return "History: "+makeGraphTabName(transMeta);
    }
    
    public String makeSlaveTabName(SlaveServer slaveServer)
    {
        return "Slave server: "+slaveServer.getName();
    }
    
    
    public void addSpoonLog(TransMeta transMeta)
    {
        // See if there already is a tab for this log
        // If no, add it
        // If yes, select that tab
        //
        String tabName = makeLogTabName(transMeta);
        CTabItem tabItem=findCTabItem(tabName);
        if (tabItem==null)
        {
            SpoonLog spoonLog = new SpoonLog(tabfolder, this, transMeta);
            tabItem = new CTabItem(tabfolder, SWT.CLOSE);
            tabItem.setText(tabName);
            tabItem.setToolTipText("Execution log for transformation : "+makeGraphTabName(transMeta));
            tabItem.setControl(spoonLog);

            // If there is an associated history window, we want to keep that one up-to-date as well.
            //
            SpoonHistory spoonHistory = findSpoonHistoryOfTransformation(transMeta);
            CTabItem historyItem = findCTabItem(makeHistoryTabName(transMeta));
            
            if (spoonHistory!=null && historyItem!=null)
            {
                SpoonHistoryRefresher spoonHistoryRefresher = new SpoonHistoryRefresher(historyItem, spoonHistory);
                tabfolder.addSelectionListener(spoonHistoryRefresher);
                spoonLog.setSpoonHistoryRefresher(spoonHistoryRefresher);
            }

            
            tabMap.put(tabName, new TabMapEntry(tabItem, tabName, spoonLog));
        }
        int idx = tabfolder.indexOf(tabItem);
        tabfolder.setSelection(idx);
    }
    
    public void addSpoonHistory(TransMeta transMeta, boolean select)
    {
        // See if there already is a tab for this history view
        // If no, add it
        // If yes, select that tab
        //
        String tabName = makeHistoryTabName(transMeta);
        CTabItem tabItem=findCTabItem(tabName);
        if (tabItem==null)
        {
            SpoonHistory spoonHistory = new SpoonHistory(tabfolder, this, transMeta);
            tabItem = new CTabItem(tabfolder, SWT.CLOSE);
            tabItem.setText(tabName);
            tabItem.setToolTipText("Execution history for transformation : "+makeGraphTabName(transMeta));
            tabItem.setControl(spoonHistory);
            
            // If there is an associated log window that's open, find it and add a refresher
            SpoonLog spoonLog = findSpoonLogOfTransformation(transMeta);
            if (spoonLog!=null)
            {
                SpoonHistoryRefresher spoonHistoryRefresher = new SpoonHistoryRefresher(tabItem, spoonHistory);
                tabfolder.addSelectionListener(spoonHistoryRefresher);
                spoonLog.setSpoonHistoryRefresher(spoonHistoryRefresher);
            }
            spoonHistory.markRefreshNeeded(); // will refresh when first selected
                        
            tabMap.put(tabName, new TabMapEntry(tabItem, tabName, spoonHistory));
        }
        if (select)
        {
            int idx = tabfolder.indexOf(tabItem);
            tabfolder.setSelection(idx);
        }
    }

    /**
     * Rename the tabs
     */
    public void renameTabs()
    {
        Collection collection = tabMap.values();
        List list = new ArrayList();
        list.addAll(collection);
        
        for (Iterator iter = list.iterator(); iter.hasNext();)
        {
            TabMapEntry entry = (TabMapEntry) iter.next();
            if (entry.getTabItem().isDisposed())
            {
                // this should not be in the map, get rid of it.
                tabMap.remove(entry.getObjectName());
                continue;
            }
            
            String before = entry.getTabItem().getText();
            if (entry.getObject() instanceof SpoonGraph)
            {
                entry.getTabItem().setText( makeGraphTabName( (TransMeta) entry.getObject().getManagedObject() ) );
            }
            if (entry.getObject() instanceof SpoonLog) entry.getTabItem().setText( makeLogTabName( (TransMeta) entry.getObject().getManagedObject() ) );
            if (entry.getObject() instanceof SpoonHistory) entry.getTabItem().setText( makeHistoryTabName( (TransMeta) entry.getObject().getManagedObject() ) );
            String after = entry.getTabItem().getText();

            if (!before.equals(after))
            {
                entry.setObjectName(after);
                tabMap.remove(before);
                tabMap.put(after, entry);
                
                // Also change the transformation map
                if (entry.getObject() instanceof SpoonGraph)
                {
                    transformationMap.remove(before);
                    transformationMap.put(after, entry.getObject().getManagedObject());
                }
                
                System.out.println("Renamed tab ["+before+"] to ["+after+"]");
            }
        }
    }
    
    /**
     * This contains a map between the name of a transformation and the TransMeta object.
     * If the transformation has no name it will be mapped under a number [1], [2] etc. 

     * @return the transformation map
     */
    public Map getTransformationMap()
    {
        return transformationMap;
    }

    /**
     * @param transformationMap the transformation map to set
     */
    public void setTransformationMap(Map transformationMap)
    {
        this.transformationMap = transformationMap;
    }

    /**
     * @return the tabMap
     */
    public Map getTabMap()
    {
        return tabMap;
    }

    /**
     * @param tabMap the tabMap to set
     */
    public void setTabMap(Map tabMap)
    {
        this.tabMap = tabMap;
    }
    
}

