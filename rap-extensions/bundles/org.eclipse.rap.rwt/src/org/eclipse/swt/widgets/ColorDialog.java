/*******************************************************************************
 * Copyright (c) 2010, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.widgets;

import org.eclipse.rap.rwt.internal.RWTMessages;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;


/**
 * Instances of this class allow the user to select a color from a predefined
 * set of available colors.
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
 * @since 1.2
 */
public class ColorDialog extends Dialog {

  private class PaletteListener extends MouseAdapter {
    private RGB rgb;

    public PaletteListener( RGB rgb ) {
      this.rgb = rgb;
    }

    @Override
    public void mouseDown( MouseEvent event ) {
      setColorFromPalette( rgb );
    }
  }

  private class SpinnerListener implements ModifyListener {
    private final Spinner spinner;
    private final int colorIndex;

    public SpinnerListener( Spinner spinner, int colorIndex ) {
      this.spinner = spinner;
      this.colorIndex = colorIndex;
    }

    @Override
    public void modifyText( ModifyEvent event ) {
      setColorFomSpinner( colorIndex, spinner.getSelection() );
    }
  }

  // Layout
  private static final int BUTTON_WIDTH = 60;
  private static final int PALETTE_BOX_SIZE = 12;
  private static final int PALETTE_BOXES_IN_ROW = 14;
  private static final int COLOR_DISPLAY_BOX_SIZE = 76;

  private static final int MAX_RGB_COMPONENT_VALUE = 255;

  // Color components
  private static final int RED = 0;
  private static final int GREEN = 1;
  private static final int BLUE = 2;

  // Palette colors
  private static final RGB[] PALETTE_COLORS = new RGB[] {
    new RGB( 0, 0, 0 ),
    new RGB( 70, 70, 70 ),
    new RGB( 120, 120, 120 ),
    new RGB( 153, 0, 48 ),
    new RGB( 237, 28, 36 ),
    new RGB( 255, 126, 0 ),
    new RGB( 255, 194, 14 ),
    new RGB( 255, 242, 0 ),
    new RGB( 168, 230, 29 ),
    new RGB( 34, 177, 76 ),
    new RGB( 0, 183, 239 ),
    new RGB( 77, 109, 243 ),
    new RGB( 47, 54, 153 ),
    new RGB( 111, 49, 152 ),
    new RGB( 255, 255, 255 ),
    new RGB( 220, 220, 220 ),
    new RGB( 180, 180, 180 ),
    new RGB( 156, 90, 60 ),
    new RGB( 255, 163, 177 ),
    new RGB( 229, 170, 122 ),
    new RGB( 245, 228, 156 ),
    new RGB( 255, 249, 189 ),
    new RGB( 211, 249, 188 ),
    new RGB( 157, 187, 97 ),
    new RGB( 153, 217, 234 ),
    new RGB( 112, 154, 209 ),
    new RGB( 84, 109, 142 ),
    new RGB( 181, 165, 213 )
  };

  private RGB rgb;
  private Label colorDisplay;
  private Spinner spRed;
  private Spinner spBlue;
  private Spinner spGreen;

  /**
   * Constructs a new instance of this class given only its parent.
   *
   * @param parent a composite control which will be the parent of the new
   *          instance
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public ColorDialog( Shell parent ) {
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
   * @param parent a composite control which will be the parent of the new
   *          instance (cannot be null)
   * @param style the style of control to construct
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed
   *              subclass</li>
   *              </ul>
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public ColorDialog( Shell parent, int style ) {
    super( parent, checkStyle( parent, style ) );
    checkSubclass();
    setText( RWTMessages.getMessage( "RWT_ColorDialogTitle" ) );
  }

  /**
   * Makes the receiver visible and brings it to the front of the display.
   *
   * <!-- Begin RAP specific -->
   * <p><strong>RAP Note:</strong> This method is not supported when running the application in
   * JEE_COMPATIBILITY mode. Use <code>Dialog#open(DialogCallback)</code> instead.</p>
   * <!-- End RAP specific -->
   *
   * @return the selected color, or null if the dialog was cancelled, no color
   *         was selected, or an error occurred
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @exception UnsupportedOperationException when running the application in JEE_COMPATIBILITY mode
   *
   * @see org.eclipse.rap.rwt.application.Application.OperationMode
   * @see #open(DialogCallback)
   */
  public RGB open() {
    checkOperationMode();
    prepareOpen();
    runEventLoop( shell );
    return rgb;
  }

  /**
   * Returns the currently selected color in the receiver.
   *
   * @return the RGB value for the selected color, may be null
   * @see PaletteData#getRGBs
   */
  public RGB getRGB() {
    return rgb;
  }

  /**
   * Sets the receiver's selected color to be the argument.
   *
   * @param rgb the new RGB value for the selected color, may be null to let the
   *          platform select a default when open() is called
   * @see PaletteData#getRGBs
   */
  public void setRGB( RGB rgb ) {
    this.rgb = rgb;
  }

  @Override
  protected void prepareOpen() {
    createShell();
    createControls();
    if( rgb == null ) {
      rgb = new RGB( 255, 255, 255 );
    }
    updateColorDisplay();
    updateSpinners();
    configureShell();
  }

  private void createShell() {
    shell = new Shell( parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL );
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent event ) {
        if( returnCode == SWT.CANCEL ) {
          ColorDialog.this.rgb = null;
        }
      }
    } );
    shell.setLayout( new GridLayout( 1, false ) );
  }

  private void createControls() {
    createColorArea();
    createPalette();
    createButtons();
  }

  private void createPalette() {
    Composite paletteComp = new Composite( shell, SWT.NONE );
    GridData palData = new GridData( SWT.CENTER, SWT.CENTER, true, false );
    paletteComp.setLayoutData( palData );
    paletteComp.setLayout( new GridLayout( PALETTE_BOXES_IN_ROW, true ) );
    Label title = new Label( paletteComp, SWT.NONE );
    String titleText = RWTMessages.getMessage( "RWT_ColorDialogLabelBasicColors" );
    title.setText( titleText );
    GridData titleData = new GridData( SWT.LEFT, SWT.CENTER, true, false );
    titleData.horizontalSpan = PALETTE_BOXES_IN_ROW;
    title.setLayoutData( titleData );
    for( int i = 0; i < PALETTE_COLORS.length; i++ ) {
      createPaletteColorBox( paletteComp, PALETTE_COLORS[ i ] );
    }
  }

  private void createColorArea() {
    // Current color selection display
    Composite areaComp = new Composite( shell, SWT.NONE );
    GridData compData = new GridData( SWT.CENTER, SWT.CENTER, true, false );
    areaComp.setLayoutData( compData );
    areaComp.setLayout( new GridLayout( 2, true ) );
    colorDisplay = new Label( areaComp, SWT.BORDER | SWT.FLAT );
    GridData data = new GridData();
    data.widthHint = COLOR_DISPLAY_BOX_SIZE;
    data.heightHint = COLOR_DISPLAY_BOX_SIZE;
    colorDisplay.setLayoutData( data );
    // Color components spinners
    Composite spinComp = new Composite( areaComp, SWT.NONE );
    spinComp.setLayout( new GridLayout( 2, true ) );
    Label rLabel = new Label( spinComp, SWT.NONE );
    rLabel.setText( RWTMessages.getMessage( "RWT_ColorDialogLabelRed" ) );
    spRed = new Spinner( spinComp, SWT.BORDER );
    spRed.setMaximum( MAX_RGB_COMPONENT_VALUE );
    spRed.addModifyListener( new SpinnerListener( spRed, RED ) );
    //
    Label gLabel = new Label( spinComp, SWT.NONE );
    gLabel.setText( RWTMessages.getMessage( "RWT_ColorDialogLabelGreen" ) );
    spGreen = new Spinner( spinComp, SWT.BORDER );
    spGreen.setMaximum( MAX_RGB_COMPONENT_VALUE );
    spGreen.addModifyListener( new SpinnerListener( spGreen, GREEN ) );
    //
    Label bLabel = new Label( spinComp, SWT.NONE );
    bLabel.setText( RWTMessages.getMessage( "RWT_ColorDialogLabelBlue" ) );
    spBlue = new Spinner( spinComp, SWT.BORDER );
    spBlue.setMaximum( MAX_RGB_COMPONENT_VALUE );
    spBlue.addModifyListener( new SpinnerListener( spBlue, BLUE ) );
  }

  private void createButtons() {
    Composite composite = new Composite( shell, SWT.NONE );
    composite.setLayout( new GridLayout( 0, true ) );
    GridData gridData = new GridData( SWT.RIGHT, SWT.CENTER, true, false );
    composite.setLayoutData( gridData );
    Button okButton = createButton( composite, SWT.getMessage( "SWT_OK" ), SWT.OK );
    shell.setDefaultButton( okButton );
    createButton( composite, SWT.getMessage( "SWT_Cancel" ), SWT.CANCEL );
    okButton.forceFocus();
  }

  private void configureShell() {
    shell.setText( title );
    Rectangle displaySize = parent.getDisplay().getBounds();
    Point prefSize = shell.computeSize( SWT.DEFAULT, SWT.DEFAULT );
    shell.setSize( prefSize );
    int locationX = ( displaySize.width - prefSize.x ) / 2 + displaySize.x;
    int locationY = ( displaySize.height - prefSize.y ) / 2 + displaySize.y;
    shell.setLocation( new Point( locationX, locationY ) );
    shell.pack();
  }

  private void updateColorDisplay() {
    colorDisplay.setBackground( new Color( getDisplay(), rgb ) );
  }

  private void updateSpinners() {
    spRed.setSelection( rgb.red );
    spGreen.setSelection( rgb.green );
    spBlue.setSelection( rgb.blue );
  }

  private Label createPaletteColorBox( Composite parent, RGB color ) {
    Label result = new Label( parent, SWT.BORDER | SWT.FLAT );
    result.setBackground( new Color( getDisplay(), color ) );
    GridData data = new GridData();
    data.widthHint = PALETTE_BOX_SIZE;
    data.heightHint = PALETTE_BOX_SIZE;
    result.setLayoutData( data );
    result.addMouseListener( new PaletteListener( color ) );
    return result;
  }

  private Button createButton( Composite parent, String text, final int buttonId ) {
    ( ( GridLayout )parent.getLayout() ).numColumns++;
    Button result = new Button( parent, SWT.PUSH );
    GridData data = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
    int widthHint = convertHorizontalDLUsToPixels( shell, BUTTON_WIDTH );
    Point minSize = result.computeSize( SWT.DEFAULT, SWT.DEFAULT, true );
    data.widthHint = Math.max( widthHint, minSize.x );
    result.setLayoutData( data );
    result.setText( text );
    result.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        ColorDialog.this.returnCode = buttonId;
        shell.close();
      }
    } );
    return result;
  }

  private void setColorFomSpinner( int colorIndex, int value ) {
    switch( colorIndex ) {
      case RED:
        rgb.red = value;
      break;
      case GREEN:
        rgb.green = value;
      break;
      case BLUE:
        rgb.blue = value;
      break;
    }
    updateColorDisplay();
  }

  private void setColorFromPalette( RGB selectedColor ) {
    rgb.blue = selectedColor.blue;
    rgb.green = selectedColor.green;
    rgb.red = selectedColor.red;
    updateColorDisplay();
    updateSpinners();
  }

  private Display getDisplay() {
    return parent.getDisplay();
  }

}
