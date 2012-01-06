/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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
