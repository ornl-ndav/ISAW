package OverplotView;

/*
 * $Id$
 * ----------
 *
 * $Log$
 * Revision 1.4  2001/06/29 16:29:24  neffk
 * integrated ColorAttribute class for storing color information in
 * GraphableData objects
 *
 * Revision 1.3  2001/06/28 22:02:45  neffk
 * all data that is to be associated with the data is now stored as various
 * types of Attributes.
 *
 * Revision 1.2  2001/06/27 16:45:23  neffk
 * added toString() method
 *
 * Revision 1.1  2001/06/21 15:44:42  neffk
 * redesign of OverplotView
 * ----------
 */

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Data;
import java.awt.Color;
import java.lang.String;

/**
 * container for data and state information associated with data that
 * is intended for visualization.
 */
public class GraphableData
{

  public static final String COLOR    = "Color";
  public static final String MARKER   = "Marker Type";
  public static final String NAME     = "Name of this data";
  public static final String LINETYPE = "Line Type";
  public static final String OFFSET   = "Absolute (addivite) Offset";

  private Data          data;
  private AttributeList attrs;
  private float         offset;
 

  /**
   * default constructor
   */
  public GraphableData( Data d )
  {
    data = d;
    offset = 0.0f;
    attrs = new AttributeList();
  }


  /**
   * associate information with this data.  use the fields provided
   * by GraphableData as the 'name' parameter of each (sub class of) Attribute
   * added to this data to provide a uniform way for recipients to find
   * the information that they need.
   */
  public void addAttribute( Attribute attr )
  {
    attrs.addAttribute( attr );
  }


  /**
   * returns a clone of the data.
   */
  public Data getData()
  {
    return (Data)data.clone();
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
   * provides a string representation of this GraphableData object.
   */
  public String toString()
  {
    return data.toString();
  }
}



