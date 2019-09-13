/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.pentaho.di.core.Const.FORM_MARGIN;
import static org.pentaho.di.core.Const.MARGIN;
import static org.pentaho.di.ui.core.ConstUI.MEDUIM_MARGIN;

/**
 * Builder class to simplify creation and layout of SWT controls.
 * <p>
 * To create a radio button inside of a composite and below a label, for example:
 * <p>
 * <p>
 * Button radioBtn = new WidgetBuilder<Button>()
 * .container( compositeWidgetContainingTheButton )
 * .text( textToDisplay )
 * .style( SWT.RADIO )
 * .props( propsUI )
 * .below( labelWidget )
 * .swtControlFactory( Button::new )
 * .build();
 * <p>
 * The .swtControlFactory( Button::new ) tells the builder how to initialize the swt control.
 * <p>
 * For PDI specific control implementations, use .pdiVarControlFactory.  For example:
 * <p>
 * ComboVar combo = new WidgetBuilder<ComboVar>()
 * .container( parentComp )
 * .style( SWT.SINGLE | SWT.LEFT | SWT.BORDER )
 * .space( variableSpace )
 * .props( propsUI )
 * .rightOf( labelWidget )
 * .below( radioBtn )
 * .pdiVarControlFactory( ComboVar::new )
 * .build();
 *
 * The WidgetBuilder.Templates subclass provides convenience methods for common widget structures.  For example,
 * 2 lines create a text box inside of a group, and below a label:
 *
 *   Group group = template.group(shell, controlAbove, "title").build()
 *   Combo text = template.text(group, "initial text", template.label(group, "label text", null).build()).build()
 */
@SuppressWarnings ( { "unused", "WeakerAccess" } )
public class WidgetBuilder<T extends Control> {

  private String text;

  private Composite container;
  private int style;

  private SWTControlFactory<T> swtControlFactory;
  private PDIVarControlFactory<T> pdiVarControlFactory;

  private FormDataBuilder formDataBuilder = new FormDataBuilder();
  private PropsUI props;
  private String[] items;
  private VariableSpace space;
  private int marginHeight = FORM_MARGIN;
  private int marginWidth = FORM_MARGIN;
  private Image image;


  public WidgetBuilder<T> below( Control controlAbove, int margin ) {
    formDataBuilder.top( controlAbove, margin ).left( new FormAttachment( controlAbove, 0, SWT.LEFT ) );
    return this;
  }

  public WidgetBuilder<T> rightOf( Control controlToLeft, int margin ) {
    formDataBuilder.left( controlToLeft, margin ).top( new FormAttachment( controlToLeft, 0, SWT.TOP ) );
    return this;
  }

  public WidgetBuilder<T> container( Composite container ) {
    this.container = container;
    return this;
  }

  public WidgetBuilder<T> left( int percentageOfParent, int marginPixels ) {
    formDataBuilder.left( percentageOfParent, marginPixels );
    return this;
  }

  public WidgetBuilder<T> right( int percentageOfParent, int marginPixels ) {
    formDataBuilder.right( percentageOfParent, marginPixels );
    return this;
  }

  public WidgetBuilder<T> bottom( int percentageOfParent, int marginPixels ) {
    formDataBuilder.bottom( percentageOfParent, marginPixels );
    return this;
  }

  public WidgetBuilder<T> top( int percentageOfParent, int marginPixels ) {
    formDataBuilder.top( percentageOfParent, marginPixels );
    return this;
  }

  public WidgetBuilder<T> height( int height ) {
    formDataBuilder.height( height );
    return this;
  }

  public WidgetBuilder<T> width( int width ) {
    formDataBuilder.width( width );
    return this;
  }

  public WidgetBuilder<T> margin( int width, int height ) {
    this.marginHeight = height;
    this.marginWidth = width;
    return this;
  }


  public WidgetBuilder<T> style( int style ) {
    this.style = style;
    return this;
  }

  public WidgetBuilder<T> text( String text ) {
    this.text = text;
    return this;
  }

  public WidgetBuilder<T> image( Image image ) {
    this.image = image;
    return this;
  }


  public WidgetBuilder<T> items( String... items ) {
    this.items = items;
    return this;
  }

  public WidgetBuilder<T> props( PropsUI props ) {
    this.props = props;
    return this;
  }

  public WidgetBuilder<T> space( VariableSpace space ) {
    this.space = space;
    return this;
  }

  /**
   * @param swtControlFactory, typically the constructor for the desired swt control.  E.g. Button::new
   *    swtControlFactory OR pdiVarControlFactory must be set, but not both.
   */
  public WidgetBuilder<T> swtControlFactory( SWTControlFactory<T> swtControlFactory ) {
    this.swtControlFactory = swtControlFactory;
    return this;
  }

  /**
   * @param pdiVarControlFactory, typically the construtor for the desired pdi var control.  E.g. ComboVar::new
   *    swtControlFactory OR pdiVarControlFactory must be set, but not both.
   */
  public WidgetBuilder<T> pdiVarControlFactory( PDIVarControlFactory<T> pdiVarControlFactory ) {
    this.pdiVarControlFactory = pdiVarControlFactory;
    return this;
  }

  public T build() {
    if ( swtControlFactory == null && pdiVarControlFactory == null ) {
      throw new IllegalStateException( "swtControlFactory or pdiVarControlFactory must be set." );
    }
    T control;
    if ( swtControlFactory != null ) {
      control = swtControlFactory.init( container, style );
    } else {
      control = pdiVarControlFactory.init( space, container, style );
    }
    typeSpecificHandling( control );
    control.setLayoutData( formDataBuilder.result() );
    props.setLook( control );
    return control;
  }

  private void typeSpecificHandling( T control ) {
    if ( text != null ) {
      invoke( control, "setText", text );
    }
    if ( image != null ) {
      invoke( control, "setImage", image );
    }
    if ( items != null ) {
      invoke( control, "setItems", items );
    }
    if ( control instanceof Composite ) {
      FormLayout layout = new FormLayout();
      layout.marginHeight = marginHeight;
      layout.marginWidth = marginWidth;
      ( (Composite) control ).setLayout( layout );
    }
  }

  private void invoke( T control, String methodName, Object val ) {
    try {
      Method method = control.getClass().getMethod( methodName, val.getClass() );
      method.invoke( control, val );
    } catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException e ) {
      throw new IllegalStateException( e );
    }
  }

  @FunctionalInterface
  public interface SWTControlFactory<C> {
    C init( Composite container, int style );
  }

  @FunctionalInterface
  public interface PDIVarControlFactory<C> {
    C init( VariableSpace space, Composite container, int style );
  }

  public static class Templates {
    private final VariableSpace space;
    private final PropsUI props;

    public Templates( PropsUI props, VariableSpace space ) {
      this.props = props;
      this.space = space;
    }

    public WidgetBuilder<Label> label( Composite composite, String text, Control above ) {
      return new WidgetBuilder<Label>()
        .container( composite )
        .text( text )
        .style( SWT.LEFT )
        .props( props )
        .below( above, MARGIN )
        .swtControlFactory( Label::new );
    }

    public WidgetBuilder<Label> separator( Composite composite, Control above ) {
      return new WidgetBuilder<Label>()
        .container( composite )
        .style( SWT.HORIZONTAL | SWT.SEPARATOR )
        .props( props )
        .below( above, 15 )
        .left( 0, 0 )
        .right( 100, 0 )
        .swtControlFactory( Label::new );
    }

    public WidgetBuilder<Label> image( Composite container, Image image ) {
      return new WidgetBuilder<Label>()
        .container( container )
        .image( image )
        .style( SWT.RIGHT )
        .props( props )
        .right( 100, -MARGIN )
        .swtControlFactory( Label::new );
    }

    public WidgetBuilder<TextVar> textvar( Composite composite, String text, Control above ) {
      return new WidgetBuilder<TextVar>()
        .container( composite )
        .text( text )
        .style( SWT.SINGLE | SWT.LEFT | SWT.BORDER )
        .props( props )
        .below( above, MARGIN )
        .pdiVarControlFactory( TextVar::new )
        .left( 0, MARGIN )
        .right( 0, 250 )
        .space( space );
    }

    public WidgetBuilder<Text> text( Composite composite, String text, Control above ) {
      return new WidgetBuilder<Text>()
        .container( composite )
        .text( text )
        .style( SWT.SINGLE | SWT.LEFT | SWT.BORDER )
        .props( props )
        .below( above, MARGIN )
        .swtControlFactory( Text::new )
        .left( 0, MARGIN )
        .right( 0, 250 );
    }

    public WidgetBuilder<ComboVar> comboVar( Composite composite, Control above, String... items ) {
      return new WidgetBuilder<ComboVar>()
        .container( composite )
        .items( items )
        .style( SWT.SINGLE | SWT.LEFT | SWT.BORDER )
        .props( props )
        .space( space )
        .below( above, MARGIN )
        .pdiVarControlFactory( ComboVar::new );
    }

    public WidgetBuilder<ComboVar> comboVar( Composite composite, Control left, Control above, String... items ) {
      return new WidgetBuilder<ComboVar>()
        .container( composite )
        .items( items )
        .style( SWT.SINGLE | SWT.LEFT | SWT.BORDER )
        .props( props )
        .space( space )
        .below( above, MARGIN )
        .rightOf( left, MARGIN )
        .pdiVarControlFactory( ComboVar::new );
    }

    public WidgetBuilder<CCombo> combo( Composite composite, Control above, String... items ) {
      return new WidgetBuilder<CCombo>()
        .container( composite )
        .items( items )
        .style( SWT.SINGLE | SWT.LEFT | SWT.BORDER )
        .props( props )
        .space( space )
        .below( above, MARGIN )
        .swtControlFactory( CCombo::new );
    }

    public WidgetBuilder<Button> button( Composite composite, String text, Control above, int style ) {
      return new WidgetBuilder<Button>()
        .container( composite )
        .text( text )
        .style( style )
        .props( props )
        .below( above, MEDUIM_MARGIN )
        .swtControlFactory( Button::new );
    }

    public WidgetBuilder<Group> group( Composite composite, Control above, String text ) {
      return new WidgetBuilder<Group>()
        .container( composite )
        .props( props )
        .margin( MARGIN, MARGIN )
        .style( SWT.BORDER_SOLID )
        .left( 0, 0 )
        .right( 100, -MARGIN )
        .text( text )
        .below( above, 15 )
        .swtControlFactory( Group::new );
    }
  }
}
