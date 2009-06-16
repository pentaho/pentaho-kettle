/* * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 package org.pentaho.di.core;

import org.pentaho.di.repository.ObjectId;

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
  public ObjectId getObjectId();
  /**
   * @return The Type ID of the source generating the CheckResult. The Type ID
   * is the system-defined type identifier (like TRANS or SORT).
   */
  public String getTypeId();
}
