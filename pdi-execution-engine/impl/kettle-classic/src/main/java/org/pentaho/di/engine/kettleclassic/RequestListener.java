package org.pentaho.di.engine.kettleclassic;

import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.model.LogicalModelElement;
import org.pentaho.di.engine.api.model.ModelElement;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.remote.ExecutionRequest;
import org.pentaho.di.engine.api.remote.Notification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by nbaker on 1/31/17.
 */
public class RequestListener {
  ClassicKettleEngine engine = new ClassicKettleEngine();
  Map<String, LogicalModelElement> modelElementMap = new HashMap<>(  );

  public void requestAdded( ExecutionRequest request ) {

    // Claim it
    request.update( new Notification( "unknown", Notification.Type.CLAIM ) );


    Transformation transformation = request.getTransformation();

    // populate map of ID to LogicalModelElement
    modelElementMap.put( transformation.getId(), transformation );
    modelElementMap.putAll( transformation.getOperations().stream().collect( Collectors.toMap( ModelElement::getId, Function.identity() )));
    modelElementMap.putAll( transformation.getHops().stream().collect( Collectors.toMap( ModelElement::getId, Function.identity() )));

    // Get a context
    ClassicKettleExecutionContext context = (ClassicKettleExecutionContext) engine.prepare(
      transformation );

    // TODO: We need setters for these
    context.getEnvironment().putAll( request.getEnvironment() );
    context.getParameters().putAll( request.getParameters() );

    // Wire Topics from the request to Subscriptions that then report back thru the ExecutionRequest
    Map<String, Set<Class<? extends Serializable>>> reportingTopics = request.getReportingTopics();
    reportingTopics.entrySet().forEach( stringSetEntry -> {
      LogicalModelElement element = modelElementMap.get( stringSetEntry.getKey() );
      stringSetEntry.getValue().forEach( type -> createSubscription( element, type, context, request ) );
    });

    // Fire off the transformation
    context.execute();
  }

  private <S extends LogicalModelElement, D extends Serializable> void createSubscription( S element, Class<D> type, ClassicKettleExecutionContext context, ExecutionRequest request ) {
    context.subscribe( element, type, t -> request.update( element.getId(), t ) );
  }

  public void requestRemoved( ExecutionRequest request ) {
    // TODO: Implement cancel
  }
}
