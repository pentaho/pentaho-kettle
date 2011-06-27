package org.pentaho.di.repository;

import org.pentaho.di.core.ProgressMonitorListener;

/**
 * Handles importing a repository.
 * 
 * @author jganoff
 */
public interface IRepositoryImporter extends ProgressMonitorListener, RepositoryImportFeedbackInterface,
    RepositoryElementReadListener {

  /**
   * Import objects from an XML document to a repository.
   * 
   * @param feedback Required to provide feedback to the user.
   * @param fileDirectory Base directory to load files (named {@code filenames}) from
   * @param filenames Names of files found in {@code fileDirectory} to be imported.
   * @param baseDirectory Base directory to load objects into.
   * @param overwrite Should objects in the repository be overwritten with ones we're importing?
   * @param continueOnError Should the import continue if there is an error importing an object?
   * @param versionComment Comment to use when saving imported objects.
   */
  public void importAll(RepositoryImportFeedbackInterface feedback, String fileDirectory, String[] filenames,
      RepositoryDirectoryInterface baseDirectory, boolean overwrite, boolean continueOnError, String versionComment);
  
  /**
   * Overrides repository directory for all imported transformations.
   */
  public void setTransDirOverride(String transDirOverride);
  
  /**
   * Overrides repository directory for all imported jobs.
   */
  public void setJobDirOverride(String jobDirOverride);
}
