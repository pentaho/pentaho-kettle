package org.pentaho.di.ui.spoon.partition.processor;

import org.pentaho.di.trans.step.StepPartitioningMeta;

/**
 * @author Evgeniy_Lyakhov@epam.com
 */
public class MethodProcessorFactory {

  public static MethodProcessor create( int methodType ) {
    switch( methodType ) {
      case StepPartitioningMeta.PARTITIONING_METHOD_NONE:
        return new NoneMethodProcessor();
      case StepPartitioningMeta.PARTITIONING_METHOD_MIRROR:
        return new MirrorMethodProcessor();
      case StepPartitioningMeta.PARTITIONING_METHOD_SPECIAL:
        return new SpecialMethodProcessor();
      default:
        return new NoneMethodProcessor();
    }

  }
}
