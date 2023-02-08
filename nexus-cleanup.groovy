//
// nexus-cleanup.groovy by P.Schweizer / last changed 22.06.2021
//
// A script to remove old releases from a Nexus3-repository,
// - https://www.sonatype.com/nexus-repository-oss
// A fork of purge-old-release-from-nexus-3 by Matt Harrison
// - https://stackoverflow.com/a/45894920
// Logging forked from the script by Neil201
// - https://stackoverflow.com/a/57604767
// VersionComparator (a sorter for version-numbers) by Rob Friesel
// - https://gist.github.com/founddrama/971284)
//
// Changelog:
// - ignoring the sorting "ORDER BY last_updated" because dates where reset by
//   Nexus during migration from Nexus 2.x to Nexus 3.x
// - added 3 lists (foundVersions, sortedVersions & removeVersions) for keeping
//   the versions per component in a sortable format
// - added sorting with Rob Friesel's version comperator, this sorter handles
//   textstrings and sorts a release "1.0.14" higher than "1.0.2"
// - components will now be deleted if they appear in the removeVersions-list
// - added more logoutput, forked from the script by Neil201
// - added more documentation to the code
// - code cleaned up (as much as possible) by using npm-groovy-lint
// - fixed the links for the scripts where this script was forked from
// - added function for retention by agronlun
// - optimized the code, thanks to agronlun and emetriqChris
//

// Imports for the API
import org.sonatype.nexus.repository.storage.StorageFacet
import org.sonatype.nexus.repository.storage.Query
import org.joda.time.DateTime

// Configuration
def repositoryName = 'releases' // Name of your nexus-repository
def maxArtifactCount = 100      // Max. amount of artifacts to keep in repository
def retentionDays = 0           // Delete surplus artifacts only when older than X (0 = disable)

// VersionComperator (Sorter) by Rob Friesel
def versionComparator = { comperatorA, comperatorB ->
    def VALID_TOKENS = /.-_/
    a = comperatorA.tokenize(VALID_TOKENS)
    b = comperatorB.tokenize(VALID_TOKENS)

    for (i in 0..<Math.max(a.size(), b.size())) {
        if (i == a.size()) {
            return b[i].isInteger() ? -1 : 1
      } else if (i == b.size()) {
            return a[i].isInteger() ? 1 : -1
      }

        if (a[i].isInteger() && b[i].isInteger()) {
            int c = (a[i] as int) <=> (b[i] as int)
            if (c != 0) {
                return c
        }
      } else if (a[i].isInteger()) {
            return 1
      } else if (b[i].isInteger()) {
            return -1
      } else {
            int c = a[i] <=> b[i]
            if (c != 0) {
                return c
        }
      }
    }

    return 0
}

// Lets start
log.info('==================================================')
log.info(':::Cleanup script started...')
log.info('==================================================')
log.info(":::Operating on Repository: ${repositoryName}")
log.info('==================================================')

// Get a date
def retentionDate = DateTime.now().minusDays(retentionDays).dayOfMonth().roundFloorCopy()
// Get a repository
def repo = repository.repositoryManager.get(repositoryName)
// Check repository
if (repo.format instanceof org.sonatype.nexus.repository.maven.internal.Maven2Format) {
       log.info(":::Type of Repository: ${repo.format}")
} else {
       log.warn(":::WARNING: repository is NOT of type MAVEN2!")
}
log.info('==================================================')
// Get a database transaction
def tx = repo.facet(StorageFacet).txSupplier().get()
try {
    // Begin the transaction
    tx.begin()
    // Init
    int totalDelCompCount = 0
    def previousComponent = null
    def uniqueComponents = []

    // Get a collection of all components incl. their group
    tx.findComponents(Query.builder().suffix(' ORDER BY group, name').build(), [repo]).each { component ->
        if (previousComponent == null || (!component.group().equals(previousComponent.group()) || !component.name().
        equals(previousComponent.name()))) {
            uniqueComponents.add(component)
        }
        previousComponent = component
    }

    // Get a collection of all informations from a component
    uniqueComponents.each { uniqueComponent ->
        def componentVersions = tx.findComponents(Query.builder().where('group = ').param(uniqueComponent.group()).
        and('name = ').param(uniqueComponent.name()).suffix(' ORDER BY last_updated DESC').build(), [repo])
        log.info("Processing Component: ${uniqueComponent.group()}, ${uniqueComponent.name()}")

        // Get a list of all version-numbers form the collection
        def foundVersions = []
        componentVersions.eachWithIndex { component, index ->
            foundVersions.add(component.version())
         }
        log.info("Found Versions: ${foundVersions}")

        // Get a sorted list of all version-numbers (with the VersionComperator)
        sortedVersions = foundVersions.sort(versionComparator)
        log.info("Sorted Versions: ${sortedVersions}")

        // Get a list of all surplus version-numbers
        surplusVersions = sortedVersions.dropRight(maxArtifactCount)

        // Get a list of all surplus version-numbers older than retention date by agronlun
        def removeVersions = []
        componentVersions.eachWithIndex { component, index ->
            if (component.version() in surplusVersions) {
                def lastUpdateDate = component.lastUpdated()
                if (lastUpdateDate == null) {
                    log.warn("lastUpdated not found: ${component.group()}, ${component.name()} ${component.version()}")
                } else {
                    if (lastUpdateDate.isBefore(retentionDate)) {
                        removeVersions.add(component.version())
                    } else {
                        log.info("Version before retention: ${component.version()} - ${component.lastUpdated()}")
                    }
                }
            }
        }
        log.info("Remove Versions: ${removeVersions}")

        // Count total amount of surplus version-numbers
        totalDelCompCount = totalDelCompCount + removeVersions.size()
        log.info("Component Total Count: ${componentVersions.size()}")
        log.info("Component Remove Count: ${removeVersions.size()}")

        // If there are surplus versions for the component, delete them
        if (removeVersions.size() > 0) {
            componentVersions.eachWithIndex { comp, index ->
                if (comp.version() in removeVersions) {
                    def lastUpdated = comp.lastUpdated()
                    log.info("Deleting Component: ${comp.group()}, ${comp.name()} ${comp.version()} - ${lastUpdated}")
                    // -------------------------------------------------
                    // uncomment to delete surplus versions of component
                    // tx.deleteComponent(comp);
                    // -------------------------------------------------
                }
            }
        }
        log.info('==================================================')
    }
    // Show statistics at he end
    log.info(" *** Total Deleted Component Count: ${totalDelCompCount} *** ")
    log.info('==================================================')
} finally {
    // End the transaction and close it
    tx.commit()
    tx.close()
}
