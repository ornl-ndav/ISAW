package DataSetTools.util;

import DataSetTools.dataset.Attribute;

public class Endpoint
{
  private boolean closed;
  private Attribute attr;

  public Endpoint( Attribute a, boolean closed_ )
  {
    set( a, closed_ );
  }


  public void set( Attribute a, boolean closed_ )
  {
    attr = a;
    closed = closed_;
  }


  public Attribute getAttr()
  {
    return attr;
  }


  /**
   * test if this object comes before the given Endpoint.  this method
   * considers whether the interval is open or closed.
   */
  public boolean before( Attribute a )
  {
    if( closed )
      if( attr.compare(a) >= 0 )
        return true;
      else if( attr.compare(a) < 0 )
        return false;
    
    if( !closed )
      if( attr.compare(a) > 0 )
        return true;
      else if( attr.compare(a) <= 0 )
        return false;
  }


   /**
    * test if this object comes after the given ednpoint.  this method
    * considers whether the interval is open or closed.
    */
  public boolean after( Attribute a )
  {
    if( closed )
      if( attr.compare(a) <= 0 )
        return true;
      else if( attr.compare(a) > 0 )
        return false;
  
    if( !closed )
      if( attr.compare(a) < 0 )
        return true;
      else if( attr.compare(a) >= 0 )
        return false;
  }

  
  public boolean closed()
  {
    if( closed )
     return true;
    else
      return false;
  }


  public String toString()
  {
    if( closed )
      return new String(  "[" + attr.getName() + "]"  );
    else
      return new String(  "(" + attr.getName() + ")"  );
  }
}
