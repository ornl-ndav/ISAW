package OverplotView.components.containers;

/**
 * $Id$
 *
 * RedrawInstruction contains state information of a GraphableData object.  
 * it allows for a 1 function interface between a general, graphics package 
 * independant data format (GraphableData) and some graphics package.  this is
 * achieved by using this object to pass information about adding data,
 * redrawing the graph, and an assortment of other duties instead of a larger
 * number of functions.  this 1 function interface loosly couples the data and
 * the graph, an optimal situation in the case that the graphing package may
 * change over time.
 */

import java.lang.*;

import OverplotView.*;
import DataSetTools.util.*;

public class RedrawInstruction extends Object
{

  private boolean         bDraw;
  private boolean         bAdd;
  private GraphableData   data;
  private floatPoint2D    xRangeU;
  private floatPoint2D    yRangeU;

  /**
   * MAIN CONSTRUCTOR.  other constructors call this constructor, supplying
   * default data if necessary.
   *
   *  @param draw_ used by recipiant to decide whether or not to redraw
   *  @param add_  used by recipiant to decide whether or not to add data
   *  @param offset_ percent offset (0,infinity)
   *  @param xrange_ range of data in horizontal direction
   *  @param yrange_ range of data in vertical direction
   *  @param data_ GraphableData if add_ is set, otherwise null
   */
  public RedrawInstruction( boolean       draw_,
                            boolean       add_,
                            floatPoint2D  xrange_,
                            floatPoint2D  yrange_,
                            GraphableData data_ )
  {
    bDraw          = draw_;
    bAdd           = add_;
    data           = data_;
    xRangeU        = xrange_;
    yRangeU        = yrange_;
  }



  public boolean draw() 
  {
    return bDraw;
  }



  public boolean add() 
  {
    return bAdd;
  }



  public GraphableData getData() 
  {
    return data;
  }


  
  public floatPoint2D getXRange()
  {
    return xRangeU;
  }



  public floatPoint2D getYRange()
  {
    return yRangeU;
  }

  public String toString()
  {
    String msg = 
      "bDraw: "  + ( bDraw ? "1" : "0" ) + " " +
      "bAdd: "   + ( bAdd  ? "1" : "0" ) + " " +
      "xRange: " + ( xRangeU != null ? xRangeU.toString() : "null" )    + " " +
      "yRange: " + ( yRangeU != null ? yRangeU.toString() : "null" )    + " " +
      "data: " + ( data != null ? "set" : "null" ) + " ";
    return msg;
  }
}




