package org.pentaho.di.engine.api;


import org.pentaho.di.engine.api.model.ITransformation;

import java.io.Serializable;

/**
 * An IEngine is responsible for executing an ITransformation.
 */
public interface IEngine {
  IExecutionContext prepare( ITransformation trans );
}
