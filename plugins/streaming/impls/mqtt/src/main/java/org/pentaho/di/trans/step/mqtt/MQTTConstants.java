/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step.mqtt;

@SuppressWarnings( "all" )
class MQTTConstants {
  private MQTTConstants() { }

  static final String MQTT_SERVER = "MQTT_SERVER";
  static final String TOPICS = "TOPICS";
  static final String TOPIC = "TOPIC";
  static final String FIELD_TOPIC = "FIELD_TOPIC";
  static final String TOPIC_IN_FIELD = "TOPIC_IN_FIELD";
  static final String MSG_OUTPUT_NAME = "Message";
  static final String TOPIC_OUTPUT_NAME = "Topic name";
  static final String QOS = "QOS";
  static final String SSL_GROUP = "SSL";
  static final String USE_SSL = "USE_SSL";
  static final String SSL_KEYS = "SSL_KEYS";
  static final String SSL_VALUES = "SSL_VALUES";
  static final String USERNAME = "USERNAME";
  static final String PASSWORD = "PASSWORD";
  static final String CLIENT_ID = "CLIENT_ID";
  static final String MESSAGE_FIELD = "MESSAGE_FIELD";
  static final String KEEP_ALIVE_INTERVAL = "KEEP_ALIVE_INTERVAL";
  static final String MAX_INFLIGHT = "MAX_INFLIGHT";
  static final String CONNECTION_TIMEOUT = "CONNECTION_TIMEOUT";
  static final String CLEAN_SESSION = "CLEAN_SESSION";
  static final String STORAGE_LEVEL = "STORAGE_LEVEL";
  static final String SERVER_URIS = "SERVER_URIS";
  static final String MQTT_VERSION = "MQTT_VERSION";
  static final String AUTOMATIC_RECONNECT = "AUTOMATIC_RECONNECT";
}
