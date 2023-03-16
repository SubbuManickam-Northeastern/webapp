#Update Packages
sudo yum update -y

sudo yum upgrade -y


#Install Java 17
sudo yum install java-17-amazon-corretto-devel -y

export JAVA_HOME="/usr/lib/jvm/java-17-amazon-corretto.x86_64"

export PATH=$PATH:$JAVA_HOME/bin
