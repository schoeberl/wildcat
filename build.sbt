
scalaVersion := "2.11.7"

scalaSource in Compile := baseDirectory.value / "src"

scalaSource in Compile := baseDirectory.value / "simsrc"

libraryDependencies += "edu.berkeley.cs" %% "chisel" % "latest.release"
