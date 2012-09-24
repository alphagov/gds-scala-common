package uk.gov.gds.common.play.logging

import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc._


trait RequestLogging {

  object RequestLoggingAction {

    def apply(block: Request[AnyContent] => Result): Action[AnyContent] = apply(BodyParsers.parse.anyContent)(block)

    def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Result): Action[A] = new Action[A] {
      def parser = bodyParser
      def apply(ctx: Request[A]) = {
        val start = new DateTime()
        val response = block(ctx)
        val end = new DateTime()
        Logger.info(
          "Time taken to " + ctx.method +
            " path " + ctx.path +
            " was " + end.minus(start.getMillis).getMillis)
        response
      }
    }
  }
}