/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.amazon.hive.ui;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.dom4j.DocumentException;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.amazon.hive.job.AmazonHiveJobExecutor;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.Binding.Type;import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class AmazonHiveJobExecutorDialog extends JobEntryDialog implements JobEntryDialogInterface {
  private static final Class<?> CLZ = AmazonHiveJobExecutor.class;

  private AmazonHiveJobExecutor jobEntry;

  private AmazonHiveJobExecutorController controller = new AmazonHiveJobExecutorController();

  private XulDomContainer container;

  private BindingFactory bf;

  private ResourceBundle bundle = new ResourceBundle() {
    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject(String key) {
      return BaseMessages.getString(CLZ, key);
    }
  };

  public AmazonHiveJobExecutorDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta) throws XulException,
      DocumentException {
    super(parent, jobEntry, rep, jobMeta);

    this.jobEntry = (AmazonHiveJobExecutor) jobEntry;

    SwtXulLoader swtXulLoader = new SwtXulLoader();
    swtXulLoader.registerClassLoader(getClass().getClassLoader());
    swtXulLoader.register("VARIABLETEXTBOX", "org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox"); //$NON-NLS-1$ //$NON-NLS-1$
    swtXulLoader.setOuterContext(shell);

    container = swtXulLoader.loadXul("org/pentaho/amazon/hive/ui/AmazonHiveJobExecutorDialog.xul", bundle); //$NON-NLS-1$

    final XulRunner runner = new SwtXulRunner();
    runner.addContainer(container);

    container.addEventHandler(controller);

    bf = new DefaultBindingFactory();
    bf.setDocument(container.getDocumentRoot());
    bf.setBindingType(Type.BI_DIRECTIONAL);

    bf.createBinding("jobentry-name", "value", controller, AmazonHiveJobExecutorController.JOB_ENTRY_NAME); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("jobentry-hadoopjob-name", "value", controller, AmazonHiveJobExecutorController.HADOOP_JOB_NAME); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("jobentry-hadoopjob-flow-id", "value", controller, AmazonHiveJobExecutorController.HADOOP_JOB_FLOW_ID); //$NON-NLS-1$ //$NON-NLS-3$
    bf.createBinding("q-url", "value", controller, AmazonHiveJobExecutorController.Q_URL); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("access-key", "value", controller, AmazonHiveJobExecutorController.ACCESS_KEY); //$NON-NLS-2$ //$NON-NLS-3$
    bf.createBinding("secret-key", "value", controller, AmazonHiveJobExecutorController.SECRET_KEY); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("bootstrap-actions", "value", controller, AmazonHiveJobExecutorController.BOOTSTRAP_ACTIONS); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("s3-staging-directory", "value", controller, AmazonHiveJobExecutorController.STAGING_DIR); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("command-line-arguments", "value", controller, AmazonHiveJobExecutorController.CMD_LINE_ARGS); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("num-instances", "value", controller, AmazonHiveJobExecutorController.NUM_INSTANCES); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("master-instance-type", "selectedItem", controller, AmazonHiveJobExecutorController.MASTER_INSTANCE_TYPE); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("slave-instance-type", "selectedItem", controller, AmazonHiveJobExecutorController.SLAVE_INSTANCE_TYPE); //$NON-NLS-1$ //$NON-NLS-2$

    XulTextbox numInstances = (XulTextbox) container.getDocumentRoot().getElementById("num-instances"); //$NON-NLS-1$
    numInstances.setValue("" + controller.getNumInstances());

    @SuppressWarnings("unchecked") //$NON-NLS-1$
    XulMenuList<String> masterInstanceType = (XulMenuList<String>) container.getDocumentRoot().getElementById("master-instance-type"); //$NON-NLS-1$
    masterInstanceType.setSelectedItem("" + controller.getMasterInstanceType());

    @SuppressWarnings("unchecked") //$NON-NLS-1$
    XulMenuList<String> slaveInstanceType = (XulMenuList<String>) container.getDocumentRoot().getElementById("slave-instance-type"); //$NON-NLS-1$
    slaveInstanceType.setSelectedItem("" + controller.getSlaveInstanceType());

    bf.createBinding("alive", "selected", controller, AmazonHiveJobExecutorController.ALIVE); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("blocking", "selected", controller, AmazonHiveJobExecutorController.BLOCKING); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("logging-interval", "value", controller, AmazonHiveJobExecutorController.LOGGING_INTERVAL); //$NON-NLS-1$ //$NON-NLS-2$ 

    XulTextbox loggingInterval = (XulTextbox) container.getDocumentRoot().getElementById("logging-interval"); //$NON-NLS-1$
    loggingInterval.setValue("" + controller.getLoggingInterval());

    ExtTextbox tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("access-key"); //$NON-NLS-1$
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("secret-key"); //$NON-NLS-1$
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("jobentry-hadoopjob-name"); //$NON-NLS-1$
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("jobentry-hadoopjob-flow-id"); //$NON-NLS-1$
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("bootstrap-actions"); //$NON-NLS-1$
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("s3-staging-directory"); //$NON-NLS-1$
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("q-url"); //$NON-NLS-1$
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("command-line-arguments"); //$NON-NLS-1$
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("num-instances"); //$NON-NLS-1$
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("logging-interval"); //$NON-NLS-1$
    tempBox.setVariableSpace(controller.getVariableSpace());
    
    bf.setBindingType(Type.BI_DIRECTIONAL);

    controller.setJobEntry((AmazonHiveJobExecutor) jobEntry);
    controller.init();
  }

  public JobEntryInterface open() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("amazon-emr-job-entry-dialog"); //$NON-NLS-1$
    dialog.show();
    return jobEntry;
  }

}
