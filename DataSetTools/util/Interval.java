/*
 * File: Interval.java
 *
 * Copyright (C) 2001, Kevin Neff
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>

 *
 * $Log$
 * Revision 1.6  2003/10/16 00:34:42  dennis
 * Fixed javadocs to build cleanly with jdk 1.4.2
 *
 * Revision 1.5  2002/11/27 23:23:49  pfpeterson
 * standardized header
 *
 */

package DataSetTools.util;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.DetPosAttribute;
import DataSetTools.dataset.DoubleAttribute;
import DataSetTools.dataset.FloatAttribute;
import DataSetTools.dataset.IntAttribute;
import DataSetTools.dataset.IntListAttribute;
import DataSetTools.dataset.StringAttribute;
import DataSetTools.dataset.StringAttribute;
import DataSetTools.util.*;
import java.lang.IllegalArgumentException;

import java.lang.Integer;

/**
 * container for arbitrary bounded intervals of Attributes.
 */

public class Interval 
{
  public static final String SEPARATOR = ":";

  private int      defaultEndpoint = 0;
  private String   attr_type = null;
  private Endpoint low, high;


  /**
   * initialize this object with the endpoints of the interval.
   * the order which the parameters are specified is not important because
   * they are arranged as high and low internally.  there is no default
   * constructor because i don't want people trying to use uninitialized
   * intervals and i'm not interested in reading more about exceptions...
   * 
   *   @param e1 an Attribute whos value represents one of the bounds of
   *             of the interval
   *   @param e2 an Attribute whos value represents the other bound of
   *             of the interval
   *
   */ 
  public Interval( Endpoint e1, Endpoint e2 )
  {
    set( e1, e2 );
  }


  /**
   * parses an interval from it's string representation.  the string
   * representation of an interval is the name of the attribute, followed
   * by two values seperated by a colon, enclosed in () or [], depending
   * on if the interval is open or closed.  for example, to specify an
   * interval of GROUP_ID's from 10 to 100, use 'GROUP_ID[10:100]'.
   *
   * essentially, this method parses two (2) double values, which are 
   * then used in conjunction w/ each Attribute object's getNumericValue()
   * method.
   */
  public Interval( String str )
  {

              //keep track of whether each endpoint is
              //'closed' (e.g. closed = [ or ])
    boolean cl = false,
            cr = false;

    int i,j,k,index = 0;
    String start_value = null, 
           end_value   = null;


    str = str.trim();  //remove whitespace on ends

    //find the beginning of the endpoint
    i = str.indexOf( '[' );
    j = str.indexOf( '(' );
    k = str.indexOf( SEPARATOR );
    if(  i < 0  &&  j < 0  ||  k < 0  )
      throw new IllegalArgumentException( "marker not found" );

    if(  i > 0  &&  j > 0  )
    {
      if( i > j )
        index = j;
      else
      {
        index = i;
        cl = true;
      }
    }
    else
    {
      if( i > j )
      {
        index = i;
        cl = true;
      }
      else
        index = j;
    }

    if(  index < k  )
    {
      attr_type = str.substring(0,index);
      start_value = str.substring(index+1,k);
      str = str.substring(k,str.length());
    }
    else
    {
      System.out.println( "error: problem found in Interval(String)" );
    }


                       //find the end of the endpoint 
                       //and make a copy of it, leaving
                       //the rest of the string alone
    k = str.indexOf( SEPARATOR );
    i = str.indexOf( ']' );
    j = str.indexOf( ')' );
    if(  i < 0  &&  j < 0  ||  k < 0  )
      throw new IllegalArgumentException( 
        "unmatched marker.  use [3:4] or (3:4)" );

    if(  i > 0  &&  j > 0  )
    {
      if( i > j )
        index = j;
      else
      {
        index = i;
        cr = true;
      }
    }
    else
    {
      if( i > j )
      {
        index = i;
        cr = true;
      }
      else
        index = j;
    }

    if(  index > k  )
    {
      end_value = str.substring(k+1,index);
      str = str.substring(index+1,str.length());
    }
    else
    {
      System.out.println( "error: problem found in Interval(String)" );
    }   
    

//    System.out.println( "str: " + str );


//    System.out.println( "type:  [" + attr_type   + "]" );
//    System.out.println( "start: [" + start_value + "]" );   
//    System.out.println( "end:   [" + end_value   + "]" );   

    create_endpoints( start_value, end_value,
                      cl,          cr );
  }


  /**
   * copy constructor.  initializes this object with Interval 'i', carefully
   * cloning member data to ensure a "deep copy".
   */
  public Interval( Interval i )
  {
    low  = (Endpoint)i.getLow().clone();
    high = (Endpoint)i.getHigh().clone();
  }




  /**
   * Change the endpoints of this Interval object.  This method is
   * provided as a convenience so that one need not be constantly moving up
   * Intervals in iterative methods.
   *
   *   @param e1 an Attribute whose value represents one of the bounds of
   *             of the interval
   *   @param e2 an Attribute whose value represents the other bound of
   *             of the interval
   */
  public void set( Endpoint e1, Endpoint e2 )
  {
                      //are values are the same?
    if( e1.isBefore(e2) )
    {
      low =  e1;
      high = e2;
    }
    else if(  e2.isBefore(e1)  )
    {
      low =  e2;
      high = e1;
    }
                      //are the names the same?  assign lexigraphically
    else if(  e1.getAttr().getName().equals( e2.getAttr().getName() )  )
    {
      //e1 comes first
      if( e1.getAttr().getName().compareTo(e2.getAttr().getName()) < 0 )
      {
        low  = e1;  
        high = e2;
      }

      else if( e1.getAttr().getName().compareTo(e2.getAttr().getName()) == 0 )
      {
        low  = e1;
        high = e2;
      }

      //e1 comes last
      else if( e1.getAttr().getName().compareTo(e2.getAttr().getName()) < 0 )
      {
        low  = e2;
        high = e1;
      }
    }
  }


  /**
   * sets the low endpoint of the interval.  if the value of e is lower than
   * the current value, e replaces the endpoint.  if the values are equal 
   * but the names are not, the assignment is made.  if the values and 
   * names are equal, but one interval is closed and one is open, the closed 
   * interval is used.  returns true on assignment.
   *
   * to replace the endpoint objects contained in this Interval, 
   * use set( Endpoint, Endpoint).  this method can be used to find the 
   * minimum value of a group Attributes in an iterative fashion.
   */
  public boolean setLow( Endpoint e )
  {
    return false;
  }


  /**
   * sets the high endpoint of the interval.  if the value of e is higher than
   * the current value, e replaces the endpoint.  if the values are equal 
   * but the names are not, the assignment is made.  if the values and 
   * names are equal but one interval is closed and one is open, then the
   * closed interval is used.  returns true on assignment.
   *
   * to replace the endpoint objects contained in this Interval, 
   * use set( Endpoint, Endpoint).  this method can be used to find the 
   * maximum value of a group Attributes in an iterative fashion.
   */
  public boolean setHigh( Endpoint e )
  {
    return false;
  }


  /**
   * get the endpoint who is considered 'low' by comparison to the other
   * bound.  the 'low' bound is determined by the Attribute's .compare()
   * mothod.
   */
  public Endpoint getLow()
  {
    return low;
  }


  /**
   * get the endpoint who is considered 'high' by comparison to the other
   * bound.  the 'high' bound is determined by the Attribute's .compare()
   * mothod.
   */
  public Endpoint getHigh()
  {
    return high;
  }


  /**
   * get the name, or effective type, of this Inverval object.
   */
  public String getType()
  {
    return high.getAttr().getName();
  }


  /**
   * test if an Endpoint falls within this interval.  this method 
   * uses Endpoint.isAfter() and Endpoint.isBefore() to consider whether 
   * the interval is open or closed.
   */
  public boolean within( Endpoint e )
  {
    if( low.isBefore(e)  &&  high.isAfter(e) )
      return true;
    else 
      return false;
  }


  /**
   * test if an Attribute falls within this interval.  this method 
   * uses Endpoint.isAfter() and Endpoint.isBefore() to consider whether 
   * the interval is open or closed.
   */
  public boolean within( Attribute a )
  {
    Endpoint e = new Endpoint( a, true );
    if( low.isBefore(e)  &&  high.isAfter(e) )
      return true;
    else 
      return false;
  }


  /**
   * constructs an interval that represents the intersection of this
   * object and interval 'i'.  if there is no intersection, an open interval
   * with equal endpoints is returned.
   */
  public Interval intersect( Interval i )
  {
    Interval ret = null;
  
    //complete internal intersection (i within this)
    if(  within(i.getLow())  &&  within(i.getHigh())  )
    {
      //System.out.println( "complete internal intersection" );
      ret = new Interval( i );
    }

    //complete external intersection (this within i)
    else if( i.getLow().isBefore(low)  &&  i.getHigh().isAfter(high)  )
    {
      System.out.println( "complete external intersection" );
      ret = new Interval( this );
    }
   
    //interval 'i' overlaps the upper part of this interval
    else if(  within(i.getLow())  &&  !within(i.getHigh())  )
//      ret = new Interval(  i.getLow().getAttr(), high.getAttr()  );
      ret = new Interval(  i.getLow(), high  );

    //upper endpoint of interval 'i' is in this Interval
    else if(  !within(i.getLow())  &&  within(i.getHigh())  )
      ret = new Interval(  low,  i.getHigh()  );

    //no intersection 
    else if(  high.isAfter( i.getLow() )  ||  low.isBefore( i.getHigh() )  )
      ret = new Interval(  new Endpoint(  low.getAttr(), false ),
                           new Endpoint(  low.getAttr(), false )  );

    return ret;
  }


  /**
   * constructs an interval that represents the union of this object 
   * and 'i' if they intersect.  since intervals only contain two (2) 
   * endpoints, it's not possible and it doesn't make sense to union 
   * non-intersecting intervals.
   */
  public Interval quasiUnion( Interval i )
  {
    return new Interval( this );
  }


  public String getAttrType()
  {
    return attr_type;
  }


  /**
   * displays a summary of this Interval *object*.  the String object returned
   * by this function is primarily for debugging because it contains the 
   * endpoints of the interval.
   */
  public String toString()
  {
    String lowDelim, highDelim;
    if( low.isClosed() )
      lowDelim = "[";
    else
      lowDelim = "(";

    if( high.isClosed() )
      highDelim = "]";
    else
      highDelim = ")";

    return new String(  lowDelim + low + ":" + high + highDelim  );
  }

  
  private void create_endpoints( String v1,  String v2, 
                                 boolean cl, boolean cr )
  {
    Endpoint e1 = null,
             e2 = null;

    Double value = new Double( v1 );
    if(  value.isNaN()  )
      System.out.println( "number not found" );
    else
    {
      DoubleAttribute attr = new DoubleAttribute(  attr_type, 
                                                   value.doubleValue()  );
      e1 = new Endpoint( attr, cl );
    }

    value = new Double( v2 );
    if(  value.isNaN()  )
      System.out.println( "number not found" );
    else
    {
      DoubleAttribute attr = new DoubleAttribute(  attr_type, 
                                                   value.doubleValue()  );
      e2 = new Endpoint( attr, cr );
    }   
    
    set( e1, e2 );
  }

}





