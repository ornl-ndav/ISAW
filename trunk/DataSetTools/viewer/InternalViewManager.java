/*
 * @(#)InternalViewManager.java  
 *
 * Programmer:  Dennis Mikkelson
 *
 *  $Log$
 *  Revision 1.3  2001/01/29 21:28:01  dennis
 *  Print error message if the DataSet is null in the
 *  constructor.
 *  Also, now uses CVS version numbers.
 *
 *  Revision 1.2  2000/12/07 23:09:06  dennis
 *  Now includes basic support for maintaining ViewerState.
 *  Also refined some reasons for doing an update.
 *
 *  Revision 1.1  2000/07/10 22:59:15  dennis
 *  July 10, 2000 version... many changes, switched to CVS
 *
 *  Revision 1.18  2000/06/14 22:18:46  dennis
 *  changed order of parameters to SumCurrentlySelected() since the operator
 *  was changed.
 *
 *  Revision 1.17  2000/06/12 19:53:58  dennis
 *  Now implements Serializable and handles DATA_CHANGED notifications
 *
 *  Revision 1.20  2000/05/18 20:58:56  dennis
 *  made default Frame size slightly larger and removed unused "Show All"
 *  option.
 *
 *  Revision 1.19  2000/05/16 22:35:24  dennis
 *  Added code to get the XScale to use for X-axis conversions from the
 *  viewer.  Changed makeTempDataSet() to accept a boolean parameter that
 *  determines whether or not the default parameters of the conversion operator
 *  should be used.
 *
 *  Revision 1.18  2000/05/11 15:19:52  dennis
 *  Added RCS logging.
 *
 *  Modified:
 *   1.1 2000/03/30  Dennis Mikkelson
 *                   Changed it to pass a second "tempDataSet" to the viewers,
 *                   so that axis conversions can be done and hidden Data
 *                   blocks can be omitted.
 *  1.11 2000/04/04  Dennis Mikkelson
 *                   Added translation arrays between indices in the original
 *                   DataSet and tempDataSet. "Pointed At" messages are now
 *                   transferred from one to the other.
 *  1.12 2000/04/11  Added edit menu options to Delete, Hide, Group the
 *                   un-selected items, as well as the selected items.
 *  1.13 2000/04/28  Temporarily removed Group and Hide operations.  Added
 *                   option to save the tempDataSet to the tree as a new
 *                   DataSet
 *  1.14 2000/05/10  Sum selected groups and delete selected groups are not
 *                   implemented using operators.
 */
 
package DataSetTools.viewer;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;
import DataSetTools.viewer.util.*;
import DataSetTools.viewer.Graph.*;
import DataSetTools.viewer.Image.*;
//import OverplotView.*;                         // import this for Kevin's viewer
import DataSetTools.viewer.ViewerTemplate.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *  An InternalViewManager object manages viewers for a DataSet in an internal 
 *  frame.  It contains a menu bar that allows the user to select the type 
 *  of viewer to be used to view the DataSet and appropriate view options.  
 *  It also is an observer of the DataSet and will be notified of changes 
 *  in the DataSet.  Since an InternalViewManager is a JInternalFrame, it can 
 *  be closed by the user, or by the program.
 */

public class InternalViewManager extends    JInternalFrame
                                 implements IViewManager,
                                            Serializable
{
   private   InternalViewManager     view_manager = null;
   private   DataSetViewer   viewer;
   private   ViewerState     state;
   private   DataSet         dataSet;
   private   DataSet         tempDataSet;
   private   DataSetOperator conversion_operator = null;
   private   int[]           original_index;      // records the index in the
                                                  // original dataSet that 
                                                  // corresponds to an index in
                                                  // the tempDataSet; 
   private   int[]           new_index;           // records the index in
                                                  // tempDataSet that 
                                                  // corresponds to an index in
                                                  // the original DataSet  
   private JCheckBoxMenuItem show_all_button;

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
   private static final String NO_CONVERSION_OP     = "None";
    
   /**  
    *  Accepts a DataSet and view type and creates an instance of a 
    *  InternalViewManager for the data set.  
    *
    *  @param  ds        The DataSet to be viewed
    *  @param  view_type String describing the initial type of viewer to be 
    *                    used.  The valid strings are listed in the interface,
    *                    IViewManager
    * 
    *  @see IViewManager
    *  @see ViewManager
    *  @see DataSetViewer
    *  @see DataSet
    */
   public InternalViewManager(DataSet ds, String view_type )
   {
      view_manager = this;
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      
      dataSet = ds; 
      if ( ds == null )
         System.out.println("ERROR: ds is null in ViewManager constructor");
      dataSet.addIObserver( this );

      addInternalFrameListener(new InternalFrameAdapter()
      {
        public void internalFrameClosing(InternalFrameEvent e)
        {
          destroy();
        }
      });

      setBounds(0,0,600,425);
      makeTempDataSet( true );
      setView( view_type ); 
      setVisible(true);
      conversion_operator = null;
   }

   /**
    *  Specify a new DataSet to be viewed.
    *
    *  @param  ds   The new DataSet to be used by this InternalViewManager
    */ 
   public void setDataSet( DataSet ds )
   {
     dataSet.deleteIObserver( this );
     dataSet = ds;
     makeTempDataSet( true );
     ds.addIObserver( this );

     viewer.setDataSet( tempDataSet ); 
   }

   /**
    *  Get the DataSet that is currently used by this InternalViewManager
    */   
   public DataSet getDataSet()
   {
     return dataSet;
   }

   /**
    *  Set a new viewer type for this InternalViewManager.  The available view 
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

     viewer = null;
      if ( view_type == IMAGE )
        viewer = new ImageView( tempDataSet, state );
      else if ( view_type == SCROLLED_GRAPHS )
        viewer = new GraphView( tempDataSet, state );
      else if ( view_type == SELECTED_GRAPHS )                  // Use either
//        viewer = new SelectedGraphView( tempDataSet );          // Kevin's or
        viewer = new ViewerTemplate( tempDataSet, state );      // Template  
      else
      {
        System.out.println( 
                  "ERROR: Unsupported view type in InternalViewManager:" );
        System.out.println( "      " + view_type );
        System.out.println( "using " + IMAGE + " by default" );
        viewer = new ImageView( tempDataSet, state );
      }
      getContentPane().add(viewer);
      getContentPane().setVisible(true);

      setJMenuBar( viewer.getMenuBar() );
      BuildFileMenu();
      BuildEditMenu();
      BuildViewMenu();
      BuildConversionsMenu();
      BuildOptionMenu();
   }

  /**
   *  Destroy the current InternalViewManager and remove it from the list of 
   *  observers of the current DataSet.
   */
   public void destroy()
   {
     dataSet.deleteIObserver( view_manager );
     tempDataSet.deleteIObserver( view_manager );
     viewer = null;
     dispose(); 
   }

   /**
    *  Update the InternalViewManager due to a change in the DataSet.  This 
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
     if ( !( reason instanceof String) )   // we only deal with Strings
     {
//     System.out.println("ERROR: InternalViewManager update called with wrong reason");
       return;
     }
 
     if ( observed == dataSet )             // message about original dataSet
     {
       if ( (String)reason == DESTROY )
         destroy();

       else if ( (String)reason == DATA_DELETED   ||
                 (String)reason == DATA_REORDERED ||
                 (String)reason == DATA_CHANGED   ||
                 (String)reason == HIDDEN_CHANGED   )
       {
         makeTempDataSet( false );
         viewer.setDataSet( tempDataSet );
       }
       else if ( (String)reason == POINTED_AT_CHANGED   )
       {                                             // tell the viewer the new
         int index = dataSet.getPointedAtIndex();    // "pointed at" index if
         if ( index != DataSet.INVALID_INDEX )       //  valid and different 
           if ( new_index[ index ] != DataSet.INVALID_INDEX )
             if ( new_index[ index ] != tempDataSet.getPointedAtIndex() )
             {
               tempDataSet.setPointedAtIndex( new_index[ index ] );
               viewer.redraw( (String)reason );
             }  
       }
       else if ( (String)reason == GROUPS_CHANGED    ||
                 (String)reason == SELECTION_CHANGED ||
                 (String)reason == FIELD_CHANGED     ||
                 (String)reason == ATTRIBUTE_CHANGED  )
       {
         viewer.redraw( (String)reason );
       }
       else
         System.out.println("Message " + reason + " not handled for dataSet "+
                            "in InternalViewManager.update()");
     }     

     else if ( observed == tempDataSet )    // message about temporary dataSet
     {                                      // translate to original dataSet
                                            // and notify it's observers
       if ( (String)reason == POINTED_AT_CHANGED ) 
       {
         int i = tempDataSet.getPointedAtIndex();
         dataSet.setPointedAtIndex( original_index[i] ); 
         dataSet.notifyIObservers( POINTED_AT_CHANGED );
       }

       else if ( (String)reason == SELECTION_CHANGED ) 
                                                  // synchonize selections and
       {                                          // notify dataSet's observers 
         for ( int i = 0; i < tempDataSet.getNum_entries(); i++ )
           dataSet.setSelectFlag(original_index[i], 
                                 tempDataSet.getData_entry(i) ); 

         dataSet.notifyIObservers( reason );
       }

       else if ( (String)reason == DataSetViewer.BINS_CHANGED ||
                 (String)reason == DataSetViewer.X_RANGE_CHANGED    )
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
                            "in InternalViewManager.update()");
     }


     else
     {
       System.out.println("ERROR: bad DataSet in InternalViewManager.update()" );
     }
   }

  /**
   *  Trace the finalization of objects
   */
/*
  protected void finalize() throws IOException
  {
    System.out.println( "finalize InternalViewManager" );
  }
*/

/* --------------------------------------------------------------------------
 *
 *  Private Methods
 */

   private void makeTempDataSet( boolean use_default_conversion_range )
   {
     if ( dataSet.getNum_entries() <= 0 )       // degnerate case, use original
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
       
       UniformXScale x_scale = viewer.getXConversionScale();
       if ( x_scale == null || use_default_conversion_range ) 
         op.setDefaultParameters();
       else
       {                                           // try to set the parameters
         Parameter p = op.getParameter(2);
         if ( p.getName() != Parameter.NUM_BINS )
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

       tempDataSet = (DataSet)op.getResult();
       for ( int i = 0; i < num_new; i++ )     // preserve the selection flags
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
{
                                                // set up view menu items
  ViewMenuHandler view_menu_handler = new ViewMenuHandler();
  JMenu view_menu = viewer.getMenuBar().getMenu(DataSetViewer.VIEW_MENU_ID);

  JMenuItem button = new JMenuItem( IMAGE );
  button.addActionListener( view_menu_handler );
  view_menu.add( button );

  button = new JMenuItem( SCROLLED_GRAPHS );
  button.addActionListener( view_menu_handler );
  view_menu.add( button );

  button = new JMenuItem( SELECTED_GRAPHS );
  button.addActionListener( view_menu_handler );
  view_menu.add( button );
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
  if ( CurrentConversionName() == NO_CONVERSION_OP )
    button.setSelected(true);
  button.addActionListener( conversion_menu_handler );
  conversion_menu.add( button );
  group.add( button );

  DataSetOperator op;

  int n_ops         = dataSet.getNum_operators();
  for ( int i = 0; i < n_ops; i++ )
  {
    op = dataSet.getOperator(i);
    if ( op.getCategory() == Operator.X_AXIS_CONVERSION )
    {
      button = new JRadioButtonMenuItem( op.getTitle() );
      button.addActionListener( conversion_menu_handler );
      if ( CurrentConversionName() == op.getTitle() )
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
/*
                                                // set up option menu items
  OptionMenuHandler option_menu_handler = new OptionMenuHandler();
  JMenu option_menu = viewer.getMenuBar().getMenu(DataSetViewer.OPTION_MENU_ID);

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
      if ( action == CLOSE_LABEL )
        destroy();
      else if ( action == SAVE_NEW_DATA_SET )
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

      if ( action == SUM_SELECTED )
      {
        DataSetOperator op = new SumCurrentlySelected( dataSet, true, false );
        op.getResult();
        dataSet.notifyIObservers( IObserver.DATA_DELETED );
      }
      else if ( action == SUM_UNSELECTED )
      {
        DataSetOperator op = new SumCurrentlySelected( dataSet, false, false );
        op.getResult();
        dataSet.notifyIObservers( IObserver.DATA_DELETED );
        dataSet.notifyIObservers( IObserver.SELECTION_CHANGED );
      }

/* //  delete by hiding...
      else if ( action == DELETE_SELECTED )
      {
         dataSet.hideSelected( true );
         dataSet.notifyIObservers( IObserver.HIDDEN_CHANGED );
         if ( dataSet.clearSelections() )
           dataSet.notifyIObservers( IObserver.SELECTION_CHANGED );
      }
      else if ( action == DELETE_UNSELECTED )
      {
         dataSet.hideSelected( false );
         dataSet.notifyIObservers( IObserver.HIDDEN_CHANGED );
         if ( dataSet.clearSelections() )
           dataSet.notifyIObservers( IObserver.SELECTION_CHANGED );
      }
*/
   // delete for real
      else if ( action == DELETE_SELECTED )
      {
        DataSetOperator op = new DeleteCurrentlySelected( 
                                              dataSet, true, false );
        op.getResult();
        dataSet.notifyIObservers( IObserver.DATA_DELETED );
      }
      else if ( action == DELETE_UNSELECTED )
      {
        DataSetOperator op = new DeleteCurrentlySelected( 
                                              dataSet, false, false );
        op.getResult();
        dataSet.notifyIObservers( IObserver.DATA_DELETED );
        dataSet.notifyIObservers( IObserver.SELECTION_CHANGED );
      }
      else if ( action == CLEAR_SELECTED )
      {
        if ( dataSet.clearSelections() )
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
      if ( action == SHOW_ALL && dataSet.getNumHidden() > 0 )
      {
         makeTempDataSet( false ); 
         viewer.setDataSet( tempDataSet );
      }
    }
  }


  private class ViewMenuHandler implements ActionListener,
                                           Serializable
  {
    public void actionPerformed( ActionEvent e )
    {
      String action = e.getActionCommand();
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
      if ( action != CurrentConversionName() ) 
      {
        JRadioButtonMenuItem button = (JRadioButtonMenuItem)e.getSource();
        button.setSelected(true);
        conversion_operator = dataSet.getOperator( action );  
        makeTempDataSet( true );
        viewer.setDataSet( tempDataSet );
      }
    }
  }

}
