package OverplotView.graphics;

/**
 * $Id$
 *
 * $Log$
 * Revision 1.1  2001/06/21 21:46:30  neffk
 * *** empty log message ***
 *
 */

import java.util.Vector;
import OverplotView.GraphableDataGraph;

public class sgtGraphableDataGraph 
  implements GraphableDataGraph
{

  public void init( Vector gd )
  {
    System.out.println( "selected:" );
    for( int i=0;  i<gd.size();  i++ )
      System.out.println(  gd.get(i).toString()  );

    System.out.println( "----------" );
  }


  public void redraw()
  {
    System.out.println( 
      "if there were a graphic output, it'd be redrawing right now" );
  }
}

import java.util.Vector;

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

import java.awt.*;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JTimeSeries extends JApplet {
  JButton tree_;
  JButton space_ = null;
  JPane pane_;
  MyMouse myMouse_;
  LineAttributeDialog lad_;

  Vector data;
  JPlotLayout ltsl;

  

  /** default constructor */
  public JTimeSeries()
  {
    data = new Vector();
  }



  /** takes a vector of SimpleLine objects.  GraphableDataManager has already
   *  taken care of the messy details of offsets and such, so all we have 
   *  to do is visualize the data.
   */
  public JTimeSeries( Vector d )
  {
    if( data.size() == 0 )
      data = d;
    else
      for( int i=0;  i<d.size();  i++ )
        data.addElement( d.elementAt(i) );
  }



  public void addData( Collection c )
  {
    data.add( c );
  }



  public void addData( Vector d )
  {
    if( data.size() == 0 )
      data = d;
    else
      for( int i=0;  i<d.size();  i++ )
        data.addElement( d.elementAt(i) );
  }


 
  /** this is the main method of this class.  it creates a graphics screen 
   * and adds all of the current data to it. 
   */
  JPlotLayout makeGraph() 
  {
    
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



  class MyAction implements java.awt.event.ActionListener {
    public void actionPerformed(java.awt.event.ActionEvent event) {
      System.out.println( "mouse event" );
      /*
      Object obj = event.getSource();
      if(obj == space_) {
	System.out.println("  <<Mark>>");
      }
      if(obj == tree_)
	tree_actionPerformed(event);
      */
    }
  }

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
