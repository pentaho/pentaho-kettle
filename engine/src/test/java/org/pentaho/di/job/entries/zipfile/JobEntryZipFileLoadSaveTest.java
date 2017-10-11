/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.zipfile;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobEntryZipFileLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryZipFile> {
  @Override
  protected Class<JobEntryZipFile> getJobEntryClass() {
    return JobEntryZipFile.class;
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return new HashMap<String, String>() {
      {
        put( "createParentFolder", "getcreateparentfolder" );
      }
    };
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return new HashMap<String, String>() {
      {
        put( "createParentFolder", "setcreateparentfolder" );
      }
    };
  }

  @Override
  protected List<String> listCommonAttributes() {
    // NOTE: Many of these "properties" refer to the method to get at the variable. In these cases the member variables
    // and their getters/setters don't follow the bean getter/setter pattern.
    return Arrays.asList(
      "zipFilename",
      "compressionRate",
      "ifZipFileExists",
      "afterZip",
      "wildcard",
      "wildcardExclude",
      "sourceDirectory",
      "moveToDirectory",
      "addFileToResult",
      "datafromprevious",
      "createParentFolder",
      "dateInFilename",
      "timeInFilename",
      "specifyFormat",
      "dateTimeFormat",
      "createMoveToDirectory",
      "includingSubFolders",
      "storedSourcePathDepth"
    );
  }
}
