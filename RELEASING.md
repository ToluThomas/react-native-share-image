# Releasing

This guide is for maintainers. A release has two parts, and they happen at different times:

| Part | What does it | When |
|---|---|---|
| **npm package** | `.github/workflows/publish.yml` | Automatically, when a merge to `main` changes the `version` in `package.json` |
| **GitHub release** (tag and release notes) | You, with `npm run release` | After the merge, once you push the tag |

## Prerequisites

- The [GitHub CLI](https://cli.github.com/) is installed and logged in (`gh auth login`).
- You have push access to the repository.

## Steps

### 1. Choose the version

Follow [Semantic Versioning](https://semver.org/):

- **Patch** (`2.0.0` → `2.0.1`): bug fixes, internal changes, tooling.
- **Minor** (`2.0.0` → `2.1.0`): new features that don't break existing code.
- **Major** (`2.0.0` → `3.0.0`): breaking changes, such as API changes or a higher minimum React Native, iOS or Android version.

### 2. Update the changelog and version on your branch

1. In `CHANGELOG.md`, rename `## [Unreleased]` to the new version and add today's date:

   ```markdown
   ## [2.0.1] - 2026-11-17
   ```

   Add a new, empty `## [Unreleased]` heading above it for future changes. At the bottom of the file, update the comparison links:

   ```markdown
   [Unreleased]: https://github.com/ToluThomas/react-native-share-image/compare/v2.0.1...HEAD
   [2.0.1]: https://github.com/ToluThomas/react-native-share-image/compare/v2.0.0...v2.0.1
   ```

2. Bump the version without letting npm make its own commit and tag:

   ```bash
   npm version 2.0.1 --no-git-tag-version
   ```

3. Commit both files, then open a pull request to `main`.

### 3. Merge the pull request

When the pull request is merged into `main`, the **Publish to npm** workflow publishes the new version. Check that it worked:

- In the repository's **Actions** tab, the workflow run is green.
- `npm view react-native-share-image version` prints the new version.

> Merging any change to the `version` field publishes a release. Only change `version` when you intend to release.

### 4. Tag the release

Tag the commit on `main` that contains the version bump:

```bash
git checkout main
git pull
git tag v2.0.1
git push origin v2.0.1
```

The tag must be `v` followed by the exact version in `package.json`.

### 5. Create the GitHub release

```bash
npm run release
```

This runs `scripts/release.sh`. The script reads the version from `package.json`, takes that version's section of `CHANGELOG.md` as the release notes, and runs `gh release create`.

The script stops with an error, and creates nothing, if:

- `CHANGELOG.md` has no entry for the version, or
- the tag hasn't been pushed to GitHub (`--verify-tag`).

The new release appears on the [Releases page](https://github.com/ToluThomas/react-native-share-image/releases) and is marked **Latest**.

## Fixing mistakes

- **Wrong release notes:** edit the release on GitHub, or run `gh release edit v2.0.1 --notes-file <file>`.
- **Tag on the wrong commit, before the release was created:**

  ```bash
  git tag -d v2.0.1
  git push origin :refs/tags/v2.0.1
  ```

  Then tag the correct commit and push the tag again.
- **A broken version was published to npm:** you can't publish the same version number twice. Publish a fixed patch version. You can also mark the broken one with `npm deprecate react-native-share-image@2.0.1 "<reason>"`.
