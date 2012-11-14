package uk.gov.gds.common.model

case class MapitResponse(areas: Map[String, MapitArea])

case class MapitArea(codes: Map[String, String], name: String)

case class MapitLocalAuth(gss: String, opcs: String)