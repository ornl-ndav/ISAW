package OverplotView.components.containers;

/**
 * $Id$
 *
 * $Log$
 * Revision 1.1  2000/07/06 14:47:16  neffk
 * Initial revision
 *
 */

import java.awt.*;
import java.lang.*;

public class sgtEntityColor
  implements EntityColor
{

  private Color color;


  /**
   * sets color
   *
   */
  public sgtEntityColor( Color c )
  {
    color = c;
  }



  /**
   * returns a string representation of this object in the form
   *  
   *   "<class>::toString() [ <properties > ]"
   */
  public String toString()
  {
    return color.toString();
  }



  /** 
   * returns the color (without cast)
   *
   */
  public Object getColor()
  {
    return color;
  }



  /** 
   * returns the color
   *
   */
  public Color getSGTColor()
  {
    return color;
  }
}


