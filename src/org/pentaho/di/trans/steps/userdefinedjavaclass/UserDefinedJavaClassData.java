/***** BEGIN LICENSE BLOCK *****
The contents of this package are subject to the GNU Lesser Public License
 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at
http://www.gnu.org/licenses/lgpl-2.1.txt

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the
License.

The Original Code is Kettle User Defined Java Class Step

The Initial Developer of the Original Code is
Daniel Einspanjer deinspanjer@mozilla.com
Portions created by the Initial Developer are Copyright (C) 2009
the Initial Developer. All Rights Reserved.

Contributor(s):
Matt Casters mcaster@pentaho.com

***** END LICENSE BLOCK *****/

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


public class UserDefinedJavaClassData extends BaseStepData implements StepDataInterface
{
    public RowMetaInterface inputRowMeta;
    public RowMetaInterface outputRowMeta;
	public Map<String, String>	parameterMap;
	public Map<String, String>	infoMap;
	public Map<String, String>	targetMap;
	
	public UserDefinedJavaClassData()
	{
		super();
	}
}
