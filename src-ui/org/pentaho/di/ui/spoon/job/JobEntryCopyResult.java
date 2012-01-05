/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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
