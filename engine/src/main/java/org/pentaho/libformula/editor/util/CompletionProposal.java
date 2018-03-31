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

package org.pentaho.libformula.editor.util;

public class CompletionProposal {
  private String menuText;
  private String completionString;
  int offset;

  public CompletionProposal( String menuText, String completionString, int offset ) {
    this.menuText = menuText;
    this.completionString = completionString;
    this.offset = offset;
  }

  /**
   * @return the menuText
   */
  public String getMenuText() {
    return menuText;
  }

  /**
   * @param menuText
   *          the menuText to set
   */
  public void setMenuText( String menuText ) {
    this.menuText = menuText;
  }

  /**
   * @return the completionString
   */
  public String getCompletionString() {
    return completionString;
  }

  /**
   * @param completionString
   *          the completionString to set
   */
  public void setCompletionString( String completionString ) {
    this.completionString = completionString;
  }

  /**
   * @return the offset
   */
  public int getOffset() {
    return offset;
  }

  /**
   * @param offset
   *          the offset to set
   */
  public void setOffset( int offset ) {
    this.offset = offset;
  }
}
