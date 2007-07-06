/*
 * File:  UpstreamMonitorID.java
 *
 * Copyright (C) 2002, Peter F. Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.9  2007/07/06 19:33:36  dennis
 *  Now calls static method from MonitorID_Calc class to do the
 *  actual work.
 *
 *  Revision 1.8  2007/07/04 20:29:17  dennis
 *  Removed unused import.
 *
 *  Revision 1.7  2007/07/04 20:21:03  dennis
 *  Added missing import.
 *
 *  Revision 1.6  2007/07/04 20:16:50  dennis
 *  Now will use the DetectorPosition attribute to determine the angle
 *  from the beam to the detector, if the RAW_ANGLE attribute is not
 *  present.  Also, a tolerance of about 2.5 degrees off from the beam
 *  direction is allowed for the monitor position, before it is discarded
 *  as not being a beam monitor.
 *  Made some improvments to the javadocs.
 *
 *  Revision 1.5  2005/08/24 20:29:22  dennis
 *  Added/Moved to menu DATA_SET_INFO_MACROS
 *
 *  Revision 1.4  2003/12/15 02:38:18  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.3  2003/01/23 19:21:03  dennis
 *  Added getDocumentation() method. (Chris Bouzek)
 *
 *  Revision 1.2  2002/11/27 23:21:43  pfpeterson
 *  standardized header
 *
 *  Revision 1.1  2002/05/24 14:21:23  pfpeterson
 *  added to cvs
 */

package DataSetTools.operator.Generic.Special;

import java.util.Vector;

import DataSetTools.dataset.DataSet;
import DataSetTools.operator.Operator;
import DataSetTools.operator.Parameter;
import DataSetTools.retriever.RunfileRetriever;

/**
 * This operator determines what the group ID of the upstream monitor
 * is for a given monitor dataset.
 */

public class UpstreamMonitorID extends    GenericSpecial {

    /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
    /**
     * Construct an operator with a default parameter list.
     */
    public UpstreamMonitorID( ){
        super( "Upstream Monitor GID" );
    }

    /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
    /**
     *  Construct operator to determine the upstream monitor group id.
     *
     *  @param  ds     The monitor data set.
     */

    public UpstreamMonitorID( DataSet ds ){
        this();

        parameters=new Vector();
        addParameter(new Parameter("Monitor",ds));
    }


    /* ------------------------- setDefaultParameters ----------------------- */
    /**
     *  Set the parameters to default values.
     */
    public void setDefaultParameters(){
        parameters = new Vector();  // must do this to create empty list of
                                    // parameters

        parameters=new Vector();
        addParameter( new Parameter("Monitor",DataSet.EMPTY_DATA_SET ));
    }

    /* --------------------------- getCommand ------------------------------ */
    /**
     * @return the command name to be used with script processor, UpMonitorID
     */
    public String getCommand(){
        return "UpMonitorID";
    }


   /* ---------------------------- getCategoryList -------------------------- */
   /** 
    *  Get the list of categories describing where this operator should appear
    *  in the menu system.
    *
    *  @return an array of strings listing the menu where the operator 
    *  should appear.
    */
    public String[] getCategoryList()
    {
      return Operator.DATA_SET_INFO_MACROS;
    }


    /* ---------------------- getDocumentation --------------------------- */
    /**
     *  Returns the documentation for this method as a String.  The format
     *  follows standard JavaDoc conventions.
     */
    public String getDocumentation()
    {
      StringBuffer s = new StringBuffer("");
      s.append("@overview  This operator sarches through the specified ");
      s.append("DataSet for the upstream monitor data entry with ");
      s.append("the highest total count.  The DataSet should contain only ");
      s.append("monitor Data, and the corresponding detector positions ");
      s.append("must be along the beam.  Specifically, any group that is ");
      s.append("more than about 2.5 degrees away from the beam, when ");
      s.append("viewed from the sample position will NOT be considered ");
      s.append("to be a monitor.  An upstream monitor is assumed ");
      s.append("to be before the sample, so its position is ");
      s.append("essentially at 180 degrees from the beam direction. /n");

      s.append("@assumptions The DataSet contains only monitor Data and ");
      s.append("at least one upstream monitor exists in the DataSet. ");

      s.append("@algorithm Searches through the specified DataSet ds for ");
      s.append("the histogram of the upstream monitor with the highest ");
      s.append("total count.\n");
      s.append("It then determines the group ID for that data entry.\n");

      s.append("@param ds The monitor DataSet to be used for the ");
      s.append("operator.\n");

      s.append("@return Integer representing the group ID of the histogram ");
      s.append("of the upstream monitor with the largest total count ");
      s.append("attribute.\n");

      s.append("@error Returns -1 if no upstream monitor is found.\n");
      return s.toString();
    }

    /* --------------------------- getResult ------------------------------- */
    /*
     * This operator sarches through the specified DataSet for the upstream 
     * monitor data entry with the highest total count.  The DataSet should
     * contain only monitor Data, and the corresponding detector positions must
     * be along the beam.  Specifically, any group that is more than about
     * 2.5 degrees away from the beam, when viewed from the sample position
     * will NOT be considered to be a monitor.  An upstream monitor is assumed
     * to be before the sample, so its position is essentially 180 degrees
     * from the beam direction.
     *
     * @return Integer Object with the Group ID of the histogram of the 
     * upstream monitor with the largest TOTAL_COUNT attribute. If no 
     * upstream monitor is found it will return -1.
     */
    public Object getResult()
    {
      DataSet mon = (DataSet)(getParameter(0).getValue());
      return new Integer( MonitorID_Calc.UpstreamMonitorID( mon ) );
    }

    /* ------------------------------ clone ------------------------------- */
    /**
     * Get a copy of this UpstreamMonitorID Operator.  The
     * list of parameters is also copied.
     */
    public Object clone(){

        UpstreamMonitorID new_op = new UpstreamMonitorID( );
        new_op.CopyParametersFrom( this );
        return new_op;
    }


    /* ------------------------------ main ------------------------------- */
    /**
     * Main method for testing purposes.
     */
    public static void main(String[] args){
        if(args.length==1){
            String filename=args[0];
            RunfileRetriever rr=new RunfileRetriever(filename);
            DataSet mds=rr.getDataSet(0);

            //UpstreamMonitorID op=new UpstreamMonitorID();
            UpstreamMonitorID op=new UpstreamMonitorID(mds);
            System.out.println("For "+filename+" monitor GID= "
                               +op.getResult());
            /* ----------- added by Chris Bouzek ------------ */
            System.out.println("Documentation: " + op.getResult());
        }
        else{
            UpstreamMonitorID op=new UpstreamMonitorID();
            System.out.println("USAGE: UpstreamMonitorID <filename>");
            /* ----------- added by Chris Bouzek ------------ */
            System.out.println("Documentation: " + op.getDocumentation());     
        }

    }
}
