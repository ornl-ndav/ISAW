/*
 * File:  ProffenViewController.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2004/07/29 13:54:33  rmikk
 * redraw implemented
 *
 * Revision 1.2  2004/07/28 19:52:23  dennis
 * Removed unused import and some unreachable code.
 *
 * Revision 1.1  2004/07/28 18:23:53  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.viewer;

import Command.ScriptUtil;
import DataSetTools.dataset.DataSet;
import DataSetTools.viewer.Table.LargeJTableViewComponent;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.TwoD.*;
import javax.swing.*;
import gov.anl.ipns.ViewTools.UI.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.*;
import gov.anl.ipns.ViewTools.Components.Menu.*;
import java.awt.*;
import java.awt.event.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.Messaging.*;

/**
 * @author mikkelsonr
 * This class creates a DataSetViewer that can view a DataSet with two dimensions
 *  from the dimensions: DataSet Number, Grid Number, Row number, Column number, and
 *  time channel or time.  The values in each of these dimensions can be rebinned
 */
public class ProffenViewController extends DataSetViewer implements
                   IPreserveState {
    private ObjectState state_Controller, 
                        state_ViewComponent,
                        state_ArrayMaker,
                        TotalObjectState;
    DataSetGRCTArrayMaker ArrayMaker;
    IViewComponent2D   Viewer;
    ViewerState viewerState = null;  //Not used
    
	/**
	 * @param data_set  The DataSet to be viewed by this viewer
	 */
	public ProffenViewController(DataSet data_set) {
		super(data_set); 
        ArrayMaker = new DataSetGRCTArrayMaker(data_set, null);
        Viewer = new ImageViewComponent( new VirtualArray2D(10,10));
    //Viewer= new DataSetTools.viewer.Table.LargeJTableViewComponent(null,new VirtualArray2D(10,10));
        Viewer.dataChanged((IVirtualArray2D) ArrayMaker.getArray());
		init();
	}
  

	/**
	 * @param data_set The DataSet to be viewed by this viewer
	 * @param state    The viewer state
	 */
	public ProffenViewController(DataSet data_set, ViewerState state) {
		super(data_set, state);
        viewerState = state;
        ArrayMaker = new DataSetGRCTArrayMaker(data_set, null);
        Viewer = new ImageViewComponent( new VirtualArray2D(10,10));
       // Viewer= new DataSetTools.viewer.Table.LargeJTableViewComponent(null,new VirtualArray2D(10,10));
        Viewer.dataChanged((IVirtualArray2D) ArrayMaker.getArray());
        init();
       
		// XXX Auto-generated constructor stub
	}
  
    private void init(){
       Viewer.addActionListener( new ViewerActionListener());
       ArrayMaker.addActionListener( new ArrayMakerActionListener());
       JPanel ControlPanel = new JPanel();
       BoxLayout layoutManager = new BoxLayout( ControlPanel , BoxLayout.Y_AXIS);
       ControlPanel.setLayout( layoutManager);
       JComponent[] ArrayControls = ArrayMaker.getSharedControls();
       ControlPanel.add( ArrayControls[0]);
       //----------------- tabbed panels---------------------------
       //          --------------- for Array-----------------------
       JTabbedPane ControlBlock = new JTabbedPane();
       for( int i=2; i+1< ArrayControls.length; i+=2){
          JPanel jp = new JPanel();
          BoxLayout bLayout = new BoxLayout( jp, BoxLayout.Y_AXIS);
          jp.setLayout( bLayout);
          jp.add(ArrayControls[i]);
          jp.add(ArrayControls[i+1]);
          String title = ((ViewControl)ArrayControls[i]).getTitle();
          ControlBlock.addTab( title, jp);
       }
       ControlBlock.addTab("Conversions", ArrayControls[1]);
       //         ------------- for viewer -------------------
       ViewControl[] vcomp = Viewer.getControls();
       JPanel jp = new JPanel();
       BoxLayout bLayout = new BoxLayout( jp, BoxLayout.Y_AXIS);
       jp.setLayout( bLayout);
       ViewControl pan = null;
       for( int i=0; i < vcomp.length; i++)
          if(vcomp[i].getTitle() != ImageViewComponent.PAN_NAME)
             jp.add(vcomp[i]);
          else
             pan = vcomp[i];
       ControlBlock.addTab("View", jp);
       //--------------- Add ControlBlock to Control Panel ----------
       ControlPanel.add( ControlBlock);
       if( pan != null)
           ControlPanel.add(pan);
       
       //--------------- Set up DataSetViewer ------------------------
      Float F = new Float(20f) ;
      if( state_Controller != null)
         F = (Float)state_Controller.get( ViewerState.CONTROL_WIDTH);
      float f =20;
      if(!F.isNaN())
          f = F.floatValue();
      setLayout( new GridLayout(1,1));
      add( new SplitPaneWithState(JSplitPane.HORIZONTAL_SPLIT,
                        Viewer.getDisplayPanel(), ControlPanel, 1-f/100));
                        
     // -------------------- Set Up Menus ------------------------------------
     ViewMenuItem[] Men = Viewer.getMenuItems();
     JMenuBar menuBar = this.getMenuBar();
     SetUpMenuItems( menuBar, Men);
     invalidate();
    }
    
    private void SetUpMenuItems( JMenuBar menuBar, ViewMenuItem[] Men){
    
     for( int i=0; i < Men.length; i++){
       ViewMenuItem vm = Men[i];
       String path = vm.getPath();
       int j=0;
       JMenu jm = null;
       for( int k = Math.max(path.indexOf("."),path.length()); j < path.length();
          k = Math.max(path.indexOf(".",k+1),path.length()) ){
          String item = path.substring(j,k);
          JMenu  jm1;
          if( j ==0){
          
             jm1 = find( item, menuBar);
             if(jm1 == null){
                jm1 = new JMenu(item);
                menuBar.add(jm1);
             }
          }else{
             jm1 = find( item, jm);
             if( jm1 == null)
                jm.add(new JMenu(item));
          } 
          jm = jm1;
          j=k+1;
       }
       jm.add(vm.getItem());
     }
    }
   
   private JMenu find( String item, JMenuBar menuBar){
     for( int i=0; i< menuBar.getMenuCount(); i++)
        if( menuBar.getMenu(i).getText().equals(item))
           return menuBar.getMenu(i);
         return null;
   }
   private JMenu find( String item , JMenu menu){
     if( menu == null)
       return null;
     for( int i=0; i< menu.getItemCount(); i++)
       if( menu.getItem(i) instanceof JMenu)
         if( menu.getItem(i).getText().equals(item))
             return (JMenu)menu.getItem(i);
     return null;
     
   //------------------------ add Listeners----------------------------------
  
   }
	/* 
	 * @see DataSetTools.viewer.DataSetViewer#redraw(java.lang.String)
	 */
   public void redraw(String reason) {
     
          if( reason == IObserver.POINTED_AT_CHANGED){
            floatPoint2D fpt= ArrayMaker.redrawNewSelect(  reason );
            Viewer.setPointedAt( fpt);
          }	

	}
//--------------------IPreserveState Methods------------------
  /**
   *  Method required by the IPreserveState interface
   */
  public void setObjectState(ObjectState new_state){
      if( new_state == null)
         return;
      TotalObjectState = new_state;
      state_Controller =(ObjectState) TotalObjectState.get("Controller"); 
      state_ViewComponent =(ObjectState) TotalObjectState.get("View"); 
      state_ArrayMaker =(ObjectState) TotalObjectState.get("Model"); 
      Viewer.setObjectState( state_ViewComponent );
      ArrayMaker.setObjectState( state_ArrayMaker);
  }
  
  /**
   * Method required by the IPreserveState interface
   * @see gov.anl.ipns.ViewTools.Components.IPreserveState#getObjectState(boolean)
   */
  public ObjectState getObjectState(boolean is_default){
     if(!is_default)
        return TotalObjectState;
     
     ObjectState view, Contr, model;
     view = Viewer.getObjectState( true);
     model =ArrayMaker.getObjectState(true);
     Contr = new ObjectState();
     Contr.insert("ImageTable", new Boolean(true));
     Contr.insert(ViewerState.CONTROL_WIDTH, new Float(.7));
     ObjectState Res = new ObjectState();
     Res.insert("Controller", Contr);
     Res.insert("View", view);
     Res.insert("Model", model);
     return Res;
  }
  
 //  Attempts to go through default object state and match with viewer State values
 //  with prefix Proffen
 
 /**
  *   Attempts to write the object state values to a viewer state with prefixes of
  *   ProffenX where X = v(View),m(model) or c for (Controller)
  */
 public ViewerState getState(){
     return viewerState;
 }
 
 public static void main( String args[]){
   DataSet[] DSS = null;
   try{
   
       DSS = Command.ScriptUtil.load( "C:/ISAW-old/SampleRuns/scd08339.run");
   }catch(Exception ss){
       System.exit(0);
   }
   DataSet DS = DSS[DSS.length-1];
   ProffenViewController profView = new ProffenViewController( DS, null);
   JFrame jf = new JFrame("Test");
   jf.getContentPane().setLayout(new GridLayout(1,1));
   jf.getContentPane().add(profView);
   jf.setJMenuBar( profView.getMenuBar());
   jf.setSize(800,600);
   jf.show();
     
 }
 
 
 class ViewerActionListener implements ActionListener{
    public void actionPerformed( ActionEvent evt){
       
       if( evt.getActionCommand()== IViewComponent.POINTED_AT_CHANGED){
         floatPoint2D pt = Viewer.getPointedAt();
         ArrayMaker.setPointedAt(pt);
       }
    }
 }//ViewerActionListener
 
 class ArrayMakerActionListener implements ActionListener{
   public void actionPerformed( ActionEvent evt){
      if( evt.getActionCommand().equals(IArrayMaker.DATA_CHANGED)){
        Viewer.dataChanged( (IVirtualArray2D)(ArrayMaker.getArray()));
        repaint();
        
      }  
   }
  }//ArrayMakerActionListener
}
