# mclogs-fabric
A fabric mod to easily share and analyse your server logs with [mclo.gs](https://mclo.gs)

This mod requires the [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
### commands:
    /mclogs
Upload your current log to mclogs
    
    /mclogs list
List all available log files

    /mclogs share <filename>
Share a specific log file

All commands require the permission level 2

### Developing
This mod uses the [mclogs-java](https://github.com/aternosorg/mclogs-java) library.
You need to run the following command to add it to the project:
`git submodule init && git submodule update`