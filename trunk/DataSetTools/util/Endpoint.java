package DataSetTools.util;

import DataSetTools.dataset.Attribute;

public class Endpoint
{
  final int SEPERATOR = ':';
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
  public boolean isBefore( Endpoint e )
  {
    if( closed )
      if( attr.compare(e.getAttr()) <= 0 )
        return true;
      else if( attr.compare(e.getAttr()) > 0 )
        return false;
    
    if( !closed )
      if( attr.compare(e.getAttr()) < 0 )
        return true;
      else if( attr.compare(e.getAttr()) >= 0 )
        return false;

    return false;  //javac is retarded
  }

  /**
   * test if this object comes after 'e' the given ednpoint.  this method
   * takes into account whether the interval is open or closed.  
   * if e comes after this...
   */
  public boolean isAfter( Endpoint e )
  {
    if( closed )
      if( attr.compare(e.getAttr()) >= 0 )
        return true;
      else if( attr.compare(e.getAttr()) < 0 )
        return false;
  
    if( !closed )
      if( attr.compare(e.getAttr()) > 0 )
        return true;
      else if( attr.compare(e.getAttr()) <= 0 )
        return false;

    return false;  //javac is retarded
  }

  
  public boolean isClosed()
  {
    if( closed )
     return true;
    else
      return false;
  }


  public void setClosed( boolean c )
  {
    closed = c;
  }


  public String toString()
  {
    return new String( attr.getName() + "=" + attr.getValue() );
  }

 
  public Object clone()
  {
    return new Endpoint( attr, closed );
  }

}
