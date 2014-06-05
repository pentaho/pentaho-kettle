
On Ubuntu 12 and higher library libwebkitgtk-1.0-0 needs to be installed.

sudo apt-get install libwebkitgtk-1.0.0
 
On Centos you might also need to install this library:

yum install libwebkitgtk

It is installed in the standard desktop installation of Centos 6.5


On some installations of Ubuntu 14.04, Unity doesn't display the menu bar.
In order to fix that, spoon.sh has a setting to disable this integration,
export UBUNTU_MENUPROXY=0 . You can try to remove that setting if you wish 
to see if it works propery on your machine
