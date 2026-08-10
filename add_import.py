path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

import_statement = "import androidx.compose.foundation.layout.requiredWidth"
if import_statement not in content:
    content = content.replace("import androidx.compose.foundation.layout.width", "import androidx.compose.foundation.layout.width\n" + import_statement)
    with open(path, "w") as f:
        f.write(content)
    print("Import added")
else:
    print("Import already exists")
