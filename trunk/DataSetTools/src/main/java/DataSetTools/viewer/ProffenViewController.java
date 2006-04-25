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
 * Revision 1.10  2005/06/18 18:26:15  rmikk
 * Omitted using default inputs when actual inputs were available
 * Added extra global variables for more versatility in later changes to the
 *    subcomponents.
 * Changed listening and redrawing methodology so hopefully infinite cycles 
 *   are not encountered.
 * Separated out some code into methods  for more versatility in later changes to the
 *    subcomponents.
 *
 * Revision 1.9  2005/05/25 19:37:49  dennis
 * Replaced direct call to .show() method for window,
 * since .show() is deprecated in java 1.5.
 * Now calls WindowShower.show() to create a runnable
 * that is run from the Swing thread and sets the
 * visibility of the window true.
 *
 * Revision 1.8  2005/04/16 20:57:29  rmikk
 * Added code to do nothing if a pointed at change notification  did not change
 * the current pointed at.  Eliminated a StackOverflowError
 *
 * Revision 1.7  2004/08/18 17:17:54  rmikk
 * Added support for markers at integer hkl values if there is enough information
 *
 * Revision 1.6  2004/08/05 15:27:27  rmikk
 * Added options for the ViewComponent to be a Table or an Image View
 *
 * Revision 1.5  2004/08/04 22:12:15  rmikk
 * Fixed an error in the menu system
 *
 * Revision 1.4  2004/07/29 19:46:11  rmikk
 * Fixed the Menu maker to give.
 * Checked for a null condition before invoking an imageViewComponent Method
 *
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


//import Command.ScriptUtil;
import DataSetTools.dataset.DataSet;
//import DataSetTools.viewer.Table.LargeJTableViewComponent;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.TwoD.*;
import javax.swing.*;
import gov.anl.ipns.ViewTools.UI.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.*;
import gov.anl.ipns.ViewTools.Components.Menu.*;
import gov.anl.ipns.ViewTools.Components.Transparency.*;
import java.awt.*;
import java.awt.event.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.Sys.WindowShower;


/**
 * @author mikkelsonr
 * This class creates a DataSetViewer that can view a DataSet with two dimensions
 * from the dimensions: DataSet Number, Grid Number, Row number, Column number,
 * and time channel or time.  The values in each of these dimensions can be 
 * rebinned. The view can be either an imageView or a table view.  Markers are
 *  implemented and the Object State is used.
 */
public class ProffenViewController extends DataSetViewer implements
        IPreserveState {
    private ObjectState state_Controller, 
                        state_ViewComponent,
                        state_ArrayMaker,
                        TotalObjectState;
    DataSetGRCTArrayMaker ArrayMaker;
    IViewComponent2D   Viewer;
    ViewerState state = null;  
    int mode = 0;
    ViewControl[] vcomp;
    ViewMenuItem[] Men;
    ViewControl pan = null;
    JTabbedPane ControlBlock= null;
    JPanel PanHolder = new JPanel(new GridLayout(1, 1)),
           intensityHolder = new JPanel(new GridLayout(1, 1)),
           colorHolder = new JPanel(new GridLayout(1, 1)),
           ViewHolder = new JPanel(new GridLayout(1, 1)),
           ViewPanel= null,
           ViewViewPanel=null;
           
    /**
     * @param data_set  The DataSet to be viewed by this viewer
     */
    public ProffenViewController(DataSet data_set) {
        this(data_set, null); 
    }
  
    /**
     * @param data_set The DataSet to be viewed by this viewer
     * @param state    The viewer state
     */
    public ProffenViewController(DataSet data_set, ViewerState state) {
      
        super(data_set, state);
        ArrayMaker = new DataSetGRCTArrayMaker(data_set, null);
        if (state == null)
            state = new ViewerState();
        
        this.state = state;

        mode = state.get_int(ViewerState.SLICEVIEWMODE);
        if (mode == 0)
            Viewer = new ImageViewComponent((IVirtualArray2D) ArrayMaker.getArray());
        else
            Viewer = new DataSetTools.viewer.Table.LargeJTableViewComponent
                                      (null, (IVirtualArray2D) ArrayMaker.getArray());
      ArrayMaker.addActionListener(new ArrayMakerActionListener());
        //Viewer.dataChanged((IVirtualArray2D) ArrayMaker.getArray());
        init();
    }
    

    // Initializes max and mins and controls
    private void init() {
      
        
       
        JPanel ControlPanel = new JPanel();
        BoxLayout layoutManager = new BoxLayout(ControlPanel, 
                                                  BoxLayout.Y_AXIS);

        ControlPanel.setLayout(layoutManager);
        JComponent[] ArrayControls = ArrayMaker.getSharedControls();

        ControlPanel.add(ArrayControls[0]);
        //----------------- tabbed panels---------------------------
        //          --------------- for Array-----------------------
        ControlBlock = new JTabbedPane();

        for (int i = 2; i + 1 < ArrayControls.length; i += 2) {
            JPanel jp = new JPanel();
            BoxLayout bLayout = new BoxLayout(jp, BoxLayout.Y_AXIS);

            jp.setLayout(bLayout);
            jp.add(ArrayControls[i]);
            jp.add(ArrayControls[i + 1]);
            String title = ((ViewControl) ArrayControls[i]).getTitle();

            ControlBlock.addTab(title, jp);
        }
        ControlBlock.addTab("Conversions", ArrayControls[1]);
        //         ------------- for viewer -------------------
        SetUpViewComponent( ControlBlock, ControlPanel);
       // -------------------- Set Up Menus ------------------------------------
     
        JRadioButtonMenuItem  
                ShowImage =  new JRadioButtonMenuItem("Show Image"),
                ShowTable = new JRadioButtonMenuItem("Show Table");

        ShowImage.addActionListener(new ChangeViewer(0));
        ShowTable.addActionListener(new ChangeViewer(1));
        ButtonGroup MViewer = new ButtonGroup();

        MViewer.add(ShowImage);
        MViewer.add(ShowTable);
        if (mode == 0)
            ShowImage.setSelected(true);
        else
            ShowTable.setSelected(true);
        JMenuBar menuBar = this.getMenuBar();
        JMenu opt = menuBar.getMenu(DataSetViewer.OPTION_MENU_ID);

        opt.add(ShowImage);
        opt.add(ShowTable);
        if (Viewer instanceof IMarkerAddible)
            ((IMarkerAddible) Viewer).addMarker(ArrayMaker.getMarkers());
        invalidate();
    }

    private void SetUpViewComponent( JTabbedPane ControlBlock, JPanel ControlPanel){
    
          if( ViewViewPanel != null)
              ViewViewPanel.removeAll();
          ViewViewPanel = new JPanel(new GridLayout(1, 1));
          BoxLayout bLayout = new BoxLayout(ViewViewPanel, BoxLayout.Y_AXIS);
          Viewer.addActionListener(new ViewerActionListener());
          ViewViewPanel.setLayout(bLayout);
        
          SetUpNewViewer();
      
          ControlBlock.addTab("View", ViewViewPanel);
       
          ControlPanel.add(ControlBlock);
          ControlPanel.add(PanHolder);
          //--------------- Set up DataSetViewer ------------------------
          Float F = new Float(20f);

          if (state_Controller != null)
              F = (Float) state_Controller.get(ViewerState.CONTROL_WIDTH);
          float f = 20;

          if (!F.isNaN())
              f = F.floatValue();
          setLayout(new GridLayout(1, 1));
          ViewHolder.removeAll();
          ViewHolder.add(Viewer.getDisplayPanel());
          removeAll();
          add(new SplitPaneWithState(JSplitPane.HORIZONTAL_SPLIT,
                  ViewHolder, ControlPanel, 1 - f / 100));
    }                


    private void SetUpNewViewer() {
      
        vcomp = Viewer.getControls();
        pan = null;
        for (int i = 0; i < vcomp.length; i++)
            if (vcomp[i].getTitle() != ImageViewComponent.PAN_NAME)
                  ViewViewPanel.add(vcomp[i]);
            else
                pan = vcomp[i];
        if (pan != null)
            PanHolder.add(pan);
       
            //--------------- Add ControlBlock to Control Panel ----------
      

        Men = Viewer.getMenuItems();
        JMenuBar menuBar = this.getMenuBar();

        SetUpMenuItems(menuBar, Men);
    }


    private int indexx(int x, int def) {
      
        if (x >= 0)
            return x;
        return def;
    }


    private void SetUpMenuItems(JMenuBar menuBar, ViewMenuItem[] Men) {
    
        for (int i = 0; i < Men.length; i++) {
            ViewMenuItem vm = Men[i];
            String path = vm.getPath();
            int j = 0;
            JMenu jm = null;

            for (int k = indexx(path.indexOf("."), path.length()); 
                                                      j < path.length();
                k = indexx(path.indexOf(".", k + 1), path.length())) {
                String item = path.substring(j, k);
                JMenu  jm1;

                if (j == 0) {
          
                    jm1 = find(item, menuBar);
                    if (jm1 == null) {
                        jm1 = new JMenu(item);
                        menuBar.add(jm1);
                    }
                } else {
                    jm1 = find(item, jm);
                    if (jm1 == null) {
                        jm1 = new JMenu(item);
                        jm.add(jm1);
               
                    }
                } 
                jm = jm1;
                j = k + 1;
            }
            jm.add(vm.getItem());
        }
    }
   
   
   
    private JMenu find(String item, JMenuBar menuBar) {
      
        for (int i = 0; i < menuBar.getMenuCount(); i++)
            if (menuBar.getMenu(i).getText().equals(item))
                return menuBar.getMenu(i);
        return null;
    }
    
    
   
    private JMenu find(String item, JMenu menu) {
      
        if (menu == null)
            return null;
        for (int i = 0; i < menu.getItemCount(); i++)
            if (menu.getItem(i) instanceof JMenu)
                if (menu.getItem(i).getText().equals(item))
                    return (JMenu) menu.getItem(i);
        return null;
   
    }
    
    

    /* 
     * @see DataSetTools.viewer.DataSetViewer#redraw(java.lang.String)
     */
    public void redraw(String reason) {
     
        if (reason == IObserver.POINTED_AT_CHANGED) {
            int ptIndx = this.getDataSet().getPointedAtIndex();
            float time =this.getDataSet().getPointedAtX();
            if(ArrayMaker.getCurrentPointedAtIndex()==ptIndx)
              if(ArrayMaker.getCurrentPointedAtTime()==time)
                return;
            floatPoint2D fpt = ArrayMaker.redrawNewSelect(reason);

            if (fpt != null)
                Viewer.setPointedAt(fpt);
            Viewer.getDisplayPanel().repaint();
            
        }	
    }



    //--------------------IPreserveState Methods------------------
    /**
     *  Method required by the IPreserveState interface
     */
    public void setObjectState(ObjectState new_state) {
      
        if (new_state == null)
            return;
            
        TotalObjectState = new_state;
        state_Controller = (ObjectState) TotalObjectState.get("Controller"); 
        state_ViewComponent = (ObjectState) TotalObjectState.get("View"); 
        state_ArrayMaker = (ObjectState) TotalObjectState.get("Model"); 
        Viewer.setObjectState(state_ViewComponent);
        ArrayMaker.setObjectState(state_ArrayMaker);
    }
  
  
  
    /**
     * Method required by the IPreserveState interface
     * @see gov.anl.ipns.ViewTools.Components.IPreserveState#getObjectState(boolean)
     */
    public ObjectState getObjectState(boolean is_default) {
      
        if (!is_default)
            return TotalObjectState;
     
        ObjectState view, Contr, model;

        view = Viewer.getObjectState(true);
        model = ArrayMaker.getObjectState(true);
        Contr = new ObjectState();
        Contr.insert("ImageTable", new Boolean(true));
        Contr.insert(ViewerState.CONTROL_WIDTH, new Float(.3));
        ObjectState Res = new ObjectState();

        Res.insert("Controller", Contr);
        Res.insert("View", view);
        Res.insert("Model", model);
        return Res;
    }
  
  
  
 
    public ViewerState getState() {
      
        return state;
    }
 
 
 
    public static void main(String args[]) {
      
        DataSet[] DSS = null;

        try {
   
            DSS = Command.ScriptUtil.load(
                              "C:/ISAW-old/SampleRuns/scd08339.run");
        } catch (Exception ss) {
            System.exit(0);
        }
        DataSet DS = DSS[DSS.length - 1];
        ProffenViewController profView = new ProffenViewController(DS, null);
        JFrame jf = new JFrame("Test");

        jf.getContentPane().setLayout(new GridLayout(1, 1));
        jf.getContentPane().add(profView);
        jf.setJMenuBar(profView.getMenuBar());
        jf.setSize(800, 600);
        WindowShower.show(jf);
     
    }
 
   //---------------------- Listeners---------------------------
    class ViewerActionListener implements ActionListener {
      
        public void actionPerformed(ActionEvent evt) {
       
            if (evt.getActionCommand() == IViewComponent.POINTED_AT_CHANGED) {
                floatPoint2D pt = Viewer.getPointedAt();
                
                ArrayMaker.setPointedAt(pt);
            }
        }
    }//ViewerActionListener
 


    class ArrayMakerActionListener implements ActionListener {
      
        public void actionPerformed(ActionEvent evt) {
          
            if (evt.getActionCommand().equals(IArrayMaker.DATA_CHANGED)) {
              
               
                Viewer.dataChanged((IVirtualArray2D) ArrayMaker.getArray());
               //DataChanged();
                
                if (Viewer instanceof IMarkerAddible) {
                    ((IMarkerAddible) Viewer).removeAllMarkers();
                    ((IMarkerAddible) Viewer).addMarker(
                                                 ArrayMaker.getMarkers());
                }
                repaint();
        
            }  
        }
      public void DataChanged(){
        int mode = 0;
        if( state != null)
           mode=state.get_int(ViewerState.SLICEVIEWMODE);
        if (mode == 0)
          Viewer = new ImageViewComponent((IVirtualArray2D) ArrayMaker.getArray());
        else
           Viewer = new DataSetTools.viewer.Table.LargeJTableViewComponent
                                             (null, (IVirtualArray2D) ArrayMaker.getArray());
        if( state_Controller != null)
          if( state_Controller.get("View")!= null)
           Viewer.setObjectState( (ObjectState)state_Controller.get("View"));
        Viewer.addActionListener(new ViewerActionListener());
        ViewHolder.removeAll();
        ViewHolder.add( Viewer.getDisplayPanel());
        ViewViewPanel.removeAll();
        PanHolder.removeAll();
        vcomp = Viewer.getControls();
        pan = null;
        for (int i = 0; i < vcomp.length; i++)
           if (vcomp[i].getTitle() != ImageViewComponent.PAN_NAME)
               ViewViewPanel.add(vcomp[i]);
           else
               pan = vcomp[i];
           if (pan != null)
             PanHolder.add(pan);
         ViewHolder.repaint();
         ViewViewPanel.repaint();
         PanHolder.repaint();
      }
    }//ArrayMakerActionListener



    // Changes from table view component to Image View component
    class ChangeViewer implements ActionListener {
      
        int mode;
        public ChangeViewer(int mode) {
          
            this.mode = mode;
        }  

        public void actionPerformed(ActionEvent evt) {
          
            if (mode == 0)
                if (Viewer instanceof ImageViewComponent)
                    return;
            if (mode == 1)
                if (!(Viewer instanceof ImageViewComponent))
                    return;
                    
            PanHolder.removeAll();
            ViewPanel.removeAll();
            ViewHolder.removeAll();
            //eliminate menu items
            JMenuBar bar = getMenuBar();

            for (int i = 0; i < Men.length; i++) {
              
                String path = Men[i].getPath();
                int k = path.indexOf('.');

                if (k < 0)
                    k = path.length();
                String head = null;

                if (k > 0)
                    head = path.substring(0, k);
                int kPos = 0;

                if (head != null)
                    for (kPos = 0; (kPos < bar.getMenuCount()) && !head.equals(bar.getMenu(kPos).getText());
                        kPos++) {}
                if (kPos >= bar.getMenuCount())
                    head = null;
                if (head != null) {
                    String next = Men[i].getItem().getText();

                    if (k < path.length()) {
                        int k1 = path.indexOf('.', k + 1);

                        if (k1 < 0)
                            k1 = path.length();
                        next = path.substring(k + 1, k1);
                    }
                    JMenu jmenu = bar.getMenu(kPos);
                    int kk = 0;

                    for (kk = 0; (kk < jmenu.getItemCount()) && 
                                 !jmenu.getItem(kk).getText().equals(next);
                        kk++) {}
                    if (kk < jmenu.getItemCount())
                        jmenu.remove(kk);
                }
       
            }//For each view menu component

            //------------------ Now recreate the view-------------
            if (mode == 0)
                Viewer = new ImageViewComponent(new VirtualArray2D(10, 10));
            else
                Viewer = new DataSetTools.viewer.Table.LargeJTableViewComponent
                                       (null, new VirtualArray2D(10, 10));
        
            Viewer.dataChanged((IVirtualArray2D) ArrayMaker.getArray());
            Viewer.addActionListener(new ViewerActionListener());
            SetUpNewViewer();
      
            state.set_int(ViewerState.SLICEVIEWMODE, mode);
    
            ViewHolder.add(Viewer.getDisplayPanel()); 
            ViewHolder.validate();
        }
    }//ChangeViewer
}
