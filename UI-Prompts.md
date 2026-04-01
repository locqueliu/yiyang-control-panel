# Yiyang 安卓局域网控制工具 原型图提示词

> 视觉风格：现代极简（Minimalism）
> 配色方案：浅色系，white and soft gray background, dark text, teal accent
> 目标平台：移动端（Mobile application）

---

## 页面 1：设备发现页

**页面说明**：用户打开 App 后自动扫描局域网设备、查看设备列表并选择当前设备的主入口页面。

**提示词**：
```text
A modern mobile app screen for a local network desktop pet controller for Yiyang devices, device discovery screen, vertical mobile layout with sticky top app bar and current network status, top area shows page title, scan status text, refresh scan button, main content contains a list of rounded device cards with device name, product model, firmware version, IP address, serial number, online status badge, signal icon and primary select button, secondary section shows recently connected device cards and scan tips, clean spacing, clear hierarchy, minimalist design, white background, soft gray cards, dark text, teal accent color for primary actions and status highlights, subtle shadows, rounded corners, polished Android UI, UI/UX design, high fidelity mockup, 4K resolution, professional, Figma style, dribbble, behance
```

---

## 页面 2：控制主页

**页面说明**：用户选择设备后进行方向控制、动作控制、灯光控制、音量控制和系统操作的核心工作界面。

**提示词**：
```text
A modern mobile app screen for a Yiyang local control application, control dashboard screen, vertical mobile layout with top device status card and segmented content sections, top card shows selected device name, model, firmware version, IP address and online badge, below is a directional control panel with a cross-shaped arrow button cluster and a speed slider, next section is an action control grid with icon buttons for stand, sit, lie down, sleep, wag tail, wave, stretch, sway and bow, next section is a lamp control card with on off switch, mode chips, brightness slider, speed slider and compact color picker, bottom utility section contains volume slider, alarm shortcut card, reboot device button and enter Wi-Fi setup button, minimalist design, clean layout, ample white space, white background, soft gray modular cards, teal accent color, subtle shadows, rounded corners, premium Android app style, UI/UX design, high fidelity mockup, 4K resolution, professional, Figma style, dribbble, behance
```

---

## 页面 3：闹钟管理页

**页面说明**：用户查看、添加、编辑、启停和删除设备闹钟的页面。

**提示词**：
```text
A mobile app screen for a smart desktop pet controller, alarm management page, vertical layout with top navigation bar and floating add alarm button, main area shows a clean list of alarm cards, each card contains time, label, repeat days, ringtone type, action type, enabled toggle and quick edit delete actions, upper filter row shows all alarms, enabled, disabled tabs, bottom sheet style edit preview card is visible for one selected alarm, minimalist design, white background, light gray surfaces, dark text, teal accent for active toggles and primary actions, subtle dividers, rounded list cards, elegant Android productivity UI, UI/UX design, high fidelity mockup, 4K resolution, professional, Figma style, dribbble, behance
```

---

## 页面 4：舵机微调页

**页面说明**：用户读取各部位舵机当前微调值，并逐项拖动滑块后保存的调试页面。

**提示词**：
```text
A modern Android app screen for a robotic desktop pet controller, servo trim settings page, vertical mobile layout with compact top device info card and a list of servo adjustment modules, each module contains body part label, technical servo type tag, current trim value, wide horizontal slider from minus fifty to plus fifty, save button on the right, top area includes refresh angle button, empty state hint is hidden because a device is selected, clean engineering-focused interface, minimalist design, white background, soft gray cards, dark text, teal accent color for sliders and save buttons, subtle shadows, rounded corners, precise and professional control panel aesthetic, UI/UX design, high fidelity mockup, 4K resolution, professional, Figma style, dribbble, behance
```

---

## 页面 5：设备高级配置页

**页面说明**：用户修改全局音乐、风扇、双击模式、电量检测、LED 数量、主题、屏幕翻转等高级参数的表单页面。

**提示词**：
```text
A modern mobile app screen for a Yiyang device utility app, advanced device configuration page, long scroll vertical layout with multiple form sections and clear labeled controls, top section shows selected device summary, main content contains switch rows for global music, fan function and battery detection, segmented selector for double click mode, numeric stepper for LED count, theme selector cards, color picker rows for wallpaper time date and wakeup colors, dropdowns for PCB version and display type, segmented control for screen flip, every field has a short helper text below, sticky bottom action bar with refresh config button and save config button, minimalist design, clean layout, white background, soft gray panels, dark text, teal accent color, gentle shadows, rounded input cards, premium Android settings style, UI/UX design, high fidelity mockup, 4K resolution, professional, Figma style, dribbble, behance
```

---

## 页面 6：唤醒词配置页

**页面说明**：用户编辑唤醒词拼音、显示名称和识别阈值，并刷新或保存配置的页面。

**提示词**：
```text
A mobile app screen for a local AI wake word configuration tool inside a desktop pet controller app, wake word settings page, vertical mobile layout with top device status strip and centered form card, form contains pinyin wake word input field, display name input field, threshold slider with numeric value badge and sensitivity hint, action row with refresh config button and primary save config button, lower info card explains pinyin format, threshold recommendation and reboot notice, minimalist design, white background, pale gray cards, dark text, teal accent color, subtle shadows, rounded corners, friendly but technical Android UI, UI/UX design, high fidelity mockup, 4K resolution, professional, Figma style, dribbble, behance
```

---

## 页面 7：壁纸上传页

**页面说明**：用户预览当前壁纸、选择新图片、查看压缩进度并上传或重置壁纸的页面。

**提示词**：
```text
A modern Android app screen for a smart desktop pet wallpaper manager, wallpaper upload page, vertical layout with current wallpaper preview card at top, preview header contains reset to default button, middle section shows large image picker area with drag and tap upload style adapted for mobile, selected image preview thumbnail, compression progress bar, file size note, supported format chips for PNG GIF JPG WEBP, bottom area contains prominent upload wallpaper button and an info card with usage notes about 240 by 240 size and file limit, minimalist design, white background, soft gray containers, dark text, teal accent color, subtle shadows, rounded corners, polished mobile utility app style, UI/UX design, high fidelity mockup, 4K resolution, professional, Figma style, dribbble, behance
```

---

## 页面 8：音乐接口配置页

**页面说明**：用户新建或选择本地音乐配置，编辑 API 类型、接口地址、字段映射和请求参数映射，并一键同步到当前设备的页面。

**提示词**：
```text
A modern mobile app screen for an ESP32 music API configuration tool, music config editor page, vertical long-form mobile layout with sticky top bar showing current selected device and sync status, first section contains local configuration picker, new config button, import JSON button and export button, second section contains basic info fields for config name and remark, third section shows API type segmented control with RANDOM COMBINED SEPARATED options and URL input fields, next sections contain data path field, field mapping table cards, request parameter mapping editors, sample JSON paste box and parameter table paste box, bottom section shows a real time JSON preview panel and two sticky action buttons for test device connection and sync to current device, minimalist design, organized sections, white background, light gray cards, dark text, teal accent color, subtle shadows, rounded corners, bento-like modular grouping inside a minimal style, professional Android app UI, UI/UX design, high fidelity mockup, 4K resolution, professional, Figma style, dribbble, behance
```

---

## 页面 9：无设备空状态页

**页面说明**：用户首次打开 App 或扫描不到设备时的引导界面。

**提示词**：
```text
A mobile app empty state screen for a local network desktop pet controller, no device found page, vertical mobile layout with simple top app bar, center illustration of a cute robotic pet and Wi-Fi search signal, headline explains no devices found, subtext gives short troubleshooting tips for same network and hotspot mode, primary button says rescan devices, secondary button says view setup tips, bottom area shows a minimal recent device placeholder card and permission hint, minimalist design, calm and friendly atmosphere, white background, soft gray elements, dark text, teal accent buttons, subtle shadows, rounded corners, polished Android onboarding style, UI/UX design, high fidelity mockup, 4K resolution, professional, Figma style, dribbble, behance
```
