package com.example

import com.group7.dsl.arrivals
import com.group7.dsl.buildScenario
import com.group7.dsl.thenFork
import com.group7.dsl.thenJoin
import com.group7.dsl.thenQueue
import com.group7.dsl.thenService
import com.group7.dsl.thenSink
import com.group7.dsl.trackAll
import com.group7.dsl.withMetrics
import com.group7.generators.Delays
import com.group7.generators.Generators
import com.group7.metrics.Occupancy
import com.group7.policies.fork.ForkPolicy
import com.group7.policies.fork.LeastFullForkPolicy
import com.group7.policies.generic_fj.RandomPolicy
import com.group7.policies.generic_fj.forkPolicy
import com.group7.properties.Queue
import com.group7.runSimulations
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

fun main() {
    runSimulations(
        mapOf(
            "Random" to demoPort(forkPolicy(RandomPolicy())),
            "JSQ" to demoPort(LeastFullForkPolicy())
        ),
        20.days,
    )
}

private class Truck

private fun demoPort(
    policy: ForkPolicy<Truck>
) = buildScenario {
    arrivals(
        "Truck Arrivals",
        Generators.constant(::Truck, Delays.exponentialWithMean(3.1.minutes)),
    ).thenFork("Truck Split", listOf({
        it.thenQueue("Long Service Queue").thenService("Long Service", Delays.exponentialWithMean(10.minutes))
    }, {
        it.thenQueue("Short Service Queue").thenService("Short Service", Delays.exponentialWithMean(3.minutes))
    }), policy)
        .thenJoin("Truck Join")
        .thenSink("Truck Departures")
}
    .withMetrics { trackAll<Queue<*>>(Occupancy) }