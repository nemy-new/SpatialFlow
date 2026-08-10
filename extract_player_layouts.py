import re

path = "app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt"
with open(path, "r") as f:
    content = f.read()

# I will check if the file is already refactored
if "fun FullPlayerPhoneLayout(" in content:
    print("Already refactored.")
    exit(0)

# The goal is to move the Phone and Tablet specific layouts to separate functions.
# However, given the number of local variables, this might be very difficult to do with regex alone.
# Let's inspect the `if (isTablet)` block.
