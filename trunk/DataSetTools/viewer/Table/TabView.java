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
 * Revision 1.3  2001/08/09 21:49:59  rmikk
 * Added Documentation.
 * Incorporated Prompts to indicate that Group indecies are
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

/** This implements DataSetViewer by giving tables of information
 */
public class TabView extends DataSetViewer
{   JMenu View,  
          Edit, 
          Options;
    JMenuItem  fileView,  
               tableView, 
               consoleView;
    JMenuItem  selectEdit, 
               selectAllEdit;
    JMenuItem  DBSeqOpt, 
               DBPairedOpt;
    ButtonGroup radios = new ButtonGroup();
    table_view tv;
    Boolean DBSeq;

    /**
   *@param DS  the Data Set that is to be viewed
   */ 
   public TabView( DataSet DS )
     { super( DS );
     
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
       fileView = new JMenuItem( "Save to File" );
       tableView = new JMenuItem( "Table" );
       consoleView = new JMenuItem( "Console" );
       selectEdit = new JMenuItem( "Select Group indicies" );
       selectAllEdit = new JCheckBoxMenuItem( "Select All Groups" , false );
       DBSeqOpt = new JRadioButtonMenuItem( "List Groups Sequentially" ,
                                           true );
       DBPairedOpt = new JRadioButtonMenuItem( "List Groups in Columns" ,
                                               false);      
       if( View != null )
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
      MyActionListener  al = new MyActionListener();
      fileView.addActionListener( al );
      tableView.addActionListener( al );
      consoleView.addActionListener( al );
      selectEdit.addActionListener( al );
      selectAllEdit.addActionListener( al );
      DBSeqOpt.addActionListener( al );
      DBPairedOpt.addActionListener( al );
      //DSOperations.addActionListener( al );
  // Setup table_view
      DataSet DSS[];
      DSS = new DataSet[1];
      DSS[0] = DS;
      tv = new table_view( DSS );
      add( tv );
      tv.DBSeq = true;
      tv.useAll = false;
      }//Constructor



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
         if( e.getSource().equals( fileView ))
	   {tv.mode = 1;
	    JFileChooser JFC = new JFileChooser( 
                            System.getProperty( "user.dir" ));
            JFC.setDialogType( JFileChooser.SAVE_DIALOG );
            int retStatus = JFC.showSaveDialog( null );
            if( retStatus == JFileChooser.APPROVE_OPTION )
               {File F = JFC.getSelectedFile();
                tv.filename = F.getPath().trim();          
               }
            tv.Showw();
           }
         else if( e.getSource().equals( tableView ) )
          { tv.mode = 2;
            tv.Showw();
          }
         else if( e.getSource().equals( consoleView ))
          {tv.mode = 0;
           tv.Showw();
          }
         else if( e.getSource().equals( selectEdit ))
          { 
            DataSet DS = getDataSet();        
            DataSetOperator op = DS.getOperator( "Set DataSet Field" );        
            if( op == null )
              {System.out.println( " No such operator " );
               return;
               }
            op.setDefaultParameters();
            IntListString IString = new IntListString( "1,3:5" );        
            Parameter PP = new Parameter( "Group ID's=" , IString );        
            op.setParameter( PP , 1 );        
       
            DSSettableFieldString argument = new DSSettableFieldString( 
                                      DSFieldString.SELECTED_GROUPS );         
        
            MOperator newOp =
               new MOperator( op , 0 , ( Object )argument );
        
            JParametersDialog JP = new JParametersDialog( newOp , null ,
                                       null , null );
            tv.useAll = false;
           }
         else if( e.getSource().equals( selectAllEdit ))
            tv.useAll = selectAllEdit.isSelected() ;
           
         else if( e.getSource().equals( DBSeqOpt ))
            tv.DBSeq = true;               
           
         else if( e.getSource().equals( DBPairedOpt ))
            tv.DBSeq = false;         
     }
   }


   class MOperator extends DataSetOperator
     { int paramPos = -1;
       Object DefValue = null;
       String Title = "xxx";
       DataSetOperator op = null;
       public  MOperator( DataSetOperator op , int paramPos , Object DefValue )
          {super( op.getTitle());
     
           Title = op.getTitle();
           this.paramPos = paramPos;
           this.op = op;
           this.DefValue = DefValue;
     
           setDefaultParameters();
          }
       public MOperator()
         {super( "unknown" );          
          setDefaultParameters();
         }
       public String getCommand()
         { return op.getCommand();
         }
       public void setDefaultParameters()
         {
           if( op == null )
             return;
           parameters = new Vector();        
           CopyParametersFrom( op );   
           parameters.remove( paramPos );      
        }

       public DataSet getDataSet()
          {return op.getDataSet();
          }
       public Object getResult()
         {
           for( int i = 0 ; i < paramPos ; i++ )       
              op.setParameter(  getParameter( i ) , i );
      
    
           op.setParameter( new Parameter( "ttt" , DefValue ) , paramPos );
     
           for( int i = paramPos + 1 ; i < op.getNum_parameters() ; i++ )
              op.setParameter( getParameter( i - 1 ) , i );        
     
           return  op.getResult();
         }
       public Object clone()
         {MOperator Res = new MOperator( op , paramPos , DefValue );
           return Res;
         }
   }//MOperator

   /** redraw does nothing for this viewer
   */
   public  void redraw( java.lang.String reason )
     {}

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
      JF.setSize( 500 , 800 );
      tb.setSize( 500 , 750 );
      JF.show();
      JF.validate();
  }
}
