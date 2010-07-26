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

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.MenuEvent;

import gov.anl.ipns.Util.File.FileIO;
import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.Util.Sys.IhasWindowClosed;
import gov.anl.ipns.Util.Sys.WindowShower;
//import gov.anl.ipns.ViewTools.Components.AxisInfo;
import gov.anl.ipns.ViewTools.Components.IVirtualArrayList1D;
import gov.anl.ipns.ViewTools.Components.ObjectState;
import gov.anl.ipns.ViewTools.Components.Menu.ViewMenuItem;
import gov.anl.ipns.ViewTools.Components.OneD.*;
import gov.anl.ipns.ViewTools.Components.ViewControls.*;
import gov.anl.ipns.ViewTools.Panels.Graph.GraphJPanel;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.Parameters.*;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.util.FilenameUtil;
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
   public static String SPECTRA_FILE ="Normalize with File";
   protected MessageCenter messageCenter;
   protected String        frame_title;
   protected String        title;
   protected String        x_units;
   protected String        y_units;
   protected String        x_label;
   protected String        y_label;
   protected  boolean      normalize;
   protected  boolean      useOtherFile;
   private   JPanel        place_holder_panel;
   private   JFrame        display_frame;
   private   Dimension     size = new Dimension( 900, 300 );
   private   Point         location = new Point( 200, 300 );
   private   FunctionViewComponent  fvc;
   DataArray1D  CompareGraph  = null;

   FileChooserPanel but = null;// for loading in file for comparison
   
   ActionListener  Menu_listener;
   JMenu AddGraph = null;
   JMenu FxnCtrls = null;
   

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

      useOtherFile = false;
   }

   public abstract void WindowClose( String ID);
    
   public void  killFunctionWindow()
   {
      if( FxnCtrls == null)
         return;
      
     JCheckBoxMenuItem chk = (JCheckBoxMenuItem) FxnCtrls.getMenuComponent(  0 );
     
     if( chk != null && chk.getState( ))
        chk.doClick( );
   }

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
      JPanel displPanel = null;
      
      if (fvc != null)
      {
         displPanel =fvc.getDisplayPanel();
         display_frame.getContentPane().add( displPanel);
      }
      else
         display_frame.getContentPane().add(place_holder_panel);

      JMenuBar menBar = new JMenuBar();
      JMenu opts = new JMenu("Options");
      menBar.add(opts);
      
      JCheckBoxMenuItem Normalize = new JCheckBoxMenuItem("Normalize", normalize);
      opts.add( Normalize);
      Normalize.addActionListener( Menu_listener );
      
      JCheckBoxMenuItem NormFileName = new JCheckBoxMenuItem(SPECTRA_FILE, useOtherFile);
      NormFileName.addActionListener(  Menu_listener );
      opts.add( NormFileName);
      if( fvc != null)
      {
         opts.addSeparator( );
         opts.add(  SaveImageActionListener.getActiveMenuItem( "Save Image" , displPanel ) );
         opts.add(PrintComponentActionListener.getActiveMenuItem( "Print Image" , displPanel ) );
      }
     
      if( AddGraph == null)
      {
         AddGraph = new JMenu("Add Graph");
         JMenuItem Load = new JMenuItem("New Graph");
         MenuListener mmm=  new MenuListener( this ,D_Q);
         mmm.setJMenu(  AddGraph);
         Load.addActionListener( mmm );
         AddGraph.add( Load );
        
      }
      
      menBar.add( AddGraph );
     
     // JMenuItem Clear = new JMenuItem("Clear Compare Data");
     // Clear.addActionListener(  new MenuListener( this ,D_Q) );
    //  opts.add( Clear );

     
      
      FxnCtrls = new JMenu("Controls");
      menBar.add( FxnCtrls);
      
      JMenu Help = new JMenu("Help");
      menBar.add( Help);
      JMenuItem help = new JMenuItem("about");
      Help.add( help);
      help.addActionListener( new ShowHelpActionListener( 
            FileIO.CreateExecFileName(
                  System.getProperty( "Help_Directory" ), "IsawEVDQViewers.html",false )) );
      
      
      if( fvc != null)
      {
         ViewMenuItem[] Mens = fvc.getMenuItems( );
         FxnCtrls.add( Mens[0].getItem( ) );
         
      }else
         EventTools.ShowEventsApp.Command.Util.sendInfo( 
               "Function Controls not valid. Close and restart after data is available"  );
      
      display_frame.setJMenuBar( menBar );
      display_frame.setBounds( location.x,
                               location.y,
                               (int)(size.getWidth()),
                               (int)(size.getHeight()) );
  
      WindowShower.show( display_frame );
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

   /**
    * Adds a Graph to the function view
    * 
    * @param fileName  FileName with the data to add
    * @param ScaleFactor  Factor to multiply the data by
    * @param GraphName    The name of this graph for later reference.
    */
   public void setOtherGraph( String fileName, float ScaleFactor, String GraphName )
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
      Data D1 =D.multiply( ScaleFactor , 0 );
      
      CompareGraph =  new DataArray1D(D1.getX_values(),D1.getY_values(),
                           D.getErrors(),GraphName,true,false);
       AddGraph();
   }

   
   private void AddGraph( )
   {
      boolean redraw = false;
      if( fvc != null && CompareGraph != null)
      {
        
         Vector V = getGraphData();
         Vector Vs = getGraphSettings();
         
         if( CompareGraph != null)
            V.add(  CompareGraph );
         VirtualArrayList1D array =new VirtualArrayList1D( V);
         array.setPointedAtGraph( -1 );
         fvc.dataChanged( array);
         setGraphSettings( Vs);
         redraw = true;
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
      //((LabelCombobox)(controlList[FunctionControls.VC_SHIFT])).setSelectedIndex( 0 );
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
            System.setProperty( "ShowWCToolTip" , prop_str );

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
            V.add( new DataArray1D( CompareGraph.getXArray(), 
                                    CompareGraph.getYArray(),
                                    CompareGraph.getErrorArray(), 
                                    CompareGraph.getTitle(), true, false));
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
   protected static Vector  ConCat( Boolean TF, String D_Q)
   {
      Vector V = new Vector(2);
      V.add( TF);
      V.add( D_Q);
      return V;
   }
   
   // In order
   private Vector  getGraphData()
   {
      if( fvc != null )
      {
         IVirtualArrayList1D data = fvc.getArray();
         Vector V = new Vector();
         for( int i = 0 ; i < data.getNumGraphs( ) ; i++ )
         {
            DataArray1D data1 = new DataArray1D( data.getXValues( i ) , data
                  .getYValues( i ) );
            data1.setTitle( data.getGraphTitle( i ) );
            V.add( data1 );
         }
         return V;
      }
      return null;
         
   }
   private void setGraphData( Vector V)
   {
      if( fvc != null)
        {
         VirtualArrayList1D array = new VirtualArrayList1D( V);
         array.setPointedAtGraph( -1 );
         fvc.dataChanged( array);
        }
   }
   
   private Vector getGraphSettings()
   {
      if( fvc == null || fvc.getArray( )== null ||fvc.getArray().getNumGraphs( ) < 1)
         return null;
      Vector Res = new Vector();
      ObjectState oState = fvc.getObjectState( false );
      int NumGraphs = fvc.getArray().getNumGraphs( );
      for( int i=0; i<NumGraphs; i++ )
      {
        Res.add( (ObjectState)oState.get(FunctionViewComponent.GRAPHJPANEL+"."+
                                  GraphJPanel.GRAPH_DATA+(i+1) ));
      }
      
      return Res;
         
   }
   
   private void setGraphSettings( Vector settings)
   {
      if( settings == null || settings.size() < 1 || fvc == null)
         return;

      ObjectState oState = fvc.getObjectState( false );
      for( int i=0; i < settings.size( ); i++)
      {
         if( !oState.insert(FunctionViewComponent.GRAPHJPANEL+"."+
                GraphJPanel.GRAPH_DATA+(i+1)  , settings.elementAt( i ) ));
         oState.reset(FunctionViewComponent.GRAPHJPANEL+"."+
                GraphJPanel.GRAPH_DATA+(i+1)  , settings.elementAt( i ) );
      }
      
      fvc.setObjectState(  oState );
         
   }
   
   private  void showNewInfo()
   {

      if( display_frame != null )
      {
         display_frame.invalidate();
         display_frame.validate();
         fvc.paintComponents();
         display_frame.repaint();
         fvc.paintComponents();
      }
   }
   public void RemoveGraph( String MenuName)
   {
      if( AddGraph == null || MenuName == null ||fvc == null)
          return;
      int k = -1;
      for( int i= AddGraph.getMenuComponentCount( )-1; i>=1; i--)
      {
         if(AddGraph.getMenuComponent( i ) instanceof JMenuItem)
            if( MenuName.equals( ((JMenuItem)AddGraph.getMenuComponent(i)).getText( )))
               AddGraph.remove( i );
         
      }
      
      Vector V = getGraphData();
      Vector Vs = getGraphSettings();
      IVirtualArrayList1D array = fvc.getArray();
      for( int i= array.getNumGraphs( )-1; i>=0; i--)
         if( MenuName.equals( array.getGraphTitle( i )))
         {
            V.remove( i );
            Vs.remove( i );
         }
      fvc.dataChanged( new VirtualArrayList1D( V));
      setGraphSettings( Vs);
      showNewInfo();
      
   }
}


class MenuListener implements ActionListener, IhasWindowClosed
{
   GraphViewHandler gv;
   JTextField textField;
   JTextField nameField;
   String D_Q;
   String fileName ;
   FileChooserPanel but;
   FinishJFrame jf;
   String NameGraph;
   JMenu  AddGraphMen;
   private static String OPT_MESSAGE =
       "Normalize normalizes with the incident spectrum and Protons on Target,"+
       " if present, on the \n"+
       "Load form in IsawEV.\n\n "+
       "If loading from live data, the System property \"ScaleWith\"(case sensitive)"+
       "must be\n"+
       "set to \"Protons on target\"(case sensitive) to scale with the current "+
       "Total Protons on Target\n or NumEvents, or Max_xxxx where xxxx can be"+
       " any float or integer number";
   
   public MenuListener( GraphViewHandler gv, String D_Q)
   {
      this.gv = gv;
      fileName = null;
      textField = null;
      this.D_Q = D_Q;
      but = null;
      NameGraph = null;
   }
   
   public void setJMenu( JMenu AddGraphMen)
   {
      this.AddGraphMen = AddGraphMen;
   }
   
   private Dimension getFrameSize( int ncharsWidth, int ncharsHeight, 
         Component component)
   {
      FontMetrics Fmt = component.getFontMetrics( component.getFont( ) );
      Dimension Res = new Dimension();
      Res.width = ncharsWidth*(Fmt.getMaxAdvance( )+3);
      Res.height = ncharsHeight*(Fmt.getMaxAscent( )+ Fmt.getMaxDescent( )+3);
      return Res;
   }
   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   @Override
   public void actionPerformed( ActionEvent evt )
   {
    if( D_Q .equals( "NEW_GRAPH" ))//erase Graph
    {
       if( JOptionPane.showConfirmDialog( null ,
               "Really delete "+evt.getActionCommand() ) == JOptionPane.YES_OPTION)
           gv.RemoveGraph( evt.getActionCommand( ));
       return;
    }
    
    if( evt.getActionCommand().equals( "Normalize"))
       gv.messageCenter.send(  new Message( Commands.NORMALIZE_QD_GRAPHS,
                  GraphViewHandler.ConCat(((JCheckBoxMenuItem)(evt.getSource())).isSelected(),
                               D_Q),true,true) );
    
    else if( evt.getActionCommand().equals( "New Graph"))
    {
       NameGraph = null;
       jf = new FinishJFrame("Enter FileName");
       jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE  );
       jf.getContentPane().setLayout(  new GridLayout(4,1) );
       jf.addWindowListener( new IndirectWindowCloseListener(this,"but") );
       but = new FileChooserPanel(
               FileChooserPanel.LOAD_FILE,"File Name");
       jf.getContentPane().add(  but );
       
       JPanel pan = new JPanel();
       pan.setLayout( new GridLayout(1,2));
       pan.add(  new JLabel("Scale Factor") );
       textField = new JTextField( 10);
       pan.add( textField);
       jf.getContentPane().add( pan);
       
       pan = new JPanel( new GridLayout(1,2));
       pan.add(  new JLabel("Graph Name") );
       nameField = new JTextField(12);
       pan.add(nameField );
       jf.getContentPane().add( pan);
       
       JButton OK = new JButton("OK");
       JPanel panOK = new JPanel();
       BoxLayout bl = new BoxLayout( panOK, BoxLayout.X_AXIS);
       panOK.setLayout( bl );
       panOK.add( Box.createHorizontalGlue( ) );
       panOK.add(OK);
       panOK.add( Box.createHorizontalGlue( ) );
       
       jf.getContentPane().add( panOK );
       OK.addActionListener( this);
       
       Dimension size= getFrameSize( 20,4, jf.getContentPane( ));
       size.height +=30;//Top and bottom borders
       jf.setSize( size);
       jf.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
       WindowShower.show( jf );
    }
    else if( evt.getActionCommand().equals( "FileName"))
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
    }
    else if( evt.getActionCommand().equals( "OK"))
    {
       if( but == null)
          return;
       
       fileName = but.getTextField( ).getText( );
       String GraphName = nameField.getText( );
       if( GraphName == null || GraphName.length() <1)
          GraphName = fileName;
       
       if( fileName != null)
          fileName = fileName.trim( );
       
       if(fileName == null ||  fileName.length() < 1)
       {
          jf.dispose( );
          return;
       }
          
       float scale_factor =1;
       try
       {
          scale_factor = Float.parseFloat( textField.getText().trim());
       }catch( Exception s)
       {
          scale_factor =1;
       }
       
       gv.setOtherGraph( fileName, scale_factor, GraphName);
       if( AddGraphMen != null)
       {
          JMenuItem G = new JMenuItem( GraphName);
          AddGraphMen.add( G );
          
          G.addActionListener(new MenuListener( gv, "NEW_GRAPH") );
       }
       jf.dispose( );
    }
    else if( evt.getActionCommand().equals( "Clear Compare Data" ))
    {
       gv.setOtherGraph(  null , -1,"");
    }
    else if( evt.getActionCommand().equals( "Help" ))
    {
       JOptionPane.showMessageDialog( null , new JTextArea( OPT_MESSAGE) );
    }
      
   }

   @Override
   public void WindowClose(String ID)
   {
      but = null;
   }

}
