package Operators.Generic.Wizard;
import DataSetTools.wizard.*;
import java.lang.reflect.*;
import DataSetTools.parameter.*;
import java.util.*;
import java.util.zip.*;

public class WizardMethods {

	/**
	 * This method wraps the constructor for a Wizard.  This was written in
	 * order to allow the generation of an ISAW Operator to do this.
	 * @param title The title for this Wizard
	 * @param standalone Is this Wizard being run as a standalone application.  
	 * Normally this is set to false.
	 * @return A new Wizard object.
	 */
	public static Wizard createWizard(String title, boolean standalone){
		Wizard myWiz = new Wizard(title, standalone);
		return myWiz; 
	}
	
	/**
	 * This method is used to add a ScriptForm to a Wizard
	 * @param myWiz The Wizard to add the form to
	 * @param scriptName the name of the script to add
	 * @param retType the return type for the script.  This is the name of 
	 * a parameterGUI without the PG extension.
	 * @param retName A prompt for the parameter GUI used for the return value
	 * @param retDef A default value for the return value
	 * @param constParams A list of parameters to hold constant on this form.
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 */
	public static void addScriptForm(Wizard myWiz, String scriptName, String retType, 
			String retName, Object retDef, Vector constParams) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, NoSuchMethodException,
			InvocationTargetException{
		Class c;
		try {
			c = Class.forName("DataSetTools.parameter."+retType + "PG");
		
		}
		catch (ClassNotFoundException CNFEx){
			System.out.println("Exception in addScriptForm");
			CNFEx.printStackTrace();
			throw new ClassNotFoundException("Trouble creating "+retType+"PG",CNFEx);
		}
		
		Class[] params = {(new String()).getClass(), (new Object()).getClass()};
		Constructor[] allConstr = c.getConstructors();
		
		Constructor PGConstr;
		try {
			PGConstr = c.getConstructor(params);
		}
		catch (NoSuchMethodException nsmEx) {
			nsmEx.printStackTrace();
			throw new NoSuchMethodException("Could not find constructor for "+ retType+"PG");
		}

		int[] cParams = new int[constParams.size()];
		for (int ii=0; ii < constParams.size();ii++ ) {
			cParams[ii] = ((Integer)(constParams.elementAt(ii))).intValue();
		}
		Object[] oParams = new Object[]{ retName, retDef };
		try {
			myWiz.addForm(new ScriptForm(scriptName,(IParameterGUI)(PGConstr.newInstance(oParams)), cParams));
		}
		catch (InvocationTargetException ITFoc){
			throw ITFoc;
		}
		
	}

	public static void wizardLinkParameters(Wizard myWiz, Vector vLinks) throws Exception {
		int numEntries = vLinks.size();
		int numForms = myWiz.getNumForms();
		//Make sure that all elements of this vector are vectors.  This shows that this
		//array is at least 2D
		for (int ii = 0; ii<numEntries; ii++){
			if (!(vLinks.elementAt(ii) instanceof Vector)){
				throw new Exception("wizardLinkParameters input must be a 2D array see row " + ii);
			}
		}
		//Try to extract the ints
		int[][] links = new int[numEntries][];
		for (int ii = 0; ii<numEntries; ii++) {
			Vector linkSet = (Vector)(vLinks.elementAt(ii));
			int numLinksSet = linkSet.size();
			if (numLinksSet != numForms){
				throw new Exception("wizardLinkParameters: the number of links must equal"+
						"the number of forms.  #forms= " + numForms + " row " + ii +
						" has " + numLinksSet + "links");
			}
			links[ii] = new int[numLinksSet];
			for (int jj=0; jj<numLinksSet; jj++){
				if( !(linkSet.elementAt(jj) instanceof Integer) ) {
					throw new Exception("wizardLinkParameters: All entries must be " +
							"Integers see element [" + ii + "," +jj);
				}
				links[ii][jj] = ((Integer)(linkSet.elementAt(jj))).intValue();
			}
		}
		myWiz.linkFormParameters(links);
	}

	/**
	 * 
	 * @param myWiz
	 * @param myHelp
	 */
	public static void wizardSetHelpMessage(Wizard myWiz, String myHelp){
		myWiz.setHelpMessage(myHelp);
	}

	/**
	 * 
	 * @param myWiz
	 * @param myURL
	 */
	public static void wizardSetHelpURL(Wizard myWiz, String myURL){
		myWiz.setHelpURL(myURL);
	}

	/**
	 * 
	 * @param myWiz
	 * @param myDir
	 */
	public static void wizardSetProjectsDirectory(Wizard myWiz, String myDir){
		myWiz.setProjectsDirectory(myDir);
	}

	/**
	 * 
	 * @param myWiz
	 */
	public static void wizardLoader(Wizard myWiz){
		myWiz.wizardLoader(new String[0]);
		}
}