/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.core.lifecycle;

import java.util.HashSet;
import java.util.Set;

public class LifeEventInfo
{
	public enum Hint
	{
		DISPLAY_MSG_BOX, DISPLAY_BROWSER;
	};
	
	public enum State
	{
		SUCCESS,FAIL,HALTED;
	};

	private String message;
	
	private String name;

	private Set<Hint> hints = new HashSet<Hint>();
	
	private State state;

	public void setHint(Hint hint)
	{
		hints.add(hint);
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public boolean hasHint(Hint h)
	{
		for (Hint hint : hints)
			if (hint == h)
				return true;


		return false;
	}

	public State getState()
	{
		return state;
	}

	public void setState(State state)
	{
		this.state = state;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
