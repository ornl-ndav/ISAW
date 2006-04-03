package Operators.Generic.Text;

public class StringMethods {

	/**
	 * Static method to be used to construct an operator to convert the characters in a 
	 * text string to lower case.
	 * @param inStr
	 * @return
	 */
	public static String LowerCase(String inStr){
		return inStr.toLowerCase();
	}
	
	/**
	 * Static method to be used to construct an operator to convert the characters in a 
	 * text string to upper case.
	 * @param inStr
	 * @return
	 */
	public static String UpperCase(String inStr){
		return inStr.toUpperCase();
	}
	
}
