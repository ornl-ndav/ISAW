/*
 * File:   FocusIncidentSpectrum2.java     
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
 * $Log$
 * Revision 1.8  2003/12/15 02:06:09  bouzekc
 * Removed unused imports.
 *
 * Revision 1.7  2003/02/12 21:54:44  dennis
 * Changed to use PixelInfoList instead of SegmentInfoList
 *
 * Revision 1.6  2003/01/29 17:52:07  dennis
 * Added getDocumentation() method. (Chris Bouzek)
 *
 * Revision 1.5  2003/01/15 20:57:30  dennis
 * Changed to used SegmentInfo, SegInfoListAttribute, etc.
 *
 * Revision 1.4  2002/11/27 23:29:54  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/09/19 15:57:38  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.2  2002/07/10 15:53:20  pfpeterson
 * Implements HiddenOperator.
 *
 * Revision 1.1  2002/07/03 19:29:31  pfpeterson
 * Added to CVS.
 *
 *
 */

package Operators;

import java.util.Vector;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.HistogramTable;
import DataSetTools.dataset.PixelInfoList;
import DataSetTools.dataset.XScale;
import DataSetTools.math.DetectorPosition;
import DataSetTools.math.Vector3D;
import DataSetTools.math.tof_data_calc;
import DataSetTools.operator.HiddenOperator;
import DataSetTools.operator.Operator;
import DataSetTools.operator.Parameter;
import DataSetTools.operator.Generic.Special.GenericSpecial;
import DataSetTools.operator.Generic.Special.UpstreamMonitorID;
import DataSetTools.parameter.IParameter;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.util.SharedData;

/**
 * This operator focuses the incident spectrum from a beam monitor to
 * a bank of detectors at a specified total flight path length and
 * range of angles. This based on the FORTRAN SUBROUTINE
 * inc_spec_focus from IPNS. It is an overriden version of
 * DataSetTools.operator.DataSet.Special.FocusIncidentSpectrum with a
 * shorter parameter list.
 */

public class  FocusIncidentSpectrum2 extends GenericSpecial 
                                                    implements HiddenOperator{
    private static final boolean DEBUG=false;
    /**
     * Construct an operator with a default parameter list.  If this
     * constructor is used, the operator must be subsequently added to
     * the list of operators of a particular DataSet.  Also,
     * meaningful values for the parameters should be set ( using a
     * GUI ) before calling getResult() to apply the operator to the
     * DataSet this operator was added to.
     */
    public FocusIncidentSpectrum2(){
        super("Focus Incident Spectrum" );
    }

    /**
     * This is the shortened constructor that will not appear in the
     * operator GUI. It calculates most of the values needed for the
     * full constructor which it then calls
     *
     * @param mds           The monitor DataSet that will be focused.
     * @param group_id      The group_id of the monitor DataBlock.
     * @param ds            The DataSet the monitor will be focused to.
     * @param new_group_id  The group_id of the DataBlock the monitor
     *                      will be focused to.
     */
    public FocusIncidentSpectrum2( DataSet mds, int group_id, DataSet ds, 
                                   int new_group_id){
        this();
        
        IParameter parameter = getParameter(0);
        parameter.setValue( mds );

        parameter=getParameter(1);
        parameter.setValue( new Integer( group_id ) );
        
        parameter=getParameter(2);
        parameter.setValue(ds);
        
        parameter=getParameter(3);
        parameter.setValue(new Integer(new_group_id));
    }

    /**
     *  The command name to be used with the script processor: in this
     *  case IncSpecFocus.
     */
    public String getCommand(){
        return "IncSpecFocus";//super.getCommand();
    }

    /**
     *  Set the parameters to default values.
     */
    public void setDefaultParameters(){
        parameters = new Vector();  // must do this to clear any old parameters
        addParameter(new Parameter("Monitor DataSet",DataSet.EMPTY_DATA_SET));
        addParameter(new Parameter("Group ID of monitor", new Integer(0)));
        addParameter(new Parameter("DataSet to focus to",DataSet.EMPTY_DATA_SET));
        addParameter(new Parameter("Group ID of focused Data",new Integer(0)));

        if(DEBUG){
            System.out.println(this.getClass().getName()+"("+this.getCommand()
                               +") has "+parameters.size()+" parameters");
        }
    }

    /* ---------------------- getDocumentation --------------------------- */
    /**
     *  Returns the documentation for this method as a String.  The format
     *  follows standard JavaDoc conventions.
     */
    public String getDocumentation()
    {
      StringBuffer s = new StringBuffer("");
      s.append("@overview This operator focuses the incident spectrum from a ");
      s.append("beam monitor to a bank of detectors at a specified total ");
      s.append("flight path length and range of angles.\n");
      s.append("Note that it is an overriden version of ");
      s.append("DataSetTools.operator.DataSet.Special.FocusIncidentSpectrum ");
      s.append("with a shorter parameter list.\n");
      s.append("@assumptions The specified group_id of the monitor DataSet must ");
      s.append("be a valid group ID.\n");
      s.append("The specified new_group_id of the DataBlock must be a valid ");
      s.append("group ID.\n");
      s.append("@algorithm The calculations for this operator are based on the ");
      s.append("FORTRAN SUBROUTINE inc_spec_focus from IPNS.  These are ");
      s.append("in the file tof_data_calc in the DataSetTools/math/ ");
      s.append("directory.\n");
      s.append("First this operator retrieves the monitor data from the ");
      s.append("specified group ID of the DataSet mds.  It also retrieves ");
      s.append("the focusing data from the specified new_group_id of the ");
      s.append("DataSet ds.\n");
      s.append("It then retrieves the detector position, path length, theta ");
      s.append("values, and segment information from the DataBlock to be ");
      s.append("focused to.\n");
      s.append("Then it uses the inc_spec_focus routine to calculate the ");
      s.append("spectrum focus.\n");
      s.append("Finally it makes an empty copy of the mds DataSet and copies ");
      s.append("the focused data to the new DataSet.\n");    
      s.append("@param mds The monitor DataSet that will be focused.\n");
      s.append("@param group_id The group_id of the monitor DataBlock.\n");
      s.append("@param ds The DataSet the monitor will be focused to.\n");
      s.append("@param new_group_id  The group_id of the DataBlock the ");
      s.append("monitor will be focused to.\n");
      s.append("@return A new DataSet containing the focused incident ");
      s.append("spectra.\n");
      s.append("@error Returns an error if the specified group_id of the ");
      s.append("monitor DataSet is not a valid group ID.\n");
      s.append("@error Returns an error if the specified new_group_id of ");
      s.append("the DataBlock is not a valid group ID. \n");
      return s.toString();
    }
    
    /**
     * Focuses the incident spectra from the monitor DataSet mds's group_id to the 
     * DataSet ds's new_group_id.
     *
     * @return New DataSet containing the focused incident spectra.
     */
    public Object getResult(){
        // get the parameter values
        DataSet mds=(DataSet)getParameter(0).getValue(); 
        int group_id=((Integer)getParameter(1).getValue()).intValue();
        DataSet ds=(DataSet)getParameter(2).getValue(); 
        int new_group_id=((Integer)getParameter(3).getValue()).intValue();

        if(DEBUG){
            System.out.println("P0="+mds+" P1="+group_id+" P2="+ds+" P3="
                               +new_group_id);
        }

        Data monitor_data=mds.getData_entry_with_id(group_id);
        if(monitor_data==null){
            String message="ERROR: no monitor data entry with the group ID "
                +group_id;
            SharedData.addmsg(message);
            return message;
        }
        if(!monitor_data.isHistogram()){
            monitor_data=new HistogramTable(monitor_data,false,
                                            monitor_data.getGroup_ID());
        }

        Data focus_data=ds.getData_entry_with_id(new_group_id);
        if(focus_data==null){
            String message="ERROR: no focusing data entry with the group ID "
                +new_group_id;
            SharedData.addmsg(message);
            return message;
        }
        if(!focus_data.isHistogram()){
            focus_data=new HistogramTable(focus_data,false,
                                          focus_data.getGroup_ID());
        }

        // get information out of the data being focused to
        XScale new_x_scale=focus_data.getX_scale();
        DetectorPosition detpos = (DetectorPosition)
                         focus_data.getAttributeValue(Attribute.DETECTOR_POS);
        float path_length=((Float)focus_data.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
        path_length+=detpos.getDistance();
        float theta=(float)(180.*detpos.getScatteringAngle()/Math.PI);
        float theta_min=theta;
        float theta_max=theta;

        PixelInfoList pil = (PixelInfoList)
                      focus_data.getAttributeValue(Attribute.PIXEL_INFO_LIST);

        float tth;
        for( int i=0 ; i<pil.num_pixels(); i++ ){
            Vector3D vec = pil.pixel(i).position();
            DetectorPosition det_pos = new DetectorPosition( vec );
            tth=(float)(180.*det_pos.getScatteringAngle()/Math.PI);
            if(DEBUG) System.out.println("det["+i+"]="+tth);
            if(tth<theta_min) theta_min=tth;
            if(tth>theta_max) theta_max=tth;
        }

        if(DEBUG){
            System.out.println("position="+path_length+"m "+theta_min+"<"+theta
                               +"<"+theta_max);
            System.out.println("xscale="+new_x_scale);
        }

        // perform the calculation
        Data new_data=tof_data_calc.IncSpecFocus( monitor_data,new_x_scale,
                                                  path_length, theta,
                                                  theta_min, theta_max,
                                                  new_group_id);
        DataSet new_ds=mds.empty_clone();
        new_ds.addData_entry(new_data);
        return new_ds;
  }

    /**
     * Get a copy of the current FocusIncidentSpectrum Operator.  The
     * list of parameters and the reference to the DataSet to which it
     * applies are also copied.
     */
    public Object clone(){
        FocusIncidentSpectrum2 new_op = new FocusIncidentSpectrum2( );

        // copy the data set associated with this operator
        new_op.CopyParametersFrom( this );
        
        return new_op;
    }

    public static void main(String[] args){
        if(args.length==1){
            String filename=args[0];
            RunfileRetriever rr=new RunfileRetriever(filename);
            DataSet mds=rr.getDataSet(0);
            DataSet ds=rr.getDataSet(1);
            Operator op=new UpstreamMonitorID(mds);
            int monNum=((Integer)op.getResult()).intValue();
            op=new FocusIncidentSpectrum2(mds,monNum,ds,1);
            op.getResult();
	    /*-- added by Chris Bouzek ---*/
	    System.out.println("Documentation: " + op.getResult());
	    /*----------------------------*/
        }else{
            System.out.println("USAGE: FocusIncidentSpectrum2 <filename>");
	    /*-- added by Chris Bouzek ---*/
	    Operator op=new FocusIncidentSpectrum2();
	    System.out.println("Documentation: " + op.getDocumentation());
	    /*----------------------------*/
        }
    }
    
}
