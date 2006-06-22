/*
 * File:  Atom.java
 *
 * Copyright (C) 2001, Peter Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.7  2004/03/15 03:28:16  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.6  2003/02/04 16:05:03  pfpeterson
 * Throws an InstantiationError if cannot find element in the database.
 * Also fixed bug with finding the incoherent cross section.
 *
 * Revision 1.5  2003/02/03 22:46:55  pfpeterson
 * Fixed a labeling bug with various cross sections. Small speed
 * and readability improvements.
 *
 * Revision 1.4  2002/11/27 23:15:35  pfpeterson
 * standardized header
 *
 */
package DataSetTools.materials;

import gov.anl.ipns.Util.File.*;
import gov.anl.ipns.Util.Sys.*;

import java.io.*;
import DataSetTools.util.*;

/**
 * Methods for getting information about atoms dependent on the
 * isotope.
 */

public class Atom{
    // instance variables
    private String element            = null;
    private int    isotope            = 0;
    private float  concentration      = Float.NaN;
    private float  abundance          = Float.NaN;
    private float  molarMass          = Float.NaN;
    private float  density            = Float.NaN;
    private float  halflife           = Float.NaN;
    private float  R_b_coh            = Float.NaN;
    private float  I_b_coh            = Float.NaN;
    private float  R_b_inc            = Float.NaN;
    private float  I_b_inc            = Float.NaN;
    private float  xs_coh             = Float.NaN;
    private float  xs_inc             = Float.NaN;
    private float  xs_tot             = Float.NaN;
    private float  xs_abs             = Float.NaN;
    private float  storeT             = Float.NaN;
    private float  promptAct          = Float.NaN;
    private float  contactDose        = Float.NaN;

    // class variables
    private static String isotopefile = null;

    static{
	isotopefile = SharedData.getProperty( "ISAW_HOME" );
	if(isotopefile==null){
	    isotopefile=SharedData.getProperty("user.home");
	    isotopefile = isotopefile+java.io.File.separator+"ISAW";
	}
	isotopefile = isotopefile.trim();
	isotopefile = isotopefile+java.io.File.separator+"Databases";
	isotopefile = isotopefile+java.io.File.separator+"ScattInfo.dat";
	if( new File(isotopefile).exists()){
	    // do nothing
	}else{
	    isotopefile = null;
	}
    }

    /* --------------------Constructor Methods-------------------- */
    /**
     *  Creates an atom and looks up the isotopic information.
     *
     *  @param ele  Element name
     *  @param iso  Isotopic number
     *  @param conc Concentration (for use in materials)
     */
    public Atom( String ele, int iso, float conc ){
	element=ele;
	isotope=iso;
	concentration=conc;
	if(!this.getIsotopeInfo()){
          throw new InstantiationError("Could not find information on: "+ele);
                                       //this.element=null;
	}
    }

    /**
     *  Creates an atom and looks up the isotopic information. The
     *  name of the atom is given as a string such as H(2)_3 for three
     *  deuterated Hydrogen atoms.
     *
     *  @param raw  The atom as specified above
     */
    public Atom( String raw ){
	int index;
	String symbol;

	index=raw.indexOf("_");
	if( index >0 ){
	    symbol=raw.substring(0,index);
	    concentration=Float.parseFloat(raw.substring(index+1));
	}else{
	    symbol=raw;
	    concentration=1.0f;
	}

	index=symbol.indexOf("(");
	if( index>0 ){
	    element=symbol.substring(0,index);
	    isotope=
		Integer.parseInt(symbol.substring(index+1,symbol.length()-1));
	    
	}else{
	    element=symbol;
	}
	element=formatElement(element);
	if(!this.getIsotopeInfo()){
	    this.element=null;
	}
    }

    /**
     *  Obtain the information that depends on the isotope name. If
     *  the atom does not exist in the database then its element name
     *  will be set to null. If the information does not exist in the
     *  file (listed as '-') then the value is set to Float.NaN.
     */
    private boolean getIsotopeInfo(){
	String line = null;
	String symbol = null;
	int index=-1;
	String matchSym = this.element;
	if(this.isotope>0){
	    matchSym=this.isotope+matchSym;
	}
	matchSym=" "+matchSym+" ";

	try{
            boolean found=false;
	    TextFileReader f = new TextFileReader( isotopefile );
	    line=f.read_line();
	    while(line!=null){
		symbol=" "+f.read_String()+" ";
		index=symbol.indexOf(matchSym);
		if(index>=0){
		    //System.out.println(symbol);
		    f.unread();
                    found=true;
		    break;
		}
		line=f.read_line();
	    }
	    line=f.read_line();
	    f.close();
	    if( !found || line==null ){
		return false;
	    }
	    splitIsotopeInfo(line);
	}catch( Exception E){
	    return false;
	}

	return true;
    }

    /**
     *  Takes a line from the isotopic information database and
     *  divides it into the appropriate instance variables.
     */
  private boolean splitIsotopeInfo( String line){
    StringBuffer sb=new StringBuffer(line); // convert to StringBuffer
    StringUtil.trim(sb);                    // for fast access

    StringUtil.getString(sb); // get rid of element/isotope tag

    // the natural abundance of the isotope
    this.abundance=checkNullFloat(sb);
    if(isotope==0) this.abundance=100f;

    // molar mass
    this.molarMass=checkNullFloat(sb);

    // density
    this.density=checkNullFloat(sb);

    // half life
    this.halflife=checkNullFloat(sb);

    // real portion of the coherent scattering length
    this.R_b_coh=checkNullFloat(sb);

    // imaginary portion of the coherent scattering length
    this.I_b_coh=checkNullFloat(sb);

    // real portion of the incoherent scattering length
    this.R_b_inc=checkNullFloat(sb);

    // imaginary portion of the incoherent scattering length
    this.I_b_inc=checkNullFloat(sb);

    // coherent scattering cross section
    this.xs_coh=checkNullFloat(sb);

    // incoherent scattering cross section
    this.xs_inc=checkNullFloat(sb);

    // total scattering cross section
    this.xs_tot=checkNullFloat(sb);

    // scattering absorption cross section
    this.xs_abs=checkNullFloat(sb);

    // time needed to store before no longer 'hot'
    this.storeT=checkNullFloat(sb);

    // prompt activity
    this.promptAct=checkNullFloat(sb);

    // contact dose
    this.contactDose=checkNullFloat(sb);

    return true;
  }

    /**
     *  Check that a String value from the isotope file is not equal
     *  to '-'.
     */
    private static boolean checkNull( String value){
	if(value.length()==1){
	    if(value.indexOf("-")>=0){
		return true;
	    }
	}
	return false;
    }

    /**
     *  Convert the String value from the isotope file into the
     *  appropriate float.
     */
    private static float checkNullFloat( StringBuffer stringbuffer){
        String value=StringUtil.getString(stringbuffer);

	int index=0;
	if(value.indexOf("*")>=0){
	    // the isotope is naturally radioactive
	    return Float.POSITIVE_INFINITY;
	}

	if(checkNull(value)){
	    // the value is not known
	    return Float.NaN;
	}

	// remove and '<' signs
	index=value.indexOf("<");
	if(index>=0){
	    index=index+1;
	    value=value.substring(index,value.length());
	}
	
	// remove (+/-)
	index=value.indexOf("-)");
	if(index>=0){
	    index=index+2;
	    value=value.substring(index,value.length());
	}

	// remove uncertainties
	index=value.indexOf("(");
	if(index>=0){
	    value=value.substring(0,index);
	}

	// convert years and minutes into days
	index=value.indexOf("y");
	if(index>0){
	    value=value.substring(0,index);
	    Float temp=new Float(Float.parseFloat(value)*365.25f);
	    value=temp.toString();
	}
	index=value.indexOf("d");
	if(index>0){
	    value=value.substring(0,index);
	}
	index=value.indexOf("h");
	if(index>0){
	    value=value.substring(0,index);
	    Float temp=new Float(Float.parseFloat(value)/24.0f);
	    value=temp.toString();
	}
	index=value.indexOf("m");
	if(index>0){
	    value=value.substring(0,index);
	    Float temp=new Float(Float.parseFloat(value)/(24.0f*60.0f));
	    value=temp.toString();
	}

	return Float.parseFloat(value);
    }

    /**
     *  Take a String of arbitrary length and convert it into a two
     *  letter string where the first character is uppercase and the
     *  second character is lowercase. If the String is one character
     *  it is just capitalized. If the String is more than two
     *  characters it is chopped to only be two characters long.
     */
    private static String formatElement( String ele ){
	String rs=null;
	if(ele.length()==1){
	    rs=ele.toUpperCase();
	}else{
	    String first = ele.substring(0,1);
	    String rest  = ele.substring(1,2);
	    rs=first.toUpperCase()+rest.toLowerCase();
	}
	return rs;
    }

    /* ------------------- accessor and mutator methods -------------------- */
    /**
     *  Accessor method for the element name
     */
    public String element(){
      if(this.element==null)
        return null;
      else
	return new String(this.element);
    }

    /**
     *  Accessor method for the isotope number
     */
    public int isotope(){
	return this.isotope;
    }

    /**
     *  Accessor method for the concentration
     */
    public float concentration(){
	return this.concentration;
    }

    /**
     *  Accessor method for the natural abundance
     */
    public float abundance(){
	return this.abundance;
    }

    /**
     *  Accessor method for the molar mass
     */
    public float molarMass(){
	return this.molarMass;
    }

    /**
     *  Accessor method for the atomic Weight
     */
    public float atomicWeight(){
	return this.molarMass;
    }

    /**
     *  Accessor method for the density.
     */
    public float density(){
	return this.density;
    }

    /**
     *  Mutator method for the density
     */
    public float density(float dens){
	this.density=dens;
	return this.density;
    }

    /**
     *  Accessor method for the half life.
     */
    public float halflife(){
	return this.halflife;
    }

    /**
     *  Accessor method for the real part of the coherent scattering
     *  length
     */
    public float R_b_coh(){
	return this.R_b_coh;
    }

    /**
     *  Accessor method for the imaginary part of the coherent
     *  scattering length
     */
    public float I_b_coh(){
	return this.I_b_coh;
    }

    /**
     *  Accessor method for the real part of the incoherent scattering
     *  length
     */
    public float R_b_inc(){
	return this.R_b_inc;
    }

    /**
     *  Accessor method for the imaginary part of the incoherent scattering
     *  length
     */
    public float I_b_inc(){
	return this.I_b_inc;
    }

    /**
     *  Accessor method for the coherent scattering cross-section
     */
    public float xs_coh(){
	return this.xs_coh;
    }

    /**
     *  Accessor method for the coherent scattering cross-section
     */
    public float xs_inc(){
	return this.xs_inc;
    }

    /**
     *  Accessor method for the total scattering cross-section
     */
    public float xs_tot(){
	return this.xs_tot;
    }

    /**
     *  Accessor method for the scattering absorption cross-section
     */
    public float xs_abs(){
	return this.xs_abs;
    }

    /**
     *  Accessor method for the storage time
     */
    public float storeT(){
	return this.storeT;
    }

    /**
     *  Accessor method for the prompt activation
     */
    public float promptAct(){
	return this.promptAct;
    }

    /**
     *  Accessor method for the contact dosage
     */
    public float contactDose(){
	return this.contactDose;
    }

    /* -------------------- String methods -------------------- */
    /**
     *  Print all of the information available about the atom.
     */
    public void printInfo( ){
	System.out.println("----------"+this+"----------");
	if(!(this.element==null)){
	    System.out.println("conc="+abundance+"  molarMass="+molarMass
			       +"  density="+density+"  half-life="+halflife);
	    System.out.println("b:  coh("+R_b_coh+", "+I_b_coh+")  inc("
			       +R_b_inc+", "+I_b_inc+")");
	    System.out.println("xs: coh"+xs_coh+"  inc"
			       +xs_inc+"  tot"+xs_tot+"  abs"+xs_abs);
	    System.out.println("ST="+storeT+"  PA="+promptAct+"  CD="
			       +contactDose);
	}
    }

    /**
     *  Format the toString method to be the full version of how it
     *  can be specified. For example, Hydrogen would be returned as
     *  'H(0)_1' meaning that it is one atom of isotopically averaged
     *  Hydrogen.
     */
    public String toString( ){
	String rs= new String(element);
	if(isotope!=0.0f){
	    rs=rs+"("+isotope+")";
	    }
	if(concentration!=1.0f){
	    rs=rs+"_"+concentration;
	}
	if(element==null){
	    rs=null;
	}
	return rs;
    }

    /**
     *  The main program for testing purposes.
     */
    public static void main( String[] args ){
	System.out.println("file is: "+isotopefile);
	Atom mine;

	System.out.println("==========Si,O_2==========");
	mine = new Atom("si");
	mine.printInfo();
	mine = new Atom("o_2");
	mine.printInfo();

	System.out.println("==========H(1)_2,O==========");
	mine = new Atom("h(1)_2");
	mine.printInfo();
	mine = new Atom("o");
	mine.printInfo();

	/* System.out.println("==========oberweiSS==========");
	   mine = new Atom("oberweiSS");
	   mine.printInfo();
	   
	   System.out.println("==========Ru(98)==========");
	   mine = new Atom("Ru(98)");
	   mine.printInfo();

	   System.out.println("==========Mg==========");
	   mine = new Atom("Mg");
	   mine.printInfo();

	   System.out.println("==========Na==========");
	   mine = new Atom("Na");
	   mine.printInfo();

	   System.out.println("==========Sc==========");
	   mine = new Atom("Sc");
	   mine.printInfo();

	   System.out.println("==========Al==========");
	   mine = new Atom("Al");
	   mine.printInfo(); 

	   System.out.println("==========Hg(199)==========");
	   mine = new Atom("Hg(199)");
	   mine.printInfo(); */

	   System.out.println("==========Am==========");
	   mine = new Atom("Am");
	   mine.printInfo(); 
    }
}
