/*
 * File:  SCDReciprocalLattice.java
 *
 * Copyright (C) 2004 Dennis Mikkelson 
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
 * Revision 1.11  2004/08/11 21:33:14  dennis
 * Now checks if files loaded ok and returns an error string if not.
 * Returns "Success" if files are loaded ok.
 *
 * Revision 1.10  2004/07/30 13:28:01  dennis
 * Now calls viewer.SetThresholdScale before finding peaks.  This
 * fixes a problem with handling files with a large number of counts.
 * Previously too many peaks would be found in such data sets.
 *
 * Revision 1.9  2004/07/28 16:50:29  dennis
 * Major rewrite using the GL_RecipPlaneView to display the
 * reciprocal lattice.  Also fixed getDocumentation().
 *
 */
package Operators.TOF_SCD;

import DataSetTools.trial.*;
import DataSetTools.operator.*;
import gov.anl.ipns.Util.SpecialStrings.*;

public class SCDReciprocalLattice implements Wrappable
{
  public DataDirectoryString Data_Directory     = new DataDirectoryString();
  public LoadFileString      Calibration_File   = new LoadFileString();
  public LoadFileString      Orientation_Matrix = new LoadFileString(); 
  public IntListString       Run_Numbers        = new IntListString("8336"); 
  public int                 Threshold_Level    = 60;
  public boolean             Show_Contours      = false;
  public float               Contour_Level      = 50;
  public boolean             Show_Regions       = false;
  public boolean             Show_HKL_Marks     = false;
  public boolean             Calculate_FFTs     = false;

  /**
   *  Get the command name for this operator
   *
   *  @return The command name: "SCDReciprocalLattice"
   */
  public String getCommand() 
  {
    return "SCDReciprocalLattice";
  }

  /**
   *  Get the documentation for this operator
   *
   *  @return String explaining the use of this operator
   */
  public String getDocumentation() 
  {
    StringBuffer s = new StringBuffer(  );
  
    s.append("@overview This operator reads through a sequence of SCD run ");
    s.append("files and constructs a view of the peaks in 3D \"Q\" ");
    s.append("space.");

    s.append("@assumptions All the run numbers in 'Run_Numbers' correspond ");
    s.append("to valid SCD files in the directory 'path'.\n");

    s.append("@algorithm  The operator loads all of the specified ");
    s.append("run files.  If a calibration file was specified, the ");
    s.append("calibration information will be applied to the detector ");
    s.append("positions.  Bins that are above the specified threshold ");
    s.append("will be marked with a voxel with a color corresponding ");
    s.append("to the data value.  Iso-surfaces can be drawn as contour ");
    s.append("lines if requested.  If an orientation matrix is specified ");
    s.append("marks can be placed at integer hkl points.  ");
    s.append("Also, a wire frame outline of the region of Q space ");
    s.append("covered by each detector can be drawn.  If an orientation "); 
    s.append("matrix is provided h,k,l axes will be drawn, otherwise ");
    s.append("Qx, Qy, Qz axes are drawn.");

    s.append("@param  Data_Directory The directory path to the data directory");
    s.append("@param  Calibration_File The calibration file (instprm.dat)");
    s.append("@param  Orientation_Matrix The orientation matrix file");
    s.append("@param  Run_Numbers A list of run numbers to be loaded"); 
    s.append("@param  Threshold_Level Minimum value to count as a peak");
    s.append("@param  Show_Contours Flag to enable drawing iso-surfaces");
    s.append("@param  Contour_Level Data value at which iso-surface is drawn");
    s.append("@param  Show_Regions  Flag to enable drawing detector coverage");
    s.append("@param  Show_HKL_Marks  Flag to enable marking integer hkl");
    s.append("@param  Calculate_FFTs  Flag to enable calculating FFT of");
    s.append("@param  projections on lines in various directions to ");
    s.append("@param  help find families of planes.");

    s.append("@return If successful, it returns the string 'Done'. ");
    s.append("@error Returns an error string if a DataSet cannot be  ");
    s.append("constructed from any particular file. ");

    return s.toString(  );
  }

  /**
   *  Restrict the DataSet to the specified domain.
   */
  public Object calculate() 
  {
/*
    System.out.println( Data_Directory.toString() );
    System.out.println( Calibration_File.toString() );
    System.out.println( Orientation_Matrix.toString() );
    System.out.println( Run_Numbers.toString() );
    System.out.println( Threshold_Level );
    System.out.println( Show_Contours );
    System.out.println( Contour_Level );
    System.out.println( Show_Regions ); 
    System.out.println( Show_HKL_Marks );
    System.out.println( Calculate_FFTs );
*/
    GL_RecipPlaneView viewer = 
        new GL_RecipPlaneView( Data_Directory.toString(),
                               Run_Numbers.toString(),
                               Calibration_File.toString(),
                               Orientation_Matrix.toString() );

    viewer.SetThresholdScale( Threshold_Level );

    if ( !viewer.loadFiles() )
      return new ErrorString("Couldn't load files");
    viewer.initialize( true );
    viewer.ShowContours( Show_Contours, Contour_Level );
    viewer.ShowBoundaries( Show_Regions );
    viewer.ShowHKL_Marks( Show_HKL_Marks );
    if ( Calculate_FFTs )
      viewer.CalculateFFTs();

    return "Success";
  }
}
