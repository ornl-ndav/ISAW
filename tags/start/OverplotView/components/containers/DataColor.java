package OverplotView.components.containers;

/**
 * $Id$
 *
 * concrete implementation of interface EntityColor
 *
 * $Log$
 * Revision 1.1  2000/07/06 14:47:16  neffk
 * Initial revision
 *
 */

import java.lang.*;
import java.awt.*;

public class DataColor 
  implements EntityColor
{

  Color color;

  public DataColor( Color c )
  {
    color = c;
  }



  public Object getColor() 
  {
    return color;
  }



  public void setColor( Color c ) 
  {
    color = c;
  }



  public String toString()
  {
    return color.toString();
  }
}
