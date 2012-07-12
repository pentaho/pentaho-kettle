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

package org.pentaho.hadoop.shim.api.fs;

import java.io.IOException;

/**
 * An abstraction for {@link org.apache.hadoop.fs.FileSystem}.
 * 
 * @author Jordan Ganoff (jganoff@pentaho.com)
 */
public interface FileSystem {
  /**
   * @return the underlying File System implementation
   */
  Object getDelegate();
  
  /**
   * Create a {@link Path} object out of the path string provided.
   * 
   * @param path Location of path to create
   * @return Path to the string provided
   */
  Path asPath(String path);

  /**
   * Creates a path by composing a parent and a relative path to a child.
   * 
   * @param parent Parent path
   * @param child String representing the location of the child path relative to {@code parent}
   * @return Path of child relative to parent
   */
  Path asPath(Path parent, String child);

  /**
   * @see #asPath(Path, String)
   * 
   * @param parent String representing the location of the path to use to resolve {@code child}
   * @param child String representing the location of the child path relative to {@code parent}
   * @return Path of child relative to parent
   */
  Path asPath(String parent, String child);

  /**
   * Does the path reference an object?
   * 
   * @param path Path
   * @return {@code true} if the path points to an object
   * @throws IOException Error communicating with the file system
   */
  boolean exists(Path path) throws IOException;

  /**
   * Removes the path provided.
   * 
   * @param path Path to remove
   * @param recursive Flag indicating if we should delete all children (recursively)
   * @return {@code true} if the path was deleted successfully
   * @throws IOException Error deleting path
   */
  boolean delete(Path path, boolean recursive) throws IOException;
}
