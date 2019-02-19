package example

import scala.math._
import cats._

object ProfunctorOpticsExample extends App {
  println("Hello")
}

object Profunctor {

  sealed trait Profunctor[F[_, _]] {
    def dimap[A, B, C, D](fab: F[A, B])(contraMap: C => A, map: B => D): F[C, D]
  }

  object instances {

    implicit def functionProfunctor = new Profunctor[Function1] {
      override def dimap[A, B, C, D](
          fab: A => B
      )(contraMap: C => A, map: B => D): C => D =
        contraMap.andThen(fab).andThen(map)
    }

    implicit def upstarProfunctor[F[_]: Functor]: Profunctor[Optics.Upstar[F, ?, ?]] =
      new Profunctor[Optics.Upstar[F, ?, ?]] {
        def dimap[A, B, C, D](
            fab: Optics.Upstar[F, A, B]
        )(contraMap: C => A, map: B => D) = {
          val cToFb: C => F[B] = contraMap.andThen(fab.aToFb)
          Optics.Upstar((c: C) => Functor[F].map(cToFb(c))(b => map(b)))
        }
      }

    implicit def downStarProfunctor[F[_]: Functor] = new Profunctor[Optics.DownStar[F, ?, ?]] {
      def dimap[A, B, C, D](
          fab: Optics.DownStar[F, A, B]
      )(contraMap: C => A, map: B => D) = {
        val fcToB: F[C] => B = (fc: F[C]) => fab.faToB(Functor[F].map(fc)(contraMap))
        Optics.DownStar(fcToB.andThen(map))
      }
    }

    implicit def upstarFunctor[F[_]: Functor, D] = new Functor[Optics.Upstar[F, D, ?]] {
      def map[A, B](fa: Optics.Upstar[F, D, A])(f: A => B): Optics.Upstar[F, D, B] =
        upstarProfunctor[F].dimap(fa)(identity, f)
    }

    implicit def downstarFunctor[F[_]: Functor, D] = new Functor[Optics.DownStar[F, D, ?]] {
      def map[A, B](fa: Optics.DownStar[F, D, A])(f: A => B): Optics.DownStar[F, D, B] =
        downStarProfunctor[F].dimap(fa)(identity, f)
    }
  }
}

object Cartesian {

  import Profunctor._
  import Profunctor.instances._

  sealed abstract class Cartesian[F[_, _]: Profunctor] {
    def first[A, B, C](fab: F[A, B]): F[(A, C), (B, C)]
    def second[A, B, C](fab: F[A, B]): F[(C, A), (C, B)]
  }

  object instance {

    val functionCartesian = new Cartesian[Function1] {
      def first[A, B, C](fab: A => B): ((A, C)) => (B, C) =
        (tuple: (A, C)) => (fab(tuple._1), tuple._2)

      def second[A, B, C](fab: A => B): ((C, A)) => (C, B) =
        (tuple: (C, A)) => (tuple._1, fab(tuple._2))
    }

    def upstarCartesian[F[_]: Functor] = new Cartesian[Optics.Upstar[F, ?, ?]] {
      def first[A, B, C](fab: Optics.Upstar[F, A, B]): Optics.Upstar[F, (A, C), (B, C)] =
        Optics.Upstar((tuple: (A, C)) => Functor[F].map(fab.aToFb(tuple._1))(b => (b, tuple._2)))

      def second[A, B, C](fab: Optics.Upstar[F, A, B]): Optics.Upstar[F, (C, A), (C, B)] =
        Optics.Upstar((tuple: (C, A)) => Functor[F].map(fab.aToFb(tuple._2))(b => (tuple._1, b)))
    }
  }
}

object Cocartesian {
  import Profunctor._
  import Profunctor.instances._

  abstract class Cocartesian[P[_, _]: Profunctor] {
    def left[A, B, C](pab: P[A, B]): P[Either[A, C], Either[B, C]]
    def right[A, B, C](pab: P[A, B]): P[Either[C, A], Either[C, B]]
  }

  object instances {

    val functionCocartesian = new Cocartesian[Function1] {
      def left[A, B, C](pab: A => B): Either[A, C] => Either[B, C]  = _.left.map(pab)
      def right[A, B, C](pab: A => B): Either[C, A] => Either[C, B] = _.right.map(pab)
    }

    def upstarCocartesian[F[_]: Applicative] = new Cocartesian[Optics.Upstar[F, ?, ?]] {

      def left[A, B, C](pab: Optics.Upstar[F, A, B]): Optics.Upstar[F, Either[A, C], Either[B, C]] = Optics.Upstar(
        (aOrC: Either[A, C]) => {
          aOrC match {
            case Left(a)  => Applicative[F].map(pab.aToFb(a))(b => Left(b))
            case Right(c) => Applicative[F].pure(Right(c))
          }
        }
      )

      def right[A, B, C](pab: Optics.Upstar[F, A, B]): Optics.Upstar[F, Either[C, A], Either[C, B]] =
        ???
    }
  }
}

object Optics {

  case class Upstar[F[_], A, B](val aToFb: A => F[B])

  case class DownStar[F[_], A, B](val faToB: F[A] => B)

  sealed trait Lens[A, B, S, T] {
    def view(s: S): A
    def update(b: B, s: S): T
  }

  sealed trait Prism[A, B, S, T] {
    def `match`(s: S): Either[T, A]
    def build(b: B): T
  }

  sealed trait Adapter[A, B, S, T] {
    def from(s: S): A
    def to(b: B): T
  }

  sealed trait Traversal[A, B, S, T] {
    def modifyF[F[_]: Applicative](f: A => F[B])(s: S): F[T]
  }

  object instances {

    def leftPairValueLens[A, B, C]: Lens[A, B, (A, C), (B, C)] =
      new Lens[A, B, (A, C), (B, C)] {
        override def view(s: (A, C))         = s._1
        override def update(b: B, s: (A, C)) = (b, s._2)
      }

    val sign: Lens[Boolean, Boolean, Integer, Integer] =
      new Lens[Boolean, Boolean, Integer, Integer] {
        override def view(s: Integer)               = s >= 0
        override def update(b: Boolean, s: Integer) = if (b) abs(s) else -s
      }

    def the[A, B]: Prism[A, B, Option[A], Option[B]] =
      new Prism[A, B, Option[A], Option[B]] {
        def `match`(s: Option[A]): Either[Option[B], A] = s match {
          case Some(a) => Right(a)
          case None    => Left(None)
        }
        def build(b: B): Option[B] = Some(b)
      }

  }
}
