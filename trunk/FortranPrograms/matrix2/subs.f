!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

C***************************************************************
C*******************   SUBROUTINE ABC   ************************
C***************************************************************


      SUBROUTINE ABC (U,A,B,C,ALPHA,BETA,GAMMA,VOL)


C***  CALCULATE CELL PARAMETERS FROM THE U MATRIX.
C***  THE INVERSE G MATRIX IS EQUAL TO THE PRODUCT OF U
C***  AND THE TRANSPOSE OF U.
C***                              A.J. SCHULTZ   12/8/80

!	Linux version: February, 2002		A. J. Schultz
!       SNS version: March, 2009		A. J. Schultz

      DIMENSION U(3,3),UT(3,3),G(3,3),GI(3,3)
      DATA RAD /57.295779513/
      
      DO 600 I=1,3
      DO 600 J=1,3
600   UT(I,J) = U(J,I)

      CALL MATMULx (U,UT,GI)
      
	call matin2(GI, G)
      
      A = SQRT(G(1,1))
      B = SQRT(G(2,2))
      C = SQRT(G(3,3))
	ARG1 = G(2,3)/(B*C)
	ARG2 = G(1,3)/(A*C)
	ARG3 = G(1,2)/(A*B)
      ALPHA = ( ACOS( ARG1 ) )  *  RAD
      BETA  = ( ACOS( ARG2 ) )  *  RAD
      GAMMA = ( ACOS( ARG3 ) )  *  RAD

C***  CALCULATE VOLUME

      CSTAR = SQRT(GI(3,3))
      VOL = A * B * SIN(GAMMA/RAD) * (1.0/CSTAR)

      RETURN
      END

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!



C**********   SUBROUTINE CENTER   **********
C
      SUBROUTINE CENTER (H,K,L,ICELL,IEX)
C
C***  THIS SUBROUTINE TESTS FOR EXTINCTIONS DUE TO A CENTERED
C***  LATTICE.
C
C***  A.J. SCHULTZ   1/7/81
C
C
      INTEGER H,HK,HL,HKL,K,L,KL,IEX,ICELL
C
      IEX = 0
C
      GO TO (10,20,30,40,50,60,70),ICELL
C
C***  PRIMITIVE LATTICE
C
10    RETURN
C
C***  A-CENTERED LATTICE
C
20    KL = IABS(K + L )
      IF (MOD(KL,2) .NE. 0) IEX=1
      RETURN
C
C***  B-CENTERED LATTICE
C
30    HL = IABS(H + L )
      IF (MOD(HL,2) .NE. 0) IEX=1
      RETURN
C
C***  C-CENTERED LATTICE
C
40    HK = IABS(H + K)
      IF (MOD(HK,2) .NE. 0) IEX=1
      RETURN
C
C***  F-CENTERED LATTICE
C
50    HK =IABS( H + K  )
      HL =IABS( H + L  )
      KL =IABS( K + L  )
      IF (MOD(HK,2).NE. 0 .OR. MOD(HL,2).NE. 0 .OR. MOD(KL,2).NE. 0)
     1     IEX = 1
      RETURN
C
C***  I-CENTERED LATTICE
C
60    HKL = IABS(H + K + L )
      IF (MOD(HKL,2) .NE. 0) IEX=1
      RETURN
C
C***  R-CENTERED
C
   70 HKL=IABS(-H+K+L)
      IF (MOD(HKL,3) .NE. 0) IEX=1
      RETURN
C
      END



!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


	SUBROUTINE CROSS (A,B,C)

C***	Calculate the cross product C = A X B

	DIMENSION A(3),B(3),C(3)

	C(1) = A(2)*B(3) - A(3)*B(2)
	C(2) = A(3)*B(1) - A(1)*B(3)
	C(3) = A(1)*B(2) - A(2)*B(1)

	RETURN
	END

	
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
	
C
C**********   SUBROUTINE TO INVERT A 3X3 MATRIX   **********
C
      SUBROUTINE MATIN2 (MX, MXI)
C
      REAL*8 A(3,3), AI(3,3), D
      REAL*4 MX(3,3), MXI(3,3)
C
      DO 10 I=1,3
      DO 10 J=1,3
        A(I,J) = DBLE(MX(I,J))
10    CONTINUE

      I = 0
      
      DO 1 J=1,3
        M = J+1
        IF (M.GT.3) M=M-3
        N=J+2
        IF (N.GT.3) N=N-3
        DO 1 I=1,3
          K=I+1
          IF (K.GT.3) K=K-3
          L=I+2
          IF (L.GT.3) L=L-3
          AI(I,J) = A(M,K)*A(N,L) - A(M,L)*A(N,K)
1     CONTINUE

      D = AI(1,1)*A(1,1) + AI(2,1)*A(1,2) + A(1,3)*AI(3,1)
      IF (DABS(D) .LT. 0.00000001) write(*, 1000)
      
      DO 3 I=1,3
        DO 3 J=1,3
          AI(I,J) = AI(I,J)/D
          MXI(I,J) = SNGL(AI(I,J))
3     CONTINUE

      RETURN
1000  FORMAT (' INVERSION OF MATRIX IS INDETERMINANT')
      END


	
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


C
C***  SUBROUTINE TO MULTIPLY TWO 3X3 MATRICES  ***
C
      SUBROUTINE MATMULx (A, B, C)
C
      DIMENSION A(3,3), B(3,3), C(3,3)
C
      DO 10 I=1,3
      DO 10 J=1,3
10    C(I,J) = 0.0
C
      DO 100 I=1,3
      DO 100 J=1,3
      DO 100 K=1,3
100   C(I,J) = A(I,K) * B(K,J)  +  C(I,J)
C
      RETURN
      END

      

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

      

	Subroutine qvec	(twoth, az, wl, qx, qy, qz)
	
!  Calculate q vector qx,qy,qz from the two-theta and azimuthal
!  angles and the inverse d-spacing.

!  A.J.Schultz  February 2009
	
	wlstar = 1.0/wl
	
	qx = wlstar * cos(az) * sin(twoth)
	qy = wlstar * sin(az) * sin(twoth)
	qz = wlstar * (cos(twoth) - 1.0)
	
	return
	end



!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


	SUBROUTINE READREFL_SNS_HEADER (IUNIT, IEOF)

!!!	This subroutine will read the first 6 lines of a peaks or
!!!	integrate file with the SNS format.
!!!	A. J. Schultz	June, 2008

!	The first variable of each record is ISENT.

!	Linux version, January 2002, A.Schultz

!	ISENT = 0 --> variable name list for describing the histogram
!	      = 1 --> variable values of above list
!	      = 2 --> variable name list for a reflection
!	      = 3 --> variable values of above list
!	      = 4 --> variable name list for parameters for detectors
!	      = 5 --> variable values of parameters for detectors
!	      = 6 --> variable name list: L1    T0_SHIFT
!	      = 7 --> variable values: L1    T0_SHIFT

!	IEOF = 0 --> not end-of-file
!	     = 1 --> end-of-file


	INTEGER DETNUM(100), DN				!DN added 4/21/08

	REAL	L1

	CHARACTER EXPNAM*14

! Labelled common for 100 SNS detectors		3/21/08
	common /detect/ nrows(100), ncols(100), width(100),
     1	HEIGHT(100), DEPTH(100), DIST(100), CenterX(100),
     2	CenterY(100), CenterZ(100), BaseX(100), BaseY(100),
     3	BaseZ(100), UpX(100), UpY(100), UpZ(100), nod
	COMMON    /INSPAR/ L1,TZERO
	COMMON    /DATA11/ NRUN,DETNUM,dn,EXPNAM



	CHARACTER*160 TEXT

	IEOF=0

	write (*, 50)
50	format(//,'Reflection file header:')

!  Read the first line
	READ (IUNIT, 100, END=999) TEXT
100	FORMAT(A)
!	write (*, 100) TEXT

!  Read the second line, ISENT = 6
	READ (IUNIT,100,END=999) TEXT
!	write (*, 100) TEXT

!  Read the third line, ISENT = 7
	READ (IUNIT,100,END=999) TEXT

	READ (TEXT,150) ISENT
150	FORMAT(I1)

! ISENT=7
	IF (ISENT.EQ.7) THEN
		READ (text, *) ISENT, L1, TZERO
	END IF

!	write (*, 100) TEXT

!  Read the fourth line, ISENT = 4
	READ (IUNIT,100,END=999) TEXT

!  Read lines with ISENT = 5
	nod = 0				!number of detectors
300	read (iunit, 100) text      	!ISENT=5

	READ (TEXT,150) ISENT
          IF (ISENT.NE.5) THEN	!End of reading detector parameters
                  BACKSPACE (IUNIT)
!                  write (*, *) ' '
!                  write (*, *) 'There are ',nod,' detectors.'
!                  write (*, *)
                  RETURN
          END IF

!	write (*, 100) TEXT

	  nod = nod + 1
	  i = nod
	  READ (TEXT, *) ISENT, detnum(i), NROWS(i), NCOLS(i),
     1	WIDTH(i), HEIGHT(i), DEPTH(i), DIST(i),
     2	CenterX(i), CenterY(i), CenterZ(i),
     3	BaseX(i), BaseY(i), BaseZ(i),
     4	UpX(i), UpY(i), UpZ(i)
         GO TO 300

999	IEOF=1	!END OF FILE
	RETURN
	END


!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!



	SUBROUTINE READREFL_SNS (IUNIT, IEOF)

!!!	Read a peaks or integrate file with the SNS format.
!!!	A. J. Schultz	April, 2008


!	Read a reflection from a reflection file.
!	The first variable of each record is ISENT.

!	Linux version, January 2002, A.Schultz

!	ISENT = 0 --> variable name list for describing the histogram
!	      = 1 --> variable values of above list
!	      = 2 --> variable name list for a reflection
!	      = 3 --> variable values of above list
!	      = 4 --> variable name list for parameters for detectors
!	      = 5 --> variable values of parameters for detectors
!	      = 6 --> variable name list: L1    T0_SHIFT
!	      = 7 --> variable values: L1    T0_SHIFT

!	IEOF = 0 --> not end-of-file
!	     = 1 --> end-of-file


	INTEGER H
	INTEGER REFLAG,SEQNUM
	INTEGER DETNUM(100), DN				!DN added 4/21/08
	REAL	INTI,L2
	CHARACTER EXPNAM*14

! Labelled common for 100 SNS detectors		3/21/08
	COMMON    /PEAKS/  col, row, chan, L2, twoth, az, dsp
	COMMON    /MONI/   MONCNT
	COMMON    /SET/    CHI, PHI, OMEGA, DETA
	COMMON    /DATA11/ NRUN,DETNUM,dn,EXPNAM
	COMMON    /DATA12/ SEQNUM, H, K, L, X, Y, WL,
     1			   IPKOBS, INTI, SIGI, REFLAG

	CHARACTER*160 TEXT


	READ (IUNIT,100,END=999) TEXT
100	FORMAT(A)

	IEOF=0

	READ (TEXT,150) ISENT
150	FORMAT(I1)

! ISENT=3 ---> reflection
	IF (ISENT.EQ.3) THEN				!Reflection record
		READ (text, *) ISENT,SEQNUM,H,K,L,col,row,chan,L2, !ISENT=3
     1		twoth,az,wl,dsp,ipkobs,inti,sigi,reflag
		RETURN
	END IF

! ISENT=0 ---> new histogram
	IF (ISENT.EQ.0) THEN	!First record of a new histogram
		READ (IUNIT,*) ISENT,NRUN,DN,		!ISENT=1
     1		CHI,PHI,OMEGA,MONCNT
		READ (IUNIT,100) TEXT				!ISENT=2
		READ (IUNIT, *) ISENT,SEQNUM,H,K,L,col,row,chan,L2, !ISENT=3
     1		twoth,az,wl,dsp,ipkobs,inti,sigi,reflag
		RETURN
	END IF


	WRITE (*,*) '*** Error in SUBROUTINE READREFL ***'
	RETURN

999	IEOF=1	!END OF FILE
	RETURN
	END
	
	

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


		SUBROUTINE RECIPR

C	Calculate reciprocal cell parameters from the real parameters.
C	The parameters are transferred through COMMON/CELL/.

C	A. J. Schultz     7/13/82


	COMMON /CELL/ A,B,C,ALPHA,BETA,GAMMA,VOL,
     &                AS,BS,CS,ALPHAS,BETAS,GAMMAS,VOLS
	DATA RAD/57.29577951/

	CA = COS(ALPHA/RAD)
	SA = SIN(ALPHA/RAD)
	CB = COS(BETA/RAD)
	SB = SIN(BETA/RAD)
	CG = COS(GAMMA/RAD)
	SG = SIN(GAMMA/RAD)
	VOL = A*B*C*SQRT(1.0 - CA**2 - CB**2 - CG**2
     &                 + 2*CA*CB*CG)

C----------------  Reciprocal cell parameters  -------------------

	AS = B*C*SA/VOL
	BS = A*C*SB/VOL
	CS = A*B*SG/VOL
	ALPHAS = ACOS((CB*CG - CA)/(SB*SG)) * RAD
	BETAS  = ACOS((CA*CG - CB)/(SA*SG)) * RAD
	GAMMAS = ACOS((CA*CB - CG)/(SA*SB)) * RAD
	VOLS = 1.0/VOL

	RETURN
	END
	

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	
      SUBROUTINE TMAT (T)
C                                                                       
C     The nomenclature used here is from W.C. HAMILTON,                 
C     International Tables, Vol. IV, pp. 273-284, 1974.                 
C                                                                       
C     This subroutine contructs the T matrix from the real unit
C     cell parameters in "COMMON/CELL/".  The real and reciprocal
C     unit cell volumes and the reciprocal cell parameters are
C     also calculated.
C                                                                       
C     A.J. Schultz   7/12/82
C                                                                       
C                                                                       
	COMMON /CELL/ A,B,C,ALPHA,BETA,GAMMA,VOL,
     &                AS,BS,CS,ALPHAS,BETAS,GAMMAS,VOLS
	DATA RAD/57.29577951/
C
	DIMENSION T(3,3)
C
C	Calculate reciprocal cell parameters and construct the T
C	matrix.
C
	CALL RECIPR
C
	CAS = COS(ALPHAS/RAD)
	CBS = COS(BETAS/RAD)
	CGS = COS(GAMMAS/RAD)
	SGS = SIN(GAMMAS/RAD)
C
	DO 10 I=1,3
	DO 10 J=1,3
	T(I,J) = 0.0
10	CONTINUE

	T(1,1) = AS*SGS
	T(1,2) = AS*CGS
	T(2,2) = BS
	T(3,2) = CS*CAS
	T(3,1) = (AS*CS*CBS - T(1,2)*T(3,2)) / T(1,1)
	T(3,3) = SQRT(CS**2 - T(3,2)**2 - T(3,1)**2)
C
	RETURN
	END

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


	SUBROUTINE TSTVOL (U, VOLUME)

C***	Test orientation matrix for right or left handed unit cell.
C***	If the volume is negative, then cell is left handed.

	DIMENSION U(3,3),A(3),B(3),C(3),AXB(3)

	A(1) = U(1,1)
	A(2) = U(1,2)
	A(3) = U(1,3)
	B(1) = U(2,1)
	B(2) = U(2,2)
	B(3) = U(2,3)
	C(1) = U(3,1)
	C(2) = U(3,2)
	C(3) = U(3,3)

C***	V = A dot (B cross C)

	CALL CROSS (B,C,AXB)

	VOLUME = A(1)*AXB(1) + A(2)*AXB(2) + A(3)*AXB(3)
	VOLUME = 1./VOLUME

	RETURN
	END

	
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	Subroutine rotation_matrix (omega, chi, phi, fmat)

	dimension fmat(3,3), fmat1(3,3)
	dimension R_phi(3,3), R_chi(3,3), R_om(3,3)
	
	rad = 180.0/acos(-1.0)
	
	do I = 1,3
	do J = 1,3
	fmat(I,J) = 0.0
	R_phi(I,J) = 0.0
	R_chi(I,J) = 0.0
	R_om(I,J) = 0.0
	end do
	end do

	ph = phi/rad
	cp = cos(ph)
	sp = sin(ph)
	R_phi(1,1) = cp
	R_phi(1,2) = sp
	R_phi(2,1) = -sp
	R_phi(2,2) = cp
	R_phi(3,3) = 1.0

	ch = chi/rad        !changed -chi to chi, 8/23/07
	cc = cos(ch)
	sc = sin(ch)
	R_chi(1,1) = 1.0
	R_chi(2,2) = cc
	R_chi(2,3) = sc
	R_chi(3,2) = -sc
	R_chi(3,3) = cc

	om = -omega/rad
	co = cos(om)
	so = sin(om)
	R_om(1,1) = co
	R_om(1,2) = so
	R_om(2,1) = -so
	R_om(2,2) = co
	R_om(3,3) = 1.0

	call matmulx (R_chi, R_phi, fmat1)
	call matmulx (fmat1, R_om, fmat)

	return
	end
	
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


      SUBROUTINE TXB (T, B, C)

!	Multiply 3x3 matrix T times vector B.
!	Product is vector C.

      DIMENSION T(3,3),B(3),C(3)
      DO 10 I=1,3
   10 C(I)=0.0
      DO 20 I=1,3
      DO 20 J=1,3
   20 C(I)=C(I)+T(I,J)*B(J)
      RETURN
      END

      
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


	Subroutine vec_dot_mat (V1, W, V2)
	
	dimension V1(3), V2(3), W(3,3)
	
	v2(1) = v1(1)*w(1,1) + v1(2)*w(2,1) + v1(3)*w(3,1)
	v2(2) = v1(1)*w(1,2) + v1(2)*w(2,2) + v1(3)*w(3,2)
	v2(3) = v1(1)*w(1,3) + v1(2)*w(2,3) + v1(3)*w(3,3)
	
	return
	end
	

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
