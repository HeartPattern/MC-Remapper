# !Important

MC-Remapper is not tested. Do not trust it.

# MC-Remapper

Deobfuscator for Minecraft

Mojang provide mapping file of obfuscated class, field, and method name for minecraft mod developer.
This program apply mapping file to original minecraft code. It does not decompile jar,
but only apply mapping. You have to decompile jar with your favorite decompiler after deobfuscate minecraft with mc-remapper.

# Compile
Clone project to your local machine and open terminal in directory where build.gradle located.
Run following command to compile.

```
./gradlew installDist
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

Path to output file. If file is already exists in path, that will be erased.

### --thread (Optional, Default=8)
Number of thread used for apply mapping to class.

### --fixlocalvar (Optional, Default=no)
Fix local variable name \u2603(☃). This variable name is declared multiple time in same scope, so some decompiler does not work. There are three options.

|option|description|
|---|---|
|no|Do not fix local variable name|
|rename|Rename problematic local variable to debug$index like debug1, debug2...|
|delete|Delete problematic local varaible|
 

### --reobf (Optional)

Reobf option reverse mapping direction. By default, MC-Remapper map obfuscated code to deobfuscated code. But if you write your mod/plugin with deobfuscated minecraft source and apply it to forge/bukkit, minecraft will crash with NoClassDefFoundException, NoSuchMethodException, or else. That's because, forge or bukkit use obfuscated code but your plugin tried to access to deobfuscated code. In this case, you have to re-obfuscate your mod/plugin to use obfuscated minecraft code. You can use this option to map deobfuscated to. obfuscated code.

## Example usage

```
./MC-Remapper --mapping https://launcher.mojang.com/v1/objects/448ccb7b455f156bb5cb9cdadd7f96cd68134dbd/server.txt --input server.jar --output deobf.jar --thread 8 --fixlocalvar=delete
```

