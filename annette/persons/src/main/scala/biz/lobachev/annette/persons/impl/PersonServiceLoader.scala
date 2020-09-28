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

package biz.lobachev.annette.persons.impl

import akka.cluster.sharding.typed.scaladsl.Entity
import biz.lobachev.annette.attributes.api.AttributeService
import biz.lobachev.annette.core.discovery.AnnetteDiscoveryComponents
import biz.lobachev.annette.core.elastic.ElasticModule
import biz.lobachev.annette.persons.api.PersonServiceApi
import biz.lobachev.annette.persons.impl.person._
import biz.lobachev.annette.persons.impl.person.dao.{PersonCassandraDbDao, PersonElasticIndexDao}
import biz.lobachev.annette.persons.impl.person.model.PersonSerializerRegistry
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaClientComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.LoggerConfigurator
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable

class PersonServiceLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new PersonServiceApplication(context) with AnnetteDiscoveryComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication = {
    // workaround for custom logback.xml
    val environment = context.playContext.environment
    LoggerConfigurator(environment.classLoader).foreach {
      _.configure(environment)
    }
    new PersonServiceApplication(context) with LagomDevModeComponents
  }

  override def describeService = Some(readDescriptor[PersonServiceApi])
}

abstract class PersonServiceApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaClientComponents
    with AhcWSComponents {

  lazy val jsonSerializerRegistry = PersonRepositorySerializerRegistry

  val elasticModule = new ElasticModule(config)
  import elasticModule._

  override lazy val lagomServer = serverFor[PersonServiceApi](wire[PersonServiceApiImpl])
  lazy val personElastic        = wire[PersonElasticIndexDao]
  lazy val personService        = wire[PersonEntityService]
  lazy val personRepository     = wire[PersonCassandraDbDao]
  readSide.register(wire[PersonDbEventProcessor])
  readSide.register(wire[PersonIndexEventProcessor])

  clusterSharding.init(
    Entity(PersonEntity.typeKey) { entityContext =>
      PersonEntity(entityContext)
    }
  )

  lazy val attributeService = serviceClient.implement[AttributeService]
  wire[AttributeServiceSubscriber]

}

object PersonRepositorySerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = PersonSerializerRegistry.serializers
}