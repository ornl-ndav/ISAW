/*
 * File:  ParameterInfo.java 
 *             
 * Copyright (C) 2005, Ruth Mikkelson
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
 * Revision 1.3  2005/06/14 18:45:34  rmikk
 * Returned "" in place of null
 *
 * Revision 1.2  2005/06/14 14:25:49  rmikk
 * Fixed some initialization errors
 *
 * Revision 1.1  2005/06/06 14:07:37  rmikk
 * Initial Checking
 *
 */

package DataSetTools.parameter;


//import java.util.*;

import Command.ParameterClassList;
import DataSetTools.components.ParametersGUI.*;

import java.awt.*;
/**
 * @author mikkelsonr
 * This class is a utility class that can give information about ParameterGUI's
 * that can be used by programs for tooltips and other information about
 * the data types needed and the GUI's that are used to elicit input from users.
 */
public class ParameterInfo {

    static ParameterClassList Params ;
    static String[] Type ={
      "Array",
      "Boolean",
      //"Browse",
      "ChoiceList",
      //"Chooser",
      "DataDir",
      "DataSet",
      "FloatArrayArray",
      "FloatArray",
      "Float",
      "FuncString",
      //"Hash",
      "InstName",
      "IntArray",
      "IntegerArray",
      "Integer",
      //"IXScale",
      "LoadFileArray",
      "LoadFile",
      "Material",
      "MonitorDataSet",
      "PlaceHolder",
      "PrinterName",
      "PulseHeightDataSet",
      "Qbins1",
      "Qbins",
      "RadioButton",
      "RealArray",
      "SampleDataSet",
      "SaveFile",
      "StringArray",
      //"StringEntry",
      "String",
      "UniformXScale",
      "VariableXScale"
    };
    static Class[]   parms={
      
      ArrayPG.class,
      BooleanPG.class,
      //BrowsePG.class,
      ChoiceListPG.class,
      //ChooserPG.class,
      DataDirPG.class,
      DataSetPG.class,
      FloatArrayArrayPG.class,
      FloatArrayPG.class,
      FloatPG.class,
      FuncStringPG.class,
      //HashPG.class,
      InstNamePG.class,
      IntArrayPG.class,
      IntegerArrayPG.class,
      IntegerPG.class,
      //IXScalePG.class,
      LoadFileArrayPG.class,
      LoadFilePG.class,
      MaterialPG.class,
      MonitorDataSetPG.class,
      PlaceHolderPG.class,
      PrinterNamePG.class,
      PulseHeightDataSetPG.class,
      Qbins1PG.class,
      QbinsPG.class,
      RadioButtonPG.class,
      RealArrayPG.class,
      SampleDataSetPG.class,
      SaveFilePG.class,
      StringArrayPG.class,
      //StringEntryPG.class,
      StringPG.class,
      UniformXScalePG.class,
      VariableXScalePG.class,
     
    };
    static String[] Classes = {
      "Vector",//ArrayPG
      "Boolean",
       //BrowsePG
      "String",//ChoiceListPG.java
      //"String",//ChooserPG.java
      "String",//DataDirPG.java
      "DataSet",//DataSetPG.java
      "Vector of Vector of Float",//FloatArrayArrayPG.java
      "Vector of Float",//FloatArrayPG.java
      "Float",//FloatPG.java
      "String",//FuncStringPG.java
      //"null",//HashPG.java
      "String",//InstNamePG.java
      "String form of list of int",//IntArrayPG.java
      "Vector of Integer",//IntegerArrayPG.java
      "Integer",//IntegerPG.java
      //IXScalePG.java
      "String[]",//LoadFileArrayPG.java
      "String",//LoadFilePG.java
      "String",//MaterialPG.java
      "MonitorDataSet",//MonitorDataSetPG.java
      "Object",//PlaceHolderPG.java
      "String",//PrinterNamePG.java
      "DataSet",//PulseHeightDataSetPG.jav
      "Vector of bin boundaries",//Qbins1PG.java
      "Vector of bin boundaries",//QbinsPG.java
      "String",//RadioButtonPG.java
      "int[],int[][],.. or float[], float[][], float[][][], String",//RealArrayPG.java
      "SampleDataSet",//SampleDataSetPG.java
      "String",//SaveFilePG.java
      "String[]",//StringArrayPG.java
      //StringEntryPG.java
      "String",//StringPG.java
      "Vector of bin boundaries",//UniformXScalePG.java
      "Vector of bin boundaries",//VariableXScalePG.java
      
    };
    static boolean[] UsesStringInput={
      
      
      true,//ArrayPG.class,
      true,//BooleanPG.class,
      //true,//BrowsePG.class,
      true,//ChoiceListPG.class,
      //false,//ChooserPG.class,
      true,//DataDirPG.class,
      false,//DataSetPG.class,
      true,//FloatArrayArrayPG.class,
      true,//FloatArrayPG.class,
      true,//FloatPG.class,
      true,//FuncStringPG.class,
      //false,//HashPG.class,
      true,//InstNamePG.class,
      true,//IntArrayPG.class,
      true,//IntegerArrayPG.class,
      true,//IntegerPG.class,
      //false,//IXScalePG.class,
      true,//LoadFileArrayPG.class,
      true,//LoadFilePG.class,
      true,//MaterialPG.class,
      false,//MonitorDataSetPG.class,
      false,//PlaceHolderPG.class,
      false,//PrinterNamePG.class,
      false,//PulseHeightDataSetPG.class,
      false,//Qbins1PG.class,
      false,//QbinsPG.class,
      true,//RadioButtonPG.class,
      true,//RealArrayPG.class,
      false,//SampleDataSetPG.class,
      true,//SaveFilePG.class,
      true,//StringArrayPG.class,
     //true,//StringEntryPG.class,
      true,//StringPG.class,
      false,//UniformXScalePG.class,
      false,//VariableXScalePG.class,;
    };
    static String[] GUIelements={
 
      "JTextField",//ArrayPG.class,
      "JCheckBox",//BooleanPG.class,
      //"JFileBrowser",//BrowsePG.class,
      "JComboBox",//ChoiceListPG.class,
      //"JComboBox",//ChooserPG.class,
      "JFileChooser",//DataDirPG.class,
      "JComboBox",//DataSetPG.class,
      "JTextField,JList,JButtons",//FloatArrayArrayPG.class,
      "JTextField,JList,JButtons",//FloatArrayPG.class,
      "JTextField",//FloatPG.class,
      "JTextField",//FuncStringPG.class,
      //"",//HashPG.class,
      "JTextField",//InstNamePG.class,
      "JTextField",//IntArrayPG.class,
      "JTextField,JList,JButtons",//IntegerArrayPG.class,
      "JTextField",//IntegerPG.class,
      //"",//IXScalePG.class,
      "JFileBrowser,JList,JButtons",//LoadFileArrayPG.class,
      "JFileChhoser",//LoadFilePG.class,
      "JTextField",//MaterialPG.class,
      "JComboBox",//MonitorDataSetPG.class,
      "JLabel",//PlaceHolderPG.class,
      "JComboBox",//PrinterNamePG.class,
      "JComboBox",//PulseHeightDataSetPG.class,
      "JTextFields,JCheckBox,JButtons",//Qbins1PG.class,
      "JTextFields,JCheckBox,JList,JButtons",//QbinsPG.class,
      "JRadioButtons",//RadioButtonPG.class,
      "JTextField",//RealArrayPG.class,
      "JComboBox",//SampleDataSetPG.class,
      "JFileChooser",//SaveFilePG.class,
      "JTextField,JList,JButtons",//StringArrayPG.class,
      //"JTextField",//StringEntryPG.class,
      "JTextField",//StringPG.class,
      "JTextFields",//UniformXScalePG.class,
      "JTextField",//VariableXScalePG.class,
     
    }
    ;
    static String[] OtherInfo={
      "Entry :[1,3,\"abc\",[2,4]]",//ArrayPG.class,
      "",//BooleanPG.class,
     //"",//BrowsePG.class,
      "",//ChoiceListPG.class,
      //"",//ChooserPG.class,
      "",//DataDirPG.class,
      "",//DataSetPG.class,
      "Press Done,Enter Float then addItem.For medium sized lists",//FloatArrayArrayPG.class,
      "Press Enter Float, then enter data then return(addItem).\n For medium sized lists",//FloatArrayPG.class,
      "",//FloatPG.class,
      "",//FuncStringPG.class,
      //"",//HashPG.class,
      "",//InstNamePG.class,
      "This is the old intList, the string form for a set of integers",//IntArrayPG.class,
      "Press Enter Integer, then enter data then return(addItem).\nFor medium sized lists",//IntegerArrayPG.class,
      "",//IntegerPG.class,
      //"",//IXScalePG.class,
      "Press Enter File to Load, then keep selecting files with Browser \nthen addItem(or return).For medium sized lists",
                                     //LoadFileArrayPG.class,
      "",//LoadFilePG.class,
      "",//MaterialPG.class,
      "",//MonitorDataSetPG.class,
      "Cannot change. Holds arbitrary Object. Should not use in ISAW scripts",//PlaceHolderPG.class,
      "",//PrinterNamePG.class,
      "",//PulseHeightDataSetPG.class,
      "Has options for creating either uniform or logarithmic bin boundaries",//Qbins1PG.class,
      "Has options for creating uniform,logarithmic and combo bin boundaries",//QbinsPG.class,
      "",//RadioButtonPG.class,
      "InitValues: new int[0][0][0]. This sets the data type for this PG.\n DO NOT USE IN ISAW SCRIPTS",//RealArrayPG.class,
      "",//SampleDataSetPG.class,
      "",//SaveFilePG.class,
      "Press Enter String, then enter data then return(addItem).\nFor medium sized lists",//StringArrayPG.class,
      "",//StringEntryPG.class,
      "",//StringPG.class,
      "Enter first last and nsteps",//UniformXScalePG.class,
      "Entry:[1,3,5,7]"//VariableXScalePG.class,
     
    };
    
	/**
	 * 
	 */
	public ParameterInfo() {
        Params=new ParameterClassList();
        /*ParameterClassList plist=new ParameterClassList();
        Params=plist.paramList;
        
        int n= Params.size();
        Classes = new Class[n];
        UsesStringInput = new boolean[n];
        GUIelements = new String[n];
        OtherInfo = new String[n];
        Type = new String[n];
        parms = new Class[n];
        int i=0;
        for(Enumeration E = Params.elements(); E.hasMoreElements();i++){
           Object O =(E.nextElement());
           parms[i]= (Class)O;
           
           OtherInfo[i]="";
           if( parms[i].equals( RealArrayPG.class)){
             OtherInfo[i]="Large 1..nD arrays. Init Value determines Data Type\n";
             OtherInfo[i]+="Passed by Reference if no user entry used.";
           }else if( parms[i].equals(PlaceHolderPG.class)){
             OtherInfo[i]=" Object holder. do not use in Scripts unless the Object is";
             OtherInfo[i]+=" a data type supported by ISAW Scripting Language"; 
           }
           IParameter param = plist.getInstance( Classes[i], false);
           Classes[i]=param.getValue().getClass();
           if( param instanceof ParamUsesString)
             UsesStringInput[i] = true;
           else 
             UsesStringInput[i] = false;
           Type[i] = param.getType();
           ParameterGUI parmG =(ParameterGUI)param;
           if( param instanceof VectorPG)
              GUIelements[i]= "JList to edit/manage for med arrays";
           else if(parmG instanceof RealArrayPG){
              GUIelements[i]="JTextField";
           }else{
             
             parmG.initGUI( null );
             EntryWidget Wid = parmG.getEntryWidget();
             GUIelements[i]="";
             GUIelements[i]= SetUpGUIElements( Wid );
           
           }
          
        }*/
    }
        
        private String SetUpGUIElements( Component Wid){
            if( Wid == null) return"";
            if(!(Wid instanceof Container))
               return "";
            if( ((Container)Wid).countComponents()<=0){
                return Class2String( Wid.getClass(), true);
            }
            if( Wid.getClass().equals( ArrayEntryJFrame.class ))
               return "Med Array Entry";
            if( Wid instanceof javax.swing.JLabel)
               return "";
            String S="";
            for(int i = 0; i< ((Container)Wid).countComponents(); i++){
                Component C = ((Container)Wid).getComponent( i );
                if( !(C instanceof javax.swing.JLabel))
                   S+= SetUpGUIElements( C)+";";
            }
            return S;
        }
        
        private String Class2String( Class C, boolean elimLeadPack){
          String S = C.toString();
          if( S.startsWith("class "))
             S = S.substring( 6).trim();
          if( S.startsWith("L[")){//  Array
            int i,c=0;
            for( i=1; (i<S.length())&&(S.charAt(i)=='[');){i++;}
            String lparen="",rparen="";
            if( i<S.length()) if( S.charAt(i)=='L'){
                lparen ="(";
                rparen =")";
            }
            try{
            String Res = lparen+ Class2String( Class.forName( S.substring(c+1)),elimLeadPack)+rparen;
            for( int j=0;j<i;j++)Res+="[]";
            return Res;
            }catch(Exception s){
               return "";
            }
          }
         if(!elimLeadPack)
            return S;
         int j = S.lastIndexOf('.');
         if( j <0)
            return S;
         return S.substring(j+1);
          
        }

	public static void main(String[] args) {
      ParameterInfo Pinf = new ParameterInfo();
      for(int i=0;i<Pinf.Type.length-1; i++){
        System.out.println("Name="+Type[i]+"     Returns "+Classes[i]);
        System.out.println("GUI Elements="+GUIelements[i]+"   uses String input= "+
                        UsesStringInput[i]);
        if( OtherInfo[i].length()>0)
          System.out.println("Other info="+OtherInfo[i]);
        System.out.println("========================================");                
        
      }
  }
  

  public static  int getNParamTypes(){
     return Type.length;
  }
  public static String getToolTip( int i , boolean oneLine){
    String Res = "Name="+ Type[i]+"   Returns "+Classes[i];
    if( oneLine)
       return Res;
    Res ="<html>"+Res+"<BR>GUI Elements= "+GUIelements[i]+"<BR>";
    if( OtherInfo[i].length()>0)
      Res+= "Other Info="+OtherInfo[i];
    Res +="</html>";
    return Res;
  }
    
  public static String getType( int i){
       return Type[i];
  }
  
  public static String getValueClass(int i){
       return Classes[i];
  }
  
  public static boolean isEqual( int i, Class val){
     IParameter p = Params.getInstance( Type[i]);
     if(p == null){
      
       return false;
     }
     if( val == null)
       return false;
    if( Type[i].equals("RealArray")){
        if( !val.isArray()) return false;
        Class v=val;
        for( v = val; v.getComponentType().isArray();
             v=v.getComponentType()){}
        if( v .equals( String.class))
           return true;
        if(v.equals(float.class))
           return true;
        if(v .equals( int.class))
           return true;
        if(v .equals( long.class))
           return true;
        if(v .equals( short.class))
           return true;
        if(v .equals(byte.class))
           return true;
        if(v .equals( double.class))
           return true;
        return false;
        
       
     }else if( Type[i].equals("PlaceHolder"))
        return true;
     Object O = p.getValue();
     if( O== null)
        System.out.println("parm w null getValue  for "+ Type[i]);
     if( O != null)
    
        if( O.getClass().equals(val))
           return true;
        else if( val.isPrimitive())
            if( O instanceof Boolean)
               if (val.equals(boolean.class))
                  return true;
               else
                  return false;
             else if( !(O instanceof Number))
                  return false;
             else if(O instanceof Float)
                 if(val.equals(float.class))
                    return true;
                 else 
                    return false;

            else if(O instanceof Integer)
               if(val.equals(int.class))
                return true;
               else 
                 return false;
                     
     
     
     return false;
  }
 

}
