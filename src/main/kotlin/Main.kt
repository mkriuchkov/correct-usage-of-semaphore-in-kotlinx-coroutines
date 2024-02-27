package org.example

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

const val DELAY = 500L

suspend fun main() = coroutineScope {
    println("Starting correct...")
    correct()
    println("Correct - done")

    println("Starting wrong...")
    wrong()
    println("wrong - done")
}


suspend fun correct() = coroutineScope {
    val from = 1
    val to = 100
    val semaphore = Semaphore(5)

    (from..to).map {
        val r = async {
            println("$it Before permit: ${semaphore.availablePermits}")

            // Inside Coroutine Scope
            // When withPermit is placed inside the coroutine scope, it means the permit from the semaphore
            // is acquired just before starting the suspending operation (delay in this case) and released
            // immediately after it completes. This approach is generally used when one wants to limit the
            // number of concurrent coroutines executing a specific suspending operation. It's useful for
            // controlling access to a particular resource or operation that only a fixed number of coroutines
            // should be doing at the same time.
            semaphore.withPermit {
                delay(DELAY)
            }
            println("$it After permit: ${semaphore.availablePermits}")
        }
        r
    }.awaitAll()
}

suspend fun wrong() = coroutineScope {
    val from = 1
    val to = 100
    val semaphore = Semaphore(5)

    (from..to).map {
        // Outside the coroutine scope, implies acquiring a permit before launching each coroutine and
        // releasing it as soon as the coroutine's initial setup is done.
        //
        // !!!*** It doesn't wait for the coroutine to complete its execution. ***!!!
        //
        // This pattern doesn't effectively limit the concurrency of the suspending operation (delay in your case)
        // itself because the permit is released right after the coroutine is launched, not after it finishes.
        // This might not be what one intend if his goal is to limit concurrent executions of the suspending block.
        semaphore.withPermit {
            println("$it Before async: ${semaphore.availablePermits}")
            val r = async {
                delay(DELAY)
            }
            println("$it After async: ${semaphore.availablePermits}")
            r
        }
    }.awaitAll()
}
