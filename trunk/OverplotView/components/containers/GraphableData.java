package OverplotView.components.containers;

/**
 * $Id$
 *
 * the GraphableData class has the responsibility of keeping state information
 * that is necessary for graphing objects of its type.
 *
 * $Log$
 * Revision 1.3  2000/07/06 20:11:19  neffk
 * added these files, just to make sure, since they had a ? in front of them
 * when updating.
 *
 * Revision 1.1  2000/06/22 14:05:26  neffk
 * Initial revision
 *
 * Revision 1.7  2000/06/01 20:10:27  neffk
 * moved to .../isaw/DataSetTools/components/conatiners/ as of 1.7
 *
 * Revision 1.6  2000/06/01 18:58:08  neffk
 * updated comments
 *
 * Revision 1.5  2000/05/02 08:18:03  psam
 * 1) offset is now transient.  no field stores its value.
 *
 * Revision 1.4  2000/05/01 05:28:05  psam
 * 1) modified main constructor to take an offset in addition to it all of the
 *    other values
 * 2) added a protected function to calculate an array of values (yval + offset)
 * 3) added public getOffsetData() to return the new values
 * 4) added getOffset()
 * 5) added setOffset()
 *
 * Revision 1.3  2000/04/30 20:21:15  psam
 * added a more appropriate constructor to initialize units and labels
 *
 * Revision 1.2  2000/04/24 18:36:11  psam
 * works 1.0
 *
 * Revision 1.1  2000/04/08 00:51:42  psam
 * Initial revision
 *
 *
 */

import java.awt.*;
import java.lang.*;
import java.util.*;

import DataSetTools.dataset.*;

import gov.noaa.noaaserver.sgt.datamodel.*;

import OverplotView.util.*;

public class GraphableData extends DataSet
{


  /**
   * main GraphableData constructor.  all other constructors must call
   * this constructor.
   * 
   * @param offset_ - percent of total range (value, not axis) to offset data.
   *                  should be between 0 and 1.
   */
  public GraphableData( String       t_,
                        Data         d_,
                        OperationLog log_,
                        String       x_units_,
                        String       x_label_,
                        String       y_units_,
                        String       y_label_,
                        String       id_ )
  {
    super( t_, log_, x_units_, x_label_, y_units_, y_label_ );
           
    id = id_; 
    setData( d_ );
   
    xdomainU = new UniformXScale( 0, 1, 2 );
    yrangeU  = new UniformXScale( 0, 1, 2 );

    offset = 0.0f;
    offsetData = null;

    marker = new sgtMarker( 0 );
    color = new sgtEntityColor( Color.black );
  }



  /**
   * constructs a GraphableData object from a DataSet.  since GraphableData
   * objects have only one Data object in them, only entry 0 will be used.
   *
   * note that 'id_' will be changing soon... this will probably be data 
   * contained in the DataSet class.
   */
  public GraphableData( DataSet ds, String id_ ) 
  {
    super(  ds.getTitle(),
            ds.getOp_log(),
            ds.getX_units(),
            ds.getX_label(),
            ds.getY_units(),
            ds.getY_label()  );

    setData(  ds.getData_entry( 0 )  );
    id = id_; 

    xdomainU = new UniformXScale( 0, 1, 2 );
    yrangeU  = new UniformXScale( 0, 1, 2 );

    offset = 0.0f;
    offsetData = null;
  }
           


  /**
   * allows access to selected attribute of this piece of data.  this 
   * selection property is used to determin which items will be graph and
   * which will not be graphed.
   */
  public boolean isSelected()
  {
    return getData_entry( 0 ).isSelected();
  }



  /**
   * allows user to change selected property
   */
  public void setSelected( boolean f )
  {
    getData_entry( 0 ).setSelected( f );
  }
  /**
   * allows the user to set the offset for this data
   */
  public void setOffset( float offset_ )
  {
    offset = offset_;
    calculateOffset();  //initializes 'float [] offsetData'
  }



  /**
   * allows access to the offset for this data
   */
  public float getOffset()
  {
    return offset;
  }



  public String getID()
  {
    return id;
  }

  
  public LineType getLineType()
  {
    return lt;
  }


  public Marker getMarkerType()
  {
    return marker;
  }


  public EntityColor getColor()
  {
    return color;
  }


  public void setID( String _id )
  { 
    id = _id;  
  }


  public void setLineType( LineType _lt )
  {
    lt = _lt;
  }


  public void setMarkerType( Marker _m )
  {
    marker = _m;
  }


  public void setColor( EntityColor _c )
  {
    System.out.println( "EntityColor::setColor()" );
    color = _c;
  }



  /**
   * allows the user access to the data that this object contains.
   * because the object also contains offset information, to ensure
   * one gets "real" or correct data, call setOffset(), and change the
   * offset to 0.
   */
  public Data getData()
  {
    return new Data(  getData_entry(0).getX_scale(), 
                      offsetData, 
                      getData_entry(0).getGroup_ID()  );
  }



  
  public void setData( Data d )
  {
    if( this.getNum_entries() > 0 )
      this.removeData_entry( 0 );

    this.addData_entry( d );
  }



  public void setDomainU( UniformXScale _xD )
  {
    xdomainU = _xD;
  }



  public UniformXScale getDomainU()
  {
    return xdomainU;
  }       



  public void setRangeU( UniformXScale _yR )
  {
    yrangeU = _yR;
  }



  public UniformXScale getRangeU()
  {
    return yrangeU;
  }



  

  /**
   * adds the value of the offset onto the y values of the data and
   * returns the array of new values.
   */
  protected void calculateOffset()
  {
    float[] data = this.getData_entry(0).getY_values();
    offsetData   = new float[ data.length ];
    for( int i=0; i<data.length; i++ )
      offsetData[i] = data[i] + offset;
  }



  /**
   * constructs a SimpleLine from this object's data
   *
   */

  public SimpleLine getSGTData()
  {
    NumberTypeConverter converter = new NumberTypeConverter();

    SimpleLine sl;
    SGTMetaData xMeta;
    SGTMetaData yMeta;
    double[] axis, values;

    axis = converter.toDouble( getData().getX_scale().getXs()  );
    values = converter.toDouble( getData().getY_values()  );

    xMeta = new SGTMetaData(  getX_label(),
                              getX_units(),
                              false,
                              false  );
    yMeta = new SGTMetaData(  getY_label(),
                              getY_units(),
                              true,
                              false  );
    sl = new SimpleLine(axis, values, "Test Series");
    sl.setXMetaData( xMeta );
    sl.setYMetaData( yMeta );

    Integer group = new Integer(  getData().getGroup_ID()  );
    AttributeList alist = getData().getAttributeList();
    String id_str = "Group #" + group.toString();
    sl.setTitle( id_str );

    return sl;
  }



/*---------------------------------static------------------------------------*/


/*
//  private static LineTypeDesignator ltDesignator = null;
//  private static MarkerDesignator mDesignator = null;
//  private static ColorDesignator colorDesignator = null;

  private static LineTypeDesignator ltDesignator = 
    new LineTypeDesignator();
  private static MarkerDesignator mDesignator = 
    new MarkerDesignator();
  private static ColorDesignator colorDesignator = 
    new ColorDesignator();

  public static void addLT( LineType _lt )
  {
    ltDesignator.add( _lt );
  }


  public static void addMarker( Marker _m )
  {
    mDesignator.add( _m );
  }


  public static void addColor( EntityColor _c )
  {
    colorDesignator.add( _c );
  }
*/


/*-----------------------------------data-------------------------------------*/

  private  LineType      lt;
  private  Marker        marker;
  private  EntityColor   color;
  private  UniformXScale xdomainU;
  private  UniformXScale yrangeU;

  private  String        id;
  private  float[]       offsetData;
  private  float         offset;
}


