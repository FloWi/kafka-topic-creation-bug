package de.kaufhof.kafkaConsumerBug

import java.util.UUID

import org.scalatest.{FlatSpec, MustMatchers}

import concurrent.duration._

trait Helper { self: MustMatchers with FreshKafkaTopics =>
  def doTheTest() = {
    val numberOfMessages: Int = 100

    val producer = new BlockingProducer(kafkaSeedNodes, testRandomIdentifier)
    val consumer = new BlockingConsumer(kafkaSeedNodes,
                                        topic = testRandomIdentifier,
                                        groupId = UUID.randomUUID().toString)

    1.to(numberOfMessages).foreach { i =>
      println(s"sending ${i}th message")
      producer.sendBlocking(i.toString)
    }

    val allMessagesFromBlockingConsumer =
      collection.mutable.MutableList.empty[String]

    def isSuccessful = allMessagesFromBlockingConsumer.size == numberOfMessages

    def readAllMessageFromBlockingConsumer(): Seq[String] = {
      val messages = collection.mutable.MutableList.empty[String]

      var messageFromBlockingConsumer: Option[String] = None
      while ({
        messageFromBlockingConsumer =
          Option(consumer.readQueue.poll()).map(_.value())
        messageFromBlockingConsumer
      }.isDefined) {
        println(
          s"received one message in blocking consumer: $messageFromBlockingConsumer")
        messages.+=(messageFromBlockingConsumer.get)
      }
      messages
    }

    val maxNumberOfTries = 5

    var numberOfTries = 0
    while (!isSuccessful && numberOfTries < maxNumberOfTries) {

      println(s"trying to read...")

      allMessagesFromBlockingConsumer.++=(readAllMessageFromBlockingConsumer())
      numberOfTries += 1
      Thread.sleep(500)
    }

    allMessagesFromBlockingConsumer.size mustBe numberOfMessages
  }
}

class WorkingKafkaConsumerSpec
    extends FlatSpec
    with MustMatchers
    with RandomizedTestEnvironment
    with FreshKafkaTopics
    with Helper {

  override def sleepEnabledAfterTopicCreation: Boolean = true

  it should "work with sleep enabled after topic creation" in {
    doTheTest()
  }
}

class NotWorkingKafkaConsumerSpec
    extends FlatSpec
    with MustMatchers
    with RandomizedTestEnvironment
    with FreshKafkaTopics
    with Helper {

  override def sleepEnabledAfterTopicCreation: Boolean = false

  it should "fail with no sleep enabled after topic creation" in {
    doTheTest()
  }

}
