/*
 * File:  TimeFocus.java 
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 * Revision 1.6  2003/02/28 15:14:37  dennis
 * Added getDocumentation() method.  (Shannon Hintzman)
 *
 * Revision 1.5  2003/02/24 21:05:17  pfpeterson
 * Changed to use IParameterGUI rather than IParameter.
 *
 * Revision 1.4  2002/11/27 23:30:47  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/07/10 15:52:07  pfpeterson
 * Uses information from gsas calibration from attributes if present.
 *
 * Revision 1.2  2002/07/08 15:44:34  pfpeterson
 * Now uses tof_data_calc for the calculation.
 *
 * Revision 1.1  2002/05/31 19:26:21  dennis
 * Basic time focusing for diffractometers.
 *
 *
 */
package Operators.TOF_Diffractometer;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.TOF_Diffractometer.*;
import DataSetTools.operator.DataSet.EditList.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;
import DataSetTools.math.*;
import java.util.*;
import DataSetTools.gsastools.GsasCalib;
import DataSetTools.parameter.*;

/** 
 *  This operator will focus one or more spectra in a DataSet to a specified
 *  scattering angle, using the ratio L'*sin(theta') / L*sin(theta).  The
 *  current total flight path length is L and half the scattering angle is 
 *  theta.  L' and theta' are the new path length and angle.
 */
public class TimeFocus extends GenericTOF_Diffractometer
{
  private static final String TITLE = "Time Focus";

 /* ------------------------ Default constructor ------------------------- */ 
 /** 
  *  Creates operator with title "Time Focus" and a default 
  *  list of parameters.
  */  
  public TimeFocus()
  {
    super( TITLE );
  }

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  Construct a TimeFocus operator that focuses the specified groups
  *  in a neutron Time-of-flight diffractometer DataSet to the specified 
  *  angle and final flight path length.  Note: After focussing, the detector
  *  position will be in the "scattering plane" at a distance given by
  *  "final_L_m" and at angle "angle_deg".
  *
  *  @param  ds          DataSet for which the focusing should be done. 
  *  @param  group_str   String containing list of group ids of the spectra in 
  *                      the DataSet that should be focused.  If the list
  *                      is empty, all spectra will be focused.
  *  @param  angle_deg   The scattering angle, 2*theta, (in degrees) to which 
  *                      the spectrum should be focused.  This angle must be 
  *                      greater than 0 and less than 180 degrees.  If it is
  *                      less than or equal to 0, the angle will not be changed.
  *  @param  final_L_m   The final flight path length in meters.  This must
  *                      be greater than 0.  If it is less than or equal to 0, 
  *                      the final path length will not be changed.
  *  @param  make_new_ds Flag to indicate whether or not to make a new DataSet. 
  */
  public TimeFocus( DataSet ds, 
                    String  group_str, 
                    float   angle_deg,
                    float   final_L_m,
                    boolean make_new_ds )
  {
    this(); 
    getParameter(0).setValue(ds);
    getParameter(1).setValue(group_str);
    getParameter(2).setValue(new Float(angle_deg));
    getParameter(3).setValue(new Float(final_L_m));
    getParameter(4).setValue(new Boolean(make_new_ds));
  }

  /* ---------------------------- getDocumentation -------------------------- */

  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();

    Res.append("@overview This operator will focus one or more spectra in a ");
    Res.append("DataSet to a specified scattering angle, using the ratio ");
    Res.append("L'*sin(theta') / L*sin(theta).");

    Res.append("@algorithm If successful, this operator produces a DataSet ");
    Res.append("where the specified groups have been focussed to the ");
    Res.append("specified angle and final flight path values.  If the ");
    Res.append("make_new_ds flag is true, a new DataSet is returned, ");
    Res.append("otherwise the original (altered) DataSet will be returned.  ");
    Res.append("If an error is encountered while focussing the Data blocks, ");
    Res.append("any Data blocks that were already focussed will remain ");
    Res.append("focussed.");
    Res.append("NOTE: This operator is very similar to the TimeFocusGID ");
    Res.append("operator, however, it gets the spectra to focus from ");
    Res.append("the DataSet by going through all spectra in the DataSet ");
    Res.append("and checking whether or not their ID is in the list of ");
    Res.append("IDs to focus.  The checking is done efficiently using ");
    Res.append("a binary search.  This faster if a large number ");
    Res.append("of spectra, are to be focused.");

    Res.append("@param  ds - DataSet for which the focusing should be done.");
    Res.append("@param  group_str - String containing list of group ids of ");
    Res.append("the spectra in the DataSet that should be focused.  If the ");
    Res.append("list is empty, all spectra will be focused.");
    Res.append("@param angle_deg - The scattering angle, 2*theta, (in ");
    Res.append("degrees) to which the spectrum should be focused.  This ");
    Res.append("angle must be greater than 0 and less than 180 degrees.  If ");
    Res.append("it is less than or equal to 0, the angle will not be changed.");
    Res.append("@param  final_L_m - The final flight path length in meters. ");
    Res.append("This must be greater than 0.  If it is less than or equal to ");
    Res.append("0, the final path length will not be changed.");
    Res.append("@param  make_new_ds - Flag to indicate whether or not to ");
    Res.append("make a new DataSet.");

    Res.append("@return If successful, a DataSet is returned, where the ");
    Res.append("specified groups have been focussed to the specified angle ");
    Res.append("and final flight path values, otherwise it returns an ");
    Res.append("ErrorString.");

    Res.append("@error \"DataSet is null in TimeFocus\"");
    Res.append("@error \"Invalid angle in TimeFocus \" + angle_deg ");
    Res.append("@error \"Invalid final path in TimeFocus \" + final_L_m ");
    Res.append("@error \"NO DetectorPosition for group \" + d.getGroup_ID()");
    Res.append("@error \"NO initial path for group \" + d.getGroup_ID()");

    return Res.toString();
  }

 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "TimeFocus", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "TimeFocus";
  }

 /* ------------------------ setDefaultParameters ------------------------- */ 
 /** 
  * Sets default values for the parameters.  This must match the data types 
  * of the parameters.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new DataSetPG("DataSet parameter", DataSet.EMPTY_DATA_SET) );
    addParameter( new StringPG("List of IDs to focus", "") );
    addParameter( new FloatPG("New Angle(degrees)", 90) );
    addParameter( new FloatPG("New Final Path(m)", 1) );
    addParameter( new BooleanPG("Make New DataSet", false) );
  }

 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  *  Executes this operator using the values of the current parameters.
  *
  *  @return  If successful, this operator produces a DataSet where the
  *           specified groups have been focussed to the specified angle
  *           and final flight path values.  If the make_new_ds flag is
  *           true, a new DataSet is returned, otherwise the original
  *           (altered) DataSet will be returned.  If an error is encountered
  *           while focussing the Data blocks, any Data blocks that were
  *           already focussed will remain focussed. 
  */
  public Object getResult()
  {
    DataSet ds        =  (DataSet)(getParameter(0).getValue());
    String  group_str =  (String) (getParameter(1).getValue());
    float   angle_deg = ((Float)  (getParameter(2).getValue())).floatValue();
    float   final_L_m = ((Float)  (getParameter(3).getValue())).floatValue();
    boolean make_new_ds=((Boolean)(getParameter(4).getValue())).booleanValue();

                                       // check for degenerate cases
    if ( ds == null )
      return new ErrorString("DataSet is null in TimeFocus");

    if ( angle_deg <= 0 || angle_deg >= 180  )
      return new ErrorString("Invalid angle in TimeFocus " + angle_deg );

    if ( final_L_m <= 0 )
      return new ErrorString("Invalid final path in TimeFocus " + final_L_m );

    int ids[] = IntList.ToArray( group_str );

    DataSet new_ds;
    if ( make_new_ds )
      new_ds = (DataSet)ds.clone();
    else
      new_ds = ds;

    Data             d, new_d;
    float            y_vals[];
    float            errors[];
    int              id;
    int              index;
    DetectorPosition pos;
    float            new_theta = (float)(angle_deg * Math.PI / 360);
    float            r, theta, initial_path; 
    Float            initial_path_obj;
    XScale           x_scale; 
    float            focus_dif_c=0f;
    GsasCalib        calib=null;
    AttributeList    attr_list;

    for ( int i = 0; i < new_ds.getNum_entries(); i++ ){
      calib=null;
      d = new_ds.getData_entry( i );     
  
      id    = d.getGroup_ID();
      index = arrayUtil.get_index_of( id, ids, 0, ids.length-1 );
      if ( index > 0 || ids.length == 0 ){              // focus
        pos = (DetectorPosition)d.getAttributeValue( Attribute.DETECTOR_POS );
        if ( pos == null )
          return new ErrorString("NO DetectorPosition for group " +
                                  d.getGroup_ID() );

        initial_path_obj=(Float)
                        d.getAttributeValue(Attribute.INITIAL_PATH);
        if ( initial_path_obj == null )
          return new ErrorString("NO initial path for group " +
                                  d.getGroup_ID() );
        initial_path = initial_path_obj.floatValue();

        r     = pos.getDistance();
        theta = pos.getScatteringAngle() / 2;
        calib=(GsasCalib)d.getAttributeValue(Attribute.GSAS_CALIB);

        if(calib!=null){
            focus_dif_c=(float)
                (252.816*2.*Math.sin(new_theta)*(final_L_m+initial_path));
            x_scale=tof_data_calc.DiffractometerFocus(d.getX_scale(),
                                                      calib.dif_c(),
                                                      focus_dif_c);
        }else{
            x_scale=tof_data_calc.DiffractometerFocus(d.getX_scale(),
                                                      r+initial_path,theta,
                                             final_L_m+initial_path,new_theta);
        }

        y_vals  = d.getY_values();
        errors  = d.getErrors();
        new_d = Data.getInstance( x_scale, y_vals, errors, id );
        new_d.setAttributeList(d.getAttributeList());
        new_d.removeAttribute(Attribute.GSAS_CALIB);
        
        pos = new DetectorPosition();
        if ( final_L_m > 0 )
            r = final_L_m;
        if ( angle_deg > 0 )
            theta = new_theta;
        pos.setSphericalCoords( r, theta*2, (float)(Math.PI/2.0) );
        
        new_d.setAttribute(new DetPosAttribute( Attribute.DETECTOR_POS, pos));
        new_ds.replaceData_entry( new_d, i );
      }
    }
    new_ds.removeAttribute(Attribute.GSAS_IPARM);
    if(calib!=null){
        new_ds.addLog_entry("Time Focused groups " + group_str + 
                            " to DIFC " + focus_dif_c );
    }else{
        new_ds.addLog_entry("Time Focused groups " + group_str + 
                            " to angle " + angle_deg +
                            " with L2 " + final_L_m );
    }

    return new_ds;
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    Operator op = new TimeFocus();
    op.CopyParametersFrom( this );
    return op;
  }

 /* ------------------------------- main --------------------------------- */ 
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
     System.out.println("Test of TimeFocus starting...");

                                                              // load a DataSet
     String filename = "/IPNShome/dennis/ARGONNE_DATA/gppd12358.run";
     RunfileRetriever rr = new RunfileRetriever( filename );
     DataSet ds = rr.getDataSet(1);
                                                              // make operator
                                                              // and call it
     TimeFocus op = new TimeFocus( ds, "44:73", 90, 1, true );
     Object obj = op.getResult();
     if ( obj instanceof DataSet )                   // we got a DataSet back
     {                                               // so show it and original
       DataSet new_ds = (DataSet)obj;
       ViewManager vm1 = new ViewManager( ds,     IViewManager.IMAGE );
       ViewManager vm2 = new ViewManager( new_ds, IViewManager.IMAGE );
     }
     else
       System.out.println( "Operator returned " + obj );

     System.out.println("Test of TimeFocus done.");
  }
}
