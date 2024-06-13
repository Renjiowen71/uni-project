package invertedindex;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class InvertedIndex extends Configured implements Tool{
	public static enum StatsCounter {
		TOTAL_VALID_WORDS,
		TOTAL_VALID_WORD_LENGTH,
		UNIQUE_WORDS,
		TOTAL_DOCUMENTS,
		EXECUTION_TIME_MAPPER,
		EXECUTION_TIME_REDUCER,
		EXECUTION_TIME_COMBINER,
		DOCUMENT_ID_READ_FAILED,
		SKIPED_WORDS

	}
	public int run(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		if (args.length != 2) {
			System.err.printf("Usage: %s [generic options] <input> <output>\n",
					getClass().getSimpleName());
			ToolRunner.printGenericCommandUsage(System.err);
			return -1;
		}
		// Set configuration for stopwords filename and changing the textinputformat delimiter
		Configuration conf = new Configuration();
		conf.set("textinputformat.record.delimiter", "</Document>");
		conf.set("stopwords.filename","stop_words.txt");
		// Creating job
		Job job = Job.getInstance(conf, "inverted index");
		job.setJarByClass(InvertedIndex.class);
		// Setting output key and value
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LocationWritable.class);
		// Setting mapper, reducer, combiner, partitioner, and comparator
		job.setMapperClass(InvertedIndexMapper.class);
		job.setReducerClass(InvertedIndexReducer.class);
		job.setNumReduceTasks(3);
		job.setCombinerClass(InvertedIndexCombiner.class);
		job.setPartitionerClass(InvertedIndexPartitioner.class);
		job.setSortComparatorClass(InvertedIndexComparator.class);
		//Setting format class
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileSystem fs = FileSystem.get(conf);
		//Check if output path (args[1])exist or not
		if(fs.exists(new Path(args[1]))){
			//If exist delete the output path
			fs.delete(new Path(args[1]),true);
		}
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		int jobComp = job.waitForCompletion(true)?0:1;
		long executionTime = System.currentTimeMillis() - start;
		System.out.println("runTime "+executionTime);
		return jobComp;
	}

  	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new InvertedIndex(), args);
		System.exit(exitCode);
	}
}


