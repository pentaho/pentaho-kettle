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
  public String getName();
  public String getDescription();
  public long getID();
  public String getTypeId();
}
