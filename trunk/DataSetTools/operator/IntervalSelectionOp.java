
/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2001/07/25 17:36:46  neffk
 * commented method setParameter() out.
 *
 * Revision 1.2  2001/07/11 18:37:12  neffk
 * changed getResult() to deal w/ potential multiple interval
 * parameters.  getResult() now returns type int[] of GROUP_ID's instead
 * of a Vector object of the same.  also, removed some magic strings and
 * the setParameter method (previously added for debugging purposes).
 *
 * Revision 1.1  2001/07/09 21:55:57  neffk
 * selects based on a open or closed interval.
 *
 */

package DataSetTools.operator;

import DataSetTools.util.Interval;
import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.StringAttribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.util.IObserver;
import java.io.Serializable;
import java.lang.String;
import java.lang.Object;
import java.util.Vector;

/**
 * selects Data objects in a single DataSet object based on an attribute
 * name and value.
 */
public class IntervalSelectionOp
  extends    DS_Attribute
  implements Serializable
{

  public static final String TITLE = "Select by Interval";
  public static final String INT   = "Interval";


  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */
  public IntervalSelectionOp()
  {
    super( TITLE );
  }


  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   */
  public IntervalSelectionOp( DataSet ds )
  {
    this();
    setDataSet( ds );
  }


  public String getCommand()
  {
    return new String( TITLE );
  }


  public void setDefaultParameters()
  {
    parameters = new Vector();  //clear old parameters

    Parameter parameter = new Parameter( "DataSet to Select on",
                                         DataSet.EMPTY_DATA_SET );
    addParameter( parameter );

    String def_value = new String( Attribute.GROUP_ID + "[1:2]" );
    parameter = new Parameter( "Interval", def_value );
    addParameter( parameter );
  }


  /**
   * creates Interval objects from the string parameter and returns
   * a Vector object of results, where each element of the Vector is
   * the group id of selected Data objects.
   */
  public Object getResult()
  {

                                 //an Object object compatible container
                                 //that returns a list of the GROUP_ID's of
                                 //the Data objects that fell within this
                                 //interval
    int[] index_list = new int[ getDataSet().getNum_entries() ];

    Interval interval = null;
    int selected_so_far_count = 0;

    System.out.println( "num: " + getNum_parameters() );
    System.out.println( "size: " + parameters.size() );

    for(  int i=0;  i<getNum_parameters();  i++ )
    {
      Parameter p = (Parameter)parameters.get(1);

 
                                 //only two (2) different types of
                                 //parameters are delt with in this op.
                                 //String parameters are parsed into Interval
                                 //objects, DataSets are acted upon.
      if(  p.getValue() instanceof DataSet  )
        setDataSet(  (DataSet)p.getValue()  );

      else if(  p.getValue() instanceof String  )
      {
        String s = (String)p.getValue();
        interval = new Interval( s );

        System.out.println( "s: " + s );

        selected_so_far_count = 0;
        for(  int j=0;  j<getDataSet().getNum_entries();  j++ )
        { 
          Data d = getDataSet().getData_entry(j);
          Attribute a = d.getAttribute( interval.getType() );

          if(  interval.within( a )  )
          {
            getDataSet().getData_entry(i).setSelected( true );
            index_list[ selected_so_far_count++ ] = d.getGroup_ID();
          }
          else
            d.setSelected( false );
 
          getDataSet().notifyIObservers( IObserver.SELECTION_CHANGED );
        }
      }
    }
    
    int[] return_list = new int[ selected_so_far_count ];
    for(  int i=0;  i<selected_so_far_count;  i++ )
      return_list[i] = index_list[i];

    return return_list;
  }


  public Object clone()
  {
    IntervalSelectionOp new_op = new IntervalSelectionOp();

    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );
  
    return new_op;
  }


/*
  public boolean setParameter( Parameter p, int i )
  {
    if(  p.getValue() instanceof String )
      System.out.println(  (String)(p.getValue())  );
    else if(  p.getValue() instanceof DataSet  )
      System.out.println(  p.getValue() );

    return super.setParameter( p, i );
  }
*/

}
