package OverplotView.components.containers;

/**
 * $Id$
 *
 * Designator is a container object that shells out one object at a time.  it
 * is usefull for dishing out one color at a time from a list.
 *
 * $Log$
 * Revision 1.1  2000/07/06 14:47:16  neffk
 * Initial revision
 *
 * Revision 1.1  2000/06/22 14:05:26  neffk
 * Initial revision
 *
 * Revision 1.2  2000/04/08 00:49:44  psam
 * added Id tag
 *
 * Revision 1.1  2000/04/06 05:19:54  psam
 * Initial revision
 *
 *
 */

import java.lang.*;
import java.util.*;

public class Designator extends Object
{
  String _title;

  int _current_index;
  Vector _items;
  

  public Designator()
  {
    _current_index = -1;
    _title = "default";
    _items = new Vector();
  }


  public Designator( String title )
  {
    _current_index = -1;
    _title = title;
    _items = new Vector();
  }


  public void add( Object o )
  {
    _current_index = -1;
    _items.add( o );
  }


  public void add( int index, Object o )
  {
    _current_index = -1;
    _items.add( index, o );
  }


  public void remove( int index )
  {
    _current_index = -1;
    _items.remove( index );
  }


  public void remove( Object o )
  {
    _current_index = -1;
    _items.remove( o );
  }


  public void removeAllElements()
  {
    _current_index = -1;
    _items.removeAllElements();
  }
 

  public void reset()
  {
    _current_index = -1;
  }


  public Object getNext()
  {
    _current_index = (_current_index + 1) % _items.size();
    return _items.get( _current_index );
  }


  public Object getSame()
  {
    return _items.get( _current_index );
  } 

  public String toString() 
  {
    String tostring = _title + " ";
    for( int i=0; i<_items.size(); i++ )
      tostring += _items.get(i) + " ";
 
    return tostring;
  }


  public String getTitle()
  {
    return _title;
  }
}


