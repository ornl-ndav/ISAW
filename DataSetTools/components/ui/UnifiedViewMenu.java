/*
 * File:  UnifiedViewMenu.java
 *
 * Copyright (C) 2007, Andrew Moe
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
 * Revision 1.1  2007/10/19 21:23:10  amoe
 * Initial commit.
 *
 * 
 */
package DataSetTools.components.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import gov.anl.ipns.Util.Sys.SharedMessages;
import gov.anl.ipns.Util.Sys.WindowShower;

import DataSetTools.dataset.DataSet;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.viewer.ViewManager;

/**
 * <code>UnifiedViewMenu</code> is a universal menu for selecting a 
 * DataSetViewer and viewing it through the <code>ViewManager</code>.
 * 
 * @author moea
 */
public class UnifiedViewMenu extends JMenu
{
  private ViewManager view_manager;
  private DataSet dataset;
  
  private String[] viewer_flag_list;

  
  public UnifiedViewMenu(String title)
  {        
    this(title, null, null );
  }
  
/**
 * This constructor takes a <code>DataSet</code> and creates an instance of 
 * the <code>UnifiedViewMenu</code>.  This will eventually create a new 
 * <code>ViewManager</code> that uses the specified <code>DataSet</code>.
 * 
 * @param ds - This is the <code>DataSet</code> that will be shown in a 
 * viewer, in the <code>ViewManager</code>.
 */
  public UnifiedViewMenu(String title, DataSet ds)
  {        
    this( title, ds, null );
  }
  
  /**
   * This constructor takes a <code>ViewManager</code> and creates an instance 
   * of the <code>UnifiedViewMenu</code>.  Here, the <code>DataSet</code> that 
   * will be viewed is already contained within the <code>ViewManager</code>.
   * 
   * @param vm - This is the <code>ViewManager</code> that will be used by 
   * the <code>UnifiedViewMenu</code>.
   */
  public UnifiedViewMenu(String title, ViewManager vm)
  {
    this( title, vm.getDataSet(), vm );
  }
  
  /**
   * This constructor takes a <code>DataSet</code> and a 
   * <code>ViewManager</code> and creates an instance of 
   * <code>UnifiedViewMenu</code>.
   */
  private UnifiedViewMenu(String title, DataSet ds, ViewManager vm)
  {
    dataset = ds;
    view_manager = vm;
    
    //setting this view menu title
    this.setText(title);
       
    //init menu
    init_view_menu(ViewManager.getViewList());
  }
  
  public void set_dataset(DataSet ds)
  {
    dataset = ds;
  }
  
  private void init_view_menu(String[] v_list)
  {
    viewer_flag_list = (String[])v_list.clone();
    
    ViewMenuListener menu_listener = new ViewMenuListener();
    
    JMenuItem viewer_add_view;
    //JSeparator separator;
    
    //adding Additional View menu item
    viewer_add_view = new JMenuItem(ViewManager.ADDITIONAL_VIEW);
    //separator = new JSeparator();
    viewer_add_view.addActionListener(menu_listener);
    
    this.add(viewer_add_view);
    //this.add(separator);
    
    //setting up viewer lists in viewmenu
    for(int i=0;i<viewer_flag_list.length;i++)
    {
      viewer_add_view = new JMenuItem(viewer_flag_list[i]);
      //viewer_list_item.setVisible(SHOW_VIEWER_LIST);
      viewer_add_view.addActionListener(menu_listener);
      
      this.add(viewer_add_view);
    }
    
    
  }
  
  /*
  private void init_directory_list(String[] d_list)
  {
    directory_flag_list = (String[])d_list.clone();
    
    //setting up directory lists in viewmenu
    for(int i=0;i<directory_flag_list.length;i++)
    {
      JMenu viewer_list_item = new JMenu(directory_flag_list[i]);
      viewer_list_item.setVisible(SHOW_DIRECTORY_LIST);
      viewer_list_item.addActionListener(new ViewMenuListener());
      
      this.add(viewer_list_item);
    }
  }
  */
  
  /*
  //This is a temporary method for specifying the available viewers.
  private String[] temp_get_viewer_list()
  {
    return new String[]{
        IViewManager.IMAGE,
        IViewManager.SELECTED_GRAPHS,
        IViewManager.DIFFERENCE_GRAPH,
        IViewManager.SCROLLED_GRAPHS,
        IViewManager.PARALLEL_YofX,
        IViewManager.GRX_Y,        
        IViewManager.INSTRUMENT_TABLE,
        IViewManager.THREE_D,
        
        IViewManager.CONTOUR,
        IViewManager.TWO_D_VIEWER,
        IViewManager.HKL_SLICE,
        IViewManager.SLICE_VIEWER,
        IViewManager.COUNTS_X_Y,
        
        IViewManager.TABLE,
        IViewManager.CONTOUR_QY_QZ_vs_QX,
        IViewManager.CONTOUR_QX_QY_vs_QZ,
        IViewManager.CONTOUR_QXYZ_SLICES,        
        };
  }
  */
  
  
  
  /*
   * ---Listeners----------------------------------------
   */
  /**
   * The <code>ViewMenuListener<code> is responsible for listening to 
   * the menu item selections in the <code>UnifiedViewMenu</code>.
   */
  private class ViewMenuListener implements ActionListener
  {
    public void actionPerformed( ActionEvent e)
    {
      String viewer_flag = e.getActionCommand();
      
      if(dataset == null)
      {
        //System.err.println("Cannot open DataSetViewer, no Dataset specified.");        
        SharedMessages.addmsg("Cannot open DataSetViewer, no Dataset specified.");
      }
      else
      {
        if(view_manager == null)
        {
          // If the flag says "Additional View", then create a ViewManager with 
          // a default view.
          if(viewer_flag.equals(ViewManager.ADDITIONAL_VIEW))
            view_manager = new ViewManager(dataset,ViewManager.IMAGE,true);
          
          else //Create the ViewManager with the specified view.
            view_manager = new ViewManager(dataset,viewer_flag,true);
        }
        else
        {
          // If the flag says "Additional View", then create a ViewManager with 
          // the current view.
          if(viewer_flag.equals(ViewManager.ADDITIONAL_VIEW))
            view_manager = new ViewManager(dataset,view_manager.getView(),true);
          
          else  //Set the ViewManager with the specified view.
          {
            view_manager.setView(viewer_flag);
          
            if(!view_manager.isVisible())
              WindowShower.show( view_manager );
          }        
        }
      }      
    }    
  }
  
  /*
   * ---Main Test----------------------------------------
   */
  public static void main(String[] args)
  {
    //setting up test frame
    JFrame jf = new JFrame();
    jf.setSize(500,500);
    jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );    
    JMenuBar jmb = new JMenuBar();
    
    //setting up test dataset
    /*
     * DataSet ds = new DataSet();    
    float[][] testData = ContourViewComponent.getTestDataArr(41,51,3,4);
    for (int i=0; i<testData.length; i++)
       ds.addData_entry(
             new FunctionTable(new UniformXScale(0, 
                               testData[i].length-1, 
                               testData[i].length),
                               testData[i], 
                               i));
    ds.setSelectFlag(5, true);
    ds.setSelectFlag(6, true);
    */
    
    String filename = "/home/moea/workspace/ISAW/SampleRuns/SCD06496.RUN";
    RunfileRetriever rr = new RunfileRetriever(filename);
    DataSet ds = rr.getDataSet(1);
    
    //setting up menus
    //UnifiedViewMenu uvm = new UnifiedViewMenu(ds);
    //UnifiedViewMenu uvm = new UnifiedViewMenu(
    //                 new ViewManager(ds,IViewManager.SCROLLED_GRAPHS,true));
    UnifiedViewMenu uvm = new UnifiedViewMenu("MyMenu");
    uvm.set_dataset(ds);    
    
    //display test frame   
    jmb.add(uvm);
    jf.getContentPane().add(jmb,BorderLayout.NORTH);
    jf.setVisible(true);
  }

}
