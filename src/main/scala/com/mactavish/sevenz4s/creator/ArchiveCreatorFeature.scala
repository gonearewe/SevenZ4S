package com.mactavish.sevenz4s.creator

import net.sf.sevenzipjbinding.{IOutFeatureSetEncryptHeader, IOutFeatureSetLevel, IOutFeatureSetMultithreading, IOutFeatureSetSolid}

object ArchiveCreatorFeature {

  trait SetEncryptHeader[T <: AbstractArchiveCreator[T] with SetEncryptHeader[T]] {
    this: T =>

    protected val archive: IOutFeatureSetEncryptHeader

    def setHeaderEncryption(b: Boolean): T = {
      archive.setHeaderEncryption(b)
      this
    }
  }

  trait SetLevel[T <: AbstractArchiveCreator[T] with SetLevel[T]] {
    this: T =>

    protected val archive: IOutFeatureSetLevel

    def setLevel(compressionLevel: Int): T = {
      archive.setLevel(compressionLevel)
      this
    }
  }

  trait SetMultithreading[T <: AbstractArchiveCreator[T] with SetMultithreading[T]] {
    this: T =>

    protected val archive: IOutFeatureSetMultithreading

    def setThreadCount(threadCount: Int): T = {
      archive.setThreadCount(threadCount)
      this
    }
  }

  trait SetSolid[T <: AbstractArchiveCreator[T] with SetSolid[T]] {
    this: T =>

    protected val archive: IOutFeatureSetSolid

    def setSolid(b: Boolean): T = {
      archive.setSolid(b)
      this
    }

    def setSolidFiles(i: Int): T = {
      archive.setSolidFiles(i)
      this
    }

    def setSolidSize(l: Long): T = {
      archive.setSolidSize(l)
      this
    }

    def setSolidExtension(b: Boolean): T = {
      archive.setSolidExtension(b)
      this
    }
  }
}
