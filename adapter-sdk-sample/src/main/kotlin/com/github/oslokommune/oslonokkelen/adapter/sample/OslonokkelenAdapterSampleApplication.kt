package com.github.oslokommune.oslonokkelen.adapter.sample

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int

fun main(args: Array<String>) {
    val command = RootCommand().subcommands(SampleAdapterCommand())
    command.main(args)
}

class RootCommand :CliktCommand(name = "oslonokkelen-sample-adapter") {
    override fun run() {

    }
}

class SampleAdapterCommand : CliktCommand(name = "server") {

    private val port by option("--port", help = "Server port")
        .int()
        .default(8080)

    override fun run() {
        echo("Starting adapter on port $port")
    }
}