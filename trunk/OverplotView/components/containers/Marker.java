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
 * Revision 1.1  2000/07/06 14:47:16  neffk
 * Initial revision
 *
 * Revision 1.1  2000/06/22 14:05:26  neffk
 * Initial revision
 *
 * Revision 1.1  2000/04/08 00:55:32  psam
 * Initial revision
 *
 *
 */


public interface Marker
{
  public String toString();


  public Object getMarker();
}
  
