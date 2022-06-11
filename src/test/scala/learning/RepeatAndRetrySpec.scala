package learning

import cats.implicits._
import zio._
import zio.clock.Clock
import zio.duration._
import zio.test._
import zio.test.environment.Live

@SuppressWarnings(Array("org.wartremover.warts.Serializable", "org.wartremover.warts.JavaSerializable"))
object RepeatAndRetrySpec extends DefaultRunnableSpec {

  override def spec: ZSpec[Environment, Failure] =
    suite("RepeatAndRetrySpec")(
      testM("retry") {
        for {
          counter <- Ref.make(0)
          effect = for {
            count <- counter.get
            _ <- Live.live(console.putStrLn(s"attempt ${count.toString}"))
            _ <- counter.set(count + 1)
            _ <- if (count === 5) ZIO.succeed(()) else ZIO.fail("err")
          } yield ()
          _ <- effect.retry(Schedule.spaced(1.second)).provideSomeLayer[zio.test.environment.Live](Clock.live)
          _ <- Live.live(console.putStrLn("success!"))
        } yield assertCompletes
      },
      testM("repeat") {
        for {
          counter <- Ref.make(0.1)
          effect = for {
            count <- counter.get
            _ <- Live.live(console.putStrLn(s"attempt ${count.toString}"))
            _ <- counter.set(count + 0.1)
          } yield count
          e <- effect
            .repeat(Schedule.recurUntil[Double](_ === 0.4) && Schedule.recurs(5) && Schedule.spaced(2.second))
            .provideSomeLayer[zio.test.environment.Live](Clock.live)
          _ <- Live.live(console.putStrLn(e.toString()))
          _ <- Live.live(console.putStrLn("success!"))
        } yield assertCompletes
      }
    )
}
