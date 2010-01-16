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

import java.awt.Point;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*;

import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Components.AxisInfo;
import gov.anl.ipns.ViewTools.Components.IVirtualArrayList1D;
import gov.anl.ipns.ViewTools.Components.ObjectState;
import gov.anl.ipns.ViewTools.Components.OneD.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.LabelCombobox;
import gov.anl.ipns.ViewTools.Components.ViewControls.ViewControl;
import gov.anl.ipns.Util.Sys.*;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.util.SharedData;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.Util;
import MessageTools.*;

/**
 * Abstract base class for classes that display a graph of x,y values
 * in response to messages.  
 */
abstract public class GraphViewHandler implements IReceiveMessage,
                                                  IhasWindowClosed
{
   protected MessageCenter messageCenter;
   protected String        frame_title;
   protected String        title;
   protected String        x_units;
   protected String        y_units;
   protected String        x_label;
   protected String        y_label;
   protected  boolean      normalize;
   private   JPanel        place_holder_panel;
   private   JFrame        display_frame;
   private   Dimension     size = new Dimension( 900, 300 );
   private   Point         location = new Point( 200, 300 );
   private   FunctionViewComponent  fvc;
   DataArray1D  CompareGraph  = null;
   

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
      normalize= false;
   }

   public abstract void WindowClose( String ID);
        

   /**
    * Creates a new JFrame to display the graph every time
    * it is called.  Will display a graph if its been built
    * or will display a placeholder saying no data loaded.
    */
   protected void ShowGraph()
   {
      if( display_frame != null)
         return;
      
      display_frame = new FinishJFrame(frame_title);
      String D_Q ="D";
      if( frame_title.indexOf( "Q" )>=0)
         D_Q="Q";
      display_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      display_frame.addWindowListener( new IndirectWindowCloseListener(this,D_Q));
      if (fvc != null)
         display_frame.getContentPane().add(fvc.getDisplayPanel());
      else
         display_frame.getContentPane().add(place_holder_panel);

      display_frame.setVisible(true);
      display_frame.setBounds( location.x,
                               location.y,
                               (int)(size.getWidth()),
                               (int)(size.getHeight()) );
      JMenuBar menBar = new JMenuBar();
      JMenu opts = new JMenu("Options");
      menBar.add(opts);
      
      JCheckBoxMenuItem normalize = new JCheckBoxMenuItem("Normalize", false);
      opts.add( normalize);
      normalize.addActionListener( new MenuListener( this, D_Q) );
      
      JMenuItem Load = new JMenuItem("Compare to Data in File");
      Load.addActionListener(  new MenuListener( this ,D_Q) );
      opts.add( Load );
      
      
      JMenuItem Help = new JMenuItem("Help");
      Help.addActionListener(  new MenuListener( this ,D_Q) );
      opts.add( Help );

      JMenuItem Clear = new JMenuItem("Clear Compare Data");
      Clear.addActionListener(  new MenuListener( this ,D_Q) );
      opts.add( Clear );
      
      display_frame.setJMenuBar( menBar );
      
   }


   /**
    * Hide the graph by disposing of the JFrame that contains it.
    */
   protected void HideGraph()
   {
      if ( display_frame != null )
      {
        location = display_frame.getLocation();
        size     = display_frame.getSize();
        display_frame.setVisible(false);
        display_frame.dispose();
        display_frame = null;
      }
   }

   public void setOtherGraph( String fileName, float ScaleFactor)
   {
      if( fileName == null)
      {
         CompareGraph = null;
         AddGraph();
         return;
      }
      if( Float.isNaN(ScaleFactor) || ScaleFactor <=0 )
         ScaleFactor = 1;
      DataSet DS = Util.ReadDSFile( fileName );
      if( DS == null)
         return;
      Data D = DS.getData_entry( 0 );
      CompareGraph =  new DataArray1D(D.getX_values(),D.getY_values(),
                           D.getErrors(),"External Graph",true,false);
       AddGraph();
    
   }
   
   private void AddGraph( )
   {
      boolean redraw = false;
      if( fvc != null && CompareGraph != null)
      {
         IVirtualArrayList1D data = fvc.getArray();
         DataArray1D data1 = new DataArray1D( data.getXValues( 0 ), data.getYValues( 0 ));
         data1.setTitle( data.getGraphTitle( 0 ));
         Vector V = new Vector();
         V.add(  data1 );
         if( CompareGraph != null)
            V.add(  CompareGraph );
         fvc.dataChanged( new VirtualArrayList1D( V));
         StateChange( fvc);
         redraw = true;
         
      }else if( fvc != null)
      {
         if( fvc.getArray().getNumGraphs() > 1)
         {
            IVirtualArrayList1D data = fvc.getArray();
            DataArray1D data1 = new DataArray1D( data.getXValues( 0 ), data.getYValues( 0 ));
            data1.setTitle( data.getGraphTitle( 0 ));
            fvc.dataChanged( new VirtualArrayList1D( data1));
            StateChange( fvc);
            redraw = true;
         }
      }
      

      if( display_frame != null && redraw)
      {

         display_frame.invalidate();
         display_frame.validate();
         fvc.paintComponents();
         display_frame.repaint();
         fvc.paintComponents();
      }
       
      
   }
   private void StateChange( FunctionViewComponent fvc)
   {

      if( CompareGraph == null)
         return;
      ViewControl[] controlList = fvc.getControlList();
      ((LabelCombobox)(controlList[FunctionControls.VC_SHIFT])).setSelectedIndex( 0 );
      ((LabelCombobox)(controlList[FunctionControls.VC_LINE_SELECTED])).setSelectedIndex( 1 );
      ((LabelCombobox)(controlList[FunctionControls.VC_LINE_STYLE])).setSelectedIndex( 1 );
      ObjectState Ostate = fvc.getObjectState( false );
      if( !Ostate.reset("Graph JPanel.Graph Data2.Line Color", java.awt.Color.green));
         if(!Ostate.insert("Graph JPanel.Graph Data1.Line Color", java.awt.Color.green))
            System.out.println("Could not set color");
       fvc.setObjectState( Ostate );
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
   private synchronized void  setPanelInformation( float[][] xyValues )
   {
      float[] x_values = xyValues[0];
      float[] y_values = xyValues[1];
      float[] errors = null;

      if( fvc == null)
      {
         String prop_str = System.getProperty("ShowWCToolTip");
         System.setProperty("ShowWCToolTip","true");
        
         fvc = FunctionViewComponent.getInstance( x_values, y_values, errors, 
                                                  title, 
                                                  x_units, y_units, 
                                                  x_label, y_label);
         if(prop_str == null)
            System.clearProperty( "ShowWCToolTip" );
         else
            System.setProperty(  "ShowWCToolTip" , prop_str );

         AddGraph();
         if ( display_frame != null )
         {
           display_frame.getContentPane().removeAll();
           display_frame.getContentPane().add(fvc.getDisplayPanel());
         }
      }
      else
      {  
         Vector V = new Vector();
         V.add(new DataArray1D(x_values,y_values,errors,title,true,false));
         if( CompareGraph != null)
            V.add( new DataArray1D(CompareGraph.getXArray(), CompareGraph.getYArray(),
                     CompareGraph.getErrorArray(), CompareGraph.getTitle(), true, false));
         VirtualArrayList1D varr = 
            new VirtualArrayList1D(
                V);

         varr.setTitle( title );
         fvc.dataChanged( varr );
      }

      if( CompareGraph != null)
         StateChange(fvc);
      
      if ( display_frame != null )
      {
         display_frame.invalidate();
         display_frame.validate();
         display_frame.repaint();
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
class MenuListener implements ActionListener
{
   GraphViewHandler gv;
   JTextField textField;
   String D_Q;
   String fileName ;

   private static String OPT_MESSAGE =
       "Normalize normalizes with the incident spectrum and Protons on Target,"+
       " if present, on the \n"+
       "Load form in IsawEV.\n\n "+
       "If loading from live data, the System property \"Scale With\"(case sensitive)"+
       "must be\n"+
       "set to \"Protons on Target\"(not case sensitive) to scale with the current "+
       "Total Protons on Target";
   public MenuListener( GraphViewHandler gv, String D_Q)
   {
      this.gv = gv;
      fileName = null;
      textField = null;
      this.D_Q = D_Q;
   }
   
   private Vector  ConCat( Boolean TF, String D_Q)
   {
      Vector V = new Vector(2);
      V.add( TF);
      V.add( D_Q);
      return V;
   }
   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   @Override
   public void actionPerformed( ActionEvent evt )
   {
    if( evt.getActionCommand().equals( "Normalize"))
       gv.messageCenter.send(  new Message( Commands.NORMALIZE_QD_GRAPHS,
                  ConCat(((JCheckBoxMenuItem)(evt.getSource())).isSelected(),
                               D_Q),true,true) );
    
    else if( evt.getActionCommand().equals( "Compare to Data in File"))
    {
       FinishJFrame jf = new FinishJFrame("Enter FileName");
       jf.getContentPane().setLayout(  new GridLayout(3,1) );
       JButton but = new JButton("FileName");
       jf.getContentPane().add(  but );
       but.addActionListener( this );
       
       JPanel pan = new JPanel();
       pan.setLayout( new GridLayout(1,2));
       pan.add(  new JLabel("Scale Factor") );
       textField = new JTextField( 10);
       pan.add( textField);
       jf.getContentPane().add( pan);
       
       JButton OK = new JButton("OK");
       jf.getContentPane().add( OK );
       OK.addActionListener( this);
       jf.setSize(  300,400 );
       jf.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
       WindowShower.show(  jf );
       
       
    }else if( evt.getActionCommand().equals( "FileName"))
    {
       JFileChooser jfc = new JFileChooser();
       if( jfc.showOpenDialog( null )!= JFileChooser.APPROVE_OPTION)
          return;
       try
       {
          fileName = jfc.getSelectedFile().getCanonicalPath();
       }catch(Exception s)
       {
          SharedData.addmsg( "Error in filename "+s );
       }
    }else if( evt.getActionCommand().equals( "OK"))
    {
       if( fileName == null)
          return;
       float scale_factor =1;
       try
       {
          scale_factor = Float.parseFloat( textField.getText().trim());
       }catch( Exception s)
       {
          scale_factor =1;
       }
       gv.setOtherGraph( fileName, scale_factor);
    }else if( evt.getActionCommand().equals( "Clear Compare Data" ))
    {
       gv.setOtherGraph(  null , -1);
    }else if( evt.getActionCommand().equals( "Help" ))
    {
       JOptionPane.showMessageDialog( null , new JTextArea( OPT_MESSAGE) );
    }
      
      
   }
   
}
