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

public abstract class AbstractBaseCell {

  /** The formatted value. */
  private String formattedValue;

  /** The raw value. */
  private String rawValue;

  public boolean right = false;

  public boolean sameAsPrev = false;

  private String parentDimension = null;

  /**
   * Blank Constructor for serialization dont use.
   */
  public AbstractBaseCell() {
  }

  /**
   * BaseCell Constructor, every cell type should inherit basecell.
   *
   * @param right
   * @param sameAsPrev
   */
  public AbstractBaseCell( final boolean right, final boolean sameAsPrev ) {
    this.right = right;
    this.sameAsPrev = sameAsPrev;
  }

  /**
   * Gets the formatted value.
   *
   * @return the formatted value
   */
  public String getFormattedValue() {
    return formattedValue;
  }

  /**
   * Gets the raw value.
   *
   * @return the raw value
   */
  public String getRawValue() {
    return rawValue;
  }

  /**
   * Sets the formatted value.
   *
   * @param formattedValue
   *          the new formatted value
   */
  public void setFormattedValue( final String formattedValue ) {
    this.formattedValue = formattedValue;
  }

  /**
   * Sets the raw value.
   *
   * @param rawValue
   *          the new raw value
   */
  public void setRawValue( final String rawValue ) {
    this.rawValue = rawValue;
  }

  /**
   *
   * @param set
   */
  public void setRight( final boolean set ) {
    this.right = set;
  }

  /**
   * Set true if value is same as the previous one in the row.
   *
   * @param same
   */
  public void setSameAsPrev( final boolean same ) {
    this.sameAsPrev = same;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return formattedValue;
  }

  public void setParentDimension( final String pdim ) {
    parentDimension = pdim;
  }

  public String getParentDimension() {
    return parentDimension;
  }

}
