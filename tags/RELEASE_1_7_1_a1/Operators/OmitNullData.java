/*
 * File:  OmitNullData.java 
 *
 * Copyright (C) 2004, Tom Worlton
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
 * Contact : Thomas Worlton
 *		 IPNS Division
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
 * Revision 1.2  2004/03/24 18:34:01  dennis
 * Converted to Unix text format.
 *
 * Revision 1.1  2004/03/24 18:24:14  dennis
 * Initial commit of OmitNullData operator, adapted from
 * "Crunch" operator.  (T. Worlton)
 *
 *
 */
package Operators;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import java.util.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.SpecialStrings.*;
/** 
 *  This operator removes null Data blocks from a DataSet.
 */
public class OmitNullData extends GenericSpecial{
    private static final String  TITLE = "OmitNullData";
    private static final boolean DEBUG = false;
    private static final int BUFFER_SIZE_INCREMENT = 100;

    /* ------------------------ Default constructor ------------------------ */ 
    /**
     *  Creates operator with title "Operator Template" and a default
     *  list of parameters.
     */  
    public OmitNullData()
    {
	super( TITLE );
    }
    
    /* ---------------------------- Constructor ---------------------------- */ 
    /** 
     *  Creates operator with title "OmitNullData" and the
     *  specified list of parameters.  The getResult method must still
     *  be used to execute the operator.
     *
     *  @param  ds          Sample DataSet to remove dead detectors from.
     *  @param  new_ds      Whether to make a new DataSet.
     */
    public OmitNullData( DataSet ds, boolean new_ds ){
	this(); 
	parameters = new Vector();
	addParameter( new Parameter("DataSet parameter", ds) );
      addParameter( new Parameter("Make new DataSet", new Boolean(new_ds)));
    }
    
    /* -------------------------- getCommand ------------------------------- */ 
    /** 
     * Get the name of this operator to use in scripts
     * 
     * @return  "OmitNullData", the command used to invoke this operator in Scripts
     */
    public String getCommand(){
	return "OmitNullData";
    }
    
    /* ---------------------- setDefaultParameters ------------------------- */ 
    /** 
     * Sets default values for the parameters.  This must match the
     * data types of the parameters.
     */
    public void setDefaultParameters(){
	parameters = new Vector();
	addParameter(new Parameter("DataSet parameter",DataSet.EMPTY_DATA_SET ));
      addParameter(new Parameter("Make new DataSet", new Boolean(false)));
    }

    /* ---------------------- getDocumentation --------------------------- */
    /**
     *  Returns the documentation for this method as a String.  The format
     *  follows standard JavaDoc conventions.
     */
    public String getDocumentation()
    {
      StringBuffer s = new StringBuffer("");
      s.append("@overview This operator removes null Data blocks from a DataSet ");
      s.append("@assumptions The specified DataSet ds is not null.\n");
      s.append("@algorithm This operator removes Data blocks with zero total ");
      s.append("counts from the specified DataSet. It also appends a log ");
      s.append("message indicating that the OmitNullData operator was applied ");
      s.append("to the DataSet.\n");
      s.append("@param ds Sample DataSet to remove null data from.\n");
      s.append("@param new_ds Whether to make a new DataSet.\n");
      s.append("@return new_ds DataSet containing the original DataSet minus the ");
      s.append("Data block(s) with zero total counts.\n");
      s.append("@error Returns an error if the specified DataSet ds is null.\n");
      return s.toString();
    }
    
    /* ---------------------------- getResult ------------------------------ */ 
    /** 
     *  Removes dead detectors from the specified DataSet.
     *
     *  @return DataSet containing the the original DataSet minus the dead 
     *  detectors (if successful).
     */
    public Object getResult(){
	DataSet ds        = (DataSet)(getParameter(0).getValue());
	boolean mk_new_ds = ((Boolean)(getParameter(1).getValue())).booleanValue();

	if( ds==null )
	    return new ErrorString( "DataSet is null in OmitNullData" );

	// initialize new data set to be the same as the old
	String       title = ds.getTitle();
	OperationLog oplog = ds.getOp_log();
	String     x_units = ds.getX_units();
	String     x_label = ds.getX_label();
	String     y_units = ds.getY_units();
	String     y_label = ds.getY_label();
	DataSet     new_ds = null;
      int		dead_ones[];
      int		ndead=0;

        // initialize new_ds
        if(mk_new_ds){
            new_ds=(DataSet)ds.clone();
        }else{
            new_ds=ds;
        }

	// Remove Data blocks with zero total count
	int[] bad_det = new int[new_ds.getNum_entries()];
      dead_ones = new int[BUFFER_SIZE_INCREMENT];
      int min_count = 0;
	int MAX_ID=new_ds.getMaxGroupID();
	for( int i=1 ; i<=MAX_ID ; i++ ){
	    Data det=new_ds.getData_entry_with_id(i);
	    if( det == null ){ continue; }
	    Float count=(Float)
		det.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
	    if( count.floatValue() <= min_count ){
		new_ds.removeData_entry_with_id(i);
            if( DEBUG ) {
            System.out.println( 
              "removing data group " + i + " with " + count + " total counts" );
            }
            dead_ones = AppendToList ( i, dead_ones, ndead );
            ndead++;
	    }
	}
      int final_list[] = new int[ ndead ];
      for ( int i=0; i < ndead; i++ )
        final_list[i] = dead_ones[i];
      
      if( DEBUG ) {
        System.out.println( 
           "Null Data Blocks = " + IntList.ToString(final_list) );
       }
	
	if(ndead > 0) {
	new_ds.addLog_entry("Removed null data from ( " + ds + ", data blocks "
                            + IntList.ToString(final_list) + " )" );}
	return new_ds;
    }

    /* ----------------------------- AppendToList --------------------- */
    private static int[] AppendToList( int new_int, int list[], int inext ) {
      int new_list[];
      if ( inext >= list.length ) {
        new_list = new int[ list.length + BUFFER_SIZE_INCREMENT ];
        for ( int i = 0; i < list.length; i++ )
          new_list[i] = list[i];
      }
      else
        new_list = list;
      new_list [inext] = new_int;
      return new_list;
    }
   
    /* ------------------------------ clone -------------------------------- */ 
    /** 
     *  Creates a clone of this operator.
     */
    public Object clone(){ 
	Operator op = new OmitNullData();
	op.CopyParametersFrom( this );
	return op;
    }
    

    /* ------------------------------ main --------------------------------- */ 
    /** 
     * Test program to verify that this will compile and run ok.  
     *
     */
    public static void main( String args[] ){
	System.out.println("Test of OmitNullData starting...");
	
	String filename="/home/groups/SCD_PROJECT/SampleRuns/GPPD12358.RUN";
	RunfileRetriever rr = new RunfileRetriever( filename );
	DataSet ds = rr.getDataSet(1);

	OmitNullData op = new OmitNullData( ds, true );
	Object obj = op.getResult();
	if(obj instanceof DataSet ){
	    DataSet new_ds=(DataSet)obj;
	    ViewManager vm1 = new ViewManager(     ds, IViewManager.IMAGE );
	    ViewManager vm2 = new ViewManager( new_ds, IViewManager.IMAGE );
	}else{
	    System.out.println( "Operator returned: " + obj );
	}
	
	/*-- added by Chris Bouzek --*/
	System.out.println("Documentation: " + op.getDocumentation());
	/*---------------------------*/
	
	System.out.println("Test of OmitNullData done.");
    }
}
