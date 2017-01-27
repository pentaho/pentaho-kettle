package org.pentaho.di.engine.api;


import org.pentaho.di.engine.api.model.Transformation;

import java.io.Serializable;

/**
 * An Engine is responsible for executing an ITransformation.
 */
public interface Engine {
  ExecutionContext prepare( Transformation trans );
}
