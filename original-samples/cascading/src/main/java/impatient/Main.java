/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: https://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package impatient;

import java.util.Properties;

import cascading.flow.Flow;
import cascading.flow.FlowDef;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.operation.aggregator.Count;
import cascading.operation.regex.RegexSplitGenerator;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.property.AppProps;
import cascading.scheme.hadoop.TextDelimited;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;

//
// Modified Impatient Part 2 Main class
//
// Extracted the Flow setup into a separate method for reusability

public class
  Main
  {
  public static void
  main( String[] args )
   {
     String docPath = args[ 0 ];
	 String wcPath = args[ 1 ];

	 Properties properties = new Properties();
	 AppProps.setApplicationJarClass( properties, Main.class );
	 HadoopFlowConnector flowConnector = new HadoopFlowConnector( properties );

	 FlowDef flowDef = createFlowDef(docPath, wcPath);

	 // write a DOT file and run the flow
	 Flow wcFlow = flowConnector.connect( flowDef );
	 wcFlow.writeDOT( "dot/wc.dot" );
	 wcFlow.complete();
   }

  public static FlowDef
  createFlowDef( String docPath, String wcPath )
   {
    // create source and sink taps
    Tap docTap = new Hfs( new TextDelimited( true, "\t" ), docPath );
    Tap wcTap = new Hfs( new TextDelimited( true, "\t" ), wcPath );

    // specify a regex operation to split the "document" text lines into a token stream
    Fields token = new Fields( "token" );
    Fields text = new Fields( "text" );
    RegexSplitGenerator splitter = new RegexSplitGenerator( token, "[ \\[\\]\\(\\),.]" );
    // only returns "token"
    Pipe docPipe = new Each( "token", text, splitter, Fields.RESULTS );

    // determine the word counts
    Pipe wcPipe = new Pipe( "wc", docPipe );
    wcPipe = new GroupBy( wcPipe, token );
    wcPipe = new Every( wcPipe, Fields.ALL, new Count(), Fields.ALL );

    // connect the taps, pipes, etc., into a flow
    FlowDef flowDef = FlowDef.flowDef()
     .setName( "wc" )
     .addSource( docPipe, docTap )
     .addTailSink( wcPipe, wcTap );

    return flowDef;
    }
  }
