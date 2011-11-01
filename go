#!/bin/bash

BOOTSTRAP_DIR=bootstrap
if [ ! -d $BOOTSTRAP_DIR ]; then
  mkdir $BOOTSTRAP_DIR
fi

SPRING_ZIP=$BOOTSTRAP_DIR/spring-framework-3.1.0.RC1.zip
if [ ! -e $SPRING_ZIP ]; then
  echo "Downloading to $SPRING_ZIP"
  curl http://s3.amazonaws.com/dist.springframework.org/milestone/SPR/spring-framework-3.1.0.RC1.zip -o $SPRING_ZIP
fi

SPRING_HOME=$BOOTSTRAP_DIR/spring-framework-3.1.0.RC1
if [ ! -d $SPRING_HOME ]; then
  unzip -q -d $BOOTSTRAP_DIR $SPRING_ZIP
fi

ANT_TARBALL=$BOOTSTRAP_DIR/apache-ant-1.8.2-bin.tar.gz
if [ ! -e $ANT_TARBALL ]; then
  echo "Downloading to $ANT_TARBALL"
  curl http://archive.apache.org/dist/ant/binaries/apache-ant-1.8.2-bin.tar.gz -o $ANT_TARBALL
fi

ANT_HOME=$BOOTSTRAP_DIR/apache-ant-1.8.2
if [ ! -d $ANT_HOME ]; then
  tar xzf $ANT_TARBALL -C $BOOTSTRAP_DIR
fi

IVY_TARBALL=$BOOTSTRAP_DIR/apache-ivy-2.2.0-bin.tar.gz
if [ ! -e $IVY_TARBALL ]; then
  echo "Downloading to $IVY_TARBALL"
  curl http://archive.apache.org/dist/ant/ivy/2.2.0/apache-ivy-2.2.0-bin.tar.gz -o $IVY_TARBALL
fi

IVY_HOME=$BOOTSTRAP_DIR/apache-ivy-2.2.0
if [ ! -d $IVY_HOME ]; then
  tar xzf $IVY_TARBALL -C $BOOTSTRAP_DIR
fi

JRUBY_JAR=$BOOTSTRAP_DIR/jruby-complete-1.6.5.jar
if [ ! -e $JRUBY_JAR ]; then
  echo "Downloading to $JRUBY_JAR"
  curl http://jruby.org.s3.amazonaws.com/downloads/1.6.5/jruby-complete-1.6.5.jar -o $JRUBY_JAR
fi

export ANT_HOME=`cd $ANT_HOME; pwd`
export IVY_HOME=`cd $IVY_HOME; pwd`
export SPRING_HOME=`cd $SPRING_HOME; pwd`

java -jar $JRUBY_JAR -S rake $@
