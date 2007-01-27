 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.test;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/*
 * Created on 15-apr-04
 *
 */

public class TextFileImporter 
{
	private String filename;
	private int nrrows;
	private int margin;
	
	public TextFileImporter(String filename, int nrrows, int margin)
	{
		this.filename = filename;
		this.nrrows   = nrrows;
		this.margin   = margin;
	}
	
	public Vector guessLayout()
	{
		Vector retval = new Vector();
		
		// First determine the maximum line length.
		
		try
		{
			int maxlength = 0;
			int linenr = 1;
			
			// First pass...
			FileReader fr = new FileReader(filename);
			String line = readLine(fr);
			while (line!=null && line.length()>0 && linenr<nrrows)
			{
				if (line.length()>maxlength) maxlength=line.length();
				linenr++;
				line = readLine(fr);
			}
			fr.close();
			
			System.out.println("Max. length = "+maxlength);
			
			Hashtable positions[] = new Hashtable[maxlength];
			for (int i=0;i<maxlength;i++)
			{
				positions[i] = new Hashtable();
			}
		
			// Second pass: get statistics...			
			fr = new FileReader(filename);
			line = readLine(fr);
			linenr=1;
			while (line!=null && line.length()>0 && linenr<nrrows)
			{
				for (int i=0;i<line.length();i++)
				{
					char ch = line.charAt(i);
					if (ch!=' ')
					{
						Character chr = new Character(line.charAt(i));
						Integer occ = (Integer)positions[i].get(chr);
						if (occ==null) occ=new Integer(1); else occ=new Integer(occ.intValue()+1);
						
						positions[i].put(chr, occ);
					}
				}

				line = readLine(fr);
				linenr++;
			}
			fr.close();
 
 			// What have we learned?
 			// What's the minimal nr of occurences?
 			int min = 999;
 			for (int i=0;i<maxlength;i++)
 			{
 				Hashtable pos = positions[i];
 				int size = nrentries(pos);
 				if (min>size && size>0) min=size;
 			}
 			
 			System.out.println("Minimal nr of occurances : "+min);
 			
			boolean hadpeak = false;
			for (int i=0;i<maxlength;i++)
			{
				Hashtable pos = positions[i];
				int size = nrentries(pos);
				boolean valey=false;
				
				if (size<=min+margin && hadpeak && size>0)
				{ 
				     valey=true; 
				     hadpeak=false;
				}
				else 
				{
					if (size>min+margin)
					{
						hadpeak=true;
					} 
				}
				
				if (valey) 
				{
					System.out.print("x");
					retval.add(new Integer(i));
				} 
				else 
				{
					System.out.print(" ");
				} 
			}
			System.out.println();
			
		}
		catch(IOException e)
		{
			
		}
		
		return retval;
	}
	
	private int nrentries(Hashtable h)
	{
		Enumeration keys = h.keys();
		int k=0;
		while (keys.hasMoreElements()) 
		{
			k++;
			keys.nextElement();
		}
		return k;
	}
	
	private String readLine(FileReader fr)
	{
		String retval="";
		boolean eoln=false;
		try
		{
			int ch = fr.read();
			// Any left over from previous line?  Skip it.
			if (ch=='\n' || ch=='\r') ch = fr.read();
			 
			while (ch>=0 && !eoln)
			{
				if (ch=='\n' || ch=='\r')
				{
					eoln=true;
				}
				else
				{
					retval+=(char)ch;
				}
				ch=fr.read();
			}
		}
		catch(IOException e)
		{
			retval=null;
		}
		
		return retval;
	}
}
