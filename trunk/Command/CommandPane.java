/*
 * File:  CommandPane.java 
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
 * Revision 1.33  2001/12/21 17:50:20  dennis
 * 1: Almost completely separated out the status pane.
 * 2: Included a Save and Clear option on this status pane
 *    for demonstration only.
 * 3: The open and save buttons now use the new actionlisteners:
 *    OpenFileToDocListener and SaveDocToFileListener
 *
 * Revision 1.32  2001/11/19 19:04:09  Ruth
 * Now uses a new constructor for JParametersDialog, that makes the
 * dialog box modal.  If there is an error when the dialog box is
 * exited, the cursor will be on the line with the error.
 *
 * Revision 1.31  2001/11/12 21:20:06  dennis
 *  1. Eliminated a Debug print that appears when using the
 *     immediate pane.
 *
 * Revision 1.30  2001/11/09 18:25:29  dennis
 *   1. Made Changes to use arbitrary SciptProcessorOperator's like Jython
 *   2. The statusLine is now resizable
 *   3. The title bar in the stand alone CommandPane now says
 *      Command Pane instead of Test.
 *   4. The latest filename now appears as the title of the
 *      Program Editor window.
 *   5. Changed several utility routines from ScriptProcessor to static methods
 *
 * Revision 1.29  2001/09/07 16:17:02  dennis
 * Removed debug print (Ruth)
 *
 * Revision 1.28  2001/08/02 16:21:16  rmikk
 * Added a routine to take care of the differing end of line
 * indicators from different wordprocessors.
 *
 * Revision 1.27  2001/07/18 16:23:00  neffk
 * changed the DataSet[] parameter of getExecScropt(...) to
 * IDataSetListHandler so that CommandPane could have a dynamic list
 * of DataSet objects.
 *
 * Revision 1.26  2001/07/11 20:53:22  rmikk
 * Change the Prgm on the Command Pane buttons to the
 * word "Script"
 *
 * Revision 1.25  2001/07/03 22:14:17  rmikk
 * Added Code to eliminate the "UNKNOWN" description at
 * the top of the JParametersDialog
 *
 * Revision 1.24  2001/06/26 14:34:54  rmikk
 * Fixed Session Log reporting
 *
 * Revision 1.23  2001/06/25 19:59:31  chatter
 * Added Last Parameter to JParametersDialog Constructor
 *
 * Revision 1.22  2001/06/25 19:25:26  rmikk
 * Added SplitPane With State and Clearred the
 * Status Line before every run
 *
 * Revision 1.21  2001/06/05 16:50:35  rmikk
 * Changed props.dat to IsawProps.dat
 *
 * Revision 1.20  2001/06/04 20:04:17  rmikk
 * Fixed Documentation
 * Change ; to pathSeparator
 *
 * Revision 1.19  2001/06/01 21:14:12  rmikk
 * Added Documentation for javadocs etc.
 *



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

2-2-2001
   - For loops now use Arrays and can be variables.

5-17-2001
   - Converting CommandPane to an operator "successfully"
   - JFrame returned in a function call

5-20-2001
   -Separated out the GUI part of CommandPane from the processor part
    which went to ScriptProcessor.java
   -Added a wide Cursor  
*/
package Command; 

//import IsawGUI.Isaw.*; 
import IsawGUI.*; 
import java.io.*; 
import DataSetTools.retriever.*; 
import DataSetTools.dataset.*; 
import DataSetTools.util.*; 
import DataSetTools.viewer.*; 
import DataSetTools.components.containers.*;
import DataSetTools.components.ParametersGUI.*;
import java.awt.*; 
import javax.swing.*; 
import javax.swing.text.*; 
import java.awt.event.*; 
import javax.swing.border.*; 
import DataSetTools.operator.*; 
import java.beans.*; 
import java.util.Vector;
import Command.*;

/** Pane to enter, execute and handle commands
 *  A command can be executed immediately  or 
 *  A sequence of commands can all be executed.
 *      The Immediate commands are entered in the immediate window
 *      The sequence of commands are entered in the Editor window.
 *  The commands can act on Isaw Data Sets and will be extended to include
 *  most of the commands available in the GUI.
 */


public class CommandPane extends JPanel  implements PropertyChangeListener , 
                                         IObservable 

{
 JButton Run ,  
          Open , 
          Save , 
          Help , 
          Clear ; 

    JTextArea  Commands , 
               Immediate ; 

    StatusPane    StatusLine ; 

    String FilePath = null  ;             // for macro storage and retrieval
    File SelectedFile = null;
    Document logDoc=null;
    public ScriptProcessorOperator  SP;
    boolean Debug = false;
    PropertyChangeSupport PC;
/**
*  Creates the JPanel for editing and executing scripts
*/
public CommandPane()
    { 
      initt();
      SP = new ScriptProcessor(  Commands.getDocument());
      SP.addPropertyChangeListener( StatusLine );
      PC = new PropertyChangeSupport( this );
      PC.addPropertyChangeListener( StatusLine );
      //SP.addPropertyChangeListener( this );
     }

/**
*  Set the Document that logs operations
*
*@param   doc  the Document that will receive log information
*
*NOTE: In the future the log document may be executable to redo
*      a session
*/
public void setLogDoc(Document doc)
   { logDoc = doc;
     SP.setLogDoc( doc );
   }
 /** Adds property change Listener.  So far the only events sent have the
 *  Display and Clear property for the Status Pane
 */
 public void addPropertyChangeListener(PropertyChangeListener listener)
   { PC.addPropertyChangeListener( listener );
     SP.addPropertyChangeListener( listener);
    }
/** Adds property change Listener.  So far the only events sent have the
 *  Display and Clear property for the Status Pane
 */
 public void removePropertyChangeListener(PropertyChangeListener listener)
   {PC.removePropertyChangeListener( listener );
     //SP.removePropertyChangeListener( listener);
   }
 private void initt()
   {    JPanel JP;   
        Rectangle R = getBounds() ; 
        
      
        Run = new JButton( "Run Script" ) ; 
       
        Open = new JButton( "Open Script" ) ; 
        Save = new JButton( "Save Script" ) ; 
        Help = new JButton( "Help" ) ; 
        Clear = new JButton("Clear");
        Run.addActionListener( new MyMouseListener(this ) ) ; 
        
        //Open.addActionListener( new MyMouseListener( this ) ) ; 
        //Save.addActionListener(new MyMouseListener( this ) ) ; 
        Help.addActionListener( new MyMouseListener( this ) ) ; 
        Clear.addActionListener( new MyMouseListener( this ) ) ; 

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
           Immediate.addKeyListener( new MyKeyListener( this )) ;        
	   Commands.setCaret( new MyCursor());
           Immediate.setCaret( new MyCursor());
           //JSplitPane JPS = new JSplitPane(JSplitPane.VERTICAL_SPLIT) ; 
           //JPS.setResizeWeight( .8);
           JScrollPane X =  new JScrollPane( Commands ) ; 
           X.setBorder( new TitledBorder( "Prgm Editor" ) ) ; 
          
           //JPS.add( X ) ; 
         
           JScrollPane Y =  new JScrollPane( Immediate ) ; 
           Y.setBorder( new TitledBorder( "Immediate" ) ) ; 
        
           //JPS.add( X ) ;
           SplitPaneWithState JPS = new SplitPaneWithState( JSplitPane.VERTICAL_SPLIT ,
                        X , Y , .75f);    
           //add( JPS , BorderLayout.CENTER);           
            X = null;
          
	  
         StatusLine = new StatusPane( 3 , 50 ) ; 
	 // StatusLine.setBackground( Color.white);
         X = new JScrollPane( StatusLine);
         X.setBorder(new TitledBorder( "Status" ));
         JPanelwithToolBar YY = new JPanelwithToolBar("Save", "Clear",
                           new SaveDocToFileListener( StatusLine.getDocument(), null),
                           new ClearDocListener( StatusLine.getDocument()),
                           X);
	 //StatusLine.setBorder( new TitledBorder( "Status" ));
         StatusLine.setEditable( false );
         SplitPaneWithState Center= new SplitPaneWithState( JSplitPane.VERTICAL_SPLIT ,
                      JPS,  YY,.80f);
  
        add(Center, BorderLayout.CENTER);         
	 //add( X , BorderLayout.SOUTH ) ; 
 
     try{        
       FilePath = System.getProperty("Script_Path");  
       FilePath = DataSetTools.util.StringUtil.fixSeparator(FilePath);   
       if( Debug )
             System.out.println( "FilePath is "+FilePath);   
        }
     catch( Exception s) 
      {FilePath = null;
       if( Debug ) 
          System.out.println(" System properties could not be set");
      }
        OpenFileToDocListener Opn = new OpenFileToDocListener( Commands.getDocument(),
                FilePath);
        SaveDocToFileListener Sav = new SaveDocToFileListener( Commands.getDocument(),
                FilePath, Opn,"filename");
        Opn.addPropertyChangeListener( Sav);
                           
        Open.addActionListener( Opn);
        Save.addActionListener( Sav );

   }
/**
*  This routine can be used by Isaw to run a macro with parameters.
*  It creates a GUI that lets users enter values for the parameters in the
*  macro<P>
*
*@param    fname    The name of the file that stores the Macro
*@param    X        An Obsever who will receive the data sets that are "Sent" with the SEND command and that
*                   are the result of the script fname
*@param   DSS[]     A list of data sets that can be selected as values for Data Set Parameters.
*
*/
 public void getExecScript( String fname ,IObserver X , IDataSetListHandler ds_src, Document  DocLog)
  {    int i;
       String S;
       Object RES;
         
	 ScriptOperator cp = new ScriptOperator( fname );
         cp.addIObserver( X );
         cp.addPropertyChangeListener( this );
         cp.setLogDoc(DocLog);
        
        if( cp.getErrorCharPos() >= 0)
          {
           return ;
          }
        new IsawGUI.Util().appendDoc(logDoc,"#$ Script File Execute "+fname);
        DataSetTools.components.ParametersGUI.JParametersDialog pDialog = 
                new DataSetTools.components.ParametersGUI.JParametersDialog(cp, ds_src, logDoc ,X);
   }

/**
*  Allows for DataSets from external sources to be used by the ScriptProcessor
*
*@param   dss   The data set that is to be added to this CommandPane unit
*/ 
  public void addDataSet( DataSet dss )   
    {  SP.addDataSet( dss );
     }
/**
*  Receives a PropertyChange Event. 
*@param evt  the PropertyChangeEvent.
*Note: The only event that is serviced is the one with the property name "Display".<BR>
*<ul> The new value will be displayed in the Status line </ul>
*/ 
public void propertyChange( PropertyChangeEvent evt )
     {/*if(Debug)
	System.out.println("IN PROPERTY CHANGEXXXXXX");
      if( evt.getPropertyName().equals( "Display" ) )    
	  {String S;
           Object O = evt.getNewValue();     
           
           if(Debug)
             System.out.println(" in Display Data Type ="+ O.getClass());
	                    // ","+PL.hasListeners("Display"));
	    S = O.toString();
          // if( StatusLine != null)
	   //   new Util().appendDoc(StatusLine.getDocument(), S ) ; 
          //else
	  //    System.out.println( "Display is " + S ) ;
           PC.firePropertyChange( "Display", null, S);
         
           
          }
      */ 

    }
/**
* adds and IObserver.
*@param iobs   The IObserver that is to be added
*NOTE: This unit only notifies observers of a new DataSet
*/   
 public void addIObserver(IObserver iobs) 
   {SP.addIObserver( iobs );
   }
                
public void  deleteIObserver(IObserver iobs) 
    {  SP.deleteIObserver( iobs );
   }
               
public void  deleteIObservers() 
    { SP.deleteIObservers();
    }
                
public void appendlog(  Document logDoc, Document appendDoc, String Message)
  {if( appendDoc == null) 
      return;
   if( logDoc == null)
      return;
  try{
     
     String Txt = appendDoc.getText( appendDoc.getStartPosition().getOffset(),
                                   appendDoc.getLength());
     if( Txt !=null)
       if( Txt.length() >0)
          if( Txt.charAt( Txt.length() - 1) != '\n')
            Txt = Txt + "\n";
       Txt = "#$ Start"+Message+"\n"+Txt+"#$ End"+Message+"\n";
       logDoc.insertString( logDoc.getLength(), Txt, null);
      }
   catch(Exception s)
       {System.out.println("Error in appendlog="+s); }

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
        line = ScriptProcessor.getNextMacroLine( Commands.getDocument(), line);
        System.out.println( "line ="+line);
     
       }

        if( e.getKeyChar() == KeyEvent.VK_ENTER )	
          if( e.getSource().equals( CP.Immediate ) )
	      {  fixUP( CP.Immediate.getDocument());
                 int i = CP.Immediate.getCaretPosition() ; 
	          Document doc = CP.Immediate.getDocument() ;
                  Element E = doc.getDefaultRootElement(); 
	          int line  = E.getElementIndex( i ) - 1 ;             
		  
	            try{ 
                      if( doc.getText( i , 1 ).charAt( 0 ) >= ' ' )
                        { CP.Immediate.getDocument().remove( i - 1 , 1 ) ;                
		        }
	               }
	            catch( javax.swing.text.BadLocationException s )
                         {}
		    
                  
                 
             
	          
	          CP.SP.resetError() ; 
                 // if( StatusLine != null )
                    // StatusLine.setText( "" ) ; 
                    new IsawGUI.Util().appendDoc(CP.logDoc,"#$ Start Immediate Run");
                   new IsawGUI.Util().appendDoc(CP.logDoc,ScriptProcessor.getLine(CP.Immediate.getDocument(), line));
	          CP.SP.execute1( Immediate.getDocument() , line ) ;
                   new IsawGUI.Util().appendDoc(CP.logDoc,"#$ End Immediate Run"); 
                  if( CP.SP.getErrorCharPos() >= 0 )
                    {//if( StatusLine != null )
                      // new Util().appendDoc(CP.StatusLine.getDocument(), 
                       CP.PC.firePropertyChange("Display", null,"Status: Error " + 
                            CP.SP.getErrorMessage() + 
                           " on line " + line + " character" +CP.SP.getErrorCharPos() ) ; 
		     int p;
                     Element Eline = null;
                     if( (line >= 0) &&( line < E.getElementCount()))
                       {Eline = E.getElement(line);
                        p = Eline.getStartOffset() + CP.SP.getErrorCharPos();
                        if( p > Eline.getEndOffset()) p = Eline.getEndOffset()-1;
                        CP.Immediate.setCaretPosition(p);
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
/** Attempts to fix differences in CR-LF handling of systems
*@param Doc  the Plain document with the errant characters
*/
public static void fixUP( Document Doc )
 {String S;
  try{
     S = Doc.getText( 0 , Doc.getLength() );
     }
  catch( BadLocationException e)
     { return;
     }
  char c;
  if( S.length() <= 0)
    return;
  if( S.length() <= 1)
    {c= S.charAt( 0);
     if( ( c != 10) ||( c != 13))
       return;
     try{
       Doc.remove( 0 , 1 );
       Doc.insertString( 0 , "\n" , null ); 
        }
     catch(BadLocationException e)
       { return;
        }
    }
  for( int i= S.length()-1; i>= 0; i--)
   { c = S.charAt( i );
     if( (int)c <32)
       if( c != '\n')
         if( (c == 10) ||( c==13))
          {boolean remove = false;
          
           if( i + 1  < S.length())
             if( S.charAt( i + 1) == '\n' )
                remove = true;
           if( i > 0)
             if( S.charAt( i - 1 ) == '\n' )
                remove = true;
           try{
              Doc.remove( i, 1 );
              if( !remove )
                  Doc.insertString( i , "\n" , null );
              }
           catch( BadLocationException ee){}
                
          }
   }
  
  
 }

//*****************SECTION:MAIN********************
/**
* Test program for this unit- no args are used
*/
public static void  main( String args[] )
    { 
    java.util.Properties isawProp;
     isawProp = new java.util.Properties(System.getProperties());
   String path = System.getProperty("user.home")+"\\";
       path = StringUtil.fixSeparator(path);
       try {
	    FileInputStream input = new FileInputStream(path + "IsawProps.dat" );
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
      F = new JFrame( "Command Pane" ); 

     P = new CommandPane(); 
     Dimension D = P.getToolkit().getScreenSize();
     F.setSize((int)(.6* D.width) , (int)(.7*D.height) ); 
     F.show() ;  
     F.getContentPane().add( P ); 
   
     F.validate(); 
  }
     
private void setErrorCursor( JTextArea TA, int line, int charpos)
  {int p;
  Element Eline = null;
  Document doc = TA.getDocument();
  Element E = doc.getDefaultRootElement();
  if( (line >= 0) &&( line < E.getElementCount()))
        {Eline = E.getElement(line);
        p = Eline.getStartOffset() + charpos;
         if( p > Eline.getEndOffset()) p = Eline.getEndOffset()-1;
              {TA.requestFocus();
               TA.setCaretPosition(p);
                
              }
         }
  }     


private  class MyMouseListener extends MouseAdapter implements ActionListener,
                                                               Serializable
  {CommandPane CP;
    public MyMouseListener (CommandPane cp )
       {CP = cp;
 
       }
    public void actionPerformed( ActionEvent e )
    {Document doc ; 
     if( e.getSource().equals( CP.Run ) ) 
       { fixUP(CP.Commands.getDocument());
        CP.SP.setDocument(CP.Commands.getDocument());
        
        CP.SP.setDefaultParameters();
        if( CP.SP.getErrorCharPos() >= 0)
          {//new Util().appendDoc( CP.StatusLine.getDocument(), 
             CP.PC.firePropertyChange( "Display", null,"setDefault Error "+
                                 CP.SP.getErrorMessage()+" at position "+
                                    CP.SP.getErrorCharPos()+" on line "+CP.SP.getErrorLine()); 
           CP.SP.setDocument (null);
           return;
          }
        
        //StatusLine.setText("");
        CP.PC.firePropertyChange("Clear", null,null);
        appendlog( logDoc, Commands.getDocument(), "CommandPane Run");
        if( CP.SP.getNum_parameters() > 0 )
	    { if( SelectedFile !=null)
                CP.SP.setTitle( SelectedFile.toString() );
              else
                CP.SP.setTitle( "CommandPane");
              JParametersDialog pDialog =   new JParametersDialog((GenericOperator)(CP.SP), SP, 
                                               new PlainDocument(), null, true);
           }
        else
           CP.SP.getResult();
        
        if( CP.SP.getErrorCharPos() >= 0)
          {//new Util().appendDoc( StatusLine.getDocument(), 
            CP.PC.firePropertyChange("Display", null,"Error "+
                                CP.SP.getErrorMessage()+" at position "+
                                CP.SP.getErrorCharPos()+" on line "+CP.SP.getErrorLine()); 
           CP.SP.setDocument( null);
           CP.setErrorCursor( Commands, SP.getErrorLine(), SP.getErrorCharPos());
           return;
          }
               }
   
     else if( e.getSource().equals( CP.Clear ))
      {if( CP.SP == null ) return;
    
       CP.SP.reset();
       //CP.StatusLine.setText("");
        CP.PC.firePropertyChange("Clear", null,null);
      
      }
    else if( e.getSource().equals( CP.Save ) || e.getSource().equals( CP.Open ))
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
		   Commands.setBorder(
		        new TitledBorder( "Last File:"+filename ));
                   /*Container X = CP.getParent();
                    while( X!= null)  
                     {if( X instanceof JFrame)
                       {((JFrame)X).setTitle(filename);
                         X = null;
                        }
		      else if( X instanceof JScrollPane)
		        { ((JScrollPane)X).setBorder(
			      new TitledBorder( "Prgm Editor:"+filename ));
			   X = null;
			}
                      else
                      X = X.getParent();		      
                     } 
		    */
		    
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
          

             S = S.trim();
             if( S.length() < 1) 
                S = null;
             else if( "\\/".indexOf(S.charAt(S.length() - 1 ))< 0)
                S = S + java.io.File.separator;
             
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
	      {t = CP.indexOf( java.io.File.pathSeparator, s+1);
               if( t < 0) t = CP.length();
               S = CP.substring(s,t) .trim();
               if( S.length() > 0 ) 
                  if ( S.charAt( S.length() -1) != '/') 
                      S = S + "/";
               if( new File( S + "IsawHelp/Command/CommandPane.html").exists())
                 S= S + "IsawHelp/Command/CommandPane.html";
               else S = null;     
           
                }
              }
         if( S == null )S = "http://www.pns.anl.gov/isaw/IsawHelp/CommandPane.html";
         else S = "file:///" + S;
         //H.displayURL( S ) ;
          S= S.replace( '\\','/');
          //System.out.println("Source is"+S); 
          try{
             H = new HTMLPage( S ) ;
             Dimension D = getToolkit().getScreenSize();
             H.setSize((int)(.6* D.width) , (int)(.6*D.height) ); 
             H.show();
             }
           catch(Exception s)
             {//if(CP.StatusLine!=null) 
                 CP.PC.firePropertyChange("Display" , null, "CANNOT FIND HELP FILE");
             //else 
              //     System.out.println("CANNOT FIND HELP FILE");
             }
        
        }
    }// end actionperformed 
 }//End mouseAdapter 

}


