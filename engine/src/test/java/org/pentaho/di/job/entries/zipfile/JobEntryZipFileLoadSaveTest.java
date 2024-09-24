/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.job.entries.zipfile;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobEntryZipFileLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryZipFile> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
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
