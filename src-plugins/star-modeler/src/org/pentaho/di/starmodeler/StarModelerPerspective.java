package org.pentaho.di.starmodeler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleObjectExistsException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.starmodeler.generator.JobGenerator;
import org.pentaho.di.starmodeler.generator.MetadataGenerator;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.FileListener;
import org.pentaho.di.ui.spoon.MainSpoonPerspective;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveListener;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.util.SerializationService;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulTab;
import org.pentaho.ui.xul.components.XulTabpanel;
import org.pentaho.ui.xul.containers.XulTabbox;
import org.pentaho.ui.xul.containers.XulTabpanels;
import org.pentaho.ui.xul.containers.XulTabs;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.pentaho.ui.xul.swt.tags.SwtTab;
import org.pentaho.ui.xul.util.XulDialogCallback;
import org.w3c.dom.Node;

public class StarModelerPerspective extends AbstractXulEventHandler implements SpoonPerspective, FileListener, XulEventHandler {

  private static Class<?> PKG = StarModelerPerspective.class; // for i18n
  
  private LogChannelInterface logger = LogChannel.GENERAL;
  
  protected XulDomContainer container;
  protected XulRunner runner;
  
  protected Document document;
  protected XulTabs tabs;
  protected XulTabpanels panels;
  protected XulTabbox tabbox;
  protected List<SpoonPerspectiveListener> listeners = new ArrayList<SpoonPerspectiveListener>();
  protected EngineMetaInterface selectedMeta;
  protected List<StarDomain> models = new ArrayList<StarDomain>();
  
  protected Map<XulTab, EngineMetaInterface> metas = new WeakHashMap<XulTab, EngineMetaInterface>();

  
  private String defaultLocale = LanguageChoice.getInstance().getDefaultLocale().toString();
  
  private String defaultExtension="star";

  private static StarModelerPerspective instance;
  
  public class XulTabAndPanel{
    public XulTab tab;
    public XulTabpanel panel;
    public XulTabAndPanel(XulTab tab, XulTabpanel panel){
      this.tab = tab;
      this.panel = panel;
    }
  }
  
  public XulTabAndPanel createTab(){

    try {
      XulTab tab = (XulTab) document.createElement("tab");
      if(name != null){
        tab.setLabel(name);
      }
      XulTabpanel panel = (XulTabpanel) document.createElement("tabpanel"); //$NON-NLS-1
      panel.setSpacing(0);
      panel.setPadding(0);
      
      tabs.addChild(tab);
      panels.addChild(panel);
      tabbox.setSelectedIndex(panels.getChildNodes().indexOf(panel));

      return new XulTabAndPanel(tab, panel);
      
    } catch (XulException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public void setMetaForTab(XulTab tab, EngineMetaInterface meta){
    metas.put(tab, meta);
  }
  
  private StarModelerPerspective() {
    
    String perspectiveSrc = "org/pentaho/di/starmodeler/xul/perspective.xul";
    try {
      SwtXulLoader loader = new SwtXulLoader();
      loader.registerClassLoader(getClass().getClassLoader());
      
      Spoon.getInstance().addFileListener(this);
      
      container = loader.loadXul(perspectiveSrc, new PDIMessages(this.getClass())); //$NON-NLS-1$

      runner = new SwtXulRunner();
      runner.addContainer(container);
      runner.initialize();

      document = container.getDocumentRoot();
      container.addEventHandler(this);
      tabs = (XulTabs) document.getElementById("tabs");
      panels = (XulTabpanels) document.getElementById("tabpanels");
      tabbox = (XulTabbox) tabs.getParent();
      BindingFactory bf = new DefaultBindingFactory();
      setDefaultExtension("star");
      bf.setDocument(document);
      
      bf.createBinding(tabbox, "selectedIndex", this, "selectedMeta", new BindingConvertor<Integer, EngineMetaInterface>() {
        public EngineMetaInterface sourceToTarget(Integer value) {
          return metas.get(tabs.getTabByIndex(value));
        }

        public Integer targetToSource(EngineMetaInterface value) {
          for (XulTab tab : metas.keySet()) {
            if (metas.get(tab) == value) {
              return tab.getParent().getChildNodes().indexOf(tab);
            }
          }
          return -1;
        }
      });


    } catch (Exception e) {
      logger.logError("Error initializing perspective", e);
    }

  }
  
  public static StarModelerPerspective getInstance() {
    if (instance==null) {
      instance=new StarModelerPerspective();
    }
    return instance;
  }

  @Override
  public String getDisplayName(Locale l) {
    return BaseMessages.getString(PKG, "StarModelerPerspective.Perspective.Name");
  }

  @Override
  public InputStream getPerspectiveIcon() {
    ClassLoader loader = getClass().getClassLoader();
    return loader.getResourceAsStream("org/pentaho/di/starmodeler/images/starmodeler.png");
  }
  
  @Override
  public String getId() {
    return "020-StarModeler";
  }
  
  public boolean acceptsXml(String nodeName) {
    return false;
  }
  
  public String[] getFileTypeDisplayNames(Locale locale) {
    return new String[]{ "Star models" };
  }
  
  public String[] getSupportedExtensions() {
    return new String[]{ "star" };
  }
  
  public final Composite getUI() {
    return (Composite) container.getDocumentRoot().getRootElement().getFirstChild().getManagedObject();
  }

  
  
  
  @Override
  public boolean open(Node transNode, String fname, boolean importfile) {
    try {
      String xml = KettleVFS.getTextFileContent(fname, Const.XML_ENCODING);
      Domain domain = new SerializationService().deserializeDomain(xml);
      StarDomain starDomain = new StarDomain();
      starDomain.setDomain(domain);
      starDomain.setFilename(fname);
      createTabForModel(starDomain, ModelerHelper.MODELER_NAME);
      PropsUI.getInstance().addLastFile(LastUsedFile.FILE_TYPE_SCHEMA, fname, null, false, null);
      Spoon.getInstance().addMenuLast();
      return true;
    } catch(Exception e) {
      new ErrorDialog(Spoon.getInstance().getShell(), "Error", "There was an error opening model from file '"+fname+"'", e);
    }
    
    return false;
  }

  @Override
  public boolean save(EngineMetaInterface meta, String fname, boolean isExport) {
    try {
      
      String xml = meta.getXML();
      OutputStream outputStream = KettleVFS.getOutputStream(fname, false);
      outputStream.write(xml.getBytes(Const.XML_ENCODING));
      outputStream.close();
      
      meta.setFilename(fname);
      meta.clearChanged();
      Spoon.getInstance().enableMenus();
      
      return true;
    } catch(Exception e) {
      new ErrorDialog(Spoon.getInstance().getShell(), "Error saving model", "There was an error while saving the model:", e);
    }
    
    return false;
  }

  @Override
  public void syncMetaName(EngineMetaInterface meta, String name) {
  }

  @Override
  public boolean accepts(String fileName) {
    if(fileName == null || fileName.indexOf('.') == -1){
      return false;
    }
    String extension = fileName.substring(fileName.lastIndexOf('.')+1);
    return extension.equals(defaultExtension);
  }

  @Override
  public String getRootNodeName() {
    return null;
  }


  @Override
  public void setActive(boolean active) {
    for(SpoonPerspectiveListener listener : listeners){
      if(active){
        listener.onActivation();
      } else {
        listener.onDeactication();
      }
    }
  }

  @Override
  public List<XulOverlay> getOverlays() {
    return null;
  }

  @Override
  public List<XulEventHandler> getEventHandlers() {
    return null;
  }

  @Override
  public void addPerspectiveListener(SpoonPerspectiveListener listener) {
    if(listeners.contains(listener) == false){
      listeners.add(listener);
    }
  }

  
  
  
  
  
  @Override
  public Object getData() {
    return null;
  }
  
  @Override
  public String getName() {
    return "starModelerPerspective";
  }
  
  @Override
  public XulDomContainer getXulDomContainer() {
    return null;
  }
  
  @Override
  public void setData(Object arg0) {
  }
  
  @Override
  public void setName(String arg0) {
  }
  
  @Override
  public void setXulDomContainer(XulDomContainer arg0) {
    // TODO Auto-generated method stub
    
  }
  
  public void setNameForTab(XulTab tab, String name){
    String tabName = name;
    List<String> usedNames = new ArrayList<String>();
    for(XulComponent c : tabs.getChildNodes()){
      if(c != tab){
        usedNames.add(((SwtTab) c).getLabel());
      }
    }
    if(usedNames.contains(name)){
      int num = 2;
      while(true){
        tabName = name +" ("+num+")";
        if(usedNames.contains(tabName) == false){
          break;
        }
        num++;
      }
    }
    
    tab.setLabel(tabName);
  }
  

  public void createTabForModel(final StarDomain starDomain, final String modelerName) throws Exception {
    SpoonPerspectiveManager.getInstance().activatePerspective(getClass());
    
    final XulTabAndPanel tabAndPanel = createTab();
    PropsUI props = PropsUI.getInstance();

    final Composite comp = (Composite) tabAndPanel.panel.getManagedObject();
    props.setLook(comp);
    comp.setLayout(new FillLayout());
    
    final ScrolledComposite scrolledComposite = new ScrolledComposite(comp, SWT.V_SCROLL | SWT.H_SCROLL);
    props.setLook(scrolledComposite);
    scrolledComposite.setLayout(new FillLayout());

    final Composite parentComposite = new Composite(scrolledComposite, SWT.NONE);
    props.setLook(parentComposite);
    
    int margin = Const.MARGIN;
    int middle = Const.MIDDLE_PCT;
    
    FormLayout formLayout = new FormLayout();
    formLayout.marginLeft=10;
    formLayout.marginRight=10;
    formLayout.marginTop=10;
    formLayout.marginBottom=10;
    formLayout.spacing=margin;
    parentComposite.setLayout(formLayout);
    
    // Add a group for the logical stars
    // Then below one for the physical hints...
    //
    final Group logicalGroup = new Group(parentComposite, SWT.SHADOW_NONE);
    props.setLook(logicalGroup);
    logicalGroup.setText(BaseMessages.getString(PKG, "StarModelerPerspective.LogicalGroup.Label"));
    FormLayout groupLayout = new FormLayout();
    groupLayout.marginLeft=10;
    groupLayout.marginRight=10;
    groupLayout.marginTop=10;
    groupLayout.marginBottom=10;
    groupLayout.spacing=margin;
    logicalGroup.setLayout(groupLayout);
    
    // Add a line to edit the name of the logical domain
    //
    Label nameLabel = new Label(logicalGroup, SWT.RIGHT);
    props.setLook(nameLabel);
    nameLabel.setText(BaseMessages.getString(PKG, "StarModelerPerspective.DomainName.Label"));
    FormData fdNameLabel = new FormData();
    fdNameLabel.left=new FormAttachment(0, 0);
    fdNameLabel.right=new FormAttachment(middle, 0);
    fdNameLabel.top =new FormAttachment(0, 0);
    nameLabel.setLayoutData(fdNameLabel);
    
    final Text nameText = new Text(logicalGroup, SWT.BORDER | SWT.SINGLE);
    props.setLook(nameText);
    nameText.setText(Const.NVL(starDomain.getDomain().getName(defaultLocale), ""));
    FormData fdNameText = new FormData();
    fdNameText.left=new FormAttachment(middle, margin);
    fdNameText.right=new FormAttachment(100, 0);
    fdNameText.top =new FormAttachment(0, 0);
    nameText.setLayoutData(fdNameText);
    nameText.addModifyListener(new ModifyListener() {  public void modifyText(ModifyEvent event) { 
        starDomain.getDomain().setName(new LocalizedString(defaultLocale, nameText.getText()));
        setNameForTab(tabAndPanel.tab, starDomain.getDomain().getName(defaultLocale));
      } });
    Control lastControl = nameText;
    
    // Add a line to edit the name of the logical domain
    //
    Label descriptionLabel = new Label(logicalGroup, SWT.RIGHT);
    props.setLook(descriptionLabel);
    descriptionLabel.setText(BaseMessages.getString(PKG, "StarModelerPerspective.DomainDescription.Label"));
    FormData fdDescriptionLabel = new FormData();
    fdDescriptionLabel.left=new FormAttachment(0, 0);
    fdDescriptionLabel.right=new FormAttachment(middle, 0);
    fdDescriptionLabel.top =new FormAttachment(lastControl, margin);
    descriptionLabel.setLayoutData(fdDescriptionLabel);
    
    final Text descriptionText = new Text(logicalGroup, SWT.BORDER | SWT.SINGLE);
    props.setLook(descriptionText);
    descriptionText.setText(Const.NVL(starDomain.getDomain().getDescription(defaultLocale), ""));
    FormData fdDescriptionText = new FormData();
    fdDescriptionText.left=new FormAttachment(middle, 5);
    fdDescriptionText.right=new FormAttachment(100, 0);
    fdDescriptionText.top =new FormAttachment(lastControl, margin);
    descriptionText.setLayoutData(fdDescriptionText);
    descriptionText.addModifyListener(new ModifyListener() {  public void modifyText(ModifyEvent event) { 
      starDomain.getDomain().setDescription(new LocalizedString(defaultLocale, descriptionText.getText())); } });
    lastControl = descriptionText;
    
    
    // Then we'll add a table view of all the models and their descriptions
    //
    Label modelsLabel = new Label(logicalGroup, SWT.RIGHT);
    props.setLook(modelsLabel);
    modelsLabel.setText(BaseMessages.getString(PKG, "StarModelerPerspective.DomainModels.Label"));
    FormData fdModelsLabel = new FormData();
    fdModelsLabel.left=new FormAttachment(0, 0);
    fdModelsLabel.right=new FormAttachment(middle, 0);
    fdModelsLabel.top =new FormAttachment(lastControl, margin);
    modelsLabel.setLayoutData(fdModelsLabel);

    ColumnInfo[] colinf=new ColumnInfo[] {
      new ColumnInfo(BaseMessages.getString(PKG, "StarModelerPerspective.ModelName.Column"), ColumnInfo.COLUMN_TYPE_TEXT, false, true),
      new ColumnInfo(BaseMessages.getString(PKG, "StarModelerPerspective.ModelDescription.Column"), ColumnInfo.COLUMN_TYPE_TEXT, false, true),
    };
    
    final TableView modelsList=new TableView(new Variables(), logicalGroup, SWT.BORDER, colinf, 1, null, props);
    modelsList.setReadonly(true);
    modelsList.table.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        if (modelsList.getSelectionIndex()<0) return;
        TableItem item = modelsList.table.getSelection()[0];
        String name = item.getText(1);
        if (editModel(logicalGroup.getShell(), starDomain, defaultLocale, name)) {
          refreshModelsList(starDomain, modelsList);
        }
      }
    });
    
    FormData fdModelsList = new FormData();
    fdModelsList.top = new FormAttachment(lastControl, margin);
    fdModelsList.bottom = new FormAttachment(lastControl, 350);
    fdModelsList.left = new FormAttachment(middle, margin);
    fdModelsList.right = new FormAttachment(100, 0);
    modelsList.setLayoutData(fdModelsList);
    lastControl = modelsList;
    
    
    
    // A few buttons to edit the list
    // 
    Button newModelButton = new Button(logicalGroup, SWT.PUSH);
    newModelButton.setText(BaseMessages.getString(PKG, "StarModelerPerspective.Button.NewModel"));
    FormData fdNewModelButton = new FormData();
    fdNewModelButton.top = new FormAttachment(lastControl, margin);
    fdNewModelButton.left = new FormAttachment(middle, margin);
    newModelButton.setLayoutData(fdNewModelButton);
    newModelButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) { 
        if (newModel(logicalGroup.getShell(), starDomain)) {
          refreshModelsList(starDomain, modelsList);
        }
      }
    });
    
    Button editModelButton = new Button(logicalGroup, SWT.PUSH);
    editModelButton.setText(BaseMessages.getString(PKG, "StarModelerPerspective.Button.EditModel"));
    FormData fdEditModelButton = new FormData();
    fdEditModelButton.top = new FormAttachment(lastControl, margin);
    fdEditModelButton.left = new FormAttachment(newModelButton, margin);
    editModelButton.setLayoutData(fdEditModelButton);
    editModelButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) { 
        if (modelsList.getSelectionIndex()<0) return;
        TableItem item = modelsList.table.getSelection()[0];
        String name = item.getText(1);
        if (editModel(logicalGroup.getShell(), starDomain, defaultLocale, name)) {
          refreshModelsList(starDomain, modelsList);
        }
      }
    });

    Button delModelButton = new Button(logicalGroup, SWT.PUSH);
    delModelButton.setText(BaseMessages.getString(PKG, "StarModelerPerspective.Button.DeleteModel"));
    FormData fdDelModelButton = new FormData();
    fdDelModelButton.top = new FormAttachment(lastControl, margin);
    fdDelModelButton.left = new FormAttachment(editModelButton, margin);
    delModelButton.setLayoutData(fdDelModelButton);
    delModelButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        if (modelsList.getSelectionIndex()<0) return;
        TableItem item = modelsList.table.getSelection()[0];
        String name = item.getText(1);
        if (deleteModel(logicalGroup.getShell(), starDomain, defaultLocale, name)) {
          refreshModelsList(starDomain, modelsList);
        }
      }
    });
    
    FormData fdLogicalGroup = new FormData();
    fdLogicalGroup.top = new FormAttachment(0, 0);
    fdLogicalGroup.left = new FormAttachment(0, 0);
    fdLogicalGroup.right = new FormAttachment(100, 0);
    logicalGroup.setLayoutData(fdLogicalGroup);
    lastControl = logicalGroup;
    
    // And now for the physical hints
    //
    final Group physicalGroup = new Group(parentComposite, SWT.SHADOW_NONE);
    props.setLook(physicalGroup);
    physicalGroup.setText(BaseMessages.getString(PKG, "StarModelerPerspective.PhysicalGroup.Label"));
    FormLayout phGroupLayout = new FormLayout();
    phGroupLayout.marginLeft=10;
    phGroupLayout.marginRight=10;
    phGroupLayout.marginTop=10;
    phGroupLayout.marginBottom=10;
    phGroupLayout.spacing=margin;
    physicalGroup.setLayout(phGroupLayout);
    
    // The target database (optional)
    //
    final List<DatabaseMeta> sharedDatabases= SharedDatabaseUtil.loadSharedDatabases();
    String[] databaseNames = SharedDatabaseUtil.getSortedDatabaseNames(sharedDatabases);
    
    Label targetDatabaseLabel = new Label(physicalGroup, SWT.RIGHT);
    props.setLook(targetDatabaseLabel);
    targetDatabaseLabel.setText(BaseMessages.getString(PKG, "StarModelerPerspective.TargetDatabase.Label"));
    FormData fdTargetDatabaseLabel = new FormData();
    fdTargetDatabaseLabel.left=new FormAttachment(0, 0);
    fdTargetDatabaseLabel.right=new FormAttachment(middle, 0);
    fdTargetDatabaseLabel.top =new FormAttachment(0, 0);
    targetDatabaseLabel.setLayoutData(fdTargetDatabaseLabel);
    
    final Button newDatabaseButton = new Button(physicalGroup, SWT.PUSH);
    newDatabaseButton.setText(BaseMessages.getString("System.Button.New"));
    FormData fdNewDatabaseButton = new FormData();
    fdNewDatabaseButton.right=new FormAttachment(100, 0);
    fdNewDatabaseButton.top =new FormAttachment(0, 0);
    newDatabaseButton.setLayoutData(fdNewDatabaseButton);

    final CCombo targetDatabase = new CCombo(physicalGroup, SWT.BORDER | SWT.SINGLE);
    targetDatabase.setItems(databaseNames);
    props.setLook(targetDatabase);
    String targetDb = ConceptUtil.getString(starDomain.getDomain(), DefaultIDs.DOMAIN_TARGET_DATABASE);
    targetDatabase.setText(Const.NVL(targetDb, ""));
    FormData fdTargetDatabase = new FormData();
    fdTargetDatabase.left=new FormAttachment(middle, 5);
    fdTargetDatabase.right=new FormAttachment(newDatabaseButton, -margin);
    fdTargetDatabase.top =new FormAttachment(lastControl, margin);
    targetDatabase.setLayoutData(fdTargetDatabase);
    targetDatabase.addModifyListener(new ModifyListener() {  public void modifyText(ModifyEvent event) { 
      starDomain.getDomain().setProperty(DefaultIDs.DOMAIN_TARGET_DATABASE, targetDatabase.getText()); } });
    lastControl = targetDatabase;
    
    newDatabaseButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        createSharedDatabase(targetDatabase);
      }
    });

    
    // put some utility buttons at the bottom too...
    //
    Button sqlJobButton = new Button(physicalGroup, SWT.PUSH);
    sqlJobButton.setText(BaseMessages.getString(PKG, "StarModelerPerspective.Button.GenerateSQLJob"));
    sqlJobButton.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { generateSqlJobButton(starDomain); } });
    Button domainJobButton = new Button(physicalGroup, SWT.PUSH);
    domainJobButton.setText(BaseMessages.getString(PKG, "StarModelerPerspective.Button.GenerateDomainJob"));
    domainJobButton.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { generateDomainJobButton(starDomain); } });
    Button physicalModelButton = new Button(physicalGroup, SWT.PUSH);
    physicalModelButton.setText(BaseMessages.getString(PKG, "StarModelerPerspective.Button.GeneratePhysicalModel"));
    physicalModelButton.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { generatePhysicalModelButton(starDomain); } });
    Button mondrianSchemaButton = new Button(physicalGroup, SWT.PUSH);
    mondrianSchemaButton.setText(BaseMessages.getString(PKG, "StarModelerPerspective.Button.GenerateMondrianSchema"));
    mondrianSchemaButton.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { generateMondrialSchemaButton(starDomain); } });
    BaseStepDialog.positionBottomButtons(physicalGroup, 
        new Button[] { sqlJobButton, domainJobButton, physicalModelButton, mondrianSchemaButton, }, margin, lastControl);
    
    FormData fdPhysicalGroup = new FormData();
    fdPhysicalGroup.top = new FormAttachment(logicalGroup, 2*margin);
    fdPhysicalGroup.left = new FormAttachment(0, 0);
    fdPhysicalGroup.right = new FormAttachment(100, 0);
    physicalGroup.setLayoutData(fdPhysicalGroup);
    lastControl = physicalGroup;
    
    parentComposite.layout(true);
    parentComposite.pack();
    
    // What's the size: 
    Rectangle bounds = parentComposite.getBounds();
    
    scrolledComposite.setContent(parentComposite);
    scrolledComposite.setExpandHorizontal(true);
    scrolledComposite.setExpandVertical(true);
    scrolledComposite.setMinWidth(bounds.width);
    scrolledComposite.setMinHeight(bounds.height);

    
    refreshModelsList(starDomain, modelsList);
    models.add(starDomain);
    
    setNameForTab(tabAndPanel.tab, starDomain.getDomain().getName(defaultLocale));
    setMetaForTab(tabAndPanel.tab, starDomain);
    setSelectedMeta(starDomain);
    setActive(true);

    comp.layout();
    
    Spoon.getInstance().enableMenus();
  }
  
  protected void createSharedDatabase(CCombo targetDatabase) {
    Shell shell = Spoon.getInstance().getShell();
    boolean retry=true;
    while (retry) {
      try {
        DatabaseMeta dbMeta = new DatabaseMeta();
        DatabaseDialog databaseDialog = new DatabaseDialog(shell, dbMeta);
        if (databaseDialog.open()!=null) {
          // Add dbMeta to the list of shared databases...
          //
          SharedDatabaseUtil.addSharedDatabase(dbMeta);
          
          final List<DatabaseMeta> sharedDatabases= SharedDatabaseUtil.loadSharedDatabases();
          String[] databaseNames = SharedDatabaseUtil.getSortedDatabaseNames(sharedDatabases);
          
          targetDatabase.setItems(databaseNames);
        }
        retry = false;
      } catch(KettleObjectExistsException e) {
        new ErrorDialog(shell, 
            BaseMessages.getString(PKG, "StarModelerPerspective.Exception.UnableToCreateSharedDB.Title"), 
            BaseMessages.getString(PKG, "StarModelerPerspective.Exception.UnableToCreateSharedDB.Message"), e);
      } catch (KettleException e) {
        new ErrorDialog(shell, 
            BaseMessages.getString(PKG, "StarModelerPerspective.Exception.UnableToCreateSharedDB.Title"), 
            BaseMessages.getString(PKG, "StarModelerPerspective.Exception.UnableToCreateSharedDB.Message"), e);
        retry=false;
      }
    }
  }

  protected void generateSqlJobButton(StarDomain starDomain) {
    final Spoon spoon = Spoon.getInstance();
    
    List<DatabaseMeta> sharedDatabases = SharedDatabaseUtil.loadSharedDatabases(); 
    
    // TODO: validate presence of repository, repository directory
    //
    JobGenerator jobGenerator = new JobGenerator(starDomain, spoon.rep, new RepositoryDirectory(), sharedDatabases, defaultLocale);
    try {
      JobMeta jobMeta = jobGenerator.generateSqlJob();
      spoon.addJobGraph(jobMeta);
      SpoonPerspectiveManager.getInstance().activatePerspective(MainSpoonPerspective.class);
    } catch(Exception e) {
      new ErrorDialog(spoon.getShell(),
          BaseMessages.getString(PKG, "StarModelerPerspective.ErrorGeneratingSqlJob.Title"),
          BaseMessages.getString(PKG, "StarModelerPerspective.ErrorGeneratingSqlJob.Message"), e);
      
    }
      
    
  }

  protected void generateDomainJobButton(StarDomain starDomain) {
    final Spoon spoon = Spoon.getInstance();
    
    List<DatabaseMeta> sharedDatabases = SharedDatabaseUtil.loadSharedDatabases(); 
    
    JobGenerator jobGenerator = new JobGenerator(starDomain, spoon.rep, new RepositoryDirectory(), sharedDatabases, defaultLocale);
    try {
      List<TransMeta> transMetas = jobGenerator.generateDimensionTransformations();
      for (TransMeta transMeta : transMetas) {
        spoon.addTransGraph(transMeta);
      }
      SpoonPerspectiveManager.getInstance().activatePerspective(MainSpoonPerspective.class);
    } catch(Exception e) {
      new ErrorDialog(spoon.getShell(),
          BaseMessages.getString(PKG, "StarModelerPerspective.ErrorGeneratingSqlJob.Title"),
          BaseMessages.getString(PKG, "StarModelerPerspective.ErrorGeneratingSqlJob.Message"), e);
      
    }
    
  }

  protected void generatePhysicalModelButton(StarDomain starDomain) {
    
    try {
      List<DatabaseMeta> sharedDatabases = SharedDatabaseUtil.loadSharedDatabases(); 
      MetadataGenerator generator = new MetadataGenerator(starDomain.getDomain(), sharedDatabases);
      Domain physicalMetadataModel = generator.generatePhysicalMetadataModel();
      System.out.println("Generated physical model: "+physicalMetadataModel.getName(defaultLocale));
    } catch(Exception e) {
      new ErrorDialog(Spoon.getInstance().getShell(), 
          BaseMessages.getString(PKG, "StarModelerPerspective.ErrorGeneratingPhysicalModel.Title"),
          BaseMessages.getString(PKG, "StarModelerPerspective.ErrorGeneratingPhysicalModel.Message"),
          e);
    }   
    
  }

  protected void generateMondrialSchemaButton(StarDomain starDomain) {
  }



  protected void refreshModelsList(StarDomain starDomain, TableView modelsList) {
    modelsList.clearAll();
    for (LogicalModel model : starDomain.getDomain().getLogicalModels()) {
      TableItem item = new TableItem(modelsList.table, SWT.NONE);
      item.setText(1, Const.NVL(model.getName(defaultLocale), ""));
      item.setText(2, Const.NVL(model.getDescription(defaultLocale), ""));
    }
    modelsList.removeEmptyRows();
    modelsList.setRowNums();
    modelsList.optWidth(true);
    Spoon.getInstance().enableMenus();
  }


  /**
   * Create a new model in the domain
   * 
   * @param domain the domain to create the new model in
   */
  private boolean newModel(Shell shell, StarDomain starDomain) {
    LogicalModel model = new LogicalModel();

    StarModelDialog dialog = new StarModelDialog(shell, model, defaultLocale);
    if (dialog.open()!=null) {
      starDomain.getDomain().getLogicalModels().add(model);
      starDomain.setChanged(true);
      return true;
    }
    return false;
  }

  /**
   * Edit a model in the domain
   * @param shell 
   * @param LogicstarDomain the domain to edit the model in
   * @param modelName the name of the model to edit
   * @return 
   */
  private boolean editModel(Shell shell, StarDomain starDomain, String locale, String modelName) {
    LogicalModel logicalModel = findLogicalTable(starDomain.getDomain(), locale, modelName);
    if (logicalModel!=null) {
      StarModelDialog dialog = new StarModelDialog(shell, logicalModel, locale);
      if (dialog.open()!=null) {
        starDomain.setChanged(true);
        return true;
      }
    }
    return false;
  }
  
  /**
   * Delete a model in the domain
   * 
   * @param domain the domain to delete the model from
   * @param modelName the name of the model to delete
   */
  private boolean deleteModel(Shell shell, StarDomain starDomain, String locale, String modelName) {
    LogicalModel logicalModel = findLogicalTable(starDomain.getDomain(), locale, modelName);
    if (logicalModel!=null) {
      // TODO: show warning dialog.
      //
      starDomain.getDomain().getLogicalModels().remove(logicalModel);
      starDomain.setChanged(true);

      return true;
    }
    return false;
  }
  
  private LogicalModel findLogicalTable(Domain domain, String locale, String modelName) {
    for (LogicalModel logicalModel : domain.getLogicalModels()) {
      String name = ConceptUtil.getName(logicalModel, locale);
      if (name!=null && name.equalsIgnoreCase(modelName)) return logicalModel;
    }
    return null;
  }
  
 
  public EngineMetaInterface getActiveMeta() {
    int idx = tabbox.getSelectedIndex();
    if( idx == -1 || idx >= tabbox.getTabs().getChildNodes().size()) {
      return null;
    }
    return metas.get(tabbox.getTabs().getChildNodes().get( idx ));
  }
  
  public void setSelectedMeta(EngineMetaInterface meta){
    EngineMetaInterface prevVal = this.selectedMeta;
    this.selectedMeta = meta;
    Spoon.getInstance().enableMenus();
    firePropertyChange("selectedMeta", prevVal, meta);
  }
  
  public EngineMetaInterface getSelectedMeta(){
    return selectedMeta;
  }
  
  public String getDefaultExtension() {
    return defaultExtension;
  }
  
  public void setDefaultExtension(String defaultExtension) {
    this.defaultExtension = defaultExtension;
  }
  
  protected static class CloseConfirmXulDialogCallback implements XulDialogCallback<Object>{
    public boolean closeIt = false;
    public void onClose(XulComponent sender, Status returnCode, Object retVal) {
      if(returnCode == Status.ACCEPT){
        closeIt = true;
      }
    }
    public void onError(XulComponent sender, Throwable t) {}
  }

  public boolean onTabClose(final int pos) throws XulException {
    
    if(models.get(pos).hasChanged()){
      XulConfirmBox confirm = (XulConfirmBox) document.createElement("confirmbox"); //$NON-NLS-1$
      confirm.setTitle(BaseMessages.getString(this.getClass(), "StarModelerPerspective.unsavedChangesTitle")); //$NON-NLS-1$
      confirm.setMessage(BaseMessages.getString(this.getClass(), "StarModelerPerspective.unsavedChangesMessage")); //$NON-NLS-1$
      
      CloseConfirmXulDialogCallback callback = new CloseConfirmXulDialogCallback();
      confirm.addDialogCallback(callback);
      confirm.open();
      if(callback.closeIt){
        models.remove(pos);
        metas.remove(tabbox.getTabs().getChildNodes().get(pos));
        return true;
      } else {
        return false;
      }
      
    } else {
      models.remove(pos);
      metas.remove(pos);
    }
    return true;
  }
}