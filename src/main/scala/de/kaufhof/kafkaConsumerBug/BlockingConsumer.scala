package de.kaufhof.kafkaConsumerBug

import java.util
import java.util.concurrent.LinkedBlockingQueue
import java.util.{Properties, UUID}

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, KafkaConsumer}
import scala.collection.JavaConversions._


class BlockingConsumer(bootstrapServer: String, groupId: String, topic: String) {

  private val props = new Properties()
  props.put("bootstrap.servers", bootstrapServer)
  props.put("group.id", groupId)
  props.put("enable.auto.commit", "true")
  props.put("auto.commit.interval.ms", "1000")
  props.put("session.timeout.ms", "30000")
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  private val consumer = new KafkaConsumer[String, String](props)
  consumer.subscribe(util.Arrays.asList(topic))

  @volatile private var shutdownRequested = false

  val readQueue = new LinkedBlockingQueue[ConsumerRecord[String, String]]()

  new Thread(new Runnable {
    override def run(): Unit = {
      while (!shutdownRequested) {
        val records = consumer.poll(100)
        if(records.count() > 0) {
          println(s"BlockingConsumer received ${records.count()} records")
          records.foreach(readQueue.add)
        }
      }
    }
  }, "test-kafka-consumer").start()

  sys.addShutdownHook{
    shutdownRequested = true
  }
}
