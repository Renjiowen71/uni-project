package invertedindex;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class InvertedIndexReducer extends Reducer<Text, LocationWritable, Text, LocationWritable> {

    private final LocationWritable result = new LocationWritable();

    public void reduce(Text key, Iterable<LocationWritable> values, Context context) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        //Deleting previous Map
        result.reset();
        //For each value, merge them together
        for (LocationWritable val : values) {
            result.merge(val);
        }
        context.getCounter(InvertedIndex.StatsCounter.UNIQUE_WORDS).increment(1);
        context.write(key, result);
        long executionTime = System.currentTimeMillis() - startTime;
        context.getCounter(InvertedIndex.StatsCounter.EXECUTION_TIME_REDUCER).increment(executionTime);
    }
}
