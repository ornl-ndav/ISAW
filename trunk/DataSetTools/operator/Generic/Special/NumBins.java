/*
 * File:  NumBins.java   
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
 *           Argonne, IL 60439-4845
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2002/05/24 16:51:07  pfpeterson
 *  added to cvs
 *
 *  Revision 1.1  2002/05/24 14:21:23  pfpeterson
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
 * This operator determines what the number of bins of a given
 * DataBlock over the specified range.
 */

public class NumBins extends    GenericSpecial {
    /* ----------------------- DEFAULT CONSTRUCTOR ------------------------- */
    /**
     * Construct an operator with a default parameter list.
     */
    public NumBins( ){
        super( "Number of Bins" );
    }

    /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
    /**
     *  Construct operator to determine the upstream monitor group id.
     *
     *  @param  mds     The monitor data set.
     */
    
    public NumBins( DataSet ds, Integer id, Float xmin, Float xmax ){
        this();
        
        parameters=new Vector();
        addParameter(new Parameter("DataSet",ds));
        addParameter(new Parameter("Group ID",id));
        addParameter(new Parameter("Minimum x",xmin));
        addParameter(new Parameter("Maximum x",xmax));
    }

    
    /* ------------------------- setDefaultParmeters ----------------------- */
    /**
     *  Set the parameters to default values.
     */
    public void setDefaultParameters(){
        parameters = new Vector();  // must do this to create empty list of 
                                    // parameters

        parameters=new Vector();
        addParameter( new Parameter("DataSet",DataSet.EMPTY_DATA_SET ));
        addParameter(new Parameter("Group ID",new Integer(0)));
        addParameter(new Parameter("Minimum x",new Float(0)));
        addParameter(new Parameter("Maximum x",
                                   new Float(Float.POSITIVE_INFINITY)));
    }
    
    /* --------------------------- getCommand ------------------------------ */
    /**
     * @return	the command name to be used with script processor.
     */
    public String getCommand(){
        return "NumBins";
    }
    
    /* --------------------------- getResult ------------------------------- */
    /*
     * This returns the group id of the detector with the largest
     * TOTAL_COUNT attribute. If no upstream monitor is found it will
     * return -1.
     */
    public Object getResult(){
        DataSet ds      = (DataSet)(getParameter(0).getValue());
        int     id   = ((Integer)getParameter(1).getValue()).intValue();
        float   xmin = ((Float)getParameter(2).getValue()).floatValue();
        float   xmax = ((Float)getParameter(3).getValue()).floatValue();

        int numBins=-1;
        XScale xscale;
        {
            Data sds=ds.getData_entry_with_id(id);
            if(sds==null){
                return new Integer(numBins);
            }else{
                xscale=sds.getX_scale();
            }
        }
        float xs_min=xscale.getStart_x();
        float xs_max=xscale.getEnd_x();
        int maxNumBins=xscale.getNum_x();

        // confirm that the min and max are sorted correctly
        if(xmin>xmax){ // this should never be entered
            float temp=xmin;
            xmin=xmax;
            xmax=temp;
        }
        if(xs_min>xs_max){ // this should never be entered
            float temp=xs_min;
            xs_min=xs_max;
            xs_max=temp;
        }

        // make sure that bounds don't go outside of the data set
        if(xmin<xs_min) xmin=xs_min;
        if(xmax>xs_max) xmax=xs_max;

        // if we are looking at the full range then just return the
        // total number of bins
        if(xmin==xs_min && xmax==xs_max) return new Integer(maxNumBins-1);

        numBins=0;
        float[] xs=ds.getData_entry_with_id(id).getX_values();
        for(int i=0 ; i<maxNumBins ; i++ ){
            if(xs[i]>=xmin){
                if(xs[i]<=xmax){
                    numBins++;
                }else{
                    i=maxNumBins;
                }
            }
        }
        return new Integer(numBins-1);
    }  


    /* ------------------------------ clone ------------------------------- */
    /**
     * Get a copy of the current SpectrometerEvaluator Operator.  The
     * list of parameters is also copied.
     */

    public Object clone(){
        NumBins new_op = 
            new NumBins( );
        
        new_op.CopyParametersFrom( this );
        
        return new_op;
    }

    public static void main(String[] args){
        String filename="";
        if(args.length==1){
            filename=args[0];
            RunfileRetriever rr=new RunfileRetriever(filename);
            DataSet ds=rr.getDataSet(1);
            Float xmin=new Float( 3000);
            Float xmax=new Float(30000);
            NumBins op;
            
            op=new NumBins(ds,new Integer(1),xmin, xmax);
            System.out.println("For "+filename+"(1)= "+op.getResult());
            op=new NumBins(ds,new Integer(2),xmin, xmax);
            System.out.println("For "+filename+"(2)= "+op.getResult());
            op=new NumBins(ds,new Integer(3),xmin, xmax);
            System.out.println("For "+filename+"(3)= "+op.getResult());
            op=new NumBins(ds,new Integer(4),xmin, xmax);
            System.out.println("For "+filename+"(4)= "+op.getResult());
            op=new NumBins(ds,new Integer(5),xmin, xmax);
            System.out.println("For "+filename+"(5)= "+op.getResult());
        }else{
            System.out.println("USAGE: NumBins <filename>");
        }
    }
}
