Spring for Apache Hadoop Samples
================================

This readme file covers the Spring for Apache Hadoop samples run via the Spring Data hd-shell 
application.

To run these samples you need to have access to a Hadoop installation. The default settings 
are as follows:

hd.fs=hdfs://localhost:9000
mapred.job.tracker=localhost:9001

If you want to use a different host or port then either edit the config/config.properties file or
run the 'config set' command from inside the hd-shell application.

To start the hd-shell application enter 'sh ./bin/run' from the directory where you unzipped 
the spring-hadoop-samples.zip file.

