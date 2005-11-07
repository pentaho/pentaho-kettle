 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 
/*
 * Created on 15-dec-2003
 *
 */

package be.ibridge.kettle.core.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.trans.step.BaseStepDialog;

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

	private Composite    wLookComp, wGeneralComp;
	private FormData     fdLookComp, fdGeneralComp;

	private FontData     fixed_font_data, var_font_data, graph_font_data, grid_font_data, note_font_data;
	private Font         fixed_font, var_font, graph_font, grid_font, note_font;
	private int          iconsize;
	private int          linewidth;
	private int          shadowsize;
	private int          max_undo; 
	private int          middle_pct;    

	private RGB          background_rgb, graphcol_rgb, tabcol_rgb;	
	private Color        background, graphcol, tabcol;

	private Label        wlFFont;
	private Canvas       wFFont;
	private Button       wbFFont;
    private FormData     fdlFFont, fdbFFont, fdFFont;

	private Label        wlVFont;
	private Canvas       wVFont;
	private Button       wbVFont;
	private FormData     fdlVFont, fdbVFont, fdVFont;

	private Label        wlGFont;
	private Canvas       wGFont;
	private Button       wbGFont;
	private FormData     fdlGFont, fdbGFont, fdGFont;

	private Label        wlDFont;
	private Canvas       wDFont;
	private Button       wbDFont;
	private FormData     fdlDFont, fdbDFont, fdDFont;

	private Label        wlNFont;
	private Canvas       wNFont;
	private Button       wbNFont;
	private FormData     fdlNFont, fdbNFont, fdNFont;

	private Label        wlBGColor;
	private Canvas       wBGColor;
	private Button       wbBGColor;
	private FormData     fdlBGColor, fdbBGColor, fdBGColor;

	private Label        wlGrColor;
	private Canvas       wGrColor;
	private Button       wbGrColor;
	private FormData     fdlGrColor, fdbGrColor, fdGrColor;

	private Label        wlTabColor;
	private Canvas       wTabColor;
	private Button       wbTabColor;
	private FormData     fdlTabColor, fdbTabColor, fdTabColor;

	private Label        wlIconsize;
	private Text         wIconsize;
	private FormData     fdlIconsize, fdIconsize;

	private Label        wlLineWidth;
	private Text         wLineWidth;
	private FormData     fdlLineWidth, fdLineWidth;

	private Label        wlShadowSize;
	private Text         wShadowSize;
	private FormData     fdlShadowSize, fdShadowSize;
		
	private Label        wlMaxUndo;
	private Text         wMaxUndo;
	private FormData     fdlMaxUndo, fdMaxUndo;

	private Label        wlMiddlePct;
	private Text         wMiddlePct;
	private FormData     fdlMiddlePct, fdMiddlePct;
    
    private Label        wlAntiAlias;
    private Button       wAntiAlias;
    private FormData     fdlAntiAlias, fdAntiAlias;

    private Label        wlOriginalLook;
    private Button       wOriginalLook;
    private FormData     fdlOriginalLook, fdOriginalLook;

	private Label        wlShowTips;
	private Button       wShowTips;
	private FormData     fdlShowTips, fdShowTips;

	private Label        wlUseCache;
	private Button       wUseCache;
	private FormData     fdlUseCache, fdUseCache;

	private Label        wlOpenLast;
	private Button       wOpenLast;
	private FormData     fdlOpenLast, fdOpenLast;
		
	private Label        wlAutoSave;
	private Button       wAutoSave;
	private FormData     fdlAutoSave, fdAutoSave;

	private Label        wlSaveConf;
	private Button       wSaveConf;
	private FormData     fdlSaveConf, fdSaveConf;

	private Label        wlAutoSplit;
	private Button       wAutoSplit;
	private FormData     fdlAutoSplit, fdAutoSplit;

    private Label        wlShowRep;
    private Button       wShowRep;
    private FormData     fdlShowRep, fdShowRep;

    private Label        wlExitWarning;
    private Button       wExitWarning;
    private FormData     fdlExitWarning, fdExitWarning;

    private Label        wlClearCustom;
    private Button       wClearCustom;
    private FormData     fdlClearCustom, fdClearCustom;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	private Props props;
	
	public EnterOptionsDialog(Shell parent, Props pr)
	{
		super(parent, SWT.NONE);
		props=pr;
	}

	public Props open()
	{
		Shell parent = getParent();
		display = parent.getDisplay();

		getData();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE );
        props.setLook(shell);
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Kettle options...");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
		
		int h = 30;

		wTabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

		//////////////////////////
		// START OF GENERAL TAB///
		///
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText("General");

		FormLayout generalLayout = new FormLayout ();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wGeneralComp);
		wGeneralComp.setLayout(generalLayout);

		// MaxUndo line
		wlMaxUndo=new Label(wGeneralComp, SWT.RIGHT);
		wlMaxUndo.setText("Maximum Undo Level ");
        props.setLook(wlMaxUndo);
		fdlMaxUndo=new FormData();
		fdlMaxUndo.left = new FormAttachment(0, 0);
		fdlMaxUndo.right= new FormAttachment(middle, -margin);
		fdlMaxUndo.top  = new FormAttachment(0, 0);
		wlMaxUndo.setLayoutData(fdlMaxUndo);
		wMaxUndo=new Text(wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wMaxUndo.setText(""+max_undo);
        props.setLook(wMaxUndo);
		fdMaxUndo=new FormData();
		fdMaxUndo.left = new FormAttachment(middle, 0);
		fdMaxUndo.right= new FormAttachment(100, -margin);
		fdMaxUndo.top  = new FormAttachment(0, 0);
		wMaxUndo.setLayoutData(fdMaxUndo);

		// Show tips on startup?
		wlShowTips=new Label(wGeneralComp, SWT.RIGHT);
		wlShowTips.setText("Show tips at startup? ");
        props.setLook(wlShowTips);
		fdlShowTips=new FormData();
		fdlShowTips.left = new FormAttachment(0, 0);
		fdlShowTips.top  = new FormAttachment(wMaxUndo, margin);
		fdlShowTips.right= new FormAttachment(middle, -margin);
		wlShowTips.setLayoutData(fdlShowTips);
		wShowTips=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wShowTips);
		wShowTips.setSelection(props.showTips());
		fdShowTips=new FormData();
		fdShowTips.left = new FormAttachment(middle, 0);
		fdShowTips.top  = new FormAttachment(wMaxUndo, margin);
		fdShowTips.right= new FormAttachment(100, 0);
		wShowTips.setLayoutData(fdShowTips);

		// Use DB Cache?
		wlUseCache=new Label(wGeneralComp, SWT.RIGHT);
		wlUseCache.setText("Use Database cache? ");
        props.setLook(wlUseCache);
		fdlUseCache=new FormData();
		fdlUseCache.left = new FormAttachment(0, 0);
		fdlUseCache.top  = new FormAttachment(wShowTips, margin);
		fdlUseCache.right= new FormAttachment(middle, -margin);
		wlUseCache.setLayoutData(fdlUseCache);
		wUseCache=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wUseCache);
		wUseCache.setSelection(props.useDBCache());
		fdUseCache=new FormData();
		fdUseCache.left = new FormAttachment(middle, 0);
		fdUseCache.top  = new FormAttachment(wShowTips, margin);
		fdUseCache.right= new FormAttachment(100, 0);
		wUseCache.setLayoutData(fdUseCache);


		// Auto load last file at startup?
		wlOpenLast=new Label(wGeneralComp, SWT.RIGHT);
		wlOpenLast.setText("Open last file at startup? ");
        props.setLook(wlOpenLast);
		fdlOpenLast=new FormData();
		fdlOpenLast.left = new FormAttachment(0, 0);
		fdlOpenLast.top  = new FormAttachment(wUseCache, margin);
		fdlOpenLast.right= new FormAttachment(middle, -margin);
		wlOpenLast.setLayoutData(fdlOpenLast);
		wOpenLast=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wOpenLast);
		wOpenLast.setSelection(props.openLastFile());
		fdOpenLast=new FormData();
		fdOpenLast.left = new FormAttachment(middle, 0);
		fdOpenLast.top  = new FormAttachment(wUseCache, margin);
		fdOpenLast.right= new FormAttachment(100, 0);
		wOpenLast.setLayoutData(fdOpenLast);

		// Auto save changed files?
		wlAutoSave=new Label(wGeneralComp, SWT.RIGHT);
		wlAutoSave.setText("Auto save changed file? ");
        props.setLook(wlAutoSave);
		fdlAutoSave=new FormData();
		fdlAutoSave.left = new FormAttachment(0, 0);
		fdlAutoSave.top  = new FormAttachment(wOpenLast, margin);
		fdlAutoSave.right= new FormAttachment(middle, -margin);
		wlAutoSave.setLayoutData(fdlAutoSave);
		wAutoSave=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wAutoSave);
		wAutoSave.setSelection(props.getAutoSave());
		fdAutoSave=new FormData();
		fdAutoSave.left = new FormAttachment(middle, 0);
		fdAutoSave.top  = new FormAttachment(wOpenLast, margin);
		fdAutoSave.right= new FormAttachment(100, 0);
		wAutoSave.setLayoutData(fdAutoSave);

		// Show confirmation after save?
		wlSaveConf=new Label(wGeneralComp, SWT.RIGHT);
		wlSaveConf.setText("Show save confirmation? ");
        props.setLook(wlSaveConf);
		fdlSaveConf=new FormData();
		fdlSaveConf.left = new FormAttachment(0, 0);
		fdlSaveConf.top  = new FormAttachment(wAutoSave, margin);
		fdlSaveConf.right= new FormAttachment(middle, -margin);
		wlSaveConf.setLayoutData(fdlSaveConf);
		wSaveConf=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wSaveConf);
		wSaveConf.setSelection(props.getSaveConfirmation());
		fdSaveConf=new FormData();
		fdSaveConf.left = new FormAttachment(middle, 0);
		fdSaveConf.top  = new FormAttachment(wAutoSave, margin);
		fdSaveConf.right= new FormAttachment(100, 0);
		wSaveConf.setLayoutData(fdSaveConf);

		// Automatically split hops?
		wlAutoSplit=new Label(wGeneralComp, SWT.RIGHT);
		wlAutoSplit.setText("Automatically split hops? ");
        props.setLook(wlAutoSplit);
		fdlAutoSplit=new FormData();
		fdlAutoSplit.left = new FormAttachment(0, 0);
		fdlAutoSplit.top  = new FormAttachment(wSaveConf, margin);
		fdlAutoSplit.right= new FormAttachment(middle, -margin);
		wlAutoSplit.setLayoutData(fdlAutoSplit);
		wAutoSplit=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wAutoSplit);
		wAutoSplit.setSelection(props.getAutoSplit());
		fdAutoSplit=new FormData();
		fdAutoSplit.left = new FormAttachment(middle, 0);
		fdAutoSplit.top  = new FormAttachment(wSaveConf, margin);
		fdAutoSplit.right= new FormAttachment(100, 0);
		wAutoSplit.setLayoutData(fdAutoSplit);

        // Show repository dialog at startup?
        wlShowRep=new Label(wGeneralComp, SWT.RIGHT);
        wlShowRep.setText("Show the repository dialog at startup? ");
        props.setLook(wlShowRep);
        fdlShowRep=new FormData();
        fdlShowRep.left = new FormAttachment(0, 0);
        fdlShowRep.top  = new FormAttachment(wAutoSplit, margin);
        fdlShowRep.right= new FormAttachment(middle, -margin);
        wlShowRep.setLayoutData(fdlShowRep);
        wShowRep=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wShowRep);
        wShowRep.setSelection(props.showRepositoriesDialogAtStartup());
        fdShowRep=new FormData();
        fdShowRep.left = new FormAttachment(middle, 0);
        fdShowRep.top  = new FormAttachment(wAutoSplit, margin);
        fdShowRep.right= new FormAttachment(100, 0);
        wShowRep.setLayoutData(fdShowRep);

        // Show exit warning?
        wlExitWarning=new Label(wGeneralComp, SWT.RIGHT);
        wlExitWarning.setText("Show a warning before exiting? ");
        props.setLook(wlExitWarning);
        fdlExitWarning=new FormData();
        fdlExitWarning.left = new FormAttachment(0, 0);
        fdlExitWarning.top  = new FormAttachment(wShowRep, margin);
        fdlExitWarning.right= new FormAttachment(middle, -margin);
        wlExitWarning.setLayoutData(fdlExitWarning);
        wExitWarning=new Button(wGeneralComp, SWT.CHECK);
        props.setLook(wExitWarning);
        wExitWarning.setSelection(props.showExitWarning());
        fdExitWarning=new FormData();
        fdExitWarning.left = new FormAttachment(middle, 0);
        fdExitWarning.top  = new FormAttachment(wShowRep, margin);
        fdExitWarning.right= new FormAttachment(100, 0);
        wExitWarning.setLayoutData(fdExitWarning);

        // Clear custom parameters. (from step)
        wlClearCustom=new Label(wGeneralComp, SWT.RIGHT);
        wlClearCustom.setText("Clear custom parameters (steps/plugins)");
        props.setLook(wlClearCustom);
        fdlClearCustom=new FormData();
        fdlClearCustom.left = new FormAttachment(0, 0);
        fdlClearCustom.top  = new FormAttachment(wExitWarning, margin);
        fdlClearCustom.right= new FormAttachment(middle, -margin);
        wlClearCustom.setLayoutData(fdlClearCustom);
        wClearCustom=new Button(wGeneralComp, SWT.PUSH);
        props.setLook(wClearCustom);
        wClearCustom.setText("Clear custom flags and parameters");
        fdClearCustom=new FormData();
        fdClearCustom.left = new FormAttachment(middle, 0);
        fdClearCustom.top  = new FormAttachment(wExitWarning, margin);
        fdClearCustom.right= new FormAttachment(100, 0);
        wClearCustom.setLayoutData(fdClearCustom);
        wClearCustom.setToolTipText("Clicking this button will erase all the custom flags and parameters"+Const.CR+"that are used in the dialogs of the steps and plugins. ");
        wClearCustom.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
                mb.setMessage("Are you sure you want to clear the custom step/plugin parameters and flags?");
                mb.setText("Question");
                int id = mb.open();
                if (id==SWT.YES)
                {
                    props.clearCustomParameters();
                    props.saveProps();
                    MessageBox ok = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
                    ok.setMessage("The custom step/plugin parameters and flags were cleared");
                    ok.open();
                }
            }
        });

		fdGeneralComp = new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 100);
		wGeneralComp.setLayoutData(fdGeneralComp);

		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);

		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////

		//////////////////////////
		// START OF LOOK TAB///
		///
		wLookTab=new CTabItem(wTabFolder, SWT.NONE);
		wLookTab.setText("Look && Feel");
		
		wLookComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wLookComp);
		
		FormLayout lookLayout = new FormLayout();
		lookLayout.marginWidth  = 3;
		lookLayout.marginHeight = 3;
		wLookComp.setLayout(lookLayout);


		// Fixed font
		wlFFont=new Label(wLookComp, SWT.RIGHT);
		wlFFont.setText("Fixed width font");
        props.setLook(wlFFont);
		fdlFFont=new FormData();
		fdlFFont.left  = new FormAttachment(0, 0);
		fdlFFont.right = new FormAttachment(middle, -margin);
		fdlFFont.top   = new FormAttachment(0, margin+10);
		wlFFont.setLayoutData(fdlFFont);
		
		wFFont = new Canvas(wLookComp, SWT.BORDER );
        props.setLook(wFFont);
		wFFont.addPaintListener(new PaintListener() 
			{
				public void paintControl(PaintEvent pe) 
				{
					pe.gc.setFont(fixed_font);
					Rectangle max = wFFont.getBounds();
					String name = fixed_font_data.getName();
					Point size = pe.gc.textExtent(name);
					
					pe.gc.drawText(name, (max.width-size.x)/2, (max.height-size.y)/2 );
				}
			}
		);
		fdFFont=new FormData();
		fdFFont.left   = new FormAttachment(middle, 0);
		fdFFont.right  = new FormAttachment(100, -75);
		fdFFont.top    = new FormAttachment(0, margin);
		fdFFont.bottom = new FormAttachment(0, h    );
		wFFont.setLayoutData(fdFFont);
		
		wbFFont = new Button(wLookComp, SWT.PUSH | SWT.BORDER | SWT.CENTER);
		wbFFont.setText("change");
        props.setLook(wbFFont);
        fdbFFont = new FormData();
		fdbFFont.left = new FormAttachment(wFFont, margin);
		fdbFFont.right= new FormAttachment(100, 0);
		fdbFFont.top  = new FormAttachment(0, margin);
		wbFFont.setLayoutData(fdbFFont);
		wbFFont.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) 
				{
					FontDialog fd = new FontDialog(shell);
					//fd.setFontData(fixed_font_data);
					FontData newfd = fd.open();
					if (newfd!=null)
					{
						fixed_font_data = newfd;
						fixed_font.dispose();
						fixed_font = new Font(display, fixed_font_data);
						wFFont.redraw();
					}
				}
			}
		);

		// Variable font
		wlVFont=new Label(wLookComp, SWT.RIGHT);
		wlVFont.setText("Default font");
        props.setLook(wlVFont);
		fdlVFont=new FormData();
		fdlVFont.left  = new FormAttachment(0, 0);
		fdlVFont.right = new FormAttachment(middle, -margin);
		fdlVFont.top   = new FormAttachment(0, h+margin+10);
		wlVFont.setLayoutData(fdlVFont);
		
		wVFont = new Canvas(wLookComp, SWT.BORDER );
        props.setLook(wVFont);
		wVFont.addPaintListener(new PaintListener() 
			{
				public void paintControl(PaintEvent pe) 
				{
					pe.gc.setFont(var_font);
					Rectangle max = wVFont.getBounds();
					String name = "Kettle";
                    if (var_font_data!=null) name = var_font_data.getName();
                    else name = display.getSystemFont().getFontData()[0].getName();
                    
					Point size = pe.gc.textExtent(name);
					
					pe.gc.drawText(name, (max.width-size.x)/2, (max.height-size.y)/2 );
				}
			}
		);
		fdVFont=new FormData();
		fdVFont.left   = new FormAttachment(middle, 0);
		fdVFont.right  = new FormAttachment(100, -75);
		fdVFont.top    = new FormAttachment(0, h+margin);
		fdVFont.bottom = new FormAttachment(0, 2*h+margin );
		wVFont.setLayoutData(fdVFont);
		
		wbVFont = new Button(wLookComp, SWT.PUSH | SWT.BORDER | SWT.CENTER);
		wbVFont.setText("change");
        props.setLook(wbVFont);
		fdbVFont=new FormData();
		fdbVFont.left = new FormAttachment(wVFont, margin);
		fdbVFont.right= new FormAttachment(100, 0);
		fdbVFont.top  = new FormAttachment(0, h+margin);
		wbVFont.setLayoutData(fdbVFont);
		wbVFont.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) 
				{
					FontDialog fd = new FontDialog(shell);
					//fd.setFontData(var_font_data);
					FontData newfd = fd.open();
					if (newfd!=null)
					{
						var_font_data = newfd;
						var_font.dispose();
						var_font = new Font(display, var_font_data);
						wVFont.redraw();
					}
				}
			}
		);

		// Graph font
		wlGFont=new Label(wLookComp, SWT.RIGHT);
		wlGFont.setText("Graph font");
        props.setLook(wlGFont);
		fdlGFont=new FormData();
		fdlGFont.left  = new FormAttachment(0, 0);
		fdlGFont.right = new FormAttachment(middle, -margin);
		fdlGFont.top   = new FormAttachment(0, 2*h+margin+10);
		wlGFont.setLayoutData(fdlGFont);
		
		wGFont = new Canvas(wLookComp, SWT.BORDER );
        props.setLook(wGFont);
		wGFont.addPaintListener(new PaintListener() 
			{
				public void paintControl(PaintEvent pe) 
				{
					pe.gc.setFont(graph_font);
					Rectangle max = wGFont.getBounds();
					String name = graph_font_data.getName();
					Point size = pe.gc.textExtent(name);
					
					pe.gc.drawText(name, (max.width-size.x)/2, (max.height-size.y)/2 );
				}
			}
		);
		fdGFont=new FormData();
		fdGFont.left   = new FormAttachment(middle, 0);
		fdGFont.right  = new FormAttachment(100, -75);
		fdGFont.top    = new FormAttachment(0, 2*h+margin);
		fdGFont.bottom = new FormAttachment(0, 3*h+margin );
		wGFont.setLayoutData(fdGFont);
		
		wbGFont = new Button(wLookComp, SWT.PUSH | SWT.BORDER | SWT.CENTER);
		wbGFont.setText("change");
        props.setLook(wbGFont);
		fdbGFont=new FormData();
		fdbGFont.left = new FormAttachment(wGFont, margin);
		fdbGFont.right= new FormAttachment(100, 0);
		fdbGFont.top  = new FormAttachment(0, 2*h+margin);
		wbGFont.setLayoutData(fdbGFont);
		wbGFont.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) 
				{
					FontDialog fd = new FontDialog(shell);
					FontData newfd = fd.open();
					if (newfd!=null)
					{
						graph_font_data = newfd;
						graph_font.dispose();
						graph_font = new Font(display, graph_font_data);
						wGFont.redraw();
					}
				}
			}
		);

		// Grid font
		wlDFont = new Label(wLookComp, SWT.RIGHT);
		wlDFont.setText("Grid font");
        props.setLook(wlDFont);
		fdlDFont = new FormData();
		fdlDFont.left = new FormAttachment(0, 0);
		fdlDFont.right = new FormAttachment(middle, - margin);
		fdlDFont.top = new FormAttachment(0, 3*h + margin + 10);
		wlDFont.setLayoutData(fdlDFont);

		wDFont = new Canvas(wLookComp, SWT.BORDER);
        props.setLook(wDFont);
		wDFont.addPaintListener(new PaintListener() 
		{
				public void paintControl(PaintEvent pe) 
				{
					pe.gc.setFont(grid_font);
					Rectangle max = wDFont.getBounds();
					String name = grid_font_data.getName();
					Point size = pe.gc.textExtent(name);
	
					pe.gc.drawText(
						name,
						(max.width - size.x) / 2,
						(max.height - size.y) / 2);
				}
			}
		);
		fdDFont = new FormData();
		fdDFont.left = new FormAttachment(middle, 0);
		fdDFont.right = new FormAttachment(100, -75);
		fdDFont.top = new FormAttachment(0, 3*h + margin);
		fdDFont.bottom = new FormAttachment(0, 4*h + margin);
		wDFont.setLayoutData(fdDFont);

		wbDFont = new Button(wLookComp, SWT.PUSH | SWT.BORDER | SWT.CENTER);
        wbDFont.setText("change");
        props.setLook(wbDFont);
		fdbDFont = new FormData();
		fdbDFont.left = new FormAttachment(wDFont, margin);
		fdbDFont.right = new FormAttachment(100, 0);
		fdbDFont.top = new FormAttachment(0, 3*h + margin);
		wbDFont.setLayoutData(fdbDFont);
		wbDFont.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) 
				{
					FontDialog fd = new FontDialog(shell);
					FontData newfd = fd.open();
					if (newfd != null) 
					{
						grid_font_data = newfd;
						grid_font.dispose();
						grid_font = new Font(display, grid_font_data);
						wDFont.redraw();
					}
				}
			}
		);

		// Note font
		wlNFont = new Label(wLookComp, SWT.RIGHT);
		wlNFont.setText("Note font");
        props.setLook(wlNFont);
		fdlNFont = new FormData();
		fdlNFont.left  = new FormAttachment(0, 0);
		fdlNFont.right = new FormAttachment(middle, -margin);
		fdlNFont.top   = new FormAttachment(0, 4*h + margin + 10);
		wlNFont.setLayoutData(fdlNFont);

		wNFont = new Canvas(wLookComp, SWT.BORDER);
        props.setLook(wNFont);
		wNFont.addPaintListener(new PaintListener() 
		{
				public void paintControl(PaintEvent pe) 
				{
					pe.gc.setFont(note_font);
					Rectangle max = wNFont.getBounds();
					String name = note_font_data.getName();
					Point size = pe.gc.textExtent(name);
	
					pe.gc.drawText(
						name,
						(max.width - size.x) / 2,
						(max.height - size.y) / 2);
				}
			}
		);
		fdNFont = new FormData();
		fdNFont.left = new FormAttachment(middle, 0);
		fdNFont.right = new FormAttachment(100, -75);
		fdNFont.top = new FormAttachment(0, 4*h + margin);
		fdNFont.bottom = new FormAttachment(0, 5*h + margin);
		wNFont.setLayoutData(fdNFont);

		wbNFont = new Button(wLookComp, SWT.PUSH | SWT.BORDER | SWT.CENTER);
		wbNFont.setText("change");
        props.setLook(wbNFont);
		fdbNFont = new FormData();
		fdbNFont.left = new FormAttachment(wNFont, margin);
		fdbNFont.right = new FormAttachment(100, 0);
		fdbNFont.top = new FormAttachment(0, 4*h + margin);
		wbNFont.setLayoutData(fdbNFont);
		wbNFont.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) 
				{
					FontDialog fd = new FontDialog(shell);
					FontData newfd = fd.open();
					if (newfd != null) 
					{
						note_font_data = newfd;
						note_font.dispose();
						note_font = new Font(display, note_font_data);
						wNFont.redraw();
					}
				}
			}
		);

		// Background color
		wlBGColor = new Label(wLookComp, SWT.RIGHT);
		wlBGColor.setText("Background color");
        props.setLook(wlBGColor);
		fdlBGColor = new FormData();
		fdlBGColor.left = new FormAttachment(0, 0);
		fdlBGColor.right = new FormAttachment(middle, -margin);
		fdlBGColor.top = new FormAttachment(0, 5*h + margin + 10);
		wlBGColor.setLayoutData(fdlBGColor);

		wBGColor = new Canvas(wLookComp, SWT.BORDER);
        props.setLook(wBGColor);
        wBGColor.setBackground(background);
		fdBGColor = new FormData();
		fdBGColor.left = new FormAttachment(middle, 0);
		fdBGColor.right = new FormAttachment(100, -75);
		fdBGColor.top = new FormAttachment(0, 5*h + margin);
		fdBGColor.bottom = new FormAttachment(0, 6*h + margin);
		wBGColor.setLayoutData(fdBGColor);

		wbBGColor = new Button(wLookComp, SWT.PUSH | SWT.BORDER | SWT.CENTER);
		wbBGColor.setText("change");
        props.setLook(wbBGColor);
		fdbBGColor = new FormData();
		fdbBGColor.left = new FormAttachment(wBGColor, margin);
		fdbBGColor.right = new FormAttachment(100, 0);
		fdbBGColor.top = new FormAttachment(0, 5*h + margin);
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
						background_rgb = newbg;
						background.dispose();
						background=new Color(display, background_rgb);
						wBGColor.setBackground(background);
						wBGColor.redraw();
					}
				}
			}
		);

		// Graph background color
		wlGrColor = new Label(wLookComp, SWT.RIGHT);
		wlGrColor.setText("Graph background color");
        props.setLook(wlGrColor);
		fdlGrColor = new FormData();
		fdlGrColor.left = new FormAttachment(0, 0);
		fdlGrColor.right = new FormAttachment(middle, -margin);
		fdlGrColor.top = new FormAttachment(0, 6*h + margin + 10);
		wlGrColor.setLayoutData(fdlGrColor);

		wGrColor = new Canvas(wLookComp, SWT.BORDER);
        props.setLook(wGrColor);
        wGrColor.setBackground(graphcol);
		fdGrColor = new FormData();
		fdGrColor.left = new FormAttachment(middle, 0);
		fdGrColor.right = new FormAttachment(100, -75);
		fdGrColor.top = new FormAttachment(0, 6*h + margin);
		fdGrColor.bottom = new FormAttachment(0, 7*h + margin);
		wGrColor.setLayoutData(fdGrColor);

		wbGrColor = new Button(wLookComp, SWT.PUSH | SWT.BORDER | SWT.CENTER);
		wbGrColor.setText("change");
        props.setLook(wbGrColor);
		fdbGrColor = new FormData();
		fdbGrColor.left = new FormAttachment(wGrColor, margin);
		fdbGrColor.right = new FormAttachment(100, 0);
		fdbGrColor.top = new FormAttachment(0, 6*h + margin);
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
						graphcol_rgb = newbg;
						graphcol.dispose();
						graphcol=new Color(display, graphcol_rgb);
						wGrColor.setBackground(graphcol);
						wGrColor.redraw();
					}
				}
			}
		);
		
		// Tab selected color
		wlTabColor = new Label(wLookComp, SWT.RIGHT);
		wlTabColor.setText("Color of selected tabs");
        props.setLook(wlTabColor);
		fdlTabColor = new FormData();
		fdlTabColor.left = new FormAttachment(0, 0);
		fdlTabColor.right = new FormAttachment(middle, -margin);
		fdlTabColor.top = new FormAttachment(0, 7*h + margin + 10);
		wlTabColor.setLayoutData(fdlTabColor);

		wTabColor = new Canvas(wLookComp, SWT.BORDER);
        props.setLook(wTabColor);
        wTabColor.setBackground(tabcol);
		fdTabColor = new FormData();
		fdTabColor.left = new FormAttachment(middle, 0);
		fdTabColor.right = new FormAttachment(100, -75);
		fdTabColor.top = new FormAttachment(0, 7*h + margin);
		fdTabColor.bottom = new FormAttachment(0, 8*h + margin);
		wTabColor.setLayoutData(fdTabColor);

		wbTabColor = new Button(wLookComp, SWT.PUSH | SWT.BORDER | SWT.CENTER);
        wbTabColor.setText("change");
        props.setLook(wbTabColor);
		fdbTabColor = new FormData();
		fdbTabColor.left = new FormAttachment(wTabColor, margin);
		fdbTabColor.right = new FormAttachment(100, 0);
		fdbTabColor.top = new FormAttachment(0, 7*h + margin);
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
						tabcol_rgb = newbg;
						tabcol.dispose();
						tabcol=new Color(display, tabcol_rgb);
						wTabColor.setBackground(tabcol);
						wTabColor.redraw();
					}
				}
			}
		);
		
		
		// Iconsize line
		wlIconsize=new Label(wLookComp, SWT.RIGHT);
		wlIconsize.setText("Icon size ");
        props.setLook(wlIconsize);
		fdlIconsize=new FormData();
		fdlIconsize.left = new FormAttachment(0, 0);
		fdlIconsize.right= new FormAttachment(middle, -margin);
		fdlIconsize.top  = new FormAttachment(wTabColor, margin);
		wlIconsize.setLayoutData(fdlIconsize);
		wIconsize=new Text(wLookComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wIconsize.setText(""+iconsize);
        props.setLook(wIconsize);
		fdIconsize=new FormData();
		fdIconsize.left = new FormAttachment(middle, 0);
		fdIconsize.right= new FormAttachment(100, -margin);
		fdIconsize.top  = new FormAttachment(wTabColor, margin);
		wIconsize.setLayoutData(fdIconsize);

		// LineWidth line
		wlLineWidth=new Label(wLookComp, SWT.RIGHT);
		wlLineWidth.setText("Line width ");
        props.setLook(wlLineWidth);
		fdlLineWidth=new FormData();
		fdlLineWidth.left = new FormAttachment(0, 0);
		fdlLineWidth.right= new FormAttachment(middle, -margin);
		fdlLineWidth.top  = new FormAttachment(wIconsize, margin);
		wlLineWidth.setLayoutData(fdlLineWidth);
		wLineWidth=new Text(wLookComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wLineWidth.setText(""+linewidth);
        props.setLook(wLineWidth);
		fdLineWidth=new FormData();
		fdLineWidth.left = new FormAttachment(middle, 0);
		fdLineWidth.right= new FormAttachment(100, -margin);
		fdLineWidth.top  = new FormAttachment(wIconsize, margin);
		wLineWidth.setLayoutData(fdLineWidth);

		// ShadowSize line
		wlShadowSize=new Label(wLookComp, SWT.RIGHT);
		wlShadowSize.setText("Shadow size ");
        props.setLook(wlShadowSize);
		fdlShadowSize=new FormData();
		fdlShadowSize.left = new FormAttachment(0, 0);
		fdlShadowSize.right= new FormAttachment(middle, -margin);
		fdlShadowSize.top  = new FormAttachment(wLineWidth, margin);
		wlShadowSize.setLayoutData(fdlShadowSize);
		wShadowSize=new Text(wLookComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wShadowSize.setText(""+shadowsize);
        props.setLook(wShadowSize);
		fdShadowSize=new FormData();
		fdShadowSize.left = new FormAttachment(middle, 0);
		fdShadowSize.right= new FormAttachment(100, -margin);
		fdShadowSize.top  = new FormAttachment(wLineWidth, margin);
		wShadowSize.setLayoutData(fdShadowSize);

		// MiddlePct line
		wlMiddlePct=new Label(wLookComp, SWT.RIGHT);
		wlMiddlePct.setText("Dialog middle percentage ");
        props.setLook(wlMiddlePct);
		fdlMiddlePct=new FormData();
		fdlMiddlePct.left = new FormAttachment(0, 0);
		fdlMiddlePct.right= new FormAttachment(middle, -margin);
		fdlMiddlePct.top  = new FormAttachment(wShadowSize, margin);
		wlMiddlePct.setLayoutData(fdlMiddlePct);
		wMiddlePct=new Text(wLookComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wMiddlePct.setText(""+middle_pct);
        props.setLook(wMiddlePct);
		fdMiddlePct=new FormData();
		fdMiddlePct.left = new FormAttachment(middle, 0);
		fdMiddlePct.right= new FormAttachment(100, -margin);
		fdMiddlePct.top  = new FormAttachment(wShadowSize, margin);
		wMiddlePct.setLayoutData(fdMiddlePct);
        
        
        // Enable anti-aliasing
        wlAntiAlias=new Label(wLookComp, SWT.RIGHT);
        wlAntiAlias.setText("Canvas anti-aliasing enabled? ");
        props.setLook(wlAntiAlias);
        fdlAntiAlias=new FormData();
        fdlAntiAlias.left = new FormAttachment(0, 0);
        fdlAntiAlias.top  = new FormAttachment(wMiddlePct, margin);
        fdlAntiAlias.right= new FormAttachment(middle, -margin);
        wlAntiAlias.setLayoutData(fdlAntiAlias);
        wAntiAlias=new Button(wLookComp, SWT.CHECK);
        props.setLook(wAntiAlias);
        wAntiAlias.setSelection(props.isAntiAliasingEnabled());
        fdAntiAlias=new FormData();
        fdAntiAlias.left = new FormAttachment(middle, 0);
        fdAntiAlias.top  = new FormAttachment(wMiddlePct, margin);
        fdAntiAlias.right= new FormAttachment(100, 0);
        wAntiAlias.setLayoutData(fdAntiAlias);

        // Show original look
        wlOriginalLook=new Label(wLookComp, SWT.RIGHT);
        wlOriginalLook.setText("Take over look from the OS? ");
        props.setLook(wlOriginalLook);
        fdlOriginalLook=new FormData();
        fdlOriginalLook.left = new FormAttachment(0, 0);
        fdlOriginalLook.top  = new FormAttachment(wAntiAlias, margin);
        fdlOriginalLook.right= new FormAttachment(middle, -margin);
        wlOriginalLook.setLayoutData(fdlOriginalLook);
        wOriginalLook=new Button(wLookComp, SWT.CHECK);
        props.setLook(wOriginalLook);
        wOriginalLook.setSelection(props.isOSLookShown());
        fdOriginalLook=new FormData();
        fdOriginalLook.left = new FormAttachment(middle, 0);
        fdOriginalLook.top  = new FormAttachment(wAntiAlias, margin);
        fdOriginalLook.right= new FormAttachment(100, 0);
        wOriginalLook.setLayoutData(fdOriginalLook);
        

		fdLookComp=new FormData();
		fdLookComp.left  = new FormAttachment(0, 0);
		fdLookComp.right = new FormAttachment(100, 0);
		fdLookComp.top   = new FormAttachment(0, 0);
		fdLookComp.bottom= new FormAttachment(100, 100);
		wLookComp.setLayoutData(fdLookComp);
	
		wLookComp.layout();
		wLookTab.setControl(wLookComp);

		/////////////////////////////////////////////////////////////
		/// END OF LOOK TAB
		/////////////////////////////////////////////////////////////


		// Some buttons
		wOK=new Button(shell, SWT.PUSH );
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");
		
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
		
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		// Clean up used resources!
		shell.addDisposeListener(new DisposeListener() 
			{
				public void widgetDisposed(DisposeEvent arg0) 
				{
					fixed_font.dispose();
					var_font.dispose();
					graph_font.dispose();
					grid_font.dispose();
					note_font.dispose();
					background.dispose();
					graphcol.dispose();
				}
			}
		);

		wTabFolder.setSelection(0);
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return props;
	}

	public void dispose()
	{
		shell.dispose();
	}
	
	public void getData()
	{
		fixed_font_data = props.getFixedFont();
		fixed_font = new Font(display, fixed_font_data);
		
		var_font_data = props.getDefaultFont();
		if (var_font_data!=null) var_font = new Font(display, var_font_data);
        else var_font = display.getSystemFont();
		
		graph_font_data = props.getGraphFont();
		graph_font = new Font(display, graph_font_data);
		
		grid_font_data = props.getGridFont();
		grid_font = new Font(display, grid_font_data);
		
		note_font_data = props.getNoteFont();
		note_font = new Font(display, note_font_data);
		
		background_rgb = props.getBackgroundRGB();
		if (background_rgb!=null) background = new Color(display, background_rgb);
        else background = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

		graphcol_rgb = props.getGraphColorRGB();
		graphcol = new Color(display, graphcol_rgb);

		tabcol_rgb = props.getTabColorRGB();
		tabcol = new Color(display, tabcol_rgb);

		iconsize = props.getIconSize();
		linewidth = props.getLineWidth();
		shadowsize = props.getShadowSize();
		max_undo = props.getMaxUndo();
		middle_pct = props.getMiddlePct();
	}
	
	private void cancel()
	{
		props.setScreen(new WindowProperty(shell));
		props=null;
		dispose();
	}
	
	private void ok()
	{
		iconsize   = Const.toInt(wIconsize.getText(), Const.ICON_SIZE);
		linewidth  = Const.toInt(wLineWidth.getText(), Const.LINE_WIDTH);
		shadowsize = Const.toInt(wShadowSize.getText(), Const.SHADOW_SIZE);
		max_undo   = Const.toInt(wMaxUndo.getText(), Const.MAX_UNDO);		
		middle_pct = Const.toInt(wMiddlePct.getText(), Const.MIDDLE_PCT);
		
		props.setFixedFont     ( fixed_font_data );
		props.setDefaultFont   ( var_font_data   );
		props.setGraphFont     ( graph_font_data );
		props.setGridFont      ( grid_font_data  );
		props.setNoteFont      ( note_font_data  );
		props.setBackgroundRGB ( background_rgb  );
		props.setGraphColorRGB ( graphcol_rgb    );
		props.setTabColorRGB   ( tabcol_rgb      );
		props.setIconSize      ( iconsize        );
		props.setLineWidth     ( linewidth       );
		props.setShadowSize    ( shadowsize      );
		props.setMiddlePct     ( middle_pct      );

		props.setMaxUndo                         ( max_undo                    );
		props.setShowTips                        ( wShowTips.getSelection()    );
		props.setUseDBCache                      ( wUseCache.getSelection()    );
		props.setOpenLastFile                    ( wOpenLast.getSelection()    );
		props.setAutoSave                        ( wAutoSave.getSelection()    );
		props.setSaveConfirmation                ( wSaveConf.getSelection()    );
		props.setAutoSplit                       ( wAutoSplit.getSelection()   );
        props.setRepositoriesDialogAtStartupShown( wShowRep.getSelection()     );
        props.setAntiAliasingEnabled             ( wAntiAlias.getSelection()   );
        props.setExitWarningShown                ( wExitWarning.getSelection() );
        props.setOSLookShown                     ( wOriginalLook.getSelection());
		
        props.saveProps();
        
		dispose();
	}
}
