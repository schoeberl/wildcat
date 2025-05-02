
scalacOptions ++= Seq(
  "-feature",
  "-language:reflectiveCalls",
)

Compile / unmanagedSourceDirectories += baseDirectory.value / "soc-comm/src"


// Chisel 3.5
/*
scalaVersion := "2.13.10"
addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % "3.5.6" cross CrossVersion.full)
libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.5.6"
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "0.5.6"
libraryDependencies += "edu.berkeley.cs" % "ip-contributions" % "0.5.4"
*/

scalaVersion := "2.13.14"
val chiselVersion = "3.6.1"
addCompilerPlugin("edu.berkeley.cs" %% "chisel3-plugin" % chiselVersion cross CrossVersion.full)
libraryDependencies += "edu.berkeley.cs" %% "chisel3" % chiselVersion
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "0.6.2"
libraryDependencies += "edu.berkeley.cs" % "ip-contributions" % "0.6.1"


/*
scalaVersion := "2.13.14"
val chiselVersion = "5.3.0"
addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full)
libraryDependencies += "org.chipsalliance" %% "chisel" % chiselVersion
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "5.0.2"
libraryDependencies += "net.fornwall" % "jelf" % "0.9.0"
*/


/*
scalaVersion := "2.13.14"
val chiselVersion = "6.5.0"
addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full)
libraryDependencies += "org.chipsalliance" %% "chisel" % chiselVersion
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "6.0.0"
libraryDependencies += "net.fornwall" % "jelf" % "0.9.0"
*/




libraryDependencies += "net.fornwall" % "jelf" % "0.9.0"
libraryDependencies += "com.fazecast" % "jSerialComm" % "[2.0.0,3.0.0)"


// because I am reusing a.out -- should go with a change in compilation
Test / parallelExecution := false

// library name
name := "wildcat"
