/*******************************************************************************
 * Copyright (c) 2013, 2014 EclipseSource and others.
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
import org.eclipse.rap.rwt.remote.JsonMapping;
import org.eclipse.swt.graphics.Image;


/**
 * Defines a region in a template that displays an image.
 *
 * @since 2.2
 */
public class ImageCell extends Cell<ImageCell> {

  /**
   * Represents a method used to scale an image.
   */
  public static enum ScaleMode {
    /**
     * The image is displayed in its original size.
     */
    NONE,
    /**
     * The image is scaled to the maximum size that fits into the cell. The aspect ratio is
     * preserved.
     */
    FIT,
    /**
     * The image is scaled to the minimum size required to cover the entire cell. The aspect ratio
     * is preserved.
     */
    FILL,
    /**
     * The image is scaled to the exact bounds of the cell. The aspect ratio is not preserved.
     */
    STRETCH
  }

  private static final String TYPE_IMAGE = "image";
  private static final String PROPERTY_IMAGE = "image";
  private static final String PROPERTY_SCALE_MODE = "scaleMode";
  private Image image;
  private ScaleMode scaleMode;

  /**
   * Constructs a new image cell and adds it to the given template.
   *
   * @param template the parent template, must not be <code>null</code>
   */
  public ImageCell( Template template ) {
    super( template, TYPE_IMAGE );
  }

  /**
   * Sets the image to be displayed in this cell if the <em>bindingIndex</em> is not set.
   * This can be used to display a static image.
   *
   * @param image an image, or <code>null</code> if no image should be displayed
   * @return the cell itself, to enable method chaining
   */
  public ImageCell setImage( Image image ) {
    this.image = image;
    return this;
  }

  Image getImage() {
    return image;
  }

  /**
   * Selects the method used for image scaling. The default is </code>ScaleMode.NONE</code>.
   *
   * @param scaleMode the scale mode to use, must not be <code>null</code>
   * @return the cell itself, to enable method chaining
   */
  public ImageCell setScaleMode( ScaleMode scaleMode ) {
    ParamCheck.notNull( scaleMode, "scaleMode" );
    this.scaleMode = scaleMode;
    return this;
  }

  ScaleMode getScaleMode() {
    return scaleMode;
  }

  @Override
  protected JsonObject toJson() {
    JsonObject json = super.toJson();
    if( image != null ) {
      json.add( PROPERTY_IMAGE, JsonMapping.toJson( image ) );
    }
    if( scaleMode != null ) {
      json.add( PROPERTY_SCALE_MODE, scaleMode.name() );
    }
    return json;
  }

}
