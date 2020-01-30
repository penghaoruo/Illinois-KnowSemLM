mvn compile
MoreLib=$(echo target/dependency/*.jar | tr ' ' ':')
dataDir=data:data/
CLASSPATH=".:target/classes:$MoreLib:$dataDir:/home/hpeng7/gurobi650/linux64/lib/gurobi.jar:illinois-edison-3.0.18.jar"
#CLASSPATH=".:target/classes:$MoreLib:$dataDir:/home/hpeng7/gurobi650/linux64/lib/gurobi.jar:lib:lib/*"
nice java -Xmx30g -cp ${CLASSPATH} edu.illinois.cs.cogcomp.slm.main.MainClass
