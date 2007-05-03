/*
 * File: IntrinsicJavaScript.java 
 *             
 * Copyright (C) 2007, Ruth Mikkelson
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
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2007/05/03 14:23:44  rmikk
 * Initial checkin for a class to make operators out of  scripting languages
 *   found by the javax.script package
 *
 */

package DataSetTools.operator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.text.Document;

import gov.anl.ipns.Util.Messaging.IObservable;
import gov.anl.ipns.Util.Messaging.IObserver;
import Command.IScriptProcessor;
import Command.Script;
import Command.ScriptUtil;
import DataSetTools.components.ParametersGUI.IDataSetListHandler;
import DataSetTools.dataset.DataSet;
import java.util.*;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Parameters.IParameter;
import DataSetTools.operator.Generic.GenericOperator;
import javax.script.*;
import gov.anl.ipns.Util.SpecialStrings.*;

/**
 * This class implements any scripting language found by the
 * javax.ScriptEngineManager. The scripting language must have special
 * constructs to get at any of the java classes or application classes needed.
 * All datasets on IsawGUI's tree will beavailable under the name ISAWDSx where
 * x is the tag( the number to the left of the colon in the name in the tree).
 * Also, the variable IOBS, a IObserverList, can be used to notify iobservers of
 * new Data Sets that can be sent to the tree.
 * 
 * @author Ruth
 * 
 */
public class IntrinsicJavaScript extends GenericOperator implements
         IScriptProcessor , IObserver , IObservable , IDataSetListHandler {

   Document              log;

   Document              doc;

   Command.Script        ScriptHandler;

   String                text;

   Vector                DataSets;

   PropertyChangeSupport PChange;

   String                errorMessage;

   int                   errLine;

   int                   errPos;

   String                title;

   int                   NumParams = 0;

   IObserverList         IobsList;

   Object                Result;

   String                filename;

   ScriptEngine          eng;

   boolean               ScriptLoaded;

   public String         ScriptLang;

   String                Command;

   boolean               hasSetDefaultParams;

   boolean               hasGetResult;


   /**
    * Constructor with no script
    * 
    * @param ScriptLang
    *           The scripting language
    * @throws IllegalArgumentException
    *            If the ScriptEngine cannot be created
    */
   public IntrinsicJavaScript( String ScriptLang )
                                           throws IllegalArgumentException {

      super( "UNKNOWN" );
      init();

      this.ScriptLang = ScriptLang;
      if( ScriptLang == null )
         throw new IllegalArgumentException( "Script language cannot be null" );
      
      eng = ( new ScriptEngineManager() ).getEngineByName( ScriptLang );
      
      if( eng == null )
         throw new IllegalArgumentException( "Scriptt language " + ScriptLang
                  + " is unknown" );
      setIsaw();
   }


   // Used be several constructors before the ScriptEngine is created
   private void init() {

      log = null;
      doc = null;
      text = null;
      DataSets = new Vector();
      PChange = new PropertyChangeSupport( this );
      errorMessage = "";
      errLine = - 1;
      errPos = - 1;
      title = null;
      IobsList = new IObserverList();
      Result = null;
      filename = null;
      ScriptLoaded = false;
      Command = null;
      hasSetDefaultParams = true;
      hasGetResult = true;


   }


   // Used by several consrucors afer the ScriptEngine is created
   private void setIsaw() {

      eng.put( "IOBS" , this.IobsList );
   }


 

   /**
    * Constructor that creates an operator from a file. The engine must
    * correspond to the filename
    * 
    * @param eng
    *           The script engine for the extension of filename
    * @param filename
    *           The file where the script is storedd
    * 
    * @throws IllegalArgumentException
    *            if the ScriptEngine cannot be created.
    */
   private IntrinsicJavaScript( ScriptEngine eng, String filename )
            throws IllegalArgumentException {

      super( "UNKNOWN" );
      init();
      
      this.eng = eng;
      this.filename = filename;
      
      Command.Script scr = new Script( filename );
      ScriptLang = eng.getFactory().getLanguageName();
      
      text = scr.toString();
      setDefaultParameters();
      
      setIsaw();
      

   }


   /**
    * Method to create an IntrinsicJavaScript Operator from a filename.
    * 
    * @param filename
    *           The name of the file with the script.
    * @return An instance of an IntrinsicJavaScript operator
    * @throws IllegalArgumentException
    *            If no ScriptEngine can be found to parse this script NOTE:
    *            Check getErrorMessage, getErrorLine, etc. to determine errors
    *            in setDefaultParameters.
    */
   public static IntrinsicJavaScript ScriptHandlerFromFile( String filename )
            throws IllegalArgumentException {

      if( filename == null )
         throw new IllegalArgumentException( " filename cannot be null" );
      
      int k = filename.lastIndexOf( "." );
      if( k < 0 )
         throw new IllegalArgumentException(
                  "filename does not have an extension" );
      
      String ext = filename.substring( k + 1 );
      if( ext == null )
         throw new IllegalArgumentException(
                  "filename does not have a good extension" );
      
      ScriptEngine eng = ( new ScriptEngineManager() )
               .getEngineByExtension( ext );
      
      if( eng == null )
         throw new IllegalArgumentException(
                  "File extension not recognized as a Script" );
      
      return new IntrinsicJavaScript( eng , filename );


   }


   /**
    * Utilitiy to return the names of all scripting languages currently
    * available to be IntrinsicJavaScript operators
    * 
    * @return An array of Strings of language names
    */
   public static String[] getScriptLanguages() {

      List< ScriptEngineFactory > engFactories = ( new ScriptEngineManager() )
               .getEngineFactories();
      
      String[] Res = new String[ engFactories.size() ];
      
      for( int i = 0 ; i < engFactories.size() ; i++ ) {
         
         ScriptEngineFactory fact = engFactories.get( i );
         Res[ i ] = fact.getLanguageName();
         
      }
      
      return Res;
   }


   /**
    * Adds the data set for access by the scripts under the name ISAWDSx where x
    * represents the tag on the data set
    * 
    * @param dss
    *           The data set that is to be added
    */
   public void addDataSet( DataSet dss ) {

      DataSets.addElement( dss );
      
      dss.addIObserver( this );
      
      long tag = dss.getTag();
      eng.put( "ISAWDS" + tag , dss );

   }


   // ---------------- GenericOperator methods
   // ----------------------------------------

   /**
    * Returns an array of Strings to determine where in the Macros menu this
    * operator will be listed. Currently these operators are NOT detected and so
    * will not occur in the Macros menu.
    */
   public String[] getCategoryList() {

      return new String[]{ "HiddenOperator" };
      
   }


   /**
    * Sets the logging documment
    * 
    * @param the
    *           document where the logging information goes.
    * 
    * No logging is included yet.
    */
   public void setLogDoc( Document doc ) {

      log = doc;

   }


   /**
    * Executes 1 line in a Document
    * 
    * @param Doc
    *           the document where the line of code to be executed resides.
    * @param line
    *           the line in the document that is to be executed.
    */
   public void execute1( Document Doc , int line ) {


   }


   /**
    * Must be set before getResult executes the code in this document.<BR>
    * The document does not have to be reset if only the text in it is changed
    * 
    * @param Doc
    *           The document containg the script.
    */
   public void setDocument( Document Doc ) {

      this.doc = Doc;
      ScriptHandler = new Script( Doc );
      text = ScriptHandler.toString();
      
      ScriptLoaded = false;
      
      setDefaultParameters();
      
      filename = null;
   }


   /**
    * Executes the getResult method of the script if any.
    * 
    * @return the result of the scripts getResult method or ,if none, the value
    *         of the Result variable in the script.
    */
   public Object getResult() {

      if( text == null )
         return new ErrorString( "No Script is loaded" );
      
      if( ! LoadScript() )
         return new ErrorString( "ScriptError:" + errorMessage );
      
      if( hasGetResult )
         try {
            
            Result = eng.eval( "getResult()" );
            hasGetResult = true;
            return Result;
            
         }
         catch( Exception s ) {
            
            hasGetResult = false;
         }

      try {
         
         Result = eng.get( "Result" );
         return Result;
         
      }
      catch( Exception ss ) {
         
         return Result;
      }

   }


   /**
    * Not implemented yet. Should call a reset in the script to initialize the
    * state of any variables
    */
   public void reset() {


   }


   /**
    * Resets the error variables
    */
   public void resetError() {

      errorMessage = "";
      errPos = - 1;
      errLine = - 1;


   }


   /**
    * Returns the position on the line where the error occurred
    * 
    * @return the position on the line where the error occurred or -1 if there
    *         is no error.
    */
   public int getErrorCharPos() {


      return errPos;
   }


   /**
    * Returns the line where the error occurred
    * 
    * @return the p line where the error occurred or -1 if there is no error.
    */
   public int getErrorLine() {


      return errLine;
   }


   /**
    * Returns a message about the error
    * 
    * @return a message about the error or an empty string if there is no error.
    */
   public String getErrorMessage() {


      return errorMessage;
   }


   /**
    * Not used
    * 
    * @param pcs
    *           a PropertyChange Support object
    */
   public void setPropertyChangeList( PropertyChangeSupport pcs ) {

      PChange = pcs;

   }


   /**
    * Gets the number of parameters. This is stored in the variable
    * NumParameters in the script
    * 
    * @return the number of parameters
    */
   public int getNum_parameters() {


      return NumParams;
   }


   /**
    * Sets the title for this operator
    * 
    * @param title
    *           The new title for this operator
    */
   public void setTitle( String title ) {

      this.title = title;

   }


   /**
    * Uses Script Variables NumParameters, and Parameter1, Parameter2, and the
    * setDefaultParameters in the Script to determine the parameters. ...
    * .NumParameters must be an double and Paramter1,Parameter2, etc. must be an
    * IParameter.
    */
   public void setDefaultParameters() {

      if( text == null )
         return;
      
      if( ! LoadScript() )
         return;
      
      if( hasSetDefaultParams )
         try {

            eng.eval( "setDefaultParameters()" );
            NumParams = ( (Number) eng.get( "NumParameters" ) ).intValue();
            hasSetDefaultParams = true;

         }
         catch( ScriptException s ) {
            
            errPos = s.getColumnNumber();
            errLine = s.getLineNumber();
            errorMessage = s.getMessage();
            NumParams = 0;
            hasSetDefaultParams = false;
           
            return;
            
         }
         catch( Throwable s ) {
            
            errorMessage = s.toString();
            errPos = 0;
            errLine = 0;
            hasSetDefaultParams = false;
            
         }


   }


   /**
    * Returns an array of DataSets that can be used by the scripts.
    * 
    * @return an array of DataSets that can be used by the scripts.
    */
   public DataSet[] getDataSets() {

      DataSet[] DSS = new DataSet[ DataSets.size() ];
      
      for( int i = 0 ; i < DSS.length ; i++ )
         
         DSS[ i ] = (DataSet) ( DataSets.elementAt( i ) );
      
      return DSS;
   }


   public void addPropertyChangeListener( PropertyChangeListener pcl ) {

      PChange.addPropertyChangeListener( pcl );

   }


   public void addPropertyChangeListener( String propery ,
            PropertyChangeListener pcl ) {

      PChange.addPropertyChangeListener( propery , pcl );

   }


   public void removePropertyChangeListener( PropertyChangeListener pcl ) {

      PChange.removePropertyChangeListener( pcl );
   }


   /**
    * Checks for data sets that are destroyed so they can be deleted from this
    * system
    * 
    * @param observed_obj
    *           The object that has changed
    * @peram reason the reason for the change
    */
   public void update( Object observed_obj , Object reason ) {

      if( observed_obj instanceof DataSet )
         if( reason == IObserver.DESTROY ) {
            
            DataSet dss = (DataSet) observed_obj;
            DataSets.remove( dss );

            Bindings bnd = eng.getBindings( ScriptContext.ENGINE_SCOPE );
            long tag = dss.getTag();
            bnd.remove( "ISAWDS" + tag );
            eng.setBindings( bnd , ScriptContext.ENGINE_SCOPE );
            
            return;
         }
   }


   public void propertyChange( PropertyChangeEvent arg0 ) {


      if( PChange == null )
         return; // no one to notify
      
      PChange.firePropertyChange( arg0 );

   }


   public void addIObserver( IObserver iobs ) {

      IobsList.addIObserver( iobs );

   }


   public void deleteIObserver( IObserver iobs ) {

      IobsList.deleteIObserver( iobs );

   }


   public void deleteIObservers() {

      IobsList.deleteIObservers();

   }


   /**
    * Returns the title for this operator
    */
   public String getTitle() {

      if( title != null )
         return title;

      try {
         
         title = (String) eng.eval( "getTitle()" );
         
      }
      catch( Exception s ) {
         
         title = this.GetFileNameInfo( filename , false );
         
      }
      return title;
   }


   // loads the script into the script engine if it has not already been loaded.
   // returns false if an error occurs on loading the script in.
   private boolean LoadScript() {

      if( ! ScriptLoaded ) {
         
         try {
            
            Result = eng.eval( text );
            ScriptLoaded = true;
            return true;
            
         }
         catch( ScriptException s ) {
            
            errPos = s.getColumnNumber();
            errLine = s.getLineNumber();
            errorMessage = s.getMessage();
            return false;
            
         }
      }
      return true;

   }


   /**
    * Returns the command name for this operator. Since these are not installed
    * this is not used.
    * 
    * @return Command name
    */
   public String getCommand() {

      if( ! LoadScript() )
         return "UNKNOWN";
      
      if( Command != null )
         return Command;
      
      try {
         
         Object C = eng.eval( "getCommand()" );
         Command = C.toString();
         return Command;
         
      }
      catch( Exception s ) {
         
         if( filename != null )
            Command = GetFileNameInfo( filename , true );
         else
            Command = "UNKNOWN";
         
         return Command;
         
      }
   }


   /**
    * Calculates default names for title and Command from the filename
    * 
    * @param filename
    *           The filename
    * @param isVar
    *           True if the resultant name must be a variable otherwise false.
    * @return The fixed up name
    */
   private String GetFileNameInfo( String filename , boolean isVar ) {

      if( filename == null )
         if( isVar )
            return "UNKNOWN";
         else
            return "";
      
      int k = filename.lastIndexOf( "." );
      if( k < 0 )
         k = filename.length();
      
      String Res = filename.replace( '\\' , '/' );
      
      int k1 = Res.lastIndexOf( '/' );
      if( k1 < 0 )
         k1 = - 1;
      
      Res = filename.substring( k1 + 1 , k );
      
      if( ! isVar )
         return Res;
      
      Res = Res.replace( ' ' , '_' );
      Res = Res.replace( '-' , '_' );
      Res = Res.replace( '&' , '_' );
      Res = Res.replace( '+' , '_' );
      Res = Res.replace( '*' , '_' );
      Res = Res.replace( '^' , '_' );
      Res = Res.replace( '/' , '_' );
      
      return Res;
   }


   /**
    * Returns the index-th parameter in the parameter list
    * 
    * @param index
    *           the index( starting at 0) of the parameter in question
    * 
    * @return the Parameter of interest or null if none
    */
   public IParameter getParameter( int index ) {

      if( index < 0 )
         return null;
      
      if( index >= NumParams )
         return null;
      
      if( ! LoadScript() ) {
         
         return null;
         
      }
      
      try {
         
         IParameter P = (IParameter) eng.get( "Parameter" + index );
         return P;
         
      }
      catch( Throwable t ) {
         
         return null;

      }
   }


   /**
    * Returns the result of the getDocumentation from the script or the default
    * documentation
    * 
    * @return the result of the getDocumentation from the script or the default
    *         documentation
    */
   public String getDocumentation() {

      if( ! LoadScript() )
         return super.getDocumentation();
      
      try {
         
         return (String) eng.eval( "getDocumentation()" );
         
      }
      catch( Exception s ) {
         
         String S = super.getDocumentation();
         S = S + "<P>";
         S += " To integrate a script in ISAW, you must have<br>";
         S += "  1. Methods setDefaultParameters() and Object getResult()<br>";
         S += "  2. Variable NumParameters and Parameter0,Parameter1,...of the";
         S += " correct data types<br>";
         S += "3.(optional) Methods getTitle(),getCommand(), setDocumenation ";
         S += "are used if available<br>";
         S += "<P> Currently these operators are not autodetected";
         
         return S;
      }
   }


   /**
    * Test program for this module. It creates a JParamatersDialog, liss the
    * Scripting languages and extensions.
    * 
    * @param args
    *           The filename with the script in it.
    */
   public static void main( String[] args ) {

      ScriptUtil.display( IntrinsicJavaScript.getScriptLanguages() );
      
      System.out.println( "-------------------------------" );
      List< ScriptEngineFactory > fact = ( new ScriptEngineManager() )
               .getEngineFactories();
      
      for( int i = 0 ; i < fact.size() ; i++ ) {
         
         ScriptEngineFactory f = fact.get( i );
         List< String > exts = f.getExtensions();
         
         for( int j = 0 ; j < exts.size() ; j++ )
            System.out.println( exts.get( j ) );
         
         System.out.println( "              ------------" );
         
      }

      IntrinsicJavaScript jsc = IntrinsicJavaScript
               .ScriptHandlerFromFile( args[ 0 ] );

      new DataSetTools.components.ParametersGUI.JParametersDialog( jsc , null ,
               null , jsc );

   }
}
