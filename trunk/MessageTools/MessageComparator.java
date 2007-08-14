/*
 * File:  MessageComparator.java
 *
 * Copyright (C) 2005 Dennis Mikkelson
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
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2007/08/14 00:09:01  dennis
 *  Added MessageTools files from UW-Stout repository.
 *
 *  Revision 1.3  2006/10/23 00:16:43  dennis
 *  Switched time stamp to a double, obtained System.nanoTime()/1.0e6
 *
 *  Revision 1.2  2006/09/24 05:27:34  dennis
 *  No longer implements Serializable
 *
 *  Revision 1.1  2005/11/29 01:59:36  dennis
 *  Initial version.
 *
 */

package MessageTools;

import java.util.*;

/**
 *  This class is used to compare messages for sorting.  The comparison is
 *  done in two stages.  First, the time stamps on the messages are compared.
 *  If the time stamps are different, the comparsion is based on the 
 *  time stamps.  If the time stamps are the same, the comparison is based
 *  on the tag value that was set when the message was received by the
 *  message center.
 */

public class MessageComparator implements Comparator
{

  /* --------------------------- compare -------------------------------- */
  /**
   *  Compare two Message objects based on time stamp and tag value.  
   *
   *  @param  o1   The first message object
   *  @param  o2   The first message object
   *
   *  @return Return -1 if o1's time stamp is less than o2's time stamp, OR if
   *             their time stamps are equal, but o1's tag value is less than
   *             o2's tag value.
   *          Return 0 if the time stamps and tag values are the same.
   *          Return +1 if o1's time stamp is greater than o2's time stamp, OR 
   *             if their time stamps are equal, but o1's tag value is greater
   *             than o2's tag value.
   */
  public int compare( Object o1, Object o2 )
  {
    double time_1 = ((Message)o1).getTime_stamp(); 
    double time_2 = ((Message)o2).getTime_stamp(); 
     
    if ( time_1 < time_2 )
      return -1;
    else if ( time_1 == time_2 )
    {
      long tag_1 = ((Message)o1).getTag();
      long tag_2 = ((Message)o2).getTag();
      if ( tag_1 < tag_2 )
        return -1;
      else if ( tag_1 == tag_2 )
        return 0;
      else
        return 1;
    }
    else
      return 1;
  }

}
