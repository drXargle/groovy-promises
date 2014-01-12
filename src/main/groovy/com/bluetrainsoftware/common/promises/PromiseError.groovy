package com.bluetrainsoftware.common.promises

/**
 *
 * @author: Richard Vowles - https://plus.google.com/+RichardVowles
 */
class PromiseError extends RuntimeException {
	def error

	public PromiseError(String msg, def error = null) {
		super(msg)

		this.error = error
	}
}
