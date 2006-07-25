/**
 *  $Log$
 *  Revision 1.3  2006/07/25 19:57:00  dennis
 *  Minor fix to javadocs.
 *
 *
 *  revision 1.2, date: 2006/04/03 18:53:56;  author: hammonds;  
 *  New methods to add to scripting language.
 *
 *  revision 1.1, date: 2006/04/03 16:55:08;  author: hammonds; 
 *  Methods added to allow changing the case of a string.
 */

package Operators.Generic.Text;

public class StringMethods {

	/**
	 * Static method to be used to construct an operator to convert 
         * the characters in a text string to lower case.
	 * @param inStr
	 * @return A new String containing the lower case versions of the
         *         specified String.
	 */
	public static String LowerCase(String inStr){
		return inStr.toLowerCase();
	}
	
	/**
	 * Static method to be used to construct an operator to convert 
         * the characters in a text string to upper case.
	 * @param inStr
	 * @return A new String containing the upper case versions of the
         *         specified String.
	 */
	public static String UpperCase(String inStr){
		return inStr.toUpperCase();
	}
	
	/**
	 * This method determines if a string ends with a specified string.
	 * @param inStr  The main string which is being checked
	 * @param compStr The string for comparison.
	 * @return  does inStr end with compStr
	 */
	public static boolean EndsWith(String inStr, String compStr){
		return inStr.endsWith(compStr);
	}

	/**
	 * This method determines if a string starts with a specified string.
	 * @param inStr  The main string which is being checked
	 * @param compStr The string for comparison.
	 * @return  does inStr start with compStr
	 */
	public static boolean StartsWith(String inStr, String compStr){
		return inStr.startsWith(compStr);
	}
	
	/**
	 * returns the index within the string of the specified substring 
	 * @param inStr  the string to be searched
	 * @param compStr the substring that is being searched for
	 * @return the index of where compStr can be found in inStr.  If inStr 
	 * does not contain the substring then -1 is returned.
	 */
	public static int IndexOf(String inStr, String compStr){
		return inStr.indexOf(compStr);
	}

	/**
	 * Get the length of a string.
	 * @param inStr any String
	 * @return the length of the input string.
	 */
	public static int StringLength(String inStr){
		return inStr.length();
	}

	/**
	 * return a substring of the original string.  The return string starts
	 * at the beginning index and ends at the ending index - 1.
	 * @param inStr  the input string from which to extract the 
         *               ending string
	 * @param begIndx the beginning index of the substring
	 * @param endIndx the ending index of the substring
	 * @return a substring of the original string
	 */
	public static String SubString(String inStr, int begIndx, int endIndx){
		return inStr.substring(begIndx, endIndx);
	}

}
