/*
 * File:  RestrictDomain.java
 *
 * Copyright (C) 2004 Dennis Mikkelson 
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2004/06/27 05:35:49  dennis
 * Changed to use resample() method on Data block to obtain the new
 * Data restricted to smaller interval.  This is less efficient than
 * the previous version for histogramed Data, but will work for all
 * types of Data blocks (tables, models and event).  It also fixes an
 * incompatibiltiy of the original version with previous versions of
 * ISAW, due to recent change of XScale.restrict() to calculate the
 * new right hand endpoint using getI_GLB() instead of getI().  This
 * introduced an off by one error when used with previous versions
 * of ISAW.
 *
 * Revision 1.1  2004/06/25 15:31:01  dennis
 * This operator restricts the domain of a DataSet to a specified
 * subinterval of the original x-scale.
 *
 *
 */
package Operators.Special;

import java.util.*;
import DataSetTools.operator.*;
import DataSetTools.dataset.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.Messaging.*;

public class RestrictDomain implements Wrappable
{
  public DataSet ds          = DataSet.EMPTY_DATA_SET;
  public float   x_min       = Float.NEGATIVE_INFINITY; 
  public float   x_max       = Float.POSITIVE_INFINITY; 
  public boolean make_new_ds = true;

  /**
   *  Get the command name for this operator
   *
   *  @return The command name: "RestrictDomain"
   */
  public String getCommand() 
  {
    return "RestrictDomain";
  }

  /**
   *  Get the documentation for this operator
   *
   *  @return String explaining the use of this operator
   */
  public String getDocumentation() 
  {
    StringBuffer s = new StringBuffer(  );
    s.append( "@overview This operator restricts the domain of the specified ");
    s.append( "DataSet to the interval [x_min,x_max].  If the make_new_ds " );
    s.append( "parameter is true, the original DataSet will not be altered, " );
    s.append( "but a new DataSet with the restricted domain will be ");
    s.append( "constructed.  If the make_new_ds parameter is false, the ");
    s.append( "Data blocks in the specified DataSet will will be altered ");
    s.append( "if needed to restrict the domain of the Data.");

    s.append( "@algorithm Each Data block that includes x-values outside of " );
    s.append( "the interval [x_min, x_max ] will be replaced by a new Data " );
    s.append( "block containing only those x,y values that lie within the " );
    s.append( "interval.  Data blocks whose x-values already lie entirely ");
    s.append( "within the interval will not be changed.   Data blocks whose ");
    s.append( "x-values lie entirely outside of the interval will be ");  
    s.append( "deleted.  The restricted interval must be large enough to ");
    s.append( "contain at least two sample points in the original x_scale, ");
    s.append( "or the Data block will be deleted.");

    s.append( "@param ds      The DataSet to be restricted to [x_min,x_max]." );

    s.append( "@param x_min       The left endpoint of the new interval." );

    s.append( "@param x_max       The right endpoint of the new interval." );

    s.append( "@param make_new_ds Flag indicating whether or not to make a "); 
    s.append( "new DataSet." );

    s.append( "@return If make_new_ds is true, return a new DataSet with new ");
    s.append( "Data blocks restricted to [x_min,x_max].  If make_new_ds is " );
    s.append( "false, the original DataSet is returned with its Data blocks ");
    s.append( "restricted to the specified interval. ");

    s.append( "@error If the original DataSet is null or empty, or if any " );
    s.append( "of the Data blocks has invalid end points, or if x_min and ");
    s.append( "x_max are invalid, error strings will be returned.");
    return s.toString(  );
  }

  /**
   *  Restrict the DataSet to the specified domain.
   */
  public Object calculate() 
  {
    if ( ds == null )
      return new ErrorString("ERROR: DataSet null in RestrictDomain operator");

    if ( ds.getNum_entries() <= 0 )
      return new ErrorString("ERROR: Empty DataSet in RestrictDomain operator");

    if ( x_min >= x_max )
      return new ErrorString("ERROR: invalid endpoints ["+x_min+","+x_max+"]"+
                             " in RestrictDomain operator");

    int n_changed = 0;

    DataSet new_ds;
    if ( make_new_ds )
      new_ds = ds.empty_clone();
    else
      new_ds = ds;
                                      
    Data   d,
           new_d;
    XScale x_scale = null,
           last_x_scale = null,
           new_x_scale  = null;
    float  start,
           end;
    int    first_i = 0,
           last_i = 0;

    Vector new_data = new Vector();
                                         // go through list backwards, since 
                                         // we may be deleting some Data blocks.
    for ( int i = ds.getNum_entries() - 1; i >= 0; i-- )
    {
      d = ds.getData_entry(i);
      x_scale = d.getX_scale();
      start = x_scale.getStart_x();
      end   = x_scale.getEnd_x();
      
      if ( x_min <= start && end <= x_max )     // add clone of data block to 
      {                                         // new_ds if making new_ds
        if ( make_new_ds )                      // else just leave in ds
          new_data.add( d.clone() );
      }
      else if ( end < x_min || start > x_max )  // discard from current ds if 
      {                                         // not making new_ds.  Don't
        if ( !make_new_ds )                     // copy into new_ds if making
        {                                       // a new_ds
          ds.removeData_entry( i ); 
          n_changed++;
        }
      }
      else                                      // make new data block
      {
        if ( x_scale != last_x_scale )          // different x_scale so we
        {                                       // need a new restricted x_scale
           first_i = x_scale.getI( x_min );
           last_i  = x_scale.getI_GLB( x_max );
           new_x_scale = x_scale.restrict( new ClosedInterval(x_min,x_max) );
           last_x_scale = x_scale;
        }
        if ( first_i >= last_i )                // degenerate interval so skip
        {
           if ( !make_new_ds ) 
           {
             ds.removeData_entry( i );
             n_changed++;
           }
        }
        else                                    // make new data block
        { 
          if ( make_new_ds )
          {
            new_d = (Data)d.clone();
            new_d.resample( new_x_scale, IData.SMOOTH_NONE );
            new_data.add( new_d );
          }
          else
            d.resample( new_x_scale, IData.SMOOTH_NONE );    

          n_changed++;
        }
      }
    }
    
    new_ds.addLog_entry( "Applied RestrictDomain ["+x_min+", "+x_max+"], " +
                         make_new_ds + ", changed "+n_changed+" Data blocks ");
                         
    if ( make_new_ds )                // add Data blocks to new_ds in reverse
    {                                 // order to preserve original order
      for ( int i = new_data.size()-1; i >= 0; i-- )
        new_ds.addData_entry( (Data)(new_data.elementAt(i)) );
    }
    else
      ds.notifyIObservers( IObserver.DATA_CHANGED ); 

    return new_ds;
  }
}
