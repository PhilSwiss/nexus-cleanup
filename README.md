![Nexus Cleanup Logo](https://repository-images.githubusercontent.com/284816242/b3c14d00-4a24-11eb-9336-4d5fc17af16b)

Nexus-Cleanup
=============

Remove old releases from a Nexus3-repository

This Groovy-script will purge old versions (sorted by version-number) from a repository hosted on [Nexus 3.x](https://www.sonatype.com/nexus-repository-oss)
You can set the desired repository and a maximum amount of versions at the begining of this script.

Information
===========

After upgrading from Nexus 2.x to Nexus 3.x, the build-in function for keeping the latest X releases was sadly gone.
Ofcourse there is an ongoing [feature-request](https://issues.sonatype.org/browse/NEXUS-10821) for that.
 
Tried the [script](https://stackoverflow.com/a/45894920) by [Matt Harrison](https://stackoverflow.com/users/1267396/matt-harrison) from StackOverflow for this problem, but the migration-tool in Nexus reseted all last_updated-values to the date of the migration, sad again.
 
Now tried to sort the releases by version via *'ORDER BY version DESC'*, but that resulted in a mess,
where a version **3.9.0** is newer than **3.11.0** and so on, not a suitable scenario.

In the end, I forked [Matt Harrison's script](https://stackoverflow.com/a/45894920) and added the nice [logoutput](https://stackoverflow.com/a/57604767) by [Neil201](https://stackoverflow.com/users/5998653/neil201)

Added some helper-lists and the [VersionComperator](https://gist.github.com/founddrama/971284) (a version sorter) by [Rob Friesel](https://gist.github.com/founddrama)

Finally linted the script with [npm-groovy-lint](https://github.com/nvuillam/npm-groovy-lint) to get a cleaner code, somehow

Disclaimer
===========

There is no warranty for the script or it's functionality, use it at your own risk. 

No licensing at the moment, because this script is put together from various sites with different licensing schemes.

The icon/logo consists of the Nexus Logo © by [Sonatype Inc.](https://www.sonatype.com) & the Broom Symbol © by [HiClipart](https://www.hiclipart.com)
