package org.pentaho.di.trans.steps.excelinput.poisax;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.openxml4j.opc.Package;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class SheetLister implements HSSFListener {
  
  private SSTRecord sstrec;
  
  private List<SheetInfo> sheets;

  public SheetLister(String filename) throws KettleException  {
    InputStream inputStream = null;
    InputStream din = null;
    
    sheets = new ArrayList<SheetInfo>();
    
    try {
      
      Package pkg = Package.open(filename);
      XSSFReader reader = new XSSFReader(pkg);
      SharedStringsTable sst = reader.getSharedStringsTable();
      XMLReader parser = fetchSheetParser(sst);
      
      Iterator<InputStream> sheets = reader.getSheetsData();
      while(sheets.hasNext()) {
        System.out.println("Processing new sheet:\n");
        InputStream sheet = sheets.next();
        InputSource sheetSource = new InputSource(sheet);
        parser.parse(sheetSource);
        sheet.close();
        System.out.println("");
      }
      
      inputStream = KettleVFS.getInputStream(filename);
      XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
      POIFSFileSystem poifs = new POIFSFileSystem(inputStream);
      
      // get the Workbook (excel part) stream in a InputStream
      din = poifs.createDocumentInputStream("Workbook");
      
      // construct out HSSFRequest object
      HSSFRequest req = new HSSFRequest();
      
      req.addListenerForAllRecords(this);
      
      // create our event factory
      HSSFEventFactory factory = new HSSFEventFactory();
      
      // process our events based on the document input stream
      factory.processEvents(req, din);
            
    } catch(Exception e) {
      throw new KettleException(e);
    } finally {
      try {
        // once all the events are processed close our file input stream
        if (inputStream!=null) {
          inputStream.close();
        }
      } catch(Exception e) {
        throw new KettleException(e);
      } finally {
        try {
          // don't leak the data input stream either.       
          if (din!=null) {
            din.close();
          }
        } catch(Exception e) {
          throw new KettleException(e);
        }
      }
    }
  }
  
  public XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException {
    XMLReader parser =
      XMLReaderFactory.createXMLReader(
          "org.apache.xerces.parsers.SAXParser"
      );
    ContentHandler handler = new SheetHandler(sst);
    parser.setContentHandler(handler);
    return parser;
  }
  
  /** 
   * See org.xml.sax.helpers.DefaultHandler javadocs 
   */
  private static class SheetHandler extends DefaultHandler {
    private SharedStringsTable sst;
    private String lastContents;
    private boolean nextIsString;
    
    private SheetHandler(SharedStringsTable sst) {
      this.sst = sst;
    }
    
    public void startElement(String uri, String localName, String name,
        Attributes attributes) throws SAXException {
      // c => cell
      if(name.equals("c")) {
        // Print the cell reference
        System.out.print(attributes.getValue("r") + " - ");
        // Figure out if the value is an index in the SST
        String cellType = attributes.getValue("t");
        if(cellType != null && cellType.equals("s")) {
          nextIsString = true;
        } else {
          nextIsString = false;
        }
      }
      // Clear contents cache
      lastContents = "";
    }
    
    public void endElement(String uri, String localName, String name)
        throws SAXException {
      // Process the last contents as required.
      // Do now, as characters() may be called more than once
      if (nextIsString) {
        int idx = Integer.parseInt(lastContents);
        lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
        nextIsString = false;
      }

      // v => contents of a cell
      // Output after we've seen the string contents
      if(name.equals("v")) {
        System.out.println(lastContents);
      }
    }

    public void characters(char[] ch, int start, int length)
        throws SAXException {
      lastContents += new String(ch, start, length);
    }
  }
  
  /**
   * @return the sheets
   */
  public List<SheetInfo> getSheets() {
    return sheets;
  }

  @Override
  public void processRecord(Record arg0) {
    // TODO Auto-generated method stub
    
  }

}
