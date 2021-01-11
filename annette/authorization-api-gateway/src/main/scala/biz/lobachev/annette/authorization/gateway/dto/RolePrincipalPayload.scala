package biz.lobachev.annette.authorization.gateway.dto

import biz.lobachev.annette.authorization.api.role.AuthRoleId
import biz.lobachev.annette.core.model.auth.AnnettePrincipal
import play.api.libs.json.Json

case class RolePrincipalPayload(
  roleId: AuthRoleId,
  principal: AnnettePrincipal
)

object RolePrincipalPayload {
  implicit val format = Json.format[RolePrincipalPayload]
}