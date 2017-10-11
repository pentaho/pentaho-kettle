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
import org.pentaho.di.imp.ImportRules;

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
   * @param feedback
   *          Required to provide feedback to the user.
   * @param fileDirectory
   *          Base directory to load files (named {@code filenames}) from
   * @param filenames
   *          Names of files found in {@code fileDirectory} to be imported.
   * @param baseDirectory
   *          Base directory to load objects into.
   * @param overwrite
   *          Should objects in the repository be overwritten with ones we're importing?
   * @param continueOnError
   *          Should the import continue if there is an error importing an object?
   * @param versionComment
   *          Comment to use when saving imported objects.
   */
  public void importAll( RepositoryImportFeedbackInterface feedback, String fileDirectory, String[] filenames,
    RepositoryDirectoryInterface baseDirectory, boolean overwrite, boolean continueOnError,
    String versionComment );

  /**
   * Set the list of rules that need to be applied to every imported object.
   *
   * @param importRules
   *          The rules to use during import into the repository
   */
  public void setImportRules( ImportRules importRules );

  /**
   * Overrides repository directory for all imported transformations.
   */
  public void setTransDirOverride( String transDirOverride );

  /**
   * Overrides repository directory for all imported jobs.
   */
  public void setJobDirOverride( String jobDirOverride );

  /**
   * Returns a list of exceptions that the implementation may maintain.
   *
   * @return
   */
  public List<Exception> getExceptions();
}
