/*
 * File:  DiffractometerQToWavelength.java
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
 * Revision 1.4  2002/11/27 23:17:04  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/09/19 16:00:26  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.2  2002/07/08 20:46:02  pfpeterson
 * Now uses String constants in FontUtil.
 *
 * Revision 1.1  2002/07/02 17:05:53  pfpeterson
 * Added to CVS.
 *
 *
 */

package DataSetTools.operator.DataSet.Conversion.XAxis;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
 * This operator converts a Q DataSet for a Diffractometer, to
 * wavelength. The DataSet must contain spectra with attributes giving
 * the detector position.
 */

public class DiffractometerQToWavelength extends XAxisConversionOp{
    /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
    /**
     * Construct an operator with a default parameter list.  If this
     * constructor is used, the operator must be subsequently added to
     * the list of operators of a particular DataSet.  Also,
     * meaningful values for the parameters should be set ( using a
     * GUI ) before calling getResult() to apply the operator to the
     * DataSet this operator was added to.
     */
    public DiffractometerQToWavelength(){
        super( "Convert to Wavelength" );
    }
    
    /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
    /**
     *  Construct an operator for a specified DataSet and with the
     *  specified parameter values so that the operation can be
     *  invoked immediately by calling getResult().
     *
     *  @param  ds          The DataSet to which the operation is applied
     *  @param  wlmin        The minimum wl value to be binned
     *  @param  wlmax        The maximum wl value to be binned
     *  @param  num_wl       The number of "bins" to be used between wlmin
     *                      and wlmax
     */

    public DiffractometerQToWavelength( DataSet ds, float wlmin,
                                        float wlmax, int num_wl ){
        this();                         // do the default constructor, then set
                                        // the parameter value(s) by altering a
                                        // reference to each of the parameters

        IParameter parameter = getParameter( 0 );
        parameter.setValue( new Float( wlmin ) );
        
        parameter = getParameter( 1 );
        parameter.setValue( new Float( wlmax ) );
        
        parameter = getParameter( 2 );
        parameter.setValue( new Integer( num_wl ) );
        
        setDataSet( ds );       // record reference to the DataSet that
                                // this operator should operate on
    }
    
    /* --------------------------- getCommand ------------------------------ */
    /**
     * @return the command name to be used with script processor: in
     * this case, QtoWL
     */
    public String getCommand(){
        return "QtoWL";
    }


    /* ------------------------- setDefaultParmeters ----------------------- */
    /**
     *  Set the parameters to default values.
     */
    public void setDefaultParameters(){
        UniformXScale scale = getXRange();
        
        parameters = new Vector();  // must do this to clear any old parameters
        
        float wlmin=Float.NaN;
        float wlmax=Float.NaN;
        
        if( scale!=null){
            wlmin=scale.getStart_x();
            if( Float.isNaN(wlmin) ) wlmin=0f;
            wlmax=scale.getEnd_x();
            if( Float.isNaN(wlmax) || Float.isInfinite(wlmax) ) wlmax=20f;
        }
        addParameter( new Parameter( "Min Wavelength("+FontUtil.ANGSTROM+")", new Float(wlmin) ) );
        addParameter( new Parameter( "Max Wavelength("+FontUtil.ANGSTROM+")", new Float(wlmax) ) );
        addParameter( new Parameter( Parameter.NUM_BINS, new Integer(1000) ) );
    }
    
    
    /* -------------------------- new_X_label ---------------------------- */
    /**
     * Get string label for converted x values.
     *
     *  @return String describing the x label and units for converted
     *  x values.
     */
    public String new_X_label(){
        return new String( FontUtil.LAMBDA+"("+FontUtil.ANGSTROM+")" );
    }


    /* --------------------- convert_X_Value ------------------------------- */
    /**
     * Evaluate the axis conversion function at one point only.
     *
     *  @param x the x-value where the axis conversion function is to
     *  be evaluated.
     *
     *  @param i the index of the Data block for which the axis
     *  conversion function is to be evaluated.
     *
     *  @return the value of the axis conversion function at the
     *  specified x.
     */
    public float convert_X_Value( float x, int i ){
        DataSet ds = this.getDataSet();       // make sure we have a DataSet
        if ( ds == null )
            return Float.NaN;
        
        int num_data = ds.getNum_entries();   // make sure we have a valid Data
        if ( i < 0 || i >= num_data )         // index
            return Float.NaN;
        
        Data data               = ds.getData_entry( i );
        AttributeList attr_list = data.getAttributeList();
        
        // get the detector position and initial path length
        DetectorPosition position=(DetectorPosition)
            attr_list.getAttributeValue( Attribute.DETECTOR_POS);
        if( position == null ) return Float.NaN;

        float angle_radians = position.getScatteringAngle();
        if( Float.isNaN(angle_radians) ) return Float.NaN;
        
        return tof_calc.WavelengthofDiffractometerQ( angle_radians, x );
    }
    
    
    /* ---------------------------- getResult ------------------------------ */
    public Object getResult(){

        DataSet ds = this.getDataSet();  // get the current data set

        DataSetFactory factory = new DataSetFactory( 
                                                    ds.getTitle(),
                                                    "Angstroms",
                                                    "wavelength",
                                                    "Counts",
                                                    "Scattering Intensity" );
        
        // must take care of the operation log... this starts with it empty
        DataSet new_ds = factory.getDataSet(); 
        new_ds.copyOp_log( ds );
        new_ds.addLog_entry( "Converted to Wavelength" );
        
        // copy the attributes of the original data set
        new_ds.setAttributeList( ds.getAttributeList() );
        
        // get the scale parameters 
        float wlmin = ( (Float)(getParameter(0).getValue()) ).floatValue();
        float wlmax = ( (Float)(getParameter(1).getValue()) ).floatValue();
        int   num_wl = ((Integer)(getParameter(2).getValue()) ).intValue() + 1;
        
        // validate bounds
        if ( wlmin > wlmax ){             // swap bounds to be in proper order
            float temp = wlmin;
            wlmin = wlmax;
            wlmax = temp;
        }
        
        UniformXScale new_wl_scale;
        if ( num_wl <= 1.0 || wlmin >= wlmax )       // no valid scale set
            new_wl_scale = null;
        else
            new_wl_scale = new UniformXScale( wlmin, wlmax, num_wl );  
        
        Data             data,
                         new_data;
        DetectorPosition position;
        float            y_vals[];       // y_values from one spectrum
        float            errors[];       // errors from one spectrum
        float            wl_vals[];      // wl values at bin boundaries
        XScale           wl_scale;
        float            angle_radians;
        int              num_data = ds.getNum_entries();
        AttributeList    attr_list;
        
        for ( int j = 0; j < num_data; j++ ){
            data = ds.getData_entry( j );   // get reference to the data entry
            attr_list = data.getAttributeList();
            
            // get the detector position and initial path length 
            position=(DetectorPosition)
                attr_list.getAttributeValue(Attribute.DETECTOR_POS);
            
            // has needed attributes so convert it to wl
            if( position != null ){
                
                angle_radians=position.getScatteringAngle();
                wl_vals           = data.getX_scale().getXs();
                for ( int i = 0; i < wl_vals.length; i++ )
                    wl_vals[i] = 
                        tof_calc.WavelengthofDiffractometerQ( angle_radians, 
                                                              wl_vals[i] );
                arrayUtil.Reverse(wl_vals);
                wl_scale = new VariableXScale( wl_vals );
                
                y_vals  = data.getY_values();
                arrayUtil.Reverse(y_vals);

                errors  = data.getErrors();
                arrayUtil.Reverse(errors);
                
                new_data = Data.getInstance( wl_scale, 
                                             y_vals, 
                                             errors, 
                                             data.getGroup_ID() );

                new_data.setAttributeList( attr_list );
                                                        
                if ( new_wl_scale != null )   // resample if a num_bins>1
                    new_data.resample( new_wl_scale, IData.SMOOTH_NONE ); 

                new_ds.addData_entry( new_data );      
            }
        }
        new_ds.addOperator(new DiffractometerWavelengthToQ());
        new_ds.addOperator(new DiffractometerWavelengthToD());
        
        return new_ds;
    }  
    
    /* ------------------------------ clone ------------------------------- */
    /**
     * Get a copy of the current DDiffractometerQToWavelength
     * Operator.  The list of * parameters and the reference to the
     * DataSet to which it applies are also copied.
     */
    public Object clone(){
        DiffractometerQToWavelength new_op = 
            new DiffractometerQToWavelength( );
        // copy the data set associated
        // with this operator
        new_op.setDataSet( this.getDataSet() );
        new_op.CopyParametersFrom( this );
        
        return new_op;
    }
}
