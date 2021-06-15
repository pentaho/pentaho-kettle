/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Austin Riddle (Texas Center for Applied Technology) - RAP implementation
 *    EclipseSource - ongoing development
 *******************************************************************************/
package org.eclipse.swt.widgets;

import static org.eclipse.swt.internal.widgets.LayoutUtil.createButtonLayoutData;
import static org.eclipse.swt.internal.widgets.LayoutUtil.createFillData;
import static org.eclipse.swt.internal.widgets.LayoutUtil.createGridLayout;
import static org.eclipse.swt.internal.widgets.LayoutUtil.createHorizontalFillData;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.rap.fileupload.DiskFileUploadReceiver;
import org.eclipse.rap.fileupload.FileUploadHandler;
import org.eclipse.rap.fileupload.UploadSizeLimitExceededException;
import org.eclipse.rap.fileupload.UploadTimeLimitExceededException;
import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;
import org.eclipse.rap.rwt.internal.RWTMessages;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.FileUploadRunnable;
import org.eclipse.swt.internal.widgets.ProgressCollector;
import org.eclipse.swt.internal.widgets.UploadPanel;
import org.eclipse.swt.internal.widgets.Uploader;
import org.eclipse.swt.internal.widgets.UploaderService;
import org.eclipse.swt.internal.widgets.UploaderWidget;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;


/**
 * Instances of this class allow the user to navigate the file system and select
 * a file name. The selected file will be uploaded to the server and the path
 * made available as the result of the dialog.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd><!--SAVE, OPEN,--> MULTI</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * The OPEN style is applied by default and setting any other styles has no
 * effect. <!--Note: Only one of the styles SAVE and OPEN may be specified.-->
 * </p>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em> within the
 * SWT implementation.
 * </p>
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#filedialog">FileDialog
 *      snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      ControlExample, Dialog tab</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 */
@SuppressWarnings( "restriction" )
public class FileDialog extends Dialog {

  private static final String[] EMPTY_ARRAY = new String[ 0 ];

  private final ServerPushSession pushSession;
  private ThreadPoolExecutor singleThreadExecutor;
  private Display display;
  private ScrolledComposite uploadsScroller;
  private Button okButton;
  private Label spacer;
  private UploadPanel placeHolder;
  private ProgressCollector progressCollector;
  private ClientFile[] clientFiles;
  private String[] filterExtensions;
  private long sizeLimit = -1;
  private long timeLimit = -1;
  private File uploadDirectory;
  /**
   * Constructs a new instance of this class given only its parent.
   *
   * @param parent a shell which will be the parent of the new instance
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   */
  public FileDialog( Shell parent ) {
    this( parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL );
  }

  /**
   * Constructs a new instance of this class given its parent and a style value
   * describing its behavior and appearance.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must be
   * built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
   * constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a shell which will be the parent of the new instance
   * @param style the style of dialog to construct
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   */
  public FileDialog( Shell parent, int style ) {
    super( parent, checkStyle( parent, style ) );
    checkSubclass();
    pushSession = new ServerPushSession();
  }

  /**
   * Returns the path of the first file that was selected in the dialog relative
   * to the filter path, or an empty string if no such file has been selected.
   *
   * @return the relative path of the file
   */
  public String getFileName() {
    String[] fileNames = getFileNames();
    return fileNames.length == 0 ? "" : fileNames[ 0 ];
  }

  /**
   * Returns a (possibly empty) array with the paths of all files that were
   * selected in the dialog relative to the filter path.
   *
   * @return the relative paths of the files
   */
  public String[] getFileNames() {
    if( returnCode == SWT.OK ) {
      String[] completedFileNames = getCompletedFileNames();
      if( isMulti() || completedFileNames.length == 0 ) {
        return completedFileNames;
      }
      return new String[] { completedFileNames[ completedFileNames.length - 1 ] };
    }
    return EMPTY_ARRAY;
  }

  /**
   * Set the file extensions which the dialog will
   * use to filter the files it shows to the argument,
   * which may be null.
   * <p>
   * An extension filter string must be of the form ".extension".
   * </p>
   *
   * @param extensions the file extension filter
   *
   * @since 3.2
   */
  public void setFilterExtensions( String[] extensions ) {
    filterExtensions = extensions;
  }

  /**
   * Returns the file extensions which the dialog will
   * use to filter the files it shows.
   *
   * @return the file extensions filter
   *
   * @since 3.2
   */
  public String[] getFilterExtensions() {
    return filterExtensions;
  }

  /**
   * Sets initial client files to be uploaded. The upload of these files will start immediately
   * after opening the dialog. Hence, this method must be called before opening the dialog.
   * <p>
   * A user can drag and drop files from the client operating system on any control with a drop
   * listener attached. In this case, the client files can be obtained from the
   * {@link ClientFileTransfer} object. This FileDialog can then be used to handle the upload and
   * display upload progress.
   * </p>
   *
   * @param files an array of client files to be added to the dialog
   *
   * @rwtextension This method is not available in SWT.
   * @since 3.1
   */
  public void setClientFiles( ClientFile[] files ) {
    clientFiles = files;
  }

  /**
   * Sets the maximum upload size in bytes. If upload size is bigger it will be interrupted.
   * A value of -1 indicates no limit.
   *
   * @param limit the maximum upload size in bytes
   *
   * @see UploadSizeLimitExceededException
   *
   * @rwtextension This method is not available in SWT.
   * @since 3.3
   */
  public void setUploadSizeLimit( long limit ) {
    sizeLimit = limit;
  }

  /**
   * Returns the maximum upload size in bytes. The default value of -1 indicates no limit.
   *
   * @rwtextension This method is not available in SWT.
   * @since 3.3
   */
  public long getUploadSizeLimit() {
    return sizeLimit;
  }

  /**
   * Set the directory where the files should be uploaded to. If no directory is set or it is set to
   * <code>null</code> a temporary directory is used.
   *
   *  @param directory the directory to upload to
   *  @rwtextension This method is not available in SWT.
   *  @since 3.7
   */
  public void setUploadDirectory( File directory ) {
    uploadDirectory = directory;
  }

  /**
   * Returns the directory where the files should be uploaded to, or <code>null</code>
   * when a temporary directory is used.
   *
   *  @rwtextension This method is not available in SWT.
   *  @since 3.7
   */
  public File getUploadDirectory() {
    return uploadDirectory;
  }

  /**
   * Sets the maximum upload duration in milliseconds. If upload takes longer it will be
   * interrupted. The default value of -1 indicates no limit.
   *
   * @param limit the maximum upload duration in milliseconds
   *
   * @see UploadTimeLimitExceededException
   *
   * @rwtextension This method is not available in SWT.
   * @since 3.3
   */
  public void setUploadTimeLimit( long limit ) {
    timeLimit = limit;
  }

  /**
   * Returns the maximum upload duration in milliseconds. The default value of -1 indicates no
   * limit.
   *
   * @rwtextension This method is not available in SWT.
   * @since 3.3
   */
  public long getUploadTimeLimit() {
    return timeLimit;
  }

  /**
   * Returns a list with exceptions thrown during the file upload. Will never return null.
   *
   * @see UploadSizeLimitExceededException
   * @see UploadTimeLimitExceededException
   *
   * @rwtextension This method is not available in SWT.
   * @since 3.3
   */
  public java.util.List<Exception> getExceptions() {
    return progressCollector.getUploadExceptions();
  }

  /**
   * Makes the dialog visible and brings it to the front
   * of the display.
   *
   * <!-- Begin RAP specific -->
   * <p><strong>RAP Note:</strong> This method is not supported when running the application in
   * JEE_COMPATIBILITY mode. Use <code>Dialog#open(DialogCallback)</code> instead.</p>
   * <!-- End RAP specific -->
   *
   * @return a string describing the absolute path of the first selected file,
   *         or null if the dialog was cancelled or an error occurred
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the dialog has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the dialog</li>
   * </ul>
   */
  public String open() {
    checkOperationMode();
    prepareOpen();
    runEventLoop( shell );
    String fileName = getFileName();
    return returnCode == SWT.CANCEL || "".equals( fileName ) ? null : fileName;
  }

  @Override
  protected void prepareOpen() {
    createShell();
    createControls();
    initializeBounds();
    initializeDefaults();
    pushSession.start();
    singleThreadExecutor = createSingleThreadExecutor();
    if( clientFiles != null && clientFiles.length > 0 ) {
      handleFileDrop( clientFiles );
    }
  }

  private void createShell() {
    shell = new Shell( getParent(), getStyle() );
    shell.setText( getText() );
    shell.addDisposeListener( new DisposeListener() {
      @Override
      public void widgetDisposed( DisposeEvent event ) {
        cleanup();
      }
    } );
    display = shell.getDisplay();
  }

  private void initializeBounds() {
    Point prefSize = shell.computeSize( SWT.DEFAULT, SWT.DEFAULT );
    prefSize.y += isMulti() ? 165 : 10;
    shell.setMinimumSize( prefSize );
    Rectangle displaySize = getParent().getDisplay().getBounds();
    int locationX = ( displaySize.width - prefSize.x ) / 2 + displaySize.x;
    int locationY = ( displaySize.height - prefSize.y ) / 2 + displaySize.y;
    shell.setBounds( locationX, locationY, prefSize.x, prefSize.y );
    // set spacer real layout data after shell prefer size calculation
    spacer.setLayoutData( createHorizontalFillData() );
  }

  private void initializeDefaults() {
    setReturnCode( SWT.CANCEL );
  }

  private void createControls() {
    shell.setLayout( createGridLayout( 1, 10, 10 ) );
    createDialogArea( shell );
    createButtonsArea( shell );
  }

  private void createDialogArea( Composite parent ) {
    Composite dialogArea = new Composite( parent, SWT.NONE );
    dialogArea.setLayoutData( createFillData() );
    dialogArea.setLayout( createGridLayout( 1, 0, 5 ) );
    createUploadsArea( dialogArea );
    createProgressArea( dialogArea );
    createDropTarget( dialogArea );
  }

  private void createUploadsArea( Composite parent ) {
    uploadsScroller = new ScrolledComposite( parent, isMulti() ? SWT.V_SCROLL : SWT.NONE );
    uploadsScroller.setLayoutData( createFillData() );
    uploadsScroller.setExpandHorizontal( true );
    uploadsScroller.setExpandVertical( true );
    Composite scrolledContent = new Composite( uploadsScroller, SWT.NONE );
    scrolledContent.setLayout( new GridLayout( 1, false ) );
    uploadsScroller.setContent( scrolledContent );
    uploadsScroller.addControlListener( new ControlAdapter() {
      @Override
      public void controlResized( ControlEvent event ) {
        updateScrolledComposite();
      }
    } );
    placeHolder = createPlaceHolder( scrolledContent );
  }

  private UploadPanel createPlaceHolder( Composite parent ) {
    String text = isMulti()
                ? RWTMessages.getMessage( "RWT_FileDialogMultiUploadPanelMessage" )
                : RWTMessages.getMessage( "RWT_FileDialogSingleUploadPanelMessage" );
    UploadPanel panel = new UploadPanel( parent, new String[] { text } );
    panel.setLayoutData( createHorizontalFillData() );
    return panel;
  }

  private void createProgressArea( Composite parent ) {
    progressCollector = new ProgressCollector( parent );
    progressCollector.setLayoutData( createHorizontalFillData() );
  }

  private void createDropTarget( Control control ) {
    DropTarget dropTarget = new DropTarget( control, DND.DROP_MOVE | DND.DROP_COPY );
    dropTarget.setTransfer( new Transfer[] { ClientFileTransfer.getInstance() } );
    dropTarget.addDropListener( new DropTargetAdapter() {
      @Override
      public void dropAccept( DropTargetEvent event ) {
        if( !ClientFileTransfer.getInstance().isSupportedType( event.currentDataType ) ) {
          event.detail = DND.DROP_NONE;
        }
      }
      @Override
      public void drop( DropTargetEvent event ) {
        handleFileDrop( ( ClientFile[] )event.data );
      }
    } );
  }

  private void handleFileDrop( ClientFile[] clientFiles ) {
    placeHolder.dispose();
    if( !isMulti() ) {
      clearUploadArea();
    }
    ClientFile[] files = isMulti() ? clientFiles : new ClientFile[] { clientFiles[ 0 ] };
    UploadPanel uploadPanel = createUploadPanel( getFileNames( files ) );
    updateScrolledComposite();
    Uploader uploader = new UploaderService( files );
    DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();
    receiver.setUploadDirectory( uploadDirectory );
    FileUploadHandler handler = new FileUploadHandler(receiver);
    handler.setMaxFileSize( sizeLimit );
    handler.setUploadTimeLimit( timeLimit );
    FileUploadRunnable uploadRunnable = new FileUploadRunnable( uploadPanel,
                                                                progressCollector,
                                                                uploader,
                                                                handler );
    singleThreadExecutor.execute( uploadRunnable );
  }

  private static String[] getFileNames( ClientFile[] clientFiles ) {
    String[] fileNames = new String[ clientFiles.length ];
    for( int i = 0; i < fileNames.length; i++ ) {
      fileNames[ i ] = clientFiles[ i ].getName();
    }
    return fileNames;
  }

  private void createButtonsArea( Composite parent ) {
    Composite buttonsArea = new Composite( parent, SWT.NONE );
    buttonsArea.setLayout( createGridLayout( 4, 0, 5 ) );
    buttonsArea.setLayoutData( createHorizontalFillData() );
    String text = isMulti() ? SWT.getMessage( "SWT_Add" ) : SWT.getMessage( "SWT_Browse" );
    createFileUpload( buttonsArea, text );
    createSpacer( buttonsArea );
    okButton = createButton( buttonsArea, SWT.getMessage( "SWT_OK" ) );
    parent.getShell().setDefaultButton( okButton );
    okButton.forceFocus();
    okButton.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event event ) {
        okPressed();
      }
    } );
    Button cancelButton = createButton( buttonsArea, SWT.getMessage( "SWT_Cancel" ) );
    cancelButton.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event event ) {
        cancelPressed();
      }
    } );
  }

  protected FileUpload createFileUpload( Composite parent, String text ) {
    FileUpload fileUpload = new FileUpload( parent, isMulti() ? SWT.MULTI : SWT.NONE );
    fileUpload.setText( text );
    fileUpload.setLayoutData( createButtonLayoutData( fileUpload ) );
    if( filterExtensions != null ) {
      fileUpload.setFilterExtensions( filterExtensions );
    }
    fileUpload.addListener( SWT.Selection, new Listener() {
      @Override
      public void handleEvent( Event event ) {
        handleFileUploadSelection( ( FileUpload )event.widget );
      }
    } );
    fileUpload.moveAbove( null );
    return fileUpload;
  }

  private void createSpacer( Composite buttonArea ) {
    spacer = new Label( buttonArea, SWT.NONE );
    spacer.setLayoutData( createButtonLayoutData( spacer ) );
  }

  protected Button createButton( Composite parent, String text ) {
    Button button = new Button( parent, SWT.PUSH );
    button.setText( text );
    button.setLayoutData( createButtonLayoutData( button ) );
    return button;
  }

  private void handleFileUploadSelection( FileUpload fileUpload ) {
    placeHolder.dispose();
    if( !isMulti() ) {
      clearUploadArea();
    }
    UploadPanel uploadPanel = createUploadPanel( fileUpload.getFileNames() );
    updateScrolledComposite();
    updateButtonsArea( fileUpload );
    Uploader uploader = new UploaderWidget( fileUpload );
    DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();
    receiver.setUploadDirectory( uploadDirectory );
    FileUploadHandler handler = new FileUploadHandler( receiver );
    handler.setMaxFileSize( sizeLimit );
    handler.setUploadTimeLimit( timeLimit );
    FileUploadRunnable uploadRunnable = new FileUploadRunnable( uploadPanel,
                                                                progressCollector,
                                                                uploader,
                                                                handler );
    singleThreadExecutor.execute( uploadRunnable );
  }

  private void updateScrolledComposite() {
    Composite content = ( Composite )uploadsScroller.getContent();
    for( int i = 0; i < 2; i++ ) { // workaround for bug 414868
      Rectangle clientArea = uploadsScroller.getClientArea();
      Point minSize = content.computeSize( clientArea.width, SWT.DEFAULT );
      uploadsScroller.setMinSize( minSize );
    }
    uploadsScroller.setOrigin( 0, 10000 );
    content.layout();
  }

  private void updateButtonsArea( FileUpload fileUpload ) {
    Composite buttonsArea = fileUpload.getParent();
    hideControl( fileUpload );
    String text = isMulti() ? SWT.getMessage( "SWT_Add" ) : SWT.getMessage( "SWT_Browse" );
    createFileUpload( buttonsArea, text );
    buttonsArea.layout();
  }

  private UploadPanel createUploadPanel( String[] fileNames ) {
    Composite parent = ( Composite )uploadsScroller.getContent();
    UploadPanel uploadPanel = new UploadPanel( parent, fileNames );
    uploadPanel.setLayoutData( createHorizontalFillData() );
    return uploadPanel;
  }

  private void clearUploadArea() {
    Composite parent = ( Composite )uploadsScroller.getContent();
    for( Control child : parent.getChildren() ) {
      child.dispose();
    }
  }

  private static void hideControl( Control control ) {
    if( control != null ) {
      GridData layoutData = ( GridData )control.getLayoutData();
      layoutData.exclude = true;
      control.setVisible( false );
    }
  }

  private void setButtonEnabled( final boolean enabled ) {
    if( !display.isDisposed() ) {
      display.asyncExec( new Runnable() {
        @Override
        public void run() {
          if( !okButton.isDisposed() ) {
            okButton.setEnabled( enabled );
          }
        }
      } );
    }
  }

  private void okPressed() {
    setReturnCode( SWT.OK );
    close();
  }

  private void cancelPressed() {
    setReturnCode( SWT.CANCEL );
    close();
  }

  private void setReturnCode( int code ) {
    returnCode = code;
  }

  private boolean isMulti() {
    return ( getStyle() & SWT.MULTI ) != 0;
  }

  private void close() {
    shell.close();
  }

  private void cleanup() {
    pushSession.stop();
    singleThreadExecutor.shutdownNow();
    if( returnCode == SWT.CANCEL ) {
      deleteUploadedFiles( progressCollector.getCompletedFileNames() );
    }
  }

  void deleteUploadedFiles( String[] fileNames ) {
    for( String fileName : fileNames ) {
      File file = new File( fileName );
      if( file.exists() ) {
        file.delete();
      }
    }
  }

  static int checkStyle( Shell parent, int style ) {
    int result = style;
    int mask = SWT.PRIMARY_MODAL | SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL;
    if( ( result & SWT.SHEET ) != 0 ) {
      result &= ~SWT.SHEET;
      if( ( result & mask ) == 0 ) {
        result |= parent == null ? SWT.APPLICATION_MODAL : SWT.PRIMARY_MODAL;
      }
    }
    if( ( result & mask ) == 0 ) {
      result |= SWT.APPLICATION_MODAL;
    }
    if( ( result & ( SWT.LEFT_TO_RIGHT ) ) == 0 ) {
      if( parent != null ) {
        if( ( parent.getStyle() & SWT.LEFT_TO_RIGHT ) != 0 ) {
          result |= SWT.LEFT_TO_RIGHT;
        }
      }
    }
    result |= SWT.TITLE | SWT.BORDER;
    result &= ~SWT.MIN;
    return result;
  }

  String[] getCompletedFileNames() {
    return progressCollector.getCompletedFileNames();
  }

  ThreadPoolExecutor createSingleThreadExecutor() {
    return new SingleThreadExecutor();
  }

  private final class SingleThreadExecutor extends ThreadPoolExecutor {

    public SingleThreadExecutor() {
      super( 1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>() );
    }

    @Override
    protected void beforeExecute( Thread thread, Runnable runnable ) {
      setButtonEnabled( false );
    }

    @Override
    protected void afterExecute( Runnable runnable, Throwable throwable ) {
      if( getQueue().size() == 0 ) {
        setButtonEnabled( true );
      }
    }

  }

}
