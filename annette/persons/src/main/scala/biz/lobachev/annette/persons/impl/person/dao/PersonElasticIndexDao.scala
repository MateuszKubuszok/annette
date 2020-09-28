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

package biz.lobachev.annette.persons.impl.person.dao

import biz.lobachev.annette.attributes.api.query.AttributeElastic
import biz.lobachev.annette.core.elastic.{AbstractElasticIndexDao, ElasticSettings, FindResult}
import biz.lobachev.annette.persons.api.person._
import biz.lobachev.annette.persons.impl.person.PersonEntity
import biz.lobachev.annette.persons.impl.person.PersonEntity.PersonDeleted
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.analysis.{Analysis, CustomAnalyzer, EdgeNGramTokenizer}
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.indexes.CreateIndexRequest
import com.sksamuel.elastic4s.requests.searches.sort.{FieldSort, SortOrder}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

class PersonElasticIndexDao(elasticSettings: ElasticSettings, elasticClient: ElasticClient)(implicit
  override val ec: ExecutionContext
) extends AbstractElasticIndexDao(elasticSettings, elasticClient)
    with PersonIndexDao
    with AttributeElastic {

  override val log = LoggerFactory.getLogger(this.getClass)

  override def indexSuffix = "persons"

  // *************************** Index API ***************************

  def createIndexRequest: CreateIndexRequest =
    createIndex(indexName)
      .mapping(
        properties(
          keywordField("id"),
          textField("lastname").fielddata(true).analyzer("name_analyzer").searchAnalyzer("name_search"),
          textField("firstname").fielddata(true).analyzer("name_analyzer").searchAnalyzer("name_search"),
          textField("middlename").fielddata(true).analyzer("name_analyzer").searchAnalyzer("name_search"),
          keywordField("phone"),
          keywordField("email"), //.analyzer("ngram_name_analyzer").searchAnalyzer("standard"),
          dateField("updatedAt")
        )
      )
      .analysis(
        Analysis(
          analyzers = List(
            CustomAnalyzer(
              name = "name_analyzer",
              tokenizer = "name_tokenizer", //StandardTokenizer,
              charFilters = Nil,
              tokenFilters = "lowercase" :: Nil
            ),
            CustomAnalyzer(
              name = "name_search",
              tokenizer = "lowercase",
              charFilters = Nil,
              tokenFilters = Nil
            )
          ),
          tokenizers = List(
            EdgeNGramTokenizer(
              "name_tokenizer",
              minGram = 2,
              maxGram = 10
            )
          )
        )
      )

  def createPerson(event: PersonEntity.PersonCreated): Future[Unit] =
    elasticClient.execute {
      indexInto(indexName)
        .id(event.id)
        .fields(
          "id"         -> event.id,
          "lastname"   -> event.lastname,
          "firstname"  -> event.firstname,
          "middlename" -> event.middlename,
          "phone"      -> event.phone,
          "email"      -> event.email,
          "updatedAt"  -> event.createdAt
        )
        .refresh(RefreshPolicy.Immediate)
    }.map(processResponse("createPerson", event.id)(_))

  def updatePerson(event: PersonEntity.PersonUpdated): Future[Unit] =
    elasticClient.execute {
      updateById(indexName, event.id)
        .doc(
          "id"         -> event.id,
          "lastname"   -> event.lastname,
          "firstname"  -> event.firstname,
          "middlename" -> event.middlename,
          "phone"      -> event.phone,
          "email"      -> event.email,
          "updatedAt"  -> event.updatedAt
        )
        .refresh(RefreshPolicy.Immediate)
    }.map(processResponse("updatePerson", event.id)(_))

  def deletePerson(event: PersonDeleted): Future[Unit] =
    elasticClient.execute {
      deleteById(indexName, event.id)
    }
      .map(processResponse("deletePerson", event.id)(_))

  // *************************** Search API ***************************

  def findPerson(query: PersonFindQuery): Future[FindResult] = {

    val fieldQuery             = Seq(
      query.firstname.map(matchQuery("firstname", _)),
      query.lastname.map(matchQuery("lastname", _)),
      query.middlename.map(matchQuery("middlename", _)),
      query.phone.map(matchQuery("phone", _)),
      query.email.map(matchQuery("email", _))
    ).flatten
    val filterQuery            = buildFilterQuery(
      query.filter,
      Seq("lastname" -> 3.0, "firstname" -> 2.0, "middlename" -> 1.0, "email" -> 3.0, "phone" -> 1.0)
    )
    val sortBy: Seq[FieldSort] = buildSortBy(query)
    val attributeQuery         = buildAttributeQuery(query.attributes)

    val searchRequest = search(indexName)
      .bool(must(filterQuery ++ fieldQuery ++ fieldQuery ++ attributeQuery))
      .from(query.offset)
      .size(query.size)
      .sortBy(sortBy)
      .sourceInclude("updatedAt")
      .trackTotalHits(true)

    //println(elasticClient.show(searchRequest))

    findEntity(searchRequest)

  }

  private def buildSortBy(query: PersonFindQuery) =
    query.sortBy.map { sortBy =>
      val sortOrder =
        if (sortBy.ascending) SortOrder.Asc
        else SortOrder.Desc
      sortBy.field match {
        case PersonSortField.Lastname  =>
          Seq(
            FieldSort("lastname.keyword", order = sortOrder),
            FieldSort("firstname.keyword", order = sortOrder),
            FieldSort("middlename.keyword", order = sortOrder)
          )
        case PersonSortField.Firstname =>
          Seq(
            FieldSort("firstname.keyword", order = sortOrder),
            FieldSort("lastname.keyword", order = sortOrder),
            FieldSort("middlename.keyword", order = sortOrder)
          )
        case PersonSortField.Email     =>
          Seq(
            FieldSort("email.keyword", order = sortOrder)
          )
        case PersonSortField.Phone     =>
          Seq(
            FieldSort("phone.keyword", order = sortOrder)
          )
      }
    }.getOrElse(Seq.empty)
}
