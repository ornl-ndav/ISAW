/*
 * File:  IsawLite.java 
 *             
 * Copyright (C) 2003, Peter F. Peterson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.4  2003/06/02 15:25:08  pfpeterson
 * Trivial change to check mail notification.
 *
 * Revision 1.3  2003/05/22 21:35:38  pfpeterson
 * Fixed bug so GUI mode works again.
 *
 * Revision 1.2  2003/05/22 20:38:34  pfpeterson
 * First implementation of nogui mode. Copied code for working with
 * command line from Operators.TextPrompt.
 *
 * Revision 1.1  2003/05/22 18:32:05  pfpeterson
 * Added to CVS.
 *
 */
package IsawGUI;

import Command.Script_Class_List_Handler;
import Command.ScriptOperator;
import DataSetTools.components.ParametersGUI.JParametersDialog;
import DataSetTools.dataset.DataSet;
import DataSetTools.operator.Operator;
import DataSetTools.parameter.IParameter;
import DataSetTools.parameter.DataSetPG;
import DataSetTools.parameter.StringPG;
import DataSetTools.util.DataDirectoryString;
import DataSetTools.util.ErrorString;
import DataSetTools.util.LoadFileString;
import DataSetTools.util.SaveFileString;
import DataSetTools.util.IntList;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Vector;

/**
 * Methods for loading executing an operator using a variety of
 * methods for specifying it.
 */
public class IsawLite{
  public static transient boolean                   LoadDebug = true;
  public static transient Script_Class_List_Handler SCLH      = null;
  public static           boolean                   GUI       = true;

  /**
   * Do not allow anyone to instantiate this class.
   */
  private IsawLite(){}

  /**
   * Factored out code to print a message to line and exit.
   */
  private static void exit(String message, int code){
    if(message!=null && message.length()>0)
      System.out.println(message);
    System.exit(code);
  }

  /**
   * Print a diagnostic message if in debug mode.
   */
  private static void checkOperator(Operator operator){
    if(!LoadDebug) return;

    if(operator==null)
      System.out.println(" NO");
    else
      System.out.println(" OK");
  }

  /**
   * Try to create an operator from a script file. This returns null
   * if not successful.
   */
  public static Operator fromScript(String filename){
    Operator operator=null;
    // try getting script with that name
    if(LoadDebug) System.out.print("Try getting as a script... ");
    if(filename.endsWith(".iss")){ // this is a script attempt
      File file=new File(filename);
      if(file.isFile())
        operator=new ScriptOperator(filename);
    }else{
      if(LoadDebug) System.out.print("(Does not end in .iss)");
    }
    checkOperator(operator);
    return operator;
  }

  /**
   * Try to create an operator from a class name. This returns null if
   * not successful.
   *
   * @param classname the package qualified class name to look for.
   */
  public static Operator fromClassName(String classname){
    Operator operator=null;

    // try getting a class with that name
    if(LoadDebug) System.out.print("Try getting instance directly... ");
    try{
      Class klass=Class.forName(classname);
      int modifier=klass.getModifiers();
      if(Modifier.isAbstract(modifier))
        exit(classname+" is an abstract class",-1);
      if(Modifier.isInterface(modifier))
        exit(classname+" is an interface",-1);
      if(Operator.class.isAssignableFrom(klass))
        operator=(Operator)klass.newInstance();
    }catch(NoClassDefFoundError e){
      if(LoadDebug) System.out.print("(NoClassDefFound)");
    }catch(ClassNotFoundException e){
      if(LoadDebug) System.out.print("(ClassNotFound)");
    }catch(InstantiationException e){
      exit(classname+" can not be instantiated",-1);
    }catch(IllegalAccessException e){
      exit(classname+" can not be instantiated",-1);
    }
    checkOperator(operator);

    return operator;
  }

  /**
   * Try to create an operator from a command name. This returns null if
   * not successful.
   *
   * @param command the name of the command to look for.
   * @param num_param the number of parameters the desired operator
   * should have. If it doesn't matter then set this to -1.
   */
  public static Operator fromCommandName(String command, int num_param){
    Operator operator=null;
    int position;

    // try using Script_Class_List_Handler
    if(LoadDebug) System.out.print("Trying as command name... ");
    if(SCLH==null)
      SCLH=new Script_Class_List_Handler();
    int num_operators=SCLH.getNum_operators();

    // determine the starting position
    position=SCLH.getOperatorPosition(command);
    if(position<0){
      checkOperator(null);
      return null;
    }

    // don't care about number of parameters
    if(num_param<0){
      operator=SCLH.getOperator(position);
      checkOperator(operator);
      return operator;
    }

    // scan for the right number of parameters
    for( ; position<num_operators ; position++ ){
      operator=SCLH.getOperator(position);
      if(! command.equals(operator.getCommand()) ){
        operator=null;
        break;
      }
      if(num_param==operator.getNum_parameters()){
        break;
      }
    }

    // print diagnostic and return
    checkOperator(operator);
    return operator;
  }

  /**
   * Determine if the specified operator needs any DataSets.
   */
  private static boolean hasDataSets(Operator operator){
    int        num_param = operator.getNum_parameters();
    IParameter iparm     = null;
    Object     value     = null;

    for( int i=0 ; i<num_param ; i++ ){
      iparm=operator.getParameter(i);
      if(iparm instanceof DataSetPG) return true;
      value=iparm.getValue();
      if(value instanceof DataSet) return true;
    }

    // must be okay
    return false;
  }

  /**
   * Prints out the origin of the Operator. This is the class name
   * unless it is a script, then it is the file name.
   */
  private static void printSource(Operator operator){
    if(operator instanceof ScriptOperator)
      System.out.println(((ScriptOperator)operator).getFileName());
    else
      System.out.println(operator.getClass().getName().toString());
  }

  /**
   * Deals with nogui interactions. This modifies the parameter in
   * place and returns an ErrorString if anything goes wrong.
   */
  public static ErrorString readUser(IParameter iparm){
    String inPrompt = iparm.getName();
    String inType   = iparm.getType();
    String inDef    = null;

    // fix up the type
    if(inType==null){
      Object val=iparm.getValue();
      if(val instanceof int[])
        inType="Array";
      else if(val instanceof float[])
        inType="Array";
      else{
        String name=val.getClass().getName().toString();
        int index=name.lastIndexOf(".");
        if(index>=0)
          inType=name.substring(index+1);
        else
          inType=name;
      }
    }

    // fix up the default value
    if( iparm instanceof StringPG )
      inDef=((StringPG)iparm).getStringValue();
    else
      inDef=iparm.getValue().toString();

    Object newVal=readUser(inPrompt,inType,inDef);
    if( newVal instanceof ErrorString){
      return (ErrorString)newVal;
    }else{
      iparm.setValue(newVal);
      return null;
    }
  }

  /**
   * Deals with the nogui interactions.
   */
  public static Object readUser(String inPrompt, String inType, String inDef){
    System.out.print(inPrompt);
    if ( !inDef.trim().equals("") ) {
      System.out.print( "( Default: " + inDef + " )");
    }
    System.out.println(":");

    if ( !(inType.equals("String") || inType.equals("Integer") || 
	   inType.equals("Float") || inType.equals("Array") ||
	   inType.equals("Boolean") || inType.equals("DataDirectoryString") ||
	   inType.equals("LoadFileString") || 
	   inType.equals("SaveFileString") ) ) {
      return new ErrorString("Invalid Type:" + inType );
    }

    String inLine = new String();
    try{
      InputStreamReader inStr = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(inStr);
      inLine = br.readLine();
      if ( inLine.length() == 0 ) inLine = inDef;
    }catch (IOException e){
      // let it drop on the floor
    }

    if( inType.equals("String") ){
      return inLine;
    }else if( inType.equals("DataDirectoryString") ){
      return new DataDirectoryString(inLine);
    }else if( inType.equals("LoadFileString") ){
      return new LoadFileString(inLine);
    }else if( inType.equals("SaveFileString") ){
      return new SaveFileString(inLine);
    }else if( inType.equals("Float") ){
      try {
	Float inFloat = new Float(inLine);
	return inFloat;
      }catch( NumberFormatException e){
	return new ErrorString( "Value: " + inLine 
				+ " could not be cast as a Float");
      }
    }else if( inType.equals("Integer") ){
      try {
	Integer inInteger = new Integer( inLine );
	return inInteger;
      }catch ( NumberFormatException e ){
	return new ErrorString( "Value: " + inLine 
				+ " could not be cast as an Integer");
      }
    }else if (inType.equals("Boolean")) {
      inLine=inLine.toUpperCase();
      if ( inLine.equals("TRUE") || inLine.equals("YES") )
	return new Boolean(true);
      else if (inLine.equals("FALSE") || inLine.equals("NO") )
	return new Boolean(false);
      else
	return new ErrorString("TextPrompt: Value " + inLine +
			       "could not be cast as a Boolean");
    }else if( inType.equals("Array") ) {
      Vector ov = new Vector();
      inLine = inLine.trim();
      if ( inLine.charAt(0) != '[' && 
	   inLine.charAt(inLine.trim().length() -1) != ']' ) {
        return new ErrorString( "Arrays must be enclosed in " +
                                "braces e.g. [1:8,13] or [1.4,4.5,8.6]" );
      }
      inLine = inLine.substring(1,inLine.length()-1);
      int lastInd = 0;
      int ind = 0;
      while (ind != -1) {
	String part = new String();
	ind = inLine.indexOf(",", lastInd );
	if (ind != -1 )
	  part = inLine.substring( lastInd, ind );
	else 
	  part = inLine.substring( lastInd );
	
	lastInd = ind+1;
	part=part.trim();
	int[] ilist = IntList.ToArray(part);
	if ( ilist.length > 0 ) {
	  Integer[] olist = new Integer[ilist.length];
	  Vector oArray = new Vector();
	  for (int ii = 0; ii < ilist.length;ii++){
	    olist[ii] = new Integer(ilist[ii]);
	    ov.add(olist[ii]);
	  }
	}else {
	  try {
	    Integer iv = new Integer(part);
	    ov.add(iv);
	  }catch(NumberFormatException iex){
	    try{
	      Float fv = new Float(part);
	      ov.add(fv);
	    }
	    catch ( NumberFormatException fex ) {
	      if( part.equalsIgnoreCase("true") ){
		ov.add(new Boolean(true));
	      }else if( part.equalsIgnoreCase("false") ){
		ov.add(new Boolean(false));
	      }else{
		ov.add(part);
	      }
	    }
	  }
	}
      }
      return ov;
    }else{
      return new ErrorString("TextPrompt: ERROR");
    }
  }

  /**
   * Allows running of Scripts and Operators without Isaw and/or the
   * CommandPane
   */
  public static void main( String args [] ){
    // show usage information if there are not enough parameters
    if( (args==null) || (args.length<1) )
      exit("USAGE: IsawLite [#parameters] "
           +"<CommandName/ClassName/ScriptFile>",-1);

    // useful variables
    Operator operator  = null;
    String   command   = null;
    int      num_param = -1;

    // process the command line
    for( int i=0 ; i<args.length ; i++ ){
      if(args[i].equals("-nogui") || args[i].equals("--nogui") ){
        GUI=false; // nogui mode
      }else{
        try{
          num_param=Integer.parseInt(args[i].trim()); // number of parameters
        }catch(NumberFormatException e){
          command=args[i]; // command name
        }
      }
    }

    // print out what was found from the command line
    if(LoadDebug){
      System.out.println("====================");
      System.out.println("COMMAND="+command);
      System.out.println("NUMPARM="+num_param);
      System.out.println("NOGUI  ="+(!GUI));
      System.out.println("====================");
    }

    // try to get the operator
    operator=fromScript(command);
    if(operator==null)
      operator=fromClassName(command);
    if(operator==null)
      operator=fromCommandName(command,num_param);

    // did anything work?
    if(operator==null)
      exit("Could not find "+command,-1);
    
    if(LoadDebug) printSource(operator);

    if(hasDataSets(operator))
      exit("Cannot run "+command+": requires a dataset",-1);

    // run the operator
    Object result=null;
    num_param=operator.getNum_parameters();
    if(num_param==0){
      result=operator.getResult();
    }else if(num_param>0){
      if(GUI){ // GUI mode
        JParametersDialog diag=new JParametersDialog(operator,null,null,null);
        diag.addWindowListener( new WindowAdapter(){
            public void windowClosed(WindowEvent e){
              exit(null,0);
            }
          });
      }else{ // NOGUI mode
        // process the parameters
        num_param=operator.getNum_parameters();
        IParameter iparm=null;
        ErrorString error=null;
        for( int i=0 ; i<num_param ; i++ ){
          iparm=operator.getParameter(i);
          error=readUser(iparm);
          if(error!=null)
            exit(error.toString(),-1);
        }
        result=operator.getResult();
      }
    }else{
      exit("Unusual number of parameters (less than zero)",-1);
    }

    // process the result
    if(result instanceof ErrorString)
      exit("ERROR: "+result.toString(),-1);
    else
      if(num_param==0 || !GUI)
        exit("Result: "+result,0);
  }

}
