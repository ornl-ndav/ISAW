
deploy:
	@ echo "Please Use Continuum to Deploy... :-}"

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
		-Dversion=1.0 -Dpackaging=jar -Dfile=jogl.jar
	mvn install:install-file -DgroupId=ISAW -DartifactId=jogl-full \
		-Dversion=1.0 -Dpackaging=jar -Dfile=jogl_full.jar

tarball:
	tar cvf JOGL.Client.tar \
		./INSTALL ./jogl.jar ./Linux/*.so ./Win32/*.dll
	gzip JOGL.Client.tar

