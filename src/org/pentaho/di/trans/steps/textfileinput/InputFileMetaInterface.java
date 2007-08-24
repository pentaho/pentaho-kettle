package org.pentaho.di.trans.steps.textfileinput;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.StepMetaInterface;

public interface InputFileMetaInterface extends StepMetaInterface {

	public TextFileInputField[] getInputFields();

	public int getFileFormatTypeNr();
	
	public boolean hasHeader();
	
	public int getNrHeaderLines();
	
	public String[] getFilePaths(VariableSpace space);

	public boolean isErrorIgnored();
	public String getErrorCountField();
	public String getErrorFieldsField();
	public String getErrorTextField();
	public String getFileType();
	public String getEnclosure();
 	public String getEscapeCharacter();
	public String getSeparator();
	public boolean isErrorLineSkipped();
	public boolean includeFilename();
	public boolean includeRowNumber();
}
