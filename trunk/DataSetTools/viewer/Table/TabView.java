/*
 * File:  TabView.java 
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
 * Contact : Ruth Mikkelson <mikkelsond@uwstout.edu>
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
 * Revision 1.9  2001/08/14 16:11:27  rmikk
 * Used subroutine to set the selected indicies instead of
 * setting the text on the appropriate label
 *
 * Revision 1.8  2001/08/14 01:57:32  rmikk
 * Improved the Layout
 *
 * Revision 1.7  2001/08/13 16:23:34  dennis
 * Added constructor that takes a ViewerState parameter and
 * separated initialization code in an init() method.
 *
 * Revision 1.6  2001/08/13 16:07:48  rmikk
 * Used getDataSet before each Showw() in case the data
 * set changed
 *
 * Revision 1.5  2001/08/13 14:50:03  rmikk
 * Fixed misspelling of indicies
 *
 * Revision 1.4  2001/08/13 14:38:02  rmikk
 * Improved a Menu title to indicate the indicies are used in
 * selection.
 *
 * Revision 1.3  2001/08/09 21:49:59  rmikk
 * Added Documentation.
 * Incorporated Prompts to indicate that Group indicies are
 *    needed not Group ID's.
 * Added Displayable Fields- Raw Angle, Solid Angle, and
 *   Group Index
 *
 */


package DataSetTools.viewer.Table;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import javax.swing.*;
import java.awt.event.*;
import DataSetTools.util.*;
import DataSetTools.components.ParametersGUI.*;
import java.util.*;
import Command.*;
import java.io.*;
import java.awt.*;

/** This implements DataSetViewer by giving tables of information
 */
public class TabView extends DataSetViewer implements StateListener
{   JMenu View,  
          Edit, 
          Options;
   
   
    table_view tv;
    Boolean DBSeq;


  /**
   *@param DS     the Data Set that is to be viewed
   *@param state  holds the state information for the view 
   */ 
  public TabView( DataSet DS, ViewerState state )
  {
    super( DS, state );
    init( DS );
    
    
    if( state != null )
      tv.restoreState( state.get_String( "table_view Data" ) );
  }

  /**
   *@param DS  the Data Set that is to be viewed
   */ 
   public TabView( DataSet DS )
   {
      super( DS );
      init( DS );
      
   }
  
   private void init( DataSet DS )
    {  
       setLayout( new GridLayout( 1,1 ));
       JMenuBar jmb = getMenuBar();
       int n = jmb.getMenuCount();
       View = Edit = Options = null;
       for( int i = 0 ; i < n ; i++ )
         {JMenu item = jmb.getMenu( i );
          if( "View".equals( item.getText()))
              View = item;
          else if( "Edit".equals( item.getText()))
              Edit = item;
          else if( "Options".equals( item.getText()))
              Options = item;
         }
       
       
      /* if( View != null )
         { View.add( fileView );
           View.add( tableView );
           View.add( consoleView );
         }
      
       if( Edit != null )
         {Edit.add( selectEdit );
          Edit.add( selectAllEdit );
         }
       if( Options != null )
        {Options.add( DBSeqOpt );
         Options.add( DBPairedOpt );
         radios.add(  DBSeqOpt );
         radios.add( DBPairedOpt );
         JMenu JM = new opMenu( new MyOpHandler( DS ), null , null ,
                                 null , 2 ); 
         Options.add( JM );
         //Options.add( DSOperations );
        }
      */
      MyActionListener  al = new MyActionListener();
     /* fileView.addActionListener( al );
      tableView.addActionListener( al );
      consoleView.addActionListener( al );
     */
     
     // selectAllEdit.addActionListener( al );
      //DBSeqOpt.addActionListener( al );
     // DBPairedOpt.addActionListener( al );
      //DSOperations.addActionListener( al );
  // Setup table_view
      DataSet DSS[];
      DSS = new DataSet[1];
      DSS[0] = DS;
      tv = new table_view( DSS );
      add( tv );
      tv.DBSeq = true;
      tv.useAll = false;
      tv.addStateListener( this );
      }//Constructor

   public void setState( String State )
       { ViewerState vs = getState();
        
         vs.set_String(  "table_view Data" , State );
       }

   /** An implementation of OperatorHandler that gets operators from
   * a data set
   */
   public class MyOpHandler implements OperatorHandler
    {DataSet DS;
     
     /**  
     *@param DS the Data Set with the operator list
     */
     public MyOpHandler( DataSet DS )
      {this.DS = DS;
      }

     /** Returns the number of Data Set operators the Data Set hax
     */
     public int getNum_operators()
       {return DS.getNum_operators();
       } 
       
     /** Returns the operator whose index is index or null if none
    */        
     public Operator getOperator( int index ) 
      {
         return DS.getOperator( index );
      }
                    
    } // end MyOpHandler



    class MyActionListener implements ActionListener
      { 
       public void actionPerformed( ActionEvent e )
        {
        /* if( e.getSource().equals( fileView ))
	   {tv.mode = 1;
	    JFileChooser JFC = new JFileChooser( 
                            System.getProperty( "user.dir" ));
            JFC.setDialogType( JFileChooser.SAVE_DIALOG );
            int retStatus = JFC.showSaveDialog( null );
            if( retStatus == JFileChooser.APPROVE_OPTION )
               {File F = JFC.getSelectedFile();
                tv.filename = F.getPath().trim();          
               }
           // tv.setDataSet( getDataSet());
            //tv.Showw();
           }
         else if( e.getSource().equals( tableView ) )
          { tv.mode = 2;
           // tv.setDataSet( getDataSet());
           // tv.Showw();
          }
         else if( e.getSource().equals( consoleView ))
          {tv.mode = 0;
           //tv.setDataSet( getDataSet());
           //tv.Showw();
          }
         else
        
         
          if( e.getSource().equals( selectAllEdit ))
            tv.useAll = selectAllEdit.isSelected() ;
           
         else if( e.getSource().equals( DBSeqOpt ))
            tv.DBSeq = true;               
           
         else if( e.getSource().equals( DBPairedOpt ))
            tv.DBSeq = false;  
     */       
     }
   }

    public void setDataSet( DataSet ds )
     { super.setDataSet( ds );
       tv.setDataSet( ds );
      }
  
   /** redraw Checks for selection Changed
   */
   public  void redraw( java.lang.String reason )
    { 
	
      if( reason.equals( IObserver.SELECTION_CHANGED))
        tv.setSelectedGRoup_Display(
          IntList.ToString(getDataSet().getSelectedIndices()));
      else if( reason.equals( DataSetViewer.NEW_DATA_SET ))
        tv.setDataSet( getDataSet());
      }

   /** A Test program for routines in this module
   */
   public static void main( String args[] )
     {JFrame JF = new JFrame( "Test" );
      DataSet DSS[];
      DSS = ( new IsawGUI.Util() ).loadRunfile( 
                 "C:\\SampleRuns\\gppd9898.run" );
    
      DSS[1].setSelectFlag( 0 , true );
      DSS[1].setSelectFlag( 3 , true );
      DSS[1].setSelectFlag( 9 , true );  
      TabView tb = new TabView( DSS[1]);
      JF.getContentPane().add( tb );
      JF.setJMenuBar( tb.getMenuBar() );
      JF.setSize( 800 , 800 );
      //tb.setSize( 750 , 750 );
      JF.show();
      JF.validate();
  }
}
