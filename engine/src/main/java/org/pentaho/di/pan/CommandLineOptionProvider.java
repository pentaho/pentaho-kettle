/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.pan;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.repository.Repository;

import java.util.Map;

public interface CommandLineOptionProvider {

  /**
   * Return the additional commandline option in the form of namedParams
   *
   * @param log LogChannelInterface object for logging information
   * @return NamedParam  NameParam containing the name and description of the commandline options
   */
  NamedParams getAdditionalCommandlineOptions( LogChannelInterface log );

  /**
   * Validate the parameter value and set the project context
   * @param log
   * @param params Map containing the param/value
   * @param repository Repository object in case we are connected to repository
   * @return CommandExecutorResult Returns the return code which is zero in case of success and non-zero code in case of error
   * @throws KettleException
   */
  CommandExecutorResult handleParameter( LogChannelInterface log, Map<String, String> params, Repository repository ) throws KettleException;
}
