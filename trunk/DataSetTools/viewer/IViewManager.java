/*
 * @(#)IViewManager.java 
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 *  Revision 1.6  2002/07/10 19:38:21  rmikk
 *  Added a string for the Contour view
 *
 *  Revision 1.5  2001/08/13 16:19:34  dennis
 *  Added Ruth's Table view
 *
 *  Revision 1.4  2001/05/09 21:32:55  dennis
 *  Added code to include the ThreeDViewer
 *
 *  Revision 1.3  2001/04/26 14:21:35  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.2  2001/01/29 21:26:48  dennis
 *  Now uses CVS version numbers.
 *
 *  Revision 1.1  2000/07/10 22:59:14  dennis
 *  Now Using CVS
 *
 *  Revision 1.3  2000/05/11 15:50:58  dennis
 *  Added RCS logging
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
  public static final String TABLE           = "Table View";
  public static final String THREE_D         = "3D View";
  public static final String CONTOUR         = "Contour View";
  public void setDataSet( DataSet ds );

  public DataSet getDataSet();

  public void setView( String view_type );

  public void destroy();
}
