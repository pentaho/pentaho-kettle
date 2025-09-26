package org.pentaho.di.trans.step.mqtt;

import org.json.simple.JSONObject;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.util.Map;

public class MQTTConsumerHelper extends BaseStepHelper {
  protected static final String MQTT_REFERENCE_PATH = "referencePath";
  private final MQTTConsumerMeta mqttConsumerMeta;

  public MQTTConsumerHelper( MQTTConsumerMeta mqttConsumerMeta ) {
    this.mqttConsumerMeta = mqttConsumerMeta;
  }

  /**
   * Handles step-specific actions for MQTT Consumer step.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta,
                                        Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( method.equalsIgnoreCase( MQTT_REFERENCE_PATH ) ) {
      response = getReferencePath( transMeta );
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }

    return response;
  }

  /**
   * Fetches the reference path of the sub transformation used in the MQTT Consumer step.
   *
   * @return  A JSON object containing:
   * - "referencePath": The full path to the referenced transformation, with environment variables substituted.
   * - "isValidReference": A boolean indicating whether the referenced transformation is valid.
   *
   */
  private JSONObject getReferencePath( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    response.put( MQTT_REFERENCE_PATH, transMeta.environmentSubstitute( mqttConsumerMeta.getTransformationPath() ) );
    return validateAndPutReferenceStatus( transMeta, response, mqttConsumerMeta );
  }
}
