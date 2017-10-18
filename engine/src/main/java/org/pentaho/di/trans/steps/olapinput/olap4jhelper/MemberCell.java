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

import java.util.List;

public class MemberCell extends AbstractBaseCell {

  private String parentDimension = null;

  private String parentMember = null;

  private MemberCell rightOf = null;

  private String uniqueName;

  private String rightOfDimension;

  private List<String> memberPath;

  /**
   *
   * Blank Constructor for Serializable niceness, don't use it.
   *
   */
  public MemberCell() {
    super();
  }

  /**
   *
   * Creates a member cell.
   *
   * @param b
   * @param c
   */
  public MemberCell( final boolean right, final boolean sameAsPrev ) {
    super();
    this.right = right;
    this.sameAsPrev = sameAsPrev;
  }

  public void setParentDimension( final String parDim ) {
    parentDimension = parDim;
  }

  public String getParentDimension() {
    return parentDimension;
  }

  /**
   * TODO JAVADOC
   *
   * @param parentMember
   */
  public void setParentMember( final String parentMember ) {

    this.parentMember = parentMember;

  }

  public String getParentMember() {
    return parentMember;
  }

  /**
   * TODO JAVADOC
   *
   * @param memberCell
   */
  public void setRightOf( final MemberCell memberCell ) {
    this.rightOf = memberCell;

  }

  public MemberCell getRightOf() {
    return rightOf;
  }

  /**
   * TODO JAVADOC
   *
   * @param name
   */
  public void setRightOfDimension( String name ) {

    this.rightOfDimension = name;

  }

  public String getRightOfDimension() {
    return this.rightOfDimension;
  }

  public void setMemberPath( List<String> memberPath ) {
    this.memberPath = memberPath;

  }

  public List<String> getMemberPath() {
    return memberPath;
  }

  public String getUniqueName() {
    return uniqueName;
  }

  public void setUniqueName( String name ) {
    uniqueName = name;
  }

}
