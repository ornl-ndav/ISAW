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
 * Contact : Peter Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source, Building 360
 *           Argonne National Laboratory
 *           9700 South Cass Avenue
 *           Argonne, IL 60439-4814
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 */
package Operators;

import DataSetTools.operator.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;
import java.util.*;

/** 
 *  This operator removes empty detectors from a data set. 
 */
public class Crunch extends GenericSpecial{
    private static final String TITLE = "Crunch";

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
     */
    public Crunch( DataSet ds ){

	this(); 
	parameters = new Vector();
	addParameter( new Parameter("DataSet parameter", ds) );

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
    }
    
    /* ----------------------------- getResult ------------------------------ */ 
    /** 
     *  Executes this operator using the values of the current parameters.
     *
     *  @return If successful, this operator produces a DataSet
     *  containing the the original DataSet minus the dead detectors.
     */
    public Object getResult(){
	DataSet ds        =  (DataSet)(getParameter(0).getValue());
	
	if( ds==null )
	    return new ErrorString( "DataSet is null in Crunch" );

	// initialize new data set to be the same as the old
	String       title = ds.getTitle();
	OperationLog oplog = ds.getOp_log();
	String     x_units = ds.getX_units();
	String     x_label = ds.getX_label();
	String     y_units = ds.getY_units();
	String     y_label = ds.getY_label();
	DataSet     new_ds = new DataSet( title,
					  oplog,
					  x_units, x_label,
					  y_units, y_label );
	ds.addLog_entry("Applied Crunch");
	Object obj=ds.clone();
	if( obj instanceof DataSet ){
	    new_ds = (DataSet)obj;
	}else{
	    System.out.println( "Could not clone DataSet" );
	}

	int[] bad_det = new int[new_ds.getNum_entries()];
	int bi=0;
	int MAX_ID=new_ds.getMaxGroupID();
	// remove the empty detectors
	for( int i=1 ; i<=MAX_ID ; i++ ){
	    Data det=new_ds.getData_entry_with_id(i);
	    if( det == null ){
		// do nothing
	    }else{
		Float count=(Float)
		    det.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
		if( count.floatValue() == 0 ){
		    new_ds.removeData_entry_with_id(i);
		}
	    }
	}
	    
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
	RunfileRetriever rr = new RunfileRetriever( filename );
	DataSet ds = rr.getDataSet(1);

	Crunch op = new Crunch( ds );
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
