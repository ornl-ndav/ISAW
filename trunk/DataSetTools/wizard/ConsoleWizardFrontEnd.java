/*
 * File:  ConsoleWizardFrontEnd.java
 *
 * Copyright (C) 2004 Chris Bouzek
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA and by
 * the National Science Foundation under grant number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2004/01/09 22:26:32  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.wizard;

import Command.*;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import DataSetTools.wizard.util.*;

import java.io.File;

import java.util.*;


/**
 * This class is meant as a front end for remote execution of Wizards.  It does
 * not have the capability to view intermediate results.
 */
class ConsoleWizardFrontEnd implements IWizardFrontEnd {
  //~ Instance fields **********************************************************

  private Wizard wiz = null;

  //~ Constructors *************************************************************

  /**
   * Creates a new ConsoleWizardFrontEnd.
   *
   * @param wizard The wizard to use for this front end.
   */
  public ConsoleWizardFrontEnd( Wizard wizard ) {
    wiz = wizard;
  }

  //~ Methods ******************************************************************

  /* (non-Javadoc)
   * @see DataSetTools.wizard.IWizardFrontEnd#getFile(boolean)
   */
  public File getFile( boolean saving ) {
    System.out.println( "\nWhere is the file located?  I need the full path:" );

    //somehow read the file name in
    String saveFilePath = getStringInput(  );

    File save_file            = new File( saveFilePath );

    if( saving ) {
      //make sure the extension is on there
      saveFilePath   = new WizardFileFilter(  ).appendExtension( 
          saveFilePath );
          
      save_file            = new File( saveFilePath );
    }

    if( saving && save_file.exists(  ) ) {
      String choice  = null;
      StringBuffer s = new StringBuffer(  );
      s.append( "\nYou are about to overwrite " );
      s.append( save_file.toString(  ) );
      s.append( ".\n  Is this is OK? [y/n]: " );
      System.out.println( s );

      //read the response
      choice   = getStringInput(  );
      choice   = choice.toLowerCase(  );

      //if this occurred, the user clicked <No>, so we'll do a recursive call
      if( choice.startsWith( "n" ) ) {
        return this.getFile( saving );
      }
    }

    //somehow we got a bad file
    if( ( save_file == null ) || save_file.getName(  ).equals( "" ) ) {
      System.out.println( "\nERROR: Please enter a valid file name." );

      return null;
    } else {
      //successfully retrieved a File
      return save_file;
    }
  }

  /* (non-Javadoc)
   * @see DataSetTools.wizard.IWizardFrontEnd#close()
   */
  public final void close(  ) {
    if( wiz.getModified(  ) ) {
      System.out.println( "\nWould you like to save? [y/n]" );

      //read the response
      String save = getStringInput(  );
      save   = save.toLowerCase(  );

      if( save.startsWith( "y" ) ) {
        wiz.save(  );
      }
    }

    //save the projects directory
    if( wiz.getProjectsDirectory(  ) != null ) {
      TextWriter.writeASCII( Wizard.CONFIG_FILE, wiz.getProjectsDirectory(  ) );
    }

    if( wiz.getStandalone(  ) ) {
      System.exit( 0 );
    }
  }

  /* (non-Javadoc)
   * @see DataSetTools.wizard.IWizardFrontEnd#displayAndSaveErrorMessage(java.lang.Throwable, java.lang.String, java.lang.StringBuffer)
   */
  public void displayAndSaveErrorMessage( 
    Throwable e, String errFile, StringBuffer errMess ) {
    String errDir         = wiz.getErrorDirectory(  );
    StringBuffer message  = errMess;
    GregorianCalendar cal = new GregorianCalendar(  );
    int month             = cal.get( Calendar.MONTH );
    int day               = cal.get( Calendar.DAY_OF_MONTH );
    String time           = String.valueOf( month ) + "_" +
      String.valueOf( day );
    errFile               = errFile + time + ".err";
    message.append( "\nPlease see the " );
    message.append( errFile );
    message.append( " file in\n" );
    message.append( errDir );
    message.append( " for more information.\n" );
    System.out.println( message );

    String saveFile = StringUtil.setFileSeparator( errDir + "/" + errFile );
    TextWriter.writeStackTrace( saveFile, e );
  }

  /**
   * Displays the given Form.  Unlike the SwingWizardFrontEnd, this prints out
   * the parameters, asking for input on each and storing the values.
   */
  public void showForm( int index ) {
    boolean ignore = wiz.getIgnorePropertyChanges(  );
    wiz.setIgnorePropertyChanges( true );

    if( ( index < 0 ) || ( index > ( wiz.getNumForms(  ) - 1 ) ) ) {  // invalid index
      System.out.println( 
        "\nError: invalid form number in show(" + index + ")\n" );

      return;
    }

    Form f = wiz.getForm( index );  // show the specified form
    wiz.setCurrentFormNumber( index );

    if( wiz.getNumForms(  ) == 1 ) {
      System.out.println( "\n" + f.getTitle(  ) + "\n" );
    } else {
      System.out.println( 
        "\nForm " + ( index + 1 ) + ": " + f.getTitle(  ) + "\n" );
    }

    //now go through the form's parameters, displaying the name and value 
    //for each
    ParameterGUI param = null;

    for( int i = 0; i < f.getNum_parameters(  ); i++ ) {
      param = ( ParameterGUI )f.getParameter( i );
      System.out.print( param.getName(  ) + "\t\t" );
      System.out.println( param.getValue(  ) );
    }
    System.out.println( "Navigation: " );
    System.out.print( FIRST_COMMAND + "\t\t" );
    System.out.println( IGUIWizardFrontEnd.FIRST_COMMAND );
    System.out.print( BACK_COMMAND + "\t\t" );
    System.out.println( IGUIWizardFrontEnd.BACK_COMMAND );
    System.out.print( NEXT_COMMAND + "\t\t" );
    System.out.println( IGUIWizardFrontEnd.NEXT_COMMAND );
    System.out.print( LAST_COMMAND + "\t\t" );
    System.out.println( IGUIWizardFrontEnd.LAST_COMMAND );
    System.out.print( CLEAR_COMMAND + "\t\t" );
    System.out.println( IGUIWizardFrontEnd.CLEAR_COMMAND );
    System.out.print( CLEAR_ALL_COMMAND + "\t\t" );
    System.out.println( IGUIWizardFrontEnd.CLEAR_ALL_COMMAND );
    System.out.print( EXEC_COMMAND + "\t\t" );
    System.out.println( IGUIWizardFrontEnd.EXEC_COMMAND );
    System.out.print( EXEC_ALL_COMMAND + "\t\t" );
    System.out.println( IGUIWizardFrontEnd.EXEC_ALL_COMMAND );
    System.out.print( SAVE_WIZARD_COMMAND + "\t\t" );
    System.out.println( IGUIWizardFrontEnd.SAVE_WIZARD_COMMAND );
    System.out.print( LOAD_WIZARD_COMMAND + "\t\t" );
    System.out.println( IGUIWizardFrontEnd.LOAD_WIZARD_COMMAND );
    System.out.print( EXIT_COMMAND + "\t\t" );
    System.out.println( IGUIWizardFrontEnd.EXIT_COMMAND );
    wiz.setIgnorePropertyChanges( ignore );
    processCommands(  );
  }

  /* (non-Javadoc)
   * @see DataSetTools.wizard.IWizardFrontEnd#showLastValidForm()
   */
  public void showLastValidForm(  ) {
    int lastValidNum = wiz.getLastValidFormNum(  );

    if( lastValidNum < 0 ) {
      showForm( 0 );
    } else {
      showForm( lastValidNum );
    }
  }

  /**
   * This method is meant to read a line from the command prompt and  return it
   * as a String.
   *
   * @return The line read from the command prompt.
   */
  private String getStringInput(  ) {
    return Script_Class_List_Handler.getString(  );
  }

  /**
   * Method designed to process command line input.  This is sort of the
   * equivalent of an ActionListener or KeyListener.
   */
  private void processCommands(  ) {
    String input   = getStringInput(  );
    int curFormNum = wiz.getCurrentFormNumber(  );

    try {
      //figure out what number they selected
      int selection = Integer.parseInt( input );

      switch( selection ) {
        case ( EXIT_COMMAND ): {
          close(  );

          break;
        }

        case ( FIRST_COMMAND ): {
          showForm( 0 );

          break;
        }

        case ( BACK_COMMAND ): {
          if( ( curFormNum - 1 ) >= 0 ) {
            showForm( --curFormNum );
          } else {
            System.out.println( "\nFORM 0 SHOWN, CAN'T STEP BACK\n" );
            showForm( curFormNum  );
          }

          break;
        }

        case ( NEXT_COMMAND ): {
          if( ( curFormNum + 1 ) < wiz.getNumForms(  ) ) {
            showForm( ++curFormNum );
          } else {
            System.out.println( "\nNO MORE FORMS, CAN'T ADVANCE\n" );
            showForm( curFormNum  );
          }

          break;
        }

        case ( LAST_COMMAND ): {
          showForm( wiz.getNumForms(  ) - 1 );

          break;
        }

        case ( CLEAR_COMMAND ): {
          wiz.invalidate( 0 );
          System.out.println( 
             "\n****Form " + ( curFormNum + 1 ) + " reset****" );
          showForm( curFormNum );

          break;
        }

        case ( CLEAR_ALL_COMMAND ): {
          wiz.invalidate( curFormNum );
          System.out.println( "\n****All forms reset****" );
          showForm( curFormNum );

          break;
        }

        case ( EXEC_COMMAND ): {
          wiz.exec_forms( curFormNum );
          showForm( curFormNum );

          break;
        }

        case ( EXEC_ALL_COMMAND ): {
          wiz.exec_forms( wiz.getNumForms(  ) - 1 );
          showForm( curFormNum );

          break;
        }

        case ( SAVE_WIZARD_COMMAND ): {
          wiz.save(  );
          showForm( curFormNum );

          break;
        }

        case ( LOAD_WIZARD_COMMAND ): {
          wiz.load(  );
          showForm( curFormNum );

          break;
        }
        
        
      }
    } catch( NumberFormatException nfe ) {
      System.out.println( "\nPlease select a valid choice\n" );
      processCommands(  );
    }
  }
}
