#!/usr/bin/env groovy

def script = "postman-batch-result.groovy"
def cli = new CliBuilder(usage: "${script}: -[h] [file]")
cli.with {
    h longOpt: "help", "show usage information"
}

def opt = cli.parse(args)
if (!opt) {
    println "problem! error processing arguments"
    return
} else if (opt.h || opt.arguments().isEmpty()) {
    // show usage text for -h or --help option
    cli.usage()
    println " file  <input-file>"
    return
} else if (opt.arguments().size() == 0) {
    // less than perfect solution
    println "unknown argument; '${script} -h, --help' for more information"
    return
} else if (opt.arguments().size() > 1) {
    println "too many arguments; '${script} -h, --help' for more information"
    return
}

// handle all non-option arguments
def extraArguments = opt.arguments()
if (extraArguments) {
    def file = extraArguments[0]

    def batch = new Resource(file)
    def json = batch.json
    assert json instanceof Map

    def overall = json.results
    assert overall instanceof ArrayList
    println "\noverall\n======="
    println "result set count: " + overall.size
    println "record count: " + json.count
    assert overall.size == 1
    def results = overall[0]    // use `results` from hereon

    def testSummary = results.tests
    def criticalTests = []
    assert testSummary instanceof Map

    println "\ntestSummary\n==========="
    testSummary.each { k, v ->
        print (v ? "all" : "some"); println " '${k}' tests " + (v ? "PASSED" : "FAILED")
        if (k.contains("critical ::"))
            criticalTests.add(k)
    }

    assert criticalTests.size() > 0 : "no critical tests found!"
    println "\ncriticalTests\n============="
    criticalTests.each { println "'" + it +"'"}

    def testPassFailCounts = results.testPassFailCounts
    assert testPassFailCounts instanceof Map
    println "\ntestPassFailCounts\n=================="
    testPassFailCounts.each { k, v ->
        println "'${k}'"
        assert v instanceof Map
        v.each { r, c ->
            println "\t${r}: ${c}"
        }
    }

    def allTests = results.allTests
    assert allTests instanceof ArrayList
    assert allTests.size == json.count

    println "\nallTests\n========"
    for (test in allTests) {
        assert test instanceof Map
        test.each { k, v ->
            if (allTests.size < 10) println "'${k}': ${v}"
            if (k.contains("critical ::") && !v)
                assert false : "**** BIG PROBLEM: critical test ('${k}') failed! ****"
        }
    } // for

    println "\n~~~~ WOOHOO! EVERYTHING'S FINE: all critical tests passed ~~~~"
    0 // exit code

} // if