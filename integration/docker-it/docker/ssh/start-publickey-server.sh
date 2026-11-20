#!/bin/sh
# Public key SSH server of ssh-test-environment/docker-compose.yml. Runs in an alpine container.
set -e

apk add --no-cache openssh-server
ssh-keygen -A
adduser -D -s /bin/sh keyuser
passwd -u keyuser
mkdir -p /home/keyuser/.ssh
cp /tmp/authorized_keys /home/keyuser/.ssh/authorized_keys
chown -R keyuser:keyuser /home/keyuser/.ssh
chmod 700 /home/keyuser/.ssh
chmod 600 /home/keyuser/.ssh/authorized_keys
sed -i "s/#PasswordAuthentication yes/PasswordAuthentication no/" /etc/ssh/sshd_config
sed -i "s/#PermitRootLogin prohibit-password/PermitRootLogin no/" /etc/ssh/sshd_config
sed -i "s/#PubkeyAuthentication yes/PubkeyAuthentication yes/" /etc/ssh/sshd_config
exec /usr/sbin/sshd -D -e
