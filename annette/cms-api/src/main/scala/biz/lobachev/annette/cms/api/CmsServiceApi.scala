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

package biz.lobachev.annette.cms.api

import akka.{Done, NotUsed}
import biz.lobachev.annette.cms.api.space._
import biz.lobachev.annette.cms.api.post._
import biz.lobachev.annette.core.exception.AnnetteTransportExceptionSerializer
import biz.lobachev.annette.core.model.category._
import biz.lobachev.annette.core.model.elastic.FindResult
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

import scala.collection.immutable.Map

trait CmsServiceApi extends Service {

  def createCategory: ServiceCall[CreateCategoryPayload, Done]
  def updateCategory: ServiceCall[UpdateCategoryPayload, Done]
  def deleteCategory: ServiceCall[DeleteCategoryPayload, Done]
  def getCategoryById(id: CategoryId, fromReadSide: Boolean = true): ServiceCall[NotUsed, Category]
  def getCategoriesById(fromReadSide: Boolean = true): ServiceCall[Set[CategoryId], Seq[Category]]
  def findCategories: ServiceCall[CategoryFindQuery, FindResult]

  def createSpace: ServiceCall[CreateSpacePayload, Done]
  def updateSpaceName: ServiceCall[UpdateSpaceNamePayload, Done]
  def updateSpaceDescription: ServiceCall[UpdateSpaceDescriptionPayload, Done]
  def updateSpaceCategory: ServiceCall[UpdateSpaceCategoryPayload, Done]
  def assignSpaceTargetPrincipal: ServiceCall[AssignSpaceTargetPrincipalPayload, Done]
  def unassignSpaceTargetPrincipal: ServiceCall[UnassignSpaceTargetPrincipalPayload, Done]
  def activateSpace: ServiceCall[ActivateSpacePayload, Done]
  def deactivateSpace: ServiceCall[DeactivateSpacePayload, Done]
  def deleteSpace: ServiceCall[DeleteSpacePayload, Done]
  def getSpaceById(id: SpaceId, fromReadSide: Boolean = true): ServiceCall[NotUsed, Space]
  def getSpacesById(fromReadSide: Boolean = true): ServiceCall[Set[SpaceId], Map[SpaceId, Space]]
  def getSpaceViews: ServiceCall[GetSpaceViewsPayload, Map[SpaceId, SpaceView]]
  def canAccessToSpace: ServiceCall[CanAccessToSpacePayload, Boolean]
  def findSpaces: ServiceCall[SpaceFindQuery, FindResult]

  def createPost: ServiceCall[CreatePostPayload, Done]
  def updatePostFeatured: ServiceCall[UpdatePostFeaturedPayload, Done]
  def updatePostAuthor: ServiceCall[UpdatePostAuthorPayload, Done]
  def updatePostTitle: ServiceCall[UpdatePostTitlePayload, Done]
  def updatePostIntro: ServiceCall[UpdatePostIntroPayload, Done]
  def updatePostContent: ServiceCall[UpdatePostContentPayload, Done]
  def updatePostPublicationTimestamp: ServiceCall[UpdatePostPublicationTimestampPayload, Done]
  def publishPost: ServiceCall[PublishPostPayload, Done]
  def unpublishPost: ServiceCall[UnpublishPostPayload, Done]
  def assignPostTargetPrincipal: ServiceCall[AssignPostTargetPrincipalPayload, Done]
  def unassignPostTargetPrincipal: ServiceCall[UnassignPostTargetPrincipalPayload, Done]
  def deletePost: ServiceCall[DeletePostPayload, Done]
  def getPostById(id: PostId, fromReadSide: Boolean = true): ServiceCall[NotUsed, Post]
  def getPostAnnotationById(id: PostId, fromReadSide: Boolean = true): ServiceCall[NotUsed, PostAnnotation]
  def getPostsById(fromReadSide: Boolean = true): ServiceCall[Set[PostId], Map[PostId, Post]]
  def getPostAnnotationsById(fromReadSide: Boolean = true): ServiceCall[Set[PostId], Map[PostId, PostAnnotation]]
  def getPostViews: ServiceCall[GetPostViewsPayload, Map[PostId, PostView]]
  def canAccessToPost: ServiceCall[CanAccessToPostPayload, Boolean]
  def findPosts: ServiceCall[PostFindQuery, FindResult]
  def addPostMedia: ServiceCall[AddPostMediaPayload, Done]
  def removePostMedia: ServiceCall[RemovePostMediaPayload, Done]
  def addPostDoc: ServiceCall[AddPostDocPayload, Done]
  def updatePostDocName: ServiceCall[UpdatePostDocNamePayload, Done]
  def removePostDoc: ServiceCall[RemovePostDocPayload, Done]

  def viewPost: ServiceCall[ViewPostPayload, Done]
  def likePost: ServiceCall[LikePostPayload, Done]
  def unlikePost: ServiceCall[UnlikePostPayload, Done]
  def getPostMetricById: ServiceCall[GetPostMetricPayload, PostMetric]
  def getPostMetricsById: ServiceCall[GetPostMetricsPayload, Map[PostId, PostMetric]]

  def movePost: ServiceCall[MovePostPayload, Done]
  def getWikiHierarchyById(id: SpaceId, fromReadSide: Boolean = true): ServiceCall[NotUsed, WikiHierarchy]

  final override def descriptor = {
    import Service._
    named("cms")
      .withCalls(
        pathCall("/api/cms/v1/createCategory", createCategory),
        pathCall("/api/cms/v1/updateCategory", updateCategory),
        pathCall("/api/cms/v1/deleteCategory", deleteCategory),
        pathCall("/api/cms/v1/getCategoryById/:id/:fromReadSide", getCategoryById _),
        pathCall("/api/cms/v1/getCategoriesById/:fromReadSide", getCategoriesById _),
        pathCall("/api/cms/v1/findCategories", findCategories),
        pathCall("/api/cms/v1/createSpace", createSpace),
        pathCall("/api/cms/v1/updateSpaceName", updateSpaceName),
        pathCall("/api/cms/v1/updateSpaceDescription", updateSpaceDescription),
        pathCall("/api/cms/v1/updateSpaceCategory", updateSpaceCategory),
        pathCall("/api/cms/v1/assignSpaceTargetPrincipal", assignSpaceTargetPrincipal),
        pathCall("/api/cms/v1/unassignSpaceTargetPrincipal", unassignSpaceTargetPrincipal),
        pathCall("/api/cms/v1/activateSpace", activateSpace),
        pathCall("/api/cms/v1/deactivateSpace", deactivateSpace),
        pathCall("/api/cms/v1/deleteSpace", deleteSpace),
        pathCall("/api/cms/v1/getSpaceById/:id/:fromReadSide", getSpaceById _),
        pathCall("/api/cms/v1/getSpacesById/:fromReadSide", getSpacesById _),
        pathCall("/api/cms/v1/getSpaceViews", getSpaceViews),
        pathCall("/api/cms/v1/canAccessToSpace", canAccessToSpace),
        pathCall("/api/cms/v1/findSpaces", findSpaces),
        pathCall("/api/cms/v1/createPost", createPost),
        pathCall("/api/cms/v1/updatePostFeatured", updatePostFeatured),
        pathCall("/api/cms/v1/updatePostAuthor", updatePostAuthor),
        pathCall("/api/cms/v1/updatePostTitle", updatePostTitle),
        pathCall("/api/cms/v1/updatePostIntro", updatePostIntro),
        pathCall("/api/cms/v1/updatePostContent", updatePostContent),
        pathCall("/api/cms/v1/updatePostPublicationTimestamp", updatePostPublicationTimestamp),
        pathCall("/api/cms/v1/publishPost", publishPost),
        pathCall("/api/cms/v1/unpublishPost", unpublishPost),
        pathCall("/api/cms/v1/assignPostTargetPrincipal", assignPostTargetPrincipal),
        pathCall("/api/cms/v1/unassignPostTargetPrincipal", unassignPostTargetPrincipal),
        pathCall("/api/cms/v1/deletePost", deletePost),
        pathCall("/api/cms/v1/getPostById/:id/:fromReadSide", getPostById _),
        pathCall("/api/cms/v1/getPostAnnotationById/:id/:fromReadSide", getPostAnnotationById _),
        pathCall("/api/cms/v1/getPostsById/:fromReadSide", getPostsById _),
        pathCall("/api/cms/v1/getPostAnnotationsById/:fromReadSide", getPostAnnotationsById _),
        pathCall("/api/cms/v1/getPostViews", getPostViews),
        pathCall("/api/cms/v1/canAccessToPost", canAccessToPost),
        pathCall("/api/cms/v1/findPosts", findPosts),
        pathCall("/api/cms/v1/addPostMedia", addPostMedia),
        pathCall("/api/cms/v1/removePostMedia", removePostMedia),
        pathCall("/api/cms/v1/addPostDoc", addPostDoc),
        pathCall("/api/cms/v1/updatePostDocName", updatePostDocName),
        pathCall("/api/cms/v1/removePostDoc", removePostDoc),
        pathCall("/api/cms/v1/viewPost", viewPost),
        pathCall("/api/cms/v1/likePost", likePost),
        pathCall("/api/cms/v1/unlikePost", unlikePost),
        pathCall("/api/cms/v1/getPostMetricById", getPostMetricById),
        pathCall("/api/cms/v1/getPostMetricsById", getPostMetricsById),
        pathCall("/api/cms/v1/movePost", movePost),
        pathCall("/api/cms/v1/getWikiHierarchyById/:id/:fromReadSide", getWikiHierarchyById _)
      )
      .withExceptionSerializer(new AnnetteTransportExceptionSerializer())
      .withAutoAcl(true)
  }
}
