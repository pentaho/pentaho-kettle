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

package org.pentaho.amazon.emr.ui;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.dom4j.DocumentException;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.amazon.emr.job.AmazonElasticMapReduceJobExecutor;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
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
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

import java.util.Enumeration;
import java.util.ResourceBundle;

public class AmazonElasticMapReduceJobExecutorDialog extends JobEntryDialog implements JobEntryDialogInterface {
  private static final Class<?> CLZ = AmazonElasticMapReduceJobExecutor.class;

  private AmazonElasticMapReduceJobExecutor jobEntry;

  private AmazonElasticMapReduceJobExecutorController controller = new AmazonElasticMapReduceJobExecutorController();

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

  public AmazonElasticMapReduceJobExecutorDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta) throws XulException,
      DocumentException {
    super(parent, jobEntry, rep, jobMeta);

 /*   final BindingConvertor<String, Integer> bindingConverter = new BindingConvertor<String, Integer>() {

      public Integer sourceToTarget(String value) {
        return Integer.parseInt(value);
      }

      public String targetToSource(Integer value) {
        return value.toString();
      }

    }; */

    this.jobEntry = (AmazonElasticMapReduceJobExecutor) jobEntry;

    SwtXulLoader swtXulLoader = new SwtXulLoader();
    swtXulLoader.registerClassLoader(getClass().getClassLoader());
    swtXulLoader.register("VARIABLETEXTBOX", "org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox");
    swtXulLoader.setOuterContext(shell);

    container = swtXulLoader.loadXul("org/pentaho/amazon/emr/ui/AmazonElasticMapReduceJobExecutorDialog.xul", bundle); //$NON-NLS-1$

    final XulRunner runner = new SwtXulRunner();
    runner.addContainer(container);

    container.addEventHandler(controller);

    bf = new DefaultBindingFactory();
    bf.setDocument(container.getDocumentRoot());
    bf.setBindingType(Type.BI_DIRECTIONAL);

    bf.createBinding("jobentry-name", "value", controller, AmazonElasticMapReduceJobExecutorController.JOB_ENTRY_NAME); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bf.createBinding("jobentry-hadoopjob-name", "value", controller, AmazonElasticMapReduceJobExecutorController.HADOOP_JOB_NAME); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bf.createBinding("jobentry-hadoopjob-flow-id", "value", controller, AmazonElasticMapReduceJobExecutorController.HADOOP_JOB_FLOW_ID); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bf.createBinding("jar-url", "value", controller, AmazonElasticMapReduceJobExecutorController.JAR_URL); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bf.createBinding("access-key", "value", controller, AmazonElasticMapReduceJobExecutorController.ACCESS_KEY); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bf.createBinding("secret-key", "value", controller, AmazonElasticMapReduceJobExecutorController.SECRET_KEY); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    BindingConvertor<String, FileObject> stagingDirBinding = new BindingConvertor<String, FileObject>() {

      @Override
      public FileObject sourceToTarget(String s) {
        // resolve the file
        try {
          VariableSpace vs = controller.getVariableSpace();
          FileSystemOptions opts = new FileSystemOptions();
          DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(
              opts,
              new StaticUserAuthenticator(null, controller.getAccessKey(), controller.getSecretKey())
          );
          FileObject file = KettleVFS.getFileObject(s, vs, opts);
          return file;
        } catch (FileSystemException e) {
          return null;
        } catch (KettleFileException e) {
          return null;
        }
      }

      @Override
      public String targetToSource(FileObject fileObject) {
        return fileObject.getName().getURI();
      }
    };

    bf.createBinding("s3-staging-directory", "value", controller, AmazonElasticMapReduceJobExecutorController.STAGING_DIR); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    bf.setBindingType(Type.ONE_WAY);
    bf.createBinding("s3-staging-directory", "value", controller, AmazonElasticMapReduceJobExecutorController.STAGING_DIR_FILE, stagingDirBinding); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    bf.setBindingType(Type.BI_DIRECTIONAL);
//    bf.createBinding("num-instances", "value", controller, AmazonElasticMapReduceJobExecutorController.NUM_INSTANCES, bindingConverter); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bf.createBinding("num-instances", "value", controller, AmazonElasticMapReduceJobExecutorController.NUM_INSTANCES); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bf.createBinding("master-instance-type", "selectedItem", controller, AmazonElasticMapReduceJobExecutorController.MASTER_INSTANCE_TYPE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bf.createBinding("slave-instance-type", "selectedItem", controller, AmazonElasticMapReduceJobExecutorController.SLAVE_INSTANCE_TYPE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    bf.createBinding("command-line-arguments", "value", controller, AmazonElasticMapReduceJobExecutorController.CMD_LINE_ARGS); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    XulTextbox numInstances = (XulTextbox) container.getDocumentRoot().getElementById("num-instances");
    numInstances.setValue("" + controller.getNumInstances());

    XulMenuList<String> masterInstanceType = (XulMenuList<String>) container.getDocumentRoot().getElementById("master-instance-type");
    masterInstanceType.setSelectedItem("" + controller.getMasterInstanceType());

    XulMenuList<String> slaveInstanceType = (XulMenuList<String>) container.getDocumentRoot().getElementById("slave-instance-type");
    slaveInstanceType.setSelectedItem("" + controller.getSlaveInstanceType());

    bf.createBinding("blocking", "selected", controller, AmazonElasticMapReduceJobExecutorController.BLOCKING); //$NON-NLS-1$ //$NON-NLS-2$ 
//    bf.createBinding("logging-interval", "value", controller, AmazonElasticMapReduceJobExecutorController.LOGGING_INTERVAL, bindingConverter); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("logging-interval", "value", controller, AmazonElasticMapReduceJobExecutorController.LOGGING_INTERVAL); //$NON-NLS-1$ //$NON-NLS-2$ 

    XulTextbox loggingInterval = (XulTextbox) container.getDocumentRoot().getElementById("logging-interval");
    loggingInterval.setValue("" + controller.getLoggingInterval());

    ExtTextbox tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("access-key");
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("secret-key");
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("jobentry-hadoopjob-name");
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("jobentry-hadoopjob-flow-id");
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("s3-staging-directory");
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("jar-url");
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("command-line-arguments");
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("num-instances");
    tempBox.setVariableSpace(controller.getVariableSpace());
    tempBox = (ExtTextbox) container.getDocumentRoot().getElementById("logging-interval");
    tempBox.setVariableSpace(controller.getVariableSpace());
    
    bf.setBindingType(Type.BI_DIRECTIONAL);

    controller.setJobEntry((AmazonElasticMapReduceJobExecutor) jobEntry);
    controller.init();
  }

  public JobEntryInterface open() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("amazon-emr-job-entry-dialog"); //$NON-NLS-1$
    dialog.show();
    return jobEntry;
  }

}
