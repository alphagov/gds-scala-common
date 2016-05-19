package uk.gov.gds.common.localauth

import uk.gov.gds.common.mongo.repository.{ Ascending, IdentityBasedMongoRepository }
import uk.gov.gds.common.model.Authority

class NotFoundException(message: String) extends Exception(message)

abstract class AuthorityRepositoryBase extends IdentityBasedMongoRepository[Authority] {

  protected val databaseIdProperty = "urlSlug"

  override def createIndexes() {
    super.createIndexes()
    addIndex(index("agencyId" -> Ascending))
    addIndex(index("snacCode" -> Ascending))
  }

  def getAgencyIdFromSnacCode(snacCode: String) = loadBySnacCode(snacCode) match {
    case Some(authority) => authority.agencyId
    case _ => throw new NotFoundException("snacCode " + snacCode + " not found")
  }

  def loadByAgencyId(agencyId: Int): Option[Authority] = collection.findOne(where("agencyId" -> agencyId))

  def loadByUrlSlug(urlSlug: String) = load(urlSlug)

  def loadBySnacCode(snacCode: String): Option[Authority] = collection.findOne(where("snacCode" -> snacCode))

  def loadListBySnacCodes(strings: List[String]): List[Authority] = collection.find(where("snacCode" -> in(strings)))

  def updateAuthority(urlSlug: String, newAgencyId: Int) {
    collection.update(where("urlSlug" -> urlSlug), update("$set" -> query("agencyId" -> newAgencyId)))
  }
}
