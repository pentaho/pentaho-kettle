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

import org.w3c.css.sac.AttributeCondition;


public class NullAttributeCondition implements AttributeCondition, ConditionExt
{

  @Override
  public String getLocalName() {
    return null;
  }

  @Override
  public String getNamespaceURI() {
    return null;
  }

  @Override
  public boolean getSpecified() {
    return false;
  }

  @Override
  public String getValue() {
    return null;
  }

  @Override
  public short getConditionType() {
    return SAC_ATTRIBUTE_CONDITION;
  }

  @Override
  public int getSpecificity() {
    return 0;
  }

  @Override
  public String[] getConstraints() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "null condition";
  }

}
