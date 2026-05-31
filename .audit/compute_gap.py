import re

def norm(path: str) -> str:
    # collapse any {placeholder} -> {x}
    return re.sub(r"\{[^}/]+\}", "{x}", path)

backend = set()
with open(r"c:/Users/ritwi/Desktop/HRMS/hrms/.audit/backend_endpoints.txt", "r", encoding="utf-8") as f:
    for line in f:
        line = line.strip()
        if not line: continue
        v, p = line.split(" ", 1)
        backend.add(v + " " + norm(p))

frontend = set()
with open(r"c:/Users/ritwi/Desktop/HRMS/hrms/.audit/frontend_calls.txt", "r", encoding="utf-8") as f:
    for line in f:
        line = line.strip()
        if not line: continue
        v, p = line.split(" ", 1)
        frontend.add(v + " " + norm(p))

# A backend endpoint is "covered" if frontend has same verb+path OR same path with any verb
missing = sorted(backend - frontend)
print(f"Backend: {len(backend)}  Frontend: {len(frontend)}  Missing: {len(missing)}")

# Group by module (first 3 path segments after /api/v1/ or /api/)
from collections import defaultdict
by_mod = defaultdict(list)
for e in missing:
    v, p = e.split(" ", 1)
    parts = [x for x in p.split("/") if x]
    # determine module: skip api/v1
    if parts and parts[0] == "api":
        if len(parts) > 1 and parts[1] in ("v1", "v2", "public"):
            mod = parts[2] if len(parts) > 2 else "root"
        else:
            mod = parts[1] if len(parts) > 1 else "root"
    elif parts:
        mod = parts[0]
    else:
        mod = "root"
    by_mod[mod].append(e)

with open(r"c:/Users/ritwi/Desktop/HRMS/hrms/.audit/gap.txt", "w", encoding="utf-8") as out:
    out.write(f"BACKEND={len(backend)} FE={len(frontend)} MISSING={len(missing)}\n\n")
    for mod, eps in sorted(by_mod.items(), key=lambda kv: -len(kv[1])):
        out.write(f"## {mod}  ({len(eps)})\n")
        for e in eps:
            out.write(f"  {e}\n")
        out.write("\n")
print("wrote .audit/gap.txt")
