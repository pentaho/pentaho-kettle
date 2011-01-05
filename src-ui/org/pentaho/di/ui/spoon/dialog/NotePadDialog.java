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

package org.pentaho.di.ui.spoon.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Dialog to enter a text. (descriptions etc.)
 * 
 * @author Samatar
 * @since 20-04-2009
 */
public class NotePadDialog extends Dialog {
	
	private static Class<?> PKG = NotePadDialog.class;  // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private NotePadMeta notePadMeta;

	private Label wlDesc;
	private StyledTextComp wDesc;
	private FormData fdlDesc, fdDesc;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private Shell shell;
	private String title;
	private PropsUI props;

	private CTabFolder wNoteFolder;
	private FormData fdNoteFolder;

	private CTabItem wNoteContentTab, wNoteFontTab;

	private FormData fdNoteContentComp, fdNoteFontComp;

	private Label wlFontName;
	private CCombo wFontName;
	private FormData fdlFontName, fdFontName;

	private Label wlFontSize;
	private Spinner wFontSize;
	private FormData fdlFontSize, fdFontSize;

	private Label wlFontBold;
	private Button wFontBold;
	private FormData fdlFontBold, fdFontBold;

	private Label wlDrawShadow;
	private Button wDrawShadow;
	private FormData fdlDrawShadow, fdDrawShadow;

	private Label wlFontItalic;
	private Button wFontItalic;
	private FormData fdlFontItalic, fdFontItalic;

	private Button wbBackGroundColorChange;
	private FormData fdBackGroundColorChange;

	private Label wBackGroundColor;
	private FormData fdBackGroundColor;

	private Button wbFontColorChange;
	private FormData fdFontColorChange;

	private Label wFontColor;
	private FormData fdFontColor;

	private Button wbBorderColorChange;
	private FormData fdBorderColorChange;

	private Label wBorderColor;
	private FormData fdBorderColor;

	private Label wlFontColor;
	private FormData fdlFontColor;

	private Label wlBackGroundColor;
	private FormData fdlBackGroundColor;

	private Label wlBorderColor;
	private FormData fdlBorderColor;

	private static GUIResource guiresource = GUIResource.getInstance();

	public static RGB COLOR_RGB_BLACK = guiresource.getColorBlack().getRGB();
	public static RGB COLOR_RGB_YELLOW = guiresource.getColorYellow().getRGB();
	public static RGB COLOR_RGB_GRAY = guiresource.getColorGray().getRGB();

	private Color fontColor;
	private Color bgColor;
	private Color borderColor;

	private Font	font;
	
	/**
	 * Dialog to allow someone to show or enter a text in variable width font
	 * 
	 * @param parent
	 *            The parent shell to use
	 * @param title
	 *            The dialog title
	 * @param message
	 *            The message to display
	 * @param text
	 *            The text to display or edit
	 */
	public NotePadDialog(Shell parent, String title, NotePadMeta nMeta) {
		super(parent, SWT.NONE);
		props = PropsUI.getInstance();
		this.title = title;
		if (nMeta != null) {
			notePadMeta = nMeta;
		}
	}

	public NotePadDialog(Shell parent, String title) {
		this(parent, title, null);
	}

	public NotePadMeta open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX
				| SWT.MIN | SWT.NONE);
		props.setLook(shell);
		shell.setImage(guiresource.getImageNoteSmall());

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(title);

		int margin = Const.MARGIN;
		int middle = 30;

		wNoteFolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wNoteFolder, PropsUI.WIDGET_STYLE_TAB);

		// ////////////////////////
		// START OF NOTE CONTENT TAB///
		// /
		wNoteContentTab = new CTabItem(wNoteFolder, SWT.NONE);
		wNoteContentTab.setText(BaseMessages.getString(PKG, "NotePadDialog.ContentTab.Note"));
		Composite wNoteContentComp = new Composite(wNoteFolder, SWT.NONE);
		props.setLook(wNoteContentComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth = 3;
		fileLayout.marginHeight = 3;
		wNoteContentComp.setLayout(fileLayout);

		// From step line
		wlDesc = new Label(wNoteContentComp, SWT.NONE);
		wlDesc.setText(BaseMessages.getString(PKG, "NotePadDialog.ContentTab.Note.Label"));
		props.setLook(wlDesc);
		fdlDesc = new FormData();
		fdlDesc.left = new FormAttachment(0, 0);
		fdlDesc.top = new FormAttachment(0, margin);
		wlDesc.setLayoutData(fdlDesc);
		wDesc = new StyledTextComp(wNoteContentComp, SWT.MULTI | SWT.LEFT
				| SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");

		wDesc.setText("");
		// props.setLook(wDesc, PropsUI.WIDGET_STYLE_FIXED);
		// props.setLook(wDesc);
		fdDesc = new FormData();
		fdDesc.left = new FormAttachment(0, 0);
		fdDesc.top = new FormAttachment(wlDesc, margin);
		fdDesc.right = new FormAttachment(100, 0);
		fdDesc.bottom = new FormAttachment(100, -margin);
		wDesc.setLayoutData(fdDesc);

		fdNoteContentComp = new FormData();
		fdNoteContentComp.left = new FormAttachment(0, 0);
		fdNoteContentComp.top = new FormAttachment(0, 0);
		fdNoteContentComp.right = new FormAttachment(100, 0);
		fdNoteContentComp.bottom = new FormAttachment(100, 0);
		wNoteContentComp.setLayoutData(fdNoteContentComp);
		wNoteContentComp.layout();
		wNoteContentTab.setControl(wNoteContentComp);

		// ///////////////////////////////////////////////////////////
		// / END OF NOTE CONTENT TAB
		// ///////////////////////////////////////////////////////////

		// ////////////////////////
		// START OF NOTE FONT TAB///
		// /
		wNoteFontTab = new CTabItem(wNoteFolder, SWT.NONE);
		wNoteFontTab.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Label"));
		Composite wNoteFontComp = new Composite(wNoteFolder, SWT.NONE);
		props.setLook(wNoteFontComp);

		FormLayout notefontLayout = new FormLayout();
		fileLayout.marginWidth = 3;
		fileLayout.marginHeight = 3;
		wNoteFontComp.setLayout(notefontLayout);

		// Font name
		wlFontName = new Label(wNoteFontComp, SWT.RIGHT);
		wlFontName.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Name.Label"));
		props.setLook(wlFontName);
		fdlFontName = new FormData();
		fdlFontName.left = new FormAttachment(margin, margin);
		fdlFontName.top = new FormAttachment(0, 3 * margin);
		fdlFontName.right = new FormAttachment(middle, -margin);
		wlFontName.setLayoutData(fdlFontName);
		wFontName = new CCombo(wNoteFontComp, SWT.BORDER | SWT.READ_ONLY);
		wFontName.setItems(Const.GetAvailableFontNames());
		props.setLook(wFontName);
		fdFontName = new FormData();
		fdFontName.left = new FormAttachment(middle, 0);
		fdFontName.top = new FormAttachment(0, 3 * margin);
		fdFontName.right = new FormAttachment(100, -margin);
		wFontName.setLayoutData(fdFontName);
		wFontName.select(0);
		wFontName.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	refreshTextNote();
            }
        });


		// FontSize line
		wlFontSize = new Label(wNoteFontComp, SWT.RIGHT);
		wlFontSize.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Size.Label"));
		props.setLook(wlFontSize);
		fdlFontSize = new FormData();
		fdlFontSize.left = new FormAttachment(margin, margin);
		fdlFontSize.top = new FormAttachment(wFontName, margin);
		fdlFontSize.right = new FormAttachment(middle, -margin);
		wlFontSize.setLayoutData(fdlFontSize);
		wFontSize = new Spinner(wNoteFontComp, SWT.BORDER);
		wFontSize.setMinimum(0);
		wFontSize.setMaximum(70);
		wFontSize.setIncrement(1);
		fdFontSize = new FormData();
		fdFontSize.left = new FormAttachment(middle, 0);
		fdFontSize.top = new FormAttachment(wFontName, margin);
		fdFontSize.right = new FormAttachment(100, -margin);
		wFontSize.setLayoutData(fdFontSize);
		wFontSize.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	refreshTextNote();
            }
        });


		// Font bold?
		wlFontBold = new Label(wNoteFontComp, SWT.RIGHT);
		wlFontBold.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Bold.Label"));
		props.setLook(wlFontBold);
		fdlFontBold = new FormData();
		fdlFontBold.left = new FormAttachment(margin, margin);
		fdlFontBold.top = new FormAttachment(wFontSize, margin);
		fdlFontBold.right = new FormAttachment(middle, -margin);
		wlFontBold.setLayoutData(fdlFontBold);
		wFontBold = new Button(wNoteFontComp, SWT.CHECK);
		props.setLook(wFontBold);
		fdFontBold = new FormData();
		fdFontBold.left = new FormAttachment(middle, 0);
		fdFontBold.top = new FormAttachment(wFontSize, margin);
		fdFontBold.right = new FormAttachment(100, -margin);
		wFontBold.setLayoutData(fdFontBold);
		wFontBold.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	refreshTextNote();
            }
        });
		// Font Italic?
		wlFontItalic = new Label(wNoteFontComp, SWT.RIGHT);
		wlFontItalic.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Italic.Label"));
		props.setLook(wlFontItalic);
		fdlFontItalic = new FormData();
		fdlFontItalic.left = new FormAttachment(margin, margin);
		fdlFontItalic.top = new FormAttachment(wFontBold, margin);
		fdlFontItalic.right = new FormAttachment(middle, -margin);
		wlFontItalic.setLayoutData(fdlFontItalic);
		wFontItalic = new Button(wNoteFontComp, SWT.CHECK);
		props.setLook(wFontItalic);
		fdFontItalic = new FormData();
		fdFontItalic.left = new FormAttachment(middle, 0);
		fdFontItalic.top = new FormAttachment(wFontBold, margin);
		fdFontItalic.right = new FormAttachment(100, -margin);
		wFontItalic.setLayoutData(fdFontItalic);
		wFontItalic.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	refreshTextNote();
            }
        });
		// Font color line
		wlFontColor = new Label(wNoteFontComp, SWT.RIGHT);
		wlFontColor.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Color.Label"));
		props.setLook(wlFontColor);
		fdlFontColor = new FormData();
		fdlFontColor.left = new FormAttachment(margin, margin);
		fdlFontColor.top = new FormAttachment(wFontItalic, 2 * margin);
		fdlFontColor.right = new FormAttachment(middle, -margin);
		wlFontColor.setLayoutData(fdlFontColor);

		// Change font color
		wbFontColorChange = new Button(wNoteFontComp, SWT.PUSH);
		wbFontColorChange.setImage(guiresource.getImageColor());
		wbFontColorChange
				.setToolTipText(BaseMessages.getString(PKG, "NotePadDialog.Font.Color.Change.Tooltip"));
		props.setLook(wbFontColorChange);
		fdFontColorChange = new FormData();
		fdFontColorChange.top = new FormAttachment(wFontItalic, 2 * margin);
		fdFontColorChange.right = new FormAttachment(100, -margin);
		wbFontColorChange.setLayoutData(fdFontColorChange);
		wbFontColorChange.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ColorDialog cd = new ColorDialog(shell);
				cd.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Color.Dialog.Label"));
				cd.setRGB(wFontColor.getBackground().getRGB());
				RGB newColor = cd.open();
				if (newColor == null) {
					return;
				}
				fontColor.dispose();
				fontColor = new Color(shell.getDisplay(), newColor);
				wFontColor.setBackground(fontColor);
				refreshTextNote();
			}
		});

		// Font color
		wFontColor = new Label(wNoteFontComp, SWT.NONE);
		wFontColor.setToolTipText(BaseMessages.getString(PKG, "NotePadDialog.Font.Color.Tooltip"));
		props.setLook(wFontColor);
		wFontColor.setEnabled(false);
		fdFontColor = new FormData();
		fdFontColor.left = new FormAttachment(wlFontColor, margin);
		fdFontColor.top = new FormAttachment(wFontItalic, 2 * margin);
		fdFontColor.right = new FormAttachment(wbFontColorChange, -margin);
		wFontColor.setLayoutData(fdFontColor);

		// Background color line
		wlBackGroundColor = new Label(wNoteFontComp, SWT.RIGHT);
		wlBackGroundColor.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.BackGroundColor.Label"));
		props.setLook(wlBackGroundColor);
		fdlBackGroundColor = new FormData();
		fdlBackGroundColor.left = new FormAttachment(margin, margin);
		fdlBackGroundColor.top = new FormAttachment(wFontColor, 2 * margin);
		fdlBackGroundColor.right = new FormAttachment(middle, -margin);
		wlBackGroundColor.setLayoutData(fdlBackGroundColor);

		// Change Background color
		wbBackGroundColorChange = new Button(wNoteFontComp, SWT.PUSH);
		wbBackGroundColorChange.setImage(guiresource.getImageColor());
		wbBackGroundColorChange.setToolTipText(BaseMessages.getString(PKG, "NotePadDialog.Font.BackGroundColor.Change.Tooltip"));
		props.setLook(wbBackGroundColorChange);
		fdBackGroundColorChange = new FormData();
		fdBackGroundColorChange.top = new FormAttachment(wFontColor, 2 * margin);
		fdBackGroundColorChange.right = new FormAttachment(100, -margin);
		fdBackGroundColorChange.right = new FormAttachment(100, -margin);
		wbBackGroundColorChange.setLayoutData(fdBackGroundColorChange);
		wbBackGroundColorChange.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ColorDialog cd = new ColorDialog(shell);
				cd.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Color.Dialog.Label"));
				cd.setRGB(wBackGroundColor.getBackground().getRGB());
				RGB newColor = cd.open();
				if (newColor == null) {
					return;
				}
				bgColor.dispose();
				bgColor = new Color(shell.getDisplay(), newColor);
				wBackGroundColor.setBackground(bgColor);
				refreshTextNote();
			}
		});

		// Background color
		wBackGroundColor = new Label(wNoteFontComp, SWT.BORDER);
		wBackGroundColor.setToolTipText(BaseMessages.getString(PKG, "NotePadDialog.Font.BackGroundColor.Tooltip"));
		props.setLook(wBackGroundColor);
		wBackGroundColor.setEnabled(false);
		fdBackGroundColor = new FormData();
		fdBackGroundColor.left = new FormAttachment(wlBackGroundColor, margin);
		fdBackGroundColor.top = new FormAttachment(wFontColor, 2 * margin);
		fdBackGroundColor.right = new FormAttachment(wbBackGroundColorChange,
				-margin);
		wBackGroundColor.setLayoutData(fdBackGroundColor);

		// Border color line
		wlBorderColor = new Label(wNoteFontComp, SWT.RIGHT);
		wlBorderColor.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.BorderColor.Label"));
		props.setLook(wlBorderColor);
		fdlBorderColor = new FormData();
		fdlBorderColor.left = new FormAttachment(margin, margin);
		fdlBorderColor.top = new FormAttachment(wBackGroundColor, 2 * margin);
		fdlBorderColor.right = new FormAttachment(middle, -margin);
		wlBorderColor.setLayoutData(fdlBorderColor);

		// Change border color
		wbBorderColorChange = new Button(wNoteFontComp, SWT.PUSH);
		wbBorderColorChange.setImage(guiresource.getImageColor());
		wbBorderColorChange
				.setToolTipText(BaseMessages.getString(PKG, "NotePadDialog.Font.BorderColor.Change.Tooltip"));
		props.setLook(wbBorderColorChange);
		fdBorderColorChange = new FormData();
		fdBorderColorChange.top = new FormAttachment(wBackGroundColor,
				2 * margin);
		fdBorderColorChange.right = new FormAttachment(100, -margin);
		wbBorderColorChange.setLayoutData(fdBorderColorChange);
		wbBorderColorChange.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ColorDialog cd = new ColorDialog(shell);
				cd.setText(BaseMessages.getString(PKG, "NotePadDialog.Font.Color.Dialog.Label"));
				cd.setRGB(wBorderColor.getBackground().getRGB());
				RGB newColor = cd.open();
				if (newColor == null) {
					return;
				}
				borderColor.dispose();
				borderColor = new Color(shell.getDisplay(), newColor);
				wBorderColor.setBackground(borderColor);
			}
		});

		// border color
		wBorderColor = new Label(wNoteFontComp, SWT.BORDER);
		wBorderColor.setToolTipText(BaseMessages.getString(PKG, "NotePadDialog.Font.BorderColor.Tooltip"));
		props.setLook(wBorderColor);
		wBorderColor.setEnabled(false);
		fdBorderColor = new FormData();
		fdBorderColor.left = new FormAttachment(wlBorderColor, margin);
		fdBorderColor.top = new FormAttachment(wBackGroundColor, 2 * margin);
		fdBorderColor.right = new FormAttachment(wbBorderColorChange, -margin);
		wBorderColor.setLayoutData(fdBorderColor);

		// Draw note?
		wlDrawShadow = new Label(wNoteFontComp, SWT.RIGHT);
		wlDrawShadow.setText(BaseMessages.getString(PKG, "NotePadDialog.DrawShadow.Label"));
		props.setLook(wlDrawShadow);
		fdlDrawShadow = new FormData();
		fdlDrawShadow.left = new FormAttachment(margin, margin);
		fdlDrawShadow.top = new FormAttachment(wBorderColor, margin);
		fdlDrawShadow.right = new FormAttachment(middle, -margin);
		wlDrawShadow.setLayoutData(fdlDrawShadow);
		wDrawShadow = new Button(wNoteFontComp, SWT.CHECK);
		wDrawShadow.setToolTipText(BaseMessages.getString(PKG, "NotePadDialog.DrawShadow.Tooltip"));
		props.setLook(wDrawShadow);
		fdDrawShadow = new FormData();
		fdDrawShadow.left = new FormAttachment(middle, 0);
		fdDrawShadow.top = new FormAttachment(wBorderColor, margin);
		fdDrawShadow.right = new FormAttachment(100, -margin);
		wDrawShadow.setLayoutData(fdDrawShadow);

		fdNoteFontComp = new FormData();
		fdNoteFontComp.left = new FormAttachment(0, 0);
		fdNoteFontComp.top = new FormAttachment(0, 0);
		fdNoteFontComp.right = new FormAttachment(100, 0);
		fdNoteFontComp.bottom = new FormAttachment(100, 0);
		wNoteFontComp.setLayoutData(fdNoteFontComp);
		wNoteFontComp.layout();
		wNoteFontTab.setControl(wNoteFontComp);

		// ///////////////////////////////////////////////////////////
		// / END OF NOTE FONT TAB
		// ///////////////////////////////////////////////////////////

		fdNoteFolder = new FormData();
		fdNoteFolder.left = new FormAttachment(0, 0);
		fdNoteFolder.top = new FormAttachment(0, margin);
		fdNoteFolder.right = new FormAttachment(100, 0);
		fdNoteFolder.bottom = new FormAttachment(100, -50);
		wNoteFolder.setLayoutData(fdNoteFolder);

		// Some buttons

		wOK = new Button(shell, SWT.PUSH);
	    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell,
				new Button[] { wOK, wCancel }, margin, wNoteFolder);

		// Add listeners
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};

		wOK.addListener(SWT.Selection, lsOK);
		wCancel.addListener(SWT.Selection, lsCancel);

		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		getData();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return notePadMeta;
	}

	public void dispose() {
		props.setScreen(new WindowProperty(shell));
		fontColor.dispose();
		bgColor.dispose();
		borderColor.dispose();
		if (font!=null && !font.isDisposed()) {
			font.dispose();
		}
		shell.dispose();
	}

	public void getData() {
		if (notePadMeta != null) {
			wDesc.setText(notePadMeta.getNote());
			wFontName.setText(notePadMeta.getFontName()==null?props.getNoteFont().getName():notePadMeta.getFontName());
			wFontSize.setSelection(notePadMeta.getFontSize()==-1?props.getNoteFont().getHeight():notePadMeta.getFontSize());
			wFontBold.setSelection(notePadMeta.isFontBold());
			wFontItalic.setSelection(notePadMeta.isFontItalic());
			fontColor = new Color(shell.getDisplay(), new RGB(notePadMeta
					.getFontColorRed(), notePadMeta.getFontColorGreen(), notePadMeta
					.getFontColorBlue()));
			bgColor = new Color(shell.getDisplay(),
					new RGB(notePadMeta.getBackGroundColorRed(), notePadMeta
							.getBackGroundColorGreen(), notePadMeta
							.getBackGroundColorBlue()));
			borderColor = new Color(shell.getDisplay(), new RGB(notePadMeta
					.getBorderColorRed(), notePadMeta.getBorderColorGreen(), notePadMeta
					.getBorderColorBlue()));
			wDrawShadow.setSelection(notePadMeta.isDrawShadow());
		} else {
			wFontName.setText(props.getNoteFont().getName());
			wFontSize.setSelection(props.getNoteFont().getHeight());
			wFontBold.setSelection(false);
			wFontItalic.setSelection(false);
			fontColor = new Color(shell.getDisplay(), new RGB(
					NotePadMeta.COLOR_RGB_BLACK_RED,
					NotePadMeta.COLOR_RGB_BLACK_GREEN,
					NotePadMeta.COLOR_RGB_BLACK_BLUE));
			bgColor = new Color(shell.getDisplay(),
					new RGB(NotePadMeta.COLOR_RGB_DEFAULT_BG_RED,
							NotePadMeta.COLOR_RGB_DEFAULT_BG_GREEN,
							NotePadMeta.COLOR_RGB_DEFAULT_BG_BLUE));
			borderColor = new Color(shell.getDisplay(), new RGB(
					NotePadMeta.COLOR_RGB_DEFAULT_BORDER_RED,
					NotePadMeta.COLOR_RGB_DEFAULT_BORDER_GREEN,
					NotePadMeta.COLOR_RGB_DEFAULT_BORDER_BLUE));
			wDrawShadow.setSelection(true);
		}

		wFontColor.setBackground(fontColor);
		wBackGroundColor.setBackground(bgColor);
		wBorderColor.setBackground(borderColor);

		wNoteFolder.setSelection(0);
		wDesc.setFocus();
		wDesc.setSelection(wDesc.getText().length());
		
		refreshTextNote();
	}

	private void cancel() {
		notePadMeta = null;
		dispose();
	}

	private void ok() {
		notePadMeta = new NotePadMeta();
		if (wDesc.getText() != null) notePadMeta.setNote(wDesc.getText());
		if (wFontName.getText() != null) notePadMeta.setFontName(wFontName.getText());
		notePadMeta.setFontSize(wFontSize.getSelection());
		notePadMeta.setFontBold(wFontBold.getSelection());
		notePadMeta.setFontItalic(wFontItalic.getSelection());
    	// font color
        notePadMeta.setFontColorRed(wFontColor.getBackground().getRed());
        notePadMeta.setFontColorGreen(wFontColor.getBackground().getGreen());
        notePadMeta.setFontColorBlue(wFontColor.getBackground().getBlue());
        // background color
        notePadMeta.setBackGroundColorRed(wBackGroundColor.getBackground().getRed());
        notePadMeta.setBackGroundColorGreen(wBackGroundColor.getBackground().getGreen());
        notePadMeta.setBackGroundColorBlue(wBackGroundColor.getBackground().getBlue());
        // border color
        notePadMeta.setBorderColorRed(wBorderColor.getBackground().getRed());
        notePadMeta.setBorderColorGreen(wBorderColor.getBackground().getGreen());
        notePadMeta.setBorderColorBlue(wBorderColor.getBackground().getBlue());
		notePadMeta.setDrawShadow(wDrawShadow.getSelection());
		dispose();
	}
	
	private void refreshTextNote()
	{
	    int swt=SWT.NORMAL;
	    if(wFontBold.getSelection()) swt=SWT.BOLD;
	    if(wFontItalic.getSelection()) swt=swt | SWT.ITALIC;
	    // dispose of old font only after setting it on wDesc
	    Font oldFont = font;
	    font = new Font(shell.getDisplay(), wFontName.getText(), wFontSize.getSelection(), swt);
		  wDesc.setFont(font);
      if (oldFont != null && !oldFont.isDisposed()) {
        oldFont.dispose();
      }
		for (Control control : wDesc.getChildren()) {
			control.setBackground(bgColor);
		}

		wFontColor.setBackground(fontColor);
		wBackGroundColor.setBackground(bgColor);
		wBorderColor.setBackground(borderColor);
	}
}
