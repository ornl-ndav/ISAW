	Program matrix2

!	Calculate UB matrix from lattice constants and 2 reflections.

C				MATRIX

C-------------------------------------------------------------------------------
C	User inputs unit cell parameters.  Program then tries to index
C	some reflections and calculate an orientation matrix.

C	For references, see W.C. Hamilton, International Tables, Vol. IV,
C	page 280.  Also, W.R. Busing and H.A. Levy, Acta Cryst. 22,
C       457 (1967).

C	Written by:  A. J. Schultz			Oct., 1986
C-------------------------------------------------------------------------------
!
!	Modified for SNS:
!	A. J. Schultz		February, 2009
!

	INTEGER H
	INTEGER REFLAG,SEQNUM
	INTEGER DETNUM(100), DN				!DN added 4/21/08
	REAL	INTI,L2
	CHARACTER EXPNAM*14


	COMMON /CELL/ A,B,C,ALPHA,BETA,GAMMA,VOL,
     1                AS,BS,CS,ALPHAS,BETAS,GAMMAS,VOLS

! Labelled common for 100 SNS detectors		3/21/08
	common /detect/ nrows(100), ncols(100), width(100),
     1	HEIGHT(100), DEPTH(100), DIST(100), CenterX(100),
     2	CenterY(100), CenterZ(100), BaseX(100), BaseY(100),
     3	BaseZ(100), UpX(100), UpY(100), UpZ(100), nod
	COMMON    /INSPAR/ L1,TZERO


! Labelled common for 100 SNS detectors		3/21/08
	COMMON    /PEAKS/  col, row, chan, L2, twoth, az, dsp
	COMMON    /MONI/   MONCNT
	COMMON    /SET/    CHI, PHI, OMEGA, DETA
	COMMON    /DATA11/ NRUN,DETNUM,dn,EXPNAM
	COMMON    /DATA12/ SEQNUM, H, K, L, X, Y, WL,
     1			   IPKOBS, INTI, SIGI, REFLAG


	DIMENSION GG(6)			! Elements of reciprocal metric tensor.
	DIMENSION T(3,3),TINV(3,3)	! Orthonormal reciprocal lattice.
	DIMENSION HH(3,3),HHINV(3,3),XX(3,3)

	DIMENSION WLI(10),
     1		  CHII(10),PHII(10),OMEGAI(10),DETAI(10),DETDI(10),
     2		  IH(10,500),IK(10,500),IL(10,500),XD(10,3),
     3		  DSO(10),JMAX(10),ISEQ(10),twothi(10),azi(10)

	DIMENSION XD1(3),XD2(3),XD3(3),
     1            H1(3),H2(3),H2X(3),H3(3),
     2		  HC1(3),HC2(3),HC3(3)

	dimension U(3,3), q1(3), q2(3), Uinv(3,3), xhkl(3), fmat(3,3)
     
        character brefl*60
     	DATA RAD/57.29577951/

! Begin

	write (*, 30)
30 	FORMAT(' a,b,c,alpha,beta,gamma? ',$)
	read (*, *) a,b,c,alpha,beta,gamma
	write (*, 320) a,b,c,alpha,beta,gamma
320	FORMAT(/,' Real lattice parameters:'/,
     1            5X,3F8.4,3F7.2)

C   Calculate reciprocal lattice parameters and the T matrix
C   for transforming reciprocal lattice vectors hkl into a
C   crystal cartesian system.
	CALL TMAT (T)

	VOL1 = A * B * C * SIN(ALPHA/RAD) * SIN(BETA/RAD) 
     1       * SIN(GAMMAS/RAD)
	write (*, 3101) VOL1
3101	FORMAT(/,' Volume = ',F10.2)

	write (*, 32) AS,BS,CS,ALPHAS,BETAS,GAMMAS
32	FORMAT(/,' Reciprocal lattice parameters:'/,
     1             5X,3F8.4,3F7.2/)

	write (*, 33)
33	FORMAT(/,' Type of cell (P=1,A=2,B=3,C=4,F=5,I=6,R=7): ',$)
	read (*, *) ICELL
	print *, icell

	  GG(1) = AS*AS				! Calculate reciprocal
	  GG(2) = BS*BS				! metric tensor
	  GG(3) = CS*CS				! elements.
	  GG(4) = AS*BS*COS(GAMMAS/RAD)
	  GG(5) = AS*CS*COS(BETAS/RAD)
	  GG(6) = BS*CS*COS(ALPHAS/RAD)
	  
	print *
	print *,' DELTA1 is used for selecting possible hkl indices for'
	print *,' an observed peak with d-spacing d(obs). An hkl is'
	print *,' selected if abs(d(obs) - d(hkl)) is less than DELTA1,'
	print *,' where d(hkl) is calculated from the hkl indices and'
	print *,' the lattice constants.'
	print *
	print *,' DELTA2 is used to select possible pairs of peaks with'
	print *,' abs(d(obs_1) - d(obs_2)) less than DELTA2.'
	  
	write (*, 35)
35	format(/,' Input delta1, delta2 (e.g., 0.01, 0.03): ',$)
	read (*, *) DELTA1, DELTA2
	print "(2F7.3)", delta1, delta2	

        write (*, 200)
200     format(/,' Reflection file name? ',$)
        read (*, *) BREFL
        print *, BREFL
	OPEN(UNIT=9, file=BREFL, status='OLD')
!  Initial read of integrate file to get instrument and detectors calibration.
	call readrefl_SNS_header (9, ieof)
	write (*, *) ' '

C-------------------------------------------------------------------------
C		Get reflections and find possible hkl's.
C-------------------------------------------------------------------------

40	I = 0
45	write (*, 50)
50	FORMAT(/,' Input SEQNUM (terminate with 0): ',$)
	read (*, *) SEQNUM
	print *, seqnum
	  IF (SEQNUM .EQ. 0) GO TO 100
	I = I + 1
	ISEQ(I) = SEQNUM

		REWIND 1
!55	READ (1,END=310) SEQNUM,HSTNUM,NRUN,H,K,L,X,Y,Z,XCM,YCM,WL,
!     1	  IPK,IT,BKG,INTI,SIG,REFLAG,CHI,PHI,OMEGA,DETA,DETD,MONCNT
55	call READREFL_SNS (9, IEOF)
	if (ieof .eq. 1) stop ' END-OF-FILE'

!	GO TO 60
!310			STOP ' END-OF-FILE'

60	IF (SEQNUM .EQ. ISEQ(I)) THEN			! **
!	  CALL KEYWRT (45,3,0,0)
!	  CALL CTAU (XCM,YCM,WL,TAU,DETA,DETD)
!	  XCMI(I) = col
!	  YCMI(I) = row
	  twothi(I) = twoth
	  azi(I) = az
	  WLI(I) = WL
	  tau = 1.0/dsp
	  DSO(I) = TAU				! Observed d-star.
	   CHII(I) = CHI
	   PHII(I) = PHI
	   OMEGAI(I) = OMEGA
	   DETDI(I) = DETD
	   DETAI(I) = DETA
!	write (*, *) ' reciprocal d-spacing = ',TAU
	write (*, *) '  d-spacing = ',dsp
!	    CALL LAUE (XCM,YCM,WL,XD(I,1),XD(I,2),XD(I,3))
	  call qvec(twoth, az, wl, xd(I,1), xd(I,2), xd(I,3))

	  MAXH = NINT(TAU/AS) + 1		! Begin testing for possible
	  MAXK = NINT(TAU/BS) + 1		! hkl indices.
	  MAXL = NINT(TAU/CS) + 1

	  J = 0
	    DO H = -MAXH,MAXH
	    DO K = -MAXK,MAXK
	    DO L = -MAXL,MAXL

	      CALL CENTER (H,K,L,ICELL,IEX)

	      IF (IEX.EQ.0) THEN				! ****

	      DSC = SQRT(H*H*GG(1) + K*K*GG(2) + L*L*GG(3)	! Calculated
     1              + 2*H*K*GG(4) + 2*H*L*GG(5) + 2*K*L*GG(6))	! d-star.

	      IF (ABS(DSC-TAU)/tau .LE. DELTA1) THEN		! *
		J = J + 1
	        IH(I,J) = H			! 
		IK(I,J) = K			! Save hkl if close fit.
		IL(I,J) = L			!
	      END IF					! *

	      END IF						! ****

	    END DO
	    END DO
	    END DO


	  JMAX(I) = J
	  write (*, 70) I,SEQNUM,J
70	FORMAT(/,' *** No. ',I2,'     SEQNUM = ',I3,
     1           '     Number of possible hkl fits = ',I3)
	  write (*, 75) (IH(I,J),IK(I,J),IL(I,J),J=1,JMAX(I))
75	FORMAT( 6( 5(3X,3I4)/ ) )
	  write (*, 76)
76	FORMAT(' Do you want to (1) test each of the above?'/,
     1         '             or (2) select one hkl?'/,
     2         ' Input 1 or 2 : ',$)
	read (*, *) JHKL
	print *, jhkl

	  IF (JHKL.EQ.2) THEN
	    JMAX(I) = 1
	    write (*, 78)
78	    FORMAT(' Input h,k,l : ',$)
	    read (*, *)IH(I,1),IK(I,1),IL(I,1)
	    print *, IH(I,1),IK(I,1),IL(I,1)
	  END IF

	  GO TO 45

	ELSE

	  GO TO 55

	END IF						! **

100     continue

C------------------------------------------------------------------------
C   		Begin attempt to obtain an orientation matrix.
C------------------------------------------------------------------------

	IMAX = I

	DO I=1,IMAX-1			! "Primary" reflection I

		XD1(1) = XD(I,1)
		XD1(2) = XD(I,2)
		XD1(3) = XD(I,3)

	DO J=1,JMAX(I)			! Test each hkl of reflection I

	H1(1) = IH(I,J)			! H1(j) are hkl indices of refl #1
	H1(2) = IK(I,J)			! which is the primary reflection.
	H1(3) = IL(I,J)

					! HC1(j) are the reflection coordinates
	CALL TXB (T,H1,HC1)		! in the orthonormal crystal reciprocal 
					! cartesian system.

	DO II=I+1,IMAX			! Search for a "secondary" reflection II

C   Calculate observed reciprocal distance between two reflections.
	  SUM = 0.0
	  DO M=1,3
	  SUM = SUM + (XD1(M)-XD(II,M))**2
	  END DO
	  DO12 = SQRT(SUM)	! Observed distance between reflections.

	DO JJ=1,JMAX(II)

		XD2(1) = XD(II,1)
		XD2(2) = XD(II,2)
		XD2(3) = XD(II,3)

	H2(1) = IH(II,JJ)
	H2(2) = IK(II,JJ)
	H2(3) = IL(II,JJ)
	CALL TXB (T,H2,HC2)


C   Calculate distance between reciprocal lattice points hkl(1) and hkl(2)
C   based in the known unit cell parameters.
	  SUM = 0.0
	  DO M=1,3
	  SUM = SUM + (HC1(M)-HC2(M))**2
	  END DO
	  DC12 = SQRT(SUM)	! Calculated distance between hkl(1) and hkl(2)


C   Test if observed and calculated distances are in agreement.
	DIFF12 = DO12 - DC12
	IF (ABS(DIFF12)/do12 .LE. DELTA2) THEN			! ***

C		Calculate an orientation matrix based on 2 reflections
C		and the unit cell parameters.

C		First find the cross product HC3 = HC1 X HC2

		  CALL CROSS (HC1,HC2,HC3)

C		Then calculate H3 = T(inverse) * HC3

		  CALL MATIN2 (T,TINV)
		  CALL TXB (TINV,HC3,H3)

C		Calculate the cross product XD3 = XD1 X XD2

		  CALL CROSS (XD1,XD2,XD3)

C		Replace H2 by H2X = H3 X H1.

		  CALL CROSS (HC3,HC1,HC2)
		  CALL TXB (TINV,HC2,H2X)

C		Replace XD2 by XD2 = XD3 X XD1

		  CALL CROSS (XD3,XD1,XD2)

C		Finally, U = HH(inverse) * XX

		  DO N=1,3
		    HH(1,N) = H1(N)
		    XX(1,N) = XD1(N)
		  END DO
		  DO N=1,3
		    HH(2,N) = H2X(N)
		    XX(2,N) = XD2(N)
		  END DO
		  DO N=1,3
		    HH(3,N) = H3(N)
		    XX(3,N) = XD3(N)
		  END DO

		CALL MATIN2 (HH,HHINV)

!		CALL MATMUL (HHINV,XX,U)
		U = matmul(HHINV, XX)

C   Calculate volume and test for right handed unit cell.
		CALL TSTVOL (U, VOL2)

C   Test if calculated and observed unit cell volumes agree within 10%.
		IF (ABS(VOL1-VOL2)/VOL1 .LE. 0.1) THEN		!$$$$$$$$$$

	write (*, 210)
210	FORMAT(' '//,' **********')
	write (*, 220) I,ISEQ(I),(H1(M),M=1,3)
220	FORMAT(' Primary reflection:    no. ',I2,'     SEQNUM = ',I3,
     1         '     hkl = ',3F5.0)
	write (*, 221) II,ISEQ(II),(H2(M),M=1,3)
221	FORMAT(' Secondary reflection:  no. ',I2,'     SEQNUM = ',I3,
     1         '     hkl = ',3F5.0)
	write (*, 222)
222	FORMAT(' ')

		write (*, 238) VOL2
238		FORMAT(' Unit cell volume = ',F10.2)
		write (*, 241)

! To be consistent with SNS axes and ISAW, we need to permute
! the columns of the UB matrix:
!		write (*, 240) ((U(M,N),N=1,3),M=1,3)
                write (*, 240) (U(M,3),U(M,1), U(M,2), M=1,3)
240		FORMAT(' U matrix:'//,2(3F10.6/),3F10.6)
		write (*, 241) a,b,c,alpha,beta,gamma,vol1
241		FORMAT(7F10.3)
		write (*, 242)
242		format('     0.000     0.000     0.000     0.000     0.000',
     1                 '     0.000     0.000')
	print *
	print *,'*** The above lattice parameters are the input'
	print *,'*** parameters.'
	print *,'*** They are not parameters derived from the matrix.'
	print *


C   Calculate hkl's for all reflections.

	  DO III=1,IMAX
	    CHI = CHII(III)
	    PHI = PHII(III)
	    OMEGA = OMEGAI(III)
	    DETA = DETAI(III)
	    DETD = DETDI(III)
!	  CALL XUH (XCMI(III),YCMI(III),WLI(III),U,XH,XK,XL)

	  call qvec (twothi(III), azi(III), wli(III), qx, qy, qz)
	  call rotation_matrix (omegai(III), chii(III), phii(III), fmat)
	  q1(1) = qx
	  q1(2) = qy
	  q1(3) = qz
!	  call vec_dot_mat (q1, fmat, q2)
	  q2 = matmul(q1, fmat)
	  call matin2 (U, Uinv)
!	  call vec_dot_mat (q2, Uinv, xhkl)
	  xhkl = matmul(q2, Uinv)
	  xh = xhkl(1)
	  xk = xhkl(2)
	  xl = xhkl(3)
	  
	  

	  write (*, 250) III,ISEQ(III),XH,XK,XL
250	  FORMAT(' No. ',I2,'     SEQNUM = ',I3,
     1           '     hkl = ',3F7.2)
	  END DO

	        ELSE						!$$$$$$$$$$

C   ABS(VOL1-VOL2)/VOL1 is greater than 0.1

!	    	write (*, 255) I,ISEQ(I),(H1(M),M=1,3)
!	    	write (*, 256) II,ISEQ(II),(H2(M),M=1,3)
!		write (*, 260) VOL1,VOL2
!260		FORMAT('      VOL1 = ',F8.2,'     VOL2 = ',F8.2)

		END IF						!$$$$$$$$$$

	  ELSE							! ***

C  ABS(DO12-DC12) is greater than DELTA.

!	    write (*, 255) I,ISEQ(I),(H1(M),M=1,3)
!255	    FORMAT(/,' *** No. ',I2,'     SEQNUM = ',I3,
!     1               '     hkl = ',3F5.0)
!	    write (*, 256) II,ISEQ(II),(H2(M),M=1,3)
!256	    FORMAT('     No. ',I2,'     SEQNUM = ',I3,
!     1             '     hkl = ',3F5.0)
!	    write (*, 257) DO12,DC12,DIFF12
!257	    FORMAT('     DO12 = ',F8.5,'     DC12 = ',F8.5,
!     1             '     DIFF = ',F8.5)

	    continue

	  END IF						! ***

	END DO		! JJ
	END DO		! II
	END DO		! J
	END DO		! I

	STOP
	END
	
