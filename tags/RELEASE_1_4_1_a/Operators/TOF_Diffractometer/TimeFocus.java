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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
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
    parameters = new Vector();
    addParameter( new Parameter("DataSet parameter", ds) );
    addParameter( new Parameter("List of IDs to focus", new String(group_str)));
    addParameter( new Parameter("New Angle(degrees)", new Float(angle_deg) ) );
    addParameter( new Parameter("New Final Path(m)", new Float(final_L_m) ) );
    addParameter( new Parameter("Make New DataSet", new Boolean(make_new_ds)));
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
    addParameter( new Parameter("DataSet parameter", DataSet.EMPTY_DATA_SET) );
    addParameter( new Parameter("List of IDs to focus", new String("") ) );
    addParameter( new Parameter("New Angle(degrees)", new Float(90) ) );
    addParameter( new Parameter("New Final Path(m)", new Float(1) ) );
    addParameter( new Parameter("Make New DataSet", new Boolean(false) ) );
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

    new_ds.addLog_entry("Focused groups " + group_str + 
                        " to angle " + angle_deg +
                        " with L2 " + final_L_m );

    Data             d, 
                     new_d;
    float            x_vals[];
    float            y_vals[];
    float            errors[];
    int              id;
    int              index;
    DetectorPosition pos;
    float            new_theta = (float)(angle_deg * Math.PI / 360);
    float            theta;
    float            r;
    Float            initial_path_obj;
    float            initial_path; 
    float            scale_factor;
    XScale           x_scale; 
    AttributeList    attr_list;
    for ( int i = 0; i < new_ds.getNum_entries(); i++ )
    {
      d = new_ds.getData_entry( i );     
  
      id    = d.getGroup_ID();
      index = arrayUtil.get_index_of( id, ids, 0, ids.length-1 );
      if ( index > 0 || ids.length == 0 )              // focus
      {
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
        scale_factor = 1;
        if ( final_L_m > 0 )
          scale_factor = (final_L_m + initial_path)/(r + initial_path);

        if ( angle_deg > 0 )
          scale_factor *= Math.sin( new_theta ) / Math.sin( theta ); 

        if ( scale_factor != 1 )
        {
          x_vals = d.getX_scale().getXs();
          for ( int j = 0; j < x_vals.length; j++ )
            x_vals[j] *= scale_factor;

          x_scale = new VariableXScale( x_vals );
          y_vals  = d.getY_values();
          errors  = d.getErrors();
          new_d = Data.getInstance( x_scale, y_vals, errors, id );
          new_d.setAttributeList( d.getAttributeList() );

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
