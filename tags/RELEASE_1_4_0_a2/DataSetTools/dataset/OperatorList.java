/*
 * File:  OperatorList.java
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.1  2002/06/14 21:17:25  rmikk
 *  Implements IXmlIO interface. Used to Handle the list
 *  of operators
 *
 */
  package DataSetTools.dataset;
  import DataSetTools.dataset.*;
  import java.io.*;
  import DataSetTools.operator.DataSet.*;

  /**
  *  Class to handle the list of operators of a data set
  */
  public class OperatorList implements IXmlIO
  { DataSet DS;

  /** 
  * Constructor for OperatorList
  *
  */ 
    public OperatorList( DataSet DS)
    { this.DS = DS;
    }


  /**
  * Implments the IXmlIO interface to let OperatorList write the list of
  * data set operators.
  *
  * @param stream the OutputStream to which the information is written
  * @param mode  either IXmlIO.BASE64 of IXmlIO.NORMAL.
  *
  * @return  true if successful otherwise false<P>
  *
  * NOTE: Does not write the parameters yet.<P>
  * NOTE: This routine writes both the end and start OperatorList tags<P>
  * NOTE: The operators are supplied by the DataSet given in the constructor<P>
  *
  * @see #OperatorList( DataSetTools.dataset.DataSet)  Constructor
  */
  public boolean XMLwrite( OutputStream stream, int mode)
  { StringBuffer SS = new StringBuffer( 500);
    SS.append("<OperatorList>\n");
    for( int i = 0; i< DS.getNum_operators(); i++)
    { DataSetTools.operator.DataSet.DataSetOperator op = DS.getOperator( i );
      SS.append( "<");
      String cls = op.getClass().toString();
      if(cls.indexOf("class") ==0)
      { cls = cls.substring(5);
        if( ": =-".indexOf( cls.charAt(0)) >=0)
          cls=cls.substring( 1);
      }
      cls=cls.trim();
      SS.append( cls); SS.append(">\n  </");
      SS.append( cls);
      SS.append(">\n");
    }
    SS.append("</OperatorList>\n");
    try
    {
      stream.write( SS.substring(0).getBytes());
         
    }
    catch(Exception s)
    { return xml_utils.setError("Err="+s.getClass()+" "+s.getMessage());
    }
    return true;
  }

  /**
  * Implments the IXmlIO interface to let OperatorList read a list of
  * data set operators.
  *
  * @param stream the InputStream from which the information is read
  *
  * @return  true if successful otherwise false <P>
  *
  * NOTE: Does not read the parameters yet<P>
  * NOTE: Assumes the <OperatorList> tag is fully read
  */
  public boolean XMLread( InputStream stream)
  { String Tag;
    boolean done = false;
    while( ! done)
    { Tag = xml_utils.getTag( stream);
      if( Tag == null)
        return xml_utils.setError(xml_utils.getErrorMessage());
        
      if(!xml_utils.skipAttributes( stream ))
        return xml_utils.setError(xml_utils.getErrorMessage()); 
      if( Tag.length() <1)
        return xml_utils.setError( "0 length tag in operator list");
      if( Tag.charAt(0) != '/')
        try
        {
          Class op = Class.forName( Tag );
          DataSetOperator dsop= (DataSetOperator)(op.newInstance());
          DS.addOperator( dsop);
          String X=xml_utils.skipBlock( stream);
          if( X == null)
            return xml_utils.setError(xml_utils.getErrorMessage());
          if(!X.equals('/'+Tag))
            return xml_utils.setError( "Improper End Tag for "+Tag);
        }
        catch(java.lang.NoClassDefFoundError s1)
        { xml_utils.setError( "Improper operator:"+
                    s1.getClass()+"::"+s1.getMessage());
        }
        catch( Exception s)
        { xml_utils.setError( "Improper operator:"+
                    s.getClass()+"::"+s.getMessage());
        }
      else if( Tag.equals("/OperatorList"))
        done = true;
      else 
        return xml_utils.setError("Improper End tag "+Tag+
                  "for OperatorList");
    }

    return true;
  }

  }
