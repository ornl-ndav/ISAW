
/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2001/07/11 16:33:27  neffk
 * 0) no longer return unused part of the parameter string in the
 *    constructor
 * 1) added getType() method
 * 2) removed a few debug comments
 *
 * Revision 1.2  2001/06/19 20:34:51  neffk
 * set( String ) is partailly working.  it only works for Attribute.GROUP_ID.
 *
 * Revision 1.1  2001/06/13 19:45:42  neffk
 * used for interval selection.
 *
 */

package DataSetTools.util;

import DataSetTools.util.*;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.DetPosAttribute;
import DataSetTools.dataset.DoubleAttribute;
import DataSetTools.dataset.FloatAttribute;
import DataSetTools.dataset.IntAttribute;
import DataSetTools.dataset.StringAttribute;
import DataSetTools.dataset.IntListAttribute;
import DataSetTools.dataset.StringAttribute;
import DataSetTools.dataset.Attribute;

import java.lang.Integer;

/**
 * container for arbitrary bounded intervals of Attributes.
 */

public class Interval 
{
  final private String SEPERATOR = ":";

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
   */
  public Interval( String str )
  {
    System.out.println( "I" );

    String start_value = null, 
           end_value   = null;
    int i,j,k,index = 0;

    str = str.trim();  //remove whitespace on ends

    //find the beginning of the endpoint
    i = str.indexOf( '[' );
    j = str.indexOf( '(' );
    k = str.indexOf( SEPERATOR );
    if(  i < 0  &&  j < 0  ||  k < 0  )
    {
      System.out.println( "marker not found" );
//      return new String();
    }
    if(  i > 0  &&  j > 0  )
    {
      if( i > j )
        index = j;
      else
        index = i;
    }
    else
    {
      if( i > j )
        index = i;
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


//    System.out.println( "start value: " + start_value );


   //find the end of the endpoint and make a copy of it, leaving
   //the rest of the string alone
//    System.out.println( "str: " + str );
    k = str.indexOf( SEPERATOR );
    i = str.indexOf( ']' );
    j = str.indexOf( ')' );
    if(  i < 0  &&  j < 0  ||  k < 0  )
    {
      System.out.println( "marker not found" );
//      return new String();
    }
    if(  i > 0  &&  j > 0  )
    {
      if( i > j )
        index = j;
      else
        index = i;
    }
    else
    {
      if( i > j )
        index = i;
      else
        index = j;
    }

    if(  index > k  )
    {
      end_value = str.substring(k+1,i);
      str = str.substring(i+1,str.length());
    }
    else
    {
      System.out.println( "error: problem found in Interval(String)" );
    }   
    

//    System.out.println( "str: " + str );


//    System.out.println( "type:  [" + attr_type   + "]" );
//    System.out.println( "start: [" + start_value + "]" );   
//    System.out.println( "end:   [" + end_value   + "]" );   

    create_endpoints( start_value, end_value );
//    return str;
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
   * change the endpoints of this Interval object.  this method is
   * provided as a convenience so that one need not be constantly newing up
   * Intervals in iterative methods.
   *
   *   @param e1 an Attribute whos value represents one of the bounds of
   *             of the interval
   *   @param b2 an Attribute whos value represents the other bound of
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
    else
    {
      low =  e2;
      high = e1;
    }
    
    //are the names the same?  assign lexigraphically
    if(  e1.getAttr().getName().equals( e2.getAttr().getName() )  )
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

  
  private void create_endpoints( String v1, String v2 )
  {
    Endpoint e1 = null,
             e2 = null;

    if(  attr_type.equals( Attribute.DELTA_2THETA )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.DETECTOR_IDS )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.DETECTOR_POS )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.DS_TAG )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.EFFICIENCY_FACTOR )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.END_DATE )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.END_TIME )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.ENERGY_IN )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.ENERGY_OUT )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.FILE_NAME )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.GROUP_ID )  )
    {
      Integer i1 = new Integer( v1 );
      Integer i2 = new Integer( v2 );
      e1 = new Endpoint(  new IntAttribute( attr_type, i1.intValue() ),
                          true  );
      e2 = new Endpoint(  new IntAttribute( attr_type, i2.intValue() ),
                          true  );
      set( e1, e2 );
    }
    else if(  attr_type.equals( Attribute.INITIAL_PATH )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.INST_NAME )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.INST_TYPE )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.MAGNETIC_FIELD )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.NOMINAL_ENERGY_IN )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.NUMBER_OF_PULSES )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.PRESSURE )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.Q_VALUE )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.RAW_ANGLE )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.RUN_NUM )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.RUN_TITLE )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.SAMPLE_NAME )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.SOLID_ANGLE )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.TEMPERATURE )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.TIME_FIELD_TYPE )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.TITLE )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.TOTAL_COUNT )  )
    {
      System.out.println( "not implemented" );
    }
    else if(  attr_type.equals( Attribute.UPDATE_TIME )  )
    {
      System.out.println( "not implemented" );
    }
    else
    {
      System.out.println( "bad attribute type" );
    }
  }

}





