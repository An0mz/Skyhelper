# SkyHelper

**SkyHelper** is a lightweight Fabric mod for **Hypixel SkyBlock** that enhances your client experience.

---
# For now we only have two features:

## Sea Lumies Highlighter
- **Automatic Highlighting**: Outlines all sea lumies in water when you’re on the Galatea map and submerged, making underwater navigation and resource gathering easier.  
- **Configurable**: Enable or disable the highlighter, choose your highlight color, and adjust opacity via the config file or mod menu.  
- **Performance‑Friendly**: Runs only when the config flag is enabled and the conditions (Galatea + submerged) are met.

## Scrollable Tooltip

- **Long Tooltip Support**: Converts any oversize item tooltip into a scrollable panel, so you can read full enchantment lists, lore text, or modded descriptions without cutting off content.  
- **Mouse‑Wheel Control**: Scroll tooltips with your mouse wheel and see a slim scroll bar indicator on the side.  
- **Universal Compatibility**: Works with vanilla and modded items alike—no extra setup required.

---

## ⚙️ Configuration (`config/skyhelper.json`)
**Edit using Mod Menu or by pressing i**
```json
{
  "seaLumies": {
    "enabled": true,
    "highlightColor": 16711680,
    "opacity": 0.5
  },
  "tooltip": {
    "scrollSpeed": 1.0
  }
}
```

- `seaLumies.enabled` (boolean): Toggle sea pickle highlighting.  
- `seaLumies.highlightColor` (hex): Color of the outline.  
- `seaLumies.opacity` (0.0–1.0): Transparency level.  
- `tooltip.scrollSpeed` (float): Multiplier for scroll wheel sensitivity.

---

## 🚀 Usage

1. **Install**: Place the `skyhelper.jar` in your `mods/` folder.  
2. **Launch**: Start Minecraft with Fabric Loader.  
3. **Enjoy**: Underwater highlights on Galatea and fully scrollable tooltips!

---

*SkyHelper by Anomz*  
