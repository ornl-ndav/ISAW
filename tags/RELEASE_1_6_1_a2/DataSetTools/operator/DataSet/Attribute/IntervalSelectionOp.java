/*
 * File: IntervalSelectionOp.java
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.4  2003/12/16 00:03:16  bouzekc
 * Removed unused imports.
 *
 * Revision 1.3  2003/07/07 15:50:26  bouzekc
 * Added getDocumentation(), fixed code comment spelling
 * errors.
 *
 * Revision 1.2  2002/11/27 23:16:41  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/02/22 21:00:08  pfpeterson
 * Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Attribute;

import java.io.Serializable;
import java.util.Vector;

import DataSetTools.dataset.Attribute;
import DataSetTools.dataset.Data;
import DataSetTools.dataset.DataSet;
import DataSetTools.operator.Parameter;
import DataSetTools.util.IObserver;
import DataSetTools.util.Interval;

/**
 * Selects Data objects in a single DataSet object based on an attribute
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
   *  @return javadoc-style documentation for this Operator.
   */
  public String getDocumentation(  ){
    StringBuffer s = new StringBuffer(  );
    s.append( "@overview Selects Data objects in a single DataSet Object " );
    s.append( "based on a given interval." );
    s.append( "@assumptions The DataSet is non-empty." );
    s.append( "@algorithm Using the given interval, selects Data Objects " );
    s.append( "from the DataSet.\n" );
    s.append( "@param ds DataSet to perform the selection on.\n" );
    s.append( "@param interval The interval to use for the selection.\n" );
    s.append( "@return A Vector containing the group IDs of the selected " );
    s.append( "Data Objects.\n" );

    return s.toString(  );
  }

  /**
   * Creates Interval Objects from the string parameter and returns
   * a Vector of results, where each element of the Vector is
   * the group ID of selected Data objects.
   */
  public Object getResult()
  {

                                 //an Object compatible container
                                 //that returns a list of the GROUP_ID's of
                                 //the Data objects that fell within this
                                 //interval
    int[] index_list = new int[ getDataSet().getNum_entries() ];

    Interval[] intervals = null;
    int selected_so_far_count = 0;

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

        String[] blocks = parse_blocks( s );
        intervals = parse_intervals( blocks );

//        System.out.println(  "I: " + interval.toString()  );

        selected_so_far_count = 0;
        for(  int j=0;  j<getDataSet().getNum_entries();  j++ )
        { 
          Data d = getDataSet().getData_entry(j);
          Attribute a = d.getAttribute(  intervals[j].getType()  );

          for(  int k=0;  k<intervals.length;  k++ )
          { 
            if(  intervals[k].within( a )  )
            {
              d.setSelected( true );
              index_list[ selected_so_far_count++ ] = d.getGroup_ID();
            }
            else
              d.setSelected( false );
          }
        }
      }
      getDataSet().notifyIObservers( IObserver.SELECTION_CHANGED );
    }

                                  //pack into a more appropriately 
                                  //sized array
    int[] return_list = new int[ selected_so_far_count ];
    for(  int i=0;  i<selected_so_far_count;  i++ )
      return_list[i] = index_list[i];

    return return_list;
  }


  /**
   * parses comma separated intervals and values out of the parameter
   */ 
  public String[] parse_blocks( String str )
  {

                                 //intervals and individual numbers
                                 //must be separated by commas, so 
                                 //break up 'str' at commas    
    int comma_count = 0;
    for( int i=0;  i<str.length();  i++ )
      if(  str.charAt(i) == ','  )
        comma_count++;

    String[] blocks = new String[ comma_count + 1 ];

    if( comma_count == 0 )
    {
      int start = 0;
      int end   = str.length();
      blocks[0] = str.substring( start, end );
      return blocks;
    }
    
    int start = 0;
    int end   = str.indexOf( "," );
    blocks[0] = str.substring( start, end );
    str = str.substring(  end + 1, str.length()  );


    int count = 1;
    while(  count < comma_count  )
    {
      start = 0;
      end   = str.indexOf( "," );

      blocks[count] = str.substring( start, end ).trim();
      str = str.substring(  end + 1, str.length()   );

      count++;
    }

    blocks[ comma_count ] = str.trim();

    return blocks;
  }


  /**
   *
   */ 
  public Interval[] parse_intervals( String[] blocks )
  {
    String type = "";

    Interval[] intervals = new Interval[ blocks.length ];
    for( int i=0;  i<blocks.length;  i++ )
    {
      String block = blocks[i];
      int separator = block.indexOf( Interval.SEPARATOR );

                     //if there's a SEPARATOR, then it must
                     //be an interval
      if( separator > 0 )
      {
        intervals[i] = new Interval( type + block );
      }

                 //if there's no SEPARATOR, then we'll assume
                 //this is an individual number. 
      else 
      {
        Double value = new Double( block );
        if(  value.isNaN()  ) 
          System.out.println( "bad number: " + block );
//          throw IllegalArgumentException( "illegal: " + block );
          
        String str = new String(  "[" + 
                                  value.toString() + 
                                  Interval.SEPARATOR +
                                  value.toString() + 
                                  "]"  );
        intervals[i] = new Interval( str );
      }
    }
 
    return intervals;
  }


  /**
   * make a deep copy of this object.
   */ 
  public Object clone()
  {
    IntervalSelectionOp new_op = new IntervalSelectionOp();

    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );
  
    return new_op;
  }


  public static void main( String args[] )
  {
    IntervalSelectionOp isop = new IntervalSelectionOp();

    System.out.println( "----- blocks -----" );
    String[] strs = isop.parse_blocks( "[0:1)" );
    for( int i=0;  i<strs.length;  i++ )
      System.out.println( strs[i] );

    System.out.println( "\n" + "----- intervals -----" );
    Interval[] intervals = isop.parse_intervals( strs );
    for( int i=0;  i<intervals.length;  i++ )
      System.out.println(  intervals[i].toString()  );
    
  }
}
