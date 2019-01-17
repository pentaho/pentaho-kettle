
Please use https://community.hitachivantara.com/community/products-and-solutions/pentaho/ for questions. If you find a defect, please report it in our JIRA system at https://jira.pentaho.com. If you are a subscription customer, please contact Pentaho Support for assistance.


Installation instructions:

  OS X:
    - You can launch Spoon by navigating to the folder this "read me" file is located in and double clicking on the "Data Integration" application icon, depending on your system.
  Ubuntu 16.04 and later:
    - The libwebkitgtk package needs to be installed.  This can be done by running "apt-get install libwebkitgtk-1.0.0"
    - On some installations of Ubuntu 16.04, Unity doesn't display the menu bar. In order to fix that, spoon.sh has a setting to disable this integration, "export UBUNTU_MENUPROXY=0". You can try to remove that setting if you wish to see if it works propery on your machine
  CentOS 6 Desktop:
    - The libwebkitgtk package needs to be installed.  This can be done by running "yum install libwebkitgtk"


Infobright Bulk Loader:

  For Windows, make sure to copy the appropriate file for your computer to your Windows system path (for example %WINDIR%/System32/): 

    libswt/win32/infobright_jni_64bit.dll (Windows 64-bit)
    libswt/win32/infobright_jni.dll       (Windows 32-bit)

  Rename the file to: infobright_jni.dll
