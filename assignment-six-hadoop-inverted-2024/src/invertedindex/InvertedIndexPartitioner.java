package invertedindex;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class InvertedIndexPartitioner extends Partitioner<Text, LocationWritable> {

    @Override
    public int getPartition(Text key, LocationWritable value, int numPartitions) {
        int wordLength = key.getLength();

        // Assuming the maximum word length is 15 and the number of reducers is numPartitions
        int maxWordLength = 15;

        // Calculate the partition number based on the word length
        int partition = (wordLength - 1) / ((maxWordLength - 1) / numPartitions + 1);
        return numPartitions - 1 - Math.min(partition, numPartitions - 1); // Ensure partition is within valid range
    }
}