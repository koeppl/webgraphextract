import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.LazyIntIterator;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;


public class Streaming {

	private static final Logger LOGGER = LoggerFactory.getLogger(Streaming.class);


    public static void main(String arg[]) throws Exception {
        SimpleJSAP jsap = new SimpleJSAP(Streaming.class.getName(),
                "Extracting the adjacency lists of a webgraph",
                new Parameter[]{
                    new FlaggedOption("logInterval", JSAP.LONG_PARSER, Long.toString(ProgressLogger.DEFAULT_LOG_INTERVAL), JSAP.NOT_REQUIRED, 'l', "log-interval",
                            "The minimum time interval between activity logs in milliseconds."),
                    new UnflaggedOption("basename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of the webgraph."),
                    new FlaggedOption("outfile", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 'o', "outfile", "The filename of the output."),
                    new FlaggedOption("type", JSAP.LONG_PARSER, "0", JSAP.NOT_REQUIRED, 't', "type", "output type (0: binary, 1: human readable, 2: adjacency matrix)."),
				});

        JSAPResult jsapResult = jsap.parse(arg);
        if(jsap.messagePrinted()) {
            System.exit(1);
        }

        final String basename = jsapResult.getString("basename");
        final long outputtype = jsapResult.getLong("type");
		final String outfilename = jsapResult.getString("outfile");
        ProgressLogger pl = new ProgressLogger(LOGGER, jsapResult.getLong("logInterval"), TimeUnit.MILLISECONDS);
		final ImmutableGraph graph = ImmutableGraph.loadOffline(basename);


		pl.start("Processing " + graph.numNodes() + " nodes and " + graph.numArcs() + " arcs.");
		pl.expectedUpdates = graph.numNodes();
		pl.itemsName = "nodes";

		if(outputtype == 1) {
			humanDump(graph,  outfilename, pl);
		} else if(outputtype == 2) {
			matrixDump(graph, outfilename, pl);
		} else {
			binaryDump(graph, outfilename, pl);
		}
    }



    public static void binaryDump(final ImmutableGraph gr, final String outfilename, final ProgressLogger pl) throws Exception {

		assert gr.numNodes() < Integer.MAX_VALUE - gr.numNodes(); // we want to store values up to 2*gr.numNodes() in 32bit


		{//!write stats

			RandomAccessFile out = new RandomAccessFile(outfilename + ".sta","rw");
			out.writeInt(Integer.reverseBytes(gr.numNodes()));
			out.writeLong(Long.reverseBytes(gr.numArcs()));
			out.close();
		}


		NodeIterator nIter = gr.nodeIterator();

		RandomAccessFile out = new RandomAccessFile(outfilename + ".dat", "rw");
		FileChannel channel = out.getChannel();

		while(nIter.hasNext()) {
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			DataOutputStream dataout = new DataOutputStream(bytestream);

			int u = nIter.nextInt();
			LazyIntIterator eIter = nIter.successors();

			int v = 0;
			while((v = eIter.nextInt()) != -1) {
                dataout.writeInt(Integer.reverseBytes(v));
			}

            dataout.writeInt(Integer.reverseBytes(gr.numNodes()+u));
			dataout.close();
			bytestream.flush();
			channel.write(ByteBuffer.wrap(bytestream.toByteArray()));
            pl.update();
		}
		channel.close();
		out.close();
		pl.done();
	}

    public static void humanDump(final ImmutableGraph g, final String outfilename, final ProgressLogger pl) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(outfilename));
			out.println("nodes=" + g.numNodes());
			out.println("arcs=" + g.numArcs());

        NodeIterator nIter = g.nodeIterator();
        while (nIter.hasNext()) {
            int u = nIter.nextInt();
            LazyIntIterator eIter = nIter.successors();

			out.print(u + " : ");
            int v = 0;
            while ((v = eIter.nextInt()) != -1) {
                    out.print(" " + v);
            }
			out.println();
            pl.update();
        }
		out.close();
        pl.done();
    }

    public static void matrixDump(final ImmutableGraph g, final String outfilename, final ProgressLogger pl) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(outfilename));
        NodeIterator nIter = g.nodeIterator();
        while (nIter.hasNext()) {
            nIter.nextInt();
            LazyIntIterator eIter = nIter.successors();
			int v = eIter.nextInt();
			for(int entry = 0; entry < g.numNodes(); ++entry) {
				if(v < entry && v >= 0) {
					v = eIter.nextInt();
				}
				out.print(v == entry ? '1' : '0');
				if(entry+1 < g.numNodes()) { out.print(','); }
			}
			out.println();
            pl.update();
        }
		out.close();
        pl.done();
    }

}
