packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

variable "region" {
  default = "us-east-1"
}

variable "source_ami" {
  default = "ami-0dfcb1ef8550277af"
}

variable "ami_users" {
  default = ["332779329231"]
}

variable "AWS_ACCESS_KEY" {
  type = string
  default = ""
}

variable "AWS_SECRET_ACCESS_KEY" {
  type = string
  default = ""
}

locals {
  timestamp = regex_replace(timestamp(), "[- TZ:]", "")
}

source "amazon-ebs" "ec2-ami" {
  ami_name = "ec2-ami-${local.timestamp}"
  source_ami = var.source_ami
  instance_type = "t2.micro"
  region = var.region
  ssh_username = "ec2-user"
  ami_users = var.ami_users
  access_key = var.AWS_ACCESS_KEY
  secret_key = var.AWS_SECRET_ACCESS_KEY
}

build {
  sources = [
    "source.amazon-ebs.ec2-ami"
  ]

  provisioner "file" {
    source = "./webapp/target/webapp-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/webapp-0.0.1-SNAPSHOT.jar"
  }

  provisioner "file" {
    source = "./java_app.service"
    destination = "/tmp/java_app.service"
  }

  provisioner "shell" {
    inline = [
      "cd ~",
      "sudo mkdir -p webapp",
      "sudo chmod 755 webapp",
      "cd /tmp",
      "sudo mv webapp-0.0.1-SNAPSHOT.jar ~/webapp",
      "sudo mv java_app.service /etc/systemd/system",
      "cd ~/webapp",
      "sudo chmod 755 webapp-0.0.1-SNAPSHOT.jar",
      "cd"
    ]
  }

  provisioner "shell" {
    script = "./ami_setup.sh"
  }
