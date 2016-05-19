package uk.gov.gds.common.mongo.repository

import com.novus.salat.CaseClass
import uk.gov.gds.common.model.HasIdentity
import com.mongodb.WriteConcern

abstract class IdentityBasedMongoRepository[A <: CaseClass with HasIdentity](implicit m: Manifest[A])
    extends SimpleMongoRepository[A] {

  protected val databaseIdProperty: String

  override def load(id: String) = findOne(where(databaseIdProperty -> id))

  override def load(ids: List[String]) = SimpleMongoCursor(where(databaseIdProperty -> in(ids)))

  override def unsafeDelete(id: String) = collection.remove(where(databaseIdProperty -> id), WriteConcern.NORMAL)

  override def safeDelete(id: String) = collection.remove(where(databaseIdProperty -> id), WriteConcern.MAJORITY)

  override def startup() {
    super.startup()
    createIdIndex()
  }

  protected def createIdIndex() {
    addIndex(index(databaseIdProperty -> Ascending))
  }
}