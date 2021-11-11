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

package biz.lobachev.annette.cms.impl.blogs.post

import akka.Done
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.util.Timeout
import biz.lobachev.annette.cms.api.blogs.post._
import biz.lobachev.annette.cms.impl.blogs.post.dao.{PostDbDao, PostIndexDao}
import biz.lobachev.annette.core.model.auth.AnnettePrincipal
import biz.lobachev.annette.core.model.indexing.FindResult
import io.scalaland.chimney.dsl._
import org.slf4j.LoggerFactory

import scala.collection.immutable.Set
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class PostEntityService(
  clusterSharding: ClusterSharding,
  dbDao: PostDbDao,
  indexDao: PostIndexDao
)(implicit
  ec: ExecutionContext
) {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout = Timeout(50.seconds)

  private def refFor(id: PostId): EntityRef[PostEntity.Command] =
    clusterSharding.entityRefFor(PostEntity.typeKey, id)

  private def convertSuccess(confirmation: PostEntity.Confirmation, id: PostId, maybeId: Option[String] = None): Done =
    confirmation match {
      case PostEntity.Success                            => Done
      case PostEntity.PostAlreadyExist                   => throw PostAlreadyExist(id)
      case PostEntity.PostNotFound                       => throw PostNotFound(id)
      case PostEntity.PostPublicationDateClearNotAllowed => throw PostPublicationDateClearNotAllowed(id)
      case PostEntity.WidgetContentNotFound              => throw WidgetContentNotFound(id, maybeId.getOrElse(""))
      case _                                             => throw new RuntimeException("Match fail")
    }

  private def convertSuccessPost(confirmation: PostEntity.Confirmation, id: PostId): Post =
    confirmation match {
      case PostEntity.SuccessPost(post) => post
      case PostEntity.PostAlreadyExist  => throw PostAlreadyExist(id)
      case PostEntity.PostNotFound      => throw PostNotFound(id)
      case _                            => throw new RuntimeException("Match fail")
    }

  private def convertSuccessPostAnnotation(confirmation: PostEntity.Confirmation, id: PostId): PostAnnotation =
    confirmation match {
      case PostEntity.SuccessPostAnnotation(postAnnotation) => postAnnotation
      case PostEntity.PostAlreadyExist                      => throw PostAlreadyExist(id)
      case PostEntity.PostNotFound                          => throw PostNotFound(id)
      case _                                                => throw new RuntimeException("Match fail")
    }

  def createPost(payload: CreatePostPayload, targets: Set[AnnettePrincipal]): Future[Done] =
    refFor(payload.id)
      .ask[PostEntity.Confirmation](replyTo =>
        payload
          .into[PostEntity.CreatePost]
          .withFieldConst(_.targets, targets)
          .withFieldConst(_.replyTo, replyTo)
          .transform
      )
      .map(convertSuccess(_, payload.id))

  def updatePostFeatured(payload: UpdatePostFeaturedPayload): Future[Done] =
    refFor(payload.id)
      .ask[PostEntity.Confirmation](replyTo =>
        payload
          .into[PostEntity.UpdatePostFeatured]
          .withFieldConst(_.replyTo, replyTo)
          .transform
      )
      .map(convertSuccess(_, payload.id))

  def updatePostAuthor(payload: UpdatePostAuthorPayload): Future[Done] =
    refFor(payload.id)
      .ask[PostEntity.Confirmation](replyTo =>
        payload
          .into[PostEntity.UpdatePostAuthor]
          .withFieldConst(_.replyTo, replyTo)
          .transform
      )
      .map(convertSuccess(_, payload.id))

  def updatePostTitle(payload: UpdatePostTitlePayload): Future[Done] =
    refFor(payload.id)
      .ask[PostEntity.Confirmation](replyTo =>
        payload
          .into[PostEntity.UpdatePostTitle]
          .withFieldConst(_.replyTo, replyTo)
          .transform
      )
      .map(convertSuccess(_, payload.id))

  def updateWidgetContent(payload: UpdatePostWidgetContentPayload): Future[Done] =
    refFor(payload.id)
      .ask[PostEntity.Confirmation](replyTo =>
        payload
          .into[PostEntity.UpdateWidgetContent]
          .withFieldConst(_.replyTo, replyTo)
          .transform
      )
      .map(convertSuccess(_, payload.id, Some(payload.widgetContent.id)))

  def changeWidgetContentOrder(payload: ChangePostWidgetContentOrderPayload): Future[Done] =
    refFor(payload.id)
      .ask[PostEntity.Confirmation](replyTo =>
        payload
          .into[PostEntity.ChangeWidgetContentOrder]
          .withFieldConst(_.replyTo, replyTo)
          .transform
      )
      .map(convertSuccess(_, payload.id, Some(payload.widgetContentId)))

  def deleteWidgetContent(payload: DeletePostWidgetContentPayload): Future[Done] =
    refFor(payload.id)
      .ask[PostEntity.Confirmation](replyTo =>
        payload
          .into[PostEntity.DeleteWidgetContent]
          .withFieldConst(_.replyTo, replyTo)
          .transform
      )
      .map(convertSuccess(_, payload.id, Some(payload.widgetContentId)))

  def updatePostPublicationTimestamp(payload: UpdatePostPublicationTimestampPayload): Future[Done] =
    refFor(payload.id)
      .ask[PostEntity.Confirmation](replyTo =>
        payload
          .into[PostEntity.UpdatePostPublicationTimestamp]
          .withFieldConst(_.replyTo, replyTo)
          .transform
      )
      .map(convertSuccess(_, payload.id))

  def publishPost(payload: PublishPostPayload): Future[Done] =
    refFor(payload.id)
      .ask[PostEntity.Confirmation](replyTo =>
        payload
          .into[PostEntity.PublishPost]
          .withFieldConst(_.replyTo, replyTo)
          .transform
      )
      .map(convertSuccess(_, payload.id))

  def unpublishPost(payload: UnpublishPostPayload): Future[Done] =
    refFor(payload.id)
      .ask[PostEntity.Confirmation](replyTo =>
        payload
          .into[PostEntity.UnpublishPost]
          .withFieldConst(_.replyTo, replyTo)
          .transform
      )
      .map(convertSuccess(_, payload.id))

  def assignPostTargetPrincipal(payload: AssignPostTargetPrincipalPayload): Future[Done] =
    refFor(payload.id)
      .ask[PostEntity.Confirmation](replyTo =>
        payload
          .into[PostEntity.AssignPostTargetPrincipal]
          .withFieldConst(_.replyTo, replyTo)
          .transform
      )
      .map(convertSuccess(_, payload.id))

  def unassignPostTargetPrincipal(payload: UnassignPostTargetPrincipalPayload): Future[Done] =
    refFor(payload.id)
      .ask[PostEntity.Confirmation](replyTo =>
        payload
          .into[PostEntity.UnassignPostTargetPrincipal]
          .withFieldConst(_.replyTo, replyTo)
          .transform
      )
      .map(convertSuccess(_, payload.id))

  def deletePost(payload: DeletePostPayload): Future[Done] =
    refFor(payload.id)
      .ask[PostEntity.Confirmation](replyTo =>
        payload
          .into[PostEntity.DeletePost]
          .withFieldConst(_.replyTo, replyTo)
          .transform
      )
      .map(convertSuccess(_, payload.id))

  def getPost(id: PostId): Future[Post] =
    refFor(id)
      .ask[PostEntity.Confirmation](PostEntity.GetPost(id, _))
      .map(convertSuccessPost(_, id))

  def getPostAnnotation(id: PostId): Future[PostAnnotation] =
    refFor(id)
      .ask[PostEntity.Confirmation](PostEntity.GetPostAnnotation(id, _))
      .map(convertSuccessPostAnnotation(_, id))

  def canAccessToPost(payload: CanAccessToPostPayload): Future[Boolean] =
    dbDao.canAccessToPost(payload.id, payload.principals)

  def getPostById(id: PostId, fromReadSide: Boolean): Future[Post] =
    if (fromReadSide)
      dbDao
        .getPostById(id)
        .map(_.getOrElse(throw PostNotFound(id)))
    else
      getPost(id)

  def getPostAnnotationById(
    id: PostId,
    fromReadSide: Boolean
  ): Future[PostAnnotation] =
    if (fromReadSide)
      dbDao
        .getPostAnnotationById(id)
        .map(_.getOrElse(throw PostNotFound(id)))
    else
      getPostAnnotation(id)

  def getPostsById(ids: Set[PostId], fromReadSide: Boolean): Future[Seq[Post]] =
    if (fromReadSide)
      dbDao.getPostsById(ids)
    else
      Future
        .traverse(ids) { id =>
          refFor(id)
            .ask[PostEntity.Confirmation](PostEntity.GetPost(id, _))
            .map {
              case PostEntity.SuccessPost(post) => Some(post)
              case _                            => None
            }
        }
        .map(_.flatten.toSeq)

  def getPostAnnotationsById(ids: Set[PostId], fromReadSide: Boolean): Future[Seq[PostAnnotation]] =
    if (fromReadSide)
      dbDao.getPostAnnotationsById(ids)
    else
      Future
        .traverse(ids) { id =>
          refFor(id)
            .ask[PostEntity.Confirmation](PostEntity.GetPostAnnotation(id, _))
            .map {
              case PostEntity.SuccessPostAnnotation(post) => Some(post)
              case _                                      => None
            }
        }
        .map(_.flatten.toSeq)

  def getPostViews(payload: GetPostViewsPayload): Future[Seq[PostView]] =
    dbDao.getPostViewsById(payload)

  def findPosts(query: PostFindQuery): Future[FindResult] = indexDao.findPosts(query)

  def viewPost(payload: ViewPostPayload): Future[Done] = dbDao.viewPost(payload.id, payload.updatedBy)

  def likePost(payload: LikePostPayload): Future[Done] = dbDao.likePost(payload.id, payload.updatedBy)

  def unlikePost(payload: UnlikePostPayload): Future[Done] = dbDao.unlikePost(payload.id, payload.updatedBy)

  def getPostMetricById(payload: GetPostMetricPayload): Future[PostMetric] =
    dbDao.getPostMetricById(payload.id, payload.principal)

  def getPostMetricsById(payload: GetPostMetricsPayload): Future[Seq[PostMetric]] =
    dbDao.getPostMetricsById(payload.ids, payload.principal)

}
