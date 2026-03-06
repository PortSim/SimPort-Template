package com.example

import com.group7.dsl.NodeBuilder
import com.group7.dsl.arrivals
import com.group7.dsl.buildScenario
import com.group7.dsl.thenDelay
import com.group7.dsl.thenFork
import com.group7.dsl.thenJoin
import com.group7.dsl.thenPushFork
import com.group7.dsl.thenQueue
import com.group7.dsl.thenService
import com.group7.dsl.thenSink
import com.group7.dsl.trackGlobal
import com.group7.dsl.withMetrics
import com.group7.generators.Delays
import com.group7.generators.Generators
import com.group7.metrics.Occupancy
import com.group7.metrics.ResidenceTime
import com.group7.metrics.Throughput
import com.group7.runSimulations
import com.group7.utils.thenSubnetwork
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

fun main() {
    runSimulations(
        (12..18).associate { numCranes ->
            "$numCranes Cranes" to demoPort(numCranes = numCranes)
        },
        100.days,
    )
}

private class Truck

private fun demoPort(
    entryGateLanes: Int = 6,
    exitGateLanes: Int = 6,
    numCranes: Int = 29,
    truckArrivalsPerHour: Double = 50.0,
    averageGateServiceTime: Duration = 6.minutes,
    averageTravelTime: Duration = 5.6.minutes,
    averageHandlingTimeAtStack: Duration = 6.minutes,
    numTokens: Int = 30,
) = buildScenario {
    arrivals(
        "Truck Arrivals",
        Generators.constant(::Truck, Delays.exponential(truckArrivalsPerHour, DurationUnit.HOURS)),
    )
        .thenQueue("Truck Arrival Queue")
        .thenSubnetwork(capacity = numTokens) { entrance ->
            entrance
                .thenQueueAndGates("Entrance", entryGateLanes, averageGateServiceTime)
                .thenDelay("Travel to stacks", Delays.exponentialWithMean(averageTravelTime))
                .thenFork("ASC Split", numCranes) { i, lane ->
                    lane
                        .thenQueue("ASC Queue $i")
                        .thenService("ASC $i", Delays.exponentialWithMean(averageHandlingTimeAtStack))
                }
                .thenJoin("ASC Join")
                .thenDelay("Travel to gates", Delays.exponentialWithMean(averageTravelTime))
                .thenQueueAndGates("Exit", exitGateLanes, averageGateServiceTime)
        }
        .thenSink("Truck Departures")
}
    .withMetrics { 
        trackGlobal(Occupancy)
        trackGlobal(ResidenceTime)
        trackGlobal(Throughput)
    }

private fun <T> NodeBuilder<T, *>.thenQueueAndGates(
    description: String,
    numLanes: Int,
    averageServiceTime: Duration,
) = this
    .thenQueue("$description Queue")
    .thenPushFork("$description Lane Split", numLanes) { i, lane ->
        lane.thenService("$description Gate $i", Delays.exponentialWithMean(averageServiceTime))
    }
    .thenJoin("$description Lane Join")