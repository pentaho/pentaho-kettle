package be.ibridge.kettle.trans.step.textfileinput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleException;

public class TextFilePlayListFactoryLineNumber implements
		TextFilePlayListFactory {

	private final Date replayDate;

	private final String encoding;

	private final String directory;

	private final String extension;

	private TextFilePlayList currentPlayList;

	public TextFilePlayListFactoryLineNumber(Date replayDate, String directory,
			String extension, String encoding) {
		this.replayDate = replayDate;
		this.encoding = encoding;
		this.directory = directory;
		this.extension = extension;
	}

	class TextFilePlayList {
		File processingFile;

		public TextFilePlayList(File processingFile) {
			this.processingFile = processingFile;
		}

		File getProcessingFile() {
			return processingFile;
		}

		boolean isProcessingNeeded(long lineNumberInFile) {
			return false;
		}
	}

	class TextFilePlayLineNumber extends TextFilePlayList {
		Set lineNumbers = new HashSet();

		public TextFilePlayLineNumber(File file, String encoding,
				File processingFile) throws KettleException {
			super(processingFile);
			initialize(file, encoding);
		}

		private void initialize(File file, String encoding)
				throws KettleException {
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

	public boolean isProcessingNeeded(TextFileLine textFileLine)
			throws KettleException {
		if (currentPlayList == null
				|| !currentPlayList.getProcessingFile().equals(
						textFileLine.file))
			initializeCurrentPlayList(textFileLine);

		return currentPlayList.isProcessingNeeded(textFileLine.lineNumber);
	}

	private void initializeCurrentPlayList(TextFileLine textFileLine)
			throws KettleException {
		File lineFile = AbstractTextFileErrorHandler.getLineNumberFilename(
				directory, textFileLine.file.getName(), replayDate, extension);
		if (lineFile.exists())
			currentPlayList = new TextFilePlayLineNumber(lineFile, encoding,
					textFileLine.file);
		else
			currentPlayList = new TextFilePlayList(textFileLine.file);
	}
}
