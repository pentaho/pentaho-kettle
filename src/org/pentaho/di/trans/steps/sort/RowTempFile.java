package org.pentaho.di.trans.steps.sort;
    /**
     * Keeps track of which temporary file a row is coming from
     */
	public class RowTempFile {
		public Object[] row;
		public int fileNumber;
		public RowTempFile(Object[] row, int fileNumber) {
			this.row = row;
			this.fileNumber=fileNumber;
		}
	}