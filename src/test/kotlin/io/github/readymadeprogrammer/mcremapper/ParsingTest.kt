package io.github.readymadeprogrammer.mcremapper

import io.github.readymadeprogrammer.mcremapper.mapping.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ParsingTest{
    @Test
    fun classParsingTest(){
        val raw = "net.minecraft.CrashReport -> d:"
        val result = Pair(ClassSignature("net.minecraft.CrashReport"), "d")
        assertEquals(result, parseClassMapping(raw))
    }

    @Test
    fun fieldParsingTest(){
        val raw = "org.apache.logging.log4j.Logger LOGGER -> a"
        val result = Pair(FieldSignature(TypeSignature("Lorg.apache.logging.log4j.Logger;"), "LOGGER"), "a")
        assertEquals(result, parseFieldMapping(raw))
    }

    @Test
    fun arrayFieldParsingTest(){
        val raw = "java.lang.String[][] requirements -> f"
        val result = Pair(FieldSignature(TypeSignature("[[Ljava.lang.String;"), "requirements"), "f")
        assertEquals(result, parseFieldMapping(raw))
    }

    @Test
    fun methodParsingTest(){
        val raw = "176:176:net.minecraft.advancements.Advancement${'$'}Builder display(net.minecraft.world.level.ItemLike,net.minecraft.network.chat.Component,net.minecraft.network.chat.Component,net.minecraft.resources.ResourceLocation,net.minecraft.advancements.FrameType,boolean,boolean,boolean) -> a\n"
        val result = Pair(
                MethodSignature(
                        TypeSignature("Lnet.minecraft.advancements.Advancement${'$'}Builder;"),
                        listOf(
                                TypeSignature("Lnet.minecraft.world.level.ItemLike;"),
                                TypeSignature("Lnet.minecraft.network.chat.Component;"),
                                TypeSignature("Lnet.minecraft.network.chat.Component;"),
                                TypeSignature("Lnet.minecraft.resources.ResourceLocation;"),
                                TypeSignature("Lnet.minecraft.advancements.FrameType;"),
                                TypeSignature("Z"),
                                TypeSignature("Z"),
                                TypeSignature("Z")
                        ),
                        "display"
                ),
                "a"
        )
        assertEquals(result, parseMethodMapping(raw))
    }
}