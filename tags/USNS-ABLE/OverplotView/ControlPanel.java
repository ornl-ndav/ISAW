package OverplotView;

/**
 * ControlPanel.java
 *
 * provides a tabbed pane, filled with various selection methods and other
 * user options
 *
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.colorchooser.*;

import OverplotView.components.containers.*;

import DataSetTools.components.ui.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.viewer.*;

import gov.noaa.noaaserver.sgt.LineAttribute;
 

public class ControlPanel
  extends JTabbedPane
{

  public ControlPanel( DataSet d_, GraphableDataManager m_ )
  {
    manager = m_;
    data_set = d_;
    data = new GraphableData[  d_.getNum_entries()  ];

    String xunits = data_set.getX_units();
    String xlabel = data_set.getX_label();
    String yunits = data_set.getY_units();
    String ylabel = data_set.getY_label();

    for( int i=0; i<data_set.getNum_entries(); i++ )
    {

      String i_val = new String();
      Data d = data_set.getData_entry(i);
      int id = d.getGroup_ID();

      AttributeList alist = data_set.getData_entry(i).getAttributeList();
      String id_str = alist.getAttributeValue( Attribute.RUN_NUM ) + 
                      "::Group #" + i_val.valueOf( id );

      OperationLog log = data_set.getOp_log();
//      log.addEntry( id_str + 
//                    " converted into a GraphableData Object" + newline );

      data[i] = new GraphableData(  id_str,
                                    d,
                                    log, 
                                    xunits,
                                    xlabel,
                                    yunits,
                                    ylabel, 
                                    id_str  );
    }

    
  }

  

  /**
   * creates and initializes all tabs in tabbed pane
   *
   */
  public void init()
  {
    //initialize super classes?

    ImageIcon icon = new ImageIcon( "graph.gif" );

    // *** SELECT ***
    Component selectC = initSelect_inJPanel();
    this.addTab("Select", icon, selectC, "select data to be graphed" );
    this.setSelectedIndex( 0 );


    // *** WINDOW ***
    Component windowC = initWindow_inJPanel();
    //this.addTab("Window", icon, windowC, "select viewable graph region" );


    // *** DATA ***
    Component dataC = initData_inJPanel();
    this.addTab("Data", icon, dataC, "view data properties" );


    // *** GRAPH ***
    Component graphC = initGraph_inJPanel();
    this.addTab("Graph", icon, graphC, "set graph attributes" );
  }


 
  /*
   * draws Selection tab.  initializing the lists of data and controls
   * for adding and subtracting data from the graph
   *
   */  
  protected Component initSelect_inJPanel()
  {
    //create list
    dataDLM = new DefaultListModel();
    dataJL = new JList( dataDLM );

    graphDLM = new DefaultListModel();
    graphJL = new JList( graphDLM );

    dataJL.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
    dataJL.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

    //create a scrolled pane
    JScrollPane paneJS = new JScrollPane( dataJL );
    paneJS.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );

    JScrollPane paneJS1 = new JScrollPane( graphJL );
    paneJS1.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );

    //create a listener for selection events and attach it to list
    dataJL.addListSelectionListener(  new SelectListSelectionListener()  );
    graphJL.addListSelectionListener(  new GraphedListSelectionListener()  );

    //create a listener for double-click events and attach it to the list
    dataJL.addMouseListener(  new DoubleClickSelectionListener()  );
    graphJL.addMouseListener(  new DoubleClickDeselectionListener()  );

   
    String addS = "Add";
    addJB = new JButton( addS );
    addJB.setActionCommand( addS ); 
    addJB.addActionListener( new addActionListener() );

    String removeS = "Remove";
    removeJB = new JButton( removeS );
    removeJB.setActionCommand( removeS ); 
    removeJB.addActionListener( new removeActionListener() );

    String graphS = "Graph";
    graphJB = new JButton( graphS );
    graphJB.setActionCommand( graphS ); 
    graphJB.addActionListener( new graphActionListener() );
    graphJB.setEnabled( true );


    //deal with selections that were present in the DataSet that was given
    // to us from the ViewManager.
    for( int i=0; i<data_set.getNum_entries(); i++ )
      if(  data_set.isSelected( i ) == true  )
        addToGraphList( data[i] ); 


//      {
//        System.out.println(  "constructed using " + data[i].toString()  );
//        int[] tmp = { i };
//        addToGraphList( tmp );
//        addToGraphList( data[i] ); 
//      }



    //add data blocks to list
    int last = data.length;
    for( int i=0; i<last; i++ ) 
    {
      String key = new String(  data[i].getID()  );
      dataDLM.addElement( key );
      if(  data[i].isSelected()  )
      {
        String[] list = { key };
        addToGraphList( list );
      }
    }

    //layout
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(  new GridLayout( 3, 1 )  );
    buttonPanel.add( addJB );
    buttonPanel.add( removeJB );
    buttonPanel.add( graphJB );

    JLabel sliderLabel = new JLabel( "Offset (%)", 
                                     JLabel.CENTER );
    sliderLabel.setAlignmentX( Component.CENTER_ALIGNMENT );
    JSlider offsetSlider = new JSlider( JSlider.HORIZONTAL,
                                        0, 
                                        100,
                                        0 );
    offsetSlider.addChangeListener( new SliderListener() );
    offsetSlider.addMouseListener( new OffsetSliderMouseHandler() );

    offsetSlider.setPaintTicks( false );
    offsetSlider.setPaintLabels( false );
    offsetSlider.setBorder(  BorderFactory.createTitledBorder( "offset" )  );

    JPanel offsetPanel = new JPanel();
    offsetPanel.setLayout(  new GridLayout( 1, 1 )  );
    offsetPanel.add( offsetSlider );


    JPanel middle = new JPanel();
    middle.setLayout(  new GridLayout( 2, 1 )  );
    middle.add( buttonPanel );
    middle.add( offsetPanel );

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout( new GridLayout( 1, 3 ) );
    mainPanel.add( paneJS );
    mainPanel.add( middle );
    mainPanel.add( paneJS1 );

    addJB.setEnabled( false );
    removeJB.setEnabled( false );
    if( manager.size() == 0 )
      graphJB.setEnabled( false );
    else 
      graphJB.setEnabled( true );

    return mainPanel; 
  }





  /*
   * create a jpanel that contains the controls associated with manual
   * range selection
   */
  protected Component initWindow_inJPanel()
  {
/*
    JPanel panelJP = new JPanel();

    manager.setXRangeUI( xrange = new TextRangeUI(  "xrange", 
                                                    manager.getXRange().x,
                                                    manager.getXRange().y )  );

    manager.setYRangeUI( yrange = new TextRangeUI( "yrange", 
                                                   manager.getYRange().x, 
                                                   manager.getYRange().y )  );


    xrangeActionListener xrangeListener = new xrangeActionListener();
    xrange.addActionListener(  xrangeListener  );
    yrangeActionListener yrangeListener = new yrangeActionListener();
    yrange.addActionListener(  yrangeListener  );

    panelJP.setLayout(  new GridLayout( 5, 5 )  );

    //row 1
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );

    //row 2
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );
 
    //row 3
    panelJP.add(  new JPanel()  );
    panelJP.add( xrange );
    panelJP.add(  new JPanel()  );
    panelJP.add( yrange );
    panelJP.add(  new JPanel()  );

    //row 4
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );

    //row 5
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );
    panelJP.add(  new JPanel()  );

    return panelJP;
*/
    return new JPanel();
  }



  /*
   * takes care of displaying data from the dataset's log
   */
  protected Component initData_inJPanel()
  {
    int last;
    AttributeList attrList = data_set.getAttributeList();

    last = attrList.getNum_attributes();
    Attribute attr[] = new Attribute[ last ];

    String attrS = "Attributes for " + data_set.getTitle();
    JTextArea attrJTA = new JTextArea( last, 80 );
    attrJTA.setEditable( false );
    JScrollPane attrJSP = new JScrollPane( attrJTA,
                                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    attrJTA.append( attrS + newline + newline );
    for( int i=0; i<last; i++ ) {
      attr[i] = attrList.getAttribute( i );
      String name = new String( attr[i].getName() );
      String value = new String( attr[i].getStringValue() );

      attrJTA.append( name + ":   " + value + newline );
    }

    String logS = "Operation Log for " + data_set.getTitle();
    attrJTA.append( newline + newline + logS + newline );

    OperationLog logOL = data_set.getOp_log();
    last = logOL.numEntries()-1;
    for( int i=0; i<last; i++ ) {
      attrJTA.append(  logOL.getEntryAt( i )  );
    }

    JPanel panel = new JPanel( new GridLayout( 1, 1 ) );
    panel.add( attrJSP );

    return panel;
  }


  protected Component initGraph_inJPanel()
  {

    //
    // **[ DATA LIST ]**  set up list of selected data
    //
    propertyDLM = new DefaultListModel();
    propertyJL = new JList( propertyDLM );
    propertyJL.addListSelectionListener( new PropertyListSelectionListener() );
    propertyJL.setSelectionMode( 
      ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

    JScrollPane propertyListJSP = new JScrollPane( propertyJL );
    propertyListJSP.setVerticalScrollBarPolicy( 
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS  );

    //layout list of selected data
    JPanel propertyListJP = new JPanel();
    propertyListJP.setLayout(  new GridLayout( 1, 1 )  );
    propertyListJP.add( propertyListJSP );

    //add all items that are currently selected (in selected list in
    // 'Select' tab
    for( int i=0;  i<graphDLM.size();  i++ )
      propertyDLM.add(  i, graphDLM.get( i )  );


    //
    // **[ COLOR CHOOSER ]**  set up a list of colors
    //
    chooserJCC = new JColorChooser( Color.black );
    chooserJCC.setPreviewPanel(  new JPanel()  );
    chooserJCC.getSelectionModel().addChangeListener( 
      new ColorChooserChangeListener()  );
    chooserJCC.setBorder(  
      BorderFactory.createTitledBorder( "Choose Text Color" )  );

    //Add the components to the demo frame
    JPanel colorChooserJP = new JPanel();
    colorChooserJP.setLayout(  new GridLayout( 1, 1 )  );
    colorChooserJP.add( chooserJCC );



    //
    // **[ MARKER LIST ]**  initialize a list of possible markers
    //
    markerDLM = new DefaultListModel();

    markerJL  = new JList( markerDLM );
    markerJL.addListSelectionListener( new MarkerListSelectionListener() );
    markerJL.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

    JScrollPane markerListJSP = new JScrollPane( markerJL );
    markerListJSP.setVerticalScrollBarPolicy(
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS  );

    //add markers to the array 'markers'
    //  list originally found in LineProfileLayout: 2, 4, 18, 20, 48, 22, 28, 88
    int markerCount = 4;
    markers = new int[ markerCount ];
    markers[0] = LineAttribute.SOLID;
    markers[1] = 2;
    markers[2] = 4; 
    markers[3] = 18;
    //markers[4] = 18;
    //markers[5] = 20;
    //markers[6] = 48;
    //markers[7] = 22;
    //markers[8] = 28;
    //markers[9] = 88;

    //add the (above) colors to 'colorJL', where each color and it's string
    //representation have correspondint indices.
    markerDLM.addElement( "(none)" ); 
    markerDLM.addElement( "cross" ); 
    markerDLM.addElement( "plus" ); 
    markerDLM.addElement( "square" ); 

    //layout list of selected data
    JPanel markerListJP = new JPanel();
    markerListJP.setLayout(  new GridLayout( 1, 1 )  );
    markerListJP.add( markerListJSP );


    //
    // **[ BUTTONS ]**  initialize apply button
    //
    String graphTabApplyS = "Apply";
    graphTabApplyJB = new JButton( graphTabApplyS );
    graphTabApplyJB.setActionCommand( graphTabApplyS ); 
    graphTabApplyJB.addActionListener( new GraphTabApplyActionListener() );
    graphTabApplyJB.setEnabled( true );

    //layout button(s)
    JPanel controlJP = new JPanel();
    controlJP.add( graphTabApplyJB );
    

    //
    // layout this tab
    //
    JPanel listJP = new JPanel();
    listJP.setLayout(  new GridLayout( 1, 3 )  );
    listJP.add( propertyListJSP );
//    listJP.add( colorListJP );
    listJP.add( colorChooserJP );
    listJP.add( markerListJP );

    JPanel viewableAreaJP = new JPanel();
    viewableAreaJP.setLayout(  new BorderLayout()  );
    viewableAreaJP.add( listJP, BorderLayout.NORTH );
    viewableAreaJP.add( controlJP, BorderLayout.SOUTH );
    return viewableAreaJP;
  }


  public void setSize( Dimension d )
  {
    //System.out.println( "ControlPanel::setSize(...)" );
    manager.getGraph().setSize( d );
  }


/*-------------------------=[ button listeners ]=-----------------------------*/


  /**
   * listens to the 'graph' button 
   *
   */
  class graphActionListener 
    implements ActionListener 
  {
    public void actionPerformed( ActionEvent e ) 
    {
      manager.setPercentOffset( percent_offset );
      manager.redraw();
      //manager.redraw(  getSelectedKeyList()  );
    }
  }



  /**
   * listens to the 'add' button 
   *
   */
  class addActionListener 
    implements ActionListener 
  {
    public void actionPerformed( ActionEvent e ) 
    {
      addToGraphList( data_selections );
    }
  }



  /**
   * listens to the 'remove' button 
   *
   */
  class removeActionListener 
    implements ActionListener 
  {
    public void actionPerformed( ActionEvent e ) 
    {
      removeFromGraphList( graph_selections );
      if(  graphDLM.size() > 0  )
        removeJB.setEnabled( true );
      else
        removeJB.setEnabled( false );
    }
  }



  /**
   * listens to the 'apply' tab in the 'Graph' tab
   *
   */
  class GraphTabApplyActionListener
    implements ActionListener
  {
    public void actionPerformed( ActionEvent e )
    {
      manager.redraw();
      //manager.redraw(  getSelectedKeyList()  );
    }
  }



/*---------------------------=[ range listeners ]=----------------------------*/

  /**
   * listens to the x range selection object
   */
  class xrangeActionListener 
    implements ActionListener 
  {
    public void actionPerformed( ActionEvent e ) 
    {
      manager.setXRange(  new floatPoint2D( xrange.getMin(),
                                            xrange.getMax() ) );
      manager.setYRange(  new floatPoint2D( yrange.getMin(),
                                            yrange.getMax() ) );
      manager.setPercentOffset( percent_offset );
      manager.redraw();
      //manager.redraw(  getSelectedKeyList()  );
    }
  }



  /**
   * listens to the y range selection object
   */
  class yrangeActionListener 
    implements ActionListener 
  {
    public void actionPerformed( ActionEvent e ) 
    {
/*
      System.out.print( 
        "ControlPanel::actionPerformed(...)::xrange: start: " );
      System.out.print( xrange.getMin() );
      System.out.print( "  end: " );
      System.out.print( xrange.getMax() );
      System.out.print( "\n" );

      System.out.print( 
        "ControlPanel::actionPerformed(...)::yrange: start: " );
      System.out.print( yrange.getMin() );
      System.out.print( "  end: " );
      System.out.print( yrange.getMax() );
      System.out.print( "\n" );
*/

      manager.setXRange(  new floatPoint2D( xrange.getMin(),
                                            xrange.getMax() ) );
      manager.setYRange(  new floatPoint2D( yrange.getMin(),
                                            yrange.getMax() ) );
      manager.setPercentOffset( percent_offset );
      manager.redraw();
      //manager.redraw(  getSelectedKeyList()  );
    }
  }




/*---------------------=[ offset slider listeners ]=--------------------------*/


  /**
   * hack for JSlider that is used to determin offset
   */
  private class OffsetSliderMouseHandler
    extends MouseAdapter
  {
    public void mouseReleased(MouseEvent e)
    {
      JSlider source = (JSlider)e.getSource();
      source.getValue();

      //this value is bewteen 0 and 1 (as of 175-2000)
      percent_offset = (float)source.getValue() / 100;
      manager.setPercentOffset( percent_offset );
      manager.redraw();
      //manager.redraw(  getSelectedKeyList()  );
    }
  }



  /**
   * Listens to 'offsetSlider'
   */
  class SliderListener implements ChangeListener 
  {
    public void stateChanged( ChangeEvent e ) 
    {

      JSlider source = (JSlider)e.getSource();
      if(  !source.getValueIsAdjusting()  ) 
                              // set image log scale when slider stops moving
                              // #### NOTE: This should work... in fact, it
                              //            used to work.  With swing1.1.1beta2
                              //            it does not work.  The kludge,
                              //            "LogScaleMouseHandler" was added
                              //            as a workaround.  When the slider
                              //            is fixed, so that the method to
                              //            getValueIsAdjusting() again works,
                              //            the OffsetSliderMouseHandler should
                              //             be removed.

      {
        System.out.println( "SliderListener::getValueIsAdjusting()" );
      }
    }
  }    



/*------------------------=[ color chooser listener ]=------------------------*/

  /**
   * listens to the color selection devide on 'Graph' tab
   *
   */
  class ColorChooserChangeListener 
    implements ChangeListener
  {
    public void stateChanged(ChangeEvent e) 
    {
      Color newColor = chooserJCC.getColor();
      if(  property_selections != null  )
      {
        for( int i=0;  i<property_selections.length;  i++ )
        {
          GraphableData d = (GraphableData)manager.get(property_selections[i]);
          d.setColor(  new sgtEntityColor( newColor )  );
        }
      }
    }
  }



/*--------------------------=[ list listensers ]=-----------------------------*/

  /**
   * listens to list of potentially graphable data
   *
   */
  class SelectListSelectionListener 
    implements ListSelectionListener 
  {
    public void valueChanged( ListSelectionEvent e ) 
    {
      JList list = (JList)e.getSource();

      //get selections 
      int[] selections = list.getSelectedIndices();
      data_selections = new String[ selections.length ];
      for( int i=0; i<selections.length; i++ )
        data_selections[i] = (String)dataDLM.get( selections[i] ); 

      //update observers, if need be
      if(  !dataJL.getSelectionModel().isSelectionEmpty()  &&
            dataJL.getSelectedIndices().length == 1  )
      { 
        int selectedIndex = dataJL.getSelectionModel().getMaxSelectionIndex();
        data_set.setPointedAtIndex( selectedIndex );
        data_set.notifyIObservers(  IObserver.POINTED_AT_CHANGED );
      }

      //adjust buttons and selections
      addJB.setEnabled( true );
      removeJB.setEnabled( false );
    }
  }



  /**
   * listens to list of data to be graphed
   *
   */
  class GraphedListSelectionListener 
    implements ListSelectionListener 
  {
    public void valueChanged( ListSelectionEvent e ) 
    {
      JList list = (JList)e.getSource();

      //get selections
      int[] selections = list.getSelectedIndices();
      graph_selections = new String[ selections.length ];
      for( int i=0; i<selections.length; i++ )
        graph_selections[i] = (String)graphDLM.get( selections[i] ); 

      //update observers, if need be
      if(  !graphJL.getSelectionModel().isSelectionEmpty()  &&
            graphJL.getSelectedIndices().length == 1  )
      { 
        int selectedIndex = graphJL.getSelectionModel().getMaxSelectionIndex();
        String key = (String)graphDLM.get( selectedIndex );
        GraphableData value = (GraphableData)manager.get( key );
        data_set.setPointedAtIndex(  value.getData().getGroup_ID()  );
        data_set.notifyIObservers(  IObserver.POINTED_AT_CHANGED );
      }

      //adjust controls
      if(  graphDLM.size() > 0  )
      {
        removeJB.setEnabled( true );
        removeJB.paint(  removeJB.getGraphics()  );
      }
      else
      {
        removeJB.setEnabled( false );
        removeJB.paint(  removeJB.getGraphics()  );
      }

      addJB.setEnabled( false );
      graphJB.setEnabled( true );
    }
  }



  /**
   * listens to the list of data who's properties can be edited
   *
   */
  class PropertyListSelectionListener 
    implements ListSelectionListener 
  {
    public void valueChanged( ListSelectionEvent e ) 
    {
      JList list = (JList)e.getSource();

      //get selections
      int[] selections = list.getSelectedIndices();
      property_selections = new String[ selections.length ];
      for( int i=0; i<selections.length; i++ )
        property_selections[i] = (String)propertyDLM.get( selections[i] ); 

      //adjust
      //  controls....
      markerJL.getSelectionModel().clearSelection();
    }
  }



  /**
   * listens to the list of markers
   *
   */
  class MarkerListSelectionListener 
    implements ListSelectionListener 
  {
    public void valueChanged( ListSelectionEvent e ) 
    {
      JList list = (JList)e.getSource();

      //get selections
      int selection = list.getSelectedIndex();
      //System.out.println( "marker selection: " + selection );

      //adjust data values to reflect changes in the selected color
      if(  property_selections != null  &&  selection >= 0 )
      {
        for( int i=0;  i<property_selections.length;  i++ )
        {
          GraphableData d = (GraphableData)manager.get(property_selections[i]);
          d.setMarkerType(  new sgtMarker( markers[selection] )  ); 
          //System.out.println(  "setting marker for " + d.toString()  );
        }
      }
    }
  }



  /**
   * listens for mouse events that effect the data list in 'Select' tab
   *
   */
  class DoubleClickSelectionListener
    extends MouseAdapter
  {
    public void mouseClicked( MouseEvent e )
    {
      int count = e.getClickCount();
      if( count == 2 )
        addToGraphList( data_selections );
    }
  }


  /**
   * listens for mouse events that affect the list of selected data in 'Select'
   * tab.
   *
   */
  class DoubleClickDeselectionListener
    extends MouseAdapter
  {
    public void mouseClicked( MouseEvent e )
    {
      int count = e.getClickCount();
      if( count == 2 )
        removeFromGraphList( graph_selections );
    }
  }





/*----------------------------=[ list methods ]=------------------------------*/


  /**
   * clears everything from the list of data to be graphed
   */
  public void clearGraphList()
  {
    //remove all elements from propertyList
    clearPropertyList();

    for( int i=0; i<graphDLM.size(); i++ )
    {
      String id = (String)graphDLM.get( i );
      manager.remove( id ).setSelected( false );
      graphDLM.removeElement( id );
      //System.out.println( "cleared: " + id );  //**dbg**
    }
    dataJL.getSelectionModel().clearSelection();
    graphJL.getSelectionModel().clearSelection();
  }




  /**
   * adds selected spectra to the list of data to be graphed.  this method
   * only adds on basis of index, so if a selection is made and the data is
   * sorted before this method is called, the selection will be invalid.
   *
   */
/*
  protected void addToGraphList( int[] selections )
  {
    System.out.println( "ERROR: addToGraphList( int[] selections )" );
  }
*/  


  /**
   * marks a data block for graphing and adds data blocks to list
   */
  public void addToGraphList( GraphableData d )
  {
    if( !graphDLM.contains( d.getID() )  )
    {
      d.setSelected( true );
      d.setSelectionOrder(  manager.getNextOrder()  );
      manager.add( d );
      graphDLM.addElement(  d.getID()  );
    }

    if( manager.size() > 0 )
      graphJB.setEnabled( true );
  }



  /**
   * marks a number of data blocks for graphing
   */
  public void addToGraphList( String[] selections )
  {

    //adds selected spectra to the list in the 'Graph' tab
    addToPropertyList( selections );

    for( int i=0; i<selections.length; i++ )
    {

      //find the data block w/ the appropriate id
      GraphableData d = null;
      for( int j=0; j<data.length; j++ )
        if(  data[j].getID().compareTo( selections[i].toString() ) == 0  )
        {
          d = data[j];
          break;
        }
        

      //add to list of specta to be graphed (if it's not already there)
      if( !graphDLM.contains( d.getID() )  )
      {
        d.setSelected( true );
        d.setSelectionOrder(  manager.getNextOrder()  );
        manager.add( d );
        graphDLM.addElement(  d.getID()  );
      }
    }

    graphJB.setEnabled( true );
  }




  /**
   * removes data blocks from list of data to be graphed
   */
  protected void removeFromGraphList( String[] selections )
  {

    //removes selected spectra from the list in the 'Graph' tab
    removeFromPropertyList( selections );

    for( int i=0;  i<selections.length;  i++ )
    {
      String id = selections[i];
      manager.remove( id ).setSelected( false );
      graphDLM.removeElement( id );

      //System.out.println( "removed: " + id );
    }
    graphJL.getSelectionModel().clearSelection();
  }



  /** 
   * add an element to the list of selected spectra in the 'Graph' tab.  the
   * string that is added is the key for retrieving the appropriate 
   * GraphableData object.
   */
  public void addToPropertyList( String[] selections )
  {
    for( int i=0;  i<selections.length;  i++ )
      if(  propertyDLM == null  )
        return;
      else if(  !propertyDLM.contains( selections[i] )  )
        propertyDLM.addElement( selections[i] );
  }



  /** 
   * remove all items from the list used for editing properties
   *
   */
  public void clearPropertyList()
  {
    propertyDLM.clear();
  }



  /** 
   * add an element to the list of selected spectra in the 'Graph' tab.  the
   * string that is added is the key for retrieving the appropriate 
   * GraphableData object.
   */
  public void removeFromPropertyList( String[] selections )
  {
    for( int i=0;  i<selections.length;  i++ )
      propertyDLM.removeElement( selections[i] );
  }



/*------------------------------=[ misc ]=------------------------------------*/

  /**
   * returns an array of Strings.  used to plot data in the order that it was
   * selected.
   */
  protected String[] getSelectedKeyList()
  {
    Object[] objects = graphDLM.toArray();
    String[] strings = new String[ objects.length ];

    for( int i=0;  i<graphDLM.toArray().length;  i++ )
      strings[i] = (String)objects[i];

    return strings;
  }
  




/*---------------------------=[ private data ]=-------------------------------*/

  private final String log = "GraphableData Object created";
  private final String newline = "\n";
  private final String nl = "\n";

  private int selectionOrder = 0;
  private DataSet data_set = null;
  private TextRangeUI xrange, yrange; 
  private GraphableDataManager manager = null;

  //used in 'Select' tab
  private DefaultListModel dataDLM, graphDLM; 
  private JList dataJL, graphJL;

  /* used to communicate list selections between the listener and a function
     the strings are, incidently, the key to their corresponding GraphableData 
     object in 'manager' */
  private String[] data_selections;
  private String[] graph_selections;
  private String[] property_selections;

  //used in 'Graph' tab
  private DefaultListModel propertyDLM, colorDLM, markerDLM; 
  private JList propertyJL, markerJL;

  //listen to x and y rangeUIs
  private xrangeActionListener xrangeListener;
  private yrangeActionListener yrangeListener;

  private JButton graphJB, addJB, removeJB;
  private GraphableData[] data;

  private float percent_offset;

  //'Graph' tab lists 
  private Color[] colors;
  private int[] markers;

  private JButton graphTabApplyJB;
  private JColorChooser chooserJCC;
}



