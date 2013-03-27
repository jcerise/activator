package snap

import java.io.File

class ShimWriter(val name: String, version: String) {

  private val sbtContents = """
// Note: This file is autogenerated by Builder.  Please do not modify!
addSbtPlugin("com.typesafe.builder" % "sbt-shim-""" + name + """" % """" + version + "\")\n"

  private val SHIM_FILE_NAME = "builder-" + name + "-shim.sbt"

  private lazy val pluginSbtFile = {
    val tmp = java.io.File.createTempFile(name, "sbt-shim")
    IO.write(tmp, sbtContents)
    tmp.deleteOnExit()
    tmp
  }

  private lazy val sbtFileSha = Hashing.sha512(pluginSbtFile)

  private def makeTarget(basedir: File): File =
    new File(new File(basedir, "project"), SHIM_FILE_NAME)

  // update the shim file ONLY if it already exists. Returns true if it makes a change.
  def updateIfExists(basedir: File): Boolean = {
    val target = makeTarget(basedir)
    if (target.exists && Hashing.sha512(target) != sbtFileSha) {
      IO.copyFile(pluginSbtFile, target)
      true
    } else {
      false
    }
  }

  // update the shim file EVEN IF it doesn't exist. Returns true if it makes a change.
  def ensureExists(basedir: File): Boolean = {
    val target = makeTarget(basedir)
    if (target.exists && Hashing.sha512(target) == sbtFileSha) {
      false
    } else {
      IO.copyFile(pluginSbtFile, target)
      true
    }
  }
}

object ShimWriter {
  val knownShims = Set("eclipse", "play")
}
