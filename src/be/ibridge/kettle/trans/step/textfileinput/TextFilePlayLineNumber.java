package be.ibridge.kettle.trans.step.textfileinput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleException;

public class TextFilePlayLineNumber implements TextFilePlayer {
	Set lineNumbers = new HashSet();

	public TextFilePlayLineNumber(File file, String encoding)
			throws KettleException {
		initialize(file, encoding);
	}

	private void initialize(File file, String encoding) throws KettleException {
		BufferedReader reader = null;
		try {
			if (encoding == null)
				reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(file)));
			else
				reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(file), encoding));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0)
					lineNumbers.add(Long.valueOf(line));
			}
		} catch (Exception e) {
			throw new KettleException("Could not read line number file "
					+ file.getAbsolutePath(), e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					LogWriter.getInstance().logBasic(
							"TextFilePlayLineNumber",
							"Could not close line number file "
									+ file.getAbsolutePath());
				}
		}
	}

	public boolean isProcessingNeeded(long lineNumberInFile) {
		return lineNumbers.contains(new Long(lineNumberInFile));
	}
}
