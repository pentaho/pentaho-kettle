/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.olapinput.olap4jhelper;

public class CellDataSet {

  private int width;

  private int height;

  private AbstractBaseCell[][] cellSetHeader;

  private AbstractBaseCell[][] cellSetBody;

  private int offset;

  public CellDataSet() {
    super();
  }

  public CellDataSet( final int width, final int height ) {
    this.width = width;
    this.height = height;
  }

  public AbstractBaseCell[][] getCellSetHeaders() {
    return cellSetHeader;
  }

  public void setCellSetHeaders( final AbstractBaseCell[][] cellSet ) {
    this.cellSetHeader = cellSet;
  }

  public AbstractBaseCell[][] getCellSetBody() {
    return cellSetBody;
  }

  public void setCellSetBody( final AbstractBaseCell[][] cellSet ) {
    this.cellSetBody = cellSet;
  }

  public void setOffset( final int offset ) {
    // TODO Auto-generated method stub
    this.offset = offset;
  }

  public int getOffset() {
    return offset;

  }

  /**
   * @param width
   *          the width to set
   */
  public void setWidth( final int width ) {
    this.width = width;
  }

  /**
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * @param height
   *          the height to set
   */
  public void setHeight( final int height ) {
    this.height = height;
  }

  /**
   * @return the height
   */
  public int getHeight() {
    return height;
  }
}
