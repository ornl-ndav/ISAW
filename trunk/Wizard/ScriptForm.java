/*
 * File:  ScriptForm.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
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
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2003/02/26 17:22:07  rmikk
 * Now writes to DataSetTools.util.SharedData.status_pane
 *
 * Revision 1.1  2003/02/24 13:31:49  rmikk
 * Initial checkin.  Creates a form from a script or GenericOperator
 *
 */
package Wizard;


import DataSetTools.wizard.*;
import Command.*;
import DataSetTools.util.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import java.beans.*;


/** This class creates a form that executes a Script or Java Operator
 */
public class ScriptForm extends Form implements IObserver, PropertyChangeListener
{

   GenericOperator operator;
   String errormessage;
   Wizard wizard;
   String[] ScriptParamNames, result_params;
   String title;

   /** Constructor for ScriptForm
    *  @param title    The title on the top of the form
    *  @param const_params   The name of the parameters that cannot change
    *  @param editable_params  The name of the parameters that can be editted
    *  @param result_params    The name of the parameters that can get a result
    *  @param  operator    The operator that is executed in the execute command
    *  @param ScriptParameterNames  The wizard parameter names corresponding to the
    *              operators arguments in the proper order for the operator
    *  @param wizard  The wizard with the parameter names
    *  
    *  NOTE: An error will result in this form doing nothing.
    */
   public ScriptForm( String title, String[] const_params,
                      String[] editable_params, String[] result_params,
                      GenericOperator operator, String[] ScriptParameterNames,
                      Wizard wizard )
   {
      super( title, const_params, editable_params, result_params, wizard );
      this.operator = operator;
      errormessage = null;
      this.title = title;
      if( operator instanceof ScriptOperator )
         errormessage = ( ( ScriptOperator ) operator ).getErrorMessage();
      if( errormessage != null )
         if( errormessage.length() < 1 )
            errormessage = null;
      if( errormessage != null )
         if( errormessage.length() > 0 )
         {
            seterr( "Error in operator" +
               operator.getCommand() + ":" + errormessage + "\n" );
            operator = null;
            return;
         }

         //Send can send things to Iobservers
      if( operator instanceof IObservable )
         ( ( IObservable ) operator ).addIObserver( this );
      if( operator instanceof ScriptOperator )
         ( ( ScriptOperator ) operator ).addPropertyChangeListener( this );
      operator.setDefaultParameters();
      this.wizard = wizard;
      ScriptParamNames = ScriptParameterNames;
      this.result_params = result_params;

   }

   /** This update method is required to implement the IObserver interface.
    *   The ScriptOperators Save command invoke this method.  If reason is one
    *   of the result_params, that result_parm will be changed to the value of
    *   observed_obj. 
    *
    *  NOTE: reason should not correspond to any strings listed at the top of the
    *  IObserver interface 
    */
   public void update( Object observed_obj, Object reason )
   {  
      if( reason == null )
         return;
      if( !( reason instanceof String ) )
         return;
      boolean isResult = false;

      if( result_params == null )
         return;
      for( int i = 0; ( i < result_params.length ) && !isResult; i++ )
         if( reason.equals( result_params[i] ) )
            isResult = true;

      if( !isResult )
         return;

      IParameterGUI ipgui = wizard.getParameter( ( String ) reason );

      ipgui.setValue( observed_obj );
   }

   //utility method to quickly report errors everywhere
   private boolean seterr( String err )
   {
      errormessage = err;
      DataSetTools.util.SharedData.addmsg( err );
      return false;
   }

   /** 
    *  This method is required to implement the PropertyChangeListener interface.
    *  Scripts will invoke this method with the "Display" command.
    *  This method will show the new value in the status_display
    */
   public void propertyChange( PropertyChangeEvent evt )
   {  
      if( !( evt.getPropertyName().equals( "Display" ) ) )
         return;

      DataSetTools.util.SharedData.addmsg( ( new NexIO.NxNodeUtils() ).Showw(
            evt.getNewValue() ) );

   }

   /** 
    *  Executes the operator after setting its parameter values
    */
   public boolean execute()
   { 
      if( operator == null )
         return seterr( "no operator in form" + getTitle() + ":" + errormessage );
      if( ScriptParamNames == null )
         if( operator.getNum_parameters() > 0 )
            return seterr( "The number of operator parameters does not equal the " +
                  "number of ScriptParamNames in form " + getTitle() );
      if( ScriptParamNames != null )
         if( ScriptParamNames.length != operator.getNum_parameters() )
            return seterr( "The number of operator parameters does not equal the " +
                  "number of ScriptParamNames in form " + getTitle() );


      for( int i = 0; i < operator.getNum_parameters(); i++ )
      {
         Object value = wizard.getParameter( ScriptParamNames[i] ).getValue();

         Object ppvalue = operator.getParameter( i ).getValue();
         // Could not get this error checking to compile
         // if( ppvalue != null) 
         //   if( !(value instanceof ppvalue.getClass()))
         //      return false;

         Parameter x = new Parameter( "", value );

         value = operator.getParameter( i ).getValue();

         operator.setParameter( x, i );

      }

      Object result = operator.getResult();

      if( result instanceof ErrorString )
         return seterr( "operator result " + result + " in form " + getTitle() );
      if( result_params != null )
         if( result_params.length > 0 )
         {
            IParameterGUI param = wizard.getParameter( result_params[0] );

            param.setValue( result );
         }
      validateAllParams();
      setCompleted( true );
      return true;

   }
  private void validateAllParams()
   {int i;
    if(const_params != null)
      for( i=0;i<const_params.length;i++)
        { IParameterGUI ipg =wizard.getParameter( const_params[i]);
          if( ipg != null)
             ipg.setValid( true);
         }

    if(editable_params != null)
      for( i=0;i<editable_params.length;i++)
        { IParameterGUI ipg =wizard.getParameter( editable_params[i]);
          if( ipg != null)
             ipg.setValid( true);
         }
    if(result_params != null)
      for( i=0;i<result_params.length;i++)
        { IParameterGUI ipg =wizard.getParameter( result_params[i]);
          if( ipg != null)
             ipg.setValid( true);
         }





    }

   /**Test program for this class.  It requires a script whose name is Tscript.iss with 2 
   * integer parameters.  The file must be in the directory where the java command was issued.
   */
   public static void main( String args[] )
   {
      Wizard wz = new Wizard( "Test case", true );

      wz.setParameter( "abc", new IntegerPG( "abc", 5 ,true) );
      wz.setParameter( "cde", new IntegerPG( "cde", 7,true ) );
      wz.setParameter( "fgh", new IntegerPG( "fgh", 9 ,true) );
      wz.setParameter( "ijk", new IntegerPG( "ijk", 11 ,true) );
      wz.setParameter( "lmn", new IntegerPG( "lmn", 13 ,true) );

      String edit_parms_1[] = { "abc", "cde" };
      String out_parms_1[] = {"ijk", "lmn"};
      String Script_Params[] = {"cde", "abc"};
      ScriptForm sf = new ScriptForm( "sub abc-cde", null, edit_parms_1, out_parms_1,
            new ScriptOperator( "Tscript.iss" ), Script_Params, wz );

      wz.add( sf );

    /*
       String constParams2[]={"ijk","abc"};
       String edit_parms_2[] ={"cde","fgh"};
       String out_parms_2[]= {"lmn"};
       String[] Script_Params1 = {"ijk","cde"};

       ScriptForm sf1= new ScriptForm( "sub cde-ijk",constParams2, edit_parms_2,
       out_parms_2,
       new ScriptOperator("Tscript.iss"),Script_Params1, wz);
       wz.add(sf1);
       
    */

      wz.show( 0 );

   }

}
