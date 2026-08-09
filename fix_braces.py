import re

file_path = "app/src/main/java/com/codetrio/overdrive/MainActivity.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """                                )
                            }
                            }
                        }
                    }
                ) { paddingValues ->"""

replacement = """                                )
                            }
                            }
                        }
                        }
                    }
                ) { paddingValues ->"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Fixed braces")
else:
    print("Could not find target to fix braces")
