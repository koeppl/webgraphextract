import it.unimi.dsi.webgraph.BVGraph;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;

public class Transforma {

    public static void main(String [] args) throws Exception {
        if(args.length!=1) {
            System.out.println("Uso: java Transforma <basename>");
            return;
        }
        BVGraph gr = BVGraph.loadMapped(args[0]);

        // FileOutputStream outputStream = new FileOutputStream(args[0]+".adj");
        // outputStream.write(Integer.toString(gr.numNodes()).getBytes());
		// outputStream.write('\n');
        // outputStream.write(Long.toString(gr.numArcs()).getBytes());
		// outputStream.write('\n');
        // for(int i=0;i<gr.numNodes();i++) {
        //     int[] array = gr.successorArray(i);
		// 	if(array.length > 0) {
		// 		for(int j=0;j+1<array.length;j++) {
		// 			outputStream.write(Integer.toString(array[j]).getBytes());
		// 			outputStream.write(',');
		// 		}
		// 		outputStream.write(Integer.toString(array[array.length-1]).getBytes());
		// 	}
        //     //int j=0;
        //     // for(int k = 0; k < gr.numNodes(); ++k) {
        //     //     if(j<gr.outdegree(i) && array[j] == k) {
        //     //         outputStream.write('1');
        //     //         ++j;
        //     //     } else {
        //     //         outputStream.write('0');
        //     //     }
        //     // }
        //     // for(int j=0;j<gr.outdegree(i);j++) {
        //     //     outputStream.write(((new Integer(1+array[j]).toString()) + ",").getBytes());
        //     // }
        //     outputStream.write('\n');
        //     if(i%1000000==0) System.out.println("Nodes "+i);
        // }
        // outputStream.close();

        RandomAccessFile out = new RandomAccessFile(args[0]+".adj","rw");
        out.writeInt(Integer.reverseBytes(gr.numNodes()));
        out.writeLong(Long.reverseBytes(gr.numArcs()));
		System.out.println("total nodes: " + gr.numNodes());
		assert gr.numNodes() < Integer.MAX_VALUE - gr.numNodes(); // we want to store values up to 2*gr.numNodes() in 32bit
        for(int i=0;i<gr.numNodes();i++) {
            int [] array = gr.successorArray(i);
            // out.writeInt(Integer.reverseBytes(-(i+1)));
			assert array.length >= gr.outdegree(i);
            // out.writeInt(Integer.reverseBytes(gr.outdegree(i)));
            out.writeInt(Integer.reverseBytes(gr.numNodes()+i));
            for(int j=0;j<gr.outdegree(i);++j) {
                out.writeInt(Integer.reverseBytes(1+array[j]));
            }
            if(i%1000000==0) System.out.println(i + " nodes processed...");
        }
        out.close();
    }

}
