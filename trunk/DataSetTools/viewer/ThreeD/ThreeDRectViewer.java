package DataSetTools.viewer.ThreeD;

import gov.anl.ipns.Util.Numeric.ClosedInterval;
import gov.anl.ipns.ViewTools.Components.IPreserveState;
import gov.anl.ipns.ViewTools.Components.ObjectState;
import gov.anl.ipns.ViewTools.Components.ViewControls.FrameController;
import gov.anl.ipns.ViewTools.Components.ViewControls.XScaleController;
import gov.anl.ipns.ViewTools.Panels.Image.ImageJPanel;
import gov.anl.ipns.ViewTools.Panels.Image.IndexColorMaker;
import gov.anl.ipns.ViewTools.Panels.ThreeD.AltAzController;
import gov.anl.ipns.ViewTools.Panels.ThreeD.ImageRectangle;
import gov.anl.ipns.ViewTools.Panels.ThreeD.ThreeD_JPanel;
import gov.anl.ipns.ViewTools.UI.ColorScaleMenu;
import gov.anl.ipns.ViewTools.UI.FontUtil;
import gov.anl.ipns.ViewTools.UI.SplitPaneWithState;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
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
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.Grid_util;
import DataSetTools.dataset.IDataGrid;
import DataSetTools.dataset.OperationLog;
import DataSetTools.dataset.UniformXScale;
import DataSetTools.dataset.XScale;
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
   private float   last_pointed_at_index = -1;
   private boolean redraw_cursor       = true;
   private boolean notify_ds_observers = true;

   private ThreeD_JPanel            threeD_panel      = null; 
   //private ImageJPanel              color_scale_image = null;
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
     
      ClosedInterval interval =dataSet.getYRange( );
      MaxAbs = Math.abs( interval.getEnd_x( ) );
      if( Math.abs(interval.getStart_x( ))>MaxAbs)
         MaxAbs =Math.abs(interval.getStart_x( ));
      for( int i=0; i< gridIDs.length; i++)
         {
             
         IDataGrid grid = Grid_util.getAreaGrid( dataSet , gridIDs[i] );
         int[][] ColorIndicies = getColorIndices( grid, x_scale, x_scale.getStart_x());
         Detector[i] = new ImageRectangle( grid.position( ), grid.x_vec( ), grid.y_vec( ),
                            grid.width( ), grid.height(), ColorIndicies,colorModelwTransp,
                           threeD_panel);
             
         }
      
      threeD_panel.setObjects(  "Detectors" , Detector );
    
      //-------------------------XScale Chooser ----------------------------
      String label = getDataSet().getX_units();
      float x_min  = x_scale.getStart_x();
      float x_max  = x_scale.getEnd_x();
                                    // set n_steps to 0 to default to NO REBINNING
      x_scale_ui = new XScaleController( "X Scale", label, x_min, x_max, x_scale.getNum_x( ) );

      x_scale_ui.addActionListener( new XScaleListener() );
      
    ;
      
      
      //---------------------- AltAzController ------------------

      view_control = new AltAzController();
      view_control.addActionListener( new AltAzControlListener() );
      view_control.addControlledPanel( threeD_panel );
      view_control.apply( true );
      
      //------------------ Frame Controller --------------------
      frame_control = new FrameController();
      frame_control.setBorderTitle( getDataSet().getX_label() );
      frame_control.setTextLabel( getDataSet().getX_units() );
      frame_control.setFrame_values(x_scale.getXs());
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

      // TODO Auto-generated method stub

   }

   public javax.swing.JComponent getDisplayComponent()
   {
      return threeD_panel;
   }
   
   public ObjectState getObjectState(boolean isDefault)
   {
      /**
       *  getState().set_float( ViewerState.V_AZIMUTH,  azimuth );
      getState().set_float( ViewerState.V_ALTITUDE, altitude );
      getState().set_float( ViewerState.V_DISTANCE, distance );
      RowCol states
       */
      return null;
   }
   
   public void setObjectState(ObjectState new_state)
   {
      
   }
   /**
    * @param args
    */
   public static void main(String[] Args)
   {
      Object[] args = new Object[5];
     
      args[0] ="C:/ISAW/SampleRuns/SNS/TOPAZ/TOPAZ_3007_histo.nxs";
      args[1] = false;
      //args[1] = 6;
     args[2] = "none"; 
     args[3] ="TOF_NSCD"; 
     args[4] =false; 
      
      Object Res = null;
      try
      {
         //Res = Command.ScriptUtil.ExecuteCommand( "OneDS" , args);
         Res = Command.ScriptUtil.ExecuteCommand( "LoadMergeNXS" , args);
      }catch( Exception s)
      {
         s.printStackTrace( );
         System.exit(0);
      }
      
      
     ViewManager vm = (new ViewManager( (DataSet)Res, IViewManager.THREE_D_RECT));

   }
   
  private void MakeGUI()
  {
     setLayout( new GridLayout(1,1));
    
     control_panel = new Box( BoxLayout.Y_AXIS );
     control_panel.add( view_control);
     control_panel.add( x_scale_ui);
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
     int[][] Colors = new int[grid.num_rows( )][grid.num_cols( )];
     float scale_factor= log_scale.length/MaxAbs;
     for( int row=0; row < grid.num_rows( ); row++)
        for( int col=0; col< grid.num_cols( ); col++)
        {
           
           float value =grid.getData_entry( row+1 , col+1 ).getY_values()[frame];
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

  class XScaleListener implements ActionListener
  {

   @Override
   public void actionPerformed(ActionEvent e)
   {
      if( !e.getActionCommand().equals( XScaleController.XSCALE_CHANGED))
         return;
      XScale xscl =(XScale)x_scale_ui.getControlValue( );
      float time = frame_control.getFrameValue( );
      DataSet Ds = getDataSet();
      for( int i=0; i< Ds.getNum_entries( ); i++)
         Ds.getData_entry( i).resample(  xscl , 0 );
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
  
  class ViewMouseMotionAdapter  extends MouseMotionAdapter
  {
     
  }
  
  class ViewMouseAdapter extends MouseAdapter
  {
     
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
      setColorModel();
    
      getState().set_String( ViewerState.COLOR_SCALE, action );
    }
  }
}
