/*******************************************************************************
 * Copyright (c) 2010, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Zahn (ARS) - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.widgets;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import org.eclipse.rap.rwt.internal.RWTMessages;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

/**
 * Instances of this class allow the user to select a font from all available
 * fonts in the system.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em> within the
 * SWT implementation.
 * </p>
 *
 * @since 1.3
 */
public class FontDialog extends Dialog {

  private static final int BUTTON_WIDTH = 60;

  private FontData fontData;
  private RGB rgb;
  private Text txtFontFamily;
  private List lstFontFamily;
  private Spinner spFontSize;
  private Button cbBold;
  private Button cbItalic;
  private Label lblColor;
  private Label lblPreview;

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
  public FontDialog( Shell parent ) {
    this( parent, SWT.APPLICATION_MODAL );
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
  public FontDialog( Shell parent, int style ) {
    super( parent, checkStyle( parent, style ) );
    checkSubclass();
    setText( RWTMessages.getMessage( "RWT_FontDialogTitle" ) );
  }

  /**
   * Returns a FontData set describing the font that was selected in the dialog,
   * or null if none is available.
   *
   * @return the FontData for the selected font, or null
   */
  public FontData[] getFontList() {
    FontData[] result = null;
    if( fontData != null ) {
      result = new FontData[ 1 ];
      result[ 0 ] = fontData;
    }
    return result;
  }

  /**
   * Sets the set of FontData objects describing the font to be selected by
   * default in the dialog, or null to let the platform choose one.
   *
   * @param fontData the set of FontData objects to use initially, or null to
   *          let the platform select a default when open() is called
   * @see Font#getFontData
   */
  public void setFontList( FontData[] fontData ) {
    if( fontData != null && fontData.length > 0 ) {
      this.fontData = fontData[ 0 ];
    } else {
      this.fontData = null;
    }
  }

  /**
   * Returns an RGB describing the color that was selected in the dialog, or
   * null if none is available.
   *
   * @return the RGB value for the selected color, or null
   * @see PaletteData#getRGBs
   */
  public RGB getRGB() {
    return rgb;
  }

  /**
   * Sets the RGB describing the color to be selected by default in the dialog,
   * or null to let the platform choose one.
   *
   * @param rgb the RGB value to use initially, or null to let the platform
   *          select a default when open() is called
   * @see PaletteData#getRGBs
   */
  public void setRGB( RGB rgb ) {
    this.rgb = rgb;
  }

  /**
   * Makes the dialog visible and brings it to the front of the display.
   *
   * <!-- Begin RAP specific -->
   * <p><strong>RAP Note:</strong> This method is not supported when running the application in
   * JEE_COMPATIBILITY mode. Use <code>Dialog#open(DialogCallback)</code> instead.</p>
   * <!-- End RAP specific -->
   *
   * @return a FontData object describing the font that was selected, or null if
   *         the dialog was cancelled or an error occurred
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the dialog has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the dialog</li>
   *              </ul>
   * @exception UnsupportedOperationException when running the application in JEE_COMPATIBILITY mode
   *
   * @see org.eclipse.rap.rwt.application.Application.OperationMode
   * @see #open(DialogCallback)
   */
  public FontData open() {
    checkOperationMode();
    prepareOpen();
    runEventLoop( shell );
    return fontData;
  }

  @Override
  protected void prepareOpen() {
    initializeDefaults();
    createShell();
    createControls();
    updateControls();
    addChangeListeners();
    layoutAndCenterShell();
  }

  private void initializeDefaults() {
    if( fontData == null ) {
      FontData systemFontData = getDisplay().getSystemFont().getFontData()[ 0 ];
      String fontName = getFirstFontName( systemFontData.getName() );
      int fontHeight = systemFontData.getHeight();
      int fontStyle = systemFontData.getStyle();
      fontData = new FontData( fontName, fontHeight, fontStyle );
    }
    if( rgb == null ) {
      rgb = new RGB( 0, 0, 0 );
    }
  }

  static String getFirstFontName( String fontName ) {
    String result = fontName;
    int index = result.indexOf( ',' );
    if( index != -1 ) {
      result = result.substring( 0, index );
    }
    result = result.trim();
    if( result.length() > 2 ) {
      char firstChar = result.charAt( 0 );
      char lastChar = result.charAt( result.length() - 1 );
      boolean isQuoted =  ( firstChar == '\'' && lastChar == '\'' )
                       || ( firstChar == '"' && lastChar == '"' );
      if( isQuoted ) {
        result = result.substring( 1, result.length() - 1 );
      }
    }
    return result;
  }

  private void createShell() {
    shell = new Shell( parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL );
    shell.setText( getText() );
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent event ) {
        handleShellClose();
      }
    } );
  }

  private void layoutAndCenterShell() {
    Point prefSize = shell.computeSize( SWT.DEFAULT, SWT.DEFAULT );
    // leave some space in preview area for larger fonts
    prefSize.y += 50;
    shell.setSize( prefSize );
    Rectangle displaySize = parent.getDisplay().getBounds();
    int locationX = ( displaySize.width - prefSize.x ) / 2 + displaySize.x;
    int locationY = ( displaySize.height - prefSize.y ) / 2 + displaySize.y;
    shell.setLocation( locationX, locationY );
  }

  private void createControls() {
    GridLayout mainLayout = new GridLayout( 2, true );
    mainLayout.marginWidth = 10;
    mainLayout.marginHeight = 10;
    mainLayout.horizontalSpacing = 10;
    mainLayout.verticalSpacing = 10;
    shell.setLayout( mainLayout );
    createLeftArea( shell );
    createRightArea( shell );
    createPreviewArea( shell );
    createButtonArea( shell );
    fillAvailableFonts();
  }

  private void createLeftArea( Composite parent ) {
    Composite leftArea = createVerticalArea( parent );
    createFontFamilyGroup( leftArea );
  }

  private void createRightArea( Composite parent ) {
    Composite rightArea = createVerticalArea( parent );
    createFontSizeGroup( rightArea );
    createFontStyleGroup( rightArea );
    createFontColorGroup( rightArea );
  }

  private static Composite createVerticalArea( Composite parent ) {
    Composite result = new Composite( parent, SWT.NONE );
    result.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    result.setLayout( layout );
    return result;
  }

  private void createFontFamilyGroup( Composite parent ) {
    Group result = new Group( parent, SWT.NONE );
    result.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    result.setText( RWTMessages.getMessage( "RWT_FontDialogFontFamilyTitle" ) );
    result.setLayout( new GridLayout() );
    txtFontFamily = new Text( result, SWT.BORDER );
    GridData textData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    txtFontFamily.setLayoutData( textData );
    lstFontFamily = new List( result, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER );
    GridData listData = new GridData( SWT.FILL, SWT.FILL, true, true );
    lstFontFamily.setLayoutData( listData );
    lstFontFamily.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        int selectionIndex = lstFontFamily.getSelectionIndex();
        if( selectionIndex != -1 ) {
          txtFontFamily.setText( lstFontFamily.getItem( selectionIndex ) );
        }
      }
    } );
  }

  private void createFontSizeGroup( Composite parent ) {
    Group result = new Group( parent, SWT.NONE );
    result.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    result.setText( RWTMessages.getMessage( "RWT_FontDialogFontSizeTitle" ) );
    result.setLayout( new GridLayout() );
    spFontSize = new Spinner( result, SWT.BORDER );
    spFontSize.setDigits( 0 );
    spFontSize.setMinimum( 0 );
    spFontSize.setMaximum( 200 );
    GridData spinnerData = new GridData( SWT.FILL, SWT.FILL, true, true );
    spFontSize.setLayoutData( spinnerData );
  }

  private void createFontStyleGroup( Composite parent ) {
    Display display = getDisplay();
    Group result = new Group( parent, SWT.NONE );
    result.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    result.setText( RWTMessages.getMessage( "RWT_FontDialogFontStyleTitle" ) );
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    result.setLayout( layout );
    cbBold = new Button( result, SWT.CHECK );
    cbBold.setText( RWTMessages.getMessage( "RWT_FontDialogFontStyleBold" ) );
    FontData normalFont = cbBold.getFont().getFontData()[ 0 ];
    Font boldFont = new Font( display, normalFont.getName(), normalFont.getHeight(), SWT.BOLD );
    cbBold.setFont( boldFont );
    cbItalic = new Button( result, SWT.CHECK );
    cbItalic.setText( RWTMessages.getMessage( "RWT_FontDialogFontStyleItalic" ) );
    Font italicFont = new Font( display, normalFont.getName(), normalFont.getHeight(), SWT.ITALIC );
    cbItalic.setFont( italicFont );
  }

  private void createFontColorGroup( Composite parent ) {
    Group result = new Group( parent, SWT.NONE );
    result.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    result.setText( RWTMessages.getMessage( "RWT_FontDialogFontColorTitle" ) );
    result.setLayout( new GridLayout( 2, false ) );
    lblColor = new Label( result, SWT.BORDER );
    lblColor.setLayoutData( new GridData( 20, 20 ) );
    Button changeColorButton = new Button( result, SWT.PUSH );
    changeColorButton.setText( RWTMessages.getMessage( "RWT_FontDialogFontColorSelect" ) );
    changeColorButton.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        openColorDialog();
      }
    } );
  }

  private void openColorDialog() {
    final ColorDialog dialog = new ColorDialog( shell );
    dialog.setRGB( rgb );
    dialog.open( new DialogCallback() {
      @Override
      public void dialogClosed( int returnCode ) {
        RGB selected = dialog.getRGB();
        if( selected != null ) {
          rgb = selected;
          updateControls();
        }
      }
    } );
  }

  private void addChangeListeners() {
    SelectionListener selectionListener = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        updateFontData();
      }
    };
    spFontSize.addSelectionListener( selectionListener );
    cbBold.addSelectionListener( selectionListener );
    cbItalic.addSelectionListener( selectionListener );
    txtFontFamily.addModifyListener( new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent event ) {
        String text = txtFontFamily.getText();
        selectFontFamilyInList( text );
        updateFontData();
      }
    } );
  }

  private void createPreviewArea( Composite parent ) {
    Composite previewArea = new Composite( parent, SWT.BORDER );
    GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 );
    gridData.minimumWidth = 300;
    previewArea.setLayoutData( gridData );
    previewArea.setLayout( new GridLayout() );
    lblPreview = new Label( previewArea, SWT.CENTER );
    GridData labelData = new GridData( SWT.FILL, SWT.CENTER, true, true );
    lblPreview.setLayoutData( labelData );
    lblPreview.setText( RWTMessages.getMessage( "RWT_FontDialogPreviewText" ) );
    Color bgColor = getDisplay().getSystemColor( SWT.COLOR_LIST_BACKGROUND );
    previewArea.setBackground( bgColor );
    previewArea.setBackgroundMode( SWT.INHERIT_DEFAULT );
  }

  private void createButtonArea( Composite parent ) {
    Composite composite = new Composite( parent, SWT.NONE );
    GridData layoutData = new GridData( SWT.RIGHT, SWT.CENTER, false, false, 2, 1 );
    composite.setLayoutData( layoutData );
    GridLayout layout = new GridLayout( 2, true );
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    composite.setLayout( layout );
    Button okButton = createButton( composite, SWT.getMessage( "SWT_OK" ), SWT.OK );
    okButton.getShell().setDefaultButton( okButton );
    okButton.forceFocus();
    createButton( composite, SWT.getMessage( "SWT_Cancel" ), SWT.CANCEL );
  }

  private Button createButton( Composite parent, String text, final int returnCode ) {
    Button result = new Button( parent, SWT.PUSH );
    result.setText( text );
    GridData data = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
    int widthHint = convertHorizontalDLUsToPixels( result, BUTTON_WIDTH );
    Point minSize = result.computeSize( SWT.DEFAULT, SWT.DEFAULT );
    data.widthHint = Math.max( widthHint, minSize.x );
    result.setLayoutData( data );
    result.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        FontDialog.this.returnCode = returnCode;
        shell.close();
      }
    } );
    return result;
  }

  private void handleShellClose() {
    if( returnCode != SWT.OK ) {
      fontData = null;
      rgb = null;
    }
  }

  private void fillAvailableFonts() {
    Collection<String> fontFamilies = new HashSet<>();
    FontData[] fontList = getDisplay().getFontList( null, true );
    if( fontList != null ) {
      for( int i = 0; i < fontList.length; i++ ) {
        fontFamilies.add( fontList[ i ].getName() );
      }
    }
    String[] availableFontNames = fontFamilies.toArray( new String[ fontFamilies.size() ] );
    Arrays.sort( availableFontNames );
    lstFontFamily.setItems( availableFontNames );
  }

  private void updateControls() {
    String fontName = fontData.getName();
    if( !txtFontFamily.getText().equals( fontName ) ) {
      txtFontFamily.setText( fontName );
    }
    selectFontFamilyInList( fontName );
    spFontSize.setSelection( fontData.getHeight() );
    cbBold.setSelection( ( fontData.getStyle() & SWT.BOLD ) != 0 );
    cbItalic.setSelection( ( fontData.getStyle() & SWT.ITALIC ) != 0 );
    updatePreview();
  }

  private void selectFontFamilyInList( String fontFamily ) {
    lstFontFamily.deselectAll();
    String[] items = lstFontFamily.getItems();
    for( int i = 0; i < items.length; i++ ) {
      String item = items[ i ].toLowerCase( Locale.ENGLISH );
      if( fontFamily.toLowerCase( Locale.ENGLISH ).equals( item ) ) {
        lstFontFamily.select( i );
      }
    }
  }

  private void updatePreview() {
    if( lblPreview != null ) {
      Display display = getDisplay();
      Font font = new Font( display, fontData );
      lblPreview.setFont( font );
      Color color = new Color( display, rgb );
      lblPreview.setForeground( color );
      lblColor.setBackground( color );
      lblPreview.getParent().layout( true );
    }
  }

  private void updateFontData() {
    String name = txtFontFamily.getText();
    int height = spFontSize.getSelection();
    int style = SWT.NORMAL;
    if( cbBold.getSelection() ) {
      style |= SWT.BOLD;
    }
    if( cbItalic.getSelection() ) {
      style |= SWT.ITALIC;
    }
    fontData = new FontData( name, height, style );
    updateControls();
  }

  private Display getDisplay() {
    return parent.getDisplay();
  }

}
