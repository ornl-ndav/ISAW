package OverplotView;

/**
 * $Id$
 *
 * forces basic services needed by GraphableDataAdapter.
 *
 * $Log$
 * Revision 1.1  2000/07/06 16:17:44  neffk
 * Initial revision
 *
 * Revision 1.9  2000/06/20 20:50:04  neffk
 * SELECTED GRAPH VIEW DISTRO #1
 *
 * Revision 1.8  2000/06/15 16:07:29  neffk
 * fixed comment
 *
 * Revision 1.7  2000/06/15 15:58:41  neffk
 *
 * Revision 1.6  2000/06/01 14:12:08  neffk
 * added a new line in log message
 *
 * Revision 1.5  2000/05/02 08:21:45  psam
 * 1) added float percent_offset parameter to transferData()
 *
 * Revision 1.4  2000/05/01 05:32:51  psam
 * added public abstract void setRanges( UXS x, UXS y ) and associated import
 *
 * Revision 1.3  2000/04/30 20:21:15  psam
 * added a more appropriate constructor to initialize units and labels
 *
 * Revision 1.2  2000/04/24 18:36:11  psam
 * works 1.0
 *
 * Revision 1.1  2000/04/08 01:02:43  psam
 * Initial revision
 *
 *
 */

import java.awt.*;
import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.util.*;
import OverplotView.components.containers.*;

import gov.noaa.noaaserver.sgt.*;

public interface SelectedGraph 
{

  /**
   * redraws or adds data to graph, depending on the RedrawInstruction.  
   *
   * note: this function should make sure it clears all of its data and gets
   *       fresh data.
   *
   */
  public void redraw( RedrawInstruction instruction );



  /**
   * removes all data from graph
   *
   */
  public abstract void clear();

 

  /**
   * sets the size of the graph.  usually calculated by program.
   *
   */
  public void setSize( Dimension d );
  

}

