# Releasing

Flatgen releases as Blockpilot does. Set `mod_version` in `flatgen/gradle.properties`, head the release's section of
`CHANGELOG.md` `## <version> (<date>)`, commit both as `chore(release): <version>`, and push a tag `v<version>`.

The `release` workflow (`.github/workflows/release.yml`) checks that the tag matches `mod_version` and that the
changelog has the section, runs everything the `build` workflow checks on the tagged commit (the pictures, every node
built with its tests and GameTests, every jar booted on real servers), and uploads the twelve jars to CurseForge with
[mod-publish-actions](https://github.com/sparten11740/mod-publish-actions). Once the uploads have gone through, it
creates the GitHub release with the jars. The notes on both are the changelog's section. A version ending in
`-alpha…` or `-beta…` goes out as that release type, anything else as a release.

The upload jobs run in the `release` environment, which holds the secret `CURSEFORGE_TOKEN` and the variable
`CURSEFORGE_PROJECT_ID`. When an upload fails, fix its cause and use Re-run failed jobs on the run: the uploads that
went through stay done, and the GitHub release follows.

A new node needs a row in the workflow's `curseforge` matrix: the Minecraft version its jar is named for, its loader
and Java version, and the game versions or loaders it runs on besides, where there are more.
