/*
 * File:  ParameterViewer.java
 *
 * Copyright (C) 2003, Christopher M. Bouzek
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intendse Pulsed Neutron Source
 * Division of Argonne National Laboratory and the National Science
 * Foundation.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.12  2004/01/08 14:51:15  bouzekc
 * Changed visibility on tryToDisplayASCII() to public static.  Added public
 * static method to view a DataSet from a runfile.
 *
 * Revision 1.11  2003/12/15 02:47:33  bouzekc
 * Removed unused imports.
 *
 * Revision 1.10  2003/08/16 01:41:03  bouzekc
 * Is now more aggressive when determining if a File exists in
 * tryToDisplayASCII().
 *
 * Revision 1.9  2003/08/14 23:40:59  bouzekc
 * Now sets the size of the JFrame "list" to 30% of the screen size and width.
 *
 * Revision 1.8  2003/08/14 23:08:14  bouzekc
 * Now handles StringBuffer values.  Will now display Strings as ASCII files
 * using ViewASCII.
 *
 * Revision 1.7  2003/08/14 02:27:30  bouzekc
 * Reformatted code.
 *
 * Revision 1.6  2003/06/26 16:48:15  bouzekc
 * Removed extraneous comment.
 *
 * Revision 1.5  2003/06/26 16:46:46  bouzekc
 * Reformatted for readability.
 *
 * Revision 1.4  2003/06/10 21:57:30  bouzekc
 * Now allows viewing of arrays of ASCII files using the
 * multiple view menu.  Split off code for viewing ASCII
 * files into a private method.
 *
 * Revision 1.3  2003/06/03 22:57:34  bouzekc
 * Changed ASCII file viewing to accept nearly all ASCII
 * files.
 *
 * Revision 1.2  2003/06/02 22:13:23  bouzekc
 * Added code to view ASCII files.
 * Fixed inconsistent indenting.
 *
 * Revision 1.1  2003/03/19 14:59:38  pfpeterson
 * Added to CVS. (Chris Bouzek)
 *
 */
package DataSetTools.parameter;

import DataSetTools.dataset.DataSet;

import DataSetTools.operator.Generic.Special.ViewASCII;

import DataSetTools.retriever.RunfileRetriever;

import DataSetTools.util.ErrorString;
import DataSetTools.util.SharedData;
import DataSetTools.util.TextWriter;

import DataSetTools.viewer.IViewManager;
import DataSetTools.viewer.ViewManager;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;


/**
 * Class for viewing IParameterGUIs.  Originally designed for the Wizards, so
 * that DataSets and other parameters could be viewed after operations were
 * performed.
 */
public class ParameterViewer implements ActionListener {
  //~ Instance fields **********************************************************

  private IParameterGUI param;
  private JFrame holder;
  private JList selector;
  private Container c;
  private JButton view_b;
  private JButton cancel_b;
  private JScrollPane jsp;
  private Vector items;
  private JPanel button_p;
  private String[] item_names;
  private final String VIEW   = "View";
  private final String CANCEL = "Cancel";

  //~ Constructors *************************************************************

  /**
   * Creates a new ParameterViewer object.
   *
   * @param ipg The IParameterGUI to attempt to view.
   */
  public ParameterViewer( IParameterGUI ipg ) {
    param = ipg;
  }

  //~ Methods ******************************************************************

  /**
   * Displays parameters based on their types.
   *
   * @param e The triggering ActionEvent.
   */
  public void actionPerformed( ActionEvent e ) {
    int index;
    Object obj;

    if( e.getActionCommand(  ).equals( CANCEL ) ) {
      holder.dispose(  );
    } else if( e.getActionCommand(  ).equals( VIEW ) ) {
      index = selector.getSelectedIndex(  );

      if( ( index >= 0 ) && ( items != null ) && ( items.size(  ) > 0 ) ) {
        obj = items.elementAt( index );

        //DataSet?
        if( obj instanceof DataSet ) {
          new ViewManager( ( DataSet )obj, IViewManager.IMAGE );
        } else if( obj instanceof String ) {
          tryToDisplayASCII( ( String )obj );
        }
      }
    }
  }

  /**
   * Utility method to display DataSets.  Defaults to image view.
   *
   * @param runfile The name of the Runfile to get the DataSet from.
   * @param dsNum The DataSet number to display.
   */
  public static void displayDataSet( String runfileName, int dsNum ) {
    new ViewManager( 
      new RunfileRetriever( runfileName ).getDataSet( dsNum ),
      IViewManager.IMAGE );
  }

  /**
   * Shows the parameters according to a preferred viewing state. For example,
   * a DataSet will be shown in a ViewManager, but a number (such as a Float)
   * is simply displayed in a message box.  For parameters which can be
   * selected from a list, such as an ArrayPG of DataSets, only one can be
   * selected at a time.
   */
  public void showParameterViewer(  ) {
    Object obj;
    Vector v;

    if( param != null ) {
      obj = param.getValue(  );

      if( obj instanceof StringBuffer ) {
        //make it a String right away
        obj = obj.toString(  );
      }

      //is it a DataSet?
      if( obj instanceof DataSet ) {
        new ViewManager( ( DataSet )obj, IViewManager.IMAGE );
      }
      //is it a Vector?
      else if( obj instanceof Vector ) {
        v = ( Vector )obj;

        if( !v.isEmpty(  ) ) {
          obj = v.elementAt( 0 );

          if( v.size(  ) == 1 ) {
            if( obj instanceof DataSet ) {
              new ViewManager( ( DataSet )obj, IViewManager.IMAGE );
            } else if( obj instanceof String ) {
              tryToDisplayASCII( ( String )obj );
            }
          } else {
            //display a list so the user can choose the DataSet to view
            //basic setup for selection list
            int num_items = v.size(  );
            item_names   = new String[num_items];
            items        = new Vector( num_items );

            for( int i = 0; i < num_items; i++ ) {
              obj             = v.elementAt( i );
              item_names[i]   = obj.toString(  );
              items.add( obj );
            }
            this.makeSelectionList(  );
          }
        }
      } else if( obj instanceof String ) {  //see if it is a file
        tryToDisplayASCII( ( String )obj );
      } else {
        //handle some other IParameterGUI types
        JOptionPane.showMessageDialog( null, obj.toString(  ) );
      }
    }
  }

  /**
   * Utility method to display ASCII text.  If the String given is a File, it
   * reads the File.  If the String is text, it will create a temporary File
   * so that the ViewASCII operator can display it.
   *
   * @param s The String (either filename or readable text) to display.
   */
  public static void tryToDisplayASCII( String s ) {
    String tempName;
    String fileName;
    s = s.replace( '\"', ' ' ).trim(  );

    //try to see if it is a File or a String
    File tempFile = new File( s );
    Object obj;

    if( tempFile.exists(  ) && tempFile.isFile(  ) ) {
      fileName = tempFile.toString(  );

      //try to determine if it is a viewable file
      //is this a file we can read?
      if( fileName.indexOf( '.' ) > 0 ) {
        tempName = fileName.toLowerCase(  );

        //ASCII files
        //we don't want any windoze executables, or 
        //any big ISAW data files
        if( 
          ( ( tempName.indexOf( ".sdds" ) >= 0 ) ||
            ( tempName.indexOf( ".exe" ) >= 0 ) ||
            ( tempName.indexOf( ".hdf" ) >= 0 ) ||
            ( tempName.indexOf( ".run" ) >= 0 ) ||
            ( tempName.indexOf( ".nxs" ) >= 0 ) ) ) {
          return;
        }
      }
    } else {
      try {
        //ASCII text, so create a temporary File for viewing
        tempFile = File.createTempFile( "results", null );
        TextWriter.writeASCII( tempFile, s );
        fileName = tempFile.toString(  );
      } catch( IOException ioe ) {
        SharedData.addmsg( "Unable to write temporary file " + tempFile );

        //something went really wrong...we'll have to leave
        return;
      }
    }

    //now display the File
    obj = new ViewASCII( fileName ).getResult(  );

    if( obj instanceof ErrorString ) {
      SharedData.addmsg( obj.toString(  ) );
    }
  }

  /**
   * Convenience method for displaying ASCII files
   *
   * @param file The file name for loading.
   */
  public static void tryToDisplayASCII( File file ) {
    tryToDisplayASCII( file.toString(  ) );
  }

  /**
   * Sets the JFrame's opening size.
   */
  private void setInitialSize(  ) {
    int screenheight = ( int )( Toolkit.getDefaultToolkit(  ).getScreenSize(  )
                                       .getHeight(  ) * 0.30f );
    int screenwidth  = ( int )( Toolkit.getDefaultToolkit(  ).getScreenSize(  )
                                       .getWidth(  ) * 0.30f );
    holder.setBounds( 0, 0, screenwidth, screenheight );
  }

  /**
   * Note that this relies mainly on what is in the String array item_names in
   * order to create the list
   */
  private void makeSelectionList(  ) {
    if( item_names != null ) {
      //create the selection list 
      holder = new JFrame(  );
      holder.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
      c = holder.getContentPane(  );
      setInitialSize(  );
      selector = new JList( item_names );
      selector.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      selector.setPreferredSize( new Dimension( 320, 240 ) );
      jsp = new JScrollPane( selector );
      jsp.setPreferredSize( new Dimension( 320, 240 ) );
      view_b     = new JButton( VIEW );
      cancel_b   = new JButton( CANCEL );
      view_b.addActionListener( this );
      cancel_b.addActionListener( this );
      view_b.setPreferredSize( new Dimension( 80, 40 ) );
      cancel_b.setPreferredSize( new Dimension( 80, 40 ) );
      c.setLayout( new BorderLayout(  ) );
      c.add( jsp, BorderLayout.CENTER );
      button_p = new JPanel( new GridLayout(  ) );
      button_p.add( view_b );
      button_p.add( cancel_b );
      c.add( button_p, BorderLayout.SOUTH );
      holder.show(  );
    }
  }
}
