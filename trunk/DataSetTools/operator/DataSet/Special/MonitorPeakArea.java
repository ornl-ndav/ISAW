/*
 * File:  MonitorPeakArea.java
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 * Revision 1.5  2004/01/24 19:41:13  bouzekc
 * Removed unused variables from main().  Removed unused imports.
 *
 * Revision 1.4  2003/01/13 17:33:44  dennis
 * Added getDocumentation() method, main test code and javadocs for
 * getResult().  (Chris Bouzek)
 *
 * Revision 1.3  2002/11/27 23:19:20  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/09/19 16:02:52  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.1  2002/02/22 21:03:43  pfpeterson
 * Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Special;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.peak.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;
import  DataSetTools.retriever.*;

/**
  *  This operator calculates the area under a peak in a specified monitor
  *  using a specified extent factor. The Group ID of the monitor to be
  *  integrated is specified by the parameter "Group ID".  The interval
  *  [a,b] over which the integration is determined based on the "extent
  *  factor".  Specifically, the peak is integrated over the interval of
  *  length (extent_factor * FWHM) centered on the peak.
  *  This operator just produces a numerical result that is displayed
  *  in the operator dialog box.
  */

public class MonitorPeakArea  extends    DS_Special
                              implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public MonitorPeakArea( )
  {
    super( "Monitor Peak Area" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator to find the area under a monitor peak for
   *  the specified monitor DataSet using the specified group_id and
   *  extent factor.
   *
   *  @param  ds              The monitor DataSet from which a peak area is to
   *                          be found.
   *  @param  group_id        The group_id of the monitor Data block that is
   *                          to be integrated.
   *  @param  extent_factor   The peak is integrated over an interval of
   *                          length extent_factor*FWHM centered on the peak.
   */

  public MonitorPeakArea( DataSet      ds,
                          int          group_id,
                          float        extent_factor )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters
    IParameter parameter = getParameter(0);
    parameter.setValue( new Integer( group_id ) );

    parameter = getParameter( 1 );
    parameter.setValue( new Float( extent_factor ) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   *         in this case, PeakA
   */
   public String getCommand()
   {
     return "PeakA";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Monitor Group ID",new Integer(1));
    addParameter( parameter );

    parameter = new Parameter("Extent factor", new Float(8.5));
    addParameter( parameter );

  }

  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator calculates the area under a peak in ");
    s.append("a specified monitor using a specified extent factor.\n");
    s.append("@assumptions The specified group ID corresponds to a valid ");
    s.append("data entry.\n");
    s.append("@algorithm First this operator acquires the data entry ");
    s.append("corresponding to the group_id parameter.\n");
    s.append("Then it uses the extent_factor parameter to determine over ");
    s.append("which interval to integrate.\n");
    s.append("Finally it calculates the area by integration.\n");
    s.append("@param ds The monitor DataSet from which a peak area is to ");
    s.append("be found.\n");
    s.append("@param group_id The group_id of the monitor Data block that ");
    s.append("is to be integrated.\n");
    s.append("@param extent_factor The peak is integrated over an interval ");
    s.append("of length extent_factor*FWHM centered on the peak.\n");
    s.append("@return Float object which represents the area under the ");
    s.append("specified peak.\n");
    s.append("@error Returns an error if the specified group ID does not ");
    s.append("correspond to a valid data entry.\n");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   *  Calculates the area under a peak based on a group ID and an extent
   *  factor.
   *
   *  @return Float object which represents the area under the specified peak.
   */
  public Object getResult()
  {                                  // get the parameters

    int group_id        = ( (Integer)(getParameter(0).getValue()) ).intValue();
    float extent_factor = ( (Float)(getParameter(1).getValue()) ).floatValue();

                                     // get the current data set and do the
                                     // operation
    DataSet ds = this.getDataSet();

    Data data = ds.getData_entry_with_id( group_id );
    if ( data == null )
    {
      ErrorString message = new ErrorString(
                           "ERROR: no data entry with the group_ID "+group_id );
      System.out.println( message );
      return message;
    }
    else
    {
      HistogramDataPeak peak = new HistogramDataPeak( data, extent_factor );
      peak.setEvaluationMode( IPeak.PEAK_ONLY );
      float result = peak.Area();
      return new Float( result );
    }
  }

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current MonitorPeakArea Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    MonitorPeakArea new_op = new MonitorPeakArea( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

  /* --------------------------- main ----------------------------------- */
  /*
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
    DataSet ds1 = null;
    int groupID;
    float extentFactor;
    StringBuffer m = new StringBuffer();
    MonitorPeakArea op;

    groupID = 26;
    extentFactor = (float)10.0;

    String file_name = "/home/groups/SCD_PROJECT/SampleRuns/hrcs2447.run ";
                       //"D:\\ISAW\\SampleRuns\\hrcs2447.run";

    try
    {
      RunfileRetriever rr = new RunfileRetriever( file_name );
      ds1 = rr.getDataSet(1);
    }
    catch(Exception e)
    {
      System.err.println("Error opening file");
    }

    op = new MonitorPeakArea(ds1, groupID, extentFactor);

    Object result = op.getResult();
    m.append("\nThe results of calling getResult() for ");
    m.append("FocusIncidentSpectrum for file ");
    m.append(file_name);
    m.append(" on a group ID of ");
    m.append(groupID);
    m.append(" and an extent factor of ");
    m.append(extentFactor);
    m.append(" are:\n\n");
    m.append(result.toString());
    m.append("\n\nThe results of calling getDocumentation() for ");
    m.append("FocusIncidentSpectrum are:\n\n");
    m.append(op.getDocumentation());

    System.out.print(m.toString());
  }

}
