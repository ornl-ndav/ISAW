/*
 * File:  Material.java
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
 * Revision 1.4  2003/12/15 02:24:21  bouzekc
 * Removed unused imports.
 *
 * Revision 1.3  2003/02/04 16:03:47  pfpeterson
 * Throws an InstantiationError if not all atoms can be created.
 *
 * Revision 1.2  2002/11/27 23:15:35  pfpeterson
 * standardized header
 *
 */
package DataSetTools.materials;

import java.io.File;
import java.util.Vector;

/**
 * Methods for getting information about atoms dependent on the
 * isotope.
 */

public class Material{
    // instance variables
    private Vector atoms = new Vector();

    // class variables
    private static String materialfile = null;

    static {
	//materialfile = System.getProperty("ISAW_HOME").trim();
	materialfile = "/IPNShome/pfpeterson/ISAW";
	materialfile = materialfile+java.io.File.separator+"Materials.dat";
	if( new File(materialfile).exists()){
	    // do nothing
	}else{
	    materialfile = null;
	}
    }

    /* --------------------Constructor Methods-------------------- */
    public Material( String mat ){
	Vector Satoms=new Vector();
	int index=0;
	
	while(true){
	    index=mat.indexOf(",");
	    if(index>0){
		Satoms.add(mat.substring(0,index));
		mat=mat.substring(index+1,mat.length());
	    }else{
		Satoms.add(mat);
		break;
	    }
	}

	for( int i=0 ; i<Satoms.size() ; i++ ){
	    Atom tempAtom=new Atom((String)Satoms.elementAt(i));
	    if(tempAtom.element()!=null){
		this.atoms.add(tempAtom);
	    }else{
              throw new InstantiationError("Could not find information for \""
                                           +Satoms.elementAt(i)+"\"");
            }
	}
	
    }

    /* ------------------- accessor and mutator methods -------------------- */
    /**
     *  accessor method to obtain the total number of atoms
     */
    public int numAtoms(){
	return this.atoms.size();
    }

    /**
     * accessor method to obtain the ith atom of the element
     */
    public Atom atomAt(int i){
	/* Atom temp=((Atom)this.atoms.elementAt(i)).clone();
	   return temp; */
	return (Atom)this.atoms.elementAt(i);
    }

    /**
     * method to aid in calculating the normalized concentration
     */
    private float totalConc(){
	float total=0.0f;
	for( int j=0 ; j<this.numAtoms() ; j++ ){
	    total+=((Atom)this.atoms.elementAt(j)).concentration();
	}

	return total;
    }

    /**
     * method to aid in calculating the mass fraction
     */
    private float totalMass(){
	float total=0.0f;
	for( int j=0 ; j<this.numAtoms() ; j++ ){
	    total+=((Atom)this.atoms.elementAt(j)).molarMass()*((Atom)this.atoms.elementAt(j)).concentration();
	}

	return total;
    }

    /**
     * accessor method to obtain the normalized concentration of the
     * ith atom
     */
    public float normConc(int i){
	float total=this.totalConc();
	return ((Atom)this.atoms.elementAt(i)).concentration()/total;
    }

    /**
     * accessor method to obtain the mass fraction of the ith atom
     */
    public float massFrac(int i){
	float total=this.totalMass();
	Atom temp=(Atom)this.atoms.elementAt(i);

	return temp.molarMass()*temp.concentration()/total;
    }

    /**
     * accessor method to obtain the storage time of the ith atom
     */
    public float storeT(int i){
	return ((Atom)this.atoms.elementAt(i)).storeT();
    }

    /**
     * accessor method to obtain the prompt activity of the ith atom
     */
    public float promptAct(int i){
	return ((Atom)this.atoms.elementAt(i)).promptAct();
    }

    /**
     * accessor method to obtain the contact dose of the ith atom
     */
    public float contactDose(int i){
	return ((Atom)this.atoms.elementAt(i)).contactDose();
    }

    /* -------------------- String methods -------------------- */
    /**
     *  Print all of the information available about the atom.
     */
    public void printInfo( ){
	System.out.println("=========="+this+"==========");
    }

    /**
     *  Format the toString method to be the full version of how the
     *  material can be specified. For example, water would be
     *  returned as 'H(0)_2,O(0)_1' meaning that it is two atoms of
     *  isotopically averaged Hydrogen and one atom of isotropically
     *  averaged Oxygen.
     */
    public String toString( ){
	String rs=null;

	if(this.atoms.size()>0){
	    rs=this.atoms.elementAt(0)+",";
	}
	for( int i=1 ; i<this.atoms.size() ; i++ ){
	    rs=rs+this.atoms.elementAt(i)+",";
	}
	if(rs!=null){
	    rs=rs.substring(0,rs.length()-1);
	}

	return rs;
    }

    /**
     *  The main program for testing purposes.
     */
    public static void main( String[] args ){
	System.out.println("file is: "+materialfile);
	Material mine;

	System.out.println("==========Si,O_2==========");
	mine=new Material("Si,O_2");
	System.out.println(mine+"-->");

	System.out.println("==========H(1)_2,O==========");
	mine=new Material("H(1)_2,O");
	System.out.println(mine+"-->");

	System.out.println("==========Ru(98)==========");
	mine=new Material("Ru(98)");
	System.out.println(mine+"-->");

	System.out.println("==========oberweiSS==========");
	mine=new Material("oberweiSS");
	System.out.println(mine+"-->");

    }
}
