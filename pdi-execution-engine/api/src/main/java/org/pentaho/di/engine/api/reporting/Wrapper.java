package org.pentaho.di.engine.api.reporting;

import java.util.Optional;

public interface Wrapper {
  <T> Optional<T> unwrap( Class<T> clazz );
}
