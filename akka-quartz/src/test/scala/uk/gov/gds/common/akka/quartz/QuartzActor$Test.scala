package uk.gov.gds.common.akka.quartz

import org.specs2.mutable._
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import akka.actor.{Props, ActorSystem, Actor}
import akka.util.{Duration, Timeout}
import akka.pattern.ask
import akka.dispatch.{Future, Await}


class QuartzActor$Test extends Specification  {



	object SpecActors {
		case class Tickle()
		case class GetTickle()
		class RecvActor extends Actor {
			var lastMsg : Any = null
			def receive = {
				case a : Tickle => lastMsg = a
				case GetTickle() => context.sender ! lastMsg
				case c => println("Unknown message on the recvactor!!" + c)
			}
		}
	}

	"Basic logic" should {
		"has equals" in {
			val t = 1
			1 must beEqualTo(t)
		}
	}

	"Basic single actors should" should {
		implicit val system = ActorSystem("GAT")
		val ar = TestActorRef(new QuartzActor)
		val recv = TestActorRef(new SpecActors.RecvActor)
		implicit val timeout = Timeout(Duration(5, "seconds"))
		"add a cron job" in {

			val f = (ar ? AddCronSchedule(recv, "0/5 * * * * ?", SpecActors.Tickle(), true))
			f.value.get must beLike {
				case Right(t : AddCronScheduleResult) => ok
			}
		}
		"deliver messages on time" in {

			Thread.sleep(10000)
			(recv ? SpecActors.GetTickle()).value.get must beLike {
				case Right(SpecActors.Tickle()) => ok
			}
		}

		"add then cancel messages" in {
			val d = ar ? AddCronSchedule(recv, "4 4 * * * ?", SpecActors.Tickle(), true)
			val cancel = d.value.get match {
				case Right(AddCronScheduleSuccess(cancel)) => cancel
			}
			cancel.cancel()
			Thread.sleep(100)
			cancel.isCancelled must beEqualTo(true)
		}

		"fail with invalid cron expressions" in {
			(ar ? AddCronSchedule(recv, "clearly invalid", SpecActors.Tickle(), true)).value.get must beLike {
				case Right(AddCronScheduleFailure(e)) => ok
			}
		}
	}

}
