package mdmoss.doobiegen

import mdmoss.doobiegen.output.File
import mdmoss.doobiegen.sql.Table
import Analysis._

class Generator(analysis: Analysis) {

  val a = analysis
  val db = analysis.model
  val target = analysis.target

  def gen: Seq[File] = {
    /* First aim - objects for each database table */

    val tableFiles = db.tables.map { t =>

      val contents =
        s"""package ${a.targetPackage(t)}
            |
            |/* Todo handle imports better */
            |import doobie.imports._
            |import java.sql.Timestamp
            |
            |object ${a.targetObject(t)} {
            |
            |  ${genPkNewType(t)}
            |
            |  ${genRowType(t)}
            |
            |  ${genInsert(t)}
            |}
         """.stripMargin


      File(
        a.targetPackage(t),
        a.targetObject(t) + ".scala",
        contents,
        isTest = false
      )
    }

    tableFiles
  }

  def genPkNewType(table: Table): String = {
    a.pkNewType(table).map { pk =>
      s"case class ${pk._2.symbol}(${pk._1.map(f => s"${f.scalaName}: ${f.scalaType.symbol}").mkString(", ")})"
    }.getOrElse("")
  }

  def genRowType(table: Table): String = {
    val row = a.rowNewType(table)
    s"case class ${row._2.symbol}(${row._1.map(f => s"${f.scalaName}: ${f.scalaType.symbol}").mkString(", ")})"
  }

  def genInsert(table: Table): String = {
    val in = a.insert(table)
    val scope = in.fn.privatePkg.map(p => s"private [$p] ").getOrElse("")

    s"""
       |${scope}def insert(${in.fn.params.map(f => s"${f.name}: ${f.`type`.symbol}").mkString(", ")}): ${in.fn.returnType} = {
       |  ${in.fn.body}
       |}
     """.stripMargin


  }

}
