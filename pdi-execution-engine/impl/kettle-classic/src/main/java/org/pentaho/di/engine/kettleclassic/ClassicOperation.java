package org.pentaho.di.engine.kettleclassic;

import com.google.common.collect.ImmutableMap;
import org.pentaho.di.engine.api.IHop;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.IOperationVisitor;
import org.pentaho.di.trans.step.StepMeta;

import java.util.List;
import java.util.Map;

/**
 * Created by nbaker on 1/6/17.
 */
public class ClassicOperation implements IOperation {
  private final StepMeta meta;
  private ClassicTransformation transformation;

  public ClassicOperation( StepMeta meta ){
    this.meta = meta;
  }

  @Override public String getId() {
    return meta.getName();
  }

  @Override public List<IOperation> getFrom() {
    return null;
  }

  @Override public List<IOperation> getTo() {
    return null;
  }

  @Override public List<IHop> getHopsIn() {
    return null;
  }

  @Override public List<IHop> getHopsOut() {
    return null;
  }

  @Override public Map<String, Object> getConfig() {
    return ImmutableMap.of();
  }

  @Override public <T> T accept( IOperationVisitor<T> visitor ) {
    return null;
  }

  public void setTransformation( ClassicTransformation transformation ) {
    this.transformation = transformation;
  }
}
