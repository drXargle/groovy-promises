package com.bluetrainsoftware.common.promises

import groovy.transform.CompileStatic

@CompileStatic
class Promise {
	def resolved
	def error

	/**
	 * resolved could be null
	 */
	private boolean resolvedSet = false

	/**
	 * error could be null
	 */
	private boolean errorSet = false

	private boolean processingQueue = false

	static class BasePromiseAction {
		Closure success
		Closure error
	}

	static class Then extends BasePromiseAction {
	}

	static class Done extends BasePromiseAction {

	}

	// these are synchronized
	List<Then> thens = new Vector<Then>()
	List<Done> dones = new Vector<Done>()

	protected void reject(def error) {
		this.resolvedSet = false
		this.error = error
		this.errorSet = true

		processQueue()

	}

	protected void resolve(def resolved) {
		this.resolved = resolved
		this.resolvedSet = true

		processQueue()
	}

	public Promise(Closure promiseClosure) {
		try {
			if (promiseClosure.maximumNumberOfParameters == 0) {
				promiseClosure.delegate = this
				promiseClosure()
			} else if (promiseClosure.maximumNumberOfParameters == 1) {
				promiseClosure.call(this.&resolve)
			} else {
				promiseClosure.call(this.&resolve, this.&reject)
			}
		} catch (Exception ex) {
			reject(ex)
		}
	}

	/**
	 * calls the appropriate closure with optional parameters of:
	 * value, resolveMethod, rejectMethod
	 *
	 * @param call
	 * @param value
	 *
	 * @return
	 */
	private def call(Closure call, def value) {

		try {
			if (call.maximumNumberOfParameters == 1) {
				value = call.call(value)
			} else if (call.maximumNumberOfParameters == 2) {
				value = call.call(value, this.&resolve)
			} else {
				value = call.call(value, this.&resolve, this.&reject)
			}

			/**
			 * If this is a promise, we need to add a "done" to the end of it so we can
			 * get the final value in the chain, and re-start out chain. We stop our chain until this happens
			 */
			if (value instanceof Promise) {
				(value as Promise).done( this.&resolve, this.&reject )
				resolvedSet = false
				errorSet = false
			}
		} catch ( Exception ex ) {
			reject(ex)
			value = ex
		}

		return value
	}

	private void processQueue() {
		if (processingQueue) {
			return
		}

		try {
			processingQueue = true

			while (thens.size() > 0 && (resolvedSet || errorSet) ) {
				Then then = thens.remove(0)

				if (then.success && resolvedSet) {
					resolved = call(then.success, resolved)
				} else if (then.error && errorSet) {
					error = call(then.error, error)
				}
			}

			while ((resolvedSet || errorSet) && dones.size()) {
				Done done = dones.remove(0)

				if (done.success && resolvedSet) {
					done.success.call(resolved)
				} else if (done.error && errorSet) {
					done.error.call(error)
				}
			}
		} finally {
			processingQueue = false
		}
	}

	/**
	 * With "done" we ignore the result of them and keep the original "resolved" value
	 *
	 * @param success
	 * @param failure
	 * @return
	 */
	Promise done(Closure success, Closure failure = null) {
		dones.add(new Done(success: success, error: failure))

		if (resolvedSet || errorSet) {
			processQueue()
		}

		return this
	}

	/**
	 * With "then", the result becomes the input of the next "then".
	 *
	 * @param success
	 * @param failure
	 * @return
	 */
	Promise then(Closure success, Closure failure = null) {
		thens.add(new Then(success: success, error: failure))

		if (resolvedSet || errorSet) {
			processQueue()
		}

		return this
	}

	Promise error(Closure failure) {
		thens.add(new Then(error: failure))

		if (resolvedSet || errorSet) {
			processQueue()
		}

		return this
	}
}
