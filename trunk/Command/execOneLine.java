
/* 6-9-2000
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
   
*/
package Command;
import IsawGUI.Isaw.*;
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
import java.beans.*;
import java.util.*;
import java.util.zip.*;
/** 
*  This class parses and executes one line of code using the values of variables 
*  obtained from  the execution of previous lines of code
*/ 
public class execOneLine implements IObservable , 
                                    IObserver 
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
    public static final String ER_NoSuchOperator              = "Operation is not Allowed";
    public static final String ER_FunctionUndefined           = "Function is undefined";
    public static final String  ER_OutputOperationInvalid     =" Could not Save";
    public static final String ER_MissingQuote                = "Quotation mark missing";
    public static final String ER_MissingBracket              =" Unpaired brackets";
    public static final String ER_ImproperDataType            =" Improper Data Type";
    public static final String ER_ExtraArguments              ="Extra Arguments";
    private Document logDocument = null;

    private boolean Debug= true;

   


    //-----Run Space  Variables --------------
   DataSet   ds[],                                //Copy of the data set(s) passed in
	       ds1[];                              //   by the constructor
    
     
    Integer Ivals[]; 
    String Ivalnames[]; 

    Float Fvals[]; 
    String Fvalnames[];

    String Svals[]; 
    String Svalnames[];

    IObserverList  OL; 
    PropertyChangeSupport PC;
 
   
    DataSet lds[];                      //use getTitle for the  variable names
    Object Result;                     //Storage for Intermediate Results of
                                       //  operations

    Document Macr[];                  //Stores Macros
    String SMacrNames[];


    //Error variables

     int perror;                      //position of error on a line

     int lerror;                     //line number of error.                        

     String serror;                 // error message.
 
   
    

/** 
  
*/
    public execOneLine()
      {initt();
        OL = new IObserverList();
	  PC = new PropertyChangeSupport( this );
      }

/**
*This constructor adds the data set to the variable space

*/
    public execOneLine( DataSet Dat )
      { ds = new DataSet[1]; 
         ds[0] = eliminateSpaces( Dat );
	 initt();
         OL = new IObserverList();
         PC = new PropertyChangeSupport( this );
      }

/** 
 *This constructor adds the set of data sets to the  variable space.<P>
* The names of the variables are defined by their title.
*/
    public execOneLine( DataSet dss[] )
      { ds = dss;
          int i;
          for( i = 0 ; i < ds.length ; i++ )
	     ds[i] = eliminateSpaces( ds[i] );
	  initt();
          OL = new IObserverList();
          PC = new PropertyChangeSupport( this );
    
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
* This method allows outside data sets to be added to the variable space
*@param
*      dss    The data set to be added
*/
    public void addDataSet( DataSet dss )   
      { int i;
        ds1 = null;
        if(Debug)
	    System.out.print("in DataSet Add"+dss);
        if( ds == null )
	    {ds1 = new DataSet[ 1 ];
	     ds1[ 0 ] = dss;
	     i = 0 ;
             //System.out.print("XA");
            }
        else
          {ds1 = new DataSet[ ds.length + 1 ];
           ds1[ ds.length ] = dss;
           for( i = 0 ; i<ds.length ; i++ )
	       ds1[ i ] = ds[ i ];
           i = ds.length ;           //System.out.print("XB");
          }
        ds = ds1;
        ds1 = null;  
        ds [ i ].addIObserver(this);   
        //System.out.println("XD");
        if(Debug)
	    for(i = 0 ; i<ds.length ; i++)
                System.out.println( "AddDataSet: ds[i]=:"+ds[i]);
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
        if(  S.charAt( j1 ) == '[' )                // Check for []
	  {C = C +  brackSub( S , j1 , end );
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
          return execSave( S , j , end );
              
         }
       else if( C.equals( "SEND") )
	 {   if(start!=0)
               {seterror( i , "Must be the First command on a line" );
	        return i;
                }
	        if(Debug)
	          System.out.println("Send j1="+j1);	
	  return execSend(S,j1,end);
              
         }
        else if ( C.equals( "REM" ) )
	  {if( start == 0 ) return S.length(); }
           
	  
             if( Debug )
                System.out.println( "1C=" + C + ":" + (C=="(") ); 
       if( C.equals("(") )
         {
                if( Debug )
                   System.out.println( "in Lparen" + i );
          j = execute( S  ,  i + 1 , end );	   
             if( (j >= end) ||  (j >= S.length()) )
               {seterror( j , ER_MisMatchParens );
                return j;
               }
             if( S.charAt(j) != ')' )
               {seterror( j , ER_MisMatchParens );
                return j;
               }
          j = skipspaces( S , 1 , j + 1 );
          if( j > end ) j = end;
                if( Debug )
                  System.out.println( "in (j=" + j + " , " + i );
             if( j >= end )
               return j;
             if( j >= S.length() )
               return j;
          if( S.charAt(j) == ')' )
            return j; 
          if( S.charAt(j) == ':' ) 
	      return j;
          if( "+-*^/<>&".indexOf(S.charAt(j)) >= 0 )
            return execArithm( S , j , end );

          if( ",):]".indexOf(S.charAt(j)) >= 0 )
            return j;


	      seterror( j , "Improper symbol" + j );
              return j;
         }
    
       else if( i >= end )
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
             if( S.charAt(j1) == '=' )
		 {//System.out.println("C"); 
		 if( start != 0)  
		   {seterror( j1 , ER_IllegalCharacter);
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
                       System.out.println("kk=" + kk);
                  if( ( kk  <= j1 + 1) ||  (perror >= 0) )
	             {return S.length() + 1;
                     }
                  else 
                    return skipspaces( S , 1 , kk );
                 }
       
	   j = execOneFactor( S , i , end );
	     if( perror >= 0 )
              return perror;

	  Object R1 = Result;
	  
                if( Debug )
                  System.out.println( "Aft operate Result=" + Result+","+ Result.getClass() );
	  j = skipspaces( S , 1 , j );
          if( j > end) j = end;
	  if( (j < 0) ||  (j >= S.length())|| ( j >= end) )
	       return j;

	  if( "),:]".indexOf( S.charAt( j ) ) >= 0 )
            return j;

	  if( "+-*^/<>&".indexOf( S.charAt( j )) >= 0 )
            return execArithm( S , j , end );

	     seterror( j , ER_IllegalCharacter );
	     return j;
           }
               
     
     }
    
	   
    
//             Executes the LOAD command.
// Brings the data sets into the local workspac
    private int  execLoad( String S , int start, int end )
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
          if( V.get( 0 ) instanceof DataSet[] )
            {  if(Debug)  System.out.println("Load in Dataset[]");
              DataSet DS[] = (DataSet [ ] ) V.get( 0 );
              x = 1;
              varname = null;
              if( V.size( ) > 2 )
                 varname = (String ) V.get ( 1 );
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
          filename = (String) V.get(0);
          x = 2;
          varname=null;
          if( V.size() == 3)
             varname = (String) V.get(1);
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
       if( dss == null )
         Result = null;
       else
           Result = new Integer( dss.length );
       return j;
    /*
       // Get First Argument      
       i = start;
       i = skipspaces( S , 1 , i );
          if( (i >= end) ||  (i >= S.length()) )
	    {seterror( i , ER_MissingArgument );
	     return -1;
            }
          if( i < 0 )
            {perror = start;
             serror = "internal Error 1";
             return -1;
            }
    
       if( S.charAt( i ) == '(' )
	 j = execute( S , i + 1 , end );
       else
         j = execute( S , i , end );

          if( perror >= 0 )
	    {   if( Debug )
                  System.out.println("in execLoad exec error,i,j=" + i + "," + j+","+S.length());
	     return perror;
            }
       
            if( Debug )
               System.out.print("ExecLoad/Exec=OK" + i + "," + j+","+Result);
     
       
          if( Result == null )
	    {
             seterror( i , ER_ImproperArgument );
	     return  j;
            }
          if( !( Result  instanceof String ) )
	    {seterror( i , ER_ImproperArgument );          
	     return j;
             }
       filename = (String)Result;

       //Get 2nd argument if possible    
       j=skipspaces( S , 1 , j   );
       String varname=null;
             if(Debug)
	         System.out.println("ere get varname j= "+j+","+S.length());
       if( (j<S.length()) && ( j < end ))
         if(j>=0)
	   if ( S.charAt( j ) == ',')
	       {       if(Debug)
                          System.out.println("XXXXXXXXX"+j+","+S.charAt(j));
               j=execute( S , j + 1, end );
	          if( perror >= 0) return perror;
                  if( !(Result instanceof String)) return j;
               j = skipspaces( S , 1 , j );
               varname = (String)Result;
	     }

      
       if( S.charAt(i) == '(' )
         {   if( (j >= end) ||  (j >= S.length()) )
	       {seterror( i , ER_MisMatchParens );             
	        return i;
               }
	     if( S.charAt(j) != ')' )
               {seterror( i , ER_MisMatchParens );              
	        return j;
               }
          j = skipspaces( S , 1 , j + 1 );
         if( j > end) j = end;

          } 
      
     
             if(Debug)
	         System.out.println("in mid Load varname="+varname);

       //Execute low level Load

       dss = new IsawGUI.Util().loadRunfile( filename  );
       if( dss == null )
        {seterror( start , "Data File Improper" );
         return -1;
        }
       if( dss.length <= 0 )
         {seterror( start , "Data File Improper" );
          return  -1;
         }
       int j1 = j;
       DataSet DDs;
  
       for( i = 0 ; i < dss.length ; i++ )
         {DDs = eliminateSpaces( dss[i] );
	 dss[i].addOperator( new GetDSAttribute());
         dss[i].addOperator(new SetDSAttribute());
         dss[i].addOperator( new GetDataAttribute());
         dss[i].addOperator(new SetDataAttribute());
        dss[i].addOperator(new SetField());
         dss[i].addOperator(new GetField());
        
	 if( varname != null)
	    if( varname.length()>0)
		if( varname.toUpperCase().charAt(0) >'Z')
		    {}
                else if( varname.toUpperCase().charAt(0) < 'A')
                    {}
                else DDs.setTitle(varname + new Integer(i).toString().trim());
          j = findd( DDs.getTitle() , ds );         
          if( isInListDS ( j , ds ) )
            {seterror( start , "DataFile already loadedA" );
             return -1;
            }
          j = findd( DDs.getTitle() , lds );         
          if( j < 0 )
            {
            }                     
          else if( j >= lds.length )
            {
            }
          //else if( lds[j] != null )
          else if( isInListDS( j , lds ) )
            {seterror( start , "DataFile already loadedB" );
             return -1;
            }
          Assign( DDs.getTitle() , DDs);
          if( perror >= 0) perror = S.length();
          if( Debug )
            System.out.println("Assign Dat set=" + DDs.getTitle());
         }
       if( start > 0 )
          Result = new Integer( dss.length );
       else
         Result = null;
  */    
     
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
        for( i = 0 ; i < dss.length ; i++ )
         {DDs = eliminateSpaces( dss[i] );
	  if( varname != null)
	    if( varname.length() > 0 )
		if( varname.toUpperCase().charAt(0) >'Z')
		    {}
                else if( varname.toUpperCase().charAt(0) < 'A')
                    {}
                else DDs.setTitle(varname + new Integer(i).toString().trim());
            Object X = getVal( DDs.getTitle());
    
          if( X != null )
            {seterror( 1000 , "DataFile already loadedX" );
             return;
            }
     
          Assign( DDs.getTitle() , DDs);
        }
           
        
         
    
    

    }

/**
* Used by other parsers to load a file of data sets into the local space
*@param       filename   The name of the file of data sets
*@param       varname    The name these data sets will be referred to.
*                        They will be called varname0, varname1, ...  
*
*@return     The number of data sets loaded
* Note: if varname is null, the names will be derived from the filename and data set type.
*/
    public DataSet[] Load( String filename , String varname )
    {   DataSet DDs,
                dss[];
        int i,
            j;
  
       dss = new IsawGUI.Util().loadRunfile( filename );
       if( dss == null )
        {seterror( 1000 , "Data File Improper" );
         return null;
        }
       if( dss.length <= 0 )
         {seterror( 1000 , "Data File Improper" );
          return  null;
         }
        for( i = 0 ; i < dss.length ; i++ )
         {DDs = eliminateSpaces( dss[i] );
	  if( varname != null)
	    if( varname.length() > 0 )
		if( varname.toUpperCase().charAt(0) >'Z')
		    {}
                else if( varname.toUpperCase().charAt(0) < 'A')
                    {}
                else DDs.setTitle(varname + new Integer(i).toString().trim());
          j = findd( DDs.getTitle() , ds );         
          if( j >= 0 )
            {seterror( 8 , "DataFile already loadedC" );
             return null;
            }
          j = findd( DDs.getTitle() , lds );         
          if( j < 0 )
            {
            }                     
          else if( j >= lds.length )
            {
            }
          //else if( lds[j] != null )
          else if( isInListDS( j , lds ) )
            {seterror( 0 , "DataFile already loaded" );
             return null;
            }
          Assign( DDs.getTitle() , DDs);
         
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

/*
      if( (i < 0) ||  (i >= end) ||  (i >= S.length()) )
        {seterror( i , ER_MissingArgument);
         return i;
        }
      if( S.charAt(i) == '(' )
        j = execute( S , i + 1 , end);
      else
        j = execute( S , i , end );

      if( perror >= 0 )
        {if( perror > S.length() )
           perror = i;       
         return i;
        }

      if( S.charAt( i ) == '(' )
        {if( (j >= end) ||  (j >= S.length()) )
	   {seterror( i , ER_MisMatchParens );
	    return i;
           }
         if( S.charAt(j) != ')' )
           {seterror( i , ER_MisMatchParens );
	    return i;
           }
        }
*/
       i = start;
      
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
      else if( V.size() > 4)
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
      try{
          

          x = 1;

          DS = (DataSet) (V.get(0));
          x = 2;
          DisplayType="IMAGE";
          if( V.size() >2)
             DisplayType = (String)(V.get(1));
          x=3;
          FrameType ="External Frame";
          if( V.size() >3 )
            FrameType = (String) (V.get(2));
          x = 4;
          Display( DS , DisplayType , FrameType );
          System.out.println("Return from Display1");
          if( perror >= 0 ) 
             perror = start;
          return j;
         }
     catch( ClassCastException s)
        { if( x > 1)
          if( (Result == null) || (V.size() > 2 ) ) 
            {seterror( i , ER_ImproperArgument+" " + x  );
             if( x-1 < V.size())
               System.out.println( "V and class = "+ V.get(i) +","+V.get(i).getClass());
             else System.out.println("Aft Display args ="+ DisplayType+","+FrameType);
             return i;
            }
        }
      Result = (Object) V.get(0);
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
	  SS = "Result= ";
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

/**
 * Creates a viewer for the data set
 *@param      ds            The data set to be viewed
 *@param      DisplayType   The type of display
 *
 * NOTE: DisplayType must be "IMAGE" , "Scrolled_Graph", or "Selected_Graph"<Br>
 *       FrameType must be "External Frame" or "Internal Frame"
 */
    public void Display( DataSet ds , String DisplayType , String FrameType )
      {  String X = null;
         if( Debug) System.out.println( "IN DISPLAY1 args="+DisplayType +","+FrameType);
         if( DisplayType.toUpperCase().equals("IMAGE")) X = IViewManager.IMAGE;
         else if( DisplayType.toUpperCase().equals("SCROLLED_GRAPH")) X = IViewManager.SCROLLED_GRAPHS;
         else if( DisplayType.toUpperCase().equals("SELECTED_GRAPH")) X = IViewManager.SELECTED_GRAPHS;
         else
           { seterror( 1000 , ER_ImproperArgument+" "+ DisplayType );
             return;
           }
         new JDataViewUI().ShowDataSet( ds , FrameType, X );
      }

    private int  execSave( String S , int start, int end )
      { 
	  /*if( ( start < 0) || (start >= S.length()  ) || ( start >= end) )
	     {seterror(S.length(),"internal Errory");
	      return S.length();
	     }
          
          int j=skipspaces( S, 1 , start );
          if( (j<0) || ( j >= S.length() )|| ( j >= end) )
	    { seterror( j ,  ER_MissingArgument);
	      return j;
            }
	  if( S.charAt( j ) == '(' )
            j=execute( S , j + 1 , end );
          else
	    j = execute ( S , j , end);
          if( perror >= 0 ) return j;
          if( !( Result instanceof DataSet) )
	      { seterror( j, ER_ImproperArgument);
	        return j;
	      } 
          DataSet Ds = (DataSet) Result;
          j = skipspaces( S , 1 ,j);
          if( (j >= end ) || (j >= S.length() ) )
	    {}
          else if ( j < 0)
	    { seterror ( S.length() + 1 , "Internal Error {" );
	      return S.length() + 1 ; 
	    }
          else if( S.charAt( j ) != ',' )
	    { seterror( j, ER_MissingArgument);
	      return j;
	    }
          j = execute( S , j + 1, end );
          if( perror >= 0)
	    return perror;
          if( !( Result instanceof String) )
	    { seterror ( j , ER_ImproperArgument);
	      return j;
            }
         */
       
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

          DS = (DataSet) (V.get(0));
          x = 2;
          filename = null;
          if( V.size() >2)
             filename = (String)(V.get(1));
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
 *@param      filename  The filename to which is to be saved

  Needs work.  All IObservers of the data set will also be saved
  presently
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

    

    private int execSend(String S, int start, int end)
      {int i, j;
       i = skipspaces( S , 1 , start );
       if( (i < 0) || ( i >= S.length()) || ( i >= end ))
         { seterror( start , ER_MissingArgument);
           return i;
         }
       if( S.charAt(i) == '(' )
          j = execute( S , i + 1 , end );
       else
          j = execute( S , i  , end );
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
       if( !( Result instanceof DataSet ) )
         {if(Debug)
	   System.out.println("in Not Correct Data Type");
          seterror( start , ER_ImproperArgument );
          return start;
         }
       if(Debug)
	   System.out.println("Send er observ");
       ((DataSet)Result).addIObserver(this);
       OL.notifyIObservers( this , Result );
       Result = null;
       return end;
      }

 /**
   Sends the data set to all Iobservers
  *@param   ds    The data set that is to be sent
  *@see  DataSetTools.util.IObserverList#addIObserver(DataSetTools.util.IObserver) addIobserver<P>
  *
  * Not implemented yet. 
  */
    public void Send( DataSet ds)
      {
      }

/**
   Returns the Result of the last expression <P>
   Statements "should" return null<P>
   This routines allow execute to be used on expressions and not just statements
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
	 System.out.println("ds is null");
        }
      return ds;
    }

   
// Executes an argument list.  Done when at end of string or a )
// Assumes 1st character is not a left paren for the argument list
    //    public int execArgs(String S,int start,int end)
    //     {int i;
    //     i=execute(S,start,end);
    //    return end;    
    //    }
    //Result contains the first operand.  Start has the operation
    private   int execArithm(String S, int start, int end) 
      {  int i,i1;
         int j;
         Object R1,R2;
         boolean done;
         R1 = Result;
               if( Debug )
                  System.out.println("in ExAr" + start);
            if( start < 0 )
	      {seterror( S.length() + 5 , "internal error" + start );
	       return S.length() + 5;
	      }
            if( (start >= S.length()) || (start >= end) )
              {seterror( S.length() + 5 , "internal error" + start );
	       return S.length() + 5;
	      }
            if( "+-*/&^<>".indexOf( S.charAt( start ) ) < 0 )
              {seterror( start , "internal error" + start );
	       return S.length() + 5;
	      }
         i1 = start;
         j = skipspaces( S , 1, i1);
         j = execAllFactors( S , j , end );
            if( perror >= 0 )
	      {
	       if( perror > S.length() )
                 perror = start;
	      return j;
	      }
             
         R1 = Result;
         i = skipspaces( S , 1 , j );
         if( i > end) i = end;
         done = false;
         if( (i >= end) ||  (i >= S.length()) ||  (i < 0) )
           done = true;
         else if( "),]:".indexOf( S.charAt( i ) ) >= 0 )
	   done = true;
         while( !done )
           {j = execOneTerm( S , i + 1 , end );
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
           else if( "),]:".indexOf( S.charAt( i ) ) >= 0 )
	     done = true;
           }//While !done      


      return i;
      }
    //S[start] is operation Result is previous
    private int execAllFactors( String S , int start , int end )
      { Object R1;
        int j;
        if( Debug )
          System.out.println("ExAr,st=" + start);
        boolean done;
       
        int i = skipspaces( S , 1 , start );
          if( ( i < 0 ) ||  ( i >= S.length() ) || ( i >= end) )
            {seterror( start , ER_MissingArgument +"A");
             return start;
            }
        if( "*/^".indexOf( S.charAt( i ) ) < 0 )
          return i;
        R1 = Result;      
        j = execOneFactor( S , i + 1 , end );
        j = skipspaces( S , 1 , j );
      
          if( perror >= 0 )
	    {
             if( perror > S.length() )
               perror = start;
             return perror;
            }  
        if( (j >= S.length()) || ( j >= end ) )
          {
           operateArith( R1 , Result , S.charAt( i ) );
           if( perror >= S.length() )
             perror = j;        
           return j;
          }
           if( j < 0 )
             { 
              seterror( i + 1 , "internal Error3" );
               return i;
             }
        if( "),+-:]<>".indexOf( S.charAt( j  )) >= 0 )
          {          
            operateArith( R1 , Result , S.charAt( i ) );
               if( perror >= S.length() )
                 perror = j;
          
	    return j;        
          }
        if( "*/^".indexOf( S.charAt( j ) ) < 0 )
          {seterror( j , ER_IllegalCharacter );       
           return j;
           }
       if( S.charAt( i )!= '^')
         operateArith( R1, Result , S.charAt (i));
       else
	 { int k = execOneFactor( S, i+1, end);
	   operateArith( R1, Result , S.charAt(i));
           j = k;
         }
          if( perror >= S.length()) perror = j;
          if (perror >= 0) return perror;

        Object R2;
        R2 = Result;

        done = false;
        i = j;
        //System.out.println("execAll got here"+i+","+S.length());
        while( !done )
          { 
            j = execOneFactor( S , i + 1 , end ); 
            j = skipspaces( S , 1 , j );     
            if( perror < 0 ) 
	      {operateArith( R2 , Result , S.charAt( i ) );
	      if( perror >= 0) 
                 {perror = i; 
                  return i;
                 }
	       R2 = Result;
	      }
            else
	     {return perror;
             }
        
        i = skipspaces( S , 1 , j );
        if( ( i >= end ) ||  ( i >= S.length() ) ||  ( i < 0 ) ) 
          done = true;
        else if( "+-&),:]<>".indexOf( S.charAt( i ) ) >= 0 )
	  done = true;
        else if( "*/".indexOf( S.charAt( j ) ) < 0 )
	  {seterror( j , ER_IllegalCharacter );
	   return j;
	  }
        else
          done = false;
       }//while !done
    
        return j;
      }

    //start is start of a new Factor
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
          return skipspaces( S , 1 , j + 1 );
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
         else if( (j< S.length()) && ( j < end))
	   if(S.charAt(j) == '\"')
	     { seterror( j , ER_IllegalCharacter);
	       return j;
	     } 
       
	     
      String C;
      C = S.substring( i , j );
      
      if ( (j1 >= 0) && (j1 < S.length()) && ( j1 < end ) )
         if(  S.charAt( j1 ) == '[' )                // Check for []
	  {C = C +  brackSub( S , j1 , end );
	  
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
           
          }

 
    

      if( (j1 < S.length()) && ( j1 < end ) )
        if( S.charAt( j1 ) == '(' )//function
          {
           return execOperation( C , S , j , end );
          }     
	else if( "([{}".indexOf( S.charAt( j1 ) ) >= 0 )
	  {seterror( j , ER_IllegalCharacter );
	   return j;
	  }
      boolean valgot = false ;
      try{
	  Result = new Integer( C );
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
	 {seterror( S.length() + 2 , "internal errorp" );
	 return null;
	 }
       if( Debug)
         System.out.println( "get args ,start= "+ start);
       Vector Args = new Vector();
       int i , j;
       boolean done;
       i = skipspaces( S , 1 ,start) ;
       done = false;
       while( !done )
         {j = execute( S , i , end );
         if( perror >= 0 )
           return null;
         Args.add( Result );
         if( ( j >= S.length() ) || ( j >= end ) )
	   {Args.add(new Integer(j));
	    return Args;
	   }
         if( j < 0 )
	   {seterror( S.length() + 3 , "InternalerrorG" );
	    return null;
	   }
         if( S.charAt( j ) == ')' )
	   done = true;
         else if( S.charAt( j ) != ',' )
	   {seterror( j , ER_IllegalCharacter );
	    return null;
	   }
         if( S.charAt(j) == ',')
            i = skipspaces(S , 1, j+1 );
         else
            i = j;
         }//while not done
      Args.add(new Integer( i ));
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
       i = skipspaces( S , 1 , i );
 
          if( perror >= 0 )
	    {return perror;
            }
       R1 = Result;
       boolean done = (i < 0) ||  (i >= S.length()) ||  (i >= end);
       if( !done )
	 if( "+-)<>,:]".indexOf( S.charAt( i ) ) >= 0 )
           done = true;
       while( !done )
         { 
           j = execOneFactor( S , i + 1 , end );
           j = skipspaces( S , 1 , j );
	   if( perror >= 0 )
	     {return perror;
             }
	   operateArith( R1 , Result , S.charAt( i ) );
           if( perror >= 0 ) { perror = i; return perror;}
	   i = j;
	   done = (i  < 0) ||  (i >= S.length()) ||  (i > end);
           if( !done )
	     if( "+-)<>,:]".indexOf( S.charAt( i ) ) >= 0 )
               done = true;
	   R1 = Result;
         }
       return i;

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
        if( R1 instanceof DataSet )
          {operateArithDS( R1 , R2 , c );
	   return;
	  }
	if( R2 instanceof DataSet )
          {operateArithDS( R1 , R2 , c );
	   return;
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
	      {seterror( 1000 , ER_IllegalCharacter );
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
      Args.add( DS );
      Args.add( Arg2 );
      Args.add(new Boolean (true));
      
      DoDataSetOperation( Args , Arg );
    
      }
/**
 * Find and Executes an  operation from lists of operations 
 * @param   Args     The vector of argument values
 * @param   Command  The command to be executed
 *@return
 *    The value in the variable Result
 *    An error if the operation is not defined or does not work
 */
    public void DoOperation( Vector Args, String Command )
      { if(Debug)
          System.out.println("Start DoOperation comm =" + Command);
        Operator op = (new GenericOperatorList()).getOperator( Command );
        if( op == null )
          { if(Debug)
              System.out.print("A");
            DoDataSetOperation( Args, Command  );
            return;
          }
        int i;
        if(Debug)
              System.out.print("B");
        if( !checkArgs( Args, op , 0 ))
          {  if(Debug)
              System.out.print("C");
            DoDataSetOperation( Args, Command  );
            return;
          }        
        
        SetOpParameters( op , Args , 0);
        Result = op.getResult();
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
         { System.out.println("Check no op");
           return false;
          }
       if( Args == null )
         if( op.getNum_parameters() != 0 )
           return false;
         else
           return true;
       if(Debug)
         System.out.println("Check sizes = "+ Args.size() +","+ op.getNum_parameters());
       if( op.getNum_parameters() !=  Args.size() -start )
         return false;
       for( k = 0; (k < op.getNum_parameters()) && fit ; k++ )          
         { 
           Arg2 = Args.get( k +start );
           //if( Debug)
	   // System.out.print("Check"+Arg2.getClass()+","+op.getParameter(k).getValue().getClass());
           if( op.getParameter(k).getValue() == null)
              {if(Debug) System.out.print("H");                         
               }
                
	    else if( ( Arg2 instanceof String ) && 
                          ( op.getParameter(k).getValue() instanceof AttributeNameString ) )
	      {if(Debug)System.out.print("D");
	      }
                 
            else if( Arg2.getClass().equals( op.getParameter( k ).getValue().getClass() ))
	      {
		 if( Debug ) System.out.print("E");
              }
                       
	    else 
               fit = false;
	    if(Debug)System.out.println("F");                    
					
         }//For k
        return fit;
     }
    private void SetOpParameters ( Operator op , Vector Args , int start )
     {int k;
      for( k = 0 ; k < op.getNum_parameters() ; k++ )
        {if( (Args.get( k + start ) instanceof String)  &&  
             (op.getParameter( k ).getValue( ) instanceof AttributeNameString) )
            op.getParameter( k ).setValue( new AttributeNameString( (String)( Args.get(k + start) ) ));
          else
            op.getParameter( k ).setValue( Args.get( k + start ) );
        }
     }
    private void DoDataSetOperation( Vector Args , String Command )
      {
       int i,k;
       Operator op;
       DataSet DS;
       Object Arg2;
       boolean fit;
       if( !( Args.get(0)instanceof DataSet ) )
	 {seterror( 1000 , ER_NoSuchOperator  );
	  return;
	 }
       DS = (DataSet)Args.get(0);
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
               
		  if(Debug)System.out.println("F");                    
					
                
	       if(Debug)System.out.println("GG");
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
	 {seterror( S.length() + 2 , "internal errorp" );
	 return start;
	 }
       if( S.charAt( start ) != '(' )
         {seterror( S.length() + 2 , "internal errorp" );
	  return start;
         }
       if( Debug)
         System.out.println( "In execOperation comm ="+Command);
       Vector Args = new Vector();
       int i , j;
       boolean done;
       i = start;
       done = false;
       while( !done )
         {j = execute( S , i + 1 , end );
         if( perror >= 0 )
           return perror;
         Args.add( Result );
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
	   {seterror( j , ER_IllegalCharacter );
	    return j;
	   }
         i = skipspaces(S , 1,j);
         }//while not done

       j = skipspaces( S , 1 , i + 1 );;
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
            System.out.print(Args.get(i2) + "," + Args.get(i2).getClass() + ",");
          System.out.println("");
          }
	/*   i2 = 0;
        String Commd = null;
       for( i1 = SS.indexOf(';') ; ( i1 < SS.length() ) && ( Commd == null ) ;   )
         {
           k1 = SS.indexOf( ';' , i1 + 1 );
         k2 = SSI.indexOf( ";" , i2 + 1 );
         if( SS.substring( i1 , k1 + 1 ).toUpperCase().equals(";" + Command.toUpperCase() + ";") )
           {Commd = SSI.substring( i2 + 1 , k2 );
           }
          i1 = k1;
         i2 = k2;
         };
	*/
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

  /** 
   *  Returns the value of the variable S
   *@param  S   A string used to refer to a variable 
   *@return  The value of this string or
   *         an error message if the variable is not found
   *  @see #getErrorCharPos()
   */
     public Object getVal( String S )
       {int i;
       //System.out.println("getVal");
        i = findd( S.toUpperCase() , Ivalnames );
        if( isInList( i , Ivalnames ) )
	  {if( i == 0 ) return new Boolean(false);
	   if( i == 1 )  return new Boolean(true);
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
        i = findd( S , lds );
        if( isInListDS( i ,lds ) )
          {return lds[ i ];
          }

        i=findd(S,ds);
        if( isInListDS( i , ds ) )
	  {return ds[ i ];
          }
        seterror( 1000 , ER_NoSuchVariable );
        return null;
     
      }
//Finds matching quote and returns unquoted string
//  Does not test for backslash stuff
     private String getString( String S ,  int start )
       {if( ( start < 0 ) ||  ( start + 1 >= S.length() ) )
          {seterror( S.length() + 2 , ER_IllegalCharacter );
           return null;
          }
        if( S.charAt( start ) != '\"' )
          {seterror( S.length() + 2 ,  ER_IllegalCharacter );
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
       DataSet ds;

       ds = ds1;
       ds.getTitle().trim();
       for( j = ds.getTitle().indexOf(' ') ; ( j >= 0 ) && ( j < ds.getTitle().length() ) ; 
                          j = ds.getTitle().indexOf(' '))
	 {ds.setTitle( ds.getTitle().substring( 0 , j ) + ds.getTitle().substring( j + 1 ) );
	 }
	
       return ds;
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
         Ivals = new Integer[4];  
         Ivalnames = new String[4]; 
         Ivals[0] = new Integer( 0 ); 
         Ivals[1] = new Integer ( -1 );
         Ivals[2] = new Integer ( 0 );
         Ivalnames[0] = "FALSE";
         Ivalnames[1] = "TRUE";
         Ivalnames[2] = null;
        

         Fvals = null;  
         Fvalnames = null;
         lds = null;                         // May need an Empty data set somewhere but where?
         //lds = new DataSet[10];
         //lds[ 0 ] = new DataSet("EMPTY", ""); 
         //lds[ 1 ]= null;
         perror = -1;
         serror = "";
         lerror = -1;
         Result=null;
      }


     private  void seterror( int charnum, String errorMessage)
       {
        if(charnum < 0)return ;      
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
         System.out.print("findfrst start, S=" + start + "." + S);
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
         {
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

          i += dir;

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
    private boolean isInListDS( int i , DataSet Llist[] )
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
		          DataSet Llist[])
      {int i;
      //System.out.println("in finddDS"+SearchName);
       if( Llist == null )
         return -1;
       for( i = 0 ; i < Llist.length ; i++ )
	   {//System.out.print("i, title="+i);
          if( Llist[i] == null )
            return i;
          //System.out.println( Llist[i].getTitle());
	  if( Llist[i].getTitle().toUpperCase().equals( SearchName.toUpperCase() ) )
            return i;
	 }
       return i;
      }
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
           else if( SearchList[i] instanceof DataSet )
             {if( ( ( DataSet )SearchList[i]).getTitle().toUpperCase().equals( SearchName.toUpperCase() ) )
                return i;
             }
           else
            {if( ( ((String)SearchList[i]).toUpperCase() ).equals( SearchName.toUpperCase() ) )
               return i;
            }
         }
       if( Debug )
         System.out.print( "not findd" );
       return i;
      }



    private void Assign(String vname,
                       Object Result)
      {int   i,
             j;
      boolean found = true ;
      Object X = getVal( vname );
      if( (perror >= 0) && ( serror.equals( ER_NoSuchVariable)))
	 {perror = -1;
	  serror = "";
          found = false;
         }

       if( Result instanceof Integer )  //what about array of integers??
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
         {i = findd( vname , ds );       
          if( ds != null )
            if( ( i >= 0 ) && ( i < ds.length ) )
		{//DSPAssign( ds[i] , (DataSet)Result );
		  ds[ i ].copy((DataSet)Result);   //= (DataSet)((DataSet)Result).clone();;
                 return;
              }
          i = findd( vname , lds );
  
          if( lds == null )
            {lds = new DataSet[ 10 ]; 
             i = 0;
             lds[ 0 ] = null;
            }

          else if( i >= lds.length )
            {DataSet IName[];
             IName = new DataSet[ lds.length + 10 ];
                             
             for( j = 0 ; j < lds.length ; j++ )
	       {IName[ j ] = lds[ j ];
               }
             IName[ lds.length ] = null;
             i = lds.length;
             lds = IName;
             lds[ i ] = null;
            }

          if( Debug )
            System.out.print("Assign2 i=" + i + "," + lds.length);
       
          if( lds[i] == null )
	    {
              lds[ i ] = (DataSet)((DataSet)Result).clone();
              lds[ i ].setTitle( vname );
              if( i + 1 < lds.length )
                lds[ i + 1 ] = null;
             }
          else 
            lds[ i ] = (DataSet)((DataSet)Result).clone();
 
         };
      }//end Assign


    

     public void update( Object observed_obj , Object reason )
       {
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
       {PC.addPropertyChangeListener( listener );
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
     *@param 
     *     listener      The listener who wants to be notified of a non Data Set
     *                   "Display" value
     * @param    PropertyName   Must be Display
     */
     public void addPropertyChangeListener( String propertyName,
                                            PropertyChangeListener listener)
      {PC.addPropertyChangeListener( propertyName , listener );
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
       {PC.removePropertyChangeListener( propertyName , listener );
       }

    
}
