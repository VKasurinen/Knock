package com.vkasurinen.knock.chat.data.audio

import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator

/**
 * The FFmpeg binary that JAVE bundles for this OS. The JVM has no AAC encoder or decoder, so
 * voice messages are converted between AAC (what Android and iOS send) and raw PCM with it.
 * PCM is always 16 bit signed little endian mono.
 */
internal object Ffmpeg {

    // Extracts the binary from the JAR into the temp directory on first use
    private val executablePath: String by lazy {
        DefaultFFMPEGLocator().executablePath
    }

    /**
     * Encodes the PCM written to the process' stdin into an AAC voice message at [outputPath].
     * The file is complete once stdin is closed and the process has exited.
     */
    fun startAacEncoder(sampleRate: Int, outputPath: String): Process {
        return start(
            "-f", "s16le",
            "-ar", sampleRate.toString(),
            "-ac", "1",
            "-i", "pipe:0",
            "-c:a", "aac",
            "-b:a", "64k",
            // Moves the index to the start of the file, so it can be played while downloading
            "-movflags", "+faststart",
            "-y", outputPath
        )
    }

    /**
     * Decodes [inputPath] and writes it as PCM to the process' stdout.
     */
    fun startPcmDecoder(inputPath: String, sampleRate: Int): Process {
        return start(
            "-i", inputPath,
            "-f", "s16le",
            "-ar", sampleRate.toString(),
            "-ac", "1",
            "pipe:1"
        )
    }

    private fun start(vararg arguments: String): Process {
        return ProcessBuilder(executablePath, "-hide_banner", "-loglevel", "error", *arguments)
            .start()
    }
}
