/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.shapefilereader.ShapeFileReaderMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.ProviderFilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

class ShapeSelectionAdapterFileDialogTextVar extends SelectionAdapterFileDialogTextVar {
  private TextVar wDbf;

  public ShapeSelectionAdapterFileDialogTextVar( LogChannelInterface log, TextVar textUiWidget, AbstractMeta meta,
                                        SelectionAdapterOptions options, TextVar wDbf ) {
    super( log, textUiWidget, meta, options );
    this.wDbf = wDbf;
  }

  @Override
  protected void setText(String text) {
    super.setText(text);
    if ( text.toUpperCase().endsWith( ".SHP" ) && ( wDbf.getText( ) == null || wDbf.getText( ).length( ) == 0 ) ) {
      String strdbf = text.substring( 0, text.length( ) - 4 );
      wDbf.setText( strdbf + ".dbf" );
    }
  }
}

public class ShapeFileReaderDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = ShapeFileReaderMeta.class; // for i18n purposes, needed by Translator2!!

  private Label wlShape;
  private Button wbShape;
  private TextVar wShape;
  private FormData fdlShape, fdbShape, fdbcShape, fdShape;

  private Label wlDbf;
  private Button wbDbf;
  private TextVar wDbf;
  private FormData fdlDbf, fdbDbf, fdbcDbf, fdDbf;

  private Label wlEncoding;
  private CCombo wEncoding;
  private FormData fdlEncoding, fdEncoding;

  private ShapeFileReaderMeta input;
  private boolean backup_changed;

  public ShapeFileReaderDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    input = (ShapeFileReaderMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();
    //set encoding based on environment variable or empty otherwise
    if ( StringUtils.isBlank( input.getEncoding() ) ) {
      input.setEncoding( StringUtils.isNotBlank( transMeta.getVariable( "ESRI.encoding" ) )
        ? transMeta.getVariable( "ESRI.encoding" ) : "" );
    }

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

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
    shell.setText( BaseMessages.getString( PKG, "ShapeFileReader.Step.Name" ) );

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

    wShape = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wShape );
    wShape.addModifyListener( lsMod );
    fdShape = new FormData();
    fdShape.left = new FormAttachment( middle, 0 );
    fdShape.right = new FormAttachment( wbShape, -margin );
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

    wDbf = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wDbf );
    wDbf.addModifyListener( lsMod );
    fdDbf = new FormData();
    fdDbf.left = new FormAttachment( middle, 0 );
    fdDbf.right = new FormAttachment( wbDbf, -margin );
    fdDbf.top = new FormAttachment( wShape, margin );
    wDbf.setLayoutData( fdDbf );

    //Encoding
    wlEncoding = new Label( shell, SWT.RIGHT );
    wlEncoding.setText( BaseMessages.getString( PKG, "ShapeFileReader.Encoding.Label" ) );
    props.setLook( wlEncoding );
    fdlEncoding = new FormData();
    fdlEncoding.left = new FormAttachment( 0, 0 );
    fdlEncoding.top = new FormAttachment( wDbf, margin );
    fdlEncoding.right = new FormAttachment( middle, -margin );
    wlEncoding.setLayoutData( fdlEncoding );
    wEncoding = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
    wEncoding.setEditable( false );
    props.setLook( wEncoding );
    wEncoding.addModifyListener( lsMod );
    fdEncoding = new FormData();
    fdEncoding.left = new FormAttachment( middle, 0 );
    fdEncoding.top = new FormAttachment( wDbf, margin );
    fdEncoding.right = new FormAttachment( 100, 0 );
    wEncoding.setLayoutData( fdEncoding );
    wEncoding.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        if ( wEncoding.getItemCount() == 0 ) {
          Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
          shell.setCursor( busy );
          setEncodings();
          shell.setCursor( null );
          busy.dispose();
        }
      }
    } );

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

    wbShape.addSelectionListener(
            new ShapeSelectionAdapterFileDialogTextVar(
                    log,
                    wShape,
                    transMeta,
                    new SelectionAdapterOptions(
                            SelectionOperation.FILE,
                            new FilterType[] { FilterType.SHP, FilterType.ALL },
                            FilterType.SHP,
                            new ProviderFilterType[] {ProviderFilterType.LOCAL} ),
                    wDbf ) );

    wDbf.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent arg0 ) {
        wDbf.setToolTipText( transMeta.environmentSubstitute( wDbf.getText() ) );
      }
    } );

    wbDbf.addSelectionListener(
            new SelectionAdapterFileDialogTextVar(
                    log,
                    wDbf,
                    transMeta,
                    new SelectionAdapterOptions(
                            SelectionOperation.FILE,
                            new FilterType[] { FilterType.DBF, FilterType.ALL },
                            FilterType.DBF,
                            new ProviderFilterType[] {ProviderFilterType.LOCAL} ) ) );

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
    if ( StringUtils.isNotBlank( input.getEncoding() ) ) {
      wEncoding.setText( input.getEncoding() );
    }

    wStepname.selectAll();
  }

  private void setEncodings() {
    List<Charset> values = new ArrayList<>( Charset.availableCharsets().values() );
    for ( Charset charSet : values ) {
      wEncoding.add( charSet.displayName() );
    }

    int idx = Const.indexOfString( input.getEncoding(), wEncoding.getItems() );
    if ( idx >= 0 ) {
      wEncoding.select( idx );
    }
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
    input.setEncoding( StringUtils.isNotBlank( wEncoding.getText() )
      ? wEncoding.getText() : input.getEncoding() );

    dispose();
  }

  private Image getImage() {
    return SwtSvgImageUtil
      .getImage( shell.getDisplay(), getClass().getClassLoader(), "ESRI.svg", ConstUI.ICON_SIZE,
        ConstUI.ICON_SIZE );
  }
}
