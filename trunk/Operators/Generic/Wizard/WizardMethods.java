package Operators.Generic.Wizard;
import DataSetTools.wizard.*;
import java.lang.reflect.*;
import DataSetTools.parameter.*;
import java.util.*;
import java.util.zip.*;

public class WizardMethods {

	public static Wizard createWizard(String title, boolean standalone){
		Wizard myWiz = new Wizard(title, standalone);
		return myWiz;
	}
	
	public static void addScriptForm(Wizard myWiz, String scriptName, String retType, 
			String retName, Object retDef, Vector constParams) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, NoSuchMethodException {
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
	
	public static void wizardLoader(Wizard myWiz){
		myWiz.wizardLoader(new String[0]);
	}
}