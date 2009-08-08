/*
 * File:  TimeFocusIndex.java 
 *
 * Copyright (C) 2002, Peter F. Peterson, 2009 Dennis Mikkelson
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
 *  4/7/09 Adapted from TimeFocusGID
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package Operators.TOF_Diffractometer;

import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import java.util.Vector;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.DetPosAttribute;
import DataSetTools.dataset.XScale;
import DataSetTools.gsastools.GsasCalib;
import DataSetTools.math.tof_data_calc;
import DataSetTools.operator.Operator;
import DataSetTools.operator.Parameter;
import DataSetTools.operator.Generic.TOF_Diffractometer.GenericTOF_Diffractometer;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.viewer.IViewManager;
import DataSetTools.viewer.ViewManager;

/** 
 *  This operator will focus one or more spectra in a DataSet to a specified
 *  scattering angle, using the ratio L'*sin(theta') / L*sin(theta).  The
 *  current total flight path length is L and half the scattering angle is 
 *  theta.  L' and theta' are the new path length and angle.  The spectra
 *  to be focused are specified by their index (i.e. position) in the 
 *  DataSet rather than by their group ID.  This is MUCH more efficient since
 *  it is not necessary to search through the list of Data blocks to find
 *  the Data block with the specified group ID.
 */
public class TimeFocusIndex extends GenericTOF_Diffractometer{
    private static final String TITLE = "Time Focus Index";

    /** 
     *  Creates operator with title "Time Focus Index" and a default list of
     *  parameters.
     */  
    public TimeFocusIndex(){
        super( TITLE );
    }

    /** 
     *  Construct a TimeFocusIndex operator that focuses the specified
     *  groups in a neutron Time-of-flight diffractometer DataSet to
     *  the specified angle and final flight path length.  Note:
     *  After focusing, the detector position will be in the
     *  "scattering plane" at a distance given by "final_L_m" and at
     *  angle "angle_deg".
     *
     *  @param  ds          DataSet for which the focusing should be done. 
     *  @param  index_str   String containing list of indices of the 
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
    public TimeFocusIndex( DataSet ds, String  index_str, float   angle_deg,
                         float   final_L_m, boolean make_new_ds ){
        this(); 
        parameters = new Vector();
        addParameter( new Parameter("DataSet parameter", ds) );
        addParameter( new Parameter("List of Group IDs to focus",
                                    new String(index_str)));
        addParameter( new Parameter("New Angle(degrees)",
                                    new Float(angle_deg) ) );
        addParameter( new Parameter("New Final Path(m)",
                                    new Float(final_L_m) ) );
        addParameter( new Parameter("Make New DataSet",
                                    new Boolean(make_new_ds)));
    }
    
  /* ---------------------------- getDocumentation -------------------------- */
 
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This operator will focus one or more spectra in a ");
    Res.append("DataSet to a specified scattering angle, using the ratio ");
    Res.append("L'*sin(theta') / L*sin(theta).");
    Res.append("NOTE: This operator is very similar to the TimeFocusGID ");
    Res.append("operator, however, it gets spectra to focus from ");
    Res.append("the DataSet by specifying the index (i.e. position ) of ");
    Res.append("the Data block in the DataSet, rather than the Group ID. ");
 
    Res.append("@algorithm If successful, this operator produces a DataSet ");
    Res.append("where the specified groups have been focused to the ");
    Res.append("specified angle and final flight path values.  If the ");
    Res.append("make_new_ds flag is true, a new DataSet is returned, ");
    Res.append("otherwise the original (altered) DataSet will be returned.  ");
    Res.append("If an error is encountered while focusing the Data blocks, ");
    Res.append("any Data blocks that were already focused will remain ");
    Res.append("focused.");
       
    Res.append("@param  ds - DataSet for which the focusing should be done.");
    Res.append("@param  index_str - String containing list of indices of ");
    Res.append("the spectra in the DataSet that should be focused.  If the ");
    Res.append("list is empty, all spectra will be focused.");
    Res.append("@param angle_deg - The scattering angle, 2*theta, (in ");
    Res.append("degrees) to which the spectrum should be focused.  This ");
    Res.append("angle must be greater than 0 and less than 180 degrees.  If ");
    Res.append("not, an ErrorString will be returned.");
    Res.append("@param  final_L_m - The final flight path length in meters. ");
    Res.append("This must be greater than 0.  If it is less than or equal to ");
    Res.append("0, an ErrorString will be returned.");
    Res.append("@param  make_new_ds - Flag to indicate whether or not to "); 
    Res.append("make a new DataSet.");
    
    Res.append("@return If successful, a DataSet is returned, where the ");
    Res.append("specified groups have been focused to the specified angle ");
    Res.append("and final flight path values, otherwise it returns an ");
    Res.append("ErrorString.");
    
    Res.append("@error \"DataSet is null in TimeFocusIndex\"");
    Res.append("@error \"Invalid angle in TimeFocusIndex \" + angle_deg ");
    Res.append("@error \"Invalid final path in TimeFocusIndex \" + final_L_m ");
    Res.append("@error \"NO DetectorPosition for group \" + d.getGroup_ID()");
    Res.append("@error \"NO initial path for group \" + d.getGroup_ID()");
    Res.append("@error \"Invalid index specifier: \" + index_str");
    
    return Res.toString();
  }
  
 /* ---------------------------- getCommand ------------------------------- */     
    /** 
     * Get the name of this operator to use in scripts
     * 
     * @return "TimeFocusIndex", the command used to invoke this
     * operator in Scripts
     */
    public String getCommand(){
        return "TimeFocusIndex";
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

 /* ----------------------------- getResult ------------------------------ */ 
    /** 
     *  Executes this operator using the values of the current parameters.
     *
     *  @return If successful, this operator produces a DataSet where
     *          the specified groups have been focused to the
     *          specified angle and final flight path values.  If the
     *          make_new_ds flag is true, a new DataSet is returned,
     *          otherwise the original (altered) DataSet will be
     *          returned.  If an error is encountered while focusing
     *          the Data blocks, any Data blocks that were already
     *          focused will remain focused.
     */
    public Object getResult(){
        DataSet ds        =  (DataSet)(getParameter(0).getValue());
        String  index_str =  (String)(getParameter(1).getValue());
        float   angle_deg = ((Float)(getParameter(2).getValue())).floatValue();
        float   final_L_m = ((Float)(getParameter(3).getValue())).floatValue();
        boolean make_new_ds
                        =((Boolean)(getParameter(4).getValue())).booleanValue();
        
        // check for degenerate cases
        if ( ds == null )
            return new ErrorString("DataSet is null in TimeFocusIndex");
        
        if ( angle_deg <= 0 || angle_deg >= 180  )
          return new ErrorString("Invalid angle in TimeFocusIndex "+angle_deg);
        
        if ( final_L_m <= 0 )
            return new ErrorString("Invalid final path in TimeFocusIndex " 
                                   + final_L_m );
        
                                // If no indexes specified, use them all
        if ( index_str == null || index_str.trim().length() <= 0 )
          index_str = "0:" + (ds.getNum_entries()-1);

        int indices[] = IntList.ToArray( index_str );
        if( indices==null || indices.length==0 )
          return new ErrorString("Invalid index specifier:"+index_str);
        
        DataSet new_ds;
        if ( make_new_ds )
            new_ds = (DataSet)ds.clone();
        else
            new_ds = ds;
        
        Data             d, new_d;
        float            y_vals[], errors[];
        int              gid;
        DetectorPosition pos;
        float            new_theta = (float)(angle_deg * Math.PI / 360);
        float            theta, r, initial_path;
        Float            initial_path_obj;
        XScale           x_scale; 
        // set up what focus_dif_c is
        float            focus_dif_c=0f;
        GsasCalib        calib=null;

        for ( int i = 0; i < indices.length; i++ ){
            calib=null;
            d = new_ds.getData_entry( indices[i] );     
            if(d!=null){
                pos = (DetectorPosition)d.getAttributeValue( 
                                                      Attribute.DETECTOR_POS );
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
                gid     = d.getGroup_ID();
                new_d = Data.getInstance( x_scale, y_vals, errors, gid );
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
                new_ds.replaceData_entry( new_d, indices[i] );
            }
        }
        new_ds.removeAttribute(Attribute.GSAS_IPARM);
        if(calib!=null){
            new_ds.addLog_entry("Time Focused groups (Index=" + index_str + 
                                ") to DIFC " + focus_dif_c );
        }else{
            new_ds.addLog_entry("Time Focused groups (Index=" + index_str + 
                                ") to angle " + angle_deg +
                                " with L2 " + final_L_m );
        }
        
        return new_ds;
    }

 /* ------------------------------- clone -------------------------------- */     
    /** 
     *  Creates a clone of this operator.
     */
    public Object clone(){
        Operator op = new TimeFocusIndex();
        op.CopyParametersFrom( this );
        return op;
    }

 /* ------------------------------- main --------------------------------- */     
    /** 
     * Test program to verify that this will complile and run ok.  
     *
     */
    public static void main( String args[] ){
        System.out.println("Test of TimeFocusIndex starting...");
        if(args.length==1){
            // load a DataSet
            String filename = args[0];
            RunfileRetriever rr = new RunfileRetriever( filename );
            DataSet ds = rr.getDataSet(1);
            // make operator and call it
            TimeFocusIndex op = new TimeFocusIndex( ds, "44:73", 90, 1, true );
            Object obj = op.getResult();
            if ( obj instanceof DataSet ){            // we got a DataSet back
                // so show it and original
                DataSet new_ds = (DataSet)obj;
                new ViewManager( ds,     IViewManager.IMAGE );
                new ViewManager( new_ds, IViewManager.IMAGE );
            }else
                System.out.println( "Operator returned " + obj );
        }else{
            System.out.println("USAGE: TimeFocusIndex <filename>");
        }
            
        System.out.println("Test of TimeFocusIndex done.");
    }
}
