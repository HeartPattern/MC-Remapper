package io.github.readymadeprogrammer.mcremapper

import org.junit.Ignore
import org.junit.Test

class RunTest(){
    @Test
    @Ignore
    fun runTest(){
        main(arrayOf(
            "--mapping",
            "https://launcher.mojang.com/v1/objects/448ccb7b455f156bb5cb9cdadd7f96cd68134dbd/server.txt",
            "--input",
            "server.jar",
            "--output",
            "deobf.jar"
        ))
    }
}