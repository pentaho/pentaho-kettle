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


public class AttributeConditionImpl implements AttributeCondition, ConditionExt {

  private final String localName;
  private final String value;
  private final boolean specified;

  public AttributeConditionImpl( String localName, String value, boolean specified ) {
    this.localName = localName;
    this.value = value;
    this.specified = specified;
  }

  @Override
  public String getLocalName() {
    return localName;
  }

  @Override
  public String getNamespaceURI() {
    return null;
  }

  @Override
  public boolean getSpecified() {
    return specified;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public short getConditionType() {
    return SAC_ATTRIBUTE_CONDITION;
  }

  @Override
  public int getSpecificity() {
    return ATTR_SPEC;
  }

  @Override
  public String[] getConstraints() {
    if( value != null || localName == null ) {
      throw new UnsupportedOperationException();
    }
    return new String[] { "[" + localName };
  }

  @Override
  public String toString() {
    String result;
    if( value != null ) {
      result = "[" + localName + "=\"" + value + "\"]";
    } else {
      result = "[" + localName + "]";
    }
    return result;
  }

}
