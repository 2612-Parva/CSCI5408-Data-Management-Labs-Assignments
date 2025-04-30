package org.example;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

public class WordFrequencyCounter {
    public static void main(String[] args) {
        // Initialize Spark session
        SparkSession spark = SparkSession.builder()
                .appName("WordFrequencyCounter")
                .master("local[*]")
                .getOrCreate();

        JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());

        // Load the text file into an RDD
        JavaRDD<String> textData = sc.textFile("file:///home/parvapatel2612/sample.txt");

        // Extract words, clean them, and remove non-alphabetic characters
        JavaRDD<String> words = textData
                .flatMap(line -> Arrays.asList(line.replaceAll("[^a-zA-Z ]", "")
                        .toLowerCase().split("\\s+")).iterator())
                .filter(word -> !word.isEmpty());

        // Map each word to a (word, 1) pair, then count occurrences
        JavaPairRDD<String, Integer> wordFrequencies = words
                .mapToPair(word -> new Tuple2<>(word, 1))
                .reduceByKey(Integer::sum);

        // Sort words by frequency in descending order
        JavaPairRDD<Integer, String> swappedPairs = wordFrequencies.mapToPair(Tuple2::swap);
        JavaPairRDD<Integer, String> sortedWordFrequencies = swappedPairs.sortByKey(false);

        // Prepare output format: "word: count"
        JavaRDD<String> outputData = sortedWordFrequencies.map(entry -> entry._2() + ": " + entry._1());

        // Save results to a text file
        outputData.saveAsTextFile("file:///home/parvapatel2612/output.txt");

        // Display the output in the console
        List<Tuple2<Integer, String>> finalResults = sortedWordFrequencies.collect();
        finalResults.forEach(entry -> System.out.println(entry._2() + ": " + entry._1()));

        // Close Spark context
        sc.close();
    }
}


