package OverplotView.components.containers;

/**
 * $Id$
 * ----------
 *
 * maintains a list of GraphableData as well as state information for (and a 
 * reference to) a SelectedGraph.  the GraphableData objects are stored in
 * a Hashtable, where the key is give by the GraphableData object's getID()
 * method.
 * ----------
 *
 * changelog
 *  $Log$
 *  Revision 1.4  2000/07/12 14:58:39  neffk
 *  added a makefile for generating documentation
 *
 *  Revision 1.3  2000/07/06 20:11:19  neffk
 *  added these files, just to make sure, since they had a ? in front of them
 *  when updating.
 *
 *  Revision 1.3  2000/06/27 21:44:11  neffk
 *  *** empty log message ***
 *
 *  Revision 1.2  2000/06/26 15:07:18  neffk
 *  fixed offset problems (175-0)
 *
 *  Revision 1.1  2000/06/22 14:05:26  neffk
 *  Initial revision
 * ----------
 */

import DataSetTools.util.*;
import DataSetTools.components.ui.*;

import java.awt.*;
import java.lang.*;
import java.util.*;

import OverplotView.*;
import OverplotView.components.containers.*;

public class GraphableDataManager 
  extends Hashtable
{

  /**
   * this graph is used to visualize the data that this GraphableDataManager
   * is maintaining. 
   */
  SelectedGraph graph;

  //state information for associated SelectedGraph
  private float percent_offset;
  private floatPoint2D xrange, yrange;  //in world coordinates
  private TextRangeUI xrangeTRUI, yrangeTRUI;

  private float abs_offset;

  private boolean trace = false;


/*--------------------------=[ default constructor ]=-------------------------*/


  /**
   * DEFAULT CONSTRUCTOR
   *
   * initializes this object with a graph, xrange, yrange, and a list of 
   * colors.
   */
  public GraphableDataManager( SelectedGraph graph_ )
  {
    graph = graph_;
    percent_offset = 0.0f;
    xrange = new floatPoint2D( 0, 1 );  
    yrange = new floatPoint2D( 0, 1 ); 

    EntityColor[] color_list = new EntityColor[4];
    color_list[0] = new DataColor( Color.red );
    color_list[1] = new DataColor( Color.green );
    color_list[2] = new DataColor( Color.blue );
    color_list[3] = new DataColor( Color.black );
//    setColors( color_list );
  }




/*-------------------------------=[ redraw ]=---------------------------------*/

  /**
   * this is the top-level mechanism for redrawing the graph.
   * 'redraw' constructs a RedrawInstruction object and passes it to the
   * SelectedGraph that is associated with this GraphableDaraManager.
   */
  public void redraw()
  {
    if( trace )
      System.out.println( "GraphableDataManager.java::redraw()" );

    calculateRanges();
    graph.clear();

    GraphableData data = null;
    Collection listGDV = values();  //get GraphableData objects
    Object it = listGDV.iterator();

    int count = 0;
    while(  ((Iterator)it).hasNext()  )
    {
//      System.out.println( "adding..." );
   
      data = (GraphableData)((Iterator)it).next();
      //System.out.println(  data.toString()  );
      data.setOffset( new Integer(count).floatValue() * abs_offset );
      RedrawInstruction instruction = new RedrawInstruction(
           false,
           true,
           xrange,
           yrange,
           data  );

      graph.redraw( instruction );
      count++;
    }

    //redraw graph
//    System.out.println( "drawing..." );
    RedrawInstruction instruction = new RedrawInstruction(
         true,
         false,
         xrange,
         yrange,
         null );
    graph.redraw( instruction );
  }



  /**
   * redraws the graphs in the order of the list of keys
   *
   */
  public void redraw( String[] keys )
  {
    if( trace )
      System.out.println( "GraphableDataManager::redraw(String[])" );

    GraphableData data = null;

    calculateRanges();
    graph.clear();

    //add data in the order that corresponds to 'keys'
    for( int keyIndex=0;  keyIndex<size();  keyIndex++ )
    {
      if(   containsKey(  keys[keyIndex].toString()  )   )
      {
         data = (GraphableData)get( keys[keyIndex] );

         System.out.println( "key: " + keys[keyIndex].toString()  );
         if(  data.toString().compareTo( keys[keyIndex].toString() ) == 0  )
           System.out.println( "match" );

         data.setOffset( keyIndex * abs_offset );
         RedrawInstruction instruction = new RedrawInstruction(
           false,
           true,
           xrange,
           yrange,
           data  );
      }
    }

    //redraw graph
    RedrawInstruction instruction = new RedrawInstruction(
         true,
         false,
         xrange,
         yrange,
         null );
    graph.redraw( instruction );
  }




/*------------------------=[ range & offset methods ]=------------------------*/


  /**
   * sets data offset.  this offset is applied to each data block.  the
   * first data block is not offset, but all of the rest are 1 offset 
   * reletive to the previous block.  
   */
  public void setPercentOffset( float o )
  {
    percent_offset = o;
  }



  /**
   * gets data offset
   */
  public float getPercentOffset()
  {
    return percent_offset;
  }



  /**
   * give the object a referance to some text range UI's for updating
   * purposes.
   *
   */
  public void setXRangeUI( TextRangeUI x )
  {
    xrangeTRUI = x;
  }



  /**
   * give the object a referance to some text range UI's for updating
   * purposes.
   *
   */
  public void setYRangeUI( TextRangeUI y )
  {
    yrangeTRUI = y;
  }



  /**
   * calculates x and y ranges based on data.  this range is considered
   * default, and will be maintained until setXRange() or setYRange() are
   * called.
   */
  public void calculateRanges()
  {
    //if there's nothing in this container to manage
    if( size() == 0 ) 
    {
      xrange = new floatPoint2D( 0, 0 );
      yrange = new floatPoint2D( 0, 0 );
      return;
    }

    //otherwise...
    GraphableData data = null;
    Collection listGDV = values();  //get GraphableData objects
    Object it = listGDV.iterator();
    data = (GraphableData)((Iterator)it).next(); 

    //initialize xrange and yrange
    xrange.x = data.getXRange().getStart_x();
    xrange.y = data.getXRange().getEnd_x();
    yrange.x = data.getYRange().getStart_x();
    yrange.y = data.getYRange().getEnd_x();

    //look for ranges more extreme if there's more than one data block
    int count = 1;
    
    while(  ((Iterator)it).hasNext()  )
    {
      data = (GraphableData)((Iterator)it).next(); 
      //System.out.println(  data.toString()  );
      data.setOffset( new Integer(count).floatValue() * abs_offset );

      if( data.getXRange().getStart_x() > xrange.x )  
        xrange.x = data.getXRange().getStart_x();
      if( data.getXRange().getEnd_x() > xrange.y )  
        xrange.y = data.getXRange().getEnd_x();
      if( data.getYRange().getStart_x() > yrange.x )  
        yrange.x = data.getYRange().getStart_x();
      if( data.getYRange().getEnd_x() > yrange.y )  
        yrange.y = data.getYRange().getEnd_x();

      count++;
    }

    //find the additive constant for each graph
    abs_offset = size() * percent_offset * ( yrange.y - yrange.x );
  
    //find how tall the graph will be when additive constant is applied to data
    yrange.y = yrange.y + (  ( size() - 1 ) * abs_offset  );

    xrangeTRUI.setMax( xrange.y );
    xrangeTRUI.setMin( xrange.x );
    yrangeTRUI.setMax( yrange.y );
    yrangeTRUI.setMin( yrange.x );
  }




/*--------------------------=[ container methods ]=---------------------------*/


  /**
   * adds 'd' to this container (which extends Hashtable) using the String
   * returned by d.getID() as the key, and the object as the value.
   */
  public void add( GraphableData d )
  {
    if( (d instanceof GraphableData) )
    {
      put( d.getID(), d );
    }
    else
    {
      System.out.println( "GraphableDataManager::add( GraphableData d ): "
                          + "parameter is not GraphableData" );
      System.exit( -102 );
    }
  }



  /**
   * removeds key/value pair where the key is 'id' 
   */
  public GraphableData remove( String id )
  {
    return (GraphableData)super.remove( id );
  }



  /**
   * removes all records.  deselectes all Data objects.
   */
  public void clear()
  {
    int count = 0;
    GraphableData data = null;
    Collection listGDV = values();  //get GraphableData objects
    Object it = listGDV.iterator();
    while(  ((Iterator)it).hasNext()  )
    {
      data = (GraphableData)((Iterator)it).next();
      data.setSelected( false );
    }
    super.clear();
  }




/*------------------------------=[ graph methods ]=---------------------------*/


  /**
   * allows users access to the graph
   */
  public SelectedGraph getGraph()
  { 
    return graph;
  }



  /**
   * sets graph
   */
  public void setGraph( SelectedGraph g )
  {
    graph = g;
  }




  /**
   * sets the upper and lower bounds for the domain of x
   */
  public void setXRange( floatPoint2D range_ )
  {
    xrange = range_;
  }



  /** 
   * sets the upper and lower bounds for the range of y
   */
  public void setYRange( floatPoint2D range_ )
  {
    yrange = range_;
  }  



  /**
   * gets the upper and lower bounds of x
   */
  public floatPoint2D getXRange()
  { 
    return xrange;
  }



  /**
   * gets the upper and lower bounds of y
   */
  public floatPoint2D getYRange()
  {
    return xrange;
  }



}


  
  




