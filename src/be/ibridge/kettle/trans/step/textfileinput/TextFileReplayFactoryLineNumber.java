package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;
import java.util.Date;

import be.ibridge.kettle.core.exception.KettleException;

public class TextFileReplayFactoryLineNumber implements TextFileReplayFactory {

	private final Date replayDate;

	private final String encoding;

	private final String directory;

	private final String extension;

	public TextFileReplayFactoryLineNumber(Date replayDate,
			String directory, String extension, String encoding) {
		this.replayDate = replayDate;
		this.encoding = encoding;
		this.directory = directory;
		this.extension = extension;
	}

	public TextFilePlayer createPlayer(File file) throws KettleException {
		File lineFile = AbstractTextFileLineErrorHandler.getLineNumberFilename(
				directory, file.getName(), replayDate, extension);
		if (!lineFile.exists())
			return TextFilePlayerNone.INSTANCE;
		return new TextFilePlayLineNumber(lineFile, encoding);
	}
}
