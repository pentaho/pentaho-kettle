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

import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.SimpleSelector;


public class ConditionalSelectorImpl implements ConditionalSelector, SelectorExt {

  private final SimpleSelector selector;
  private final Condition condition;

  public ConditionalSelectorImpl( SimpleSelector selector, Condition condition ) {
    this.selector = selector;
    this.condition = condition;
  }

  @Override
  public Condition getCondition() {
    return condition;
  }

  @Override
  public SimpleSelector getSimpleSelector() {
    return selector;
  }

  @Override
  public short getSelectorType() {
    return SAC_CONDITIONAL_SELECTOR;
  }

  @Override
  public int getSpecificity() {
    Specific specificSelector = ( Specific )selector;
    Specific specificCondition = ( Specific )condition;
    return specificSelector.getSpecificity()
           + specificCondition.getSpecificity();
  }

  @Override
  public String getElementName() {
    return ( ( SelectorExt )selector ).getElementName();
  }

  @Override
  public String[] getConstraints() {
    return ( ( ConditionExt )condition ).getConstraints();
  }

  @Override
  public String toString() {
    return selector.toString() + condition.toString();
  }

}
