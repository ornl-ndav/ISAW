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
 * $Log$
 * Revision 1.8  2004/03/15 03:37:02  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.7  2004/01/30 02:26:34  bouzekc
 * Removed unused variables and imports.
 *
 * Revision 1.6  2003/12/15 02:38:18  bouzekc
 * Removed unused imports.
 *
 * Revision 1.5  2003/04/17 20:38:33  pfpeterson
 * Added a check that IntList returned a non-empty array.
 *
 * Revision 1.4  2003/02/28 15:14:37  dennis
 * Added getDocumentation() method.  (Shannon Hintzman)
 *
 * Revision 1.3  2002/11/27 23:30:47  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/07/10 15:52:08  pfpeterson
 * Uses information from gsas calibration from attributes if present.
 *
 * Revision 1.1  2002/07/08 15:44:48  pfpeterson
 * Added to CVS.
 *
 *
 */
package Operators.TOF_Diffractometer;

import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.Util.Numeric.IntList;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

import java.util.Vector;
import java.util.Arrays;

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
 *  theta.  L' and theta' are the new path length and angle.
 */
public class TimeFocusGID extends GenericTOF_Diffractometer{
    private static final String TITLE = "Time Focus GID";

    /** 
     *  Creates operator with title "Time Focus GID" and a default list of
     *  parameters.
     */  
    public TimeFocusGID(){
        super( TITLE );
    }

    /** 
     *  Construct a TimeFocusGID operator that focuses the specified
     *  groups in a neutron Time-of-flight diffractometer DataSet to
     *  the specified angle and final flight path length.  Note:
     *  After focusing, the detector position will be in the
     *  "scattering plane" at a distance given by  "final_L_m" and at
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
    public TimeFocusGID( DataSet ds, 
                         String  group_str, 
                         float   angle_deg,
                         float   final_L_m, 
                         boolean make_new_ds ){
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
    
  /* ---------------------------- getDocumentation -------------------------- */
 
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This operator will focus one or more spectra in a ");
    Res.append("DataSet to a specified scattering angle, using the ratio ");
    Res.append("L'*sin(theta') / L*sin(theta).");
    Res.append("It gets the spectra to focus from ");
    Res.append("the DataSet by requesting spectra with particular ");
    Res.append("group IDs.  This is more efficient ONLY if a small number ");
    Res.append("of spectra, selected from a large DataSet, are to be focused.");
 
    Res.append("@algorithm If successful, this operator produces a DataSet ");
    Res.append("where the specified groups have been focused to the ");
    Res.append("specified angle and final flight path values.  If the ");
    Res.append("make_new_ds flag is true, a new DataSet is returned, ");
    Res.append("otherwise the original (altered) DataSet will be returned.  ");
    Res.append("If an error is encountered while focusing the Data blocks, ");
    Res.append("any Data blocks that were already focused will remain ");
    Res.append("focused.");
       
    Res.append("@param  ds - DataSet for which the focusing should be done.");
    Res.append("@param  group_str - String containing list of group ids of ");
    Res.append("the spectra in the DataSet that should be focused.  If the ");
    Res.append("list is empty, all spectra will be focused.");
    Res.append("@param angle_deg - The scattering angle, 2*theta, (in ");
    Res.append("degrees) to which the spectrum should be focused.  This ");
    Res.append("angle must be greater than 0 and less than 180 degrees.  If ");
    Res.append("it is less than or equal to 0, an error string is returned. ");
    Res.append("@param  final_L_m - The final flight path length in meters. ");
    Res.append("This must be greater than 0.  If it is less than or equal to ");
    Res.append("0, an error string is be returned.");
    Res.append("@param  make_new_ds - Flag to indicate whether or not to "); 
    Res.append("make a new DataSet.");
    
    Res.append("@return If successful, a DataSet is returned, where the ");
    Res.append("specified groups have been focused to the specified angle ");
    Res.append("and final flight path values, otherwise it returns an ");
    Res.append("ErrorString.");
    
    Res.append("@error \"DataSet is null in TimeFocusGID\"");
    Res.append("@error \"Invalid angle in TimeFocusGID \" + angle_deg ");
    Res.append("@error \"Invalid final path in TimeFocusGID \" + final_L_m ");
    Res.append("@error \"NO DetectorPosition for group \" + d.getGroup_ID()");
    Res.append("@error \"NO initial path for group \" + d.getGroup_ID()");
    
    return Res.toString();
  }
  
 /* ---------------------------- getCommand ------------------------------- */     
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
        String  group_str =  (String)(getParameter(1).getValue());
        float   angle_deg = ((Float)(getParameter(2).getValue())).floatValue();
        float   final_L_m = ((Float)(getParameter(3).getValue())).floatValue();
        boolean make_new_ds
                        =((Boolean)(getParameter(4).getValue())).booleanValue();
        
        // check for degenerate cases
        if ( ds == null )
            return new ErrorString("DataSet is null in TimeFocusGID");
        
        if ( angle_deg <= 0 || angle_deg >= 180  )
            return new ErrorString("Invalid angle in TimeFocusGID "+angle_deg);
        
        if ( final_L_m <= 0 )
            return new ErrorString("Invalid final path in TimeFocusGID " 
                                   + final_L_m );

                                     // find all group IDs if none specified
         if ( group_str == null || group_str.trim().length() <= 0 )
         {
           int   num_groups = ds.getNum_entries();
           int[] id_list = new int[num_groups];
           for ( int i = 0; i < num_groups; i++ )
             id_list[i] = ds.getData_entry(i).getGroup_ID();
           Arrays.sort( id_list );
           group_str = IntList.ToString( id_list );
         }
        
        int ids[] = IntList.ToArray( group_str );
        if(ids==null || ids.length==0)
          return new ErrorString("Invalid Grouping specifier:"+group_str);
        
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

                  // Getting a Data block by ID is VERY expensive for a 
                  // large DataSet.  Processing all selected Data blocks is
                  // is much more efficient if a lot of Data blocks are to be
                  // processed.  So... we mark as selected the Data blocks
                  // with the specified group IDs and process the selected
                  // Data blocks.  To avoid side effects, we first save and
                  // then restore the selected flags.

        int[] original_selections = ds.getSelectedIndices();

        new_ds.clearSelections();
        new_ds.setSelectFlagsByID( ids, true );
        int[] indices_to_focus = new_ds.getSelectedIndices();

        for ( int i = 0; i < indices_to_focus.length; i++ ){
            calib=null;
            d = new_ds.getData_entry( indices_to_focus[i] );     
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
                new_d = Data.getInstance( x_scale, 
                                          y_vals, 
                                          errors, 
                                          d.getGroup_ID());

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
                new_ds.replaceData_entry( new_d, indices_to_focus[i] );

//                System.out.println("Processed Group ID " + d.getGroup_ID() +
//                                   "   "  + new_d.getGroup_ID() );
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
        
                              // Now restore the original selection flags
        new_ds.clearSelections();
        new_ds.setSelectFlagsByIndex( original_selections, true );

        return new_ds;
    }

 /* ------------------------------- clone -------------------------------- */     
    /** 
     *  Creates a clone of this operator.
     */
    public Object clone(){
        Operator op = new TimeFocusGID();
        op.CopyParametersFrom( this );
        return op;
    }

 /* ------------------------------- main --------------------------------- */     
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
                new ViewManager( ds,     IViewManager.IMAGE );
                new ViewManager( new_ds, IViewManager.IMAGE );
            }else
                System.out.println( "Operator returned " + obj );
        }else{
            System.out.println("USAGE: TimeFocusGID <filename>");
        }
            
        System.out.println("Test of TimeFocusGID done.");
    }
}
