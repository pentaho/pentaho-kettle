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

public class DataCell extends AbstractBaseCell {

  private Number rawNumber = null;

  private MemberCell parentColMember = null;

  /**
   *
   * Blank constructor for serialization purposes, don't use it.
   *
   */
  public DataCell() {
    super();
  }

  /**
   * Construct a Data Cell containing olap data.
   *
   * @param b
   * @param c
   */
  public DataCell( final boolean right, final boolean sameAsPrev ) {
    super();
    this.right = right;
    this.sameAsPrev = sameAsPrev;
  }

  public MemberCell getParentColMember() {
    return parentColMember;
  }

  public void setParentColMember( final MemberCell parentColMember ) {
    this.parentColMember = parentColMember;
  }

  public MemberCell getParentRowMember() {
    return parentRowMember;
  }

  public void setParentRowMember( final MemberCell parentRowMember ) {
    this.parentRowMember = parentRowMember;
  }

  private MemberCell parentRowMember = null;

  public Number getRawNumber() {
    return rawNumber;
  }

  public void setRawNumber( final Number rawNumber ) {
    this.rawNumber = rawNumber;
  }

}
