package org.pentaho.di.core.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.vfs.KettleVFS;

public class SwingGUIResource {
	private static SwingGUIResource instance;
	
	private Map<String, Image>	stepImages;
	private Map<String, Image>	entryImages;
	
	private SwingGUIResource() throws KettleException {
		this.stepImages = loadStepImages();
		this.entryImages = loadEntryImages();
	}
	
	public static SwingGUIResource getInstance() throws KettleException {
		if (instance==null) {
			instance=new SwingGUIResource();
		}
		return instance;
	}
	
	private Map<String, Image> loadStepImages() throws KettleException {
		Map<String, Image> map = new HashMap<String, Image>();
		
		for (PluginInterface plugin: PluginRegistry.getInstance().getPlugins(StepPluginType.class)) {
			Image image = getImageIcon(plugin);
			for (String id : plugin.getIds()) {
				map.put(id, image);
			}
		}
		
		return map;
	}
	
	private Map<String, Image> loadEntryImages() throws KettleException {
		Map<String, Image> map = new HashMap<String, Image>();
		
		for (PluginInterface plugin : PluginRegistry.getInstance().getPlugins(JobEntryPluginType.class)) {
			if ("SPECIAL".equals(plugin.getIds()[0])) {
				continue;
			}
			
			String imageFile = plugin.getImageFile();
			if (imageFile==null) {
				throw new KettleException("No image file (icon) specified for plugin: "+plugin);
			}
			Image image = getImageIcon(imageFile);
			if (image==null) {
				throw new KettleException("Unable to find image file: "+plugin.getImageFile()+" for plugin: "+plugin);
			}
			if (image.getHeight(null)<0) {
				image = getImageIcon(plugin);
				if (image==null || image.getHeight(null)<0) {
					throw new KettleException("Unable to load image file: "+plugin.getImageFile()+" for plugin: "+plugin);
				}
			}
			
			map.put(plugin.getIds()[0], image);
		}
		
		return map;
	}
	
	private Image getImageIcon(String fileName) throws KettleException {
		try {
			ImageIcon imageIcon = new javax.swing.ImageIcon("/"+fileName);
			return imageIcon.getImage();
		} catch(Throwable e) {
			throw new KettleException("Unable to load image from file : '"+fileName+"'", e);
		}
	}

	private Image getImageIcon(PluginInterface plugin) throws KettleException {
		try {
			PluginRegistry registry = PluginRegistry.getInstance();
			Object object = registry.loadClass(plugin);
			InputStream inputStream = object.getClass().getResourceAsStream(plugin.getImageFile());
			if (inputStream==null) {
				inputStream = object.getClass().getResourceAsStream("/"+plugin.getImageFile());
			}
			if (inputStream==null) {
				inputStream = registry.getClass().getResourceAsStream(plugin.getImageFile());
			}
			if (inputStream==null) {
				inputStream = registry.getClass().getResourceAsStream("/"+plugin.getImageFile());
			}
			try {
				inputStream = new FileInputStream(plugin.getImageFile());
			} catch(FileNotFoundException e) {
				// Ignore, throws error below
			}
			
			if (inputStream==null) {
				throw new KettleException("Unable to find file: "+plugin.getImageFile()+" for plugin: "+plugin);
			}
			BufferedImage image = ImageIO.read(inputStream);
			inputStream.close();
			return image;
		} catch(Throwable e) {
			throw new KettleException("Unable to load image from file : '"+plugin.getImageFile()+"' for plugin: "+plugin, e);
		}
	}
	
	public Map<String, Image> getEntryImages() {
		return entryImages;
	}
	
	public Map<String, Image> getStepImages() {
		return stepImages;
	}
}
