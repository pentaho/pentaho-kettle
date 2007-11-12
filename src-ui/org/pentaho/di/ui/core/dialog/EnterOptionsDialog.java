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

 
/*
 * Created on 15-dec-2003
 *
 */

package org.pentaho.di.ui.core.dialog;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.Messages;



/**
 * Allows you to set the configurable options for the Kettle environment
 * 
 * @author Matt
 * @since 15-12-2003
 */
public class EnterOptionsDialog extends Dialog
{
	private Display      display; 

	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;

	private CTabItem     wLookTab, wGeneralTab;

    private ScrolledComposite sLookComp, sGeneralComp;
	private Composite    wLookComp, wGeneralComp;
	private FormData     fdLookComp, fdGeneralComp;

	private FontData     fixedFontData, graphFontData, noteFontData;
	private Font         fixedFont, graphFont, noteFont;

	private RGB          backgroundRGB, graphColorRGB, tabColorRGB;	
	private Color        background, graphColor, tabColor;

	private Label        wlFFont;
	private Canvas       wFFont;
	private Button       wbFFont, wdFFont;

	private Label        wlGFont;
	private Canvas       wGFont;
	private Button       wbGFont, wdGFont;

	private Label        wlNFont;
	private Canvas       wNFont;
	private Button       wbNFont, wdNFont;

	private Label        wlBGColor;
	private Canvas       wBGColor;
	private Button       wbBGColor, wdBGcolor;

	private Label        wlGrColor;
	private Canvas       wGrColor;
	private Button       wbGrColor, wdGrColor;

	private Label        wlTabColor;
	private Canvas       wTabColor;
	private Button       wbTabColor, wdTabColor;

	private Label        wlIconsize;
	private Text         wIconsize;

	private Label        wlLineWidth;
	private Text         wLineWidth;

	private Label        wlShadowSize;
	private Text         wShadowSize;
		
	private Label        wlMaxUndo;
	private Text         wMaxUndo;

    private Label        wlDefaultPreview;
    private Text         wDefaultPreview;

    private Label        wlMaxNrLogLines;
    private Text         wMaxNrLogLines;

	private Label        wlMiddlePct;
	private Text         wMiddlePct;
    
    private Label        wlAntiAlias;
    private Button       wAntiAlias;

    private Label        wlOriginalLook;
    private Button       wOriginalLook;

    private Label        wlBranding;
    private Button       wBranding;

	private Label        wlShowTips;
	private Button       wShowTips;

    private Label        wlShowWelcome;
    private Button       wShowWelcome;

	private Label        wlUseCache;
	private Button       wUseCache;

	private Label        wlOpenLast;
	private Button       wOpenLast;
		
	private Label        wlAutoSave;
	private Button       wAutoSave;

    private Label        wlOnlyActiveFile;
    private Button       wOnlyActiveFile;

    private Label        wlDBConnXML;
    private Button       wDBConnXML;

    private Label        wlAskReplaceDB;
    private Button       wAskReplaceDB;

    private Label        wlReplaceDB;
    private Button       wReplaceDB;

	private Label        wlSaveConf;
	private Button       wSaveConf;

	private Label        wlAutoSplit;
	private Button       wAutoSplit;
    
    private Label        wlCopyDistrib;
    private Button       wCopyDistrib;

    private Label        wlShowRep;
    private Button       wShowRep;

    private Label        wlExitWarning;
    private Button       wExitWarning;

    private Label        wlClearCustom;
    private Button       wClearCustom;

    private Label        wlDefaultLocale;
    private Combo        wDefaultLocale;

    private Label        wlFailoverLocale;
    private Combo        wFailoverLocale;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	private PropsUI props;

    private int middle;

    private int margin;

	private Button tooltipBtn;

    /**
     * @deprecated Use CT without <i>props</i> parameter instead
     */
	public EnterOptionsDialog(Shell parent, PropsUI props)
	{
		super(parent, SWT.NONE);
		this.props = props;
	}

    public EnterOptionsDialog(Shell parent)
    {
        super(parent, SWT.NONE);
        props = PropsUI.getInstance();
    }
    
	public Props open()
	{
		Shell parent = getParent();
		display = parent.getDisplay();

		getData();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE );
        props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageLogoSmall());
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("EnterOptionsDialog.Title"));
		
		middle = props.getMiddlePct();
		margin = Const.MARGIN;
		
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
        wTabFolder.setSimple(false);
        
        addGeneralTab();
        addLookTab();

		// Some buttons
		wOK=new Button(shell, SWT.PUSH );
		wOK.setText(Messages.getString("System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(0, 0);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(wOK, -margin);
		wTabFolder.setLayoutData(fdTabFolder);

		/////////////////////////////////////////////////////////////
		/// END OF TABS
		/////////////////////////////////////////////////////////////


		
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
		wCancel.addListener(SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wIconsize.addSelectionListener  (lsDef);
		wLineWidth.addSelectionListener (lsDef);
		wShadowSize.addSelectionListener(lsDef);
		wMaxUndo.addSelectionListener   (lsDef);
		wMiddlePct.addSelectionListener (lsDef);
        wDefaultPreview.addSelectionListener (lsDef);
		
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wTabFolder.setSelection(0);
		
		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return props;
	}

	private void addLookTab()
    {
        int h = 40;

        //////////////////////////
        // START OF LOOK TAB///
        ///
        wLookTab=new CTabItem(wTabFolder, SWT.NONE);
        wLookTab.setText(Messages.getString("EnterOptionsDialog.LookAndFeel.Label"));
        
        sLookComp = new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
        sLookComp.setLayout(new FillLayout());
        
        wLookComp = new Composite(sLookComp, SWT.NONE);
        props.setLook(wLookComp);
        
        FormLayout lookLayout = new FormLayout();
        lookLayout.marginWidth  = 3;
        lookLayout.marginHeight = 3;
        wLookComp.setLayout(lookLayout);

        
        // Fixed font
        int nr = 0;
        wlFFont=new Label(wLookComp, SWT.RIGHT);
        wlFFont.setText(Messages.getString("EnterOptionsDialog.FixedWidthFont.Label"));
        props.setLook(wlFFont);
        FormData fdlFFont = new FormData();
        fdlFFont.left  = new FormAttachment(0, 0);
        fdlFFont.right = new FormAttachment(middle, -margin);
        fdlFFont.top   = new FormAttachment(0, nr*h + margin+10);
        wlFFont.setLayoutData(fdlFFont);

        wdFFont = new Button(wLookComp, SWT.PUSH | SWT.BORDER);
        props.setLook(wdFFont);

        FormData fddFFont = layoutResetOptionButton(wdFFont);
        fddFFont.right= new FormAttachment(100, 0);
        fddFFont.top  = new FormAttachment(0, nr*h + margin);
        fddFFont.bottom  = new FormAttachment(0, (nr+1)*h + margin);
        wdFFont.setLayoutData(fddFFont);
        wdFFont.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent arg0) 
                {
                    fixedFontData = new FontData(ConstUI.FONT_FIXED_NAME, ConstUI.FONT_FIXED_SIZE, ConstUI.FONT_FIXED_TYPE);
                    fixedFont.dispose();
                    fixedFont = new Font(display, fixedFontData);
                    wFFont.redraw();
                }
            }
        );

        wbFFont = new Button(wLookComp, SWT.PUSH | SWT.BORDER);
        props.setLook(wbFFont);

        FormData fdbFFont = layoutEditOptionButton(wbFFont);
        fdbFFont.right= new FormAttachment(wdFFont, -margin);
        fdbFFont.top  = new FormAttachment(0, nr*h + margin);
        fdbFFont.bottom  = new FormAttachment(0, (nr+1)*h + margin);
        wbFFont.setLayoutData(fdbFFont);
        wbFFont.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent arg0) 
                {
                    FontDialog fd = new FontDialog(shell);
                    fd.setFontList(new FontData[] { fixedFontData });
                    FontData newfd = fd.open();
                    if (newfd!=null)
                    {
                        fixedFontData = newfd;
                        fixedFont.dispose();
                        fixedFont = new Font(display, fixedFontData);
                        wFFont.redraw();
                    }
                }
            }
        );

        wFFont = new Canvas(wLookComp, SWT.BORDER );
        props.setLook(wFFont);
        FormData fdFFont = new FormData();
        fdFFont.left   = new FormAttachment(middle, 0);
        fdFFont.right  = new FormAttachment(wbFFont, -margin);
        fdFFont.top    = new FormAttachment(0, margin);
        fdFFont.bottom = new FormAttachment(0, h    );
        wFFont.setLayoutData(fdFFont);
        wFFont.addPaintListener(new PaintListener() 
            {
                public void paintControl(PaintEvent pe) 
                {
                    pe.gc.setFont(fixedFont);
                    Rectangle max = wFFont.getBounds();
                    String name = fixedFontData.getName()+" - "+fixedFontData.getHeight(); //$NON-NLS-1$
                    Point size = pe.gc.textExtent(name);
                    
                    pe.gc.drawText(name, (max.width-size.x)/2, (max.height-size.y)/2, true );
                }
            }
        );

        // Graph font
        nr++;
        wlGFont=new Label(wLookComp, SWT.RIGHT);
        wlGFont.setText(Messages.getString("EnterOptionsDialog.GraphFont.Label"));
        props.setLook(wlGFont);
        FormData fdlGFont = new FormData();
        fdlGFont.left  = new FormAttachment(0, 0);
        fdlGFont.right = new FormAttachment(middle, -margin);
        fdlGFont.top   = new FormAttachment(0, nr*h+margin+10);
        wlGFont.setLayoutData(fdlGFont);

        wdGFont = new Button(wLookComp, SWT.PUSH | SWT.BORDER);
        props.setLook(wdGFont);

        FormData fddGFont = layoutResetOptionButton(wdGFont);
        fddGFont.right= new FormAttachment(100, 0);
        fddGFont.top  = new FormAttachment(0, nr*h+margin);
        fddGFont.bottom = new FormAttachment(0, (nr+1)*h+margin);
        wdGFont.setLayoutData(fddGFont);
        wdGFont.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent arg0) 
                {
                    graphFont.dispose();
                    
                    graphFontData = props.getDefaultFontData();
                    graphFont = new Font(display, graphFontData);
                    wGFont.redraw();
                }
            }
        );

        wbGFont = new Button(wLookComp, SWT.PUSH | SWT.BORDER);
        props.setLook(wbGFont);

        FormData fdbGFont = layoutEditOptionButton(wbGFont);
        fdbGFont.right= new FormAttachment(wdGFont, -margin);
        fdbGFont.top  = new FormAttachment(0, nr*h+margin);
        fdbGFont.bottom = new FormAttachment(0, (nr+1)*h+margin);
        wbGFont.setLayoutData(fdbGFont);
        wbGFont.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent arg0) 
                {
                    FontDialog fd = new FontDialog(shell);
                    fd.setFontList(new FontData[] { graphFontData });
                    FontData newfd = fd.open();
                    if (newfd!=null)
                    {
                        graphFontData = newfd;
                        graphFont.dispose();
                        graphFont = new Font(display, graphFontData);
                        wGFont.redraw();
                    }
                }
            }
        );

        wGFont = new Canvas(wLookComp, SWT.BORDER );
        props.setLook(wGFont);
        FormData fdGFont = new FormData();
        fdGFont.left   = new FormAttachment(middle, 0);
        fdGFont.right  = new FormAttachment(wbGFont, -margin);
        fdGFont.top    = new FormAttachment(0, nr*h+margin);
        fdGFont.bottom = new FormAttachment(0, (nr+1)*h+margin );
        wGFont.setLayoutData(fdGFont);
        wGFont.addPaintListener(new PaintListener() 
            {
                public void paintControl(PaintEvent pe) 
                {
                    pe.gc.setFont(graphFont);
                    Rectangle max = wGFont.getBounds();
                    String name = graphFontData.getName()+" - "+graphFontData.getHeight(); //$NON-NLS-1$
                    Point size = pe.gc.textExtent(name);
                    
                    pe.gc.drawText(name, (max.width-size.x)/2, (max.height-size.y)/2, true );
                }
            }
        );
        
        

        // Note font
        nr++;
        wlNFont = new Label(wLookComp, SWT.RIGHT);
        wlNFont.setText(Messages.getString("EnterOptionsDialog.NoteFont.Label"));
        props.setLook(wlNFont);
        FormData fdlNFont = new FormData();
        fdlNFont.left  = new FormAttachment(0, 0);
        fdlNFont.right = new FormAttachment(middle, -margin);
        fdlNFont.top   = new FormAttachment(0, nr*h + margin + 10);
        wlNFont.setLayoutData(fdlNFont);

        wdNFont = new Button(wLookComp, SWT.PUSH | SWT.BORDER);
        props.setLook(wdNFont);

        FormData fddNFont = layoutResetOptionButton(wdNFont);
        fddNFont.right = new FormAttachment(100, 0);
        fddNFont.top = new FormAttachment(0, nr*h + margin);
        fddNFont.bottom = new FormAttachment(0, (nr+1)*h + margin);
        wdNFont.setLayoutData(fddNFont);
        wdNFont.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent arg0) 
                {
                    noteFontData = props.getDefaultFontData();
                    noteFont.dispose();
                    noteFont = new Font(display, noteFontData);
                    wNFont.redraw();
                }
            }
        );

        wbNFont = new Button(wLookComp, SWT.PUSH | SWT.BORDER);
        props.setLook(wbNFont);

        FormData fdbNFont = layoutEditOptionButton(wbNFont);
        fdbNFont.right = new FormAttachment(wdNFont, -margin);
        fdbNFont.top = new FormAttachment(0, nr*h + margin);
        fdbNFont.bottom = new FormAttachment(0, (nr+1)*h + margin);
        wbNFont.setLayoutData(fdbNFont);
        wbNFont.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent arg0) 
                {
                    FontDialog fd = new FontDialog(shell);
                    fd.setFontList(new FontData[] { noteFontData });
                    FontData newfd = fd.open();
                    if (newfd != null) 
                    {
                        noteFontData = newfd;
                        noteFont.dispose();
                        noteFont = new Font(display, noteFontData);
                        wNFont.redraw();
                    }
                }
            }
        );

        wNFont = new Canvas(wLookComp, SWT.BORDER);
        props.setLook(wNFont);
        FormData fdNFont = new FormData();
        fdNFont.left = new FormAttachment(middle, 0);
        fdNFont.right = new FormAttachment(wbNFont, -margin);
        fdNFont.top    = new FormAttachment(0, nr*h + margin);
        fdNFont.bottom = new FormAttachment(0, (nr+1)*h + margin);
        wNFont.setLayoutData(fdNFont);
        wNFont.addPaintListener(new PaintListener() 
        {
                public void paintControl(PaintEvent pe) 
                {
                    pe.gc.setFont(noteFont);
                    Rectangle max = wNFont.getBounds();
                    String name = noteFontData.getName()+" - "+noteFontData.getHeight(); //$NON-NLS-1$
                    Point size = pe.gc.textExtent(name);
    
                    pe.gc.drawText(name, (max.width - size.x) / 2, (max.height - size.y) / 2, true);
                }
            }
        );


        // Background color
        nr++;
        wlBGColor = new Label(wLookComp, SWT.RIGHT);
        wlBGColor.setText(Messages.getString("EnterOptionsDialog.BackgroundColor.Label"));
        props.setLook(wlBGColor);
        FormData fdlBGColor = new FormData();
        fdlBGColor.left = new FormAttachment(0, 0);
        fdlBGColor.right = new FormAttachment(middle, -margin);
        fdlBGColor.top = new FormAttachment(0, nr*h + margin + 10);
        wlBGColor.setLayoutData(fdlBGColor);

        wdBGcolor = new Button(wLookComp, SWT.PUSH | SWT.BORDER);
        props.setLook(wdBGcolor);

        FormData fddBGColor = layoutResetOptionButton(wdBGcolor);
        fddBGColor.right = new FormAttachment(100, 0);  // to the right of the dialog
        fddBGColor.top = new FormAttachment(0, nr*h + margin);
        fddBGColor.bottom = new FormAttachment(0, (nr+1)*h + margin);
        wdBGcolor.setLayoutData(fddBGColor);
        wdBGcolor.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent arg0) 
                {
                    background.dispose();
    
                    backgroundRGB = new RGB(ConstUI.COLOR_BACKGROUND_RED, ConstUI.COLOR_BACKGROUND_GREEN, ConstUI.COLOR_BACKGROUND_BLUE);
                    background=new Color(display, backgroundRGB);
                    wBGColor.setBackground(background);
                    wBGColor.redraw();
                }
            }
        );

        wbBGColor = new Button(wLookComp, SWT.PUSH | SWT.BORDER);
        props.setLook(wbBGColor);

        FormData fdbBGColor = layoutEditOptionButton(wbBGColor);
        fdbBGColor.right = new FormAttachment(wdBGcolor, -margin); // to the left of the "default" button
        fdbBGColor.top = new FormAttachment(0, nr*h + margin);
        fdbBGColor.bottom = new FormAttachment(0, (nr+1)*h + margin);
        wbBGColor.setLayoutData(fdbBGColor);
        wbBGColor.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent arg0) 
                {
                    ColorDialog cd = new ColorDialog(shell);
                    cd.setRGB(props.getBackgroundRGB());
                    RGB newbg = cd.open();
                    if (newbg != null) 
                    {
                        backgroundRGB = newbg;
                        background.dispose();
                        background=new Color(display, backgroundRGB);
                        wBGColor.setBackground(background);
                        wBGColor.redraw();
                    }
                }
            }
        );

        wBGColor = new Canvas(wLookComp, SWT.BORDER);
        props.setLook(wBGColor);
        wBGColor.setBackground(background);
        FormData fdBGColor = new FormData();
        fdBGColor.left = new FormAttachment(middle, 0);
        fdBGColor.right = new FormAttachment(wbBGColor, -margin);
        fdBGColor.top    = new FormAttachment(0, nr*h + margin);
        fdBGColor.bottom = new FormAttachment(0, (nr+1)*h + margin);
        wBGColor.setLayoutData(fdBGColor);

        // Graph background color
        nr++;
        wlGrColor = new Label(wLookComp, SWT.RIGHT);
        wlGrColor.setText(Messages.getString("EnterOptionsDialog.BackgroundColorGraph.Label"));
        props.setLook(wlGrColor);
        FormData fdlGrColor = new FormData();
        fdlGrColor.left = new FormAttachment(0, 0);
        fdlGrColor.right = new FormAttachment(middle, -margin);
        fdlGrColor.top = new FormAttachment(0, nr*h + margin + 10);
        wlGrColor.setLayoutData(fdlGrColor);

        wdGrColor = new Button(wLookComp, SWT.PUSH | SWT.BORDER);
        props.setLook(wdGrColor);

        FormData fddGrColor = layoutResetOptionButton(wdGrColor);
        fddGrColor.right = new FormAttachment(100, 0);
        fddGrColor.top = new FormAttachment(0, nr*h + margin);
        fddGrColor.bottom = new FormAttachment(0, (nr+1)*h + margin);
        wdGrColor.setLayoutData(fddGrColor);
        wdGrColor.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent arg0) 
                {
                    graphColor.dispose();

                    graphColorRGB = new RGB(ConstUI.COLOR_GRAPH_RED, ConstUI.COLOR_GRAPH_GREEN, ConstUI.COLOR_GRAPH_BLUE);
                    graphColor=new Color(display, graphColorRGB);
                    wGrColor.setBackground(graphColor);
                    wGrColor.redraw();
                }
            }
        );

        wbGrColor = new Button(wLookComp, SWT.PUSH | SWT.BORDER);
        props.setLook(wbGrColor);

        FormData fdbGrColor = layoutEditOptionButton(wbGrColor);
        fdbGrColor.right = new FormAttachment(wdGrColor, -margin);
        fdbGrColor.top = new FormAttachment(0, nr*h + margin);
        fdbGrColor.bottom = new FormAttachment(0, (nr+1)*h + margin);
        wbGrColor.setLayoutData(fdbGrColor);
        wbGrColor.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent arg0) 
                {
                    ColorDialog cd = new ColorDialog(shell);
                    cd.setRGB(props.getGraphColorRGB());
                    RGB newbg = cd.open();
                    if (newbg != null) 
                    {
                        graphColorRGB = newbg;
                        graphColor.dispose();
                        graphColor=new Color(display, graphColorRGB);
                        wGrColor.setBackground(graphColor);
                        wGrColor.redraw();
                    }
                }
            }
        );

        wGrColor = new Canvas(wLookComp, SWT.BORDER);
        props.setLook(wGrColor);
        wGrColor.setBackground(graphColor);
        FormData fdGrColor = new FormData();
        fdGrColor.left = new FormAttachment(middle, 0);
        fdGrColor.right = new FormAttachment(wbGrColor, -margin);
        fdGrColor.top    = new FormAttachment(0, nr*h + margin);
        fdGrColor.bottom = new FormAttachment(0, (nr+1)*h + margin);
        wGrColor.setLayoutData(fdGrColor);

        // Tab selected color
        nr++;
        wlTabColor = new Label(wLookComp, SWT.RIGHT);
        wlTabColor.setText(Messages.getString("EnterOptionsDialog.TabColor.Label"));
        props.setLook(wlTabColor);
        FormData fdlTabColor = new FormData();
        fdlTabColor.left = new FormAttachment(0, 0);
        fdlTabColor.right = new FormAttachment(middle, -margin);
        fdlTabColor.top = new FormAttachment(0, nr*h + margin + 10);
        wlTabColor.setLayoutData(fdlTabColor);

        wdTabColor = new Button(wLookComp, SWT.PUSH | SWT.BORDER | SWT.CENTER);
        props.setLook(wdTabColor);

        FormData fddTabColor = layoutResetOptionButton(wdTabColor);
        fddTabColor.right = new FormAttachment(100, 0);
        fddTabColor.top = new FormAttachment(0, nr*h + margin);
        fddTabColor.bottom = new FormAttachment(0, (nr+1)*h + margin);
        wdTabColor.setLayoutData(fddTabColor);
        wdTabColor.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent arg0) 
                {
                    tabColor.dispose();
                    
                    tabColorRGB = new RGB(ConstUI.COLOR_TAB_RED, ConstUI.COLOR_TAB_GREEN, ConstUI.COLOR_TAB_BLUE);
                    tabColor=new Color(display, tabColorRGB);
                    wTabColor.setBackground(tabColor);
                    wTabColor.redraw();
                }
            }
        );

        wbTabColor = new Button(wLookComp, SWT.PUSH | SWT.BORDER);
        props.setLook(wbTabColor);

        FormData fdbTabColor = layoutEditOptionButton(wbTabColor);
        fdbTabColor.right = new FormAttachment(wdTabColor, -margin);
        fdbTabColor.top = new FormAttachment(0, nr*h + margin);
        fdbTabColor.bottom = new FormAttachment(0, (nr+1)*h + margin);
        wbTabColor.setLayoutData(fdbTabColor);
        wbTabColor.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent arg0) 
                {
                    ColorDialog cd = new ColorDialog(shell);
                    cd.setRGB(props.getTabColorRGB());
                    RGB newbg = cd.open();
                    if (newbg != null) 
                    {
                        tabColorRGB = newbg;
                        tabColor.dispose();
                        tabColor=new Color(display, tabColorRGB);
                        wTabColor.setBackground(tabColor);
                        wTabColor.redraw();
                    }
                }
            }
        );

        wTabColor = new Canvas(wLookComp, SWT.BORDER);
        props.setLook(wTabColor);
        wTabColor.setBackground(tabColor);
        FormData fdTabColor = new FormData();
        fdTabColor.left = new FormAttachment(middle, 0);
        fdTabColor.right = new FormAttachment(wbTabColor, -margin);
        fdTabColor.top    = new FormAttachment(0, nr*h + margin);
        fdTabColor.bottom = new FormAttachment(0, (nr+1)*h + margin);
        wTabColor.setLayoutData(fdTabColor);

        // Iconsize line
        wlIconsize=new Label(wLookComp, SWT.RIGHT);
        wlIconsize.setText(Messages.getString("EnterOptionsDialog.IconSize.Label"));
        props.setLook(wlIconsize);
        FormData fdlIconsize = new FormData();
        fdlIconsize.left = new FormAttachment(0, 0);
        fdlIconsize.right= new FormAttachment(middle, -margin);
        fdlIconsize.top  = new FormAttachment(wTabColor, margin);
        wlIconsize.setLayoutData(fdlIconsize);
        wIconsize=new Text(wLookComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wIconsize.setText(Integer.toString(props.getIconSize()));
        props.setLook(wIconsize);
        FormData fdIconsize = new FormData();
        fdIconsize.left = new FormAttachment(middle, 0);
        fdIconsize.right= new FormAttachment(100, -margin);
        fdIconsize.top  = new FormAttachment(wTabColor, margin);
        wIconsize.setLayoutData(fdIconsize);

        // LineWidth line
        wlLineWidth=new Label(wLookComp, SWT.RIGHT);
        wlLineWidth.setText(Messages.getString("EnterOptionsDialog.LineWidth.Label"));
        props.setLook(wlLineWidth);
        FormData fdlLineWidth = new FormData();
        fdlLineWidth.left = new FormAttachment(0, 0);
        fdlLineWidth.right= new FormAttachment(middle, -margin);
        fdlLineWidth.top  = new FormAttachment(wIconsize, margin);
        wlLineWidth.setLayoutData(fdlLineWidth);
        wLineWidth=new Text(wLookComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wLineWidth.setText(Integer.toString(props.getLineWidth()));
        props.setLook(wLineWidth);
        FormData fdLineWidth = new FormData();
        fdLineWidth.left = new FormAttachment(middle, 0);
        fdLineWidth.right= new FormAttachment(100, -margin);
        fdLineWidth.top  = new FormAttachment(wIconsize, margin);
        wLineWidth.setLayoutData(fdLineWidth);

        // ShadowSize line
        wlShadowSize=new Label(wLookComp, SWT.RIGHT);
        wlShadowSize.setText(Messages.getString("EnterOptionsDialog.ShadowSize.Label"));
        props.setLook(wlShadowSize);
        FormData fdlShadowSize = new FormData();
        fdlShadowSize.left = new FormAttachment(0, 0);
        fdlShadowSize.right= new FormAttachment(middle, -margin);
        fdlShadowSize.top  = new FormAttachment(wLineWidth, margin);
        wlShadowSize.setLayoutData(fdlShadowSize);
        wShadowSize=new Text(wLookComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wShadowSize.setText(Integer.toString(props.getShadowSize()));
        props.setLook(wShadowSize);
        FormData fdShadowSize = new FormData();
        fdShadowSize.left = new FormAttachment(middle, 0);
        fdShadowSize.right= new FormAttachment(100, -margin);
        fdShadowSize.top  = new FormAttachment(wLineWidth, margin);
        wShadowSize.setLayoutData(fdShadowSize);

        // MiddlePct line
        wlMiddlePct=new Label(wLookComp, SWT.RIGHT);
        wlMiddlePct.setText(Messages.getString("EnterOptionsDialog.DialogMiddlePercentage.Label"));
        props.setLook(wlMiddlePct);
        FormData fdlMiddlePct = new FormData();
        fdlMiddlePct.left = new FormAttachment(0, 0);
        fdlMiddlePct.right= new FormAttachment(middle, -margin);
        fdlMiddlePct.top  = new FormAttachment(wShadowSize, margin);
        wlMiddlePct.setLayoutData(fdlMiddlePct);
        wMiddlePct=new Text(wLookComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wMiddlePct.setText(Integer.toString(props.getMiddlePct()));
        props.setLook(wMiddlePct);
        FormData fdMiddlePct = new FormData();
        fdMiddlePct.left = new FormAttachment(middle, 0);
        fdMiddlePct.right= new FormAttachment(100, -margin);
        fdMiddlePct.top  = new FormAttachment(wShadowSize, margin);
        wMiddlePct.setLayoutData(fdMiddlePct);
        
        
        // Enable anti-aliasing
        wlAntiAlias=new Label(wLookComp, SWT.RIGHT);
        wlAntiAlias.setText(Messages.getString("EnterOptionsDialog.CanvasAntiAliasing.Label"));
        props.setLook(wlAntiAlias);
        FormData fdlAntiAlias = new FormData();
        fdlAntiAlias.left = new FormAttachment(0, 0);
        fdlAntiAlias.top  = new FormAttachment(wMiddlePct, margin);
        fdlAntiAlias.right= new FormAttachment(middle, -margin);
        wlAntiAlias.setLayoutData(fdlAntiAlias);
        wAntiAlias=new Button(wLookComp, SWT.CHECK);
        props.setLook(wAntiAlias);
        wAntiAlias.setSelection(props.isAntiAliasingEnabled());
        FormData fdAntiAlias = new FormData();
        fdAntiAlias.left = new FormAttachment(middle, 0);
        fdAntiAlias.top  = new FormAttachment(wMiddlePct, margin);
        fdAntiAlias.right= new FormAttachment(100, 0);
        wAntiAlias.setLayoutData(fdAntiAlias);

        // Show original look
        wlOriginalLook=new Label(wLookComp, SWT.RIGHT);
        wlOriginalLook.setText(Messages.getString("EnterOptionsDialog.UseOSLook.Label"));
        props.setLook(wlOriginalLook);
        FormData fdlOriginalLook = new FormData();
        fdlOriginalLook.left = new FormAttachment(0, 0);
        fdlOriginalLook.top  = new FormAttachment(wAntiAlias, margin);
        fdlOriginalLook.right= new FormAttachment(middle, -margin);
        wlOriginalLook.setLayoutData(fdlOriginalLook);
        wOriginalLook=new Button(wLookComp, SWT.CHECK);
        props.setLook(wOriginalLook);
        wOriginalLook.setSelection(props.isOSLookShown());
        FormData fdOriginalLook = new FormData();
        fdOriginalLook.left = new FormAttachment(middle, 0);
        fdOriginalLook.top  = new FormAttachment(wAntiAlias, margin);
        fdOriginalLook.right= new FormAttachment(100, 0);
        wOriginalLook.setLayoutData(fdOriginalLook);

        // Show branding graphics
        wlBranding=new Label(wLookComp, SWT.RIGHT);
        wlBranding.setText(Messages.getString("EnterOptionsDialog.Branding.Label"));
        props.setLook(wlBranding);
        FormData fdlBranding = new FormData();
        fdlBranding.left = new FormAttachment(0, 0);
        fdlBranding.top  = new FormAttachment(wOriginalLook, margin);
        fdlBranding.right= new FormAttachment(middle, -margin);
        wlBranding.setLayoutData(fdlBranding);
        wBranding=new Button(wLookComp, SWT.CHECK);
        props.setLook(wBranding);
        wBranding.setSelection(props.isBrandingActive());
        FormData fdBranding = new FormData();
        fdBranding.left = new FormAttachment(middle, 0);
        fdBranding.top  = new FormAttachment(wOriginalLook, margin);
        fdBranding.right= new FormAttachment(100, 0);
        wBranding.setLayoutData(fdBranding);

        // DefaultLocale line
        wlDefaultLocale=new Label(wLookComp, SWT.RIGHT);
        wlDefaultLocale.setText(Messages.getString("EnterOptionsDialog.DefaultLocale.Label"));
        props.setLook(wlDefaultLocale);
        FormData fdlDefaultLocale = new FormData();
        fdlDefaultLocale.left = new FormAttachment(0, 0);
        fdlDefaultLocale.right= new FormAttachment(middle, -margin);
        fdlDefaultLocale.top  = new FormAttachment(wBranding, margin);
        wlDefaultLocale.setLayoutData(fdlDefaultLocale);
        wDefaultLocale=new Combo(wLookComp, SWT.SINGLE | SWT.READ_ONLY | SWT.LEFT | SWT.BORDER);
        wDefaultLocale.setItems(GlobalMessages.localeDescr);
        props.setLook(wDefaultLocale);
        FormData fdDefaultLocale = new FormData();
        fdDefaultLocale.left = new FormAttachment(middle, 0);
        fdDefaultLocale.right= new FormAttachment(100, -margin);
        fdDefaultLocale.top  = new FormAttachment(wBranding, margin);
        wDefaultLocale.setLayoutData(fdDefaultLocale);
        // language selections...
        int idxDefault = Const.indexOfString(LanguageChoice.getInstance().getDefaultLocale().toString(), GlobalMessages.localeCodes);
        if (idxDefault>=0) wDefaultLocale.select(idxDefault);

        // FailoverLocale line
        wlFailoverLocale=new Label(wLookComp, SWT.RIGHT);
        wlFailoverLocale.setText(Messages.getString("EnterOptionsDialog.FailoverLocale.Label"));
        props.setLook(wlFailoverLocale);
        FormData fdlFailoverLocale = new FormData();
        fdlFailoverLocale.left = new FormAttachment(0, 0);
        fdlFailoverLocale.right= new FormAttachment(middle, -margin);
        fdlFailoverLocale.top  = new FormAttachment(wDefaultLocale, margin);
        wlFailoverLocale.setLayoutData(fdlFailoverLocale);
        wFailoverLocale=new Combo(wLookComp, SWT.SINGLE | SWT.READ_ONLY | SWT.LEFT | SWT.BORDER);
        wFailoverLocale.setItems(GlobalMessages.localeDescr);
        props.setLook(wFailoverLocale);
        FormData fdFailoverLocale = new FormData();
        fdFailoverLocale.left = new FormAttachment(middle, 0);
        fdFailoverLocale.right= new FormAttachment(100, -margin);
        fdFailoverLocale.top  = new FormAttachment(wDefaultLocale, margin);
        wFailoverLocale.setLayoutData(fdFailoverLocale);
        int idxFailover = Const.indexOfString(LanguageChoice.getInstance().getFailoverLocale().toString(), GlobalMessages.localeCodes);
        if (idxFailover>=0) wFailoverLocale.select(idxFailover);


        fdLookComp=new FormData();
        fdLookComp.left  = new FormAttachment(0, 0);
        fdLookComp.right = new FormAttachment(100, 0);
        fdLookComp.top   = new FormAttachment(0, 0);
        fdLookComp.bottom= new FormAttachment(100, 100);
        wLookComp.setLayoutData(fdLookComp);
    
        wLookComp.pack();
        
        Rectangle bounds = wLookComp.getBounds();
        sLookComp.setContent(wLookComp);
        sLookComp.setExpandHorizontal(true);
        sLookComp.setExpandVertical(true);
        sLookComp.setMinWidth(bounds.width);
        sLookComp.setMinHeight(bounds.height);
        
        wLookTab.setControl(sLookComp);

        /////////////////////////////////////////////////////////////
        /// END OF LOOK TAB
        /////////////////////////////////////////////////////////////
    }

    private void addGeneralTab()
    {
        //////////////////////////
        // START OF GENERAL TAB///
        ///
        wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
        wGeneralTab.setText(Messages.getString("EnterOptionsDialog.General.Label"));

        FormLayout generalLayout = new FormLayout ();
        generalLayout.marginWidth  = 3;
        generalLayout.marginHeight = 3;
        
        sGeneralComp = new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
        sGeneralComp.setLayout(new FillLayout());
        
        wGeneralComp = new Composite(sGeneralComp, SWT.NONE);
        props.setLook(wGeneralComp);
        wGeneralComp.setLayout(generalLayout);

        // MaxUndo line
        wlMaxUndo=new Label(wGeneralComp, SWT.RIGHT);
        wlMaxUndo.setText(Messages.getString("EnterOptionsDialog.MaximumUndo.Label"));
        props.setLook(wlMaxUndo);
        FormData fdlMaxUndo = new FormData();
        fdlMaxUndo.left = new FormAttachment(0, 0);
        fdlMaxUndo.right= new FormAttachment(middle, -margin);
        fdlMaxUndo.top  = new FormAttachment(0, 0);
        wlMaxUndo.setLayoutData(fdlMaxUndo);
        wMaxUndo=new Text(wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wMaxUndo.setText(Integer.toString(props.getMaxUndo()));
        props.setLook(wMaxUndo);
        FormData fdMaxUndo = new FormData();
        fdMaxUndo.left = new FormAttachment(middle, 0);
        fdMaxUndo.right= new FormAttachment(100, -margin);
        fdMaxUndo.top  = new FormAttachment(0, 0);
        wMaxUndo.setLayoutData(fdMaxUndo);

        // Default preview size
        wlDefaultPreview=new Label(wGeneralComp, SWT.RIGHT);
        wlDefaultPreview.setText(Messages.getString("EnterOptionsDialog.DefaultPreviewSize.Label"));
        props.setLook(wlDefaultPreview);
        FormData fdlDefaultPreview = new FormData();
        fdlDefaultPreview.left = new FormAttachment(0, 0);
        fdlDefaultPreview.right= new FormAttachment(middle, -margin);
        fdlDefaultPreview.top  = new FormAttachment(wMaxUndo, margin);
        wlDefaultPreview.setLayoutData(fdlDefaultPreview);
        wDefaultPreview=new Text(wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wDefaultPreview.setText(Integer.toString(props.getDefaultPreviewSize()));
        props.setLook(wDefaultPreview);
        FormData fdDefaultPreview = new FormData();
        fdDefaultPreview.left = new FormAttachment(middle, 0);
        fdDefaultPreview.right= new FormAttachment(100, -margin);
        fdDefaultPreview.top  = new FormAttachment(wMaxUndo, margin);
        wDefaultPreview.setLayoutData(fdDefaultPreview);

        // Max Nr of log lines
        wlMaxNrLogLines=new Label(wGeneralComp, SWT.RIGHT);
        wlMaxNrLogLines.setText(Messages.getString("EnterOptionsDialog.MaxNrLogLinesSize.Label"));
        props.setLook(wlMaxNrLogLines);
        FormData fdlMaxNrLogLines = new FormData();
        fdlMaxNrLogLines.left = new FormAttachment(0, 0);
        fdlMaxNrLogLines.right= new FormAttachment(middle, -margin);
        fdlMaxNrLogLines.top  = new FormAttachment(wDefaultPreview, margin);
        wlMaxNrLogLines.setLayoutData(fdlMaxNrLogLines);
        wMaxNrLogLines=new Text(wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wMaxNrLogLines.setText(Integer.toString(props.getMaxNrLinesInLog()));
        props.setLook(wMaxNrLogLines);
        FormData fdMaxNrLogLines = new FormData();
        fdMaxNrLogLines.left = new FormAttachment(middle, 0);
        fdMaxNrLogLines.right= new FormAttachment(100, -margin);
        fdMaxNrLogLines.top  = new FormAttachment(wDefaultPreview, margin);
        wMaxNrLogLines.setLayoutData(fdMaxNrLogLines);

        // Show tips on startup?
        wlShowTips=new Label(wGeneralComp, SWT.RIGHT);
        wlShowTips.setText(Messages.getString("EnterOptionsDialog.ShowTipsStartup.Label"));
        props.setLook(wlShowTips);
        FormData fdlShowTips = new FormData();
        fdlShowTips.left = new FormAttachment(0, 0);
        fdlShowTips.top  = new FormAttachment(wMaxNrLogLines, margin);
        fdlShowTips.right= new FormAttachment(middle, -margin);
        wlShowTips.setLayoutData(fdlShowTips);
        wShowTips=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wShowTips);
        wShowTips.setSelection(props.showTips());
        FormData fdShowTips = new FormData();
        fdShowTips.left = new FormAttachment(middle, 0);
        fdShowTips.top  = new FormAttachment(wMaxNrLogLines, margin);
        fdShowTips.right= new FormAttachment(100, 0);
        wShowTips.setLayoutData(fdShowTips);

        // Show welcome page on startup?
        wlShowWelcome=new Label(wGeneralComp, SWT.RIGHT);
        wlShowWelcome.setText(Messages.getString("EnterOptionsDialog.ShowWelcomePage.Label"));
        props.setLook(wlShowWelcome);
        FormData fdlShowWelcome = new FormData();
        fdlShowWelcome.left = new FormAttachment(0, 0);
        fdlShowWelcome.top  = new FormAttachment(wShowTips, margin);
        fdlShowWelcome.right= new FormAttachment(middle, -margin);
        wlShowWelcome.setLayoutData(fdlShowWelcome);
        wShowWelcome=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wShowWelcome);
        wShowWelcome.setSelection(props.showWelcomePageOnStartup());
        FormData fdShowWelcome = new FormData();
        fdShowWelcome.left = new FormAttachment(middle, 0);
        fdShowWelcome.top  = new FormAttachment(wShowTips, margin);
        fdShowWelcome.right= new FormAttachment(100, 0);
        wShowWelcome.setLayoutData(fdShowWelcome);

        // Use DB Cache?
        wlUseCache=new Label(wGeneralComp, SWT.RIGHT);
        wlUseCache.setText(Messages.getString("EnterOptionsDialog.UseDatabaseCache.Label"));
        props.setLook(wlUseCache);
        FormData fdlUseCache = new FormData();
        fdlUseCache.left = new FormAttachment(0, 0);
        fdlUseCache.top  = new FormAttachment(wShowWelcome, margin);
        fdlUseCache.right= new FormAttachment(middle, -margin);
        wlUseCache.setLayoutData(fdlUseCache);
        wUseCache=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wUseCache);
        wUseCache.setSelection(props.useDBCache());
        FormData fdUseCache = new FormData();
        fdUseCache.left = new FormAttachment(middle, 0);
        fdUseCache.top  = new FormAttachment(wShowWelcome, margin);
        fdUseCache.right= new FormAttachment(100, 0);
        wUseCache.setLayoutData(fdUseCache);


        // Auto load last file at startup?
        wlOpenLast=new Label(wGeneralComp, SWT.RIGHT);
        wlOpenLast.setText(Messages.getString("EnterOptionsDialog.OpenLastFileStartup.Label"));
        props.setLook(wlOpenLast);
        FormData fdlOpenLast = new FormData();
        fdlOpenLast.left = new FormAttachment(0, 0);
        fdlOpenLast.top  = new FormAttachment(wUseCache, margin);
        fdlOpenLast.right= new FormAttachment(middle, -margin);
        wlOpenLast.setLayoutData(fdlOpenLast);
        wOpenLast=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wOpenLast);
        wOpenLast.setSelection(props.openLastFile());
        FormData fdOpenLast = new FormData();
        fdOpenLast.left = new FormAttachment(middle, 0);
        fdOpenLast.top  = new FormAttachment(wUseCache, margin);
        fdOpenLast.right= new FormAttachment(100, 0);
        wOpenLast.setLayoutData(fdOpenLast);

        // Auto save changed files?
        wlAutoSave=new Label(wGeneralComp, SWT.RIGHT);
        wlAutoSave.setText(Messages.getString("EnterOptionsDialog.AutoSave.Label"));
        props.setLook(wlAutoSave);
        FormData fdlAutoSave = new FormData();
        fdlAutoSave.left = new FormAttachment(0, 0);
        fdlAutoSave.top  = new FormAttachment(wOpenLast, margin);
        fdlAutoSave.right= new FormAttachment(middle, -margin);
        wlAutoSave.setLayoutData(fdlAutoSave);
        wAutoSave=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wAutoSave);
        wAutoSave.setSelection(props.getAutoSave());
        FormData fdAutoSave = new FormData();
        fdAutoSave.left = new FormAttachment(middle, 0);
        fdAutoSave.top  = new FormAttachment(wOpenLast, margin);
        fdAutoSave.right= new FormAttachment(100, 0);
        wAutoSave.setLayoutData(fdAutoSave);

        // Auto save changed files?
        wlOnlyActiveFile=new Label(wGeneralComp, SWT.RIGHT);
        wlOnlyActiveFile.setText(Messages.getString("EnterOptionsDialog.OnlyActiveFile.Label"));
        props.setLook(wlOnlyActiveFile);
        FormData fdlOnlyActiveFile = new FormData();
        fdlOnlyActiveFile.left = new FormAttachment(0, 0);
        fdlOnlyActiveFile.top  = new FormAttachment(wAutoSave, margin);
        fdlOnlyActiveFile.right= new FormAttachment(middle, -margin);
        wlOnlyActiveFile.setLayoutData(fdlOnlyActiveFile);
        wOnlyActiveFile=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wOnlyActiveFile);
        wOnlyActiveFile.setSelection(props.isOnlyActiveFileShownInTree());
        FormData fdOnlyActiveFile = new FormData();
        fdOnlyActiveFile.left = new FormAttachment(middle, 0);
        fdOnlyActiveFile.top  = new FormAttachment(wAutoSave, margin);
        fdOnlyActiveFile.right= new FormAttachment(100, 0);
        wOnlyActiveFile.setLayoutData(fdOnlyActiveFile);

        // Only save used connections to XML?
        wlDBConnXML=new Label(wGeneralComp, SWT.RIGHT);
        wlDBConnXML.setText(Messages.getString("EnterOptionsDialog.OnlySaveUsedConnections.Label"));
        props.setLook(wlDBConnXML);
        FormData fdlDBConnXML = new FormData();
        fdlDBConnXML.left = new FormAttachment(0, 0);
        fdlDBConnXML.top  = new FormAttachment(wOnlyActiveFile, margin);
        fdlDBConnXML.right= new FormAttachment(middle, -margin);
        wlDBConnXML.setLayoutData(fdlDBConnXML);
        wDBConnXML=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wDBConnXML);
        wDBConnXML.setSelection(props.areOnlyUsedConnectionsSavedToXML());
        FormData fdDBConnXML = new FormData();
        fdDBConnXML.left = new FormAttachment(middle, 0);
        fdDBConnXML.top  = new FormAttachment(wOnlyActiveFile, margin);
        fdDBConnXML.right= new FormAttachment(100, 0);
        wDBConnXML.setLayoutData(fdDBConnXML);

        // Ask about replacing existing connections?
        wlAskReplaceDB=new Label(wGeneralComp, SWT.RIGHT);
        wlAskReplaceDB.setText(Messages.getString("EnterOptionsDialog.ReplaceDBAsk.Label"));
        props.setLook(wlAskReplaceDB);
        FormData fdlAskReplaceDB = new FormData();
        fdlAskReplaceDB.left = new FormAttachment(0, 0);
        fdlAskReplaceDB.top  = new FormAttachment(wDBConnXML, margin);
        fdlAskReplaceDB.right= new FormAttachment(middle, -margin);
        wlAskReplaceDB.setLayoutData(fdlAskReplaceDB);
        wAskReplaceDB=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wAskReplaceDB);
        wAskReplaceDB.setToolTipText(Messages.getString("EnterOptionsDialog.ReplaceDBAsk.Tooltip"));
        wAskReplaceDB.setSelection(props.askAboutReplacingDatabaseConnections());
        FormData fdAskReplaceDB = new FormData();
        fdAskReplaceDB.left = new FormAttachment(middle, 0);
        fdAskReplaceDB.top  = new FormAttachment(wDBConnXML, margin);
        fdAskReplaceDB.right= new FormAttachment(100, 0);
        wAskReplaceDB.setLayoutData(fdAskReplaceDB);

        // Only save used connections to XML?
        wlReplaceDB=new Label(wGeneralComp, SWT.RIGHT);
        wlReplaceDB.setText(Messages.getString("EnterOptionsDialog.ReplaceDB.Label"));
        props.setLook(wlReplaceDB);
        FormData fdlReplaceDB = new FormData();
        fdlReplaceDB.left = new FormAttachment(0, 0);
        fdlReplaceDB.top  = new FormAttachment(wAskReplaceDB, margin);
        fdlReplaceDB.right= new FormAttachment(middle, -margin);
        wlReplaceDB.setLayoutData(fdlReplaceDB);
        wReplaceDB=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wReplaceDB);
        wReplaceDB.setToolTipText(Messages.getString("EnterOptionsDialog.ReplaceDB.Tooltip"));
        wReplaceDB.setSelection(props.replaceExistingDatabaseConnections());
        FormData fdReplaceDB = new FormData();
        fdReplaceDB.left = new FormAttachment(middle, 0);
        fdReplaceDB.top  = new FormAttachment(wAskReplaceDB, margin);
        fdReplaceDB.right= new FormAttachment(100, 0);
        wReplaceDB.setLayoutData(fdReplaceDB);

        // Show confirmation after save?
        wlSaveConf=new Label(wGeneralComp, SWT.RIGHT);
        wlSaveConf.setText(Messages.getString("EnterOptionsDialog.ShowSaveConfirmation.Label"));
        props.setLook(wlSaveConf);
        FormData fdlSaveConf = new FormData();
        fdlSaveConf.left = new FormAttachment(0, 0);
        fdlSaveConf.top  = new FormAttachment(wReplaceDB, margin);
        fdlSaveConf.right= new FormAttachment(middle, -margin);
        wlSaveConf.setLayoutData(fdlSaveConf);
        wSaveConf=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wSaveConf);
        wSaveConf.setSelection(props.getSaveConfirmation());
        FormData fdSaveConf = new FormData();
        fdSaveConf.left = new FormAttachment(middle, 0);
        fdSaveConf.top  = new FormAttachment(wReplaceDB, margin);
        fdSaveConf.right= new FormAttachment(100, 0);
        wSaveConf.setLayoutData(fdSaveConf);

        // Automatically split hops?
        wlAutoSplit=new Label(wGeneralComp, SWT.RIGHT);
        wlAutoSplit.setText(Messages.getString("EnterOptionsDialog.AutoSplitHops.Label"));
        props.setLook(wlAutoSplit);
        FormData fdlAutoSplit = new FormData();
        fdlAutoSplit.left = new FormAttachment(0, 0);
        fdlAutoSplit.top  = new FormAttachment(wSaveConf, margin);
        fdlAutoSplit.right= new FormAttachment(middle, -margin);
        wlAutoSplit.setLayoutData(fdlAutoSplit);
        wAutoSplit=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wAutoSplit);
        wAutoSplit.setToolTipText(Messages.getString("EnterOptionsDialog.AutoSplitHops.Tooltip"));
        wAutoSplit.setSelection(props.getAutoSplit());
        FormData fdAutoSplit = new FormData();
        fdAutoSplit.left = new FormAttachment(middle, 0);
        fdAutoSplit.top  = new FormAttachment(wSaveConf, margin);
        fdAutoSplit.right= new FormAttachment(100, 0);
        wAutoSplit.setLayoutData(fdAutoSplit);

        // Show warning for copy / distribute...
        wlCopyDistrib=new Label(wGeneralComp, SWT.RIGHT);
        wlCopyDistrib.setText(Messages.getString("EnterOptionsDialog.CopyOrDistributeDialog.Label"));
        props.setLook(wlCopyDistrib);
        FormData fdlCopyDistrib = new FormData();
        fdlCopyDistrib.left = new FormAttachment(0, 0);
        fdlCopyDistrib.top  = new FormAttachment(wAutoSplit, margin);
        fdlCopyDistrib.right= new FormAttachment(middle, -margin);
        wlCopyDistrib.setLayoutData(fdlCopyDistrib);
        wCopyDistrib=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wCopyDistrib);
        wCopyDistrib.setToolTipText(Messages.getString("EnterOptionsDialog.CopyOrDistributeDialog.Tooltip"));
        wCopyDistrib.setSelection(props.showCopyOrDistributeWarning());
        FormData fdCopyDistrib = new FormData();
        fdCopyDistrib.left = new FormAttachment(middle, 0);
        fdCopyDistrib.top  = new FormAttachment(wAutoSplit, margin);
        fdCopyDistrib.right= new FormAttachment(100, 0);
        wCopyDistrib.setLayoutData(fdCopyDistrib);

        // Show repository dialog at startup?
        wlShowRep=new Label(wGeneralComp, SWT.RIGHT);
        wlShowRep.setText(Messages.getString("EnterOptionsDialog.ShowRepoDialog.Label"));
        props.setLook(wlShowRep);
        FormData fdlShowRep = new FormData();
        fdlShowRep.left = new FormAttachment(0, 0);
        fdlShowRep.top  = new FormAttachment(wCopyDistrib, margin);
        fdlShowRep.right= new FormAttachment(middle, -margin);
        wlShowRep.setLayoutData(fdlShowRep);
        wShowRep=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wShowRep);
        wShowRep.setSelection(props.showRepositoriesDialogAtStartup());
        FormData fdShowRep = new FormData();
        fdShowRep.left = new FormAttachment(middle, 0);
        fdShowRep.top  = new FormAttachment(wCopyDistrib, margin);
        fdShowRep.right= new FormAttachment(100, 0);
        wShowRep.setLayoutData(fdShowRep);

        // Show exit warning?
        wlExitWarning=new Label(wGeneralComp, SWT.RIGHT);
        wlExitWarning.setText(Messages.getString("EnterOptionsDialog.AskOnExit.Label"));
        props.setLook(wlExitWarning);
        FormData fdlExitWarning = new FormData();
        fdlExitWarning.left = new FormAttachment(0, 0);
        fdlExitWarning.top  = new FormAttachment(wShowRep, margin);
        fdlExitWarning.right= new FormAttachment(middle, -margin);
        wlExitWarning.setLayoutData(fdlExitWarning);
        wExitWarning=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wExitWarning);
        wExitWarning.setSelection(props.showExitWarning());
        FormData fdExitWarning = new FormData();
        fdExitWarning.left = new FormAttachment(middle, 0);
        fdExitWarning.top  = new FormAttachment(wShowRep, margin);
        fdExitWarning.right= new FormAttachment(100, 0);
        wExitWarning.setLayoutData(fdExitWarning);
        
        // Clear custom parameters. (from step)
        wlClearCustom=new Label(wGeneralComp, SWT.RIGHT);
        wlClearCustom.setText(Messages.getString("EnterOptionsDialog.ClearCustomParameters.Label"));
        props.setLook(wlClearCustom);
        FormData fdlClearCustom = new FormData();
        fdlClearCustom.left = new FormAttachment(0, 0);
        fdlClearCustom.top  = new FormAttachment(wExitWarning, margin + 10);
        fdlClearCustom.right= new FormAttachment(middle, -margin);
        wlClearCustom.setLayoutData(fdlClearCustom);
        wClearCustom=new Button(wGeneralComp, SWT.PUSH | SWT.BORDER);
        props.setLook(wClearCustom);
        
        FormData fdClearCustom = layoutResetOptionButton(wClearCustom);
        fdClearCustom.left = new FormAttachment(middle, 0);
        fdClearCustom.top  = new FormAttachment(wExitWarning, margin);
        wClearCustom.setLayoutData(fdClearCustom);
        wClearCustom.setToolTipText(Messages.getString("EnterOptionsDialog.ClearCustomParameters.Tooltip"));
        wClearCustom.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
                mb.setMessage(Messages.getString("EnterOptionsDialog.ClearCustomParameters.Question"));
                mb.setText(Messages.getString("EnterOptionsDialog.ClearCustomParameters.Title"));
                int id = mb.open();
                if (id==SWT.YES)
                {
                    props.clearCustomParameters();
                    props.saveProps();
                    MessageBox ok = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
                    ok.setMessage(Messages.getString("EnterOptionsDialog.ClearCustomParameters.Confirmation"));
                    ok.open();
                }
            }
        });
        
        //Tooltips
        Label tooltipLbl =new Label(wGeneralComp, SWT.RIGHT);
        tooltipLbl.setText(Messages.getString("EnterOptionsDialog.ToolTipsEnabled.Label"));
        props.setLook(tooltipLbl);
        FormData fdlToolTipData = new FormData();
        fdlToolTipData.left = new FormAttachment(0, 0);
        fdlToolTipData.top  = new FormAttachment(wClearCustom, margin);
        fdlToolTipData.right= new FormAttachment(middle, -margin);
        tooltipLbl.setLayoutData(fdlToolTipData);
        tooltipBtn =new Button(wGeneralComp, SWT.CHECK);
        props.setLook(tooltipBtn);
        tooltipBtn.setSelection(props.showToolTips());
        FormData toolTipBtnData = new FormData();
        toolTipBtnData.left = new FormAttachment(middle, 0);
        toolTipBtnData.top  = new FormAttachment(wClearCustom, margin);
        toolTipBtnData.right= new FormAttachment(100, 0);
        tooltipBtn.setLayoutData(toolTipBtnData);
       

        fdGeneralComp = new FormData();
        fdGeneralComp.left  = new FormAttachment(0, 0);
        fdGeneralComp.right = new FormAttachment(100, 0);
        fdGeneralComp.top   = new FormAttachment(0, 0);
        fdGeneralComp.bottom= new FormAttachment(100, 100);
        wGeneralComp.setLayoutData(fdGeneralComp);

        wGeneralComp.pack();
        
        Rectangle bounds = wGeneralComp.getBounds();
        
        sGeneralComp.setContent(wGeneralComp);
        sGeneralComp.setExpandHorizontal(true);
        sGeneralComp.setExpandVertical(true);
        sGeneralComp.setMinWidth(bounds.width);
        sGeneralComp.setMinHeight(bounds.height);
        
        wGeneralTab.setControl(sGeneralComp);

        /////////////////////////////////////////////////////////////
        /// END OF GENERAL TAB
        /////////////////////////////////////////////////////////////
        
    }

    /**
     * Setting the layout of a <i>Reset</i> option button. Either a button image is set -
     * if existing - or a text.
     * 
     * @param button The button
     */
    private FormData layoutResetOptionButton(Button button)
    {
        FormData fd = new FormData();
        Image editButton = GUIResource.getInstance().getResetOptionButton();
        if (editButton != null)
        {
            button.setImage(editButton);
            button.setBackground(GUIResource.getInstance().getColorWhite());
            fd.width = editButton.getBounds().width + 4;
            fd.height = editButton.getBounds().height;
        }
        else
        {
            button.setText(Messages.getString("EnterOptionsDialog.Button.Reset"));
        }

        button.setToolTipText(Messages.getString("EnterOptionsDialog.Button.Reset.Tooltip"));
        return fd;
    }
    
    /**
     * Setting the layout of an <i>Edit</i> option button. Either a button image is set -
     * if existing - or a text.
     * 
     * @param button The button
     */
    private FormData layoutEditOptionButton(Button button)
    {
        FormData fd = new FormData();
        Image editButton = GUIResource.getInstance().getEditOptionButton();
        if (editButton != null)
        {
            button.setImage(editButton);
            button.setBackground(GUIResource.getInstance().getColorWhite());
            fd.width = editButton.getBounds().width + 4;
            fd.height = editButton.getBounds().height;
        }
        else
        {
            button.setText(Messages.getString("EnterOptionsDialog.Button.Edit"));
        }

        button.setToolTipText(Messages.getString("EnterOptionsDialog.Button.Edit.Tooltip"));
        return fd;
    }

    public void dispose()
	{
        fixedFont.dispose();
        graphFont.dispose();
        noteFont.dispose();
        
        background.dispose();
        graphColor.dispose();
        tabColor.dispose();
        
		shell.dispose();
	}
	
	public void getData()
	{
		fixedFontData = props.getFixedFont();
		fixedFont = new Font(display, fixedFontData);
		
		graphFontData = props.getGraphFont();
		graphFont = new Font(display, graphFontData);
		
		noteFontData = props.getNoteFont();
		noteFont = new Font(display, noteFontData);
		
		backgroundRGB = props.getBackgroundRGB();
		if (backgroundRGB==null) backgroundRGB = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB();
        background = new Color(display, backgroundRGB);

		graphColorRGB = props.getGraphColorRGB();
		graphColor = new Color(display, graphColorRGB);

		tabColorRGB = props.getTabColorRGB();
		tabColor = new Color(display, tabColorRGB);
	}
	
	private void cancel()
	{
		props.setScreen(new WindowProperty(shell));
		props=null;
		dispose();
	}
	
	private void ok()
	{        
		props.setFixedFont     ( fixedFontData );
		props.setGraphFont     ( graphFontData );
		props.setNoteFont      ( noteFontData  );
		props.setBackgroundRGB ( backgroundRGB  );
		props.setGraphColorRGB ( graphColorRGB    );
		props.setTabColorRGB   ( tabColorRGB      );
		props.setIconSize      ( Const.toInt(wIconsize.getText(), props.getIconSize() ));
		props.setLineWidth     ( Const.toInt(wLineWidth.getText(), props.getLineWidth() ));
		props.setShadowSize    ( Const.toInt(wShadowSize.getText(), props.getShadowSize() ));
		props.setMiddlePct     ( Const.toInt(wMiddlePct.getText(), props.getMiddlePct() ));
        
        props.setDefaultPreviewSize( Const.toInt( wDefaultPreview.getText(), props.getDefaultPreviewSize() ));

        props.setMaxNrLinesInLog                     ( Const.toInt(wMaxNrLogLines.getText(), Const.MAX_NR_LOG_LINES) );
		props.setMaxUndo                             ( Const.toInt(wMaxUndo.getText(), props.getMaxUndo() ));
        
		props.setShowTips                            ( wShowTips.getSelection()     );
        props.setShowWelcomePageOnStartup            ( wShowWelcome.getSelection()  );
		props.setUseDBCache                          ( wUseCache.getSelection()     );
		props.setOpenLastFile                        ( wOpenLast.getSelection()     );
		props.setAutoSave                            ( wAutoSave.getSelection()     );
        props.setOnlyUsedConnectionsSavedToXML       ( wDBConnXML.getSelection()    );
        props.setAskAboutReplacingDatabaseConnections( wAskReplaceDB.getSelection() );
        props.setReplaceDatabaseConnections          ( wReplaceDB.getSelection()    );
		props.setSaveConfirmation                    ( wSaveConf.getSelection()     );
		props.setAutoSplit                           ( wAutoSplit.getSelection()    );
        props.setShowCopyOrDistributeWarning         ( wCopyDistrib.getSelection()  );
        props.setRepositoriesDialogAtStartupShown    ( wShowRep.getSelection()      );
        props.setAntiAliasingEnabled                 ( wAntiAlias.getSelection()    );
        props.setExitWarningShown                    ( wExitWarning.getSelection()  );
        props.setOSLookShown                         ( wOriginalLook.getSelection() );
        props.setBrandingActive                      ( wBranding.getSelection()     );
        props.setShowToolTips						 ( tooltipBtn.getSelection()	);

        int defaultLocaleIndex = wDefaultLocale.getSelectionIndex();
        if ( defaultLocaleIndex < 0 || 
             defaultLocaleIndex >= GlobalMessages.localeCodes.length )
        {
        	// Code hardening, when the combo-box ever gets in a strange state,
        	// use the first language as default (should be English)
            defaultLocaleIndex = 0;
        }
        int failoverLocaleIndex = wFailoverLocale.getSelectionIndex();
        if ( failoverLocaleIndex < 0  ||
        	 failoverLocaleIndex >= GlobalMessages.localeCodes.length )
        {
        	failoverLocaleIndex = 0;
        }
       
        LanguageChoice.getInstance().setDefaultLocale( new Locale( GlobalMessages.localeCodes[defaultLocaleIndex]) );
        LanguageChoice.getInstance().setFailoverLocale( new Locale( GlobalMessages.localeCodes[failoverLocaleIndex]) );
        LanguageChoice.getInstance().saveSettings();
        
        props.saveProps();
        
		dispose();
	}
}
