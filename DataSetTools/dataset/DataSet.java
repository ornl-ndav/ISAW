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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.18  2001/07/02 16:41:43  dennis
 *  Added methods:
 *    getAttribute( index )
 *    getAttribute( name )
 *
 *  Revision 1.17  2001/06/29 22:05:21  dennis
 *  Removed un-needed calls to System.gc
 *  (They introduced a large time delay!!)
 *
 *  Revision 1.16  2001/06/07 21:19:07  dennis
 *  Added calls to System.gc() at the end of clone() and copy()
 *  methods.
 *
 *  Revision 1.15  2001/06/06 21:20:24  dennis
 *  The copy() method now also preserves the last_sort_attribute value.
 *
 *  Revision 1.14  2001/06/04 22:48:12  dennis
 *  Added method getLastSortAttribute().
 *
 *  Revision 1.13  2001/06/04 20:01:33  dennis
 *  Added Quick Sort option to Sort() method.
 *
 *  Revision 1.12  2001/04/25 19:03:35  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.11  2001/04/02 20:49:40  dennis
 *  Added method to remove an operator from the DataSet.
 *
 *  Revision 1.10  2001/02/15 23:29:05  dennis
 *  Now the copy() method will not attempt to copy a DataSet
 *  into itself.  The observers will just be notified with
 *  a DATA_CHANGED message.
 *
 *  Revision 1.9  2000/11/07 16:02:58  dennis
 *  Changed the return type of the getYRange() method from a UniformXScale to
 *  a ClosedInterval so that the case where the YRange degenerated to one point
 *  could be handled.
 *
 *  Revision 1.8  2000/10/03 21:37:34  dennis
 *  Modified to add a unique integer "tag" to each DataSet as it is created.
 *  Added routine to get the "tag".
 *  Changed vector.clear() to vector.removeAllElements() for compatibility
 *  with 1.1.8 version of Java.
 *
 *  Revision 1.7  2000/08/01 01:33:21  dennis
 *  Changed clone() to display an error message if a null Data block is
 *  found while cloning
 *
 *  Revision 1.6  2000/07/17 21:01:14  dennis
 *  Minor reformat of documentation
 *
 *  Revision 1.5  2000/07/14 14:40:09  dennis
 *  Added copy(ds) method to copy the contents of a DataSet ds
 *  to the current DataSet
 *
 *  Revision 1.4  2000/07/11 21:17:47  dennis
 *  Added method getIndex_of_data() to get the index of a specific Data 
 *   block in the DataSet
 *
 *  Revision 1.3  2000/07/10 22:23:54  dennis
 *  Now using CVS 
 *
 *  Revision 1.27  2000/06/15 15:17:32  dennis
 *  Added methods insertData_entry() and replaceData_entry()
 *
 *  Revision 1.26  2000/06/15 14:15:33  dennis
 *  Added methods to get/set the list of observers as a whole.  This was
 *  needed to make the object serializable.  Specifically, if the DataSet
 *  is serialized, all objects that it refers to would also be serialized,
 *  includeing the viewers, etc.  Since this was not desired, it was
 *  necessary to serialize the DataSet in four steps:
 *    1.get the list of observers
 *    2.remove all observers from the DataSet itself
 *    3.serialize the DataSet
 *    4.set the original list of obervers back in the DataSet
 *
 *  Revision 1.25  2000/06/08 15:10:47  dennis
 *  Added wrapper methods th directly set/get attributes without getting
 *  the entire list of attributes.
 *
 *  Revision 1.24  2000/05/12 15:44:34  dennis
 *  Modified the getXRange() method to use a new form of UniformXScale.expand()
 *  that does not alter the current UniformXScale.
 *
 *  Revision 1.23  2000/05/11 16:00:45  dennis
 *  Added RCS logging
 *
 * 1.03 2000/03/16 Added routines to work with "selection" tags on the Data
 *                 blocks.  Also added routine to delete the selected Data
 *                 blocks.
 *
 * 1.02  99/06/04  Added a vector of operations to the data set.  When a
 *                 data set is constructed, the list of operations that are
 *                 allowed on this data set should also be specified.
 */

package  DataSetTools.dataset;

import java.util.Vector;
import java.io.*;
import java.lang.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;

/**
 * The concrete root class for a set of Data objects.  A DataSet object
 * bundles together a vector of Data objects with associated units, labels,
 * title, attributes and log information.  Data objects can be added to or 
 * removed from the data set using the methods of this class.  Also, the
 * list of attributes is extensible and can be accessed through methods in
 * this class.
 *
 * @see DataSetTools.dataset.Data
 *
 * @version 1.02  
 */

public class DataSet implements IAttributeList,
                                Serializable,
                                IObservable
{
  private static long current_ds_tag = 0; // Each DataSet will be assigned a
                                          // unique, immutable tag when it is
                                          // created.

  public static final int INVALID_GROUP_ID = -1;
  public static final int INVALID_INDEX    = -1;

  public static final int NOT_SORTED  = -1;
  public static final int Q_SORT      =  0;
  public static final int BUBBLE_SORT =  1;

                                          // Some operators need a default 
                                          // DataSet to hold in a parameter.
                                          // To avoid always constructing new
                                          // DataSets for this purpose, we
                                          // provide one constant empty DataSet
  public static final DataSet EMPTY_DATA_SET 
                      = new DataSet( "EMPTY_DATA_SET","Constant Empty DataSet");


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
  private IObserverList observers;
  private Vector        operators;
  private OperationLog  op_log;

  private long          ds_tag;
  private int           pointed_at_index;
  private String        last_sort_attribute = "";

  /**
   * Constructs an empty data set with the specified title, initial log
   * object, units and labels.
   *
   * @param   title     String giving a title for the data set.
   * @param   op_log    OperationLog object giving the initial log record
   *                    for this data set. 
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
    this.ds_tag           = current_ds_tag++;   // record tag and advance to 
                                                // the next tag value. 
    setTitle( title );
  }

  /**
   * Constructs an empty data set with the specified title, initial log 
   * string, units and labels. 
   *
   * @param   title     String giving a title for the data set.
   * @param   log_info  String giving log information for the data set.
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
   * Constructs an empty data set with the specified title and log information.
   * The units and labels are assigned default values.
   *
   * @param   title     String giving a title for the data set.
   * @param   log_info  String giving log information for the data set.
   *
   * @see DataSetTools.dataset.Data
   */
  public DataSet( String  title, String  log_info )
  {
    this( title, log_info, "X UNITS", "X LABEL", "Y UNITS", "Y LABEL");
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
     observers = (IObserverList)list.clone();
   }



  /**
   * Returns the title of the data set 
   *
   * @return  The DataSet title.
   */
  public String getTitle() { return title; }


  /**
   * Returns a numeric tag that is unique to this data set within a program.
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
      ((Data)data.elementAt(index)).setSelected( d );
    else
      System.out.println("Error: setSelectFlag(i,d) called with invalid index");
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
   *  Toggle the selected flag of the specified Data object to the specified 
   *  value.  If the index is not valid, there is no effect.
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
    boolean some_removed = false;
                                     //  NOTE: We must remove in reverse order

    for ( int i = data.size() - 1; i >= 0; i-- )
      if ( isSelected(i) == status )
      {
        data.removeElementAt(i);
        some_removed = true;
      }

    return some_removed;
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
    // This check is necessary, since we might have deleted some elements
    // from the DataSet, after the pointed_at_index was set.
    if ( pointed_at_index < 0  || pointed_at_index >= data.size() )
      pointed_at_index = INVALID_INDEX;
    
    return pointed_at_index;
  }


  /**
   * Sets the title of the DataSet both as an instance variable and as
   * an attribute of the DataSet.
   *
   * @param  title   The String to use for the new title of the data set
   */
  public void setTitle( String title ) 
  { 
    this.title = title; 

    StringAttribute title_attr = new StringAttribute( Attribute.TITLE, title );
    attr_list.setAttribute( title_attr, 0 );
  }


  /**
   * Returns the log information for the data set 
   */
  public OperationLog getOp_log() { return op_log; }


  /**
   * Set the entire operation log for the data set
   */
  public void setOp_log( OperationLog op_log ) 
  { 
    this.op_log = (OperationLog)op_log.clone(); 
  }

  /**
   * Copy the operation log from the specified the data set
   */
  public void copyOp_log( DataSet data_set )
  { 
    this.op_log = (OperationLog)data_set.getOp_log().clone();
  }


  /**
   * Adds a new entry to the log information for the data set 
   *
   * @param log_info   The String to be added to the log information for
   *                   this data set.
   */
  public void addLog_entry( String log_info )
  {
    this.op_log.addEntry( log_info );
  }


  /**
   * Returns the number of Data objects in the DataSet 
   */
  public int getNum_entries() { return data.size(); }


  /**
   * Returns a reference to the first data object in this data set with the 
   * specified group ID.  If there is no data object with the correct ID, this
   * returns null.
   *
   * @param  group_id      The group id of the requested Data object in the 
   *                       list of Data objects in this DataSet.
   */
  public Data getData_entry_with_id( int group_id )
  {
     for ( int i = 0; i < data.size(); i++ )
       if ( ((Data)data.elementAt( i )).getGroup_ID() == group_id )
         return (Data)data.elementAt( i );

     return null;     // if we didn't find the right id
  }


  /**
   * Returns a reference to the Data object from the specified 
   * position in the data set if the index is valid, otherwise return null.
   *
   * @param  index   The index of the requested Data object in the list of
   *                 Data objects in this DataSet. 
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
   *       
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
   * Adds a new Data object to the end of the list of Data objects.
   *
   * @param  entry   The Data object to be added to the list of Data objects
   *                 in this DataSet.
   */
  public void addData_entry( Data entry )
  {
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
   * Removes the data entry from the specified position in the data set.
   *
   * @param  index  The index of the Data object to be removed from the list
   *                of Data objects in this DataSet.
   */
  public void removeData_entry( int index )
  {
     data.removeElementAt( index );
  }


  /**
   * Removes the first data object in this data set with the specified group id.
   * If there is no data object with the correct ID, this has no effect.
   *
   * @param  group_id      The group_id of the requested Data object in the 
   *                       list of Data objects in this DataSet.
   */
  public void removeData_entry_with_id( int group_id )
  {
     for ( int i = 0; i < data.size(); i++ )
       if ( ((Data)data.elementAt( i )).getGroup_ID() == group_id )
       {
         data.removeElementAt(i);           // found the id, so remove the
         return;                            // the data object and exit
       }
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
   * operators on this data set.
   *
   * @param  index   The index of the requested operation in the list of
   *                 operators in this DataSet.
   */
  public DataSetOperator getOperator( int index )
  {
     return (DataSetOperator)operators.elementAt( index );
  }


  /**
   * Returns a reference to the operation in the list of available operators 
   * on this data set with the specified TITLE.  If the named operator is not
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
   * Adds a new operator to the list of operators for this data set.
   *
   * @param  operator    The operation to be added to the list of operations
   *                     in this DataSet.
   */
  public void addOperator( DataSetOperator operator )
  {
    operator.setDataSet( this );
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
   * Get the range of X values for the collection of Data objects in this
   * data set.
   */
  public UniformXScale getXRange()
  {
    Data           data_block;
    UniformXScale  range;

    if ( this.getNum_entries() < 1 )
      return null;
    else
    {
      data_block = (Data)getData_entry( 0 );
      range      = new UniformXScale( data_block.getX_scale().getStart_x(),
                                      data_block.getX_scale().getEnd_x(),
                                      2                     );
                           // an X "range" is a UniformXScale with only
                           // two entries. 
    }

    for ( int i = 1; i < this.getNum_entries(); i++ )
    {
      data_block = (Data)getData_entry( i );
      range = range.expand( data_block.getX_scale() );
    }

    return range;
  }

  /**
   * Get the range of Y values for the collection of Data objects in this
   * data set.
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
   */
  public String getX_units() { return x_units; }

  /**
   * Sets the units for the "X" axis 
   *
   * @param  units   String giving the units for the "X" axis
   */
  public void setX_units( String units ) { this.x_units = units; }

  /**
   * Returns the label for the "X" axis 
   */
  public String getX_label() { return x_label; }

  /**
   * Sets the label for the "X" axis 
   *
   * @param  label  String giving the label for the "X" axis
   */
  public void setX_label( String label ) { this.x_label = label; }

  /**
   * Returns the units for the "Y" scale 
   */
  public String getY_units() { return y_units; }

  /**
   * Sets the units for the "Y" scale 
   *
   * @param  units   String giving the units for the "Y" axis
   */
  public void setY_units( String units ) { this.y_units = units; }

  /**
   * Returns the label for the "Y" axis 
   */
  public String getY_label() { return y_label; }

  /**
   * Sets the label for the "Y" axis 
   *
   * @param  label  String giving the label for the "Y" axis
   */
  public void setY_label( String label ) { this.y_label = label; }


  /**
   *  Get a copy of the list of attributes for this Data object.
   */
  public AttributeList getAttributeList()
  {
    return (AttributeList)attr_list.clone();
  }

  /**
   *  Set the list of attributes for this DataSet object to be a COPY of the 
   *  specified list of attributes.
   */
  public void setAttributeList( AttributeList attr_list )
  {
    this.attr_list = (AttributeList)attr_list.clone();

    setTitle( this.title );   // force the attribute list to contain the
                              // correct title
  }

  /**
   * Gets the number of attributes set for this object.
   */
  public int getNum_attributes()
  {
    return attr_list.getNum_attributes();
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
  }

  /**
   * Get the attribute at the specified index from the list of
   * attributes. If the index is invalid, this returns null.
   * 
   * @param  index  The position of the attribute in the list of attributes.
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
   */
  public Object getAttributeValue( String name )
  {
    return attr_list.getAttributeValue( name );
  }



  /**
   *  Sort the list of Data entries based on the specified attribute.  If
   *  a stable sort is needed, specify BUBBLE_SORT, else specify Q_SORT.
   *
   *  @param  attr_name   The name of the attribute on which to sort.  The
   *                      attr_name parameter must be the name of an attribute
   *                      that is stored with every Data entry in this data
   *                      set.
   *  @param  increasing  Flag indicating whether to sort the list in 
   *                      increasing or decreasing order.
   *  @param  sort_type   Flag specifying the sort type, either
   *                      DataSet.Q_SORT or DataSet.BUBBLE_SORT.
   *
   *  @return    This returns true if the DataSet entries were sorted and 
   *             returns false otherwise.
   */
  public boolean Sort( String attr_name, boolean increasing, int sort_type )
  {
    int n = data.size();

    if ( n <= 1 )            // empty or short list is sorted by default
      return true;

    int       position[] = new int[ n ];
    Attribute attr[]     = new Attribute[ n ];

    AttributeList  attr_list;        // save the required attribute from each
    Attribute      one_attr;         // Data entry and the index of the Data
    for ( int i = 0; i < n; i++ )    // entry in arrays
    {
      attr_list = getData_entry(i).getAttributeList();
      one_attr = attr_list.getAttribute( attr_name );
      if ( one_attr == null )
        return false;               // attribute missing from this Data object 
      
      attr[i]     = one_attr;
      position[i] = i;
    }

    if ( sort_type == Q_SORT )
      QSort( attr, position, 0, n-1, increasing );
    else
      BubbleSort( attr, position, increasing );

                                                  // copy the data objects to
    Vector new_data = new Vector();               // a new vector in the right
    for ( int i = 0; i < n; i++ )                 // order
      new_data.addElement( data.elementAt( position[i] ) );

    data = new_data;

    last_sort_attribute = attr_name;
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
   *  @param  d    The DataSet whose attribute list is to be combined
   *               with the current object's attribute list
   */
   public void CombineAttributeList( DataSet ds )
   {
     attr_list.combine( ds.getAttributeList() );
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
      this.notifyIObservers( IObserver.DATA_CHANGED );
      return;
    }

    this.title               = ds.title;
    this.x_units             = ds.x_units;
    this.x_label             = ds.x_label;
    this.y_units             = ds.y_units;
    this.y_label             = ds.y_label;

                                         // Note: we are NOT copying the ds_tag
    this.pointed_at_index    = ds.pointed_at_index;
    this.last_sort_attribute = ds.last_sort_attribute;

    this.setAttributeList( ds.getAttributeList() );

    this.data = new Vector(); 
    int num_entries = ds.getNum_entries();

    for ( int i = 0; i < num_entries; i++ )
    {
      Data d = ds.getData_entry( i );
      this.addData_entry( (Data)(d.clone()) );
    }

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


  /**
   * Clone the current DataSet, including the operation log, the list of
   * operators and the list of individual Data objects.
   */
   public Object clone()
  {
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

    return new_ds;
  }



  /**
   * Clone an EMPTY DataSet with the same title, units, label, operation log,
   * and operators as the original data set.
   */
   public DataSet empty_clone()
  {
    DataSet new_ds = new DataSet( getTitle(),           // get a new data set
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
    new_ds.last_sort_attribute = last_sort_attribute;

    return new_ds;
  }


  /**
   *  Provide an identifier string for this DataSet
   */
  public String toString()
  {
     return ""+ds_tag+":"+title;
  }

  /**
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

/* ---------------------------- BubbleSort -------------------------------- */

  private static void BubbleSort( Attribute list[], 
                                  int       index[], 
                                  boolean   increasing )
  {
   int pass, 
       k;
   int n = index.length;

   if ( increasing )                                 // put in increasing order
   {
     for ( pass = 1; pass < n; pass++ )
       for ( k = 0; k < n - pass; k++ )
         if ( list[ index[k] ].compare( list[ index[k+1] ] ) > 0 )
         {                                          
           int temp   = index[k];
           index[k]   = index[k+1];
           index[k+1] = temp;
         }
    }
    else                                              // put in decreasing order
    {
     for ( pass = 1; pass < n; pass++ )
       for ( k = 0; k < n - pass; k++ )
         if ( list[ index[k] ].compare( list[ index[k+1] ] ) < 0 )
         {                                                
           int temp   = index[k];
           index[k]   = index[k+1];
           index[k+1] = temp;
         }
    }
  }


/* ------------------------------- swap ---------------------------------- */

  private static void swap( int index[], int i, int j )
  {
    int  temp = index[i];
    index[i]  = index[j];
    index[j]  = temp;
  }


/* -------------------------------- QSort --------------------------------- */

   private void QSort( Attribute list[], 
                       int       index[],
                       int       start, 
                       int       end, 
                       boolean   increasing )
   {
     int   i = start;
     int   j = end;

     if ( i >= j )                      // at most one element, so we're
       return;                          // done with this sublist

     swap( index, start, (i+j)/2 );

     while ( i < j )
     {
       if ( increasing )
       {
         while ( i < end && list[index[i]].compare( list[index[start]] ) <= 0 )
           i++;
         while ( list[index[j]].compare( list[index[start]] ) > 0 )
           j--;
       }
       else
       {
         while ( i < end && list[index[i]].compare( list[index[start]] ) >= 0 )
           i++;
         while ( list[index[j]].compare( list[index[start]] ) < 0 )
           j--;
       }
 
       if ( i < j )
         swap( index, i, j );
     }
     swap( index, start, j );

     QSort( list, index, start, j-1, increasing );
     QSort( list, index, j+1, end, increasing );
 } 

}
