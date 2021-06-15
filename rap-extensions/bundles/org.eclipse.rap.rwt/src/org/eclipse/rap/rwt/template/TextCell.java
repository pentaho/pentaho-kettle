/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.template;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.util.ParamCheck;


/**
 * Defines a region in a template that displays a text.
 *
 * @since 2.2
 */
public class TextCell extends Cell<TextCell>  {

  private static final String TYPE_TEXT = "text";
  private static final String PROPERTY_TEXT = "text";
  private static final String PROPERTY_WRAP = "wrap";
  private String text;
  private boolean wrap;

  /**
   * Constructs a new text cell and adds it to the given template.
   */
  public TextCell( Template template ) {
    super( template, TYPE_TEXT );
  }

  /**
   * Sets the text to be displayed in the cell if the <em>bindingIndex</em> is not set.
   * Can be used to display a static text.
   *
   * @param text the text to display, must not be <code>null</code>
   * @return the cell itself, to enable method chaining
   */
  public TextCell setText( String text ) {
    ParamCheck.notNull( text, "text" );
    this.text = text;
    return this;
  }

  String getText() {
    return text;
  }

  /**
   * Enables automatic line wrapping. The default is <code>false</code>.
   *
   * @param wrap <code>true</code> to enable automatic line wrapping
   * @return the cell itself, to enable method chaining
   */
  public TextCell setWrap( boolean wrap ) {
    this.wrap = wrap;
    return this;
  }

  boolean isWrap() {
    return wrap;
  }

  @Override
  protected JsonObject toJson() {
    JsonObject json = super.toJson();
    if( text != null ) {
      json.add( PROPERTY_TEXT, text );
    }
    if( wrap ) {
      json.add( PROPERTY_WRAP, wrap );
    }
    return json;
  }

}
