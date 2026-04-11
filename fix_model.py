with open('app/src/main/java/com/wled/app/data/model/WledModels.kt', 'r') as f:
    content = f.read()

if "val cct: Int =" not in content:
    content = content.replace("val intensity: Int = 128,", "val intensity: Int = 128,\n    val cct: Int = 128,")
    with open('app/src/main/java/com/wled/app/data/model/WledModels.kt', 'w') as f:
        f.write(content)
