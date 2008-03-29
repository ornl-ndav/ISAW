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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.7  2004/05/10 22:46:53  dennis
 * Test program now just instantiates a ViewManager to diplay
 * calculated DataSet, rather than keeping a reference to it.
 * This removes an Eclipse warning about a local variable that is
 * not read.
 *
 * Revision 1.6  2004/03/15 06:10:53  dennis
 * Removed unused import statements.
 *
 * Revision 1.5  2004/03/15 03:28:43  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.4  2002/11/27 23:23:30  pfpeterson
 * standardized header
 *
 */

package DataSetTools.trial;

import gov.anl.ipns.Util.Messaging.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;

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
        new ViewManager( new_ds, "IMAGE" );

        new_ds = null;
        System.runFinalization();
        //System.gc();
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
