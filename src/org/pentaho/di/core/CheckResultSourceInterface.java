/***********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 package org.pentaho.di.core;

/**
 * 
 * Implementing classes of this interface can provide more information about
 * the source of the CheckResult remark.
 * 
 * @author mbatchel
 * 6/25/07
 */
public interface CheckResultSourceInterface {
  /**
   * @return The name of the source generating the CheckResult
   */
  public String getName();
  /**
   * @return The description of the source generating the CheckResult
   */
  public String getDescription();
  /**
   * @return The ID of the source generating the CheckResult
   */
  public long getID();
  /**
   * @return The Type ID of the source generating the CheckResult. The Type ID
   * is the system-defined type identifier (like TRANS or SORT).
   */
  public String getTypeId();
}
