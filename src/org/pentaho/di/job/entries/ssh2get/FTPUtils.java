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

package org.pentaho.di.job.entries.ssh2get;


public class FTPUtils
{
	public static String FILE_SEPARATOR="/";
	   /**
     * normalize / to \ and remove trailing slashes from a path
     * 
     * @param path
     * @return normalized path
     * @throws Exception
     */
    public static String normalizePath(String path) throws Exception {
        if (path==null) return path;
        String normalizedPath = path.replaceAll("\\\\", FILE_SEPARATOR);
        while (normalizedPath.endsWith("\\") || normalizedPath.endsWith(FILE_SEPARATOR)) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length()-1);
        }
        
        return normalizedPath;
    }
}