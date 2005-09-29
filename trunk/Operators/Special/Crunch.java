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
 * Revision 1.4  2005/09/29 22:18:33  dennis
 * If the TOTAL_COUNTS attribute is NOT set on a Data block, the
 * total counts for that Data block will now be calculated, used
 * for this operator AND the TOTAL_COUNTS attribute will be set
 * on the Data block.
 * This fixes a problem with trying to filter out bad Data blocks
 * from HIPPO data, which do not have the TOTAL_COUNTS attribute
 * set by the NeXus loader.
 *
 * Revision 1.3  2005/08/24 20:11:31  dennis
 * Added/moved to Macros->Data Set->Edit List menu.
 *
 * Revision 1.2  2004/05/10 20:42:28  dennis
 * Test program now just instantiates a ViewManager to diplay
 * calculated DataSet, rather than keeping a reference to it.
 * This removes an Eclipse warning about a local variable that is
 * not read.
 *
 * Revision 1.1  2004/05/07 17:56:59  dennis
 * Moved operators that extend GenericSpecial from Operators
 * to Operators/Special
 *
 * Revision 1.14  2004/05/07 15:51:41  dennis
 * Made log messages consistent for Crunch.java Crunch2.java
 * OmitNullData.java.  Make appropriate log entry whether or
 * not Data blocks were actually deleted.
 *
 * Revision 1.13  2004/05/07 14:43:45  dennis
 * Now notifies observers of Data blocks that are deleted.
 * Now includes list of IDs that were removed in log message.
 *
 * Revision 1.12  2004/05/03 18:05:07  dennis
 * Removed unused variables bad_det[] and bi.
 *
 * Revision 1.11  2004/04/29 21:14:22  dennis
 * Now steps through the list of Data blocks based on index
 * rather than group ID.
 *
 * Revision 1.10  2004/03/15 19:36:52  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.9  2004/03/15 03:36:58  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.8  2003/07/07 15:55:43  bouzekc
 * Added missing param tags in constructor and
 * getDocumentation().  Fixed spelling error in parameter
 * name.
 *
 * Revision 1.7  2003/01/29 17:52:07  dennis
 * Added getDocumentation() method. (Chris Bouzek)
 *
 * Revision 1.6  2002/11/27 23:29:54  pfpeterson
 * standardized header
 *
 */
package Operators.Special;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.Messaging.*;

import java.util.*;

/** 
 *  This operator removes detectors from a data set according to three
 *  criteria, all involve the total counts. First it removes detectors
 *  with zero counts. Next it removes detectors below the user
 *  specified threshold. Finally the average and standard of deviation
 *  is found for the total counts, then detectors outside of the user
 *  specified number of sigma are removed (generally too many counts).
 */
public class Crunch extends GenericSpecial
{
    private static final String  TITLE = "Crunch";
    private static final boolean DEBUG = false;

    /* ----------------------- Default constructor ------------------------- */ 
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
     *  @param  width       How many sigma around average to keep
     *  @param  min_count   Minimum counts to keep bank
     *  @param  new_ds      Whether to make a new DataSet.
     */
    public Crunch( DataSet ds, float width, float min_count, boolean new_ds )
    {
      this(); 
      parameters = new Vector();
      addParameter( new Parameter("DataSet parameter", ds) );
      addParameter( new Parameter("Minum counts to keep",new Float(min_count)));
      addParameter( new Parameter("Number of sigma to keep", new Float(width)));
      addParameter( new Parameter("Make new DataSet", new Boolean(new_ds)));
    }
    
    /* -------------------------- getCommand ------------------------------- */ 
    /** 
     * Get the name of this operator to use in scripts
     * 
     * @return  "Crunch", the command used to invoke this operator in Scripts
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

    
    /* ----------------------- setDefaultParameters ------------------------ */ 
    /** 
     * Sets default values for the parameters.  This must match the
     * data types of the parameters.
     */
    public void setDefaultParameters()
    {
      parameters = new Vector();
      addParameter(new Parameter("DataSet parameter",DataSet.EMPTY_DATA_SET ));
      addParameter(new Parameter("Minimum counts to keep", new Float(0.0f)));
      addParameter(new Parameter("Number of sigma to keep",new Float(2.0f)));
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
      s.append("@overview This operator removes detectors from a DataSet ");
      s.append("according to three criteria, all of which involve the total ");
      s.append("counts.  If the TOTAL_COUNT attribute is present in the ");
      s.append("Data block for a detector, the value from the TOTAL_COUNT ");
      s.append("attribute will be used.  If the TOTAL_COUNT attribute is ");
      s.append("not present, the total counts will be calculated and the ");
      s.append("calculated total counts will BOTH be used in this operator ");
      s.append("AND will be set as the TOTAL_COUNTS attribute of the ");
      s.append("Data block.\n");
      s.append("@assumptions The specified DataSet ds is not null.\n");
      s.append("@algorithm First this operator removes detectors with zero ");
      s.append("counts from the specified DataSet. Next it removes detectors ");
      s.append("below the user specified threshold. Finally the average and ");
      s.append("standard deviation is found for the total counts, then ");
      s.append("detectors outside of the user specified number of sigma are ");
      s.append("removed (generally too many counts).  It also appends a log ");
      s.append("message indicating that the Crunch operator was applied to ");
      s.append("the DataSet.\n");
      s.append("@param ds Sample DataSet to remove dead detectors from.\n");
      s.append("@param min_count Minimum counts to keep.\n");
      s.append("@param width How many sigma around the average to keep.\n");
      s.append("@param new_ds Whether to make a new DataSet.\n");
      s.append("@return DataSet containing the the original DataSet minus ");
      s.append("the dead detectors.\n");
      s.append("@error Returns an error if the specified DataSet ");
      s.append("ds is null.\n");
      return s.toString();
    }
    
    /* ----------------------------- getResult ----------------------------- */ 
    /** 
     *  Removes dead detectors from the specified DataSet.
     *
     *  @return DataSet containing the the original DataSet minus the dead 
     *  detectors (if successful).
     */
    public Object getResult()
    {
      DataSet ds        = (DataSet)(getParameter(0).getValue());
      float   min_count = ((Float) (getParameter(1).getValue())).floatValue();
      float   width     = ((Float) (getParameter(2).getValue())).floatValue();
      boolean mk_new_ds =((Boolean)(getParameter(3).getValue())).booleanValue();

      Vector dead_ones = new Vector();    // keep track of the ones removed

      if( ds==null )
        return new ErrorString( "DataSet is null in Crunch" );

      // initialize new data set to be the same as the old
      DataSet  new_ds = null;
      if(mk_new_ds)
        new_ds=(DataSet)ds.clone();
      else
        new_ds=ds;

      // first remove detectors below min_count
      int n_data = new_ds.getNum_entries();
      for( int i = n_data - 1; i >= 0; i-- )
      {
        Data det = new_ds.getData_entry(i);
        if( det == null )  
          continue;
                                            // use TOTAL_COUNT attribute if
                                            // present, else calculate total
        Float count = (Float)
               det.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
        float total_count;                   
        if ( count == null )
        {
          total_count = 0;
          float ys[] = det.getY_values();
          if ( ys != null )
            for ( int k = 0; k < ys.length; k++ )
              total_count += ys[k]; 

          det.setAttribute(
             new FloatAttribute( Attribute.TOTAL_COUNT, total_count) );
        } 
        else
          total_count = count.floatValue();

        if( total_count < min_count )
        {
          dead_ones.add( new Integer( det.getGroup_ID() ) );
          new_ds.removeData_entry(i);
        }
      }

      // find the average total counts
      float avg=0f;
      float num_det=0f;
      for( int i = 0; i < new_ds.getNum_entries(); i++ )
      {
        Data det = new_ds.getData_entry(i);
        if( det == null ) 
          continue; 
        Float count = (Float)
               det.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
        avg = avg + count.floatValue();
        if( DEBUG )
          System.out.println( i + "  " + count );
        num_det++;
      }

      if( num_det != 0f )
        avg = avg / num_det;
      else
        avg = 0f;
	
      float dev = 0f;
      if( avg != 0f )
      {
        // find the stddev of the total counts
        for( int i = 0; i < new_ds.getNum_entries(); i++ )
        {
          Data det = new_ds.getData_entry(i);
          if( det == null )
            continue;
          Float count = (Float)
                det.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
          dev = dev + (avg-count.floatValue())*(avg-count.floatValue());
      }

      if(avg != 0)
        dev = dev / (num_det - 1f); 
      dev = (float)Math.sqrt( (double)dev );
      if(DEBUG)System.out.println( num_det + "  "+avg+"  "+dev );
	    
      // remove detectors outside of width * sigma
      width = width * dev;
      n_data = new_ds.getNum_entries();
      for( int i= n_data-1 ; i >= 0; i-- )
      {
        Data det = new_ds.getData_entry(i);
        if( det == null )
         continue; 
        Float count = (Float)
               det.getAttributeList().getAttributeValue(Attribute.TOTAL_COUNT);
        float diff = (float)Math.abs(avg-count.floatValue());
        if( diff > width )
        {
          dead_ones.add( new Integer( det.getGroup_ID() ) );
          new_ds.removeData_entry(i);
          if(DEBUG)System.out.println("removing det"+i+" with "
                                       +count+" total counts");
        }
      }
    }

    int final_list[] = null;
    if( dead_ones.size() > 0 )
    {
      final_list = new int[ dead_ones.size() ];
      for ( int i=0; i < dead_ones.size(); i++ )
        final_list[i] = ((Integer)dead_ones.elementAt(i)).intValue();

      Arrays.sort(final_list);

      new_ds.addLog_entry("Applied Crunch( " + ds + 
                          ", " + min_count + 
                          ", " + width/dev + 
                          ", " + mk_new_ds + 
                          " ), removed Data blocks : " +
                          IntList.ToString(final_list) );
      if ( !mk_new_ds )
        ds.notifyIObservers( IObserver.DATA_DELETED );
    }
    else
      new_ds.addLog_entry("Applied Crunch( " + ds +
                          ", " + min_count +
                          ", " + width/dev +
                          ", " + mk_new_ds + 
                          " ), and removed NO Data blocks " );

    return new_ds;
  }
    
    /* ------------------------------ clone -------------------------------- */ 
    /** 
     *  Creates a clone of this operator.
     */
    public Object clone(){ 
	Operator op = new Crunch();
	op.CopyParametersFrom( this );
	return op;
    }
    

    /* ------------------------------ main --------------------------------- */ 
    /** 
     * Test program to verify that this will compile and run ok.  
     *
     */
    public static void main( String args[] ){
	System.out.println("Test of Crunch starting...");
	
	//String filename="/IPNShome/pfpeterson/ISAW/SampleRuns/GPPD12358.RUN";
	//String filename="/IPNShome/pfpeterson/data/ge_10k/glad4606.run";
	String filename="/home/groups/SCD_PROJECT/SampleRuns/GPPD12358.RUN";
	RunfileRetriever rr = new RunfileRetriever( filename );
	DataSet ds = rr.getDataSet(1);

	Crunch op = new Crunch( ds, 2.0f, 1000f, true );
	Object obj = op.getResult();
	if(obj instanceof DataSet ){
	    DataSet new_ds=(DataSet)obj;
	    new ViewManager(     ds, IViewManager.IMAGE );
	    new ViewManager( new_ds, IViewManager.IMAGE );
	}else{
	    System.out.println( "Operator returned: " + obj );
	}
	
	/*-- added by Chris Bouzek --*/
	System.out.println("Documentation: " + op.getDocumentation());
	/*---------------------------*/
	
	System.out.println("Test of Crunch done.");
    }
}
