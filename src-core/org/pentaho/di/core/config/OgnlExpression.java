/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
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
public class OgnlExpression
{
    private Object       expression;

    public OgnlExpression(String expressionString) throws OgnlException
    {
        expression = Ognl.parseExpression(expressionString);
    }

    public Object getExpression()
    {
        return expression;
    }

    public Object getValue(OgnlContext context, Object rootObject) throws OgnlException
    {
        return Ognl.getValue(expression, context, rootObject);
    }

    public void setValue(OgnlContext context, Object rootObject, Object value) throws OgnlException
    {
        Ognl.setValue(expression, context, rootObject, value);
    }
}
