/*
 * File:  DataSetViewer.java 
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 *  Revision 1.12  2002/12/10 22:13:35  pfpeterson
 *  Fixed javadoc
 *
 *  Revision 1.11  2002/11/27 23:24:18  pfpeterson
 *  standardized header
 *
 */

package DataSetTools.viewer;

import  DataSetTools.dataset.*;
import  java.awt.*;
import  java.io.*;
import  javax.swing.*;

/**
 *  DataSetViewer is the abstract base class for objects that provide views of
 *  DataSets.  
 *
 *  @see DataSetTools.viewer.Image.ImageView
 *  @see DataSetTools.viewer.Graph.GraphView
 *  @see DataSetTools.viewer.ThreeD.ThreeDView
 *  @see DataSetTools.viewer.Table.TabView
 *  @see DataSetTools.viewer.OverplotView.GraphableDataManager
 *
 */ 

public abstract class DataSetViewer extends    JPanel
                                    implements Serializable
{
    public static final int FILE_MENU_ID   = 0;
    public static final int EDIT_MENU_ID   = 1;
    public static final int VIEW_MENU_ID   = 2;
    public static final int OPTION_MENU_ID = 3;

    public static final String NEW_DATA_SET    = "New Data Set";

    private   DataSet     data_set; // The Data Set being viewed 
    private   ViewerState state;    // state information for the DataSetViewer
    
    protected JMenuBar menu_bar;    // NOTE: it is the responsibility 
                                    // of the user of this class to add the 
                                    // MenuBar to whatever frame the viewer 
                                    // is placed in. 
 
    JMenu fileMenu    = new JMenu("File");      // Meus for the menu bar 
    JMenu editMenu    = new JMenu("Edit");
    JMenu viewMenu    = new JMenu("View");
    JMenu optionsMenu = new JMenu("Options");

    /**
     * Accepts a DataSet creates an instance of a viewer for the data set, 
     * using default options.
     *
     * @param  data_set   The DataSet to be viewed
     */
    public DataSetViewer( DataSet data_set )
    {
        this( data_set, null );
    }
    
    /** 
     * Accepts a DataSet and a ViewerState object and creates an instance of 
     * a viewer for the data set, with it's options initialized from the
     * state paramter.  If the state parameter is null, a new default state
     * is created.
     * 
     * @param  data_set   The DataSet to be viewed
     * @param  state      The state of options for this viewer
     */
    public DataSetViewer( DataSet data_set, ViewerState state )
    {
        this.data_set = data_set;
        if ( state == null )
          this.state    = new ViewerState();
        else
          this.state    = state;

        //neccesary so popup menus show up above heavyweight containers
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        menu_bar = new JMenuBar();
        menu_bar.add(fileMenu); 
        menu_bar.add(editMenu); 
        menu_bar.add(viewMenu); 
        menu_bar.add(optionsMenu);
    }

    /**
     *  Change the DataSet being viewed to the specified DataSet.  Derived
     *  classes should override this and take what additional steps are needed
     *  to change the specific viewer to the deal with the new DataSet.
     *
     *  @param  ds  The new DataSet to be viewed
     */
    public void setDataSet( DataSet ds )
    {
      data_set = ds;
    }
    
    /**
     *  Get a reference to the DataSet currently being viewed.
     */
    public DataSet getDataSet()
    {
      return data_set;
    }

    /**
     *  Check whether or not the DataSet is valid and non-empty.
     */
    public boolean validDataSet()
    {
      if ( data_set == null )
        return false;

      if ( data_set.getNum_entries() == 0 )
        return false;

      return true;
    }

    /**
     *  Get a reference to a state object containing information about the
     *  state of the options for this viewer. 
     */
    public ViewerState getState()
    {
      return state;
    }


    /**
     *  Redraw the view of the DataSet, since the DataSet has been changed.
     *
     *  @param  reason  The reason that the redraw was suggested.  The reasons
     *                  will be the reasons sent out via notification
     *                  of DataSet observers, or the message NEW_DATA_SET,
     *                  if the entire DataSet was replaced.  
     */
    public abstract void redraw( String reason );

    /**
     *  Get a reference to the menu bar for this viewer.
     */
    public JMenuBar getMenuBar()
    { 
      return menu_bar; 
    }

    /**
     *  Return a range of X values specified by the user to be used to 
     *  control X-Axis conversions.  This may be overridden in derived 
     *  classes.  ( The default behavior is to just returns null. )
     *
     *  @return  null.  Derived classes may override this an return a 
     *                  meaningful X scale.  In that case, number of bins
     *                  and the start and end X values will be used for 
     *                  any subsequent conversions operations.
     *
     */
    public XScale getXConversionScale()
    {
      return null;
    } 


}
