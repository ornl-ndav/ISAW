package OverplotView.components.containers;

/**
 * $Id$
 *
 * $Log$
 * Revision 1.1  2000/07/06 14:47:16  neffk
 * Initial revision
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


