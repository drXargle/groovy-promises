package com.bluetrainsoftware.common.promises

import com.bluetrainsoftware.common.promises.Promise
import org.junit.Test


class PromiseTest {

	@Test
	public void okPromise() {
		int count = 0
		Promise promise = new Promise( { resolve, reject ->
			resolve("fish")
		}).then({count ++}).then({count++})
		println count
		assert count == 2
	}

	@Test
	public void errorPromise() {
		int count = 0
		Promise promise = new Promise( { resolve, reject ->
			reject("fish")
		}).error({count ++}).error({count++})
		println count
		assert count == 2
	}

	@Test
	public void promiseCompletedThenAttachThen() {
		int count = 0
		Promise promise = new Promise( { resolve, reject ->
			resolve("fish")
		})
		assert count == 0
		promise.then({count ++})
		assert count == 1
		promise.then({count ++})
		assert count == 2
	}

	@Test
	public void delayedResult() {
		int count = 0

		Closure resolveIt = null
		Closure pClosure = { Closure resolve, Closure reject ->
			resolveIt = resolve
		}

		new Promise(pClosure).then({count ++}).then({count++})

		assert count == 0
		resolveIt("fish")
		assert count == 2
	}

	@Test
	public void thenReturnsPromise() {
		Closure resolveIt = null
		int count = 0
		int innerCount = 0

	  new Promise({ resolve, reject ->
		  resolve("fish")}).then({

		  return new Promise({ resolve ->
			  resolveIt = resolve }).then({innerCount ++}).then({
			    assert innerCount == 1
			    assert count == 0
		  })
	  }).then({
		  count ++
	  })

		assert count == 0
		assert innerCount == 0
		resolveIt("fish")
		assert count == 1
		assert innerCount == 1
	}

	@Test
	public void errorCallsError() {
		int errorCount = 0
		int okCount = 0

		new Promise({ resolve, reject ->
			resolve("fish")
		}).then({ throw new RuntimeException("blah")}).then({okCount ++}).error({
			errorCount ++
		})

		assert errorCount == 1
		assert okCount == 0
	}
}
