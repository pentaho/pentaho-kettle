package org.pentaho.di.ui.core.widget;

import org.pentaho.di.ui.core.PropsUI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TypedListener;

/**
 * A flat button. As seen in the File Open/Save Dialog
 */
public class FlatButton {

  private CLabel label;

  private boolean enabled;

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
        if ( enabled ) {
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
    return enabled;
  }

  public FlatButton setEnabled( boolean enabled ) {

    if ( disabledImage != null && enabledImage != null ) {
      label.setImage( enabled ? enabledImage : disabledImage );
    } else if ( enabledImage != null ) {
      label.setImage( enabledImage );
    } else if ( disabledImage != null ) {
      label.setImage( disabledImage );
    }
    label.redraw();

    this.enabled = enabled;
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

