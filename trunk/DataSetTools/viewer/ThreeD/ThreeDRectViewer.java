package DataSetTools.viewer.ThreeD;

import gov.anl.ipns.MathTools.Geometry.Vector3D;
import gov.anl.ipns.Util.Messaging.IObserver;
import gov.anl.ipns.Util.Numeric.ClosedInterval;
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
import gov.anl.ipns.ViewTools.Panels.ThreeD.ThreeD_JPanel;
import gov.anl.ipns.ViewTools.UI.ColorScaleImage;
import gov.anl.ipns.ViewTools.UI.ColorScaleMenu;
import gov.anl.ipns.ViewTools.UI.FontUtil;
import gov.anl.ipns.ViewTools.UI.SplitPaneWithState;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.IndexColorModel;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import DataSetTools.components.ui.DataSetXConversionsTable;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.Grid_util;
import DataSetTools.dataset.IDataGrid;
import DataSetTools.dataset.OperationLog;
import DataSetTools.dataset.UniformXScale;
import DataSetTools.dataset.XScale;
import DataSetTools.operator.DataSet.DataSetOperator;
import DataSetTools.operator.DataSet.Attribute.GetPixelInfo_op;
import DataSetTools.viewer.DataSetViewer;
import DataSetTools.viewer.IViewManager;
import DataSetTools.viewer.ViewManager;
import DataSetTools.viewer.ViewerState;


public class ThreeDRectViewer extends DataSetViewer implements Serializable,
      IPreserveState
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   int[] gridIDs = null;
   ImageRectangle[]  Detector;
   //private float   last_pointed_at_index = -1;
   private boolean redraw_cursor       = true;
   private boolean notify_ds_observers = true;

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
   private final int ZERO_COLOR_INDEX    = NUM_POSITIVE_COLORS;
   java.awt.image.IndexColorModel   colorModel = null;
   java.awt.image.IndexColorModel   colorModelwTransp = null;
   private float                    log_scale[]       = null; 
   float MaxAbs                                       =0;
   float last_pointed_at_x;
   

   private final Integer  AXES            = 200000000;
   //private final Integer  BEAM            = 300000000;
   //private final Integer  INSTRUMENT      = 400000000;
   
   // ----- ObjectState keys -------------------------
   public static String    VIEWER_STATE_OS="VIEWER_STATE_OS";
   public static String    FRAME_CONTROL_OS="FRAME_CONTROL_OS";
   public static String    VIEW_CONTROL_MINDIS_OS="VIEW_CONTROL_MINDIS_OS";//Array of mindist,maxdist, dist
   public static String    VIEW_CONTROL_MAXDIS_OS="VIEW_CONTROL_MAXDIS_OS";
   public static String    VIEW_CONTROL_DIS_OS="VIEW_CONTROL_DIS_OS";
   public static String    XSCALE_OS ="XSCALE_OS";
   public static String   LASTPOINEDAT_OS="LASTPOINEDAT_OS";
   public static String    MAXABS_OS ="MAXABS_OS";
   public static String   AZIMUTH_OS="AZIMUTH_OS";
   public static String   ALT_OS="ALT_OS";
   float MinDis,MaxDis;
   
   
   public ThreeDRectViewer(DataSet dataSet)
   {

      this( dataSet , null );
    
      // TODO Auto-generated constructor stub
   }
   //TODO
//setDataSet needs to redo some of this stuff so separate out as an init
//set/getObjectState  add AltAz control status, other ViewControl( check if set and getObject State
//    implemented.
   public ThreeDRectViewer(DataSet dataSet, ViewerState state)
   {

      super( dataSet , state );
      DataSetOperator op = dataSet.getOperator( "Pixel Info" );
      if( op == null)
         System.out.println("No Pixel Info Operator");
      else
         ((GetPixelInfo_op)op).ShowDetector = true;
      ClosedInterval interval =dataSet.getYRange( );
      MaxAbs = Math.abs( interval.getEnd_x( ) );
      if( Math.abs(interval.getStart_x( ))>MaxAbs)
         MaxAbs =Math.abs(interval.getStart_x( ));
      
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
      
      gridIDs = Grid_util.getAreaGridIDs( dataSet );
   
      UniformXScale x_scale  = getDataSet().getXRange();
      Detector = new ImageRectangle[ gridIDs.length];
     
      MinDis = Float.MAX_VALUE;
      MaxDis = Float.MIN_VALUE;
      last_pointed_at_x = x_scale.getStart_x();
      for( int i=0; i< gridIDs.length; i++)
         {
             
         IDataGrid grid = Grid_util.getAreaGrid( dataSet , gridIDs[i] );
         int[][] ColorIndicies = getColorIndices( grid, x_scale, x_scale.getStart_x());
         Detector[i] = new ImageRectangle( grid.position( ), grid.x_vec( ), grid.y_vec( ),
                            grid.width( ), grid.height(), ColorIndicies,colorModelwTransp,
                           threeD_panel);
         float D = grid.position( ).length( );
         if( D< MinDis)
            MinDis = D;
         if( D > MaxDis)
            MaxDis = D;
             
         }
      
      threeD_panel.setObjects(  "Detectors" , Detector );
      draw_axes( MaxDis/5);
      //-------------------- color_scale------------------------
      color_scale_image = new ColorScaleImage();
      color_scale_image.setNamedColorModel( 
                  getState().get_String( ViewerState.COLOR_SCALE), true, true );

      //-------------------------XScale Chooser ----------------------------
      String label = getDataSet().getX_units();
      float x_min  = x_scale.getStart_x();
      float x_max  = x_scale.getEnd_x();
                                    // set n_steps to 0 to default to NO REBINNING
      x_scale_ui = new XScaleController( "X Scale", label, x_min, x_max, x_scale.getNum_x( ) );

      x_scale_ui.addActionListener( new XScaleListener() );
      XScale xscl = (XScale)x_scale_ui.getControlValue( );
 
      
      
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
      frame_control.addActionListener( new FrameControlListener() );
     
      setNewXScale( xscl);
 
      
      //---------------- DS Conversions ------------------------
      conv_table = new DataSetXConversionsTable( getDataSet() );
      JPanel conv_panel = new JPanel();
      conv_panel.setLayout( new GridLayout(1,1) );
      conv_panel.add( conv_table.getTable() );
      border = new TitledBorder( LineBorder.createBlackLineBorder(), "Pixel Data");
      border.setTitleFont( FontUtil.BORDER_FONT );
      conv_panel.setBorder( border );
      
      //--------------- Make GUI ----------------------------
     
       MakeGUI();
       
       redraw( NEW_DATA_SET );
      
      
      threeD_panel.addMouseMotionListener( new ViewMouseMotionAdapter() );
      threeD_panel.addMouseListener( new ViewMouseAdapter() );
      
      OptionMenuHandler option_menu_handler = new OptionMenuHandler();
      JMenu option_menu = menu_bar.getMenu( OPTION_MENU_ID );

                                                         // color options
      JMenu color_menu = new ColorScaleMenu( option_menu_handler );
      option_menu.add( color_menu );
    
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
            frame_control.setControlValue( last_pointed_at_x, false );
            redraw_cursor = false;
          }
          
          if (index != DataSet.INVALID_INDEX && ! Float.isNaN( last_pointed_at_x ))
          {
           
            float y_val = ds.getData_entry( index ).getY_value( last_pointed_at_x , 0 );
            conv_table.showConversions( last_pointed_at_x, y_val, index );
          }

          notify_ds_observers = false;            // since we are setting the
                                                  // frame controller by request,
                                                  // don't notify ds observers

          redraw_cursor = true;          // just skip drawing for one notification
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
        state.insert( LASTPOINEDAT_OS , last_pointed_at_x );
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
        state.insert( LASTPOINEDAT_OS , -1f);
        state.insert(  MAXABS_OS,MaxAbs);
        state.insert( ALT_OS ,45f  );
        state.insert( AZIMUTH_OS ,45f  );
     }
     
      return state;
   }
   
   public void setObjectState(ObjectState new_state)
   {
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
     last_pointed_at_x= ((Float)new_state.get( LASTPOINEDAT_OS )).floatValue( );
     MaxAbs =((Float)new_state.get( MAXABS_OS )).floatValue( );

     XScale xscl1 = (XScale)x_scale_ui.getControlValue( );
     if( xscl1.getStart_x( ) != xscl.getStart_x() || xscl1.getEnd_x( ) != xscl.getEnd_x()
              || xscl1.getNum_x( ) != xscl.getNum_x())
           setNewXScale((XScale)x_scale_ui.getControlValue());
     
     int value = log_scale_slider.getValue();
     setLogScale( value );
     
     setColorModel();
     redraw(IObserver.POINTED_AT_CHANGED);
     
    
   }
   
   
   /**
    * @param args
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
      distance = MaxDist;
      view_control.setViewAngle( 45 );
      view_control.setAltitudeAngle( altitude );
      view_control.setAzimuthAngle( azimuth );
      view_control.setDistanceRange( .2f*MinDist , 4*MaxDist );//If change rule change in getObjectState
      view_control.setDistance( distance );
      view_control.apply( true );
   }
  
  private void MakeGUI()
  {
     setLayout( new GridLayout(1,1));
    
     control_panel = new Box( BoxLayout.Y_AXIS );
     control_panel.add( view_control);
     control_panel.add( x_scale_ui);
     control_panel.add(color_scale_image );
     control_panel.add( log_scale_slider );
     control_panel.add( frame_control);
     control_panel.add(  Box.createVerticalGlue( ) );
     control_panel.add( conv_table.getTable());
     
    JPanel graph_container = new JPanel();
     graph_container.setLayout( new GridLayout(1,1) );
     OperationLog op_log = getDataSet().getOp_log();
     if ( op_log.numEntries() <= 0 )
       title = "Graph Display Area";
     else
       title = op_log.getEntryAt( op_log.numEntries() - 1 );

     TitledBorder border = new TitledBorder( LineBorder.createBlackLineBorder(), title );
     border.setTitleFont( FontUtil.BORDER_FONT );
     graph_container.setBorder( border );
     graph_container.add( threeD_panel );
     
     split_pane = new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
                                          graph_container,
                                          control_panel,
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

//New colormodel where 0 represents completely transparent. The
  // other indecies map( with offset 1) to the other color model
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
     
     int nbits =(int)(.5+Math.log( size+1 )/Math.log( 2 ));
     
     if( nbits < 1)
        nbits = 3;
     
     return new IndexColorModel( nbits ,size+1,red,green,blue,alpha);
  }
  private int[][]  getColorIndices( IDataGrid grid, XScale xscl,float time)
  {
     int frame = xscl.getI( time );
     if( frame >= xscl.getNum_x( ))
        frame = xscl.getNum_x( )-1;
     int[][] Colors = new int[grid.num_rows( )][grid.num_cols( )];
     float scale_factor= log_scale.length/MaxAbs;
     for( int row=0; row < grid.num_rows( ); row++)
        for( int col=0; col< grid.num_cols( ); col++)
        {
           float[] yvals=grid.getData_entry( row+1 , col+1 ).getY_values();
           
           float value =yvals[Math.min( frame, yvals.length-1)];
           value *=scale_factor;
           int index =0;
           if( value >=0)
              index =(int)( ZERO_COLOR_INDEX+1 + log_scale[ Math.min( (int)value, log_scale.length-1)]);
           else
              index =(int)( ZERO_COLOR_INDEX+1 - log_scale[ Math.min( -(int)value, log_scale.length-1)]);
           Colors[row][col] = index;
           
        }
     return Colors;
  }
  
  public void setColorModel()
  {
     for( int i=0; i< Detector.length; i++)
        {
        IDataGrid grid = Grid_util.getAreaGrid( getDataSet() , gridIDs[i] );
        int[][] ColorIndicies = getColorIndices( grid, (XScale)x_scale_ui.getControlValue( ),
                           ((Number)frame_control.getControlValue()).floatValue( ));
            Detector[i].setColorModel( ColorIndicies, colorModelwTransp);
        }
     threeD_panel.repaint( );
  }
  
  private void setLogScale( double s )
  {
    if ( s > 100 )                                // clamp s to [0,100]
      s = 100;
    if ( s < 0 )
      s = 0;

    s = Math.exp(20 * s / 100.0) + 0.1; // map [0,100] exponentially to get
                                        // scale change that appears more linear
    double scale = NUM_POSITIVE_COLORS / Math.log(s);

    log_scale = new float[LOG_TABLE_SIZE];

    for ( int i = 0; i < LOG_TABLE_SIZE; i++ )
      log_scale[i] = (byte)
                     (scale * Math.log(1.0+((s-1.0)*i)/LOG_TABLE_SIZE));
  }

  private void setNewXScale( XScale xscl)
  {
     float time = frame_control.getFrameValue( );
     DataSet Ds = getDataSet();
     MaxAbs =0;
     for( int i=0; i< Ds.getNum_entries( ); i++)
     {
        Data Db = Ds.getData_entry( i); 
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
     if( MaxAbs ==0) MaxAbs = 10;
     
     frame_control.setFrame_values( xscl.getXs());
     frame_control.setFrameValue(time );
     for( int i=0; i< gridIDs.length;i++)
        {
        IDataGrid grid = Grid_util.getAreaGrid( Ds , gridIDs[i] );
        int[][] ColorIndicies = getColorIndices( grid, xscl, time );
        Detector[i].setColorModel( ColorIndicies , colorModelwTransp );
        }
     
     threeD_panel.repaint( );
  }
  
 
  class XScaleListener implements ActionListener
  {

   @Override
   public void actionPerformed(ActionEvent e)
   {
      if( !e.getActionCommand().equals( XScaleController.XSCALE_CHANGED))
         return;
      XScale xscl =(XScale)x_scale_ui.getControlValue( );
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
            for( int i=0; i< Detector.length;i++)
            {
               IDataGrid grid = Grid_util.getAreaGrid( getDataSet() , gridIDs[i] );
               int[][] ColorIndicies = getColorIndices( grid, (XScale)x_scale_ui.getControlValue( ),
                                  ((Number)frame_control.getControlValue()).floatValue( ));
               Detector[i].setColorModel( ColorIndicies, colorModelwTransp);
            }
         threeD_panel.repaint( );
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
  class FrameControlListener implements ActionListener
  {

   @Override
   public void actionPerformed(ActionEvent e)
   {
       if( !e.getActionCommand().equals( FrameController.FRAME_CHANGED ))
          return;
       float time = ((Float)frame_control.getControlValue()).floatValue( );
       for( int i=0; i< gridIDs.length; i++)

       {  IDataGrid grid = Grid_util.getAreaGrid( getDataSet() , gridIDs[i] );
          int[][] ColorIndicies = getColorIndices( grid, (XScale)x_scale_ui.getControlValue( ),
                                     time);
          Detector[i].setColorModel( ColorIndicies, colorModelwTransp);
       }
       threeD_panel.repaint();
      
   }
     
  }
  
  private void processMouseClickEvent( MouseEvent e)
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
     
     if( BestRes[2] < 0 || DetIndex < 0 || DetIndex >= gridIDs.length)
        return;
     
     IDataGrid grid = Grid_util.getAreaGrid( getDataSet() , gridIDs[DetIndex]);
     
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
     
     
     
        return;
     
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
