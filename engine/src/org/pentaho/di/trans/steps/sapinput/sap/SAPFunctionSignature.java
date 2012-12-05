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

package org.pentaho.di.trans.steps.sapinput.sap;

import java.util.Collection;
import java.util.Vector;

public class SAPFunctionSignature {
	
	private Collection<SAPField> input = new Vector<SAPField>();
	private Collection<SAPField> output = new Vector<SAPField>();

	@Override
	public String toString() {
		return "SAPFunctionSignature [input=" + input + ", output=" + output
				+ "]";
	}

	public Collection<SAPField> getInput() {
		return input;
	}

	public void setInput(Collection<SAPField> input) {
		this.input = input;
	}

	public void addInput(SAPField input) {
		this.input.add(input);
	}

	public Collection<SAPField> getOutput() {
		return output;
	}

	public void setOutput(Collection<SAPField> output) {
		this.output = output;
	}

	public void addOutput(SAPField output) {
		this.output.add(output);
	}

}
