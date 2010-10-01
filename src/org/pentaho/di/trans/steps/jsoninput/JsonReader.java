package org.pentaho.di.trans.steps.jsoninput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.jsonpath.JsonJar;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;

public class JsonReader {
	private static Class<?> PKG = JsonInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final String JAVA_SCRIPT = "JavaScript"; 
	private static final String JSON_SCRIPT = "json.js"; 
	private static final String JSON_PATH_SCRIPT = "jsonpath.js"; 
	private static final String EVAL_FALSE = "false"; 
	private static final String EVAL = "var obj=";
	private static final String JSON_PATH = "jsonPath";
	
	private ScriptEngine jsEngine;
	
	private boolean ignoreMissingPath;

	public JsonReader() throws KettleException {
		init();
		this.ignoreMissingPath=false;
	}
	
	public void SetIgnoreMissingPath(boolean value) {
		this.ignoreMissingPath=value;
	}
	private void init() throws KettleException {
		
		try {
			
			ScriptEngineManager sm = new ScriptEngineManager();
			setEngine(sm.getEngineByName(JAVA_SCRIPT));
			if (getEngine() == null) {
				throw new KettleException(BaseMessages.getString(PKG, "JsonReader.Error.NoScriptEngineFound"));
			}
			
			// Load Json
			loadJsonScript(JSON_SCRIPT);
			
			// Load JsonPath
			loadJsonScript(JSON_PATH_SCRIPT);
			
		}catch(Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "JsonReader.Error.EngineInit", e.getMessage()), e);
		}
	}
	private void loadJsonScript(String script) throws Exception {
		InputStream is=null;
		InputStreamReader isr=null;
		try {
			is = JsonJar.class.getResource(script).openStream();
			isr = new InputStreamReader(is);
			getEngine().eval(new BufferedReader(isr));
		}finally {
			try {
				if(is!=null) is.close();
				if(isr!=null) isr.close();
			}catch(Exception e){};
		}
	}
	private ScriptEngine getEngine() {
		return jsEngine;
	}
	private void setEngine(ScriptEngine script) {
		jsEngine= script;
	}
	private Invocable getInvocable() {
		return (Invocable) getEngine();
	}
	public void readFile(String filename) throws KettleException {
		FileReader fr=null;
		try {
			fr = new FileReader(filename);
			Object o = JSONValue.parse(fr);
			if(o==null) {
				throw new KettleException(BaseMessages.getString(PKG, "JsonReader.Error.ParsingFile", filename));
			}
			eval(o);
		}catch(Exception e) {
			throw new KettleException(e);
		}finally {
			try {
				if(fr!=null) fr.close();
			}catch(Exception e){}
		}
	}
	public void readString(String value)  throws KettleException {
		try {
			Object o= JSONValue.parse(value);
			if(o==null) {
				throw new KettleException(BaseMessages.getString(PKG, "JsonReader.Error.ParsingString", value));
			}
			eval(o);
		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	public void readUrl(String value)  throws KettleException {
		InputStreamReader is=null;
		try {
			URL url = new URL(value);
			is = new InputStreamReader(url.openConnection().getInputStream());
			Object o = JSONValue.parse(is);
			if(o==null) {
				throw new KettleException(BaseMessages.getString(PKG, "JsonReader.Error.ParsingUrl", value));
			}
			eval(o);
		}catch(Exception e) {
			throw new KettleException(e);
		}finally {
			try {
				if(is!=null) is.close();
			}catch(Exception e){}
		}
	}
	private void eval(Object o) throws Exception {
		getEngine().eval(EVAL + o.toString());
	}
	public NJSONArray getPath(String value) throws KettleException{
		try {
			String ro = getInvocable().invokeFunction(JSON_PATH, value).toString();
			if(!ro.equals(EVAL_FALSE)) {
				
				NJSONArray ra = new NJSONArray((JSONArray) JSONValue.parse(ro));
				return ra;
			} else {
				if(!isIgnoreMissingPath()) throw new KettleException(BaseMessages.getString(PKG, "JsonReader.Error.CanNotFindPath", value));
			}
		}catch(Exception e) {
			throw new KettleException(e);
		}
		
		// The Json Path is missing
		// and user do not want to fail
		// so we need to populate it with NULL values
		NJSONArray ja= new NJSONArray();
		ja.setNull(true);
	
		return ja;
	}
	
	public boolean isIgnoreMissingPath() {
		return this.ignoreMissingPath;
	}
	
}