package org.pentaho.di.core.gui;

public class SpoonFactory {

	private static SpoonInterface spoonInstance;
	
	public static SpoonInterface getInstance() {
		return spoonInstance;
	}
	
	public static void setSpoonInstance(SpoonInterface anInstance) {
		spoonInstance = anInstance;
	}
	
}
