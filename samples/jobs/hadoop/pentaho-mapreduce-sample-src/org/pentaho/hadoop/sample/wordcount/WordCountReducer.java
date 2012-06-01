package org.pentaho.hadoop.sample.wordcount;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class WordCountReducer extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
  private IntWritable totalWordCount = new IntWritable();

  public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output,
      Reporter reporter) throws IOException {
    int wordCount = 0;
    while (values.hasNext()) {
      wordCount += ((IntWritable) values.next()).get();
    }

    this.totalWordCount.set(wordCount);
    output.collect(key, this.totalWordCount);
  }
}