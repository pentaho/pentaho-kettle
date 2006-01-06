package be.ibridge.kettle.core;

import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.StepPlugin;

/**
 * This is a singleton class that contains allocated Fonts, Colors, etc.
 * All colors etc. are allocated once and released once at the end of the program.
 * 
 * @author Matt
 * @since  27/10/2005
 *
 */
public class GUIResource
{
    private static GUIResource guiResource;
    
    private Display display;
    
    // 33 resources
    
    /* * * Colors * * */
    private Color colorBackground;
    private Color colorGraph;
    private Color colorTab;

    private Color colorRed;
    private Color colorGreen;
    private Color colorBlue;
    private Color colorOrange;
    private Color colorYellow;
    private Color colorMagenta;
    private Color colorBlack;
    private Color colorGray;
    private Color colorDarkGray;
    private Color colorLightGray;
    private Color colorDemoGray;
    private Color colorWhite;
    private Color colorDirectory;

    /* * * Fonts * * */
    private Font fontGraph;
    private Font fontNote;
    // private Font fontGrid;
    // private Font fontDefault;
    private Font fontFixed;

    /* * * Images * * */
    private Hashtable imagesSteps;
    private Hashtable imagesStepsSmall;
    private Image     imageHop;
    private Image     imageConnection; 
    private Image     imageBol;
    private Image     imageKettle;
    private Image     imageCredits;
    private Image     imageStart;
    private Image     imageDummy;
    private Image     imageSpoon;

    private GUIResource(Display display)
    {
        this.display = display;
        
        getResources(false);
        
        display.addListener(SWT.Dispose, new Listener()
            {
                public void handleEvent(Event event)
                {
                    dispose();
                }
            }
        );
    }
    
    public static final GUIResource getInstance()
    {
        if (guiResource!=null) return guiResource;
        guiResource = new GUIResource(Props.getInstance().getDisplay());
        return guiResource;
    }
        
    public void reload()
    {
        dispose();
        getResources(true);
    }
    
    private void getResources(boolean reload)
    {
        Props props = Props.getInstance();
        
        if (props.getBackgroundRGB()!=null) colorBackground = new Color(display, props.getBackgroundRGB() );
        if (props.getGraphColorRGB()!=null) colorGraph      = new Color(display, props.getGraphColorRGB() );
        if (props.getTabColorRGB()  !=null) colorTab        = new Color(display, props.getTabColorRGB()   );
        
        colorRed        = new Color(display, 255,   0,   0 );
        colorGreen      = new Color(display,   0, 255,   0 );
        colorBlue       = new Color(display,   0,   0, 255 );
        colorGray       = new Color(display, 100, 100, 100 );
        colorYellow     = new Color(display, 255, 255,   0 );
        colorMagenta    = new Color(display, 255,   0, 255);
        colorOrange     = new Color(display, 255, 165,   0 );

        colorWhite      = new Color(display, 255, 255, 255 );
        colorDemoGray   = new Color(display, 248, 248, 248 );
        colorLightGray  = new Color(display, 225, 225, 225 );
        colorDarkGray   = new Color(display, 100, 100, 100 );
        colorBlack      = new Color(display,   0,   0,   0 );

        colorDirectory  = new Color(display,   0,   0, 255 );
        
        if (props.getGraphFont()   != null) fontGraph   = new Font(display, props.getGraphFont());
        if (props.getNoteFont()    != null) fontNote    = new Font(display, props.getNoteFont());
        // if (props.getGridFont()    != null) fontGrid    = new Font(display, props.getGridFont());
        // if (props.getDefaultFont() != null) fontDefault = new Font(display, props.getDefaultFont());
        if (props.getFixedFont()   != null) fontFixed   = new Font(display, props.getFixedFont());
        
        // Load all images from files...
        if (!reload) loadStepImages();
    }
    
    private void dispose()
    {
        // Colors 
        
        if (colorBackground!=null) colorBackground.dispose();
        if (colorGraph     !=null) colorGraph     .dispose();
        if (colorTab       !=null) colorTab       .dispose();
        
        colorRed      .dispose();
        colorGreen    .dispose();
        colorBlue     .dispose();
        colorGray     .dispose();
        colorYellow   .dispose();
        colorMagenta  .dispose();
        colorOrange   .dispose();

        colorWhite    .dispose();
        colorDemoGray .dispose();
        colorLightGray.dispose();
        colorDarkGray .dispose();
        colorBlack    .dispose();
        colorDirectory.dispose();
        
        // Fonts
        
        if (fontGraph  !=null) fontGraph  .dispose();
        if (fontNote   !=null) fontNote   .dispose();
        // if (fontGrid   !=null) fontGrid   .dispose();
        // if (fontDefault!=null) fontDefault.dispose();
        if (fontFixed  !=null) fontFixed  .dispose();
        
        // Images
        /*
        Enumeration en = imagesStepsSmall.elements();
        while (en.hasMoreElements())
        {
            Image im = (Image) en.nextElement();
            im.dispose();
        }

        en = imagesSteps.elements();
        while (en.hasMoreElements())
        {
            Image im = (Image) en.nextElement();
            im.dispose();
        }
        
        imageHop.dispose();
        imageBol.dispose();
        imageConnection.dispose();
        imageCredits.dispose();
        imageKettle.dispose();
        imageStart.dispose();
        imageDummy.dispose();
        imageSpoon.dispose();
        */
    }
    
    /**
     * Load all step/jobentry images from files. 
     *
     */
    private void loadStepImages()
    {
        imagesSteps      = new Hashtable();
        imagesStepsSmall = new Hashtable();
        LogWriter log = LogWriter.getInstance();

        ////
        //// STEP IMAGES TO LOAD
        ////
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
                    image = new Image(display, getClass().getResourceAsStream(filename));
                }
                catch(Exception e)
                {
                    log.logError("Kettle", "Unable to find required image file ["+(Const.IMAGE_DIRECTORY + filename)+" : "+e.toString());
                    image = new Image(display, Const.ICON_SIZE, Const.ICON_SIZE);
                    GC gc = new GC(image);
                    gc.drawRectangle(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(Const.ICON_SIZE, 0, 0, Const.ICON_SIZE);
                    gc.dispose();
                }
            } 
            else
            {
                String filename = steps[i].getIconFilename();
                try
                {
                    image = new Image(display, filename);
                }
                catch(Exception e)
                {
                    log.logError("Kettle", "Unable to find required image file ["+(Const.IMAGE_DIRECTORY + filename)+" : "+e.toString());
                    image = new Image(display, Const.ICON_SIZE, Const.ICON_SIZE);
                    GC gc = new GC(image);
                    gc.drawRectangle(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(Const.ICON_SIZE, 0, 0, Const.ICON_SIZE);
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

            imagesSteps.put(steps[i].getID(), image);
            imagesStepsSmall.put(steps[i].getID(), small_image);
        }
        
        imageHop         = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "HOP.png"));
        imageConnection  = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "CNC.png"));
        imageBol         = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "BOL.png"));
        imageKettle      = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "kettle_logo.png"));
        imageCredits     = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "credits.png"));
        imageStart       = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "STR.png"));
        imageDummy       = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "DUM.png"));
        imageSpoon       = new Image(display, getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + "spoon32.png"));
    }

    /**
     * @return Returns the colorBackground.
     */
    public Color getColorBackground()
    {
        Color retval = colorBackground;
        if (retval==null) retval = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorBlack.
     */
    public Color getColorBlack()
    {
        Color retval = colorBlack;
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorBlue.
     */
    public Color getColorBlue()
    {
        Color retval = colorBlue;
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorDarkGray.
     */
    public Color getColorDarkGray()
    {
        Color retval = colorDarkGray;
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorDemoGray.
     */
    public Color getColorDemoGray()
    {
        Color retval = colorDemoGray;
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorDirectory.
     */
    public Color getColorDirectory()
    {
        Color retval = colorDirectory;
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorGraph.
     */
    public Color getColorGraph()
    {
        Color retval = colorGraph;
        if (retval==null) retval = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorGray.
     */
    public Color getColorGray()
    {
        Color retval = colorGray;
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorGreen.
     */
    public Color getColorGreen()
    {
        Color retval = colorGreen;
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorLightGray.
     */
    public Color getColorLightGray()
    {
        Color retval = colorLightGray;
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorMagenta.
     */
    public Color getColorMagenta()
    {
        Color retval = colorMagenta;
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorOrange.
     */
    public Color getColorOrange()
    {
        Color retval = colorOrange;
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorRed.
     */
    public Color getColorRed()
    {
        Color retval = colorRed;
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorTab.
     */
    public Color getColorTab()
    {
        Color retval = colorTab;
        if (retval==null) retval = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorWhite.
     */
    public Color getColorWhite()
    {
        Color retval = colorWhite;
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the colorYellow.
     */
    public Color getColorYellow()
    {
        Color retval = colorYellow;
        if (retval.isDisposed()) throw new RuntimeException("Color can not be disposed!!");
        return retval;
    }

    /**
     * @return Returns the display.
     */
    public Display getDisplay()
    {
        return display;
    }

    
    /*
     * @return Returns the fontDefault.
     *
    public Font getFontDefault()
    {
        if (fontDefault==null) return display.getSystemFont();
        return fontDefault;
    }
    */
    
    /**
     * @return Returns the fontFixed.
     */
    public Font getFontFixed()
    {
        if (fontFixed==null) return display.getSystemFont();
        return fontFixed;
    }

    /**
     * @return Returns the fontGraph.
     */
    public Font getFontGraph()
    {
        if (fontGraph==null) return display.getSystemFont();
        return fontGraph;
    }

    /*
     * @return Returns the fontGrid.
     *
    public Font getFontGrid()
    {
        if (fontGrid==null) return display.getSystemFont();
        return fontGrid;
    }
    */

    /**
     * @return Returns the fontNote.
     */
    public Font getFontNote()
    {
        if (fontNote==null) return display.getSystemFont();
        return fontNote;
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
     * @return Returns the imageKettle.
     */
    public Image getImageKettle()
    {
        return imageKettle;
    }

    /**
     * @return Returns the imageSpoon.
     */
    public Image getImageSpoon()
    {
        return imageSpoon;
    }

    /**
     * @return Returns the imagesSteps.
     */
    public Hashtable getImagesSteps()
    {
        return imagesSteps;
    }

    /**
     * @return Returns the imagesStepsSmall.
     */
    public Hashtable getImagesStepsSmall()
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
   
    
}
