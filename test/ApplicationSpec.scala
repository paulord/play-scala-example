import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(app, FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
    }

    "create new person" in new WithApplication {

        val requestData = Json.toJson(Map("name" -> JsString("Test Person Name"), "age" -> JsNumber(30), "sex" -> JsString("F")))
        val postResult = route(app, FakeRequest(POST, controllers.routes.PersonController.addPerson.url).withJsonBody(requestData)).get
        status(postResult) must equalTo(SEE_OTHER)
        redirectLocation(postResult) must beSome(controllers.routes.PersonController.index.url)

        val getResult = route(app, FakeRequest(GET, controllers.routes.PersonController.getPersons.url, FakeHeaders(), "")).get
        status(getResult) must equalTo(OK)
        val json = contentAsJson(getResult)
        (json(0) \ "name").as[String] must equalTo("Test Person Name")
        (json(0) \ "age").as[Int] must equalTo(30)
        (json(0) \ "sex").as[String] must equalTo("F")

    }

  }
}
