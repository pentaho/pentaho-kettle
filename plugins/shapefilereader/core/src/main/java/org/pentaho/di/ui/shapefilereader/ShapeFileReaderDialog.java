/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

/*
 * Created on 18-mei-2003
 *
 */
package org.pentaho.di.ui.shapefilereader;

import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.shapefilereader.ShapeFileReaderMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.util.SwtSvgImageUtil;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class ShapeFileReaderDialog extends BaseStepDialog implements StepDialogInterface {
  private Label wlShape;
  private Button wbShape;
  private Button wbcShape;
  private Text wShape;
  private FormData fdlShape, fdbShape, fdbcShape, fdShape;

  private Label wlDbf;
  private Button wbDbf;
  private Button wbcDbf;
  private Text wDbf;
  private FormData fdlDbf, fdbDbf, fdbcDbf, fdDbf;

  private ShapeFileReaderMeta input;
  private boolean backup_changed;

  public ShapeFileReaderDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    input = (ShapeFileReaderMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    shell.setImage( getImage() );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    backup_changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( "ESRI Shapefile Reader" );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( "Step name " );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    // Shape line
    wlShape = new Label( shell, SWT.RIGHT );
    wlShape.setText( "Name of the shapefile (.shp) " );
    props.setLook( wlShape );
    fdlShape = new FormData();
    fdlShape.left = new FormAttachment( 0, 0 );
    fdlShape.top = new FormAttachment( wStepname, margin );
    fdlShape.right = new FormAttachment( middle, -margin );
    wlShape.setLayoutData( fdlShape );

    wbShape = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbShape );
    wbShape.setText( "&Browse..." );
    fdbShape = new FormData();
    fdbShape.right = new FormAttachment( 100, 0 );
    fdbShape.top = new FormAttachment( wStepname, margin );
    wbShape.setLayoutData( fdbShape );

    wbcShape = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbcShape );
    wbcShape.setText( "&Variable..." );
    fdbcShape = new FormData();
    fdbcShape.right = new FormAttachment( wbShape, -margin );
    fdbcShape.top = new FormAttachment( wStepname, margin );
    wbcShape.setLayoutData( fdbcShape );

    wShape = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wShape );
    wShape.addModifyListener( lsMod );
    fdShape = new FormData();
    fdShape.left = new FormAttachment( middle, 0 );
    fdShape.right = new FormAttachment( wbcShape, -margin );
    fdShape.top = new FormAttachment( wStepname, margin );
    wShape.setLayoutData( fdShape );

    // Dbf line
    wlDbf = new Label( shell, SWT.RIGHT );
    wlDbf.setText( "Name of the DBF file (.dbf) " );
    props.setLook( wlDbf );
    fdlDbf = new FormData();
    fdlDbf.left = new FormAttachment( 0, 0 );
    fdlDbf.top = new FormAttachment( wShape, margin );
    fdlDbf.right = new FormAttachment( middle, -margin );
    wlDbf.setLayoutData( fdlDbf );

    wbDbf = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbDbf );
    wbDbf.setText( "&Browse..." );
    fdbDbf = new FormData();
    fdbDbf.right = new FormAttachment( 100, 0 );
    fdbDbf.top = new FormAttachment( wShape, margin );
    wbDbf.setLayoutData( fdbDbf );

    wbcDbf = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbcDbf );
    wbcDbf.setText( "&Variable..." );
    fdbcDbf = new FormData();
    fdbcDbf.right = new FormAttachment( wbDbf, -margin );
    fdbcDbf.top = new FormAttachment( wShape, margin );
    wbcDbf.setLayoutData( fdbcDbf );

    wDbf = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wDbf );
    wDbf.addModifyListener( lsMod );
    fdDbf = new FormData();
    fdDbf.left = new FormAttachment( middle, 0 );
    fdDbf.right = new FormAttachment( wbcDbf, -margin );
    fdDbf.top = new FormAttachment( wShape, margin );
    wDbf.setLayoutData( fdDbf );

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( "  &OK  " );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( "  &Cancel  " );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, null );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );

    wShape.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent arg0 ) {
        wShape.setToolTipText( transMeta.environmentSubstitute( wShape.getText() ) );
      }
    } );

    wbShape.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        FileDialog dialog = new FileDialog( shell, SWT.OPEN );
        dialog.setFilterExtensions( new String[] { "*.shp;*.SHP", "*" } );
        if ( wShape.getText() != null ) {
          dialog.setFileName( wShape.getText() );
        }

        dialog.setFilterNames( new String[] { "Shape files", "All files" } );

        if ( dialog.open() != null ) {
          String str = dialog.getFilterPath() + System.getProperty( "file.separator" ) + dialog.getFileName();
          wShape.setText( str );
          if ( str.toUpperCase().endsWith( ".SHP" ) && ( wDbf.getText() == null || wDbf.getText().length() == 0 ) ) {
            String strdbf = str.substring( 0, str.length() - 4 );
            wDbf.setText( strdbf + ".dbf" );
          }
        }
      }
    } );

    // Listen to the Variable... button
    wbcShape.addSelectionListener( new SelectionAdapter() {
      @SuppressWarnings( "unchecked" )
      public void widgetSelected( SelectionEvent e ) {
        Properties sp = System.getProperties();
        Enumeration keys = sp.keys();
        int size = sp.values().size();
        String[] key = new String[size];
        String[] val = new String[size];
        String[] str = new String[size];
        int i = 0;
        while ( keys.hasMoreElements() ) {
          key[i] = (String) keys.nextElement();
          val[i] = sp.getProperty( key[i] );
          str[i] = key[i] + "  [" + val[i] + "]";
          i++;
        }

        EnterSelectionDialog esd =
            new EnterSelectionDialog( shell, str, "Select an Environment Variable", "Select an Environment Variable" );
        if ( esd.open() != null ) {
          int nr = esd.getSelectionNr();
          wShape.insert( "%%" + key[nr] + "%%" );
          wShape.setToolTipText( transMeta.environmentSubstitute( wShape.getText() ) );
        }
      }

    } );

    wDbf.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent arg0 ) {
        wDbf.setToolTipText( transMeta.environmentSubstitute( wDbf.getText() ) );
      }
    } );

    wbDbf.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        FileDialog dialog = new FileDialog( shell, SWT.OPEN );
        dialog.setFilterExtensions( new String[] { "*.dbf;*.DBF", "*" } );
        if ( wDbf.getText() != null ) {
          dialog.setFileName( wDbf.getText() );
        }

        dialog.setFilterNames( new String[] { "DBF files", "All files" } );

        if ( dialog.open() != null ) {
          String str = dialog.getFilterPath() + System.getProperty( "file.separator" ) + dialog.getFileName();
          wDbf.setText( str );
        }
      }
    } );

    // Listen to the Variable... button
    wbcDbf.addSelectionListener( new SelectionAdapter() {
      @SuppressWarnings( "unchecked" )
      public void widgetSelected( SelectionEvent e ) {
        Properties sp = System.getProperties();
        Enumeration keys = sp.keys();
        int size = sp.values().size();
        String[] key = new String[size];
        String[] val = new String[size];
        String[] str = new String[size];
        int i = 0;
        while ( keys.hasMoreElements() ) {
          key[i] = (String) keys.nextElement();
          val[i] = sp.getProperty( key[i] );
          str[i] = key[i] + "  [" + val[i] + "]";
          i++;
        }

        EnterSelectionDialog esd =
            new EnterSelectionDialog( shell, str, "Select an Environment Variable", "Select an Environment Variable" );
        if ( esd.open() != null ) {
          int nr = esd.getSelectionNr();
          wDbf.insert( "${" + key[nr] + "}" );
          wDbf.setToolTipText( transMeta.environmentSubstitute( wDbf.getText() ) );
        }
      }

    } );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    input.setChanged( changed );

    // Set the shell size, based upon previous time...
    setSize();

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  // Read data from input (TextFileInputMeta)
  public void getData() {
    if ( input.getShapeFilename() != null ) {
      wShape.setText( input.getShapeFilename() );
    }
    if ( input.getDbfFilename() != null ) {
      wDbf.setText( input.getDbfFilename() );
    }

    wStepname.selectAll();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( backup_changed );
    dispose();
  }

  private void ok() {
    stepname = wStepname.getText(); // return value

    input.setShapeFilename( wShape.getText() );
    input.setDbfFilename( wDbf.getText() );

    dispose();
  }

  private Image getImage() {
    return SwtSvgImageUtil
      .getImage( shell.getDisplay(), getClass().getClassLoader(), "ESRI.svg", ConstUI.ICON_SIZE,
        ConstUI.ICON_SIZE );
  }
}
