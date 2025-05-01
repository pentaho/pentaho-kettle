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

import java.util.List;

public interface CommandLineOptionProvider {
  /**
   * Prepare additional commandline option.
   *
   * @param options Existing commandline options
   * @param param   Additional commandline option.
   */
  void prepareAdditionalCommandlineOption( List<CommandLineOption> options, StringBuilder param );

  /**
   * Handle additional parameter
   *
   * @param log
   * @param param Commandline option String value.
   * @return CommandExecutorResult
   * @throws KettleException
   */
  CommandExecutorResult handleParameter( LogChannelInterface log, String param ) throws KettleException;
}
