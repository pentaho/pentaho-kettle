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

import ognl.OgnlContext;
import ognl.OgnlException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OgnlExpressionTest {

  public static class Bean {
    private String name = "initial";

    public String getName() {
      return name;
    }

    public void setName( String name ) {
      this.name = name;
    }
  }

  @Test
  public void parsesAndEvaluatesSimplePropertyExpression() throws OgnlException {
    OgnlExpression expr = new OgnlExpression( "name" );
    OgnlContext ctx = new OgnlContext();
    Bean root = new Bean();

    Object value = expr.getValue( ctx, root );

    assertEquals( "initial", value );
  }

  @Test
  public void setsValueThroughExpression() throws OgnlException {
    OgnlExpression expr = new OgnlExpression( "name" );
    OgnlContext ctx = new OgnlContext();
    Bean root = new Bean();

    expr.setValue( ctx, root, "updated" );

    assertEquals( "updated", root.getName() );
  }

  @Test( expected = OgnlException.class )
  public void malformedExpressionFailsToParse() throws OgnlException {
    new OgnlExpression( "name(" );
  }

  @Test( expected = OgnlException.class )
  public void gettingUnknownPropertyThrows() throws OgnlException {
    OgnlExpression expr = new OgnlExpression( "doesNotExist" );
    OgnlContext ctx = new OgnlContext();

    expr.getValue( ctx, new Bean() );
  }
}
