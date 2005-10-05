/** 
 * File: Data.java
 *
 * Copyright (C) 2002, Dennis Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.45  2005/05/27 03:44:07  dennis
 *  Removed unused imports (no longer needed since the convenience
 *  methods to get specific attributes were removed).
 *
 *  Revision 1.44  2005/05/27 03:21:19  dennis
 *  Removed convenience methods for getting particular attributes for
 *  TOF neutron scattering from these general purpose classes.  The
 *  convenience methods were moved to the new AttrUtil class.
 *
 *  Revision 1.43  2005/03/28 22:47:39  dennis
 *  Removed TITLE attribute, since the DataSet already has a
 *  field for the title.
 *
 *  Revision 1.42  2005/02/07 22:30:55  dennis
 *  Added protected method copyFields( Data d ) that copies data
 *  members from Data object d to the current Data object.  This
 *  is a convenience method for derived classes that makes the
 *  clone method more reliable.
 *  Added convenience method getLabelName() to get the name of
 *  the attribute to be used as a label, IF the label is specified
 *  by an attribute name.
 *  Fixed bug in add() method.  Adding a Data object that was not
 *  an instance of TabluatedData would have failed.
 *
 *  Revision 1.41  2004/06/22 15:34:18  rmikk
 *  Added documentation for setGridId method and associated variable
 *
 *  Revision 1.40  2004/05/25 20:01:11  kramer
 *
 *  Implemented inherited methods from the interface IAttributeList.
 *
 *  Revision 1.39  2004/04/26 13:06:48  rmikk
 *  Passes on the Hashtable of already created Grids to subclasses that
 *    need it
 *
 *  Revision 1.38  2004/03/15 06:10:35  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.37  2004/03/15 03:28:05  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.36  2003/07/21 22:11:19  rmikk
 *  Fixed bug in stitch. Error array not allocated if
 *    square root errors were calculated( Dennis)
 *
 *  Revision 1.35  2003/02/10 13:28:37  dennis
 *  getAttributeList() now returns a reference to the attribute list,
 *  not a clone.
 *
 *  Revision 1.34  2002/11/27 23:14:06  pfpeterson
 *  standardized header
 *
 *  Revision 1.33  2002/10/03 15:42:45  dennis
 *  Changed setSqrtErrors() to setSqrtErrors(boolean) in Data classes.
 *  Added use_sqrt_errors flag to Data base class and changed derived
 *  classes to use this.  Added isSqrtErrors() method to check state
 *  of flag.  Derived classes now check this flag and calculate rather
 *  than store the errors if the use_sqrt_errors flag is set.
 *
 *  Revision 1.32  2002/09/11 23:26:06  dennis
 *     The toString() method now returns the String obtained from the
 *  getLabel() method if it is a non-degenerate String.  It returns
 *  "Group ID" + group_id, if getLabel() otherwise.
 *     The "Label" attribute is no longer set.  The getLabel() method
 *  should be used instead of the Label attribute.
 *
 *  Revision 1.31  2002/09/11 22:51:40  dennis
 *     The group_id field is now set from the Attribute, if either form
 *  of setAttribute() is called with the attribute name "Group ID" or if
 *  setAttributeList() is called with an attribute list containing
 *  a "Group ID" attribute.
 *
 *  Revision 1.30  2002/08/01 22:33:34  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.29  2002/07/11 18:18:23  dennis
 *  Added getLabel() and setLabel() methods.
 *  Added  serialVersionUID = 1L;
 *
 *  Revision 1.28  2002/07/10 16:02:23  pfpeterson
 *  Added removeAttribute() methods.
 *
 *  Revision 1.27  2002/07/08 15:38:56  pfpeterson
 *  Added SUM option to stich() which adds the counts together.
 *
 *  Revision 1.26  2002/06/14 20:48:59  rmikk
 *  Implements the IXmlIO interface
 *
 *  Revision 1.25  2002/06/10 22:32:01  dennis
 *  Fixed some problems with error propagation that were introduced when
 *  the Data object hierarchy was made.  Also made error propagation more
 *  efficient for scalar operations when the error in the scalar is 0.
 *
 *  Revision 1.24  2002/04/19 15:42:26  dennis
 *  Revised Documentation
 *
 *  Revision 1.23  2002/04/04 18:10:08  dennis
 *  Moved implementation of stitch() and of scalar and DataSet
 *  add(), subtract(), multiply(), divide() to this level.
 *  A TabulatedData object will be created and returned
 *  by these methods, for ANY type of Data object.
 *
 *  Revision 1.22  2002/03/18 21:37:17  dennis
 *  Now adds Label attribute, initialized as the Group ID.
 *  getCopyOfY_values() and getCopyOfErrors() now check whether
 *  or not the array is null before trying to copy it.
 *
 *  Revision 1.21  2002/03/14 23:41:51  dennis
 *  Now implements Serializable.
 *
 *  Revision 1.20  2002/03/13 16:08:31  dennis
 *  Data class is now an abstract base class that implements IData
 *  interface. FunctionTable and HistogramTable are concrete derived
 *  classes for storing tabulated functions and frequency histograms
 *  respectively.
 *
 */
package DataSetTools.dataset;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.File.*;
import gov.anl.ipns.Util.Numeric.*;
import java.util.*;
import java.io.*;


public abstract class Data implements IData,
                                      Serializable ,
                                      IXmlIO
{
  // NOTE: any field that is static or transient is NOT serialized.
  //
  // CHANGE THE "serialVersionUID" IF THE SERIALIZATION IS INCOMPATIBLE WITH
  // PREVIOUS VERSIONS, IN WAYS THAT CAN NOT BE FIXED BY THE readObject()
  // METHOD.  SEE "IsawSerialVersion" COMMENTS BELOW.  CHANGING THIS CAUSES
  // JAVA TO REFUSE TO READ DIFFERENT VERSIONS.
  //
  public  static final long serialVersionUID = 1L;

  public  static final String FUNCTION  = "Function";
  public  static final String HISTOGRAM = "Histogram";
  private static long  select_count     = 1;
                                           // each Data block is tagged as
  transient protected long selected = 0;   // selected by giving it a non-zero
                                           // selected value.  The value is
                                           // value of select_count after
                                           // incrementing select_count.
                                           // selected == select_count
                                           // for the most recently selected
                                           // Data block.
  transient protected boolean  hide = false;
                                           // if label_is_attribute == true,
                                           // use the named attribute for the
                                           // label value, otherwise, use the
                                           // label_string directly as the  
                                           // label
  transient private   String   label_string = Attribute.GROUP_ID;
  transient private   boolean  label_is_attribute = true;

  // NOTE: The following fields are serialized.  If new fields are added that
  //       are not static, reasonable default values should be assigned in the
  //       readObject() method for compatibility with old servers, until the
  //       servers can be updated.

  private int IsawSerialVersion = 1;         // CHANGE THIS WHEN ADDING OR
                                             // REMOVING FIELDS, IF
                                             // readObject() CAN FIX ANY
                                             // COMPATIBILITY PROBLEMS

  protected int          group_id;  //NOTE: we force a Data object to have an
                                    // id and also keep the same id as an
                                    // attribute.  The id should only be
                                    // changed through the setID() method.
  protected XScale        x_scale;
  private   boolean       use_sqrt_errors = false;
  protected AttributeList attr_list;
  
  /**
   *  A Hashtable of the gridID's and their grid that have already been set 
   * up while reading through the Datablocks in an XML file
   */
  transient protected Hashtable gridIDs = null;

  /**
   *  Create an instance of a Data object representing a function or histogram.
   *  Since the x & y values are specified, this form of getInstance() returns
   *  a TabulatedData object.  If there is one more x value than there are 
   *  y values a HistogramTable is returned.  Otherwise a FunctionTable is
   *  returned.  When constructing the FunctionTable, extra values will be 
   *  ignored and missing values will be taken to be 0.
   *
   *  @param  x_scale    The collection of x values for which the y values are
   *                     tabulated.
   *  @param  y_values   If there are one more x values than y_values, these
   *                     y_values are assumed to represent the values at the
   *                     centers of histogram bins given by the x values.
   *                     Otherwise, the y_values are assumed to represent the
   *                     values of a function at the specified x values.
   *  @param  errors     Array of error estimates for the y_values.
   *  @param  group_id   The group_id to use for this Data object.      
   *
   *  @return A HistogramTable or a FunctionTable object corresponding to the 
   *          specified x, y and error values.  
   */ 
  public static Data getInstance( XScale x_scale, 
                                  float  y_values[], 
                                  float  errors[], 
                                  int    group_id  )
  {
    if ( y_values.length == x_scale.getNum_x() - 1 )
      return new HistogramTable( x_scale, y_values, errors, group_id );
    else
      return new FunctionTable( x_scale, y_values, errors, group_id );
  }

  /**
   *  Create an instance of a Data object representing a function or histogram.
   *  Since the x & y values are specified, this form of getInstance() returns
   *  a TabulatedData object.  If there is one more x value than there are 
   *  y values a HistogramTable is returned.  Otherwise a FunctionTable is
   *  returned.  When constructing the FunctionTable, extra values will be 
   *  ignored and missing values will be taken to be 0.
   *
   *  @param  x_scale    The collection of x values for which the y values are
   *                     tabulated.
   *  @param  y_values   If there are one more x values than y_values, these
   *                     y_values are assumed to represent the values at the
   *                     centers of histogram bins given by the x values.
   *                     Otherwise, the y_values are assumed to represent the
   *                     values of a function at the specified x values.
   *  @param  group_id   The group_id to use for this Data object.
   *
   *  @return A HistogramTable or a FunctionTable object corresponding to the
   *          specified x, y without error estimates.
   */
  public static Data getInstance( XScale x_scale,
                                  float  y_values[],
                                  int    group_id  )
  {
    if ( y_values.length == x_scale.getNum_x() - 1 )
      return new HistogramTable( x_scale, y_values, group_id );
    else
      return new FunctionTable( x_scale, y_values, group_id );
  }


  /**
   * Constructs a Data object with "X" scale and a group id for that Data 
   * object.  This will be called from the constructors of subclasses.  Any
   * concrete derived class must manage the y-value information associated 
   * with that subclass.
   *
   * @param   x_scale   the list of x values for this data object
   * @param   group_id  an integer id for this data object
   */
  public Data( XScale  x_scale, int group_id )
  {
    this.x_scale   = x_scale;
    this.attr_list = new AttributeList();
    setGroup_ID( group_id );
    selected = 0;
    hide     = false;
  }

  /**
   * Sets the ID of the Data object both as an instance variable and as
   * an attribute of the Data object.
   *
   * @param  group_id   The ID to use for the new group ID of the Data object
   */
  public void setGroup_ID( int group_id )
  {
    this.group_id = group_id;

    IntAttribute id_attr = new IntAttribute( Attribute.GROUP_ID, group_id );
    attr_list.setAttribute( id_attr, 0 );
  }

  /**
   *  Get the group ID for this Data block.
   *
   *  @return The ID used for this Data object.
   */
  public int getGroup_ID()
  {
    return group_id;
  }

  /**
   *  Set the selected flag of this Data block to the specified value.
   *
   *  @param  selected  New value for the selected flag.
   */
  public void setSelected( boolean selected )
  {
    if ( selected )
    {
      select_count++;
      this.selected = select_count;
    }
    else
      this.selected = 0;
  }

  /**
   *  Get the selected flag of this Data block.
   *
   *  @return  true if this Data block is selected, false otherwise.
   */
  public boolean isSelected( )
  {
    if ( selected != 0 )
      return true;
    else
      return false;
  }

  /**
   *  Toggle the selected flag of this Data block to the opposite state that
   *  it is currently in.
   */
  public void toggleSelected()
  {
    if ( isSelected() )
      setSelected( false );
    else
      setSelected( true );
  }

  /**
   *  Determine if this is the most recently selected Data block.
   *
   *  @return  true if this Data block is the most recently selected,
   *           otherwise return false.
   */
  public boolean isMostRecentlySelected( )
  {
    if ( selected == select_count )
      return true;
    else
      return false;
  }

  /**
   *  Get the value of the selection count for this Data block.
   *
   *  @return  The value of the selection count for this Data block.
   */
  public long getSelectionTagValue()
  {
    return selected;
  }

  /**
   *  Set the "hide" flag of this Data block to the specified value.
   *  
   *  @param  hide  New value for the hidden flag.
   */
  public void setHide( boolean hide )
  {
    this.hide = hide;
  }

  /**
   *  get the "hide" flag of this Data block.
   *
   *  @return  true if this Data block is marked "hide", false otherwise.
   */
  public boolean isHidden( )
  {
    return hide;
  }

  /**
   *  Toggle the "hide" flag of this Data block to the opposite state that
   *  it is currently in.
   */
  public void toggleHide()
  {
    hide = !hide;
  }
 
  /**
   *  Specify what label to use for this Data block.  The label can be either
   *  the name of an attribute (in which case the string form of the attribute
   *  will be used as the label) or a simple string.
   *
   *  @param  label  If label is the name of an attribute of the Data block,
   *                 at the time that this method is called, then the getLabel()
   *                 method will return the String form of the attribute, named
   *                 by the label parameter, otherwise the label parameter will
   *                 just be saved in the Data block and returned by getLabel().
   */
  public void setLabel( String label )
  {
    if (label == null)                               // ignore invalid request
      return;

    label_string = label;
    Attribute attr = attr_list.getAttribute(label);

    if ( attr == null )                              // use string directly
      label_is_attribute = false;                    // as label
    
    else                                             // use named attribute
      label_is_attribute = true;                     // as label
  }

  /**
   *  Get the label String for this Data block.  This will be either the label
   *  specified by setLabel(), or the String form of the attribute named by
   *  the label.  The named attribute is used if the attribute was present in
   *  the list of attributes at the time setLabel() and getLabel() is called.
   *
   *  @return  A String label for this Data block. 
   */
  public String getLabel()
  {
    if ( label_is_attribute )
    {
      Attribute attr = attr_list.getAttribute( label_string );
      if ( attr != null )
        return attr.toString();
    }

    return label_string;
  }

  /**
   *  Get the name of the label's attribute, if the label is based on an
   *  attribute, or null if the label has not been specified, or is specified
   *  as just a String.
   *  
   *  @return  A String label for this Data block. 
   */
  public String getLabelName()
  {
    if ( label_is_attribute )
      return label_string;

    return null;
  }

  /**
   *  Copy the fields from the specified Data object to the current Data
   *  object.  This is a convenience method to be used by derived classes
   *  when they are creating a clone.
   *
   *  @param  d   The Data object whose fields are to be copied.
   */
   protected void copyFields( Data d )
   {
     if ( d == null )
       return;

     selected           = d.selected;
     hide               = d.hide;
     label_string       = d.label_string;
     label_is_attribute = d.label_is_attribute;
     use_sqrt_errors    = d.use_sqrt_errors;
     if ( d.gridIDs != null )
       gridIDs = (Hashtable)(d.gridIDs.clone());
   }

  /**
   * Combine the attribute list of the specified Data object with the attribute
   * list of the current Data object to obtain a new attribute list for the
   * current Data object.
   *
   *  @param  d    The Data object whose attribute list is to be combined
   *               with the current object's attribute list
   */
   public void combineAttributeList( Data d )
   {
     attr_list.combine( d.getAttributeList() );
   }

  /**
   *  Get a reference to the list of attributes for this Data object.
   *
   *  @return The AttributeList for this Data object. 
   */
  public AttributeList getAttributeList()
  {
    return attr_list;
  }

  /**
   *  Replace the list of attributes for this Data object with a COPY of the
   *  specified list of attributes.
   *
   *  @param attr_list  The list of attributes to be copied to this Data object.
   */
  public void setAttributeList( AttributeList attr_list )
  {
    if ( !attr_list.equals( this.attr_list ) )            // only clone if this
      this.attr_list = (AttributeList)attr_list.clone();  // is a new list

    boolean id_set = false;
    int     i      = 0;
    while ( i < this.attr_list.getNum_attributes() && !id_set )
    {
      Attribute attribute = this.attr_list.getAttribute(i);
      id_set = syncGroup_ID_to_Attribute( attribute );
      i++;
    }

    if ( !id_set )
      setGroup_ID( this.group_id );   // force the attribute list to contain the
                                      // correct ID
  }

  /**
   * Gets the number of attributes set for this object.
   *
   *  @return the length of the AttributeList for this Data block. 
   */
  public int getNum_attributes()
  {
    return attr_list.getNum_attributes();
  }

  /**
   * Remove the attribute at the specified index from the list of
   * attributes. If the index is invalid, this does nothing.
   *
   * @param index The position of the attribute to remove.
   */
  public void removeAttribute( int index ){
      attr_list.removeAttribute(index);
  }

  /**
   * Remove the attribute with the specified name from the list of
   * attributes. If the named attribute is not in the list, this does
   * nothing.
   *
   * @param name The name of the attribute to remove.
   */
  public void removeAttribute( String name ){
      attr_list.removeAttribute( name );
  }

  /**
   * Set the value of the specified attribute in the list of attributes.
   * If the attribute is already present in the list, the value is changed
   * to the value of the new attribute.  If the attribute is not already
   * present in the list, the new attribute is added to the list.
   *
   *  @param  attribute    The new attribute to be set in the list of
   *                       attributes.
   */
  public void setAttribute( Attribute attribute )
  {
    attr_list.setAttribute( attribute );
    syncGroup_ID_to_Attribute( attribute );
  }

  /**
   * Set the value of the specified attribute in the list of attributes at
   * at the specified position.  If the attribute is already present in the
   * list, the value is changed to the value of the new attribute.  If the
   * attribute is not already present in the list, the new attribute is added
   * to the list.
   *
   *  @param  attribute    The new attribute to be set in the list of
   *                       attributes.
   *
   *  @param  index        The position where the attribute is to be
   *                       inserted.
   */
  public void setAttribute( Attribute attribute, int index )
  {
    attr_list.setAttribute( attribute, index );
    syncGroup_ID_to_Attribute( attribute );
  }


  /**
   * Get the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   *
   * @param  index  The position of the attribute in the list of attributes.
   *
   * @return The Attribute is returned as a generic object. 
   */
  public Attribute getAttribute( int index )
  {
    return attr_list.getAttribute( index );
  }


  /**
   * Get the attribute with the specified name from the list of
   * attributes.  If the named attribute is not in the list, this
   * returns null.
   *
   * @param  name  The name of the attribute value to get.
   *
   * @return The Attribute is returned as a generic object. 
   */
  public Attribute getAttribute( String name )
  {
    return attr_list.getAttribute( name );
  }


  /**
   * Get the value of the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   *
   * @param  index  The position of the attribute in the list of attributes.
   *
   * @return The Object that is the specified Attribute's value is returned 
   *         as a generic object. 
   */
  public Object  getAttributeValue( int index )
  {
    return attr_list.getAttributeValue( index );
  }


  /**
   * Get the value of the attribute with the specified name from the list of
   * attributes.  If the named attribute is not in the list, this returns null.
   *
   * @param  name  The name of the attribute value to get.
   *
   * @return The Object that is the specified Attribute's value is returned 
   *         as a generic object. 
   */
  public Object getAttributeValue( String name )
  {
    return attr_list.getAttributeValue( name );
  }

  /**
   * Returns a COPY of the array of x values represented by the x scale 
   * for this Data object 
   *
   * @return The list of x values currently used by this Data object. 
   */
  public float[] getX_values()
  { 
    return x_scale.getXs(); 
  }

  /**
   * Returns a reference to the "X" scale for this Data object
   *
   * @return The XScale object used by this Data object. 
   */
  public XScale getX_scale() 
  { 
    return x_scale; 
  }

  // These are convenience methods that get a reference to the lower level 
  // array and then copy it and return a copy of it.  

  /**
   *  Get a copy of the y_values corresponding to the XScale for this Data 
   *  object.
   *
   *  @return  A reference to a new copy of the y_values
   */
  public float[] getCopyOfY_values()       
  {
    float y_vals[] = getY_values();
    if ( y_vals != null )
    {
      float new_vals[] = new float[ y_vals.length ];
      System.arraycopy( y_vals, 0, new_vals, 0, y_vals.length );
      return new_vals;
    }
    else
      return null;
  } 

  /**
   *  Get a copy of the error estimates for the y_values for this Data 
   *  object.
   *
   *  @return  A reference to a new copy of the error estimates. 
   */
  public float[] getCopyOfErrors()       
  {
    float errors[] = getErrors();
    if ( errors != null )
    {
      float new_vals[] = new float[ errors.length ];
      System.arraycopy( errors, 0, new_vals, 0, errors.length );
      return new_vals;
    }
    else
      return null;
  } 

  /**
   *  Specify whether the errors are to be estimated as the square root of
   *  the y values.  If use_sqrt is true, derived classes may 
   *  calculate the error estimates "on the fly" rather than storing them.
   *
   *  @param use_sqrt If true, error estimates will be calculated as the
   *                  square root of the y values, if false, error estimates
   *                  may be specified in other ways for derived classes.
   */
  public void setSqrtErrors( boolean use_sqrt )
  {
    use_sqrt_errors = use_sqrt;
  }

  /**
   *  Check whether or not errors for this Data block are calculated as the
   *  square root of the y values.
   *
   *  @return true if the error estimates are calculated as the square root 
   *               of the y values, and false otherwise.
   */
  public boolean isSqrtErrors()
  {
    return use_sqrt_errors;
  }

  /**
    * Construct a new Data object by ADDING corresponding "y" values of the
    * current Data object and the specified Data object d.  If both the
    * current and the specified Data object d have errors, the errors
    * will propagate to the new Data object.  The attributes for the resulting 
    * Data object are a combination of the attributes of the current Data 
    * object and the specified Data object d.
    *
    * The combination is treated differently, depending on whether or not
    * the two data blocks have the same group ID and on what the attribute is.
    * Specifically the treatment of some of the most important attributes is
    * as follows:
    *
    * Same Group ID:
    *   TOTAL_COUNT       summed
    *   NUMBER_OF_PULSES  summed
    *   SOLID_ANGLE       averaged
    *   RAW_ANGLE         averaged
    *   DELTA_TWO_THETA   averaged
    *   DETECTOR_POS      averaged
    *   DETECTOR_POS      average, weighted by SOLID_ANGLES if present
    *
    * Different Group ID:
    *   TOTAL_COUNT       summed
    *   NUMBER_OF_PULSES  averaged
    *   SOLID_ANGLE       summed
    *   RAW_ANGLE         keep raw angle of current Data object
    *   DELTA_TWO_THETA   max of current value or difference of RAW_ANGLEs
    *   DETECTOR_POS      average, weighted by SOLID_ANGLES if present
    *
    * @param   other_d   The Data object to be added to the current data object
    *
    */
  public Data add( Data other_d )
  {
    String type;
    if ( isHistogram() )
      type = Data.HISTOGRAM;
    else
      type = Data.FUNCTION;
                                           // make a new TabulatedData object
                                           // from both the Data objects
    TabulatedData temp = TabulatedData.getInstance( this, group_id, type );
    TabulatedData d    = TabulatedData.getInstance( other_d, group_id, type );

    if ( ! this.compatible( d ) )
      d.resample( x_scale, SMOOTH_LINEAR );

    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] += d.y_values[i];

    if ( temp.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( temp.errors[i] * temp.errors[i] +
                                               d.errors[i] *    d.errors[i] );
    else
      temp.errors = null;
                                         // now take care of attributes... most
                                         // will use the default combine method
                                         // but some be treated differently
    Attribute attr,
              attr1,
              attr2;
    temp.combineAttributeList( d );
    temp.attr_list.add( Attribute.TOTAL_COUNT,
                        this.getAttributeList(),
                        d.getAttributeList()    );
    // do weighted sum of DetectorPosition, weighted by solid angle.
    attr1 = this.attr_list.getAttribute(Attribute.DETECTOR_POS);
    attr2 = d.attr_list.getAttribute(Attribute.DETECTOR_POS);
    if ( attr1 != null && attr2 != null )
    {
      DetectorPosition points[] = new DetectorPosition[2];
      points[0] = (DetectorPosition)attr1.getValue();
      points[1] = (DetectorPosition)attr2.getValue();
      attr1 = this.attr_list.getAttribute(Attribute.SOLID_ANGLE);
      attr2 = d.attr_list.getAttribute(Attribute.SOLID_ANGLE);
      if ( attr1 != null && attr2 != null )
      {
        float weights[] = new float[2];
        weights[0] = (float)attr1.getNumericValue();
        weights[1] = (float)attr2.getNumericValue();
        DetectorPosition ave_pos =
                  DetectorPosition.getAveragePosition( points, weights );
        attr = new DetPosAttribute( Attribute.DETECTOR_POS, ave_pos );
        temp.attr_list.setAttribute(attr);
      }
    }
                                         // special cases for adding same
                                         // groups from different runs, or
                                         // different groups ( same run? )
    if ( this.group_id == d.group_id )
      temp.attr_list.add( Attribute.NUMBER_OF_PULSES,
                          this.getAttributeList(),
                          d.getAttributeList()    );
    else
    {
      temp.attr_list.add( Attribute.SOLID_ANGLE,
                          this.getAttributeList(),
                          d.getAttributeList()    );

                                           // keep raw angle of first Data block
      attr = this.attr_list.getAttribute(Attribute.RAW_ANGLE);
      if ( attr != null )
        temp.attr_list.setAttribute( attr );
                                                    // approximate maximum for
                                                    // delta two theta
      attr1 = this.attr_list.getAttribute(Attribute.RAW_ANGLE);
      attr2 = d.attr_list.getAttribute(Attribute.RAW_ANGLE);
      if ( attr1 != null && attr2 != null )
      {
        float new_delta = (float)Math.abs( attr1.getNumericValue() -
                                           attr2.getNumericValue() );
        attr = this.attr_list.getAttribute(Attribute.DELTA_2THETA);
        if ( attr != null )
        {
          float delta = (float)attr.getNumericValue();
          attr = new FloatAttribute( Attribute.DELTA_2THETA,
                                     Math.max( delta, new_delta ) );
          temp.attr_list.setAttribute(attr);
        }
        else
        {
          attr = new FloatAttribute( Attribute.DELTA_2THETA,
                                     Math.max( new_delta, new_delta ) );
          temp.attr_list.setAttribute(attr);
        }
      }
    }

    return temp;
  }


  /**
    * Construct a new Data object by SUBTRACTING corresponding "y" values of
    * the current Data object and the specified Data object d.  If both the
    * current and the specified Data object have errors, the errors
    * will propagate to the new Data object.  The attributes for the resulting 
    * Data object are the same as the attributes of the current Data object.
    *
    * @param   other_d   The Data object to be subtracted from the current data
    *                    object
    */

  public Data subtract( Data other_d )
  {
    String type;
    if ( isHistogram() )
      type = Data.HISTOGRAM;
    else
      type = Data.FUNCTION;
                                           // make a new TabulatedData object
                                           // from both the Data objects
    TabulatedData temp = TabulatedData.getInstance( this, group_id, type );
    TabulatedData d    = TabulatedData.getInstance( other_d, group_id, type );

    if ( ! this.compatible( d ) )
      d.resample( x_scale, SMOOTH_LINEAR ); 

    for ( int i = 0; i < temp.y_values.length; i++ )
      temp.y_values[i] -= d.y_values[i];

    if ( temp.errors != null && d.errors != null )
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( temp.errors[i] * temp.errors[i] +
                                               d.errors[i] *    d.errors[i] );
    else
      temp.errors = null;

    return temp;
  }


  /**
    * Construct a new Data object by MULTIPLYING corresponding "y" values of
    * the current Data object and the specified Data object d.  If both the
    * current and the specified Data object d have errors, the errors
    * will propagate to the new Data object.  The attributes for the 
    * resulting Data object are the same as the attributes of the current 
    * Data object.
    *
    * @param   other_d   The Data object to be multiplied times the current data
    *                    object
    */

  public Data multiply( Data other_d )
  {
    String type;
    if ( isHistogram() )
      type = Data.HISTOGRAM;
    else                                   
      type = Data.FUNCTION;
                                           // make a new TabulatedData object
                                           // from both the Data objects
    TabulatedData temp = TabulatedData.getInstance( this, group_id, type );
    TabulatedData d    = TabulatedData.getInstance( other_d, group_id, type );

    if ( ! this.compatible( d ) )
      d.resample( x_scale, SMOOTH_LINEAR ); 

    if ( temp.errors != null && d.errors != null )       // propagate errors 
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt(
              temp.errors[i] * d.y_values[i] * temp.errors[i] * d.y_values[i] +
              d.errors[i] * temp.y_values[i] * d.errors[i] * temp.y_values[i] );
    else
      temp.errors = null;

    for ( int i = 0; i < temp.y_values.length; i++ ) // multiply values last
      temp.y_values[i] *= d.y_values[i];             // since error calculation
                                                     // needs original values 
    return temp;
  }


  /**
    * Construct a new Data object by DIVIDING corresponding "y" values of
    * the current Data object and the specified Data object d.  If both the
    * current and the specified Data object d have errors, the errors
    * will propagate to the new Data object.  The attributes for the
    * resulting Data object are the same as the attributes of the current
    * Data object.
    *
    * @param   other_d  The Data object to be divided into the current data
    *                   object
    */

  public Data divide( Data other_d )
  {
    String type;
    if ( isHistogram() )
      type = Data.HISTOGRAM;
    else
      type = Data.FUNCTION;
                                           // make a new TabulatedData object
                                           // from both the Data objects
    TabulatedData temp = TabulatedData.getInstance( this, group_id, type ); 
    TabulatedData d    = TabulatedData.getInstance( other_d, group_id, type ); 

    if ( !temp.compatible( d ) )
      d.resample( temp.x_scale, SMOOTH_LINEAR );

    if ( temp.errors != null && d.errors != null )          // errors set 
      for ( int i = 0; i < temp.errors.length; i++ )
        if ( temp.y_values[i] != 0 && d.y_values[i] != 0 ) 
        { 
          temp.errors[i] = Math.abs((temp.y_values[i]/d.y_values[i])) * 
            (float) Math.sqrt(
            temp.errors[i]/temp.y_values[i] * temp.errors[i]/temp.y_values[i]+
                  d.errors[i]/d.y_values[i] * d.errors[i]/d.y_values[i] );
        }
        else                                                // error undefined
          temp.errors[i] = 0;                               // so set to 0
    else
      temp.errors = null;

    for ( int i = 0; i < temp.y_values.length; i++ ) // divide values last
      if ( d.y_values[i] != 0 )                      // since error calculation
        temp.y_values[i] /= d.y_values[i];           // needs original values
      else
        temp.y_values[i] = 0;                        // set value to 0 if we
                                                     // would have divided by 0

    return temp;
  }


  /**
    * Construct a new Data object by ADDING the specified value "val" to each
    * "y" value of the  current Data object.  The error in the value "val"
    * is specified.  If the current Data object has an error array,
    * the error values will be combined with the specified error and
    * set in the new Data object.
    *
    * @param   val  The value to be added to the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "val".
    */

  public Data add( float val, float err )
  {
    TabulatedData temp = TabulatedData.getInstance( this, this.group_id ); 

    for ( int i = 0; i < temp.y_values.length; i++ )     // add values 
      temp.y_values[i] += val;
   
    if ( temp.errors != null && err > 0 )                // adjust the errors
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( temp.errors[i] * temp.errors[i] +
                                                       err * err );
    return temp;
  }


  /**
    * Construct a new Data object by SUBTRACTING the specified value "val"
    * from each "y" value of the  current Data object.  The error in the value
    * "val" is specified.  If the current Data object has errors,
    * the error values will be combined with the specified error and set
    * in the Data object.
    *
    * @param   val  The value to be added to the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "val".
    */

  public Data subtract( float val, float err )
  {
    TabulatedData temp = TabulatedData.getInstance( this, this.group_id );

    for ( int i = 0; i < temp.y_values.length; i++ )     // subtract values
      temp.y_values[i] -= val;

    if ( temp.errors != null && err > 0 )                // adjust the errors
      for ( int i = 0; i < temp.errors.length; i++ )
        temp.errors[i] = (float) Math.sqrt( temp.errors[i] * temp.errors[i] +
                                                       err * err );
    return temp;
  }


  /**
    * Construct a new Data object by MULTIPLYING the specified value "val"
    * times each "y" value of the  current Data object.  The error in the value
    * "val" is specified.  If the current Data object has an error array,
    * the error values will be combined with the specified error and set
    * in the new Data object.
    *
    * @param   val  The value to be multiplied times the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "val".
    */

  public Data multiply( float val, float err )
  {
    TabulatedData temp = TabulatedData.getInstance( this, this.group_id );

    if ( temp.errors != null )                           // propagate errors
    {
      float abs_val = Math.abs(val);

      if ( err <= 0 )                                    // invalid or 0 error
        for ( int i = 0; i < temp.errors.length; i++ )
          temp.errors[i] *= abs_val;

      else
        for ( int i = 0; i < temp.errors.length; i++ )
          temp.errors[i] = (float) Math.sqrt(
                             temp.errors[i] * val * temp.errors[i] * val +
                           err * temp.y_values[i] * err * temp.y_values[i] );
    }

    for ( int i = 0; i < temp.y_values.length; i++ )    // multiply values must
      temp.y_values[i] *= val;                          // be done AFTER the
                                                        // errors, since error 
                                                        // calculation needs
                                                        // original y's
    return temp;
  }


  /**
    * Construct a new Data object by DIVIDING the specified value "val"
    * into each "y" value of the  current Data object.  The error in the value
    * "val" is specified. If the current Data object has specified error values,
    * the error values will be combined with the specified error and set in
    * the new Data object.
    *
    * @param   val  The value to be divided into the y values of the current
    *               data object
    * @param   err  The error bound for the specified value "val".
    *
    * @return a new Data set with each y value divided by the given value. 
    *         If the specified value is zero, division can't be done so 
    *         this returns null.
    */

  public Data divide( float val, float err )
  {
    if ( val == 0.0f )
      return null;

    TabulatedData temp = TabulatedData.getInstance( this, this.group_id );

    if ( temp.errors != null )                          // propagate errors 
    {
      float abs_val = Math.abs(val);

      if ( err <= 0 )                                   // invalid or 0 error
        for ( int i = 0; i < temp.errors.length; i++ )
          temp.errors[i] /= abs_val;

      else
        for ( int i = 0; i < temp.errors.length; i++ )
          if ( temp.y_values[i] != 0 ) 
            temp.errors[i] = Math.abs((temp.y_values[i]/val))*(float)Math.sqrt(
              temp.errors[i]/temp.y_values[i] * temp.errors[i]/temp.y_values[i]+
              err/val * err/val );
          else
            temp.errors[i] = 0;                         // undefined 
    }

    for ( int i = 0; i < temp.y_values.length; i++ )    // divide values must 
      temp.y_values[i] /= val;                          // be done AFTER the
                                                        // errors since error 
                                                        // calculation needs
                                                        // original y's 
    return temp;
  }


  /**
   *    "Stitch" another Data block together with the current Data block to 
   *  form a new TabulatedData block with the same attributes as the current
   *  Data block, but whose data is a combination of the two. 
   *
   *    New TabulatedData blocks will first be generated from the current
   *  Data object and the other_data block.  These TabulatedData blocks will
   *  of the same type ( HISTOGRAM or FUNCTION ) as the current Data block.  
   *  Then a new XScale is formed and the y-values from the two Data blocks 
   *  are "stitched" together over the new XScale.  The XScale for the new 
   *  Data object covers an interval containing the union of the current 
   *  Data's XScale and the other Data's XScale.
   *
   *    If the XScale of the current Data block is a uniform XScale, the new
   *  XScale will be a uniform XScale with the same spacing as the current
   *  XScale, aligned with the current XScale.  If the XScale of the current
   *  Data block is a VariableXScale, the new XScale will be a VariableXScale
   *  using the same x-values as the current Data's for the full extent of the
   *  current Data's XScale.  In this case, the new VariableXScale will only
   *  use the other_data's x-values for the interval covered by it's XScale
   *  and NOT covered by the current Data's XScale.
   *
   *    The other_data object will be resampled over the new XScale before
   *  forming the y-values.  For the portion of the new XScale covered by
   *  only the current Data, the current Data's y-values are used.  For the
   *  portion of the new XScale covered by only the other Data's XScale, the
   *  other Data's y-values are used.  The y-values for portions of the
   *  intervals that overlap are selected from the current Data or the other
   *  Data's y-values, or the average of the y-values, as determined by the
   *  "overlap" parameter.
   *
   *  @param  other_data  The other Data block whose data is to be combined
   *                      with the current data.
   *  @param  overlap     Flag that indicates what should be done on the
   *                      interval where the two Data blocks overlap ( if any ).
   *                      This must be one of the constants:
   *
   *                              Data.KEEP
   *                              Data.AVERAGE
   *                              Data.DISCARD
   *                              Data.SUM
   *
   *                      indicating that the original Data's values should be
   *                      kept, averaged with the other Data's values or
   *                      discarded and the other Data's values used instead.
   *
   *  @return  A new Data object with the same attributes as the current
   *           Data object, but whose data is a combination of the two.  The
   *           XScale for the new DataObject
   */
  public Data stitch( Data other_data, int overlap )
  {
    String type;
    if ( isHistogram() )
      type = Data.HISTOGRAM;
    else
      type = Data.FUNCTION;
                                           // make a new TabulatedData object
                                           // from both the Data objects
    TabulatedData new_data  = TabulatedData.getInstance( this, group_id, type );
    TabulatedData temp_data = TabulatedData.getInstance( other_data, 
                                                         group_id, type );

                                         // set error values for new_data if
                                         // needed and not present
    boolean has_error_info = false; 
    if ( new_data.errors != null )
    {
      has_error_info = true;
      if ( temp_data.errors == null )   // if no previous error values set     
      {                                 // allocate error array and set default
        int n = temp_data.y_values.length;
        temp_data.errors = new float[ n ];
        for ( int i = 0; i < n; i++ )
          temp_data.errors[i] = 0; 
      }
    }
                                        // record the extent of the original
                                        // XScales
    ClosedInterval interval = new ClosedInterval( new_data.x_scale.getStart_x(),
                                                  new_data.x_scale.getEnd_x() );
    ClosedInterval other_interval =
                           new ClosedInterval( temp_data.x_scale.getStart_x(),
                                               temp_data.x_scale.getEnd_x() );

                                          // extablish a new XScale as the
                                          // common XScale for the Data blocks
    XScale new_x_scale = new_data.x_scale.extend( temp_data.x_scale );
    temp_data.resample( new_x_scale, SMOOTH_LINEAR );
    new_data.resample( new_x_scale, SMOOTH_LINEAR );
                                          // Now combine the y_values and errors
    float x[] = new_x_scale.getXs();
    if ( !new_data.isHistogram() )                 // use one x value to decide
      for ( int i = 0; i < new_data.y_values.length; i++ )
      {
        if ( interval.contains( x[i] ) )             // keep the current y_value
        {                                            // or alter it if necessary
          if ( other_interval.contains( x[i] ) )
            if ( overlap == Data.AVERAGE )
            {
              new_data.y_values[i] = ( new_data.y_values[i] +
                                       temp_data.y_values[i] )/2;
              if ( has_error_info )
                new_data.errors[i] = (float)Math.sqrt(
                                  new_data.errors[i] * new_data.errors[i] +
                                 temp_data.errors[i] * temp_data.errors[i] )/2;
            }
            else if ( overlap == Data.DISCARD )
            {
              new_data.y_values[i] = temp_data.y_values[i];
              if ( has_error_info )
                new_data.errors[i] = temp_data.errors[i];
            }
            else if ( overlap == Data.SUM ){
              new_data.y_values[i] =new_data.y_values[i]+temp_data.y_values[i];
              if ( has_error_info )
                new_data.errors[i] = (float)Math.sqrt(
                                  new_data.errors[i] * new_data.errors[i] +
                                 temp_data.errors[i] * temp_data.errors[i] );
            }
        }

        else if ( other_interval.contains( x[i] ) )  // use the other y_value
        {
          new_data.y_values[i] = temp_data.y_values[i];
          if ( has_error_info )
            new_data.errors[i] = temp_data.errors[i];
        }
        else
        {
          new_data.y_values[i] = 0;                  // neither interval
          if ( has_error_info )
            new_data.errors[i] = 0;
        }
      }

    else if ( new_data.isHistogram() )           // use bin boudaries to decide
      for ( int i = 0; i < new_data.y_values.length; i++ )
      {
        if ( interval.contains( x[i] ) &&
             interval.contains( x[i + 1] ) )         // keep the current y_value
        {                                            // or alter it if necessary
          if ( other_interval.contains( x[ i ] ) &&
               other_interval.contains( x[ i + 1 ] ) )
            if ( overlap == Data.AVERAGE )
            {
              new_data.y_values[i] = ( new_data.y_values[i] +
                                       temp_data.y_values[i] )/2;
              if ( has_error_info )
                new_data.errors[i] = (float)Math.sqrt(
                                  new_data.errors[i] * new_data.errors[i] +
                                 temp_data.errors[i] * temp_data.errors[i] )/2;
            }  
            else if ( overlap == Data.DISCARD )
            {
              new_data.y_values[i] = temp_data.y_values[i];
              if ( has_error_info )
                new_data.errors[i] = temp_data.errors[i];
            }
            else if ( overlap == Data.SUM ){
              new_data.y_values[i] =new_data.y_values[i]+temp_data.y_values[i];
              if ( has_error_info )
                new_data.errors[i] = (float)Math.sqrt(
                                  new_data.errors[i] * new_data.errors[i] +
                                 temp_data.errors[i] * temp_data.errors[i] );
            }
        }

        else if ( other_interval.contains( x[i] ) &&
                  other_interval.contains( x[i+1] ) ) // use the other y_value
        {
          new_data.y_values[i] = temp_data.y_values[i];
          if ( has_error_info )
            new_data.errors[i] = temp_data.errors[i];
        }
        else
        {
          new_data.y_values[i] = 0;                  // neither interval
          if ( has_error_info )
            new_data.errors[i] = 0;
        }
      }

    return new_data;
  }


  /**
   *  Provide an identifier string for this Data block
   */
  public String toString()
  {
    String result = getLabel();

    if ( result == null || result.length() == 0 )
      return Attribute.GROUP_ID + group_id;
    else
      return result;
  }


  // The remaining methods are abstract and will have to be implemented in 
  // each derived class.  Consequently, the semantics of the methods will vary
  // from class to class.
  // NOTE: There is not a setY_values() or a setX_values().  The x and y 
  //       values must remain coordinated, and are only directly set in the 
  //       constructor or indirectly adjusted using the resample method. 
 
  abstract public boolean isHistogram();

  abstract public float[] getY_values();
  abstract public float[] getY_values( XScale x_scale, int smooth_flag ); 
  abstract public float   getY_value( float x, int smooth_flag ); 

  abstract public float[] getErrors();

  abstract public void    resample( XScale x_scale, int smooth_flag );
  abstract public Object  clone();

  /**
    * Returns true or false depending on whether the two Data objects are
    * capable of being added, etc., based on the size and extent of their
    * x_scales.  If the XScales are NOT uniform, each corresponding point
    * of the XScales should probably be compared, however, this is not
    * currently done.  Variable x_scales are assumed to be incompatible
    *
    *  @param  d     The Data object to be compared with the current data
    *                object.
    */
  public boolean compatible( Data d )
  {
    if ( !(  x_scale instanceof UniformXScale)  ||
         !(d.x_scale instanceof UniformXScale)      )
      return false;

    if ( this.x_scale.getNum_x() != d.x_scale.getNum_x() )
      return false;

    if ( this.x_scale.getStart_x() != d.x_scale.getStart_x() )
      return false;

    if ( this.x_scale.getEnd_x() != d.x_scale.getEnd_x() )
      return false;

    return true;
  }

  /** Implements the IXmlIO interface so a Data Object can write itself
  *
  * @param stream  the OutputStream to which the data is written
  * @param mode    either IXmlIO.BASE64 or IXmlOP.NORMAL. This indicates 
  *                how a Data's xvals, yvals and errors are written
  * @return true if successful otherwise false<P>
  *
  * NOTE: This implementation returns false. All subclasses should implement
  *      a legitimate XMLwrite routine
  */
  public boolean XMLwrite( OutputStream stream, int mode)
    {
     return xml_utils.setError("Data XMLwrite not implemented");
    }

  /** Implements the IXmlIO interface so a Data Object can read itself
  *
  * @param stream  the InputStream from which the data is to be read
 
  * @return true if successful otherwise false<P>
  *
  * NOTE: This implementation returns false. All subclasses should implement
  *      a legitimate XMLread routine
  */
  public boolean XMLread( InputStream stream )
    {
     return xml_utils.setError("Data XMLread not implemented");
    }
    
  /**
   * Gives access to the Hashtable of grid's that have already been set up
   * as the DataSet reads through the Datablocks described in an XML file
   * @param gridIds  The Hashtable of gridID's with their associated grid
   */
  public void setGridIds( Hashtable gridIds ){
    this.gridIDs = gridIds; 
  
  }
  

/* -----------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

/* ---------------------------- readObject ------------------------------- */
/**
 *  The readObject method is called when objects are read from a serialized
 *  ojbect stream, such as a file or network stream.  The non-transient and
 *  non-static fields that are common to the serialized class and the
 *  current class are read by the defaultReadObject() method.  The current
 *  readObject() method MUST include code to fill out any transient fields
 *  and new fields that are required in the current version but are not
 *  present in the serialized version being read.
 */

  private void readObject( ObjectInputStream s ) throws IOException,
                                                        ClassNotFoundException
  {
    s.defaultReadObject();               // read basic information

    if ( IsawSerialVersion != 1 )
      System.out.println("Warning:Data IsawSerialVersion != 1");

                                         // set meaningful values for transient
    hide         = false;                // fields
    selected     = 0;
    label_string = Attribute.GROUP_ID;
    label_is_attribute = true;
  }

/* ----------------------- syncGroup_ID_to_Attribute ---------------------- */
/**
 *  Set the group_id field from the specified attribute if the attribute
 *  name is the same as the "constant" Attribute.GROUP_ID, and then return
 *  true.  If the name is NOT the same as Attribute.GROUP_ID, then do nothing
 *  but return false.  
 */
  private boolean syncGroup_ID_to_Attribute( Attribute attribute )
  {
    if ( attribute.getName().equals( Attribute.GROUP_ID ) )  // set the field
    {                                                        // group_id to
      int id = (int)(attribute.getNumericValue());           // match
      group_id = id;
      return true;
    }

    return false;
  }

}
