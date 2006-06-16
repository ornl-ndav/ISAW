/*
 * File:  execOneLine.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.89  2006/06/16 18:11:17  rmikk
 * Returns line numbers and methods 3 levels deep where exceptions occure
 *
 * Revision 1.88  2006/06/09 16:13:02  rmikk
 * Fixed a bug dealing with "" in a little used static method . It is currently being
 *    used while parsing the initial value in a Scripts parameters
 *
 * Revision 1.87  2006/04/12 19:15:35  rmikk
 * Eliminated this reference to static variables so warnings are reduced
 *
 * Revision 1.86  2006/03/13 20:35:18  rmikk
 * Now allows for Display to appear at a  place not at the beginning
 *
 * Revision 1.85  2005/06/24 03:32:12  rmikk
 * Fixed problem with finding variables
 *
 * Revision 1.84  2005/06/14 13:37:53  rmikk
 * Removed variables that represent parameters from the system before
 * starting a new script
 *
 * Revision 1.83  2005/06/13 20:27:34  rmikk
 * Did additional type checking with the new Object data type to ensure that
 *   there is only one variable with a specific name
 *
 * Revision 1.82  2005/06/10 20:56:14  rmikk
 * Fixed an error in concatenating two strings
 *
 * Revision 1.81  2005/06/07 14:57:24  rmikk
 * An opaque Object type is now supported even as a parameter
 * Code had to be added to ensure that no operations can be done on these
 *    variables
 * Caught any exception and converted it to an error condition giving
 *    Exception type, class name, and line number where it occurred.
 *
 * Revision 1.80  2005/06/06 14:41:05  rmikk
 * Fixed getSHOp so if there is any throwable a null operator is returned(i.e.
 *    could not find an operator)
 * If an op.getResult creates an exceptions, an errorstring giving the
 *   Class name and line number of this error is given.
 *
 * Revision 1.79  2005/06/02 16:31:30  dennis
 * Fixed error in SetOpParameters() method.  Misplaced '}'
 * enclosed a return in a for loop, which broke the
 * setting of paramters for DataSetOperators.  This fixes
 * the bug that broke the lruns.iss script.
 *
 * Revision 1.78  2005/05/31 20:13:27  rmikk
 * Eliminated the javadoc error with 2 @return statements
 *
 * Revision 1.77  2005/05/13 19:47:52  rmikk
 * Added code to give a good error message if not all the arguments
 *    for an operator match the arguments supplied.
 *
 * Revision 1.76  2005/04/23 12:42:49  rmikk
 * Double values that need to be used in  ISAW scripts are converted to 
 * Floats.  If passed through( stored in an array or an argument to an
 * operator) they are still Double.  Operators should check Data Types of
 * any Vector they receive.
 *
 * Revision 1.75  2005/04/16 18:50:49  rmikk
 * The Display script command now returns a ViewManager when viewing a 
 * DataSet
 *
 * Revision 1.74  2005/01/10 20:15:12  rmikk
 * Fixed a misterious error with the & operation
 *
 * Revision 1.73  2005/01/07 17:46:54  rmikk
 * Added type checking to eliminate run time errors
 * Added a method to clear ChooserPG storage
 *
 * Revision 1.72  2004/03/17 20:25:08  dennis
 * Fixed @see tag that was broken when view components, math and
 * util were moved to gov package.
 *
 * Revision 1.71  2004/03/15 19:34:46  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.70  2004/03/15 03:30:16  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.69  2004/01/29 18:17:29  dennis
 * Fixed javadoc error.
 *
 * Revision 1.68  2004/01/08 20:07:06  bouzekc
 * In co-operation with Ruth, removed unused variables and made two
 * unused private utility methods public static methods.
 *
 * Revision 1.67  2003/12/14 19:18:09  bouzekc
 * Removed unused import statements.
 *
 * Revision 1.66  2003/12/09 14:41:14  rmikk
 * fixed javadoc warnings
 *
 * Revision 1.65  2003/12/02 18:58:34  rmikk
 * Fixed incompatibility error with DataSetPG.
 *
 * Revision 1.64  2003/11/22 20:37:05  rmikk
 * Updated the javadocs for the Send command
 *
 * Revision 1.61  2003/09/14 17:57:03  rmikk
 * -Added code to reduce the holding on to Data Sets and
 *  clearing out memory
 *
 * Revision 1.60  2003/07/23 21:53:39  rmikk
 * Fixed error in setting operands to an operator when too
 * few operand values are given.
 *
 * Revision 1.59  2003/07/15 16:43:19  rmikk
 * Removed a Display window from the Vector of open
 * displays when the window is closed.  This is done as
 * the script is executing.
 *
 * Revision 1.58  2003/07/14 16:47:35  rmikk
 * Added type checking code to prevent a run time error
 *
 * Revision 1.57  2003/07/01 20:08:12  rmikk
 * -Added a lot more documentation
 * -Rearranged methods into sections
 *
 * Revision 1.56  2003/06/26 22:33:28  rmikk
 * Removed reference to the never used static instance of the
 *   Script_Class_List_Handler.  Eliminated a strange error.
 *
 * Revision 1.55  2003/06/24 20:13:13  dennis
 * Now calls ScriptUtil.display(). (Ruth)
 *
 * Revision 1.54  2003/06/19 22:29:55  pfpeterson
 * Converted to use more of ScriptUtil. Removed dead code and commented
 * out code.
 *
 * Revision 1.53  2003/06/19 21:20:18  pfpeterson
 * Uses ScriptUtil for 'Display'.
 *
 * Revision 1.52  2003/06/10 20:22:36  pfpeterson
 * Only gets Script_Class_List_Handler when needed. Also added polymorphism
 * for Integer to use Float.
 *
 * Revision 1.51  2003/06/10 19:07:45  pfpeterson
 * Made Load(filename,varname) more forgiving with case for runfiles.
 *
 * Revision 1.50  2003/06/10 15:28:46  pfpeterson
 * Added an explicit check to see if the file exists before trying to
 * 'Load' it. Returns a more appropriate error message with this failure.
 *
 * Revision 1.49  2003/06/10 15:20:05  pfpeterson
 * Fixed null pointer exception while loading.
 *
 * Revision 1.48  2003/06/06 22:27:38  pfpeterson
 * Made reference to Script_Class_List_Handler a static variable.
 *
 * Revision 1.47  2003/06/03 22:02:52  rmikk
 * -Allows for other Viewer Types that can be added at
 *   a later date
 *
 * Revision 1.46  2003/04/25 22:21:07  pfpeterson
 * Changed Script_Class_List_Handler to be a class variable (rather
 * than instance) that is not initialized until the first instance
 * of execOneLine is created.
 *
 * Revision 1.45  2003/04/25 19:51:44  rmikk
 * Fixed error. Now the Script Load command returns an integer
 *    representing the number of DataSets and not 0
 *
 * Revision 1.44  2003/03/21 20:16:08  rmikk
 * Deleted IObservers from some DataSets that leave the
 *   scripting system
 *
 * Revision 1.43  2003/03/21 17:23:36  rmikk
 * Added code to reduce memory needs for sequences of
 *   arrays with large DataSets
 *
 * Revision 1.42  2003/03/07 19:47:30  rmikk
 * Returns an error when assigning an illegal object to a variable
 *
 * Revision 1.41  2003/02/24 13:29:16  rmikk
 * Eliminated an error.
 *
 * Revision 1.40  2003/02/21 19:35:44  pfpeterson
 * Changed calls to fixSeparator appropriate (not deprecated) method.
 *
 * Revision 1.39  2003/01/02 20:45:26  rmikk
 * Includes two methods to setIObserverList and setPropertyChangeList
 *
 * Revision 1.38  2002/12/16 20:43:45  pfpeterson
 * Fixed problem where execOneLine.getSHOp did not find operators where subclassing
 * happened with the parameters. This was fixed by changing a comparison of classes
 * using 'equals' to a comparison using 'instanceof'. Now accepts Integers will be
 * accepted as Floats. Also changed a variable name to make it more clear to the
 * people reading the source.
 *
 * Revision 1.37  2002/11/27 23:12:10  pfpeterson
 * standardized header
 *
 * Revision 1.36  2002/11/11 16:55:01  rmikk
 * The NULL value now removes variables from the list of
 *     variables that are not parameters.
 *
 * Revision 1.35  2002/08/19 17:07:10  pfpeterson
 * Reformated file to make it easier to read.
 *
 * Revision 1.34  2002/06/12 18:45:58  rmikk
 * Added code to consider tabs like spaces
 *
 * Revision 1.33  2002/02/22 20:33:47  pfpeterson
 * Operator Reorganization.
 *
 * Revision 1.32  2002/02/11 21:32:18  rmikk
 * Fixed a bug that occurred with the new StringChoiceList.
 * The fix was a major fix that could break the other SpecialStrings
 *  CVS: ----------------------------------------------------------------------
 *
 * Revision 1.31  2002/01/11 19:27:36  rmikk
 * The Display view can now Display Tables
 * The Send can now take 1 or 2 arguments
 *
 * Revision 1.30  2001/11/27 18:39:57  dennis
 * Change the javadocs to reflect that this Save now works and it
 * only saves in java binary form. (Ruth)
 *
*/
package Command;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import java.io.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import java.awt.*;
import javax.swing.text.*;
import java.awt.event.*;
import DataSetTools.operator.*;
import java.beans.*;
import java.util.*;
import DataSetTools.parameter.*;
/** 
 *  This class lexically analyzes, parses and executes one line of code using the values
 *  stored in variables  from the execution of previous lines of code
 *  The data types supported by the parser are Float, Integer, Boolean,
 *  DataSet, and Vector( Arrays). Also True, False, and Null are predefined
 *  constants.
 *
 *  The lexical analyzer is built into this code.  The arguments of most routines are 
 *  lexical analyzer variables.  For example:
 *
 *         int execute( String S , int start , int end )
 *
 *  S is the whole line of code to be executed. 
 *  The parameters start and end refer to positions in the string S.
 *  The return value is also a position in the string S.  It is the next position that is to be
 *  lexically analyzed and parsed.
 *
 *  The execute method above is the START of the whole process.
 *  A Global variabel Result holds the result so far as the line is being executed
 */ 
public class execOneLine implements gov.anl.ipns.Util.Messaging.IObserver,IObservable ,
                                           PropertyChangeListener ,Customizer {
    public static final String ER_NoSuchFile         = "File not Found";
    public static final String ER_NoSuchVariable     = "Variable not Found";
    public static final String ER_IllegalCharacter   = "Illegal Character";
    public static final String ER_MisMatchQuote      = "MisMatched Quotes";
    public static final String ER_MisMatchParens     = "Mismatched Parenthesis";
    public static final String ER_DataSetAlreadyHere
                                           = "Data Set has already been loaded";
    public static final String ER_NotImplementedYet  = "Not implemented yet";
    public static final String ER_MissingOperator    = "Operation is missing";
    public static final String ER_ImproperArgument   = "Improper Argument";
    public static final String ER_MissingArgument    = "Argument is Missing";
    public static final String ER_NumberFormatError  = "Number Format Error";
    public static final String ER_NoSuchOperator     = "Operation is not Found";
    public static final String ER_FunctionUndefined  = "Function is undefined";
    public static final String  ER_OutputOperationInvalid
                                                     = " Could not Save";
    public static final String ER_MissingQuote       = "Quotation mark missing";
    public static final String ER_MissingBracket     = " Unpaired brackets";
    public static final String ER_ImproperDataType   = " Improper Data Type";
    public static final String ER_ExtraArguments     = "Extra Arguments";
    public static final String ER_No_Result          = " Result is null ";
    public static final String ER_IMPROPER_DATA_TYPE
                                           = "Variable has incorrect data type";
    public static final String ER_ReservedWord       = " Reserved word";
    public static final String ER_ArrayIndex_Out_Of_bounds
                                                  = "Array index out of Bounds";
    public static final String WN_Return          = "Return Statement executed";
    
    private Document logDocument       = null;
    private static final boolean Debug = false;

   
    /******************* Symbol Table(s) ********************/

   
    Hashtable   ds =new Hashtable(); //Copy of the data set(s) "Global" DataSets
	                               // These are passed in by outside sources.
                                       // The copy method( not clone) is used 
                                       // to change their values

  
    Integer Ivals[]; 
    String Ivalnames[]; 
    
    Float Fvals[]; 
    String Fvalnames[];
    
    String Svals[]; 
    String Svalnames[];
    
    Hashtable BoolInfo = new Hashtable();
   
    Hashtable lds = new Hashtable() ;   //local dataset storage. 
    Object Result;                     //Storage for Intermediate Results of
                                       //  operations

    Hashtable ArrayInfo = new Hashtable();
    Hashtable MacroInfo = new Hashtable();                  //Stores Macros
    Hashtable ObjectInfo= new Hashtable();
    
    /* ----------------------  End Symbol Table(s) --------------------------*/
    IObserverList  OL; 
    PropertyChangeSupport PC;
    
    // Contains user supplied operators 
    //static Script_Class_List_Handler SH = null;
    
    /*----------------------------  Error variables ------------------------*/
    int perror;                      //position of error on a line
    int lerror;                      //line number of error.
    String serror;                   // error message.
 
    /*---------------------------End Error Variables ---------------------------/


    /*------------------------- Save for later Deletes --------------------- */ 
    Vector Graphs = new Vector();  // Saves all displays so as to
                                   // delete them when done.

    Vector Params = new Vector();      // Saves all Global parameters
                                       // added on Script
    /* ------------------------ End Saves for later Deletes -----------------*/


    

   //************************** Constructors **********************************

    /** Initializes all variables
     *  
     */
    public execOneLine(){
        initt();        
        OL = new IObserverList();        
        PC = new PropertyChangeSupport( this );
    }

    /**
     * This constructor adds the data set to the variable space
     * @param   Dat  the data Set to be added as a "Global" data set
     * @param  vname  the name used to refer to this data set
     */
    public execOneLine( DataSet Dat, String vname ){
        initt();
        ds.put(Dat,vname);
        OL = new IObserverList();
        PC = new PropertyChangeSupport( this );
    }
    
    /** 
     * Deprecated
     * This constructor adds the set of data sets to the variable
     * space.<P> The names of the variables are defined by their
     * tag number
     */
    public execOneLine( DataSet dss[], String vname ){
        int i;
        initt();
        
        for( i = 0 ; i < dss.length ; i++ )
            ds.put(dss[i],vname+"["+i+"]");//ds[i] = eliminateSpaces( ds[i] );
        OL = new IObserverList();
        PC = new PropertyChangeSupport( this );
        OL = new IObserverList();
    }

  

   /**
     * Initialized the variables and workspace for a new run
     */
    public  void initt(){
        clearHT( BoolInfo);
        BoolInfo = null;
        BoolInfo = new Hashtable();
        BoolInfo.put("FALSE",new Boolean(false));
        BoolInfo.put("TRUE", new Boolean(true));
        Ivals=null;
        Ivalnames=null;
         
        Fvals = null;  
        Fvalnames = null;
        Svals=null; 
        Svalnames=null;
        lds.clear();        // May need an Empty data set somewhere but where?
        //ds = new HashMap()'
        perror = -1;
        serror = "";
        lerror = -1;
        Result=null;
        clearVec( Params);
        Params = null;
        Params= new Vector();
        
        //ds= new Hashtable();
        ArrayInfo.clear();
        ArrayInfo = null;
        ArrayInfo=new Hashtable();
        System.gc();
    }
   /**
   *  Unfinished
   *   Clears out entries in Vectors, Hashtables, Arrays, etc. and 
   *   Makes them null.
   */
   public void Clearr( Object O){
      if( O == null) return;
      if( O instanceof Vector)
          clearVec( (Vector)O);
      else if( O instanceof Hashtable)
          clearHT( (Hashtable)O);
      else if( O.getClass().isArray()){
        

      }
      else if( O instanceof IParameter){
         Object O1 =((IParameter) O).getValue();
         ((IParameter) O).setValue( new Object());
         Clearr( O1);
         O1 = null;
        
      }
   }
   public void clearHT( Hashtable  HT){
     if( HT == null)
       return;
     HT.clear();
   }
   public void clearVec( Vector V){
      if(V==null)
        return;
      for( int i=0; i<V.size(); i++){
        ds.remove(V.elementAt(i));
      }
      V.removeAllElements();
   }
 

   //*****************************  Listener set ups *******************

    /** Sets the whole list of property change listeners  
    *   NOTE: Used when alternating between different languages
    *   Presently the PropertyChangeListeners listens for the "Display" property only.
    *   Deprecated by DataSetTools.util.SharedData.addmsg( Object O);
    */
  

  public void setPropertyChangeList( PropertyChangeSupport PcSupp) {
       PC = PcSupp;
    }
     
    /** Sets the whole IObserverList.  
    *   NOTE: Used when alternating between different languages
    */
    public void setIObserverList( IObserverList IOlist){
      OL = IOlist;
    }
      
   
    
    /**  
     * Sets the document that will log the operations
     *
     * @param doc    The document that logs operations
     */
    public void setLogDoc( Document doc){
        logDocument = doc;
    }
    

 

   //************************** Error Handling Routines *******************
    /**
     * Use this method to reset the error to false.
     *
     * It is used to continue executing immediate instructions after
     * an error occurs
     */
    public void resetError(){
        perror = -1;
        serror = null;
    }

    /**
     * @return the error message or null if no error exists
     */
    public String getErrorMessage(){
        if( perror < 0 )
            return null;
        else 
            return serror;
    }

    /**
     * @return returns the position in the string where the error occurred
     */
    public int getErrorCharPos(){
        return perror;
    }

   /**
   *   Utility to quickly set errors in one statement
   */
   private  void seterror( int charnum, String errorMessage){
        //if(charnum < 0)return ;      
        perror = charnum;
        serror = errorMessage;
    }

    //**************************  Global Data Sets  **************************
    /**
     * This Method adds a data set to the global data sets of the
     * command pane
     *
     * These data sets will be removed when the initt routine is
     * executed
     * 
     * @param dss the data set to be added as a Global Data Set
     * @param vname the name that will be used by the command pane to
     * refer to this data set
     */
    public void addParameterDataSet(DataSet dss, String vname){
        addDataSet(dss,vname);
        Params.addElement(vname.toUpperCase().trim());
    }

    /**
     * This method allows outside data sets to be added to the
     * variable space
     * @param dss The data set to be added as a Global data Set with the 
     *                  name vname
     * @param vname The name that will be used by the command pane to
     * refer to this data set
     */
    public void addDataSet(DataSet dss, String vname){
        ds.put( vname.toUpperCase().trim() , dss );
    }

    /**
     * This method allows outside data sets to be added to the variable space
     *
     * @param dss The data set to be added
     *
     * NOTE: The name that the command pane will use to refer to this
     * data set is DSx where x us the value of the DataSet Tag
     * attribute
     */
    public void addDataSet( DataSet dss ){
        String vname;
        if(Debug)System.out.println("IN ADD DATASET");
        long tag = dss.getTag();         
        
        //if(tag != null){
            vname ="ISAWDS"+new Long(tag).toString();
        //}else vname = dss.getTitle();
        if(Debug)System.out.println("EndADD DATA SET vname="+vname);
        addDataSet(dss, vname);
        dss.addIObserver(this);
    }
    

     /**
     *  Gets the set of all Global DataSets. 
     */
    public DataSet[] getGlobalDataset(){
        if( Debug){
            System.out.println( "in getGlobalDataSet") ;
            if( ds == null )
                if(Debug)System.out.println("ds is null");
        }
        Enumeration D = ds.elements();//.toArray();
        DataSet DD[] ;

        int i;
        
        //count the elements
        for( i=0; D.hasMoreElements(); i++){
          D.nextElement();
        }
        
        DD = new DataSet[i];
        D= ds.elements();
        for( i=0; D.hasMoreElements(); i++){
            DD[i]=(DataSet)(D.nextElement());
        }
        
        return DD;
    }

   // ----------  Removes Displays when Clear button pressed ------------------
    /**
     * Removes all displays created by the CommandPane
     *
     * @see #Display( DataSetTools.dataset.DataSet, java.lang.String ,
     * java.lang.String)
     */
    public void removeDisplays(){
        if( Graphs == null)
            return;
        if( Graphs.size() <= 0 )
            return;
        ViewManager vm;
        for( int i = 0 ; i < Graphs.size() ; i++){
            vm  = (ViewManager)Graphs.elementAt( i );
            vm.destroy();
            vm= null;
        }  
        vm= null;
        Graphs= new Vector();      
    }
   


   //*********************** Execute One Line Section **********************
    /**
     * execute starts lexing, parsing, and executing the String S
     * up to the end of the String, a colon or an unmatched
     * parenthesis or a comma or at the same level as the start's
     * character
     *
     * @param S The string to be executed
     * @param start The starting character
     * @param end The last character of the string to be considered
     *
     * @return The position of the first character that was not
     * considered
     * The Global variable Result contains the current value
     */
    public int execute( String S , int start , int end ){
        int i = start,
            j,
            j1,
            kk,
            retn;

      try{
      
        //----------------- Check for error conditions---------------------

        String Command;
        if( start != 0){
            if(Result != null ){
                //seterror( start , "internalerrorn" );
                // return start;
            }
        }
        
        S = Trimm( S );  
        if( end > S.length() )
            end = S.length();
        
        if( perror >= 0 )
            return perror;
        
        if( Debug )
            System.out.println( "in execute String" + S + " , " + start );
        if( S == null || S.length()<=0 ) 
            return 0;
        if( start < 0 || end<=0 )
            return 0; 
        if( start >= end )
            return S.length();
        if( (start >= S.length()) || ( start >= end) )
            return start;
        

        //----------------------- Get next "TOKEN" ----------------------------
        i = skipspaces( S , 1 , start );
        if( i > end) i = end;
        
        j = findfirst( S , 1 , i , " \t(+-*=;:'/^+,[]<>&)\",","" );     
                                 //  \t(+-*=;:'/^+,[]<>&)\",","" are the set of 
                                 // "delimiters".  Should be a constant

        if( j > end) j = end;
        if( (i >= 0) && (i < S.length() ) && (i < end) )
            if(" \t(+-*=<>;:'/^+,[]&)\"," .indexOf( S.charAt( i ) ) >= 0 )
                j = i ;
        
        
        if( Debug )
            System.out.print("i ,j=" + i+ "," + j);
        if( (j < i) ||  (i < 0) ||  (i >= end) ||  (i >= S.length() ) ){
            return i;
        }
        j1 = skipspaces( S , 1 , j );                      
        if( j1 > end) j1 = end;
        if( j <= i ){
            Command = S.substring( i, i + 1 ); 
            j1 = j; 
        }else 
            Command = S.substring( i , j ).trim();
        

          //?????????? Is the "Command"  an array reference ????????????????????
        if ( (j1 >= 0) && (j1 < S.length()) && ( j1 < end))
            if(  (S.charAt( j1 ) == '[') && (j==j1)&&(j1>0)&&(i<j1) ){  

                int j2=findfirst(S,1,j1+1,"]","[]");
                if((j2>=end)||(j2>=S.length())){
                    seterror(j1, ER_MissingBracket);
                    return j1;
                }
                j1=j2+1;
                Command = S.substring(i,j1).trim().toUpperCase();
                j1 = skipspaces(S,1,j1);
                
                j=j1;
                if(Debug)
                    System.out.println("[], C,j="+Command+","+j);
            }
        if( Command.charAt(0) != '\"' )
            Command = Command.toUpperCase();
        if( Debug )
            System.out.print( "C=" + Command );
        if( Command.equals("LOAD") ){
            int ii = execLoad( S , j , end );
            if( start == 0 )
                Result = null;
            if( Debug )
                System.out.println( "Aftret ExecLoac ret=" + ii );
            return ii;                             
            
        }else if( Command.equals( "DISPLAY" ) ){
            //if( start != 0 ){
           //     seterror( i , "Must be the First command on a line" );
	       // return i;
            //}
            retn = execDisplay( S , j , end );
            //Result = null;
            return retn;
            
        }else if( Command.equals("SAVE") ){
            if( start != 0 ){
                seterror( i , "Must be the First command on a line" );
                return i;
            }
            retn = execSave( S , j , end );
            Result = null;
            return retn;
            
        }else if( Command.equals( "SEND") ){
            if(start!=0){
                seterror( i , "Must be the First command on a line" );
	        return i;
            }
            if(Debug)
                System.out.println("Send j1="+j1);	
            retn =execSend(S,j1,end);
            Result = null;
            return retn;             
        }else if ( (Command.equals( "REM" )) && (start == 0)  ){
            Result = null; 
            return S.length(); 
        }else if( Command.equals("RETURN")){
            j = skipspaces( S, 1, j);
            if( j < S.length()){
                j = execute( S, j, S.length());               
                if( perror >= 0)
                    return j;
            }
            seterror( j , WN_Return );
            return j;
        }            
        
         
        if( Debug )
            System.out.println( "1C=" + Command + ":" + (Command=="(") ); 
        
        
        if( i >= end ){
            Result = null;
            return end;
        }else if( i >= S.length() ){
            Result = null;
            return S.length();

        // ?????? Is it an Assignment statement
        }else{
            if((j1<S.length())&&(j1>=0)&&(j1 < end))
                if( (start==0) &&(S.charAt(j1) == '=') ){
                    if(Debug) 
                         System.out.println("Assignement op "+ Command); 
                    if( start != 0){
                        seterror( j1 , ER_IllegalCharacter+"A");
                        return j1;
                    }  

                    //----- Get the result from RHS of =  -----------------
                    kk = execute( S , j1 + 1 , end);  


                    //-----Assign it to the variable of the LHS --------------                 
                    if( perror < 0 )
                        Assign( Command.toUpperCase() , Result ); 
                    else 
                        return perror;

                    if( perror >= 0) 
                        perror = kk;

                    Result = null;                //The result of an assignment 
                                                  //statement is null
                    if( Debug )
                        System.out.println("kk=" + kk+","+perror);
                    if( ( kk  <= j1 + 1) ||  (perror >= 0) ){
                        return S.length() + 1;
                    }else
                        return skipspaces( S , 1 , kk );
                }

            // Line or part of line is an expression not a Command. Execute it
            return execExpr(S,start,end);                        
            
        }
      }catch(Exception sss){
        String[] SS = ScriptUtil.GetExceptionStackInfo(sss,true,1);
        S ="";
        if( SS != null) 
          for(  i = 0 ; ( i < 3 ) && ( i < SS.length ) ;  i++){
        	S +=  SS[i]+"\n  ";  
          }
        serror= sss.toString()+"\n  "+ S;
        perror = i;
        return i;
      }
    }
    


    //--------- Finishes the Load, Display, Save, and Send commands -----------	   
    
    /**
     * Executes the LOAD command.  Brings the data sets into the local
     * workspace
     * @param S The string to be executed
     * @param start The starting character
     * @param end The last character of the string to be considered
     *
     * @return The position of the first character that was not
     * considered

     * The Error conditions are in the error variables
  
     */
    private int execLoad( String S , int start, int end ){
        String  filename,
            varname;
        DataSet dss[];
        int     i,
            j;
        

        //---------------------- Check input conditions ------------------
        if( Debug )
            System.out.println( "In EXecLoad " + start);
        start = skipspaces(S , 1 , start);
        i = start;
        
        if( (start < 0) || (start >= S.length()) || (start >= end)){
            seterror( S.length(),"Internal ErrorSL");
            return start;
        }

        //  --------  Get the Other Arguments after the command -----------
        Vector V;
        if( S.charAt( i ) == '(')
            V = getArgs( S , i + 1 , end);
        else
            V = getArgs(S , i ,end );
        
        if( V == null )
            return perror;
        if( V.size() <=1 ){
            seterror( start, "ER_MissingArgument");
            return start;
        }
        if( perror >= 0){
            perror = start;          
            return perror;
        }
        j =((Integer)V.lastElement()).intValue(); 
        
        
        j = skipspaces( S , 1, j );


                     // Check for mismatched parenthesis 
        if( S.charAt( i ) == '(')
            if( (j >= end) || ( j >= S.length()))
                seterror( i, ER_MisMatchParens);
            else if( S.charAt( j ) != ')')
                seterror( i, ER_MisMatchParens);
            else j = skipspaces( S , 1 , j+1);
        int x = 0;
        
        
        if(Debug)
            System.out.println("Load after Arg get");

        //------------------- Process Arguments ---------------------
        try{

            //????????????? Is the first argument an array of Data Sets?
            if( V.elementAt( 0 ) instanceof DataSet[] )
                {  if(Debug)  System.out.println("Load in Dataset[]");
                DataSet DS[] = (DataSet [ ] ) V.elementAt( 0 );
                x = 1;
                varname = null;
                if( V.size( ) > 2 )
                    varname = (String ) V.elementAt ( 1 );
                x = 2;
                if( V.size( ) > 3 )
                    { seterror( start ,ER_ImproperArgument + (x+1) );
                    return j; 
                    }
                Load( DS , varname);
                if( perror >= 0 )
                    perror = start + 2;
                return j;
                }
            

            //?????????????????? Is the first argument a filename
            if(Debug) System.out.println("Load not DataSet");
            x = 1;    
            filename = (String) V.elementAt(0);
            x = 2;
            varname=null;
            
            
            if( V.size() == 3)
                varname = (String) V.elementAt(1);
            else if( V.size() > 3 )
                {seterror( start, ER_ExtraArguments);
                return start;
                }
        }
        catch( ClassCastException s)
            { seterror ( start, ER_ImproperArgument + " " +x);
            return end; 
            }
        
        
        dss = Load( filename , varname);
        
        if( perror >= 0 )
            perror = start;
        
        if( dss == null )
            Result = new Integer( 0);
        else
            Result = new Integer( dss.length );
        dss = null;
        return j;
        
     
    }

    /** 
    *   Loads an array of DataSets into the local DataSet table.
    *   @param  dss    The array of data sets to be loaded into execOneLine
    *   @param varname  the name used to refer to the elements of this DataSet.
    *               if null or improper, the DataSet title will determine 
    *               variable the name.   
    */
    public void Load ( DataSet dss[] , String varname){
        int i;
        DataSet DDs;
        if( dss == null ){
            seterror( 1000 , "Data File Improper" );
            return ;
        }
        if( dss.length <= 0 ){
            seterror( 1000 , "Data File Improper" );
            return  ;
        }
        String vname;
        for( i = 0 ; i < dss.length ; i++ ){
            DDs = eliminateSpaces( dss[i] );
            vname = DDs.getTitle();
            if( varname != null)
                if( varname.length() > 0 )
                    if( varname.toUpperCase().charAt(0) >'Z')
                        {}
                    else if( varname.toUpperCase().charAt(0) < 'A')
                        {}
                    else vname=(varname + "["+new Integer(i).
                                               toString().trim()+"]");
            /*Object X = getVal( DDs.getTitle());
              if( X != null )
              {seterror( 1000 , "DataFile already loadedX" );
              return;
              }
            */
            seterror(-1,"");
            Assign( vname , DDs);
        }
    }

    /**
     * Used by other parsers to load a file of data sets into the
     * local space
     *
     * @param filename The name of the file of data sets(.run
     * extension)<BR> the file from save command(.isd extension)<BR>
     * class(with dots) (.class extension)<BR>
     * @param varname The name these data sets will be referred to.
     * <BR> They will be called varname0, varname1, ... <BR> The
     * .class files must subclass DataSetTools.operator.  Name is from
     * getCommand
     *
     * @return     The number of data sets loaded
     *
     * Note: if varname is null, the names will be derived from the
     * filename and data set type.
     *
     */
    public DataSet[] Load( String filename , String varname ){
      DataSet[] DDs=null;
      try{
        DDs=ScriptUtil.load(filename);
      }catch(IOException e){
        seterror(1000,e.getMessage());
        return null;
      }
      Load(DDs,varname);
      return DDs;
    }

    /**
     *  Displays the information from the string S.

     * @param S The string to be executed
     * @param start The starting character
     * @param end The last character of the string to be considered
     *
     * @return The position of the first character that was not
     * considered

     * Error conditions are in the error variables
     */
    private int  execDisplay( String S, int start, int end){
        int    i,
            j;
        DataSet DS;
        Result = null;
        // ----------------- check inputs ----------------------
        i = skipspaces(S,1,start);
        if( i > end) i = end;
        if( Debug )
            System.out.print("Disp A ,i" + i);
        
        i = start;
        
        if( (start < 0) || (start >= S.length()) || (start >= end)){
            seterror( S.length(),ER_MissingArgument);
            return start;
        }


        // ------------- Get other Arguments on the line ---------------------
        Vector V = new Vector();
        if( S.charAt( i ) == '(')
            V = getArgs( S , i + 1 , end);
        else
            V = getArgs( S , i , end );
        
        if( V == null )
            return perror;
        if( V.size() == 0)
            return perror;

        if( V.size() > 4){
            seterror( start, ER_ImproperArgument +" "+V.elementAt(4) );
            return start;
        }
        j =((Integer)V.lastElement()).intValue(); 
        j = skipspaces( S , 1, j );
        

        // ------------ Check for matched Parenthesis ---------------
        if( Debug)System.out.println("Display i,j="+i+","+j+","+start+","+S);
        if( S.charAt( i ) == '(')
            if( (j >= end) || ( j >= S.length()))
                seterror( i, ER_MisMatchParens);
            else if( S.charAt( j ) != ')')
                seterror( i, ER_MisMatchParens);
            else
                j = skipspaces( S , 1 , j+1);
        if( perror >= 0)
            return perror;
        

         //-------------- Process other arguments -------------------------
        int x = 0;
        String DisplayType="IMAGE"; 
        String FrameType="External Frame";
        if( V.size() > 1)
            if( V.elementAt( 0 ) == null){
              //PC.firePropertyChange( "Display" , null , (Object)"(null)" );
                Result = ScriptUtil.display("(null)");
                return j;
            }
        if( V.size() <= 1){
            //PC.firePropertyChange( "Display" , null , (Object)"No Result" );
             Result= ScriptUtil.display("No Result" );
            return j;
        }

        try{
          //????????????????? Display a DataSet ???????????????????
          x = 1;

          DS = (DataSet) (V.elementAt(0));
          x = 2;
          DisplayType="IMAGE";
          if( V.size() >2)
              DisplayType = (String)(V.elementAt(1));
          x=3;
          FrameType ="External Frame";
          if( V.size() >3 )
              FrameType = (String) (V.elementAt(2));
          x = 4;
          Result = Display( DS , DisplayType , FrameType );
          
          if( perror >= 0 ) 
              perror = start;
          return j;
        }catch( ClassCastException s){
            //??????????? Display another type of object ????????????????????
            if( x > 1)
                if( (Result == null) || (V.size() > 2 ) ){
                    seterror( i , ER_ImproperArgument+" " + x  );
                    if( Debug){
                        if( x-1 < V.size())
                            System.out.println( "V and class = "
                               + V.elementAt(i) +","+
                                V.elementAt(i).getClass());
                        else System.out.println("Aft Display args ="
                                                + DisplayType+","+FrameType);
                    }
                    return i;
                }
        }
        Result = (Object) V.elementAt(0);
        if( Debug )
            System.out.println("In Display Res="+Result+Result.getClass());
        
        if( (Result == null) || (V.size() > 2 ) ){
            seterror( i , ER_ImproperArgument+"B" );
            return i;
        }

        Result = ScriptUtil.display( Result );

        Result = null;
        return end;
    }

    /**
     * Utility that converts a Vector to a displayable string<P>
     * Elements of a vector can be another vector, a Data Set.<Br>
     * Strings and Special strings will be quoted
     *
     * @param V The vector to be converted to its string form
     * @return A String representation of the number.
     *
     * NOTE: If the string representation is run through execOneLine,
     * hopefully the vector form is reconstructed
     */
    public static String Vect_to_String(Vector V){
        if(V == null) return "null";
        String res="[";
        
        int i;
        for( i=0; i< V.size(); i++){
            Object O= V.elementAt(i);
            if( O instanceof Vector)
                res = res+ Vect_to_String((Vector)O);
            else if ((O instanceof String) ||(O instanceof SpecialString))
                res = res +"\""+ O.toString()+"\"";
            else if( O instanceof DataSet)
                res =res+ "ISAWDS"+((DataSet)O).getTag();
            else res = res+ O.toString();
            if( i<V.size()-1)res = res+",";
        }
        res = res+"]";
        return res;
    }

    /**
     * Creates a viewer for the data set
     *
     * @param ds The data set to be viewed
     * @param DisplayType The type of display
     *
     * NOTE: DisplayType must be "IMAGE" , "Scrolled_Graph", "Table",
     * Three_D", or "Selected_Graph"<Br> FrameType must be "External
     * Frame" or "Internal Frame".
     */
    public ViewManager Display( DataSet ds , String DisplayType , String FrameType ){
      ViewManager vm = ScriptUtil.display(ds,DisplayType);
      Graphs.addElement(vm);
      vm.addWindowListener( new DisplayWindowListener() );
      return vm;
    }

    class DisplayWindowListener extends WindowAdapter{
        public void windowClosed(WindowEvent e) {
           Window W = e.getWindow();
           Graphs.removeElement( W);
           W = null;
        }

    }//DisplayWindowListener
    private int  execSave( String S , int start, int end ){
        int i = start;
        int j;
        String filename;
        if( (start < 0) || (start >= S.length()) || (start >= end)){
             seterror( S.length(),"Internal ErrorSL");
             return start;
        }
        Vector V;
        if( S.charAt( i ) == '(')
            V = getArgs( S , i + 1 , end);
        else
            V = getArgs( S , i , end );
        if( V == null )
            return perror;
        if( V.size() <=1 ){
            seterror( start, ER_MissingArgument);
            return start;
        }else if( V.size() > 3){
            seterror( start, ER_ImproperArgument+"A" );
            return start;
        }
        j =((Integer)V.lastElement()).intValue(); 
        j = skipspaces( S , 1, j );
        if( S.charAt( i ) == '(')
            if( (j >= end) || ( j >= S.length()))
                seterror( i, ER_MisMatchParens);
            else if( S.charAt( j ) != ')')
                seterror( i, ER_MisMatchParens);
            else
                j = skipspaces( S , 1 , j+1);
        if( perror >= 0)
            return perror;
        if( (j < S.length()) &&( j < end )) j = skipspaces( S , 1 , j+1);
        int x = 0;
        DataSet DS;
        try{
            x = 1;
            DS = (DataSet) (V.elementAt(0));
            x = 2;
            filename = null;
            if( V.size() >2)
                filename = (String)(V.elementAt(1));
            x=3;
            Save( DS , filename );
            if( perror >= 0) 
                perror = j;
            return j; 
        }catch( ClassCastException s){
            if( x > 1)
                if( (Result == null) || (V.size() > 2 ) ){
                    seterror( i , ER_ImproperArgument+" " + x  );
                    return i;
                }
        }
          
        return j;  
    }

    /**
     * This Method saves the data set to a file. It can now be replaced by 
     * ScriptUtil.save
     *
     * @param ds The data set to be saved
     * @param filename The filename to which the data set is to be.
     *  The Extension determines the format for saving.
     * @see ScriptUtil#save
     *
     */
    public void Save( DataSet ds , String filename ){
      try{
        ScriptUtil.save(filename,ds);
      }catch(IOException e){
        seterror(1000,e.getMessage());
      }catch(IllegalArgumentException e){
        seterror(1000,e.getMessage());
      }catch(IndexOutOfBoundsException e){
        seterror(1000,e.getMessage());
      }
    }

    
    /**
     * Send information to all IObservers
     * Now uses two arguments.
     * 1 arg --> IObserver.update( this, arg1)
     * 2 args--> IObserver.update( arg2,arg1)

     * @param S The string to be executed. It starts with the Send command. 
     * @param start The starting character
     * @param end The last character of the string to be considered
     *
     * @return The position of the first character that was not
     * considered

     * Error conditions are in the error variables
     */
    private int execSend(String S, int start, int end){
        int i, j;
        i = skipspaces( S , 1 , start );
        if( (i < 0) || ( i >= S.length()) || ( i >= end )){
             seterror( start , ER_MissingArgument);
             return i;
        }
        //--------------------- Get other arguments on the line ---------------
        Vector args;
        if( S.charAt(i) == '(' )
            args = getArgs( S , i + 1 , end );
        else
            args = getArgs( S , i  , end );
        if( args == null){
            return i;
        }

        //--------------------- Check for various errors -----------------
        if( (args.size() < 2) || (args.size()>3)){
            seterror( i, "Internal Error 7A");
            return i;
        }
        try{
            j = ((Integer)(args.lastElement())).intValue();
        }catch( Exception uu){
            seterror( i, "Internal Error 7A");
            return i;
        }
        if( S.charAt( i ) == '(')
            if( (j < 0 ) || ( j >= S.length() ) || ( j >= end )){
                seterror ( j , ER_MisMatchParens );
                return j;
            }else if( S.charAt(j ) != ')'){
                seterror ( j , ER_MisMatchParens );
                return j;
            }else{
                j++;
            }
        j = skipspaces( S , 1, j);   
        if(Debug)
            System.out.print("Send"+perror+","+j);
        if( perror >= 0 )
            return S.length() + 2;
        if(Debug)
            System.out.print("Send after error");

        //------------------------ Process arguments ----------------------------
        Object arg1,arg2;
        if( args.size() <= 2){
            arg1=this;
            arg2= args.firstElement();
        }else{
            arg2= args.firstElement();
            arg1= args.elementAt(1);
        }
       
       
        OL.notifyIObservers( arg1,arg2 );
        Result = null;
        return end;
    }

    /**
     * Sends the data set to all Iobservers
     *
     * @param   ds    The data set that is to be sent
     * Not implemented yet. 
     * @see  gov.anl.ipns.Util.Messaging.IObserverList#addIObserver(gov.anl.ipns.Util.Messaging.IObserver) addIObserver  
     *
     */
    public void Send( DataSet ds){
      OL.notifyIObservers( this,ds );
    }




   //************************** Expression Parsing**************************
   //
    /**
     * First called to lex, parse, and execute an arbitrary expression.
     * Does whole expression with And's, Or's, Not's, <, <=, and
     * Algebraic Expressions.

     *
     *  This method handles processing the And and Or commands.  It calls
     *  execNonAnOrExpr to handle the expression before and after the AND or OR

     * @param S The string to be executed. 
     * @param start The starting character. MUST be a start of an expression
     * @param end The last character of the string to be considered
     *
     * @return The position of the first character that was not
     * considered.  This should be end or the position of a ),], or :. 
     * S.charAt(end) should be one of these expression terminating symbols 
     * or the end of the string S.
     */
    private int execExpr(String S, int start, int end){
        int i;
        int j;
        Object Res1;
        boolean done;

        // -------------- initialize and error check
        Result = null;
        if(Debug)
             System.out.println("ExExpr start="+start);
        i=skipspaces(S,1,start);
        Result= null;
        if(i>=end) return i;
        if(i>=S.length())return i;
                
        //  ------------ execute to an And or and Or command ----------------

        j=execNonAndOrExpr(S, i, end);


             //----------------- check for errors -------------
        if(perror>=0) return perror;
        char op=0;
        j=skipspaces(S, 1, j);
        if( j >= end) return j;
        if(j >= S.length()) return j;

        //?????????????? Does the rest of the uparsed string
        //                      start with an And or an or ????????????
        if(S.substring(j).toUpperCase().indexOf("AND")==0){
            op='#'; j=j+3;
        }else  if(S.substring(j).toUpperCase().indexOf("OR")==0){
            op='|';j=j+2;
        }else{
            //seterror(j,ER_IllegalCharacter+"U"); could be left paren , etc
            return j;
        }


        if( (skipspaces(S,1,j)>=end)||(skipspaces(S,1,j)>=S.length()) ){
            seterror(j,ER_MissingArgument+"U1"); return j;}
        if(" \t(+-".indexOf(S.charAt(j))>=0){
        }else{
            seterror(j,ER_IllegalCharacter+"U2");
            return j;
        }


        //------------ Get other operand then 
        //                   process the And or Or statement ------------------
        Res1=Result;
        done=false;
        if(Debug) 
             System.out.println("in ExecExprA"+j);
        while(!done){

            j=execNonAndOrExpr(S, j, end);

            if(Debug)
                  System.out.println("XX perror and j"+perror+","+j);
            if(perror>=0) 
                  return perror;

            // Now Execute the And or Or operation. Result contains result
            operateArith( Res1,Result,op);  
            op=0;
            j=skipspaces(S, 1, j);
            if( j >= end) 
                  return j;
            if(j >= S.length()) 
                  return j;

            if(Debug) 
                  System.out.println("Left="+S.substring(j).toUpperCase());

            //????????????? Is there another And or Or left ??????????????????
            if(S.substring(j).toUpperCase().indexOf("AND")==0){
                op='#';
                j = j + 3;
            }else  if(S.substring(j).toUpperCase().indexOf("OR")==0){
                op='|';
                j = j + 2;
            }else{
                op=0;
            }
            if(Debug) 
                  System.out.println("YY op,j="+op+","+j);
            if(op == 0) 
                  done = true;
            else if( j >= end) 
                  done = true;
            else if( j >= S.length()) 
                  done = true;
            Res1= Result;
            
        }

        return j;     
    }



    /**
     * executes to an And or an Or command. Takes care of the NOT operation 
     * if it is the first operation after the start position of String S.
     *  
     * @param S The string to be executed. 
     * @param start The starting character. Must be the start of an expression
     * @param end The last character of the string to be considered
     *
     * @return The position of the first character that was not
     * considered. Here the next statement should be an And or and Or or the 
     * end of the line or ) or .... S.charAt(end) should be one of these 
     * expression terminating characters.
     *
     * This method calls execNonAndOrNotExpr to parse S before any AND, OR, 
     * and NOT commands.
     */
    private int execNonAndOrExpr( String S, int start, int end){
        int j;
        start=skipspaces(S , 1 , start );
        // Check for NOT
        Result = null;
        if(Debug) 
            System.out.println("exenon&| expr,start="+start);
        if(start >= end) 
            return end;
        if(start >= S.length()) 
            return end;
    

        if( S.substring(start).toUpperCase().indexOf("NOT")==0){
            if((start+3 >=end) || (start + 3 >= S.length())){
                seterror(start + 3, ER_MissingArgument+"V"); 
                return start+3;
            }
            if( " \t(".indexOf(S.charAt(start+3))>=0){

                 j=execNonAndOrExpr(S, start+3,end);
                 if(Debug)
                       System.out.println("in NOT case"+perror+","+Result);

                 if(perror>=0) 
                       return perror;
                 if( Result == null){
                     seterror(start+3, ER_ImproperArgument+"V1");
                     return start+4;
                 }else if(Result instanceof Boolean) 
                     Result = new Boolean( !((Boolean)Result).booleanValue());
                 else if( Result instanceof Integer)
                     if( ((Integer)Result).intValue()==0)
                         Result = new Boolean(false);
                     else Result = new Boolean(true);
                 else{
                     seterror(start+3, ER_ImproperArgument+"V2");
                     return start+4;
                 }
            }else j=execNonAndOrNotExpr(S, start,end); 
        }else j=execNonAndOrNotExpr(S, start,end);  
        
        return j;
    }
    



    /*
     * Executes String S from start to an AND, OR, or NOT command. This method
     * is also responsible for executing the comparison operations, <, <=,>,
     * >=, <>, and =
     * 
     * @param S The string to be executed. It starts with the Send command. 
     * @param start The starting character. Must be a start of an expression
     * @param end The last character of the string to be considered
     *
     * @return The position of the first character that was not
     * considered. 
    */
    private int execNonAndOrNotExpr( String S, int start, int end){
        int j;
        start= skipspaces( S, 1 , start);
        Result =null;

        // ------------- Check for Errors ---------------------
        if( (start >= end) || (start >= S.length())||( start < 0)) 
            return start;
        if(Debug)
              System.out.println("in execnon&|!, start="+start);

        // ---------- Execute nonLogical expression
        //                                Check for errors -------------
        j=execArithm( S, start, end);

        if(Debug) 
              System.out.println("in execnon&|!A j="+j+","+perror);
        if( perror >= 0 ) 
              return j;
        j = skipspaces( S, 1, j);
        if( (j >= end) || (j >= S.length()) || (j < 0) ) 
              return j;
        if(Debug) 
              System.out.println("in execnon&|!Bj="+j+","+perror);
        
        if("=<>".indexOf(S.charAt(j))<0){
            //seterror( j, ER_IllegalCharacter+"W");  could have been true or false
            return j;
        }
        if(Debug) 
              System.out.println("in execnon&|!C j="+j+","+perror);

        if( (j+1 >= S.length()) ||(j+1 >= end)){
            seterror( j+2 , ER_MissingArgument+"W1");
            return j+2;
        }

        //-------------- Check if next operation is a Comparison-------------- 
        // coding op= <,>,= same  <=, >= --> the 5th character  
        //  after < and > resp,::<> --> 5th char after =
        char op=S.charAt(j);
        if( "=>".indexOf(S.charAt(j+1))>=0){
            if(S.charAt(j+1)=='='){
                if(op != '=')op = (char)((byte)op + 5);
            }else 
                op =(byte)'='+5;
            j++;
        }
         
        j++;
        if(Debug) 
              System.out.println("in execnon&|!D j,op="+j+","+op);
        
        Object Res1=Result;   //Save previous result

        //------------ Get 2nd argument for the comparison --------------------
        j=execArithm(S , j , end );
        if(Debug) System.out.println("in execnon&|!E j="+j+","+perror);
        
        if(perror >=0) 
              return j;
        j= skipspaces(S, 1, j);
        operateCompare(Res1,Result, op);
        if(perror >=0) 
              perror = j;
        return j;
    }


   
    /**
     * Result contains the first operand or if null executes the whole
     * expression.  Executes until there is an AND, OR, NOT, or comparison
     * operation. Also this method is responsible for parsing the ":" (range) 
     * operation.
     *
     * @param S The string to be executed.  
     * @param start The starting character
     * @param end The last character of the string to be considered
     *
     * @return The position of the first character that was not
     * considered
     * 
     * This Method keeps executing one term, using the method execOneTerm, and
     * combining them.
     */
    private   int execArithm(String S, int start, int end){
        int i;
        int j;
        Object R1;
        boolean done;
        Result = null;
        if(Debug)
              System.out.println("ExArithm start="+start);
        i=skipspaces(S,1,start);
        Result= null;
        if(i>=end) 
              return i;
        if(i>=S.length())
              return i;

        j=execOneTerm(S, i, end);
        if(Debug)
              System.out.println("ExArithA"+Result+","+perror+","+j);
        if( perror >= 0 ) 
              return perror;
        R1=Result;  //Saves Result
        

        i=j;
        done = false;
        if( (i >= end) ||  (i >= S.length()) ||  (i < 0) )
            done = true;
        else if( "),]<>=".indexOf( S.charAt( i ) ) >= 0 )
            done = true;
        else if("+-&:".indexOf(S.charAt(i))<0)
            done = true;
        if(Debug)
              System.out.println("ExArithB,done,perror,char"+done+","
                                    +perror+","+j);

        //------- Keep getting the next term and combining result ------------
        while( !done ){

            //??????????????? Is operation a : (Range operation) ?????????
            if(S.charAt(i)==':'){
                Object R = Result;
                j=execArithm(S, i+1, end);   // Get Second operand to the ":"
                if( perror >= 0) 
                      return perror;
                if( (j >= end) ||(j>=S.length())){

                }else if("),]<>=".indexOf(S.charAt( j ))< 0){
                    seterror( j, ER_IllegalCharacter);
                    return j;
                }

                operateArith(R, Result, ':');   // do the operation
                if( perror >=0)
                      return perror;
                else 
                      return j;
            }
            
            //--------------- Get next term ---------------------
            j = execOneTerm( S , i + 1 , end );
            if(Debug)
                  System.out.println("ExArithC"+Result+","+perror);
            
            if( perror < 0 ){
                operateArith( R1 , Result , S.charAt( i ) ); //Operate on it
                R1 = Result;
                if( perror >= 0){
                    perror = i + 2 ;
                    return perror;
                }
            }else
                return perror;

            //----------------- Get ready for the next term -----------------
            i = j;
            done = false;
            if( ( i >= end ) ||  ( i >= S.length() ) ||  ( i < 0 ) )
                done = true;
            else if( "),]<>=".indexOf( S.charAt( i ) ) >= 0 )
                done = true;
            else if("+-&:".indexOf(S.charAt(i))<0)
                done = true;
            
        }//While !done      
        return i;
    }
    



 

   /**
     * Executes one term. This method keeps executing one Factor,
     * method execOneFactor, and combining the results.
     *
     * The global variable Result contains the value from the previous
     * Factor.
     * 
     * @param S The string to be executed.  
     * @param start The starting character
     * @param end The last character of the string to be considered
     *
     * @return The position of the first character that was not
     * processed.  This should be a +,-, or end of expression condition
     */
    private int execOneTerm( String S , int start , int end){ 
        int i,
            j;
        Object R1;
        
        i = skipspaces( S , 1 , start );
        if( Debug )
            System.out.println("in Ex1Trm,st=" + start + "PP1");

        i = execOneFactor( S , i , end );
        if(Debug)System.out.println("Ex1trmA"+perror+","+Result+","+i);
        i = skipspaces( S , 1 , i );
        
        if( perror >= 0 ){
	    return perror;
        }
        R1 = Result;
        boolean done = (i < 0) ||  (i >= S.length()) ||  (i >= end);
        if( !done )
            if( "+-)&<>,=:]".indexOf( S.charAt( i ) ) >= 0 )
                done = true;
            else if("*/".indexOf(S.charAt(i))<0) 
                  done = true;
        if(Debug)
              System.out.println("Ex1trmB"+perror+","+i);
        
        //-------- Keep getting the next Factor ------------------------
        while( !done ){
            j = execOneFactor( S , i + 1 , end );
            j = skipspaces( S , 1 , j );
            if(Debug)
                  System.out.println("Ex1trmC"+perror);
            
            if( perror >= 0 ){
                return perror;
            }
            
            // Now execute the product or quotient operation
            operateArith( R1 , Result , S.charAt( i ) );   
            if( perror >= 0 ) { 
                  perror = i; return perror;
            }
            i = j;
            done = (i  < 0) ||  (i >= S.length()) ||  (i > end);
            if( !done )
                if( "+-)&<>=,:]".indexOf( S.charAt( i ) ) >= 0 )
                    done = true;
                else if("*/".indexOf(S.charAt(i))<0) 
                    done = true;
            
            R1 = Result;
        }
        return i;
    }



    /**
     * Lexes, parses, and calculates the value of One Factor. This includes
     * Lexing, parsing and calculating array references, powers, Strings,
     * variable references, constans, and operator calls along with expressions
     * enclosed in paranthesis.
     *
     * The variable Result  contains the product so far for this term as
     * it is parsed.
     * 
     * @param S The string to be executed.  
     * @param start The starting character
     * @param end The last character of the string to be considered
     *
     * @return The position of the first character that was not
     * considered. This should be a +,-,AND,OR,NOT or an end of expression
     * condition.
     */
    private int execOneFactor( String S , int start , int end ){//go til * or /
        int i,
            j,
            j1;

        if(Debug)
            System.out.print("Exec1Fact st"+start+","+end);
        i = skipspaces( S , 1 , start );
        if( (i >= S.length()) || ( i >= end ) ){
	    seterror( i , ER_MissingArgument +"B");
            return i;
        }
        if( i < 0 ){
	    seterror( i , "internal error" );
            return S.length() + 5;
        }
        if( Debug )
            System.out.println("in exe1Fact start=" + i);

        //???????????????????? Is it an Array reference ????????????????
        if(S.charAt(i) == '['){
            Vector V = getArgs(S,i+1, end);
            if( V == null)
                return i;   //error occured
            int n= ((Integer)V.lastElement()).intValue();
            if( (n >= end) || ( n < 0)|| (n>=S.length())){
                seterror( n, ER_MissingArgument);
                return n;
            } 
            if( S.charAt(n) != ']'){
                seterror( n, ER_MissingBracket);
                return n;
            }
            if( V.size()>0)
                V.removeElementAt(V.size()-1);
            Result = V;
            return n+1;
        }
        //?????????????? Does it start with a String ??????????????????????
        if( S.charAt( i ) == '\"' ){
            String S1 = getString( S , i );
            if( perror >= 0){
                perror = i;
                return i;
            }
            
            j = i + S1.length() + 2;
            
            if(Debug)
                System.out.println("aft getStr"+S1+","+i+","+j);
            if( (j > end) || ( j > S.length())){
                perror = j;
                serror = ER_MisMatchQuote ;
                return j;
            }
            Result = S1; 
            if(perror < 0)	  
                return skipspaces( S , 1 , j );		  
            else{
                perror = i;
                return i;
            }
        }
        //????????????? Does it start with unary + or unary 0 ????????????????
        if( ( S.charAt( i ) == '-' ) ||  ( S.charAt( i ) == '+' ) ){
            j = execOneFactor( S , i + 1 , end );                
            if( perror >= 0 )
                return perror;
            Object R3 = Result;
            
            if( S.charAt( i ) == '-' )
                operateArith( R3 , new Integer( -1 ) , '*' );
            if( Debug )
                System.out.println("in un-=exe1Fac Res=" + R3 + "," + Result);
            if( perror >= 0 ){
                perror = i;
                return perror;
            }
            j = skipspaces( S , 1 , j );
            
            return j;
        }

        if("*/".indexOf(S.charAt(i))>=0){
            seterror(i, ER_IllegalCharacter);
            return i;
        }
        //???????????? Does it start with a parenthesis ????????????????????
        if( S.charAt(i) == '(' ){
            if( Debug )
                System.out.println("in LParen,i=" + i);
            j = execute( S , i + 1 , end );
            if( ( j < 0 ) ||  ( j >= S.length() ) || ( j >= end ) ){
                seterror( i , ER_MisMatchParens );
                return i;
            }else if( S.charAt( j ) != ')' ){
                seterror( i , ER_MisMatchParens );
                return i;
            }
            j= skipspaces( S , 1 , j + 1 );
            if( j>= end ) return j;
            if( j >= S.length()) return j;
            if( j < 0 ) return j;
            if( S.charAt(j) != '^') return j;
            Object R3= Result;
            j = execOneFactor( S , j+1 , end );
            if( perror >= 0) return j;
            operateArith( R3 , Result , '^' );
            if(perror >= 0) perror = j;
            return j;
        } 

        //--------------------- New Factor case -------------------------        
        j = findfirst( S , 1 , i , "\t+-*<>(=&^/):[]{},\" " , "" );
        if( j > end)
            j = end;
        j1 = skipspaces( S , 1 , j );
        if( j1 > end) 
            j1 = end;
               //------------------ Check for errors -------------------- 
        if( ( j < 0 ) ||  ( j > S.length() ) ||  ( j1 > S.length() )
                                             || ( j > end) || ( j1 > end) ){
            seterror( S.length() + 3 , "internalerrorp" );
            return S.length() + 3;	
        }else if( (j1< S.length()) && ( j1 < end))
            if(S.charAt(j1) == '\"'){
                seterror( j , ER_IllegalCharacter+"K");
                return j;
            } 
        
        
        String C;
        C = S.substring( i , j );
        //----------------- Check if array reference follows -----------------
        if ( (j1 >= 0) && (j1 < S.length()) && ( j1 < end ) )
            if(  S.charAt( j1 ) == '[' ){                // Check for []
                int j2=findfirst(S,1,j1+1,"]","[]");
                if((j2>=end) ||( j2 >= S.length())){
                    seterror(j1,ER_MissingBracket +" at "+j1);
                    return j1;
                }
                j1=j2+1;
                C = S.substring(i,j1);
                j=j1;
            }

        //??????????????????? Is factor a function call ???????????????????
        if( (j1 < S.length()) && ( j1 < end ) )
            if( S.charAt( j1 ) == '(' ){//function
                j = execOperation( C , S , j1 , end );
                j = skipspaces( S , 1 , j);
                if( (j >= end) || (j >= S.length()) ) 
                    return j;
                if( j < 0) 
                    return j;
                if( S.charAt( j) != '^') 
                    return j;
                Object R3= Result;
                j = execOneFactor( S , j+1 , end );
                if( perror >= 0) 
                    return j;
                operateArith( R3 , Result , '^' );
                if(perror >= 0) 
                    perror = j;
                return j;
                
            }else if( "([{}".indexOf( S.charAt( j1 ) ) >= 0 ){
                seterror( j , ER_IllegalCharacter+"L" );
                return j;
            }
      
        boolean valgot = false ;
        if(C.toUpperCase().equals("AND") ||C.toUpperCase().equals("OR")
                                             ||C.toUpperCase().equals("NOT")){
            seterror(j,ER_IllegalCharacter+"L1");
            return j;
        }
        if(Debug)
            System.out.println("Ex1Fact ere get 1 numb"+perror);
        //???????????????? Is factor an Integer ???????????????????????
        try{
            Result = new Integer( C );
            if(Debug)
                  System.out.println("Result="+C+","+perror);
            if( (j < S.length()) && ( j < end )){
                if( S.charAt(j) != '^')return j;
                valgot = true; 
            }else return j;
	 }catch( NumberFormatException s ){
             // let it drop on the floor
         }

        //?????????????? Is the Factor a Float ?????????????????????????????
        if(!valgot)
            try{
                Result = new Float( C );
                if( (j < S.length()) && ( j < end )){
                    if( S.charAt(j) != '^')
                        return j;
                    valgot = true; 
                }else return j;
            }catch( NumberFormatException s ){
                // let it drop on the floor
            }
      

        //???????????? Does the Factor refer to a Variable ???????????
        if(!valgot){
            Result = getVal ( C);
            if( perror >= 0)
                perror = i;
            if( (j < S.length()) && ( j < end )){
                if( S.charAt(j) != '^')return j;
                valgot = true;
            }else return j;
        }

        //???????????????????? Does the Factor have an ^ in it ????????????????????
        if( (perror < 0))
            if( (j < S.length()) && ( j < end ))
                if( S.charAt(j) == '^' ){
                    Object R3= Result;
                    j = execOneFactor( S , j+1 , end );
                    if( perror >= 0) return j;
                    operateArith( R3 , Result , '^' );
                    if(perror >= 0) perror = j;
                    return j;
                }
        return j;
   
    }




    /**
     * Routine to get Arguments in arrays, and function calls.

     * @param S The string to be executed.  
     * @param start The starting character. This must be the first character
     *          past the ( of a function call.
     * @param end The last character of the string to be considered. This should be
     *     a )(if function call with starting () or ] if array reference.
     *
     * @return a Vector containing the value of the arguments followed
     *        by the next char in S to be processed , or null if there is an error
     * 
     */
    private Vector getArgs( String S , int start , int end ){
        if( ( start < 0 ) ||  ( start >= S.length() ) || ( start >= end ) ){
            seterror( S.length() + 2 , "internal errorp" +S);
            return null;
        }
        if( Debug)
            System.out.println( "getArgs ,start= "+ start);
        Vector Args = new Vector();
        int i , j;
        boolean done;
        i = skipspaces( S , 1 ,start) ;
        done = (i >= S.length()) ||( i >= end);
        if(!done) 
            if("])".indexOf(S.charAt(i))>=0)
                done =true;
        while( !done ){
            Result = null;
            j = execExpr( S , i , end );
            if(Debug)
                System.out.println("getArgsA"+Result+","+perror+","+j);
            if( perror >= 0 )
                return null;
            if(Result instanceof SubRange)
                Args.addAll( ((SubRange)Result).Vect());
            else
                Args.addElement( Result );
            if( ( j >= S.length() ) || ( j >= end ) ){
                Args.addElement(new Integer(j));
                return Args;
            }
            if( j < 0 ){
                seterror( S.length() + 3 , "InternalerrorG" );
                return null;
            }
            if( S.charAt( j ) == ')' )
                done = true;
            else if( S.charAt(j) == ']')
                done = true;
            else if( (S.charAt( j ) != ',') ){
                seterror( j , ER_IllegalCharacter+"B");
                return null;
            }
            if( S.charAt(j) == ',')
                i = skipspaces(S , 1, j+1 );
            else
                i = j;
        }//while not done
        Args.addElement(new Integer( i ));
        return Args;
        
    }
    
    
    private boolean CanOp( Object Value){
      if( Value == null )
         return false;
      if( Value instanceof Integer)
         return true;
      if( Value instanceof Float)
         return true;
      if( Value instanceof String)
         return true;
      if( Value instanceof DataSet)
         return true;
      if( Value instanceof Vector)
         return true;
      if( Value instanceof Boolean)
         return true;
      return false;
    }

    //************************** Execute operation**************************
    /** 
    *  Executes the Comparison operators( <, > , <=, etc.). 
    * @param LeftValue    The first operand
    * @param RightValue    the second operand
    * @param operation     the operation to be executed
    *
    * @return    Nothing. The Global variable Result will contain
    *      the result

    * 
    */
    public void operateCompare( Object LeftValue,Object RightValue, char operation){

      if(!CanOp(LeftValue)){
        seterror(1000, ER_IMPROPER_DATA_TYPE);
        return;
      }
      if(!CanOp(RightValue)){
         seterror(1000, ER_IMPROPER_DATA_TYPE);
         return;
      }
        Result = null;

        if( (LeftValue==null) ||(RightValue==null)){
            seterror( 1000, ER_ImproperArgument);
            return;
        }
        if(Debug)
              System.out.println("in op comp args="+
                               LeftValue+","+RightValue+","+operation);

        //---------------------- Data Type Boolean -----------------------
        //             ----Mixed Types Convert-----------------
        if(LeftValue instanceof Boolean)
            if( !(RightValue instanceof Boolean))
                if( RightValue instanceof Integer){
                    if( ((Integer)RightValue).intValue()==0)
                        RightValue=new Boolean(false);
                    else 
                        RightValue=new Boolean(true);
                }else if( (RightValue instanceof String) && (operation=='&')){
                    Result = LeftValue.toString() + (String)RightValue;
                    return;
                }else{
                    seterror(1000, ER_ImproperDataType);
                    return;
                }

        if(RightValue instanceof Boolean)
            if( !(LeftValue instanceof Boolean))
                if( LeftValue instanceof Integer){
                    if( ((Integer)LeftValue).intValue()==0)
                        LeftValue=new Boolean(false);
                    else 
                        LeftValue=new Boolean(true);
                }else if( (LeftValue instanceof String)&&(operation=='&')){
                    Result = (String)LeftValue+ RightValue.toString();
                    return;
                }else{
                    seterror(1000, ER_ImproperDataType);
                    return;
                }

    
        //          ---------- Both Boolean ------------
        if(LeftValue instanceof Boolean){
            if( LeftValue.equals(RightValue)){
                if("<>".indexOf(operation)>=0) 
                    Result = new Boolean(false);
                else if( (byte)operation == (byte)'='+5)
                    Result = new Boolean(false);
                else  
                    Result = new Boolean(true);
                return;

            }else if( ((Boolean)LeftValue).booleanValue()){  // false < true
                if(">".indexOf( operation )>=0) 
                    Result= new Boolean(true);
                else if("<=".indexOf( operation )>=0) 
                    Result = new Boolean(false);
                else if((byte)operation-5 ==(byte)'<' ) 
                    Result = new Boolean(false);
                else 
                    Result = new Boolean(true);
                return;

            }else
                if((operation=='<')||((byte)operation-5 == (byte)'<'))
                    Result= new Boolean(true);
                else if( (operation=='>')||((byte)operation-5 == (byte)'>'))
                    Result= new Boolean(false);
                else if( (byte)operation == '=' +5) 
                    Result = new Boolean(true);
                else if( operation == '=')
                    Result = new Boolean(false);
            return;
        }     

        //----------------------------- Data Type DataSet -------------------  
        if(LeftValue instanceof DataSet)
            if( !(RightValue instanceof DataSet)){
                seterror(1000,ER_ImproperDataType);
                return;
            }

        if(RightValue instanceof DataSet)
            if( !(LeftValue instanceof DataSet)){
                seterror(1000,ER_ImproperDataType);
                return;
            }

        if( LeftValue instanceof DataSet){
            if( (operation =='=') && (LeftValue.equals(RightValue)))
                Result= new Boolean(true);
            else if( ((byte)operation - 5 == (byte)'='))
                if(LeftValue.equals(RightValue)) 
                    Result= new Boolean(false);
                else 
                    Result= new Boolean(true);
            else{
                seterror(1000, execOneLine.ER_NoSuchOperator);
                return;
            }
            return;
        }




        //------------------------------- Data Type String --------------------------
        if(LeftValue instanceof String)
            if( (RightValue instanceof Vector)&&( operation == '&'))
                RightValue= execOneLine.Vect_to_String((Vector)RightValue);
            else if(!(RightValue instanceof String)) 
                RightValue = RightValue.toString();

        if(RightValue instanceof String)
            if( (LeftValue instanceof Vector) && ( operation == '&'))
                LeftValue= execOneLine.Vect_to_String((Vector)LeftValue);
            else if(!(LeftValue instanceof String)) 
                LeftValue = LeftValue.toString();
        
        if( (RightValue instanceof String)  &&(LeftValue instanceof String)){
            String S1 = LeftValue.toString();
            String S2 = RightValue.toString();
            int i,
                Less;  // Less = 1 means true, -1 means false else undefined.
            Less = 2;
            //Should use string compare
            for( i = 0 ; (Less >= 2)&&
                         (i < java.lang.Math.min( S1.length() , S2.length())) ; 
                 i++)
                if( S1.charAt(i) < S2.charAt(i))
                    Less =1;
                else  if( S1.charAt(i) > S2.charAt(i))
                    Less =-1;
            
            if(Less >= 2)
                if( S1.length() < S2.length())
                    Less = 1;
                else if(S1.length()>S2.length())
                    Less =-1;
                else Less =0;
            
            if( (operation =='=') ||((byte)operation -5 ==(byte)'<') || 
                                                      ((byte)operation-5 ==(byte)'>'))
                if( Less ==0){ 
                    Result = new Boolean( true);
                    return;
                }

            if( (byte)operation-5==(byte)'=') 
                if(Less ==0) 
                    Result = new Boolean(false);
                else 
                    Result = new Boolean(true);
            else if((operation == '<') ||((byte)operation -5 == (byte)'<'))
                if( Less <=0) 
                    Result = new Boolean(false);
                else 
                    Result = new Boolean(true);
            else if(  operation == '=')
                if( Less ==0) 
                    Result= new Boolean(true);
                else 
                    Result= new Boolean(false);
            else 
                if( Less >=0) 
                    Result = new Boolean(false);
                else 
                    Result = new Boolean(true);

            return;
        }


        //--------------------------- Data Type Number ------------------------
        if( !(LeftValue instanceof Number) ||!(RightValue instanceof Number)){
            seterror( 1000, ER_NoSuchOperator);
            return;
        }
        
        operateArith(RightValue,LeftValue,'-');
        if(perror >=0) return;
        int Less = 0;
        if( ((Number)Result).floatValue()>0) 
            Less =1;
        else if(((Number)Result).floatValue() <0) 
            Less =-1;
        else 
            Less =0;

        if(Debug)
            System.out.println("in opcomp Res and Less ="+
                                            Result+","+Less +","+operation);

        if( ( operation =='=') ||((byte)operation -5 ==(byte)'<') ||
                                ((byte)operation-5 ==(byte)'>'))
            if( Less ==0){ 
                Result = new Boolean( true);return;
            }
        
        if( (byte)operation-5==(byte)'=') 
            if(Less  ==0) 
                Result = new Boolean(false);
            else 
                Result = new Boolean(true);
        else if((operation == '<') ||(byte)operation -5 == (byte)'<')
            if( Less <=0)  
                Result = new Boolean(false);
            else 
                Result = new Boolean(true);
        else if( operation == '=')
            if( Less  ==0) 
                Result= new Boolean(true);
            else 
                Result= new Boolean(false);
        else 
            if( Less  >= 0) 
                Result = new Boolean(false);
            else 
                Result = new Boolean(true);
        return;
   } 


    /**
    *   Executes the logical operations, AND and OR
    *
    *   @param R1  the left operand
    *   @param R2  the right operand
    *   @param  operation  The operation. '#' means OR otherwise  AND
    *
    *   @return   The result is in the Global variable Result
    */

    private void operateLogic(Object R1 , Object R2 , char operation ){
      if(!CanOp(R1)){
        seterror(1000, ER_IMPROPER_DATA_TYPE);
        return;
      }
      if(!CanOp(R2)){
         seterror(1000, ER_IMPROPER_DATA_TYPE);
         return;
      }
        Result = null;
        if( R1 instanceof Integer)
            if( ((Integer)R1).intValue()==0)
                R1= new Boolean(false);
            else R1=new Boolean(false);
        
        if( R2 instanceof Integer)
            if( ((Integer)R2).intValue()==0)
                R2= new Boolean(false);
            else R2=new Boolean(false);

        if( !(R1 instanceof Boolean)  || !(R2 instanceof Boolean)){
            seterror(1000, ER_ImproperDataType);
            return;
        }
        if(operation =='#')
            Result =new Boolean( ((Boolean)R1).booleanValue() 
                                 && ((Boolean)R2).booleanValue());
        else
            Result =new Boolean( ((Boolean)R1).booleanValue() 
                                 || ((Boolean)R2).booleanValue());
  }

    /** 
     * Executes an arithmetic operation.
     *
     * Can be used by other parsers. Use the getResult() method to
     * determine the value
     *
     * @param LeftValue   The left operand
     * @param RightValue the right operand
     * @param operation +,-,*, or /
     *
     * NOTE: The data types will converted if possible and the
     * appropriate add, subtract,... will be used Use getErrorCharPos
     * to determine if an error occurred<br>
     * @see #getErrorCharPos()
     * @see #getResult()
     */
    public void operateArith( Object LeftValue , Object RightValue , char operation ){
    if(!CanOp(LeftValue)){
      seterror(1000,ER_ImproperDataType+" "+ operation);
     return;
    }
    if( !CanOp(RightValue)){
       seterror(1000,ER_ImproperDataType+" "+ operation);
       return;
    }
    
      
	if( Debug )
            System.out.println("in Op ARith o=" + operation);
        if("#|".indexOf( operation ) >=0 ){
            operateLogic(LeftValue,RightValue, operation );
            return;
        }

        //------------------------- SubRange(:) op ----------------------------
        if( operation ==':'){
            if(LeftValue instanceof Integer)
                if(RightValue instanceof Integer){
                    Result= new SubRange(((Integer)LeftValue).intValue(), 
                               ((Integer)RightValue).intValue());
                    return;
                }
            seterror(1000, ER_ImproperDataType+" "+ operation );
            Result = null;
            return;
        }

        //-------------------------- & ----------------------
        if( (LeftValue instanceof Boolean) ||(RightValue instanceof Boolean))
            if( ( operation != '&') || !(LeftValue instanceof String 
                                    || RightValue instanceof String)){
                seterror( 1000, ER_ImproperDataType);
                return ;
            }else{
                if( LeftValue instanceof String) 
                    Result = (String)LeftValue +RightValue.toString();
                else 
                    Result = LeftValue.toString()+(String)RightValue;
                return;
            }

            
        if( (LeftValue instanceof DataSet) &&(operation != '&') ){
            operateArithDS( LeftValue , RightValue , operation );
            return;
        }
	if( (RightValue instanceof DataSet) &&( operation !='&') ){
            operateArithDS( LeftValue , RightValue , operation );
            return;
    }
    
        if( (LeftValue instanceof DataSet) || (RightValue instanceof DataSet) )
           if( !(LeftValue instanceof String )&&!(RightValue instanceof String)){
           
            seterror(1000, ER_ImproperDataType+" "+ operation );
            Result = null;
            return;
        }

        if( (LeftValue instanceof Vector) || (RightValue instanceof Vector))
            if( ( operation != '&') || !(LeftValue instanceof String || 
                                         RightValue instanceof String)){
                operateVector(LeftValue, RightValue, operation);
                return;
            }else{
                if( LeftValue instanceof String)
                    Result = (String)LeftValue +
                               execOneLine.Vect_to_String((Vector)RightValue);
                else
                    Result = execOneLine.Vect_to_String((Vector)LeftValue)+
                                           (String)RightValue;
            }
   
       //---------------------- +-*/^<> operations -----------------------
	if( "+-/*<>^".indexOf( operation ) >= 0 ){
            if( LeftValue instanceof String ){
                Integer II;
                try{
                    II = new Integer( (String)LeftValue );
                    LeftValue = II;
                }catch ( NumberFormatException s ){
                    try{
                        LeftValue = new Float( (String)LeftValue );
                    }catch( NumberFormatException t ){
                        seterror( 1000 , ER_NumberFormatError );
                        return;
		    }
                }
            }
            if( RightValue instanceof String ){
                Integer II;
                try{
                    II = new Integer( (String)RightValue );
                    RightValue = II;
                }catch ( NumberFormatException s ){
                    try{
                        RightValue = new Float( (String)RightValue );
                    }catch( NumberFormatException t ){
                        seterror( 1000 , ER_NumberFormatError );
                        return;
                    }
                }
	    }
            if( LeftValue instanceof Integer )
                if( RightValue instanceof Float )
                    LeftValue = 
                       new Float( ( ( Integer )LeftValue ).floatValue() );
            
            if( RightValue instanceof Integer )
                if( LeftValue instanceof Float )
                    RightValue =
                      new Float( ( ( Integer ) RightValue ).floatValue() );
            
            if( operation == '+' ){
                if( LeftValue instanceof Integer )
                    Result = new Integer(((Integer)LeftValue).intValue() 
                                         + ((Integer)RightValue).intValue());
                else 
                    Result = new Float(((Float)LeftValue).floatValue() 
                                       + ((Float)RightValue).floatValue());
            }else if ( operation == '-' ){
                if(LeftValue instanceof Integer)
                    Result = new Integer(((Integer)LeftValue).intValue()
                                         - ( (Integer)RightValue).intValue());
                else 
                    Result = new Float(((Float)LeftValue).floatValue()
                                       - ((Float)RightValue).floatValue());
            }else if( operation == '/' ){
                if(LeftValue instanceof Integer)
                    Result = new Integer(((Integer)LeftValue).intValue()
                                         / ((Integer)RightValue).intValue());
                else 
                    Result = new Float(((Float)LeftValue).floatValue()
                                       / ((Float)RightValue).floatValue());
            }else if( operation == '*' ){
                if(LeftValue instanceof Integer)
                    Result = new Integer(((Integer)LeftValue).intValue()
                                         * ((Integer)RightValue).intValue());
                else 
                    Result = new Float(((Float)LeftValue).floatValue()
                                       * ((Float)RightValue).floatValue());
            }else if ( operation == '^' ){
                double  base, 
                        power; 
                base=0; 
                power=0;
		if( LeftValue instanceof Integer)  
                    base = ((Integer)LeftValue).doubleValue();
                else if( LeftValue instanceof Float) 
                    base = ((Float)LeftValue).doubleValue();
                
                if( RightValue instanceof Integer)  
                    power = ((Integer)RightValue).doubleValue();
                else if( RightValue instanceof Float) 
                    power= ((Float)RightValue).doubleValue();
                
                Result = new Float(java.lang.Math.pow( base, power ));
            }else{
                seterror( 1000 , ER_IllegalCharacter+"c" );
                Result = null;
                return; 
            }
	    if( Debug )
                System.out.println("ops&Result=" + LeftValue + "," + operation 
                      + "," + RightValue + "=" + Result);


        }else if( operation == '&'){

            if( !(LeftValue instanceof String) )
		LeftValue = LeftValue.toString().trim();
            if( !(RightValue instanceof String) )
		RightValue = RightValue.toString().trim();

            Result = (String)LeftValue + (String)RightValue;

            if( Debug )
                System.out.println("Arith op & Res=" + Result + ";" +
                                   LeftValue  + ";" + RightValue);
        }
    }

    /**
    *    Executes the Array operations +,-,*,/, and &
    */
    private void operateVector( Object R1, Object R2, char c){
        if(!CanOp(R1)){
          seterror(1000, ER_IMPROPER_DATA_TYPE);
          return;
        }
        if(!CanOp(R2)){
           seterror(1000, ER_IMPROPER_DATA_TYPE);
           return;
        }
        int i;
        Vector Res = new Vector();
        Object r2i;
        if( "+-*/".indexOf(c)>=0){
            if( R1 instanceof Vector){
                if(R2 instanceof Vector)
                    if(((Vector)R2).size() != ((Vector)R1).size()){
                        seterror(1000, "Incompatible arrays");
                        Result= null;
                        return;
                    }
         
                for(i=0;i<((Vector)R1).size(); i++){
                    if(R2 instanceof Vector)
                        r2i= ((Vector)R2).elementAt(i);
                    else
                        r2i=R2;
                    operateArith(((Vector)R1).elementAt(i),r2i,c);
                    Res.addElement(Result);
                } 
                Result=Res;
            }else{ //if R1 instanceof Vector
                for( i=0;i<((Vector)R2).size();i++){
                    operateArith(R1,((Vector)R2).elementAt(i),c);
                    Res.addElement(Result);
                }
                Result =Res;
            }
            //if c is +-*/
        }else if( (!(R2 instanceof Vector))||(!( R1 instanceof Vector))){
            seterror(1000,"improper Arguments");
            Result = null;
            return;
        }else{
            Result = ((Vector)R1).clone();
            ((Vector)Result).addAll((Vector)R2); // must redo for Java 1.9
        }
    }//operateVector




    /**
     *  Executes the DataSet arithmetic operations, +,-,*, and /
     */
    private void operateArithDS( Object R1 , Object R2 , char c ){
      if(!CanOp(R1)){
        seterror(1000, ER_IMPROPER_DATA_TYPE);
        return;
      }
      if(!CanOp(R2)){
         seterror(1000, ER_IMPROPER_DATA_TYPE);
         return;
      }
        String Arg;
        if( c == '+' )
            Arg = "Add";
        else if( c == '-' )
            Arg = "Sub";
        else if( c == '*' )
            Arg = "Mult";
        else if( c == '/' )
            Arg = "Div";
        else{
            seterror( 1000 , "internalerror88" );
            return;       
        }
        Vector Args = new Vector();
        
        if( ( R1 == null ) ||  ( R2 == null ) ){
            seterror( 1000 , ER_ImproperArgument + "null" );
	}
        if( ( R1 instanceof String ) ||  ( R2 instanceof String ) ){
            seterror( 1000 , ER_ImproperArgument );
            return;
	}
        if( R1 instanceof Integer )
            R1 = new Float( ( ( Integer )R1 ).floatValue() );
        if(R2 instanceof Integer)
            R2 = new Float( ( ( Integer )R2 ).floatValue() );
        DataSet DS;
        Object Arg2;
        if( R1 instanceof DataSet ){
            DS = (DataSet)R1;
            Arg2 = R2; 
	}else{
            if( c == '-' ){
                operateArithDS( R2 , (Object)(new Float( -1 )) , '*' );
                R2 = Result;
                if(R2 == null){
                    seterror( 1000 , ER_ImproperArgument );
                    return;
                }
            }
            
            DS = (DataSet)R2;
            Arg2 = R1;
        }
        Args.addElement( DS );
        Args.addElement( Arg2 );
        Args.addElement(new Boolean (true));
      
        DoDataSetOperation( Args , Arg );
    }


    /**
     * Find and Executes an  operation from lists of operations 
     *
     * @param Args The vector of argument values
     * @param Command The command to be executed
     *
     * @return The value in the variable Result<BR> An error if the
     * operation is not defined or does not work
     */
    public void DoOperation( Vector Args, String Command ){
        if(Debug)
            System.out.println("Start DoOperation comm =" + Command);
        //Operator op = GenericOperatorList.getOperator( Command );
        if( Args == null )
            return;
        
        //????????????????? Is the Operator a Generic operator ??????????????????????
        Operator op = getSHOp( Args, Command );
        
        
        if( op == null ){
           //????????????????? Is it a Macro command( not used) ????????????
            op =(Operator)MacroInfo.get(Command);
            if( !checkArgs( Args, op, 0 ))
                op = null;
            
            //????????????? Is it a Data Set command ?????????????????????
            if(op==null)
                if( Args.size() > 0){
                    DoDataSetOperation( Args, Command  );
                    return;
                }
            seterror (1000 , execOneLine.ER_NoSuchOperator);
            return;
        }

       // ------------------ Execute the operation ------------------------
 
        if(!SetOpParameters( op , Args , 0)){
            seterror(1000,"Improper Args for "+ op.getCommand());
            return;
         }

        
        if( op instanceof IObservable)
            ((IObservable)op).addIObserver( this );
        if( op instanceof Customizer)
            ((Customizer)op).addPropertyChangeListener( this );

        try{
            
		    Result = op.getResult();
		    
        }catch(Throwable s){
          String[] SS = ScriptUtil.GetExceptionStackInfo( s,true,1);
          String S="";
          if( SS.length>0)
             S = SS[0];
          seterror (1000 ,  s.toString()+"in "+S);
          return;
        }
        
        
        op.setDefaultParameters();
        op = null;

        /*op.setDefaultParameters();

        if( op instanceof IObservable)
            ((IObservable)op).deleteIObserver( this );
        if( op instanceof Customizer)
            ((Customizer)op).removePropertyChangeListener( this );
        */
        if( Result instanceof ErrorString ){

            seterror (1000 , ((ErrorString)Result).toString() );
	    if(Debug)
                System.out.println("ErrorX Ocurred in get Result" + Result);
            Result = null;
        }
    }
      
    /**
    *    Gets the Generic operator corresponding to the given command 
    *    with the given arguments
    *
    *    @see  Script_Class_List_Handler#getNewOperator(String, Object[])
    */
    private Operator getSHOp( Vector Args, String Command){
      try{
        return ScriptUtil.getNewOperator(Command,Args.toArray());
      }catch(MissingResourceException e){
        return null;
      }catch(ClassCastException e){
        return null;
      }catch(Throwable s){
        return null;
      }
    }

    /**
    *   Checks to see if the Args correspond in data type to the ars in 
    *   operator op
    *
    *   @param Args   The arguments to match with the operators parameters
    *   @param op     The operator
    *   @param start  0 for Generic operators and 1 for DataSet operators
    */
    private boolean checkArgs( Vector Args , Operator op , int start){
        int k ;
        boolean fit =true;
        Object Arg2;
        if( op == null){
            //System.out.println("Check no op");
            return false;
        }
        if( Args == null )
            if( op.getNum_parameters() != 0 )
                return false;
            else
                return true;
        if(Debug)
            System.out.println("Check sizes = "+ Args.size() +","
                               + op.getNum_parameters());

        if( op.getNum_parameters() !=  Args.size() -start )
            return false;

        fit = true;
        for( k =0 ; (k < op.getNum_parameters()) && fit ; k++ ){
            Arg2 = Args.elementAt( k +start );
            if( Debug){
                System.out.print("Check"+Arg2.getClass());
	        if( op.getParameter(k) != null)
                    System.out.println( op.getParameter(k).getValue());
            }
            
            if( op.getParameter(k).getValue() == null){
                                     
            }else if( ( Arg2 instanceof String ) && 
                      ( op.getParameter(k).getValue() instanceof 
                        SpecialString ) ){
                
            }else if( Arg2.getClass().equals(
                                 op.getParameter(k).getValue().getClass() )){
                if( Debug ) System.out.print("E"+ Arg2.getClass());
            }else if( (Arg2 instanceof  Integer)
                      &&(op.getParameter(k).getValue() instanceof  Float)){
            }else 
                fit = false;
	                       
            
        }//For k
        return fit;
    }


    /**
    *    Sets the parameters of the operator to the values in the Vector Args
    *
    *  @param  op   The operator whose arguments are to be set
    *  @param  Args  The values for the operator, op's, parameters
    *  @param  start 0 for GenericOperators and 1 for DataSetOperators
    */
    private boolean SetOpParameters ( Operator op , Vector Args , int start ){
        int k=-1;
        try{
         
           int nn = op.getNum_parameters();
           if( Args.size()- start < nn)
               nn = Args.size() - start;
           for( k = 0 ; k < nn ; k++ ){
               if( (Args.elementAt( k + start ) instanceof String)  &&  
                   (op.getParameter( k ).getValue( ) instanceof SpecialString) ){
                   if( op.getParameter(k).getValue() != null){
                       SpecialString X=(SpecialString)
                           (op.getParameter(k).getValue());
                      
		       X.setString((String)(Args.elementAt(k+start)));
		   }
               }else if( op.getParameter(k).getValue() instanceof Float){
                   Float F = new Float(
                              ((Number)(Args.elementAt(k + start))).floatValue());
                   op.getParameter(k).setValue( F);
               }else if( op.getParameter(k).getValue() instanceof Integer){
                   Integer I = new Integer(
                              ((Number)(Args.elementAt(k + start))).intValue());
                   op.getParameter(k).setValue( I);
               }else if( op.getParameter(k) instanceof DataSetPG){
                    ((DataSetPG)op.getParameter(k)).addItem( Args.elementAt(k+start));
                     op.getParameter( k ).setValue( Args.elementAt( k + start ) );
                    
               }else{
                   op.getParameter( k ).setValue( Args.elementAt( k + start ) );
               }
          }
          return true;

        }catch( Exception ss){
          return false;
        }
    }


    /**
    *    Finds and executes a DataSet operator
    *
    *   @param  Args  the arguments for the opertor
    *   @param  Command the Command Name for the operator
    *   @return  The Global variable Result will contain the result
    */
    private void DoDataSetOperation( Vector Args , String Command ){
        int i;
        Operator op;
        DataSet DS;
        boolean fit;
        if( !( Args.elementAt(0)instanceof DataSet ) ){
            seterror( 1000 , ER_NoSuchOperator  );
            return;
        }
        DS = (DataSet)Args.elementAt(0);
        if(Debug)
            System.out.println("Command="+Command+":"+Args.size());
        for( i = 0 ; i < DS.getNum_operators() ; i++ ){
            op = DS.getOperator( i );
            if( Debug )
                System.out.print("OPList," + op.getCommand() + "," 
                                 + op.getClass().toString() + "," 
                                 + op.getNum_parameters() + ",");
            //fit = true;       //.getClass().to.String()
            fit = false;
            if( (op.getCommand().equals(Command) ) && 
                (op.getNum_parameters() == Args.size()-1) )
                fit = checkArgs( Args , op , 1 );
  
            if(fit){
                if(!SetOpParameters( op , Args , 1)){
                  seterror(1000,"Improper Args for "+op.getCommand());
                  return;
                }
        try{
        
		    Result = op.getResult();
        }catch(Throwable s){
          String[] SS = ScriptUtil.GetExceptionStackInfo( s,true,1);
          String S="";
          if( SS.length>0)
             S = SS[0];
          seterror (1000 ,  s.toString()+"in "+S);
          return;
        }
		op.setDefaultParameters();   
                if( Result instanceof ErrorString ){
                    seterror (1000 , ((ErrorString)Result).toString() );
    
                    Result = null;
                }
		return;
                
            }         
        }//For i = 0
        seterror( 1000 , ER_NoSuchOperator );
    }

    public static void ClearChooserPGParameters( Operator op){

       if( op == null) 
         return;
       for( int i=0; i < op.getNum_parameters(); i++){
          IParameter param = op.getParameter( i);
          if( param instanceof DataSetPG)
            ((ChooserPG)param).clear();
       }

    }


    /**
     *  This method executes the operation corresponding to  Command
     * 
     * @param  Command  the command name of the operator to be executed
     * @param S The string to be executed.  
     * @param start The starting character. This must be the first character
     *          past the ( of a function call.
     * @param end The last character of the string to be considered. 
     *
     * @return  The position in the String S of the next character to be 
     *          lexed and parsed.
     */
    private int execOperation(String Command, String S,  int start, int end ){
        if( ( start < 0 ) ||  ( start >= S.length() ) || ( start >= end ) ){
            seterror( S.length() + 2 , "internal errorq" );
            return start;
        }
        if( S.charAt( start ) != '(' ){
            seterror( S.length() + 2 , "internal errorr" );
            return start;
        }
        if( Debug)
            System.out.println( "In execOperation comm ="+Command);

        //Should have used my getArgs method
        // This is probably where the getArgs method was copied from
        Vector Args = new Vector();
        int i , j;
        boolean done;
        i = start;
        done = false;
        j = skipspaces(S, 1, i + 1 );
        if( j < end )
            if( (S.charAt(j) == ')')){
                done = true;
                i = skipspaces( S , 1 , j);
            }
        while( !done ){
            j = execute( S , i + 1 , end );
            if( perror >= 0 )
                return perror;
            Args.addElement( Result );
            if( ( j >= S.length() ) || ( j >= end ) ){
                seterror( j , ER_MisMatchParens );
                return j;
            }
            if( j < 0 ){
                seterror( S.length() + 3 , "InternalerrorG" );
                return S.length() + 3;
            }
            if( S.charAt( j ) == ')' )
                done = true;
            else if( S.charAt( j ) != ',' ){
                seterror( j , ER_IllegalCharacter+"D" );
                return j;
            }
            i = skipspaces(S , 1,j);
        }//while not done
        
        j = skipspaces( S , 1 , i + 1 );
        
        if( Debug )
            System.out.println("Got to Here after arg");
        
        int i2;
        
        if(Debug){
            System.out.print("Args = " + Args.size());
            for(i2 = 0 ; i2 < Args.size() ; i2++)
                System.out.print(Args.elementAt(i2) + ","
                                        + Args.elementAt(i2).getClass() + ",");
            System.out.println("");
        }
	
        if( Command != null ){
            if( Debug )
                System.out.println("Command=" + Command);
            DoOperation( Args , Command );
            if( perror >= 0 ) perror = start;
        }else{
            perror = j;
            serror = ER_FunctionUndefined;
            return j;
        }
        return skipspaces( S , 1 , j );
    }
   


    //************SECTION:UTILITIES EXEC and Variable Handling ***************

    /**
     * Returns the Result of the last expression
     *
     * Statements "should" return null
     *
     * This routines allows the execute routine to be used on
     * expressions and not just statements
     *
     * @return The value of the last operation.
     */
    public Object getResult(){
        return Result;
    }  


    /**
    *    Gets the value of the array variable S1.substring(0,"[") at the 
    *    indicated index.
    *    @param  S1  A string whose initial characters(to "[" if present) 
    *                represent the name of an array variable in the 
    *                execOneLine namespace
    *    @ return    the value of the variable or null if there is an error
    *    The error message is set if there is an error
    */
    private Object getValArray(String S1){
        String S = S1;
        Object O;
        
        if( perror >=0 ) 
            return null;
        int k = S.indexOf('[');
        Vector V = getArgs(S, k+1, S.length());
        
        if(V == null)
            return null;
        
        if(k < 0){
            seterror(1000, "Internal Error getValArry");
            return null; 
        }
        //---------------Get the whole array------------------
        Vector d1 = (Vector)ArrayInfo.get(S.substring(0,k).toUpperCase());
        if( d1== null){
            seterror(1000,ER_NoSuchVariable);
            return null; 
        }


        Object d=d1;
        //----------- get the associated subArray: Array[1,3,7] ---------------
        for(int i=0; i<V.size()-1; i++){
            O = V.elementAt(i);
            if(O == null){
                seterror(1000, ER_MissingArgument +" at index="+i);
                return null; 
            }
            if( !(O instanceof Integer)){
                seterror(1000, ER_IMPROPER_DATA_TYPE +"A at index="+i);
                return null; 
            }
            if(!(d instanceof Vector)){
                seterror(1000, ER_IMPROPER_DATA_TYPE+"B" );
                return null; 
            }
            int indx = ((Integer)O).intValue();
            if(((Vector)d).size()<=indx){
                seterror(1000, ER_ArrayIndex_Out_Of_bounds+" "+indx);
                return null;
            }
            d =((Vector)d).elementAt(indx);
        } 
        return d;
    }

  

  /** 
   * Returns the value of the variable S
   *
   * @param S A string used to refer to a variable
   *
   * @return The value of this string or an error message if the
   * variable is not found
   *
   * @see #getErrorCharPos()
   */
    public Object getVal( String S ){
        int i;
        if( S.toUpperCase().equals("NULL"))
            return new Nulll();
        if(S.indexOf('[')>=0)
            return getValArray(S);
        if( ArrayInfo.containsKey(S.toUpperCase()))
            return ArrayInfo.get(S.toUpperCase());
        if( BoolInfo.containsKey(S.toUpperCase()))
            return BoolInfo.get(S.toUpperCase());
        if( ObjectInfo.containsKey( S.toUpperCase()))
            return ObjectInfo.get(S.toUpperCase());
        
        i = findd( S.toUpperCase() , Ivalnames );
        if( isInList( i , Ivalnames ) ){
            return Ivals[ i ];
        }
        i = findd( S.toUpperCase() , Fvalnames );
        if( isInList( i , Fvalnames ) ){
            return Fvals[ i ];
        }
        i = findd( S.toUpperCase() , Svalnames );
        if( isInList( i , Svalnames ) ){
            return Svals[ i ];
        }
        if(ds.containsKey(S.toUpperCase().trim()))
            return ds.get(S.toUpperCase().trim());
        
        if(lds.containsKey(S.toUpperCase().trim()))
            return lds.get(S.toUpperCase().trim());
        Enumeration vals,keyss;
        
        vals=lds.elements();
        keyss=lds.keys();
        if(Debug)System.out.println("Loc DS Vals,Searchname"+S);
        
        Object X;
        for( i=0;vals.hasMoreElements();i++){
            X= vals.nextElement();
         
            if(Debug) System.out.println("val="+X);
        }
        
        if(Debug)System.out.println("Loc DS Keys");
        for( i=0;keyss.hasMoreElements();i++){
            X=keyss.nextElement();
            if(Debug) System.out.println("val="+X);
        }
        
        vals=ds.elements();
        keyss=ds.keys();
        
        if(Debug)System.out.println("Glob DS Vals,Searchname"+S+","+ds.size());
        for( i=0;vals.hasMoreElements();i++){
            X= vals.nextElement();
            if(Debug) System.out.println("val="+X);
        }
        
        if(Debug)System.out.println("Glob DS Keys");
        for( i=0;keyss.hasMoreElements();i++){
            X = keyss.nextElement();
            if(Debug) System.out.println("val="+X);
        }

        seterror( 1000 , ER_NoSuchVariable );
        return null;
    }

    /**
    *    Assigns the Result to an Array variable.  This method is
    *    invoked by the Assign method
    * @param  vname  A String whose first characters( to "[") represent an 
    *                array variable name and the last characters represent 
    *                 the index
    * @param  Result  The value to be assigned to the variable
    * @return  Error conditions set if the operation is not possible
    */
    public void AssignArray(String vname, Object Result){
        String S = vname;//fixx(S1);
        Object O;
        if(Debug)
            System.out.println("TOP ASsignArray vname,Result="
                               +vname+","+Result);
         
        if( perror >=0 ) return ;
        int k= S.indexOf('[');
        Vector V = getArgs(S, k+1, S.length());
        //System.out.print("B");
        if(V == null){
            return ;
        }   
        if(Debug) System.out.println("Args="+Vect_to_String(V)); 
      
        if(k<0){
            seterror(1000, "Internal Error getValArry");
            return ; 
        }
        boolean newVar=false;
        Vector d1 = (Vector)ArrayInfo.get(S.substring(0,k).toUpperCase());
        if((V.size()<2)&&(d1!=null)){ //ar[]=[1,2,3]
            if(Result instanceof Vector){
                d1.removeAllElements();
                d1.addAll((Vector)Result);
                return;
            }
	}else if( (V.size()<2)&&(d1==null)){
            if( Result instanceof Vector)
                ArrayInfo.put(S.substring(0,k).toUpperCase(), Result);
            else
                seterror(1000,ER_IMPROPER_DATA_TYPE+"C");
            return;
        }
    
        if( d1== null){
            d1=new Vector();
            newVar=true;
        }
        if(Debug)
            System.out.println("d1="+Vect_to_String(d1));
        Object d=d1;
        for(int i=0; i<V.size()-1; i++){
            O = V.elementAt(i);
            if(Debug)
                System.out.println("element i="+i+","+O); 
            if(O == null){
                seterror(1000, ER_MissingArgument +" at index="+i);
                return; 
            }
            if( !(O instanceof Integer)){
                seterror(1000, ER_IMPROPER_DATA_TYPE +"D at index="+i);
                return; 
            }
            if(i<V.size()-2)
                if(!(d instanceof Vector)){
                    seterror(1000, ER_IMPROPER_DATA_TYPE+" E" );
                    return; 
                }   
            int indx = ((Integer)O).intValue();
            if( ((i<V.size()-2) && ((Vector)d).size()<indx)
                || ((i==V.size()-1) &&((Vector)d).size()<indx-1) ){
                seterror(1000, ER_ArrayIndex_Out_Of_bounds+" "+indx);
                return ;
            }
            if(i<V.size()-2){
                d=((Vector)d).elementAt( indx);
            }else if(((Vector)d).size()>indx){
              Object O1=  ((Vector)d).set(indx,Result); 
              if( O1 != null) O1 = null;
            }else{
                ((Vector)d).addElement(Result);
            }
        } 
        if(newVar) 
            ArrayInfo.put(vname.substring(0,k).toUpperCase(),d);
        
    }

    /**
    *    Assigns the Result a variable in execOneLine's namespace.  T
    *    @param  vname  A String whose first characters( to "[") represent 
    *                   an array 
    *                variable name and the last characters represent the index
    *    @param  Result  The value to be assigned to the variable
    *    @return  Error conditions set if the operation is not possible
    */
    public void Assign(String vname, Object Result){
        int   i,j;
        Object O;
        boolean found = true ;
        String nam = vname;
        if( Result instanceof Nulll)
           {removeVar( vname);
            return;
           }
        if(vname.indexOf('[')>=0)
            nam = vname.substring(0,vname.indexOf('[')   ).toUpperCase();
        getVal( nam );
        if( (perror >= 0) && ( serror.equals( ER_NoSuchVariable))){
            perror = -1;
            serror = "";
            found = false;
        }
        if(Debug) System.out.println("in Assign,vname and nam="+vname+" "+nam);
        if(vname.indexOf('[')>=0){
            if(found && !ArrayInfo.containsKey(nam)){
                seterror(1000,ER_IMPROPER_DATA_TYPE+"F" );          
                return; 
            }
            AssignArray(vname, Result);
            return;
        }
        if(Result instanceof Double)
          Result = new Float( ((Double)Result).floatValue());
          
        if(ObjectInfo.containsKey( vname.toUpperCase())  ){
        
             ObjectInfo.put(vname, Result);
             return;
        }
        if( Result instanceof Boolean){
            if(vname.toUpperCase().equals("TRUE")
               || vname.toUpperCase().equals("FALSE")){
                seterror(1000, ER_ReservedWord);
                return;
            }
            if(found && !BoolInfo.containsKey(vname)){
                seterror(1000,ER_IMPROPER_DATA_TYPE+"G" );
                return; 
            }
            O =BoolInfo.put(vname.toUpperCase(),Result); 
           
           if( O != null) O = null;       
            
        }else if( Result instanceof Vector){
            if(found && !ArrayInfo.containsKey(vname)){
                seterror(1000,ER_IMPROPER_DATA_TYPE+"H" );
                return; 
            }
            O =ArrayInfo.put(vname.toUpperCase(),Result);
            if( O != null) O = null;
        }else if( Result instanceof Integer ){ //what about array of integers??
            i = findd( vname , Ivalnames );
            if( Ivalnames == null ){
                if( found ){
                    seterror (1000,ER_ImproperDataType);
                    return; 
                }
                Ivalnames = new String[ 10 ]; 
                Ivals = new Integer[ 10 ];
                i = 0;
                Ivalnames[ 0 ] = null;
            }
            if( i >= Ivalnames.length ){
                String IName[];
                if( found ){
                    seterror (1000,ER_ImproperDataType);
                    return; 
                }
                IName = new String[ Ivalnames.length + 10 ];
                Integer Ival[];
                Ival = new Integer[ Ivalnames.length + 10 ];
                for( j = 0 ; j < Ivalnames.length ; j++ ){
                    IName[ j ] = Ivalnames[ j ]; 
                    Ival[ j ] = Ivals[ j ];
                }
                Ivalnames = IName; 
                Ivals = Ival;
                Ivalnames[ i ] = null;
            }
            if(Ivalnames[ i ] == null){
                if( found ){
                    seterror (1000,ER_ImproperDataType);
                    return; 
	        }
                Ivalnames[ i ] = vname.toUpperCase();		 
                Ivals[ i ] = (Integer)Result;
                if( i + 1 < Ivalnames.length )
                    Ivalnames[ i + 1 ] = null;
            }else 
                Ivals[ i ] = (Integer)Result;
        }else if( Result instanceof Float ){
            i = findd( vname , Fvalnames );
            if( Fvalnames == null ){
                if( found ){
                    seterror (1000,ER_ImproperDataType);
                    return; 
                }
                Fvalnames = new String[ 10 ]; 
                Fvals = new Float[ 10 ];
                i = 0;
                Fvalnames[ 0 ] = null;
            }
            if( i >= Fvalnames.length ){
                String IName[];
                if( found ) {
                    seterror (1000,ER_ImproperDataType);
                    return; 
                } 
                IName = new String[ Fvalnames.length + 10 ];
                Float Fval[];
                Fval = new Float[ Fvalnames.length + 10 ];
                for( j = 0 ; j < Fvalnames.length ; j++ ){
                    IName[ j ] = Fvalnames [ j ]; 
                    Fval[ j ] = Fvals[ j ];
                }
                Fvalnames = IName; 
                Fvals = Fval;
                Fvalnames[ i ] = null;
            }
            if( Fvalnames[i] == null ){
                if( found ){
                    seterror (1000,ER_ImproperDataType);
                    return; 
                }
                Fvalnames[ i ] = vname.toUpperCase();
                Fvals[ i ] = (Float)Result;
                if( i + 1 < Fvalnames.length )
                    Fvalnames[ i + 1 ] = null;
            }else
                Fvals[ i ] = (Float)Result;
        }else if( Result instanceof String ){
            i = findd( vname , Svalnames );
            if( Svalnames == null ){
                Svalnames = new String[ 10 ];
                Svals = new String[ 10 ]; 
                i = 0;
                Svalnames[ 0 ] = null;
            }
            if( i >= Svalnames.length ){
                String IName[]; 
                IName = new String[ Svalnames.length + 10 ];
                String Sval[];
                Sval = new String[ Svalnames.length + 10 ];
                for( j = 0 ; j < Svalnames.length ; j++ ){
                    IName[ j ] = Svalnames[ j ]; 
                    Sval[ j ] = Svals[ j ];
                }
                Svalnames = IName; 
                Svals = Sval;
                Svalnames[ i ] = null;
            }
     
            if( Svalnames[ i ] == null ){
                if( found ){
                    seterror (1000,ER_ImproperDataType);
                    return; 
                }
                Svalnames[ i ] = vname.toUpperCase();
                Svals[ i ] = (String)Result;
                if( i + 1 < Svalnames.length )
                    Svalnames[ i + 1 ] = null;
            }else
                Svals[ i ] = (String)Result;
        }else if( Result instanceof DataSet ){
             DataSet D;
         
             if(ds.containsKey(vname.toUpperCase().trim())){
                 D =(DataSet) ds.get(vname.toUpperCase().trim());
                 D.copy((DataSet)Result); //=(DataSet)((DataSet)Result).clone();
                 Result = null;
                 return;
             }
              
             D =(DataSet) ((DataSet)Result).clone();
             
             O=lds.put(vname.toUpperCase().trim(),D);
             if( O != null) O = null;
             return;
        }
      else{//make sure vname not already in some other list
       
        //seterror(1000, "DataType Not supported for assignment operation");
         if(BoolInfo.containsKey(vname.toUpperCase())){
         
           
            seterror(1000, "Improper Data Type for variable "+vname);
            return;
         }if( ds.containsKey( vname.toUpperCase())){
         
            seterror(1000, "Improper Data Type for variable "+vname);
            return;
         }if( lds.containsKey( vname.toUpperCase())){
         
            seterror(1000, "Improper Data Type for variable "+vname);
            return;
         }if( findd(vname, Svalnames) >=0 ){
         
            if(findd(vname,Svalnames)>=Svalnames.length){
            
               seterror(1000, "Improper Data Type for variable "+vname);
               return;
            }
         }if( findd(vname, Fvalnames) >=0 ){
           if( findd(vname,Fvalnames)>=Fvalnames.length){
           
            seterror(1000, "Improper Data Type for variable "+vname);
            return;
           }
         } if( findd(vname, Ivalnames) >=0 ){
            if( findd(vname,Ivalnames)>=Ivalnames.length){
            
            seterror(1000, "Improper Data Type for variable "+vname);
            return;
            }
         } 
         ObjectInfo.put(vname.toUpperCase().trim(), Result); 
      }
        
    }//end Assign

    /**
   * Takes care of null values. This allows variables to be garbage
   * collected.
   */
   class Nulll{
    }

   //************************** String utilities **************************
    /**
     * Finds matching quote and returns unquoted string. Does will not handle
     * escape characters.  The length of the result is needed to determine the
     * position of the next character to be processed
     *
     *  @param  S  The string S
     *  @param  start the start of the string. It must be a "
     *  @return the string between two consecutive " or null if an error
     *  NOTE: use the length of the result to determine the next character to 
     *        be processed
     */
    private String getString( String S ,  int start ){
        if( ( start < 0 ) ||  ( start + 1 >= S.length() ) ){
            seterror( S.length() + 2 , ER_IllegalCharacter+"E" );
            return null;
        }
        if( S.charAt( start ) != '\"' ){
            seterror( S.length() + 2 ,  ER_IllegalCharacter+"F" );
            return null;
        }
        int i;
        for( i = start + 1 ; i < S.length() ; i++ ){
            if( Debug )
                System.out.print("in getstring,i,c=" + i + "," + S.charAt(i));
            if( S.charAt( i ) == '\"' ){
                return S.substring( start + 1 , i );
            }
        }
        seterror( start , ER_MisMatchQuote );
        return null;
    }

    /**
     * Eliminates spaces in the Title of ds1. Not needed now but called by
     * several routines.
     */
    private DataSet  eliminateSpaces( DataSet ds1 ){
        return ds1;
    }

    /**
     * eliminates trailing non printing characters
     */
    private String Trimm( String S ){
        if( S == null )
            return S;
        int i;
        i = S.length()-1;
        if( i < 0 )
            return S;
        if( S.charAt( i ) < ' ' ) 
            return Trimm( S.substring( 0 , i ) );
        else 
            return S;
    }

  
    /**
     * Finds the first of occurrence of one letter in SearchChars in
     * the String S starting at start in direction
     * dir(right=1,left=-1). Does not work with ""
     *     
     * The search will not search items between two parentheses or
     * BraceChars if the
     * search started outside the parenthsesis.
     * @param  S  the string to search in
     * @param  dir  direction to search, 1 to the right , -1 to left
     * @param start  the position in String S to start searching.
     * @param SearchChars The "set" of characters to be found
     * @param BraceChars  Pairs of characters represent start and end of
     *                  "braces". The SearchChars between braces will not
     *                   be found. 
     *  @return the position of the found character or the end( dir =1) or
     *           -1(if dir =-1) ;
     */

    private int findfirst( String   S,
                           int      dir,
                           int      start,
                           String   SearchChars,
                           String    BraceChars ){
        int    i,
            j,
            brclev;
        char c;
        boolean done;
        
        if( Debug )
            System.out.print("findfrst start, S=" + start + "." + S+":");
        if( start < 0 )
            return -1;

        if( S == null )
            return -1;
        else if( S.length() <= 0 )
            return -1;
        else if( start >= S.length() )
            return S.length();

        if( dir == 0 )
            return -1;
        else if( dir > 0 )
            dir = 1; 
        else 
            dir = -1;

        i = skipspaces( S , dir , start );
        brclev = 0;   
        
        c = 0;
        
        if( i >= S.length() )
            done = true; 
        else if( i < start )
            done = true; 
        else if( brclev < 0 ) 
            done = true;
        else if( ( brclev == 0 ) && ( SearchChars.indexOf( c ) >= 0 ) )
            done = true; 
        else{
            done = false;
            c   = S.charAt( i );
        }
   
        while( !done ){
            if(Debug)System.out.print(c);
            if( BraceChars != null )
                j = BraceChars.indexOf( S.charAt(i) );
            else 
                j = -1;
            if(j < 0){
            }else if( j == 2 * (int)(j / 2) )
                brclev++;
            else 
                brclev--;

            //** test this out.  if brclev<0 like end of line
            if(brclev>=0) i += dir;
            
            c = 0;
            if( i  >= S.length() )
                done = true; 
            else if( i < start )
                done = true; 
            else if( brclev < 0 )
                done = true;
            else{
                done = false;
                c = S.charAt( i );
            }
            if( ( brclev == 0 ) && ( SearchChars.indexOf(c) >= 0 ) )
                done = true;
        }
        if(Debug) System.out.println("return value ="+i);
        if( ( i <= S.length() ) && ( i >= 0 ) ) 
            return i; 
        else 
            return -1;
    }

    /**
     *  Skips spaces in a string
     *
     * @param S String
     * @param dir direction. positive skips forward<P>
     * @param start The starting index where the skipping starts
     *
     * @return returns the position in the string where first nonspace
     * occurs or the end of the string ( dir > 0 ) or -1( dir < 0 )
     */
    public int skipspaces( String S, int dir, int start){
        int  i; 
        char c;
        
        i = start;
        if( dir == 0 ) 
            return start;
        if( dir > 0 )
            dir = 1;
        else 
            dir = -1;
        c = 'z';
        if( i < S.length() ) 
            if( i >= 0 )
                c = S.charAt(i);
        if( c <= ' ' ) 
            return skipspaces( S , dir , start + dir );
        else 
            return i;
    }

  /**
     * Finds the first of occurrence of one letter in SearchChars in
     * the String S starting at start. Handles quoted strings
     *     
     * The search will not search items between two parentheses or
     * BraceChars if the
     * search started outside the parenthsesis.
     * @param  S           the string to search in
     * @param  start       the position in String S to start searching.
     * @param  SrchChars   The "set" of characters to be found
     * @param  brcpairs    Pairs of characters represent start and end of
     *                     "braces". The SearchChars between braces will not
     *                     be found. 
     *  @return the position of the found character or the end of the string
     */

    public static int finddQuote( String S, 
                                  int    start, 
                                  String SrchChars,
                                  String brcpairs ){
        int i, j;
        int brclevel;
        boolean quote;
        
        if( S == null )
            return -1;
        if( SrchChars == null )  return -1;
        if( ( start < 0 ) || ( start >= S.length() ) )
            return S.length();
        brclevel=0;
        quote=false;          
        
        for ( i = start ; i < S.length() ; i++ ){
            char c = S.charAt( i );
            
            if( c == '\"' ){
                if( (!quote) && (brclevel==0) && (SrchChars.indexOf(c)>=0) )
                    return i;
                quote = !quote;
                //if( i >= 1)
                //    if( S.charAt( i - 1 )  =='\"' ){
                //       quote = !quote;
               //     }
            }else if( quote ){
            }else if( SrchChars.indexOf( c ) >= 0 ){
                if( brclevel == 0 )
                    return i;
            }
            if( ( !quote ) && ( brcpairs != null ) ){
                j = brcpairs.indexOf( c );
                if(j<0) {}
                else if( j == 2* (int)( j / 2 ))
                    brclevel++;
                else
                    brclevel--;
            }
            if( brclevel < 0) return i;
        }
        return S.length();
    }
    

    /**
    *  Self explanatory
    */
    private boolean isInList( int i, String Llist[] ){
        if( i < 0 )
            return false;
        if( Llist == null )
            return false;
        if( i >= Llist.length )
            return false;
        if( Llist[i] == null )
            return false;
        return true;
        
    }

    /**
    *  Finds SearchName in a list of objects
    */
    private int findd(   String SearchName, Object SearchList[] ){
        if( SearchList == null )
            return -1;
        if( SearchList.length <= 0 )
            return -1;
        int i;
        if( Debug )
            System.out.println("Src=" + SearchName + ":");
        for( i = 0 ;(i < SearchList.length) && (SearchList[i] != null) ; i++ ){
            if( Debug )
                System.out.println("findd  i="+i+","+SearchList[i]+":"); 
            if( SearchList[i] == null )
                return i;
            
            else{
                if((((String)SearchList[i]).toUpperCase())
                                             .equals(SearchName.toUpperCase()))
                    return i;
            }
        }
        if( Debug )
            System.out.print( "not findd" );
        return i;
    }

   //************************SECTION:EVENTS********************

    /**
    *   Removes a DataSet from execOneLine's namespace.
    *   
    *   This is done when a DataSet is destroyed? or assigned a value Nulll
    */
    public void removeVar( String vname){
        if( lds !=null)
            if( lds.containsKey( vname.toUpperCase())){
                 Object ds= getVal( vname.toUpperCase());
                 if( ds != null)
                     ((DataSet)ds).deleteIObserver( this);
                 lds.remove( vname.toUpperCase());
                 return;
            } 
        if( BoolInfo !=null)
            if( BoolInfo .containsKey( vname.toUpperCase())){
                 BoolInfo.remove( vname.toUpperCase());
                 return;
            }   
        if( ArrayInfo !=null)
            if( ArrayInfo.containsKey( vname.toUpperCase())){
                 ArrayInfo.remove( vname.toUpperCase());
                 return;
            }

       if(ObjectInfo.containsKey(vname.toUpperCase())){
       
        ObjectInfo.remove( vname.toUpperCase());
        return;
       }
       int i;
       if(Fvalnames !=null)
       if( Fvalnames.length>0)
       {for( i=0; (i < Fvalnames.length)  && (Fvalnames[ i ] != null)
                       && (!Fvalnames[i].equals(vname.toUpperCase())) ; i++ )
         {}
        if( i< Fvalnames.length)
          if( Fvalnames[i] != null )
            if(Fvalnames[i].equals(vname.toUpperCase())){
               for(int j = i; j + 1 < Fvalnames.length;j++){
                  Fvalnames[j]=Fvalnames[j+1];
                  Fvals[j]=Fvals[j+1];
               }
            Fvalnames[ Fvals.length - 1 ] = null;
            return;
            }       
        }
       if(Ivalnames !=null)
       if( Ivalnames.length>0){
        for( i=0; (i < Ivalnames.length)  && (Ivalnames[ i ] != null)
                       && (!Ivalnames[i].equals(vname.toUpperCase())) ; i++ )
         {}
        if( i< Ivalnames.length)
          if( Ivalnames[i] != null )
            if(Ivalnames[i].equals(vname.toUpperCase())){
               for(int j = i; j + 1 < Ivalnames.length;j++){
                  Ivalnames[j]=Ivalnames[j+1];
                  Ivals[j]=Ivals[j+1];
               }
            Ivalnames[ Ivals.length - 1 ] = null;
            return;
            }       
        }
       if(Svalnames !=null)
       if( Svalnames.length>0){
        for( i=0; (i < Svalnames.length)  && (Svalnames[ i ] != null)
                       && (!Svalnames[i].equals(vname.toUpperCase())) ; i++ )
         {}
        if( i< Svalnames.length)
           if( Svalnames[i] != null )
            if(Svalnames[i].equals(vname.toUpperCase())){
               for(int j = i; j + 1 < Svalnames.length;j++){
                  Svalnames[j]=Svalnames[j+1];
                  Svals[j]=Svals[j+1];
               }
            Svalnames[ Svals.length - 1 ] = null;
            return;
            }       
        }
      seterror( 1000 , ER_NoSuchVariable );
    }

  
  /**
   * Removes name from a Hashtable if the name is not null.
   * @param vname  Name to remove. 
   * @param DS The Hashtable to remove.
   */
    public static void Delete( String vname, Hashtable DS){
        if(vname == null) return;
        DS.remove(vname.toUpperCase().trim()); 
    }
    

 
    public void propertyChange(PropertyChangeEvent evt){
        PC.firePropertyChange( evt );
    }

    public void update( Object observed_obj , Object reason ){
        

        if( observed_obj != null) 
            if( observed_obj instanceof DataSet)
                if( reason instanceof String)
                    if( reason.equals( IObserver.DESTROY)){
                        long tag = ((DataSet)observed_obj).getTag();
                        ds.remove( "ISAWDS"+tag);
                        (( DataSet)observed_obj).deleteIObserver( this );
                        observed_obj = null;
                    
                  } 
        if( reason instanceof DataSet)  //Send Command from a subscript
            OL.notifyIObservers( observed_obj , reason );
    } 

   

    /** 
     * @param iobs The Iobserver who wants to be notified of a new data
     * set.
     */
    public void addIObserver( IObserver iobs ){
        OL. addIObserver( iobs );
    }
    
    /** 
     * @param iobs The Iobserver who no longer wants to be notified of
     * a new data set
     */
    public void deleteIObserver( IObserver iobs ){

        OL.deleteIObserver( iobs );
    }

    /** 
     *
     */
    public void deleteIObservers(){
        OL.deleteIObservers();
    }

    /** 
     * @param listener The listener who wants to be notified of a non
     * Data Set "Display" value
     */
    public void addPropertyChangeListener( PropertyChangeListener listener ){
        PC.addPropertyChangeListener( listener );
    }
    
    /** 
     * @param listener The listener who no longer wants to be notified
     * of a non Data Set "Display" value
     */
    public void removePropertyChangeListener( PropertyChangeListener listener ){
        PC.removePropertyChangeListener( listener );
    }
    
    /**
     * Needed to fill out the Customizer interface
     */
    public void setObject( Object bean){
    }

    /** 
     * @param listener The listener who wants to be notified of a non
     * Data Set "Display" value
     * @param propertyName Must be Display
     */
     public void addPropertyChangeListener( String propertyName,
                                            PropertyChangeListener listener){
         PC.addPropertyChangeListener( listener );
     }

    /** 
     * @param listener The listener who no longer wants to be notified
     * of a non Data Set "Display" value
     * @param propertyName Must be Display
     */
     public void removePropertyChangeListener( String propertyName,
                                               PropertyChangeListener listener){
         PC.removePropertyChangeListener( listener );
     }
    
 //------------------------- SubRange Class --------------------

    /**
    *  This class stores and handles subranges of integers like 3:60
    */
    private class SubRange{
        int first,last;
        public SubRange( int first, int last){
            this.first = first;
            this.last = last;
        }
        /**
        *    Returns the Array form for the subrange
        */
        Vector Vect(){
            int dir=1;
            if(first> last) dir = -1;
            Vector V = new Vector();
            for( int i = first;(i-last)*dir <=0    ; i+=dir)
                V.addElement( new Integer(i));
            return V;
        }   
        
    }
}
