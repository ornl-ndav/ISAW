/*
 * File: DataSetViewerMaker.java
 *
 * Copyright (C) 2003, Ruth Mikkelson 
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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.12  2004/03/10 15:53:23  serumb
 *  Added an Ancestor Listener to call the kill method
 *  when the view component is removed.
 *
 *  Revision 1.11  2004/01/24 22:02:38  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.10  2003/12/16 23:13:32  dennis
 *  Removed calls to getSharedControls() and getPrivateControls() for
 *  the IVirtualArray1D, since these methods were removed from the
 *  interface.
 *
 *  Revision 1.9  2003/11/25 20:11:31  rmikk
 *  Added a Save Image as a submenu of the File Menu
 *
 *  Revision 1.8  2003/11/21 18:16:41  dennis
 *  Added call to repaint() method in setDataSet(), so that the
 *  axes are redrawn when the DataSet is changed.  This happens
 *  when axis conversions are used from the ViewManager.
 *
 *  Revision 1.7  2003/11/21 14:49:18  rmikk
 *  Implemented the DataSetViewer.setDataSet method
 *
 *  Revision 1.6  2003/10/31 14:48:42  dennis
 *  Fixed CVS log tag.
 *
 *  Revision 1.5  2003/10/27 14:54:30  rmikk
 *  Added the printing capability to this DataSetViewer
 *
 *  Revision 1.4  2003/08/08 15:48:24  dennis
 *  Added GPL copyright information and log tag and to record CVS
 */

package DataSetTools.viewer;

import javax.swing.*;
import javax.swing.event.*;
import DataSetTools.dataset.*;
import DataSetTools.components.View.*;
import DataSetTools.components.View.OneD.*;
import java.awt.event.*;
import java.awt.*;
import DataSetTools.components.containers.*;


public class DataSetViewerMaker  extends DataSetViewer
  {
   DataSet ds;
   ViewerState state;
   DataSetData viewArray;
   FunctionViewComponent viewComp;
   DataSetData update_array;


   public DataSetViewerMaker( DataSet               ds, 
                              ViewerState           state, 
                              DataSetData           viewArray, 
                              FunctionViewComponent viewComp )
     {
      super( ds, state);
      this.viewArray = viewArray;
      this.viewComp = viewComp;
      this.ds = ds;
      this.state = state;
      //viewComp.setData( viewArray);
      JPanel East = new JPanel( new GridLayout( 1,1));
      
      BoxLayout blayout = new BoxLayout( East,BoxLayout.Y_AXIS);
     
      East.setLayout( blayout);

      JComponent[] Compcontrols = viewComp.getSharedControls();
      if( Compcontrols != null)
        for( int i=0; i< Compcontrols.length; i++)
          East.add( Compcontrols[i]);    
      JComponent[] CompPcontrols = viewComp.getPrivateControls();
      if( CompPcontrols != null)
        for( int i=0; i< CompPcontrols.length; i++)
          East.add( CompPcontrols[i]);  
      
      PrintComponentActionListener.setUpMenuItem( getMenuBar(), this);
      SaveImageActionListener.setUpMenuItem( getMenuBar(), this);
      East.add( Box.createRigidArea(new Dimension(30,500)) );    
      viewArray.addActionListener( new ArrayActionListener());
      viewComp.addActionListener( new CompActionListener());
      setLayout( new GridLayout( 1,1));
      SplitPaneWithState the_pane =
       new SplitPaneWithState(JSplitPane.HORIZONTAL_SPLIT,
          viewComp.getDisplayPanel(), East, .70f);
      the_pane.addAncestorListener( new ancestor_listener());
      add(the_pane);
      invalidate();
     }

  public void redraw( String reason)
    {
       if ( !validDataSet() )
         return;
       if( reason.equals( "SELECTION CHANGED" ) )
       {
          update_array = new DataSetData( getDataSet() );
          viewComp.dataChanged(update_array);
         // viewComp.getGraphJPanel().repaint();
       }
       else if( reason.equals( "POINTED AT CHANGED" )) 
          viewComp.dataChanged();
    }
    /**
     *  Change the DataSet being viewed to the specified DataSet.  Derived
     *  classes should override this and take what additional steps are needed
     *  to change the specific viewer to the deal with the new DataSet.
     *
     *  @param  ds  The new DataSet to be viewed
     */
    public void setDataSet( DataSet ds )
    {
      super.setDataSet(ds);
      viewArray.setDataSet(ds);
      repaint();
    }
    

  public class ArrayActionListener  implements ActionListener
    {
     public void actionPerformed( ActionEvent evt)
       {
         viewComp.dataChanged(new DataSetData( getDataSet() ));
       }
     }

  public class CompActionListener implements ActionListener
    {
     public void actionPerformed( ActionEvent evt)
       {
       }
    }
  private class ancestor_listener implements AncestorListener {
    //methods
    public void ancestorRemoved(AncestorEvent event) {
    viewComp.kill();
    }
    public void ancestorAdded(AncestorEvent event){
    };
    public void ancestorMoved(AncestorEvent event){
    };
  }

  }//DataSetViewerMaker
