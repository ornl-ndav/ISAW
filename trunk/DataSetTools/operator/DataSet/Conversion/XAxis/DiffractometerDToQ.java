/*
 * File:  DiffractometerDToQ.java
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
 * Revision 1.6  2003/01/09 15:32:15  dennis
 * Added getDocumentation(), main test program and java docs on getResult().
 * (Chris Bouzek)
 *
 * Revision 1.5  2002/11/27 23:17:04  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/09/19 16:00:23  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.3  2002/07/08 20:46:00  pfpeterson
 * Now uses String constants in FontUtil.
 *
 * Revision 1.2  2002/07/02 17:07:24  pfpeterson
 * Now uses string constants defined in IsawGUI.Isaw and adds
 * operator for Q->Wavelength.
 *
 * Revision 1.1  2002/06/19 21:58:02  pfpeterson
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
import  DataSetTools.viewer.*;
import  DataSetTools.retriever.*;

/**
 * This operator converts a d-spacing DataSet for a Diffractometer to Q.
 */

public class DiffractometerDToQ extends XAxisConversionOp{
    /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
    /**
     * Construct an operator with a default parameter list.  If this
     * constructor is used, the operator must be subsequently added to
     * the list of operators of a particular DataSet.  Also,
     * meaningful values for the parameters should be set ( using a
     * GUI ) before calling getResult() to apply the operator to the
     * DataSet this operator was added to.
     */
    public DiffractometerDToQ(){
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

    public DiffractometerDToQ( DataSet ds, float Qmin,
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
     * The command name to be used with script processor: in this
     * case, DtoQ
     */
    public String getCommand(){
        return "DtoQ";
    }


    /* ------------------------- setDefaultParameters ----------------------- */
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
        if( i<0 || i>=num_data ) return Float.NaN;
        if( Float.isNaN(x) ) return Float.NaN;

        return tof_calc.DiffractometerQofDSpacing( x );
    }

    /* ---------------------- getDocumentation --------------------------- */
    /**
     *  Returns the documentation for this method as a String.  The format
     *  follows standard JavaDoc conventions.
     */
    public String getDocumentation()
    {
      StringBuffer s = new StringBuffer("");
      s.append("@overview This operator converts the X-axis units on a ");
      s.append("DataSet from D-spacing to Q values.");
      s.append("@assumptions The DataSet must contain spectra with ");
      s.append("attributes giving the detector position. In addition, ");
      s.append("it is assumed that the XScale for the spectra represents ");
      s.append("D-spacing.");
      s.append("@algorithm Creates a new DataSet which has the same title ");
      s.append("as the input DataSet, the same y-values as the input ");
      s.append("DataSet, and whose X-axis units have been converted from  ");
      s.append("D-spacing to Q values.  ");
      s.append("The new DataSet also has a message appended to its log ");
      s.append("indicating that a conversion to units of Q values on the ");
      s.append("X-axis was done.  ");
      s.append("Furthermore, two operators are added to the DataSet: ");
      s.append("DiffractometerQToD and DiffractometerQToWavelength.");
      s.append("@param ds The DataSet to which the operation is applied.");
      s.append("@param Qmin The minimum Q value to be binned.");
      s.append("@param Qmax The maximum Q value to be binned.");
      s.append("@param num_Q The number of \"bins\" to be used between ");
      s.append("Qmin and Qmax.");
      s.append("@return A new DataSet which is the result of converting the ");
      s.append("input DataSet's X-axis units to Q values.");
      return s.toString();
    }

    /* ---------------------------- getResult ------------------------------- */
    /**
     *  Converts the input DataSet to a DataSet which is identical except that
     *  the new DataSet's X-axis units have been converted from D-spacing to
     *  Q values.
     *
     *	@return DataSet whose X-axis units have been converted to Q values.
     */
    public Object getResult(){

        DataSet ds = this.getDataSet();  // get the current data set
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
        DataSetFactory factory = new DataSetFactory(
                                                    ds.getTitle(),
                                                    "Inverse Angstroms",
                                                    "Q",
                                                    "Counts",
                                                    "Scattering Intensity" );

        // ## must take care of the operation log... this starts with it empty
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

            Q_vals = data.getX_scale().getXs();
            for ( int i = 0; i < Q_vals.length; i++ )
                Q_vals[i] = tof_calc.DiffractometerQofDSpacing( Q_vals[i] );

            arrayUtil.Reverse(Q_vals);
            Q_scale = new VariableXScale( Q_vals );

            y_vals  = data.getY_values();
            arrayUtil.Reverse(y_vals);

            errors  = data.getErrors();
            arrayUtil.Reverse(errors);

            new_data = Data.getInstance( Q_scale, y_vals, errors,
                                         data.getGroup_ID() );

            new_data.setAttributeList( attr_list );

            if ( new_Q_scale != null ) // rebin if number of bins>1
                new_data.resample( new_Q_scale, IData.SMOOTH_NONE );

            new_ds.addData_entry( new_data );
        }
        new_ds.addOperator(new DiffractometerQToD());
        new_ds.addOperator(new DiffractometerQToWavelength());

        return new_ds;
    }

    /* ------------------------------ clone ------------------------------- */
    /**
     * Get a copy of the current DDiffractometerDToQ Operator. The
     * list of parameters and the reference to the DataSet to which it
     * applies are also copied.
     */
    public Object clone(){
        DiffractometerDToQ new_op =
            new DiffractometerDToQ( );
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
      float min_1 = (float)0.5;
      float max_1 = (float)1.0;
      float min_2 = (float)6.5;
      float max_2 = (float)12.0;
      String file_name = "/home/groups/SCD_PROJECT/SampleRuns/GPPD12358.RUN";
      //"D:\\ISAW\\SampleRuns\\GPPD12358.RUN";

      try
      {
        RunfileRetriever rr = new RunfileRetriever( file_name );
        DataSet ds1 = rr.getDataSet(1);

        //need to have units of D-spacing first
        DiffractometerTofToD tofd =
                             new DiffractometerTofToD(ds1, min_1, max_1, 6000);

        DataSet new_ds1 = (DataSet)tofd.getResult();
        ViewManager viewer = new ViewManager(new_ds1, IViewManager.IMAGE);

        DiffractometerDToQ op = 
                           new DiffractometerDToQ(new_ds1, min_2, max_2, 1000);
        DataSet new_ds2 = (DataSet)op.getResult();
        ViewManager new_viewer = new ViewManager(new_ds2, IViewManager.IMAGE);
        System.out.println(op.getDocumentation());
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
}
