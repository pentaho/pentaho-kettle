/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.core.vfs.connections.ui.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.CheckBoxVar;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.PasswordVisibleTextVar;
import org.pentaho.di.ui.core.widget.TextVar;

import java.util.Set;

/**
 * This class provides common layout logic to all VFSDetailComposites.  You can still roll your own layout but these
 * methods will greatly simplify the process, if you keep within the mold.
 */
public class VFSDetailsCompositeHelper {
  private PropsUI props;
  private int margin;
  private int maxLabelWidth = 0;
  private final Class<?> pkg;

  public VFSDetailsCompositeHelper() throws IllegalAccessException {
    throw new IllegalAccessException( "Must use the messageClass, props constructor" );
  }

  public VFSDetailsCompositeHelper( Class<?> messageClass, PropsUI props ) {
    this.props = props;
    margin = Const.MARGIN;
    pkg = messageClass;
  }

  public PropsUI getProps() {
    return props;
  }

  public int getMaxLabelWidth() {
    return maxLabelWidth;
  }

  public int getMargin() {
    return margin;
  }

  /**
   * Creates a label attached to left side of the parent composite and below topWidget.
   *
   * @param composite The composite to draw the control on
   * @param flags     The SWT flags
   * @param key       the key to the message to use on the label
   * @param topWidget The Widget immediately above this control.  It will be used to set top in the layout. If null, the
   *                  widget will be placed at margin*2 below the top of the composite.
   * @return
   */
  public Label createLabel( Composite composite, int flags, String key, Control topWidget ) {
    Label label = new Label( composite, flags );
    getProps().setLook( label );
    label.setText( BaseMessages.getString( pkg, key ) );
    maxLabelWidth = Math.max( maxLabelWidth, label.computeSize( SWT.DEFAULT, SWT.DEFAULT ).x );
    label.setLayoutData( getFormDataLabel( topWidget ) );
    return label;
  }

  /**
   * Creates a Text control to the left side of the parent composite and below the topWidget specified.
   *
   * @param composite T
   * @param flags     The SWT flags
   * @param topWidget The Widget immediately above this control.  It will be used to set top in the layout.
   * @param width     Controls the right edge of the control.  If 0 the control will fill the the composite minus the
   *                  margin.  If non-zero the control will be width pixels wide.
   * @return
   */
  public Text createText( Composite composite, int flags, Control topWidget, int width ) {
    Text text = new Text( composite, flags );
    getProps().setLook( text );
    text.setLayoutData( getFormDataField( topWidget, width ) );
    return text;
  }

  /**
   * Creates a TextVar control to the left side of the parent composite and below the topWidget specified.
   *
   * @param variableSpace The variableSpace to be used when specifying varaibles
   * @param composite     T
   * @param flags         The SWT flags
   * @param topWidget     The Widget immediately above this control.  It will be used to set top in the layout.
   * @param width         Controls the right edge of the control.  If 0 the control will fill the the composite minus
   *                      the margin.  If non-zero the control will be width pixels wide.
   * @return
   */
  public TextVar createTextVar( VariableSpace variableSpace, Composite composite, int flags, Control topWidget,
                                int width ) {
    TextVar textVar = new TextVar( variableSpace, composite, flags );
    getProps().setLook( textVar );
    textVar.setLayoutData( getFormDataField( topWidget, width ) );
    return textVar;
  }

  /**
   * Creates a CCombo control to the left side of the parent composite and below the topWidget specified.
   *
   * @param composite The parent composite
   * @param flags     The SWT flags
   * @param topWidget The Widget immediately above this control.  It will be used to set top in the layout.
   * @param width     Controls the right edge of the control.  If 0 the control will fill the the composite minus the
   *                  margin.  If non-zero the control will be width pixels wide.
   * @return
   */
  public CCombo createCCombo( Composite composite, int flags, Control topWidget, int width ) {
    CCombo cCombo = new CCombo( composite, flags );
    getProps().setLook( cCombo );
    cCombo.setLayoutData( getFormDataField( topWidget, width ) );
    return cCombo;
  }

  public ComboVar createComboVar( VariableSpace variableSpace, Composite composite, int flags, Control topWidget,
                                  int width ) {
    ComboVar comboVar = new ComboVar( variableSpace, composite, flags );
    getProps().setLook( comboVar );
    comboVar.setLayoutData( getFormDataField( topWidget, width ) );
    return comboVar;
  }

  public PasswordTextVar createPasswordTextVar( VariableSpace variableSpace, Composite composite, int flags,
                                                Control topWidget, int width ) {
    PasswordTextVar passwordTextVar = new PasswordTextVar( variableSpace, composite, flags );
    getProps().setLook( passwordTextVar );
    passwordTextVar.setLayoutData( getFormDataField( topWidget, width ) );
    return passwordTextVar;
  }

  public PasswordVisibleTextVar createPasswordVisibleTextVar( VariableSpace variableSpace, Composite composite,
                                                              int flags,
                                                              Control topWidget, int width ) {
    PasswordVisibleTextVar PasswordVisibleTextVar = new PasswordVisibleTextVar( variableSpace, composite, flags );
    getProps().setLook( PasswordVisibleTextVar );
    PasswordVisibleTextVar.setLayoutData( getFormDataField( topWidget, width ) );
    return PasswordVisibleTextVar;
  }

  public CheckBoxVar createCheckboxVar( VariableSpace variableSpace, Composite composite, Control topWidget ) {
    CheckBoxVar checkBoxVar = new CheckBoxVar( variableSpace, composite, SWT.CHECK );
    getProps().setLook( checkBoxVar );
    checkBoxVar.setLayoutData( getFormDataField( topWidget ) );
    return checkBoxVar;
  }

  /**
   * This method places a centered title at the top of the composite recieved
   *
   * @param composite    The composite holding the details
   * @param key          The key to the message file
   * @param skipControls Controls that should not be lined up along a vertical column
   * @return
   */
  public Label createTitleLabel( Composite composite, String key, Set<Control> skipControls ) {
    Label wlTitle = new Label( composite, SWT.CENTER );
    FontData[] fD = wlTitle.getFont().getFontData();
    fD[ 0 ].setHeight( 14 ); //Make the title bigger
    wlTitle.setFont( new Font( wlTitle.getDisplay(), fD[ 0 ] ) );
    props.setLook( wlTitle );
    wlTitle.setText( BaseMessages.getString( pkg, key ) );
    FormData formData = new FormData();
    formData.top = new FormAttachment( 0, 0 ); // First Item
    formData.left = new FormAttachment( 0, 0 );
    formData.right = new FormAttachment( 100, 0 );
    wlTitle.setLayoutData( formData );
    skipControls.add( wlTitle );  //We don't want this to be lined up with the entries
    return wlTitle;
  }

  public FormData getFormDataLabel( Control topWidget ) {
    FormData formData = new FormData();
    if ( topWidget == null ) {
      formData.top = new FormAttachment( 0, margin * 2 ); // First Item
    } else {
      formData.top = new FormAttachment( topWidget, margin * 2 ); // Following Items
    }
    formData.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    return formData;
  }

  public FormData getFormDataField( Control topWidget ) {
    return getFormDataField( topWidget, 0 );
  }

  public FormData getFormDataField( Control topWidget, int width ) {
    FormData formData = new FormData();
    if ( topWidget == null ) {
      formData.top = new FormAttachment( 0, margin * 2 );  //First Item
    } else {
      formData.top = new FormAttachment( topWidget, margin * 2 ); //Following Items
    }
    if ( width == 0 ) {
      formData.right = new FormAttachment( 100, -margin - 9 ); //Fill but make sure scrollbar won't overlap hence subtracting 9 pixels
    }
    formData.left = new FormAttachment( 0, 0 );
    return formData;
  }

  /**
   * Sets the Size of the Detail Composite to the track the Parent ScrolledComposites size.
   *
   * @param wComposite The DetailComposite
   */
  public static void setupCompositeResizeListener( Composite wComposite ) {
    wComposite.getParent().addListener( SWT.Resize, arg0 -> {
      updateScrollableRegion( wComposite );
    } );
  }

  public static void updateScrollableRegion( Composite wComposite ) {
    Rectangle r = wComposite.getParent().getClientArea();
    if ( r.width != 0 ) {
      wComposite.setSize( r.width, SWT.DEFAULT );
    }
  }

}

