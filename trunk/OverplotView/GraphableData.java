package OverplotView;

/**
 * $Id$
 *
 * container for data and state information associated with graphical
 * objects, including color, linetype, and offset.  offset information is
 * stored directly, but all other attributes are stored as 
 * DataSetTools.dataset.Attributes.  access these attributes with a call to
 * getAttributeList().
 *
 * $Log$
 * Revision 1.2  2001/06/27 16:45:23  neffk
 * added toString() method
 *
 * Revision 1.1  2001/06/21 15:44:42  neffk
 * redesign of OverplotView
 *
 */

import DataSetTools.dataset.*;
import java.awt.Color;
import java.lang.String;
import java.util.*;

public class GraphableData
{

  public static final String COLOR    = "Color";
  public static final String MARKER   = "Marker Type";
  public static final String LINETYPE = "Line Type";

  private Data          hidden_data;
  private Data          visable_data;
  private AttributeList attrs;
  private float         offset;
 

  /**
   * default constructor
   */
  public GraphableData( Data d )
  {
    hidden_data  = d;
    visable_data = d;
    offset = 0.0f;
  }


  /**
   * allows the user to set the offset for this data.  'offset_' is an
   * absolute value that's added directly to the y-values of this data
   * block.  since this is absolute, it would be a good idea to make 
   * scale selection UI objects select on [0:1] and calculate the offset 
   * as a percent of the tallest data on the graph.
   */
  public void setOffset( float offset_ )
  {
    offset = offset_;
    calculateOffset();
  }


  /**
   * allows the user access to the data that this object contains.  to ensure
   * data is not offset, set offset to zero (0).
   */
  public Data getData()
  {
    return (Data)visable_data.clone();
  }


  /**
   * get the (absolute) offset for this data.
   */
  public float getOffset()
  {
    return offset;
  }


  /**
   * returns a clone of the attributes for this data
   */
  public AttributeList getAttributeList()
  {
    return (AttributeList)attrs.clone();
  }


  /**
   * set the attributes for this data.  use this mechanism to set colors,
   * marker types, line types, etc.
   */
  public void setAttributeList( AttributeList l )
  {
    attrs = l;
  }
 

  /**
   *
   */
  public String toString()
  {
    return hidden_data.toString();
  }
 

/*----------------------------=[ private methods ]=---------------------------*/


  /**
   * calculates the adjusted_data.  offset is a quantity that is added
   * directly to the dependant variable.     
   */
  private void calculateOffset()
  {
    float[] values = hidden_data.getY_values();
    float[] od = new float[values.length];
    for( int i=0; i<values.length; i++ )
      od[i] = values[i] + offset;

    visable_data = new Data(  hidden_data.getX_scale(), 
                              od, 
                              hidden_data.getGroup_ID()  );
  }
}



