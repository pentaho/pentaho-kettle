/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.ui.core.dialog.EnterPrintDialog;



/**
 * This class handles printing for Kettle.
 * 
 * @author Matt
 * @since 28-03-2004
 *
 */
public class PrintSpool 
{
	private PrinterData printerdata;
	private Printer printer;
	private PaletteData palette;
		
	public PrintSpool()
	{
		printerdata = Printer.getDefaultPrinterData();
		if ( printerdata != null )
		{
			// Fail silently instead of crashing.
		    printer = new Printer(printerdata);
		}
	}
	
	public PrinterData getPrinterData()
	{
		return printerdata;
	}
	
	// Ask which printer to use...
		
	public Printer getPrinter(Shell sh)
	{
		PrintDialog pd = new PrintDialog(sh);
		printerdata = pd.open();
		if (printerdata!=null)
		{
			if (printer!=null) printer.dispose();
			printer = new Printer(printerdata);
		}
		
		return printer;
	}	
	
	public void dispose()
	{
		if (printer!=null) printer.dispose();
	}
	
	public int getDepth()
	{
		return printer.getDepth();
	}
	
	public PaletteData getPaletteData()
	{
		switch(getDepth())
		{
			case 1  : 
				palette = new PaletteData(new RGB[] { new RGB(0,0,0), new RGB(255,255,255)} );
				break;
			default :
				palette = new PaletteData(0,0,0);
				palette.isDirect = true;
				break;
		}
		
		return palette;
	}
	
	public void printImage(Shell sh, Image img)
	{
		if (printerdata!=null)
		{
			Rectangle imgbounds = img.getBounds();
			Point max = new Point(imgbounds.width, imgbounds.height);
			
			// What's the printers DPI?
			Point dpi_printer = printer.getDPI();
				
			// What's the screens DPI?
			Point dpi_screen = Display.getCurrent().getDPI();
			
			// Resize on printer: calculate factor:
			double factorx = (double)dpi_printer.x / (double)dpi_screen.x;
			double factory = (double)dpi_printer.y / (double)dpi_screen.y;
				
			// Get size of 1 page?
			Rectangle page = printer.getBounds();
			
			double margin_left   = 0.40; // 0,40 inch about 1cm
			double margin_right  = 0.40; 
			double margin_top    = 0.40;
			double margin_bottom = 0.40; 

			EnterPrintDialog epd = new EnterPrintDialog(sh,
				1, 1, 100, factorx, factory, page,
				margin_left, margin_right, margin_top, margin_bottom, 
				img
				);
			if (epd.open()==SWT.OK)
			{
				double page_left      = epd.leftMargin*dpi_printer.x;
				double page_right     = epd.rightMargin *dpi_printer.x;
				double page_top       = epd.topMargin *dpi_printer.y;
				double page_bottom    = epd.bottomMargin*dpi_printer.y;
				double page_sizex     = page.width - page_left - page_right;
				double page_sizey     = page.height- page_top  - page_bottom;
				
				double size_on_paperx = max.x * factorx;
				double size_on_papery = max.y * factory;
				double actual_sizex   = size_on_paperx * epd.scale / 100;
				double actual_sizey   = size_on_papery * epd.scale / 100;

				// Create new print job.
				printer.startJob("Kettle : Spoon print job");

				// How much of the image do we print on each page: all or just a page worth of pixels?
				
				for (int c=0;c<epd.nrcols;c++)
				{
					double left_to_printx = actual_sizex - page_sizex * c; 
					double printx = ( left_to_printx > page_sizex ) ? page_sizex : ( left_to_printx>=0 ? left_to_printx : 0 ); 
					 
					for (int r=0;r<epd.nrrows;r++)
					{
						double left_to_printy = actual_sizey - page_sizey * r; 
						double printy = ( left_to_printy > page_sizey ) ? page_sizey: ( left_to_printy>=0 ? left_to_printy : 0 ); 

						int startx = (int)(actual_sizex - left_to_printx);
						int starty = (int)(actual_sizey - left_to_printy);
						
						int fromx  = (int)(startx/(factorx*epd.scale/100));
						int fromy  = (int)(starty/(factory*epd.scale/100));
						int imx  = (int)(max.x * printx/actual_sizex) -1; 
						int imy  = (int)(max.y * printy/actual_sizey) -1; 
						
						printer.startPage();
						GC gc_printer = new GC(printer);
						
						gc_printer.drawImage(img, fromx, fromy, imx, imy, 
						     (int)page_left, (int)page_top, (int)printx, (int)printy 
						     );
						
						//ShowImageDialog sid = new ShowImageDialog(sh, props, img);
						//sid.open();
						
						System.out.println("img dept = "+img.getImageData().depth);
						System.out.println("prn dept = "+printer.getDepth());
						System.out.println("img size = ("+img.getBounds().x+","+img.getBounds().y+") : ("+img.getBounds().width+","+img.getBounds().height+")");
						System.out.println("fromx="+fromx+", fromy="+fromy+", imx="+imx+", imy="+imy+", page_left="+(int)page_left+", page_top="+(int)page_top+", printx="+(int)printx+", printy="+(int)printy);
						
						printer.endPage();
						gc_printer.dispose();
					}
				}

				printer.endJob();
				printer.dispose();
			}
			img.dispose();
		}
	}

}
