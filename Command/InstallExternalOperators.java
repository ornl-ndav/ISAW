/*
 * File:  InstallExternalOperators.java 
 *             
 * Copyright (C) 2006, Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 * * Modified:
 *
 * $Log$
 * Revision 1.2  2007/04/27 12:48:24  rmikk
 * Fixed javadoc errors
 *
 * Revision 1.1  2007/02/21 20:23:16  rmikk
 * A "singleton" class to handle the list of external operator handlers.  The
 * String name of an INewOperatorHandler class has to be added to a static
 * array of Strings for the external operators are to be added to the system.
 *
 * */
package Command;
import DataSetTools.operator.Generic.*;
import java.util.*;

public class InstallExternalOperators {
   /**
    * Add the class name for these INewOperatorHandler. For example, if the handler
    * is located in the file xxx/abc/def/java  the class name would be
    * xxx.abc.def.class.  These must be loadable via the System classloader
    */
   public static String[]  handlers= {};
   
   
   int newOpHandlerIndex =0;
   public InstallExternalOperators() {

      super();
     
   }
   
   public ExternalOpnInfo[] Restore(){
      Vector<ExternalOpnInfo>  V = new Vector<ExternalOpnInfo>();
      for( int i=0; i < handlers.length; i++){
         try{
            Class C = Class.forName( handlers[i]);
            if( C != null ){
               INewOperatorHandler Handler = (INewOperatorHandler)(C.newInstance());
               ExternalOpnInfo[] opInfos = Handler.Restore();
               if( opInfos != null)
                  for(int j =0; j< opInfos.length; i++ ){
                     V.addElement( opInfos[j]); 
                     
                  }
            }
         }catch(Exception s){
            
         }
      }
      ExternalOpnInfo[] Res = new ExternalOpnInfo[ V.size() ];
      for( int i=0; i< V.size(); i++)
         Res[i] = V.elementAt(i);
     
      return Res;
   }
      
  
   
   public void Save(){
      for( int i=0; i < handlers.length; i++){
         try{
            Class C = Class.forName( handlers[i]);
            if( C != null ){
               INewOperatorHandler Handler = (INewOperatorHandler)(C.newInstance());
               Handler.SaveOperators();
               
            }
         }catch(Exception s){
            
         }
      }
      
   }
   
   
   public GenericOperator getOperator( ExternalOpnInfo opInfo){
      String HandlerClassName = opInfo.HandlerClassName;
      try{
         Class C = Class.forName(HandlerClassName);
         if( C == null)
            return null;
         INewOperatorHandler Handler = (INewOperatorHandler)(C.newInstance());
         return Handler.getOperator( opInfo);
         
      }catch(Exception s){
         return null;
      }
   }
   
   /**
    * Use this form, if you have implemented some active load mechanism.
    * The only fields in op that must be specified are in the 2nd constructor from
    * Command.OpnInfo, 
    * 
    * @return an object that with information on the operator or null if there
    *      is no new operators
    */
 
   
   
   /**
    * Use this form to add a new operator to the installed operator list
    * @return the next Operator or null if there is none
    */
   public GenericOperator  getNextOperator(){
      if( newOpHandlerIndex< 0)
         return null;
      if(newOpHandlerIndex >= handlers.length){
         newOpHandlerIndex = -1;
         return null;
      }
      try{
         Class C = Class.forName( handlers[ newOpHandlerIndex ]);
         if( C != null){
           
         INewOperatorHandler Handler = (INewOperatorHandler)(C.newInstance());
         GenericOperator op = Handler.getNextOperator( );
         if( op !=null)
            return op;
         }
      }catch( Exception s){
         
      }
      newOpHandlerIndex++;
      return getNextOperator();
      
   }
   
   /**
    * loads  the Generic operator described by ExternalOpnInfo cp
    * @param op  Contains the information to get at the operator that is to be loaded
    * @return  returns the corresponding Generic operator
    */
   
   
   

}
