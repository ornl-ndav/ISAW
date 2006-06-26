/*
 * File: IViewManager.java 
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
 *  Revision 1.17  2006/06/26 16:29:26  amoe
 *  -added public static final String DIFFERENCE_GRAPH
 *
 *  Revision 1.16  2006/05/31 14:29:27  rmikk
 *  Added a constant for the new POINTEDAT_TABLE view
 *
 *  Revision 1.15  2004/03/15 19:33:59  dennis
 *  Removed unused imports after factoring out view components,
 *  math and utilities.
 *
 *  Revision 1.14  2004/03/15 03:28:58  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.13  2004/01/29 00:02:57  dennis
 *  Added HKL_SliceView
 *
 *  Revision 1.12  2003/10/30 17:16:06  dennis
 *  Removed "OLD Selected Graph View" that used SGT.
 *
 *  Revision 1.11  2003/08/08 17:52:47  dennis
 *  Added final String names for "Brent" view and old selected graph
 *  view.
 *
 *  Revision 1.10  2003/03/18 14:42:43  dennis
 *  Added option for popping up an additional ViewManager to the
 *  view menu of an existing ViewManager
 *
 *  Revision 1.9  2002/11/27 23:24:18  pfpeterson
 *  standardized header
 *
 *  Revision 1.8  2002/07/17 19:10:14  rmikk
 *  Fixed up the table views menu choices
 *
 *  Revision 1.7  2002/07/16 21:36:11  rmikk
 *  Change TABLE to represent the "Advanced Table"
 *
 *  Revision 1.6  2002/07/10 19:38:21  rmikk
 *  Added a string for the Contour view
 *
 */

package DataSetTools.viewer;

import gov.anl.ipns.Util.Messaging.*;
import DataSetTools.dataset.*;

/**
 * The IViewManager interface provides an interface that ViewManager objects
 * must implement.  A ViewManager accepts a DataSet and allows the user to
 * control views of the DataSet.  It also is updated when the DataSet it
 * observes is changed. 
 */
public interface IViewManager extends IObserver
{
  public static final String ADDITIONAL_VIEW = "Additional View";
  public static final String IMAGE           = "Image View";
  public static final String POINTEDAT_TABLE ="Pointed At Table View";
  public static final String SCROLLED_GRAPHS = "Scrolled Graph View";
  public static final String SELECTED_GRAPHS = "Selected Graph View";
  public static final String DIFFERENCE_GRAPH= "Difference Graph View";
  public static final String TABLE           = "Table Generator";
  public static final String THREE_D         = "3D View";
  public static final String CONTOUR         = "Contour View";
  public static final String HKL_SLICE       = "HKL Slice View";
  public void setDataSet( DataSet ds );

  public DataSet getDataSet();

  public void setView( String view_type );

  public void destroy();
}
