/*
 * File:  ScriptOperator.java 
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
 * Revision 1.32  2003/12/14 19:18:08  bouzekc
 * Removed unused import statements.
 *
 * Revision 1.31  2003/11/23 20:00:48  rmikk
 * The main program now executes all Jython scripts not just those that
 *   are written in the special format for operators.
 *
 * Revision 1.30  2003/11/22 20:39:29  rmikk
 * The main program now executes Jython operators as well as Isaw Scripts
 *
 * Revision 1.29  2003/10/10 01:56:58  bouzekc
 * Removed CTRL-M characters.
 *
 * Revision 1.28  2003/10/10 01:55:12  bouzekc
 * Merge of ScriptProcessor and ScriptOperator.
 *
 * Revision 1.59  2003/08/19 18:56:39  rmikk
 * Eliminated quotes for the initial values given in scripts
 *
 * Revision 1.58  2003/07/07 21:49:43  rmikk
 * -Rearranged Methods into sections.
 * -Added a lot of documentation
 *
 * Revision 1.57  2003/06/27 16:27:59  rmikk
 * Reverted to version 1.51
 * The if then statement no longer requires all upper case THEN
 * The setDefaultParameters now initializes the parameters variable
 *
* Revision 1.56  2003/06/26 21:48:54  rmikk
 * Reinstated commented out code to deal correctly(ignore) with
 *  $  category = ...
 *  $ Title =....
 *  $ Command=
 *
 * Revision 1.55  2003/06/26 14:37:34  bouzekc
 * Now makes only one call to String.toUpperCase() in
 * execute().
 *
 * Revision 1.54  2003/06/26 14:28:26  bouzekc
 * Commented out several empty if() blocks.
 *
 * Revision 1.53  2003/06/26 14:11:24  bouzekc
 * Removed commented-out sections of code.
 *
 * Revision 1.52  2003/06/26 13:55:18  bouzekc
 * Removed unused local variables.
 *
 * Revision 1.51  2003/06/18 18:17:26  pfpeterson
 * Removed dead code and implemented PropertyChanger.
 *
 * Revision 1.50  2003/06/17 16:34:04  pfpeterson
 * Uses new methods in IssScript for getCommand, getTitle, getDocumentation,
 * and getCategoryList.
 *
 * Revision 1.49  2003/06/13 20:07:48  pfpeterson
 * Takes advantage of the IssScript class.
 *
 * Revision 1.48  2003/06/10 14:57:22  bouzekc
 * Fixed NullPointerException when DataType is "INTLIST."
 *
 * Revision 1.47  2003/06/09 22:28:45  rmikk
 * Incorporated the ParameterList into the data types checked in scripts
 *
 * Revision 1.46  2003/06/09 16:46:55  rmikk
 * ReAdded the data type IntList
 *
 * Revision 1.45  2003/06/03 22:01:42  rmikk
 * -Change to IParameterPG parameters
 *
 * Revision 1.44  2003/06/02 22:47:55  pfpeterson
 * processMacroLine now properly returns true when there is not an
 * error in ALL cases.
 *
 * Revision 1.43  2003/06/02 14:29:36  rmikk
 * -Checked for and handled errors occurring in the
 *     parameter lines.
 *
 * Revision 1.42  2003/05/28 18:53:46  pfpeterson
 * Changed System.getProperty to SharedData.getProperty
 *
 * Revision 1.41  2003/04/21 19:09:43  pfpeterson
 * Reimplemented that '$' or '#$$' must be the first bit of non-whitespace
 * in a line.
 *
 * Revision 1.40  2003/03/25 22:50:10  pfpeterson
 * Fixed bug where initial parameter value contained no spaces.
 *
 * Revision 1.39  2003/03/25 22:03:15  pfpeterson
 * Fixed a bug where spaces removed from part of the initial value.
 *
 * Revision 1.38  2003/03/21 19:29:11  rmikk
 * Reset This modules error variables, too, in resetErrors
 *
 * Revision 1.37  2003/03/17 20:17:57  pfpeterson
 * Fixed a problem with macro lines. This is done by (internally)
 * inserting a space on either side of a '=' sign so the parsing
 * happens as expected.
 *
 * Revision 1.36  2003/03/14 16:55:07  pfpeterson
 * No longer creates list of IObservers or a PropertyChangeSupport
 * unless caller registers a need.
 *
 * Revision 1.35  2003/03/14 15:19:47  pfpeterson
 * Major reworking of class. Now processes Script objects, only instantiates swing components when necessary, and removed obsolete code.
 *
 * Revision 1.34  2003/03/13 21:27:10  pfpeterson
 * Fixed bug where we didn't skip comments while processing parameter lines.
 *
 * Revision 1.33  2003/03/11 20:12:52  pfpeterson
 * Fixed bug with setting initial value of a parameter.
 *
 * Revision 1.32  2003/03/11 15:33:40  pfpeterson
 * Does not use a Document until getResult(). setDefaultParameters()
 * now uses a StringBuffer.
 *
 * Revision 1.31  2003/03/10 21:05:14  pfpeterson
 * Split out code for parsing parameter definitions out into seperate
 * method. Also changed parsing parameters to use StringBuffer rather
 * than Document.
 *
 * Revision 1.30  2003/03/10 19:26:24  pfpeterson
 * Provided a new constructor which takes a StringBuffer and simplified
 * all constructors.
 *
 * Revision 1.29  2003/03/07 20:20:53  pfpeterson
 * Reduced the indent level to two.
 *
 * Revision 1.28  2003/03/05 14:21:08  rmikk
 * Fixed error.  The conditional part of an if or elseif statement
 *   is not executed if no other lines in the same block are
 *   executed
 *
 * Revision 1.27  2003/02/24 13:28:17  rmikk
 * Improved update method to allow scripts to send values to
 *    variables.  For use in wizards
 *
 * Revision 1.26  2003/02/19 16:50:25  pfpeterson
 * Now passes the default value to the parameter that is created for
 * DataDirectoryString, LoadFileString, SaveFileString, and
 * InstrumentNameString.
 *
 * Revision 1.25  2003/01/02 20:43:21  rmikk
 * Added methods to add a whole IObserverList and Property
 *   change support.
 *
 * Revision 1.24  2002/11/27 23:12:10  pfpeterson
 * standardized header
 *
 * Revision 1.23  2002/10/23 22:28:18  pfpeterson
 * Fixed a bug where a DataDirectoryString in a script was not
 * converted to the DataDirectoryString class.
 *
 * Revision 1.22  2002/08/19 17:07:06  pfpeterson
 * Reformated file to make it easier to read.
 *
 * Revision 1.21  2002/06/28 13:31:12  rmikk
 * -Eliminated dead code
 * -Improved indentations slightly
 *
 * Revision 1.20  2002/06/12 18:44:56  rmikk
 * Added code to deal with tabs like spaces
 *
 * Revision 1.19  2002/04/03 19:53:34  pfpeterson
 * Added SampleDataSet and MonitorDataSet.
 *
 * Revision 1.18  2002/04/02 22:51:05  pfpeterson
 * Provides for LoadFileString and SaveFileString.
 *
 * Revision 1.17  2002/02/22 20:33:44  pfpeterson
 * Operator Reorganization.
 *
 * Revision 1.16  2001/11/27 18:40:44  dennis
 * Added code to set the Result to null after an
 * if statement if finished. (Ruth)
 *
*/
package Command; 

import IsawGUI.*; 
import DataSetTools.dataset.*; 
import DataSetTools.util.*; 
import DataSetTools.parameter.*; 
import DataSetTools.components.ParametersGUI.*;

import javax.swing.text.*; 
import java.awt.event.*; 
import DataSetTools.operator.*; 
import DataSetTools.operator.Generic.*;
import java.beans.*; 
import java.util.Vector;


/**
 * Lexes, parses and executes Structure statements( if, else, for, on error),
 * parameter statements, and provides methods to interface with ISAW by
 * supplying titles, command names, execution results, etc. This class only
 * works on Isaw scripts.<P>
 * execOneLine is used to lex, parse, and executes lines not containing
 * structure or parameter statements and also expressions that are part of the
 * lines of the script containing the structure commands.
 */
public class ScriptOperator  extends  GenericOperator
                               implements  IScriptProcessor,PropertyChangeListener, IObservable,
                                IObserver, IDataSetListHandler,PropertyChanger{
  
  Command.execOneLine ExecLine ; 
  
  private int lerror;                        //line number of error. 
  private int perror;                        //position on line of error
  private String serror;                     //error message
  
  private IObserverList OL; 
  private PropertyChangeSupport PL;
  private Document logDocument = null;
  
  private boolean Debug = false;
  private Vector vnames= new Vector();
  private IssScript script=null;
  private ParameterClassList param_types = new ParameterClassList();

  //--------------------------- Constructors ---------------------------
  
  /**
   * Constructor that initializes the Script system
   */
  private ScriptOperator(){
    super("UNKNOWN");
    ExecLine=new Command.execOneLine();
    ExecLine.resetError();
    seterror(-1,"");
    lerror=-1;
  }

  /**
   * Constructor with no Visual Editing Element.  This form could be
   * used for Batch files
   *
   * @param TextFileName The name of the text file containg commands
   */
  public ScriptOperator ( String TextFileName ){
    this();
    this.script=new IssScript(TextFileName);
    setDefaultParameters();
  }
  
  /**
  *     Constructor that uses the StringBuffer representation of the
  *     script
  *     @param  buffer   the buffer that stores the Isaw Script
  */
  public ScriptOperator( StringBuffer buffer ){
    this();
    this.script=new IssScript(buffer);
    setDefaultParameters();
  }

  /**
   * Constructor that can be used to run Macros- non visual?
   *
   * @param  Doc      The Document that has macro commands
   */
   public ScriptOperator ( Document Doc  ){
    this();
    if( Doc==null || Doc.getLength()==0 ) return;
    this.script=new IssScript(Doc);
    setDefaultParameters(); 
  }
  

 //------------------------ Events ---------------------------------
  /**
   * The only "property" that changes is the "Display"<br> Use this
   * method if you want to be notified of the Display Command for non
   * Data Sets
   *
   * @param P The class that wants to be notified
   */
  public void addPropertyChangeListener( PropertyChangeListener P){
    if( ExecLine == null ) return;
    ExecLine.addPropertyChangeListener( P );
    if( PL == null )
      PL = new PropertyChangeSupport((Object) this );
    PL.addPropertyChangeListener(P);
  }

  public void addPropertyChangeListener( String prop,PropertyChangeListener P){
    if( ExecLine == null ) return;
    ExecLine.addPropertyChangeListener( P );
    if( PL == null )
      PL = new PropertyChangeSupport((Object) this );
    PL.addPropertyChangeListener(prop,P);
  }

  public void removePropertyChangeListener(PropertyChangeListener P){
    if(PL!=null)
      PL.removePropertyChangeListener(P);
  }

  /**
   * Sets the whole IObserverList.  
   *   NOTE: Used when alternating between different languages
   */
  public void setIObserverList( IObserverList IOlist)
  {
    OL = IOlist;
    ExecLine.setIObserverList( IOlist);
  }
 
 
  
  /**
   * Sets the whole list of property change listeners.  
   *   NOTE: Used when alternating between different languages
   */
  public void setPropertyChangeList( PropertyChangeSupport PcSupp) 
  {
    PL = PcSupp;
    ExecLine.setPropertyChangeList( PcSupp );
  } 

  /**
   * adds an Iobserver to be notified when a new data Set is sent
   *
   * @param iobs an Iobserver who wants to be notified of a data set
   */
  public void addIObserver( IObserver iobs ){
    ExecLine. addIObserver( this ) ; 
    if(OL==null) 
       OL=new IObserverList();
    OL.addIObserver( iobs ) ; 
  }

  /**
   * deletes an Iobserver who no longer wants to be notified when a
   * new data Set is sent
   *
   * @param iobs an Iobserver who wants to be notified of a data set
   */
  public void deleteIObserver( IObserver iobs ){
    ExecLine.deleteIObserver( this ) ; 
    if(OL==null)
      return; // can't remove anyone
    else
      OL.deleteIObserver( iobs ) ; 
  }
    
  /**
   * deletes all the Iobserver 
   */
  public void deleteIObservers(){
    ExecLine.deleteIObservers();
    if(OL==null)
      return; // can't remove anyone
    else
      OL.deleteIObservers();
  }

   //-----------------  Event Handling -----------------
/**
   * Executes when a PropertyChangeEvent Occurs
   *
   * @param  e   The property change Event. 
   *
   * NOTE: The only PropertyChangeEvent processed has a name
   * "Display"
   */
  public void propertyChange(PropertyChangeEvent e){
    if(PL==null) return; // no one to notify
    PL.firePropertyChange( e );
  }

 /**
   * Executed when an IObservable notifies this IObserver
   *
   *@see DataSetTools.util.IObserver
   */ 
  public void update(  Object observed_obj ,  Object reason ){
    if(OL==null) return; // no one to notify

    if( !(reason instanceof String))
      OL.notifyIObservers( this  ,  reason  );
    else
      OL.notifyIObservers( observed_obj, reason);

  }

 //---------------------- Interface Setups and Retrievers---------------

 /** 
   * Sets up the document that logs all operations
   *
   * @param doc The document that logs operations
   *
   * Note: Not used yet
   */
  public void setLogDoc( Document doc){
    logDocument = doc;
    ExecLine.setLogDoc( doc);
  }
  


  public void setDocument( Document doc){
    this.script=new IssScript(doc);
  }
  
   public Object clone(){
       ScriptOperator op = new ScriptOperator( script.getFilename());
       op.CopyParametersFrom( this);
       return op;
       
     }

 
  /**
   * adds a data set to the permanent workspace of the programs
   *
   * @param dss The data set that is to be added
   */
  public void addDataSet( DataSet dss ){
    if( Debug)System.out.println( dss.getTitle());
    ExecLine.addDataSet( dss ) ; 
  }

 /**
   * Resets error condition so program can continue executing one line
   */
  public void resetError(){
    ExecLine.resetError();
    perror = -1;
    lerror = -1;
    serror = "";
  }
  
  /**
   * resets the error conditions and the variable space and clears
   * displays
   *
   * NOTE: The data sets that were added from outside stay
   */
  public void reset(){
    ExecLine.initt();
    ExecLine.resetError();
    ExecLine.removeDisplays();
  }



  public String getVersion(){
    return "6-01-2001";
  }

  /**
   * Gets the position in a line where the error occurred
   */
  public int getErrorCharPos(){
    if(ExecLine == null ) return -1;
    return perror;
    //return ExecLine.getErrorCharPos();
  }

  /**
   * Gets the Message for the error 
   */
  public String getErrorMessage(){
    if( ExecLine == null ) return "";
    return serror;
  }

  /**
   * Returns the line where the error occurred
   */
  public int getErrorLine(){
    return lerror;
  }

  /**
   *    Utility to set the error at the current line
  */
  private void seterror( int poserr , String ermess ){
    perror = poserr ; 
    serror = ermess ; 
  }
      
//------------------------- Operator info Methods-----------------    
  /**
   * Returns the documentation. This is generated from the script from
   * the lines that start with a '#' up to the first non-empty line.
   */
  public String getDocumentation(){
    return this.script.getDocumentation();
  }

 
  public String[] getCategoryList(){
    return this.script.getCategoryList();
  }

  /** 
   * Gives the Command to use this script in the as a function in
   * this ScriptOperator.<BR> NOT USED
   *
   * NOTE: ScriptOperators are used for functions in this ScriptOperator
   *
   *@see  Command.ScriptOperator
   */
  public String getCommand(){
    return this.script.getCommand();
  }
    
  public String getFileName(){
    return this.script.getFilename();
  }

  String Title = null;
  /**
   * Gives the Title of this program document. The Title appears in
   * dialog boxes and menu items
   *
   * NOTE: To set the Title have a line "$Title= title string" in
   * the document
   */
  public String getTitle(){
    if( Title == null)
      Title = this.script.getTitle();
    return Title;
  }

   public void setTitle( String Title){
     this.Title = Title;
   }

  /**
   * Utility that returns all the data sets that have been added
   * from outside sources
   */
  public DataSet[] getDataSets(){
    return ExecLine.getGlobalDataset();
  }     

  //----------------- Lex and Parse Section -----------------


 //----------------------- Executes Script ---------------------------
  /**
   * Executes the whole script then returns the result
   *
   * @return the result.  If there is an error the result is a
   * subclass of ErrorString
   *
   * @see DataSetTools.util.ErrorString
   */
  public Object getResult(){
    int i;
    String S;
        
    ExecLine.initt();
    ExecLine.resetError();
    seterror( -1, "");
    lerror = -1;
    
    //--------------- Add Parameters to execOneLine -----------------    
    for( i = 0 ; i < getNum_parameters() ; i++){
      if(getParameter( i ).getValue() == null){
        serror = "Undefined Parameter "+i;
        perror =i;
        lerror = i;
        return new ErrorString( serror );
        
      }else if( getParameter( i ).getValue() instanceof DataSet){
        DataSet ds = (DataSet)(getParameter( i ).getValue());        
        ExecLine.addParameterDataSet( ds , (String)vnames.elementAt(i));
      }else if( getParameter(i).getValue() instanceof Vector){
        Vector V = (Vector)( getParameter(i).getValue());
        ExecLine.Assign((String)(vnames.elementAt(i)) , V );
      
      }else if( getParameter(i).getValue() instanceof SpecialString){
        ExecLine.Assign((String)(vnames.elementAt(i)),
                        getParameter(i).getValue().toString());

      }else{
        ExecLine.Assign((String)(vnames.elementAt(i)),
                        getParameter(i).getValue());
      }
    }// for i=0 to Num_parameters

    //-------------  Execute the script--------------------
    int k =lerror; 
    k = executeBlock( this.script,0 ,true ,0) ;
         
    // ------------- Check for errors -------------------
    if( getErrorMessage().equals(execOneLine.WN_Return))
      seterror( -1,"");
    if( (perror < 0) && 
        !execOneLine.WN_Return.equals( ExecLine.getErrorMessage()))
      seterror( ExecLine.getErrorCharPos(), ExecLine.getErrorMessage());
        
    if( (perror >= 0) && (lerror <  0 ))
      lerror = k;
    
    // ???????? is there a return statment ??????????    
    boolean ReturnStatement=  execOneLine.WN_Return.
      equals(ExecLine.getErrorMessage());    

    //----------- Set up Return Value -------------------
  
    if( ExecLine != null )
      if( (ExecLine.getErrorCharPos() >= 0) && !ReturnStatement){
        return new ErrorString( ExecLine.getErrorMessage() +" on line "+
                                lerror+ "at position "
                                +ExecLine.getErrorCharPos() );
      }else{
        return ExecLine.getResult();
      }
    else
      return null;
  }




  /**
   * Executes one line of a Script
   *
   * @param script The Script with the line in it
   * @param line  The line in the document to be executed
   */
  public  void execute1( Script script, int line ){
    String S ; 
    
    if( script==null ) return;
    if( line < 0 )return ;    
    S = script.getLine(line);
    
    if( S == null ){
      seterror( 0 , "Bad Line number");
      lerror = line;
      return;
    }
    int kk = ExecLine.execute( S , 0 , S.length() ) ; 
    perror = ExecLine.getErrorCharPos() ; 
    if( perror >= 0 ){
      lerror = line ; 
      serror = ExecLine.getErrorMessage() ; 
    }else if( kk < S.length() - 1 ){
      lerror = line ;  
      perror = kk ; 
      serror = "Extra Characters at the end of command" ; 
    } 
  }
  
  /**
   * Executes one line in a Document
   *
   * @param Doc   The document with the line in it
   * @param line  The line in the document to be executed
   */
  public  void execute1( Document  Doc, int line ){
    String S ; 
    
    if( Doc == null )return ;   
    if( line < 0 )return ;    
    S = getLine( Doc , line);
    
    if( S == null ){
      seterror( 0 , "Bad Line number");
      lerror = line;
      return;
    }
    int kk = ExecLine.execute( S , 0 , S.length() ) ; 
    perror = ExecLine.getErrorCharPos() ; 
    if( perror >= 0 ){
      lerror = line ; 
      serror = ExecLine.getErrorMessage() ; 
    }else if( kk < S.length() - 1 ){
      lerror = line ;  
      perror = kk ; 
      serror = "Extra Characters at the end of command" ; 
    } 
  }
  
 
  /**
   * executes all the lines of code in the script
   *
   * @param  script The script that has the program in it
   */
  public void execute( Script script ){
    int line ; 
    if(script==null)
      return;

    ExecLine.initt() ; 

    ExecLine.resetError();
    perror = -1 ; 
    line = executeBlock( script , 0 ,  true ,0 ) ; 
    if( perror < 0)
      seterror( ExecLine.getErrorCharPos(),ExecLine.getErrorMessage());
    if( perror >= 0 ) 
      if( lerror < 0 )lerror = line;
    if(line<script.numLines())
      if( perror < 0 ){
        seterror(  line , "Did Not finish program" ) ; 
        lerror = line;
      }
    
  }
  

  /**
  *     Goes through lines of code in the script starting at line "start".
  *     The code is executed if exec is true and onerror is false. This
  *     method will execute lines of code until it hits the end of the script
  *     or an end structure statement like "end if","end for", etc.

  *     @param   script   the script containing the code to be executed
  *     @param   start    the starting line of the script to execute.
  *     @param   exec     If true the lines will be executed, otherwise they
  *                       will just be past through.
  *     @param   onerror  If greater than zero, lines will be ignored until an
  *                       "end error" or "else error" where onerror is 
  *                        decremented.
  *     @return   The last line processed by this method. It should be the
  *               end of the script or an end or else structure type statment
  */

  private int executeBlock( Script script, int start, boolean exec,
                            int onerror ){
    int line ;
    String S ; 
        

    if( script == null){
      seterror (0 , "No Document");
      if( Debug)System.out.println("NO DOCUMENT");
      lerror = 0;
      return 0 ;
    }
    line = start ; 
    if(Debug)
      System.out.print( "In exeBlock line ,ex=" + line+","+
                        exec + perror ) ; 
    while ( ( line < script.numLines() ) && ( perror < 0 ) ){
      S = script.getLine( line );

      if( S !=  null ){
        int i ; 
        char c ; 
        for( i = S.length() - 1 ;  i >=  0  ;  i-- ){
          if( S.charAt( i ) <=  ' ' )S = S.substring( 0 , i ) ; 
          else i = -1 ; 
        }
      }
           
      if( S == null ){
        if( Debug )
          System.out.println(" S is null " );
      }else if( S.trim() == null){
      }else if( S.trim().indexOf( "#") == 0 ){
      }else if( S.trim().indexOf("$" ) == 0){
      }else if( S.toUpperCase().trim().indexOf( "ELSE ERROR" ) == 0 ){
        return line ; 
      }else if( S.toUpperCase().trim().indexOf( "END ERROR" ) == 0 ){
        return line ; 
      }else if( S.toUpperCase().trim().indexOf( "ON ERROR" ) == 0 ){
        line = executeErrorBlock( script , line , exec ) ;               
      }else if( onerror > 0 ){
      }else if( S.toUpperCase().trim().indexOf( "FOR " ) == 0 ){
        line = executeForBlock ( script , line , exec ,onerror ) ; 
      }else if( S.toUpperCase().trim().indexOf( "ENDFOR" ) == 0 ){
        return line ; 
      }else if( S.toUpperCase().trim().indexOf("IF ") == 0){
        line = executeIfStruct( script, line, exec , onerror );
      }else if( S.toUpperCase().trim().equals("ELSE")){
        return line;
      }else if( S.toUpperCase().trim().indexOf("ELSEIF") == 0){
        return line;
      }else if( S.toUpperCase().trim().equals("ENDIF")){
        return line;
      }else if( S.trim().length() <= 0 ){
      }else if( exec ){
        //can transverse a sequence of lines. On error , if then
        ExecLine.resetError();               
        execute1( script  , line ) ;
      }
          
      if( perror >= 0 ){
        if( lerror < 0 )lerror = line ; 
        if(Debug)
          System.out.println("errbot"+line) ; 
        return line ; 
      }
      if(Debug)System.out.println( " Thru" +line) ; 
      boolean done = false;
      while( !done ){
        String SS = script.getLine( line );
        if( SS == null )
          done = true;
        else if( SS.length() < 1)
          done = true;
        else if( SS.charAt( SS.length() - 1) != '\\' )
          done = true;
        else
          line ++;
      }
      line++;
    }
    return line; 
  }


  /**    
  *     Executes a For Block
  *     @param   script   the script containing the code to be executed
  *     @param   start    the starting line of the script to execute.
  *     @param   exec     If true the lines will be executed, otherwise they
  *                       will just be past through.
  *     @param   onerror  If greater than zero, lines will be ignored until an
  *                       "end error" or "else error" where onerror is 
  *                        decremented.
  *     @return   The last line processed by this method. It should have and
  *               end for statement
  */   
  private int executeForBlock( Script script , int start , boolean execute, 
                               int onerror ){
    String var; 
    int i, j, k, n; 
    int line;  
    String S; 
    Vector V;
    if(script==null) return -1;
    if( start < 0 ) return start ; 
    if( start >= script.numLines() ) return start ;  
        
    S = script.getLine( start );
    if( S == null){
      seterror ( 0 , "Internal Errorb" ) ; 
      return start ; 
    }         
    i =  S.toUpperCase().indexOf( "FOR " ) ; 
    if( i < 0 ){
      seterror ( 1 ,  "internal error" ) ; 
      return start ; 
    }
    if( execute && (onerror==0)){
      j = S.toUpperCase().indexOf( " IN " ,  i + 1 ) ;
      if( j< 0){
        perror = i;
        seterror(i, "Syntax Error IN required");
        lerror = start;
        return start;
      }
      var= S.substring( i + 4, j).trim(); 
      String iter = S.substring( j + 4  ).trim() ;
            
      int kk = ExecLine.execute( iter, 0, iter.length());
      perror = ExecLine.getErrorCharPos();
            
            
      if( perror >=0){
        perror = perror +j;
        seterror(perror+j, ExecLine.getErrorMessage());
        lerror = start;
        return start;
      }
      kk = ExecLine.skipspaces( iter, 1,kk);
      if( kk < iter.length() ){
        seterror( kk, "Extra Characters at the end of command");
        lerror = start;
        return start;
      }  
      Object U = ExecLine.getResult();
      if( !(U instanceof Vector) || (U==null)){
        seterror( j+4, ExecLine.ER_IMPROPER_DATA_TYPE);
        lerror = start;
        return start;
      }
      V = (Vector)U;
      n=V.size();
    }else{ //just go thru motions
      n=1;
      V = new Vector();
      var ="MMM";
    }   
    line = nextLine( script, start );
         
    int kline = line;
    for( int jj = 0; jj< n ;jj++){
      if(execute && (onerror ==0)){
        ExecLine.Assign(var,V.elementAt(jj)); 
        if( (perror >= 0) && (onerror == 0) ){
          seterror(i+4 , ExecLine.getErrorMessage());
          lerror = start;
          return start ; 
        }
      }
            
      //if(execute) 
      kline = executeBlock( script , line , execute ,0 ) ; 
      if ( (perror >= 0) && (onerror == 0) )
        return kline ; 
            
      if( kline >= script.numLines() ){
        seterror( 0 , "No EndFor for a FORb" + S ) ; 
        return kline ; 
      }
              
      S = script.getLine( kline );
      if( S == null ){
        seterror( 0 ,  "Internal error" ) ; 
        return kline ; 
      }
             
      if( perror >= 0 ) return kline ;
            
      if( !S.toUpperCase().trim().equals( "ENDFOR" ) ){
        seterror( 0 , "No EndFor for a FORx" + S ) ; 
        return kline ; 
      };
                  
    }	//end for jj=    
        
    return kline ; 
  }

   /**    
  *     Executes a on Error Block
  *     @param   script   the script containing the code to be executed
  *     @param   start    the starting line of the script to execute.
  *     @param   exec     If true the lines will be executed, otherwise they
  *                       will just be past through.
  *     @return   The last line processed by this method. It should have and
  *               end error statement
  */   
  private int executeErrorBlock( Script script, int start, boolean execute ){
    String var;      
    int i, j, k; 
    int line; 
    String S; 
    int mode; 
    if(Debug) System.out.println("In exec Error" ); 
    if(script==null) return -1;
    if( start < 0 ) return start;
    if(start>=script.numLines());
       
    S = script.getLine(start);
       
    if( S == null ){
      seterror ( 0 , "Internal Errorc" );
      return start;
    }
    if( !S.toUpperCase().trim().equals( "ON ERROR" ) ){
      seterror ( 0, "internal error d" );
      return start ; 
    }
    line = executeBlock( script, nextLine( script ,start)  , execute, 0 );
    if( perror >= 0 ){
      ExecLine.resetError();
      perror = -1 ; 
      serror="";
      if(Debug) System.out.println("In ExERROR ERROR occured");
           
      line = executeBlock( script , line , false, 1 );
      mode = 0 ;
      if(Debug) System.out.println("After EXERROR ocurred "+line);
    }else
      mode = 1;
 
    if( line >= script.numLines() ){
      seterror ( 0 , " No ENDERROR for On Error" ) ; 
      return line ; 
    }

    S = script.getLine( line );
    if( S == null ){
      seterror ( 0 , "Internal Errorc" );
      return start;
    }
    if( S.toUpperCase().trim().equals( "ELSE ERROR" ) ){
      if(Debug)
        System.out.println("ELSE ERROR on line="+line+","+execute+mode);
      line =executeBlock( script, nextLine(script, line), execute, mode );
          
      if( perror >= 0){
        lerror = line;
        if(Debug) System.out.println( "ELSE ERROR ERROR ob lin"+line);
        int pperror = perror;
        perror = -1;
        //not done cause did not reset error on this line????
        line = executeBlock( script, line, execute, 1);
        perror = pperror;
        if(Debug)System.out.println("ELSE ERROR ERROR2, line,perror ="+
                                    line+","+perror);
      }
    }else if( S.toUpperCase().trim().equals( "END ERROR" ) ){
      if(Debug)System.out.println("ENd ERROR occurred"+perror+","+line);
      return line ; 
    }else{
      seterror( line , " NO ELSE or END Error for On ERRor" ) ; 
      return line ;  
    }

    S = script.getLine( line );
    if( S == null ){
      seterror ( 0 , "Internal Errorc" ) ; 
      return start ; 
    }
    if( !S.toUpperCase().trim().equals( "END ERROR") ){
      seterror( 0 , " NO ELSE or END Error for On ERRor" );
    }

    if(Debug)System.out.println("END ERROR occurred"+perror+","+line+","
                                +lerror);

    if( perror < 0)
      return line ; 
    else 
      return line + 1; // in case nested on errors 
  }


 /**    
  *     Executes a If Blocks
  *     @param   script   the script containing the code to be executed
  *     @param   start    the starting line of the script to execute.
  *     @param   exec     If true the lines will be executed, otherwise they
  *                       will just be past through.
  *     @param   onerror  If greater than zero, lines will be ignored until an
  *                       "end error" or "else error" where onerror is 
  *                        decremented.
  *     @return   The last line processed by this method. It should have and
  *               end if statement
  */   
  private int executeIfStruct( Script script, int line, boolean execute,
                               int onerror ){
    String S;
    int i , 
      j;
    if( Debug) 
      System.out.print("Start if line=" + line);
    S = script.getLine( line );
    if( Debug)
      System.out.println( ":: line is " + S );
    if( S == null){
      perror = 0;
      serror = "Internal Error 12";
      lerror = line;
      return line;
    } 
    //????????? Does line start with IF ?????????????
    i = S.toUpperCase().indexOf( "IF " ) ;
    if( i < 0){
      perror = 0;
      serror = "Internal Error 12";
      lerror = line;
      return line;
    }

    //?????????? Get rest of line, eliminate the trailing THEN????
    i = i + 3;
    j = S.length();
    if( S.trim().length() >= 8 )
      if( S.toUpperCase().trim().substring( S.trim().length() - 5 )
                .equals( " THEN" ) )
        j = S.toUpperCase().lastIndexOf("THEN") ;

    boolean b;
    Object X;
    //?????????????? Determing If condition ???????????????
    if(!execute)
      X = new Boolean(true);
    else{
      int kk =ExecLine.execute( S, i, j);
      if( ExecLine.getErrorCharPos()>=0){
        seterror(ExecLine.getErrorCharPos(), ExecLine.getErrorMessage());
        lerror= line;
        return line;
      }
      kk = ExecLine.skipspaces( S, 1,kk);
      if( kk < j ){
        seterror( kk, "Extra Characters at the end of command");
        lerror = line;
        return line;
      }  
      X = ExecLine.getResult();
    }
    if( X instanceof Integer)
      if( ((Integer)X).intValue()==0) X = new Boolean(false);
      else X = new Boolean(true);
    if( !(X instanceof Boolean)){
      seterror(j,execOneLine.ER_ImproperDataType);
      lerror= line;
      return line;
    }
    b= ((Boolean)X).booleanValue();   
    if(Debug) 
      System.out.println("aft eval b and err ="+b+","+perror);
    if( perror >= 0 )
      return line;

    //------- Now go through Next block and execute or not--------
    line = nextLine( script, line );
    j = executeBlock ( script , line , b && execute , 0 ) ;
    if( Debug)
      System.out.println( "ExIf::aft exe 1st block, perror=" +
                          perror +serror );
    if( perror >= 0 )
      return j;
    //------------ Get end block statement --------------
    S = script.getLine ( j );
    if(Debug)
      System.out.println("ExIf:: Els or Elseif?"+S);
    if( S == null){
      seterror( 0 , "Improper line" );
      lerror = j;
      return j;
    }
    int x=0;
    //??????????? Is it ELSE or ELSEIF ????????????????????
    if( S.toUpperCase().trim().indexOf( "ELSE" ) == 0)
      if( S.toUpperCase().trim().indexOf( "ELSEIF" ) == 0 ){
        j = executeIfStruct( script , j , !b && execute ,0);  
        return j;
      }else{
        j = executeBlock( script, nextLine( script, j ), (!b) && execute, 0 );
        x = 2;
      }
      
    if(Debug) 
      System.out.println( "ExIf:: aft exec 1st block, perror=" + perror );
    if( perror >= 0) 
      return j;
    //??????????????? Is it ENDIF
    S = script.getLine ( j );
    if( Debug ) 
      System.out.println( "ExIf:: ENDIF?" + S );
    if( S == null){
      seterror( 0, "Improper line" );
      lerror = j;
      return j;
    }
    if(! S.toUpperCase().trim().equals( "ENDIF" ) ){
      seterror( 0, "If without an ENDIF" );
      lerror = line;
      return j;
    }  
       
    ExecLine.Result=null;
    if( Debug) 
      System.out.println( "ExIF end OK, line is " + j );       
    return j;
  } 

 //------------------ Handle Parameters ------------------------------

 /**
   *  Sets Default parameters for getResult or for the JParametersGUI
   *  Dialog box. This will parse the macroBuffer created in the
   *  constructor.
   *  Error conditions are returned in the error variables.
   */
  public void setDefaultParameters(){
    parameters = new Vector();
    if(this.script==null) return; // don't do anything before there is a script

    String Line;
      
    if( Debug) 
      System.out.println("Start get Def par"+ perror);

    String text=this.script.toString();

    // check that string isn't empty
    if( text==null || text.length()<=0 )
      return;

    // remove everything after the last suspected parameter and create
    // a StringBuffer
    StringBuffer buffer=null;
    int index=text.lastIndexOf("$");
    if(index<0){
      buffer=null;
    }else{
      index=text.indexOf("\n",index);
      if(index>0)
        buffer=new StringBuffer(text.substring(0,index+1));
    }

    // must have a non-empty buffer
    if( buffer==null || buffer.length()<=0 )
      return;

    // initialize the list of variables and parameters
    parameters= new Vector();
    vnames= new Vector();

    // parse the parameters
    int linenum=0;
    index=-1;
    while(true){
      index=buffer.toString().indexOf("\n");
      if(index>=0){
        Line=buffer.substring(0,index);
        buffer.delete(0,index+1);
        if( !processMacroLine(Line,linenum))
          return;
        linenum++;
      }else if(buffer.length()>0){
        Line=buffer.toString();
        buffer.delete(0,buffer.length());
        if( !processMacroLine(Line,linenum))
            return;
        linenum++;
      }else{
        break;
      }
    }
    ExecLine.resetError();
    seterror( -1,"");
    return ;
  }



  /**
   * Utility to get the Next Macro line( starts with $ or #$) in a
   * document
   *
   * @param Doc The document containing the program
   * @param prevLine the line number of the previous Macro line of
   * -1 for first
   *
   * @return The line number of the next macro line or -1 if none
   */
  public static int getNextMacroLine( Document Doc, int prevLine){
    String Line;
    prevLine++;
    if( prevLine < 0 ) 
      prevLine = 0;
    Line = getLine( Doc , prevLine);
    if( Line == null )
      return -1;
    if( Line.trim().indexOf("#$$") == 0)
      return prevLine;
    if( Line.trim().indexOf("$") == 0){
      //if(Debug)System.out.println("get $ Macro line #"+prevLine);
      return prevLine;
    }
    return getNextMacroLine( Doc, prevLine++);
  }

  private String EliminateQuotes( String S){
     if( S == null)
       return null;
     String S1 = new String( S.trim());
     if( S1 == null)
       return null;
     if( S1.length() < 2)
       return S;
     if( S1.charAt(0) !='\"')
       return S;
     if( S1.charAt(S1.length()-1) !='\"')
       return S;
     return S1.substring(1,S1.length()-1);
  }

  /**
   * This turns a string into a parameter that it adds to the list of
   * parameters for the operator. This can also parse some 'MACRO'
   * information such as setting the title or command name.
   *
   * @param line the String to be parsed
   * @param linenum the line number that the string came from. This is
   * used in the case when an error is encountered.
   */
  private boolean processMacroLine( String line, int linenum ){
    String VarName, DataType, InitValue, Prompt;
    int index=-1;
    StringBuffer buffer=null;

    if( Debug)
      System.out.println("Line="+line);
            
    // confirm that there is something to work with
    if( line==null || line.length()<=0 || line.indexOf("$")<0 )
      return true;
    {
      String checker=line.trim();
      if( (checker.indexOf("$")!=0) && (checker.indexOf("#$$")!=0) )
        return true;
    }

    index=line.indexOf("#");
    if(index==0){ 
      if(line.indexOf("#$$")<0)
        return true; // legacy code. #$$ used to be parameter
    }

    // Eliminate the #$ in the legacy parameter indicator
    index=line.indexOf("#$$");
    if(index>=0){
      buffer=new StringBuffer(line.substring(index+3).trim());
    }else{
      index=line.indexOf("$");
      if(index < 0) 
          return false; // something wrong
      buffer=new StringBuffer(line.substring(index+1).trim());
    }

    // insert extra spaces if there is an '=' sign
    index=buffer.toString().indexOf("=");
    if( index>0 ){
      buffer.insert(index+1," ");
      buffer.insert(index," ");
    }

    // get the variable name
    VarName=StringUtil.getString(buffer);

    // get the Data Type and initial value
    int num_space=buffer.length();
    DataType=StringUtil.getString(buffer);
    if(DataType==null) 
         return false; // REMOVE-nope line is not long enough

    // check if there is in initial value
    index=DataType.indexOf("("); 
    if(index>0){
      num_space = num_space - buffer.length() - DataType.length();
      InitValue = DataType.substring(index+1);
      DataType=DataType.substring(0,index);
      index=InitValue.indexOf(")");
      if(index > 0){ // the init value is complete so trim off the ')'
        InitValue=InitValue.substring(0,index);
      }else{  // look in the buffer for the rest of the init value
        index=buffer.toString().indexOf(")");
        if(index>0){
          InitValue=InitValue+Format.string("",num_space)
            +buffer.substring(0,index);
          buffer.delete(0,index+1);
          StringUtil.trim(buffer);
        }else{
          InitValue=InitValue.substring(0,InitValue.length()-1);
        }
      }
    }else{
      InitValue=null;
    }
    String DataType_C = DataType;
    DataType = DataType.toUpperCase();

    // get the prompt
    Prompt=buffer.toString().trim();
    if(Debug)
      System.out.println("in line ["+linenum+"] "+DataType+","+Prompt);
            
    // add name to list of variables
    if( !DataType.equals( "=" ))
      vnames.addElement( VarName );
   
    // parse type and create a parameter
    if( DataType.equals("=")){
      // do nothing
    }else if( (DataType .equals( "INT") ) || ( DataType.equals( "INTEGER"))){
      if( InitValue == null)
        InitValue ="0";
      try {
        InitValue=InitValue.trim();
        addParameter( new IntegerPG( Prompt , 
                                      new Integer(InitValue)) );
      }catch( Exception s){
         addParameter( new Parameter( Prompt, new Integer (0)) );
      }
    }else if ( DataType.equals( "FLOAT")){
      if( InitValue == null)
        InitValue ="0.0";
      try {
        InitValue=InitValue.trim();
        addParameter( new FloatPG( Prompt,
                                     new Float(InitValue)) );
      }catch( Exception s){
        addParameter( new Parameter( Prompt , new Float (0.0)) );
      }
    }else if( DataType.equals( "STRING")){
      if( InitValue == null)
        InitValue ="";
      addParameter( new StringPG ( Prompt , EliminateQuotes(InitValue) ) ) ;
    }else if(DataType.equals("BOOLEAN")){
      if( InitValue == null)
        InitValue ="true";
      try {
        addParameter( new BooleanPG ( Prompt , 
                                 new Boolean(InitValue.toLowerCase().trim())));
      }catch( Exception s){
        addParameter( new BooleanPG( Prompt, new Boolean (true)) );
      }
    }else if( DataType.equals("ARRAY")){
      addParameter( new ArrayPG( Prompt, InitValue));
    }else if( DataType.equals("DATADIRECTORYSTRING")){
     
      addParameter( new DataDirPG( Prompt, null));
                                 //  new DataDirectoryString(EliminateQuotes(DirPath))));
    }else if( DataType.equals("DSSETTABLEFIELDSTRING")){
      
      DSSettableFieldString dsf = new DSSettableFieldString();
      ChoiceListPG choice= new ChoiceListPG( Prompt, InitValue);
      for( int i = 0; i< dsf.num_strings(); i++)
        choice.addItem( dsf.getString(i));
      addParameter( choice);
    }else if( DataType.equals("LOADFILESTRING")){ 
     
      addParameter(new LoadFilePG(Prompt, EliminateQuotes(InitValue)));
    }else if( DataType.equals("SAVEFILESTRING")){ 
     
      addParameter(new SaveFilePG(Prompt,EliminateQuotes(InitValue)));
    }else if( DataType.equals( "INTLIST" )){
       if(InitValue != null)
         addParameter( new IntArrayPG( Prompt, InitValue.trim() ));
       else
         addParameter( new IntArrayPG( Prompt, InitValue ));


    }else if (DataType.equals( "DSFIELDSTRING")){
      if( InitValue == null )
        addParameter( new Parameter( Prompt,new DSFieldString() ));
      else
        addParameter( new SaveFilePG( Prompt, EliminateQuotes(InitValue) ));
                                     //new DSFieldString(InitValue.trim()) ));
    }else if( DataType.equals( "INSTRUMENTNAMESTRING")){
      String Instrument_Name = null;
      if(InitValue != null && InitValue.length() > 0)
        Instrument_Name = EliminateQuotes(InitValue);
      else
        Instrument_Name = SharedData.getProperty("Default_Instrument");
      if( Instrument_Name == null )
        Instrument_Name = "";
      addParameter(  new InstNamePG( Prompt, Instrument_Name ));
    }else if( DataType.equals( "SERVERTYPESTRING")){
      ServerTypeString STS = new ServerTypeString();
      ChoiceListPG clpg = new ChoiceListPG( Prompt, EliminateQuotes(InitValue));
      for( int i=0; i< STS.num_strings(); i++)
         clpg.addItem( STS.getString(i));
               
      addParameter( new Parameter( Prompt , STS ));
                
    }else if( DataType.equals("CHOICE")){
      int nn = ExecLine.execute( InitValue, 0, InitValue.length()); 
      Vector V= new Vector();
      if( ExecLine.getErrorCharPos() <0)
        if( ExecLine.getResult() instanceof Vector)
          V = (Vector)(ExecLine.getResult());
       ChoiceListPG cpg = new ChoiceListPG( Prompt, null);
      for( nn=0; nn< V.size(); nn++){
        cpg.addItem( (V.elementAt(nn)).toString()) ;
      }
      
      addParameter(  cpg);
    }else if ( DataType.equals( "DATASET") ){
     
      addParameter ( new DataSetPG(Prompt, null));// PP );
    }else if ( DataType.equals( "SAMPLEDATASET") ){
     
      addParameter ( new SampleDataSetPG(Prompt, null) );
    }else if ( DataType.equals( "MONITORDATASET") ){
     
      addParameter ( new MonitorDataSetPG( Prompt, null) );
    }else{
      IParameter param = param_types.getInstance( DataType_C);
      if( param != null)//Need to do something with ChooserPG's
                        // InitValues should be addItem quantities BUT DataSetPG
        try{
             param.setName( Prompt);
             param.setValue( EliminateQuotes(EliminateQuotes(InitValue)));
             addParameter( param );
             return true;
            }
        catch(Exception ss)
          { 
            DataSetTools.util.SharedData.addmsg( "Parameter Error="+ss); 
           }
    
      index=line.toUpperCase().indexOf(DataType);
      seterror( index , "Data Type not supported " + DataType);
      lerror = linenum;
      return false; 
    }
      
    if( Debug)
      System.out.println("At bottom get def "+ perror+","+serror);


    return true;
  }

  //----------------------- Text Utilities ------------------
  private int nextLine( Script script, int line1 ){
    boolean done=false;
    int line=line1;
    String SS=null;
    while(!done){
      SS=script.getLine(line);
      if(SS==null)
        done=true;
      else if(SS.length()<1)
        done=true;
      else if(SS.charAt(SS.length()-1)!='\\')
        done=true;
      else
        line++;
    }
    line++;
    return line;
  }

  /**
   * Utility to return a given line from the Document
   *
   * @param Doc the document with the line
   * @param start  the line number to be returned
   * @return  The string representation of that line or null if there is none
   */
  public static String getLine( Document Doc, int start ){
    return getLine( Doc, start, true );
  }
    
  /**
   * Same as getLine above if Continued is true.  It can be used when
   * keeping track of line numbers.
   */
  private static String getLine( Document Doc, int start, boolean Continued ){
    String var;
    int i, j, k;
    int line;
    String S;
    boolean mode;
    Element  E, F;
       
    if( Doc == null ) 
      return null ; 
    E = Doc.getDefaultRootElement() ; 
    if( start < 0 ) 
      return null ; 
    if( start >= E.getElementCount() ) 
      return null ;   
    F = E.getElement( start ) ; 
    try{
      S = Doc.getText( F.getStartOffset(),
                       F.getEndOffset() -  F.getStartOffset() );
    }catch( BadLocationException s ){
      //seterror ( 0 , "Internal Errorc" ) ; 
      return null ; 
    }
     
    if( S != null) 
      if( S.charAt(S.length() - 1 )<' ' ) 
        S = S.substring( 0,S.length() - 1 );
    if(Continued)
      if( S.length() >0 )
        if( S.charAt( S.length() - 1) == '\\' ){
          String S2 = getLine( Doc,start + 1 );
          if( S2 != null)
            S = S.substring( 0, S.length() - 1 )+ S2;
        }
    return S;
  }

  /**
   * Utility to delete xcess spaces outside of quotes(") in a string S
   */   
  public String delSpaces( String S){
    boolean quote, onespace; 
    int i;
    String Res;
    char prevchar;
    if( S == null ) return null;
    Res  = "";
    quote = false;
    onespace = false;
    prevchar = 0; 
    for ( i =0; i < S.length() ; i++){
      if( S.charAt( i) == '\"'){
        quote = ! quote;
        if( i > 0)
          if (!quote )
            if( S.charAt( i-1) == '\\') quote = !quote;
        Res = Res + S.charAt ( i );
        prevchar = S.charAt ( i );
      }else if( quote ){
        Res = Res + S.charAt(i);
        prevchar = S.charAt ( i );
      }else if( " \t".indexOf(S.charAt(i))>=0){
        //!!if( S.charAt ( i ) == ' ')
        if( " +-*/^():[]{}," . indexOf(S.charAt(i + 1 )) >= 0){
        }else if( i+1>= S.length()){
        }else if( i < 1) {
        }else if("+-*/^():[]{},".indexOf(S.charAt( i - 1 ) ) >= 0){
        }else
          Res = Res + S.charAt( i ) ; 
        prevchar = ' ';
      }else{
        Res = Res + S.charAt(i);
        prevchar = S.charAt(i);
      }
    }
    return Res;
  }

    /**
     * Allows running of Scripts without Isaw and/or the CommandPane
     */
    public static void main( String args [] ){
        DataSetTools.util.SharedData sd = new DataSetTools.util.SharedData();

        if( args == null)
            System.exit( 0 );
        if( args.length < 1)
            System.exit( 0 );
        Operator SO=null;
        try{
          SO = IsawLite.fromScript( args[ 0 ] );
        }catch(InstantiationError e){
          if( args[0].toUpperCase().trim().endsWith(".PY")){
             try
              {
                SO= new PyScriptOperator( args[0] ); 
              }
              catch( Throwable ss)
              {
                System.out.println("ERROR:"+e.getMessage());
                System.exit(-1);
               }

          }else{
            System.out.println("ERROR:"+e.getMessage());
            System.exit(-1);
          }
        }
        if( !(SO instanceof IScriptProcessor)){
           System.out.println("Impropert type of Script");
           System.exit(-1);
        }
        if( SO!=null && ((IScriptProcessor)SO).getErrorMessage()!= null &&
                        ((IScriptProcessor)SO).getErrorMessage().length() > 0){
            System.out.println("Error ="+args[0]+"--"+((IScriptProcessor)SO).getErrorMessage());
            System.exit(-1);
        }
        boolean dialogbox=false;
        if( SO.getNum_parameters() > 0){
            JParametersDialog JP = new JParametersDialog(SO, null, null, null ,true);
            JP.addWindowListener( new WindowAdapter(){
                public void windowClosed(WindowEvent e){
                  System.exit(0);
                }
              } );
            dialogbox=true;
        }else{
            Object XX = SO.getResult();
            System.out.println("Result =" +XX );        
        }

        if( SO != null)
            if( ((IScriptProcessor)SO).getErrorMessage() != null )
                if( ((IScriptProcessor)SO).getErrorMessage().length() > 0)
                    System.out.println("An Error occurred "
                                       +((IScriptProcessor)SO).getErrorMessage());
        if(!dialogbox)System.exit( 0 );
    }

}
