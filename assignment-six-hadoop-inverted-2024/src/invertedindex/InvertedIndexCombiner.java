package invertedindex;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class InvertedIndexCombiner extends Reducer<Text, LocationWritable, Text, LocationWritable> {

    private final LocationWritable result = new LocationWritable();

    public void reduce(Text key, Iterable<LocationWritable> values, Context context) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        //Deleting previous Map
        result.reset();
        //For each value, merge them together
        for (LocationWritable value : values) {
            result.merge(value);
        }

        context.write(key, result);
        long executionTime = System.currentTimeMillis() - startTime;
        context.getCounter(InvertedIndex.StatsCounter.EXECUTION_TIME_COMBINER).increment(executionTime);
    }
}
