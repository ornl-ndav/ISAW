/*
 * @(#)DataSetTools.viewer.util.KeySelect 0.1  2000/05/02  Dennis Mikkelson
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.1  2000/07/10 23:04:19  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.4  2000/05/31 18:58:25  dennis
 *  removed unused variable
 *
 *  Revision 1.3  2000/05/16 22:27:07  dennis
 *  fixed minor documentation error
 *
 *  Revision 1.2  2000/05/11 15:50:09  dennis
 *  Added javadoc comments
 *
 *
 */

package DataSetTools.viewer.util;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;

/**
 *  The KeySelect class implements the keyboard based spectrum selection
 *  policy for the ImageView and GraphView viewers.
 */   

public final class KeySelect implements Serializable
{

  /**
   *  Don't let anyone instantiate this class
   */
   private KeySelect(){}

  /**
   *  Mark Data blocks in ds as "Selected" based on the specified KeyEvent
   *  and on the Data block that is under the cursor.
   *
   *  @param  ds         The DataSet in which Data blocks are being selected.
   *  @param  new_index  The index of the Data block under the cursor.
   *  @param  e          The KeyEvent to be interpreted.
   */
  public static void ProcessKeySelection( DataSet  ds,  
                                          int      new_index, 
                                          KeyEvent e )
  {
    char  key   = e.getKeyChar();
    int   code  = e.getKeyCode();
    int   last  = ds.getMostRecentlySelectedIndex();

    int   row     = new_index;

    if ( code != KeyEvent.VK_S &&
         code != KeyEvent.VK_T &&
         code != KeyEvent.VK_D    )       // no selection operation
      return;

    if ( !e.isControlDown() && code == KeyEvent.VK_S )  // control key is used
    {                                                   // to append selections
      ds.clearSelections();
    }

    if ( !e.isShiftDown() )                         // lower case, so change
    {                                               // selection on one block
      if (code == KeyEvent.VK_S )
        ds.setSelectFlag( row, true );
      else if ( code == KeyEvent.VK_T )
        ds.toggleSelectFlag( row );
      else
        ds.setSelectFlag( row, false );
    }

    else                                           // change select on range
      {                                            // of Data blocks
        if ( last == DataSet.INVALID_INDEX )       // just select one Data block
        {
          if ( code == KeyEvent.VK_S )
            ds.setSelectFlag( row, true );
          else if ( code == KeyEvent.VK_T )
            ds.toggleSelectFlag( row );
          else
            ds.setSelectFlag( row, false );
        }
        else
        {
          if ( last <= row )                           // this "if" is needed to
            for ( int i = last; i <= row; i++ )        // avoid doing i=last
              if ( code == KeyEvent.VK_S )             // twice
                ds.setSelectFlag( i, true );
              else if ( code == KeyEvent.VK_T )
                ds.toggleSelectFlag( i );
              else
                ds.setSelectFlag( i, false );

          else
            for ( int i = row; i <= last; i++ )
              if ( code == KeyEvent.VK_S )
                ds.setSelectFlag( i, true );
              else if ( code == KeyEvent.VK_T )
                ds.toggleSelectFlag( i );
              else
                ds.setSelectFlag( i, false );

           ds.toggleSelectFlag( row );      // make current row the
           ds.toggleSelectFlag( row );      // most recently selected
                                                      // if it was selected
        }
      }
      ds.notifyIObservers( IObserver.SELECTION_CHANGED );
  }
} 
