/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
