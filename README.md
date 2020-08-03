# nexus-cleanup
Remove old releases from a Nexus3-repository

This Groovy-script will purge old versions (sorted by version-number) from a repository hosted on Nexus 3.x
https://www.sonatype.com/nexus-repository-oss)
You can set the desired repository and a maximum amount of versions at the begining of this script.

Some more informations, if you are interested:

After upgrading from Nexus 2.x to Nexus 3.x, the build-in function for keeping the latest X releases was sadly gone.
Ofcourse there is an ongoing feature-request for that: https://issues.sonatype.org/browse/NEXUS-10821
 
Tried the script by Matt Harrison from StackOverflow for this problem,
https://stackoverflow.com/questions/40742766/purge-old-release-from-nexus-3,
but the migration-tool in Nexus reseted all last_updated-values to the date of the migration, sad again.
 
Now tried to sort the releases by version via ' ORDER BY version DESC', but that resulted in a mess,
where a version 3.9.0 is newer than 3.11.0 and so on, not a suitable scenario.
 
Finally forked Matt Harrison's script
https://stackoverflow.com/questions/40742766/purge-old-release-from-nexus-3

Forked the logoutput by Neil201
https://stackoverflow.com/questions/40742766/purge-old-release-from-nexus-3

Added some helper-lists and the VersionComperator (a version sorter) by Rob Friesel
https://gist.github.com/founddrama/971284
