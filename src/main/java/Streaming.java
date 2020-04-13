import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.LazyIntIterator;

import java.io.RandomAccessFile;


public class Streaming {

	static final int kProgressStep = 100000;

    public static void main(String [] args) throws Exception {

        if(args.length!=1) {
            System.out.println("Usage: java Streaming <basename>");
            return;
        }
        BVGraph gr = BVGraph.loadOffline(args[0]);

		{
			System.out.println("total nodes: " + gr.numNodes());
			System.out.println("total arcs: " + gr.numArcs());

			RandomAccessFile out = new RandomAccessFile(args[0]+".stat","rw");
			out.writeInt(Integer.reverseBytes(gr.numNodes()));
			out.writeLong(Long.reverseBytes(gr.numArcs()));
			out.close();
		}

		long startTime = System.nanoTime();
		long stampTime = System.nanoTime();
        RandomAccessFile out = new RandomAccessFile(args[0]+".adj","rw");
		NodeIterator nIter = gr.nodeIterator();
		while(nIter.hasNext()) {
			int u = nIter.nextInt();
			LazyIntIterator eIter = nIter.successors();
			//System.out.println("visit node " + u);

			int v = 0;
			while((v = eIter.nextInt()) != -1) {
                out.writeInt(Integer.reverseBytes(v));
			}
            out.writeInt(Integer.reverseBytes(gr.numNodes()+u));
            if((u+1) % kProgressStep == 0) {
				System.out.print(u + " nodes processed\t" + ( (u*100.0f)/gr.numNodes()) + "%\t" + (kProgressStep*1e6f/(System.nanoTime()-stampTime)) + " nodes per microsecond ");
				stampTime = System.nanoTime();
				System.out.println((gr.numNodes() - u) / (( (stampTime - startTime) /  u)) + " seconds remaining");
			}
		}
		out.close();
	}


}
