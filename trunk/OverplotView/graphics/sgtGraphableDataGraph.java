package OverplotView.graphics;

/**
 * $Id$
 *
 * graphical output panel
 *
 * $Log$
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
import java.util.Vector;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;

import OverplotView.GraphableData;
import OverplotView.IGraphableDataGraph;


public class sgtGraphableDataGraph 
  extends JApplet
  implements IGraphableDataGraph
{ 

  JButton tree_;
  JButton space_ = null;
  JPane pane_;
  MyMouse myMouse_;
  LineAttributeDialog lad_;

  Vector data;
  JPlotLayout ltsl;

  /** 
   * default constructor 
   */
  public sgtGraphableDataGraph()
  {
    data = new Vector();
  }





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


  private SGTData convert_GraphableData_to_SGTData( GraphableData d )
  {

//TODO: code this converstion function

    return new SimpleLine();
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
    if( data.size() == 0 )
      data = gd;
    else
      for( int i=0;  i<gd.size();  i++ )
        data.addElement( 
          convert_GraphableData_to_SGTData( (GraphableData)gd.elementAt(i) )  );
  }


  /**
   * draws the graph
   */
  public JComponent redraw()
  {
    System.out.println( 
      "if there were a graphic output, it'd be redrawing right now" );

    ltsl = new JPlotLayout( (SGTData)data.firstElement(), 
                            "Time Series Demo", 
                            null, 
                            false );

    for( int i=1;  i<data.size();  i++ )
      //ltsl.addData( (SGTData)data.get(i), ""+i );
      //ltsl.addData( (Collection)data.get(i), ""+i );
      ltsl.addData( (SGTLine)data.get(i), ""+i );

    ltsl.setTitles("Time Series Demo", 
                   "using JPlotLayout", 
                   "");

    myMouse_ = new MyMouse();
    ltsl.addMouseListener(myMouse_);

    ltsl.setOverlayed( false );
    return ltsl;
  }

}
