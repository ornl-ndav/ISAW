/*
 * File:  ViewManager.java
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
 *  Revision 1.36  2003/09/11 17:22:41  rmikk
 *  Added a constructor that takes the ViewerState
 *  Extracted code to return a DataSetViewer given a DataSet,
 *     View Name and ViewerStata
 *
 *  Revision 1.35  2003/08/08 17:54:10  dennis
 *  Added option to change to New Selected Graph (Brent's) view.
 *
 *  Revision 1.34  2003/03/18 14:42:44  dennis
 *  Added option for popping up an additional ViewManager to the
 *  view menu of an existing ViewManager
 *
 *  Revision 1.33  2003/03/04 20:25:35  dennis
 *  Title on window is now set properly if the contents of the DataSet
 *  are changed to a different run.
 *
 *  Revision 1.32  2002/12/09 13:11:21  dennis
 *  Now checks for valid "pointed at" index, before using it as index
 *  into list of (possibly) reordered Data blocks.
 *
 *  Revision 1.31  2002/11/27 23:24:18  pfpeterson
 *  standardized header
 *
 *  Revision 1.30  2002/11/07 16:33:04  pfpeterson
 *  Closes viewer when message is recieved from the DataSet.
 *
 *  Revision 1.29  2002/10/16 19:22:15  dennis
 *  Added option to "link/unlink" views.  If the view is not linked to
 *  other views, the POINTED_AT_CHANGED messages are not passed out
 *  to other viewers, or acted on if they come from other viewers.
 *
 *  Revision 1.28  2002/10/08 15:44:06  dennis
 *  Added conversions of "Pointed At X" to proper units when the X-Axis
 *  of the DataSet has been converted in the ViewManager.
 *
 *  Revision 1.27  2002/10/07 19:35:57  dennis
 *  "Clear Selections" menu option now clears selections in the temporary
 *  DataSet as well as the original DataSet.  This fixes a bug where the
 *  "Clear Selection" failed to clear the selections on the viewer if an
 *  Axis Conversion had been done.
 *
 *  Revision 1.26  2002/10/02 22:04:15  dennis
 *  Now check the result of calling the conversion operator.  If some Data
 *  blocks are not converted, don't try to set the selection flags or
 *  PointedAt indices, since the meaning of the indices has changes.
 *  If a DataSet is not returned, just use the empty clone of the current
 *  DataSet.
 *
 *  Revision 1.25  2002/09/20 16:46:53  dennis
 *  Now uses IParameter rather than Parameter
 *
 *  Revision 1.24  2002/07/23 18:22:44  dennis
 *  Now passes "pointed at" x values between the DataSet
 *  and the tempDataSet.
 *
 *  Revision 1.23  2002/07/18 22:08:55  dennis
 *  Moved separate OverplotView hiearchy into DataSetTools/viewer
 *  hierarchy.
 *
 *  Revision 1.22  2002/07/17 19:10:55  rmikk
 *  Fixed up the table views menu choices and reordered
 *    the view menu
 *
 *  Revision 1.21  2002/07/16 21:37:55  rmikk
 *  Introduced support for the other quick table views
 *
 *  Revision 1.20  2002/07/12 18:26:15  rmikk
 *  Used the Constructor with the state variable for starting
 *    the Selected Graph view.
 *
 *  Revision 1.19  2002/07/10 19:39:03  rmikk
 *  Added code to incorporate the Contour View
 *
 *  Revision 1.18  2002/02/22 20:37:11  pfpeterson
 *  Operator reorganization.
 *
 */
 
package DataSetTools.viewer;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.operator.DataSet.EditList.*;
import DataSetTools.operator.DataSet.Math.DataSet.*;
import DataSetTools.operator.DataSet.Conversion.XAxis.*;
import DataSetTools.util.*;
import DataSetTools.components.ui.*;
import DataSetTools.viewer.util.*;
import DataSetTools.viewer.Graph.*;
import DataSetTools.viewer.Image.*;
import DataSetTools.viewer.ThreeD.*;
import DataSetTools.viewer.Table.*;
import DataSetTools.viewer.Contour.*;
import DataSetTools.viewer.OverplotView.*;
import DataSetTools.viewer.ViewerTemplate.*;
import DataSetTools.components.View.*;
import DataSetTools.components.View.OneD.*;
import DataSetTools.parameter.*;
import DataSetTools.math.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *  A ViewManager object manages viewers for a DataSet in an external 
 *  frame.  It contains a menu bar that allows the user to select the type 
 *  of viewer to be used to view the DataSet and appropriate view options.  
 *  It also is an observer of the DataSet and will be notified of changes 
 *  in the DataSet.  Since a ViewManager is a JFrame, it can 
 *  be closed by the user, or by the program.
 */

public class ViewManager extends    JFrame
                         implements IViewManager,
                                    Serializable
{
   public static boolean debug_view_manager   = false;

   private   ViewManager     view_manager = null;
   private   DataSetViewer   viewer = null;
   private   String          viewType = IMAGE;
   private   ViewerState     state = null;
   private   DataSet         dataSet;
   private   DataSet         tempDataSet;
   private   XAxisConversionOp conversion_operator = null;
   private   int[]           original_index;      // records the index in the
                                                  // original dataSet that 
                                                  // corresponds to an index in
                                                  // the tempDataSet; 
   private   int[]           new_index;           // records the index in
                                                  // tempDataSet that 
                                                  // corresponds to an index in
                                                  // the original DataSet  
   private JCheckBoxMenuItem show_all_button;
   private JCheckBoxMenuItem link_viewers_button;

   private static final String CLOSE_LABEL          = "Close Viewer";
   private static final String SAVE_NEW_DATA_SET    = "Save As New DataSet";

   private static final String SUM_MENU           = "Sum";
   private static final String SUM_SELECTED       = "Sum Selected Data";
   private static final String SUM_UNSELECTED     = "Sum Unselected Data";

   private static final String DELETE_MENU          = "Delete";
   private static final String DELETE_SELECTED      = "Delete Selected Data";
   private static final String DELETE_UNSELECTED    = "Delete Unselected Data";

   private static final String CLEAR_MENU           = "Clear";
   private static final String CLEAR_SELECTED       = "Clear Selected Flags";

   private static final String SHOW_ALL             = "Show All";
   private static final String LINK_VIEWS           = "Link Views";
   private static final String NO_CONVERSION_OP     = "None";
   private static TableViewMenuComponents table_MenuComp   = null;
    
   /**  
    *  Accepts a DataSet and view type and creates an instance of a 
    *  ViewManager for the data set.  
    *
    *  @param  ds        The DataSet to be viewed
    *  @param  view_type String describing the initial type of viewer to be 
    *                    used.  The valid strings are listed in the interface,
    *                    IViewManager
    * 
    *  @see IViewManager
    *  @see DataSetViewer
    *  @see DataSet
    */
   public ViewManager(DataSet ds, String view_type )
   {  this( ds,view_type, null);
    }

   /**  
    *  Accepts a DataSet and view type and creates an instance of a 
    *  ViewManager for the data set.  
    *
    *  @param  ds        The DataSet to be viewed
    *  @param  view_type String describing the initial type of viewer to be 
    *                    used.  The valid strings are listed in the interface,
    *                    IViewManager
    *  @param   ViewerState The viewer state
    * 
    *  @see IViewManager
    *  @see DataSetViewer
    *  @see DataSet
    */  
   public ViewManager(DataSet ds, String view_type, ViewerState state ){
      super( ds.toString() );
      view_manager = this;
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      
      dataSet = ds; 
      if ( ds == null )
        System.out.println("ERROR: ds is null in ViewManager constructor");
      else
        dataSet.addIObserver( this );

      addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent ev)
        {
          free_resources();
        }
      });

      setBounds(0,0,600,425);
      makeTempDataSet( true );
      this.state = state;
      setView( view_type ); 
      show();
  //  setVisible(true);
      conversion_operator = null;
      System.gc();
   }

   /**
    *  Specify a new DataSet to be viewed.
    *
    *  @param  ds   The new DataSet to be used by this ViewManager
    */ 
   public void setDataSet( DataSet ds )
   {
     if ( ds == dataSet )               // no change, just the same DataSet
     {
       if ( ds != null )
         setTitle( ds.toString() );
       return;
     }

     dataSet.deleteIObserver( this );
     dataSet = ds;
     makeTempDataSet( true );
     if ( ds != null )
     {
       ds.addIObserver( this );
       setTitle( ds.toString() );
     }

     if ( viewer != null )
       viewer.setDataSet( tempDataSet ); 

     System.gc();
   }

   /**
    *  Get the DataSet that is currently used by this ViewManager
    */   
   public DataSet getDataSet()
   {
     return dataSet;
   }

   /**
    *  Set a new viewer type for this ViewManager.  The available view 
    *  types are specified in the interface IViewManager.  Currently, the 
    *  supported view types are IViewManager.IMAGE and 
    *  IViewManager.SCROLLED_GRAPHS
    *
    *  @param   view_type  String specifying the type of viewer to be used.  
    *                      The valid strings are listed in the interface, 
    *                      IViewManager.
    */
   public void setView( String view_type )
   {
     getContentPane().setVisible(false);
     getContentPane().removeAll();
    
     if ( viewer != null )
         state = viewer.getState();

      viewer = ViewManager.getDataSetView( tempDataSet,view_type, state);
      if( viewer instanceof ImageView)
          viewType = IMAGE;
      else 
          viewType = view_type;
      getContentPane().add(viewer);
      getContentPane().setVisible(true);

      setJMenuBar( viewer.getMenuBar() );
      BuildFileMenu();
      BuildEditMenu();
      BuildViewMenu();
      BuildConversionsMenu();
      BuildOptionMenu();
      System.gc();
   }

   public static DataSetViewer getDataSetView( DataSet tempDataSet, String view_type,
            ViewerState state){
      DataSetViewer viewer = null;
      
      if ( view_type.equals( IMAGE ))
        viewer = new ImageView( tempDataSet, state );
      else if ( view_type.equals( SCROLLED_GRAPHS ))
        viewer = new GraphView( tempDataSet, state );
      else if ( view_type.equals( THREE_D ))
        viewer = new ThreeDView( tempDataSet, state );
      else if ( view_type.equals( SELECTED_GRAPHS ))             // Brent's 
      {
        DataSetData dsd = new DataSetData( tempDataSet );
        FunctionViewComponent viewComp = new FunctionViewComponent( dsd);
        viewer = new DataSetViewerMaker(tempDataSet, state, dsd, viewComp);
      }
      else if ( view_type.equals( SELECTED_GRAPH2 ))             // use either
        viewer = new GraphableDataManager( tempDataSet, state ); // Kevin's or
//        viewer = new ViewerTemplate( tempDataSet, state );     // Template  
      else if ( view_type.equals( TABLE)) //TABLE ) )
         viewer = new TabView( tempDataSet, state ); 
      else if ( view_type.equals( CONTOUR ) )
        viewer = new ContourView( tempDataSet, state ); 
      else
      { 
        if( table_MenuComp == null)
           table_MenuComp= new TableViewMenuComponents();
        viewer = table_MenuComp.getDataSetViewer(view_type, tempDataSet, state);
        if( viewer == null)
        {
           System.out.println( "ERROR: Unsupported view type in ViewManager:" );
           System.out.println( "      " + view_type );
           System.out.println( "using " + IMAGE + " by default" );
           viewer = new ImageView( tempDataSet, state );
           
        }
      }
      return viewer;
   }

  /**
   *  Send WINDOW_CLOSING event to shutdown the ViewManager cleanly and
   *  completely.
   */
   public void destroy()
   {
     WindowEvent win_ev = new WindowEvent( view_manager,
                                           WindowEvent.WINDOW_CLOSING );
     view_manager.dispatchEvent( win_ev );
   }


   /**
    *  Update the ViewManager due to a change in the DataSet.  This 
    *  method should be called by the DataSet's notification method, when 
    *  the DataSet is changed.
    *
    *  @param  observed  If all is well, this will be a reference to the 
    *                    DataSet that is being managed.
    *  @param  reason    Object telling the nature of the change and/or a
    *                    command.  The valid reasons are listed in the interface
    *                    IObserver
    *
    *  @see IObserver                     
    */
   public void update( Object observed, Object reason )
   {
     if ( viewer == null )
     {
       if ( debug_view_manager )
         System.out.println("ERROR: ViewManager Previously Destroyed .......");
       return;
     }

     if ( !( reason instanceof String) )   // we only deal with Strings
       return;

     String r_string = (String)reason;
  
     if ( debug_view_manager )
       System.out.println("ViewManager UPDATE : " + r_string );

     if ( observed == dataSet )             // message about original dataSet
     {
       if ( r_string.equals( DESTROY ))
         destroy();
       else if ( r_string.equals( CLOSE_VIEWERS ))
         destroy();

       else if (  r_string.equals( DATA_DELETED )   ||
                  r_string.equals( DATA_REORDERED ) ||
                  r_string.equals( DATA_CHANGED )   ||
                  r_string.equals( HIDDEN_CHANGED )  )
       {
         makeTempDataSet( false );
         viewer.setDataSet( tempDataSet );
         setTitle( dataSet.toString() );
         System.gc();
       }
       else if ( r_string.equals( POINTED_AT_CHANGED )  )
       {
         if ( !link_viewers_button.getState() )      // nothing to do
           return;
                                                     // tell the viewer the new
         int index = dataSet.getPointedAtIndex();    // "pointed at" index if
         if ( index != DataSet.INVALID_INDEX )       //  valid and different 
           if ( new_index[ index ] != DataSet.INVALID_INDEX )
             {
               tempDataSet.setPointedAtIndex( new_index[ index ] );
               float new_x = dataSet.getPointedAtX();
               if ( !Float.isNaN(new_x) )            // valid new_x is clamped
               {                                     // and mapped by the conv.
                 Data d = dataSet.getData_entry( index );          // operator
                 float x_min = d.getX_scale().getStart_x();             
                 float x_max = d.getX_scale().getEnd_x();             
                 if ( new_x < x_min )
                   new_x = x_min;
                 if ( new_x > x_max )
                   new_x = x_max;
                 if ( conversion_operator != null )
                   new_x = conversion_operator.convert_X_Value( new_x, index );
               }
               tempDataSet.setPointedAtX( new_x );
               viewer.redraw( (String)reason );
             }  
       }
       else if ( r_string.equals( GROUPS_CHANGED )    ||
                 r_string.equals( SELECTION_CHANGED ) ||
                 r_string.equals( FIELD_CHANGED )     ||
                 r_string.equals( ATTRIBUTE_CHANGED )  ) 
       {
         viewer.redraw( (String)reason );
       }
       else
         System.out.println("Message " + reason + " not handled for dataSet "+
                            "in ViewManager.update()");
     }     

     else if ( observed == tempDataSet )    // message about temporary dataSet
     {                                      // translate to original dataSet
                                            // and notify it's observers
       if ( r_string.equals( POINTED_AT_CHANGED )) 
       {
         if ( !link_viewers_button.getState() )      // call redraw for tempDS 
         {
           viewer.redraw( r_string );
           return;
         }

         int i = tempDataSet.getPointedAtIndex();
         if ( i != DataSet.INVALID_INDEX ) 
           dataSet.setPointedAtIndex( original_index[i] ); 

         float new_x = tempDataSet.getPointedAtX();
         float orig_x = new_x;
         if ( conversion_operator != null && !Float.isNaN(new_x) )
           orig_x = solve( new_x );
         dataSet.setPointedAtX( orig_x ); 
         dataSet.notifyIObservers( POINTED_AT_CHANGED );
       }

       else if ( r_string.equals( SELECTION_CHANGED )) 
                                                  // synchonize selections and
       {                                          // notify dataSet's observers 
         for ( int i = 0; i < tempDataSet.getNum_entries(); i++ )
           dataSet.setSelectFlag(original_index[i], 
                                 tempDataSet.getData_entry(i) ); 

         dataSet.notifyIObservers( reason );
       }

       else if ( r_string.equals( XScaleChooserUI.N_STEPS_CHANGED ) ||
                 r_string.equals( XScaleChooserUI.X_RANGE_CHANGED )  )
       {
         if ( conversion_operator == null )
           viewer.redraw( (String)reason );
         else
         {
           makeTempDataSet( false );
           viewer.setDataSet( tempDataSet );
         } 
       }

       else
         System.out.println("Message "+reason+" not handled for tempDataSet "+
                            "in ViewManager.update()");
     }


     else
     {
       System.out.println("ERROR: bad DataSet in ViewManager.update()" );
     }
   }

  /**
   *  Trace the finalization of objects
   */
/*
  protected void finalize() throws IOException
  {
    System.out.println( "finalize ViewManager" );
  }
*/

/* --------------------------------------------------------------------------
 *
 *  Private Methods
 */

  /**
   *  Destroy the current ViewManager and remove it from the list of
   *  observers of the current DataSet when window closing event is received.
   */
   private void free_resources()
   {
     dataSet.deleteIObserver( this );
     tempDataSet.deleteIObserver( this );
     viewer = null;
     dispose();
     System.gc();
   }


   private void makeTempDataSet( boolean use_default_conversion_range )
   {
                                                // degnerate case, use original
     if ( dataSet == null || dataSet.getNum_entries() <= 0 )       
     {
       tempDataSet = dataSet;
       return;
     }                                          // otherwise, fabricate a new
                                                // data set and original_index
                                                // list

                                           // first copy the non-hidden spectra
     tempDataSet    = dataSet.empty_clone();
     original_index = new int[ dataSet.getNum_entries() ];
     new_index      = new int[ dataSet.getNum_entries() ];
     int  num_new   = 0;
     Data d;
     for ( int i = 0; i < dataSet.getNum_entries(); i++ )
     { 
       d = dataSet.getData_entry( i );
       if ( !d.isHidden() || show_all_button.getState() )
       {
         tempDataSet.addData_entry( d );
         original_index[ num_new ] = i;
         new_index[i] = num_new;
         num_new++; 
       }
       else
         new_index[i] = DataSet.INVALID_INDEX; // since not in tempDataSet
     }
                                               // do the conversion and record
     if ( conversion_operator != null )        // the indices, unchanged 
     {
       DataSetOperator op = tempDataSet.getOperator( CurrentConversionName() ); 
       
       UniformXScale x_scale;
       XScale temp_scale = viewer.getXConversionScale();
       if ( temp_scale == null )
         x_scale = null;
       else if ( temp_scale instanceof UniformXScale )
         x_scale = (UniformXScale)temp_scale;
       else
         x_scale = new UniformXScale( temp_scale.getStart_x(),
                                      temp_scale.getEnd_x(),
                                      temp_scale.getNum_x() );
         
       if ( x_scale == null || use_default_conversion_range ) 
       {
         op.setDefaultParameters();
         IParameter p = op.getParameter(2);                // 0 means use the
         if ( p.getName().equals( Parameter.NUM_BINS ))   // number of bins
           p.setValue( new Integer( 0 ) );                // in the DataSet
       }
       else
       {                                           // try to set the parameters
         IParameter p = op.getParameter(2);
         if ( !p.getName().equals( Parameter.NUM_BINS ))
           op.setDefaultParameters();              // fall back to defaults
         else
         {
           p.setValue( new Integer( x_scale.getNum_x() ) );

           p = op.getParameter(0);
           p.setValue( new Float( x_scale.getStart_x() ) );

           p = op.getParameter(1);
           p.setValue( new Float( x_scale.getEnd_x() ) );
         }
       }  

       Object result = op.getResult();
       if ( result instanceof DataSet )
       {
         tempDataSet = (DataSet)op.getResult();
         if ( tempDataSet.getNum_entries() == num_new )
                                            // we didn't lose Data blocks, so
         {
           for ( int i = 0; i < num_new; i++ ) // preserve the selection flags
           {
             d = dataSet.getData_entry( original_index[i] );
             tempDataSet.setSelectFlag( i, d );
           }
                                                // preserve the pointed at index
                                                // if possible
           int k = dataSet.getPointedAtIndex(); 
           if ( k != DataSet.INVALID_INDEX )
             if ( new_index[k] != DataSet.INVALID_INDEX )
               tempDataSet.setPointedAtIndex( new_index[k] );
         }
       }
       else
         tempDataSet = dataSet.empty_clone();
    }

     tempDataSet.addIObserver( this );
   }


private void BuildFileMenu()
{
                                                // set up file menu items
  FileMenuHandler file_menu_handler = new FileMenuHandler();
  JMenu file_menu = viewer.getMenuBar().getMenu(DataSetViewer.FILE_MENU_ID);

  JMenuItem button = new JMenuItem( SAVE_NEW_DATA_SET );
  button.addActionListener( file_menu_handler );
  file_menu.add( button );

  button = new JMenuItem( CLOSE_LABEL );
  button.addActionListener( file_menu_handler );
  file_menu.add( button );
}

private void BuildEditMenu()
{
                                                // set up edit menu items
  EditMenuHandler edit_menu_handler = new EditMenuHandler();
  JMenu edit_menu = viewer.getMenuBar().getMenu(DataSetViewer.EDIT_MENU_ID);

  JMenu group_menu = new JMenu( SUM_MENU );            // group menu
  edit_menu.add( group_menu );

  JMenuItem button = new JMenuItem( SUM_SELECTED );
  button.addActionListener( edit_menu_handler );
  group_menu.add( button );

  button = new JMenuItem( SUM_UNSELECTED );
  button.addActionListener( edit_menu_handler );
  group_menu.add( button );

  JMenu delete_menu = new JMenu( DELETE_MENU );            // delete menu
  edit_menu.add( delete_menu );

  button = new JMenuItem( DELETE_SELECTED );
  button.addActionListener( edit_menu_handler );
  delete_menu.add( button );

  button = new JMenuItem( DELETE_UNSELECTED );
  button.addActionListener( edit_menu_handler );
  delete_menu.add( button );

  JMenu clear_menu = new JMenu( CLEAR_MENU );            // clear menu
  edit_menu.add( clear_menu );

  button = new JMenuItem( CLEAR_SELECTED );
  button.addActionListener( edit_menu_handler );
  clear_menu.add( button );

                                              // Add sort options
  if ( dataSet.getNum_entries() > 1 )
  {
    JMenu sort_menu = new JMenu( "Sort by..." );
    edit_menu.add( sort_menu );
    AttributeList attr_list = dataSet.getData_entry(0).getAttributeList();
    for ( int i = 0; i < attr_list.getNum_attributes(); i++ )
    {
      Attribute attr = attr_list.getAttribute(i);
      button = new JMenuItem( attr.getName() );
      button.addActionListener( edit_menu_handler );
      sort_menu.add( button );
    }
  }
}

private void BuildViewMenu()
{                                                   // set up view menu items
  ViewMenuHandler view_menu_handler = new ViewMenuHandler();
  JMenu view_menu = viewer.getMenuBar().getMenu(DataSetViewer.VIEW_MENU_ID);

  JMenuItem button = new JMenuItem( ADDITIONAL_VIEW );
  button.addActionListener( view_menu_handler );
  view_menu.add( button );

  button = new JMenuItem( IMAGE );
  button.addActionListener( view_menu_handler );
  view_menu.add( button );

  button = new JMenuItem( THREE_D );
  button.addActionListener( view_menu_handler );
  view_menu.add( button );
  
  button = new JMenuItem( CONTOUR );
  button.addActionListener( view_menu_handler );
  view_menu.add( button );

  button = new JMenuItem( SCROLLED_GRAPHS );
  button.addActionListener( view_menu_handler );
  view_menu.add( button );

  button = new JMenuItem( SELECTED_GRAPHS );
  button.addActionListener( view_menu_handler );
  view_menu.add( button );

  button = new JMenuItem( SELECTED_GRAPH2 );
  button.addActionListener( view_menu_handler );
  view_menu.add( button );
  
  JMenu Tables = new JMenu( "Selected Table View");
  view_menu.add( Tables);
  
  BuildTableMenu( Tables);

  button = new JMenuItem( TABLE );
  button.addActionListener( view_menu_handler );
  view_menu.add( button );
}

 public void BuildTableMenu( JMenu Tables )
 { 
    int n= TableViewMenuComponents.getNMenuItems();
    ViewMenuHandler view_menu_handler = new ViewMenuHandler();
    if( table_MenuComp == null)
      table_MenuComp = new TableViewMenuComponents();
   
    table_MenuComp.addMenuItems( Tables , view_menu_handler);
    
   /* Tables.addSeparator();  
    JMenuItem button;
    button = new JMenuItem( "Advanced Table");
    button.addActionListener( view_menu_handler );
    Tables.add( button );
   */
 }

/*
 * Build the menu of conversion options and turn on the radio button for the 
 * currently active conversion operator.
 */
private void BuildConversionsMenu()
{ 
  JMenu conversion_menu = new JMenu("Axis Conversions...");
  ConversionMenuHandler conversion_menu_handler = new ConversionMenuHandler();
  ButtonGroup group = new ButtonGroup();

  JRadioButtonMenuItem button = new JRadioButtonMenuItem(NO_CONVERSION_OP);
  if ( CurrentConversionName().equals( NO_CONVERSION_OP ))
    button.setSelected(true);
  button.addActionListener( conversion_menu_handler );
  conversion_menu.add( button );
  group.add( button );

  DataSetOperator op;

  int n_ops         = dataSet.getNum_operators();
  for ( int i = 0; i < n_ops; i++ )
  {
    op = dataSet.getOperator(i);
    if ( op instanceof XAxisConversionOp )
    {
      button = new JRadioButtonMenuItem( op.getTitle() );
      button.addActionListener( conversion_menu_handler );
      if ( CurrentConversionName().equals( op.getTitle() ))
        button.setSelected(true);

      conversion_menu.add( button );     
      group.add( button );
    }
  } 

  JMenu view_menu = viewer.getMenuBar().getMenu(DataSetViewer.VIEW_MENU_ID);
  view_menu.add( conversion_menu );         
}


private void BuildOptionMenu()
{
                                                // set up option menu items
  OptionMenuHandler option_menu_handler = new OptionMenuHandler();
  JMenu option_menu = viewer.getMenuBar().getMenu(DataSetViewer.OPTION_MENU_ID);

  link_viewers_button = new JCheckBoxMenuItem(LINK_VIEWS);
  link_viewers_button.addActionListener( option_menu_handler );
  link_viewers_button.setState( true );
  option_menu.add( link_viewers_button );
/*
  show_all_button = new JCheckBoxMenuItem(SHOW_ALL);
  show_all_button.addActionListener( option_menu_handler );
  show_all_button.setState( false );
  option_menu.add( show_all_button );
*/
}


private String CurrentConversionName()     // get current conversion name
{
  String name;     
  if ( conversion_operator == null )
    name = NO_CONVERSION_OP;
  else
    name = conversion_operator.getTitle();
 
  return name;
}


private float solve( float new_x ) // find what x in the original DataSet maps
{                                  // maps to new_x in converted tempDataSet
  if ( conversion_operator == null )
    return new_x;

  int index = dataSet.getPointedAtIndex();
  XScale x_scale = dataSet.getData_entry(index).getX_scale();
  float a = x_scale.getStart_x();
  float b = x_scale.getEnd_x();
  float f_a = conversion_operator.convert_X_Value( a, index );
  float f_b = conversion_operator.convert_X_Value( b, index );

  float f_min = Math.min( f_a, f_b );
  float f_max = Math.max( f_a, f_b );
  if ( new_x <= f_min )                // clamp the values at the ends of the
  {                                    // Data blocks.  There are two cases,
    if ( f_a <= f_b )                  // since the conversion may reverse the
      return a;                        // order.
    else
      return b;
  }

  if ( new_x >= f_max )
  {
    if ( f_a <= f_b )
      return b;
    else
      return a;
  }

  ConversionFunction f =
                  new ConversionFunction(index, conversion_operator, new_x );

  return (float)NumericalAnalysis.BisectionMethod( f, a, b, 20 );
}


  public class ConversionFunction implements IOneVariableFunction
  {
    private  int               index;
    private  XAxisConversionOp op;
    private  float             new_x;

    public  ConversionFunction( int               index, 
                                XAxisConversionOp op, 
                                float             new_x )
    {
      this.index = index;
      this.op    = op;
      this.new_x = new_x;
    }

    public double getValue( double x )
    {
      return ( new_x - op.convert_X_Value( (float)x, index ) );
    }
  }


/* -------------------------------------------------------------------------
 *
 *   Event handling objects for the Frame and menu bar
 */

  private class FileMenuHandler implements ActionListener,
                                           Serializable
  {
    public void actionPerformed( ActionEvent e )
    {
      String action = e.getActionCommand();

      if ( action.equals( CLOSE_LABEL ))
        destroy();
      else if ( action.equals( SAVE_NEW_DATA_SET ))
      {
        DataSet new_ds = (DataSet)tempDataSet.clone();
        dataSet.notifyIObservers( new_ds );
      }
      else
        System.out.println( action );
    }
  }

  private class EditMenuHandler implements ActionListener,
                                           Serializable
  {
    public void actionPerformed( ActionEvent e )
    {
      String action = e.getActionCommand();
      boolean changed;

      if ( action.equals( SUM_SELECTED ))
      {
        DataSetOperator op = new SumCurrentlySelected( dataSet, true, false );
        op.getResult();
        dataSet.notifyIObservers( IObserver.DATA_DELETED );
      }
      else if ( action.equals( SUM_UNSELECTED ))
      {
        DataSetOperator op = new SumCurrentlySelected( dataSet, false, false );
        op.getResult();
        dataSet.notifyIObservers( IObserver.DATA_DELETED );
        dataSet.notifyIObservers( IObserver.SELECTION_CHANGED );
      }

/* //  delete by hiding...
      else if ( action.equals( DELETE_SELECTED ))
      {
         dataSet.hideSelected( true );
         dataSet.notifyIObservers( IObserver.HIDDEN_CHANGED );
         if ( dataSet.clearSelections() )
           dataSet.notifyIObservers( IObserver.SELECTION_CHANGED );
      }
      else if ( action.equals( DELETE_UNSELECTED ))
      {
         dataSet.hideSelected( false );
         dataSet.notifyIObservers( IObserver.HIDDEN_CHANGED );
         if ( dataSet.clearSelections() )
           dataSet.notifyIObservers( IObserver.SELECTION_CHANGED );
      }
*/
   // delete for real
      else if ( action.equals( DELETE_SELECTED ))
      {
        DataSetOperator op = new DeleteCurrentlySelected( 
                                              dataSet, true, false );
        op.getResult();
        dataSet.notifyIObservers( IObserver.DATA_DELETED );
      }
      else if ( action.equals( DELETE_UNSELECTED ))
      {
        DataSetOperator op = new DeleteCurrentlySelected( 
                                              dataSet, false, false );
        op.getResult();
        dataSet.notifyIObservers( IObserver.DATA_DELETED );
        dataSet.notifyIObservers( IObserver.SELECTION_CHANGED );
      }
      else if ( action.equals( CLEAR_SELECTED ))
      {
        if ( tempDataSet.clearSelections() || dataSet.clearSelections() )
          dataSet.notifyIObservers( IObserver.SELECTION_CHANGED );
      } 
      else 
      {
        DataSetOperator op = new DataSetSort( dataSet, action, true, false );
        op.getResult();
      }
    }
  }

  private class OptionMenuHandler implements ActionListener,
                                             Serializable
  {
    public void actionPerformed( ActionEvent e )
    {
      String action = e.getActionCommand();
      if ( action.equals( SHOW_ALL ) && dataSet.getNumHidden() > 0 )
      {
         makeTempDataSet( false ); 
         viewer.setDataSet( tempDataSet );
      }
    }
  }


  private class ViewMenuHandler implements ActionListener,
                                           Serializable
  {  
     boolean errors = false, 
              index = false;
    
    public void actionPerformed( ActionEvent e )
    {
      String action = e.getActionCommand();
      if ( action.equals( ADDITIONAL_VIEW ) )
      {
        ViewManager vm = new ViewManager( dataSet, viewType );
      }
      else
        setView( action ); 
    }
  }

  private class ConversionMenuHandler implements ActionListener,
                                                 Serializable
  {
    public void actionPerformed( ActionEvent e )
    {
      String action  = e.getActionCommand();
                                                 // if the request is differnt
                                                 // do the new conversion
      if ( !action.equals( CurrentConversionName() ) ) 
      {
        JRadioButtonMenuItem button = (JRadioButtonMenuItem)e.getSource();
        button.setSelected(true);
        conversion_operator = (XAxisConversionOp)dataSet.getOperator( action ); 
        makeTempDataSet( true );
        viewer.setDataSet( tempDataSet );
      }
    }
  }

}
