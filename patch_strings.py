import re

def update_strings(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    new_strings = """    <string name="setting_customize_bottom_nav">Customize Bottom Navigation</string>
    <string name="setting_customize_bottom_nav_desc">Change visible tabs and their order</string>
    <string name="customize_bottom_nav_title">Bottom Navigation</string>
    <string name="error_need_at_least_one_tab">At least one tab must be visible</string>
</resources>"""

    content = content.replace("</resources>", new_strings)

    with open(filepath, 'w') as f:
        f.write(content)

def update_strings_ja(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    new_strings = """    <string name="setting_customize_bottom_nav">ボトムナビゲーションのカスタマイズ</string>
    <string name="setting_customize_bottom_nav_desc">表示するタブとその順序を変更します</string>
    <string name="customize_bottom_nav_title">ボトムナビゲーション</string>
    <string name="error_need_at_least_one_tab">少なくとも1つのタブを表示する必要があります</string>
</resources>"""

    content = content.replace("</resources>", new_strings)

    with open(filepath, 'w') as f:
        f.write(content)

update_strings('app/src/main/res/values/strings.xml')
update_strings_ja('app/src/main/res/values-ja/strings.xml')

