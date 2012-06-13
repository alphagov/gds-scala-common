package uk.gov.gds.common.play

import play.api.mvc.{ Result, Results }

trait JsonResult {
  def toResponse: Result;
}

trait JsonResults extends Results {
  import uk.gov.gds.common.json.JsonSerializer.toJson

  private val JSON_CONTENT_TYPE = "application/json; charset=utf-8"

  private def error(message: String) = toJson(Map("error" -> message))

  case class InternalServerErrorJsonResult(message: String) extends JsonResult {
    def toResponse = InternalServerError(error(message)).as(JSON_CONTENT_TYPE)
  }

  case class NotFoundJsonResult(message: String) extends JsonResult {
    def toResponse = NotFound(error(message)).as(JSON_CONTENT_TYPE)
  }

  case class OkJsonResult(json: String) extends JsonResult {
    def toResponse = Ok(json).as(JSON_CONTENT_TYPE)
  }

  case class BadJsonResult(message: String) extends JsonResult {
    def toResponse = BadRequest(message).as(JSON_CONTENT_TYPE)
  }
}