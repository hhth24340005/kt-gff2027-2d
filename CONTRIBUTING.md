# Contributing

## Releasing

Versions follow `v26.<minor>.<patch>`, where `<minor>` starts at 1 and `<patch>` starts at 0. Pushing a tag runs CI, builds the application images for Linux x64, macOS arm64, and Windows x64, and publishes them to [Releases](https://github.com/hhth24340005/kt-gff2027-2d/releases):

```bash
jj tag set v26.1.0 -r main
jj git push --tag v26.1.0
```
