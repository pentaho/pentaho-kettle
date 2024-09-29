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
