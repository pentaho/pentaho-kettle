
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.utils;

public class VFSConnectionTestOptions {

  public VFSConnectionTestOptions() {
  }

  /**
   *
   * @param ignoreRootLocation
   */
  public VFSConnectionTestOptions( boolean ignoreRootLocation ) {
    this.ignoreRootPath = ignoreRootLocation;
  }


  private boolean ignoreRootPath;


  /**
   * Returns true if vfs connection root path is ignored
   * @return boolean
   */
  public boolean isIgnoreRootPath() {
    return ignoreRootPath;
  }


  /**
   * Sets the ignoreRootPath, given as a boolean
   * @param ignoreRootPath ignoring the root path
   */
  public void setIgnoreRootPath( boolean ignoreRootPath ) {

    this.ignoreRootPath = ignoreRootPath;

  }
}

