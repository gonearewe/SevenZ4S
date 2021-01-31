package fun.mactavish.sevenz4s

import net.sf.sevenzipjbinding.{ArchiveFormat => Format}

sealed trait ArchiveFormat

sealed trait CreatableArchiveFormat extends ArchiveFormat

sealed trait UpdatableArchiveFormat extends ArchiveFormat

/**
 * `SingleArchiveFormat` usually only supports the compression of stream, they
 * don't have the ability of archiving, and often works together with some
 * archiving-only format.
 *
 * For example, for `file.tar.gz`, `Tar` first archives and `Gzip` does the compression.
 */
sealed trait SingleArchiveFormat extends ArchiveFormat

object ArchiveFormat {

  private[sevenz4s] def of(format: Format): ArchiveFormat = {
    format match {
      case Format.BZIP2 => BZIP2
      case Format.SEVEN_ZIP => SEVEN_Z
      case Format.ZIP => ZIP
      case Format.TAR => TAR
      case Format.SPLIT => SPLIT
      case Format.RAR => RAR
      case Format.RAR5 => RAR5
      case Format.LZMA => LZMA
      case Format.ISO => ISO
      case Format.HFS => HFS
      case Format.GZIP => GZIP
      case Format.CPIO => CPIO
      case Format.Z => Z
      case Format.ARJ => ARJ
      case Format.CAB => CAB
      case Format.LZH => LZH
      case Format.CHM => CHM
      case Format.NSIS => NSIS
      case Format.AR => AR
      case Format.RPM => RPM
      case Format.UDF => UDF
      case Format.WIM => WIM
      case Format.XAR => XAR
      case Format.FAT => FAT
      case Format.NTFS => NTFS
    }
  }

  final case object SEVEN_Z extends CreatableArchiveFormat with UpdatableArchiveFormat

  final case object ZIP extends CreatableArchiveFormat

  final case object TAR extends CreatableArchiveFormat with UpdatableArchiveFormat

  final case object GZIP extends CreatableArchiveFormat with UpdatableArchiveFormat with SingleArchiveFormat

  final case object BZIP2 extends CreatableArchiveFormat with UpdatableArchiveFormat with SingleArchiveFormat

  final case object SPLIT extends ArchiveFormat

  final case object RAR extends ArchiveFormat

  final case object RAR5 extends ArchiveFormat

  final case object LZMA extends SingleArchiveFormat

  final case object ISO extends ArchiveFormat

  final case object HFS extends ArchiveFormat

  final case object CPIO extends ArchiveFormat

  final case object Z extends SingleArchiveFormat

  final case object ARJ extends ArchiveFormat

  final case object CAB extends ArchiveFormat

  final case object LZH extends ArchiveFormat

  final case object CHM extends ArchiveFormat

  final case object NSIS extends ArchiveFormat

  final case object AR extends ArchiveFormat

  final case object RPM extends ArchiveFormat

  final case object UDF extends ArchiveFormat

  final case object WIM extends ArchiveFormat

  final case object XAR extends ArchiveFormat

  final case object FAT extends ArchiveFormat

  final case object NTFS extends ArchiveFormat

}
