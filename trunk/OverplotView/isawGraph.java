package  OverplotView;

/**
 * $Id$
 *
 * $Log$
 * Revision 1.3  2000/08/04 20:32:25  neffk
 * routine check-in.
 *
 * Revision 1.2  2000/07/28 14:55:48  neffk
 * reworked the zooming so that both vertical and horizontal aspects are
 * changed with respect to the selection window.
 *
 * Revision 1.1  2000/07/27 16:49:16  neffk
 * this is release USNS-ABLE
 *
 * Revision 1.1  2000/07/13 21:09:11  neffk
 * Initial revision
 * 
 */

import java.util.Vector;
import java.util.Enumeration;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import gov.noaa.noaaserver.sgt.*;
import gov.noaa.noaaserver.sgt.datamodel.*;
import gov.noaa.noaaserver.sgt.util.*;
import gov.noaa.noaaserver.sgt.awt.*;
//import gov.noaa.noaaserver.sgt.Point2D.Double;

import DataSetTools.components.ui.*;

import OverplotView.components.containers.*;

public class isawGraph 
  extends GraphicLayout 
{
  //
  // save handles to unique components
  //
  Logo logo_;
  LineKey lineKey_ = null;
  int layerCount_;

  double xSize_ =  5.00;  //XSIZE_
  double ySize_ =  5.00;  //YSIZE_

  double xMin_  =  0.5;  //XMIN_
  double xMax_  =  4.5;  //XMAX_
  double yMin_  =  0.5;  //YMIN_
  double yMax_  =  4.5;  //YMAX_

  double xOriginU = 0.0;
  double yOriginU = 0.0;

  double mainTitleHeight_ = 0.30;  //MAIN_TITLE_HEIGHT_
  double titleHeight_     = 0.22;  //TITLE_HEIGHT_
  double labelHeight_     = 0.18;  //LABEL_HEIGHT_
  double warnHeight_      = 0.15;  //WARN_HEIGHT_
  double keyHeight_       = 0.16;  //KEY_HEIGHT_
  
  double xKeySize_ =  6.00;  //XKEYSIZE_
  double yKeySize_ = 12.00;  //YKEYSIZE_
  
  Color paneColor_ = Color.white;
  Color keyPaneColor_ = Color.white;

  private boolean trace = false;


/*----------------------------=[ constructor ]=-------------------------------*/

  /**
   * isawGraph constructor.
   *
   * @param 
   */
  public isawGraph(  Point2D.Double x, 
                     Point2D.Double y, 
                     double xSize,
                     double ySize,
                     double xOrigU,
                     double yOrigU  )
  {
    super(  "test graph", null, new Dimension(400,300)  );

    //set physical sizes before init() is called
    init(  new Point2D.Double( xMin_ = x.x, xMax_ = x.y ),
           new Point2D.Double( yMin_ = y.x, yMax_ = y.y ),
           xSize_ = xSize,
           ySize_ = ySize,
           xOrigU,
           yOrigU  );
    
    setTitles( "", "", "" );
  }




/*------------------------=[ initilization & redraw ]=------------------------*/

  public void init(  Point2D.Double x,
                     Point2D.Double y,
                     double xSize,
                     double ySize,
                     double xOrigU,
                     double yOrigU  )
  {
    if( trace )
      System.out.println( "isawGraph::init()" );

    xMin_ = x.x;
    xMax_ = x.y;
    yMin_ = y.x;
    yMax_ = y.y;
    xSize_ = xSize;
    ySize_ = ySize;

    xOriginU = xOrigU;
    yOriginU = yOrigU;

    Layer layer;
    CartesianGraph graph = null;
    LinearTransform xt, yt;
    PlainAxis xbot, yleft;

    //remove old data, graph, and such
    data_.removeAllElements();
    removeAll();


    //create Pane and descendants for the isawGraph layout
    setLayout(new StackedLayout());
    setBackground(paneColor_);
    layer = new Layer(  "Layer 1", new Dimension2D( xSize_, ySize_ )  );
    add( layer );

    layerCount_ = 0;


    //create LineCartesianGraph and transforms
    graph = new CartesianGraph("Profile Graph 1");
    xt = new LinearTransform(xMin_, xMax_, 0.0, 1.0);
    yt = new LinearTransform(yMin_, yMax_, 0.0, 1.0);
    graph.setXTransform(xt);
    graph.setYTransform(yt);


    //create axes
    Font axfont = new Font( "Helvetica", Font.ITALIC, 14 );
    xbot = new PlainAxis( "Bottom Axis" );
    xbot.setRangeU(  new Range2D(0.0, 1.0)  );
    xbot.setDeltaU( 0.2 );
    xbot.setNumberSmallTics( 0 );
    xbot.setLabelHeightP( labelHeight_ );
    xbot.setLocationU(  new Point2D.Double( xOriginU, yOriginU )  );
    xbot.setLabelFont( axfont );
    graph.addXAxis( xbot );

    yleft = new PlainAxis( "Left Axis" );
    yleft.setRangeU(  new Range2D( 0.0, 1.0 )  );
    yleft.setDeltaU( 0.2 );
    yleft.setNumberSmallTics( 0 );
    yleft.setLabelHeightP( labelHeight_ );
    yleft.setLocationU(  new Point2D.Double( xOriginU, yOriginU )  );
    yleft.setLabelFont( axfont );
    graph.addYAxis( yleft );

    //create legend
    lineKey_ = new LineKey();
    lineKey_.setId("Line Key");
    lineKey_.setVAlign(LineKey.TOP);
    lineKey_.setHAlign(LineKey.RIGHT);
    lineKey_.setLocationP(new Point2D.Double(xSize_ - 0.01, ySize_));
    layer.addChild(lineKey_);

    layer.setGraph( graph );
  }



  public String getLocationSummary(SGTData grid) 
  {
    return "";
  }



  public void addData( Collection lines ) 
  {
    addData(lines, null);
  }



  public void addData( Collection lines, String descrip ) 
  {
    if( trace )
      System.out.println("addData(Collection) called");

    for(int i=0; i < lines.size(); i++) {
      SGTLine line = (SGTLine)lines.elementAt(i);
      addData(line, line.getTitle());
    }
  }



  /**
   * Add data
   *
   */
  public void addData( SGTData datum ) 
  {
    addData(datum, null);
  }



  /**
   * required 
   *
   */
  public void addData( SGTData datum, String descrip )
  {
  }



  /** 
   * add GraphableData to the graph, using that attributes of the data 
   * instead of the values contained by isaGraphw
   *
   */
  public void addData( GraphableData graphableData )
  {
    Layer layer, newLayer;
    CartesianGraph graph, newGraph;
    PlainAxis xbot, yleft;
    LinearTransform xt, yt;
    SGLabel xtitle, ytitle, lineTitle;
    SGTData data;
    LineAttribute lineAttr;
    String xLabel, yLabel;
    Range2D xRange, yRange;
    Range2D xnRange = null, ynRange = null;
    Point2D.Double origin = null;
    boolean data_good = true;
    double save;

    String descrip = new String(  graphableData.getID()  );
    SGTData datum = graphableData.getSGTData();

    if(data_.size() == 0)
      setBaseUnit(Units.getBaseUnit(((SGTLine)datum).getXMetaData()));

    datum = Units.convertToBaseUnit(  datum,
                                      getBaseUnit(),
                                      Units.X_AXIS  );



    if(data_.size() == 0)
    {
      super.addData(datum);


      // only one data set...
      // determine range and titles from data

      data = (SGTData)data_.firstElement();
      xRange = findRange( (SGTLine)data, X_AXIS );
      yRange = findRange( (SGTLine)data, Y_AXIS );

      if(  Double.isNaN(xRange.start) || Double.isNaN(yRange.start)  )
        data_good = false;

      if( data_good )
      {
        //compute a nice range from this data's range (in user coord plane)
        xnRange = Graph.computeRange( xRange, 6 );
        ynRange = Graph.computeRange( yRange, 6 );

        origin = new Point2D.Double( xnRange.start, ynRange.start );
      }


      xLabel = ( (SGTLine)data ).getXMetaData().getUnits();
      yLabel = ( (SGTLine)data ).getYMetaData().getUnits();


      //attach information to pane and descendents
      try
      {
        layer = getLayer("Layer 1");
      }
      catch (LayerNotFoundException e)
      {
        return;
      }
      graph = (CartesianGraph)layer.getGraph();


      //axes
      try
      {
        Font tfont = new Font( "Helvetica", Font.PLAIN, 14 );
        xbot = (PlainAxis)graph.getXAxis( "Bottom Axis" );
        if( data_good )
        {
          xbot.setRangeU( xnRange );
          xbot.setDeltaU( xnRange.delta );
          xbot.setLocationU( origin );
        }
        xtitle = new SGLabel( "xaxis title",
                               xLabel,
                               new Point2D.Double(0.0, 0.0)  );
        xtitle.setFont(tfont);
        xtitle.setHeightP(titleHeight_);
        xbot.setTitle(xtitle);

        yleft = (PlainAxis)graph.getYAxis("Left Axis");
        if(data_good)
        {
          yleft.setRangeU(ynRange);
          yleft.setDeltaU(ynRange.delta);
          yleft.setLocationU(origin);
        }
        ytitle = new SGLabel(  "yaxis title",
                               yLabel,
                               new Point2D.Double(0.0, 0.0)  );
        ytitle.setFont(tfont);
        ytitle.setHeightP(titleHeight_);
        yleft.setTitle(ytitle);
      }
      catch (AxisNotFoundException e)
      {
      }


      //transforms
      if(data_good)
      {
        xt = (LinearTransform)graph.getXTransform();
        xt.setRangeU(xnRange);

        yt = (LinearTransform)graph.getYTransform();
        yt.setRangeU(ynRange);
      }

      if(  ( (sgtMarker)(graphableData.getMarkerType()) ).getSGTMarker() == 0  )
      {
        //set color and marker type
        lineAttr = new LineAttribute(
          LineAttribute.SOLID,
          (  (sgtMarker)(graphableData.getMarkerType())  ).getSGTMarker(),
          (  (sgtEntityColor)(graphableData.getColor())  ).getSGTColor()  );
      }
      else
      {
        //set color and marker type
        lineAttr = new LineAttribute(
          LineAttribute.MARK,
          (  (sgtMarker)(graphableData.getMarkerType())  ).getSGTMarker(),
          (  (sgtEntityColor)(graphableData.getColor())  ).getSGTColor()  );
      }
      graph.setData(data, lineAttr);


      //add to lineKey
      lineTitle = new SGLabel(  "line title",
                                descrip,
                                new Point2D.Double(0.0, 0.0)  );
      lineTitle.setHeightP(keyHeight_);
      lineKey_.addLineGraph(  (LineCartesianRenderer)graph.getRenderer(),
                              lineTitle  );
    }
    else
    {

      // more than one data set...
      // add new layer
/*
      if(((SGTLine)datum).getYMetaData().isReversed() != zUp_)
      {
        //System.out.println("New datum has reversed ZUp!");
        SGTData modified = flipZ(datum);
        datum = modified;
      }
*/
      super.addData(datum);

      data_good = false;
      layerCount_++;

      //loop over data sets, getting ranges
      if(  isOverlayed()  )
      {
        Range2D xTotalRange = new Range2D();
        Range2D yTotalRange = new Range2D();

        boolean first = true;

        for (Enumeration e = data_.elements() ; e.hasMoreElements() ;)
        {
          data = (SGTData)e.nextElement();
          xRange = findRange((SGTLine)data, X_AXIS);
          yRange = findRange((SGTLine)data, Y_AXIS);
          if(!((SGTLine)data).getYMetaData().isReversed())
          {
            save = yRange.start;
            yRange.start = yRange.end;
            yRange.end = save;
          }
          if(first)
          {
            if(Double.isNaN(xRange.start) || Double.isNaN(yRange.start))
            {
              first = true;
            }
            else
            {
              first = false;
              data_good = true;
              xTotalRange = new Range2D(xRange.start, xRange.end);
              yTotalRange = new Range2D(yRange.start, yRange.end);
            }
          }
          else
          {
            if(!Double.isNaN(xRange.start) && !Double.isNaN(yRange.start))
            {
              data_good = true;
              xTotalRange.start = Math.min(xTotalRange.start, xRange.start);
              xTotalRange.end = Math.max(xTotalRange.end, xRange.end);
              if(!((SGTLine)data).getYMetaData().isReversed())
              {
                yTotalRange.start = Math.max(yTotalRange.start, yRange.start);
                yTotalRange.end = Math.min(yTotalRange.end, yRange.end);
              }
              else
              {
                yTotalRange.start = Math.min(yTotalRange.start, yRange.start);
                yTotalRange.end = Math.max(yTotalRange.end, yRange.end);
              }
            }
          }
        }
        try {
          layer = getLayer("Layer 1");
        } catch (LayerNotFoundException e) {
          return;
        }
        graph = (CartesianGraph)layer.getGraph();

        if(data_good)
        {
          xnRange = Graph.computeRange(xTotalRange, 6);
          ynRange = Graph.computeRange(yTotalRange, 6);
          origin = new Point2D.Double(xnRange.start, ynRange.start);

          // axes
          try
          {
            xbot = (PlainAxis)graph.getXAxis("Bottom Axis");
            xbot.setRangeU(xnRange);
            xbot.setDeltaU(xnRange.delta);
            xbot.setLocationU(origin);

            yleft = (PlainAxis)graph.getYAxis("Left Axis");
            yleft.setRangeU(ynRange);
            yleft.setDeltaU(ynRange.delta);
            yleft.setLocationU(origin);
          }
          catch (AxisNotFoundException e)
          {
          }
        }

        // transforms
        xt = (LinearTransform)graph.getXTransform();
        yt = (LinearTransform)graph.getYTransform();

        if( data_good )
        {
          xt.setRangeU(xnRange);
          yt.setRangeU(ynRange);
        }

        // create new layer and graph
        newLayer = new Layer(  "Layer " + (layerCount_+1),
                               new Dimension2D(xSize_, ySize_)  );
        newGraph = new CartesianGraph( "Graph " + (layerCount_+1), xt, yt);
        add( newLayer );
        newLayer.setGraph( newGraph );
        newLayer.invalidate();
        validate();



        int style = ((sgtMarker)(graphableData.getMarkerType())).getSGTMarker();
        if( style == LineAttribute.SOLID  )
        {
          //set color and marker type
          lineAttr = new LineAttribute(
            LineAttribute.SOLID,
            (  (sgtMarker)(graphableData.getMarkerType())  ).getSGTMarker(),
            (  (sgtEntityColor)(graphableData.getColor())  ).getSGTColor()  );
        }
        else
        {
          //set color and marker type
          lineAttr = new LineAttribute(
            LineAttribute.MARK,
            (  (sgtMarker)(graphableData.getMarkerType())  ).getSGTMarker(),
            (  (sgtEntityColor)(graphableData.getColor())  ).getSGTColor()  );
        }
        newGraph.setData(datum, lineAttr);


        //add to lineKey
        if( descrip == null )
        {
          xLabel = ((SGTLine)datum).getXMetaData().getName();
          lineTitle = new SGLabel(  "line title",
                                    xLabel,
                                    new Point2D.Double(0.0, 0.0)  );
        } else {
          lineTitle = new SGLabel(  "line title",
                                    descrip,
                                    new Point2D.Double(0.0, 0.0)  );
        }
        lineTitle.setHeightP(keyHeight_);
        lineKey_.addLineGraph(
            (LineCartesianRenderer)newGraph.getRenderer(), lineTitle  );
      }
    }
  }



  /**
   * Clear the current zoom.
   */
  public void resetZoom()
  {
    SGTData data;
    Range2D xRange, yRange;
    boolean data_good = false;
    double save;

    // loop over data sets, getting ranges
    Range2D xTotalRange = new Range2D();
    Range2D yTotalRange = new Range2D();

    boolean first = true;

    for (Enumeration e = data_.elements();  e.hasMoreElements()  ;) 
    {
      data = (SGTData)e.nextElement();
      xRange = findRange((SGTLine)data, X_AXIS);
      yRange = findRange((SGTLine)data, Y_AXIS);
/*
      if(  !((SGTLine)data).getYMetaData().isReversed()  ) 
      {
        save = yRange.start;
        yRange.start = yRange.end;
        yRange.end = save;
      }
*/
      if( first ) 
      {
        if(  Double.isNaN(xRange.start) || Double.isNaN(yRange.start)  ) 
        {
          first = true;
        } 
        else 
        {
          first = false;
          data_good = true;
          xTotalRange = new Range2D(xRange.start, xRange.end);
          yTotalRange = new Range2D(yRange.start, yRange.end);
        }
      } 
      else 
      {
        if(  !Double.isNaN(xRange.start) && !Double.isNaN(yRange.start)  ) 
        {
          data_good = true;
          xTotalRange.start = Math.min(xTotalRange.start, xRange.start);
          xTotalRange.end = Math.max(xTotalRange.end, xRange.end);
          if(  !((SGTLine)data).getYMetaData().isReversed()  ) 
          {
            yTotalRange.start = Math.max(yTotalRange.start, yRange.start);
            yTotalRange.end = Math.min(yTotalRange.end, yRange.end);
          } 
          else 
          {
            yTotalRange.start = Math.min(yTotalRange.start, yRange.start);
            yTotalRange.end = Math.max(yTotalRange.end, yRange.end);
          }
        }
      }
    }
    if( data_good ) 
    {
      setXRange( xTotalRange );
      setYRange( yTotalRange );
    }
  }



  /**
   * required 
   *
   */
  public void setXRange( TimeRange trnge ) 
  {
  }



  /**
   * Reset the x range. This method is designed to provide
   * zooming functionality.
   *
   * @param rnge new x range
   */
  public void setXRange( Range2D rnge ) 
  {
    //System.out.println(  "zoom: horiz: " + rnge.start + " to " + rnge.end  );

    Point2D.Double origin;
    PlainAxis xbot, yleft;
    Range2D xr, yr, xnRange;
    Layer layer = getFirstLayer();
    CartesianGraph graph = (CartesianGraph)layer.getGraph();
    LinearTransform xt = (LinearTransform)graph.getXTransform();

    xnRange = Graph.computeRange(rnge, 6);
    xt.setRangeU(xnRange);

    try 
    {
      xbot = (PlainAxis)graph.getXAxis( "Bottom Axis" );
      yleft = (PlainAxis)graph.getYAxis( "Left Axis" );

      xbot.setRangeU( xnRange );
      xbot.setDeltaU( xnRange.delta );

      xr = xbot.getRangeU();
      yr = yleft.getRangeU();
      origin = new Point2D.Double( xr.start, yr.start );
      xbot.setLocationU( origin );

      yleft.setLocationU( origin );

      //set clipping
      if( clipping_ ) 
      {
        setAllClip(xr.start, xr.end, yr.start, yr.end);
      } 
      else 
      {
        setAllClipping( false );
      }
    } 
    catch( AxisNotFoundException e ) 
    {
    }
  }



  /**
   * Reset the y range. This method is designed to provide
   * zooming functionality.
   * 
   * @param rnge new y range
   */
/*
  public void setYRange(Range2D rnge) 
  {
  }
*/



  /**
   * Reset the y range. This method is designed to provide
   * zooming functionality.
   *
   * @param rnge new y range
   * @param testZUp test to see if Z is Up
   */
//  public void setYRange(Range2D rnge, boolean testZUp) 
  public void setYRange( Range2D rnge ) 
  {
    //System.out.println(  "zoom: vert: " + rnge.start + " to " + rnge.end  );

    SGTData grid;
    Point2D.Double origin;
    PlainAxis xbot, yleft;
    Range2D xr, yr, ynRange;
    double save;
    Layer layer = getFirstLayer();
    CartesianGraph graph = (CartesianGraph)layer.getGraph();
    LinearTransform yt = (LinearTransform)graph.getYTransform();

/*
    if(testZUp) {
      grid = (SGTData)data_.elements().nextElement();
      if(!((SGTLine)grid).getYMetaData().isReversed()) {
        save = rnge.end;
        rnge.end = rnge.start;
        rnge.start = save;
      }
    }
*/

    ynRange = Graph.computeRange(rnge, 6);
    yt.setRangeU(ynRange);

    try 
    {
      xbot = (PlainAxis)graph.getXAxis("Bottom Axis");
      yleft = (PlainAxis)graph.getYAxis("Left Axis");

      yleft.setRangeU( ynRange );
      yleft.setDeltaU( ynRange.delta );

      xr = xbot.getRangeU();
      yr = yleft.getRangeU();
      origin = new Point2D.Double(xr.start, yr.start);
      yleft.setLocationU(origin);

      xbot.setLocationU(origin);

      //set clipping
      if( clipping_ ) 
      {
        setAllClip(xr.start, xr.end, yr.start, yr.end);
      }
      else 
      {
        setAllClipping(false);
      }
    } 
    catch (AxisNotFoundException e) 
    {
    }
  }



  /**
   * required
   */
  public void setYRange(TimeRange trnge) 
  {
  }



  /**
   *
   */
  private void setAllClip( Pane pane, 
                           double xmin, 
                           double xmax, 
                           double ymin, 
                           double ymax) 
  {
    Layer ly;
    Component[] comps = pane.getComponents();
    for(int i=0; i < comps.length; i++)
    {
      if(comps[i] instanceof Layer)
      {
        ly = (Layer)comps[i];
        ((CartesianGraph)ly.getGraph()).setClip(xmin, xmax, ymin, ymax);
      }
    }
  }



  private void setAllClipping(Pane pane, boolean clip) 
  {
    Layer ly;
    Component[] comps = pane.getComponents();
    for(int i=0; i < comps.length; i++) {
      if(comps[i] instanceof Layer) {
        ly = (Layer)comps[i];
        ((CartesianGraph)ly.getGraph()).setClipping(clip);
      }
    }
  }



  public void clear() 
  {
    lineKey_.clearAll();
    data_.removeAllElements();
    removeAll();
    draw();
  }



  public void clearDataOnly() 
  {
    lineKey_.clearAll();
    data_.removeAllElements();
    Layer layer = getFirstLayer();
    if(  ((CartesianGraph)layer.getGraph()).getRenderer() != null  )
      ((CartesianGraph)layer.getGraph()).setRenderer(null);
    removeAll();
    add(layer);   // restore first layer
    draw();
  }


  public void setKeyBoundsP(Rectangle2D.Double bounds)
  {
  }



  public Rectangle2D.Double getKeyBoundsP() 
  {
    return new Rectangle2D.Double( 0.0, 0.0 );
  }



  public void setLayerSizeP(Dimension2D d) 
  {
    double xMax = d.width - (xSize_ - xMax_);
    double yMax = d.height - (ySize_ - yMax_);
    Component[] comps = getComponents();
    Layer layer = getFirstLayer();
    CartesianGraph graph = (CartesianGraph)layer.getGraph();
    LinearTransform yt = (LinearTransform)graph.getYTransform();
    LinearTransform xt = (LinearTransform)graph.getXTransform();
    for(int i=0; i < comps.length; i++) {
      if(comps[i] instanceof Layer) {
        ((Layer)comps[i]).setSizeP(d);
      }
    }
    yt.setRangeP(new Range2D(yMin_, yMax));
    xt.setRangeP(new Range2D(xMin_, xMax));
    //
    double xpos;
    if(iconImage_ != null) {
      Rectangle bnds = logo_.getBounds();
      xpos = layer.getXDtoP(bnds.x + bnds.width) + 0.05;
    } else {
      xpos = (xMin_ + xMax_)*0.5;
    }
    double ypos = d.height - 1.2f*mainTitleHeight_;
    mainTitle_.setLocationP(new Point2D.Double(xpos, ypos));
    ypos = ypos - 1.2f*warnHeight_;
    title2_.setLocationP(new Point2D.Double(xpos, ypos));
    ypos = ypos - 1.1f*warnHeight_;
    title3_.setLocationP(new Point2D.Double(xpos, ypos));
    lineKey_.setLocationP(new Point2D.Double(d.width - 0.01, d.height));
  }

}
