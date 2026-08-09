import re

def main():
    with open('./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt', 'r') as f:
        content = f.read()

    # Find the ButtonGroup block dynamically from the phone layout
    button_group_start_str = "androidx.compose.material3.ButtonGroup("
    button_group_start = content.find(button_group_start_str)
    
    # We need to find the end of the ButtonGroup. Let's find the `Spacer(modifier = Modifier.height(32.dp))` after it
    # or `Spacer(modifier = Modifier.height(bottomSpacerHeight))`?
    # No, wait, in phone layout, after ButtonGroup there is some code. Let's just find the exact closing bracket of ButtonGroup.
    # To do this safely, we can count brackets.
    if button_group_start == -1:
        print("Error: Could not find ButtonGroup")
        return
        
    idx = button_group_start + len("androidx.compose.material3.ButtonGroup")
    open_brackets = 0
    in_group = False
    
    for i in range(idx, len(content)):
        char = content[i]
        if char == '(':
            open_brackets += 1
            in_group = True
        elif char == ')':
            open_brackets -= 1
        elif char == '{':
            open_brackets += 1
            in_group = True
        elif char == '}':
            open_brackets -= 1
            
        if in_group and open_brackets == 0:
            button_group_end = i + 1
            break
            
    button_group_code = content[button_group_start:button_group_end].strip()

    # The tablet layout uses this exact `Row` for controls:
    controls_row_start_str = """                            // Circular Playback Controls
                            Row(
                                modifier = Modifier.width(albumArtSize),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {"""
    
    controls_start = content.find(controls_row_start_str)
    if controls_start == -1:
        print("Error: Could not find Circular Playback Controls")
        return
        
    controls_end_str = """                                }
                            }"""
    controls_end = content.find(controls_end_str, controls_start) + len(controls_end_str)
    
    original_controls = content[controls_start:controls_end]
    
    # Replace the circular controls with the button group
    # But wait, we want the ButtonGroup to have width(albumArtSize) so it aligns nicely with the artwork!
    # Let's wrap the button group in a Box or replace its fillMaxWidth() with width(albumArtSize)
    # The ButtonGroup code has `.fillMaxWidth()`. We can replace it with `.width(albumArtSize)`
    
    tablet_button_group_code = button_group_code.replace(".fillMaxWidth()", ".width(albumArtSize)")
    
    new_controls = "                            // Reverted Playback Controls\n                            " + tablet_button_group_code.replace("\n", "\n                            ")
    
    content = content.replace(original_controls, new_controls)
    
    # Now for the Album Art padding.
    # We used:
    # Spacer(modifier = Modifier.height(tabletTopOffset - (statusBarTopDp + 68.dp)))
    # We will replace it with:
    # Spacer(modifier = Modifier.height(32.dp))
    
    old_spacer = "Spacer(modifier = Modifier.height(tabletTopOffset - (statusBarTopDp + 68.dp)))"
    new_spacer = "Spacer(modifier = Modifier.height(24.dp))"
    
    content = content.replace(old_spacer, new_spacer)
    
    with open('./app/src/main/java/com/codetrio/overdrive/ui/player/FullPlayer.kt', 'w') as f:
        f.write(content)
        
    print("Success")

if __name__ == "__main__":
    main()
