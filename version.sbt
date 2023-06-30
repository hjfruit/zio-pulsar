ThisBuild / version := s"0.3.2-${git.gitHeadCommit.value.map(_.substring(0, 8)).get}-SNAPSHOT"
