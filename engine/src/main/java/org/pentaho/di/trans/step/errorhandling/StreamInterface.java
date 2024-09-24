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

package org.pentaho.di.trans.step.errorhandling;

import org.pentaho.di.trans.step.StepMeta;

public interface StreamInterface {

  public enum StreamType {
    INPUT, OUTPUT, INFO, TARGET, ERROR,
  }

  public String getStepname();

  public void setStepMeta( StepMeta stepMeta );

  public StepMeta getStepMeta();

  public StreamType getStreamType();

  public void setStreamType( StreamType streamType );

  public String getDescription();

  public void setDescription( String description );

  public StreamIcon getStreamIcon();

  public void setStreamIcon( StreamIcon streamIcon );

  public void setSubject( Object subject );

  public Object getSubject();
}
