/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans;

import java.util.List;

import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.step.StepMeta;
import org.w3c.dom.Node;

/*
 * Created on 19-jun-2003
 * 
 */

/**
 * Defines a link between 2 steps in a transformation
 */
public class TransHopMeta implements Cloneable, XMLInterface, Comparable<TransHopMeta>
{
	private static Class<?> PKG = Trans.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String XML_TAG = "hop";

	private StepMeta from_step;

	private StepMeta to_step;

	private boolean enabled;

	public boolean split = false;

	private boolean changed;

	private ObjectId id;

	public TransHopMeta(StepMeta from, StepMeta to, boolean en)
	{
		from_step = from;
		to_step = to;
		enabled = en;
	}

	public TransHopMeta(StepMeta from, StepMeta to)
	{
		from_step = from;
		to_step = to;
		enabled = true;
	}

	public TransHopMeta()
	{
		this(null, null, false);
	}

	public TransHopMeta(Node hopnode, List<StepMeta> steps) throws KettleXMLException
	{
		try
		{
			from_step = searchStep(steps, XMLHandler.getTagValue(hopnode, "from")); //$NON-NLS-1$
			to_step = searchStep(steps, XMLHandler.getTagValue(hopnode, "to")); //$NON-NLS-1$
			String en = XMLHandler.getTagValue(hopnode, "enabled"); //$NON-NLS-1$

			if (en == null)
				enabled = true;
			else
				enabled = en.equalsIgnoreCase("Y"); //$NON-NLS-1$
		} catch (Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "TransHopMeta.Exception.UnableToLoadHopInfo"), e); //$NON-NLS-1$
		}
	}

	public void setFromStep(StepMeta from)
	{
		from_step = from;
	}

	public void setToStep(StepMeta to)
	{
		to_step = to;
	}

	public StepMeta getFromStep()
	{
		return from_step;
	}

	public StepMeta getToStep()
	{
		return to_step;
	}

	private StepMeta searchStep(List<StepMeta> steps, String name)
	{
		for (StepMeta stepMeta : steps)
			if (stepMeta.getName().equalsIgnoreCase(name))
				return stepMeta;

		return null;
	}

	public Object clone()
	{
		try
		{
			Object retval = super.clone();
			return retval;
		} catch (CloneNotSupportedException e)
		{
			return null;
		}
	}

	public boolean equals(Object obj)
	{
		TransHopMeta other = (TransHopMeta) obj;
		if (from_step == null || to_step == null)
			return false;
		return from_step.equals(other.getFromStep()) && to_step.equals(other.getToStep());
	}

	/**
	 * Compare 2 hops.
	 */
	public int compareTo(TransHopMeta obj)
	{
		return toString().compareTo(obj.toString());
	}

	public ObjectId getObjectId()
	{
		return id;
	}

	public void setObjectId(ObjectId id)
	{
		this.id = id;
	}

	public void setChanged()
	{
		setChanged(true);
	}

	public void setChanged(boolean ch)
	{
		changed = ch;
	}

	public boolean hasChanged()
	{
		return changed;
	}

	public void setEnabled()
	{
		setEnabled(true);
	}

	public void setEnabled(boolean en)
	{
		enabled = en;
		setChanged();
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void flip()
	{
		StepMeta dummy = from_step;
		from_step = to_step;
		to_step = dummy;
	}

	public String toString()
	{
		String str_fr = (from_step == null) ? "(empty)" : from_step.getName(); //$NON-NLS-1$
		String str_to = (to_step == null) ? "(empty)" : to_step.getName(); //$NON-NLS-1$
		return str_fr + " --> " + str_to + " (" + (enabled ? "enabled" : "disabled") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	public String getXML()
	{
		StringBuilder retval = new StringBuilder(200);

		if (from_step != null && to_step != null)
		{
			retval.append("  <hop> "); //$NON-NLS-1$
			retval.append(XMLHandler.addTagValue("from", from_step.getName(), false)); //$NON-NLS-1$
			retval.append(XMLHandler.addTagValue("to", to_step.getName(), false)); //$NON-NLS-1$
			retval.append(XMLHandler.addTagValue("enabled", enabled, false)); //$NON-NLS-1$
			retval.append(" </hop>"); //$NON-NLS-1$
		}

		return retval.toString();
	}
}
