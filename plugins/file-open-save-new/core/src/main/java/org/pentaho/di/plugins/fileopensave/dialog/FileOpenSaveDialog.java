/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2022 by Hitachi Vantara : http://www.pentaho.com
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


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TypedListener;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.plugins.fileopensave.api.file.FileDetails;
import org.pentaho.di.plugins.fileopensave.api.providers.Directory;
import org.pentaho.di.plugins.fileopensave.api.providers.Entity;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.FileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.Utils;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.controllers.FileController;
import org.pentaho.di.plugins.fileopensave.providers.local.model.LocalFile;
import org.pentaho.di.plugins.fileopensave.providers.recents.model.RecentTree;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryFile;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSFile;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSTree;
import org.pentaho.di.plugins.fileopensave.service.FileCacheService;
import org.pentaho.di.plugins.fileopensave.service.ProviderServiceService;
import org.pentaho.di.ui.core.FileDialogOperation;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterStringDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.events.dialog.ProviderFilterType;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class FileOpenSaveDialog extends Dialog implements FileDetails {
  private static final Class<?> PKG = FileOpenSaveDialog.class;

  private final Image logo = GUIResource.getInstance().getImageLogoSmall();
  private static final int OPTIONS = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX;
  private static final String HELP_URL = Const.getDocUrl( "Products/Work_with_transformations#Open_a_transformation" );
  private static final String FILE_EXTENSION_RESOURCE_PATH = "extensions/supported_file_filters.json";

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
  private static final String ALL_FILE_TYPES = "ALL";
  private static final String FILE_PERIOD = ".";
  private FilterFileType[] validFileTypes;
  private String shellTitle = "Open";
  private String objectId;
  private String name;
  private String path;
  private String parentPath;
  private String type;
  private String connection;
  private String provider;
  private String command = FileDialogOperation.OPEN;
  private FileDialogOperation fileDialogOperation = new FileDialogOperation( command );

  private Text txtFileName;
  private LogChannelInterface log;
  private int width;
  private int height;

  // The left-hand tree viewer
  protected TreeViewer treeViewer;
  protected TableViewer fileTableViewer;

  protected Text txtSearch;

  private static final FileController FILE_CONTROLLER;

  private Label lblComboFilter;

  private TypedComboBox<FilterFileType> typedComboBox;

  private Text txtNav;

  // Buttons
  private Button btnSave;

  private Button btnOpen;
  private Button btnCancel;

  // Colors
  private Color clrGray;
  private Color clrBlack;

  // Images
  private Image imgTime;
  private Image imgVFS;
  private Image imgFolder;
  private Image imgDisk;
  private Image imgFile;

  // Dialogs

  private EnterStringDialog enterStringDialog;

  // Top Right Buttons
  private FlatButton flatBtnAdd;

  private FlatButton flatBtnRefresh;

  private FlatButton flatBtnUp;

  static {
    FILE_CONTROLLER = new FileController( FileCacheService.INSTANCE.get(), ProviderServiceService.get() );
  }

  public FileOpenSaveDialog( Shell parentShell, int width, int height, LogChannelInterface logger ) {
    super( parentShell );
    this.log = logger;
    this.width = width;
    this.height = height;
    setShellStyle( OPTIONS );
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream( FILE_EXTENSION_RESOURCE_PATH );
      String jsonString = new BufferedReader(
        new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) )
        .lines()
        .collect( Collectors.joining( "\n" ) );
      validFileTypes = objectMapper.readValue( jsonString, FilterFileType[].class );
    } catch ( Exception ex ) {
      log.logError( "Could not load resource", ex );
    }
  }

  public void open( FileDialogOperation fileDialogOperation ) {

    this.fileDialogOperation = fileDialogOperation;
    command = fileDialogOperation.getCommand();
    shellTitle = BaseMessages.getString( PKG, "FileOpenSaveDialog.dialog." + command + ".title" );
    open();
    if ( getShell() != null ) {
      while ( !getShell().isDisposed() ) {
        if ( !getShell().getDisplay().readAndDispatch() ) {
          getShell().getDisplay().sleep();
        }
      }
    } else {
      clearState();
    }
  }

  LabelProvider labelProvider = new LabelProvider() {
    @Override public String getText( Object element ) {
      if ( element instanceof Tree ) {
        return ( (Tree) element ).getName();
      } else if ( element instanceof Directory ) {
        return ( (Directory) element ).getName();
      } else if ( element instanceof File ) {
        return ( (File) element ).getName();
      }
      return null;
    }

    @Override public Image getImage( Object element ) {
      if ( element instanceof Tree ) {
        if ( element instanceof RecentTree ) {
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
  };

  @Override protected void configureShell( Shell newShell ) {
    newShell.setImage( logo );
    newShell.setText( shellTitle );
    PropsUI.getInstance().setLook( newShell );
    newShell.setMinimumSize( 545, 458 );
  }

  @Override protected Point getInitialSize() {
    return new Point( width, height );
  }

  protected void createOpenLayout( Composite parent, Composite select ) {
    btnOpen = new Button( parent, SWT.NONE );
    btnOpen.setEnabled( false );
    PropsUI.getInstance().setLook( btnOpen );
    lblComboFilter.setLayoutData(
      new FormDataBuilder().top( select, 20 ).right( typedComboBox.viewer.getCombo(), -5 ).result() );
    typedComboBox.viewer.getCombo()
      .setLayoutData( new FormDataBuilder().top( select, 20 ).right( btnOpen, -15 ).result() );

    btnOpen.setLayoutData( new FormDataBuilder().top( select, 20 ).right( btnCancel, -15 ).result() );
    btnOpen.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.open.button" ) );
    btnOpen.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {

        if ( command.equals( FileDialogOperation.SELECT_FILE ) || command.equals( FileDialogOperation.OPEN ) ) {
          if ( StringUtils.isNotEmpty( name ) ) {
            getShell().dispose();
          }
        } else if ( command.equals( FileDialogOperation.SELECT_FOLDER ) ) {
          if ( StringUtils.isNotEmpty( path ) ) {
            getShell().dispose();
          }
        } else if ( command.equals( FileDialogOperation.SELECT_FILE_FOLDER ) ) {
          if ( StringUtils.isNotEmpty( path ) || StringUtils.isNotEmpty( name ) ) {
            getShell().dispose();
          }
        } else {
          // TODO: Display something
        }
      }
    } );
    btnCancel.setLayoutData( new FormDataBuilder().top( select, 20 ).right( 100, -30 ).result() );
    btnCancel.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.cancel.button" ) );

  }

  @Override protected Control createContents( Composite parent ) {

    FormLayout formLayout = new FormLayout();
    formLayout.marginTop = 20;
    formLayout.marginBottom = 25;

    parent.setLayout( formLayout );
    Composite header = createHeader( parent );
    header.setLayoutData( new FormDataBuilder().top( 0, 0 ).left( 0, 0 ).right( 100, 0 ).result() );
    Composite buttons = createButtonsBar( parent );
    buttons.setLayoutData( new FormDataBuilder().top( header, 25 ).left( 0, 0 ).right( 100, 0 ).result() );

    FlatButton flatBtnHelp =
      new FlatButton( parent, SWT.NONE ).setEnabledImage( rasterImage( "img/help.svg", 24, 24 ) )
        .setDisabledImage( rasterImage( "img/help.svg", 24, 24 ) ).setEnabled( true )
        .setLayoutData( new FormDataBuilder().bottom( 100, 0 ).left( 0, 20 ).result() ).addListener(
          new SelectionAdapter() {
            @Override public void widgetSelected( SelectionEvent selectionEvent ) {
              openHelpDialog();
            }
          } );
    flatBtnHelp.getLabel().setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.help.label" ) );

    Composite select = createFilesBrowser( parent );
    select.setLayoutData(
      new FormDataBuilder().top( buttons, 15 ).left( 0, 0 ).right( 100, 0 ).bottom( flatBtnHelp.getLabel(), -20 )
        .result() );

    typedComboBox = new TypedComboBox<>( parent );

    String[] fileFilters = StringUtils.isNotEmpty( fileDialogOperation.getFilter() )
      ? fileDialogOperation.getFilter().split( "," )
      : new String[] { ALL_FILE_TYPES };
    List<FilterFileType> filterFileTypes = new ArrayList<>();
    int indexOfDefault = 0;
    for ( int i = 0; i < fileFilters.length; i++ ) {
      int finalI = i;
      Optional<FilterFileType> optionalFileFilterType = Arrays.stream( validFileTypes )
        .filter( filterFileType -> filterFileType.getId().equals( fileFilters[ finalI ] ) ).findFirst();
      if ( optionalFileFilterType.isPresent() ) {
        filterFileTypes.add( optionalFileFilterType.get() );
        if ( fileFilters[ i ].equals( fileDialogOperation.getDefaultFilter() ) ) {
          indexOfDefault = i;
        }
      } else {
        log.logBasic( "OptionalFileFilterType not found" );
      }
    }

    typedComboBox.addSelectionListener( ( typedComboBox, newSelection ) -> {
      IStructuredSelection treeViewerSelection = (TreeSelection) ( treeViewer.getSelection() );
      selectPath( treeViewerSelection.getFirstElement() );
      processState();
    } );

    typedComboBox.setLabelProvider( element -> {
      String fileExtensions = element.getValue()
        .replace( '\\', '*' )
        .replace( '|', ',' )
        .replace( "$", "" );
      return element.getLabel() + " (" + fileExtensions + ")";
    } );

    typedComboBox.setContent( filterFileTypes );
    typedComboBox.selectFirstItem();
    typedComboBox.setSelection( filterFileTypes.get( indexOfDefault ) );

    lblComboFilter = new Label( parent, SWT.NONE );
    lblComboFilter.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.save.file-filter.label" ) );
    PropsUI.getInstance().setLook( lblComboFilter );

    btnCancel = new Button( parent, SWT.NONE );
    PropsUI.getInstance().setLook( btnCancel );
    btnCancel.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        clearState();
        parent.dispose();
      }
    } );

    if ( isSaveState() ) {
      createSaveLayout( parent, select );
    } else {
      createOpenLayout( parent, select );
    }
    return parent;
  }

  private void clearState() {
    parentPath = null;
    type = null;
    provider = null;
    path = null;
  }

  private void createSaveLayout( Composite parent, Composite select ) {
    txtFileName = new Text( parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    Label filenameLabel = new Label( parent, SWT.NONE );

    filenameLabel.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.save.file-name.label" ) );

    PropsUI.getInstance().setLook( filenameLabel );
    PropsUI.getInstance().setLook( txtFileName );

    txtFileName.setSize( 40, 40 ); // TODO: Figure out how to set size correctly
    btnSave = new Button( parent, SWT.NONE );
    btnSave.setEnabled( false );

    filenameLabel.setLayoutData( new FormDataBuilder().top( select, 20 ).right( txtFileName, -5 ).result() );
    txtFileName.setLayoutData( new FormDataBuilder().top( select, 20 ).right( lblComboFilter, -15 ).result() );
    lblComboFilter.setLayoutData(
      new FormDataBuilder().top( select, 20 ).right( typedComboBox.viewer.getCombo(), -5 ).result() );
    typedComboBox.viewer.getCombo()
      .setLayoutData( new FormDataBuilder().top( select, 20 ).right( btnSave, -15 ).result() );

    txtFileName.addModifyListener( modifyEvent -> processState() );

    PropsUI.getInstance().setLook( btnSave );
    btnSave.setLayoutData( new FormDataBuilder().top( select, 20 ).right( btnCancel, -15 ).result() );
    btnSave.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.save.button" ) );


    btnSave.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        StructuredSelection structuredSelection;

        if ( fileTableViewer.getSelection().isEmpty() ) {
          structuredSelection = (StructuredSelection) treeViewer.getSelection();
        } else {
          structuredSelection = (StructuredSelection) fileTableViewer.getSelection();
        }

        if ( structuredSelection.getFirstElement() instanceof File
          && txtFileName.getText() != null
          && StringUtils.isNotEmpty( txtFileName.getText() ) ) {
          processOnSavePressed( (File) structuredSelection.getFirstElement() );
        }
      }
    } );
    btnCancel.setLayoutData( new FormDataBuilder().top( select, 20 ).right( 100, -30 ).result() );
    btnCancel.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.cancel.button" ) );

  }

  private void processOnSavePressed( File file ) {
    if ( file != null ) {

      // Local File Provider
      if ( file instanceof LocalFile ) {
        parentPath = file.getParent();
        if ( file instanceof Directory ) {
          path = file.getPath();
        } else {
          path = file.getParent();
        }
      } else if ( file instanceof RepositoryFile ) {
        path = null; // Path isn't used, only `parentPath` is used
        if ( file instanceof Directory ) {
          parentPath = file.getPath();
        } else {
          parentPath = file.getParent();
        }
      } else if ( file instanceof VFSFile ) {
        connection = ( (VFSFile) file ).getConnection();
        parentPath = file.getParent();
        if ( file instanceof Directory ) {
          path = file.getPath();
        } else {
          path = file.getParent();
        }
      }
      // Properties needed for all file types
      type = fileDialogOperation.getFileType();
      name = txtFileName.getText().contains( FILE_PERIOD ) ? txtFileName.getText().split( "\\" + FILE_PERIOD )[ 0 ] : txtFileName.getText();
      provider = file.getProvider();

      getShell().dispose();
    } else {
      // TODO: Display something informing the user
    }
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
    lblSelect.setText( StringUtils.capitalize( shellTitle ) );
    Font bigFont = new Font( getShell().getDisplay(),
      Arrays.stream( lblSelect.getFont().getFontData() )
        .<FontData>map(
          fd -> {
            fd.setHeight( 22 );
            fd.setStyle( SWT.BOLD );
            return fd;
          } )
        .toArray( FontData[]::new ) );
    lblSelect.setFont( bigFont );
    getShell().addDisposeListener( ( e ) -> bigFont.dispose() );


    // TODO: Implement "Search Button" behavior
    final Color clrWhite = new Color( getShell().getDisplay(), 255, 255, 255 );
    Composite searchComp = new Composite( headerComposite, SWT.BORDER );
    PropsUI.getInstance().setLook( searchComp );
    searchComp.addDisposeListener( e -> clrWhite.dispose() );
    searchComp.setLayoutData( new FormDataBuilder().right( 100, 0 ).result() );
    searchComp.setBackground( clrWhite );

    RowLayout searchLayout = new RowLayout();
    searchLayout.center = true;
    searchComp.setLayout( searchLayout );

    Label lblSearch = new Label( searchComp, SWT.NONE );
    PropsUI.getInstance().setLook( lblSearch );
    lblSearch.setLayoutData( new RowData() );
    lblSearch.setBackground( clrWhite );
    lblSearch.setImage( rasterImage( "img/Search.S_D.svg", 25, 25 ) );

    RowData rd = new RowData();
    rd.width = 200;
    txtSearch = new Text( searchComp, SWT.NONE );
    PropsUI.getInstance().setLook( txtSearch );
    txtSearch.setBackground( clrWhite );
    txtSearch.setLayoutData( rd );
    txtSearch.addModifyListener( (event) -> {performSearch(event);});

    headerComposite.layout();

    return headerComposite;
  }

  private void performSearch(ModifyEvent event) {
    IStructuredSelection treeViewerSelection = (TreeSelection) ( treeViewer.getSelection() );
    selectPath( treeViewerSelection.getFirstElement() );
    processState();
  }

  private Composite createButtonsBar( Composite parent ) {
    Composite buttons = new Composite( parent, SWT.NONE );
    PropsUI.getInstance().setLook( buttons );

    FormLayout formLayout = new FormLayout();
    formLayout.marginLeft = 20;
    formLayout.marginRight = 20;
    buttons.setLayout( formLayout );

    FlatButton backButton =
      new FlatButton( buttons, SWT.NONE ).setEnabledImage( rasterImage( "img/Backwards.S_D.svg", 32, 32 ) )
        .setDisabledImage( rasterImage( "img/Backwards.S_D_disabled.svg", 32, 32 ) )
        .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.back.button" ) )
        .setEnabled( false );

    FlatButton forwardButton =
      new FlatButton( buttons, SWT.NONE ).setEnabledImage( rasterImage( "img/Forwards.S_D.svg", 32, 32 ) )
        .setDisabledImage( rasterImage( "img/Forwards.S_D_disabled.svg", 32, 32 ) )
        .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.forward.button" ) )
        .setEnabled( true ).setLayoutData( new FormDataBuilder().left( backButton.getLabel(), 0 ).result() );

    Composite fileButtons = new Composite( buttons, SWT.NONE );
    PropsUI.getInstance().setLook( fileButtons );
    fileButtons.setLayout( new RowLayout() );
    fileButtons.setLayoutData( new FormDataBuilder().right( 100, 0 ).result() );


    flatBtnUp =
      new FlatButton( fileButtons, SWT.NONE ).setEnabledImage( rasterImage( "img/Up_Folder.S_D.svg", 32, 32 ) )
        .setDisabledImage( rasterImage( "img/Up_Folder.S_D_disabled.svg", 32, 32 ) )
        .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.up-directory.button" ) )

        .setLayoutData( new RowData() ).setEnabled( false ).addListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent selectionEvent ) {
            TreeSelection treeSelection = (TreeSelection) treeViewer.getSelection();
            if ( !treeSelection.isEmpty() ) {
              if ( hasParentFolder( treeSelection ) ) {
                TreePath[] paths = treeSelection.getPaths();
                if ( paths.length > 0 ) {
                  TreePath parentPath = paths[ paths.length - 1 ].getParentPath();
                  ISelection currentSelection = new StructuredSelection( parentPath.getLastSegment() );
                  treeViewer.setSelection( currentSelection );
                }
              }
            }
          }
        } );


    flatBtnAdd = new FlatButton( fileButtons, SWT.NONE )
      .setEnabledImage( rasterImage( "img/New_Folder.S_D.svg", 32, 32 ) )
      .setDisabledImage( rasterImage( "img/New_Folder.S_D_disabled.svg", 32, 32 ) )
      .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.add-folder.button" ) )
      .setLayoutData( new RowData() ).setEnabled( false ).addListener(
        new SelectionAdapter() {
          @Override public void widgetSelected( SelectionEvent selectionEvent ) {
            enterStringDialog = new EnterStringDialog( getShell(), StringUtils.EMPTY,
              BaseMessages.getString( PKG, "file-open-save-plugin.app.add-folder.shell-text" ),
              BaseMessages.getString( PKG, "file-open-save-plugin.app.add-folder.line-text" ) );
            String newFolderName = enterStringDialog.open();

            if ( StringUtils.isNotEmpty( newFolderName ) ) {
              addFolder( newFolderName );
            }
          }
        } );


    FlatButton
      deleteButton =
      new FlatButton( fileButtons, SWT.NONE ).setEnabledImage( rasterImage( "img/Close.S_D.svg", 32, 32 ) )
        .setDisabledImage( rasterImage( "img/Close.S_D_disabled.svg", 32, 32 ) )
        .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.delete.button" ) )
        .setLayoutData( new RowData() ).setEnabled( false );

    flatBtnRefresh =
      new FlatButton( fileButtons, SWT.NONE ).setEnabledImage( rasterImage( "img/Refresh.S_D.svg", 32, 32 ) )
        .setDisabledImage( rasterImage( "img/Refresh.S_D_disabled.svg", 32, 32 ) )
        .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.refresh.button" ) )
        .setLayoutData( new RowData() ).setEnabled( true ).addListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent selectionEvent ) {
            refreshDisplay( selectionEvent );
          }
        } );

    txtNav = new Text( buttons, SWT.BORDER );

    this.txtNav.setEditable( true );
    PropsUI.getInstance().setLook( txtNav );
    txtNav.setBackground( getShell().getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
    txtNav.setLayoutData(
      new FormDataBuilder().left( forwardButton.getLabel(), 10 ).right( fileButtons, -10 ).height( 32 ).result() );

    return buttons;
  }

  private void refreshDisplay( SelectionEvent selectionEvent ) {
    StructuredSelection fileTableViewerSelection = (StructuredSelection) ( fileTableViewer.getSelection() );
    TreeSelection treeViewerSelection = (TreeSelection) ( treeViewer.getSelection() );
    FileProvider fileProvider = null;

    // Refresh the current element of the treeViewer
    if ( !treeViewerSelection.isEmpty() ) {
      if ( treeViewerSelection.getFirstElement() instanceof Tree ) {
        try {
          fileProvider = ProviderServiceService.get().get( ( (Tree) treeViewerSelection.getFirstElement() )
            .getProvider() );
        } catch ( Exception ex ) {
          log.logDebug( "Unable to find provider" );
        }
        for ( Object file : ( (Tree) treeViewerSelection.getFirstElement() ).getChildren() ) {
          FILE_CONTROLLER.clearCache( (File) file );
        }
        treeViewer.collapseAll();
      } else {
        try {
          fileProvider = ProviderServiceService.get().get( ( (File) treeViewerSelection.getFirstElement() ).getProvider() );
        } catch ( Exception ex ) {
          log.logDebug( "Unable to find provider" );
        }
        FILE_CONTROLLER.clearCache( (File) ( treeViewerSelection.getFirstElement() ) );
      }
      if ( fileProvider != null ) {
        fileProvider.clearProviderCache();
      }
      if ( treeViewerSelection.getFirstElement() instanceof File
        && StringUtils.isBlank( ( (File) treeViewerSelection.getFirstElement() ).getParent() ) ) {
        treeViewer.collapseAll();
      }

      treeViewer.refresh( treeViewerSelection.getFirstElement(), true );
      fileTableViewer.refresh( true );
      treeViewer.setSelection( treeViewerSelection, true );
    } else if ( treeViewerSelection.isEmpty() && fileTableViewerSelection.isEmpty() ) {
      try {
        fileProvider = ProviderServiceService.get().get( fileDialogOperation.getProvider() );
        fileProvider.clearProviderCache();
        treeViewer.setInput( FILE_CONTROLLER.load( ProviderFilterType.ALL_PROVIDERS.toString() ).toArray() );
        treeViewer.refresh( true );
        fileTableViewer.refresh( true );
      } catch ( Exception ex ) {
        // Ignored
      }
    }
  }

  private Composite createFilesBrowser( Composite parent ) {
    clrGray = getShell().getDisplay().getSystemColor( SWT.COLOR_GRAY );
    clrBlack = getShell().getDisplay().getSystemColor( SWT.COLOR_BLACK );
    imgTime = rasterImage( "img/Time.S_D.svg", 25, 25 );
    imgVFS = rasterImage( "img/VFS_D.svg", 25, 25 );
    imgFolder = rasterImage( "img/file_icons/Archive.S_D.svg", 25, 25 );
    imgDisk = rasterImage( "img/Disk.S_D.svg", 25, 25 );
    imgFile = rasterImage( "img/file_icons/Doc.S_D.svg", 25, 25 );
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

    treeViewer.setLabelProvider( labelProvider );

    treeViewer.setContentProvider( new FileTreeContentProvider( FILE_CONTROLLER ) );

    // Load the various file types on the left
    treeViewer.setInput( FILE_CONTROLLER.load( ProviderFilterType.ALL_PROVIDERS.toString() ).toArray() );

    treeViewer.addPostSelectionChangedListener( e -> {
      IStructuredSelection selection = (IStructuredSelection) e.getSelection();
      flatBtnUp.setEnabled( hasParentFolder( selection ) );
      Object selectedNode = selection.getFirstElement();
      // Expand the selection in the treeviewer
      if ( selectedNode != null && !treeViewer.getExpandedState( selectedNode ) ) {
        treeViewer.refresh( selectedNode, true );
        treeViewer.setExpandedState( selectedNode, true );
      }
      // Update the path that is selected
      selectPath( selectedNode, false );
      // Clears the selection from fileTableViewer
      fileTableViewer.setSelection( new StructuredSelection() );
      txtSearch.setText("");
      processState();
    } );

    fileTableViewer = new TableViewer( sashForm, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION );
    PropsUI.getInstance().setLook( fileTableViewer.getTable() );
    fileTableViewer.getTable().setHeaderVisible( true );
    TableViewerColumn tvcName = new TableViewerColumn( fileTableViewer, SWT.NONE );
    tvcName.getColumn().setText( BaseMessages.getString( PKG, "file-open-save-plugin.files.name.header" ) );
    tvcName.getColumn().setWidth( 250 );

    ColumnLabelProvider clpName = new ColumnLabelProvider() {

      @Override public String getText( Object element ) {
        File f = (File) element;
        return f.getName();
      }

      @Override public Image getImage( Object element ) {
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
      @Override public Color getForeground( Object element ) {
        return clrGray;
      }

      @Override public String getText( Object element ) {
        return super.getText( StringUtils.capitalize( ( (File) element ).getType() ) );
      }
    } );

    TableViewerColumn tvcModified = new TableViewerColumn( fileTableViewer, SWT.NONE );
    tvcModified.getColumn().setText( BaseMessages.getString( PKG, "file-open-save-plugin.files.modified.header" ) );
    tvcModified.getColumn().setWidth( 140 );
    tvcModified.getColumn().setResizable( false );

    tvcModified.setLabelProvider( new ColumnLabelProvider() {
      SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yy hh:mm aa" );

      @Override public Color getForeground( Object element ) {
        return clrGray;
      }

      @Override public String getText( Object element ) {
        try {
          return super.getText( sdf.format( ( (File) element ).getDate() ) );
        } catch ( Exception e ) {
          return "";
        }
      }
    } );

    fileTableViewer.getTable().addListener( SWT.Resize, e -> {
      Rectangle r = fileTableViewer.getTable().getClientArea(); tvcName.getColumn()
        .setWidth( Math.max( 150, r.width - tvcType.getColumn().getWidth() - tvcModified.getColumn().getWidth() ) );

    } );

    fileTableViewer.setContentProvider( new ArrayContentProvider() );

    fileTableViewer.addPostSelectionChangedListener( e -> {
      IStructuredSelection selection = (IStructuredSelection) e.getSelection();
      Object selectedNode = selection.getFirstElement();
      if ( selectedNode instanceof File ) {
        // Sets the name
        if ( txtFileName != null && !( selectedNode instanceof Directory ) ) {
          txtFileName.setText( ( (File) selectedNode ).getName() );
          name = ( (File) selectedNode ).getName();
        }
        processState();
        txtNav.setText( getNavigationPath( (File) selectedNode ) );
      }
    } );

    fileTableViewer.addDoubleClickListener( e -> {
      Object selection = ( (IStructuredSelection) e.getSelection() ).getFirstElement();

      if ( selection instanceof Directory ) {
        treeViewer.setExpandedState( selection, true );
        treeViewer.setSelection( new StructuredSelection( selection ), true );

        if ( command.contains( FileDialogOperation.SAVE ) || command.equals( FileDialogOperation.SELECT_FOLDER ) ) {
          parentPath = ( (Directory) selection ).getParent();
          path = ( (Directory) selection ).getPath();
          provider = ( (Directory) selection ).getProvider();
        }
        txtNav.setText( getNavigationPath( (File) selection ) );
      } else if ( selection instanceof File ) {
        File localFile = (File) selection;
        if ( command.equalsIgnoreCase( FileDialogOperation.SELECT_FILE )
          || command.equalsIgnoreCase( FileDialogOperation.OPEN )
          || command.equalsIgnoreCase( FileDialogOperation.SELECT_FILE_FOLDER ) ) {
          txtNav.setText( getNavigationPath( localFile ) );
          String fileExtension = extractFileExtension( localFile.getPath() );
          if ( isValidFileExtension( fileExtension ) ) {
            openFileSelector( localFile );
            getShell().dispose();
          }
        }
      }
      processState();
    } );

    sashForm.setWeights( new int[] { 1, 2 } );

    return browser;
  }

  private void openFileSelector( File f ) {
    setStateVariablesFromSelection( f );
  }

  private void setButtonOpenState() {
    if ( btnOpen != null && !getShell().isDisposed() ) {
      openStructuredSelectionPath( (IStructuredSelection) treeViewer.getSelection() );

      openStructuredSelectionPath( (IStructuredSelection) fileTableViewer.getSelection() );

      if ( command.equalsIgnoreCase( FileDialogOperation.SELECT_FILE_FOLDER ) ) {
        btnOpen.setEnabled( StringUtils.isNotEmpty( path ) || StringUtils.isNotEmpty( name ) );
      } else if ( command.equals( FileDialogOperation.SELECT_FOLDER ) ) {
        btnOpen.setEnabled( StringUtils.isNotEmpty( path ) && StringUtils.isEmpty( name ) );
      } else if ( command.equals( FileDialogOperation.SELECT_FILE ) || command.equals( FileDialogOperation.OPEN ) ) {
        btnOpen.setEnabled( StringUtils.isNotEmpty( name ) );
      } else {
        btnOpen.setEnabled( false );
      }
    }
  }

  private boolean isSaveState() {
    return command.equals( FileDialogOperation.SAVE )
      || command.equals( FileDialogOperation.SAVE_TO ) || command.equals( FileDialogOperation.SAVE_TO_FILE_FOLDER );
  }

  private void processState() {
    setButtonSaveState();
    setButtonOpenState();
  }

  private void setButtonSaveState() {
    if ( isSaveState() && txtFileName != null && !getShell().isDisposed() ) {
      // If the path set by the treeViewer; use the left-hand values
      saveStructuredSelectionPath( (IStructuredSelection) treeViewer.getSelection() );

      // If the path is set by the fileTableViewer override the treeViewer values (use the right-hand values)
      saveStructuredSelectionPath( (IStructuredSelection) fileTableViewer.getSelection() );


      if ( StringUtils.isNotEmpty( path ) ) {


        if ( ( command.equals( FileDialogOperation.SAVE_TO_FILE_FOLDER )
          || command.equals( FileDialogOperation.SAVE )
          || command.equals( FileDialogOperation.SAVE_TO ) )
          && StringUtils.isNotEmpty( txtFileName.getText() ) ) {
          btnSave.setEnabled( true );
        }
      } else {
        btnSave.setEnabled( false );
      }
    }
  }

  private void openStructuredSelectionPath( IStructuredSelection selection ) {
    IStructuredSelection selectedFileTreeViewer = selection.isEmpty() ? null : selection;
    if ( selectedFileTreeViewer != null && selectedFileTreeViewer.getFirstElement() instanceof Directory ) {
      setStateVariablesFromSelection( selectedFileTreeViewer );
      name = null;
    } else if ( selectedFileTreeViewer != null && selectedFileTreeViewer.getFirstElement() instanceof File ) {
      String tempName = ( (File) selectedFileTreeViewer.getFirstElement() ).getPath();

      if ( command.equals( FileDialogOperation.SELECT_FILE )
        || command.equalsIgnoreCase( FileDialogOperation.SELECT_FILE_FOLDER )
        || command.equals( FileDialogOperation.OPEN ) ) {

        if ( typedComboBox.getSelection().getId().equals( ALL_FILE_TYPES ) ) {
          name = tempName;
          // Check for correct file type before assigning name value
        } else {
          String fileExtension = extractFileExtension( tempName );
          name = isValidFileExtension( fileExtension ) ? tempName : null;
        }
      } else {
        name = tempName;
      }
      setStateVariablesFromSelection( selectedFileTreeViewer );
    }
  }

  private boolean isValidFileExtension( String fileExtension ) {
    return Utils.matches( fileExtension, typedComboBox.getSelection().getValue() )
      || typedComboBox.getSelection().getId().equals( ALL_FILE_TYPES );
  }

  private String extractFileExtension( String fullFilePath ) {
    int lastIndexOfPeriod = fullFilePath.lastIndexOf( FILE_PERIOD );
    String fileExtension = ( lastIndexOfPeriod == -1 )
      ? StringUtils.EMPTY : fullFilePath.substring( lastIndexOfPeriod );
    return fileExtension;
  }

  private void saveStructuredSelectionPath( IStructuredSelection selection ) {
    IStructuredSelection selectedFileTreeViewer = selection.isEmpty() ? null : selection;
    if ( selectedFileTreeViewer != null && selectedFileTreeViewer.getFirstElement() instanceof File ) {
      setStateVariablesFromSelection( selectedFileTreeViewer );
    }
  }

  private void setStateVariablesFromSelection( IStructuredSelection selectedFileTreeViewer ) {
    setStateVariablesFromSelection( (File) selectedFileTreeViewer.getFirstElement() );
  }

  private void setStateVariablesFromSelection( File f ) {
    path = f.getPath();
    parentPath = f.getParent();
    provider = f.getProvider();
    type = f.getType();
    if ( f instanceof VFSFile ) {
      connection = ( (VFSFile) f ).getConnection();
      parentPath = ( (VFSFile) f ).getConnectionParentPath();
      path = ( (VFSFile) f ).getConnectionPath();
    }
    if ( f instanceof RepositoryFile ) {
      objectId = ( (RepositoryFile) f ).getObjectId();
    }
  }


  private boolean addFolder( String newFolderName ) {
    try {
      Object selection;
      Object treeViewerDestination;
      StructuredSelection fileTableViewerSelection = (StructuredSelection) ( fileTableViewer.getSelection() );
      TreeSelection treeViewerSelection = (TreeSelection) ( treeViewer.getSelection() );
      FileProvider fileProvider = null;
      String parentPathOfSelection = "";


      if ( !fileTableViewerSelection.isEmpty() ) {
        selection = fileTableViewerSelection.getFirstElement();
        if ( selection instanceof Directory ) {
          treeViewerDestination = fileTableViewerSelection.getFirstElement();
        } else {
          treeViewerDestination = treeViewerSelection.getFirstElement();
        }
      } else {
        selection = treeViewerSelection.getFirstElement();
        treeViewerDestination = treeViewerSelection.getFirstElement();
      }

      if ( selection instanceof Directory ) {
        fileProvider = ProviderServiceService.get().get( ( (Directory) selection ).getProvider() );
        parentPathOfSelection = ( (Directory) selection ).getPath();
      } else if ( selection instanceof File ) {
        fileProvider = ProviderServiceService.get().get( ( (File) selection ).getProvider() );
        parentPathOfSelection = Paths.get( ( (File) selection ).getParent() ).getParent().toString();
      }

      if ( fileProvider != null ) {
        fileProvider.createDirectory( parentPathOfSelection, (File) selection, newFolderName );
        FILE_CONTROLLER.clearCache( (File) treeViewerDestination );
        treeViewer.refresh( treeViewerDestination, true );

        selectPath( treeViewerDestination, false );

        IStructuredSelection selectionAsStructuredSelection = new StructuredSelection( treeViewerDestination );
        treeViewer.setSelection( selectionAsStructuredSelection, true );
        if ( !treeViewer.getExpandedState( selectionAsStructuredSelection ) ) {
          treeViewer.setExpandedState( selectionAsStructuredSelection, true );
        }
        // Set selection in fileTableViewer to new folder
        for ( TableItem tableItem : fileTableViewer.getTable().getItems() ) {
          if ( tableItem.getText( 0 ).equals( newFolderName ) ) {
            fileTableViewer.getTable().setSelection( tableItem );
            fileTableViewer.getTable().setFocus();
            break;
          }
        }
        processState();
        return true;
      } else {
        throw new KettleException( "Unable to select file provider!" );
      }
    } catch ( Exception ex ) {
      new ErrorDialog( getShell(), "Error",
        BaseMessages.getString( PKG, "file-open-save-plugin.error.unable-to-move-file.message" ), ex, false );
    }
    return false;
  }

  private Image rasterImage( String path, int width, int height ) {
    SwtUniversalImage img =
      SwtSvgImageUtil.getUniversalImage( getShell().getDisplay(), getClass().getClassLoader(), path );
    Image image = img.getAsBitmapForSize( getShell().getDisplay(), width, height );
    getShell().addDisposeListener( e -> {
      img.dispose();
      image.dispose();
    } );
    return image;
  }

  boolean hasParentFolder( IStructuredSelection structuredSelection ) {
    return !structuredSelection.isEmpty() && structuredSelection.getFirstElement() instanceof Directory;
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
        // Sets state to blank
        parentPath = null;
        path = null;
        name = null;
        if ( children.size() != 0 ) {
          txtNav.setText( getNavigationPath( (File) children.get( 0 ) ) );
        } else {
          txtNav.setText( StringUtils.EMPTY );
        }
      }
      flatBtnAdd.setEnabled( false );
      processState();

    } else if ( selectedElement instanceof Directory ) {
      try {
          String searchString = txtSearch.getText();
          fileTableViewer.setInput( FILE_CONTROLLER.getFiles( (File) selectedElement, null, useCache ).stream()
            .filter(
              file -> searchString.isEmpty() || file.getName().toLowerCase().contains( searchString.toLowerCase() ) )
            .sorted( Comparator.comparing( f -> f instanceof Directory, Boolean::compare ).reversed()
              .thenComparing( Comparator.comparing( f -> ( (File) f ).getName(),
                String.CASE_INSENSITIVE_ORDER ) ) )
            .toArray() );

        for ( TableItem fileTableItem : fileTableViewer.getTable().getItems() ) {
          Object tableItemObject = fileTableItem.getData();
          if ( !( tableItemObject instanceof Directory ) ) {
            String fileName = ( (File) tableItemObject ).getPath();
            String fileExtension = extractFileExtension( fileName );
            boolean isValidFileExtension = isValidFileExtension( fileExtension );
            if ( isValidFileExtension ) {
              fileTableItem.setForeground( clrBlack );
            } else {
              fileTableItem.setForeground( clrGray );
            }
          }
        }


        txtNav.setText( getNavigationPath( (File) selectedElement ) );
        flatBtnAdd.setEnabled( ( (Directory) selectedElement ).isCanAddChildren() );

        processState();
      } catch ( FileException e ) {
        // TODO Auto-generated catch block
        log.logBasic( e.getMessage() );
      }
    }

  }

  protected String getNavigationPath( File file ) {
    return file instanceof VFSFile ? ( (VFSFile) file ).getConnectionPath() : ( file ).getPath();
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

        @Override public void mouseEnter( MouseEvent arg0 ) {
          origColor = label.getBackground();
          if ( enabled.get() ) {
            label.setBackground( hoverColor );
          }
        }

        @Override public void mouseExit( MouseEvent e ) {
          if ( origColor != null ) {
            label.setBackground( origColor );
          }
        }

      } );

      label.addMouseListener( new MouseAdapter() {
        private boolean down = false;

        @Override
        public void mouseDown( MouseEvent me ) {
          down = true;
        }

        @Override
        public void mouseUp( MouseEvent me ) {
          if ( down && isEnabled() ) {
            label.notifyListeners( SWT.Selection, new Event() );
          }
          down = false;
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

    public FlatButton addListener( SelectionListener listener ) {
      TypedListener typedListener = new TypedListener( listener );
      label.addListener( SWT.Selection, typedListener );
      return this;
    }


  }

  protected static class FileTreeContentProvider implements ITreeContentProvider {

    private final FileController fileController;

    public FileTreeContentProvider( FileController fileController ) {
      this.fileController = fileController;
    }

    @Override public Object[] getElements( Object inputElement ) {
      return (Object[]) inputElement;
    }

    @Override public Object[] getChildren( Object parentElement ) {

      if ( parentElement instanceof Tree ) {

        Tree parentTree = (Tree) parentElement;
        if ( parentTree.isHasChildren() ) {
          return ( parentTree ).getChildren().toArray();
        }
      } else if ( parentElement instanceof Directory ) {
        try {
          return fileController.getFiles( (Directory) parentElement, null, true ).stream()
            .filter( Directory.class::isInstance )
            .sorted( Comparator.comparing( Entity::getName, String.CASE_INSENSITIVE_ORDER ) ).toArray();
        } catch ( FileException e ) {
          // TODO: Error message that something went wrong
        }
      }

      return new Object[ 0 ];
    }

    @Override public Object getParent( Object element ) {

      return null;
    }

    @Override public boolean hasChildren( Object element ) {
      if ( element instanceof Tree ) {
        return ( (Tree) element ).isHasChildren();
      } else if ( element instanceof Directory ) {
        return ( (Directory) element ).isHasChildren();
      }
      return false;
    }

    @Override public void dispose() {
      // TODO Auto-generated method stub

    }

    @Override public void inputChanged( Viewer arg0, Object arg1, Object arg2 ) {
      // TODO Auto-generated method stub


    }

  }

  // TypedComboBox Definition

  protected interface TypedComboBoxSelectionListener<T> {

    void selectionChanged( TypedComboBox<T> typedComboBox, T newSelection );
  }

  protected interface TypedComboBoxLabelProvider<T> {

    String getListLabel( T element );

  }

  protected class TypedComboBox<T> {

    private ComboViewer viewer;
    private TypedComboBoxLabelProvider<T> labelProvider;
    private List<T> content;
    private List<TypedComboBoxSelectionListener<T>> selectionListeners;
    private T currentSelection;

    public TypedComboBox( Composite parent ) {
      this.viewer = new ComboViewer( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
      this.viewer.setContentProvider( new ArrayContentProvider() );

      viewer.setLabelProvider( new LabelProvider() {
        @Override
        public String getText( Object element ) {
          T typedElement = getTypedObject( element );
          if ( labelProvider != null && typedElement != null ) {
            return labelProvider.getListLabel( typedElement );
          } else {
            return element.toString();
          }
        }
      } );

      viewer.addSelectionChangedListener( event -> {
        IStructuredSelection selection = (IStructuredSelection) event
          .getSelection();
        T typedSelection = getTypedObject( selection.getFirstElement() );
        if ( typedSelection != null ) {
          currentSelection = typedSelection;
          notifySelectionListeners( typedSelection );
        }

      } );

      this.content = new ArrayList<>();
      this.selectionListeners = new ArrayList<>();
    }

    public void setLabelProvider( TypedComboBoxLabelProvider<T> labelProvider ) {
      this.labelProvider = labelProvider;
    }

    public void setContent( List<T> content ) {
      this.content = content;
      this.viewer.setInput( content.toArray() );
    }

    public T getSelection() {
      return currentSelection;
    }

    public void setSelection( T selection ) {
      if ( content.contains( selection ) ) {
        viewer.setSelection( new StructuredSelection( selection ), true );
      }
    }

    public void selectFirstItem() {
      if ( !content.isEmpty() ) {
        setSelection( content.get( 0 ) );
      }
    }

    public void addSelectionListener( TypedComboBoxSelectionListener<T> listener ) {
      this.selectionListeners.add( listener );
    }

    public void removeSelectionListener(
      TypedComboBoxSelectionListener<T> listener ) {
      this.selectionListeners.remove( listener );
    }

    private T getTypedObject( Object o ) {
      if ( content.contains( o ) ) {
        return content.get( content.indexOf( o ) );
      } else {
        return null;
      }
    }

    public void notifySelectionListeners( T newSelection ) {
      for ( TypedComboBoxSelectionListener<T> listener : selectionListeners ) {
        listener.selectionChanged( this, newSelection );
      }
    }
  }

  public static class FilterFileType {
    private String id;
    private String value;
    private String label;

    public FilterFileType() {
      this.id = StringUtils.EMPTY;
      this.value = StringUtils.EMPTY;
      this.label = StringUtils.EMPTY;
    }

    public FilterFileType( String id, String value, String label ) {
      this.id = id;
      this.value = value;
      this.label = label;
    }

    public String getId() {
      return id;
    }

    public void setId( String id ) {
      this.id = id;
    }

    public String getValue() {
      return value;
    }

    public void setValue( String value ) {
      this.value = value;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel( String label ) {
      this.label = label;
    }
  }
}
