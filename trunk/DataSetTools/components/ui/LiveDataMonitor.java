/*
 * File:  LiveDataMonitor.java
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
 *  Revision 1.23  2002/11/27 23:13:34  pfpeterson
 *  standardized header
 *
 *  Revision 1.22  2002/04/18 22:00:57  dennis
 *  Fixed name problem that prevented compiling with jdk1.3.1_03
 *  (Though it did compile with 1.4.0)
 *
 *  Revision 1.21  2002/04/18 21:34:24  dennis
 *  If the LiveDataManager Thread sends a message to this LiveDataMonitor,
 *  the request is put into a queue and run by the Swing Event handling
 *  thread using SwingUtilities.invokeLater().  This guarantees that the
 *  Swing drawing that is needed in response to the message will be done
 *  in the Swing Event handling thread.
 *
 *  Revision 1.20  2002/04/10 16:02:55  dennis
 *  Removed automatic request for update after requesting that a DataSet be
 *  shown.
 *
 */

package DataSetTools.components.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import DataSetTools.util.*;

/**
 *
 *  This class is a JPanel that contains a user interface to control a
 *  LiveDataManager, which can periodically update DataSets from a 
 *  LiveDataServer.
 *
 *  @see NetComm.LiveDataServer
 *  @see DataSetTools.retriever.LiveDataRetriever
 *  @see DataSetTools.retriever.LiveDataManager
 */

public class LiveDataMonitor extends    JPanel
                             implements IObservable,
                                        Serializable 
        
{
  public static final Color  BACKGROUND  = Color.white;
  public static final Color  FOREGROUND  = Color.black;
  public static final Color  ALERT_COLOR = Color.red;
  private String           data_source_name = "";
  private String           current_status   
                                   = RemoteDataRetriever.NOT_CONNECTED_STRING;
  private LiveDataManager  data_manager = null;
  private JLabel           status_label = new JLabel();
  private JLabel           source_label = new JLabel();
  private ViewManager      viewers[]    = new ViewManager[0];
  private JCheckBox        show_box[]   = new JCheckBox[0];
  private JCheckBox        auto_box[]   = new JCheckBox[0];
  private JButton          button[]     = new JButton[0];
  private JButton          record[]     = new JButton[0];
  private JLabel           ds_label[]   = new JLabel[0];
  private JPanel           panel[]      = new JPanel[0];
  private IObserverList    observers;
  private Vector           event_queue;          // queue for passing the action
                                                 // event from the DataManager
                                                 // to the event thread
 
 /* ------------------------------ CONSTRUCTOR ---------------------------- */
 /** 
  *  Construct a LiveDataMonitor GUI, and a LiveDataManager to control the
  *  updates of live data from the specified data source.  This also
  *  makes the connection to the LiveDataServer via a LiveDataRetriever.
  *
  *  @param  data_source_name  The name or IP address of the system that is
  *                            running the LiveDataServer.
  *
  */
  public LiveDataMonitor( String data_source_name ) 
  { 
    event_queue = new Vector();
    this.data_source_name = data_source_name;

    data_manager = new LiveDataManager( data_source_name );
    data_manager.addActionListener( new DataManagerListener() );
    observers = new IObserverList();

    SetUpGUI();
  }


  /**
   *  Add the specified object to the list of observers to notify when this
   *  observable object changes.
   *
   *  @param  iobs   The observer object that is to be notified.
   *
   */
   public void addIObserver( IObserver iobs )
   {
     observers.addIObserver( iobs );
   }


  /**
   *  Remove the specified object from the list of observers to notify when
   *  this observable object changes.
   *
   *  @param  iobs   The observer object that should no longer be notified.
   *
   */
   public void deleteIObserver( IObserver iobs )
   {
     observers.deleteIObserver( iobs );
   }


  /**
   *  Remove all objects from the list of observers to notify when this
   *  observable object changes.
   */
   public void deleteIObservers( )
   {
     observers.deleteIObservers();
   }


  /**
   *  Notify all observers in the list ( by calling their update(,) method )
   *  that the observed object has changed.
   *
   * @param  reason        Object indicating the nature of the change in the
   *                       observable object, or specifying the action that the
   *                       observer should take.  This is passed as the second
   *                       parameter to the update(,) method of the observers
   *                       and will typically be a String.
   */
   public void notifyIObservers( Object reason )
   {
     observers.notifyIObservers( this, reason );
   }






/**
 *  Get the number of distinct DataSets from the current data source.
 *  The monitors are placed into one DataSet.  Any sample histograms are
 *  placed into separate DataSets.
 *
 *  @return the number of distinct DataSets in this runfile.
 */
  public int numDataSets()
  {
    if ( data_manager != null )
      return data_manager.numDataSets();
    else
      return 0; 
  }

/**
 * Get the type of the specified data set from the current data source.
 * The type is an integer flag that indicates whether the data set contains
 * monitor data or data from other detectors.
 */

  public int getType( int data_set_num )
  {
    if ( data_manager != null )
      return data_manager.getType( data_set_num );
    else
      return Retriever.INVALID_DATA_SET;
  }


/**
 *  Get the specified DataSet from the current data source.
 *
 *  @param  data_set_num  The number of the DataSet in this runfile
 *                        that is to be read from the runfile.  data_set_num
 *                        must be between 0 and numDataSets()-1
 *
 *  @return the requested DataSet.
 */
  public DataSet getDataSet( int data_set_num )
  {
    if ( data_manager != null )
    {
      DataSet temp_ds = data_manager.getDataSet( data_set_num ); 
      if ( temp_ds != null )
        return (DataSet)(temp_ds.clone());
    }

    return null;
  }


/**
 *
 */
  public void destroy()
  {
    // SHOULD FIRST SHUTDOWN THE data_manager... NOT YET IMPLEMENTED

    removeAll();                    // get rid of all of the components
  }

 /* ------------------------------------------------------------------------
  *
  *  PRIVATE METHODS 
  *
  */


  private void ExpandStorage()
  {
    int num_ds     = data_manager.numDataSets();
    int old_length = viewers.length;

    if ( num_ds > old_length )
    {                                                         // get more space
      ViewManager new_viewers[]  = new ViewManager[ num_ds ];
      JCheckBox   new_show_box[] = new JCheckBox[ num_ds ];
      JCheckBox   new_auto_box[] = new JCheckBox[ num_ds ];
      JButton     new_button[]   = new JButton[ num_ds ];
      JButton     new_record[]   = new JButton[ num_ds ];
      JLabel      new_ds_label[] = new JLabel[ num_ds ];
      JPanel      new_panel[]    = new JPanel[ num_ds ];

      for ( int i = 0; i < viewers.length; i++ )             // save the old
      {                                                      // objects
        new_viewers[i]  = viewers[i];
        new_show_box[i] = show_box[i];
        new_auto_box[i] = auto_box[i];
        new_button[i]   = button[i];
        new_record[i]   = record[i];
        new_ds_label[i] = ds_label[i];
        new_panel[i]    = panel[i];
      }
                                                             // shift over to
      viewers  = new_viewers;                                // the new ones
      show_box = new_show_box;
      auto_box = new_auto_box;
      button   = new_button;
      record   = new_record;
      ds_label = new_ds_label;
      panel    = new_panel;
                                                             // construct new
                                                             // labels, etc.
      for ( int i = old_length; i < viewers.length; i++ )
      {
        viewers[i]  = null;

        panel[i] = new JPanel();                // use a separate "sub" panel
        panel[i].setLayout( new FlowLayout() ); // for each possible DataSet
        panel[i].setBackground( BACKGROUND );
        TitledBorder border = 
           new TitledBorder( LineBorder.createBlackLineBorder(),"DataSet #"+i);
        border.setTitleFont( FontUtil.BORDER_FONT );
        border.setTitleColor( FOREGROUND );
        panel[i].setBorder( border );

        show_box[i] = new JCheckBox( "Show" );
        show_box[i].setFont( FontUtil.BORDER_FONT );
        show_box[i].setSelected( false );
        show_box[i].setBackground( BACKGROUND );
        ShowCheckboxListener show_box_listener = new ShowCheckboxListener( i );
        show_box[i].addActionListener( show_box_listener );

        auto_box[i] = new JCheckBox( "Auto" );
        auto_box[i].setFont( FontUtil.BORDER_FONT );
        auto_box[i].setBackground( BACKGROUND );
        auto_box[i].setSelected( false );
        AutoCheckboxListener auto_box_listener = new AutoCheckboxListener( i );
        auto_box[i].addActionListener( auto_box_listener );

        button[i] = new JButton("Update");
        button[i].setFont( FontUtil.BORDER_FONT );
        button[i].setBackground( BACKGROUND );
        UpdateButtonListener button_listener = new UpdateButtonListener( i );
        button[i].addActionListener( button_listener );

        record[i] = new JButton("Record");
        record[i].setFont( FontUtil.BORDER_FONT );
        record[i].setBackground( BACKGROUND );
        RecordButtonListener record_listener = new RecordButtonListener( i );
        record[i].addActionListener( record_listener );

        ds_label[i]  = new JLabel( "DATA SET" );
        ds_label[i].setFont( FontUtil.BORDER_FONT );
        ds_label[i].setForeground( FOREGROUND );

        panel[i].add( ds_label[i] );             // Add the components for this
        panel[i].add( show_box[i] );             // DataSet to the current panel
        panel[i].add( auto_box[i] );
        panel[i].add( button[i] );
        panel[i].add( record[i] );
      }
    }
  }
 

  /* ------------------------------ ErrorMessage --------------------------- */

  private String ErrorMessage()
  {
    if ( data_manager == null )
      return "ERROR: No DataManager!!!";

    int code = data_manager.numDataSets();

    if ( code >= 0 )
      return "" + code + " DataSets";       
 
    return  RemoteDataRetriever.error_message( code );
  }


  /* ------------------------------ SetUpGUI ------------------------------- */

  synchronized private void SetUpGUI()
  {
    setBackground( BACKGROUND );
    setForeground( FOREGROUND );

    ExpandStorage();
                                                  // Clear the panel and set up
                                                  // a new layout using a box.
    setVisible( false );
    removeAll();                                  
    setLayout( new GridLayout( 1, 1 ) );
    Box panel_box = new Box( BoxLayout.Y_AXIS );
    add( panel_box );
                                                            // Set up the update
                                                            // time control
    JSlider time_slider = new JSlider( JSlider.HORIZONTAL, 0, 600, 
                                       (int)data_manager.getUpdateInterval() );

    time_slider.setMinimumSize( new Dimension(50, 20) );
    time_slider.setPaintTicks( true );
    time_slider.setMajorTickSpacing( 60 );
    time_slider.setMinorTickSpacing( 20 );
    time_slider.addChangeListener( new UpdateTimeListener() );
    time_slider.setBackground( BACKGROUND );
    data_manager.setUpdateInterval( time_slider.getValue() );

    JPanel label_panel  = new JPanel();
    label_panel.setLayout( new GridLayout( 1, 1 ) );

    source_label.setText( data_source_name.toUpperCase() );
    source_label.setFont( FontUtil.BORDER_FONT );
    source_label.setBackground( BACKGROUND );
    source_label.setForeground( FOREGROUND );
    source_label.setHorizontalAlignment( SwingConstants.CENTER );
    source_label.setHorizontalTextPosition( SwingConstants.CENTER );
    source_label.setVerticalAlignment( SwingConstants.CENTER );
    source_label.setVerticalTextPosition( SwingConstants.CENTER );

    status_label.setText( ErrorMessage() );
    status_label.setFont( FontUtil.BORDER_FONT );
    status_label.setBackground( BACKGROUND );
    status_label.setForeground( FOREGROUND );
    status_label.setHorizontalAlignment( SwingConstants.CENTER );
    status_label.setHorizontalTextPosition( SwingConstants.CENTER );
    status_label.setVerticalAlignment( SwingConstants.CENTER );
    status_label.setVerticalTextPosition( SwingConstants.CENTER );

    TitledBorder border = new TitledBorder( LineBorder.createBlackLineBorder(),
                                            "Data Source:" );
    border.setTitleFont( FontUtil.BORDER_FONT );
    border.setTitleColor( FOREGROUND );
    label_panel.setBorder( border );
    label_panel.add( source_label );
    label_panel.add( status_label );
    label_panel.setBackground( BACKGROUND );
    panel_box.add( label_panel );

    for ( int i = 0; i < data_manager.numDataSets(); i++ )
    {
      panel_box.add( panel[i] );                 // Add the panel to this
                                                 // LiveDataMonitor

      if ( auto_box[i].isSelected() )
        data_manager.setUpdateIgnoreFlag( i, false );
      else
        data_manager.setUpdateIgnoreFlag( i, true );

      if ( viewers[i] != null && viewers[i].isVisible()  )
      {
        DataSet ds = data_manager.getDataSet(i);
        if ( ds != null )
          viewers[i].setDataSet( ds );
      }
      else 
        viewers[i] = null;
    }

    border = new TitledBorder( LineBorder.createBlackLineBorder(),
                               "Auto Update Interval( 0 - 10 Min )" );
    border.setTitleFont( FontUtil.BORDER_FONT );
    border.setTitleColor( FOREGROUND );
    time_slider.setBorder( border );

    panel_box.add( time_slider );

    FixLabels();
    validate(); 
    setVisible( true );
  }


 /* ------------------------------ FixLabels ------------------------------ */
 /**
  *  Check if any of the DataSet titles have changed and adjust the labels if
  *  they have.
  */
  private void FixLabels()
  {
    if (ds_label == null || ds_label.length == 0 )
      return;

    int num_labels = data_manager.numDataSets();
    if ( num_labels > ds_label.length )
      num_labels = ds_label.length;

    for ( int i = 0; i < num_labels; i++ )
    {
      DataSet ds = data_manager.getDataSet( i );
      if ( ds != null )
      {
        String cur_title = ds.getTitle();
        if ( !cur_title.equalsIgnoreCase( ds_label[i].getText() ) )
          ds_label[i].setText( cur_title );
      }
      else 
        ds_label[i].setText( "NO DATA SET" );
    } 
  }


 /* ------------------------------------------------------------------------
  *
  *  INTERNAL CLASSES
  *
  */
    
  /* ------------------------ UpdateTimeListener -------------------------- */

  private class UpdateTimeListener implements ChangeListener,
                                              Serializable
  {
    public void stateChanged( ChangeEvent e )
    {
      JSlider slider = (JSlider)e.getSource();

      if ( !slider.getValueIsAdjusting() )
        data_manager.setUpdateInterval( slider.getValue() );

      FixLabels();
    }
  };

  /* ----------------------- UpdateButtonListener ------------------------- */

  private class UpdateButtonListener implements ActionListener,
                                                Serializable
  {
    int     my_index;

    public UpdateButtonListener( int index )
    {
      my_index = index;
    }

    public void actionPerformed( ActionEvent e )
    {
      if ( viewers[ my_index ] == null  ||
          !viewers[ my_index ].isVisible() )    // make another viewer
      {
        DataSet ds = data_manager.getDataSet( my_index );
        if ( ds != null )
        {
          viewers[my_index] = new ViewManager( ds, IViewManager.IMAGE );
          viewers[my_index].addWindowListener( 
                                         new ViewManagerListener(my_index));
        }
      }

      data_manager.UpdateDataSetNow( my_index ); 
      show_box[ my_index ].setSelected( true );   // update implies we show it

      FixLabels();
    }
  }


  /* ----------------------- RecordButtonListener ------------------------- */

  private class RecordButtonListener implements ActionListener,
                                                Serializable
  {
    int     my_index;

    public RecordButtonListener( int index )
    {
      my_index = index;
    }

    public void actionPerformed( ActionEvent e )
    {
      DataSet ds = data_manager.getDataSet(my_index);
      if ( ds != null )
      {
        ds = (DataSet)ds.clone();
        observers.notifyIObservers( this, ds );
      }

      FixLabels();
    }
  }


  /* ----------------------- ShowCheckboxListener ---------------------- */

  private class ShowCheckboxListener implements ActionListener,
                                                Serializable
  {
    int     my_index;

    public ShowCheckboxListener( int index )
    { 
      my_index = index;
    }

    public void actionPerformed( ActionEvent e )
    { 
      JCheckBox check_box = (JCheckBox)(e.getSource());

      if ( check_box.isSelected() )
      {
        if ( viewers[ my_index ] == null  ||
            !viewers[ my_index ].isVisible() )    // make another viewer
        { 
          DataSet ds = data_manager.getDataSet( my_index );
          if ( ds != null )
          {
           viewers[my_index] = new ViewManager( ds, IViewManager.IMAGE );
           viewers[my_index].addWindowListener( 
                                            new ViewManagerListener(my_index));
          }
        }
      }
      else
      {
        viewers[my_index].destroy();
        auto_box[my_index].setSelected( false );  // Don't show it implies
                                                  // don't auto update it.
        data_manager.setUpdateIgnoreFlag( my_index, true );
      }

      FixLabels();
    }
  }


  /* ----------------------- AutoCheckboxListener ---------------------- */

  private class AutoCheckboxListener implements ActionListener,
                                                Serializable
  {
    int     my_index;

    public AutoCheckboxListener( int index )
    {
      my_index = index;
    }

    public void actionPerformed( ActionEvent e )
    {
      JCheckBox check_box = (JCheckBox)(e.getSource());

      if ( check_box.isSelected() )
      {
        if ( viewers[ my_index ] == null  ||
            !viewers[ my_index ].isVisible() )    // make another viewer
        {
          DataSet ds = data_manager.getDataSet( my_index );
          if ( ds != null )
          {
           viewers[my_index] = new ViewManager( ds, IViewManager.IMAGE );
           viewers[my_index].addWindowListener( 
                                          new ViewManagerListener(my_index));
          }
        }
        show_box[ my_index ].setSelected( true );   // Auto update implies 
                                                    // we also show it.
        data_manager.setUpdateIgnoreFlag( my_index, false );
      }
      else
        data_manager.setUpdateIgnoreFlag( my_index, true );

      FixLabels();
    }
  }


  /* ----------------------- DataManagerListener ---------------------- */

  private class DataManagerListener implements ActionListener,
                                               Serializable
  {
    public void actionPerformed( ActionEvent e )       // since this is called
    {                                                  // from a different 
      event_queue.addElement(e);                       // thread, save the 
      Runnable actionRunnable = new Runnable()         // event in a queue and 
      {                                                // use this "Runnable"
        public void run()                              // to actually carry out 
        {                                              // the requested action 
          if ( event_queue.size() <= 0 )               // in the Swing event 
            return;                                    // handling thread.
                                                         
          ActionEvent event = (ActionEvent)event_queue.elementAt(0);
          event_queue.removeElementAt(0);
          run_actionPerformed( event );
        }
      };
      SwingUtilities.invokeLater( actionRunnable );
    }

    public void run_actionPerformed( ActionEvent e )
    {
      String message = e.getActionCommand();

      if ( message.startsWith( LiveDataManager.DATA_CHANGED ) )
        SetUpGUI();

      else
      { 
        if ( message.startsWith( RemoteDataRetriever.DAS_OFFLINE_STRING )  ||
             message.startsWith( RemoteDataRetriever.DATA_OLD_STRING )    )
        { 
          status_label.setForeground( ALERT_COLOR );
          status_label.setText( message ); 
        }
        else
        {
          status_label.setForeground( FOREGROUND );
          status_label.setText( message ); 
        }

        current_status = message;
      }
    }
  }


  /* ------------------------ ViewManagerListener -------------------- */

  private class ViewManagerListener extends WindowAdapter
  {
    int index;
    public ViewManagerListener( int index )
    {
      this.index = index;
    }

    public void windowClosing(WindowEvent ev)
    {
      if ( index < show_box.length )
        show_box[ index ].setSelected( false );

      if ( index < auto_box.length )
        auto_box[ index ].setSelected( false );

      if ( index < viewers.length )
        viewers [ index ] = null;
    }
  }


/* -------------------------------------------------------------------------
 *
 * MAIN 
 *
 */
  public static void main(String[] args)
  {
     if ( args.length < 1 )
     {
       System.out.println("Please enter the instrument computer name ");
       System.out.println("or IP address on the command line ");
       System.exit(1);
     } 

     String instrument_computer = args[0];
     LiveDataMonitor monitor = new LiveDataMonitor( instrument_computer );

     JFrame frame = new JFrame( "Live Data Monitor" );
     frame.setBounds( 20, 20, 450, 350 );
     frame.getContentPane().add( monitor );
 
     frame.setVisible( true );
  }
}
