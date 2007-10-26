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
 * Revision 1.3  2007/10/26 22:40:28  amoe
 * -In the ViewMenuListener, made it so a viewer will not be shown/set if the
 * DataSet array length is 0 or less.
 *
 * -Made the ViewMenuListener not keep a reference of new ViewManagers.
 *
 * Revision 1.2  2007/10/23 06:50:42  amoe
 * -Changed DataSet to Dataset[].  Single DataSets will be retrieved from this.
 * -Made the ViewMenuListener ignore empty datasets.
 * -Added setVisibleAddViewerItem(..) .
 *
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
import DataSetTools.viewer.IViewManager;
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
  private DataSet[] dss;
  
  private String[] viewer_flag_list;

  private JMenuItem additional_viewer;
  private JSeparator separator;
  
  public UnifiedViewMenu()
  {
    this("X",null,null);
  }
  
  public UnifiedViewMenu(DataSet ds)
  {        
    this( "X", new DataSet[]{ds}, null );
  }
  
  public UnifiedViewMenu(DataSet[] dss)
  {
    this( "X", dss, null );
  }
  
  public UnifiedViewMenu(ViewManager vm)
  {
    this( "X", new DataSet[]{vm.getDataSet()}, vm );
  }
  
  public UnifiedViewMenu(String name)
  {        
    this(name, null, null );
  }
  
  /**
   * This constructor takes a <code>DataSet</code> and creates an instance of 
   * the <code>UnifiedViewMenu</code>.  This will eventually create a new 
   * <code>ViewManager</code> that uses the specified <code>DataSet</code>.
   * 
   * @param ds - This is the <code>DataSet</code> that will be shown in a 
   * viewer, in the <code>ViewManager</code>.
   */
  public UnifiedViewMenu(String name, DataSet ds)
  {        
    this( name, new DataSet[]{ds}, null );
  }
  
  public UnifiedViewMenu(String name, DataSet[] dss)
  {
    this(name,dss,null);
  }
  
  /**
   * This constructor takes a <code>ViewManager</code> and creates an instance 
   * of the <code>UnifiedViewMenu</code>.  Here, the <code>DataSet</code> that 
   * will be viewed is already contained within the <code>ViewManager</code>.
   * 
   * @param vm - This is the <code>ViewManager</code> that will be used by 
   * the <code>UnifiedViewMenu</code>.
   */
  public UnifiedViewMenu(String name, ViewManager vm)
  {
    this( name, new DataSet[]{vm.getDataSet()}, vm );
  }
  
  /**
   * This constructor takes a <code>DataSet</code> and a 
   * <code>ViewManager</code> and creates an instance of 
   * <code>UnifiedViewMenu</code>.
   */
  private UnifiedViewMenu(String name, DataSet[] dss, ViewManager vm)
  {
    this.dss = dss;
    view_manager = vm;
    
    //setting this view menu title
    this.setText(name);
    
    additional_viewer = new JMenuItem(ViewManager.ADDITIONAL_VIEW);
    separator = new JSeparator();
    
    //init menu
    init_view_menu(ViewManager.getViewList());
  }
  
  public void setDataSet(DataSet ds)
  {
    this.dss = new DataSet[]{ds};
  }
  
  public void setDataSetArray(DataSet[] dss)
  {
    this.dss = dss;
  }
  
  public void setVisibleAddViewerItem(boolean bool)
  {
    additional_viewer.setVisible(bool);
    separator.setVisible(bool);
  }
  
  private void init_view_menu(String[] v_list)
  {
    viewer_flag_list = (String[])v_list.clone();
    
    JMenuItem viewer_item;
    ViewMenuListener menu_listener = new ViewMenuListener();
    
    //adding Additional View menu item
    additional_viewer.addActionListener(menu_listener);    
    this.add(additional_viewer);
    this.add(separator);
    
    //setting up viewer lists in viewmenu
    for(int i=0;i<viewer_flag_list.length;i++)
    {
      viewer_item = new JMenuItem(viewer_flag_list[i]);
      viewer_item.addActionListener(menu_listener);      
      this.add(viewer_item);
    }
  }

  
  /*
   * ---Listeners----------------------------------------
   */
  /**
   * The <code>ViewMenuListener<code> is responsible for listening to 
   * the menu item selections in the <code>UnifiedViewMenu</code>.
   */
  private class ViewMenuListener implements ActionListener
  {
    public void actionPerformed( ActionEvent e )
    {
      String viewer_flag = e.getActionCommand();
      String err_no_data = "Cannot open DataSetViewer, no Data specified.";
      String err_empty_dataset= "Cannot open DataSetViewer, empty DataSet";

      if(dss != null && dss.length > 0)
      {
        if(view_manager == null)
        {
          // If the flag says "Additional View", then create a ViewManager for each
          // dataset in dss with a default view.
          if(viewer_flag.equals(ViewManager.ADDITIONAL_VIEW))
          {            
            for(DataSet dataset: dss)
            {
              if(dataset == DataSet.EMPTY_DATA_SET)                
                SharedMessages.addmsg(err_empty_dataset);
              else
                /*view_manager =*/ new ViewManager(dataset,ViewManager.IMAGE,true);
            }
          }
          
          //Create the ViewManager for each dataset in dss, with the specified view.
          else 
          {
            for(DataSet dataset: dss)
            {
              if(dataset == DataSet.EMPTY_DATA_SET)                
                SharedMessages.addmsg(err_empty_dataset);
              else
                /*view_manager =*/ new ViewManager(dataset,viewer_flag,true);
            }
          }
        }
        else
        {
          // If the flag says "Additional View", then create a ViewManager for 
          // each dataset with the current view.
          if(viewer_flag.equals(ViewManager.ADDITIONAL_VIEW))
          {
            for(DataSet dataset: dss)
            {
              if(dataset == DataSet.EMPTY_DATA_SET)                
                SharedMessages.addmsg(err_empty_dataset);
              else
                /*view_manager = */new ViewManager(dataset,view_manager.getView(),true);
            }
          }
          else
          {        	
        	//view_manager.setDataSet(dss[0]);  
            view_manager.setView(viewer_flag);
            
            //DBG
            System.out.println("Current DS: "+dss[0].getTitle());
            System.out.println("view_manager DS: "+view_manager.getDataSet().getTitle());
            //-DBG
          
            if(!view_manager.isVisible())
              WindowShower.show( view_manager );
            
            //if(dss != null)
            //{
            //  for(DataSet dataset: dss)
            //    view_manager = new ViewManager(dataset,viewer_flag,true);
            //}
          }        
        }
      }   
      else
      {      
        SharedMessages.addmsg(err_no_data);
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
    
    String filename1 = "/home/moea/workspace/ISAW/SampleRuns/SCD06496.RUN";
    RunfileRetriever rr1 = new RunfileRetriever(filename1);    
    DataSet ds1 = rr1.getDataSet(1);
    String filename2 = "/home/moea/workspace/ISAW/SampleRuns/SCD06497.RUN";
    RunfileRetriever rr2 = new RunfileRetriever(filename2);    
    DataSet ds2 = rr2.getDataSet(1);
    DataSet[] dss = new DataSet[]{ds2,ds1};
    
    //testing all possible UVM combinations
    //UnifiedViewMenu uvm = new UnifiedViewMenu();  //kinda-OK!!!NNN
    UnifiedViewMenu uvm = new UnifiedViewMenu(ds1); //kinda-OK!!!NNN
    //UnifiedViewMenu uvm = new UnifiedViewMenu(dss); //kinda-OK!!!NNN
    //UnifiedViewMenu uvm = new UnifiedViewMenu("MyMenuName");  //kinda-OK!!!NNN
    //UnifiedViewMenu uvm = new UnifiedViewMenu("MyMenuName",ds1); //kinda-OK!!!NNN
    //UnifiedViewMenu uvm = new UnifiedViewMenu("MyMenuName",dss);  //kinda-OK!!!NNN
     
     //ViewManager vm = new ViewManager(ds1,IViewManager.THREE_D,true);
     //UnifiedViewMenu uvm = new UnifiedViewMenu(vm);  //kinda-OK!!!
     //UnifiedViewMenu uvm = new UnifiedViewMenu("MyMenuName",vm);  //kinda-OK!!!
    
    //uvm.setDataSetArray(dss);
    
    uvm.setVisibleAddViewerItem(false);
     
    //display test frame   
    jmb.add(uvm);
    jf.getContentPane().add(jmb,BorderLayout.NORTH);
    jf.setVisible(true);
  }

}
