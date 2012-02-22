grammar FastSimpleGenericEdifactDirectXML;

options {
    output=template;
}

@header {
package org.pentaho.di.trans.steps.edi2xml.grammar;
import org.apache.commons.lang.StringEscapeUtils;
import java.util.LinkedList;
}

@lexer::header {
package org.pentaho.di.trans.steps.edi2xml.grammar;
}


@members {

	public static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	public static final String TAG_EDIFACT = "<edifact>\n";
	public static final String TAG_EDIFACT_END = "</edifact>";
	public static final String TAG_ELEMENT = "\t\t<element>\n";
	public static final String TAG_ELEMENT_END = "\t\t</element>\n";	
	public static final String TAG_VALUE = "\t\t\t<value>";	
	public static final String TAG_VALUE_END = "</value>\n";
		
	public LinkedList tagIndexes = new LinkedList();
		
	// helper functions to sanitize incoming input
	public String sanitizeText(String txt){

		// resolve all RELEASE characters 
		if (txt.indexOf("?") >= 0){
			txt = txt.replace((CharSequence)"?+","+");
			txt = txt.replace((CharSequence)"?:",":");
			txt = txt.replace((CharSequence)"?'","'");		
			txt = txt.replace((CharSequence)"??","?");		
		}
		
		// enocde XML entities
		return StringEscapeUtils.escapeXml(txt);
	}
	
	// assume about 8k for an edifact message
	public StringBuilder buf = new StringBuilder(8192);

	// helper method for writing tag indexes to the stream
	public void appendIndexes(){

		if (tagIndexes.size() == 0) return;

		//System.out.println(tagIndexes);
		for(Object i : tagIndexes){
			String s = (String) i;
			buf.append("\t\t<index>"+s+"</index>\n");
		}
	}
	
	// error handling overrides -> just exit
	protected void mismatch(IntStream input, int ttype, BitSet follow) throws RecognitionException {
		throw new MismatchedTokenException(ttype, input);
	}

	public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
		throw e;
	}
	
}

@rulecatch {
	// do not try to recover from parse errors, propagate the error instead
	catch (RecognitionException e) { throw e; }
}	

// START: edifact message
edifact			
@after{
	//System.out.println(buf.toString());
}
			: una? 
			{ buf = new StringBuilder(8192); buf.append(XML_HEAD); buf.append(TAG_EDIFACT); }
			segment*
			{buf.append(TAG_EDIFACT_END);}
;

// UNA is parsed, but the service strings are ignored 
// when parsing the message. The special chars must be given as default
una			: 'UNA:+.? \''|'UNA:+,? \'';

segment			: tag {buf.append("\t<"+$tag.name+">\n"); appendIndexes();}
			data_element* SEGMENT_TERMINATOR (' '|'\n'|'\r'|'\t')*
			{buf.append("\t</"+$tag.name+">\n");}
;

data_element		: ss data_element_payload;

data_element_payload	: {buf.append(TAG_ELEMENT);} 
			(composite_data_item ds)* composite_data_item
			{buf.append(TAG_ELEMENT_END);} 
;
composite_data_item	: composite_data_item_val
			{buf.append(TAG_VALUE); buf.append(sanitizeText($composite_data_item_val.text)); buf.append(TAG_VALUE_END);}
;
composite_data_item_val	: txt|;	// Attention: matches the empty string!

// NOTE: this is to support explicit nesting/looping indexes
tag returns [String name, List indexes]	: tag_name {tagIndexes.clear();} (ds i+=tag_index_id)* 
					  {$name = $tag_name.text.trim();}
;
// for tag names, spaces should be stripped
tag_name		: txt;

// Looping and Nesting Support
tag_index_id		: tag_index_id_val {tagIndexes.add($tag_index_id_val.text);};
tag_index_id_val	: txt|;	

// mapped tokens
ds			: COMPLEX_ELEMENT_ITEM_SEPARATOR;
ss			: ELEMENT_SEPARATOR;
txt			: TEXT_DATA;
//END: edifact message

//START: tokens
RELEASE_CHARACTER			: '?';
ELEMENT_SEPARATOR			: '+';
SEGMENT_TERMINATOR			: '\'';
COMPLEX_ELEMENT_ITEM_SEPARATOR		: ':';
TEXT_DATA				: (~(RELEASE_CHARACTER|SEGMENT_TERMINATOR|COMPLEX_ELEMENT_ITEM_SEPARATOR|ELEMENT_SEPARATOR)|(RELEASE_CHARACTER ELEMENT_SEPARATOR)|(RELEASE_CHARACTER RELEASE_CHARACTER)|(RELEASE_CHARACTER COMPLEX_ELEMENT_ITEM_SEPARATOR)|(RELEASE_CHARACTER SEGMENT_TERMINATOR))+;
//END: tokens
