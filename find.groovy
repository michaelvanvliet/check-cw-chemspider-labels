@Grab(group='org.codehaus.gpars', module='gpars', version='1.1.0')

import groovyx.gpars.GParsPool
import groovy.json.JsonSlurper

// script to find multiple preferred labels in Chemspider branch
def filename = 'conceptsWithMultiplePreferredLabels.txt'
new File(filename).delete()
def resultsFile = new File(filename)

new File('20131212-linksets').eachFile { chemspiderTTL ->

    if ("${chemspiderTTL.name}".contains('chemspider') ){

        resultsFile <<  "\nStarting ${chemspiderTTL.name}\n"

        chemspiderTTL.eachLine { cTTLLine ->

            def UUID = "${cTTLLine.split('skos:')[0].split(':')[-1]}".trim()

            if (UUID.size() == 36){

                try {
                    def concept = new JsonSlurper().parseText(new URL('http://conceptwiki.nbiceng.net/web-ws/concept/get?uuid='+UUID+'&branch=4').text)

                    def labels = [:]
                    concept.labels.each { label ->
                        if (label.type == 'PREFERRED'){
                            if (!labels[label.language.code]) { labels[label.language.code] = [] }
                            labels[label.language.code] << label.text
                        }
                    }

                    GParsPool.withPool() {
                        labels.eachParallel { lang, labelsFound ->
                            if (labelsFound.size() >= 2){
                                resultsFile << "${UUID}\t${labelsFound.join(', ')}\n"
                                println UUID + ": " + labelsFound.join(', ')
                            }
                        }
                    }
                } catch (e) {
                    println e
                }
            }
        }
    }
}
