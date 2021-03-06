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
 * Revision 1.3  2005/08/24 20:11:31  dennis
 * Added/moved to Macros->Data Set->Edit List menu.
 *
 * Revision 1.2  2004/06/29 13:28:06  dennis
 * Changed name from Crunch to Crunch2 in returned ErrorStrings.
 *
 * Revision 1.1  2004/05/07 17:57:00  dennis
 * Moved operators that extend GenericSpecial from Operators
 * to Operators/Special
 *
 * Revision 1.9  2004/05/07 15:51:41  dennis
 * Made log messages consistent for Crunch.java Crunch2.java
 * OmitNullData.java.  Make appropriate log entry whether or
 * not Data blocks were actually deleted.
 *
 * Revision 1.8  2004/05/06 22:22:06  dennis
 * Changed Title to Crunch2 to match the command name and
 * file name for this operator.
 *
 * Revision 1.7  2004/03/30 16:05:58  dennis
 * Changed string returned by getCommand() to Crunch2 instead of Crunch.
 *
 * Revision 1.6  2004/03/15 19:36:52  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.5  2004/03/15 03:36:58  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.4  2004/01/30 02:19:38  bouzekc
 * Removed unused imports and variables.
 *
 * Revision 1.3  2003/12/15 01:45:31  bouzekc
 * Removed unused imports.
 *
 * Revision 1.2  2003/10/09 19:34:15  rmikk
 * Fixed the clone method to return a new Crunch2 instead of a
 *   new Crunch
 *
 * Revision 1.1  2002/12/09 20:03:24  pfpeterson
 * Added to CVS.
 *
 */
package Operators.Special;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.dataset.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Messaging.*;

import java.util.*;

/** 
 *  This operator removes detectors from a data set so it has the same
 *  group ids as the second parameter.
 */
public class Crunch2 extends GenericSpecial
{
  private static final String  TITLE = "Crunch2";
  private static final boolean DEBUG = false;
  
  /* ----------------------- Default constructor ------------------------- */ 
  /**
   * Creates operator with title "Operator Template" and a default
   * list of parameters.
   */  
  public Crunch2()
  {
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
  public Crunch2( DataSet ds, DataSet tds, boolean new_ds )
  {
    this(); 
    
    getParameter(0).setValue(ds);
    getParameter(1).setValue(tds);
    getParameter(2).setValue(new Boolean(new_ds));
  }
    
  /* -------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return "Crunch2", the command used to invoke this operator in
   * Scripts
   */
  public String getCommand()
  {
    return TITLE;
  }


  /* ---------------------------- getCategoryList -------------------------- */
  /**
   *  Get the list of categories describing where this operator should appear
   *  in the menu system.
   *
   *  @return an array of strings listing the menu where the operator 
   *  should appear.
   */
   public String[] getCategoryList()
   {
     return Operator.DATA_SET_EDIT_LIST_MACROS;
   }

    
  /* --------------------------- getDocumentation -------------------------- */
  /**
   *
   */
  public String getDocumentation()
  {
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
  public void setDefaultParameters()
  {
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
  public Object getResult()
  {
    DataSet ds        = (DataSet)(getParameter(0).getValue());
    DataSet tds       = (DataSet)(getParameter(1).getValue());
    boolean mk_new_ds = ((Boolean)getParameter(2).getValue()).booleanValue();
    DataSet new_ds    = null;

    Vector removed_ids = new Vector();    // keep track of the ones removed

    if( ds==null )
      return new ErrorString( "DataSet is null in Crunch2" );
    if( tds==null)
      return new ErrorString( "Template DataSet is null in Crunch2" );
    if( ds==tds)
      return new ErrorString( "DataSet and Template must be different" );

    // initialize new_ds
    if(mk_new_ds)
      new_ds=(DataSet)ds.clone();
    else
      new_ds=ds;

    // get a new DataBlock which will be instatiated several times
    Data data  = null;
    Data tdata = null;
    int  gid   = 0;

    // remove the proper DataSets
    for( int i=0 ; i<ds.getNum_entries() ; i++ )
    {
      data=new_ds.getData_entry(i);
      if(data==null) continue;
      
      gid=data.getGroup_ID();
      tdata=tds.getData_entry_with_id(gid);
      if(tdata==null)
      {
        removed_ids.add( new Integer(gid) );
        new_ds.removeData_entry_with_id(gid);
        i--;
      }
    }

    // in debug mode compare the list of Data kept
    if(DEBUG)
    {
      int[] list=new int[new_ds.getNum_entries()];

      for( int i=0 ; i<list.length ; i++ )
      {
        list[i]=new_ds.getData_entry(i).getGroup_ID();
      }
      System.out.println("new="+IntList.ToString(list));

      list=new int[tds.getNum_entries()];
      for( int i=0 ; i<list.length ; i++ )
      {
        list[i]=tds.getData_entry(i).getGroup_ID();
      }
      System.out.println("old="+IntList.ToString(list));
    }

    // add some information to the log
    int final_list[] = null;
    if( removed_ids.size() > 0 )
    {
      final_list = new int[ removed_ids.size() ];
      for ( int i=0; i < removed_ids.size(); i++ )
        final_list[i] = ((Integer)removed_ids.elementAt(i)).intValue();

      Arrays.sort(final_list);

      new_ds.addLog_entry("Applied Crunch2( " + ds +
                          ", " + tds + 
                          ", " + mk_new_ds +
                          " ), removed Data blocks : " +
                          IntList.ToString(final_list) );
      if ( !mk_new_ds )
        ds.notifyIObservers( IObserver.DATA_DELETED );
    }
    else
      new_ds.addLog_entry("Applied Crunch2( " + ds + 
                          ", " + tds + 
                          ", " + mk_new_ds +
                          " ), and removed NO Data blocks." );

    // return the right stuff
    return new_ds;
  }
  
  /* ------------------------------- clone -------------------------------- */ 
  /** 
   * Creates a clone of this operator.
   */
  public Object clone()
  { 
    Operator op = new Crunch2();
    op.CopyParametersFrom( this );
    return op;
  }

  /* ------------------------------- main --------------------------------- */ 
  /** 
   * Test program to verify that this will complile and run ok.  
   *
   */
  public static void main( String args[] )
  {
    System.out.println("Crunch2 compiled and ran");
  }
}
