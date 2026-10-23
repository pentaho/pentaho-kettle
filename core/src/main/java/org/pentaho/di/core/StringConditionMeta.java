package org.pentaho.di.core;

/**
 * Contract for step metadata that stores its rule as a single Java String expression.
 *
 * <p>This is intentionally narrower than StepMetaInterface so steps with a textual condition can expose a common
 * API without forcing all step metadata implementations to share the same condition model.</p>
 */
public interface StringConditionMeta {
  String getCondition();
}
