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



/** Pane to enter and execute and handle commands
 *  A command can be executed immediately  or 
 *  A sequence of commands can all be executed.
 *      The Immediate commands are entered in the immediate window
 *      The sequence of commands are entered in the Editor window.
 
 *  The commands can act on Isaw Data Sets and will be extended to include
 *  most of the commands availabe in the GUI.
 */
public class CommandPane  extends JPanel 
                          implements KeyListener , 
                                     ActionListener , 
				     PropertyChangeListener , 
                                     IObservable ,  IObserver
{    
  
 //***********************SECTION GUI****************
  JButton Run , 
            Open , 
            Save , 
            Help , 
            Undo ; 

    JTextArea  Commands , 
               Immediate ; 

    JLabel    StatusLine ; 
  
    Command.execOneLine ExecLine ; 
    
    int lerror ,                                //line number of error. 
        perror ;                                //position on line of error
    String serror ;                             //error message

   
    IObserverList OL ; 

    String FilePath = null  ;                   // for macro storage and retrieval
    Document logDocument = null;

    boolean Debug = false;


    /** Constructor with Visual Editor and no data sets from outside this program
     *  This can be used with a stand alone editor
     */
    public CommandPane()
    {initialize() ; 
    ExecLine = new Command.execOneLine() ; 
    
    init() ; 
    OL = new IObserverList() ; 
      
    }
   


    /** Constructor with Visual Editor and one data sets from outside this program
     *  This can be used with the Isaw package
     *    @param    Dat        One data set
     */
    public CommandPane( DataSet Dat )
    {initialize() ;  
    ExecLine = new Command.execOneLine( Dat ) ; 
    init() ; 
    OL = new IObserverList() ; 
   }

    /** Constructor with Visual Editor and a set of data set from outside this program
     *
     *    @param    dss[]        A list of data sets
     */
    public CommandPane( DataSet dss[] )
    {
       initialize() ; 
     ExecLine = new Command.execOneLine( dss ) ; 
     init() ; 
     OL = new IObserverList() ; 
    }



    /** Constructor with no Visual Editing Element.  
     *  This form could be used for Batch files
     *
     *    @param   TextFileName     The name of the text file containg commands
     */
    public CommandPane( String TextFileName )
      {int c ; 
       int offset ; 
       String line ; 
       Document doc ; 
       FileReader fr ; 
       File f ; 
       ExecLine = new Command.execOneLine() ; 
       OL = new IObserverList() ; 
       try{
          f = new File( TextFileName ) ; 
          }
       catch( NullPointerException s )
	 {System.out.println( " Improper filename" ) ; 
	  return ; 
         }
       try{
           fr = new FileReader( f ) ; 
          }
	 catch( FileNotFoundException s )
	   {System.out.println( "File not found" ) ; 
	    return ; 
	   }
       
      
       doc = new PlainDocument() ; 
                 
      
                 
       line = "" ; 
       offset = 0 ; 
       try{
       for( c = fr.read() ; c !=  -1 ;  )
	 {  //System.out.print( "J" + line + "," ) ; 
           line = line  +  new Character( ( char )c ).toString() ; 
           if( c < 32 ) //assumes the new line character
	     { //System.out.print( "K" ) ; 
               doc.insertString( offset , line , null ) ; 
	       offset += line.length() ; 
               line = "" ;
               }
           //System.out.print( "L" ) ; 
            c = fr.read() ; 
         }
       if( line.length() > 0 )doc.insertString( offset , line , null ) ; 
       fr.close() ; 
         }
       catch( IOException s )
         { System.out.println( "Format Error in file" ) ; 
	 return ; 
         }
       catch( BadLocationException s )
	 {System.out.println( " Internal error in document" ) ; 
	 return ; 
	 }
       execute( doc ) ; 
    }



    /** Constructor that can be used to run Macros
     *
     *    @param  Doc      The Document that has macro commands
     *    @param  P        The parameters and values to run the Macro
     */
    public CommandPane( Document Doc , Parameter P )
      {OL = new IObserverList() ; 
       ExecLine = new Command.execOneLine() ; 

      }
/** 
  * Sets up the document that logs all operations
  *@param   doc     The document that logs operations
*/
    public void setLogDoc( Document doc)
    { logDocument = doc;
      ExecLine.setLogDoc( doc);
    }
    private void initialize()
    {
      Run  =  null ;   
     Open = Save = Help = Undo = null ;  
     Commands = null ; 
    
    
    }

    /** Initializes the Visual Editor Elements
     *
     */
    public void init()
    {JPanel JP ; 
       
        Rectangle R = getBounds() ; 
        
       
        Run = new JButton( "Run Prgm" ) ; 
       
        Open = new JButton( "Open Prgm" ) ; 
        Save = new JButton( "Save Prgm" ) ; 
        Help = new JButton( "Help" ) ; 
        
        Run.addActionListener( this ) ; 
        Open.addActionListener( this ) ; 
        Save.addActionListener( this ) ; 
        Help.addActionListener( this ) ; 
        //Undo.addActionListener( this ) ; 

        setLayout( new BorderLayout() ) ; 
           JP = new JPanel() ; 
           JP.setLayout( new GridLayout( 1 , 6 ) ) ; 
        
           JP.add( Run ) ; 
           JP.add( new JLabel( "       " ) ) ; 
           JP.add( Open ) ; 
           JP.add( Save ) ; 
           JP.add( Help ) ; 
           //JP.add( Undo ) ; 
	   
        add( JP , BorderLayout.NORTH ) ; 

	

           Commands = new JTextArea( 7 , 50 ) ; 
           Commands.setLineWrap( true ) ; 
           Immediate = new JTextArea( 5 , 50 ) ; 
           Immediate.addKeyListener( this ) ;        
	 
           JSplitPane JPS = new JSplitPane(JSplitPane.VERTICAL_SPLIT) ; 
           JScrollPane X =  new JScrollPane( Commands ) ; 
           X.setBorder( new TitledBorder( "Prgm Editor" ) ) ; 
          
           JPS.add( X ) ; 
          
           X =  new JScrollPane( Immediate ) ; 
           X.setBorder( new TitledBorder( "Immediate" ) ) ; 
          
           JPS.add( X ) ; 

          
         
        add( JPS , BorderLayout.CENTER ) ; 

	   
         StatusLine = new JLabel( "         " ) ; 
         StatusLine.setBackground( Color.white);
	 StatusLine.setBorder( new TitledBorder( "Status" ));
        
	 add( StatusLine , BorderLayout.SOUTH ) ; 

	ExecLine.addPropertyChangeListener( "Display" , this ) ; 
	
    }

    /**
     *adds a data set to the permanent workspace of the programs
     *@param    dss    The data set that is to be added
     */
    public void addDataSet( DataSet dss )   
      {ExecLine.addDataSet( dss ) ; 

      }
   


//

    //Executes one line in a Document
    private  void execute1( Document  Doc  ,  
                           int       line )

    {
     String S ; 
     Element E ; 
       
     if( Doc == null )return ; 
  
     if( line < 0 )return ; 
     E = Doc.getDefaultRootElement().getElement( line ) ; 
     if( E == null )return ;  
     
     try{
          S = Doc.getText( E.getStartOffset() , E.getEndOffset() - E.getStartOffset() - 1 ) ;         
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
     catch( javax.swing.text.BadLocationException s )
	 {if( StatusLine == null )
            System.out.println( "Bad line numbers" + E + "," + 
                           E.getStartOffset() + "," + E.getEndOffset() ) ; 
	 else 
           {StatusLine.setText( "Bad line numbers" + E + "," + E.getStartOffset() + "," 
                                  + E.getEndOffset() ) ; 
	   
           }
          
        }

    }




    //Executes all Commands in a document
    private void execute( Document Doc )
      {int line ; 
       Element E ; 
       if( Doc == null ) return ; 
       E = Doc.getDefaultRootElement() ; 
       ExecLine.initt() ; 
       perror = -1 ; 
       line = executeBlock( Doc , 0 ,  true  ) ; 
       if( line  <  E.getElementCount() )
	   if( perror < 0 )
              {seterror(  line , "Did Not finish program" ) ; 
               lerror = line;
              }
      
         
    }

   private int executeBlock ( Document Doc , int start ,  boolean exec )
     { int line ; 
       String S ; 
       Element  E = Doc.getDefaultRootElement(),
	        F ;                 

       line = start ; 
       if(Debug)System.out.print( "In exeBlock line ,ex=" + line+","+exec ) ; 
       while ( ( line < E.getElementCount() ) && ( perror < 0 ) )
	   {F =  E.getElement( line ) ; 
            try
	      {
 	       S = Doc.getText( F.getStartOffset() , F.getEndOffset() - F.getStartOffset() ) ; 
              }
            catch( BadLocationException s )
		{ seterror( 1000 ,  "Internal Error p" ) ; 
		  return line ; 
                }
           
           if( S !=  null )
               { //System.out.print( " S=" + S + ":" + S.toUpperCase().trim() ) ; 
	         int i ; 
                 char c ; 
                 for( i = S.length() - 1 ;  i >=  0  ;  i-- )
		  { if( S.charAt( i ) <=  ' ' )S = S.substring( 0 , i ) ; 
		    else i = -1 ; 
		  }
               }
            if( S == null )
		{}
            else if( S.toUpperCase().trim().indexOf( "FOR " ) == 0 )
	      line = executeForBlock ( Doc , line , exec ) ; 
	    else if ( S.toUpperCase().trim().indexOf( "ENDFOR" ) == 0 )
	      return line ; 
	    else if( S.toUpperCase().trim().indexOf( "ON ERROR" ) == 0 )
	       line = executeErrorBlock( Doc , line , true ) ; 
          else if( S.toUpperCase().trim().indexOf( "ELSE ERROR" ) == 0 )
	       return line ; 
	    else if( S.toUpperCase().trim().indexOf( "END ERROR" ) == 0 )
	      return line ; 
           else if( S.trim().length() <= 0 )
	      {}
            
	    else if( exec )  //can transverse a sequence of lines. On error , if then
             execute1( Doc  , line ) ; 
            if( perror >= 0 )
		{lerror = line ; 
		  if(Debug)System.out.println("errbot"+line) ; 
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
       if(Debug)System.out.println("in subs i ="+i);
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
          Res = Res + ExecLine.getResult().toString()+ VarIter.charAt(k);
          s = k+1;
          i = findQuote(VarIter,1, k+1, ":","()[]{}");
 
         }
       Res = Res + VarIter.substring(s);
       return Res;
      }

    private int executeForBlock( Document Doc , int start , boolean execute )
    { String var ; 
      Command.ListHandler LL ; 
      int i , j , k ; 
      int line  ;  
       String S ; 
       Element  E ,
	        F ;  
       //System.out.println( "In exec For" ) ; 
       if( Doc == null ) return -1 ; 
       E = Doc.getDefaultRootElement() ; 
       if( start < 0 ) return start ; 
       if( start >= E.getElementCount() ) return start ;   
       F = E.getElement( start ) ; 
       try{
         S = Doc.getText( F.getStartOffset() , F.getEndOffset()  -  F.getStartOffset() ) ; 
          }
       catch( BadLocationException s )
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
	   { if(perror >=  0) {perror = 0;} else serror = "Improper Iterator";
             seterror ( perror + j+4 , serror );
             lerror = start;
	     return start;
           }
       
       LL = new Command.ListHandler(Iter ) ; 
       LL.start() ; 
       line = start + 1 ; 
       String NextI =  LL.next() ; 
       while( (NextI != null ) && ( line < E.getElementCount() ) )
	   {S = var + " = " + NextI ; 
            NextI =  LL.next() ; 
	    i = ExecLine.execute ( S , 0 , S.length() ) ; 
            if( perror >= 0 )
		{ return start ; 
                }
            line = executeBlock( Doc , start + 1 , execute ) ; 
	    if ( perror >= 0 )
		return line ; 
            if( line >= E.getElementCount() )
	      { seterror( 0 , "No EndFor for a FORb" + S ) ; 
	        return line ; 
              }
             F = E.getElement(line ) ; 
             try{
               S = Doc.getText( F.getStartOffset() ,  F.getEndOffset() - F.getStartOffset() ) ; 
	        }
             catch( BadLocationException s )
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
       //seterror( 0 , "No EndFor for a FOR" ) ; 
	    
       return line ; 
                     

      }

    public int executeErrorBlock( Document Doc , int start , boolean execute )
      {String var ;      
      int i , j , k ; 
      int line ; 
       String S ; 
       boolean mode ; 
       Element  E ,
	        F ;  
       if(Debug) System.out.println("In exec Error" ) ; 
       if( Doc == null ) return -1 ; 
       E = Doc.getDefaultRootElement() ; 
       if( start < 0 ) return start ; 
       if( start >= E.getElementCount() ) return start ;   
       F = E.getElement( start ) ; 
       try{
         S = Doc.getText( F.getStartOffset() , F.getEndOffset()  -  F.getStartOffset() ) ; 
          }
       catch( BadLocationException s )
	 {seterror ( 0 , "Internal Errorc" ) ; 
	  return start ; 
	 }
       if( !S.toUpperCase().trim().equals( "ON ERROR" ) )
	   {seterror ( 0, "internal error d" ) ; 
	    return start ; 
           }
       line = executeBlock( Doc , start + 1 , true ) ; 
       if( perror >= 0 )
	 {ExecLine.resetError() ; 
	   perror = -1 ; 
           serror="";
         if(Debug) System.out.println("In ExERROR ERROR occured");
  	   line = executeBlock( Doc , line , false ) ; 
          mode  = true ;    
          if(Debug) System.out.println("After EXERROR ocurred "+line);
          
         }
       else mode = false ; 
 
       if( line >= E.getElementCount() )
         {seterror ( 0 , " No ENDERROR for On Error" ) ; 
	  return line ; 
          }
       F = E.getElement( line ) ; 
       try{
          S = Doc.getText( F.getStartOffset() , F.getEndOffset() - F.getStartOffset() ) ; 
           }
       catch( BadLocationException s )
	  {seterror ( 0 , "Internal Errorc" ) ; 
	   return line ; 
	  }
      if( S.toUpperCase().trim().equals( "ELSE ERROR" ) )
	{ 
          line =executeBlock( Doc , line + 1 ,mode ) ;  
	}
      else if( S.toUpperCase().trim().equals( "END ERROR" ) )
	  {   if(Debug)System.out.println("ENd ERROR occurred"+perror+","+line);
            return line ; 
          
        }
      else
	{  seterror( line , " NO ELSE or END Error for On ERRor" ) ; 
	   return line ;  
	}

         
       F = E.getElement( line ) ; 
       try{
          S = Doc.getText( F.getStartOffset() , F.getEndOffset() - F.getStartOffset() ) ; 
           }
       catch( BadLocationException s )
	  {seterror ( 0 , "Internal Errorc" ) ; 
	   return line ; 
	  }

     if( !S.toUpperCase().trim().equals( "END ERROR") )
      {seterror( 0 , " NO ELSE or END Error for On ERRor" ) ;  }
      return line ;  
      


      }

    public void seterror( int poserr , String ermess )
      { perror = poserr ; 
        serror = ermess ; 
      }
      
       /*  for(i = 0 ; 
              (i < Doc.getDefaultRootElement().getElementCount() ) && ( perror < 0 ) ; 
                              i++ )
	  {
           execute1( Doc , i ) ; 
            if( perror >= 0 )
               {
                 lerror = i ; 
                 return ; 
               }
        }
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


  public void propertyChange( PropertyChangeEvent evt )
    {if(Debug)
	System.out.println("IN PROPERTY CHANGEXXXXXX");
     if( evt.getPropertyName().equals( "Display" ) )
	if( evt.getSource().equals( ExecLine ) )
	    if( StatusLine != null)
		StatusLine.setText( "Status:" + evt.getNewValue() ) ; 
            else
		System.out.println( evt.getNewValue() ) ; 

    }   


  public void actionPerformed( ActionEvent e )
    {Document doc ; 
    if( e.getSource().equals( Run ) ) 
       {
        doc = Commands.getDocument() ; 	
        StatusLine.setText( "Status" ) ; 
        perror = -1 ; 
        ExecLine.resetError() ; 
        execute( doc ) ; 
	//System.out.println("doc=" + doc + " , " + perror ) ; 
       if( perror >= 0 )
          {  StatusLine.setText( "Status: Error " + serror + " on line " + lerror + " character" + perror ) ; 
             Element E = doc.getDefaultRootElement();
             if( lerror >= E.getElementCount()) Commands.setCaretPosition(doc.getLength()-1);
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
    
    else if( e.getSource().equals( Save ) || e.getSource().equals( Open ))
        {final JFileChooser fc = new JFileChooser(FilePath) ; 
         int state  ; 
         if( e.getSource().equals( Save ))
	     state = fc.showSaveDialog( null ) ; 
          else
             state =  fc.showOpenDialog( null  ) ;
 
         if( state != JFileChooser.APPROVE_OPTION )return ; 
         FilePath= fc.getCurrentDirectory().toString();
	 File f = fc.getSelectedFile() ; 
         String filename  = f.toString() ; 
         String fname = f.getName() ; 
         System.out.println( "The filename is "  + filename ) ;                        
	
	 if( e.getSource().equals( Save ) )
	     {Element line ; 
	     try{
                 FileWriter fw = new FileWriter( f ) ; 
                 doc = Commands.getDocument() ; 
	         int i ; 
		
                 Element  root ; 
                 root = doc.getDefaultRootElement() ; 
		
                 for( i = 0 ; i < root.getElementCount() ; i++ )
                    {line = root.getElement( i ) ; 
		   
		     fw.write( doc.getText( line.getStartOffset() , line.getEndOffset() - line.getStartOffset() - 1 ) ) ; 
                     fw.write( "\n" ) ; 
                     }
		 fw.close() ; 
                 // System.out.print( "F" ) ; 
	         }
	     catch( IOException s )
                     {StatusLine.setText( "Status: Unsuccessful" ) ; }
             catch( javax.swing.text.BadLocationException s )
                 {//System.out.print( "N" ) ; 
                 }
	       //System.out.print( "G" ) ; 
             }        
         else if( e.getSource().equals( Open ) )
             {try{  
                  FileReader fr = new FileReader( f ) ; 
                  int c , offset ; 
	          String line ; 
                  doc = Commands.getDocument() ; 
                 
                  doc.remove( 0 , doc.getLength() ) ; 
		
                  line = "" ; offset = 0 ; 
                  for( c = fr.read() ; c != -1 ;   )
	              {  //System.out.print( "J" + line + "," ) ; 
                        line = line + new Character( ( char )c ).toString() ; 
                        if( c < 32 ) //assumes the new line character
			    { //System.out.print( "K" ) ; 
                           doc.insertString( offset , line , null ) ; 
		           offset+=line.length() ; 
                           line = "" ; 

                          }
			
                         c = fr.read() ; 
                       }
		  if( line.length() > 0 )doc.insertString( offset , line , null ) ; 
                  fr.close() ; 
                  Commands.setCaretPosition(0);
	           }
	        catch( IOException s )
                   {StatusLine.setText( "Status: unsuccessful" ) ; }
                 catch( javax.swing.text.BadLocationException s )
		     { //System.out.print( "M" ) ; 
                     }
	    //System.out.print( "O" ) ; 
	    
	     }

        }
        
    else if( e.getSource().equals( Help ) )
        {BrowserControl H = new BrowserControl() ; 
	String S;
        String Base="IPNS_Software";
        S= System.getProperty("user.dir");
        //System.out.print("us dir="+S+",");
        int kk = S.indexOf("/"+Base);
        //System.out.print("A:k="+kk+",");
        if(kk < 0) kk = S.indexOf("\\"+Base);
        //System.out.println("B:k="+kk+",");
        if( kk >= 0 )
	    S = "file://"+S.substring(0,kk)+"/"+Base+"/IsawHelp/CommandPane.html";
        else
	    {S = System.getProperty("java.class.path");
	    //System.out.print("classpth dir="+S+",");
             kk = S.indexOf("/"+Base);
	     //System.out.print("C:k="+kk+",");
            if(kk < 0) kk = S.indexOf("\\"+Base); 
	    //System.out.print("D:k="+kk+",");
            if( kk >= 0) 
               S = "file://"+S.substring(0,kk)+"/"+Base+"/IsawHelp/CommandPane.html";
            else
		S = null;
	    //if( S!=null) System.out.println(S.substring(7));
	    if( S!= null) if (new File( S.substring(7)).exists()){} else S = null;
           
            }
         if( S == null )S = "http://www.pns.anl.gov/isaw/IsawHelp/CommandPane.html";
         H.displayURL( S ) ; 
          
        
        }
    }  

public String getVersion()
  { return "7_6_00";
  } 
public void keyTyped( KeyEvent e )
    {if( e.getKeyChar() == KeyEvent.VK_ENTER )	
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
                  if( StatusLine != null )
                     StatusLine.setText( "Status" ) ; 
	          execute1( Immediate.getDocument() , line ) ;  
                  if( perror >= 0 )
                    {if( StatusLine != null )
                        StatusLine.setText( "Status: Error " + serror + " on line " + line + " character" + perror ) ; 
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
public void keyPressed( KeyEvent e )
    {//StatusLine.setText( "ABCDEFGHIJKLMNOPQRSTUVWXYZ" ) ; 
    }
public void keyReleased( KeyEvent e )
    {
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
           
            else if(SrchChars.indexOf(c)>=0)
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
    {JFrame F ;  CommandPane P ; 
    F = new JFrame( "Test" ) ; 
    

 P = new CommandPane() ; 
   
    F.setSize( 800 , 400 ) ; 
   F.show() ;  
    F.getContentPane().add( P ) ; 
   
     F.validate() ; 
     
     
     //  F.pack() ;  
    char c ; 

    }




}
