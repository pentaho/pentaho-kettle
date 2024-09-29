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


package org.pentaho.di.core.logging;

import java.util.List;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepMeta;
import org.w3c.dom.Node;

public interface LogTableInterface extends LogTableCoreInterface {
 /**
   * @return The log table meta-data in XML format.
   */
  public String getXML();

  /**
   * Load the information for this logging table from the job XML node
   *
   * @param jobnode
   *          the node to load from
   * @param databases
   *          the list of database to reference.
   * @param steps
   *          the steps to reference (or null)
   */
  public void loadXML( Node jobnode, List<DatabaseMeta> databases, List<StepMeta> steps );

  /**
   * Generate DDL necessary to create the log table or alter the existing table to the present specification
   *
   * @param logTable
   * @param transMeta
   * @return The ddl that will perform the task
   * @throws KettleException
   */
  default StringBuilder generateTableSQL( LogTableInterface logTable, AbstractMeta transMeta ) throws
    KettleException {
    throw new UnsupportedOperationException(
      "The " + logTable.getLogTableType() + " does not support the generation of table creation DDL" );
  }

}
