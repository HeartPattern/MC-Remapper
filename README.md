# !Important

MC-Remapper is not working for now. It only map class and package correctly, and cannot map field, method, and inner class. For now, use only for test.

# MC-Remapper

Deobfuscator for Minecraft

Mojang provide mapping file of obfuscated class, field, and method name for minecraft mod developer.
This program apply mapping file to original minecraft code. It does not decompile jar,
but only apply mapping. You have to decompile jar with your favorite decompiler after deobfuscate minecraft with mc-remapper.

# Compile
Clone project to your local machine and open terminal in directory where build.gradle located.
Run following command to compile.

```
./gradlew installZip
```

Runnable script and runtime libraries will generate under `build/install/MC-Remapper`.



# Usage

Open terminal at `build/install/MC-Remapper/bin`. Execute MC-Remapper (Mac/Linux) or MC-Remapper.bat(Windows) with following parameters.



### --mapping

Url of mapping file. You can find this in `.minecraft/versions/$version$/$version$.json`.

`client_mappings` for minecraft client, and `server_mapping` for minecraft server.



### --input

Path to input jar file. Input jar can be minecraft client(`.minecraft/versions/$version$/$version$.jar`), minecraft client(Download from minecraft.net) or other file for reobfuscating.(--reobf flag)



### --output

Path to output file. If file is already exists in path, that wil be erased.



### --reobf

Reobf option reverse mapping direction. By default, MC-Remapper map obfuscated code to deobfuscated code. But if you write your mod/plugin with deobfuscated minecraft source and apply it to forge/bukkit, minecraft will crash with NoClassDefFoundException, NoSuchMethodException, or else. That's because, forge or bukkit use obfuscated code but your plugin tried to access to deobfuscated code. In this case, you have to re-obfuscate your mod/plugin to use obfuscated minecraft code. You can use this option to map deobfuscated to. obfuscated code.



## Example usage

```
./MC-Remapper --mapping https://launcher.mojang.com/v1/objects/448ccb7b455f156bb5cb9cdadd7f96cd68134dbd/server.txt --input server.jar --output deobf.jar
```

