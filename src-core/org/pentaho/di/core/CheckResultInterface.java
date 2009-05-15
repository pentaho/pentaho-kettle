/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core;


public interface CheckResultInterface {

  public static final int TYPE_RESULT_NONE = 0;

  public static final int TYPE_RESULT_OK = 1;

  public static final int TYPE_RESULT_COMMENT = 2;

  public static final int TYPE_RESULT_WARNING = 3;

  public static final int TYPE_RESULT_ERROR = 4;

  /**
   * @return The type of the Check Result (0-4)
   */
  public int getType();
  /**
   * @return The internationalized type description
   */
  public String getTypeDesc();
  /**
   * @return The text of the check result.
   */
  public String getText();
  /**
   * @return The source of the check result
   */
  public CheckResultSourceInterface getSourceInfo();
  /**
   * @return String description of the check result
   */
  public String toString();
  /**
   * @return The component-specific result code. 
   */
  public String getErrorCode();
  /**
   * Sets the component-specific result/error code.
   * @param errorCode Unchecked string that can be used for validation
   */
  public void setErrorCode(String errorCode);
  /**
   * Sets the check-result type
   * @param value The type from 0-4
   */
  public void setType(int value);
  /**
   * Sets the text for the check-result
   * @param value 
   */
  public void setText(String value);
  
}
