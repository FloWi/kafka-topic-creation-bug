package de.kaufhof.kafkaTopicCreationBug

import java.util.UUID

import kafka.admin.AdminUtils
import kafka.utils.ZkUtils
import org.scalatest.{BeforeAndAfterAll, Suite}

trait RandomizedTestEnvironment {
  val testRandomIdentifier: String =
    UUID.randomUUID().toString.replace("-", "_")
}

trait FreshKafkaTopics
    extends BeforeAndAfterAll
    with RandomizedTestEnvironment { self: Suite =>

  def sleepEnabledAfterTopicCreation: Boolean

  val zkUrl = "127.0.0.1:2181"
  val kafkaSeedNodes = "127.0.0.1:9092"

  override protected def beforeAll(): Unit = {

    super.beforeAll()

    createKafkaTopic(testRandomIdentifier)
    if(sleepEnabledAfterTopicCreation) {
      Thread.sleep(1000)
    }
  }

  private def createKafkaTopic(topicName: String) = {
    import scala.concurrent.duration._
    val sessionTimeout = 10.seconds
    val connectionTimeout = 10.seconds
    val zkUtils =
      ZkUtils(zkUrl,
              sessionTimeout.toMillis.toInt,
              connectionTimeout.toMillis.toInt,
              isZkSecurityEnabled = false)

    println(s"creating topic $topicName")
    AdminUtils.createTopic(zkUtils, topicName, 16, 1)

    println(s"Successfully created topic: '$topicName")
  }
}
