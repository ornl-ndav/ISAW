/* 
 * File: GraphViewHandler.java
 *
 * Copyright (C) 2009, Dennis Mikkelson 
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.ShowEventsApp.ViewHandlers;

import java.awt.GridLayout;

import javax.swing.*;

import gov.anl.ipns.ViewTools.Components.AxisInfo;
import gov.anl.ipns.ViewTools.Components.OneD.*;
import MessageTools.*;

/**
 * Abstract base class for classes that display a graph of x,y values
 * in response to messages.  
 */
abstract public class GraphViewHandler implements IReceiveMessage
{
   protected MessageCenter messageCenter;
   protected String        frame_title;
   protected String        title;
   protected String        x_units;
   protected String        y_units;
   protected String        x_label;
   protected String        y_label;

   private   JPanel        place_holder_panel;
   private   JFrame        display_frame;
   private FunctionViewComponent  fvc;
   //private   JPanel        graphPanel;
   

   /**
    * Construct the class using the specified MessageCenter.  Derived
    * class constructors will specify the title Strings and the 
    * messages that graph responds to.
    *
    * @param messageCenter
    */
   protected GraphViewHandler(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      this.place_holder_panel = placeholderPanel();
   }

   
   /**
    * Creates a new JFrame to display the graph every time
    * it is called.  Will display a graph if its been built
    * or will display a placeholder saying no data loaded.
    */
   protected void ShowGraph()
   {
      display_frame = new JFrame(frame_title);
      display_frame.getContentPane().setLayout(new GridLayout(1,1));
      display_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      display_frame.setBounds(0, 0, 1000, 500);
      display_frame.setVisible(true);
      
      if (fvc != null)
         display_frame.getContentPane().add(fvc.getDisplayPanel());
      else
         display_frame.getContentPane().add(place_holder_panel);
      
     
      display_frame.repaint();
      display_frame.validate();
      display_frame.getContentPane().repaint();
      if( fvc != null)
         fvc.paintComponents();
   }


   /**
    * Hide the graph by disposing of the JFrame that contains it.
    */
   protected void HideGraph()
   {
      display_frame.dispose();
   }

   
   /**
    * Placeholder to put in the frame if no data is loaded.
    * 
    * @return Panel
    */
   private JPanel placeholderPanel()
   {
      JPanel placeholderpanel = new JPanel();
      placeholderpanel.setLayout(new GridLayout(1,1));
      
      JLabel label = new JLabel("No Data Loaded!");
      label.setHorizontalAlignment(JLabel.CENTER);
      
      placeholderpanel.add(label);
      
      return placeholderpanel;
   }
   

   /**
    * Takes the data and creates an instance of
    * FunctionViewComponent and adds it to the graphPanel
    * and then to the frame if the frame has been created.
    * This allows for the graph to be updated while the frame 
    * is displayed.
    * 
    * @param xyValues X,Y values of the data for the graph.
    */
   private synchronized void  setPanelInformation(float[][] xyValues)
   {
      float[] x_values = xyValues[0];
      float[] y_values = xyValues[1];
      float[] errors = null;

      if(display_frame != null)
         display_frame.getContentPane().removeAll();
      if( fvc == null)
      {
         String prop_str = System.getProperty("ShowWCToolTip");
         System.setProperty("ShowWCToolTip","true");
        
         fvc = FunctionViewComponent.getInstance(       
             x_values, y_values, errors, title, x_units, y_units, x_label, y_label);
         
         if(prop_str == null)
            System.clearProperty( "ShowWCToolTip" );
         else
            System.setProperty(  "ShowWCToolTip" , prop_str );
         
      }else
      {  
         VirtualArrayList1D varr =new VirtualArrayList1D( new DataArray1D(x_values,y_values,errors,title,false,false));
         AxisInfo xAxis= varr.getAxisInfo( AxisInfo.X_AXIS );
         AxisInfo yAxis =varr.getAxisInfo( AxisInfo.Y_AXIS );
         varr.setAxisInfo( AxisInfo.X_AXIS , xAxis );
         varr.setAxisInfo( AxisInfo.Y_AXIS , yAxis );
         fvc.dataChanged( varr );
      }
      if (display_frame != null)
      {
         display_frame.getContentPane().removeAll();
         display_frame.getContentPane().add(fvc.getDisplayPanel());
         if (display_frame != null)
         {  
            
            display_frame.getContentPane().validate();
            if( fvc != null)
            { fvc.getDisplayPanel().invalidate();
              fvc.paintComponents();
            }
           
         }
      }
   }
   
  
   public class setInfoThread extends Thread
   {
     float[][] xy_values;

     public setInfoThread( float[][] xy_values )
     {
       this.xy_values = xy_values;
     }

     public void run()
     {
       setPanelInformation( xy_values );
     }
   }


   protected void setInfo( float[][] values )
   {
     Thread set_info_thread = new setInfoThread( values );
     SwingUtilities.invokeLater( set_info_thread );
   }


   /**
    * Receive messages to display the frame, hide the frame,
    * get the xy values, and set the values/create the graph.
    */
   abstract public boolean receive(Message message);

}
