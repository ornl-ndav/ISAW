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
 * Revision 1.8  2001/07/03 22:14:58  rmikk
 * Added Code that will eliminate the "UNKOWN" at the
 * top of the JParameterDialog box.
 *
 * Revision 1.7  2001/06/26 14:42:33  rmikk
 * -Changed DataSetListHandler to IDataSetListHandler
 * -Removed references to the session log
 *
 * Revision 1.6  2001/06/07 19:01:43  rmikk
 * Added a Test to catch parameters with null values.
 *
 * Revision 1.5  2001/06/04 20:14:51  rmikk
 * Updated Documentation.
 * Fixed Title = so no space after = needed
 *
 * Revision 1.4  2001/06/04 14:08:54  rmikk
 * Fixed error in SetDefaultParameters to allow space after
 * the = in the "parameter"  Title=
 *
 * Revision 1.3  2001/06/01 21:14:13  rmikk
 * Added Documentation for javadocs etc.
 *

5-20-2001   Ruth Mikkelson  Separated the Processor part from the
                            GUI. See CommandPane.java for previous docs 


  
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
import java.beans.*; 
import java.util.Vector;
//import Command.*;

/**
 * Processes the script or one line of a script.  <BR>
 * It has features to send back results that are displayable and also
 * results that are data sets. 
 */
public class ScriptProcessor  extends GenericOperator 
                          implements  
				      PropertyChangeListener , 
                                      IObservable ,  IObserver,
                                      IDataSetListHandler
{    
  
 //***********************SECTION GUI****************
 
    
    Command.execOneLine ExecLine ; 
    
    private int lerror ,                        //line number of error. 
        perror ;                                //position on line of error
    private String serror ;                     //error message

   
    private IObserverList OL ; 
    private PropertyChangeSupport PL;
    private Document logDocument = null;

    private boolean Debug = false;
    Document MacroDocument = null;

     private Vector vnames= new Vector();
     private String command ="UNKNOWN";
     private String Title = "UNKNOWN";
     private String CategoryList="OPERATOR";

    

    



    /** Constructor with no Visual Editing Element.  
     *  This form could be used for Batch files
     *
     *    @param   TextFileName     The name of the text file containg commands
     */
    public ScriptProcessor ( String TextFileName )
      {super( "UNKNOWN");
       int c ;       
       Document doc ; 
       initialize() ; 
       File f ; 
       ExecLine = new Command.execOneLine() ; 
       OL = new IObserverList() ;
       PL = new PropertyChangeSupport((Object) this );   
      
        MacroDocument =new Util().openDoc( TextFileName);
        setDefaultParameters();
        Title = TextFileName;
    
    }



    /** Constructor that can be used to run Macros- non visual?
     *
     *    @param  Doc      The Document that has macro commands
     *    @param  O        Observer for the Send command
     */
    public ScriptProcessor ( Document Doc  )
      {super( "UNKNOWN");
       OL = new IObserverList() ;         
       initialize();     
       ExecLine = new execOneLine() ;
       PL = new PropertyChangeSupport( (Object)this ); 
       MacroDocument = Doc ;
       ExecLine.initt();
       ExecLine.resetError();
       seterror( -1,"");
       lerror = -1;
       setDefaultParameters();  
      }
/** 
  * Sets up the document that logs all operations
  *@param   doc     The document that logs operations
  * Note: Not used yet
*/
    public void setLogDoc( Document doc)
    { logDocument = doc;
      ExecLine.setLogDoc( doc);
    }

    public void setTitle( String title)
    {Title = title;
    }
    private void initialize()
    {
      
         
    
    
    }

    /** Initializes the Visual Editor Elements
     *
     */ 
    private void init()
    {

	
	
    }

    /**
     *adds a data set to the permanent workspace of the programs
     *@param    dss    The data set that is to be added
     */
    public void addDataSet( DataSet dss )   
    { if( Debug)System.out.println( dss.getTitle());
       ExecLine.addDataSet( dss ) ; 
       
      }
   



    /**Executes one line in a Document
     * @param Doc   The document with the line in it
     * @param line  The line in the document to be executed
     */
    public  void execute1( Document  Doc  ,  
                           int       line )

    {
     String S ; 
     //Element E ; 
       
     if( Doc == null )return ;   
     if( line < 0 )return ;    
     S = getLine( Doc , line);          
     if( S == null )
        {seterror( 0 , "Bad Line number");
         lerror = line;
          return;
        }
     int kk = ExecLine.execute( S , 0 , S.length() ) ; 
     perror = ExecLine.getErrorCharPos() ; 
     if( perror >= 0 )
	{lerror = line ; 
	 serror = ExecLine.getErrorMessage() ; 
              
	 }
     else if( kk < S.length() - 1 )
        {lerror = line ;  
         perror = kk ; 
         serror = "Extra Characters at the end of command" ; 
        } 

    }




    /** resets the error conditions and the variable space
    *  NOTE: The data sets that were added from outside stay
    */
    public void reset()
      {
        ExecLine.initt();
        ExecLine.resetError();
      }

    /** executes all the lines of code in the document
    *@param  Doc  The document that has the program in it
    */
    public void execute( Document Doc )
      {int line ; 
       Element E ; 
       if( Doc == null ) return ;      
       // if( MacroDocument == null)
       //    new IsawGUI.Util().appendDoc( logDocument , "#$ Start Program Run");
       // else
       //    new IsawGUI.Util().appendDoc(logDocument , "#$ Start Macro Run");

       E = Doc.getDefaultRootElement() ; 
       /* for( line = 0 ; line < E.getElementCount(); line ++)
         { String S = getLine( Doc , line);
           new IsawGUI.Util().appendDoc(logDocument , S);
         }
       */
       ExecLine.initt() ; 
      

       ExecLine.resetError();
       perror = -1 ; 
       line = executeBlock( Doc , 0 ,  true ,0 ) ; 
       if( perror < 0)
           seterror( ExecLine.getErrorCharPos(),ExecLine.getErrorMessage());
       if( perror >= 0 ) 
          if( lerror < 0 )lerror = line;
       if( line  <  E.getElementCount() )
	   if( perror < 0 )
              {seterror(  line , "Did Not finish program" ) ; 
               lerror = line;
              }
       

       /*  if( MacroDocument == null)
	  new IsawGUI.Util().appendDoc( logDocument , "#$ End Program Run");
       else
          new IsawGUI.Util().appendDoc(logDocument , "#$ End Macro Run");        
       */
         
    }

  //deprecated??
   private void execute( Parameter Args[])
    {int i;
     String S;
     //System.out.println("YYYYYYYYYYYYYYYYYYYYYY");
     Vector vnames = new Vector();
     ExecLine.initt();
     ExecLine.resetError();
     seterror( -1, "");
     lerror = -1;
     if( Args != null)    
     for( i = 0 ; i < Args.length ; i++)
       {  if( Args[i].getValue() instanceof DataSet)
            { DataSet ds = (DataSet)(Args[i].getValue());
              //vnames.add( Args[i].getName());//ds.getTitle());
              //vnames.add( ds);
              //ds.setTitle(Args[i].getName());
              ExecLine.addParameterDataSet( ds,Args[i].getName());
              
            }
          else
            {S = Args[i].getName().toString() + "=" ;
             if( (Args[i].getValue() instanceof String) ||(Args[i].getValue() instanceof SpecialString))
                S = S + '"' +  Args[i].getValue().toString()+ '"';
             else
                S = S + Args[i].getValue().toString();            
             ExecLine.resetError() ;
             int j = ExecLine.execute ( S , 0 , S.length());
             perror =ExecLine.getErrorCharPos() ;
             if( perror >= 0 )
                serror = ExecLine.getErrorMessage()+" in Parameters " + S;
             if( perror >= 0)
               {lerror = i;
                //return;  must reset dataset titles
               }
            }

       }
      int k =lerror; 
      if( perror < 0)
	  { //new IsawGUI.Util().appendDoc(logDocument , "#$ Start Macro Run");
          S="(";
          int line;
          if( Args != null)
          for( i=0; i< Args.length ; i++)
             {S = S + Args[i].getValue().toString();
              if( i+1 < Args.length) S = S+",";
             }
           S = S + ")";
	   //new IsawGUI.Util().appendDoc(logDocument , "Args ="+S);

          Element E = MacroDocument.getDefaultRootElement() ; 
	  /* for( line = 0 ; line < E.getElementCount(); line ++)
             { S = getLine( MacroDocument , line);
                new IsawGUI.Util().appendDoc(logDocument , S);
              }
	  */
          k = executeBlock( MacroDocument ,2 ,true ,0) ;
          //new IsawGUI.Util().appendDoc(logDocument , "#$ End Macro Run");
         }

     //for(i = 0 ; i < (vnames.size()/2) ; i++)
     //  {DataSet ds =(DataSet)(vnames.get( 2*i + 1));
     //   ds.setTitle( vnames.get( 2*i ).toString() );
     //  }
    if( perror < 0)
         seterror( ExecLine.getErrorCharPos(), ExecLine.getErrorMessage());
    if( (perror >= 0) && (lerror <  0 ))
        lerror = k;
        
      }

   private int executeBlock ( Document Doc , int start ,  boolean exec ,int onerror )
     { int line ;
      
       String S ; 
     
      if( Doc == null)
         { seterror (0 , "No Document");
           if( Debug)System.out.println("NO DOCUMENT");
           lerror = 0;
            return 0 ;
         }
       Element  E = Doc.getDefaultRootElement(),
	        F ;                 
       line = start ; 
       if(Debug)System.out.print( "In exeBlock line ,ex=" + line+","+exec + perror ) ; 
       while ( ( line < E.getElementCount() ) && ( perror < 0 ) )
	   {
           S = getLine( Doc , line );
           
           
           if( S !=  null )
               { 
	         int i ; 
                 char c ; 
                 for( i = S.length() - 1 ;  i >=  0  ;  i-- )
		  { if( S.charAt( i ) <=  ' ' )S = S.substring( 0 , i ) ; 
		    else i = -1 ; 
		  }
               }
           
            if( S == null )
	      {if( Debug )
                 System.out.println(" S is null " );
               }
            else if( S.trim() == null)
               {
               }
            else if( S.trim().indexOf( "#") == 0 )
               {
               }
            else if( S.trim().indexOf("$" ) == 0)
              {
               }
            else if( S.toUpperCase().trim().indexOf( "ELSE ERROR" ) == 0 )
	       return line ; 
	    else if( S.toUpperCase().trim().indexOf( "END ERROR" ) == 0 )
	      return line ; 
            else if( S.toUpperCase().trim().indexOf( "ON ERROR" ) == 0 )
	       line = executeErrorBlock( Doc , line , exec ) ;               

            else if( onerror > 0 )
              {
              }
            else if( S.toUpperCase().trim().indexOf( "FOR " ) == 0 )
	      line = executeForBlock ( Doc , line , exec ,onerror ) ; 
	    else if( S.toUpperCase().trim().indexOf( "ENDFOR" ) == 0 )
	      return line ; 

	    else if( S.toUpperCase().trim().indexOf("IF ") == 0)
               {line = executeIfStruct( Doc, line, exec , onerror );              
                
               }
            else if( S.toUpperCase().trim().equals("ELSE"))
               return line;
            else if( S.toUpperCase().trim().indexOf("ELSEIF") == 0)
               return line;
            else if( S.toUpperCase().trim().equals("ENDIF"))
               return line;
           else if( S.trim().length() <= 0 )
	      {}
            
	   else if( exec )  //can transverse a sequence of lines. On error , if then
             { ExecLine.resetError();
               execute1( Doc  , line ) ; 
             }
          
            if( perror >= 0 )
	      {if( lerror < 0 )lerror = line ; 
		  if(Debug)
                    System.out.println("errbot"+line) ; 
		return line ; 
              }
	      if(Debug)System.out.println( " Thru" +line) ; 
            line ++  ; 
              


	   } ; 
       return line ; 
     }
    

    
private int executeForBlock( Document Doc , int start , boolean execute, int onerror )
    { String var ; 
      //Command.ListHandler LL ; 
      int i , j , k , n; 
      int line  ;  
       String S ; 
       Element  E ,
	        F ;  
       Vector V;
       if( Doc == null ) return -1 ; 
       E = Doc.getDefaultRootElement() ; 
       if( start < 0 ) return start ; 
       if( start >= E.getElementCount() ) return start ;  
  
       S = getLine( Doc , start );
       if( S == null)
	 {seterror ( 0 , "Internal Errorb" ) ; 
	  return start ; 
	 }         
       i =  S.toUpperCase().indexOf( "FOR " ) ; 
       if( i < 0 )
	 { seterror ( 1 ,  "internal error" ) ; 
	   return start ; 
         }
       if( execute && (onerror==0))
         {j = S.toUpperCase().indexOf( " IN " ,  i + 1 ) ;
          if( j< 0)
	     {perror = i;
	      seterror(i, "Syntax Error IN required");
              lerror = start;
              return start;
             }
          var= S.substring( i + 4, j).trim(); 
          String iter = S.substring( j + 4  ).trim() ;
      
           int kk = ExecLine.execute( iter, 0, iter.length());
           perror = ExecLine.getErrorCharPos();
        
       
          if( perror >=0)
	     {perror = perror +j;
               seterror(perror+j, ExecLine.getErrorMessage());
               lerror = start;
               return start;
              }
          Object U = ExecLine.getResult();
         if( !(U instanceof Vector) || (U==null))
            {seterror( j+4, ExecLine.ER_IMPROPER_DATA_TYPE);
             lerror = start;
             return start;
            }
          V = (Vector)U;
          n=V.size();
        }
     else //just go thru motions
        {n=1;
        V = new Vector();
        var ="MMM";
        }   
      
          line = start + 1 ; 
       
       for( int jj = 0; jj< n ;jj++)
	   {/*S = null;
            
            if( V.elementAt(i) instanceof String)
                S =  var + " = " + "\""+ V.elementAt(i)+"\"" ;
            else if(V.elementAt(i) instanceof DataSet)
              //help need variable name.  The to String will not do.
              ExecLine.Assign( var, V.elementAt(i));
            else if(V.elementAt(i) instanceof Vector)
              ExecLine.Assign(var,V.elementAt(i);
            else
                S =  var + " = " + V.elementAt(i); 
           
	    if( execute) i = ExecLine.execute ( S , 0 , S.length() ) ;
            */
            if(execute && (onerror ==0))
             {ExecLine.Assign(var,V.elementAt(jj)); 
              if( (perror >= 0) && (onerror == 0) )
		{seterror(i+4 , ExecLine.getErrorMessage());
                 lerror = start;
                 return start ; 
                }
              }
              //if(execute) 
              line = executeBlock( Doc , start + 1 , execute ,0 ) ; 
	      if ( (perror >= 0) && (onerror == 0) )
		 return line ; 
              if( line >= E.getElementCount() )
                { seterror( 0 , "No EndFor for a FORb" + S ) ; 
	          return line ; 
                }
             
               S = getLine( Doc , line );
               if( S == null )
	       {
		seterror( 0 ,  "Internal error" ) ; 
                return line ; 
	       }
             if( perror >= 0 ) return line ; 
	     if( !S.toUpperCase().trim().equals( "ENDFOR" ) )
	       {seterror( 0 , "No EndFor for a FORx" + S ) ; 
		return line ; 
               };
            
                  
           }	//end for jj=    
       return line ; 
                     

      }

    private int executeErrorBlock( Document Doc , int start , boolean execute )
      {String var ;      
      int i , j , k ; 
      int line ; 
       String S ; 
       int mode ; 
       Element  E ,
	        F ;  
       if(Debug) System.out.println("In exec Error" ) ; 
       if( Doc == null ) return -1 ; 
       E = Doc.getDefaultRootElement() ; 
       if( start < 0 ) return start ; 
       if( start >= E.getElementCount() ) return start ;   
  
       S = getLine( Doc , start);

       if( S == null )
	 {seterror ( 0 , "Internal Errorc" ) ; 
	  return start ; 
	 }
       if( !S.toUpperCase().trim().equals( "ON ERROR" ) )
	   {seterror ( 0, "internal error d" ) ; 
	    return start ; 
           }
       line = executeBlock( Doc , start + 1 , execute, 0 ) ; 
       if( perror >= 0 )
	 {ExecLine.resetError() ; 
	   perror = -1 ; 
           serror="";
           if(Debug) System.out.println("In ExERROR ERROR occured");
          
  	   line = executeBlock( Doc , line , false, 1 ) ; 
           mode  = 0  ;    
           if(Debug) System.out.println("After EXERROR ocurred "+line);
          
         }
       else mode = 1 ; 
 
       if( line >= E.getElementCount() )
         {seterror ( 0 , " No ENDERROR for On Error" ) ; 
	  return line ; 
          }


      S = getLine( Doc , line );
      if( S == null )
	 {seterror ( 0 , "Internal Errorc" ) ; 
	  return start ; 
	 }
      if( S.toUpperCase().trim().equals( "ELSE ERROR" ) )
	{ if(Debug)
            System.out.println("ELSE ERROR on line="+line+","+execute+mode);
          line =executeBlock( Doc , line + 1 , execute, mode ) ;
          
          if( perror >= 0)
            {lerror = line;
             if(Debug) System.out.println( "ELSE ERROR ERROR ob lin"+line);
             int pperror = perror;
             perror = -1;
             line = executeBlock( Doc, line, execute, 1);
             perror = pperror;
             if(Debug)System.out.println("ELSE ERROR ERROR2, line,perror ="+line+","+perror);
            }
            
	}
      else if( S.toUpperCase().trim().equals( "END ERROR" ) )
	  {if(Debug)System.out.println("ENd ERROR occurred"+perror+","+line);
            return line ; 
          
        }
      else
	{  seterror( line , " NO ELSE or END Error for On ERRor" ) ; 
	   return line ;  
	}

    
          

     S = getLine( Doc , line );
     if( S == null )
	 {seterror ( 0 , "Internal Errorc" ) ; 
	  return start ; 
	 }
     if( !S.toUpperCase().trim().equals( "END ERROR") )
      {seterror( 0 , " NO ELSE or END Error for On ERRor" ) ;  }

      if(Debug)System.out.println("ENd ERROR occurred"+perror+","+line+","+lerror);

      if( perror < 0)
          return line ; 
      else 
          return line+1; // in case nested on errors 
      


      }
  private int executeIfStruct( Document Doc, int line, boolean execute, int onerror )
     { String S;
       int i , 
           j;
       if( Debug) 
          System.out.print("Start if line=" + line);
       S = getLine( Doc, line );
       if( Debug)
          System.out.println( ":: line is " + S );
      if( S == null)
        { perror = 0;
          serror = "Internal Error 12";
          lerror = line;
          return line;
        } 
      i = S.toUpperCase().indexOf( "IF " ) ;
      if( i < 0)
        { perror = 0;
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
      //if( execute )
      //   b= evaluate( S, i, j );
      //else
      //   b = false;
      int kk =ExecLine.execute( S, i, j);
      if( ExecLine.getErrorCharPos()>=0)
         { seterror(ExecLine.getErrorCharPos(), ExecLine.getErrorMessage());
           lerror= line;
           return line;
         }
      Object X = ExecLine.getResult();
      if( X instanceof Integer)
         if( ((Integer)X).intValue()==0) X = new Boolean(false);
         else X = new Boolean(true);
      if( !(X instanceof Boolean))
        { seterror(j,execOneLine.ER_ImproperDataType);
           lerror= line;
           return line;
         }
      b= ((Boolean)X).booleanValue();   
      if(Debug) 
           System.out.println("aft eval b and err ="+b+","+perror);
      if( perror >= 0 )
         return line;
     
      j = executeBlock ( Doc , line + 1 , b && execute , 0 ) ;
      if( Debug)
           System.out.println( "ExIf::aft exe 1st block, perror=" + perror +serror );
      if( perror >= 0 ) 
        return j;
      S = getLine ( Doc , j );
       if(Debug) 
           System.out.println("ExIf:: Els or Elseif?"+S);
      if( S == null)
       { seterror( 0 , "Improper line" );
         lerror = j;
         return j;
       }
      int x=0;
      if( S.toUpperCase().trim().indexOf( "ELSE" ) == 0)
        if( S.toUpperCase().trim().indexOf( "ELSEIF" ) == 0 )
          { j = executeIfStruct( Doc , j , !b && execute ,0);  
            return j;
	             
          }
        else 
            {j = executeBlock( Doc , j+1 , (!b) && execute, 0 );
             x = 2;
            }
      if(Debug) 
        System.out.println( "ExIf:: aft exec 1st block, perror=" + perror );
      if( perror >= 0) 
        return j;

      S = getLine ( Doc , j );
     if( Debug ) 
       System.out.println( "ExIf:: ENDIF?" + S );
      if( S == null)
        {seterror( 0, "Improper line" );
         lerror = j;
         return j;
        }
      if(! S.toUpperCase().trim().equals( "ENDIF" ) )
       {seterror( 0, "If without an ENDIF" );
        lerror = line;
         return j;
       }  
      if( Debug) 
        System.out.println( "ExIF end OK, line is " + j );       
      return j;
      
     } 
  /**
   *  Utility to get the Next Macro line( starts with $ or #$) in a document
   *  @param Doc  The document containing the program
   *  @param prevLine  the line number of the previous Macro line of -1 for first
   *  @return  The line number of the next macro line or -1 if none
  */
   public int getNextMacroLine( Document Doc, int prevLine)
    { String Line;
      prevLine++;
      if( prevLine < 0 ) 
        prevLine = 0;
      Line = getLine( Doc , prevLine);
      if( Line == null )
        return -1;
      if( Line.trim().indexOf("#$$") == 0)
         return prevLine;
      if( Line.trim().indexOf("$") == 0)
         {if(Debug)System.out.println("get $ Macro line #"+prevLine);
           return prevLine;
         }
      return getNextMacroLine( Doc, prevLine++);
    } 

 /** Utility to return a given line from the Document
 *@param Doc the document with the line
 *@param start  the line number to be returned
 *@return  The string representation of that line or null if there is none
 */
   public String getLine( Document Doc, int start )
     {
      String var ;      
      int i , j , k ; 
      int line ; 
      String S ; 
      boolean mode ; 
      Element  E ,
	       F ;  
       
      if( Doc == null ) 
        return null ; 
      E = Doc.getDefaultRootElement() ; 
      if( start < 0 ) 
        return null ; 
      if( start >= E.getElementCount() ) 
         return null ;   
      F = E.getElement( start ) ; 
      try{
        S = Doc.getText( F.getStartOffset() , F.getEndOffset()  -  F.getStartOffset() ) ; 
         }
      catch( BadLocationException s )
	{seterror ( 0 , "Internal Errorc" ) ; 
	 return null ; 
	}
     
      if( S != null) 
         if( S.charAt(S.length() - 1 )<' ' ) 
             S = S.substring( 0,S.length() - 1 );
      return S;

    }

    private void seterror( int poserr , String ermess )
      { perror = poserr ; 
        serror = ermess ; 
      }
      
 /** Utility to delete xcess spaces outside of quotes(") in a string S
*/   
    public String delSpaces( String S)
      { boolean quote,
                onespace; 
        int i ; 
        String Res;
       char prevchar;
       if( S == null ) return null;
       Res  = "";
       quote = false;
       onespace = false;
       prevchar = 0; 
       for ( i =0; i < S.length() ; i++)
         {   
         
           if( S.charAt( i) == '\"') 
             {quote = ! quote;
	      if( i > 0)
	        if (!quote )
		  if( S.charAt( i-1) == '\\') quote = !quote;
              Res = Res + S.charAt ( i );
              prevchar = S.charAt ( i );
             }
           else if( quote ) 
             { Res = Res + S.charAt(i);
               prevchar = S.charAt ( i );
             }
           else if( S.charAt ( i ) == ' ')
	     {
	       if( " +-*/^():[]{}," . indexOf(S.charAt(i + 1 )) >= 0)
		   {}
               else if( i+1>= S.length()){}
               else if( i < 1) {}
               else if("+-*/^():[]{},".indexOf(S.charAt( i - 1 ) ) >= 0)
		   {}
               else
		   Res = Res + S.charAt( i ) ; 
               prevchar = ' ';
	     }
           else
	     {
	       Res = Res + S.charAt(i);
               prevchar = S.charAt(i);
	     }
  

          }
       return Res;

     
      }
 


/* deprecated
*  Determines if a document as parameters (#$$) ot ($). If so, a vector representation
*  of the parameters is returned
*@param   doc    A (Plain)Document that contains a script
*
* @return   null if there are NO parameters or <P>
*            A Vector where each parameter in the script represents 2 indeces as follows:<ul>
*             <li> first (least index) variable name
*             <li> Next a parameterGUI  of the correct data type and Message
*            </ul>
*/
 /* private  Vector getDefaultParameterss( Document doc )
    { Element E;
      String S , 
             Line;
      int start ,
          i;
      Vector V = new Vector();
      if( Debug) System.out.println("Start get Def par"+ perror);
       if( doc == null) 
         return null;
       
       S = " Enter Parameter Value ";
       Line = getLine( doc , 1 );
       if( Line == null )
          Line ="";
       if( Line.trim().indexOf('#')==0)
         { i = Line.indexOf( '#');
           Line = Line.substring( i+1);
         }
       V.addElement( Line );
       E = doc.getDefaultRootElement();
       start = 0;
       int j , k;
       if(Debug) System.out.println("Next line="+getNextMacroLine( doc,-1));
       for( i = getNextMacroLine( doc,-1) ; i>= 0; i = getNextMacroLine( doc , i))
         {Line = getLine( doc , i);
         
         if( Debug)
            System.out.println("Line="+Line);
         
          if(Line == null )
            return V;
          if( Line.trim().indexOf("#$$")==0)
             start = Line.indexOf("#$$")+3;
          else start = Line.indexOf("$")+1;
          if(Debug) System.out.println("start="+start);
          if( start < 1)
            return V;
          start = ExecLine.skipspaces(Line , 1 , start );
          j = findQuote ( Line , 1 , start, " " , "" );
          if( (j >= 0) && ( j < Line.length() ))
            V.addElement(Line.substring( start , j).trim());
          start = j;
          if( start >= Line.length())
            {seterror( start , "Improper Parameter Format");
             return null;
            }
          start = ExecLine.skipspaces(Line , 1, start );
          j = findQuote( Line , 1, start, " ", "" );
       
          String DT = Line.substring( start , j );//.toUpperCase();
          
          String Message;
          j = ExecLine.skipspaces( Line , 1, j );
        
          if( j < Line.length() )
            Message = Line.substring( j ).trim();
          else
            Message = "";
          if(Debug)
            System.out.println("in line start end="+ start + ","+DT+","+Message);
          DT = DT.toUpperCase();
          if( (DT .equals( "INT") ) || ( DT.equals( "INTEGER")))
             V.addElement( new JIntegerParameterGUI
                          ( new Parameter ( Message , new Integer (0)) ) );
          else if ( DT.equals( "FLOAT"))
             V.addElement( new JFloatParameterGUI
                          (  new Parameter ( Message , new Float (0.0)) ) ); 
          else if( DT.equals( "STRING"))
             V.addElement( new JStringParameterGUI
                      ( new Parameter ( Message , "" ) ) );
          else if(DT.equals("BOOLEAN"))
             V.addElement(new JBooleanParameterGUI
                           ( new Parameter ( Message, new Boolean(true)) ) );
          else if( DT.equals("ARRAY"))
	      V.addElement(new JArrayParameterGUI(
                        new Parameter( Message, new Vector())));
          else if( DT.equals("DataDirectoryString".toUpperCase()))
             { String DirPath = System.getProperty("Data_Directory");
               if( DirPath != null )
                   DirPath = DataSetTools.util.StringUtil.fixSeparator( DirPath+"\\");
               else
                   DirPath = "";
               V.addElement( new JStringParameterGUI
                              ( new Parameter( Message, DirPath)));
              }
          else if( DT.equals("DSSettableFieldString".toUpperCase()))
            { //V.addElement(new Parameter(Message, new DSSettableFieldString());
                AttributeList A = new AttributeList();
            // Attribute A1;
	     DSSettableFieldString dsf1 = new DSSettableFieldString();
             
             for( k =0; k< dsf1.num_strings(); k++)
		 {   
                    A.addAttribute( new StringAttribute( dsf1.getString(k), "") );
                }
               
              V.addElement( new JAttributeNameParameterGUI( new Parameter( Message ,new DSSettableFieldString() ) , A));
             
            }
          else if (DT.equals( "DSFieldString".toUpperCase()))
            {  //V.addElement( new Parameter(Message , new DSFieldSTring());
              //String Fields[] = {"Title","X_label", "X_units", "PointedAtIndex","SelectFlagOn",
             //          "SelectFlagOff","SelectFlag","Y_label","Y_units", "MaxGroupID",
              //          "MaxXSteps","MostRecentlySelectedIndex", "NumSelected" , "XRange",
              //         "YRange"};
             AttributeList A = new AttributeList();
            // Attribute A1;
	    DSFieldString dsf = new DSFieldString();
            
             
             for( k =0; k< dsf.num_strings(); k++)
		 {   

                    A.addAttribute( new StringAttribute( dsf.getString(k), "") );
                 }
           
             
             
             V.addElement( new JAttributeNameParameterGUI( new Parameter( Message ,new DSFieldString() ) , A));
            
            }
          else if( DT.equals( "InstrumentNameString".toUpperCase()))
            {String XX = System.getProperty("Default_Instrument");
             if( XX == null )
               XX = "";
             V.addElement( new JStringParameterGUI
                        ( new Parameter( Message, XX )));
            }
          else if ( DT.equals( "DataSet".toUpperCase()) )
           {
	   DataSet DS[] = ExecLine.getGlobalDataset();
            DataSet dd = new DataSet("DataSet","");
            Parameter PP = new Parameter( Message , dd);
            if(Debug)System.out.println("Dat Set Param dd="+PP.getValue()+","+PP.getName());
            JlocDataSetParameterGUI JJ = new JlocDataSetParameterGUI( PP , DS);
            V.addElement(JJ);
            
            if(Debug)
              {System.out.print("DS"+JJ.getClass()+","); 
               System.out.print( JJ.getParameter()+",");
               System.out.print(  JJ.getParameter().getValue()+",");
              // System.out.println(JJ.getParameter().getValue().getClass());
              }

            } 
          else
            { seterror( start+12 , "Data Type not supported " + DT);
	    lerror = i;
              return null; 
          }
      
        if( Debug) System.out.println("At bottom get def "+ perror+","+serror);
       }// For i=0 to count
       return V;
    }
    
*/
/**deprecated
  * A utility to get parameters for a macro AND execute the macro<P>
  * 
  * NOTE: To use this successfully<ul>
  * <li> The "Macro" Construtor was used 
  *<li>  The addDataSet Method was used to add any data sets that could be used as paramers
  *</ul>
  *
  * 
  *
  * NOTE: The result can be used by the execute( Parameter ) method
  *
  * @see Command.CommandPane()  Constructor
  * @see ScriptProcessor.addDataSet( DataSetTools.dataSet.DataSet ) addDataSet
  * @see ScriptProcessor.getErrorCharPos()
  *@see ScriptProcessor.getErrorMessage()
  * Sample Code Segment
  *<Pre> 
   *    Document D = new util().Open( filename);  
   *    CommandPane cp = new CommandPane( D , CP );
   *    cp.addPropertyChangeListener( CP ) ;// To get non data set DISPLAY info
   *    
   *    
   *    DataSet dss[] = ExecLine.getGlobalDataset();//Data sets can come from 
   *                                               //anywhere
   *     if( dss != null)
   *        for( int i = 0; i < dss.length ; i++ )
   *          cp.addDataSet( dss [i]);
   *     Parameter P[] = cp.GUIgetParameters();
   *    
   *     if( Debug) 
   *        System.out.println("After macro execut error " + 
   *            cp.getErrorCharPos()+ "," + cp.getErrorMessage() +","+cp.getErrorLine());
    </pre>
  */
  /*private  Parameter[] GUIgetParameterss()
     { if(Debug)System.out.println("Start of GUIgetParameters");
       if( MacroDocument == null)
         {seterror( 1000, "Macro Does not Exist ");
          return  null;
         }
    if( Debug) System.out.println("Start GUI get Parameters");
    Vector V1 = getDefaultParameterss( MacroDocument );
    if( Debug )System.out.println("after get defaults "+ perror+","+lerror+","+serror);
    if( perror >= 0)
       {return null; 
       }
    if( Debug )
       if( V1 != null )
         for( int i = 0 ; i < V1.size() ; i++)
           System.out.println( "par i ="+V1.elementAt(i));
    if( V1 == null)
      return new Parameter[0];
    if( V1.size() <2)
      return new Parameter[0];
    Vector V = new Vector();
    V.addElement( V1.elementAt(0)); 
    for( int i = 2; i < V1.size(); i+=2)
     {V.addElement( V1.elementAt( i ) );
     }
     Command.JScriptParameterDialog X = new Command.JScriptParameterDialog( V, ExecLine.getGlobalDataset() );
     if( Debug ) System.out.println( "After Dialog box");
     V = X.getResult();
     if( V == null)
       return null;
     Parameter U ;
     Parameter P[] = new Parameter[ V.size() - 1] ;
     JParameterGUI JPP;
     for( int i = 1 ; i < V.size(); i++)
       { JPP= (JParameterGUI)(V.elementAt( i )) ;
         U = JPP.getParameter();
         P[i-1]= new Parameter( (String)(V1.elementAt(2*i -1 )) , U.getValue()) ;//new Parameter((String)( V1.get( i - 1 )), U.getValue());
       
       }
    
     return P;

         
   } 
*/
/*  private  Command.JScriptParameterDialog X = null;
  // Will eventually get an operator form for a Macro with parameters.
  //    So any result will hava a place to display
   public void SendMessageToScript( String Message)
    {if( MacroDocument == null )
       return;
     if( X== null )
       return;
     X.setMessage( Message);
    }
*/

//************************SECTION:EVENTS********************
    /**
     *adds an Iobserver to be notified when a new data Set is sent
     *@param  iobs    an Iobserver who wants to be notified of a data set
     */
  public void addIObserver( IObserver iobs )
    {ExecLine. addIObserver( this ) ; 
     OL.addIObserver( iobs ) ; 
     
    }

   /**
     *deletes an Iobserver who no longer wants to be notified when a new data Set is sent
     *@param  iobs    an Iobserver who wants to be notified of a data set
     */
  public void deleteIObserver( IObserver iobs )
    {ExecLine.deleteIObserver( this ) ; 
      OL.deleteIObserver( iobs ) ; 
    }

   /**
     *deletes all the Iobserver 
    
     */

  public void deleteIObservers()
    {ExecLine.deleteIObservers() ; 
      OL.deleteIObservers() ; 
    }

  
    public String getVersion()
    { return "6-01-2001";
    } 


/**
* Gets the position in a line where the error occurred
*
*/
public int getErrorCharPos()
   { if(ExecLine == null ) return -1;
     return perror;
     //return ExecLine.getErrorCharPos();
   }

/**
* Gets the Message for the error 
*
*/
public String getErrorMessage()
   { if( ExecLine == null ) return "";
      return serror;
     //return ExecLine.getErrorMessage();
   }
/**
* Returns the line where the error occurred
*/
public int getErrorLine()
  { return lerror;
  }

/**
*  Sets Default parameters for getResult or for the JParametersGUI Dialog box<P>
*  INPUTS:  The variable doc stored in MacroDocument<BR>
*  OUPUTS:  The parameters are set
*/
public void setDefaultParameters()
   {  Element E;
      String S , 
             Line,
             VarName,
              InitValue;
      int start ,
          i;
      Document doc = MacroDocument;
      
      if( Debug) 
          System.out.println("Start get Def par"+ perror);
      if( MacroDocument == null) 
         return ;
       
      parameters= new Vector();
      vnames= new Vector();
      E = doc.getDefaultRootElement();
       start = 0;
       int j , k;
       if(Debug) System.out.println("Next line="+getNextMacroLine( doc,-1));
       for( i = getNextMacroLine( doc,-1) ; i>= 0; 
                     i = getNextMacroLine( doc , i))
         {Line = getLine( doc , i);
         
         if( Debug)
            System.out.println("Line="+Line);
         
          if(Line == null )
            return;
          if( Line.trim().indexOf("#$$")==0)
             start = Line.indexOf("#$$")+3;
          else 
              start = Line.indexOf("$")+1;
          if(Debug) System.out.println("start="+start);
          if( start < 1)
            return;
          start = ExecLine.skipspaces(Line , 1 , start );
          j = findQuote ( Line , 1 , start, " =" , "" );
          if( (j >= 0) && ( j < Line.length() ))
            VarName = Line.substring( start , j).trim();
          else
             VarName = null;
          
	// Now get the Data Type or =
          start = j;
          if( start >= Line.length())
            {seterror( start , "Improper Parameter Format");
             lerror = i;
             return ;
            }
          start = ExecLine.skipspaces(Line , 1, start );
          j = findQuote( Line , 1, start, " (", "" );
          if( start < Line.length())
	    if( Line.charAt( start) == '=')
                j = start + 1;
          String DT = Line.substring( start , j );//.toUpperCase();
          
       // Now get the initial value
          InitValue = null;
          if( DT.equals( "=" ) )
             InitValue = null;
          else if( j<Line.length() )
             if( Line.charAt( j ) == '(')
               {start = j+1;
                j = findQuote( Line, 1, start , ")", "()");
                if( (j < 0 ) || ( j >= Line.length()) || ( j <= start) )
                  {seterror ( start, "Unmatched Parens" );
                   lerror = i;
                       return;
                   }
                 InitValue = Line.substring( start , j );
                 
                 j++;
               }
         
      // Now get Message
          String Message;
          j = ExecLine.skipspaces( Line , 1, j );
        
          if( j < Line.length() )
            Message = Line.substring( j ).trim();
          else
            Message = "";
          if(Debug)
            System.out.println("in line start end="+ start + ","+DT+","+Message);
          DT = DT.toUpperCase();


          if( !DT.equals( "=" ))
             vnames.addElement( VarName );
          if( DT.equals("="))
             {VarName = VarName.toUpperCase();
              if( VarName.equals("COMMAND"))
                 {command = Message;
                   }
              else if(VarName.equals( "TITLE" ))
                Title = Message ;
              else if( VarName.equals( "CATEGORY") )
                 CategoryList= Message ;
             }
          else if( (DT .equals( "INT") ) || ( DT.equals( "INTEGER")))
             { if( InitValue == null)
                  InitValue ="0";
               try {InitValue=InitValue.trim();
                    addParameter( new Parameter ( Message , new Integer (InitValue)) ) ;
                   }
               catch( Exception s)
                  {System.out.println("catch for Int, InitValue="+InitValue);
                     addParameter( new Parameter ( Message , new Integer (0)) ) ;

                  }

             }
          else if ( DT.equals( "FLOAT"))
            { if( InitValue == null)
                  InitValue ="0.0";
               try {InitValue=InitValue.trim();
                    addParameter( new Parameter ( Message , new Float (InitValue)) ) ;
                   }
               catch( Exception s)
                  {addParameter( new Parameter ( Message , new Float (0.0)) ) ;

                  }

             }
          else if( DT.equals( "STRING"))
             {if( InitValue == null)
                  InitValue ="";
              addParameter( new Parameter ( Message , InitValue ) ) ;
             }
          else if(DT.equals("BOOLEAN"))
             { if( InitValue == null)
                  InitValue ="true";

               try {
                    addParameter( new Parameter ( Message , new Boolean (InitValue.toLowerCase().trim())) ) ;
                   }
               catch( Exception s)
                  {addParameter( new Parameter ( Message , new Boolean (true)) ) ;

                  }

             }
          else if( DT.equals("ARRAY"))
	      { if( InitValue != null)
                    ExecLine.execute(InitValue , 0 , InitValue.length());
                Vector V = new Vector();
                if( InitValue != null )
                  if( ExecLine.getErrorCharPos() < 0)
                    {if( ExecLine.getResult() instanceof Vector)
                       V = (Vector)(ExecLine.getResult());
                     }
                             
                 addParameter( new Parameter( Message, V));
               }
          else if( DT.equals("DataDirectoryString".toUpperCase()))
             { String DirPath = System.getProperty("Data_Directory");
               if( DirPath != null )
                   DirPath = DataSetTools.util.StringUtil.fixSeparator( DirPath+"\\");
               else
                   DirPath = "";
               addParameter( new Parameter( Message, DirPath));
              }
          else if( DT.equals("DSSettableFieldString".toUpperCase()))
            { if(InitValue == null)
                  addParameter( new Parameter( Message ,new DSSettableFieldString() ) );
              else
                   addParameter( new Parameter( Message ,new DSSettableFieldString(InitValue.trim()) ) );

             
            }
          else if (DT.equals( "DSFieldString".toUpperCase()))
            {  if( InitValue == null )
                addParameter( new Parameter( Message ,new DSFieldString() ));
               else
                addParameter( new Parameter( Message ,new DSFieldString(InitValue.trim()) ));            
            }
          else if( DT.equals( "InstrumentNameString".toUpperCase()))
            {String XX = System.getProperty("Default_Instrument");
             if( XX == null )
               XX = "";
             addParameter(  new Parameter( Message, XX ));
            }
          else if ( DT.equals( "DataSet".toUpperCase()) )
           {
	    
            DataSet dd = new DataSet("DataSet","");
            if( InitValue != null)
              {ExecLine.execute( InitValue, 0, InitValue.length());
               if( ExecLine.getErrorCharPos() < 0 )
                  if( ExecLine.getResult() instanceof DataSet )
                    dd = ( DataSet )( ExecLine.getResult() );
               }
            Parameter PP = new Parameter( Message , dd);
            addParameter ( PP );
           

            } 
          else
            { seterror( start+12 , "Data Type not supported " + DT);
	    lerror = i;
              return; 
          }
      
        if( Debug) System.out.println("At bottom get def "+ perror+","+serror);
       }// For i=0 to count
       ExecLine.resetError();
       seterror( -1,"");
       return ;
   
   }
/** 
*  Gives the Command to use this script in the as a function in this ScriptProcessor.<BR> NOT USED  
*
* NOTE: ScriptOperators are used for functions in this scriptProcessor
*@see  Command.ScriptOperator
*/
public String getCommand()
  { return command;
  }
/**
*  Gives the Title of this program document. The Title appears in dialog boxes and menu items
*
*NOTE:  To set the Title have a line "$Title= title string" in the document
*/
public String getTitle()
  {return Title;
  }

/** Executes the whole script then returns the result
*@return  the result.  If there is an error the result is a subclass of ErrorString
*@see DataSetTools.util.ErrorString

*/
public Object getResult()
  { 
      int i;
     String S;
     
     ExecLine.initt();
     ExecLine.resetError();
     seterror( -1, "");
     lerror = -1;
     
     for( i = 0 ; i < getNum_parameters() ; i++)
	 { if(getParameter( i ).getValue() == null)
            {serror = "Undefined Parameter "+i;
             perror =i;
             lerror = i;
             return new ErrorString( serror );
              }
           else if( getParameter( i ).getValue() instanceof DataSet)
            { DataSet ds = (DataSet)(getParameter( i ).getValue());        
              
              ExecLine.addParameterDataSet( ds , (String)vnames.elementAt(i));
              
            }
          else
            {S =  (String)vnames.elementAt(i)+ "=" ;
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
             if( perror >= 0)
               {lerror = i;
                //return;  must reset dataset titles
               }
            }

       }// for i=0 to Num_parameters
      int k =lerror; 
      if( perror < 0)
	  { //new IsawGUI.Util().appendDoc(logDocument , "#$ Start Macro Run");
          S="(";
          int line;
          
          for( i=0; i< getNum_parameters() ; i++)
             {S = S + getParameter( i ).getValue().toString();
              if( i+1 < getNum_parameters() ) 
                    S = S + "," ;
             }
           S = S + ")";
	   //new IsawGUI.Util().appendDoc(logDocument , "Args ="+S);

          Element E = MacroDocument.getDefaultRootElement() ; 
	  /*  for( line = 0 ; line < E.getElementCount(); line ++)
             { S = getLine( MacroDocument , line);
                new IsawGUI.Util().appendDoc(logDocument , S);
              }
	  */
          k = executeBlock( MacroDocument ,0 ,true ,0) ;
          //new IsawGUI.Util().appendDoc(logDocument , "#$ End Macro Run");
         }

    
    if( perror < 0)
         seterror( ExecLine.getErrorCharPos(), ExecLine.getErrorMessage());
    if( (perror >= 0) && (lerror <  0 ))
        lerror = k;
        
     


   if( ExecLine != null )
      if( ExecLine.getErrorCharPos() >= 0)
        return new ErrorString( ExecLine.getErrorMessage() +" on line "+lerror+ "at position "
                         +ExecLine.getErrorCharPos() );
      else
        return ExecLine.getResult();
    else
      return null;
  }

/**
* Executes when a PropertyChangeEvent Occurs
*
*@param  e   The property change Event. 
*NOTE: The only PropertyChangeEvent processed has a name "Display"
*/
  public void propertyChange(PropertyChangeEvent e)
   {
     PL.firePropertyChange( e );
   }
/**
*  The only "property" that changes is the "Display"<br>
*  Use this method if you want to be notified of the Display
*  Command for non Data Sets
* @param  P  The class that wants to be notified
*/
public void addPropertyChangeListener( PropertyChangeListener P)
  {if( ExecLine == null ) return;
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
public  void   update(  Object observed_obj ,  Object reason ) 
    {OL.notifyIObservers( this  ,  reason  ) ; 
    }
/** Utility that returns all the data sets that have been added from outside sources
*/
public DataSet[] getDataSets()
   {return ExecLine.getGlobalDataset();
   } 


private int findQuote(String S, int dir ,int start, String SrchChars,String brcpairs)
      {int i, j, j1;
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
       for ( i = start ; (i < S.length()) &&( i >= 0) ; i += dir )
         { char c = S.charAt(i);
            
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
           
            else if(SrchChars.indexOf(c) >= 0)
               {if( brclevel == 0)
                   return i;
               }
            if( ( !quote ) && ( brcpairs != null ) )
              { j = brcpairs.indexOf(c);
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

