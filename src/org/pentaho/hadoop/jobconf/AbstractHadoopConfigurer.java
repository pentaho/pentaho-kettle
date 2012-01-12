/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.hadoop.jobconf;

public abstract class AbstractHadoopConfigurer implements
HadoopConfigurer {

  protected String m_filesystemURL;
  protected String m_jobtrackerURL;

  public String getFilesystemURL() {
    return m_filesystemURL;
  }

  public String getJobtrackerURL() {
    return m_jobtrackerURL;
  }
  
  /**
   * Default implementation returns false
   * 
   * @return false
   */
  public boolean isDetectable() {
    return false;
  }
  
  /**
   * Default implementation returns false
   * 
   * @return false
   */
  public boolean isAvailable() {
    return false;
  }
}
