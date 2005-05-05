/*
 * File:  GLADQ2R.java
 *
 * Copyright (C) 2005 J. Tao
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
 * Contact : Julian Tao <taoj@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 *
 * Modified:
 * $Log$
 * Revision 1.1  2005/05/05 02:06:10  taoj
 * added into the cvs
 *
 * Revision 1.1  2005/02/15 17:45:11  taoj
 * test version.
 *
 */

package Operators.TOF_Diffractometer;

import Operators.TOF_Diffractometer.Ftr;
import DataSetTools.operator.Wrappable;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;

/**
 * This class uses Ftr.java to convert I(Q) to S(Q) and preform the Fourier transformation from S(Q)
 * to various distribution functions. It corresponds to the FTR routine on GLAD.
 */
public class GLADQ2R implements Wrappable {
  //~ Instance fields **********************************************************
  
  private boolean DEBUG = false;
  /* @param ioq_smp sample IofQ dataset;
   * 
   */
  public DataSet ds0;
  public DataSet ioq_smp;
  public float QCut = 25.0f;
  public float NumberDensity;
  
  //~ Methods ******************************************************************

  /**
   * @return The script name for this Operator.
   */
  public String getCommand(  ) {
    return "GLAD_Q2R";
  }

  /**
   * Returns the documentation for this method as a String.  The format follows
   * standard JavaDoc conventions.
   */
  public String getDocumentation(  ) {
    StringBuffer s = new StringBuffer( "" );
    s.append( "@overview This operator removes detectors from a DataSet " );
    s.append( "according to three criteria, all of which involve the total " );
    s.append( "counts.\n" );
    s.append( "@assumptions The specified DataSet ds is not null.\n" );
    s.append( "@algorithm First this operator removes detectors with zero " );
    s.append( "counts from the specified DataSet. Next it removes detectors " );
    s.append( "below the user specified threshold. Finally the average and " );
    s.append( "standard deviation is found for the total counts, then " );
    s.append( "detectors outside of the user specified number of sigma are " );
    s.append( "removed (generally too many counts).  It also appends a log " );
    s.append( "message indicating that the Crunch operator was applied to " );
    s.append( "the DataSet.\n" );
    s.append( "@param ds Sample DataSet to remove dead detectors from.\n" );
    s.append( "@param min_count Minimum counts to keep.\n" );
    s.append( "@param width How many sigma around the average to keep.\n" );
    s.append( "@param new_ds Whether to make a new DataSet.\n" );
    s.append( "@return DataSet containing the the original DataSet minus the " );
    s.append( "dead detectors.\n" );
    s.append( "@error Returns an error if the specified DataSet ds is null.\n" );

    return s.toString(  );
  }


  
  /**
   * Removes dead detectors from the specified DataSet.
   *
   * @return The crunched DataSet.
   */
  public Object calculate(  ) {    
    
    System.out.println("Extracting IofQ...");
    GLADScatter smprun = (GLADScatter)((Object[])ds0.getAttributeValue(GLADRunProps.GLAD_PROP))[2]; 
    float bbarsq = smprun.bbarsq;
    if (NumberDensity != 0.0f) smprun.density = NumberDensity;
    Data dioq = ioq_smp.getData_entry(ioq_smp.getNum_entries()-1);
    Ftr f = new Ftr(dioq, bbarsq);    
    f.calculateDofR(QCut, 0, smprun.density);
    
    DataSet ds = new DataSet ("DS", 
       "Construct a dataset holding the distribution functions.",
       "1/Angstrom", "Q",
       "", "");

    
    float fofrs[][];
    fofrs = f.getTofR();
//    StringBuffer output = new StringBuffer("GofR:\n");
//    String entry;
 
    for (int i = 0; i<fofrs[0].length; i++){
      if (i<1100) {
//        entry= "["+gofrs[0][i]+","+gofrs[1][i]+"]"+",";
//        output.append(entry);
        System.out.println("r:\t"+fofrs[0][i]+"\t"+"tor:\t"+fofrs[1][i]);
      } 
//             System.out.println("r: "+dors[0][i]+" Dr: "+dors[1][i]);
    }
//    System.out.println(output);     

    return null;
  }    

}


