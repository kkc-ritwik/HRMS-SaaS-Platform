import re, os
ROOT = r"c:/Users/ritwi/Desktop/HRMS/hrms/hrms-frontend/src"
files = []
for root, dirs, names in os.walk(ROOT):
    rs = root.replace("\\", "/")
    if "node_modules" in rs:
        continue
    for n in names:
        if n.endswith(".ts") or n.endswith(".tsx"):
            files.append(os.path.join(root, n))

calls = set()
PATTERNS = [
    # api.<verb>(path)
    (r'\bapi\.(get|post|put|delete|patch)\s*\(\s*[`\'\"]([^`\'\"]+)[`\'\"]', 1, 2),
    # GET(path) / POST(path) helpers
    (r"\b(GET|POST|PUT|DELETE|PATCH)\s*\(\s*[`\'\"]([^`\'\"]+)[`\'\"]", 1, 2),
    # bare G(path)/P(path)/PU(path)/PA(path)/D(path) helpers used in catalog.ts
    (r"\b([GPD]|PU|PA)\s*(?:<[^>]+>)?\s*\(\s*[`\'\"]([^`\'\"]+)[`\'\"]", 1, 2),
]

verb_map = {
    "G": "GET", "P": "POST", "PU": "PUT", "PA": "PATCH", "D": "DELETE",
    "get": "GET", "post": "POST", "put": "PUT", "delete": "DELETE", "patch": "PATCH",
    "GET": "GET", "POST": "POST", "PUT": "PUT", "DELETE": "DELETE", "PATCH": "PATCH",
}

for f in files:
    try:
        src = open(f, "r", encoding="utf-8", errors="ignore").read()
    except Exception:
        continue
    for pat, vidx, pidx in PATTERNS:
        for m in re.finditer(pat, src):
            v_raw = m.group(vidx)
            verb = verb_map.get(v_raw, v_raw.upper())
            path = m.group(pidx).split("?")[0].strip()
            if not path.startswith("/"):
                path = "/" + path
            calls.add(verb + " " + path)

# normalise: replace ${var} and {id} with {x}
norm = set()
for c in calls:
    v, p = c.split(" ", 1)
    p = re.sub(r"\$\{[^}]+\}", "{x}", p)
    p = re.sub(r"\{[^}/]+\}", "{x}", p)
    norm.add(v + " " + p)

print("Frontend api calls:", len(norm))
with open(r"c:/Users/ritwi/Desktop/HRMS/hrms/.audit/frontend_calls.txt", "w", encoding="utf-8") as out:
    for e in sorted(norm):
        out.write(e + "\n")
