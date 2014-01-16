package com.bluetrainsoftware.common.promises

import groovy.transform.CompileStatic

/**
 *
 * @author: Richard Vowles - https://plus.google.com/+RichardVowles
 */
@CompileStatic
class PromiseError extends RuntimeException {
	def error

	public PromiseError(String msg, def error = null) {
		super(msg)

		this.error = error
	}
}
