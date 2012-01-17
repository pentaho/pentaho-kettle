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

package org.pentaho.di.ui.job.entries.hadooptransjobexecutor;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.dom4j.DocumentException;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.hadooptransjobexecutor.JobEntryHadoopTransJobExecutor;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class JobEntryHadoopTransJobExecutorDialog extends JobEntryDialog implements JobEntryDialogInterface {
  private static final Class<?> CLZ = JobEntryHadoopTransJobExecutor.class;

  private JobEntryHadoopTransJobExecutor jobEntry;

  private JobEntryHadoopTransJobExecutorController controller = new JobEntryHadoopTransJobExecutorController();

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

  public JobEntryHadoopTransJobExecutorDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta) throws XulException, DocumentException, Throwable {
    super(parent, jobEntry, rep, jobMeta);
    
    this.jobEntry = (JobEntryHadoopTransJobExecutor) jobEntry;

    SwtXulLoader swtXulLoader = new SwtXulLoader();
    swtXulLoader.registerClassLoader(getClass().getClassLoader());
    swtXulLoader.register("VARIABLETEXTBOX", "org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox");
    swtXulLoader.setOuterContext(shell);

    container = swtXulLoader.loadXul("org/pentaho/di/ui/job/entries/hadooptransjobexecutor/JobEntryHadoopTransJobExecutorDialog.xul", bundle); //$NON-NLS-1$

    final XulRunner runner = new SwtXulRunner();
    runner.addContainer(container);

    container.addEventHandler(controller);

    bf = new DefaultBindingFactory();
    bf.setDocument(container.getDocumentRoot());
    bf.setBindingType(Type.BI_DIRECTIONAL);
    
    bf.createBinding("hadoop-distribution", "selectedItem", controller, JobEntryHadoopTransJobExecutorController.HADOOP_DISTRIBUTION); //$NON-NLS-1$ //$NON-NLS-2$ 
    
    bf.createBinding("mapper-storage-type", "selectedItem", controller, JobEntryHadoopTransJobExecutorController.MAPPER_STORAGE_TYPE); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("combiner-storage-type", "selectedItem", controller, JobEntryHadoopTransJobExecutorController.COMBINER_STORAGE_TYPE); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("reducer-storage-type", "selectedItem", controller, JobEntryHadoopTransJobExecutorController.REDUCER_STORAGE_TYPE); //$NON-NLS-1$ //$NON-NLS-2$ 

    bf.createBinding("jobentry-name", "value", controller, JobEntryHadoopTransJobExecutorController.JOB_ENTRY_NAME); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("jobentry-hadoopjob-name", "value", controller, JobEntryHadoopTransJobExecutorController.HADOOP_JOB_NAME); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("jobentry-map-transformation", "value", controller, JobEntryHadoopTransJobExecutorController.MAP_TRANS); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("jobentry-combiner-transformation", "value", controller, JobEntryHadoopTransJobExecutorController.COMBINER_TRANS); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("jobentry-reduce-transformation", "value", controller, JobEntryHadoopTransJobExecutorController.REDUCE_TRANS); //$NON-NLS-1$ //$NON-NLS-2$ 

    bf.createBinding("jobentry-map-input-stepname", "value", controller, JobEntryHadoopTransJobExecutorController.MAP_TRANS_INPUT_STEP_NAME); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("jobentry-map-output-stepname", "value", controller, JobEntryHadoopTransJobExecutorController.MAP_TRANS_OUTPUT_STEP_NAME); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("jobentry-combiner-input-stepname", "value", controller, JobEntryHadoopTransJobExecutorController.COMBINER_TRANS_INPUT_STEP_NAME); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("jobentry-combiner-output-stepname", "value", controller, JobEntryHadoopTransJobExecutorController.COMBINER_TRANS_OUTPUT_STEP_NAME); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("jobentry-reduce-input-stepname", "value", controller, JobEntryHadoopTransJobExecutorController.REDUCE_TRANS_INPUT_STEP_NAME); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("jobentry-reduce-output-stepname", "value", controller, JobEntryHadoopTransJobExecutorController.REDUCE_TRANS_OUTPUT_STEP_NAME); //$NON-NLS-1$ //$NON-NLS-2$


    bf.createBinding("classes-suppress-output-map-key", "selected", controller, JobEntryHadoopTransJobExecutorController.SUPPRESS_OUTPUT_MAP_KEY); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("classes-suppress-output-map-value", "selected", controller, JobEntryHadoopTransJobExecutorController.SUPPRESS_OUTPUT_MAP_VALUE); //$NON-NLS-1$ //$NON-NLS-2$
  
    bf.createBinding("classes-suppress-output-key", "selected", controller, JobEntryHadoopTransJobExecutorController.SUPPRESS_OUTPUT_KEY); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("classes-suppress-output-value", "selected", controller, JobEntryHadoopTransJobExecutorController.SUPPRESS_OUTPUT_VALUE); //$NON-NLS-1$ //$NON-NLS-2$

    bf.createBinding("classes-input-format", "value", controller, JobEntryHadoopTransJobExecutorController.INPUT_FORMAT_CLASS); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("classes-output-format", "value", controller, JobEntryHadoopTransJobExecutorController.OUTPUT_FORMAT_CLASS); //$NON-NLS-1$ //$NON-NLS-2$

 /*   final BindingConvertor<String, Integer> bindingConverter = new BindingConvertor<String, Integer>() {

      public Integer sourceToTarget(String value) {
        return Integer.parseInt(value);
      }

      public String targetToSource(Integer value) {
        return value.toString();
      }

    }; */

    bf.createBinding("num-map-tasks", "value", controller, JobEntryHadoopTransJobExecutorController.NUM_MAP_TASKS); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("num-reduce-tasks", "value", controller, JobEntryHadoopTransJobExecutorController.NUM_REDUCE_TASKS); //$NON-NLS-1$ //$NON-NLS-2$ 

    bf.createBinding("blocking", "selected", controller, JobEntryHadoopTransJobExecutorController.BLOCKING); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("logging-interval", "value", controller, JobEntryHadoopTransJobExecutorController.LOGGING_INTERVAL); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("input-path", "value", controller, JobEntryHadoopTransJobExecutorController.INPUT_PATH); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("output-path", "value", controller, JobEntryHadoopTransJobExecutorController.OUTPUT_PATH); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding("clean-output-path", "selected", controller, JobEntryHadoopTransJobExecutorController.CLEAN_OUTPUT_PATH); //$NON-NLS-1$ //$NON-NLS-2$

    bf.createBinding("hdfs-hostname", "value", controller, JobEntryHadoopTransJobExecutorController.HDFS_HOSTNAME); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("hdfs-port", "value", controller, JobEntryHadoopTransJobExecutorController.HDFS_PORT); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("job-tracker-hostname", "value", controller, JobEntryHadoopTransJobExecutorController.JOB_TRACKER_HOSTNAME); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("job-tracker-port", "value", controller, JobEntryHadoopTransJobExecutorController.JOB_TRACKER_PORT); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding("working-dir", "value", controller, JobEntryHadoopTransJobExecutorController.WORKING_DIRECTORY); //$NON-NLS-1$ //$NON-NLS-2$ 

    XulTree variablesTree = (XulTree) container.getDocumentRoot().getElementById("fields-table"); //$NON-NLS-1$
    bf.setBindingType(Type.ONE_WAY);
    bf.createBinding(controller.getUserDefined(), "children", variablesTree, "elements"); //$NON-NLS-1$//$NON-NLS-2$
    bf.setBindingType(Type.BI_DIRECTIONAL);

    XulTextbox loggingInterval = (XulTextbox) container.getDocumentRoot().getElementById("logging-interval"); //$NON-NLS-1$
    loggingInterval.setValue("" + controller.getLoggingInterval()); //$NON-NLS-1$

    XulTextbox mapTasks = (XulTextbox) container.getDocumentRoot().getElementById("num-map-tasks"); //$NON-NLS-1$
    mapTasks.setValue("" + controller.getNumMapTasks()); //$NON-NLS-1$

    XulTextbox reduceTasks = (XulTextbox) container.getDocumentRoot().getElementById("num-reduce-tasks"); //$NON-NLS-1$
    reduceTasks.setValue("" + controller.getNumReduceTasks()); //$NON-NLS-1$

    controller.setJobEntry((JobEntryHadoopTransJobExecutor) jobEntry);
    controller.setShell(parent);
    controller.setRepository(rep);
    controller.init();
  }

  public JobEntryInterface open() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("job-entry-dialog"); //$NON-NLS-1$
    dialog.show();
    return jobEntry;
  }

}
