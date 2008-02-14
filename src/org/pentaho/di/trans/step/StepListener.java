package org.pentaho.di.trans.step;

import org.pentaho.di.trans.Trans;

public interface StepListener {
	public void stepFinished(Trans trans, StepMeta stepMeta, StepInterface step);
}
