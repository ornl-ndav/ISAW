/*
 * File: MOperator.java
 *
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Revision 1.3  2002/11/27 23:25:37  pfpeterson
 * standardized header
 *
 */
package DataSetTools.viewer.Table;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.dataset.*;
import java.util.*;
public class MOperator extends DataSetOperator
     { int paramPos = -1;
       Object DefValue = null;
       String Title = "xxx";
       DataSetOperator op = null;
       public  MOperator( DataSetOperator op , int paramPos , Object DefValue )
          {super( op.getTitle());
     
           Title = op.getTitle();
           this.paramPos = paramPos;
           this.op = op;
           this.DefValue = DefValue;
     
           setDefaultParameters();
          }
       public MOperator()
         {super( "unknown" );          
          setDefaultParameters();
         }
       public String getCommand()
         { return op.getCommand();
         }
       public void setDefaultParameters()
         {
           if( op == null )
             return;
           parameters = new Vector();        
           CopyParametersFrom( op );   
           parameters.remove( paramPos );      
        }

       public DataSet getDataSet()
          {return op.getDataSet();
          }
       public Object getResult()
         {
           for( int i = 0 ; i < paramPos ; i++ )       
              op.setParameter(  getParameter( i ) , i );
      
    
           op.setParameter( new Parameter( "ttt" , DefValue ) , paramPos );
     
           for( int i = paramPos + 1 ; i < op.getNum_parameters() ; i++ )
              op.setParameter( getParameter( i - 1 ) , i );        
     
           return  op.getResult();
         }
       public Object clone()
         {MOperator Res = new MOperator( op , paramPos , DefValue );
           return Res;
         }
   }//MOperator
