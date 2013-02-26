# Building and running from source

    $ mvn clean package 
    $ cd 
    $ sh ./target/runtime/bin/server -appConfig syslog-hdfs


# Running from executable distribution

    $ cd <install-directory>
    $ sh ./bin/run


To run the wordcount batch job start the batch-admin server


    hd-shell> server start --app batch_jobs 
    Running: /home/mpollack/projects/spring-hadoop-samples/shell/target/spring-hadoop-shell/runtime/bin/server -batchAdmin
    Server started.

To view the status of the server, use the command 'server status'

    hd-shell> server status
    server is running

To view the log of the server, use the command 'server log'.  Here we only show the last two lines of the log

    hd-shell> server log

    01:14:07.540 [server-1] INFO DispatcherServlet - FrameworkServlet 'Batch Servlet': initialization completed in 1550 ms
    01:14:07.542 [server-1] INFO log - Started SocketConnector@0.0.0.0:8081


You can launch the UI to browse jobs, execute jobs etc.

    hd-shell> launch --console batch_admin 

Or use the shell admin commands


    hd-shell> admin job-list
    name               description     executionCount  launchable  incrementable
    -----------------  --------------  --------------  ----------  -------------
    wordcountBatchJob  No description  1               true        false        
    exportProducts     No description  0               true        false        
    importProducts     No description  0               true        false        


Make sure that the input and output directories are emtpy

    hd-shell> hadoop fs -rmr /user/gutenberg/input/word
    command is:hadoop fs -rmr /user/gutenberg/input/word
    Deleted hdfs://localhost:9000/user/gutenberg/input/word

    hd-shell> hadoop fs -rmr /user/gutenberg/output/word
    command is:hadoop fs -rmr /user/gutenberg/output/word
    Deleted hdfs://localhost:9000/user/gutenberg/output/word

To run the wordcountBatchJob pass in the following values for the input and output directories.  


    hd-shell> admin job-start --jobName wordcountBatchJob --jobParameters mr.input=/user/gutenberg/input/word,mr.output=/user/gutenberg/output/word
    id  name               status     startTime  duration  exitCode 
    --  -----------------  ---------  ---------  --------  ---------
    1   wordcountBatchJob  COMPLETED  01:04:24   00:00:02  COMPLETED

If you want to run the job again, add another parameter to differentiate it from the previous execute, for example add 'run=2'

To view the results, look to see if the pre-packaged file 'nietzsche-chapter-1.txt' was copied to the input directory and view the output directory

    hd-shell> hadoop fs -ls /user/gutenberg/input/word
    command is:hadoop fs -ls /user/gutenberg/input/word
    Found 1 items
    -rw-r--r--   3 mpollack supergroup      51384 2013-02-26 01:04 /user/gutenberg/input/word/nietzsche-chapter-1.txt


    hd-shell> hadoop fs -ls /user/gutenberg/output/word
    command is:hadoop fs -ls /user/gutenberg/output/word
    Found 2 items
    -rw-r--r--   3 mpollack supergroup          0 2013-02-26 01:04 /user/gutenberg/output/word/_SUCCESS
    -rw-r--r--   3 mpollack supergroup      31752 2013-02-26 01:04 /user/gutenberg/output/word/part-r-00000


