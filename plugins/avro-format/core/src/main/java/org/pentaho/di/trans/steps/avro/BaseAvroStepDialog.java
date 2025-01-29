/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.avro;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.trans.steps.avro.input.AvroInputMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseAvroStepDialog extends BaseStepDialog
  implements StepDialogInterface {
  protected static final Class<?> BPKG = BaseAvroStepDialog.class;

  protected ModifyListener lsMod;

  public static final int MARGIN = 15;
  public static final int FIELDS_SEP = 10;
  public static final int FIELD_LABEL_SEP = 5;

  public static final int FIELD_SMALL = 150;
  public static final int FIELD_MEDIUM = 250;
  public static final int FIELD_LARGE = 350;

  private static final String ELLIPSIS = "...";
  private static final int TABLE_ITEM_MARGIN = 2;
  private static final int TOOLTIP_SHOW_DELAY = 350;
  private static final int TOOLTIP_HIDE_DELAY = 2000;
  // width of the icon in a varfield
  protected static final int VAR_EXTRA_WIDTH = GUIResource.getInstance().getImageVariable().getBounds().width;

  protected Image icon;

  protected TextVar wPath;
  protected Button wbBrowse;
  protected Button wbGetDataFromField;
  protected Button wbGetDataFromFile;
  protected ComboVar wFieldNameCombo;
  protected CCombo encodingCombo;
  protected Composite wDataFileComposite;
  protected Composite wDataFieldComposite;

  protected boolean isInputStep;
  private Map<String, Integer> incomingFields = new HashMap<>();

  protected static final String HDFS_SCHEME = "hdfs";

  public BaseAvroStepDialog( Shell parent, BaseStepMeta in, TransMeta transMeta, String sname ) {
    super( parent, in, transMeta, sname );
    if ( baseStepMeta instanceof AvroInputMeta ) {
      isInputStep = true;
    }
  }

  public BaseStepMeta getStepMeta( ) {
    return (BaseStepMeta) baseStepMeta;
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE );
    setShellImage( shell, baseStepMeta );

    lsMod = e -> getStepMeta().setChanged();
    changed = getStepMeta().hasChanged();

    createUI();
    props.setLook( shell );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    int height = Math.max( getMinHeight( shell, getWidth() ), getHeight() );
    shell.setMinimumSize( getWidth(), height );
    shell.setSize( getWidth(), height );
    getData(  );
    shell.open();
    wStepname.setFocus();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  protected void createUI() {
    Control prev = createHeader();

    //main fields
    if ( !isInputStep ) {
      prev = addFileWidgets( shell, prev );
    }

    createFooter( shell );

    Composite afterFile = new Composite( shell, SWT.NONE );
    afterFile.setLayout( new FormLayout() );
    Label separator = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    separator.setLayoutData( new FormDataBuilder().left().right().bottom( wCancel, -MARGIN ).height( 2 ).result() );
    afterFile.setLayoutData( new FormDataBuilder().left().top( prev, 0 ).right().bottom( separator, -MARGIN ).result() );
    createAfterFile( afterFile );
  }

  protected Control createFooter( Composite shell ) {

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( "System.Button.Cancel" ) );
    wCancel.addListener( SWT.Selection, lsCancel );
    wCancel.setLayoutData( new FormDataBuilder().right().bottom().result() );

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( "System.Button.OK" ) );
    wOK.addListener( SWT.Selection, lsOK );
    wOK.setLayoutData( new FormDataBuilder().right( wCancel, -FIELD_LABEL_SEP ).bottom().result() );
    lsPreview = getPreview();
    if ( lsPreview != null ) {
      wPreview = new Button( shell, SWT.PUSH );
      wPreview.setText( getBaseMsg( "BaseStepDialog.Preview" ) );
      wPreview.pack();
      wPreview.addListener( SWT.Selection, lsPreview );
      int offset = wPreview.getBounds().width / 2;
      wPreview.setLayoutData( new FormDataBuilder().bottom().left( new FormAttachment( 50, -offset ) ).result() );
    }
    return wCancel;
  }

  protected void cancel() {
    stepname = null;
    getStepMeta().setChanged( changed );
    dispose();
  }

  protected void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }
    stepname = wStepname.getText();

    getInfo( false );
    dispose();
  }

  protected abstract Control createAfterFile( Composite container );

  protected abstract String getStepTitle();


  /**
   * Read the data from the meta object and show it in this dialog.
   *
   */
  protected abstract void getData(  );

  /**
   * Fill meta object from UI options.
   *
   * @param preview flag for preview or real options should be used. Currently, only one option is differ for preview -
   *                EOL chars. It uses as "mixed" for be able to preview any file.
   */
  protected abstract void getInfo( boolean preview );

  protected abstract int getWidth();

  protected abstract int getHeight();

  protected abstract Listener getPreview();

  protected Label createHeader() {
    // main form
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;
    shell.setLayout( formLayout );
    // title
    shell.setText( getStepTitle() );
    // buttons
    lsOK = e -> ok();
    lsCancel = e -> cancel();

    // Stepname label
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( getBaseMsg( "BaseStepDialog.StepName" ) );
    wlStepname.setLayoutData( new FormDataBuilder().left().top().result() );
    // Stepname field
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    wStepname.addModifyListener( lsMod );
    wStepname.setLayoutData( new FormDataBuilder().left().top( wlStepname, FIELD_LABEL_SEP ).width( FIELD_MEDIUM )
      .result() );

    Label separator = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    separator.setLayoutData( new FormDataBuilder().left().right().top( wStepname, 15 ).height( 2 ).result() );

    addIcon();
    return separator;
  }

  protected void addIcon() {
    Label wicon = new Label( shell, SWT.RIGHT );
    String stepId = getStepMeta().getParentStepMeta().getStepID();
    wicon.setImage( GUIResource.getInstance().getImagesSteps().get( stepId ).getAsBitmapForSize( shell.getDisplay(),
      ConstUI.LARGE_ICON_SIZE, ConstUI.LARGE_ICON_SIZE ) );
    wicon.setLayoutData( new FormDataBuilder().top().right().result() );
  }

  protected Control addFileWidgets( Composite parent, Control prev ) {
    Label wlPath = new Label( parent, SWT.RIGHT );
    wlPath.setText( getBaseMsg( "AvroDialog.Filename.Label" ) );
    wlPath.setLayoutData( new FormDataBuilder().left().top( prev, MARGIN ).result() );

    wbBrowse = new Button( parent, SWT.PUSH );
    wbBrowse.setText( BaseMessages.getString( "System.Button.Browse" ) );

    wbBrowse.setLayoutData( new FormDataBuilder().top( wlPath ).right().result() );

    wPath = new TextVar( transMeta, parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wPath.addModifyListener( event -> {
      if ( wPreview != null ) {
        wPreview.setEnabled( !Utils.isEmpty( wPath.getText() ) );
      }
    } );
    wPath.addModifyListener( lsMod );
    wPath.setLayoutData( new FormDataBuilder().left().right( wbBrowse, -FIELD_LABEL_SEP ).top( wlPath, FIELD_LABEL_SEP )
      .result() );

    wbBrowse.addSelectionListener( new SelectionAdapterFileDialogTextVar(
      log, wPath, transMeta, new SelectionAdapterOptions( transMeta.getBowl(), selectionOperation() ) ) );

    return wPath;
  }

  protected abstract SelectionOperation selectionOperation();

  protected String getBaseMsg( String key ) {
    return BaseMessages.getString( BPKG, key );
  }

  protected int getMinHeight( Composite comp, int minWidth ) {
    comp.pack();
    return comp.computeSize( minWidth, SWT.DEFAULT ).y;
  }

  protected void setTruncatedColumn( Table table, int targetColumn ) {
    table.addListener( SWT.EraseItem, event -> {
      if ( event.index == targetColumn ) {
        event.detail &= ~SWT.FOREGROUND;
      }
    } );
    table.addListener( SWT.PaintItem, event -> {
      TableItem item = (TableItem) event.item;
      int colIdx = event.index;
      if ( colIdx == targetColumn ) {
        String contents = item.getText( colIdx );
        if ( Utils.isEmpty( contents ) ) {
          return;
        }
        Point size = event.gc.textExtent( contents );
        int targetWidth = item.getBounds( colIdx ).width;
        int yOffset = Math.max( 0, ( event.height - size.y ) / 2 );
        if ( size.x > targetWidth ) {
          contents = shortenText( event.gc, contents, targetWidth );
        }
        event.gc.drawText( contents, event.x + TABLE_ITEM_MARGIN, event.y + yOffset, true );
      }
    } );
  }


  protected void addColumnTooltip( Table table, int columnIndex ) {
    final DefaultToolTip toolTip = new DefaultToolTip( table, ToolTip.RECREATE, true );
    toolTip.setRespectMonitorBounds( true );
    toolTip.setRespectDisplayBounds( true );
    toolTip.setPopupDelay( TOOLTIP_SHOW_DELAY );
    toolTip.setHideDelay( TOOLTIP_HIDE_DELAY );
    toolTip.setShift( new Point( ConstUI.TOOLTIP_OFFSET, ConstUI.TOOLTIP_OFFSET ) );
    table.addMouseTrackListener( new MouseTrackAdapter() {
      @Override
      public void mouseHover( MouseEvent e ) {
        Point coord = new Point( e.x, e.y );
        TableItem item = table.getItem( coord );
        if ( item != null && item.getBounds( columnIndex ).contains( coord ) ) {
          String contents = item.getText( columnIndex );
          if ( !Utils.isEmpty( contents ) ) {
            toolTip.setText( contents );
            toolTip.show( coord );
            return;
          }
        }
        toolTip.hide();
      }

      @Override
      public void mouseExit( MouseEvent e ) {
        toolTip.hide();
      }
    } );
  }

  protected String shortenText( GC gc, String text, final int targetWidth ) {
    if ( Utils.isEmpty( text ) ) {
      return "";
    }
    int textWidth = gc.textExtent( text ).x;
    int extra = gc.textExtent( ELLIPSIS ).x + 2 * TABLE_ITEM_MARGIN;
    if ( targetWidth <= extra || textWidth <= targetWidth ) {
      return text;
    }
    int len = text.length();
    for ( int chomp = 1; chomp < len && textWidth + extra >= targetWidth; chomp++ ) {
      text = text.substring( 0, text.length() - 1 );
      textWidth = gc.textExtent( text ).x;
    }
    return text + ELLIPSIS;
  }

  protected void updateIncomingFieldList( ComboVar comboVar ) {
    // Search the fields in the background
    StepMeta stepMeta = transMeta.findStep( stepname );
    if ( stepMeta != null ) {
      try {
        RowMetaInterface row = transMeta.getPrevStepFields( stepMeta );
        incomingFields.clear();
        // Remember these fields...
        for ( int i = 0; i < row.size(); i++ ) {
          incomingFields.put( row.getValueMeta( i ).getName(), i );
        }

        // Add the currentMeta fields...
        final Map<String, Integer> fields = new HashMap<>( incomingFields );

        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<>( keySet );

        String[] fieldNames = entries.toArray( new String[ 0 ] );

        Const.sortStrings( fieldNames );
        comboVar.setItems( fieldNames );
      } catch ( KettleException e ) {
        logError( getBaseMsg( "System.Dialog.GetFieldsFailed.Message" ) );
      }
    }
  }

}
