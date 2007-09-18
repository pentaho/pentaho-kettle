package org.pentaho.di.trans.step;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;


public interface RowListener
{
	/**
	 * This method is called when a row is read from another step
	 * @param rowMeta the metadata of the row
	 * @param row the data of the row
	 * @throws KettleStepException an exception that can be thrown to hard stop the step
	 */
    public void rowReadEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException;

	/**
	 * This method is called when a row is written to another step (even if there is no next step)
	 * @param rowMeta the metadata of the row
	 * @param row the data of the row
	 * @throws KettleStepException an exception that can be thrown to hard stop the step
	 */
    public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException;

    /**
	 * This method is called when the error handling of a row is writing a row to the error stream.
	 * @param rowMeta the metadata of the row
	 * @param row the data of the row
	 * @throws KettleStepException an exception that can be thrown to hard stop the step
	 */
    public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException;
}
