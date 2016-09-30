package de.kaufhof.kafkaTopicCreationBug

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

class BlockingProducer(bootstrapServer: String, topic: String) {
  private val props = new Properties()
  props.put("bootstrap.servers", bootstrapServer)
  props.put("acks", "all")
  props.put("retries", 0:java.lang.Integer)
  props.put("batch.size", 16384:java.lang.Integer)
  props.put("linger.ms", 1:java.lang.Integer)
  props.put("buffer.memory", 33554432:java.lang.Integer)
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)

  def sendBlocking(message: String): Unit = {
    producer.send(new ProducerRecord[String, String](topic, message)).get()
  }

  sys.addShutdownHook(producer.close())
}
