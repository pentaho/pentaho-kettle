package org.pentaho.di.trans.steps.autodoc;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.ui.Drawable;
import org.pentaho.reporting.engine.classic.core.DataRow;

public class TransJobDrawable implements Drawable {

  private DataRow dataRow;
  private boolean pixelateImages;

  public TransJobDrawable(DataRow dataRow, boolean pixelateImages) {
    this.dataRow = dataRow;
    this.pixelateImages = pixelateImages;
  }
  
  @Override
  public void draw(Graphics2D graphics2D, Rectangle2D rectangle2D) {
    try {
      ReportSubjectLocation location = (ReportSubjectLocation) dataRow.get("location");
      if (location.isTransformation()) {
        TransformationInformation ti = TransformationInformation.getInstance();
        ti.drawImage(graphics2D, rectangle2D, location, pixelateImages);
      } else  {
        JobInformation ji = JobInformation.getInstance();
        ji.drawImage(graphics2D, rectangle2D, location, pixelateImages);
      }
    } catch(Exception e) {
      throw new RuntimeException("Unable to draw image onto report", e);
    }
  }

  /**
   * @return the dataRow
   */
  public DataRow getDataRow() {
    return dataRow;
  }

  /**
   * @param dataRow the dataRow to set
   */
  public void setDataRow(DataRow dataRow) {
    this.dataRow = dataRow;
  }

  /**
   * @return the pixelateImages
   */
  public boolean isPixelateImages() {
    return pixelateImages;
  }

  /**
   * @param pixelateImages the pixelateImages to set
   */
  public void setPixelateImages(boolean pixelateImages) {
    this.pixelateImages = pixelateImages;
  }
}
