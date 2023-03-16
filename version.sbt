ThisBuild / version := s"0.3.0-${git.gitHeadCommit.value.map(_.substring(0, 8)).get}-SNAPSHOT"
