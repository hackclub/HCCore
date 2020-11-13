# HCCore [![Build Status](https://travis-ci.com/hackclub/HCCore.svg?branch=master)](https://travis-ci.com/hackclub/HCCore)

Main plugin for the Hack Club Minecraft server.

## Contributing

```sh
git clone https://github.com/hackclub/HCCore
cd HCCore

# build with checks and formatting
./gradlew build

# build without some checks and formatting
./gradlew build -x spotlessApply -x spotlessCheck -x pmdMain -x pmdTest -x spotbugsMain -x spotBugsTest
```
## License

[MIT License](LICENSE.txt)
