/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.datafactory;

/**
 * Interface for retrieving resources needed to establish a new datasource in the report engine. This allows plugins to
 * provide the necessary resources to render any Kettle input step as a native datasource in Report Designer.
 *
 * @author gmoran
 *
 */
public interface DynamicDatasource {

  /**
   * What class will represent the edit dialog for this datasource?
   *
   * @return fully qualified class name of dialog class; must implement <code>BaseStepGenericXulDialog</code> @
   */
  public String getDialogClass();

  /**
   * Return the name of the Kettle transformation template that holds the uninitialized input step. The template should
   * reside in the archive next to the implementation of the DynamicDatasource interface.
   *
   * @return template name as String
   */
  public String getTemplate();

  /**
   * The name of the resource to embed the transformation as.
   *
   * @return resource name as String
   */
  public String getResourceEntryName();

  /**
   * The name of the step that represents the datasource, as it appears in the Kettle transformation template.
   *
   * @return the step name as String
   */
  public String getStepName();

  /**
   * The name of the query as it appears in the UI of the receiver module. TODO: this should be localizable.
   *
   * @return the query name as String
   */
  public String getQueryName();

}
