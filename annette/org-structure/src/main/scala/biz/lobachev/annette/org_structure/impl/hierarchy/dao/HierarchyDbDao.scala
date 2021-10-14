/*
 * Copyright 2013 Valery Lobachev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biz.lobachev.annette.org_structure.impl.hierarchy.dao

import akka.Done
import akka.stream.Materializer
import biz.lobachev.annette.core.model._
import biz.lobachev.annette.core.model.auth._
import biz.lobachev.annette.microservice_core.db.{CassandraQuillDao, CassandraTableBuilder}
import biz.lobachev.annette.org_structure.api.hierarchy
import biz.lobachev.annette.org_structure.api.hierarchy.ItemTypes.ItemType
import biz.lobachev.annette.org_structure.api.hierarchy._
import biz.lobachev.annette.org_structure.impl.hierarchy.entity.HierarchyEntity
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import java.time.OffsetDateTime
import scala.collection.immutable.{Seq, _}
import scala.concurrent.{ExecutionContext, Future}

private[impl] class HierarchyDbDao(
  override val session: CassandraSession
)(implicit
  val ec: ExecutionContext,
  val materializer: Materializer
) extends CassandraQuillDao {

  import ctx._

  private implicit val itemTypeEncoder = genericStringEncoder[ItemType]
  private implicit val itemTypeDecoder = genericStringDecoder[ItemType](ItemTypes.withName)
  println(itemTypeEncoder.toString)
  println(itemTypeDecoder.toString)

  private val itemSchema           = quote(querySchema[ItemRecord]("org_items"))
  private val personPositionSchema = quote(querySchema[PersonPosition]("person_positions"))
  private val chiefUnitSchema      = quote(querySchema[ChiefUnitRecord]("chief_units"))
  private val externalIdSchema     = quote(querySchema[ExternalIdRecord]("external_ids"))

  private implicit val insertItemMeta = insertMeta[ItemRecord]()
  private implicit val updateItemMeta = updateMeta[ItemRecord](_.id)

  println(insertItemMeta.toString)
  println(updateItemMeta.toString)

  def createTables(): Future[Done] = {
    import CassandraTableBuilder.types._
    for {
      _ <- session.executeCreateTable(
             CassandraTableBuilder("org_items")
               .column("id", Text, true)
               .column("org_id", Text)
               .column("parent_id", Text)
               .column("root_path", List(Text))
               .column("name", Text)
               .column("type", Text)
               .column("category_id", Text)
               .column("source", Text)
               .column("external_id", Text)
               .column("children", List(Text))
               .column("chief", Text)
               .column("person_limit", Int)
               .column("persons", Set(Text))
               .column("org_roles", Set(Text))
               .column("updated_at", Timestamp)
               .column("updated_by", Text)
               .build
           )
      _ <- session.executeCreateTable(
             CassandraTableBuilder("person_positions")
               .column("person_id", Text)
               .column("position_id", Text)
               .withPrimaryKey("person_id", "position_id")
               .build
           )

      _ <- session.executeCreateTable(
             CassandraTableBuilder("chief_units")
               .column("position_id", Text)
               .column("unit_id", Text)
               .withPrimaryKey("position_id", "unit_id")
               .build
           )

      _ <- session.executeCreateTable(
             CassandraTableBuilder("external_ids")
               .column("external_id", Text, true)
               .column("item_id", Text)
               .build
           )
    } yield Done
  }

  def createOrganization(event: HierarchyEntity.OrganizationCreated) = {
    val itemRecord = ItemRecord(
      id = event.orgId,
      orgId = event.orgId,
      parentId = hierarchy.ROOT,
      rootPath = List(event.orgId),
      name = event.name,
      `type` = ItemTypes.Unit,
      categoryId = event.categoryId,
      source = event.source,
      externalId = event.externalId,
      children = Some(List.empty),
      updatedAt = event.createdAt,
      updatedBy = event.createdBy
    )
    ctx.run(itemSchema.insert(lift(itemRecord)))
  }

  def deleteOrganization(event: HierarchyEntity.OrganizationDeleted) =
    ctx.run(itemSchema.filter(_.id == lift(event.orgId)).delete)

  def createUnit(event: HierarchyEntity.UnitCreated) = {
    val itemRecord = ItemRecord(
      id = event.unitId,
      orgId = OrgItemKey.extractOrgId(event.unitId),
      parentId = event.parentId,
      rootPath = event.rootPath.toList,
      name = event.name,
      `type` = ItemTypes.Unit,
      categoryId = event.categoryId,
      source = event.source,
      externalId = event.externalId,
      children = Some(List.empty),
      updatedAt = event.createdAt,
      updatedBy = event.createdBy
    )
    for {
      _ <- ctx.run(itemSchema.insert(lift(itemRecord)))
      _ <- ctx.run(
             itemSchema
               .filter(_.id == lift(event.parentId))
               .update(
                 _.children  -> lift(Option(event.parentChildren.toList)),
                 _.updatedAt -> lift(event.createdAt),
                 _.updatedBy -> lift(event.createdBy)
               )
           )
    } yield Done
  }

  def deleteUnit(event: HierarchyEntity.UnitDeleted) =
    for {
      _ <- ctx.run(itemSchema.filter(_.id == lift(event.unitId)).delete)
      _ <- ctx.run(
             itemSchema
               .filter(_.id == lift(event.parentId))
               .update(
                 _.children  -> lift(Option(event.parentChildren.toList)),
                 _.updatedAt -> lift(event.deletedAt),
                 _.updatedBy -> lift(event.deletedBy)
               )
           )
    } yield Done

  def assignCategory(event: HierarchyEntity.CategoryAssigned) =
    ctx.run(
      itemSchema
        .filter(_.id == lift(event.itemId))
        .update(
          _.categoryId -> lift(event.categoryId),
          _.updatedAt  -> lift(event.updatedAt),
          _.updatedBy  -> lift(event.updatedBy)
        )
    )

  def assignChief(event: HierarchyEntity.ChiefAssigned) =
    for {
      _ <- ctx.run(
             itemSchema
               .filter(_.id == lift(event.unitId))
               .update(
                 _.chief     -> lift(Option(event.chiefId)),
                 _.updatedAt -> lift(event.updatedAt),
                 _.updatedBy -> lift(event.updatedBy)
               )
           )
      _ <- ctx.run(
             chiefUnitSchema.insert(
               lift(
                 ChiefUnitRecord(
                   positionId = event.chiefId,
                   unitId = event.unitId
                 )
               )
             )
           )
    } yield Done

  def unassignChief(event: HierarchyEntity.ChiefUnassigned) = {
    val none: Option[CompositeOrgItemId] = None
    for {
      _ <- ctx.run(
             itemSchema
               .filter(_.id == lift(event.unitId))
               .update(
                 _.chief     -> lift(none),
                 _.updatedAt -> lift(event.updatedAt),
                 _.updatedBy -> lift(event.updatedBy)
               )
           )
      _ <- ctx.run(
             chiefUnitSchema
               .filter(r =>
                 r.positionId == lift(event.chiefId) &&
                   r.unitId == lift(event.unitId)
               )
               .delete
           )
    } yield Done
  }

  def createPosition(event: HierarchyEntity.PositionCreated) = {
    val itemRecord = ItemRecord(
      id = event.positionId,
      orgId = OrgItemKey.extractOrgId(event.positionId),
      parentId = event.parentId,
      rootPath = event.rootPath.toList,
      name = event.name,
      `type` = ItemTypes.Position,
      categoryId = event.categoryId,
      source = event.source,
      externalId = event.externalId,
      personLimit = Some(event.limit),
      persons = Some(Set.empty),
      orgRoles = Some(Set.empty),
      updatedAt = event.createdAt,
      updatedBy = event.createdBy
    )
    for {
      _ <- ctx.run(itemSchema.insert(lift(itemRecord)))
      _ <- ctx.run(
             itemSchema
               .filter(_.id == lift(event.parentId))
               .update(
                 _.children  -> lift(Option(event.parentChildren.toList)),
                 _.updatedAt -> lift(event.createdAt),
                 _.updatedBy -> lift(event.createdBy)
               )
           )
    } yield Done
  }

  def deletePosition(event: HierarchyEntity.PositionDeleted) =
    for {
      _ <- ctx.run(itemSchema.filter(_.id == lift(event.positionId)).delete)
      _ <- ctx.run(
             itemSchema
               .filter(_.id == lift(event.parentId))
               .update(
                 _.children  -> lift(Option(event.parentChildren.toList)),
                 _.updatedAt -> lift(event.deletedAt),
                 _.updatedBy -> lift(event.deletedBy)
               )
           )
    } yield Done

  def updateName(event: HierarchyEntity.NameUpdated) =
    ctx.run(
      itemSchema
        .filter(_.id == lift(event.itemId))
        .update(
          _.name      -> lift(event.name),
          _.updatedAt -> lift(event.updatedAt),
          _.updatedBy -> lift(event.updatedBy)
        )
    )

  def updateSource(event: HierarchyEntity.SourceUpdated) =
    ctx.run(
      itemSchema
        .filter(_.id == lift(event.itemId))
        .update(
          _.source    -> lift(event.source),
          _.updatedAt -> lift(event.updatedAt),
          _.updatedBy -> lift(event.updatedBy)
        )
    )

  def updateExternalId(event: HierarchyEntity.ExternalIdUpdated) =
    for {
      _ <- ctx.run(
             itemSchema
               .filter(_.id == lift(event.itemId))
               .update(
                 _.externalId -> lift(event.externalId),
                 _.updatedAt  -> lift(event.updatedAt),
                 _.updatedBy  -> lift(event.updatedBy)
               )
           )
      _ <- event.externalId.map { externalId =>
             ctx.run(
               externalIdSchema.insert(
                 lift(
                   ExternalIdRecord(
                     externalId = externalId,
                     itemId = event.itemId
                   )
                 )
               )
             )
           }.getOrElse(Future.successful(Done))

      _ <- event.oldExternalId.map { oldExternalId =>
             ctx.run(
               externalIdSchema
                 .filter(r => r.externalId == lift(oldExternalId) && r.itemId == lift(event.itemId))
                 .delete
             )
           }.getOrElse(Future.successful(Done))
    } yield Done

  def changePositionLimit(event: HierarchyEntity.PositionLimitChanged) =
    ctx.run(
      itemSchema
        .filter(_.id == lift(event.positionId))
        .update(
          _.personLimit -> lift(Option(event.limit)),
          _.updatedAt   -> lift(event.updatedAt),
          _.updatedBy   -> lift(event.updatedBy)
        )
    )

  def assignPerson(event: HierarchyEntity.PersonAssigned) =
    for {
      _ <- ctx.run(
             itemSchema
               .filter(_.id == lift(event.positionId))
               .update(
                 _.persons   -> lift(Option(event.persons)),
                 _.updatedAt -> lift(event.updatedAt),
                 _.updatedBy -> lift(event.updatedBy)
               )
           )
      _ <- ctx.run(
             personPositionSchema.insert(
               lift(
                 PersonPosition(
                   personId = event.personId,
                   positionId = event.positionId
                 )
               )
             )
           )
    } yield Done

  def unassignPerson(event: HierarchyEntity.PersonUnassigned) =
    for {
      _ <- ctx.run(
             itemSchema
               .filter(_.id == lift(event.positionId))
               .update(
                 _.persons   -> lift(Option(event.persons)),
                 _.updatedAt -> lift(event.updatedAt),
                 _.updatedBy -> lift(event.updatedBy)
               )
           )
      _ <- ctx.run(
             personPositionSchema
               .filter(r =>
                 r.personId == lift(event.personId) &&
                   r.positionId == lift(event.positionId)
               )
               .delete
           )
    } yield Done

  def assignOrgRole(event: HierarchyEntity.OrgRoleAssigned) =
    ctx.run(
      itemSchema
        .filter(_.id == lift(event.positionId))
        .update(
          _.orgRoles  -> lift(Option(event.orgRoles)),
          _.updatedAt -> lift(event.updatedAt),
          _.updatedBy -> lift(event.updatedBy)
        )
    )

  def unassignOrgRole(event: HierarchyEntity.OrgRoleUnassigned) =
    ctx.run(
      itemSchema
        .filter(_.id == lift(event.positionId))
        .update(
          _.orgRoles  -> lift(Option(event.orgRoles)),
          _.updatedAt -> lift(event.updatedAt),
          _.updatedBy -> lift(event.updatedBy)
        )
    )

  private def updateChildren(
    unitId: CompositeOrgItemId,
    children: Seq[CompositeOrgItemId],
    updatedBy: AnnettePrincipal,
    updatedAt: OffsetDateTime
  ) =
    ctx.run(
      itemSchema
        .filter(_.id == lift(unitId))
        .update(
          _.children  -> lift(Option(children.toList)),
          _.updatedAt -> lift(updatedAt),
          _.updatedBy -> lift(updatedBy)
        )
    )

  def moveItem(event: HierarchyEntity.ItemMoved) =
    for {
      _ <- ctx.run(
             itemSchema
               .filter(_.id == lift(event.itemId))
               .update(
                 _.parentId  -> lift(event.newParentId),
                 _.updatedAt -> lift(event.updatedAt),
                 _.updatedBy -> lift(event.updatedBy)
               )
           )
      _ <- updateChildren(event.oldParentId, event.oldParentChildren, event.updatedBy, event.updatedAt)
      _ <- updateChildren(event.newParentId, event.newParentChildren, event.updatedBy, event.updatedAt)
    } yield Done

  def changeItemOrder(event: HierarchyEntity.ItemOrderChanged) =
    updateChildren(event.parentId, event.parentChildren, event.updatedBy, event.updatedAt)

  def updateRootPath(event: HierarchyEntity.RootPathUpdated) =
    ctx.run(
      itemSchema
        .filter(_.id == lift(event.orgItemId))
        .update(
          _.rootPath -> lift(event.rootPath.toList)
        )
    )

  def getOrgItemById(id: CompositeOrgItemId): Future[Option[OrgItem]] =
    ctx
      .run(itemSchema.filter(_.id == lift(id)))
      .map(_.headOption.map(_.toOrgItem))

  def getOrgItemsById(ids: Set[CompositeOrgItemId]): Future[Seq[OrgItem]] =
    ctx
      .run(itemSchema.filter(b => liftQuery(ids).contains(b.id)))
      .map(_.map(_.toOrgItem))

  def getItemIdsByExternalId(externalIds: Set[String]): Future[Map[String, CompositeOrgItemId]] =
    ctx
      .run(externalIdSchema.filter(b => liftQuery(externalIds).contains(b.externalId)))
      .map(_.map(r => r.externalId -> r.itemId).toMap)

  def getPersonPrincipals(personId: PersonId): Future[Set[AnnettePrincipal]]                    =
    for {
      positionIds         <- ctx.run(personPositionSchema.filter(_.personId == lift(personId)).map(_.positionId))
      result              <-
        ctx
          .run(itemSchema.filter(b => liftQuery(positionIds).contains(b.id)).map(r => (r.id, r.orgRoles, r.rootPath)))
          .map(_.map(r => convertToPrincipals(r._1, r._2.getOrElse(Set.empty), r._3.dropRight(1))))
      chiefUnitPrincipals <-
        ctx
          .run(chiefUnitSchema.filter(b => liftQuery(positionIds).contains(b.positionId)).map(_.unitId))
          .map(_.map(r => UnitChiefPrincipal(r)))
    } yield result.flatten.toSet ++ chiefUnitPrincipals.toSet

  def getPersonPositions(personId: PersonId): Future[Set[PersonPosition]] =
    ctx
      .run(personPositionSchema.filter(_.personId == lift(personId)))
      .map(_.toSet)

  def convertToPrincipals(positionId: String, orgRoles: Set[String], rootPath: List[String]): Seq[AnnettePrincipal] = {
    val positionPrincipal        = OrgPositionPrincipal(positionId)
    val orgRolePrincipals        = orgRoles.map(r => OrgRolePrincipal(r))
    val directUnitPrincipals     = DirectUnitPrincipal(rootPath.last)
    val descendantUnitPrincipals = rootPath.map(unitId => DescendantUnitPrincipal(unitId))
    Seq(positionPrincipal, directUnitPrincipals) ++ orgRolePrincipals ++ descendantUnitPrincipals
  }
}