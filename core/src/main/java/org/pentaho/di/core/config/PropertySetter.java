/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.HashMap;
import java.util.Map;

import ognl.OgnlContext;
import ognl.OgnlException;

import org.apache.commons.beanutils.BeanUtils;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Helper class that allows properties to be set based on predefined prefixes, such as ognl:.
 *
 * @author Alex Silva
 *
 */
public class PropertySetter {

  public static final String OGNL = "ognl";
  public static final String I18N = "i18n";

  private Map<String, OgnlExpression> ognlExpressions = new HashMap<>();

  private OgnlContext octx = new OgnlContext();

  // this should not be a static/factory method in order to allow caching of
  // compiled ognl expressions
  public void setProperty( Object obj, String property, String value ) throws KettleConfigException {
    String[] expression = value.split( ":" );
    Object val;

    if ( expression.length == 0 ) {
      throw new KettleConfigException( "No value found for property ["
        + property + "] and obbject class [" + obj.getClass().getName() + "]" );
    }

    String directive = expression[0];

    if ( I18N.equalsIgnoreCase( directive ) ) {
      val = setI18nProperty( expression, value );
    } else if ( OGNL.equalsIgnoreCase( directive ) ) {
      val = setOgnlProperty( expression, value );
    } else {
      val = value;
    }

    try {
      // SET!
      BeanUtils.setProperty( obj, property, val );
    } catch ( Exception e ) {
      throw new KettleConfigException( e );
    }

  }

  private Object setI18nProperty( String[] expression, String value ) throws KettleConfigException {
    if ( expression.length == 3 ) {
      String packageName = expression[1];
      String key = expression[2];
      return BaseMessages.getString( packageName, key );
    } else {
      throw new KettleConfigException(
        "the i18, directive need 3 parameters: i18n, the package name and the key, but "
          + expression.length + " parameters were found in [" + value + "]" );
    }
  }

  private Object setOgnlProperty( String[] expression, String value ) throws KettleConfigException {
    if ( expression.length >= 2 ) {
      OgnlExpression expr = ognlExpressions.get( value );
      if ( expr == null ) {
        synchronized ( ognlExpressions ) {
          try {
            expr = new OgnlExpression( expression[1] );
            ognlExpressions.put( value, expr );

          } catch ( OgnlException e ) {
            throw new KettleConfigException( "Unable to parse expression [" + expression[1] + "] with Ognl", e );
          }
        }
      }

      // evaluate
      try {
        return expr.getValue( octx, this );
      } catch ( OgnlException e ) {
        throw new KettleConfigException(
          "Unable to get value for expression [" + expression[1] + "] with Ognl", e );
      }
    } else {
      throw new KettleConfigException(
        "the ognl, directive need at least 2 parameters: ongl and the expression but "
          + expression.length + " parameters were found in [" + value + "]" );
    }
  }
}
