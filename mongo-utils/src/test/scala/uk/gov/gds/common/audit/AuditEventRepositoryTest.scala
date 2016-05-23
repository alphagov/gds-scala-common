package uk.gov.gds.common.audit

import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{ FunSuite, GivenWhenThen }
import uk.gov.gds.common.logging.Logging
import uk.gov.gds.common.mongo.UnauthenticatedMongoDatabaseManagerForTests
import uk.gov.gds.common.testutil.MongoDatabaseBackedTest

class AuditEventRepositoryTest
    extends FunSuite
    with GivenWhenThen
    with ShouldMatchers
    with MongoDatabaseBackedTest
    with Logging {

  protected def databaseManager = UnauthenticatedMongoDatabaseManagerForTests

  test("Can store audit event") {
    TestAuditEventRepository.audit(
      auditType = "test",
      tags = Map("tag" -> "value"),
      detail = Map("foo" -> "bar")
    )

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
    TestAuditEventRepository.audit("foo")
    TestAuditEventRepository.audit("bar")

    TestAuditEventRepository.all.total should be(2)

    val foundEvents = TestAuditEventRepository.find(auditType = "foo")

    foundEvents.total should be(1)
    foundEvents.pageOfData.head.auditType should be("foo")
  }

  test("Can find latest event by type") {
    val newest = AuditEvent("test")
    val oldest = AuditEvent(auditType = "test", timestamp = DateTime.now.minusDays(1))

    TestAuditEventRepository.testAudit(newest)
    TestAuditEventRepository.testAudit(oldest)

    TestAuditEventRepository.all.total should be(2)

    val itemsFromDatabase = TestAuditEventRepository.find(auditType = "test")

    itemsFromDatabase.total should be(2)
    itemsFromDatabase.pageOfData.head.auditType should be(newest.auditType)
  }

  test("Can find audit events by type and tags") {
    TestAuditEventRepository.audit("foo", Map("tag" -> "1"), Map("test data" -> "older event"))
    TestAuditEventRepository.audit("foo", Map("tag" -> "1"), Map("test data" -> "recent event"))
    TestAuditEventRepository.audit("bar", Map("anothertag" -> "2"))

    val taggedEvents = TestAuditEventRepository.find("foo", Map("tag" -> "1"))

    taggedEvents.total should be(2)
    taggedEvents.pageOfData.head.auditType should be("foo")
  }

  test("Can find audit events by type and timestamp") {
    TestAuditEventRepository.testAudit(AuditEvent("foo", Map("tag" -> "1"), Map("test data" -> "older event"), timestamp = DateTime.parse("2012-12-03T12:00:00.000Z")))
    TestAuditEventRepository.testAudit(AuditEvent("foo", Map("tag" -> "1"), Map("test data" -> "older event"), timestamp = DateTime.parse("2012-12-03T12:01:00.000Z")))
    TestAuditEventRepository.testAudit(AuditEvent("foo", Map("tag" -> "1"), Map("test data" -> "older event"), timestamp = DateTime.parse("2012-12-03T12:02:00.000Z")))
    TestAuditEventRepository.testAudit(AuditEvent("bar", Map("tag" -> "1"), Map("test data" -> "older event"), timestamp = DateTime.parse("2012-12-03T12:03:00.000Z")))
    TestAuditEventRepository.testAudit(AuditEvent("foo", Map("tag" -> "1"), Map("test data" -> "older event"), timestamp = DateTime.parse("2012-12-03T12:04:00.000Z")))

    val taggedEvents = TestAuditEventRepository.find(
      "foo",
      Map("tag" -> "1"),
      Some(DateTime.parse("2012-12-03T12:00:30.000Z")),
      Some(DateTime.parse("2012-12-03T12:03:30.000Z"))
    )

    taggedEvents.total should be(2)
    taggedEvents.pageOfData.head.auditType should be("foo")
  }

  test("Can find only one audit event by type and tags") {
    TestAuditEventRepository.audit("foo", Map("tag" -> "1"), Map("test data" -> "older event"))
    TestAuditEventRepository.audit("foo", Map("tag" -> "1"), Map("test data" -> "recent event"))
    TestAuditEventRepository.audit("bar", Map("anothertag" -> "2"))

    val taggedEvents = TestAuditEventRepository.find(
      "foo",
      Map("tag" -> "1")
    )

    taggedEvents.total should be(2)

    val taggedEventOption = TestAuditEventRepository.findOne("foo", Map("tag" -> "1"))

    taggedEventOption should not be None
    taggedEventOption.get.auditType should be("foo")
  }
}

