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

package org.pentaho.di.ui.job.entries.sqoop;

import org.pentaho.di.job.entries.sqoop.AbstractSqoopJobEntry;
import org.pentaho.di.job.entries.sqoop.SqoopConfig;
import org.pentaho.di.job.entries.sqoop.SqoopExportJobEntry;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulTree;

import java.util.Collection;

/**
 * Controller for the Sqoop Export Dialog.
 */
public class SqoopExportJobEntryController extends AbstractSqoopJobEntryController {

  public SqoopExportJobEntryController(XulDomContainer container, AbstractSqoopJobEntry sqoopJobEntry, BindingFactory bindingFactory) {
    super(container, sqoopJobEntry, bindingFactory, "sqoop-export");
  }

  @Override
  protected void createBindings(SqoopConfig config, XulDomContainer container, BindingFactory bindingFactory, Collection<Binding> bindings) {
    bindings.add(bindingFactory.createBinding(config, "jobEntryName", "jobEntryName", "value"));

    XulTree variablesTree = (XulTree) container.getDocumentRoot().getElementById("advanced-table");
    bindingFactory.setBindingType(Binding.Type.ONE_WAY);
    bindings.add(bindingFactory.createBinding(config.getArguments(), "children", variablesTree, "elements"));
  }
}
