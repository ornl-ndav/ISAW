/*
 * File:  SCDRecipLat.java 
 *
 * Copyright (C) 2002, Dennis Mikkelson
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
 * Revision 1.10  2003/01/08 23:21:21  dennis
 * Slice size in pixels is now a named constant.  Slice is now properly
 * centered in ImageFrame.
 *
 * Revision 1.9  2003/01/08 21:52:29  dennis
 * Now sets runfile retriever and DataSet to null when no longer used,
 * to allow earlier garbage collection.  Also, makes 400x400 image instead
 * of 500x500 image, and checks for null vectors before drawing a slice.
 *
 * Revision 1.8  2003/01/08 17:45:30  dennis
 * Added option to show slice through Qxyz space.  This is currently just
 * a "proof of concept", and works only for "old" SCD data.
 *
 *
 * Revision 1.7  2002/11/27 23:23:30  pfpeterson
 * standardized header
 *
 * Revision 1.6  2002/11/08 23:09:29  dennis
 * Set background back to dark gray
 *
 * Revision 1.5  2002/11/01 00:12:04  dennis
 * Improved documentation, increased marker size to 10 and made pick IDs
 * unique through out all spectra.
 *
 * Revision 1.4  2002/10/31 23:22:31  dennis
 * Extensive revision, includes:
 *  1) readout of vector Qxyz, when a data point is pointed at
 *  2) selection of "origin" and three other points to specify
 *     basis vectors
 *  3) computation of angles between basis vectors
 *
 * Revision 1.3  2002/10/29 22:20:47  dennis
 * Now uses 3D ball objects (drawn as squares) to represent the
 * data points.  Also, the threshold for peaks in a time slice is
 * based on the 10*average intensity in the time slice.
 *
 * Revision 1.2  2002/08/05 19:06:04  pfpeterson
 * Set the package so it can be called when inside a jar.
 *
 * Revision 1.1  2002/08/05 05:38:40  dennis
 * Rudimentary command line version of SCD reciprocal lattice viewer.
 *
 */

package DataSetTools.trial;

import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.dataset.*;
import DataSetTools.components.image.*;
import DataSetTools.components.ThreeD.*;
import DataSetTools.components.ui.*;
import DataSetTools.math.*;
import DataSetTools.instruments.*;
import DataSetTools.components.containers.*;
import DataSetTools.util.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

/** 
 *  This class reads through a sequence of SCD run files and constructs
 *  a basic view of the peaks in 3D "Q" space.
 */

public class SCDRecipLat
{
  public static final String ORIGIN = " origin ";
  public static final String A_STAR = " a* (+)";
  public static final String B_STAR = " b* (*)";
  public static final String C_STAR = " c* (X)";

  public static final String A_B_SLICE = "a*<->b* Slice";
  public static final String B_C_SLICE = "b*<->c* Slice";
  public static final String C_A_SLICE = "c*<->a* Slice";
  public static final int    SLICE_SIZE = 400;

  ImageFrame a_b_frame = null;
  ImageFrame b_c_frame = null;
  ImageFrame c_a_frame = null;

  String path = null;
  String run_nums = null;
  String threshold = "";

  ThreeD_JPanel   vec_Q_space;
  AltAzController controller;
  Color           colors[];
  JLabel          q_readout;
  VectorReadout   origin_vec;
  VectorReadout   a_star_vec;
  VectorReadout   b_star_vec;
  VectorReadout   c_star_vec;

  JLabel          a_star_b_star;
  JLabel          b_star_c_star;
  JLabel          c_star_a_star;

  Vector          vec_q_transformer;

  int             global_obj_index = 0;  // needed to keep the pick ids distinct

  /* ---------------------------- Constructor ----------------------------- */

  public SCDRecipLat()
  {
    JFrame scene_f = new JFrame("Reciprocal Lattice Viewer");
    JPanel q_panel = new JPanel();

    vec_Q_space = new ThreeD_JPanel();
    controller  = new AltAzController();
    q_readout   = new JLabel("undefined");

    origin_vec  = new VectorReadout( ORIGIN );
    origin_vec.setVector( new Vector3D(0,0,0) );
    a_star_vec  = new VectorReadout( A_STAR, "Select +" );
    b_star_vec  = new VectorReadout( B_STAR, "Select *" );
    c_star_vec  = new VectorReadout( C_STAR, "Select X" );

    TitledBorder border = new TitledBorder(
                             LineBorder.createBlackLineBorder(),"Qxyz");
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
    control_panel.add( q_panel );
    control_panel.add( origin_vec );
    control_panel.add( a_star_vec );
    control_panel.add( b_star_vec );
    control_panel.add( c_star_vec );

    JPanel angle_panel = new JPanel();
    border = new TitledBorder( LineBorder.createBlackLineBorder(),"Angles");
    border.setTitleFont( FontUtil.BORDER_FONT );
    angle_panel.setBorder( border );
    angle_panel.setBackground( Color.white );
    angle_panel.setForeground( Color.black );
    angle_panel.setLayout( new GridLayout(3,1) );
    a_star_b_star = new JLabel("a*<->b* : undefined");
    b_star_c_star = new JLabel("b*<->c* : undefined");
    c_star_a_star = new JLabel("c*<->a* : undefined");
    a_star_b_star.setFont( FontUtil.LABEL_FONT );
    b_star_c_star.setFont( FontUtil.LABEL_FONT );
    c_star_a_star.setFont( FontUtil.LABEL_FONT );
    a_star_b_star.setHorizontalAlignment( JTextField.CENTER );
    b_star_c_star.setHorizontalAlignment( JTextField.CENTER );
    c_star_a_star.setHorizontalAlignment( JTextField.CENTER );
    a_star_b_star.setForeground( Color.black );
    b_star_c_star.setForeground( Color.black );
    c_star_a_star.setForeground( Color.black );
    angle_panel.add( a_star_b_star );
    angle_panel.add( b_star_c_star );
    angle_panel.add( c_star_a_star );
    control_panel.add( angle_panel );

    JPanel button_panel = new JPanel();
    button_panel.setLayout( new GridLayout(3,1) );
    JButton a_b_button = new JButton(A_B_SLICE);
    JButton b_c_button = new JButton(B_C_SLICE);
    JButton c_a_button = new JButton(C_A_SLICE);
    button_panel.add( a_b_button );
    button_panel.add( b_c_button );
    button_panel.add( c_a_button );
    SliceButtonListener slice_listener = new SliceButtonListener();
    a_b_button.addActionListener( slice_listener );
    b_c_button.addActionListener( slice_listener );
    c_a_button.addActionListener( slice_listener );
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

    vec_Q_space.setBackground( new Color( 90, 90, 90 ) );
    draw_axes(1, vec_Q_space );
    scene_f.setSize(900,700);

    controller.setDistanceRange( 0.1f, 500 );
    controller.addControlledPanel( vec_Q_space );

    scene_f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    scene_f.setVisible( true );

    vec_Q_space.addMouseListener( new ViewMouseInputAdapter() );
    vec_Q_space.addMouseMotionListener( new ViewMouseInputAdapter() );
    ReadoutListener listener = new ReadoutListener();
    origin_vec.addActionListener( listener );
    a_star_vec.addActionListener( listener );
    b_star_vec.addActionListener( listener );
    c_star_vec.addActionListener( listener );
    Redraw();

    vec_q_transformer = new Vector();
  }
 

  /* ---------------------------- Angle ---------------------------------- */
  /*
   *  Calculate the angle between two vectors, in degrees and return the
   *  result in a formatted string.  
   *
   *  @param  v1    The first vector
   *  @param  v2    The second vector
   *
   *  @return If both v1 and v2 are vectors of length > 0, this calculates
   *          the angle between the vectors.  If either is null or of 
   *          length 0, the String "undefined" is returned.
   */
  private String Angle( Vector3D v1, Vector3D v2 )
  {
    if ( v1 == null || v2 == null || v1.length() == 0 || v2.length() == 0 )
      return "undefined";

    float len_1 = v1.length();
    float len_2 = v2.length();
    double angle_rad = Math.acos( v1.dot(v2) / (len_1 * len_2) );
    float angle = (float)(angle_rad * (180.0/Math.PI));

    return Format.real( angle, 6, 2 );
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


  /* ------------------------- Redraw ------------------------------ */
  /*
   *  Redraw the current origin and basis vectors, and set the new
   *  values for the cosines of the angles between the basis vectors.
   */
  private void Redraw()
  {
    Vector3D origin = origin_vec.getVector();
    MarkPoint( origin, ORIGIN, Polymarker.BOX );

    Vector3D a_star = a_star_vec.getVector();
    if ( a_star == null )
    {
      vec_Q_space.removeObjects( A_STAR );
      vec_Q_space.removeObjects( A_STAR+"LINE" );
    }
    else
    {
      BasisVector( a_star, A_STAR+"LINE", Color.yellow );
      a_star.add( origin );
      MarkPoint( a_star, A_STAR, Polymarker.PLUS );
    }

    Vector3D b_star = b_star_vec.getVector();
    if ( b_star == null )
    {
      vec_Q_space.removeObjects( B_STAR );
      vec_Q_space.removeObjects( B_STAR+"LINE" );
    }
    else
    {
      BasisVector( b_star, B_STAR+"LINE", Color.yellow );
      b_star.add( origin );
      MarkPoint( b_star, B_STAR, Polymarker.STAR );
    }

    Vector3D c_star = c_star_vec.getVector();
    if ( c_star == null )
    {
      vec_Q_space.removeObjects( C_STAR );
      vec_Q_space.removeObjects( C_STAR+"LINE" );
    }
    else
    {
      BasisVector( c_star, C_STAR+"LINE", Color.yellow );
      c_star.add( origin );
      MarkPoint( c_star, C_STAR, Polymarker.CROSS );
    }

    a_star_b_star.setText( "a*<->b* : " + 
                        Angle( a_star_vec.getVector(), b_star_vec.getVector()));
    b_star_c_star.setText( "b*<->c* : " + 
                        Angle( b_star_vec.getVector(), c_star_vec.getVector()));
    c_star_a_star.setText( "c*<->a* : " + 
                        Angle( c_star_vec.getVector(), a_star_vec.getVector()));
    vec_Q_space.repaint();
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

     path = StringUtil.getCommand( 1, "-D", args );
     run_nums = StringUtil.getCommand( 1, "-R", args );

     if ( path.length() <= 0 || run_nums.length() <= 0 )
     {
       showUsage();
       System.exit(0);
     }

     path = path + "/";

     threshold = StringUtil.getCommand( 1, "-T", args );
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
       "  -T<relative threshold> specify scale factor to apply to the");
    System.out.println(
       "                         default value for the threshold.");
    System.out.println(
       "                        (values < 1 increase number of points shown)");
    System.out.println(
       "                        (values > 1 decrease number of points shown)");
    System.out.println("  -H,-h  print this message");
   }


 /* ------------------------- findAverages ---------------------- */
 /**
  *  Find the average intensity at each time slice. 
  * 
  *  @param  ds    The DataSet to be averaged.  
  *
  *  @return An array with an entry for each time bin.  The value is the
  *          average across all spectra of the values in that time bin.
  */
  private float[] findAverages( DataSet ds )
  {
    if ( ds == null || ds.getNum_entries() <= 0 )
      return null;
                                       // find out how many times there are
    Data d = ds.getData_entry(0);
    float y[] = d.getY_values();
    float sums[] = new float[ y.length ];

    for ( int i = 0; i < y.length; i++ )        // zero out the counters
      sums[i] = 0;

                                                // now total the values at all
                                                // times
    for ( int index = 0; index < ds.getNum_entries(); index++ )
    {
      d = ds.getData_entry(index);
      y = d.getY_values();
      for ( int i = 0; i < sums.length; i++ )
        sums[i]++;
    }

    int n_pixels = ds.getNum_entries();
    for ( int i = 0; i < sums.length; i++ )
      sums[i] /= n_pixels;

    return sums;
  }


 /* ------------------------ makeGoniometerRotation ------------------------ */
 /*
  *  Make the cumulative rotation matrix to "unwind" the rotations by chi,
  *  phi and omega, to put the data into one common reference frame for the
  *  crystal.
  */
  private Tran3D makeGoniometerRotation( DataSet ds )
  {
      float omega = ((Float)ds.getAttributeValue(Attribute.SAMPLE_OMEGA))
                         .floatValue();
      float phi   = ((Float)ds.getAttributeValue(Attribute.SAMPLE_PHI))
                         .floatValue();
      float chi   = ((Float)ds.getAttributeValue(Attribute.SAMPLE_CHI))
                         .floatValue();

      return tof_calc.makeEulerRotationInverse( phi, chi, -omega );
  }




  /* ------------------------- getPeaks ---------------------------- */
  /*
   *  Get an array of peaks from the dataset, based on the specified
   *  threshold scale factor.
   *
   *  @param ds            The data set from which peaks are extracted.
   *  @param thresh_scale  The relative scale factor used to calculate
   *                       the threshold.  The default threshold is 10
   *                       times the average intensity in the time slice.
   *  @return an array os ThreeD_Objects representing the points above the
   *                      threshold.
   */
  private IThreeD_Object[] getPeaks( DataSet ds, float thresh_scale )
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
      float aves[]   = findAverages( ds );

      Tran3D combinedR = makeGoniometerRotation( ds );

      int n_data = ds.getNum_entries();
      d = ds.getData_entry(0);
      int n_bins = d.getX_scale().getNum_x() - 1;

      int n_objects = n_data * n_bins;
      objs = new IThreeD_Object[n_objects];
      int obj_index = 0;

      for ( int i = 0; i < n_data; i++ )
      {
        d = ds.getData_entry(i);
        DetectorPosition pos = (DetectorPosition)
                              d.getAttributeValue( Attribute.DETECTOR_POS );
        float initial_path =
             ((Float)d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
        times = d.getX_scale().getXs();
        ys    = d.getY_values();
        for ( int j = 0; j < ys.length; j++ )
        {
          if ( ys[j] > 10*thresh_scale * aves[j] )
          {
            t = (times[j] + times[j+1]) / 2;
            q_pos = tof_calc.DiffractometerVecQ(pos,initial_path,t);

            cart_coords = q_pos.getCartesianCoords();
            pts[0].set( cart_coords[0], cart_coords[1], cart_coords[2] );
            combinedR.apply_to( pts[0], pts[0] );

            int color_index = (int)(ys[j]*3.0f/thresh_scale);
            if ( color_index > 127 )
              color_index = 127;
            c = colors[ color_index ];
            objs[obj_index] = new Ball( pts[0], 0.02f, c );
            objs[obj_index].setPickID( global_obj_index );
            obj_index++;
            global_obj_index++;
          }
        }
      }
      System.out.println("Number of points = " + obj_index );

      IThreeD_Object non_zero_objs[] = new IThreeD_Object[obj_index];
      for ( int i = 0; i < obj_index; i++ )
        non_zero_objs[i] = objs[i];

      vec_q_transformer.add( new VecQToTOF(ds ) );
      return non_zero_objs;
  }

  /* ------------------------- make_slice ---------------------------- */
  private float[][] make_slice( Vector3D origin, 
                                Vector3D base,
                                Vector3D up,
                                int      n_pixels )
  {
    if( origin == null || base == null || up == null )
      return null;

    int n_rows = n_pixels;
    int n_cols = n_rows;
    float image[][] = new float[n_rows][n_cols];

    float size;
/*
    size = base.length();
    if ( up.length() > size )
      size = up.length();
*/
    size = 20;
                                             // make two orthonormal vectors
    Vector3D base1 = new Vector3D( base );
    base1.normalize();    
    Vector3D base2 = new Vector3D( up );      
    base2.normalize();
    float component = base1.dot(base2);
    Vector3D temp = new Vector3D( base1 );
    temp.multiply( component );
    base2.subtract( temp );
    base2.normalize();

    float b1[] = base1.get();
    float b2[] = base2.get();
    float orig[] = origin.get();
    Vector3D q = new Vector3D();
    float step = size/(n_rows/2);
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
        d_col = (col - n_rows/2)*step;        
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

    return image;
  }

  /* ------------------------- main -------------------------------- */

  public static void main( String args[] )
  {
    SCDRecipLat viewer = new SCDRecipLat();

    float thresh_scale = 1.0f;

    viewer.parseArgs( args );
    if ( viewer.threshold.length() > 0 )
      try
      {
        thresh_scale = (new Float(viewer.threshold)).floatValue();
        thresh_scale = Math.abs( thresh_scale );   
        if ( thresh_scale == 0 )
        {
          thresh_scale = 1;
          System.out.println("threshold of 0 ignored, using default...");
        }
      }
      catch ( Exception e )
      {
        System.out.println("Invalid threshold value, ignored");
      }

    int runs[] = IntList.ToArray( viewer.run_nums );
    String file_names[] = new String[ runs.length ];

    for ( int i = 0; i < runs.length; i++ )
     file_names[i] = viewer.path+InstrumentType.formIPNSFileName("scd",runs[i]);

    RunfileRetriever rr;
    DataSet ds;
    viewer.global_obj_index = 0;
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
        IThreeD_Object non_zero_objs[] = viewer.getPeaks( ds, thresh_scale );
        viewer.vec_Q_space.setObjects( file_names[count], non_zero_objs );
      }
      ds = null;
   }
     
    System.out.println("All files loaded");
  }


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

/* ------------------------- SliceButtonListener ------------------------- */
/**
 * Class to handle SliceButton press events
 */
private class SliceButtonListener implements ActionListener
{
  public void actionPerformed( ActionEvent e )
  {
    String action = e.getActionCommand();
    System.out.println( action );

    float image[][] = null;
    if ( action.equals( A_B_SLICE ) )
    {
      image = make_slice( origin_vec.getVector(), 
                          a_star_vec.getVector(),
                          b_star_vec.getVector(),
                          SLICE_SIZE );
      if ( a_b_frame == null )
        a_b_frame = new ImageFrame( image, A_B_SLICE );
      else
        a_b_frame.setData( image );
    }
    else if ( action.equals( B_C_SLICE ) )
    {
      image = make_slice( origin_vec.getVector(), 
                          b_star_vec.getVector(),
                          c_star_vec.getVector(),
                          SLICE_SIZE );
      if ( b_c_frame == null )
        b_c_frame = new ImageFrame( image, B_C_SLICE );
      else
        b_c_frame.setData( image );
    }
    else if ( action.equals( C_A_SLICE ) )
    {
      image = make_slice( origin_vec.getVector(), 
                          c_star_vec.getVector(),
                          a_star_vec.getVector(),
                          SLICE_SIZE );
      if ( c_a_frame == null )
        c_a_frame = new ImageFrame( image, C_A_SLICE );
      else
        c_a_frame.setData( image );
    }
  }
}


/* ------------------------- ReadoutListener ----------------------- */
/**
 *  Class to handle user selection and scale factor change events
 *  from the VectorReadout components for origin, a*, b*, c*
 */
private class ReadoutListener implements ActionListener 
{
   public void actionPerformed( ActionEvent e )
   {
     String        action  = e.getActionCommand();
     VectorReadout readout = (VectorReadout)e.getSource();

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
     else                         // must be a numeric scale factor so scale
     {                            // the vector by the scale factor
       float scale_factor = 1;
       try
       {
         scale_factor = Float.valueOf(action).floatValue();
         Vector3D vec = readout.getVector();
         vec.multiply( scale_factor );
         readout.setVector( vec );
       }
       catch ( Exception exception )
       {                       // just use the default scale factor 
       }
     }

     Redraw();
   }
 } 


}
