package org.broadinstitute.sting.queue.function.scattergather

import org.broadinstitute.sting.commandline.ArgumentSource
import java.io.File
import org.broadinstitute.sting.queue.QException
import org.broadinstitute.sting.queue.function.{QFunction, CommandLineFunction}

/**
 * Shadow clones another command line function.
 */
class CloneFunction extends CommandLineFunction {
  var originalFunction: ScatterGatherableFunction = _
  var index: Int = _

  private var overriddenFields = Map.empty[ArgumentSource, Any]
  private var withScatterPartCount = 0

  private def withScatterPart[A](f: () => A): A = {
    var originalValues = Map.empty[ArgumentSource, Any]
    withScatterPartCount += 1
    if (withScatterPartCount == 1) {
      overriddenFields.foreach{
        case (field, overrideValue) => {
          originalValues += field -> originalFunction.getFieldValue(field)
          originalFunction.setFieldValue(field, overrideValue)
        }
      }
    }
    try {
      f()
    } finally {
      if (withScatterPartCount == 1) {
        originalValues.foreach{
          case (name, value) =>
            originalFunction.setFieldValue(name, value)
        }
      }
      withScatterPartCount -= 1
    }
  }

  override def dotString = withScatterPart(() => originalFunction.dotString)
  override def description = withScatterPart(() => originalFunction.description)
  override protected def functionFieldClass = originalFunction.getClass
  override def useStatusOutput(file: File) =
    !isLogFile(file) && originalFunction.useStatusOutput(file)

  def commandLine = withScatterPart(() => originalFunction.commandLine)

  def getFieldValue(field: String): AnyRef = {
    val source = QFunction.findField(originalFunction.getClass, field)
    getFieldValue(source)
  }

  override def getFieldValue(source: ArgumentSource): AnyRef = {
    source.field.getName match {
      case "jobOutputFile" => jobOutputFile
      case "jobErrorFile" => jobErrorFile
      case _ => overriddenFields.get(source) match {
        case Some(value) => value.asInstanceOf[AnyRef]
        case None => {
          val value = originalFunction.getFieldValue(source)
          overriddenFields += source -> value
          value
        }
      }
    }
  }

  def setFieldValue(field: String, value: Any): Unit = {
    val source = QFunction.findField(originalFunction.getClass, field)
    setFieldValue(source, value)
  }

  override def setFieldValue(source: ArgumentSource, value: Any): Unit = {
    source.field.getName match {
      case "jobOutputFile" => jobOutputFile = value.asInstanceOf[File]
      case "jobErrorFile" => jobErrorFile = value.asInstanceOf[File]
      case _ => overriddenFields += source -> value
    }
  }
}
