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
 * Revision 1.5  2004/05/07 15:51:41  dennis
 * Made log messages consistent for Crunch.java Crunch2.java
 * OmitNullData.java.  Make appropriate log entry whether or
 * not Data blocks were actually deleted.
 *
 * Revision 1.4  2004/05/07 04:14:10  dennis
 * Now prints the group IDs of data blocks that are omitted, if
 * DEBUG is true.
 * If a new DataSet is not created, it now notifies observers of
 * the original DataSet, if Data blocks are deleted.
 * Now uses a Vector to keep track of omitted group IDs.
 *
 * Revision 1.3  2004/04/28 21:11:29  dennis
 * Now also omits detectors at origin. (Bad position info)
 * When DEBUG is on this now prints the reason the group was
 * omitted.
 * DEBUG set on.
 *
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
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Messaging.*;


/** 
 *  This operator removes null Data blocks from a DataSet.
 */
  public class OmitNullData extends GenericSpecial
  {

    public static final String NULL_DATA_BLOCK = "Null Data Block";
    public static final String LOW_COUNT       = "Low Count";
    public static final String DETECTOR_AT_ORIGIN = "Detector at Origin";

    private static final String  TITLE = "OmitNullData";
    private static final boolean DEBUG = true;

    /* ------------------------ Default constructor ------------------------ */ 
    /**
     *  Creates operator with title "OmitNullData" and a default
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
    public OmitNullData( DataSet ds, boolean new_ds )
    {
      this(); 
      parameters = new Vector();
      addParameter( new Parameter("DataSet parameter", ds) );
      addParameter( new Parameter("Make new DataSet", new Boolean(new_ds)));
    }
    
    /* -------------------------- getCommand ------------------------------- */ 
    /** 
     * Get the name of this operator to use in scripts
     * 
     * @return  "OmitNullData", the command used to invoke this operator 
     *           in Scripts
     */
    public String getCommand()
    {
      return TITLE;
    }
    
    /* ---------------------- setDefaultParameters ------------------------- */ 
    /** 
     * Sets default values for the parameters.  This must match the
     * data types of the parameters.
     */
    public void setDefaultParameters()
    {
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
      s.append("@overview This operator removes null Data blocks from ");
      s.append("a DataSet ");
      s.append("@assumptions The specified DataSet ds is not null.\n");
      s.append("@algorithm This operator removes Data blocks with zero total ");
      s.append("counts from the specified DataSet. It also appends a log ");
      s.append("message indicating that the OmitNullData operator was ");
      s.append("applied to the DataSet.\n");
      s.append("@param ds Sample DataSet to remove null data from.\n");
      s.append("@param new_ds Whether to make a new DataSet.\n");
      s.append("@return new_ds DataSet containing the original DataSet minus ");
      s.append("the Data block(s) with zero total counts.\n");
      s.append("@error Returns an error if the specified DataSet ds ");
      s.append("is null.\n");
      return s.toString();
    }
    
    /* ---------------------------- getResult ------------------------------ */ 
    /** 
     *  Removes dead detectors from the specified DataSet.
     *
     *  @return DataSet containing the the original DataSet minus the dead 
     *  detectors (if successful).
     */
    public Object getResult()
    {
      DataSet ds        = (DataSet)(getParameter(0).getValue());
      boolean mk_new_ds =((Boolean)(getParameter(1).getValue())).booleanValue();

      if( ds==null )
        return new ErrorString( "DataSet is null in OmitNullData" );

      // initialize new data set to be the same as the old
      DataSet new_ds = null;

      // initialize new_ds
      if(mk_new_ds)
        new_ds=(DataSet)ds.clone();
      else
        new_ds=ds;

      // Remove Data blocks with zero total count
      Vector dead_ones = new Vector();
      int min_count = 0;

      String    err_string = null;
      int       n_data = ds.getNum_entries();
      Attribute attr;
      for( int i = n_data-1; i >= 0; i-- )   // go through list backwards so
      {                                      // we can easily remove bad ones
        err_string = null;
        Data det = new_ds.getData_entry(i);

        if( det == null )
          err_string = NULL_DATA_BLOCK;

        if ( err_string == null )
        { 
          attr = det.getAttribute(Attribute.TOTAL_COUNT);
          if ( attr != null )
          {
            Float count = (Float)attr.getValue();
            if( count.floatValue() <= min_count )
              err_string = LOW_COUNT; 
          } 
        }

        if ( err_string == null )
        {
          attr = det.getAttribute(Attribute.DETECTOR_POS);
          if ( attr != null && attr.getValue() instanceof DetectorPosition )
          {
            DetectorPosition pos = (DetectorPosition)attr.getValue();
            float sphere_coords[] = pos.getSphericalCoords();
            if ( sphere_coords[0] <= 0 )
              err_string = DETECTOR_AT_ORIGIN;
          }  
        }
      
        if ( err_string != null )
        {
          int id = det.getGroup_ID();
          new_ds.removeData_entry(i);
          dead_ones.add( new Integer(id) );
          if ( DEBUG )
            System.out.println("Removed Group " + id + ": " + err_string );
         }
      }
 
      int final_list[] = null;
      if( dead_ones.size() > 0 )
      {
        final_list = new int[ dead_ones.size() ];
        for ( int i=0; i < dead_ones.size(); i++ )
          final_list[i] = ((Integer)dead_ones.elementAt(i)).intValue();

        Arrays.sort(final_list);
      
        new_ds.addLog_entry("Applied OmitNullData( " + ds + 
                            ", " + mk_new_ds + 
                            " ), removed Data blocks : " +
                            IntList.ToString(final_list) );
        if ( !mk_new_ds )
          ds.notifyIObservers( IObserver.DATA_DELETED );
      }
      else
        new_ds.addLog_entry("Applied OmitNullData to " + ds + 
                            ", " + mk_new_ds +
                            ", and removed NO Data blocks ");

      if( DEBUG )
      {
        if ( final_list == null )
          System.out.println("NO DATA BLOCKS REMOVED");
        else
          System.out.println(
           "Removed Groups: " + IntList.ToString(final_list) );
      }

      return new_ds;
    }


    /* ------------------------------ clone -------------------------------- */ 
    /** 
     *  Creates a clone of this operator.
     */
    public Object clone()
    { 
      Operator op = new OmitNullData();
      op.CopyParametersFrom( this );
      return op;
    }
    

    /* ------------------------------ main --------------------------------- */ 
    /** 
     * Test program to verify that this will compile and run ok.  
     *
     */
    public static void main( String args[] )
    {
      System.out.println("Test of OmitNullData starting...");
	
      String filename="/home/groups/SCD_PROJECT/SampleRuns/GPPD12358.RUN";
      RunfileRetriever rr = new RunfileRetriever( filename );
      DataSet ds = rr.getDataSet(1);

      OmitNullData op = new OmitNullData( ds, true );
      Object obj = op.getResult();
      if(obj instanceof DataSet )
      {
        DataSet new_ds=(DataSet)obj;
        ViewManager vm1 = new ViewManager(     ds, IViewManager.IMAGE );
        ViewManager vm2 = new ViewManager( new_ds, IViewManager.IMAGE );
      }
      else
        System.out.println( "Operator returned: " + obj );
	
      /*-- added by Chris Bouzek --*/
      System.out.println("Documentation: " + op.getDocumentation());
      /*---------------------------*/
	
      System.out.println("Test of OmitNullData done.");
    }
}
