/*
 * File:  MaskChannels.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.2  2005/08/25 15:01:06  dennis
 * Moved to DATA_SET_EDIT_LIST_MACROS category in menus.
 *
 * Revision 1.1  2005/04/07 19:16:24  dennis
 * Initial (rough) version of operator to set specified channels to zero
 * for one or all Data blocks in a DataSet.
 *
 *
 */
package Operators.Special;

import java.util.*;

import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Numeric.*;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;

/**
 *  This class sets the data values to zero for the specified list of 
 *  channel numbers.   
 */
public class MaskChannels implements Wrappable, IWrappableWithCategoryList
{
  public DataSet  data_set = DataSet.EMPTY_DATA_SET;
  public int      group_id = 49;
  public IntListString  channel_numbers = new IntListString("0");

  /**
   *  Get the command name to be used in scripts.
   *  @return The command name 'MaskChannels'
   */
  public String getCommand() 
  {
    return "MaskChannels";
  }


 /**
  *  Get the list of categories describing where this operator should appear
  *  in the menu system.
  *
  *  @return an array of strings listing the menu where the operator 
  *  should appear.
  */
  public String[] getCategoryList()
  {
    return Operator.DATA_SET_EDIT_LIST_MACROS;
  }


  /**
   */
  public String getDocumentation() 
  {
    StringBuffer s = new StringBuffer();
    s.append( "@overview " );

    s.append( "@algorithm " );

    s.append( "@param data_set  The DataSet whose Data block or " );
    s.append( "Data blocks are to have certain channels set to zero." );
    s.append( "@param group_id  The ID for the Data block " );
    s.append( "whose channels are to be masked.  If this is a negative " );
    s.append( "number, all Data blocks in the DataSet while have the " );
    s.append( "specified channels set to zero." );

    s.append( "@return  " );
    s.append( " " );
    return s.toString();
  }

  /**
   */
  public Object calculate() 
  {
    if ( data_set == null )
      return new ErrorString("Null DataSet in MaskChannels");

    if ( data_set == DataSet.EMPTY_DATA_SET )
      return new ErrorString("Empty DataSet in MaskChannels");

    int n_data = data_set.getNum_entries();
    if ( n_data <= 0 )
      return new ErrorString("No Data blocks in DataSet in MaskChannels"); 

    Data   d;
    Vector data_blocks;
    if ( group_id < 0 )
    {
      data_blocks = new Vector( n_data );
      for ( int i = 0; i < n_data; i++ )
      {
        d = data_set.getData_entry(i);
        if ( d != null )
        {
          if ( !(d instanceof TabulatedData ) )
           return new ErrorString("DataSet with TabulatedData in MaskChannels");
          data_blocks.addElement( d );   
        }
      }
    }
    else
    {
      d = data_set.getData_entry_with_id( group_id );
      if ( d == null )
        return new ErrorString("ID " + group_id + 
                               " not in DataSet in MaskChannels");
      if ( !(d instanceof TabulatedData ) )
        return new ErrorString("DataSet with TabulatedData in MaskChannels");
      data_blocks = new Vector(1);
        data_blocks.addElement( d );   
    }
   
    int mask_channels[] = IntList.ToArray( channel_numbers.toString() );
    int channel;
    TabulatedData data_table;
    n_data = data_blocks.size();
    for ( int i = 0; i < n_data; i++ )
    {
      data_table = (TabulatedData)data_blocks.elementAt(i);
      float y[]     = data_table.getY_values();
      float sigma[] = data_table.getErrors();
      boolean errors_changed = false;
      for ( int k = 0; k < mask_channels.length; k++ )
      {
        channel = mask_channels[k];
        if ( channel >= 0 && channel < y.length )
        {
          y[channel] = 0;
          sigma[channel] = 0;
          errors_changed = true;
        }
      }
      if (errors_changed)
        data_table.setErrors( sigma );
    }
    
    String message = "Set channels " + channel_numbers.toString();
    if ( group_id < 0 )
      message = message + " to zero for all IDs";
    else 
      message = message + " to zero in ID: " + group_id;

    data_set.addLog_entry( message );
    return message;
  }
}
