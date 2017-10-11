/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.config;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

/**
 * Utility class to encapsulate OGNL expressions.
 *
 * @author Alex Silva
 *
 */
public class OgnlExpression {
  private Object expression;

  public OgnlExpression( String expressionString ) throws OgnlException {
    expression = Ognl.parseExpression( expressionString );
  }

  public Object getExpression() {
    return expression;
  }

  public Object getValue( OgnlContext context, Object rootObject ) throws OgnlException {
    return Ognl.getValue( expression, context, rootObject );
  }

  public void setValue( OgnlContext context, Object rootObject, Object value ) throws OgnlException {
    Ognl.setValue( expression, context, rootObject, value );
  }
}
