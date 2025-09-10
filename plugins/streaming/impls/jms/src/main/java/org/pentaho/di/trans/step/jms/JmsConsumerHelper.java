package org.pentaho.di.trans.step.jms;

import org.json.simple.JSONObject;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.util.Map;

public class JmsConsumerHelper extends BaseStepHelper {
  private final JmsConsumerMeta jmsConsumerMeta;

  public JmsConsumerHelper( JmsConsumerMeta jmsConsumerMeta ) {
    this.jmsConsumerMeta = jmsConsumerMeta;
  }

  /**
   * Handles step-specific actions for JMSConsumer step.
   */
  @Override
  protected JSONObject handleStepAction(String method, TransMeta transMeta,
                                        Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( method.equalsIgnoreCase( REFERENCE_PATH ) ) {
      response = getReferencePath( transMeta );
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }

    return response;
  }

  /**
   * Fetches the reference path of the sub transformation used in the jmsConsumer step.
   *
   * @return  A JSON object containing:
   * - "referencePath": The full path to the referenced transformation, with environment variables substituted.
   * - "isTransReference": A boolean indicating that this is a transformation reference.
   *
   */
  private JSONObject getReferencePath( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    response.put( REFERENCE_PATH, transMeta.environmentSubstitute( jmsConsumerMeta.getTransformationPath() ) );
    response.put( IS_TRANS_REFERENCE, true );
    return response;
  }
}
