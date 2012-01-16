/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon.dialog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.market.MarketEntry;
import org.pentaho.di.core.market.Marketplace;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


public class MarketplaceDialog extends Dialog
{
	private static Class<?> PKG = MarketplaceDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlMarketplaces;

	private Button wClose;

	private Shell         shell;
	
	private PropsUI props;
	
	private List<Marketplace> marketplaces;

  private ExpandBar bar;

  private ArrayList<MarketEntry> marketEntries;

  private int margin;

  private int middle;
	
	public MarketplaceDialog(Shell parent)
	{
		super(parent, SWT.NONE);
		
		props = PropsUI.getInstance();
	}

	public void open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
 		shell.setImage(GUIResource.getInstance().getImageSpoon());

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("PDI Marketplace"); //Preview selection screen
		shell.setImage(GUIResource.getInstance().getImageLogoSmall());
		
		margin = Const.MARGIN;
    middle = props.getMiddlePct();
		
		// First read the markets file, set a default when non exists
		//
		if (!readMarketsFile()) return;
		
		if (!readMarketEntries()) return;
		
		wlMarketplaces=new Label(shell, SWT.NONE);
		wlMarketplaces.setText("Detected plugins"); 
 		props.setLook(wlMarketplaces);
		FormData fdlMarketplaces=new FormData();
		fdlMarketplaces.left = new FormAttachment(0, 0);
		fdlMarketplaces.top  = new FormAttachment(0, margin);
		wlMarketplaces.setLayoutData(fdlMarketplaces);
		
		bar = new ExpandBar(shell, SWT.V_SCROLL | SWT.H_SCROLL);
		FormData fdBar=new FormData();
		fdBar.left   = new FormAttachment(0, 0);
		fdBar.top    = new FormAttachment(wlMarketplaces, margin);
		fdBar.right  = new FormAttachment(100, 0);
		fdBar.bottom = new FormAttachment(100, -50);
		bar.setLayoutData(fdBar);
		
		// Add an expand item for each market place...
		//
		for (final MarketEntry marketEntry : marketEntries) {
		  Composite composite = new Composite(bar, SWT.NONE);
		  FormLayout layout = new FormLayout();
		  layout.marginHeight = margin;
		  layout.marginWidth  = margin;
		  composite.setLayout(layout);
		  props.setLook(composite);
		  
		  // Add a series of details in the expand-bar item as well as an install button...
		  //
		  
		  Control lastControl = null;

		  // The ID
		  addLeftLabel(composite, "ID:", lastControl);
		  lastControl = addRightLabel(composite, Const.NVL(marketEntry.getId(), ""), lastControl);
		  
		  // The name
		  addLeftLabel(composite, "Name:", lastControl);
		  lastControl = addRightLabel(composite, Const.NVL(marketEntry.getName(), ""), lastControl);

      // The description
		  if (!Const.isEmpty(marketEntry.getDescription())) {
        addLeftLabel(composite, "Description:", lastControl);
        lastControl = addRightLabel(composite, marketEntry.getDescription(), lastControl);
		  }

      // The package URL
      addLeftLabel(composite, "Package URL:", lastControl);
      lastControl = addRightURL(composite, marketEntry.getPackageUrl(), lastControl);


      // The documentation URL
      if (!Const.isEmpty(marketEntry.getDocumentationUrl())) {
        addLeftLabel(composite, "Documentation:", lastControl);
        lastControl = addRightURL(composite, marketEntry.getDocumentationUrl(), lastControl);
      }

      // The case tracking URL
      if (!Const.isEmpty(marketEntry.getCasesUrl())) {
        addLeftLabel(composite, "Case tracking:", lastControl);
        lastControl = addRightURL(composite, marketEntry.getCasesUrl(), lastControl);
      }

      // The source code URL
      if (!Const.isEmpty(marketEntry.getSourceUrl())) {
        addLeftLabel(composite, "Source code:", lastControl);
        lastControl = addRightURL(composite, marketEntry.getSourceUrl(), lastControl);
      }
      
      // The license name
      if (!Const.isEmpty(marketEntry.getLicenseName())) {
        addLeftLabel(composite, "License:", lastControl);
        lastControl = addRightLabel(composite, Const.NVL(marketEntry.getLicenseName(), ""), lastControl);
      }

      // The license text
      if (!Const.isEmpty(marketEntry.getLicenseText())) {
        addLeftLabel(composite, "License details:", lastControl);
        lastControl = addRightLabel(composite, Const.NVL(marketEntry.getLicenseText(), ""), lastControl);
      }

      // The support level
      addLeftLabel(composite, "Support level:", lastControl);
      lastControl = addRightLabel(composite, marketEntry.getSupportLevel().getDescription(), lastControl);

      // The support message
      if (!Const.isEmpty(marketEntry.getSupportMessage())) {
        addLeftLabel(composite, "Support message:", lastControl);
        lastControl = addRightLabel(composite, marketEntry.getSupportMessage(), lastControl);
      }

      // The support URL
      if (!Const.isEmpty(marketEntry.getSupportUrl())) {
        addLeftLabel(composite, "Support URL:", lastControl);
        lastControl = addRightURL(composite, marketEntry.getSupportUrl(), lastControl);
      }

      Button button = new Button(composite, SWT.PUSH);
      button.setText("  Install this plugin  ");
      FormData fdButton = new FormData();
      fdButton.top = new FormAttachment(lastControl, 4*margin);
      fdButton.left = new FormAttachment(middle, margin);
      button.setLayoutData(fdButton);
      button.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { installPlugin(marketEntry); } });
      
      Label wlName = new Label(composite, SWT.LEFT);
      props.setLook(wlName);
      wlName.setText("Name:");
      FormData fdlName = new FormData();
      fdlName.top = new FormAttachment(0, 0);
      fdlName.left = new FormAttachment(0, 0);
      fdlName.right = new FormAttachment(middle, 0);
      wlName.setLayoutData(fdlName);
      
		  
		  // add widgets to composite...
		  //
		  ExpandItem expandItem = new ExpandItem (bar, SWT.NONE, 0);
		  expandItem.setText(marketEntry.getName());
		  expandItem.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		  expandItem.setControl(composite);
		  Image image;
		  switch(marketEntry.getType()) {
		  case Step        : image = GUIResource.getInstance().getImageTransGraph(); break;  
      case JobEntry    : image = GUIResource.getInstance().getImageTransGraph(); break;  
      case SpoonPlugin : image = GUIResource.getInstance().getImageTransGraph(); break;  
      case Database    : image = GUIResource.getInstance().getImageConnection(); break;  
      default : image = GUIResource.getInstance().getImageSpoon(); break;  
      }
		  expandItem.setImage(image);
		}

		wClose=new Button(shell, SWT.PUSH);
		wClose.setText(BaseMessages.getString(PKG, "System.Button.Close"));
		wClose.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { dispose(); }});
		
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wClose }, margin, null);
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { dispose(); } } );

		BaseStepDialog.setSize(shell);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
	}

  protected void installPlugin(final MarketEntry marketEntry) {
    
    // First see if we need to put a support disclaimer in front of the user...
    //
    
    
    
    try {
      // Unzip content of zip file in appropriate folder in KETTLE_HOME...
      //
      String subfolder;
      switch(marketEntry.getType()) {
      case Step : subfolder = "steps"; break;
      case JobEntry : subfolder = "jobentries"; break;
      case Partitioner: subfolder = "steps"; break;
      case SpoonPlugin: subfolder = "spoon"; break;
      case Database: subfolder = "databases"; break;
      case Repository: subfolder = "repositories"; break;
      default: subfolder = null;
      }
      
      // ~/.kettle/plugins/steps/
      // ~/.kettle/plugins/
      //
      String folderName = Const.getKettleDirectory() + Const.FILE_SEPARATOR + "plugins" + ( subfolder==null ? "" : Const.FILE_SEPARATOR + subfolder );
      
      File folder = new File(folderName);
      if (folder.exists()) {
        // TODO: ask about overwrite...
        //
      }
      
      // Read the package, extract in folder
      //
      InputStream inputStream = KettleVFS.getInputStream(marketEntry.getPackageUrl());
      ZipInputStream zis = new ZipInputStream(inputStream);
      ZipEntry zipEntry = zis.getNextEntry();
      byte[] buffer = new byte[1024];
      int bytesRead = 0;
      FileOutputStream fos=null;
      
      while (zipEntry!=null) {
        try {
          File file = new File(folderName + File.separator + zipEntry.getName());
          
          if (zipEntry.isDirectory()) {
            file.mkdirs();
          } else {
            file.getParentFile().mkdirs();
            
            fos = new FileOutputStream(file);
            while( (bytesRead=zis.read(buffer))!=-1) {
              fos.write(buffer, 0, bytesRead);
            }
          }
          
          zipEntry = zis.getNextEntry();
        } finally {
          if (fos!=null) {
            try {
              fos.close();
            } catch(IOException e) {
              // Ignore.
            }
          }
        }
      }
      
      // At the end, refresh the plugin registry
      //
      PluginRegistry.init();
      Spoon spoon = Spoon.getInstance(); 
      spoon.refreshCoreObjects();
      spoon.refreshTree();
      spoon.refreshGraph();
      spoon.enableMenus();
      GUIResource.getInstance().reload();
      
      // Refresh active filter too
      spoon.selectionFilter.setText(spoon.selectionFilter.getText());
      
    } catch(Exception e) {
      new ErrorDialog(shell, "Error", "Error installing plugin: ", e);
    }
    
  }

  private void addLeftLabel(Composite composite, String string, Control lastControl) {
    Label label = new Label(composite, SWT.RIGHT);
    props.setLook(label);
    label.setText(string);
    label.setFont(GUIResource.getInstance().getFontBold());
    FormData fdLabel = new FormData();
    if (lastControl!=null) {
      fdLabel.top = new FormAttachment(lastControl, 2*margin);
    } else {
      fdLabel.top = new FormAttachment(0, 0);
    }
    fdLabel.left = new FormAttachment(0, 0);
    fdLabel.right = new FormAttachment(middle/2, 0);
    label.setLayoutData(fdLabel);
  }

  private Control addRightLabel(Composite composite, String string, Control lastControl) {
    Label label = new Label(composite, SWT.LEFT);
    props.setLook(label);
    label.setText(string);
    FormData fdLabel = new FormData();
    if (lastControl!=null) {
      fdLabel.top = new FormAttachment(lastControl, 2*margin);
    } else {
      fdLabel.top = new FormAttachment(0, 0);
    }
    fdLabel.left = new FormAttachment(middle/2, margin);
    fdLabel.right = new FormAttachment(100, 0);
    label.setLayoutData(fdLabel);
    
    return label;
  }
  
  private Control addRightURL(Composite composite, final String string, Control lastControl) {
    Link link = new Link(composite, SWT.LEFT);
    props.setLook(link);
    link.setText("<a>"+string+"</a>");
    FormData fdLabel = new FormData();
    if (lastControl!=null) {
      fdLabel.top = new FormAttachment(lastControl, 2*margin);
    } else {
      fdLabel.top = new FormAttachment(0, 0);
    }
    fdLabel.left = new FormAttachment(middle/2, margin);
    fdLabel.right = new FormAttachment(100, 0);
    link.setLayoutData(fdLabel);
    
    link.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        Spoon.getInstance().addSpoonBrowser(string, string);
      }
    });
    
    return link;
  }
  
  private boolean readMarketsFile() {
    marketplaces = new ArrayList<Marketplace>();
    String marketplacesFile = Const.getMarketplacesFile();
    try {
      File file = new File(marketplacesFile);
      if (file.exists()) {
        Document doc = XMLHandler.loadXMLFile(marketplacesFile);
        Node placesNode = XMLHandler.getSubNode(doc, "marketplaces");
        List<Node> nodes = XMLHandler.getNodes(placesNode, Marketplace.XML_TAG);
        for (Node node : nodes ) {
          marketplaces.add(new Marketplace(node));
        }
      } else {
        marketplaces = Marketplace.getDefaultMarketplaces();
      }
      
      return true;
    } catch(Exception e) {
      new ErrorDialog(shell, "Error", "There was an error reading from marketplaces file: "+marketplacesFile, e);
      return false;
    }
  }
  
  private boolean readMarketEntries() {
    marketEntries = new ArrayList<MarketEntry>();
    for (Marketplace marketplace : marketplaces) {
      try {
        // Read the content from the given URL...
        //
        Document doc = XMLHandler.loadXMLFile( KettleVFS.getInputStream(marketplace.getEntriesUrl()) );
        
        Node marketNode = XMLHandler.getSubNode(doc, "market");
        List<Node> entryNodes = XMLHandler.getNodes(marketNode, MarketEntry.XML_TAG);
        for (Node entryNode : entryNodes ) {
          marketEntries.add(new MarketEntry(entryNode));
        }
      } catch(Exception e) {
        new ErrorDialog(shell, "Error", "There was an error reading from marketplace '"+marketplace.getName()+"' with URL '"+marketplace.getEntriesUrl()+"'", e);
      }
    }
    return !marketEntries.isEmpty();
  }

  public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}		
}
