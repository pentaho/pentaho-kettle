package org.pentaho.di.trans.steps.userdefinedjavaclass;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta.FieldInfo;

public class CodeSnippit {
	private final String name;
	private final String code;
	private final String description;
	private final List<FieldInfo> addedFields;

	public CodeSnippit(String name, String code,
			String description, List<FieldInfo> addedFields) {
		super();
		this.name = name;
		this.code = code;
		this.description = description;
		this.addedFields = addedFields;
	}

	public String getName() {
		return name;
	}
	
	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
	
	public ListIterator<FieldInfo> getAddedFields() {
		return Collections.unmodifiableList(addedFields).listIterator();
	}
}
