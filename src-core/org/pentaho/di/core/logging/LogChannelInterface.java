/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.core.logging;

public interface LogChannelInterface {

  /**
   * @return the id of the logging channel
   */
  public String getLogChannelId();
  
  public LogLevel getLogLevel();
  
  public void setLogLevel(LogLevel logLevel);

  /**
   * @return the containerObjectId
   */
  public String getContainerObjectId();
  
  /**
   * @param containerObjectId the containerObjectId to set
   */
  public void setContainerObjectId(String containerObjectId);
    
  public boolean isBasic();

  public boolean isDetailed();

  public boolean isDebug();

  public boolean isRowLevel();

  public boolean isError();

  public void logMinimal(String message);

  public void logMinimal(String message, Object... arguments);

  public void logBasic(String message);

  public void logBasic(String message, Object... arguments);

  public void logDetailed(String message);

  public void logDetailed(String message, Object... arguments);

  public void logDebug(String message);

  public void logDebug(String message, Object... arguments);

  public void logRowlevel(String message);

  public void logRowlevel(String message, Object... arguments);

  public void logError(String message);

  public void logError(String message, Throwable e);

  public void logError(String message, Object... arguments);


}
