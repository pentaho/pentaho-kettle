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

package org.pentaho.util;

import java.io.File;

/**
 * Class to provide simple File services.
 * 
 * @author sflatley
 */
public class FileUtil {

    public static synchronized boolean deleteDir(File dir) {
        
        if (dir.isDirectory()) { 
            String[] children = dir.list(); 
            for (int i=0; i<children.length; i++) { 
                boolean success = deleteDir(new File(dir, children[i])); 
                if (!success) { 
                    return false; 
                } 
            }
        } // The directory is now empty so delete it 
        return dir.delete(); 
    } 
}