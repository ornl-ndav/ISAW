/*
 * File:  Crunch.java 
 *
 * Copyright (C) 2001, Peter Peterson
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
 * $Log$
 * Revision 1.6  2002/11/27 23:29:54  pfpeterson
 * standardized header
 *
 *
 */
package Operators;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;
import java.util.*;

/** 
 *  This operator removes detectors from a data set according to three
 *  criteria, all involve the total counts. First it removes detectors
 *  with zero counts. Next it removes detectors below the user
 *  specified threshold. Finally the average and standard of deviation
 *  is found for the total counts, then detectors outside of the user
 *  specified number of sigma are removed (generally too many counts).
 */
public class Crunch extends GenericSpecial{
    private static final String  TITLE = "Crunch";
    private static final boolean DEBUG = false;

    /* ------------------------ Default constructor ------------------------- */ 
    /**
     *  Creates operator with title "Operator Template" and a default
     *  list of parameters.
     */  
    public Crunch()
    {
	super( TITLE );
    }
    
    /* ---------------------------- Constructor ----------------------------- */ 
    /** 
     *  Creates operator with title "Operator Template" and the
     *  specified list of parameters.  The getResult method must still
     *  be used to execute the operator.
     *
     *  @param  ds          Sample DataSet to remove dead detectors from.
     *  @param  min_count   Minimum counts to keep bank
     *  @param  width       How many sigma around average to keep
     */
    public Crunch( DataSet ds, float width, float min_count, boolean new_ds ){

	this(); 
	parameters = new Vector();
	addParameter( new Parameter("DataSet parameter", ds) );
	addParameter( new Parameter("Minum counts to keep", new Float(min_count)));
	addParameter( new Parameter("Number of sigma to keep", new Float(width)));
        addParameter( new Parameter("Make new DataSet", new Boolean(new_ds)));

    }
    
    /* --------------------------- getCommand ------------------------------- */ 
    /** 
     * Get the name of this operator to use in scripts
     * 
     * @return  "Crunch", the command used to invoke this operator in Scripts
     */
    public String getCommand(){
	return "Crunch";
    }
    
    /* ----------------------- setDefaultParameters ------------------------- */ 
    /** 
     * Sets default values for the parameters.  This must match the
     * data types of the parameters.
     */
    public void setDefaultParameters(){
	parameters = new Vector();
	addParameter(new Parameter("DataSet parameter",DataSet.EMPTY_DATA_SET ));
	addParameter(new Parameter("Minum counts to keep", new Float(0.0f)));
	addParameter(new Parameter("Number of sigma to keep",new Float(2.0f)));
        addParameter(new Parameter("Make new DataSet", new Boolean(false)));
    }
    
    /* ----------------------------- getResult ------------------------------ */ 
    /** 
     *  Executes this operator using the values of the current parameters.
     *
     *  @return If successful, this operator produces a DataSet
     *  containing the the original DataSet minus the dead detectors.
     */
    public Object getResult(){
	DataSet ds        = (DataSet)(getParameter(0).getValue());
	float   min_count = ((Float) (getParameter(1).getValue())).floatValue();
	float   width     = ((Float) (getParameter(2).getValue())).floatValue();
	boolean mk_new_ds = ((Boolean)(getParameter(3).getValue())).booleanValue();

	if( ds==null )
	    return new ErrorString( "DataSet is null in Crunch" );

	// initialize new data set to be the same as the old
	String       title = ds.getTitle();
	OperationLog oplog = ds.getOp_log();
	String     x_units = ds.getX_units();
	String     x_label = ds.getX_label();
	String     y_units = ds.getY_units();
	String     y_label = ds.getY_label();
	DataSet     new_ds = null;

        // initialize new_ds
        if(mk_new_ds){
            new_ds=(DataSet)ds.clone();
        }else{
            new_ds=ds;
        }

	// first remove detectors below min_count
	int[] bad_det = new int[new_ds.getNum_entries()];
	int bi=0;
	int MAX_ID=new_ds.getMaxGroupID();
	for( int i=1 ; i<=MAX_ID ; i++ ){
	    Data det=new_ds.getData_entry_with_id(i);
	    if( det == null ){ continue; }
	    Float count=(Float)
		det.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
	    if( count.floatValue() < min_count ){
		new_ds.removeData_entry_with_id(i);
	    }
	}

	// find the average total counts
	float avg=0f;
	float num_det=0f;
	for( int i=1 ; i<=MAX_ID ; i++ ){
	    Data det=new_ds.getData_entry_with_id(i);
	    if( det == null ){ continue; }
	    Float count=(Float)
	       det.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
	    avg=avg+count.floatValue();
	    if(DEBUG)System.out.println(i+"  "+count);
	    num_det++;
	}
	if(num_det!=0f){
	    avg=avg/num_det;
	}else{
	    avg=0f;
	}
	
	float dev=0f;
	if( avg != 0f ){
	    // find the stddev of the total counts
	    for( int i=1 ; i<=MAX_ID ; i++ ){
		Data det=new_ds.getData_entry_with_id(i);
		if( det==null ){ continue; }
		Float count=(Float)
		    det.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
		dev=dev+(avg-count.floatValue())*(avg-count.floatValue());
	    }
	    if(avg!=0){ dev=dev/(num_det-1f); }
	    dev=(float)Math.sqrt((double)dev);
	    if(DEBUG)System.out.println(num_det+"  "+avg+"  "+dev);
	    
	    // remove detectors outside of width*sigma
	    width=width*dev;
	    for( int i=1 ; i<=MAX_ID ; i++ ){
		Data det=new_ds.getData_entry_with_id(i);
		if( det==null ){ continue; }
		Float count=(Float)
		    det.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
		float diff=(float)Math.abs((double)(avg-count.floatValue()));
		if( diff>width ){
		    new_ds.removeData_entry_with_id(i);
		    if(DEBUG)System.out.println("removing det"+i+" with "
                                                +count+" total counts");
		}
	    }
	}
	
	new_ds.addLog_entry("Applied Crunch( "+ds+", "
                            +min_count+", "+width/dev+" )");
	return new_ds;
    }
    
    /* ------------------------------- clone -------------------------------- */ 
    /** 
     *  Creates a clone of this operator.
     */
    public Object clone(){ 
	Operator op = new Crunch();
	op.CopyParametersFrom( this );
	return op;
    }
    

    /* ------------------------------- main --------------------------------- */ 
    /** 
     * Test program to verify that this will complile and run ok.  
     *
     */
    public static void main( String args[] ){
	System.out.println("Test of Crunch starting...");
	
	String filename="/IPNShome/pfpeterson/ISAW/SampleRuns/GPPD12358.RUN";
	//String filename="/IPNShome/pfpeterson/data/ge_10k/glad4606.run";
	RunfileRetriever rr = new RunfileRetriever( filename );
	DataSet ds = rr.getDataSet(1);

	Crunch op = new Crunch( ds, 2.0f, 1000f, true );
	Object obj = op.getResult();
	if(obj instanceof DataSet ){
	    DataSet new_ds=(DataSet)obj;
	    ViewManager vm1 = new ViewManager(     ds, IViewManager.IMAGE );
	    ViewManager vm2 = new ViewManager( new_ds, IViewManager.IMAGE );
	}else{
	    System.out.println( "Operator returned" + obj );
	}
	
	System.out.println("Test of Crunch done.");
    }
}
