/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.core.gui;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepLoaderException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobPlugin;
import org.pentaho.di.laf.BasePropertyHandler;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.StepPlugin;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.util.ImageUtil;

/**
 * This is a singleton class that contains allocated Fonts, Colors, etc. All
 * colors etc. are allocated once and released once at the end of the program.
 * 
 * @author Matt
 * @since 27/10/2005
 * 
 */
public class GUIResource
{
	private static LogWriter log = LogWriter.getInstance();

	private static GUIResource guiResource;

	private Display display;

	// 33 resources

	/* * * Colors * * */
	private ManagedColor colorBackground;

	private ManagedColor colorGraph;

	private ManagedColor colorTab;

	private ManagedColor colorRed;

	private ManagedColor colorGreen;

	private ManagedColor colorBlue;

	private ManagedColor colorOrange;

	private ManagedColor colorYellow;

	private ManagedColor colorMagenta;

	private ManagedColor colorBlack;

	private ManagedColor colorGray;

	private ManagedColor colorDarkGray;

	private ManagedColor colorLightGray;

	private ManagedColor colorDemoGray;

	private ManagedColor colorWhite;

	private ManagedColor colorDirectory;

	private ManagedColor colorPentaho;

	private ManagedColor colorLightPentaho;

	/* * * Fonts * * */
	private ManagedFont fontGraph;

	private ManagedFont fontNote;

	private ManagedFont fontFixed;

	private ManagedFont fontLarge;

	private ManagedFont fontTiny;

	/* * * Images * * */
	private Map<String, Image> imagesSteps;

	private Map<String, Image> imagesStepsSmall;

	private Map<String, Image> imagesJobentries;

	private Map<String, Image> imagesJobentriesSmall;

	private Image imageHop;

	private Image imageConnection;

	private Image imageKettleLogo;

	private Image imageLogoSmall;

	private Image imageBanner;

	private Image imageBol;

	private Image imageArrow;

	private Image imageCredits;

	private Image imageStart;

	private Image imageDummy;

	private Image imageStartSmall;

	private Image imageDummySmall;

	private Image imageSpoon;

	private Image imageJob;

	private Image imagePentaho;

	private Image imageVariable;

	private Image imageTransGraph;

	private Image imageJobGraph;

	private Image imageEditOptionButton;

	private Image imageResetOptionButton;

	private ManagedFont fontBold;

	/**
	 * GUIResource also contains the clipboard as it has to be allocated only
	 * once! I don't want to put it in a separate singleton just for this one
	 * member.
	 */
	private static Clipboard clipboard;

	private GUIResource(Display display)
	{
		this.display = display;

		getResources(false);

		display.addListener(SWT.Dispose, new Listener()
		{
			public void handleEvent(Event event)
			{
				dispose(false);
			}
		});

		clipboard = null;
	}

	public static final GUIResource getInstance()
	{
		if (guiResource != null)
			return guiResource;
		guiResource = new GUIResource(PropsUI.getDisplay());
		return guiResource;
	}

	public void reload()
	{
		dispose(true);
		getResources(true);
	}

	private void getResources(boolean reload)
	{
		PropsUI props = PropsUI.getInstance();

		colorBackground = new ManagedColor(display, props.getBackgroundRGB());
		colorGraph = new ManagedColor(display, props.getGraphColorRGB());
		colorTab = new ManagedColor(display, props.getTabColorRGB());

		colorRed = new ManagedColor(display, 255, 0, 0);
		colorGreen = new ManagedColor(display, 0, 255, 0);
		colorBlue = new ManagedColor(display, 0, 0, 255);
		colorYellow = new ManagedColor(display, 255, 255, 0);
		colorMagenta = new ManagedColor(display, 255, 0, 255);
		colorOrange = new ManagedColor(display, 255, 165, 0);

		colorWhite = new ManagedColor(display, 255, 255, 255);
		colorDemoGray = new ManagedColor(display, 240, 240, 240);
		colorLightGray = new ManagedColor(display, 225, 225, 225);
		colorGray = new ManagedColor(display, 150, 150, 150);
		colorDarkGray = new ManagedColor(display, 100, 100, 100);
		colorBlack = new ManagedColor(display, 0, 0, 0);

		colorDirectory = new ManagedColor(display, 0, 0, 255);
		// colorPentaho = new ManagedColor(display, 239, 128, 51 ); // Orange
		colorPentaho = new ManagedColor(display, 188, 198, 82);
		colorLightPentaho = new ManagedColor(display, 238, 248, 152);

		// Load all images from files...
		if (!reload)
		{
			loadFonts();
			loadCommonImages();
			loadStepImages();
			loadJobEntryImages();
		}
	}

	private void dispose(boolean reload)
	{
		// Colors
		colorBackground.dispose();
		colorGraph.dispose();
		colorTab.dispose();

		colorRed.dispose();
		colorGreen.dispose();
		colorBlue.dispose();
		colorGray.dispose();
		colorYellow.dispose();
		colorMagenta.dispose();
		colorOrange.dispose();

		colorWhite.dispose();
		colorDemoGray.dispose();
		colorLightGray.dispose();
		colorDarkGray.dispose();
		colorBlack.dispose();

		colorDirectory.dispose();
		colorPentaho.dispose();
		colorLightPentaho.dispose();

		if (!reload) // display shutdown, clean up our mess
		{
			// Fonts
			fontGraph.dispose();
			fontNote.dispose();
			fontFixed.dispose();
			fontLarge.dispose();
			fontTiny.dispose();
			fontBold.dispose();

			// Common images
			imageHop.dispose();
			imageConnection.dispose();
			imageLogoSmall.dispose();
			imageKettleLogo.dispose();
			imageBanner.dispose();
			imageBol.dispose();
			imageArrow.dispose();
			imageCredits.dispose();
			imageStart.dispose();
			imageDummy.dispose();
			imageStartSmall.dispose();
			imageDummySmall.dispose();
			imageSpoon.dispose();
			imageJob.dispose();
			imagePentaho.dispose();
			imageVariable.dispose();
			imageTransGraph.dispose();
			imageJobGraph.dispose();

			disposeImage(imageEditOptionButton);
			disposeImage(imageResetOptionButton);

			// big images
			disposeImages(imagesSteps.values());

			// Small images
			disposeImages(imagesStepsSmall.values());
		}
	}

	private void disposeImages(Collection<Image> c)
	{
		for (Image image : c)
		{
			disposeImage(image);
		}
	}

	private void disposeImage(Image image)
	{
		if (image != null && !image.isDisposed())
			image.dispose();
	}

	/**
	 * Load all step images from files.
	 * 
	 */
	private void loadStepImages()
	{
		imagesSteps = new Hashtable<String, Image>();
		imagesStepsSmall = new Hashtable<String, Image>();

		// //
		// // STEP IMAGES TO LOAD
		// //
		StepLoader steploader = StepLoader.getInstance();
		StepPlugin steps[] = steploader.getStepsWithType(StepPlugin.TYPE_ALL);
		for (int i = 0; i < steps.length; i++)
		{
			Image image = null;
			Image small_image = null;

			if (steps[i].isNative())
			{
				String filename = steps[i].getIconFilename();
				try
				{
					//InputStream stream = getClass().getResourceAsStream(filename);
					image = ImageUtil.getImage(display, filename); // new Image(display, stream);
				} catch (Exception e)
				{
					log.logError("Kettle", "Unable to find required step image file or image format not supported (e.g. interlaced) [" + filename + " : " + e.toString());
					image = new Image(display, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
					GC gc = new GC(image);
					gc.drawRectangle(0, 0, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
					gc.drawLine(0, 0, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
					gc.drawLine(ConstUI.ICON_SIZE, 0, 0, ConstUI.ICON_SIZE);
					gc.dispose();
				}
			} else
			{
				String filename = steps[i].getIconFilename();
				try
				{
					image = new Image(display, filename);
				} catch (Exception e)
				{
					log.logError("Kettle", "Unable to find required step image file [" + filename + " : " + e.toString());
					image = new Image(display, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
					GC gc = new GC(image);
					gc.drawRectangle(0, 0, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
					gc.drawLine(0, 0, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
					gc.drawLine(ConstUI.ICON_SIZE, 0, 0, ConstUI.ICON_SIZE);
					gc.dispose();
				}
			}

			// Calculate the smaller version of the image @ 16x16...
			// Perhaps we should make this configurable?
			//
			if (image != null)
			{
				int xsize = image.getBounds().width;
				int ysize = image.getBounds().height;
				small_image = new Image(display, 16, 16);
				GC gc = new GC(small_image);
				gc.drawImage(image, 0, 0, xsize, ysize, 0, 0, 16, 16);
				gc.dispose();
			}

			imagesSteps.put(steps[i].getID()[0], image);
			imagesStepsSmall.put(steps[i].getID()[0], small_image);
		}
	}

	private void loadFonts()
	{
		PropsUI props = PropsUI.getInstance();

		fontGraph = new ManagedFont(display, props.getGraphFont());
		fontNote = new ManagedFont(display, props.getNoteFont());
		fontFixed = new ManagedFont(display, props.getFixedFont());

		// Create a large version of the graph font
		FontData largeFontData = new FontData(props.getGraphFont().getName(), props.getGraphFont().getHeight() * 3, props.getGraphFont().getStyle());
		fontLarge = new ManagedFont(display, largeFontData);

		// Create a tiny version of the graph font
		FontData tinyFontData = new FontData(props.getGraphFont().getName(), props.getGraphFont().getHeight() -2, props.getGraphFont().getStyle());
		fontTiny = new ManagedFont(display, tinyFontData);

		// Create a bold version of the default font to display shared objects
		// in the trees
		int extraHeigth=0;
		if (Const.isOSX()) extraHeigth=3;
		FontData boldFontData = new FontData( props.getDefaultFont().getName(), props.getDefaultFont().getHeight()+extraHeigth, props.getDefaultFont().getStyle() | SWT.BOLD );
		fontBold = new ManagedFont(display, boldFontData);
	}

	private void loadCommonImages()
	{
		imageHop = ImageUtil.getImageAsResource(display,BasePropertyHandler.getProperty("HOP_image")); // "ui/images/HOP.png"
		imageConnection = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("CNC_image")); // , "ui/images/CNC.png"
		imageKettleLogo = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("Logo_lrg_image")); // , "ui/images/logo_kettle_lrg.png"
		imageBanner = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("Banner_bg_image")); // , "ui/images/bg_banner.png"
		imageBol = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("BOL_image")); // , "ui/images/BOL.png"
		imageCredits = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("Credits_image")); // , "ui/images/credits.png"
		imageStart = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("STR_image")); // , "ui/images/STR.png"
		imageDummy = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("DUM_image")); // , "ui/images/DUM.png"
		imageSpoon = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("spoon_image")); // , "ui/images/spoon32.png"
		imageJob = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("Chef_image")); // , "ui/images/chef.png"
		imagePentaho = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("CorpLogo_image")); // , "ui/images/PentahoLogo.png"
		imageVariable = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("Variable_image")); // , "ui/images/variable.png"
		imageEditOptionButton = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("EditOption_image")); // , "ui/images/edit_option.png"
		imageResetOptionButton = ImageUtil.getImageAsResource(display, BasePropertyHandler.getProperty("ResetOption_image")); // , "ui/images/reset_option.png"

		imageStartSmall = new Image(display, 16, 16);
		GC gc = new GC(imageStartSmall);
		gc.drawImage(imageStart, 0, 0, 32, 32, 0, 0, 16, 16);
		gc.dispose();
		imageDummySmall = new Image(display, 16, 16);
		gc = new GC(imageDummySmall);
		gc.drawImage(imageDummy, 0, 0, 32, 32, 0, 0, 16, 16);
		gc.dispose();

		// Makes transparent images "on the fly"
		//
		imageTransGraph = ImageUtil.makeImageTransparent(display, ImageUtil.getImageAsResource(display,
				BasePropertyHandler.getProperty("SpoonIcon_image")), new RGB(255, 255, 255)); // , "ui/images/spoongraph.png"
		imageJobGraph = ImageUtil.makeImageTransparent(display, ImageUtil.getImageAsResource(display, 
				BasePropertyHandler.getProperty("ChefIcon_image")), // , "ui/images/chefgraph.png"
				new RGB(255, 255, 255));
		imageLogoSmall = ImageUtil.makeImageTransparent(display, ImageUtil.getImageAsResource(display,
				BasePropertyHandler.getProperty("Logo_sml_image")), new RGB(255, 255, 255)); // , "ui/images/kettle_logo_small.png"
		imageArrow = ImageUtil.makeImageTransparent(display, ImageUtil.getImageAsResource(display, 
				BasePropertyHandler.getProperty("ArrowIcon_image")), // , "ui/images/arrow.png"
				new RGB(255, 255, 255));
		imageBanner = ImageUtil.makeImageTransparent(display, ImageUtil.getImageAsResource(display, 
				BasePropertyHandler.getProperty("Banner_bg_image")), // , "ui/images/bg_banner.png"
				new RGB(255, 255, 255));
	}
	
	/**
	 * Load all step images from files.
	 * 
	 */
	private void loadJobEntryImages()
	{
		imagesJobentries = new Hashtable<String, Image>();
		imagesJobentriesSmall = new Hashtable<String, Image>();

		// //
		// // JOB ENTRY IMAGES TO LOAD
		// //
		JobEntryLoader jobEntryLoader = JobEntryLoader.getInstance();
		if (!jobEntryLoader.isInitialized())
			return; // Running in Spoon I guess...

		JobPlugin plugins[] = jobEntryLoader.getJobEntriesWithType(JobPlugin.TYPE_ALL);
		for (int i = 0; i < plugins.length; i++)
		{
			try
			{
				if (jobEntryLoader.getJobEntryClass(plugins[i]).getJobEntryType() == JobEntryType.SPECIAL)
					continue;
			} catch (KettleStepLoaderException e)
			{
				log.logError("Kettle", "Unable to create job entry from plugin [" + plugins[i] + "]", e);
				continue;
			}

			Image image = null;
			Image small_image = null;

			if (plugins[i].isNative())
			{
				String filename = plugins[i].getIconFilename();
				try
				{
					image = ImageUtil.getImage(display, filename);
				} catch (Exception e)
				{
					log.logError("Kettle", "Unable to find required job entry image file [" + filename + "] : " + e.toString());
					image = new Image(display, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
					GC gc = new GC(image);
					gc.drawRectangle(0, 0, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
					gc.drawLine(0, 0, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
					gc.drawLine(ConstUI.ICON_SIZE, 0, 0, ConstUI.ICON_SIZE);
					gc.dispose();
				}
			} else
			{
				String filename = plugins[i].getIconFilename();
				try
				{
					image = new Image(display, filename);
				} catch (Exception e)
				{
					log.logError("Kettle", "Unable to find required job entry image file [" + filename
							+ "] : " + e.toString());
					image = new Image(display, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
					GC gc = new GC(image);
					gc.drawRectangle(0, 0, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
					gc.drawLine(0, 0, ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
					gc.drawLine(ConstUI.ICON_SIZE, 0, 0, ConstUI.ICON_SIZE);
					gc.dispose();
				}
			}

			// Calculate the smaller version of the image @ 16x16...
			// Perhaps we should make this configurable?
			//
			if (image != null)
			{
				int xsize = image.getBounds().width;
				int ysize = image.getBounds().height;
				small_image = new Image(display, 16, 16);
				GC gc = new GC(small_image);
				gc.drawImage(image, 0, 0, xsize, ysize, 0, 0, 16, 16);
				gc.dispose();
			}

			imagesJobentries.put(plugins[i].getID(), image);
			imagesJobentriesSmall.put(plugins[i].getID(), small_image);
		}
	}

	/**
	 * @return Returns the colorBackground.
	 */
	public Color getColorBackground()
	{
		return colorBackground.getColor();
	}

	/**
	 * @return Returns the colorBlack.
	 */
	public Color getColorBlack()
	{
		return colorBlack.getColor();
	}

	/**
	 * @return Returns the colorBlue.
	 */
	public Color getColorBlue()
	{
		return colorBlue.getColor();
	}

	/**
	 * @return Returns the colorDarkGray.
	 */
	public Color getColorDarkGray()
	{
		return colorDarkGray.getColor();
	}

	/**
	 * @return Returns the colorDemoGray.
	 */
	public Color getColorDemoGray()
	{
		return colorDemoGray.getColor();
	}

	/**
	 * @return Returns the colorDirectory.
	 */
	public Color getColorDirectory()
	{
		return colorDirectory.getColor();
	}

	/**
	 * @return Returns the colorGraph.
	 */
	public Color getColorGraph()
	{
		return colorGraph.getColor();
	}

	/**
	 * @return Returns the colorGray.
	 */
	public Color getColorGray()
	{
		return colorGray.getColor();
	}

	/**
	 * @return Returns the colorGreen.
	 */
	public Color getColorGreen()
	{
		return colorGreen.getColor();
	}

	/**
	 * @return Returns the colorLightGray.
	 */
	public Color getColorLightGray()
	{
		return colorLightGray.getColor();
	}

	/**
	 * @return Returns the colorMagenta.
	 */
	public Color getColorMagenta()
	{
		return colorMagenta.getColor();
	}

	/**
	 * @return Returns the colorOrange.
	 */
	public Color getColorOrange()
	{
		return colorOrange.getColor();
	}

	/**
	 * @return Returns the colorRed.
	 */
	public Color getColorRed()
	{
		return colorRed.getColor();
	}

	/**
	 * @return Returns the colorTab.
	 */
	public Color getColorTab()
	{
		return colorTab.getColor();
	}

	/**
	 * @return Returns the colorWhite.
	 */
	public Color getColorWhite()
	{
		return colorWhite.getColor();
	}

	/**
	 * @return Returns the colorYellow.
	 */
	public Color getColorYellow()
	{
		return colorYellow.getColor();
	}

	/**
	 * @return Returns the display.
	 */
	public Display getDisplay()
	{
		return display;
	}

	/**
	 * @return Returns the fontFixed.
	 */
	public Font getFontFixed()
	{
		return fontFixed.getFont();
	}

	/**
	 * @return Returns the fontGraph.
	 */
	public Font getFontGraph()
	{
		return fontGraph.getFont();
	}

	/**
	 * @return Returns the fontNote.
	 */
	public Font getFontNote()
	{
		return fontNote.getFont();
	}

	/**
	 * @return Returns the imageBol.
	 */
	public Image getImageBol()
	{
		return imageBol;
	}

	/**
	 * @return Returns the imageConnection.
	 */
	public Image getImageConnection()
	{
		return imageConnection;
	}

	/**
	 * @return Returns the imageCredits.
	 */
	public Image getImageCredits()
	{
		return imageCredits;
	}

	/**
	 * @return Returns the imageDummy.
	 */
	public Image getImageDummy()
	{
		return imageDummy;
	}

	/**
	 * @return Returns the imageHop.
	 */
	public Image getImageHop()
	{
		return imageHop;
	}

	/**
	 * @return Returns the imageSpoon.
	 */
	public Image getImageSpoon()
	{
		return imageSpoon;
	}

	/**
	 * @return Returns the image Pentaho.
	 */
	public Image getImagePentaho()
	{
		return imagePentaho;
	}

	/**
	 * @return Returns the imagesSteps.
	 */
	public Map<String, Image> getImagesSteps()
	{
		return imagesSteps;
	}

	/**
	 * @return Returns the imagesStepsSmall.
	 */
	public Map<String, Image> getImagesStepsSmall()
	{
		return imagesStepsSmall;
	}

	/**
	 * @return Returns the imageStart.
	 */
	public Image getImageStart()
	{
		return imageStart;
	}

	/**
	 * @return Returns the imagesJobentries.
	 */
	public Map<String, Image> getImagesJobentries()
	{
		return imagesJobentries;
	}

	/**
	 * @param imagesJobentries
	 *            The imagesJobentries to set.
	 */
	public void setImagesJobentries(Hashtable<String, Image> imagesJobentries)
	{
		this.imagesJobentries = imagesJobentries;
	}

	/**
	 * @return Returns the imagesJobentriesSmall.
	 */
	public Map<String, Image> getImagesJobentriesSmall()
	{
		return imagesJobentriesSmall;
	}

	/**
	 * @param imagesJobentriesSmall
	 *            The imagesJobentriesSmall to set.
	 */
	public void setImagesJobentriesSmall(Hashtable<String, Image> imagesJobentriesSmall)
	{
		this.imagesJobentriesSmall = imagesJobentriesSmall;
	}

	/**
	 * @return Returns the imageChef.
	 */
	public Image getImageChef()
	{
		return imageJob;
	}

	/**
	 * @param imageChef
	 *            The imageChef to set.
	 */
	public void setImageChef(Image imageChef)
	{
		this.imageJob = imageChef;
	}

	/**
	 * @return the fontLarge
	 */
	public Font getFontLarge()
	{
		return fontLarge.getFont();
	}

	/**
	 * @return the tiny font 
	 */
	public Font getFontTiny()
	{
		return fontTiny.getFont();
	}

	/**
	 * @return Returns the clipboard.
	 */
	public Clipboard getNewClipboard()
	{
		if (clipboard != null)
		{
			clipboard.dispose();
			clipboard = null;
		}
		clipboard = new Clipboard(display);

		return clipboard;
	}

	public void toClipboard(String cliptext)
	{
		if (cliptext == null)
			return;

		getNewClipboard();
		TextTransfer tran = TextTransfer.getInstance();
		clipboard.setContents(new String[] { cliptext }, new Transfer[] { tran });
	}

	public String fromClipboard()
	{
		getNewClipboard();
		TextTransfer tran = TextTransfer.getInstance();

		return (String) clipboard.getContents(tran);
	}

	public Font getFontBold()
	{
		return fontBold.getFont();
	}

	/**
	 * @return the imageVariable
	 */
	public Image getImageVariable()
	{
		return imageVariable;
	}

	public Image getImageTransGraph()
	{
		return imageTransGraph;
	}

	public Image getImageJobGraph()
	{
		return imageJobGraph;
	}

	public Image getEditOptionButton()
	{
		return imageEditOptionButton;
	}

	public Image getResetOptionButton()
	{
		return imageResetOptionButton;
	}

	/**
	 * @return the imageArrow
	 */
	public Image getImageArrow()
	{
		return imageArrow;
	}

	/**
	 * @param imageArrow
	 *            the imageArrow to set
	 */
	public void setImageArrow(Image imageArrow)
	{
		this.imageArrow = imageArrow;
	}

	/**
	 * @return the imageDummySmall
	 */
	public Image getImageDummySmall()
	{
		return imageDummySmall;
	}

	/**
	 * @param imageDummySmall
	 *            the imageDummySmall to set
	 */
	public void setImageDummySmall(Image imageDummySmall)
	{
		this.imageDummySmall = imageDummySmall;
	}

	/**
	 * @return the imageStartSmall
	 */
	public Image getImageStartSmall()
	{
		return imageStartSmall;
	}

	/**
	 * @param imageStartSmall
	 *            the imageStartSmall to set
	 */
	public void setImageStartSmall(Image imageStartSmall)
	{
		this.imageStartSmall = imageStartSmall;
	}

	/**
	 * @return the imageBanner
	 */
	public Image getImageBanner()
	{
		return imageBanner;
	}

	/**
	 * @param imageBanner
	 *            the imageBanner to set
	 */
	public void setImageBanner(Image imageBanner)
	{
		this.imageBanner = imageBanner;
	}

	/**
	 * @return the imageKettleLogo
	 */
	public Image getImageKettleLogo()
	{
		return imageKettleLogo;
	}

	/**
	 * @param imageKettleLogo
	 *            the imageKettleLogo to set
	 */
	public void setImageKettleLogo(Image imageKettleLogo)
	{
		this.imageKettleLogo = imageKettleLogo;
	}

	/**
	 * @return the colorPentaho
	 */
	public Color getColorPentaho()
	{
		return colorPentaho.getColor();
	}

	/**
	 * @return the imageLogoSmall
	 */
	public Image getImageLogoSmall()
	{
		return imageLogoSmall;
	}

	/**
	 * @param imageLogoSmall
	 *            the imageLogoSmall to set
	 */
	public void setImageLogoSmall(Image imageLogoSmall)
	{
		this.imageLogoSmall = imageLogoSmall;
	}

	/**
	 * @return the colorLightPentaho
	 */
	public Color getColorLightPentaho()
	{
		return colorLightPentaho.getColor();
	}

	public void drawPentahoGradient(Display display, GC gc, Rectangle rect, boolean vertical)
	{
		if (!vertical)
		{
			gc.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			gc.setBackground(GUIResource.getInstance().getColorPentaho());
			gc.fillGradientRectangle(rect.x, rect.y, 2 * rect.width / 3, rect.height, vertical);
			gc.setForeground(GUIResource.getInstance().getColorPentaho());
			gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			gc.fillGradientRectangle(rect.x + 2 * rect.width / 3, rect.y, rect.width / 3 + 1, rect.height,
					vertical);
		} else
		{
			gc.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			gc.setBackground(GUIResource.getInstance().getColorPentaho());
			gc.fillGradientRectangle(rect.x, rect.y, rect.width, 2 * rect.height / 3, vertical);
			gc.setForeground(GUIResource.getInstance().getColorPentaho());
			gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			gc.fillGradientRectangle(rect.x, rect.y + 2 * rect.height / 3, rect.width, rect.height / 3 + 1,
					vertical);
		}
	}

	/**
	 * Generic popup with a toggle option
	 * 
	 * @param dialogTitle
	 * @param image
	 * @param message
	 * @param dialogImageType
	 * @param buttonLabels
	 * @param defaultIndex
	 * @param toggleMessage
	 * @param toggleState
	 * @return
	 */
	public Object[] messageDialogWithToggle(Shell shell, String dialogTitle, Image image, String message,
			int dialogImageType, String buttonLabels[], int defaultIndex, String toggleMessage,
			boolean toggleState)
	{
		int imageType = 0;
		switch (dialogImageType)
		{
		case Const.WARNING:
			imageType = MessageDialog.WARNING;
			break;
		}

		MessageDialogWithToggle md = new MessageDialogWithToggle(shell, dialogTitle, image, message,
				imageType, buttonLabels, defaultIndex, toggleMessage, toggleState);
		int idx = md.open();
		return new Object[] { Integer.valueOf(idx), Boolean.valueOf(md.getToggleState()) };
	}

	public static Point calculateControlPosition(Control control) {
		// Calculate the exact location...
		//
		Rectangle r = control.getBounds();
		Point p = control.getParent().toDisplay(r.x, r.y);
		
		return p;
		
		/*
		Point location = control.getLocation();
		
		Composite parent = control.getParent();
		while (parent!=null) {
			
			Composite newParent = parent.getParent();
			if (newParent!=null) {
				location.x+=parent.getLocation().x;
				location.y+=parent.getLocation().y;
			}
			else {
				if (parent instanceof Shell) {
					// Top level shell.
					Shell shell = (Shell)parent;
					Rectangle bounds = shell.getBounds();
					Rectangle clientArea = shell.getClientArea();
					location.x += bounds.width-clientArea.width;
					location.y += bounds.height-clientArea.height;
				}
			}
			parent = newParent;
		}
		
		return location;
		*/
	}
}
