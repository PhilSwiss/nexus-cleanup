![Nexus Cleanup Logo](https://repository-images.githubusercontent.com/284816242/b3c14d00-4a24-11eb-9336-4d5fc17af16b)

Nexus-Cleanup
=============

Remove old releases from a Nexus3-repository

This Groovy-script will purge old versions (sorted by version-number) from a repository hosted on [Nexus 3.x](https://www.sonatype.com/nexus-repository-oss).

You can set the desired repository and a maximum amount of versions at the begining of this script.


Information
===========

After upgrading from Nexus 2.x to Nexus 3.x, the build-in function for keeping the latest X releases was sadly gone.
Ofcourse there is an ongoing [feature-request](https://issues.sonatype.org/browse/NEXUS-10821) for that.
 
I tried the [script](https://stackoverflow.com/a/45894920) by [Matt Harrison](https://stackoverflow.com/users/1267396/matt-harrison) from StackOverflow for this problem, but the migration-tool in Nexus had reset all _last_updated_-values to the date of the migration, sad again.
 
Now I tried to sort the releases by version via *'ORDER BY version DESC'*, but that resulted in a mess,
where a version **3.9.0** is newer than **3.11.0** and so on, not a suitable scenario.

In the end, I forked [Matt Harrison's script](https://stackoverflow.com/a/45894920) and the nice [logoutput](https://stackoverflow.com/a/57604767) by [Neil201](https://stackoverflow.com/users/5998653/neil201)

Added some helper-lists and the [VersionComperator](https://gist.github.com/founddrama/971284) (a version sorter) by [Rob Friesel](https://gist.github.com/founddrama)

Finally, I linted the script with [npm-groovy-lint](https://github.com/nvuillam/npm-groovy-lint) to get a cleaner code, somehow

Installation
============

1. add the line **nexus.scripts.allowCreation=true** to *$data-dir/etc/nexus.properties*
2. restart the *Nexus*-service, to apply the changes
3. in the webinterface of Nexus, go to *Administration* > *Tasks* and create a new *Execute script*-task
4. configure the task by filling out the required fields (Task name, Task frequency, etc.)
5. copy 'n' paste all lines from **nexus-cleanup.groovy** into the *Source*-field
6. disable further script creation, by adding a leading *#*-character to the line from step 1.
7. restart the *Nexus*-service, once again

More information about setting up custom scripts, can be found in this [article](https://support.sonatype.com/hc/en-us/articles/360045220393-Scripting-Nexus-Repository-Manager-3) from the [Sonatype Support Knowledge Base](https://support.sonatype.com/hc/en-us).


Disclaimer
===========

There is no warranty for the script or it's functionality, use it at your own risk. 

No licensing at the moment, because this script is put together from various sites with different licensing schemes.

The icon/logo consists of the Nexus Logo © by [Sonatype Inc.](https://www.sonatype.com) & a Broom Symbol © by [HiClipart](https://www.hiclipart.com).


Bug tracker
===========

If you have any suggestions, bug reports or annoyances please report them to the issue tracker at:

https://github.com/PhilSwiss/nexus-cleanup/issues


Contributing
============

Development of `nexus-cleanup` happens at GitHub: https://github.com/PhilSwiss/nexus-cleanup
