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

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;

public interface RepositoryImportFeedbackInterface {

  public void addLog( String line );

  public void setLabel( String labelText );

  public void updateDisplay();

  public boolean transOverwritePrompt( TransMeta transMeta );

  public boolean jobOverwritePrompt( JobMeta jobMeta );

  public void showError( String title, String message, Exception e );

  public boolean askContinueOnErrorQuestion( String title, String message );

  public boolean isAskingOverwriteConfirmation();
}
