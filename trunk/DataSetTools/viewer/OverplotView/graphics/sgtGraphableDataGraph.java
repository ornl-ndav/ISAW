/*
 * File: sgtGraphableDataGraph.java
 *
 * Copyright (C) 2001, Kevin Neff
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
 * $Log$
 * Revision 1.2  2002/11/27 23:25:22  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/07/18 22:06:56  dennis
 * Moved separate OverplotView hiearchy into DataSetTools/viewer
 * hierarchy.
 *
 * Revision 1.9  2001/12/21 17:54:27  dennis
 * -Implemented offsets for graphs (Ruth)
 *
 */

package DataSetTools.viewer.OverplotView.graphics;

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

import DataSetTools.viewer.OverplotView.*;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.AttributeList;
import DataSetTools.dataset.Data;

/**
 * graphical output panel
 */
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
    ltsl = null;
  }


  /**
   * type converter.  this function converts GraphableData to SimpleLine
   * objects.
   */
  private SGTData convert_GraphableData_to_SGTData( GraphableData d ,float YShift1)
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
    float dx=0;
    float dy =0;
    Object F = d.getAttributeList().getAttributeValue( "Xshift");
    if( F != null)
       dx = ((Float) F).floatValue();
    F = d.getAttributeList().getAttributeValue( "Yshift");
    if( F != null)
       dy = ((Float) F).floatValue()*YShift1;
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
        x[ i*2 ] = x_values[i] +dx;
        y[ i*2 ] = y_values[i]+dy;
        x[ i*2 + 1 ] = x_values[i+1]+dx;
        y[ i*2 + 1 ] = y_values[i]+dy;

      }

      x_values = x;
      y_values = y;
    }
                                  

    SimpleLine l = new SimpleLine(  x_values, 
                                    y_values,
                                    (String)d.getAttributeList().
                                            getAttributeValue( 
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
    Object XX = attrs.getAttributeValue( "Yshift");
    float DY = 0.0f;
    if( XX != null)
       DY = ((Float)XX).floatValue();
   
    for( int i=0;  i<gd.size();  i++ )
      data.addElement( 
        convert_GraphableData_to_SGTData( (GraphableData)gd.elementAt(i) ,DY) );
  }

public JPlotLayout getJPane()
   { return ltsl;
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
    
    //System.out.println( "redrawing..." );

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
	  { SimpleLine sl =(SimpleLine)data.get(i);
          ltsl.addData( sl, sl.getTitle());
        }

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
