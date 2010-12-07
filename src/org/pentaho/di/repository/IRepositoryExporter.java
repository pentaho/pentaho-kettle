package org.pentaho.di.repository;

import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;

/**
 * Handles exporting a repository.
 * 
 * @author jganoff
 */
public interface IRepositoryExporter {

  /**
   * Export objects of a repository.
   * 
   * @param monitor Progress Monitor for providing feedback during the export process.
   * @param xmlFilename Filename to write out to.
   * @param root Root directory to start export from.
   * @param exportType Type of objects to export: "all", "trans", "job"
   * 
   * @throws KettleException
   */
  public void exportAllObjects(ProgressMonitorListener monitor, String xmlFilename, RepositoryDirectoryInterface root,
      String exportType) throws KettleException;
}
