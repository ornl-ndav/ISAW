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
 *  Revision 1.35  2007/09/09 23:21:31  dennis
 *  Partial clean up of logic related to handling of POINTED_AT_CHANGED
 *  messages.
 *  Removed some old commented out code, not used since the function
 *  controls were moved to their own JPanel.
 *
 *  Revision 1.34  2007/07/18 19:52:12  dennis
 *  Fixed problem with detecting focus.  Previously, the
 *  parent of the dsvm was cast to a JFrame, and checked
 *  for having focus.  Now the DisplayComponent of the
 *  viewer is checked for having focus.  This fixes a
 *  class cast exception when used with GraphicsDevices
 *  and Displayables.
 *
 *  Revision 1.33  2007/07/13 16:52:45  dennis
 *  Added getDisplayComponent() method to return just the data display
 *  panel without any controls.
 *
 *  Revision 1.32  2007/06/19 20:47:06  dennis
 *  Removed debug print.
 *
 *  Revision 1.31  2007/06/08 19:38:27  dennis
 *  Now redraws for DATA_CHANGED, DATA_DELETED, DATA_REORDERED and
 *  HIDDEN_CHANGED messages as well as for SELECTION_CHANGED.
 *  Removed debug print.
 *
 *  Revision 1.30  2007/06/08 19:25:32  dennis
 *  Removed local variable holding reference to DataSet ds.  Now
 *  uses getDataSet() method to get the reference from the base
 *  class.
 *  Minor reformatting for readability.
 *
 *  Revision 1.29  2007/03/30 19:12:01  amoe
 *  In the PointedAtListener, the actionPerformed() only does something
 *  if the DataSetViewerMaker's root pane is not null.
 *
 *  Revision 1.28  2007/03/12 15:20:13  amoe
 *  -Updated redraw():  when the POINTED_AT_CHANGED event occurs, the view 
 *   component will only get updated when it is not focused or when the 
 *   dataset needs to be updated.
 *  -Added inner class PointedAtListener to listen for cursor updates on 
 *   the view component, and update the observers.
 *
 *  Revision 1.27  2007/01/12 14:48:46  dennis
 *  Removed unused imports.
 *
 *  Revision 1.26  2006/10/20 05:28:33  amoe
 *  Edited redraw() to sychronize the cursors, without using a GraphJPanel.
 *
 *  Revision 1.25  2006/08/09 19:21:56  amoe
 *  Added code to redraw() so the graph cursor would be synchronized
 *  with the other cursor(s).
 *
 *  Revision 1.24  2006/07/19 18:07:14  dennis
 *  Removed unused imports.
 *
 *  Revision 1.23  2005/08/25 13:57:38  rmikk
 *  Removed the unused Split Pane so the bars will not show on the
 *     selected graph view
 *
 *  Revision 1.22  2005/05/25 18:39:18  dennis
 *  Removed unused imports.
 *
 *  Revision 1.21  2005/05/24 17:29:44  serumb
 *  Removed comments from commented out code that previously caused errors.
 *  Now implements IpreserveState, and has a constructor that takes in the
 *  Object State.
 *
 *  Revision 1.20  2005/05/13 14:38:37  rmikk
 *  Temporarily eliminated support for the ObjectState, until one minor error 
 *     is fixed.
 *
 *  Revision 1.19  2005/04/16 19:40:24  rmikk
 *  Added Methods to work with the ObjectState. Brent's SelectedGraph View
 *    can now remember, save, and restore its state.
 *
 *  Revision 1.18  2005/01/10 15:55:08  dennis
 *  Removed empty statement.
 *
 *  Revision 1.17  2004/07/02 19:17:31  serumb
 *  Added code to get the menu items from the view component.
 *
 *  Revision 1.16  2004/04/16 20:30:17  millermi
 *  - DataSetData no longer used as a parameter, now used to
 *    convert DataSets to IVirtualArrayList1D objects.
 *
 *  Revision 1.15  2004/03/15 19:33:58  dennis
 *  Removed unused imports after factoring out view components,
 *  math and utilities.
 *
 *  Revision 1.14  2004/03/15 03:28:58  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.13  2004/03/10 23:40:57  millermi
 *  - Changed IViewComponent interface, no longer
 *    distinguish between private and shared controls/
 *    menu items.
 *  - Combined private and shared controls/menu items.
 *
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

//import gov.anl.ipns.ViewTools.Panels.Graph.GraphJPanel;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.Util.Numeric.floatPoint2D;
import gov.anl.ipns.ViewTools.Components.OneD.*;
import gov.anl.ipns.ViewTools.Components.Menu.*;
import gov.anl.ipns.ViewTools.Components.*;

import javax.swing.*;
import javax.swing.event.*;
import DataSetTools.dataset.*;
import DataSetTools.components.View.*;
import java.awt.*;
import gov.anl.ipns.Util.Messaging.*;

import java.awt.event.*;

import gov.anl.ipns.ViewTools.Components.OneD.VirtualArrayList1D;

public class DataSetViewerMaker  extends DataSetViewer
                                 implements IPreserveState
  {
   ViewerState state;
   VirtualArrayList1D viewArray;
   FunctionViewComponent viewComp;
   DataSetViewerMaker dsvm;
   
   boolean DSVM_sent_message = false;


   public DataSetViewerMaker( DataSet               ds, 
                              ViewerState           state, 
                              VirtualArrayList1D    viewArray, 
                              FunctionViewComponent viewComp )
     {
      super( ds, state);
      this.viewArray = viewArray;
      this.viewComp = viewComp;
      this.state = state;
      this.dsvm = this;

      ViewMenuItem[] Comp_menuItems = viewComp.getMenuItems();
      if( Comp_menuItems != null)
        for( int i=0; i< Comp_menuItems.length; i++)
          (getMenuBar().getMenu(3)).add( Comp_menuItems[i].getItem()); 
      
      PrintComponentActionListener.setUpMenuItem( getMenuBar(), this);
      SaveImageActionListener.setUpMenuItem( getMenuBar(), this);

      setLayout( new GridLayout( 1,1));

      JPanel  the_pane= viewComp.getDisplayPanel() ;
      the_pane.addAncestorListener( new ancestor_listener());
      
      //adding listener update cursor for all displayed graphs
      viewComp.addActionListener(new PointedAtListener());
      
      add(the_pane);
      invalidate();
     }
 

   public DataSetViewerMaker( DataSet               ds,
                              ViewerState           state,
                              VirtualArrayList1D    viewArray,
                              FunctionViewComponent viewComp,
                              ObjectState           Ostate    )
   {
     this( ds, state, viewArray, viewComp);
     setObjectState(Ostate);
   }


   public void redraw( String reason)
    {
      if ( !validDataSet() )
        return;

      if( reason.equals( IObserver.SELECTION_CHANGED ) ||
          reason.equals( IObserver.DATA_CHANGED )      ||
          reason.equals( IObserver.DATA_DELETED )      ||
          reason.equals( IObserver.DATA_REORDERED )    ||
          reason.equals( IObserver.HIDDEN_CHANGED )      )
      {
        viewArray = DataSetData.convertToVirtualArray(getDataSet());
        viewComp.dataChanged(viewArray);
      }

      else if( reason.equals( "POINTED AT CHANGED" ))
      {
        if( viewComp.isDrawingPointedAtGraph() && 
           (getDataSet().getPointedAtIndex() != viewArray.getPointedAtGraph()))
        {
          viewArray.setPointedAtGraph( getDataSet().getPointedAtIndex() ); 
          viewComp.dataChanged();
        }
          
        if( !DSVM_sent_message )                 // ignore one message 
        {                                        // if we sent a message
          floatPoint2D fpt = new floatPoint2D();
          fpt.x = getDataSet().getPointedAtX();
          viewComp.setPointedAt(fpt);
        }
        else
          DSVM_sent_message = false;
      }
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
      viewArray = DataSetData.convertToVirtualArray(ds);
      viewComp.dataChanged(viewArray);
      viewComp.paintComponents( );
      repaint();
    }


   /* ----------------------- getDisplayComponent -------------------------- */
   /**
    *  Get the JComponent that contains the graph of the data, without
    *  any associated controls or auxillary displays.
    */
   public JComponent getDisplayComponent()
   {
     if ( viewComp != null )
       return viewComp.getDisplayPanel();
     else
       return this;
   }


    //-------------------- IPreserveState Methods & Variables-----------------

    ObjectState Ostate= null;
   
     public void setObjectState( ObjectState new_state){
     //   return;
        Ostate = new_state;
        if( viewArray instanceof IPreserveState)
            ((IPreserveState)viewArray).setObjectState( 
                           (ObjectState)Ostate.get("ArrayMaker"));
        if(viewComp instanceof IPreserveState)
            ((IPreserveState)viewComp).setObjectState( 
                    (ObjectState)Ostate.get("View"));
      
     }
     public ObjectState getObjectState( boolean is_default){
         ObjectState state = new ObjectState();

         if( viewArray instanceof IPreserveState)
             state.insert("ArrayMaker",
                     ((IPreserveState)viewArray).getObjectState( is_default));
                          
         if(viewComp instanceof IPreserveState)
             state.insert("View",((IPreserveState)viewComp).getObjectState 
                     (is_default));
         return state;
     }



  private class ancestor_listener implements AncestorListener {
    //methods
    public void ancestorRemoved(AncestorEvent event) {
    viewComp.kill();
    }
    public void ancestorAdded(AncestorEvent event){
    }
    
    public void ancestorMoved(AncestorEvent event){
    }
  }
  
  /*
   * PointedAtListener listens for the current cursor position in the 
   * view component, and updates the dataset.  This is so the other dataset
   * viewers can be updated with a pointed at position.
   */
  private class PointedAtListener implements ActionListener
  {	  
    String message;
    float new_x;
	  
    public void actionPerformed(ActionEvent ae)
    {		   
      message = ae.getActionCommand();		  
      new_x  = viewComp.getPointedAt().x;		  	  
			  
      if ( message.equals(IViewComponent.POINTED_AT_CHANGED) )
      {			 		
        DSVM_sent_message = true;
        getDataSet().setPointedAtX( new_x );
        getDataSet().notifyIObservers( IObserver.POINTED_AT_CHANGED );
      }
    }
  }

}//DataSetViewerMaker
