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

import akka.Done
import biz.lobachev.annette.cms.api.blogs.blog._
import biz.lobachev.annette.cms.api.blogs.post._
import biz.lobachev.annette.cms.api.common.Updated
import biz.lobachev.annette.cms.api.pages.space._
import biz.lobachev.annette.cms.api.pages.page._
import biz.lobachev.annette.cms.api.files.{FileDescriptor, RemoveFilePayload, RemoveFilesPayload, StoreFilePayload}
import biz.lobachev.annette.core.model.category._
import biz.lobachev.annette.core.model.indexing.FindResult

import scala.concurrent.Future

trait CmsService {

  // ************************** CMS Files **************************

  def storeFile(payload: StoreFilePayload): Future[Done]
  def removeFile(payload: RemoveFilePayload): Future[Done]
  def removeFiles(payload: RemoveFilesPayload): Future[Done]
  def getFiles(objectId: String): Future[Seq[FileDescriptor]]

  // ************************** CMS Blogs **************************

  def createBlogCategory(payload: CreateCategoryPayload): Future[Done]
  def updateBlogCategory(payload: UpdateCategoryPayload): Future[Done]
  def deleteBlogCategory(payload: DeleteCategoryPayload): Future[Done]
  def getBlogCategoryById(id: CategoryId, fromReadSide: Boolean = true): Future[Category]
  def getBlogCategoriesById(ids: Set[CategoryId], fromReadSide: Boolean = true): Future[Seq[Category]]
  def findBlogCategories(payload: CategoryFindQuery): Future[FindResult]

  def createBlog(payload: CreateBlogPayload): Future[Done]
  def updateBlogName(payload: UpdateBlogNamePayload): Future[Done]
  def updateBlogDescription(payload: UpdateBlogDescriptionPayload): Future[Done]
  def updateBlogCategoryId(payload: UpdateBlogCategoryPayload): Future[Done]
  def assignBlogTargetPrincipal(payload: AssignBlogTargetPrincipalPayload): Future[Done]
  def unassignBlogTargetPrincipal(payload: UnassignBlogTargetPrincipalPayload): Future[Done]
  def activateBlog(payload: ActivateBlogPayload): Future[Done]
  def deactivateBlog(payload: DeactivateBlogPayload): Future[Done]
  def deleteBlog(payload: DeleteBlogPayload): Future[Done]
  def getBlogById(id: BlogId, fromReadSide: Boolean = true): Future[Blog]
  def getBlogsById(ids: Set[BlogId], fromReadSide: Boolean = true): Future[Seq[Blog]]
  def getBlogViews(payload: GetBlogViewsPayload): Future[Seq[BlogView]]
  def canAccessToBlog(payload: CanAccessToBlogPayload): Future[Boolean]
  def findBlogs(payload: BlogFindQuery): Future[FindResult]

  def createPost(payload: CreatePostPayload): Future[Post]
  def updatePostFeatured(payload: UpdatePostFeaturedPayload): Future[Updated]
  def updatePostAuthor(payload: UpdatePostAuthorPayload): Future[Updated]
  def updatePostTitle(payload: UpdatePostTitlePayload): Future[Updated]
  def updatePostWidgetContent(payload: UpdatePostWidgetContentPayload): Future[Updated]
  def changePostWidgetContentOrder(payload: ChangePostWidgetContentOrderPayload): Future[Updated]
  def deletePostWidgetContent(payload: DeletePostWidgetContentPayload): Future[Updated]
  def updatePostPublicationTimestamp(payload: UpdatePostPublicationTimestampPayload): Future[Updated]
  def publishPost(payload: PublishPostPayload): Future[Updated]
  def unpublishPost(payload: UnpublishPostPayload): Future[Updated]
  def assignPostTargetPrincipal(payload: AssignPostTargetPrincipalPayload): Future[Updated]
  def unassignPostTargetPrincipal(payload: UnassignPostTargetPrincipalPayload): Future[Updated]
  def deletePost(payload: DeletePostPayload): Future[Updated]
  def getPostById(
    id: PostId,
    fromReadSide: Boolean = true,
    withIntro: Option[Boolean],
    withContent: Option[Boolean],
    withTargets: Option[Boolean]
  ): Future[Post]
  def getPostsById(
    ids: Set[PostId],
    fromReadSide: Boolean = true,
    withIntro: Option[Boolean],
    withContent: Option[Boolean],
    withTargets: Option[Boolean]
  ): Future[Seq[Post]]
  def getPostViews(payload: GetPostViewsPayload): Future[Seq[Post]]
  def canAccessToPost(payload: CanAccessToPostPayload): Future[Boolean]
  def findPosts(query: PostFindQuery): Future[FindResult]

  def viewPost(payload: ViewPostPayload): Future[Done]
  def likePost(payload: LikePostPayload): Future[Done]
  def unlikePost(payload: UnlikePostPayload): Future[Done]
  def getPostMetricById(payload: GetPostMetricPayload): Future[PostMetric]
  def getPostMetricsById(payload: GetPostMetricsPayload): Future[Seq[PostMetric]]

  // ************************** CMS Pages **************************

  def createSpaceCategory(payload: CreateCategoryPayload): Future[Done]
  def updateSpaceCategory(payload: UpdateCategoryPayload): Future[Done]
  def deleteSpaceCategory(payload: DeleteCategoryPayload): Future[Done]
  def getSpaceCategoryById(id: CategoryId, fromReadSide: Boolean = true): Future[Category]
  def getSpaceCategoriesById(ids: Set[CategoryId], fromReadSide: Boolean = true): Future[Seq[Category]]
  def findSpaceCategories(payload: CategoryFindQuery): Future[FindResult]

  def createSpace(payload: CreateSpacePayload): Future[Done]
  def updateSpaceName(payload: UpdateSpaceNamePayload): Future[Done]
  def updateSpaceDescription(payload: UpdateSpaceDescriptionPayload): Future[Done]
  def updateSpaceCategoryId(payload: UpdateSpaceCategoryPayload): Future[Done]
  def assignSpaceTargetPrincipal(payload: AssignSpaceTargetPrincipalPayload): Future[Done]
  def unassignSpaceTargetPrincipal(payload: UnassignSpaceTargetPrincipalPayload): Future[Done]
  def activateSpace(payload: ActivateSpacePayload): Future[Done]
  def deactivateSpace(payload: DeactivateSpacePayload): Future[Done]
  def deleteSpace(payload: DeleteSpacePayload): Future[Done]
  def getSpaceById(id: SpaceId, fromReadSide: Boolean = true): Future[Space]
  def getSpacesById(ids: Set[SpaceId], fromReadSide: Boolean = true): Future[Seq[Space]]
  def getSpaceViews(payload: GetSpaceViewsPayload): Future[Seq[SpaceView]]
  def canAccessToSpace(payload: CanAccessToSpacePayload): Future[Boolean]
  def findSpaces(payload: SpaceFindQuery): Future[FindResult]

  def createPage(payload: CreatePagePayload): Future[Page]
  def updatePageAuthor(payload: UpdatePageAuthorPayload): Future[Updated]
  def updatePageTitle(payload: UpdatePageTitlePayload): Future[Updated]
  def updatePageWidgetContent(payload: UpdatePageWidgetContentPayload): Future[Updated]
  def changePageWidgetContentOrder(payload: ChangePageWidgetContentOrderPayload): Future[Updated]
  def deletePageWidgetContent(payload: DeletePageWidgetContentPayload): Future[Updated]
  def updatePagePublicationTimestamp(payload: UpdatePagePublicationTimestampPayload): Future[Updated]
  def publishPage(payload: PublishPagePayload): Future[Updated]
  def unpublishPage(payload: UnpublishPagePayload): Future[Updated]
  def assignPageTargetPrincipal(payload: AssignPageTargetPrincipalPayload): Future[Updated]
  def unassignPageTargetPrincipal(payload: UnassignPageTargetPrincipalPayload): Future[Updated]
  def deletePage(payload: DeletePagePayload): Future[Updated]
  def getPageById(
    id: PageId,
    fromReadSide: Boolean = true,
    withContent: Option[Boolean],
    withTargets: Option[Boolean]
  ): Future[Page]
  def getPagesById(
    ids: Set[PageId],
    fromReadSide: Boolean = true,
    withContent: Option[Boolean],
    withTargets: Option[Boolean]
  ): Future[Seq[Page]]
  def getPageViews(payload: GetPageViewsPayload): Future[Seq[Page]]
  def canAccessToPage(payload: CanAccessToPagePayload): Future[Boolean]
  def findPages(query: PageFindQuery): Future[FindResult]

  def viewPage(payload: ViewPagePayload): Future[Done]
  def likePage(payload: LikePagePayload): Future[Done]
  def unlikePage(payload: UnlikePagePayload): Future[Done]
  def getPageMetricById(payload: GetPageMetricPayload): Future[PageMetric]
  def getPageMetricsById(payload: GetPageMetricsPayload): Future[Seq[PageMetric]]

}
