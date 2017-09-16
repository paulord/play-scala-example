package controllers

import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import models._
import dal._

import scala.concurrent.{ ExecutionContext, Future }

import javax.inject._

class PersonController @Inject() (repo: PersonRepository, val messagesApi: MessagesApi)
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport{
  /**
   * The mapping for the person forms
   */
   val personForm: Form[PersonForm] = Form {
      mapping(
        "name" -> nonEmptyText,
        "age" -> number.verifying(min(0), max(140)),
        "sex" -> text(minLength=1, maxLength=1).verifying(pattern("""M|F""".r))
      )(PersonForm.apply)(PersonForm.unapply)
   }

  /**
   * The index action.
   */
  def index = Action.async { implicit request =>
    repo.list().map { people =>
      Ok(views.html.index(people))
    }
  }

  /**
   * The add person action.
   *
   * This is asynchronous, since we're invoking the asynchronous methods on PersonRepository.
   */
  def addPerson = Action.async { implicit request =>
    // Bind the form first, then fold the result, passing a function to handle errors, and a function to handle succes.
    personForm.bindFromRequest.fold(
      // The error function. We return the index page with the error form, which will render the errors.
      // We also wrap the result in a successful future, since this action is synchronous, but we're required to return
      // a future because the person creation function returns a future.
      errorForm => {
        Future.successful(Ok(views.html.addPerson(errorForm)))
      },
      // There were no errors in the from, so create the person.
      person => {
        repo.create(person.name, person.age, person.sex).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.PersonController.index)
        }
      }
    )
  }

  def viewAddPerson = Action.async { implicit request =>
    Future.successful(Ok(views.html.addPerson(personForm)))
  }


  def updatePerson(id: Long) = Action.async { implicit request =>

    // Bind the form first, then fold the result, passing a function to handle errors, and a function to handle succes.
    personForm.bindFromRequest.fold(
      // The error function. We return the index page with the error form, which will render the errors.
      // We also wrap the result in a successful future, since this action is synchronous, but we're required to return
      // a future because the person creation function returns a future.
      errorForm => {
        Future.successful(Ok(views.html.updatePerson(errorForm, id)))
      },
      // There were no errors in the from, so create the person.
      person => {
        repo.update(id, person.name, person.age, person.sex).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.PersonController.index)
        }
      }
    )
  }

  def viewPerson(id: Long) = Action.async { implicit request =>

    repo.get(id).map { person =>

        Ok(views.html.updatePerson(personForm.bind(
          Map("name" -> person.name, "age" -> person.age.toString, "sex" -> person.sex)
        ), id))

    }

  }

  /**
   * A REST endpoint that gets all the people as JSON.
   */
  def getPersons = Action.async { implicit request =>
    repo.list().map { people =>
      Ok(Json.toJson(people))
    }
  }
}

/**
 * The Person form.
 *
 * Generally for forms, you should define separate objects to your models, since forms very often need to present data
 * in a different way to your models.  In this case, it doesn't make sense to have an id parameter in the form, since
 * that is generated once it's created.
 */
 case class PersonForm(name: String, age: Int, sex: String)
