package OverplotView.components.containers;

/**
 * $Id$
 *
 *
 * $Log$
 * Revision 1.1  2000/07/06 14:47:16  neffk
 * Initial revision
 *
 * Revision 1.1  2000/06/22 14:05:26  neffk
 * Initial revision
 *
 * Revision 1.1  2000/04/20 12:17:42  psam
 * Initial revision
 *
 *
 */

public class MarkerDesignator
{

  private Designator d; 


  public MarkerDesignator()
  {
    d = new Designator();
  }


  public MarkerDesignator( String _title )
  {
    d = new Designator( _title );
  }


  public void add( Object o )
  {
    d.add( o );
  }


  public void add( int index, Object o )
  {
    d.add( index, o );
  }


  public void remove( int i )
  {
    d.remove( i );
  }


  public void remove( Object o )
  {
    d.remove( o );
  }


  public void removeAllElements()
  {
    d.removeAllElements();
  }


  public void reset()
  {
    d.reset();
  }


  public Marker getNext()
  {
    return (Marker)d.getNext();
  }


  public Marker getSame()
  {
    return (Marker)d.getSame();
  }

  public String getTitle()
  {
    return d.getTitle();
  }
}

  
