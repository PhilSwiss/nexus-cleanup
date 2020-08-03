//
// purge-old-release-from-nexus-3 by Matt Harrison
// - https://stackoverflow.com/questions/40742766/purge-old-release-from-nexus-3
// version-sorting by founddrama 
// - https://gist.github.com/founddrama/971284
// Changelog:
// - ignoring the sorting by "ORDER BY last_updated" because dates where resetted
//   by Nexus during migration from Nexus 2.x to Nexus 3.x 
// - added 3 lists (found, sorted & remove) for keeping the versions per component
//   in a sortable format
// - added sorting with founddrama's version sorter, this sorter works with text-
//   strings and sorts for example "1.0.14" higher than "1.0.2"
// - components will now be deleted if they appear in the remove-list
// - added more logoutput, inspired by the script by neil201 on stackoverflow
//

import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.Query;

def repositoryName = 'releases';
def maxArtifactCount = 100;

log.info("==================================================");
log.info(":::Cleanup script started...");
log.info("==================================================");
log.info(":::Operating on Repository: ${repositoryName}");
log.info("==================================================");

// Get a repository
def repo = repository.repositoryManager.get(repositoryName);
// Get a database transaction
def tx = repo.facet(StorageFacet).txSupplier().get();
try {
    // Begin the transaction
    tx.begin();

    int totalDelCompCount = 0;
    def previousComponent = null;
    def uniqueComponents = [];
    tx.findComponents(Query.builder().suffix(' ORDER BY group, name').build(), [repo]).each{component -> 
        if (previousComponent == null || (!component.group().equals(previousComponent.group()) || !component.name().equals(previousComponent.name()))) {
            uniqueComponents.add(component);
        }
        previousComponent = component;
    }

    uniqueComponents.each {uniqueComponent ->
        def componentVersions = tx.findComponents(Query.builder().where('group = ').param(uniqueComponent.group()).and('name = ').param(uniqueComponent.name()).suffix(' ORDER BY last_updated DESC').build(), [repo]);
        log.info("Processing Component: ${uniqueComponent.group()}, ${uniqueComponent.name()}");

        // log.info("Info: " + componentVersions.getClass());
        def foundVersions = [];
        componentVersions.eachWithIndex { component, index ->
          foundVersions.add(component.version());
         }
         log.info("Found Versions: ${foundVersions}")

        // version-sorting by founddrama 
        def versionComparator = { a, b ->
          def VALID_TOKENS = /.-_/
          a = a.tokenize(VALID_TOKENS)
          b = b.tokenize(VALID_TOKENS)
          
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

        sortedVersions = foundVersions.sort(versionComparator)
        
        log.info("Sorted Versions: ${sortedVersions}")

        removeVersions = sortedVersions.dropRight(maxArtifactCount)
        
        totalDelCompCount = totalDelCompCount + removeVersions.size();
        
        log.info("Remove Versions: ${removeVersions}");
        
        log.info("Component Total Count: ${componentVersions.size()}");

        log.info("Component Remove Count: ${removeVersions.size()}");

        if (componentVersions.size() > maxArtifactCount) {
            componentVersions.eachWithIndex { component, index ->
                if (component.version() in removeVersions) {
                    log.info("Deleting Component: ${component.group()}, ${component.name()} ${component.version()}")
                    // ------------------------------------------------
                    // uncomment to delete components and their assets
                    // tx.deleteComponent(component);
                    // ------------------------------------------------
                }
            }
        }
        log.info("==================================================");
     
    }
    log.info(" *** Total Deleted Component Count: ${totalDelCompCount} *** ");
    log.info("==================================================");

} finally {
    // End the transaction
    tx.commit();
}