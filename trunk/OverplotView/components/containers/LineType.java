package OverplotView.components.containers;

/**
 * $Id$
 *
 * any line-type from any graph package can implement this interface, thus
 * making the line-type acceptable for storage in class GraphableData, without
 * changing the line-type object in such a way that it can no longer be used as
 * a line-type.
 *
 * $Log$
 * Revision 1.1  2000/07/06 14:47:16  neffk
 * Initial revision
 *
 * Revision 1.1  2000/06/22 14:05:26  neffk
 * Initial revision
 *
 * Revision 1.1  2000/04/08 00:54:56  psam
 * Initial revision
 *
 */

import java.lang.*;

public interface LineType
{
  public String toString();

  public Object getLineType();
}
  
