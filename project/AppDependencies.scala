/*
 * Copyright 2026 HM Revenue & Customs
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

import play.sbt.PlayImport
import sbt._

object AppDependencies {

  private val bootstrapVersion = "10.5.0"
  private val hmrcMongoVersion = "2.11.0"
  private val mockitoVersion = "5.21.0"
  private val wiremockVersion = "3.8.0"
  private val scalaMockVersion = "7.5.3"
  private val jsoupVersion = "1.22.1"

  val compile = Seq(
    PlayImport.ws,
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"         % hmrcMongoVersion
  )

  val test = Seq(
    "org.scalamock" %% "scalamock" % scalaMockVersion % Test,
    "org.jsoup" % "jsoup" % jsoupVersion % Test,
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion            % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % hmrcMongoVersion            % Test,
    "org.mockito" % "mockito-core" % mockitoVersion % Test,
    "com.github.tomakehurst" % "wiremock" % wiremockVersion % Test,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.20.1",
    "org.scalatest"       %% "scalatest"              % "3.2.19" % Test
  )

  val it = Seq.empty
}
