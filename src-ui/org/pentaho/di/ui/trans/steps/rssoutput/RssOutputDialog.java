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

package org.pentaho.di.ui.trans.steps.rssoutput;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.rssoutput.RssOutputMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;


public class RssOutputDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = RssOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlRemarqChannel;
	private FormData     fdlRemarqChannel;
	private Label        wlRemarqMandatory;
	private FormData     fdlRemarqMandatory;
	
	private Label        wlRemarqItem;
	private FormData     fdlRemarqItem;

	
	private Label        wlAddToResult;
	private Button       wAddToResult;
	private FormData     fdlAddToResult, fdAddToResult;
	
	private Label        wlCustomRss;
	private Button       wCustomRss;
	private FormData     fdlCustomRss, fdCustomRss;
	
	private Label        wlDisplayItem;
	private Button       wDisplayItem;
	private FormData     fdlDisplayItem, fdDisplayItem;
	
	private Group wFileName;
	private FormData  fdFileName;
	
	private Group wChannelGroup;
	private FormData  fdChannelGroup;
	
	private Group wResultFile;
	private FormData  fdResultFile;
	
	private Group wFields;
	private FormData  fdFields;
	
	
	private Label        wlFilename;
	private Button       wbFilename;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdFilename;
	
	private Label        wlExtension;
	private TextVar      wExtension;
	private FormData     fdlExtension, fdExtension;
	
	private Label        wlChannelTitle;
	private CCombo       wChannelTitle;
	private FormData     fdlChannelTitle, fdChannelTitle;
	
	private Label        wlChannelLink;
	private CCombo      wChannelLink;
	private FormData     fdlChannelLink, fdChannelLink;
	
	private Label        wlChannelDescription;
	private CCombo      wChannelDescription;
	private FormData     fdlChannelDescription, fdChannelDescription;
	
	private Label        wlChannelPubDate;
	private CCombo      wChannelPubDate;
	private FormData     fdlChannelPubDate, fdChannelPubDate;
	
	private Label        wlChannelImageTitle;
	private CCombo       wChannelImageTitle;
	private FormData     fdlChannelImageTitle, fdChannelImageTitle;
	
	private Label        wlChannelImageLink;
	private CCombo       wChannelImageLink;
	private FormData     fdlChannelImageLink, fdChannelImageLink;
	
	private Label        wlChannelImageUrl;
	private CCombo       wChannelImageUrl;
	private FormData     fdlChannelImageUrl, fdChannelImageUrl;
	
	private Label        wlChannelImageDescription;
	private CCombo       wChannelImageDescription;
	private FormData     fdlChannelImageDescription, fdChannelImageDescription;
	
	private Label        wlChannelLanguage;
	private CCombo       wChannelLanguage;
	private FormData     fdlChannelLanguage, fdChannelLanguage;
	
	private Label        wlChannelCopyright;
	private CCombo       wChannelCopyright;
	private FormData     fdlChannelCopyright, fdChannelCopyright;
	
    private Label        wlChannelAuthor;
    private CCombo       wChannelAuthor;
    private FormData     fdlChannelAuthor, fdChannelAuthor;

	private Label        wlAddStepnr;
	private Button       wAddStepnr;
	private FormData     fdlAddStepnr, fdAddStepnr;


	private Label        wlAddDate;
	private Button       wAddDate;
	private FormData     fdlAddDate, fdAddDate;

	private Label        wlAddTime;
	private Button       wAddTime;
	private FormData     fdlAddTime, fdAddTime;
	
	private Button       wbShowFiles;
	private FormData     fdbShowFiles;
    
    private Label        wlVersion;
    private CCombo       wVersion;
    private FormData     fdlVersion, fdVersion; 
    
    private Label        wlItemTitle;
    private CCombo       wItemTitle;
    private FormData     fdlItemTitle, fdItemTitle;
    
    private Label        wlItemLink;
    private CCombo       wItemLink;
    private FormData     fdlItemLink, fdItemLink;
    
    private Label        wlItemDescription;
    private CCombo       wItemDescription;
    private FormData     fdlItemDescription, fdItemDescription;
    
    private Label        wlItemPubDate;
    private CCombo       wItemPubDate;
    private FormData     fdlItemPubDate, fdItemPubDate;
    
    private Label        wlItemAuthor;
    private CCombo       wItemAuthor;
    private FormData     fdlItemAuthor, fdItemAuthor;
    
    private Label        wlGeoPointLat;
    private CCombo       wGeoPointLat;
    private FormData     fdlGeoPointLat, fdGeoPointLat;
    
    private Label        wlGeoPointLong;
    private CCombo       wGeoPointLong;
    private FormData     fdlGeoPointLong, fdGeoPointLong;
        
	private Label				wlChannelCustom;
	private TableView			wChannelCustom;
	private FormData			fdlChannelCustom, fdChannelCustom;
	

	private TableView			wNameSpaceCustom;
	private FormData			fdNameSpaceCustom;
    
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wChannelTab;
	private Composite    wChannelComp;
	private FormData     fdChannelComp;
	
	private CTabItem     wGeneralTab,wContentTab,wCustomTab,wCustomNameSpace ;
	private Composite    wGeneralComp,wContentComp,wCustomComp,wCustomNameSpaceComp ;
	private FormData     fdGeneralComp,fdContentComp,fdCustomComp,fdCustomNameSpace;
	
	
	private Label        wlCreateParentFolder;
	private Button       wCreateParentFolder;
	private FormData     fdlCreateParentFolder, fdCreateParentFolder;
	
	private Label        wlAddImage;
	private Button       wAddImage;
	private FormData     fdlAddImage, fdAddImage;
	
	private Label        wlAddGeoRSS;
	private Button       wAddGeoRSS;
	private FormData     fdlAddGeoRSS, fdAddGeoRSS;
	
	private Label        wluseGeoRSSGML;
	private Button       wuseGeoRSSGML;
	private FormData     fdluseGeoRSSGML, fduseGeoRSSGML;
	
	private Label        wlFileNameInField;
	private Button       wFileNameInField;
	private FormData     fdlFileNameInField, fdFileNameInField;
	
	private Label        wlFieldFilename;
	private CCombo       wFieldFilename;
	private FormData     fdlFieldFilename, fdFieldFilename;
	
	private Label        wlEncoding;
	private CCombo       wEncoding;
	private FormData     fdlEncoding, fdEncoding;
	
	private Label				wlItemCustom,wlNameSpace;
	private TableView			wItemCustom;
	private FormData			fdlItemCustom, fdItemCustom,fdlNameSpace;
	
	private Button				wGetCustomItem;
	private FormData			fdGetCustomItem;
	private Listener			lsGetCustomItem;
	
	private boolean      gotEncodings = false; 
	private boolean		gotPreviousFields=false;
	private String[] fieldNames;
	private String[] rss_versions={"rss_2.0","rss_1.0","rss_0.94","rss_0.93","rss_0.92","atom_1.0","atom_0.3"};
	
	
	
	
    private RssOutputMeta input;
	
	public RssOutputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(RssOutputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

        
		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		backupChanged = input.hasChanged();
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "RssOutputDialog.DialogTitle"));
		
        // get previous fields name
		getFields();
		
		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 		
		//////////////////////////
		// START OF CHANNEL TAB   ///
		//////////////////////////
		
		
		
		wChannelTab=new CTabItem(wTabFolder, SWT.NONE);
		wChannelTab.setText(BaseMessages.getString(PKG, "RssOutputDialog.ChannelTab.TabTitle"));
		
		wChannelComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wChannelComp);

		FormLayout channelLayout = new FormLayout();
		channelLayout.marginWidth  = 3;
		channelLayout.marginHeight = 3;
		wChannelComp.setLayout(channelLayout);
			
		// Create Custom RSS?
		wlCustomRss=new Label(wChannelComp, SWT.RIGHT);
		wlCustomRss.setText(BaseMessages.getString(PKG, "RssOutputDialog.CustomRss.Label"));
		props.setLook(wlCustomRss);
		fdlCustomRss=new FormData();
		fdlCustomRss.left  = new FormAttachment(0, 0);
		fdlCustomRss.top   = new FormAttachment(0, margin);
		fdlCustomRss.right = new FormAttachment(middle, -margin);
		wlCustomRss.setLayoutData(fdlCustomRss);
		wCustomRss=new Button(wChannelComp, SWT.CHECK);
		wCustomRss.setToolTipText(BaseMessages.getString(PKG, "RssOutputDialog.CustomRss.Tooltip"));
 		props.setLook(wCustomRss);
		fdCustomRss=new FormData();
		fdCustomRss.left  = new FormAttachment(middle, 0);
		fdCustomRss.top   = new FormAttachment(0, margin);
		fdCustomRss.right = new FormAttachment(100, 0);
		wCustomRss.setLayoutData(fdCustomRss);
		
		wCustomRss.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				setCustomRSS();
			}
		}
	);
		
		// Create Custom RSS?
		wlDisplayItem=new Label(wChannelComp, SWT.RIGHT);
		wlDisplayItem.setText(BaseMessages.getString(PKG, "RssOutputDialog.DisplayItem.Label"));
		props.setLook(wlDisplayItem);
		fdlDisplayItem=new FormData();
		fdlDisplayItem.left  = new FormAttachment(0, 0);
		fdlDisplayItem.top   = new FormAttachment(wCustomRss, margin);
		fdlDisplayItem.right = new FormAttachment(middle, -margin);
		wlDisplayItem.setLayoutData(fdlDisplayItem);
		wDisplayItem=new Button(wChannelComp, SWT.CHECK);
		wDisplayItem.setToolTipText(BaseMessages.getString(PKG, "RssOutputDialog.DisplayItem.Tooltip"));
 		props.setLook(wDisplayItem);
		fdDisplayItem=new FormData();
		fdDisplayItem.left  = new FormAttachment(middle, 0);
		fdDisplayItem.top   = new FormAttachment(wCustomRss, margin);
		fdDisplayItem.right = new FormAttachment(100, 0);
		wDisplayItem.setLayoutData(fdDisplayItem);
		
		wDisplayItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{}
		}
	);
		
		
		// ChannelGroup grouping?
		// ////////////////////////
		// START OF ChannelGroup GROUP
		// 

		wChannelGroup = new Group(wChannelComp, SWT.SHADOW_NONE);
		props.setLook(wChannelGroup);
		wChannelGroup.setText(BaseMessages.getString(PKG, "RssOutputDialog.Group.ChannelGroup.Label"));
		
		FormLayout groupChannelGroupLayout = new FormLayout();
		groupChannelGroupLayout.marginWidth = 10;
		groupChannelGroupLayout.marginHeight = 10;
		wChannelGroup.setLayout(groupChannelGroupLayout);
		
		// RemarqChannel
		wlRemarqChannel=new Label(wChannelGroup, SWT.RIGHT);
		wlRemarqChannel.setText(BaseMessages.getString(PKG, "RssOutputDialog.RemarqChannel.Label"));
		props.setLook(wlRemarqChannel);
		fdlRemarqChannel=new FormData();
		fdlRemarqChannel.left  = new FormAttachment(0, 0);
		fdlRemarqChannel.top   = new FormAttachment(wDisplayItem, margin);
		//fdlRemarq.right = new FormAttachment(middle, -margin);
		wlRemarqChannel.setLayoutData(fdlRemarqChannel);
		
		// RemarqMandatory
		wlRemarqMandatory=new Label(wChannelGroup, SWT.RIGHT);
		wlRemarqMandatory.setText(BaseMessages.getString(PKG, "RssOutputDialog.RemarqMandatory.Label"));
		props.setLook(wlRemarqMandatory);
		fdlRemarqMandatory=new FormData();
		fdlRemarqMandatory.left  = new FormAttachment(0, 0);
		fdlRemarqMandatory.top   = new FormAttachment(wlRemarqChannel, margin);
		//fdlRemarq.right = new FormAttachment(middle, -margin);
		wlRemarqMandatory.setLayoutData(fdlRemarqMandatory);
		
		
		// ChannelTitle line
		
		wlChannelTitle=new Label(wChannelGroup, SWT.RIGHT);
        wlChannelTitle.setText(BaseMessages.getString(PKG, "RssOutputDialog.ChannelTitle.Label"));
        props.setLook(wlChannelTitle);
        fdlChannelTitle=new FormData();
        fdlChannelTitle.left = new FormAttachment(0, 0);
        fdlChannelTitle.top  = new FormAttachment(wlRemarqMandatory, 2*margin);
        fdlChannelTitle.right= new FormAttachment(middle, -margin);
        wlChannelTitle.setLayoutData(fdlChannelTitle);
        wChannelTitle=new CCombo(wChannelGroup, SWT.BORDER | SWT.READ_ONLY);
        wChannelTitle.setEditable(true);
        wChannelTitle.setItems(fieldNames);
        props.setLook(wChannelTitle);
        wChannelTitle.addModifyListener(lsMod);
        fdChannelTitle=new FormData();
        fdChannelTitle.left = new FormAttachment(middle, 0);
        fdChannelTitle.top  = new FormAttachment(wlRemarqMandatory, 2*margin);
        fdChannelTitle.right= new FormAttachment(100, 0);
        wChannelTitle.setLayoutData(fdChannelTitle);

		
        // Channel Description
		wlChannelDescription=new Label(wChannelGroup, SWT.RIGHT);
        wlChannelDescription.setText(BaseMessages.getString(PKG, "RssOutputDialog.ChannelDescription.Label"));
        props.setLook(wlChannelDescription);
        fdlChannelDescription=new FormData();
        fdlChannelDescription.left = new FormAttachment(0, 0);
        fdlChannelDescription.top  = new FormAttachment(wChannelTitle, margin);
        fdlChannelDescription.right= new FormAttachment(middle, -margin);
        wlChannelDescription.setLayoutData(fdlChannelDescription);
        wChannelDescription=new CCombo(wChannelGroup, SWT.BORDER | SWT.READ_ONLY);
        wChannelDescription.setEditable(true);
        wChannelDescription.setItems(fieldNames);
        props.setLook(wChannelDescription);
        wChannelDescription.addModifyListener(lsMod);
        fdChannelDescription=new FormData();
        fdChannelDescription.left = new FormAttachment(middle, 0);
        fdChannelDescription.top  = new FormAttachment(wChannelTitle, margin);
        fdChannelDescription.right= new FormAttachment(100, 0);
        wChannelDescription.setLayoutData(fdChannelDescription);
        
		// ChannelLink line
    	wlChannelLink=new Label(wChannelGroup, SWT.RIGHT);
        wlChannelLink.setText(BaseMessages.getString(PKG, "RssOutputDialog.ChannelLink.Label"));
        props.setLook(wlChannelLink);
        fdlChannelLink=new FormData();
        fdlChannelLink.left = new FormAttachment(0, 0);
        fdlChannelLink.top  = new FormAttachment(wChannelDescription, margin);
        fdlChannelLink.right= new FormAttachment(middle, -margin);
        wlChannelLink.setLayoutData(fdlChannelLink);
        wChannelLink=new CCombo(wChannelGroup, SWT.BORDER | SWT.READ_ONLY);
        wChannelLink.setEditable(true);
        wChannelLink.setItems(fieldNames);
        props.setLook(wChannelLink);
        wChannelLink.addModifyListener(lsMod);
        fdChannelLink=new FormData();
        fdChannelLink.left = new FormAttachment(middle, 0);
        fdChannelLink.top  = new FormAttachment(wChannelDescription, margin);
        fdChannelLink.right= new FormAttachment(100, 0);
        wChannelLink.setLayoutData(fdChannelLink);

		// ChannelPubDate line
    	wlChannelPubDate=new Label(wChannelGroup, SWT.RIGHT);
        wlChannelPubDate.setText(BaseMessages.getString(PKG, "RssOutputDialog.ChannelPubDate.Label"));
        props.setLook(wlChannelPubDate);
        fdlChannelPubDate=new FormData();
        fdlChannelPubDate.left = new FormAttachment(0, 0);
        fdlChannelPubDate.top  = new FormAttachment(wChannelLink, margin);
        fdlChannelPubDate.right= new FormAttachment(middle, -margin);
        wlChannelPubDate.setLayoutData(fdlChannelPubDate);
        wChannelPubDate=new CCombo(wChannelGroup, SWT.BORDER | SWT.READ_ONLY);
        wChannelPubDate.setEditable(true);
        wChannelPubDate.setItems(fieldNames);
        props.setLook(wChannelPubDate);
        wChannelPubDate.addModifyListener(lsMod);
        fdChannelPubDate=new FormData();
        fdChannelPubDate.left = new FormAttachment(middle, 0);
        fdChannelPubDate.top  = new FormAttachment(wChannelLink, margin);
        fdChannelPubDate.right= new FormAttachment(100, 0);
        wChannelPubDate.setLayoutData(fdChannelPubDate);
		
        // Channel Language
    	wlChannelLanguage=new Label(wChannelGroup, SWT.RIGHT);
        wlChannelLanguage.setText(BaseMessages.getString(PKG, "RssOutputDialog.ChannelLanguage.Label"));
        props.setLook(wlChannelLanguage);
        fdlChannelLanguage=new FormData();
        fdlChannelLanguage.left = new FormAttachment(0, 0);
        fdlChannelLanguage.top  = new FormAttachment(wChannelPubDate, margin);
        fdlChannelLanguage.right= new FormAttachment(middle, -margin);
        wlChannelLanguage.setLayoutData(fdlChannelLanguage);
        wChannelLanguage=new CCombo(wChannelGroup, SWT.BORDER | SWT.READ_ONLY);
        wChannelLanguage.setEditable(true);
        wChannelLanguage.setItems(fieldNames);
        props.setLook(wChannelLanguage);
        wChannelLanguage.addModifyListener(lsMod);
        fdChannelLanguage=new FormData();
        fdChannelLanguage.left = new FormAttachment(middle, 0);
        fdChannelLanguage.top  = new FormAttachment(wChannelPubDate, margin);
        fdChannelLanguage.right= new FormAttachment(100, 0);
        wChannelLanguage.setLayoutData(fdChannelLanguage);
        
        // Channel Author
		wlChannelAuthor=new Label(wChannelGroup, SWT.RIGHT);
        wlChannelAuthor.setText(BaseMessages.getString(PKG, "RssOutputDialog.ChannelAuthor.Label"));
        props.setLook(wlChannelAuthor);
        fdlChannelAuthor=new FormData();
        fdlChannelAuthor.left = new FormAttachment(0, 0);
        fdlChannelAuthor.top  = new FormAttachment(wChannelLanguage, margin);
        fdlChannelAuthor.right= new FormAttachment(middle, -margin);
        wlChannelAuthor.setLayoutData(fdlChannelAuthor);
        wChannelAuthor=new CCombo(wChannelGroup, SWT.BORDER | SWT.READ_ONLY);
        wChannelAuthor.setEditable(true);
        wChannelAuthor.setItems(fieldNames);
        props.setLook(wChannelAuthor);
        wChannelAuthor.addModifyListener(lsMod);
        fdChannelAuthor=new FormData();
        fdChannelAuthor.left = new FormAttachment(middle, 0);
        fdChannelAuthor.top  = new FormAttachment(wChannelLanguage, margin);
        fdChannelAuthor.right= new FormAttachment(100, 0);
        wChannelAuthor.setLayoutData(fdChannelAuthor);
        
        // Channel Copyright
    	wlChannelCopyright=new Label(wChannelGroup, SWT.RIGHT);
        wlChannelCopyright.setText(BaseMessages.getString(PKG, "RssOutputDialog.ChannelCopyright.Label"));
        props.setLook(wlChannelCopyright);
        fdlChannelCopyright=new FormData();
        fdlChannelCopyright.left = new FormAttachment(0, 0);
        fdlChannelCopyright.top  = new FormAttachment(wChannelAuthor, margin);
        fdlChannelCopyright.right= new FormAttachment(middle, -margin);
        wlChannelCopyright.setLayoutData(fdlChannelCopyright);
        wChannelCopyright=new CCombo(wChannelGroup, SWT.BORDER | SWT.READ_ONLY);
        wChannelCopyright.setEditable(true);
        wChannelCopyright.setItems(fieldNames);
        props.setLook(wChannelCopyright);
        wChannelCopyright.addModifyListener(lsMod);
        fdChannelCopyright=new FormData();
        fdChannelCopyright.left = new FormAttachment(middle, 0);
        fdChannelCopyright.top  = new FormAttachment(wChannelAuthor, margin);
        fdChannelCopyright.right= new FormAttachment(100, 0);
        wChannelCopyright.setLayoutData(fdChannelCopyright);
        
        // Add Image ?
		wlAddImage=new Label(wChannelGroup, SWT.RIGHT);
		wlAddImage.setText(BaseMessages.getString(PKG, "RssOutputDialog.AddImage.Label"));
 		props.setLook(wlAddImage);
		fdlAddImage=new FormData();
		fdlAddImage.left = new FormAttachment(0, 0);
		fdlAddImage.top  = new FormAttachment(wChannelCopyright, margin);
		fdlAddImage.right= new FormAttachment(middle, -margin);
		wlAddImage.setLayoutData(fdlAddImage);
		wAddImage=new Button(wChannelGroup, SWT.CHECK );
		wAddImage.setToolTipText(BaseMessages.getString(PKG, "RssOutputDialog.AddImage.Tooltip"));
 		props.setLook(wAddImage);
		fdAddImage=new FormData();
		fdAddImage.left = new FormAttachment(middle, 0);
		fdAddImage.top  = new FormAttachment(wChannelCopyright, margin);
		fdAddImage.right= new FormAttachment(100, 0);
		wAddImage.setLayoutData(fdAddImage);
		wAddImage.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setImage();
					input.setChanged();
				}
			}
		);
		
        
        // Channel Image title
    	wlChannelImageTitle=new Label(wChannelGroup, SWT.RIGHT);
        wlChannelImageTitle.setText(BaseMessages.getString(PKG, "RssOutputDialog.ChannelImageTitle.Label"));
        props.setLook(wlChannelImageTitle);
        fdlChannelImageTitle=new FormData();
        fdlChannelImageTitle.left = new FormAttachment(0, 0);
        fdlChannelImageTitle.top  = new FormAttachment(wAddImage, margin);
        fdlChannelImageTitle.right= new FormAttachment(middle, -margin);
        wlChannelImageTitle.setLayoutData(fdlChannelImageTitle);
        wChannelImageTitle=new CCombo(wChannelGroup, SWT.BORDER | SWT.READ_ONLY);
        wChannelImageTitle.setEditable(true);
        wChannelImageTitle.setItems(fieldNames);
        props.setLook(wChannelImageTitle);
        wChannelImageTitle.addModifyListener(lsMod);
        fdChannelImageTitle=new FormData();
        fdChannelImageTitle.left = new FormAttachment(middle, 0);
        fdChannelImageTitle.top  = new FormAttachment(wAddImage, margin);
        fdChannelImageTitle.right= new FormAttachment(100, 0);
        wChannelImageTitle.setLayoutData(fdChannelImageTitle);

        // Channel Image Link
    	wlChannelImageLink=new Label(wChannelGroup, SWT.RIGHT);
        wlChannelImageLink.setText(BaseMessages.getString(PKG, "RssOutputDialog.ChannelImageLink.Label"));
        props.setLook(wlChannelImageLink);
        fdlChannelImageLink=new FormData();
        fdlChannelImageLink.left = new FormAttachment(0, 0);
        fdlChannelImageLink.top  = new FormAttachment(wChannelImageTitle, margin);
        fdlChannelImageLink.right= new FormAttachment(middle, -margin);
        wlChannelImageLink.setLayoutData(fdlChannelImageLink);
        wChannelImageLink=new CCombo(wChannelGroup, SWT.BORDER | SWT.READ_ONLY);
        wChannelImageLink.setEditable(true);
        wChannelImageLink.setItems(fieldNames);
        props.setLook(wChannelImageLink);
        wChannelImageLink.addModifyListener(lsMod);
        fdChannelImageLink=new FormData();
        fdChannelImageLink.left = new FormAttachment(middle, 0);
        fdChannelImageLink.top  = new FormAttachment(wChannelImageTitle, margin);
        fdChannelImageLink.right= new FormAttachment(100, 0);
        wChannelImageLink.setLayoutData(fdChannelImageLink);

        // Channel Image Url
        wlChannelImageUrl=new Label(wChannelGroup, SWT.RIGHT);
        wlChannelImageUrl.setText(BaseMessages.getString(PKG, "RssOutputDialog.ChannelImageUrl.Label"));
        props.setLook(wlChannelImageUrl);
        fdlChannelImageUrl=new FormData();
        fdlChannelImageUrl.left = new FormAttachment(0, 0);
        fdlChannelImageUrl.top  = new FormAttachment(wChannelImageLink, margin);
        fdlChannelImageUrl.right= new FormAttachment(middle, -margin);
        wlChannelImageUrl.setLayoutData(fdlChannelImageUrl);
        wChannelImageUrl=new CCombo(wChannelGroup, SWT.BORDER | SWT.READ_ONLY);
        wChannelImageUrl.setEditable(true);
        wChannelImageUrl.setItems(fieldNames);
        props.setLook(wChannelImageUrl);
        wChannelImageUrl.addModifyListener(lsMod);
        fdChannelImageUrl=new FormData();
        fdChannelImageUrl.left = new FormAttachment(middle, 0);
        fdChannelImageUrl.top  = new FormAttachment(wChannelImageLink, margin);
        fdChannelImageUrl.right= new FormAttachment(100, 0);
        wChannelImageUrl.setLayoutData(fdChannelImageUrl);

        // Channel Image Description
        wlChannelImageDescription=new Label(wChannelGroup, SWT.RIGHT);
        wlChannelImageDescription.setText(BaseMessages.getString(PKG, "RssOutputDialog.ChannelImageDescription.Label"));
        props.setLook(wlChannelImageDescription);
        fdlChannelImageDescription=new FormData();
        fdlChannelImageDescription.left = new FormAttachment(0, 0);
        fdlChannelImageDescription.top  = new FormAttachment(wChannelImageUrl, margin);
        fdlChannelImageDescription.right= new FormAttachment(middle, -margin);
        wlChannelImageDescription.setLayoutData(fdlChannelImageDescription);
        wChannelImageDescription=new CCombo(wChannelGroup, SWT.BORDER | SWT.READ_ONLY);
        wChannelImageDescription.setEditable(true);
        wChannelImageDescription.setItems(fieldNames);
        props.setLook(wChannelImageDescription);
        wChannelImageDescription.addModifyListener(lsMod);
        fdChannelImageDescription=new FormData();
        fdChannelImageDescription.left = new FormAttachment(middle, 0);
        fdChannelImageDescription.top  = new FormAttachment(wChannelImageUrl, margin);
        fdChannelImageDescription.right= new FormAttachment(100, 0);
        wChannelImageDescription.setLayoutData(fdChannelImageDescription);

        // Encoding
        wlEncoding=new Label(wChannelComp, SWT.RIGHT);
        wlEncoding.setText(BaseMessages.getString(PKG, "RssOutputDialog.Encoding.Label"));
        props.setLook(wlEncoding);
        fdlEncoding=new FormData();
        fdlEncoding.left = new FormAttachment(0, 0);
        fdlEncoding.top  = new FormAttachment(wChannelGroup, 2*margin);
        fdlEncoding.right= new FormAttachment(middle, -margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding=new CCombo(wChannelComp, SWT.BORDER | SWT.READ_ONLY);
        wEncoding.setEditable(true);
        props.setLook(wEncoding);
        wEncoding.addModifyListener(lsMod);
        fdEncoding=new FormData();
        fdEncoding.left = new FormAttachment(middle, 0);
        fdEncoding.top  = new FormAttachment(wChannelGroup, 2*margin);
        fdEncoding.right= new FormAttachment(100, 0);
        wEncoding.setLayoutData(fdEncoding);
        wEncoding.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setEncodings();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );

        
        // Version
		wlVersion=new Label(wChannelComp, SWT.RIGHT);
        wlVersion.setText(BaseMessages.getString(PKG, "RssOutputDialog.Version.Label"));
        props.setLook(wlVersion);
        fdlVersion=new FormData();
        fdlVersion.left = new FormAttachment(0, 0);
        fdlVersion.top  = new FormAttachment(wEncoding, margin);
        fdlVersion.right= new FormAttachment(middle, -margin);
        wlVersion.setLayoutData(fdlVersion);
        wVersion=new CCombo(wChannelComp, SWT.BORDER | SWT.READ_ONLY);
        wVersion.setEditable(true);
        wVersion.setItems(rss_versions);
        props.setLook(wVersion);
        wVersion.addModifyListener(lsMod);
        fdVersion=new FormData();
        fdVersion.left = new FormAttachment(middle, 0);
        fdVersion.top  = new FormAttachment(wEncoding, margin);
        fdVersion.right= new FormAttachment(100, 0);
        wVersion.setLayoutData(fdVersion);
                
        
		fdChannelGroup = new FormData();
		fdChannelGroup.left = new FormAttachment(0, margin);
		fdChannelGroup.top = new FormAttachment(wDisplayItem, margin);
		fdChannelGroup.right = new FormAttachment(100, -margin);
		wChannelGroup.setLayoutData(fdChannelGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF ChannelGroup GROUP
		// ///////////////////////////////////////////////////////////

		fdChannelComp=new FormData();
		fdChannelComp.left  = new FormAttachment(0, 0);
		fdChannelComp.top   = new FormAttachment(0, 0);
		fdChannelComp.right = new FormAttachment(100, 0);
		fdChannelComp.bottom= new FormAttachment(100, 0);
		wChannelComp.setLayoutData(fdChannelComp);
		
		wChannelComp.layout();
		wChannelTab.setControl(wChannelComp);
 		props.setLook(wChannelComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////

		
		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		
		
		
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "RssOutputDialog.GeneralTab.TabTitle"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		
		
		// Fields grouping?
		// ////////////////////////
		// START OF Fields GROUP
		// 

		wFields = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wFields);
		wFields.setText(BaseMessages.getString(PKG, "RssOutputDialog.Group.Fields.Label"));
		
		FormLayout groupFieldsLayout = new FormLayout();
		groupFieldsLayout.marginWidth = 10;
		groupFieldsLayout.marginHeight = 10;
		wFields.setLayout(groupFieldsLayout);
		
		// RemarqItem
		wlRemarqItem=new Label(wFields, SWT.RIGHT);
		wlRemarqItem.setText(BaseMessages.getString(PKG, "RssOutputDialog.RemarqItem.Label"));
		props.setLook(wlRemarqItem);
		fdlRemarqItem=new FormData();
		fdlRemarqItem.left  = new FormAttachment(0, 0);
		fdlRemarqItem.top   = new FormAttachment(0, margin);
		//fdlRemarq.right = new FormAttachment(middle, -margin);
		wlRemarqItem.setLayoutData(fdlRemarqItem);
	
		
 		// Item Title
		wlItemTitle=new Label(wFields, SWT.RIGHT);
        wlItemTitle.setText(BaseMessages.getString(PKG, "RssOutputDialog.ItemTitle.Label"));
        props.setLook(wlItemTitle);
        fdlItemTitle=new FormData();
        fdlItemTitle.left = new FormAttachment(0, 0);
        fdlItemTitle.top  = new FormAttachment(wlRemarqItem, 3*margin);
        fdlItemTitle.right= new FormAttachment(middle, -margin);
        wlItemTitle.setLayoutData(fdlItemTitle);
        wItemTitle=new CCombo(wFields, SWT.BORDER | SWT.READ_ONLY);
        wItemTitle.setEditable(true);
        wItemTitle.setItems(fieldNames);
        props.setLook(wItemTitle);
        wItemTitle.addModifyListener(lsMod);
        fdItemTitle=new FormData();
        fdItemTitle.left = new FormAttachment(middle, 0);
        fdItemTitle.top  = new FormAttachment(wlRemarqItem, 3*margin);
        fdItemTitle.right= new FormAttachment(100, 0);
        wItemTitle.setLayoutData(fdItemTitle);
          

 		// Item Description
		wlItemDescription=new Label(wFields, SWT.RIGHT);
        wlItemDescription.setText(BaseMessages.getString(PKG, "RssOutputDialog.ItemDescripion.Label"));
        props.setLook(wlItemDescription);
        fdlItemDescription=new FormData();
        fdlItemDescription.left = new FormAttachment(0, 0);
        fdlItemDescription.top  = new FormAttachment(wItemTitle, margin);
        fdlItemDescription.right= new FormAttachment(middle, -margin);
        wlItemDescription.setLayoutData(fdlItemDescription);
        wItemDescription=new CCombo(wFields, SWT.BORDER | SWT.READ_ONLY);
        wItemDescription.setEditable(true);
        wItemDescription.setItems(fieldNames);
        props.setLook(wItemDescription);
        wItemDescription.addModifyListener(lsMod);
        fdItemDescription=new FormData();
        fdItemDescription.left = new FormAttachment(middle, 0);
        fdItemDescription.top  = new FormAttachment(wItemTitle, margin);
        fdItemDescription.right= new FormAttachment(100, 0);
        wItemDescription.setLayoutData(fdItemDescription);
       
 		// Item Link
		wlItemLink=new Label(wFields, SWT.RIGHT);
        wlItemLink.setText(BaseMessages.getString(PKG, "RssOutputDialog.ItemLink.Label"));
        props.setLook(wlItemLink);
        fdlItemLink=new FormData();
        fdlItemLink.left = new FormAttachment(0, 0);
        fdlItemLink.top  = new FormAttachment(wItemDescription, margin);
        fdlItemLink.right= new FormAttachment(middle, -margin);
        wlItemLink.setLayoutData(fdlItemLink);
        wItemLink=new CCombo(wFields, SWT.BORDER | SWT.READ_ONLY);
        wItemLink.setEditable(true);
        wItemLink.setItems(fieldNames);
        props.setLook(wItemLink);
        wItemLink.addModifyListener(lsMod);
        fdItemLink=new FormData();
        fdItemLink.left = new FormAttachment(middle, 0);
        fdItemLink.top  = new FormAttachment(wItemDescription, margin);
        fdItemLink.right= new FormAttachment(100, 0);
        wItemLink.setLayoutData(fdItemLink); 
 		
        // Item PubDate
		wlItemPubDate=new Label(wFields, SWT.RIGHT);
        wlItemPubDate.setText(BaseMessages.getString(PKG, "RssOutputDialog.ItemPubDate.Label"));
        props.setLook(wlItemPubDate);
        fdlItemPubDate=new FormData();
        fdlItemPubDate.left = new FormAttachment(0, 0);
        fdlItemPubDate.top  = new FormAttachment(wItemLink, margin);
        fdlItemPubDate.right= new FormAttachment(middle, -margin);
        wlItemPubDate.setLayoutData(fdlItemPubDate);
        wItemPubDate=new CCombo(wFields, SWT.BORDER | SWT.READ_ONLY);
        wItemPubDate.setEditable(true);
        wItemPubDate.setItems(fieldNames);
        props.setLook(wItemPubDate);
        wItemPubDate.addModifyListener(lsMod);
        fdItemPubDate=new FormData();
        fdItemPubDate.left = new FormAttachment(middle, 0);
        fdItemPubDate.top  = new FormAttachment(wItemLink, margin);
        fdItemPubDate.right= new FormAttachment(100, 0);
        wItemPubDate.setLayoutData(fdItemPubDate);
         
 		// Item Author
		wlItemAuthor=new Label(wFields, SWT.RIGHT);
        wlItemAuthor.setText(BaseMessages.getString(PKG, "RssOutputDialog.ItemAuthor.Label"));
        props.setLook(wlItemAuthor);
        fdlItemAuthor=new FormData();
        fdlItemAuthor.left = new FormAttachment(0, 0);
        fdlItemAuthor.top  = new FormAttachment(wItemPubDate, margin);
        fdlItemAuthor.right= new FormAttachment(middle, -margin);
        wlItemAuthor.setLayoutData(fdlItemAuthor);
        wItemAuthor=new CCombo(wFields, SWT.BORDER | SWT.READ_ONLY);
        wItemAuthor.setEditable(true);
        wItemAuthor.setItems(fieldNames);
        props.setLook(wItemAuthor);
        wItemAuthor.addModifyListener(lsMod);
        fdItemAuthor=new FormData();
        fdItemAuthor.left = new FormAttachment(middle, 0);
        fdItemAuthor.top  = new FormAttachment(wItemPubDate, margin);
        fdItemAuthor.right= new FormAttachment(100, 0);
        wItemAuthor.setLayoutData(fdItemAuthor);
        
        // Add GeoRSS ?
		wlAddGeoRSS=new Label(wFields, SWT.RIGHT);
		wlAddGeoRSS.setText(BaseMessages.getString(PKG, "RssOutputDialog.AddGeoRSS.Label"));
 		props.setLook(wlAddGeoRSS);
		fdlAddGeoRSS=new FormData();
		fdlAddGeoRSS.left = new FormAttachment(0, 0);
		fdlAddGeoRSS.top  = new FormAttachment(wItemAuthor, margin);
		fdlAddGeoRSS.right= new FormAttachment(middle, -margin);
		wlAddGeoRSS.setLayoutData(fdlAddGeoRSS);
		wAddGeoRSS=new Button(wFields, SWT.CHECK );
		wAddGeoRSS.setToolTipText(BaseMessages.getString(PKG, "RssOutputDialog.AddGeoRSS.Tooltip"));
 		props.setLook(wAddGeoRSS);
		fdAddGeoRSS=new FormData();
		fdAddGeoRSS.left = new FormAttachment(middle, 0);
		fdAddGeoRSS.top  = new FormAttachment(wItemAuthor, margin);
		fdAddGeoRSS.right= new FormAttachment(100, 0);
		wAddGeoRSS.setLayoutData(fdAddGeoRSS);
		wAddGeoRSS.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					activateGeoRSS();
					input.setChanged();
				}
			}
		);
	    // Add GeoRSS ?
		wluseGeoRSSGML=new Label(wFields, SWT.RIGHT);
		wluseGeoRSSGML.setText(BaseMessages.getString(PKG, "RssOutputDialog.useGeoRSSGML.Label"));
 		props.setLook(wluseGeoRSSGML);
		fdluseGeoRSSGML=new FormData();
		fdluseGeoRSSGML.left = new FormAttachment(0, 0);
		fdluseGeoRSSGML.top  = new FormAttachment(wAddGeoRSS, margin);
		fdluseGeoRSSGML.right= new FormAttachment(middle, -margin);
		wluseGeoRSSGML.setLayoutData(fdluseGeoRSSGML);
		wuseGeoRSSGML=new Button(wFields, SWT.CHECK );
		wuseGeoRSSGML.setToolTipText(BaseMessages.getString(PKG, "RssOutputDialog.useGeoRSSGML.Tooltip"));
 		props.setLook(wuseGeoRSSGML);
		fduseGeoRSSGML=new FormData();
		fduseGeoRSSGML.left = new FormAttachment(middle, 0);
		fduseGeoRSSGML.top  = new FormAttachment(wAddGeoRSS, margin);
		fduseGeoRSSGML.right= new FormAttachment(100, 0);
		wuseGeoRSSGML.setLayoutData(fduseGeoRSSGML);
		wuseGeoRSSGML.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
       
 		// GeoPointLat
		wlGeoPointLat=new Label(wFields, SWT.RIGHT);
        wlGeoPointLat.setText(BaseMessages.getString(PKG, "RssOutputDialog.GeoPointLat.Label"));
        props.setLook(wlGeoPointLat);
        fdlGeoPointLat=new FormData();
        fdlGeoPointLat.left = new FormAttachment(0, 0);
        fdlGeoPointLat.top  = new FormAttachment(wuseGeoRSSGML, margin);
        fdlGeoPointLat.right= new FormAttachment(middle, -margin);
        wlGeoPointLat.setLayoutData(fdlGeoPointLat);
        wGeoPointLat=new CCombo(wFields, SWT.BORDER | SWT.READ_ONLY);
        wGeoPointLat.setEditable(true);
        wGeoPointLat.setItems(fieldNames);
        props.setLook(wGeoPointLat);
        wGeoPointLat.addModifyListener(lsMod);
        fdGeoPointLat=new FormData();
        fdGeoPointLat.left = new FormAttachment(middle, 0);
        fdGeoPointLat.top  = new FormAttachment(wuseGeoRSSGML, margin);
        fdGeoPointLat.right= new FormAttachment(100, 0);
        wGeoPointLat.setLayoutData(fdGeoPointLat);
        
 		// GeoPointLong
		wlGeoPointLong=new Label(wFields, SWT.RIGHT);
        wlGeoPointLong.setText(BaseMessages.getString(PKG, "RssOutputDialog.GeoPointLong.Label"));
        props.setLook(wlGeoPointLong);
        fdlGeoPointLong=new FormData();
        fdlGeoPointLong.left = new FormAttachment(0, 0);
        fdlGeoPointLong.top  = new FormAttachment(wGeoPointLat, margin);
        fdlGeoPointLong.right= new FormAttachment(middle, -margin);
        wlGeoPointLong.setLayoutData(fdlGeoPointLong);
        wGeoPointLong=new CCombo(wFields, SWT.BORDER | SWT.READ_ONLY);
        wGeoPointLong.setEditable(true);
        wGeoPointLong.setItems(fieldNames);
        props.setLook(wGeoPointLong);
        wGeoPointLong.addModifyListener(lsMod);
        fdGeoPointLong=new FormData();
        fdGeoPointLong.left = new FormAttachment(middle, 0);
        fdGeoPointLong.top  = new FormAttachment(wGeoPointLat, margin);
        fdGeoPointLong.right= new FormAttachment(100, 0);
        wGeoPointLong.setLayoutData(fdGeoPointLong);
        
        
		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, margin);
		fdFields.top = new FormAttachment(0, margin);
		fdFields.right = new FormAttachment(100, -margin);
		wFields.setLayoutData(fdFields);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Fields GROUP
		// ///////////////////////////////////////////////////////////
        
		
		
		
		
		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////

 		
		//////////////////////////
		// START OF CUSTOM TAB   ///
		//////////////////////////
		
		wCustomTab=new CTabItem(wTabFolder, SWT.NONE);
		wCustomTab.setText(BaseMessages.getString(PKG, "RssOutputDialog.CustomTab.TabTitle"));
		
		wCustomComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wCustomComp);

		FormLayout customLayout = new FormLayout();
		customLayout.marginWidth  = 3;
		customLayout.marginHeight = 3;
		wCustomComp.setLayout(customLayout);
		
		
		wlChannelCustom = new Label(wCustomComp, SWT.NONE);
		wlChannelCustom.setText(BaseMessages.getString(PKG, "RssOutputDialog.Keys.Label")); //$NON-NLS-1$
 		props.setLook(wlChannelCustom);
		fdlChannelCustom = new FormData();
		fdlChannelCustom.left = new FormAttachment(0, 0);
		fdlChannelCustom.top = new FormAttachment(0, margin);
		wlChannelCustom.setLayoutData(fdlChannelCustom);

		int nrChannelCols = 2;
		int nrChannelRows = (input.getChannelCustomFields() != null ? input.getChannelCustomFields().length : 1);

		ColumnInfo[] ciChannel = new ColumnInfo[nrChannelCols];
		ciChannel[0] = new ColumnInfo(BaseMessages.getString(PKG, "RssOutputDialog.ColumnInfo.Tag"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciChannel[1] = new ColumnInfo(BaseMessages.getString(PKG, "RssOutputDialog.ColumnInfo.Field"), ColumnInfo.COLUMN_TYPE_CCOMBO);
		ciChannel[0].setUsingVariables(true);
		ciChannel[1].setComboValues(fieldNames);
		wChannelCustom=new TableView(transMeta,wCustomComp,
			      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
			      ciChannel,
			      nrChannelRows,
			      lsMod,
				  props
			      );
		
		wGet = new Button(wCustomComp, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "RssOutputDialog.GetFields.Button")); //$NON-NLS-1$
		fdGet = new FormData();
		fdGet.right = new FormAttachment(100, 0);
		fdGet.top = new FormAttachment(wlChannelCustom, margin);
		wGet.setLayoutData(fdGet);
		fdChannelCustom = new FormData();
		fdChannelCustom.left = new FormAttachment(0, 0);
		fdChannelCustom.top = new FormAttachment(wlChannelCustom, margin);
		fdChannelCustom.right = new FormAttachment(wGet, -margin);
		fdChannelCustom.bottom = new FormAttachment(wlChannelCustom, 190);
		wChannelCustom.setLayoutData(fdChannelCustom);


		// THE BUTTONS
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		
		// THE Item Custom
		wlItemCustom = new Label(wCustomComp, SWT.NONE);
		wlItemCustom.setText(BaseMessages.getString(PKG, "RssOutputDialog.ItemCustom.Label")); //$NON-NLS-1$
 		props.setLook(wlItemCustom);
		fdlItemCustom = new FormData();
		fdlItemCustom.left = new FormAttachment(0, 0);
		fdlItemCustom.top = new FormAttachment(wChannelCustom, margin);
		wlItemCustom.setLayoutData(fdlItemCustom);

		int UpInsCols = 2;
		int UpInsRows = (input.getItemCustomFields()!= null ? input.getItemCustomFields().length : 1);

		ColumnInfo[] ciItem = new ColumnInfo[UpInsCols];
		ciItem[0] = new ColumnInfo(BaseMessages.getString(PKG, "RssOutputDialog.ColumnInfo.Tag"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciItem[1] = new ColumnInfo(BaseMessages.getString(PKG, "RssOutputDialog.ColumnInfo.Field"), ColumnInfo.COLUMN_TYPE_CCOMBO);//$NON-NLS-1$
		ciItem[1].setComboValues(fieldNames);
		wItemCustom = new TableView(transMeta,wCustomComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
				ciItem, UpInsRows, lsMod, props);

		wGetCustomItem = new Button(wCustomComp, SWT.PUSH);
		wGetCustomItem.setText(BaseMessages.getString(PKG, "RssOutputDialog.GetItemFields.Label")); //$NON-NLS-1$
		fdGetCustomItem = new FormData();
		fdGetCustomItem.top   = new FormAttachment(wlItemCustom, margin);
		fdGetCustomItem.right = new FormAttachment(100, 0);
		wGetCustomItem.setLayoutData(fdGetCustomItem);

		fdItemCustom = new FormData();
		fdItemCustom.left = new FormAttachment(0, 0);
		fdItemCustom.top = new FormAttachment(wlItemCustom, margin);
		fdItemCustom.right = new FormAttachment(wGetCustomItem, -margin);
		fdItemCustom.bottom = new FormAttachment(100, -2*margin);
		wItemCustom.setLayoutData(fdItemCustom);
		
		
		fdCustomComp=new FormData();
		fdCustomComp.left  = new FormAttachment(0, 0);
		fdCustomComp.top   = new FormAttachment(0, 0);
		fdCustomComp.right = new FormAttachment(100, 0);
		fdCustomComp.bottom= new FormAttachment(100, 0);
		wCustomComp.setLayoutData(fdCustomComp);
		
		wCustomComp.layout();
		wCustomTab.setControl(wCustomComp);
 		props.setLook(wCustomComp);
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF CUSTOM TAB
		/////////////////////////////////////////////////////////////

 		
 		//////////////////////////////////////
		// START OF CUSTOM NAMESPACE TAB   ///
		//////////////////////////////////////
		
		wCustomNameSpace=new CTabItem(wTabFolder, SWT.NONE);
		wCustomNameSpace.setText(BaseMessages.getString(PKG, "RssOutputDialog.CustomNameSpace.TabTitle"));
		wCustomNameSpaceComp  = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wCustomNameSpaceComp);
		FormLayout customnamespaceLayout = new FormLayout();
		customnamespaceLayout.marginWidth  = 3;
		customnamespaceLayout.marginHeight = 3;
		wCustomNameSpaceComp.setLayout(customnamespaceLayout);

		// Namespaces
		wlNameSpace = new Label(wCustomNameSpaceComp, SWT.NONE);
		wlNameSpace.setText(BaseMessages.getString(PKG, "RssOutputDialog.NameSpace.Label")); //$NON-NLS-1$
 		props.setLook(wlNameSpace);
		fdlNameSpace = new FormData();
		fdlNameSpace.left = new FormAttachment(0, 0);
		fdlNameSpace.top = new FormAttachment(0, margin);
		wlNameSpace.setLayoutData(fdlItemCustom);

 		
		int nrRows = (input.getNameSpaces()!= null ? input.getNameSpaces().length : 1);

		ColumnInfo[] ciNameSpace = new ColumnInfo[2];
		ciNameSpace[0] = new ColumnInfo(BaseMessages.getString(PKG, "RssOutputDialog.ColumnInfo.NameSpace.Title"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciNameSpace[1] = new ColumnInfo(BaseMessages.getString(PKG, "RssOutputDialog.ColumnInfo.NameSpace"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciNameSpace[0].setUsingVariables(true);
		ciNameSpace[1].setUsingVariables(true);
		wNameSpaceCustom = new TableView(transMeta,wCustomNameSpaceComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
				ciNameSpace, nrRows, lsMod, props);
		
		fdNameSpaceCustom= new FormData();
		fdNameSpaceCustom.left = new FormAttachment(0, 0);
		fdNameSpaceCustom.top = new FormAttachment(wlNameSpace, margin);
		fdNameSpaceCustom.right = new FormAttachment(100, -margin);
		fdNameSpaceCustom.bottom = new FormAttachment(100, -2*margin);
		wNameSpaceCustom.setLayoutData(fdNameSpaceCustom);
		
		
		
		
		
		fdCustomNameSpace=new FormData();
		fdCustomNameSpace.left  = new FormAttachment(0, 0);
		fdCustomNameSpace.top   = new FormAttachment(0, 0);
		fdCustomNameSpace.right = new FormAttachment(100, 0);
		fdCustomNameSpace.bottom= new FormAttachment(100, 0);
		wCustomNameSpaceComp.setLayoutData(fdCustomNameSpace);
		
		wCustomNameSpaceComp.layout();
		wCustomNameSpace.setControl(wCustomNameSpaceComp);
 		props.setLook(wCustomNameSpaceComp);
 		
		/////////////////////////////////////////////////////////////
		/// END OF CUSTOM NAMESPACE TAB
		/////////////////////////////////////////////////////////////

		
		
		//////////////////////////
		// START OF CONTENT TAB///
		///
		wContentTab=new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText(BaseMessages.getString(PKG, "RssOutputDialog.ContentTab.TabTitle"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
 		wContentComp.setLayout(contentLayout);
		
 		// File grouping?
		// ////////////////////////
		// START OF FileName GROUP
		// 

		wFileName = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wFileName);
		wFileName.setText(BaseMessages.getString(PKG, "RssOutputDialog.Group.File.Label"));
		
		FormLayout groupFileLayout = new FormLayout();
		groupFileLayout.marginWidth = 10;
		groupFileLayout.marginHeight = 10;
		wFileName.setLayout(groupFileLayout);
		
		
		
		// Filename line
		wlFilename=new Label(wFileName, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "RssOutputDialog.Filename.Label"));
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wFields, margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename=new Button(wFileName, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename);
		wbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(wFields, 0);
		wbFilename.setLayoutData(fdbFilename);

		wFilename=new TextVar(transMeta,wFileName, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top  = new FormAttachment(wFields, margin);
		fdFilename.right= new FormAttachment(wbFilename, -margin);
		wFilename.setLayoutData(fdFilename);
		
		// Create Parent Folder
		wlCreateParentFolder=new Label(wFileName, SWT.RIGHT);
		wlCreateParentFolder.setText(BaseMessages.getString(PKG, "RssOutputDialog.CreateParentFolder.Label"));
 		props.setLook(wlCreateParentFolder);
		fdlCreateParentFolder=new FormData();
		fdlCreateParentFolder.left = new FormAttachment(0, 0);
		fdlCreateParentFolder.top  = new FormAttachment(wFilename, margin);
		fdlCreateParentFolder.right= new FormAttachment(middle, -margin);
		wlCreateParentFolder.setLayoutData(fdlCreateParentFolder);
		wCreateParentFolder=new Button(wFileName, SWT.CHECK );
		wCreateParentFolder.setToolTipText(BaseMessages.getString(PKG, "RssOutputDialog.CreateParentFolder.Tooltip"));
 		props.setLook(wCreateParentFolder);
		fdCreateParentFolder=new FormData();
		fdCreateParentFolder.left = new FormAttachment(middle, 0);
		fdCreateParentFolder.top  = new FormAttachment(wFilename, margin);
		fdCreateParentFolder.right= new FormAttachment(100, 0);
		wCreateParentFolder.setLayoutData(fdCreateParentFolder);
		wCreateParentFolder.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		
		// FileName defined in a field
		wlFileNameInField=new Label(wFileName, SWT.RIGHT);
		wlFileNameInField.setText(BaseMessages.getString(PKG, "RssOutputDialog.FileNameInField.Label"));
 		props.setLook(wlFileNameInField);
		fdlFileNameInField=new FormData();
		fdlFileNameInField.left = new FormAttachment(0, 0);
		fdlFileNameInField.top  = new FormAttachment(wCreateParentFolder, margin);
		fdlFileNameInField.right= new FormAttachment(middle, -margin);
		wlFileNameInField.setLayoutData(fdlFileNameInField);
		wFileNameInField=new Button(wFileName, SWT.CHECK );
		wFileNameInField.setToolTipText(BaseMessages.getString(PKG, "RssOutputDialog.FileNameInField.Tooltip"));
 		props.setLook(wFileNameInField);
		fdFileNameInField=new FormData();
		fdFileNameInField.left = new FormAttachment(middle, 0);
		fdFileNameInField.top  = new FormAttachment(wCreateParentFolder, margin);
		fdFileNameInField.right= new FormAttachment(100, 0);
		wFileNameInField.setLayoutData(fdFileNameInField);
		wFileNameInField.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setFieldFilename();
				}
			}
		);
		
		// FieldFieldFileName line
		wlFieldFilename=new Label(wFileName, SWT.RIGHT);
		wlFieldFilename.setText(BaseMessages.getString(PKG, "RssOutputDialog.FieldFilename.Label"));
        props.setLook(wlFieldFilename);
        fdlFieldFilename=new FormData();
        fdlFieldFilename.left = new FormAttachment(0, 0);
        fdlFieldFilename.top  = new FormAttachment(wFileNameInField, margin);
        fdlFieldFilename.right= new FormAttachment(middle, -margin);
        wlFieldFilename.setLayoutData(fdlFieldFilename);
        wFieldFilename=new CCombo(wFileName, SWT.BORDER | SWT.READ_ONLY);
        wFieldFilename.setEditable(true);
        wFieldFilename.setItems(fieldNames);
        props.setLook(wFieldFilename);
        wFieldFilename.addModifyListener(lsMod);
        fdFieldFilename=new FormData();
        fdFieldFilename.left = new FormAttachment(middle, 0);
        fdFieldFilename.top  = new FormAttachment(wFileNameInField, margin);
        fdFieldFilename.right= new FormAttachment(100, -margin);
        wFieldFilename.setLayoutData(fdFieldFilename);
        
		
		// Extension line
		wlExtension=new Label(wFileName, SWT.RIGHT);
		wlExtension.setText(BaseMessages.getString(PKG, "System.Label.Extension"));
 		props.setLook(wlExtension);
		fdlExtension=new FormData();
		fdlExtension.left = new FormAttachment(0, 0);
		fdlExtension.top  = new FormAttachment(wFieldFilename, margin);
		fdlExtension.right= new FormAttachment(middle, -margin);
		wlExtension.setLayoutData(fdlExtension);
		
		wExtension=new TextVar(transMeta,wFileName, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wExtension);
 		wExtension.addModifyListener(lsMod);
 		fdExtension=new FormData();
		fdExtension.left = new FormAttachment(middle, 0);
		fdExtension.top  = new FormAttachment(wFieldFilename, margin);
		fdExtension.right= new FormAttachment(100, -margin);
		wExtension.setLayoutData(fdExtension);

		// Create multi-part file?
		wlAddStepnr=new Label(wFileName, SWT.RIGHT);
		wlAddStepnr.setText(BaseMessages.getString(PKG, "RssOutputDialog.AddStepnr.Label"));
 		props.setLook(wlAddStepnr);
		fdlAddStepnr=new FormData();
		fdlAddStepnr.left = new FormAttachment(0, 0);
		fdlAddStepnr.top  = new FormAttachment(wExtension, 2*margin);
		fdlAddStepnr.right= new FormAttachment(middle, -margin);
		wlAddStepnr.setLayoutData(fdlAddStepnr);
		wAddStepnr=new Button(wFileName, SWT.CHECK);
 		props.setLook(wAddStepnr);
		fdAddStepnr=new FormData();
		fdAddStepnr.left = new FormAttachment(middle, 0);
		fdAddStepnr.top  = new FormAttachment(wExtension, 2*margin);
		fdAddStepnr.right= new FormAttachment(100, 0);
		wAddStepnr.setLayoutData(fdAddStepnr);
		wAddStepnr.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);

	
		// Create multi-part file?
		wlAddDate=new Label(wFileName, SWT.RIGHT);
		wlAddDate.setText(BaseMessages.getString(PKG, "RssOutputDialog.AddDate.Label"));
 		props.setLook(wlAddDate);
		fdlAddDate=new FormData();
		fdlAddDate.left = new FormAttachment(0, 0);
		fdlAddDate.top  = new FormAttachment(wAddStepnr, margin);
		fdlAddDate.right= new FormAttachment(middle, -margin);
		wlAddDate.setLayoutData(fdlAddDate);
		wAddDate=new Button(wFileName, SWT.CHECK);
 		props.setLook(wAddDate);
		fdAddDate=new FormData();
		fdAddDate.left = new FormAttachment(middle, 0);
		fdAddDate.top  = new FormAttachment(wAddStepnr, margin);
		fdAddDate.right= new FormAttachment(100, 0);
		wAddDate.setLayoutData(fdAddDate);
		wAddDate.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
					// System.out.println("wAddDate.getSelection()="+wAddDate.getSelection());
				}
			}
		);
		// Create multi-part file?
		wlAddTime=new Label(wFileName, SWT.RIGHT);
		wlAddTime.setText(BaseMessages.getString(PKG, "RssOutputDialog.AddTime.Label"));
 		props.setLook(wlAddTime);
		fdlAddTime=new FormData();
		fdlAddTime.left = new FormAttachment(0, 0);
		fdlAddTime.top  = new FormAttachment(wAddDate, margin);
		fdlAddTime.right= new FormAttachment(middle, -margin);
		wlAddTime.setLayoutData(fdlAddTime);
		wAddTime=new Button(wFileName, SWT.CHECK);
 		props.setLook(wAddTime);
		fdAddTime=new FormData();
		fdAddTime.left = new FormAttachment(middle, 0);
		fdAddTime.top  = new FormAttachment(wAddDate, margin);
		fdAddTime.right= new FormAttachment(100, 0);
		wAddTime.setLayoutData(fdAddTime);
		wAddTime.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		

		


		wbShowFiles=new Button(wFileName, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbShowFiles);
		wbShowFiles.setText(BaseMessages.getString(PKG, "RssOutputDialog.ShowFiles.Button"));
		fdbShowFiles=new FormData();
		fdbShowFiles.left = new FormAttachment(middle, 0);
		fdbShowFiles.top  = new FormAttachment(wAddTime, margin*2);
		wbShowFiles.setLayoutData(fdbShowFiles);
		wbShowFiles.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					RssOutputMeta tfoi = new RssOutputMeta();
					getInfo(tfoi);
					try 
					{
						String files[] = tfoi.getFiles(transMeta);
						if (files!=null && files.length>0)
						{
							EnterSelectionDialog esd = new EnterSelectionDialog(shell, files, BaseMessages.getString(PKG, "RssOutputDialog.SelectOutputFiles.DialogTitle"), BaseMessages.getString(PKG, "RssOutputDialog.SelectOutputFiles.DialogMessage"));
							esd.setViewOnly();
							esd.open();
						}
						else
						{
							MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
							mb.setMessage(BaseMessages.getString(PKG, "RssOutputDialog.NoFilesFound.DialogMessage"));
							mb.setText(BaseMessages.getString(PKG, "System.DialogTitle.Error"));
							mb.open(); 
						}
					}catch(KettleStepException s)
					{
						
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage(BaseMessages.getString(PKG, "RssOutputDialog.ErrorGettingFiles.DialogMessage", s.getMessage()));
						mb.setText(BaseMessages.getString(PKG, "System.DialogTitle.Error"));
						mb.open(); 
					}	
				}
			}
		);
		
		
		
		fdFileName = new FormData();
		fdFileName.left = new FormAttachment(0, margin);
		fdFileName.top = new FormAttachment(wFields, margin);
		fdFileName.right = new FormAttachment(100, -margin);
		wFileName.setLayoutData(fdFileName);
		
		// ///////////////////////////////////////////////////////////
		// / END OF FileName GROUP
		// ///////////////////////////////////////////////////////////
        

 		
		
 		// File grouping?
		// ////////////////////////
		// START OF ResultFile GROUP
		// 

		wResultFile = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wResultFile);
		wResultFile.setText(BaseMessages.getString(PKG, "RssOutputDialog.Group.ResultFile.Label"));
		
		FormLayout groupResultFile = new FormLayout();
		groupResultFile.marginWidth = 10;
		groupResultFile.marginHeight = 10;
		wResultFile.setLayout(groupResultFile);

		// Add File to the result files name
		wlAddToResult=new Label(wResultFile, SWT.RIGHT);
		wlAddToResult.setText(BaseMessages.getString(PKG, "RssOutputDialog.AddFileToResult.Label"));
		props.setLook(wlAddToResult);
		fdlAddToResult=new FormData();
		fdlAddToResult.left  = new FormAttachment(0, 0);
		fdlAddToResult.top   = new FormAttachment(wFileName, margin);
		fdlAddToResult.right = new FormAttachment(middle, -margin);
		wlAddToResult.setLayoutData(fdlAddToResult);
		wAddToResult=new Button(wResultFile, SWT.CHECK);
		wAddToResult.setToolTipText(BaseMessages.getString(PKG, "RssOutputDialog.AddFileToResult.Tooltip"));
 		props.setLook(wAddToResult);
		fdAddToResult=new FormData();
		fdAddToResult.left  = new FormAttachment(middle, 0);
		fdAddToResult.top   = new FormAttachment(wFileName, margin);
		fdAddToResult.right = new FormAttachment(100, 0);
		wAddToResult.setLayoutData(fdAddToResult);
		SelectionAdapter lsSelAR = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                input.setChanged();
            }
        };
		wAddToResult.addSelectionListener(lsSelAR);

	
		fdResultFile = new FormData();
		fdResultFile.left = new FormAttachment(0, margin);
		fdResultFile.top = new FormAttachment(wFileName, margin);
		fdResultFile.right = new FormAttachment(100, -margin);
		wResultFile.setLayoutData(fdResultFile);
		
		// ///////////////////////////////////////////////////////////
		// / END OF ResultFile GROUP
		// ///////////////////////////////////////////////////////////
        
	
 		fdContentComp = new FormData();
 		fdContentComp.left  = new FormAttachment(0, 0);
 		fdContentComp.top   = new FormAttachment(0, 0);
 		fdContentComp.right = new FormAttachment(100, 0);
 		fdContentComp.bottom= new FormAttachment(100, 0);
 		wContentComp.setLayoutData(wContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);


		/////////////////////////////////////////////////////////////
		/// END OF CONTENT TAB
		/////////////////////////////////////////////////////////////


		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
	


		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get(wChannelCustom);        } };
		lsGetCustomItem   = new Listener() { public void handleEvent(Event e) { get(wItemCustom);        } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		wGet.addListener   (SWT.Selection, lsGet   );
		wGetCustomItem.addListener   (SWT.Selection, lsGetCustomItem   );
		
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );

		wbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					FileDialog dialog = new FileDialog(shell, SWT.SAVE);
					dialog.setFilterExtensions(new String[] {"*.txt", "*.TXT", "*"});
					if (wFilename.getText()!=null)
					{
						dialog.setFileName(transMeta.environmentSubstitute(wFilename.getText()));
					}
					dialog.setFilterNames(new String[] {BaseMessages.getString(PKG, "System.FileType.TextFiles"), BaseMessages.getString(PKG, "System.FileType.CSVFiles"), BaseMessages.getString(PKG, "System.FileType.AllFiles")});
					if (dialog.open()!=null)
					{
						String extension = wExtension.getText();
						if ( extension != null && dialog.getFileName() != null &&
								dialog.getFileName().endsWith("." + extension) )
						{
							// The extension is filled in and matches the end 
							// of the selected file => Strip off the extension.
							String fileName = dialog.getFileName();
						    wFilename.setText(dialog.getFilterPath()+System.getProperty("file.separator")+
						    		          fileName.substring(0, fileName.length() - (extension.length()+1)));
						}
						else
						{
						    wFilename.setText(dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName());
						}
					}
				}
			}
		);
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		lsResize = new Listener() 
		{
			public void handleEvent(Event event) 
			{
				//Point size = shell.getSize();
				
			}
		};
		shell.addListener(SWT.Resize, lsResize);

		
		wTabFolder.setSelection(0);
		
		// Set the shell size, based upon previous time...
		setSize();
		getData();
		setCustomRSS();
		setFieldFilename();
		setImage();
		activateGeoRSS();
		
		input.setChanged(changed);//backupChanged);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	private void setImage()
	{
		wlChannelImageTitle.setEnabled(wAddImage.getSelection());
		wChannelImageTitle.setEnabled(wAddImage.getSelection());
		wlChannelImageLink.setEnabled(wAddImage.getSelection());
		wChannelImageLink.setEnabled(wAddImage.getSelection());
		wlChannelImageUrl.setEnabled(wAddImage.getSelection());
		wChannelImageUrl.setEnabled(wAddImage.getSelection());
		wChannelImageDescription.setEnabled(wAddImage.getSelection());
		wlChannelImageDescription.setEnabled(wAddImage.getSelection());
			
	}
	private void setCustomRSS()
	{
		
		wlDisplayItem.setEnabled(wCustomRss.getSelection());
		wDisplayItem.setEnabled(wCustomRss.getSelection());
		wlRemarqChannel.setEnabled(!wCustomRss.getSelection());
		wlRemarqMandatory.setEnabled(!wCustomRss.getSelection());
		wChannelTitle.setEnabled(!wCustomRss.getSelection());
		wlChannelTitle.setEnabled(!wCustomRss.getSelection());
		wlChannelTitle.setEnabled(!wCustomRss.getSelection());
		wlChannelTitle.setEnabled(!wCustomRss.getSelection());
		wlChannelTitle.setEnabled(!wCustomRss.getSelection());
		wlChannelTitle.setEnabled(!wCustomRss.getSelection());
		wlChannelTitle.setEnabled(!wCustomRss.getSelection());
		wlChannelTitle.setEnabled(!wCustomRss.getSelection());
		wlChannelTitle.setEnabled(!wCustomRss.getSelection());
		wlChannelTitle.setEnabled(!wCustomRss.getSelection());
		wlChannelDescription.setEnabled(!wCustomRss.getSelection());
		wChannelDescription.setEnabled(!wCustomRss.getSelection());
		wlChannelLink.setEnabled(!wCustomRss.getSelection());
		wChannelLink.setEnabled(!wCustomRss.getSelection());
		
		wlChannelPubDate.setEnabled(!wCustomRss.getSelection());
		wChannelPubDate.setEnabled(!wCustomRss.getSelection());
		wlChannelLanguage.setEnabled(!wCustomRss.getSelection());
		wChannelLanguage.setEnabled(!wCustomRss.getSelection());
		wlChannelAuthor.setEnabled(!wCustomRss.getSelection());
		wChannelAuthor.setEnabled(!wCustomRss.getSelection());
		wChannelCopyright.setEnabled(!wCustomRss.getSelection());
		wlChannelCopyright.setEnabled(!wCustomRss.getSelection());
		
		wlChannelImageTitle.setEnabled(!wCustomRss.getSelection());
		wChannelImageTitle.setEnabled(!wCustomRss.getSelection());
		wlChannelImageLink.setEnabled(!wCustomRss.getSelection());
		wChannelImageLink.setEnabled(!wCustomRss.getSelection());
		wlChannelImageUrl.setEnabled(!wCustomRss.getSelection());
		wChannelImageUrl.setEnabled(!wCustomRss.getSelection());
		wChannelImageDescription.setEnabled(!wCustomRss.getSelection());
		wlAddImage.setEnabled(!wCustomRss.getSelection());
		wAddImage.setEnabled(!wCustomRss.getSelection());
		
		wlRemarqItem.setEnabled(!wCustomRss.getSelection());
		wlItemTitle.setEnabled(!wCustomRss.getSelection());
		wItemTitle.setEnabled(!wCustomRss.getSelection());
		wItemDescription.setEnabled(!wCustomRss.getSelection());
		wlItemDescription.setEnabled(!wCustomRss.getSelection());
		wlItemLink.setEnabled(!wCustomRss.getSelection());
		wItemLink.setEnabled(!wCustomRss.getSelection());
		wlItemPubDate.setEnabled(!wCustomRss.getSelection());
		wItemPubDate.setEnabled(!wCustomRss.getSelection());
		wlItemAuthor.setEnabled(!wCustomRss.getSelection());
		wItemAuthor.setEnabled(!wCustomRss.getSelection());
		wAddGeoRSS.setEnabled(!wCustomRss.getSelection());
		
		wlChannelCustom.setEnabled(wCustomRss.getSelection());
		wChannelCustom.setEnabled(wCustomRss.getSelection());
		wItemCustom.setEnabled(wCustomRss.getSelection());
		wGetCustomItem.setEnabled(wCustomRss.getSelection());
		wGet.setEnabled(wCustomRss.getSelection());
		wlNameSpace.setEnabled(wCustomRss.getSelection());
		wCustomNameSpaceComp.setEnabled(wCustomRss.getSelection());
		
	}
	private void activateGeoRSS()
	{
		wluseGeoRSSGML.setEnabled(wAddGeoRSS.getSelection());
		wuseGeoRSSGML.setEnabled(wAddGeoRSS.getSelection());
		wlGeoPointLat.setEnabled(wAddGeoRSS.getSelection());
		wGeoPointLat.setEnabled(wAddGeoRSS.getSelection());
		wlGeoPointLong.setEnabled(wAddGeoRSS.getSelection());
		wGeoPointLong.setEnabled(wAddGeoRSS.getSelection());
	}
	 private void setFieldFilename()
	    {
	    	wlFieldFilename.setEnabled(wFileNameInField.getSelection());
	    	wFieldFilename.setEnabled(wFileNameInField.getSelection());
	    	wlExtension.setEnabled(!wFileNameInField.getSelection());
	    	wExtension.setEnabled(!wFileNameInField.getSelection());
	    	wlFilename.setEnabled(!wFileNameInField.getSelection());
	    	wFilename.setEnabled(!wFileNameInField.getSelection());
	    	wbFilename.setEnabled(!wFileNameInField.getSelection());
	    	
	    	
	    	wAddDate.setEnabled(!wFileNameInField.getSelection());
	    	wAddTime.setEnabled(!wFileNameInField.getSelection());
	    	wAddStepnr.setEnabled(!wFileNameInField.getSelection());
	    	
	      	wbShowFiles.setEnabled(!wFileNameInField.getSelection());
	      	
	    	
	    }
	 private void setEncodings()
    {
        // Encoding of the text file:
        if (!gotEncodings)
        {
            gotEncodings = true;
            
            wEncoding.removeAll();
            List<Charset> values = new ArrayList<Charset>(Charset.availableCharsets().values());
            for (int i=0;i<values.size();i++)
            {
                Charset charSet = (Charset)values.get(i);
                wEncoding.add( charSet.displayName() );
            }
            
            // Now select the default!
            String defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
            int idx = Const.indexOfString(defEncoding, wEncoding.getItems() );
            if (idx>=0) wEncoding.select( idx );
        }
    }
	 private void getFields()
	 {
		if(!gotPreviousFields)
		{
		 try{
			 RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			 if (r!=null)
			 {
				fieldNames = r.getFieldNames();
			 }
		 	}catch(KettleException ke){
				new ErrorDialog(shell, BaseMessages.getString(PKG, "RssOutputDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "RssOutputDialog.FailedToGetFields.DialogMessage"), ke);
			}
		 	gotPreviousFields=true;
		}
	 }

	 private void get(TableView wTable)
		{
			try
			{
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r != null)
				{
	                TableItemInsertListener listener = new TableItemInsertListener()
	                {
	                    public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
	                    {
	                        return true;
	                    }
	                };
	                BaseStepDialog.getFieldsFromPrevious(r, wTable, 1, new int[] { 1, 2 }, new int[] {}, -1, -1 , listener);
				}
			}
			catch (KettleException ke)
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "RssOutputDialog.UnableToGetFieldsError.DialogTitle"), //$NON-NLS-1$
						BaseMessages.getString(PKG, "RssOutputDialog.UnableToGetFieldsError.DialogMessage"), ke); //$NON-NLS-1$
			}
		}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{

		if (input.getVersion()  != null) wVersion.setText(input.getVersion());
		if (input.getEncoding()  != null) 
			wEncoding.setText(input.getEncoding());
		else
			wEncoding.setText("iso-8859-1");
		
		if (input.getChannelDescription()  != null) wChannelDescription.setText(input.getChannelDescription());
		if (input.getChannelLink()  != null) wChannelLink.setText(input.getChannelLink());
		if (input.getChannelPubDate()  != null) wChannelPubDate.setText(input.getChannelPubDate());
		
		if (input.getChannelCopyright()  != null) wChannelCopyright.setText(input.getChannelCopyright());
		if (input.getChannelImageTitle()  != null) wChannelImageTitle.setText(input.getChannelImageTitle());
		if (input.getChannelImageLink()  != null) wChannelImageLink.setText(input.getChannelImageLink());
		if (input.getChannelImageUrl()  != null) wChannelImageUrl.setText(input.getChannelImageUrl());
		if (input.getChannelImageDescription()  != null) wChannelImageDescription.setText(input.getChannelImageDescription());
		
		if (input.getChannelLanguage()  != null) wChannelLanguage.setText(input.getChannelLanguage());
		if (input.getChannelAuthor()  != null) wChannelAuthor.setText(input.getChannelAuthor());
		if (input.getChannelTitle()  != null) wChannelTitle.setText(input.getChannelTitle());
		wAddImage.setSelection(input.AddImage());
		
		// items ...
		if (input.getItemTitle()  != null) wItemTitle.setText(input.getItemTitle());
		if (input.getItemDescription()  != null) wItemDescription.setText(input.getItemDescription());
		if (input.getItemLink()  != null) wItemLink.setText(input.getItemLink());
		if (input.getItemPubDate()  != null) wItemPubDate.setText(input.getItemPubDate());
		if (input.getItemAuthor()  != null) wItemAuthor.setText(input.getItemAuthor());
		
		// GeoRSS
		wAddGeoRSS.setSelection(input.AddGeoRSS());
		wuseGeoRSSGML.setSelection(input.useGeoRSSGML());
		if (input.getGeoPointLat()  != null) wGeoPointLat.setText(input.getGeoPointLat());
		if (input.getGeoPointLong()  != null) wGeoPointLong.setText(input.getGeoPointLong());
		
		if (input.getFileName()  != null) wFilename.setText(input.getFileName());
		if (input.getFileNameField()  != null) wFieldFilename.setText(input.getFileNameField());

		wFileNameInField.setSelection(input.isFilenameInField());
		wCreateParentFolder.setSelection(input.isCreateParentFolder());
		if (input.getExtension() != null) 
		{
			wExtension.setText(input.getExtension());
		}
		else
		{
			wExtension.setText("xml");
		}

		wAddDate.setSelection(input.isDateInFilename());
		wAddTime.setSelection(input.isTimeInFilename());
		wAddStepnr.setSelection(input.isStepNrInFilename());
        
        wAddToResult.setSelection( input.AddToResult() );
        wCustomRss.setSelection( input.isCustomRss() );
        wDisplayItem.setSelection( input.isDisplayItem() );
        
        
    	if (input.getChannelCustomFields()!=null)
    	{
    		for (int i=0;i<input.getChannelCustomFields().length;i++)
    		{
    			TableItem item = wChannelCustom.table.getItem(i);
    			if (input.getChannelCustomTags()[i]!=null) item.setText(1, input.getChannelCustomTags()[i]);
    			if (input.getChannelCustomFields()[i]!=null)  item.setText(2, input.getChannelCustomFields()[i]);	
    		}
    	}
		wChannelCustom.setRowNums();
		wChannelCustom.optWidth(true);
		
		
    	if (input.getItemCustomFields()!=null)
    	{
    		for (int i=0;i<input.getItemCustomFields().length;i++)
    		{
    			TableItem item = wItemCustom.table.getItem(i);
    			if (input.getItemCustomTags()[i]!=null) item.setText(1, input.getItemCustomTags()[i]);
    			if (input.getItemCustomFields()[i]!=null)  item.setText(2, input.getItemCustomFields()[i]);	
    		}
    	}
		wItemCustom.setRowNums();
		wItemCustom.optWidth(true);
		
		
		if (input.getNameSpaces()!=null)
    	{
    		for (int i=0;i<input.getNameSpaces().length;i++)
    		{
    			TableItem item = wNameSpaceCustom.table.getItem(i);
    			if (input.getNameSpacesTitle()[i]!=null) item.setText(1, input.getNameSpacesTitle()[i]);
    			if (input.getNameSpaces()[i]!=null) item.setText(2, input.getNameSpaces()[i]);
    		}
    	}
		wNameSpaceCustom.setRowNums();
		wNameSpaceCustom.optWidth(true);
		
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		dispose();
	}
	
	private void getInfo(RssOutputMeta info)
	{

		info.setChannelTitle(wChannelTitle.getText() );
		info.setChannelDescription(wChannelDescription.getText() );
		info.setChannelLink(wChannelLink.getText() );
		info.setChannelPubDate(wChannelPubDate.getText() );
		info.setChannelCopyright(wChannelCopyright.getText() );
		info.setChannelAuthor(wChannelAuthor.getText() );
		
		// Channel image
		info.setAddImage(wAddImage.getSelection() );
		info.setChannelImageTitle(wChannelImageTitle.getText() );
		info.setChannelImageLink(wChannelImageLink.getText() );
		info.setChannelImageUrl(wChannelImageUrl.getText() );
		info.setChannelImageDescription(wChannelImageDescription.getText() );
		
		info.setChannelLanguage(wChannelLanguage.getText() );
		
		info.setVersion(wVersion.getText() );
		info.setEncoding(wEncoding.getText() );
		
		// Items ...
		info.setItemTitle(wItemTitle.getText() );
		info.setItemDescription(wItemDescription.getText() );
		info.setItemLink(wItemLink.getText() );
		info.setItemPubDate(wItemPubDate.getText() );
		info.setItemAuthor(wItemAuthor.getText() );
		info.setAddGeoRSS(wAddGeoRSS.getSelection());
		info.setUseGeoRSSGML(wuseGeoRSSGML.getSelection());
		info.setGeoPointLat(wGeoPointLat.getText() );
		info.setGeoPointLong(wGeoPointLong.getText() );
		
		info.setCreateParentFolder(wCreateParentFolder.getSelection() );
		info.setFilenameInField(wFileNameInField.getSelection() );
		
		info.setFileNameField(   wFieldFilename.getText() );
		info.setFileName(   wFilename.getText() );
		info.setExtension(  wExtension.getText() );
		info.setStepNrInFilename( wAddStepnr.getSelection() );
		info.setDateInFilename( wAddDate.getSelection() );
		info.setTimeInFilename( wAddTime.getSelection() );
		
		info.setAddToResult( wAddToResult.getSelection() );
		info.setCustomRss( wCustomRss.getSelection() );
		info.setDisplayItem( wDisplayItem.getSelection() );
		
		
				
		int nrchannelfields         = wChannelCustom.nrNonEmpty();
		info.allocate(nrchannelfields);
		for (int i=0;i<nrchannelfields;i++)
		{
			TableItem item = wChannelCustom.getNonEmpty(i);
			info.getChannelCustomTags()[i]  = item.getText(1);	
			info.getChannelCustomFields()[i] = item.getText(2);
		}
		
		// Custom item fields
		int nritemfields         = wItemCustom.nrNonEmpty();
		info.allocateitem(nritemfields);
		for (int i=0;i<nritemfields;i++)
		{
			TableItem item = wItemCustom.getNonEmpty(i);
			info.getItemCustomTags()[i]  = item.getText(1);	
			info.getItemCustomFields()[i] = item.getText(2);
		}
		
		// Add Namespaces ?
		int nrnamespaces  = wNameSpaceCustom.nrNonEmpty();
		info.allocatenamespace(nrnamespaces);
		for (int i=0;i<nrnamespaces;i++)
		{
			TableItem item = wNameSpaceCustom.getNonEmpty(i);
			info.getNameSpacesTitle()[i]  = item.getText(1);	
			info.getNameSpaces()[i]  = item.getText(2);	
		}
	}
	
	private void ok()
	{
		stepname = wStepname.getText(); // return value
		getInfo(input);
		dispose();
	}
}