/**
 * 
 */
package Operators.Generic.System;
import java.text.ParseException;
/**
 * @author John Hammonds
 *
 */
public class ParseStringMacroBase {

	/**
	 * 
	 */
	public ParseStringMacroBase() {
		super();
		// TODO Auto-generated constructor stub
	}

    /**
     * Static method for parsing a string to look for macros.  Macros have the form 
     * $(MACRO_NAME) which are imbedded within strings.  The macro will be substituted by
     * values found in the System properties.  
     */
	public static String parseMacroString(String inStr) throws ParseException {
		StringBuffer inStrBuf = new StringBuffer(inStr);
		int startIndex=0;
		while ((startIndex = inStrBuf.indexOf("$")) != -1){
			if(inStrBuf.charAt(startIndex + 1) == '{' ) {
				int lastIndex=0;
				if (( lastIndex = inStrBuf.indexOf("}", startIndex)) != -1){
					String macro = inStrBuf.substring(startIndex+2,lastIndex);
					inStrBuf.delete(startIndex,lastIndex+1);
					String macroSubs = System.getProperty(macro);
					if (macroSubs == null) {
						String err = ParseStringMacroBase.getErrString( inStr, startIndex, lastIndex);
						throw new ParseException(err, startIndex);
					}
					inStrBuf.insert(startIndex, macroSubs);
			    }
				else {
					String err = ParseStringMacroBase.getErrString(inStr, startIndex, startIndex+1);
					throw new ParseException(err, startIndex);
				}
			}
		}
		return inStrBuf.toString();
	}
	
	/**
	 * private method to construct a string showing where problems are
	 */
	private static String getErrString(String orig, int first, int last){
		StringBuffer errStr = new StringBuffer("Error parsing String macros.\n"); 
		errStr.append("OriginalString: " + orig + "\n");
		errStr.append("                ");
		for (int ii = 0; ii < first; ii++){
			errStr.append(" ");
		}
		for (int ii=first; ii < last; ii++){
			errStr.append("^");
		}
		errStr.append("\n");
		return errStr.toString();		   
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String str1 = new String("This is the user home directory ${user.home}");
		String str2 = new String("This is the user current directory ${user.dir}");
		
		try {
			System.out.println("Original: " + str1);
			String newStr1 = ParseStringMacroBase.parseMacroString(str1);
			System.out.println("With Subst: " + newStr1);
		}
		catch(ParseException ex){
			ex.printStackTrace();
		}
		try {
			System.out.println("Original: " + str2);
			String newStr2 = ParseStringMacroBase.parseMacroString(str2);
			System.out.println("" + newStr2);
		}
		catch(ParseException ex){
			ex.printStackTrace();
		}
	}

}
