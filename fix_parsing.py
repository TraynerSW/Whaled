with open('app/src/main/java/com/wled/app/data/service/WledApiService.kt', 'r') as f:
    content = f.read()

if 'cct = seg.get("cct")?.asInt ?: 128' not in content:
    content = content.replace('intensity = seg.get("int")?.asInt ?: 128,\n                colors = colors,', 'intensity = seg.get("int")?.asInt ?: 128,\n                cct = seg.get("cct")?.asInt ?: 128,\n                colors = colors,')
    with open('app/src/main/java/com/wled/app/data/service/WledApiService.kt', 'w') as f:
        f.write(content)
