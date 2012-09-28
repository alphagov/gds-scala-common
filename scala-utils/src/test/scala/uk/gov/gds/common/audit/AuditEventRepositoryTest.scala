package uk.gov.gds.common.audit

import org.scalatest.{GivenWhenThen, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.common.testutil.MongoDatabaseBackedTest
import uk.gov.gds.common.logging.Logging
import org.joda.time.DateTime
import uk.gov.gds.common.mongo.UnauthenticatedMongoDatabaseManagerForTests

class TestAuditEventRepositoryTest
  extends FunSuite
  with GivenWhenThen
  with ShouldMatchers
  with MongoDatabaseBackedTest
  with Logging {

  protected def databaseManager = UnauthenticatedMongoDatabaseManagerForTests

  test("Can store audit event") {
    TestAuditEventRepository.audit(
      AuditEvent(
        auditType = "test",
        tags = Map("tag" -> "value"),
        detail = Map("foo" -> "bar")))

    val events = TestAuditEventRepository.all
    events.total should be(1)

    val auditEvent = events.pageOfData.head

    auditEvent.auditType should be("test")
    auditEvent.tags("tag") should be("value")
    auditEvent.detail("foo") should be("bar")
    auditEvent.hostname should not be (null)
    auditEvent.timestamp should not be (null)
  }

  test("Can find audit event by type") {
    TestAuditEventRepository.audit(AuditEvent("foo"))
    TestAuditEventRepository.audit(AuditEvent("bar"))

    TestAuditEventRepository.all.total should be(2)

    val foundEvents = TestAuditEventRepository.find(auditType = "foo")

    foundEvents.total should be(1)
    foundEvents.pageOfData.head.auditType should be("foo")
  }

  test("Can find latest event by type") {
    val newest = AuditEvent("test")
    val oldest = AuditEvent(auditType = "test", timestamp = DateTime.now.minusDays(1))

    TestAuditEventRepository.audit(newest)
    TestAuditEventRepository.audit(oldest)

    TestAuditEventRepository.all.total should be(2)

    val itemsFromDatabase = TestAuditEventRepository.find(auditType = "test")

    itemsFromDatabase.total should be(2)
    itemsFromDatabase.pageOfData.head should be(newest)
  }

  test("Can find audit events by type and tags") {
    TestAuditEventRepository.audit(AuditEvent("foo", Map("tag" -> "1"), Map("test data" -> "older event")))
    TestAuditEventRepository.audit(AuditEvent("foo", Map("tag" -> "1"), Map("test data" -> "recent event")))
    TestAuditEventRepository.audit(AuditEvent("bar", Map("anothertag" -> "2")))

    // These are unsafe fast inserts - worth hanging around a bit on the off chance we head a secondary
    Thread.sleep(1000)

    val taggedEvents = TestAuditEventRepository.find("foo", Map("tag" -> "1"))

    taggedEvents.total should be(2)
    taggedEvents.pageOfData.head.auditType should be("foo")
    taggedEvents.pageOfData.head.detail.get("test data").get should be("recent event")
  }
}

