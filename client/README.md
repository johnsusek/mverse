# MVerse - Client

You don't need to follow these instruction if you just want to join MVerse servers. In that case
just download the .jar and put it in your mods folder.

If you want to develop/build the MVerse client, follow the below instructions:

## Set up
`./gradlew eclipse`
`./gradlew genVSCodeRuns`

## Develop
`./gradlew build`

## VSCode
Make sure to set something like this in your vscode runClient launch task:
`"args": "--username=YOUR_USERNAME --password=YOUR_PASSWORD"`

## Eclipse
Same as for VSCode, you need to pass --username and --password args
