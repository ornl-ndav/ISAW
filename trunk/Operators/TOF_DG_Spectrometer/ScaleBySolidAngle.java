/*
 * File:  ScaleBySolidAngle.java
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
 * Revision 1.1  2004/08/23 20:32:02  dennis
 * Operator to multiply each spectrum by S/T where S is the solid
 * angle of the detector element(s) for that spectrum and T is
 * the total solid angle.
 *
 */
package Operators.TOF_DG_Spectrometer;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Messaging.*;

public class ScaleBySolidAngle implements Wrappable
{
  public DataSet   ds          = DataSet.EMPTY_DATA_SET;
  public boolean   make_new_ds = false;

  /**
   *  Get the command name for this operator
   *
   *  @return The command name: "ScaleBySolidAngle"
   */
  public String getCommand() 
  {
    return "ScaleBySolidAngle";
  }

  /**
   *  Get the documentation for this operator
   *
   *  @return String explaining the use of this operator
   */
  public String getDocumentation() 
  {
    StringBuffer s = new StringBuffer(  );
  
    s.append("@overview This operator multiplies each DataBlock in this ");
    s.append("DataSet by S/T where S is the solid angle for the detector ");
    s.append("pixel for the Data block and T is the total solid angle of ");
    s.append("all pixels.");

    s.append("@assumptions The Data blocks must have the solid angle ");
    s.append("for their detector pixel, recorded as an attribute. ");

    s.append("@algorithm  The operator first gets the solid angle for ");
    s.append("for each Data block, saves it in a table and computes the ");
    s.append("total solid angle.  Next the y values for each Data block ");
    s.append("are multiplied by S/T.");

    s.append("@return If successful, it returns the string 'Success'. ");
    s.append("@error Returns an error string if any Data blocks doesn't have ");
    s.append("a solid angle attribute.");

    return s.toString(  );
  }

  /**
   *  Restrict the DataSet to the specified domain.
   */
  public Object calculate() 
  {
    DataSet new_ds;

    if ( make_new_ds )
      new_ds = (DataSet)ds.clone();
    else
      new_ds = ds;
                                                 // get all of the solid angles
    int n_data = new_ds.getNum_entries();
    float solid_angle[] = new float[ n_data ];
    float total = 0;
    for ( int i = 0; i < n_data; i++ )
    {
      solid_angle[i] = new_ds.getData_entry(i).getSolidAngle();
      if ( Float.isNaN( solid_angle[i] ) )
        return new ErrorString("Missing solid angle on Data block " + i );

      total += solid_angle[i];
    } 
         
    if ( total <= 0 )
      return new ErrorString("Total solid angle is not positive: " + total );

    SharedData.addmsg("Total solid angle = " + total );

    for ( int i = 0; i < n_data; i++ )
    {
      Data d = new_ds.getData_entry(i);
      float scale = solid_angle[i]/total;
      if ( d instanceof TabulatedData )       // just multiply, since then we
      {                                       // don't need to clone attributes
        float y[] = d.getY_values();
        for ( int j = 0; j < y.length; j++ )
          y[j] *= scale;
      }
      else                                    // use the Data multiply method
      {                                       // to produce a new Data object
        d = d.multiply( scale, 0 );
        new_ds.replaceData_entry( d, i );
      }
    }

    new_ds.addLog_entry("Scaled Data entries by SolidAngle/TotalSolidAngle");

    if ( make_new_ds )
      return new_ds;
    else
    {
      ds.notifyIObservers( IObserver.DATA_CHANGED );
      return "Success";
    }
  }
}
