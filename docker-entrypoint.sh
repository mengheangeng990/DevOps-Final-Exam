#!/bin/bash
set -e

if [ -n "${MYSQL_ROOT_PASSWORD}" ]; then
  echo "root:${MYSQL_ROOT_PASSWORD}" | chpasswd
fi

mkdir -p /var/run/sshd
sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config
if ! grep -q "^PermitRootLogin" /etc/ssh/sshd_config; then
  echo "PermitRootLogin yes" >> /etc/ssh/sshd_config
else
  sed -i 's/^PermitRootLogin.*/PermitRootLogin yes/' /etc/ssh/sshd_config
fi

service ssh start
nginx
exec java -jar /app/idcard-manager.jar
