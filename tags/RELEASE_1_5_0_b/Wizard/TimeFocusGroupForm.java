/*
 * File:  TimeFocusGroupForm.java
 *
 * Copyright (C) 2003, Christopher Bouzek
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * Modified: 
 *
 * $Log$
 * Revision 1.2  2003/03/19 23:07:37  pfpeterson
 * Expanded TimeFocusGroupForm to allow for up to 20 'banks' to be
 * focused and grouped. (Chris Bouzek)
 *
 * Revision 1.1  2003/03/19 15:07:51  pfpeterson
 * Added to CVS. (Chris Bouzek)
 *
 * Revision 1.5  2003/03/13 19:00:52  dennis
 * Added $Log$
 * Added Revision 1.2  2003/03/19 23:07:37  pfpeterson
 * Added Expanded TimeFocusGroupForm to allow for up to 20 'banks' to be
 * Added focused and grouped. (Chris Bouzek)
 * Added
 * Added Revision 1.1  2003/03/19 15:07:51  pfpeterson
 * Added Added to CVS. (Chris Bouzek)
 * Added comment to include revision information.
 *
 *
 */

package Wizard;

import java.io.*;
import DataSetTools.wizard.*;
import DataSetTools.parameter.*;
import DataSetTools.dataset.*;
import java.util.Vector;
import DataSetTools.operator.Operator;
import Operators.TOF_Diffractometer.*;
import DataSetTools.util.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;

/**
 *  This class defines a form for time focusing spectra in
 *  a DataSet under the control of a Wizard.
 */
public class TimeFocusGroupForm extends    Form
                              implements Serializable
{
  private IParameterGUI[] ipgs;
  private int new_GID;
  private Float angle, path;
  private String focusing_GIDs;
  
  /**
   *  Construct a TimeFocusGroupForm to time focus and group the spectra in a 
   *  DataSet using the arguments in operands[].  This constructor basically
   *  just calls the super class constructor and builds an appropriate
   *  help message for the form.
   *
   *  @param  constants The list of names of parameters to be time focused
                        and grouped.
   *  @param  operands  The list of names of parameters to use for the
   *                    time focusing and grouping    .
   *  @param  result    The list of names of parameters which have been
   *                    time focused and grouped.
   *  @param  w         The wizard controlling this form.
   */
  public TimeFocusGroupForm( String constants[], String operands[], String result[], Wizard w )
  {
    super("Time focus DataSets", constants, operands, result, w );

    StringBuffer help = new StringBuffer();
    help.append("This form lets you time focus and group the spectra in one or more ");
    help.append("DataSets.\nNote that whatever you put in for parameters will affect ");
    help.append("ALL DataSets in the list.\n");
    setHelpMessage( help.toString() );
  }

  /**
   *  This builds the portions of the default form panel that contain a
   *  list of parameters inside of a panel with a titled border.  It is
   *  used to build up to three portions, corresponding to the constant,
   *  user-specified and result parameters.
   *
   *  @param  title  The title to put on the border
   *  @param  params The names of the parameter to include in this sub-panel
   */
  protected JPanel build_param_panel( String title, String params[] )
  {
    if ( params == null || params.length <= 0 )
      return null;

    JPanel sub_panel;
    TitledBorder border;
    int num_params;
    final int NUM_IDS = 20;
    
    num_params = params.length;
    sub_panel = new JPanel();
    border = new TitledBorder(LineBorder.createBlackLineBorder(), title);
    border.setTitleFont( FontUtil.BORDER_FONT );
    sub_panel.setBorder( border );
    
    //multiple grid entry
    if( title == VAR_FRAME_HEAD )
    {
      sub_panel.setLayout( new GridLayout( 0, 3 ) );
      ipgs = new IParameterGUI[num_params];
    }      
    else
      sub_panel.setLayout( new GridLayout( params.length, 1 ) );
       
    //get the params
    for ( int i = 0; i < num_params; i++ )
    {
      IParameterGUI param = wizard.getParameter( params[i] );
      param.init();
      
      
      if( title == VAR_FRAME_HEAD )
      {
        ipgs[i] = param;
        
        if( i < 3 )  //add the labels on this pass
          sub_panel.add(param.getLabel());
      }
      else
        sub_panel.add( param.getGUIPanel() );
          
    }
     
      if( title == VAR_FRAME_HEAD )
        for( int i = 0; i < num_params; i++ )
          sub_panel.add(ipgs[i].getEntryWidget());

    return sub_panel;
  } 

  /**
   *  Time focuses and groups the Vector of DataSets and loads them
   *  into a new Vector of DataSets (in an ArrayPG).
   *
   *  @return true if all of the parameters are valid and all hist_ds
   *  can be time focused and grouped; false if any significant error occurs
   */
  public boolean execute()
  {
    SharedData.addmsg("Executing...\n");
    ArrayPG histograms, tfgr;
    Vector hist_ds_vec;
    Operator tf, gro;
    Object obj, result;
    DataSet hist_ds;
    int num_ds, p_index, edit_len;
    IParameterGUI param;
    DataSet ds;

    //get the DataSet array
    histograms = (ArrayPG)wizard.getParameter("RunList");
    hist_ds_vec = (Vector)histograms.getValue();

    //get the time focus/group result parameter
    tfgr = (ArrayPG)wizard.getParameter("TimeFocusGroupResults");
    //clear it out when the form is re-run
    tfgr.clearValue();
    
    edit_len = editable_params.length;

    //make sure list exists
    if( hist_ds_vec != null )
    {
      //get the hist_ds_vec array size
      num_ds = hist_ds_vec.size();
      //go through the array, getting each runfile's hist_ds
      for( int i = 0; i < num_ds; i++ )
      {
        obj = hist_ds_vec.elementAt(i);

        if( obj instanceof DataSet )
        {
        hist_ds = (DataSet)obj;
        p_index = 0;
        
        while( p_index < edit_len )
        { 
          focusing_GIDs = (String)this.getEditableParamValue(ipgs[p_index]);
          angle = ((Float)this.getEditableParamValue(ipgs[p_index + 1]));
          path = ((Float)this.getEditableParamValue(ipgs[p_index + 2]));
          
          //was there an error entering params?
          if( focusing_GIDs == null || angle == null || path == null )
          {
            System.out.println("ERROR");
            return false;               
          }   

          //time_focus the DataSet
          if( hist_ds != DataSet.EMPTY_DATA_SET )
          {
            //do NOT make a new DataSet
            tf = new TimeFocusGID(hist_ds, focusing_GIDs,
                               angle.floatValue(), path.floatValue(), false);
            result = tf.getResult();
          }
          else
          {
            SharedData.addmsg("Encountered empty DataSet: " + hist_ds);
            return false;
          }

          if( result instanceof DataSet )
          {
            hist_ds = (DataSet)result;
            SharedData.addmsg(hist_ds + " time focused.\n");
            
            //must have a list of group IDs in order to group it
            if( focusing_GIDs.length() != 0 )
            { //DO NOT make a new DataSet
              gro = new Grouping(hist_ds, focusing_GIDs, new_GID, false);
              result = gro.getResult();
            }
          }
          else
          {
            if( result instanceof ErrorString )
              SharedData.addmsg(result.toString() + "\n");
            else
              SharedData.addmsg("Could not time focus DataSet: "
                                + hist_ds);
            return false;
          }
          
          //check the grouped DataSet for correctness
          if( result instanceof DataSet )
          {
            hist_ds = (DataSet)result;
            SharedData.addmsg(hist_ds + " grouped.\n");
          }
          else
          {
            if( result instanceof ErrorString )
              SharedData.addmsg(result.toString() + "\n");
            else
              SharedData.addmsg("Could not group DataSet: "
                                + hist_ds);
            return false;
          }          
          
          p_index += 3;
        }//while
          //add the time focused DataSet to time focused results
          tfgr.addItem(hist_ds);
        }//if
        else //something went wrong in previous form
        {
          SharedData.addmsg("Encountered non-DataSet.\n");
          return false;
        }       
        
      }//for( num_ds )
      
      tfgr.setValid(true);
      SharedData.addmsg("Finished time focusing and grouping DataSets.\n\n");
      return true;
    }
    //broke, need to return false to let the wizard know
    else
    {
      SharedData.addmsg("No histograms selected.\n");
      return false;
    }

  }
  
  private Object getEditableParamValue(IParameterGUI param)
  {
    Object obj;
    if( param.getName().equals( 
      wizard.getParameter( editable_params[0] ).getName() ) )
    {     
      String focusing_GIDs;
      //get the user input parameters
      focusing_GIDs = ((IntArrayPG)param).getStringValue();
      if( focusing_GIDs instanceof String )
      {
        //set the new group ID
        if( focusing_GIDs.length() > 0 )
          new_GID = new Integer(focusing_GIDs.substring(0,1)).intValue();
        else 
          new_GID = 1;
          
        param.setValid(true);
        return focusing_GIDs;
      }
      else
      {
        new_GID = 1;
        param.setValid(true);
        return new String("1");
      } 
    }
    else if( param.getName().equals( 
      wizard.getParameter( editable_params[1] ).getName() ) )
    {
      Float angle;
      obj = param.getValue();
      if( obj instanceof Float)
      {
        angle = ((Float)obj);
        param.setValid(true);
        return angle;
      }
      else
      {
        param.setValid(true);
        return new Float(1.0f);
      }
    }
    else if( param.getName().equals( 
      wizard.getParameter( editable_params[2] ).getName() ) )
    {
      Float path;
      obj = param.getValue();
      if( obj instanceof Float)
      {
        path = ((Float)obj);
        param.setValid(true);
        return path;
      }
      else
      {
        param.setValid(true);
        return new Float(1.0f);
      }
    }
    else
      return null;
  } 

}
