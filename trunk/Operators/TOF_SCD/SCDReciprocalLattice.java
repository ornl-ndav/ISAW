/*
 * File:  SCDReciprocalLattice.java 
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
 * Revision 1.5  2003/04/02 15:18:02  dennis
 * Added getDocumentation() method. (Joshua Olson)
 *
 * Revision 1.4  2003/02/18 20:23:52  dennis
 * Switched to use SampleOrientation attribute instead of separate
 * phi, chi and omega values.
 *
 * Revision 1.3  2002/11/27 23:31:01  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/09/19 15:58:49  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.1  2002/08/02 22:51:07  dennis
 * Very crude, marginally useful form of reciprocal lattice viewer in
 * the form of an operator.
 *
 *
 */
package Operators.TOF_SCD;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.parameter.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.dataset.*;
import DataSetTools.components.image.*;
import DataSetTools.components.ThreeD.*;
import DataSetTools.math.*;
import DataSetTools.instruments.*;
import DataSetTools.util.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

/** 
 *    This operator reads through a sequence of SCD run files and constructs
 *  a basic view of the peaks in 3D "Q" space.
 */

public class SCDReciprocalLattice extends GenericTOF_SCD
{
  private static final String TITLE = "SCD reciprocal lattice";

 /* ------------------------- DefaultConstructor -------------------------- */
 /** 
  */  
  public SCDReciprocalLattice()
  {
    super( TITLE );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for with the specified parameter values so
   *  that the operation can be invoked immediately by calling getResult().
   *
   *  @param  path        The directory path to the data directory
   *  @param  run_numbers A list of run numbers to be loaded
   */
   public SCDReciprocalLattice(  DataDirectoryString    path,
                                 IntListString          runs  )
   {
      super( TITLE );

      IParameter parameter = getParameter(0);
      parameter.setValue( path );

      parameter = getParameter(1);
      parameter.setValue( runs );
   }


 /* ------------------------------ getCommand ----------------------------- */
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "RecipLattice", the command used to invoke this operator in Scripts
  */
  public String getCommand()
  {
    return "RecipLattice";
  }

 /* ------------------------ setDefaultParameters ------------------------- */
 /** 
  * Sets default values for the parameters.  The parameters set must match the 
  * data types of the parameters used in the constructor.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Path to runfiles:",
                          new DataDirectoryString("") );
    addParameter( parameter );

    parameter = new Parameter("List of run numbers",
                               new IntListString("") );
    addParameter( parameter );
  }

/* ------------------------------ draw_axes ----------------------------- */

private static void draw_axes( float length, ThreeD_JPanel threeD_panel  )
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

 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */                                                                 				         
  public String getDocumentation()
  {                                                                                                               
    StringBuffer s = new StringBuffer("");                                                 
    s.append("@overview This operator reads through a sequence of SCD run ");
    s.append("files and constructs a basic view of the peaks in 3D \"Q\" ");
    s.append("space.");    
    s.append("@assumptions All the run numbers in 'run_numbers' can be used ");
    s.append("to create a file in the directory 'path'.\n");
    s.append("All the files can be used to create a non-empty DataSet. \n");
    s.append("@algorithm The operator stores the run numbers. A file name");
    s.append(" is created for each run number. \n\n ");    
    s.append("The following is done for each file name: \n ");
    s.append("a) A file is created for the file name.  A DataSet is ");
    s.append("constructed from this file.  If the DataSet is empty then an ");
    s.append("error string is returned and execution of the operator ");
    s.append("terminates.  Otherwise the operator continues. \n ");
    s.append("b) The first entry of Data in the DataSet is stored.  The ");
    s.append("goniometer angles (phi, chi and omega) of the Data are printed ");
    s.append("to the console. \n\n");    
    s.append("The following is done for each entry of Data (in each DataSet):");                       
    s.append("\n a) The Data's DetectorPosition and initial path are stored.");
    s.append("\n b) All the x's on the X Scale are stored in an array.");
    s.append("\n c) All the y's are stored in an array.\n\n");       
    s.append("The following is done for each y value in the Data: \n ");
    s.append("The y's are compared to a threshold that varies with the t.o.f.");
    s.append(" to determine where peaks in the diffraction pattern occur.\n\n");
    s.append("For each y value that has a peak, the following is done: \n ");
    s.append("a) A color is determined based upon the height of the peak.\n"); 
    s.append("b) This peak's position is currently expressed in terms of time"); 
    s.append(" of flight.  The operator translates this position so that it ");
    s.append("is expressed in terms of a 3D Reciprocal Lattice space.\n");
    s.append("c) A PolyMarker object is constructed from the new coordinates ");
    s.append("and the color.  The PolyMarker is stored.\n\n"); 
    s.append("So the operator has now obtained all the peaks (Polymarker ");
    s.append("objects) for this file.\n");
    s.append("The total number of peaks for this file is printed to the ");
    s.append("console.\n");
    s.append("Then the peaks are displayed in a 3D scatter plot.");     
    s.append("\n\n The operator does this for every file.  Then the string ");
    s.append("'Done' is returned. ");
    s.append("\n\n\n");    
    s.append("To summarize the 'Algorithm': \n ");
    s.append("A file is created in the directory for each run number. \n ");
    s.append("Each file is used to create a DataSet. \n ");
    s.append("Each Data entry (in every DataSet) has x values and y values.\n");
    s.append("Some of those y values have peaks.  \n ");
    s.append("All the peaks for all the files are displayed as a 3D scatter ");
    s.append("plot.");  
    s.append("@param  path The directory path to the data directory ");
    s.append("@param  run_numbers A list of run numbers to be loaded");	           
    s.append("@return If successful, it returns the string 'Done'. ");  
    s.append("@error Returns an error string if a DataSet cannot be  ");
    s.append("constructed from any particular file. ");
    return s.toString();
  }

 /* ------------------------------ getResult ------------------------------- */
 /** 
  *  Executes this operator using the current values of the parameters.
  *
  */
  public Object getResult()
  {
    String path =
                 ((DataDirectoryString)getParameter(0).getValue()).toString();
    String run_nums =
                 ((IntListString)getParameter(1).getValue()).toString();

    int runs[] = IntList.ToArray( run_nums );
    String file_names[] = new String[ runs.length ];
    for ( int i = 0; i < runs.length; i++ )
     file_names[i] = path + InstrumentType.formIPNSFileName("scd",runs[i]);

    JFrame scene_f = new JFrame("Test for ThreeD_JPanel");
    scene_f.setBounds(0,0,500,500);
    ThreeD_JPanel vec_Q_space = new ThreeD_JPanel();
    vec_Q_space.setBackground( new Color( 90, 90, 90 ) );
    scene_f.getContentPane().add( vec_Q_space );
    scene_f.setVisible( true );

    JFrame controller_f = new JFrame("Q_space Controller");
    controller_f.setBounds(0,0,200,200);
    AltAzController controller = new AltAzController();
    controller_f.getContentPane().add( controller );
    controller_f.setVisible( true );

    controller.setDistanceRange( 0.1f, 500 );
    controller.addControlledPanel( vec_Q_space );
    for ( int count = 0; count < file_names.length; count++ )
    {
    System.out.println("Loading file: " + file_names[count]);
    RunfileRetriever rr = new RunfileRetriever( file_names[count] );
    DataSet ds = rr.getDataSet( 1 );
    if ( ds == null )
      return new ErrorString("File not found: " + file_names[count]);
    Data    d  = ds.getData_entry(0);

    SampleOrientation orientation =
        (SampleOrientation)d.getAttributeValue(Attribute.SAMPLE_ORIENTATION);

    Tran3D combinedR = orientation.getGoniometerRotationInverse();

    System.out.println("phi, chi, omega = " + orientation.getPhi() +
                                       ", " + orientation.getChi() +
                                       ", " + orientation.getOmega() );
    int n_data = ds.getNum_entries();
    int n_bins = d.getX_scale().getNum_x() - 1;

    int n_objects         = n_data * n_bins;
    IThreeD_Object objs[] = new IThreeD_Object[n_objects];
    Vector3D       pts[]  = new Vector3D[1];
    pts[0]                = new Vector3D();
    float t;
    float cart_coords[];

    Color colors[] = IndexColorMaker.getColorTable(
                                IndexColorMaker.HEATED_OBJECT_SCALE, 128 );

    int obj_index = 0;
    for ( int i = 0; i < n_data; i++ )
    {
      d = ds.getData_entry(i);
      DetectorPosition pos = (DetectorPosition)
                              d.getAttributeValue( Attribute.DETECTOR_POS ); 
      float initial_path = ((Float)d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
      float times[] = d.getX_scale().getXs();
      float ys[]    = d.getY_values();
      for ( int j = 0; j < ys.length; j++ )
      {
        float tof_weight = (float)Math.sqrt(times[n_bins]/times[j]);
        if ( ys[j] > 35/tof_weight )
        {
          t = (times[j] + times[j+1]) / 2;
          Position3D q_pos = tof_calc.DiffractometerVecQ(pos, initial_path, t);

          cart_coords = q_pos.getCartesianCoords();
          pts[0].set( cart_coords[0], cart_coords[1], cart_coords[2] );
          combinedR.apply_to( pts[0], pts[0] );

          int color_index = (int)(ys[j]*tof_weight/100 * 127.0f);
          if ( color_index > 127 )
            color_index = 127;
          Color c = colors[ color_index ];
          objs[obj_index] = new Polymarker( pts, c );
          obj_index++;
        }
      }
    }
    System.out.println("Number of points = " + obj_index );
    IThreeD_Object non_zero_objs[] = new IThreeD_Object[obj_index];
    for ( int i = 0; i < obj_index; i++ )
      non_zero_objs[i] = objs[i];

    vec_Q_space.setObjects( file_names[count], non_zero_objs );
    draw_axes(1, vec_Q_space );
   }
     
    return "Done";
  }

 /* --------------------------------- clone -------------------------------- */
 /** 
  *  Creates a clone of this operator.  ( Operators need a clone method, so 
  *  that Isaw can make copies of them when needed. )
  */
  public Object clone()
  { 
    SCDReciprocalLattice op = new SCDReciprocalLattice();
    op.CopyParametersFrom( this );
    return op;
  }

 /* ------------------------------- main ----------------------------------- */
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
    System.out.println("LoadExpression (trivial test).");
  }
}
