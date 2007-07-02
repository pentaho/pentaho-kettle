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
        return Ognl.getValue(getExpression(), context, rootObject);
    }

    public void setValue(OgnlContext context, Object rootObject, Object value) throws OgnlException
    {
        Ognl.setValue(getExpression(), context, rootObject, value);
    }
}
