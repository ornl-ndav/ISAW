/*
 * File:  DiffractometerWavelengthToQ.java
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
 *           Intense Pulsed Neutron Source
 *           Argonne National Laboratory
 *           Argonne, IL 60439-4814, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.4  2002/09/19 16:00:32  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.3  2002/07/08 20:46:07  pfpeterson
 * Now uses String constants in FontUtil.
 *
 * Revision 1.2  2002/07/02 17:07:26  pfpeterson
 * Now uses string constants defined in IsawGUI.Isaw and adds
 * operator for Q->Wavelength.
 *
 * Revision 1.1  2002/06/19 21:58:05  pfpeterson
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
 * This operator converts a wavelength DataSet for a Diffractometer,
 * to Q. The DataSet must contain spectra with attributes giving the
 * detector position.
 */

public class DiffractometerWavelengthToQ extends XAxisConversionOp{
    /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
    /**
     * Construct an operator with a default parameter list.  If this
     * constructor is used, the operator must be subsequently added to
     * the list of operators of a particular DataSet.  Also,
     * meaningful values for the parameters should be set ( using a
     * GUI ) before calling getResult() to apply the operator to the
     * DataSet this operator was added to.
     */
    public DiffractometerWavelengthToQ(){
        super( "Convert to Q" );
    }
    
    /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
    /**
     *  Construct an operator for a specified DataSet and with the
     *  specified parameter values so that the operation can be
     *  invoked immediately by calling getResult().
     *
     *  @param  ds          The DataSet to which the operation is applied
     *  @param  Qmin        The minimum Q value to be binned
     *  @param  Qmax        The maximum Q value to be binned
     *  @param  num_Q       The number of "bins" to be used between Qmin
     *                      and Qmax
     */

    public DiffractometerWavelengthToQ( DataSet ds, float Qmin,
                                        float Qmax, int num_Q ){
        this();                         // do the default constructor, then set
                                        // the parameter value(s) by altering a
                                        // reference to each of the parameters

        IParameter parameter = getParameter( 0 );
        parameter.setValue( new Float( Qmin ) );
        
        parameter = getParameter( 1 );
        parameter.setValue( new Float( Qmax ) );
        
        parameter = getParameter( 2 );
        parameter.setValue( new Integer( num_Q ) );
        
        setDataSet( ds );       // record reference to the DataSet that
                                // this operator should operate on
    }
    
    /* --------------------------- getCommand ------------------------------ */
    /**
     * @return the command name to be used with script processor: in
     * this case, WLtoQ
     */
    public String getCommand(){
        return "WLtoQ";
    }


    /* ------------------------- setDefaultParmeters ----------------------- */
    /**
     *  Set the parameters to default values.
     */
    public void setDefaultParameters(){
        UniformXScale scale = getXRange();
        
        parameters = new Vector();  // must do this to clear any old parameters
        
        float Qmin=Float.NaN;
        float Qmax=Float.NaN;
        
        if( scale!=null){
            Qmin=scale.getStart_x();
            if( Float.isNaN(Qmin) ) Qmin=0f;
            Qmax=scale.getEnd_x();
            if( Float.isNaN(Qmax) || Float.isInfinite(Qmax) ) Qmax=20f;
        }
        addParameter( new Parameter( "Min Q("+FontUtil.INV_ANGSTROM+")",
                                     new Float(Qmin) ) );
        addParameter( new Parameter( "Max Q("+FontUtil.INV_ANGSTROM+")",
                                     new Float(Qmax) ) );
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
        return new String( "Q("+FontUtil.INV_ANGSTROM+")" );
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
        
        return tof_calc.DiffractometerQofWavelength( angle_radians, x );
    }
    
    
    /* ---------------------------- getResult ------------------------------ */
    public Object getResult(){

        DataSet ds = this.getDataSet();  // get the current data set

        DataSetFactory factory = new DataSetFactory( 
                                                    ds.getTitle(),
                                                    "Inverse Angstroms",
                                                    "Q",
                                                    "Counts",
                                                    "Scattering Intensity" );
        
        // must take care of the operation log... this starts with it empty
        DataSet new_ds = factory.getDataSet(); 
        new_ds.copyOp_log( ds );
        new_ds.addLog_entry( "Converted to Q" );
        
        // copy the attributes of the original data set
        new_ds.setAttributeList( ds.getAttributeList() );
        
        // get the scale parameters 
        float Qmin = ( (Float)(getParameter(0).getValue()) ).floatValue();
        float Qmax = ( (Float)(getParameter(1).getValue()) ).floatValue();
        int   num_Q = ( (Integer)(getParameter(2).getValue()) ).intValue() + 1;
        
        // validate bounds
        if ( Qmin > Qmax ){             // swap bounds to be in proper order
            float temp = Qmin;
            Qmin = Qmax;
            Qmax = temp;
        }
        
        UniformXScale new_Q_scale;
        if ( num_Q <= 1.0 || Qmin >= Qmax )       // no valid scale set
            new_Q_scale = null;
        else
            new_Q_scale = new UniformXScale( Qmin, Qmax, num_Q );  
        
        Data             data,
                         new_data;
        DetectorPosition position;
        float            y_vals[];       // y_values from one spectrum
        float            errors[];       // errors from one spectrum
        float            Q_vals[];      // Q values at bin boundaries
        XScale           Q_scale;
        float            angle_radians;
        int              num_data = ds.getNum_entries();
        AttributeList    attr_list;
        
        for ( int j = 0; j < num_data; j++ ){
            data = ds.getData_entry( j );   // get reference to the data entry
            attr_list = data.getAttributeList();
            
            // get the detector position and initial path length 
            position=(DetectorPosition)
                attr_list.getAttributeValue(Attribute.DETECTOR_POS);
            
            // has needed attributes so convert it to Q
            if( position != null ){
                
                angle_radians=position.getScatteringAngle();
                Q_vals           = data.getX_scale().getXs();
                for ( int i = 0; i < Q_vals.length; i++ )
                    Q_vals[i] = 
                        tof_calc.DiffractometerQofWavelength( angle_radians, 
                                                              Q_vals[i] );
                arrayUtil.Reverse(Q_vals);
                Q_scale = new VariableXScale( Q_vals );
                
                y_vals  = data.getY_values();
                arrayUtil.Reverse(y_vals);

                errors  = data.getErrors();
                arrayUtil.Reverse(errors);
                
                new_data = Data.getInstance( Q_scale, 
                                             y_vals, 
                                             errors, 
                                             data.getGroup_ID() );

                new_data.setAttributeList( attr_list );
                                                        
                if ( new_Q_scale != null )   // resample if a num_bins>1
                    new_data.resample( new_Q_scale, IData.SMOOTH_NONE ); 

                new_ds.addData_entry( new_data );      
            }
        }
        new_ds.addOperator(new DiffractometerQToD());
        new_ds.addOperator(new DiffractometerQToWavelength());
        
        return new_ds;
    }  
    
    /* ------------------------------ clone ------------------------------- */
    /**
     * Get a copy of the current DDiffractometerWavelengthToQ
     * Operator.  The list of * parameters and the reference to the
     * DataSet to which it applies are also copied.
     */
    public Object clone(){
        DiffractometerWavelengthToQ new_op = 
            new DiffractometerWavelengthToQ( );
        // copy the data set associated
        // with this operator
        new_op.setDataSet( this.getDataSet() );
        new_op.CopyParametersFrom( this );
        
        return new_op;
    }
}
