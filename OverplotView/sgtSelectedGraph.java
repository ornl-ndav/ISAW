package OverplotView;

/**
 * $Id$
 *
 * graphing implemented by the sgt package
 *
 * $Log$
 * Revision 1.3  2000/07/17 14:55:21  neffk
 * sloppy checkin to correct previous checkin errors
 *
 * Revision 1.2  2000/07/07 21:57:13  neffk
 * changed the aspect ratio of the graph to 1:2 (vertical:horizontal)
 *
 * Revision 1.1.1.1  2000/07/06 16:17:44  neffk
 * imported source code
 *
 * Revision 1.12  2000/06/22 14:04:30  neffk
 * new comments and a few changes in formatting
 *
 * Revision 1.11  2000/06/20 20:57:23  neffk
 * *** empty log message ***
 *
 * Revision 1.10  2000/06/15 14:51:18  neffk
 * redraw works again
 *
 * Revision 1.9  2000/06/01 18:50:33  neffk
 * set title lines of the graph to zero length so that they wouldn't interfere
 * w/ the legend/key
 *
 * Revision 1.8  2000/05/31 22:14:56  neffk
 * uses Pane.draw() instead of using a constructor to redraw graph
 *
 * Revision 1.7  2000/05/02 09:13:23  psam
 * deleted some debug info
 *
 * Revision 1.6  2000/05/02 08:24:40  psam
 * added float percent_offset to addData()
 *
 * Revision 1.5  2000/05/01 05:46:10  psam
 * 1) Graphs with positive yaxis (solution achieved by calling the 4 arg
 * constructor of SGTMetaData.  default behavior of the graph is a negative
 * axis, so you have to reverse the y-axis, as awakward as that may seem)
 * 2) only attempts to set ranges when there is data attached
 * 3) cut out some debug println's
 *
 * Revision 1.4  2000/04/30 20:21:15  psam
 * uses isawLineProfileLayout
 *
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import OverplotView.components.containers.*;
import OverplotView.util.*;

import DataSetTools.dataset.*;
import DataSetTools.util.*;

import gov.noaa.noaaserver.sgt.*;
import gov.noaa.noaaserver.sgt.datamodel.*;
import gov.noaa.noaaserver.sgt.demo.*;
import gov.noaa.noaaserver.sgt.awt.*;


public class sgtSelectedGraph 
     extends     JComponent
     implements  SelectedGraph
{
  
  isawLineProfileLayout graph = null;
  JPanel graphPanel;
  private Vector dataBlocks;
  private Vector graphableDataBlocks;
  int dataCount;  //keeps track of how many data blocks  
                  //have been added to the graph (as
                  //opposed to dataBlocks)

   


  /**
   * DEFAULT CONSTRUCTOR
   * 
   */
  public sgtSelectedGraph( JPanel p )
  {
//    color_list = new EntityColor[4];
//    color_list[0] = new DataColor( Color.red );
//    color_list[1] = new DataColor( Color.green );
//    color_list[2] = new DataColor( Color.blue );
//    color_list[3] = new DataColor( Color.cyan );

    dataBlocks = new Vector();
    graphableDataBlocks = new Vector();
    graphPanel = p;
    redraw();
  }



  /**
   * constructs a RedrawInstruciton that forces the graph to be redrawn
   */
  public void redraw()
  {
    RedrawInstruction instruction = new RedrawInstruction( true,
                                                           false,
                                                           null,
                                                           null,
                                                           null );
    redraw( instruction );
  }



  /**
   * main mechanism for redrawing the graph
   */
  public void redraw( RedrawInstruction instruction )
  {

    if(  graph == null  &&  instruction.draw()  )
    {
      //System.out.println( "initializing graph..." );
      graph = new isawLineProfileLayout( 0.5, 4.5, 0.5, 4.5, 5, 5 );
      graphPanel.removeAll();
      graphPanel.setLayout(  new GridLayout( 1, 1 )  );
      graphPanel.add( graph );
      graphPanel.doLayout();
      graphPanel.invalidate();
      graphPanel.revalidate();
    }

    // add data to graph
    if(  instruction.add()  &&  !instruction.draw()  )
    {
      //System.out.println(  instruction.toString()  );
      graphableDataBlocks.add(  instruction.getData()  );
    }  
   
    // draw/update graph
    else if(  instruction.draw()  &&  
              graphPanel.isShowing()  &&
              graphableDataBlocks.size() > 0  &&
              graph != null  )
    {
      //System.out.println(  instruction.toString()  );


      //add data to the graph
      for( int i=0;  i<graphableDataBlocks.size();  i++ )
        graph.addData(  (GraphableData)graphableDataBlocks.get(i)  );

      if( instruction.getXRange() != null && instruction.getYRange() != null  )
      {
        graph.setXRange(  new Range2D( instruction.getXRange().x,
                                       instruction.getXRange().y )  );
        graph.setYRange(  new Range2D( instruction.getYRange().x,
                                       instruction.getYRange().y )  );
      }

      graphPanel.removeAll();
      graphPanel.setLayout(  new GridLayout( 1, 1 )  );
      graphPanel.add( graph );
      graphPanel.revalidate();


      if(  graphableDataBlocks.size() > 0  )
        graph.draw();
    }

      graphPanel.removeAll();
      graphPanel.setLayout(  new GridLayout( 1, 1 )  );
      graphPanel.add( graph );
      graphPanel.revalidate();
  }

  

  /**
   * clears all data from the graph
   *
   */
  public void clear()
  {
    if(  graphableDataBlocks.size() > 0  ) 
    {
      graph.clear();
      graphableDataBlocks.removeAllElements();
      dataCount = 0;
    }
  }



  /**
   * resets the JPanel that the graph is drawn in.
   *
   */
  public void setGraphPanel( JPanel jp )
  {
    graphPanel = jp;
  }
 


  public isawLineProfileLayout getGraph()
  {
    return graph;
  }
 


  /**
   * must be implemented to satisfy SelectedGraph implementation
   */
  public void calculateGraphSize( Dimension d )
  {
    System.out.println( "sgtSelectedGraph::calculateGraphSize(): " + d );

    //System.out.println(  "xsize: " + graph.getXSize()  );
    //System.out.println(  "ysize: " + graph.getYSize()  );
    //System.out.println(  "width: " + graph.getXAxisP().toString()  );
    //System.out.println(  "height: " + graph.getYAxisP().toString()  );

    double width = (double)d.width/100.0;
    double height = (double)d.height/100.0;

    System.out.println( "adjusted width: " + width );
    System.out.println( "adjusted height: " + height );

    graph.setXSize( width );
    graph.setYSize( height ); 
    graph.setXAxisP(  (width/10.0), width-(width/10.0)  );
    graph.setYAxisP(  (height/10.0), height-(height/10.0)  );

    System.out.println(  "xsize: " + graph.getXSize()  );
    System.out.println(  "ysize: " + graph.getYSize()  );
    System.out.println(  "width: " + graph.getXAxisP().toString()  );
    System.out.println(  "height: " + graph.getYAxisP().toString()  );

    //System.out.println(  "width: " + graph.getXAxisP().toString()  );
    //System.out.println(  "height: " + graph.getYAxisP().toString()  );

    graph.recalculateAxes();
  }


}
