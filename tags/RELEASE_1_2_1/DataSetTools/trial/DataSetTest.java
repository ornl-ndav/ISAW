/*
 * File:  DataSetTest.java
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
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
 * Revision 1.3  2001/04/26 15:28:26  dennis
 * Added copyright and GPL info at the start of the file.
 *
 *
 */

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;

import java.awt.*;
import java.awt.event.*;

/**
  *  ( Derived from DataSetDemo1.java, tests setting new DataSet into a viewer )
  *
  *    IMPORTANT!! Since this demo does NOT provide for any user interaction, 
  *  you will have to edit this source file to specify different runfiles to 
  *  load, or to call different operators, etc. 
  *
  */  

public class DataSetTest 
{
    String           run_A = "HRCS0979.RUN";
    String           run_B = "gppd9902.run"; 

    DataSet          A_histogram_ds,
                     B_histogram_ds,
                     current;

    RunfileRetriever rr;    // The RunfileRetriever object calls John's runfile
                            // package and returns the data as DataSets

    ViewManager      view_manager;  // Variable to hold reference to a 
                                    // ViewManager that will display a DataSet

  public DataSetTest()
  {                                               // load two DataSets but
    rr = new RunfileRetriever( run_A );           // only show one of them
    A_histogram_ds = rr.getDataSet( 1 );

    rr = new RunfileRetriever( run_B ); 
    B_histogram_ds = rr.getDataSet( 1 );

 //   view_manager = new ViewManager( A_histogram_ds,IViewManager.SELECTED_GRAPHS);
    view_manager = new ViewManager( A_histogram_ds,IViewManager.IMAGE);
    current = A_histogram_ds;

    FileMenuHandler file_menu_handler = new FileMenuHandler();
    JMenu file_menu = view_manager.getJMenuBar().getMenu(
                                      DataSetViewer.FILE_MENU_ID );

    JMenuItem button = new JMenuItem( "Switch DataSet" );
    button.addActionListener( file_menu_handler );
    file_menu.add( button );
  }

  /**
    *  The main program method for this object
    */
  public static void main(String args[])
  {
    DataSetTest test = new DataSetTest();   // create an instance of this test
                                            // object
  }

  private class FileMenuHandler implements ActionListener
  {
    public void actionPerformed( ActionEvent e )
    {
      String action = e.getActionCommand();
      System.out.println( action );
      if ( action.equals( "Switch DataSet" )) 
      {
        if ( current == A_histogram_ds )
        {
          current = B_histogram_ds;
          view_manager.setDataSet( current );
        }
        else
        {
          current = A_histogram_ds;
          view_manager.setDataSet( current );
        }        
      }
    }
  }
} 
