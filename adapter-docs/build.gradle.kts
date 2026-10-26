import java.io.InputStream
import java.util.concurrent.TimeUnit.SECONDS

plugins {
    alias(libs.plugins.asciidoc.convert)
    alias(libs.plugins.asciidoc.pdf)
    alias(libs.plugins.asciidoc.gems)
}

tasks.asciidoctor {
    setOutputDir(file("../docs"))
    outputOptions {
        if (project.hasProperty("pdf")) {
            backends("pdf")
        } else {
            backends("html5")
        }
    }
    resources {
        include("images/**")
    }
    attributes(
        mapOf(
            "toc" to "left",
            "toclevels" to "3",
            "source-highlighter" to "highlightjs"
        )
    )
    setBaseDir(file("src/docs/asciidoc"))

    doFirst {
        ProcessUtil.requireTool("graphviz", "sudo apt install graphviz", "dot", "-V")
        ProcessUtil.requireTool("seqdiag", "sudo pip3 install seqdiag", "seqdiag", "--version")
        ProcessUtil.requireTool("blockdiag", "sudo pip3 install blockdiag", "blockdiag", "--version")
        ProcessUtil.requireTool(
            "svgbob",
            "cargo install --git https://github.com/ivanceras/svgbob --rev 2bf94a2eddff2215b5f43e21658502c98d945941",
            "svgbob",
            "--help"
        )
    }
}


asciidoctorj {
    setVersion(libs.versions.asciidoctorj.asProvider().get())
    modules.diagram.setVersion(libs.versions.asciidoctorj.diagram.get())
    logLevel = LogLevel.INFO
}


object ProcessUtil {

    fun requireTool(name: String, installHint: String, vararg command: String) {
        try {
            execute(*command)
        } catch (ignored: Exception) {
            throw GradleException("Unable to find '${command.first()}'. Please install $name.\nThis is required for generating documentation diagrams.\n\n\u001B[91m$installHint\u001B[0m")
        }
    }

    fun execute(vararg command: String): ProcessOutput {
        val process = ProcessBuilder(*command).start()

        return execute(process, command)
    }

    private fun execute(process: Process, command: Array<out String>): ProcessOutput {
        if (!process.waitFor(5, SECONDS)) {
            process.destroy()
            throw ProcessException(
                command = command.joinToString(" "),
                details = "Timed out waiting for: $command",
                exitCode = -1
            )
        }
        if (process.exitValue() != 0) {
            throw ProcessException(
                command = command.joinToString(" "),
                details = readStream(process.errorStream),
                exitCode = process.exitValue()
            )
        }

        return ProcessOutput(
            out = readStream(process.inputStream),
            err = readStream(process.errorStream)
        )
    }

    private fun readStream(stream: InputStream): String {
        return stream.reader(Charsets.UTF_8).readText().trim()
    }

}

data class ProcessOutput(
    val out: String,
    val err: String
)

class ProcessException(command: String, exitCode: Int, details: String) :
    GradleException("$command failed with exit code $exitCode\n-------------------------------\n$details")
