/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.steps.autodoc;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.ui.Drawable;
import org.pentaho.di.core.bowl.Bowl;

public class TransJobDrawable implements Drawable {

  private ReportSubjectLocation location;
  private boolean pixelateImages;
  private Bowl bowl;

  public TransJobDrawable( Bowl bowl, ReportSubjectLocation location, boolean pixelateImages ) {
    this.bowl = bowl;
    this.location = location;
    this.pixelateImages = pixelateImages;
  }

  @Override
  public void draw( Graphics2D graphics2D, Rectangle2D rectangle2D ) {
    try {
      if ( location.isTransformation() ) {
        TransformationInformation ti = TransformationInformation.getInstance();
        ti.drawImage( bowl, graphics2D, rectangle2D, location, pixelateImages );
      } else {
        JobInformation ji = JobInformation.getInstance();
        ji.drawImage( bowl, graphics2D, rectangle2D, location, pixelateImages );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( "Unable to draw image onto report", e );
    }
  }

  /**
   * @return the pixelateImages
   */
  public boolean isPixelateImages() {
    return pixelateImages;
  }

  /**
   * @param pixelateImages
   *          the pixelateImages to set
   */
  public void setPixelateImages( boolean pixelateImages ) {
    this.pixelateImages = pixelateImages;
  }
}
