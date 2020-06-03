package io.heartpattern.mcremapper.parser

import io.heartpattern.mcremapper.model.*
import io.heartpattern.mcremapper.parser.proguard.ClassProguardParser
import io.heartpattern.mcremapper.parser.proguard.FieldProguardParser
import io.heartpattern.mcremapper.parser.proguard.MethodProguardParser
import org.junit.Test
import kotlin.test.assertEquals

class ParserTest {
    @Test
    fun classParseTest() {
        val raw = """net.minecraft.DefaultUncaughtExceptionHandler -> g:
    org.apache.logging.log4j.Logger logger -> a
    8:10:void <init>(org.apache.logging.log4j.Logger) -> <init>
    14:15:int uncaughtException(java.lang.Thread,java.lang.Throwable) -> t"""

        val result = ClassMapping(
            ClassSignature("net.minecraft.DefaultUncaughtExceptionHandler"),
            "g",
            setOf(
                FieldMapping(
                    FieldSignature(
                        TypeSignature("Lorg.apache.logging.log4j.Logger;"),
                        "logger"
                    ),
                    "a"
                )
            ),
            setOf(
                MethodMapping(
                    MethodSignature(
                        TypeSignature("V"),
                        listOf(
                            TypeSignature("Lorg.apache.logging.log4j.Logger;")
                        ),
                        "<init>"
                    ),
                    "<init>"
                ),
                MethodMapping(
                    MethodSignature(
                        TypeSignature("I"),
                        listOf(
                            TypeSignature("Ljava.lang.Thread;"),
                            TypeSignature("Ljava.lang.Throwable;")
                        ),
                        "uncaughtException"
                    ),
                    "t"
                )
            )
        )

        assertEquals(result, ClassProguardParser.parse(raw))
    }

    @Test
    fun fieldParsingTest() {
        val raw = "org.apache.logging.log4j.Logger LOGGER -> a"
        val result = FieldMapping(
            FieldSignature(TypeSignature(
                "Lorg.apache.logging.log4j.Logger;"),
                "LOGGER"
            ),
            "a"
        )

        assertEquals(result, FieldProguardParser.parse(raw))
    }

    @Test
    fun arrayFieldParsingTest() {
        val raw = "java.lang.String[][] requirements -> f"
        val result = FieldMapping(
            FieldSignature(TypeSignature(
                "[[Ljava.lang.String;"),
                "requirements"
            ),
            "f"
        )

        assertEquals(result, FieldProguardParser.parse(raw))
    }

    @Test
    fun methodParsingTest() {
        val raw = "176:176:net.minecraft.advancements.Advancement\$Builder display(net.minecraft.world.level.ItemLike,net.minecraft.network.chat.Component,net.minecraft.network.chat.Component,net.minecraft.resources.ResourceLocation,net.minecraft.advancements.FrameType,boolean,boolean,boolean) -> a"
        val result = MethodMapping(
            MethodSignature(
                TypeSignature("Lnet.minecraft.advancements.Advancement\$Builder;"),
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

        assertEquals(result, MethodProguardParser.parse(raw))
    }
}