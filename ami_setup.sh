#Update Packages
sudo yum update

sudo yum upgrade -y


#Install Java 17
sudo yum install java-17-amazon-corretto-devel -y

export JAVA_HOME="/usr/lib/jvm/java-17-amazon-corretto.x86_64"

export PATH=$PATH:$JAVA_HOME/bin



#Install MySQL
sudo yum install mariadb mariadb-server -y

sudo systemctl start mariadb

sudo mysqladmin -u root password "Avengers2232@"

mysqladmin -u root --password=Avengers2232@ --host=localhost --port=3306 create webapp_cloud

sudo systemctl enable mariadb



#Start Java Application
sudo systemctl daemon-reload

sudo systemctl start java_app.service

sudo systemctl enable java_app.service