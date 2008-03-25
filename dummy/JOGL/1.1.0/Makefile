
JOGL	=	./jogl-1.1.0-linux-i586/lib/jogl.jar

GLUEGEN	=	./jogl-1.1.0-linux-i586/lib/gluegen-rt.jar

install:  local_deploy

deploy:
	mvn deploy:deploy-file -DgroupId=ISAW -DartifactId=jogl \
		-Dversion=1.1.0 -Dpackaging=jar -Dfile=$(JOGL) \
		-DrepositoryId=sns-internal \
		-DpomFile=pom.xml \
		-Durl=scpexe://flathead.ornl.gov/maven/internal
	mvn deploy:deploy-file -DgroupId=ISAW -DartifactId=gluegen \
		-Dversion=1.1.0 -Dpackaging=jar -Dfile=$(GLUEGEN) \
		-DrepositoryId=sns-internal \
		-DpomFile=pom.xml \
		-Durl=scpexe://flathead.ornl.gov/maven/internal

old_deploy:
	mvn deploy:deploy-file -DgroupId=ISAW -DartifactId=jogl \
		-Dversion=1.0 -Dpackaging=jar -Dfile=jogl.jar \
		-DrepositoryId=sns-internal \
		-Durl=scpexe://flathead.ornl.gov/maven/internal
	mvn deploy:deploy-file -DgroupId=ISAW -DartifactId=jogl-full \
		-Dversion=1.0 -Dpackaging=jar -Dfile=jogl_full.jar \
		-DrepositoryId=sns-internal \
		-Durl=scpexe://flathead.ornl.gov/maven/internal

local_deploy:
	mvn install:install-file -DgroupId=ISAW -DartifactId=jogl \
		-Dversion=1.1.0 -Dpackaging=jar -Dfile=$(JOGL)
	mvn install:install-file -DgroupId=ISAW -DartifactId=gluegen \
		-Dversion=1.1.0 -Dpackaging=jar -Dfile=$(GLUEGEN)

tarball:
	tar cvf JOGL.Client.tar \
		./INSTALL ./jogl.jar ./Linux/*.so ./Win32/*.dll
	gzip JOGL.Client.tar

