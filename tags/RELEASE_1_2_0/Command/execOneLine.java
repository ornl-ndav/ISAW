
/*op.getParameter(k).getValue()*
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
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
 * Revision 1.29  2001/11/12 21:18:39  dennis
 *   1. & between a string and boolean or Vector now changes these
 *      non string data type to string.
 *
 *   2. Float parameters in operators now also match with
 *      integer arguments.
 *
 * Revision 1.28  2001/11/09 18:18:11  dennis
 * Fixed the method Vect_to_String to put quotes around the string entries.
 * Also, data sets are replaced by ISAWDS[tag number]
 *
 * Revision 1.27  2001/08/16 20:30:50  rmikk
 * Fixed the javadocs @see tags
 *
 * Revision 1.26  2001/08/15 14:15:30  rmikk
 * Set Default Parameters on all operators that had their
 *    parameters changed.  The data types of these operators
 *    will now be accurate when used later.
 *
 * Revision 1.25  2001/08/02 20:54:40  rmikk
 * This now supports the RETURN statement.
 *
 * Revision 1.24  2001/07/30 21:36:22  rmikk
 * Implemented  the Iobservable  delete event
 *
 * Revision 1.23  2001/07/20 16:36:28  rmikk
 * Fixed error that occurred when two operators have the
 * same command and neither match the arguments.
 *
 * Revision 1.22  2001/07/20 14:01:13  rmikk
 * 1. Enabled THREE_D displays of data sets.
 * 2. Can now load Nexus files using IsawGUI.Util's load
 *    function
 *
 * Revision 1.21  2001/06/29 19:12:02  rmikk
 * Eliminated a "check nop" system output
 *
 * Revision 1.20  2001/06/25 19:07:03  rmikk
 * Fixed error witj a:b+c to allow expressions after the :
 *
 * Revision 1.19  2001/06/04 20:15:36  rmikk
 * Fixed Documentation
 *
 
 6-9-2000
   implemented Load, Display, Send(check) , Expressions with data sets, and Data Set Operators
   Ned to 
       extend the Load function to give variable names
       implement update and keep track of when a data set is changing
      need to implement vname[i] for looping
   
 7-5-2000
   implemented getResult.
   Tested a lot.  Released for testing

 7-12-2000:
   Introduced the symbols : < > and tightened the "end" parameter in 
   execute( String, int,int ) to work with the outside For and If-Else-ENdif structures
7-14-2000
   Display now works with int[]

9-14-2000
  
   -Fixed error report after the SEND command.  
      The seterror routine can now reset the error to no error

   -Implemented Boolean variables and expressions. 
   -Implemented Load "isd" files(JVM Binary Data Set files).
   -Implemented Load ".class" files( Not a filename. must be in ClassPath

10-1-30
  - Fixed errors in & 3 terms
  - Fixed errors in AND and  OR ing 3 or more "terms"

10-1-30
  - Started to incorporate Variable names for Data Sets.
    

12-1-00
  -Variable names for Datasets incorporated.  The title is no longer
   used as the variable name
  -Fixed error with ==
  -Fixed error when  array index calculation had an error
  -  & can be applied to a data set to yield its toString Value
 
12-12-00-
  -Tested inequalities to get correct answer(operateCompare)
  -fixed initt so all local string, data set and well as boolean variables are cleared(initt)
      May cause some problems with the macro run??

1/31/01-
  -implemented arrays as Vectors. Includes multidimensional arrays.
  - Bolstered variables to retain data type after first assignment

2-21-
  -Load with varname now makes this an array of DataSets  
      
*/
package Command;
//import IsawGUI.Isaw.*;
import IsawGUI.*;
import java.io.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import javax.swing.border.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import java.beans.*;
import java.util.*;
import java.util.zip.*;
import java.beans.*;
/** 
*  This class parses and executes one line of code using the values of variables 
*  obtained from  the execution of previous lines of code
*/ 
public class execOneLine implements DataSetTools.util.IObserver,IObservable ,
                                    PropertyChangeListener ,Customizer 
                                     
{
    public static final String ER_NoSuchFile                  = "File not Found";
    public static final String ER_NoSuchVariable              = "Variable not Found";
    public static final String ER_IllegalCharacter            = "Illegal Character";
    public static final String ER_MisMatchQuote               = "MisMatched Quotes";
    public static final String ER_MisMatchParens              = "Mismatched Parenthesis";
    public static final String ER_DataSetAlreadyHere          = "Data Set has already been loaded";
    public static final String ER_NotImplementedYet           = "Not implemented yet";
    public static final String ER_MissingOperator             = "Operation is missing";
    public static final String ER_ImproperArgument            = "Improper Argument";
    public static final String ER_MissingArgument             = "Argument is Missing";
    public static final String ER_NumberFormatError           = "Number Format Error";
    public static final String ER_NoSuchOperator              = "Operation is not Found";
    public static final String ER_FunctionUndefined           = "Function is undefined";
    public static final String  ER_OutputOperationInvalid     =" Could not Save";
    public static final String ER_MissingQuote                = "Quotation mark missing";
    public static final String ER_MissingBracket              =" Unpaired brackets";
    public static final String ER_ImproperDataType            =" Improper Data Type";
    public static final String ER_ExtraArguments              ="Extra Arguments";
    public static final String ER_No_Result                  =" Result is null ";
    public static final String ER_IMPROPER_DATA_TYPE          ="Variable has incorrect data type";
    public static final String ER_ReservedWord                =" Reserved word";
    public static final String  ER_ArrayIndex_Out_Of_bounds   =
                                                  "Array index out of Bounds";
    public static final String WN_Return     ="Return Statement executed";
    private static final String OP_Arithm                      ="+-*/^";
    private static final String Ops                           =OP_Arithm+"&:";
    private static final String EndExpr                       =",)]<>=";
    
    private Document logDocument = null;

    private boolean Debug= false;

   


    //-----Run Space  Variables --------------

   Hashtable   ds =new Hashtable();        //Copy of the data set(s) passed in
	                                    //   by the constructor
    
     
    Integer Ivals[]; 
    String Ivalnames[]; 

    Float Fvals[]; 
    String Fvalnames[];

    String Svals[]; 
    String Svalnames[];

    Hashtable BoolInfo = new Hashtable();

   
    Hashtable lds = new Hashtable() ;   //local dataset storage. Global at ds
    Object Result;                     //Storage for Intermediate Results of
                                       //  operations

    Hashtable ArrayInfo = new Hashtable();
    Hashtable MacroInfo = new Hashtable();                  //Stores Macros
   
    IObserverList  OL; 
    PropertyChangeSupport PC;

    // Contains user supplied operators 
    Script_Class_List_Handler SH = new Script_Class_List_Handler();

    //Error variables

     int perror;                      //position of error on a line

     int lerror;                     //line number of error.                        

     String serror;                 // error message.
 
    Vector Graphs = new Vector();                // Saves all displays so as to delete them when done.
    Vector Params = new Vector();		// Saves all Global parameters added on Script 
                                                //w. param execute
    

/** 
  
*/
    public execOneLine()
      {
        initt();        
        OL = new IObserverList();        
        PC = new PropertyChangeSupport( this );
        
      }

/**
*This constructor adds the data set to the variable space

*/
    public execOneLine( DataSet Dat, String vname )
      { 
	 initt();
         ds.put(Dat,vname);
         OL = new IObserverList();
         PC = new PropertyChangeSupport( this );
      }

/** 
 *This constructor adds the set of data sets to the  variable space.<P>
* The names of the variables are defined by their title.
*/
    public execOneLine( DataSet dss[], String vname )
      { 
          int i;
          initt();

          for( i = 0 ; i < dss.length ; i++ )
	     ds.put(dss[i],vname+"["+i+"]");//ds[i] = eliminateSpaces( ds[i] );
	            OL = new IObserverList();
          PC = new PropertyChangeSupport( this );
          OL = new IObserverList();

      }

 /**  
  *  Sets the document that will log the operations
  *@param doc    The document that logs operations
*/
 public void setLogDoc( Document doc)
    { logDocument = doc;
    }

/**
* Use this method to reset the error to false.<P>
* It is used to continue executing immediate instructions after an error occurs
*/
    public void resetError()
      {perror = -1;
        serror = null;
      }

/**
* @return   the error message or null if no error exists
*/
    public String getErrorMessage()
       {if( perror < 0 )
           return null;
        else 
           return serror;
       }

/**
*@return   returns the position in the string where the error occurred
*/
    public int getErrorCharPos()
       {return perror;
       }
/**
* This Method adds a data set to the global data sets of the command pane 
*
* These data sets will be removed when the initt routine is executed
* 
*@param  dss  the data set to be added
*@param   vname   the name that will be used by the command pane to refer to this data set
*/
public void addParameterDataSet(DataSet dss, String vname)
  { addDataSet(dss,vname);
    Params.addElement(vname.toUpperCase().trim());
   }
/**
* This method allows outside data sets to be added to the variable space
*@param
*      dss    The data set to be added with the name vname
*@param
*      vname  The name that will be used by the command pane to refer to this data set
 
*/

public void addDataSet(DataSet dss, String vname)
   { ds.put( vname.toUpperCase().trim() , dss );
    
      }
/**
* This method allows outside data sets to be added to the variable space
*@param
*      dss    The data set to be added
*
*NOTE: The name that the command pane will use to refer to this data set is DSx where
* x us the value of the DataSet Tag attribute

*/
    public void addDataSet( DataSet dss )   
      {  
        String vname;
        if(Debug)System.out.println("IN ADD DATASET");
        long tag = dss.getTag();         

        //if(tag != null)
          { vname ="ISAWDS"+new Long(tag).toString();
           }        
        //else vname = dss.getTitle();
        if(Debug)System.out.println("EndADD DATA SET vname="+vname);
        addDataSet(dss, vname);
        dss.addIObserver(this);
      }
/**
*  Removes all displays created by the CommandPane
*
* @see  #Display( DataSetTools.dataset.DataSet, java.lang.String , java.lang.String)
*/
    public void removeDisplays()
    {  if( Graphs == null)
	return;
       if( Graphs.size() <= 0 )
	   return;
       ViewManager vm;
       for( int i = 0 ; i < Graphs.size() ; i++)
	   { vm  = (ViewManager)Graphs.elementAt( i );
             vm.destroy();
             vm= null;
           }  
        vm= null;
        Graphs= new Vector();      
       
    }
   
/**
*execute starts lexing, parsing, and executing the String S until the end of the String, a colon or
*  an unmatched parenthesis or a comma or at the same level as the start's character
*@param       S       The string to be executed
*@param       start   The starting character
*@param       end     The last character of the string to be considered
*
*@return
*       The position of the first character that was not considered
*/
    public int execute( String S , int start , int end )
      {int i,
           j,
           j1,
           kk,
           retn;
       String C;
          if( start != 0)
             if(Result != null )
	         {//seterror( start , "internalerrorn" );
		     // return start;
                 }
         
          S = Trimm( S );  
          if( end > S.length() )
             end = S.length();
          
          if( perror >= 0 )
            return perror;
         
          if( Debug )
            System.out.println( "in execute String" + S + " , " + start );
          if( S == null) 
            return 0;
          if( start < 0 )
            return 0; 
          if( S.length() <= 0 )
            return 0;
          if( end <= 0 ) 
	      return 0;
          if( start >= end )
            return S.length();
          if( (start >= S.length()) || ( start >= end) )
            return start;
      
       i = skipspaces( S , 1 , start );
       if( i > end) i = end;
      
       j = findfirst( S , 1 , i , " (+-*=;:'/^+,[]<>&)\",","" );
       if( j > end) j = end;
       if( (i >= 0) && (i < S.length() ) && (i < end) )
	 if(" (+-*=<>;:'/^+,[]&)\"," .indexOf( S.charAt( i ) ) >= 0 )
	   j = i ;
     
       
             if( Debug )
               System.out.print("i ,j=" + i+ "," + j);
       if( (j < i) ||  (i < 0) ||  (i >= end) ||  (i >= S.length() ) )
            {return i;
            }
      j1 = skipspaces( S , 1 , j );
      if( j1 > end) j1 = end;
      if( j <= i ) 
         {C = S.substring( i, i + 1 ); 
          j1 = j; 
         }
      else 
         C = S.substring( i , j ).trim();
     
      if ( (j1 >= 0) && (j1 < S.length()) && ( j1 < end))
        if(  (S.charAt( j1 ) == '[') && (j==j1)&&(j1>0)&&(i<j1) )  // Check for vname[]
	  {int j2=findfirst(S,1,j1+1,"]","[]");
           if((j2>=end)||(j2>=S.length()))
              {seterror(j1, ER_MissingBracket);
                return j1;
              }
            j1=j2+1;
            C = S.substring(i,j1).trim().toUpperCase();
            j1 = skipspaces(S,1,j1);
            
            j=j1;
            if(Debug)
              System.out.println("[], C,j="+C+","+j);
           /*
           C = C +  brackSub( S , j1 , end );
	  if(Debug)
	      System.out.println("aft brack sub C,err=" + C + perror);
	   if(perror >=0) return j1;
	   j = finddQuote( S , j1 + 1 , "]" , "[]()");
           if( j > end) j = end;
           if( (j < 0)  || ( (j >= S.length()) || ( j >= end)))
	     { seterror( j1 , ER_MissingBracket+j+"A");
	       return j1;
	     }
           if( S.charAt( j ) != ']')
             { seterror( j1 , ER_MissingBracket+j+"B");
	       return j1;
	     }
           j = j + 1;
           j1 = skipspaces( S , 1 , j);
           if( j1 > end) j1 = end;
          */ 
          }
      if( C.charAt(0) != '\"' )
        C = C.toUpperCase();
            if( Debug )
              System.out.print( "C=" + C );
      if( C.equals("LOAD") )
        { int ii = execLoad( S , j , end );
                if( start == 0 )
                  Result = null;
	           if( Debug )
                      System.out.println( "Aftret ExecLoac ret=" + ii );
             return ii;                             
                 
          }
       else if( C.equals( "DISPLAY" ) )
         {   if( start != 0 )
               {seterror( i , "Must be the First command on a line" );
	        return i;
               }
          retn = execDisplay( S , j , end );
	  Result = null;
	  return retn;
         
          }
       else if( C.equals("SAVE") )
         {
             if( start != 0 )
               {seterror( i , "Must be the First command on a line" );
	        return i;
               }
             retn = execSave( S , j , end );
             Result = null;
             return retn;
              
         }
       else if( C.equals( "SEND") )
	 {   if(start!=0)
               {seterror( i , "Must be the First command on a line" );
	        return i;
                }
	        if(Debug)
	          System.out.println("Send j1="+j1);	
	      retn =execSend(S,j1,end);
              Result = null;
             return retn;             
         }
        else if ( (C.equals( "REM" )) && (start == 0)  )
	  {    Result = null; 
               return S.length(); 
          }
       else if( C.equals("RETURN"))
          { j = skipspaces( S, 1, j);
            if( j < S.length())
              { 
                 j = execute( S, j, S.length());               
                if( perror >= 0)
                   return j;
              }
           seterror( j , WN_Return );
           return j;
          }            
	  
        if( Debug )
           System.out.println( "1C=" + C + ":" + (C=="(") ); 
       
    
       if( i >= end )
         {Result = null;
          return end;
         }
       else if( i >= S.length() )
         {Result = null;
          return S.length();
         }
       // Start to new stuff
       else 
	 { if((j1<S.length())&&(j1>=0)&&(j1 < end))
             if( (start==0) &&(S.charAt(j1) == '=') )
		 {if(Debug) System.out.println("Assignement op "+ C); 
		 if( start != 0)  
		   {seterror( j1 , ER_IllegalCharacter+"A");
		    return j1;
		   }  
                 kk = execute( S , j1 + 1 , end);            
	         if( perror < 0 )
                   Assign( C.toUpperCase() , Result );
                 else 
                   return perror;
                 if( perror >= 0) perror = kk;
	         int cc;
	         Result = null;
                    if( Debug )
                       System.out.println("kk=" + kk+","+perror);
                  if( ( kk  <= j1 + 1) ||  (perror >= 0) )
	             {return S.length() + 1;
                     }
                  else 
                    return skipspaces( S , 1 , kk );
                 }
            return execExpr(S,start,end);
            
           }
               
     
     }
    
	   
    
//             Executes the LOAD command.
// Brings the data sets into the local workspac
    private int execLoad( String S , int start, int end )
      {String  C,
               filename,
               varname;
       DataSet dss[];
       int     i,
               j;

       if( Debug )
         System.out.println( "In EXecLoad " + start);
      start = skipspaces(S , 1 , start);
      i = start;
      
      if( (start < 0) || (start >= S.length()) || (start >= end))
        { seterror( S.length(),"Internal ErrorSL");
          return start;
        }
      Vector V;
      if( S.charAt( i ) == '(')
           V = getArgs( S , i + 1 , end);
      else
          V = getArgs(S , i ,end );
      
      if( V == null )
        return perror;
      if( V.size() <=1 )
        {seterror( start, "ER_MissingArgument");
         return start;
        }
       if( perror >= 0)
           {  perror = start;          
              return perror;
           }
      j =((Integer)V.lastElement()).intValue(); 
     

      j = skipspaces( S , 1, j );
          if( S.charAt( i ) == '(')
             if( (j >= end) || ( j >= S.length()))
               seterror( i, ER_MisMatchParens);
             else if( S.charAt( j ) != ')')
               seterror( i, ER_MisMatchParens);
             else j = skipspaces( S , 1 , j+1);
      int x = 0;
    

      if(Debug)
        System.out.println("Load after Arg get");
      try{
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
       return j;
    
     
      }
   public void Load ( DataSet dss[] , String varname)
    { int i;
      DataSet DDs;
      if( dss == null )
        {seterror( 1000 , "Data File Improper" );
         return ;
        }
       if( dss.length <= 0 )
         {seterror( 1000 , "Data File Improper" );
          return  ;
         }
        String vname;
        for( i = 0 ; i < dss.length ; i++ )
         {DDs = eliminateSpaces( dss[i] );
           vname = DDs.getTitle();
	  if( varname != null)
	    if( varname.length() > 0 )
		if( varname.toUpperCase().charAt(0) >'Z')
		    {}
                else if( varname.toUpperCase().charAt(0) < 'A')
                    {}
                else vname=(varname + "["+new Integer(i).toString().trim()+"]");
      /*      Object X = getVal( DDs.getTitle());
    
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
* Used by other parsers to load a file of data sets into the local space
*@param       filename   The name of the file of data sets(.run extension)<BR>
*                                    the file from save command(.isd extension)<BR>
*                                    class(with dots)          (.class extension)<BR>                 
*@param       varname    The name these data sets will be referred to.  <BR>                          
*                        They will be called varname0, varname1, ... <BR>
*                        The .class files must subclass DataSetTools.operator.  Name is from getCommand 
*
*@return     The number of data sets loaded
* Note: if varname is null, the names will be derived from the filename and data set type.
*/
    public DataSet[] Load( String filename , String varname )
    {   DataSet DDs,
                dss[];
        int i,
            j;

        Util util = new Util();
        filename = StringUtil.fixSeparator(filename);
     String Ext;
     i= filename.lastIndexOf(".");
     if( i<0)// Not supporting classnames yet
       { seterror( 1000, ER_ImproperArgument+" 1");
         return null;
       }
   
     Ext= filename.substring(i+1).toUpperCase();
    
     if( Ext.equals("ISD"))
        {dss= new DataSet[1];
         dss[0]= DataSet_IO.LoadDataSet(filename);
         if(dss == null)
           {seterror( 1000 , "Data File Improper" );
             return null;
           }
        }
     else if(Ext.equals("CLASS"))
         {  try{       
               Class X = Class.forName(filename.substring(0,i));               
                Object O = X.newInstance();
                if( !(O instanceof Operator))                 
                  {seterror(1000,"Improper Class");
                   return null;}
                MacroInfo.put( ((Operator)O).getCommand(), O);
                return new DataSet[0];
               
             }
           catch(Exception ss)
            {seterror(1000, "Unknown Class");
             return null;
            }
         }
      else// (Ext.equals("RUN")or hdf or)
       {try{

             dss = util.loadRunfile( filename );
             

           }
        catch( Exception s)
          { dss = null;
            seterror( 1000, s.toString());
            return dss;
          }
         
        
         util = null;
         if( dss == null )
            {seterror( 1000 , "Data File Improper" );
             return null;
             }
         if( dss.length <= 0 )
           {seterror( 1000 , "Data File Improper" );
            return  null;
            }
        }
     
        

        for( i = 0 ; i < dss.length ; i++ )
	 {DDs = eliminateSpaces( dss[i] );
          

          String vname=DDs.getTitle();
	  if( varname != null)
	    if( varname.length() > 0 )
		if( varname.toUpperCase().charAt(0) >'Z')
		    {}
                else if( varname.toUpperCase().charAt(0) < 'A')
                    {}
                else vname=(varname +"["+ new Integer(i).toString().trim()+"]");
         
          if( Debug) System.out.print("error="+perror+",");       
        
          

          Assign( vname, DDs);
            

           
          if( Debug )
            System.out.println("Assign Dat set=" + DDs.getTitle());
         }
	return dss;
      }



    // Executes the DISPLAY comman
    //    It Finds the data set that the variable refers to
    //    Then it creates a viewer for the data set
 
    private int  execDisplay(     String    S,
                                 int       start, 
                                 int       end)

      {int    i,
              j;
       DataSet DS;

   
      i = skipspaces(S,1,start);
      if( i > end) i = end;
      if( Debug )
        System.out.print("Disp A ,i" + i);

       i = start;
     
      if( (start < 0) || (start >= S.length()) || (start >= end))
        { seterror( S.length(),ER_MissingArgument);
          return start;
        }
      Vector V = new Vector();
      if( S.charAt( i ) == '(')
           V = getArgs( S , i + 1 , end);
      else
         V = getArgs( S , i , end );
     
      if( V == null )
        return perror;
      if( V.size() == 0)
        return perror;
     // if( V.size() <=1 )
     //   {PC.firePropertyChange( "Display"  , null , (Object)"(null)" );
     //    Integer XX = new Integer(start);
     //    if(V.size()> 0)
     //     { XX = (Integer)(V.elementAt( V.size() - 1 ));            
     //     } 

     //    return XX.intValue();
     //   }
     // else if( (V.size() == 2)&&(V.elementAt(0) == null ))
      //   {PC.firePropertyChange( "Display"  , null , (Object)"(null)" );

      //   return start;
      //  }
      //
      if( V.size() > 4)
        { seterror( start, ER_ImproperArgument+"A" );
          return start;
        }
      j =((Integer)V.lastElement()).intValue(); 
      j = skipspaces( S , 1, j );
       
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

      int x = 0;
      String DisplayType="IMAGE"; 
      String FrameType="External Frame";
      if( V.size() > 1)
        if( V.elementAt( 0 ) == null)
          {PC.firePropertyChange( "Display"  , null , (Object)"(null)" );
           return j;
          }
       if( V.size() <= 1)
          {PC.firePropertyChange( "Display"  , null , (Object)"No Result" );
           return j;
          }

      try{
          

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
          Display( DS , DisplayType , FrameType );
         
          if( perror >= 0 ) 
             perror = start;
          return j;
         }
     catch( ClassCastException s)
        { if( x > 1)
          if( (Result == null) || (V.size() > 2 ) ) 
            {seterror( i , ER_ImproperArgument+" " + x  );
             if( Debug)
              { if( x-1 < V.size())
                 System.out.println( "V and class = "+ V.elementAt(i) +","+V.elementAt(i).getClass());
               else System.out.println("Aft Display args ="+ DisplayType+","+FrameType);
              }
             return i;
            }
        }
      Result = (Object) V.elementAt(0);
      if( Debug )
         System.out.println("In Display Res="+Result+Result.getClass());
     
      if( (Result == null) || (V.size() > 2 ) ) 
        {seterror( i , ER_ImproperArgument+"B" );
         return i;
        }
   

     // if( Result instanceof DataSet )
      //   { new JDataViewUI().ShowDataSet( (DataSet)Result , "External Frame" , IViewManager.IMAGE );
      
       //  }
     // else
	{ String SS;
	  SS = "";
  	 if( Result instanceof Integer )  
            SS = SS + (Integer)Result;
         else if( Result instanceof Float ) 
           SS = SS + (Float)Result;
         else if( Result instanceof String ) 
           SS = SS + (String)Result;
         else if ( Result instanceof int[] )
	   { int Z[]= (int[]) Result;
             
             SS = SS + "[";
             for( i = 0 ; i < Z.length ; i++ )
               { SS = SS + Z[i];
                 if( i + 1 < Z.length)
                   SS = SS + ",";
               }
             SS = SS + "]";

           }
         else if( Result instanceof Vector)
          {SS = Vect_to_String((Vector)Result);
          }
         else if( Result == null)
	     SS = "(null)";
         else
	     SS = SS + Result.toString();
	     // return end;
         PC.firePropertyChange( "Display"  , null , (Object)SS );

         }
       Result = null;
  
      return end;
  
      }
 /** Utility that converts a Vector to a displayable string<P>
 *Elements of a vector can be another vector, a Data Set.<Br>
 * Strings and Special strings will be quoted
 *@param   V     The vector to be converted to its string form
 *@return  A String representation of the number.  
 *NOTE: If the string representation is run through execOneLine, hopefully
 * the vector form is reconstructed
 */
 public static String Vect_to_String(Vector V)
   {
    if(V == null) return "null";
    String res="[";
   
    int i;
    for( i=0; i< V.size(); i++)
      {Object O= V.elementAt(i);
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
 *@param      ds            The data set to be viewed
 *@param      DisplayType   The type of display
 *
 * NOTE: DisplayType must be "IMAGE" , "Scrolled_Graph", "Table", Three_D",
 *       or "Selected_Graph"<Br>
 *       FrameType must be "External Frame" or "Internal Frame". 
 */
    public void Display( DataSet ds , String DisplayType , String FrameType )
      {  String X = null;
         if( Debug) System.out.println( "IN DISPLAY1 args="+DisplayType +
                                      ","+FrameType);
         if( DisplayType.toUpperCase().equals("IMAGE"))
                 X = IViewManager.IMAGE;
         else if( DisplayType.toUpperCase().equals("SCROLLED_GRAPH")) 
              X = IViewManager.SCROLLED_GRAPHS;
         else if( DisplayType.toUpperCase().equals("SELECTED_GRAPH")) 
             X = IViewManager.SELECTED_GRAPHS;
         else if( DisplayType.toUpperCase().equals("THREE_D"))
	     X = IViewManager.THREE_D;
         else if( DisplayType.toUpperCase().equals("TABLE"))
             X= IViewManager.TABLE;
         else
           { seterror( 1000 , ER_ImproperArgument+" "+ DisplayType );
             return;
           }
	 ViewManager  vm = new ViewManager(ds , X );
        
         Graphs.addElement( vm );
      }

    private int  execSave( String S , int start, int end )
      { 
	  
       
       int i = start;
       int j;
       String filename;
      if( (start < 0) || (start >= S.length()) || (start >= end))
        { seterror( S.length(),"Internal ErrorSL");
          return start;
        }
      Vector V;
      if( S.charAt( i ) == '(')
           
         V = getArgs( S , i + 1 , end);
      else
         V = getArgs( S , i , end );
      if( V == null )
        return perror;
      if( V.size() <=1 )
        {seterror( start, ER_MissingArgument);
         return start;
        }
      else if( V.size() > 3)
        { seterror( start, ER_ImproperArgument+"A" );
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
          
         }
     catch( ClassCastException s)
        { if( x > 1)
          if( (Result == null) || (V.size() > 2 ) ) 
            {seterror( i , ER_ImproperArgument+" " + x  );
             
             return i;
            }
        }
          
       return j;  
      }

/**
 *This Method saves the data set to a file
 *@param       ds        The data set to be saved
 *@param      filename  The filename to which the data set is to be saved( as java binary format)

 */
    public void Save( DataSet ds , String filename )
    { //System.out.println("Start Save Sub with ds, filename "+ ds +","+filename);
       // try{  
	    /*FileOutputStream fos = new FileOutputStream(filename);
          GZIPOutputStream gout = new GZIPOutputStream(fos);
          ObjectOutputStream oos = new ObjectOutputStream(gout);
          oos.writeObject(ds);
          oos.close();
	    */
	  if(!( new DataSet_IO().SaveDataSet( ds , filename)))
            { seterror(1000, ER_OutputOperationInvalid);
           }
        //catch(Exception s)
	  {
	     // seterror(1000, ER_OutputOperationInvalid);
             
	  }

       }

    
   // Now usess two arguments.
   // 1 arg --> IObserver.update( this, arg1)
   //2 args--> IObserver.update( arg2,arg1)
    private int execSend(String S, int start, int end)
      {int i, j;
       i = skipspaces( S , 1 , start );
       if( (i < 0) || ( i >= S.length()) || ( i >= end ))
         { seterror( start , ER_MissingArgument);
           return i;
         }
       Vector args;
       if( S.charAt(i) == '(' )
          args = getArgs( S , i + 1 , end );
       else
          args = getArgs( S , i  , end );
       if( args == null) 
            {
             return i;
             }
       if( (args.size() < 2) || (args.size()>3))
          { seterror( i, "Internal Error 7A");
            return i;
           }
        try{
          j = ((Integer)(args.lastElement())).intValue();
            }
        catch( Exception uu)
          {seterror( i, "Internal Error 7A");
            return i;
          }
       if( S.charAt( i ) == '(')
         if( (j < 0 ) || ( j >= S.length() ) || ( j >= end ))
           { seterror ( j , ER_MisMatchParens );
             return j;
           }
         else if( S.charAt(j ) != ')')
           { seterror ( j , ER_MisMatchParens );
             return j;
           }
         else j++;
       j = skipspaces( S , 1, j);        
       if(Debug)
	   System.out.print("Send"+perror+","+j);
       if( perror >= 0 )
         return S.length() + 2;
       if(Debug)
	   System.out.print("Send after error");
     /*  if( !( Result instanceof DataSet ) )
         {if(Debug)
	   System.out.println("in Not Correct Data Type");
          seterror( start , ER_ImproperArgument );
          return start;
         }
       if(Debug)
	   System.out.println("Send er observ");
     */
        Object arg1,arg2;
        if( j<2) 
           {arg1=this;
            arg2= args.firstElement();
           }
        else
           {
              arg2= args.firstElement();
              arg1= args.elementAt(1);
           }
       if( arg1 instanceof DataSet)
          ((DataSet)arg1).addIObserver( this);

       if( arg2 instanceof DataSet)
          ((DataSet)arg2).addIObserver( this);
 
       OL.notifyIObservers( arg1,arg2 );
       Result = null;
       return end;
      }

 /**
   Sends the data set to all Iobservers
  *@param   ds    The data set that is to be sent
 * Not implemented yet. 
  *@see  DataSetTools.util.IObserverList#addIObserver(DataSetTools.util.IObserver)
          addIObserver  <P>
  *
 
  */
    public void Send( DataSet ds)
      {
      }

/**
   Returns the Result of the last expression <P>
   Statements "should" return null<P>
   This routines allows the execute routine to be used on expressions and not just statements
   *@return   The value of the last operation.
*/
    public Object getResult()
      {return Result;
      }  
/**
*  Gets the ith global Data Set

*/
  public DataSet[] getGlobalDataset()
    { if( Debug) 
        {System.out.println( "in getGlobalDataSet") ;
	if( ds == null )
	 if(Debug)System.out.println("ds is null");
        }
       Enumeration D = ds.elements();//.toArray();
       DataSet DD[] ;
        //D =  new DataSet[DO.length];
       //if(Debug) System.out.println("sizes="+ds.size()+","+DO.length+","+D.length);
       //for(int i=0; i<DO.length;i++)
          Vector VV= new Vector();
	  int i;
          for( i=0; D.hasMoreElements(); i++){Object X=D.nextElement();}
	     
	  DD = new DataSet[i];
	  D= ds.elements();
	  for( i=0; D.hasMoreElements(); i++)
           {DD[i]=(DataSet)(D.nextElement());
           }
 
/*      Collection dsvalues= ds.values();
      if(Debug)System.out.println("getGlobalDSs nkeys="+dsvalues.size()+","+dsvalues.getClass());
      Object DO[]= dsvalues.toArray(new DataSet[0]);
      if(Debug)System.out.println("getGlobalDSs"+DO.length);
      DataSet[] D =(DataSet[])(DO);
*/
      
      return DD;
    }
//Doe whole expression with And's, Or's, Not's, <, <=, and Algebraic Expressions
  private int execExpr(String S, int start, int end)
   {int i,i1;
         int j;
         Object Res1;
         boolean done;
         Result = null;
        if(Debug)System.out.println("ExExpr start="+start);
         i=skipspaces(S,1,start);
         Result= null;
         if(i>=end) return i;
         if(i>=S.length())return i;
         j=execNonAndOrExpr(S, i, end);
           if(perror>=0) return perror;
         char op=0;
         j=skipspaces(S, 1, j);
         if( j >= end) return j;
         if(j >= S.length()) return j;
         if(S.substring(j).toUpperCase().indexOf("AND")==0)
              {op='#'; j=j+3;}
         else  if(S.substring(j).toUpperCase().indexOf("OR")==0)
              {op='|';j=j+2;}
         else
            {//seterror(j,ER_IllegalCharacter+"U"); could be left paren , etc
             return j;
            }
          if( (skipspaces(S,1,j)>=end)||(skipspaces(S,1,j)>=S.length()) )
            { seterror(j,ER_MissingArgument+"U1"); return j;}
          if(" (+-".indexOf(S.charAt(j))>=0){}
          else
             {seterror(j,ER_IllegalCharacter+"U2");
              return j;
               }
           Res1=Result;
          done=false;
          if(Debug) System.out.println("in ExecExprA"+j);
          while(!done)
            { j=execNonAndOrExpr(S, j, end);
              if(Debug)System.out.println("XX perror and j"+perror+","+j);
              if(perror>=0) return perror;
              operateArith( Res1,Result,op);
              op=0;
             j=skipspaces(S, 1, j);
             if( j >= end) return j;
             if(j >= S.length()) return j;
             if(Debug) System.out.println("Left="+S.substring(j).toUpperCase());
             if(S.substring(j).toUpperCase().indexOf("AND")==0)
              {op='#';
               j = j + 3;
               }
             else  if(S.substring(j).toUpperCase().indexOf("OR")==0)
              {op='|';
                j = j + 2;
               }
            else
              {op=0;
              }
             if(Debug) System.out.println("YY op,j="+op+","+j);
             if(op == 0) done = true;
             else if( j >= end) done = true;
             else if( j >= S.length()) done = true;
             Res1= Result;
            
            }

          return j;     

   }
 // executes to an And or an Or
 private int execNonAndOrExpr( String S, int start, int end)
   {int j;
    start=skipspaces(S , 1 , start );
    // Check for NOT
    Result = null;
    if(Debug) System.out.println("exenon&| expr,start="+start);
    if(start >= end) 
        return end;
    if(start >= S.length()) 
         return end;
    

    if( S.substring(start).toUpperCase().indexOf("NOT")==0)
      { if((start+3 >=end) || (start + 3 >= S.length()))
          {seterror(start + 3, ER_MissingArgument+"V"); 
           return start+3;
          }
        if( " (".indexOf(S.charAt(start+3))>=0)
          { j=execNonAndOrExpr(S, start+3,end);
             if(Debug)System.out.println("in NOT case"+perror+","+Result);

            if(perror>=0) return perror;
            if( Result == null)
               {seterror(start+3, ER_ImproperArgument+"V1");
                return start+4;
               }
            else if(Result instanceof Boolean) 
               Result = new Boolean( !((Boolean)Result).booleanValue());
            else if( Result instanceof Integer)
              if( ((Integer)Result).intValue()==0)
                Result = new Boolean(false);
              else Result = new Boolean(true);
            else
              {seterror(start+3, ER_ImproperArgument+"V2");
               return start+4;
               }
           }
        else j=execNonAndOrNotExpr(S, start,end); 
       }
        
      else j=execNonAndOrNotExpr(S, start,end);  
  
      
      return j;

       

   }
 private int execNonAndOrNotExpr( String S, int start, int end)
  {int j;
    start= skipspaces( S, 1 , start);
    Result = null;
    if( (start >= end) || (start >= S.length())||( start < 0)) return start;
    if(Debug)System.out.println("in execnon&|!, start="+start);
    j=execArithm( S, start, end);
    if(Debug) System.out.println("in execnon&|!A j="+j+","+perror);
    if( perror >= 0 ) return j;
    j = skipspaces( S, 1, j);
    if( (j >= end) || (j >= S.length()) || (j < 0) ) return j;
    if(Debug) System.out.println("in execnon&|!Bj="+j+","+perror);

    if("=<>".indexOf(S.charAt(j))<0)
      {//seterror( j, ER_IllegalCharacter+"W");  could have been true or false
       return j;
      }
     if(Debug) System.out.println("in execnon&|!C j="+j+","+perror);

    if( (j+1 >= S.length()) ||(j+1 >= end))
      {seterror( j+2 , ER_MissingArgument+"W1");
       return j+2;
       }
    char op=S.charAt(j);
    if( "=>".indexOf(S.charAt(j+1))>=0)
       {
        if(S.charAt(j+1)=='=')
          { if(op != '=')op = (char)((byte)op + 5);}
        else op =(byte)'='+5;
        j++;
        }
         
    j++;
    if(Debug) System.out.println("in execnon&|!D j,op="+j+","+op);

    Object Res1=Result;
    j=execArithm(S , j , end );
    if(Debug) System.out.println("in execnon&|!E j="+j+","+perror);

     if(perror >=0) return j;
     j= skipspaces(S, 1, j);
     operateCompare(Res1,Result, op);
     if(perror >=0) perror = j;
     return j;

    
  }
   
//Result contains the first operand.  Start has the operation
// New Revision: Result=null; Does arithmetic expressions from start
//  Executes the whole arithmetic expression to "end"
    private   int execArithm(String S, int start, int end) 
      {  int i,i1;
         int j;
         Object R1,R2;
         boolean done;
        Result = null;
        if(Debug)System.out.println("ExArithm start="+start);
         i=skipspaces(S,1,start);
         Result= null;
         if(i>=end) return i;
         if(i>=S.length())return i;
         j=execOneTerm(S, i, end);
         if(Debug)System.out.println("ExArithA"+Result+","+perror+","+j);
          if( perror >= 0 ) return perror;
          R1=Result;

         i=j;
         done = false;
         if( (i >= end) ||  (i >= S.length()) ||  (i < 0) )
           done = true;
         else if( "),]<>=".indexOf( S.charAt( i ) ) >= 0 )
	   done = true;
         else if("+-&:".indexOf(S.charAt(i))<0)
           done = true;
         if(Debug)System.out.println("ExArithB,done,perror,char"+done+","+perror+","+j);
         
         while( !done )
           {
	     if(S.charAt(i)==':')
		{ Object R = Result;
                  j=execArithm(S, i+1, end);
		  if( perror >= 0) return perror;
                  if( (j >= end) ||(j>=S.length()))
                     {}
                  else if("),]<>=".indexOf(S.charAt( j ))< 0)
                     {seterror( j, ER_IllegalCharacter);
                      return j;
                     }
                 operateArith(R, Result, ':');
                 if( perror >=0)return perror;
                 else return j;
                }

            j = execOneTerm( S , i + 1 , end );
            if(Debug)System.out.println("ExArithC"+Result+","+perror);

               if( perror < 0 )
	         {operateArith( R1 , Result , S.charAt( i ) );
	          R1 = Result;
                    if( perror >= 0)
		      { perror = i + 2 ;
		        return perror;
                      }
                 }
              else
	         return perror;

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
    

//start is start of a new Factor, previous Result should have been saved.
    private int execOneFactor( String S , int start , int end )//go til * or /
      {int i,
           j,
           j1;
       String R1;
       if(Debug)
	   System.out.print("Exec1Fact st"+start+","+end);
       i = skipspaces( S , 1 , start );
          if( (i >= S.length()) || ( i >= end ) )
	    {seterror( i , ER_MissingArgument +"B");
	     return i;
	    }
          if( i < 0 )
	    {seterror( i , "internal error" );
	     return S.length() + 5;
	    }
             if( Debug )
               System.out.println("in exe1Fact start=" + i);
       if(S.charAt(i) == '[')
        {
          Vector V = getArgs(S,i+1, end);
          if( V == null)
            return i;   //error occured
          int n= ((Integer)V.lastElement()).intValue();
          if( (n >= end) || ( n < 0)|| (n>=S.length()))
            {seterror( n, ER_MissingArgument);
             return n;
              } 
          if( S.charAt(n) != ']')
            {seterror( n, ER_MissingBracket);
             return n;
            }
          if( V.size()>0)
            V.removeElementAt(V.size()-1);
            Result = V;
            return n+1;
        }
       if( S.charAt( i ) == '\"' )
         {String S1 = getString( S , i );
	 if( perror >= 0)
	     {perror = i;
	      return i;
             }
	 
	  j = i + S1.length() + 2;

          if(Debug)
	     System.out.println("aft getStr"+S1+","+i+","+j);
          if( (j > end) || ( j > S.length()))
             { perror = j;
               serror = ER_MisMatchQuote ;
               return j;
             }
          Result = S1; 
	  if(perror < 0)	  
	     return skipspaces( S , 1 , j );		  
          else  
	    {perror = i;
             return i;
            }
    
	      
         }
       if( ( S.charAt( i ) == '-' ) ||  ( S.charAt( i ) == '+' ) )
         {
          j = execOneFactor( S , i + 1 , end );                
	     if( perror >= 0 )
               return perror;
	  Object R3 = Result;
          
	  if( S.charAt( i ) == '-' )
            operateArith( R3 , new Integer( -1 ) , '*' );
                if( Debug )
                   System.out.println("in un-=exe1Fac Res=" + R3 + "," + Result);
	     if( perror >= 0 )
               {perror = i;
                return perror;
               }
	  j = skipspaces( S , 1 , j );
		  
	  return j;
	         
         }
      if("*/".indexOf(S.charAt(i))>=0)
        {seterror(i, ER_IllegalCharacter+"J");
         return i;
        }
      if( S.charAt(i) == '(' )
        {      if( Debug )
                 System.out.println("in LParen,i=" + i);
          j = execute( S , i + 1 , end );
	  if( ( j < 0 ) ||  ( j >= S.length() ) || ( j >= end ) )
	    {seterror( i , ER_MisMatchParens );
	     return i;
	    } 
	  else if( S.charAt( j ) != ')' )
            { seterror( i , ER_MisMatchParens );
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
     

      j = findfirst( S , 1 , i , "+-*<>(=&^/):[]{},\" " , "" );
      if( j > end) j = end;
      j1 = skipspaces( S , 1 , j );
      if( j1 > end) j1 = end;
    
         if( ( j < 0 ) ||  ( j > S.length() ) ||  ( j1 > S.length() )
               || ( j > end) || ( j1 > end) )
           {seterror( S.length() + 3 , "internalerrorp" );
            return S.length() + 3;	
           } 
         else if( (j1< S.length()) && ( j1 < end))
	   if(S.charAt(j1) == '\"')
	     { seterror( j , ER_IllegalCharacter+"K");
	       return j;
	     } 
       
	     
      String C;
      C = S.substring( i , j );
      
      if ( (j1 >= 0) && (j1 < S.length()) && ( j1 < end ) )
         if(  S.charAt( j1 ) == '[' )                // Check for []
	  { int j2=findfirst(S,1,j1+1,"]","[]");
	  //System.out.println("j2="+j2+","+j1+","+i+","+S);
            if((j2>=end) ||( j2 >= S.length()))
              {seterror(j1,ER_MissingBracket +" at "+j1);
               return j1;
              }
             j1=j2+1;
             C = S.substring(i,j1);
             j=j1;
            /*C = C +  brackSub( S , j1 , end );
	  
	   if(perror >=0) return j1;
	   j = finddQuote( S , j1 + 1 , "]" , "[]()");
           if( j> end) j = end;
           if( (j < 0)  || ( (j >= S.length())||(j >= end)))
	     { seterror( j1 , ER_MissingBracket+j+"C");
	       return j1;
	     }
           if( S.charAt( j ) != ']')
             { seterror( j1 , ER_MissingBracket+j+"D");
	       return j1;
	     }
           j = j + 1;
           j1 = skipspaces( S , 1 , j);
           */
          }

 
    

      if( (j1 < S.length()) && ( j1 < end ) )
        if( S.charAt( j1 ) == '(' )//function
          {
           j = execOperation( C , S , j1 , end );
           j = skipspaces( S , 1 , j);
           if( (j >= end) || (j >= S.length()) ) return j;
           if( j < 0) return j;
           if( S.charAt( j) != '^') return j;
           Object R3= Result;
                j = execOneFactor( S , j+1 , end );
                if( perror >= 0) return j;
                operateArith( R3 , Result , '^' );
                if(perror >= 0) perror = j;
                return j;

          }     
	else if( "([{}".indexOf( S.charAt( j1 ) ) >= 0 )
	  {seterror( j , ER_IllegalCharacter+"L" );
	   return j;
	  }
      
      boolean valgot = false ;
      if(C.toUpperCase().equals("AND") ||C.toUpperCase().equals("OR")
         ||C.toUpperCase().equals("NOT"))
        {seterror(j,ER_IllegalCharacter+"L1");
         return j;
         }
      if(Debug)System.out.println("Ex1Fact ere get 1 numb"+perror);
      try{
	  Result = new Integer( C );
          if(Debug)System.out.println("Result="+C+","+perror);
          if( (j < S.length()) && ( j < end )) 
	    {if( S.charAt(j) != '^')return j;
             valgot = true; 
            }
          else return j;
           
	 }
      catch( NumberFormatException s ){}

      if(!valgot)
      try{
          Result = new Float( C );
          if( (j < S.length()) && ( j < end )) 
	    {if( S.charAt(j) != '^')
                return j;
             valgot = true; 
            }
          else return j;
        
	 }
      catch( NumberFormatException s ){}
      
     
      if(!valgot)
	{
        Result = getVal ( C);
        if( perror >= 0)
	   perror = i;
        if( (j < S.length()) && ( j < end )) 
	    {if( S.charAt(j) != '^')return j;
	    valgot = true;
            }
        else return j;
        }

       if( (perror < 0))
	 if( (j < S.length()) && ( j < end ))
           if( S.charAt(j) == '^' ) 
	       {Object R3= Result;
                j = execOneFactor( S , j+1 , end );
                if( perror >= 0) return j;
                operateArith( R3 , Result , '^' );
                if(perror >= 0) perror = j;
                return j;
               }
       return j;
   
      }

// Routine to get Args and store in Vector
//    start is to the right of the ( if there is one.
//    gets argument until end of string or ) or ..
// Returns null if there is an error
// Returns a list of argument values followed by last position in String
   private Vector getArgs( String S , int start , int end )
     {
       if( ( start < 0 ) ||  ( start >= S.length() ) || ( start >= end ) )
	 {seterror( S.length() + 2 , "internal errorp" +S);
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
       while( !done )
	 {Result = null;
          j = execExpr( S , i , end );
         if(Debug)
            System.out.println("getArgsA"+Result+","+perror+","+j);
         if( perror >= 0 )
           return null;
         if(Result instanceof SubRange)
           Args.addAll( ((SubRange)Result).Vect());
         else
           Args.addElement( Result );
         if( ( j >= S.length() ) || ( j >= end ) )
	   {Args.addElement(new Integer(j));
	    return Args;
	   }
         if( j < 0 )
	   {seterror( S.length() + 3 , "InternalerrorG" );
	    return null;
	   }
         if( S.charAt( j ) == ')' )
	   done = true;
         else if( S.charAt(j) == ']')
           done = true;
         else if( (S.charAt( j ) != ',') )
	   {seterror( j , ER_IllegalCharacter+"B");
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
//start is the start of the term
// executes one term to a + or - or )]:, or end of String
    private int execOneTerm( String S , int start , int end)//go til a + or -
      {int i,
	   j;
       Object R1;
	     
       i = skipspaces( S , 1 , start );
       if( Debug )
          System.out.println("in Ex1Trm,st=" + start + "PP1");

       i = execOneFactor( S , i , end );
       if(Debug)System.out.println("Ex1trmA"+perror+","+Result+","+i);
       i = skipspaces( S , 1 , i );
 
          if( perror >= 0 )
	    {return perror;
            }
       R1 = Result;
       boolean done = (i < 0) ||  (i >= S.length()) ||  (i >= end);
       if( !done )
	 if( "+-)&<>,=:]".indexOf( S.charAt( i ) ) >= 0 )
           done = true;
         else if("*/".indexOf(S.charAt(i))<0) done = true;
          if(Debug)System.out.println("Ex1trmB"+perror+","+i);

       while( !done )
         { 
           j = execOneFactor( S , i + 1 , end );
           j = skipspaces( S , 1 , j );
           if(Debug)System.out.println("Ex1trmC"+perror);

	   if( perror >= 0 )
	     {return perror;
             }
     
	   operateArith( R1 , Result , S.charAt( i ) );
           if( perror >= 0 ) { perror = i; return perror;}
	   i = j;
	   done = (i  < 0) ||  (i >= S.length()) ||  (i > end);
           if( !done )
	     if( "+-)&<>=,:]".indexOf( S.charAt( i ) ) >= 0 )
               done = true;
             else if("*/".indexOf(S.charAt(i))<0) done = true;


	   R1 = Result;
         }
       return i;

      }
public void operateCompare( Object R1,Object R2, char c)
  { Result = null;
    if( (R1==null) ||(R2==null))
      {seterror( 1000, ER_ImproperArgument);
       return;
       }
    if(Debug)System.out.println("in op comp args="+R1+","+R2+","+c);
    if(R1 instanceof Boolean)
      if( !(R2 instanceof Boolean))
        if( R2 instanceof Integer)
          {if( ((Integer)R2).intValue()==0)
            R2=new Boolean(false);
          else 
            R2=new Boolean(true);
           }
        else if( (R2 instanceof String) && (c=='&'))
           {Result = R1.toString() + (String)R2;
            return;
            }
        else
           {seterror(1000, ER_ImproperDataType);
            return;
            }
   if(R2 instanceof Boolean)
      if( !(R1 instanceof Boolean))
        if( R1 instanceof Integer)
           {if( ((Integer)R1).intValue()==0)
            R1=new Boolean(false);
           else 
            R1=new Boolean(true);
            }
         else if( (R1 instanceof String)&&(c=='&'))
           {Result = (String)R1+ R2.toString();
            return;
            }
         else
           {seterror(1000, ER_ImproperDataType);
            return;
            }
   if(R1 instanceof Boolean)
     { if( R1.equals(R2))
         { if("<>".indexOf(c)>=0) 
              Result = new Boolean(false);
	 //else if( (byte)c-2  ==(byte)'=')
           else if( (byte)c == (byte)'='+5)
              Result = new Boolean(false);
           else  
            Result = new Boolean(true);
           return;
         }
       else if( ((Boolean)R1).booleanValue())// false < true
         {if(">".indexOf(c)>=0) Result= new Boolean(true);
          else if("<=".indexOf(c)>=0) Result = new Boolean(false);
          else if((byte)c-5 ==(byte)'<' ) Result = new Boolean(false);
          //else if((byte)c-2 ==(byte)'<' ) Result = new Boolean(false);
        
          else Result = new Boolean(true);
          return;
         }
       else
        if((c=='<')||((byte)c-5 == (byte)'<'))Result= new Boolean(true);
        else if( (c=='>')||((byte)c-5 == (byte)'>'))Result= new Boolean(false);
        else if( (byte)c == '=' +5) Result = new Boolean(true);
        else if( c=='=')Result = new Boolean(false);
        return;
      }       
    if(R1 instanceof DataSet)
       if( !(R2 instanceof DataSet))
         {seterror(1000,ER_ImproperDataType);
          return;
          }
    if(R2 instanceof DataSet)
       if( !(R1 instanceof DataSet))
         {seterror(1000,ER_ImproperDataType);
          return;
          }
    if( R1 instanceof DataSet)
      { if( (c=='=') && (R1.equals(R2)))
          Result= new Boolean(true);
        //else if( ((byte)c-2 == (byte)'='))
         else if( ((byte)c-5 == (byte)'='))
          if(R1.equals(R2)) Result= new Boolean(false);
          else Result= new Boolean(true);
        else
         {seterror(1000, execOneLine.ER_NoSuchOperator);
          return;
         }
        return;
       }
    if(R1 instanceof String)
       if( (R2 instanceof Vector)&&(c=='&')) R2= execOneLine.Vect_to_String((Vector)R2);
       else if(!(R2 instanceof String)) R2 = R2.toString();

    if(R2 instanceof String)
       if( (R1 instanceof Vector)&&(c=='&')) R1= execOneLine.Vect_to_String((Vector)R1);
       else if(!(R1 instanceof String)) R1 = R1.toString();


   if( (R2 instanceof String)  &&(R1 instanceof String))
     {String S1 = R1.toString();
        String S2 = R2.toString();
        int i,x;
        x=2;
        for( i = 0 ; (x>=2)&&(i < java.lang.Math.min( S1.length() , S2.length())) ; i++)
           if( S1.charAt(i) < S2.charAt(i))
             x=1;
           else  if( S1.charAt(i) > S2.charAt(i))
            x=-1;

        if(x>=2)
          if( S1.length() < S2.length())
             x=1;
          else if(S1.length()>S2.length())
             x=-1;
          else x=0;
          
      if( (c=='=') ||((byte)c-5 ==(byte)'<') || ((byte)c-5 ==(byte)'>'))
        if( x==0){ Result = new Boolean( true);return;}

     if( (byte)c-5==(byte)'=') 
       if(x ==0) Result = new Boolean(false);
       else Result = new Boolean(true);
    else if((c=='<') ||((byte)c-5==(byte)'<'))
       if( x<=0) Result = new Boolean(false);
       else Result = new Boolean(true);
    else if( c== '=')
       if( x ==0) Result= new Boolean(true);
       else Result= new Boolean(false);
    else 
       if( x>=0) Result = new Boolean(false);
       else Result = new Boolean(true);
   


      
       return;

     }
   if( !(R1 instanceof Number) ||!(R2 instanceof Number))
     {seterror( 1000, ER_NoSuchOperator);
      return;
     }

   operateArith(R2,R1,'-');
   if(perror >=0) return;
    int x=0;
    if( ((Number)Result).floatValue()>0) x=1;
    else if(((Number)Result).floatValue() <0) x=-1;
     else x=0;
    if(Debug)System.out.println("in opcomp Res and x="+Result+","+x+","+c);
    if( (c=='=') ||((byte)c-5 ==(byte)'<') || ((byte)c-5 ==(byte)'>'))
        if( x==0){ Result = new Boolean( true);return;}

    if( (byte)c-5==(byte)'=') 
       if(x ==0) Result = new Boolean(false);
       else Result = new Boolean(true);
    else if((c=='<') ||(byte)c-5==(byte)'<')
       if( x<=0) Result = new Boolean(false);
       else Result = new Boolean(true);
    else if( c== '=')
      if( x ==0) Result= new Boolean(true);
      else Result= new Boolean(false);
    else 
       if( x >= 0) Result = new Boolean(false);
       else Result = new Boolean(true);
           return;




   } 
private void operateLogic(Object R1 , Object R2 , char c )
  {Result = null;
   if( R1 instanceof Integer)
     if( ((Integer)R1).intValue()==0)
        R1= new Boolean(false);
     else R1=new Boolean(false);

   if( R2 instanceof Integer)
     if( ((Integer)R2).intValue()==0)
        R2= new Boolean(false);
     else R2=new Boolean(false);

    if( !(R1 instanceof Boolean)  || !(R2 instanceof Boolean))
      {seterror(1000, ER_ImproperDataType);
       return;
       }
    if(c=='#')
        Result =new Boolean( ((Boolean)R1).booleanValue() && ((Boolean)R2).booleanValue());
     else
         Result =new Boolean( ((Boolean)R1).booleanValue() || ((Boolean)R2).booleanValue());



  }
/** 
 *Can be used by other parsers
 *@param   R1, R2  the two objects to be operated on
 *@param   c       +,-,*, or /
 *NOTE: The data types will converted if possible and the appropriate add, 
 *      subtract,... will be used

 * Use getErrorCharPos to determine if an error occurred<br>
 *     @see #getErrorCharPos()
 */
    public void operateArith( Object R1 , Object R2 , char c )
      {
	if( Debug )
          System.out.println("in Op ARith o=" + c);
        if("#|".indexOf(c)>=0)
          {operateLogic(R1,R2,c);
           return;
          }
        if( c==':')
	   {if(R1 instanceof Integer)
               if(R2 instanceof Integer)
                 {Result= new SubRange(((Integer)R1).intValue(), ((Integer)R2).intValue());
                  return;
                 }
            seterror(1000, ER_ImproperDataType+" "+c);
            Result = null;
            return;
           }

        if( (R1 instanceof Boolean) ||(R2 instanceof Boolean))
         if( (c!='&') || !(R1 instanceof String || R2 instanceof String))
          { seterror( 1000, ER_ImproperDataType);
            return ;
          }
         else 
           {if( R1 instanceof String) Result = (String)R1 +R2.toString();
            else Result = R1.toString()+(String)R2;
           }
            
        if( (R1 instanceof DataSet) &&(c!='&') )
          {operateArithDS( R1 , R2 , c );
	   return;
	  }
	if( (R2 instanceof DataSet) &&(c!='&') )
          {operateArithDS( R1 , R2 , c );
	   return;
	  }
        if( (R1 instanceof Vector) || (R2 instanceof Vector))
         if( (c!='&') || !(R1 instanceof String || R2 instanceof String))
          {operateVector(R1,R2,c);
           return;
          }
         else 
          { if( R1 instanceof String) Result = (String)R1 +execOneLine.Vect_to_String((Vector)R2);
            else Result = execOneLine.Vect_to_String((Vector)R1)+(String)R2;
          }
	if( "+-/*<>^".indexOf( c ) >= 0 )
          { 
            if( R1 instanceof String )
              {
	       Integer II;
               try
	         {II = new Integer( (String)R1 );
	           R1 = II;
	          }
               catch ( NumberFormatException s )
	         {try
	            {R1 = new Float( (String)R1 );
	            }
	          catch( NumberFormatException t )
	            { seterror( 1000 , ER_NumberFormatError );
	              return;
		    }
		 }
             }
           if( R2 instanceof String )
	     {
	      Integer II;
              try
		{II = new Integer( (String)R2 );
		 R2 = II;
	        }
              catch ( NumberFormatException s )
	        {try
		   {R2 = new Float( (String)R2 );
		   }
		 catch( NumberFormatException t )
		   { seterror( 1000 , ER_NumberFormatError );
		     return;
		   }
	         }
	    }
           if( R1 instanceof Integer )
	     if( R2 instanceof Float )
	        R1 = new Float( ( ( Integer )R1 ).floatValue() );
	      
		
           if( R2 instanceof Integer )
	     if( R1 instanceof Float )
	       R2 = new Float( ( ( Integer ) R2 ).floatValue() );

           if( c == '+' )
             {if( R1 instanceof Integer )
                Result = new Integer(((Integer)R1).intValue() + ((Integer)R2).intValue());
	      else 
                Result = new Float(((Float)R1).floatValue() + ((Float)R2).floatValue());
	      }
	    else if (c == '-' )
              {if(R1 instanceof Integer)
                 Result = new Integer(((Integer)R1).intValue()- ( (Integer)R2).intValue());
	      else 
                 Result = new Float(((Float)R1).floatValue() - ((Float)R2).floatValue());
	      }
	    else if( c == '/' )
              {if(R1 instanceof Integer)
                 Result = new Integer(((Integer)R1).intValue() / ((Integer)R2).intValue());
	       else 
                 Result = new Float(((Float)R1).floatValue() / ((Float)R2).floatValue());
	      }
	    else if( c == '*' )
              {if(R1 instanceof Integer)
                 Result = new Integer(((Integer)R1).intValue() * ((Integer)R2).intValue());
	       else 
                 Result = new Float(((Float)R1).floatValue() * ((Float)R2).floatValue());
	      }
	    else if ( c == '^' )
		{ double  x, y; x=0; y=0;
		if( R1 instanceof Integer)  x = ((Integer)R1).doubleValue();
                else if( R1 instanceof Float) x= ((Float)R1).doubleValue();

                if( R2 instanceof Integer)  y = ((Integer)R2).doubleValue();
                else if( R2 instanceof Float) y= ((Float)R2).doubleValue();
                                  
                  Result = new Float(java.lang.Math.pow( x, y ));
	      }
            else
	      {seterror( 1000 , ER_IllegalCharacter+"c" );
	       Result = null;
	       return; 
	      }
	    if( Debug )
              System.out.println("ops&Result=" + R1 + "," + c + "," + R2 + "=" + Result);

             }
	   else if( c == '&')
	     {if( !(R1 instanceof String) )
		R1 = R1.toString().trim();
	      if( !(R2 instanceof String) )
		R2 = R2.toString().trim();
	      Result = (String)R1 + (String)R2;
	      if( Debug )
                System.out.println("Arith op & Res=" + Result + ";" + R1 + ";" + R2);
	     }
	     

      }

  private void operateVector( Object R1, Object R2, char c)
   {int i;
     Vector Res = new Vector();
    Object r2i;
    if( "+-*/".indexOf(c)>=0)
     {if( R1 instanceof Vector)
        {if(R2 instanceof Vector)
           if(((Vector)R2).size() != ((Vector)R1).size())
              {seterror(1000, "Incompatible arrays");
               Result= null;
               return;}
         
          for(i=0;i<((Vector)R1).size(); i++)
            {
             if(R2 instanceof Vector) r2i= ((Vector)R2).elementAt(i);
             else r2i=R2;
             operateArith(((Vector)R1).elementAt(i),r2i,c);
             Res.addElement(Result);
            } 
          Result=Res;
        }//if R1 instanceof Vector
       else
         {for( i=0;i<((Vector)R2).size();i++)
             { operateArith(R1,((Vector)R2).elementAt(i),c);
               Res.addElement(Result);
             }
           Result =Res;
         }
      }//if c is +-*/
    else if( (!(R2 instanceof Vector))||(!( R1 instanceof Vector)))
       {seterror(1000,"improper Arguments");
        Result = null;
        return;
       }
    else
      {Result = R1;
       ((Vector)Result).addAll((Vector)R2); // must redo for Java 1.9

      }
      
   }//operateVector
   // Does the function operations
    private void operateArithDS( Object R1 , Object R2 , char c )
      {String Arg;
      if( c == '+' )
        Arg = "Add";
      else if( c == '-' )
        Arg = "Sub";
      else if( c == '*' )
        Arg = "Mult";
      else if( c == '/' )
        Arg = "Div";
      else
       { seterror( 1000 , "internalerror88" );
         return;       
       }
      Vector Args = new Vector();
   
      if( ( R1 == null ) ||  ( R2 == null ) )
	{seterror( 1000 , ER_ImproperArgument + "null" );
	}
      if( ( R1 instanceof String ) ||  ( R2 instanceof String ) )
	{seterror( 1000 , ER_ImproperArgument );
	return;
	}
      if( R1 instanceof Integer )
	R1 = new Float( ( ( Integer )R1 ).floatValue() );
      if(R2 instanceof Integer)
	R2 = new Float( ( ( Integer )R2 ).floatValue() );
      DataSet DS;
      Object Arg2;
      if( R1 instanceof DataSet )
	{DS = (DataSet)R1;
	 Arg2 = R2; 
	}
      else
        {if( c == '-' )
           {operateArithDS( R2 , (Object)(new Float( -1 )) , '*' );
	    R2 = Result;
	    if(R2 == null)
	      {seterror( 1000 , ER_ImproperArgument );
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
private Operator getSHOp( Vector Args, String Command)
  { int i = SH.getOperatorPosition( Command );
    if( i < 0)
      return null;
    int j = i;
    boolean done = false;
    boolean found = false;
    while( (!done) && (!found ) )
       {int n = SH.getNumParameters( i );
        
        if( n == Args.size())
         {found = true;
           for( j = 0; (j < n) && found; j++)
              {Object P = SH.getOperatorParameter( i , j );
               if( P == null)
                  {}
               else if( (Args.elementAt( j ) instanceof String)
                        &&(P instanceof SpecialString))
                 { }
               else if( (Args.elementAt(j) instanceof  Integer)
                    &&(P instanceof  Float))
                 {}
               else if ( Args.elementAt(j).getClass().equals( P.getClass()))
                  { }
               else found = false; 
              }
          }
        if( found )
          return SH.getOperator( i ) ;
        i++;
        String C1= SH.getOperatorCommand( i );
        if( C1 == null)
           done = true;
        else if( ! C1.equals( Command ) )
           done = true;
        }
    return null;
   }
/**
 * Find and Executes an  operation from lists of operations 
 * @param   Args     The vector of argument values
 * @param   Command  The command to be executed
 *@return
 *    The value in the variable Result<BR>
 *    An error if the operation is not defined or does not work
 */
    public void DoOperation( Vector Args, String Command )
      { if(Debug)
          System.out.println("Start DoOperation comm =" + Command);
        //Operator op = GenericOperatorList.getOperator( Command );
        if( Args == null )
            return;
        
        Operator op = getSHOp( Args, Command );
        
        if( op == null )
          { if(Debug)
              System.out.print("A");
            op =(Operator)MacroInfo.get(Command);
            if( !checkArgs( Args, op, 0 ))
               op = null;
   
            if(op==null)
              if( Args.size() > 0)
              {DoDataSetOperation( Args, Command  );
               return;
               }
           seterror (1000 , execOneLine.ER_NoSuchOperator);
           return;
          }
        int i;
        if(Debug)
              System.out.print("B");
        
      
        SetOpParameters( op , Args , 0);
        if( op instanceof IObservable)
           ((IObservable)op).addIObserver( this );
        if( op instanceof Customizer)
           ((Customizer)op).addPropertyChangeListener( this );
        Result = op.getResult();
        op.setDefaultParameters();
       if( op instanceof IObservable)
           ((IObservable)op).deleteIObserver( this );
        if( op instanceof Customizer)
           ((Customizer)op).removePropertyChangeListener( this );

        if( Result instanceof ErrorString )
          {seterror (1000 , ((ErrorString)Result).toString() );
	    if(Debug)
               System.out.println("ErrorX Ocurred in get Result" + Result);
	       Result = null;
	  }


      }
   
    private boolean checkArgs( Vector Args , Operator op , int start)
     { int k ;
       boolean fit =true;
       Object Arg2;
       if( op == null)
	   {//System.out.println("Check no op");
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
       for( k =0 ; (k < op.getNum_parameters()) && fit ; k++ )          
         { 
           Arg2 = Args.elementAt( k +start );
           if( Debug)
	       {System.out.print("Check"+Arg2.getClass());
	        if( op.getParameter(k) != null)
                 
		   System.out.println( op.getParameter(k).getValue());
               }
            
           if( op.getParameter(k).getValue() == null)
              {if(Debug) System.out.print("H");                         
               }
                
	    else if( ( Arg2 instanceof String ) && 
                          ( op.getParameter(k).getValue() instanceof 
                              SpecialString ) )
	      {if(Debug)System.out.print("D");
	      }
                 
            else if( Arg2.getClass().equals( op.getParameter( k ).getValue().getClass() ))
	      {
		 if( Debug ) System.out.print("E"+ Arg2.getClass());
              }
            else if( (Arg2 instanceof  Integer)
                    &&(op.getParameter(k).getValue() instanceof  Float))
                 {}           
	    else 
               fit = false;
	    if(Debug)System.out.println("F");                    
					
         }//For k
        return fit;
     }
  private void SetOpParameters ( Operator op , Vector Args , int start )
     {int k;
      for( k = 0 ; k < op.getNum_parameters() ; k++ )
        {if( (Args.elementAt( k + start ) instanceof String)  &&  
             (op.getParameter( k ).getValue( ) instanceof SpecialString) )
	   { /*try{ Object V=null;
               if( op.getParameter(k).getValue() instanceof hasCloneMethod)
                  { V=((hasCloneMethod)( op.getParameter(k).getValue())).clone();
                   }
               else
                 {Class C = op.getParameter( k ).getValue().getClass();
                  Class ArgsC[] = new Class[1];
                  ArgsC[0] = Class.forName("java.lang.String");
                  java.lang.reflect.Constructor Cons = C.getConstructor( ArgsC);
                  Object Argvs[] = new Object[1];
                  Argvs[0] = Args.elementAt( k + start ) ; 
                  V =  Cons.newInstance(Argvs);          
                  }
              SpecialString X =(SpecialString)V;
             X.setString((String)(Args.elementAt(k+start)));
             op.getParameter( k ).setValue( X );
	       */
	      if( op.getParameter(k).getValue() != null)
	        { SpecialString X=(SpecialString)(op.getParameter(k).getValue());
		
		    X.setString((String)(Args.elementAt(k+start)));
		}
            /* }
           catch( Exception s )
             {System.out.println("Internal ERROR XXXX"+ s+","+s.getMessage()+","+
                    op.getClass()+", arg="+k);
             }*/
           }
          else if( op.getParameter(k).getValue() instanceof Float)
            {Float F = new Float(((Number)(Args.elementAt(k + start))).floatValue());
             op.getParameter(k).setValue( F);
             }
          else
            op.getParameter( k ).setValue( Args.elementAt( k + start ) );
        }
     }
    private void SetOpParameters1 ( Operator op , Vector Args , int start )
     {int k;
      for( k = 0 ; k < op.getNum_parameters() ; k++ )
        {if( (Args.elementAt( k + start ) instanceof String)  &&  
             (op.getParameter( k ).getValue( ) instanceof AttributeNameString) )
            op.getParameter( k ).setValue( new AttributeNameString( (String)( Args.elementAt(k + start) ) ));
          else
            op.getParameter( k ).setValue( Args.elementAt( k + start ) );
        }
     }
    private void DoDataSetOperation( Vector Args , String Command )
      {
       int i,k;
       Operator op;
       DataSet DS;
       Object Arg2;
       boolean fit;
       if( !( Args.elementAt(0)instanceof DataSet ) )
	 {seterror( 1000 , ER_NoSuchOperator  );
	  return;
	 }
       DS = (DataSet)Args.elementAt(0);
       if(Debug)
	   System.out.println("Command="+Command+":"+Args.size());
       for( i = 0 ; i < DS.getNum_operators() ; i++ )
	 {op = DS.getOperator( i );
          if( Debug )
            System.out.print("OPList," + op.getCommand() + "," + op.getClass().toString() + "," + 
			   op.getNum_parameters() + ",");
	
	   //fit = true;       //.getClass().to.String()
           fit = false;
           if( (op.getCommand().equals(Command) ) && 
                        (op.getNum_parameters() == Args.size()-1) )
                 fit = checkArgs( Args , op , 1 );
               
		  if(Debug)System.out.print("F");                    
					
                
	       if(Debug)System.out.print("GG");
	     if(fit)
	       {/*for( k = 0 ; k < op.getNum_parameters() ; k++ )
                  {if( Args.get( k + 1 ) instanceof String )
                     op.getParameter( k ).setValue( new AttributeNameString( (String)( Args.get(k + 1) ) ));
                   else
                     op.getParameter( k ).setValue( Args.get( k + 1 ) );
                  }
                 */
                SetOpParameters( op , Args , 1);
		Result = op.getResult();
		op.setDefaultParameters();   
                if( Result instanceof ErrorString )
		  {seterror (1000 , ((ErrorString)Result).toString() );
		   if(Debug)
                      System.out.println("Error Ocurred in get Result" + Result);
		   Result = null;
		   }
		return;
                   
		}         
      
       }//For i = 0

	
        seterror( 1000 , ER_NoSuchOperator );
    
      }

//Command is the operation(DS or Attrib or...)
//start is the position of the (
    private  int execOperation( String Command , String S ,  int start , int end )
      {if( ( start < 0 ) ||  ( start >= S.length() ) || ( start >= end ) )
	 {seterror( S.length() + 2 , "internal errorq" );
	 return start;
	 }
       if( S.charAt( start ) != '(' )
         {seterror( S.length() + 2 , "internal errorr" );
	  return start;
         }
       if( Debug)
         System.out.println( "In execOperation comm ="+Command);
       Vector Args = new Vector();
       int i , j;
       boolean done;
       i = start;
       done = false;
       j = skipspaces(S, 1, i + 1 );
       if( j < end )
         if( (S.charAt(j) == ')'))
            {done = true;
              i = skipspaces( S , 1 , j);
             }
       while( !done )
         {j = execute( S , i + 1 , end );
         if( perror >= 0 )
           return perror;
         Args.addElement( Result );
         if( ( j >= S.length() ) || ( j >= end ) )
	   {seterror( j , ER_MisMatchParens );
	    return j;
	   }
         if( j < 0 )
	   {seterror( S.length() + 3 , "InternalerrorG" );
	    return S.length() + 3;
	   }
         if( S.charAt( j ) == ')' )
	   done = true;
         else if( S.charAt( j ) != ',' )
	   {seterror( j , ER_IllegalCharacter+"D" );
	    return j;
	   }
         i = skipspaces(S , 1,j);
         }//while not done

       j = skipspaces( S , 1 , i + 1 );
      // if( !( Args.get( 0 )instanceof DataSet ) )
       //  {seterror(  i , ER_NotImplementedYet );
	//  return i;
       //  }
        
        if( Debug )
          System.out.println("Got to Here after arg");
    
        int i1,k1,i2,k2;
      
        if(Debug)
          { System.out.print("Args = " + Args.size());
            for(i2 = 0 ; i2 < Args.size() ; i2++)
            System.out.print(Args.elementAt(i2) + "," + Args.elementAt(i2).getClass() + ",");
          System.out.println("");
          }
	
        if( Command != null )
          {if( Debug )
            System.out.println("Command=" + Command);
          DoOperation( Args , Command );
         if( perror >= 0 ) perror = start;
         }
       else
         {perror = j;
          serror = ER_FunctionUndefined;
	  return j;
         }

       return skipspaces( S , 1 , j );
      }
   
//**************************SECTION:UTILITIES EXEC***************
     private boolean isDataSetOP( String C )
       {return false;
       }
  private Object getValArray(String S1)
   { String S = S1;//fixx(S1);
     Object O;
   
     if( perror >=0 ) return null;
      int k= S.indexOf('[');
     Vector V = getArgs(S, k+1, S.length());
      
     if(V == null)return null;
     //System.out.print("C");
     int n = ((Integer)V.lastElement()).intValue();
     
    
       //System.out.print("D"+","+n+","+k);
     if(k<0)
        {seterror(1000, "Internal Error getValArry");
           return null; 
         }
      Vector d1 = (Vector)ArrayInfo.get(S.substring(0,k).toUpperCase());
       //System.out.print("E");
      if( d1== null)
       {seterror(1000,ER_NoSuchVariable);
           return null; 
       }
       //System.out.print("F");
      Object d=d1;
      for(int i=0; i<V.size()-1; i++)
        {O = V.elementAt(i);
          //System.out.print("G"+O);
         if(O == null)
           {seterror(1000, ER_MissingArgument +" at index="+i);
           return null; 
           }
         if( !(O instanceof Integer))
          {seterror(1000, ER_IMPROPER_DATA_TYPE +"A at index="+i);
           return null; 
          }
         if(!(d instanceof Vector))
           {seterror(1000, ER_IMPROPER_DATA_TYPE+"B" );
           return null; 
            }   
         int indx = ((Integer)O).intValue();
         if(((Vector)d).size()<=indx)
           {seterror(1000, ER_ArrayIndex_Out_Of_bounds+" "+indx);
            return null;
            }
         d =((Vector)d).elementAt(indx);
        } 
      //System.out.println("getvalArray res="+d);
      return d;
   }
  /** 
   *  Returns the value of the variable S
   *@param  S   A string used to refer to a variable 
   *@return  The value of this string or
   *         an error message if the variable is not found
   *  @see #getErrorCharPos()
   */
     public Object getVal( String S )
       {int i;
       
        if(S.indexOf('[')>=0)
            return getValArray(S);
        if( ArrayInfo.containsKey(S.toUpperCase()))
            return ArrayInfo.get(S.toUpperCase());
        if( BoolInfo.containsKey(S.toUpperCase()))
           return BoolInfo.get(S.toUpperCase());
           
        i = findd( S.toUpperCase() , Ivalnames );
        if( isInList( i , Ivalnames ) )
	  {
           return Ivals[ i ];
          }
        i = findd( S.toUpperCase() , Fvalnames );
        if( isInList( i , Fvalnames ) )
	  { return Fvals[ i ];
          }
        i = findd( S.toUpperCase() , Svalnames );
        if( isInList( i , Svalnames ) )
	  {return Svals[ i ];
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
         for( i=0;vals.hasMoreElements();i++)
          {
           X= vals.nextElement();
         
            if(Debug) System.out.println("val="+X);
            }
         
         if(Debug)System.out.println("Loc DS Keys");
         for( i=0;keyss.hasMoreElements();i++)
          { X=keyss.nextElement();
            if(Debug) System.out.println("val="+X);}
        
        vals=ds.elements();
           keyss=ds.keys();
          
         if(Debug)System.out.println("Glob DS Vals,Searchname"+S+","+ds.size());
         for( i=0;vals.hasMoreElements();i++)
          { X= vals.nextElement();
            if(Debug) System.out.println("val="+X);}
        
         if(Debug)System.out.println("Glob DS Keys");
         for( i=0;keyss.hasMoreElements();i++)
          {X = keyss.nextElement();
           if(Debug) System.out.println("val="+X);}

        

         seterror( 1000 , ER_NoSuchVariable );
        return null;
     
      }
//Finds matching quote and returns unquoted string
//  Does not test for backslash stuff
     private String getString( String S ,  int start )
       {if( ( start < 0 ) ||  ( start + 1 >= S.length() ) )
          {seterror( S.length() + 2 , ER_IllegalCharacter+"E" );
           return null;
          }
        if( S.charAt( start ) != '\"' )
          {seterror( S.length() + 2 ,  ER_IllegalCharacter+"F" );
           return null;
          }
       int i;
       for( i = start + 1 ; i < S.length() ; i++ )
	 {if( Debug )
            System.out.print("in getstring,i,c=" + i + "," + S.charAt(i));
          if( S.charAt( i ) == '\"' )
            {return S.substring( start + 1 , i );
            }
         }
       seterror( start , ER_MisMatchQuote );
       return null;
      }
 //Eliminates spaces in the Title of ds1
    private DataSet  eliminateSpaces( DataSet ds1 )
      {int j;
      /* DataSet ds;

       ds = ds1;
       ds.getTitle().trim();
       for( j = ds.getTitle().indexOf(' ') ; ( j >= 0 ) && ( j < ds.getTitle().length() ) ; 
                          j = ds.getTitle().indexOf(' '))
	 {ds.setTitle( ds.getTitle().substring( 0 , j ) + ds.getTitle().substring( j + 1 ) );
	 }
	*/
       return ds1;
      }

 //eliminates trailing non characters
    private String Trimm( String S )
      {
       String res;
       if( S == null )
         return S;
       int i;
       res = S;
       i = S.length()-1;
       if( i < 0 )
         return S;
       if( S.charAt( i ) < ' ' ) 
         return Trimm( S.substring( 0 , i ) );
       else 
         return S;
      }

    /**
     *Initialized the variables and workspace for a new run
     */
      public  void initt()
        { 
         BoolInfo = new Hashtable();
        BoolInfo.put("FALSE",new Boolean(false));
         BoolInfo.put("TRUE", new Boolean(true));
         Ivals=null;
         Ivalnames=null;
         
         
         Fvals = null;  
         Fvalnames = null;
         Svals=null; 
         Svalnames=null;
         lds.clear();            // May need an Empty data set somewhere but where?
         //ds = new HashMap()'
         perror = -1;
         serror = "";
         lerror = -1;
         Result=null;
         for(int i=0; i<Params.size();i++)
           { ds.remove(Params.elementAt(i));
            }
         Params= new Vector();
        
        //ds= new Hashtable();
        ArrayInfo=new Hashtable();
      }


     private  void seterror( int charnum, String errorMessage)
       {
        //if(charnum < 0)return ;      
        perror = charnum;
        serror = errorMessage;
    
      }




    //Finds the first of occurrence of one letter in SearchChars in the String S
    //  starting at strart in direction dir(right=1,left=1)

    //  The search will not search items between two parentheses if the search started
    //  outside the parenthsesis.

     private int findfirst(   String   S,
                              int      dir,
                              int      start,
                              String   SearchChars,
                             String    BraceChars)

      {int    i,
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
       if( S.length() <= 0 )
         return -1;
       if( start >= S.length() )
         return S.length();
       if( dir == 0 )
         return -1;
       if( dir > 0 )
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
       else 
         {done = false;
          c   = S.charAt( i );
         }
   
       while( !done )
	   {if(Debug)System.out.print(c);
          if( BraceChars != null )
            j = BraceChars.indexOf( S.charAt(i) );
          else 
            j = -1;
          if(j < 0)
            {}
          else if( j == 2 * (int)(j / 2) )
            brclev++;
          else 
            brclev--;

          if(brclev>=0) i += dir; //** test this out.  if brclev<0 like end of line

          c = 0;
          if( i  >= S.length() )
            done = true; 
          else if( i < start )
            done = true; 
          else if( brclev < 0 )
            done = true;
          else 
            { 
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
  * @param  S    String
  * @param  dir   direction. positive skips forward<P>
  * @param  start The starting index where the skipping starts
  *
  * @return  returns the position in the string where first nonspace occurs or
  *         the end of the string ( dir > 0 ) or -1( dir < 0 )              

  */
     public int skipspaces(String   S, 
                      int       dir, 
                      int      start)

       { 
        int    i,
               j; 
        char    c;

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


    // Used to replace sss[i] by sss0 or sss1 etc. whatever i contains.
    //  NOTE: Just returns the 0,1,2 in string form
    private String brackSub( String S , int start , int end )
    {  Object R1, R2;
       R1 = Result;
       //System.out.print("in brackSub"+start); 
       if((start < 0) || (start >= S.length())) 
	  {seterror( start , "Internal Error brackSub");
	   Result = R1;
           return "";
	  }
       //System.out.print("A");
        if(S.charAt(start)!='[')
	  {seterror( start , "Internal Error brackSub");
	  Result = R1;
           return "";
	  }
        //System.out.print("B");
        int j=execute( S, start + 1, end );
        if(perror >= 0)
          {Result = R1;
           return "";
           }
        j=skipspaces( S , 1 , j);
	//System.out.print("C"+Result+","+j);
        if( (j < 0) || ( j >= S.length()))
           {seterror( start , ER_MissingBracket + j +"E");
	    Result = R1;
            return "";
	   } 
	//System.out.print("D"+perror);
        if(S.charAt(j) != ']' )
          {seterror( start , ER_MissingBracket+j+"F");
	   Result = R1;
           return "";
	   }
	//System.out.print("E");
        R2 = Result;
        Result = R1;
        if(R2 == null )
	    return "";
        return R2.toString();
              
         
         
      }

   private int finddQuote( String S , int start , String SrchChars , String brcpairs )
      {int i, j, j1;
       int brclevel;
       boolean quote;

       if( S == null )
          return -1;
       if( SrchChars == null )  return -1;
       if( ( start < 0 ) || ( start >= S.length() ) )
          return S.length();
       brclevel=0;
       quote=false;          
      
       for ( i = start ; i < S.length() ; i++ )
         { char c = S.charAt( i );
            
            if( c == '\"' )
             {if( ( !quote ) && ( brclevel == 0 ) && (SrchChars.indexOf(c) >= 0 ) )
                 return i;
              quote = !quote;
              if( i >= 1)
                if( S.charAt( i - 1 )  =='\"' )
                   {quote = !quote;}
             }
            else if( quote )
               { }
           
            else if( SrchChars.indexOf( c ) >= 0 )
               {if( brclevel == 0 )
                   return i;
               }
            if( ( !quote ) && ( brcpairs != null ) )
              { j = brcpairs.indexOf( c );
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


    private boolean isInList( int i, String Llist[] )
      {if( i < 0 )
         return false;
       if( Llist == null )
         return false;
       if( i >= Llist.length )
         return false;
       if( Llist[i] == null )
         return false;
       return true;
     
      }
/*    private boolean isInListDS( int i , DataSet Llist[] )
      {if( i < 0 )
         return false;
       if( Llist == null )
          return false;
       if( i >= Llist.length )
         return false;
       if( Llist[i] == null )
          return false;
       return true;
     
      }
     private int finddDS( String SearchName,
		          Hashtable Llist)
      {int i;
      //System.out.println("in finddDS"+SearchName);
       if( Llist == null )
         return -1;
       if(Llist.containsKey(SearchName.toUpperCase().trim() ))
         return
       else return -1;
       return i;
      }
*/
     private int findd(   String SearchName, 
                          Object SearchList[]
                      )
       {
        if( SearchList == null )
          return -1;
        if( SearchList.length <= 0 )
          return -1;
        int i;
        if( Debug )
          System.out.println("Src=" + SearchName + ":");
        for( i = 0 ; (i < SearchList.length) && (SearchList[i] != null) ; i++ )
          {if( Debug )
             System.out.println("findd  i=" + i + "," + SearchList[i] + ":"); 
           if( SearchList[i] == null )
             return i;
           
           else
            {if( ( ((String)SearchList[i]).toUpperCase() ).equals( SearchName.toUpperCase() ) )
               return i;
            }
         }
       if( Debug )
         System.out.print( "not findd" );
       return i;
      }


    public void AssignArray(String vname, Object Result)
     {String S = vname;//fixx(S1);
      Object O;
      if(Debug)
         System.out.println("TOP ASsignArray vname,Result="+vname+","+Result);
      if( perror >=0 ) return ;
      int k= S.indexOf('[');
      Vector V = getArgs(S, k+1, S.length());
      //System.out.print("B");
     if(V == null)
           {return ;
           }   
     if(Debug) System.out.println("Args="+Vect_to_String(V)); 
     int n = ((Integer)V.lastElement()).intValue();  
      
     if(k<0)
        {seterror(1000, "Internal Error getValArry");
           return ; 
        }
      boolean newVar=false;
      Vector d1 = (Vector)ArrayInfo.get(S.substring(0,k).toUpperCase());
      if((V.size()<2)&&(d1!=null)) //ar[]=[1,2,3]
        {if(Result instanceof Vector)
          {d1.removeAllElements();
           d1.addAll((Vector)Result);
           return;
          }
	}
       else if( (V.size()<2)&&(d1==null))
	 { if( Result instanceof Vector)
               ArrayInfo.put(S.substring(0,k).toUpperCase(), Result);
           else
	       seterror(1000,ER_IMPROPER_DATA_TYPE+"C");
	  return;
         }
       
    
      if( d1== null)
       {d1=new Vector();
        newVar=true;
       }
      if(Debug)
         System.out.println("d1="+Vect_to_String(d1));
      Object d=d1;
      for(int i=0; i<V.size()-1; i++)
        {O = V.elementAt(i);
         if(Debug)
           System.out.println("element i="+i+","+O); 
         if(O == null)
           {seterror(1000, ER_MissingArgument +" at index="+i);
           return; 
           }
         if( !(O instanceof Integer))
          {seterror(1000, ER_IMPROPER_DATA_TYPE +"D at index="+i);
           return; 
          }
         if(i<V.size()-2)
         if(!(d instanceof Vector))
           {seterror(1000, ER_IMPROPER_DATA_TYPE+" E" );
           return; 
            }   
         int indx = ((Integer)O).intValue();
         if((  (i<V.size()-2) && ((Vector)d).size()<indx     )  ||
            (  (i==V.size()-1) &&((Vector)d).size()<indx-1    )
            )
           {seterror(1000, ER_ArrayIndex_Out_Of_bounds+" "+indx);
            return ;
            }
        if(i<V.size()-2)
             d=((Vector)d).elementAt( indx);
        else if(((Vector)d).size()>indx)
           {((Vector)d).setElementAt(Result,indx); 
            }
         else ((Vector)d).addElement(Result);
        
        } 
     if(newVar) 
        ArrayInfo.put(vname.substring(0,k).toUpperCase(),d);
     
     }
    public void Assign(String vname,
                       Object Result)
      {int   i,
             j;
       
      boolean found = true ;
      String nam = vname;
      if(vname.indexOf('[')>=0)
            nam = vname.substring(0,vname.indexOf('[')   ).toUpperCase();
      Object X = getVal( nam );
      if( (perror >= 0) && ( serror.equals( ER_NoSuchVariable)))
	 {perror = -1;
	  serror = "";
          found = false;
         }
      if(Debug) System.out.println("in Assign,vname and nam="+vname+" "+nam);
      if(vname.indexOf('[')>=0)
          {if(found && !ArrayInfo.containsKey(nam))
             {seterror(1000,ER_IMPROPER_DATA_TYPE+"F" );          
              return; 
             }
           AssignArray(vname, Result);
           return;
          }
     
       if( Result instanceof Boolean)
         {if(vname.toUpperCase().equals("TRUE") || 
             vname.toUpperCase().equals("FALSE"))
            {seterror(1000, ER_ReservedWord);
             return;
             }
           if(found && !BoolInfo.containsKey(vname))
             {seterror(1000,ER_IMPROPER_DATA_TYPE+"G" );
              return; 
             }
           BoolInfo.put(vname.toUpperCase(),Result);         
            
          }
       else if( Result instanceof Vector)
          {if(found && !ArrayInfo.containsKey(vname))
             {seterror(1000,ER_IMPROPER_DATA_TYPE+"H" );
              return; 
             }
            ArrayInfo.put(vname.toUpperCase(),Result);
          }
       else if( Result instanceof Integer )  //what about array of integers??
         {i = findd( vname , Ivalnames );
          if( Ivalnames == null )
	    {if( found ) 
		{seterror (1000,ER_ImproperDataType);
		return; 
	       }
             Ivalnames = new String[ 10 ]; 
	       Ivals = new Integer[ 10 ];
             i = 0;
             Ivalnames[ 0 ] = null;
            }
          if( i >= Ivalnames.length )
            {String IName[];
             if( found ) 
		{seterror (1000,ER_ImproperDataType);
		return; 
	       }
             IName = new String[ Ivalnames.length + 10 ];
             Integer Ival[];
             Ival = new Integer[ Ivalnames.length + 10 ];
             for( j = 0 ; j < Ivalnames.length ; j++ )
	       {IName[ j ] = Ivalnames[ j ]; 
                Ival[ j ] = Ivals[ j ];
               }
              Ivalnames = IName; 
              Ivals = Ival;
              Ivalnames[ i ] = null;
               }
             if(Ivalnames[ i ] == null)
               {if( found ) 
		{seterror (1000,ER_ImproperDataType);
		return; 
	        }
                Ivalnames[ i ] = vname.toUpperCase();		 
                Ivals[ i ] = (Integer)Result;
                if( i + 1 < Ivalnames.length )
                  Ivalnames[ i + 1 ] = null;
               }
           else 
             Ivals[ i ] = (Integer)Result;
 
         }
       else if( Result instanceof Float )
         {i = findd( vname , Fvalnames );
          if( Fvalnames == null )
            {if( found ) 
		{seterror (1000,ER_ImproperDataType);
		return; 
	       }
             Fvalnames = new String[ 10 ]; 
             Fvals = new Float[ 10 ];
             i = 0;
             Fvalnames[ 0 ] = null;
            }
          if( i >= Fvalnames.length )
            {String IName[];
             if( found ) 
		{seterror (1000,ER_ImproperDataType);
		return; 
	       } 
             IName = new String[ Fvalnames.length + 10 ];
             Float Fval[];
             Fval = new Float[ Fvalnames.length + 10 ];
             for( j = 0 ; j < Fvalnames.length ; j++ )
	       {IName[ j ] = Fvalnames [ j ]; 
                Fval[ j ] = Fvals[ j ];
               }
             Fvalnames = IName; 
             Fvals = Fval;
             Fvalnames[ i ] = null;
                           
            }
         if( Fvalnames[i] == null )
           {if( found ) 
		{seterror (1000,ER_ImproperDataType);
		return; 
	       }
            Fvalnames[ i ] = vname.toUpperCase();
            Fvals[ i ] = (Float)Result;
            if( i + 1 < Fvalnames.length )
              Fvalnames[ i + 1 ] = null;
           } 
         else 
           Fvals[ i ] = (Float)Result;
 
         }
       else if( Result instanceof String )
         {i = findd( vname , Svalnames );
          if( Svalnames == null )
            {Svalnames = new String[ 10 ];
             Svals = new String[ 10 ]; 
             i = 0;
             Svalnames[ 0 ] = null;
            }
          if( i >= Svalnames.length )
            {
             String IName[]; 
             IName = new String[ Svalnames.length + 10 ];
             String Sval[];
             Sval = new String[ Svalnames.length + 10 ];
             for( j = 0 ; j < Svalnames.length ; j++ )
	       {IName[ j ] = Svalnames[ j ]; 
                Sval[ j ] = Svals[ j ];
               }
             Svalnames = IName; 
             Svals = Sval;
             Svalnames[ i ] = null;
            }
     
          if( Svalnames[ i ] == null )
            {  
               if( found ) 
		{seterror (1000,ER_ImproperDataType);
		return; 
	       }
               Svalnames[ i ] = vname.toUpperCase();
               Svals[ i ] = (String)Result;
               if( i + 1 < Svalnames.length )
                  Svalnames[ i + 1 ] = null;
            }
          else 
            Svals[ i ] = (String)Result;
 
         }
       else if( Result instanceof DataSet )
         { DataSet D;
         
           if(ds.containsKey(vname.toUpperCase().trim()))
            {    D =(DataSet) ds.get(vname.toUpperCase().trim());
		D.copy((DataSet)Result);   //= (DataSet)((DataSet)Result).clone();
                 return;
              }
              
                D =(DataSet) ((DataSet)Result).clone();
               
               lds.put(vname.toUpperCase().trim(),D);
              
                return;
 
            }

      }//end Assign

     private void Delete( String vname, Hashtable DS)
       { if(vname == null) return;
        DS.remove(vname.toUpperCase().trim()); 
       }
    
     public void propertyChange(PropertyChangeEvent evt)
        { 
          PC.firePropertyChange( evt );
         }


     public void update( Object observed_obj , Object reason )
       { if( observed_obj != null) 
          if( observed_obj instanceof DataSet)
            if( reason instanceof String)
              if( reason.equals( IObserver.DESTROY))
                 { 
		   long tag = ((DataSet)observed_obj).getTag();
                   ds.remove( "ISAWDS"+tag);
                   observed_obj = null;
                       
                 } 
         if( reason instanceof DataSet)  //Send Command from a subscript
               OL.notifyIObservers( observed_obj , reason );
       } 
//************************SECTION:EVENTS********************
    /** 
     *@param    obs    The Iobserver who wants to be notified of a new
     *                 data set.
     */
     public void addIObserver( IObserver iobs )
       {OL. addIObserver( iobs );
       }

    /** 
     *@param  iobs   The Iobserver who no longer wants to be notified of a 
     *               new data set
     */
     public void deleteIObserver( IObserver iobs )
       {OL.deleteIObserver( iobs );
       }

    /** 
     *
     */
     public void deleteIObservers()
       {OL.deleteIObservers();
       }

    /** 
     *@param 
     *     listener   The listener who wants to be notified of a non Data Set
     *                "Display" value
     */
     public void addPropertyChangeListener( PropertyChangeListener listener )
       {
       PC.addPropertyChangeListener( listener );
       }

     /** 
     *@param 
     *     listener   The listener who no longer wants to be notified of a 
     *                non Data Set "Display" value
     *                
     */
     public void removePropertyChangeListener( PropertyChangeListener listener )
       {PC.removePropertyChangeListener( listener );
       }

     /**
      * Needed to fill out the Customizer interface
      */
     public void setObject( Object bean)
         {
         }
    /** 
     *@param 
     *     listener      The listener who wants to be notified of a non Data Set
     *                   "Display" value
     * @param    PropertyName   Must be Display
     */
     public void addPropertyChangeListener( String propertyName,
                                            PropertyChangeListener listener)
      {PC.addPropertyChangeListener(   listener );
       }

     /** 
     *@param 
     *     listener   The listener who no longer wants to be notified of a 
     *                non Data Set "Display" value
     *@param  PropertyName   Must be Display
     *                
     */
     public void removePropertyChangeListener( String propertyName,
                                               PropertyChangeListener listener)
       {PC.removePropertyChangeListener( listener );
       }

 private class SubRange
  {int first,last;
   public SubRange( int first, int last)
     {this.first = first;
      this.last = last;
     }
   Vector Vect()
     {int dir=1;
      if(first> last) dir = -1;
      Vector V = new Vector();
      for( int i = first;(i-last)*dir <=0    ; i+=dir)
            V.addElement( new Integer(i));
      return V;
     }   

   }   
}

