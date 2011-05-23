package org.pentaho.di.repository;

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;

public interface RepositoryImportFeedbackInterface {

  public void addLog(String line);
  public void setLabel(String labelText);
  public void updateDisplay();
  
  public boolean transOverwritePrompt(TransMeta transMeta);
  public boolean jobOverwritePrompt(JobMeta jobMeta);

  public void showError(String title, String message, Exception e);
  public boolean askContinueOnErrorQuestion(String title, String message);
  
}
