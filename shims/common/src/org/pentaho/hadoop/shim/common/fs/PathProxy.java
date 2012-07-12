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

package org.pentaho.hadoop.shim.common.fs;

import org.pentaho.hadoop.shim.api.fs.Path;

public class PathProxy extends org.apache.hadoop.fs.Path implements Path {

  public PathProxy(String path) {
    super(path);
  }
  
  public PathProxy(String parent, String child) {
    this(new PathProxy(parent), child);
  }

  public PathProxy(Path parent, String child) {
    super((org.apache.hadoop.fs.Path) parent, child);
  }
}
