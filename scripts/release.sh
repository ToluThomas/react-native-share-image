#!/usr/bin/env sh
# Creates a GitHub release for the current package.json version, using that
# version's section of CHANGELOG.md as the release notes.
#
# Before running: merge the version bump to main, then tag it and push the tag:
#   git tag vX.Y.Z && git push origin vX.Y.Z
set -eu

cd "$(dirname "$0")/.."

VERSION=$(node -p "require('./package.json').version")
TAG="v$VERSION"

# Lines under "## [X.Y.Z]" up to the next "## " heading.
NOTES=$(awk -v v="$VERSION" '/^## /{p = index($0, "[" v "]") > 0; next} p' CHANGELOG.md)

if [ -z "$(printf '%s' "$NOTES" | tr -d '[:space:]')" ]; then
  echo "error: CHANGELOG.md has no entry for [$VERSION]. Add one (or rename [Unreleased]) first." >&2
  exit 1
fi

# --verify-tag stops gh from creating the tag itself if it hasn't been pushed.
gh release create "$TAG" --verify-tag --title "$TAG" --notes "$NOTES"
