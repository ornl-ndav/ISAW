

all:
	jikes -depend IsawGUI/Isaw.java


run-all:
	java IsawGUI.Isaw


jar:
	jar -cvf OverplotView.jar ./OverplotView


doc:
	javadoc -d docs/html                   \
          DataSetTools.dataset                 \
          DataSetTools.retriever               \
          DataSetTools.peak                    \
          DataSetTools.viewer                  \
          DataSetTools.viewer.Image            \
          DataSetTools.viewer.Graph            \
          DataSetTools.viewer.ViewerTemplate   \
          DataSetTools.math                    \
          DataSetTools.util                    \
          DataSetTools.instruments             \
          DataSetTools.operator                \
          DataSetTools.gsastools               \
          DataSetTools.components.containers   \
          DataSetTools.components.image        \
          DataSetTools.components.ui           \
          OverplotView                         \
          OverplotView.util                    \
          OverplotView.components.containers   \
          Command                              \
          IPNS.Runfile                         \
          graph                                \
          IsawGUI


clean:
	rm *.class; rm */*.class; rm */*/*.class; rm */*/*/*.class;


clean-doc:
	rm docs/html/*.html; rm docs/html/*/*.html; rm docs/html/*/*/*.html; 


