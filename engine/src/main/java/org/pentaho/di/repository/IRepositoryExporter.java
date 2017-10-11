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

package org.pentaho.di.repository;

import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.imp.ImportRules;

/**
 * Handles exporting a repository.
 *
 * @author jganoff
 */
public interface IRepositoryExporter {

  /**
   * Export objects of a repository.
   *
   * @param monitor
   *          Progress Monitor for providing feedback during the export process.
   * @param xmlFilename
   *          Filename to write out to.
   * @param root
   *          Root directory to start export from.
   * @param exportType
   *          Type of objects to export: "all", "trans", "job"
   *
   * @throws KettleException
   */
  public void exportAllObjects( ProgressMonitorListener monitor, String xmlFilename,
    RepositoryDirectoryInterface root, String exportType ) throws KettleException;

  /**
   * Pass a set of import rules to the exporter to validate against during the export. This will allow a user to make
   * sure that the export transformations can be imported with the given set of rules.
   *
   * @param importRules
   *          The import rules to adhere to during export.
   */
  public void setImportRulesToValidate( ImportRules importRules );
}
