/*
 * File:  table_view.java 
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
 * Revision 1.7  2001/08/14 16:12:22  rmikk
 * Major restructuring of the GUI so more options were
 * visible
 *
 * Revision 1.6  2001/08/14 01:59:00  rmikk
 * Improved layout.
 * Added line indicating the selected indicies.
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

    String Fields[] = { "Title", "X_units", "Y_units", "X_label", "Y_label",
                        "Initial_Path", "Run Number", "Pressur", "Temp",
                        "Group ID", "detector x Pos", "detector y Pos",
                        "detector z Pos",
                        "detector r Pos", "detector rho Pos", 
                        "detector theta Pos", "detector phi Pos", 
			"Group Index", "Raw Angle", "Solid Angle",
                        "x value", "y value", "error" };
 
    String args[] ={ DSFieldString.TITLE, DSFieldString.X_UNITS,
                     DSFieldString.Y_UNITS, 
                     DSFieldString.X_LABEL, DSFieldString.Y_LABEL,
                     Attribute.INITIAL_PATH, Attribute.RUN_NUM, 
                     Attribute.PRESSURE , Attribute.TEMPERATURE ,
                     Attribute.GROUP_ID , "x" , "y" ,"z" ,
                     "r" , "R" , "t" , "p" , 
                     null ,Attribute.RAW_ANGLE , Attribute.SOLID_ANGLE ,
                     "x" , "y"  , "e" }; 


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

   int nDSfields = 5;
   int nDSattr = 4;   
   int nDBattr = 11;                        
   //JComboBox OutputMode; 
   //JCheckBox Sequent;//paired or sequential
   //JCheckBox UseAll;
   boolean useAll = false;
   String filename = null;
   JButton  Add , 
            Remove ,
            Up , 
            Down;
 
   JRadioButtonMenuItem  fileView,  
                         tableView, 
                         consoleView;
  
   ButtonGroup  GotoRadioButtons = new ButtonGroup();
   JButton  selectEdit;
   JCheckBox selectAllEdit;
   JRadioButton  DBSeqOpt, 
                  DBPairedOpt;
   ButtonGroup radios = new ButtonGroup();

   JLabel  SelectedIndecies;
   JButton GO;
    
   JList   unsel ,
           sel;

   DefaultListModel unselModel , 
                    selModel;    
   int use[] ,
       Nuse[];
   DataSet DSS[];
   boolean DBSeq = false; // DB are paired 
   boolean XYcol = false;
   boolean DBcol = false;
   ExcelAdapter EA = null;
   IsawGUI.Util  util;
   
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
    {//OutputMode = null; 
     //Sequent = null;paired or sequential
    // UseAll = null;
     Add = Remove = Up = Down = null;
     unsel = sel = null;
     unselModel = selModel = null;    
     use = Nuse = null;
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
     // System.out.println( "# data Sets="+ DS.length );
      //setLayout( new BorderLayout() );

//Top component
      //JPanel JP = new JPanel();
      //JP.setLayout( new GridLayout( 1 , 7 ));      
      
     // OK = new JButton( "OK" );
      //OK.addActionListener( this );
      //JP.add( new JLabel("Select:" , SwingConstants.RIGHT ));
      //Sequent = new JCheckBox( "Dat.Bl Seq" , false );
     // JP.add( Sequent );
     // UseAll = new JCheckBox( "Use All DB" , false );
     // JP.add( UseAll );
     // String Outs[]={"Console" , "File" , "Table" };
     // OutputMode = new JComboBox( Outs );
     // JP.add ( new JLabel( "Output:" , SwingConstants.RIGHT ));
     // JP.add( OutputMode );
      //JP.add( OK );
      //OK.setActionListener( new myActionListener( OK ));
      //add( JP , BorderLayout.NORTH );

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
       
       Nuse = new int[ Fields.length + 1 ];
       for( int i = 0 ; i < Fields.length ; i++ )
          {unselModel.addElement( Fields[ i ] );
           Nuse[ i ] = i;
          }
       Nuse[ Fields.length ] = -1;
       JPbl.add( new JScrollPane( unsel , 
          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,
           JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ) ,
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
       use = new int[ Fields.length+ 1 ];
        Arrays.fill( use , -1 );
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
      
        fileView = new JRadioButtonMenuItem( "Save to File" ,false);
       tableView = new JRadioButtonMenuItem( "Make a Table",false );
       consoleView = new JRadioButtonMenuItem( "Write to Console" ,true);   
       fileView.addActionListener( this );  
       consoleView.addActionListener( this );  
       tableView.addActionListener( this );          
       GotoRadioButtons.add( fileView );
       GotoRadioButtons.add( tableView );       
       GotoRadioButtons.add ( consoleView);
       
      
       
       GO = new JButton( "Display");
       GO.addActionListener( this );
      
       /* GG.add(Goto);
       Goto.add( fileView);
       Goto.add( tableView);
       Goto.add( consoleView);
       */
       SelectedIndecies = new JLabel( );
       setSelectedGRoup_Display( IntList.ToString( DS[0].
                                 getSelectedIndices() ) );

       selectEdit = new JButton( "Select Group indicies" );
       selectEdit.addActionListener( this );
       selectAllEdit = new JCheckBox( "Use All Groups" , false );
       DBSeqOpt = new JRadioButton( "List Groups Sequentially" ,
                                           true );
       DBPairedOpt = new JRadioButton( "List Groups in Columns" ,
                                               false); 
       DBSeqOpt.addActionListener( this );
       DBPairedOpt.addActionListener( this );
       selectAllEdit.addActionListener( this );    
       radios.add( DBSeqOpt );
       radios.add( DBPairedOpt );
       // New Layout
           Box RightPanel = new Box( BoxLayout.Y_AXIS );
           RightPanel.add( new JLabel ("Controls", SwingConstants.CENTER ));
           JPanel Selects = new JPanel( new GridLayout( 3,1 ));
           Selects.add( SelectedIndecies );
           Selects.add ( selectEdit );
           Selects.add( selectAllEdit );
           Selects.setBorder( BorderFactory.createEtchedBorder() );
           RightPanel.add( Selects );

           JPanel Grouping = new JPanel( new GridLayout( 2, 1 ));
           Grouping.add( DBSeqOpt);
           Grouping.add( DBPairedOpt );
           Grouping.setBorder( BorderFactory.createEtchedBorder() );
           RightPanel.add( Grouping);
        
          // RightPanel.add( Box.createVerticalGlue() );
 
           JPanel Output = new JPanel( new GridLayout( 4, 1));
           Output.add( fileView );
           Output.add( tableView );
           Output.add( consoleView );
           Output.add( GO);          
           Output.setBorder( BorderFactory.createEtchedBorder() );
           RightPanel.add( Output );

           //Spacing
           //JP = new JPanel();
           //JP.setPreferredSize( new Dimension( 20, 5000 ) );
           //Output.add( JP );
          
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
     { String Res ="Selected indicies:";
       
       if( S == null)
          Res += "None        ";
       else if( S.length() <1)
          Res += "None         ";
       else if( S.length() >15)
         Res += S.substring( 0,11)+"...";
       else
         Res += S;
       SelectedIndecies.setText( Res+"    " );

     } 
 /**
*  Restores the state of this system to the way it was the previous time
* the ViewManager visited this module.<BR>
* The used indicies, filename, output choice, useAll and Data Block sequential
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
       int nused = (new Integer( state.substring( 0, i ))).intValue();
       use = new int[nused+1];
    for( k = 0; k < nused ; k++)
      {   j = state.indexOf( ";", i+1);
          use[k] = (new Integer( state.substring( i+1, j))).intValue();
          i = j;
       }
    j=i;
    use[nused ] = -1;
    selModel.clear();
    for( i = 0 ; i < nused ; i++)
       selModel.addElement( Fields[ use [i ] ] );

   //filename
   k = state.indexOf( ";",j+1);
   if( k == j+1)
       filename = null;
   else
       filename = state.substring( j+1, k );
   j = k;
   //useAll
   k = state.indexOf( ";",j+1);
   if( k == j+1)
     {useAll = false;
      selectAllEdit.setSelected( false );
     }
   else
     {useAll = true;
      selectAllEdit.setSelected( true );
     }
   //Sequential data blocks
   j = k;
   DBSeq = false;
   k = state.indexOf( ";", j+1);
   if( k > j+1)
     {DBSeq = true;
      DBSeqOpt.setSelected( true );
      DBPairedOpt.setSelected( false);
     
     }
   else
     {DBSeqOpt.setSelected( false );
      DBPairedOpt.setSelected( true );
     
     }
   j = k;
   //mode?
    k = state.indexOf( ";", j+1);
  
   mode = ( new Integer( state.substring(j+1,k))).intValue();
   if( mode == 1)
      fileView.setSelected( true );
   else if( mode ==2)
      tableView.setSelected( true );
   else
      consoleView.setSelected( true );
   
   
   }
  
  public void setState( )
   {String  S ="";
   
    int n=0;
    for( int i=0; (i<use.length) && (use[i] >= 0) ; i++)
       { S += use[i] +";";
         n++;
       }
    S = n +";"+S;
   
    if( filename == null )
      S += ";";
    else 
      S += filename +";";
    
    if( useAll)
      S += "T;";
    else
      S +=";";
   
    if( DBSeq)
      S +="T;";
    else
      S += ";";
  
   if( fileView.isSelected())
     mode = 1;
   else if( tableView.isSelected())
     mode = 2;
   else if( consoleView.isSelected())
     mode = 0;
   S += mode+";";
   
    for( int i = 0; i < StateListeners.size(); i++ )
      (( StateListener)(StateListeners.elementAt( i ))).setState( S );
   }
  Vector StateListeners = new Vector();
  public void addStateListener( StateListener S)
   {StateListeners.addElement( S );
   }
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
   public void insertElementAt( int indx , int value , int list[] )
     {if( indx < 0 ) 
        return;
      if( indx > list.length ) 
        return;
      for( int i = list.length - 1 ; i > indx ; i-- )
         list[ i ] = list[ i - 1 ];
      list[ indx ] = value;
      }

   /**Utility that will add value to the end of list
   */
   public void addElement( int value , int list[] )
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


   /** Shows the selected list of Fields that are to be displayed
   */
   public void show()
     {int i;
      System.out.print( "unsel=" );
        for( i = 0; ( i < Nuse.length ) && ( Nuse[ i ] >= 0 ) ; i++ )
         System.out.print( Nuse[ i ]+ ":"+  Fields[ Nuse[ i ] ]+ " " );
        System.out.println( "" );  

      System.out.print( "sel=" );
        for( i = 0 ; ( i < use.length ) && ( use[ i ] >= 0 ); i++ )
         System.out.print( use[ i ]+ ":"+ Fields[ use[ i ] ]+ " " );
        System.out.println( "" ); 
    }


   /** Handles the events associated with this JPanel components
   */
   public void actionPerformed( ActionEvent e  )
   {if( e.getSource().equals( Add ))
     { int indx = unsel.getSelectedIndex();      
        if( indx < 0 ) 
           return;
        String S = ( String )( unsel.getSelectedValue() );
        int FieldIndx = Nuse[ indx ];
        //unselModel.remove( indx );
        // remove( indx , Nuse );
        selModel.addElement( S );
        addElement( FieldIndx , use );
        if( indx+ 1 < unselModel.getSize() )
           unsel.setSelectionInterval( indx+ 1, indx+ 1 );
        unsel.requestFocus();
        setState();
      }
     else if( e.getSource().equals( Remove  ) )
      { int indx = sel.getSelectedIndex();    
        if( indx < 0 ) 
           return;
        String S = ( String )( sel.getSelectedValue() );
        int FieldIndx = use[ indx ]; 
        selModel.remove( indx );
        remove( indx, use );
        //unselModel.addElement( S );
        // addElement( FieldIndx, Nuse );
        if( indx < selModel.getSize() )
          sel.setSelectionInterval( indx,indx );
        sel.requestFocus();
        setState();
       }
      else if( e.getSource().equals( Up ) )
       {int indx = sel.getSelectedIndex();
        if( indx <= 0 ) 
           return;
        String S = ( String )( sel.getSelectedValue() );
        int FieldIndx = use[ indx ];
        selModel.remove( indx );
        remove( indx, use );
        selModel.insertElementAt( S, indx - 1 );
        insertElementAt( indx - 1, FieldIndx , use );
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
        String S = ( String )( sel.getSelectedValue() );
        int FieldIndx = use[ indx ];
        selModel.remove( indx );
        remove( indx, use );
        selModel.insertElementAt( S, indx +  1 );
        insertElementAt( indx +  1 , FieldIndx , use );
        if( indx + 1  < selModel.getSize() )
         sel.setSelectionInterval( indx +  1 , indx + 1 );
        sel.requestFocus();
        setState();
       }
      else if( e.getSource().equals( GO ) )
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
      else if( e.getSource().equals( selectEdit ))
          { 
            DataSet DS = DSS[0];               
            DataSetOperator op = DS.getOperator( "Set DataSet Field" );        
            if( op == null )
              {System.out.println( " No such operator " );
               return;
               }
            op.setDefaultParameters();
            IntListString IString = new IntListString( "1,3:5" );        
            Parameter PP = new Parameter( "Group Indicies=" , IString );        
            op.setParameter( PP , 1 );        
       
            DSSettableFieldString argument = new DSSettableFieldString( 
                                      DSFieldString.SELECTED_GROUPS );         
        
            MOperator newOp =
               new MOperator( op , 0 , ( Object )argument );
        
            JParametersDialog JP = new JParametersDialog( newOp , null ,
                                       null , null );
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
               }
           setState();
          }
       else
        {  useAll = selectAllEdit.isSelected();
          DBSeq = DBSeqOpt.isSelected();
          mode = 0;
          if( fileView.isSelected())
             mode = 1;
          else if( tableView.isSelected())
             mode = 2;
          else if( consoleView.isSelected())
           mode = 0;
         
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


  FileOutputStream f = null;
  JFrame  JF;
  JTable JTb;
  JTextArea HeaderInfoPane;
  DefaultTableModel DTM;
  Vector V = new Vector();
  int mode= 0;
  boolean startline = true;
  boolean columnHeader = false; //for table view feature
                               
  private void initOutput( DataSet D )
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
             System.out.println( "IOExon init="+ u+ ","+ 
             D.toString()+ ".dat" );}

      }
    else
     { JF = new JFrame( D.toString() );
        
       JMenu JM = new JMenu( "Select" );
       JMenuItem JMi = new JMenuItem( "All" );
       JM.add( JMi );
       JMenuBar JMB= new JMenuBar();
       JMB.add( JM );
       JF.setJMenuBar( JMB );
       JMi.addActionListener( new MyActionListener() );
       // Tabbed pane
       JTabbedPane JtabPane = new JTabbedPane();
       // Table Pane
       DTM = new DefaultTableModel();
       JTb = new JTable( DTM );
        EA = new ExcelAdapter( JTb );
       // The HeaderInfo Pane
       HeaderInfoPane = new JTextArea (20, 50 );
       // Glue together
       JF.getContentPane().add( JtabPane );
       JtabPane.add( "Table",new JScrollPane( JTb ,
                                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ,
                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED  ));
       JtabPane.add( "Header", new JScrollPane( HeaderInfoPane ,
                                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ,
                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED  ));
    
       JF.setSize( 400, 400 );
       JF.show();
       JF.validate();
       row = 0;
       col = 0;
       V = new Vector();
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
           if( startline )
           f.write( S.getBytes() );
        else
           f.write( ("\t"+ S ).getBytes() );
        }
     catch( IOException ss ){}
    
     else if( columnHeader)
      DTM.addColumn( S );
     else
       V.addElement( S );

     startline = false;
    }
  private void OutputEndField()
   {
    if( mode == 0 )
       System.out.println( "" );
    else if( mode == 1 )
       try{if(  f != null );
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
          f.close();
          f = null;
          filename = null;
       }
    catch( IOException ss ){}
   else
     V = new Vector();
   
  }
 
  /** Converts an array of Fields in String form to an int array
  *   that is used in the Showw method
  *@see   #Showw(DataSetTools.dataset.DataSet[], int[], boolean, boolean)
 */
  public int[] Convertt( String fields[] )
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

  boolean header = false;
  private void MakeHeaderInfo( DataSet DSS[] , boolean UseAll )
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
     {OutputField( "#     "+ DSS[ i ].toString()+ ":"+  
                         nd.Showw( DSS[ i ].getSelectedIndices() ));
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
        mode = 0;
        if( fileView.isSelected())
           mode = 1;
        else if( tableView.isSelected())
           mode = 2;
        else if( consoleView.isSelected())
           mode = 0;
        
        Showw( DSS, use, DBSeq, useAll );
      }

  /** Views the data for the data set.  This is used for the non-GUI access
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
  public void Showw( DataSet DSS[], int used[],  boolean  DBSeq, 
                                boolean UseAll )
    {int ncols = 0;
     int i = 0;     
     while(( i < used.length ) &&  ( used[ i ] >= 0 ) )
        {i++;
         ncols++;
        }
     DataHandler DH[];
     DBcol = XYcol = false;
     DH = new DataHandler[ ncols ];
     for( i = 0 ; i < ncols ; i++ )
      {if( used[ i ] < nDSfields )
         DH[ i ] = new DSfield( args[ used[ i ] ] );
       else if( used[ i ] < nDSfields+ nDSattr )
         DH[ i ] = new DSattr( args[ used[ i ] ] );       
       else if( used[ i ] < nDSfields+ nDSattr+nDBattr )
        { DH[ i ] = new DBattr(args[ used[ i ] ] );
          DBcol = true;
        }
       else
         {
           DH[ i ] = new XYData( args[ used[ i ] ] );
           XYcol = true;
           DBcol = true;
         }
      }          
/*      i = JCBNuse.getSelectedIndex();
     if( (i < 0 ) || ( i >= DSS.length  ) ) 
        {System.out.println( "No Data Sets are Selected" );
         return;
        }
*/
    initOutput( DSS[ 0 ] );
    columnHeader = false;
    MakeHeaderInfo( DSS , UseAll );
    header = false;
    for(  i = 0 ; i < DSS.length ; i++ )
    if( DBSeq )   
      {DataSet DS = DSS[ i ]; 
       columnHeader = true;      
       for( int ii = 0 ; ii < ncols ; ii++ )
          OutputField( Fields[ used[ ii  ] ] );
       OutputEndField();
       columnHeader = false;
       for( int j = 0 ; j < DS.getNum_entries() ; j++ )
         if( UseAll || DS.getData_entry( j ).isSelected() )
          {Data DB = DS.getData_entry( j );
           float xx[ ];
           if( XYcol )
               xx = DB.getX_scale().getXs();
           else
               xx = new float[ 1 ];
           for( int k = 0 ; k < xx.length ; k++ )
             { for ( int l = 0 ; l < ncols ; l++ )                   
                    OutputField( DH[ l ].getVal( DSS, i, j, k ) ); 
               OutputEndField();
             }             
          }
      }
    else //paired
     {float xvals[]; 
      xvals = null;     
      DataSet DS = DSS[ i ];
      if( XYcol )           
            xvals = MergeXvals( 0, DS, null, UseAll );                   
      int n = 1;
      if( xvals != null )
         n = xvals.length;
      if( n <= 0 )
         n = 1;
       int count =  0;   
       columnHeader = true;
       for( int j = 0 ; j < DS.getNum_entries() ; j++ )
          {Data DB = DS.getData_entry( j );
           if( UseAll || DB.isSelected() )
            {                      
              for( int l = 0 ; l < ncols ; l++ )
                { 
                    if( ( count == 0 )||
                          ( used[ l ]!= nDSfields+  nDSattr +  nDBattr ))  
                      OutputField( "Group"+ DB.getGroup_ID()+ ":"+ 
                                                      Fields[ used[ l ] ] );
                                    
                 if( used[ l ]== nDSfields+  nDSattr +  nDBattr ) 
                   { count = 1;                                        
                   }
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
        for( int j = 0 ; j < DS.getNum_entries() ; j++ )
          {Data DB = DS.getData_entry( j );
           if( UseAll ||  DB.isSelected() )
            { float xx[];
              xx = DB.getX_scale().getXs();
            
              boolean C = contains( xx,  x );
              if( xvals == null )
                    C = true;
              for( int l = 0 ; l < ncols ; l++ )
                { if( C )
                    {if(( count == 0 )||
                          ( used[ l ]!= nDSfields+  nDSattr +  nDBattr ))  
                      OutputField( DH[ l ].getVal( DSS,  i, j ,k ));
                    }
                 else 
                    OutputField( "  " );
                 if( used[ l ] == nDSfields+  nDSattr +  nDBattr ) 
                   { count = 1;                                        
                   }
                }                        
            }
          }//for
         OutputEndField();


       }
           
      }//end paired Data Blocks

    }
  private float[] MergeXvals ( int db , DataSet DS , float xvals[]
                               , boolean UseAll )
    {if( db >= DS.getNum_entries() )
        return xvals; 
     if( db == 0 )
       { Data DB = DS.getData_entry( 0 );
         XScale XX = DB.getX_scale();
         return MergeXvals( 1 , DS , XX.getXs() , UseAll );
        }
     Data DB = DS.getData_entry( db );
     if( UseAll ||  !DB.isSelected())
        return MergeXvals( db+ 1, DS , xvals , UseAll );
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
     return MergeXvals ( db+ 1 ,  DS , Res , UseAll );

    }


  private boolean contains( float xx[],  float x )
    {float delta;
     if( xx == null ) 
       return false;
     if( xx.length <= 0 )
       return false;
     delta = ( xx[ xx.length - 1 ] - xx[ 0 ] )/ xx.length/ 20.0f;     
     for( int i = 0 ; i< xx.length ; i++ )
        {
         if( java.lang.Math.abs( xx[ i ] - x ) < delta )
           return true;
         else if(  xx[ i ] > x )
            return false;
     
        }    
     return false;
    }

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
    }


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


  class XYData extends DataHandler
    {public XYData( String arg )
        {super( arg ) ;
        }
 
      public Object getVal( DataSet DSS[], int DS_index,  int DB_index, 
                 int XY_index )
         {if( arg == null ) 
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

 
  public class MyActionListener implements ActionListener
    {
      public void actionPerformed( ActionEvent e )
        {JTb.setRowSelectionInterval( 0 , DTM.getRowCount() - 1 );
         JTb.setColumnSelectionInterval( 0 , DTM.getColumnCount() - 1 );
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
