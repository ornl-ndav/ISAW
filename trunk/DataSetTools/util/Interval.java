package DataSetTools.util;

import DataSetTools.util.*;
import DataSetTools.dataset.Attribute;

/**
 * $Id$
 *
 * container for arbitrary bounded intervals of Attributes.
 *
 * $Log$
 * Revision 1.1  2001/06/13 19:45:42  neffk
 * used for interval selection.
 *
 *
 */
public class Interval 
{
  int defaultEndpoint = 0;
  private Endpoint low, high;

  /**
   * initialize this Interval *object* with the endpoints of the interval.
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
   * copy constructor.  initializes this object with Interval 'i', carefully
   * cloning member data to ensure a "deep copy".
   */
  public Interval( Interval i )
  {
    low  = i.getLow().clone();
    high = i.getHigh().clone();
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
    low  = getLowEndpoint( e1, e2 );
    high = getHighEndpoint( e1, e2 );
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
   * constructs an interval that represents the intersection of this
   * object and interval 'i'.  if there is no intersection, an open interval
   * with equal endpoints is returned.
   */
  public Interval intersect( Interval i )
  {
    Interval ret = null;
  
    //complete internal intersection
    if(  within(i.getLow())  &&  within(i.getHigh())  )
      ret = new Interval( i );

    //complete external intersection
    if( i.getLow().before(low)  &&  i.getHigh().after(high)  )
      ret = new Interval( this );
   
    //interval 'i' overlaps the upper part of this interval
    else if(  within(i.getLow())  &&  !within(i.getHigh())  )
      ret = new Interval(  i.getLow().getAttr(), high.getAttr()  );

    //upper endpoint of interval 'i' is in this Interval
    else if(  !within(i.getLow())  &&  within(i.getHigh())  )
      ret = new Interval(  low.getAttr(),  i.getHigh().getAttr()  );

    //no intersection 
    else if(  high.after( i.getLow() )  ||  low.before( i.getHigh() )  )
      ret = new Interval(  new Endpoint( defaultEndpoint, false ),
                           new Endpoint( defaultEndpoint, false )  );

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


  /**
   * displays a summary of this Interval *object*.  the String object returned
   * by this function is primarily for debugging because it contains the 
   * endpoints of the interval.
   */
  public String toString()
  {
    String lowDelim, highDelim;
    if( low.closed() )
      lowDelim = "[";
    else
      lowDelim = "(";

    if( high.closed() )
      highDelim = "]";
    else
      highDelim = ")";

    return new String(  lowDelim + low + ":" + high + highDelim  );
  }


/*----=[ private functions ]=-------------------------------------------------*/


  private Endpoint getLowEndpoint( Endpoint e1, Endpoint e2 )
  {
    if( e1.before(e2) )
      return e1;   
    else
      return e2;
  }


  private Endpoint getHighEndpoint( Endpoint e1, Endpoint e2  )
  {
    if( e1.after(e2) )
      return e1;
    else
      return e2;
  }


/*----=[ protected functions ]=-----------------------------------------------*/


  protected boolean within( Endpoint e )
  {
    if( low.after(e)  &&  high.before(e) )
      return true;
    else 
      return false;
  }
}





