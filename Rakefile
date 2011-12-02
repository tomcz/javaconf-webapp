require "rake/clean"
require "ant"

CLEAN.include("build")
CLOBBER.include("out")
CLOBBER.include("lib")

DIST_DIR ="build/dist"
COMPILE_DIR = "build/compile"
TEST_REPORT_DIR = "build/report"
COBERTURA_DIR = "build/cobertura"

desc "Create distributable WAR bundle"
task :default => [:clean, :run_tests, :make_war]

task :fetch_spring_libs do
  compile_libs = [
    "org.springframework.aop",
    "org.springframework.asm",
    "org.springframework.beans",
    "org.springframework.context",
    "org.springframework.context.support",
    "org.springframework.core",
    "org.springframework.expression",
    "org.springframework.jdbc",
    "org.springframework.orm",
    "org.springframework.transaction",
    "org.springframework.web",
    "org.springframework.web.servlet",
  ]
  test_libs = [
    "org.springframework.test",
  ]
  copy_spring_libs "lib/compile/source", "src", compile_libs
  copy_spring_libs "lib/compile/jar", "dist", compile_libs
  copy_spring_libs "lib/test/source", "src", test_libs
  copy_spring_libs "lib/test/jar", "dist", test_libs
  puts
end

def copy_spring_libs(target, type, libs)
  ant.mkdir :dir => target
  ant.copy :todir => target do
    fileset :dir => File.join(ENV["SPRING_HOME"], type) do
      libs.each { |lib| self.include :name => "#{lib}-*.jar" }
    end
  end
end

task :fetch_ivy_libs do
  ant.taskdef :resource => "org/apache/ivy/ant/antlib.xml" do
    classpath do
      fileset :dir => ENV["IVY_HOME"], :includes => "ivy-*.jar"
    end
  end
  ant.configure
  ant.resolve :file => "ivy.xml", :transitive => "no"
  ant.retrieve :pattern => "lib/[conf]/[type]/[artifact]-[revision].[ext]", :sync => "yes"
  puts
end

task :setup_paths => [:fetch_ivy_libs, :fetch_spring_libs] do
  ant.path :id => "compile.classpath" do
    fileset :dir => "lib/provided/jar"
    fileset :dir => "lib/compile/jar"
  end
  ant.path :id => "test.classpath" do
    fileset :dir => COMPILE_DIR
    fileset :dir => "lib/test/jar"
    path :refid => "compile.classpath"
  end
  ant.path :id => "cobertura.classpath" do
    fileset :dir => "lib/cobertura/jar"
  end
end

desc "Compile and create JARs"
task :make_jars => :setup_paths do
  make_jar "src/main", "compile.classpath", "javaconf-webapp.jar"
  make_jar "src/unit-tests", "test.classpath", "javaconf-webapp-unit-tests.jar"
  make_jar "src/integration-tests", "test.classpath", "javaconf-webapp-int-tests.jar"
  make_jar "src/system-tests", "test.classpath", "javaconf-webapp-system-tests.jar"
end

def make_jar(source_dir, classpath, jar_file_name)
  classes_dir = "#{COMPILE_DIR}/classes"
  ant.mkdir :dir => classes_dir
  ant.javac :srcdir => source_dir, :destdir => classes_dir, :classpathref => classpath,
            :source => "1.6", :target => "1.6", :includeantruntime => "no", :debug => "yes"
  ant.copy :todir => classes_dir, :includeemptydirs => "no" do
    fileset :dir => source_dir, :excludes => "**/*.java"
  end
  ant.jar :jarfile => "#{COMPILE_DIR}/#{jar_file_name}", :basedir => classes_dir
  ant.delete :dir => classes_dir
  puts
end

desc "Run all unit, integration and system tests"
task :run_tests => :make_jars do
  ant.mkdir :dir => TEST_REPORT_DIR
  ant.junit :fork => "yes", :forkmode => "perBatch", :printsummary => "yes",
            :haltonfailure => "no", :failureproperty => "tests.failed" do
    classpath :refid => "test.classpath"
    formatter :type => "xml"
    ["unit-tests", "integration-tests", "system-tests"].each do |test_type|
      batchtest :todir => TEST_REPORT_DIR do
        fileset :dir => "src/#{test_type}", :includes => "**/*Tests.java"
      end
    end
  end
  if ant.project.getProperty("tests.failed")
    ant.junitreport :todir => TEST_REPORT_DIR do
      fileset :dir => TEST_REPORT_DIR, :includes => "TEST-*.xml"
      report :todir => "#{TEST_REPORT_DIR}/html"
    end
    ant.fail :message => "One or more tests failed. Please check the test report for more info."
  end
  puts
end

task :make_war => :make_jars do
  ant.mkdir :dir => DIST_DIR
  ant.war :warfile => "#{DIST_DIR}/example.war", :webxml => "src/webapp/WEB-INF/web.xml" do
    fileset :dir => "src/webapp", :excludes => "**/web.xml"
    classes :dir => "src/main", :includes => "logback.xml"
    lib :dir => COMPILE_DIR, :excludes => "*-tests.jar"
    lib :dir => "lib/compile/jar"
  end
  puts
end

desc "Run application in Jetty"
task :run_jetty => [:clean, :make_jars] do
  ant.java :classname => "example.jetty.WebServer", :fork => "yes", :failonerror => "yes" do
    classpath :refid => "test.classpath"
  end
end

desc "Create cobertura code coverage report"
task :cobertura => :make_jars do
  cobertura_datafile = "#{COBERTURA_DIR}/cobertura.ser"
  cobertura_classes = "#{COBERTURA_DIR}/classes"
  cobertura_report = "#{COBERTURA_DIR}/report"

  ant.taskdef :name => "cobertura_instrument",
              :classname => "net.sourceforge.cobertura.ant.InstrumentTask",
              :classpathref => "cobertura.classpath"

  ant.taskdef :name => "cobertura_report",
              :classname => "net.sourceforge.cobertura.ant.ReportTask",
              :classpathref => "cobertura.classpath"

  ant.mkdir :dir => cobertura_classes
  ant.cobertura_instrument :todir => cobertura_classes, :datafile => cobertura_datafile do
    fileset :dir => COMPILE_DIR, :excludes => "*-tests.jar"
  end

  ant.junit :fork => "yes", :forkmode => "perBatch", :printsummary => "yes", :haltonfailure => "no" do
    sysproperty :key => "net.sourceforge.cobertura.datafile", :value => cobertura_datafile
    classpath do
      fileset :dir => cobertura_classes
      fileset :dir => COMPILE_DIR
      path :refid => "test.classpath"
      path :refid => "cobertura.classpath"
    end
    ["unit-tests", "integration-tests", "system-tests"].each do |test_type|
      batchtest { fileset :dir => "src/#{test_type}", :includes => "**/*Tests.java" }
    end
  end

  ant.mkdir :dir => cobertura_report
  ant.cobertura_report :format => "html", :datafile => cobertura_datafile,
                       :destdir => cobertura_report, :srcdir => "src/main"
end

desc "Create source bundle"
task :src_zip do
  ant.mkdir :dir => DIST_DIR
  ant.zip :destfile => "#{DIST_DIR}/example-src.zip", :basedir => "${basedir}" do
    exclude :name => "**/.DS_Store"
    exclude :name => "bootstrap/"
    exclude :name => "**/*.swp"
    exclude :name => "*.iws"
    exclude :name => "build/"
    exclude :name => "out/"
    exclude :name => "lib/"
  end
end
