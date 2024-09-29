/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.net.MalformedURLException;
import java.net.URL;

public class ShowHelpDialog extends Dialog {

  private static Class<?> PKG = Spoon.class;

  private static final String DOC_URL = Spoon.DOCUMENTATION_URL;
  private static final String PREFIX = "https://help";
  private static final String PRINT_PREFIX = "https://f1.help";
  private static final String PRINT_SCRIPT = "javascript:window.print();";
  private static final int TOOLBAR_HEIGHT = 34;
  private static final int TOOL_ITEM_SPACING = 4;
  private static final int MARGIN = 5;

  private boolean fromPrint;

  private String dialogTitle;
  private String url;
  private String homeURL;

  private Browser wBrowser;

  private ToolBar toolbarLeft;
  private ToolBar toolbarRight;
  private ToolItem tltmBack;
  private ToolItem tltmForward;
  private ToolItem tltmRefresh;
  private ToolItem tltmHome;
  private ToolItem tltmPrint;

  private Image imageBackEnabled;
  private Image imageBackDisabled;
  private Image imageForwardEnabled;
  private Image imageForwardDisabled;
  private Image imageRefreshEnabled;
  private Image imageRefreshDisabled;
  private Image imageHomeEnabled;
  private Image imageHomeDisabled;
  private Image imagePrintEnabled;
  private Image imagePrintDisabled;
  private Text textURL;

  private Cursor cursorEnabled;

  private Shell shell;
  private Display display;
  private PropsUI props;

  public ShowHelpDialog( Shell parent, String dialogTitle, String url, String header ) {
    super( parent, SWT.NONE );
    props = PropsUI.getInstance();
    this.dialogTitle = BaseMessages.getString( PKG, "Spoon.Documentation.Pentaho.Title" );
    this.url = url;
    try {
      this.homeURL = new URL( DOC_URL ).toString();
    } catch ( MalformedURLException e ) {
    }
  }

  public ShowHelpDialog( Shell parent, String dialogTitle, String url ) {
    this( parent, dialogTitle, url, "" );
  }

  protected Shell createShell( Shell parent ) {
    return new Shell( parent, SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.DIALOG_TRIM );
  }

  public void open() {
      Program.launch( url );
  }

  private void setImages() {
    imageBackEnabled = GUIResource.getInstance().getImageBackEnabled();
    imageBackDisabled = GUIResource.getInstance().getImageBackDisabled();
    imageForwardEnabled = GUIResource.getInstance().getImageForwardEnabled();
    imageForwardDisabled = GUIResource.getInstance().getImageForwardDisabled();
    imageRefreshEnabled = GUIResource.getInstance().getImageRefreshEnabled();
    imageRefreshDisabled = GUIResource.getInstance().getImageRefreshDisabled();
    imageHomeEnabled = GUIResource.getInstance().getImageHomeEnabled();
    imageHomeDisabled = GUIResource.getInstance().getImageHomeDisabled();
    imagePrintEnabled = GUIResource.getInstance().getImagePrintEnabled();
    imagePrintDisabled = GUIResource.getInstance().getImagePrintDisabled();
    cursorEnabled = new Cursor( display, SWT.CURSOR_HAND );
  }

  private void setUpListeners() {
    setUpSelectionListeners();
    addProgressAndLocationListener();
    addShellListener();
  }

  private void setUpSelectionListeners() {
    SelectionListener selectionListenerBack = new SelectionListener() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        back();
      }

      @Override
      public void widgetDefaultSelected( SelectionEvent arg0 ) {
      }
    };

    SelectionListener selectionListenerForward = new SelectionListener() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        forward();
      }

      @Override
      public void widgetDefaultSelected( SelectionEvent arg0 ) {
      }
    };

    SelectionListener selectionListenerRefresh = new SelectionListener() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        refresh();
      }

      @Override
      public void widgetDefaultSelected( SelectionEvent arg0 ) {
      }
    };

    SelectionListener selectionListenerHome = new SelectionListener() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        home();
      }

      @Override
      public void widgetDefaultSelected( SelectionEvent arg0 ) {
      }
    };

    SelectionListener selectionListenerPrint = new SelectionListener() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        print();
      }

      @Override
      public void widgetDefaultSelected( SelectionEvent arg0 ) {
      }
    };
    tltmBack.addSelectionListener( selectionListenerBack );
    tltmForward.addSelectionListener( selectionListenerForward );
    tltmRefresh.addSelectionListener( selectionListenerRefresh );
    tltmHome.addSelectionListener( selectionListenerHome );
    tltmPrint.addSelectionListener( selectionListenerPrint );
  }

  private void addProgressAndLocationListener() {
    ProgressListener progressListener = new ProgressListener() {
      @Override
      public void changed( ProgressEvent event ) {
      }

      @Override
      public void completed( ProgressEvent event ) {
        if ( fromPrint ) {
          wBrowser.execute( PRINT_SCRIPT );
          fromPrint = false;
        }
        setForwardBackEnable();
      }
    };

    LocationListener listener = new LocationListener() {
      @Override
      public void changing( LocationEvent event ) {
        if ( event.location.endsWith( ".pdf" ) ) {
          Program.launch( event.location );
          event.doit = false;
        }
      }

      @Override
      public void changed( LocationEvent event ) {
        textURL.setText( event.location );
      }
    };
    wBrowser.addProgressListener( progressListener );
    wBrowser.addLocationListener( listener );
  }

  private void addShellListener() {
    // Detect [X] or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        ok();
      }
    } );
  }

  private void back() {
    wBrowser.back();
  }

  private void forward() {
    wBrowser.forward();
  }

  private void refresh() {
    wBrowser.refresh();
  }

  private void home() {
    wBrowser.setUrl( homeURL != null ? homeURL : url );
  }

  private void print() {
    String printURL = wBrowser.getUrl();
    if ( printURL.startsWith( PREFIX ) ) {
      printURL = printURL.replace( PREFIX, PRINT_PREFIX );
      fromPrint = true;
      wBrowser.setUrl( printURL );
    } else {
      wBrowser.execute( PRINT_SCRIPT );
    }
  }

  private void setForwardBackEnable() {
    setBackEnable( wBrowser.isBackEnabled() );
    setForwardEnable( wBrowser.isForwardEnabled() );
  }

  private void setBackEnable( boolean enable ) {
    tltmBack.setEnabled( enable );
  }

  private void setForwardEnable( boolean enable ) {
    tltmForward.setEnabled( enable );
  }

  public void dispose() {
    shell.dispose();
  }

  private void ok() {
    dispose();
  }
}
