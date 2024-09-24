/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.spoon.partition.processor;

import org.pentaho.di.trans.step.StepPartitioningMeta;

/**
 * @author Evgeniy_Lyakhov@epam.com
 */
public class MethodProcessorFactory {

  public static MethodProcessor create( int methodType ) {
    switch ( methodType ) {
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
