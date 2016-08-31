grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

forkConfig = [maxMemory: 1024, minMemory: 64, debug: false, maxPerm: 512]
grails.project.fork = [
   test: forkConfig, // configure settings for the test-app JVM
   run: forkConfig, // configure settings for the run-app JVM
   war: forkConfig, // configure settings for the run-war JVM
   console: forkConfig // configure settings for the Swing console JVM
]

grails {
   tomcat {
       jvmArgs = ["-Duser.timezone=UTC"]
   }
}

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()
        
        mavenRepo "http://repo.spring.io/milestone/"
        
        //grailsRepo "https://grails.org/plugins"

        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.20'
        runtime "mysql:mysql-connector-java:5.1.22"

      
      // ==============================================================================
      // Rule Engine
      // Esto es para que ande el HTTPBuilder
      // http://stackoverflow.com/questions/6552697/how-to-import-groovyx-net-http
       // http://grails.1312388.n4.nabble.com/Incompatibility-between-HTTPBuilder-and-Grails-1-3-3-td2535575.html
       // https://gist.github.com/839088
       // 
      runtime('org.codehaus.groovy.modules.http-builder:http-builder:0.5.2') {
         excludes "commons-logging", "xml-apis", "groovy"
      }
      
      runtime('com.thoughtworks.xstream:xstream:1.4.3') {
      }
      // ==============================================================================
    }

    plugins {
        //runtime ":hibernate:$grailsVersion"
        //runtime ":jquery:1.7.2"
        //runtime ":resources:1.1.6"
        
        runtime ':hibernate4:4.3.10' // or ':hibernate:3.6.10.14'
        runtime ':jquery:1.11.1'
        runtime ':database-migration:1.4.0'
        
        build ":tomcat:7.0.55.2"
        
        compile ':scaffolding:2.1.2'
        compile ':asset-pipeline:2.5.7'
        compile ':cache:1.1.8'
        compile ':quartz:1.0.2'
        compile "org.grails.plugins:cxf:2.1.1"
        
        
        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.4"

        //build ":tomcat:$grailsVersion"
        //runtime ":database-migration:1.1"
        //compile ':cache:1.0.0'
    }
}
