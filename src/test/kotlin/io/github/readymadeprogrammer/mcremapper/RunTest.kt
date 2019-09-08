package io.github.readymadeprogrammer.mcremapper

import org.junit.Ignore
import org.junit.Test

class RunTest(){
    @Test
    @Ignore
    fun runTest(){
        main(arrayOf(
            "--mapping",
            "https://launcher.mojang.com/v1/objects/c0c8ef5131b7beef2317e6ad80ebcd68c4fb60fa/client.txt",
            "--input",
            "1.14.4.jar",
            "--output",
            "deobf.jar",
            "--thread",
            "8"
        ))
    }
}