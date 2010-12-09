//
// Google Analytics Plugin for Pentaho PDI a.k.a. Kettle
// 
// Copyright (C) 2010 Slawomir Chodnicki
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

package org.pentaho.di.trans.steps.googleanalytics;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.DataFeed;

public class GaInputStepData extends BaseStepData implements StepDataInterface {

	public RowMetaInterface outputRowMeta;
	
	// meta info for a string conversion 
	public ValueMetaInterface[] conversionMeta;
	
	// holds currently processed feed
	public DataQuery query;
	public DataFeed feed;
	public int entryIndex;
	
    public GaInputStepData()
	{
		super();
	}
}
	
