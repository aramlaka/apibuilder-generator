package controllers

import com.bryzek.apidoc.generator.v0.models.json._
import com.bryzek.apidoc.generator.v0.models.{Invocation, InvocationForm, Generator}
import lib.{ServiceFileNames, Validation}
import play.api.libs.json._
import play.api.mvc._

object Invocations extends Controller {

  def postByKey(key: String) = Action(parse.json(maxLength = 1024 * 1024)) { request: Request[JsValue] =>
    Generators.findGenerator(key) match {
      case Some((_, generator)) =>
        request.body.validate[InvocationForm] match {
          case e: JsError => Conflict(Json.toJson(Validation.invalidJson(e)))
          case s: JsSuccess[InvocationForm] => {
            val form = s.get
            generator.invoke(form) match {
              case Left(errors) => Conflict(Json.toJson(Validation.errors(errors)))
              case Right(code) => {
                Ok(
                  Json.toJson(
                    Invocation(
                      source = code, // backwards compatible
                      files = Seq(
                        ServiceFileNames.toFile(
                          form.service.namespace,
                          form.service.organization.key,
                          form.service.application.key,
                          code
                        )
                      )
                    )
                  )
                )
              }
            }
          }
        }
      case _ => NotFound
    }
  }

}
