package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextFileList {
	private List files = new ArrayList();

	private List nonExistantFiles = new ArrayList(1);

	private List nonAccessibleFiles = new ArrayList(1);

	public List getFiles() {
		return files;
	}

	public List getNonAccessibleFiles() {
		return nonAccessibleFiles;
	}

	public List getNonExistantFiles() {
		return nonExistantFiles;
	}

	void addFile(File file) {
		files.add(file);
	}

	void addNonAccessibleFile(File file) {
		nonAccessibleFiles.add(file);
	}

	void addNonExistantFile(File file) {
		nonExistantFiles.add(file);
	}

	public void sortFiles() {
		Collections.sort(files);
		Collections.sort(nonAccessibleFiles);
		Collections.sort(nonExistantFiles);
	}

	public File getFile(int i) {
		return (File) files.get(i);
	}

	public int nrOfFiles() {
		return files.size();
	}
}
