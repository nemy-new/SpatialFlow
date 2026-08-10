path2 = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path2, "r") as f:
    content = f.read()

target = """                                    isEmbedded = true,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }"""

replacement = """                                    isEmbedded = true,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        }
                    }"""

content = content.replace(target, replacement)

with open(path2, "w") as f:
    f.write(content)
