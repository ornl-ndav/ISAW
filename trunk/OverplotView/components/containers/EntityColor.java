package OverplotView.components.containers;

/**
 * $Id$
 *
 * $Log$
 * Revision 1.3  2000/07/06 20:11:19  neffk
 * added these files, just to make sure, since they had a ? in front of them
 * when updating.
 *
 * Revision 1.1  2000/06/15 18:56:40  neffk
 * Initial revision
 *
 */

import java.lang.*;

public interface EntityColor
{

  /**
   * returns a string representation of this object in the form
   *  
   *   "<class>::toString() [ <properties > ]"
   */
  public String toString();



  /** 
   * returns the color (without cast)
   */
  public Object getColor();
  
}


