/*
 * File:   ObserverTest.java
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
 * Revision 1.3  2001/04/26 15:28:49  dennis
 * Added copyright and GPL info at the start of the file.
 *
 */

package DataSetTools.trial;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;

public class ObserverTest extends    JFrame
                          implements ActionListener
{
  Button  loadButton;
  Button  modifyButton;
  Button  testButton;
  Button  deleteButton;
  ViewManager      view_man;

  public ObserverTest()
  {
    super( "ObserverTest" );
    setSize( 200, 200 );

    JPanel toolbar = new JPanel();
    toolbar.setLayout( new FlowLayout(FlowLayout.LEFT) );

    loadButton = new Button("Load");
    loadButton.addActionListener( this );
    toolbar.add( loadButton );

    modifyButton = new Button("Modify");
    modifyButton.addActionListener( this );
    toolbar.add( modifyButton );

    testButton = new Button("Test");
    testButton.addActionListener( this );
    toolbar.add( testButton );

    deleteButton = new Button("Delete");
    deleteButton.addActionListener( this );
    toolbar.add( deleteButton );

    getContentPane().add( toolbar, BorderLayout.NORTH );
  }

  public void actionPerformed( ActionEvent ae )
  {
    String command = ae.getActionCommand();
    System.out.println( command );
    DataSet ds;

    if ( command.equals( "Load" ) )
    {
      System.out.println("Should load");
      RunfileRetriever rr = new RunfileRetriever(
                          "/usr/LOCAL/IPNS_Software/SampleRuns/gppd9898.run" );
      ds = rr.getDataSet( 1 );
//      view_man = new ViewManager( ds, IViewManager.SCROLLED_GRAPHS );
      view_man = new ViewManager( ds, IViewManager.IMAGE );
      view_man = new ViewManager( ds, IViewManager.IMAGE );
      rr = null;
      ds = null;
//      view_man = null;
    } 

    if ( command.equals( "Modify" ))
    {
      ds = view_man.getDataSet();
      System.out.println("Should modify");
      for ( int i = 0; i < 100; i++ )           // delete 100 spectra
        ds.removeData_entry( 10 );

      ds.notifyIObservers( IObserver.DATA_REORDERED );
   }

    if ( command.equals( "Test" ))
    {
      System.out.println("Loop to repeatedly create/destroy DataSet");
      DataSet new_ds;
      for (int i = 0; i < 100; i++)
      {
        System.out.println("1. Now Getting DataSet #" + i );
        RunfileRetriever rr = new RunfileRetriever(
                          "/usr/LOCAL/IPNS_Software/SampleRuns/gppd9898.run" );
        new_ds = rr.getDataSet( 1 );
        new_ds.removeData_entry( 0 );
        ViewManager viewer = new ViewManager( new_ds, "IMAGE" );

        new_ds = null;
        viewer = null;
        System.runFinalization();
        System.gc();
      }
    }

    if ( command.equals( "Delete" ))
    {
      System.out.println("Should delete");
//      view_man.destroy(); 
      view_man.getDataSet().notifyIObservers( IObserver.DESTROY ); 
    } 
  }

  public static void main(String args[])
  {
    ObserverTest test = new ObserverTest();
    test.setVisible( true );
  } 

} 
