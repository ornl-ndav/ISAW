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
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
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

import java.io.*;
import java.util.Vector;
import DataSetTools.dataset.*;
import DataSetTools.instruments.DetectorInfo;
import DataSetTools.util.*;
import DataSetTools.math.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Special.*;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.parameter.*;

/**
 * This operator focusses the incident spectrum from a beam monitor to
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

    /**
     * Executes the operator using the current values of the parameters.
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
        DetectorPosition detpos=(DetectorPosition)focus_data.getAttributeValue(Attribute.DETECTOR_POS);
        float path_length=((Float)focus_data.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
        path_length+=detpos.getDistance();
        float theta=(float)(180.*detpos.getScatteringAngle()/Math.PI);
        float theta_min=theta;
        float theta_max=theta;

        DetectorInfo[] dets
            =(DetectorInfo[])focus_data.getAttributeValue(Attribute.DETECTOR_INFO_LIST);

        float tth;
        for( int i=0 ; i<dets.length ; i++ ){
            tth=(float)(180.*dets[i].getPosition().getScatteringAngle()/Math.PI);
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
        }else{
            System.out.println("USAGE: FocusIncidentSpectrum2 <filename>");
        }
    }
    
}
