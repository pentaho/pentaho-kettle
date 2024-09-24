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

package org.pentaho.di.trans.steps.webservices;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.steps.webservices.wsdl.XsdType;

public class WebServiceField implements Cloneable {
  private String name;

  private String wsName;

  private String xsdType;

  public WebServiceField clone() {
    try {
      return (WebServiceField) super.clone();
    } catch ( CloneNotSupportedException e ) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String toString() {

    return name != null ? name : super.toString();
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getWsName() {
    return wsName;
  }

  public void setWsName( String wsName ) {
    this.wsName = wsName;
  }

  public String getXsdType() {
    return xsdType;
  }

  public void setXsdType( String xsdType ) {
    this.xsdType = xsdType;
  }

  public int getType() {
    return XsdType.xsdTypeToKettleType( xsdType );
  }

  /**
   * We consider a field to be complex if it's a type we don't recognize. In that case, we will give back XML as a
   * string.
   *
   * @return
   */
  public boolean isComplex() {
    return getType() == ValueMetaInterface.TYPE_NONE;
  }
}
