
Dear OSX user,

We did a series of bug-fixes lately for all kinds of issues that annoyed our OSX users.
Most of these have been fixed.
We even made an installation image (.dmg) for the people that like to fire Spoon without too much to think about.

If you read this message however, here are the installation instructions:

After unzipping the distribution (which you did since you are reading this message) you can launch Spoon by running "sh spoon.sh" from a Terminal window.

Since we now have a test system you are even more encouraged to report problems on our JIRA system:

  http://jira.pentaho.org

At the time of writing (14/11/2007) there is one serious problem with the Java Runtime Environment made by Apple. 

  http://jira.pentaho.org/browse/PDI-458

The problem freezes the Java process and has no solution. There is no valid situation for the JVM to freeze like this and as such we consider this an issue for Apple to solve by releasing an update to the Java runtime. At the time of writing we could find no such update.  We applogize for the inconvenience if you are hit by this bug, but there is little to nothing that we can do about it at the moment.  The problem also does not occur on any other platform that we know of including Windows(XP/Vista), Linux(x86/x86_64) and Solaris.

All the best

The development team

