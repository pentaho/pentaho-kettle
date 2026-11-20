#!/bin/sh
# Password SSH server of ssh-test-environment/docker-compose.yml. Runs in an alpine container.
set -e

apk add --no-cache openssh-server
ssh-keygen -A
adduser -D -s /bin/sh testuser
echo "testuser:testpass123" | chpasswd
mkdir -p /home/testuser/.ssh
chown -R testuser:testuser /home/testuser/.ssh
chmod 700 /home/testuser/.ssh
sed -i "s/#PasswordAuthentication yes/PasswordAuthentication yes/" /etc/ssh/sshd_config
sed -i "s/#PermitRootLogin prohibit-password/PermitRootLogin no/" /etc/ssh/sshd_config
sed -i "s/#PubkeyAuthentication yes/PubkeyAuthentication yes/" /etc/ssh/sshd_config
exec /usr/sbin/sshd -D -e
