/* 
 * File: FavoritesViewMenu.java
 *
 * Copyright (C) 2010, Ruth Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author:$
 *  $Date:$            
 *  $Rev:$
 */
package DataSetTools.components.ui;

import gov.anl.ipns.Util.Sys.CreateJMenuTree;
import gov.anl.ipns.Util.Sys.FinishJFrame;
import gov.anl.ipns.Util.Sys.WindowShower;

import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

import Command.ScriptOperator;
import DataSetTools.components.ParametersGUI.IDataSetListHandler;
import DataSetTools.components.ParametersGUI.JParametersDialog;
import DataSetTools.operator.PyScriptOperator;
import DataSetTools.operator.Generic.GenericOperator;
import DataSetTools.util.SharedData;

/**
 * This class can produce a JMenu with submenu options for operators and menu
 * paths described in a favorites file designated in IsawProps.dat
 * 
 * @author ruth
 * 
 */
public class FavoritesViewMenu
{

   String[]    Menus;

   JMenuItem[] LeafItems;

   String      Menu;

   /**
    * Constructor.
    * 
    * @param dataSetList
    *           Handler for a list of data sets
    * 
    *           Also the System property Favorites is used to determine the file
    *           with the favorite information in it
    */
   public FavoritesViewMenu(IDataSetListHandler dataSetList)
   {
      SharedData dat = new SharedData();
      Menu = "Favorites";
      String filename = SharedData.getProperty(  "Favorites" );
      
      if ( filename == null )
      {
         Menus = null;
         LeafItems = null;
         return;
      }

      try
      {
         Vector< String > menus = new Vector< String >( );
         Vector< JMenuItem > leaves = new Vector< JMenuItem >( );
         Vector< JMenuItem > TailLeaves = new Vector< JMenuItem >( );

         FileInputStream fin = new FileInputStream( filename );

         JMenuItem LastJMenuItem = null;

         boolean done = false;
         while( !done )
         {
            String line = readLine( fin );
            if ( line == null )
               done = true;
            else if ( line.length( ) < 1 || line.trim( ).startsWith( "#" ) )
            {

            } else if ( line.trim( ).toUpperCase( ).startsWith( "MENU=" ) )
            {
               Menu = line.trim( ).substring( 5 );
               
            } else if ( line.trim( ).toUpperCase( ).startsWith( "PATH=" ) )

            {
               String X = line.trim( ).substring( 5 );
               if ( X != null && X.length( ) > 0 )
               {
                  if ( LastJMenuItem != null )
                  {
                     menus.add( X );
                     leaves.add( LastJMenuItem );
                     LastJMenuItem = null;
                  }
               } else
               {
                  if ( LastJMenuItem != null )
                     TailLeaves.add( LastJMenuItem );
                  LastJMenuItem = null;
               }

            } else if ( line.trim( ).toUpperCase( ).startsWith( "TITLE=" ) )
            {
               if ( LastJMenuItem != null )
               {
                  String Title = line.trim( ).substring( 6 );
                  LastJMenuItem.setText( Title );
               }
               
            } else if ( line.trim( ).toUpperCase( ).startsWith( "OPERATOR=" ) )
            {
               if ( LastJMenuItem != null )
                  TailLeaves.add( LastJMenuItem );
               
               LastJMenuItem = getOperatorHandler( line.trim( ).substring( 9 ) ,
                     dataSetList );
            }

         }
         
         Menus = menus.toArray( new String[ 0 ] );
         
         leaves.addAll( TailLeaves );
         
         LeafItems = leaves.toArray( new JMenuItem[ 0 ] );

      } catch( Exception ss )
      {
         JOptionPane.showMessageDialog( null ,
               "<html><body>Cannot read Favorites file " + filename
                     + ".<BR> Error is " + ss.toString( ) + "</body></html>" );
         
         Menus = null;
         LeafItems = null;
      }

   }

   /**
    * Returns a JMenu tree filled out with paths and with MenuItems that will
    * run operators.
    * 
    * @return a JMenu tree filled out with paths and with MenuItems that will
    * run operators.
    */
   public JMenu getFavoritesJMenu()
   {

      if ( LeafItems == null )
         return null;

      CreateJMenuTree jmt = new CreateJMenuTree( Menu , Menus , LeafItems );
      return jmt.getJMenu( );

   }

   private JMenuItem getOperatorHandler(String line,
         IDataSetListHandler dataSetList)
   {

      if ( line == null || line.length( ) < 1 )
         return null;

      if ( line.toUpperCase( ).trim( ).startsWith( "FILE:" ) )
         line = line.trim( ).substring( 6 ).trim( );
      else if ( line.toUpperCase( ).trim( ).startsWith( "CLASS:" ) )
         line = line.trim( ).substring( 7 ).trim( );
      line = line.trim( );
      try
      {
         GenericOperator op = null;
         if ( line.toUpperCase( ).endsWith( ".ISS" ) )
         {
            op = new ScriptOperator( line );

         } else if ( line.toUpperCase( ).endsWith( ".PY" ) )
         {
            op = new PyScriptOperator( line );

         } else
         {

            Class C = Class.forName( line );
            op = ( GenericOperator ) C.newInstance( );

         }
         JMenuItem menItem = new JMenuItem( op.getTitle( ) );
         menItem.addActionListener( new OpActionListener( op , dataSetList ) );
         return menItem;
      } catch( Exception s )
      {
         SharedData.addmsg( "Error in Parsing Favorites File " + s );
         SharedData.addmsg( "line =" + line );
         return null;
      }

   }

   /**
    * Test program
    * 
    * @param args
    */
   public static void main(String[] args)
   {

      SharedData D = new SharedData( );// to get in the System properties

      FinishJFrame jf = new FinishJFrame( "Test" );
      jf.setSize( 300 , 400 );
      
      JMenuBar jmenbar = new JMenuBar( );
      jf.setJMenuBar( jmenbar );
      
      FavoritesViewMenu fvmen = new FavoritesViewMenu( null );
      jmenbar.add( fvmen.getFavoritesJMenu( ) );
      
      WindowShower.show( jf );
   }

   /**
    * return null at end of file. "" for empty line
    * 
    * @param fin
    * @return
    */
   private String readLine(FileInputStream fin)
   {

      try
      {
         String S = "";
         int c = 0;
         for( c = fin.read( ) ; c < 32 && c >= 0 ; c = fin.read( ) )
         {
         }
         
         if ( c < 0 )
            return null;
         
         S += "" + ( char ) c;

         for( c = fin.read( ) ; c >= 32 ; c = fin.read( ) )
            S += "" + ( char ) c;

         return S;

      } catch( Exception s )
      {
         return null;
      }
   }

   //Listener that pops up the JParametersDialog box
   class OpActionListener implements ActionListener
   {

      GenericOperator     op;

      IDataSetListHandler dataSetList;

      public OpActionListener(GenericOperator op,
            IDataSetListHandler dataSetList)
      {

         this.op = op;
         this.dataSetList = dataSetList;
      }

      @Override
      public void actionPerformed(ActionEvent arg0)
      {

         new JParametersDialog( op , dataSetList , null , null );

      }

   }
}
