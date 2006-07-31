/*
 * File: ContourMenuHandler.java
 *
 * Copyright (C) 2005, Dominic Kramer
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
 * Primary   Dominic Kramer <kramerd@uwstout.edu>
 * Contact:  Student Developer, University of Wisconsin-Stout
 *           
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log: ContourMenuHandler.java,v $
 * Revision 1.4  2005/10/07 21:32:36  kramer
 *
 * Added javadoc comments for every field, constructor, method, inner class,
 * etc. in this class.
 *
 * Revision 1.3  2005/07/29 15:32:52  kramer
 *
 * Now when the user selects to use a solid/colorscale to color the contour
 * lines, the controls for the ControlColorScale are disabled/enabled.
 *
 * Revision 1.2  2005/07/28 15:36:40  kramer
 *
 * Modified this class's listeners to invoke setter methods instead of
 * directly 'setting values'.  Also, the setter methods have been modified
 * to update the graphical elements when a value is set on the element.
 *
 * Revision 1.1  2005/07/25 20:52:58  kramer
 *
 * Initial checkin.  This is a module of the ContourViewComponent that is
 * responsible for handling the ViewMenus that are located on the component.
 *
 */
package gov.anl.ipns.ViewTools.Components.TwoD.Contour;

import gov.anl.ipns.Util.Messaging.Information.InformationCenter;
import gov.anl.ipns.Util.Messaging.Information.InformationHandler;
import gov.anl.ipns.Util.Messaging.Property.PropertyChangeConnector;
import gov.anl.ipns.Util.Sys.ColorSelector;
import gov.anl.ipns.ViewTools.Components.IVirtualArray2D;
import gov.anl.ipns.ViewTools.Components.ObjectState;
import gov.anl.ipns.ViewTools.Components.Menu.MenuItemMaker;
import gov.anl.ipns.ViewTools.Components.Menu.ViewMenuItem;
import gov.anl.ipns.ViewTools.Components.ViewControls.ColorControl;
import gov.anl.ipns.ViewTools.Panels.Contour.ContourJPanel;
import gov.anl.ipns.ViewTools.Panels.Image.IndexColorMaker;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

/**
 * This is a module of a <code>ContourViewComponent</code> that is 
 * responsible for mainting the menu items associated with the 
 * view component.  That is, this class maintains the state 
 * information for the menu items and listens to other modules of 
 * the view component to keep the state of the menu items 
 * synchronized with the other modules.
 */
public class ContourMenuHandler extends ContourChangeHandler 
                                   implements InformationHandler
{
//--------------------=[ InformationCenter keys ]=----------------------------//
   /**
    * "Is double sided info key" - This static constant String is a key 
    * used with a {@link InformationCenter InformationCenter} to 
    * reference whether the user has specified that the color scale 
    * should be double sided.
    */
   public static final String IS_DOUBLE_SIDED_INFO_KEY = 
                                  "Is double sided info key";
   
   /**
    * "Color info key" - This static constant String is a key used with 
    * a {@link InformationCenter InformationCenter} to reference the 
    * line color of the contour plot.
    */
   public static final String COLOR_INFO_KEY = 
                                  "Color info key";
   
   /**
    * "Preserve aspect ratio info key" - This static constant String is 
    * a key used with a {@link InformationCenter InformationCenter}  to 
    * reference whether the aspect ratio should be preserved when 
    * rendering the contour plot.
    */
   public static final String PRESERVE_ASPECT_RATIO_INFO_KEY = 
                                  "Preserve aspect ratio info key";
//--------------------=[ InformationCenter keys ]=----------------------------//
   
   
//------------------------=[ ObjectState keys ]=------------------------------//
   /**
    * "Is double sided key" - This static constant String is a key used for 
    * referencing whether or not this component should use a double-sided 
    * named colorscale when it uses a colorscale to determine the color to 
    * render the contour lines with.  The value that this key references is 
    * a <code>Boolean</code> object.
    */
   public static final String IS_DOUBLE_SIDED_KEY = "Is double sided key";
   
   /**
    * "Aspect ratio key" - This static constant String is a key used for 
    * referencing whether or not the aspect ratio is currently preserved for 
    * this component.  The value that this key references is a 
    * <code>Boolean</code>.
    */
   public static final String ASPECT_RATIO_KEY = "Aspect ratio key";
   
   /**
    * "Solid color key" - This static constant String is a key used for 
    * referencing the state information about the <code>ColorControl</code> 
    * that is used to make the contour lines colored with a solid color.  
    * The value that this key references is an <code>ObjectState</code> of 
    * a <code>ColorControl</code> object.
    */
   public static final String SOLID_COLOR_KEY = "Solid color key";
//----------------------=[ End ObjectState keys ]=----------------------------//
   
   
//--------------------=[ Default field values ]=------------------------------//
   /**
    * Specifies if the aspect ratio will be preserved when displaying the 
    * contour plot by default.  The value of this field is the same as the 
    * value of the field 
    * {@link ContourJPanel#DEFAULT_PRESERVE_ASPECT_RATIO 
    * ContourJPanel.DEFAULT_PRESERvE_ASPEcT_RATIO}.
    */
   public static final boolean DEFAULT_PRESERVE_ASPECT_RATIO = 
      ContourJPanel.DEFAULT_PRESERVE_ASPECT_RATIO;
   
   /**
    * Specifies if the named colorscales used to determine the colors to 
    * render the contour lines with should be a double-sided contour scale 
    * by default.  The value of this field is <code>false</code>.
    */
   public static final boolean DEFAULT_IS_DOUBLE_SIDED = false;
   
   /**
    * Specifies the default solid color that the contour lines will be 
    * rendered using.  If the field {@link ContourJPanel#DEFAULT_COLOR_SCALE 
    * ContourJPanel.DEFAULT_COLOR_SCALE} has at least one element, this 
    * field's value is the value of that element.  Otherwise, this field's 
    * value is <code>Color.BLACK</code>
    */
   public static final Color DEFAULT_LINE_COLOR;
      static
      {
         Color[] arr = ContourJPanel.DEFAULT_COLOR_SCALE;
         if (arr.length<1)
            DEFAULT_LINE_COLOR = Color.BLACK;
         else
            DEFAULT_LINE_COLOR = arr[0];
      }
//------------------=[ End default field values ]=----------------------------//
      
      
//-----------------------------=[ Fields ]=-----------------------------------//
   /**
    * This is the array of menus items that this class maintains, listens 
    * to, and synchronizes.
    */
   private ViewMenuItem[] menuItems;
   
   /**
    * This is the menu item that is used to specify if the contour plot 
    * should be rendered with its aspect ratio preserved or not.
    */
   private JCheckBoxMenuItem aspectRatioItem;
   
   /**
    * This is the menu item that is used to specify the solid color to 
    * use when coloring the contour lines.
    */
   private ColorControl contourColorItem;
   
   /**
    * This is the menu item that is used to specify if the named color scale 
    * used to color the contour lines should be double-sided or not.
    */
   private JCheckBoxMenuItem isDoubleSidedItem;
   
   /**
    * This is the menu that contains all of the menu items that 
    * are used to specify the colorscale to use when rendering 
    * a contour plot.
    */
   private JMenu colorscaleControlMenu;
//---------------------------=[ End fields ]=---------------------------------//
   
   
//--------------------------=[ Constructors ]=--------------------------------//
   /**
    * Constructs a module for a 
    * {@link ContourViewComponent ContourViewComponent} that handles 
    * working with the view component's menu items.
    * 
    * @param connector      Serves to connect several modules of a 
    *                         {@link ContourViewComponent 
    *                         ContourViewComponent} so that if a 
    *                         property in one module is changed, the 
    *                         other modules are notified.
    * @param center         Serves as the central location where the 
    *                         data shared between several modules of a 
    *                         {@link ContourViewComponent 
    *                         ContourViewComponent} is stored.
    * @param panel          The panel that is responsible for 
    *                         rendering the contour plot.
    * @param useColorScale  If <code>true</code>, the colorscale 
    *                         specified by the parameter 
    *                         <code>colorscale</code> will be used 
    *                         to color the contour plot.  
    *                         If <code>false</code>, the color 
    *                         specified by the parameter 
    *                         <code>lineColor</code> will be used 
    *                         to color the contour plot.
    */
   public ContourMenuHandler(PropertyChangeConnector connector, 
                             InformationCenter center, 
                             ContourJPanel panel, boolean useColorScale)
   {
      this(connector, center, panel, 
           DEFAULT_LINE_COLOR, ContourControlHandler.DEFAULT_COLOR_SCALE, 
           DEFAULT_IS_DOUBLE_SIDED, useColorScale, 
           DEFAULT_PRESERVE_ASPECT_RATIO);
   }
   
   /**
    * Constructs a module for a 
    * {@link ContourViewComponent ContourViewComponent} that handles 
    * working with the view component's menu items.
    * 
    * @param connector            Serves to connect several modules of a 
    *                               {@link ContourViewComponent 
    *                               ContourViewComponent} so that if a 
    *                               property in one module is changed, the 
    *                               other modules are notified.
    * @param center               Serves as the central location where the 
    *                               data shared between several modules of a 
    *                               {@link ContourViewComponent 
    *                               ContourViewComponent} is stored.
    * @param panel                The panel that is responsible for 
    *                               rendering the contour plot.
    * @param lineColor            Specifies the initial line color that 
    *                               should be set in the menu items and 
    *                               used to render the contour lines.
    * @param colorScale           Specifies the initial colorscale that 
    *                               should be set in the menu items and 
    *                               used to render the contour lines.
    * @param isDoubleSided        If <code>true</code> the colorscale 
    *                               specified by the parameter 
    *                               <code>colorscale</code> will be a 
    *                               double-sided colorscale.  
    *                               If <code>false</code>, it it won't.
    * @param useColorScale        If <code>true</code>, the colorscale 
    *                               specified by the parameter 
    *                               <code>colorscale</code> will be used 
    *                               to color the contour plot.  
    *                               If <code>false</code>, the color 
    *                               specified by the parameter 
    *                               <code>lineColor</code> will be used 
    *                               to color the contour plot.
    * @param preserveAspectRatio  If <code>true</code>, the contour plot 
    *                               will be rendered with its aspect ratio 
    *                               preserved.  If <code>false</code>, its 
    *                               apsect ratio will not be preserved.
    */
   public ContourMenuHandler(PropertyChangeConnector connector, 
                             InformationCenter center, 
                             ContourJPanel panel, 
                             Color lineColor, String colorScale, 
                             boolean isDoubleSided, boolean useColorScale, 
                             boolean preserveAspectRatio)
   {
      super(connector, center, panel);
      
      //now to tell the info center which data this class will maintain
      getInfoCenter().registerHandler(this, IS_DOUBLE_SIDED_INFO_KEY);
      getInfoCenter().registerHandler(this, COLOR_INFO_KEY);
      getInfoCenter().registerHandler(this, PRESERVE_ASPECT_RATIO_INFO_KEY);
      
      //now to connect to the PropertyChangeConnector
      getPropertyConnector().addHandler(this);
      
      //make the menu items that allow the location of the colorscale to 
      //be specified
        //this will listen to when the item are selected
        MoveColorScaleListener locationListener = new MoveColorScaleListener();
        JMenuItem controlPanelItem = new JMenuItem(CONTROL_PANEL_LOCATION);
          controlPanelItem.addActionListener(locationListener);
        JMenuItem belowItem = new JMenuItem(BELOW_IMAGE_LOCATION);
          belowItem.addActionListener(locationListener);
        JMenuItem rightItem = new JMenuItem(RIGHT_IMAGE_LOCATION);
          rightItem.addActionListener(locationListener);
        JMenuItem noneItem = new JMenuItem(NONE_LOCATION);
          noneItem.addActionListener(locationListener);
      
      colorscaleControlMenu = new JMenu("Display Position");
        colorscaleControlMenu.add(controlPanelItem);
        colorscaleControlMenu.add(belowItem);
        colorscaleControlMenu.add(rightItem);
        colorscaleControlMenu.add(noneItem);
      
      //make the controls for the line colors
      //first make the controls to select if the scale should be double sided
      isDoubleSidedItem = 
        new JCheckBoxMenuItem("Is Double Sided");
      isDoubleSidedItem.setSelected(isDoubleSided);
      final ActionListener doubleSidedListener = new ActionListener()
      {
         /**
          * Used to store the color scale.  If the user selects to 
          * have the color scale double sided, this class will be able 
          * to remember which color scale to make/not make double 
          * sided.
          */
         private String colorScale = 
            IndexColorMaker.HEATED_OBJECT_SCALE_2;
       
         /**
          * Invoked when the user selects a different color scale from the 
          * menu of built-in color scales, or when the user checks the 
          * JCheckboxMenuitem to make the colorscale double sided or not.
          */
         public void actionPerformed(ActionEvent event)
         {
            if ( !(event.getSource() instanceof JCheckBoxMenuItem) )
            {
               //then a menuitem was selected that specifies one of the 
               //built-in colorscales and getActionCommand() returns 
               //the String that identifies the colorscale
               colorScale = event.getActionCommand();
            }
            else
               setIsDoubleSidedColorScale(getIsDoubleSidedColorScale());
           
            colorScaleNameChanged(colorScale);
         }
      };
      isDoubleSidedItem.addActionListener(doubleSidedListener);
    
      //second make the menu that contains the built-in colorscales
      
      //make the listener that will listen to when the user selects a 
      //new built-in colorscale
      ActionListener colorListener = new ActionListener()
      {
         /**
          * Invoked when the user selects an item from the menu that 
          * contains all of the built-in colorscales.
          */
         public void actionPerformed(ActionEvent event)
         {
            doubleSidedListener.actionPerformed(event);
         }
      };
      
      //make the menu that will list the built-in colorscales
      ViewMenuItem colorscaleMenu = 
         new ViewMenuItem(ViewMenuItem.PUT_IN_OPTIONS, 
                          MenuItemMaker.getColorScaleMenu(colorListener));
     
      //make the item that will allow one color to be selected for the 
      //contour lines
      contourColorItem = new ColorControl("", " Solid Color ", 
                                          DEFAULT_LINE_COLOR,
                                          ColorSelector.TABBED);
      contourColorItem.setSelectedColor(lineColor);
      contourColorItem.setBorderVisible(false);
      contourColorItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            if (event.getActionCommand().equals(ColorControl.COLOR_CHANGED))
            {
               setLineColor(contourColorItem.getSelectedColor());
            }
         }
      });
     
      //make sure that the right color or color scale is applied to the 
      //ContourJPanel and ControlColorScale
      if (useColorScale)
         colorScaleNameChanged(colorScale);
      else
         colorChanged(lineColor);
      
      //make the menu that will hold the two items made
      JMenu lineColorMenu = new JMenu("Line Colors");
        lineColorMenu.add(contourColorItem);
        lineColorMenu.add(new JSeparator());
        lineColorMenu.add(colorscaleMenu.getItem());
        lineColorMenu.add(isDoubleSidedItem);
        lineColorMenu.add(colorscaleControlMenu);
     
    //create the array of menu items
    menuItems = new ViewMenuItem[2];
      menuItems[0] = new ViewMenuItem(ViewMenuItem.PUT_IN_OPTIONS, 
                                     lineColorMenu);
    //now make the controls for preserving the aspect ratio 
      menuItems[1] = 
         new ViewMenuItem(ViewMenuItem.PUT_IN_OPTIONS, 
                          generateAspectRatioMenuItem(preserveAspectRatio));
   }
//------------------------=[ End constructors ]=------------------------------//
   
   
//--------=[ Methods implemented for the ContourChangeHandler class ]=--------//
   /**
    * This method is implemented for the <code>ContourChangeHandler</code> 
    * class.  However, it does nothing.  If the contour data is changed, 
    * this class does not have to change any of the menu items.
    * 
    * @param v2d The new data that is going to be plotted.
    * 
    * @see ContourChangeHandler#reinit(IVirtualArray2D)
    */
   public void reinit(IVirtualArray2D v2d)
   {
      //nothing has to be done if a new virtual array is given
   }
   
   /**
    * This method is implemented for the <code>ContourChangeHandler</code> 
    * class.  If the colorscale being used to color the contour plot 
    * changes, this method is invoked.  When it is invoked, the menu 
    * item is enabled to allow the colorscale to be 
    * specified/unspecified as a "double-sided" colorscale.  In addition, 
    * the menu that contains all of the possible colorscales is enabled.
    * 
    * @param colorscale The name of the colorscale that will now be 
    *                   used to color the contour plot.
    * 
    * @see ContourChangeHandler#changeColorScaleName(String)
    */
   public void changeColorScaleName(String colorscale)
   {
      isDoubleSidedItem.setEnabled(true);
      this.colorscaleControlMenu.setEnabled(true);
   }

   /**
    * This method is implemented for the <code>ContourChangeHandler</code> 
    * class.  If the color being used to color the contour plot changes, 
    * this method is invoked.  When it is invoked, the menu item that 
    * allows the current colorscale to be specified/unspecified as a 
    * "double-sided" colorscale is disabled.  In addition, the menu that 
    * contains all of the possible colorscales is disabled.  This is 
    * because if a solid color is being used to color the contour plot, 
    * these options are not needed.
    * 
    * @param color The color that is now going to be used to color the 
    *              contour plot.
    * 
    * @see ContourChangeHandler#changeColor(Color)
    */
   public void changeColor(Color color)
   {
      isDoubleSidedItem.setEnabled(false);
      this.colorscaleControlMenu.setEnabled(false);
   }

   /**
    * This method is implemented for the <code>ContourChangeHandler</code> 
    * class.  If the colorscale being used to color the contour plot is 
    * modified to be/not be "double-sided", this method is invoked.  
    * When invoked, this method modifies the checkbox in the menus to 
    * reflect the new state of the "double-sidedness" of the colorscale.
    * 
    * @param isDoubleSided <code>True</code> if the colorscale used to 
    *                      color the contour plot is now double-sided and 
    *                      <code>false</code> if it isn't.
    * 
    * @see ContourChangeHandler#changeIsDoubleSided(boolean)
    */
   public void changeIsDoubleSided(boolean isDoubleSided)
   {
      if (isDoubleSidedItem==null)
         return;
      
      isDoubleSidedItem.setSelected(isDoubleSided);
   }
//------=[ End methods implemented for the ContourChangeHandler class ]=------//
   
   
//------=[ Methods implemented for the InformationHandler interface ]=--------//
   /**
    * This method is implemented for the 
    * {@link InformationHandler InformationHandler} interface.  Given a 
    * certain string alias, the data referenced by that alias will be 
    * returned.
    * 
    * @param key The string alias for some particular data.
    * @return    The data associated with the given string or 
    *            <code>null</code> if <code>key</code> is not 
    *            understood by this class.
    * 
    * @see InformationHandler#getValue(String)
    * 
    * @see #IS_DOUBLE_SIDED_INFO_KEY
    * @see #COLOR_INFO_KEY
    * @see #PRESERVE_ASPECT_RATIO_INFO_KEY
    */
   public Object getValue(String key)
   {
      if (key==null)
         return null;
      
      if (key.equals(IS_DOUBLE_SIDED_INFO_KEY))
         return new Boolean(getIsDoubleSidedColorScale());
      else if (key.equals(COLOR_INFO_KEY))
         return getLineColor();
      else if (key.equals(PRESERVE_ASPECT_RATIO_INFO_KEY))
         return new Boolean(getPreserveAspectRatio());
      else
         return null;
   }
//----=[ End methods implemented for the InformationHandler interface ]=------//
   
   
//-----------=[ Methods implemented for the IPreserveState interface ]=-------//
   /**
    * Used to set the state information of this object to match the state 
    * information encapsulated in the <code>ObjectStage</code> parameter 
    * given.
    * 
    * @param state An encapsulation of this Object's state.
    */
   public void setObjectState(ObjectState state)
   {
      if (state==null)
         return;
      
      //set the states for the ViewMenuItems
      Object val = state.get(ASPECT_RATIO_KEY);
      if ( (val != null) && (val instanceof Boolean) )
         setPreserveAspectRatio( ((Boolean)val).booleanValue() );
      
      val = state.get(SOLID_COLOR_KEY);
      if ( (val != null) && (val instanceof ObjectState) )
         contourColorItem.setObjectState((ObjectState)val);
      
      val = state.get(IS_DOUBLE_SIDED_KEY);
      if ( (val != null) && (val instanceof Boolean) )
         setIsDoubleSidedColorScale(((Boolean)val).booleanValue());
   }

   /**
    * Used to get an encapsulation of this Object's state information.
    * 
    * @param is_default If <code>true</code>, this Object's default state 
    *                   is returned.  Otherwise, its current state is 
    *                   returned.
    * 
    * @return An encapsulation of this Object's state.
    */
   public ObjectState getObjectState(boolean is_default)
   {
      ObjectState state = new ObjectState();
        //store if the aspect ratio is preserved
        boolean useAspectRatio = DEFAULT_PRESERVE_ASPECT_RATIO;
        if (!is_default)
           useAspectRatio = aspectRatioItem.isSelected();
        state.insert(ASPECT_RATIO_KEY, new Boolean(useAspectRatio));
        
      //store state of the control which controls the solid line 
      //color of the contour lines
        state.insert(SOLID_COLOR_KEY, 
                     contourColorItem.getObjectState(is_default));
        
      //store if the colorscale is double sided
        boolean isDoubleSided = DEFAULT_IS_DOUBLE_SIDED;
        if (!is_default)
           isDoubleSided = isDoubleSidedItem.isSelected();
        state.insert(IS_DOUBLE_SIDED_KEY, new Boolean(isDoubleSided));
        
      return state;
   }
//---------=[ End methods implemented for the IPreserveState interface ]=-----//
   
   
//-------------=[ Getter/setter methods for the menu items ]=-----------------//
   /**
    * Used to get the array of menu items that this class maintains, 
    * listens to, and synchronizes.
    * 
    * @return This class's menu items.
    */
   public ViewMenuItem[] getMenuItems()
   {
      return menuItems;
   }
   
   /**
    * Used to determine if this class's menu items reflect 
    * that contour plot colorscale should be double sided 
    * or not.
    * 
    * @return <code>True</code> if the contour plot's 
    *         colorscale should be double sided and 
    *         <code>false</code> if it shouldn't.
    */
   public boolean getIsDoubleSidedColorScale()
   {
      if (isDoubleSidedItem==null)
         return DEFAULT_IS_DOUBLE_SIDED;
      
      return isDoubleSidedItem.isSelected();
   }
   
   /**
    * Used to specify if this class's menu items should reflect 
    * that contour plot colorscale should be double sided 
    * or not.
    * 
    * @param doubleSided <code>True</code> if the contour plot's 
    *                    colorscale should be double sided and 
    *                    <code>false</code> if it shouldn't.
    */
   public void setIsDoubleSidedColorScale(boolean doubleSided)
   {
      isDoubleSidedItem.setSelected(doubleSided);
      isDoubleSidedChanged(doubleSided);
   }
   
   /**
    * Used to determine if this class's menu items 
    * specify if the aspect ratio should be preserved 
    * or not when rendering the contour plot.
    * 
    * @return <code>True</code> if the contour plot should 
    *         have its aspect ratio preserved and 
    *         <code>false</code> if it shouldn't.
    */
   public boolean getPreserveAspectRatio()
   {
      if (aspectRatioItem==null)
         return DEFAULT_PRESERVE_ASPECT_RATIO;
      
      return aspectRatioItem.isSelected();
   }
   
   /**
    * Used to set if this class's menu items should 
    * specify that the contour plot's aspect ratio 
    * should either be preserved or not 
    * (as specified).
    * 
    * @param isSelected If <code>true</code> the 
    *                   contour plot's aspect ratio 
    *                   will be preserved and if 
    *                   <code>false</code>, it 
    *                   won't be.
    */
   public void setPreserveAspectRatio(boolean isSelected)
   {
      //update the menu item to be either selected or deselected
      if (aspectRatioItem != null)
        aspectRatioItem.setSelected(isSelected);
      
      preserveAspectRatioChanged(isSelected);
   }
   
   /**
    * Used to get the color used to render the contour lines 
    * as specified by this class's menu items.
    * 
    * @return The color to use when rendering the contour lines.
    */
   public Color getLineColor()
   {
      if (contourColorItem==null)
         return DEFAULT_LINE_COLOR;
      
      return contourColorItem.getSelectedColor();
   }
   
   /**
    * Used to set the color that should be used when rendering 
    * the contour plot's contour lines.
    * 
    * @param color The line to use when rendering the contour 
    *              lines.
    */
   public void setLineColor(Color color)
   {
      if (contourColorItem == null)
         return;
         
      contourColorItem.setSelectedColor(color);
      colorChanged(color);
   }
//-----------=[ End getter/setter methods for the menu items ]=---------------//
   

//------------=[ Methods used to generate the menu items ]=-------------------//
   /**
    * Used to instantiate and initialize the field 
    * {@link #aspectRatioItem aspectRatioItem}.
    * 
    * @return {@link #aspectRatioItem aspectRatioItem}.  That is, 
    *         after constructing the field, a reference to it is 
    *         returned.
    */
   private JCheckBoxMenuItem generateAspectRatioMenuItem(boolean 
                                                         preserveAspectRatio)
   {
      //make the control for the aspect ratio
      aspectRatioItem = 
         new JCheckBoxMenuItem("Preserve Aspect Ratio");
      aspectRatioItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            setPreserveAspectRatio(aspectRatioItem.isSelected());
         }
      });
      setPreserveAspectRatio(preserveAspectRatio);

      return aspectRatioItem;
   }
//----------=[ End methods used to generate the menu items ]=-----------------//
   
   
//----------------------------=[ Listeners ]=---------------------------------//
   /**
    * This class has several menu items that are used to specify 
    * the location of the control that specifies the color 
    * scale to use when rendering the contour plot.  This class 
    * listens for selections on these menu items.
    */
   public class MoveColorScaleListener implements ActionListener
   {
      /**
       * Invoked when one of the menu items that specify the 
       * location of the control for the colorscale has been 
       * selected.
       * 
       * @param event An encapsulation of information about 
       *              the menu item that has been selected.
       */
      public void actionPerformed(ActionEvent event)
      {
         colorScaleLocationChanged(event.getActionCommand());
      }
   }
//--------------------------=[ End listeners ]=-------------------------------//
}
