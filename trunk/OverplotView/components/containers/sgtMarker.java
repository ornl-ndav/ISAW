package OverplotView.components.containers;

/**
 * $Id$
 *
 * any marker from any graph package can implement this interface, thus
 * making the marker acceptable for storage in class GraphableData, without
 * changing the marker object in such a way that it can no longer be used as
 * a marker.
 *
 * $Log$
 * Revision 1.3  2000/07/06 20:11:19  neffk
 * added these files, just to make sure, since they had a ? in front of them
 * when updating.
 *
 * Revision 1.1  2000/06/22 14:05:26  neffk
 * Initial revision
 *
 * Revision 1.1  2000/04/08 00:55:32  psam
 * Initial revision
 */


public class sgtMarker
  implements Marker
{

  int marker;


  public sgtMarker( int m )
  {
    marker = m;
  }



  public String toString()
  {
    return new String();
  }



  public Object getMarker()
  {
    return new Object();
  }



  public int getSGTMarker()
  {
    return marker;
  }
}
  
