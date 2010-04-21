package EventTools.ShowEventsApp.Command;

import gov.anl.ipns.MathTools.Geometry.Vector3D;

public class SelectionInfoCmd
{
   private float counts;
   private int   det_num;
   private int   col;
   private int   row;
   private int   page;
   private int   seq_num;
   
   private Vector3D hkl;
   private Vector3D Qxyz;

   private float raw_Q;
   private float d_spacing;
   private float two_theta;
   private float tof;
   private float e_mev;
   private float wavelength;

   private Vector3D projected_hkl;
   private float    psi_deg;
   private float    tilt_deg;

   
   public SelectionInfoCmd( float    counts,
                            int      det_num,
                            int      col,
                            int      row,
                            int      page,
                            int      seq_num,
                            Vector3D hkl,
                            Vector3D Qxyz,
                            float    raw_Q,
                            float    d_spacing,
                            float    two_theta,
                            float    tof,
                            float    e_mev,
                            float    wavelength )
   {
      this.counts = counts;
      this.det_num    = det_num;
      this.col        = col;
      this.row        = row;
      this.page       = page; 
      this.seq_num    = seq_num;
      this.hkl        = new Vector3D( hkl );
      this.Qxyz       = new Vector3D( Qxyz );
      this.raw_Q      = raw_Q;
      this.d_spacing  = d_spacing;
      this.two_theta  = two_theta;
      this.tof        = tof;
      this.e_mev      = e_mev;
      this.wavelength = wavelength;

      this.projected_hkl = new Vector3D();
      this.psi_deg       = 0.0f;
      this.tilt_deg      = 0.0f;
   }

   
   public float getCounts()
   {
      return counts;
   }

   public void setCounts( float counts )
   {
      this.counts = counts;
   }

   public float getDetNum()
   {
      return det_num;
   }

   public float getCol()
   {
      return col;
   }

   public float getRow()
   {
      return row;
   }

   public float getHistPage()
   {
      return page;
   }

   public void setHistPage( int page )
   {
      this.page = page;
   }


   public int getSeqNum()
   {
      return seq_num;
   }
   
   public Vector3D getHKL()
   {
      return new Vector3D( hkl );
   }

   public void setHKL( Vector3D hkl )
   {
     this.hkl = new Vector3D( hkl );
   }

   public Vector3D getQxyz()
   {
      return new Vector3D( Qxyz );
   }

   public float getRaw_Q()
   {
      return raw_Q;
   }

   public float getD_spacing()
   {
      return d_spacing;
   }
   

   public float getTwo_theta()
   {
      return two_theta;
   }

   public float getTof()
   {
      return tof;
   }

   public float getE_mev()
   {
      return e_mev;
   }

   public float getWavelength()
   {
      return wavelength;
   }

   public Vector3D getProjectedHKL()
   {
      return new Vector3D( projected_hkl );
   }

   public void setProjectedHKL( Vector3D projected_hkl )
   {
      this.projected_hkl = new Vector3D( projected_hkl ); 
   }

   public float getPSI()
   {
      return psi_deg;
   }

   public void setPSI( float psi_deg )
   {
     this.psi_deg = psi_deg;
   }
   

   public void setSeqNum( int seq_num )
   {
     this.seq_num = seq_num;
   }

   public float getTilt()
   {
      return tilt_deg;
   }

   public void setTilt( float tilt_deg )
   {
     this.tilt_deg = tilt_deg;
   }

   public String toString()
   {
      return "\nCounts      : " + getCounts() +
             "\nDet Num     : " + getDetNum() + 
             "\nPage        : " + getHistPage() + 
             "\nSeq Num     : " + getSeqNum() + 
             "\nHKL         : " + getHKL() + 
             "\nQxyz        : " + getQxyz() + 
             "\nRaw Q       : " + getRaw_Q() + 
             "\nD Spacing   : " + getD_spacing() + 
             "\n2 theta     : " + getTwo_theta() + 
             "\nTOF         : " + getTof() + 
             "\nE(mev)      : " + getE_mev() + 
             "\nWavelength  : " + getWavelength() +
             "\nProj HKL    : " + getProjectedHKL() +
             "\nPSI(deg)    : " + getPSI() +
             "\nTilt(deg)   : " + getTilt(); 
   }
}
