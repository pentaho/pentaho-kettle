/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme.css;

import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;


public class AndConditionImpl implements CombinatorCondition, ConditionExt {
  private final Condition first;

  private final Condition second;

  public AndConditionImpl( Condition first, Condition second ) {
    this.first = first;
    this.second = second;
  }

  @Override
  public Condition getFirstCondition() {
    return first;
  }

  @Override
  public Condition getSecondCondition() {
    return second;
  }

  @Override
  public short getConditionType() {
    return SAC_AND_CONDITION;
  }

  @Override
  public int getSpecificity() {
    Specific specificFirst = ( Specific )first;
    Specific specificSecond = ( Specific )second;
    return specificFirst.getSpecificity() + specificSecond.getSpecificity();
  }

  @Override
  public String[] getConstraints() {
    String[] cond1 = ( ( ConditionExt )first ).getConstraints();
    String[] cond2 = ( ( ConditionExt )second ).getConstraints();
    String[] result = new String[ cond1.length + cond2.length ];
    System.arraycopy( cond1, 0, result, 0, cond1.length );
    System.arraycopy( cond2, 0, result, cond1.length, cond2.length );
    return result;
  }

  @Override
  public String toString() {
    return first.toString() + second.toString();
  }

}
