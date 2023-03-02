import sbt.Keys._
import sbt._

object Publishing {
  lazy val publishing = Seq(
    publishTo        := (if (version.value.endsWith("SNAPSHOT"))
                    Some(
                      "hjgpscm-public".at(
                        "https://jfrog-artifactory.hjgpscm.com/artifactory/public;build.timestamp=" + new java.util.Date().getTime
                      )
                    )
                  else Some("hjgpscm-public".at("https://jfrog-artifactory.hjgpscm.com/artifactory/public"))),
    credentials += Credentials(Path.userHome / ".sbt" / ".credentials_hjfruit"),
    homepage         := Some(url("https://github.com/jczuchnowski/zio-pulsar/")),
    licenses         := List("BSD 2-Clause" -> url("https://opensource.org/licenses/BSD-2-Clause")),
    organization     := "fc.xuanwu.star",
    organizationName := "FC Xuanwu",
    developers       := List(
      Developer(
        "jczuchnowski",
        "Jakub Czuchnowski",
        "jakub.czuchnowski@gmail.com",
        url("https://github.com/jczuchnowski")
      ),
      Developer(
        id = "yangbajing",
        name = "Yang Jing",
        email = "yang.xunjing@qq.com",
        url = url("https://git.hjgpscm.com/yangjing")
      ),
      Developer(
        id = "wanghaonan",
        name = "Wang Hao Nan",
        email = "wanghaonan@hjfruit.com",
        url = url("https://git.hjgpscm.com/wanghaonan")
      ),
      Developer(
        id = "liguobin",
        name = "Li Guo Bin",
        email = "liguobin@hjfruit.com",
        url = url("https://git.hjgpscm.com/liguobin")
      )
    ),
    scmInfo          := Some(
      ScmInfo(
        url("https://git.hjgpscm.com/xuanwu/xuanwu-zio-pulsar.git"),
        "scm:git:git://git.hjgpscm.com/xuanwu/xuanwu-zio-pulsar.git"
      )
    )
  )
}
