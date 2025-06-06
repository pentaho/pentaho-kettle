/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.trans.ael.websocket.handler;

import org.pentaho.di.engine.api.remote.Message;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventHandlerExecutionException;

import java.io.Serializable;

/**
 * Created by fcamara on 8/17/17.
 */
public interface MessageEventHandler extends Serializable {
  /**
   * Performs the action corresponding to the given Message
   */
  void execute( Message message ) throws MessageEventHandlerExecutionException;

  /**
   * Returns a unique identifier for each instance of the Handler
   */
  String getIdentifier();
}
