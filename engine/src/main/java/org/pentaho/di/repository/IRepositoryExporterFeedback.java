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
