package io.github.readymadeprogrammer.mcremapper

import kotlin.test.Test
import kotlin.test.assertEquals

class ParsingTest {
    @Test
    fun classParsingTest() {
        val raw = "net.minecraft.CrashReport -> d:"
        val result = Mapping(ClassInfo("net.minecraft.CrashReport"), "d")
        assertEquals(result, parseClassMapping(raw))
    }

    @Test
    fun fieldParsingTest() {
        val raw = "org.apache.logging.log4j.Logger LOGGER -> a"
        val result = Mapping(FieldInfo(TypeDescriptor("Lorg.apache.logging.log4j.Logger;"), "LOGGER"), "a")
        assertEquals(result, parseFieldMapping(raw))
    }

    @Test
    fun arrayFieldParsingTest() {
        val raw = "java.lang.String[][] requirements -> f"
        val result = Mapping(FieldInfo(TypeDescriptor("[[Ljava.lang.String;"), "requirements"), "f")
        assertEquals(result, parseFieldMapping(raw))
    }

    @Test
    fun methodParsingTest() {
        val raw = "176:176:net.minecraft.advancements.Advancement${'$'}Builder display(net.minecraft.world.level.ItemLike,net.minecraft.network.chat.Component,net.minecraft.network.chat.Component,net.minecraft.resources.ResourceLocation,net.minecraft.advancements.FrameType,boolean,boolean,boolean) -> a\n"
        val result = Mapping(
            MethodInfo(
                TypeDescriptor("Lnet.minecraft.advancements.Advancement${'$'}Builder;"),
                listOf(
                    TypeDescriptor("Lnet.minecraft.world.level.ItemLike;"),
                    TypeDescriptor("Lnet.minecraft.network.chat.Component;"),
                    TypeDescriptor("Lnet.minecraft.network.chat.Component;"),
                    TypeDescriptor("Lnet.minecraft.resources.ResourceLocation;"),
                    TypeDescriptor("Lnet.minecraft.advancements.FrameType;"),
                    TypeDescriptor("Z"),
                    TypeDescriptor("Z"),
                    TypeDescriptor("Z")
                ),
                "display"
            ),
            "a"
        )
        assertEquals(result, parseMethodMapping(raw))
    }
}