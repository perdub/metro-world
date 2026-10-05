#!/usr/bin/env python3
"""Download compatible server mods into the container context before image build."""
import hashlib
import json
from pathlib import Path
import time
from urllib.parse import urlencode, quote
from urllib.request import Request, urlopen

MC_VERSION = "1.21.1"
PROJECTS = {"fabric-api": "0.116.1+1.21.1", "lithium": None,
            "ferrite-core": None, "krypton": None}
ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / "server" / "mods"
API = "https://api.modrinth.com/v2"


def fetch(url):
    for attempt in range(3):
        try:
            request = Request(url, headers={"User-Agent": "MetroWorld-container-builder/1.0"})
            with urlopen(request, timeout=30) as response:
                return response.read()
        except OSError:
            if attempt == 2:
                raise
            time.sleep(1 + attempt)


def api(path):
    return json.loads(fetch(API + path))


def compatible(version):
    return MC_VERSION in version["game_versions"] and "fabric" in version["loaders"]


def resolve(project, number=None):
    query = urlencode({"loaders": json.dumps(["fabric"]),
                       "game_versions": json.dumps([MC_VERSION])})
    versions = api(f"/project/{quote(project, safe='')}/version?{query}")
    candidates = [v for v in versions if compatible(v) and
                  (v["version_number"] == number if number else v["version_type"] == "release")]
    if not candidates:
        raise RuntimeError(f"No compatible Fabric {MC_VERSION} release for {project} {number or ''}")
    return max(candidates, key=lambda v: v["date_published"])


def main():
    DEST.mkdir(parents=True, exist_ok=True)
    if not (DEST / "metro-world.jar").is_file():
        raise RuntimeError("Build and copy Metro World to server/mods/metro-world.jar first")
    manifest = {"minecraft": MC_VERSION, "loader": "fabric", "mods": []}
    selected = {p: resolve(p, n) for p, n in PROJECTS.items()}
    pending = list(selected.values())
    seen = set()
    # Clean only artifacts managed by a previous invocation, preserving the project's mod.
    lock = DEST / "server-mods.json"
    if lock.exists():
        for mod in json.loads(lock.read_text())["mods"]:
            name = mod["file"]
            if Path(name).name != name or name == "metro-world.jar":
                raise RuntimeError("Invalid managed filename")
            (DEST / name).unlink(missing_ok=True)
    while pending:
        version = pending.pop(0)
        if version["id"] in seen:
            continue
        if not compatible(version):
            raise RuntimeError(f"Incompatible dependency: {version['id']}")
        seen.add(version["id"])
        for dependency in version.get("dependencies", []):
            if dependency["dependency_type"] != "required":
                continue
            if dependency.get("version_id"):
                pending.append(api("/version/" + quote(dependency["version_id"], safe="")))
            elif dependency.get("project_id"):
                project_id = dependency["project_id"]
                existing = next((v for v in selected.values() if v["project_id"] == project_id), None)
                resolved = existing or resolve(project_id)
                selected[project_id] = resolved
                pending.append(resolved)
            else:
                raise RuntimeError("Required dependency has no resolvable identifier")
        files = [f for f in version["files"] if f.get("primary")]
        file = files[0] if files else version["files"][0]
        name = file["filename"]
        if Path(name).name != name or not name.endswith(".jar") or name == "metro-world.jar":
            raise RuntimeError(f"Invalid mod filename: {name}")
        content = fetch(file["url"])
        checksum = hashlib.sha512(content).hexdigest()
        if checksum != file["hashes"]["sha512"]:
            raise RuntimeError(f"Checksum mismatch for {name}")
        (DEST / name).write_bytes(content)
        manifest["mods"].append({"project": version["project_id"], "version_id": version["id"],
                                  "version": version["version_number"], "file": name,
                                  "sha512": checksum})
        print(f"Bundled {name}")
    lock.write_text(json.dumps(manifest, indent=2) + "\n")


if __name__ == "__main__":
    main()
