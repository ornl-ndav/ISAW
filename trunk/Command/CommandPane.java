/* 6/4/2000:
   a)Implemented GUI Interface to project
       i)Improve: Titles should not dissapear when scrolling
       ii) Save Button: Fix so the Dialog box says Save instead of Open
       iii) Improve documntation initiated by the Help Button
   b) Parser just started
         Load a string constant and Display a Dataset Variable are all that
         are implemented

   6/15/2000
    c) For - End FOR and On Error-Else Error -End Error implemented

    d) Rem and blank lines caught

    e) Help button HTML documents passable.  Need to update for current version.

    f) var[0] --> var0  and [I] can be inbetween brackets

    g) The Batch facility has been implemented and tested

    h)  send and get a data set have been implemented and tested

    i) The save facility should use Dennis' save.(elim IObservers before th Object save
   To Do

    1) Load one Isaw Data set

    2) plan and implement dataset.Title 

  7/14/2000
    1) If- else- elseif- endif implemented

    2) GetDSAttr, GetDataAttr, GetField, and appropriate Sets have been 
       implemented in the DataSetTools.operator.

 7/28/00
   1) Parameters and Macros with parameters have been implemented.
      Interfaces so a GUI gets a value of the parameters is available.
      Macros have not been implemented as an instruction yet.

   2) This class contributes to the Isaw session log.   

9-14-2000
   -Fixed on error, end-error structure with nesting
   -Fixed if-then error.  The "if" need not be in column 1
   -Eliminated a debug print when not in debug mode
   -if  and elseif statements use the new Boolean type

10-1-2000
   - Implemented starting character $ for parameters in addition to #$$

12-1-2000
   - Fixed IsawHelp search path
   - Added support for the parameter data types 
     DSFieldString, DSSettableFieldString in addition to the other supported data types
     InstrumentNameString and DataDirectoryString
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
import java.beans.*; 
import java.util.Vector;
import Command.*;

/** Pane to enter and execute and handle commands
 *  A command can be executed immediately  or 
 *  A sequence of commands can all be executed.
 *      The Immediate commands are entered in the immediate window
 *      The sequence of commands are entered in the Editor window.
 *  The commands can act on Isaw Data Sets and will be extended to include
 *  most of the commands availabe in the GUI.
 */
public class CommandPane  extends JPanel 
                          implements  //KeyListener , 
                                      // ActionListener , 
				     PropertyChangeListener , 
                                      IObservable ,  IObserver
{    
  
 //***********************SECTION GUI****************
  JButton Run , 
          Open , 
          Save , 
          Help , 
          Clear ; 

    JTextArea  Commands , 
               Immediate ; 

    JTextArea    StatusLine ; 
  
    Command.execOneLine ExecLine ; 
    
    int lerror ,                                //line number of error. 
        perror ;                                //position on line of error
    String serror ;                             //error message

   
    IObserverList OL ; 
    PropertyChangeSupport PL;
    String FilePath = null  ;                   // for macro storage and retrieval
    File SelectedFile = null;
    Document logDocument = null;

    private boolean Debug = false;
    private Document MacroDocument = null;

    /** Constructor with Visual Editor and no data sets from outside this program
     *  This can be used with a stand alone editor
     */
    public CommandPane()
    {initialize() ; 
    ExecLine = new Command.execOneLine() ; 
    
    init() ; 
    OL = new IObserverList() ; 
    PL = new PropertyChangeSupport( this );  
    }
   


    /** Constructor with Visual Editor and one data sets from outside this program
     *  This can be used with the Isaw package
     *    @param    Dat        One data set
     */
    public CommandPane( DataSet Dat , String vname)
    {initialize() ;  
    ExecLine = new Command.execOneLine( Dat , vname ) ; 
    init() ; 
    OL = new IObserverList() ; 
      PL = new PropertyChangeSupport( this );  
   }

    /** Constructor with Visual Editor and a set of data set from outside this program
     *
     *    @param    dss[]        A list of data sets
     */
    public CommandPane( DataSet dss[], String vname )
    {
       initialize() ; 
     ExecLine = new Command.execOneLine( dss ,vname) ; 
     init() ; 
     OL = new IObserverList() ; 
       PL = new PropertyChangeSupport( this );  
    }



    /** Constructor with no Visual Editing Element.  
     *  This form could be used for Batch files
     *
     *    @param   TextFileName     The name of the text file containg commands
     */
    public CommandPane( String TextFileName )
      {int c ;       
       Document doc ; 
       initialize() ; 
       File f ; 
       ExecLine = new Command.execOneLine() ; 
       OL = new IObserverList() ;
       PL = new PropertyChangeSupport( this );   
      
        doc =new Util().openDoc( TextFileName);
      
       execute( doc ) ; 
    }



    /** Constructor that can be used to run Macros- non visual?
     *
     *    @param  Doc      The Document that has macro commands
     *    @param  O        Observer for the Send command
     */
    public CommandPane( Document Doc , IObserver O )
      {OL = new IObserverList() ; 
       initialize();
       ExecLine = new Command.execOneLine() ;
       addIObserver( O );
       PL = new PropertyChangeSupport( this );  
       MacroDocument = Doc ;
       ExecLine.initt();
       ExecLine.resetError();
       seterror( -1,"");
       lerror = -1;
        
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

    private void initialize()
    {
      Run  =  null ;   
     Open = Save = Help = Clear = null ;  
     Commands = null ; 
     try{
        //System.setProperty( "Scriptpath", "C:\\Ruth\\ISAW\\Scripts\\");
        //System.setProperty("DataDirectory" , "C:\\Ruth\\ISAW\\SampleRuns\\");
         //System.setProperty("DefaultInstrument" , "hrcs");
     
       FilePath = System.getProperty("Script_Path");
      
       
            FilePath = DataSetTools.util.StringUtil.fixSeparator(FilePath);
     
     
        if( Debug )System.out.println( "FilePath is "+FilePath);
   
        }
     catch( Exception s) 
      {FilePath = null;
       if( Debug ) System.out.println(" System properties could not be set");
      }
     
    
    
    }

    /** Initializes the Visual Editor Elements
     *
     */
    private void init()
    {JPanel JP ; 
       
        Rectangle R = getBounds() ; 
        
      
        Run = new JButton( "Run Prgm" ) ; 
       
        Open = new JButton( "Open Prgm" ) ; 
        Save = new JButton( "Save Prgm" ) ; 
        Help = new JButton( "Help" ) ; 
        Clear = new JButton("Clear");
        Run.addActionListener( new MyMouseListener() ) ; 
        Open.addActionListener( new MyMouseListener() ) ; 
        Save.addActionListener(new MyMouseListener() ) ; 
        Help.addActionListener( new MyMouseListener() ) ; 
        Clear.addActionListener( new MyMouseListener() ) ; 

        setLayout( new BorderLayout() ) ; 
           JP = new JPanel() ; 
           JP.setLayout( new GridLayout( 1 , 5 ) ) ; 
         
           JP.add( Run ) ; 
           JP.add( new JLabel( "       " ) ) ; 
         
           JP.add( Open ) ; 
       
           JP.add( Save ) ; 
           JP.add( Clear );
           JP.add( Help ) ; 
        
	
        add( JP , BorderLayout.NORTH ) ; 
     
	

           Commands = new JTextArea( 7 , 50 ) ; 
           Commands.setLineWrap( true ) ; 
           Commands.setFont(FontUtil.MONO_FONT ) ;
           Immediate = new JTextArea( 5 , 50 ) ; 
           Immediate .setFont(FontUtil.MONO_FONT ) ;
           Immediate.addKeyListener( new MyKeyListener(this)) ;        
	 
           JSplitPane JPS = new JSplitPane(JSplitPane.VERTICAL_SPLIT) ; 
           //JPS.setResizeWeight( .8);
           JScrollPane X =  new JScrollPane( Commands ) ; 
           X.setBorder( new TitledBorder( "Prgm Editor" ) ) ; 
          
           JPS.add( X ) ; 
         
           X =  new JScrollPane( Immediate ) ; 
           X.setBorder( new TitledBorder( "Immediate" ) ) ; 
        
           JPS.add( X ) ;
           add( JPS , BorderLayout.CENTER); 
           X = null;
          
         
       

	  
         StatusLine = new JTextArea( 3 , 50 ) ; 
	 // StatusLine.setBackground( Color.white);
         X = new JScrollPane( StatusLine);
         X.setBorder(new TitledBorder( "Status" ));
	 //StatusLine.setBorder( new TitledBorder( "Status" ));
         
	 add( X , BorderLayout.SOUTH ) ; 

	ExecLine.addPropertyChangeListener( "Display" , this ) ; 
	
    }

    /**
     *adds a data set to the permanent workspace of the programs
     *@param    dss    The data set that is to be added
     */
    public void addDataSet( DataSet dss )   
    { if( Debug)System.out.println( dss.getTitle());
       ExecLine.addDataSet( dss ) ; 
       
      }
   



    //Executes one line in a Document

    private  void execute1( Document  Doc  ,  
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




    //Executes all Commands in a document

    private void execute( Document Doc )
      {int line ; 
       Element E ; 
       if( Doc == null ) return ;        

         if( MacroDocument == null)
	     new IsawGUI.Util().appendDoc( logDocument , "#$ Start Program Run");
         else
              new IsawGUI.Util().appendDoc(logDocument , "#$ Start Macro Run");

       E = Doc.getDefaultRootElement() ; 
         for( line = 0 ; line < E.getElementCount(); line ++)
           { String S = getLine( Doc , line);
              new IsawGUI.Util().appendDoc(logDocument , S);
            }
       ExecLine.initt() ; 
       if(StatusLine !=null) StatusLine.setText("");
       if( Immediate !=null) Immediate.setText("");
       ExecLine.resetError();
       perror = -1 ; 
       line = executeBlock( Doc , 0 ,  true ,0 ) ; 
       if( perror < 0)seterror( ExecLine.getErrorCharPos(),ExecLine.getErrorMessage());
       if( perror >= 0 ) 
          if( lerror < 0 )lerror = line;
       if( line  <  E.getElementCount() )
	   if( perror < 0 )
              {seterror(  line , "Did Not finish program" ) ; 
               lerror = line;
              }
       

       if( MacroDocument == null)
	  new IsawGUI.Util().appendDoc( logDocument , "#$ End Program Run");
       else
          new IsawGUI.Util().appendDoc(logDocument , "#$ End Macro Run");        
      
         
    }
   private void execute( Parameter Args[])
    {int i;
     String S;
     
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
        { new IsawGUI.Util().appendDoc(logDocument , "#$ Start Macro Run");
          S="(";
          int line;
          if( Args != null)
          for( i=0; i< Args.length ; i++)
             {S = S + Args[i].getValue().toString();
              if( i+1 < Args.length) S = S+",";
             }
           S = S + ")";
          new IsawGUI.Util().appendDoc(logDocument , "Args ="+S);

          Element E = MacroDocument.getDefaultRootElement() ; 
          for( line = 0 ; line < E.getElementCount(); line ++)
             { S = getLine( MacroDocument , line);
                new IsawGUI.Util().appendDoc(logDocument , S);
              }

          k = executeBlock( MacroDocument ,2 ,true ,0) ;
          new IsawGUI.Util().appendDoc(logDocument , "#$ End Macro Run");
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
    private String SubsRangeVars( String VarIter)
      {int i, j, k, s, t ;
       String Res;
      
       if( VarIter == null ) 
         return null;
       Res = "";
       s = 0;
       if(VarIter.length()<=0) return null;
       if(VarIter.charAt(0)!='[')
           return null;
       i = findQuote( VarIter, 1,1, ":", "(){}[]");
       if( i < VarIter.length())
	   if( VarIter.charAt(i) != ':')
	       return VarIter;
       if(Debug)System.out.println("in subs i ="+i+","+VarIter.length());
       while( (i < VarIter.length() ) && ( i>=0 ))
	 {j = findQuote( VarIter, -1, i , ",[" , "(){}[]");
	  k = findQuote( VarIter, +1, i , ",]" , "(){}[]");
          if( Debug)
	      System.out.println("Subs range,i,j,k="+i+","+j+","+k+","+s +","+VarIter);
          if( (j >= VarIter.length()) || ( k < 0 ))
	     return null;
          Res = Res +VarIter.substring(s , j+1);
          t = ExecLine.execute( VarIter, j+1 , i);
          perror = ExecLine.getErrorCharPos();
          if( perror >= 0) 
                 {serror = ExecLine.getErrorMessage();
                   return null;
                 }
          Res = Res + ExecLine.getResult().toString() + ":" ;
          t = ExecLine.execute( VarIter , i + 1 , k ) ;
          perror = ExecLine.getErrorCharPos();
          if( perror >= 0) 
            {serror = ExecLine.getErrorMessage();
              return null;
            }
          if( ExecLine.getResult()!= null)
              Res = Res + ExecLine.getResult().toString()+ VarIter.charAt(k);
          else
              {perror = i; serror = " No Result";
               return null;
               }
          s = k+1;
          i = findQuote(VarIter,1, k+1, ":","()[]{}");
          if( i < VarIter.length())
	    if( VarIter.charAt(i) != ':')
	       i = VarIter.length() ;
 
         }
       Res = Res + VarIter.substring(s);
       return Res;
      }

    private int executeForBlock( Document Doc , int start , boolean execute, int onerror )
    { String var ; 
      Command.ListHandler LL ; 
      int i , j , k ; 
      int line  ;  
       String S ; 
       Element  E ,
	        F ;  
      
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
       j = S.toUpperCase().indexOf( " IN " ,  i + 1 ) ; 
       var = S.substring( i + 4 ,  j + 1 ).trim() ;
       String Iter = SubsRangeVars( S.substring( j + 4 ).trim());
       if(Debug)
	   System.out.println("Iter = "+ Iter);
       
       if( (Iter == null) || ( perror >= 0) )
         if( onerror == 0)
	   { if(perror >=  0) {perror = 0;} else serror = "Improper Iterator";
             seterror ( perror + j+4 , serror );
             lerror = start;
	     //return start;
           }
          else
            execute = false;
       
       LL = new Command.ListHandler(Iter ) ; 
       LL.start() ; 
       line = start + 1 ; 
       String NextI =  LL.next() ; 
       while( (NextI != null ) && ( line < E.getElementCount() ) )
	   {S = var + " = " + NextI ; 
            NextI =  LL.next() ; 
	    if( execute) i = ExecLine.execute ( S , 0 , S.length() ) ; 
            if( (perror >= 0) && (onerror == 0) )
		{ return start ; 
                }
            if(execute) line = executeBlock( Doc , start + 1 , execute ,0 ) ; 
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
             if( perror >= 0 ) return perror ; 
	     if( !S.toUpperCase().trim().equals( "ENDFOR" ) )
	       {seterror( 0 , "No EndFor for a FORx" + S ) ; 
		return line ; 
               }
            
                  
           }
      
	    
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
  /*
   *  Used for more free form placement of the parameter definitions
   *  in a document
  */
   private int getNextMacroLine( Document Doc, int prevLine)
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
   private String getLine( Document Doc, int start )
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
 


/*
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
  public Vector getDefaultParameters( Document doc )
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
    

/**
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
  * @see #CommandPane( javax.swing.text.Document , DataSetTools.util.IObserver)  Constructor
  * @see #addDataSet( DataSetTools.DataSet.DataSet ) addDataSet
  * @see #execute( DataSetTools.operator.Parameter[]) Macro Execute
  * @see #getErrorCharPos()
  *@see #getErrorMessage()
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
  public  Parameter[] GUIgetParameters()
     { if(Debug)System.out.println("Start of GUIgetParameters");
       if( MacroDocument == null)
         {seterror( 1000, "Macro Does not Exist ");
          return  null;
         }
    if( Debug) System.out.println("Start GUI get Parameters");
    Vector V1 = getDefaultParameters( MacroDocument );
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
 /**
*  This routine can be used by Isaw to run a macro with parameters.
*  It creates a GUI that lets users enter values for the parameters in the
*  macro<P>
*
*@param    fname    The name of the file that stores the Macro
*@param    X        An Obsever who will receive the data sets that are "Sent" with the SEND command
*@param   DSS[]     A list of data sets that can be selected as values for Data Set Parameters.
*
*@return           The result( null usually) or an error message
*/
 public Object getExecScript( String fname ,IObserver X , DataSet DDS[], Document  DocLog)
  {    int i;
       String S;
     
        seterror(-1,"");
        lerror = -1;
         Document doc = (new Util()).openDoc( fname ); 
       
         
	 CommandPane cp = new CommandPane( doc , X);
         cp.setLogDoc(DocLog);
         MessageBox B;
         if( DDS != null )
           for( i = 0; i < DDS.length ; i++)
             cp.addDataSet(DDS[i]);
         
         Parameter P[] = cp.GUIgetParameters();
         seterror( cp.getErrorCharPos(), cp.getErrorMessage());
         lerror = cp.getErrorLine();
         if( Debug)System.out.println("getExec agter getGUIParam"+perror+","+lerror+","+serror);
         if( perror >= 0 )
           { B = new MessageBox("Error="+serror+" on line "+ lerror + "at position "+ perror);
           return "Error="+serror+" on line "+ lerror + "at position "+ perror;
           }
         if( Debug)System.out.println("Ere cp.execute" );
         if(Debug)
          {if( P == null) System.out.println("P is null");
           else System.out.println("P has length"+ P.length);
          }
         cp.execute(P);
         seterror( cp.getErrorCharPos(), cp.getErrorMessage());
         lerror = cp.getErrorLine();
         lerror = cp.getErrorLine();
         if( Debug)
	     System.out.println("End getExec err&result are"+perror+","+lerror+","+serror+","+cp.getResult());
         
         if( perror < 0 ) 
	   {  Object O = cp.getResult();
              String SS ;
              if( O == null ) SS = "(null)"; else SS = O.toString();
              if( cp.StatusLine == null)
                B = new MessageBox( SS );
              
               return cp.getResult();
           }
         if( cp.StatusLine == null )
         B = new MessageBox("Macro Error "+ serror +" on line"+lerror+" at position "+ perror);
         return serror;
        

  }

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


  public void propertyChange( PropertyChangeEvent evt )
    {if(Debug)
	System.out.println("IN PROPERTY CHANGEXXXXXX");
     if( evt.getPropertyName().equals( "Display" ) )
	//if( evt.getSource().equals( ExecLine ) )
	  {String S;
           Object O = evt.getNewValue();     
           
           if(Debug)
             System.out.println(" in Display Data Type ="+ O.getClass());
	                    // ","+PL.hasListeners("Display"));
             S = O.toString();
           if( StatusLine != null)
	      new Util().appendDoc(StatusLine.getDocument(), S ) ; 
          else
	      System.out.println( "Display is " + S ) ;
           PL.firePropertyChange("Display" , null , O );
          }

    }   

private  class MyMouseListener extends MouseAdapter implements ActionListener,
                                                               Serializable
  {public void actionPerformed( ActionEvent e )
    {Document doc ; 
     if( e.getSource().equals( Run ) ) 
       {doc = Commands.getDocument() ;
        MacroDocument = doc;
       
        ExecLine.resetError() ; 
        ExecLine.initt();
        seterror( -1,"");
        if(StatusLine != null) StatusLine.setText("");
        if(Immediate != null) Immediate.setText("");
        lerror = -1;
        Parameter P[] = GUIgetParameters();
       
         if( perror >= 0)
          {new Util().appendDoc( StatusLine.getDocument(), "Error"+serror+" at position"+
                                    perror+"on line"+lerror); 
           MacroDocument = null;
           return;
          }
        if( P == null ) 
          {MacroDocument = null;
            return;
          }
      
         
       
        StatusLine.setText( "" ) ; 
        perror = -1 ; 
       
        
        if( P.length > 0 )
          { execute( P ) ;
            if( perror >= 0)
              new Util().appendDoc( StatusLine.getDocument(), "Error"+serror+" at position"+
                                    perror+"on line"+lerror); 
          }
        else
         { MacroDocument = null; 
           execute( doc ) ; 
          }
	MacroDocument = null;
        if( perror >= 0 )
          {  new Util().appendDoc(StatusLine.getDocument(), "Status: Error " +  serror + " on line " 
                                       + lerror + " character" + perror ) ; 
             Element E = doc.getDefaultRootElement();
             if( lerror >= E.getElementCount()) 
                Commands.setCaretPosition(doc.getLength()-1);
             else if( lerror >= 0 )
	       {int p;
                Element Eline = E.getElement( lerror);
                p = Eline.getStartOffset() + perror ;
                if( p >= Eline.getEndOffset() ) p =Eline.getEndOffset();
                Commands.setCaretPosition(p);
                Commands.requestFocus();
                    
	       }
          }
       }
     else if( e.getSource().equals( Clear ))
      {if( ExecLine == null ) return;
      // Commands.setText("");
      // Immediate.setText("");
       ExecLine.removeDisplays();
       StatusLine.setText("");
       ExecLine.initt();
      }
    else if( e.getSource().equals( Save ) || e.getSource().equals( Open ))
        {final JFileChooser fc = new JFileChooser(FilePath) ; 
         int state  ; 
         if( SelectedFile != null )
           fc.setSelectedFile( SelectedFile );
         if( e.getSource().equals( Save ))
	     state = fc.showSaveDialog( null ) ; 
          else
             state =  fc.showOpenDialog( null  ) ;
       
         if( state != JFileChooser.APPROVE_OPTION )return ; 
         FilePath= fc.getCurrentDirectory().toString();
	 SelectedFile = fc.getSelectedFile() ; 
         String filename  = SelectedFile.toString() ; 
         String fname = SelectedFile.getName() ; 
         System.out.println( "The filename is "  + filename ) ;                        
	
	 if( e.getSource().equals( Save ) )
	     {
                 doc = Commands.getDocument() ; 
                 (new Util()).saveDoc( doc , filename );      
             }        
         else if( e.getSource().equals( Open ) )
             {               
		 doc = (new Util()).openDoc( filename );
                 if( doc != null)
                  {Commands.setDocument( doc ); 
                   Commands.setCaretPosition(0);   
                  }
                else
                  System.out.println("Document is null");   
	     } 
         }     
    else if( e.getSource().equals( Help ) )
        {//BrowserControl H = new BrowserControl() ; 
           HTMLPage H;
	 String S;
         
         S = System.getProperty("Help_Directory");
	 if( S!= null)
           { S = DataSetTools.util.StringUtil.fixSeparator( S);
            // System.out.print("A");if(S!=null)System.out.println(S); else System.out.println("");

             S = S.trim();
             if( S.length() < 1) 
                S = null;
             else if( "\\/".indexOf(S.charAt(S.length() - 1 ))< 0)
                S = S + java.io.File.separator;
             //System.out.println("A@="+ S + "Command/CommandPane.html");
             if( new File( S + "Command/CommandPane.html").exists())
               {}
             else S = null;
             // System.out.print("B");if(S!=null)System.out.println(S); else System.out.println("");
            } 

         if( S == null)
            { S= System.getProperty("user.dir").trim();
               //System.out.print("C");

              if( S!=null) 
                if( S.length() > 0 ) 
                 if(  "\\/".indexOf(S.charAt(S.length() - 1 ) ) < 0)
	           S = S + java.io.File.separator;
              S = DataSetTools.util.StringUtil.fixSeparator( S);
             // System.out.println(S);
	      if( !new File( S + "IsawHelp/Command/CommandPane.html").exists()) 
                 S = null;
              else 
                  S = S +"IsawHelp"+java.io.File.separator;
             

            }
         //System.out.print("D");if(S!=null)System.out.println(S); else System.out.println("");

        if( S != null )
	    S = S + "Command/CommandPane.html";
        else
	    {String CP = System.getProperty("java.class.path").replace( '\\','/') ;
	    int s, t ;
              //System.out.println("E");

            for( s = 0; (s < CP.length()) && (S == null); s++)
	      {t = CP.indexOf( ";", s+1);
               if( t < 0) t = CP.length();
               S = CP.substring(s,t) .trim();
               if( S.length() > 0 ) if ( S.charAt( S.length() -1) != '/') S = S + "/";
                //System.out.print("F");if(S!=null)System.out.println(S); else System.out.println("");


               if( new File( S + "IsawHelp/Command/CommandPane.html").exists())
                 S= S + "IsawHelp/Command/CommandPane.html";
               else S = null;     
           
                }
              }
          //System.out.print("G");if(S!=null)System.out.println(S); else System.out.println("");


         if( S == null )S = "http://www.pns.anl.gov/isaw/IsawHelp/CommandPane.html";
         else S = "file:///" + S;
         //H.displayURL( S ) ;
          S= S.replace( '\\','/');
          System.out.println("Source is"+S); 
          try{
            H = new HTMLPage( S ) ;
            Dimension D = getToolkit().getScreenSize();
             H.setSize((int)(.6* D.width) , (int)(.6*D.height) ); 
              H.show();
             }
           catch(Exception s)
             {if(StatusLine!=null) StatusLine.setText("CANNOT FIND HELP FILE");
              else System.out.println("CANNOT FIND HELP FILE");
             }
        
        }
    }// end actionperformed 
 }//End mouseAdapter 

  public String getVersion()
    { return "7_6_00";
    } 

private class MyKeyListener  extends KeyAdapter 
                             implements KeyListener
  { CommandPane CP;
    int line =0;
    public MyKeyListener( CommandPane CP) 
       {this.CP = CP;
       }
    public void keyTyped( KeyEvent e )
    { 
      if('x' == 'y') //e.getKeyChar())   used for testing macros)
      { 
        line = getNextMacroLine( Commands.getDocument(), line);
        System.out.println( "line ="+line);
     
       }

        if( e.getKeyChar() == KeyEvent.VK_ENTER )	
          if( e.getSource().equals( Immediate ) )
	      {  int i = Immediate.getCaretPosition() ; 
	          Document doc = Immediate.getDocument() ;
                  Element E = doc.getDefaultRootElement(); 
	          int line  = E.getElementIndex( i ) - 1 ;             
		  
	            try{ 
                      if( doc.getText( i , 1 ).charAt( 0 ) >= ' ' )
                        { Immediate.getDocument().remove( i - 1 , 1 ) ;                
		        }
	               }
	            catch( javax.swing.text.BadLocationException s )
                         {}
		    
                  
                 
             
	          perror = -1 ; 
                  serror = "" ; 
                  lerror = -1 ; 
	          ExecLine.resetError() ; 
                 // if( StatusLine != null )
                    // StatusLine.setText( "" ) ; 
                    new IsawGUI.Util().appendDoc(logDocument,"#$ Start Immediate Run");
                   new IsawGUI.Util().appendDoc(logDocument,getLine(Immediate.getDocument(), line));
	          execute1( Immediate.getDocument() , line ) ;
                  
                   new IsawGUI.Util().appendDoc(logDocument,"#$ End Immediate Run"); 
                  if( perror >= 0 )
                    {if( StatusLine != null )
                        new Util().appendDoc(StatusLine.getDocument(), "Status: Error " + serror + 
                           " on line " + line + " character" + perror ) ; 
		     int p;
                     Element Eline = null;
                     if( (line >= 0) &&( line < E.getElementCount()))
                       {Eline = E.getElement(line);
                        p = Eline.getStartOffset() + perror;
                        if( p > Eline.getEndOffset()) p = Eline.getEndOffset()-1;
                        Immediate.setCaretPosition(p);
                       }
		    
                     }//if perror>=0
                
                    line = E.getElementCount() - 1;
                    if( line >= 0)
                      if( E.getElement(line).getEndOffset() - E.getElement(line).getStartOffset() < 2)
                         return;
		    try{
                       doc.insertString( doc.getLength(), "\n " , null );
		       }
                    catch( BadLocationException s){System.out.println("XXCVB");}
	      }//if immediate window
    
    }
  }//End MyKeyListener

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

public Object getResult()
  { if( ExecLine != null )
      return ExecLine.getResult();
    else
      return null;
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
       PL = new PropertyChangeSupport( this );
   PL.addPropertyChangeListener(P);
  }


public  void   update(  Object observed_obj ,  Object reason ) 
    {OL.notifyIObservers( this  ,  reason  ) ; 
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
//*****************SECTION:MAIN********************
public static void  main( String args[] )
    { 
    java.util.Properties isawProp;
     isawProp = new java.util.Properties(System.getProperties());
   String path = System.getProperty("user.home")+"\\";
       path = StringUtil.fixSeparator(path);
       try {
	    FileInputStream input = new FileInputStream(path + "props.dat" );
          isawProp.load( input );
	   // Script_Path = isawProp.getProperty("Script_Path");
         // Data_Directory = isawProp.getProperty("Data_Directory");
          //Default_Instrument = isawProp.getProperty("Default_Instrument");
	    //Instrument_Macro_Path = isawProp.getProperty("Instrument_Macro_Path");
	    //User_Macro_Path = isawProp.getProperty("User_Macro_Path");
          System.setProperties(isawProp);  
    //    System.getProperties().list(System.out);
          input.close();
       }
       catch (IOException ex) {
          System.out.println("Properties file could not be loaded due to error :" +ex);
       }
      JFrame F ;  
      CommandPane P; 
      F = new JFrame( "Test" ); 
     

     P = new CommandPane(); 
     Dimension D = P.getToolkit().getScreenSize();
     F.setSize((int)(.6* D.width) , (int)(.7*D.height) ); 
     F.show() ;  
     F.getContentPane().add( P ); 
   
     F.validate(); 
     
     
     
     

    }



}
