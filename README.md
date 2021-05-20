![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.heartpattern.io%2Frepository%2Fmaven-public%2Fio%2Fheartpattern%2Fmcremapper%2Fmaven-metadata.xml) ![Jenkins](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.heartpattern.io%2Fjob%2FHeartPattern%2Fjob%2FMC-Remapper%2Fjob%2Fmaster%2F) ![GitHub top language](https://img.shields.io/github/languages/top/HeartPattern/MC-Remapper) ![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/HeartPattern/MC-Remapper) ![GitHub repo size](https://img.shields.io/github/repo-size/HeartPattern/MC-Remapper) ![GitHub](https://img.shields.io/github/license/HeartPattern/MC-Remapper) ![GitHub commit activity](https://img.shields.io/github/commit-activity/y/HeartPattern/MC-Remapper) ![GitHub last commit](https://img.shields.io/github/last-commit/HeartPattern/MC-Remapper)
# MC-Remapper

Deobfuscator for Minecraft

Mojang provide mapping file of obfuscated class, field, and method name for minecraft mod developer.
This program applies mapping file to original minecraft code. It does not decompile jar,
but only apply mapping. You have to decompile jar with your favorite decompiler after deobfuscate minecraft with mc-remapper.

# Compile
Java 11 is required to compile MC-Remapper.\
Clone project to your local machine and open terminal in the directory where build.gradle located.
Run following command to compile.

```
./gradlew installDist
```

Runnable script and runtime libraries will generate under `build/install/MC-Remapper`.

# Usage

Open terminal at `build/install/MC-Remapper/bin`. 
Execute MC-Remapper (Mac/Linux) or MC-Remapper.bat(Windows) with following parameters.

## Arguments
### Execute with specific file
To run mc remapper with a specific file, you can provide input jar and mapping txt. 

Input jar is path to file or url of obfuscated minecraft client or server.

Mapping txt is path to file or url of proguard's mapping txt file.  You can find it at `.minecraft/versions/$version$/$version$.json` file.

### Execute with version

To run mc remapper with automatically download artifact and mapping tt, you can provide artifact name and version id.

Artifact name is either server or client.

Version id is release version or snapshot version.


## Options

### --output or --output-name(Default=Generate from other arguments)

Path to output file. If file already exists, overwrite it.

### --thread (Default=8)
Number of thread used for apply mapping to class.

### --fixlocalvar (Default=no)
Fix local variable name \u2603(☃). 
This variable name declared multiple time in same scope, 
so some decompiler does not work. There are three options.

|option|description|
|---|---|
|no|Do not fix|
|rename|Rename problematic local variable to debug$index like debug1, debug2...|
|delete|Delete problematic local variable|


### --reobf (Flag, Default=no)

__This option is useless for now. Both forge and bukkit modify nms with their own mapping, therefore applying this option is meaningless__

Reobf option reverses mapping direction. By default, MC-Remapper map obfuscated code to deobfuscated code. However, if you write your mod/plugin with deobfuscated minecraft source and apply it to forge/bukkit, minecraft will crash with NoClassDefFoundException, NoSuchMethodException, or else. That's because, forge or bukkit use obfuscated code, but your plugin tried to access to deobfuscated code. In this case, you have to re-obfuscate your mod/plugin to use obfuscated minecraft code. You can
use this option to map deobfuscated to. obfuscated code.

For csrg/srg mappings, this option is needed to apply deobfuscating mapping. i.e. deobfuscating = --reobf, reobfuscating = no --reobf.


### --format (Default=proguard)

Supported mapping format: proguard, csrg, srg

### --autologger (Flag, default=no)

Static final fields with type org.apache.logging.log4j.Logger is automatically renamed to `LOGGER` if it is not explicitly renamed by the mapping.

### --autotoken (Flag, default=no)

Members' names that are Java keywords are automatically appended an underscore (_).

### --mappackage (Map, default=None)

Renames packages AFTER other mappings are applied. Can occur multiple times, where they are merged.

Example: --mappackage =net.minecraft.server.v1_15 --mappackage net.minecraft.server=net.minecraft.server.v1_15

The unnamed package is renamed to net.minecraft.server.v1_15, and net.minecraft.server is renamed to net.minecraft.server.v1_15.

# Example usages

## Execute by specifying input file and mapping url

```
./MC-Remapper server.jar https://launcher.mojang.com/v1/objects/448ccb7b455f156bb5cb9cdadd7f96cd68134dbd/server.txt
```

## Execute by specifying input and mapping file with options

```
./MC-Remapper server.jar server_mapping.txt --output deobf.jar --thread 8 --fixlocalvar=delete --reobf
```

## Execute by specifying version

```
./MC-Remapper server 1.15.2
```

