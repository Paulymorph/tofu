package tofu.syntax
import cats.syntax._
import cats.Parallel
import cats.Defer

object parallel
    extends ParallelSyntax with ParallelTraverseSyntax with ParallelFlatSyntax with ParallelApplySyntax
    with ParallelBitraverseSyntax with ParallelUnorderedTraverseSyntax with ParallelFoldMapASyntax
    with ParallelTraverseFilterSyntax {

  final implicit class TofuParallelOps[F[_], A](private val fa: F[A]) extends AnyVal {
    def parReplicate(count: Int)(implicit F: Parallel[F]): F[List[A]] =
      F.sequential(F.applicative.replicateA(count, F.parallel(fa)))

    def parReplicate_(count: Long)(implicit F: Parallel[F], FD: Defer[F]): F[Unit] =
      if (count <= 0) F.monad.unit
      else
        F.sequential(F.applicative.productR(F.parallel(fa))(F.parallel(FD.defer(parReplicate_(count - 1)))))

    def parReplicateBatch_(count: Long, batch: Long)(implicit F: Parallel[F], FD: Defer[F]): F[Unit] =
      F.monad.productR(parReplicate_(count.min(batch)))(FD.defer(parReplicateBatch_(count - batch, batch)))
  }
}
