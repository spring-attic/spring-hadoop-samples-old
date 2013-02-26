# Building and running

    $ mvn clean package appassembler:assemble
    $ sh ./target/runtime/bin/server -appConfig syslog-hdfs

To send a message to syslog

    $ logger -p local3.info -t TESTING "Test Syslog Message"

Look at the data inside hadoop

    $ hadoop fs -ls /data

