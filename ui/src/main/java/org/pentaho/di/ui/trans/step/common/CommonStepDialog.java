/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2018 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */
package org.pentaho.di.ui.trans.step.common;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.dialog.BaseDialog;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.dialog.SimpleMessageDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.util.List;

/**
 * A common implementation of the {@link BaseStepDialog} that creates many of the common UI components.
 *
 * @param <StepMetaType>
 */
public abstract class CommonStepDialog<StepMetaType extends CommonStepMeta> extends BaseStepDialog implements
  StepDialogInterface {

  protected static final int MARGIN_SIZE = BaseDialog.MARGIN_SIZE;
  protected static final int LABEL_SPACING = BaseDialog.LABEL_SPACING;
  protected static final int ELEMENT_SPACING = BaseDialog.ELEMENT_SPACING;
  protected static final int MEDIUM_FIELD = BaseDialog.MEDIUM_FIELD;
  protected static final int MEDIUM_SMALL_FIELD = BaseDialog.MEDIUM_SMALL_FIELD;
  protected static final int SMALL_FIELD = BaseDialog.SMALL_FIELD;
  protected static final int SHELL_WIDTH_OFFSET = BaseDialog.SHELL_WIDTH_OFFSET;
  protected static final int VAR_ICON_WIDTH = BaseDialog.VAR_ICON_WIDTH;
  protected static final int VAR_ICON_HEIGHT = BaseDialog.VAR_ICON_HEIGHT;

  private static final int SHELL_WIDTH = 610;

  private static Class<?> PKG = StepInterface.class;

  protected final StepMetaType meta;

  protected Label footerSpacer;
  protected Label headerSpacer;

  protected ModifyListener lsMod;

  protected CTabFolder m_wTabFolder;

  public CommonStepDialog( Shell parent, Object meta, TransMeta tr, String sname ) {
    super( parent, (StepMetaInterface) meta, tr, sname );
    this.meta = (StepMetaType) meta;
  }

  private void initListeners() {
    // define the listener for the ok button
    lsOK = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        ok();
      }
    };

    // define the listener for the cancel button
    lsCancel = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    // define the listener adapter for default widget selection
    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    // define the listener for the meta changes
    lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        meta.setChanged();
      }
    };

    // define a listener for the "preview" action
    lsPreview = new Listener() {
      public void handleEvent( Event e ) {
        preview();
      }
    };

    initListenersImpl();
  }

  /**
   * Can be overridden to initialize additional listeners.
   */
  protected void initListenersImpl() {
    // override
  }

  private Display prepareLayout() {

    // Prep the parent shell and the dialog shell
    final Shell parent = getParent();
    final Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, meta );
    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    changed = meta.hasChanged();

    final FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = BaseDialog.MARGIN_SIZE;
    formLayout.marginHeight = BaseDialog.MARGIN_SIZE;

    shell.setLayout( formLayout );
    shell.setText( getTitle() );
    return display;
  }

  public String open() {

    final Display display = prepareLayout();
    initListeners();

    buildHeader();
    buildBody();
    buildFooter();

    open( display );

    return stepname;
  }

  private void open( final Display display ) {
    shell.pack();
    final int height = shell.computeSize( SHELL_WIDTH, SWT.DEFAULT ).y;
    // for some reason the actual width and minimum width are smaller than what is requested - add the
    // SHELL_WIDTH_OFFSET to get the desired size
    shell.setMinimumSize( SHELL_WIDTH + BaseDialog.SHELL_WIDTH_OFFSET, height );
    shell.setSize( SHELL_WIDTH + BaseDialog.SHELL_WIDTH_OFFSET, height );

    getData( meta );
    meta.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  private void buildHeader() {

    buildPreHeader();

    // Step icon
    final Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    wicon.setLayoutData( new FormDataBuilder().top( 0, -BaseDialog.LABEL_SPACING ).right( 100, 0 ).result() );
    props.setLook( wicon );

    // Step name label
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "CommonStepDialog.Stepname.Label" ) ); //$NON-NLS-1$
    props.setLook( wlStepname );
    fdlStepname = new FormDataBuilder().left( 0, 0 ).top( 0, -BaseDialog.LABEL_SPACING ).result();
    wlStepname.setLayoutData( fdlStepname );

    // Step name field
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    wStepname.addSelectionListener( lsDef );
    fdStepname = new FormDataBuilder().width( BaseDialog.MEDIUM_FIELD ).left( 0, 0 ).top(
      wlStepname, BaseDialog.LABEL_SPACING ).result();
    wStepname.setLayoutData( fdStepname );

    // horizontal separator between step name and tabs
    headerSpacer = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    props.setLook( headerSpacer );
    headerSpacer.setLayoutData( new FormDataBuilder().left().right( 100, 0 ).top(
      wStepname, BaseDialog.MARGIN_SIZE ).width( SHELL_WIDTH - 2 * ( BaseDialog.MARGIN_SIZE ) ).result() );

    buildPostHeader();
  }

  /**
   * Called at the top of {@link #buildHeader()}, can be overridden to build additional "header" elements.
   */
  protected void buildPreHeader() {
    // override
  }

  /**
   * Called at the bottom of {@link #buildHeader()}, can be overridden to build additional "header" elements.
   */
  protected void buildPostHeader() {
    // override
  }

  protected abstract void buildBody();

  private void buildFooter() {

    buildPreFooter();

    buildCancelButton();
    buildOkButton();

    footerSpacer = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    footerSpacer.setLayoutData( new FormDataBuilder().left().bottom(
      wCancel, -BaseDialog.MARGIN_SIZE ).right( 100, 0 ).result() );

    buildPostFooter();
  }

  /**
   * Called at the top of {@link #buildFooter()}, can be overridden to build additional "footer" elements.
   */
  protected void buildPreFooter() {
    // override
  }

  /**
   * Called at the bottom of {@link #buildFooter()}, can be overridden to build additional "footer" elements.
   */
  protected void buildPostFooter() {
    // override
  }

  protected Button buildPreviewButton() {

    wPreview = new Button( shell, SWT.PUSH | SWT.CENTER );
    updatePreviewButtonStatus();
    wPreview.setText( BaseMessages.getString( PKG, "System.Button.Preview" ) ); //$NON-NLS-1$
    wPreview.pack();
    props.setLook( wPreview );
    wPreview.setLayoutData( new FormDataBuilder().bottom().left( 50, -( wPreview.getBounds().width / 2 ) ).result() );
    wPreview.addListener( SWT.Selection, lsPreview );
    return wPreview;
  }

  protected Button buildGetFieldsButton( final Composite parent, final SelectionAdapter listener ) {
    // get fields button
    wGet = new Button( parent, SWT.PUSH );
    updateGetFieldsButtonStatus();
    wGet.setText( BaseMessages.getString( PKG, "CommonStepDialog.Button.GetFields" ) ); //$NON-NLS-1$
    props.setLook( wGet );
    wGet.setLayoutData( new FormDataBuilder().right( 100, 0 ).bottom( 100, 0 ).result() );
    wGet.addSelectionListener( listener );
    return wGet;
  }

  protected Button buildCancelButton() {

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) ); //$NON-NLS-1$
    wCancel.setLayoutData( new FormDataBuilder().bottom().right( 100, 0 ).result() );
    wCancel.addListener( SWT.Selection, lsCancel );
    return wCancel;
  }

  protected Button buildOkButton() {

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) ); //$NON-NLS-1$
    wOK.setLayoutData( new FormDataBuilder().bottom().right(
      wCancel, Const.isOSX() ? 0 : -BaseDialog.LABEL_SPACING ).result() );
    wOK.addListener( SWT.Selection, lsOK );
    return wOK;
  }

  /**
   * To be overridden by a preview-capable dialog, returns true by default.
   *
   * @return true by default
   */
  protected boolean fieldsExist() {
    return true;
  }

  private void preview() {

    // given that the preview button is disabled in the absence of fields, this should never occur, but we check, for
    // good measure
    if ( !fieldsExist() ) {
      openNoFieldsDialog();
      return;
    }

    // Create the XML meta step
    final StepMetaType populatedMeta = (StepMetaType) getPopulatedMeta();

    final TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(
      transMeta, populatedMeta, wStepname.getText() );

    final EnterNumberDialog numberDialog = new EnterNumberDialog( shell,
      props.getDefaultPreviewSize(), BaseMessages.getString( PKG,
      "CommonStepDialog.PreviewSize.DialogTitle" ), //$NON-NLS-1$
      BaseMessages.getString( PKG, "CommonStepDialog.PreviewSize.DialogMessage" ) ); //$NON-NLS-1$
    final int previewSize = numberDialog.open();
    if ( previewSize > 0 ) {
      final TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(
        shell, previewMeta, new String[] { wStepname.getText() },
        new int[] { previewSize } );
      progressDialog.open( false );

      final Trans trans = progressDialog.getTrans();
      final String loggingText = progressDialog.getLoggingText();

      if ( !progressDialog.isCancelled() ) {
        if ( trans.getResult() != null ) {
          if ( trans.getResult().getNrErrors() > 0 ) {
            openPreviewError();
            // there are errors - return
            return;
          }
        }
      }

      final List previewRows = progressDialog.getPreviewRows( wStepname.getText() );
      if ( previewRows == null || previewRows.size() == 0 ) {
        openNoRowsToPreviewError();
      } else {
        final PreviewRowsDialog prd = new PreviewRowsDialog( shell, transMeta, SWT.NONE,
          wStepname.getText(), progressDialog.getPreviewRowsMeta( wStepname
          .getText() ), previewRows,
          loggingText );
        prd.open();
      }
    }
  }

  /**
   * To be overridden by a preview-capable step dialog.
   */
  protected void updatePreviewButtonStatus() {
    // no-op
  }

  /**
   * Can be overridden by the implementing class, if the "Get fields" button is to be enabled only under certain
   * conditions.
   */
  protected void updateGetFieldsButtonStatus() {
    // override
  }

  protected void ok() {
    if ( StringUtils.isEmpty( wStepname.getText().trim() ) ) {
      return;
    }
    stepname = wStepname.getText();
    populateMeta( meta );
    dispose();
  }

  protected void cancel() {
    stepname = null;
    meta.setChanged( changed );
    dispose();
  }

  protected Image getImage() {
    final PluginInterface plugin =
      PluginRegistry.getInstance().getPlugin( StepPluginType.class, stepMeta.getStepMetaInterface() );
    final String id = plugin.getIds()[ 0 ];
    if ( id != null ) {
      return GUIResource.getInstance().getImagesSteps().get( id ).getAsBitmapForSize( shell.getDisplay(),
        ConstUI.LARGE_ICON_SIZE, ConstUI.LARGE_ICON_SIZE );
    }
    return null;
  }

  /**
   * Returns a new instance of {@link StepMetaType} that is populated according to dialog selection.
   *
   * @return a new instance of {@link StepMetaType} that is populated according to dialog selection
   */
  protected abstract StepMetaType getPopulatedMeta();

  /**
   * Creates a new instance of {@link StepMetaType} and populates it with provided data from the dialog.
   *
   * @param meta a new instance of {@link StepMetaType}
   */
  protected abstract void populateMeta( final StepMetaType meta );

  protected abstract String getTitle();

  /**
   * Copy information from the {@link StepMetaType} meta to the dialog fields.
   */
  public abstract void getData( final StepMetaType meta );

  protected CTabFolder buildTabFolder() {
    m_wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( m_wTabFolder, Props.WIDGET_STYLE_TAB );
    m_wTabFolder.setSimple( false );
    return m_wTabFolder;
  }

  protected void layoutTabFolder() {
    m_wTabFolder.setSelection( 0 );
    m_wTabFolder.setLayoutData( new FormDataBuilder().left().top(
      headerSpacer, BaseDialog.MARGIN_SIZE ).right( 100, 0 ).bottom( new FormAttachment(
        footerSpacer, -BaseDialog.MARGIN_SIZE ) ).result() );
  }

  protected void openDialog( final String title, final String message, final int dialogType ) {
    final Dialog dialog = new SimpleMessageDialog( shell, title, message, dialogType );
    dialog.open();
  }

  protected void openPreviewError() {
    openDialog( BaseMessages.getString( PKG, "CommonStepDialog.ErrorMessage.PreviewError.Title" ), //$NON-NLS-1$
      BaseMessages.getString( PKG, "CommonStepDialog.ErrorMessage.PreviewError.Message" ), //$NON-NLS-1$
      MessageDialog.ERROR );
  }

  protected void openNoRowsToPreviewError() {
    openDialog( BaseMessages.getString( PKG, "CommonStepDialog.WarningMessage.NoPreview.Title" ), //$NON-NLS-1$
      BaseMessages.getString( PKG, "CommonStepDialog.WarningMessage.NoPreview.Message" ), //$NON-NLS-1$
      MessageDialog.WARNING );
  }

  protected void openNoFieldsDialog() {
    openDialog( BaseMessages.getString( PKG, "CommonStepDialog.WarningMessage.GetFieldsNoFields.Title" ), //$NON-NLS-1$
      BaseMessages.getString( PKG, "CommonStepDialog.WarningMessage.GetFieldsNoFields.Message" ), //$NON-NLS-1$
      MessageDialog.WARNING );
  }

  protected void openFieldsErrorDialog() {
    openDialog( BaseMessages.getString( PKG, "CommonStepDialog.ErrorMessage.GetFieldsError.Title" ), //$NON-NLS-1$
      BaseMessages.getString( PKG, "CommonStepDialog.ErrorMessage.GetFieldsError.Message" ), //$NON-NLS-1$
      MessageDialog.ERROR );
  }
}
