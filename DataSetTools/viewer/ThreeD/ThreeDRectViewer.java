/*
 * File:  ThreeDRectViewer.java
 *
 * Copyright (C) 2011, Ruth Mikkelson, Dennis Mikkelson
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 */

package DataSetTools.viewer.ThreeD;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Position3D;
import gov.anl.ipns.MathTools.Geometry.Tran3D;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.Operator.IOperator;
import gov.anl.ipns.Operator.Threads.ParallelExecutor;
import gov.anl.ipns.Util.Messaging.IObserver;
import gov.anl.ipns.ViewTools.Components.IPreserveState;
import gov.anl.ipns.ViewTools.Components.ObjectState;
import gov.anl.ipns.ViewTools.Components.ViewControls.FrameController;
import gov.anl.ipns.ViewTools.Components.ViewControls.XScaleController;
import gov.anl.ipns.ViewTools.Panels.Image.ImageJPanel;
import gov.anl.ipns.ViewTools.Panels.Image.IndexColorMaker;
import gov.anl.ipns.ViewTools.Panels.ThreeD.AltAzController;
import gov.anl.ipns.ViewTools.Panels.ThreeD.IThreeD_Object;
import gov.anl.ipns.ViewTools.Panels.ThreeD.ImageRectangle;
import gov.anl.ipns.ViewTools.Panels.ThreeD.Polyline;
import gov.anl.ipns.ViewTools.Panels.ThreeD.Polymarker;
import gov.anl.ipns.ViewTools.Panels.ThreeD.ThreeD_JPanel;
import gov.anl.ipns.ViewTools.UI.ColorScaleImage;
import gov.anl.ipns.ViewTools.UI.ColorScaleMenu;
import gov.anl.ipns.ViewTools.UI.FontUtil;
import gov.anl.ipns.ViewTools.UI.SplitPaneWithState;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.IndexColorModel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import DataSetTools.components.ui.DataSetXConversionsTable;
import DataSetTools.dataset.AttrUtil;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.Grid_util;
import DataSetTools.dataset.IDataGrid;
import DataSetTools.dataset.IPixelInfo;
import DataSetTools.dataset.OperationLog;
import DataSetTools.dataset.PixelInfoList;
import DataSetTools.dataset.RowColGrid;
import DataSetTools.dataset.UniformGrid;
import DataSetTools.dataset.UniformXScale;
import DataSetTools.dataset.XScale;
import DataSetTools.instruments.SampleOrientation;
import DataSetTools.math.tof_calc;
import DataSetTools.operator.DataSet.DataSetOperator;
import DataSetTools.operator.DataSet.Attribute.GetPixelInfo_op;
import DataSetTools.viewer.DataSetViewer;
import DataSetTools.viewer.IViewManager;
import DataSetTools.viewer.ViewManager;
import DataSetTools.viewer.ViewerState;
import DataSetTools.viewer.util.Rebinner;

/**
 * The basic original 3D Viewer retooled to work only with rectangular detector banks.
 * 
 * @author ruth
 * 
 * TODO: The counts in the Conversion table are now incorrect for the "rebinned" on
 * the fly data set. They give the counts for the bin in the original bins.
 *
 */
public class ThreeDRectViewer extends DataSetViewer implements Serializable,
      IPreserveState
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   IDataGrid[] Grids = null;
   ImageRectangle[]  Detector;
   private float   last_pointed_at_index = -1;
   private boolean redraw_cursor       = true;
   private boolean notify_ds_observers = false;//true if comes from internal ds

   private ThreeD_JPanel            threeD_panel      = null;
 
   private ImageJPanel              color_scale_image = null;
 //------------Controls ----------------------------
   private XScaleController          x_scale_ui        = null;
   private JSlider                  log_scale_slider  = new JSlider();
   private AltAzController          view_control      = null;
   private FrameController           frame_control     = null;
   private DataSetXConversionsTable conv_table        = null;
   
   //  ------GUI elements -------------------
   private Box                      control_panel     = null; 
   private SplitPaneWithState       split_pane        = null;
   private String                   title             ="";
   //--------------- Color stuff-----------
   private final int LOG_TABLE_SIZE      = 60000;
   private final int NUM_POSITIVE_COLORS = 127;
   private final int ZERO_COLOR_INDEX    = NUM_POSITIVE_COLORS;//+1 really
   private int MarkerColorIndex  =-1;
   java.awt.image.IndexColorModel   colorModel = null;
   java.awt.image.IndexColorModel   colorModelwTransp = null;
   private float                    log_scale[]       = null; 
   float MaxAbs                                       =0;
   float last_pointed_at_x;
   XScale xscl0;
   private Rebinner                 rebinner;
   private int[][][]                DRC2Index;
   private  Hashtable<Integer,Integer> DetID2Index;

   private final Integer  AXES            = 200000000;
   private final Integer  BEAM            = 300000000;
   private final Integer  INSTRUMENT      = 400000000;
   
   // ----- ObjectState keys -------------------------
   public static String    VIEWER_STATE_OS="VIEWER_STATE_OS";
   public static String    FRAME_CONTROL_OS="FRAME_CONTROL_OS";
   public static String    VIEW_CONTROL_MINDIS_OS="VIEW_CONTROL_MINDIS_OS";//Array of mindist,maxdist, dist
   public static String    VIEW_CONTROL_MAXDIS_OS="VIEW_CONTROL_MAXDIS_OS";
   public static String    VIEW_CONTROL_DIS_OS="VIEW_CONTROL_DIS_OS";
   public static String    XSCALE_OS ="XSCALE_OS";
   public static String   LASTPOINTEDAT_OS="LASTPOINEDAT_OS";
   public static String   LASTPOINTEDATDDAT_OS="LASTPOINEDATDAT_OS";
   public static String    MAXABS_OS ="MAXABS_OS";
   public static String   AZIMUTH_OS="AZIMUTH_OS";
   public static String   ALT_OS="ALT_OS";
   public static String   THREED__PANEL_OS ="THREED__PANEL_OS";
   
   
   float MinDis,MaxDis;
   boolean ShowLattice = false;
   Point   pixel_point =null;
   
   public ThreeDRectViewer(DataSet dataSet)
   {
      this( dataSet , null );
   }

//TODO
//setDataSet needs to redo some of this stuff so separate out as an init

   public ThreeDRectViewer(DataSet dataSet, ViewerState state)
   {
      super( dataSet , state );
     
      boolean MakeshowButton = dataSet.getAttribute(  Attribute.ORIENT_MATRIX ) != null;
           
      DataSetOperator op = dataSet.getOperator( "Pixel Info" );
      if( op == null)
         System.out.println("No Pixel Info Operator");
      else
         ((GetPixelInfo_op)op).ShowDetector = true;

      int[] grids = Grid_util.getAreaGridIDs( dataSet );
      Grids= new IDataGrid[grids.length];
      for( int k=0; k < grids.length ; k++)
         Grids[k]= getUGrid( Grid_util.getAreaGrid( getDataSet(), grids[k] ));
      
      rebinner = new Rebinner( getDataSet() );
      SetUpDRC2Index();
      xscl0  = rebinner.getXScale( );//getDataSet().getData_entry(1).getX_scale();
      XScale x_scale1 = xscl0;
    
      UniformXScale x_scale  = getDataSet().getXRange();;
      
      //-------------------------XScale Chooser ----------------------------
      String label = getDataSet().getX_units();
      float x_min  = x_scale.getStart_x();
      float x_max  = x_scale.getEnd_x();
                                    // set n_steps to 0 to default to NO REBINNING
      x_scale_ui = new XScaleController( "X Scale", label, x_min, x_max, 0 );

      x_scale_ui.addActionListener( new XScaleListener() );
      XScale xscl = (XScale)x_scale_ui.getControlValue( );
      if( xscl == null)
         xscl= xscl0;
      
      //-------------Find MaxAbs ---------------------------
      MaxAbs =0;
    
      this.setMaxAbs( );
    
      colorModel = IndexColorMaker.getDualColorModel( 
            getState().get_String( ViewerState.COLOR_SCALE ),
            NUM_POSITIVE_COLORS );
      colorModelwTransp = BuildAlphaColModel( colorModel);
      
      //------------------threeD_panel--------------------------------
      if ( threeD_panel != null )          // get rid of old components first 
      {
        threeD_panel.removeAll();
        split_pane.removeAll();
        control_panel.removeAll();
        removeAll();
      }

      threeD_panel = new ThreeD_JPanel();
      threeD_panel.setBackground( new Color( 90, 90, 90 ) );
     
      ///------------------
      //setting coord info source to null because it doesn't really apply to 
      // a 3d environment
      threeD_panel.setCoordInfoSource(null);
      //-------------------- Log color scale slider -----------------------
      log_scale_slider.setPreferredSize( new Dimension(120,50) );
      log_scale_slider.setValue( getState().get_int( ViewerState.BRIGHTNESS) );
      log_scale_slider.setMajorTickSpacing(20);
      log_scale_slider.setMinorTickSpacing(5);
      log_scale_slider.setPaintTicks(true);

      TitledBorder border = new TitledBorder(
                                 LineBorder.createBlackLineBorder(),"Brightness");
      border.setTitleFont( FontUtil.BORDER_FONT );
      log_scale_slider.setBorder( border );
      log_scale_slider.addChangeListener( new LogScaleEventHandler() );
      if( log_scale== null)
         setLogScale( getState().get_int( ViewerState.BRIGHTNESS) );
      
      Detector = new ImageRectangle[ Grids.length];
     
      MinDis = Float.MAX_VALUE;
      MaxDis = Float.MIN_VALUE;
      last_pointed_at_x = x_scale.getStart_x();
      float[] yvalsA = rebinner.getY_valuesAtX(x_scale.getStart_x() );
      for( int i=0; i< Grids.length; i++)
      {
         UniformGrid grid = (UniformGrid)Grids[i] ;
         int[][] ColorIndicies = getColorIndices( grid, x_scale1, x_scale.getStart_x(), yvalsA);
         Detector[i] = new ImageRectangle( grid.position( ), grid.x_vec( ), grid.y_vec( ),
                            grid.width( ), grid.height(), ColorIndicies, colorModelwTransp,
                           threeD_panel);
         float D = grid.position( ).length( );
         if( D< MinDis)
            MinDis = D;
         if( D > MaxDis)
            MaxDis = D;
      }
      
      threeD_panel.setObjects( "Detectors" , Detector );
      draw_axes( MaxDis/5 );
      draw_beam( MaxDis );
      draw_instrument( MaxDis );

      //-------------------- color_scale------------------------
      color_scale_image = new ColorScaleImage();
      color_scale_image.setNamedColorModel( 
                  getState().get_String( ViewerState.COLOR_SCALE), true, true );

      //---------------------- AltAzController ------------------

      view_control = new AltAzController();
      view_control.addActionListener( new AltAzControlListener() );
      view_control.addControlledPanel( threeD_panel );
   
      initViewControl( MinDis, MaxDis);
      
      //------------------ Frame Controller --------------------
      frame_control = new FrameController();
      frame_control.setBorderTitle( getDataSet().getX_label() );
      frame_control.setTextLabel( getDataSet().getX_units() );
      frame_control.setFrame_values(xscl.getXs());
      frame_control.setFrameNumber(0);
      frame_control.addActionListener( new FrameControlListener() );
   
      //---------------- DS Conversions ------------------------
      conv_table = new DataSetXConversionsTable( getDataSet() );
      JPanel conv_panel = new JPanel();
      conv_panel.setLayout( new GridLayout(1,1) );
      conv_panel.add( conv_table.getTable() );
      border = new TitledBorder( LineBorder.createBlackLineBorder(), "Pixel Data");
      border.setTitleFont( FontUtil.BORDER_FONT );
      conv_panel.setBorder( border );
      
      //--------------- Make GUI ----------------------------
     
       MakeGUI( MakeshowButton );
       
       redraw( NEW_DATA_SET );
      
       threeD_panel.addMouseMotionListener( new ViewMouseMotionAdapter() );
       threeD_panel.addMouseListener( new ViewMouseAdapter() );
      
       OptionMenuHandler option_menu_handler = new OptionMenuHandler();
       JMenu option_menu = menu_bar.getMenu( OPTION_MENU_ID );
                                                         // color options
       JMenu color_menu = new ColorScaleMenu( option_menu_handler );
       option_menu.add( color_menu );
   }

   private UniformGrid getUGrid( IDataGrid grid)
   {  
      if( grid == null)
         return null;
      UniformGrid grid1 = null;
      if( grid instanceof RowColGrid)
         grid1 = new UniformGrid( grid.ID( ), grid.units( ),grid.position( ),
                   grid.x_vec( ), grid.y_vec( ), grid.width(),
                   grid.height( ), grid.depth( ),grid.num_rows( ), grid.num_cols( ));
               //RowColGrid.getUniformDataGrid( (RowColGrid)grid , .001f );
      else if (grid instanceof UniformGrid)
         grid1=(UniformGrid)grid;
      if( grid1 != null)
         grid1.setData_entries( getDataSet() );
      return grid1;
   }

   @Override
   public void redraw(String reason)
   {
      if ( !validDataSet() )
         return;

       if ( reason.equals(IObserver.SELECTION_CHANGED))  // no selection display yet
         return;

       else if ( reason.equals(IObserver.POINTED_AT_CHANGED) )
       { 
          if ( threeD_panel.isDoingBox() )           // don't interrupt the zoom in 
            return;                                  // process 
         
          DataSet ds = getDataSet();
         
          int index = ds.getPointedAtIndex();

          if ( !Float.isNaN( ds.getPointedAtX() )   &&
               ds.getPointedAtX() != last_pointed_at_x )
          {
            last_pointed_at_x = ds.getPointedAtX();
            
            int frameNum = frame_control.getFrameNumber( );
            frame_control.setControlValue( last_pointed_at_x, false );
            if( frameNum != frame_control.getFrameNumber( ))
               setNewData( last_pointed_at_x);
            
           // if( ds.getPointedAtIndex() == last_pointed_at_index)// ?? behaviour
           //   redraw_cursor = false;
          }
          
          if (index != DataSet.INVALID_INDEX && ! Float.isNaN( last_pointed_at_x ))
          {
             
            float y_val = rebinner.getY_valueAtX( index, last_pointed_at_x );
            conv_table.showConversions( last_pointed_at_x, y_val, index );
          }
          
          Vector3D detector_location = group_location( ds.getPointedAtIndex() );
         
          if ( detector_location != null && redraw_cursor )
          {
            pixel_point = threeD_panel.project( detector_location );
         
            if(!notify_ds_observers)
            	threeD_panel.set_crosshair( pixel_point );           
          }else                                               // ###### is this right?
        	
          
          last_pointed_at_index = ds.getPointedAtIndex();
          redraw_cursor = true;          //not used
          notify_ds_observers = false;    //not used fixed ViewControl
          
          threeD_panel.repaint();
       }
   }


   public javax.swing.JComponent getDisplayComponent()
   {
      return threeD_panel;
   }

 
   public ObjectState getObjectState(boolean isDefault)
   {
      ObjectState state = new ObjectState();
     if( !isDefault)
     {
        state.insert( VIEWER_STATE_OS , getState() );
        state.insert(FRAME_CONTROL_OS,frame_control.getObjectState( isDefault));
       
        state.insert(XSCALE_OS,x_scale_ui.getObjectState(isDefault));
        state.insert(VIEW_CONTROL_MINDIS_OS,.2f*MinDis);
        state.insert( VIEW_CONTROL_MAXDIS_OS, 4f*MaxDis );
        state.insert(VIEW_CONTROL_DIS_OS, view_control.getDistance( ));
        state.insert( LASTPOINTEDAT_OS , last_pointed_at_x );
        state.insert( ThreeDRectViewer.LASTPOINTEDATDDAT_OS , last_pointed_at_index);
        state.insert(  MAXABS_OS,MaxAbs );
        state.insert( ALT_OS , getState().get_float( ViewerState.V_ALTITUDE )  );
        state.insert( AZIMUTH_OS ,getState().get_float( ViewerState.V_AZIMUTH )  );
        
     }else
     {
        state.insert( VIEWER_STATE_OS , new ViewerState() );
        state.insert(FRAME_CONTROL_OS,frame_control.getObjectState( false));
       
        state.insert(XSCALE_OS,x_scale_ui.getObjectState(false));
        state.insert(VIEW_CONTROL_MINDIS_OS,MinDis);
        state.insert( VIEW_CONTROL_MAXDIS_OS,MaxDis );
        state.insert(VIEW_CONTROL_DIS_OS, view_control.getDistance( ));
        state.insert( LASTPOINTEDAT_OS , -1f);
        state.insert( LASTPOINTEDATDDAT_OS , -1);
        state.insert(  MAXABS_OS,MaxAbs);
        state.insert( ALT_OS ,45f  );
        state.insert( AZIMUTH_OS ,45f  );
     }
      state.insert( THREED__PANEL_OS ,  threeD_panel.getObjectState( isDefault ));
      return state;
   }
   
   public void setObjectState(ObjectState new_state)
   {
      super.setObjectState( new_state );
      if( new_state == null)
         return;
     ViewerState vs =(ViewerState) new_state.get( VIEWER_STATE_OS );
     if( vs != null)
     {
        ViewerState state = getState();
        String color = vs.get_String( ViewerState.COLOR_SCALE );
        state.set_String( ViewerState.COLOR_SCALE , color );
        colorModel = IndexColorMaker.getDualColorModel( 
              getState().get_String( ViewerState.COLOR_SCALE ),
              NUM_POSITIVE_COLORS );
        colorModelwTransp = BuildAlphaColModel( colorModel);
        color_scale_image.setNamedColorModel( ViewerState.COLOR_SCALE, true, true );
        state.set_int( ViewerState.BRIGHTNESS ,vs.get_int( ViewerState.BRIGHTNESS ) );
        log_scale_slider.setValue( getState().get_int( ViewerState.BRIGHTNESS) );
        if( log_scale== null)
           setLogScale( getState().get_int( ViewerState.BRIGHTNESS) );
       state.set_float( ViewerState.V_AZIMUTH,  ((Float)new_state.get(AZIMUTH_OS)).floatValue() );
       state.set_float( ViewerState.V_ALTITUDE,  ((Float)new_state.get(ALT_OS)).floatValue());
       state.set_float( ViewerState.V_DISTANCE,  vs.get_float( ViewerState.V_DISTANCE ) );
     }
     
     frame_control.setObjectState( (ObjectState) new_state.get( FRAME_CONTROL_OS ) );
     XScale xscl = (XScale)x_scale_ui.getControlValue( );
     
     x_scale_ui.setObjectState((ObjectState)new_state.get(XSCALE_OS));
     float MinDis =((Float) new_state.get( VIEW_CONTROL_MINDIS_OS)).floatValue( );
     float MaxDis =((Float) new_state.get( VIEW_CONTROL_MAXDIS_OS)).floatValue( );
     float Dis =((Float) new_state.get(VIEW_CONTROL_DIS_OS)).floatValue( );
     
     initViewControl( MinDis,MaxDis);
     view_control.setDistance(  Dis );
     last_pointed_at_x= ((Float)new_state.get( LASTPOINTEDAT_OS )).floatValue( );
     last_pointed_at_index = ((Number) new_state.get( LASTPOINTEDATDDAT_OS )).intValue( );
     MaxAbs =((Float)new_state.get( MAXABS_OS )).floatValue( );

     XScale xscl1 = (XScale)x_scale_ui.getControlValue( );
     if( xscl == null && xscl1 == null)
     {
        
     }
     else if( xscl==null || xscl1 == null || 
              xscl1.getStart_x( ) != xscl.getStart_x() || 
              xscl1.getEnd_x( )   != xscl.getEnd_x()   ||
              xscl1.getNum_x( )   != xscl.getNum_x())
     {
       MaxAbs=0;
       setNewXScale((XScale)x_scale_ui.getControlValue());
     }
     int value = log_scale_slider.getValue();
     setLogScale( value );
     
     setColorModel();
     threeD_panel.setObjectState( (ObjectState)new_state.get( THREED__PANEL_OS ) );
     redraw(IObserver.POINTED_AT_CHANGED);
   }
   
   
   /**
    * @param Args
    */
   public static void main(String[] Args)
   {
      Object[] args = new Object[2];
      args[0] ="C:/ISAW/SampleRuns/SNS/TOPAZ/TOPAZ_3007_histo.nxs";
     // args[1] = false;
      args[1] = 6;
    // args[2] = "none"; 
    //  args[3] ="TOF_NSCD"; 
    // args[4] =false; 
      
      Object Res = null;
      try
      {
        Res = Command.ScriptUtil.ExecuteCommand( "OneDS" , args);
        // Res = Command.ScriptUtil.ExecuteCommand( "LoadMergeNXS" , args);
      }catch( Exception s)
      {
         s.printStackTrace( );
         System.exit(0);
      }
      
     ViewManager vm = (new ViewManager( (DataSet)Res, IViewManager.THREE_D_RECT));
   }
   

   private void initViewControl( float MinDist, float MaxDist)
   {
      float altitude = getState().get_float( ViewerState.V_ALTITUDE );
      float azimuth  = getState().get_float( ViewerState.V_AZIMUTH );
      float distance = getState().get_float( ViewerState.V_DISTANCE );
      distance = 3*MaxDist;
      view_control.setViewAngle( 45 );
      view_control.setAltitudeAngle( altitude );
      view_control.setAzimuthAngle( azimuth );
      view_control.setDistanceRange( .5f*MinDist , 5*MaxDist );//If change rule change in getObjectState
      view_control.setDistance( distance );
      view_control.apply( true );
   }

  
  private void MakeGUI( boolean MakeshowButton )
  {
     setLayout( new GridLayout(1,1));
    
     control_panel = new Box( BoxLayout.Y_AXIS );
     control_panel.add( x_scale_ui);
     control_panel.add(color_scale_image );
     control_panel.add( log_scale_slider );
     control_panel.add( view_control);
     control_panel.add( frame_control);
     
     //--------------------- Show Lattice Button ------------------
     if( MakeshowButton)
     {
        JButton button = new JButton("Show Lattice Marks");
        button.addActionListener(  new ActionListener() 
        {
           public void actionPerformed( ActionEvent evt)
           {
              JButton B = (JButton)evt.getSource( );
              if( B.getText( ) =="Show Lattice Marks")
              {
                 ShowLattice = true;
                 B.setText("Hide Lattice Marks");
              }else
              {
                 ShowLattice = false;
                 B.setText("Show Lattice Marks");
              }
              setColorModel();
           }
        }
        
        );
      
        control_panel.add( button);
        
     }
     control_panel.add( conv_table.getTable());
     control_panel.add(  Box.createVerticalGlue( ) );
     
    JPanel graph_container = new JPanel() ;
   
    graph_container.setLayout( new GridLayout(1,1) );
    JPanel Graph1 = new JPanel();
   
     Graph1.setLayout(  new GridLayout(1,1) );
  
     OperationLog op_log = getDataSet().getOp_log();
     if ( op_log.numEntries() <= 0 )
       title = "Graph Display Area";
     else
       title = op_log.getEntryAt( op_log.numEntries() - 1 );

     TitledBorder border = new TitledBorder( LineBorder.createBlackLineBorder(), title );
     border.setTitleFont( FontUtil.BORDER_FONT );
     graph_container.setBorder( border );
     threeD_panel.setAlignmentX( 0 );
     threeD_panel.setAlignmentY( 0 );
     
     Graph1.add( threeD_panel );
     graph_container.add( Graph1 );
     JPanel controlContainer = new JPanel();
     controlContainer.setLayout(  new GridLayout(1,1) );
     controlContainer.add( control_panel);
     split_pane = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
                                          graph_container,
                                          controlContainer,
                                          0.7f );
     add(  split_pane);
   // add( control_panel);
  }

  
  /* ------------------------------ draw_axes ----------------------------- */

  private void draw_axes( float length  )
  {
    IThreeD_Object objects[] = new IThreeD_Object[ 4 ];
    Vector3D points[] = new Vector3D[2];

    points[0] = new Vector3D( 0, 0, 0 );                    // y_axis
    points[1] = new Vector3D( 0, length, 0 );
    objects[0] = new Polyline( points, Color.green );
                                                            // z_axis
    points[1] = new Vector3D( 0, 0, length );
    objects[1] = new Polyline( points, Color.blue );

    points[1] = new Vector3D( length, 0, 0 );               // +x-axis
    objects[2] = new Polyline( points, Color.red );

    points[1] = new Vector3D( -length/3, 0, 0 );            // -x-axis
    objects[3] = new Polyline( points, Color.red );

    threeD_panel.setObjects( AXES, objects );
  }


  /* --------------------------- draw_beam --------------------------- */

  private void draw_beam( float radius  )
  {
    int N_STEPS = 50;
    IThreeD_Object objects[] = new IThreeD_Object[ N_STEPS ];
    Vector3D points[] = new Vector3D[1];

    points[0] = new Vector3D( 0, 0, 0 );                    // sample
    Polymarker sample = new Polymarker( points, Color.red );
    sample.setType( Polymarker.STAR );
    sample.setSize( 10 );
    objects[0] = sample; 
    
    for ( int i = 1; i < N_STEPS; i++ )                  // beam is segmented
    {                                                    // for depth sorting
      points = new Vector3D[2];
      points[0] = new Vector3D( -(i-1)*radius/N_STEPS, 0, 0 );// beam is in 
      points[1] = new Vector3D( -i*radius/N_STEPS, 0, 0 );    // -x-axis dir
      objects[i] = new Polyline( points, Color.red );
    }

    threeD_panel.setObjects( BEAM, objects );
  }


  /* --------------------------- draw_instrument --------------------------- */

  private void draw_instrument( float radius  )
  {
    final int N_PIECES = 180;
    IThreeD_Object objects[] = new IThreeD_Object[ N_PIECES ];
    Vector3D points[];
                                                            // draw circle for
                                                            // instrument 
    float delta_angle = 2 * 3.14159265f / N_PIECES;
    float angle = 0;
    float x,
          y;
    for ( int i = 0; i < N_PIECES; i++ )                    // circle is segmented
    {                                                       // for depth sorting
      points = new Vector3D[2];
      x = radius * (float)Math.cos( angle );
      y = radius * (float)Math.sin( angle );
      points[0] = new Vector3D( x, y, 0 );  

      angle += delta_angle;
      x = radius * (float)Math.cos( angle );
      y = radius * (float)Math.sin( angle );
      points[1] = new Vector3D( x, y, 0 );  
      objects[i] = new Polyline( points, Color.black );
    }

    threeD_panel.setObjects( INSTRUMENT, objects );
  }


  /* ---------------------------- group_location --------------------------- */

  private Vector3D group_location( int index )
  {
    DataSet ds = getDataSet();
    int     n_data = ds.getNum_entries();

    if ( index < 0 || index >= n_data )
      return null;

    Data d = ds.getData_entry(index);
   
    Position3D position= (Position3D)d.getAttributeValue( Attribute.DETECTOR_POS);
   
    if ( position == null )
      return null;

    float coords[] = position.getCartesianCoords();
    for ( int i = 0; i < 3; i++ )
      if ( Float.isNaN( coords[i] ) )
      {
       // group_NaN_count++;
        return null;
      }

    Vector3D pt_3D = new Vector3D( coords[0], coords[1], coords[2] );

    return pt_3D;
  }

 
//New colormodel where 0 represents completely transparent. The
  // other indecies map( with offset 1) to the other color model
  // The top one will be a marker blue color
  private IndexColorModel BuildAlphaColModel(IndexColorModel colorModel)
  {
     if( colorModel == null )
        if( this.colorModel != null )
           return this.colorModel;
        else
           colorModel = IndexColorMaker.getColorModel(
                          IndexColorMaker.HEATED_OBJECT_SCALE_2 , 200 );
     
     int size = colorModel.getMapSize( );
     
     byte[] red = new byte[size+1];
     byte[] green = new byte[size+1];
     byte[] blue = new byte[size+1];
     byte[] alpha = new byte[size+1];
     
     Arrays.fill( alpha , (byte)255 );
     red[0]= green[0]= blue[0]=alpha[0]= 0;
     byte[] buff = new byte[size];
     
     colorModel.getReds( buff );
     System.arraycopy( buff , 0, red , 1 , size );
     
     colorModel.getGreens( buff );
     System.arraycopy( buff , 0, green , 1 , size );
     
     colorModel.getBlues( buff );
     System.arraycopy( buff , 0, blue , 1 , size );

     int s =size;
     MarkerColorIndex = s;
     red[s]   = (byte)255;
     green[s] = (byte) 255;
     blue[s]  = (byte)255; 
     alpha[s] = (byte)255;
     
     int nbits =(int)(.5+Math.log( size+1 )/Math.log( 2 ));
     
     if( nbits < 1)
        nbits = 3;
     
     return new IndexColorModel( nbits ,size+1,red,green,blue,alpha);
  }
  
  private float[] getTimeInfo( XScale SpectrXscale, float time1, float time2, XScale FxXscale,
                          float[] itsValues)
  {
     if( FxXscale != null)
     if( FxXscale == SpectrXscale)
        return itsValues;

     int frame1 = SpectrXscale.getI_GLB(time1 );
     int frame2 = SpectrXscale.getI_GLB( time2 );
     float alpha1 = (time1 - SpectrXscale.getX( frame1 ))/
                           (SpectrXscale.getX( frame1 +1) - SpectrXscale.getX( frame1 ));
     float alpha2 = (SpectrXscale.getX( frame2+1 )-time2)/
                   (SpectrXscale.getX( frame2+1 )-SpectrXscale.getX( frame2 )) ;
     float[] Res = new float[4];
     Res[0]= frame1;
     Res[1] = frame2;
     Res[2] = alpha1;
     Res[3] = alpha2;
     
     return Res;
  }
  

  private int[][]  getColorIndices( IDataGrid grid, XScale xscl,float time, float[] yvalsA)
  {
     XScale base = xscl0;
     if( xscl == null ||  xscl.getNum_x( )==0)
        xscl = base;
     
     int frame = xscl.getI( time );
     
     if( frame+1>= xscl.getNum_x( ))
        frame = xscl.getNum_x( )-2;
     float time1 = xscl.getX( frame );
     float time2 =xscl.getX( frame+ 1 );
     float[] orig = getTimeInfo(base, time1,time2, null, null);
     
     int[][] Colors = new int[grid.num_rows( )][grid.num_cols( )];
     float scale_factor =(log_scale.length-2)/MaxAbs;
     Integer DIndx = DetID2Index.get(grid.ID());
     int dIndex = -1;
    
     int[][] Indicies2 = null;
    if( DIndx != null && DIndx.intValue()>=0 && DIndx.intValue()< Grids.length )
        dIndex = DIndx.intValue( );
     
      //yvalsA = rebinner.getY_valuesAtX( time );
    
     if( dIndex >=0)
        Indicies2 = DRC2Index[dIndex];
  
     for(  int col=0; col< grid.num_cols( ); col++)
     {  int[] Indicies1 = null;
        if( Indicies2 != null)
           Indicies1= Indicies2[col];
        for(int row=0; row < grid.num_rows( ); row++)
    
        {
           float S = 0;
           if( dIndex >=0 )
           {  int k =Indicies1[row];
              if( k >=0 )
                  S = yvalsA[ k ];
           }else
           {
              Data D = grid.getData_entry(  row+1 , col+1 );
              float[] yvals = D.getY_values();
              float[] inds = getTimeInfo( D.getX_scale( ), time1,time2,base, orig);
              int k1 = Math.min( (int)inds[1],yvals.length-1);
              for( int k= (int)inds[0]; k <= k1; k++)
                S += yvals[k];
              S -= inds[2]*(yvals[(int)inds[0]]);
              S -= inds[3]*( yvals[k1]);
            }
       
           float value = S*scale_factor;
           int index =0;
           if( value >=0)
              index =(int)( ZERO_COLOR_INDEX+1 + log_scale[ Math.min( (int)value, log_scale.length-1)]);
           else
              index =(int)( ZERO_COLOR_INDEX+1 - log_scale[ Math.min( -(int)value, log_scale.length-1)]);
           
           if( index ==0)
              index =1;
           else if (index == 2*NUM_POSITIVE_COLORS)
              index = 2*NUM_POSITIVE_COLORS -1;
           
           Colors[row][col] = index;
        }
     }
     return Colors;
  }
    
    //Finds point "on" line from Point1 to Point2 whose index-th component is IntVal
     private float[] getPoint( float[] Point1, float[]Point2, int index, int IntVal)
     { 
       float alpha = -1;
  
       if(Point1[index]!= Point2[index])
        
       alpha = (IntVal -Point2[index])/(Point1[index]-Point2[index]);
     
       else if ( Math.floor(Point2[index] ) == Point2[index] )
         alpha =0;
     
       float[] C = new float[3];
    // char[] spaces = new char[3*index+1];
    // Arrays.fill( spaces ,' ' );
       if ( alpha >=-.25 && alpha <= 1.25 )
       {  
       //System.out.print( spaces+"(" );
        for( int g=0; g<3;g++)
        {
          C[g] = Point2[g]*(1-alpha)+alpha*Point1[g];
          //System.out.print( C[g]+"," );
        }
      //System.out.println(")");
        for( int g=0; g <= index; g++ )
          C[g]= (float)Math.floor( C[g]+.01 );
        return C;
     }
     else
       return null;
     }
     
     public int[][] Do1Mark( IDataGrid grid, XScale xscl, float time1, 
                             float L0, float[][] UB)
     {
        float[][][] CornerQs = new float[5][2][3];
        int i1 =xscl.getI_GLB(time1)-2;
        int i2 = i1+4;
        if( i1+1 >= xscl.getNum_x( ))
        {
           i1--;
           i2--;
        }
        if( i1 < 0)
        {
           i1++;
           i2++;
        }
        float[] time= new float[2];
        time[0] = xscl.getX( i1 );
        time[1] = xscl.getX( i2 );
        Vector3D minPos = findMinPosition( grid );
        Vector3D[] positions = new Vector3D[5];
        positions[0] = grid.position( 1f,1f);
        positions[1] = grid.position( 1f,(float) grid.num_cols( ));
        positions[2] = grid.position( (float) grid.num_rows( ),(float) grid.num_cols( ));
        positions[3] = grid.position( (float) grid.num_rows( ),1f);
        positions[4] = minPos;
        SampleOrientation sampOrient = AttrUtil.getSampleOrientation( getDataSet() );
        Tran3D invOrient = sampOrient.getGoniometerRotationInverse( );
        for( int i=0;i<5;i++)
        {  
           Vector3D P = new Vector3D(positions[i]);

           float angle_radians = (float)Math.acos( P.getX()/P.length() );
           
           P.subtract( new Vector3D(P.length( ),0,0) );
           P.normalize( );
           for( int s=0; s<2;s++)  
           {
              float Q = tof_calc.DiffractometerQ( angle_radians ,L0+positions[i].length() , time[s] );
              Vector3D V = new Vector3D(P);
              V.multiply((float)(Q/2/Math.PI));
              Vector3D RR = new Vector3D();
              invOrient.apply_to( V , RR );
              CornerQs[i][s][0]= RR.getX( );
              CornerQs[i][s][1]= RR.getY( );
              CornerQs[i][s][2]= RR.getZ( );
           }
        }
        
        // Now get hkl's
        float[][][] Cornerhkls = new float[5][2][3];
        float[][]invUB = LinearAlgebra.getInverse( UB );
        for( int i=0; i<5;i++)
           for( int s=0; s<2;s++)
           {
              Cornerhkls[i][s] = LinearAlgebra.mult( invUB , CornerQs[i][s] );
           }
        
        float min= Integer.MAX_VALUE ;
        float max =Integer.MIN_VALUE;
        for( int i=0; i<5;i++)
           for( int s=0; s<2;s++)
              {
                 if( Cornerhkls[i][s][0] <min)
                    min=Cornerhkls[i][s][0] ;

                 if( Cornerhkls[i][s][0] > max)
                    max = Cornerhkls[i][s][0] ;
              }
        
        java.util.Vector<float[]> Res = new java.util.Vector<float[]>();
        float missVal = .4f;
        int hmin =(int) Math.floor( min -missVal);
        if( hmin != min)
           hmin++;
        int hmax =(int) Math.floor(  max+missVal);
      
        for( int h = hmin; h <=hmax; h++)
        {   int pt = 0;
            
            float[][] C = new float[25][3];
            for( int s=0; s<2;s++)
            {
               for( int i=0; i<5;i++)
                  for( int I=i+1; I<5;I++)
               {
                     float[] CC = getPoint( Cornerhkls[I][s], Cornerhkls[i][s],0,h);
                     if( CC != null)
                     {
                        C[pt]=CC;
                        pt++;
                     }
               }
            }
            for( int i=0; i<5;i++)
            {
               float[]CC = getPoint(Cornerhkls[i][0],Cornerhkls[i][1],0,h);
               if( CC != null)
               {
                  C[pt]= CC;
                  pt++;
               }
            }

            //-----------------now find k's; ----------------------------
            float mink= Integer.MAX_VALUE;
            float maxk = Integer.MIN_VALUE;
            for( int i=0;i<pt;i++)
            {
               if( C[i][1]<mink )
                  mink= C[i][1];
               if( C[i][1]>maxk )
                  maxk= C[i][1];           
             }
            int Mink = (int) Math.floor( mink -missVal);
            if( mink != Mink)
               Mink++;
            int Maxk = (int) Math.floor( maxk+missVal);
            
            for( int k= Mink; k<= Maxk; k++)
            {
               java.util.Vector<float[]> ConsthkPts = new java.util.Vector<float[]>();
               for( int i=0; i < pt;i++)
                 for( int j=i+1; j<pt;j++)
                 {                 
                    float[] CC = getPoint(C[j],C[i],1,k);
                    if( CC != null)
                       ConsthkPts.add( CC );
                 }
               
                int N= ConsthkPts.size();
                java.util.Vector<float[]> internal = new java.util.Vector<float[]>();
                for(  int i=0; i<N;i++)
                   for( int j=i+1;j<N;j++)
                   {
                      float[] p1 = ConsthkPts.get( i );
                      float[] p2 = ConsthkPts.get( j );
                      int lmin = Math.min(  (int)Math.floor(p1[2]-missVal) , (int)Math.floor(p2[2]-missVal) );
                      int lmax = Math.max(  (int)Math.floor(p1[2]+missVal) , (int)Math.floor(p2[2]+missVal) );
                      if( lmin !=p1[2] && lmin != p2[2])
                         lmin++;
                      for( int l = lmin; l<=lmax;l++)
                      {
                         float[] p = new float[3];
                         p[0]=p1[0];
                         p[1] = p1[1];
                         p[2] = l;
                         boolean neww = true;
                         for( int ll =0; ll<internal.size() && neww; ll++ )
                         {
                            float[] pp = internal.get( ll );
                            if( pp[2]==p[2])
                               neww = false;
                         }
                         if( neww)  
                            {
                             Res.add(p);
                             internal.add(p);
                            }
                      }
                   }
            }
         }
     
        Tran3D Orient = sampOrient.getGoniometerRotation( );
        //Convert to positions
        java.util.Vector<float[]> VRes = new java.util.Vector<float[]>();
        for( int i=0; i<Res.size( );i++)
        {
           float[]p = Res.get( i );
           p = LinearAlgebra.mult( UB , p );//now a q;
           Vector3D R = new Vector3D();
           Orient.apply_to( new Vector3D(p) , R);
           float Q = 2*(float)Math.PI*R.length( );
        
           float s= R.length()*R.length()/2/Math.abs( R.getX() );
           R.set(  R.getX( )+ s, R.getY( ),R.getZ() );
           R.normalize( );
           //Now find r,c where hit detector
           float kk = grid.position( ).dot( grid.z_vec() )/R.dot( grid.z_vec() );
           R.multiply( kk );
           R.subtract( grid.position() );//position of point on grid
           //R.multiply(-1);
           float y = R.dot( grid.y_vec( ) );
           float x = R.dot( grid.x_vec( ) );
           float row = grid.row( x,y);
           float col = grid.col(x,y);
           if( row <.5 || col <.5 || row >grid.num_rows( )+.5 ||  
                 col > grid.num_cols( )+.5)
           {
            p[0]=Float.NaN; p[1]=Float.NaN;
           // Res.set( i , p );
           }else
           {
              Vector3D pos =  grid.position(row,col);
              float Time = tof_calc.TOFofDiffractometerQ( 
                                (float) Math.acos( pos.getX( )/pos.length()),
                                L0+pos.length() , Q );
              if( Time >= time[0] && Time <=time[1])
              {  
                 p[0]= row -1;
                 p[1] = col -1;
                 VRes.add( p );
              }
              else
              {
                 p[0]=Float.NaN; p[1]=Float.NaN;
                // Res.set( i , p );
              }
           }
        }
        if( VRes.size() <=0)
           return null;
        
        int[][] RRes = new int[VRes.size()][2];
        for( int i=0; i<RRes.length; i++)
           for( int j=0; j<2;j++)
              RRes[i][j] =(int) VRes.get( i )[j];
        return RRes;       
     }
  
  
  public Vector3D findMinPosition( IDataGrid grid)
  {
     float k = grid.position( ).dot( grid.z_vec() );
     
     Vector3D line = new Vector3D(grid.position( ));
     Vector3D base = new Vector3D( grid.z_vec());
     base.multiply( k );
     line.subtract( base);
     float x = line.dot( grid.x_vec() );
     float y = line.dot(  grid.y_vec() );

     if( Math.abs( x) < grid.width()/2 && Math.abs( y )< grid.height()/2 )
        return grid.position( grid.row( x , y ), grid.col( x , y ));
     
     if( Math.abs( x )< grid.width()/2)
     {
        Vector3D Res = new Vector3D(grid.position());
        Vector3D X = new Vector3D( grid.x_vec());
        X.multiply( x );
        Res.add(X);
        return Res;
        
     }else if( Math.abs(y) < grid.height()/2)
     {
        Vector3D Res = new Vector3D(grid.position());
        Vector3D X = new Vector3D( grid.y_vec());
        X.multiply( y );
        Res.add(X);
        return Res;
        
     }else if( y/x >= grid.height()/grid.width())//hit edge par xvec
     {
        x=grid.height()/2*x/y;
        Vector3D Res = new Vector3D(grid.position());
        Vector3D X = new Vector3D( grid.x_vec());
        X.multiply( x );
        Res.add(X);
        return Res;
        
     }else //hit edge parallel to yvec
     {
        y= grid.width()/2*y/x; 
        Vector3D Res = new Vector3D(grid.position());
        Vector3D X = new Vector3D( grid.y_vec());
        X.multiply( y );
        Res.add(X);
        return Res;
     }
  }
  
  public void setMaxAbs()
   {
     float[] max_min = rebinner.getDataRange( );
     if( Math.abs( max_min[0] ) > Math.abs( max_min[1] ))
        MaxAbs = Math.abs( max_min[0] );
     else
        MaxAbs =Math.abs( max_min[1] );
   }
 
  public void setColorModel()
  {
    int Nprocessors = Runtime.getRuntime().availableProcessors()-1;
    DoSet1ColorModel[] RunList = new DoSet1ColorModel[Grids.length];

    float L0 = AttrUtil.getInitialPath( getDataSet() );
    float[][] UB = AttrUtil.getOrientMatrix( getDataSet() );
    float time =((Number)frame_control.getControlValue()).floatValue( );
    float[] yvalsA =rebinner.getY_valuesAtX( time );
    for( int i=0; i< Grids.length; i++)
    {
      //IDataGrid grid1 = Grid_util.getAreaGrid( getDataSet() , Grids[i] );
      UniformGrid grid = (UniformGrid)Grids[i];
      XScale xscl =(XScale)x_scale_ui.getControlValue( );        
       
      RunList[i]= new DoSet1ColorModel( grid, xscl,time, Detector[i], 
                                        L0, UB, yvalsA );
    }
    int lastDetector = 0;
    int nactive =0;
    while( lastDetector < Detector.length ||
           (lastDetector >=Detector.length && nactive > 0) )
     {
       while( nactive < Nprocessors  &&  lastDetector < Detector.length)
       {
         RunList[lastDetector].start();
         lastDetector++;
    	 nactive++;
       }
       if ( lastDetector >= Detector.length)
            lastDetector = Detector.length;
     
       for( int i=0; i<lastDetector; i++)
       {
         if( RunList[i] != null)
           if( RunList[i].getState() == Thread.State.TERMINATED)
           {
             RunList[i] = null;
          // System.out.println("Finished "+i);
             nactive--;
           }
       }
       if ( nactive >= Nprocessors )
         try 
         {
           //System.out.println("Sleep,lastDetector, nactive="+
           //                    lastDetector+","+nactive);
           Thread.sleep(10);
         } 
         catch (InterruptedException e) 
         {
           e.printStackTrace();
         }
     }
     threeD_panel.repaint();  
  }
  
  class DoSet1ColorModel extends Thread
  {
    IDataGrid grid;
    XScale xscl;
    float time;
    ImageRectangle Detector;
    float L0;
    float[][]UB;
    float[] yvalsA;

    public DoSet1ColorModel( IDataGrid grid, XScale xscl, float time,
                             ImageRectangle Detector, float L0, float[][]UB,
		             float[] yvalsA )
    {
      this.grid = grid;
      if ( xscl != null )
        this.xscl = xscl;
      else
        this.xscl = xscl0;
      this.time = time;
      this.Detector = Detector;
      this.L0 = L0;
      this.UB = UB;
      this.yvalsA = yvalsA;
    }
	  
    public void run()
    {
      int[][] MarkedCells = null;
      try
      {
        if ( ShowLattice )
          MarkedCells = Do1Mark( grid,xscl, time, L0, UB );
		     
        int[][] ColorIndicies = getColorIndices( grid, xscl, time, yvalsA );
        if( ColorIndicies != null)
        {
          if( MarkedCells != null && MarkerColorIndex >0)
            for( int i=0; i< MarkedCells.length; i++)
            {
              int row = MarkedCells[i][0];
              int col = MarkedCells[i][1];
              for( int r = Math.max( row-10 , 0 ); 
                       r <= Math.min( row+10, grid.num_rows()-1 ); r++ )
              {
                if ( col-10 >= 0 ) 
                  ColorIndicies[r][col-10] = MarkerColorIndex; 
                if ( col+10 <= grid.num_cols()-1 )  
                  ColorIndicies[r][col+10] = MarkerColorIndex;   
              }
              for( int c = Math.max( col-10 , 0 ); 
                       c <= Math.min( col+10, grid.num_cols()-1); c++ )
              {
                if ( row-10 >=0 ) 
                  ColorIndicies[row-10][c] =MarkerColorIndex; 
                if ( row+10 <=grid.num_rows()-1 ) 
                  ColorIndicies[row+10][c] =MarkerColorIndex;   
              }
            }
          Detector.setColorModel( ColorIndicies, colorModelwTransp);
        }
      }catch(Exception s)
      {
        return;
      }
    }
  }
  
  private void setLogScale( double s )
  {
    if ( s > 100 )                                // clamp s to [0,100]
      s = 100;
    if ( s < 0 )
      s = 0;

    s = Math.exp(20 * s / 100.0) + 0.1; // map [0,100] exponentially to get
                                        // scale change that appears more linear
    double scale = (NUM_POSITIVE_COLORS) / Math.log(s);

    log_scale = new float[LOG_TABLE_SIZE];

    for ( int i = 0; i < LOG_TABLE_SIZE; i++ )
      log_scale[i] = (byte)
                     (scale * Math.log(1.0+((s-1.0)*i)/LOG_TABLE_SIZE));
  }

  // increases this.MaxAbs if MaxAbs is more
  private synchronized void setMaxAbs( float MaxAbs)
  {
    if( MaxAbs > this.MaxAbs)
      this.MaxAbs = MaxAbs;
  }
 
  
  class MyRebin1 extends Thread
  {
    IDataGrid grid;
    XScale xscl;
    public MyRebin1( IDataGrid grid, XScale xscl)
    {
      this.grid = grid;
      if( xscl != null)
        this.xscl = xscl;
      else
       this.xscl = xscl0;  
   }
	  
   public void run()
   {
     MaxAbs =0;
     for( int row =1 ; row <= grid.num_rows(); row++)
       for( int col=1; col <= grid.num_cols(); col++)
         {
           Data Db = grid.getData_entry(row,col); 
           Db.resample(  xscl , 0 );
           float[] yVals = Db.getY_values( );
           if( yVals != null)
           for( int j=0; j<yVals.length; j++)
           {
             float val =Math.abs(yVals[j]);
             if( val > MaxAbs)
               MaxAbs=val;
          }
        }
      if( MaxAbs == 0 ) 
        MaxAbs = 10;
     
      setMaxAbs( MaxAbs );
    }
  }
  
  /**
   * This rebins all the data sets and set frame controller and
   * colors for all detector.
   * 
   * @param xscl
   */
  private void setNewXScale( XScale xscl)
  {
     float time = frame_control.getFrameValue( );
    
     MaxAbs =0;
     
     if( xscl== null || xscl.getNum_x( )<=0)
        rebinner.reset( );
     else
        rebinner.setXScale( xscl );
     xscl = rebinner.getXScale( );
     frame_control.setFrame_values( xscl.getXs());
     frame_control.setFrameValue(time );
     
     MaxAbs = 0;
     setMaxAbs();
     setColorModel();
  }

  public void processMouseClickEvent( MouseEvent e)
  {
     float pix_x = e.getX( );
     float pix_y =e.getY();
     float dist = Float.MAX_VALUE;
     float[] Res = new float[3];
     float[] BestRes = new float[3];
     int DetIndex = -1;
     Arrays.fill( BestRes , -1 );
     for( int i=0; i< Detector.length; i++)
     {
        if( Detector[i].getColRowAndDistance( pix_x , pix_y , Res ))
           if( Res[2] < dist)
           {
              System.arraycopy( Res,0,BestRes,0,3);
              dist = Res[2];
              DetIndex = i;
           }
     }
     
     if( BestRes[2] < 0 || DetIndex < 0 || DetIndex >= Grids.length)
        return;
     
     //IDataGrid grid1 = Grid_util.getAreaGrid( getDataSet() , Grids[DetIndex]);
     UniformGrid grid = (UniformGrid)Grids[DetIndex];
     
     if( grid == null)
        return;
     
     Data Db = grid.getData_entry( 1+(int)BestRes[1], 1+(int)BestRes[0]);
     if( Db == null)
        return;
     
     DataSet DS = getDataSet();
     int DSindx = DS.getIndex_of_data( Db );
     if( DSindx <0 || DSindx >= DS.getNum_entries( ))
        return;
     
     DS.setPointedAtIndex( DSindx );
     float time = frame_control.getFrameValue( );
     redraw_cursor = false;
     DS.setPointedAtX(time );
     DS.notifyIObservers( IObserver.POINTED_AT_CHANGED );
     notify_ds_observers = true;
     return;
  }

 
  private void SetUpDRC2Index( )
  {
    if ( Grids == null || Grids.length == 0 )
      throw new IllegalArgumentException( "NO AREA DETECTORS IN DATA SET" );

    IDataGrid grid = Grids[0];
    DRC2Index = new int[Grids.length][grid.num_cols( )][grid.num_rows( )];
    java.util.Vector<IOperator> clears = new java.util.Vector<IOperator>();
    for( int i=0; i <Grids.length; i++)
       clears.add( new Clear1DRCIndex( i));
    
    int maxThreads =Runtime.getRuntime().availableProcessors()-1;
    ParallelExecutor Pexec = new ParallelExecutor( clears, maxThreads, 1000);
     java.util.Vector Res = Pexec.runOperators( );
     if( Res == null || Res.size() < Grids.length)
        System.out.println("Clearing was unsuccessful");
     DetID2Index = new Hashtable<Integer,Integer>();
     for( int i=0; i< Grids.length; i++)
        DetID2Index.put( Grids[i].ID( ) , i );
     
     java.util.Vector<IOperator> SetEntries = new java.util.Vector<IOperator>();
     DataSet DS = getDataSet();
     int NDataPerProcess = (int)( DS.getNum_entries( )/(float)maxThreads + 1);
     int NProcesses = 0;
     for( int startIndex =0; startIndex < DS.getNum_entries();
                            startIndex +=NDataPerProcess)
     {
        SetEntries.add( new Do1DRC2Index( DS, 
                                          startIndex, 
                                          startIndex+NDataPerProcess,
                                          DetID2Index));
        NProcesses ++;
     }

     ParallelExecutor Pexec1 =
                  new ParallelExecutor( SetEntries, maxThreads, 60000000 );
     java.util.Vector Res1 = Pexec1.runOperators( );
     if( Res == null || Res1.size()< NProcesses )
     {
        System.out.println(
                      "Could not set up all entries in Det,Row,Col to index");
     }
  }
  
  class Do1DRC2Index  implements IOperator
  {
     DataSet DS;
     int startIndex; 
     int endIndex;
     Hashtable<Integer,Integer> GridID2Index;
     public Do1DRC2Index( DataSet DS, 
                          int startIndex, int endIndex, 
                          Hashtable<Integer,Integer> GridID2Index)
     {
        this.DS = DS;
        this.startIndex = startIndex ; 
        this.endIndex = endIndex;
        this.GridID2Index = GridID2Index;
     }
     
     public Object getResult()
     {
         if( startIndex < 0)
            startIndex =0;
         if( endIndex <= 0)
            return null;
         if( startIndex >= DS.getNum_entries( ))
            return null;
         if( endIndex > DS.getNum_entries())
            endIndex = DS.getNum_entries();
         for( int i= startIndex; i < endIndex; i++)
         {
            Data D = DS.getData_entry(i);
            PixelInfoList plist = AttrUtil.getPixelInfoList(  D );
            if( plist != null && plist.num_pixels( )>0)
            {
               IPixelInfo pix0 = plist.pixel( 0);
               int row = (int)(.5+pix0.row( ));
               int col = (int)(.5+ pix0.col( ));
               
               Integer Ind = GridID2Index.get( pix0.DataGrid( ).ID()) ;
               if( Ind != null && Ind.intValue()>=0 && 
                     Ind.intValue()< DRC2Index.length &&
                     row >=1 && col >=1
                     )
                  DRC2Index[Ind.intValue()][col-1][row-1] = i;
            }
         }
        return null; 
     }
  }


  class Clear1DRCIndex implements IOperator
  {
     int detIndex;
     public Clear1DRCIndex( int detIndx)
     {
        this.detIndex = detIndx;
     }
     public Object getResult()
     {
       if( detIndex < 0 || detIndex >= DRC2Index.length)
          return null;
       
       for( int c= 0; c< DRC2Index[detIndex].length; c++)
          Arrays.fill( DRC2Index[detIndex][c] , -1 );
       
       return null;
     }
  }

  class XScaleListener implements ActionListener
  {
   @Override
   public void actionPerformed(ActionEvent e)
   {
      if( !e.getActionCommand().equals( XScaleController.XSCALE_CHANGED))
         return;
      XScale xscl =(XScale)x_scale_ui.getControlValue( );
      
      MaxAbs =0;
      setNewXScale( xscl);
   }
  }
  
  class  LogScaleEventHandler implements ChangeListener
  {
   @Override
   public void stateChanged(ChangeEvent e)
   {
      JSlider slider = (JSlider)e.getSource();

      // set new log scale when slider stops moving
     if ( !slider.getValueIsAdjusting() )
      {
         int value = slider.getValue();
         setLogScale( value );
         getState().set_int( ViewerState.BRIGHTNESS, value );
         if( Detector != null)
        	 setColorModel();
       }
   }
  }
  
  class  AltAzControlListener implements ActionListener
  {

   @Override
   public void actionPerformed(ActionEvent e)
   {
      float altitude = view_control.getAltitudeAngle(); 
      float azimuth  = view_control.getAzimuthAngle(); 
      float distance = view_control.getDistance(); 
      
      getState().set_float( ViewerState.V_AZIMUTH,  azimuth );
      getState().set_float( ViewerState.V_ALTITUDE, altitude );
      getState().set_float( ViewerState.V_DISTANCE, distance );
   }
  }
  

  private void setNewData( float time )
  {
    setColorModel();
  }
  
  class FrameControlListener implements ActionListener
  {
   @Override
   public void actionPerformed(ActionEvent e)
   {
       if( !e.getActionCommand().equals( FrameController.FRAME_CHANGED ))
          return;
       float time = ((Float)frame_control.getControlValue()).floatValue( );
    
       setColorModel();
       getDataSet().setPointedAtX( time  );
       getDataSet().notifyIObservers( IObserver.POINTED_AT_CHANGED );
       notify_ds_observers= true;
   }
  }
  

  class ViewMouseMotionAdapter  extends MouseMotionAdapter
  {
     public void mouseDragged( MouseEvent e )
     {
        processMouseClickEvent( e );
     }
  }
 
  class ViewMouseAdapter extends MouseAdapter
  {
     public void mouseClicked(MouseEvent e)
     {
        
        processMouseClickEvent( e );
     }
  }
  
  /* -------------------------- OptionMenuHandler --------------------------- */
  /**
   *  Listen for Option menu selections and just print out the selected option.
   *  It may be most convenient to have a separate listener for each menu.
   */
  private class OptionMenuHandler implements ActionListener
  {
    public void actionPerformed( ActionEvent e )
    {
      String action = e.getActionCommand();
      colorModel = IndexColorMaker.getDualColorModel( action,
                                                       NUM_POSITIVE_COLORS );

      colorModelwTransp = BuildAlphaColModel( colorModel);
      color_scale_image.setNamedColorModel( action, true, true );
      setColorModel();
    
      getState().set_String( ViewerState.COLOR_SCALE, action );
    }
  }
}
