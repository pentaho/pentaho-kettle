/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.plugins.fileopensave.api.file.FileDetails;
import org.pentaho.di.plugins.fileopensave.api.providers.Directory;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.controllers.FileController;
import org.pentaho.di.plugins.fileopensave.providers.local.model.LocalFile;
import org.pentaho.di.plugins.fileopensave.providers.recents.model.RecentTree;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSTree;
import org.pentaho.di.plugins.fileopensave.service.FileCacheService;
import org.pentaho.di.plugins.fileopensave.service.ProviderServiceService;
import org.pentaho.di.ui.core.FileDialogOperation;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.events.dialog.ProviderFilterType;
import org.pentaho.di.ui.util.HelpUtils;
import org.pentaho.di.ui.util.SwtSvgImageUtil;


public class FileOpenSaveDialog extends Dialog implements FileDetails {
  private static final Class<?> PKG = FileOpenSaveDialog.class;

  public static final String STATE_SAVE = "save";
  public static final String STATE_OPEN = "open";
  public static final String SELECT_FOLDER = "selectFolder";
  // private Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private static final int OPTIONS = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX;
  private static final String HELP_URL =
          Const.getDocUrl( "Products/Work_with_transformations#Open_a_transformation" );

  public static final String PATH_PARAM = "path";
  public static final String USE_SCHEMA_PARAM = "useSchema";
  public static final String CONNECTION_PARAM = "connection";
  public static final String PROVIDER_PARAM = "provider";
  public static final String PROVIDER_FILTER_PARAM = "providerFilter";
  public static final String FILTER_PARAM = "filter";
  public static final String DEFAULT_FILTER_PARAM = "defaultFilter";
  public static final String CONNECTION_FILTER_PARAM = "connectionTypes";
  public static final String ORIGIN_PARAM = "origin";
  public static final String FILENAME_PARAM = "filename";
  public static final String FILE_TYPE_PARM = "fileType";
  public static final String OBJECT_ID_PARAM = "objectId";
  public static final String NAME_PARAM = "name";
  public static final String PARENT_PARAM = "parent";
  public static final String TYPE_PARAM = "type";



  private String shellTitle = "Open";
  private String objectId;
  private String name;
  private String path;
  private String parentPath;
  private String type;
  private String connection;
  private String provider;
  private String command = STATE_SAVE;
  private FileDialogOperation fileDialogOperation = new FileDialogOperation(command);

  private Text txtFileName;
  private LogChannelInterface log;
  private int width;
  private int height;

  // The left-hand tree viewer
  protected TreeViewer treeViewer;
  protected TableViewer fileTableViewer;

  private static final FileController FILE_CONTROLLER;

  static {
    FILE_CONTROLLER = new FileController( FileCacheService.INSTANCE.get(), ProviderServiceService.INSTANCE.get() );
  }

  public FileOpenSaveDialog( Shell parentShell, int width, int height, LogChannelInterface logger ) {
    super( parentShell );
    this.log = logger;
    this.width = width;
    this.height = height;
    setShellStyle( OPTIONS );
  }


  public void open(FileDialogOperation fileDialogOperation ) {

    try {
      String dialogPath = fileDialogOperation.getPath() != null
              ? fileDialogOperation.getPath()
              : fileDialogOperation.getStartDir();
    } catch (Exception ex) {

    }

    this.fileDialogOperation = fileDialogOperation;
    command = fileDialogOperation.getCommand();
    shellTitle = BaseMessages.getString( PKG, "FileOpenSaveDialog.dialog." + command + ".title");

    this.open();
    while ( !this.getShell().isDisposed() ) {
      if ( !getShell().getDisplay().readAndDispatch() ) {
        getShell().getDisplay().sleep();
      }
    }
  }

  @Override
  protected void configureShell( Shell newShell ) {
    // newShell.setImage( LOGO );
    newShell.setText( shellTitle );
    PropsUI.getInstance().setLook( newShell );
    newShell.setMinimumSize( 545, 458 );
  }

  @Override
  protected Point getInitialSize() {
    return new Point( width, height );
  }

  @Override
  protected Control createContents( Composite parent ) {

    FormLayout formLayout = new FormLayout();
    formLayout.marginTop = 20;
    formLayout.marginBottom = 25;

    parent.setLayout( formLayout );
    Composite header = createHeader( parent );
    header.setLayoutData( new FormDataBuilder().top( 0, 0 ).left( 0, 0 ).right( 100, 0 ).result() );
    Composite buttons = createButtonsBar( parent );
    buttons.setLayoutData( new FormDataBuilder().top( header, 25 ).left( 0, 0 ).right( 100, 0 ).result() );

    FlatButton helpButton =
            new FlatButton( parent, SWT.NONE )
                    .setEnabledImage( rasterImage( "img/help.svg", 24, 24 ) )
                    .setDisabledImage( rasterImage( "img/help.svg", 24, 24 ) )
                    .setEnabled( true )
                    .setLayoutData( new FormDataBuilder().bottom( 100, 0 ).left( 0, 20 ).result() );
    helpButton.getLabel().setText(BaseMessages.getString( PKG,  "file-open-save-plugin.app.help.label"));

    Composite select = createFilesBrowser( parent );
    select.setLayoutData( new FormDataBuilder().top( buttons, 15 ).left( 0, 0 ).right( 100, 0 )
            .bottom( helpButton.getLabel(), -20 ).result() );

    Combo comboFilter = new Combo(parent, SWT.NONE);
    Label labelComboFilter = new Label(parent, SWT.NONE);
    labelComboFilter.setText(BaseMessages.getString(PKG, "file-open-save-plugin.app.save.file-filter.label"));
    PropsUI.getInstance().setLook(labelComboFilter);
    PropsUI.getInstance().setLook(comboFilter);
    String filters = fileDialogOperation.getFilter() != null ? fileDialogOperation.getFilter() : fileDialogOperation.getDefaultFilter();

    if(filters != null) {
      String[] splitFilters = filters.split(" ");
      if(splitFilters.length > 0) {
        comboFilter.setItems(splitFilters);

      } else {
        comboFilter.setItems(filters);
      }
    } else {
      comboFilter.setItems(new String[]{});
      comboFilter.setText("All Files"); //TODO: Solve why `fileDialogOperation.getDefaultFilter()` is null
    }
    if (command.equalsIgnoreCase(STATE_SAVE)) {
      txtFileName = new Text(parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
      Label filenameLabel = new Label(parent, SWT.NONE);

      filenameLabel.setText(BaseMessages.getString(PKG, "file-open-save-plugin.app.save.file-name.label"));

      PropsUI.getInstance().setLook(filenameLabel);
      PropsUI.getInstance().setLook(txtFileName);

      txtFileName.setSize(40, 40); // TODO: Figure out how to set size correctly


      filenameLabel.setLayoutData(new FormDataBuilder().top(select, 20).left(helpButton.getLabel(), 150).result());
      txtFileName.setLayoutData(new FormDataBuilder().top(select, 20).left(filenameLabel, 5).result());
      labelComboFilter.setLayoutData(new FormDataBuilder().top(select, 20).left(txtFileName, 25).result());
      comboFilter.setLayoutData(new FormDataBuilder().top(select, 20).left(labelComboFilter, 5).result());

    }
    Button btnCancel = new Button(parent, SWT.NONE);
    PropsUI.getInstance().setLook(btnCancel);

    btnCancel.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(SelectionEvent selectionEvent) {
        parentPath = null;
        type = null;
        provider = null;

        getShell().dispose();
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent selectionEvent) {

      }
    });

    if (this.command.equalsIgnoreCase(STATE_OPEN)) {
      Button btnOpen = new Button(parent, SWT.NONE);
      PropsUI.getInstance().setLook(btnOpen);
      labelComboFilter.setLayoutData(new FormDataBuilder().top(select, 20).right(comboFilter, -5).result());
      comboFilter.setLayoutData(new FormDataBuilder().top(select, 20).right(btnOpen, -15).result());

      btnOpen.setLayoutData(new FormDataBuilder().top(select, 20).right(btnCancel, -15).result());
      btnOpen.setText(BaseMessages.getString(PKG, "file-open-save-plugin.app.open.button"));
      btnOpen.addSelectionListener(new SelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent selectionEvent) {
          StructuredSelection structuredSelection = (StructuredSelection) fileTableViewer.getSelection();
          File file = (File) structuredSelection.getFirstElement();
          if (file != null) {
            parentPath = file.getParent();
            type = file.getType();
            provider = file.getProvider();

            getShell().dispose();
          } else {
            // TODO: Display something?
          }
        }


        @Override
        public void widgetDefaultSelected(SelectionEvent selectionEvent) {

        }
      });
      btnCancel.setLayoutData(new FormDataBuilder().top(select, 20).right(100, -30).result());
      btnCancel.setText(BaseMessages.getString(PKG, "file-open-save-plugin.app.cancel.button"));
    }
    else if (this.command.equalsIgnoreCase(FileDialogOperation.SAVE))
    {


      Button btnSave = new Button(parent, SWT.NONE);
      PropsUI.getInstance().setLook(btnSave);
      btnSave.setLayoutData(new FormDataBuilder().top(select, 20).left(comboFilter, 30).result());
      btnSave.setText(BaseMessages.getString( PKG, "file-open-save-plugin.app.save.button"));
      btnSave.addSelectionListener(new SelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent selectionEvent) {
          // TODO: Make behavior mimic original thin client
          StructuredSelection structuredSelection = (StructuredSelection) fileTableViewer.getSelection();
          Directory directory = (Directory) structuredSelection.getFirstElement();

          if (directory != null) {

            parentPath = directory.getParent();
            type = fileDialogOperation.getFileType();
            path = directory.getPath();
            name = txtFileName.getText().contains(".") ? txtFileName.getText().split(".")[0] : txtFileName.getText();
            provider = directory.getProvider();

            getShell().dispose();
          } else {
            // TODO: Display something informing the user
          }
        }


        @Override
        public void widgetDefaultSelected(SelectionEvent selectionEvent) {

        }


      });
      btnCancel.setLayoutData(new FormDataBuilder().top(select, 20).left(btnSave, 15).result());
      btnCancel.setText(BaseMessages.getString(PKG, "file-open-save-plugin.app.cancel.button"));
    }
    return parent;
  }

  public Composite createHeader( Composite parent ) {
    Composite headerComposite = new Composite( parent, SWT.NONE );

    FormLayout formLayout = new FormLayout();
    formLayout.marginLeft = 20;
    formLayout.marginRight = 20;

    headerComposite.setLayout( formLayout );
    PropsUI.getInstance().setLook( headerComposite );

    Label lblSelect = new Label( headerComposite, SWT.LEFT );
    PropsUI.getInstance().setLook( lblSelect );

    lblSelect.setText(StringUtils.capitalize(shellTitle));


    FontData[] fontData = lblSelect.getFont().getFontData();
    Arrays.stream( fontData ).forEach( fd -> fd.height = 20 );
    final Font bigFont = new Font( getShell().getDisplay(), fontData );
    lblSelect.setFont( bigFont );
    lblSelect.addDisposeListener( ( e ) -> bigFont.dispose() );
    lblSelect.setLayoutData( new FormDataBuilder().result() );

    // TODO: A whole bunch more with search function
    final Color WHITE = new Color( getShell().getDisplay(), 255, 255, 255 );
    Composite searchComp = new Composite( headerComposite, SWT.BORDER );
    PropsUI.getInstance().setLook( searchComp );
    searchComp.addDisposeListener( ( e ) -> WHITE.dispose() );
    searchComp.setLayoutData( new FormDataBuilder().right( 100, 0 ).result() );
    searchComp.setBackground( WHITE );

    RowLayout searchLayout = new RowLayout();
    searchLayout.center = true;
    searchComp.setLayout( searchLayout );

    Label lblSearch = new Label( searchComp, SWT.NONE );
    PropsUI.getInstance().setLook( lblSearch );
    lblSearch.setLayoutData( new RowData() );
    lblSearch.setBackground( WHITE );
    lblSearch.setImage( rasterImage( "img/Search.S_D.svg", 25, 25 ) );

    RowData rd = new RowData();
    rd.width = 200;
    Text txtSearch = new Text( searchComp, SWT.NONE );
    PropsUI.getInstance().setLook( txtSearch );
    txtSearch.setBackground( WHITE );
    txtSearch.setLayoutData( rd );

    headerComposite.layout();

    return headerComposite;
  }

  private Composite createButtonsBar( Composite parent ) {
    Composite buttons = new Composite( parent, SWT.NONE );
    PropsUI.getInstance().setLook( buttons );

    FormLayout formLayout = new FormLayout();
    formLayout.marginLeft = 20;
    formLayout.marginRight = 20;
    buttons.setLayout( formLayout );

    FlatButton backButton =
            new FlatButton( buttons, SWT.NONE )
                    .setEnabledImage( rasterImage( "img/Backwards.S_D.svg", 32, 32 ) )
                    .setDisabledImage( rasterImage( "img/Backwards.S_D_disabled.svg", 32, 32 ) )
                    .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.back.button" ) )
                    .setEnabled( false );
    FlatButton forwardButton =
            new FlatButton( buttons, SWT.NONE )
                    .setEnabledImage( rasterImage( "img/Forwards.S_D.svg", 32, 32 ) )
                    .setDisabledImage( rasterImage( "img/Forwards.S_D_disabled.svg", 32, 32 ) )
                    .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.forward.button" ) )
                    .setEnabled( true )
                    .setLayoutData( new FormDataBuilder().left( backButton.getLabel(), 0 ).result() );

    Composite fileButtons = new Composite( buttons, SWT.NONE );
    PropsUI.getInstance().setLook( fileButtons );
    fileButtons.setLayout( new RowLayout() );
    fileButtons.setLayoutData( new FormDataBuilder().right( 100, 0 ).result() );

    FlatButton upButton =
            new FlatButton( fileButtons, SWT.NONE )
                    .setEnabledImage( rasterImage( "img/Up_Folder.S_D.svg", 32, 32 ) )
                    .setDisabledImage( rasterImage( "img/Up_Folder.S_D_disabled.svg", 32, 32 ) )
                    .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.up-directory.button" ) )

                    .setLayoutData( new RowData() )
                    .setEnabled( true );

    FlatButton addButton =
            new FlatButton( fileButtons, SWT.NONE )
                    .setEnabledImage( rasterImage( "img/New_Folder.S_D.svg", 32, 32 ) )
                    .setDisabledImage( rasterImage( "img/New_Folder.S_D_disabled.svg", 32, 32 ) )
                    .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.add-folder.button" ) )
                    .setLayoutData( new RowData() )
                    .setEnabled( false );

    FlatButton deleteButton =
            new FlatButton( fileButtons, SWT.NONE )
                    .setEnabledImage( rasterImage( "img/Close.S_D.svg", 32, 32 ) )
                    .setDisabledImage( rasterImage( "img/Close.S_D_disabled.svg", 32, 32 ) )
                    .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.delete.button" ) )
                    .setLayoutData( new RowData() )
                    .setEnabled( false );

    FlatButton refreshButton =
            new FlatButton( fileButtons, SWT.NONE )
                    .setEnabledImage( rasterImage( "img/Refresh.S_D.svg", 32, 32 ) )
                    .setDisabledImage( rasterImage( "img/Refresh.S_D_disabled.svg", 32, 32 ) )
                    .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.refresh.button" ) )
                    .setLayoutData( new RowData() )
                    .setEnabled( true );

    Composite navComposite = new Composite( buttons, SWT.BORDER );
    PropsUI.getInstance().setLook( navComposite );
    navComposite.setBackground( getShell().getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
    navComposite.setLayoutData(
            new FormDataBuilder().left( forwardButton.getLabel(), 10 ).right( fileButtons, -10 ).height( 32 ).result() );

    return buttons;
  }

  private Composite createFilesBrowser( Composite parent ) {
    Composite browser = new Composite( parent, SWT.NONE );
    PropsUI.getInstance().setLook( browser );
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginRight = 0;
    gridLayout.marginLeft = 0;
    browser.setLayout( gridLayout );

    SashForm sashForm = new SashForm( browser, SWT.HORIZONTAL );
    PropsUI.getInstance().setLook( sashForm );
    sashForm.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    treeViewer = new TreeViewer( sashForm, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION );
    PropsUI.getInstance().setLook( treeViewer.getTree() );

    Image imgTime = rasterImage( "img/Time.S_D.svg", 25, 25 );
    Image imgVFS = rasterImage( "img/VFS_D.svg", 25, 25 );
    Image imgFolder = rasterImage( "img/file_icons/Archive.S_D.svg", 25, 25 );
    Image imgDisk = rasterImage( "img/Disk.S_D.svg", 25, 25 );
    Image imgFile = rasterImage( "img/file_icons/Doc.S_D.svg", 25, 25 );
    Color clrGray = getShell().getDisplay().getSystemColor( SWT.COLOR_GRAY );

    treeViewer.setLabelProvider( new LabelProvider() {
      @Override
      public String getText( Object element ) {
        if ( element instanceof Tree ) {
          return ( (Tree) element ).getName();
        } else if ( element instanceof Directory ) {
          return ( (Directory) element ).getName();
        } else if ( element instanceof File ) {
          return ( (File) element ).getName();
        }
        return null;
      }

      @Override
      public Image getImage( Object element ) {
        if ( element instanceof Tree ) {
          if( element instanceof RecentTree ) {
            return imgTime;
          } else if ( element instanceof VFSTree ) {
            return imgVFS;
          }
          return imgDisk;
        } else if ( element instanceof Directory ) {
          return imgFolder;
        }
        return null;
      }
    } );

    treeViewer.setContentProvider( new FileTreeContentProvider( FILE_CONTROLLER ) );

    // Load the various file types on the left
    treeViewer.setInput( FILE_CONTROLLER.load( ProviderFilterType.ALL_PROVIDERS.toString() ).toArray() );

    treeViewer.addPostSelectionChangedListener( e -> {
      IStructuredSelection selection = (IStructuredSelection) e.getSelection();
      Object selectedNode = selection.getFirstElement();
      // Expand the selection in the treeviewer
      if( !treeViewer.getExpandedState( selectedNode ) ) {
        treeViewer.setExpandedState( selectedNode, true );
      }
      // Update the path that is selected
      selectPath( selectedNode );
    } );

    fileTableViewer = new TableViewer( sashForm, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION );
    PropsUI.getInstance().setLook( fileTableViewer.getTable() );
    fileTableViewer.getTable().setHeaderVisible( true );

    TableViewerColumn tvcName = new TableViewerColumn( fileTableViewer, SWT.NONE );
    tvcName.getColumn().setText( BaseMessages.getString( PKG, "file-open-save-plugin.files.name.header" ) );
    tvcName.getColumn().setWidth( 250 );

    ColumnLabelProvider clpName = new ColumnLabelProvider() {

      @Override
      public String getText( Object element ) {
        File f = (File) element;
        return f.getName();
      }

      @Override
      public Image getImage( Object element ) {
        if ( element instanceof Directory ) {
          return imgFolder;
        } else if ( element instanceof File ) {
          return imgFile;
        }
        return null;
      }

    };

    tvcName.setLabelProvider( clpName );

    TableViewerColumn tvcType = new TableViewerColumn( fileTableViewer, SWT.NONE );
    tvcType.getColumn().setText( BaseMessages.getString( PKG, "file-open-save-plugin.files.type.header" ) );
    tvcType.getColumn().setWidth( 100 );
    tvcType.getColumn().setResizable( false );
    tvcType.setLabelProvider( new ColumnLabelProvider() {
      @Override
      public Color getForeground( Object element ) {
        return clrGray;
      }

      @Override
      public String getText( Object element ) {
        return super.getText( StringUtils.capitalize( ( (File) element ).getType() ) );
      }
    } );

    TableViewerColumn tvcModified = new TableViewerColumn( fileTableViewer, SWT.NONE );
    tvcModified.getColumn().setText( BaseMessages.getString( PKG, "file-open-save-plugin.files.modified.header" ) );
    tvcModified.getColumn().setWidth( 140 );
    tvcModified.getColumn().setResizable( false );

    tvcModified.setLabelProvider( new ColumnLabelProvider() {
      SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yy hh:mm aa" );

      @Override
      public Color getForeground( Object element ) {
        return clrGray;
      }

      @Override
      public String getText( Object element ) {
        try {
          return super.getText( sdf.format( ( (File) element ).getDate() ) );
        } catch ( Exception e ) {
          return "";
        }
      }
    } );

    fileTableViewer.getTable().addListener( SWT.Resize, ( e ) -> {
      Rectangle r = fileTableViewer.getTable().getClientArea();
      tvcName.getColumn()
              .setWidth( Math.max( 150, r.width - tvcType.getColumn().getWidth() - tvcModified.getColumn().getWidth() ) );

    } );

    fileTableViewer.setContentProvider(new ArrayContentProvider() );
    fileTableViewer.addDoubleClickListener( ( e ) -> {
      Object selection = ( (IStructuredSelection ) e.getSelection() ).getFirstElement();

      if( selection != null && selection instanceof Directory  ) {
        treeViewer.setExpandedState( selection, true );
        treeViewer.setSelection( new StructuredSelection( selection ), true );
      } else if (selection != null && selection instanceof File) {
        // TODO: Make this work for more than just the `LocalFileProvider`
        File f = (File) selection;

        path = f.getPath();
        parentPath = f.getParent();
        type = f.getType();
        connection = this.fileDialogOperation.getConnection();
        provider = f.getProvider();
      }
    });


    fileTableViewer.addDoubleClickListener( ( e ) -> {
      LocalFile localFileRoot = new LocalFile();
      localFileRoot.setPath( "/" ); // TODO: Check if necessary

      treeViewer.expandToLevel( localFileRoot, TreeViewer.ALL_LEVELS );
    });

    sashForm.setWeights( new int[] {
            1, 2 } );

    return browser;
  }

  private Image rasterImage( String path, int width, int height ) {
    SwtUniversalImage img =
            SwtSvgImageUtil.getUniversalImage( getShell().getDisplay(), getClass().getClassLoader(), path );
    Image image = img.getAsBitmapForSize( getShell().getDisplay(), width, height );
    getShell().addDisposeListener( ( e ) -> {
      img.dispose();
      image.dispose();
    } );
    return image;
  }

  private void openHelpDialog() {
    Program.launch( HELP_URL );
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId( String objectId ) {
    this.objectId = objectId;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public String getConnection() {
    return connection;
  }

  public void setConnection( String connection ) {
    this.connection = connection;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider( String provider ) {
    this.provider = provider;
  }

  public String getParentPath() {
    return parentPath;
  }

  public void setParentPath( String parentPath ) {
    this.parentPath = parentPath;
  }

  protected void selectPath( Object selectedElement ) {
    selectPath( selectedElement, true );
  }

  protected void selectPath( Object selectedElement, boolean useCache ) {

    if ( selectedElement instanceof Tree ) {

      List<Object> children = ( (Tree) selectedElement ).getChildren();
      if ( children != null ) {
        fileTableViewer.setInput( children.toArray() );
      }
    } else if ( selectedElement instanceof Directory ) {
      try {
        fileTableViewer.setInput( FILE_CONTROLLER.getFiles( (File) selectedElement, null, useCache ).stream()
                .sorted( Comparator.comparing( f -> f instanceof Directory, Boolean::compare ).reversed()
                        .thenComparing( Comparator.comparing( f -> ( (File) f ).getName(), String.CASE_INSENSITIVE_ORDER ) ) )
                .toArray() );
      } catch ( FileException e ) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }

  protected static class FlatButton {

    private CLabel label;

    private AtomicBoolean enabled = new AtomicBoolean( true );

    private Color hoverColor;
    private Image enabledImage;
    private Image disabledImage;

    public FlatButton( Composite parent, int style ) {
      label = new CLabel( parent, style );
      PropsUI.getInstance().setLook( label );
      setEnabled( true );
      setHoverColor( parent.getDisplay().getSystemColor( SWT.COLOR_GRAY ) );

      label.addMouseTrackListener( new MouseTrackAdapter() {

        private Color origColor;

        @Override
        public void mouseEnter( MouseEvent arg0 ) {
          origColor = label.getBackground();
          if ( enabled.get() ) {
            label.setBackground( hoverColor );
          }
        }

        @Override
        public void mouseExit( MouseEvent e ) {
          if ( origColor != null ) {
            label.setBackground( origColor );
          }
        }

      } );
    }

    public CLabel getLabel() {
      return label;
    }

    public boolean isEnabled() {
      return enabled.get();
    }

    public FlatButton setEnabled( boolean enabled ) {

      if ( disabledImage != null && enabledImage != null ) {
        label.setImage( enabled ? enabledImage : disabledImage );
      } else if ( enabledImage != null && disabledImage == null ) {
        label.setImage( enabledImage );
      } else if ( enabledImage == null && disabledImage != null ) {
        label.setImage( disabledImage );
      }
      label.redraw();

      this.enabled.set( enabled );
      return this;

    }

    public Image getEnabledImage() {
      return enabledImage;
    }

    public FlatButton setEnabledImage( Image enabledImage ) {
      this.enabledImage = enabledImage;
      return this;
    }

    public Image getDisabledImage() {
      return disabledImage;
    }

    public FlatButton setDisabledImage( Image disabledImage ) {
      this.disabledImage = disabledImage;
      return this;
    }

    public FlatButton setToolTipText( String toolTipText ) {
      label.setToolTipText( toolTipText );
      return this;
    }

    public Color getHoverColor() {
      return hoverColor;
    }

    public FlatButton setHoverColor( Color hoverColor ) {
      this.hoverColor = hoverColor;
      return this;
    }

    public FlatButton setLayoutData( Object o ) {
      label.setLayoutData( o );
      return this;
    }

  }

  protected static class FileTreeContentProvider implements ITreeContentProvider {

    private final FileController fileController;

    public FileTreeContentProvider( FileController fileController ) {
      this.fileController = fileController;
    }

    @Override
    public Object[] getElements( Object inputElement ) {
      return (Object[]) inputElement;
    }

    @Override
    public Object[] getChildren( Object parentElement ) {

      if ( parentElement instanceof Tree ) {

        Tree parentTree = (Tree) parentElement;
        if( parentTree.isHasChildren() ) {
          return ( parentTree ).getChildren().toArray();
        }
      } else if ( parentElement instanceof Directory ) {
        try {
          return fileController.getFiles( (Directory) parentElement, null, true ).stream()
                  .filter( f -> f instanceof Directory )
                  .sorted( Comparator.comparing( f -> f.getName(), String.CASE_INSENSITIVE_ORDER ) ).toArray();
        } catch ( FileException e ) {
          // TODO: Error message that something went wrong
        }
      }

      return new Object[0];
    }

    @Override
    public Object getParent( Object element ) {

      return null;
    }

    @Override
    public boolean hasChildren( Object element ) {
      if ( element instanceof Tree ) {
        return ( (Tree) element ).isHasChildren();
      } else if ( element instanceof Directory ) {
        return ( (Directory) element ).isHasChildren();
      }
      return false;
    }

    @Override
    public void dispose() {
      // TODO Auto-generated method stub

    }

    @Override
    public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
      // TODO Auto-generated method stub

    }

  }

}
