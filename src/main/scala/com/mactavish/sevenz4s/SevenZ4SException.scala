package com.mactavish.sevenz4s

/**
 * Library used exception.
 *
 * Note that the 7z-binding library which this library is built upon throws its own
 * exception.
 *
 * @param message exception message
 */
case class SevenZ4SException(message: String) extends Exception(message)
