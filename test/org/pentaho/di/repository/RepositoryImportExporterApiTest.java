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

package org.pentaho.di.repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.pentaho.di.core.ProgressMonitorListener;

public class RepositoryImportExporterApiTest extends TestCase {

  /**
   * Validate the the repository export api hasn't changed from what we use in example files.
   * 
   * @see RepositoryExporter#exportAllObjects(ProgressMonitorListener, String, RepositoryDirectoryInterface, String)
   */
  public void testExportApi() throws Exception {
    Class<RepositoryExporter> exporter = RepositoryExporter.class;

    // Make sure we the expected constructor that takes a repository
    Constructor<RepositoryExporter> c = exporter.getConstructor(Repository.class);
    assertNotNull(c);

    // Make sure we have the correct signature for exporting objects
    // RepositoryExporter.exportAllObjects(ProgressMonitorListener, String, RepositoryDirectoryInterface, String)
    Class<?> param1 = ProgressMonitorListener.class;
    Class<?> param2 = String.class;
    Class<?> param3 = RepositoryDirectoryInterface.class;
    Class<?> param4 = String.class;
    Method m = exporter.getMethod("exportAllObjects", param1, param2, param3, param4); //$NON-NLS-1$
    assertNotNull(m);
  }

  
  /**
   * Validate the the repository import api hasn't changed from what we use in example files.
   * 
   * @see RepositoryImporter#importAll(RepositoryImportFeedbackInterface, String, String[], RepositoryDirectoryInterface, boolean, boolean, String)
   */
  public void testImportApi() throws Exception {
    Class<RepositoryImporter> importer = RepositoryImporter.class;

    // Make sure we the expected constructor that takes a repository
    Constructor<RepositoryImporter> c = importer.getConstructor(Repository.class);
    assertNotNull(c);

    // Make sure we have the correct signature for importing objects
    // RepositoryImporter.importAll(RepositoryImportFeedbackInterface, String, String[], RepositoryDirectoryInterface, boolean, boolean, String)
    Class<?> param1 = RepositoryImportFeedbackInterface.class;
    Class<?> param2 = String.class;
    Class<?> param3 = String[].class;
    Class<?> param4 = RepositoryDirectoryInterface.class;
    Class<?> param5 = boolean.class;
    Class<?> param6 = boolean.class;
    Class<?> param7 = String.class;
    importer.getMethod("importAll", param1, param2, param3, param4, param5, param6, param7); //$NON-NLS-1$
  }
}
