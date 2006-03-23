/**
 * 
 */
package be.ibridge.kettle.trans.step.playlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleException;

class TextFilePlayListReplayLineNumberFile extends TextFilePlayListReplayFile {
	Set lineNumbers = new HashSet();

	public TextFilePlayListReplayLineNumberFile(File lineNumberFile,
			String encoding, File processingFile) throws KettleException {
		super(processingFile);
		initialize(lineNumberFile, encoding);
	}

	private void initialize(File lineNumberFile, String encoding)
			throws KettleException {
		BufferedReader reader = null;
		try {
			if (encoding == null)
				reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(lineNumberFile)));
			else
				reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(lineNumberFile), encoding));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0)
					lineNumbers.add(Long.valueOf(line));
			}
		} catch (Exception e) {
			throw new KettleException("Could not read line number file "
					+ lineNumberFile.getAbsolutePath(), e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					LogWriter.getInstance().logBasic(
							"TextFilePlayLineNumber",
							"Could not close line number file "
									+ lineNumberFile.getAbsolutePath());
				}
		}
	}

	public boolean isProcessingNeeded(File file, long lineNr)
			throws KettleException {
		return lineNumbers.contains(new Long(lineNr));
	}
}