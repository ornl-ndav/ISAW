/*
 * File:  table_view.java 
 *
 * Copyright (C) 2001, Ruth Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at yoJTableur option) any later version.
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
 * Revision 1.19  2002/02/27 16:47:50  rmikk
 * Fixed MergeXvals. It is also now a public static utility method
 *
 * Revision 1.18  2002/02/22 20:37:52  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.17  2002/02/16 15:57:47  rmikk
 * -Fixed error when no Groups were selected and the "UseAll" was selected
 * -Added a file close at the end of the Showw routine so users who enter only
 *  through this Showw routine will have the file closed
 * -Turned off the autoresize of the JTable so the scrolling now works nicely
 *
 * Revision 1.16  2002/02/11 21:41:55  rmikk
 * Major Change to the previous version.  Now all fields,DataSet Attributes, and
 * Data block[0] attributes are used.  The non-GUI interface is also changed and
 * augmented to reflect the difference.
 *
 * Revision 1.14  2001/11/27 18:37:20  dennis
 *    1. If no indices are selected:
 *       a) Indicated in the label with RED letters
 *       b) The header tab shows in the table mode
 *       c) The header information states in all capital letters:
 *          NO SELECTED INDICES  (Ruth)
 *
 * Revision 1.13  2001/09/06 14:18:56  dennis
 * Removed <control-M> characters.
 *
 * Revision 1.12  2001/09/06 14:13:51  dennis
 * - The "Save to File", "Make a Table", and "Write to Console" radio
 *   buttons are now command buttons.  The Display command button is now deleted.
 * - The Prompt when Selecting Group indices is changed to include a
 *   sample entry.  The sample entry in the text box has been deleted.(Ruth)
 *
 * Revision 1.11  2001/08/30 14:35:58  dennis
 * Added a "Copy Selected" JMenuItem to the Select Menu
 * in the table's menu bar. (Ruth)
 *
 * Revision 1.10  2001/08/17 21:17:21  rmikk
 * Fixed major errors in views when groups are paired.
 *
 * Revision 1.9  2001/08/16 17:08:45  rmikk
 * Spelled Pressure Correctly
 *
 * Revision 1.8  2001/08/15 14:13:16  rmikk
 * Added scroll pane to one of the lists.
 * Reset the data sets SetField operator parameters to
 *   default parameters so as not to interfere with subsequent
 *   uses of this operator.
 *
 * Revision 1.6  2001/08/14 01:59:00  rmikk
 * Improved layout.
 * Added line indicating the selected indices.
 * Added the state feature to save and restore state when
 *    traversing several views
 *
 * Revision 1.5  2001/08/13 16:08:47  rmikk
 * Added method setDataSet
 *
 * Revision 1.4  2001/08/13 14:39:36  rmikk
 * The header and the table in the table view is now in
 *    tabbed panes.
 * The JTable column headings in the table part are now the
 *   column headings from the table.
 *
 * Revision 1.3  2001/08/09 21:50:42  rmikk
 * Added Documentation.
 * Added new Fields- Group Index, Raw Angle, Solid Angle
 *
 */

package DataSetTools.viewer.Table;

import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.operator.DataSet.Attribute.*;
import DataSetTools.math.*;
import javax.swing.*;
import DataSetTools.dataset.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import DataSetTools.util.*;
import java.io.*;
import javax.swing.table.*;
import IsawGUI.*;
import java.awt.image.*;

import DataSetTools.components.ParametersGUI.*;

/** A form of the table view that is run without any GUI
 */
public class table_view extends JPanel implements ActionListener 
                                               
  { 

    
// unsuccessful attempt to get arrows to replace Up, Down, Add, Remove in buttons
    protected static int right_arrow[] = {0,1,0,0,0,0,0,0,
                                          0,1,1,0,0,0,0,0,
                                          0,1,1,1,0,0,0,0,
                                          0,1,1,1,1,1,1,0,
                                          0,1,1,1,1,1,1,1,
                                          0,1,1,1,1,0,0,0,
                                          0,1,1,0,0,0,0,0,
                                          0,1,0,0,0,0,0,0};

   protected static int up_arrow[] ={0,0,0,0,1,0,0,0,
                                     0,0,0,1,1,0,0,0,
                                     0,0,0,1,1,1,0,0,
                                     0,0,1,1,1,1,0,0,
                                     0,0,1,1,1,1,1,0,
                                     0,1,1,1,1,1,1,0,
                                     0,1,1,1,1,1,1,1,
                                     1,1,1,1,1,1,1,1};

   protected static int down_arrow[] ={1,1,1,1,1,1,1,1,
                                       0,1,1,1,1,1,1,1,
                                       0,1,1,1,1,1,1,0,
                                       0,0,1,1,1,1,1,0,
                                       0,0,1,1,1,1,0,0,
                                       0,0,0,1,1,1,0,0,
                                       0,0,0,1,1,0,0,0,
                                       0,0,0,1,0,0,0,0};

                  
   
   boolean useAll = false;
   String filename = null;
   JButton  Add , 
            Remove ,
            Up , 
            Down;
 
   JButton  fileView,  
            tableView, 
            consoleView;
  
   
   JButton  selectEdit;
   JCheckBox selectAllEdit;
 

   JLabel  SelectedIndecies;
   
    
   JList   unsel ,
           sel;

   DefaultListModel unselModel , 
                    selModel;    
  //int use[] ,
  //     Nuse[];
   DataSet DSS[];
   boolean DBSeq = false; // DB are paired 
 
   ExcelAdapter EA = null;
   IsawGUI.Util  util;
   JComboBox Order = null;
   JMenuItem JMi= null;
   JMenuItem JCp = null;
  /** Only Constructor without GUI components
  *@param  outputMedia  <ol>The views can be sent to
   *  <li> 0-Console
   *  <li> 1-File
   *  <li> 2-Table
   *  </ol>
   * NOTE: Call the Showw routines to View
   *
  */  
  public table_view( int outputMedia ) 
     { initt();
       mode = outputMedia;
     }//call Showw with args to execute

   private void initt()
    {
     Add = Remove = Up = Down = null;
     unsel = sel = null;
     unselModel = selModel = null;    
     //use = Nuse = null;
     DSS = null;
     mode = 0;
     util = new IsawGUI.Util();
   
    }
   
   /** Constructor that creates the JPanel components that allow for the
   *   selection of the fields that are to be displayed
   *@param  DS[]  The list of data sets to be viewed( for The TableViewer there
   *               is only one data set in this list
   */     
   public table_view( DataSet DS[] )
      { //setLayout( new GridLayout( 1,1));
        setLayout( new BorderLayout( ));
      initt();
      DSS = DS;



// Bottom components
       JPanel JP = new JPanel();
       JP.setLayout( new GridLayout( 1 , 2 ));
// Bottom left
       JPanel JPbl = new JPanel();
       JPbl.setLayout( new BorderLayout() );
       
       unselModel = new DefaultListModel();

       unsel = new JList( unselModel ); 
       unsel.setBorder( BorderFactory.createTitledBorder(
          BorderFactory.createEtchedBorder() , "Possible Fields" ));
       
      
      
         unselModel.addElement( new FieldInfo("X values", new XYData("x")));
         unselModel.addElement( new FieldInfo("Y values", new XYData("y")));
         unselModel.addElement( new FieldInfo("Error values", new XYData("e")));
         unselModel.addElement( new FieldInfo("XY index", new XYData(null)));
         Data db = DSS[0].getData_entry( 0 );
         for( int i = 0; i< db.getNum_attributes() ;i++)
           { Attribute A = db.getAttribute( i );
             Object Val = A.getValue();
             if( (Val instanceof Number) ||(Val instanceof Color)||(Val instanceof String))
                unselModel.addElement( new FieldInfo(A.getName(), new DBattr(A.getName())) );
             else if( A instanceof DetPosAttribute)
                { unselModel.addElement( new FieldInfo("DetPos x", new DSDetPos("DetPos x",
                                    1,0  )) );
                  unselModel.addElement( new FieldInfo("DetPos y", new DSDetPos("DetPos y",
                                    1,1  )) );
                  unselModel.addElement( new FieldInfo("DetPos z", new DSDetPos("DetPos z",
                                    1,2  )) );
                  unselModel.addElement( new FieldInfo("DetPos r", new DSDetPos("DetPos r",
                                    2,0  )) );
                  unselModel.addElement( new FieldInfo("DetPos theta", new DSDetPos("DetPos theta",
                                    2,1  )));
                  unselModel.addElement( new FieldInfo("DetPos rho", new DSDetPos("DetPos rho",
                                    3,0  )));
                  unselModel.addElement( new FieldInfo("DetPos phi", new DSDetPos("DetPos phi",
                                    3,2  )));
                  unselModel.addElement( new FieldInfo("Scat Ang", new DSDetPos("Scat Ang",
                                    -1,-1  )));
                 }
           }
       unselModel.addElement( new FieldInfo( "Group Index", new DBattr(null))); 
       DSFieldString dsf = new DSFieldString ();
       for( int i=0; i< dsf.num_strings(); i++)
         { String S = dsf.getString( i );
           unselModel.addElement( new FieldInfo(S, new DSfield(S)));
                                 
         }
       for( int i=0; i<DSS[0].getNum_attributes();i++)
         {Attribute A  = DSS[0].getAttribute( i );
          Object Val = A.getValue();
          if( Val !=null)
             if( (Val instanceof Number) ||(Val instanceof Color)||(Val instanceof String))
                unselModel.addElement( new FieldInfo(A.getName(), new DSattr(A.getName())) );
          }
          
       
       JPbl.add( new JScrollPane( unsel , 
          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,
           JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ) ,
                       BorderLayout.CENTER );
       JPanel JP2 = new JPanel();
       JP2.setLayout( new GridLayout( 6 , 1 ));
       Add = new JButton( "Add>" );
       Remove = new JButton( "<Remove" );
       JP2.add( new JLabel("" ));
       JP2.add( new JLabel("" ));
       JP2.add( Add );
       JP2.add( Remove );
       JP2.add( new JLabel("" ));
       JP2.add( new JLabel("" ));
       JPbl.add( JP2 , BorderLayout.EAST );
       JP.add( JPbl );
       Add.addActionListener( this );
       Remove.addActionListener( this );
       JPanel JPbr = new JPanel();
       JPbr.setLayout( new BorderLayout() );
       
       selModel = new DefaultListModel();
       sel = new JList( selModel ); 
       //use = new int[ unselModel.getSize()+ 1 ];
       // Arrays.fill( use , -1 );
       sel.setBorder( BorderFactory.createTitledBorder(
          BorderFactory.createEtchedBorder() , "Display Fields" ));
       JPbr.add( sel , BorderLayout.CENTER );
        JP2 = new JPanel( new GridLayout( 6 , 1 ));

      /* Image R_arrw = createImage(new MemoryImageSource(8, 8, 
                                  right_arrow, 0, 8));
       Image U_arrw = createImage(new MemoryImageSource(8, 8, 
                                  up_arrow, 0, 8));
       Image D_arrw = createImage(new MemoryImageSource(8, 8, 
                                  down_arrow, 0, 8));
      */

       Up = new JButton( "Up" );//new ImageIcon( U_arrw));
       Down = new JButton( "Down" );//new ImageIcon( D_arrw))
       JP2.add( new JLabel( "" ));
       JP2.add( new JLabel( "" ));
       JP2.add( Up );
       JP2.add( Down );
       JP2.add( new JLabel( "" ));
       JP2.add( new JLabel( "" ));
       JPbr.add( JP2 , BorderLayout.EAST  ); 
       
       JP.add(JPbr );
       Up.addActionListener( this );
       Down.addActionListener( this );
       JP.setBorder( BorderFactory.createTitledBorder(
                     BorderFactory.createLineBorder( Color.black ),
                     "Display"));
       add( JP ,BorderLayout.CENTER );       

       // Bottom status panel
      
        fileView = new JButton( "Save to File" );
       tableView = new JButton( "Make a Table" );
       consoleView = new JButton( "Write to Console" );   
       fileView.addActionListener( this );  
       consoleView.addActionListener( this );  
       tableView.addActionListener( this );            
     
     
      
      
       SelectedIndecies = new JLabel( );
       setSelectedGRoup_Display( IntList.ToString( DS[0].
                                 getSelectedIndices() ) );

       selectEdit = new JButton( "Select Group indices" );
       selectEdit.addActionListener( this );
       selectAllEdit = new JCheckBox( "Use All Groups" , false );
      
        
       selectAllEdit.addActionListener( this );    
      
       // New Layout
           Box RightPanel = new Box( BoxLayout.Y_AXIS );
           RightPanel.add( new JLabel ("Controls", SwingConstants.CENTER ));
           JPanel Selects = new JPanel( new GridLayout( 3,1 ));
           Selects.add( SelectedIndecies );
           Selects.add ( selectEdit );
           Selects.add( selectAllEdit );
           Selects.setBorder( BorderFactory.createEtchedBorder() );
           RightPanel.add( Selects );

         
           Object[] X ={ new DescrCode("HGT,F","Gr,Time vs Field"),
                         new DescrCode("HT,FG","Time vs Field,Gr")

                        //,new DescrCode("HT,GF","Time vs Gr,Field")
                        //,new DescrCode("HTG,F","Time,Gr vs Field")
                        //,new DescrCode("HG,TF","Gr vs Time,Field")
                         
                         };
             Order = new JComboBox(X  );
             //Order.setEditable(true);
             Order.setSelectedIndex(0);
             
                                            
            JPanel JJ = new JPanel();
            BoxLayout bl = new BoxLayout( JJ, BoxLayout.X_AXIS);
            JJ.setLayout( bl);
            JJ.add( new JLabel("Order"));
            JJ.add (Order);
            RightPanel.add(JJ);
            RightPanel.add( Box.createVerticalGlue() );
 
           JPanel Output = new JPanel( new GridLayout( 3, 1));
           Output.add( fileView );
           Output.add( tableView );
           Output.add( consoleView );
                 
           Output.setBorder( BorderFactory.createEtchedBorder() );
           RightPanel.add( Output );

           
          
           RightPanel.add( Box.createVerticalGlue() );
         /*  RightPanel.setBorder( BorderFactory.createTitledBorder(
                     BorderFactory.createLineBorder( Color.black ),
                     "Controls"));
         */

        add( RightPanel, BorderLayout.EAST );
       //End new layout
       // Old layout
       /*JMenu Options = new JMenu( "  Options   ");
       Options.add( selectEdit );
       Options.add( selectAllEdit );
       Options.add( new JSeparator() );
       Options.add( DBSeqOpt);
       Options.add( DBPairedOpt );
       GG.add( Options );
       selectEdit.addActionListener( this );
       Box BottomPanel = new Box( BoxLayout.X_AXIS );
       BottomPanel.add( SelectedIndecies);
       BottomPanel.add( GG);
       //BottomPanel.add( Options );
       BottomPanel.add( Box.createHorizontalGlue());
       BottomPanel.add( Goto );
       Goto.addItemListener( this );
       BottomPanel.add( GO);
        add(BottomPanel, BorderLayout.SOUTH );
       */
       //End old layout
      
     }
   public void setSelectedGRoup_Display( String S )
     { String Res ="Selected indices:";
       SelectedIndecies.setForeground( Color.black);
       if( S == null)
          {Res = "NO SELECT GROUPS";
           SelectedIndecies.setForeground( Color.red);

           }
       else if( S.length() <1)
          {Res = "NO SELECT GROUPS";
              SelectedIndecies.setForeground( Color.red);

           }
       else if( S.length() >15)
         Res += S.substring( 0,11)+"...";
       else
         Res += S;
       SelectedIndecies.setText( Res+"    " );
      

     } 

 /**
*  Restores the state of this system to the way it was the previous time
* the ViewManager visited this module.<BR>
* The used indices, filename, output choice, useAll and Data Block sequential
* fields are all saved as one String
*/
  public void restoreState( String state )
   {
    int i =0;
    int j,k;
  
    if( state == null )
       return;
    if( state.length() < 1)
       return;
    // used
    i = state.indexOf( ";");
       String nused ;
     selModel.clear();
    j=0;  
    for( i = state.indexOf( ";"); i>=0 ;  )
      {  nused =  state.substring( j, i );
         j=i+1;
         selModel.addElement( getFieldInfo( DSS[0],nused));
         if(i+1<state.length())
           i = state.indexOf( ";",i+1);
         else i=-1;
       }
    
   for(  i = 0; i < StateListeners.size(); i++ )
      (( StateListener)(StateListeners.elementAt( i ))).setState( state );
   }
  
  public void setState( )
   {String  S ="";
    if(selModel==null) return;
    int n=0;
    for( int i=0; (i<selModel.size()); i++)
       { if(selModel.elementAt(i)!=null)
             S += selModel.elementAt(i).toString() +";";
         n++;
       }

   for( int i = 0; i < StateListeners.size(); i++ )
      (( StateListener)(StateListeners.elementAt( i ))).setState( S );
   }


  Vector StateListeners = new Vector();
  /** Add's listeners who can save State information
  *
  *@param  S  A state Listener 
  *
  */
  public void addStateListener( StateListener S)
   {StateListeners.addElement( S );
   }


  /** Removes a listener for State changes
  @param  S  A state Listener 
  *
  */
  public void removeStateListener( StateListener S )
   { StateListeners.remove( S );
   }


  /** Utility that removes the indx-th element of the list
  */
   public void remove( int indx , int listt[] )
     {if( indx < 0 ) 
        return;
      if( indx >= listt.length ) 
        return;
      if( listt == null ) 
        return;
      for( int i = indx ; i+ 1 < listt.length ; i++ )
         listt[ i ] = listt[ i+ 1 ];
      listt[ listt.length - 1 ] = -1;
     }


  /** Utility that inserts  value at position indx in list
  */
 /*  public void insertElementAt( int indx , int value , int list[] )
     {if( indx < 0 ) 
        return;
      if( indx > list.length ) 
        return;
      for( int i = list.length - 1 ; i > indx ; i-- )
         list[ i ] = list[ i - 1 ];
      list[ indx ] = value;
      }
*/
   /**Utility that will add value to the end of list
   */
  /* public void addElement( int value , int list[] )
     {if( list == null ) 
        return;
      if( list.length == 0 ) 
        return;
      if( list[ 0 ] == -1 )
       {list[ 0 ] = value;
        return;
       }
      int k = list.length - 1;   
      for( int i = list.length -  1 ; ( i > 0 )&&( list[ i ] < 0 ) ; i-- )
        {k = i;}
      if( list[ k ] < 0 )
        list[ k ] = value;
      else //no more values
        return;
     }

*/
  
 

   /** Handles the events associated with this JPanel components
   *@param  e  the action event
   *NOTE: This handles the click of the Up, Down, Add, Remove, Select Groups,
   *       file View, Table View, and Console View Buttons
   */
   public void actionPerformed( ActionEvent e  )
   {if( e.getSource().equals( Add ))
     { int indx = unsel.getSelectedIndex();      
        if( indx < 0 ) 
           return;
        Object S = ( unsel.getSelectedValue() );
        
        selModel.addElement( S );
        //addElement( unsel.getSelectedIndex(), use );
        if(indx+1 < unselModel.getSize())
          unsel.setSelectionInterval( indx+1, indx+1);
        unsel.requestFocus();
        setState();
      }
     else if( e.getSource().equals( Remove  ) )
      { int indx = sel.getSelectedIndex();    
        if( indx < 0 ) 
           return;
        Object S = ( sel.getSelectedValue() );
        
        selModel.remove( indx );
        //remove( indx, use );
      
        if( indx < selModel.getSize() )
          sel.setSelectionInterval( indx,indx );
        else
          {indx =selModel.getSize() -1;
            sel.setSelectionInterval(indx,indx);
          }
        sel.requestFocus();
        setState();
       }
      else if( e.getSource().equals( Up ) )
       {int indx = sel.getSelectedIndex();
        if( indx <= 0 ) 
           return;
        Object S = ( sel.getSelectedValue() );
        //int Fieldindx= use[indx];
        selModel.remove( indx );
        //remove( indx, use );
        selModel.insertElementAt( S, indx - 1 );
        //insertElementAt( indx - 1, Fieldindx , use );
        if( ( indx >= 1 ) &&( indx - 1 < selModel.getSize() ))
            sel.setSelectionInterval( indx - 1, indx - 1 );
        sel.requestFocus();
        setState();
       }
      else if( e.getSource().equals( Down ))
       {int indx = sel.getSelectedIndex();
        if( indx < 0 ) 
          return;
        if( indx+ 1 >= selModel.size() ) 
          return;
        Object S = ( sel.getSelectedValue() );
        //int FieldIndx = use[ indx ];
        selModel.remove( indx );
        //remove( indx, use );
        selModel.insertElementAt( S, indx +  1 );
        //insertElementAt( indx +  1 , FieldIndx , use );
        if( indx + 1  < selModel.getSize() )
         sel.setSelectionInterval( indx +  1 , indx + 1 );
        sel.requestFocus();
        setState();
       }
   /* else if( e.getSource().equals( GO ) )
       { //System.out.println( "Sequential?"+ Sequent.isSelected() );
          //mode = OutputMode.getSelectedIndex();
          useAll = selectAllEdit.isSelected();
          DBSeq = DBSeqOpt.isSelected();
          mode = 0;
          if( fileView.isSelected())
             mode = 1;
          else if( tableView.isSelected())
             mode = 2;
          else if( consoleView.isSelected())
           mode = 0;
          Showw( DSS, use , DBSeq, useAll );
       }
   */
      else if( e.getSource().equals( selectEdit ))
          { 
            DataSet DS = DSS[0];               
           /* DataSetOperator op = DS.getOperator( "Set DataSet Field" );        
            if( op == null )
              {System.out.println( " No such operator " );
               return;
               }
            op.setDefaultParameters();
            IntListString IString = new IntListString( );        
            Parameter PP = new Parameter( "Group Indices (e.g. 1,3:5)" ,
                                                IString );        
            op.setParameter( PP , 1 );        
       
            DSSettableFieldString argument = new DSSettableFieldString( 
                                      DSFieldString.SELECTED_GROUPS );         
           */
            SelectIndicesOp newOp =new SelectIndicesOp();
            newOp.setDataSet(DS);
               
            
            JParametersDialog JP = new JParametersDialog( newOp , null ,
                                       null , null );
            //op.setDefaultParameters();
            useAll = false;
           }
       else if( e.getSource().equals( fileView ))
          { mode = 1;
            setState();
	    JFileChooser JFC = new JFileChooser( 
                            System.getProperty( "user.dir" ));
            JFC.setDialogType( JFileChooser.SAVE_DIALOG );
            int retStatus = JFC.showSaveDialog( null );
            if( retStatus == JFileChooser.APPROVE_OPTION )
               {File F = JFC.getSelectedFile();
                filename = F.getPath().trim(); 
                Showw(); 
                try{
                  if( f!=null) f.close();
                  f= null;        
                   }
                catch( IOException ee){}
               }
           setState();
          }
       else if( e.getSource().equals( tableView ))
         { mode = 2;
           Showw();
           setState();
         }
       else if(e.getSource().equals( consoleView ))
        { mode = 0;
          Showw();
          setState();
        }
       else
        {  useAll = selectAllEdit.isSelected();
          //DBSeq = DBSeqOpt.isSelected();
         /* mode = 0;
          if( fileView.isSelected())
             mode = 1;
          else if( tableView.isSelected())
             mode = 2;
          else if( consoleView.isSelected())
           mode = 0;
          */
         
          setState();
        }
    
     }
  /** Sets the data set in case it has changed
  */
  public void setDataSet( DataSet ds )
   {DSS[0] = ds;
   }


  /** Sets the filename where the File view will be written.
  */
  public void setFileName( String filename )
   { this.filename = filename;
   }

  // Ouput directors
  FileOutputStream f = null;
  JFrame  JF;
  JTable JTb;
  JTextArea HeaderInfoPane;
  DefaultTableModel DTM;
  Vector V = new Vector();
  int mode= 0;
  boolean startline = true;
  boolean columnHeader = false; //for table view feature
                               
  private void initOutput( DataSet D, int[] Sel )
    { //mode = OutputMode.getSelectedIndex();
     startline = true;
     if( mode == 0 )
       {}
     else if( mode == 1 )
      {
       String S;
       if( filename == null )
          {   
            S = D.toString()+ ".dat";
            int k = S.indexOf( ":" );
            if( k >= 0 ) 
               S = S.substring( k+ 1 );
           }
       else
             S = filename;                        
      
       File fl = new File( S );
       try{ 
            f = new FileOutputStream( fl );
          }
       catch( IOException u ){f = null;
             System.out.println( "file init="+ u+ ","+ 
             filename );}

      }
    else
     { JF = new JFrame( D.toString() );
        
       JMenu JM = new JMenu( "Select" );
       JMi = new JMenuItem( "All" );
       JCp = new JMenuItem("Copy Sel");
       JM.add( JMi );
       JM.add( JCp);
       JMenuBar JMB= new JMenuBar();
       JMB.add( JM );
       JF.setJMenuBar( JMB );
       JMi.addActionListener( new MyActionListener() );
       JCp.addActionListener( new MyActionListener());
       // Tabbed pane
       JTabbedPane JtabPane = new JTabbedPane();
       // Table Pane
       DTM = new DefaultTableModel();
       JTb = new JTable( DTM );
       JTb.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
        EA = new ExcelAdapter( JTb );
       // The HeaderInfo Pane
       HeaderInfoPane = new JTextArea (20, 50 );
       // Glue together
       JF.getContentPane().add( JtabPane );
       JtabPane.add( "Table",new JScrollPane( JTb ,
                                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ,
                               JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS  ));
       JtabPane.add( "Header", new JScrollPane( HeaderInfoPane ,
                                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ,
                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED  ));
    
       JF.setSize( 400, 400 );
       JF.show();
       JF.validate();
       row = 0;
       col = 0;
       V = new Vector();
       if((Sel!=null)||(useAll))
           JtabPane.setSelectedIndex( 0);
       else if( Sel.length <=0)
            JtabPane.setSelectedIndex( 0);
       else
           JtabPane.setSelectedIndex(1);
       
     }
   }



  int row = 0;
  int col = 0;
  private void OutputField( Object res )
    {
     String S;
     if( res != null )
         S = new NexIO.NxNodeUtils().Showw( res ) ;
     else
         S = "";
     //System.out.print( "Output field mode="+ mode );
     if( mode == 0 )
       if( startline )
           System.out.print( S );
       else
            System.out.print( "\t"+ S );
     else if( mode == 1 )
      try
        {if( f != null )
           {if( startline )
              f.write( S.getBytes() );
            else
              f.write( ("\t"+ S ).getBytes() );
           }
        }
     catch( IOException ss ){}
    
     else if( columnHeader)
      {if( DTM.getColumnCount() <20)
          DTM.addColumn( S );      
      }
     else if( V.size() < 19)
       V.addElement( S );
     else if( V.size() ==20)
        V.addElement("....");

     startline = false;
    }



  private void OutputEndField()
   { 
    if( mode == 0 )
       System.out.println( "" );
    else if( mode == 1 )
       try{if(  f != null )
            f.write( "\n".getBytes() );
          }
       catch( IOException sss ){}
    else if( header)
      {String S="";
       for( int i = 0; i <V.size(); i++)
          S = S +(String)(V.elementAt( i));
      

       V = new Vector();
       util.appendDoc( HeaderInfoPane.getDocument(), S );
      }
    else if( !columnHeader )  
     {
      for( int i = DTM.getColumnCount() ; i < V.size() ; i++ )
          DTM.addColumn( new Integer( i ) );
       DTM.addRow( V );
       
       V = new Vector();
      }
    startline = true;
   }


 private void OutputClose()
  {
   if( mode == 0 )
    {}
   else if( mode == 1 )
    try{
          if( f!= null) f.close();
          f = null;
          filename = null;
       }
    catch( IOException ss ){}
   else
     V = new Vector();
   
  }
 
  /** Converts an array of Fields in String form to an int array
  *   that is used in the Showw method
  *   #Showw(DataSetTools.dataset.DataSet[], DefaultListModel, boolean, int[])
 */
 /* private int[] Convertt( String fields[], String Fields[] )
     {int Res[];
     
      if( fields == null )
        return null;
      if( fields.length < 1 )
        return null;     
      Res = new int[ fields.length + 1 ];
      Res[ fields.length ] = -1;        
      for( int i = 0 ; i < fields.length ; i++ )
       { Res [ i ] = -1;            
         for( int j = 0 ; ( j < Fields.length )&&( Res[ i ] < 0 ) ; j++ )
            if( Fields[ j ].equals( fields[ i ] ) )
              Res[ i ] = j;
               
         if( Res[ i ] < 0 )
           return null;
       }
     return Res;
     }

 */

  boolean header = false;
  private void MakeHeaderInfo( DataSet DSS[] , boolean UseAll, int[] SelInd )
    {String S;
     int i;
     header = true;
     NexIO.NxNodeUtils nd = new NexIO.NxNodeUtils();
     // 1st line data set name( s )
     S = "#Data Set";
     if( DSS.length > 1 )
        S += "s";
     S = S+ ":";
     for( i = 0 ; i < DSS.length ; i++ )
        {S = S + DSS[ i ].toString();
         if( i+ 1 < DSS.length )
           S = S +" ; ";
        }
     OutputField( S );
     OutputEndField();
    //Next Line are selected groups for each
    OutputField( "#Selected Groups" ); OutputEndField();
    for( i = 0 ; i < DSS.length ; i++ )
     { String SS ="NO SELECTED INDICES";
      /* if( DSS[i].getNumSelected()>0)
              SS= nd.Showw( DSS[ i ].getSelectedIndices() );
      */
       if( SelInd != null) if( SelInd.length > 0)
           SS= nd.Showw( SelInd);
        
       OutputField( "#     "+ DSS[ i ].toString()+ ":"+  SS);
                        
      OutputEndField();
     }
    // Next Lines are the operation logs;
    OutputField( "#Operations" ); OutputEndField();
    for( i = 0 ; i< DSS.length ; i++ )
      {DataSet DS = DSS[ i ];
       S = "#     "+ DS.toString()+ ":";
       OperationLog oplog = DS.getOp_log();
       if( oplog != null )
       for( int j = 0 ; j < oplog.numEntries() ; j++ )
         { S += oplog.getEntryAt( j );
           if( j+ 1 < oplog.numEntries() )
              S += "\n             ";
         }
       OutputField( S ); OutputEndField();
      }
    header = false;
    }
 
  /** Views the data for the data set.  All information has been setup from
  *   the GUI
  */
  public void Showw()
      { //useAll = UseAll.isSelected();
        /*mode = 0;
        if( fileView.isSelected())
           mode = 1;
        else if( tableView.isSelected())
           mode = 2;
        else if( consoleView.isSelected())
           mode = 0;
        */
        Object O = Order.getSelectedItem();
        
        String order;
        if( O instanceof String) 
              order = (String)O;
        else 
           order=((DescrCode) O).getCode();
        Showw( DSS, selModel, order, useAll, null );
      }


/** Used to set up the ListModel argument in the Showw method. This argument must
*   be a ListModel of FieldInfo.<P> 
*   This method along with the Showw Method gives a non GUI based entry into this package<P>
*
*   The field must be "x", "y","e" ,"Group Index", or "XY index",or a data set or data block
*   attribute name or a String in DSFieldString 
*      fields x,y, error, group index, or index on the x or y values
*
*/
public FieldInfo getFieldInfo( DataSet DS,String field)
  {if( field.equals("X values"))
       return new FieldInfo("X values", new XYData("x"));
   if( field.equals("Y values"))
       return new FieldInfo("Y values", new XYData("y"));
   if( field.equals("Error values"))
        return new FieldInfo("Error values", new XYData("e"));
   if(field.equals("XY index"))
       return new FieldInfo("XY index", new XYData(null));
   if(field.indexOf("DetPos ")==0)
     { int n1,n2;
       String S =field.substring( 7).trim();
       n1 = ";x;y;z;r;theta;rho;phi;Scat Angle;".indexOf(";"+field+";");
       if( n1 < 0) return null;
       if( n1 <6)
          {n1=0; n2 = n1/2;}
       else if( n1 <13)
          {if(n1<7) n2=0; else n2=1;
           n1=1;
          }
       else if( n1 <15)
         {n1 = 2; n2= 0;}
       else if( n1 < 19)
         {n1 = 2; n2 = 3;}
        else {n1=-1; n2=-1;}

       return new FieldInfo( field, new DSDetPos(field, n1,n2));
      }
     if(field.equals("Group Index") )return  new FieldInfo( "Group Index", new DBattr(null)); 
     DSFieldString dsf = new DSFieldString ();
     for( int i=0; i< dsf.num_strings(); i++)
         { String S = dsf.getString( i );
           if( field.equals(S)) return ( new FieldInfo(S, new DSfield(S)));
                                 
         }
  
    Data db = DS.getData_entry( 0 );
    for( int i = 0; i< db.getNum_attributes() ;i++)
     { Attribute A = db.getAttribute( i );
       Object Val = A.getValue();
       if( (Val instanceof Number) ||(Val instanceof Color)||(Val instanceof String))
         if( A.getName().equals(field)) return new FieldInfo(A.getName(), new DBattr(A.getName()));
     }
   return null;
   }

/** Will be the new Showw
*The GENERAL entry point to the Table View.  The Mode must be set with constructor.
*
*@param   DSS[] the Array of data sets to be viewed. Only an array with one element has been tested
*@param   selModel <ul> Stores the fields to be displayed. Use getFieldInfo to get proper
*                        elements in the selModel<ul>
*@see  #getFieldInfo(DataSet, String)
*@param order  <ul> Indicates which index will be looped first, second, etc.  Eg.
*           <BR> HGT,F : 1st rows Hist 0, then Hist 1. In Hist 0 group the first rows are Group 0, then 1
*                        In the Group 0 rows the first rows are times,  The comma means the selected
*                        Fields are listed across the colums<P>
*                HT,GF : 1st rows are Hist 0, then 1.. In Hist i, the first rows are the first time, 
*                   2nd time,etc. The 1st columns deal with Group 0, then Group 1... In the Group 0
*                   columns Field 1 then selected Field2 are listed in that order.<P>
*              In the future G may be replaced by  I and J for the row and column of area detectors.
*            </ul>
*@param  UseAll  A flag that when true uses all the data blocks without changing the selected data
*                blocks.   
*
*@param SelIndecies  If not null, these indecies will be used in place of the selected indecies. THE
*                     LIST MUST BE INCREASING.
*/ 
public void Showw1( DataSet DSS[], DefaultListModel selModel,  String  order, 
                                boolean UseAll, int[] SelIndecies )
  {
   boolean has_Xcol=false;
   boolean XYCol=false;boolean DBCol=false;
   int comma =order.indexOf(",");
   if( comma < 0) return;
   if( comma >= order.length() -1) return;
   if( selModel.getSize() <= 0)
     {DataSetTools.util.SharedData.status_pane.add("No Items have Been Selected");
      return;
      }

   //****If there are no fields that involve time, the size of the output is reducec
   for( int i = 0 ; i < selModel.getSize() ; i++ )
      { FieldInfo V =(FieldInfo)(selModel.getElementAt( i ));
        if( V.getDataHandler() instanceof  XYData)
            {XYCol=true;
             if(V.toString().equals("X values")) has_Xcol = true;
             }
        else if( V.getDataHandler() instanceof DBattr)
              DBCol = true;
        else if( V.getDataHandler() instanceof DSDetPos)
              DBCol = true;
        
      } 
         
    
 
    //**** Set up Group List.  It is either selected Groups or the SelIndecies in the parameter
    int Groups[];
    Groups = null;
    if( !useAll)
      Groups = DSS[0].getSelectedIndices();
    if( SelIndecies != null)
      Groups= SelIndecies;
    initOutput( DSS[ 0 ], Groups );
    //*** Merge the xvals( times) to get correct correspondences
    float xvals[];
    xvals = null;
    int k = order.indexOf("T");
    int kG = order.indexOf("G");
    int kI = order.indexOf("I");
    int kJ = order.indexOf("J");
    if( (k>comma) || (k<kG)|| (k<kI)||(k<kJ))
       {xvals = null;//DSS[0].getData_entry(0).getX_scale().getXs();
        for( int i =0; i<DSS[0].getNum_entries(); i++)
           xvals = MergeXvals( i,DSS[0],xvals, useAll, Groups);
       }


   

    //**** Set up the Permutation from the natural loop order "HGTF" 
    int Permutations[];
    Permutations = new int[4];
    Permutations[0]= 0;
    for( int i = 1;i < 4;i++)
      {char c = "HGTF".charAt(i);
       int k6 =order.indexOf(c);
       if( k6 > comma) k6--;
       
       Permutations[k6] = i;
      } 
   
  //*****Now get column headers going
   Nextt Nexts[];
   MakeHeaderInfo( DSS , UseAll , Groups);
   header = false;
   columnHeader = true;
   boolean done = false;
  
  //****  First time the column headers are created. 2nd time the body of the table is created
  while( ! done)
   { 
    Nexts = new Nextt[ order.length() -1];
    Nexts[0]= new Nextt(0,0);
    if( (kG < comma)&&columnHeader) 
        Nexts[1] = new Nextt(0,0);
    else
    {
     if( useAll &&(DBCol || XYCol))
       Nexts[1] = new Nextt(0, DSS[0].getNum_entries()-1);
    /* else if((SelIndecies==null)&&(DBCol ||XYCol) )
       Nexts[1] = new Nextt(0, DSS[0].getNumSelected()-1); //Next_Gr(DSS[0] );
     else if((SelIndecies!=null)&&(DBCol ||XYCol) )
        Nexts[1] = new Nextt(0, SelIndecies.length);
    */
     else if( DBCol || XYCol) 
        Nexts[1] = new Nextt(0, Groups.length-1);
     else 
        Nexts[1] = new Nextt(0,0);
     }
   
   if( !XYCol)
     Nexts[2] = new Nextt(0,0);
   else if( columnHeader && (order.indexOf("T")<comma))
     Nexts[2] = new Nextt(0,0);
   else if( xvals != null)
     Nexts[2] = new Nextt(0, xvals.length -1);
   else
     Nexts[2] = new Next_t( DSS, 0 , useAll);

   Nexts[3] = new Nextt( 0, selModel.getSize()-1);

   int i = "HGTF".indexOf(order.charAt(comma +1 ));
   Nexts[i].setEndRecordMake( true);                  
   
   
    
    if( false)  //Debug info
      {System.out.println("Perms="+Permutations[0]+","+Permutations[1]+","+Permutations[2]
          +","+Permutations[3]);
       
       System.out.println("Nexts"); 
       for(  i = 0; i<4;i++)
         System.out.println("     "+i+":"+Nexts[i].min+","+Nexts[i].max+"::"+Nexts[i].getClass());
      }
    
    opnGroup op = new opnGroup( DSS,Permutations, xvals, Groups,order);
    MNestedForLoops lp = new MNestedForLoops(Nexts, op, Permutations);
    lp.execute();
    if( columnHeader) 
     columnHeader = false;
    else done = true;
    }
   
    OutputClose();
   }


  /** Deprecated Views the data for the data set.  This is used for the non-GUI access
 *    to this viewer
 *@param  DSS  The list of data sets to be table-viewed.
 *@param  used  The list of used fields
 *@param  DBSeq  <OL><LI>true if the Data Blocks are viewed sequentially
 *                    ( one above the
 *              other. <LI>If false, the Data blocks are Viewed left to right
                </ol>
 *@param UseAll  <UL>true if all data blocks are to be used, otherwise only the
 *               selected data blocks will be used</ul>
 */
public void Showw( DataSet DSS[], DefaultListModel selModel,  String  order, 
                                boolean UseAll, int[] SelIndecies )
 // public void Showw1( DataSet DSS[], int used[],  boolean  DBSeq, 
  //                              boolean UseAll )
    {int ncols = selModel.size();
    // int i = 0;     
    // while(( i < used.length ) &&  ( used[ i ] >= 0 ) )
     //   {i++;
     //    ncols++;
     //   }
     
     boolean has_Xcol,XYcol,DBcol;
     DBcol = XYcol = has_Xcol = false;
     boolean DBSeq=true;
     if(order !=null) if( order.indexOf(',')<3)DBSeq = false;
     for(int i = 0 ; i < selModel.size() ; i++ )
      { FieldInfo V =(FieldInfo)(selModel.getElementAt( i ));
        if( V.getDataHandler() instanceof  XYData)
            {XYcol=true;
             if(V.toString().equals("X values")) has_Xcol = true;
             }
        else if( V.getDataHandler() instanceof DBattr)
              DBcol = true;
        else if( V.getDataHandler() instanceof DSDetPos)
              DBcol = true;
      }          
     
    
   
     if(SelIndecies == null)
         if(!useAll) SelIndecies= DSS[0].getSelectedIndices() ;
         else 
           {SelIndecies = new int[DSS[0].getNum_entries()];
            for(int k=0;k<DSS[0].getNum_entries();k++)SelIndecies[k]=k;
            }
     
     if( SelIndecies == null) return;
    initOutput( DSS[ 0 ] ,SelIndecies);
    columnHeader = false;
    MakeHeaderInfo( DSS , UseAll ,SelIndecies);
    header = false;
    for(  int i = 0 ; i < DSS.length ; i++ )
    if( DBSeq )   
      {
       DataSet DS = DSS[ i ]; 
       columnHeader = true;      
       for( int ii = 0 ; ii < selModel.size() ; ii++ )
          OutputField( ((FieldInfo)(selModel.getElementAt( ii))).toString() );
       OutputEndField();
       columnHeader = false;
       for( int j = 0 ; j < SelIndecies.length ; j++ )
        // if( UseAll || DS.getData_entry( j ).isSelected() )
        if( SelIndecies[j] < DS.getNum_entries())
          {Data DB = DS.getData_entry( SelIndecies[j] );
           float xx[ ];
           if( XYcol )
               xx = DB.getX_scale().getXs();
           else
               xx = new float[ 1 ];
           
           for( int k = 0 ; k < xx.length ; k++ )
             { for ( int l = 0 ; l < selModel.size() ; l++ )                   
                   { OutputField( ((FieldInfo)(selModel.getElementAt(l))).
                                       getDataHandler().getVal( DSS, i, SelIndecies[j], k ) );
                 
                    }
               OutputEndField();
             }             
          }
         OutputClose();
      }
    else //paired
     {
      float xvals[]; 
      xvals = null;     
      DataSet DS = DSS[ i ];
      
      if( XYcol )           
            xvals = MergeXvals( 0, DS, null, UseAll ,SelIndecies);                   
      int n = 1;
      if( xvals != null )
         n = xvals.length;
      if( n <= 0 )
         n = 1;
      int count =  0;   
      columnHeader = true;
      if( has_Xcol)
	   OutputField( "x");
      
      for( int jj = 0 ; jj < SelIndecies.length ; jj++ )
        if( SelIndecies[jj] < DS.getNum_entries())
          { int j=SelIndecies[jj];
            Data DB = DS.getData_entry( j );
           //if( UseAll || DB.isSelected() )
            { count = 1;                     
              for( int l = 0 ; l < selModel.size() ; l++ )                
                { FieldInfo finfo = (FieldInfo)selModel.getElementAt( l) ;
                  if( ( count == 0 )||
                          (! finfo.toString().equals("X values")) )  
                      OutputField( "Gr"+ DB.getGroup_ID()+ ":"+ 
                                       finfo.toString() );
                                    
                  if( finfo.toString().equals("X values") ) 
                      count = 1;                                        
                   
                }
             }//if DB.isSelected                        
            }
       OutputEndField();
       columnHeader = false;
     
      for( int k = 0 ; k < n ; k++ ) //xvals
       {float x = Float.NaN;
        if( xvals != null )           
           x = xvals[ k ];
        count = 0;
       
        if( has_Xcol)
	   OutputField( new Float( x));
           
          
        
        for( int jj = 0 ; jj < SelIndecies.length ; jj++ )
          { int j= SelIndecies[jj];
            Data DB = DS.getData_entry( j );
           //if( UseAll ||  DB.isSelected() )
            { float xx[];
              xx = DB.getX_scale().getXs();             
             
              int k1  = contains( xx,  x );//should be true
              if( xvals == null )
                    k1 = 0;  //no xvals needed
              count = 1;
              for( int l = 0 ; l < selModel.size() ; l++ )
                { FieldInfo finfo = (FieldInfo)(selModel.getElementAt(l) );
                  if( k1 >= 0 )
                    {if(( count == 0 )||
                          ( !finfo.toString().equals("X values") ))  
                       {OutputField( finfo.getDataHandler().getVal( DSS,  i, j , k1 ));   
                       }
                    }
                 else if( !finfo.toString().equals("X values")  )
                    {OutputField( "  " );
                     
                    }

                 if( finfo.toString().equals("X values")  ) 
                   { count = 1;                                        
                   }
                }//for l    
                                
            } 
          }//for j
          OutputEndField();
        


       }//for k
       OutputClose();
      }//end paired Data Blocks

    }

  /** Creates a float[] with "all" the xvalues in groups of the SelIndices 
  * Parameter UseAll does NOT work
  *@param  dbi    data block, this is recursive so start at 0. It will do all
  *@param  DS    the data set
   *@param xvals  Result so far for the dbi's less than this dbi
   *@param      SelIndices The index of the groups to have their xvalues merged.
   *@result  The set of merged xvalues or null
   *NOTE: Two xvalues are the same at a given point if they are within 1/20th of the
   * average length of an interval.
  */

  public static float[] MergeXvals ( int dbi , DataSet DS , float xvals[]
                               , boolean UseAll, int[] SelIndices )
    { if(SelIndices == null)
         SelIndices = DS.getSelectedIndices() ;
     if(SelIndices == null) 
         if(!UseAll)return xvals;
     if( UseAll &&(dbi> DS.getNum_entries()))
         return xvals;  
     else if( UseAll)
          {}
     else if( dbi >= SelIndices.length )
        return xvals; 
     if( dbi < 0) return xvals;
     int db;
     if( UseAll) db=dbi;
     else db=SelIndices[dbi];
     if( xvals == null )       
       { Data DB = DS.getData_entry( db );
         //if( !DB.isSelected() )
          // return MergeXvals( dbi+1, DS, xvals, UseAll, SelIndices);
         XScale XX = DB.getX_scale();
                
         return MergeXvals( dbi + 1 , DS , XX.getXs() , UseAll, SelIndices );
        }
     Data DB = DS.getData_entry( db );
   /*  if( UseAll)
         {}
     else if( DB.isSelected())
         {}
     else
        return MergeXvals( dbi+ 1, DS , xvals , UseAll, SelIndices );
    */
     XScale XX = DB.getX_scale();
     
     float xlocvals[];
     xlocvals = XX.getXs();
     float Delta = 
        ( xvals[ xvals.length - 1 ] - xvals[ 0 ] )/xvals.length /20.0f;       
     if( Delta < 0 ) Delta = 0.0f;
     int j = 0; 
     int i = 0;
     int n = 0;
     while( ( i < xvals.length ) ||( j < xlocvals.length ) )
       { 
         if( i >= xvals.length )
           {j++;
            n++;}
         else if( j >= xlocvals.length )
           {i++;
            n++;}

         else if( xvals[ i ] < xlocvals[ j ] - Delta )
          { i++; 
            n++;}
        else if( xvals[ i ] > xlocvals[ j ]+Delta )
          {j++;
           n++;}
        else
          {i++;
           j++;
           n++;}
       }  
     float Res[];
     Res = new float[ n  ];
     j = 0; i = 0;
     n = 0;
     while( ( i < xvals.length ) ||( j < xlocvals.length ) )
       { if( i >= xvals.length )
           {Res[ n ] = xlocvals[ j ];j++;n++;}
         else if( j >= xlocvals.length )
           {Res[ n ] = xvals[ i ]; i++;n++;}

         else if( xvals[ i ] < xlocvals[ j ] - Delta )
          { Res[ n ] = xvals[ i ];i++; n++;}
        else if( xvals[ i ] > xlocvals[ j ]+ Delta )
          {Res[ n ] = xlocvals[ j ];j++;n++;}
        else
          {Res[ n ] = ( xvals[ i ]+ xlocvals[ j ] )/2.0f;i++;j++;n++;}
       }
     return MergeXvals ( dbi+ 1 ,  DS , Res , UseAll,  SelIndices );

    }


  /** Determins whether an x value is "in" a float array xx. By "in" it must be close enough
  *   to an entry
  */
  public int contains( float xx[],  float x )
    {float delta;
     if( xx == null ) 
       return -1;
     if( xx.length <= 0 )
       return -1;
     delta = ( xx[ xx.length - 1 ] - xx[ 0 ] )/ xx.length/ 20.0f;     
     for( int i = 0 ; i< xx.length ; i++ )
        {
         if( java.lang.Math.abs( xx[ i ] - x ) < delta )
           return i;
         else if(  xx[ i ] > x )
            return -1;
     
        }    
     return -1;
    }

  // Interface for all data handlers
  class DataHandler
    {
     String arg;
   
     public DataHandler( String argument)
         {arg = argument;
         }
     public Object getVal( DataSet DSS[] , int DS_index , int DB_index , 
                           int XY_index )
        {return null;
        }
     public String getArg(){ return arg;}
    }

  // Class of entries in the sel and unsel listbox entries
  class FieldInfo
    {  String name;
       DataHandler dh;
       public FieldInfo( String name, DataHandler dh)
         { this.name = name;
           this.dh = dh;
         }
       public String toString()
         {return name;}
       DataHandler getDataHandler()
          {return dh;}

     }

  // Data Handler for the Detector Position Attribute of a Data Block
  class DSDetPos extends DataHandler
    { int mode, pos;
      public DSDetPos( String arg, int mode, int pos)
          {super( arg);
           this.mode = mode;
           this.pos = pos;

           }
      public Object getVal( DataSet DSS[] , int DS_index , int DB_index, 
                                    int XY_index )
          {  

              

              GetDataAttribute SF = new GetDataAttribute( DSS[DS_index] , 
                                              new Integer( DB_index  ),
                                              new AttributeNameString( Attribute.DETECTOR_POS ));
             Object O = SF.getResult();
             if( O instanceof DetectorPosition)
                 { float xx[];
                   xx=null;
                   if( mode == 1)
                     xx = ((DetectorPosition)O).getCartesianCoords();
                   else if( mode == 2)
                     xx = ((DetectorPosition)O).getCylindricalCoords();
                   else if(mode == 3)
                     xx = ((DetectorPosition)O).getSphericalCoords();
                   else if( mode == -1)
                     return new Float(((DetectorPosition)O).getScatteringAngle());
                   else return null;
                   
                   if( (xx == null) ||(pos < 0) ||(pos > 2)) return null;
                       return new Float( xx[pos]);
                  
                  }
             else return null;
            }     
     }

  // Data Handler for Data Set Fields
  class DSfield extends DataHandler
     {public DSfield( String arg ){super( arg ) ;}

      public Object getVal( DataSet DSS[] , int DS_index , int DB_index, 
                                    int XY_index )
         {if( arg == null ) 
               return new Integer ( DS_index );
          if( ( DSS == null ) ||( DS_index < 0 ) ) 
             return null;
          if( DS_index >= DSS.length ) 
              return null;
          DataSet D = DSS[ DS_index ];
          GetField SF = new GetField( D, new DSFieldString( arg ));
          Object X = SF.getResult();
          if( X instanceof ErrorString )
               return null;
          return X;
        } 
   
   }// DSfield


  // Data Handler for DataSet Attributes
  class DSattr extends DataHandler
     {public DSattr( String arg )
         {super( arg ) ;
         }

      public Object getVal( DataSet DSS[], int DS_index,  int DB_index, 
                     int XY_index )
        {if( (DSS == null ) ||( DS_index < 0 ) ) 
            return null;
         if( DS_index >= DSS.length ) 
             return null;
         DataSet D = DSS[ DS_index ];
         GetDSAttribute SF = new GetDSAttribute( D , 
                                  new AttributeNameString( arg ));
         Object X = SF.getResult();
         if( X instanceof ErrorString ) 
            return null; 
         return X;
        }    
   }//DSattr


  //Data Handler for Data Block Attributes
  class DBattr extends DataHandler //Detector position args do special
     {public DBattr( String arg )
          {super( arg ) ;
          }

      public Object getVal( DataSet DSS[], int DS_index,  
                            int DB_index, int XY_index )
        {
          if( arg == null ) 
              return new Integer( DB_index );
          if( (DSS == null ) ||( DS_index < 0 )||( DB_index < 0 ) ) 
	    return null; 
          if( DS_index >= DSS.length ) 
            return null; 

          DataSet DS = DSS[ DS_index ];
        
          if( DB_index >= DS.getNum_entries() ) 
             return null;
          int match = ";x;y;z;r;t;p;R;".indexOf( ";" +arg +";" );
          String args;
          if( match >= 0 )
            args =  Attribute.DETECTOR_POS;
          else 
            args = arg;
          GetDataAttribute SF = new GetDataAttribute( DS , 
                                              new Integer( DB_index  ),
                                              new AttributeNameString( args ));
          Object Result = SF.getResult();
          if( Result == null ) 
             return null ;
          if( Result instanceof ErrorString ) 
              return null;
          if( !(Result instanceof DetectorPosition )) 
             return Result;

          float coords[];
          if( match < 5 )
            coords = (( DetectorPosition )Result ).getCartesianCoords();
          else if( match < 9 )
            coords = (( DetectorPosition )Result ).getCylindricalCoords();
          else
            coords = (( DetectorPosition )Result ).getSphericalCoords();
          if( ( match == 0 ) ||( match == 6 )||( match == 12 )) 
             return new Float( coords[ 0 ] );
          if( ( match == 2 ) ||( match == 8 ) ) 
             return new Float( coords[ 1 ] );
          return new Float( coords[ 2 ] );        
        }    
   }

  //Data Handler for XY data.
  class XYData extends DataHandler
    {public XYData( String arg )
        {super( arg ) ;
        }
 
      public Object getVal( DataSet DSS[], int DS_index,  int DB_index, 
                 int XY_index )
         { 
          if( arg == null ) 
             return new Integer( XY_index );
         
          if( ( DSS == null ) ||( DS_index < 0 )||( DB_index < 0 ) 
                         ||( XY_index < 0 )) 
             return null;
         
          if( DS_index >= DSS.length ) 
                return null;
         

          DataSet DS = DSS[ DS_index ];
     
          if( DB_index >= DS.getNum_entries() ) 
             return null;
          
          Data DB = DS.getData_entry( DB_index );
          float Res[];
          if( arg.equals( "x" ))
             Res = ( DB.getX_scale().getXs() );
          else if( arg.equals( "y") )
             Res = DB.getY_values();
          else if( arg.equals( "e" ))
             Res = DB.getErrors();
          else 
             return null; 
         
          if( XY_index < 0 )
             return null;
         
          if( Res == null ) 
              return null;
        
          if( XY_index >= Res.length ) 
            return null;
          
          return  new Float( Res[ XY_index ] );
         }

   }

  // A specialized DataHandler for XY error data. Here XY index is an index into a fixed
  // set of xvals.  It gets corresponding y vals, errors, etc.
  class XYDataVal extends XYData  // index is index into xvals
    {float xvals[], dx;
      public XYDataVal( float xvals[], String arg)
        { super( arg );
          this.xvals = xvals;
          dx = 0;
          if( !(xvals == null))
            if(xvals.length > 1)
              {dx = xvals[1]-xvals[0];
               for(int i =1; i+1 < xvals.length; i++)
                 if( xvals[i+1]-xvals[i] <dx)
                    dx = xvals[i+1]-xvals[i];
               } 
            
        }
      public Object getVal( DataSet DSS[], int DS_index,  int DB_index, 
                 int XY_index )//XY_index is now index into xvals not ..
        {//Find closest in DSS[DS_index].DataEntry[dB_index]
         if( XY_index < 0)
            return null;
         if( xvals == null)
            return null;
         if( XY_index >=xvals.length)
             return null;
         float x = xvals[ XY_index];
         int i = java.util.Arrays.binarySearch(xvals,x);
         if( i>= xvals.length){i--;}
         else if(i < 0) return null;
         else if( java.lang.Math.abs(xvals[i] - x) <dx/8.0)  {}
         else
           { if( i > 0)
                if( java.lang.Math.abs(x - xvals[i-1]) < dx/8.0) i = i-1;
             else if( i+1 < xvals.length-1)
               if(java.lang.Math.abs( xvals[i+1]-x) < dx/8.0 ) i=i+1;
             else 
               return null;
            }
         return super.getVal( DSS, DS_index, DB_index, i);
         }
      
    }

  //General Next Handler that increments from min to max
  class Nextt implements INextHandler
    { int min,max, current;
      boolean make = false;
     public Nextt(){}
     public Nextt( int min, int max)
       {this.min=min;
        this.max = max;
       }
     public int next()
       { current++;
         //if( make)System.out.println("   in Nextt make is true");
         if( current > max)
           { if( make)
                OutputEndField();
             
             return -1;
            }
         
         return current;
       }
   
     public int start()
       { current = min;
         return current;
        }
     public void setEndRecordMake( boolean make)
        {this.make = make;
        }
      
    }

  // Next index in the SelectedIndex of Groups array
  class Next_Gr extends Nextt // Next selected Group's index 
   { DataSet DS ;
     int SelInd[];
     public Next_Gr( DataSet DS)
      { this.DS = DS;
        SelInd = DS.getSelectedIndices();
        if( SelInd == null)
          {min = -1;
           max = -1;
           }

        min=0;
        max = SelInd.length-1;
       }
     public int start()
       { current = min;
         if( current >=0)
          if( SelInd != null)
              if( current < SelInd.length)
                return SelInd[current];
         return -1;
       }
     public int next()
       { int x = super.next();
         //if( make) System.out.println("make="+make);
         if( x >=0)
           if( SelInd != null)
             if( x < SelInd.length)
               return SelInd[x];
          return -1;
         }

    }

  // Next Handler for times where time fields do not have to match in size for @ group
  class Next_t extends Nextt // Next times. 
                             // 
    { DataSet DS;
      boolean useAll;
      int Group, ll[]; //Group is index into selected indices
      public Next_t( DataSet DSS[], int DSindex, boolean useAll)
         {this.DS = DSS[DSindex];
          this.useAll = useAll;
          ll = DS.getSelectedIndices();
          Group = -1;
          current = -1;
          min = -1;
          max = -1;
          
            
         }
      public int start() //assumes through on now start next group
       { Group++;  //Group is the index into ll the list of selected indices
         if(!useAll)
           if(ll == null)
              return -1;
         if( !useAll)
           if( Group >=ll.length)
             return -1;
         if( useAll &&( Group >= DS.getNum_entries()))
            return -1;
         if( Group < 0) return -1;
         int sGroup = Group; //sGroup is index of the group in the data set
         if( !useAll)
            sGroup = ll[Group];
         min=0;
         max = DS.getData_entry(sGroup).getX_scale().getXs().length -1;
       
         current = 0;
         return 0;
            
        }
     }

  // This is the operation done at the base of the n nested for loops
  class opnGroup implements INtupleOperation
   { int Perm[];
     DataSet DSS[];
     float xvals[]=null;
     String order;
     
     int Groups[];

     public opnGroup( DataSet DS[],int Permutation[], float xvals[], int Groups[],String order)
      { Perm = null;
        DSS=DS;
        this.order = order;
        this.xvals = xvals;
         this.Groups=Groups;
       }
     public void execute( int ntuple[])
      {if( ntuple == null) return;
       if( ntuple.length !=4) return;
       
       int Field = ntuple[3];
       int Hist = ntuple[0];
       int Group = ntuple[1];
       int time = ntuple[2];
       int comma = order.indexOf(",");
       if( comma < 0) 
          {if( Field ==0)
             if( Hist == 0);
               if( Group ==0)
                  if( time == 0)
                     SharedData.status_pane.add("Wrong Order Descriptor");
           return;
           }
      // System.out.println("opn Grp tuple="+Hist+","+Group+","+time+","+Field+","+columnHeader);
       
       FieldInfo FF = (FieldInfo)(selModel.getElementAt( Field));
       DataHandler dh = FF.getDataHandler();
      
      // Eliminate Repeated xval columns
       if( xvals != null)
       if( FF.toString().equals( "X values"))
          if( comma < order.length() -2) // more than one instance across columns
           if( order.indexOf("T")< comma)// and rows are times
            { int kk= "HGTF".indexOf(order.charAt(comma+1));
              if( kk == 3) kk = "HGTF".indexOf(order.charAt( comma + 2));
              //System.out.println("in elim cols, kk and ntuple[kk]="+kk+","+ntuple[kk]);
              if( kk < 0) return;
              
              if( ntuple[kk] > 0) return;
             }
       
      // Eliminate Repeated columns pertaining to Group 
        if( order.indexOf("G")< comma)
          if( FF.getDataHandler() instanceof DBattr)
             if( comma < order.length() -2) // more than one instance across columns
               { int kk= "HGTF".indexOf(order.charAt(comma+1));
                 if( kk == 3) kk = "HGTF".indexOf(order.charAt( comma + 2));
                //System.out.println("in elim cols, kk and ntuple[kk]="+kk+","+ntuple[kk]);
                if( kk < 0) return;
                if( ntuple[kk] > 0) return;
               }
       if( xvals != null)
         if( dh instanceof XYData)
            dh = new XYDataVal( xvals, dh.getArg());
       if( columnHeader)
         {String S = FF.toString();
          
          
          if( (comma >= 0) && (order.indexOf("G")>comma))
               S = "Gr"+Group+":"+S;
           // xvals could be across columns too may have to add something for that
           OutputField( S );
          }
       else
         {int xGroup = Group;
          if(Groups != null)xGroup = Groups[Group];
           OutputField(dh.getVal( DSS,Hist,xGroup,time));
          //System.out.println("   xxx"+dh.getVal( DSS,Hist,Group,time));
          
          }
    
      }
    }

  //Action Listener for the MenuItems to Select all and copy select
 // in the JFrame containing the JTable
  public class MyActionListener implements ActionListener
    {
      public void actionPerformed( ActionEvent e )
        {JMenuItem targ = (JMenuItem) e.getSource();
	if( targ.equals( JMi))
         {JTb.setRowSelectionInterval( 0 , DTM.getRowCount() - 1 );
          JTb.setColumnSelectionInterval( 0 , DTM.getColumnCount() - 1 );
         }
        else if( targ.equals( JCp))
         {EA.actionPerformed( new ActionEvent( JTb, 0,"Copy"));
         }
        }
    }

 // Class of elements in the "order" JComboBox
 class DescrCode
   { String Code, Report;
     public DescrCode( String Code, String Report)
       {this.Code = Code;
        this.Report = Report;
       }
     public String toString()
        { return Report;
        }
     public String getCode()
       {return Code;
        }
    }

// Operator used to Select Indices 
class SelectIndicesOp extends DataSetOperator
  {
      public SelectIndicesOp()
        {super("Select Groups by Index");
         setDefaultParameters();
        }
      public String getCommand()
         {return "SelGrIndex";}
      public void setDefaultParameters()
        {  parameters= new Vector();
           parameters.add( new Parameter("Enter indices,", new IntListString()));
         }
      public Object getResult()
       { DataSet DS = getDataSet();
         IntListString S =(IntListString)(getParameter(0).getValue());
         int[] A; 
         if( S== null) A = null; else  A= IntList.ToArray(S.toString());
         int j=0;
         for(int i=0;i<DS.getNum_entries();i++)
           { DS.setSelectFlag(i,false);
             if(A!=null) if(j <A.length)
              if( A[j] == i)
                 {DS.setSelectFlag(i,true);j++;
                 }
       
             }
        DS.notifyIObservers(IObserver.SELECTION_CHANGED );
        return "Success";  

       }
      public Object clone()
        {return null;
        }
   }

 /** Test program for this module.  Not functional
 *
 */
  public static void main( String args[] )
   {JFrame JF = new JFrame( "Test" );
   
    IsawGUI.Util ut = new IsawGUI.Util();
    String filename = "C:\\SampleRuns\\gppd9899.run";
    if( args != null )
      if( args.length > 0)
         filename = args[ 0 ];
    DataSet DS[];
    DS = ut.loadRunfile( filename );
    System.out.println( "DS length=" +DS.length );
    if( DS == null )
      { System.out.println( "Error loading file ");
        System.exit( 0 );
      }
    int choice = 1;
    if( args != null) if ( args.length > 1 )
       try{
         choice = ( new Integer( args[ 1 ] )).intValue();
          }
       catch( Exception uu )
         {choice = 1;
         }
    if( choice >= DS.length )
       choice = 0;
    DataSet DSS[];
    DSS = new DataSet[ 1 ];
    DSS[ 0 ] = DS[ choice ]; 
    
    int i = 0;
     
    DSS[ i ].setSelectFlag( 0 ,true );
    DSS[ i ].setSelectFlag( 5 ,true );
    DSS[ i ].setSelectFlag( 8 ,true );
      
    table_view TV = new table_view (DSS );
    JF.getContentPane().add( TV );
    JF.setSize( 400,  300 );
    JF.show();
    JF.validate();

   }

  }
