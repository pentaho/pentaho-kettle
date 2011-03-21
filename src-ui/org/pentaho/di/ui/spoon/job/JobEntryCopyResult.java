package org.pentaho.di.ui.spoon.job;

import java.util.List;

import org.pentaho.di.job.entry.JobEntryCopy;

public class JobEntryCopyResult {
  
  private String  jobEntryName;
  private Boolean result;
  private Long    errors;
  private int     copyNr;

  public JobEntryCopyResult(String jobEntryName, Boolean result, Long errors, int copyNr) {
    this.jobEntryName = jobEntryName;
    this.result = result;
    this.errors = errors;
    this.copyNr = copyNr;
  }

  public static JobEntryCopyResult findResult(List<JobEntryCopyResult> results, JobEntryCopy copy) {
    for (JobEntryCopyResult result : results) {
      if (result.getJobEntryName().equalsIgnoreCase(copy.getName()) && result.getCopyNr()==copy.getNr()) {
        return result;
      }
    }
    return null;
  }
  
  /**
   * @return the jobEntryName
   */
  public String getJobEntryName() {
    return jobEntryName;
  }

  /**
   * @param jobEntryName
   *          the jobEntryName to set
   */
  public void setJobEntryName(String jobEntryName) {
    this.jobEntryName = jobEntryName;
  }

  /**
   * @return the result
   */
  public Boolean getResult() {
    return result;
  }

  /**
   * @param result
   *          the result to set
   */
  public void setResult(Boolean result) {
    this.result = result;
  }

  /**
   * @return the errors
   */
  public Long getErrors() {
    return errors;
  }

  /**
   * @param errors
   *          the errors to set
   */
  public void setErrors(Long errors) {
    this.errors = errors;
  }

  /**
   * @return the copyNr
   */
  public int getCopyNr() {
    return copyNr;
  }

  /**
   * @param copyNr
   *          the copyNr to set
   */
  public void setCopyNr(int copyNr) {
    this.copyNr = copyNr;
  }

  
}
