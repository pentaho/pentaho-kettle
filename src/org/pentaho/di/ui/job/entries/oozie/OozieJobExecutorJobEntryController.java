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

package org.pentaho.di.ui.job.entries.oozie;

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.BlockableJobConfig;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorConfig;
import org.pentaho.di.job.entries.oozie.OozieJobExecutorJobEntry;
import org.pentaho.di.ui.job.AbstractJobEntryController;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.util.Collection;

/**
 * User: RFellows
 * Date: 6/4/12
 */
public class OozieJobExecutorJobEntryController extends AbstractJobEntryController<OozieJobExecutorConfig, OozieJobExecutorJobEntry> {

  public static final String OOZIE_JOB_EXECUTOR = "oozie-job-executor";
  private static final String VALUE = "value";

  public OozieJobExecutorJobEntryController(JobMeta jobMeta, XulDomContainer container, OozieJobExecutorJobEntry jobEntry, BindingFactory bindingFactory) {
    super(jobMeta, container, jobEntry, bindingFactory);
  }

  @Override
  protected void syncModel() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  protected void createBindings(OozieJobExecutorConfig config, XulDomContainer container, BindingFactory bindingFactory, Collection<Binding> bindings) {
    bindingFactory.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bindings.add(bindingFactory.createBinding(config, BlockableJobConfig.JOB_ENTRY_NAME, BlockableJobConfig.JOB_ENTRY_NAME, VALUE));
    bindings.add(bindingFactory.createBinding(config, OozieJobExecutorConfig.OOZIE_URL, OozieJobExecutorConfig.OOZIE_URL, VALUE));
    bindings.add(bindingFactory.createBinding(config, OozieJobExecutorConfig.OOZIE_WORKFLOW, OozieJobExecutorConfig.OOZIE_WORKFLOW, VALUE));
  }

  @Override
  protected String getDialogElementId() {
    return OOZIE_JOB_EXECUTOR;
  }

  /**
   * Make sure everything required is entered and valid
   */
  @Bindable
  public void test() {

  }

  @Override
  protected void beforeInit() {
//    this.suppressEventHandling = false;
  }
}
