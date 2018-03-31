/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.StepMeta;

import java.util.List;

import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MQTT_VERSION;

public class MqttOption {
  private static Class<?> PKG = MqttOption.class;

  private final String key;
  private final String text;
  private String value;

  public MqttOption( String key, String text, String value ) {
    this.key = key;
    this.text = text;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getText() {
    return text;
  }

  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }

  public static void checkInteger( List<CheckResultInterface> remarks, StepMeta stepMeta, VariableSpace space,
                                   String identifier,
                                   String value ) {
    try {
      if ( !StringUtil.isEmpty( space.environmentSubstitute( value ) ) ) {
        Long.parseLong( space.environmentSubstitute( value ) );
      }
    } catch ( NumberFormatException e ) {
      remarks.add( new CheckResult(
        CheckResultInterface.TYPE_RESULT_ERROR,
        BaseMessages
          .getString( PKG, "MQTTMeta.CheckResult.NotANumber",
            BaseMessages.getString( PKG, "MQTTDialog.Options." + identifier ) ),
        stepMeta ) );
    }
  }

  public static void checkBoolean( List<CheckResultInterface> remarks, StepMeta stepMeta, VariableSpace space,
                                   String identifier,
                                   String value ) {
    if ( !StringUtil.isEmpty( space.environmentSubstitute( value ) ) && null == BooleanUtils
      .toBooleanObject( space.environmentSubstitute( value ) ) ) {
      remarks.add( new CheckResult(
        CheckResultInterface.TYPE_RESULT_ERROR,
        BaseMessages.getString( PKG, "MQTTMeta.CheckResult.NotABoolean",
          BaseMessages.getString( PKG, "MQTTDialog.Options." + identifier ) ),
        stepMeta ) );
    }
  }

  public static void checkVersion( List<CheckResultInterface> remarks, StepMeta stepMeta, VariableSpace space,
                                   String value ) {
    String version = space.environmentSubstitute( value );
    if ( !StringUtil.isEmpty( version ) ) {
      try {
        ( new MqttConnectOptions() ).setMqttVersion( Integer.parseInt( version ) );
      } catch ( Exception e ) {
        remarks.add( new CheckResult(
          CheckResultInterface.TYPE_RESULT_ERROR,
          BaseMessages.getString( PKG, "MQTTMeta.CheckResult.NotCorrectVersion",
            BaseMessages.getString( PKG, "MQTTDialog.Options." + MQTT_VERSION ), version ),
          stepMeta ) );
      }
    }
  }
}
