package OverplotView.graphics;

/**
 * $Id$
 *
 * graphical output panel
 *
 * $Log$
 * Revision 1.5  2001/06/29 15:06:55  neffk
 * the correct labels, units, and title appear on the graph.  also, the number
 * of spectra now correspond to the number of selections (fixed in previous
 * revision).
 *
 * Revision 1.4  2001/06/29 14:17:47  neffk
 * graph no longer has a stray line going from the origin to the end of the
 * spectra.  also, this class no longer extends JApplet.
 *
 * Revision 1.3  2001/06/28 22:08:46  neffk
 * GraphableData --> SGTData converter in complete.  it graphs all data, making
 * appropriate changes for tabulated functions or histogram data.
 *
 * Revision 1.2  2001/06/27 16:51:36  neffk
 * this class now implements the IGraphableDataGraph interface.
 *
 */


import gov.noaa.pmel.sgt.swing.JPlotLayout;
import gov.noaa.pmel.sgt.swing.JClassTree;
import gov.noaa.pmel.sgt.swing.prop.LineAttributeDialog;
import gov.noaa.pmel.sgt.JPane;
import gov.noaa.pmel.sgt.LineCartesianRenderer;
import gov.noaa.pmel.sgt.LineAttribute;
import gov.noaa.pmel.sgt.demo.TestData;
import gov.noaa.pmel.util.GeoDate;
import gov.noaa.pmel.util.TimeRange;
import gov.noaa.pmel.util.IllegalTimeValue;
import gov.noaa.pmel.sgt.dm.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;
import java.util.Vector;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import OverplotView.GraphableData;
import OverplotView.IGraphableDataGraph;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Data;

public class sgtGraphableDataGraph 
  implements IGraphableDataGraph
{ 

  JButton tree_;
  JButton space_ = null;
  JPane pane_;
  MyMouse myMouse_;
  LineAttributeDialog lad_;

  private Vector        data;
  private JPlotLayout   ltsl;
  private AttributeList attrs;


  /** 
   * default constructor 
   */
  public sgtGraphableDataGraph()
  {
    data = new Vector();
    attrs = new AttributeList();
  }


  /**
   * type converter.  this function converts GraphableData to SimpleLine
   * objects.
   */
  private SGTData convert_GraphableData_to_SGTData( GraphableData d )
  {
                                           //set up names and units
                                           //meta data for each line
    String x_label = (String)attrs.getAttributeValue( 
                                                 IGraphableDataGraph.X_LABEL );
    String x_units = (String)attrs.getAttributeValue( 
                                                 IGraphableDataGraph.X_UNITS );
    String y_label = (String)attrs.getAttributeValue( 
                                                 IGraphableDataGraph.Y_LABEL );
    String y_units = (String)attrs.getAttributeValue( 
                                                 IGraphableDataGraph.Y_UNITS );
    SGTMetaData x_meta = new SGTMetaData( x_label, x_units );
    SGTMetaData y_meta = new SGTMetaData( y_label, y_units );

                                    //get the y-values and convert
                                    //them to double[]
    Data data_block = d.getData();
    float[] yf = data_block.getY_values();
    double[] y_values = new double[ yf.length ];
    for( int i=0;  i<yf.length;  i++ )
      y_values[i] = (double)yf[i];


                                    //get the x-values and convert
                                    //them to double[]
    float[] xf = data_block.getX_scale().getXs();
    double[] x_values = new double[ xf.length ];
    for( int i=0;  i<xf.length;  i++ )
      x_values[i] = (double)xf[i];
    

                                //there are two formats possible
                                //for Data: tabulated function or histogram.
                                //tabulated functions are easy to deal with
                                //because there is an x-value for every y-value.
                                //however, histograms are constructed of bin
                                //boundaries and values.  that means that either
                                //the x- or y-values will be fence posts, which
                                //ever is larger by 1.


                                //graph histogram data.  there are a variety of
                                //ways to do this... one could arbitrarily
                                //choose the left or right bin boundary and
                                //force it to correspond to the value, or find
                                //the center of the bin, or actually draw it as
                                //a histogram, with a 'bump' for every bin.
    if( data_block.isHistogram()  )
    {
      double[] x = new double[  x_values.length * 2 - 2 ]; 
      double[] y = new double[  y_values.length * 2     ];

      for( int i=0;  i<y_values.length;  i++ )
      {
/*
                         //draws the histogram with sides.  
                         //if this is used in the future, 
                         //increase the size of x[] and y[], 
        x[i] = x_values[j];    //left side of the bin
        y[i]=0.0;              // ...  
        x[i+1] = x_values[j];  // ...
        y[i+1] = y_values[j];  // ...

        x[i+2] = x_values[j+1]; //top of bin
        y[i+2] = y_values[j];   // ...

        x[i+3] = x_values[j+1];  //right side of the bin
        y[i+3] = 0.0;            // ...
*/

                              //draws the histogram as a series 
                              //of connected horizontal lines
        x[ i*2 ] = x_values[i];
        y[ i*2 ] = y_values[i];
        x[ i*2 + 1 ] = x_values[i+1];
        y[ i*2 + 1 ] = y_values[i];

      }

      x_values = x;
      y_values = y;
    }
                                  

    SimpleLine l = new SimpleLine(  x_values, 
                                    y_values,
                                    (String)attrs.getAttributeValue( 
                                                    GraphableData.NAME )  );
    l.setXMetaData( x_meta );
    l.setYMetaData( y_meta );

    return l;
  }


/*----------------------=[ required interface methods ]=----------------------*/


  /**
   * adds data to this container or replaces it.  in order to keep things
   * simple for me, there's no way to incrementally build this graph by adding
   * one bit of data at a time, so developers should keep track of it in their 
   * own code. 
   */
  public void init( Vector gd )
  {
    data.removeAllElements();
    for( int i=0;  i<gd.size();  i++ )
      data.addElement( 
        convert_GraphableData_to_SGTData( (GraphableData)gd.elementAt(i) )  );
  }


  /**
   * draws the graph
   */
  public JComponent redraw()
  {
    if(  data == null  )
    {
      System.out.println( 
        "if there were a graphic output, it'd be redrawing right now" );

      return new JPanel();
    }
    
    System.out.println( "redrawing..." );

    JPanel graph_panel = new JPanel();
    if(  data.size() > 0  )
    {

      ltsl = new JPlotLayout( (SGTData)( data.firstElement() ), 
                              "Time Series Demo", 
                              null, 
                              false );

      for( int i=0;  i<data.size();  i++ )
        //ltsl.addData( (SGTData)data.get(i), ""+i );
        //ltsl.addData( (Collection)data.get(i), ""+i );
        ltsl.addData( (SGTLine)data.get(i), ""+i );

      ltsl.setTitles( 
        (String)attrs.getAttributeValue( IGraphableDataGraph.TITLE ),
        (String)attrs.getAttributeValue( IGraphableDataGraph.TITLE_SUB1 ),
        (String)attrs.getAttributeValue( IGraphableDataGraph.TITLE_SUB2 )  );
        
      myMouse_ = new MyMouse();
      ltsl.addMouseListener(myMouse_);

      ltsl.setOverlayed( false );
      
      graph_panel.add( ltsl );
    }
    graph_panel.setLayout(  new FlowLayout( FlowLayout.CENTER, 0, 0 )  );
    graph_panel.doLayout();
    return graph_panel;
  }


  /**
   * set units, labels, etc
   */
  public void setAttributeList( AttributeList l )
  {
    attrs = l;
  }



/*----------------------------=[ inner classes ]=-----------------------------*/

  /**
   * shows editable attributes when editable items are clicked upon
   */
  class MyMouse extends MouseAdapter {
    public void mouseReleased(MouseEvent event) {
      Object object = event.getSource();
      if(object == pane_)
	maybeShowLineAttributeDialog(event);
    }
    
    void maybeShowLineAttributeDialog(MouseEvent e) {
      if(e.isPopupTrigger() || e.getClickCount() == 2) {
	Object obj = pane_.getObjectAt(e.getX(), e.getY());
	pane_.setSelectedObject(obj);
	if(obj instanceof LineCartesianRenderer) {
	  LineAttribute attr = ((LineCartesianRenderer)obj).getLineAttribute();
	  if(lad_ == null) {
	    lad_ = new LineAttributeDialog();
	  }
	  lad_.setLineAttribute(attr);
	  if(!lad_.isShowing())
	    lad_.setVisible(true);
	}
      }
    }
  }
}
