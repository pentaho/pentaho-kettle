/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2024 by Hitachi Vantara : http://www.pentaho.com
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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.TypedListener;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.plugins.fileopensave.api.file.FileDetails;
import org.pentaho.di.plugins.fileopensave.api.overwrite.OverwriteStatus;
import org.pentaho.di.plugins.fileopensave.api.overwrite.OverwriteStatus.OverwriteMode;
import org.pentaho.di.plugins.fileopensave.api.providers.Directory;
import org.pentaho.di.plugins.fileopensave.api.providers.Entity;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.FileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.Result;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.Utils;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.InvalidFileProviderException;
import org.pentaho.di.plugins.fileopensave.controllers.FileController;
import org.pentaho.di.plugins.fileopensave.dragdrop.ElementDragListener;
import org.pentaho.di.plugins.fileopensave.dragdrop.ElementTransfer;
import org.pentaho.di.plugins.fileopensave.dragdrop.ElementTreeDropAdapter;
import org.pentaho.di.plugins.fileopensave.providers.local.model.LocalFile;
import org.pentaho.di.plugins.fileopensave.providers.recents.model.RecentTree;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryFile;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryTree;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSFile;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSLocation;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSTree;
import org.pentaho.di.plugins.fileopensave.service.FileCacheService;
import org.pentaho.di.plugins.fileopensave.service.ProviderServiceService;
import org.pentaho.di.ui.core.FileDialogOperation;
import org.pentaho.di.ui.core.FileDialogOperation.CustomImage;
import org.pentaho.di.ui.core.FileDialogOperation.CustomImageProvider;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterStringDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.WarningDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.ProviderFilterType;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

// This dialog's use of generics is inconsistent and will likely never be fixed,
// so we might as well not be swamped by warnings
@SuppressWarnings( { "rawtypes", "unchecked" } )
public class FileOpenSaveDialog extends Dialog implements FileDetails {
  private static final Class<?> PKG = FileOpenSaveDialog.class;

  private final Image logo = GUIResource.getInstance().getImageLogoSmall();
  private static final int OPTIONS = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX;
  private static final String HELP_URL = Const.getDocUrl( "mk-95pdia003/data-integration-perspective-in-the-pdi-client/work-with-transformations/open-a-transformation" );
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
  private static final String PASTE_ACTION_SKIP = "skip";
  private static final String PASTE_ACTION_REPLACE = "replace";
  private static final String PASTE_ACTION_KEEP_BOTH = "keep-both";

  /** default size of a file icon in the file browser */
  private static final int SIZE_ICON_BROWSER = 25;

  private FilterFileType[] validFileTypes;
  private String shellTitle = "Open";
  private String objectId;
  private String name;
  private String path;
  private String parentPath;
  private String type;

  /**
   * separate VFS connection name variable is no longer needed
   * @deprecated
   * The connection name is in the URI since full pvfs paths are being used.
   */
  @Deprecated
  private String connection;
  private String provider;
  private String providerFilter;
  private String command = FileDialogOperation.OPEN;
  private FileDialogOperation fileDialogOperation = new FileDialogOperation( command );

  private Text txtFileName;
  private LogChannelInterface log;
  private int width;
  private int height;

  // The left-hand tree viewer
  protected TreeViewer treeViewer;
  protected TableViewer fileTableViewer;

  protected Comparator<Object> fileTableViewerComparator;

  protected final Comparator<Object> DEFAULT_FOLDER_VS_FILE_COMPARATOR =
      Comparator.comparing( f -> f instanceof Directory, Boolean::compare );

  protected final Comparator<Object> DEFAULT_FILE_TABLE_VIEWER_COMPARATOR =
      DEFAULT_FOLDER_VS_FILE_COMPARATOR.reversed()
          .thenComparing( Comparator.comparing( f -> ( (File) f ).getName(),
            String.CASE_INSENSITIVE_ORDER ) );

  protected TableColumn fTableNameColumn;
  protected TableColumn fTableTypeColumn;
  protected TableColumn fTableModifiedColumn;


  protected Text txtSearch;
  protected Set<File> selectedItems = new HashSet<>();

  protected String pasteAction = null;
  protected boolean isCutActionSelected = false;
  protected boolean isApplyToAll = false;

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
  private Image imgTrans;
  private Image imgJob;

  private final Map<String, Image> customImages = new HashMap<>();

  // Dialogs

  private EnterStringDialog enterStringDialog;

  private WarningDialog warningDialog;


  // Top Right Buttons
  private FlatButton flatBtnAdd;

  private FlatButton flatBtnRefresh;

  private FlatButton flatBtnUp;

  private FlatButton flatBtnDelete;

  private FlatButton flatBtnBack;

  private FlatButton flatBtnForward;

  private Boolean navigateBtnFlag = false;

  List<Object> selectionHistory = new ArrayList<>();

  int currentHistoryIndex;

  StackLayout stackLayout;
  Composite recentComposite;

  boolean isFileTreeProcessing = false;

  Label noRecentFilesLabel = null;

  private FileController fileController;

  public FileOpenSaveDialog( Shell parentShell, int width, int height, LogChannelInterface logger ) {
    super( parentShell );
    this.log = logger;
    this.width = width;
    this.height = height;
    setShellStyle( OPTIONS );
    try (
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream( FILE_EXTENSION_RESOURCE_PATH );
        BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) ) ) {
      ObjectMapper objectMapper = new ObjectMapper();
      String jsonString = reader.lines().collect( Collectors.joining( "\n" ) );
      validFileTypes = objectMapper.readValue( jsonString, FilterFileType[].class );
    } catch ( Exception ex ) {
      log.logError( "Could not load resource", ex );
    }
  }

  public void open( FileDialogOperation fileDialogOperation ) {

    this.fileDialogOperation = fileDialogOperation;
    command = fileDialogOperation.getCommand();
    this.fileController =
        new FileController( FileCacheService.INSTANCE.get(), ProviderServiceService.get(), fileDialogOperation.getFileLoadListener() );

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

  private void loadCustomImages( CustomImageProvider customImageProvider, Map<String, Image> customImages,
      ClassLoader defaultClassLoader ) {
    for ( Map.Entry<String, CustomImage> entry : customImageProvider.getCustomImages().entrySet() ) {
      CustomImage location = entry.getValue();
      ClassLoader classLoader = location.getClassLoader().orElse( defaultClassLoader );
      // returns a default image if unable to load
      Image image = rasterImage( classLoader, location.getPath(), SIZE_ICON_BROWSER, SIZE_ICON_BROWSER );
      customImages.put( entry.getKey(), image );
    }
  }

  LabelProvider labelProvider = new LabelProvider() {
    @Override public String getText( Object element ) {
      if ( element instanceof Tree ) {
        return ( (Tree) element ).getNameDecoded();
      } else if ( element instanceof Directory ) {
        return ( (Directory) element ).getNameDecoded();
      } else if ( element instanceof File ) {
        return ((File) element).getNameDecoded();
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
    newShell.setMinimumSize( 845, 458 );
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

        if ( command.equals( FileDialogOperation.SELECT_FILE ) || command.equals( FileDialogOperation.OPEN )
          || command.equals( FileDialogOperation.IMPORT ) ) {
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

    getShell().setDefaultButton( btnOpen );
    
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
      String[] finalFileFilters = fileFilters;
      Optional<FilterFileType> optionalFileFilterType = Arrays.stream( validFileTypes )
        .filter( filterFileType -> filterFileType.getId().equals( finalFileFilters[ finalI ] ) ).findFirst();
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
      if ( element.getLabel().contains( "*." ) ) {
        return element.getLabel();
      }
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
    treeViewer.setSelection( new StructuredSelection( treeViewer.getTree().getItem( 0 ).getData() ) );
    flatBtnBack.setEnabled( false );

    select.setFocus();

    return parent;
  }

  private void setPreviousSelection() {
    String targetPath = this.fileDialogOperation.getPath();
    if ( StringUtils.isNotEmpty( targetPath ) ) {
      // If the path is file, set the parent to be the target path
      if ( targetPath.startsWith( "file:///" ) ) {
        targetPath = targetPath.substring( 7 );
      }
      java.io.File filePath = new java.io.File( targetPath );
      if ( !filePath.exists() && !ProviderFilterType.REPOSITORY.toString().equalsIgnoreCase( fileDialogOperation.getProvider() ) ) {
        return;
      } else if ( filePath.isFile() ) {
        targetPath = filePath.getParent();
      }
    }

    String[] targetPathArray;
    // Sets navigation to previous selection
    if ( StringUtils.isNotEmpty( targetPath ) ) {
      FileProvider fileProvider = determineProviderFromFilePath( this.fileDialogOperation );
      if ( fileProvider != null ) {
        char pathSplitter = targetPath.contains( "/" ) ? '/' : '\\';
        // URL and Linux File Paths
        targetPathArray = getStringsAtEachDirectory( targetPath, pathSplitter );
        if ( targetPathArray == null ) {
          return;
        }
        Tree tree = fileProvider.getTree();
        TreeItem[] treeItems = treeViewer.getTree().getItems();
        Tree selectedTree = null;
        for ( TreeItem currentTreeItem : treeItems ) {
          Object currentObject = currentTreeItem.getData();
          if ( currentObject instanceof Tree && ( (Tree) currentObject ).getName().equals( fileProvider.getName() ) ) {
            selectedTree = (Tree) currentObject;
            break;
          }
        }
        if ( selectedTree != null ) {
          ISelection structuredSelection = new StructuredSelection( selectedTree );
          treeViewer.setSelection( structuredSelection, true );
          List<File> children = tree.getChildren();
          // Sort by increasing length
          sortFileList( children );
          File currentFile;

          int targetPathArrayIndex;

          // Skip the single "/" when accessing repository
          if ( !children.isEmpty() && children.get( 0 ) instanceof RepositoryFile ) {
            targetPathArrayIndex = 1;
          } else {
            targetPathArrayIndex = 0;
          }

          do {
            currentFile = null;

            if ( targetPathArrayIndex == targetPathArray.length ) {
              break;
            }
            for ( File file : children ) {
              if ( isFileEqual( file, targetPathArray[ targetPathArrayIndex ] ) ) {
                currentFile = file;
                break;
              }
            }
            if ( currentFile instanceof Directory ) {
              treeViewer.setSelection( new StructuredSelection( currentFile ), true );
              treeViewer.setExpandedState( currentFile, true );
              try {
                children = fileController.getFiles( currentFile, null, true );
                // Sort in increasing order
                if ( children.size() > 0 ) {
                  sortFileList( children );
                }
              } catch ( FileException e ) {
                // Ignore
              }
            }
            targetPathArrayIndex++;
          } while ( currentFile != null );
        }
      }
    }
  }

  private boolean isFileEqual( File file, String fileName ) {
    boolean isFileEqual = false;
    if ( file instanceof VFSFile ) {
      if ( file instanceof VFSLocation ) {
        String pathName = ( (VFSLocation) file ).getConnectionPath();
        pathName = pathName.substring( 0, pathName.length() - 1 ); // Removes last "/" for comparison
        isFileEqual = pathName.equals( fileName );
      } else if ( ( (VFSFile) file ).getConnectionPath().equals( fileName ) ) {
        isFileEqual = true;
      }
    } else if ( file.getPath().equals( fileName )
      || ( ( fileName != null && fileName.length() > 0 )
      && file.getPath()
      .equals( fileName.substring( 0, fileName.length() - 1 ) ) ) ) { // Removes last "/" for comparison
      isFileEqual = true;
    }
    return isFileEqual;
  }

  // Sorts file list in increasing order by path length
  private void sortFileList( List<File> children ) {
    if ( children.get( 0 ) instanceof VFSFile ) {
      children.sort(
        ( f1, f2 ) -> ( (VFSFile) f2 ).getConnectionPath().length() - ( ( (VFSFile) f1 ).getConnectionPath()
          .length() ) );
    } else {
      children.sort( ( f1, f2 ) -> ( f2 ).getPath().length() - ( ( f1 ).getPath().length() ) );
    }
  }

  private String[] getStringsAtEachDirectory( String targetPath, char pathSplitter ) {
    String[] targetPathArray;

    int currentTargetPathArrayIndex = 0;
    if ( pathSplitter == '\\' ) {
      int arraySize = targetPath.split( "\\\\" ).length;
      targetPathArray = new String[ arraySize ];
      for ( int i = 0; i < targetPath.length(); i++ ) {
        if ( targetPath.charAt( i ) == pathSplitter || i == targetPath.length() - 1 ) {
          targetPathArray[ currentTargetPathArrayIndex ] = targetPath.substring( 0, i + 1 );
          currentTargetPathArrayIndex++;
        }
      }
    } else {
      // For  VFS File Path
      if ( targetPath.contains( "pvfs://" ) || targetPath.contains( "hc://" ) ) {
        int indexOfDoubleSlash = targetPath.indexOf( "//" ) + 2;
        int arraySize = targetPath.substring( indexOfDoubleSlash ).split( String.valueOf( pathSplitter ) ).length;
        targetPathArray = new String[ arraySize ];

        for ( int i = indexOfDoubleSlash; i < targetPath.length(); i++ ) {
          if ( targetPath.charAt( i ) == pathSplitter ) {
            targetPathArray[ currentTargetPathArrayIndex ] = targetPath.substring( 0, i );
            currentTargetPathArrayIndex++;
          }
        }
      } else {
        // Repository or Linux File Path
        int arraySize = targetPath.split( String.valueOf( pathSplitter ) ).length;
        if ( arraySize == 0 ) {
          return null;
        }
        targetPathArray = new String[ arraySize ];
        targetPathArray[ currentTargetPathArrayIndex ] = String.valueOf( pathSplitter );
        currentTargetPathArrayIndex++;
        for ( int i = currentTargetPathArrayIndex; i < targetPath.length(); i++ ) {
          if ( targetPath.charAt( i ) == pathSplitter ) {
            targetPathArray[ currentTargetPathArrayIndex ] = targetPath.substring( 0, i );
            currentTargetPathArrayIndex++;
          }
        }
      }
      targetPathArray[ currentTargetPathArrayIndex ] = targetPath;
    }
    return targetPathArray;
  }

  private String createFileNameFromPath( String filePath ) {
    String tempName = null;
    if ( filePath != null && filePath.contains( "/" ) ) {
      tempName = filePath.substring( filePath.lastIndexOf( '/' ) + 1 );
    } else if ( filePath != null && filePath.contains( "\\" ) ) {
      tempName = filePath.substring( filePath.lastIndexOf( '\\' ) + 1 );
    }
    return tempName;
  }


  private void clearState() {
    parentPath = null;
    type = null;
    provider = null;
    providerFilter = null;
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

    filenameLabel.setLayoutData( new FormDataBuilder().top( select, 20 ).left( parent, 110 ).result() );
    txtFileName.setLayoutData( new FormDataBuilder().top( select, 20 ).left( filenameLabel, 10 )
      .right( lblComboFilter, -15 ).result() );
    lblComboFilter.setLayoutData(
      new FormDataBuilder().top( select, 20 ).right( typedComboBox.viewer.getCombo(), -5 ).result() );
    typedComboBox.viewer.getCombo()
      .setLayoutData( new FormDataBuilder().top( select, 20 ).right( btnSave, -15 ).result() );

    txtFileName.addModifyListener( modifyEvent -> processState() );

    // Pre-populates passed-in name
    if ( StringUtils.isNotEmpty( this.fileDialogOperation.getFilename() ) ) {
      txtFileName.setText( fileDialogOperation.getFilename() );
    }
    PropsUI.getInstance().setLook( btnSave );
    btnSave.setLayoutData( new FormDataBuilder().top( select, 20 ).right( btnCancel, -15 ).result() );
    btnSave.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.save.button" ) );


    btnSave.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        StructuredSelection structuredSelection;
        StructuredSelection treeSelection;

        if ( fileTableViewer.getSelection().isEmpty() ) {
          structuredSelection = (StructuredSelection) treeViewer.getSelection();
          treeSelection = structuredSelection;
        } else {
          structuredSelection = (StructuredSelection) fileTableViewer.getSelection();
          treeSelection = (StructuredSelection) treeViewer.getSelection();
        }

        if ( structuredSelection.getFirstElement() instanceof File
          && txtFileName.getText() != null
          && StringUtils.isNotEmpty( txtFileName.getText() ) ) {
          processOnSavePressed( (File) structuredSelection.getFirstElement() );
          // clear the parent directory from the cache so the file shows up on next dialog open
          fileController.clearCache( (File) ( treeSelection ).getFirstElement() );
        }
      }
    } );
    btnCancel.setLayoutData( new FormDataBuilder().top( select, 20 ).right( 100, -30 ).result() );
    btnCancel.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.cancel.button" ) );

    getShell().setDefaultButton( btnSave );
    
  }

  private void processOnSavePressed( File file ) {
    if ( file != null ) {
      setStateVariablesFromSelection( file );
      if ( file instanceof RepositoryFile ) {
        path = null; // this path gets ignored; only parentPath used for repo files
      }
      String fileName = txtFileName.getText();
      if ( typedComboBox.getSelection().getId().equalsIgnoreCase( String.valueOf( FilterType.XML ) )
        && !fileName.endsWith( ".xml" ) ) {
        name = fileName + ".xml";
      } else if ( fileDialogOperation.getCommand().equalsIgnoreCase( FileDialogOperation.EXPORT ) ) {
        if ( typedComboBox.getSelection().getId().equalsIgnoreCase( String.valueOf( FilterType.KETTLE_TRANS ) )
          && !fileName.endsWith( ".ktr" ) ) {
          name = fileName + ".ktr";
        } else if ( typedComboBox.getSelection().getId().equalsIgnoreCase( String.valueOf( FilterType.KETTLE_JOB ) )
          && !fileName.endsWith( ".kjb" ) ) {
          name = fileName + ".kjb";
        }
      } else if ( fileDialogOperation.getCommand().equalsIgnoreCase( FileDialogOperation.EXPORT_ALL ) ) {
        name = fileName + ".zip";
      } else {
        name = fileName;
      }
      getShell().dispose();
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
    txtSearch.addTraverseListener( traverseEvent -> {
      if ( traverseEvent.detail == SWT.TRAVERSE_RETURN ) {
        performSearch();
      }
    } );

    FlatButton flatBtnClr =
      new FlatButton( searchComp, SWT.NONE ).setEnabledImage( rasterImage( "img/Close.S_D.svg", 25, 25 ) )
        .setDisabledImage( rasterImage( "img/Close.S_D_disabled.svg", 25, 25 ) ).setEnabled( true )
        .setLayoutData( new RowData() ).addListener(
          new SelectionAdapter() {
            @Override public void widgetSelected( SelectionEvent selectionEvent ) {
              if ( StringUtils.isEmpty( txtSearch.getText() ) ) {
                return;
              }

              txtSearch.setText( "" );
              restorePriorSearchState();
            }
          } );
    txtSearch.addModifyListener( modifyEvent -> flatBtnClr.setEnabled( !StringUtils.isEmpty( txtSearch.getText() ) ) );

    headerComposite.layout();

    return headerComposite;
  }

  private void performSearch() {
    IStructuredSelection treeViewerSelection = (TreeSelection) ( treeViewer.getSelection() );
    selectPath( treeViewerSelection.getFirstElement(), false, true );
    processState();
  }

  private void restorePriorSearchState() {
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

    flatBtnBack =
      new FlatButton( buttons, SWT.NONE ).setEnabledImage( rasterImage( "img/Backwards.S_D.svg", 32, 32 ) )
        .setDisabledImage( rasterImage( "img/Backwards.S_D_disabled.svg", 32, 32 ) )
        .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.back.button" ) )
        .setEnabled( false ).addListener( new SelectionAdapter() {
          @Override public void widgetSelected( SelectionEvent selectionEvent ) {
            if ( !selectionHistory.isEmpty() ) {
              int currentIndex = currentHistoryIndex;
              if ( currentIndex > 0 ) {
                Object previousPath = selectionHistory.get( currentIndex - 1 );
                navigateBtnFlag = true;
                currentHistoryIndex--;
                treeViewer.setSelection( new StructuredSelection( previousPath ) );
                flatBtnForward.setEnabled( true );
              } else {
                flatBtnBack.setEnabled( false );
              }
            } else {
              flatBtnBack.setEnabled( false );
            }
          }
        } );

    flatBtnForward =
      new FlatButton( buttons, SWT.NONE ).setEnabledImage( rasterImage( "img/Forwards.S_D.svg", 32, 32 ) )
        .setDisabledImage( rasterImage( "img/Forwards.S_D_disabled.svg", 32, 32 ) )
        .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.forward.button" ) )
        .setEnabled( false ).setLayoutData( new FormDataBuilder().left( flatBtnBack.getLabel(), 0 ).result() )
        .addListener(
          new SelectionAdapter() {
            @Override public void widgetSelected( SelectionEvent selectionEvent ) {
              if ( !selectionHistory.isEmpty() ) {
                int currentIndex = currentHistoryIndex;
                if ( currentIndex >= 0 && currentIndex < selectionHistory.size() - 1 ) {
                  Object nextPath = selectionHistory.get( currentIndex + 1 );
                  navigateBtnFlag = true;
                  currentHistoryIndex++;
                  treeViewer.setSelection( new StructuredSelection( nextPath ) );
                  flatBtnBack.setEnabled( true );
                  flatBtnForward.setEnabled( currentHistoryIndex != selectionHistory.size() - 1 );
                } else {
                  flatBtnForward.setEnabled( false );
                }
              } else {
                flatBtnForward.setEnabled( false );
              }
            }
          } );

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


    flatBtnDelete =
      new FlatButton( fileButtons, SWT.NONE ).setEnabledImage( rasterImage( "img/Close.S_D.svg", 32, 32 ) )
        .setDisabledImage( rasterImage( "img/Close.S_D_disabled.svg", 32, 32 ) )
        .setToolTipText( BaseMessages.getString( PKG, "file-open-save-plugin.app.delete.button" ) )
        .setLayoutData( new RowData() ).setEnabled( false ).addListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent selectionEvent ) {
            Listener ok = new Listener() {
              @Override
              public void handleEvent( final Event event ) {
                deleteFileOrFolder();
              }
            };

            Listener cancel = new Listener() {
              @Override
              public void handleEvent( final Event event ) { /* do nothing close dialog */ }
            };

            Map<String, Listener> listenerMap = new LinkedHashMap<>();
            listenerMap.put( BaseMessages.getString( "System.Button.OK" ), ok );
            listenerMap.put( BaseMessages.getString( "System.Button.Cancel" ), cancel );
            String title = StringUtils.EMPTY;
            String message = StringUtils.EMPTY;
            List<String> messageList;
            StructuredSelection fileTableViewerSelection = (StructuredSelection) ( fileTableViewer.getSelection() );
            if ( !fileTableViewerSelection.isEmpty() ) {
              File fileOrFolderToDelete = (File) fileTableViewerSelection.getFirstElement();
              if ( fileTableViewerSelection.size() == 1 ) {
                if ( fileOrFolderToDelete instanceof Directory ) {
                  messageList = ( deleteBtnMessages( "folder", fileOrFolderToDelete.getName(), 1 ) );
                } else {
                  messageList = ( deleteBtnMessages( "file", fileOrFolderToDelete.getName(), 1 ) );
                }
              } else {
                messageList = ( deleteBtnMessages( "many", StringUtils.EMPTY, fileTableViewerSelection.size() ) );
              }
              title = messageList.get( 0 );
              message = messageList.get( 1 );
              warningDialog = new WarningDialog( getShell(), title, message, listenerMap );
              warningDialog.open();
            }
          }
        } );

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
      new FormDataBuilder().left( flatBtnForward.getLabel(), 10 ).right( fileButtons, -10 ).height( 32 ).result() );

    txtNav.addTraverseListener( new TraverseListener() {
      @Override public void keyTraversed( TraverseEvent traverseEvent ) {
        if ( traverseEvent.detail == SWT.TRAVERSE_RETURN ) {

          TreeSelection previousSelection =
            treeViewer.getSelection().isEmpty() ? new TreeSelection() : (TreeSelection) treeViewer.getSelection();
          boolean isFilePresent = false;

          List<Object> children = new ArrayList<>();
          if ( !previousSelection.isEmpty() ) {
            Object fileOrTree = previousSelection.isEmpty() ? null : previousSelection.getFirstElement();
            if ( fileOrTree == null ) {
              return;
            }

            if ( fileOrTree instanceof Tree ) {
              children = ( (Tree) fileOrTree ).getChildren();
            } else if ( fileOrTree instanceof File ) {
              TreePath[] treePaths = previousSelection.getPaths();
              Object object = treePaths[ 0 ].getFirstSegment();
              children = ( (Tree) object ).getChildren();
            }
          }

          try {
            String pathToSearchFor;
            pathToSearchFor = txtNav.getText();

            isFilePresent = searchForFileInTreeViewer( pathToSearchFor, children );
          } catch ( FileException e ) {
            // Ignore
          }

          if ( !isFilePresent ) {
            treeViewer.setSelection( previousSelection );
          }
        }
      }
    } );
    return buttons;
  }

  private void addSelectionHistoryItems( Object selectionNode ) {
    selectionHistory.add( selectionNode );
    currentHistoryIndex = selectionHistory.size() - 1;
  }

  public boolean searchForFileInTreeViewer( String path, List<Object> children ) throws FileException {
    Optional<File> file = Optional.empty();
    Optional<File> parent = Optional.empty();

    if ( !children.isEmpty() ) {
      List<File> childrenAsFiles = new ArrayList<>( children.size() );
      for ( Object object : children ) {
        childrenAsFiles.add( (File) object );
      }
      file = getFileMatch( path, childrenAsFiles );
      while ( file.isPresent() ) {

        if ( parent.isPresent() && ( path.endsWith( "/" ) || path.endsWith( "\\" ) ) ) {
          path = path.substring( 0, path.length() - 1 );
        }

        if ( file.get() instanceof VFSFile && ( (VFSFile) file.get() ).getConnectionPath().equals( path ) ) {
          break;
        }
        if ( file.get().getPath().equals( path ) ) {
          break;
        }
        childrenAsFiles = fileController.getFiles( file.get(), null, true );
        if ( file.isPresent() ) {
          treeViewer.setSelection( new StructuredSelection( file.get() ), true );
          parent = file;
          file = getFileMatch( path, childrenAsFiles );
        }
      }
    }
    if ( file.isPresent() && !( file.get() instanceof Directory ) ) {
      treeViewer.setSelection( new StructuredSelection( parent.get() ), true );
      treeViewer.setExpandedState( parent.get(), true );
      fileTableViewer.setSelection( new StructuredSelection( file.get() ), true );
    } else if ( file.isPresent() ) {
      treeViewer.setSelection( new StructuredSelection( file.get() ), true );
      treeViewer.setExpandedState( file.get(), true );
      fileTableViewer.setSelection( new StructuredSelection( file.get() ), true );
    }
    return file.isPresent();
  }

  private Optional<File> getFileMatch( String path, List<File> childrenAsFiles ) {
    Optional<File> file;
    if ( childrenAsFiles.get( 0 ) instanceof VFSFile ) {
      file = childrenAsFiles.stream().filter( f -> {
          boolean pathIsLonger = path.length() > ( (VFSFile) f ).getConnectionPath().length();
          if ( pathIsLonger ) {
            return path.startsWith( ( (VFSFile) f ).getConnectionPath() );
          } else {
            return ( (VFSFile) f ).getConnectionPath().startsWith( path );
          }
        } ).sorted(
          ( f1, f2 ) -> ( (VFSFile) f2 ).getConnectionPath().length() - ( (VFSFile) f1 ).getConnectionPath().length() )
        .filter( f -> path.contains( ( (VFSFile) f ).getConnectionPath() ) ).findFirst();
    } else {
      file = childrenAsFiles.stream().filter( f -> {
          boolean pathIsLonger = path.length() > f.getPath().length();
          if ( pathIsLonger ) {
            return path.startsWith( f.getPath() );
          } else {
            return f.getPath().startsWith( path );
          }
        } ).sorted( ( f1, f2 ) -> f2.getPath().length() - f1.getPath().length() )
        .filter( f -> path.contains( f.getPath() ) ).findFirst();
    }
    return file;
  }

  public void refreshDisplay( SelectionEvent selectionEvent ) {
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
          fileController.clearCache( (File) file );
        }
        treeViewer.collapseAll();
      } else {
        try {
          fileProvider =
            ProviderServiceService.get().get( ( (File) treeViewerSelection.getFirstElement() ).getProvider() );
        } catch ( Exception ex ) {
          log.logDebug( "Unable to find provider" );
        }
        fileController.clearCache( (File) ( treeViewerSelection.getFirstElement() ) );
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
        treeViewer.setInput( fileController.load( ProviderFilterType.ALL_PROVIDERS.toString() ).toArray() );
        treeViewer.refresh( true );
        fileTableViewer.refresh( true );
      } catch ( Exception ex ) {
        // Ignored
      }
    }
  }


  public String getProviderFilter() {
    return providerFilter;
  }

  public void setProviderFilter( String providerFilter ) {
    this.providerFilter = providerFilter;
  }

  private Composite createFilesBrowser( Composite parent ) {
    clrGray = getShell().getDisplay().getSystemColor( SWT.COLOR_GRAY );
    clrBlack = getShell().getDisplay().getSystemColor( SWT.COLOR_BLACK );
    imgTime = rasterImage( "img/Time.S_D.svg", 25, 25 );
    imgVFS = rasterImage( "img/VFS_D.svg", 25, 25 );
    imgFolder = rasterImage( "img/file_icons/Archive.S_D.svg", 25, 25 );
    imgDisk = rasterImage( "img/Disk.S_D.svg", 25, 25 );
    imgFile = rasterImage( "img/file_icons/Doc.S_D.svg", 25, 25 );
    imgTrans = rasterImage( "img/file_icons/Transformation.S_D.svg", 25, 25 );
    imgJob = rasterImage( "img/file_icons/Job.S_D.svg", 25, 25 );
    fileDialogOperation.getCustomImageProvider()
        .ifPresent( provider -> loadCustomImages( provider, customImages, getClass().getClassLoader() ) );
    Composite browser = new Composite( parent, SWT.NONE );
    PropsUI.getInstance().setLook( browser );
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginRight = 0;
    gridLayout.marginLeft = 0;
    browser.setLayout( gridLayout );

    SashForm sashForm = new SashForm( browser, SWT.HORIZONTAL );
    PropsUI.getInstance().setLook( sashForm );
    sashForm.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    treeViewer = new TreeViewer( sashForm, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI );
    PropsUI.getInstance().setLook( treeViewer.getTree() );
    // Add drag/drop support
    Transfer[] dropTransfers = new Transfer[] { ElementTransfer.getInstance(), FileTransfer.getInstance() };
    Transfer[] dragTransfers = new Transfer[] { ElementTransfer.getInstance() };
    int ops = DND.DROP_COPY | DND.DROP_MOVE;

    treeViewer.addDragSupport( ops, dragTransfers, new ElementDragListener( treeViewer, this, log ) );
    treeViewer.addDropSupport( ops, dropTransfers, new ElementTreeDropAdapter( treeViewer, log ) );

    stackLayout = new StackLayout();
    recentComposite = new Composite( sashForm, SWT.BORDER );
    PropsUI.getInstance().setLook( recentComposite );
    recentComposite.setLayout( stackLayout );

    treeViewer.setLabelProvider( labelProvider );

    treeViewer.setContentProvider( new FileTreeContentProvider( fileController, this ) );

    // Load the various tree types on the left
    treeViewer.setInput( fileController.load( providerFilter ).toArray() );

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
      selectPath( selectedNode );
      // Clears the selection from fileTableViewer
      fileTableViewer.setSelection( new StructuredSelection() );
      txtSearch.setText( "" );
      if ( selectionHistory != null ) {
        flatBtnBack.setEnabled( currentHistoryIndex != 0 );
      }

      if ( selectedNode instanceof RepositoryTree && ( (RepositoryTree) selectedNode ).getChildren().isEmpty() ||
        selectedNode instanceof RecentTree && ( (RecentTree) selectedNode ).getChildren().isEmpty() ) {

        if ( noRecentFilesLabel == null ) {
          noRecentFilesLabel = new Label( recentComposite, SWT.CENTER );
          PropsUI.getInstance().setLook( noRecentFilesLabel );
          noRecentFilesLabel.setText(
            BaseMessages.getString( PKG, "file-open-save-plugin.app.middle.no-recents.message" ) );
        }
        stackLayout.topControl = noRecentFilesLabel;
        stackLayout.topControl.getParent().layout();

      } else if ( !( selectedNode instanceof RecentTree ) || !( (RecentTree) selectedNode ).getChildren().isEmpty() ) {
        stackLayout.topControl = fileTableViewer.getControl();
        stackLayout.topControl.getParent().layout();

        if ( !System.getProperty( "user.home" ).equals( fileDialogOperation.getPath() ) && !isFileTreeProcessing ) {
          // To prevent recursive looping at startup
          isFileTreeProcessing = true;
          setPreviousSelection();
        }
      }

      processState();
    } );

    treeViewer.addSelectionChangedListener( new ISelectionChangedListener() {
      @Override public void selectionChanged( SelectionChangedEvent selectionChangedEvent ) {
        IStructuredSelection selection = (IStructuredSelection) selectionChangedEvent.getSelection();
        Object selectedNode = selection.getFirstElement();
        if ( selectionChangedEvent.getSelection() instanceof TreeSelection && !navigateBtnFlag ) {
          addSelectionHistoryItems( selectedNode );
        }
        navigateBtnFlag = false;
      }
    } );

    fileTableViewer = new TableViewer( recentComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION );

    // Add drag/drop support
    fileTableViewer.addDragSupport( ops, dragTransfers, new ElementDragListener( fileTableViewer, this, log ) );

    PropsUI.getInstance().setLook( fileTableViewer.getTable() );
    fileTableViewer.getTable().setHeaderVisible( true );
    Menu fileTableMenu = new Menu( fileTableViewer.getTable() );
    MenuItem copyItem = new MenuItem( fileTableMenu, SWT.NONE );
    copyItem.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.copy" ) );
    SelectionAdapter copyAdapter = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        performCopy( e );
      }
    };
    copyItem.addSelectionListener( copyAdapter );

    MenuItem pasteItem = new MenuItem( fileTableMenu, SWT.NONE );
    pasteItem.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.paste" ) );
    SelectionAdapter pasteAdapter = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        performPaste();
        refreshDisplay( e );
      }
    };
    pasteItem.addSelectionListener( pasteAdapter );

    MenuItem cutItem = new MenuItem( fileTableMenu, SWT.NONE );
    cutItem.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.cut" ) );

    SelectionAdapter cutAdapter = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        performCut( e );
      }
    };
    cutItem.addSelectionListener( cutAdapter );

    MenuItem deleteItem = new MenuItem( fileTableMenu, SWT.NONE );
    deleteItem.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.delete" ) );

    SelectionAdapter deleteAdapter = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        performDelete( e );
      }
    };
    deleteItem.addSelectionListener( deleteAdapter );

    MenuItem renameItem = new MenuItem( fileTableMenu, SWT.NONE );
    renameItem.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.rename" ) );

    SelectionAdapter renameAdapter = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        performRename( e );
      }
    };
    renameItem.addSelectionListener( renameAdapter );

    if ( Spoon.getInstance().rep == null ) {
      fileTableViewer.getTable().setMenu( fileTableMenu );
    }

    fileTableViewer.getTable().addMenuDetectListener( new MenuDetectListener() {
      @Override
      public void menuDetected( MenuDetectEvent e ) {
        pasteItem.setEnabled( false );
        copyItem.setEnabled( false );
        cutItem.setEnabled( false );
        deleteItem.setEnabled( false );
        renameItem.setEnabled( false );
        int selectionIndices[] = fileTableViewer.getTable().getSelectionIndices();
        if ( selectionIndices.length > 0 ) {
          copyItem.setEnabled( true );
          cutItem.setEnabled( true );
          deleteItem.setEnabled( true );
          renameItem.setEnabled( true );
        }
        if ( selectedItems.size() > 0 ) {
          if ( selectionIndices.length == 0 ) {
            IStructuredSelection treeViewerSelection = (IStructuredSelection) treeViewer.getSelection();
            File destFolder = (File) treeViewerSelection.getFirstElement();
            if ( !StringUtils.equalsIgnoreCase( destFolder.getName(),
              selectedItems.stream().findFirst().get().getName() ) ) {
              pasteItem.setEnabled( true );
            }
          } else if ( StringUtils.equalsIgnoreCase(
            fileTableViewer.getTable().getItem( selectionIndices[ 0 ] ).getText( 1 ), "Folder" ) ) {
            if ( !StringUtils.equalsIgnoreCase(
              fileTableViewer.getTable().getItem( selectionIndices[ 0 ] ).getText( 0 ),
              selectedItems.stream().findFirst().get().getName() ) ) {
              pasteItem.setEnabled( true );
            }
          }
        }
      }
    } );

    //Setup the JFace view sorter (in case the user wants to sort the columns)
    fileTableViewer.setSorter( new ViewerSorter() {
      @Override
      public int compare( Viewer viewer, Object e1, Object e2 ) {
        
        if( fileTableViewerComparator != null ) {
          return fileTableViewerComparator.compare( e1, e2 );
        }
        return DEFAULT_FILE_TABLE_VIEWER_COMPARATOR.compare( e1, e2 );
        
      }
    });
    
    // Mouse Listner added to capture an event when the user click on empty space on Dialogue which deselects the
    // file/folder
    fileTableViewer.getTable().addMouseListener( new MouseListener() {

      @Override
      public void mouseDoubleClick( MouseEvent mouseEvent ) {
      }

      @Override
      public void mouseDown( MouseEvent mouseEvent ) {
        if ( fileTableViewer.getTable().getItem( new Point( mouseEvent.x, mouseEvent.y ) ) == null ) {
          fileTableViewer.getTable().deselectAll();
        }
      }

      @Override
      public void mouseUp( MouseEvent e ) {
      }
    } );

    TableViewerColumn tvcName = new TableViewerColumn( fileTableViewer, SWT.NONE );
    fTableNameColumn = tvcName.getColumn();
    fTableNameColumn.setText( BaseMessages.getString( PKG, "file-open-save-plugin.files.name.header" ) );
    fTableNameColumn.setWidth( 250 );
    fTableNameColumn.addListener( SWT.Selection, e -> { sortColumnSelection( fTableNameColumn ); } );
    ColumnLabelProvider clpName = new ColumnLabelProvider() {

      @Override public String getText( Object element ) {
        File f = (File) element;
        return f.getNameDecoded();
      }

      @Override public Image getImage( Object element ) {
        if ( element instanceof File ) {
          File file = (File) element;
          String path = file.getPath();
          return fileDialogOperation.getCustomImageProvider().flatMap( provider -> provider.getImage( path ) )
              .map( customImages::get ).filter( Objects::nonNull ).orElseGet( () -> getImageForFile( file ) );
        }
        return null;
      }

    };

    tvcName.setLabelProvider( clpName );

    TableViewerColumn tvcType = new TableViewerColumn( fileTableViewer, SWT.NONE );
    fTableTypeColumn = tvcType.getColumn();
    fTableTypeColumn.setText( BaseMessages.getString( PKG, "file-open-save-plugin.files.type.header" ) );
    fTableTypeColumn.setWidth( 100 );
    fTableTypeColumn.setResizable( false );
    fTableTypeColumn.addListener( SWT.Selection, e -> { sortColumnSelection( fTableTypeColumn ); } );
    
    tvcType.setLabelProvider( new ColumnLabelProvider() {
      @Override public Color getForeground( Object element ) {
        return clrGray;
      }

      @Override public String getText( Object element ) {
        return super.getText( StringUtils.capitalize( ( (File) element ).getType() ) );
      }
    } );

    TableViewerColumn tvcModified = new TableViewerColumn( fileTableViewer, SWT.NONE );
    fTableModifiedColumn = tvcModified.getColumn();
    fTableModifiedColumn.setText( BaseMessages.getString( PKG, "file-open-save-plugin.files.modified.header" ) );
    fTableModifiedColumn.setWidth( 140 );
    fTableModifiedColumn.setResizable( false );
    fTableModifiedColumn.addListener( SWT.Selection, e -> { sortColumnSelection( fTableModifiedColumn ); } );
    
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
        flatBtnDelete.setEnabled( ( (File) selectedNode ).isCanEdit() );
        processState();
        txtNav.setText( getNavigationPath( (File) selectedNode ) );
      }
    } );

    fileTableViewer.addDoubleClickListener( e -> {
      Object selection = ( (IStructuredSelection) e.getSelection() ).getFirstElement();

      if ( selection instanceof Directory ) {
        List<Directory> parentDirectories = new ArrayList<>();
        Directory directory = (Directory) selection;
        while ( directory != null && !treeViewer.getExpandedState( directory ) ) {
          parentDirectories.add( directory );
          try {
            File file = fileController.getParent( directory );
            if ( file instanceof Directory && StringUtils.isNotEmpty( file.getPath() ) ) {
              directory = (Directory) file;
            } else {
              break;
            }
          } catch ( InvalidFileProviderException ex ) {
            throw new RuntimeException( ex );
          }
        }
        for ( int i = parentDirectories.size() - 1; i >= 0; i-- ) {
          treeViewer.refresh( parentDirectories.get( i ), true );
          treeViewer.setExpandedState( parentDirectories.get( i ), true );
        }
        selectPath( selection, true, false );
        treeViewer.setExpandedState( selection, true );
        treeViewer.setSelection( new StructuredSelection( selection ), true );

        if ( command.contains( FileDialogOperation.SAVE ) || command.equals( FileDialogOperation.SELECT_FOLDER )
          || command.equals( FileDialogOperation.EXPORT ) || command.equals( FileDialogOperation.EXPORT_ALL ) ) {
          parentPath = ( (Directory) selection ).getParent();
          path = ( (Directory) selection ).getPath();
          provider = ( (Directory) selection ).getProvider();
        }
        txtNav.setText( getNavigationPath( (File) selection ) );
      } else if ( selection instanceof File ) {
        File localFile = (File) selection;
        if ( command.equalsIgnoreCase( FileDialogOperation.SELECT_FILE )
          || command.equalsIgnoreCase( FileDialogOperation.OPEN )
          || command.equalsIgnoreCase( FileDialogOperation.IMPORT )
          || command.equalsIgnoreCase( FileDialogOperation.SELECT_FILE_FOLDER ) ) {
          txtNav.setText( getNavigationPath( localFile ) );
          String fileExtension = extractFileExtension( localFile.getPath() );
          if ( isValidFileExtension( fileExtension ) ) {
            setStateVariablesFromSelection( localFile );
            getShell().dispose();
          }
        }
      }
      processState();
    } );

    sashForm.setWeights( new int[] { 1, 2 } );

    return browser;
  }
  
  private void sortColumnSelection( TableColumn selColumn ) {
    
    TableColumn sortColumn = fileTableViewer.getTable().getSortColumn();
    
    int direction = fileTableViewer.getTable().getSortDirection();
    
    if( selColumn.equals( sortColumn ) ) {
      switch( direction ) {
        case SWT.UP:
          direction = SWT.DOWN;
          break;
        case SWT.DOWN:
          direction = SWT.NONE;
          sortColumn = null;
          break;
      }
    } else {
      sortColumn = selColumn;
      direction = SWT.UP;
    }
    fileTableViewer.getTable().setSortColumn( sortColumn );
    fileTableViewer.getTable().setSortDirection( direction );
    
    //Get it again now that we've set it
    sortColumn = fileTableViewer.getTable().getSortColumn();
    
    if( sortColumn != null ) {
    
      Comparator<Object> baseComparator = DEFAULT_FOLDER_VS_FILE_COMPARATOR;
      if( direction == SWT.UP ) {
        baseComparator = baseComparator.reversed();
      }
      
      Comparator<Object> colComparator = null;
      
      if( sortColumn == fTableNameColumn ) {
        colComparator = Comparator.comparing( o -> ((Entity) o).getName(), String.CASE_INSENSITIVE_ORDER );
      } else if( sortColumn == fTableTypeColumn ) {
        colComparator = Comparator.comparing( o -> ((Entity) o).getType() );
      } else if( sortColumn == fTableModifiedColumn ) {
        colComparator = Comparator.comparingLong( o -> { 
          Date d = ((Entity) o).getDate();
          return d == null ? 0 : d.getTime();
        });
      } 
      if( direction == SWT.DOWN ) {
        colComparator = colComparator.reversed();
      }
      fileTableViewerComparator = baseComparator.thenComparing( colComparator );

    } else {
      //no comparator, use default
      fileTableViewerComparator = null;
    }

    fileTableViewer.refresh( true ); 
  }

  private Image getImageForFile( File file ) {
    if ( file instanceof Directory ) {
      return imgFolder;
    } else if ( file != null && file.getType() != null ) {
      if ( file.getType().equals( File.TRANSFORMATION ) ) {
        return imgTrans;
      } else if ( file.getType().equals( File.JOB ) ) {
        return imgJob;
      }
    }
    return imgFile;
  }

  private void performRename( SelectionEvent e ) {
    selectedItems.clear();
    for ( int index : fileTableViewer.getTable().getSelectionIndices() ) {
      File file = (File) fileTableViewer.getTable().getItem( index ).getData();
      selectedItems.add( file );
    }
    selectedItems.forEach( ( file ) -> {
      createRenameDialog( file );
    } );
    refreshDisplay( e );
  }

  private void performDelete( SelectionEvent e ) {
    selectedItems.clear();
    for ( int index : fileTableViewer.getTable().getSelectionIndices() ) {
      File file = (File) fileTableViewer.getTable().getItem( index ).getData();
      selectedItems.add( file );
    }
    fileController.delete( new ArrayList<File>( selectedItems ) );
    refreshDisplay( e );

  }

  private void performCut( SelectionEvent e ) {
    selectedItems.clear();
    for ( int index : fileTableViewer.getTable().getSelectionIndices() ) {
      File file = (File) fileTableViewer.getTable().getItem( index ).getData();
      fileTableViewer.getTable().getItem( index ).setGrayed( true );
      selectedItems.add( file );
      isCutActionSelected = true;
    }
  }

  private void performPaste() {
    selectedItems.forEach( ( file ) -> {
      Result result; //TODO: Use this result to propagate status of paste of each item.
      File destFolder;
      StructuredSelection fileTableViewerSelection = (StructuredSelection) ( fileTableViewer.getSelection() );
      IStructuredSelection treeViewerSelection = (IStructuredSelection) treeViewer.getSelection();
      boolean deleteCutFileFlag = true;
      if ( fileTableViewerSelection.isEmpty() ) {
        destFolder = (File) treeViewerSelection.getFirstElement();
      } else {
        destFolder = (File) fileTableViewerSelection.getFirstElement();
      }
      String newFilePath = getNewFilePath( file.getName(), destFolder );

      if ( fileController.fileExists( destFolder, newFilePath ) == Boolean.TRUE ) {
        if ( !isCutActionSelected ) {
          if ( !isApplyToAll ) {
            createPasteWarningDialog( file.getName() );
          }
          switch( pasteAction ) {
            case PASTE_ACTION_REPLACE:
              copyFile( file, destFolder, newFilePath,
                new OverwriteStatus( getShell(), OverwriteMode.OVERWRITE ) );
              break;
            case PASTE_ACTION_KEEP_BOTH:
              if ( StringUtils.isNotEmpty( newFilePath ) ) {
                result = fileController.getNewName( destFolder, newFilePath );
                if ( result.getStatus() == Result.Status.SUCCESS ) {
                  fileController.copyFile( file, destFolder, (String) result.getData(),
                    new OverwriteStatus( getShell(), OverwriteMode.RENAME ) );
                }
              }
              break;
            case PASTE_ACTION_SKIP:
            default:
              log.logBasic( file.getName() + " is skipped" );
          }
        } else {
          if ( file.getParent() == destFolder.getPath() ) {
            deleteCutFileFlag = false;
          }
        }

      } else {
        result = copyFile( file, destFolder, newFilePath,
          new OverwriteStatus( getShell(), OverwriteMode.CANCEL ) );
      }

      if ( isCutActionSelected && deleteCutFileFlag ) {
        List<File> cutFiles = new ArrayList<File>();
        cutFiles.add( file );
        fileController.delete( cutFiles );
      }
    } );

    pasteAction = null;
    isCutActionSelected = false;
    isApplyToAll = false;
    selectedItems.clear();
  }

  private String getNewFilePath( String fileName, File destFolder ) {
    if ( destFolder instanceof Directory ) {
      if ( destFolder instanceof LocalFile ) {
        return destFolder.getPath() + java.io.File.separator + fileName;
      } else {
        return destFolder.getPath() + "/" + fileName;
      }
    }
    return null;
  }

  private Result copyFile( File file, File destFolder, String path, OverwriteStatus overwriteStatus ) {
    if ( StringUtils.isNotEmpty( path ) ) {
      return fileController.copyFile( file, destFolder, path, overwriteStatus );
    }
    return null;
  }

  private void createRenameDialog( File file ) {
    RenameDialog renameDialog = new RenameDialog( getShell() );
    String renameValue = renameDialog.open( file.getName() );
    if ( renameValue != null ) {
      String newPath = file.getParent() + "/" + renameValue;
      // Prevent rename folder to the same name
      if ( !file.getPath().equals( newPath ) ) {
        fileController.rename( file, newPath, new OverwriteStatus( null, OverwriteMode.OVERWRITE ) );
      }
    }
  }

  private void createPasteWarningDialog( String fileName ) {
    Map<String, PasteConfirmationDialog.ActionListener> actionListeners = new HashMap<>();
    PasteConfirmationDialog.ActionListener skipListener = ( event, applyToAll ) -> {
      isApplyToAll = applyToAll;
      pasteAction = PASTE_ACTION_SKIP;
    };

    PasteConfirmationDialog.ActionListener keepBothListener = ( event, applyToAll ) -> {
      isApplyToAll = applyToAll;
      pasteAction = PASTE_ACTION_KEEP_BOTH;
    };
    PasteConfirmationDialog.ActionListener replaceListener = ( event, applyToAll ) -> {
      isApplyToAll = applyToAll;
      pasteAction = PASTE_ACTION_REPLACE;
    };
    actionListeners.put( BaseMessages.getString( PKG, "file-open-save-plugin.app.skip.button" ), skipListener );
    actionListeners.put( BaseMessages.getString( PKG, "file-open-save-plugin.app.keepBoth.button" ), keepBothListener );
    actionListeners.put( BaseMessages.getString( PKG, "file-open-save-plugin.app.replace.button" ), replaceListener );
    PasteConfirmationDialog pasteConfirmationDialog = new PasteConfirmationDialog( getShell(), actionListeners );
    pasteConfirmationDialog.open( fileName );
  }


  private void performCopy( SelectionEvent e ) {
    selectedItems.clear();
    for ( int index : fileTableViewer.getTable().getSelectionIndices() ) {
      File file = (File) fileTableViewer.getTable().getItem( index ).getData();
      selectedItems.add( file );
    }
  }

  private void setButtonOpenState() {
    if ( btnOpen != null && !getShell().isDisposed() ) {
      openStructuredSelectionPath( (IStructuredSelection) treeViewer.getSelection() );

      openStructuredSelectionPath( (IStructuredSelection) fileTableViewer.getSelection() );

      boolean canOpen = canOpen( command, name, path ) && fileDialogOperation.getOpenCondition().test( path );
      btnOpen.setEnabled( canOpen );
    }
  }

  private boolean canOpen( String command, String name, String path ) {
      if ( command.equalsIgnoreCase( FileDialogOperation.SELECT_FILE_FOLDER ) ) {
        return StringUtils.isNotEmpty( path ) || StringUtils.isNotEmpty( name );
      } else if ( command.equals( FileDialogOperation.SELECT_FOLDER ) ) {
        return StringUtils.isNotEmpty( path ) && StringUtils.isEmpty( name );
      } else if ( command.equals( FileDialogOperation.SELECT_FILE ) || command.equals( FileDialogOperation.OPEN )
        || command.equals( FileDialogOperation.IMPORT ) ) {
        return StringUtils.isNotEmpty( name );
      } else {
        return false;
      }
  }

  private boolean isSaveState() {
    return fileDialogOperation.isSaveCommand();
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
        if ( fileDialogOperation.isSaveCommand() && StringUtils.isNotEmpty( txtFileName.getText() ) ) {
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
      setStateVariablesFromSelection( (File) selectedFileTreeViewer.getFirstElement() );
      name = null;
    } else if ( selectedFileTreeViewer != null && selectedFileTreeViewer.getFirstElement() instanceof File ) {
      String tempName = createFileNameFromPath( ( (File) selectedFileTreeViewer.getFirstElement() ).getPath() );

      if ( command.equals( FileDialogOperation.SELECT_FILE )
        || command.equalsIgnoreCase( FileDialogOperation.SELECT_FILE_FOLDER )
        || command.equals( FileDialogOperation.OPEN ) || command.equals( FileDialogOperation.IMPORT ) ) {

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
      setStateVariablesFromSelection( (File) selectedFileTreeViewer.getFirstElement() );
    }
  }

  private boolean isValidFileExtension( String fileExtension ) {
    return typedComboBox.getSelection().getId().equals( ALL_FILE_TYPES )
      || Utils.matches( fileExtension, typedComboBox.getSelection().getValue() );
  }

  private String extractFileExtension( String fullFilePath ) {
    int lastIndexOfPeriod = fullFilePath.lastIndexOf( FILE_PERIOD );
    String fileExtension = ( lastIndexOfPeriod == -1 )
      ? StringUtils.EMPTY : fullFilePath.substring( lastIndexOfPeriod );
    return fileExtension;
  }

  private FileProvider determineProviderFromFilePath( FileDialogOperation fileDialogOperation ) {
    try {
      String targetPath = fileDialogOperation.getPath();
      if ( StringUtils.isNotEmpty( targetPath ) ) {
        // If the path is file, set the parent to be the target path
        java.io.File filePath = new java.io.File( targetPath );
        if ( filePath.isFile() ) {
          boolean isKettleFile =
            filePath.getName() != null && ( filePath.getName().contains( FilterType.KTR.toString() )
              || filePath.getName().contains( FilterType.KJB.toString() ) );
          // Path is a file. It is coming from browse that is embedded in steps and job entry
          if ( fileDialogOperation.getProvider() == null ) {
            // Since the provider is null this could be local provider
            return ProviderServiceService.get().get( ProviderFilterType.LOCAL.toString() );
          } else if ( !isKettleFile && ProviderFilterType.REPOSITORY.toString().equalsIgnoreCase( fileDialogOperation.getProvider() ) ) {
            // It is user file which is not a transformation or job and the user is connected to repository.
            // Default the provider to local
            return ProviderServiceService.get().get( ProviderFilterType.LOCAL.toString() );
          } else if ( ProviderFilterType.REPOSITORY.toString().equalsIgnoreCase( fileDialogOperation.getProvider() ) ) {
            // It is a kettle transformation or job and user is connected to repository, set the provider to repository
            return ProviderServiceService.get().get( ProviderFilterType.REPOSITORY.toString() );
          }
        }
        // Set the provider file dialog operation.
        ProviderFilterType providerFilterType =
          ProviderFilterType.valueOf( fileDialogOperation.getProvider().toUpperCase() );
        return ProviderServiceService.get().get( providerFilterType.toString() );
      }
      // Path is null so there can't be a provider
      return null;

    } catch ( InvalidFileProviderException e ) {
      return null;
    }
  }

  private void saveStructuredSelectionPath( IStructuredSelection selection ) {
    IStructuredSelection selectedFileTreeViewer = selection.isEmpty() ? null : selection;
    if ( selectedFileTreeViewer != null && selectedFileTreeViewer.getFirstElement() instanceof File ) {
      setStateVariablesFromSelection( (File) selectedFileTreeViewer.getFirstElement() );
    }
  }

  private void setStateVariablesFromSelection( File f ) {
    type = f.getType();
    provider = f.getProvider();
    name = f.getName();
    connection = ( f instanceof VFSFile ) ? ( (VFSFile) f ).getConnection() : null;
    objectId = ( f instanceof RepositoryFile ) ? ( (RepositoryFile) f ).getObjectId() : null;

    if ( isSaveState() ) {
      path = ( f instanceof Directory ) ? f.getPath() : f.getParent();
      try {
        File parentFile = ProviderServiceService.get().get( provider ).getParent( f );
        if ( null != parentFile ) {
          parentPath = parentFile.getParent();
        } else {
          parentPath = f.getParent();
        }
      } catch ( InvalidFileProviderException e ) {
        new ErrorDialog( getShell(), "Error",
          "Error getting parent of parent file", e, false );
      }
      if ( f instanceof VFSFile ) {
        path =
          ( f instanceof Directory ) ? ( (VFSFile) f ).getConnectionPath() : ( (VFSFile) f ).getConnectionParentPath();
        parentPath = path;
      }
    } else {
      path = f.getPath();
      parentPath = f.getParent();
      if ( f instanceof VFSFile ) {
        path = ( (VFSFile) f ).getConnectionPath();
        parentPath = ( (VFSFile) f ).getConnectionParentPath();
      }
    }
    if ( f instanceof RepositoryFile ) {
      // path should be null, but we don't set this null until the end; it's used as an internal state variable too much
      parentPath = ( f instanceof Directory ) ? f.getPath() : f.getParent();
    }
  }

  private void deleteFileOrFolder() {
    StructuredSelection fileTableViewerSelection = (StructuredSelection) ( fileTableViewer.getSelection() );
    try {
      List<Object> selection = null;
      Object treeViewerDestination = null;
      TreeSelection treeViewerSelection = (TreeSelection) ( treeViewer.getSelection() );
      if ( !fileTableViewerSelection.isEmpty() ) {
        selection = fileTableViewerSelection.toList();
        treeViewerDestination = treeViewerSelection.getFirstElement();
      }
      if ( selection.isEmpty() ) {
        return;
      }
      FileProvider fileProvider = null;
      if ( fileTableViewerSelection.getFirstElement() instanceof File ) {
        fileProvider = ProviderServiceService.get().get( ( (File) selection.get( 0 ) ).getProvider() );
      }

      if ( fileProvider != null ) {
        List<File> selectionList = new ArrayList<>();
        for ( Object file : selection ) {
          selectionList.add( (File) file );
        }
        Result result = fileController.delete( selectionList );
        List<File> filesToDelete = (List<File>) result.getData();
        if ( filesToDelete.size() > 0 ) {
          fileController.clearCache( (File) treeViewerDestination );
          treeViewer.refresh( treeViewerDestination, true );
          selectPath( treeViewerDestination );
          treeViewer.setSelection( treeViewerSelection, true );
        } else {
          throw new FileException();
        }
      }
    } catch ( FileException | InvalidFileProviderException ex ) {
      File fileOrFolderToDelete = (File) fileTableViewerSelection.getFirstElement();
      String title = StringUtils.EMPTY;
      String message = StringUtils.EMPTY;
      if ( fileOrFolderToDelete instanceof Directory ) {
        title = "file-open-save-plugin.error.unable-to-delete-folder.title";
        message = "file-open-save-plugin.error.unable-to-delete-folder.message";
      } else {
        title = "file-open-save-plugin.error.unable-to-delete-file.title";
        message = "file-open-save-plugin.error.unable-to-delete-file.message";
      }
      new ErrorDialog( getShell(), BaseMessages.getString( PKG, title ),
        BaseMessages.getString( PKG, message ), ex, false );
    }
  }

  private List<String> deleteBtnMessages( String fileType, String fileName, int fileSelectionCount ) {
    List<String> messageList = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    String title = "file-open-save-plugin.error.delete-" + fileType + ".title";
    messageList.add( BaseMessages.getString( PKG, title ) );
    String messageBefore;
    String messageAfter;
    if ( StringUtils.equalsIgnoreCase( fileType, "file" ) && fileSelectionCount == 1 ) {
      messageBefore = BaseMessages.getString( PKG, "file-open-save-plugin.error.delete-" + fileType + ".message" );
      messageAfter = "?";
    } else {
      messageBefore =
        BaseMessages.getString( PKG, "file-open-save-plugin.error.delete-" + fileType + ".before.message" );
      messageAfter = BaseMessages.getString( PKG, "file-open-save-plugin.error.delete-" + fileType + ".after.message" );
    }
    sb.append( messageBefore );
    sb.append( " " );
    if ( fileSelectionCount > 1 ) {
      sb.append( fileSelectionCount );
      sb.append( " " );
    } else {
      sb.append( fileName );
      sb.append( " " );
    }
    sb.append( messageAfter );
    messageList.add( String.valueOf( sb ) );
    return messageList;
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
        File file = fileProvider.createDirectory( parentPathOfSelection, (File) selection, newFolderName );
        if ( file == null ) {
          Listener cancel = event -> { /* do nothing close dialog */ };
          Map<String, Listener> listenerMap = new LinkedHashMap<>();
          listenerMap.put( BaseMessages.getString( "System.Button.Cancel" ), cancel );
          warningDialog = new WarningDialog( getShell(),
            BaseMessages.getString( PKG, "file-open-save-plugin.error.unable-to-create-folder.title" ),
            BaseMessages.getString( PKG, "file-open-save-plugin.error.unable-to-create-folder.message" ), listenerMap );
          warningDialog.open();
        }
        fileController.clearCache( (File) treeViewerDestination );
        treeViewer.refresh( treeViewerDestination, true );

        selectPath( treeViewerDestination );

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
        BaseMessages.getString( PKG, "file-open-save-plugin.error.unable-to-create-folder.title" ), ex, false );
    }
    return false;
  }

  private Image rasterImage( String path, int width, int height ) {
    return rasterImage( getClass().getClassLoader(), path, width, height );
  }

  private Image rasterImage( ClassLoader classLoader, String path, int width, int height ) {
    SwtUniversalImage img =
      SwtSvgImageUtil.getUniversalImage( getShell().getDisplay(), classLoader, path );
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

  /**
   * Separate VFS connection name variable is no longer needed.
   * @deprecated
   * The connection name is in the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  public String getConnection() {
    return connection;
  }

  /**
   * Separate VFS connection name variable is no longer needed.
   * @deprecated
   * The connection name is in the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
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
    selectPath( selectedElement, true, false );
  }

  protected void selectPath( Object selectedElement, boolean useCache, boolean search ) {

    if ( selectedElement instanceof Tree ) {

      List<Object> children = ( (Tree) selectedElement ).getChildren();
      if ( children != null ) {
        fileTableViewer.setInput( children.toArray() );
        // Sets state to blank
        parentPath = null;
        path = null;
        name = null;
        txtNav.setText( ( (Tree<?>) selectedElement ).getName() );
      }
      processState();

    } else if ( selectedElement instanceof Directory ) {
      if ( !search ) {
        txtSearch.setText( "" );
      }
      String searchString = txtSearch.getText();
      BusyIndicator.showWhile( this.getShell().getDisplay(), () -> {
        try {
          String currentFilters = typedComboBox.getSelection().getId().equals( ALL_FILE_TYPES ) ? null
            : typedComboBox.getSelection().getValue();
          if ( StringUtils.isNotEmpty( currentFilters ) ) {
            currentFilters = currentFilters.replace( "\\", "" );
          }
          List<File> files = search ?
            fileController.searchFiles( (File) selectedElement, currentFilters, searchString ) :
            fileController.getFiles( (File) selectedElement, currentFilters, useCache );
          if ( fileDialogOperation.isShowOnlyFolders() ) {
            files.removeIf( f -> !( f instanceof Directory ) );
          }
          fileTableViewer.setInput( files.stream()
            .sorted( DEFAULT_FILE_TABLE_VIEWER_COMPARATOR )
            .toArray() );
        } catch ( FileException e ) {
          new ErrorDialog( getShell(), "Error",
            "Error populating file table", e, false );
        }
      } );

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
    }

  }

  protected String getNavigationPath( File file ) {
    return file instanceof VFSFile ? ( (VFSFile) file ).getConnectionPath() : ( file ).getPath();
  }

  protected LogChannelInterface getLog() {
    return log;
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
    private final FileOpenSaveDialog parentDialog;

    public FileTreeContentProvider( FileController fileController, FileOpenSaveDialog fileOpenSaveDialog ) {
      this.fileController = fileController;
      this.parentDialog = fileOpenSaveDialog;
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
        List<File> childrenList = new ArrayList<>();
        BusyIndicator.showWhile( parentDialog.getShell().getDisplay(), () -> {
          try {
            childrenList.addAll( fileController.getFiles( (Directory) parentElement, null, true ).stream()
              .filter( Directory.class::isInstance )
              .sorted( Comparator.comparing( Entity::getName, String.CASE_INSENSITIVE_ORDER ) )
              .collect( Collectors.toList() ) );
          } catch ( FileException e ) {
            new ErrorDialog( parentDialog.getShell(), "Error",
              "Error populating file tree", e, false );
          }
        } );
        return childrenList.toArray();
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
