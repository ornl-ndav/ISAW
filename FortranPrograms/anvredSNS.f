C**************************   ANVRED  ******************************
C
C ARGONNE NATIONAL LABORATORY VARIABLE WAVELENGTH DATA REDUCTION PROGRAM
C
C		Major contributions from:
C			P. C. W. Leung
C			A. J. Schultz
C			R. G. Teller
C			L. R. Falvello
C
C     The data output by this program are corrected  for  variations  in
C  spectral distribution, variations in detector efficiency  across  the
C  face  of  the  detector,  and the pertinent geometric factors such as
C  (SIN(THETA))**2 and LAMBDA**4.
C
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!	Linux version:	A. Schultz   January, 2003                    !
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

!	Version 4:		A. Schultz		June, 2003
!		1. Process data from two detectors
!		2. Does not use an x-file.
!		3. Gets MONCNT from integrate file. If zero, sets CMONX = 1.0.
!		4. Corrected ALPHAP for calculation of SPECT1.

!	Version 5:		A. Schultz		July, 2003
!		This version outputs a expnam.hkl file which can be input
!		into SHELX with HKL 2.
!	Version 5a:
!		Cleaned-up and removed a lot of unused code.
!		Added a test for dmin.
!
!	Version 6:		L. Falvello		January, 2004
!		Polyhedral absorption correction with two detectors.
!
!	Version 7:		A. Schultz		2007
!		Use spectrum obtained from each SCD detector
!
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!	ANVRED_SNS:		A. Schultz		2008               !
!		Process SNS data.                                          !
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!	ANVRED_SNS_v2:		A. Schultz		June, 2008
!		New spherical absorption correction. Removed all
!		of the old correction code.
!	ANVRED_SNS-v2.1: read detector parameters from integrate file.
!	ANVRED_SNS-v2.2: get filename for spectrum file.
!       ANVRED_SNS-v2.3: everything included in one file.  8/17/2008
!       anvredSNS_2.4: made compatible with gfortran including removal
!                      of FREIN3 and READ133.         10/8/2008
!	anvredSNS_2.5: the datacom_SNS.inc file is no longer used. Cleaned
!			up the code using ftnchek.    10/13/08
!	anvredSNS_2.6: assign a common scale factor for each
!			crystal setting, or for each detector. 1/29/09
!
!	4/13/09	Number of possible spectra increased from 2 to 100.
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


	Program ANVRED_SNS


	INTEGER H,HSTNUM
	INTEGER REFLAG,SEQNUM
	INTEGER DETNUM(100), DN				!DN added 4/21/08

	REAL	INTI,L1,L2

	CHARACTER EXPNAM*14

! Labelled common for 100 SNS detectors		3/21/08
	common /detect/ nrows(100), ncols(100), width(100),
     1	HEIGHT(100), DEPTH(100), DIST(100), CenterX(100),
     2	CenterY(100), CenterZ(100), BaseX(100), BaseY(100),
     3	BaseZ(100), UpX(100), UpY(100), UpZ(100), nod
     
! Labelled common for the incident spectrum	July, 2009
	common /spect_data/ xtime(1700), counts(1700)
     
	COMMON    /PEAKS/  col, row, chan, L2, twoth, az, dsp
	COMMON    /INSPAR/ L1, TZERO
	COMMON    /MONI/   MONCNT
	COMMON    /SET/    CHI, PHI, OMEGA, DETA
	COMMON    /DATA5/  MNSUM
	COMMON    /DATA8/  HSTNUM
	COMMON    /DATA11/ NRUN, DETNUM, dn, EXPNAM
	COMMON    /DATA12/ SEQNUM, H, K, L, X, Y, WL,
     1			   IPKOBS, INTI, SIGI, REFLAG

	DATA	HOM /0.39559974/

	INTEGER CURHST
	integer today(3), now(3)
	CHARACTER*240 NAM1,BNAM,DEFNAM, SpecNam
	character*240 ANS
	CHARACTER ALINE*78

	DIMENSION PJ(11,100)	!Spectrum coefficients for 100 detectors
				! 4-13-2009
	dimension xtof(100), spect1(100)

       DATA IQ/1/
       DATA DN/1/

C--------------------------------------------------------------------

	OPEN(UNIT=16,FILE='anvred.log')

	WRITE (*, *) ' '
	WRITE (*, *) '*** anvredSNS ***'
	WRITE (*, *) ' '
	WRITE (16, *) '*** anvredSNS ***'
	WRITE (16, *) ' '

C
C  REQUEST THE DATA NEEDED TO DEFINE THE FILE NEEDED
C
	WRITE (*, 11)
11	format(' Experiment name (EXPNAM)? ',$)
	READ (*, 100) EXPNAM
100	FORMAT (A) 
	LCS = LNBLNK(EXPNAM)

	WRITE (16,11)
	WRITE (16,100) EXPNAM

C
C  CHECK ON THE EXISTANCE OF THE REFL FILE
C
	  defnam=expnam(1:LCS)//'.integrate'
	  lcs2 = lcs + 10

	WRITE (*,552) DEFNAM(1:LCS2)
552	FORMAT(/,
     1' Input the name of the input reflection file <',A,
     2'>: ',$)
	CALL READANS (NCHRS, NAM1)	
	IF (nchrs.eq.0) nam1=defnam
	  OPEN(UNIT=9,STATUS='OLD',FILE=NAM1,
     1	err=5004)
	
!  Initial read of integrate file to get instrument and detectors calibration.
	call readrefl_SNS_header (9, ieof)
	write (*, *) ' '
		

	WRITE (16,552) DEFNAM(1:LCS2)
	WRITE (16,'(A)') NAM1

	defnam=expnam(1:LCS)//'.hkl'
	LCS2 = LCS+4
	WRITE (*, 60) defnam(1:LCS2)
60	FORMAT(/,
     1' Input the name of the output reflection file <',A,
     2'>: ',$)
	CALL READANS (NCHRS, BNAM)
	if (nchrs.eq.0) bnam=defnam

	WRITE (16,60) defnam(1:LCS2)
	WRITE (16,'(A)') BNAM

	OPEN(UNIT=4,STATUS='OLD',FILE=BNAM,ERR=610)
	WRITE (*, *) ' ***Output will be appended to existing file.***'
605	READ (4,100,END=400) ALINE
	GO TO 605

610	WRITE (*,*)
     1' ***New output reflection file is being created.***'
	OPEN(UNIT=4,STATUS='NEW',FILE=BNAM,ERR=5004)

400	CONTINUE

C
C  ECHO MUCH OF THE ABOVE DATA ON THE LINE-PRINTER
C

     	call idate(today)   ! today(1)=day, (2)=month, (3)=year
      	call itime(now)     ! now(1)=hour, (2)=minute, (3)=second
     	write ( 16, 9000 )  today(2), today(1), today(3), now
9000	format (//,' ***   ANVRED   ', i2.2, '/', i2.2, '/', i4.4,' ',
     1  i2.2, ':', i2.2, ':', i2.2,'   ***'//)
	
      WRITE (*, 9613)
9613	FORMAT(//,
     1' Do you wish to do an absorption correction ',
     1'(Y,<N>)? ',$)
	CALL READANS (NCHRS, ANS)
	if (nchrs.eq.0) ans='N'
	IABS = 0
      
      IF (ANS.EQ.'Y' .OR. ANS.EQ.'y') THEN
        WRITE (16, 198)
198     FORMAT(//,' ***** ABSORPTION CORRECTION APPLIED ******')
        IABS = 1
      END IF

C
C   SETUP FOR ABSORPTION CORRECTION
C
	IF (ANS.EQ.'Y' .OR. ANS.EQ.'y') 
     1          CALL ABSOR1_SNS (smu, amu, radius)
	transmin = 1.0
	transmax = 0.0

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	write (*, 9010)
9010	format(/,'Is the incident spectrum correction based on'/,
     1          '   (1) coefficients of a fitted equation'/,
     2          'or (2) the actual spectral data?'/,
     3          'Input 1 or 2 <1>: ',$)
     	CALL READANS (NCHRS, ANS)
	  IF (NCHRS.EQ.0) THEN
		ispec = 1
	  ELSE
		READ (ANS, *) ispec
	  END IF
	WRITE (16, 9010)
	WRITE (16, *) ispec

	if (ispec .eq. 1) then  !!!!!!!!!!

        WRITE (*, 1330) 
1330	FORMAT(' Enter the spectrum file name: ',$)
        READ (*, 100) SpecNam


        OPEN(UNIT=21,STATUS='OLD',FILE=SpecNam)
        WRITE (*, *) ' '
        WRITE (*, 6290) SpecNam
6290	FORMAT(
     1  ' Incident spectrum coefficients in ',A)
        WRITE (16,6290)
        WRITE (*, *) ' '

		do ii=1,nod	! 4-13-09
	
            read (21,100) ALINE
            read (21,*) (PJ(J,ii),J=1,11)
            write (*,100) ALINE
            write (*,*) (PJ(J,ii),J=1,11)

		end do
		
	end if                  !!!!!!!!!!
			
	if (ispec .eq. 2) then  !*********
			
        write (*, 789)
789	    format(/,'Input averaging range +/- (<5>): ',$)
        CALL READANS (NCHRS, ANS)
 
        IF (NCHRS.EQ.0) THEN
            navg_range = 5
        ELSE
            READ (ANS, *) navg_range
        END IF
        
        write (16, 789)
        write (16, *) navg_range
        
        write (*, 810)
810     format(/,'Input first detector bank number.'/,
     1           'At this time, for SNAP, input 10.'/,
     2           'For TOPAZ, input 1.'/,
     3           'Initial Bank number (<1>): ',$)
        call readans (nchrs, ans)
        
        IF (NCHRS.EQ.0) THEN
            initBankNo = 1
        ELSE
            READ (ANS, *) initBankNo
        END IF
        
        write (16, 810)
        write (16, *) initBankNo
        
	end if                  !*********

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

!  Permit only data above a I/sig(I) threshold to be saved.
	WRITE (*, 5360)
5360	FORMAT(//,' Input minimum I/sig(I) (<0>): ',$)
	CALL READANS (NCHRS, ANS)
          IF (NCHRS.EQ.0) THEN
		ISIG = 0
	  ELSE
		READ (ANS, *) ISIG
	  END IF
	WRITE (16, 5360)
	WRITE (16, *) ISIG

!  Reject border peaks.
	WRITE (*, 6360)
6360	FORMAT(//,' Reject peaks within N channels of the border.'/,
     1' Input a value for N (<5>): ',$)
	CALL READANS (NCHRS, ANS)
	  IF (NCHRS.EQ.0) THEN
		NBCH = 10
	  ELSE
		READ (ANS, *) NBCH
	  END IF
	WRITE (16, 6360)
	WRITE (16, *) NBCH

!	Removed, ajs 10/28/09	
!  Reject peaks for which CENTROID failed (REFLAG .NE. 10)
! 	WRITE (*, 7360)
! 7360	FORMAT(//,
!      1' Reject peaks for which the centroid calculation failed',
!      2' (<y> or n): ',$)
! 	CALL READANS (NCHRS, ANS)
! 	  IF (NCHRS.EQ.0) THEN
! 		JREF = 1
! 	  ELSE
! 		IF (ANS.EQ.'Y' .OR. ANS.EQ.'y') THEN
! 			JREF = 1
! 		ELSE
! 			JREF = 0
! 		END IF
! 	  END IF

! 	WRITE (16, 7360)
! 	WRITE (16, '(A)') ANS

!  Reject peaks below a threshold peak count	!ajs 5/6/03
	WRITE (*, 8360)
8360	FORMAT(//,
     1' Input mininum peak count (<10>): ',$)
	CALL READANS (NCHRS, ANS)
	  IF (NCHRS.EQ.0) THEN
		IPKMIN = 0
	  ELSE
		READ (ANS, *) IPKMIN
	  END IF

	WRITE (16, 8360)
	WRITE (16, '(A)') ANS

!  Reject peaks below a minimum d-spacing		!ajs 7/2/03
	WRITE (*, 8370)
8370	FORMAT(//,
     1' Input mininum d-spacing (<0.5>): ',$)
	CALL READANS (NCHRS, ANS)
	  IF (NCHRS.EQ.0) THEN
		DMIN = 0.0
	  ELSE
		READ (ANS, *) DMIN
	  END IF

	WRITE (16, 8370)
	WRITE (16, '(A)') ANS

!  Scale factor identier assignments			!ajs 1/29/09
	WRITE (*, 8380)
8380	FORMAT(//,
     1' Assign common scale factors'/,
     2'      1 = for each crystal setting (fewer scale factors)'/,
     3'   or 2 = for each detector in each crystal setting'/,
     4' Input 1 or 2 (<1>): ',$)
	CALL READANS (NCHRS, ANS)
	  IF (NCHRS.EQ.0) THEN
		IIQ = 1
	  ELSE
		READ (ANS, *) IIQ
	  END IF

	WRITE (16, 8380)
	WRITE (16, '(A)') IIQ

!  Multiply by a scale factor.
	WRITE (*, 5370)
5370	FORMAT(//,
     1' If integrated counts are very large, one may wish to multiply'/,
     2' by a factor, such as 0.1 or less.'/,
     3' Scaling factor applied to FSQ and sig(FSQ) (<1.0>): ',$)
	CALL READANS (NCHRS, ANS)
	  IF (NCHRS.EQ.0) THEN
		SCALEFACTOR = 1.0
	  ELSE
		READ (ANS, *) SCALEFACTOR
	  END IF

	WRITE (16, 5370)
	WRITE (16, *) SCALEFACTOR 
	
	write (*, *) ' '


C-----------------------------------------------------------------------
C  Calculate spectral correction at 1.0 Angstrom to normalize
C  spectral correction factors later on.   This code was copied
C  from SEPD_SPEC.


		do id = 1, nod

	if (ispec .eq. 1) then	! The spectrum is calculated from
				! coefficients

	
	  xtof(id) = (L1 + dist(id)) / hom
	
c++	TOF = WLMIN * XTOF
c++  Normalize to a wavelength of 1.0 Angstroms
	  TOF = 1.0 * XTOF(id)
	  T = TOF/1000.

C	SPECT1 = A1(JS)*EXP(-A2(JS)/T**2)/T**5 + A3(JS)*EXP(-A4(JS)*T*T)
C     &		+ A5(JS)*EXP(-A6(JS)*T**3) + A7(JS)*EXP(-A8(JS)*T**4) 
! TYPE 2 function in GSAS.
	  SPECT1(id) = PJ(1,id) + PJ(2,id)*EXP(-PJ(3,id)/T**2)/T**5
     1	+ PJ(4,id)*EXP(-PJ(5,id)*T**2)
     2	+ PJ(6,id)*EXP(-PJ(7,id)*T**3)
     3	+ PJ(8,id)*EXP(-PJ(9,id)*T**4)
     4	+ PJ(10,id)*EXP(-PJ(11,id)*T**5)

	else	! ispec = 2
!!!!!!!!!!!! July 2009

	  ibank = id + initBankNo - 1
	  write (ans, '(I2)') ibank
	  LCS = LNBLNK(ans)
	  SpecNam = 'Bank'//ans(1:LCS)//'_spectrum.asc'
	 
	  open(unit=22, file=SpecNam, status='old')
	
	  do j=1,7		! Skip the first 7 lines.
	    read (22, 100) aline
	  end do
	
	  do j=1,1700
	    read (22, *) xtime(j), counts(j)
	  end do
	
	  close(unit=22)

	  xtof(id) = (L1 + dist(id)) / hom
	
	  wl = 1.0
	  spect1(id) = 1.0
	  call spectrum (WL, XTOF(id), NAVG_RANGE, spect1(id), SPECT)
	  spect1(id) = spect
	  
!!!!!!!!!!!!!!!
	end if
		end do


C-----------------------------------------------------------------------
 

C
C  SET THE CURRENT HISTOGRAM NUMBER TO 0 AND INITIALIZE THE MONITOR COUN
C
	CURHST = 0
	IDET = 0		!++
	HSTNUM = 0		!++
	CMON = 1000000.
	NCNTR = 0		!Number of processed reflections
C
C   SET UP LOOP TO PROCESS THE REFLECTION DATA
C
	write (*, *) ' '

4000	CONTINUE

	CALL READREFL_SNS (9, IEOF)
	
	IF (IEOF .EQ. 1) GO TO 114

	IF (NRUN.EQ.CURHST .AND. DN.EQ.IDET) GO TO 1100
	
	if (nrun .ne. curhst) then	! 1-29-09
		CURHST = NRUN
		if (IIQ .eq. 1) hstnum = hstnum + 1	! 1-29-09
	end if	
	
	IDET = DN	!IDET and DN is the arbitrary detector number.
			!ID is a sequential number in the order they are listed.
	do id=1,nod
		if (detnum(id).eq.dn) go to 1090
	end do
	STOP 'Error -- no match for detector ID'
	
	
1090	continue


	if (IIQ .eq. 2) hstnum = hstnum + 1
	
	WRITE (*, *) '   NRUN=',NRUN,'   DETNUM=',DN,'   HSTNUM=',HSTNUM

	MNSUM = MONCNT	!++

	IF (MNSUM.EQ.0) THEN	!++ Check for zero.
		CMONX = 1.0
	ELSE
		CMONX = CMON / MNSUM
		IF (CMONX .EQ. 0) CMONX=1.
	END IF

      WRITE (16, 1110) NRUN,CHI,PHI,OMEGA,DETA,MNSUM,CMONX
 1110 FORMAT(/,' HISTOGRAM NUMBER',I5/3X,' ANGLES ARE CHI =',F7.2,1X,
     * '  PHI =',F7.2,'  OMEGA =',F7.2,'  DETA =',F7.2/,
     * '    TOTAL MONITOR COUNTS ELAPSD',I10,'   CMONX =',F8.4,
     * '    * DATA SCALED TO 1 MILLION MONITOR COUNTS *'/,
     * '    CORREC = SCALEFACTOR * CMONX * SINSQT / ( SPECT ',
     * ' * (DET EFF) * WL4 * ABTRANS )')

      write(16,117) 
  117 FORMAT(/,'    H   K   L       FSQ     SIG     WL      INTI',      
     2'     SIG   SPECT  DET_EFF SINSQT',
     3'  ABTRANS   TBAR'/)


1100	CONTINUE


		IF (ISIG .GT. 0 .AND. INTI .LT. ISIG*SIGI) GO TO 4000
		IF (INTI .EQ. 0.0) GO TO 4000
		IF (IPKOBS .LT. IPKMIN) GO TO 4000	!ajs 5/6/03
		x = col
		y = row
		IF (x.LT.NBCH .OR. x.GT.(ncols(id)-NBCH)) GO TO 4000	!ajs 4/14/03
		IF (Y.LT.NBCH .OR. Y.GT.(nrows(id)-NBCH)) GO TO 4000
!		IF (JREF.EQ.1 .AND. REFLAG.NE.10) GO TO 4000  !removed ajs 10/28/09
!		IF(REFLAG.GT.299) GO TO 4000                  !removed ajs 10/28/09
		if (reflag .eq. 0) go to 4000  !peak not indexed. ajs 10/28/09

!  The d-spacing is read from the SNS refelction file.

		IF (DSP.LT.DMIN) GO TO 4000



	NCNTR=NCNTR+1		!Number of processed reflections.
C+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
C   Spectral correction based on SCD TiZr data.


		if (ispec .eq. 1) then

C	SPECT = A1(JS)*EXP(-A2(JS)/T**2)/T**5 + A3(JS)*EXP(-A4(JS)*T*T)
C     &		+ A5(JS)*EXP(-A6(JS)*T**3) + A7(JS)*EXP(-A8(JS)*T**4)

	TOF = WL * XTOF(id)
	T = TOF/1000.

! TYPE 2 function in GSAS.

	SPECT = PJ(1,id) + PJ(2,id)*EXP(-PJ(3,id)/T**2)/T**5
     1	+ PJ(4,id)*EXP(-PJ(5,id)*T**2)
     2	+ PJ(6,id)*EXP(-PJ(7,id)*T**3)
     3	+ PJ(8,id)*EXP(-PJ(9,id)*T**4)
     4	+ PJ(10,id)*EXP(-PJ(11,id)*T**5)

     	SPECT = SPECT/SPECT1(id)
     
		else		! ispec = 2     		
     	
	call spectrum (WL, XTOF(id), NAVG_RANGE, SPECT1(id), SPECT)
	
		end if
 
C+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


	SINSQT = (WL/(2.0*dsp))**2

	eff = 1.0

	WL4=WL**4	

	CORREC = SCALEFACTOR *( SINSQT*CMONX ) / ( WL4*SPECT*EFF ) !ajs Added SCALEFACTOR 3/14/03

C
C   ABSORPTION CORRECTION
C
      trans = 1.0	!transmission

C LRF 7/96
	IF (IABS.NE.0) THEN
      
          call absor_sphere(smu, amu, radius, twoth, wl, trans, tbar)
		
		if (trans.lt.transmin) transmin = trans
		if (trans.gt.transmax) transmax = trans
	
	ENDIF
	
	CORREC = CORREC/trans

!ajs 3/14/03
	FSQ = INTI * CORREC
	SIGFSQ = SIGI * CORREC

      write(16,111)H,K,L,FSQ,SIGFSQ,WL,INTI,SIGI,SPECT,EFF,
     2 SINSQT,trans,tbar			! TBAR=Coppens's Tbar.
  111 FORMAT (1X,3I4,F10.2,F8.2,F7.3,F10.2,F8.2,4F8.4,F8.4)
  
	IQ = HSTNUM

!++  Write output file that is compatible with SHELX   June, 2003
      WRITE(4,1160)H,K,L,FSQ,SIGFSQ,IQ,WL,TBAR,CURHST,SEQNUM,
     2    EFF,trans
 1160 FORMAT(3I4,2F8.2,I4,F8.4,F7.4,2I7,2F7.4)


       GO TO 4000

C
C  END OF THE REFLECTION LOOP
C
  114 WRITE (16, 1113)
 1113 FORMAT ('*',79X/' END       END OF THE DATA SET',51X)
      CLOSE(UNIT=45)
	WRITE(4,*)'  0   0   0    0.00    0.00   0  0.0000'
      CLOSE(UNIT=4)
C LRF 7/96
      if (iabs.ne.0) write(*,7001) transmax, transmin
 7001 format(/,1x,'Absorption correction applied -- max., min.',
     1  ' transmission: ',2f8.4,/)
	if (iabs.ne.0) WRITE (16, 7001) transmax, transmin
C
      WRITE (*, 20) NCNTR
	WRITE (16, 20) NCNTR
   20 FORMAT(1X,I6,' REFLECTIONS PROCESSED',/)
      STOP
 5004 WRITE (*, 5104)
 5104 FORMAT(1X,18A1,' FILE DOES NOT EXIST')
      STOP
      END
!!!!!!!!!!! END OF PROGRAM !!!!!!!!!!!


C------------------------------------------------------------
C------------------------------------------------------------
C------------------------------------------------------------


      SUBROUTINE ABSOR1_SNS (smu, amu, radius)
      
	character ANS*240

      write(*,1001) ' '
 1001 format(1x,a,6f10.5)
      write(*,1001) 'Input linear absorption coefficients (mu)'
     1     //' corresponding to:'
      write(*,1001)
     1  '  (a) Total scattering (wavelength independent); and'
      write(*,1001)
     1  '  (b) True absorption at <Lambda> = 1.8 Angstroms.'
      write(*,1001)
     1  '  (*) Use dimensions of reciprocal centimeters. (*)'
      write(*,1001) ' '

  101 write(*,1002) 
     1   '(Free Format) (a)Mu(scattering); (b)Mu(absorption): '
 1002 format(a,$)
	CALL READANS (NCHRS, ANS)
      if (nCHRS.eq.0) go to 101
      READ (ANS, *) smu, amu
      
      write(*,1001) ' '
      write(*,1001) 'Mu(scattering), Mu(absorption),'
     1 //' reciprocal centimeters: ',smu,amu
      write(*,1002) '(Answer with <Y>, N, y, or n) OK? '
	call readans (nchrs, ans)
       if (nchrs.eq.0) ans='Y'
       if (ans.eq.'N' .or. ans.eq.'n') go to 101

CCC	Write to anvred.log file
      write(16,1001) ' '
      write(16,1001) 'Input linear absorption coefficients (mu)'
     1     //' corresponding to:'
      write(16,1001)
     1  '  (a) Total scattering (wavelength independent); and'
      write(16,1001)
     1  '  (b) True absorption at <Lambda> = 1.8 Angstroms.'
      write(16,1001)
     1  '  (*) Use dimensions of reciprocal centimeters. (*)'
      write(16,1001) ' '

	write(16,1002) 
     1   '(Free Format) (a)Mu(scattering); (b)Mu(absorption): '
	WRITE (16, *) SMU,AMU
CCC

C --------------------------------------------------
C Spherical absorption correction.
      write(*,1001) 'Spherical Absorption Correction --'
  151 write(*,1002) 'Enter radius of sphere, in cm.: '

	write(16,1001) 'Spherical Absorption Correction --'
	write(16,1002) 'Enter radius of sphere, in cm.: '

	CALL READANS (NCHRS, ANS)
      if (nCHRS.eq.0) go to 151
      read (ANS, *) radius

	WRITE (16, 1601) RADIUS
1601	FORMAT(F10.3)

      RETURN
      end

      
C------------------------------------------------------------
C------------------------------------------------------------
C------------------------------------------------------------


      SUBROUTINE absor_sphere(smu, amu, radius, twoth, wl, trans, tbar)
	
!	Subroutine to calculate a spherical absorption correction
!	and tbar. Based on values in:
!
!	C. W. Dwiggins, Jr., Acta Cryst. A31, 395 (1975).
!
!	In this paper, A is the transmission and A* = 1/A is
!	the absorption correction.

!	Input are the smu (scattering) and amu (absorption at 1.8 Ang.)
!	linear absorption coefficients, the radius R of the sample
!	the theta angle and wavelength.
!	The absorption (absn) and tbar are returned.

!	A. J. Schultz, June, 2008
			
	real mu, muR	!mu is the linear absorption coefficient,
			!R is the radius of the spherical sample.
	
!	For each of the 19 theta values in Dwiggins (theta = 0.0 to 90.0
!	in steps of 5.0 deg.), the ASTAR values vs.muR were fit to a third
!	order polynomial in Excel. These values are given below in the
!	data statement.
	dimension pc(4,19)	!polynomial coefficients
	
	data pc/
     1	0.9369,  2.1217, -0.1304,  1.1717,
     2	0.9490,	 2.0149,  0.0423,  1.0872,
     3	0.9778,  1.7559,  0.4664,  0.8715,
     4	1.0083,  1.4739,  0.9427,  0.6068,
     5	1.0295,  1.2669,  1.3112,  0.3643,
     6	1.0389,  1.1606,  1.5201,  0.1757,
     7	1.0392,  1.1382,  1.5844,  0.0446,
     8	1.0338,  1.1724,  1.5411, -0.0375,
     9	1.0261,  1.2328,  1.4370, -0.0853,
     1	1.0180,  1.3032,  1.2998, -0.1088,
     2	1.0107,  1.3706,  1.1543, -0.1176,
     3	1.0046,  1.4300,  1.0131, -0.1177,
     4	0.9997,  1.4804,  0.8820, -0.1123,
     5	0.9957,  1.5213,  0.7670, -0.1051,
     6	0.9929,  1.5524,  0.6712, -0.0978,
     7	0.9909,  1.5755,  0.5951, -0.0914,
     8	0.9896,  1.5913,  0.5398, -0.0868,
     9	0.9888,  1.6005,  0.5063, -0.0840,
     1	0.9886,  1.6033,  0.4955, -0.0833/
	
	
	mu = smu + (amu/1.8)*wl
	
	muR = mu*radius
	
	pi = acos(-1.0)
	
	theta = (twoth*180.0/pi)/2.0

!	Using the polymial coefficients, calulate ASTAR (= 1/transmission) at
!	theta values below and above the actual theta value.

	i = int(theta/5.0) + 1
	astar1 = pc(1,i) + pc(2,i)*mur + pc(3,i)*mur**2 + pc(4,i)*mur**3
	
	i = i+1
	astar2 = pc(1,i) + pc(2,i)*mur + pc(3,i)*mur**2 + pc(4,i)*mur**3
	
!	Do a linear interpolation between theta values.

	frac = mod(theta,5.0)/5.0
		
	astar = astar1*(1-frac) + astar2*frac		!Astar is the correction
	
	trans = 1.0/astar	!trans is the transmission
				!trans = exp(-mu*tbar)
	
!	Calculate TBAR as defined by Coppens.

	tbar = -log(trans)/mu
	
        RETURN
	END
	
	
C------------------------------------------------------------
C------------------------------------------------------------
C------------------------------------------------------------


	SUBROUTINE READREFL_SNS_HEADER (IUNIT,IEOF)

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
	write (*, 100) TEXT

!  Read the second line, ISENT = 6
	READ (IUNIT,100,END=999) TEXT
	write (*, 100) TEXT
	
!  Read the third line, ISENT = 7
	READ (IUNIT,100,END=999) TEXT

	READ (TEXT,150) ISENT
150	FORMAT(I1)

! ISENT=7
	IF (ISENT.EQ.7) THEN
		READ (text, *) ISENT, L1, TZERO
	END IF

	write (*, 100) TEXT

!  Read the fourth line, ISENT = 4
	READ (IUNIT,100,END=999) TEXT

!  Read lines with ISENT = 5
	nod = 0				!number of detectors
300	read (iunit, 100) text      	!ISENT=5

	READ (TEXT,150) ISENT
          IF (ISENT.NE.5) THEN	!End of reading detector parameters
                  BACKSPACE (IUNIT)
                  write (*, *) ' '
                  write (*, *) 'There are ',nod,' detectors.'
                  write (*, *)
                  RETURN
          END IF

	write (*, 100) TEXT
          
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


C------------------------------------------------------------
C------------------------------------------------------------
C------------------------------------------------------------


	SUBROUTINE READREFL_SNS (IUNIT,IEOF)

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


C------------------------------------------------------------
C------------------------------------------------------------
C------------------------------------------------------------

        SUBROUTINE READANS (NCHRS,ANS)

!     Read terminal input (answer) from user.
!	Return number of characters and text.
!	A.Schultz	January, 2002

        CHARACTER*240 ANS

	DO I=1,240
		ANS(I:I) = ' '
	END DO

        READ (*,'(A)') ANS
        NCHRS = LNBLNK(ANS)

        RETURN
        END


C------------------------------------------------------------
C------------------------------------------------------------
C------------------------------------------------------------

	SUBROUTINE SPECTRUM (WL, XTOF, NAVG_RANGE, SPECT1, SPECT)
	
!  Obtain spectral correction from counts vs. time data.
!  A. J. Schultz, July, 2009
	
	dimension xtime(1700), counts(1700)
	
	common /spect_data/ xtime, counts
	
c++	TOF = WL * XTOF
	TOF = WL * XTOF
	T = TOF			! T is in units of microseconds
	
	
	do j=1,1700
	  if (xtime(j) .gt. T) then
	    sum = 0.0
	    do jj=-NAVG_RANGE,NAVG_RANGE,1	! average +/-AVG_RANGE channels
	      sum = sum + counts(j+jj)
	    end do
	    spect = sum/(2*(NAVG_RANGE)+1)
	    go to 200
	  end if
	end do

200	spect = spect/spect1


	return
	end
	
C------------------------------------------------------------
C------------------------------------------------------------
C------------------------------------------------------------
