/*
 * File:  ScriptProcessor.java 
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
import java.io.*; 
import DataSetTools.retriever.*; 
import DataSetTools.dataset.*; 
import DataSetTools.util.*; 
//import DataSetTools.viewer.*; 
import DataSetTools.components.ParametersGUI.*;

import java.awt.*; 
import javax.swing.*; 
import javax.swing.text.*; 
import java.awt.event.*; 
import javax.swing.border.*; 
import DataSetTools.operator.*; 
import DataSetTools.operator.Generic.*;
import java.beans.*; 
import java.util.Vector;


/**
 * Processes the script or one line of a script.  <BR>
 * It has features to send back results that are displayable and also
 * results that are data sets. 
 */
public class ScriptProcessor  extends ScriptProcessorOperator 
                               implements  PropertyChangeListener, IObservable,
                                                IObserver, IDataSetListHandler{
  
  Command.execOneLine ExecLine ; 
  
  private int lerror;                        //line number of error. 
  private int perror;                        //position on line of error
  private String serror;                     //error message
  
  private IObserverList OL; 
  private PropertyChangeSupport PL;
  private Document logDocument = null;
  
  private boolean Debug = false;
  private StringBuffer macroBuffer=null;
  private Document MacroDocument = null;
  private Vector vnames= new Vector();
  private String command ="UNKNOWN";
  private String Title = "UNKNOWN";
  private String CategoryList="OPERATOR";
  
  /**
   * Constructor to take care of the common parts of initializing the
   * ScriptProcessor.
   */
  private ScriptProcessor(){
    super("UNKNOWN");
    ExecLine=new Command.execOneLine();
    ExecLine.resetError();
    OL = new IObserverList() ;
    PL = new PropertyChangeSupport( (Object)this );
    seterror(-1,"");
    lerror=-1;
  }

  /**
   * Constructor with no Visual Editing Element.  This form could be
   * used for Batch files
   *
   * @param TextFileName The name of the text file containg commands
   */
  public ScriptProcessor ( String TextFileName ){
    this();
    macroBuffer=Util.readTextFile(TextFileName);
    setDefaultParameters();
    Title = TextFileName;
  }
  
  public ScriptProcessor( StringBuffer buffer ){
    this();
    macroBuffer=buffer;
    setDefaultParameters();
  }

  /**
   * Constructor that can be used to run Macros- non visual?
   *
   * @param  Doc      The Document that has macro commands
   * @param  O        Observer for the Send command
   */
  public ScriptProcessor ( Document Doc  ){
    this();
    if( Doc==null || Doc.getLength()==0 ) return;
    this.setDocument(Doc);
    setDefaultParameters(); 
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
   
  /** Sets the whole list of property change listeners.  
   *   NOTE: Used when alternating between different languages
   */
  public void setPropertyChangeList( PropertyChangeSupport PcSupp) 
  {
    PL = PcSupp;
    ExecLine.setPropertyChangeList( PcSupp );
  } 

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
  
  public void setTitle( String title){
    Title = title;
  }
  
  private void initialize(){
  }
  
  /**
   * Initializes the Visual Editor Elements
   */ 
  private void init(){
  }
  
  public void setDocument( Document doc){
    MacroDocument = doc;
    CommandPane.fixUP(MacroDocument);
    macroBuffer=null;
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
   * Resets error condition so program can continue executing one line
   */
  public void resetError(){
    ExecLine.resetError();
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

  /**
   * executes all the lines of code in the document
   *
   *@param  Doc  The document that has the program in it
   */
  public void execute( Document Doc ){
    int line ; 
    Element E ; 
    if( Doc == null ) 
      return ;      

    E = Doc.getDefaultRootElement() ; 

    ExecLine.initt() ; 

    ExecLine.resetError();
    perror = -1 ; 
    line = executeBlock( Doc , 0 ,  true ,0 ) ; 
    if( perror < 0)
      seterror( ExecLine.getErrorCharPos(),ExecLine.getErrorMessage());
    if( perror >= 0 ) 
      if( lerror < 0 )lerror = line;
    if( line  <  E.getElementCount() )
      if( perror < 0 ){
        seterror(  line , "Did Not finish program" ) ; 
        lerror = line;
      }
  }
    
  private int executeBlock ( Document Doc, int start, boolean exec,
                             int onerror ){
    int line ;
    String S ; 
        
    if( Doc == null){
      seterror (0 , "No Document");
      if( Debug)System.out.println("NO DOCUMENT");
      lerror = 0;
      return 0 ;
    }
    Element E = Doc.getDefaultRootElement();
    Element F;                 
    line = start ; 
    if(Debug)
      System.out.print( "In exeBlock line ,ex=" + line+","+
                        exec + perror ) ; 
    while ( ( line < E.getElementCount() ) && ( perror < 0 ) ){
      S = getLine( Doc , line );

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
        line = executeErrorBlock( Doc , line , exec ) ;               
      }else if( onerror > 0 ){
      }else if( S.toUpperCase().trim().indexOf( "FOR " ) == 0 ){
        line = executeForBlock ( Doc , line , exec ,onerror ) ; 
      }else if( S.toUpperCase().trim().indexOf( "ENDFOR" ) == 0 ){
        return line ; 
      }else if( S.toUpperCase().trim().indexOf("IF ") == 0){
        line = executeIfStruct( Doc, line, exec , onerror );
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
        execute1( Doc  , line ) ; 
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
        String SS = getLine( Doc,line,  false );
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
    
  private int executeForBlock( Document Doc , int start , boolean execute, 
                               int onerror ){
    String var; 
    int i, j, k, n; 
    int line;  
    String S; 
    Element  E, F;  
    Vector V;
    if( Doc == null ) return -1 ; 
    E = Doc.getDefaultRootElement() ; 
    if( start < 0 ) return start ; 
    if( start >= E.getElementCount() ) return start ;  
        
    S = getLine( Doc , start );
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
    line = nextLine( Doc, start );
         
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
      kline = executeBlock( Doc , line , execute ,0 ) ; 
      if ( (perror >= 0) && (onerror == 0) )
        return kline ; 
            
      if( kline >= E.getElementCount() ){
        seterror( 0 , "No EndFor for a FORb" + S ) ; 
        return kline ; 
      }
              
      S = getLine( Doc , kline );
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

  private int executeErrorBlock( Document Doc , int start , boolean execute ){
    String var;      
    int i, j, k; 
    int line; 
    String S; 
    int mode; 
    Element E, F;  
    if(Debug) System.out.println("In exec Error" ); 
    if( Doc == null ) return -1;
    E = Doc.getDefaultRootElement();
    if( start < 0 ) return start;
    if( start >= E.getElementCount() ) return start;
       
    S = getLine( Doc , start);
       
    if( S == null ){
      seterror ( 0 , "Internal Errorc" );
      return start;
    }
    if( !S.toUpperCase().trim().equals( "ON ERROR" ) ){
      seterror ( 0, "internal error d" );
      return start ; 
    }
    line = executeBlock( Doc , nextLine( Doc ,start)  , execute, 0 );
    if( perror >= 0 ){
      ExecLine.resetError();
      perror = -1 ; 
      serror="";
      if(Debug) System.out.println("In ExERROR ERROR occured");
           
      line = executeBlock( Doc , line , false, 1 );
      mode = 0 ;
      if(Debug) System.out.println("After EXERROR ocurred "+line);
    }else
      mode = 1;
 
    if( line >= E.getElementCount() ){
      seterror ( 0 , " No ENDERROR for On Error" ) ; 
      return line ; 
    }

    S = getLine( Doc , line );
    if( S == null ){
      seterror ( 0 , "Internal Errorc" );
      return start;
    }
    if( S.toUpperCase().trim().equals( "ELSE ERROR" ) ){
      if(Debug)
        System.out.println("ELSE ERROR on line="+line+","+execute+mode);
      line =executeBlock( Doc , nextLine( Doc , line) , execute, mode );
          
      if( perror >= 0){
        lerror = line;
        if(Debug) System.out.println( "ELSE ERROR ERROR ob lin"+line);
        int pperror = perror;
        perror = -1;
        //not done cause did not reset error on this line????
        line = executeBlock( Doc, line, execute, 1);
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

    S = getLine( Doc , line );
    if( S == null ){
      seterror ( 0 , "Internal Errorc" ) ; 
      return start ; 
    }
    if( !S.toUpperCase().trim().equals( "END ERROR") ){
      seterror( 0 , " NO ELSE or END Error for On ERRor" );
    }

    if(Debug)System.out.println("ENd ERROR occurred"+perror+","+line+","
                                +lerror);

    if( perror < 0)
      return line ; 
    else 
      return line + 1; // in case nested on errors 
  }

  private int executeIfStruct( Document Doc, int line, boolean execute,
                               int onerror ){
    String S;
    int i , 
      j;
    if( Debug) 
      System.out.print("Start if line=" + line);
    S = getLine( Doc, line );
    if( Debug)
      System.out.println( ":: line is " + S );
    if( S == null){
      perror = 0;
      serror = "Internal Error 12";
      lerror = line;
      return line;
    } 
    i = S.toUpperCase().indexOf( "IF " ) ;
    if( i < 0){
      perror = 0;
      serror = "Internal Error 12";
      lerror = line;
      return line;
    }
    i = i + 3;
    j = S.length();
    if( S.trim().length() >= 8 )
      if( S.trim().substring( S.trim().length() - 5 ).equals( " THEN" ) )
        j = S.toUpperCase().lastIndexOf("THEN") ;

    boolean b;
    Object X;
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
    line = nextLine( Doc, line );
    j = executeBlock ( Doc , line , b && execute , 0 ) ;
    if( Debug)
      System.out.println( "ExIf::aft exe 1st block, perror=" +
                          perror +serror );
    if( perror >= 0 )
      return j;
    S = getLine ( Doc , j );
    if(Debug)
      System.out.println("ExIf:: Els or Elseif?"+S);
    if( S == null){
      seterror( 0 , "Improper line" );
      lerror = j;
      return j;
    }
    int x=0;
    if( S.toUpperCase().trim().indexOf( "ELSE" ) == 0)
      if( S.toUpperCase().trim().indexOf( "ELSEIF" ) == 0 ){
        j = executeIfStruct( Doc , j , !b && execute ,0);  
        return j;
      }else{
        j = executeBlock( Doc , nextLine( Doc , j ) , 
                          (!b) && execute, 0 );
        x = 2;
      }
      
    if(Debug) 
      System.out.println( "ExIf:: aft exec 1st block, perror=" + perror );
    if( perror >= 0) 
      return j;

    S = getLine ( Doc , j );
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
      return false;

    // trim off the marker flag
    index=line.indexOf("#$$");
    if(index<0)
      index=line.indexOf("$");
    if(Debug) System.out.println("start="+index);
    if(index<0)
      return false;
    buffer=new StringBuffer(line.substring(index+1).trim());

    // get the variable name
    VarName=StringUtil.getString(buffer);

    // get the Data Type and initial value
    DataType=StringUtil.getString(buffer);
    index=DataType.indexOf("("); // check if there is in initial value
    if(index>0){
      InitValue=DataType.substring(index+1);
      DataType=DataType.substring(0,index);
      index=buffer.toString().indexOf(")");
      if(index>0){
        InitValue=InitValue+buffer.substring(0,index);
        buffer.delete(0,index);
        StringUtil.trim(buffer);
      }else{
        InitValue=InitValue.substring(0,InitValue.length()-1);
      }
    }else{
      InitValue=null;
    }
    DataType = DataType.toUpperCase();

    // get the prompt
    Prompt=buffer.toString();
    if(Debug)
      System.out.println("in line ["+linenum+"] "+DataType+","+Prompt);
            
    // add name to list of variables
    if( !DataType.equals( "=" ))
      vnames.addElement( VarName );

    // parse type and create a parameter
    if( DataType.equals("=")){
      VarName = VarName.toUpperCase();
      if( VarName.equals("COMMAND")){
        command = Prompt;
      }else if(VarName.equals( "TITLE" )){
        Title = Prompt ;
      }else if( VarName.equals( "CATEGORY") )
        CategoryList= Prompt ;
    }else if( (DataType .equals( "INT") ) || ( DataType.equals( "INTEGER"))){
      if( InitValue == null)
        InitValue ="0";
      try {
        InitValue=InitValue.trim();
        addParameter( new Parameter ( Prompt , 
                                      new Integer(InitValue)) );
      }catch( Exception s){
         addParameter( new Parameter( Prompt, new Integer (0)) );
      }
    }else if ( DataType.equals( "FLOAT")){
      if( InitValue == null)
        InitValue ="0.0";
      try {
        InitValue=InitValue.trim();
        addParameter( new Parameter( Prompt,
                                     new Float(InitValue)) );
      }catch( Exception s){
        addParameter( new Parameter( Prompt , new Float (0.0)) );
      }
    }else if( DataType.equals( "STRING")){
      if( InitValue == null)
        InitValue ="";
      addParameter( new Parameter ( Prompt , InitValue ) ) ;
    }else if(DataType.equals("BOOLEAN")){
      if( InitValue == null)
        InitValue ="true";
      try {
        addParameter( new Parameter ( Prompt , 
                                 new Boolean(InitValue.toLowerCase().trim())));
      }catch( Exception s){
        addParameter( new Parameter( Prompt, new Boolean (true)) );
      }
    }else if( DataType.equals("ARRAY")){
      if( InitValue != null)
        ExecLine.execute(InitValue , 0 , InitValue.length());
      Vector V = new Vector();
      if( InitValue != null )
        if( ExecLine.getErrorCharPos() < 0){
          if( ExecLine.getResult() instanceof Vector)
            V = (Vector)(ExecLine.getResult());
        }
      addParameter( new Parameter( Prompt, V));
    }else if( DataType.equals("DATADIRECTORYSTRING")){
      String DirPath = null;
      if(InitValue!=null && InitValue.length()>0){
        DirPath=InitValue;
      }else{
        DirPath=System.getProperty("Data_Directory")+"\\";
      }
      if( DirPath != null )
        DirPath = 
          DataSetTools.util.StringUtil.setFileSeparator( DirPath);
      else
        DirPath = "";
      addParameter( new Parameter( Prompt,
                                   new DataDirectoryString(DirPath)));
    }else if( DataType.equals("DSSETTABLEFIELDSTRING")){
      if(InitValue == null)
        addParameter( new Parameter( Prompt ,
                                     new DSSettableFieldString() ));
      else
        addParameter( new Parameter( Prompt ,
                               new DSSettableFieldString(InitValue.trim()) ) );
    }else if( DataType.equals("LOADFILESTRING")){ 
      String DirPath=null;
      if(InitValue!=null && InitValue.length()>0){
        DirPath=InitValue;
      }else{
        DirPath=System.getProperty("Data_Directory")+"\\";
      }
      if(DirPath!=null)
        DirPath=DataSetTools.util.StringUtil.setFileSeparator(DirPath);
      else
        DirPath="";
      addParameter(new Parameter(Prompt,new LoadFileString(DirPath)));
    }else if( DataType.equals("SAVEFILESTRING")){ 
      String DirPath=null;
      if(InitValue!=null && InitValue.length()>0){
        DirPath=InitValue;
      }else{
        DirPath=System.getProperty("Data_Directory")+"\\";
      }
      if(DirPath!=null)
        DirPath=DataSetTools.util.StringUtil.setFileSeparator(DirPath);
      else
        DirPath="";
      addParameter(new Parameter(Prompt,new SaveFileString(DirPath)));
    }else if (DataType.equals( "DSFIELDSTRING")){
      if( InitValue == null )
        addParameter( new Parameter( Prompt,new DSFieldString() ));
      else
        addParameter( new Parameter( Prompt,
                                     new DSFieldString(InitValue.trim()) ));
    }else if( DataType.equals( "INSTRUMENTNAMESTRING")){
      String XX=null;
      if(InitValue!=null && InitValue.length()>0)
        XX=InitValue;
      else
        XX = System.getProperty("Default_Instrument");
      if( XX == null )
        XX = "";
      addParameter(  new Parameter( Prompt, XX ));
    }else if( DataType.equals( "SERVERTYPESTRING")){
      ServerTypeString STS = new ServerTypeString();
                
      addParameter( new Parameter( Prompt , STS ));
                
    }else if( DataType.equals("CHOICE")){
      int nn = ExecLine.execute( InitValue, 0, InitValue.length()); 
      Vector V= new Vector();
      if( ExecLine.getErrorCharPos() <0)
        if( ExecLine.getResult() instanceof Vector)
          V = (Vector)(ExecLine.getResult());
      String[] ss;
      ss = new String[V.size()];
      for( nn=0; nn< V.size(); nn++){
        ss[nn] = (V.elementAt(nn)).toString();
      }
      StringChoiceList SL = new StringChoiceList(ss);
      addParameter( new Parameter(Prompt, SL));
    }else if ( DataType.equals( "DATASET") ){
      DataSet dd = new DataSet("DataSet","");
      if( InitValue != null){
        ExecLine.execute( InitValue, 0, InitValue.length());
        if( ExecLine.getErrorCharPos() < 0 )
          if( ExecLine.getResult() instanceof DataSet )
            dd = ( DataSet )( ExecLine.getResult() );
      }
      Parameter PP = new Parameter( Prompt , dd);
      addParameter ( PP );
    }else if ( DataType.equals( "SAMPLEDATASET") ){
      SampleDataSet dd = new SampleDataSet();
      if( InitValue != null){
        ExecLine.execute( InitValue, 0, InitValue.length());
                   
        if( ExecLine.getErrorCharPos() < 0 )
          if( ExecLine.getResult() instanceof SampleDataSet )
            dd = ( SampleDataSet )( ExecLine.getResult() );
      }
 
      Parameter PP = new Parameter( Prompt , dd);
      addParameter ( PP );
    }else if ( DataType.equals( "MONITORDATASET") ){
      MonitorDataSet dd = new MonitorDataSet();
      if( InitValue != null){
        ExecLine.execute( InitValue, 0, InitValue.length());
        if( ExecLine.getErrorCharPos() < 0 )
          if( ExecLine.getResult() instanceof MonitorDataSet )
            dd = ( MonitorDataSet )( ExecLine.getResult() );
      }
      Parameter PP = new Parameter( Prompt , dd);
      addParameter ( PP );
    }else{
      index=line.toUpperCase().indexOf(DataType);
      seterror( index , "Data Type not supported " + DataType);
      lerror = linenum;
      return false; 
    }
      
    if( Debug)
      System.out.println("At bottom get def "+ perror+","+serror);


    return true;
  }

  private int nextLine( Document Doc, int line1 ){
    boolean done = false;
    int line = line1;  
    while( !done ){
      String SS = getLine( Doc,line,  false );
      if( SS == null )
        done = true;
      else if( SS.length() < 1)
        done = true;
      else if( SS.charAt( SS.length() - 1) != '\\' )
        done = true;
      else
        line ++;
    }
    line++  ; 
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
   * Same as getLine above if Contined is true.  It can be used when
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

  private void seterror( int poserr , String ermess ){
    perror = poserr ; 
    serror = ermess ; 
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

  // ************************SECTION:EVENTS********************

  /**
   * adds an Iobserver to be notified when a new data Set is sent
   *
   * @param iobs an Iobserver who wants to be notified of a data set
   */
  public void addIObserver( IObserver iobs ){
    ExecLine. addIObserver( this ) ; 
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
    OL.deleteIObserver( iobs ) ; 
  }
    
  /**
   * deletes all the Iobserver 
   */
  public void deleteIObservers(){
    ExecLine.deleteIObservers();
    OL.deleteIObservers();
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
   *  Sets Default parameters for getResult or for the JParametersGUI
   *  Dialog box. This will parse the macroBuffer created in the
   *  constructor.
   */
  public void setDefaultParameters(){
    String Line;
      
    if( Debug) 
      System.out.println("Start get Def par"+ perror);

    // create the text to get parameters from
    String text=null;
    if(macroBuffer!=null){
      text=macroBuffer.toString();
    }else{
      if( MacroDocument == null) 
        return;
      try{
        text=MacroDocument.getText(0,MacroDocument.getLength());
      }catch(BadLocationException e){
        // let it drop on the floor
      }
    }

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
        processMacroLine(Line,linenum);
        linenum++;
      }else if(buffer.length()>0){
        Line=buffer.toString();
        buffer.delete(0,buffer.length());
        processMacroLine(Line,linenum);
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
   * Gives the Command to use this script in the as a function in
   * this ScriptProcessor.<BR> NOT USED
   *
   * NOTE: ScriptOperators are used for functions in this scriptProcessor
   *
   *@see  Command.ScriptOperator
   */
  public String getCommand(){
    return command;
  }
    
  /**
   * Gives the Title of this program document. The Title appears in
   * dialog boxes and menu items
   *
   * NOTE: To set the Title have a line "$Title= title string" in
   * the document
   */
  public String getTitle(){
    return Title;
  }
    
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
      }else if( 3==4){
        S =  (String)vnames.elementAt(i)+ "=" ;
        if( (getParameter( i ).getValue() instanceof String) ||
            (getParameter( i ).getValue() instanceof SpecialString))
          S = S + '"' +  getParameter( i ).getValue().toString()+ '"';
        else
          S = S + getParameter( i ).getValue().toString();            
        ExecLine.resetError() ;
                
        int j = ExecLine.execute ( S , 0 , S.length());
        perror =ExecLine.getErrorCharPos() ;
                
        if( perror >= 0 )
          serror = ExecLine.getErrorMessage()+" in Parameters " + S;
        if( perror >= 0){
          lerror = i;
          //return;  must reset dataset titles
        }
      }else if( getParameter(i).getValue() instanceof SpecialString){
        ExecLine.Assign((String)(vnames.elementAt(i)),
                        getParameter(i).getValue().toString());

      }else{
        ExecLine.Assign((String)(vnames.elementAt(i)),
                        getParameter(i).getValue());
      }
    }// for i=0 to Num_parameters

    int k =lerror; 
    if(MacroDocument==null){
      if(macroBuffer==null){
        return new ErrorString("NO SCRIPT FOUND");
      }else{
        Document doc=new PlainDocument();
        try{
          doc.insertString(0,macroBuffer.toString(),null);
        }catch(BadLocationException e){
          // let it drop on the floor
        }
        this.setDocument(doc);
      }
    }
    k = executeBlock( MacroDocument ,0 ,true ,0) ;
         
    if( getErrorMessage().equals(execOneLine.WN_Return))
      seterror( -1,"");
    if( (perror < 0) && 
        !execOneLine.WN_Return.equals( ExecLine.getErrorMessage()))
      seterror( ExecLine.getErrorCharPos(), ExecLine.getErrorMessage());
        
    if( (perror >= 0) && (lerror <  0 ))
      lerror = k;
        
    boolean ReturnStatement=  execOneLine.WN_Return.
      equals(ExecLine.getErrorMessage());    

  
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
   * Executes when a PropertyChangeEvent Occurs
   *
   * @param  e   The property change Event. 
   *
   * NOTE: The only PropertyChangeEvent processed has a name
   * "Display"
   */
  public void propertyChange(PropertyChangeEvent e){
    PL.firePropertyChange( e );
  }

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

  /**
   * Executed when an IObservable notifies this IObserver
   *
   *@see DataSetTools.util.IObserver
   */ 
  public void update(  Object observed_obj ,  Object reason ){
    if( !(reason instanceof String))
      OL.notifyIObservers( this  ,  reason  );
    else
      OL.notifyIObservers( observed_obj, reason);
  }

  /**
   * Utility that returns all the data sets that have been added
   * from outside sources
   */
  public DataSet[] getDataSets(){
    return ExecLine.getGlobalDataset();
  } 

  private int findQuote(String S, int dir ,int start, String SrchChars,
                        String brcpairs){
    int i, j, j1;
    int brclevel;
    boolean quote;
        
    if(S == null)
      return -1;
    if(SrchChars == null)  return -1;
    if( ( start < 0 ) || ( start >= S.length() ) )
      return S.length();
    brclevel=0;
    quote=false;          
        
    if( dir == 0 ) return start;
    if( dir > 0 ) dir = 1;  else dir = -1;
    for ( i = start ; (i < S.length()) &&( i >= 0) ; i += dir ){
      char c = S.charAt(i);
            
      if( c == '\"' ){
        if( ( !quote ) && ( brclevel == 0 ) &&
            (SrchChars.indexOf(c) >= 0 ) )
          return i;
        quote = !quote;
        if( i >= 1)
          if( S.charAt( i - 1 )  =='\"' ){
            quote = !quote;
          }
      }else if( quote ){
      }else if(SrchChars.indexOf(c) >= 0){
        if( brclevel == 0)
          return i;
      }
      if( ( !quote ) && ( brcpairs != null ) ){
        j = brcpairs.indexOf(c);
        if(j<0) {}
        else if( j == 2* (int)(j/2))
          brclevel++;
        else
          brclevel--;
      }
      if(brclevel < 0) return i;
    }
    return i;
  }
}
