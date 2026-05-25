package com.example.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object RealCommandExecutor {

    /**
     * Execute a command and stream output line by line.
     */
    fun executeStreaming(command: String, useRoot: Boolean = false): Flow<String> = flow {
        try {
            val process = if (useRoot) {
                Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            } else {
                Runtime.getRuntime().exec(command.split(" ").toTypedArray())
            }

            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

            var line: String?
            while (stdoutReader.readLine().also { line = it } != null) {
                emit(line!!)
            }
            // Also emit stderr
            while (stderrReader.readLine().also { line = it } != null) {
                emit("[stderr] $line")
            }

            process.waitFor()
            stdoutReader.close()
            stderrReader.close()

        } catch (e: SecurityException) {
            emit("[ERROR] Permission denied: ${e.message}")
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Execute a command and return the full output synchronously.
     */
    suspend fun executeSync(command: String, useRoot: Boolean = false): CommandResult {
        return withContext(Dispatchers.IO) {
            try {
                val process = if (useRoot) {
                    Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                } else {
                    Runtime.getRuntime().exec(command.split(" ").toTypedArray())
                }
                val stdout = process.inputStream.bufferedReader().readText()
                val stderr = process.errorStream.bufferedReader().readText()
                val exitCode = process.waitFor()
                CommandResult(exitCode, stdout, stderr, null)
            } catch (e: Exception) {
                CommandResult(-1, "", "", e)
            }
        }
    }

    /**
     * Execute a command with args list and return result.
     */
    suspend fun executeWithArgs(args: Array<String>, useRoot: Boolean = false): CommandResult {
        return withContext(Dispatchers.IO) {
            try {
                val fullArgs = if (useRoot) arrayOf("su", "-c", args.joinToString(" ")) else args
                val process = Runtime.getRuntime().exec(fullArgs)
                val stdout = process.inputStream.bufferedReader().readText()
                val stderr = process.errorStream.bufferedReader().readText()
                val exitCode = process.waitFor()
                CommandResult(exitCode, stdout, stderr, null)
            } catch (e: Exception) {
                CommandResult(-1, "", "", e)
            }
        }
    }

    data class CommandResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
        val error: Exception?
    ) {
        val success: Boolean get() = exitCode == 0 && error == null
        val output: String get() = if (stdout.isNotEmpty()) stdout else stderr
    }
}
