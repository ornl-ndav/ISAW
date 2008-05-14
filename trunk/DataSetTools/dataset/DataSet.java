/*
 * File:  DataSet.java
 *
 * Copyright (C) 1999, Dennis Mikkelson
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
 *  Revision 1.59  2008/01/04 17:20:50  rmikk
 *  Updated to work with pixel info lists that are lists of pixel info's
 *
 *  Revision 1.58  2008/01/02 19:25:57  rmikk
 *  Added a method that Fixes the grid to a new reference in the PixelInfoList
 *    Attribute. The data pointers in this new grid point to data in the new data set,
 *    and the data pointers in the old grid point to the data in the old data set
 *
 *  Revision 1.57  2007/07/06 20:30:27  dennis
 *  Added convenience method: getIndex_of_data_with_id( id ), to find
 *  the position of a Data block with the specified id in the list of
 *  Data objects.
 *
 *  Revision 1.56  2007/04/27 03:19:34  dennis
 *  Fixed spelling error in javadoc.
 *
 *  Revision 1.55  2007/02/23 20:27:40  dennis
 *  Made a few small fixes and improvements to the javadocs.
 *
 *  Revision 1.54  2006/06/14 15:56:49  rmikk
 *  Set up the data grids in the XMLread for xml input for data sets
 *
 *  Revision 1.53  2006/03/15 14:52:08  dennis
 *  Changed the addOperator() method to first remove any operator
 *  of the same type as the operator being added, to avoid getting
 *  duplicate copies of the operator.
 *
 *  Revision 1.52  2005/11/21 23:13:31  dennis
 *  Fixed spelling error on variable name in javadoc comment.
 *
 *  Revision 1.51  2005/11/17 23:01:56  dennis
 *  getXRange() calculates range directly, rather than using expand()
 *  method of UniformXScale.
 *  Made a few minor improvements to the javadocs and format.
 *
 *  Revision 1.50  2005/10/28 15:28:23  dennis
 *  Fixed "spelling" error in four javadoc comments
 *
 *  Revision 1.49  2005/10/27 20:44:50  dennis
 *  Added methods setSelectFlagsByID() and setSelectFlagsByIndex().
 *
 *  Revision 1.48  2005/08/19 19:56:55  dennis
 *  Remove selected now will scan the DataSet and form a list of
 *  indices of selected Data blocks, then call removeData_entries(list)
 *  to remove all of the Data blocks "at once", if more than one Data
 *  block is selected.  This is more efficient than repeatedly calling
 *  removeData_entry(), to remove many Data blocks.
 *
 *  Revision 1.47  2005/08/18 22:56:58  dennis
 *  Added methods:
 *     removeData_entries( list[] ),
 *     removeData_entries_with_ids( list[] )
 *  to more quickly remove whole lists of Data blocks from a DataSet.
 *  These methods return a boolean value indicating whether or not
 *  the DataSet was actually modified.  This permits the calling
 *  code to avoid unneeded notification of observers of the the
 *  DataSet, if no changes were actually made.
 *  Modified:
 *     removeData_entry( index ),
 *     removeData_entry_with_id( group_id )
 *  to also return a boold value indicating whether or nat the
 *  DataSet was actually modified.
 *  Made some minor improvments to javadocs.
 *
 *  Revision 1.46  2005/05/27 03:44:09  dennis
 *  Removed unused imports (no longer needed since the convenience
 *  methods to get specific attributes were removed).
 *
 *  Revision 1.45  2005/05/27 03:21:18  dennis
 *  Removed convenience methods for getting particular attributes for
 *  TOF neutron scattering from these general purpose classes.  The
 *  convenience methods were moved to the new AttrUtil class.
 *
 *  Revision 1.44  2005/03/28 22:47:40  dennis
 *  Removed TITLE attribute, since the DataSet already has a
 *  field for the title.
 *
 *  Revision 1.43  2004/06/15 15:42:43  dennis
 *    Now prevents users from adding Data blocks, observers, attributes, etc. to
 *  the EMPTY_DATA_SET singleton.  Enforces the immutability of the
 *  EMPTY_DATA_SET object by adding conditional statements to check for the
 *  EMPTY_DATA_SET to methods that alter the DataSet.
 *    Minor improvement to code format.
 *
 *  Revision 1.42  2004/05/25 20:04:37  kramer
 *
 *  Implemented the new inherited methods from the interface IAttributeList that
 *  get attribute values.
 *
 *  Revision 1.41  2004/03/15 06:10:35  dennis
 *  Removed unused import statements.
 *
 *  Revision 1.40  2004/03/15 03:28:05  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.39  2003/10/15 23:50:12  dennis
 *  Fixed javadocs to build cleanly with jdk 1.4.2
 *
 *  Revision 1.38  2003/07/07 20:33:30  dennis
 *  Added method to remove all operators from a DataSet.
 *
 *  Revision 1.37  2003/03/04 20:42:24  dennis
 *  Added method "shallowCopy" that just copies the references
 *  to Data blocks, etc. from one DataSet to another.  While
 *  this method is "dangerous" and should not generally be used,
 *  it allows moving Data between the LiveDataRetriever and
 *  LiveDataMonitor much more efficiently.  Less space and less
 *  time is needed since the Data blocks are not cloned.
 *
 *  Revision 1.36  2003/02/10 13:28:38  dennis
 *  getAttributeList() now returns a reference to the attribute list,
 *  not a clone.
 *
 *  Revision 1.35  2002/11/27 23:14:06  pfpeterson
 *  standardized header
 *
 *  Revision 1.34  2002/10/03 15:34:33  dennis
 *  readObject() method now handles the case where the instrument type
 *  attribute is NOT an int array.  If it is a single integer, it is
 *  changed to an array with one entry.
 *
 *  Revision 1.33  2002/09/12 19:55:54  dennis
 *  Now always uses java's sort.  This fixes a stack overflow problem when
 *  sorting large arrays with equal values, that came up when sorting SCD
 *  data by crate.
 *
 *  Revision 1.32  2002/08/01 22:33:34  dennis
 *  Set Java's serialVersionUID = 1.
 *  Set the local object's IsawSerialVersion = 1 for our
 *  own version handling.
 *  Added readObject() method to handle reading of different
 *  versions of serialized object.
 *
 *  Revision 1.31  2002/08/01 19:46:18  dennis
 *  Made more robust for serialization:
 *  Made appropriate fields transient.
 *  Added DataSetVersion = 1, field, to manage our own version system.
 *  Added readObject() method that fills in values for
 *        transient fields.
 *
 *  Revision 1.30  2002/07/23 18:11:14  dennis
 *  Added fields: pointed_at_x, selected_interval
 *  Added methods: set/getSelectedInterval(), set/getPointedAtX()
 *  and adjusted copy() and empty_clone() to preserve the
 *  fields.
 *
 *  Revision 1.29  2002/07/15 19:43:48  dennis
 *  1. Added convenience method setData_label(name) that calls
 *     setLabel(name) for each Data block in the DataSet.
 *  2. Now uses Java's sort method from java.util.Arrays for use
 *     when a STABLE sort method is needed.
 *
 *  Revision 1.28  2002/07/11 18:18:44  dennis
 *  Added  serialVersionUID = 1L;
 *
 *  Revision 1.27  2002/07/10 16:02:24  pfpeterson
 *  Added removeAttribute() methods.
 *
 *  Revision 1.26  2002/07/08 15:40:45  pfpeterson
 *  New replaceData_entry_with_id() method to deal with group ids.
 *
 *  Revision 1.25  2002/06/19 20:53:43  rmikk
 *  Added a Default Constructor
 *
 *  Revision 1.24  2002/06/18 19:29:41  rmikk
 *  Fixed a minor error in XMLwrite
 *
 *  Revision 1.23  2002/06/17 22:44:24  rmikk
 *  Add a method to set the standalone varible when writing
 *
 *  Revision 1.22  2002/06/14 20:49:07  rmikk
 *  Implements the IXmlIO interface
 *
 *  Revision 1.21  2002/03/13 16:08:33  dennis
 *  Data class is now an abstract base class that implements IData
 *  interface. FunctionTable and HistogramTable are concrete derived
 *  classes for storing tabulated functions and frequency histograms
 *  respectively.
 *
 *  Revision 1.20  2002/02/22 20:35:06  pfpeterson
 *  Operator Reorganization.
 *
 */

package  DataSetTools.dataset;

import gov.anl.ipns.Util.File.*;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import java.util.*;
import java.io.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.instruments.*;

/**
 * The concrete base class for a set of Data objects.  A DataSet object
 * bundles together a vector of Data objects with associated units, labels,
 * title, attributes and log information.  Data objects can be added to or 
 * removed from the DataSet using the methods of this class.  Also, the
 * list of attributes is extensible and can be accessed through methods in
 * this class.
 *
 * @see DataSetTools.dataset.Data
 *
 * @version 1.02  
 */

public class DataSet implements IAttributeList,
                                Serializable,
                                IObservable,
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

  private static long current_ds_tag = 0; 
                                          // Each DataSet will be assigned a
                                          // unique, immutable tag when it is
                                          // created.

  public static final int INVALID_GROUP_ID = -1;
  public static final int INVALID_INDEX    = -1;
  public static final int NOT_SORTED = -1;
  public static final int Q_SORT     =  0;
  public static final int JAVA_SORT  =  1;

                                          // Some operators need a default 
                                          // DataSet to hold in a parameter.
                                          // To avoid always constructing new
                                          // DataSets for this purpose, we
                                          // provide one constant empty DataSet
  public static final DataSet EMPTY_DATA_SET 
                      = new DataSet( "EMPTY_DATA_SET","Constant Empty DataSet");

  transient private IObserverList  observers;
  transient private Vector         operators;
  transient private long           ds_tag;

  transient private int            pointed_at_index;
  transient private float          pointed_at_x;
  transient private ClosedInterval selected_interval;
  transient private String         last_sort_attribute;
  transient private boolean        xmlStandAlone; //XMLread


  // NOTE: The following fields are serialized.  If new fields are added that
  //       are not static, reasonable default values should be assigned in the
  //       readObject() method for compatibility with old servers, until the
  //       servers can be updated. 

  private int           IsawSerialVersion = 1;  // CHANGE THIS WHEN ADDING OR
                                                // REMOVING FIELDS, IF
                                                // readObject() CAN FIX ANY
                                                // COMPATIBILITY PROBLEMS

  private String        title;      // NOTE: we force a DataSet to have a title
                                    // and also keep the same title as an
                                    // attribute.  The title can only be 
                                    // changed using the setTitle() method.
  private String        x_units;
  private String        x_label;
  private String        y_units;
  private String        y_label;
  private Vector        data;

  private AttributeList attr_list;
  private OperationLog  op_log;

  /**
   * Constructs an empty DataSet with the specified title, initial log
   * object, units and labels.
   *
   * @param   title     String giving a title for the DataSet.
   * @param   op_log    OperationLog object giving the initial log record
   *                    for this DataSet. 
   * @param   x_units   String specifying the units for the "X" axis.  This 
   *                    should be specified in a standard form. 
   * @param   x_label   String identifying the quantity measured in the "X"
   *                    direction. 
   * @param   y_units   String specifying the units for the "Y" axis.  This 
   *                    should be specified in a standard form. 
   * @param   y_label   String identifying the quantity measured in the "Y"
   *                    direction. 
   *
   * @see DataSetTools.dataset.Data
   */
  public DataSet( String        title, 
                  OperationLog  op_log,
                  String        x_units,
                  String        x_label,
                  String        y_units,
                  String        y_label )
  {
    this.op_log           = (OperationLog)op_log.clone();
    this.x_units          = x_units;
    this.x_label          = x_label;
    this.y_units          = y_units;
    this.y_label          = y_label;
    this.attr_list        = new AttributeList();
    this.data             = new Vector();
    this.operators        = new Vector();
    this.observers        = new IObserverList();
    this.pointed_at_index = INVALID_INDEX;
    this.pointed_at_x     = Float.NaN;
    this.selected_interval = new ClosedInterval( Float.NEGATIVE_INFINITY,
                                                 Float.POSITIVE_INFINITY );
    this.last_sort_attribute = "";
    this.xmlStandAlone = true;
    
    this.ds_tag           = current_ds_tag++;   // record tag and advance to 
                                                // the next tag value. 
    setTitle( title );
  }


  /**
   * Constructs an empty DataSet with the specified title, initial log 
   * string, units and labels. 
   *
   * @param   title     String giving a title for the DataSet.
   * @param   log_info  String giving log information for the DataSet.
   * @param   x_units   String specifying the units for the "X" axis.  This 
   *                    should be specified in a standard form. 
   * @param   x_label   String identifying the quantity measured in the "X"
   *                    direction. 
   * @param   y_units   String specifying the units for the "Y" axis.  This 
   *                    should be specified in a standard form. 
   * @param   y_label   String identifying the quantity measured in the "Y"
   *                    direction. 
   *
   * @see DataSetTools.dataset.Data
   */
  public DataSet( String  title, 
                  String  log_info,
                  String  x_units,
                  String  x_label,
                  String  y_units,
                  String  y_label )
  {
    this( title, new OperationLog(), x_units, x_label, y_units, y_label );
    this.op_log.addEntry( log_info );
  }


  /**
   * Constructs an empty DataSet with the specified title and log information.
   * The units and labels are assigned default values.
   *
   * @param   title     String giving a title for the DataSet.
   * @param   log_info  String giving log information for the DataSet.
   *
   * @see DataSetTools.dataset.Data
   */
  public DataSet( String  title, String  log_info )
  {
    this( title, log_info, "X UNITS", "X LABEL", "Y UNITS", "Y LABEL");
  }


  /**
  * Constructs an empty DataSet with no log info and an empty string for
  * the title.  This routine creates a DataSet that will then be filled up
  * by the XMLread method
  *
  * @param xmlStandAlone   <ul>
  *         <li>true to read from the start of an xml file. The <?xml processing
  *             tag and the <DataSet tag will be read
  *         <li> false means that the XMLread assumes that the DataSet tag has
  *              been read but NOT its xml attributes
  *          </ul>
  *  @see #XMLread( java.io.InputStream )
  */ 
  public DataSet( boolean xmlStandAlone )
  { 
    this("",(String)(null));
    this.xmlStandAlone = xmlStandAlone;
  }


 /**
  * Constructs an empty DataSet with no log info and an empty string for
  * the title.  This routine creates a DataSet that will then be filled up
  * by the XMLread method<P>
  *
  *  xmlStandAlone   will be set to false
  *
  *  @see #XMLread( java.io.InputStream )
  */
  public DataSet( )
  { 
    this("",(String)(null));
    this.xmlStandAlone = false;
  }


  /**
   *  Add the specified object to the list of observers to notify when this
   *  observable object changes.
   *
   *  @param  iobs   The observer object that is to be notified.
   *
   */
   public void addIObserver( IObserver iobs )
   {
     if ( this == EMPTY_DATA_SET )
     {
       System.out.println("WARNING: Adding IObserver to EMPTY_DATA_SET");
       return;
     }
     observers.addIObserver( iobs );
   }


  /**
   *  Remove the specified object from the list of observers to notify when
   *  this observable object changes.
   *
   *  @param  iobs   The observer object that should no longer be notified.
   *
   */
   public void deleteIObserver( IObserver iobs )
   {
     observers.deleteIObserver( iobs );
   }


  /**
   *  Remove all objects from the list of observers to notify when this
   *  observable object changes.
   */
   public void deleteIObservers( )
   {
     observers.deleteIObservers();
   }


  /**
   *  Notify all observers in the list ( by calling their update(,) method )
   *  that the observed object has changed.
   *
   * @param  reason        Object indicating the nature of the change in the
   *                       observable object, or specifying the action that the
   *                       observer should take.  This is passed as the second
   *                       parameter to the update(,) method of the observers
   *                       and will typically be a String.
   */
   public void notifyIObservers( Object reason )
   {
     observers.notifyIObservers( this, reason );
   }


  /**
   *  Get a copy of the list of observers.  This method is needed for
   *  serializing a DataSet.  In particular, if a DataSet is serialized, all
   *  of it's observers are also serialized.  To avoid this, the code that
   *  serializes the DataSet should proceed as follows:
   *    1. Get a copy of the list of observers getIObserverList()
   *    2. Delete all the observers from the the DataSet using 
   *       deleteIObservers()
   *    3. Serialize the DataSet
   *    4. Reset the list of observers using setIObserverList() 
   *
   *  @return the current list of observers for this DataSet
   */
   public IObserverList getIObserverList()
   {
     return (IObserverList)observers.clone();
   }


  /**
   *  Set the list of observers to the specified list.  This method is 
   *  needed to restore the list of observers after serializing the 
   *  DataSet.  See getIObserverList().
   *
   *  @param  list  The list of observers to use for this DataSet.
   */
   public void setIObserverList( IObserverList list )
   {
     if ( this == EMPTY_DATA_SET )
     { 
       System.out.println("WARNING: EMPTY_DATA_SET.setIObserverList() called");
       return;
     }
     observers = (IObserverList)list.clone();
   }


  /**
   * Returns the title of the DataSet 
   *
   * @return  The DataSet title.
   */
  public String getTitle() { return title; }


  /**
   * Returns a numeric tag that is unique to this DataSet within a program.
   * 
   * @return  The numeric tag assigned when the DataSet was constructed 
   */
  public long getTag() { return ds_tag; }


  /**
   *  Set the selected flags of all Data objects in this DataSet to false.
   *
   *  @return  true if the state of some "selected" flag was actually changed.
   */
  public boolean clearSelections()
  {
    boolean changed = false;
    Data d;

    for ( int i = 0; i < data.size(); i++ )
    {
      d = (Data)data.elementAt(i);
      if ( d.isSelected() )
      {
        d.setSelected(false);
        changed = true;
      }
    }
    return changed;
  }


  /**
   *  Set the selected flag of the specified Data object to the specified value.
   *  If the index is not valid, there is no effect.
   *
   *  @param  index     The position of the Data object in this DataSet whose
   *                    selection flag is to be set.
   *  @param  selected  New value for the selected flag.
   */
  public void setSelectFlag( int index, boolean selected )
  {
    if ( index >= 0 && index < data.size() )
      ((Data)data.elementAt(index)).setSelected( selected );
    else
      System.out.println("Error: setSelectFlag(i,s) called with invalid index");
  }


  /**
   *  Set the selected flag of the specified Data object to same value as the
   *  selected flag of the specified Data object.  If the index is not valid, 
   *  there is no effect.
   *
   *  @param  index     The position of the Data object in this DataSet whose
   *                    selection flag is to be set.
   *  @param  d         The Data object whose selection flag is to be copied.
   */
  public void setSelectFlag( int index, Data d )
  {
    if ( index >= 0 && index < data.size() )
      ((Data)data.elementAt(index)).setSelected( d.isSelected() );
    else
      System.out.println("Error: setSelectFlag(i,d) called with invalid index");
  }


  /**
   * Set (or clear) the selection flag for all data blocks in this DataSet
   * whose index is listed in the indicies list.  NOTE: The state of the 
   * select flag is NOT changed for Data blocks NOT in the list.
   * Also, this method does NOT automatically notify the Observers of the
   * DataSet that some selections have been changed.  This allows code that
   * makes multiple changes to only notify the Observers one time, after
   * completing all changes.
   * 
   * @param  indices      The list of indexes for which the selection
   *                      flag will be set to the specified value.
   *
   * @param  selected  The new value to set for the selected flag.
   *
   * @return  true if the selection flag of some Data block was changed,
   *          false otherwise. 
   */
  public boolean setSelectFlagsByIndex( int indices[], boolean selected )
  {
     boolean  some_changed = false;
     Data     d;
     int      position;
     int      n_data = data.size();

     if ( indices == null )
     {
        System.out.println("WARNING: Null array of indices in DataSet." +
                           "setSelectFlagsByIndex()" );
        return some_changed;
     }

     for ( int i = 0; i < indices.length; i++ )// set flags on indices listed
     {                                         // if current value is different
       position = indices[i];
       if ( position >= 0 && position < n_data )
       {
         d = (Data)(data.elementAt( indices[i] ));
         if ( d.isSelected() != selected )
         { 
           d.setSelected( selected );
           some_changed = true; 
         }
       }
     }

     return some_changed;
  }


  /**
   * Set (or clear) the selection flag for all data blocks in this DataSet
   * that have an ID listed in the group_ids list.  NOTE: The state of the 
   * select flag is NOT changed for Data blocks with group_ids NOT in the list.
   * Also, this method does NOT automatically notify the Observers of the
   * DataSet that some selections have been changed.  This allows code that
   * makes multiple changes to only notify the Observers one time, after
   * completing all changes.
   * 
   * @param  group_ids     The list of group_ids for which the selection
   *                       flag will be set to the specified value.
   *
   * @param  selected  The new value to set for the selected flag.
   *
   * @return  true if the selection flag of some Data block was changed,
   *          false otherwise. 
   */
  public boolean setSelectFlagsByID( int group_ids[], boolean selected )
  {
                                               // first make a copy of the ids
                                               // so we can sort and search
     int ids[] = new int[ group_ids.length ];
     System.arraycopy( group_ids, 0, ids, 0, group_ids.length );
     Arrays.sort( ids );

     boolean  some_changed = false;
     Data     d;
     int      group_id;
     int      position;

     if ( group_ids == null )
     {
        System.out.println("WARNING: Null array of group IDs in DataSet." +
                           "setSelectFlagsByID()" );
        return some_changed;
     }

     for ( int i = 0; i < data.size(); i++ )  // set flags on IDs found, if 
     {                                        // the current value is different
       d = (Data)data.elementAt(i);
       group_id = d.getGroup_ID();
       position = Arrays.binarySearch( ids, group_id );
       if ( position >= 0 )                               // group_id was found
         if ( d.isSelected() != selected )
         {
           d.setSelected( selected );
           some_changed = true;
         }
     }

     return some_changed;
  }


  /**
   *  Get the selected flag of the specified Data object.
   *
   *  @param   index  The position of the Data object in this DataSet whose
   *                  selection flag is to be returned.
   *  @return  true if the specified Data object is selected, false otherwise.
   */
  public boolean isSelected( int index )
  {
    boolean flag = false;
    if ( index >= 0 && index < data.size() )
      flag = ((Data)data.elementAt(index)).isSelected();
    else
      System.out.println("Error: isSelected called with invalid index");

    return flag;
  }


  /**
   *  Toggle the selected flag of the specified Data object.  If the slected 
   *  flag is initially "true", it will be set "false", and visa-versa.
   *  If the index is not valid, there is no effect.
   *
   *  @param  index     The position of the Data object in this DataSet whose
   *                    selection flag is to be toggled.
   */
  public void toggleSelectFlag( int index )
  {
    if ( index >= 0 && index < data.size() )
      ((Data)data.elementAt(index)).toggleSelected( );
    else
      System.out.println("Error: toggleSelectFlag called with invalid index");
  }


  /**
   *  Get the index of the most recently selected Data object in this DataSet.
   *
   *  @return  the index of the most recently selected Data object in this
   *           DataSet.  If no Data objects are selected, this returns
   *           INVALID_INDEX. 
   */
  public int getMostRecentlySelectedIndex( )
  {
    int  index       = INVALID_INDEX;
    long max_sel_tag =  0;
    Data d           = null;

    for ( int i = 0; i < data.size(); i++ )
    {
      d = (Data)data.elementAt(i);
      if ( d.getSelectionTagValue() > max_sel_tag )
      {
        max_sel_tag = d.getSelectionTagValue();
        index = i;
      }
    }

    return index;
  }


  /**
   *  Get the number of Data objects in the DataSet that have been selected.
   *
   *  @return the number of Data objects currently marked as selected.
   */
  public int getNumSelected() 
  {
    int count = 0;
    for ( int i = 0; i < data.size(); i++ )
      if ( isSelected(i) )
        count++;
    return count;
  }


  /**
   *  Get the list of indices of currently selected Data blocks.
   *
   *  @return  an array of indices of the Data blocks that are currently
   *           selected.  This will return an empty array if none are selected.
   */
  public int[] getSelectedIndices()
  {
    int num = getNumSelected();
 
    if ( num == 0 )
      return new int[0];

    int indices[] = new int[ num ];
    int k = 0;
    for ( int i = 0; i < data.size(); i++ )
      if ( isSelected(i) )
      {
        indices[k] = i;
        k++;
      }

    return indices;
  }


  /**
   *  Set the selected interval for this DataSet.
   *
   *  @param  interval  The ClosedInterval to record as the selected interval
   *                    for this DataSet.
   */
  public void setSelectedInterval( ClosedInterval interval  )
  {
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.setSelectedIntervall() " +
                         "called");
      return;
    }
    selected_interval = interval;
  }


  /**
   *  Get the selected interval for this DataSet.
   *
   *  @return  The ClosedInterval recorded as the selected interval
   *           for this DataSet.  This will be the interval from
   *           [-infinity,infinity] if a more restricted interval is not
   *           specified.
   */
  public ClosedInterval getSelectedInterval()
  {
    return selected_interval;
  }


  /**
   *  Hide all selected Data objects, or all the un-selected Data objects in the
   *  DataSet.
   *
   *  @param  status  Determines whether the currently selected, or currently
   *                  un-selected Data objects are hidden.  If status == true,
   *                  the currently selected Data objects are hidden.  If
   *                  status == false, the currently un-selected Data objects
   *                  are hidden. 
   *
   *  @return This returns true if some Data object's hidden state was set, and
   *  returns false if the call to this method had no effect.
   */
  public boolean hideSelected( boolean status )
  {
    boolean hidden_changed = false;

    for ( int i = 0; i < data.size(); i++ )
      if ( isSelected(i) == status )
      {
        ((Data)data.elementAt(i)).setHide(true);
        hidden_changed = true;
      } 

    return hidden_changed;
  }


  /**
   *  Determine whether the specified Data object is marked as "hidden".
   *
   *  @param  index  The position of the Data object to be checked in the list
   *                 of Data objects for this Data set.
   *  
   *  @return  Returns true if the specified Data object is marked as hidden
   *           and returns false otherwise.
   */
  public boolean isHidden( int index )
  {
    boolean flag = false;
    if ( index >= 0 && index < data.size() )
      flag = ((Data)data.elementAt(index)).isHidden();
    else
      System.out.println("Error: isSelected called with invalid index");

    return flag;
  }


  /**
   *  Clear all "hide" flags for the Data objects in this DataSet. 
   *
   *  @return  true if the state of some "hide" flag was actually changed. 
   */
  public boolean clearHideFlags()
  {
    boolean changed = false;
    Data d;

    for ( int i = 0; i < data.size(); i++ )
    {
      d = (Data)data.elementAt(i);
      if ( d.isHidden() )
      {
        d.setHide( false );
        changed = true;
      }
    } 

    return changed;
  }


  /**
   *  Get the number of Data objects in the DataSet that have been hidden.
   *
   *  @return the number of Data objects currently marked as hidden.
   */
  public int getNumHidden()
  {
    int count = 0;
    for ( int i = 0; i < data.size(); i++ )
      if ( isHidden(i) )
        count++;
    return count;
  }


  /**
   *  Remove all selected Data objects, or all un-selected Data objects from 
   *  the DataSet.
   *
   *  @param  status  Determines whether the currently selected, or currently
   *                  un-selected Data objects are removed.  If status == true,
   *                  the currently selected Data objects are removed.  If
   *                  status == false, the currently un-selected Data objects
   *                  are removed.
   *
   *  @return This returns true if some Data object was removed, and returns  
   *  false if the call to this method had no effect.
   */
  public boolean removeSelected( boolean status )
  {
    int index_to_delete[] = new int[ data.size() ];
    int num_to_delete = 0;
    for ( int i = 0; i < data.size(); i++ )
      if ( isSelected(i) == status )
      {
        index_to_delete[ num_to_delete ] = i;
        num_to_delete++;
      }

    if ( num_to_delete == 0 )
      return false;    
    else if ( num_to_delete == 1 )             // quicker to just delete 1
    {
      data.removeElementAt( index_to_delete[0] );
      return true;
    }
    else
    {
      int list[] = new int[ num_to_delete ];
      System.arraycopy( index_to_delete, 0, list, 0, num_to_delete );
      return removeData_entries( list );
    }
  }

 
  /**
   *  Form a new group consisting either of all the currently selected
   *  Data objects, or of all the un-selected Data objects.
   *
   *  @param  status  Determines whether the currently selected, or currently
   *                  un-selected Data objects are grouped.  If status == true,
   *                  the currently selected Data objects are grouped.  If
   *                  status == false, the currently un-selected Data objects
   *                  are grouped.
   *
   *  @return Return the ID of the new group that was constructed, or 
   *          INVALID_INDEX if there were no Data objects with the specified
   *          selection status.
   */
  public int groupSelected( boolean status )
  {
    int     new_group_ID = getMaxGroupID() + 1;
    boolean group_formed = false;
    Data    d;

    for ( int i = 0; i < data.size(); i++ )
    {
      if ( isSelected(i) == status )
      {
        d = (Data)data.elementAt(i);
        d.setGroup_ID( new_group_ID );
        group_formed = true;
      }
    }
 
    if ( group_formed )
      return new_group_ID;
    else
      return INVALID_INDEX;
  }


  /**
   *  Clear all group_ID information in this DataSet by setting all group IDs
   *  to INVALID_GROUP_ID.
   *
   *  @return  true if the state of some Group_ID was actually changed.
   */
  public boolean clearGroupIDs()
  {
    boolean changed = false;
    Data d;

    for ( int i = 0; i < data.size(); i++ )
    {
      d = (Data)data.elementAt(i);
      if ( d.getGroup_ID() != INVALID_GROUP_ID )
      {
        d.setGroup_ID( INVALID_GROUP_ID );
        changed = true;
      }
    }

    return changed;
  }


  /**
   *  Get the largest group ID of any group in this Data set.
   *
   *  @return Returns the largest group ID of any Data object in this DataSet,
   *          or INVALID_GROUP_ID if this DataSet is empty, or no groups have
   *          been formed.
   */
  public int getMaxGroupID()
  {
    int    max_group_ID = INVALID_GROUP_ID; 
    int    id;
    Data   d;

    for ( int i = 0; i < data.size(); i++ )
    {
      d = (Data)data.elementAt(i);
      id = d.getGroup_ID(); 
      if ( id > max_group_ID )
        max_group_ID = id;
    }
 
    return max_group_ID;
  } 
  

  /**
   *  Specify the index of a Data object that is to be considered "pointed at"
   *  temporarily by the user.
   *
   *  @param  i    The index to record as "pointed at".  This index must be
   *               be a valid index into the list of Data objects.  If the 
   *               index is invalid, the "pointed at" index is recorded as 
   *               INVALID_INDEX. 
   */
  public void setPointedAtIndex( int i )
  {
    if ( i >= 0 && i < data.size() )
      pointed_at_index = i;
    else
      pointed_at_index = INVALID_INDEX;
  }


  /**
   *  Get the index of a Data object that is to be considered "pointed at"
   *  temporarily by the user. 
   *
   *  @return  The index of the Data object that has been designated as
   *           "pointed at".  If no valid index has been designated, the value
   *           returned is INVALID_INDEX.
   */
  public int getPointedAtIndex( )
  {
    // This check is necessary, since we might have eeleted some elements
    // from the DataSet, after the pointed_at_index was set.
    if ( pointed_at_index < 0  || pointed_at_index >= data.size() )
      pointed_at_index = INVALID_INDEX;
    
    return pointed_at_index;
  }


  /**
   *  Specify the "x" value that is to be considered "pointed at"
   *  temporarily by the user.
   *
   *  @param  x    The "x" value to record as "pointed at".  
   */
  public void setPointedAtX( float x )
  {
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.setPointedAtX() called");
      return;
    }

    pointed_at_x = x;
  }


  /**
   *  Get the "x" value that is to be considered "pointed at"
   *  temporarily by the user.
   *
   *  @return  The "x" value to record as "pointed at".  If this has NOT been
   *           set to a valid value, it will return Float.NaN.  The value
   *           returned may be greater than or less than any x-value in the
   *           domain of the DataSet. 
   */
  public float getPointedAtX( )
  {
    return pointed_at_x;
  }


  /**
   * Sets the title of the DataSet both as an instance variable and as
   * an attribute of the DataSet.
   *
   * @param  title   The String to use for the new title of the DataSet 
   */
  public void setTitle( String title ) 
  { 
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.setTitle() called");
      return;
    }

    this.title = title; 
  }


  /**
   * Get the log information for the DataSet.
   *
   * @return a reference to the operations log of this DataSet.
   */
  public OperationLog getOp_log() { return op_log; }


  /**
   * Set the entire operations log for this DataSet, by copying the contents
   * of the specified operations log.
   *
   * @param op_log  The new operations log to copy and use for this
   *                DataSet. 
   */
  public void setOp_log( OperationLog op_log ) 
  { 
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.setOp_log() called");
      return;
    }

    this.op_log = (OperationLog)op_log.clone(); 
  }


  /**
   * Set the entire operations log for this DataSet by copying the operations
   * log from the specified the DataSet 
   *
   * @param data_set  The DataSet whose operations log is copied.
   */
  public void copyOp_log( DataSet data_set )
  { 
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.copyOp_log() called");
      return;
    }

    this.op_log = (OperationLog)data_set.getOp_log().clone();
  }


  /**
   * Adds a new entry to the log information for the DataSet 
   *
   * @param log_info   The String to be added to the log information for
   *                   this DataSet.
   */
  public void addLog_entry( String log_info )
  {
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.addLog_entry() called");
      return;
    }

    this.op_log.addEntry( log_info );
  }


  /**
   * Get the number of Data objects in the DataSet.
   *
   * @return an int giving the number of Data objects in this DataSet.
   */
  public int getNum_entries() { return data.size(); }


  /**
   * Get a reference to the first data object in this DataSet with the 
   * specified group ID.  
   *
   * @param  group_id      The group id of the requested Data object in the 
   *                       list of Data objects in this DataSet.
   *
   * @return A reference to the first Data object in this DataSet with the 
   * specified group ID.  If there is no data object with the correct ID, 
   * this will return null.
   */
  public Data getData_entry_with_id( int group_id )
  {
     for ( int i = 0; i < data.size(); i++ )
       if ( ((Data)data.elementAt( i )).getGroup_ID() == group_id )
         return (Data)data.elementAt( i );

     return null;     // if we didn't find the right id
  }


  /**
   * Get a reference to the Data object in the specified position in the 
   * DataSet.
   *
   * @param  index   The index of the requested Data object in the list of
   *                 Data objects in this DataSet. 
   *
   * @return A reference to the Data object at the specified index in this 
   * DataSet if the index is valid, otherwise return null.
   */
  public Data getData_entry( int index ) 
  {
     if ( index >= 0 && index < data.size() )
       return (Data)data.elementAt( index ); 
     else
       return null;
  }


  /**
   * Find the position of a Data block in this DataSet. 
   *
   * @param  d   The Data block to find in the DataSet 
   *
   * @return The index at which the Data block occurs in the DataSet, if
   *         it is present in the DataSet, returns -1 otherwise.
   */
  public int getIndex_of_data( Data d )
  {
     if ( d == null )
       return INVALID_INDEX;

     for ( int index = 0; index < data.size(); index++ )
       if ( d == (Data)data.elementAt( index ) )
         return index;
     
     return INVALID_INDEX;
  }


  /**
   * Find the position of the first Data block in this DataSet that
   * has the specified Group ID.
   *
   * @param  group_id      The group id of the requested Data object
   *
   * @return The index at which the Data block occurs in the DataSet, if
   *         it is present in the DataSet, returns -1 otherwise.
   */
  public int getIndex_of_data_with_id( int group_id )
  {
     for ( int i = 0; i < data.size(); i++ )
       if ( ((Data)data.elementAt( i )).getGroup_ID() == group_id )
         return i;

     return INVALID_INDEX;     // if we didn't find the right id
  }


  /**
   * Adds a new Data object to the end of the list of Data objects.
   *
   * @param  entry   The Data object to be added to the list of Data objects
   *                 in this DataSet.
   */
  public void addData_entry( Data entry )
  {
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.addData_entry() called");
      return;
    }

    data.addElement( entry );
  }


  /**
   * Inserts a new Data object at the spcecified position in 
   * the list of Data objects.  If the specified position does not exist,
   * it is added to the end of the list.
   *
   * @param  entry   The Data object to be inserted in the list of Data objects
   *                 in this DataSet.
   *
   * @param  index   The position where the Data object is to be inserted.
   */
  public void insertData_entry( Data entry, int index )
  {
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.insertData_entry() called");
      return;
    }

    if ( index >= 0 && index < data.size() )
      data.insertElementAt( entry, index );
    else
      data.addElement( entry );
  }


  /**
   * Replaces the Data object at the spcecified position in the list of Data 
   * objects with the specified Data entry.  If the specified position does 
   * not exist, this method has no effect on the DataSet and returns false.
   *
   * @param  entry   The Data object to be inserted in the list of Data objects
   *                 in this DataSet.
   *
   * @param  index   The position where the Data object is to be replaced.
   *
   * @return  Returns true if the index was a valid index in the list of
   *          Data objects and the specified entry replaced the Data object
   *          in that position.  Returns false if the index was not valid.
   */
  public boolean replaceData_entry( Data entry, int index )
  {
    if ( index >= 0 && index < data.size() )
    {
      data.setElementAt( entry, index );
      return true;
    }
    else
      return false;
  }


  /**
   * Replaces the Data object at the spcecified position in the list of Data 
   * objects with the specified Data entry.  If the specified datablock does 
   * not exist, this method adds it new.
   *
   * @param  entry    The Data object to be inserted in the list of Data
   *                  objects in this DataSet.
   *
   * @param  group_id The position where the Data object is to be replaced.
   *
   * @return  Returns true if sucessful.
   */
  public boolean replaceData_entry_with_id( Data entry, int group_id )
  {
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.replaceData_entry_with_id()"+
                         " called");
      return false;
    }

    int index = -1;
    for( int i=0 ; i<this.getNum_entries() ; i++ )
      if( group_id == this.getData_entry(i).getGroup_ID() )
      {
        index = i;
        i     = this.getNum_entries();
      }
    
    if( index == -1 )
      this.addData_entry( entry );
    else
      this.replaceData_entry( entry, index );

    return true;
  }


  /**
   * Removes the data entry from the specified position in the DataSet.
   *
   * @param  index  The index of the Data object to be removed from the list
   *                of Data objects in this DataSet.
   *
   * @return  true if some Data block was removed.
   */
  public boolean removeData_entry( int index )
  {
    if ( index >= 0 && index < data.size() )
    {
      data.removeElementAt( index );
      return true;
    }
    else
      return false;
  }


  /**
   * Removes all data objects in this DataSet, that are initially at any
   * of the specified indices.  If there are no valid indices in the list
   * this method has no effect.
   *
   * NOTE: It is the responsibility of the calling code to notify 
   *       observers of the DataSet that Data has been deleted.  
   *       This method is more efficient than repeated calls to 
   *       removeData_entry() if many Data blocks are to be removed.
   *
   * @param  list   The list of indices to be removed.
   *
   * @return  true if some Data block was removed. 
   */
  public boolean removeData_entries( int list[] )
  {
                                            // first make a copy of the indices 
                                            // so we can sort and search
     int my_list[] = new int[ list.length ];
     System.arraycopy( list, 0, my_list, 0, list.length );
     Arrays.sort( my_list );
     Vector new_data = new Vector( data.size() - my_list.length );

     boolean  some_removed = false;
     int      position;
     for ( int i = 0; i < data.size(); i++ )  // copy over everything not listed
     {
       position = Arrays.binarySearch( my_list, i );
       if ( position < 0 )
         new_data.addElement( data.elementAt(i) );
       else
         some_removed = true;
     }

     if ( some_removed )    // use the new list and trim it, in case there
     {                      // were duplicate indices.
       data = new_data;
       data.trimToSize();
     }

     return some_removed;
  }


  /**
   * Removes the first data object in this DataSet with the specified group id.
   * If there is no data object with the correct ID, this has no effect.
   *
   * @param  group_id      The group_id of the requested Data object in the 
   *                       list of Data objects in this DataSet.
   *
   * @return  true if some Data block was removed.
   */
  public boolean removeData_entry_with_id( int group_id )
  {
     for ( int i = 0; i < data.size(); i++ )
       if ( ((Data)data.elementAt( i )).getGroup_ID() == group_id )
       {
         data.removeElementAt(i);           // found the id, so remove the
         return true;                       // the data object and exit
       }
     return false; 
  }


  /**
   * Removes all data objects in this DataSet, whose group ID is in the 
   * the specified list of group ids.  If there is no data object with 
   * any of the specified IDs, this has no effect.  NOTE: It is the
   * responsibility of the calling code to notify observers of the DataSet
   * that Data has been deleted.  This method is more efficient than
   * repeated calls to removeData_entry_with_id() if many Data blocks are
   * to be removed.
   *
   * @param  group_ids   The list of group_ids to be removed.
   *
   * @return  true if some Data block was removed. 
   */
  public boolean removeData_entries_with_ids( int group_ids[] )
  {
                                               // first make a copy of the ids
                                               // so we can sort and search
     int ids[] = new int[ group_ids.length ];
     System.arraycopy( group_ids, 0, ids, 0, group_ids.length );
     Arrays.sort( ids );
     Vector new_data = new Vector( data.size() - ids.length ); 

     boolean  some_removed = false;
     int      group_id;
     int      position; 
     for ( int i = 0; i < data.size(); i++ )  // copy over everything not listed
     {
       group_id = ((Data)data.elementAt(i)).getGroup_ID();
       position = Arrays.binarySearch( ids, group_id );
       if ( position < 0 )
         new_data.addElement( data.elementAt(i) );
       else
         some_removed = true;
     }

     if ( some_removed )    // use the new list and trim it, in case there
     {                      // were duplicate group_IDs.
       data = new_data;
       data.trimToSize();
     }

     return some_removed;
  }


  /**
   * Removes all Data objects from the vector of Data objects for this DataSet.
   *
   */
  public void removeAll_data_entries(  )
  {
     data.removeAllElements();
  }


  /**
   * Returns the number of operators for the DataSet
   */
  public int getNum_operators() { return operators.size(); }


  /**
   * Returns a reference to the specified operation in the list of available
   * operators on this DataSet.
   *
   * @param  index   The index of the requested operation in the list of
   *                 operators in this DataSet.
   */
  public DataSetOperator getOperator( int index )
  {
     if ( index >= 0 && index < operators.size() )
       return (DataSetOperator)operators.elementAt( index );
     else
       return null;
  }


  /**
   * Returns a reference to the operation in the list of available operators 
   * on this DataSet with the specified TITLE.  If the named operator is not
   * in the list, this method returns null.
   *
   * @param  title   The title of the requested operation 
   */
  public DataSetOperator getOperator( String title )
  {
     int     num_ops = getNum_operators();
     int     i       = 0;

     DataSetOperator op;
     while ( i < num_ops )
     {
       op = (DataSetOperator)operators.elementAt( i );
       if ( title.equalsIgnoreCase( op.getTitle() ) )
         return op;
       else
         i++;
     }
    
     return null;
  }


  /**
   * Adds a new operator to the list of operators for this DataSet.
   *
   * @param  operator    The operation to be added to the list of operations
   *                     in this DataSet.
   */
  public void addOperator( DataSetOperator operator )
  {
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.addOperator() called");
      return;
    }

    removeOperator( operator );        // first remove any existing operator
                                       // of this type, then add in the new
    operator.setDataSet( this );       // operator that was passed in
    operators.addElement( operator );
    operator.setDefaultParameters();
  }


  /**
   * Removes all instances of an operator from this DataSet that have same 
   * class as the specified operator. 
   *
   * @param  op  An instance of an operator of the type that is to be removed
   *             from this DataSet.
   */
  public void removeOperator( Operator op )
  {
    for ( int i = operators.size()-1; i >= 0; i-- )
    { 
      if ( operators.elementAt(i).getClass().equals( op.getClass()) )
        operators.removeElementAt(i); 
    }
  }


  /**
   * Remove all operators from this DataSet.
   */
  public void removeAllOperators()
  {
    operators.clear();
  }


  /**
   * Get the range of X values for the collection of Data objects in this
   * DataSet.
   *
   * @return a UniformXScale spanning the full range of all XScales in this
   *         DataSet, with number of points equal to the largest number of
   *         points in any XScale for this DataSet.
   */
  public UniformXScale getXRange()
  {
    if ( this.getNum_entries() < 1 )
      return null;
    else
    {
      XScale range = getData_entry( 0 ).getX_scale();
      float  min = range.getStart_x();
      float  max = range.getEnd_x();
      int    num = range.getNum_x();
      for ( int i = 1; i < this.getNum_entries(); i++ )
      {
        range = getData_entry( i ).getX_scale();
        min = Math.min( min, range.getStart_x() );
        max = Math.max( max, range.getEnd_x() );
        num = Math.max( num, range.getNum_x() );
      }
      return new UniformXScale( min, max, num ); 
    }
  }


  /**
   * Get the range of Y values for the collection of Data objects in this
   * DataSet.
   *
   * @return  The ClosedInterval determined by the min and max of all
   *          "Y" values in this DataSet.
   */
  public ClosedInterval getYRange()
  {
    Data           data_block;
    ClosedInterval range;
    float          min_y, max_y, min, max;

    if ( this.getNum_entries() < 1 )
      return null;
    else
    {
      data_block = (Data)getData_entry( 0 );
      min = arrayUtil.getMin( data_block.getY_values() );
      max = arrayUtil.getMax( data_block.getY_values() );

      for ( int i = 0; i < this.getNum_entries(); i++ )
      {
        data_block = (Data)getData_entry( i );
        min_y = arrayUtil.getMin( data_block.getY_values() );
        if ( min_y < min )
          min = min_y;

        max_y = arrayUtil.getMax( data_block.getY_values() );
        if ( max_y > max )
          max = max_y;
      }
      range = new ClosedInterval( min, max );

      return range;
    }
  }


  /**
   * Get the maximum number of X steps for the Data objects in this DataSet.
   *
   *  @return   An integer giving the largest number of X steps for any 
   *            Data object in this DataSet
   */
  public int getMaxXSteps()
  {
    Data           data_block;
    XScale         x_scale;
    int            num_steps;
    int            max_steps = 0;
    int            num_entries = getNum_entries();

    for (int i = 0; i < num_entries; i++ )
    {
      data_block = (Data)getData_entry( i );
      x_scale    = data_block.getX_scale();
      num_steps  = x_scale.getNum_x();

      if ( num_steps > max_steps )
        max_steps = num_steps;
    }

    return max_steps;
  }


  /**
   * Returns true if the current DataSet has the same x and y units as the
   * specified DataSet. 
   *
   * @param  ds  the DataSet whose units are compared to those of the current
   *             DataSet
   *
   * @return  returns true if the x and y units of the DataSets match, 
   *          retruns false otherwise
   */
  public boolean SameUnits( DataSet ds ) 
  { 
     if ( x_units.equalsIgnoreCase( ds.x_units )  &&
          y_units.equalsIgnoreCase( ds.y_units )     )
       return true;

     else
       return false;
  }


  /**
   * Returns the units for the "X" axis 
   *
   * @return the String specifying the units along the X-axis for this
   *         DataSet
   */
  public String getX_units() 
  { 
    return x_units; 
  }


  /**
   * Sets the units for the "X" axis 
   *
   * @param  units   String giving the units for the "X" axis
   */
  public void setX_units( String units )
  { 
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.setX_units() called");
      return;
    }

    this.x_units = units; 
  }


  /**
   * Returns the label for the "X" axis 
   *
   * @return the String specifying the label for the X-axis for this
   *         DataSet
   */
  public String getX_label() 
  { 
    return x_label; 
  }


  /**
   * Sets the label for the "X" axis 
   *
   * @param  label  String giving the label for the "X" axis
   */
  public void setX_label( String label ) 
  { 
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.setX_label() called");
      return;
    }

    this.x_label = label; 
  }


  /**
   * Returns the units for the "Y" scale 
   *
   * @return the String specifying the units along the Y-axis for this
   *         DataSet
   */
  public String getY_units() 
  { 
    return y_units; 
  }


  /**
   * Sets the units for the "Y" scale 
   *
   * @param  units   String giving the units for the "Y" axis
   */
  public void setY_units( String units ) 
  { 
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.setY_units() called");
      return;
    }

     this.y_units = units; 
  }


  /**
   * Returns the label for the "Y" axis 
   *
   * @return the String specifying the label for the X-axis for this
   *         DataSet
   */
  public String getY_label() 
  {
    return y_label; 
  }


  /**
   * Sets the label for the "Y" axis 
   *
   * @param  label  String giving the label for the "Y" axis
   */
  public void setY_label( String label ) 
  { 
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.setY_label() called");
      return;
    }

    this.y_label = label; 
  }

 
  /** 
   *  Set the label field on all Data blocks in this DataSet to the specified
   *  name.
   *
   *  @param name  String to use for the label field.
   *
   */
  public void setData_label( String name )
  {
    int n = data.size();
    for ( int i = 0; i < n; i++ )   
      ((Data)data.elementAt(i)).setLabel( name );
  }


  /**
   *  Get a reference to the list of attributes for this DataSet object.
   *
   *  @return return the AttributeList for this DataSet 
   */
  public AttributeList getAttributeList()
  {
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.getAttributeList() called");
      return new AttributeList();
    }

    return attr_list;
  }


  /**
   *  Set the list of attributes for this DataSet object to be a COPY of the 
   *  specified list of attributes.
   *
   *  @param  attr_list  The list of attributes to be copied
   */
  public void setAttributeList( AttributeList attr_list )
  {
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.setAttributeList() called");
      return;
    }

    if ( !attr_list.equals( this.attr_list ))             // only clone if it's
      this.attr_list = (AttributeList)attr_list.clone();  // a new attr_list

    setTitle( this.title );   // force the attribute list to contain the
                              // correct title
  }


  /**
   * Gets the number of attributes set for this DataSet.
   *
   * @return  The number of attributes in the attribute list of this DataSet.
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
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.setAttribute() called");
      return;
    }

    attr_list.setAttribute( attribute );
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
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.setAttribute(,) called");
      return;
    }

    attr_list.setAttribute( attribute, index );
  }


  /**
   * Get the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   * 
   * @param  index  The position of the attribute in the list of attributes.
   *
   * @return  A reference to the Attribute object at the specified index.
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
   * @return  A reference to the Attribute object with the specified name.
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
   * @return  A reference to the Object that is the value of the 
   *          Attribute at the specified index.
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
   * @return  A reference to the Object that is the value of the 
   *          Attribute with the specified name.
   */
  public Object getAttributeValue( String name )
  {
    return attr_list.getAttributeValue( name );
  }


  /**
   *  Sort the list of Data entries based on the specified attribute.  At this
   *  time Java's sort method will be used regardless of the sort_type 
   *  specified.  Future releases may implement other sort options.
   *  NOTE: Java' sort is a stable sort.
   *
   *  @param  attr_name   The name of the attribute on which to sort.  The
   *                      attr_name parameter must be the name of an attribute
   *                      that is stored with every Data entry in this data
   *                      set.
   *  @param  increasing  Flag indicating whether to sort the list in 
   *                      increasing or decreasing order.
   *  @param  sort_type   Flag specifying the sort type, either
   *                      DataSet.Q_SORT or DataSet.JAVA_SORT.  Only JAVA_SORT
   *                      is currently implemented.
   *
   *  @return    This returns true if the DataSet entries were sorted and 
   *             returns false otherwise.
   */
  public boolean Sort( String attr_name, boolean increasing, int sort_type )
  {
    if ( data.size() <= 1 )      // empty or short list is sorted by default
      return true;

    return JavaSort( attr_name, increasing );     // just use java's sort
  }


  /**
   *  Sort the list of Data entries based on the specified attribute using
   *  the sort method provided by Java. 
   *
   *  @param  attr_name   The name of the attribute on which to sort.  The
   *                      attr_name parameter must be the name of an attribute
   *                      that is stored with every Data entry in this data
   *                      set.
   *  @param  increasing  Flag indicating whether to sort the list in
   *                      increasing or decreasing order.
   *
   *  @return    This returns true if the DataSet entries were sorted and
   *             returns false otherwise.
   */
  public boolean JavaSort( String attr_name, boolean increasing )
  {
    int n = data.size();
                                                  // make sure all data blocks
    for ( int i = 0; i < n; i++ )                 // have the needed attribute
      if ( getData_entry(i).getAttribute( attr_name ) == null )
        return false; 

    Data list[] = new Data[n];
    for ( int i = 0; i < n; i++ )
      list[i] = getData_entry(i);

    Arrays.sort( list, new DataComparator( attr_name ) );

                                                  // copy the data objects to
    Vector new_data = new Vector(n);              // a new vector in the right
                                                  // order
    if ( increasing )
      for ( int i = 0; i < n; i++ )                
        new_data.addElement( list[i] );
    else
      for ( int i = 0; i < n; i++ )
        new_data.addElement( list[n-1-i] );

    data = new_data;

    last_sort_attribute = attr_name;
    setData_label( attr_name );

    return true;
  }


  /**
   *  Get the name of the last attribute that was used to sort this DataSet.
   *
   *  @return The name of the last attribute used for sorting. 
   */
  public String getLastSortAttribute()
  {
    return last_sort_attribute;
  }


  /**
   * Combine the attribute list of the specified DataSet with the attribute
   * list of the current DataSet to obtain a new attribute list for the
   * current DataSet.
   *
   *  @param  ds    The DataSet whose attribute list is to be combined
   *                with the current object's attribute list
   */
   public void CombineAttributeList( DataSet ds )
   {
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.CombineAttributeList() "+
                         "called");
      return;
    }

     attr_list.combine( ds.getAttributeList() );
   }


  /**
   *  Copy the contents of another DataSet into the current DataSet.  This 
   *  performs a shallow copy and just copies references to the Data 
   *  blocks, Attribute list, etc from the specified DataSet into the 
   *  current DataSet.   This is a dangerous process and should generally
   *  NOT be used.  However in special cases, like the LiveDataManager, it
   *  can be safely used and improves the efficiency for large DataSets.
   *  The list of observers is unchanged and the observers of this DataSet 
   *  are notified that the data was changed.  The numeric DataSet
   *  tag of this DataSet is also unchanged.
   *
   *  @param  ds   The DataSet whose contents are to copied into this DataSet.
   */
  public void shallowCopy( DataSet ds )
  {
    if ( ds == null )
      return;

    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.shallowCopy() called");
      return;
    }

    if ( this.equals( ds ) )
    {
      this.notifyIObservers( IObserver.DATA_CHANGED );  //TODO: Is this needed?
      return;
    }

    this.title               = ds.title;
    this.x_units             = ds.x_units;
    this.x_label             = ds.x_label;
    this.y_units             = ds.y_units;
    this.y_label             = ds.y_label;

                                         // Note: we are NOT copying the ds_tag
    this.pointed_at_index    = ds.pointed_at_index;
    this.pointed_at_x        = ds.pointed_at_x;
    this.selected_interval   = ds.selected_interval;
    this.last_sort_attribute = ds.last_sort_attribute;

    this.setAttributeList( ds.getAttributeList() );

    this.data = new Vector(); 
    int num_entries = ds.getNum_entries();

    for ( int i = 0; i < num_entries; i++ )
      this.addData_entry( ds.getData_entry( i ) );

    this.operators = new Vector();
    int num_ops = ds.getNum_operators(); 
    for ( int i = 0; i < num_ops; i++ )
      this.addOperator( ds.getOperator(i) );

    this.op_log = ds.op_log;
                                       // NOTE: We don't change the list of
                                       //       observers, but rather notify
                                       //       the observers of this DataSet
                                       //       that it's contents changed.                              
    this.notifyIObservers( IObserver.DATA_CHANGED );
  }


  /**
   *  Copy the contents of another DataSet into the current DataSet.  This
   *  performs a complete "deep copy" EXCEPT for the list of observers and the
   *  DataSet tag.  The list of observers is unchanged and the observers of
   *  this DataSet are notified that the data was changed.  The numeric DataSet
   *  tag of this DataSet is also unchanged.
   *
   *  @param  ds   The DataSet whose contents are to copied into this DataSet.
   */
  public void copy( DataSet ds )
  {
    if ( this.equals( ds ) )
    {
      this.notifyIObservers( IObserver.DATA_CHANGED );  // TODO: Is this needed
      return;
    }

    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.copy() called");
      return;
    }

    this.title               = ds.title;
    this.x_units             = ds.x_units;
    this.x_label             = ds.x_label;
    this.y_units             = ds.y_units;
    this.y_label             = ds.y_label;

                                         // Note: we are NOT copying the ds_tag
    this.pointed_at_index    = ds.pointed_at_index;
    this.pointed_at_x        = ds.pointed_at_x;
    this.selected_interval   = ds.selected_interval;
    this.last_sort_attribute = ds.last_sort_attribute;

    this.setAttributeList( ds.getAttributeList() );
    
    this.data = new Vector();
    int num_entries = ds.getNum_entries();

    for ( int i = 0; i < num_entries; i++ )
    {
      Data d = ds.getData_entry( i );
      this.addData_entry( (Data)(d.clone()) );
    }

    CloneGridInfo( this );
    
    this.operators = new Vector();
    int num_ops = ds.getNum_operators();
    for ( int i = 0; i < num_ops; i++ )
    {
      DataSetOperator op = (DataSetOperator)ds.getOperator(i).clone();
      this.addOperator( op );
    }

  
    this.op_log = (OperationLog)ds.op_log.clone();
                                       // NOTE: We don't change the list of
                                       //       observers, but rather notify
                                       //       the observers of this DataSet
                                       //       that it's contents changed.       
    this.notifyIObservers( IObserver.DATA_CHANGED );
  }
  
 //The grids in the PixelInfoAttribute are the grids with data from 
  // another data set. Using these pointers to get and set data causes
  // problems if one ore both data are changed.
  // Note: PixelInfoListAttribute is immutable.
  private void CloneGridInfo( DataSet ds){
     
     Hashtable ids = Grid_util.getAllDataGrids( ds );
     if( ids == null || ids.size() < 1)
        return;
     Enumeration En = ids.elements();
     if( En.hasMoreElements())
        for( ;En.hasMoreElements(); ){
           
           IDataGrid grid=(IDataGrid)En.nextElement();
           if( grid != null){
              
              IDataGrid grid_new = grid.clone();
              grid_new.clearData_entries();
              grid_new.setData_entries( ds );
              
              for( int row=1 ; row <= grid.num_rows() ; row++ )
                 for( int col = 1 ; col<=grid.num_cols(); col++ ){
                    
                    Data D = grid_new.getData_entry( row , col );
                    if( D != null ){
                       
                       PixelInfoList plist = AttrUtil.getPixelInfoList( D );
                       IPixelInfo[] pixInfo = new IPixelInfo[plist.num_pixels()];
                       if( plist != null ){
                          for( int i=0; i< plist.num_pixels(); i++){
                       
                          
                             DetectorPixelInfo pinf =
                                       (DetectorPixelInfo) plist.pixel( i);
                             if( pinf.gridID() == grid_new.ID()){
                                 pinf = new DetectorPixelInfo( pinf.ID(), 
                                          (short)pinf.row(),(short)pinf.col(),
                                          grid_new);
                                 pixInfo[i] = pinf;
                           
                            }else
                                pixInfo[i] = plist.pixel( i );
                         
                         
                          }  
                          D.setAttribute(new PixelInfoListAttribute( 
                                           Attribute.PIXEL_INFO_LIST,
                                           new PixelInfoList(pixInfo)));
                       }
                    }
                 }//next col/row
              
             
           }//if grid != null
        }
  }

  
  /**
   * Clone the current DataSet, including the operation log, the list of
   * operators and the list of individual Data objects.
   *
   * @return A new DataSet that is a clone of the current DataSet.
   */
  public Object clone()
  {
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.clone() called");
      return this;
    }
    
    DataSet new_ds = empty_clone();
                                      // now copy the list of Data objects.
    Data d;
    int num_entries = getNum_entries();
    for ( int i = 0; i < num_entries; i++ )
    {
      d = getData_entry( i );
      if ( d == null )
        System.out.println("ERROR: null data block " + i +" in Data.clone()");
      else
        new_ds.addData_entry( (Data)d.clone() );
    }
    
    CloneGridInfo( new_ds);
    
    return new_ds;
  }


  /**
   * Clone an EMPTY DataSet with the same title, units, label, operation log,
   * and operators as the original DataSet.
   *
   * @return A new DataSet that is a clone of the current DataSet, except
   *         for the Data blocks, and the observers.
   */
  public DataSet empty_clone()
  {
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.empty_clone() called");
      return this;
    }

    DataSet new_ds = new DataSet( getTitle(),           // get a new DataSet 
                                  (OperationLog) getOp_log().clone(),
                                  getX_units(),
                                  getX_label(),
                                  getY_units(),
                                  getY_label() );

                                      // copy the list of operations.
    DataSetOperator op;
    int num_ops = getNum_operators();
    for ( int i = 0; i < num_ops; i++ )
    {
      op = (DataSetOperator)getOperator(i).clone();
      new_ds.addOperator( op );
    }
                                      // copy the list of attributes.
    AttributeList attr_list = getAttributeList();
    new_ds.setAttributeList( attr_list );

    new_ds.pointed_at_index    = pointed_at_index;
    new_ds.selected_interval    = selected_interval;
    new_ds.last_sort_attribute = last_sort_attribute;

    return new_ds;
  }

 
 /**
  *  Set flag to indicate whether this DataSet will be written as a complete
  *  "stand alone" XML file, or will be written as part of a larger XML file
  *  when the XMLwrite method is called. 
  *
  *  @param standalone  True means the DataSet will be written as a complete
  *                     XML file.
  */
  public void setStandAlone( boolean standalone)
  {
     xmlStandAlone = standalone;
  }


 /**
  * Implements the IXmlIO interface.  This routine "writes" the
  * dataset. In standalone mode it writes the xml header.
  *
  * @param  stream  the OutputStream to which the xml data is to be written
  * @param  mode    Either IXmlIO.Base64 to write spectra information 
  *                 efficiently or IXmlIO.Normal to produce ASCII values
  *
  * @return  true if successful otherwise false
  *
  * @see  #DataSet( boolean) Constructor
  *
  */
  public boolean XMLwrite( OutputStream stream, int mode )
  { 
    StringBuffer SS = new StringBuffer(200);
    if( xmlStandAlone)
    { SS.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
     
    }
    SS.append("<DataSet ");
    SS.append(" version=\"0.0.0.0.1\" ");
    SS.append( "TITLE=\"");
    SS.append( title);
    SS.append("\"  ");
    SS.append("X_UNITS=\"");
    SS.append( x_units); SS.append("\"\n      ");
    SS.append("X_LABEL=\"");
    SS.append( x_label); SS.append("\"\n      ");
    SS.append("Y_UNITS=\"");  SS.append( y_units);
    SS.append("\"     ");
    SS.append( "Y_LABEL=\""); SS.append(y_label);
    SS.append("\">\n\n");
    try
    {
      stream.write( SS.substring( 0 ).getBytes());
     
      boolean b=attr_list.XMLwrite( stream, mode);
    
      if(!b)
        return false;
     
      OperatorList ol = new OperatorList( this );
      if(!ol.XMLwrite( stream, mode))
        return false;
      stream.write("\n".getBytes());
      if(!op_log.XMLwrite( stream, mode))
        return false;
      stream.write("\n".getBytes());

      DataSetList dsl =new DataSetList( this );
      if(!dsl.XMLwrite( stream, mode ))
        return false;
      stream.write("\n".getBytes());

      stream.write("</DataSet>\n".getBytes());
    }
    catch( Exception s)
    { return xml_utils.setError( "DataSetWrite err="+s.getClass() +" "+
                 s.getMessage());
    }
    
    return true;
  }


 /**
  * Implements the IXmlIO interface.  This routine "reads" the
  * dataset. In standalone mode it reads past the xml header. All reads 
  * assume the starting tag ( here DataSet) has already been consumed
  *
  * @param  stream  the InputStream from which the xml data is read
  *
  * @return true if succesful otherwise false
  *
  * @see #DataSet( boolean)  standalone mode
  */
  public boolean XMLread( InputStream stream )
  { 
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.XMLread() called");
      return false;
    }

    // String to ranslate internal to external names for fields
    String XLateDSFields=":TITLE;"+
                               DSFieldString.TITLE+":"+
                              "X_UNITS;"+
                               DSFieldString.X_UNITS+":"+
                              "X_LABEL;"+
                               DSFieldString.X_LABEL+":"+
                              "Y_LABEL;"+
                               DSFieldString.Y_LABEL+":"+
                              "Y_UNITS;"+
                               DSFieldString.Y_UNITS+":";

    String Tag;
    if( xmlStandAlone)
    { Tag = xml_utils.getTag( stream);
      if( Tag ==  null)
        return xml_utils.setError( xml_utils.getErrorMessage());
    
      if(Tag.equals("?xml"))
      { if(!xml_utils.skipAttributes( stream))
          return xml_utils.setError( xml_utils.getErrorMessage());
        Tag = xml_utils.getTag( stream);
        if( Tag ==  null)
          return xml_utils.setError( xml_utils.getErrorMessage()); 
       }      
      
      if(!Tag.equals("DataSet"))
        return xml_utils.setError("Improper start tag. Should be DataSet" +Tag);
    }


    Vector V = xml_utils.getNextAttribute( stream );
    
    if( V == null) 
      return xml_utils.setError( xml_utils.getErrorMessage());
    boolean done = V.size()<2;
     
    while( !done)
    { String S = (String)(V.firstElement());
      int i= XLateDSFields.indexOf(":"+S+";");
      
      if( i >= 0)
      { S = XLateDSFields.substring( i+S.length()+2,
                      XLateDSFields.indexOf(":", i+S.length()+2));   
                    
        DataSetTools.operator.DataSet.Attribute.SetField sf= 
                (new DataSetTools.operator.DataSet.Attribute.SetField(
                this, new DSSettableFieldString(S),
               V.lastElement()));
        sf .getResult(); 
      }
      V = xml_utils.getNextAttribute( stream );
      if( V == null)
        return  xml_utils.setError( xml_utils.getErrorMessage());
      done = V.size()<2;
    }
    
    if( !xml_utils.skipAttributes( stream ))
      return xml_utils.setError( xml_utils.getErrorMessage());

    Tag = xml_utils.getTag( stream);
    if( Tag ==  null)
      return xml_utils.setError( xml_utils.getErrorMessage());
    if(!Tag.equals("AttributeList"))
      return xml_utils.setError("Improper start tag. Should be AttributeList:"
                          +Tag);
    if( !xml_utils.skipAttributes( stream ))
      return xml_utils.setError( xml_utils.getErrorMessage());
    attr_list= new AttributeList();
    if(!attr_list.XMLread( stream))
      return false;
      
    
    Tag = xml_utils.getTag( stream);
    if( Tag ==  null)
      return xml_utils.setError( xml_utils.getErrorMessage());
    if(!Tag.equals("OperatorList"))
      return xml_utils.setError("Improper start tag. Should be DataSet:"+
                                                      Tag);
    if( !xml_utils.skipAttributes( stream ))
      return xml_utils.setError( xml_utils.getErrorMessage());
    OperatorList ol = new OperatorList( this);
    if(!ol.XMLread( stream))
      return false;
     
    Tag = xml_utils.getTag( stream);
    if( Tag ==  null)
      return xml_utils.setError( xml_utils.getErrorMessage());
    if(!Tag.equals("OperationLog"))
      return xml_utils.setError("Improper start tag. Should be DataSet:"
                             +Tag);
    if( !xml_utils.skipAttributes( stream ))
      return xml_utils.setError( xml_utils.getErrorMessage());
    
    if(!op_log.XMLread( stream))
      return false;
     
    Tag = xml_utils.getTag( stream);
    if( Tag ==  null)
      return xml_utils.setError( xml_utils.getErrorMessage());
    if(!Tag.equals("DataList"))
      return xml_utils.setError("Improper start tag. Should be DataList:"
             +Tag);
    if( !xml_utils.skipAttributes( stream ))
      return xml_utils.setError( xml_utils.getErrorMessage());
    DataSetList dsl = new DataSetList( this);
    if(!dsl.XMLread( stream) )
      return false;
    Vector grids = new Vector();
    for( int i = 0 ; i < this.getNum_entries() ; i++ ){
    	Data db = this.getData_entry( i );
    	PixelInfoList pixInfo = 
             (PixelInfoList) db.getAttributeValue( Attribute.PIXEL_INFO_LIST );
    	if( ( pixInfo != null ) && ( pixInfo.num_pixels() > 0 )){
    		IDataGrid grid = pixInfo.pixel(0).DataGrid();
    		if( !grids.contains( grid ) )
    			grids.addElement( grid );	
    	}
    }
    
    for( int i = 0 ; i < grids.size() ; i++ )
    	((IDataGrid)( grids.elementAt( i ) ) ).setData_entries( this );
    return true;
  }


  /**
   *  Provide an identifier string for this DataSet.
   *
   *  @return A String consisting of the DataSet's tag followed by its
   *          title.
   */
  public String toString()
  {
     return ""+ds_tag+":"+title;
  }
  

/* -----------------------------------------------------------------------
 *
 *  PROTECTED METHODS
 *
 */

  /*
   *  Trace the finalization of objects
   */
/*
  protected void finalize() throws IOException
  {
    System.out.println( "finalize DataSet" );
  }
*/


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
 *
 *  @param s  The ObjectInputStream from which the DataSet is read.
 */
  private void readObject( ObjectInputStream s ) throws IOException, 
                                                        ClassNotFoundException 
  {
    if ( this == EMPTY_DATA_SET )
    {
      System.out.println("WARNING: EMPTY_DATA_SET.readObject() called");
      return;
    }


    s.defaultReadObject();               // read basic information

    if ( IsawSerialVersion != 1 )
      System.out.println("Warning:DataSet IsawSerialVersion != 1");

    ds_tag    = current_ds_tag++;        // fill out default value for transient
    observers = new IObserverList();     // fields
    pointed_at_index  = INVALID_INDEX;
    pointed_at_x      = Float.NaN;
    selected_interval = new ClosedInterval( Float.NEGATIVE_INFINITY,
                                            Float.POSITIVE_INFINITY );
    last_sort_attribute = "";
    xmlStandAlone       = true;

    operators = new Vector();
    DataSetFactory.addOperators( this ); // Add the basic operators then     
                                         // try to reconstruct operator list
                                         // using first instrument type, if 
                                         // all needed attributes are present
    Object types_obj = attr_list.getAttributeValue(Attribute.INST_TYPE);
    if ( types_obj != null )
    {
      int inst_type = InstrumentType.UNKNOWN;
      if ( types_obj instanceof Integer )               // change to an array
      {
        inst_type  = ((Integer)types_obj).intValue();
        int list[] = new int[1];
        list[0]    = inst_type;
        setAttribute( new IntListAttribute( Attribute.INST_TYPE, list ) );
      } 
      else if ( types_obj instanceof int[] && ((int[])types_obj).length > 0 )
        inst_type = ((int[])types_obj)[0]; 

      String ds_type = (String)attr_list.getAttributeValue(Attribute.DS_TYPE); 
      
      if ( ds_type != null )
       try{
          if ( ds_type.equals( Attribute.SAMPLE_DATA ) )
            DataSetFactory.addOperators( this, inst_type );
          else if ( ds_type.equals( Attribute.MONITOR_DATA ) )
            DataSetFactory.addMonitorOperators( this, inst_type );
      }catch(Throwable ss){}
    }
  }
}
