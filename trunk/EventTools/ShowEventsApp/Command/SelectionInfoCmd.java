package EventTools.ShowEventsApp.Command;

import gov.anl.ipns.MathTools.Geometry.Vector3D;

public class SelectionInfoCmd
{
   private float counts;
   private int   det_num;
   private int   col;
   private int   row;
   private int   page;
   
   private Vector3D hkl;
   private Vector3D Qxyz;

   private float raw_Q;
   private float d_spacing;
   private float tof;
   private float e_mev;
   private float wavelength;
   
   public SelectionInfoCmd( float    counts,
                            int      det_num,
                            int      col,
                            int      row,
                            int      page,
                            Vector3D hkl,
                            Vector3D Qxyz,
                            float    raw_Q,
                            float    d_spacing,
                            float    tof,
                            float    e_mev,
                            float    wavelength )
   {
      this.counts = counts;
      this.det_num    = det_num;
      this.col        = col;
      this.row        = row;
      this.page       = page; 
      this.hkl        = new Vector3D( hkl );
      this.Qxyz       = new Vector3D( Qxyz );
      this.raw_Q      = raw_Q;
      this.d_spacing  = d_spacing;
      this.tof        = tof;
      this.e_mev      = e_mev;
      this.wavelength = wavelength;
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

   public Vector3D getHKL()
   {
      return new Vector3D( hkl );
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


   public String toString()
   {
      return "\nCounts      : " + getCounts() +
             "\nDet Num     : " + getDetNum() + 
             "\nPage        : " + getHistPage() + 
             "\nHKL         : " + getHKL() + 
             "\nQxyz        : " + getQxyz() + 
             "\nRaw Q       : " + getRaw_Q() + 
             "\nD Spacing   : " + getD_spacing() + 
             "\nTOF         : " + getTof() + 
             "\nE(mev)      : " + getE_mev() + 
             "\nWavelength  : " + getWavelength(); 
   }
}
