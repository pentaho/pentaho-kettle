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

import java.util.List;

import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;

/**
 * The only way this interface extends base IRepositoryExporter is to bring new functionality to objects
 * that implements original interface, not to break backward compatibility.
 */
public interface IRepositoryExporterFeedback extends IRepositoryExporter {

  /**
   *
   * @param monitor - export monitor.
   * @param xmlFilename - export output file name
   * @param root - repository to export from
   * @param exportType - type of items to export
   * @return - list of processed items (Jobs and transformations) with exported statuses. Note - this list is
   * not a list of a really exported objects, but only export report. Some exporters that implements this interface
   * may not create output export file in case of not success exports.
   * @throws KettleException
   */
  public List<ExportFeedback> exportAllObjectsWithFeedback( ProgressMonitorListener monitor, String xmlFilename,
      RepositoryDirectoryInterface root, String exportType ) throws KettleException;

  /**
   * Call to this fast-access way to determine that export had rules violations. This avoid full scan of export results
   * for possible failures. If this method returns true - we may be sure that one or more export rules was
   * violated.
   * @return
   */
  public boolean isRulesViolation();
}
