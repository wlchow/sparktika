package com.cloudera.se.wchow.sparktika

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.input.PortableDataStream
import org.apache.tika.metadata._
import org.apache.tika.parser._
import org.apache.tika.sax.BodyContentHandler
import java.io.InputStream
import java.io.DataInputStream
import scala.collection.mutable.ArrayBuffer


object SparkTika {
  def main (args: Array[String]) {


    // The 1st argument is the directory where the input images are located.
    if (args.length != 1 ) {
      println("usage: <input-directory>")
      System.exit(-1)
    }
    val inputDirName = args(0)

    val timestamp1 = System.currentTimeMillis()

    val sparkConf = new SparkConf()
    val sc = new SparkContext(sparkConf)

    println("DEBUG inputDirName:" + inputDirName)

    sc.hadoopConfiguration.set("mapreduce.input.fileinputformat.input.dir.recursive","true")

    def myTikaFunc(file: (String, PortableDataStream)) = {

      println("\nSTART =================================================")
      println("Processing file: " + file._1.toString)
      val myParser: AutoDetectParser = new AutoDetectParser()
      val inStream: InputStream = new DataInputStream(file._2.open)
      val contentHandler: BodyContentHandler = new BodyContentHandler(-1)
      val fileMetadata: Metadata = new Metadata()
      val context: ParseContext = new ParseContext()
      myParser.parse(inStream, contentHandler, fileMetadata, context)
      inStream.close()

      // Print the contents if you can. E.g. if this is a PDF then the contents of the PDF will be shown
      println("==content start ===============================================")
      println(contentHandler.toString())
      println("==content end   ===============================================")

      val stuffArray: ArrayBuffer[String] = ArrayBuffer()
      val metadataNames: Array[String] = fileMetadata.names()
      for (m <- metadataNames) {
        println( m + ": " + fileMetadata.get(m))
        stuffArray+=((file._1.toString) + " : " + m + " : " + fileMetadata.get(m))
      }
      println("END =================================================")

      stuffArray
    }

    sc.binaryFiles(inputDirName, 1).flatMap(x => myTikaFunc(x)).collect.foreach(println)

    val timestamp2 = System.currentTimeMillis()

    println("\n\n")
    println("Total time: " + (timestamp2-timestamp1) + " milliseconds.")
    println("\n\n")

    sc.stop()
  }


}
