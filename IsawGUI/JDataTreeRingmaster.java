package IsawGUI;

/*
 * $Id$
 *
 *
 * $Log$
 * Revision 1.8  2001/07/23 14:05:26  neffk
 * continued to replace low-level JTree modifcations with methods
 * in JDataTree.  clearing selections and deleting nodes are now internal
 * to JDataTree, making this class much easier to maintain.
 *
 * Revision 1.7  2001/07/20 16:44:09  neffk
 * reflected changes made in JDataTree.  tree is more encapsulated,
 * so some methods had to be changed to use JDataTree methods instead
 * of .getTree(), getModel(), and the like.
 *
 * Revision 1.6  2001/07/18 16:58:08  neffk
 * constructor now takes a JDataTree instead of a JTreeUI.  many other
 * changes have been made in the process of switching over to JDataTree,
 * mostly calling JDataTRee methods to do low-level things that were
 * formerly handled here.
 *
 * Revision 1.5  2001/07/11 16:40:37  neffk
 * modified to reflect change in JOperationsMenuHandler's constructor's
 * arguments.
 *
 * Revision 1.4  2001/07/02 20:27:36  neffk
 * now right-click menu allows the user to delete a single DataSet from the
 * tree.  also, the framework for manipulating runfile entries in the
 * data tree was added.
 *
 * Revision 1.3  2001/06/28 19:16:44  neffk
 * karma boost
 *
 * Revision 1.2  2001/06/27 20:19:36  neffk
 * added the appropriate constructor so that JDataTree and CommandPane can
 * remain up to date when the menus provided by this class change the DataSet
 * objects.
 *
 * Revision 1.1  2001/06/25 21:30:54  neffk
 * handles all menus and operations for JDataTree.
 *
 */


import DataSetTools.components.ui.OperatorMenu;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.util.IObserver;
import DataSetTools.operator.Operator;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.text.Document;

/**
 * encapsulates all event handling for a JDataTree.  generates
 * appropriate right-click menus for selections and handles the
 * events generated by the menus.  this includes selecting and
 * clearing selection of Data and DataSet objects, deleting tree
 * nodes, applying operators to DataSet objects, creating views
 * of DataSet objects, and all of the other actions that are
 * associated with the generated right-click menu.
 */
public class JDataTreeRingmaster
{

  final String MENU_SELECT    = "Select";
  final String MENU_CLEAR     = "Clear";
  final String MENU_CLEAR_ALL = "Clear All Selections";
  final String MENU_DELETE    = "Delete";


  JDataTree tree;                
  Document sessionLog = null;    //need this because of some 
                                 //unpleasant coupling of the tree,
                                 //the command pane, and various other stuff...


  JDataTreeRingmaster( JDataTree tree )
  {
    this.tree = tree;
  }


  JDataTreeRingmaster( JDataTree tree, Document log )
  {
    this.tree = tree;
    sessionLog = log;
  }


  /**
   * decides what kind of menu should be generated.  there are five (5)
   * types of menus that can be generated:
   *
   *   0) single Data object selection
   *   1) multiple Data object selection
   *   2) single DataSet object selection
   *   3) multiple DataSet object selection
   *   4) runfile selection
   *
   * all selected nodes of unknown types are ignored.
   */
  public void generatePopupMenu( TreePath[] tps, MouseEvent e )
  {
    MutableTreeNode node = null;
    if(  tps.length > 0  )
      node = (MutableTreeNode)(  tps[0].getLastPathComponent()  );

                                              //generates a popup menu
                                              //for single Data object
                                              //selections
    if(   node instanceof DataMutableTreeNode  &&  tps.length == 1   )
      SingleDataBlockPopupMenu( tps, e );

                                              //generates a popup menu
                                              //for single DataSet object
                                              //selections
    else if(  node instanceof DataSetMutableTreeNode  &&  tps.length == 1  )
      SingleDataSetPopupMenu( tps, e );

                                               //generates a popup menu
                                               //for multiple Data object
                                               //selections
    else if(  node instanceof DataMutableTreeNode  )
    {
      DataSet ds = tree.getDataSet( node );
      TreePath[] long_data_tps = new TreePath[ tps.length ];

      int data_count = 0;
      for( int i=0;  i<tps.length;  i++ )
      {
        node = (MutableTreeNode)(  tps[i].getLastPathComponent()  );
 
                                         //ignore all selections
                                         //that are not Data 
        if(  node instanceof DataMutableTreeNode  )
        {
                                         //only use first 
                                         //DataSet's Data.
          DataSet new_ds = tree.getDataSet( node );
          if(  ds.equals( new_ds )  )
            long_data_tps[ data_count++ ] = tps[i];
        }
      }

                                       //simlify...
      TreePath[] short_data_tps = new TreePath[ data_count ];
      for( int i=0;  i<short_data_tps.length;  i++ )
        short_data_tps[i] = long_data_tps[i];

      MultipleDataBlockPopupMenu( short_data_tps, e );
    }


                                               //generates a popup menu
                                               //for multiple DataSet object
                                               //selections
    else if(  node instanceof DataSetMutableTreeNode  )
    {

                  //this option isn't really used because currently there isn't
                  //anything that's appropriate to do for all DataSet objects
                  //for now, we'll just grab the first selection and use the
                  //menu for single DataSet object selections.
      //MultipleDataSetPopupMenu( tps, e );
      SingleDataSetPopupMenu( tps, e );
    }


                                               //generates a popup menu
                                               //for Experiment object
                                               //selections
    else if(  node instanceof Experiment  )
    {
      System.out.println( "Experiment selected in tree" );
    }


    else 
    {
      System.out.println( "type not appropriate for actionMenu" );
    }
  }


  /**
   * creates a popup menu that is appropriate for a single
   * Data object when the user right-clicks on it.
   */
  public void SingleDataBlockPopupMenu( TreePath[] tps, MouseEvent e )
  {

    class SingleDataBlockMenuItemListener
      implements ActionListener
    {
      TreePath[] tps;

      public SingleDataBlockMenuItemListener( TreePath[] tps_ )
      {
        tps = tps_;
      }

      /*
       * trap mouse events for the right-click menus.
       */
      public void actionPerformed( ActionEvent item_e )
      {
        if(  item_e.getActionCommand() == MENU_SELECT  )
          tree.selectNodesWithPaths( tps );

        else if(  item_e.getActionCommand() == MENU_CLEAR  )
        {
          DataMutableTreeNode node = (DataMutableTreeNode)(  tps[0].getLastPathComponent()  );
          Data d = node.getUserObject();
          d.setSelected( false );

                                    //find the DataSet that these Data objects
                                    //belong to and have it notify its IObservers
          DataSet ds = tree.getDataSet( node );
          ds.notifyIObservers( IObserver.SELECTION_CHANGED );
        }

        else if(  item_e.getActionCommand() == MENU_CLEAR_ALL  )
          tree.clearSelections();

        else if(  item_e.getActionCommand() == MENU_DELETE  )
        {
          DataMutableTreeNode node = (DataMutableTreeNode)(  tps[0].getLastPathComponent()  );
          tree.deleteNode( node, true );
        }
      }
    }
    SingleDataBlockMenuItemListener item_listener = null;
    item_listener = new SingleDataBlockMenuItemListener( tps );

    JMenuItem select_item = new JMenuItem( MENU_SELECT );
              //select_item.setMnemonic( KeyEvent.VK_S );
              select_item.addActionListener( item_listener );
    JMenuItem clear_item = new JMenuItem( MENU_CLEAR );
              //clear_item.setMnemonic( KeyEvent.VK_BACK_SPACE );
              clear_item.addActionListener( item_listener );
    JMenuItem clear_all_item = new JMenuItem( MENU_CLEAR_ALL );
              //clear_all_item.setMnemonic( KeyEvent.VK_S );
              clear_all_item.addActionListener( item_listener );
    JMenuItem delete_item = new JMenuItem( MENU_DELETE );
              //delete_item.setMnemonic( KeyEvent.VK_X );
              delete_item.addActionListener( item_listener );
    JPopupMenu popup_menu = new JPopupMenu( "SingleDataBlockPopupMenu" );
               popup_menu.add( select_item );
               popup_menu.add( clear_item );
               popup_menu.add( clear_all_item );
               popup_menu.add( delete_item );
               popup_menu.show(  e.getComponent(), e.getX(), e.getY()  );
  }


  /**
   * creates a popup menu that is appropriate for multiple
   * Data objects when the user right-clicks on the highlighted
   * items.
   */
  public void MultipleDataBlockPopupMenu( TreePath[] tps, MouseEvent e )
  {

    class MultipleDataBlockMenuItemListener
      implements ActionListener
    {
      TreePath[] tps;

      public MultipleDataBlockMenuItemListener( TreePath[] tps )
      {
        this.tps = tps;
      }

      public void actionPerformed( ActionEvent item_e )
      {
        if(  item_e.getActionCommand() == MENU_SELECT  )
          tree.selectNodesWithPaths( tps );

        else if(  item_e.getActionCommand() == MENU_CLEAR  )
          tree.clearSelections();

        else if(  item_e.getActionCommand() == MENU_CLEAR_ALL  )
          tree.clearSelections();

        else if(  item_e.getActionCommand() == MENU_DELETE  )
          tree.deleteNodesWithPaths( tps );
      }
    }
    MultipleDataBlockMenuItemListener item_listener = null;
    item_listener = new MultipleDataBlockMenuItemListener( tps );

    JMenuItem select_item = new JMenuItem( MENU_SELECT );
              //select_item.setMnemonic( KeyEvent.VK_S );
              select_item.addActionListener( item_listener );
    JMenuItem clear_item = new JMenuItem( MENU_CLEAR );
              //clear_item.setMnemonic( KeyEvent.VK_BACK_SPACE );
              clear_item.addActionListener( item_listener );
    JMenuItem clear_all_item = new JMenuItem( MENU_CLEAR_ALL );
              //clear_all_item.setMnemonic( KeyEvent.VK_S );
              clear_all_item.addActionListener( item_listener );
    JMenuItem delete_item = new JMenuItem( MENU_DELETE );
              //delete_item.setMnemonic( KeyEvent.VK_X );
              delete_item.addActionListener( item_listener );
    JPopupMenu popup_menu = new JPopupMenu( "MultipleDataBlockPopupMenu" );
               popup_menu.add( select_item );
               popup_menu.add( clear_item );
               popup_menu.add( clear_all_item );
               popup_menu.add( delete_item );
               popup_menu.show(  e.getComponent(), e.getX(), e.getY()  );
  }


  /**
   * creates a popup menu that is appropriate for a single
   * DataSet object when the user right-clicks on highlighted
   * items.
   */
  public void SingleDataSetPopupMenu( TreePath[] tps, MouseEvent e )
  {
    DataSetMutableTreeNode node = (DataSetMutableTreeNode)(  tps[0].getLastPathComponent()  );
    DataSet ds = node.getUserObject();

    int num_ops = ds.getNum_operators();                //create a sub-menu
    Operator ds_ops[] = new Operator[num_ops];          //for the current
    for ( int i = 0; i < num_ops; i++ )                 //DataSet object
      ds_ops[i] = ds.getOperator(i);

    DataSet[] dss = new DataSet[1];
    dss[0] = ds;
    JMenu ops_popup_menu = new JMenu( "Operations" );
    OperatorMenu om = new OperatorMenu();
    JOperationsMenuHandler popup_listener = new JOperationsMenuHandler( dss, tree, false );
    om.build( ops_popup_menu, ds_ops, popup_listener );
    ops_popup_menu.setPopupMenuVisible( true );

    class singleDataSetMenuItemListener implements ActionListener
    {
      TreePath[] tps;
 
      public singleDataSetMenuItemListener( TreePath[] tps_ )
      {
        tps = tps_;
      }


      public void actionPerformed( ActionEvent item_e )
      {
        if(  item_e.getActionCommand() == MENU_SELECT  )
          tree.selectNodesWithPaths( tps );

        if(  item_e.getActionCommand() == MENU_DELETE  )
          tree.deleteNodesWithPaths( tps );
      }
    }


    singleDataSetMenuItemListener item_listener = new singleDataSetMenuItemListener( tps );
    JMenuItem select_item = new JMenuItem( MENU_SELECT );
              //select_item.setMnemonic( KeyEvent.VK_S );
              select_item.addActionListener( item_listener );
    JMenuItem delete_item = new JMenuItem( MENU_DELETE );
              //delete_item.setMnemonic( KeyEvent.VK_X );
              delete_item.addActionListener( item_listener );
    JPopupMenu popup_menu = new JPopupMenu( "SingleDataSetPopupMenu" );
               popup_menu.add( delete_item );
               popup_menu.add( select_item );
               popup_menu.add( ops_popup_menu );
               popup_menu.show(  e.getComponent(), e.getX(), e.getY()  );
  }


  /**
   * creates a popup menu that is appropriate for multiple
   * DataSet object selection (when the user right-clicks
   * on a number of highlighted DataSet nodes)
   */
  public void MultipleDataSetPopupMenu( TreePath[] tps, MouseEvent e )
  {
    DataSetMutableTreeNode node = (DataSetMutableTreeNode)(  tps[0].getLastPathComponent()  );
    DataSet ds = node.getUserObject();

    int num_ops = ds.getNum_operators();                //create a sub-menu
    Operator ds_ops[] = new Operator[num_ops];          //for the current
    for( int i = 0; i < num_ops; i++ )                  //DataSet objects
      ds_ops[i] = ds.getOperator(i);

                               //get the DataSet objects to which we will 
                               //apply the operator.  we'll assume that all
                               //of the TreePath objects have been filtered
                               //and that they are all DataSetMutableTreeNodes.
                               //karma--
    DataSet[] dss = new DataSet[ tps.length ];
    for( int i=0;  i<tps.length;  i++ )
      dss[i] = (DataSet)(  ( (DataSetMutableTreeNode)tps[i].getLastPathComponent() ).getUserObject()  );

                               //create the actual menu
    JMenu ops_popup_menu = new JMenu( "Operations" );
    OperatorMenu om = new OperatorMenu();
    JOperationsMenuHandler popup_listener = new JOperationsMenuHandler( dss, tree, true );
    om.build( ops_popup_menu, ds_ops, popup_listener );
    ops_popup_menu.setPopupMenuVisible( true );

    class MultipleDataSetMenuItemListener implements ActionListener
    {
      TreePath[] tps;
 
      public MultipleDataSetMenuItemListener( TreePath[] tps_ )
      {
        tps = tps_;
      }


      public void actionPerformed( ActionEvent item_e )
      {
        if(  item_e.getActionCommand() == MENU_SELECT  )
          tree.selectNodesWithPaths( tps );

        if(  item_e.getActionCommand() == MENU_DELETE  )
          tree.deleteNodesWithPaths( tps );
      }
    }


    MultipleDataSetMenuItemListener item_listener = new MultipleDataSetMenuItemListener( tps );
    JMenuItem select_item = new JMenuItem( MENU_SELECT );
              //select_item.setMnemonic( KeyEvent.VK_S );
              select_item.addActionListener( item_listener );
    JMenuItem delete_item = new JMenuItem( MENU_DELETE );
              //delete_item.setMnemonic( KeyEvent.VK_X );
              delete_item.addActionListener( item_listener );
    JPopupMenu popup_menu = new JPopupMenu( "MultipleDataSetPopupMenu" );
               popup_menu.add( delete_item );
               popup_menu.add( select_item );
               popup_menu.add( ops_popup_menu );
               popup_menu.show(  e.getComponent(), e.getX(), e.getY()  );
  }


  /**
   * creates a popup menu that is appropriate for a single
   * Runfile selection (when the user right-clicks on highlighted
   * runfile)
   */
  public void ExperimentPopupMenu( TreePath[] tps, MouseEvent e )
  {
    System.out.println( "ExperimentPopupMenu(...)" );

    class ExperimentMenuItemListener implements ActionListener
    {
      TreePath[] tps;

      public ExperimentMenuItemListener( TreePath[] tps )
      {
        this.tps = tps;
      }


      public void actionPerformed( ActionEvent item_e )
      {
        if(  item_e.getActionCommand() == MENU_SELECT  )
          tree.selectNodesWithPaths( tps );

        if(  item_e.getActionCommand() == MENU_DELETE  )
          tree.deleteNodesWithPaths( tps );
      }
    }


    ExperimentMenuItemListener item_listener = new ExperimentMenuItemListener( tps );
    JMenuItem select_item = new JMenuItem( MENU_SELECT );
              //select_item.setMnemonic( KeyEvent.VK_S );
              select_item.addActionListener( item_listener );
    JMenuItem delete_item = new JMenuItem( MENU_DELETE );
              //delete_item.setMnemonic( KeyEvent.VK_X );
              delete_item.addActionListener( item_listener );
    JPopupMenu popup_menu = new JPopupMenu( "SingleDataSetPopupMenu" );
               popup_menu.add( delete_item );
               popup_menu.add( select_item );
               popup_menu.show(  e.getComponent(), e.getX(), e.getY()  );
  }





  /**
   * if 'tps' leads to a Data object, this method sets POINTED_AT
   * in that Data object's containing DataSet.
   */
  public void pointAtNode( TreePath tp )
  {
    MutableTreeNode node = (MutableTreeNode)tp.getLastPathComponent();

    if(  node instanceof DataMutableTreeNode  )
    {
      DataMutableTreeNode d_node = (DataMutableTreeNode)node;
      DataSet ds = tree.getDataSet( d_node );
      ds.setPointedAtIndex(  ds.getIndex_of_data( d_node.getUserObject() )  );
      ds.notifyIObservers( IObserver.POINTED_AT_CHANGED );
    }
  }


  /**
   * sets newly selected Data as selected and notifies their containing
   * DataSet.
   */
  public void selectNode( TreePath[] tps )
  {
    MutableTreeNode node = (MutableTreeNode)tps[0].getLastPathComponent();
    if( node instanceof Experiment )
    {
      System.out.println( "Experiment container selected in tree" );
    }
    if( node instanceof DataSetMutableTreeNode )
    {
      System.out.println( "DataSet object selected in tree" );
    }
    if( node instanceof DataMutableTreeNode )
    {
      System.out.println( "Data object selected in tree" );

/* TODO: fix selection behavior for Data objects
                                      //we can only deal w/ selections from 
                                      //one DataSet at a time, so we'll 
                                      //arbitrarily choose the DataSet 
                                      //that the first selection                                       
                                      //corresponds to
      DataSet ds = tree.getDataSet( node );
      if(  ds != DataSet.EMPTY_DATA_SET  )
      {
        for( int i=0;  i<tps.length;  i++ )
        {
          node = (MutableTreeNode)tps[i].getLastPathComponent();
          if(  node instanceof DataMutableTreeNode &&
               ds.equals( tree.getDataSet(node) )  )
          {
            DataMutableTreeNode d_node = (DataMutableTreeNode)node;
            Data d = d_node.getUserObject();
            if(  d.isSelected() == false   )
              d.setSelected( true );
            else
              d.setSelected( false );
          }
          else
            System.out.println( "non-Data selected in tree" );
        
        }
        ds.notifyIObservers( IObserver.SELECTION_CHANGED );
      }
      else
        return;
*/
    }
  }


  /**
   * recursivly find the DataSet that this Data block
   * belongs to and return it the caller
   */
  protected DataSet traverseUpToDataSet( TreePath tp )
  {
    return DataSet.EMPTY_DATA_SET;
  }


}

