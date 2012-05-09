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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.market.Market;
import org.pentaho.di.core.market.entry.MarketEntries;
import org.pentaho.di.core.market.entry.MarketEntry;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class MarketplaceDialog extends Dialog
{
	private static Class<?> MARKET_PKG = Market.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlMarketplaces;

	private Button wClose;

	private Shell         shell;
	
	private PropsUI props;
	
    private ExpandBar bar;

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
		MarketEntries marketEntries = new MarketEntries();
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
		  addLeftLabel(composite, BaseMessages.getString(MARKET_PKG, "MarketplacesDialog.ID.label"), lastControl);
		  lastControl = addRightLabel(composite, Const.NVL(marketEntry.getId(), ""), lastControl);
		  
		  // The name
		  addLeftLabel(composite, BaseMessages.getString(MARKET_PKG, "MarketplacesDialog.Name.label"), lastControl);
		  lastControl = addRightLabel(composite, Const.NVL(marketEntry.getName(), ""), lastControl);

      // The description
		  if (!Const.isEmpty(marketEntry.getDescription())) {
        addLeftLabel(composite, BaseMessages.getString(MARKET_PKG, "MarketplacesDialog.Description.label"), lastControl);
        lastControl = addRightLabel(composite, marketEntry.getDescription(), lastControl);
		  }

      // The package URL
      addLeftLabel(composite, BaseMessages.getString(MARKET_PKG, "MarketplacesDialog.PackageURL.label"), lastControl);
      lastControl = addRightURL(composite, marketEntry.getPackageUrl(), lastControl);


      // The documentation URL
      if (!Const.isEmpty(marketEntry.getDocumentationUrl())) {
        addLeftLabel(composite, BaseMessages.getString(MARKET_PKG, "MarketplacesDialog.Documentation.label"), lastControl);
        lastControl = addRightURL(composite, marketEntry.getDocumentationUrl(), lastControl);
      }

      // The case tracking URL
      if (!Const.isEmpty(marketEntry.getCasesUrl())) {
        addLeftLabel(composite, BaseMessages.getString(MARKET_PKG, "MarketplacesDialog.CaseTracking.label"), lastControl);
        lastControl = addRightURL(composite, marketEntry.getCasesUrl(), lastControl);
      }

      // The source code URL
      if (!Const.isEmpty(marketEntry.getSourceUrl())) {
        addLeftLabel(composite, BaseMessages.getString(MARKET_PKG, "MarketplacesDialog.SourceCode.label"), lastControl);
        lastControl = addRightURL(composite, marketEntry.getSourceUrl(), lastControl);
      }
      
      // The license name
      if (!Const.isEmpty(marketEntry.getLicenseName())) {
        addLeftLabel(composite, BaseMessages.getString(MARKET_PKG, "MarketplacesDialog.License.label"), lastControl);
        lastControl = addRightLabel(composite, Const.NVL(marketEntry.getLicenseName(), ""), lastControl);
      }

      // The license text
      if (!Const.isEmpty(marketEntry.getLicenseText())) {
        addLeftLabel(composite, BaseMessages.getString(MARKET_PKG, "MarketplacesDialog.LicenseDetails.label"), lastControl);
        lastControl = addRightLabel(composite, Const.NVL(marketEntry.getLicenseText(), ""), lastControl);
      }

      // The support level
      addLeftLabel(composite, BaseMessages.getString(MARKET_PKG, "MarketplacesDialog.SupportLevel.label"), lastControl);
      lastControl = addRightLabel(composite, marketEntry.getSupportLevel().getDescription(), lastControl);

      // The support message
      if (!Const.isEmpty(marketEntry.getSupportMessage())) {
        addLeftLabel(composite, BaseMessages.getString(MARKET_PKG, "MarketplacesDialog.SupportMessage.label"), lastControl);
        lastControl = addRightLabel(composite, marketEntry.getSupportMessage(), lastControl);
      }

      // The support URL
      if (!Const.isEmpty(marketEntry.getSupportUrl())) {
        addLeftLabel(composite, BaseMessages.getString(MARKET_PKG, "MarketplacesDialog.SupportURL.label"), lastControl);
        lastControl = addRightURL(composite, marketEntry.getSupportUrl(), lastControl);
      }

      final Button button = new Button(composite, SWT.PUSH);
      final ExpandItem expandItem = new ExpandItem (bar, SWT.NONE, 0);
      setButtonLabel(button, Market.isInstalled(marketEntry.getName()));
	  button.addSelectionListener(new SelectionAdapter() { 
		  public void widgetSelected(SelectionEvent e) { 
			  try {
				  Market.installUninstall(marketEntry, Market.isInstalled(marketEntry.getName()));
				  setButtonLabel(button, Market.isInstalled(marketEntry.getName()));
				  setPluginName(expandItem, marketEntry.getName(), Market.isInstalled(marketEntry.getName()));
			  }
			  catch(KettleException ke){
			     new ErrorDialog(Spoon.getInstance().getShell(), BaseMessages.getString(MARKET_PKG, "Market.error"), BaseMessages.getString(MARKET_PKG, "Market.installUninstall.error"), ke);
			  } 
		  } 
      });
      
      FormData fdButton = new FormData();
      fdButton.top = new FormAttachment(lastControl, 4*margin);
      fdButton.left = new FormAttachment(middle, margin);
      button.setLayoutData(fdButton);
      
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
		  
		  setPluginName(expandItem, marketEntry.getName(), Market.isInstalled(marketEntry.getName()));
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
		wClose.setText(BaseMessages.getString(MARKET_PKG, "System.Button.Close"));
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

  private void setButtonLabel(Button button, boolean isInstalled) {
      if (isInstalled) {
    	  button.setText("  Uninstall this plugin  ");
   	  }
      else {
    	  button.setText("  Install this plugin  ");  
      }
  }
  
  private void setPluginName(ExpandItem expandItem, String name, boolean isInstalled) {
	  if (isInstalled) {
		  expandItem.setText(BaseMessages.getString(MARKET_PKG, "Marketplaces.Dialog.PluginInstalled.message", name));
	  }
	  else {
		  expandItem.setText(BaseMessages.getString(MARKET_PKG, "Marketplaces.Dialog.PluginNotInstalled.message", name));
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
  
  public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}		
}
