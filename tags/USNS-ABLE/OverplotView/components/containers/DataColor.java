package OverplotView.components.containers;

/**
 * $Id$
 *
 * concrete implementation of interface EntityColor
 *
 * $Log$
 * Revision 1.3  2000/07/06 20:11:19  neffk
 * added these files, just to make sure, since they had a ? in front of them
 * when updating.
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
