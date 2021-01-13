package com.mactavish.sevenz4s

import net.sf.sevenzipjbinding.{IOutFeatureSetEncryptHeader, IOutFeatureSetLevel, IOutFeatureSetMultithreading, IOutFeatureSetSolid}

object ArchiveCreatorFeature {

  trait SetEncryptHeader[T <: AbstractArchiveCreator[T, _] with SetEncryptHeader[T]] {
    protected val archive: IOutFeatureSetEncryptHeader

    def setHeaderEncryption(b: Boolean): T = {
      archive.setHeaderEncryption(b)
      this.asInstanceOf[T]
    }
  }

  trait SetLevel[T <: AbstractArchiveCreator[T, _] with SetLevel[T]]{
    protected val archive: IOutFeatureSetLevel

    def setLevel(compressionLevel: Int):T={
      archive.setLevel(compressionLevel)
      this.asInstanceOf[T]
    }
  }

  trait SetMultithreading[T <: AbstractArchiveCreator[T, _] with SetMultithreading[T]]{
    protected val archive: IOutFeatureSetMultithreading

    def	setThreadCount(threadCount:Int):T={
      archive.setThreadCount(threadCount)
      this.asInstanceOf[T]
    }
  }

  trait SetSolid[T <: AbstractArchiveCreator[T, _] with SetSolid[T]] {
    protected val archive: IOutFeatureSetSolid

    def setSolid(b: Boolean): T = {
      archive.setSolid(b)
      this.asInstanceOf[T]
    }

    def setSolidFiles(i: Int): T = {
      archive.setSolidFiles(i)
      this.asInstanceOf[T]
    }

    def setSolidSize(l: Long): T = {
      archive.setSolidSize(l)
      this.asInstanceOf[T]
    }

    def setSolidExtension(b: Boolean): T = {
      archive.setSolidExtension(b)
      this.asInstanceOf[T]
    }
  }

}
