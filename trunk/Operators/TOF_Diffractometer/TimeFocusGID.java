/*
 * File:  TimeFocusGID.java 
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
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2002/07/10 15:52:08  pfpeterson
 * Uses information from gsas calibration from attributes if present.
 *
 * Revision 1.1  2002/07/08 15:44:48  pfpeterson
 * Added to CVS.
 *
 *
 */
package Operators.TOF_Diffractometer;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.TOF_Diffractometer.*;
import DataSetTools.operator.DataSet.EditList.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.gsastools.GsasCalib;
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
public class TimeFocusGID extends GenericTOF_Diffractometer{
    private static final String TITLE = "Time Focus GID";

    /** 
     *  Creates operator with title "Time Focus" and a default list of
     *  parameters.
     */  
    public TimeFocusGID(){
        super( TITLE );
    }

    /** 
     *  Construct a TimeFocusGID operator that focuses the specified
     *  groups * in a neutron Time-of-flight diffractometer DataSet to
     *  the specified * angle and final flight path length.  Note:
     *  After focussing, the detector * position will be in the
     *  "scattering plane" at a distance given by * "final_L_m" and at
     *  angle "angle_deg".
     *
     *  @param  ds          DataSet for which the focusing should be done. 
     *  @param  group_str   String containing list of group ids of the 
     *                      spectra in the DataSet that should be
     *                      focused. If the list is empty, all
     *                      spectra will be focused.
     *  @param angle_deg    The scattering angle, 2*theta, (in degrees)
     *                      to which the spectrum should be focused.
     *                      This angle must be greater than 0 and less
     *                      than 180 degrees.  If it is less than or
     *                      equal to 0, the angle will not be changed.

     *  @param final_L_m    The final flight path length in meters.  This
     *                      must be greater than 0.  If it is less
     *                      than or equal to 0, the final path length
     *                      will not be changed.

     *  @param make_new_ds  Flag to indicate whether or not to make a
     *                      new DataSet.
     */
    public TimeFocusGID( DataSet ds, String  group_str, float   angle_deg,
                         float   final_L_m, boolean make_new_ds ){
        this(); 
        parameters = new Vector();
        addParameter( new Parameter("DataSet parameter", ds) );
        addParameter( new Parameter("List of Group IDs to focus",
                                    new String(group_str)));
        addParameter( new Parameter("New Angle(degrees)",
                                    new Float(angle_deg) ) );
        addParameter( new Parameter("New Final Path(m)",
                                    new Float(final_L_m) ) );
        addParameter( new Parameter("Make New DataSet",
                                    new Boolean(make_new_ds)));
    }
    
    /** 
     * Get the name of this operator to use in scripts
     * 
     * @return "TimeFocusGID", the command used to invoke this
     * operator in Scripts
     */
    public String getCommand(){
        return "TimeFocusGID";
    }

    /** 
     * Sets default values for the parameters.  This must match the
     * data types of the parameters.
     */
    public void setDefaultParameters(){
        parameters = new Vector();
        addParameter( new Parameter("DataSet parameter",
                                    DataSet.EMPTY_DATA_SET) );
        addParameter( new Parameter("List of Group IDs to focus",
                                    new String("") ) );
        addParameter( new Parameter("New Angle(degrees)", new Float(90) ) );
        addParameter( new Parameter("New Final Path(m)", new Float(1) ) );
        addParameter( new Parameter("Make New DataSet", new Boolean(false) ) );
    }

    /** 
     *  Executes this operator using the values of the current parameters.
     *
     *  @return If successful, this operator produces a DataSet where
     *          the specified groups have been focussed to the
     *          specified angle and final flight path values.  If the
     *          make_new_ds flag is true, a new DataSet is returned,
     *          otherwise the original (altered) DataSet will be
     *          returned.  If an error is encountered while focussing
     *          the Data blocks, any Data blocks that were already
     *          focussed will remain focussed.
     */
    public Object getResult(){
        DataSet ds        =  (DataSet)(getParameter(0).getValue());
        String  group_str =  (String)(getParameter(1).getValue());
        float   angle_deg = ((Float)(getParameter(2).getValue())).floatValue();
        float   final_L_m = ((Float)(getParameter(3).getValue())).floatValue();
        boolean make_new_ds=((Boolean)(getParameter(4).getValue())).booleanValue();
        
        // check for degenerate cases
        if ( ds == null )
            return new ErrorString("DataSet is null in TimeFocusGID");
        
        if ( angle_deg <= 0 || angle_deg >= 180  )
            return new ErrorString("Invalid angle in TimeFocusGID "+angle_deg);
        
        if ( final_L_m <= 0 )
            return new ErrorString("Invalid final path in TimeFocusGID " 
                                   + final_L_m );
        
        int ids[] = IntList.ToArray( group_str );
        
        DataSet new_ds;
        if ( make_new_ds )
            new_ds = (DataSet)ds.clone();
        else
            new_ds = ds;
        
        Data             d, new_d;
        float            y_vals[], errors[];
        DetectorPosition pos;
        float            new_theta = (float)(angle_deg * Math.PI / 360);
        float            theta, r, initial_path;
        Float            initial_path_obj;
        XScale           x_scale; 
        // set up what focus_dif_c is
        float            focus_dif_c=0f;
        GsasCalib        calib=null;
        AttributeList    attr_list;

        for ( int i = 0; i < ids.length; i++ ){
            calib=null;
            d = new_ds.getData_entry_with_id( ids[i] );     
            if(d!=null){
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
                new_d = Data.getInstance( x_scale, y_vals, errors, ids[i] );
                new_d.setAttributeList( d.getAttributeList() );
                new_d.removeAttribute(Attribute.GSAS_CALIB);
                
                pos = new DetectorPosition();
                if ( final_L_m > 0 )
                    r = final_L_m;
                if ( angle_deg > 0 )
                    theta = new_theta;
                pos.setSphericalCoords( r, theta*2, (float)(Math.PI/2.0) );
                
                new_d.setAttribute(new DetPosAttribute( Attribute.DETECTOR_POS,
                                                        pos));
                new_ds.replaceData_entry_with_id( new_d, ids[i] );
            }
        }
        new_ds.removeAttribute(Attribute.GSAS_IPARM);
        if(calib!=null){
            new_ds.addLog_entry("Time Focused groups (GID=" + group_str + 
                                ") to DIFC " + focus_dif_c );
        }else{
            new_ds.addLog_entry("Time Focused groups (GID=" + group_str + 
                                ") to angle " + angle_deg +
                                " with L2 " + final_L_m );
        }
        
        return new_ds;
    }
    
    /** 
     *  Creates a clone of this operator.
     */
    public Object clone(){
        Operator op = new TimeFocusGID();
        op.CopyParametersFrom( this );
        return op;
    }
    
    /** 
     * Test program to verify that this will complile and run ok.  
     *
     */
    public static void main( String args[] ){
        System.out.println("Test of TimeFocusGID starting...");
        if(args.length==1){
            // load a DataSet
            String filename = args[0];
            RunfileRetriever rr = new RunfileRetriever( filename );
            DataSet ds = rr.getDataSet(1);
            // make operator and call it
            TimeFocusGID op = new TimeFocusGID( ds, "44:73", 90, 1, true );
            Object obj = op.getResult();
            if ( obj instanceof DataSet ){            // we got a DataSet back
                // so show it and original
                DataSet new_ds = (DataSet)obj;
                ViewManager vm1 = new ViewManager( ds,     IViewManager.IMAGE );
            ViewManager vm2 = new ViewManager( new_ds, IViewManager.IMAGE );
            }else
                System.out.println( "Operator returned " + obj );
        }else{
            System.out.println("USAGE: TimeFocusGID <filename>");
        }
            
        System.out.println("Test of TimeFocusGID done.");
    }
}
