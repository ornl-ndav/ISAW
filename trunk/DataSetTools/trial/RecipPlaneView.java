/*
 * File:  RecipPlaneView.java
 *
 * Copyright (C) 2003, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.14  2003/08/21 19:15:14  dennis
 * After refining the normal vector in FilterFFTds(), the frequency
 * attribute is now recalculated based on the least-squares approx
 * for the planar spacing, rather than the peak finding algorithm
 * using the fft.
 * The maximum and initial distance between the viewer and the origin
 * is now set to 5000 rather than 500 to more nearly give an
 * orthographic projection.
 *
 * Revision 1.13  2003/08/11 22:53:30  dennis
 * Now sets effective positions from grid, after adjusting
 * the grid.
 *
 * Revision 1.12  2003/08/11 22:15:18  dennis
 * First version that uses calibration information.
 *
 * Revision 1.11  2003/07/31 22:43:43  dennis
 * Changed call to WritePeakData to match new method name.
 *
 * Revision 1.10  2003/07/30 22:04:04  dennis
 * Modified to work with the current PeakData object.
 *
 * Revision 1.9  2003/07/29 16:18:05  dennis
 * Now writes L1 and complete information on detector (num rows, cols,
 * height, width, up_vector and base_vector) to peaks file.
 *
 * Revision 1.8  2003/07/28 21:59:02  dennis
 * Added option '-B' to ignore border pixels.  Removed debug print
 * of qxyz vs hkl values, since they can be written to a file
 * (similar to peaks file).
 *
 * Revision 1.7  2003/07/16 22:24:42  dennis
 * Changed output file format to be more like the "peaks" file
 * format.  Now writes the detector position and chi, phi, omega
 * values in the same form as "peaks" file.
 *
 * Revision 1.6  2003/07/14 13:33:20  dennis
 * Added option to print a file listing bins above the specified
 * threshold.  One line is written for each such bin, specifying:
 * run, det_id, h, k, l, row, col, tof, counts, qx, qy, qz for use
 * in Dennis' calibration program.  This is similar to the peaks
 * file, except:
 * 1.Bins forming a peak have not been combined
 * 2.TOF at the bin center is included, rather than wavelength,
 *   since the wavelength depends upon the instrument parameters.
 * 3.(Qx,Qy,Qz), calculated from nominal instrument parameters
 *   are included, for comparison & debugging purposes.
 *
 * Revision 1.5  2003/07/09 21:23:05  dennis
 * Prints file name as it's loading.
 * Builds up FFT DataSet, initially using stricter threshold
 * on the errors, gradually allowing larger errors until at least
 * 10 directions are found.
 *
 * Revision 1.4  2003/06/05 21:43:44  dennis
 * Added option to print lattice parameters.
 * Copy normal from FFT DataSet no longer attempts to further
 * refine the normal.
 *
 * Revision 1.3  2003/06/05 14:42:31  dennis
 * Now allows user to select planes of constant h, k or l either using
 * the FFT DataSet, or by selecting peaks interactively.  Also shows slices
 * of constant h, k or l through the reciprocal lattice, where the value
 * of h, k or l is specified by the user.
 *
 * Revision 1.2  2003/06/04 14:09:46  dennis
 * Now supports multiple runs with more than one area detector.
 * The code that filters the FFTs now checks for normal vectors
 * equaling the negatives of previously found normal vectors
 * and for a d-spacing of 0.
 * Added methods:
 *   ExtractPeaks()
 *   makeVecQTransformers()
 * Removed currently unused methods:
 *   findAverages()
 *   getDataGrid()
 *
 * Revision 1.1  2003/06/03 16:49:14  dennis
 * Initial version of application to calculate orientation matrix and
 * plane slices through reciprocal space.
 *
 */

package DataSetTools.trial;

import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.EditList.*;
import DataSetTools.components.image.*;
import DataSetTools.components.ThreeD.*;
import DataSetTools.components.ui.*;
import DataSetTools.math.*;
import DataSetTools.instruments.*;
import DataSetTools.components.containers.*;
import DataSetTools.util.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import jnt.FFT.*;
import DataSetTools.components.View.TwoD.ImageViewComponent;
import DataSetTools.components.View.*;

public class RecipPlaneView
{
  public static final String NORMAL_ATTRIBUTE    = "Plane Normal";
  public static final String FREQUENCY_ATTRIBUTE = "FFT Peak Frequency";
  public static final String LSQ_ERROR_ATTRIBUTE = "LSQ errors";
  public static final String D_SPACING_ATTRIBUTE = "d-Spacing";
  public static final String Q_SPACING_ATTRIBUTE = "Q-Spacing";
  public static final String PEAK_OBJECTS        = "Peaks_";
  public static final String ORIGIN = " origin ";
  public static final String VEC_1  = " (+)";
  public static final String VEC_2  = " (*)";

  public static final String CONST_H_SLICE = "Const h Slice";
  public static final String CONST_K_SLICE = "Const k Slice";
  public static final String CONST_L_SLICE = "Const l Slice";
  public static final int    SLICE_STEPS = 700;

  private final int DIMENSION = 3;       // set to 4 to allow affine transform
                                         // set to 3 to just use rotation and
                                         // scaling.
  private float SLICE_SIZE_IN_Q = 20;
  private int   FFT_DATA_LENGTH = 512;
  private int   FFT_MASK_LENGTH = 16;
  private int   SLIDER_DEF      = 20;
  private int   SLIDER_MIN      = 1;
  private int   SLIDER_MAX      = 250;
  private float thresh_scale    = 20;
  private float LSQ_THRESHOLD   = 0.10f;

  ImageFrame2 h_frame = null;
  ImageFrame2 k_frame = null;
  ImageFrame2 l_frame = null;

  String path       = null;
  String run_nums   = null;
  String calib_file = null;

  int    runs[];
  String threshold = "";
  String border_size = "";
  int    edge_pix = 0;

  ThreeD_JPanel   vec_Q_space;
  AltAzController controller;
  Color           colors[];
  JSlider         threshold_slider;
  JLabel          q_readout;
  SimpleVectorReadout   origin_vec;
  SimpleVectorReadout   vec_1;
  SimpleVectorReadout   vec_2;

  LatticePlaneUI  h_plane_ui;
  LatticePlaneUI  k_plane_ui;
  LatticePlaneUI  l_plane_ui;

  Vector          vec_q_transformer;
  Vector          all_peaks;

  int             global_obj_index = 0;  // needed to keep the pick ids distinct
  String          file_names[];
  Vector          data_sets;
  Hashtable       calibrations = null;
  Vector3D        all_vectors[];
  double          QR_Rmat[][];           // "R" factor of QR factorization
  double          QR_Umat[][];           // Matrix containing unit vectors U
                                         // describing the matrix Q, from QR
                                         // factorization.
  DataSet         projection_ds;
  DataSet         all_fft_ds;
  DataSet         filtered_fft_ds;

  /* ---------------------------- Constructor ----------------------------- */

  public RecipPlaneView()
  {
    JFrame scene_f = new JFrame("Reciprocal Lattice Plane Viewer");
    JPanel q_panel = new JPanel();

    vec_Q_space = new ThreeD_JPanel();
    controller  = new AltAzController( 45, 45, 2, 5000, 5000 );

    threshold_slider = new JSlider(SLIDER_MIN,SLIDER_MAX,SLIDER_DEF);
    threshold_slider.setMajorTickSpacing(20);
    threshold_slider.setMinorTickSpacing(5);
    threshold_slider.setPaintTicks(true);
    TitledBorder border = new TitledBorder( LineBorder.createBlackLineBorder(),
                                            "Threshold = " + SLIDER_DEF );
    border.setTitleFont( FontUtil.BORDER_FONT );
    threshold_slider.setBorder( border );

    JButton apply_button = new JButton("Apply");
    Box thresh_panel = new Box( BoxLayout.X_AXIS );
    thresh_panel.add( threshold_slider ); 
    thresh_panel.add( apply_button ); 

    q_readout = new JLabel("undefined");

    origin_vec  = new SimpleVectorReadout( ORIGIN );
    origin_vec.setVector( new Vector3D(0,0,0) );
    vec_1  = new SimpleVectorReadout( VEC_1, "Select +" );
    vec_2  = new SimpleVectorReadout( VEC_2, "Select *" );

    border = new TitledBorder( LineBorder.createBlackLineBorder(),"Qxyz");
    border.setTitleFont( FontUtil.BORDER_FONT );
    q_panel.setBorder( border );

    q_readout.setFont( FontUtil.LABEL_FONT );
    q_readout.setHorizontalAlignment( JTextField.CENTER );
    q_readout.setBackground( Color.white );
    q_readout.setForeground( Color.black );
    q_panel.setBackground( Color.white );
    q_panel.setLayout( new GridLayout(1,1) );
    q_panel.add( q_readout );

    Box control_panel = new Box( BoxLayout.Y_AXIS );
    control_panel.add( controller );
    control_panel.add( thresh_panel );
    control_panel.add( q_panel );
    control_panel.add( origin_vec );
    control_panel.add( vec_1 );
    control_panel.add( vec_2 );

    h_plane_ui = new LatticePlaneUI( "h" );
    k_plane_ui = new LatticePlaneUI( "k" );
    l_plane_ui = new LatticePlaneUI( "l" );
    control_panel.add( h_plane_ui );
    control_panel.add( k_plane_ui );
    control_panel.add( l_plane_ui );

    JButton show_lat_param = new JButton( "Lattice Parameters" );
    JButton write_file = new JButton("Write File");
    JPanel button_panel = new JPanel();
    button_panel.setLayout( new GridLayout(2,1) );
    button_panel.add( show_lat_param );
    button_panel.add( write_file );
    control_panel.add( button_panel );

    JPanel filler = new JPanel();
    filler.setPreferredSize( new Dimension( 120, 2000 ) );
    control_panel.add( filler );

    SplitPaneWithState split_pane =
                  new SplitPaneWithState( JSplitPane.HORIZONTAL_SPLIT,
                                          vec_Q_space,
                                          control_panel,
                                          0.75f );

    colors         = IndexColorMaker.getColorTable(
                                IndexColorMaker.HEATED_OBJECT_SCALE, 128 );

    scene_f.getContentPane().add( split_pane );

    vec_Q_space.setBackground( new Color( 20, 150, 90 ) );
    draw_axes(1, vec_Q_space );
    scene_f.setSize(970,750);

    controller.addControlledPanel( vec_Q_space );

    scene_f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    scene_f.setVisible( true );

    apply_button.addActionListener( new ThresholdApplyButtonHandler() );

    threshold_slider.addChangeListener( new ThresholdScaleEventHandler() );
    vec_Q_space.addMouseListener( new ViewMouseInputAdapter() );
    vec_Q_space.addMouseMotionListener( new ViewMouseInputAdapter() );

    ReadoutListener listener = new ReadoutListener();
    origin_vec.addActionListener( listener );
    vec_1.addActionListener( listener );
    vec_2.addActionListener( listener );

    PlaneListener plane_listener = new PlaneListener();
    h_plane_ui.addActionListener( plane_listener );    
    k_plane_ui.addActionListener( plane_listener );    
    l_plane_ui.addActionListener( plane_listener );    

    show_lat_param.addActionListener( new LatticeParameterListener() );
    write_file.addActionListener( new WriteFileListener() );

    Redraw();

    vec_q_transformer = new Vector();
    data_sets = new Vector();
    all_peaks = new Vector();
  }


/* ----------------------------- loadCalibrations -------------------- */
  public void loadCalibrations( String file_name )
  {
    try 
    {
      TextFileReader tfr = new TextFileReader( path + file_name );
      calibrations = new Hashtable();
      tfr.read_line();
      boolean done = false;
      while ( !done && !tfr.end_of_data() )
      {
        int det_num = tfr.read_int();
        float det_A   = tfr.read_float();
        float det_D   = tfr.read_float();
        float l1      = tfr.read_float();
        float t0      = tfr.read_float();
        float x2cm    = tfr.read_float();
        float y2cm    = tfr.read_float();
        float xleft   = tfr.read_float();
        float ylower  = tfr.read_float();
        String calib_name = tfr.read_line();

        float calib[] = { det_A, det_D, l1, t0, x2cm, y2cm, xleft, ylower };
        Integer key = new Integer( det_num );
        if ( calibrations.get( key ) == null )    // new detector
        {
          System.out.println("Using calibration: " +calib_name + 
                             " for " + det_num );
          calibrations.put( key, calib );
        }
        else
          done = true;                           // just use first calibrations
      }
    }
    catch ( Exception e )
    {
      System.out.println("Exception reading calibration file is " + e );
      e.printStackTrace();
      calibrations = null;
    }

    if ( calibrations != null )
    {
      Enumeration e = calibrations.elements();
      while ( e.hasMoreElements() )
        LinearAlgebra.print( (float[])e.nextElement() );
    }
  }


  public void applyCalibration( DataSet ds )
  {
    if ( calibrations == null )
    {
      System.out.println("No calibrations for DataSet " + ds );
      return;
    }
  
    int ids[] = Grid_util.getAreaGridIDs( ds );
    for ( int i = 0; i < ids.length; i++ )
    {
      UniformGrid grid = (UniformGrid)Grid_util.getAreaGrid( ds, ids[i] );
      float cal[] = (float[])calibrations.get( new Integer( ids[i] ) );
      if ( cal == null )
      {
        System.out.println("ERROR: No calibration for detector ID " + ids[i] );
        return;
      }
                                        // First adjust the grid according to
                                        // the calibration information
      int n_rows = grid.num_rows();
      int n_cols = grid.num_cols();
      
      float width  = n_cols * cal[4]/100;
      float height = n_rows * cal[5]/100;

      grid.setWidth( width );
      grid.setHeight( height );

      float xleft   = cal[6]/100;
      float ylower  = cal[7]/100;
      Vector3D base = grid.x_vec();
      Vector3D up   = grid.y_vec();  
      base.normalize();
      up.normalize();

      float xcenter = xleft  + width / 2;
      float ycenter = ylower + height / 2;
      Vector3D center = grid.position();

      System.out.println("ORGINAL CENTER IS: " + center );
      System.out.println("Shift in X is : " + xcenter );
      System.out.println("Shift in Y is : " + ycenter );

      base.multiply( xcenter );
      up.multiply( ycenter );
      center.add( base );
      center.add( up );
      grid.setCenter( center );
      System.out.println("NEW CENTER IS: " + center );
      
      Attribute T0_attribute = new FloatAttribute( Attribute.T0_SHIFT, cal[3] );
      Attribute l1_attribute = new FloatAttribute( Attribute.INITIAL_PATH, 
                                                   cal[2]/100 );
      for ( int row = 1; row <= n_rows; row++ )
        for ( int col = 1; col <= n_cols; col++ )
        {
          grid.getData_entry( row, col ).setAttribute( l1_attribute );
          grid.getData_entry( row, col ).setAttribute( T0_attribute );
        }

      System.out.println ("GRID IS " );
      System.out.println ("" + grid );
                                               // Finally, adjust the 
                                               // effective detector pixel
                                               // positions     
      Grid_util.setEffectivePositions( ds, ids[i] );
    }

  }


 /* ---------------------------- loadFiles --------------------------- */
  public void loadFiles()
  {
    System.out.println("Specified calibration file is : " + calib_file );

    if ( calib_file != null && calib_file.length() > 0 )
      loadCalibrations( calib_file );

    runs = IntList.ToArray( run_nums );
    file_names = new String[ runs.length ];

    for ( int i = 0; i < runs.length; i++ )
     file_names[i] = path+InstrumentType.formIPNSFileName("scd",runs[i]);

    System.out.println("Loading all files....");
    RunfileRetriever rr;
    DataSet ds;
    global_obj_index = 0;
    for ( int count = 0; count < file_names.length; count++ )
    {
      System.out.println("Loading file: " + file_names[count]);
      rr = new RunfileRetriever( file_names[count] );
      ds = rr.getFirstDataSet( Retriever.HISTOGRAM_DATA_SET );
      rr = null;
      if ( ds == null )
        System.out.println("File not found: " + file_names[count]);
      else
      {
        Attribute attr = ds.getAttribute( Attribute.RUN_TITLE );
        System.out.println("Loaded run: " + attr.toString() + " -------------");
        data_sets.addElement(ds);

        applyCalibration( ds );
      }
      ds = null;
    }


    System.out.println("DONE loading DataSets : " + data_sets.size() );
  }


 /* ---------------------------- initialize ------------------------- */

  public void initialize( boolean extract_peaks )
  {
    System.out.println("Making transformers.....");
    makeVecQTransformers();
    System.out.println("DONE");

    if ( extract_peaks )
      ExtractPeaks();

    System.out.println("Getting array of points for peaks....");
    all_vectors = get_data_points();
    System.out.println("DONE");

    System.out.println("Making QR factorization....");
    makeQR_factors();
    System.out.println("DONE");

    System.out.println("Projecting points...");
    projection_ds = ProjectPointsUniformly( all_vectors, 15 );
    System.out.println("DONE");
    ViewManager vm;
//  vm = new ViewManager( projection_ds, IViewManager.IMAGE );

    System.out.println("Doing FFT on all projections....");
    all_fft_ds = FFT( projection_ds );
    all_fft_ds.addIObserver( new FFTListener() );
    System.out.println("DONE");
//  vm = new ViewManager( all_fft_ds, IViewManager.IMAGE );

    System.out.println("Filtering FFTs of all projections....");
    float threshold = 0.5f * LSQ_THRESHOLD;
    boolean done = false;
    while (threshold < 4 * LSQ_THRESHOLD && !done )
    {
      filtered_fft_ds = FilterFFTds( all_fft_ds, threshold );
      if ( filtered_fft_ds.getNum_entries() < 10 )
      {
        threshold *= 1.4142135f;
        System.out.println("WARNING: recalculating FFT since too few found");
        System.out.println("new threshold = " + threshold );
      }
      else
        done = true;
    }

    filtered_fft_ds.addIObserver( new FFTListener() );
    System.out.println("DONE");
    vm = new ViewManager( filtered_fft_ds, IViewManager.IMAGE );
  }

/* ---------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */


/* ------------------------ ExtractPeaks --------------------------- */

  private void ExtractPeaks()
  {
     System.out.println("Applying threshold to extract peaks....");
     all_peaks = new Vector();
     for ( int i = 0; i < vec_q_transformer.size(); i++ )
     {
       IThreeD_Object non_zero_objs[] = getPeaks(i,thresh_scale);
       vec_Q_space.setObjects( PEAK_OBJECTS+i, non_zero_objs);
       System.out.println("Found peaks : " + non_zero_objs.length );
     }
     System.out.println("DONE");
  }


 /* --------------------- makeGoniometerRotationInverse -------------------- */
 /*
  *  Make the cumulative rotation matrix to "unwind" the rotations by chi,
  *  phi and omega, to put the data into one common reference frame for the
  *  crystal.
  */
  private Tran3D makeGoniometerRotationInverse( DataSet ds )
  {
    SampleOrientation orientation =
        (SampleOrientation)ds.getAttributeValue(Attribute.SAMPLE_ORIENTATION);

      return orientation.getGoniometerRotationInverse();
  }


  /* ---------------------- makeVecQTransformers --------------------- */

  private void makeVecQTransformers()
  {
     vec_q_transformer = new Vector();
     for ( int index = 0; index < data_sets.size(); index++ )
     { 
        DataSet ds = (DataSet)data_sets.elementAt(index);
        try
        {
          for ( int i = 1; i < 3; i++ )
          {
            VecQToTOF transformer = new VecQToTOF( ds, i );
            System.out.println("Found Data Grid...................... " );
            System.out.println( transformer.getDataGrid() );
            vec_q_transformer.add( transformer );
          }
        }
        catch (InstantiationError e )
        {
          System.out.println( e );
        }
     }
  }

  /* ------------------------- getPeaks ---------------------------- */
  /*
   *  Get an array of peaks from the specified grid, based on the specified
   *  threshold scale factor.
   *
   *  @param index         The index of the DataGrid in the list. 
   *  @param thresh_scale  The absolute threshold in counts.
   *
   *  @return an array of ThreeD_Objects representing the points above the
   *                      threshold.
   */
  private IThreeD_Object[] getPeaks( int index, float thresh_scale )
  {
      Data  d;
      float t;
      float ys[];
      float times[];
      float cart_coords[];
      Position3D q_pos;
      Color c;
      IThreeD_Object objs[] = null;
      Vector3D pts[] = new Vector3D[1];
      pts[0]         = new Vector3D();
      Attribute attr;
                                            // Assume all runs have the same
                                            // number of detectors in them
                                            // and each detector has a grid.
      int dets_per_run = vec_q_transformer.size()/runs.length; 
      int run_num_index = index/dets_per_run;

      int obj_index = 0;
      VecQToTOF transformer = (VecQToTOF)vec_q_transformer.elementAt(index);
      IDataGrid grid = transformer.getDataGrid();
      d = grid.getData_entry(1,1);
 
      SampleOrientation orientation = null;
      attr = d.getAttribute(Attribute.SAMPLE_ORIENTATION);
      if ( attr != null )
        orientation = (SampleOrientation)attr.getValue();

      float initial_path = 9.378f; 
      attr = d.getAttribute(Attribute.INITIAL_PATH);
      if ( attr != null )
        initial_path = (float)attr.getNumericValue();

      float t0 = 0; 
      attr = d.getAttribute(Attribute.T0_SHIFT);
      if ( attr != null )
        t0 = (float)attr.getNumericValue();

      float det_a = -90;
      attr = d.getAttribute(Attribute.DETECTOR_CEN_ANGLE);
      if ( attr != null )
        det_a = (float)attr.getNumericValue();

      float det_d = .25f;
      attr = d.getAttribute(Attribute.DETECTOR_CEN_DISTANCE);
      if ( attr != null )
        det_d = (float)attr.getNumericValue();

      int n_bins = d.getX_scale().getNum_x() - 1;
      int n_objects = grid.num_rows() * grid.num_cols() * n_bins;
      objs = new IThreeD_Object[n_objects];

      Tran3D combinedR = transformer.getGoniometerRotationInverse();

      if ( edge_pix > grid.num_rows() / 3 )   // can't discard more than 1/3
        edge_pix = grid.num_rows() / 3;       // of the rows and columns

      System.out.println("Discarding " + edge_pix + " edge rows and columns");

      for ( int row = 1+edge_pix; row <= grid.num_rows()-edge_pix; row++ )
        for ( int col = 1+edge_pix; col <= grid.num_cols()-edge_pix; col++ )
        {
          d = grid.getData_entry(row,col);
          Vector3D pos_vec = grid.position(row,col);
          DetectorPosition pos = new DetectorPosition( pos_vec );
          times = d.getX_scale().getXs();
          ys    = d.getY_values();
          for ( int j = 0; j < ys.length; j++ )
          {
            if ( ys[j] > thresh_scale )
            {
              t = (times[j] + times[j+1]) / 2;     // shift by calibrated T)
              q_pos = tof_calc.DiffractometerVecQ(pos,initial_path, t + t0 );

              cart_coords = q_pos.getCartesianCoords();
              pts[0].set( cart_coords[0], cart_coords[1], cart_coords[2] );
              combinedR.apply_to( pts[0], pts[0] );

              int color_index = (int)(ys[j]*30/thresh_scale);
              if ( color_index > 127 )
                color_index = 127;
              c = colors[ color_index ];
              objs[obj_index] = new Ball( pts[0], 0.03f, c );
              objs[obj_index].setPickID( global_obj_index );
              obj_index++;
              global_obj_index++;
              PeakData pd = new PeakData();
              pd.run_num = runs[run_num_index];
              pd.orientation = new IPNS_SCD_SampleOrientation_d(
                                                      orientation.getPhi(), 
                                                      orientation.getChi(), 
                                                      orientation.getOmega());
              pd.l1    = initial_path; 
              float coords[] = grid.position().get();
              Vector3D_d center = new Vector3D_d( coords[0], 
                                                  coords[1], 
                                                  coords[2] );
              coords = grid.x_vec().get();
              Vector3D_d base   = new Vector3D_d( coords[0], 
                                                  coords[1], 
                                                  coords[2] );
              coords = grid.y_vec().get();
              Vector3D_d up     = new Vector3D_d( coords[0], 
                                                  coords[1], 
                                                  coords[2] );
              pd.grid  = new UniformGrid_d( 
                                   grid.ID(), grid.units(),
                                   center, base, up,
                                   grid.width(), grid.height(), grid.depth(),
                                   grid.num_rows(), grid.num_cols() );
              pd.tof = t;
              pd.row = row;
              pd.col = col;
              pd.qx  = pts[0].get()[0];
              pd.qy  = pts[0].get()[1];
              pd.qz  = pts[0].get()[2];
              pd.counts = ys[j];
              pd.run_num = 
                (int)(d.getAttribute(Attribute.RUN_NUM).getNumericValue());
              all_peaks.add( pd );
            }
          }
        }
      IThreeD_Object non_zero_objs[] = new IThreeD_Object[obj_index];
      for ( int i = 0; i < obj_index; i++ )
        non_zero_objs[i] = objs[i];

      return non_zero_objs;
  }


  /* ---------------------- get_data_objects ----------------------- */

  private IThreeD_Object[] get_data_objects()
  {
    IThreeD_Object q_obj[][] = new IThreeD_Object[vec_q_transformer.size()][];

    for ( int i = 0; i < vec_q_transformer.size(); i++ )
    {
      q_obj[i] = vec_Q_space.getObjects( PEAK_OBJECTS+i );
      if ( q_obj[i] != null )
        System.out.println("i = " + i + ", list length = " + q_obj[i].length );
      else
        System.out.println("i = " + i + ", list length = " + 0 );
    }

    int total_length = 0;
    for ( int i = 0; i < vec_q_transformer.size(); i++ )
      if ( q_obj[i] != null )
        total_length += q_obj[i].length;

    IThreeD_Object obj[] = new IThreeD_Object[total_length];
    int start = 0;
    for ( int i = 0; i < vec_q_transformer.size(); i++ )
      if ( q_obj[i] != null )
      {
        System.out.println("i = " + i + ", length = " + q_obj[i].length );
        System.arraycopy( q_obj[i], 0, obj, start, q_obj[i].length );
        start += q_obj[i].length;
      }
    q_obj = null;

    System.out.println( "Total Number of Points = " + total_length );
    return obj;
  }


  /* ----------------------- get_data_points ---------------------------- */
  private Vector3D[] get_data_points()
  {
    IThreeD_Object obj[] = get_data_objects();

    Vector3D all_vectors[] = new Vector3D[ obj.length ];
    for ( int i = 0; i < obj.length; i++ )
      all_vectors[i] = obj[i].position();

    return all_vectors;
  }


  /* -------------------------- ProjectPoints ----------------------- */
 
  public Data ProjectPoints( Vector3D points[], Vector3D normal, int id )
  {
    float dist;
    int   bin;
    float y[] = new float[FFT_DATA_LENGTH];
    XScale scale = new UniformXScale( -SLICE_SIZE_IN_Q,
                                       SLICE_SIZE_IN_Q,
                                       FFT_DATA_LENGTH + 1 );

    Plane3D plane = new Plane3D();
    plane.set( normal, 0 );
    for ( int i = 0; i < points.length; i++ )
    {
      dist = plane.getDistance( points[i] );
      if ( Math.abs(dist) < SLICE_SIZE_IN_Q )          // otherwise, clip it
      {
        bin = Math.round( (FFT_DATA_LENGTH/2-1) * dist / SLICE_SIZE_IN_Q ) +
                           FFT_DATA_LENGTH/2;
        y[bin]++;
      }
    }
    Data d = Data.getInstance( scale, y, id );
    d.setAttribute( new Float1DAttribute( NORMAL_ATTRIBUTE, normal.get() ));

    return d;
  }



/* --------------------------- ProjectPointsUniformly --------------------- */

  public DataSet ProjectPointsUniformly( Vector3D points[], int n_steps )
  {
    DataSetFactory ds_factory = new DataSetFactory(
                                       "Projection parallel to planes" );
    DataSet ds = ds_factory.getDataSet();
    Vector3D normal;
    float    components[] = new float[3];
    Vector3D normals[] = new Vector3D[ n_steps * n_steps * 10 ];
    int n_used = 0;
 
    int  id = 1;
    double phi_step = Math.PI / (2*n_steps);
//    System.out.println( "phi_step = " + phi_step );
    for ( double phi = 0; phi <= (1.000001)*Math.PI/2; phi += phi_step )
    {
      double r = Math.sin(phi);
      int n_theta = (int)Math.round( 4 * r * n_steps );
      double theta_step;
      if ( n_theta == 0 )                        // n = ( 0, 1, 0 );
         theta_step = 7;                         // just use one vector
      else
         theta_step = 2*Math.PI/( n_theta );
//      System.out.println( "theta_step = " + theta_step );
      double last_theta = 2*Math.PI - theta_step/2;

      if ( Math.abs(phi - Math.PI/2) < phi_step/2 )    // use half the equator
        last_theta = Math.PI - theta_step/2;

      for ( double theta = 0; theta < last_theta; theta += theta_step )
      {
        components[0] = (float)(r*Math.cos(theta));
        components[1] = (float)(Math.cos(phi));
        components[2] = (float)(r*Math.sin(theta));
        normal = new Vector3D( components );
        normals[n_used] = normal;
        n_used++;
        Data d = ProjectPoints(points, normal, id);
        ds.addData_entry( d );
//        System.out.println( "id = " + id + " normal = " + normal );
        id++;
      }
    }

/*
    Polymarker normals_used[] = new Polymarker[n_used];
    for ( int i = 0; i < n_used; i++ )
    {
      Vector3D verts[] = new Vector3D[1];
      verts[0] = normals[i];
      normals_used[i] = new Polymarker( verts, Color.cyan );
      normals_used[i].setSize( 4 );
      normals_used[i].setType( Polymarker.BOX );
    }
    vec_Q_space.setObjects( "NORMALS", normals_used );
*/
    return ds;
  }


  /* ---------------------------- FFT -------------------------------- */
  public DataSet FFT( DataSet ds )
  {
    DataSetFactory ds_factory = new DataSetFactory( "FFT of projections" );
    DataSet fft_ds = ds_factory.getDataSet();
   
    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      Data original_d = ds.getData_entry(i);
      Data d = FFT(original_d);
      fft_ds.addData_entry( d );
    }

//    addErrorAttribute( fft_ds );

    return fft_ds;
  }



  /* ---------------------------- FFT -------------------------------- */
 
  public Data FFT( Data d )
  {
     float complex_data[] = new float[2*FFT_DATA_LENGTH];
     float re,
           im;

     float y[] = d.getY_values();
     for ( int j = 0; j < FFT_DATA_LENGTH; j++ )
     {
       complex_data[2*j  ] = y[j];
       complex_data[2*j+1] = 0;
     }

     ComplexFloatFFT fft = new ComplexFloatFFT_Radix2( FFT_DATA_LENGTH );
     fft.transform( complex_data );
     float magnitude[] = new float[FFT_DATA_LENGTH/2];
     for ( int j = 0; j < magnitude.length; j++ )
     {
       re = complex_data[2*j];
       im = complex_data[2*j+1];
       magnitude[j] = (float)(Math.sqrt(re*re+im*im));
     }
  
     XScale scale = new UniformXScale(0, magnitude.length-1, magnitude.length); 
     Data fft_d = Data.getInstance( scale, magnitude, d.getGroup_ID() );

     float peak_center = findFundamental( magnitude );
     fft_d.setAttribute( d.getAttribute( NORMAL_ATTRIBUTE ) );
     fft_d.setAttribute( new FloatAttribute( FREQUENCY_ATTRIBUTE, peak_center));
     return fft_d;
  }


  /* ---------------------------- findFundamental ---------------------- */
  /**
   *  find the fractional channel number that is the center of mass of the
   *  largest peak frequency beyond the "DC term".  This turns out to be the
   *  fundamental frequency in the type of spectra we encounter here.
   */
  public float findFundamental( float fft_array[] )
  {
                               // find the first valley using moving averages
    int n_averaged = 4;
    boolean decreasing = true;
    float sum = 0;
    for ( int i = 0; i < n_averaged; i++ )
      sum += fft_array[i];
    
    float new_sum;
    int i = 0;
    while ( decreasing && (i + n_averaged < fft_array.length) )
    {
      new_sum = sum - fft_array[i] + fft_array[i+n_averaged];

      if ( new_sum >= sum )                 // starting to increase
        decreasing = false;
      else                                  // keep advancing
      {
        sum = new_sum;
        i++;
      }
    }

    int valley = i + n_averaged/2 - 1;     
                                                     // now find the max 
                                                     // beyond the first valley
    float max_value = 0;
    int   max_channel = valley;
    for ( int j = valley; j < fft_array.length; j++ ) 
      if ( fft_array[j] > max_value )
      {
        max_value = fft_array[j];
        max_channel = j;
      }

    float peak_center = 0;
    float peak_area = 0;
    int half_width = valley/3;                        // guess half-peak width
                                                      // based on width of DC
                                                      // term
    for ( int j = max_channel-half_width; j <= max_channel+half_width; j++ )
    { 
      if ( j >= 0 && j < fft_array.length )
      {
        peak_center += fft_array[j] * j;
        peak_area += fft_array[j];
      }
    }
    peak_center /= peak_area;
 
    return peak_center;
  }


/* -------------------------- makeQR_factors ------------------------- */

  public boolean makeQR_factors()
  {
    if ( all_vectors == null || all_vectors.length < DIMENSION )
    {
       System.out.println("ERROR: need >= " + DIMENSION + 
                          " points in makeQR_factors");
       return false;
    }

    QR_Rmat = new double[all_vectors.length][DIMENSION];
    for ( int row = 0; row < all_vectors.length; row++ )
    {
      float coords[] = all_vectors[row].get();
      for ( int col = 0; col < DIMENSION; col++ )
        QR_Rmat[row][col] = coords[col];
    }

    QR_Umat = LinearAlgebra.QR_factorization( QR_Rmat );
    return true;
  }


/* ------------------------- refinePlane ----------------------------- */
/**
 *  Calculate a refined normal vector as a 3 or 4 dimensional vector and
 *  calculate the corresponding least squares errors and d_spacing.
 *
 *  @return If DIMENSION == 3, this returns: {n1,n2,n3,err,d}, and if
 *          DIMENSION == 4, this returns: {n1,n2,n3,n4,err,d}, where n4
 *          represents a shift.  
 */
  public float[] refinePlane(Vector3D normal, float q_step)
  {
    double r[] = new double[all_vectors.length];
    double q_dist;
    for ( int row = 0; row < all_vectors.length; row++ )
    {
      q_dist = normal.dot( all_vectors[row] );
      r[row] = Math.round( q_dist/q_step );
    }

    double err = LinearAlgebra.QR_solve( QR_Rmat, QR_Umat, r );

    float result[] = new float[DIMENSION + 2];
    for ( int i = 0; i < DIMENSION; i++ )
      result[i] = (float)r[i];

    float sigma;
    if ( all_vectors.length > 2 )
      sigma = (float)(err / Math.sqrt( all_vectors.length-1 ));
    else
      sigma = (float)err;
     
    result[DIMENSION] = (float)sigma;              // fill in error values

    Vector3D new_normal = new Vector3D( result );
    float q_spacing  = 1/new_normal.length();
    float d_spacing = (float)(2*Math.PI / q_spacing);
    result[DIMENSION+1] = d_spacing;            // and d-spacing value

    return result;
  }


/* ------------------------- refinePlane ----------------------------- */
/**
 *  Calculate a refined normal vector as a 3 or 4 dimensional vector and
 *  calculate the corresponding least squares errors and d_spacing.
 *
 *  @return If DIMENSION == 3, this returns: {n1,n2,n3,err,d}, and if
 *          DIMENSION == 4, this returns: {n1,n2,n3,n4,err,d}, where n4
 *          represents a shift.  
 */
  public float[] refinePlane(Vector3D normal)
  {
    Data d = ProjectPoints( all_vectors, normal, 1 );
    d = FFT( d );

    float q_spacing;
                                           // get FREQ and calculate q_spacing
    Attribute attr = d.getAttribute( FREQUENCY_ATTRIBUTE );
    if ( attr != null && attr instanceof FloatAttribute )
    {
      float max_chan = ((FloatAttribute)attr).getFloatValue();
      q_spacing = (2*SLICE_SIZE_IN_Q)/(max_chan);
    }
    else
    {
      System.out.println("ERROR: Frequency attribute wrong");
      return null;
    }

    boolean changed = true;
    int     count = 0;
    float   values[] = null;
    Vector3D new_normal = new Vector3D();
    while ( changed && count < 10 )
    {
      values = refinePlane( normal, q_spacing );
      new_normal.set(values);
      if ( new_normal.length() <= 0 )
        System.out.println("************ Warning, 0 length normal ********");

      q_spacing  = 1/new_normal.length();
      new_normal.normalize();
      if ( Math.abs(normal.dot(new_normal)) > 0.99999)
        changed = false;
      else
        normal.set( new_normal );
      count++;
    }

    return values;
  }



/* ------------------------- FilterFFTds ------------------------------- */
/*
 * Go through the FFT ds refine the normals and only keep the FFT's for 
 * which the refined normals are distinct.
 */
  public DataSet FilterFFTds( DataSet fft_ds, float err_threshold )
  {
    DataSet new_ds = fft_ds.empty_clone();
    new_ds.setTitle("Filtered FFT DataSet");
    Vector   normals = new Vector();

    int      n_data = fft_ds.getNum_entries();
    Object   value;
    Vector3D normal;
    float    q_spacing;
    float    d_spacing;
    float    sigma;
    for ( int i = 0; i < n_data; i++ )
    {
      Data d = fft_ds.getData_entry( i );
                                            // get normal
      value = d.getAttributeValue( NORMAL_ATTRIBUTE );
      normal = new Vector3D( (float[])value );

                                            // get FREQ and calculate q_spacing
      Attribute attr = d.getAttribute( FREQUENCY_ATTRIBUTE );
      if ( attr != null && attr instanceof FloatAttribute )
      {
        float max_chan = ((FloatAttribute)attr).getFloatValue();
        q_spacing = (2*SLICE_SIZE_IN_Q)/(max_chan);
      }
      else
      {
        System.out.println("ERROR: Frequency attribute wrong");
        return fft_ds;
      }
                                             // refine the plane for this normal
      boolean changed = true;
      int     count = 0;
      float   values[] = null;
      Vector3D new_normal = new Vector3D();
      while ( changed && count < 10 )
      {
        values     = refinePlane( normal, q_spacing );
        new_normal.set(values);
        if ( new_normal.length() <= 0 )
          System.out.println("************ Warning, 0 length normal ********");

        q_spacing  = 1/new_normal.length();
        new_normal.normalize();
        if ( Math.abs(normal.dot(new_normal)) > 0.99999)
          changed = false;
        else
          normal.set( new_normal );
        count++;
      }
/*
      if ( !changed )
        System.out.println("Stable after " + count );
      else
        System.out.println("Failed to stablize " + count );
*/
      d_spacing = (float)(2*Math.PI / q_spacing);
      sigma = values[DIMENSION];

      if ( sigma < err_threshold && d_spacing > 0 )
      { 
        new_normal.normalize();

        boolean duplicate = false;
        int j = 0;
        float old_vals[];
        float new_vals[];
        while (!duplicate && j < normals.size())
        {
          Vector3D saved_vec = (Vector3D)(normals.elementAt(j));
          old_vals = saved_vec.get();
          new_vals = new_normal.get();
          duplicate = true;
          for ( int k = 0; k < 3; k++ )
            if ( Math.abs( old_vals[k] - new_vals[k] ) > 0.000001f )
              duplicate = false;

          if ( !duplicate )            // try negative value
          {
            duplicate = true;
            for ( int k = 0; k < 3; k++ )
              if ( Math.abs( old_vals[k] + new_vals[k] ) > 0.000001f )
                duplicate = false;
          }
          j++;
        }
        if ( !duplicate )
        {  
          normals.addElement( new_normal );
          Data new_d = ProjectPoints( all_vectors, new_normal, normals.size());
          Data fft_d = FFT(new_d);
          fft_d.setAttribute( 
                    new Float1DAttribute( NORMAL_ATTRIBUTE, new_normal.get()));
          fft_d.setAttribute( 
                    new FloatAttribute( Q_SPACING_ATTRIBUTE, q_spacing ) );
          fft_d.setAttribute( 
                    new FloatAttribute( D_SPACING_ATTRIBUTE, d_spacing ) );
          fft_d.setAttribute( 
                    new FloatAttribute( LSQ_ERROR_ATTRIBUTE, sigma ) );
                                       // now reset the frequency attribute
                                       // based on refined normal & q values
          float max_chan = 2 * SLICE_SIZE_IN_Q / q_spacing;
          fft_d.setAttribute( 
                    new FloatAttribute( FREQUENCY_ATTRIBUTE, max_chan ) );
          new_ds.addData_entry( fft_d );
        }
      }
    } 

    Operator op = new DataSetSort( new_ds, D_SPACING_ATTRIBUTE, true, false );
    op.getResult();
    System.out.println("Filtered FFT ds has : " + normals.size() );
    return new_ds;
  }


  /* ------------------------- parseArgs ----------------------------- */
  /**
   *  Parse a list of command line arguments to extract values for the
   *  the data directories and run numbers  The commands supported are
   *  -D -R and -H
   *
   *  @param args  Array of strings from the command line, containing
   *               command characters and arguments.
   */
   private void parseArgs( String args[] )
   {
     if ( args == null || args.length < 2        ||
          StringUtil.commandPresent("-h", args ) ||
          StringUtil.commandPresent("-H", args )  )
     {
       showUsage();
       System.exit(0);
     }

     path       = StringUtil.getCommand( 1, "-D", args );
     run_nums   = StringUtil.getCommand( 1, "-R", args );
     calib_file = StringUtil.getCommand( 1, "-C", args );

     if ( path.length() <= 0 || run_nums.length() <= 0 )
     {
       showUsage();
       System.exit(0);
     }

     path = path + "/";

     border_size = StringUtil.getCommand( 1, "-B", args );
     threshold   = StringUtil.getCommand( 1, "-T", args );
   }


  /* ----------------------- showUsage ----------------------- */
  /**
   *  Print list of supported commands.
   */
   private void showUsage()
   {
    System.out.println(
       "  -D<dir name>  specifies directory for data files (required)");
    System.out.println(
       "  -R<list of run numbers> specify runs to load (required)");
    System.out.println(
       "  -C<calibration file name> specify name of calibration file");
    System.out.println(
       "  -T<relative threshold> specify scale factor to apply to the");
    System.out.println(
       "                         default value for the threshold.");
    System.out.println(
       "                        (values < 1 increase number of points shown)");
    System.out.println(
       "                        (values > 1 decrease number of points shown)");
    System.out.println(
       "  -B<border size> specifies number of border rows and column to skip");
    System.out.println("  -H,-h  print this message");
   }


  /* ------------------------- Redraw ------------------------------ */
  /*
   *  Redraw the current origin and basis vectors, and set the new
   *  values for the cosines of the angles between the basis vectors.
   */
  private void Redraw()
  {
    Vector3D origin = origin_vec.getVector();
    MarkPoint( origin, ORIGIN, Polymarker.BOX );

    Vector3D first_vec = vec_1.getVector();
    if ( first_vec == null )
    {
      vec_Q_space.removeObjects( VEC_1 );
      vec_Q_space.removeObjects( VEC_1+"LINE" );
    }
    else
    {
      BasisVector( first_vec, VEC_1+"LINE", Color.yellow );
      first_vec.add( origin );
      MarkPoint( first_vec, VEC_1, Polymarker.PLUS );
    }

    Vector3D second_vec = vec_2.getVector();
    if ( second_vec == null )
    {
      vec_Q_space.removeObjects( VEC_2 );
      vec_Q_space.removeObjects( VEC_2+"LINE" );
    }
    else
    {
      BasisVector( second_vec, VEC_2+"LINE", Color.yellow );
      second_vec.add( origin );
      MarkPoint( second_vec, VEC_2, Polymarker.STAR );
    }

    vec_Q_space.repaint();
  }

  /* ------------------------------ draw_axes ----------------------------- */
  /*
   *  Draw a simple set of red, green and blue lines to represent the
   *  coordinate system.
   */
  private  void draw_axes( float length, ThreeD_JPanel threeD_panel  )
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

    threeD_panel.setObjects( "AXES", objects );
  }

  /* ------------------------- MarkPoint --------------------------- */
  /*
   * Mark the specified point with a polymarker of the specified type.
   *
   *  @param  vec   the point to mark
   *  @param  name  the name of the Polymarker, so that it can be removed
   *                when the point is cleared
   *  @param  type  the type of marker to draw
   */
  private void MarkPoint( Vector3D vec, String name, int type )
  {
    IThreeD_Object objects[] = new IThreeD_Object[ 1 ];
    Vector3D points[] = new Vector3D[1];

    points[0] = vec;
    Polymarker marker = new Polymarker( points, Color.yellow );
    marker.setSize( 10 );
    marker.setType( type );
    objects[0] = marker;

    vec_Q_space.setObjects( name, objects );
  }

  /* ------------------------- BasisVector --------------------------- */
  /*
   * Draw a segmented line from the "origin" point in the direction given
   * by the specified vector.
   *
   * @param  vec    The vector offset from the origin.  This must be a
   *                valid non-zero vector.
   * @param  name   the name for the line object, so that it can be
   *                removed when it is not needed.
   * @param  color  the color to draw the line with
   */
  private void BasisVector( Vector3D vec, String name, Color color )
  {
    final int N_SEGMENTS = 100;

    IThreeD_Object objects[] = new IThreeD_Object[ N_SEGMENTS ];

    Vector3D points[] = new Vector3D[2];
    points[0] = new Vector3D();
    points[1] = new Vector3D();

    float length = vec.length();
    float step = length/N_SEGMENTS;
    Vector3D delta_v = new Vector3D( vec );
    if ( length > 0 )
      delta_v.multiply( step/length );

    Vector3D diff = new Vector3D();
    Polyline line;

    for ( int i = 0; i < N_SEGMENTS; i++ )
    {
      diff.set( delta_v );
      diff.multiply( i ); 
      points[0].set( origin_vec.getVector() );
      points[0].add( diff );

      diff.set( delta_v );
      diff.multiply( i+1 ); 
      points[1].set( origin_vec.getVector() );
      points[1].add( diff );

      line = new Polyline( points, color );
      objects[i] = line;
    }

    vec_Q_space.setObjects( name, objects );
  }



  /* ------------------------- make_slice ---------------------------- */

  private float[][] make_slice( Vector3D origin,
                                Vector3D normal,
                                Vector3D base,
                                Vector3D up    )
  {
    System.out.println("Start of make_slice......");
    if( origin == null || base == null || up == null )
      return null;

    int n_rows = SLICE_STEPS;
    int n_cols = SLICE_STEPS;
    float image[][] = new float[n_rows][n_cols];

    float size = 2*SLICE_SIZE_IN_Q;
                                             // make two orthonormal vectors
    Vector3D base2 = new Vector3D();
    base2.cross( normal, base );
    base2.normalize();

    Vector3D base1 = new Vector3D();
    base1.cross( normal, base2 );
    base1.normalize();   

    float b1[] = base1.get();
    float b2[] = base2.get();
    System.out.println("Origin = " + origin );
    System.out.println("base1  = " + base1 );
    System.out.println("base2  = " + base2 );

    float orig[] = origin.get();
    Vector3D q = new Vector3D();
    float step = size/n_rows;
    float d_row, d_col;
    float value;
    int   n_non_zero;
                                             // for each point in the plane...
    VecQToTOF transformer;
    float sum;
    for ( int row = 0; row < n_rows; row++ )
      for ( int col = 0; col < n_cols; col++ )
      {
        d_row = (n_rows/2 - row)*step;
        d_col = (col - n_cols/2)*step;

        q.set( orig[0] + d_row * b2[0] + d_col * b1[0],
               orig[1] + d_row * b2[1] + d_col * b1[1],
               orig[2] + d_row * b2[2] + d_col * b1[2]  );

         sum = 0;
         n_non_zero = 0;
         for ( int i = 0; i < vec_q_transformer.size(); i++ )
         {
           transformer = (VecQToTOF)(vec_q_transformer.elementAt(i));
           value = transformer.intensityAtQ( q );
           if ( value != 0 )
           {
             sum += value;
             n_non_zero++;
           }
         }
         if ( n_non_zero > 0 )
           image[row][col] = sum / n_non_zero;
         else
           image[row][col] = 0;
      }
    System.out.println("DONE");
    return image;
  }


/* ------------------------- showLatticeParameters ----------------------- */

private void showLatticeParameters( Vector3D a, Vector3D b, Vector3D c )
{
  Vector3D a_normal = new Vector3D( a );
  a_normal.normalize();

  Vector3D b_normal = new Vector3D( b );
  b_normal.normalize();

  Vector3D c_normal = new Vector3D( c );
  c_normal.normalize();

  double alpha = (180/Math.PI) * Math.acos( b_normal.dot( c_normal ) );
  double beta  = (180/Math.PI) * Math.acos( a_normal.dot( c_normal ) );
  double gamma = (180/Math.PI) * Math.acos( a_normal.dot( b_normal ) );

  Vector3D temp = new Vector3D();
  temp.cross( a, b );
  float volume = Math.abs(c.dot( temp ));

  System.out.println("-------------------------------------------------------");
  System.out.println("Lattice Parameters:");
  System.out.println("");
  System.out.println( "  "  + Format.real( a.length(), 5, 5 ) +
                      "  "  + Format.real( b.length(), 5, 5 ) +
                      "  "  + Format.real( c.length(), 5, 5 ) +
                      " : " + Format.real( alpha, 5, 5 )      +
                      "  "  + Format.real( beta, 5, 5 )       +
                      "  "  + Format.real( gamma, 5, 5 )      + 
                      " : " + Format.real( volume, 5, 5 )     );
  System.out.println("");
  System.out.println("-------------------------------------------------------");
}

/* --------------------------------------------------------------------------
 *
 *  PRIVATE CLASSES
 *
 */


/* ------------------------- ViewMouseInputAdapter ----------------------- */
/**
 *  Handles mouse events for picking data points displayed.
 */
private class ViewMouseInputAdapter extends MouseInputAdapter
{
   int last_index = IThreeD_Object.INVALID_PICK_ID;

   public void mousePressed( MouseEvent e )
   {
     handle_event(e);
   }

   public void mouseDragged( MouseEvent e )
   {
     handle_event(e);
   }

   private void handle_event( MouseEvent e )
   {
     Point pt = e.getPoint();
     int index = vec_Q_space.pickID( e.getX(), e.getY(), 5 );
     if ( index != last_index )
     {
       last_index = index;
       if ( index != IThreeD_Object.INVALID_PICK_ID )
       {
         IThreeD_Object obj = vec_Q_space.pickedObject();
         float coords[] = obj.position().get();
         String result = new String( Format.real( coords[0], 6, 3 ) );
         result += ", " + Format.real( coords[1], 6, 3 );
         result += ", " + Format.real( coords[2], 6, 3 );
         q_readout.setText( result );
       }
       else
         q_readout.setText( "undefined" );
     }
   }
}

/* -------------------- ThresholdScaleEventHandler ------------------- */

private class ThresholdScaleEventHandler implements ChangeListener
{
  public void stateChanged(ChangeEvent e)
  {
    JSlider slider = (JSlider)e.getSource();

                              // set image log scale when slider stops moving
    if ( !slider.getValueIsAdjusting() )
    {
      int value = slider.getValue();
      TitledBorder border = new TitledBorder(LineBorder.createBlackLineBorder(),
                                             "Threshold = " + value );
      border.setTitleFont( FontUtil.BORDER_FONT );
      threshold_slider.setBorder( border );

      thresh_scale = value;

      ExtractPeaks();

      Redraw();
    }
  }
}


/* ----------------------- ThresholdApplyButtonHandler ------------------- */

private class ThresholdApplyButtonHandler implements ActionListener
{
  public void actionPerformed( ActionEvent e )
  {
    String action  = e.getActionCommand();
    System.out.println("Button pressed : " + action );
    initialize( false );      
  }
}


/* --------------------------- WriteFileListener ------------------------ */

private class WriteFileListener implements ActionListener
{
  public void actionPerformed( ActionEvent e )
  {
    PeakData.WritePeakData( all_peaks, "fft_peaks.dat" );
  }
}



/* ---------------------- LatticeParameterListener ------------------------ */

private class LatticeParameterListener implements ActionListener
{
  public void actionPerformed( ActionEvent e )
  {
    float h_vals[] = h_plane_ui.get_normal(); 
    float k_vals[] = k_plane_ui.get_normal(); 
    float l_vals[] = l_plane_ui.get_normal(); 

    if ( h_vals == null || k_vals == null || l_vals == null )
    {
      System.out.println("ERROR: must have h,k,l normals set");
      return;
    }

    Vector3D a = new Vector3D( h_vals );
    Vector3D b = new Vector3D( k_vals );
    Vector3D c = new Vector3D( l_vals );

    a.multiply( h_plane_ui.get_d_spacing() ); 
    b.multiply( k_plane_ui.get_d_spacing() ); 
    c.multiply( l_plane_ui.get_d_spacing() ); 
    
    showLatticeParameters( a, b, c );

    Vector3D q;
    float mag_a = (float)(2*Math.PI/a.length());
    float mag_b = (float)(2*Math.PI/b.length());
    float mag_c = (float)(2*Math.PI/c.length());
    a.normalize();
    b.normalize();
    c.normalize();
    for ( int i = 0; i < all_peaks.size(); i++ )
    {
       PeakData pd = (PeakData)all_peaks.elementAt(i);
       q = new Vector3D( (float)pd.qx, (float)pd.qy, (float)pd.qz );
       float h = q.dot(a)/mag_a;
       float k = q.dot(b)/mag_b;
       float l = q.dot(c)/mag_c;
//     System.out.print  ("q = "+ pd.qx + ", " + pd.qy + ", " + pd.qz );
//     System.out.println(" hkl = "+ h + ", " + k + ", " + l ); 
       pd.h = h;
       pd.k = k;
       pd.l = l;
    }    
  }
}


/* ------------------------- ReadoutListener ----------------------- */
/**
 *  Class to handle user selection and scale factor change events
 *  from the SimpleVectorReadout components for origin, vec_1 and vec_2
 */
private class ReadoutListener implements ActionListener
{
   public void actionPerformed( ActionEvent e )
   {
     String              action  = e.getActionCommand();
     SimpleVectorReadout readout = (SimpleVectorReadout)e.getSource();

     if ( action.startsWith( "Select" ) )
     {
       IThreeD_Object obj = vec_Q_space.pickedObject();
       if ( obj == null )
       {
         if ( readout.getTitle().equals(ORIGIN) )      // origin defaults to
           readout.setVector( new Vector3D(0,0,0) );   // (0,0,0)
         else
           readout.setVector( null );
       }
       else
       {
         if ( readout.getTitle().equals(ORIGIN) )
           readout.setVector( obj.position() );        // just move the origin

         else                                          // get vector relative
         {                                             // to the origin
           Vector3D vec = new Vector3D( obj.position() );
           Vector3D start = new Vector3D( origin_vec.getVector() );
           start.multiply( -1 );
           vec.add( start );
           readout.setVector( vec );
         }
       } 
     }

     Redraw();
   }
 }


/* ------------------------- PlaneListener ----------------------- */
/**
 *  Class to handle user selection of plane from vectors or FFT DataSet
 */
private class PlaneListener implements ActionListener
{
   public void actionPerformed( ActionEvent e )
   {
     String         action  = e.getActionCommand();
     System.out.println("Action command is : " + action );

     if ( action.equals(LatticePlaneUI.USER_SET) )
     {
       LatticePlaneUI plane_ui = (LatticePlaneUI)e.getSource();
       Vector3D origin = origin_vec.getVector();
       Vector3D v1     = vec_1.getVector();
       Vector3D v2     = vec_2.getVector();
       if ( origin == null || v1 == null || v2 == null )
         return;
 
       Vector3D e1 = new Vector3D( v1 );
//       e1.subtract( origin );
 
       Vector3D e2 = new Vector3D( v2 );
//       e2.subtract( origin );

       Vector3D normal = new Vector3D();
       normal.cross( e1, e2 );
       normal.normalize();
       System.out.println("USER NORMAL = " + normal );

       float[] value = refinePlane( normal );

       float sigma = value[DIMENSION];
       float d_spacing = value[DIMENSION+1];

       normal.set(value);
       float length = normal.length();

       for ( int i = 0; i < 3; i++ )
        value[i] /= length;

       if ( DIMENSION == 3 )
         value[DIMENSION] = 0;

       plane_ui.set_normal( value );
       plane_ui.set_d_sigma( d_spacing, sigma );
     }
     else if ( action.equals( LatticePlaneUI.FFT_SET) )
     {
       LatticePlaneUI plane_ui = (LatticePlaneUI)e.getSource();
       int index = filtered_fft_ds.getPointedAtIndex();
       if ( index < 0 )
         return;
       Data d = filtered_fft_ds.getData_entry(index);

       float value[] = (float[])d.getAttributeValue( NORMAL_ATTRIBUTE );

       Attribute attr = d.getAttribute( D_SPACING_ATTRIBUTE );
       float d_spacing = (float)attr.getNumericValue();

       attr = d.getAttribute( LSQ_ERROR_ATTRIBUTE );
       float sigma = (float)attr.getNumericValue();

       if ( DIMENSION == 3 )
         value[DIMENSION] = 0;

       plane_ui.set_normal( value );      
       plane_ui.set_d_sigma( d_spacing, sigma );
     }
     else
     {
       LatticePlaneUI plane_ui = (LatticePlaneUI)e.getSource();
       float miller_index = plane_ui.get_miller_index();
       float d_spacing    = plane_ui.get_d_spacing();

       System.out.println("Redraw using miller index : " + miller_index );

       Vector3D normal = new Vector3D( plane_ui.get_normal() );
       if ( normal == null || normal.length() < 0.99 )
         return;

       String title      = null;
       ImageFrame2 frame = null;
       Vector3D    base  = null;
       Vector3D    up    = null;

       if ( plane_ui == h_plane_ui )
       {
         base = new Vector3D( k_plane_ui.get_normal() );
         up   = new Vector3D( l_plane_ui.get_normal() );
         if ( base == null || base.length() < 0.99 ||
                up == null || up.length()   < 0.99 )
           return;
         frame = h_frame;
         title = "h = " + miller_index;
       }
       else if ( plane_ui == k_plane_ui )
       {
         base = new Vector3D( l_plane_ui.get_normal() );
         up   = new Vector3D( h_plane_ui.get_normal() );
         if ( base == null || base.length() < 0.99 ||
                up == null || up.length()   < 0.99 )
           return;
         frame = k_frame;
         title = "k = " + miller_index;
       }
       else  // plane_ui == l_plane_ui
       {
         base = new Vector3D( h_plane_ui.get_normal() );
         up   = new Vector3D( k_plane_ui.get_normal() );
         if ( base == null || base.length() < 0.99 ||
                up == null || up.length()   < 0.99 )
           return;
         frame = l_frame;
         title = "l = " + miller_index;
       }

       Vector3D origin = new Vector3D( normal );
       origin.multiply((float)(miller_index * Math.PI * 2/d_spacing) );
       float image[][] = make_slice( origin, normal, base, up );
       VirtualArray2D va2d = new VirtualArray2D( image );
       va2d.setTitle( title );
       if ( frame == null )
         frame = new ImageFrame2( va2d );
       else 
         frame.setData( va2d );

       if ( plane_ui == h_plane_ui )
         h_frame = frame;
       else if ( plane_ui == k_plane_ui )
         k_frame = frame;
       else  // plane_ui == l_plane_ui
         l_frame = frame;
     }
   }
 }


/* ------------------------ FFTListener ---------------------------- */
/**
 *  Listen to the FFT DataSet and record the ID & Normal information
 *  from the "Pointed At" Data block.
 */
private class FFTListener implements IObserver
{
  public void update( Object observed_object, Object reason )
  {
    float q_spacing = 1;

    if ( reason instanceof String && observed_object instanceof DataSet )
     if ( reason.equals( IObserver.POINTED_AT_CHANGED ) )
     {
       System.out.println("---------------------------------------------");
       int index = ((DataSet)observed_object).getPointedAtIndex();
       Data d = ((DataSet)observed_object).getData_entry(index);
       Object value = d.getAttributeValue( NORMAL_ATTRIBUTE );
       if ( value == null )
         System.out.println( "ERROR: Missing normal " );

       if ( value instanceof float[] )
       {
         Vector3D normal = new Vector3D( (float[])value );
//         System.out.println( "Normal for this Data block is " + normal );

         Polyline normal_line[] = new Polyline[1];
         Vector3D verts[] = new Vector3D[2];
         verts[0] = origin_vec.getVector();
         verts[1] = origin_vec.getVector();
         verts[1].add(normal);

         normal_line[0] = new Polyline( verts, Color.cyan );
         vec_Q_space.setObjects( "Selected Normal", normal_line );

         Attribute attr = d.getAttribute( FREQUENCY_ATTRIBUTE );
         if ( attr instanceof FloatAttribute )
         {
           float max_chan = ((FloatAttribute)attr).getFloatValue();
           q_spacing = (2*SLICE_SIZE_IN_Q)/(max_chan);
//           System.out.println("old Q-spacing = " + q_spacing );
//           System.out.println("old d-spacing = " + 2*Math.PI/q_spacing );
           Vector3D poly_verts[] = new Vector3D[15];
           poly_verts[0] = new Vector3D( origin_vec.getVector() );
           Vector3D step_vec = new Vector3D( normal );
           step_vec.multiply( q_spacing );
           for ( int i = 1; i < poly_verts.length; i++ )
           {
             poly_verts[i] = new Vector3D( poly_verts[i-1] );
             poly_verts[i].add( step_vec );
           }
           Polymarker plane_marks[] = new Polymarker[1];
           plane_marks[0]  = new Polymarker( poly_verts, Color.cyan );
           plane_marks[0].setSize( 4 );
           plane_marks[0].setType( Polymarker.BOX );
           vec_Q_space.setObjects( "Selected Plane Spacing", plane_marks );

           float values[] = refinePlane( normal, q_spacing );
//           System.out.print("Refined normal values : " );
//           for ( int i = 0; i < values.length; i++ )
//             System.out.print(" " + values[i] );
//             System.out.println();
           Vector3D new_normal = new Vector3D( values );
           new_normal.normalize();
           System.out.println("Normalized new_normal = " + new_normal );

//           attr = d.getAttribute( LSQ_ERROR_ATTRIBUTE );
//           if ( attr != null )
//             System.out.println("LSQ ERROR = " + attr.getNumericValue() );
         }
         else
         {
           vec_Q_space.removeObjects( "Selected Plane Spacing" );
//           System.out.println("Removing plane marks" );
         }

         Redraw();
       }
     }
  }
}


  /* ---------------------------- main ---------------------------------- */
  public static void main( String args[] )
  {
    RecipPlaneView viewer = new RecipPlaneView();

    viewer.parseArgs( args );
    if ( viewer.threshold.length() > 0 )
      try
      {
        viewer.thresh_scale = (new Float(viewer.threshold)).floatValue();
        viewer.thresh_scale = Math.abs( viewer.thresh_scale );
        if ( viewer.thresh_scale == 0 )
        {
          viewer.thresh_scale = 20;
          System.out.println("threshold of 0 ignored, using default...");
        }
      }
      catch ( Exception e )
      {
        System.out.println("Invalid threshold value, ignored");
      }

    if ( viewer.border_size.length() > 0 )
      try
      {
        viewer.edge_pix = (new Integer(viewer.border_size)).intValue();
        if ( viewer.edge_pix < 0 )
          throw new Exception("");
      }
      catch ( Exception e )
      {
        System.out.println("Invalid border size, ignored");
      }

    viewer.loadFiles(); 
    viewer.initialize( true ); 
  }

}
