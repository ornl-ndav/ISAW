/*
 * File:  DownstreamMonitorID.java   
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
 *  $Log$
 *  Revision 1.2  2002/11/27 23:21:43  pfpeterson
 *  standardized header
 *
 *  Revision 1.1  2002/05/24 14:21:22  pfpeterson
 *  added to cvs
 *
 *   
 */

package DataSetTools.operator.Generic.Special;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.retriever.RunfileRetriever;

/**
 * This operator determines what the group ID of the opstream monitor
 * is for a given monitor dataset.
 */

public class DownstreamMonitorID extends    GenericSpecial {
    /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
    /**
     * Construct an operator with a default parameter list.
     */
    public DownstreamMonitorID( ){
        super( "Downstream Monitor GID" );
    }

    /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
    /**
     *  Construct operator to determine the downstream monitor group id.
     *
     *  @param  mds     The monitor data set.
     */
    
    public DownstreamMonitorID( DataSet ds ){
        this();
        
        parameters=new Vector();
        addParameter(new Parameter("Monitor",ds));
    }

    
    /* ------------------------- setDefaultParmeters ----------------------- */
    /**
     *  Set the parameters to default values.
     */
    public void setDefaultParameters(){
        parameters = new Vector();  // must do this to create empty list of 
                                    // parameters

        parameters=new Vector();
        addParameter( new Parameter("Monitor",DataSet.EMPTY_DATA_SET ));
    }
    
    /* --------------------------- getCommand ------------------------------ */
    /**
     * @return	the command name to be used with script processor.
     */
    public String getCommand(){
        return "DnMonitorID";
    }
    
    /* --------------------------- getResult ------------------------------- */
    /*
     * This returns the group id of the detector with the largest
     * TOTAL_COUNT attribute. If no downstream monitor is found it will
     * return -1.
     */
    public Object getResult(){
        DataSet mon      = (DataSet)(getParameter(0).getValue());
        Integer     mon_id   = new Integer(-1);
        float   monCount = -1f;

        for( int i=0 ; i<mon.getNum_entries() ; i++ ){
	    Data monD = mon.getData_entry(i);
	    Float ang = (Float)
		monD.getAttributeValue(Attribute.RAW_ANGLE);
	    if( Math.abs(Math.abs(ang.floatValue())-0f)==0 ){
		Float count = (Float)
                    monD.getAttributeValue(Attribute.TOTAL_COUNT);
		if(count.floatValue()>monCount){
		    monCount=count.floatValue();

		    mon_id=(Integer)monD.getAttributeValue(Attribute.GROUP_ID);
		}
	    }
	}

        return mon_id;
    }  


    /* ------------------------------ clone ------------------------------- */
    /**
     * Get a copy of the current SpectrometerEvaluator Operator.  The
     * list of parameters is also copied.
     */

    public Object clone(){
        DownstreamMonitorID new_op = 
            new DownstreamMonitorID( );
        
        new_op.CopyParametersFrom( this );
        
        return new_op;
    }

    public static void main(String[] args){
        if(args.length==1){
            String filename=args[0];
            RunfileRetriever rr=new RunfileRetriever(filename);
            DataSet mds=rr.getDataSet(0);
            
            //DownstreamMonitorID op=new DownstreamMonitorID();
            DownstreamMonitorID op=new DownstreamMonitorID(mds);
            System.out.println("For "+filename+" monitor GID= "
                               +op.getResult());
        }else{
            System.out.println("USAGE: DownstreamMonitorID <filename>");
        }
    }
}
