/*
 * File:  TimeFocusGroupForm.java
 *
 * Copyright (C) 2003, Chris M. Bouzek
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
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 *           Chris M. Bouzek <coldfusion78@yahoo.com>
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * Modified:
 *
 * $Log$
 * Revision 1.14  2003/09/11 21:21:41  bouzekc
 * Updated to work with new Form class.
 *
 * Revision 1.13  2003/08/16 00:00:38  bouzekc
 * Modified to work with new IParameterGUI and ParameterGUI.
 *
 * Revision 1.12  2003/08/14 19:42:27  bouzekc
 * Fixed javadoc error.
 *
 * Revision 1.11  2003/07/03 15:30:04  bouzekc
 * Added missing javadoc and class comments, reformatted
 * existing comments, and rearranged methods according to
 * access privilege.
 *
 * Revision 1.10  2003/07/03 15:11:12  bouzekc
 * Fixed odd CVS log entries due to double inclusion of log
 * header tag.
 *
 * Revision 1.9  2003/06/25 20:24:44  bouzekc
 * Unused private variables removed, reformatted for
 * consistency.
 *
 * Revision 1.8  2003/06/18 23:09:47  bouzekc
 * Parameter error checking now handled by superclass Form.
 *
 * Revision 1.7  2003/06/18 19:55:27  bouzekc
 * Uses errorOut() to indicate parameter errors.  More robust
 * parameter error checking.  Now fires off property change
 * events in a semi-intelligent way.  Uses super.getResult() for
 * initializing PropertyChanger variables.
 *
 * Revision 1.6  2003/06/03 23:05:17  bouzekc
 * Fixed full constructor to avoid excessive garbage
 * collection.
 * Fixed documentation to reflect constructor
 * parameter changes.
 *
 * Revision 1.5  2003/06/02 22:25:27  bouzekc
 * Fixed contact information.
 * Added call to setDefaultParameters-needed to avoid
 * NullPointerExceptions.
 *
 * Revision 1.4  2003/04/24 18:58:24  pfpeterson
 * Various small bug fixes. (Chris Bouzek)
 *
 * Revision 1.3  2003/04/02 15:02:46  pfpeterson
 * Changed to reflect new heritage (Forms are Operators). (Chris Bouzek)
 *
 * Revision 1.2  2003/03/19 23:07:37  pfpeterson
 * Expanded TimeFocusGroupForm to allow for up to 20 'banks' to be
 * focused and grouped. (Chris Bouzek)
 *
 * Revision 1.1  2003/03/19 15:07:51  pfpeterson
 * Added to CVS. (Chris Bouzek)
 *
 */
package Wizard;

import DataSetTools.dataset.DataSet;

import DataSetTools.operator.Operator;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import DataSetTools.wizard.Form;

import Operators.TOF_Diffractometer.*;

import java.awt.*;

import java.io.*;

import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;


/**
 * This class defines a form for time focusing spectra in a DataSet under the
 * control of a Wizard.
 */
public class TimeFocusGroupForm extends Form implements Serializable {
  //~ Static fields/initializers ***********************************************

  public static final int NUM_BANKS = 20;

  //~ Instance fields **********************************************************

  private IParameterGUI[] ipgs;
  private int new_GID;
  private Float angle;
  private Float path;
  private String focusing_GIDs;

  //~ Constructors *************************************************************

  /**
   * Construct a TimeFocusGroupForm.  This constructor also calls
   * setDefaultParameters in order to set the permission type of the
   * parameters.
   */
  public TimeFocusGroupForm(  ) {
    super( "Time focus and group DataSets" );
    this.setDefaultParameters(  );
  }

  /**
   * Full constructor.  Uses the input parameters to create a
   * TimeFocusGroupForm without the need to externally set the parameters.
   * getResult() may be called immediately after using this constructor.
   *
   * @param hist_vector Vector of histograms that  you wish to time focus and
   *        group.
   * @param focus_IDs IDs to focus.
   * @param foc_angle Focusing angle.
   * @param new_path The new path.
   * @param tf_array The Vector which you wish to store the time focused
   *        histograms in.
   */
  public TimeFocusGroupForm( 
    Vector hist_vector, String focus_IDs, float foc_angle, float new_path,
    Vector tf_array ) {
    this(  );
    getParameter( 0 )
      .setValue( hist_vector );
    getParameter( 1 )
      .setValue( focus_IDs );
    getParameter( 2 )
      .setValue( new Float( foc_angle ) );
    getParameter( 3 )
      .setValue( new Float( new_path ) );
    getParameter( 4 )
      .setValue( tf_array );
  }

  //~ Methods ******************************************************************

  /**
   * @return the String command used for invoking this Form in a Script.
   */
  public String getCommand(  ) {
    return "TIMEFOCUSGROUPFORM";
  }

  /**
   * Attempts to set reasonable default parameters for this form. Included in
   * this is a default setting of the DataSet array corresponding to the
   * respective runfiles' loaded histograms, as well as the corresponding type
   * of the parameter (editable, result, or constant).  Since this is for a
   * bank of 20 detectors, the setup is for 60 editable parameters.  This can
   * be changed by changing the constant.
   */
  public void setDefaultParameters(  ) {
    parameters = new Vector(  );
    addParameter( new ArrayPG( "Histograms", new Vector(  ), false ) );

    for( int i = 0; i < NUM_BANKS; i++ ) {
      addParameter( new IntArrayPG( "Focusing IDs", new String( "" ), false ) );
      addParameter( new FloatPG( "Focusing angle", new Float( 0.5f ), false ) );
      addParameter( new FloatPG( "New Path", new Float( 1.0f ), false ) );
    }

    addParameter( 
      new ArrayPG( "Time focused histograms", new Vector(  ), false ) );

    int[] editable_params = new int[NUM_BANKS * 3];

    for( int i = 0; i < editable_params.length; i++ ) {
      editable_params[i] = i + 1;
    }

    setParamTypes( 
      new int[]{ 0 }, editable_params, new int[]{ ( NUM_BANKS * 3 ) + 1 } );
  }

  /**
   * @return documentation for this OperatorForm.  Follows javadoc conventions.
   */
  public String getDocumentation(  ) {
    StringBuffer s = new StringBuffer(  );

    s.append( "@overview This Form is designed for time focusing " );
    s.append( "and grouping spectra stored an ArrayPG of DataSets, under " );
    s.append( "the control of a Wizard.\n" );
    s.append( "@assumptions It is assumed that the specified DataSets are, " );
    s.append( "non-empty.  In addition, it is assumed that the specifed " );
    s.append( "group IDs exist.\n" );
    s.append( 
      "@algorithm This Form time focuses and then groups each DataSet " );
    s.append( "in the ArrayPG, using the specified group IDs, new path, and " );
    s.append( "new angle.  It then stores the results in another ArrayPG.\n" );
    s.append( "@param hist_vector Vector of histograms that you wish to time " );
    s.append( "focus and group.\n" );
    s.append( "@param focus_IDs Group IDs to focus.\n" );
    s.append( "@param foc_angle Focusing angle.\n" );
    s.append( "@param new_path The new path.\n" );
    s.append( "@param tf_vector The Vector which you wish to store the time " );
    s.append( "focused and grouped results in.\n" );
    s.append( "@return Presently, returns a Boolean which indicates either " );
    s.append( "success or failure.\n" );
    s.append( "@error Returns a Boolean false if the specified group IDs do " );
    s.append( "not exist.\n" );
    s.append( "@error Returns a Boolean false if the specified angle <= 0" );
    s.append( "@error Returns a Boolean false if the new path <= 0" );
    s.append( "@error Returns a Boolean false if the DataSet is empty" );

    return s.toString(  );
  }

  /**
   * Time focuses and groups the Vector of DataSets and loads them into a new
   * Vector of DataSets (in an ArrayPG).
   *
   * @return Boolean indicating success or failure.
   */
  public Object getResult(  ) {
    SharedData.addmsg( "Executing...\n" );

    ArrayPG histograms;
    ArrayPG tfgr;
    Vector hist_ds_vec;
    Operator tf;
    Operator gro;
    Object obj;
    Object result;
    DataSet hist_ds;
    int num_ds;
    int p_index;
    int edit_len;
    String errMessage = null;

    //get the DataSet array
    histograms    = ( ArrayPG )super.getParameter( 0 );
    hist_ds_vec   = ( Vector )histograms.getValue(  );

    //get the time focus/group result parameter
    tfgr = ( ArrayPG )super.getParameter( ( NUM_BANKS * 3 ) + 1 );

    //clear it out when the form is re-run
    tfgr.clearValue(  );

    edit_len = super.getParamType( Form.VAR_PARAM ).length;

    Object validCheck = validateSelf(  );

    //had an error, so return
    if( validCheck instanceof ErrorString ) {
      return validCheck;
    }

    //make sure list exists
    if( hist_ds_vec != null ) {
      //get the hist_ds_vec array size
      num_ds   = hist_ds_vec.size(  );

      //set the increment amount
      increment = ( 1.0f / num_ds ) * 100.0f;

      //go through the array, getting each runfile's hist_ds
      for( int i = 0; i < num_ds; i++ ) {
        obj = hist_ds_vec.elementAt( i );

        if( obj instanceof DataSet ) {
          hist_ds   = ( DataSet )obj;
          p_index   = 0;

          while( p_index < edit_len ) {
            focusing_GIDs   = ( String )this.getEditableParamValue( 
                ipgs[p_index] );
            angle   = ( ( Float )this.getEditableParamValue( ipgs[p_index + 1] ) );
            path    = ( ( Float )this.getEditableParamValue( ipgs[p_index + 2] ) );

            if( ( focusing_GIDs == null ) || ( focusing_GIDs.length(  ) <= 0 ) ) {
              p_index += 3;

              continue;
            }

            //was there an error entering params?
            //if( focusing_GIDs == null || angle == null || path == null )
            if( ( angle == null ) || ( path == null ) ) {
              return errorOut( "Error with detector bank parameters." );
            }

            //time_focus the DataSet
            if( hist_ds != DataSet.EMPTY_DATA_SET ) {
              //make a new DataSet-otherwise it will be somewhat difficult
              //to compare the results with the original
              tf   = new TimeFocusGID( 
                  hist_ds, focusing_GIDs, angle.floatValue(  ),
                  path.floatValue(  ), true );
              result = tf.getResult(  );
            } else {
              return errorOut( "Encountered empty DataSet: " + hist_ds );
            }

            if( result instanceof DataSet ) {
              hist_ds = ( DataSet )result;
              SharedData.addmsg( hist_ds + " time focused.\n" );

              //must have a list of group IDs in order to group it
              if( focusing_GIDs.length(  ) != 0 ) {  //DO NOT make a new DataSet
                gro   = new Grouping( hist_ds, focusing_GIDs, new_GID, false );
                result = gro.getResult(  );
              }
            } else {
              if( result instanceof ErrorString ) {
                errMessage = result.toString(  );
              } else {
                errMessage = "Could not time focus DataSet: " + hist_ds;
              }

              return errorOut( errMessage );
            }

            //check the grouped DataSet for correctness
            if( result instanceof DataSet ) {
              hist_ds = ( DataSet )result;
              SharedData.addmsg( hist_ds + " grouped." );
            } else {
              if( result instanceof ErrorString ) {
                errMessage = result.toString(  );
              } else {
                errMessage = ( "Could not group DataSet: " + hist_ds );
              }

              return errorOut( errMessage );
            }

            p_index += 3;
          }

          //while
          //add the time focused DataSet to time focused results
          tfgr.addItem( hist_ds );
        }  //if
        else {  //something went wrong in previous form

          return errorOut( "Encountered non-DataSet." );
        }

        //fire a property change event off to any listeners
        oldPercent = newPercent;
        newPercent += increment;
        super.fireValueChangeEvent( ( int )oldPercent, ( int )newPercent );
      }

      //for( num_ds )
      tfgr.setValid( true );
      SharedData.addmsg( "Finished time focusing and grouping DataSets.\n" );

      return new Boolean( true );
    }
    //broke, need to return false to let the wizard know
    else {
      return errorOut( "No histograms selected." );
    }
  }

  /**
   * This builds the 'editable' portion of the form. It provides a grid for
   * multiple detector banks.
   *
   * @param num an array of indices for the parameters which populate the
   *        sub-panel
   *
   * @return the JPanel which this method builds.
   */
  protected JPanel build_focus_grid( int[] num ) {
    String title = PARAM_NAMES[VAR_PARAM];

    if( ( parameters == null ) || ( parameters.size(  ) <= 0 ) ) {
      return null;
    }

    JPanel sub_panel;
    TitledBorder border;
    int num_params;

    num_params   = num.length;
    sub_panel    = new JPanel(  );
    border       = new TitledBorder( 
        LineBorder.createBlackLineBorder(  ), title );
    border.setTitleFont( FontUtil.BORDER_FONT );
    sub_panel.setBorder( border );

    //multiple grid entry
    sub_panel.setLayout( new GridLayout( 0, 3 ) );
    ipgs = new IParameterGUI[num.length];

    //get the params
    for( int i = 0; i < num_params; i++ ) {
      IParameterGUI param = ( IParameterGUI )parameters.elementAt( num[i] );

      param.initGUI( null );

      ipgs[i] = param;

      if( i < 3 ) {  //add the labels on this pass
        sub_panel.add( param.getLabel(  ) );
      }
    }

    for( int i = 0; i < num_params; i++ ) {
      sub_panel.add( ipgs[i].getEntryWidget(  ) );
    }

    return sub_panel;
  }

  /**
   * Overridden to use the build_focus_grid() method to build the GUI.
   */
  protected void makeGUI(  ) {
    Box box = new Box( BoxLayout.Y_AXIS );

    super.prepGUI( box );

    JPanel sub_panel;
    int[] param_type = null;

    for( int i = 0; i < 3; i++ ) {
      param_type = getParamType( i );

      if( ( param_type != null ) || ( param_type.length > 0 ) ) {
        if( i == VAR_PARAM ) {
          sub_panel = build_focus_grid( param_type );
        } else {
          sub_panel = super.build_param_panel( PARAM_NAMES[i], param_type );
        }

        if( sub_panel != null ) {
          box.add( sub_panel );
        }
      }
    }

    super.enableParameters(  );
  }

  /**
   * Gets the editable parameter values from the focus grid.  Used by
   * getResult().
   *
   * @param param The parameter value to get.
   *
   * @return the value of the parameter.
   */
  private Object getEditableParamValue( IParameterGUI param ) {
    Object obj;

    //get the focus IDs
    if( param.getName(  )
               .equals( super.getParameter( 1 ).getName(  ) ) ) {
      String focusing_GIDs;

      //get the user input parameters
      focusing_GIDs = ( ( IntArrayPG )param ).getStringValue(  );

      if( focusing_GIDs instanceof String ) {
        //set the new group ID
        if( focusing_GIDs.length(  ) > 0 ) {
          new_GID = new Integer( focusing_GIDs.substring( 0, 1 ) ).intValue(  );
        } else {
          new_GID = 1;
        }

        param.setValid( true );

        return focusing_GIDs;
      } else {
        new_GID = 1;
        param.setValid( true );

        return "1";
      }
    }
    //get focus angle
    else if( param.getName(  )
                    .equals( super.getParameter( 2 ).getName(  ) ) ) {
      Float angle;

      obj = param.getValue(  );

      if( obj instanceof Float ) {
        angle = ( ( Float )obj );
        param.setValid( true );

        return angle;
      } else {
        param.setValid( true );

        return new Float( 1.0f );
      }
    }
    //get path
    else if( param.getName(  )
                    .equals( super.getParameter( 3 ).getName(  ) ) ) {
      Float path;

      obj = param.getValue(  );

      if( obj instanceof Float ) {
        path = ( ( Float )obj );
        param.setValid( true );

        return path;
      } else {
        param.setValid( true );

        return new Float( 1.0f );
      }
    } else {
      return null;
    }
  }
}
