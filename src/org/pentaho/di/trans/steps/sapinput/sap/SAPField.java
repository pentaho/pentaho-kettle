package org.pentaho.di.trans.steps.sapinput.sap;

public class SAPField {

	private String name;
	private String table;
	private String type;
	private String typePentaho;
	private Object value;

	public SAPField(String name, String table, String type) {
		super();
		this.name = name;
		this.table = table;
		this.type = type;
	}

	public SAPField(String name, String table, String type, Object value) {
		super();
		this.name = name;
		this.table = table;
		this.type = type;
		this.value = value;
	}

	@Override
	public String toString() {
		return "SAPField [name=" + name + ", table=" + table + ", type=" + type
				+ ", typePentaho=" + typePentaho + ", value=" + value + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getTypePentaho() {
		return typePentaho;
	}

	public void setTypePentaho(String typePentaho) {
		this.typePentaho = typePentaho;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
