import re, os, sys

ROOT = r"c:/Users/ritwi/Desktop/HRMS/hrms"
files = []
for root, dirs, names in os.walk(ROOT):
    rs = root.replace("\\", "/")
    if "/target/" in rs or "node_modules" in rs or "/.audit" in rs:
        continue
    for n in names:
        if n.endswith("Controller.java") and "/main/" in (rs + "/" + n):
            files.append(os.path.join(root, n))

all_eps = []
per_ctl = {}
for f in files:
    try:
        with open(f, "r", encoding="utf-8", errors="ignore") as fh:
            src = fh.read()
    except Exception:
        continue
    base = ""
    m = re.search(r'@RequestMapping\s*\(\s*[\"\']([^\"\']+)[\"\']', src)
    if m:
        base = m.group(1)
    else:
        m2 = re.search(r'@RequestMapping\s*\(\s*(?:path|value)\s*=\s*[\"\']([^\"\']+)[\"\']', src)
        if m2:
            base = m2.group(1)
    eps = set()
    for mm in re.finditer(r'@(Get|Post|Put|Delete|Patch)Mapping(?:\s*\(\s*[\"\']([^\"\']*)[\"\'])?', src):
        verb = mm.group(1).upper()
        sub = mm.group(2) or ""
        path = (base.rstrip("/") + "/" + sub.lstrip("/")) if sub else base
        if not path.startswith("/"):
            path = "/" + path
        eps.add(verb + " " + path)
    for mm in re.finditer(r'@(Get|Post|Put|Delete|Patch)Mapping\s*\(\s*(?:path|value)\s*=\s*[\"\']([^\"\']*)[\"\']', src):
        verb = mm.group(1).upper()
        sub = mm.group(2)
        path = (base.rstrip("/") + "/" + sub.lstrip("/")) if sub else base
        if not path.startswith("/"):
            path = "/" + path
        eps.add(verb + " " + path)
    per_ctl[f] = eps
    all_eps.extend(eps)

unique = sorted(set(all_eps))
print("Controllers:", len(files), "Endpoints:", len(unique))
with open(r"c:/Users/ritwi/Desktop/HRMS/hrms/.audit/backend_endpoints.txt", "w", encoding="utf-8") as out:
    for e in unique:
        out.write(e + "\n")
with open(r"c:/Users/ritwi/Desktop/HRMS/hrms/.audit/backend_by_controller.txt", "w", encoding="utf-8") as out:
    for f, eps in sorted(per_ctl.items()):
        out.write("\n# " + f + "\n")
        for e in sorted(eps):
            out.write(e + "\n")
