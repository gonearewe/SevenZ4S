package com.mactavish.sevenz4s

trait ProgressTracker extends ((Long, Long) => Unit) {
  def apply(completed: Long, total: Long): Unit
}

object ProgressTracker {
  def empty: ProgressTracker = (_, _) => {}
}
