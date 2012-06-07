/*
 * ******************************************************************************
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */

package org.pentaho.di.job.entries.oozie;

import org.pentaho.di.job.BlockableJobConfig;
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Model for the Oozie Job Executor
 *
 * User: RFellows
 * Date: 6/4/12
 */
public class OozieJobExecutorConfig extends BlockableJobConfig implements XulEventSource, Cloneable {

  public static final String OOZIE_WORKFLOW = "oozieWorkflow";
  public static final String OOZIE_URL = "oozieUrl";
  public static final String OOZIE_WORKFLOW_CONFIG = "oozieWorkflowConfig";

  private String oozieUrl = null;
  private String oozieWorkflowConfig = null;
  private String oozieWorkflow = null;

  @Bindable
  public String getOozieUrl() {
    return oozieUrl;
  }

  @Bindable
  public void setOozieUrl(String oozieUrl) {
    String prev = this.oozieUrl;
    this.oozieUrl = oozieUrl;
    pcs.firePropertyChange(OOZIE_URL, prev, this.oozieUrl);
  }

  @Bindable
  public String getOozieWorkflowConfig() {
    return oozieWorkflowConfig;
  }

  @Bindable
  public void setOozieWorkflowConfig(String oozieWorkflowConfig) {
    String prev = this.oozieWorkflowConfig;
    this.oozieWorkflowConfig = oozieWorkflowConfig;
    pcs.firePropertyChange(OOZIE_WORKFLOW_CONFIG, prev, this.oozieWorkflowConfig);
  }

  @Bindable
  public String getOozieWorkflow() {
    return this.oozieWorkflow;
  }
  @Bindable
  public void setOozieWorkflow(String oozieWorkflow) {
    String prev = this.oozieWorkflow;
    this.oozieWorkflow = oozieWorkflow;
    pcs.firePropertyChange(OOZIE_WORKFLOW, prev, oozieWorkflow);
  }

}
