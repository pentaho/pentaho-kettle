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

package org.pentaho.hadoop.shim.common;

import org.pentaho.hadoop.shim.api.Configuration;
import org.pentaho.hadoop.shim.api.fs.FileSystem;
import org.pentaho.hadoop.shim.api.fs.Path;

public class ShimUtils {

  public static org.apache.hadoop.fs.FileSystem asFileSystem(FileSystem fs) {
    return fs == null ? null : (org.apache.hadoop.fs.FileSystem) fs.getDelegate();
  }

  @SuppressWarnings("deprecation")
  public static org.apache.hadoop.mapred.JobConf asConfiguration(Configuration c) {
    return (org.apache.hadoop.mapred.JobConf) c;
  }

  public static org.apache.hadoop.fs.Path asPath(Path path) {
    return (org.apache.hadoop.fs.Path) path;
  }
}
