package OverplotView;

/**
 * $Id$
 * ----------
 *
 * GraphableDataManager is the top level component for the SelectedGraphView.
 * it interacts with the ViewManager, acts as a container for the data to 
 * be graphed, and manages the the graphical representation of its data.
 * ----------
 *
 * $Log$
 * Revision 1.13  2001/12/21 18:22:18  dennis
 * Doubled the y-shift amount.
 * Removed debug print of selection messages.
 *
 * Revision 1.12  2001/12/21 17:53:46  dennis
 * -Implemented offsets for graphs              (Ruth)
 * -Fixed error message when no data blocks are selected
 *
 * Revision 1.11  2001/12/13 18:59:49 Ruth
 * -Eliminated the AUX_EXIT option
 * -Implemented the Data Label attribute to label spectra in the
 *  selected graph view
 *
 * Revision 1.10  2001/12/12 21:20:52  dennis
 * Fixed problem with redrawing display when selections were changed
 * in the ImageView. ( Ruth )
 *
 * Revision 1.9  2001/09/27 19:49:55  dennis
 * Added editing for labels, line styles, etc.
 *
 * Revision 1.8  2001/09/03 18:57:04  dennis
 * Redraws whole graph on any update.  Previously the axes labels were
 * not changed. (Ruth)
 *
 * Revision 1.7  2001/08/30 14:41:03  dennis
 * Changed the title of a line to the Group_ID
 * not its index. (Ruth)
 *
 * Revision 1.6  2001/08/15 18:29:15  rmikk
 * If no Data blocks are selected, A screen with that message
 *     appears.
 * The View now responds to notifications when the selected
 *    groups change and the data set changes.
 *
 * Revision 1.5  2001/08/13 14:40:46  rmikk
 * Added layout and event code to ensure that the graph takes
 * up the whole pane and that it shows at first and when
 * resized
 *
 * Revision 1.4  2001/06/29 16:29:25  neffk
 * integrated ColorAttribute class for storing color information in
 * GraphableData objects
 *
 * Revision 1.3  2001/06/28 22:05:04  neffk
 * data is converted to GraphableData on redraw() instead of an explicit call.
 * also, the conversion function creates (subclasses of) Attributes to store
 * things like offset and colors.  the constructor now sets units and labels in
 * the graph using Attributes.
 *
 * Revision 1.2  2001/06/27 16:50:10  neffk
 * this class was formerly implementing IObserver and extending DataSetViewer,
 * which forced redraw(...) and update(...).  these two functions are
 * redundant, as well as the IObserver interface.  this class not longer
 * implements IObserver, and update(...) has been removed.
 * ----------
 */
 import DataSetTools.dataset.*;
//import DataSetTools.dataset.Attribute;
//import DataSetTools.dataset.AttributeList;
//import DataSetTools.dataset.FloatAttribute;
//import DataSetTools.dataset.StringAttribute;
//import DataSetTools.dataset.Data;
//import DataSetTools.dataset.DataSet;
import DataSetTools.viewer.DataSetViewer;
import DataSetTools.util.*;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.*;
//import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import OverplotView.GraphableData;
import OverplotView.IGraphableDataGraph;
import OverplotView.graphics.sgtGraphableDataGraph;
import java.awt.*;
import java.awt.event.*;
import gov.noaa.pmel.sgt.swing.*;
public class GraphableDataManager 
  extends DataSetViewer
{

  public static final String AUX_EXIT = "Auxiliary Exit";

  private Vector                graphable_data;
  private sgtGraphableDataGraph graph;
   AttributeList attrs;
  private String Error;
  JCheckBoxMenuItem HShift,VShift;

  /**
   * default constructor
   */
  public GraphableDataManager( DataSet data_set )
  {
    super(data_set);
    JMenu jm = menu_bar.getMenu(DataSetViewer.EDIT_MENU_ID);
    JMenuItem jmi = new JMenuItem("Graph");
    jm.add( jmi);
    jmi.addActionListener(new OptionMenuHandler()  );
    inittt( data_set );
  }
  private void inittt( DataSet data_set )
   {
    setLayout( new GridLayout( 1 , 1 ));
    JMenu option_menu = menu_bar.getMenu( OPTION_MENU_ID );
     OptionMenuHandler option_menu_handler = new OptionMenuHandler();
     HShift = new JCheckBoxMenuItem( "Horizontal Shift", false);
     VShift = new JCheckBoxMenuItem( "Vertical Shift",false);
     
     option_menu.add( HShift  );
     option_menu.add( VShift );
    HShift.addActionListener( option_menu_handler);
    VShift.addActionListener( option_menu_handler);
    Error =null;
    if( data_set == null)
	Error = "No data Set";
    else if( data_set.getSelectedIndices().length <=0)
        Error = "No Data Blocks are Selected";
   
       
    graph = new sgtGraphableDataGraph();
    StringAttribute title, subtitle1, subtitle2, 
                    x_units, y_units, x_label, y_label;
    title = new StringAttribute( IGraphableDataGraph.TITLE,
                                 data_set.getTitle()  );
    subtitle1 = new StringAttribute( IGraphableDataGraph.TITLE_SUB1,
                                     "" );
    subtitle2 = new StringAttribute( IGraphableDataGraph.TITLE_SUB2,
                                     "" );
    x_units = new StringAttribute(  IGraphableDataGraph.X_UNITS,
                                    data_set.getX_units()  );
    y_units = new StringAttribute(  IGraphableDataGraph.Y_UNITS,
                                    data_set.getY_units()  );
    x_label = new StringAttribute(  IGraphableDataGraph.X_LABEL,
                                    data_set.getX_label()  );
    y_label = new StringAttribute(  IGraphableDataGraph.Y_LABEL,
                                    data_set.getY_label()  );
    
    attrs = new AttributeList();
    attrs.addAttribute( title );
    attrs.addAttribute( subtitle1 );
    attrs.addAttribute( subtitle2 );
    attrs.addAttribute( x_units );
    attrs.addAttribute( y_units );
    attrs.addAttribute( x_label );
    attrs.addAttribute( y_label );
    attrs.addAttribute( new FloatAttribute("Xshift",0.0f));
    attrs.setAttribute( new FloatAttribute("Yshift",0.0f));
    graph.setAttributeList( attrs );

    redraw( );

    //modify the menu provided by DataSetViewer
   ;
    
     
    //JMenuItem exitButton = new JMenuItem( AUX_EXIT );
    //exitButton.addActionListener( option_menu_handler );
    //option_menu.add( exitButton );
   
  }

  /** Needed to prevent window from drawing a blank screen in some cases.
  * This routine just calls super.paint
  */
  public void paint( Graphics g)
     {super.paint(g);
      }
  /**
   * This will be called by the "outside world" if the contents of the
   * DataSet are changed and it is necesary to redraw the graphs using the
   * current DataSet.  one example of such a situationn is a change in 
   * selection.
   */
  public void redraw( String reason ) 
  {  //inittt( getDataSet() );
//    System.out.println( "DataSetViewer> " + reason + ","+ 
//                         IObserver.SELECTION_CHANGED );
      
    if ( reason == IObserver.DESTROY )
    {
      graphable_data = null;
      graph = null;  
      redraw();
    }
    else if( reason == IObserver.DATA_REORDERED)
    {
    }
    else if( reason == IObserver.DATA_DELETED )
    {
    }
    else if( reason == IObserver.SELECTION_CHANGED )
    { DataSet ds = getDataSet();
       Error = "No Data Blocks are Selected";
       if( ds.getSelectedIndices().length  > 0)
           Error = null;
       redraw();
      
    }
    else if( reason == IObserver.POINTED_AT_CHANGED )
    {
    }
    else if( reason == IObserver.GROUPS_CHANGED )
    {  
       
       redraw();
    }
    else if( reason == IObserver.DATA_CHANGED )
    { Attribute  x_label = new StringAttribute(  IGraphableDataGraph.X_LABEL,
                                    getDataSet().getX_label()  );
      attrs.removeAttribute(IGraphableDataGraph.X_LABEL);
      attrs.addAttribute( x_label );
      
      graph.setAttributeList( attrs);
      redraw();
    }
    else if( reason == IObserver.ATTRIBUTE_CHANGED )
    {
     redraw();
    }
    else if( reason == IObserver.FIELD_CHANGED )
    {inittt( getDataSet());
     redraw();
    }
    else if( reason == IObserver.HIDDEN_CHANGED )
    {
    }
    else if( reason == DataSetViewer.NEW_DATA_SET )
     {//System.out.println("in NEW_DATA_SET");
      inittt( getDataSet());
      redraw();
     } 
    //else  don't redraw
      //redraw();                      
                                     //default is to redraw the entire
                                        //viewer so that future expansions
                                        //in the variety of messages
                                        //will not break existing code.
                                        //however, if there are
                                        //effecient ways to update the viewer,
                                        //please maintain this code to catch
                                        //to deal with the update appropriately.

  }


  /**
   * updates the graphic visualization object, whatever that might be.
   * first, all currently selected data is added converted from Data 
   * objects to GraphableData objects
   */
  public void redraw()
  {  
                                      //convert data from Data objects
                                       //to GraphableData objects
    convert_Data_to_GraphableData();

                                       //pass the data down to the
                                       //graphic component that's handeling
                                       //the actual graphing of the data
    graph.init( graphable_data );

                                       //replace the previous graph with
                                       //the new graph that redraw() 
                                       //generates
    removeAll();
    
    JComponent graph_component ;
    if( Error == null)
      {graph_component = graph.redraw();
       graph_component.setLayout( new GridLayout( 1,1 ));
       graph_component.addComponentListener( new 
             MyComponentListener(graph.getJPane()));
        graph_component.doLayout();
      }
    else
     {graph_component = (new JTextArea( 15 , 30 ));
      ((JTextArea)graph_component).setText("\n\n    "+Error);
     
     }
    
    add( graph_component );

                                       //ask swing to redraw our new
                                       //additions to the DataSetViewer
    validate();
    graph_component.repaint();
    //setVisable( true );
  }

  public class MyComponentListener extends ComponentAdapter
   {JPlotLayout g;
    public MyComponentListener( JPlotLayout g)
      {this.g = g;
      }
    public void componentResized(ComponentEvent e)
     {g.draw();
     }
    public void componentShown(ComponentEvent e)
     { g.draw();
     }
   }
  /**
   * This will be called by the "outside world" if the viewer is to replace 
   * its reference to a DataSet by a reference to a new DataSet, ds, and
   * rebuild the entire display, titles, borders, etc.
   */
  public void setDataSet( DataSet ds )
  {   
    super.setDataSet( ds );
    this.removeAll(); 
    this.redraw( IObserver.DATA_CHANGED );
  }


/*--------------------------------=[ private ]=-------------------------------*/


  /** 
   * constructs a parallel of the selected Data blocks that are held by this
   * DataSetViewer, but stores them as GraphableData objects.
   */
  private void convert_Data_to_GraphableData()
  {
    graphable_data = new Vector();
    float dx=0; 
    float dy=0;
    int nn = getDataSet().getNumSelected();
    float xx= 50;
    if( nn>12) xx = 4*nn;
    if( HShift.getState() && (nn>0))
      { UniformXScale xs;
        xs = getDataSet().getXRange();
        dx= (xs.getEnd_x()-xs.getStart_x())/xx ;  
       }
   if( VShift.getState() && (nn > 0)) dy = 1.0f; //tag
    /*  {ClosedInterval  ys= getDataSet().getYRange();
       float xx=50;
       if( nn>12) xx=4*nn;
       dy=(ys.getEnd_x()-ys.getStart_x())/xx;

       }
   */
    
    float MXY = java.lang.Float.POSITIVE_INFINITY ;
    float MNY = java.lang.Float.NEGATIVE_INFINITY;
    int kk = 0;
    for( int i=0;  i<getDataSet().getNum_entries();  i++ )
      if(  getDataSet().getData_entry(i).isSelected()  )
      {
        GraphableData d = new GraphableData( getDataSet().getData_entry(i) );
         // new GraphableData(  getDataSet().getData_entry(i)  );


                                          //create all of the attributes
                                          //that we want our data to
                                          //have.  (generally, these are
                                          //graphical details)  then add
                                          //them to GraphableData object

        FloatAttribute offset_attr = new FloatAttribute( GraphableData.OFFSET,
                                                         0.0f );
	Data Dat = getDataSet().getData_entry(i);
	Attribute O = Dat.getAttribute( "Label");
	 						                
        String name_str =null;
	if( O != null)
	  if( O instanceof StringAttribute)
	     name_str= ((StringAttribute) O).getStringValue();
	if( name_str == null)	
	  name_str = new String( "Group # " + 
                         getDataSet().getData_entry(i).getGroup_ID() );
			 
        StringAttribute name_attr = new StringAttribute( GraphableData.NAME,
                                                         name_str );
        ColorAttribute color_attr = new ColorAttribute( GraphableData.COLOR,
                                                        Color.black );
        d.addAttribute( offset_attr );
        d.addAttribute( name_attr );
        d.addAttribute( color_attr );
        d.addAttribute( new FloatAttribute("Xshift", kk*dx ));
        d.addAttribute( new FloatAttribute("Yshift", kk*dy+0.0f ));
        kk++;                                  
                                          //add the new GraphableData object
                                          //to the list of data to be
        graphable_data.add( d );          //visualized


        float [] ys;
        ys = Dat.getY_values();
        if( ys != null)
          for( int k = 0; k < ys.length;k++)
            {if(MXY == java.lang.Float.POSITIVE_INFINITY) MXY = ys[k];
             else if( ys[k] > MXY) MXY = ys[k];
             if(MNY == java.lang.Float.NEGATIVE_INFINITY) MNY = ys[k];
             else if( ys[k] < MNY) MNY =ys[k]; 
             } 
      }
    
     FloatAttribute FF =  new FloatAttribute( "Yshift",2*(MXY-MNY)/xx);
    
     attrs.setAttribute( FF );
    
     graph.setAttributeList( attrs);
  }


  /**
   *  Listen for Option menu selections and just print out the selected option.
   *  It may be most convenient to have a separate listener for each menu.
   */
  private class OptionMenuHandler 
    implements ActionListener 
  {
    public void actionPerformed( ActionEvent e ) 
    {
      String action = e.getActionCommand();
     if( (action.equals("Horizontal Shift")) || (action.equals("Vertical Shift")))
       { redraw();
        }
     else if( action.equals("Graph"))
       {if( graph !=null)
         {JPlotLayout jp = graph.getJPane();
          if( jp !=null)
           {JClassTree ct = new JClassTree();
           ct.setModal(false);
           ct.setJPane( jp);
           ct.show();
           }
         }
       }
    }
  }
}



