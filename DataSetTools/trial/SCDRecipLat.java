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
 * Revision 1.18  2004/05/10 20:42:24  dennis
 * Test program now just instantiates a ViewManager to diplay
 * calculated DataSet, rather than keeping a reference to it.
 * This removes an Eclipse warning about a local variable that is
 * not read.
 *
 * Revision 1.17  2004/05/03 16:27:55  dennis
 * Removed one unused local variable.
 *
 * Revision 1.16  2004/03/15 06:10:54  dennis
 * Removed unused import statements.
 *
 * Revision 1.15  2004/03/15 03:28:44  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.14  2004/01/05 23:33:11  dennis
 * Now checks if value returned by VecQToTOF.intensityAtQ() is >= 0
 * since now -1 is returned instead of 0, to indicate that the Q vector
 * does not correspond to the detector.
 *
 * Revision 1.13  2003/06/04 14:20:51  dennis
 * This version is just a crude "test bed" for different methods
 * of fitting a plane to points and includes an initial form of
 * the FFT based code.
 *
 * Revision 1.12  2003/03/27 22:12:10  dennis
 * Added least squares fitting of plane in reciprocal lattice.
 * (Initial attempt)
 *
 * Revision 1.11  2003/02/18 20:19:53  dennis
 * Switched to use SampleOrientation attribute instead of separate
 * phi, chi and omega values.
 *
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
import DataSetTools.math.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.instruments.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.ViewTools.Panels.Image.*;
import gov.anl.ipns.ViewTools.Panels.ThreeD.*;
import gov.anl.ipns.ViewTools.UI.*;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.MathTools.LinearAlgebra;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import jnt.FFT.*;


/** 
 *  This class reads through a sequence of SCD run files and constructs
 *  a basic view of the peaks in 3D "Q" space.
 */

public class SCDRecipLat  implements IThreeD_drawObject
{
  public static final String NORMAL_ATTRIBUTE = "Plane Normal";
  public static final String ORIGIN = " origin ";
  public static final String A_STAR = " a* (+)";
  public static final String B_STAR = " b* (*)";
  public static final String C_STAR = " c* (X)";

  public static final String A_B_SLICE = "a*<->b* Slice";
  public static final String B_C_SLICE = "b*<->c* Slice";
  public static final String C_A_SLICE = "c*<->a* Slice";
  public static final String LOAD_ORIENT = "Load Orientation Matrix";
  
  public static final String SHOW_RESULTS = "Current Results";
  public static final int    SLICE_SIZE = 700;

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
  float[]         abNormal;
  float[]         bcNormal;
  float[]         acNormal;
  
  JLabel          a_star_b_star;
  JLabel          b_star_c_star;
  JLabel          c_star_a_star;

  Vector          vec_q_transformer;

  int             global_obj_index = 0;  // needed to keep the pick ids distinct
  String          file_names[];
  boolean         JFrameOpen;
  boolean         omit_fit_plane;
 
  int             DisplayMode;//Detemines other display elements
  InfoActionListener actListener= null;//For Info button. Need peaks
  
  //JCheckBoxMenuItem   Planes, Points;
  
  int unitSize =3;
  boolean[] omit, Cleared ;
  boolean[] reflag;
  Vector Peaks;
  float MaxIntensity;
  
  TweakerFrame     TweakFrame;
  /* ---------------------------- Constructor ----------------------------- */
  public SCDRecipLat(){
     this( null,0);
  }
  
  /**
   * Constructor
   * @param HelpFileName  Help file name
   * @param  DisplayMode  Mode for display
   *               mode =0  None
   *               mode=1  Peaks to find 3 planes
   * NOTE: A good help file for the case when viewing peaks and selecting planes
   *   is ISAW_HELP_PATH+"SCDRecipA.html"
   */
  public SCDRecipLat( String HelpFileName, int DisplayMode)
  {
    JFrame scene_f = new JFrame("Reciprocal Lattice Viewer");
    JPanel q_panel = new JPanel();

    vec_Q_space = new ThreeD_JPanel();
    controller  = new AltAzController();
    q_readout   = new JLabel("undefined");

    origin_vec  = new VectorReadout( ORIGIN  , false);
    origin_vec.setVector( new Vector3D(0,0,0 ) );
    a_star_vec  = new VectorReadout( A_STAR, "Select +" , false);
    b_star_vec  = new VectorReadout( B_STAR, "Select *"  , false);
    c_star_vec  = new VectorReadout( C_STAR, "Select X"  , false);

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
    draw_beam(1, vec_Q_space );

    scene_f.setSize(900,700);

    controller.setDistanceRange( 0.1f, 500 );
    controller.addControlledPanel( vec_Q_space );

    scene_f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    if( HelpFileName != null)
       SetUpHelp( scene_f, HelpFileName);
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
    scene_f.addWindowListener( new JFrameListener() );
    JFrameOpen = false;
    omit_fit_plane=false;
    abNormal = acNormal = bcNormal = null;
    this.DisplayMode = DisplayMode;
   
    if( DisplayMode ==1){//Add menu items and listeners
       SetUpPeakMenuItems( scene_f);
       
    }
    omit = reflag =  null;
    Peaks = null;
    MaxIntensity = -1;
    scene_f.doLayout();
    
  }
 
  private void SetUpPeakMenuItems( JFrame scene_f ) {

      if( DisplayMode !=1)
         return;
      
   
      
      JMenuBar jmb = scene_f.getJMenuBar();
      if( jmb == null ) {
         jmb = new JMenuBar();
         scene_f.setJMenuBar( jmb );
      }
      
      
      
      this.actListener = new InfoActionListener();
      JMenu Info = new JMenu("Information");
      
      jmb.add( Info );
      /*Planes = new JCheckBoxMenuItem(" 3 plane mode");
      Points = new JCheckBoxMenuItem(" 4 points mode");
      ButtonGroup BG = new ButtonGroup();
      BG.add( Planes);
      BG.add( Points);
      Points.setSelected( true );
      
      Info.add(  Points );
      Info.add( Planes );
      Info.add(  new JSeparator() );
      */
      JMenuItem Load= new JMenuItem( LOAD_ORIENT);
      Info.add( Load );
      Load.setToolTipText( "Loads and displays UB matrix as a*,b* and c*" );
      
      Load.addActionListener(  actListener );
      
      JMenuItem Res = new JMenuItem(SHOW_RESULTS);
      Info.add( Res );
      Res.setToolTipText( "Reports stats and UB so far" );
      Res.addActionListener( actListener );
      
      
      JMenu Filter = new JMenu("Omit");
      jmb.add( Filter );
      
      JCheckBoxMenuItem refOmit = new JCheckBoxMenuItem("UnCentroided peaks");
      JMenuItem Selected  = new JMenuItem("Select Peak");
      JMenuItem ClearSel = new JMenuItem("Clear all selected peaks");
      JMenu a_star = new JMenu("a* index");
        JCheckBoxMenuItem a_None = new JCheckBoxMenuItem("None");
        JCheckBoxMenuItem a_10 = new JCheckBoxMenuItem("> 10% from integer");
        JCheckBoxMenuItem a_20 = new JCheckBoxMenuItem("> 20% from integer");
        JCheckBoxMenuItem a_30 = new JCheckBoxMenuItem("> 30% from integer");
        JCheckBoxMenuItem a_40 = new JCheckBoxMenuItem("> 40% from integer");
      JMenu b_star = new JMenu("b* index");
        JCheckBoxMenuItem b_None = new JCheckBoxMenuItem("None");
        JCheckBoxMenuItem b_10 = new JCheckBoxMenuItem("> 10% from integer");
        JCheckBoxMenuItem b_20 = new JCheckBoxMenuItem("> 20% from integer");
        JCheckBoxMenuItem b_30 = new JCheckBoxMenuItem("> 30% from integer");
        JCheckBoxMenuItem b_40 = new JCheckBoxMenuItem("> 40% from integer");
      JMenu c_star = new JMenu("c* index");
        JCheckBoxMenuItem c_None = new JCheckBoxMenuItem("None");
        JCheckBoxMenuItem c_10 = new JCheckBoxMenuItem("> 10% from integer");
        JCheckBoxMenuItem c_20 = new JCheckBoxMenuItem("> 20% from integer");
        JCheckBoxMenuItem c_30 = new JCheckBoxMenuItem("> 30% from integer");
        JCheckBoxMenuItem c_40 = new JCheckBoxMenuItem("> 40% from integer");
     //JMenuItem OK = new JMenuItem("Execute");
     
     JMenuItem Save= new JMenuItem( "Save omits to Peaks");
     Filter.add(refOmit);
     Filter.add(Selected);
     Filter.add( ClearSel);
     Filter.add(a_star);
     Filter.add(b_star);
     Filter.add(c_star);
     a_star.add(a_None);
     a_star.add(a_10);
     a_star.add(a_20);
     a_star.add(a_30);
     a_star.add(a_40);
     b_star.add(b_None);
     b_star.add(b_10);
     b_star.add(b_20);
     b_star.add(b_30);
     b_star.add(b_40);
     c_star.add(c_None);
     c_star.add(c_10);
     c_star.add(c_20);
     c_star.add(c_30);
     c_star.add(c_40);
     ButtonGroup BGa = new ButtonGroup();
     ButtonGroup BGb = new ButtonGroup();
     ButtonGroup BGc = new ButtonGroup();
     BGa.add(  a_None );
     BGa.add(  a_10 );
     BGa.add(  a_20 );
     BGa.add(  a_30 );
     BGa.add(  a_40 );
     a_None.setSelected( true );
     BGb.add(  b_None );
     BGb.add(  b_10 );
     BGb.add(  b_20 );
     BGb.add(  b_30 );
     BGb.add(  b_40 );
     b_None.setSelected( true );
     BGc.add(  c_None );
     BGc.add(  c_10 );
     BGc.add(  c_20 );
     BGc.add(  c_30 );
     BGc.add(  c_40 );
     c_None.setSelected( true );
     
     //Filter.add( OK );
     Filter.add( Save );  
     FilterActionListener FF = new FilterActionListener( Filter );
     //OK.addActionListener( FF );
       refOmit.addActionListener(FF);
       a_None.addActionListener(  FF );
       a_10.addActionListener(  FF );
       a_20.addActionListener(  FF );
       a_30.addActionListener(  FF );
       a_40.addActionListener(  FF );
       b_None.addActionListener(  FF );
       b_10.addActionListener(  FF );
       b_20.addActionListener(  FF );
       b_30.addActionListener(  FF );
       b_40.addActionListener(  FF );
       c_None.addActionListener(  FF );
       c_10.addActionListener(  FF );
       c_20.addActionListener(  FF );
       c_30.addActionListener(  FF );
       c_40.addActionListener(  FF );
     ClearSel.addActionListener(  FF  );
     Selected.addActionListener(  FF  );
     Save.addActionListener( new SaveActionListener());

     
     JMenu Tweak = new JMenu("Tweak");
     MenuListener TweakListener = new TweakListener();
     Tweak.addMenuListener(  TweakListener );
     jmb.add( Tweak );
     
     JMenu D3Men = new ThreeDAttributeJMenu( this, "Attributes","Marks",
              new String[]{"Box","Cross","Dot","Plus","Star"}, "Size", 1f,30f,2f,
              null,null,null);
     
     jmb.add(  D3Men );
      jmb.doLayout();
      jmb.repaint();
 
   }
  private void SetUpHelp( JFrame scene_f, String HelpFileName){
     
     if(scene_f == null || HelpFileName == null )
        return;
     java.io.File f = new java.io.File( HelpFileName);
     if( !f.exists())
        return;
     JMenuBar jmb = scene_f.getJMenuBar();
     if( jmb == null){
        jmb = new JMenuBar();
        scene_f.setJMenuBar( jmb );
     }
     JMenu helpMenu = null;
     for( int i=0; i < jmb.getMenuCount() && helpMenu == null; i++)
        if( jmb.getMenu( i).getText().equalsIgnoreCase( "Help" ))
        		helpMenu = jmb.getMenu( i );
     if( helpMenu == null){
        helpMenu = new JMenu("Help");
        jmb.add( helpMenu );
       
     }
     JMenuItem jmi = new JMenuItem("How To");
     jmi.addActionListener( new HelpMenuListener( HelpFileName ) );
     helpMenu.add(  jmi );
     
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
    IThreeD_Object objects[] = new IThreeD_Object[ 3 ];
    Vector3D points[] = new Vector3D[2];

    points[0] = new Vector3D( 0, 0, 0 );                    // y_axis
    points[1] = new Vector3D( 0, length, 0 );
    objects[0] = new Polyline( points, Color.green );
                                                            // z_axis
    points[1] = new Vector3D( 0, 0, length );
    objects[1] = new Polyline( points, Color.blue );

    points[1] = new Vector3D( length, 0, 0 );               // +x-axis
    objects[2] = new Polyline( points, Color.red );

    threeD_panel.setObjects( "AXES", objects );
  }


  /* --------------------------- draw_beam --------------------------- */

  private void draw_beam( float length, ThreeD_JPanel threeD_panel )
  {
    IThreeD_Object objects[] = new IThreeD_Object[ 15 ];
    Vector3D points[] = new Vector3D[1];

    points[0] = new Vector3D( 0, 0, 0 );                    // sample
    Polymarker sample = new Polymarker( points, Color.red );
    sample.setType( Polymarker.STAR );
    sample.setSize( 10 );
    objects[0] = sample;

    for ( int i = 1; i < 15; i++ )                       // beam is segmented
    {                                                    // for depth sorting
      points = new Vector3D[2];
      points[0] = new Vector3D( -(i-1)*length/3, 0, 0 ); // beam in 
      points[1] = new Vector3D( -i*length/3, 0, 0 );     // -x-axis direction
      objects[i] = new Polyline( points, Color.red );
    }

    threeD_panel.setObjects( "BEAM", objects );
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

      Tran3D combinedR = makeGoniometerRotationInverse( ds );

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
            objs[obj_index] = new Ball( pts[0], 0.03f, c );
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

    fit_plane( origin, base1, base2 );

    base1.normalize();    
    base2.normalize();    

    float b1[] = base1.get();
    float b2[] = base2.get();
    float orig[] = origin.get();
    Vector3D q = new Vector3D();
    float step = (2*size)/n_rows;
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
           if ( value >= 0 )
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


  /* ---------------------- get_data_objects ----------------------- */

  private IThreeD_Object[] get_data_objects()
  {
    IThreeD_Object q_obj[][] = new IThreeD_Object[file_names.length][];

    for ( int i = 0; i < file_names.length; i++ )
    {
      q_obj[i] = vec_Q_space.getObjects( file_names[i] );
      System.out.println("i = " + i + ", list length = " + q_obj[i].length );
    }

    int total_length = 0;
    for ( int i = 0; i < file_names.length; i++ )
      total_length += q_obj[i].length;

    System.out.println("total_length = " + total_length);
    IThreeD_Object obj[] = new IThreeD_Object[total_length];
    int start = 0;
    for ( int i = 0; i < file_names.length; i++ )
    {
      System.out.println("i = " + i + ", length = " + q_obj[i].length );
      System.arraycopy( q_obj[i], 0, obj, start, q_obj[i].length );
      start += q_obj[i].length;
    }
    q_obj = null;

    return obj;
  } 


  /* -------------------------- fit_plane --------------------------- */

  private void fit_plane( Vector3D origin, Vector3D base1, Vector3D base2 )
  {
                                         // first, get the positions of the
                                         // data points.
    if( omit_fit_plane)
       return;
    IThreeD_Object obj[] = get_data_objects();
    
    Vector3D n = new Vector3D();
    n.cross( base1, base2 );
    n.normalize();

    float c = n.dot( origin );
    float n_dot_p;
                                            // first get a new list of objects
                                            // that are close to the plane
    IThreeD_Object plane_pts[] = new IThreeD_Object[ obj.length ];
    int n_used = 0;
    for ( int i = 0; i < obj.length; i++ )
    {
      n_dot_p = n.dot( obj[i].position() );      
      if ( Math.abs( n_dot_p - c ) < 0.3 )
      {
         plane_pts[ n_used ] = obj[ i ];
         n_used++;
      }
    }

    Polymarker pts_used[] = new Polymarker[n_used];
    for ( int i = 0; i < n_used; i++ )
    {
      Vector3D verts[] = new Vector3D[1];
      verts[0] = new Vector3D( plane_pts[i].position().get() );
      pts_used[i] = new Polymarker( verts, Color.green );
      pts_used[i].setSize( 4 ); 
      pts_used[i].setType( Polymarker.BOX ); 
    }
    vec_Q_space.setObjects( "LSQ_PTS", pts_used );


    System.out.println("Original normal vector = " + n );
    System.out.println("Original c = " + c );
    System.out.println("number used = " + n_used );

/*
    float points[][] = new float[n_used][3];
    float normal[]   = new float[3];
    float distance[] = new float[1];

    for ( int i = 0; i < n_used; i++ )
      points[i] = plane_pts[i].position().get();
   
    double residual = fit_plane( points, normal, distance );
    n = new Vector3D( normal );
    c = distance[0];
*/
    Vector3D points[] = new Vector3D[ n_used ];
    for ( int i = 0; i < n_used; i++ )
      points[i] = plane_pts[i].position();

    float residual;
    float time1,
          time2,
          time3,
          time4;
    ElapsedTime timer = new ElapsedTime();
    timer.reset();
 
    Plane3D test_plane1 = new Plane3D();
    test_plane1.fit( points );             // test the first fitting method 
    time1 = timer.elapsed();

    timer.reset();
    Plane3D test_plane2 = new Plane3D();
    residual = test_plane2.fit( points );  // test the second fitting method
    time2 = timer.elapsed();

    timer.reset();
    Plane3D test_plane3 = new Plane3D();
    test_plane3.fit( points );             // test the third fitting method 
    time3 = timer.elapsed();

    timer.reset();
    Plane3D test_plane4 = new Plane3D();
    test_plane4.fit( points );             // test the fourth fitting method 
    time4 = timer.elapsed();

    n = test_plane4.getNormal(); 
    c = test_plane4.getDistance();

    Vector3D n1 = test_plane1.getNormal();
    Vector3D n2 = test_plane2.getNormal();
    Vector3D n3 = test_plane3.getNormal();
    Vector3D n4 = test_plane4.getNormal();

    System.out.println("Dot products, and c values are.....");
    System.out.println("n4 . n1 = " + n4.dot(n1) );
    System.out.println("n4 . n2 = " + n4.dot(n2) );
    System.out.println("n4 . n3 = " + n4.dot(n3) );
    System.out.println("c1 = " + test_plane1.getDistance() );
    System.out.println("c2 = " + test_plane2.getDistance() );
    System.out.println("c3 = " + test_plane3.getDistance() );
    System.out.println("c4 = " + test_plane4.getDistance() );
    System.out.println("Time for fit 1 = " + time1 );
    System.out.println("Time for fit 2 = " + time2 );
    System.out.println("Time for fit 3 = " + time3 );
    System.out.println("Time for fit 4 = " + time4 );

    System.out.println("USING normal vector = " + n );
    System.out.println("c = " + c );
    System.out.println("residual = " + residual );

    System.out.println("Extracting postions.....");
    Vector3D all_vectors[] = new Vector3D[ obj.length ];
    for ( int i = 0; i < obj.length; i++ )
      all_vectors[i] = obj[i].position();  
    System.out.println("DONE Extracting postions.");

    System.out.println("Projecting points on test plane 4......");
    DataSet ds = ProjectPoints( test_plane4, all_vectors );
    new ViewManager( ds, IViewManager.IMAGE ); 
    System.out.println("Done Projecting points on test plane 4.");

//    ds = ProjectPointsRandomly( all_vectors );
    System.out.println("Projecting points uniformly ......");
    ds = ProjectPointsUniformly( all_vectors, 10 );
    ds.addIObserver( new FFTListener() );

    System.out.println("Done Projecting points uniformly.");
    new ViewManager( ds, IViewManager.IMAGE ); 

    ds = FFT( ds );
    ds.addIObserver( new FFTListener() );
    new ViewManager( ds, IViewManager.IMAGE ); 
                                                // calculate new origin and
                                                // basis vectors for the plane
    float old_c = origin.dot( n );
    origin.multiply(c/old_c); 

    Vector3D new_base1 = new Vector3D(base1);
    Vector3D temp_v = new Vector3D(n);
    temp_v.multiply( n.dot(base1) );
    new_base1.subtract( temp_v );
    base1.set( new_base1 );

    Vector3D new_base2 = new Vector3D(base2);
    temp_v.set(n);
    temp_v.multiply( n.dot(base2) );
    new_base2.subtract( temp_v );
    base2.set( new_base2 );

    System.out.println("Using Origin : " + origin );
    System.out.println("Using base1  : " + base1 );
    System.out.println("Using base2  : " + base2 );

                                                 // draw the plane used
    Polyline plane_used[] = new Polyline[1];
    Vector3D verts[] = new Vector3D[4];
    for ( int i = 0; i < 4; i++ )
      verts[i] = new Vector3D(origin);

    temp_v.set( base1 );
    temp_v.multiply( 5 );
    verts[1].add(temp_v);

    temp_v.set( base2 );
    temp_v.multiply( 5 );
    verts[2].add(temp_v);
    plane_used[0] = new Polyline( verts, Color.green );
    vec_Q_space.setObjects( "LSQ_PLANE", plane_used );
  }


  /* ------------------------- ProjectPoints ---------------------------- */

  public DataSet ProjectPoints( Plane3D plane, Vector3D points[] )
  {
    float y[] = new float[400];
    XScale scale = new UniformXScale( -20, 20, 400 );
    float dist;
    int   bin;
    for ( int i = 0; i < points.length; i++ )
    {
      dist = plane.getDistance( points[i] );
      bin = Math.round( dist * 10 ) + 200;
      if ( bin >= 0 && bin < y.length )
        y[bin]++; 
    }
    Data d = Data.getInstance( scale, y, 1 );
    d.setAttribute( new Float1DAttribute( NORMAL_ATTRIBUTE, 
                                          plane.getNormal().get() ));
    DataSetFactory ds_factory = new DataSetFactory(
                                       "Projection parallel to " + plane );
    DataSet ds = ds_factory.getDataSet();
    ds.addData_entry( d );
    return ds;
  }



  public DataSet ProjectPointsRandomly( Vector3D points[] )
  {
    DataSetFactory ds_factory = new DataSetFactory(
                                       "Projection parallel to planes" );
    DataSet ds = ds_factory.getDataSet();
    Vector3D normal;
    float    components[] = new float[3];
    for ( int count = 0; count < 500; count++ )
    {
      components[0] = (float)Math.random();
      components[1] = (float)Math.random();
      components[2] = (float)Math.random();
      normal = new Vector3D( components );
      normal.normalize();
      Data d =  ProjectPoints(points, normal,count);
      ds.addData_entry( d );
    }
    return ds;
  }


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
      for ( double theta = 0;
                   theta < 2*Math.PI - theta_step/2; 
                   theta += theta_step )
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

    return ds;
  }


  public Data ProjectPoints( Vector3D points[], Vector3D normal, int id )
  {
    float dist;
    int   bin;
    float y[] = new float[512];
    XScale scale = new UniformXScale( -20, 20, 513 );

    Plane3D plane = new Plane3D();
    plane.set( normal, 0 );
    for ( int i = 0; i < points.length; i++ )
    {
      dist = plane.getDistance( points[i] );
      bin = Math.round( dist * 20 ) + 256;
      if ( bin >= 0 && bin < y.length )
        y[bin]++;
    }
    Data d = Data.getInstance( scale, y, id );
    d.setAttribute( new Float1DAttribute( NORMAL_ATTRIBUTE, normal.get() ));

    return d;
  }


  public DataSet FFT( DataSet ds )
  {
    DataSetFactory ds_factory = new DataSetFactory(
                                       "FFT of projections" );
    DataSet fft_ds = ds_factory.getDataSet();
    
    XScale scale = new UniformXScale( 0, 511, 512 );
    
    float complex_data[] = new float[1024];
    float re, 
          im;
    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
//      System.out.println("Doing transform of projection : " + i );
      Data original_d = ds.getData_entry(i);
      float y[] = original_d.getY_values();
//      System.out.println("y.length = " + y.length );
      float average = 0;
      for ( int j = 0; j < 512; j++ )
      {
        average += y[j];
        complex_data[2*j  ] = y[j];
        complex_data[2*j+1] = 0;
      }
      average /= 512;
      for ( int j = 0; j < 512; j++ )    // subtract off the "DC" term
        complex_data[2*j] -= average;
 
      ComplexFloatFFT fft = new ComplexFloatFFT_Radix2( 512 );
      fft.transform( complex_data );
      float magnitude[] = new float[512];
      for ( int j = 0; j < 512; j++ )
      {
        re = complex_data[2*j];
        im = complex_data[2*j+1];
        magnitude[j] = (float)(Math.sqrt(re*re+im*im));
      }
      Data d = Data.getInstance( scale, magnitude, i+1 );
//      System.out.println("adding data block " + i );
      d.setAttribute( original_d.getAttribute( NORMAL_ATTRIBUTE ) );
      fft_ds.addData_entry( d );
    }

    return fft_ds;
  }



  /*
   *  Fit a plane to the specified points.  Return the residual error as 
   *  the value of the function.  Set the unit normal to the plane in the 
   *  first three components of array plane[4] and the distance of the plane
   *  from the origin in plane[3].  That is, the best fit plane is given by 
   *  the equation:
   *
   *    plane[0] x + plane[1] y + plane[2] z = plane[3] 
   *
   */
  public static double fit_plane( float points[][], 
                                  float normal[], 
                                  float distance[] )
  {
    if ( points == null || normal == null || distance == null )
    {
      System.out.println("ERROR: null parameter in fit_plane()" );
      System.out.println("points   = " + points );
      System.out.println("normal   = " + normal );
      System.out.println("distance = " + distance );
      return Double.NaN;
    }

    int n_points = points.length;
    if ( points.length < 3 )
    {
      System.out.println("ERROR: not enough points in fit_plane()" );
      System.out.println("points.length = " + points.length );
      return Double.NaN;
    }

    if ( normal.length < 3 || distance.length < 1 )
    {
      System.out.println("ERROR: not enough space for return values "+
                         "in fit_plane()" );
      System.out.println("normal.length = " + normal.length );
      System.out.println("distance.length = " + distance.length );
      return Double.NaN;
    }

    for ( int i = 0; i < points.length; i++ )
      if ( points[i].length < 3 )
      {
        System.out.println("ERROR: points not three dimensional in " + 
                           "fit_plane()" );
        System.out.println("points[i].length = " + points[i].length );
        return Double.NaN;
      }

    Vector3D v0 = new Vector3D( points[0] );
    Vector3D v1 = new Vector3D( points[n_points/2] );
    Vector3D v2 = new Vector3D( points[n_points-1] );

    Vector3D e0 = new Vector3D(v1);
    Vector3D e1 = new Vector3D(v2);

    e0.subtract( v0 );                    // set edge vectors to be v1 - v0 and
    e1.subtract( v1 );                    //                        v2 - v1

    Vector3D n = new Vector3D();
    n.cross( e1, e0 );
    if ( n.length() == 0 )
    {
      System.out.println("ERROR: first three points are co-linear in "+
                         "fit_plane()" );
      return Double.NaN;
    }
                                             // find the max component of n
    int max_index = 0;
    for ( int i = 1; i < 3; i++ )
     if ( Math.abs(n.get()[i]) > Math.abs(n.get()[max_index]) )
       max_index = i;
                                           // next find the least squares fit
                                           // to the plane.  Subject to the
                                           // constraint that n[max_index] == 1

    double A[][] = new double[n_points][3];
    double b[]   = new double[n_points];
    int position;

    for ( int i = 0; i < n_points; i++ )
    {
      position = 0;
      for ( int j = 0; j < 3; j++ )
        if ( j != max_index )
        {
          A[i][position] = points[i][j];
          position++;
        }
      A[i][2] = 1;
      b[i] = -points[i][max_index];
    }

    double Q[][] = LinearAlgebra.QR_factorization( A );
    double residual = LinearAlgebra.QR_solve( A, Q, b );

    float temp[] = new float[4];
    temp[3] = 1;                                // now pull out the solution
    temp[max_index] = 1;                        // with the specified component
    position = 0;                               // equal to 1.
    for ( int i = 0; i < 3; i++ )
      if ( i != max_index )
      {
        temp[i] = (float)b[position];
        position++;
      }

    n = new Vector3D( temp );
    float c = -(float)b[2];

    float length = n.length();                  // rescale n to be unit normal
    n.multiply(1/length);
    c = c / length;                             // and c to correspond
                                                // to the new unit normal.
    System.out.println("Best fit normal vector = " + n );
    System.out.println("c = " + c );
    System.out.println("residual = " + residual );

    for ( int i = 0; i < 3; i++ )
      normal[i] = n.get()[i];

    distance[0] = c;
    return residual;
  }
  
  /**
   * Sends back the unitized a*,b*, and c* vectors selected in this viewer.
   * NOTE: Check the help menu for the interpretation of these vectors.
   *       If there is no help menu, these are ??????
   *
   * @return  an array of unit vectors that are the normals to
   *          three planes
   */
  public float[][] getPlaneNormals(){
     
     float[][] Res = new float[3][];
     if( abNormal == null)
        Res[0]=null;
     else 
        Res[0]=abNormal;
     

     if( bcNormal == null)
        Res[1]=null;
     else 
        Res[1]=bcNormal;
     

     if( acNormal == null)
        Res[2]=null;
     else 
        Res[2]=acNormal;
     
     return Res;
     
   
  }
     
    
 
  
  //Returns the float[3] whose direction is the normal to the selected plane
  // and whose length represents the distance between two such planes
  private float[] CalcNormal( Vector3D direction, Data ds){
     if( ds == null)
        return null;
     
     float[] yvals = ds.getY_values();
     float[] xvals = ds.getX_values();
     if( yvals == null || xvals == null)
        return null;
     if( yvals.length <3  || xvals.length < 3 || yvals[0] < 0)
        return null;
     float Ty = 0;
     for( int i=0; i< yvals.length; i++)
        Ty +=yvals[i];
     Ty= Ty/yvals.length;
     int mode = 0;//0 start 1- in 1st neg region  //2 in pos region  
                  //3 next neg region  done
     float zeroTolerance = (yvals[1]-Ty)/15f;
     float maxY=Float.NEGATIVE_INFINITY;
     float maxX = Float.NaN;
     for( int i=1; i< yvals.length || mode < 3; i++ ){
        if( mode == 0){
           if( yvals[i]< Ty-zeroTolerance)
              mode =1;
        }else if( mode ==1){
           if( yvals[i]>Ty+zeroTolerance){
              mode =2;
              maxY= yvals[i];
              maxX = xvals[i];
           }
        }else if( mode ==2){
           if( yvals[i]>maxY){
              maxY= yvals[i];
              maxX = xvals[i];
           }else if( yvals[i]< Ty-zeroTolerance)
              mode = 3;
        }
     }
     if( mode < 3 || Float.isNaN( maxX ))
        return null;
     
     direction.multiply( maxX );
     return direction.get();
     
    
  }
  
  // Gets the maximum peak ipkobs and also maximum Q for all the peaks
  private float[] GetMaxPkIntensity( Vector Peaks){
     float Max = 0;
     
     float Qmax  = 0;
     if(Peaks == null)
        return null;
     for( int i=0;i<Peaks.size();i++)
        if( Peaks.elementAt(i)!= null && Peaks.elementAt(i) instanceof IPeak){
           IPeak pk = ((IPeak)Peaks.elementAt(i));
           if( pk.ipkobs() > Max)
              Max =pk.ipkobs();
           float[] Q =pk.getUnrotQ();
           float MQ = Q[0]*Q[0]+Q[1]*Q[1]+Q[2]*Q[2];
           if( MQ >Qmax)
              Qmax = MQ;
        }
     float[] Res = new float[2];
     Res[0] = Max;
     Res[1]= (float)Math.sqrt(Qmax);
     return Res;     
  }
  
  /**
   * Bad if any decimal digit is greater than 1
   * @param num  The bad number
   * @return  true if any decimal digit is greater than 1
   */
  private boolean isBad( int num ){
     if( num < 0)
        return true;
    for(int X = num; X >0; X = X/10)
       if( X % 10 > 1)
          return true;
    return false;

     
  }
  /**
   * Creates a reciprocal lattice viewer from Peaks.
   * @param Peaks  A Vector of peaks
   * @param wait   true if it should wait until Frame is closed
   * @return    The SCDRecipLat is returned for information
   */
  public SCDRecipLat getSCDLat( Vector Peaks, boolean wait){
     
     if( Peaks == null)
        return null; 
     this.actListener.setPeaks( Peaks );
     JFrameOpen = true;
     omit_fit_plane = true;
     this.Peaks = Peaks;
     
     reflag = new boolean[ Peaks.size() ];
     Arrays.fill( reflag , false );
     for( int i=0; i< Peaks.size(); i++){
        if( Peaks.elementAt(i)!= null && 
                 (Peaks.elementAt( i ) instanceof IPeak)){
           IPeak Peak = (IPeak)(Peaks.elementAt(i));
           if( isBad(Peak.reflag()))
                    reflag[i]=true;
        }
     }
     float[] Range = GetMaxPkIntensity( Peaks);
     MaxIntensity = Range[0];
     controller.setDistanceRange( 0f ,Range[1]*5f );
     if(MaxIntensity < 0 )
        MaxIntensity = Range[0];
     
     if( omit == null){
        omit = new boolean[ Peaks.size()];
        Arrays.fill( omit, false);
     }
        
     Create3DPeakView( omit );  
     
     Redraw();
     if( wait )
        checkJFrameDone();   
     

     return this;
     
  }
  
  private void notifyDataChanged(){
     
     if( TweakFrame != null)
        TweakFrame.actionPerformed( null );
     
     
  }
  protected void Create3DPeakView( boolean[] omit ){
    int nn=0;
    for( int ii =0; ii < omit.length ; ii++)
       if( !omit[ii] ) nn++;
    
    IThreeD_Object[] Allobjs = new IThreeD_Object[ nn ];
     
     file_names = new String[1];
     file_names[0]= "Peaks";
     //float ballSize = Range[1]/Math.max(Peaks.size(),400 );
     unitSize = 4;
     int j=0;
     for( int i=0; i< Peaks.size(); i++)
        if( !omit[i])
        if( Peaks.elementAt(i)!= null && 
                 (Peaks.elementAt( i ) instanceof IPeak)){
           IPeak Peak = (IPeak)(Peaks.elementAt(i));
         
           int intensity = Peak.ipkobs();
           float[] Q = Peak.getUnrotQ();
           int color = (int)(intensity*127/MaxIntensity);
           if( color >= colors.length)
              color = colors.length-1;
           Vector3D[] Marks = new Vector3D[1];
           Marks[0]= new Vector3D( Q[0], Q[1],Q[2]);
           
           Allobjs[j] = new Polymarker(Marks,colors[color]);
           ((Polymarker)Allobjs[j]).setSize( unitSize );
           ((Polymarker)Allobjs[j]).setType(  Polymarker.STAR );
           Allobjs[j].setPickID( Peak.seqnum() );
           j++;
           
        
     }
     this.vec_Q_space.setObjects(  file_names[0] , Allobjs );
     notifyDataChanged();
  }
  
  
  private void checkJFrameDone(){
     
     while( JFrameOpen )
      try{
        Thread.sleep( 3000 );
      }catch(Exception s){
         System.out.println("Exception while waiting");
         s.printStackTrace();
         JFrameOpen=false;
      }
     
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
    viewer.file_names = new String[ runs.length ];

    for ( int i = 0; i < runs.length; i++ )
     viewer.file_names[i] 
                 = viewer.path+InstrumentType.formIPNSFileName("scd",runs[i]);

    RunfileRetriever rr;
    DataSet ds;
    viewer.global_obj_index = 0;
    for ( int count = 0; count < viewer.file_names.length; count++ )
    {
      System.out.println("Loading file: " + viewer.file_names[count]);
      rr = new RunfileRetriever( viewer.file_names[count] );
      ds = rr.getFirstDataSet( Retriever.HISTOGRAM_DATA_SET );
      rr = null;
      if ( ds == null )
        System.out.println("File not found: " + viewer.file_names[count]);
      else
      {
        IThreeD_Object non_zero_objs[] = viewer.getPeaks( ds, thresh_scale );
        viewer.vec_Q_space.setObjects(viewer.file_names[count], non_zero_objs);
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
     int index = vec_Q_space.pickID( e.getX(), e.getY(), 5 );
     if( omit_fit_plane && index!=IThreeD_Object.INVALID_PICK_ID)
        if(e.getButton() == MouseEvent.BUTTON3)
           JOptionPane.showMessageDialog( null , "Seq Num="+index , "Peak" , 
                    JOptionPane.INFORMATION_MESSAGE );
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

private class TweakListener extends WindowAdapter implements MenuListener{
   
 
   
   public void menuSelected(MenuEvent e){
      
      if( TweakFrame == null){
         
         TweakFrame = new TweakerFrame( vec_Q_space, a_star_vec,
                  b_star_vec,c_star_vec);
         TweakFrame.addWindowListener( this );
      }
     
   }
   
   public void menuDeselected(MenuEvent e){
      
   }
   public void menuCanceled(MenuEvent e){
      
   }
   public void windowClosing(WindowEvent e){
      TweakFrame = null;
      
   }
     
  
 
}
private class FilterActionListener implements ActionListener{
   
   JMenu FilterMenu;
   int refl_index, a_index, b_index, c_index, sel_index; 
   public FilterActionListener( JMenu jmb){
      
      FilterMenu = jmb;
      refl_index=a_index=b_index=c_index= sel_index=-1;
      if( jmb == null)
         return;
      for( int i=0; i < FilterMenu.getItemCount() ; i++){
         String Text = FilterMenu.getItem( i ).getText();
         if(Text.indexOf( "Centroid" ) >=0 )
            refl_index = i;
         else if( Text.indexOf( "Select" ) >= 0 )
            sel_index = i;
         else if( Text.indexOf( "a*" ) >= 0 )
            a_index = i;
         else if( Text.indexOf( "b*" ) >= 0 )
            b_index = i;
         else if( Text.indexOf( "c*" ) >= 0 )
            c_index = i;
         
      }
   }
   
   private void Calculate( JMenuItem men, VectorReadout dir, boolean[] omit, boolean[] clear ){
      if( men == null || dir == null || omit == null || clear == null || 
                       Peaks == null)
         return;
      if( omit.length != clear.length || omit.length != Peaks.size())
         return;
         if( !(men instanceof JMenu) )
            return;
         JMenu mmen = (JMenu) men;
         float percent = -1;
         for( int i=0; i < mmen.getItemCount(); i++)
            if( mmen.getItem(  i ).isSelected()){
               String S = mmen.getItem( i ).getText();
               if( S.indexOf( "None" ) >= 0){}
               else if( S.indexOf( "10" ) >= 0)
                  percent = .1f;
               else if( S.indexOf( "20" ) >= 0)
                  percent = .2f;
               else if( S.indexOf( "30" ) >= 0)
                  percent = .3f;
               else if( S.indexOf( "40" ) >= 0)
                  percent = .4f;
               		
            }
         if( percent < 0)
            return;
         Vector3D NormDir = TweakerFrame.NormalUnitDir(TweakerFrame.getVec( dir ) , 
                  TweakerFrame.getVec( a_star_vec ) , TweakerFrame.getVec( b_star_vec ) , 
                  TweakerFrame.getVec( c_star_vec) );
         if( NormDir == null)
            return;
         float L = dir.getVector().dot( NormDir );
         if(  L == 0 )
            return;
         float[] dd = NormDir.get();
          
         for( int i=0; i< Peaks.size(); i++)
          if( !omit[i])
         {
            IPeak P = (IPeak)Peaks.elementAt( i );
            float[] Q = P.getUnrotQ();
            float Proj = Q[0]*dd[0]+Q[1]*dd[1]+Q[2]*dd[2];
            float cell = Math.abs( Proj/L );
            if( Math.abs( cell -(int)cell) > percent)
               if( Math.abs( (int)cell +1 -cell ) > percent )
                  omit[i] = true;
            
         }else if( Cleared[i])
            omit[i]=true;
         
   }
   public void actionPerformed( ActionEvent evt){
      if( Peaks == null || FilterMenu == null)
         return;

      if( Cleared == null){
         Cleared = new boolean[Peaks.size()];
         Arrays.fill(  Cleared , false );
      }
     
      if( evt.getActionCommand().indexOf( "Select" ) >= 0 ){
         IThreeD_Object pick = vec_Q_space.pickedObject();
         if( pick == null)
            return;
         int i = pick.getPickID();
         if( i-1 >= 0 && i-1 < Cleared.length){
            Cleared[i-1] = true;
            omit[i-1] = true;
         }
            
      }else
      if( evt.getActionCommand().indexOf("Clear")>=0 ){
         Arrays.fill(  Cleared , false );
       
         
      }
      omit = new boolean[Peaks.size()];
      if( refl_index >=0 && FilterMenu.getItem( refl_index ).isSelected())
         System.arraycopy( reflag , 0 , omit , 0 , omit.length );
      else
         Arrays.fill( omit , false );
      for( int i=0; i< omit.length; i++)
         if( Cleared[i])
            omit[i]=true;
      if( a_index >= 0)
         Calculate( FilterMenu.getItem(a_index), a_star_vec, omit, Cleared);
      if( b_index >= 0)
         Calculate( FilterMenu.getItem(b_index), b_star_vec, omit, Cleared);
      if( c_index >= 0)
         Calculate( FilterMenu.getItem(c_index), c_star_vec, omit, Cleared);
    
      vec_Q_space.removeObjects();
      Create3DPeakView(omit);
      Redraw();
      
   }
}

private class SaveActionListener implements ActionListener{
   
  
   
   public void actionPerformed( ActionEvent evt){
      if( omit == null || Peaks == null )
         return;
      if( omit.length != Peaks.size())
         return;
      for( int i=0; i < omit.length ;i++){
         IPeak p = (IPeak) (Peaks.elementAt( i ));
         int r = p.reflag();
         if( !reflag[i])
            if( omit[i])
               p.reflag( r+30);
            else
               p.reflag(r - 10*((r/10)%10 )+10);
            
       
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
    //System.out.println( action );

    float image[][] = null;
    if( action.equals( A_B_SLICE ) ) {
       
       image = make_slice( origin_vec.getVector() ,
                   a_star_vec.getVector() , b_star_vec.getVector() ,
                     SLICE_SIZE );
       if(!omit_fit_plane)
       if( a_b_frame == null )
          a_b_frame = new ImageFrame( image , A_B_SLICE );
       else
          a_b_frame.setData( image );


       if( Undefined( a_star_vec.getVector() )
                     || Undefined( b_star_vec.getVector() ) )
          abNormal = null;
       else {
          
          Vector3D V = new Vector3D();
          V.cross( a_star_vec.getVector() , b_star_vec.getVector() );
          V.normalize();
          abNormal = V.get();
       }
       
       if( omit_fit_plane )
          ShowPlane( origin_vec.getVector() , a_star_vec.getVector() ,
                        b_star_vec.getVector() , 0 );
         
   }else if ( action.equals( B_C_SLICE ) )
    {
      image = make_slice( origin_vec.getVector(), 
                          b_star_vec.getVector(),
                          c_star_vec.getVector(),
                          SLICE_SIZE );
      if(!omit_fit_plane)
      if ( b_c_frame == null )
        b_c_frame = new ImageFrame( image, B_C_SLICE );
      else
        b_c_frame.setData( image ); Vector3D V= new Vector3D();
      if( Undefined( c_star_vec.getVector())||Undefined(b_star_vec.getVector()))
        bcNormal = null;
      else{
        V.cross(b_star_vec.getVector(), c_star_vec.getVector());
        V.normalize();
        bcNormal= V.get();
      }
        if( omit_fit_plane )
           ShowPlane( origin_vec.getVector(),
                            b_star_vec.getVector(),
                            c_star_vec.getVector(), 1 );
       
    }
    else if ( action.equals( C_A_SLICE ) )
    {
      image = make_slice( origin_vec.getVector(), 
                          c_star_vec.getVector(),
                          a_star_vec.getVector(),
                          SLICE_SIZE );
      if(!omit_fit_plane)
      if ( c_a_frame == null )
        c_a_frame = new ImageFrame( image, C_A_SLICE );
      else
        c_a_frame.setData( image ); 
      if( Undefined( a_star_vec.getVector())||Undefined(c_star_vec.getVector()))
        acNormal = null;
      else{
        Vector3D V= new Vector3D();
        V.cross(a_star_vec.getVector(), c_star_vec.getVector());
        V.normalize();
        acNormal= V.get();
      }
        if( omit_fit_plane )
           ShowPlane( origin_vec.getVector(),
                            a_star_vec.getVector(),
                            c_star_vec.getVector(), 2 );
    }
  }
  private boolean Undefined( Vector3D vec){
     if( vec == null)
        return true;
     if( Float.isNaN(vec.length()))
        return true;
     return false;
  }
  private void ShowPlane( Vector3D origin, Vector3D base1, Vector3D base2,
              int code){
     

     String name = ""+(char)('a'+code);
     name +=(char)('a'+((code+1)%3));
     name +="_Plane points";
     if( Undefined( origin)||Undefined(base1)||Undefined(base2)){
        vec_Q_space.removeObjects( name);
        Redraw();
        return;
     }
     IThreeD_Object objects[] = new IThreeD_Object[ 1 ];
     Vector3D points[] = new Vector3D[3];

     points[0] = new Vector3D(origin);
     points[1]=new Vector3D(origin);
     points[2]=new Vector3D(origin);
     points[1].add(base1);
     points[2].add( base2);
     
     int type=Polymarker.CROSS;
     Color color = Color.pink;
     if(code==0){
        type=Polymarker.PLUS;
        color = Color.yellow;
     }else if( code ==1){
        type = Polymarker.STAR;
        color =  Color.lightGray;
     }
     Polymarker marker = new Polymarker( points, color);
     marker.setSize( 10 );
     
     
     marker.setType( type );
     objects[0] = marker;
     vec_Q_space.setObjects( name, objects );
     Redraw();
     
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
    System.out.println("FFTListener observer called for reason " + reason );
    System.out.println("DataSet = " + observed_object );
    if ( reason instanceof String && observed_object instanceof DataSet )
     if ( reason.equals( IObserver.POINTED_AT_CHANGED ) )
     {
       int index = ((DataSet)observed_object).getPointedAtIndex();
       Data d = ((DataSet)observed_object).getData_entry(index);
       Object value = d.getAttributeValue( NORMAL_ATTRIBUTE ); 
       if ( value == null )
         System.out.println( "ERROR: Missing normal " );

       if ( value instanceof float[] )
       {
         Vector3D normal = new Vector3D( (float[])value );
         System.out.println( normal );
       } 
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
         vec.multiply( 1/scale_factor );
         readout.setVector( vec );
       }
       catch ( Exception exception )
       {                       // just use the default scale factor 
       }
     }

     Redraw();
   }
 } 

  class JFrameListener extends WindowAdapter{

   /* (non-Javadoc)
    * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
    */
   @Override
   public void windowClosed( WindowEvent e ) {
       
       super.windowClosed( e );
       if( !JFrameOpen)
          if(e.getSource()instanceof JFrame)
             ((JFrame)e.getSource()).dispose();
       JFrameOpen = false;
   }
     
  }
  class HelpMenuListener implements ActionListener{
     String HelpFileName;
     
     public HelpMenuListener( String HelpFileName){
        if( HelpFileName != null &&
                    !HelpFileName.toUpperCase().startsWith( "FILE" )&&
                    !HelpFileName.toUpperCase().startsWith( "HTTP" )){
           java.io.File f = new java.io.File(HelpFileName);
           if( !f.exists()){
              HelpFileName = null;
              return;
           }
           try{
              HelpFileName  = f.toURI().toURL().toString();
           }catch( Exception s){
              HelpFileName = null;
           }
           
        }
        this.HelpFileName = HelpFileName;
     }
     
     public void actionPerformed( ActionEvent evt){
        IsawGUI.Browser B =(new IsawGUI.Browser( HelpFileName));
        if( B== null)
           return;
     }
  }
  
  /**
   * Manages the Information Menu item
   * @author Ruth
   *
   */
  private class InfoActionListener implements ActionListener{
     float MaxCrystalDistance = -1;
     Vector Peaks = null;
     
     /**
      * Sets the Vector of peaks for calculations required by some of
      * the options in the information menu item.
      * 
      * @param Peaks The Vector of Peaks to use
      */
     public void setPeaks( Vector Peaks){
        this.Peaks = Peaks;
     }
     
     /**
      * Set the maximum distance in real space for the distance between
      * any pair of parallel planes.
      * @param MaxCrystalDistance  The maximum distance in real space for 
      *       the distance between pairs of parallel planes.
      */
     public void setMaxXtalLength( float MaxCrystalDistance){
        this.MaxCrystalDistance = MaxCrystalDistance;
     }
     
     /**
      * Handles the operations from the Information menu
      */
     public void actionPerformed( ActionEvent evt){
        
        if( evt.getActionCommand() == LOAD_ORIENT){
           
           JFileChooser jf = new JFileChooser();
           if( jf.showOpenDialog( null )!= JFileChooser.APPROVE_OPTION)
              return;
           java.io.File file =  jf.getSelectedFile();
           Object O = 
              Operators.TOF_SCD.IndexJ.readOrient( file.toString());
           if( O==null ||
                 (O instanceof gov.anl.ipns.Util.SpecialStrings.ErrorString)){
                   
              JOptionPane.showMessageDialog( null , 
                       "Could not Load Orientation Matrix:"+O );
              
              return;
           }
           float[][]UB= (float[][])O;
           origin_vec.setVector( new Vector3D(0f,0f,0f) );
           a_star_vec.setVector( new Vector3D(UB[0][0],UB[1][0],UB[2][0]) );
           b_star_vec.setVector( new Vector3D(UB[0][1],UB[1][1],UB[2][1]) );
           c_star_vec.setVector( new Vector3D(UB[0][2],UB[1][2],UB[2][2]) );
           Redraw();
           
        }else if( evt.getActionCommand() == SHOW_RESULTS){
           if( Peaks == null || this.MaxCrystalDistance < 0)
              return;
           float[][] UnitNormals = getPlaneNormals();
           Vector Stats = new Vector(4);
           try{
              
              float[][]UB = null;

              float[][] dirs = get4Points();
              UB = CalcUB4(Peaks, dirs,Stats);
              /*if( Planes.isSelected())
                   UB =CalcUB(Peaks, UnitNormals, MaxCrystalDistance,  
                                                                Stats);
              else{
                  float[][] dirs = get4Points();
                  UB = CalcUB4(Peaks, dirs,Stats);
              }*/
              if(UB == null){
                 JOptionPane.showMessageDialog( null , "Error-Points collinear");
                 return;
              }
              String Message ="UB matrix\n";
              for( int i=0;i<3;i++)
                 Message +=UB[i][0]+" , "+UB[i][1]+" , "+UB[i][2]+"\n";
              Message +="\n Cell Parameters\n";
              double[] lats= Util.abc( LinearAlgebra.float2double( UB ) );
              if( lats != null && lats.length >=6)
              Message += lats[0]+"  "+lats[1]+"  "+lats[2]+"\n     "+lats[3]+" "+
                     lats[4]+"  "+lats[5]+"\n\n";
              Message +="\n Stats(Fraction indexed at given level)\n";
              if( Stats != null)
              for( int i=0; i< Math.min( 4, Stats.size()); i++)
                 Message += (10+10*i)+"%   :"+Stats.elementAt(i)+"\n";
              JOptionPane.showMessageDialog( null , Message );
                 
           }catch(Exception s){
              JOptionPane.showMessageDialog( null , "Error-"+s );
              s.printStackTrace();
              return;
           }
           
           
           
        }
     }
  }
  
  /**
   * Used only in a mode that calculates the UB matrix from plane
   * normals.
   * 
   * @param MaxDistance  The maximum distance between any two consecutive
   *     parallel planes.
   */
   public void setMaxCrystalDistance( float MaxDistance){
      
      if( actListener != null)
        actListener.setMaxXtalLength( MaxDistance );
   }
   
   
  /**
   * Static method to create operator to display the peaks and allow a user
   * to select three planes which will determine a UB matrix. 
   * 
   * The UB matrix is optimized and passed through Blind to put the 
   * basis Q vectors in a standard crystllographic form
   * 
   * @param Peaks   A Vector peaks
   * @param MaxXtallengthReal The maximum length of a side of a crystal
   *               in real space between
   *             
   * @param Stats  A return value where the elements represent the
   *    fraction of all peaks indexed within 10%,20%,30%,.. of all the planes
   *    defined by the calculated UB matrix
   *    
   * @return  a UB matrix or throws an IllegalArgumentException
   */
  public static float[][] GetUBFrRecipLattice( Vector Peaks,
                 float MaxXtallengthReal, Vector Stats){
     
     String HelpFileName = System.getProperty( "ISAW_HOME" );
     if( HelpFileName != null){
        HelpFileName = HelpFileName.replace( '\\','/' );
        if(!HelpFileName.endsWith( "/" ))
           HelpFileName +="/";
        HelpFileName +="IsawHelp/SCDRecipC.html";
     }
     SCDRecipLat RecipLat = new SCDRecipLat( HelpFileName, 1);
     RecipLat.setMaxCrystalDistance( MaxXtallengthReal );
     RecipLat.getSCDLat( Peaks , true );
     if( RecipLat.PlaneMode()){
       float[][] UnitNormals = RecipLat.getPlaneNormals();
     
       return RecipLat.CalcUB(Peaks, UnitNormals, MaxXtallengthReal, Stats);
     }else{// 4point mode
        float[][] Points = RecipLat.get4Points();
        return RecipLat.CalcUB4(Peaks,  Points, Stats );
        
     }
  }
  
  private  Vector3D add( Vector3D a, Vector3D b){
     if( a== null || b== null)
        return new Vector3D(0,0,0);
     Vector3D  V = new Vector3D(a);
     V.add( b );
     return V;
  }


  /**
   * Get the vectors to send into Blind.
   * @return Two dimensional array of floats with three rows.  The 
   *         three rows contain vectors a*, b* and c* to be sent in
   *         to Blind.
   */
  public float[][] get4Points(){
     if( a_star_vec == null || b_star_vec == null || c_star_vec==null ||
               origin_vec == null)
        return null;
     if( a_star_vec.getVector() == null || b_star_vec.getVector() == null ||
              c_star_vec.getVector()==null || origin_vec.getVector() == null)
       return null;
     float[][] Res = new float[3][3];
     Vector3D origin = origin_vec.getVector();
    
     Res[0]=a_star_vec.getVector().get();
     Res[1]=b_star_vec.getVector().get();
     Res[2]=c_star_vec.getVector().get();
     return Res;
  }
  
  public float[][] CalcUB4( Vector Peaks, float[][] Points, Vector Stats){
     
     float[][] Res = new float[3][3];
     if( Points == null || Points.length <3)
        return null;
     for( int i=0; i<3;i++)
        if( Points[i].length < 3)
        for( int j=0;j<3;j++)
           
           Res[j][i]= Points[i][j];
        else
           return null;
     
     IPNSSrc.blind Blind = new IPNSSrc.blind();
     Object R = Blind.blaue( Res );
     if( R instanceof ErrorString){
        JOptionPane.showMessageDialog( null , "Error "+R.toString());
        return null;
     }
     
     
    float[][]UB=  LinearAlgebra.double2float(  Blind.UB );
    updateStatsVector( Peaks, Stats, UB);
    return UB;
        
  }
  public boolean PlaneMode(){
     
     /*if( Planes == null)
        return false;
     if( Planes.isSelected())
        return true;
        */
     return false;
  }
  
  //Calculates the UB matrix from GetUB with the selected normals
  protected float[][] CalcUB( Vector Peaks, float[][] UnitNormals,
              float MaxXtallengthReal, Vector Stats){ 
     
     if( UnitNormals == null)
        throw new IllegalArgumentException("No Planes specified");
     if( UnitNormals.length < 3)
        throw new IllegalArgumentException("Not enough Planes specified");
     for( int i=0; i< 3;i++){
        
        if( UnitNormals[i]==null || UnitNormals[i].length < 3)
           throw new IllegalArgumentException("Not enough Planes specified");
        
        if( UnitNormals[i][2]< 0){
           UnitNormals[i][0]= -UnitNormals[i][0];
           UnitNormals[i][1]= -UnitNormals[i][1];
           UnitNormals[i][2]= -UnitNormals[i][2];
        }
     }
     
     float[][] Directions= new float[3][3];
     float[] line = new float[200];
     
     float[] Max = new float[3];
     for( int i=0; i< 3;i++){
        line=GetUB.ProjectPeakToDir( UnitNormals[i][0] , UnitNormals[i][1] , Peaks ,
                 null , MaxXtallengthReal , line );
        int minIndex=GetUB.findMinNonZero(line );
        int maxIndex = GetUB.findMaxNonZero( line );
        float[]Res= GetUB.CalcStats( line , minIndex , maxIndex );
        if( Res == null)
           throw new IllegalArgumentException("Direction "+i+"not good");
        
        float dSpaceRecip = Res[GetUB.LEN]/(48*MaxXtallengthReal);
        
        Directions[i]= new float[3];
        for( int j=0;j<3;j++){
           Directions[i][j]= dSpaceRecip*UnitNormals[i][j];
           if(Math.abs( Directions[i][j]) >Max[i])
              Max[i] =Math.abs( Directions[i][j]) ;
        }         
        
     }
     //Check if independent
     float det = (float)LinearAlgebra.determinant( 
                LinearAlgebra.float2double( Directions ));
     
     if( Math.abs( det )< .01*Max[0]*Max[1]*Max[2]){
        throw new IllegalArgumentException(
                 "The normals to the 3 planes are collinear");
        
     }
     
     
     boolean[] omit = new boolean[ Peaks.size()];
     java.util.Arrays.fill( omit , false );
     //Only eliminate even close outliers.
     GetUB.OmitPeaks( Peaks , Directions[0] , omit , .1f , true );
     GetUB.OmitPeaks( Peaks , Directions[1] , omit , .1f , true );
     GetUB.OmitPeaks( Peaks , Directions[2] , omit , .1f , true );
     float[] Statsfl= new float[4];
     float[][] UB =GetUB.UBMatrixFrPlanes( Directions, Peaks , omit , Statsfl 
                                                                 , 3 );
     
     if( UB == null){ 
        throw new IllegalArgumentException("Cannot Get a UB matrix. "+
                 "Planes not distinct directions or not enough points are " +
                 "close to the planes");
     }
     
     updateStatsVector( Peaks, Stats, UB);
     return UB;
  }
  
  private void updateStatsVector( Vector Peaks,  Vector Stats, float[][]UB){

     int n=4;
     if( Stats != null){
        Stats.clear();
        for( int i=0; i < n; i++){
           Stats.addElement(  GetUB.IndexStat( UB, Peaks,(i+1)*.1f, null));
        }
     }
  }
  
  public String drawObjects( int fillType, float size, java.awt.Color color, Vector pick_ids){
     
    int[] Type ={Polymarker.BOX,Polymarker.CROSS,Polymarker.DOT,
                    Polymarker.PLUS,Polymarker.STAR};
     if( DisplayMode !=1)
        return null;
     int Fill =-1;
     if( fillType >=0 && fillType < Type.length)
        Fill = Type[fillType];
     
     int Size = -1;
     if( size >.5 && size < 20)
        Size = (int)(size+.5);
     
     IThreeD_Object[] objs = vec_Q_space.getObjects( "Peaks" );
     if( objs != null &&( Fill >=0 || Size >= 1) ){
        
        for( int i=0;  i< objs.length; i++)
          if( objs[i] instanceof Polymarker){
              Polymarker P = (Polymarker)objs[i];
              if( Fill >=0)
                 P.setType(Fill);
              if( Size >=1)
                 P.setSize(  Size );
              
          
         }
      vec_Q_space.setObjects( "Peaks" , objs );
      Redraw();
     }
    
     return null;
  }
}
