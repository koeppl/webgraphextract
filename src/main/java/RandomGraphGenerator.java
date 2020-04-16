import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.LazyIntIterator;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;


public class RandomGraphGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(RandomGraphGenerator.class);


    public static void main(String arg[]) throws Exception {
        SimpleJSAP jsap = new SimpleJSAP(RandomGraphGenerator.class.getName(),
                "Extracting the adjacency lists of a webgraph",
                new Parameter[]{
                    new FlaggedOption("logInterval", JSAP.LONG_PARSER, Long.toString(ProgressLogger.DEFAULT_LOG_INTERVAL), JSAP.NOT_REQUIRED, 'l', "log-interval",
                            "The minimum time interval between activity logs in milliseconds."),
                    new UnflaggedOption("basename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of the webgraph."),
                    new FlaggedOption("nodes", JSAP.LONG_PARSER, "0", JSAP.NOT_REQUIRED, 'n', "nodes", "number of nodes"),
                    new FlaggedOption("edges", JSAP.LONG_PARSER, "0", JSAP.NOT_REQUIRED, 'm', "edges", "number of edges"),
				});

        JSAPResult jsapResult = jsap.parse(arg);
        if(jsap.messagePrinted()) {
            System.exit(1);
        }

        final String basename = jsapResult.getString("basename");
        final int num_nodes = ((Long) jsapResult.getLong("nodes")).intValue();
        final int num_edges = ((Long) jsapResult.getLong("edges")).intValue();

        ProgressLogger pl = new ProgressLogger(LOGGER, jsapResult.getLong("logInterval"), TimeUnit.MILLISECONDS);

		pl.start("Generating " + num_nodes + " nodes and " + num_edges + " arcs.");
		pl.expectedUpdates = num_edges;
		pl.itemsName = "edges";

		Random rand = new Random();

		Set<Map.Entry<Integer,Integer>> s = new HashSet<Map.Entry<Integer,Integer>>();
		while(s.size() < num_edges) {
			s.add(Map.entry(rand.nextInt(num_nodes), rand.nextInt(num_nodes)));
		}
		ArrayList<Map.Entry<Integer,Integer>> pairs = new ArrayList<Map.Entry<Integer,Integer>>();
		for(Map.Entry<Integer,Integer> e : s) {
			pairs.add(e);
		}
		pairs.sort(Comparator.comparing(p -> p.getKey()));

		// File tempFile = File.createTempFile("hello", ".tmp");
		// tempFile.deleteOnExit();
		String arcsfile = basename + ".arcs";
		FileWriter writer = new FileWriter(arcsfile);

		for(Map.Entry<Integer,Integer> e : pairs) {
			writer.write(e.getKey().toString() + " " + e.getValue().toString() + "\n");
			pl.update();
		}
		writer.close();
		pl.done();

		final ImmutableGraph graph = ArcListASCIIGraph.load(arcsfile);
		BVGraph.store(graph, basename, pl);
    }

}
