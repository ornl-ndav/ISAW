/*
 * File:  Crunch2.java 
 *
 * Copyright (C) 2002, Peter Peterson
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
 * Revision 1.2  2003/10/09 19:34:15  rmikk
 * Fixed the clone method to return a new Crunch2 instead of a
 *   new Crunch
 *
 * Revision 1.1  2002/12/09 20:03:24  pfpeterson
 * Added to CVS.
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
 *  This operator removes detectors from a data set so it has the same
 *  group ids as the second parameter.
 */
public class Crunch2 extends GenericSpecial{
  private static final String  TITLE = "Crunch";
  private static final boolean DEBUG = false;
  
  /* ----------------------- Default constructor ------------------------- */ 
  /**
   * Creates operator with title "Operator Template" and a default
   * list of parameters.
   */  
  public Crunch2(){
    super( TITLE );
  }
    
  /* --------------------------- Constructor ----------------------------- */ 
  /** 
   * Creates operator with title "Operator Template" and the specified
   * list of parameters.  The getResult method must still be used to
   * execute the operator.
   *
   * @param ds Sample DataSet to remove dead detectors from.
   * @param tds DataSet to use a template for removing groups.
   * @param new_ds Whether to make a new DataSet
   */
  public Crunch2( DataSet ds, DataSet tds, boolean new_ds ){
    this(); 
    
    getParameter(0).setValue(ds);
    getParameter(1).setValue(tds);
    getParameter(2).setValue(new Boolean(new_ds));
  }
    
  /* -------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return "Crunch", the command used to invoke this operator in
   * Scripts
   */
  public String getCommand(){
    return "Crunch";
  }
    
  /**
   *
   */
  public String getDocumentation(){
    StringBuffer sb=new StringBuffer(100);

    sb.append("@overview This operator removes detectors from a data set so "
              +"it has the same group IDs as the second parameter.\n");
    sb.append("@param The DataSet to be 'Crunch'ed.\n");
    sb.append("@param The DataSet used as a template.\n");
    sb.append("@param Whether or not to create a new DataSet.\n");
    sb.append("@return A reference to the 'Crunch'ed DataSet.\n");
    sb.append("@error If either of the DataSet parameters are null\n");
    sb.append("@error If the DataSet and template are the same object\n");

    return sb.toString();
  }

  /* ----------------------- setDefaultParameters ------------------------- */ 
  /** 
   * Sets default values for the parameters.  This must match the data
   * types of the parameters.
   */
  public void setDefaultParameters(){
    parameters = new Vector();
    addParameter(new Parameter("DataSet to Crunch", DataSet.EMPTY_DATA_SET ));
    addParameter(new Parameter("Template DataSet",  DataSet.EMPTY_DATA_SET ));
    addParameter(new Parameter("Make new DataSet",  new Boolean(false)));
  }
    
  /* ----------------------------- getResult ------------------------------ */ 
  /** 
   * Executes this operator using the values of the current
   * parameters.
   *
   * @return If successful, this operator produces a DataSet
   * containing the the original DataSet minus the groups missing in
   * the template DataSet.
   */
  public Object getResult(){
    DataSet ds        = (DataSet)(getParameter(0).getValue());
    DataSet tds       = (DataSet)(getParameter(1).getValue());
    boolean mk_new_ds = ((Boolean)getParameter(2).getValue()).booleanValue();
    DataSet new_ds    = null;

    if( ds==null )
      return new ErrorString( "DataSet is null in Crunch" );
    if( tds==null)
      return new ErrorString( "Template DataSet is null in Crunch" );
    if( ds==tds)
      return new ErrorString( "DataSet and Template must be different" );

    // initialize new data set to be the same as the old
    String       title = ds.getTitle();
    OperationLog oplog = ds.getOp_log();
    String     x_units = ds.getX_units();
    String     x_label = ds.getX_label();
    String     y_units = ds.getY_units();
    String     y_label = ds.getY_label();

    // initialize new_ds
    if(mk_new_ds){
      new_ds=(DataSet)ds.clone();
    }else{
      new_ds=ds;
    }

    // get a new DataBlock which will be instatiated several times
    Data data  = null;
    Data tdata = null;
    int  gid   = 0;

    // remove the proper DataSets
    for( int i=0 ; i<ds.getNum_entries() ; i++ ){
      data=new_ds.getData_entry(i);
      if(data==null) continue;
      
      gid=data.getGroup_ID();
      tdata=tds.getData_entry_with_id(gid);
      if(tdata==null){
        new_ds.removeData_entry_with_id(gid);
        i--;
      }
    }

    // in debug mode compare the list of Data kept
    if(DEBUG){
      int[] list=new int[new_ds.getNum_entries()];

      for( int i=0 ; i<list.length ; i++ ){
        list[i]=new_ds.getData_entry(i).getGroup_ID();
      }
      System.out.println("new="+IntList.ToString(list));

      list=new int[tds.getNum_entries()];
      for( int i=0 ; i<list.length ; i++ ){
        list[i]=tds.getData_entry(i).getGroup_ID();
      }
      System.out.println("old="+IntList.ToString(list));
    }

    // add some information to the log
    new_ds.addLog_entry("Applied Crunch( "+ds+", "+tds+" )");

    // return the right stuff
    return new_ds;
  }
  
  /* ------------------------------- clone -------------------------------- */ 
  /** 
   * Creates a clone of this operator.
   */
  public Object clone(){ 
    Operator op = new Crunch2();
    op.CopyParametersFrom( this );
    return op;
  }

  /* ------------------------------- main --------------------------------- */ 
  /** 
   * Test program to verify that this will complile and run ok.  
   *
   */
  public static void main( String args[] ){
    System.out.println("Crunch2 compiled and ran");
  }
}
