package org.pentaho.di.core.config;

import java.util.HashMap;
import java.util.Map;

import ognl.OgnlContext;
import ognl.OgnlException;

import org.apache.commons.beanutils.BeanUtils;
import org.pentaho.di.core.exception.KettleConfigException;

/**
 * Helper class that allows properties to be set based on predefined prefixes, such as ognl:.
 * 
 * @author Alex Silva
 *
 */
public class PropertySetter
{
	//for later maybe; when we have a centralized message repository.
	public static final String MESSAGE = "message";
	public static final String OGNL = "ognl";
	
	private Map<String, OgnlExpression> ognl = new HashMap<String, OgnlExpression>();	

	private OgnlContext octx = new OgnlContext();

	// this should not be a static/factory method in order to allow caching of
	// compiled ognl expressions
	public void setProperty(Object obj, String property, String value) throws KettleConfigException
	{
		String[] expression = value.split(":");
		Object val = value;

		if (expression.length >= 2)
		{
			String directive = expression[0];
			if (directive.equalsIgnoreCase(MESSAGE))
			{
				// set as a string message
				//TODO:IMPLEMENT

			} else if (directive.equals(OGNL))
			{
				OgnlExpression expr = ognl.get(value);
				if (expr == null)
				{
					synchronized (ognl)
					{
						try
						{
							ognl.put(value, expr = new OgnlExpression(expression[1]));

						} catch (OgnlException e)
						{
							throw new KettleConfigException(e);
						}
					}

					// evaluate
					try
					{
						val = expr.getValue(octx, this);
					} catch (OgnlException e)
					{
						throw new KettleConfigException(e);
					}
				}
			}
		}
		
		try
		{
			//SET!
			BeanUtils.setProperty(obj, property, val);
		} catch (Exception e)
		{
			throw new KettleConfigException(e);
		}

	}
}
