/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

// [PPP-6549] Regression coverage added ahead of the CVE-2016-3093 ognl:ognl bump
// (2.6.9 -> 3.0.21). Hardened on the vulnerable baseline first per the local
// remediation test-first discipline: these must be green before, and stay green
// after, the dependency bump.

package org.pentaho.di.core.config;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleConfigException;

import static org.junit.Assert.assertEquals;

public class PropertySetterTest {

  public static class TargetBean {
    private String value;

    public String getValue() {
      return value;
    }

    public void setValue( String value ) {
      this.value = value;
    }
  }

  @Test
  public void setsLiteralValueWhenNoDirectivePresent() throws KettleConfigException {
    PropertySetter setter = new PropertySetter();
    TargetBean bean = new TargetBean();

    setter.setProperty( bean, "value", "plainValue" );

    assertEquals( "plainValue", bean.getValue() );
  }

  @Test
  public void setsValueUsingOgnlDirective() throws KettleConfigException {
    PropertySetter setter = new PropertySetter();
    TargetBean bean = new TargetBean();

    setter.setProperty( bean, "value", "ognl:1+1" );

    assertEquals( "2", bean.getValue() );
  }

  @Test( expected = KettleConfigException.class )
  public void ognlDirectiveWithoutExpressionThrows() throws KettleConfigException {
    PropertySetter setter = new PropertySetter();
    TargetBean bean = new TargetBean();

    setter.setProperty( bean, "value", "ognl" );
  }

  @Test( expected = KettleConfigException.class )
  public void malformedOgnlExpressionThrowsWrappedException() throws KettleConfigException {
    PropertySetter setter = new PropertySetter();
    TargetBean bean = new TargetBean();

    setter.setProperty( bean, "value", "ognl:(1+1" );
  }
}
