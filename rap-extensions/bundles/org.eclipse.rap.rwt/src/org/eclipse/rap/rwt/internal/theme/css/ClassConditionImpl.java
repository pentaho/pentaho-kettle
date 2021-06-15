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


public class ClassConditionImpl implements AttributeCondition, ConditionExt {

  private final String value;

  public ClassConditionImpl( String value ) {
    this.value = value;
  }

  @Override
  public String getLocalName() {
    return null;
  }

  @Override
  public boolean getSpecified() {
    return true;
  }

  @Override
  public String getNamespaceURI() {
    return null;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public short getConditionType() {
    return SAC_CLASS_CONDITION;
  }

  @Override
  public int getSpecificity() {
    return ATTR_SPEC;
  }

  @Override
  public String[] getConstraints() {
    return new String[] { "." + value };
  }

  @Override
  public String toString() {
    return "." + value;
  }

}
