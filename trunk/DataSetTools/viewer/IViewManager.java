/*
 * @(#)IViewManager.java  0.1  2000/02/14
 *
 * ---------------------------------------------------------------------------
 *  $Log$
 *  Revision 1.1  2000/07/10 22:59:14  dennis
 *  July 10, 2000 version... many changes
 *
 *  Revision 1.3  2000/05/11 15:50:58  dennis
 *  Added RCS logging
 *
 *
 */

package DataSetTools.viewer;

import DataSetTools.dataset.*;
import DataSetTools.util.*;


/**
 * The IViewManager interface provides an interface that ViewManager objects
 * must implement.  A ViewManager accepts a DataSet and allows the user to
 * control views of the DataSet.  It also is updated when the DataSet it
 * observes is changed. 
 */
public interface IViewManager extends IObserver
{

  public static final String IMAGE           = "Image View";
  public static final String SCROLLED_GRAPHS = "Scrolled Graph View";
  public static final String SELECTED_GRAPHS = "Selected Graph View";
  
  public void setDataSet( DataSet ds );

  public DataSet getDataSet();

  public void setView( String view_type );

  public void destroy();
}
