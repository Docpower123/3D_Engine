#!/bin/bash

name=$1
if [ "$name" = "" ]; then
    name="build"
fi

#cd src/main/java/org/example

function macsucks {



  for d in */ ; do
#      cd $d
#      echo $PWD
      x=$(echo "$d" | tr -d /)
      echo $x
#      cd $x
#      echo hi
#      cd ..


#      cd $d
  done
}

macsucks

#for d in */ ; do
##    echo "$d"
#  macsucks
#done


#javac -d ../../../build *.java
#cd ../../../build
#
#jar cfm $name.jar MANIFEST.MF ./org/example/*.class

