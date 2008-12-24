/*
 * File:  NxWriteData.java 
 *             
 * Copyright (C) 2001 , Ruth Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License , or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful ,
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
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.19  2006/10/10 15:32:40  rmikk
 * Eliminated some javadoc errors and one unused routine
 *
 * Revision 1.18  2006/07/28 19:44:48  rmikk
 * Fixed it so y-offset is not all zeroes.
 * Reformatted a lot of the code.
 *
 * Revision 1.17  2005/02/12 17:50:22  rmikk
 * Eliminated writing a deprecated node
 *
 * Revision 1.16  2005/01/20 21:52:08  dennis
 * Fixed minor javadoc error.
 *
 * Revision 1.15  2005/01/14 23:32:37  rmikk
 * Fixed some logical errors in saving.  Eliminated( erroneously?) the 
 * dimensions with size 1.
 *
 * Revision 1.14  2005/01/10 17:30:36  rmikk
 * Fixed javadoc error
 *
 * Revision 1.13  2005/01/10 16:18:13  rmikk
 * Now processes a data set that has LPSD's and area grids
 *
 * Revision 1.12  2004/12/23 19:56:56  rmikk
 * Fixed indentation and added blank lines for spacing
 * Now writes Nexus standard version 1.0 format. This includes NXgeometry
 *   description, etc.
 * Unit names now conform to the standard when possible
 *
 * Revision 1.11  2004/05/14 15:03:51  rmikk
 * Removed unused variables
 *
 * Revision 1.10  2004/03/15 03:36:02  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.9  2004/02/16 02:19:23  bouzekc
 * Removed unused imports.
 *
 * Revision 1.8  2004/02/14 18:24:59  rmikk
 * Initiated initial(unsuccessful with LPSD's) attempts to save a data set with
 * several different types of detectors to a NeXus file.
 *
 * Revision 1.7  2003/11/24 13:54:19  rmikk
 * Writes out separate detectors to separate NXdata.
 * Writes out the detector orientation as Euler Transformations
 *
 * Revision 1.6  2003/06/18 20:34:12  pfpeterson
 * Changed calls for NxNodeUtils.Showw(Object) to
 * DataSetTools.util.StringUtil.toString(Object)
 *
 * Revision 1.5  2002/11/27 23:29:19  pfpeterson
 * standardized header
 *
 * Revision 1.4  2002/11/20 16:15:37  pfpeterson
 * reformating
 *
 * Revision 1.3  2002/04/01 20:50:02  rmikk
 * Each NXdata now has a name related to its DataSet Title
 * A common label attribute is added to each NXdata from one data set. This, 
 * when read will by Isaw will merge these NXdata into one data set
 *
 * Revision 1.2  2002/03/18 20:58:27  dennis
 * Added initial support for TOF Diffractometers.
 * Added support for more units.
 *
 */

package NexIO.Write;


import NexIO.*;
import DataSetTools.dataset.*;
import NexIO.Util.*;
import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.Util.Sys.StringUtil;

import java.util.*;


/**
 * Class used to write the NXdata information of a Data Set
 */
public class NxWriteData {
  
    String errormessage;
    String axis1Link = "axis1";
    String axis2Link = "axis2";
    String axis3Link = "axis3";
    public boolean write = false;
    int MIN_GRID_SIZE = 8;  //if grid ==null or numrows*numcols <MIN_GRID_SIZE
    //   the detector will be saved as individual pixels
    int MAX_GRID_SIZE = 100; // if grid larger than this, will not be merged
    int Inst_Type;
    Vector<NxWriteNode>  dataNodes = new Vector<NxWriteNode>();

    public NxWriteData( int Inst_Type ) {
      
        errormessage = "";
        this.Inst_Type = Inst_Type;
    }

    
    
    
    /**
     * Returns an error message or "" if none
     */ 
    public String getErrorMessage() {
      
        return errormessage;
    }

    /**
     * Writes the Data from a data set to a NXdata section of a Nexus
     * file
     *
     * @param nodeEntr an NxEntry node
     * @param nxInstr an NXinstrument node
     * @param DS The data set with the information
     * @param makelinks should always be true
     *
     * NOTE: This routine also writes some NXdetector information due to
     * linking requirements
     */
    public boolean processDS( NxWriteNode nodeEntr , NxWriteNode nxInstr ,  
        DataSet DS , boolean makelinks ) {
       return processDS( nodeEntr, nxInstr,DS, makelinks, false);
    }
  
    
    
    /**
     * Writes the Data from a data set to a NXdata section of a Nexus
     * file
     *
     * @param nodeEntr  an NxEntry node
     * @param nxInstr   an NXinstrument node
     * @param DS        The data set with the information
     * @param makelinks Should always be true
     * @param useLabel  Add a label field so loader merges all NXdata with the
     *                  same label( different tof axes usually)
     *
     * NOTE: This routine also writes some NXdetector information due to
     * linking requirements
     */
    public boolean processDS( NxWriteNode nodeEntr , NxWriteNode nxInstr ,  
        DataSet DS , boolean makelinks ,boolean useLabel) {

        int  i, 
             j;
      
        errormessage = " Null inputs to Write Data";
        if ( nodeEntr == null )
            return true;
    
        if ( nxInstr == null )
            return true;
  
        if ( DS == null )
            return true;
        errormessage = "";
         
        
         
        //----- process Gridded data  --------------------------
        int nNXdatas = processDSgrid( nodeEntr , nxInstr , DS , makelinks, useLabel );

        //--------process pixels as singleton's in one NXdata per XScale----------
    
        int k = nNXdatas;

        for ( j = 0 ; j < DS.getNum_entries() ; j++ ) {
      
            Data DBM = DS.getData_entry( j );
            XScale xsc = DBM.getX_scale();
            IDataGrid grid1 = getDataGrid( DBM );
      
            boolean already = false;   // flag to jump out of blank 
                                       //       do loop faster
            int ny_s = DBM.getY_values().length;
      
            float[] yvals = new float[ 0 ] , 
                errors = new float[ 0 ] ,
                distance = new float[ 0 ] ,
                azim = new float[ 0 ] ,
                polar = new float[ 0 ] , 
                solidAngle = new float[ 0 ] , 
                rawAngle = new float[ 0 ] ,
                TotCount = new float[ 0 ] ,
                DeltToTheta = new float[ 0 ];
              
            int[] id     = new int[ 0 ] ,
                  crate  = new int[ 0 ] ,
                  slot   = new int[ 0 ] , 
                  input  = new int[ 0 ];
            float[ ] width  = new  float[ 0 ] ,
                    height = new float[ 0 ] ,
                    depth  = new float[ 0 ]; 
            java.util.LinkedList orient = new LinkedList();     

            if ( ( grid1 == null ) || ( grid1.num_rows() * grid1.num_cols() <
                                                              MIN_GRID_SIZE ) )
                for ( i = 0 ; ( i < DS.getNum_entries() ) && ( !already ) ;
                                                                        i++ ) {
        
                    Data DB = DS.getData_entry( i );
                    XScale XX = DB.getX_scale();
                    IDataGrid grid2 = getDataGrid( DB );

                    if ( ( grid2 == null ) || ( grid2.num_rows() * 
                             grid2.num_cols()  < MIN_GRID_SIZE ) )
                        if ( xsc == XX )
                            if ( i < j  ) 
                                already = true;
               
                            else if ( !already ) { 
                               
                                yvals = UnionF( yvals , DB.getY_values() );
                                errors = UnionF( errors , DB.getErrors() );
                                DetectorPosition dp = ( DetectorPosition ) 
                                                  DB.getAttributeValue
                                    ( Attribute.DETECTOR_POS );

                                if ( dp != null ) {
                                    float[] coords = dp.getSphericalCoords();

                                    coords = Types.convertToNexus( coords[ 0 ] , 
                                                 coords[ 2 ] , coords[ 1 ] );
                                    
                                    distance = UnionF( distance ,
                                                     new Float( coords[ 0 ] ) );
                                    
                                    polar = UnionF( polar , 
                                                    new Float( coords[ 1 ] ) );
                                    
                                    azim = UnionF( azim , 
                                                    new Float( coords[ 2 ] ) );
                                    
                                }
              
                                if ( ( grid2 != null ) && ( grid2.num_rows() == 1 )
                                              &&  ( grid2.num_cols() == 1 ) ) {
                                          
                                    width = UnionF( width , 
                                                new Float( grid2.width() ) );
                                    depth = UnionF( depth , 
                                                 new Float( grid2.depth() ) );
                                    height = UnionF( height ,
                                                 new Float( grid2.height() ) ); 
                                    if ( ( grid2.x_vec() != null ) && 
                                                 ( grid2.y_vec() != null ) ) {
                 
                                        orient.add( grid2.x_vec().get());
                                        orient.add( grid2.y_vec().get() );
                                    }   
                                }
              
                                id = UnionI( id , new Integer( 
                                                         DB.getGroup_ID() ) );
                                
                                solidAngle = UnionF( solidAngle , DB.
                                  getAttributeValue( Attribute.SOLID_ANGLE ) );
                                
                                rawAngle = UnionF( rawAngle , DB.
                                    getAttributeValue( Attribute.RAW_ANGLE ) );
                                
                                TotCount = UnionF( TotCount , DB.
                                  getAttributeValue( Attribute.TOTAL_COUNT ) );
                                
                                DeltToTheta = UnionF( DeltToTheta , DB.
                                 getAttributeValue( Attribute.DELTA_2THETA ) );
                                
                                slot = UnionI( slot , DB.getAttributeValue( 
                                                Attribute.SLOT ) );
                                
                                crate = UnionI( crate , DB.getAttributeValue( 
                                                           Attribute.CRATE ) );
                                
                                input = UnionI( input , DB.getAttributeValue( 
                                                         Attribute.INPUT ) );
                                
             
                            }//if small or no grid
      
                }//for i = 0 i < num entries
    
                //---------make NXdata and NXdetector for this pixel-----------
    
            if ( ( grid1 == null ) || ( grid1.num_rows() * grid1.num_cols() < 
                                                             MIN_GRID_SIZE ) )
                if ( !already ) { //make an NXdata and Nxdetector
          
                    NxWriteNode datanode = nodeEntr.newChildNode( DS.getTitle() 
                          + "_" + k , "NXdata" );
                    dataNodes.add( datanode);
                    NxWriteNode detnode = nxInstr.newChildNode( DS.getTitle() 
                             + "_"  + k , "NXdetector" );
                    
                    NxWriteNode dnode = util.writeFA_SDS( datanode , "data" , 
                        yvals , NexIO.Inst_Type.makeRankArray( yvals.length / 
                         ny_s , ny_s , -1 , -1 , -1 ) );
         
                    util.writeStringAttr( dnode , "units" , "Count" );
                    util.writeIntAttr( dnode , "signal" , 1 );
                  //  util.writeStringAttr( dnode , "label" , DS.getTitle() );
          
                    util.writeStringAttr( dnode , "link" , DS.getTitle() + 
                                                              "_" + k );
          
                    dnode = util.writeFA_SDS( datanode , "errors" , errors ,
                              NexIO.Inst_Type.makeRankArray( errors.length / 
                              ny_s , ny_s , -1 , -1 , -1 ) );
         
                    dnode = util.writeFA_SDS( detnode , "time_of_flight" , 
                         DBM.getX_scale().getXs() , NexIO.Inst_Type.
                         makeRankArray( xsc.getNum_x() , -1 , -1 , -1 , -1 ) );
                          
                    if ( ( width.length > 0 ) && ( height.length > 0 ) && 
                                                ( depth.length > 0 ) ) {
                                                  
                        float[][] orientation = null;

                        if ( orient.size() > 0 ) {             
                            orientation = new float[ orient.size() / 2 ][ 6 ];
                            for ( int kk = 0 ; kk < orient.size() / 2 ; kk++ ) {
                               System.arraycopy( orient.get( 2 * kk ) , 0 , 
                                                   orientation[ kk ] , 0 , 3 );
                               System.arraycopy( orient.get( 2 * kk + 1 ) , 0 ,
                                                  orientation[ kk ] , 3 , 3 );
                            }                 
                        }            
                        SetUpGridInfoNxDet( detnode , width , height , depth , 
                                                              orientation ); 
                    }      
                    dnode.setLinkHandle( DS.getTitle() + "_" + k );
                    util.writeIntAttr( dnode , "axis" , 2 );
                    util.writeStringAttr( dnode , "units" , DS.getX_units() );
                    
                    datanode.addLink( DS.getTitle() + "_" + k ,dnode);
         
                    dnode = util.writeIA_SDS( detnode , "id" , id ,
                                NexIO.Inst_Type.makeRankArray( id.length , -1 ,
                                 -1 , -1 , -1 ) );
                 
                    dnode.setLinkHandle( "detector_number" + DS.getTitle() + 
                                                                    "_" + k );
                    datanode.addLink( "detector_number" + DS.getTitle() +
                                                                    "_" + k,
                                                                    dnode);
                    util.writeIntAttr( dnode , "axis" , 1 );
          
                    dnode = util.writeFA_SDS( detnode , "distance" , distance ,
                                NexIO.Inst_Type.makeRankArray( distance.length , 
                                -1 , -1 , -1 , -1 ) );
                    util.writeStringAttr( dnode , "units" , "meter" );
          
                    dnode = util.writeFA_SDS( detnode , "polar_angle" , polar ,
                          NexIO.Inst_Type.makeRankArray( polar.length , -1 , 
                          -1 , -1 , -1 ) );
                           
                    util.writeStringAttr( dnode , "units" , "radian" );
                    dnode = util.writeFA_SDS( detnode , "azimuthal_angle" ,
                          azim , NexIO.Inst_Type.makeRankArray( azim.length , 
                          -1 , -1 , -1 , -1 ) );
                    util.writeStringAttr( dnode , "units" , "radian" );

                    dnode = util.writeFA_SDS( detnode , "polar" , polar , 
                           NexIO.Inst_Type.makeRankArray( polar.length , -1 , 
                           -1 , -1 , -1 ) );
                    util.writeStringAttr( dnode , "units" , "radian" );

                    dnode = util.writeFA_SDS( detnode , "solid_angle" , 
                             solidAngle , NexIO.Inst_Type.makeRankArray
                             ( solidAngle.length , -1 , -1 , -1 , -1 ) );
          
                    dnode = util.writeFA_SDS( detnode , "raw_angle" , rawAngle ,
                           NexIO.Inst_Type.makeRankArray( rawAngle.length , -1 ,
                            -1 , -1 , -1 ) );
                    util.writeStringAttr( dnode , "units" , "degree" );

                    dnode = util.writeIA_SDS( detnode , "slot" , slot , NexIO.
                       Inst_Type.makeRankArray( slot.length , -1 , -1 ,
                       -1 , -1 ) );

                    dnode = util.writeIA_SDS( detnode , "crate" , crate , NexIO.
                       Inst_Type.makeRankArray( crate.length , -1 , -1 , 
                       -1 , -1 ) );
         
                    dnode = util.writeIA_SDS( detnode , "input" , input , NexIO.
                       Inst_Type.makeRankArray( input.length , -1 , -1 , 
                       -1 , -1 ) );
         
                    dnode = util.writeFA_SDS( detnode , "total_count" ,  
                             TotCount , NexIO.Inst_Type.makeRankArray( 
                             TotCount.length , -1 , -1 , -1 , -1 ) );
                    
                    util.writeStringAttr( dnode , "units" , "count" );
       
                    int[] rank = NexIO.Inst_Type.makeRankArray( 
                            DeltToTheta.length , -1 , -1 , -1 , -1 );
             
                    dnode = util.writeFA_SDS( detnode , "delta_to_theta" ,
                                DeltToTheta , rank );
                    util.writeStringAttr( dnode , "units" , "radian" );
                    
                    k++;
          
                }

        }//for j=0; j< num_entries
        if( write )
        for( i=0; i< dataNodes.size(); i++)
           dataNodes.elementAt(i).write();
        return false;
    }

    
    
    
    
    private float[] UnionF( float[] Array , Object newElement ) {
    
        if ( Array == null ) 
            return null;
       
        if ( newElement == null ) 
            return null;
        
        if ( newElement instanceof float[] )
            return UnionF( Array , ( float[] ) newElement );
         
        if ( !( newElement  instanceof Number ) ) 
            return null;
       
        float[] A1 = new float[ Array.length + 1 ];

        System.arraycopy( Array , 0 , A1 , 0 , Array.length );
        A1[ Array.length ] = ( ( Number ) newElement ).floatValue();  
        return A1;
    }
  
  
  
    
    
    
    
    
  
    private int[] UnionI( int[] Array , Object newElement ) {
    
        if ( Array == null ) 
            return null;
       
        if ( newElement == null ) 
            return null; 
        
        if ( newElement instanceof int[] )
            return UnionI( Array , ( int[] ) newElement );
       
        if ( !( newElement instanceof Integer ) )
            return null;
       
        int[] A1 = new int[ Array.length + 1 ];

        System.arraycopy( Array , 0 , A1 , 0 , Array.length );
        A1[ Array.length ] = ( ( Number ) newElement ).intValue(); 
        return A1;
    }
  
  
    
    
    
    
    
    //Creates a new float array with the new elements added to it
    private float[] UnionF( float[] Array , float[] newElement ) {
    
        if ( Array == null ) 
            return null;
       
        if ( newElement == null ) 
            return null;
         
        //if ( !( newElement instanceof float[] ) )
         //   return null;
       
        float[] A1 = new float[ Array.length + newElement.length ];

        System.arraycopy( Array , 0 , A1 , 0 , Array.length );
        System.arraycopy(  newElement , 0 , A1 , Array.length , newElement.length );  
        return A1;

    }
  
    
    
    
    
  
    //Creates a new int array with newElements added to Array
    private int[] UnionI( int[] Array , int[] newElement ) {
    
        if ( Array == null ) 
            return null;
       
        if ( newElement == null ) 
            return null; 
       
       // if ( !( newElement instanceof int[] ) )
       //     return null; 
       
        int[] A1 = new int[ Array.length + newElement.length ];

        System.arraycopy( Array , 0 , A1 , 0 , Array.length );
        System.arraycopy(  newElement , 0 , A1 , Array.length ,
                                                    newElement.length );
        
        return A1;

    }


    
    

    //Obsolete
    private float[] MergeXvals( int db , DataSet DS , float xvals[] ) {
        if ( db >= DS.getNum_entries() )
            return xvals; 
        if ( db == 0 ) {
            Data DB = DS.getData_entry( 0 );
            XScale XX = DB.getX_scale();

            return MergeXvals( 1 , DS , XX.getXs() );
        }
        Data DB = DS.getData_entry( db );
        XScale XX = DB.getX_scale();
        float xlocvals[];

        xlocvals = XX.getXs();
        float Delta = ( xvals[ xvals.length - 1 ] - xvals[ 0 ] ) /
                                                        xvals.length / 20.0f;

        //System.out.println( "Delta = "+Delta  );
        if ( Delta < 0 ) Delta = 0.0f;
        int j = 0; 
        int i = 0;
        int n = 0;
  
        while ( ( i < xvals.length ) || ( j < xlocvals.length ) ) {
            if ( i >= xvals.length ) {
                j++;
                n++;
            } else if ( j >= xlocvals.length ) {
                i++;
                n++;
            } else if ( xvals[ i ] < xlocvals[ j ] - Delta ) {
                i++;
                n++;
            } else if ( xvals[ i ] > xlocvals[ j ] + Delta ) {
                j++;
                n++;
            } else {
                i++;
                j++;
                n++;
            }
        }  
    
        float Res[];

        Res = new float[ n  ];
        j = 0;
        i = 0;
        n = 0;
        while ( ( i < xvals.length ) || ( j < xlocvals.length ) ) {
            if ( i >= xvals.length ) {
                Res[ n ] = xlocvals[ j ];
                j++;
                n++;
            } else if ( j >= xlocvals.length ) {
                Res[ n ] = xvals[ i ];
                i++;
                n++;
            } else if ( xvals[ i ] < xlocvals[ j ] - Delta ) {
                Res[ n ] = xvals[ i ];
                i++;
                n++;
            } else if ( xvals[ i ] > xlocvals[ j ] + Delta ) {
                Res[ n ] = xlocvals[ j ];
                j++;
                n++;
            } else {
                Res[ n ] = ( xvals[ i ] + xlocvals[ j ] ) / 2.0f;
                i++;
                j++;
                n++;
            }
        }
   
        return MergeXvals( db + 1 , DS , Res );

    }




    
    
    /**
     * Obsolete :Use this routine to determine interval lengths for both
     * histgram and function data
    
    private float intlength( float xvals[] , int intnum , boolean histogram ) {
        int nfix = 1;

        if ( !histogram ) 
            nfix = 0;
        if ( xvals == null ) 
            return 0.0f;
       
        if ( intnum < 0 ) 
            return 0.0f;
        if ( intnum >= xvals.length - nfix ) 
            return 0.0f;
       
        if ( histogram )
            return xvals[ intnum + 1 ] - xvals[ intnum ];
      
        float rendpt , 
            lendpt;
          
        if ( intnum == 0 )
            lendpt = xvals[ 0 ] - ( xvals[ 1 ] - xvals[ 0 ] ) / 2.0f;
      
        else
            lendpt = ( xvals[ intnum - 1 ] + xvals[ intnum ] ) / 2.0f;
      
        if ( intnum >= xvals.length - 1 )
            rendpt = xvals[ xvals.length - 1 ] +
                    ( xvals[ xvals.length - 1 ] + xvals[ xvals.length - 2 ] )
                                                                        / 2.0f;
        
        else 
            rendpt = ( xvals[ intnum ] + xvals[ intnum + 1 ] ) / 2.0f;
      
        return rendpt - lendpt;
    }

   */



    /**
     * Obsolete Returns a rebinned value for this data blocks y values
     * wrt the xvals rebinning.  Assumes histogram so far
     */
    private float[] Rebinn( Data DB , float xvals[] ) {
    
        errormessage = "null inputs to Rebinn";
        if ( DB == null )
            return null;
      
        if ( xvals == null )
            return null;
      
        float yvals[];

        yvals = DB.getY_values();
        if ( yvals == null )
            return null;
      
        float xlocvals[];
        XScale XX = DB.getX_scale();

        xlocvals = XX.getXs();
        if ( xlocvals == null )
            return null;
      
        errormessage = "";
        int ny_s = xvals.length;
        int nfix = 0;

        if ( xlocvals.length != yvals.length )
            nfix = 1;
      
        float Res[];

        Res = new float[ ny_s - nfix ];
        float Delta = ( xvals[ xvals.length - 1 ] - xvals[ 0 ] ) / xvals.length
                                                                       / 15.0f;
  
        int j = 0;

        for ( int i = 0 ; i < yvals.length ; i++ ) {
      
            boolean done = false;

            while ( !done ) {
                Res[ j ] = yvals[ i ] * ( xvals[ j + 1 ] - xvals[ j ] ) / 
                                        ( xlocvals[ i + 1 ] - xlocvals[ i ] );
                j++;
                if ( j >= xvals.length - nfix )
                    done = true;
          
                else if ( xvals[ j ] >= xlocvals[  i + 1 ] - Delta )
                    done = true;
          
            }
        } 
        return Res;  
    
    }




    /**
     *    Can this DataSet be made into a 2D or 3D Nexus NXdata.
     *    Extensions may allow for several 2D ,3D etc , NXdata by returning
     *    int[] with int's of 0(  no can do ) , or 1  ,2 ,3 which are tags for the
     *    Data blocks that can be grouped(  TO DO )
     */
    public boolean Can2Dify( int[] grids , DataSet DS ) {
     
        if ( grids == null )
            return false;
       
        if ( grids.length < 1 )
            return false;
    
        Data D = getAreaGrid( DS , grids[ 0 ] ).getData_entry( 1 , 1 );

        if ( D == null )
            return false;
        
        XScale xsc = D.getX_scale(); // should be the common Xscale for 

        // All data blocks in grid
        for ( int grid = 0 ; grid < grids.length ; grid++ ) {
            IDataGrid Grid = getAreaGrid( DS , grids[ grid ] );

            if ( Grid.num_rows() <= 1 ) if ( Grid.num_cols() <= 1 ) 
                    return false;
           
            for ( int row = 1 ; row <= Grid.num_rows() ; row++ )
                for ( int col = 1 ; col <= Grid.num_cols() ; col++ ) {
                    D = Grid.getData_entry( row , col );
                    if ( D == null )
                        return false;
                
                    if ( D.getX_scale() != xsc )
                        return false;
                 
                } 
        }
        return true;
    }



  
   /**
    * "Writes" out the Data from the DataSet DS.  Also , NXdetector elements 
    * are also writte 
    * @param nodeEntr  A node whose class is NXentry
    * @param nxInstr   The NXinstrument node for this NXentry
    * @param DS        The DataSet to be written
    * @param makelinks   Not used. Links are made
    * @return  1 if successful or -1 if unsuccessful
    */
    public int processDSgrid( NxWriteNode nodeEntr , NxWriteNode nxInstr , 
        DataSet DS , boolean makelinks ,boolean useLabel) {
              
        int nNXdatas = 0;
        int[] grids = getAreaGrids( DS ); 
        
        for ( int i = 0 ; i < grids.length ; i++ )
        
           
            if ( processDSgrid1( nodeEntr , nxInstr , DS , makelinks ,
                                                                grids[ i ], grids.length ) )
                return -1;
               
            else 
                nNXdatas++;

            if( ProcessGroupedGrids(  nodeEntr , nxInstr , DS , makelinks , 
                                                            grids ) )
                return -1;
        return 1;    

    }
  
  
    // Not fixed. Several grids are grouped in one NXdata-NXdetector. For
    // LPSD's 
    private boolean ProcessGroupedGrids( NxWriteNode nodeEntr , 
                    NxWriteNode nxInstr , DataSet DS , boolean makelinks ,
                    int[] grids ) {
              
        if ( grids == null ) 
            return false;
      
        if ( grids.length < 1 ) 
            return false;
      
        int NGridClasses = 0;
        int[] GridClass = new int[ grids.length ];

        java.util.Arrays.fill( GridClass , -1 );
   
        //Set up GridClass array and NGridClasses
        for ( int i = 0 ; i < grids.length ; i++ ) {
            IDataGrid grid1 = getAreaGrid( DS , grids[ i ] );

            if ( grid1 != null )
                if ( grid1.num_rows() * grid1.num_cols() >= MIN_GRID_SIZE )
                    if ( grid1.num_rows() * grid1.num_cols() < MAX_GRID_SIZE ){
        
                        XScale xscl = grid1.getData_entry( 1 , 1 ).
                                                                getX_scale();
                        
                        boolean already = false;
                        boolean set = false;

                        for ( int j = 0 ; ( j < grids.length ) && !already ;
                                                                        j++ ) {
          
                            IDataGrid grid2 = getAreaGrid( DS , grids[ j ] );
                           
                            if ( grid2 != null )
                                if ( grid2.num_rows() == grid1.num_rows())
                                    if ( grid2.num_cols() == grid1.num_cols() )
                                        if ( grid2.getData_entry( 1 , 1 ).
                                                         getX_scale() == xscl )
                                           
                                            if ( j < i )
                                                already = true;                                              
                                            else { 
                                               
                                                GridClass[ j ] = NGridClasses;
                                                set = true;
                                            }
                        }
                        //two are compatible
                        if ( set ) 
                            NGridClasses++; 
                    }//if grid in this range
        }
        int det = -1;
        for ( int i = 0 ; i < NGridClasses ; i++ ) {
     
            int NDet = 0;
            int first = -1;

            for ( int k = 0 ; k < GridClass.length ; k++ ) 
                if ( GridClass[ k ] == i ) {
                    NDet++;
                    if ( first < 0 ) 
                        first = k;
                }
      
            IDataGrid grid1 = getAreaGrid( DS , grids[ first ] );
            Data d = grid1.getData_entry( 1 , 1 );
            int ny_s = d.getY_values().length;
       
            float[][][][] data = new float[ NDet ]
                                                [ grid1.num_cols() ][ grid1.num_rows() ][ ny_s ];
            
            float[][][][] errors = new float[ NDet ]
                                                [ grid1.num_cols() ][ grid1.num_rows() ][ ny_s ];
            
            float[] distance = new float[ NDet ] ,
                    azim     = new float[ NDet ] ,
                    polar    = new float[ NDet ] ,
                    width    = new float[ NDet ] ,
                    height   = new float[ NDet ] ,
                    depth    = new float[ NDet ];
            
            float[][] orientation = new float[ NDet ][ 6 ];//version null was 3
            
            int[] slot  = new int[ NDet ] ,
                  crate = new int[ NDet ] ,
                  input = new int[ NDet ] ,
                  id    = new int[ NDet ];
            det++;
            
            float[] xvals   = d.getX_scale().getXs();
            float[] row_cm  = new float[ grid1.num_rows() ];
            float[] col_cm  = new float[ grid1.num_cols() ];
            float[] DetNums = new float[ NDet ];
            boolean is_slot = true , 
                   is_crate = true , 
                   is_input = true;
     
            Fill( row_cm ); 
            Fill( col_cm );
      
            for ( int j = first ; j < grids.length ; j++ )
                if ( GridClass[ first ] == GridClass[ j ] ) {
          
                    IDataGrid grid = getAreaGrid( DS , grids[ j ] );
                    for ( int col = 1 ; col <= grid.num_cols() ; col++ )
                        for ( int row = 1 ; row <= grid.num_rows() ; row++)
                         {
             
                            Data db = grid.getData_entry( row , col );

                            System.arraycopy( db.getY_values() , 0 , 
                                     data[ det ][ col - 1 ][ row - 1 ] ,0 ,
                                                                     ny_s );
                            
                            System.arraycopy( db.getErrors() , 0 , 
                                        errors[ det ][ col - 1 ][ row - 1 ] ,
                                        0 , ny_s );
                        }
                    id[ det ] = grids[ j ];
                    DetNums[ det ] = grids[ j ];
                    slot[ det ] = ConvertDataTypes.intValue( d. 
                                          getAttributeValue(Attribute.SLOT ) );
                    
                    crate[ det ] = ConvertDataTypes.intValue( d.
                                         getAttributeValue( Attribute.CRATE ) );
                    
                    input[ det ] = ConvertDataTypes.intValue( d.
                                       getAttributeValue( Attribute.INPUT ) );
                    
                   
                    if ( slot[ det ] < 0 ) 
                        is_slot = false;
             
                    if ( crate[ det ] < 0 ) 
                        is_crate = false;
             
                    if ( input[ det ] < 0 ) 
                        is_input = false;
              
                    float[] coords = ( new Position3D( grid.position() ) ).
                                                          getSphericalCoords();

                    coords = Types.convertToNexus( coords[ 0 ] , coords[ 2 ] , 
                                                                coords[ 1 ] );
                    
                    distance[ det ] = coords[ 0 ];
                    azim[ det ] = coords[ 2 ];
                    polar[ det ] = coords[ 1 ];
          
                   
                    float[] ycoords = grid.x_vec().get();

                   
                    orientation[ det ][ 0 ] = ycoords[ 1 ];
                    orientation[ det ][ 1 ] = ycoords[ 2 ];
                    orientation[ det ][ 2 ] = ycoords[ 0 ];
           
                   
                    ycoords = grid.y_vec().get();
                    orientation[ det ][ 3 ] = ycoords[ 1 ];
                    orientation[ det ][ 4 ] = ycoords[ 2 ];
                    orientation[ det ][ 5 ] = ycoords[ 0 ];
                                 
                    width[ det ] = grid.width();
                    height[ det ] = grid.height();
                    depth[ det ] = grid.depth();
                    det++;

                }// sum all in this GridClass

                //Create the NXdata
            if ( !is_slot ) 
                slot = null;
            if ( !is_crate ) 
                crate = null;
            if ( !is_input ) 
                input = null;
            
            NxWriteNode nxData = nodeEntr.newChildNode( DS.getTitle() +
                    "_G2" + det , "NXdata" );
            dataNodes.add( nxData);
            NxWriteNode nxDetector = nxInstr.newChildNode( DS.getTitle() +
                    "_G2" + det , "NXdetector" );

            if ( CreateG3NXdetectorNode( nxDetector , id , slot , crate ,
                   input , distance , azim , polar , orientation , width ,
                   height , depth , DS , xvals , det) )
                return true;
          
            if ( CreateG3NXdataNode( nxData , data , errors , row_cm , 
                                             col_cm , DetNums , DS , det ) )
                return true;
       
        }//for i < NGridClasses
        return false;

    }
   
    //fills with 1 ,2 ,3 ,etc
    private void Fill( float[] f ) {
        if ( f == null )
            return;
       
        for ( int i = 0 ; i < f.length ; i++ )
            f[ i ] = ( i + 1 );
    }
    
    
   
    private boolean CreateG3NXdetectorNode( NxWriteNode nxDetector , int[]id ,
        int[] slot , int[] crate , int[] input , float[] distance , 
        float[] azim , float[] polar , float[][]orientation , float[] width , 
        float[] height , float[] depth , DataSet DS , float[] tof , int det) {
           
       boolean useLabel = false;
        //Inst_Type inst = new Inst_Type();
        NxWriteNode node = util.writeIA_SDS( nxDetector , "id" ,
               id , NexIO.Inst_Type.makeRankArray( id.length , -1 , -1 , -1 , -1 ) );

        int[] Det = new int[1];
        Det[0]=det;
        node =util.writeIA_SDS( nxDetector , "detector_number" ,
                 Det , NexIO.Inst_Type.makeRankArray( 1 , -1 , -1 , -1 , -1 ) );
        if ( slot != null )
            node = util.writeIA_SDS( nxDetector , "slot" , slot ,
                     NexIO.Inst_Type.makeRankArray( slot.length , -1 , -1 , -1 , -1 ) );
        
        if ( crate != null )
            node = util.writeIA_SDS( nxDetector , "crate" , crate ,
                     NexIO.Inst_Type.makeRankArray( crate.length , -1 , -1 , -1 , -1 ) );
        
        if ( input != null )
            node = util.writeIA_SDS( nxDetector , "input" , input ,
                     NexIO.Inst_Type.makeRankArray( input.length , -1 , -1 , -1 , -1 ) );
        
        node = util.writeFA_SDS( nxDetector , "distance" , distance ,
                 NexIO.Inst_Type.makeRankArray( distance.length , -1 , -1 , -1 , -1 ) );
        
        util.writeStringAttr( node , "units" , "meter" );
          
        node = util.writeFA_SDS( nxDetector , "azimuthal_angle" , azim ,
                 NexIO.Inst_Type.makeRankArray( azim.length , -1 , -1 , -1 , -1 ) );
        
        util.writeStringAttr( node , "units" , "radian" );
         
        node = util.writeFA_SDS( nxDetector , "polar_angle" , polar ,
                 NexIO.Inst_Type.makeRankArray( polar.length , -1 , -1 , -1 , -1 ) );
        
        util.writeStringAttr( node , "units" , "radian" );
     
        SetUpGridInfoNxDet( nxDetector , width , height , depth , orientation );
      
        node = util.writeFA_SDS( nxDetector , "time_of_flight" , tof ,  
                 NexIO.Inst_Type.makeRankArray( tof.length , -1 , -1 , -1 , -1 ) );
        
        util.writeIntAttr( node , "axis" , 4 );
        util.writeStringAttr( node , "units" , DS.getX_units() );
        node.setLinkHandle( DS.getTitle() + "_G2" + det );
      
        return false;
    }



    private boolean CreateG3NXdataNode( NxWriteNode nxData , 
             float[][][][] data , float[][][][] errors , float[]row_cm , 
             float[]col_cm , float[]DetNums , DataSet DS , int det ) {
      
       boolean useLabel = false;
        NxWriteNode node = nxData.newChildNode( "data" , "SDS" );
        int[] ranks = util.setRankArray( data ,false );
       
        ranks = util.setRankArray( data ,false );
        node.setNodeValue( data , Types.Float ,ranks );
        util.writeStringAttr( node , "units" , DS.getX_units() );
        //XXXXXX-Omit for SNS save v
        if( useLabel)
           util.writeStringAttr( node , "label" , DS.getTitle() );
        util.writeStringAttr( node , "link" , DS.getTitle() + "_G2" + det );
        util.writeIntAttr( node , "signal" , 1 );

        nxData.addLink( DS.getTitle() + "_G2" + det , node);
        node = nxData.newChildNode( "errors" , "SDS" );
        ranks = util.setRankArray( errors ,false );
      
        util.setRankArray( errors ,false );
        node.setNodeValue( errors , Types.Float ,ranks );
        util.writeStringAttr( node , "units" , DS.getX_units() );

      
        node = util.writeFA_SDS( nxData , "x_offset" , row_cm , 
                    util.setRankArray( row_cm ,false ) );
        util.writeIntAttr( node , "axis" , 2 );
        util.writeStringAttr( node , "link" , DS.getTitle() + "_G2" + det );
        //XXXXX- omit line below for SNS Save
        if( useLabel)
           util.writeStringAttr( node , "label" , DS.getTitle() );
 
        node = util.writeFA_SDS( nxData , "y_offset" , col_cm , 
                    util.setRankArray( col_cm ,false ) );
        util.writeIntAttr( node , "axis" , 3 );
     
        //node = util.writeFA_SDS( nxData , "DetIDS" , DetNums , 
        //            util.setRankArray( DetNums ,false ) );

        return false;

    }
 
    // Sets one grid per NXdetectr-NXdata pair
    public boolean processDSgrid1( NxWriteNode nodeEntr , NxWriteNode nxInstr , 
        DataSet DS , boolean makelinks , int GridNum , int ngridsTot) {

        IDataGrid grid = getAreaGrid( DS , GridNum );
      

        if ( grid == null ) 
            return false;
      
        if ( grid.num_rows() * grid.num_cols() < MAX_GRID_SIZE )
            return false;
        
        if ( !grid.isData_entered() )
               grid .setData_entries( DS );
       String tag ="";
       if( ngridsTot > 1)
          tag ="_G1"+GridNum;
  
        NxWriteNode Nxdata = nodeEntr.newChildNode( DS.getTitle() + tag  ,
              "NXdata" );
        dataNodes.add( Nxdata);
        
        NxWriteNode Nxdetector = nxInstr.newChildNode( DS.getTitle()+tag , 
              "NXdetector" );
                                          
        //------------------ time_of_flight field -----------------------
        NxWriteNode tofnode = Nxdetector.newChildNode( "time_of_flight" , 
                                                                     "SDS" );

        tofnode.addAttribute( "axis" , NexIO.Inst_Type.makeRankArray( 3 , -1 , 
                          -1 , -1 , -1 ) , NexIO.Types.Int , NexIO.Inst_Type.
                          makeRankArray( 1 , -1 , -1 , -1 , -1 ) );

        tofnode.addAttribute( "units" , ( DS.getX_units() + ( char ) 0 )
            .getBytes() , NexIO.Types.Char , NexIO.Inst_Type.makeRankArray
            ( DS.getX_units().length() + 1 , -1 , -1 , -1 , -1 ) );
          
        float[] offset = new float[ 1 ];

        offset[ 0 ] = 0.0f;
        tofnode.addAttribute( "histogram_offset" , offset ,NexIO.Types.Float ,
                      NexIO.Inst_Type.makeRankArray( 1 , -1 , -1 , -1 , -1 ) );
        
        float[] xvals = DS.getData_entry( 0 ).getX_scale().getXs();

        tofnode.setNodeValue( xvals , NexIO.Types.Float , NexIO.Inst_Type.
                           makeRankArray( xvals.length , -1 , -1 , -1 , -1 ) );
           
        tofnode.setLinkHandle( "NXdata_" + GridNum );
        Nxdata.addLink( "NXdata_" + GridNum, tofnode );

        //----------------------- axis 1 ------------------------------
        NxWriteNode xoffset = Nxdata.newChildNode( "x_offset" , "SDS" );

        xoffset.addAttribute( "axis" , NexIO.Inst_Type.makeRankArray(1 , -1 ,
                 -1 , -1 , -1 ) , NexIO.Types.Int , NexIO.Inst_Type.
                 makeRankArray( 1 , -1 , -1 , -1 , -1 ) );
        
        xoffset.addAttribute( "units" , ( grid.units() + ( char ) 0 ).getBytes() , 
            NexIO.Types.Char , NexIO.Inst_Type.makeRankArray( grid.units()
                     .length() + 1 , -1 , -1 , -1 , -1 ) );
          
        xoffset.addAttribute( "link" , ( DS.getTitle() + "_G1" + GridNum + 
                 ( char ) 0 ).getBytes() , NexIO.Types.Char , NexIO.Inst_Type. 
                 makeRankArray(( DS.getTitle() + "_G1" + GridNum ).length() +
                 1 , -1 , -1 , -1 , -1 ) );
          
        float[]  col_cm;
     
        col_cm = new float[ grid.num_cols() ];
        for ( int j = 0 ; j < grid.num_cols() ; j++ )
            col_cm[ j ] = grid.x( 1 , j+1 );
        
        xoffset.setNodeValue( col_cm , NexIO.Types.Float , NexIO.Inst_Type.
                         makeRankArray( col_cm.length , -1 , -1 , -1 , -1 ) );
              
        //--------------------------axis 2 --------------------------------
        NxWriteNode yoffset = Nxdata.newChildNode( "y_offset" , "SDS" );

        yoffset.addAttribute( "axis" , NexIO.Inst_Type.makeRankArray
            ( 2 , -1 , -1 , -1 , -1 ) , NexIO.Types.Int , NexIO.Inst_Type.
            makeRankArray( 1 , -1 , -1 , -1 , -1 ) );
         
        yoffset.addAttribute( "units" , ( grid.units() + ( char ) 0 )
            .getBytes() , NexIO.Types.Char , NexIO.Inst_Type.makeRankArray
            ( grid.units().length() + 1 , -1 , -1 , -1 , -1 ) );
         
        float[]  row_cm;
     
        row_cm = new float[ grid.num_rows() ];
        for ( int j = 0 ; j < grid.num_rows() ; j++ )
            row_cm[ j ] = grid.y( j+1 , 1 );
        
        yoffset.setNodeValue( row_cm , NexIO.Types.Float , NexIO.Inst_Type.
                          makeRankArray( row_cm.length , -1 , -1 , -1 , -1 ) );

        //----------------------  data ---------------------------
        int ny_s = grid.getData_entry( 1 , 1 ).getY_values().length;
        int numRows = grid.num_rows();
        int numCols = grid.num_cols();
        float[][][] data = new float[ grid.num_cols() ][ grid.num_rows() ]
                                                                  [ ( ny_s ) ];
     
        float[][][] errs = new float[ grid.num_cols() ][ grid.num_rows() ]
                                                                 [ ( ny_s ) ];
        for ( int col = 1 ; col <= grid.num_cols() ; col++ ) 
            for ( int row = 1 ; row <= grid.num_rows() ; row++ )
            {
          
                float[] yvalues = grid.getData_entry( row , col )
                                                           .getY_values();

                System.arraycopy( yvalues , 0 , data[ col - 1 ][ row - 1 ] , 
                                                                   0 , ny_s );
                
                float[] errvalues = grid.getData_entry( row , col ).getErrors();
                if( errvalues != null  && errs != null)
                   System.arraycopy( yvalues , 0 , errs[ col - 1 ][ row - 1 ] , 
                                                                   0 , ny_s );
                else
                   errs = null;
            }
     
        NxWriteNode dataNode = Nxdata.newChildNode( "data" , "SDS" );

        dataNode.addAttribute( "signal" , NexIO.Inst_Type.makeRankArray
            ( 1 , -1 , -1 , -1 , -1 ) , NexIO.Types.Int , NexIO.Inst_Type.
            makeRankArray( 1 , -1 , -1 , -1 , -1 ) );
          
        dataNode.addAttribute( "units" , ( DS.getY_units() + ( char ) 0 ) 
                 .getBytes() ,NexIO.Types.Char , NexIO.Inst_Type.
                 makeRankArray( DS.getY_units().length() + 1 , -1 , -1 , -1 , 
                          -1 ) );
        //XXXX delete lines below for SNS Save  
        if( !tag.equals(""))
        dataNode.addAttribute( "label" , ( DS.getTitle() + ( char ) 0 ) 
                .getBytes() ,NexIO.Types.Char , NexIO.Inst_Type.makeRankArray
                 ( DS.getTitle().length() + 1 , -1 , -1 , -1 , -1 ) );
         
        dataNode.setNodeValue( data , NexIO.Types.Float , NexIO.Inst_Type.
                           makeRankArray(  numCols ,numRows , ny_s , -1 , -1 ) );

        dataNode = Nxdata.newChildNode( "errors" , "SDS" );
        
        dataNode.addAttribute( "units" , ( DS.getY_units() + ( char ) 0 ) 
                 .getBytes() ,NexIO.Types.Char , NexIO.Inst_Type.makeRankArray
                 ( DS.getY_units().length() + 1 , -1 , -1 , -1 , -1 ) );
        
        if( errs != null)
           dataNode.setNodeValue( errs , NexIO.Types.Float , NexIO.Inst_Type.
                 makeRankArray(  numCols,numRows ,  ny_s , -1 , -1 ) );

        //-------------------NxDetector fields -----------------------------
    
        NxWriteNode distanceNode = Nxdetector.newChildNode( "distance" , "SDS" );
        Vector3D position = grid.position();
        float[] rtp = ( new Position3D( position ) ).getSphericalCoords();

        rtp = Types.convertToNexus( rtp[ 0 ] , rtp[ 2 ] , rtp[ 1 ] );              
        float[] res = new float[ 1 ];

        res[ 0 ] = rtp[ 0 ];
        distanceNode.setNodeValue( res , NexIO.Types.Float , NexIO.Inst_Type.
                                      makeRankArray( 1 , -1 , -1 , -1 , -1 ) );
     
        distanceNode.addAttribute( "units" , ( DS.getX_units() + ( char ) 0 ).
                 getBytes() , NexIO.Types.Char , NexIO.Inst_Type.makeRankArray(
                 DS.getX_units().length() + 1 , -1 , -1 , -1 , -1 ) );

        NxWriteNode azimuthNode = Nxdetector.newChildNode( "azimuthal_angle" , 
                "SDS" );

        res = new float[ 1 ];
        res[ 0 ] = rtp[ 2 ];
        azimuthNode.setNodeValue( res , NexIO.Types.Float , NexIO.Inst_Type.
                 makeRankArray( 1 , -1 , -1 , -1 , -1 ) );
        
        azimuthNode.addAttribute( "units" , ( "radian" + (char) 0 ).getBytes() ,
            NexIO.Types.Char , NexIO.Inst_Type.makeRankArray( 7 ,
                -1 , -1 , -1 , -1 ) );

        NxWriteNode polarNode = Nxdetector.newChildNode( "polar_angle" ,
                                                                      "SDS" );

        res = new float[ 1 ];
        res[ 0 ] = rtp[ 1 ];
        polarNode.setNodeValue( res , NexIO.Types.Float , NexIO.Inst_Type.
                                     makeRankArray( 1 , -1 , -1 , -1 , -1 ) );
        
        polarNode.addAttribute( "units" , ( "radian" + ( char ) 0).getBytes() ,
            NexIO.Types.Char , NexIO.Inst_Type.makeRankArray( 7 ,
                -1 , -1 , -1 , -1 ) );

        int[][] IDs = new int[ numRows ][ numCols ];

        for ( int i = 1 ; i <= numRows ; i++ )
            for ( int j = 1 ; j <= numCols ; j++ )
                IDs[ i - 1 ][ j - 1 ] = grid.getData_entry( i , j ).getGroup_ID();
        
        NxWriteNode idNode = Nxdetector.newChildNode( "id" , "SDS" );

        idNode.setNodeValue( IDs , NexIO.Types.Int , NexIO.Inst_Type.
                           makeRankArray( numRows , numCols , -1 , -1 , -1 ) );
        int[] Det = new int[1];
        Det[0]=grid.ID();
        NxWriteNode detNode = Nxdetector.newChildNode( "detector_number" , "SDS" );
        detNode.setNodeValue(  Det ,  NexIO.Types.Int , NexIO.Inst_Type.
                           makeRankArray( 1 , -1 , -1 , -1 , -1 ) );
        float[] width  = new float[ 1 ] ,
                height = new float[ 1 ] ,
                depth  = new float[ 1 ];
        
        float[][] orientation = new float[ 1 ][ 6 ];

        width[ 0 ] = grid.width();
        depth[ 0 ] = grid.depth();
        height[ 0 ] = grid.height();
        float[] v = grid.x_vec().get();

        orientation[ 0 ][ 0 ] = v[ 1 ];
        orientation[ 0 ][ 1 ] = v[ 2 ];
        orientation[ 0 ][ 2 ] = v[ 0 ];
     
        v = grid.y_vec().get();
        orientation[ 0 ][ 3 ] = v[ 1 ];
        orientation[ 0 ][ 4 ] = v[ 2 ];
        orientation[ 0 ][ 5 ] = v[ 0 ];
     
        this.SetUpGridInfoNxDet( Nxdetector , width , height , depth , 
                                                              orientation );

       
    

    
        float solidAngle[][] = new float[ numRows ][ numCols ];
        float rawAngle[][] = new float[ numRows ][ numCols ];
        float Tot_Count[][] = new float[ numRows ][ numCols ];

        int[][] slot = new int[ numRows ][ numCols ];
        int[][] crate = new int[ numRows ][ numCols ];
        int[][] input = new int[ numRows ][ numCols ];
        Object XX;

        for ( int row = 1 ; row <= numRows ; row++ )
            for ( int col = 1 ; col <= numCols ; col++ ) {
        
                Data DB = grid.getData_entry( row , col );

                XX = DB.getAttributeValue( Attribute.SOLID_ANGLE );
                if ( XX == null ) 
                    solidAngle = null;
                if ( ( XX != null ) && ( solidAngle != null ) ) {
        
                    float F = ConvertDataTypes.floatValue( XX );

                    if ( Float.isNaN( F ) )
                        solidAngle[ row - 1 ][ col - 1 ] = F;
                    else 
                        solidAngle[  row - 1 ][ col - 1  ] = -1.0f;
                } else if ( solidAngle != null )
                    solidAngle[  row - 1 ][ col - 1  ] = -1.0f;

                XX = DB.getAttributeValue( Attribute.RAW_ANGLE );
                if ( XX == null )
                    rawAngle = null;
                if ( ( XX != null ) && ( rawAngle != null ) ) {
       
                    float F = ConvertDataTypes.floatValue( XX );

                    if ( Float.isNaN( F ) )
                        rawAngle[ row - 1 ][ col - 1 ] = F;
                    else 
                        rawAngle[  row - 1 ][ col - 1  ] = -1.0f;
                } else if ( rawAngle != null )
                    rawAngle[ row - 1 ][ col - 1  ] = -1.0f;

                XX = DB.getAttributeValue( Attribute.TOTAL_COUNT );
                if ( XX == null )
                    Tot_Count = null;
                if ( ( XX != null ) && ( Tot_Count != null ) ) {
        
                    float  F = ConvertDataTypes.floatValue( XX );

                    if ( Float.isNaN( F ) )
                        Tot_Count[ row - 1 ][ col - 1   ] = -1.0f;
                    else if ( F >= 0 )
                        Tot_Count[ row - 1 ][ col - 1  ] = F;
                    else 
                        Tot_Count[ row - 1 ][ col - 1  ] = -1.0f;
                } else if ( Tot_Count != null )
                    Tot_Count[ row - 1 ][ col - 1  ] = -1.0f;

                    //slot----
                XX = DB.getAttributeValue( Attribute.SLOT );
                if ( XX == null )
                    slot = null;
                if ( ( XX != null ) && ( slot != null ) ) {
        
                    float[] F = ConvertDataTypes.floatArrayValue( XX );

                    slot[ row - 1 ][ col - 1 ] = -1;
                    if ( F != null )
                        if ( F.length > 0 )
                            slot[ row - 1 ][ col - 1 ] = ( int ) ( F[ 0 ] );
        
                } else if ( slot != null )
                    slot[ row - 1 ][ col - 1 ] = -1;

                    //crate
                XX = DB.getAttributeValue( Attribute.CRATE );
                if ( XX == null ) 
                    crate = null;
                if ( ( XX != null ) && ( crate != null ) ) {
        
                    float[ ] F = ConvertDataTypes.floatArrayValue( XX );

                    crate[ row - 1 ][ col - 1 ] = -1;
                    if ( F != null )
                        if ( F.length > 0 )
                            crate[ row - 1 ][ col - 1 ] = ( int ) ( F[ 0 ] );
                } else if ( crate != null )
                    crate[ row - 1 ][ col - 1 ] = -1;

                    //input
                XX = DB.getAttributeValue( Attribute.INPUT );
                if ( XX == null ) 
                    input = null;
                if ( ( XX != null ) && ( input != null ) ) {
       
                    float[] F = ConvertDataTypes.floatArrayValue( XX );

                    input[ row - 1 ][ col - 1 ] = -1;
                    if ( F != null )
                        if ( F.length > 0 )
                            input[ row - 1 ][ col - 1 ] = ( int ) ( F[ 0 ] );
                } else if ( input != null )
                    input[ row - 1 ][ col - 1 ] = -1;
            }//For all groups

        if ( slot != null ) {
            NxWriteNode nn = Nxdetector.newChildNode( "slot" , "SDS" );

            nn.setNodeValue( slot , NexIO.Types.Int ,
                NexIO.Inst_Type.makeRankArray( slot.length , -1 , -1 ,
                -1 , -1 ) );
      
        }

        if ( crate != null ) {
            NxWriteNode nn = Nxdetector.newChildNode( "crate" , "SDS" );

            nn.setNodeValue( crate , NexIO.Types.Int , NexIO.Inst_Type.
                            makeRankArray( numRows,numCols , -1 , -1 , -1 ) );
     
        }
        if ( input != null ) {
            NxWriteNode nn = Nxdetector.newChildNode( "input" , "SDS" );

            nn.setNodeValue( input , NexIO.Types.Int , NexIO.Inst_Type.
                              makeRankArray( numRows,numCols , -1 , -1 , -1 ) );
      
        }
        if ( solidAngle != null ) {
            NxWriteNode nn = Nxdetector.newChildNode( "solid_angle" , "SDS" );

            nn.setNodeValue( solidAngle , NexIO.Types.Float , NexIO.Inst_Type.
                             makeRankArray( numRows,numCols , -1 , -1 , -1 ) );
            
            nn.addAttribute( "units" , ( "radian" + ( char ) 0 ).getBytes() , 
                     NexIO.Types.Char , NexIO.Inst_Type.makeRankArray( 8 , -1 ,
                     -1 , -1 , -1 ) );
            
        }
        if ( rawAngle != null ) {
            NxWriteNode nn = Nxdetector.newChildNode( "raw_angle" , "SDS" );

            nn.setNodeValue( rawAngle , NexIO.Types.Float , NexIO.Inst_Type.
                         makeRankArray( numRows,numCols , -1 , -1 , -1 ) );
            
            nn.addAttribute( "units" , ( "degree" + ( char ) 0 ).getBytes() , 
                     NexIO.Types.Char ,NexIO.Inst_Type.makeRankArray( 8 , -1 ,
                    -1 , -1 , -1 ) );
            
        }

        if ( Tot_Count != null ) {
           
            NxWriteNode nn = Nxdetector.newChildNode( "total_count" , "SDS" );

            nn.setNodeValue( Tot_Count , NexIO.Types.Float , NexIO.Inst_Type.
                        makeRankArray( numRows,numCols  , -1 , -1 , -1 ) );
    
        }

               return false;
 
    }
  
    
    
    
    
    private void SetUpGridInfoNxDet( NxWriteNode nxDetector , float[] width , 
        float[] height , float[] depth , float[][] orientation ) {
 
        //Inst_Type inst = new Inst_Type();
        NxWriteNode geomNode = nxDetector.newChildNode( "geometry" , 
                                                              "NXgeometry" ); 

        if ( ( width != null ) && ( height != null ) && ( depth != null ) ) {
    
            float[] size = new float[ 3 * width.length ];

            for ( int i = 0 ; i < width.length; i++ ) {
                size[ 3 * i ] = width[ i ];
                size[ 3 * i + 1 ] = depth[ i ];
                size[ 3 * i + 2 ] = height[ i ];  
            }
     
            NxWriteNode NxshapeNode = geomNode.newChildNode( "shape" , 
                                                                 "NXshape" );
            
            NxWriteNode shapeNode = NxshapeNode.newChildNode( "shape" , "SDS" );

            shapeNode.setNodeValue( ( "nxbox" + ( char ) 0 ).getBytes() , 
                      Types.Char , NexIO.Inst_Type.makeRankArray( 6 , -1 , -1 , -1 , -1 ) );
            
            shapeNode = null;
             
            NxWriteNode node = util.writeFA_SDS( NxshapeNode , "size" , size , 
                     NexIO.Inst_Type.makeRankArray( width.length , 3 , -1 , -1 , -1 ) );

            util.writeStringAttr( node , "units" , "meter" );
     
        }       

       
        if ( orientation != null ) {
  
            NxWriteNode orientNode = geomNode.newChildNode( "orientation" , 
                    "NXorientation" );
            float[] orientt = new float[ orientation.length * 6 ];

            for ( int i = 0 ; i < orientation.length ; i++ )
                System.arraycopy( orientation[ i ] , 0 , orientt , 6 * i , 6 );
            
            util.writeFA_SDS( orientNode , "value" , orientt , NexIO.Inst_Type. 
                     makeRankArray(orientation.length , 6 , -1 , -1 , -1 ) );                                                  
        }   
    }
  
    
    
    
    /**
     * Returns the IDataGrid associated with the given data block
     * @param db  the data block with the IDataGrid
     * @return  The IDataGrid associated with this data block or null if none
     */
    public static IDataGrid getDataGrid( Data db ) {

        PixelInfoList pilist = ( PixelInfoList ) db.getAttributeValue(  
                Attribute.PIXEL_INFO_LIST );
                                    
        if ( pilist == null )
            return null;
         
        IPixelInfo pinf = pilist.pixel( 0 );

        if ( pinf == null ) 
            return null;
         
        return pinf.DataGrid();
    }

    
    
    
    
    
    
    /**
     *  Returns the IDataGrid in the DataSet DS with grid number = gridNum
     * @param DS    The data set with the IDataGrid
     * @param gridNum  The grid number for the grid
     * @return   The IDataGrid in the data set DS with the given gridNum or null. 
     */
    public static IDataGrid getAreaGrid( DataSet DS , int gridNum ) {
    
        for ( int i = 0 ; i < DS.getNum_entries() ; i++ ) {
       
            IDataGrid grid = getDataGrid( DS.getData_entry( i ) );

            if ( gridNum == grid.ID() )
                return grid;
          
        }
        return null;
    }
  
    
    
    
    
    
    /**
     * Returns the set of all grid numbers for DataGrids in the given DataSet. 
     * These include Grids with all dimensions , not just area grids
     * @param DS  The DataSet with the grids
     * @return  An array of grid numbers of the grids in this data set. It may 
     *          have a length of 1. 
     */
    public static int[] getAreaGrids( DataSet DS ) {
       
        Vector V = new Vector( DS.getNum_entries() );
        IDataGrid grid = null;

        for ( int i = 0 ; i < DS.getNum_entries() ; i++ ) {
            IDataGrid dgrid = getDataGrid( DS.getData_entry( i ) );

            if ( dgrid != null ) { 
                Integer id = new Integer( dgrid.ID() );

                if ( V.indexOf( id ) < 0 ) 
                    V.addElement( new Integer( dgrid.ID() ) );
          
                if ( grid == null )
                    grid =  dgrid;
            }
        }
        
        int[] Res = new int[ V.size() ];

        for ( int i = 0 ; i < Res.length ; i++ )
            Res[ i ] = ( ( Integer ) ( V.elementAt( i ) ) ).intValue();
      
        java.util.Arrays.sort( Res );
        if( grid instanceof UniformGrid)
           UniformGrid.setDataEntriesInAllGrids( DS );
    
        return Res;

    }
  
    
    
    
    
    
    
    public static void main( String args[] ) {
        NxWriteData nw = new NxWriteData( 1 );
        IsawGUI.Util ut = new IsawGUI.Util();
        DataSet dss[];
    
        dss = ut.loadRunfile( "C:\\SampleRuns\\gppd998.run" );
   
        float xvals[];

        xvals = nw.MergeXvals( 0 , dss[ 1 ] , null );
        System.out.println( "*****final xval list= *****" );
        System.out.println( StringUtil.toString( xvals ) );
        Data DB = dss[ 1 ].getData_entry( 0 );
        float ynew[] , xold[] , yold[];

        ynew = nw.Rebinn( DB , xvals );
        XScale XX = DB.getX_scale();

        xold = XX.getXs();
        yold = DB.getY_values();
        System.out.println( "xold= " + StringUtil.toString( xold ) );
        System.out.println( "" );
        System.out.println( "xbew= " + StringUtil.toString( xvals ) );
        System.out.println( "" );
        System.out.println( "yold= " + StringUtil.toString( yold ) );
        System.out.println( "" );
        System.out.println( "ynew= " + StringUtil.toString( ynew ) );
        System.out.println( "" );
    }
}
