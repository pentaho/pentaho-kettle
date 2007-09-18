package org.pentaho.di.trans.debug;

import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;

public interface BreakPointListener {
	public void breakPointHit(TransDebugMeta transDebugMeta, StepDebugMeta stepDebugMeta, RowMetaInterface rowBufferMeta, List<Object[]> rowBuffer);
}
