#!/bin/bash

if test ! -f "tools/asm-9.3.jar"; then
  echo "Downloading asm-9.3.jar from Maven central"
  mkdir -p tools
  cd tools
  wget "https://search.maven.org/remotecontent?filepath=org/ow2/asm/asm/9.3/asm-9.3.jar" -O "asm-9.3.jar" -q
  cd ..
fi

if test ! -f "tools/asm-util-9.3.jar"; then
  echo "Downloading asm-util-9.3.jar from Maven central"
  mkdir -p tools
  cd tools
  wget "https://search.maven.org/remotecontent?filepath=org/ow2/asm/asm-util/9.3/asm-util-9.3.jar" -O "asm-util-9.3.jar" -q
  cd ..
fi

echo "Compiling Edina"
mvn clean package -q

echo "Generating asm code"
java -classpath "tools/asm-9.3.jar:tools/asm-util-9.3.jar" org.objectweb.asm.util.ASMifier edinaj/target/classes/dev/cerus/edina/edinaj/asm/Stack.class > asm_stack.txt
java -classpath "tools/asm-9.3.jar:tools/asm-util-9.3.jar" org.objectweb.asm.util.ASMifier edinaj/target/classes/dev/cerus/edina/edinaj/asm/Natives.class > asm_natives.txt
java -classpath "tools/asm-9.3.jar:tools/asm-util-9.3.jar" org.objectweb.asm.util.ASMifier edinaj/target/classes/dev/cerus/edina/edinaj/asm/App.class > asm_app.txt
java -classpath "tools/asm-9.3.jar:tools/asm-util-9.3.jar" org.objectweb.asm.util.ASMifier edinaj/target/classes/dev/cerus/edina/edinaj/asm/Launcher.class > asm_launcher.txt
echo "Done"